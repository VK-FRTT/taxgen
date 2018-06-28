package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmdbwriter.DpmDbProducer
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.yclsourceprovider.YclSourceRecorder
import fi.vm.yti.taxgen.yclsourceprovider.api.YclSourceApiAdapter
import fi.vm.yti.taxgen.yclsourceprovider.folder.YclSourceFolderStructureAdapter
import fi.vm.yti.taxgen.yclsourceprovider.folder.YclSourceFolderStructureRecorder
import fi.vm.yti.taxgen.yclsourceprovider.zip.YclSourceZipFileAdapter
import fi.vm.yti.taxgen.yclsourceprovider.zip.YclSourceZipFileRecorder
import fi.vm.yti.taxgen.ycltodpmmapper.YclToDpmMapper
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

    private val outWriter = PrintWriter(BufferedWriter(OutputStreamWriter(outStream, charset)), true)
    private val errWriter = PrintWriter(BufferedWriter(OutputStreamWriter(errStream, charset)), true)

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

            if (detectedOptions.cmdCaptureYclSourcesToFolder != null ||
                detectedOptions.cmdCaptureYclSourcesToZip != null
            ) {
                detectedOptions.ensureSingleSourceGiven()

                resolveYclSource(detectedOptions).use { yclSource ->
                    resolveYclSourceRecorder(detectedOptions, yclSource).use { yclSourceRecorder ->

                        outWriter.println("Capturing YTI Codelist based sources...")
                        yclSourceRecorder.capture()
                    }
                }
            }

            if (detectedOptions.cmdProduceDpmDb != null) {
                detectedOptions.ensureSingleSourceGiven()

                resolveYclSource(detectedOptions).use { yclSource ->
                    val dbProducer = resolveDpmDbProducer(detectedOptions)

                    outWriter.println("Producing DPM database from YTI Codelist based sources...")

                    val dpmDictionaries = YclToDpmMapper().dpmDictionariesFromYclSource(yclSource)

                    dbProducer.writedb()
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

    private fun resolveYclSource(detectedOptions: DetectedOptions): YclSource {
        if (detectedOptions.sourceConfigFile != null) {
            return YclSourceApiAdapter(
                configFilePath = detectedOptions.sourceConfigFile
            )
        }

        if (detectedOptions.sourceFolder != null) {
            return YclSourceFolderStructureAdapter(
                baseFolderPath = detectedOptions.sourceFolder
            )
        }

        if (detectedOptions.sourceZipFile != null) {
            return YclSourceZipFileAdapter(
                sourceZipPath = detectedOptions.sourceZipFile
            )
        }

        thisShouldNeverHappen("No suitable source given")
    }

    private fun resolveYclSourceRecorder(
        detectedOptions: DetectedOptions,
        yclSource: YclSource
    ): YclSourceRecorder {

        if (detectedOptions.cmdCaptureYclSourcesToFolder != null) {
            return YclSourceFolderStructureRecorder(
                baseFolderPath = detectedOptions.cmdCaptureYclSourcesToFolder,
                yclSource = yclSource,
                forceOverwrite = detectedOptions.forceOverwrite
            )
        }

        if (detectedOptions.cmdCaptureYclSourcesToZip != null) {
            return YclSourceZipFileRecorder(
                targetZipPath = detectedOptions.cmdCaptureYclSourcesToZip,
                yclSource = yclSource,
                forceOverwrite = detectedOptions.forceOverwrite
            )
        }

        thisShouldNeverHappen("No suitable source recorder given")
    }

    private fun resolveDpmDbProducer(
        detectedOptions: DetectedOptions
    ): DpmDbProducer {
        return DpmDbProducer(
            targetDbPath = detectedOptions.cmdProduceDpmDb!!,
            forceOverwrite = detectedOptions.forceOverwrite
        )
    }
}
