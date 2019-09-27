package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.ProcessingOptions
import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest

internal class DpmDbWriter_ContentOptionals_ModuleTest : DpmDbWriter_ContentModuleTestBase() {

    val domainLabelTranslationsQuery =
        """
        SELECT
            D.DomainCode,
            C.ConceptType,
            T.Role,
            TL.IsoCode,
            T.Text
        FROM mDomain AS D
        INNER JOIN mConcept AS C ON C.ConceptID = D.ConceptID
        INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
        INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
        WHERE D.DomainCode = 'ExpDom-1-Code'
        ORDER BY D.DomainCode, T.Role DESC, TL.IsoCode
        """

    val domainInherentTextQuery =
        """
        SELECT
            D.DomainCode,
            D.DomainLabel,
            D.DomainDescription
        FROM mDomain AS D
        WHERE D.DomainCode = 'ExpDom-1-Code'
        """

    val hierarchyNodeTextsQuery =
        """
        SELECT
            C.ConceptType,
            H.HierarchyLabel,
            M.MemberCode,
            N.HierarchyNodeLabel,
            T.Role,
            TL.IsoCode,
            T.Text
        FROM mHierarchyNode AS N
        LEFT JOIN mHierarchy AS H ON H.HierarchyID = N.HierarchyID
        INNER JOIN mMember AS M ON M.MemberID = N.MemberID
        INNER JOIN mConcept AS C ON C.ConceptID = N.ConceptID
        INNER JOIN mConceptTranslation AS T ON T.ConceptID = N.ConceptID
        INNER JOIN mLanguage AS TL ON TL.LanguageID = T.LanguageID
        WHERE M.MemberCode IN ("Mbr-1-Code", "Mbr-4-Code", "ed1")
        ORDER BY H.HierarchyLabel ASC, M.MemberCode ASC, T.Role DESC, TL.IsoCode ASC
        """

    val allFiLabelTranslations =
        """
        SELECT
            C.ConceptType,
            T.Role,
            TL.IsoCode,
            T.Text
        FROM mConcept AS C
        INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
        INNER JOIN mLanguage AS TL ON TL.LanguageID = T.LanguageID
        WHERE T.Role = "label" AND TL.IsoCode = "fi"
        ORDER BY C.ConceptType ASC, T.Role DESC, TL.IsoCode ASC
        """

    fun inherentTextLanguageProcessingOptions(
        inherentTextLanguage: Language?
    ) = ProcessingOptions(
        sqliteDbDpmElementInherentTextLanguage = inherentTextLanguage,
        sqliteDbMandatoryLabelLanguage = null,
        sqliteDbMandatoryLabelSourceLanguages = null,
        sqliteDbDpmElementUriStorageLabelLanguage = null,
        sqliteDbHierarchyNodeLabelCompositionLanguages = null,
        sqliteDbHierarchyNodeLabelCompositionNodeFallbackLanguage = null
    )

    fun mandatoryLabelLanguageProcessingOptions(
        mandatoryLabelLanguage: Language?,
        mandatoryLabelSourceLanguages: List<Language>?
    ) = ProcessingOptions(
        sqliteDbDpmElementInherentTextLanguage = null,
        sqliteDbMandatoryLabelLanguage = mandatoryLabelLanguage,
        sqliteDbMandatoryLabelSourceLanguages = mandatoryLabelSourceLanguages,
        sqliteDbDpmElementUriStorageLabelLanguage = null,
        sqliteDbHierarchyNodeLabelCompositionLanguages = null,
        sqliteDbHierarchyNodeLabelCompositionNodeFallbackLanguage = null
    )

    fun uriStorageProcessingOptions(
        uriStorageLabelLanguage: Language?
    ) = ProcessingOptions(
        sqliteDbDpmElementInherentTextLanguage = null,
        sqliteDbMandatoryLabelLanguage = null,
        sqliteDbMandatoryLabelSourceLanguages = null,
        sqliteDbDpmElementUriStorageLabelLanguage = uriStorageLabelLanguage,
        sqliteDbHierarchyNodeLabelCompositionLanguages = null,
        sqliteDbHierarchyNodeLabelCompositionNodeFallbackLanguage = null
    )

