package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SQLite DPM DB content: members")
internal class DpmDbWriter_MemberContent_UnitTest : DpmDbWriter_UnitTestBase() {

    @Test
    fun `should have DPM Members with Domain, Concept and Owner relation`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    M.MemberCode,
                    M.MemberLabel,
                    M.MemberXBRLCode,
                    M.IsDefaultMember,
                    D.DomainCode,
                    C.ConceptType,
                    C.CreationDate,
                    C.ModificationDate,
                    C.FromDate,
                    C.ToDate,
                    O.OwnerName
                FROM mMember AS M
                INNER JOIN mDomain AS D ON D.DomainID = M.DomainID
                INNER JOIN mConcept AS C ON C.ConceptID = M.ConceptID
                INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
              """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "#MemberCode, #MemberLabel, #MemberXBRLCode, #IsDefaultMember, #DomainCode, #ConceptType, #CreationDate, #ModificationDate, #FromDate, #ToDate, #OwnerName",
            "Mbr-1-Code, Mbr-1-Lbl-Fi, FixPrfx_ExpDom-Code:Mbr-1-Code, 1, ExpDom-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "Mbr-2-Code, Mbr-2-Lbl-Fi, FixPrfx_ExpDom-Code:Mbr-2-Code, 0, ExpDom-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "Mbr-3-Code, Mbr-3-Lbl-Fi, FixPrfx_ExpDom-Code:Mbr-3-Code, 0, ExpDom-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "Mbr-4-Code, Mbr-4-Lbl-Fi, FixPrfx_ExpDom-Code:Mbr-4-Code, 0, ExpDom-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "Mbr-5-Code, Mbr-5-Lbl-Fi, FixPrfx_ExpDom-Code:Mbr-5-Code, 0, ExpDom-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName"
        )
    }

    @Test
    fun `should have ConceptTranslations for DPM Member`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    M.MemberCode,
                    C.ConceptType,
                    T.Text, T.Role,
                    TL.IsoCode
                FROM mMember AS M
                INNER JOIN mConcept AS C ON C.ConceptID = M.ConceptID
                INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
                INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                WHERE M.MemberCode = 'Mbr-1-Code'
                ORDER BY T.Role DESC, TL.IsoCode ASC
              """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "#MemberCode, #ConceptType, #Text, #Role, #IsoCode",
            "Mbr-1-Code, Member, Mbr-1-Lbl-En, label, en",
            "Mbr-1-Code, Member, Mbr-1-Lbl-Fi, label, fi",
            "Mbr-1-Code, Member, Mbr-1-Desc-En, description, en",
            "Mbr-1-Code, Member, Mbr-1-Desc-Fi, description, fi"
        )
    }
}
