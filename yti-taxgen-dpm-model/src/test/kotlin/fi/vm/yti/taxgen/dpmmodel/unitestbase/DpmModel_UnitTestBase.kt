package fi.vm.yti.taxgen.dpmmodel.unitestbase

import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.ExplicitDimension
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.dpmmodel.Metric
import fi.vm.yti.taxgen.dpmmodel.MetricDomain
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TypedDimension
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
import fi.vm.yti.taxgen.dpmmodel.datafactory.Factory
import fi.vm.yti.taxgen.dpmmodel.dpmTestData
import fi.vm.yti.taxgen.dpmmodel.validation.Validatable
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationResultCollector
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationSubjectDescriptor
import kotlin.reflect.KClass
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach

internal open class DpmModel_UnitTestBase<T : Any>(
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

    protected fun instantiateAndValidate(
        customValidationAdapter: ((Any, ValidationResultBuilder) -> (Unit))? = null
    ) {
        require(attributeOverrides != null)

        val attributes = Factory.Builder.attributesFor(kClass, attributeOverrides)

        val collector = ValidationResultCollector()

        @Suppress("UNCHECKED_CAST")
        instance = Factory.Builder.instantiate(kClass, attributes) as T
        val theInstance = instance!!

        val subject = if (customValidationAdapter == null) {
            (theInstance as Validatable).validationSubjectDescriptor()
        } else {
            ValidationSubjectDescriptor("", emptyList())
        }

        collector.withSubject(subject) {
            if (customValidationAdapter == null) {
                (theInstance as Validatable).validate(collector)
            } else {
                customValidationAdapter(theInstance, collector)
            }
        }

        validationErrors = collector.uniqueResults().map { result -> result.toString() }
    }

    protected fun language(languageCode: String) = Language.findByIso6391Code(languageCode)!!

    protected fun metric(baseId: String): Metric {
        return Metric(
            uri = "met_${baseId}_uri",
            metricCode = "met_${baseId}_code",
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
            uri = "exp_dom_${baseId}_uri",
            domainCode = "exp_dom_${baseId}_code",
            concept = Factory.instantiate(),
            members = listOf(Factory.instantiate()),
            hierarchies = emptyList()
        )
    }

    protected fun member(baseId: String, default: Boolean): Member {
        return Member(
            uri = "member_${baseId}_uri",
            memberCode = "member_${baseId}_code",
            concept = Factory.instantiate(),
            defaultMember = default
        )
    }

    protected fun hierarchy(baseId: String, vararg roots: HierarchyNode): Hierarchy {
        return Hierarchy(
            uri = "hierarchy_${baseId}_uri",
            hierarchyCode = "hierarchy_${baseId}_code",
            concept = Factory.instantiate(),
            rootNodes = roots.toList()
        )
    }

    protected fun hierarchyNode(
        baseId: String,
        referencedElementCode: String,
        vararg children: HierarchyNode
    ): HierarchyNode {
        return HierarchyNode(
            uri = "hierarchy_node_${baseId}_uri",
            concept = Factory.instantiate(),
            abstract = false,
            comparisonOperator = "=",
            unaryOperator = "+",
            referencedElementCode = referencedElementCode,
            childNodes = children.toList()
        )
    }

    protected fun typedDomain(baseId: String): TypedDomain {
        return TypedDomain(
            uri = "typ_dom_${baseId}_uri",
            domainCode = "typ_dom_${baseId}_code",
            concept = Factory.instantiate(),
            dataType = "String"
        )
    }

    protected fun explicitDimension(baseId: String, referencedDomainCode: String): ExplicitDimension {
        return ExplicitDimension(
            uri = "exp_dim_${baseId}_uri",
            dimensionCode = "exp_dim_${baseId}_code",
            concept = Factory.instantiate(),
            referencedDomainCode = referencedDomainCode
        )
    }

    protected fun typedDimension(baseId: String, referencedDomainCode: String): TypedDimension {
        return TypedDimension(
            uri = "typ_dim_${baseId}_uri",
            dimensionCode = "typ_dim_${baseId}_code",
            concept = Factory.instantiate(),
            referencedDomainCode = referencedDomainCode
        )
    }

    protected fun metricDomain(): MetricDomain {
        return MetricDomain(
            uri = "MET",
            domainCode = "MET",
            concept = Factory.instantiate(),
            metrics = listOf(Factory.instantiate()),
            hierarchies = listOf()
        )
    }

    protected fun owner(baseId: String): Owner {
        return Owner(
            name = "name_$baseId",
            namespace = "namespace_$baseId",
            prefix = "prefix_$baseId",
            location = "location_$baseId",
            copyright = "copyright_$baseId",
            languageCodes = listOf("en")
        )
    }

    protected fun emptyDictionaryWithOwner(ownerBaseId: String): DpmDictionary {
        return DpmDictionary(
            owner = owner(ownerBaseId),
            explicitDomains = emptyList(),
            typedDomains = emptyList(),
            explicitDimensions = emptyList(),
            typedDimensions = emptyList(),
            metricDomain = null
        )
    }
}
