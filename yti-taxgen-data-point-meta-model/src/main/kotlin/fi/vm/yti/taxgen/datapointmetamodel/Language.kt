package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.validationfw.validateProperty

data class Language(
    val iso6391Code: String,
    val label: Map<Language, String>
) {
    init {
        validateProperty(
            instance = this,
            property = "iso6391Code",
            minLength = 2,
            maxLength = 2
        )

        //TODO - validate label text not blank
    }

    override fun hashCode(): Int {
        return iso6391Code.hashCode()
    }

    override fun toString(): String {
        val labelString = label.map { (lang, text) -> "${lang.iso6391Code}: $text" }.joinToString()
        return "{Language(iso6391Code=$iso6391Code, label={$labelString})"
    }

    override fun equals(other: Any?): Boolean {
        if (other == this) {
            return true
        }
        if (other !is Language) {
            return false
        }

        return other.iso6391Code == iso6391Code
    }

    fun nativeLabel(): String? {
        val language = label.keys.find { it.iso6391Code == iso6391Code }
        return label[language]
    }

    fun englishLabel(): String? {
        val language = label.keys.find { it.iso6391Code == "en" }
        return label[language]
    }
}
