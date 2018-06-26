package fi.vm.yti.taxgen.cli

import joptsimple.OptionException
import joptsimple.OptionParser
import joptsimple.OptionSpec
import joptsimple.ValueConversionException
import joptsimple.util.PathConverter
import joptsimple.util.PathProperties
import java.io.PrintWriter
import java.nio.file.Path

class DefinedOptions {
    private val optionParser = OptionParser()

    private val cmdShowHelp: OptionSpec<Void>
    private val cmdProduceDpmDb: OptionSpec<Path>
    private val cmdCaptureYclSourcesToFolder: OptionSpec<Path>
    private val cmdCaptureYclSourcesToZip: OptionSpec<Path>

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

        cmdProduceDpmDb = optionParser
            .accepts(
                "produce-dpm-db",
                "produce DPM DB from given sources"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

        cmdCaptureYclSourcesToFolder = optionParser
            .accepts(
                "capture-ycl-sources-to-folder",
                "capture YTI Codelist based sources to folder"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

        cmdCaptureYclSourcesToZip = optionParser
            .accepts(
                "capture-ycl-sources-to-zip",
                "capture YTI Codelist based sources to zip file"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

        sourceConfigFile = optionParser
            .accepts(
                "source-config",
                "configuration file linking to sources on YTI Reference Data -service"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter(PathProperties.FILE_EXISTING, PathProperties.READABLE))

        sourceFolder = optionParser
            .accepts(
                "source-folder",
                "load source data from folder"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter(PathProperties.DIRECTORY_EXISTING))

        sourceZipFile = optionParser
            .accepts(
                "source-zip",
                "load source data from zip file"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter(PathProperties.FILE_EXISTING, PathProperties.READABLE))

        forceOverwrite = optionParser
            .accepts(
                "force-overwrite",
                "silently overwrites the possibly existing target file(s)"
            )
    }

    fun detectOptionsFromArgs(args: Array<String>): DetectedOptions {
        return try {
            doDetectOptions(args)
        } catch (exception: OptionException) {
            val cause = exception.cause

            if (cause is ValueConversionException) {
                haltWithError("Option ${exception.options().first()}: ${cause.message}")
            } else {
                haltWithError(exception.message)
            }
        }
    }

    fun printHelp(outWriter: PrintWriter) {
        optionParser.printHelpOn(outWriter)
    }

    private fun doDetectOptions(args: Array<String>): DetectedOptions {
        val optionSet = optionParser.parse(*args)

        if (!optionSet.hasOptions()) {
            haltWithError("No options given (-h will show valid options)")
        }

        return DetectedOptions(
            cmdShowHelp = optionSet.has(this.cmdShowHelp),
            cmdProduceDpmDb = optionSet.valueOf(this.cmdProduceDpmDb),
            cmdCaptureYclSourcesToFolder = optionSet.valueOf(this.cmdCaptureYclSourcesToFolder),
            cmdCaptureYclSourcesToZip = optionSet.valueOf(this.cmdCaptureYclSourcesToZip),

            forceOverwrite = optionSet.has(this.forceOverwrite),

            sourceConfigFile = optionSet.valueOf(this.sourceConfigFile),
            sourceFolder = optionSet.valueOf(this.sourceFolder),
            sourceZipFile = optionSet.valueOf(this.sourceZipFile)
        )
    }
}
