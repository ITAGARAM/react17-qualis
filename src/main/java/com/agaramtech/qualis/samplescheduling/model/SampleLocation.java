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
 * @author sujatha.v AT-E274
 * SWSM-5 24/07/2025  
 */
@Entity
@Table(name = "samplelocation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleLocation extends CustomizedResultsetRowMapper<SampleLocation> implements Serializable, RowMapper<SampleLocation>{

    private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nsamplelocationcode")
	private int nsamplelocationcode;
	
	// added by sujatha ATE_274 whose value is from the sitehierarchyconfig 18-09-2025
	@Column(name = "nsitehierarchyconfigcode", nullable = false)
	private int nsitehierarchyconfigcode;
	
	// added by sujatha ATE_274 for added extra field in the UI Central Site
	@Column(name = "ncentralsitecode")
	private int ncentralsitecode;
	
	@Column(name = "nregioncode")
	private int nregioncode;
	
	@Column(name = "ndistrictcode")
	private int ndistrictcode;
	
	@Column(name = "ncitycode")
	private int ncitycode;

	@Column(name = "nvillagecode")
	private int nvillagecode;

	@Column(name = "ssamplelocationname", length = 150, nullable = false)
	private String ssamplelocationname;
	
    //modified by Sujatha for SWSM-5 on 05/08/2025 modified length from 150 to 15
	@Column(name = "slatitude", length = 15)
	private String slatitude="";

    //modified by Sujatha for SWSM-5 on 05/08/2025 modified length from 150 to 15
	@Column(name = "slongitude", length = 15)
	private String slongitude="";
	
	@Column(name= "sdescription", length= 300)
	private String sdescription="";
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sregionname;
	
	@Transient
	private transient String sdistrictname;
	
	@Transient
	private transient String scityname;

	@Transient
	private transient String svillagename;
	
	@Transient
	private transient String smodifieddate;
	
	//added by sujatha ATE_274 SWSM-5 for Sample location enhancement is based on site hierarchy configuration
	@Transient
	private transient int ntransactionstatus;
	
	@Transient
	private transient String ssitename;
	
	@Transient
	private transient String srelation;
	
	@Transient
	private transient String ssitetypename;
	
	@Transient
	private transient String scentralsitename;
	
	@Transient
	private transient int nlevel;
	
//	@Transient
//	private transient int nsitehierarchyconfigcode;
	
	@Override
	public SampleLocation mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleLocation objSampleLocation = new SampleLocation();
		objSampleLocation.setNsamplelocationcode(getInteger(arg0, "nsamplelocationcode", arg1));
		objSampleLocation.setNcentralsitecode(getInteger(arg0, "ncentralsitecode", arg1));
		objSampleLocation.setNregioncode(getInteger(arg0, "nregioncode", arg1));
		objSampleLocation.setNdistrictcode(getInteger(arg0, "ndistrictcode", arg1));
		objSampleLocation.setNcitycode(getInteger(arg0, "ncitycode", arg1));
		objSampleLocation.setNvillagecode(getInteger(arg0, "nvillagecode", arg1));
		objSampleLocation.setSsamplelocationname(getString(arg0, "ssamplelocationname", arg1));
		objSampleLocation.setSlatitude(getString(arg0, "slatitude", arg1));
		objSampleLocation.setSlongitude(getString(arg0, "slongitude", arg1));
		objSampleLocation.setSdescription(getString(arg0, "sdescription", arg1));
		objSampleLocation.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleLocation.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleLocation.setNstatus(getShort(arg0, "nstatus", arg1));
		objSampleLocation.setSregionname(getString(arg0, "sregionname", arg1));
		objSampleLocation.setSdistrictname(getString(arg0, "sdistrictname", arg1));
		objSampleLocation.setScityname(getString(arg0, "scityname", arg1));
		objSampleLocation.setSvillagename(getString(arg0, "svillagename", arg1));
		objSampleLocation.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		//added by sujatha ATE_274 SWSM-5 for Sample location enhancement is based on site hierarchy configuration
		objSampleLocation.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objSampleLocation.setSsitename(getString(arg0, "ssitename", arg1));
		objSampleLocation.setSrelation(getString(arg0, "srelation", arg1));
		objSampleLocation.setSsitetypename(getString(arg0, "ssitetypename", arg1));
		objSampleLocation.setScentralsitename(getString(arg0, "scentralsitename", arg1));
		objSampleLocation.setNlevel(getInteger(arg0, "nlevel", arg1));
		objSampleLocation.setNsitehierarchyconfigcode(getInteger(arg0, "nsitehierarchyconfigcode", arg1));
		return objSampleLocation;
	}
}
