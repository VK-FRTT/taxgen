package fi.vm.yti.taxgen.sqliteprovider.dictionarycreate

import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.sqliteprovider.DpmDbWriter
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbDictionaries
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbFixedEntities
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbLanguages
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbOwners
import fi.vm.yti.taxgen.sqliteprovider.helpers.ModelTransformer
import fi.vm.yti.taxgen.sqliteprovider.helpers.SqliteOps
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class DictionaryCreateDbWriter(
    outputDbPath: Path,
    private val forceOverwrite: Boolean,
    private val diagnostic: Diagnostic
) : DpmDbWriter {
    private val outputDbPath: Path = outputDbPath.toAbsolutePath().normalize()

    override fun writeModel(
        dpmModel: DpmModel,
        processingOptions: ProcessingOptions
    ) {
        val updatedDpmModel = ModelTransformer.transformDpmModelByProcessingOptions(
            dpmModel,
            processingOptions,
            diagnostic
        )

        initOutputDbFile()

        SqliteOps.connectDatabase(outputDbPath)

        doWriteModel(updatedDpmModel, processingOptions)
    }

    override fun outputPath(): Path = outputDbPath

    private fun initOutputDbFile() {
        FileOps.deleteConflictingOutputFileIfAllowed(outputDbPath, forceOverwrite)
        FileOps.failIfOutputFileExists(outputDbPath, diagnostic)
        FileOps.createIntermediateFolders(outputDbPath)

        val stream = this::class.java.getResourceAsStream("/dm_database_seed.db")
        Files.copy(stream, outputDbPath, StandardCopyOption.REPLACE_EXISTING)
    }

    private fun doWriteModel(
        dpmModel: DpmModel,
        processingOptions: ProcessingOptions
    ) {
        DbLanguages.configureLanguages()
        val languageIds = DbLanguages.resolveLanguageIds()

        DbDictionaries.purgeDictionaryContent()

        val dictionaryAndOwnerIds = dpmModel.dictionaries.map {
            val ownerId = DbOwners.writeOwner(it.owner)

            DbDictionaries.writeDictionaryBaseParts(
                it,
                ownerId,
                languageIds,
                processingOptions
            )

            Pair(it, ownerId)
        }

        val fixedEntitiesLookupItem = DbFixedEntities.writeFixedEntities(
            languageIds,
            diagnostic
        )

        dictionaryAndOwnerIds.forEach { (dictionary, ownerId) ->
            DbDictionaries.writeDictionaryMetricsToFixedDomain(
                dictionary,
                ownerId,
                fixedEntitiesLookupItem.metricDomainId,
                languageIds,
                processingOptions
            )
        }
    }
}
