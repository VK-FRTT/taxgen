package fi.vm.yti.taxgen.cli

class HaltException(val exitCode: Int) : RuntimeException()

fun halt(exitCode: Int): Nothing {
    throw HaltException(exitCode)
}
