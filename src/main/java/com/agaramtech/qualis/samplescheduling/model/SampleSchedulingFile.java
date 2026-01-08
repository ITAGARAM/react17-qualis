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
 * This class is used to map the fields of 'sampleschedulingfile' table of the
 * Database.
 * 
 * @author Mullai Balaji.V [SWSM-17] Sample Scheduling - Screen Development -
 *         Agaram Technologies
 * 
 */
@Entity
@Table(name = "sampleschedulingfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleSchedulingFile extends CustomizedResultsetRowMapper<SampleSchedulingFile>
		implements Serializable, RowMapper<SampleSchedulingFile>

{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsampleschedulingfilecode")
	private int nsampleschedulingfilecode;

	@Column(name = "nsampleschedulingcode", nullable = false)
	private int nsampleschedulingcode;

	@ColumnDefault("-1")
	@Column(name = "nlinkcode", nullable = false)
	private short nlinkcode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nattachmenttypecode", nullable = false)
	private short nattachmenttypecode;

	@Column(name = "sfilename", length = 100, nullable = false)
	private String sfilename;

	@Column(name = "sdescription", length = 255)
	private String sdescription = "";

	@Column(name = "nfilesize", nullable = false)
	private int nfilesize;

	@Column(name = "dcreateddate")
	private Instant dcreateddate;

	@Column(name = "ntzcreateddate")
	private short ntzcreateddate;

	@Column(name = "noffsetdcreateddate", nullable = false)
	private int noffsetdcreateddate;

	@Column(name = "ssystemfilename", length = 100)
	private String ssystemfilename = "";

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient short ntransactionstatus;
	@Transient
	private transient String slinkname;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String sattachmenttype;
	@Transient
	private transient String screateddate;
	@Transient
	private transient String sfilesize;
	@Transient
	private transient int napprovalstatus;

	@Override
	public SampleSchedulingFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleSchedulingFile objSampleSchedulingFile = new SampleSchedulingFile();

		objSampleSchedulingFile.setNsampleschedulingfilecode(getInteger(arg0, "nsampleschedulingfilecode", arg1));
		objSampleSchedulingFile.setNsampleschedulingcode(getInteger(arg0, "nsampleschedulingcode", arg1));
		objSampleSchedulingFile.setNlinkcode(getShort(arg0, "nlinkcode", arg1));
		objSampleSchedulingFile.setNattachmenttypecode(getShort(arg0, "nattachmenttypecode", arg1));
		objSampleSchedulingFile.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0, "sfilename", arg1)));
		objSampleSchedulingFile.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objSampleSchedulingFile.setNfilesize(getInteger(arg0, "nfilesize", arg1));
		objSampleSchedulingFile.setDcreateddate(getInstant(arg0, "dcreateddate", arg1));
		objSampleSchedulingFile.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		objSampleSchedulingFile.setNstatus(getShort(arg0, "nstatus", arg1));
		objSampleSchedulingFile.setSlinkname(getString(arg0, "slinkname", arg1));
		objSampleSchedulingFile.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objSampleSchedulingFile.setSattachmenttype(getString(arg0, "sattachmenttype", arg1));
		objSampleSchedulingFile.setScreateddate(getString(arg0, "screateddate", arg1));
		objSampleSchedulingFile.setSfilename(getString(arg0, "sfilename", arg1));
		objSampleSchedulingFile.setNoffsetdcreateddate(getInteger(arg0, "noffsetdcreateddate", arg1));
		objSampleSchedulingFile.setNtzcreateddate(getShort(arg0, "ntzcreateddate", arg1));
		objSampleSchedulingFile.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleSchedulingFile.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleSchedulingFile.setNapprovalstatus(getInteger(arg0, "napprovalstatus", arg1));
		objSampleSchedulingFile.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));

		return objSampleSchedulingFile;
	}

}
