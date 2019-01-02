package fi.vm.yti.taxgen.rdsdpmmapper.ext.kotlin

import fi.vm.yti.taxgen.dpmmodel.DpmElement

fun <T : DpmElement> MutableList<T>.replaceOrAddByUri(item: T) {
    val index = indexOfFirst { it.uri == item.uri }

    if (index >= 0) {
        set(index, item)
    } else {
        add(item)
    }
}
