package fi.vm.yti.taxgen.testcommons

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.testcommons.testfixture.FixtureSource
import fi.vm.yti.taxgen.testcommons.testfixture.LocalFilesystemFixtureSource
import fi.vm.yti.taxgen.testcommons.testfixture.LocalJarFixtureSource
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

object TestFixture {

    private val fixtureSource = createFixtureSource()

    private fun createFixtureSource(): FixtureSource {
        val selfResourceUrl = selfResourceUrl()

        LocalJarFixtureSource.tryCreateForResource(selfResourceUrl)?.let { return it }

        LocalFilesystemFixtureSource.tryCreateForResource(selfResourceUrl)?.let { return it }

        thisShouldNeverHappen("No suitable fixture source for resource: $selfResourceUrl")
    }

    private fun selfResourceUrl(): URL {
        val selfResourceName = "/${javaClass.name.replace('.', '/')}.class"
        return javaClass.getResource(selfResourceName)
    }

    fun yclSourceCapturePath(fixtureName: String): Path {
        val path = fixtureSource.fixturePath("ycl_source_capture", fixtureName)
        ensurePathExists(path, "YCL source capture", fixtureName)
        return path!!
    }

    fun yclSourceConfigPath(fixtureName: String): Path {
        val path = fixtureSource.fixturePath("ycl_source_config", "$fixtureName.json")
        ensurePathExists(path, "YCL source config", fixtureName)
        return path!!
    }

    fun dpmLanguageConfigPath(fixtureName: String): Path {
        val path = fixtureSource.fixturePath("dpm_language_config", "$fixtureName.json")
        ensurePathExists(path, "DPM language config", fixtureName)
        return path!!
    }

    private fun ensurePathExists(path: Path?, fixtureType: String, fixtureName: String) {
        if (path == null) thisShouldNeverHappen("$fixtureType: Could not resolve fixture $fixtureName")
        if (!Files.exists(path)) thisShouldNeverHappen("$fixtureType: Resolved fixture $path does not exist")
    }
}
