package fi.vm.yti.taxgen.sqliteoutput

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import java.sql.ResultSet
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class DpmDbWriter_DictionaryReplace_OrdinateCategorisationTransform_ModuleTest :
    DpmDbWriter_DictionaryReplaceModuleTestBase() {

    val expDimXbrlCode = "FixPrfx_dim:ExpDim-1-Code"
    val expDimIdBaseline = "5" // Parent domain ID: 5
    val expDimIdOutput = "1"

    val typDimXbrlCode = "FixPrfx_dim:TypDim-1-Code"
    val typDimIdBaseline = "8"
    val typDimIdOutput = "4"

    val memberXbrlCode = "FixPrfx_ExpDom-1-Code:Mbr-2-Code"
    val memberIdBaseline = "20"
    val memberIdOutput = "2"

    val openMemberMarker = "*"
    val openMemberFixedId = "9999"

    val defaultMemberInclusionMarker = "?"

    val hierarchyCode = "ExpDomHier-2-Code"
    val hierarchyIdBaseline = "11"
    val hierarchyIdOutput = "2"

    val hierarchyStartMemberCode = "Mbr-4-Code"
    val hierarchyStartMemberIdBaseline = "22"
    val hierarchyStartMemberIdOutput = "4"

    @Nested
    inner class TransformationSuccessCases {

        @Test
        fun `ordinate categorisation referring explicit dimension and explicit domain member should get updated`() {
            val updateStatement =
                """
                INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
                VALUES (111, $expDimIdBaseline, $memberIdBaseline, "$expDimXbrlCode($memberXbrlCode)", "source", "$expDimXbrlCode($memberXbrlCode)")
                """.trimIndent()

            val expectedCategorisations = arrayOf(
                "#OrdinateID, #DimensionID, #MemberID, #DimensionMemberSignature, #Source, #DPS",
                "111, $expDimIdOutput, $memberIdOutput, $expDimXbrlCode($memberXbrlCode), source, $expDimXbrlCode($memberXbrlCode)"
            )

            updateBaseline_ExecuteDictionaryReplace_VerifyResult(
                updateStatement = updateStatement,
                expectedCategorisations = expectedCategorisations
            )
        }

        @Test
        fun `ordinate categorisation referring typed dimension and open member should get updated`() {
            val updateStatement =
                """
                INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
                VALUES (111, $typDimIdBaseline, 3333, "$typDimXbrlCode($openMemberMarker)", "source", "$typDimXbrlCode($openMemberMarker)")
                """.trimIndent()

            val expectedCategorisations = arrayOf(
                "#OrdinateID, #DimensionID, #MemberID, #DimensionMemberSignature, #Source, #DPS",
                "111, $typDimIdOutput, $openMemberFixedId, $typDimXbrlCode($openMemberMarker), source, $typDimXbrlCode($openMemberMarker)"
            )

            updateBaseline_ExecuteDictionaryReplace_VerifyResult(
                updateStatement = updateStatement,
                expectedCategorisations = expectedCategorisations
            )
        }

        @Test
        fun `ordinate categorisation having full OpenAxisValueRestriction should get updated`() {
            val updateStatement =
                """
                INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
                VALUES (111, $expDimIdBaseline, 3333, "$expDimXbrlCode($openMemberMarker[$hierarchyIdBaseline;$hierarchyStartMemberIdBaseline;0])", "source", "$expDimXbrlCode($openMemberMarker[$hierarchyCode;$hierarchyStartMemberCode;0])")
                """.trimIndent()

            val expectedCategorisations = arrayOf(
                "#OrdinateID, #DimensionID, #MemberID, #DimensionMemberSignature, #Source, #DPS",
                "111, $expDimIdOutput, $openMemberFixedId, $expDimXbrlCode($openMemberMarker[$hierarchyIdOutput;$hierarchyStartMemberIdOutput;0]), source, $expDimXbrlCode($openMemberMarker[$hierarchyCode;$hierarchyStartMemberCode;0])"
            )

            updateBaseline_ExecuteDictionaryReplace_VerifyResult(
                updateStatement = updateStatement,
                expectedCategorisations = expectedCategorisations
            )
        }

        @Test
        fun `ordinate categorisation having full OpenAxisValueRestriction and DefaultMemberInclusion marker should get updated`() {
            val updateStatement =
                """
                INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
                VALUES (111, $expDimIdBaseline, 3333, "$expDimXbrlCode($openMemberMarker$defaultMemberInclusionMarker[$hierarchyIdBaseline;$hierarchyStartMemberIdBaseline;0])", "source", "$expDimXbrlCode($openMemberMarker$defaultMemberInclusionMarker[$hierarchyCode;$hierarchyStartMemberCode;0])")
                """.trimIndent()

            val expectedCategorisations = arrayOf(
                "#OrdinateID, #DimensionID, #MemberID, #DimensionMemberSignature, #Source, #DPS",
                "111, $expDimIdOutput, $openMemberFixedId, $expDimXbrlCode($openMemberMarker$defaultMemberInclusionMarker[$hierarchyIdOutput;$hierarchyStartMemberIdOutput;0]), source, $expDimXbrlCode($openMemberMarker$defaultMemberInclusionMarker[$hierarchyCode;$hierarchyStartMemberCode;0])"
            )

            updateBaseline_ExecuteDictionaryReplace_VerifyResult(
                updateStatement = updateStatement,
                expectedCategorisations = expectedCategorisations
            )
        }

        @Test
        fun `ordinate categorisation having partial OpenAxisValueRestriction should get updated`() {
            val updateStatement =
                """
                INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
                VALUES (111, $expDimIdBaseline, 3333, "$expDimXbrlCode($openMemberMarker[$hierarchyIdBaseline])", "source", "$expDimXbrlCode($openMemberMarker[$hierarchyCode])")
                """.trimIndent()

            val expectedCategorisations = arrayOf(
                "#OrdinateID, #DimensionID, #MemberID, #DimensionMemberSignature, #Source, #DPS",
                "111, $expDimIdOutput, $openMemberFixedId, $expDimXbrlCode($openMemberMarker[$hierarchyIdOutput]), source, $expDimXbrlCode($openMemberMarker[$hierarchyCode])"
            )

            updateBaseline_ExecuteDictionaryReplace_VerifyResult(
                updateStatement = updateStatement,
                expectedCategorisations = expectedCategorisations
            )
        }

        @Test
        fun `ordinate categorisation having partial OpenAxisValueRestriction and DefaultMemberInclusion marker should get updated`() {
            val updateStatement =
                """
                INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
                VALUES (111, $expDimIdBaseline, 3333, "$expDimXbrlCode($openMemberMarker$defaultMemberInclusionMarker[$hierarchyIdBaseline])", "source", "$expDimXbrlCode($openMemberMarker$defaultMemberInclusionMarker[$hierarchyCode])")
                """.trimIndent()

            val expectedCategorisations = arrayOf(
                "#OrdinateID, #DimensionID, #MemberID, #DimensionMemberSignature, #Source, #DPS",
                "111, $expDimIdOutput, $openMemberFixedId, $expDimXbrlCode($openMemberMarker$defaultMemberInclusionMarker[$hierarchyIdOutput]), source, $expDimXbrlCode($openMemberMarker$defaultMemberInclusionMarker[$hierarchyCode])"
            )

            updateBaseline_ExecuteDictionaryReplace_VerifyResult(
                updateStatement = updateStatement,
                expectedCategorisations = expectedCategorisations
            )
        }

        private fun updateBaseline_ExecuteDictionaryReplace_VerifyResult(
            updateStatement: String,
            expectedCategorisations: Array<String>
        ) {
            baselineDbConnection.createStatement().executeUpdate(updateStatement)

            replaceDictionaryInDb(FixtureVariety.ONLY_ONE_DICTIONARY)

            assertThat(diagnosticCollector.events).containsExactly(
                "ENTER [SQLiteDbWriter] [Mode DictionaryReplace]",
                "ENTER [DpmModelProcessingOptionsTransform] []",
                "EXIT [DpmModelProcessingOptionsTransform]",
                "ENTER [FrameworksTransformCaptureBaseline] []",
                "EXIT [FrameworksTransformCaptureBaseline]",
                "ENTER [DpmDictionaryWrite] []",
                "EXIT [DpmDictionaryWrite]",
                "ENTER [FrameworksTransformUpdateEntities] [OrdinateCategorisations]",
                "EXIT [FrameworksTransformUpdateEntities]",
                "ENTER [FrameworksTransformUpdateEntities] [OpenAxisValueRestrictions]",
                "EXIT [FrameworksTransformUpdateEntities]",
                "ENTER [FrameworksTransformUpdateEntities] [TableCells]",
                "EXIT [FrameworksTransformUpdateEntities]",
                "EXIT [SQLiteDbWriter]"
            )

            val rs = readAllOrdinateCategorisations()
            assertThat(rs.toStringList()).containsExactlyInAnyOrder(*expectedCategorisations)
        }

        private fun readAllOrdinateCategorisations(): ResultSet {
            return outputDbConnection.createStatement().executeQuery(
                """
                SELECT * FROM mOrdinateCategorisation
                """.trimIndent()
            )
        }
    }

    @Nested
    inner class SignatureTransformation {

        private val openAxisValueRestrictionPattern =
            """
            \A
            (?<prefix>[^\[\]]+)
            \[
            (?<oavr>[^\[\]]+)
            \]
            (?<postifix>.*)
            \z
            """.trimIndent().toRegex(RegexOption.COMMENTS)

        private fun extractOpenAxisValueRestrictionElements(signature: String): List<String>? {
            val oavrMatch = openAxisValueRestrictionPattern.matchEntire(signature)
            oavrMatch ?: return null

            val oavr = (oavrMatch.groups as MatchNamedGroupCollection)["oavr"]?.value
            oavr ?: return null

            return oavr.split(";")
        }

        private fun sanityCheckOpenAxisValueRestrictionElements(
            signature: String,
            legalElements: List<String>,
            signatureName: String
        ) {
            val openAxisValueRestrictionElements = extractOpenAxisValueRestrictionElements(signature)
            openAxisValueRestrictionElements ?: return

            val illegalElements = openAxisValueRestrictionElements - legalElements

            require(illegalElements.isEmpty()) {
                "$signatureName has illegal OpenAxisValueRestriction elements: $illegalElements"
            }
        }

        private fun insertCategorisation(
            id: Int,
            dms: String,
            dps: String // OpenAxisValueRestriction part is based on element codes
        ) {
            // OpenAxisValueRestriction in DimensionMemberSignature is based on DB IDs
            sanityCheckOpenAxisValueRestrictionElements(
                dms,
                listOf(
                    hierarchyIdBaseline,
                    hierarchyStartMemberIdBaseline,
                    "0",
                    " ",
                    "4949",
                    "XYZ"
                ),
                "DimensionMemberSignature"
            )

            // OpenAxisValueRestriction in DPS is based on element codes
            sanityCheckOpenAxisValueRestrictionElements(
                dps,
                listOf(
                    hierarchyCode,
                    hierarchyStartMemberCode,
                    "0",
                    " ",
                    "NonExistingHierarchyCode",
                    "NonExistingMemberCode",
                    "XYZ"
                ),
                "DPS"
            )

            diagnosticCollector.reset()

            baselineDbConnection.createStatement().executeUpdate(
                """
                DELETE FROM mOrdinateCategorisation;

                INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
                VALUES ($id, 222, 333, "$dms", "source", "$dps")
                """.trimIndent()
            )
        }

        @Nested
        inner class SignatureStructureErrors {

            @Test
            fun `extra parentheses around member reference should cause fatal error`() {
                // DimensionMemberSignature
                insertCategorisation(
                    id = 111,
                    dms = "$expDimXbrlCode(($memberXbrlCode))",
                    dps = "$expDimXbrlCode($memberXbrlCode)"
                )
                ensureHaltThrown { replaceDictionaryInDb(exceptionIsExpected = true) }

                assertThat(diagnosticCollector.events).contains(
                    "MESSAGE [FATAL] [Unsupported signature in OrdinateCategorisation.DimensionMemberSignature: $expDimXbrlCode(($memberXbrlCode))]"
                )

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode($memberXbrlCode)",
                    dps = "$expDimXbrlCode(($memberXbrlCode))"
                )
                ensureHaltThrown { replaceDictionaryInDb(exceptionIsExpected = true) }

                assertThat(diagnosticCollector.events).contains(
                    "MESSAGE [FATAL] [Unsupported signature in OrdinateCategorisation.DPS: $expDimXbrlCode(($memberXbrlCode))]"
                )
            }

            @Test
            fun `extra trailing part should cause fatal error`() {
                // DimensionMemberSignature
                insertCategorisation(
                    id = 111,
                    dms = "$expDimXbrlCode($memberXbrlCode)(foo)",
                    dps = "$expDimXbrlCode($memberXbrlCode)"
                )
                ensureHaltThrown { replaceDictionaryInDb(exceptionIsExpected = true) }

                assertThat(diagnosticCollector.events).contains(
                    "MESSAGE [FATAL] [Unsupported signature in OrdinateCategorisation.DimensionMemberSignature: $expDimXbrlCode($memberXbrlCode)(foo)]"
                )

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode($memberXbrlCode)",
                    dps = "$expDimXbrlCode($memberXbrlCode)(foo)"
                )
                ensureHaltThrown { replaceDictionaryInDb(exceptionIsExpected = true) }

                assertThat(diagnosticCollector.events).contains(
                    "MESSAGE [FATAL] [Unsupported signature in OrdinateCategorisation.DPS: $expDimXbrlCode($memberXbrlCode)(foo)]"
                )
            }

            @Test
            fun `blank dimension value should cause validation failure`() {
                // DimensionMemberSignature
                insertCategorisation(
                    id = 111,
                    dms = " ($memberXbrlCode)",
                    dps = "$expDimXbrlCode($memberXbrlCode)"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 111] [DatabaseIdSignature.DimensionIdentifier] [Value is blank]",
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 111] [DimensionMemberSignature, DPS] [Signatures do not match] [Dimensions not same, DB Dimension identifier ` ´ XBRL Dimension identifier `FixPrfx_dim:ExpDim-1-Code´]"
                )

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode($memberXbrlCode)",
                    dps = " ($memberXbrlCode)"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 112] [XbrlCodeSignature.DimensionIdentifier] [Value is blank]",
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 112] [DimensionMemberSignature, DPS] [Signatures do not match] [Dimensions not same, DB Dimension identifier `FixPrfx_dim:ExpDim-1-Code´ XBRL Dimension identifier ` ´]"
                )
            }

            @Test
            fun `blank member value should cause validation failure`() {

                // DimensionMemberSignature
                insertCategorisation(
                    id = 111,
                    dms = "$expDimXbrlCode( )",
                    dps = "$expDimXbrlCode($memberXbrlCode)"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 111] [DatabaseIdSignature.MemberIdentifier] [Value is blank]",
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 111] [DimensionMemberSignature, DPS] [Signatures do not match] [Members not same, DB Member identifier ` ´ XBRL Member identifier `FixPrfx_ExpDom-1-Code:Mbr-2-Code´]"
                )

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode($memberXbrlCode)",
                    dps = "$expDimXbrlCode( )"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 112] [XbrlCodeSignature.MemberIdentifier] [Value is blank]",
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 112] [DimensionMemberSignature, DPS] [Signatures do not match] [Members not same, DB Member identifier `FixPrfx_ExpDom-1-Code:Mbr-2-Code´ XBRL Member identifier ` ´]"
                )
            }

            @Test
            fun `blank hierarchy value in full OpenAxisValueRestriction should cause validation failure`() {
                // DimensionMemberSignature
                insertCategorisation(
                    id = 111,
                    dms = "$expDimXbrlCode(*[ ;$hierarchyStartMemberIdBaseline;0])",
                    dps = "$expDimXbrlCode(*[$hierarchyCode;$hierarchyStartMemberCode;0])"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 111] [DatabaseIdSignature.HierarchyIdentifier] [Value is blank]",
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 111] [DimensionMemberSignature, DPS] [Signatures do not match] [Hierarchies not same, DB Hierarchy identifier `null´ XBRL Hierarchy identifier `ExpDomHier-2-Code´]"
                )

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode(*[$hierarchyIdBaseline;$hierarchyStartMemberIdBaseline;0])",
                    dps = "$expDimXbrlCode(*[ ;$hierarchyStartMemberCode;0])"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 112] [XbrlCodeSignature.HierarchyIdentifier] [Value is blank]",
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 112] [DimensionMemberSignature, DPS] [Signatures do not match] [Hierarchies not same, DB Hierarchy identifier `ExpDomHier-2-Code´ XBRL Hierarchy identifier ` ´]"
                )
            }

            @Test
            fun `blank hierarchy starting member value in full OpenAxisValueRestriction should cause validation failure`() {
                // DimensionMemberSignature
                insertCategorisation(
                    id = 111,
                    dms = "$expDimXbrlCode(*[$hierarchyIdBaseline; ;0])",
                    dps = "$expDimXbrlCode(*[$hierarchyCode;$hierarchyStartMemberCode;0])"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 111] [DatabaseIdSignature.HierarchyStartingMemberIdentifier] [Value is blank]",
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 111] [DimensionMemberSignature, DPS] [Signatures do not match] [Hierarchy starting Members not same, DB Member identifier `null´ XBRL Member identifier `Mbr-4-Code´]"
                )

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode(*[$hierarchyIdBaseline;$hierarchyStartMemberIdBaseline;0])",
                    dps = "$expDimXbrlCode(*[$hierarchyCode; ;0])"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 112] [XbrlCodeSignature.HierarchyStartingMemberIdentifier] [Value is blank]",
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 112] [DimensionMemberSignature, DPS] [Signatures do not match] [Hierarchy starting Members not same, DB Member identifier `Mbr-4-Code´ XBRL Member identifier ` ´]"
                )
            }

            @Test
            fun `blank starting member inclusion marker in full OpenAxisValueRestriction should cause validation failure`() {
                // DimensionMemberSignature
                insertCategorisation(
                    id = 111,
                    dms = "$expDimXbrlCode(*[$hierarchyIdBaseline;$hierarchyStartMemberIdBaseline; ])",
                    dps = "$expDimXbrlCode(*[$hierarchyCode;$hierarchyStartMemberCode;0])"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 111] [DatabaseIdSignature.IsStartingMemberIncluded] [Value is blank]",
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 111] [DatabaseIdSignature.IsStartingMemberIncluded] [Unsupported value] [ ]",
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 111] [DimensionMemberSignature, DPS] [Signatures do not match] [Starting member inclusion not same, DB inclusion ` ´ XBRL inclusion `0´]"
                )

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode(*[$hierarchyIdBaseline;$hierarchyStartMemberIdBaseline;0])",
                    dps = "$expDimXbrlCode(*[$hierarchyCode;$hierarchyStartMemberCode; ])"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 112] [XbrlCodeSignature.IsStartingMemberIncluded] [Value is blank]",
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 112] [XbrlCodeSignature.IsStartingMemberIncluded] [Unsupported value] [ ]",
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 112] [DimensionMemberSignature, DPS] [Signatures do not match] [Starting member inclusion not same, DB inclusion `0´ XBRL inclusion ` ´]"
                )
            }

            @Test
            fun `blank hierarchy value in partial OpenAxisValueRestriction should cause validation failure`() {
                // DimensionMemberSignature
                insertCategorisation(
                    id = 111,
                    dms = "$expDimXbrlCode(*?[ ])",
                    dps = "$expDimXbrlCode(*?[$hierarchyCode])"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 111] [DatabaseIdSignature.HierarchyIdentifier] [Value is blank]",
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 111] [DimensionMemberSignature, DPS] [Signatures do not match] [Hierarchies not same, DB Hierarchy identifier `null´ XBRL Hierarchy identifier `ExpDomHier-2-Code´]"
                )

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode(*?[$hierarchyIdBaseline])",
                    dps = "$expDimXbrlCode(*?[ ])"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 112] [XbrlCodeSignature.HierarchyIdentifier] [Value is blank]",
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 112] [DimensionMemberSignature, DPS] [Signatures do not match] [Hierarchies not same, DB Hierarchy identifier `ExpDomHier-2-Code´ XBRL Hierarchy identifier ` ´]"
                )
            }
        }

        @Nested
        inner class SignatureValueErrors {

            val finalOrdinateCategorisationValidationMarker = "VALIDATION [OrdinateCategorisation (transformed)]"

            @Test
            fun `unknown dimension should cause validation failure only for DPS`() {
                // DimensionMemberSignature
                insertCategorisation(
                    id = 111,
                    dms = "FixPrfx_dim:NonExistingDimension($memberXbrlCode)",
                    dps = "$expDimXbrlCode($memberXbrlCode)"
                )
                replaceDictionaryInDb()
                diagnosticEventsShouldNotContain(finalOrdinateCategorisationValidationMarker)

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode($memberXbrlCode)",
                    dps = "FixPrfx_dim:NonExistingDimension($memberXbrlCode)"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "$finalOrdinateCategorisationValidationMarker [OrdinateID: 112] [BaselineDPS: FixPrfx_dim:NonExistingDimension(FixPrfx_ExpDom-1-Code:Mbr-2-Code)] [DbReferences.DimensionId] [Value missing (No Dimension with XBRL code `FixPrfx_dim:NonExistingDimension´)]"
                )
            }

            @Test
            fun `unknown member should cause validation failure only for DPS`() {
                // DimensionMemberSignature
                insertCategorisation(
                    id = 111,
                    dms = "$expDimXbrlCode(FixPrfx_ExpDom-1-Code:NonExistingMember)",
                    dps = "$expDimXbrlCode($memberXbrlCode)"
                )
                replaceDictionaryInDb()
                diagnosticEventsShouldNotContain(finalOrdinateCategorisationValidationMarker)

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode($memberXbrlCode)",
                    dps = "$expDimXbrlCode(FixPrfx_ExpDom-1-Code:NonExistingMember)"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATION [OrdinateCategorisation (transformed)] [OrdinateID: 112] [BaselineDPS: FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:NonExistingMember)] [DbReferences.MemberId] [Value missing (No Member with XBRL code `FixPrfx_ExpDom-1-Code:NonExistingMember´)]"
                )
            }

            @Test
            fun `unknown hierarchy in full OpenAxisValueRestriction should cause validation failure only for DPS`() {
                // DimensionMemberSignature
                insertCategorisation(
                    id = 111,
                    dms = "$expDimXbrlCode(*[4949;$hierarchyStartMemberIdBaseline;0])",
                    dps = "$expDimXbrlCode(*[$hierarchyCode;$hierarchyStartMemberCode;0])"
                )
                replaceDictionaryInDb()
                diagnosticEventsShouldNotContain(finalOrdinateCategorisationValidationMarker)

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode(*[$hierarchyIdBaseline;$hierarchyStartMemberIdBaseline;0])",
                    dps = "$expDimXbrlCode(*[NonExistingHierarchyCode;$hierarchyStartMemberCode;0])"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSequence(
                    "$finalOrdinateCategorisationValidationMarker [OrdinateID: 112] [BaselineDPS: FixPrfx_dim:ExpDim-1-Code(*[NonExistingHierarchyCode;Mbr-4-Code;0])] [DbReferences.HierarchyId] [Value missing (No Hierarchy with HierarchyCode `NonExistingHierarchyCode´ within Domain `5´)]",
                    "$finalOrdinateCategorisationValidationMarker [OrdinateID: 112] [BaselineDPS: FixPrfx_dim:ExpDim-1-Code(*[NonExistingHierarchyCode;Mbr-4-Code;0])] [DbReferences.HierarchyStartingMemberId] [Value missing (No HierarchyNode with MemberCode `Mbr-4-Code´ within Hierarchy `null´)]"
                )
            }

            @Test
            fun `unknown hierarchy starting member in full OpenAxisValueRestriction should cause validation failure only for DPS`() {
                // DimensionMemberSignature
                insertCategorisation(
                    id = 111,
                    dms = "$expDimXbrlCode(*[$hierarchyIdBaseline;4949;0])",
                    dps = "$expDimXbrlCode(*[$hierarchyCode;$hierarchyStartMemberCode;0])"
                )
                replaceDictionaryInDb()
                diagnosticEventsShouldNotContain(finalOrdinateCategorisationValidationMarker)

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode(*[$hierarchyIdBaseline;$hierarchyStartMemberIdBaseline;0])",
                    dps = "$expDimXbrlCode(*[$hierarchyCode;NonExistingMemberCode;0])"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "$finalOrdinateCategorisationValidationMarker [OrdinateID: 112] [BaselineDPS: FixPrfx_dim:ExpDim-1-Code(*[ExpDomHier-2-Code;NonExistingMemberCode;0])] [DbReferences.HierarchyStartingMemberId] [Value missing (No HierarchyNode with MemberCode `NonExistingMemberCode´ within Hierarchy `11´)]"
                )
            }

            @Test
            fun `unsupported starting member inclusion marker in full OpenAxisValueRestriction should cause validation failure only for DPS`() {
                // DimensionMemberSignature
                insertCategorisation(
                    id = 111,
                    dms = "$expDimXbrlCode(*[$hierarchyIdBaseline;$hierarchyStartMemberIdBaseline;XYZ])",
                    dps = "$expDimXbrlCode(*[$hierarchyCode;$hierarchyStartMemberCode;0])"
                )
                replaceDictionaryInDb()
                diagnosticEventsShouldNotContain(finalOrdinateCategorisationValidationMarker)
                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 111] [DatabaseIdSignature.IsStartingMemberIncluded] [Unsupported value] [XYZ]"
                )

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode(*[$hierarchyIdBaseline;$hierarchyStartMemberIdBaseline;0])",
                    dps = "$expDimXbrlCode(*[$hierarchyCode;$hierarchyStartMemberCode;XYZ])"
                )
                replaceDictionaryInDb()
                diagnosticEventsShouldNotContain(finalOrdinateCategorisationValidationMarker)
                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATION [OrdinateCategorisation (baseline)] [OrdinateID: 112] [XbrlCodeSignature.IsStartingMemberIncluded] [Unsupported value] [XYZ]"
                )
            }

            @Test
            fun `unknown hierarchy in partial OpenAxisValueRestriction should cause validation failure only for DPS`() {
                // DimensionMemberSignature
                insertCategorisation(
                    id = 111,
                    dms = "$expDimXbrlCode(*?[4949])",
                    dps = "$expDimXbrlCode(*?[$hierarchyCode])"
                )
                replaceDictionaryInDb()
                diagnosticEventsShouldNotContain(finalOrdinateCategorisationValidationMarker)

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode(*?[$hierarchyIdBaseline])",
                    dps = "$expDimXbrlCode(*?[NonExistingHierarchyCode])"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "$finalOrdinateCategorisationValidationMarker [OrdinateID: 112] [BaselineDPS: FixPrfx_dim:ExpDim-1-Code(*?[NonExistingHierarchyCode])] [DbReferences.HierarchyId] [Value missing (No Hierarchy with HierarchyCode `NonExistingHierarchyCode´ within Domain `5´)]"
                )
            }

            private fun diagnosticEventsShouldNotContain(text: String) {
                diagnosticCollector.events.forEach {
                    if (it.contains(text)) {
                        Assertions.fail<String>(
                            "\nDiagnostic events should not contain: '$text' \n\nDiagnosticEvents: \n${diagnosticCollector.events.joinToString(
                                separator = ",\n"
                            )}"
                        )
                    }
                }
            }
        }
    }
}
