package com.agaramtech.qualis.wqmis.model.jsonResponseParser.master;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
//ate135 committed by Dhanalakshmi on 21-11-2025 for Getlabdata
		//SWSM-122 WQMIS Branch creation for inetgartion
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WqmisLabDataResult {

	@JsonProperty("lab_id")
	private int lab_id;
	@JsonProperty("lab_name")
	private String lab_name;
	@JsonProperty("lab_type")
	private String lab_type;
	@JsonProperty("lab_group")
	private String lab_group;
	@JsonProperty("latitude")
	private Double latitude;
	@JsonProperty("longitude")
	private Double longitude;
}
