package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.HaltException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

internal class SQLiteProvider_DictionaryReplace_Common_UnitTest :
    SQLiteProvider_DictionaryReplaceUnitTestBase() {

    @Test
    fun `should fail when target DB is missing required Eurofiling owner`() {
        baselineDbConnection.createStatement().executeUpdate(
            """
            DELETE FROM mOwner WHERE mOwner.OwnerPrefix = "eu"
            """
        )

        val thrown = catchThrowable { replaceDictionaryInDb() }

        assertThat(thrown).isInstanceOf(HaltException::class.java)

        assertThat(diagnosticCollector.events).contains(
            "MESSAGE [FATAL] [Selecting 'Eurofiling' Owner from database failed. Found 0 Owners with prefix 'eu'.]"
        )
    }

    @Test
    fun `should fail when target DB is missing owner of the dictionary`() {
        baselineDbConnection.createStatement().executeUpdate(
            """
            DELETE FROM mOwner WHERE mOwner.OwnerPrefix = "FixPrfx"
            """
        )

        val thrown = catchThrowable { replaceDictionaryInDb() }

        assertThat(thrown).isInstanceOf(HaltException::class.java)

        assertThat(diagnosticCollector.events).contains(
            "MESSAGE [FATAL] [Selecting Owner from database failed. Found 0 Owners with prefix 'FixPrfx'.]"
        )
    }
}
