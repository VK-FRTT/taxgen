package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.commons.ops.JsonOps
import fi.vm.yti.taxgen.commons.ops.PathStack
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.CaptureInfo
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.DpmSourceRecorder
import java.nio.file.Path
import java.time.Instant

internal class DpmSourceRecorderFolderAdapter(
    outputFolderPath: Path,
    private val forceOverwrite: Boolean,
    private val diagnostic: Diagnostic
) : DpmSourceRecorder {

    private val outputFolderPath = outputFolderPath.toAbsolutePath().normalize()

    override fun contextLabel(): String = "folder"
    override fun contextIdentifier(): String = outputFolderPath.toString()

    override fun captureSources(dpmSource: DpmSource) {
        val pathStack = PathStack(
            baseFolderPath = outputFolderPath,
            createFileSystemPaths = true,
            diagnostic = diagnostic
        )

        captureDpmSource(dpmSource, pathStack)
    }

    private fun captureDpmSource(
        dpmSource: DpmSource,
        pathStack: PathStack
    ) {
        pathStack.withSubfolder("meta") {

            FileOps.writeTextFile(
                captureInfoData(),
                pathStack,
                "capture_info.json",
                forceOverwrite,
                diagnostic
            )

            FileOps.writeTextFile(
                dpmSource.config().configData,
                pathStack,
                "source_config.json",
                forceOverwrite,
                diagnostic
            )
        }

        var index = 0
        dpmSource.eachDpmDictionarySource {
            captureDpmDictionarySources(
                it,
                index,
                pathStack
            )

            index++
        }
    }

    private fun captureDpmDictionarySources(
        dictionarySource: DpmDictionarySource,
        dictionaryIndex: Int,
        pathStack: PathStack
    ) {
        pathStack.withSubfolder("dpm_dictionary_$dictionaryIndex") {

            dictionarySource.dpmOwnerConfigData {
                FileOps.writeTextFile(
                    it,
                    pathStack,
                    "dpm_owner_config.json",
                    forceOverwrite,
                    diagnostic
                )
            }

            dictionarySource.metricsSource {
                captureCodeListSource(
                    it,
                    "met",
                    pathStack
                )
            }

            dictionarySource.explicitDomainsAndHierarchiesSource {
                captureCodeListSource(
                    it,
                    "exp_dom_hier",
                    pathStack
                )
            }

            dictionarySource.explicitDimensionsSource {
                captureCodeListSource(
                    it,
                    "exp_dim",
                    pathStack
                )
            }

            dictionarySource.typedDomainsSource {
                captureCodeListSource(
                    it,
                    "typ_dom",
                    pathStack
                )
            }

            dictionarySource.typedDimensionsSource {
                captureCodeListSource(
                    it,
                    "typ_dim",
                    pathStack
                )
            }
        }
    }

    private fun captureCodeListSource(
        codeListSource: CodeListSource?,
        conceptFolderName: String,
        pathStack: PathStack
    ) {
        if (codeListSource == null) {
            return
        }
        pathStack.withSubfolder(conceptFolderName) {

            FileOps.writeTextFile(
                codeListSource.codeListMetaData(),
                pathStack,
                "code_list_meta.json",
                forceOverwrite,
                diagnostic
            )

            var codePageIndex = 0
            codeListSource.eachCodePageData { pageData ->
                FileOps.writeTextFile(
                    pageData,
                    pathStack,
                    "codes_page_$codePageIndex.json",
                    forceOverwrite,
                    diagnostic
                )
                codePageIndex++
            }

            captureExtensionSources(
                codeListSource,
                pathStack
            )

            captureSubCodeListSources(
                codeListSource,
                pathStack
            )
        }
    }

    private fun captureExtensionSources(
        codeListSource: CodeListSource,
        pathStack: PathStack
    ) {
        var extensionIndex = 0
        codeListSource.eachExtensionSource { extensionSource ->

            pathStack.withSubfolder("extension_$extensionIndex") {

                FileOps.writeTextFile(
                    extensionSource.extensionMetaData(),
                    pathStack,
                    "extension_meta.json",
                    forceOverwrite,
                    diagnostic
                )

                var extensionMemberPageIndex = 0
                extensionSource.eachExtensionMemberPageData { pageData ->
                    FileOps.writeTextFile(
                        pageData,
                        pathStack,
                        "members_page_$extensionMemberPageIndex.json",
                        forceOverwrite,
                        diagnostic
                    )

                    extensionMemberPageIndex++
                }
            }

            extensionIndex++
        }
    }

    private fun captureSubCodeListSources(
        codeListSource: CodeListSource,
        pathStack: PathStack
    ) {
        var subCodeListIndex = 0
        codeListSource.eachSubCodeListSource { subCodeListSource ->

            captureCodeListSource(subCodeListSource, "sub_code_list_$subCodeListIndex", pathStack)

            subCodeListIndex++
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
