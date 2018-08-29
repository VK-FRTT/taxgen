package fi.vm.yti.taxgen.yclsourceprovider.api.config

import com.fasterxml.jackson.annotation.JsonProperty
import fi.vm.yti.taxgen.commons.JsonOps

data class DpmDictionarySourceApiAdapterConfig(
    @JsonProperty("owner")
    private val ownerInfo: Map<String, Any>,

    @JsonProperty("codelists")
    val codelistConfigs: List<YclCodelistSourceApiAdapterConfig>
) {

    fun ownerInfoData() = JsonOps.writeAsJsonString(ownerInfo)
}
