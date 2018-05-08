package fi.vm.yti.taxgen.cli

const val TAXGEN_CLI_SUCCESS = 0
const val TAXGEN_CLI_FAIL = 1

class HaltException(
    val exitCode: Int,
    val errorMessage: String?
) : RuntimeException()

fun halt(exitCode: Int): Nothing {
    throw HaltException(exitCode, null)
}

fun haltWithError(errorMessage: String?): Nothing {
    throw HaltException(TAXGEN_CLI_FAIL, errorMessage)
}
