package fi.vm.yti.taxgen.rdsdpmmapper

import fi.vm.yti.taxgen.commons.HaltException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/*
TODO - Things to test:
- Member code prefix handling
- Code & Extension Members disordered
- Broken data:
    - invalid/codescheme_json_broken
    - invalid/codescheme_missing_used_default_code
    - invalid_dpm_element_values
      => Triggers validation error for each DPM Element type
      (to verify that each element type is really validated along the process)
*/

@DisplayName("Mapping unit test")
internal class RdsToDpmMapper_UnitTest : RdsToDpmMapper_UnitTestBase() {

    @Test
    fun `should produce 3 DPM Dictionaries with proper Owners`() {
        val dpmDictionaries = performMappingAndGetAllDictionaries("3_empty_dictionaries")

        Assertions.assertThat(dpmDictionaries.size).isEqualTo(3)

        dpmDictionaries[0].owner.apply {
            assertThat(name).isEqualTo("Owner 1/3")
            assertThat(namespace).isEqualTo("namespace")
            assertThat(prefix).isEqualTo("prefix")
            assertThat(location).isEqualTo("location")
            assertThat(copyright).isEqualTo("copyright")
            assertThat(languages).isEqualTo(hashSetOf(en))
            assertThat(defaultLanguage).isEqualTo(en)
        }

        dpmDictionaries[1].owner.apply {
            assertThat(name).isEqualTo("Owner 2/3")
            assertThat(namespace).isEqualTo("namespace")
            assertThat(prefix).isEqualTo("prefix")
            assertThat(location).isEqualTo("location")
            assertThat(copyright).isEqualTo("copyright")
            assertThat(languages).isEqualTo(hashSetOf(fi))
            assertThat(defaultLanguage).isEqualTo(en)
        }

        dpmDictionaries[2].owner.apply {
            assertThat(name).isEqualTo("Owner 3/3")
            assertThat(namespace).isEqualTo("namespace")
            assertThat(prefix).isEqualTo("prefix")
            assertThat(location).isEqualTo("location")
            assertThat(copyright).isEqualTo("copyright")
            assertThat(languages).isEqualTo(hashSetOf(sv))
            assertThat(defaultLanguage).isEqualTo(en)
        }
    }

    @Test
    fun `should produce correct diagnostic context events`() {
        performMappingFromIntegrationFixture()

        Assertions.assertThat(diagnosticCollector.events).contains(
            "ENTER [RdsToDpmMapper] [RDS source data to DPM model]"
        )
    }

    @Test
    fun `should error when owner config JSON is broken`() {

        val thrown = catchThrowable { performMappingAndGetAllDictionaries("broken_owner_config_json") }

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
        performMappingAndGetAllDictionaries("broken_null_owner_name")

        assertThat(diagnosticCollector.eventsString()).contains(
            "VALIDATION",
            "Owner.name",
            "is too short"
        )
    }

    @Test
    fun `should error when owner name is blank`() {
        performMappingAndGetAllDictionaries("broken_empty_owner_name")

        assertThat(diagnosticCollector.eventsString()).contains(
            "VALIDATION",
            "Owner.name",
            "is too short"
        )
    }

    @Test
    fun `should error when owner has unsupported language`() {
        performMappingAndGetAllDictionaries("broken_unsupported_owner_language")

        assertThat(diagnosticCollector.eventsString()).contains(
            "VALIDATION",
            "Owner.language",
            "unsupported language 'xyz'"
        )
    }

    @Test
    fun `should error when codelist extension has orphan elements`() {
        val thrown = catchThrowable { performMappingAndGetAllDictionaries("broken_orphan_extension_member") }

        assertThat(thrown).isInstanceOf(HaltException::class.java)

        assertThat(diagnosticCollector.eventsString()).contains(
            "Corrupted source data.",
            "Codelist Extension has Members, which position in DPM Hierarchy could not be determined:",
            "http://uri.suomi.fi/codelist/dpm-integration-fixture/EDA-2018-1/extension/EDA-H1/member/3 (EDA hierarchy node 5)"
        )
    }
}
