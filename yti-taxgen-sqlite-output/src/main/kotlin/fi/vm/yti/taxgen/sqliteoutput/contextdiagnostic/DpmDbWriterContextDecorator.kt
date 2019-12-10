package fi.vm.yti.taxgen.sqliteoutput.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticContexts
import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContextDetailsData
import fi.vm.yti.taxgen.sqliteoutput.DpmDbWriter
import java.nio.file.Path

class DpmDbWriterContextDecorator(
    private val realDpmDbWriter: DpmDbWriter,

    private val diagnosticContext: DiagnosticContext
) : DpmDbWriter {

    override fun writeModel(
        dpmModel: DpmModel,
        processingOptions: ProcessingOptions
    ) {
        diagnosticContext.withContext(
            contextType = DiagnosticContexts.SQLiteDbWriter.toType(),
            contextDetails = DiagnosticContextDetailsData.withContextIdentifier(realDpmDbWriter.outputPath().toString())
        ) {
            realDpmDbWriter.writeModel(dpmModel, processingOptions)
        }
    }

    override fun outputPath(): Path = realDpmDbWriter.outputPath()
}
