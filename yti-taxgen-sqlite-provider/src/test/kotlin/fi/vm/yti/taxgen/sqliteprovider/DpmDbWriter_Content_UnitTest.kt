package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("SQLite DPM Database - data content")
internal class DpmDbWriter_Content_UnitTest : DpmDbWriter_UnitTestBase() {

    @Test
    fun `should have all configured languages`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

        val rs = dbConnection.createStatement().executeQuery("SELECT IsoCode FROM mLanguage")
        val dbIsoCodes = rs.toStringList(false)

        val allKnownIsoCodes = Language.languages().map { it.iso6391Code }.toList()
        assertThat(dbIsoCodes).containsExactlyInAnyOrderElementsOf(allKnownIsoCodes)

        assertThat(dbIsoCodes).size().isEqualTo(24)
    }

    @Test
    fun `should have English language but no Concept nor ConceptTranslation relations`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    L.IsoCode AS LanguageIsoCode, L.LanguageName, L.EnglishName AS LanguageEnglishName,
                    C.ConceptType, C.OwnerID, C.CreationDate, C.ModificationDate, C.FromDate, C.ToDate,
                    T.Text, T.Role,
                    TL.IsoCode
                FROM mLanguage AS L
                LEFT JOIN mConcept AS C ON C.ConceptID = L.ConceptID
                LEFT JOIN mConceptTranslation AS T ON T.ConceptID = L.ConceptID
                LEFT JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                WHERE L.IsoCode = 'en'
                """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "LanguageIsoCode, LanguageName, LanguageEnglishName, ConceptType, OwnerID, CreationDate, ModificationDate, FromDate, ToDate, Text, Role, IsoCode",
            "en, English, English, nil, nil, nil, nil, nil, nil, nil, nil, nil"
        )
    }

    @Test
    fun `should have DPM Owner`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    O.OwnerName, O.OwnerNamespace, O.OwnerLocation, O.OwnerPrefix, O.OwnerCopyright, O.ParentOwnerID, O.ConceptID
                FROM mOwner AS O
                """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "OwnerName, OwnerNamespace, OwnerLocation, OwnerPrefix, OwnerCopyright, ParentOwnerID, ConceptID",
            "FixName, FixNamespace, FixLocation, FixPrefix, FixCopyright, nil, nil",
            "EuroFiling, http://www.eurofiling.info/xbrl/, http://www.eurofiling.info/eu/fr/xbrl/, eu, (C) Eurofiling, nil, nil"
        )
    }

    @Test
    fun `should have DPM ExplicitDomain with Concept and Owner relation`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    D.DomainCode, D.DomainLabel, D.DomainDescription, D.DomainXBRLCode, D.DataType, D.IsTypedDomain,
                    C.ConceptType, C.CreationDate, C.ModificationDate, C.FromDate, C.ToDate,
                    O.OwnerName
                FROM mDomain AS D
                INNER JOIN mConcept AS C ON C.ConceptID = D.ConceptID
                INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
              """
        )

        assertThat(rs.toStringList()).contains(
            "DomainCode, DomainLabel, DomainDescription, DomainXBRLCode, DataType, IsTypedDomain, ConceptType, CreationDate, ModificationDate, FromDate, ToDate, OwnerName",
            "MET, Metrics, nil, MET, nil, 0, Domain, nil, nil, nil, nil, EuroFiling",
            "Domain-Code, ExplicitDomain-LabelFi, ExplicitDomain-DescriptionFi, FixPrefix_exp:Domain-Code, nil, 0, Domain, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName"
        )
    }

    @Test
    fun `should have ConceptTranslations for DPM ExplicitDomain`() {
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
                WHERE D.DomainCode = 'Domain-Code'
              """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "DomainCode, ConceptType, Text, Role, IsoCode",
            "Domain-Code, Domain, ExplicitDomain-LabelEn, label, en",
            "Domain-Code, Domain, ExplicitDomain-LabelFi, label, fi"
        )
    }

    @Test
    fun `should have ConceptTranslations with EN fallbacking to FI content for DPM ExplicitDomain`() {
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
                WHERE D.DomainCode = 'Domain-Code'
              """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "DomainCode, ConceptType, Text, Role, IsoCode",
            "Domain-Code, Domain, ExplicitDomain-LabelFi, label, en",
            "Domain-Code, Domain, ExplicitDomain-LabelFi, label, fi"
        )
    }

    @Test
    fun `should have DPM Members with Domain, Concept and Owner relation`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    M.MemberCode, M.MemberLabel, M.MemberXBRLCode, M.IsDefaultMember,
                    D.DomainCode,
                    C.ConceptType, C.CreationDate, C.ModificationDate, C.FromDate, C.ToDate,
                    O.OwnerName
                FROM mMember AS M
                INNER JOIN mDomain AS D ON D.DomainID = M.DomainID
                INNER JOIN mConcept AS C ON C.ConceptID = M.ConceptID
                INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
              """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "MemberCode, MemberLabel, MemberXBRLCode, IsDefaultMember, DomainCode, ConceptType, CreationDate, ModificationDate, FromDate, ToDate, OwnerName",
            "Member-1-Code, Member-1-LabelFi, FixPrefix_Domain-Code:Member-1-Code, 1, Domain-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "Member-2-Code, Member-2-LabelFi, FixPrefix_Domain-Code:Member-2-Code, 0, Domain-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "Member-3-Code, Member-3-LabelFi, FixPrefix_Domain-Code:Member-3-Code, 0, Domain-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "Member-4-Code, Member-4-LabelFi, FixPrefix_Domain-Code:Member-4-Code, 0, Domain-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "Member-5-Code, Member-5-LabelFi, FixPrefix_Domain-Code:Member-5-Code, 0, Domain-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName"
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
                WHERE M.MemberCode = 'Member-1-Code'
              """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "MemberCode, ConceptType, Text, Role, IsoCode",
            "Member-1-Code, Member, Member-1-LabelEn, label, en",
            "Member-1-Code, Member, Member-1-LabelFi, label, fi"
        )
    }

    @Test
    fun `should have DPM Hierarchy with Domain, Concept and Owner relation`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    H.HierarchyCode, H.HierarchyLabel, H.HierarchyDescription,
                    C.ConceptType, C.CreationDate, C.ModificationDate, C.FromDate, C.ToDate,
                    D.DomainCode,
                    O.OwnerName
                FROM mHierarchy AS H
                INNER JOIN mConcept AS C ON C.ConceptID = H.ConceptID
                INNER JOIN mDomain AS D ON D.DomainID = H.DomainID
                INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
              """
        )

        assertThat(rs.toStringList()).contains(
            "HierarchyCode, HierarchyLabel, HierarchyDescription, ConceptType, CreationDate, ModificationDate, FromDate, ToDate, DomainCode, OwnerName",
            "Hierarchy-Code, Hierarchy-LabelFi, Hierarchy-DescriptionFi, Hierarchy, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, Domain-Code, FixName"
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
                WHERE H.HierarchyCode = 'Hierarchy-Code'
              """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "HierarchyCode, ConceptType, Text, Role, IsoCode",
            "Hierarchy-Code, Hierarchy, Hierarchy-LabelEn, label, en",
            "Hierarchy-Code, Hierarchy, Hierarchy-LabelFi, label, fi"
        )
    }

    @Test
    fun `should have DPM HierarchyNodes with Member, Concept and Owner relation`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
					N.HierarchyNodeLabel, N.ComparisonOperator, N.UnaryOperator, N.IsAbstract, N.'Order', N.Level, N.Path,
					H.HierarchyCode,
					M.MemberCode,
					P.MemberCode AS ParentMemberCode,
                    C.ConceptType, C.CreationDate, C.ModificationDate, C.FromDate, C.ToDate,
                    O.OwnerName
                FROM mHierarchyNode AS N
                INNER JOIN mHierarchy AS H ON H.HierarchyID = N.HierarchyID
				INNER JOIN mMember AS M ON M.MemberID = N.MemberID
				LEFT JOIN mMember AS P ON P.MemberID = N.ParentMemberID
                INNER JOIN mConcept AS C ON C.ConceptID = N.ConceptID
                INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
              """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "HierarchyNodeLabel, ComparisonOperator, UnaryOperator, IsAbstract, Order, Level, Path, HierarchyCode, MemberCode, ParentMemberCode, ConceptType, CreationDate, ModificationDate, FromDate, ToDate, OwnerName",
            "HierarchyNode-1-LabelFi, nil, nil, 0, 1, 1, nil, Hierarchy-Code, Member-1-Code, nil, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "HierarchyNode-2-LabelFi, =, +, 0, 2, 1, nil, Hierarchy-Code, Member-2-Code, nil, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "HierarchyNode-2.1-LabelFi, =, +, 0, 1, 2, nil, Hierarchy-Code, Member-3-Code, Member-2-Code, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "HierarchyNode-2.1.1-LabelFi, nil, nil, 0, 1, 3, nil, Hierarchy-Code, Member-4-Code, Member-3-Code, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "HierarchyNode-2.2-LabelFi, nil, nil, 0, 2, 2, nil, Hierarchy-Code, Member-5-Code, Member-2-Code, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName"
        )
    }

    @Test
    fun `should have ConceptTranslations for DPM HierarchyNode`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    N.HierarchyNodeLabel,
                    C.ConceptType,
                    T.Text, T.Role,
                    TL.IsoCode
                FROM mHierarchyNode AS N
                INNER JOIN mConcept AS C ON C.ConceptID = N.ConceptID
                INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
                INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                WHERE N.'Order'= 1 AND N.Level = 1
              """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "HierarchyNodeLabel, ConceptType, Text, Role, IsoCode",
            "HierarchyNode-1-LabelFi, HierarchyNode, HierarchyNode-1-LabelEn, label, en",
            "HierarchyNode-1-LabelFi, HierarchyNode, HierarchyNode-1-LabelFi, label, fi"
        )
    }

    @Nested
    @DisplayName("diagnostic events")
    inner class DiagnosticEvents {

        @Test
        fun `should contain proper context events`() {
            dbWriter.writeDpmDb(dpmDictionaryFixture())

            assertThat(diagnosticCollector.eventsString()).contains(
                "ENTER [WriteSQLiteDb]",
                "EXIT [WriteSQLiteDb]"
            )
        }
    }

    @Nested
    @DisplayName("database constraints")
    inner class DatabaseConstraints {

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
}
