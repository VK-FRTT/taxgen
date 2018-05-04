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
    private val cmdGenerateTaxonomy: OptionSpec<Void>
    private val cmdBundleSources: OptionSpec<Void>

    private val sourceYclConfig: OptionSpec<Path>
    private val sourceBundleFolder: OptionSpec<Path>
    private val sourceBundleZip: OptionSpec<Path>

    private val targetFolder: OptionSpec<Path>
    private val targetZip: OptionSpec<Path>

    init {
        cmdShowHelp = optionParser
            .accepts(
                "cmdShowHelp",
                "show this cmdShowHelp message"
            ).forHelp()

        cmdGenerateTaxonomy = optionParser
            .accepts(
                "generate-taxonomy",
                "generate taxonomy"
            )

        cmdBundleSources = optionParser
            .accepts(
                "bundle-sources",
                "bundle taxonomy sources"
            )

        sourceYclConfig = optionParser
            .accepts(
                "source-ycl-config",
                "load sources from YTI Codelist service, given configuration file contains linking details"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter(PathProperties.FILE_EXISTING, PathProperties.READABLE))

        sourceBundleFolder = optionParser
            .accepts(
                "source-bundle-folder",
                "load bundled taxonomy sources from a folder"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter(PathProperties.DIRECTORY_EXISTING))

        sourceBundleZip = optionParser
            .accepts(
                "source-bundle-zip",
                "load bundled taxonomy sources from a zip file"
            )
            .withRequiredArg()
            .withValuesConvertedBy(PathConverter(PathProperties.FILE_EXISTING, PathProperties.READABLE))

        targetFolder = optionParser
            .accepts(
                "target-folder",
                "store results to a folder"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

        targetZip = optionParser
            .accepts(
                "target-zip",
                "store results to a zip file"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter(PathProperties.FILE_EXISTING, PathProperties.WRITABLE))
    }

    fun detectOptionsFromArgs(args: Array<String>, out: PrintWriter, err: PrintWriter): DetectedOptions {
        return try {
            val detectedOptions = doDetectOptions(args)
            if (detectedOptions.cmdShowHelp) {
                optionParser.printHelpOn(out)
                halt(TAXGEN_CLI_SUCCESS)
            }

            detectedOptions
        } catch (exception: OptionException) {
            val cause = exception.cause

            if (cause is ValueConversionException) {
                printError(
                    err,
                    "Option ${exception.options().first()}: ${cause.message}"
                )
            } else {
                printErrorWithUsageHint(
                    err,
                    exception.message
                )
            }

            halt(TAXGEN_CLI_FAIL)
        } catch (exception: NoOptionsDetectedException) {
            printErrorWithUsageHint(
                err,
                "No options given"
            )

            halt(TAXGEN_CLI_FAIL)
        }
    }

    private fun doDetectOptions(args: Array<String>): DetectedOptions {
        val optionSet = optionParser.parse(*args)

        if (!optionSet.hasOptions()) throw NoOptionsDetectedException()

        return DetectedOptions(
            cmdShowHelp = optionSet.has(this.cmdShowHelp),
            cmdGenerateTaxonomy = optionSet.has(this.cmdGenerateTaxonomy),
            cmdBundleSources = optionSet.has(this.cmdBundleSources),

            yclSourceConfig = optionSet.valueOf(this.sourceYclConfig),
            sourceBundleFolder = optionSet.valueOf(this.sourceBundleFolder),
            sourceBundleZip = optionSet.valueOf(this.sourceBundleZip),

            targetFolder = optionSet.valueOf(this.targetFolder),
            targetZip = optionSet.valueOf(this.targetZip)
        )
    }

    private fun printError(err: PrintWriter, message: String?) {
        err.println("yti-taxgen: $message")
    }

    private fun printErrorWithUsageHint(err: PrintWriter, message: String?) {
        printError(err, "$message  (-h will show valid options)")
    }
}
