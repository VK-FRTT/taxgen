package fi.vm.yti.taxgen.datapointmetamodel

data class DpmElementRef(
    val id: String,
    val type: String
)

inline fun <reified T : DpmElement> dpmElementRef(id: String): DpmElementRef {
    return DpmElementRef(
        id = id,
        type = DpmElement.typeName(T::class)
    )
}
