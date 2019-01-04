package fi.vm.yti.taxgen.rdsprovider

import java.io.Closeable

interface SourceProvider : Closeable {
    fun withDpmSource(action: (DpmSource) -> Unit)
}
