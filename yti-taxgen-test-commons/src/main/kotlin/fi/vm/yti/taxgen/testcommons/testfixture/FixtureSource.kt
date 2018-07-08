package fi.vm.yti.taxgen.testcommons.testfixture

import java.nio.file.Path

internal interface FixtureSource {
    fun fixturePath(fixtureType: String, fixtureName: String): Path?
}
