package fi.vm.yti.taxgen.yclinputparser

import fi.vm.yti.taxgen.datapointmetamodel.InputParsingResult

class YclInputParser {

    companion object {
        fun parseInput(yclInputContainer : String ): InputParsingResult? {
            println("YclInputParser.parseInput => $yclInputContainer")

            val result = InputParsingResult()
            return result
        }
    }
}