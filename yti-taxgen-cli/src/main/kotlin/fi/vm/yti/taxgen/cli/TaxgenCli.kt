package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.FailException
import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticContexts
import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticHaltPolicy
import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticVerbosityPolicy
import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.commons.throwHalt
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.DiagnosticBridge
import fi.vm.yti.taxgen.rddpmmapper.RdToDpmMapper
import fi.vm.yti.taxgen.rdsource.DpmSourceRecorder
import fi.vm.yti.taxgen.rdsource.SourceFactory
import fi.vm.yti.taxgen.rdsource.SourceHolder
import fi.vm.yti.taxgen.sqliteoutput.DpmDbWriter
import fi.vm.yti.taxgen.sqliteoutput.SQLiteDpmDbWriterFactory
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

    override fun close() {
        outWriter.close()
        errWriter.close()
    }

    fun execute(args: Array<String>): Int {
        return withExceptionHarness {
            val detectedOptions = definedOptions.detectOptionsFromArgs(args)

            val diagnosticBridge = setupDiagnosticBridge(detectedOptions)

            if (detectedOptions.cmdShowHelp) {
                definedOptions.printHelp(outWriter)
                throwHalt()
            }

            if (detectedOptions.cmdShowVersion) {
                TaxgenVersion.printVersion(outWriter)
                throwHalt()
            }

            detectedOptions.ensureSingleCommandGiven()

            if (detectedOptions.cmdCaptureDpmSourcesToFolder ||
                detectedOptions.cmdCaptureDpmSourcesToZip
            ) {
                captureDpmSources(detectedOptions, diagnosticBridge)
            }

            if (detectedOptions.cmdCreateDictionaryToNewDpmDb ||
                detectedOptions.cmdReplaceDictionaryInDpmDb
            ) {
                writeDictionaryToDpmDb(detectedOptions, diagnosticBridge)
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

    private fun setupDiagnosticBridge(detectedOptions: DetectedOptions): DiagnosticBridge {
        val eventConsumer = DiagnosticTextPrinter(outWriter)
        val stoppingPolicy = DiagnosticHaltPolicy()
        val filteringPolicy = DiagnosticVerbosityPolicy(detectedOptions.verbosity)
        return DiagnosticBridge(eventConsumer, stoppingPolicy, filteringPolicy)
    }

    private fun writeDictionaryToDpmDb(
        detectedOptions: DetectedOptions,
        diagnosticBridge: DiagnosticBridge
    ) {
        diagnosticBridge.withContext(
            contextType = DiagnosticContexts.CmdWriteDictionariesToDpmDb.toType(),
            contextDetails = null
        ) {
            detectedOptions.ensureSingleSourceGiven()
            detectedOptions.ensureOutputGiven()

            lateinit var processingOptions: ProcessingOptions
            lateinit var dpmModel: DpmModel

            resolveSource(detectedOptions, diagnosticBridge).use { sourceHolder ->
                sourceHolder.withDpmSource { dpmSource ->

                    processingOptions = dpmSource.config().processingOptions

                    diagnosticBridge.setDiagnosticSourceLanguages(
                        processingOptions.diagnosticSourceLanguages
                    )

                    val dpmMapper = RdToDpmMapper(diagnosticBridge)
                    dpmModel = dpmMapper.extractDpmModel(dpmSource)
                }
            }

            diagnosticBridge.stopIfCriticalErrorsReceived {
                "Mapping failed due content errors"
            }

            val dbWriter = resolveDpmDbWriter(detectedOptions, diagnosticBridge)
            dbWriter.writeModel(
                dpmModel,
                processingOptions
            )

            diagnosticBridge.stopIfCriticalErrorsReceived {
                "Database creation failed due content errors"
            }
        }
    }

    private fun captureDpmSources(
        detectedOptions: DetectedOptions,
        diagnosticBridge: DiagnosticBridge
    ) {
        diagnosticBridge.withContext(
            contextType = DiagnosticContexts.CmdCaptureDpmSources.toType(),
            contextDetails = null
        ) {
            detectedOptions.ensureSingleSourceGiven()
            detectedOptions.ensureOutputGiven()

            resolveSource(detectedOptions, diagnosticBridge).use { sourceHolder ->
                sourceHolder.withDpmSource { dpmSource ->

                    val processingOptions = dpmSource.config().processingOptions

                    diagnosticBridge.setDiagnosticSourceLanguages(
                        processingOptions.diagnosticSourceLanguages
                    )

                    resolveSourceRecorder(detectedOptions, diagnosticBridge).use { sourceRecorder ->
                        sourceRecorder.captureSources(dpmSource)
                    }
                }
            }
        }

        diagnosticBridge.stopIfCriticalErrorsReceived {
            "Capturing failed"
        }
    }

    private fun resolveSource(
        detectedOptions: DetectedOptions,
        diagnosticBridge: DiagnosticBridge
    ): SourceHolder {
        if (detectedOptions.sourceConfigFile != null) {
            return SourceFactory.sourceForConfigFile(
                configFilePath = detectedOptions.sourceConfigFile,
                diagnosticContext = diagnosticBridge
            )
        }

        if (detectedOptions.sourceFolder != null) {
            return SourceFactory.sourceForFolder(
                sourceRootPath = detectedOptions.sourceFolder,
                diagnosticContext = diagnosticBridge
            )
        }

        if (detectedOptions.sourceZipFile != null) {
            return SourceFactory.sourceForZipFile(
                zipFilePath = detectedOptions.sourceZipFile,
                diagnosticContext = diagnosticBridge
            )
        }

        thisShouldNeverHappen("No suitable source given")
    }

    private fun resolveSourceRecorder(
        detectedOptions: DetectedOptions,
        diagnosticBridge: DiagnosticBridge
    ): DpmSourceRecorder {

        if (detectedOptions.cmdCaptureDpmSourcesToFolder) {
            return SourceFactory.folderRecorder(
                outputFolderPath = requiredOption(detectedOptions.output),
                forceOverwrite = detectedOptions.forceOverwrite,
                diagnosticContext = diagnosticBridge
            )
        }

        if (detectedOptions.cmdCaptureDpmSourcesToZip) {
            return SourceFactory.zipRecorder(
                outputZipPath = requiredOption(detectedOptions.output),
                forceOverwrite = detectedOptions.forceOverwrite,
                diagnosticContext = diagnosticBridge
            )
        }

        thisShouldNeverHappen("No suitable source recorder given")
    }

    private fun resolveDpmDbWriter(
        detectedOptions: DetectedOptions,
        diagnosticBridge: DiagnosticBridge
    ): DpmDbWriter {

        if (detectedOptions.cmdCreateDictionaryToNewDpmDb) {
            return SQLiteDpmDbWriterFactory.dictionaryCreateWriter(
                outputDbPath = requiredOption(detectedOptions.output),
                forceOverwrite = detectedOptions.forceOverwrite,
                keepPartialOutput = detectedOptions.keepPartialOutput,
                diagnosticContext = diagnosticBridge
            )
        }

        if (detectedOptions.cmdReplaceDictionaryInDpmDb) {
            detectedOptions.ensureBaselineDpmDbGiven()

            return SQLiteDpmDbWriterFactory.dictionaryReplaceWriter(
                baselineDbPath = requiredOption(detectedOptions.baselineDb),
                outputDbPath = requiredOption(detectedOptions.output),
                forceOverwrite = detectedOptions.forceOverwrite,
                keepPartialOutput = detectedOptions.keepPartialOutput,
                diagnosticContext = diagnosticBridge
            )
        }

        thisShouldNeverHappen("No suitable DB writer given")
    }

    private fun <T : Any> requiredOption(value: T?): T {
        return value ?: thisShouldNeverHappen("Command precondition mismatch.")
    }
}
