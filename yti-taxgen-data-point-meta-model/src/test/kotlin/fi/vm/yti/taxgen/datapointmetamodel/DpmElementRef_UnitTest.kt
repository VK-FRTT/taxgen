package fi.vm.yti.taxgen.datapointmetamodel

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

internal class DpmElementRef_UnitTest {

    @Test
    fun `DpmElementRefs should equal when type & id same`() {

        val ref1 = dpmElementRef<Member>("id_1")
        val ref2 = dpmElementRef<Member>("id_1")

        val res = ref1.equals(ref2)

        assertThat(res).isTrue()
    }

    @Test
    fun `DpmElementRefs should not equal when ids not same`() {

        val ref1 = dpmElementRef<Member>("id_1")
        val ref2 = dpmElementRef<Member>("id_2")

        val res = ref1.equals(ref2)

        assertThat(res).isFalse()
    }

    @Test
    fun `DpmElementRefs should not equal when types not same`() {

        val ref1 = dpmElementRef<Member>("id_1")
        val ref2 = dpmElementRef<HierarchyNode>("id_1")

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
