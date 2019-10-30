package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.FailException
import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticContexts
import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticHaltPolicy
import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.commons.throwHalt
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.DiagnosticBridge
import fi.vm.yti.taxgen.rdsdpmmapper.RdsToDpmMapper
import fi.vm.yti.taxgen.rdsprovider.DpmSourceRecorder
import fi.vm.yti.taxgen.rdsprovider.SourceFactory
import fi.vm.yti.taxgen.rdsprovider.SourceHolder
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

    private val eventConsumer = DiagnosticTextPrinter(outWriter)
    private val stoppingPolicy = DiagnosticHaltPolicy()
    private val diagnosticBridge = DiagnosticBridge(eventConsumer, stoppingPolicy)

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

            if (detectedOptions.cmdShowVersion) {
                TaxgenVersion.printVersion(outWriter)
                throwHalt()
            }

            detectedOptions.ensureSingleCommandGiven()

            if (detectedOptions.cmdCaptureDpmSourcesToFolder ||
                detectedOptions.cmdCaptureDpmSourcesToZip
            ) {
                captureDpmSources(detectedOptions)
            }

            if (detectedOptions.cmdCreateDictionaryToNewDpmDb ||
                detectedOptions.cmdReplaceDictionaryInDpmDb
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
        diagnosticBridge.withContext(
            contextType = DiagnosticContexts.CmdWriteDictionariesToDpmDb.toType(),
            contextDetails = null
        ) {
            detectedOptions.ensureSingleSourceGiven()
            detectedOptions.ensureOutputGiven()

            lateinit var processingOptions: ProcessingOptions
            lateinit var dpmModel: DpmModel

            resolveSource(detectedOptions).use { sourceHolder ->
                sourceHolder.withDpmSource { dpmSource ->

                    processingOptions = dpmSource.config().processingOptions

                    diagnosticBridge.setDiagnosticSourceLanguages(
                        processingOptions.diagnosticSourceLanguages
                    )

                    val dpmMapper = RdsToDpmMapper(diagnosticBridge)
                    dpmModel = dpmMapper.extractDpmModel(dpmSource)
                }
            }

            diagnosticBridge.stopIfSignificantErrorsReceived {
                "Mapping failed due content errors"
            }

            val dbWriter = resolveDpmDbWriter(detectedOptions)
            dbWriter.writeModel(
                dpmModel,
                processingOptions
            )
        }
    }

    private fun captureDpmSources(detectedOptions: DetectedOptions) {
        diagnosticBridge.withContext(
            contextType = DiagnosticContexts.CmdCaptureDpmSources.toType(),
            contextDetails = null
        ) {
            detectedOptions.ensureSingleSourceGiven()
            detectedOptions.ensureOutputGiven()

            resolveSource(detectedOptions).use { sourceHolder ->
                sourceHolder.withDpmSource { dpmSource ->

                    val processingOptions = dpmSource.config().processingOptions

                    diagnosticBridge.setDiagnosticSourceLanguages(
                        processingOptions.diagnosticSourceLanguages
                    )

                    resolveSourceRecorder(detectedOptions).use { sourceRecorder ->
                        sourceRecorder.captureSources(dpmSource)
                    }
                }
            }
        }

        diagnosticBridge.stopIfSignificantErrorsReceived {
            "Capturing failed"
        }
    }

    private fun resolveSource(detectedOptions: DetectedOptions): SourceHolder {
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
        detectedOptions: DetectedOptions
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
        detectedOptions: DetectedOptions
    ): DpmDbWriter {

        if (detectedOptions.cmdCreateDictionaryToNewDpmDb) {
            return DpmDbWriterFactory.dictionaryCreateWriter(
                outputDbPath = requiredOption(detectedOptions.output),
                forceOverwrite = detectedOptions.forceOverwrite,
                diagnosticContext = diagnosticBridge
            )
        }

        if (detectedOptions.cmdReplaceDictionaryInDpmDb) {
            detectedOptions.ensureBaselineDpmDbGiven()

            return DpmDbWriterFactory.dictionaryReplaceWriter(
                baselineDbPath = requiredOption(detectedOptions.baselineDb),
                outputDbPath = requiredOption(detectedOptions.output),
                forceOverwrite = detectedOptions.forceOverwrite,
                diagnosticContext = diagnosticBridge
            )
        }

        thisShouldNeverHappen("No suitable DB writer given")
    }

    private fun <T : Any> requiredOption(value: T?): T {
        return value ?: thisShouldNeverHappen("Command precondition mismatch.")
    }
}
