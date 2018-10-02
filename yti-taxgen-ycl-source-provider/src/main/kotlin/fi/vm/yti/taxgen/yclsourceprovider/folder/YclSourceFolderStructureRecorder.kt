package fi.vm.yti.taxgen.yclsourceprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.PathStack
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.yclsourceprovider.CaptureInfo
import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistExtensionSource
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.yclsourceprovider.YclSourceRecorder
import java.nio.file.Path
import java.time.Instant

class YclSourceFolderStructureRecorder(
    baseFolderPath: Path,
    private val forceOverwrite: Boolean,
    private val diagnostic: Diagnostic
) : YclSourceRecorder {

    private val baseFolderPath = baseFolderPath.toAbsolutePath().normalize()

    override fun contextLabel(): String = "folder"
    override fun contextIdentifier(): String = baseFolderPath.toString()

    override fun captureSources(yclSource: YclSource) {
        val pathStack = PathStack(
            baseFolderPath = baseFolderPath,
            createFileSystemPaths = true,
            diagnostic = diagnostic
        )

        diagnostic.withContext(this) {
            doCaptureYclSources(yclSource, pathStack)
        }
    }

    private fun doCaptureYclSources(
        yclSource: YclSource,
        pathStack: PathStack
    ) {
        diagnostic.withContext(yclSource) {

            pathStack.withSubfolder("meta") {

                FileOps.writeTextFile(
                    captureInfoData(),
                    pathStack,
                    "capture_info.json",
                    forceOverwrite,
                    diagnostic
                )

                FileOps.writeTextFile(
                    yclSource.sourceConfigData(),
                    pathStack,
                    "source_config.json",
                    forceOverwrite,
                    diagnostic
                )
            }

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

            diagnostic.withContext(dictionarySource) {

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

    private fun captureCodelistSources(
        codelistSources: List<YclCodelistSource>,
        pathStack: PathStack
    ) {
        codelistSources.withIndex().forEach { (listIndex, codelistSource) ->

            diagnostic.withContext(codelistSource) {

                pathStack.withIndexPostfixSubfolder("codelist", listIndex) {

                    FileOps.writeTextFile(
                        codelistSource.yclCodelistSourceConfigData(),
                        pathStack,
                        "ycl_codelist_source_config.json",
                        forceOverwrite,
                        diagnostic
                    )

                    FileOps.writeTextFile(
                        codelistSource.yclCodeSchemeData(),
                        pathStack,
                        "ycl_codescheme.json",
                        forceOverwrite,
                        diagnostic
                    )

                    codelistSource.yclCodePagesData().withIndex().forEach { (index, pageData) ->
                        FileOps.writeTextFile(
                            pageData,
                            pathStack,
                            "ycl_codes_page_$index.json",
                            forceOverwrite,
                            diagnostic
                        )
                    }

                    captureCodelistExtensionSources(
                        codelistSource.yclCodelistExtensionSources(),
                        pathStack
                    )
                }
            }
        }
    }

    private fun captureCodelistExtensionSources(
        codelistExtensionSources: List<YclCodelistExtensionSource>,
        pathStack: PathStack
    ) {
        codelistExtensionSources.withIndex().forEach { (listIndex, codelistExtensionSource) ->

            diagnostic.withContext(codelistExtensionSource) {

                pathStack.withIndexPostfixSubfolder("extension", listIndex) {

                    FileOps.writeTextFile(
                        codelistExtensionSource.yclExtensionData(),
                        pathStack,
                        "ycl_extension.json",
                        forceOverwrite,
                        diagnostic
                    )

                    codelistExtensionSource.yclExtensionMemberPagesData().withIndex().forEach { (index, pageData) ->
                        FileOps.writeTextFile(
                            pageData,
                            pathStack,
                            "ycl_extension_members_page_$index.json",
                            forceOverwrite,
                            diagnostic
                        )
                    }
                }
            }
        }
    }

    override fun close() {}

    private fun captureInfoData(): String {
        val info = CaptureInfo(
            createdAt = Instant.now().toString()
        )

        return JsonOps.writeAsJsonString(info)
    }
}
