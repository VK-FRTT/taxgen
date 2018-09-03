package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import java.io.Closeable

interface YclSourceRecorder : Closeable, DiagnosticContextProvider {

    override fun contextType(): String = "Writing YCL Sources"

    fun captureSources(yclSource: YclSource)
}
