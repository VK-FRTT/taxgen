package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.throwFail
import joptsimple.BuiltinHelpFormatter
import joptsimple.OptionDescriptor
import joptsimple.OptionException
import joptsimple.OptionParser
import joptsimple.OptionSpec
import joptsimple.ValueConversionException
import joptsimple.util.PathConverter
import joptsimple.util.PathProperties
import java.io.PrintWriter
import java.nio.file.Path
import java.util.LinkedHashSet

class DefinedOptions {
    private val optionParser = OptionParser()

    private val cmdShowHelp: OptionSpec<Void>
    private val cmdShowVersion: OptionSpec<Void>
    private val cmdCreateDictionaryToNewDpmDb: OptionSpec<Path>
    private val cmdReplaceDictionaryInDpmDb: OptionSpec<Path>
    private val cmdCaptureDpmSourcesToFolder: OptionSpec<Path>
    private val cmdCaptureDpmSourcesToZip: OptionSpec<Path>

    private val sourceConfigFile: OptionSpec<Path>
    private val sourceFolder: OptionSpec<Path>
    private val sourceZipFile: OptionSpec<Path>

    private val forceOverwrite: OptionSpec<Void>

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
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

        cmdReplaceDictionaryInDpmDb = optionParser
            .accepts(
                "replace-dictionary-in-dpm-db",
                "replace dictionary in DPM database from given sources"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

        cmdCaptureDpmSourcesToFolder = optionParser
            .accepts(
                "capture-dpm-sources-to-folder",
                "capture DPM sources to folder"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

        cmdCaptureDpmSourcesToZip = optionParser
            .accepts(
                "capture-dpm-sources-to-zip",
                "capture DPM sources to zip file"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

        forceOverwrite = optionParser
            .accepts(
                "force-overwrite",
                "silently overwrites the possibly existing target file(s)"
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
            cmdCreateDictionaryToNewDpmDb = optionSet.valueOf(this.cmdCreateDictionaryToNewDpmDb),
            cmdReplaceDictionaryInDpmDb = optionSet.valueOf(this.cmdReplaceDictionaryInDpmDb),
            cmdCaptureDpmSourcesToFolder = optionSet.valueOf(this.cmdCaptureDpmSourcesToFolder),
            cmdCaptureDpmSourcesToZip = optionSet.valueOf(this.cmdCaptureDpmSourcesToZip),

            forceOverwrite = optionSet.has(this.forceOverwrite),

            sourceConfigFile = optionSet.valueOf(this.sourceConfigFile),
            sourceFolder = optionSet.valueOf(this.sourceFolder),
            sourceZipFile = optionSet.valueOf(this.sourceZipFile)
        )
    }

    private class FixedOrderHelpFormatter :
        BuiltinHelpFormatter(120, 4) {

        override fun format(options: Map<String, OptionDescriptor>): String {
            addRows(LinkedHashSet(options.values))
            return formattedHelpOutput()
        }
    }
}
