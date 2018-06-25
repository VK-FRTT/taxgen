package fi.vm.yti.taxgen.yclsourceprovider

import java.io.Closeable

interface YclSourceRecorder : Closeable {

    fun capture()
}
