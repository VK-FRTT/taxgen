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
    private val cmdGenerateYclTaxonomy: OptionSpec<Void>
    private val cmdBundleYclSource: OptionSpec<Void>

    private val sourceConfig: OptionSpec<Path>
    private val sourceBundleFolder: OptionSpec<Path>
    private val sourceBundleZip: OptionSpec<Path>

    private val targetFolder: OptionSpec<Path>
    private val targetZip: OptionSpec<Path>

    init {
        cmdShowHelp = optionParser
            .accepts(
                "help",
                "show this help message"
            ).forHelp()

        cmdGenerateYclTaxonomy = optionParser
            .accepts(
                "generate-ycl-taxonomy",
                "generate taxonomy from YTI Codelist sources"
            )

        cmdBundleYclSource = optionParser
            .accepts(
                "bundle-ycl-source",
                "bundle YTI Codelist taxonomy sources"
            )

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

        targetFolder = optionParser
            .accepts(
                "target-folder",
                "store operation results to folder"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter())

        targetZip = optionParser
            .accepts(
                "target-zip",
                "store operation results as zip file"
            )
            .withOptionalArg()
            .withValuesConvertedBy(PathConverter(PathProperties.FILE_EXISTING, PathProperties.WRITABLE))
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
            cmdGenerateYclTaxonomy = optionSet.has(this.cmdGenerateYclTaxonomy),
            cmdBundleYclSource = optionSet.has(this.cmdBundleYclSource),

            sourceConfig = optionSet.valueOf(this.sourceConfig),
            sourceBundleFolder = optionSet.valueOf(this.sourceBundleFolder),
            sourceBundleZip = optionSet.valueOf(this.sourceBundleZip),

            targetFolder = optionSet.valueOf(this.targetFolder),
            targetZip = optionSet.valueOf(this.targetZip)
        )
    }
}
