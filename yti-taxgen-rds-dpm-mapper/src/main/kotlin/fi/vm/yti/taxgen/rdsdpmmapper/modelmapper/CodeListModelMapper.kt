package fi.vm.yti.taxgen.rdsdpmmapper.modelmapper

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsCode
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsCodeListMeta
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsCodePage
import fi.vm.yti.taxgen.rdsprovider.CodeListSource

internal class CodeListModelMapper(
    private val codeListSource: CodeListSource,
    private val diagnostic: Diagnostic

) {
    private val codeListMeta: RdsCodeListMeta by lazy {
        JsonOps.readValue<RdsCodeListMeta>(codeListSource.codeListMetaData(), diagnostic)
    }

    fun codeListMeta(): RdsCodeListMeta {
        return codeListMeta
    }

    fun eachCode(action: (RdsCode) -> Unit) {
        codeListSource.eachCodePageData { pageData ->
            val codePage = JsonOps.readValue<RdsCodePage>(pageData, diagnostic)
            codePage.results?.forEach(action)
        }
    }

    fun eachExtensionModelMapper(action: (ExtensionModelMapper) -> Unit) {
        codeListSource.eachExtensionSource { extensionSource ->
            val extensionModelMapper = ExtensionModelMapper(
                extensionSource = extensionSource,
                diagnostic = diagnostic
            )

            diagnostic.updateCurrentContextDetails(
                label = extensionModelMapper.extensionMetaData().diagnosticLabel()
            )

            action(extensionModelMapper)
        }
    }

    fun eachSubCodeListModelMapper(action: (CodeListModelMapper) -> Unit) {
        codeListSource.eachSubCodeListSource { subCodeListSource ->
            val codeListModelMapper = CodeListModelMapper(
                codeListSource = subCodeListSource,
                diagnostic = diagnostic
            )

            diagnostic.updateCurrentContextDetails(
                label = codeListModelMapper.codeListMeta().diagnosticLabel()
            )

            action(codeListModelMapper)
        }
    }
}
