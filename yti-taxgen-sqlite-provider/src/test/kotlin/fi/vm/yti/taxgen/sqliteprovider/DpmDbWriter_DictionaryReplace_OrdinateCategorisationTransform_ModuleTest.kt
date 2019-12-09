package fi.vm.yti.taxgen.sqliteprovider

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
        fun `ordinate categorisation having partial OpenAxisValueRestriction should get updated`() {
            val updateStatement =
                """
                INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
                VALUES (111, $expDimIdBaseline, 3333, "$expDimXbrlCode($openMemberMarker?[$hierarchyIdBaseline])", "source", "$expDimXbrlCode($openMemberMarker?[$hierarchyCode])")
                """.trimIndent()

            val expectedCategorisations = arrayOf(
                "#OrdinateID, #DimensionID, #MemberID, #DimensionMemberSignature, #Source, #DPS",
                "111, $expDimIdOutput, $openMemberFixedId, $expDimXbrlCode($openMemberMarker?[$hierarchyIdOutput]), source, $expDimXbrlCode($openMemberMarker?[$hierarchyCode])"
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
                "ENTER [SQLiteDbWriter] []",
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
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [BaselineOrdinateCategorisation.signatures: OrdinateCategorisation signatures do not match: Dimensions not same]",
                    "VALIDATION [OrdinateCategorisationSignature.dimensionIdentifier: is blank]"
                )

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode($memberXbrlCode)",
                    dps = " ($memberXbrlCode)"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 112]",
                    "VALIDATION [BaselineOrdinateCategorisation.signatures: OrdinateCategorisation signatures do not match: Dimensions not same]",
                    "VALIDATION [OrdinateCategorisationSignature.dimensionIdentifier: is blank]"
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
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [BaselineOrdinateCategorisation.signatures: OrdinateCategorisation signatures do not match: Members not same]",
                    "VALIDATION [OrdinateCategorisationSignature.memberIdentifier: is blank]"
                )

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode($memberXbrlCode)",
                    dps = "$expDimXbrlCode( )"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 112]",
                    "VALIDATION [BaselineOrdinateCategorisation.signatures: OrdinateCategorisation signatures do not match: Members not same]",
                    "VALIDATION [OrdinateCategorisationSignature.memberIdentifier: is blank]"
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
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [BaselineOrdinateCategorisation.signatures: OrdinateCategorisation signatures do not match: Hierarchies not same]",
                    "VALIDATION [OrdinateCategorisationSignature.hierarchyIdentifier: is blank]"
                )

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode(*[$hierarchyIdBaseline;$hierarchyStartMemberIdBaseline;0])",
                    dps = "$expDimXbrlCode(*[ ;$hierarchyStartMemberCode;0])"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 112]",
                    "VALIDATION [BaselineOrdinateCategorisation.signatures: OrdinateCategorisation signatures do not match: Hierarchies not same]",
                    "VALIDATION [OrdinateCategorisationSignature.hierarchyIdentifier: is blank]"
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
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [BaselineOrdinateCategorisation.signatures: OrdinateCategorisation signatures do not match: Hierarchy starting members not same]",
                    "VALIDATION [OrdinateCategorisationSignature.hierarchyStartingMemberIdentifier: is blank]"
                )

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode(*[$hierarchyIdBaseline;$hierarchyStartMemberIdBaseline;0])",
                    dps = "$expDimXbrlCode(*[$hierarchyCode; ;0])"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 112]",
                    "VALIDATION [BaselineOrdinateCategorisation.signatures: OrdinateCategorisation signatures do not match: Hierarchy starting members not same]",
                    "VALIDATION [OrdinateCategorisationSignature.hierarchyStartingMemberIdentifier: is blank]"
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
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [BaselineOrdinateCategorisation.signatures: OrdinateCategorisation signatures do not match: Starting member inclusion not same]",
                    "VALIDATION [OrdinateCategorisationSignature.startingMemberIncluded: is blank]",
                    "VALIDATION [OrdinateCategorisationSignature.startingMemberIncluded: unsupported IsStartingMemberIncluded value ' ']"
                )

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode(*[$hierarchyIdBaseline;$hierarchyStartMemberIdBaseline;0])",
                    dps = "$expDimXbrlCode(*[$hierarchyCode;$hierarchyStartMemberCode; ])"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 112]",
                    "VALIDATION [BaselineOrdinateCategorisation.signatures: OrdinateCategorisation signatures do not match: Starting member inclusion not same]",
                    "VALIDATION [OrdinateCategorisationSignature.startingMemberIncluded: is blank]",
                    "VALIDATION [OrdinateCategorisationSignature.startingMemberIncluded: unsupported IsStartingMemberIncluded value ' ']"
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
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [BaselineOrdinateCategorisation.signatures: OrdinateCategorisation signatures do not match: Hierarchies not same]",
                    "VALIDATION [OrdinateCategorisationSignature.hierarchyIdentifier: is blank]"
                )

                // DPS
                insertCategorisation(
                    id = 112,
                    dms = "$expDimXbrlCode(*?[$hierarchyIdBaseline])",
                    dps = "$expDimXbrlCode(*?[ ])"
                )
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 112]",
                    "VALIDATION [BaselineOrdinateCategorisation.signatures: OrdinateCategorisation signatures do not match: Hierarchies not same]",
                    "VALIDATION [OrdinateCategorisationSignature.hierarchyIdentifier: is blank]"
                )
            }
        }

        @Nested
        inner class SignatureValueErrors {

            val finalOrdinateCategorisationValidationMarker = "VALIDATED OBJECT [FinalOrdinateCategorisation]"

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
                    "$finalOrdinateCategorisationValidationMarker [OrdinateID: 112]",
                    "VALIDATION [OrdinateCategorisationDbReferences.dimensionId: does not have value]"
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
                    "VALIDATED OBJECT [FinalOrdinateCategorisation] [OrdinateID: 112]",
                    "VALIDATION [OrdinateCategorisationDbReferences.memberId: does not have value]"
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
                    "$finalOrdinateCategorisationValidationMarker [OrdinateID: 112]",
                    "VALIDATION [OrdinateCategorisationDbReferences.hierarchyId: does not have value]",
                    "VALIDATION [OrdinateCategorisationDbReferences.hierarchyStartingMemberId: does not have value]"
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
                    "$finalOrdinateCategorisationValidationMarker [OrdinateID: 112]",
                    "VALIDATION [OrdinateCategorisationDbReferences.hierarchyStartingMemberId: does not have value]"
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
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OrdinateCategorisationSignature.startingMemberIncluded: unsupported IsStartingMemberIncluded value 'XYZ']"
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
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 112]",
                    "VALIDATION [OrdinateCategorisationSignature.startingMemberIncluded: unsupported IsStartingMemberIncluded value 'XYZ']"
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
                    "$finalOrdinateCategorisationValidationMarker [OrdinateID: 112]",
                    "VALIDATION [OrdinateCategorisationDbReferences.hierarchyId: does not have value]"
                )
            }

            private fun diagnosticEventsShouldNotContain(text: String) {
                diagnosticCollector.events.forEach {
                    if (it.contains(text)) {
                        Assertions.fail(
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
