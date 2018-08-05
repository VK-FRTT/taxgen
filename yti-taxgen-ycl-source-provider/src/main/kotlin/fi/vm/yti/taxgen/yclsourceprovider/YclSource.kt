package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticTopicProvider
import java.io.Closeable

abstract class YclSource : Closeable, DiagnosticTopicProvider {

    override fun topicType(): String = "YCL Source"
    override fun topicName(): String = ""
    override fun topicIdentifier(): String = ""

    abstract fun sourceInfoData(): String
    abstract fun dpmDictionarySources(): List<DpmDictionarySource>
}
