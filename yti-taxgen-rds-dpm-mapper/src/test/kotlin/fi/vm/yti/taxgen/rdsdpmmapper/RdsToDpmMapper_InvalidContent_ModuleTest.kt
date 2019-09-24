package fi.vm.yti.taxgen.rdsdpmmapper

import fi.vm.yti.taxgen.commons.HaltException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

/*
TODO - Things to test:
- Broken data:
    - invalid/codescheme_json_broken
    - invalid/codescheme_missing_used_default_code
    - invalid_dpm_element_values
      => Triggers validation error for each DPM Element type
      (to verify that each element type is really validated along the process)
*/

internal class RdsToDpmMapper_InvalidContent_ModuleTest : RdsToDpmMapper_ModuleTestBase() {

    @Test
    fun `should error when owner config JSON is broken`() {

        val thrown = catchThrowable { executeRdsToDpmMapperAndGetDictionariesFrom("broken_owner_config_json") }

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
        executeRdsToDpmMapperAndGetDictionariesFrom("broken_null_owner_name")

        assertThat(diagnosticCollector.eventsString()).contains(
            "VALIDATION",
            "Owner.name",
            "is too short"
        )
    }

    @Test
    fun `should error when owner name is blank`() {
        executeRdsToDpmMapperAndGetDictionariesFrom("broken_empty_owner_name")

        assertThat(diagnosticCollector.eventsString()).contains(
            "VALIDATION",
            "Owner.name",
            "is too short"
        )
    }

    @Test
    fun `should error when owner has unsupported language`() {
        executeRdsToDpmMapperAndGetDictionariesFrom("broken_unsupported_owner_language")

        assertThat(diagnosticCollector.eventsString()).contains(
            "VALIDATION",
            "Owner.language",
            "unsupported language 'xyz'"
        )
    }

    @Test
    fun `should error when codelist extension has orphan elements`() {
        val thrown = catchThrowable { executeRdsToDpmMapperAndGetDictionariesFrom("broken_orphan_extension_member") }

        assertThat(thrown).isInstanceOf(HaltException::class.java)

        assertThat(diagnosticCollector.eventsString()).contains(
            "Corrupted source data.",
            "Codelist Extension has Members, which position in DPM Hierarchy could not be determined:",
            "http://uri.suomi.fi/codelist/dpm-integration-fixture/EDA-2018-1/extension/EDA-H1/member/3 (EDA hierarchy node 5 (fi))"
        )
    }
}
