package com.agaramtech.qualis.wqmis.model.jsonResponseParser.master;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
//ate225 committed by Mohammed Ashik on 21-11-2025 for jjmreagentsdata
//SWSM-122 WQMIS Branch creation for inetgartion
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReagentsListResult {
	
	@JsonProperty("reagent_id")
	private String reagent_id;
	@JsonProperty("reagent_name")
	private String reagent_name;

}
