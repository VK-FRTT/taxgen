package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DpmDbWriter_DictionaryReplace_Common_ModuleTest : DpmDbWriter_DictionaryReplaceModuleTestBase() {

    @Test
    fun `should fail when target DB is missing required Eurofiling owner`() {
        baselineDbConnection.createStatement().executeUpdate(
            """
            DELETE FROM mOwner WHERE mOwner.OwnerPrefix = "eu"
            """
        )

        ensureHaltThrown { replaceDictionaryInDb() }

        assertThat(diagnosticCollector.events).contains(
            "MESSAGE [FATAL] [Selecting 'Eurofiling' Owner from database failed. Found 0 Owners with prefix 'eu'.]"
        )
    }

    @Test
    fun `should fail when target DB is missing owner of the dictionary`() {
        baselineDbConnection.createStatement().executeUpdate(
            """
            DELETE FROM mOwner WHERE mOwner.OwnerPrefix = "FixPrfx"
            """
        )

        ensureHaltThrown { replaceDictionaryInDb() }

        assertThat(diagnosticCollector.events).contains(
            "MESSAGE [FATAL] [Selecting Owner from database failed. Found 0 Owners with prefix 'FixPrfx'.]"
        )
    }

    @Test
    fun `should clear all existing concepts and their translations`() {
        val conceptsAndTranslationsQuery =
            """
            SELECT mConcept.ConceptType, mOwner.OwnerName, mLanguage.IsoCode, mConceptTranslation.Text
            FROM mConcept
            INNER JOIN mOwner on mOwner.OwnerID = mConcept.OwnerID
            INNER JOIN mConceptTranslation on mConceptTranslation.ConceptID =  mConcept.ConceptID
            INNER JOIN mLanguage on mLanguage.LanguageID = mConceptTranslation.LanguageID
            WHERE mLanguage.IsoCode = "en"
            ORDER BY mConcept.ConceptType ASC, mConceptTranslation.Text ASC
            """

        val expectedConceptsAndTranslationsResultRows =
            arrayOf(
                "Dimension, AFixName, en, AExpDim-1-Lbl-En",
                "Dimension, AFixName, en, ATypDim-1-Lbl-En",
                "Domain, AFixName, en, AExpDom-1-Lbl-En",
                "Domain, AFixName, en, ATypDom-1-Lbl-En",
                "Hierarchy, AFixName, en, AExpDomHier-1-Lbl-En",
                "HierarchyNode, AFixName, en, AExpDomHierNode-1-Lbl-En",
                "Member, AFixName, en, AMbr-1-Lbl-En"
            )

        val baselineRs = baselineDbConnection.createStatement().executeQuery(conceptsAndTranslationsQuery)

        assertThat(baselineRs.toStringList(includeHeader = false)).contains(*expectedConceptsAndTranslationsResultRows)

        replaceDictionaryInDb(
            FixtureVariety.ONLY_ONE_DICTIONARY
        )

        val outputRs = outputDbConnection.createStatement().executeQuery(conceptsAndTranslationsQuery)

        assertThat(outputRs.toStringList(includeHeader = false)).doesNotContainAnyElementsOf(expectedConceptsAndTranslationsResultRows.asIterable())
    }
}
