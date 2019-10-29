package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContextDetails
import java.io.Closeable

interface DpmSourceRecorder : Closeable, DiagnosticContextDetails {
    fun captureSources(dpmSource: DpmSource)
}
