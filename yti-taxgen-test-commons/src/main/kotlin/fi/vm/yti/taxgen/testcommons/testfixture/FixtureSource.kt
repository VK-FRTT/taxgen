package fi.vm.yti.taxgen.testcommons.testfixture

import java.nio.file.Path

internal interface FixtureSource {
    fun fixturePath(fixtureTypeFolder: String, fixtureName: String): Path?
}
