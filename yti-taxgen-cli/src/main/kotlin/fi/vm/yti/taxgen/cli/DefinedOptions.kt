package fi.vm.yti.taxgen.cli

import joptsimple.OptionException
import joptsimple.OptionParser
import joptsimple.OptionSpec
import java.io.File
import java.io.PrintWriter

class DefinedOptions {

    private val optionParser = OptionParser()
    private val help: OptionSpec<Void>
    private val yclConfig: OptionSpec<File>

    init {
        help = optionParser.acceptsAll(
            listOf("h", "help"),
            "Show this help message"
        ).forHelp()

        yclConfig = optionParser
            .accepts("ycl-config", "YCL configuration file")
            .withOptionalArg()
            .ofType<File>(File::class.java)
    }

    fun detectOptionsFromArgs(args: Array<String>, consoleOut: PrintWriter): DetectedOptions {
        return try {
            val detectedOptions = parseArgsToToDetectedOptions(args)
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

    private fun parseArgsToToDetectedOptions(args: Array<String>): DetectedOptions {
        val optionSet = optionParser.parse(*args)

        return DetectedOptions(
            help = optionSet.has(this.help),
            yclConfigFile = optionSet.valueOf(this.yclConfig)
        )
    }
}
