package fi.vm.yti.taxgen.yclsourceprovider

import java.io.Closeable

interface YclSource : Closeable {

    fun sourceInfoData(): String
    fun dpmDictionarySources(): List<DpmDictionarySource>
}
