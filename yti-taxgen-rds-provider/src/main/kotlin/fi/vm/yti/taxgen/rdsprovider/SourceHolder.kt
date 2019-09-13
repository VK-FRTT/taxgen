package fi.vm.yti.taxgen.rdsprovider

import java.io.Closeable

interface SourceHolder : Closeable {
    fun withDpmSource(action: (DpmSource) -> Unit)
}
