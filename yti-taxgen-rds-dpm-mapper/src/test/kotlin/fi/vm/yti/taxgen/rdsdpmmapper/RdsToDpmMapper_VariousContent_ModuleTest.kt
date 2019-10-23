package fi.vm.yti.taxgen.rdsdpmmapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RdsToDpmMapper_VariousContent_ModuleTest : RdsToDpmMapper_ModuleTestBase() {

    @Test
    fun `should produce 3 DPM Dictionaries with proper Owners`() {
        val dpmDictionaries = executeRdsToDpmMapperAndGetDictionariesFrom("3_empty_dictionaries")

        assertThat(dpmDictionaries.size).isEqualTo(3)

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

    @Test
    fun `should prefix ExplicitDomain Member codes with prefix configured in ExplicitDomain`() {
        val dpmDictionaries = executeRdsToDpmMapperAndGetDictionariesFrom("explicit_domain_with_member_code_prefix")
        val domain = dpmDictionaries[0].explicitDomains.find { it.domainCode == "EDA" }!!

        assertThat(domain.members.size).isEqualTo(13)
        assertThat(domain.members[0].memberCode).isEqualTo("code-prefix-EDA-x1")

        domain.members.forEach {
            assertThat(it.memberCode.startsWith("code-prefix-")).isTrue()
        }
    }
}
