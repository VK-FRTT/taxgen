package fi.vm.yti.taxgen.sqliteoutput

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest

internal class DpmDbWriter_ContentMetric_ModuleTest : DpmDbWriter_ContentModuleTestBase() {

    override fun createDynamicTests(): List<DynamicNode> {

        return listOf(

            dynamicTest("should have Metric Members with Domain, Concept and Owner relation") {
                executeDpmDbWriterWithDefaults()

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
                    WHERE D.DomainCode = "MET"
                    """
                )

                assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                    "#MemberCode, #MemberLabel, #MemberXBRLCode, #IsDefaultMember, #DomainCode, #ConceptType, #CreationDate, #ModificationDate, #FromDate, #ToDate, #OwnerName",
                    "ed1, Met-1-Lbl-Fi, FixPrfx_met:ed1, 0, MET, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "bd2, Met-2-Lbl-Fi, FixPrfx_met:bd2, 0, MET, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "di3, Met-3-Lbl-Fi, FixPrfx_met:di3, 0, MET, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "ii4, Met-4-Lbl-Fi, FixPrfx_met:ii4, 0, MET, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "p5, Met-5-Lbl-Fi, FixPrfx_met:p5, 0, MET, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName"
                )
            },

            dynamicTest("should have Metrics with Member relation") {
                executeDpmDbWriterWithDefaults()

                val rs = dbConnection.createStatement().executeQuery(
                    """
                    SELECT
                        MEM.MemberCode,
                        MEM.MemberLabel,
                        M.DataType,
                        M.FlowType,
                        M.BalanceType,
                        D.DomainCode,
                        H.HierarchyCode,
                        M.HierarchyStartingMemberID,
                        M.IsStartingMemberIncluded
                    FROM mMetric AS M
                    INNER JOIN mMember AS MEM ON M.CorrespondingMemberID = MEM.MemberID
                    LEFT JOIN mDomain AS D ON M.ReferencedDomainID = D.DomainID
                    LEFT JOIN mHierarchy AS H ON M.ReferencedHierarchyID = H.HierarchyID
                    ORDER BY MEM.MemberLabel
                    """
                )

                assertThat(rs.toStringList()).containsExactly(
                    "#MemberCode, #MemberLabel, #DataType, #FlowType, #BalanceType, #DomainCode, #HierarchyCode, #HierarchyStartingMemberID, #IsStartingMemberIncluded",
                    "ed1, Met-1-Lbl-Fi, Enumeration/Code, Flow, Credit, ExpDom-1-Code, ExpDomHier-1-Code, nil, nil",
                    "bd2, Met-2-Lbl-Fi, Boolean, Flow, Debit, nil, nil, nil, nil",
                    "di3, Met-3-Lbl-Fi, Date, Stock, nil, nil, nil, nil, nil",
                    "ii4, Met-4-Lbl-Fi, Integer, Stock, nil, nil, nil, nil, nil",
                    "p5, Met-5-Lbl-Fi, Percent, nil, nil, nil, nil, nil, nil"
                )
            },

            dynamicTest("Metric.CorrespondingMember, Metric.ReferencedDomain and Metric.ReferencedHierarchy should bind to entities within same Owner") {
                executeDpmDbWriterWithDefaults()

                val rs = dbConnection.createStatement().executeQuery(
                    """
                     SELECT
                        Mem.MemberCode,
                        Mem.MemberLabel,
                        MemO.OwnerPrefix AS "MetricMember.Owner.Prefix",
                        DomO.OwnerPrefix AS "ReferencedDomain.Owner.Prefix",
                        HierO.OwnerPrefix AS "ReferencedHierarchy.Owner.Prefix"
                    FROM mMetric AS Met

                    INNER JOIN mMember AS Mem ON Mem.MemberID = Met.CorrespondingMemberID
                    INNER JOIN mConcept AS MemC ON MemC.ConceptID = Mem.ConceptID
                    INNER JOIN mOwner AS MemO ON MemO.OwnerID = MemC.OwnerID

                    LEFT JOIN mDomain AS Dom ON Dom.DomainID = Met.ReferencedDomainID
                    LEFT JOIN mConcept AS DomC ON DomC.ConceptID = Dom.ConceptID
                    LEFT JOIN mOwner AS DomO ON DomO.OwnerID = DomC.OwnerID

                    LEFT JOIN mHierarchy AS Hier ON Hier.HierarchyID = Met.ReferencedHierarchyID
                    LEFT JOIN mConcept AS HierC ON HierC.ConceptID = Hier.ConceptID
                    LEFT JOIN mOwner AS HierO ON HierO.OwnerID = HierC.OwnerID
                    """
                )

                assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                    "#MemberCode, #MemberLabel, #MetricMember.Owner.Prefix, #ReferencedDomain.Owner.Prefix, #ReferencedHierarchy.Owner.Prefix",
                    "ed1, Met-1-Lbl-Fi, FixPrfx, FixPrfx, FixPrfx",
                    "bd2, Met-2-Lbl-Fi, FixPrfx, nil, nil",
                    "di3, Met-3-Lbl-Fi, FixPrfx, nil, nil",
                    "ii4, Met-4-Lbl-Fi, FixPrfx, nil, nil",
                    "p5, Met-5-Lbl-Fi, FixPrfx, nil, nil"
                )
            }
        )
    }
}
