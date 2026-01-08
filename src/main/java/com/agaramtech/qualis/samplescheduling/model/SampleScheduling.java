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

/**
 * This class is used to map the fields of 'samplescheduling' table of the
 * Database.
 * 
 * @author Mullai Balaji.V [SWSM-17] Sample Scheduling - Screen Development -
 *         Agaram Technologies
 * 
 */
@Entity
@Table(name = "samplescheduling")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleScheduling extends CustomizedResultsetRowMapper<SampleScheduling>
		implements Serializable, RowMapper<SampleScheduling> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsampleschedulingcode")
	private int nsampleschedulingcode;

	@Column(name = "sfromyear", length = 150, nullable = false)
	private String sfromyear;

	@Column(name = "speriod", length = 150)
	private String speriod = "";

	@Column(name = "sdescription", length = 255)
	private String sdescription = "";

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient int nsampleschedulinghistorycode;
	//added by sujatha ATE_274 for added a new field ncentralsitecode
	@Transient
	private transient String scentralsitename = "";
	@Transient
	private transient String sregionname = "";
	@Transient
	private transient String sdistrictname = "";
	@Transient
	private transient String scityname = "";
	@Transient
	private transient String smonthname = "";

	@Transient
	private transient String svillagename = "";

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
	private transient String sremarks = "";

	@Transient
	private transient int ncolorcode;

	@Transient
	private transient int ntransactionstatus;

	@Transient
	private transient String scolorhexcode = "";

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
	@Override
	public SampleScheduling mapRow(ResultSet arg0, int arg1) throws SQLException {

		final SampleScheduling objSampleScheduling = new SampleScheduling();
		objSampleScheduling.setNsampleschedulingcode(getInteger(arg0, "nsampleschedulingcode", arg1));
		objSampleScheduling.setNsitehierarchyconfigcode(getInteger(arg0, "nsitehierarchyconfigcode", arg1));
		objSampleScheduling.setNtransdisplaystatus(getInteger(arg0, "ntransdisplaystatus", arg1));
		objSampleScheduling.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objSampleScheduling.setNlevel(getInteger(arg0, "nlevel", arg1));
		objSampleScheduling.setNtransdisplaystatus(getInteger(arg0, "ntransdisplaystatus", arg1));
		objSampleScheduling.setNcolorcode(getInteger(arg0, "ncolorcode", arg1));
		objSampleScheduling.setNsampleschedulinghistorycode(getInteger(arg0, "nsampleschedulinghistorycode", arg1));
		objSampleScheduling.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		objSampleScheduling.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objSampleScheduling.setSfromyear(getString(arg0, "sfromyear", arg1));
		objSampleScheduling.setStransactiondate(getString(arg0, "stransactiondate", arg1));
		objSampleScheduling.setSperiod(StringEscapeUtils.unescapeJava(getString(arg0, "speriod", arg1)));
		objSampleScheduling.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleScheduling.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objSampleScheduling.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objSampleScheduling.setNtzsampledate(getInteger(arg0, "ntzsampledate", arg1));
		objSampleScheduling.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleScheduling.setNstatus(getShort(arg0, "nstatus", arg1));
		objSampleScheduling.setSdistrictname(getString(arg0, "sdistrictname", arg1));
		objSampleScheduling.setSmonthname(getString(arg0, "smonthname", arg1));
		objSampleScheduling.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objSampleScheduling.setScityname(getString(arg0, "scityname", arg1));
		objSampleScheduling.setSvillagename(getString(arg0, "svillagename", arg1));
		objSampleScheduling.setSremarks(getString(arg0, "sremarks", arg1));
		objSampleScheduling.setSregionname(getString(arg0, "sregionname", arg1));
		objSampleScheduling.setSsitename(getString(arg0, "ssitename", arg1));
		objSampleScheduling.setSsitetypename(getString(arg0, "ssitetypename", arg1));
		objSampleScheduling.setSrelation(getString(arg0, "srelation", arg1));
		//added by sujatha ATE_274 for added a new field ncentralsitecode
		objSampleScheduling.setScentralsitename(getString(arg0, "scentralsitename", arg1));

		return objSampleScheduling;
	}

}
