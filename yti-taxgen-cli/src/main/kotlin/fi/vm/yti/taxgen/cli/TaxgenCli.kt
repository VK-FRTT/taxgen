package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.FailException
import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.commons.diagostic.Severity
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.commons.throwHalt
import fi.vm.yti.taxgen.rdsdpmmapper.RdsToDpmMapper
import fi.vm.yti.taxgen.rdsprovider.DpmSourceRecorder
import fi.vm.yti.taxgen.rdsprovider.ProviderFactory
import fi.vm.yti.taxgen.rdsprovider.SourceProvider
import fi.vm.yti.taxgen.sqliteprovider.DpmDbWriter
import fi.vm.yti.taxgen.sqliteprovider.DpmDbWriterFactory
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
    private val diagnosticContext: DiagnosticContext = DiagnosticBridge(dtp)

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

            if (detectedOptions.cmdCreateDictionaryToNewDpmDb != null ||
                detectedOptions.cmdReplaceDictionaryInDpmDb != null
            ) {
                writeDictionaryToDpmDb(detectedOptions)
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

    private fun writeDictionaryToDpmDb(detectedOptions: DetectedOptions) {
        diagnosticContext.withContext(
            contextType = DiagnosticContextType.CmdCompileDpmDb
        ) {
            detectedOptions.ensureSingleSourceGiven()

            val sourceProvider = resolveSourceProvider(detectedOptions)

            val dpmDictionaries = sourceProvider.use {
                val dpmMapper = RdsToDpmMapper(diagnosticContext)
                dpmMapper.extractDpmDictionariesFromSource(sourceProvider)
            }

            if (diagnosticContext.counters()[Severity.ERROR] != 0) {
                diagnosticContext.info("Mapping failed due content errors")
                throwHalt()
            }

            val dbWriter = resolveDpmDbWriter(detectedOptions)
            dbWriter.writeWithDictionaries(dpmDictionaries)
        }
    }

    private fun captureDpmSources(detectedOptions: DetectedOptions) {
        diagnosticContext.withContext(
            contextType = DiagnosticContextType.CmdCaptureDpmSources
        ) {
            detectedOptions.ensureSingleSourceGiven()

            val sourceProvider = resolveSourceProvider(detectedOptions)
            val recorder = resolveRdsSourceRecorder(detectedOptions)

            recorder.use { rdsSourceRecorder ->
                rdsSourceRecorder.captureSources(sourceProvider)
            }
        }

        if (diagnosticContext.counters()[Severity.ERROR] != 0) {
            diagnosticContext.info("Capturing failed")
        }
    }

    private fun resolveSourceProvider(detectedOptions: DetectedOptions): SourceProvider {
        if (detectedOptions.sourceConfigFile != null) {
            return ProviderFactory.rdsProvider(
                configFilePath = detectedOptions.sourceConfigFile,
                diagnosticContext = diagnosticContext
            )
        }

        if (detectedOptions.sourceFolder != null) {
            return ProviderFactory.folderProvider(
                sourceRootPath = detectedOptions.sourceFolder,
                diagnosticContext = diagnosticContext
            )
        }

        if (detectedOptions.sourceZipFile != null) {
            return ProviderFactory.zipFileProvider(
                zipFilePath = detectedOptions.sourceZipFile,
                diagnosticContext = diagnosticContext
            )
        }

        thisShouldNeverHappen("No suitable source given")
    }

    private fun resolveRdsSourceRecorder(
        detectedOptions: DetectedOptions
    ): DpmSourceRecorder {

        if (detectedOptions.cmdCaptureDpmSourcesToFolder != null) {
            return ProviderFactory.folderRecorder(
                baseFolderPath = detectedOptions.cmdCaptureDpmSourcesToFolder,
                forceOverwrite = detectedOptions.forceOverwrite,
                diagnosticContext = diagnosticContext
            )
        }

        if (detectedOptions.cmdCaptureDpmSourcesToZip != null) {
            return ProviderFactory.zipRecorder(
                zipFilePath = detectedOptions.cmdCaptureDpmSourcesToZip,
                forceOverwrite = detectedOptions.forceOverwrite,
                diagnosticContext = diagnosticContext
            )
        }

        thisShouldNeverHappen("No suitable source recorder given")
    }

    private fun resolveDpmDbWriter(
        detectedOptions: DetectedOptions
    ): DpmDbWriter {

        if (detectedOptions.cmdCreateDictionaryToNewDpmDb != null) {
            return DpmDbWriterFactory.dictionaryCreateWriter(
                targetDbPath = detectedOptions.cmdCreateDictionaryToNewDpmDb,
                forceOverwrite = detectedOptions.forceOverwrite,
                diagnosticContext = diagnosticContext
            )
        }

        if (detectedOptions.cmdReplaceDictionaryInDpmDb != null) {
            return DpmDbWriterFactory.dictionaryReplaceWriter(
                targetDbPath = detectedOptions.cmdReplaceDictionaryInDpmDb,
                diagnosticContext = diagnosticContext
            )
        }

        thisShouldNeverHappen("No suitable DB writer given")
    }
}
