package fi.vm.yti.taxgen.sqliteprovider.tables

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

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
object AxisTable : IntIdTable(name = "mAxis", columnName = "AxisID") {
    val domainIdCol = reference("DomainID", DomainTable, ReferenceOption.NO_ACTION).nullable()
    val axisOrientationCol = text("AxisOrientation").nullable()
    val axisLabelCol = text("AxisLabel").nullable()
    val isOpenAxisCol = bool("IsOpenAxis").nullable()
    val conceptIdCol = reference("ConceptID", ConceptTable, ReferenceOption.NO_ACTION).nullable()
}
