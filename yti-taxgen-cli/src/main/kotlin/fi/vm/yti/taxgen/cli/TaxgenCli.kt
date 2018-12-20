package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.FailException
import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.commons.diagostic.Severity
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.commons.throwHalt
import fi.vm.yti.taxgen.sqliteprovider.DpmDbWriter
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.DpmSourceRecorder
import fi.vm.yti.taxgen.rdsprovider.rds.DpmSourceRdsAdapter
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceFolderAdapter
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceRecorderFolderAdapter
import fi.vm.yti.taxgen.rdsprovider.zip.DpmSourceZipFileAdapter
import fi.vm.yti.taxgen.rdsprovider.zip.DpmSourceRecorderZipFileAdapter
import fi.vm.yti.taxgen.rdsdpmmapper.RdsToDpmMapper
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
    private val diagnostic: Diagnostic = DiagnosticBridge(dtp)

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

            if (detectedOptions.cmdCaptureDpmSourcesToFolder != null ||
                detectedOptions.cmdCaptureDpmSourcesToZip != null
            ) {
                captureDpmSources(detectedOptions)
            }

            if (detectedOptions.cmdCompileDpmDb != null) {
                compileDpmDb(detectedOptions)
            }
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

    private fun compileDpmDb(detectedOptions: DetectedOptions) {
        diagnostic.withContext(
            contextType = DiagnosticContextType.CmdCompileDpmDb
        ) {
            detectedOptions.ensureSingleSourceGiven()

            resolveRdsSource(detectedOptions).use { yclSource ->
                val dbWriter = resolveDpmDbWriter(detectedOptions)
                val dpmMapper = RdsToDpmMapper(diagnostic)

                val dpmDictionaries = dpmMapper.getDpmDictionariesFromSource(yclSource)

                if (diagnostic.counters()[Severity.ERROR] != 0) {
                    diagnostic.info("Mapping failed due content errors")
                    throwHalt()
                }

                dbWriter.writeDpmDb(dpmDictionaries)
            }
        }
    }

    private fun captureDpmSources(detectedOptions: DetectedOptions) {
        diagnostic.withContext(
            contextType = DiagnosticContextType.CmdCaptureDpmSources
        ) {
            detectedOptions.ensureSingleSourceGiven()

            resolveRdsSource(detectedOptions).use { yclSource ->
                resolveRdsSourceRecorder(detectedOptions).use { yclSourceRecorder ->
                    yclSourceRecorder.captureSources(yclSource)
                }
            }

            if (diagnostic.counters()[Severity.ERROR] != 0) {
                diagnostic.info("Capturing failed")
            }
        }
    }

    private fun resolveRdsSource(detectedOptions: DetectedOptions): DpmSource {
        if (detectedOptions.sourceConfigFile != null) {
            return DpmSourceRdsAdapter(
                configPath = detectedOptions.sourceConfigFile,
                diagnostic = diagnostic
            )
        }

        if (detectedOptions.sourceFolder != null) {
            return DpmSourceFolderAdapter(
                dpmSourceRootPath = detectedOptions.sourceFolder
            )
        }

        if (detectedOptions.sourceZipFile != null) {
            return DpmSourceZipFileAdapter(
                sourceZipPath = detectedOptions.sourceZipFile
            )
        }

        thisShouldNeverHappen("No suitable source given")
    }

    private fun resolveRdsSourceRecorder(
        detectedOptions: DetectedOptions
    ): DpmSourceRecorder {

        if (detectedOptions.cmdCaptureDpmSourcesToFolder != null) {
            return DpmSourceRecorderFolderAdapter(
                baseFolderPath = detectedOptions.cmdCaptureDpmSourcesToFolder,
                forceOverwrite = detectedOptions.forceOverwrite,
                diagnostic = diagnostic
            )
        }

        if (detectedOptions.cmdCaptureDpmSourcesToZip != null) {
            return DpmSourceRecorderZipFileAdapter(
                targetZipPath = detectedOptions.cmdCaptureDpmSourcesToZip,
                forceOverwrite = detectedOptions.forceOverwrite,
                diagnostic = diagnostic
            )
        }

        thisShouldNeverHappen("No suitable source recorder given")
    }

    private fun resolveDpmDbWriter(
        detectedOptions: DetectedOptions
    ): DpmDbWriter {
        return DpmDbWriter(
            rawTargetDbPath = detectedOptions.cmdCompileDpmDb!!,
            forceOverwrite = detectedOptions.forceOverwrite,
            diagnostic = diagnostic
        )
    }
}
