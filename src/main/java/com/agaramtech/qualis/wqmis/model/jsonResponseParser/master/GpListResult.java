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
public class GpListResult {
	
	@JsonProperty("GrampanchayatName")
	private String grampanchayatName;
	@JsonProperty("PanchayatId")
	private int panchayatId;

}
