
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
@Table(name = "samplerequestingfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleRequestingFile extends CustomizedResultsetRowMapper<SampleRequestingFile>
		implements Serializable, RowMapper<SampleRequestingFile>

{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsamplerequestingfilecode")
	private int nsamplerequestingfilecode;

	@Column(name = "nsamplerequestingcode", nullable = false)
	private int nsamplerequestingcode;

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
	public SampleRequestingFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleRequestingFile objSampleRequestingFile = new SampleRequestingFile();

		objSampleRequestingFile.setNsamplerequestingfilecode(getInteger(arg0, "nsamplerequestingfilecode", arg1));
		objSampleRequestingFile.setNsamplerequestingcode(getInteger(arg0, "nsamplerequestingcode", arg1));
		objSampleRequestingFile.setNlinkcode(getShort(arg0, "nlinkcode", arg1));
		objSampleRequestingFile.setNattachmenttypecode(getShort(arg0, "nattachmenttypecode", arg1));
		objSampleRequestingFile.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0, "sfilename", arg1)));
		objSampleRequestingFile.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objSampleRequestingFile.setNfilesize(getInteger(arg0, "nfilesize", arg1));
		objSampleRequestingFile.setDcreateddate(getInstant(arg0, "dcreateddate", arg1));
		objSampleRequestingFile.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		objSampleRequestingFile.setNstatus(getShort(arg0, "nstatus", arg1));
		objSampleRequestingFile.setSlinkname(getString(arg0, "slinkname", arg1));
		objSampleRequestingFile.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objSampleRequestingFile.setSattachmenttype(getString(arg0, "sattachmenttype", arg1));
		objSampleRequestingFile.setScreateddate(getString(arg0, "screateddate", arg1));
		objSampleRequestingFile.setSfilename(getString(arg0, "sfilename", arg1));
		objSampleRequestingFile.setNoffsetdcreateddate(getInteger(arg0, "noffsetdcreateddate", arg1));
		objSampleRequestingFile.setNtzcreateddate(getShort(arg0, "ntzcreateddate", arg1));
		objSampleRequestingFile.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleRequestingFile.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleRequestingFile.setNapprovalstatus(getInteger(arg0, "napprovalstatus", arg1));
		objSampleRequestingFile.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));

		return objSampleRequestingFile;
	}

}
