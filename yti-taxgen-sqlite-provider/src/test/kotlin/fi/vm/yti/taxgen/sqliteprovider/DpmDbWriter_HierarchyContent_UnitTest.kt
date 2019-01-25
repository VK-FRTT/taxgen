package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SQLite DPM DB content: hierarchies")
internal class DpmDbWriter_HierarchyContent_UnitTest : DpmDbWriter_UnitTestBase() {

    @Test
    fun `should have DPM Hierarchy with Domain, Concept and Owner relation`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

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

        assertThat(rs.toStringList()).contains(
            "#HierarchyCode, #HierarchyLabel, #HierarchyDescription, #ConceptType, #CreationDate, #ModificationDate, #FromDate, #ToDate, #DomainCode, #OwnerName",
            "Hier-Code, Hierarchy-Lbl-Fi, Hierarchy-Desc-Fi, Hierarchy, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, ExpDom-Code, FixName"
        )

        //TODO - verify metric hierarchy
    }

    @Test
    fun `should have ConceptTranslations for DPM Hierarchy`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    H.HierarchyCode,
                    C.ConceptType,
                    T.Text, T.Role,
                    TL.IsoCode
                FROM mHierarchy AS H
                INNER JOIN mConcept AS C ON C.ConceptID = H.ConceptID
                INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
                INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                WHERE H.HierarchyCode = 'Hier-Code'
                ORDER BY T.Role DESC, TL.IsoCode ASC
              """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "#HierarchyCode, #ConceptType, #Text, #Role, #IsoCode",
            "Hier-Code, Hierarchy, Hierarchy-Lbl-En, label, en",
            "Hier-Code, Hierarchy, Hierarchy-Lbl-Fi, label, fi",
            "Hier-Code, Hierarchy, Hierarchy-Desc-En, description, en",
            "Hier-Code, Hierarchy, Hierarchy-Desc-Fi, description, fi"
        )
    }

    @Test
    fun `should detect when multiple HierarchyNodes refer same Member`() {
        val dpmDictionariesFixture = dpmDictionaryFixture(FixtureVariety.SECOND_HIERARCHY_NODE_REFERS_SAME_MEMBER)

        val thrown = catchThrowable { dbWriter.writeDpmDb(dpmDictionariesFixture) }

        assertThat(thrown)
            .isInstanceOf(org.jetbrains.exposed.exceptions.ExposedSQLException::class.java)
            .hasMessageContaining("UNIQUE constraint failed")
            .hasMessageContaining("mHierarchyNode.HierarchyID, mHierarchyNode.MemberID")
    }
}
