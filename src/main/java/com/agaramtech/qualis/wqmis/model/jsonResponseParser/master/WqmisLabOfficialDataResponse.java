package com.agaramtech.qualis.wqmis.model.jsonResponseParser.master;

import java.util.ArrayList;

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
public class WqmisLabOfficialDataResponse {


	@JsonProperty("Status")
	private Boolean status;
	@JsonProperty("Message")
	private String message;
	@JsonProperty("data_result")
	private ArrayList<WqmisLabOfficialDataResult> data_result;

}