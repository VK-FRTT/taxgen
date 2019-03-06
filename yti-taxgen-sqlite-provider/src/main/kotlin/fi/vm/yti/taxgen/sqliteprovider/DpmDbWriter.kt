package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.dpmmodel.DpmModel

interface DpmDbWriter {

    fun writeModel(dpmModel: DpmModel)
}
