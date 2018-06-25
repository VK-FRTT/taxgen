package fi.vm.yti.taxgen.yclsourceprovider.folder

import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.yclsourceprovider.YclSourceRecorder
import fi.vm.yti.taxgen.yclsourceprovider.helpers.FileOps
import fi.vm.yti.taxgen.yclsourceprovider.helpers.PathStack
import java.nio.file.Path

class YclSourceFolderStructureRecorder(
    baseFolderPath: Path,
    private val yclSource: YclSource,
    private val forceOverwrite: Boolean
) : YclSourceRecorder {

    private val baseFolderPath = baseFolderPath.toAbsolutePath().normalize()

    override fun capture() {
        val pathStack = createPathStack()

        FileOps.writeTextFile(yclSource.sourceInfoData(), pathStack, "source_info.json", forceOverwrite)
        captureDpmDictionarySources(yclSource.dpmDictionarySources(), pathStack)
    }

    private fun createPathStack(): PathStack {
        return PathStack(
            baseFolderPath = baseFolderPath,
            createFileSystemPaths = true
        )
    }

    private fun captureDpmDictionarySources(yclDpmDictionarySources: List<DpmDictionarySource>, pathStack: PathStack) {
        yclDpmDictionarySources.withIndex().forEach { (dictionaryIndex, unit) ->

            pathStack.withIndexPostfixSubfolder("dpmdictionary", dictionaryIndex) {

                FileOps.writeTextFile(unit.dpmOwnerInfoData(), pathStack, "dpm_owner_info.json", forceOverwrite)
                captureCodelistSources(unit.yclCodelistSources(), pathStack)
            }
        }
    }

    private fun captureCodelistSources(codelistSources: List<YclCodelistSource>, pathStack: PathStack) {
        codelistSources.withIndex().forEach { (listIndex, list) ->

            pathStack.withIndexPostfixSubfolder("codelist", listIndex) {

                FileOps.writeTextFile(list.yclCodeschemeData(), pathStack, "ycl_codescheme.json", forceOverwrite)

                list.yclCodePagesData().withIndex().forEach { (index, pageData) ->
                    FileOps.writeTextFile(pageData, pathStack, "ycl_codepage_$index.json", forceOverwrite)
                }
            }
        }
    }

    override fun close() {}
}
