package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SQLite DPM DB content: hierarchies")
internal class DpmDbWriter_HierarchyContent_UnitTest : DpmDbWriter_UnitTestBase() {

    @Test
    fun `should have Hierarchy with Domain, Concept and Owner relation`() {
        runDictionaryCreateDbWriter()

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    H.HierarchyCode,
                    H.HierarchyLabel,
                    H.HierarchyDescription,
                    C.ConceptType,
                    C.CreationDate,
                    C.ModificationDate,
                    C.FromDate,
                    C.ToDate,
                    D.DomainCode,
                    O.OwnerName
                FROM mHierarchy AS H
                INNER JOIN mConcept AS C ON C.ConceptID = H.ConceptID
                INNER JOIN mDomain AS D ON D.DomainID = H.DomainID
                INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
              """
        )

        assertThat(rs.toStringList()).containsExactly(
            "#HierarchyCode, #HierarchyLabel, #HierarchyDescription, #ConceptType, #CreationDate, #ModificationDate, #FromDate, #ToDate, #DomainCode, #OwnerName",
            "ExpDomHier-1-Code, ExpDomHier-Lbl-Fi, ExpDomHier-Desc-Fi, Hierarchy, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, ExpDom-1-Code, FixName",
            "MetHier-1-Code, MetHier-Lbl-Fi, MetHier-Desc-Fi, Hierarchy, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, MET, FixName"
        )
    }

    @Test
    fun `should have ConceptTranslations for DPM Hierarchy`() {
        runDictionaryCreateDbWriter()

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    H.HierarchyCode,
                    C.ConceptType,
                    T.Role,
                    TL.IsoCode,
                    T.Text
                FROM mHierarchy AS H
                INNER JOIN mConcept AS C ON C.ConceptID = H.ConceptID
                INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
                INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                WHERE H.HierarchyCode = 'ExpDomHier-1-Code'
                ORDER BY T.Role DESC, TL.IsoCode ASC
              """
        )

        assertThat(rs.toStringList()).containsExactly(
            "#HierarchyCode, #ConceptType, #Role, #IsoCode, #Text",
            "ExpDomHier-1-Code, Hierarchy, label, en, ExpDomHier-Lbl-En",
            "ExpDomHier-1-Code, Hierarchy, label, fi, ExpDomHier-Lbl-Fi",
            "ExpDomHier-1-Code, Hierarchy, description, en, ExpDomHier-Desc-En",
            "ExpDomHier-1-Code, Hierarchy, description, fi, ExpDomHier-Desc-Fi"
        )
    }

    @Test
    fun `should detect when multiple HierarchyNodes refer same Member`() {
        val thrown = catchThrowable { runDictionaryCreateDbWriter(FixtureVariety.SECOND_HIERARCHY_NODE_REFERS_SAME_MEMBER) }

        assertThat(thrown)
            .isInstanceOf(org.jetbrains.exposed.exceptions.ExposedSQLException::class.java)
            .hasMessageContaining("UNIQUE constraint failed")
            .hasMessageContaining("mHierarchyNode.HierarchyID, mHierarchyNode.MemberID")
    }
}
