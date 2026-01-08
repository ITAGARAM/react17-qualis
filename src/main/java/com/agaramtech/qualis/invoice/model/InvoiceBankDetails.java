package com.agaramtech.qualis.invoice.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'invoicebankdetails' table of the
 * Database.
 */
@Entity
@Table(name = "invoicebankdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceBankDetails extends CustomizedResultsetRowMapper<InvoiceBankDetails>
		implements Serializable, RowMapper<InvoiceBankDetails> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbankcode")
	private int nbankcode;

	@Column(name = "sbankname", length = 100, nullable = false)
	private String sbankname;

	@Column(name = "saccountno", length = 30, nullable = false)
	private String saccountno;

	@Column(name = "sifsccode", length = 30)
	private String sifsccode = "";

	@Column(name = "saddress", length = 250)
	private String saddress = "";

	@Column(name = "sotherdetails", length = 250)
	private String sotherdetails = "";

	@Column(name = "nusercode")
	private int nusercode;

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nactive")
	private int nactive;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	private transient String sdisplaystatus;

	@Override
	public InvoiceBankDetails mapRow(ResultSet arg0, int arg1) throws SQLException {

		final InvoiceBankDetails objBankDetails = new InvoiceBankDetails();

		objBankDetails.setNbankcode(getInteger(arg0, "nbankcode", arg1));
		objBankDetails.setSbankname(StringEscapeUtils.unescapeJava(getString(arg0, "sbankname", arg1)));
		objBankDetails.setSaccountno(StringEscapeUtils.unescapeJava(getString(arg0, "saccountno", arg1)));
		objBankDetails.setSifsccode(StringEscapeUtils.unescapeJava(getString(arg0, "sifsccode", arg1)));
		objBankDetails.setSaddress(StringEscapeUtils.unescapeJava(getString(arg0, "saddress", arg1)));
		objBankDetails.setSotherdetails(StringEscapeUtils.unescapeJava(getString(arg0, "sotherdetails", arg1)));
		objBankDetails.setNsitecode(getShort(arg0, "nusercode", arg1));
		objBankDetails.setNactive(getShort(arg0, "nactive", arg1));
		objBankDetails.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objBankDetails.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBankDetails.setNstatus(getShort(arg0, "nstatus", arg1));
		objBankDetails.setSdisplaystatus(StringEscapeUtils.unescapeJava(getString(arg0, "sdisplaystatus", arg1)));
		return objBankDetails;
	}

}