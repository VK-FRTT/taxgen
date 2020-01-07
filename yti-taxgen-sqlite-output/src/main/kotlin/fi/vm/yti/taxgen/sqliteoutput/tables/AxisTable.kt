package fi.vm.yti.taxgen.sqliteoutput.tables

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mAxis` (
 *   `AxisID`	INTEGER,
 *   `AxisOrientation`	TEXT,
 *   `AxisLabel`	TEXT,
 *   `IsOpenAxis`	BOOLEAN,
 *   `ConceptID`	INTEGER,
 *   PRIMARY KEY(`AxisID`)
 * );
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - None
 */
object AxisTable : IntIdByRowIdTableBase(name = "mAxis", columnName = "AxisID") {

    val axisOrientationCol = text("AxisOrientation").nullable()

    val axisLabelCol = text("AxisLabel").nullable()

    val isOpenAxisCol = bool("IsOpenAxis").nullable()

    val conceptIdCol = integer("ConceptID").nullable() // Note: In DM database column type is plain integer instead reference
}
