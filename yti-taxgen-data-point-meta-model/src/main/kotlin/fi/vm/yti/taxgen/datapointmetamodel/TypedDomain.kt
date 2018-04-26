package fi.vm.yti.taxgen.datapointmetamodel

data class TypedDomain(
    override val concept: Concept,
    override val domainCode: String
) : Domain {

    init {
        domainValidation()
    }
}
