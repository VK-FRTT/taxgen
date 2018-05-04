package fi.vm.yti.taxgen.cli.yclsourceconfig

data class CodeListGroupConf(
    val owner: OwnerConf,
    val codeLists: List<CodeListConf>
)
