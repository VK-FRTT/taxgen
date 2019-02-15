package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test

@DisplayName("SQLite DPM DB content: domains")
internal class DpmDbWriter_ContentDomain_UnitTest : DpmDbWriter_ContentUnitTestBase() {

    override fun createDynamicTests(ctx: TestContext): List<DynamicNode> {

        return listOf(

            dynamicTest("should have Domains with Concept and Owner relation") {

                val rs = ctx.dbConnection.createStatement().executeQuery(
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
                    """
                )

                assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                    "#DomainCode, #DomainLabel, #DomainDescription, #DomainXBRLCode, #DataType, #IsTypedDomain, #ConceptType, #CreationDate, #ModificationDate, #FromDate, #ToDate, #OwnerName",
                    "ExpDom-1-Code, ExpDom-Lbl-Fi, ExpDom-Desc-Fi, FixPrfx_exp:ExpDom-1-Code, nil, 0, Domain, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "TypDom-1-Code, TypDom-Lbl-Fi, TypDom-Desc-Fi, FixPrfx_typ:TypDom-1-Code, Boolean, 1, Domain, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "MET, Metrics, nil, MET, nil, 0, Domain, nil, nil, 1970-01-01, nil, EuroFiling"
                )
            },

            dynamicTest("should have ConceptTranslations for Domains") {

                val rs = ctx.dbConnection.createStatement().executeQuery(
                    """
                    SELECT
                        D.DomainCode,
                        C.ConceptType,
                        T.Role,
                        TL.IsoCode,
                        T.Text
                    FROM mDomain AS D
                    INNER JOIN mConcept AS C ON C.ConceptID = D.ConceptID
                    INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
                    INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                    ORDER BY D.DomainCode, T.Role DESC, TL.IsoCode
                    """
                )

                assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                    "#DomainCode, #ConceptType, #Role, #IsoCode, #Text",
                    "ExpDom-1-Code, Domain, label, en, ExpDom-Lbl-En",
                    "ExpDom-1-Code, Domain, label, fi, ExpDom-Lbl-Fi",
                    "ExpDom-1-Code, Domain, description, en, ExpDom-Desc-En",
                    "ExpDom-1-Code, Domain, description, fi, ExpDom-Desc-Fi",
                    "MET, Domain, label, en, Metrics",
                    "TypDom-1-Code, Domain, label, en, TypDom-Lbl-En",
                    "TypDom-1-Code, Domain, label, fi, TypDom-Lbl-Fi",
                    "TypDom-1-Code, Domain, description, en, TypDom-Desc-En",
                    "TypDom-1-Code, Domain, description, fi, TypDom-Desc-Fi"
                )
            }
        )
    }

    @Test
    fun `should have ConceptTranslations with EN label fallback to FI content for Domains`() {
        val ctx = initDbViaDictionaryCreate(FixtureVariety.NO_EN_TRANSLATIONS)

        val rs = ctx.dbConnection.createStatement().executeQuery(
            """
            SELECT
                D.DomainCode,
                C.ConceptType,
                T.Role,
                TL.IsoCode,
                T.Text
            FROM mDomain AS D
            INNER JOIN mConcept AS C ON C.ConceptID = D.ConceptID
            INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
            INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
            ORDER BY D.DomainCode, T.Role DESC, TL.IsoCode
            """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "#DomainCode, #ConceptType, #Role, #IsoCode, #Text",
            "ExpDom-1-Code, Domain, label, en, ExpDom-Lbl-Fi",
            "ExpDom-1-Code, Domain, label, fi, ExpDom-Lbl-Fi",
            "ExpDom-1-Code, Domain, description, fi, ExpDom-Desc-Fi",
            "MET, Domain, label, en, Metrics",
            "TypDom-1-Code, Domain, label, en, TypDom-Lbl-Fi",
            "TypDom-1-Code, Domain, label, fi, TypDom-Lbl-Fi",
            "TypDom-1-Code, Domain, description, fi, TypDom-Desc-Fi"
        )
    }
}
