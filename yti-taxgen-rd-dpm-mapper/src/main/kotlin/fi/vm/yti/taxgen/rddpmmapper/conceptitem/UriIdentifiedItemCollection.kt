package fi.vm.yti.taxgen.rddpmmapper.conceptitem

class UriIdentifiedItemCollection<T : UriIdentifiedItem> {

    private val items: MutableList<T> by lazy {
        mutableListOf<T>()
    }

    fun addItem(item: T) {
        items.add(item)
    }

    fun replaceOrAddItemByUri(item: T) {
        val index = items.indexOfFirst { it.uri == item.uri }

        if (index >= 0) {
            items[index] = item
        } else {
            items.add(item)
        }
    }

    fun findByUri(uri: String): T? {
        val index = items.indexOfFirst { it.uri == uri }

        return if (index >= 0) {
            items[index]
        } else {
            null
        }
    }

    fun find(predicate: (T) -> Boolean): T? {
        return items.find(predicate)
    }

    fun itemsList(): List<T> = items
}
