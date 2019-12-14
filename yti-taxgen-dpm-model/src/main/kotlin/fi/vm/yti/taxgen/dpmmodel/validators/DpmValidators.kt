package fi.vm.yti.taxgen.dpmmodel.validators

import fi.vm.yti.taxgen.dpmmodel.DpmElement
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

fun <T : DpmElement, R : DpmElement> validateDpmElementCrossReferences(
    validationResultBuilder: ValidationResultBuilder,
    targetElements: Iterable<T>,
    targetCodeProperty: KProperty1<T, String>,
    referringElements: Iterable<R>,
    referringCodeProperty: KProperty1<R, String?>
) {
    val existingTargetCodes = targetElements.map { targetCodeProperty.getter.call(it) }.toSet()

    referringElements.forEach { element ->

        val refCode = referringCodeProperty.getter.call(element)

        if (refCode != null) {
            if (!existingTargetCodes.contains(refCode)) {
                validationResultBuilder.addError(
                    subject = element.validationSubjectDescriptor(),
                    valueName = referringCodeProperty,
                    reason = "Refers to unknown target",
                    value = refCode
                )
            }
        }
    }
}

fun validateDpmCodeContent(
    validationResultBuilder: ValidationResultBuilder,
    property: KProperty0<String>
) {
    val code: String = property.get()

    if (!isValidDpmCode(code))
        validationResultBuilder.addError(
            valueName = property,
            reason = "Illegal DPM Code",
            value = code
        )
}

const val codeStartChars = "_:"
const val codeChars = ".-_:"

private fun isValidDpmCode(code: String): Boolean {
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
