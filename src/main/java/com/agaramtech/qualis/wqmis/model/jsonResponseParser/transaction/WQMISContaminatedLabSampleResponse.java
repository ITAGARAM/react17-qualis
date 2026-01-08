package com.agaramtech.qualis.wqmis.model.jsonResponseParser.transaction;

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
public class WQMISContaminatedLabSampleResponse {
	
	@JsonProperty("Status")
	private Boolean status;
	@JsonProperty("Message")
	private String message;
	@JsonProperty("Result")
	private ArrayList<WQMISContaminatedLabSampleResults> sampleResult; 

}
