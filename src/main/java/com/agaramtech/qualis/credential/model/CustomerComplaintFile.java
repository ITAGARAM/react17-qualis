package com.agaramtech.qualis.credential.model;

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
@Table(name = "customercomplaintfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CustomerComplaintFile extends CustomizedResultsetRowMapper<CustomerComplaintFile>
		implements Serializable, RowMapper<CustomerComplaintFile>

{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ncustomercomplaintfilecode")
	private int ncustomercomplaintfilecode;

	@Column(name = "ncustomercomplaintcode", nullable = false)
	private int ncustomercomplaintcode;

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
	public CustomerComplaintFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		final CustomerComplaintFile objCustomerComplaintFile = new CustomerComplaintFile();

		objCustomerComplaintFile.setNcustomercomplaintfilecode(getInteger(arg0, "ncustomercomplaintfilecode", arg1));
		objCustomerComplaintFile.setNcustomercomplaintcode(getInteger(arg0, "ncustomercomplaintcode", arg1));
		objCustomerComplaintFile.setNlinkcode(getShort(arg0, "nlinkcode", arg1));
		objCustomerComplaintFile.setNattachmenttypecode(getShort(arg0, "nattachmenttypecode", arg1));
		objCustomerComplaintFile.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0, "sfilename", arg1)));
		objCustomerComplaintFile.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objCustomerComplaintFile.setNfilesize(getInteger(arg0, "nfilesize", arg1));
		objCustomerComplaintFile.setDcreateddate(getInstant(arg0, "dcreateddate", arg1));
		objCustomerComplaintFile.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		objCustomerComplaintFile.setNstatus(getShort(arg0, "nstatus", arg1));
		objCustomerComplaintFile.setSlinkname(getString(arg0, "slinkname", arg1));
		objCustomerComplaintFile.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objCustomerComplaintFile.setSattachmenttype(getString(arg0, "sattachmenttype", arg1));
		objCustomerComplaintFile.setScreateddate(getString(arg0, "screateddate", arg1));
		objCustomerComplaintFile.setSfilename(getString(arg0, "sfilename", arg1));
		objCustomerComplaintFile.setNoffsetdcreateddate(getInteger(arg0, "noffsetdcreateddate", arg1));
		objCustomerComplaintFile.setNtzcreateddate(getShort(arg0, "ntzcreateddate", arg1));
		objCustomerComplaintFile.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objCustomerComplaintFile.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objCustomerComplaintFile.setNapprovalstatus(getInteger(arg0, "napprovalstatus", arg1));
		objCustomerComplaintFile.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));

		return objCustomerComplaintFile;
	}

}
