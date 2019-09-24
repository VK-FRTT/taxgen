package fi.vm.yti.taxgen.rdsdpmmapper

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/*
TODO - Things to test:
- Member code prefix handling
- Code & Extension Members disordered
*/

internal class RdsToDpmMapper_VariousContent_ModuleTest : RdsToDpmMapper_ModuleTestBase() {

    @Test
    fun `should produce 3 DPM Dictionaries with proper Owners`() {
        val dpmDictionaries = executeRdsToDpmMapperAndGetDictionariesFrom("3_empty_dictionaries")

        Assertions.assertThat(dpmDictionaries.size).isEqualTo(3)

        dpmDictionaries[0].owner.apply {
            assertThat(name).isEqualTo("Owner 1/3")
            assertThat(namespace).isEqualTo("namespace")
            assertThat(prefix).isEqualTo("prefix")
            assertThat(location).isEqualTo("location")
            assertThat(copyright).isEqualTo("copyright")
            assertThat(languages).isEqualTo(hashSetOf(en))
        }

        dpmDictionaries[1].owner.apply {
            assertThat(name).isEqualTo("Owner 2/3")
            assertThat(namespace).isEqualTo("namespace")
            assertThat(prefix).isEqualTo("prefix")
            assertThat(location).isEqualTo("location")
            assertThat(copyright).isEqualTo("copyright")
            assertThat(languages).isEqualTo(hashSetOf(fi))
        }

        dpmDictionaries[2].owner.apply {
            assertThat(name).isEqualTo("Owner 3/3")
            assertThat(namespace).isEqualTo("namespace")
            assertThat(prefix).isEqualTo("prefix")
            assertThat(location).isEqualTo("location")
            assertThat(copyright).isEqualTo("copyright")
            assertThat(languages).isEqualTo(hashSetOf(sv))
        }
    }
}
