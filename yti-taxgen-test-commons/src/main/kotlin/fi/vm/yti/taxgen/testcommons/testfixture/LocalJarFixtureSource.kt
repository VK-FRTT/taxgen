package fi.vm.yti.taxgen.testcommons.testfixture

import java.net.URI
import java.net.URL
import java.nio.file.FileSystems
import java.nio.file.Path

internal class LocalJarFixtureSource(jarUri: URI) : FixtureSource {

    private val jarZipFs = FileSystems.newFileSystem(
        jarUri,
        emptyMap<String, String>()
    )

    override fun fixturePath(fixtureTypeFolder: String, fixtureName: String): Path {
        return jarZipFs.getPath("test_fixtures/$fixtureTypeFolder/$fixtureName")
    }

    companion object {

        fun tryCreateForResource(resourceUrl: URL): FixtureSource? {
            val resourceString = resourceUrl.toString()

            if (resourceString.startsWith("jar:file:")) {
                val jarUri = URI.create(resourceString.substringBefore("!"))
                return LocalJarFixtureSource(jarUri)
            }

            return null
        }
    }
}
