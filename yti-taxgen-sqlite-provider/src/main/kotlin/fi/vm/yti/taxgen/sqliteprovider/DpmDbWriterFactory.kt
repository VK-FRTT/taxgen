package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.sqliteprovider.dictionaryproducer.DictionaryCreateDbWriter
import fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.DictionaryReplaceDbWriter
import java.nio.file.Path

object DpmDbWriterFactory {

    fun dictionaryCreateWriter(
        targetDbPath: Path,
        forceOverwrite: Boolean,
        diagnosticContext: DiagnosticContext
    ): DpmDbWriter {
        return DictionaryCreateDbWriter(
            targetDbPath = targetDbPath,
            forceOverwrite = forceOverwrite,
            diagnosticContext = diagnosticContext
        )
    }

    fun dictionaryReplaceWriter(
        targetDbPath: Path,
        diagnosticContext: DiagnosticContext
    ): DpmDbWriter {
        return DictionaryReplaceDbWriter(
            targetDbPath = targetDbPath,
            diagnosticContext = diagnosticContext
        )
    }
}
