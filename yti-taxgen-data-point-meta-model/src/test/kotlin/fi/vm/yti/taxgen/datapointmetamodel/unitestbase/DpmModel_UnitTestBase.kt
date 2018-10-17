package fi.vm.yti.taxgen.datapointmetamodel.unitestbase

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationCollector
import fi.vm.yti.taxgen.datapointmetamodel.DpmElement
import fi.vm.yti.taxgen.datapointmetamodel.DpmElementRef
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.datapointmetamodel.Hierarchy
import fi.vm.yti.taxgen.datapointmetamodel.HierarchyNode
import fi.vm.yti.taxgen.datapointmetamodel.Language
import fi.vm.yti.taxgen.datapointmetamodel.Member
import fi.vm.yti.taxgen.datapointmetamodel.datafactory.Factory
import fi.vm.yti.taxgen.datapointmetamodel.dpmElementRef
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

        val collector = ValidationCollector()

        @Suppress("UNCHECKED_CAST")
        instance = Factory.Builder.instantiate(kClass, attributes) as T

        instance!!.validate(collector)
        validationErrors = collector.compileResultsToSimpleStrings()
    }

    protected fun language(languageCode: String) = Language.findByIso6391Code(languageCode)!!

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

    inline fun <reified T : DpmElement> refTo(baseId: String) =
        dpmElementRef<T>("${baseId}_id", "${baseId}_uri", "${baseId}_diagnostic_label")
}