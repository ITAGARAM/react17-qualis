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
public class LabDataListResult {
	@JsonProperty("lab_id")
	private String lab_id;
	@JsonProperty("lab_name")
	private String lab_name;
	@JsonProperty("lab_type")
	private String lab_type;
	@JsonProperty("lab_group")
	private String lab_group;
	@JsonProperty("latitude")
	private float latitude;
	@JsonProperty("longitude")
	private float longitude;
}
