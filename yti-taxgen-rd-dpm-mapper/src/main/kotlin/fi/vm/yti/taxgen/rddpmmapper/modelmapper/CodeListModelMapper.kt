package fi.vm.yti.taxgen.rddpmmapper.modelmapper

import fi.vm.yti.taxgen.commons.ops.JsonOps
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.rddpmmapper.rdsmodel.RdsCode
import fi.vm.yti.taxgen.rddpmmapper.rdsmodel.RdsCodeListMeta
import fi.vm.yti.taxgen.rddpmmapper.rdsmodel.RdsCodePage
import fi.vm.yti.taxgen.rdsource.CodeListSource

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
                contextTitle = extensionModelMapper.extensionMetaData().diagnosticContextTitleFromLabel(
                    diagnostic.diagnosticSourceLanguages()
                )
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
                contextTitle = codeListModelMapper.codeListMeta().diagnosticContextTitleFromLabel(
                    diagnostic.diagnosticSourceLanguages()
                )
            )

            action(codeListModelMapper)
        }
    }
}