    fun hierarchyNodeLabelCompositionProcessingOptions(
        inherentTextLanguage: Language?,
        compositionLanguages: List<Language>?,
        fallbackLanguage: Language?
    ) = ProcessingOptions(
        sqliteDbDpmElementInherentTextLanguage = inherentTextLanguage,
        sqliteDbMandatoryLabelLanguage = null,
        sqliteDbMandatoryLabelSourceLanguages = null,
        sqliteDbDpmElementUriStorageLabelLanguage = null,
        sqliteDbHierarchyNodeLabelCompositionLanguages = compositionLanguages,
        sqliteDbHierarchyNodeLabelCompositionNodeFallbackLanguage = fallbackLanguage
    )

    override fun createDynamicTests(): List<DynamicNode> {

        return listOf(
            dynamicContainer(
                "InherentTextLanguage option",
                listOf(

                    dynamicTest("should produce inherent texts for requested lang when such label exists") {
                        executeDpmDbWriter(
                            false,
                            FixtureVariety.TRANSLATIONS_FI_ONLY,
                            inherentTextLanguageProcessingOptions(
                                inherentTextLanguage = Language.byIso6391CodeOrFail("fi")
                            )
                        )

                        val rs = dbConnection.createStatement().executeQuery(domainInherentTextQuery)

                        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                            "#DomainCode, #DomainLabel, #DomainDescription",
                            "ExpDom-1-Code, ExpDom-Lbl-Fi, ExpDom-Desc-Fi"
                        )
                    },

                    dynamicTest("should not produce inherent texts when no label exists for requested language") {
                        executeDpmDbWriter(
                            false,
                            FixtureVariety.TRANSLATIONS_FI_ONLY,
                            inherentTextLanguageProcessingOptions(
                                inherentTextLanguage = Language.byIso6391CodeOrFail("en")
                            )
                        )

                        val rs = dbConnection.createStatement().executeQuery(domainInherentTextQuery)

                        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                            "#DomainCode, #DomainLabel, #DomainDescription",
                            "ExpDom-1-Code, nil, nil"
                        )
                    },

                    dynamicTest("should not produce inherent texts when config is null") {
                        executeDpmDbWriter(
                            false,
                            FixtureVariety.TRANSLATIONS_FI_ONLY,
                            inherentTextLanguageProcessingOptions(
                                inherentTextLanguage = null
                            )
                        )

                        val rs = dbConnection.createStatement().executeQuery(domainInherentTextQuery)

                        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                            "#DomainCode, #DomainLabel, #DomainDescription",
                            "ExpDom-1-Code, nil, nil"
                        )
                    }
                )
            ),

            dynamicContainer(
                "MandatoryLabel option",
                listOf(

                    dynamicTest("should support configurable Mandatory Language and Source Candidate Languages (FI injected to EN)") {
                        executeDpmDbWriter(
                            false,
                            FixtureVariety.TRANSLATIONS_FI_ONLY,
                            mandatoryLabelLanguageProcessingOptions(
                                mandatoryLabelLanguage = Language.byIso6391CodeOrFail("en"),
                                mandatoryLabelSourceLanguages = listOf(
                                    Language.byIso6391CodeOrFail("fi"),
                                    Language.byIso6391CodeOrFail("sv")
                                )
                            )
                        )

                        val rs = dbConnection.createStatement().executeQuery(domainLabelTranslationsQuery)

                        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                            "#DomainCode, #ConceptType, #Role, #IsoCode, #Text",
                            "ExpDom-1-Code, Domain, label, en, ExpDom-Lbl-Fi",
                            "ExpDom-1-Code, Domain, label, fi, ExpDom-Lbl-Fi",
                            "ExpDom-1-Code, Domain, description, fi, ExpDom-Desc-Fi"
                        )
                    },

                    dynamicTest("should treat Source Candidate Languages as prioritized list (SV injected to EN)") {
                        executeDpmDbWriter(
                            false,
                            FixtureVariety.TRANSLATIONS_FI_SV,
                            mandatoryLabelLanguageProcessingOptions(
                                mandatoryLabelLanguage = Language.byIso6391CodeOrFail("en"),
                                mandatoryLabelSourceLanguages = listOf(
                                    Language.byIso6391CodeOrFail("fr"),
                                    Language.byIso6391CodeOrFail("sv"),
                                    Language.byIso6391CodeOrFail("fi")
                                )
                            )
                        )

                        val rs = dbConnection.createStatement().executeQuery(domainLabelTranslationsQuery)

                        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                            "#DomainCode, #ConceptType, #Role, #IsoCode, #Text",
                            "ExpDom-1-Code, Domain, label, en, ExpDom-Lbl-Sv",
                            "ExpDom-1-Code, Domain, label, fi, ExpDom-Lbl-Fi",
                            "ExpDom-1-Code, Domain, label, sv, ExpDom-Lbl-Sv",
                            "ExpDom-1-Code, Domain, description, fi, ExpDom-Desc-Fi",
                            "ExpDom-1-Code, Domain, description, sv, ExpDom-Desc-Sv"
                        )
                    },

                    dynamicTest("should not produce Mandatory Language translation when no suitable Source Candidate Languages is found") {
                        executeDpmDbWriter(
                            false,
                            FixtureVariety.TRANSLATIONS_FI_ONLY,
                            mandatoryLabelLanguageProcessingOptions(
                                mandatoryLabelLanguage = Language.byIso6391CodeOrFail("en"),
                                mandatoryLabelSourceLanguages = listOf(
                                    Language.byIso6391CodeOrFail("fr"),
                                    Language.byIso6391CodeOrFail("sv")
                                )
                            )
                        )

                        val rs = dbConnection.createStatement().executeQuery(domainLabelTranslationsQuery)

                        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                            "#DomainCode, #ConceptType, #Role, #IsoCode, #Text",
                            "ExpDom-1-Code, Domain, label, fi, ExpDom-Lbl-Fi",
                            "ExpDom-1-Code, Domain, description, fi, ExpDom-Desc-Fi"
                        )
                    }
                )
            ),

            dynamicContainer(
                "DpmElementUriStorage option",
                listOf(
                    dynamicTest("should support configurable URI Storage Language (URI stored as PL)") {
                        executeDpmDbWriter(
                            false,
                            FixtureVariety.NONE,
                            uriStorageProcessingOptions(
                                uriStorageLabelLanguage = Language.byIso6391CodeOrFail("pl")
                            )
                        )

                        val rs = dbConnection.createStatement().executeQuery(domainLabelTranslationsQuery)

                        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                            "#DomainCode, #ConceptType, #Role, #IsoCode, #Text",
                            "ExpDom-1-Code, Domain, label, en, ExpDom-Lbl-En",
                            "ExpDom-1-Code, Domain, label, fi, ExpDom-Lbl-Fi",
                            "ExpDom-1-Code, Domain, label, pl, ExpDom-1-Uri",
                            "ExpDom-1-Code, Domain, description, en, ExpDom-Desc-En",
                            "ExpDom-1-Code, Domain, description, fi, ExpDom-Desc-Fi"
                        )
                    },

                    dynamicTest("should produce diagnostic event when target language already has a translation (URI stored as FI)") {
                        executeDpmDbWriter(
                            false,
                            FixtureVariety.NONE,
                            uriStorageProcessingOptions(
                                uriStorageLabelLanguage = Language.byIso6391CodeOrFail("fi")
                            )
                        )

                        val rs = dbConnection.createStatement().executeQuery(domainLabelTranslationsQuery)

                        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                            "#DomainCode, #ConceptType, #Role, #IsoCode, #Text",
                            "ExpDom-1-Code, Domain, label, en, ExpDom-Lbl-En",
                            "ExpDom-1-Code, Domain, label, fi, ExpDom-1-Uri",
                            "ExpDom-1-Code, Domain, description, en, ExpDom-Desc-En",
                            "ExpDom-1-Code, Domain, description, fi, ExpDom-Desc-Fi"
                        )

                        assertThat(diagnosticCollector.eventsString()).contains(
                            "MESSAGE [INFO] [DPM Element URI overwrites existing translation: ExpDom-Lbl-Fi (fi)]"
                        )
                    }
                )
            ),

            dynamicContainer(
                "HierarchyNode label composition option",
                listOf(
                    dynamicTest("should not produce composite labels when config is NULL") {
                        executeDpmDbWriter(
                            false,
                            FixtureVariety.NONE,
                            hierarchyNodeLabelCompositionProcessingOptions(
                                inherentTextLanguage = Language.byIso6391CodeOrFail("en"),
                                compositionLanguages = null,
                                fallbackLanguage = null
                            )
                        )

                        val rs = dbConnection.createStatement().executeQuery(hierarchyNodeTextsQuery)

                        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                            "#ConceptType, #HierarchyLabel, #MemberCode, #HierarchyNodeLabel, #Role, #IsoCode, #Text",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En, label, en, ExpDomHierNode-1-Lbl-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En, label, fi, ExpDomHierNode-1-Lbl-Fi",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En, description, en, ExpDomHierNode-1-Desc-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En, description, fi, ExpDomHierNode-1-Desc-Fi",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En, label, en, ExpDomHierNode-2.1.1-Lbl-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En, description, en, ExpDomHierNode-2.1.1-Desc-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En, description, fi, ExpDomHierNode-2.1.1-Desc-Fi",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En, label, en, MetHierNode-1-Lbl-En",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En, label, fi, MetHierNode-1-Lbl-Fi",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En, description, en, MetHierNode-1-Desc-En",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En, description, fi, MetHierNode-1-Desc-Fi"
                        )
                    },

                    dynamicTest("should produce composite labels only for requested language") {
                        executeDpmDbWriter(
                            false,
                            FixtureVariety.NONE,
                            hierarchyNodeLabelCompositionProcessingOptions(
                                inherentTextLanguage = Language.byIso6391CodeOrFail("en"),
                                compositionLanguages = listOf(
                                    Language.byIso6391CodeOrFail("fi")
                                ),
                                fallbackLanguage = null
                            )
                        )

                        val rs = dbConnection.createStatement().executeQuery(hierarchyNodeTextsQuery)

                        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                            "#ConceptType, #HierarchyLabel, #MemberCode, #HierarchyNodeLabel, #Role, #IsoCode, #Text",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En, label, en, ExpDomHierNode-1-Lbl-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En, label, fi, ExpDomHierNode-1-Lbl-Fi Mbr-1-Lbl-Fi",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En, description, en, ExpDomHierNode-1-Desc-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En, description, fi, ExpDomHierNode-1-Desc-Fi",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En, label, en, ExpDomHierNode-2.1.1-Lbl-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En, label, fi, Mbr-4-Lbl-Fi",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En, description, en, ExpDomHierNode-2.1.1-Desc-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En, description, fi, ExpDomHierNode-2.1.1-Desc-Fi",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En, label, en, MetHierNode-1-Lbl-En",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En, label, fi, MetHierNode-1-Lbl-Fi Met-1-Lbl-Fi",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En, description, en, MetHierNode-1-Desc-En",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En, description, fi, MetHierNode-1-Desc-Fi"
                        )
                    },

                    dynamicTest("should do composition for all requested languages") {
                        executeDpmDbWriter(
                            false,
                            FixtureVariety.NONE,
                            hierarchyNodeLabelCompositionProcessingOptions(
                                inherentTextLanguage = Language.byIso6391CodeOrFail("en"),
                                compositionLanguages = listOf(
                                    Language.byIso6391CodeOrFail("fi"),
                                    Language.byIso6391CodeOrFail("en")
                                ),
                                fallbackLanguage = null
                            )
                        )

                        val rs = dbConnection.createStatement().executeQuery(hierarchyNodeTextsQuery)

                        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                            "#ConceptType, #HierarchyLabel, #MemberCode, #HierarchyNodeLabel, #Role, #IsoCode, #Text",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En Mbr-1-Lbl-En, label, en, ExpDomHierNode-1-Lbl-En Mbr-1-Lbl-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En Mbr-1-Lbl-En, label, fi, ExpDomHierNode-1-Lbl-Fi Mbr-1-Lbl-Fi",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En Mbr-1-Lbl-En, description, en, ExpDomHierNode-1-Desc-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En Mbr-1-Lbl-En, description, fi, ExpDomHierNode-1-Desc-Fi",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En Mbr-4-Lbl-En, label, en, ExpDomHierNode-2.1.1-Lbl-En Mbr-4-Lbl-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En Mbr-4-Lbl-En, label, fi, Mbr-4-Lbl-Fi",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En Mbr-4-Lbl-En, description, en, ExpDomHierNode-2.1.1-Desc-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En Mbr-4-Lbl-En, description, fi, ExpDomHierNode-2.1.1-Desc-Fi",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En Met-1-Lbl-En, label, en, MetHierNode-1-Lbl-En Met-1-Lbl-En",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En Met-1-Lbl-En, label, fi, MetHierNode-1-Lbl-Fi Met-1-Lbl-Fi",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En Met-1-Lbl-En, description, en, MetHierNode-1-Desc-En",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En Met-1-Lbl-En, description, fi, MetHierNode-1-Desc-Fi"
                        )
                    },

                    dynamicTest("should select hierarchy node translation from given fallback language when translation for actual composition language is missing (Mbr-4-Code FI label)") {
                        executeDpmDbWriter(
                            false,
                            FixtureVariety.NONE,
                            hierarchyNodeLabelCompositionProcessingOptions(
                                inherentTextLanguage = Language.byIso6391CodeOrFail("en"),
                                compositionLanguages = listOf(
                                    Language.byIso6391CodeOrFail("fi"),
                                    Language.byIso6391CodeOrFail("en")
                                ),
                                fallbackLanguage = Language.byIso6391CodeOrFail("en")
                            )
                        )

                        val rs = dbConnection.createStatement().executeQuery(hierarchyNodeTextsQuery)

                        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                            "#ConceptType, #HierarchyLabel, #MemberCode, #HierarchyNodeLabel, #Role, #IsoCode, #Text",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En Mbr-1-Lbl-En, label, en, ExpDomHierNode-1-Lbl-En Mbr-1-Lbl-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En Mbr-1-Lbl-En, label, fi, ExpDomHierNode-1-Lbl-Fi Mbr-1-Lbl-Fi",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En Mbr-1-Lbl-En, description, en, ExpDomHierNode-1-Desc-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En Mbr-1-Lbl-En, description, fi, ExpDomHierNode-1-Desc-Fi",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En Mbr-4-Lbl-En, label, en, ExpDomHierNode-2.1.1-Lbl-En Mbr-4-Lbl-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En Mbr-4-Lbl-En, label, fi, ExpDomHierNode-2.1.1-Lbl-En Mbr-4-Lbl-Fi",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En Mbr-4-Lbl-En, description, en, ExpDomHierNode-2.1.1-Desc-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En Mbr-4-Lbl-En, description, fi, ExpDomHierNode-2.1.1-Desc-Fi",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En Met-1-Lbl-En, label, en, MetHierNode-1-Lbl-En Met-1-Lbl-En",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En Met-1-Lbl-En, label, fi, MetHierNode-1-Lbl-Fi Met-1-Lbl-Fi",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En Met-1-Lbl-En, description, en, MetHierNode-1-Desc-En",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En Met-1-Lbl-En, description, fi, MetHierNode-1-Desc-Fi"
                        )
                    },

                    dynamicTest("should produce translation with plain Member label when no HierarchyNode translation found for given fallback language (Mbr-4-Code SV label)") {
                        executeDpmDbWriter(
                            false,
                            FixtureVariety.NONE,
                            hierarchyNodeLabelCompositionProcessingOptions(
                                inherentTextLanguage = Language.byIso6391CodeOrFail("en"),
                                compositionLanguages = listOf(
                                    Language.byIso6391CodeOrFail("fi"),
                                    Language.byIso6391CodeOrFail("en")
                                ),
                                fallbackLanguage = Language.byIso6391CodeOrFail("sv")
                            )
                        )

                        val rs = dbConnection.createStatement().executeQuery(hierarchyNodeTextsQuery)

                        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                            "#ConceptType, #HierarchyLabel, #MemberCode, #HierarchyNodeLabel, #Role, #IsoCode, #Text",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En Mbr-1-Lbl-En, label, en, ExpDomHierNode-1-Lbl-En Mbr-1-Lbl-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En Mbr-1-Lbl-En, label, fi, ExpDomHierNode-1-Lbl-Fi Mbr-1-Lbl-Fi",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En Mbr-1-Lbl-En, description, en, ExpDomHierNode-1-Desc-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-1-Code, ExpDomHierNode-1-Lbl-En Mbr-1-Lbl-En, description, fi, ExpDomHierNode-1-Desc-Fi",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En Mbr-4-Lbl-En, label, en, ExpDomHierNode-2.1.1-Lbl-En Mbr-4-Lbl-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En Mbr-4-Lbl-En, label, fi, Mbr-4-Lbl-Fi",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En Mbr-4-Lbl-En, description, en, ExpDomHierNode-2.1.1-Desc-En",
                            "HierarchyNode, ExpDomHier-Lbl-En, Mbr-4-Code, ExpDomHierNode-2.1.1-Lbl-En Mbr-4-Lbl-En, description, fi, ExpDomHierNode-2.1.1-Desc-Fi",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En Met-1-Lbl-En, label, en, MetHierNode-1-Lbl-En Met-1-Lbl-En",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En Met-1-Lbl-En, label, fi, MetHierNode-1-Lbl-Fi Met-1-Lbl-Fi",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En Met-1-Lbl-En, description, en, MetHierNode-1-Desc-En",
                            "HierarchyNode, MetHier-Lbl-En, ed1, MetHierNode-1-Lbl-En Met-1-Lbl-En, description, fi, MetHierNode-1-Desc-Fi"
                        )
                    },

                    dynamicTest("should produce composite labels only for HierarchyNodes, not for other DPM Elements") {
                        executeDpmDbWriter(
                            false,
                            FixtureVariety.NONE,
                            hierarchyNodeLabelCompositionProcessingOptions(
                                inherentTextLanguage = Language.byIso6391CodeOrFail("en"),
                                compositionLanguages = listOf(
                                    Language.byIso6391CodeOrFail("fi")
                                ),
                                fallbackLanguage = Language.byIso6391CodeOrFail("sv")
                            )
                        )

                        val rs = dbConnection.createStatement().executeQuery(allFiLabelTranslations)

                        assertThat(rs.toStringList()).contains(
                            "#ConceptType, #Role, #IsoCode, #Text",
                            "Dimension, label, fi, ExpDim-Lbl-Fi",
                            "Dimension, label, fi, TypDim-Lbl-Fi",
                            "Domain, label, fi, ExpDom-Lbl-Fi",
                            "Domain, label, fi, TypDom-Lbl-Fi",
                            "Hierarchy, label, fi, ExpDomHier-Lbl-Fi",
                            "Hierarchy, label, fi, MetHier-Lbl-Fi",
                            "HierarchyNode, label, fi, ExpDomHierNode-1-Lbl-Fi Mbr-1-Lbl-Fi",
                            "HierarchyNode, label, fi, MetHierNode-1-Lbl-Fi Met-1-Lbl-Fi",
                            "Member, label, fi, Mbr-1-Lbl-Fi"
                        )
                    }
                )
            )
        )
    }
}
