package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder.FolderSourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice.YclServiceSourceBundle
import java.io.BufferedWriter
import java.io.Closeable
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
        return withExceptionHarness {
            val detectedOptions = definedOptions.detectOptionsFromArgs(args)

            if (detectedOptions.cmdShowHelp) {
                definedOptions.printHelp(outWriter)
                halt(TAXGEN_CLI_SUCCESS)
            }

            detectedOptions.ensureSingleOperation()
            detectedOptions.ensureSingleSource()
            detectedOptions.ensureSingleTarget()

            if (detectedOptions.cmdBundleYclSource) {
                val sourceBundle = resolveYclSourceBundle(detectedOptions)
            }

            if (detectedOptions.cmdGenerateYclTaxonomy) {
            }
        }
    }

    private fun withExceptionHarness(steps: () -> Unit): Int {
        return try {
            steps()
            TAXGEN_CLI_SUCCESS
        } catch (exception: HaltException) {
            if (exception.errorMessage != null) {
                errStream.println("yti-taxgen: ${exception.errorMessage}")
                errStream.println()
            }

            exception.exitCode
        } catch (exception: Throwable) {
            exception.printStackTrace(errStream)
            errStream.println()
            TAXGEN_CLI_FAIL
        }
    }

    private fun resolveYclSourceBundle(detectedOptions: DetectedOptions): SourceBundle {
        if (detectedOptions.sourceConfig != null) {
            return YclServiceSourceBundle(detectedOptions.sourceConfig)
        }

        if (detectedOptions.sourceBundleFolder != null) {
            return FolderSourceBundle(detectedOptions.sourceBundleFolder)
        }

        //if (detectedOptions.sourceBundleZip != null) {
        //    return ZipSourceBundle(detectedOptions.sourceBundleZip)
        //}

        thisShouldNeverHappen("No suitable YCL taxonomy source")
    }
}
