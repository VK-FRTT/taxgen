# TaxGen command-line reference

<br>

## 1. Overview

The TaxGen is a utility tool to programmatically generate Data Point Models from data stored in the Reference Data tool (which is part of the Finnish Interoperability Platform). The TaxGen reads data from the Reference Data tool and transposes data to Data Point Model according the mapping rules presented in [Mapping between Data Point metamodel and Reference Data tool data model](data-point-metamodel-mapping-to-reference-data-tool.md) -document.

The TaxGen is executed via Command Line Interface (CLI). The TaxGen operation is controlled via parameters passed in the command line.

 <br>

### 1.1 Revision history

| Revision | Date       | Author(s) | Description                                   |
| -------- | ---------- | --------- | --------------------------------------------- |
| 0.1      | 2019-01-26 | HE        | Initial help content for TaxGen 0.1.0 version |
|          |            |           |                                               |

 <br>

## 2. TaxGen command line options

### 2.1 Command options

` --help`

Prints help text about command line options and exit.

`--version`

Prints information about the TaxGen version and exit.

`--create-dictionary-to-new-dpm-db` _[Filename]_

Creates new DPM database in SQLite format, writes DPM dictionary content there and exit. _[Filename]_ is the filename for the created database file. 

`--replace-dictionary-in-dpm-db` _[Filename]_

Replaces DPM dictionary in existing SQLite formatted DPM database and exit.  _[Filename]_ must contain valid filename and path, pointing to existing database file. 

`--capture-dpm-sources-to-folder` _[Path]_

Captures DPM sources to folder and exit. _[Path]_ must be a valid path, pointing to folder where source contents are stored. 

`--capture-dpm-sources-to-zip` _[Filename]_

Captures DPM sources to zip file and exit. _[Filename]_ is the filename for the created ZIP file. 

 <br>

### 2.2 Source options

`--source-config` _[Filename]_

Instructs TaxGen to use given configuration file to resolve DPM sources. _[Filename]_ must be a valid filename pointing to source configuraton file. See section _4. Source configuration file structure_ for the configuration file reference.

`--source-folder`  _[Path]_

Instructs TaxGen to load DPM source content from given folder. _[Path]_ must be a valid filesystem path pointing to DPM sources. 

`--source-zip`  _[Filename]_

Instructs TaxGen to load DPM source content from given ZIP file. _[Filename]_ must be a valid filename pointing to ZIP file. 

 <br>

### 2.3 Miscellaneous options

`--force-overwrite`

When given, TaxGen silently overwrites existing conflicting target files. Note: when option is given, the TaxGen does not  wipe existing folders empty but only overwrites individual conflicting files. 

 <br>

## 3. Command line examples 

### 3.1 Show TaxGen command line help

```
taxgen --help
```

Prints help text about command line options and exit.

 <br>

### 3.2 Create DPM Dictionary to new DPM database

```
taxgen --create-dictionary-to-new-dpm-db model_2019-03-15.db --source-config sbr-2019-1-config.json
```

Create DPM Dictionary (`--create-dictionary-to-new-dpm-db`) to new DPM database file  (`model_2019-03-15.db`). DPM sources are provided via (`--source-config`)  source configuration file (`sbr-2019-1-config.json`).

 <br>

### 3.3 Replace DPM Dictionary in existing DPM database

```
taxgen --replace-dictionary-in-dpm-db model_2019-03-15.db --source-config sbr-2019-1-config.json
```

Replaces DPM Dictionary (`--replace-dictionary-in-dpm-db`) in existing DPM database file (`model_2019-03-15.db`). DPM sources are provided via (`--source-config` ) source configuration file (`sbr-2019-1-config.json`).

 <br>

## 4. Source configuration file structure

Here is shown the source configuration file structure. Configuration defines the DPM Dictionary content, by describing a set of DPM Owners and for each owner further describes Metrics, Domains and other DPM Dictionary elements. Owner specific DPM Dictionary elements are defined by linking to the relevant Codelists on the Reference Data tool (which is a part of the Finnish Interoperability Platform), by using the Codelists' URIs. 

```json
{
  "dpmDictionaries": [
    {
      "owner": {
        "name": "<The owner name>",
        "namespace": "<The owner namespace value>",
        "prefix": "<The owner prefix value>",
        "location": "<The owner location value>",
        "copyright": "<The owner copyright text>",
        "languages": [
          "<List of languages, identified by their ISO 6391 codes>",
          "<Localized texts are copied from the Reference Data tool to DPM Concept Translations only for here listed languages>"
        ]
      },
      "metrics": {
        "uri": "<Metrics Codelist URI>"
      },
      "explicitDomainsAndHierarchies": {
        "uri": "<ExplicitDomainsAndHierarchies Codelist URI>"
      },
      "explicitDimensions": {
        "uri": "<ExplicitDimensions Codelist URI>"
      },
      "typedDomains": {
        "uri": "<TypedDomains Codelist URI>"
      },
      "typedDimensions": {
        "uri": "<TypedDimensions Codelist URI>"
      }
    }
  ]
}
```

