package fi.vm.yti.taxgen.rdsource.folder

import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.rdsource.DpmDictionarySource
import fi.vm.yti.taxgen.rdsource.DpmSource
import fi.vm.yti.taxgen.rdsource.DpmSourceConfigHolder
import fi.vm.yti.taxgen.rdsource.configdata.ConfigFactory
import fi.vm.yti.taxgen.rdsource.helpers.SortOps
import java.nio.file.Path

internal class DpmSourceFolderAdapter(
    dpmSourceRootPath: Path,
    val diagnosticContext: DiagnosticContext
) : DpmSource {

    private val dpmSourceRootPath = dpmSourceRootPath.toAbsolutePath().normalize()

    private val dpmSourceConfigHolder: DpmSourceConfigHolder by lazy {
        ConfigFactory.dpmSourceConfigFromFile(
            dpmSourceRootPath.resolve("meta/source_config.json"),
            diagnosticContext
        )
    }

    override fun contextTitle(): String = "folder"
    override fun contextIdentifier(): String = dpmSourceRootPath.toString()
    override fun config(): DpmSourceConfigHolder = dpmSourceConfigHolder

    override fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit) {
        val paths = FileOps.listSubFoldersMatching(dpmSourceRootPath, "dpm_dictionary_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)

        return sortedPaths.forEach { path ->
            val dictionarySource = DpmDictionarySourceFolderAdapter(path, diagnosticContext)
            action(dictionarySource)
        }
    }
}
