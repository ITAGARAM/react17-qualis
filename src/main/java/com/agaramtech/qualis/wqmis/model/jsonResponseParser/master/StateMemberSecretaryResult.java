package com.agaramtech.qualis.wqmis.model.jsonResponseParser.master;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
//ate225 committed by Mohammed Ashik on 21-11-2025 for Getdwsm_state_member_secretary
//SWSM-122 WQMIS Branch creation for inetgartion
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StateMemberSecretaryResult {
	
	@JsonProperty("user_id")
	private int user_id;
	@JsonProperty("first_name")
	private String first_name;
	@JsonProperty("last_name")
	private String last_name;
	@JsonProperty("mobile")
	private String mobile;
	@JsonProperty("email")
	private String email;
	@JsonProperty("state")
	private String state;
	@JsonProperty("district")
	private String district; 

}
