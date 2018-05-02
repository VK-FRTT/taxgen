package fi.vm.yti.taxgen.cli

import java.io.BufferedWriter
import java.io.Closeable
import java.io.OutputStreamWriter
import java.io.PrintStream
import java.io.PrintWriter
import java.nio.charset.Charset

class TaxgenCli(
    val outStream: PrintStream,
    val errStream: PrintStream,
    val charset: Charset,
    val definedOptions: DefinedOptions
) : Closeable {

    private val consoleOut = PrintWriter(BufferedWriter(OutputStreamWriter(outStream, charset)))

    override fun close() {
        consoleOut.close()

        outStream.flush()
        errStream.flush()
    }

    fun execute(args: Array<String>): Int {
        return unlessHaltOrFail {
            val detectedOptions = definedOptions.detectOptionsFromArgs(args, consoleOut)

            if (detectedOptions.yclConfigFile != null) {
                //val yclConfig = readYclConfig(detectedOptions.yclConfigFile)
                //val yclInputBundle = bundleYclInput(yclConfig)
            }
        }
    }

    private fun unlessHaltOrFail(steps: () -> Unit): Int {
        return try {
            steps()
            TAXGEN_CLI_SUCCESS
        } catch (exception: HaltException) {
            exception.exitCode
        } catch (exception: Throwable) {
            exception.printStackTrace(errStream)
            errStream.println()
            TAXGEN_CLI_FAIL
        }
    }
}
