package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import java.nio.file.Path

interface DpmDbWriter {

    fun writeModel(
        dpmModel: DpmModel,
        processingOptions: ProcessingOptions
    )

    fun outputPath(): Path
}
