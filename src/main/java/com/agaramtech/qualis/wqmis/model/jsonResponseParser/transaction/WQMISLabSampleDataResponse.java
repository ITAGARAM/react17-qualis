package com.agaramtech.qualis.wqmis.model.jsonResponseParser.transaction;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WQMISLabSampleDataResponse {
	
	@JsonProperty("Status") 
    public boolean status;
    @JsonProperty("Message") 
    public String message;
    @JsonProperty("Result") 
    public ArrayList<WQMISLabSampleResults> result;

}
