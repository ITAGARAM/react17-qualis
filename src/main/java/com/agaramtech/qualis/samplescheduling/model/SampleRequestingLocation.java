//package com.agaramtech.qualis.samplescheduling.model;
//
//public class SampleRequestingLocation {
//
//}
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
@Table(name = "samplerequestinglocation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleRequestingLocation extends CustomizedResultsetRowMapper<SampleRequestingLocation>
implements Serializable, RowMapper<SampleRequestingLocation> { 
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nsamplerequestinglocationcode")
	private int nsamplerequestinglocationcode;
	
	@Column(name = "nsamplerequestingcode")
	private int nsamplerequestingcode;
	
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
	private transient String sstatename = "";
	@Transient
	private transient String sregionname = "";
	@Transient
	private transient String sdistrictname = "";
	@Transient
	private transient String scityname = "";
	@Transient
	private transient String svillagename = "";
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
	public SampleRequestingLocation mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleRequestingLocation objSampleRequestingLocation  = new SampleRequestingLocation();
		
		objSampleRequestingLocation.setNsamplerequestinglocationcode(getInteger(arg0, "nsamplerequestinglocationcode", arg1));
		objSampleRequestingLocation.setNsitehierarchyconfigcode(getInteger(arg0, "nsitehierarchyconfigcode", arg1));
		objSampleRequestingLocation.setNtransdisplaystatus(getInteger(arg0, "ntransdisplaystatus", arg1));
		objSampleRequestingLocation.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objSampleRequestingLocation.setNtransdisplaystatus(getInteger(arg0, "ntransdisplaystatus", arg1));
		objSampleRequestingLocation.setNcolorcode(getInteger(arg0, "ncolorcode", arg1));
		objSampleRequestingLocation.setNsamplerequestingcode(getInteger(arg0, "nsamplerequestingcode", arg1));
		objSampleRequestingLocation.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		objSampleRequestingLocation.setStransactiondate(getString(arg0, "stransactiondate", arg1));
		objSampleRequestingLocation.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleRequestingLocation.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objSampleRequestingLocation.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objSampleRequestingLocation.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleRequestingLocation.setNstatus(getShort(arg0, "nstatus", arg1));
		objSampleRequestingLocation.setNregioncode(getInteger(arg0, "nregioncode", arg1));
		objSampleRequestingLocation.setNdistrictcode(getInteger(arg0, "ndistrictcode", arg1));
		objSampleRequestingLocation.setNcitycode(getInteger(arg0, "ncitycode", arg1));
		objSampleRequestingLocation.setNvillagecode(getInteger(arg0, "nvillagecode", arg1));
		objSampleRequestingLocation.setNsamplelocationcode(getInteger(arg0, "nsamplelocationcode", arg1));
		objSampleRequestingLocation.setSregionname(getString(arg0, "sregionname", arg1));
		objSampleRequestingLocation.setSdistrictname(getString(arg0, "sdistrictname", arg1));
		objSampleRequestingLocation.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objSampleRequestingLocation.setScityname(getString(arg0, "scityname", arg1));
		objSampleRequestingLocation.setSvillagename(getString(arg0, "svillagename", arg1));
		objSampleRequestingLocation.setSsamplelocationname(getString(arg0, "ssamplelocationname", arg1));
		objSampleRequestingLocation.setSremarks(getString(arg0, "sremarks", arg1));
		objSampleRequestingLocation.setSstatename(getString(arg0, "sstatename", arg1));
		objSampleRequestingLocation.setNcentralsitecode(getInteger(arg0, "ncentralsitecode", arg1));

		return objSampleRequestingLocation;
	}

	
	
	
	
}
