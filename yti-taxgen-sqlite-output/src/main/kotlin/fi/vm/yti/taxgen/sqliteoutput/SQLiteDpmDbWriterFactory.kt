package fi.vm.yti.taxgen.sqliteoutput

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.sqliteoutput.contextdiagnostic.DpmDbWriterContextDecorator
import fi.vm.yti.taxgen.sqliteoutput.dictionarycreate.DictionaryCreateDbWriter
import fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.DictionaryReplaceDbWriter
import java.nio.file.Path

object SQLiteDpmDbWriterFactory {

    fun dictionaryCreateWriter(
        outputDbPath: Path,
        forceOverwrite: Boolean,
        diagnosticContext: DiagnosticContext
    ): DpmDbWriter {
        val writer = DictionaryCreateDbWriter(
            outputDbPath = outputDbPath,
            forceOverwrite = forceOverwrite,
            diagnosticContext = diagnosticContext
        )

        return DpmDbWriterContextDecorator(
            realDpmDbWriter = writer,
            diagnosticContext = diagnosticContext,
            contextTitle = "Mode DictionaryCreate"
        )
    }

    fun dictionaryReplaceWriter(
        baselineDbPath: Path,
        outputDbPath: Path,
        forceOverwrite: Boolean,
        diagnosticContext: DiagnosticContext
    ): DpmDbWriter {
        val writer = DictionaryReplaceDbWriter(
            baselineDbPath = baselineDbPath,
            outputDbPath = outputDbPath,
            forceOverwrite = forceOverwrite,
            diagnosticContext = diagnosticContext
        )

        return DpmDbWriterContextDecorator(
            realDpmDbWriter = writer,
            diagnosticContext = diagnosticContext,
            contextTitle = "Mode DictionaryReplace"
        )
    }
}
