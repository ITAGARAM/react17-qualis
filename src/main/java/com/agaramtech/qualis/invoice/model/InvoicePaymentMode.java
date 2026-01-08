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

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * This class is used to map the fields of 'invoicepaymentmode' table of the
 * Database.
 */

@Entity
@Table(name = "invoicepaymentmode")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoicePaymentMode extends CustomizedResultsetRowMapper<InvoicePaymentMode>
		implements Serializable, RowMapper<InvoicePaymentMode> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "npaymentcode")
	private int npaymentcode;

	@Column(name = "spaymentmode", length = 100, nullable = false)
	private String spaymentmode;

	@Column(name = "nusercode")
	private int nusercode;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	@Override
	public InvoicePaymentMode mapRow(ResultSet arg0, int arg1) throws SQLException {
		InvoicePaymentMode objInvoicePaymentMode = new InvoicePaymentMode();
		objInvoicePaymentMode.setNpaymentcode(getInteger(arg0, "npaymentcode", arg1));
		objInvoicePaymentMode.setSpaymentmode(getString(arg0, "spaymentmode", arg1));
		objInvoicePaymentMode.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInvoicePaymentMode.setNusercode(getInteger(arg0, "nusercode", arg1));
		objInvoicePaymentMode.setNstatus(getShort(arg0, "nstatus", arg1));
		objInvoicePaymentMode.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));

		return objInvoicePaymentMode;
	}
}
