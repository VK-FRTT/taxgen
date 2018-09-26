package fi.vm.yti.taxgen.datapointmetamodel.unitestbase

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrorCollector
import fi.vm.yti.taxgen.datapointmetamodel.DpmElementRef
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.datapointmetamodel.Hierarchy
import fi.vm.yti.taxgen.datapointmetamodel.HierarchyNode
import fi.vm.yti.taxgen.datapointmetamodel.Language
import fi.vm.yti.taxgen.datapointmetamodel.Member
import fi.vm.yti.taxgen.datapointmetamodel.datafactory.Factory
import fi.vm.yti.taxgen.datapointmetamodel.dpmTestData
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

        @Suppress("UNCHECKED_CAST")
        instance = Factory.Builder.instantiate(kClass, attributes) as T

        instance!!.validate(collector)
        validationErrors = collector.errorsInSimpleFormat()
    }

    protected fun language(languageCode: String) = Language.findByIso6391Code(languageCode)!!

    protected fun explicitDomain(domainCode: String): ExplicitDomain {
        return ExplicitDomain(
            id = "ed_1",
            concept = Factory.instantiate(),
            domainCode = domainCode,
            members = listOf(Factory.instantiate()),
            hierarchies = listOf()
        )
    }

    protected fun member(id: String, memberCode: String, default: Boolean): Member {
        return Member(
            id = id,
            concept = Factory.instantiate(),
            memberCode = memberCode,
            defaultMember = default
        )
    }

    protected fun hierarchy(id: String, hierarchyCode: String, vararg roots: HierarchyNode): Hierarchy {
        return Hierarchy(
            id = id,
            concept = Factory.instantiate(),
            hierarchyCode = hierarchyCode,
            rootNodes = roots.toList()
        )
    }

    protected fun hierarchyNode(id: String, memberRef: DpmElementRef, vararg children: HierarchyNode): HierarchyNode {
        return HierarchyNode(
            id = id,
            concept = Factory.instantiate(),
            abstract = false,
            comparisonOperator = "=",
            unaryOperator = "+",
            memberRef = memberRef,
            childNodes = children.toList()
        )
    }
}
