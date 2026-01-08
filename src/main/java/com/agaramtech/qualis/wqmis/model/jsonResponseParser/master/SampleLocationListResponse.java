package com.agaramtech.qualis.wqmis.model.jsonResponseParser.master;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
//ate225 committed by Mohammed Ashik on 21-11-2025 for GetSample_Location
//SWSM-122 WQMIS Branch creation for inetgartion
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SampleLocationListResponse {
	@JsonProperty("Status")
	private Boolean status;
	@JsonProperty("Message")
	private String message;
	@JsonProperty("data_result")
	private ArrayList<SampleLocationListResult> sampleLocationList;

}
