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
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'invoiceirn' table of the Database.
 */
@Entity
@Table(name = "invoiceirn")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceIrn extends CustomizedResultsetRowMapper<InvoiceIrn>
		implements Serializable, RowMapper<InvoiceIrn> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ninvoiceirnseqcode")
	private int ninvoiceirnseqcode;

	@Column(name = "sinvoiceno")
	private String sinvoiceno;

	@Column(name = "sirnno")
	private String sirnno;

	@Column(name = "nackno")
	private int nackno;

	@Column(name = "dackdate")
	private Instant dackdate;

	@Column(name = "sirnstatus", length = 20, nullable = false)
	private String sirnstatus;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nusercode", nullable = false)
	private short nusercode;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	@Column(name = "sirnstatusmessage", length = 20, nullable = false)
	private String sirnstatusmessage;

	@Override
	public InvoiceIrn mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub

		final InvoiceIrn objInvoiceIrn = new InvoiceIrn();

		objInvoiceIrn.setNinvoiceirnseqcode(getInteger(arg0, "ninvoiceirnseqcode", arg1));
		objInvoiceIrn.setSinvoiceno(StringEscapeUtils.unescapeJava(getString(arg0, "sinvoiceno", arg1)));
		objInvoiceIrn.setSirnno(StringEscapeUtils.unescapeJava(getString(arg0, "sirnno", arg1)));
		objInvoiceIrn.setNackno(getInteger(arg0, "nacko", arg1));
		objInvoiceIrn.setDackdate(getInstant(arg0, "dackdate", arg1));
		objInvoiceIrn.setSirnstatus(StringEscapeUtils.unescapeJava(getString(arg0, "sirnstatus", arg1)));
		objInvoiceIrn.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInvoiceIrn.setNusercode(getShort(arg0, "nusercode", arg1));
		objInvoiceIrn.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInvoiceIrn.setNstatus(getShort(arg0, "nstatus", arg1));
		objInvoiceIrn.setSirnstatusmessage(StringEscapeUtils.unescapeJava(getString(arg0, "sirnstatusmessage", arg1)));
		return objInvoiceIrn;

	}

}