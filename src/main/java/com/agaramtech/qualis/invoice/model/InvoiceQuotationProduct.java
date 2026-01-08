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
 * This class is used to map the fields of 'quotationitemdetails' table of the
 * Database.
 */
@Entity
@Table(name = "quotationitemdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceQuotationProduct extends CustomizedResultsetRowMapper<InvoiceQuotationProduct>
		implements Serializable, RowMapper<InvoiceQuotationProduct> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nquotationitemdetailscode")
	private int nquotationitemdetailscode;

	@Column(name = "nserialno")
	private int nserialno;

	@Column(name = "squotationseqno", length = 100, nullable = false)
	private String squotationseqno;

	@Column(name = "dquotationdate")
	private Instant dquotationdate;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Lob
	@Column(name = "jsondata1", columnDefinition = "jsonb")
	private Map<String, Object> jsondata1;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	private transient String sproductname;
	private transient String nunit;
	private transient String ncost;
	private transient String ndiscountpercentage;
	private transient String nquantity;
	private transient String ntaxvalue;
	private transient String ntaxamount;
	private transient String noverallcost;
	private transient String ntaxpercentage;
	private transient String ntotalcost;
	private transient int slno;
	private transient String staxname;
	private transient int nlimsproduct;
	private transient String squotationno;
	private transient String squotationdate;
	private transient int nproductcode;
	private transient String noverallcostvalue;

	@Override
	public InvoiceQuotationProduct mapRow(ResultSet arg0, int arg1) throws SQLException {
		final InvoiceQuotationProduct objInvoiceQuotationProduct = new InvoiceQuotationProduct();
		objInvoiceQuotationProduct.setNquotationitemdetailscode(getInteger(arg0, "nquotationitemdetailscode", arg1));
		objInvoiceQuotationProduct.setNserialno(getInteger(arg0, "nserialno", arg1));
		objInvoiceQuotationProduct
				.setSquotationseqno(StringEscapeUtils.unescapeJava(getString(arg0, "squotationseqno", arg1)));
		objInvoiceQuotationProduct.setDquotationdate(getInstant(arg0, "dquotationdate", arg1));
		objInvoiceQuotationProduct.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objInvoiceQuotationProduct.setJsondata1(getJsonObject(arg0, "jsondata1", arg1));
		objInvoiceQuotationProduct.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInvoiceQuotationProduct.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInvoiceQuotationProduct.setNstatus(getShort(arg0, "nstatus", arg1));
		objInvoiceQuotationProduct
				.setSproductname(StringEscapeUtils.unescapeJava(getString(arg0, "sproductname", arg1)));
		objInvoiceQuotationProduct.setNunit(getString(arg0, "nunit", arg1));
		objInvoiceQuotationProduct.setNcost(getString(arg0, "ncost", arg1));
		objInvoiceQuotationProduct.setNdiscountpercentage(getString(arg0, "ndiscountpercentage", arg1));
		objInvoiceQuotationProduct.setNtaxvalue(getString(arg0, "ntaxvalue", arg1));
		objInvoiceQuotationProduct.setNtaxamount(getString(arg0, "ntaxamount", arg1));
		objInvoiceQuotationProduct.setNoverallcost(getString(arg0, "noverallcost", arg1));
		objInvoiceQuotationProduct.setNtaxpercentage(getString(arg0, "ntaxpercentage", arg1));
		objInvoiceQuotationProduct.setNquantity(getString(arg0, "nquantity", arg1));
		objInvoiceQuotationProduct.setNtotalcost(getString(arg0, "ntotalcost", arg1));
		objInvoiceQuotationProduct.setSlno(getInteger(arg0, "slno", arg1));
		objInvoiceQuotationProduct.setStaxname(getString(arg0, "staxname", arg1));
		objInvoiceQuotationProduct
				.setSquotationno(StringEscapeUtils.unescapeJava(getString(arg0, "squotationno", arg1)));
		objInvoiceQuotationProduct.setStaxname(StringEscapeUtils.unescapeJava(getString(arg0, "staxname", arg1)));
		objInvoiceQuotationProduct.setStaxname(StringEscapeUtils.unescapeJava(getString(arg0, "staxname", arg1)));
		objInvoiceQuotationProduct.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInvoiceQuotationProduct.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInvoiceQuotationProduct.setNstatus(getShort(arg0, "nstatus", arg1));
		objInvoiceQuotationProduct
				.setSproductname(StringEscapeUtils.unescapeJava(getString(arg0, "sproductname", arg1)));
		objInvoiceQuotationProduct.setNunit(getString(arg0, "nunit", arg1));
		objInvoiceQuotationProduct.setNcost(getString(arg0, "ncost", arg1));
		objInvoiceQuotationProduct.setNdiscountpercentage(getString(arg0, "ndiscountpercentage", arg1));
		objInvoiceQuotationProduct.setNtaxvalue(getString(arg0, "ntaxvalue", arg1));
		objInvoiceQuotationProduct.setNtaxamount(getString(arg0, "ntaxamount", arg1));
		objInvoiceQuotationProduct.setNoverallcost(getString(arg0, "noverallcost", arg1));
		objInvoiceQuotationProduct.setNtaxpercentage(getString(arg0, "ntaxpercentage", arg1));
		objInvoiceQuotationProduct.setNquantity(getString(arg0, "nquantity", arg1));
		objInvoiceQuotationProduct.setNtotalcost(getString(arg0, "ntotalcost", arg1));
		objInvoiceQuotationProduct.setSlno(getInteger(arg0, "slno", arg1));
		objInvoiceQuotationProduct.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInvoiceQuotationProduct.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInvoiceQuotationProduct.setNstatus(getShort(arg0, "nstatus", arg1));
		objInvoiceQuotationProduct
				.setSproductname(StringEscapeUtils.unescapeJava(getString(arg0, "sproductname", arg1)));
		objInvoiceQuotationProduct.setNunit(getString(arg0, "nunit", arg1));
		objInvoiceQuotationProduct.setNcost(getString(arg0, "ncost", arg1));
		objInvoiceQuotationProduct.setNdiscountpercentage(getString(arg0, "ndiscountpercentage", arg1));
		objInvoiceQuotationProduct.setNtaxvalue(getString(arg0, "ntaxvalue", arg1));
		objInvoiceQuotationProduct.setNtaxamount(getString(arg0, "ntaxamount", arg1));
		objInvoiceQuotationProduct.setNoverallcost(getString(arg0, "noverallcost", arg1));
		objInvoiceQuotationProduct.setNtaxpercentage(getString(arg0, "ntaxpercentage", arg1));
		objInvoiceQuotationProduct.setNquantity(getString(arg0, "nquantity", arg1));
		objInvoiceQuotationProduct.setNtotalcost(getString(arg0, "ntotalcost", arg1));
		objInvoiceQuotationProduct.setSlno(getInteger(arg0, "slno", arg1));
		objInvoiceQuotationProduct.setStaxname(StringEscapeUtils.unescapeJava(getString(arg0, "staxname", arg1)));
		objInvoiceQuotationProduct
				.setSquotationno(StringEscapeUtils.unescapeJava(getString(arg0, "squotationno", arg1)));
		objInvoiceQuotationProduct.setNlimsproduct(getInteger(arg0, "nlimsproduct", arg1));
		objInvoiceQuotationProduct
				.setSquotationdate(StringEscapeUtils.unescapeJava(getString(arg0, "squotationdate", arg1)));
		objInvoiceQuotationProduct.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objInvoiceQuotationProduct
				.setNoverallcostvalue(StringEscapeUtils.unescapeJava(getString(arg0, "noverallcostvalue", arg1)));

		return objInvoiceQuotationProduct;
	}

}
