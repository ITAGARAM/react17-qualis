package com.agaramtech.qualis.samplescheduling.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
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

@Entity
@Table(name = "samplerequesting")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleRequesting extends CustomizedResultsetRowMapper<SampleRequesting>
implements Serializable, RowMapper<SampleRequesting>
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsamplerequestingcode")
	private int nsamplerequestingcode;
	
	@Column(name = "nsampleschedulingcode")
	private int nsampleschedulingcode;
	
	@Column(name = "sfromyear", length = 150, nullable = false)
	private String sfromyear;
	
	@Column(name = "sassignedto", length = 150)
	private String sassignedto = "";

    //modified semail Length by MullaiBalaji  
	@Column(name = "semail", length = 100, nullable = false)
	private String semail = "";
	
	@Column(name = "scontactnumber", length = 30, nullable = false)
	private String scontactnumber = "";
	
	@ColumnDefault("-1")
	@Column(name = "ntzcollectiondate", nullable = false)
	private short ntzcollectiondate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "noffsetdcollectiondate", nullable = false)
	private int noffsetdcollectiondate;
	
	@Column(name = "dcollectiondate", nullable = false)
	private Instant dcollectiondate;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	
	@Transient
	private transient int nregioncode;
	
	@Transient
	private transient int ndistrictcode;
	
	@Transient
	private transient int ncitycode;
	
	@Transient
	private transient int nvillagecode;
	
	@Transient
	private transient int nsamplelocationcode;
	
	@Transient
	private transient int nsamplerequestinghistorycode;
	@Transient
	private transient String sregionname;
	@Transient
	private transient String sdistrictname ;
	@Transient
	private transient String scityname ;
	@Transient
	private transient String smonthname ;
	@Transient
	private transient String svillagename ;
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
	private transient int ntzsampledate;
	@Transient
	private transient String sremarks ;
	@Transient
	private transient int ncolorcode;
	@Transient
	private transient int ntransactionstatus;
	@Transient
	private transient String scolorhexcode ;
	@Transient
	private transient String ssitename;
	@Transient
	private transient String ssitetypename;
	@Transient
	private transient String srelation;
	@Transient
	private transient int nlevel;
	@Transient
	private transient int nsitehierarchyconfigcode;	
	@Transient
	private transient int nsamplerequestinglocationcode;	

	@Transient
	private transient String ssamplelocationname;
	@Transient
	private transient String scollectiondate ;
	@Transient
	private transient String sscheduleddate ;
	@Transient
	private transient String scompleteddate ;
	
	@Transient
	private transient int ncentralsitecode;
	@Transient
	private transient String sstatename;
	
	@Transient
	private transient String speriod ;
	
	@Override
	public SampleRequesting mapRow(ResultSet arg0, int arg1) throws SQLException {

		
		final SampleRequesting objSampleRequesting = new SampleRequesting();
		objSampleRequesting.setNsamplerequestingcode(getInteger(arg0, "nsamplerequestingcode", arg1));
		objSampleRequesting.setNsampleschedulingcode(getInteger(arg0, "nsampleschedulingcode", arg1));
		objSampleRequesting.setNsitehierarchyconfigcode(getInteger(arg0, "nsitehierarchyconfigcode", arg1));
		objSampleRequesting.setNtransdisplaystatus(getInteger(arg0, "ntransdisplaystatus", arg1));
		objSampleRequesting.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objSampleRequesting.setSemail(StringEscapeUtils.unescapeJava(getString(arg0, "semail", arg1)));
		objSampleRequesting.setNlevel(getInteger(arg0, "nlevel", arg1));
		objSampleRequesting.setScontactnumber(StringEscapeUtils.unescapeJava(getString(arg0, "scontactnumber", arg1)));
		objSampleRequesting.setSstatename(getString(arg0, "sstatename", arg1));
		objSampleRequesting.setNoffsetdcollectiondate(getInteger(arg0, "noffsetdcollectiondate", arg1));
		objSampleRequesting.setNcolorcode(getInteger(arg0, "ncolorcode", arg1));
		objSampleRequesting.setNsamplerequestinghistorycode(getInteger(arg0, "nsamplerequestinghistorycode", arg1));
		objSampleRequesting.setNsamplelocationcode(getInteger(arg0, "nsamplelocationcode", arg1));
		objSampleRequesting.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		objSampleRequesting.setSassignedto(StringEscapeUtils.unescapeJava(getString(arg0, "sassignedto", arg1)));
		objSampleRequesting.setSfromyear(getString(arg0, "sfromyear", arg1));
		objSampleRequesting.setStransactiondate(getString(arg0, "stransactiondate", arg1));
		objSampleRequesting.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleRequesting.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objSampleRequesting.setNcentralsitecode(getInteger(arg0, "ncentralsitecode", arg1));
		objSampleRequesting.setNregioncode(getInteger(arg0, "nregioncode", arg1));
		objSampleRequesting.setNdistrictcode(getInteger(arg0, "ndistrictcode", arg1));
		objSampleRequesting.setNcitycode(getInteger(arg0, "ncitycode", arg1));
		objSampleRequesting.setNvillagecode(getInteger(arg0, "nvillagecode", arg1));
		objSampleRequesting.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objSampleRequesting.setNtzsampledate(getInteger(arg0, "ntzsampledate", arg1));
		objSampleRequesting.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleRequesting.setNstatus(getShort(arg0, "nstatus", arg1));
		objSampleRequesting.setSdistrictname(getString(arg0, "sdistrictname", arg1));
		objSampleRequesting.setSmonthname(getString(arg0, "smonthname", arg1));
		objSampleRequesting.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objSampleRequesting.setScityname(getString(arg0, "scityname", arg1));
		objSampleRequesting.setSvillagename(getString(arg0, "svillagename", arg1));
		objSampleRequesting.setSremarks(getString(arg0, "sremarks", arg1));
		objSampleRequesting.setSregionname(getString(arg0, "sregionname", arg1));
		objSampleRequesting.setSsitename(getString(arg0, "ssitename", arg1));
		objSampleRequesting.setSsitetypename(getString(arg0, "ssitetypename", arg1));
		objSampleRequesting.setSrelation(getString(arg0, "srelation", arg1));
		objSampleRequesting.setNsamplerequestinglocationcode(getInteger(arg0, "nsamplerequestinglocationcode", arg1));
		objSampleRequesting.setNtzcollectiondate(getShort(arg0, "ntzcollectiondate", arg1));
		objSampleRequesting.setSsamplelocationname(getString(arg0, "ssamplelocationname", arg1));
		objSampleRequesting.setDcollectiondate(getInstant(arg0, "dcollectiondate", arg1));
		objSampleRequesting.setScollectiondate(getString(arg0, "scollectiondate", arg1));
		objSampleRequesting.setSscheduleddate(getString(arg0, "sscheduleddate", arg1));
		objSampleRequesting.setScompleteddate(getString(arg0, "scompleteddate", arg1));
		objSampleRequesting.setSperiod(getString(arg0, "speriod", arg1));

		return objSampleRequesting;
	}
	
}

