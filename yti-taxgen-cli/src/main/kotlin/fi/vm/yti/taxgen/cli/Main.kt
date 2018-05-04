package fi.vm.yti.taxgen.cli

import java.nio.charset.Charset
import kotlin.system.exitProcess

/*
* Stand-alone command line application for executing taxonomy generation from console.
*/
fun main(args: Array<String>) {
    val status = TaxgenCli(
        System.out,
        System.err,
        Charset.defaultCharset(),
        DefinedOptions()
    ).use { cli ->
        cli.execute(args)
    }

    exitProcess(status)
}
