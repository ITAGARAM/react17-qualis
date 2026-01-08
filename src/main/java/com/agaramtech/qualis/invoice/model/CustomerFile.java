package com.agaramtech.qualis.invoice.model;

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

/**
 * This class is used to map the fields of 'customerfile' table of the Database.
 */
@Entity
@Table(name = "customerfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CustomerFile extends CustomizedResultsetRowMapper<CustomerFile>
		implements Serializable, RowMapper<CustomerFile> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ncustomerfilecode")
	private int ncustomerfilecode;
	@Column(name = "ncustomercode", nullable = false)
	private int ncustomercode;
	@ColumnDefault("-1")
	@Column(name = "nlinkcode", nullable = false)
	private short nlinkcode;
	@Column(name = "nattachmenttypecode", nullable = false)
	private short nattachmenttypecode;
	@Column(name = "sfilename", length = 100, nullable = false)
	private String sfilename;
	@Column(name = "sdescription", length = 255)
	private String sdescription;
	@Column(name = "nfilesize", nullable = false)
	private int nfilesize;
	@Column(name = "dcreateddate")
	private Instant dcreateddate;
	@Column(name = "ntzcreateddate")
	private short ntzcreateddate;
	@Column(name = "noffsetdcreateddate", nullable = false)
	private int noffsetdcreateddate;
	@Column(name = "ssystemfilename", length = 100)
	private String ssystemfilename;
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus;
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	private transient String slinkname;
	private transient String stransdisplaystatus;
	private transient String sattachmenttype;
	private transient String screateddate;
	private transient String sfilesize;

	@Override
	public CustomerFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		CustomerFile objCustomerFile = new CustomerFile();
		objCustomerFile.setNcustomerfilecode(getInteger(arg0, "ncustomerfilecode", arg1));
		objCustomerFile.setNcustomercode(getInteger(arg0, "ncustomercode", arg1));
		objCustomerFile.setNlinkcode(getShort(arg0, "nlinkcode", arg1));
		objCustomerFile.setNattachmenttypecode(getShort(arg0, "nattachmenttypecode", arg1));
		objCustomerFile.setSfilename(getString(arg0, "sfilename", arg1));
		objCustomerFile.setSdescription(getString(arg0, "sdescription", arg1));
		objCustomerFile.setNfilesize(getInteger(arg0, "nfilesize", arg1));
		objCustomerFile.setDcreateddate(getInstant(arg0, "dcreateddate", arg1));
		objCustomerFile.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		objCustomerFile.setNstatus(getShort(arg0, "nstatus", arg1));
		objCustomerFile.setSlinkname(getString(arg0, "slinkname", arg1));
		objCustomerFile.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objCustomerFile.setSattachmenttype(getString(arg0, "sattachmenttype", arg1));
		objCustomerFile.setScreateddate(getString(arg0, "screateddate", arg1));
		objCustomerFile.setSfilename(getString(arg0, "sfilename", arg1));
		objCustomerFile.setNoffsetdcreateddate(getInteger(arg0, "noffsetdcreateddate", arg1));
		objCustomerFile.setNtzcreateddate(getShort(arg0, "ntzcreateddate", arg1));
		objCustomerFile.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objCustomerFile.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return objCustomerFile;
	}

}
