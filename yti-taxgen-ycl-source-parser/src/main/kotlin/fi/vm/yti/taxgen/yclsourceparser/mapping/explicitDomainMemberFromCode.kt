package fi.vm.yti.taxgen.yclsourceparser.mapping

import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomainMember
import fi.vm.yti.taxgen.yclsourceparser.model.YclCode

fun explicitDomainMemberFromYclCode(yclCode: YclCode): ExplicitDomainMember {
    val concept = conceptFromYclCode(yclCode)
    val memberCode = yclCode.codeValue!!

    return ExplicitDomainMember(
        concept = concept,
        memberCode = memberCode,
        defaultMember = true
    )
}
