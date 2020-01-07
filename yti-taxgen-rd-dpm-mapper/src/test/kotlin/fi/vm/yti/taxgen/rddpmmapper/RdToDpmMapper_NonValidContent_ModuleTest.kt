package fi.vm.yti.taxgen.rddpmmapper

import fi.vm.yti.taxgen.commons.HaltException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class RdToDpmMapper_NonValidContent_ModuleTest : RdToDpmMapper_ModuleTestBase() {

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
                "VALIDATION [DPM Owner] [] [Name] [Too short (minimum 2 characters)]"
            )
        }

        @Test
        fun `should error when owner name is blank`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_owner_config_empty_owner_name")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [DPM Owner] [] [Name] [Too short (minimum 2 characters)]"
            )
        }

        @Test
        fun `should error when owner has unsupported language`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_owner_config_unsupported_owner_language")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [DPM Owner] [Unsupported owner language] [LanguageCodes] [Unsupported language] [xyz]"
            )
        }
    }

    @Nested
    inner class DpmElementContent {

        @Test
        fun `should error when Metric content is non-valid`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_dpm_elements_metric")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [Metric] [http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/code/1] [MetricCode] [Illegal DPM Code] [?1]",
                "VALIDATION [Metric] [http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/code/1] [DataType] [Unsupported value] [UnsupportedMetricDataType]",
                "VALIDATION [Metric] [http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/code/1] [MetricCode] [Unsupported code structure] [?1]"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [Hierarchy] [http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/extension/MET1] [HierarchyNode] [http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/extension/MET1/member/8] [ReferencedElementCode] [Duplicate value] [i6]",
                "VALIDATION [Hierarchy] [http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/extension/MET1] [HierarchyNode] [http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/extension/MET1/member/9] [ReferencedElementCode] [Duplicate value] [i6]"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [Hierarchy] [http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/extension/MET10] [HierarchyNode] [http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/extension/MET10/member/7] [ReferencedElementCode] [Duplicate value] [i6]",
                "VALIDATION [Hierarchy] [http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/extension/MET10] [HierarchyNode] [http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/extension/MET10/member/12] [ReferencedElementCode] [Duplicate value] [i6]"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [DPM Model] [] [Metric] [http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/code/6] [MetricCode] [Duplicate value] [i6]",
                "VALIDATION [DPM Model] [] [Metric] [http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/code/7] [MetricCode] [Duplicate value] [i6]"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [DPM Dictionary] [prefix] [Metric] [http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/code/3] [ReferencedDomainCode] [Unknown target] [EDA]"
            )

            assertThat(diagnosticCollector.validationResultCount()).isEqualTo(10)
        }

        @Test
        fun `should error when ExplicitDomain, Member, Hierarchy & HierarchyNode content is non-valid`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_dpm_elements_explicit_domain")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [ExplicitDomain] [http://uri.suomi.fi/codelist/dpm-integration-fixture/exp-doms-2018-1/code/DOME] [DomainCode] [Too long (maximum 50 characters)]"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [Member] [http://uri.suomi.fi/codelist/dpm-integration-fixture/EDA-2018-1/code/EDA-x1] [MemberCode] [Illegal DPM Code] [EDA-x1-##-NonValidDpmCode]"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [Hierarchy] [http://uri.suomi.fi/codelist/dpm-integration-fixture/EDA-2018-1/extension/EDA-H2] [HierarchyCode] [Too long (maximum 50 characters)]"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [HierarchyNode] [http://uri.suomi.fi/codelist/dpm-integration-fixture/EDA-2018-1/extension/EDA-H2/member/1] [ComparisonOperator] [Unsupported arithmetical relationship] [UnsupportedComparisonOp]"
            )

            assertThat(diagnosticCollector.validationResultCount()).isEqualTo(4)
        }

        @Test
        fun `should error when ExplicitDimension content is non-valid`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_dpm_elements_explicit_dimension")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [ExplicitDimension] [http://uri.suomi.fi/codelist/dpm-integration-fixture/exp-dims-2018-1/code/DIM] [DimensionCode] [Too long (maximum 50 characters)]"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [DPM Dictionary] [prefix] [ExplicitDimension] [http://uri.suomi.fi/codelist/dpm-integration-fixture/exp-dims-2018-1/code/DIM] [ReferencedDomainCode] [Unknown target] [DOME]",
                "VALIDATION [DPM Dictionary] [prefix] [ExplicitDimension] [http://uri.suomi.fi/codelist/dpm-integration-fixture/exp-dims-2018-1/code/EDA-D1] [ReferencedDomainCode] [Unknown target] [EDA]",
                "VALIDATION [DPM Dictionary] [prefix] [ExplicitDimension] [http://uri.suomi.fi/codelist/dpm-integration-fixture/exp-dims-2018-1/code/EDA-D2] [ReferencedDomainCode] [Unknown target] [EDA]",
                "VALIDATION [DPM Dictionary] [prefix] [ExplicitDimension] [http://uri.suomi.fi/codelist/dpm-integration-fixture/exp-dims-2018-1/code/EDA-D10] [ReferencedDomainCode] [Unknown target] [EDA]"
            )

            assertThat(diagnosticCollector.validationResultCount()).isEqualTo(5)
        }

        @Test
        fun `should error when TypedDomain content is non-valid`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_dpm_elements_typed_domain")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [TypedDomain] [http://uri.suomi.fi/codelist/dpm-integration-fixture/typ-doms-2018-1/code/DOMT] [DataType] [Unsupported value] [UnsupportedTypeDomainDataType]"
            )

            assertThat(diagnosticCollector.validationResultCount()).isEqualTo(1)
        }

        @Test
        fun `should error when TypedDimension content is non-valid`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_dpm_elements_typed_dimension")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [TypedDimension] [http://uri.suomi.fi/codelist/dpm-integration-fixture/typ-dims-2018-1/code/TDB-D1] [DimensionCode] [Too long (maximum 50 characters)]"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [DPM Dictionary] [prefix] [TypedDimension] [http://uri.suomi.fi/codelist/dpm-integration-fixture/typ-dims-2018-1/code/TDB-D1] [ReferencedDomainCode] [Unknown target] [TDB]",
                "VALIDATION [DPM Dictionary] [prefix] [TypedDimension] [http://uri.suomi.fi/codelist/dpm-integration-fixture/typ-dims-2018-1/code/TDB-D2] [ReferencedDomainCode] [Unknown target] [TDB]"
            )

            assertThat(diagnosticCollector.validationResultCount()).isEqualTo(3)
        }

        @Test
        fun `should error when DPM Model content is non-valid`() {
            executeRdsToDpmMapperAndGetDictionariesFrom("nonvalid_dpm_model")

            assertThat(diagnosticCollector.eventsString()).contains(
                "VALIDATION [DPM Model] [] [Owner.Prefix] [Duplicate value] [prefix]"
            )

            assertThat(diagnosticCollector.validationResultCount()).isEqualTo(1)
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
