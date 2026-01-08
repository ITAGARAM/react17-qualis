package com.agaramtech.qualis.wqmis.model.jsonResponseParser.transaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WQMISLabParameterResults {
	@JsonProperty("TestId") 
	public int testId;	
	@JsonProperty("ParameterId") 
	public int parameterId;
	@JsonProperty("ParameterValue") 
	public String parameterValue;
}
