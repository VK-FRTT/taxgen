package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SQLite DPM DB content: domains")
internal class DpmDbWriter_DomainContent_UnitTest : DpmDbWriter_UnitTestBase() {

    @Test
    fun `should have Domains with Concept and Owner relation`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    D.DomainCode,
                    D.DomainLabel,
                    D.DomainDescription,
                    D.DomainXBRLCode,
                    D.DataType,
                    D.IsTypedDomain,
                    C.ConceptType,
                    C.CreationDate,
                    C.ModificationDate,
                    C.FromDate,
                    C.ToDate,
                    O.OwnerName
                FROM mDomain AS D
                INNER JOIN mConcept AS C ON C.ConceptID = D.ConceptID
                INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
                ORDER BY D.DomainCode
              """
        )

        assertThat(rs.toStringList()).containsExactly(
            "#DomainCode, #DomainLabel, #DomainDescription, #DomainXBRLCode, #DataType, #IsTypedDomain, #ConceptType, #CreationDate, #ModificationDate, #FromDate, #ToDate, #OwnerName",
            "ExpDom-Code, ExplicitDomain-Lbl-Fi, ExplicitDomain-Desc-Fi, FixPrfx_exp:ExpDom-Code, nil, 0, Domain, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "MET, Metrics, nil, MET, nil, 0, Domain, nil, nil, nil, nil, EuroFiling",
            "TypDom-Code, TypedDomain-Lbl-Fi, TypedDomain-Desc-Fi, FixPrfx_typ:TypDom-Code, Boolean, 1, Domain, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName"
        )
    }

    @Test
    fun `should have ConceptTranslations for Domains`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    D.DomainCode,
                    C.ConceptType,
                    T.Text, T.Role,
                    TL.IsoCode
                FROM mDomain AS D
                INNER JOIN mConcept AS C ON C.ConceptID = D.ConceptID
                INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
                INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                ORDER BY D.DomainCode, T.Role DESC, TL.IsoCode
              """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "#DomainCode, #ConceptType, #Text, #Role, #IsoCode",
            "ExpDom-Code, Domain, ExplicitDomain-Lbl-En, label, en",
            "ExpDom-Code, Domain, ExplicitDomain-Lbl-Fi, label, fi",
            "ExpDom-Code, Domain, ExplicitDomain-Desc-En, description, en",
            "ExpDom-Code, Domain, ExplicitDomain-Desc-Fi, description, fi",
            "MET, Domain, Metrics, label, en",
            "TypDom-Code, Domain, TypedDomain-Lbl-En, label, en",
            "TypDom-Code, Domain, TypedDomain-Lbl-Fi, label, fi",
            "TypDom-Code, Domain, TypedDomain-Desc-En, description, en",
            "TypDom-Code, Domain, TypedDomain-Desc-Fi, description, fi"
        )
    }

    @Test
    fun `should have ConceptTranslations with EN label fallback to FI content for Domains`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture(FixtureVariety.NO_EN_TRANSLATIONS))

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    D.DomainCode,
                    C.ConceptType,
                    T.Text, T.Role,
                    TL.IsoCode
                FROM mDomain AS D
                INNER JOIN mConcept AS C ON C.ConceptID = D.ConceptID
                INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
                INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                ORDER BY D.DomainCode, T.Role DESC, TL.IsoCode
              """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "#DomainCode, #ConceptType, #Text, #Role, #IsoCode",
            "ExpDom-Code, Domain, ExplicitDomain-Lbl-Fi, label, en",
            "ExpDom-Code, Domain, ExplicitDomain-Lbl-Fi, label, fi",
            "ExpDom-Code, Domain, ExplicitDomain-Desc-Fi, description, fi",
            "MET, Domain, Metrics, label, en",
            "TypDom-Code, Domain, TypedDomain-Lbl-Fi, label, en",
            "TypDom-Code, Domain, TypedDomain-Lbl-Fi, label, fi",
            "TypDom-Code, Domain, TypedDomain-Desc-Fi, description, fi"
        )
    }
}
