package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import java.io.Closeable

interface YclSourceRecorder : Closeable, DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.ActivityRecordYclSources

    fun captureSources(yclSource: YclSource)
}
