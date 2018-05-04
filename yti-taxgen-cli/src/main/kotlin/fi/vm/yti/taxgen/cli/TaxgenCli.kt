package fi.vm.yti.taxgen.cli

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.cli.yclsourcebundler.toSourceBundle
import fi.vm.yti.taxgen.cli.yclsourceconfig.YclSourceConfig
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.YclSourceBundle
import java.io.BufferedWriter
import java.io.Closeable
import java.io.File
import java.io.OutputStreamWriter
import java.io.PrintStream
import java.io.PrintWriter
import java.nio.charset.Charset

class TaxgenCli(
    private val outStream: PrintStream,
    private val errStream: PrintStream,
    charset: Charset,
    private val definedOptions: DefinedOptions
) : Closeable {

    private val outWriter = PrintWriter(BufferedWriter(OutputStreamWriter(outStream, charset)))
    private val errWriter = PrintWriter(BufferedWriter(OutputStreamWriter(errStream, charset)))

    override fun close() {
        outWriter.close()
        errWriter.close()

        outStream.flush()
        errStream.flush()
    }

    fun execute(args: Array<String>): Int {
        return unlessHaltOrFail {
            val detectedOptions = definedOptions.detectOptionsFromArgs(args, outWriter, errWriter)

            if (detectedOptions.cmdBundleSources != null) {
                val sourceBundle = resolveSources(detectedOptions)
                //writeSourceBundle(sourceBundle, detectedOptions)
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

    private fun readYclSourceConfig(yclSourceConfigFile: File): YclSourceConfig {
        return jacksonObjectMapper().apply {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }.readValue(yclSourceConfigFile)
    }

    private fun resolveSources(detectedOptions: DetectedOptions): YclSourceBundle {
        if (detectedOptions.yclSourceConfig != null) {
            val yclSourceConfig = readYclSourceConfig(detectedOptions.yclSourceConfig.toFile())
            return yclSourceConfig.toSourceBundle()
        }

        halt(1)
    }
}
