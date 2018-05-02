package fi.vm.yti.taxgen.cli

import java.nio.charset.Charset
import kotlin.system.exitProcess

/*
* Stand-alone command line application for executing taxonomy generation from console.
*
* Running: java -jar yti-taxgen-cli-<Version_identifier>.jar <Options>
*
* Where options are:
* --ycl-taxonomy-config <Filename>                    Config file describing XBRL taxonomy source set.
*                                                     Links to relevant CodeSchemes from YTI Codelist tool.
*
* --output_folder <Path>
*
*
* --output_zip <Path>
*
*/
fun main(args: Array<String>) {
    /**
     * - Parse command line options
     *
     * - Parse given ycl-taxonomy-config
     *      - List of SourceSets
     *          - DPM Owner details
     *          - List of CodeSchemeApiUrls (https://koodistot-dev.suomi.fi/codelist-api/api/v1/coderegistries/yti-xbrl/codeschemes/XBRL1)
     *          - List of CodeSchemeUris (http://uri.suomi.fi/codelist/yti-xbrl/XBRL1)
     *
     * - For each CodeScheme pointed in config, fetch required contents from YTI Codelist -service, in JSON format
     *      - CodeScheme
     *      - Codes
     *      - Extensions: partial hierarchies, tags
     *
     * - Compose input data container (ycl-input)
     *      - A ZIP file containing Owner metadata + fetched CodeScheme contents
     *      - Directory structure:
     *          .
     *          +-- owner_01
     *          |   +-- owner-metadata.json
     *          |   +-- codescheme_01
     *          |   |   +-- codescheme.json
     *          |   |   +-- codes.json
     *          |   |   +-- partial_hierarchy_01
     *          |   +-- codescheme_02
     *          |       +-- codescheme.json
     *          |       +-- codes.json
     *          |       +-- extensions??
     *          +-- owner_01
     *              +-- owner-metadata.json
     *              +-- codescheme_01
     *                  +-- codescheme.json
     *                  +-- codes.json
     *                  +-- extensions
     *
     */

    val status = TaxgenCli(
        System.out,
        System.err,
        Charset.defaultCharset(),
        DefinedOptions()
    ).use { cli ->
        cli.execute(args)
    }

    exitProcess(status)
}
/*
private fun sampleFetchRequest() {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://koodistot-dev.suomi.fi/codelist-api/api/v1/coderegistries/yti-xbrl/codeschemes/XBRL1/codes/")
        .build()

    val response = client.newCall(request).execute()

    if (!response.isSuccessful) {
        exitProcess(1)
    }

    val responseHeaders = response.headers()

    for (i in 0 until responseHeaders.size()) {
        System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i))
    }

    System.out.println(response.body()?.string())
}
*/
