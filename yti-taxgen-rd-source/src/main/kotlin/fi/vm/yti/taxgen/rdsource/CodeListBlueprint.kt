package fi.vm.yti.taxgen.rdsource

import com.fasterxml.jackson
    .module.kotlin.readValue
import com.fasterxml.jackson.core.JsonProcessingException
import fi.vm.yti.taxgen.commons.ops.JsonOps
import fi.vm.yti.taxgen.commons.throwFail

data class CodeListBlueprint(
    val extensionPropertyTypeUris: List<String>,
    val subCodeListBlueprint: CodeListBlueprint?
) {

    companion object {

        fun metrics(): CodeListBlueprint {
            return fromConfig(
                """
                {
                "extensionPropertyTypeUris": [
                    "http://uri.suomi.fi/datamodel/ns/code#dpmMetric",
                    "http://uri.suomi.fi/datamodel/ns/code#definitionHierarchy"
                ],
                "subCodeListBlueprint": null
                }
                """.trimIndent()
            )
        }

        fun explicitDomainsAndHierarchies(): CodeListBlueprint {
            return fromConfig(
                """
                {
                "extensionPropertyTypeUris": [
                    "http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain"
                ],
                "subCodeListBlueprint": {
                    "extensionPropertyTypeUris": [
                        "http://uri.suomi.fi/datamodel/ns/code#definitionHierarchy",
                        "http://uri.suomi.fi/datamodel/ns/code#calculationHierarchy"
                    ],
                    "subCodeListBlueprint": null
                 }
                }
                """.trimIndent()
            )
        }

        fun explicitOrTypedDimensions(): CodeListBlueprint {
            return fromConfig(
                """
                {
                "extensionPropertyTypeUris": [
                    "http://uri.suomi.fi/datamodel/ns/code#dpmDimension"
                ],
                "subCodeListBlueprint": null
                }
                """.trimIndent()
            )
        }

        fun typedDomains(): CodeListBlueprint {
            return fromConfig(
                """
                {
                "extensionPropertyTypeUris": [
                    "http://uri.suomi.fi/datamodel/ns/code#dpmTypedDomain"
                ],
                "subCodeListBlueprint": null
                }
                """.trimIndent()
            )
        }

        private fun fromConfig(config: String): CodeListBlueprint {
            try {
                return JsonOps.lenientObjectMapper.readValue(config)
            } catch (e: JsonProcessingException) {
                throwFail("Code list blueprint configuration parsing failed: ${e.message}")
            }
        }
    }
}
