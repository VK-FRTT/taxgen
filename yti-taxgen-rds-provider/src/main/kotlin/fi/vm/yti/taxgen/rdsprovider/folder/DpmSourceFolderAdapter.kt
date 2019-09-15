package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.config.ConfigFactory
import fi.vm.yti.taxgen.rdsprovider.config.DpmSourceConfigHolder
import fi.vm.yti.taxgen.rdsprovider.helpers.SortOps
import java.nio.file.Path

internal class DpmSourceFolderAdapter(
    dpmSourceRootPath: Path,
    val diagnostic: Diagnostic
) : DpmSource {

    private val dpmSourceRootPath = dpmSourceRootPath.toAbsolutePath().normalize()

    private val dpmSourceConfigHolder: DpmSourceConfigHolder by lazy {
        ConfigFactory.configFromFile(
            dpmSourceRootPath.resolve("meta/source_config.json"),
            diagnostic
        )
    }

    override fun contextLabel(): String = "folder"
    override fun contextIdentifier(): String = dpmSourceRootPath.toString()
    override fun config(): DpmSourceConfigHolder = dpmSourceConfigHolder

    override fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit) {
        val paths = FileOps.listSubFoldersMatching(dpmSourceRootPath, "dpm_dictionary_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)

        return sortedPaths.forEach { path ->
            val dictionarySource = DpmDictionarySourceFolderAdapter(path)
            action(dictionarySource)
        }
    }
}
