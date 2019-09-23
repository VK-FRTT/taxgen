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

    override fun createDynamicTests(): List<DynamicNode> {

        return listOf(
            dynamicContainer(
                "InherentTextLanguage option",
                listOf(

                    dynamicTest("should produce inherent texts for requested lang when such label exists") {
                        executeDpmDbWriter(
                            false,
                            FixtureVariety.TRANSLATIONS_FI_ONLY,
                            ProcessingOptions(
                                sqliteDbDpmElementInherentTextLanguage = Language.byIso6391CodeOrFail("fi"),
                                sqliteDbMandatoryLabelLanguage = null,
                                sqliteDbMandatoryLabelSourceLanguages = null,
                                sqliteDbDpmElementUriStorageLabelLanguage = null
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
                            ProcessingOptions(
                                sqliteDbDpmElementInherentTextLanguage = Language.byIso6391CodeOrFail("en"),
                                sqliteDbMandatoryLabelLanguage = null,
                                sqliteDbMandatoryLabelSourceLanguages = null,
                                sqliteDbDpmElementUriStorageLabelLanguage = null
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
                            ProcessingOptions(
                                sqliteDbDpmElementInherentTextLanguage = null,
                                sqliteDbMandatoryLabelLanguage = null,
                                sqliteDbMandatoryLabelSourceLanguages = null,
                                sqliteDbDpmElementUriStorageLabelLanguage = null
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
                            ProcessingOptions(
                                sqliteDbDpmElementInherentTextLanguage = null,
                                sqliteDbMandatoryLabelLanguage = Language.byIso6391CodeOrFail("en"),
                                sqliteDbMandatoryLabelSourceLanguages = listOf(
                                    Language.byIso6391CodeOrFail("fi"),
                                    Language.byIso6391CodeOrFail("sv")
                                ),
                                sqliteDbDpmElementUriStorageLabelLanguage = null
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
                            ProcessingOptions(
                                sqliteDbDpmElementInherentTextLanguage = null,
                                sqliteDbMandatoryLabelLanguage = Language.byIso6391CodeOrFail("en"),
                                sqliteDbMandatoryLabelSourceLanguages = listOf(
                                    Language.byIso6391CodeOrFail("fr"),
                                    Language.byIso6391CodeOrFail("sv"),
                                    Language.byIso6391CodeOrFail("fi")
                                ),
                                sqliteDbDpmElementUriStorageLabelLanguage = null
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
                            ProcessingOptions(
                                sqliteDbDpmElementInherentTextLanguage = null,
                                sqliteDbMandatoryLabelLanguage = Language.byIso6391CodeOrFail("en"),
                                sqliteDbMandatoryLabelSourceLanguages = listOf(
                                    Language.byIso6391CodeOrFail("fr"),
                                    Language.byIso6391CodeOrFail("sv")
                                ),
                                sqliteDbDpmElementUriStorageLabelLanguage = null
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
                            ProcessingOptions(
                                sqliteDbDpmElementInherentTextLanguage = null,
                                sqliteDbMandatoryLabelLanguage = null,
                                sqliteDbMandatoryLabelSourceLanguages = null,
                                sqliteDbDpmElementUriStorageLabelLanguage = Language.byIso6391CodeOrFail("pl")
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
                            ProcessingOptions(
                                sqliteDbDpmElementInherentTextLanguage = null,
                                sqliteDbMandatoryLabelLanguage = null,
                                sqliteDbMandatoryLabelSourceLanguages = null,
                                sqliteDbDpmElementUriStorageLabelLanguage = Language.byIso6391CodeOrFail("fi")
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
            )
        )
    }
}
