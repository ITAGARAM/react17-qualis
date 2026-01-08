package com.agaramtech.qualis.samplescheduling.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * This class is used to map the fields of 'sampleschedulinglocation' table of the
 * Database.
 * 
 * @author Mullai Balaji.V [SWSM-17] Sample Scheduling - Screen Development -
 *         Agaram Technologies
 * 
 */
@Entity
@Table(name = "sampleschedulinglocation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleSchedulingLocation extends CustomizedResultsetRowMapper<SampleSchedulingLocation>
implements Serializable, RowMapper<SampleSchedulingLocation> { 
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nsampleschedulinglocationcode")
	private int nsampleschedulinglocationcode;
	
	@Column(name = "nsampleschedulingcode")
	private int nsampleschedulingcode;
	//added by sujatha ATE_274 for added a new field ncentralsitecode and for audit trail
	@Column(name = "ncentralsitecode", nullable = false) 
	private int ncentralsitecode;
	
	@Column(name = "nregioncode", nullable = false)
	private int nregioncode;
	
	@Column(name = "ndistrictcode", nullable = false)
	private int ndistrictcode;
	
	@Column(name = "ncitycode", nullable = false)
	private int ncitycode;
	
	@Column(name = "nvillagecode", nullable = false)
	private int nvillagecode;
	
	@Column(name = "nsamplelocationcode", nullable = false)
	private int nsamplelocationcode;
	
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	

	
	@Transient
	private transient String sregionname = "";
	@Transient
	private transient String sdistrictname = "";
	@Transient
	private transient String scityname = "";
	@Transient
	private transient String svillagename = "";
  //added by sujatha ATE_274 27-09-2025 for added a new field ncentralsitecode
	@Transient
	private transient String scentralsitename = "";
	@Transient
	private transient String ssamplelocationname="";
	@Transient
	private transient String stransactiondate;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient int ntransdisplaystatus;
	@Transient
	private transient Instant dtransactiondate;
	@Transient
	private transient short ntztransactiondate;
	@Transient
	private transient String sremarks = "";
	@Transient
	private transient int ncolorcode;
	@Transient
	private transient int ntransactionstatus;
	@Transient
	private transient String scolorhexcode = "";
	@Transient
	private transient int nsitehierarchyconfigcode;
	
	@Override
	public SampleSchedulingLocation mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleSchedulingLocation objSampleSchedulingLocation  = new SampleSchedulingLocation();
		
		objSampleSchedulingLocation.setNsampleschedulinglocationcode(getInteger(arg0, "nsampleschedulinglocationcode", arg1));
		objSampleSchedulingLocation.setNsitehierarchyconfigcode(getInteger(arg0, "nsitehierarchyconfigcode", arg1));
		objSampleSchedulingLocation.setNtransdisplaystatus(getInteger(arg0, "ntransdisplaystatus", arg1));
		objSampleSchedulingLocation.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objSampleSchedulingLocation.setNtransdisplaystatus(getInteger(arg0, "ntransdisplaystatus", arg1));
		objSampleSchedulingLocation.setNcolorcode(getInteger(arg0, "ncolorcode", arg1));
		objSampleSchedulingLocation.setNsampleschedulingcode(getInteger(arg0, "nsampleschedulingcode", arg1));
		objSampleSchedulingLocation.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		objSampleSchedulingLocation.setStransactiondate(getString(arg0, "stransactiondate", arg1));
		objSampleSchedulingLocation.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleSchedulingLocation.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objSampleSchedulingLocation.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objSampleSchedulingLocation.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleSchedulingLocation.setNstatus(getShort(arg0, "nstatus", arg1));
		objSampleSchedulingLocation.setNregioncode(getInteger(arg0, "nregioncode", arg1));
		objSampleSchedulingLocation.setNdistrictcode(getInteger(arg0, "ndistrictcode", arg1));
		objSampleSchedulingLocation.setNcitycode(getInteger(arg0, "ncitycode", arg1));
		objSampleSchedulingLocation.setNvillagecode(getInteger(arg0, "nvillagecode", arg1));
		objSampleSchedulingLocation.setNsamplelocationcode(getInteger(arg0, "nsamplelocationcode", arg1));
		objSampleSchedulingLocation.setSregionname(getString(arg0, "sregionname", arg1));
		objSampleSchedulingLocation.setSdistrictname(getString(arg0, "sdistrictname", arg1));
		objSampleSchedulingLocation.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objSampleSchedulingLocation.setScityname(getString(arg0, "scityname", arg1));
		objSampleSchedulingLocation.setSvillagename(getString(arg0, "svillagename", arg1));
		objSampleSchedulingLocation.setSsamplelocationname(getString(arg0, "ssamplelocationname", arg1));
		objSampleSchedulingLocation.setSremarks(getString(arg0, "sremarks", arg1));
		//added by sujatha ATE_274 27-09-2025 for added a new field ncentralsitecode & for audit trial
		objSampleSchedulingLocation.setNcentralsitecode(getInteger(arg0, "ncentralsitecode", arg1));
		objSampleSchedulingLocation.setScentralsitename(getString(arg0, "scentralsitename", arg1));
		return objSampleSchedulingLocation;
	}
}
