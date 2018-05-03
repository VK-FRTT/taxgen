package fi.vm.yti.taxgen.cli

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.cli.yclsourceconfig.YclSourceConfig
import java.io.BufferedWriter
import java.io.Closeable
import java.io.File
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

            if (detectedOptions.yclSourceConfigFile != null) {
                val yclSourceConfig = readYclSourceConfig(detectedOptions.yclSourceConfigFile)
                val yclSourceBundle = bundleYclSources(yclSourceConfig)
                //val dpmMetamodel = YclSourceParser.parseSources(yclSourceBundle)
                //val taxonomy = FixtaGenerator.generateTaxonomy(dpmMetamodel)
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

    private fun bundleYclSources(yclSourceConfig: YclSourceConfig): String {
        return ""
    }
}
