package fi.vm.yti.taxgen.yclsourceprovider.api

import com.fasterxml.jackson.annotation.JsonProperty

data class YclSourceApiAdapterConfig(

    val type: String,
    val configSchemaVersion: String,

    @JsonProperty("dpmDictionaries")
    val dpmDictionaryConfigs: List<DpmDictionarySourceApiAdapterConfig>
)
