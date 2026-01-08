package com.agaramtech.qualis.wqmis.model.jsonResponseParser.master;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
//ate225 committed by Mohammed Ashik on 21-11-2025 for Getstate_level_mapping
//SWSM-122 WQMIS Branch creation for inetgartion
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WQMISStateLevelMappingResult {
	
	@JsonProperty("lab_id")
	private String lab_id;
	@JsonProperty("parameter_id")
	private String parameter_id;
	@JsonProperty("method_id")
	private String method_id;
	@JsonProperty("equipment_id")
	private String equipment_id;
	@JsonProperty("reagent_id")
	private String reagent_id;

}
