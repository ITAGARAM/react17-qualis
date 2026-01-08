package com.agaramtech.qualis.wqmis.model.jsonResponseParser.master;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)

//ate135 committed by Dhanalakshmi on 21-11-2025 for GetSample_submitter
		//SWSM-122 WQMIS Branch creation for inetgartion
public class WqmisSamplesubmitterResult {
	
	
	
	@JsonProperty("user_id")
	private int user_id;
	
	@JsonProperty("first_name")
	private String first_name;
	
	@JsonProperty("last_name")
	private String last_name;
	
	@JsonProperty("user_type")
	private String user_type;
	
	@JsonProperty("mobile")
	private String mobile;
	
	@JsonProperty("email")
	private String email;
	
	@JsonProperty("state")
	private String state;
	
	
	@JsonProperty("district")
	private String district;
	
	@JsonProperty("block")
	private String block;
	
	
	@JsonProperty("gp")
	private String gp;

	
	@JsonProperty("village_town")
	private String village_town;
	
	@JsonProperty("area")
	private String area;
	
	
	@JsonProperty("house_no")
	private String house_no;

	
	@JsonProperty("pin_code")
	private String pin_code;




}
