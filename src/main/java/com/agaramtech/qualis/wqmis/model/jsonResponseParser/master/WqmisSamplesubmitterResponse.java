package com.agaramtech.qualis.wqmis.model.jsonResponseParser.master;

import java.util.ArrayList;

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
public class WqmisSamplesubmitterResponse {
	
	
	@JsonProperty("Status")
	private Boolean status;
	@JsonProperty("Message")
	private String message;
	@JsonProperty("DB_error_code")
	private int DB_error_code;
	@JsonProperty("total_result")
	private int total_result;
	@JsonProperty("data_result")
	private ArrayList<WqmisSamplesubmitterResult> data_result;

}
