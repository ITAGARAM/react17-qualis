

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
public class ParametersDataFTKUserListResult {
	@JsonProperty("parameter_id")
	private String parameter_id;
	@JsonProperty("parameter_name")
	private String parameter_name;
	@JsonProperty("MeasurementUnit")
	private String MeasurementUnit;
	@JsonProperty("Acceptablelimit")
	private String Acceptablelimit;
	@JsonProperty("Permissiblelimit")
	private String Permissiblelimit;
	@JsonProperty("Value_type")
	private String Value_type;
	@JsonProperty("Value_type_Description")
	private String Value_type_Description;

}
