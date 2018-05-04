package fi.vm.yti.taxgen.cli.yclsourcebundler

import fi.vm.yti.taxgen.cli.yclsourceconfig.CodeListConf
import fi.vm.yti.taxgen.cli.yclsourceconfig.CodeListGroupConf
import fi.vm.yti.taxgen.cli.yclsourceconfig.OwnerConf
import fi.vm.yti.taxgen.cli.yclsourceconfig.YclSourceConfig
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeListData
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeListGroupData
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.OwnerData
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.YclSourceBundle
import okhttp3.OkHttpClient
import okhttp3.Request

fun YclSourceConfig.toSourceBundle(): YclSourceBundle {
    return YclSourceBundle(
        codeListGroups = codeListGroups.map { it.toSourceBundle() }
    )
}

fun CodeListGroupConf.toSourceBundle(): CodeListGroupData {
    return CodeListGroupData(
        owner = owner.toSourceBundle(),
        codeLists = codeLists.map { it.toSourceBundle() }
    )
}

fun OwnerConf.toSourceBundle(): OwnerData {
    return OwnerData(
        namespace = namespace,
        namespacePrefix = namespacePrefix,
        officialLocation = officialLocation,
        copyrightText = copyrightText,
        supportedLanguages = supportedLanguages
    )
}

fun CodeListConf.toSourceBundle(): CodeListData {
    val metadata = fetchJson(uri)
    //val codeListUrls = codeListUrls(metadataJson)
    //val codes = fetchJson(codeListUrls.codesUrl)

    return CodeListData(
        metadataJson = metadata,
        codesJson = "codes"
    )
}

fun fetchJson(url: String): String {
    val client = OkHttpClient().newBuilder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    val request = Request.Builder()
        .get()
        .header("accept", "application/json")
        .url(url)
        .build()

    val response = client.newCall(request).execute()

    //TODO - error handling

    return response.body().use {
        it!!.string()
    }
}
