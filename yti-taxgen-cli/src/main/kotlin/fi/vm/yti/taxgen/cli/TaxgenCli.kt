package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmdbwriter.DpmDbWriter
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.yclsourceprovider.YclSourceRecorder
import fi.vm.yti.taxgen.yclsourceprovider.api.YclSourceApiAdapter
import fi.vm.yti.taxgen.yclsourceprovider.folder.YclSourceFolderStructureAdapter
import fi.vm.yti.taxgen.yclsourceprovider.folder.YclSourceFolderStructureRecorder
import fi.vm.yti.taxgen.yclsourceprovider.zip.YclSourceZipFileAdapter
import fi.vm.yti.taxgen.yclsourceprovider.zip.YclSourceZipFileRecorder
import fi.vm.yti.taxgen.ycltodpmmapper.YclSourceParser
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

            detectedOptions.ensureSingleCommandGiven()

            if (detectedOptions.cmdBundleYclSourcesToFolder != null ||
                detectedOptions.cmdBundleYclSourcesToZip != null
            ) {
                outWriter.println("Bundling YTI Codelist based XBRL Taxonomy sources...")

                detectedOptions.ensureSingleSourceGiven()

                resolveYclSourceBundle(detectedOptions).use { sourceBundle ->
                    resolveYclSourceBundleWriter(detectedOptions, sourceBundle).use { sourceBundleWriter ->
                        sourceBundleWriter.capture()
                    }
                }
            }

            if (detectedOptions.cmdWriteDpmDb != null) {
                outWriter.println("Writing DPM DB from YTI Codelist based XBRL Taxonomy sources...")

                detectedOptions.ensureSingleSourceGiven()

                resolveYclSourceBundle(detectedOptions).use { sourceBundle ->

                    val parser = YclSourceParser()
                    parser.parse(sourceBundle)

                    val writer = DpmDbWriter(
                        detectedOptions.cmdWriteDpmDb,
                        detectedOptions.forceOverwrite
                    )
                    writer.writedb()
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

    private fun resolveYclSourceBundle(detectedOptions: DetectedOptions): YclSource {
        if (detectedOptions.sourceConfig != null) {
            return YclSourceApiAdapter(
                configFilePath = detectedOptions.sourceConfig
            )
        }

        if (detectedOptions.sourceBundleFolder != null) {
            return YclSourceFolderStructureAdapter(detectedOptions.sourceBundleFolder)
        }

        if (detectedOptions.sourceBundleZip != null) {
            return YclSourceZipFileAdapter(detectedOptions.sourceBundleZip)
        }

        thisShouldNeverHappen("No suitable YCL taxonomy source")
    }

    private fun resolveYclSourceBundleWriter(
        detectedOptions: DetectedOptions,
        yclSource: YclSource
    ): YclSourceRecorder {

        if (detectedOptions.cmdBundleYclSourcesToFolder != null) {
            return YclSourceFolderStructureRecorder(
                detectedOptions.cmdBundleYclSourcesToFolder,
                yclSource,
                detectedOptions.forceOverwrite
            )
        }

        if (detectedOptions.cmdBundleYclSourcesToZip != null) {
            return YclSourceZipFileRecorder(
                detectedOptions.cmdBundleYclSourcesToZip,
                yclSource,
                detectedOptions.forceOverwrite
            )
        }

        thisShouldNeverHappen("No suitable YCL source bundle target")
    }
}
