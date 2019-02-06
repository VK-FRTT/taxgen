package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.dpmmodel.DpmDictionary

interface DpmDbWriter {

    fun writeWithDictionaries(dpmDictionaries: List<DpmDictionary>)
}
