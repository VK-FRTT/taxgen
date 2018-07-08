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
        val capturePath = fixtureSource.fixturePath("ycl_source_capture", fixtureName)

        capturePath ?: thisShouldNeverHappen("Could not resolve YCL source capture fixture $fixtureName")
        if (!Files.exists(capturePath)) thisShouldNeverHappen("Resolved YCL source capture fixture $capturePath does not exist")

        return capturePath
    }

    fun yclSourceConfigPath(fixtureName: String): Path {
        val configPath = fixtureSource.fixturePath("ycl_source_config", "$fixtureName.json")

        configPath ?: thisShouldNeverHappen("Could not resolve YCL config fixture $fixtureName")
        if (!Files.exists(configPath)) thisShouldNeverHappen("Resolved YCL source capture fixture $configPath does not exist")

        return configPath
    }
}
