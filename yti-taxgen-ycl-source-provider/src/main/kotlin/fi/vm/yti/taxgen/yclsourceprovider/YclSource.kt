package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticTopicProvider
import java.io.Closeable

abstract class YclSource : Closeable, DiagnosticTopicProvider {

    override fun topicType(): String = "Reading YCL Sources"

    abstract fun sourceInfoData(): String
    abstract fun dpmDictionarySources(): List<DpmDictionarySource>
}
