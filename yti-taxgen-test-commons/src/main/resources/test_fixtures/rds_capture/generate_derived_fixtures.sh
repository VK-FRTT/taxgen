#!/bin/sh

# This script generates altered test fixtures from existing integration_fixture -capture

clone_from_integration_fixture(){
  TARGET_FOLDER=$1
  RETAINED_CONCEPT=$2

  rm -r $TARGET_FOLDER
  cp -R "integration_fixture" $TARGET_FOLDER
  rm -r ${TARGET_FOLDER}/meta

  for CONCEPT_NAME in exp_dim exp_dom_hier met typ_dim typ_dom; do
    if [ $CONCEPT_NAME != $RETAINED_CONCEPT ]
    then
    rm -r ${TARGET_FOLDER}/dpm_dictionary_0/${CONCEPT_NAME}
    fi
  done
}


set_scalar_in_json(){
  FILE=$1
  LOCATION=$2
  VALUE=$3
  TEMP_FILE=${FILE}.tmp

  jq "$LOCATION=\"$VALUE\"" $FILE > $TEMP_FILE
  mv $TEMP_FILE $FILE
}

set_object_in_json(){
  FILE=$1
  LOCATION=$2
  VALUE=$3
  TEMP_FILE=${FILE}.tmp

  jq "$LOCATION=$VALUE" $FILE > $TEMP_FILE
  mv $TEMP_FILE $FILE
}



# DpmElement content validation fixtures
clone_from_integration_fixture "nonvalid_dpm_elements_metric" "met"
set_scalar_in_json "nonvalid_dpm_elements_metric/dpm_dictionary_0/met/codes_page_0.json" 	            ".results[4].codeValue"		            "6"
set_scalar_in_json "nonvalid_dpm_elements_metric/dpm_dictionary_0/met/extension_3/members_page_0.json" 	".results[0].memberValues[0].value"		"UnsupportedMetricDataType"
set_scalar_in_json "nonvalid_dpm_elements_metric/dpm_dictionary_0/met/extension_3/members_page_0.json" 	".results[4].memberValues[0].value"		"Integer"


clone_from_integration_fixture "nonvalid_dpm_elements_explicit_domain" "exp_dom_hier"
set_scalar_in_json "nonvalid_dpm_elements_explicit_domain/dpm_dictionary_0/exp_dom_hier/codes_page_0.json" 	                                ".results[0].codeValue"		        "DOME-TooLongCode-1234567890123456789012345678901234567890"
set_scalar_in_json "nonvalid_dpm_elements_explicit_domain/dpm_dictionary_0/exp_dom_hier/sub_code_list_6/codes_page_0.json" 	                ".results[0].codeValue"		        "EDA-x1-##-NonValidDpmCode"
set_scalar_in_json "nonvalid_dpm_elements_explicit_domain/dpm_dictionary_0/exp_dom_hier/sub_code_list_6/extension_1/members_page_0.json"    ".results[0].memberValues[0].value"	"UnsupportedComparisonOp"
set_scalar_in_json "nonvalid_dpm_elements_explicit_domain/dpm_dictionary_0/exp_dom_hier/sub_code_list_6/extension_1/extension_meta.json"    ".codeValue"		                "EDA-H2-TooLongCode-1234567890123456789012345678901234567890"


clone_from_integration_fixture "nonvalid_dpm_elements_explicit_dimension" "exp_dim"
set_scalar_in_json "nonvalid_dpm_elements_explicit_dimension/dpm_dictionary_0/exp_dim/codes_page_0.json"   ".results[0].codeValue"     "DIM-TooLongCode-1234567890123456789012345678901234567890"


clone_from_integration_fixture "nonvalid_dpm_elements_typed_domain" "typ_dom"
set_scalar_in_json "nonvalid_dpm_elements_typed_domain/dpm_dictionary_0/typ_dom/extension_0/members_page_0.json"   ".results[0].memberValues[0].value"     "UnsupportedTypeDomainDataType"


clone_from_integration_fixture "nonvalid_dpm_elements_typed_dimension" "typ_dim"
set_scalar_in_json "nonvalid_dpm_elements_typed_dimension/dpm_dictionary_0/typ_dim/codes_page_0.json"   ".results[0].codeValue"     "TDB-D1-TooLongCode-1234567890123456789012345678901234567890"


# Orphan HierarchyNode fixtures
clone_from_integration_fixture "nonvalid_orphan_extension_member" "exp_dom_hier"
set_scalar_in_json "nonvalid_orphan_extension_member/dpm_dictionary_0/exp_dom_hier/sub_code_list_6/extension_0/members_page_0.json" ".results[2].relatedMember.uri"		    "http://uri.suomi.fi/codelist/dpm-integration-fixture/EDA-2018-1/extension/EDA-H1/member/9"
set_scalar_in_json "nonvalid_orphan_extension_member/dpm_dictionary_0/exp_dom_hier/sub_code_list_6/extension_0/members_page_0.json" ".results[2].relatedMember.code.uri"    "http://uri.suomi.fi/codelist/dpm-integration-fixture/EDA-2018-1/code/EDA-x9-Missing"


# Broken JSON response content
clone_from_integration_fixture "nonvalid_codes_page_json" "exp_dim"
echo "non-json-content" > "nonvalid_codes_page_json/dpm_dictionary_0/exp_dim/codes_page_0.json"


# ExplicitDomain with Member code prefix
clone_from_integration_fixture "explicit_domain_with_member_code_prefix" "exp_dom_hier"
set_object_in_json "explicit_domain_with_member_code_prefix/dpm_dictionary_0/exp_dom_hier/extension_0/members_page_0.json" ".results[0]"  '{ "code" : { "uri" : "http://uri.suomi.fi/codelist/dpm-integration-fixture/exp-doms-2018-1/code/EDA" }, "memberValues" : [ { "value" : "code-prefix-", "valueType" : { "uri" : "http://uri.suomi.fi/datamodel/ns/code#dpmMemberXBRLCodePrefix" } } ] }'

# ExplicitDomain with HierarchyNode refering to external Member
clone_from_integration_fixture "explicit_domain_with_node_ref_to_external_member" "exp_dom_hier"
set_object_in_json "explicit_domain_with_node_ref_to_external_member/dpm_dictionary_0/exp_dom_hier/sub_code_list_6/extension_2/members_page_0.json" ".results[0]"  '{
    "uri" : "http://uri.suomi.fi/codelist/dpm-integration-fixture/EDA-2018-1/extension/EDA-H10/member/1",
    "created" : "2019-10-23T12:11:59.781Z",
    "modified" : "2019-10-23T12:11:59.781Z",
    "order" : 1,
    "code" : {
      "uri" : "http://uri.suomi.fi/codelist/UriReferringExternalCode"
    }
  }'
