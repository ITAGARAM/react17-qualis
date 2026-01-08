package com.agaramtech.qualis.wqmis.model.jsonResponseParser.transaction;

import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WQMISLabSampleResults {
	@JsonProperty("TestId") 
    public int testId;
    @JsonProperty("SampleId") 
    public String sampleId;
    @JsonProperty("Test_date") 
    public Date test_date;
    @JsonProperty("Location") 
    public String location;
    @JsonProperty("is_safe") 
    public String is_safe;
    @JsonProperty("lab_id") 
    public int lab_id;
    @JsonProperty("SourceId") 
    public int sourceId;
    @JsonProperty("Source_type") 
    public String source_type;
    @JsonProperty("Source_subtype") 
    public String source_subtype;
    @JsonProperty("SchemeId") 
    public String schemeId;
    @JsonProperty("SampleLocationfrom") 
    public String sampleLocationfrom;
    @JsonProperty("remedial_action_status") 
    public String remedial_action_status;
    @JsonProperty("VillageId") 
    public int villageId;
    @JsonProperty("HabitationId") 
    public int habitationId;
    @JsonProperty("StateLGD_code") 
    public int stateLGD_code;
    @JsonProperty("DistrictLGD_Code") 
    public int districtLGD_Code;
    @JsonProperty("BlockLGD_Code") 
    public int blockLGD_Code;
    @JsonProperty("PanchyatLGD_Code") 
    public int panchyatLGD_Code;
    @JsonProperty("VillageLGD_Code") 
    public int villageLGD_Code;
    @JsonProperty("ParameterResult") 
    public ArrayList<WQMISLabParameterResults> parameterResult;
}
