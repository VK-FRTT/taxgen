package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

import com.fasterxml.jackson.annotation.JsonFormat

data class BundleInfo(
    val sourceBundleType: String,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    val createdAt: String
)
