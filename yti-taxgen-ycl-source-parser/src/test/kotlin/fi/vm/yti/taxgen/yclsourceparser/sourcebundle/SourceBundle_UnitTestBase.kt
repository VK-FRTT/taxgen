package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions

open class SourceBundle_UnitTestBase {

    protected val objectMapper = jacksonObjectMapper()

    protected fun extractMarkerValuesFromJsonData(
        objects: List<Any>,
        jsonDataExtractor: (Any) -> String
    ): List<String> {
        val markers = objects.map { obj ->
            val jsonData = jsonDataExtractor(obj)
            val json = objectMapper.readTree(jsonData)
            Assertions.assertThat(json.isObject).isTrue()

            json.get("marker").textValue()
        }

        return markers
    }
}
