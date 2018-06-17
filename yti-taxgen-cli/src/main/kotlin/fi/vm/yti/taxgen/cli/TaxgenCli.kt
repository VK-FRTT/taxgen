package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.yclsourceparser.YclSourceParser
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundleWriter
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder.FolderSourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder.FolderSourceBundleWriter
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.ycl.YclSourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.zip.ZipSourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.zip.ZipSourceBundleWriter
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

            if (detectedOptions.cmdBundleYclSource) {
                outWriter.println("Bundling XBRL Taxonomy sources from YTI Codelist service...")

                detectedOptions.ensureSingleSource()
                detectedOptions.ensureSingleTarget()

                resolveYclSourceBundle(detectedOptions).use { sourceBundle ->
                    resolveYclSourceBundleWriter(detectedOptions, sourceBundle).use { sourceBundleWriter ->
                        sourceBundleWriter.write()
                    }
                }
            }

            if (detectedOptions.cmdGenerateYclTaxonomy) {
                outWriter.println("Generating XBRL Taxonomy from YTI Codelist service sources...")

                detectedOptions.ensureSingleSource()
                //detectedOptions.ensureSingleTarget()

                resolveYclSourceBundle(detectedOptions).use { sourceBundle ->

                    val parser = YclSourceParser()
                    parser.parse(sourceBundle)
                }
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
            return YclSourceBundle(
                sourceConfigFilePath = detectedOptions.sourceConfig
            )
        }

        if (detectedOptions.sourceBundleFolder != null) {
            return FolderSourceBundle(detectedOptions.sourceBundleFolder)
        }

        if (detectedOptions.sourceBundleZip != null) {
            return ZipSourceBundle(detectedOptions.sourceBundleZip)
        }

        thisShouldNeverHappen("No suitable YCL taxonomy source")
    }

    private fun resolveYclSourceBundleWriter(
        detectedOptions: DetectedOptions,
        sourceBundle: SourceBundle
    ): SourceBundleWriter {

        if (detectedOptions.targetFolder != null) {
            return FolderSourceBundleWriter(
                detectedOptions.targetFolder,
                sourceBundle,
                detectedOptions.targetForceOverwrite
            )
        }

        if (detectedOptions.targetZip != null) {
            return ZipSourceBundleWriter(
                detectedOptions.targetZip,
                sourceBundle,
                detectedOptions.targetForceOverwrite
            )
        }

        thisShouldNeverHappen("No suitable YCL source bundle target")
    }
}
