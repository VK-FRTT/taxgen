package fi.vm.yti.taxgen.datapointmetamodel

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

internal class DpmElementRef_UnitTest {

    @Test
    fun `DpmElementRefs should not equal when other is null`() {

        val ref1 = dpmElementRef<Member>("id_1", "diagnostic_label")
        val ref2 = null

        val res = ref1.equals(ref2)
        assertThat(res).isFalse()
    }

    @Test
    fun `DpmElementRefs should equal when type & id same, despite diagnostic label`() {

        val ref1 = dpmElementRef<Member>("id_1", "diagnostic_label")
        val ref2 = dpmElementRef<Member>("id_1", "diagnostic_label")

        val res = ref1.equals(ref2)
        assertThat(res).isTrue()

        assertThat(ref1.hashCode() == ref2.hashCode()).isTrue()
    }

    @Test
    fun `DpmElementRefs should == when type & id same, despite diagnostic label`() {

        val ref1 = dpmElementRef<Member>("id_1", "diagnostic_label")
        val ref2 = dpmElementRef<Member>("id_1", "diagnostic_label")

        val res = ref1 == ref2
        assertThat(res).isTrue()

        assertThat(ref1.hashCode() == ref2.hashCode()).isTrue()
    }

    @Test
    fun `DpmElementRefs should not equal when ids not same`() {

        val ref1 = dpmElementRef<Member>("id_1", "diagnostic_label")
        val ref2 = dpmElementRef<Member>("id_2", "diagnostic_label")

        val res = ref1.equals(ref2)
        assertThat(res).isFalse()
    }

    @Test
    fun `DpmElementRefs should not equal when types not same`() {

        val ref1 = dpmElementRef<Member>("id_1", "diagnostic_label")
        val ref2 = dpmElementRef<HierarchyNode>("id_1", "diagnostic_label")

        val res = ref1.equals(ref2)

        assertThat(res).isFalse()
    }

    @Test
    fun `DpmElement # typeName should fail for non DpmElement class`() {

        val thrown = catchThrowable { DpmElement.typeName(String::class) }

        assertThat(thrown)
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Expecting a DpmElement based class")
    }
}
