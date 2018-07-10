package fi.vm.yti.taxgen.datapointmetamodel.languages

import com.fasterxml.jackson.annotation.JsonProperty

data class LanguageConfig(

    @JsonProperty("ISO_639_1_code")
    val iso6391Code: String,

    val label: Map<String, String>
)
