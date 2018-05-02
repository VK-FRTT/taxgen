package fi.vm.yti.taxgen.cli

import joptsimple.OptionParser
import joptsimple.OptionSet
import joptsimple.OptionSpec
import java.io.File
import java.io.Writer

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

    fun detectOptionsFromArgs(args: Array<String>): DetectedOptions {
        val optionSet = optionParser.parse(*args)

        return mapParseResultToDetectedOptions(optionSet)
    }

    private fun mapParseResultToDetectedOptions(optionSet: OptionSet): DetectedOptions {
        optionSet.valueOf(this.yclConfig)

        return DetectedOptions(
            help = optionSet.has(this.help),
            yclConfigFile = optionSet.valueOf(this.yclConfig)
        )
    }

    fun renderHelp(writer: Writer) {
        optionParser.printHelpOn(writer)
    }
}
