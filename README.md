# YTI XBRL Taxonomy Generator

## 1. Overview

YTI XBRL Taxonomy Generator is a tool for generating XBRL taxonomy files from given financial data model.

In a high level the Taxonomy Generator:
1. Accepts financial data models as source (input).
2. Parses financial data model from its source format and maps model contents to Taxonomy Generator's internal data model.
3. Maps financial data model (from TaxGen internal model) to requested output format.


### 1.1 Data model input source format
Currently Taxonomy Generator supports YTI Codelist -service based source data format (YCL-source).
In practice YCL-source consists from CodeScheme and Code entities with related extensions.

In future Taxonomy Generator might also support YTI Data Model -service based source data format (YDM-source).
However, details how financial data models are mapped to YTI Data Model are still open.


### 1.2 Internal data model
Taxonomy Generator's internal data model is based on the artifacts of the Data Point Modeling methodology.
More information about Data Point Modeling methodology can be found from 
[Eurofiling project](http://www.eurofiling.info/dpm/index.shtml).
Especially [DPM formal model document](http://www.eba.europa.eu/documents/10180/632822/Description+of+DPM+formal+model.pdf) 
is useful as it explains DPM artifacts in terms of UML diagrams. 


### 1.3 Model output
Currently Taxonomy Generator is able to produce DPM database in SQLite format.


## 2. Modularization
Taxonomy Generator is modularized to isolated modules.


### 2.1 `yti-taxgen-commons`
- Common utilities of Taxonomy Generator.
- Note: Only production related utilities here.


### 2.2 `yti-taxgen-test-commons`
- Common test utilities of Taxonomy Generator.
- Note: Testing related common stuff goes here.


### 2.3 `yti-taxgen-data-point-meta-model`
- Taxonomy Generator's internal data model: Data Point Meta Model.
- In practice this module implements concepts and entities used in Data Point Modeling.
- Implements also DPM level data validations.


### 2.4 `yti-taxgen-ycl-source-provider`
- Adapter for reading YTI Codelist (YCL) based data.
- Able to provide data from remote YCL service or local filesystem based snapshots.


### 2.5 `yti-taxgen-ycl-to-dpm-mapper`
- Parses YTI Codelist (YCL) data and maps its contents to Data Point Meta Model.


### 2.6 `yti-taxgen-dpm-db-writer`
- Produces DPM database in SQLite format from Data Point Meta Model data.


### 2.7 `yti-taxgen-cli`
- Stand-alone command line application for executing taxonomy generation from console.
- Takes taxonomy configuration file as input.

