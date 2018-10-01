package fi.vm.yti.taxgen.datapointmetamodel

data class DpmElementRef(
    val id: String,
    val uri: String,
    val type: String,
    val diagnosticLabel: String
) {
    override fun hashCode(): Int {
        return ((id.hashCode() * 31) + type.hashCode()) * 31
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is DpmElementRef) {
            return false
        }

        return other.id == id && other.type == type
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
    id: String,
    uri: String,
    diagnosticLabel: String
): DpmElementRef {
    return DpmElementRef(
        id = id,
        uri = uri,
        type = DpmElement.typeName(T::class),
        diagnosticLabel = diagnosticLabel
    )
}
