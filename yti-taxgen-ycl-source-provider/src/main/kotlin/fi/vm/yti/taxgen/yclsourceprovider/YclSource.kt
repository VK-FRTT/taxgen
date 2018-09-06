package fi.vm.yti.taxgen.yclsourceprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import java.io.Closeable

abstract class YclSource : Closeable, DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.YclSource

    abstract fun sourceInfoData(): String
    abstract fun dpmDictionarySources(): List<DpmDictionarySource>
}
