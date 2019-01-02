package fi.vm.yti.taxgen.dpmmodel.unitestbase

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationCollector
import fi.vm.yti.taxgen.dpmmodel.DpmElement
import fi.vm.yti.taxgen.dpmmodel.DpmElementRef
import fi.vm.yti.taxgen.dpmmodel.ExplicitDimension
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.dpmmodel.Metric
import fi.vm.yti.taxgen.dpmmodel.TypedDimension
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
import fi.vm.yti.taxgen.dpmmodel.datafactory.Factory
import fi.vm.yti.taxgen.dpmmodel.dpmElementRef
import fi.vm.yti.taxgen.dpmmodel.dpmTestData
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

        val collector = ValidationCollector()

        @Suppress("UNCHECKED_CAST")
        instance = Factory.Builder.instantiate(kClass, attributes) as T

        instance!!.validate(collector)
        validationErrors = collector.compileResultsToSimpleStrings()
    }

    protected fun language(languageCode: String) = Language.findByIso6391Code(languageCode)!!

    protected fun metric(baseId: String, memberCodeNumber: String): Metric {
        return Metric(
            id = "${baseId}_id",
            uri = "${baseId}_uri",
            memberCodeNumber = memberCodeNumber,
            concept = Factory.instantiate(),
            dataType = "String",
            flowType = "Instant",
            balanceType = "Credit",
            referencedDomainCode = null,
            referencedHierarchyCode = null
        )
    }

    protected fun explicitDomain(baseId: String): ExplicitDomain {
        return ExplicitDomain(
            id = "${baseId}_id",
            uri = "${baseId}_uri",
            domainCode = "${baseId}_code",
            concept = Factory.instantiate(),
            members = listOf(Factory.instantiate()),
            hierarchies = listOf()
        )
    }

    protected fun member(baseId: String, default: Boolean): Member {
        return Member(
            id = "${baseId}_id",
            uri = "${baseId}_uri",
            memberCode = "${baseId}_code",
            concept = Factory.instantiate(),
            defaultMember = default
        )
    }

    protected fun hierarchy(baseId: String, vararg roots: HierarchyNode): Hierarchy {
        return Hierarchy(
            id = "${baseId}_id",
            uri = "${baseId}_uri",
            hierarchyCode = "${baseId}_code",
            concept = Factory.instantiate(),
            rootNodes = roots.toList()
        )
    }

    protected fun hierarchyNode(
        baseId: String,
        memberRef: DpmElementRef,
        vararg children: HierarchyNode
    ): HierarchyNode {
        return HierarchyNode(
            id = "${baseId}_id",
            uri = "${baseId}_uri",
            concept = Factory.instantiate(),
            abstract = false,
            comparisonOperator = "=",
            unaryOperator = "+",
            memberRef = memberRef,
            childNodes = children.toList()
        )
    }

    protected fun typedDomain(baseId: String): TypedDomain {
        return TypedDomain(
            id = "${baseId}_id",
            uri = "${baseId}_uri",
            domainCode = "${baseId}_code",
            concept = Factory.instantiate(),
            dataType = "String"
        )
    }

    protected fun explicitDimension(baseId: String, domainRef: DpmElementRef): ExplicitDimension {
        return ExplicitDimension(
            id = "${baseId}_id",
            uri = "${baseId}_uri",
            dimensionCode = "${baseId}_code",
            concept = Factory.instantiate(),
            domainRef = domainRef
        )
    }

    protected fun typedDimension(baseId: String, domainRef: DpmElementRef): TypedDimension {
        return TypedDimension(
            id = "${baseId}_id",
            uri = "${baseId}_uri",
            dimensionCode = "${baseId}_code",
            concept = Factory.instantiate(),
            domainRef = domainRef
        )
    }

    inline fun <reified T : DpmElement> refTo(baseId: String) =
        dpmElementRef<T>("${baseId}_id", "${baseId}_uri", "${baseId}_diagnostic_label")
}
