package fi.vm.yti.taxgen.rddpmmapper.ext.kotlin

import fi.vm.yti.taxgen.rddpmmapper.conceptitem.UriIdentifiedItem

fun <T : UriIdentifiedItem> MutableList<T>.replaceOrAddItemByUri(item: T) {
    val index = indexOfFirst { it.uri == item.uri }

    if (index >= 0) {
        set(index, item)
    } else {
        add(item)
    }
}
