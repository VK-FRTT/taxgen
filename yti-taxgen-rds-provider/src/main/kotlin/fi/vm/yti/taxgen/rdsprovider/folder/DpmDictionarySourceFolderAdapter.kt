package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.rdsprovider.CodeListBlueprint
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import java.nio.file.Files
import java.nio.file.Path

internal class DpmDictionarySourceFolderAdapter(
    private val dpmDictionaryRootPath: Path
) : DpmDictionarySource {

    override fun dpmOwnerConfigData(): String {
        return FileOps.readTextFile(dpmDictionaryRootPath, "dpm_owner_config.json")
    }

    override fun metricsSource(): CodeListSource? {
        return codeListSourceOrNullForConcept(
            "met",
            CodeListBlueprint.metrics()
        )
    }

    override fun explicitDomainsAndHierarchiesSource(): CodeListSource? {
        return codeListSourceOrNullForConcept(
            "exp_dom_hier",
            CodeListBlueprint.explicitDomainsAndHierarchies()
        )
    }

    override fun explicitDimensionsSource(): CodeListSource? {
        return codeListSourceOrNullForConcept(
            "exp_dim",
            CodeListBlueprint.explicitDimensions()
        )
    }

    override fun typedDomainsSource(): CodeListSource? {
        return codeListSourceOrNullForConcept(
            "typ_dom",
            CodeListBlueprint.typedDomains()
        )
    }

    override fun typedDimensionsSource(): CodeListSource? {
        return codeListSourceOrNullForConcept(
            "typ_dim",
            CodeListBlueprint.typedDimensions()
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
