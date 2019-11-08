# TaxGen command-line reference

<br>

## 1. Overview

The TaxGen is a utility tool to programmatically generate DPM database with Dictionary content from the data stored in the Reference Data tool. 
The Reference Data tool is a online codelist management tool and part of the Finnish Interoperability Platform. 
The TaxGen reads relevant input data from the Reference Data tool and transposes it to DPM Dictionary according the mapping rules presented in [Mapping between Data Point metamodel and Reference Data tool data model](data-point-metamodel-mapping-to-reference-data-tool.md) -document.

The TaxGen is executed via Command Line Interface (CLI). 
The TaxGen operation is controlled via parameters passed in the command line and configuration file defining input data read from the Reference Data tool.

<br>

### 1.1 Revision history

| Revision | Date       | Author(s) | Description                                   |
| -------- | ---------- | --------- | --------------------------------------------- |
| 0.1      | 2019-01-26 | HE        | Initial help content for TaxGen 0.1.0 version |
| 0.2      | 2019-10-08 | HE        | New command line options and configuration file structures added |


<br>

## 2. TaxGen command line options

### 2.1 Command options

` --help`

Prints help text about command line options and exit.

`--version`

Prints information about the TaxGen version and exit.

`--create-dictionary-to-new-dpm-db`

Creates new DPM database in SQLite format, writes DPM dictionary content there and exit. 
DPM source and output options must be given.

`--replace-dictionary-in-dpm-db`

Replaces DPM Dictionary contents in existing SQLite formatted DPM database and exit. 
Baseline database, DPM source and output options must be given.

`--capture-dpm-sources-to-folder`

Captures DPM sources to folder and exit. 
DPM source and output options must be given. 

`--capture-dpm-sources-to-zip`

Captures DPM sources to zip file and exit. 
DPM source and output options must be given. 

<br>

### 2.2 Source options

`--source-config` _[Filename]_

Instructs TaxGen to use given configuration file to resolve DPM sources from the Reference Data tool. 
_[Filename]_ must be a valid filename pointing to source configuraton file. 
See section _4.1 Source configuration file structure_ for the configuration file reference.

`--source-folder` _[Path]_

Instructs TaxGen to load DPM source content from given folder. 
_[Path]_ must be a valid filesystem path pointing to previously captured DPM sources. 

`--source-zip` _[Filename]_

Instructs TaxGen to load DPM source content from given ZIP file. 
_[Filename]_ must be a valid filename pointing to ZIP file, containing previously captured DPM sources.

`--baseline-db` _[Filename]_

Instructs TaxGen to use given database as baseline in dictionary replace. 
_[Filename]_ must be a valid filename pointing to existing database file.


<br>

### 2.3 Output options

`--output` _[Filename]_ or _[Path]_

File or folder where to write command output.

`--force-overwrite`

When given, TaxGen silently overwrites existing conflicting output files. 
Note: TaxGen does not wipe all existing files or folders from target folder, only overwrites individual conflicting files. 

<br>

## 3. Command line examples 

### 3.1 Show TaxGen command line help

```
$ taxgen --help
```

Prints help text about command line options and exit.

<br>

### 3.2 Create DPM Dictionary to new DPM database

```
$ taxgen --create-dictionary-to-new-dpm-db --output model_2019-03-15.db --source-config sbr-2019-1-config.json
```

Creates DPM Dictionary (`--create-dictionary-to-new-dpm-db`) to new blank DPM database file and exit. 
Output database (`--output`) is created with name (`model_2019-03-15.db`). 
DPM sources are resolved from source configuration file (`--source-config`) having name (`sbr-2019-1-config.json`).

<br>

### 3.3 Replace DPM Dictionary from existing DPM database

```
$ taxgen --replace-dictionary-in-dpm-db --baseline-db model_2019-03-15.db --output model_2019-03-15_updated.db --source-config sbr-2019-1-config.json
```

Replaces DPM Dictionary (`--replace-dictionary-in-dpm-db`) from existing DPM database file and exit. 
Baseline database (`--baseline-db`) for replace operation is (`model_2019-03-15.db`) and replaced database (`--output`) is written to (`model_2019-03-15_updated.db`). DPM sources are provided via (`--source-config` ) source configuration file (`sbr-2019-1-config.json`).

<br>

## 4. Source configuration 

### 4.1 Source configuration file structure

`--source-config` option accepts a file, containing following JSON structure. 
Configuration defines the complete DPM Dictionary content, by describing one or more Owners and for each Owner is defined its Metrics, Domains and other Dictionary elements. 
Owner specific DPM Dictionary elements are defined by linking to the relevant Codelists from the Reference Data tool (which is a part of the Finnish Interoperability Platform), by using the Codelists' URIs.
Configuration defines also common processing options, which further finetune how data (especially localized texts) are transformed along the process.

