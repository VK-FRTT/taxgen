package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.dpmmodel.Owner

data class OwnerHolder(
    val configData: String,
    val owner: Owner
)
