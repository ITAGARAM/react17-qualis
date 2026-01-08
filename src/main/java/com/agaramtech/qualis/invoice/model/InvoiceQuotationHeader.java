package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

/**
 * This class is used to map the fields of 'invoicequotationheader' table of the
 * Database.
 */
@Entity
@Table(name = "invoicequotationheader")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceQuotationHeader extends CustomizedResultsetRowMapper<InvoiceQuotationHeader>
		implements Serializable, RowMapper<InvoiceQuotationHeader> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nquotationseqcode")
	private int nquotationseqcode;

	@Column(name = "squotationno", length = 50, nullable = false)
	private String squotationno;

	@Column(name = "dquotationdate")
	private Instant dquotationdate;

	@Column(name = "dquotationfromdate")
	private Instant dquotationfromdate;

	@Column(name = "dquotationtodate")
	private Instant dquotationtodate;

	@Column(name = "nconvertedinvoice")
	private int nconvertedinvoice;

	@Column(name = "ncustomercode ")
	private int ncustomercode;

	@Column(name = "sprojectcode", length = 50)
	private String sprojectcode;

	@Column(name = "sprojectname", length = 50)
	private String sprojectname;

	@Column(name = "stenderrefno", length = 500)
	private String stenderrefno;

	@Column(name = "dtenderrefdate", length = 500)
	private Instant dtenderrefdate;

	@Column(name = "sschemename", length = 100)
	private String sschemename;

	@Column(name = "dorderrefdate")
	private Instant dorderrefdate;

	@Column(name = "ntotalamount", length = 500)
	private String ntotalamount;

	@Column(name = "ntotaltaxamount", length = 500)
	private String ntotaltaxamount;

	@Column(name = "ntotalfrightcharges", length = 500)
	private String ntotalfrightcharges;

	@Column(name = "npaymentmode")
	private int npaymentmode;

	@Column(name = "spaymentdetails", length = 500)
	private String spaymentdetails;

	@Column(name = "nbankcode")
	private int nbankcode;

	@Column(name = "ncurrencytype")
	private int ncurrencytype;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Lob
	@Column(name = "jsondata1", columnDefinition = "jsonb")
	private Map<String, Object> jsondata1;

	@Column(name = "sremarks1", length = 500)
	private String sremarks1;

	@Column(name = "sremarks2", length = 500)
	private String sremarks2;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nproducttestcode")
	private int nproducttestcode;

	@Column(name = "nusercode", nullable = false)
	private short nusercode;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	private transient String stransdisplaystatus;
	private transient String squotationdate;
	private transient String squotationfromdate;
	private transient String squotationtodate;
	private transient String stenderrefdate;
	private transient String sorderrefdate;
	private transient String CustomerName;
	private transient String CustomerType;
	private transient String CustomerGST;
	private transient String Address;
	private transient String EmailId;
	private transient String PhoneNo;
	private transient int ntransactionstatus;
	private transient String scolorhexcode;
	private transient String scolorcode;
	private transient String ssymbol;
	private transient String sproducttestdetail;
	private transient String TotalAmountInWords;
	private transient int nschemecode;
	private transient String scurrency;
	private transient int nprojectmastercode;

	@Override
	public InvoiceQuotationHeader mapRow(ResultSet arg0, int arg1) throws SQLException {

		final InvoiceQuotationHeader objQuotationHeader = new InvoiceQuotationHeader();
		objQuotationHeader.setNquotationseqcode(getInteger(arg0, "nquotationseqcode", arg1));
		objQuotationHeader.setSquotationno(StringEscapeUtils.unescapeJava(getString(arg0, "squotationno", arg1)));
		objQuotationHeader.setDquotationdate(getInstant(arg0, "dquotationdate", arg1));
		objQuotationHeader.setDquotationfromdate(getInstant(arg0, "dquotationfromdate", arg1));
		objQuotationHeader.setDquotationtodate(getInstant(arg0, "dquotationtodate", arg1));
		objQuotationHeader.setNconvertedinvoice(getInteger(arg0, "nconvertedinvoice", arg1));
		objQuotationHeader.setNcustomercode(getInteger(arg0, "ncustomercode", arg1));
		objQuotationHeader.setSprojectcode(StringEscapeUtils.unescapeJava(getString(arg0, "sprojectcode", arg1)));
		objQuotationHeader.setSprojectname(StringEscapeUtils.unescapeJava(getString(arg0, "sprojectname", arg1)));
		objQuotationHeader.setStenderrefno(StringEscapeUtils.unescapeJava(getString(arg0, "stenderrefno", arg1)));
		objQuotationHeader.setDtenderrefdate(getInstant(arg0, "dtenderrefdate", arg1));
		objQuotationHeader.setSschemename(StringEscapeUtils.unescapeJava(getString(arg0, "sschemename", arg1)));
		objQuotationHeader.setDorderrefdate(getInstant(arg0, "dorderrefdate", arg1));
		objQuotationHeader.setNtotalamount(getString(arg0, "ntotalamount", arg1));
		objQuotationHeader.setNtotaltaxamount(getString(arg0, "ntotaltaxamount", arg1));
		objQuotationHeader.setNtotalfrightcharges(getString(arg0, "ntotalfrightcharges", arg1));
		objQuotationHeader.setNpaymentmode(getInteger(arg0, "npaymentmode", arg1));
		objQuotationHeader.setSpaymentdetails(StringEscapeUtils.unescapeJava(getString(arg0, "spaymentdetails", arg1)));
		objQuotationHeader.setNbankcode(getInteger(arg0, "nbankcode", arg1));
		objQuotationHeader.setNcurrencytype(getInteger(arg0, "ncurrencytype", arg1));
		objQuotationHeader.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objQuotationHeader.setJsondata1(getJsonObject(arg0, "jsondata1", arg1));
		objQuotationHeader.setSremarks1(StringEscapeUtils.unescapeJava(getString(arg0, "sremarks1", arg1)));
		objQuotationHeader.setSremarks2(StringEscapeUtils.unescapeJava(getString(arg0, "sremarks2", arg1)));
		objQuotationHeader.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objQuotationHeader.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objQuotationHeader.setNstatus(getShort(arg0, "nstatus", arg1));
		objQuotationHeader
				.setStransdisplaystatus(StringEscapeUtils.unescapeJava(getString(arg0, "stransdisplaystatus", arg1)));
		objQuotationHeader.setSquotationdate(StringEscapeUtils.unescapeJava(getString(arg0, "squotationdate", arg1)));
		objQuotationHeader
				.setSquotationfromdate(StringEscapeUtils.unescapeJava(getString(arg0, "squotationfromdate", arg1)));
		objQuotationHeader
				.setSquotationtodate(StringEscapeUtils.unescapeJava(getString(arg0, "squotationtodate", arg1)));
		objQuotationHeader.setStenderrefdate(StringEscapeUtils.unescapeJava(getString(arg0, "stenderrefdate", arg1)));
		objQuotationHeader.setSorderrefdate(StringEscapeUtils.unescapeJava(getString(arg0, "sorderrefdate", arg1)));
		objQuotationHeader.setCustomerName(StringEscapeUtils.unescapeJava(getString(arg0, "CustomerName", arg1)));
		objQuotationHeader.setCustomerType(StringEscapeUtils.unescapeJava(getString(arg0, "CustomerType", arg1)));
		objQuotationHeader.setCustomerGST(StringEscapeUtils.unescapeJava(getString(arg0, "CustomerGST", arg1)));
		objQuotationHeader.setAddress(StringEscapeUtils.unescapeJava(getString(arg0, "Address", arg1)));
		objQuotationHeader.setEmailId(StringEscapeUtils.unescapeJava(getString(arg0, "EmailId", arg1)));
		objQuotationHeader.setPhoneNo(StringEscapeUtils.unescapeJava(getString(arg0, "PhoneNo", arg1)));
		objQuotationHeader.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objQuotationHeader.setScolorhexcode(StringEscapeUtils.unescapeJava(getString(arg0, "scolorhexcode", arg1)));
		objQuotationHeader.setScolorcode(StringEscapeUtils.unescapeJava(getString(arg0, "scolorcode", arg1)));
		objQuotationHeader.setNproducttestcode(getInteger(arg0, "nproducttestcode", arg1));
		objQuotationHeader
				.setSproducttestdetail(StringEscapeUtils.unescapeJava(getString(arg0, "sproducttestdetail", arg1)));
		objQuotationHeader
				.setTotalAmountInWords(StringEscapeUtils.unescapeJava(getString(arg0, "TotalAmountInWords", arg1)));
		objQuotationHeader.setSsymbol(StringEscapeUtils.unescapeJava(getString(arg0, "ssymbol", arg1)));
		objQuotationHeader.setNschemecode(getInteger(arg0, "nschemecode", arg1));
		objQuotationHeader.setNusercode(getShort(arg0, "nusercode", arg1));
		objQuotationHeader.setScurrency(StringEscapeUtils.unescapeJava(getString(arg0, "scurrency", arg1)));
		objQuotationHeader.setNprojectmastercode(getInteger(arg0, "nprojectmastercode", arg1));

		return objQuotationHeader;
	}

}
