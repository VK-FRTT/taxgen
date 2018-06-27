package fi.vm.yti.taxgen.testcommons

import java.nio.file.Path
import java.nio.file.Paths

object TestFixtures {

    fun yclSourceCapturePath(name: String): Path {
        val resourceUri = classLoader().getResource("test_fixtures/ycl_source_capture/$name").toURI()
        return Paths.get(resourceUri)
    }

    fun yclSourceConfigPath(name: String): Path {
        val resourceUri = classLoader().getResource("test_fixtures/ycl_source_config/$name.json").toURI()
        return Paths.get(resourceUri)
    }

    private fun classLoader(): ClassLoader = Thread.currentThread().contextClassLoader
}
