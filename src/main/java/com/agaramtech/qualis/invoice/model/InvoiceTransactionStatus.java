package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


import org.apache.commons.text.StringEscapeUtils;

import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

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
public class InvoiceTransactionStatus extends CustomizedResultsetRowMapper<InvoiceTransactionStatus>
		implements Serializable, RowMapper<InvoiceTransactionStatus> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntransactionid")
	private int ntransactionid;

	@Column(name = "sreferenceid", length = 100, nullable = false)
	private String sreferenceid;

	@Column(name = "nreferencetype", nullable = false)
	private int nreferencetype;

	@Column(name = "sdraftuser", length = 100, nullable = false)
	private String sdraftuser;

	@Column(name = "ddraftdate")
	private Instant ddraftdate;

	@Column(name = "sapproveduser", length = 100)
	private String sapproveduser;

	@Column(name = "dapproveddate")
	private Instant dapproveddate;

	@Column(name = "sinvoiceduser", length = 100)
	private String sinvoiceduser;

	@Column(name = "dinvoiceddate")
	private Instant dinvoiceddate;

	@Column(name = "ntransactionstatus", nullable = false)
	private int ntransactionstatus;

	@Column(name = "sref1", length = 500)
	private String sref1;

	@Column(name = "sref2", length = 500)
	private String sref2;

	@Column(name = "sext1", length = 500)
	private String sext1;

	@Column(name = "sext2", length = 500)
	private String sext2;

	@Column(name = "nstatus")
	private Short nstatus;

	private transient String simageid;

	@Override
	public InvoiceTransactionStatus mapRow(ResultSet arg0, int arg1) throws SQLException {
		final InvoiceTransactionStatus objInvoiceTransactionStatus = new InvoiceTransactionStatus();

		objInvoiceTransactionStatus.setNtransactionid(getInteger(arg0, "ntransactionid", arg1));
		objInvoiceTransactionStatus
				.setSreferenceid(StringEscapeUtils.unescapeJava(getString(arg0, "sreferenceid", arg1)));
		objInvoiceTransactionStatus.setNreferencetype(getInteger(arg0, "nreferencetype", arg1));
		objInvoiceTransactionStatus.setSdraftuser(StringEscapeUtils.unescapeJava(getString(arg0, "sdraftuser", arg1)));
		objInvoiceTransactionStatus.setDdraftdate(getInstant(arg0, "ddraftdate", arg1));
		objInvoiceTransactionStatus
				.setSapproveduser(StringEscapeUtils.unescapeJava(getString(arg0, "sapproveduser", arg1)));
		objInvoiceTransactionStatus.setDapproveddate(getInstant(arg0, "dapproveddate", arg1));
		objInvoiceTransactionStatus
				.setSinvoiceduser(StringEscapeUtils.unescapeJava(getString(arg0, "sinvoiceduser", arg1)));
		objInvoiceTransactionStatus.setDinvoiceddate(getInstant(arg0, "dinvoiceddate", arg1));
		objInvoiceTransactionStatus.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objInvoiceTransactionStatus.setSref1(StringEscapeUtils.unescapeJava(getString(arg0, "sref1", arg1)));
		objInvoiceTransactionStatus.setSref2(StringEscapeUtils.unescapeJava(getString(arg0, "sref2", arg1)));
		objInvoiceTransactionStatus.setSext1(StringEscapeUtils.unescapeJava(getString(arg0, "sext1", arg1)));
		objInvoiceTransactionStatus.setSext2(StringEscapeUtils.unescapeJava(getString(arg0, "sext2", arg1)));
		objInvoiceTransactionStatus.setNstatus(getShort(arg0, "nstatus", arg1));
		objInvoiceTransactionStatus.setSimageid(StringEscapeUtils.unescapeJava(getString(arg0, "simageid", arg1)));
		return objInvoiceTransactionStatus;

	}

}
