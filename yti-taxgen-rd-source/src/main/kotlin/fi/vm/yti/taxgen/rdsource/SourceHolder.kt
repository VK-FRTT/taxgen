package fi.vm.yti.taxgen.rdsource

import java.io.Closeable

interface SourceHolder : Closeable {
    fun withDpmSource(action: (DpmSource) -> Unit)
}
