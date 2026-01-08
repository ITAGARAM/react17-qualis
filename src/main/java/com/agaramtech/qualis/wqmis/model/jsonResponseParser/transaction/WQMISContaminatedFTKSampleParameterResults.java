package com.agaramtech.qualis.wqmis.model.jsonResponseParser.transaction;

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
public class WQMISContaminatedFTKSampleParameterResults {
	
	@JsonProperty("TestId")
	private int ntestid;
	
	@JsonProperty("ParameterId")
	private int nparameterid;
	
	@JsonProperty("ParameterValue")
	private String sparametervalue;

}
