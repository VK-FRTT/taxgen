package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.validationfw.validateProperty

data class Owner(
    val name: String,
    val namespace: String,
    val prefix: String,
    val location: String,
    val copyright: String,
    val supportedLanguages: List<String>
) {

    init {
        validateProperty(
            instance = this,
            property = "name",
            minLength = 10,
            maxLength = 100
        )

        validateProperty(
            instance = this,
            property = "namespace",
            minLength = 10,
            maxLength = 100
        )

        validateProperty(
            instance = this,
            property = "prefix",
            minLength = 2,
            maxLength = 10
        )

        validateProperty(
            instance = this,
            property = "location",
            minLength = 10,
            maxLength = 100
        )

        validateProperty(
            instance = this,
            property = "supportedLanguages",
            minLength = 1,
            maxLength = 50
        )
    }
}
