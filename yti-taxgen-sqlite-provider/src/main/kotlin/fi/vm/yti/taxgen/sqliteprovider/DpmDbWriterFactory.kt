package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.sqliteprovider.dictionaryproducer.DictionaryCreateDbWriter
import fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.DictionaryReplaceDbWriter
import java.nio.file.Path

object DpmDbWriterFactory {

    fun dictionaryCreateWriter(
        outputDbPath: Path,
        forceOverwrite: Boolean,
        diagnosticContext: DiagnosticContext
    ): DpmDbWriter {
        return DictionaryCreateDbWriter(
            outputDbPath = outputDbPath,
            forceOverwrite = forceOverwrite,
            diagnosticContext = diagnosticContext
        )
    }

    fun dictionaryReplaceWriter(
        baselineDbPath: Path,
        outputDbPath: Path,
        forceOverwrite: Boolean,
        diagnosticContext: DiagnosticContext
    ): DpmDbWriter {
        return DictionaryReplaceDbWriter(
            baselineDbPath = baselineDbPath,
            outputDbPath = outputDbPath,
            forceOverwrite = forceOverwrite,
            diagnosticContext = diagnosticContext
        )
    }
}
