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

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'invoiceheader' table of the
 * Database.
 */
@Entity
@Table(name = "invoiceheader")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceHeader extends CustomizedResultsetRowMapper<InvoiceHeader>
		implements Serializable, RowMapper<InvoiceHeader> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ninvoiceseqcode")
	private int ninvoiceseqcode;

	@Column(name = "sinvoiceno")
	private String sinvoiceno;

	@Column(name = "dinvoicedate")
	private Instant dinvoicedate;

	@Column(name = "squotationid", length = 50, nullable = false)
	private String squotationid;

	@Column(name = "dquotationdate")
	private Instant dquotationdate;

	@Column(name = "ncustomercode")
	private int ncustomercode;

	@Column(name = "sprojectcode", length = 50)
	private String sprojectcode;

	@Column(name = "sprocessno", length = 50)
	private String sprocessno;

	@Column(name = "dprocessdate")
	private Instant dprocessdate;

	@Column(name = "sschemename", length = 100)
	private String sschemename;

	@Column(name = "dorderreferencedate")
	private Instant dorderreferencedate;

	@Column(name = "spackdoctrefno", length = 500)
	private String spackdoctrefno;

	@Column(name = "dpackdocrefdate")
	private Instant dpackdocrefdate;

	@Column(name = "spackagerefdetails", length = 500)
	private String spackagerefdetails;

	@Column(name = "ntotalamount", length = 500)
	private String ntotalamount;

	@Column(name = "ntotaltaxamount", length = 500)
	private String ntotaltaxamount;

	@Column(name = "ntotalfrightcharges")
	private Double ntotalfrightcharges;

	@Column(name = "npaymentcode")
	private int npaymentcode;

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

	@Column(name = "nproducttestcode")
	private int nproducttestcode;

	private transient String stypename;
	private transient String CustomerName;
	private transient String sinvoicedate;
	private transient String squotationdate;
	private transient String sprocessdate;
	private transient String sorderreferencedate;
	private transient String spackdocrefdate;
	private transient String squotationno;
	private transient int ntransactionstatus;
	private transient String stransdisplaystatus;
	private transient String Address;
	private transient String EmailId;
	private transient String PhoneNo;
	private transient String CustomerType;
	private transient String CustomerGST;
	private transient String sinvoiceseqno;
	private transient String sproductname;
	private transient String nunit;
	private transient Double ncost;
	private transient String ndiscountpercentage;
	private transient String nquantity;
	private transient String ntaxvalue;
	private transient String ntaxamount;
	private transient String noverallcost;
	private transient String ntaxpercentage;
	private transient String ntotalcost;
	private transient String squotationseqno;
	private transient String staxname;
	private transient String scolorhexcode;
	private transient String scolorcode;
	private transient String sproducttestdetail;
	private transient String TotalAmountInWords;
	private transient String ssymbol;
	private transient int nschemecode;
	private transient String scurrency;

	private transient String PFatherName;
	private transient String PMobileNo;
	private transient String PatientId;
	private transient String PDOB;
	private transient String PAge;
	private transient String PatientName;
	private transient String PEmail;
	private transient String sproductrefno;

	@Override
	public InvoiceHeader mapRow(ResultSet arg0, int arg1) throws SQLException {

		final InvoiceHeader objInvoiceHeader = new InvoiceHeader();

		objInvoiceHeader.setAddress(StringEscapeUtils.unescapeJava(getString(arg0, "Address", arg1)));
		objInvoiceHeader.setEmailId(StringEscapeUtils.unescapeJava(getString(arg0, "EmailId", arg1)));
		objInvoiceHeader.setPhoneNo(StringEscapeUtils.unescapeJava(getString(arg0, "PhoneNo", arg1)));
		objInvoiceHeader.setCustomerType(StringEscapeUtils.unescapeJava(getString(arg0, "CustomerType", arg1)));
		objInvoiceHeader.setCustomerGST(StringEscapeUtils.unescapeJava(getString(arg0, "CustomerGST", arg1)));
		objInvoiceHeader.setNinvoiceseqcode(getInteger(arg0, "ninvoiceseqcode", arg1));
		objInvoiceHeader.setSinvoiceno(StringEscapeUtils.unescapeJava(getString(arg0, "sinvoiceno", arg1)));
		objInvoiceHeader.setDinvoicedate(getInstant(arg0, "dinvoicedate", arg1));
		objInvoiceHeader.setSquotationid(StringEscapeUtils.unescapeJava(getString(arg0, "squotationid", arg1)));
		objInvoiceHeader.setDquotationdate(getInstant(arg0, "dquotationdate", arg1));
		objInvoiceHeader.setNcustomercode(getInteger(arg0, "ncustomercode", arg1));
		objInvoiceHeader.setSprojectcode(StringEscapeUtils.unescapeJava(getString(arg0, "sprojectcode", arg1)));
		objInvoiceHeader.setSprocessno(StringEscapeUtils.unescapeJava(getString(arg0, "sprocessno", arg1)));
		objInvoiceHeader.setDprocessdate(getInstant(arg0, "dprocessdate", arg1));
		objInvoiceHeader.setSschemename(StringEscapeUtils.unescapeJava(getString(arg0, "sschemename", arg1)));
		objInvoiceHeader.setDorderreferencedate(getInstant(arg0, "dorderreferencedate", arg1));
		objInvoiceHeader.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInvoiceHeader.setSpackdoctrefno(StringEscapeUtils.unescapeJava(getString(arg0, "spackdoctrefno", arg1)));
		objInvoiceHeader.setDpackdocrefdate(getInstant(arg0, "dpackdocrefdate", arg1));
		objInvoiceHeader
				.setSpackagerefdetails(StringEscapeUtils.unescapeJava(getString(arg0, "spackagerefdetails", arg1)));
		objInvoiceHeader.setNtotalamount(StringEscapeUtils.unescapeJava(getString(arg0, "ntotalamount", arg1)));
		objInvoiceHeader.setNtotaltaxamount(StringEscapeUtils.unescapeJava(getString(arg0, "ntotaltaxamount", arg1)));
		objInvoiceHeader.setNtotalfrightcharges(getDouble(arg0, "ntotalfrightcharges", arg1));
		objInvoiceHeader.setNpaymentcode(getInteger(arg0, "npaymentcode", arg1));
		objInvoiceHeader.setSpaymentdetails(StringEscapeUtils.unescapeJava(getString(arg0, "spaymentdetails", arg1)));
		objInvoiceHeader.setNbankcode(getInteger(arg0, "nbankcode", arg1));
		objInvoiceHeader.setNcurrencytype(getInteger(arg0, "ncurrencytype", arg1));
		objInvoiceHeader.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objInvoiceHeader.setJsondata1(getJsonObject(arg0, "jsondata1", arg1));
		objInvoiceHeader.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInvoiceHeader.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInvoiceHeader.setNstatus(getShort(arg0, "nstatus", arg1));
		objInvoiceHeader.setStypename(StringEscapeUtils.unescapeJava(getString(arg0, "stypename", arg1)));
		;
		objInvoiceHeader.setCustomerName(StringEscapeUtils.unescapeJava(getString(arg0, "CustomerName", arg1)));
		objInvoiceHeader.setSinvoicedate(StringEscapeUtils.unescapeJava(getString(arg0, "sinvoicedate", arg1)));
		objInvoiceHeader.setSquotationdate(StringEscapeUtils.unescapeJava(getString(arg0, "squotationdate", arg1)));
		objInvoiceHeader.setSprocessdate(StringEscapeUtils.unescapeJava(getString(arg0, "sprocessdate", arg1)));
		objInvoiceHeader
				.setSorderreferencedate(StringEscapeUtils.unescapeJava(getString(arg0, "sorderreferencedate", arg1)));
		objInvoiceHeader.setSpackdocrefdate(StringEscapeUtils.unescapeJava(getString(arg0, "spackdocrefdate", arg1)));
		objInvoiceHeader.setSquotationno(StringEscapeUtils.unescapeJava(getString(arg0, "squotationno", arg1)));
		objInvoiceHeader.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objInvoiceHeader
				.setStransdisplaystatus(StringEscapeUtils.unescapeJava(getString(arg0, "stransdisplaystatus", arg1)));
		objInvoiceHeader.setSproductname(StringEscapeUtils.unescapeJava(getString(arg0, "sproductname", arg1)));
		objInvoiceHeader.setNunit(StringEscapeUtils.unescapeJava(getString(arg0, "nunit", arg1)));
		objInvoiceHeader.setStaxname(StringEscapeUtils.unescapeJava(getString(arg0, "staxname", arg1)));
		objInvoiceHeader.setNcost(getDouble(arg0, "ncost", arg1));
		objInvoiceHeader.setNdiscountpercentage(getString(arg0, "ndiscountpercentage", arg1));
		objInvoiceHeader.setNtaxvalue(StringEscapeUtils.unescapeJava(getString(arg0, "ntaxvalue", arg1)));
		objInvoiceHeader.setNtaxamount(StringEscapeUtils.unescapeJava(getString(arg0, "ntaxamount", arg1)));
		objInvoiceHeader.setNoverallcost(StringEscapeUtils.unescapeJava(getString(arg0, "noverallcost", arg1)));
		objInvoiceHeader.setNtaxpercentage(StringEscapeUtils.unescapeJava(getString(arg0, "ntaxpercentage", arg1)));
		objInvoiceHeader.setNquantity(StringEscapeUtils.unescapeJava(getString(arg0, "nquantity", arg1)));
		objInvoiceHeader.setNtotalcost(StringEscapeUtils.unescapeJava(getString(arg0, "ntotalcost", arg1)));
		objInvoiceHeader.setSinvoiceseqno(StringEscapeUtils.unescapeJava(getString(arg0, "sinvoiceseqno", arg1)));
		objInvoiceHeader.setScolorhexcode(StringEscapeUtils.unescapeJava(getString(arg0, "scolorhexcode", arg1)));
		objInvoiceHeader.setScolorcode(StringEscapeUtils.unescapeJava(getString(arg0, "scolorcode", arg1)));
		objInvoiceHeader.setNproducttestcode(getInteger(arg0, "nproducttestcode", arg1));
		objInvoiceHeader
				.setSproducttestdetail(StringEscapeUtils.unescapeJava(getString(arg0, "sproducttestdetail", arg1)));
		objInvoiceHeader
				.setTotalAmountInWords(StringEscapeUtils.unescapeJava(getString(arg0, "TotalAmountInWords", arg1)));
		objInvoiceHeader.setSsymbol(StringEscapeUtils.unescapeJava(getString(arg0, "ssymbol", arg1)));
		objInvoiceHeader.setNschemecode(getInteger(arg0, "nschemecode", arg1));
		objInvoiceHeader.setNusercode(getShort(arg0, "nusercode", arg1));
		objInvoiceHeader.setScurrency(StringEscapeUtils.unescapeJava(getString(arg0, "scurrency", arg1)));

		objInvoiceHeader.setPFatherName(StringEscapeUtils.unescapeJava(getString(arg0, "PFatherName", arg1)));
		objInvoiceHeader.setPMobileNo(StringEscapeUtils.unescapeJava(getString(arg0, "PMobileNo", arg1)));
		objInvoiceHeader.setPatientId(StringEscapeUtils.unescapeJava(getString(arg0, "PatientId", arg1)));
		objInvoiceHeader.setPDOB(StringEscapeUtils.unescapeJava(getString(arg0, "PDOB", arg1)));
		objInvoiceHeader.setPAge(StringEscapeUtils.unescapeJava(getString(arg0, "PAge", arg1)));
		objInvoiceHeader.setPatientName(StringEscapeUtils.unescapeJava(getString(arg0, "PatientName", arg1)));
		objInvoiceHeader.setPEmail(StringEscapeUtils.unescapeJava(getString(arg0, "PEmail", arg1)));
		objInvoiceHeader.setSproductrefno(StringEscapeUtils.unescapeJava(getString(arg0, "sproductrefno", arg1)));

		return objInvoiceHeader;
	}

}
