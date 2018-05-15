package fi.vm.yti.taxgen.yclsourceparser.mapping

import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomainMember
import fi.vm.yti.taxgen.yclsourceparser.model.Code

fun explicitDomainMemberFromCode(code: Code): ExplicitDomainMember {
    val concept = conceptFromCode(code)
    val memberCode = code.codeValue!!

    return ExplicitDomainMember(
        concept = concept,
        memberCode = memberCode
    )
}
