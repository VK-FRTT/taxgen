package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest

internal class SQLiteProvider_ContentMetric_UnitTest : SQLiteProvider_ContentUnitTestBase() {

    override fun createDynamicTests(): List<DynamicNode> {

        return listOf(

            dynamicTest("should have Metrics with Member relation") {

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
            }
        )
    }
}