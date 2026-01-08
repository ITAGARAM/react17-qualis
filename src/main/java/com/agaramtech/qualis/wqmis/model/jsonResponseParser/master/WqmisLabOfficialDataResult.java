package com.agaramtech.qualis.wqmis.model.jsonResponseParser.master;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
//ate199 committed by DhivyaBharathi on 21-11-2025 for getLabOfficial
		//SWSM-122 WQMIS Branch creation for inetgartion
		 
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WqmisLabOfficialDataResult {


 
	@JsonProperty("user_id")
	private int user_id;
	@JsonProperty("lab_id")
	private int lab_id;
	@JsonProperty("first_name")
	private String first_name;
	@JsonProperty("last_name")
	private String last_name;
	@JsonProperty("role")
	private String role;
	@JsonProperty("designation")
	private String designation;
	@JsonProperty("mobile")
	private String mobile;
}