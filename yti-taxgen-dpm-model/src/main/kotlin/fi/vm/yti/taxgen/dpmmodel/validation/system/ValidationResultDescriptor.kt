package fi.vm.yti.taxgen.dpmmodel.validation.system

class ValidationResultDescriptor private constructor(
    private val subjectChain: List<ValidationSubjectDescriptor>,
    private val valueName: String,
    private val reason: String,
    private val value: String,
    private val hasValue: Boolean
) {
    companion object {

        fun withEssentials(
            subjects: List<ValidationSubjectDescriptor>,
            valueName: String,
            reason: String
        ): ValidationResultDescriptor {
            return ValidationResultDescriptor(subjects, valueName, reason, "", false)
        }

        fun withEssentialsAndValue(
            subjects: List<ValidationSubjectDescriptor>,
            valueName: String,
            reason: String,
            value: String
        ): ValidationResultDescriptor {
            return ValidationResultDescriptor(subjects, valueName, reason, value, true)
        }
    }

    fun subjectChain(): List<ValidationSubjectDescriptor> {
        return subjectChain
    }

    fun valueName(): String {
        return valueName
    }

    fun reason(): String {
        return reason
    }

    fun value(): String {
        require(hasValue) {
            "Value was tried to be get for reason: $reason"
        }
        return value
    }

    fun hasValue(): Boolean {
        return hasValue
    }

    override fun toString(): String {
        val sb = StringBuilder()

        subjectChain().forEach { subject ->
            sb.append("[${subject.subjectType}] ")

            if (subject.subjectIdentifiers.isEmpty()) {
                sb.append("[] ")
            } else {
                subject.subjectIdentifiers.forEach {
                    sb.append("[$it] ")
                }
            }
        }

        sb.append("[${valueName()}] [${reason()}]")

        if (hasValue()) {
            sb.append(" [${value()}]")
        }

        return sb.toString()
    }

    override fun hashCode(): Int =
        subjectChain.hashCode() * 31 +
                valueName.hashCode() * 31 +
                reason.hashCode() * 31 +
                value.hashCode() * 31 +
                hasValue.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }

        if (other !is ValidationResultDescriptor) {
            return false
        }

        return subjectChain == other.subjectChain &&
                valueName == other.valueName &&
                reason == other.reason &&
                value == other.value &&
                hasValue == other.hasValue
    }
}
