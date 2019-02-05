package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SQLite DPM DB content: dimensions")
internal class DpmDbWriter_DimensionContent_UnitTest : DpmDbWriter_UnitTestBase() {

    @Test
    fun `should have Dimensions with Concept and Owner relation`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    D.DimensionCode,
                    D.DimensionLabel,
                    D.DimensionDescription,
                    D.DimensionXBRLCode,
                    D.IsTypedDimension,
                    DOM.DomainCode,
                    C.ConceptType,
                    C.CreationDate,
                    C.ModificationDate,
                    C.FromDate,
                    C.ToDate,
                    O.OwnerName
                FROM mDimension AS D
                INNER JOIN mConcept AS C ON C.ConceptID = D.ConceptID
                INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
                INNER JOIN mDomain AS DOM ON D.DomainID = DOM.DomainID
                ORDER BY D.DimensionCode
              """
        )

        assertThat(rs.toStringList()).containsExactly(
            "#DimensionCode, #DimensionLabel, #DimensionDescription, #DimensionXBRLCode, #IsTypedDimension, #DomainCode, #ConceptType, #CreationDate, #ModificationDate, #FromDate, #ToDate, #OwnerName",
            "ExpDim-1-Code, ExpDim-Lbl-Fi, ExpDim-Desc-Fi, FixPrfx_dim:ExpDim-1-Code, 0, ExpDom-1-Code, Dimension, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "MET, Metric dimension, nil, MET, 0, MET, Dimension, nil, nil, nil, nil, EuroFiling",
            "TypDim-1-Code, TypDim-Lbl-Fi, TypDim-Desc-Fi, FixPrfx_dim:TypDim-1-Code, 1, TypDom-1-Code, Dimension, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName"
        )
    }

    @Test
    fun `should have ConceptTranslations for Dimensions`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    D.DimensionCode,
                    C.ConceptType,
                    T.Role,
                    TL.IsoCode,
                    T.Text
                FROM mDimension AS D
                INNER JOIN mConcept AS C ON C.ConceptID = D.ConceptID
                INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
                INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                ORDER BY D.DimensionCode, T.Role DESC, TL.IsoCode
              """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "#DimensionCode, #ConceptType, #Role, #IsoCode, #Text",
            "ExpDim-1-Code, Dimension, label, en, ExpDim-Lbl-En",
            "ExpDim-1-Code, Dimension, label, fi, ExpDim-Lbl-Fi",
            "ExpDim-1-Code, Dimension, description, en, ExpDim-Desc-En",
            "ExpDim-1-Code, Dimension, description, fi, ExpDim-Desc-Fi",
            "MET, Dimension, label, en, Metric dimension",
            "TypDim-1-Code, Dimension, label, en, TypDim-Lbl-En",
            "TypDim-1-Code, Dimension, label, fi, TypDim-Lbl-Fi",
            "TypDim-1-Code, Dimension, description, en, TypDim-Desc-En",
            "TypDim-1-Code, Dimension, description, fi, TypDim-Desc-Fi"
        )
    }
}