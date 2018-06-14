package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

import java.io.Closeable

interface SourceBundle : Closeable {

    fun bundleInfoData(): String
    fun taxonomyUnits(): List<TaxonomyUnit>
}
