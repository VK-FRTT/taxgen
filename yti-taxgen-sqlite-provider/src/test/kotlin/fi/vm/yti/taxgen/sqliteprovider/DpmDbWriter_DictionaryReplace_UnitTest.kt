package fi.vm.yti.taxgen.sqliteprovider

import org.junit.jupiter.api.DisplayName

@DisplayName("SQLite DPM DB: dictionary replace")
internal class DpmDbWriter_DictionaryReplace_UnitTest() {

    //TODO tests:
    // - Input DB without eurofiling owner
    // - Input DB without target owner
    // - Input DB with valid ordinate categorisations: normal + open members
    // - Input DB with invalid ordinate categorisations: broken signature, signature referring missing member, signature referring missing dimension
}
