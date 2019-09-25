package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.sqliteprovider.contextdiagnostic.DpmDbWriterContextDecorator
import fi.vm.yti.taxgen.sqliteprovider.dictionarycreate.DictionaryCreateDbWriter
import fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.DictionaryReplaceDbWriter
import java.nio.file.Path

object DpmDbWriterFactory {

    fun dictionaryCreateWriter(
        outputDbPath: Path,
        forceOverwrite: Boolean,
        diagnosticContext: DiagnosticContext
    ): DpmDbWriter {
        val writer = DictionaryCreateDbWriter(
            outputDbPath = outputDbPath,
            forceOverwrite = forceOverwrite,
            diagnostic = diagnosticContext
        )

        return DpmDbWriterContextDecorator(
            realDpmDbWriter = writer,
            diagnosticContext = diagnosticContext
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
            diagnosticContext = diagnosticContext
        )
    }
}
