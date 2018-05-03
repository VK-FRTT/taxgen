package fi.vm.yti.taxgen.yclsourceparser

import fi.vm.yti.taxgen.datapointmetamodel.InputParsingResult

class YclSourceParser {

    companion object {
        fun parseInput(yclInputContainer: String ): InputParsingResult? {
            println("YclSourceParser.parseInput => $yclInputContainer")

            val result = InputParsingResult()
            return result
        }
    }
}
