package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest

@DisplayName("SQLite DPM DB content: members")
internal class DpmDbWriter_ContentMember_UnitTest : DpmDbWriter_ContentUnitTestBase() {

    override fun createDynamicTests(ctx: TestContext): List<DynamicNode> {

        return listOf(

            dynamicTest("should have Members with Domain, Concept and Owner relation") {

                val rs = ctx.dbConnection.createStatement().executeQuery(
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
                    "Mbr-1-Code, Mbr-1-Lbl-Fi, FixPrfx_ExpDom-1-Code:Mbr-1-Code, 1, ExpDom-1-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "Mbr-2-Code, Mbr-2-Lbl-Fi, FixPrfx_ExpDom-1-Code:Mbr-2-Code, 0, ExpDom-1-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "Mbr-3-Code, Mbr-3-Lbl-Fi, FixPrfx_ExpDom-1-Code:Mbr-3-Code, 0, ExpDom-1-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "Mbr-4-Code, Mbr-4-Lbl-Fi, FixPrfx_ExpDom-1-Code:Mbr-4-Code, 0, ExpDom-1-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "Mbr-5-Code, Mbr-5-Lbl-Fi, FixPrfx_ExpDom-1-Code:Mbr-5-Code, 0, ExpDom-1-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "ed1, Met-1-Lbl-Fi, FixPrfx_met:ed1, 0, MET, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "bd2, Met-2-Lbl-Fi, FixPrfx_met:bd2, 0, MET, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "di3, Met-3-Lbl-Fi, FixPrfx_met:di3, 0, MET, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "ii4, Met-4-Lbl-Fi, FixPrfx_met:ii4, 0, MET, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "p5, Met-5-Lbl-Fi, FixPrfx_met:p5, 0, MET, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName"
                )
            },

            dynamicTest("should have ConceptTranslations for DPM Member") {

                val rs = ctx.dbConnection.createStatement().executeQuery(
                    """
                    SELECT
                        M.MemberCode,
                        C.ConceptType,
                        T.Role,
                        TL.IsoCode,
                        T.Text
                    FROM mMember AS M
                    INNER JOIN mConcept AS C ON C.ConceptID = M.ConceptID
                    INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
                    INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                    WHERE M.MemberCode = 'Mbr-1-Code'
                    ORDER BY T.Role DESC, TL.IsoCode ASC
                    """
                )

                assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                    "#MemberCode, #ConceptType, #Role, #IsoCode, #Text",
                    "Mbr-1-Code, Member, label, en, Mbr-1-Lbl-En",
                    "Mbr-1-Code, Member, label, fi, Mbr-1-Lbl-Fi",
                    "Mbr-1-Code, Member, description, en, Mbr-1-Desc-En",
                    "Mbr-1-Code, Member, description, fi, Mbr-1-Desc-Fi"
                )
            }
        )
    }
}
