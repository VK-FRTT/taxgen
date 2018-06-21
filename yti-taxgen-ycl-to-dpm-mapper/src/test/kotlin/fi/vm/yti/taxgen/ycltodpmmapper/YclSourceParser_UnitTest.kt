package fi.vm.yti.taxgen.ycltodpmmapper

import fi.vm.yti.taxgen.yclsourceprovider.folder.FolderSourceBundle
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Paths

@DisplayName("When YCL sources are parsed")
internal class YclSourceParser_UnitTest {

    private lateinit var sourceBundle: FolderSourceBundle

    @BeforeEach
    fun init() {
    }

    @AfterEach
    fun teardown() {
        sourceBundle.close()
    }

    @Test
    fun `Should succeed parsing single explicit domain`() {
        loadSourceBundle("single_explicit_domain")

        val parser = YclSourceParser()
        parser.parse(sourceBundle)
    }

    private fun loadSourceBundle(bundleName: String) {
        val classLoader = Thread.currentThread().contextClassLoader
        val resourceUri = classLoader.getResource("yclsourceparser_unittest/$bundleName").toURI()
        val resourcePath = Paths.get(resourceUri)

        sourceBundle = FolderSourceBundle(resourcePath)
    }
}
