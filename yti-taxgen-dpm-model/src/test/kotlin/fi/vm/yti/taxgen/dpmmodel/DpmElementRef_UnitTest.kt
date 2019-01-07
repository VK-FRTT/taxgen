package fi.vm.yti.taxgen.dpmmodel

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

internal class DpmElementRef_UnitTest {

    @Test
    fun `DpmElementRefs should not equal when other is null`() {

        val ref1 = dpmElementRef<Member>("uri_1", "diagnostic_label")
        val ref2 = null

        assertThat(ref1.equals(ref2)).isFalse()
    }

    @Test
    fun `DpmElementRefs should equal and == when type and URI are same, despite diagnostic label`() {

        val ref1 = dpmElementRef<Member>("uri_1", "diagnostic_label_1")
        val ref2 = dpmElementRef<Member>("uri_1", "diagnostic_label_2")

        assertThat(ref1.equals(ref2)).isTrue()

        assertThat(ref1 == ref2).isTrue()

        assertThat(ref1.hashCode() == ref2.hashCode()).isTrue()
    }

    @Test
    fun `DpmElementRefs should not equal when URI not same`() {

        val ref1 = dpmElementRef<Member>("uri_1", "diagnostic_label")
        val ref2 = dpmElementRef<Member>("uri_2", "diagnostic_label")

        assertThat(ref1.equals(ref2)).isFalse()
    }

    @Test
    fun `DpmElementRefs should not equal when types not same`() {

        val ref1 = dpmElementRef<Member>("uri", "diagnostic_label")
        val ref2 = dpmElementRef<HierarchyNode>("uri", "diagnostic_label")

        assertThat(ref1.equals(ref2)).isFalse()
    }

    @Test
    fun `DpmElement # typeName should fail for non DpmElement class`() {

        val thrown = catchThrowable { DpmElement.typeName(String::class) }

        assertThat(thrown)
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Expecting a DpmElement based class")
    }
}
