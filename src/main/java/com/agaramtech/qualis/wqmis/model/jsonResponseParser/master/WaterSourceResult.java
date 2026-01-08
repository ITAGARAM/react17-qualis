package com.agaramtech.qualis.wqmis.model.jsonResponseParser.master;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
//ate199 committed by DhivyaBharathi on 21-11-2025 for GetWaterSource
		//SWSM-122 WQMIS Branch creation for inetgartion
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)

public class WaterSourceResult {

	   @JsonProperty("StateId")
	    private int stateid;

	    @JsonProperty("DistrictId")
	    private int districtid;

	    @JsonProperty("DistrictName")
	    private String districtname;

	    @JsonProperty("BlockId")
	    private int blockid;

	    @JsonProperty("BlockName")
	    private String blockname;

	    @JsonProperty("PanchayatId")
	    private int panchayatid;

	    @JsonProperty("PanchayatName")
	    private String panchayatname;

	    @JsonProperty("VillageId")
	    private int villageid;

	    @JsonProperty("VillageName")
	    private String villagename;

	    @JsonProperty("HabitationId")
	    private int habitationid;

	    @JsonProperty("HabitationName")
	    private String habitationname;

	    @JsonProperty("sourceId")
	    private long sourceid;

	    @JsonProperty("Location")
	    private String location;

	    @JsonProperty("SourceTypeCategoryId")
	    private int sourcetypecategoryid;

	    @JsonProperty("SourceTypeCategory")
	    private String sourcetypecategory;

	    @JsonProperty("SourceTypeId")
	    private int sourcetypeid;

	    @JsonProperty("SourceType")
	    private String sourcetype;

	    @JsonProperty("ResponseOn")
	    private String responseon;

	    @JsonProperty("SchemeId")
	    private String schemeid;

	    @JsonProperty("SchemeName")
	    private String schemename;

	    @JsonProperty("Latitude")
	    private double latitude;

	    @JsonProperty("Longitude")
	    private double longitude;

	    @JsonProperty("PWS_FHTCStatus")
	    private int pwsFhtcStatus;

}