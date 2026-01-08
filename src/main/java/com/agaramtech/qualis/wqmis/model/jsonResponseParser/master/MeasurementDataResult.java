package com.agaramtech.qualis.wqmis.model.jsonResponseParser.master;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

//ate199 committed by DhivyaBharathi on 21-11-2025 for GetMeasurement_methods_data
		//SWSM-122 WQMIS Branch creation for inetgartion
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeasurementDataResult {


 
	@JsonProperty("mm_id")
	private int mm_id;
	@JsonProperty("mm_name")
	private String mm_name;
	
}