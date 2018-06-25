package fi.vm.yti.taxgen.ycltodpmmapper

import fi.vm.yti.taxgen.yclsourceprovider.folder.YclSourceFolderStructureAdapter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Paths

@DisplayName("When YCL sources are parsed")
internal class YclSourceParser_UnitTest {

    private lateinit var yclSource: YclSourceFolderStructureAdapter

    @BeforeEach
    fun init() {
    }

    @AfterEach
    fun teardown() {
        yclSource.close()
    }

    @Test
    fun `Should succeed parsing single explicit domain`() {
        loadSourceBundle("single_explicit_domain")

        val parser = YclSourceParser()
        parser.parse(yclSource)
    }

    private fun loadSourceBundle(bundleName: String) {
        val classLoader = Thread.currentThread().contextClassLoader
        val resourceUri = classLoader.getResource("yclsourceparser_unittest/$bundleName").toURI()
        val resourcePath = Paths.get(resourceUri)

        yclSource = YclSourceFolderStructureAdapter(resourcePath)
    }
}
