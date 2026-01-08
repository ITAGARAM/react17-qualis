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
 * This class is used to map the fields of 'invoiceexportimport' table of the
 * Database.
 */
@Entity
@Table(name = "invoiceexportimport")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceExportImport extends CustomizedResultsetRowMapper<InvoiceExportImport>
		implements Serializable, RowMapper<InvoiceExportImport> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ncode", length = 50, nullable = false)
	private int ncode;

	@Column(name = "stablename", length = 50, nullable = false)
	private String stablename;

	@Column(name = "sscreenname", length = 50, nullable = false)
	private String sscreenname;

	@Column(name = "sexporturl", length = 50, nullable = false)
	private String sexporturl;

	@Column(name = "simporturl", length = 50, nullable = false)
	private String simporturl;

	@Column(name = "smergeurl", length = 50, nullable = false)
	private String smergeurl;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nmastersitecode", nullable = false)
	@ColumnDefault("-1")
	private short nmastersitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	private transient double ntax;
	private transient String staxname;
	private transient String sversionno;
	private transient double ncost;
	private transient int ntestgrouptestparametercode;
	private transient int nallottedspeccode;
	private transient String stestname;
	private transient int ntestcode;
	private transient String stestsynonym;
	private transient String sspecname;
	private transient int ncomponentcode;
	private transient String scomponentname;
	private transient String smethodname;

	@Column(name = "sdatastatus", length = 250, nullable = false)
	private String sdatastatus;

	@Override
	public InvoiceExportImport mapRow(ResultSet arg0, int arg1) throws SQLException {
		InvoiceExportImport objInvoiceExportImport = new InvoiceExportImport();
		objInvoiceExportImport.setNcode(getInteger(arg0, "ncode", arg1));
		objInvoiceExportImport.setStablename(getString(arg0, "stablename", arg1));
		objInvoiceExportImport.setSscreenname(getString(arg0, "sscreenname", arg1));
		objInvoiceExportImport.setSexporturl(getString(arg0, "sexporturl", arg1));
		objInvoiceExportImport.setSimporturl(getString(arg0, "simporturl", arg1));
		objInvoiceExportImport.setSmergeurl(getString(arg0, "smergeurl", arg1));
		objInvoiceExportImport.setNmastersitecode(getShort(arg0, "nmastersitecode", arg1));
		objInvoiceExportImport.setNstatus(getShort(arg0, "nstatus", arg1));
		objInvoiceExportImport.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInvoiceExportImport.setNtax(getDouble(arg0, "ntax", arg1));
		objInvoiceExportImport.setStaxname(getString(arg0, "staxname", arg1));
		objInvoiceExportImport.setSversionno(getString(arg0, "sversionno", arg1));
		objInvoiceExportImport.setSdatastatus(getString(arg0, "sdatastatus", arg1));
		objInvoiceExportImport.setStestname(getString(arg0, "stestname", arg1));
		objInvoiceExportImport.setStestsynonym(getString(arg0, "stestsynonym", arg1));
		objInvoiceExportImport.setNtestgrouptestparametercode(getInteger(arg0, "ntestgrouptestparametercode", arg1));
		objInvoiceExportImport.setNallottedspeccode(getInteger(arg0, "nallottedspeccode", arg1));
		objInvoiceExportImport.setNtestcode(getInteger(arg0, "ntestcode", arg1));
		objInvoiceExportImport.setNcost(getDouble(arg0, "ncost", arg1));
		objInvoiceExportImport.setSspecname(getString(arg0, "sspecname", arg1));
		objInvoiceExportImport.setNcomponentcode(getInteger(arg0, "ncomponentcode", arg1));
		objInvoiceExportImport.setScomponentname(getString(arg0, "scomponentname", arg1));
		objInvoiceExportImport.setSmethodname(getString(arg0, "smethodname", arg1));

		return objInvoiceExportImport;
	}

}