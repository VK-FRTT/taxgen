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
    private val cmdWriteDpmDb: OptionSpec<Path>
    private val cmdBundleYclSourcesToFolder: OptionSpec<Path>
    private val cmdBundleYclSourcesToZip: OptionSpec<Path>

    private val sourceConfig: OptionSpec<Path>
    private val sourceBundleFolder: OptionSpec<Path>
    private val sourceBundleZip: OptionSpec<Path>

    private val forceOverwrite: OptionSpec<Void>

    init {
        cmdShowHelp = optionParser
            .accepts(
                "help",
                "show this help message"
            ).forHelp()

        cmdWriteDpmDb = optionParser
            .accepts(
                "write-dpm-db",
                "outputs taxonomy information as DPM DB"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

        cmdBundleYclSourcesToFolder = optionParser
            .accepts(
                "bundle-ycl-sources-to-folder",
                "bundle YTI Codelist taxonomy sources to folder"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

        cmdBundleYclSourcesToZip = optionParser
            .accepts(
                "bundle-ycl-sources-to-zip",
                "bundle YTI Codelist taxonomy sources to zip file"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

        sourceConfig = optionParser
            .accepts(
                "source-config",
                "configuration file describing taxonomy sources"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter(PathProperties.FILE_EXISTING, PathProperties.READABLE))

        sourceBundleFolder = optionParser
            .accepts(
                "source-bundle-folder",
                "load bundled taxonomy sources from folder"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter(PathProperties.DIRECTORY_EXISTING))

        sourceBundleZip = optionParser
            .accepts(
                "source-bundle-zip",
                "load bundled taxonomy sources from zip file"
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
            cmdWriteDpmDb = optionSet.valueOf(this.cmdWriteDpmDb),
            cmdBundleYclSourcesToFolder = optionSet.valueOf(this.cmdBundleYclSourcesToFolder),
            cmdBundleYclSourcesToZip = optionSet.valueOf(this.cmdBundleYclSourcesToZip),

            forceOverwrite = optionSet.has(this.forceOverwrite),

            sourceConfig = optionSet.valueOf(this.sourceConfig),
            sourceBundleFolder = optionSet.valueOf(this.sourceBundleFolder),
            sourceBundleZip = optionSet.valueOf(this.sourceBundleZip)
        )
    }
}
