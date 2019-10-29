package fi.vm.yti.taxgen.dpmmodel.diagnostic

class DiagnosticContextDetailsData private constructor(
    private val contextTitle: String,
    private val contextIdentifier: String
) : DiagnosticContextDetails {

    companion object {
        fun withContextTitle(contextTitle: String): DiagnosticContextDetailsData {
            return DiagnosticContextDetailsData(contextTitle, "")
        }

        fun withContextIdentifier(contextIdentifier: String): DiagnosticContextDetailsData {
            return DiagnosticContextDetailsData("", contextIdentifier)
        }

        fun withContextTitleAndIdentifier(
            contextTitle: String,
            contextIdentifier: String
        ): DiagnosticContextDetailsData {
            return DiagnosticContextDetailsData(contextTitle, contextIdentifier)
        }
    }

    override fun contextTitle(): String = contextTitle
    override fun contextIdentifier(): String = contextIdentifier
}
