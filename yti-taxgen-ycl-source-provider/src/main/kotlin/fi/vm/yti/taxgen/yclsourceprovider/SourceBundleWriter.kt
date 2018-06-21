package fi.vm.yti.taxgen.yclsourceprovider

import java.io.Closeable

interface SourceBundleWriter : Closeable {

    fun write()
}
