# Mapping of DPM Dictionary concepts to Yti Reference Data -service

This document describes mapping from DPM Dictionary related DPM modeling concepts to elements available in Yti Reference Data -service. 

Mapping is split for clarity to three sections: 
1. DPM metrics
2. DPM "explicit" concepts, like Explicit Dimensions, Explicit Domains and Hierarchies
3. DPM "typed" concepts, like Typed Dimensions and Typed Domains 

<br>

## 1 DPM metrics

![Mapping Metrics to YTI](images/metrics-yti-mapping.png)

<br>

### 1.1 Metric

#### Structure
Concept                       | Source
----------------------------- | -------------------------------------
Single Metric                 | Yti Codelist Extension Member + Yti Code (MetricNumber)
Complete Metrics collection   | Single Yti Codelist Extension (associated as `Metrics` in DPM Dictionary Config) listing all Metrics

#### Metric attributes
Attribute                 | Data type   | Value source                                                              | Notes
------------------------- | ----------- | ------------------------------------------------------------------------- | -------------------------------------
Domain                    | Association | _Fixed_                                                                   | Domain this Member belongs to. Single fixed Explicit Domain `MET`
MemberCode                | String      | _Computed_                                                                | "${DataTypeIdentifier}${PeriodTypeIdentifier}${MetricNumber}"
MemberXBRLCode            | String      | _Computed_                                                                | "${Owner.prefix}_met:${MemberCode}
MemberLabel               | String      | YtiCodelistExtensionMember -> prefLabel                                   |
IsDefaultMember           | Boolean     | _Fixed_                                                                   | `NULL` for now 
Concept                   | Association | YtiCodelistExtensionMember                                                | Timestamps, validity dates, etc
MetricNumber              | Integer     | YtiCodelistExtensionMember -> code -> codeValue                           | 
DataType                  | String      | YtiCodelistExtensionMember -> memberValue("dpmDataType")                  | Type of data, enumerated text
FlowType                  | String      | YtiCodelistExtensionMember -> memberValue("dpmFlowType")                  | The time dynamics of the information, enumerated text
BalanceType               | String      | YtiCodelistExtensionMember -> memberValue("dpmBalanceType")               | Balance type, enumerated text
ReferencedDomainCode      | String      | YtiCodelistExtensionMember -> memberValue("dpmReferencedDomainCode")      | Associates metric with Domain, from where to obtain allowed values for this Metric
ReferencedHierarchyCode   | String      | YtiCodelistExtensionMember -> memberValue("dpmReferencedHierarchyCode")   | Associates metric with Hierarchy, from where to obtain allowed values for this Metric
HierarchyStartingNode     | String      | _Fixed_                                                                   | `NULL` for now 
IsStartingMemberIncluded  | Boolean     | _Fixed_                                                                   | `NULL` for now

#### Needed changes to Yti Reference Data -Service
- New Yti Codelist Extension & PropertyType for it: `dpmMetric`
- New memberValue PropertyTypes: `dpmDataType`, `dpmFlowType`, `dpmBalanceType`, `dpmReferencedDomainCode`, `dpmReferencedHierarchyCode`

<br>

## 2 DPM "explicit" concepts

![Mapping Explicit Dimensions, Explicit Domains and Hierarchies to YTI](images/explicit-dimensions-domains-hierarchies-yti-mapping.png)

<br>

### 2.1 Explicit Domain

#### Structure
Concept                             | Source
----------------------------------- | -------------------------------------
Single Explict Domain               | Yti Code
Complete Explict Domains collection | Single Yti Codelist (associated as `Explict Elements` in DPM Dictionary Config) listing all Explicit Domains

#### Explicit Domain attributes
Attribute                     | Data type   | Value source                                                          | Notes
----------------------------- | ----------- | --------------------------------------------------------------------- | --------------------------------------
DomainCode                    | String      | YtiCode -> codeValue                                                  |
DomainXBRLCode                | String      | _Computed_                                                            | "${Owner.prefix}_exp:${DomainCode}" 
DomainLabel                   | String      | YtiCode -> prefLabel                                                  |
DomainDescription             | String      | YtiCode -> description                                                |
DataType                      | String      | _Fixed_                                                               | `NULL` for Explicit Domains
IsTypedDomain                 | Boolean     | _Fixed_                                                               | `FALSE` for Explict Domains
Concept                       | Association | YtiCode                                                               | Timestamps, validity dates, etc
MembersCodelistUri            | String      | YtiCode -> externalReferences("MembersCodelistUri") -> first()        | Associates all Yti Codes from given Yti Codelist as Members to this Explicit Domain
HierarchiesCodelistUri        | String      | YtiCode -> externalReferences("HierarchiesCodelistUri") -> first()    | Associates all Yti Codelist Extensions from given Yti Codelist as Hierarchies to this Explicit Domain
HierarchyExtenionUris         | String      | YtiCode -> externalReferences("HierarchyExtensionUri")                | Associates given Yti Codelist Extensions as Hierarchies to this Explicit Domain
MemberXBRLCodePrefix          | String      | YtiCode -> ??                                                         | Optional prefix for Member's MemberCodes (used for enforcing MemberCodes XML element name validity)

