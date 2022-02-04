package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticOutputVerbosity
import fi.vm.yti.taxgen.commons.throwFail
import java.io.PrintWriter
import java.nio.file.Path
import java.util.LinkedHashSet
import joptsimple.BuiltinHelpFormatter
import joptsimple.OptionDescriptor
import joptsimple.OptionException
import joptsimple.OptionParser
import joptsimple.OptionSpec
import joptsimple.ValueConversionException
import joptsimple.util.EnumConverter
import joptsimple.util.PathConverter
import joptsimple.util.PathProperties

class DefinedOptions {
    private val optionParser = OptionParser()

    private val cmdShowHelp: OptionSpec<Void>
    private val cmdShowVersion: OptionSpec<Void>
    private val cmdCreateDictionaryToNewDpmDb: OptionSpec<Void>
    private val cmdReplaceDictionaryInDpmDb: OptionSpec<Void>
    private val cmdCaptureDpmSourcesToFolder: OptionSpec<Void>
    private val cmdCaptureDpmSourcesToZip: OptionSpec<Void>

    private val sourceConfigFile: OptionSpec<Path>
    private val sourceFolder: OptionSpec<Path>
    private val sourceZipFile: OptionSpec<Path>

    private val baselineDb: OptionSpec<Path>

    private val output: OptionSpec<Path>
    private val forceOverwrite: OptionSpec<Void>
    private val keepPartialOutput: OptionSpec<Void>
    private val verbosity: OptionSpec<DiagnosticOutputVerbosity>

    init {
        cmdShowHelp = optionParser
            .accepts(
                "help",
                "show this help message"
            ).forHelp()

        cmdShowVersion = optionParser
            .accepts(
                "version",
                "show version information"
            )

        cmdCreateDictionaryToNewDpmDb = optionParser
            .accepts(
                "create-dictionary-to-new-dpm-db",
                "create dictionary to new DPM database from given sources"
            )

        cmdReplaceDictionaryInDpmDb = optionParser
            .accepts(
                "replace-dictionary-in-dpm-db",
                "replace dictionary in DPM database from given sources"
            )

        cmdCaptureDpmSourcesToFolder = optionParser
            .accepts(
                "capture-dpm-sources-to-folder",
                "capture DPM sources to folder"
            )

        cmdCaptureDpmSourcesToZip = optionParser
            .accepts(
                "capture-dpm-sources-to-zip",
                "capture DPM sources to zip file"
            )

        sourceConfigFile = optionParser
            .accepts(
                "source-config",
                "configuration file linking to sources on Reference Data -service"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter(PathProperties.FILE_EXISTING, PathProperties.READABLE))

        sourceFolder = optionParser
            .accepts(
                "source-folder",
                "folder where to load sources"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter(PathProperties.DIRECTORY_EXISTING))

        sourceZipFile = optionParser
            .accepts(
                "source-zip",
                "zip file where to load sources"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter(PathProperties.FILE_EXISTING, PathProperties.READABLE))

        baselineDb = optionParser
            .accepts(
                "baseline-db",
                "database to use as baseline in dictionary replace"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter(PathProperties.FILE_EXISTING, PathProperties.READABLE))

        output = optionParser
            .accepts(
                "output",
                "file or folder where to write command output"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter())

        forceOverwrite = optionParser
            .accepts(
                "force-overwrite",
                "silently overwrites the possibly existing target file(s)"
            )

        keepPartialOutput = optionParser
            .accepts(
                "keep-partial-output",
                "in error situations, keep the partially produced output files"
            )

        verbosity = optionParser
            .accepts(
                "verbosity",
                "diagnostic verbosity, modes: ${DiagnosticOutputVerbosity.NORMAL}, ${DiagnosticOutputVerbosity.DEBUG}"
            )
            .withOptionalArg()
            .withValuesConvertedBy(VerbosityConverter())
            .defaultsTo(DiagnosticOutputVerbosity.NORMAL)
    }

    fun detectOptionsFromArgs(args: Array<String>): DetectedOptions {
        return try {
            doDetectOptions(args)
        } catch (exception: OptionException) {
            val cause = exception.cause

            if (cause is ValueConversionException) {
                throwFail("Option ${exception.options().first()}: ${cause.message}")
            } else {
                throwFail("${exception.message}")
            }
        }
    }

    fun printHelp(outWriter: PrintWriter) {
        optionParser.formatHelpWith(FixedOrderHelpFormatter())
        optionParser.printHelpOn(outWriter)
    }

    private fun doDetectOptions(args: Array<String>): DetectedOptions {
        val optionSet = optionParser.parse(*args)

        if (!optionSet.hasOptions()) {
            throwFail("No options given (-h will show valid options)")
        }

        return DetectedOptions(
            cmdShowHelp = optionSet.has(this.cmdShowHelp),
            cmdShowVersion = optionSet.has(this.cmdShowVersion),
            cmdCreateDictionaryToNewDpmDb = optionSet.has(this.cmdCreateDictionaryToNewDpmDb),
            cmdReplaceDictionaryInDpmDb = optionSet.has(this.cmdReplaceDictionaryInDpmDb),
            cmdCaptureDpmSourcesToFolder = optionSet.has(this.cmdCaptureDpmSourcesToFolder),
            cmdCaptureDpmSourcesToZip = optionSet.has(this.cmdCaptureDpmSourcesToZip),

            sourceConfigFile = optionSet.valueOf(this.sourceConfigFile),
            sourceFolder = optionSet.valueOf(this.sourceFolder),
            sourceZipFile = optionSet.valueOf(this.sourceZipFile),

            baselineDb = optionSet.valueOf(this.baselineDb),

            output = optionSet.valueOf(this.output),
            forceOverwrite = optionSet.has(this.forceOverwrite),
            keepPartialOutput = optionSet.has(this.keepPartialOutput),
            verbosity = optionSet.valueOf(verbosity)
            )
    }

    private class VerbosityConverter : EnumConverter<DiagnosticOutputVerbosity>(DiagnosticOutputVerbosity::class.java)

    private class FixedOrderHelpFormatter :
        BuiltinHelpFormatter(120, 4) {

        override fun format(options: Map<String, OptionDescriptor>): String {
            addRows(LinkedHashSet(options.values))
            return formattedHelpOutput()
        }
    }
}
