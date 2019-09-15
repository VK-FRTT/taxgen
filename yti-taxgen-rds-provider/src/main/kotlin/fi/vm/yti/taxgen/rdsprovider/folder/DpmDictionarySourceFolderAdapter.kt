package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.rdsprovider.CodeListBlueprint
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import java.nio.file.Files
import java.nio.file.Path

internal class DpmDictionarySourceFolderAdapter(
    private val dpmDictionaryRootPath: Path
) : DpmDictionarySource {

    override fun contextLabel(): String = ""
    override fun contextIdentifier(): String = ""

    override fun dpmOwnerConfigData(action: (String) -> Unit) {
        action(FileOps.readTextFile(dpmDictionaryRootPath, "dpm_owner_config.json"))
    }

    override fun metricsSource(action: (CodeListSource?) -> Unit) {
        action(
            codeListSourceOrNullForConcept(
                "met",
                CodeListBlueprint.metrics()
            )
        )
    }

    override fun explicitDomainsAndHierarchiesSource(action: (CodeListSource?) -> Unit) {
        action(
            codeListSourceOrNullForConcept(
                "exp_dom_hier",
                CodeListBlueprint.explicitDomainsAndHierarchies()
            )
        )
    }

    override fun explicitDimensionsSource(action: (CodeListSource?) -> Unit) {
        action(
            codeListSourceOrNullForConcept(
                "exp_dim",
                CodeListBlueprint.explicitOrTypedDimensions()
            )
        )
    }

    override fun typedDomainsSource(action: (CodeListSource?) -> Unit) {
        action(
            codeListSourceOrNullForConcept(
                "typ_dom",
                CodeListBlueprint.typedDomains()
            )
        )
    }

    override fun typedDimensionsSource(action: (CodeListSource?) -> Unit) {
        action(
            codeListSourceOrNullForConcept(
                "typ_dim",
                CodeListBlueprint.explicitOrTypedDimensions()
            )
        )
    }

    private fun codeListSourceOrNullForConcept(
        conceptFolder: String,
        blueprint: CodeListBlueprint
    ): CodeListSource? {
        val path = dpmDictionaryRootPath.resolve(conceptFolder)

        if (!Files.exists(path)) {
            return null
        }

        return CodeListSourceFolderAdapter(path, blueprint)
    }
}