#### Needed changes to Yti Reference Data -Service
- New External Reference types: `MembersCodelistUri`, `HierarchiesCodelistUri`, `HierarchyExtensionUri`
- Yti Code to provide storage for: `MemberXBRLCodePrefix` (with configurable MemberValues support for Yti Codes??)

<br>

### 2.2 Explict Domain Member

#### Structure mappig
Concept                                         | Source
----------------------------------------------- | -------------------------------------
Single Explict Domain Member                    | Yti Code
Members collection for single Explict Domain    | Single Yti Codelist associated to the Explict Domain via `MembersCodelistUri`

#### Explict Domain Member attributes
Attribute                     | Data type   | Value source                          | Notes
----------------------------- | ----------- | ------------------------------------- | -------------------------------------
Domain                        | Association | _Structure_                           | Domain this Member belongs to. Derived from `MembersCodelistUri` association.
MemberCode                    | String      | YtiCode -> codeValue                  |
MemberXBRLCode                | String      | _Computed_                            | "${Owner.prefix}_${Domain.DomainCode}:${MemberCode}"
MemberLabel                   | String      | YtiCode -> prefLabel                  |
IsDefaultMember               | _Computed_  |                                       | (YtiCodelist.defaultCode.codevalue == MemberCode)
Concept                       | Association | YtiCode                               | Timestamps, validity dates, etc

<br>

### 2.3 Hierarchy

#### Structure
Concept                                             | Source
--------------------------------------------------- | -------------------------------------
Single Hierarchy                                    | Yti Codelist Extension
Hierarchies collection for single Explict Domain    | All Yti Codelist Extensions which are associated to Explict Domain via `HierarchiesCodelistUri` or `HierarchyExtenionUris` and which are having PropertyType `definitionHierarchy` or `calculationHierarchy`

#### Hierarchy attributes
Attribute                    | Data type   | Source                               | Notes
---------------------------- | ----------- | ------------------------------------ | ------------------------------------
Domain                       | Association | _Structure_                          | Domain this Hierarchy relates to. Derived from `HierarchiesCodelistUri` and `HierarchyExtenionUris` associations
HierarchyCode                | String      | YtiCodelistExtension -> codeValue    |
HierarchyLabel               | String      | YtiCodelistExtension -> prefLabel    |
HierarchyDescription         | String      | _Fixed_                              | `NULL` for now
Concept                      | Association | YtiCodelistExtension                 | Timestamps, validity dates, etc

<br>

### 2.4 Hierarchy Node

#### Structure
Concept                                  | Source
---------------------------------------- | -------------------------------------
Single Hierarchy Node                    | Yti Codelist Extension Member + Yti Code (Explict Domain Member)
Nodes collection for single Hierarchy    | All Yti Codelist Extension Members present in Yti Codelist Extension.

#### Hierarchy Node attributes
Attribute            | Data type   | Value source                                                    | Notes
---------------------| ----------- | --------------------------------------------------------------- | -------------------------------------
Hierarchy            | Association | _Structure_                                                     | Hierarchy to which this Node belongs
Member               | Association | YtiCodelistExtensionMember -> code                              | Member this node represents
ParentMember         | String      | YtiCodelistExtensionMember -> parentMember() -> code            | Indicates the parent node, `NULL` for root level nodes
IsAbstract           | Boolean     | _Fixed_                                                         | `FALSE` for now
ComparisonOperator   | String      | YtiCodelistExtensionMember -> memberValue("ComparisonOperator") |
UnaryOperator        | String      | YtiCodelistExtensionMember -> memberValue("UnaryOperator")      |
Order                | String      | _Computed_                                                      | Computed from the order of Yti Codelist Extension Members appearance in Yti Codelist Extension
Level                | String      | _Computed_                                                      | Computed from the hierarchical structure of Yti Codelist Extension Members in Yti Codelist Extension
Path                 | String      | _Fixed_                                                         | `NULL` for now
Concept              | Association | YtiCodelistExtensionMember                                      | Timestamps, validity dates, etc

