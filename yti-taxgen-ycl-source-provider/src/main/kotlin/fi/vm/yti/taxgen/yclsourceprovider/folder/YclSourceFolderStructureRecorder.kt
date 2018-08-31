package fi.vm.yti.taxgen.yclsourceprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.PathStack
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.yclsourceprovider.YclSourceRecorder
import java.nio.file.Path

class YclSourceFolderStructureRecorder(
    baseFolderPath: Path,
    private val forceOverwrite: Boolean,
    private val diagnostic: Diagnostic
) : YclSourceRecorder {

    private val baseFolderPath = baseFolderPath.toAbsolutePath().normalize()

    override fun topicName(): String = "folder"
    override fun topicIdentifier(): String = baseFolderPath.toString()

    override fun captureSources(yclSource: YclSource) {
        val pathStack = PathStack(
            baseFolderPath = baseFolderPath,
            createFileSystemPaths = true,
            diagnostic = diagnostic
        )

        diagnostic.withinTopic(this) {
            doCaptureYclSources(yclSource, pathStack)
        }
    }

    private fun doCaptureYclSources(
        yclSource: YclSource,
        pathStack: PathStack
    ) {
        diagnostic.withinTopic(yclSource) {

            FileOps.writeTextFile(
                yclSource.sourceInfoData(),
                pathStack,
                "source_info.json",
                forceOverwrite,
                diagnostic
            )

            captureDpmDictionarySources(
                yclSource.dpmDictionarySources(),
                pathStack
            )
        }
    }

    private fun captureDpmDictionarySources(
        yclDpmDictionarySources: List<DpmDictionarySource>,
        pathStack: PathStack
    ) {
        yclDpmDictionarySources.withIndex().forEach { (dictionaryIndex, dictionarySource) ->

            diagnostic.withinTopic(dictionarySource) {

                pathStack.withIndexPostfixSubfolder("dpmdictionary", dictionaryIndex) {

                    FileOps.writeTextFile(
                        dictionarySource.dpmOwnerConfigData(),
                        pathStack,
                        "dpm_owner_info.json",
                        forceOverwrite,
                        diagnostic
                    )
                    captureCodelistSources(
                        dictionarySource.yclCodelistSources(),
                        pathStack
                    )
                }
            }
        }
    }

    private fun captureCodelistSources(codelistSources: List<YclCodelistSource>, pathStack: PathStack) {
        codelistSources.withIndex().forEach { (listIndex, codelistSource) ->

            diagnostic.withinTopic(codelistSource) {

                pathStack.withIndexPostfixSubfolder("codelist", listIndex) {

                    FileOps.writeTextFile(
                        codelistSource.yclCodeschemeData(),
                        pathStack,
                        "ycl_codescheme.json",
                        forceOverwrite,
                        diagnostic
                    )

                    codelistSource.yclCodePagesData().withIndex().forEach { (index, pageData) ->
                        FileOps.writeTextFile(
                            pageData,
                            pathStack,
                            "ycl_codepage_$index.json",
                            forceOverwrite,
                            diagnostic
                        )
                    }
                }
            }
        }
    }

    override fun close() {}
}
