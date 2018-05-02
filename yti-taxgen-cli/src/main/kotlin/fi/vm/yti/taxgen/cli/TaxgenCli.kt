package fi.vm.yti.taxgen.cli

import joptsimple.OptionException
import java.io.BufferedWriter
import java.io.Closeable
import java.io.OutputStreamWriter
import java.io.PrintStream
import java.io.PrintWriter
import java.nio.charset.Charset

const val TAXGEN_CLI_SUCCESS = 0
const val TAXGEN_CLI_FAIL = 1

class TaxgenCli(
    val outStream: PrintStream,
    val errStream: PrintStream,
    val charset: Charset,
    val definedOptions: DefinedOptions
) : Closeable {

    private val outWriter = PrintWriter(BufferedWriter(OutputStreamWriter(outStream, charset)))

    override fun close() {
        outWriter.close()

        outStream.flush()
        errStream.flush()
    }

    fun execute(args: Array<String>): Int {
        return try {
            doExecute(args)
            TAXGEN_CLI_SUCCESS
        } catch (exception: HaltException) {
            exception.exitCode
        } catch (exception: Throwable) {
            exception.printStackTrace(errStream)
            errStream.println()
            TAXGEN_CLI_FAIL
        }
    }

    private fun doExecute(args: Array<String>) {
        val detectedOptions = detectOptions(args)
    }

    private fun detectOptions(args: Array<String>): DetectedOptions {
        try {
            val detectedOptions = definedOptions.detectOptionsFromArgs(args)
            if (detectedOptions.help) {
                definedOptions.renderHelp(outWriter)
                halt(TAXGEN_CLI_SUCCESS)
            }

            return detectedOptions
        } catch (exception: OptionException) {
            outWriter.println("yti-taxgen-cli: ${exception.message}")
            halt(TAXGEN_CLI_FAIL)
        }
    }

    private fun halt(exitCode: Int): Nothing {
        throw HaltException(exitCode)
    }
}
