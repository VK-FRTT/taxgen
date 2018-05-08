package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice.config.YclCodeListConfig

class YclServiceCodeListsIterator(
    codeListConfigs: List<YclCodeListConfig>
) : AbstractIterator<CodeList>() {

    private val codeListConfigsIterator = codeListConfigs.iterator()

    override fun computeNext() {
        if (codeListConfigsIterator.hasNext()) {
            setNext(YclServiceCodeList(codeListConfigsIterator.next()))
        } else {
            done()
        }
    }
}
