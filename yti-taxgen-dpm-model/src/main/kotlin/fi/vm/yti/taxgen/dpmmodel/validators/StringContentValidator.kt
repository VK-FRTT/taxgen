package fi.vm.yti.taxgen.dpmmodel.validators

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import kotlin.reflect.KProperty1

fun <I : Any> validateDpmCodeContent(
    validationResults: ValidationResults,
    instance: I,
    property: KProperty1<I, String>
) {
    val name: String = property.getter.call(instance)

    if (!isValidCodeName(name))
        validationResults.addError(
            instance = instance,
            propertyName = property.name,
            message = "is illegal DPM Code"
        )
}

const val codeStartChars = "_:"
const val codeChars = ".-_:"

private fun isValidCodeName(code: String): Boolean {
    fun isLatinAlphabet(char: Char): Boolean = char in 'a'..'z' || char in 'A'..'Z'
    fun isDigit(char: Char): Boolean = char in '0'..'9'
    fun isCodeStartChar(char: Char): Boolean = isLatinAlphabet(char) || codeStartChars.contains(char)
    fun isCodeChar(char: Char): Boolean = isLatinAlphabet(char) || isDigit(char) || codeChars.contains(char)

    if (code.isEmpty()) return false

    if (!isCodeStartChar(code[0])) return false

    code.substring(1).forEach { char ->
        if (!isCodeChar(char)) return false
    }

    return true
}
