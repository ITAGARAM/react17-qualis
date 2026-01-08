

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
public class ParameterDataList {

	@JsonProperty("Status")
	private Boolean Status;
	@JsonProperty("Message")
	private String Message;
	@JsonProperty("DB_error_code")
	private int DB_error_code;
	@JsonProperty("total_result")
	private int total_result;
	@JsonProperty("data_result")
	private ArrayList<ParameterDataListResult> data_result;
	
}
