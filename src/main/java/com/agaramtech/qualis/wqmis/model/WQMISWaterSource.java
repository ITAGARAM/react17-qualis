package com.agaramtech.qualis.wqmis.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
//ate199 committed by DhivyaBharathi on 21-11-2025 for jjmwatersource
		//SWSM-122 WQMIS Branch creation for inetgartion
@Entity
@Table(name = "jjmwatersource")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WQMISWaterSource extends CustomizedResultsetRowMapper<WQMISWaterSource>
implements Serializable, RowMapper<WQMISWaterSource> {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "stateid")
	private int stateid;
	
	@Column(name = "districtid",nullable = false)
	private int districtid;

	@Column(name = "districtname", nullable = false)
	private String districtname;
	
	@Column(name = "blockid",nullable = false)
	private int blockid;
	
	@Column(name = "blockname", nullable = false)
	private String blockname;
	
	@Column(name = "panchayatid",nullable = false)
	private int panchayatid;
	
	@Column(name = "panchayatname", nullable = false)
	private String panchayatname;
	
	@Column(name = "villageid",nullable = false)
	private int villageid;
	
	@Column(name = "villagename", nullable = false)
	private String villagename;
	
	@Column(name = "habitationid",nullable = false)
	private int habitationid;
	
	@Column(name = "habitationname", nullable = false)
	private String habitationname;
	
	@Column(name = "sourceid",nullable = false)
	private int sourceid;

	@Column(name = "location", nullable = false)
	private String location;
	
	@Column(name = "sourcetypecategoryid",nullable = false)
	private int sourcetypecategoryid;

	@Column(name = "sourcetypecategory", nullable = false)
	private String sourcetypecategory;
	
	@Column(name = "sourcetypeid",nullable = false)
	private int sourcetypeid;

	@Column(name = "sourcetype", length = 50, nullable = false)
	private String sourcetype;

	@Column(name = "responseon", length=25, nullable = false)
	private String responseon;
	
	@Column(name = "schemeid", length=25, nullable = false)
	private String schemeid;
	
	@Column(name = "schemename", nullable = false)
	private String schemename;
	
	@Column(name = "latitude",nullable = false)
	private int latitude;
	
	@Column(name = "longitude",nullable = false)
	private int longitude;
	
	@Column(name = "pws_fhtcstatus",nullable = false)
	private int pws_fhtcstatus;
	
	@Column(name = "nflagstatus", nullable = false)
	@ColumnDefault("0")
	private short nflagstatus;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus;

	@Override
	public WQMISWaterSource mapRow(ResultSet arg0, int arg1) throws SQLException {

		WQMISWaterSource objWaterSource = new WQMISWaterSource();

		objWaterSource.setStateid(getInteger(arg0, "stateid", arg1));
		objWaterSource.setDistrictid(getInteger(arg0, "districtid", arg1));
		objWaterSource.setDistrictname(getString(arg0, "districtname", arg1));
		objWaterSource.setBlockid(getInteger(arg0, "blockid", arg1));
		objWaterSource.setBlockname(getString(arg0, "blockname", arg1));
		objWaterSource.setPanchayatid(getInteger(arg0, "panchayatid", arg1));
		objWaterSource.setPanchayatname(getString(arg0, "panchayatname", arg1));
		objWaterSource.setVillageid(getInteger(arg0, "villageid", arg1));
		objWaterSource.setVillagename(getString(arg0, "villagename", arg1));
		objWaterSource.setHabitationid(getInteger(arg0, "habitationid", arg1));
		objWaterSource.setHabitationname(getString(arg0, "habitationname", arg1));
		objWaterSource.setSourceid(getInteger(arg0, "sourceid", arg1));
		objWaterSource.setLocation(getString(arg0, "location", arg1));
		objWaterSource.setSourcetypecategoryid(getInteger(arg0, "sourcetypecategoryid", arg1));
		objWaterSource.setSourcetypecategory(getString(arg0, "sourcetypecategory", arg1));
		objWaterSource.setSourcetypeid(getInteger(arg0, "sourcetypeid", arg1));
		objWaterSource.setSourcetype(getString(arg0, "sourcetype", arg1));
		objWaterSource.setResponseon(getString(arg0, "responseon", arg1));
		objWaterSource.setSchemeid(getString(arg0, "schemeid", arg1));
		objWaterSource.setSchemename(getString(arg0, "schemename", arg1));
		objWaterSource.setLatitude(getInteger(arg0, "latitude", arg1));
		objWaterSource.setLongitude(getInteger(arg0, "longitude", arg1));
		objWaterSource.setPws_fhtcstatus(getInteger(arg0, "pws_fhtcstatus", arg1));
		objWaterSource.setNflagstatus(getShort(arg0, "nflagstatus", arg1));
		objWaterSource.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objWaterSource.setNstatus(getShort(arg0, "nstatus", arg1));

		return objWaterSource;
	}

}
