package com.agaramtech.qualis.invoice.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'invoiceschemes' table of the
 * Database.
 */
@Entity
@Table(name = "invoiceschemes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceSchemes extends CustomizedResultsetRowMapper<InvoiceSchemes>
		implements Serializable, RowMapper<InvoiceSchemes> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nschemecode")
	private int nschemecode;

	@Column(name = "sschemename", length = 100, nullable = false)
	private String sschemename;

	@Column(name = "dfromdate")
	private Instant dfromdate;

	@Column(name = "dtodate")
	private Instant dtodate;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	private transient String stransdisplaystatus;
	private transient String sfromdate;
	private transient String stodate;

	@Override
	public InvoiceSchemes mapRow(ResultSet arg0, int arg1) throws SQLException {
		InvoiceSchemes objInvoiceSchemes = new InvoiceSchemes();
		objInvoiceSchemes.setNschemecode(getInteger(arg0, "nschemecode", arg1));
		objInvoiceSchemes.setSschemename(getString(arg0, "sschemename", arg1));
		objInvoiceSchemes.setDfromdate(getInstant(arg0, "dfromdate", arg1));
		objInvoiceSchemes.setDtodate(getInstant(arg0, "dtodate", arg1));
		objInvoiceSchemes.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objInvoiceSchemes.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInvoiceSchemes.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInvoiceSchemes.setNstatus(getShort(arg0, "nstatus", arg1));
		objInvoiceSchemes.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objInvoiceSchemes.setSfromdate(getString(arg0, "sfromdate", arg1));
		objInvoiceSchemes.setStodate(getString(arg0, "stodate", arg1));

		return objInvoiceSchemes;
	}

}
