package fi.vm.yti.taxgen.yclsourceprovider

import java.io.Closeable

interface SourceBundle : Closeable {

    fun bundleInfoData(): String
    fun taxonomyUnits(): List<TaxonomyUnit>
}
