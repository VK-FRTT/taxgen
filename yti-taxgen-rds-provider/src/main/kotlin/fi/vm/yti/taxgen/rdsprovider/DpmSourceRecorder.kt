package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import java.io.Closeable

interface DpmSourceRecorder : Closeable, DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.RecordingDpmSource

    fun captureSources(dpmSource: DpmSource)
}
