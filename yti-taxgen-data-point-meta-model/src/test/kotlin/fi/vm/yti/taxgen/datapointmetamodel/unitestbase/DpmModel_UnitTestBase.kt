package fi.vm.yti.taxgen.datapointmetamodel.unitestbase

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrorCollector
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.datapointmetamodel.Language
import fi.vm.yti.taxgen.datapointmetamodel.Member
import fi.vm.yti.taxgen.datapointmetamodel.dpmTestData
import fi.vm.yti.taxgen.datapointmetamodel.datafactory.Factory
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import kotlin.reflect.KClass

internal open class DpmModel_UnitTestBase<T : Validatable>(
    val kClass: KClass<T>
) {
    protected var attributeOverrides: Map<String, Any?>? = null
    protected var instance: T? = null
    protected var validationErrors: List<String>? = null

    @BeforeAll
    fun init0() {
        Factory.registerDefinitions(dpmTestData())
    }

    @BeforeEach
    fun init() {
        attributeOverrides = null
        instance = null
        validationErrors = null
    }

    protected fun attributeOverrides(vararg overrides: Pair<String, Any?>) {
        attributeOverrides = overrides.toMap()
    }

    protected fun instantiateAndValidate() {
        require(attributeOverrides != null)

        val attributes = Factory.Builder.attributesFor(kClass, attributeOverrides)

        val collector = ValidationErrorCollector()
        instance = Factory.Builder.instantiate(kClass, attributes) as T
        instance!!.validate(collector)
        validationErrors = collector.errorsInSimpleFormat()
    }

    protected fun language(languageCode: String) = Language.findByIso6391Code(languageCode)!!

    protected fun member(memberCode: String, default: Boolean): Member {
        return Member(
            concept = Factory.instantiate(),
            memberCode = memberCode,
            defaultMember = default
        )
    }

    protected fun explicitDomain(domainCode: String): ExplicitDomain {
        return ExplicitDomain(
            concept = Factory.instantiate(),
            domainCode = domainCode,
            members = listOf(Factory.instantiate())
        )
    }
}
