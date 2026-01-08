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
 * This class is used to map the fields of 'invoiceproductfile' table of the
 * Database.
 */
@Entity
@Table(name = "invoiceproductfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceProductFile extends CustomizedResultsetRowMapper<InvoiceProductFile>
		implements Serializable, RowMapper<InvoiceProductFile> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nproductmasterfilecode")
	private int nproductmasterfilecode;
	@Column(name = "nproductcode", nullable = false)
	private int nproductcode;
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
	public InvoiceProductFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		InvoiceProductFile objProductMasterFile = new InvoiceProductFile();
		objProductMasterFile.setNproductmasterfilecode(getInteger(arg0, "nproductmasterfilecode", arg1));
		objProductMasterFile.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objProductMasterFile.setNlinkcode(getShort(arg0, "nlinkcode", arg1));
		objProductMasterFile.setNattachmenttypecode(getShort(arg0, "nattachmenttypecode", arg1));
		objProductMasterFile.setSfilename(getString(arg0, "sfilename", arg1));
		objProductMasterFile.setSdescription(getString(arg0, "sdescription", arg1));
		objProductMasterFile.setNfilesize(getInteger(arg0, "nfilesize", arg1));
		objProductMasterFile.setDcreateddate(getInstant(arg0, "dcreateddate", arg1));
		objProductMasterFile.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		objProductMasterFile.setNstatus(getShort(arg0, "nstatus", arg1));
		objProductMasterFile.setSlinkname(getString(arg0, "slinkname", arg1));
		objProductMasterFile.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objProductMasterFile.setSattachmenttype(getString(arg0, "sattachmenttype", arg1));
		objProductMasterFile.setScreateddate(getString(arg0, "screateddate", arg1));
		objProductMasterFile.setSfilename(getString(arg0, "sfilename", arg1));
		objProductMasterFile.setNoffsetdcreateddate(getInteger(arg0, "noffsetdcreateddate", arg1));
		objProductMasterFile.setNtzcreateddate(getShort(arg0, "ntzcreateddate", arg1));
		objProductMasterFile.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objProductMasterFile.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return objProductMasterFile;
	}

}
