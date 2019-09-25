package fi.vm.yti.taxgen.sqliteprovider.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.dpmmodel.ProcessingOptions
import fi.vm.yti.taxgen.sqliteprovider.DpmDbWriter
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
            contextType = DiagnosticContextType.SQLiteDbWriter,
            contextIdentifier = realDpmDbWriter.outputPath().toString()
        ) {
            realDpmDbWriter.writeModel(dpmModel, processingOptions)
        }
    }

    override fun outputPath(): Path = realDpmDbWriter.outputPath()
}
