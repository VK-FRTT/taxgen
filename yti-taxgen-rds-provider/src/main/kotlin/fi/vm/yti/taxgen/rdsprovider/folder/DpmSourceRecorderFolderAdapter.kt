package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.PathStack
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.CaptureInfo
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.DpmSourceRecorder
import fi.vm.yti.taxgen.rdsprovider.SourceProvider
import java.nio.file.Path
import java.time.Instant

internal class DpmSourceRecorderFolderAdapter(
    baseFolderPath: Path,
    private val forceOverwrite: Boolean,
    private val diagnostic: Diagnostic
) : DpmSourceRecorder {

    private val baseFolderPath = baseFolderPath.toAbsolutePath().normalize()

    override fun contextLabel(): String = "folder"
    override fun contextIdentifier(): String = baseFolderPath.toString()

    override fun captureSources(sourceProvider: SourceProvider) {
        val pathStack = PathStack(
            baseFolderPath = baseFolderPath,
            createFileSystemPaths = true,
            diagnostic = diagnostic
        )

        sourceProvider.withDpmSource { dpmSource ->
            captureDpmSource(dpmSource, pathStack)
        }
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
                dpmSource.sourceConfigData(),
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
        if (!codeListSource.blueprint().usesExtensions) {
            return
        }

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
        if (!codeListSource.blueprint().usesSubCodeLists) {
            return //TODO - are these uses* checks needed?
        }

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
