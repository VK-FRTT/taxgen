package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticTopicProvider
import java.io.Closeable

interface YclSourceRecorder : Closeable, DiagnosticTopicProvider {

    override fun topicType(): String = "Writing YCL Sources"

    fun captureSources(yclSource: YclSource)
}
