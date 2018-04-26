package fi.vm.yti.taxgen.commons.platformextension

inline fun <reified T : Any> Throwable.rootExceptionOfClass(): T? {
    var exception: Throwable = this

    while (exception.cause != null) {
        exception = exception.cause as Throwable
    }

    if (exception is T) return exception

    return null
}

fun Throwable.chainToString(): String {
    val messages = mutableListOf<String>()
    var exception: Throwable? = this

    while (exception != null) {
        messages.add("${exception::class.qualifiedName}: ${exception.message}")
        exception = exception.cause
    }

    return messages.joinToString(separator = ", => caused by ")
}
