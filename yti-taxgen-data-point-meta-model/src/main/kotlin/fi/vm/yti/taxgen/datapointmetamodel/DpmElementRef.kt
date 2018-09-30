package fi.vm.yti.taxgen.datapointmetamodel

data class DpmElementRef(
    val id: String,
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

    fun diagnosticHandle(): String {
        return DpmElement.formatDiagnosticHandle(
            id = id,
            type = type,
            diagnosticLabel = diagnosticLabel
        )
    }
}

inline fun <reified T : DpmElement> dpmElementRef(
    id: String,
    diagnosticLabel: String
): DpmElementRef {
    return DpmElementRef(
        id = id,
        type = DpmElement.typeName(T::class),
        diagnosticLabel = diagnosticLabel
    )
}
