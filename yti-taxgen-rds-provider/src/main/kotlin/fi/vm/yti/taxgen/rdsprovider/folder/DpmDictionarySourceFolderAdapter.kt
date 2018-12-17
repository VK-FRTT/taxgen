package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import java.nio.file.Path

internal class DpmDictionarySourceFolderAdapter(
    private val dpmDictionaryRootPath: Path
) : DpmDictionarySource {

    override fun dpmOwnerConfigData(): String {
        return FileOps.readTextFile(dpmDictionaryRootPath, "dpm_owner_config.json")
    }

    override fun metricsSource(): CodeListSource? {
        return CodeListSourceFolderAdapter(dpmDictionaryRootPath.resolve("met"))
    }

    override fun explicitDomainsAndHierarchiesSource(): CodeListSource? {
        return CodeListSourceFolderAdapter(dpmDictionaryRootPath.resolve("exp_dom"))
    }

    override fun explicitDimensionsSource(): CodeListSource? {
        return CodeListSourceFolderAdapter(dpmDictionaryRootPath.resolve("exp_dim"))
    }

    override fun typedDomainsSource(): CodeListSource? {
        return CodeListSourceFolderAdapter(dpmDictionaryRootPath.resolve("typ_dom"))
    }

    override fun typedDimensionsSource(): CodeListSource? {
        return CodeListSourceFolderAdapter(dpmDictionaryRootPath.resolve("typ_dim"))
    }
}
