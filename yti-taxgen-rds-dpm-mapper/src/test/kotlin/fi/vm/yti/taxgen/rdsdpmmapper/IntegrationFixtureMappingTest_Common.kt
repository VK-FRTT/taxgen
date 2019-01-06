package fi.vm.yti.taxgen.rdsdpmmapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Mapping Integration Fixture to DPM model - Common")
internal class IntegrationFixtureMappingTest_Common : IntegrationFixtureMappingTestBase() {

    @Test
    fun `should produce 1 DPM Dictionary with proper Owner`() {
        val dpmDictionaries = performMappingAndGetAll()

        assertThat(dpmDictionaries.size).isEqualTo(1)

        val owner = dpmDictionaries[0].owner

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
        performMappingAndGetAll()

        assertThat(diagnosticCollector.events).contains(
            "ENTER [MapRdsToDpm] [RDS source data to DPM model]"
        )
    }
}
