package fi.vm.yti.taxgen.commons

class HaltException : RuntimeException()
class FailException(val errorMessage: String) : RuntimeException()

fun throwHalt(): Nothing {
    throw HaltException()
}

fun throwFail(errorMessage: String): Nothing {
    throw FailException(errorMessage)
}

fun thisShouldNeverHappen(message: String): Nothing {
    throw IllegalStateException(message)
}
