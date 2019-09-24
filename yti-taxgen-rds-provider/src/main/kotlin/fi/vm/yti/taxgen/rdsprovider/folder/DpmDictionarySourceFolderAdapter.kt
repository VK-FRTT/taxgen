package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.CodeListBlueprint
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.configinput.ConfigFactory
import fi.vm.yti.taxgen.rdsprovider.OwnerHolder
import java.nio.file.Files
import java.nio.file.Path

internal class DpmDictionarySourceFolderAdapter(
    private val dpmDictionaryRootPath: Path,
    private val diagnostic: Diagnostic
) : DpmDictionarySource {

    override fun contextLabel(): String = ""
    override fun contextIdentifier(): String = ""

    override fun dpmOwner(action: (OwnerHolder) -> Unit) {

        val configFilePath = dpmDictionaryRootPath.resolve("dpm_owner_config.json")

        val owner = ConfigFactory.ownerFromFile(
            configFilePath,
            diagnostic = diagnostic
        )

        action(owner)
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
