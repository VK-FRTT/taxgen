package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.validationfw.validateProperty

class Owner(
    val namespace: String,
    val namespacePrefix: String,
    val officialLocation: String,
    val copyrightText: String?,
    val supportedLanguages: List<String>
) {

    init {
        validateProperty(
            instance = this,
            property = "namespace",
            minLength = 10,
            maxLength = 100
        )

        validateProperty(
            instance = this,
            property = "namespacePrefix",
            minLength = 2,
            maxLength = 10
        )

        validateProperty(
            instance = this,
            property = "officialLocation",
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
