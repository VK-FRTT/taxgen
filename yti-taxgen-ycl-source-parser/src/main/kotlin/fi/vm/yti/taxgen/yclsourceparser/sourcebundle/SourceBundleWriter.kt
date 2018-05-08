package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

import java.io.Closeable

interface SourceBundleWriter : Closeable {

    fun write()
}
