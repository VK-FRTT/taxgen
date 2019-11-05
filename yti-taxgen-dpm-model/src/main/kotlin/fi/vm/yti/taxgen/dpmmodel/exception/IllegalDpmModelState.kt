package fi.vm.yti.taxgen.dpmmodel.exception

internal fun throwIllegalDpmModelState(message: String = ""): Nothing {
    throw IllegalStateException(message)
}
