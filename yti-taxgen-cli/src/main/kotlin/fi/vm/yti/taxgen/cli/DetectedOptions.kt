package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticOutputVerbosity
import fi.vm.yti.taxgen.commons.throwFail
import java.nio.file.Path

data class DetectedOptions(
    val cmdShowHelp: Boolean,
    val cmdShowVersion: Boolean,

    val cmdCreateDictionaryToNewDpmDb: Boolean,
    val cmdReplaceDictionaryInDpmDb: Boolean,
    val cmdCaptureDpmSourcesToFolder: Boolean,
    val cmdCaptureDpmSourcesToZip: Boolean,

    val sourceConfigFile: Path?,
    val sourceFolder: Path?,
    val sourceZipFile: Path?,

    val baselineDb: Path?,

    val output: Path?,
    val forceOverwrite: Boolean,
    val keepPartialOutput: Boolean,
    val verbosity: DiagnosticOutputVerbosity
) {

    fun ensureSingleCommandGiven() {
        val commandCount = listOf(
            cmdCreateDictionaryToNewDpmDb,
            cmdReplaceDictionaryInDpmDb,
            cmdCaptureDpmSourcesToFolder,
            cmdCaptureDpmSourcesToZip
        ).count { it }

        if (commandCount != 1) {
            throwFail("Single command must be given")
        }
    }

    fun ensureSingleSourceGiven() {
        val sourceOptionCount = listOf<Any?>(
            sourceConfigFile,
            sourceFolder,
            sourceZipFile
        ).count { it != null }

        if (sourceOptionCount != 1) {
            throwFail("One source option with proper argument must be given")
        }
    }

    fun ensureOutputGiven() {
        if (output == null) {
            throwFail("Option output must be given")
        }
    }

    fun ensureBaselineDpmDbGiven() {
        if (baselineDb == null) {
            throwFail("Option baseline-db must be given")
        }
    }
}
