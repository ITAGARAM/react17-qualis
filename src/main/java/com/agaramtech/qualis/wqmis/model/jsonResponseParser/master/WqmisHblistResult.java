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
//ate135 committed by Dhanalakshmi on 21-11-2025 for getHblist
		//SWSM-122 WQMIS Branch creation for inetgartion

public class WqmisHblistResult {
	
	
	 @JsonProperty("HabitationId")
	    private Integer habitationId;

	    @JsonProperty("HabitationName")
	    private String habitationName;


}
