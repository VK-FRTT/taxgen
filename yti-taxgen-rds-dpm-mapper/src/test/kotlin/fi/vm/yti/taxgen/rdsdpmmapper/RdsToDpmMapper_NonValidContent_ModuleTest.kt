package fi.vm.yti.taxgen.rdsdpmmapper

import fi.vm.yti.taxgen.commons.HaltException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class RdsToDpmMapper_NonValidContent_ModuleTest : RdsToDpmMapper_ModuleTestBase() {

    @Test
    fun `should error when codelist extension has orphan elements`() {
        val thrown = catchThrowable { executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_orphan_extension_member") }

        assertThat(thrown).isInstanceOf(HaltException::class.java)

        assertThat(diagnosticCollector.eventsString()).contains(
            "Corrupted source data.",
            "Codelist Extension has Members, which position in DPM Hierarchy could not be determined:",
            "http://uri.suomi.fi/codelist/dpm-integration-fixture/EDA-2018-1/extension/EDA-H1/member/3 (EDA hierarchy node 5 (fi))"
        )
    }

    @Nested
    inner class OwnerConfigContent {
        @Test
        fun `should error when owner config JSON is broken`() {

            val thrown = catchThrowable { executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_owner_config_json") }

            assertThat(thrown).isInstanceOf(HaltException::class.java)

            assertThat(diagnosticCollector.eventsString()).contains(
                "MESSAGE",
                "FATAL",
                "Processing JSON content failed",
                "Unexpected character",
                "Broken owner config"
            )
        }

        @Test
        fun `should error when owner name is null`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_owner_config_null_owner_name")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION",
                "Owner.name",
                "is too short"
            )
        }

        @Test
        fun `should error when owner name is blank`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_owner_config_empty_owner_name")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION",
                "Owner.name",
                "is too short"
            )
        }

        @Test
        fun `should error when owner has unsupported language`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_owner_config_unsupported_owner_language")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION",
                "Owner.language",
                "unsupported language 'xyz'"
            )
        }
    }

    @Nested
    inner class DpmElementContent {

        @Test
        fun `should error when Metric content is non-valid`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_dpm_elements_metric")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATED OBJECT [Metric] [http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/code/1]",
                "VALIDATION [Metric.dataType: unsupported data type 'UnsupportedMetricDataType']"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATED OBJECT [Hierarchy] []",
                "VALIDATION [Hierarchy.rootNodes: duplicate referencedElementCode value 'i6']"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATED OBJECT [Hierarchy] []",
                "VALIDATION [Hierarchy.rootNodes: duplicate referencedElementCode value 'i6']"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATED OBJECT [DpmModel] []",
                "VALIDATION [DpmModel.dictionaries.metricDomain: duplicate Metric.metricCode value 'i6']"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATED OBJECT [DpmDictionary] []",
                "VALIDATION [DpmDictionary.metrics: Metric http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/code/3 refers non existing ExplicitDomain 'EDA']"
            )

            assertThat(diagnosticCollector.objectValidationFailureCount()).isEqualTo(5)
        }

        @Test
        fun `should error when ExplicitDomain, Member, Hierarchy & HierarchyNode content is non-valid`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_dpm_elements_explicit_domain")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATED OBJECT [ExplicitDomain] [http://uri.suomi.fi/codelist/dpm-integration-fixture/exp-doms-2018-1/code/DOME]",
                "VALIDATION [ExplicitDomain.domainCode: is too long (maximum 50 characters)]"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATED OBJECT [Member] [http://uri.suomi.fi/codelist/dpm-integration-fixture/EDA-2018-1/code/EDA-x1]",
                "VALIDATION [Member.memberCode: is illegal DPM Code]"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATED OBJECT [HierarchyNode] []",
                "VALIDATION [HierarchyNode.comparisonOperator: unsupported arithmetical relationship (comparison operator) 'UnsupportedComparisonOp']"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATED OBJECT [Hierarchy] []",
                "VALIDATION [Hierarchy.hierarchyCode: is too long (maximum 50 characters)]"
            )

            assertThat(diagnosticCollector.objectValidationFailureCount()).isEqualTo(4)
        }

        @Test
        fun `should error when ExplicitDimension content is non-valid`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_dpm_elements_explicit_dimension")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATED OBJECT [ExplicitDimension] [http://uri.suomi.fi/codelist/dpm-integration-fixture/exp-dims-2018-1/code/DIM]",
                "VALIDATION [ExplicitDimension.dimensionCode: is too long (maximum 50 characters)]"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATED OBJECT [DpmDictionary] []",
                "VALIDATION [DpmDictionary.explicitDimensions: ExplicitDimension http://uri.suomi.fi/codelist/dpm-integration-fixture/exp-dims-2018-1/code/DIM refers non existing domain DOME]"
            )

            assertThat(diagnosticCollector.objectValidationFailureCount()).isEqualTo(2)
        }

        @Test
        fun `should error when TypedDomain content is non-valid`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_dpm_elements_typed_domain")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATED OBJECT [TypedDomain] [http://uri.suomi.fi/codelist/dpm-integration-fixture/typ-doms-2018-1/code/DOMT]",
                "VALIDATION [TypedDomain.dataType: unsupported data type 'UnsupportedTypeDomainDataType']"
            )

            assertThat(diagnosticCollector.objectValidationFailureCount()).isEqualTo(1)
        }

        @Test
        fun `should error when TypedDimension content is non-valid`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_dpm_elements_typed_dimension")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATED OBJECT [TypedDimension] [http://uri.suomi.fi/codelist/dpm-integration-fixture/typ-dims-2018-1/code/TDB-D1]",
                "VALIDATION [TypedDimension.dimensionCode: is too long (maximum 50 characters)]"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATED OBJECT [DpmDictionary] []",
                "VALIDATION [DpmDictionary.typedDimensions: TypedDimension http://uri.suomi.fi/codelist/dpm-integration-fixture/typ-dims-2018-1/code/TDB-D1 refers non existing domain TDB]"
            )

            assertThat(diagnosticCollector.objectValidationFailureCount()).isEqualTo(2)
        }

        @Test
        fun `should error when DPM Model content is non-valid`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_dpm_model")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATED OBJECT [DpmModel] []",
                "VALIDATION [DpmModel.dictionaries: duplicate owner.prefix value 'prefix']"
            )

            assertThat(diagnosticCollector.objectValidationFailureCount()).isEqualTo(1)
        }

        @Test
        fun `should error when response JSON is non-valid`() {
            catchThrowable {
                executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_codes_page_json")
            }

            assertThat(diagnosticCollector.eventsString()).contains(
                "MESSAGE [FATAL] [Processing JSON content failed:"
            )
        }
    }
}