```json
{
  "dpmDictionaries": [
    {
      "owner": {
        "name": "The owner name.",
        "namespace": "The owner namespace value.",
        "prefix": "The owner prefix value.",
        "location": "The owner location value.",
        "copyright": "The owner copyright text.",
        "languages": [
          "Localized texts like labels are copied from the sources to DPM Dictionary only for here listed languages.",
          "List of ISO639-1 language codes."
        ]
      },
      "metrics": {
        "uri": "Metrics Codelist URI"
      },
      "explicitDomainsAndHierarchies": {
        "uri": "ExplicitDomainsAndHierarchies Codelist URI"
      },
      "explicitDimensions": {
        "uri": "ExplicitDimensions Codelist URI"
      },
      "typedDomains": {
        "uri": "TypedDomains Codelist URI"
      },
      "typedDimensions": {
        "uri": "TypedDimensions Codelist URI"
      }
    }
  ],
  "processingOptions": {
    "diagnosticSourceLanguages": [
      "Controls which localized texts are used as source when producing diagnostic messages.",
      "List of ISO639-1 language codes."
    ],
    
    "sqliteDbDpmElementInherentTextLanguage": "Controls which translated texts are copied to DPM Elements built-in label/description fields (for example to mDomain.DomainLabel and mDomain.DomainDescription). Single ISO639-1 code or null.",
    
    "sqliteDbMandatoryLabelLanguage": "Controls which label translation is mandatory in DPM Elements. When DPM Elements is missing the mandatory label translation, it is created by copying translation content from another language. Single ISO639-1 code or null.",
    "sqliteDbMandatoryLabelSourceLanguages": [
      "Controls which label translations are used as source when mandatory label translation is created by copying.",
      "List of ISO639-1 language codes or null."
    ],
    
    "sqliteDbDpmElementUriStorageLabelLanguage": "Controls to which label translation DPM Element URIs are written. Single ISO639-1 code or null.",
    
    "sqliteDbHierarchyNodeLabelCompositionLanguages": [
      "Controls which HierarchyNode label translations are updated to be compositions.",
      "Composite HierarchyNode label translation is created by combining HierarchyNode label translation with HierarchyNode's referenced element (Member or Metric) label translation.", 
      "List of ISO639-1 language codes or null."
    ],
    "sqliteDbHierarchyNodeLabelCompositionNodeFallbackLanguage": "Controls which HierarchyNode label translation is used as fallback in HierarchyNode label composition. Single ISO639-1 code or null."
  }
}
```

### 4.2 Source configuration example

In following is shown a complete sample configuration.

```json
{
  "dpmDictionaries": [
    {
      "owner": {
        "name": "DPM Dictionary sample",
        "namespace": "http://www.example.org/xbrl/crr/",
        "prefix": "fi",
        "location": "http://www.example.org/fr/xbrl/crr/",
        "copyright": "None",
        "languages": [
          "fi",
          "sv",
          "en"
        ]
      },
      "metrics": {
        "uri": "http://uri.suomi.fi/codelist/sbr-fi-dictionary/metrics-2018-1"
      },
      "explicitDomainsAndHierarchies": {
        "uri": "http://uri.suomi.fi/codelist/sbr-fi-dictionary/exp-doms-2018-1"
      },
      "explicitDimensions": {
        "uri": "http://uri.suomi.fi/codelist/sbr-fi-dictionary/exp-dims-2018-1"
      },
      "typedDomains": {
        "uri": "http://uri.suomi.fi/codelist/sbr-fi-dictionary/typ-doms-2018-1"
      },
      "typedDimensions": {
        "uri": "http://uri.suomi.fi/codelist/sbr-fi-dictionary/typ-dims-2018-1"
      }
    }
  ],
  "processingOptions": {
    "diagnosticSourceLanguages": ["fi"],
    "sqliteDbDpmElementInherentTextLanguage": "fi",
    "sqliteDbMandatoryLabelLanguage": "en",
    "sqliteDbMandatoryLabelSourceLanguages": [
      "fi",
      "sv"
    ],
    "sqliteDbDpmElementUriStorageLabelLanguage": "pl",
    "sqliteDbHierarchyNodeLabelCompositionLanguages": [
      "fi",
      "sv",
      "en"
    ],

    "sqliteDbHierarchyNodeLabelCompositionNodeFallbackLanguage": "fi"
  }
}
```
