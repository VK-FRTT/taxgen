package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.FailException
import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.commons.diagostic.Severity
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.commons.throwHalt
import fi.vm.yti.taxgen.dpmdbwriter.DpmDbWriter
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

const val TAXGEN_CLI_SUCCESS = 0
const val TAXGEN_CLI_FAIL = 1

class TaxgenCli(
    outStream: PrintStream,
    errStream: PrintStream,
    charset: Charset,
    private val definedOptions: DefinedOptions
) : Closeable {

    private val outWriter = PrintWriter(BufferedWriter(OutputStreamWriter(outStream, charset)), true)
    private val errWriter = PrintWriter(BufferedWriter(OutputStreamWriter(errStream, charset)), true)

    private val dtp = DiagnosticTextPrinter(outWriter)
    private val diagnostic = DiagnosticBridge(dtp)

    override fun close() {
        outWriter.close()
        errWriter.close()
    }

    fun execute(args: Array<String>): Int {
        return withExceptionHarness {
            val detectedOptions = definedOptions.detectOptionsFromArgs(args)

            if (detectedOptions.cmdShowHelp) {
                definedOptions.printHelp(outWriter)
                throwHalt()
            }

            detectedOptions.ensureSingleCommandGiven()

            if (detectedOptions.cmdCaptureYclSourcesToFolder != null ||
                detectedOptions.cmdCaptureYclSourcesToZip != null
            ) {
                detectedOptions.ensureSingleSourceGiven()

                resolveYclSource(detectedOptions).use { yclSource ->
                    resolveYclSourceRecorder(detectedOptions, yclSource).use { yclSourceRecorder ->
                        diagnostic.info("Capturing YTI Codelist sources..")
                        yclSourceRecorder.capture()
                    }
                }

                throwHalt()
            }

            if (detectedOptions.cmdCompileDpmDb != null) {
                detectedOptions.ensureSingleSourceGiven()

                resolveYclSource(detectedOptions).use { yclSource ->
                    val dbWriter = resolveDpmDbWriter(detectedOptions)

                    diagnostic.info("Compiling DPM database from YTI Codelist sources")

                    diagnostic.info("Mapping YTI Codelists to DPM model..")
                    val dpmDictionaries = YclToDpmMapper().getDpmDictionariesFromSource(diagnostic, yclSource)

                    if (diagnostic.counters()[Severity.ERROR] != 0) {
                        diagnostic.info("Mapping failed due content errors")
                        throwHalt()
                    }

                    diagnostic.info("Mapping done")
                    diagnostic.info("Writing DPM database..")
                    dbWriter.writeDpmDictionaries(dpmDictionaries)
                    diagnostic.info("DPM database written: ${dbWriter.targetDbPath}")
                }
            }

            throwHalt()
        }
    }

    private fun withExceptionHarness(steps: () -> Unit): Int {
        return try {
            steps()
            TAXGEN_CLI_SUCCESS
        } catch (exception: HaltException) {
            TAXGEN_CLI_SUCCESS
        } catch (exception: FailException) {
            errWriter.println("yti-taxgen: ${exception.message}")
            errWriter.println()

            TAXGEN_CLI_FAIL
        } catch (exception: Throwable) {
            errWriter.println("yti-taxgen:")
            exception.printStackTrace(errWriter)
            errWriter.println()

            TAXGEN_CLI_FAIL
        }
    }

    private fun resolveYclSource(detectedOptions: DetectedOptions): YclSource {
        if (detectedOptions.sourceConfigFile != null) {
            return YclSourceApiAdapter(
                configPath = detectedOptions.sourceConfigFile,
                diagnostic = diagnostic
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

    private fun resolveDpmDbWriter(
        detectedOptions: DetectedOptions
    ): DpmDbWriter {
        return DpmDbWriter(
            targetDbPath = detectedOptions.cmdCompileDpmDb!!,
            forceOverwrite = detectedOptions.forceOverwrite
        )
    }
}
