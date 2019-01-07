package fi.vm.yti.taxgen.dpmmodel

data class DpmElementRef(
    val uri: String,
    val type: String,
    val diagnosticLabel: String
) {
    override fun hashCode(): Int {
        return ((uri.hashCode() * 31) + type.hashCode()) * 31
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is DpmElementRef) {
            return false
        }

        return other.uri == uri && other.type == type
    }

    fun diagnosticTag(): String {
        return if (diagnosticLabel.isNotEmpty()) {
            "(DPM $type) $diagnosticLabel ($uri)"
        } else {
            "(DPM $type) ($uri)"
        }
    }
}

inline fun <reified T : DpmElement> dpmElementRef(
    uri: String,
    diagnosticLabel: String
): DpmElementRef {
    return DpmElementRef(
        uri = uri,
        type = DpmElement.typeName(T::class),
        diagnosticLabel = diagnosticLabel
    )
}
