## 1. DPM concepts - Mapping from YTI Reference Data -service

### 1.1 Metric

#### Structure
Concept                       | Source
----------------------------- | -------------------------------------
All Metrics                   | Single YTI Codelist associated as `MetricsCodelistUri` in DPM Dictionary Config

#### DPM attributes
Attribute                     | Value source                          | Notes
----------------------------- | ------------------------------------- | -------------------------------------

#### Additional attributes
Attribute                     | Value source                          | Notes
----------------------------- | ------------------------------------- | -------------------------------------




### 1.2 Explicit Domain

#### Structure
Concept                       | Source
----------------------------- | -------------------------------------
Individual Explict Domain     | YTI Codelist.Code
All Explict Domains           | Single YTI Codelist associated as `ExplictDomainsCodelistUri` in DPM Dictionary Config

#### DPM attributes
Attribute                     | Value source                         | Notes
----------------------------- | ------------------------------------ | --------------------------------------
DomainCode                    | YTICode.codeValue                    |
DomainXBRLCode                | _Computed_                           | ${Owner.prefix}_exp:${DomainCode} 
DomainLabel                   | YTICode.prefLabel                    |
DomainDescription             | YTICode.description                  |
DataType                      | _Fixed_                              | `NULL` for Explicit Domains
IsTypedDomain                 | _Fixed_                              | `FALSE` for Explict Domains
Concept                       | YTICode                              | Timestamps, validity dates, etc

#### Additional attributes
Attribute                     | Value source                                                  | Notes
----------------------------- | ------------------------------------------------------------- | -------------------------------------
MembersCodelistUri            | YTICode.externalReferences("MembersCodelistUri").first()      | Associates all YTI Codes from given YTI Codelist as Members to this Explicit Domain
HierarchiesCodelistUri        | YTICode.externalReferences("HierarchiesCodelistUri").first()  | Associates all YTI Codelist Extensions from given YTI Codelist as Hierarchies to this Explicit Domain
HierarchyExtenionUris         | YTICode.externalReferences("HierarchyExtensionUri")           | Associates all given YTI Codelist Extensions as Hierarchies to this Explicit Domain
MemberCodePrefix              | YTICode.??                                                    | Optional prefix for Member's MemberCodes

TODO
 - Property types & semantic identifiers for links:
   - MembersCodelistUri
   - HierarchiesCodelistUri
   - HierarchyExtensionUri
- Value source for MemberCodePrefix



### 1.3 DPM Explict Domain Member

#### Structure mappig
Concept                          | Source
-------------------------------- | -------------------------------------
Individual Explict Domain Member | YTI Codelist.Code
Members for one Explict Domain   | YTI Codelist associated to the Explict Domain via `MembersCodelistUri`

#### DPM attributes
Attribute                     | Value source                          | Notes
----------------------------- | ------------------------------------- | -------------------------------------
Domain                        | _Association_                         | Domain this Member belongs to. Derived from association.
MemberCode                    | YTICode.codeValue                     |
MemberXBRLCode                | _Computed_                            | "${Owner.prefix}_${Domain.DomainCode}:${MemberCode}
MemberLabel                   | YTICode.prefLabel                     |
IsDefaultMember               | _Computed_                            | YTICodelist.defaultCode.codevalue == MemberCode
Concept                       | YTICode                               | Timestamps, validity dates, etc



### 1.4 Hierarchies

#### Structure
Concept                            | Source
---------------------------------- | -------------------------------------
Individual Hierarchy               | YTI Codelist Extension
Hierarchies for one Explict Domain | All YTI Codelist Extensions associated to Explict Domain via `HierarchiesCodelistUri` and `HierarchyExtenionUris`. Only Codelist Extensions of Property Type `definitionHierarchy` or `calculationHierarchy` are accepted as Hierarchies

#### DPM attributes
Attribute                    | Source                               | Notes
---------------------------- | ------------------------------------ | ------------------------------------
Domain                       | _Association_                        | Domain this Hierarchy relates to. Derived from association.
HierarchyCode                | YTICodelistExtension.codeValue       |
HierarchyLabel               | YTICodelistExtension.prefLabel       |
HierarchyDescription         | _Fixed_                              | `NULL` for now
Concept                      | YTICodelistExtension                 | Timestamps, validity dates, etc



### 1.5 Hierarchy Node

#### Structure
Concept                       | Source
----------------------------- | -------------------------------------
Individual Hierarchy Node     | YTI Codelist Extension Member
Nodes for one Hierarchy       | All YTI Codelist Extension Members present in YTI Codelist Extension.

#### DPM attributes
Attribute             | Value source                                                 | Notes
--------------------- | ------------------------------------------------------------ | -------------------------------------
Hierarchy             | _Association_                                                | Hierarchy to which this Node belongs. Derived from association
Member                | YTICodelistExtensionMember.code                              | Member this node represents
ParentMember          | YTICodelistExtensionMember.parentMember().code               | Indicates the parent node, `NULL` for root level nodes
IsAbstract            | _Fixed_                                                      | `FALSE` for now
ComparisonOperator    | YTICodelistExtensionMember.memberValue("ComparisonOperator") |
UnaryOperator         | YTICodelistExtensionMember.memberValue("UnaryOperator")      |
Order                 | _Computed_                                                   | From order in which the YTI Codelist Extension Members are present in YTI Codelist Extension
Level                 | _Computed_                                                   | From hierarchy structure
Path                  | _Fixed_                                                      | `NULL` for now



### 1.6 Explict Dimension
#### Mapping
Concept                          | Source
-------------------------------- | -------------------------------------

#### DPM attributes
Attribute                    | Value source                         | Notes
---------------------------- | ------------------------------------ | -------------------------------------



### 1.7 Typed Domain

#### Structure
Concept                      | Source
---------------------------- | -------------------------------------

#### DPM attributes
Attribute                    | Value source                         | Notes
---------------------------- | ------------------------------------ | -------------------------------------



### 1.8 Typed Dimension

#### Structure
Concept                      | Source
---------------------------- | -------------------------------------

#### DPM attributes
Attribute                    | Value source                         | Notes
---------------------------- | ------------------------------------ | -------------------------------------



## 2. DPM Dictionary Config
```JSON
{
  "dpmDictionaries": [
	{
	  "owner": {
		"name": "SBR Sample",
		"namespace": "sbr.example.com",
		"prefix": "s2br",
        "location": "sbr.example.com/aample/location",
        "copyright": "Copyright statement about SBR Sample",
        "languages": [
          "en",
          "fi",
          "sv"
        ],
        "defaultLanguage": "en"
	  },
	  "metricsCodelistUri": "http://uri.example.com/codelist/...",
	  "explictDomainsCodelistUri": "http://uri.example.com/codelist/...",
	  "explictDimensionsCodelistUri": "http://uri.example.com/codelist/...",
	  "typedDomainsCodelistUri": "http://uri.example.com/codelist/...",
	  "typedDimensionsCodelistUri": "http://uri.example.com/codelist/..."
	}
  ]
}
```
