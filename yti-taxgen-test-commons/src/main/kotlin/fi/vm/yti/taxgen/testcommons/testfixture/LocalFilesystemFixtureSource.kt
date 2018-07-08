package fi.vm.yti.taxgen.testcommons.testfixture

import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths

internal class LocalFilesystemFixtureSource : FixtureSource {

    override fun fixturePath(fixtureType: String, fixtureName: String): Path? {
        val fixtureResource = contextClassLoader().getResource("test_fixtures/$fixtureType/$fixtureName") ?: return null

        val fixtureUri = fixtureResource.toURI()

        return Paths.get(fixtureUri)
    }

    private fun contextClassLoader(): ClassLoader = Thread.currentThread().contextClassLoader

    companion object {

        internal fun tryCreateForResource(resourceUrl: URL): FixtureSource? {
            if (resourceUrl.toString().startsWith("file:")) {
                return LocalFilesystemFixtureSource()
            }

            return null
        }
    }
}
