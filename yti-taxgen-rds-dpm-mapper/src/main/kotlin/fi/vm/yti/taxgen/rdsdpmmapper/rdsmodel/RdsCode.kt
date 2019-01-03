package fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel

import java.time.Instant
import java.time.LocalDate

internal data class RdsCode(
    override val id: String?,
    override val uri: String?,
    override val codeValue: String?,

    override val created: Instant?,
    override val modified: Instant?,
    override val startDate: LocalDate?,
    override val endDate: LocalDate?,

    override val prefLabel: Map<String, String>?,
    override val description: Map<String, String>?,

    val subCodeList: RdsCodeListMeta?
) : RdsEntity()
