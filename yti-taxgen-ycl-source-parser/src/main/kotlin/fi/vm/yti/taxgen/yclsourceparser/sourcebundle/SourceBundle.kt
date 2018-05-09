package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

import java.io.Closeable

interface SourceBundle : Closeable {

    fun bundleInfo(): BundleInfo
    fun taxonomyUnits(): Iterator<TaxonomyUnit>
}
