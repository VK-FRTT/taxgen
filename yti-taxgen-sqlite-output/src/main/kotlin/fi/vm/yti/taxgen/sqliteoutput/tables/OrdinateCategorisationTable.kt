package fi.vm.yti.taxgen.sqliteoutput.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mOrdinateCategorisation` (
 *   `OrdinateID` INTEGER,
 *   `DimensionID` INTEGER,
 *   `MemberID` INTEGER,
 *   `DimensionMemberSignature` TEXT,
 *   `Source` Text,
 *   `DPS` TEXT,
 *   FOREIGN KEY(`OrdinateID`) REFERENCES `mAxisOrdinate`(`OrdinateID`),
 *   FOREIGN KEY(`MemberID`) REFERENCES `mMember`(`MemberID`),
 *   FOREIGN KEY(`DimensionID`) REFERENCES `mDimension`(`DimensionID`),
 *   PRIMARY KEY(`OrdinateID`,`DimensionID`)
 * );
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - None
 */
object OrdinateCategorisationTable : Table("mOrdinateCategorisation") {

    val ordinateIdCol: Column<EntityID<Int>?> = reference(
        name = "OrdinateID",
        foreign = AxisOrdinateTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    val dimensionIdCol: Column<EntityID<Int>?> = reference(
        name = "DimensionID",
        foreign = DimensionTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    val memberIdCol: Column<EntityID<Int>?> = reference(
        name = "MemberID",
        foreign = MemberTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    val dimensionMemberSignatureCol: Column<String?> = text("DimensionMemberSignature").nullable()

    val sourceCol = text("Source").nullable()

    val dpsCol = text("DPS").nullable()

    override val primaryKey = PrimaryKey(ordinateIdCol, dimensionIdCol)

    fun insertOrdinateCategorisation(
        ordinateId: EntityID<Int>?,
        dimensionId: EntityID<Int>?,
        memberId: EntityID<Int>?,
        dimensionMemberSignature: String?,
        source: String?,
        dps: String?
    ) {
        OrdinateCategorisationTable.insert {
            it[ordinateIdCol] = ordinateId
            it[dimensionIdCol] = dimensionId
            it[memberIdCol] = memberId
            it[dimensionMemberSignatureCol] = dimensionMemberSignature
            it[sourceCol] = source
            it[dpsCol] = dps
        }
    }
}
