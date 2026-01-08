package com.agaramtech.qualis.wqmis.model.jsonResponseParser.transaction;

import java.util.ArrayList;
import java.util.Date;

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
public class WQMISContaminatedFTKSampleResults {
	
	@JsonProperty("TestId")
    private int ntestid;

    @JsonProperty("SampleId")
    private String ssampleid;

    @JsonProperty("Test_date")
    private Date dtesteddate;

    @JsonProperty("Location")
    private String slocation;

    @JsonProperty("is_safe")
    private String sissafe;

    @JsonProperty("lab_id")
    private int nlabid;

    @JsonProperty("SourceId")
    private int nsourceid;

    @JsonProperty("Source_type")
    private String ssourcetype;

    @JsonProperty("Source_subtype")
    private String ssourcesubtype;

    @JsonProperty("SchemeId")
    private String sschemeid;

    @JsonProperty("SampleLocationfrom")
    private String ssamplelocationfrom;

    @JsonProperty("remedial_action_status")
    private String sremedialactionstatus;

    @JsonProperty("VillageId")
    private int nvillageid;

    @JsonProperty("HabitationId")
    private int nhabitationid;

    @JsonProperty("StateLGD_code")
    private int nstatelgccode;

    @JsonProperty("DistrictLGD_Code")
    private int ndistrictlgdcode;

    @JsonProperty("BlockLGD_Code")
    private int nblocklgdcode;

    @JsonProperty("PanchyatLGD_Code")
    private int npanchayatlgdcode;

    @JsonProperty("VillageLGD_Code")
    private int nvillagelgdcode;

    @JsonProperty("ParameterResult")
    private ArrayList<WQMISContaminatedFTKSampleParameterResults> parameterResult;

}
