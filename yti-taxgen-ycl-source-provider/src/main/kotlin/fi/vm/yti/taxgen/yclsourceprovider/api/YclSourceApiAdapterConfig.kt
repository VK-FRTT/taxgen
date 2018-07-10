package fi.vm.yti.taxgen.yclsourceprovider.api

import com.fasterxml.jackson.annotation.JsonProperty

data class YclSourceApiAdapterConfig(

    val type: String,
    val configSchemaVersion: String,    //TODO  - verify type & schema version

    @JsonProperty("dpmDictionaries")
    val dpmDictionaryConfigs: List<DpmDictionarySourceApiAdapterConfig>
)
