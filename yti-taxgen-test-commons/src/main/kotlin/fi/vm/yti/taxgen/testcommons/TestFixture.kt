package fi.vm.yti.taxgen.testcommons

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.testcommons.testfixture.FixtureSource
import fi.vm.yti.taxgen.testcommons.testfixture.LocalFilesystemFixtureSource
import fi.vm.yti.taxgen.testcommons.testfixture.LocalJarFixtureSource
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

object TestFixture {

    enum class Type(val folderName: String) {
        RDS_CAPTURE("rds_capture"),
        RDS_SOURCE_CONFIG("rds_source_config"),
        DPM_LANGUAGE_CONFIG("dpm_language_config")
    }

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

    fun pathOf(fixtureType: Type, fixtureName: String): Path {
        val path = fixtureSource.fixturePath(
            fixtureType.folderName,
            fixtureName
        )

        path ?: thisShouldNeverHappen("$fixtureType: Could not resolve fixture $fixtureName")
        if (!Files.exists(path)) thisShouldNeverHappen("$fixtureType: Resolved fixture $path does not exist")

        return path
    }
}
