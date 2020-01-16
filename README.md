# TaxGen

[![Build status](https://github.com/VRK-YTI/yti-taxgen/workflows/build/badge.svg)](#)
[![E2E test status](https://github.com/VRK-YTI/yti-taxgen/workflows/e2e%20test%20nightly/badge.svg)](#)
[![E2E prod test status](https://github.com/VRK-YTI/yti-taxgen/workflows/e2e%20prod%20test%20nightly/badge.svg)](#)

<br/>

The TaxGen is a utility tool to programmatically generate Data Point Models from data stored in the Reference Data tool. In high level:

1. TaxGen reads source data from the Reference Data tool
2. TaxGen maps source data content to Data Point Model
3. TaxGen validates Data Point Model content
4. TaxGen produces a DPM database from Data Point Model content

<br/>

## 1. System structure

TaxGen is split to following modules by their responsibilities.


#### `yti-taxgen-dpm-model`
- Implements data structures and data validation rules for Data Point Modeling elements.
- Standalone, i.e. does not depend from other TaxGen modules.
- Data Point Modeling methodology is documented in [Eurofiling project](http://www.eurofiling.info/dpm/index.shtml). Especially [DPM formal model document](http://www.eba.europa.eu/documents/10180/632822/Description+of+DPM+formal+model.pdf) is useful, as it explains Data Point Modeling elements in terms of UML diagrams. 


#### `yti-taxgen-rd-source`
- Provides abstraction for reading source data from the Reference Data tool.
- Supports reading data from live Reference Data tool and from the local filesystem based snapshots.
- [The Reference Data tool](https://koodistot.suomi.fi/) is one of the tools within the Finnish Interoperability Platform. [The Finnish Interoperability Platform itself](https://yhteentoimiva.suomi.fi/en/) is a technical solution for producing and managing the information metadata (i.e. information about information) for the Finnish public sector.


#### `yti-taxgen-rd-dpm-mapper`
- Consumes source data from the Reference Data tool (via `yti-taxgen-rd-source` interfaces) and maps relevant pieces to Data Point Model (to `yti-taxgen-dpm-model` based data structures).
- Mapping implementation follows rules specified in [Mapping between Data Point metamodel and Reference Data tool data model](docs/data-point-metamodel-mapping-to-reference-data-tool.md) document.


#### `yti-taxgen-sqlite-output`
- Produces SQLite database from `yti-taxgen-dpm-model` based data structures.
- Produced database structure follows one defined in *Tool for Undertakings DPM Database* -document. Document is embedded within *Solvency 2 DPM database* -packages, which in turn can be found from [EIOPA web pages](https://eiopa.europa.eu/) under *Solvency II Data Point Models and XBRL Taxonomies* section.


#### `yti-taxgen-cli`
- Stand-alone command line application for executing TaxGen operations from command line.
- Execution is controlled via the command line parameters. See [TaxGen command-line reference](docs/taxgen-command-line-reference.md) for further information.


#### `yti-taxgen-commons`
- Common utilities used by other modules.


####  `yti-taxgen-test-commons`
- Common testing utilities and test data fixtures used by other modules.

<br/>

## 2. Development

### 2.1 Building

Prerequisites:

- Java 8+
- Gradle 4.6

<br/>

Building runnable JAR:

`$ gradlew jar`

<br/>

Executing TaxGen from JAR:

`$ java -jar yti-taxgen-cli/build/libs/taxgen-cli.jar `

<br/>

### 2.2 Testing

TaxGen contains comprehensive test suite, covering most of the TaxGen functionality. Test cases are split to two gategories: *standalone test* and *end-to-end tests*.


#### Standalone tests

These tests do not depend any external system or setup. They can be executed with:

`$ gradlew test`



#### End-to-end tests

These tests read test fixtures from the Reference Data tool. Test fixtures should normally stay fully functional in the Reference Data tool. However, if fixtures need to be initialized to the Reference Data tool, they can be created with spreadsheet files in [yti-taxgen-test-commons/src/main/resources/test_fixtures/rds_source_config/dm_integration_fixture](yti-taxgen-test-commons/src/main/resources/test_fixtures/rds_source_config/dm_integration_fixture). And linkage from tests to the Reference Data tool resources happens with  source config file [yti-taxgen-test-commons/src/main/resources/test_fixtures/rds_source_config/integration_fixture.json](yti-taxgen-test-commons/src/main/resources/test_fixtures/rds_source_config/integration_fixture.json).

End-to-end tests can be executed with:

`$ gradlew e2etest`

<br/>

### 2.3 Code style 

Code style is managed by Spotless and ktlint. Source codes can be scanned for format violations with: 

`$ gradlew spotlessCheck`



