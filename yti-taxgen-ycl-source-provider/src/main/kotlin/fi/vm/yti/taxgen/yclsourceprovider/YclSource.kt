package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import java.io.Closeable

abstract class YclSource : Closeable, DiagnosticContextProvider {

    override fun contextType(): String = "Reading YCL Sources"

    abstract fun sourceInfoData(): String
    abstract fun dpmDictionarySources(): List<DpmDictionarySource>
}
