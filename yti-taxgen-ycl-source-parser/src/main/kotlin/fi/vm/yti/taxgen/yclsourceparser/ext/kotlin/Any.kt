package fi.vm.yti.taxgen.yclsourceparser.ext.kotlin

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun Any.toJsonString(): String {
    return jacksonObjectMapper().writeValueAsString(this)
}
