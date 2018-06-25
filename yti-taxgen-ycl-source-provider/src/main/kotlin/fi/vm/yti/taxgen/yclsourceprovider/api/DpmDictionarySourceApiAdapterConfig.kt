package fi.vm.yti.taxgen.yclsourceprovider.api

import com.fasterxml.jackson.annotation.JsonProperty
import fi.vm.yti.taxgen.commons.ext.kotlin.toJsonString

data class DpmDictionarySourceApiAdapterConfig(
    @JsonProperty("owner")
    private val ownerInfo: Map<String, Any>,

    @JsonProperty("sourceCodelists")
    val codelistConfigs: List<YclCodelistSourceApiAdapterConfig>
) {

    fun ownerInfoData() = ownerInfo.toJsonString()
}
