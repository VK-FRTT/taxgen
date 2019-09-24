package fi.vm.yti.taxgen.rdsdpmmapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RdsToDpmMapper_IntegrationFixtureContent_Common_ModuleTest : RdsToDpmMapper_ModuleTestBase() {

    @Test
    fun `should produce DPM Dictionary with proper Owner`() {
        val dpmDictionary = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture()

        val owner = dpmDictionary.owner

        assertThat(owner.name).isEqualTo("DM Integration Fixture")
        assertThat(owner.namespace).isEqualTo("namespace")
        assertThat(owner.prefix).isEqualTo("prefix")
        assertThat(owner.location).isEqualTo("location")
        assertThat(owner.copyright).isEqualTo("copyright")
        assertThat(owner.languages).isEqualTo(hashSetOf(en, fi, sv))
    }

    @Test
    fun `should produce correct diagnostic context events`() {
        executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture()

        assertThat(diagnosticCollector.events).contains(
            "ENTER [RdsToDpmMapper] [RDS source data to DPM model]"
        )
    }
}
