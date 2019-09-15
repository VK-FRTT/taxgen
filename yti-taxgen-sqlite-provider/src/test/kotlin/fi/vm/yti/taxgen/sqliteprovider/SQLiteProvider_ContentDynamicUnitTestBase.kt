package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.dpmmodel.ProcessingOptions
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory

internal abstract class SQLiteProvider_ContentDynamicUnitTestBase : SQLiteProvider_ContentUnitTestBase() {

    @TestFactory
    fun `When dictionary is created`(): List<DynamicNode> {
        setupDbViaDictionaryCreate(
            false,
            FixtureVariety.NONE,
            ProcessingOptions.empty()
        )

        return createDynamicTests()
    }

    @TestFactory
    fun `When dictionary is replaced`(): List<DynamicNode> {
        setupDbViaDictionaryReplace(
            false,
            FixtureVariety.NONE,
            ProcessingOptions.empty()
        )

        return createDynamicTests()
    }

    abstract fun createDynamicTests(): List<DynamicNode>
}
