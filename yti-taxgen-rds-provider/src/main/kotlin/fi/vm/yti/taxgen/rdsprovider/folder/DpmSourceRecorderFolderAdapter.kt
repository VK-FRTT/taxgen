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
import java.nio.file.Path
import java.time.Instant

class DpmSourceRecorderFolderAdapter(
    baseFolderPath: Path,
    private val forceOverwrite: Boolean,
    private val diagnostic: Diagnostic
) : DpmSourceRecorder {

    private val baseFolderPath = baseFolderPath.toAbsolutePath().normalize()

    override fun contextLabel(): String = "folder"
    override fun contextIdentifier(): String = baseFolderPath.toString()

    override fun captureSources(dpmSource: DpmSource) {
        val pathStack = PathStack(
            baseFolderPath = baseFolderPath,
            createFileSystemPaths = true,
            diagnostic = diagnostic
        )

        diagnostic.withContext(this) {
            captureDpmSource(dpmSource, pathStack)
        }
    }

    private fun captureDpmSource(
        dpmSource: DpmSource,
        pathStack: PathStack
    ) {
        diagnostic.withContext(dpmSource) {

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

            captureDpmDictionarySources(
                dpmSource.dpmDictionarySources(),
                pathStack
            )
        }
    }

    private fun captureDpmDictionarySources(
        yclDpmDictionarySources: Sequence<DpmDictionarySource>,
        pathStack: PathStack
    ) {
        yclDpmDictionarySources.withIndex().forEach { (dictionaryIndex, dictionarySource) ->

            diagnostic.withContext(dictionarySource) {

                pathStack.withSubfolder("dpm_dictionary_$dictionaryIndex") {

                    FileOps.writeTextFile(
                        dictionarySource.dpmOwnerConfigData(),
                        pathStack,
                        "dpm_owner_config.json",
                        forceOverwrite,
                        diagnostic
                    )

                    captureCodeListSource(
                        dictionarySource.metricsSource(),
                        "met",
                        pathStack
                    )

                    captureCodeListSource(
                        dictionarySource.explicitDomainsAndHierarchiesSource(),
                        "exp_dom_hier",
                        pathStack
                    )

                    captureCodeListSource(
                        dictionarySource.explicitDimensionsSource(),
                        "exp_dim",
                        pathStack
                    )

                    captureCodeListSource(
                        dictionarySource.typedDomainsSource(),
                        "typ_dom",
                        pathStack
                    )

                    captureCodeListSource(
                        dictionarySource.typedDimensionsSource(),
                        "typ_dim",
                        pathStack
                    )
                }
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

        diagnostic.withContext(codeListSource) {

            pathStack.withSubfolder(conceptFolderName) {

                FileOps.writeTextFile(
                    codeListSource.codeListMetaData(),
                    pathStack,
                    "code_list_meta.json",
                    forceOverwrite,
                    diagnostic
                )

                codeListSource.codePagesData().withIndex().forEach { (index, pageData) ->
                    FileOps.writeTextFile(
                        pageData,
                        pathStack,
                        "codes_page_$index.json",
                        forceOverwrite,
                        diagnostic
                    )
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
    }

    private fun captureExtensionSources(
        codeListSource: CodeListSource,
        pathStack: PathStack
    ) {
        if (!codeListSource.blueprint().usesExtensions) {
            return
        }

        codeListSource.extensionSources().withIndex().forEach { (listIndex, extensionSource) ->

            diagnostic.withContext(extensionSource) {

                pathStack.withSubfolder("extension_$listIndex") {

                    FileOps.writeTextFile(
                        extensionSource.extensionMetaData(),
                        pathStack,
                        "extension_meta.json",
                        forceOverwrite,
                        diagnostic
                    )

                    extensionSource.extensionMemberPagesData().withIndex().forEach { (index, pageData) ->
                        FileOps.writeTextFile(
                            pageData,
                            pathStack,
                            "members_page_$index.json",
                            forceOverwrite,
                            diagnostic
                        )
                    }
                }
            }
        }
    }

    private fun captureSubCodeListSources(
        codeListSource: CodeListSource,
        pathStack: PathStack
    ) {
        if (!codeListSource.blueprint().usesSubCodeLists) {
            return
        }

        codeListSource.subCodeListSources().withIndex().forEach { (listIndex, subCodeListSource) ->
            diagnostic.withContext(subCodeListSource) {
                captureCodeListSource(subCodeListSource, "sub_code_list_$listIndex", pathStack)
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
