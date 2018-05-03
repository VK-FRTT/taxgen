package fi.vm.yti.taxgen.cli

import joptsimple.OptionException
import joptsimple.OptionParser
import joptsimple.OptionSpec
import java.io.File
import java.io.PrintWriter

class DefinedOptions {

    private val optionParser = OptionParser()
    private val help: OptionSpec<Void>
    private val yclSourceConfig: OptionSpec<File>

    init {
        help = optionParser.acceptsAll(
            listOf("h", "help"),
            "Show this help message"
        ).forHelp()

        yclSourceConfig = optionParser
            .accepts(
                "ycl-source-config",
                """Define taxonomy source configuration,
                |YTI CodeList service is used as information source""".trimMargin())
            .withOptionalArg()
            .ofType<File>(File::class.java)
    }

    fun detectOptionsFromArgs(args: Array<String>, consoleOut: PrintWriter): DetectedOptions {
        return try {
            val detectedOptions = doDetectOptions(args)
            if (detectedOptions.help) {
                optionParser.printHelpOn(consoleOut)
                halt(TAXGEN_CLI_SUCCESS)
            }

            detectedOptions
        } catch (exception: OptionException) {
            consoleOut.println("yti-taxgen-cli: ${exception.message}")
            halt(TAXGEN_CLI_FAIL)
        }
    }

    private fun doDetectOptions(args: Array<String>): DetectedOptions {
        val optionSet = optionParser.parse(*args)

        return DetectedOptions(
            help = optionSet.has(this.help),
            yclSourceConfigFile = optionSet.valueOf(this.yclSourceConfig)
        )
    }
}
