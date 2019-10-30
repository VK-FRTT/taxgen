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
    fun `should produce correct diagnostic events with configured source language titles`() {
        diagnosticBridge.setDiagnosticSourceLanguages(
            listOf(en, fi, sv)
        )

        executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture()

        assertThat(diagnosticCollector.events).contains(
            "ENTER [RdsCodeList] []",
            "UPDATE [RdsCodeList] [Explicit domain A 2018-1]",
            "ENTER [RdsCodesPage] []",
            "EXIT [RdsCodesPage]",
            "ENTER [RdsExtension] []",
            "UPDATE [RdsExtension] [EDA hierarchy 1]",
            "ENTER [RdsExtensionMembersPage] []",
            "EXIT [RdsExtensionMembersPage]",
            "EXIT [RdsExtension]",
            "ENTER [RdsExtension] []",
            "UPDATE [RdsExtension] [EDA hierarchy 2]",
            "ENTER [RdsExtensionMembersPage] []",
            "EXIT [RdsExtensionMembersPage]",
            "EXIT [RdsExtension]",
            "ENTER [RdsExtension] []",
            "UPDATE [RdsExtension] [EDA hierarchy 10]",
            "ENTER [RdsExtensionMembersPage] []",
            "EXIT [RdsExtensionMembersPage]",
            "EXIT [RdsExtension]",
            "EXIT [RdsCodeList]"
        )
    }

    @Test
    fun `should produce diagnostic context events with empty titles when no source language is configured`() {

        diagnosticBridge.setDiagnosticSourceLanguages(
            emptyList()
        )

        executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture()

        assertThat(diagnosticCollector.events).contains(
            "ENTER [RdsCodeList] []",
            "UPDATE [RdsCodeList] []",
            "ENTER [RdsCodesPage] []",
            "EXIT [RdsCodesPage]",
            "ENTER [RdsExtension] []",
            "UPDATE [RdsExtension] []",
            "ENTER [RdsExtensionMembersPage] []",
            "EXIT [RdsExtensionMembersPage]",
            "EXIT [RdsExtension]",
            "ENTER [RdsExtension] []",
            "UPDATE [RdsExtension] []",
            "ENTER [RdsExtensionMembersPage] []",
            "EXIT [RdsExtensionMembersPage]",
            "EXIT [RdsExtension]",
            "ENTER [RdsExtension] []",
            "UPDATE [RdsExtension] []",
            "ENTER [RdsExtensionMembersPage] []",
            "EXIT [RdsExtensionMembersPage]",
            "EXIT [RdsExtension]",
            "EXIT [RdsCodeList]"
        )
    }

    @Test
    fun `should produce diagnostic context events with empty titles when no matching source language is configured`() {

        diagnosticBridge.setDiagnosticSourceLanguages(
            listOf(en)
        )

        executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture()

        assertThat(diagnosticCollector.events).contains(
            "ENTER [RdsCodeList] []",
            "UPDATE [RdsCodeList] []",
            "ENTER [RdsCodesPage] []",
            "EXIT [RdsCodesPage]",
            "ENTER [RdsExtension] []",
            "UPDATE [RdsExtension] []",
            "ENTER [RdsExtensionMembersPage] []",
            "EXIT [RdsExtensionMembersPage]",
            "EXIT [RdsExtension]",
            "ENTER [RdsExtension] []",
            "UPDATE [RdsExtension] []",
            "ENTER [RdsExtensionMembersPage] []",
            "EXIT [RdsExtensionMembersPage]",
            "EXIT [RdsExtension]",
            "ENTER [RdsExtension] []",
            "UPDATE [RdsExtension] []",
            "ENTER [RdsExtensionMembersPage] []",
            "EXIT [RdsExtensionMembersPage]",
            "EXIT [RdsExtension]",
            "EXIT [RdsCodeList]"
        )
    }
}
