package fi.vm.yti.taxgen.rdsdpmmapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Mapping Integration Fixture to DPM model - Common")
internal class IntegrationFixtureMappingTest_Common : RdsToDpmMapper_UnitTestBase() {

    @Test
    fun `should produce DPM Dictionary with proper Owner`() {
        val dpmDictionary = performMappingFromIntegrationFixture()

        val owner = dpmDictionary.owner

        assertThat(owner.name).isEqualTo("DM Integration Fixture")
        assertThat(owner.namespace).isEqualTo("namespace")
        assertThat(owner.prefix).isEqualTo("prefix")
        assertThat(owner.location).isEqualTo("location")
        assertThat(owner.copyright).isEqualTo("copyright")
        assertThat(owner.languages).isEqualTo(hashSetOf(en, fi, sv))
        assertThat(owner.defaultLanguage).isEqualTo(en)
    }

    @Test
    fun `should produce correct diagnostic context events`() {
        performMappingFromIntegrationFixture()

        assertThat(diagnosticCollector.events).contains(
            "ENTER [MapRdsToDpm] [RDS source data to DPM model]"
        )
    }
}
