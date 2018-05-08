package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice.config

import fi.vm.yti.taxgen.datapointmetamodel.Owner

data class YclTaxonomyUnitConfig(
    val owner: Owner,
    val codeLists: List<YclCodeListConfig>
)