<br>

### 2.5 Explict Dimension

#### Structure
Concept                                 | Source
--------------------------------------- | -------------------------------------
Single Explict Dimension                | Yti Codelist Extension Member + Yti Code (Explict Domain)
Complete Explict Dimensiona collection  | Single Yti Codelist Extension listing all Explicit Dimensions

#### Explict Dimension attributes
Attribute                   | Data type   | Value source                                                | Notes
----------------------------| ----------- | ----------------------------------------------------------- | -------------------------------------
Domain                      | Association | YtiCodelistExtensionMember -> code                          | Explicit Domain from which the allowable values for this Explict Dimension are taken
DimensionCode               | String      | YtiCodelistExtensionMember -> memberValue("DimensionCode")  |
DimensionXBRLCode           | String      | _Computed_                                                  | "${Owner.prefix}_dim:${DimensionCode}"
DimensionLabel              | String      | YtiCodelistExtensionMember -> prefLabel                     |
DimensionDescription        | String      | _Fixed_                                                     | `NULL` for now
IsTypedDimension            | Boolean     | _Fixed_                                                     | `FALSE` for Explict Dimension
Concept                     | Association | YtiCodelistExtensionMember                                  | Timestamps, validity dates, etc

#### Needed changes to Yti Reference Data -Service
- New Yti Codelist Extension & PropertyType for it: `dpmDimension`
- New memberValue PropertyType: `dpmDimensionCode`

<br>

## 3 DPM "typed" concepts
![Mapping Typed Dimensions and Typed Domains to YTI](images/typed-dimensions-domains-yti-mapping.png)

<br>

### 3.1 Typed Domain

#### Structure
Concept                             | Source
----------------------------------- | -------------------------------------
Single Typed Domain                 | Yti Code
Complete Typed Domains collection   | Single Yti Codelist (associated as `Typed Domains` in DPM Dictionary Config) listing all Typed Domains

#### Typed Domain attributes
Attribute                     | Data type   | Value source                         | Notes
----------------------------- | ----------- | ------------------------------------ | --------------------------------------
DomainCode                    | String      | YtiCode.codeValue                    |
DomainXBRLCode                | String      | _Computed_                           | "${Owner.prefix}_typ:${DomainCode}" 
DomainLabel                   | String      | YtiCode -> prefLabel                 |
DomainDescription             | String      | YtiCode -> description               |
DataType                      | String      | YtiCode -> ??                        | 
IsTypedDomain                 | Boolean     | _Fixed_                              | `TRUE` for Typed Domains
Concept                       | Association | YtiCode                              | Timestamps, validity dates, etc

#### Needed changes to Yti Reference Data -Service
- Yti Code to provide storage for: `DataType` (with configurable MemberValues support for Yti Codes??)

<br>

### 3.2 Typed Dimension

#### Structure
Concept                               | Source
------------------------------------- | -------------------------------------
Single Typed Dimension                | Yti Codelist Extension Member + Yti Code (Typed Domain)
Complete Typed Dimension collection   | Single Yti Codelist Extension (associated as `Typed Dimensions` in DPM Dictionary Config) listing all Typed Dimensions

#### Typed Dimension attributes
Attribute                   | Data type   | Value source                                                | Notes
----------------------------| ----------- | ----------------------------------------------------------- | -------------------------------------
Domain                      | Association | YtiCodelistExtensionMember -> code                          | Typed Domain from which the allowable values for this Typed Dimension are taken
DimensionCode               | String      | YtiCodelistExtensionMember -> memberValue("DimensionCode")  |
DimensionXBRLCode           | String      | _Computed_                                                  | "${Owner.prefix}_dim:${DimensionCode}"
DimensionLabel              | String      | YtiCodelistExtensionMember -> prefLabel                     |
DimensionDescription        | String      | _Fixed_                                                     | `NULL` for now
IsTypedDimension            | Boolean     | _Fixed_                                                     | `TRUE` for Typed Dimension
Concept                     | Association | YtiCodelistExtensionMember                                  | Timestamps, validity dates, etc

#### Needed changes to Yti Reference Data -Service
- New Yti Codelist Extension & PropertyType for it: `dpmDimension`
- New memberValue PropertyType: `dpmDimensionCode`
