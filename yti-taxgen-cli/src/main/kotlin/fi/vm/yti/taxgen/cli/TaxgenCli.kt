package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.FailException
import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.commons.throwHalt
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
        diagnosticContext.withContext(
            contextType = DiagnosticContextType.CmdWriteDictionariesToDpmDb
        ) {
            detectedOptions.ensureSingleSourceGiven()
            detectedOptions.ensureOutputGiven()

            val sourceHolder = resolveSource(detectedOptions)

            val dpmModel = sourceHolder.use {
                val dpmMapper = RdsToDpmMapper(diagnosticContext)
                dpmMapper.extractDpmModelFromSource(it)
            }

            diagnosticContext.haltIfUnrecoverableErrors {
                "Mapping failed due content errors"
            }

            val dbWriter = resolveDpmDbWriter(detectedOptions)
            dbWriter.writeModel(dpmModel)
        }
    }

    private fun captureDpmSources(detectedOptions: DetectedOptions) {
        diagnosticContext.withContext(
            contextType = DiagnosticContextType.CmdCaptureDpmSources
        ) {
            detectedOptions.ensureSingleSourceGiven()
            detectedOptions.ensureOutputGiven()

            val sourceHolder = resolveSource(detectedOptions)
            val recorder = resolveSourceRecorder(detectedOptions)

            recorder.use {
                it.captureSources(sourceHolder)
            }
        }

        diagnosticContext.haltIfUnrecoverableErrors {
            "Capturing failed"
        }
    }

    private fun resolveSource(detectedOptions: DetectedOptions): SourceHolder {
        if (detectedOptions.sourceConfigFile != null) {
            return SourceFactory.sourceForConfigFile(
                configFilePath = detectedOptions.sourceConfigFile,
                diagnosticContext = diagnosticContext
            )
        }

        if (detectedOptions.sourceFolder != null) {
            return SourceFactory.sourceForFolder(
                sourceRootPath = detectedOptions.sourceFolder,
                diagnosticContext = diagnosticContext
            )
        }

        if (detectedOptions.sourceZipFile != null) {
            return SourceFactory.sourceForZipFile(
                zipFilePath = detectedOptions.sourceZipFile,
                diagnosticContext = diagnosticContext
            )
        }

        thisShouldNeverHappen("No suitable source given")
    }

    private fun resolveSourceRecorder(
        detectedOptions: DetectedOptions
    ): DpmSourceRecorder {

        if (detectedOptions.cmdCaptureDpmSourcesToFolder) {
            return SourceFactory.folderRecorder(
                outputFolderPath = detectedOptions.output!!,
                forceOverwrite = detectedOptions.forceOverwrite,
                diagnosticContext = diagnosticContext
            )
        }

        if (detectedOptions.cmdCaptureDpmSourcesToZip) {
            return SourceFactory.zipRecorder(
                outputZipPath = detectedOptions.output!!,
                forceOverwrite = detectedOptions.forceOverwrite,
                diagnosticContext = diagnosticContext
            )
        }

        thisShouldNeverHappen("No suitable source recorder given")
    }

    private fun resolveDpmDbWriter(
        detectedOptions: DetectedOptions
    ): DpmDbWriter {

        if (detectedOptions.cmdCreateDictionaryToNewDpmDb) {
            return DpmDbWriterFactory.dictionaryCreateWriter(
                outputDbPath = detectedOptions.output!!,
                forceOverwrite = detectedOptions.forceOverwrite,
                diagnosticContext = diagnosticContext
            )
        }

        if (detectedOptions.cmdReplaceDictionaryInDpmDb) {
            detectedOptions.ensureBaselineDpmDbGiven()

            return DpmDbWriterFactory.dictionaryReplaceWriter(
                baselineDbPath = detectedOptions.baselineDb!!,
                outputDbPath = detectedOptions.output!!,
                forceOverwrite = detectedOptions.forceOverwrite,
                diagnosticContext = diagnosticContext
            )
        }

        thisShouldNeverHappen("No suitable DB writer given")
    }
}
