# YTI XBRL Taxonomy Generator

## 1. Overview

YTI XBRL Taxonomy Generator is a tool for generating XBRL taxonomy files from given financial data model.

In a high level the Taxonomy Generator:
1. Accepts financial data models as input.
2. Parses financial data model from its input format and maps model contents to Taxonomy Generator's internal data model.
3. Analyzes financial data model (from TaxGen internal model) and renders a XBRL taxonomy from it.
4. Produces XBRL taxonomy package containing taxonomy files.


### 1.1 Data model input format
Currently Taxonomy Generator supports YTI Codelist -service based input data format (YCL input).
In practice YCL input consists from CodeScheme and Code entities with related extensions.

In future Taxonomy Generator might also support YTI Data Model -service based input data format (YDM input).
However, details how financial data models are mapped to YTI Data Model are still open.


### 1.2 Internal data model
Taxonomy Generator's internal data model is based on Data Point Model meta-model.
Data Point Models are financial data models designed by following Data Point Modelling methodology.
Data Point Model meta-model contains elements and concepts used in Data Point Models.


### 1.3 XBRL taxonomy generation
Currently Taxonomy Generator is able to produce XBRL Taxonomy in Finnish taxonomy architecture,
which in turn is based on EXTA.


## 2. Modularization
Taxonomy Generator is modularized to somewhat isolated modules with clear responsibilities.


### 2.1 `yti-taxgen-commons`
- Common utilities of Taxonomy Generator.
- Contains both production and testing related utilities.


### 2.2 `yti-taxgen-data-point-meta-model`
- Taxonomy Generator's internal data model: Data Point Meta Model
- In practice this module implements concepts and entities used in Data Point Modeling.


### 2.3 `yti-taxgen-ycl-input-parser`
- Parser for reading YTI Codelist (YCL) based financial data models.
- Parses YTI Codelist data and maps its contents to Data Point Meta Model.


### 2.4 `yti-taxgen-fixta-generator`
- Produces Finnish taxonomy architecture based XBRL Taxonomy from Data Point Meta Model data.


### 2.5 `yti-taxgen-cli`
- Stand-alone command line application for executing taxonomy generation from console.
- Takes taxonomy configuration file as input.

