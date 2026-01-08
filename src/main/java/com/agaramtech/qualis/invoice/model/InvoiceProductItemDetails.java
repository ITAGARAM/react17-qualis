package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
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
 * This class is used to map the fields of 'invoiceproductitemdetails' table of
 * the Database.
 */
@Entity
@Table(name = "invoiceproductitemdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceProductItemDetails extends CustomizedResultsetRowMapper<InvoiceProductItemDetails>
		implements Serializable, RowMapper<InvoiceProductItemDetails> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ninvoiceproductitemdetailscode  ")
	private int ninvoiceproductitemdetailscode;

	@Column(name = "nserialno")
	private int nserialno;

	@Column(name = "sinvoiceseqno ", length = 100, nullable = false)
	private String sinvoiceseqno;

	@Column(name = "dinvoicedate ")
	private Instant dinvoicedate;

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

	@Column(name = "sproductrefno ", length = 100, nullable = true)
	private String sproductrefno;

	@Column(name = "nlimsproduct")
	private int nlimsproduct;

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
	private transient String squotationno;
	private transient String squotationseqno;
	private transient String staxname;
	private transient int slno;
	private transient String noverallcostvalue;
	private transient int nproductcode;
	private transient int nproductslno;
	private transient int nindirectax;
	private transient String sindirecttaxname;
	private transient int npreregno;
	private transient List<ProductTest> testList;

	@Override
	public InvoiceProductItemDetails mapRow(ResultSet arg0, int arg1) throws SQLException {

		final InvoiceProductItemDetails objInvoiceProductItemDetails = new InvoiceProductItemDetails();
		objInvoiceProductItemDetails
				.setNinvoiceproductitemdetailscode(getInteger(arg0, "ninvoiceproductitemdetailscode", arg1));
		objInvoiceProductItemDetails.setSlno(getInteger(arg0, "slno", arg1));
		objInvoiceProductItemDetails.setNserialno(getInteger(arg0, "nserialno", arg1));
		objInvoiceProductItemDetails.setNserialno(getInteger(arg0, "nserialno", arg1));
		objInvoiceProductItemDetails
				.setSinvoiceseqno(StringEscapeUtils.unescapeJava(getString(arg0, "sinvoiceseqno", arg1)));
		objInvoiceProductItemDetails.setDinvoicedate(getInstant(arg0, "dinvoicedate", arg1));
		objInvoiceProductItemDetails.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objInvoiceProductItemDetails.setJsondata1(getJsonObject(arg0, "jsondata1", arg1));
		objInvoiceProductItemDetails.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInvoiceProductItemDetails.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInvoiceProductItemDetails.setNstatus(getShort(arg0, "nstatus", arg1));
		objInvoiceProductItemDetails
				.setSproductname(StringEscapeUtils.unescapeJava(getString(arg0, "sproductname", arg1)));
		objInvoiceProductItemDetails.setNunit(StringEscapeUtils.unescapeJava(getString(arg0, "nunit", arg1)));
		objInvoiceProductItemDetails.setStaxname(StringEscapeUtils.unescapeJava(getString(arg0, "staxname", arg1)));
		objInvoiceProductItemDetails.setNproductslno(getInteger(arg0, "nproductslno", arg1));
		objInvoiceProductItemDetails.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objInvoiceProductItemDetails.setNcost(getDouble(arg0, "ncost", arg1));
		objInvoiceProductItemDetails
				.setNdiscountpercentage(StringEscapeUtils.unescapeJava(getString(arg0, "ndiscountpercentage", arg1)));
		objInvoiceProductItemDetails.setNtaxvalue(StringEscapeUtils.unescapeJava(getString(arg0, "ntaxvalue", arg1)));
		objInvoiceProductItemDetails.setNtaxamount(StringEscapeUtils.unescapeJava(getString(arg0, "ntaxamount", arg1)));
		objInvoiceProductItemDetails
				.setNoverallcost(StringEscapeUtils.unescapeJava(getString(arg0, "noverallcost", arg1)));
		objInvoiceProductItemDetails.setNtaxpercentage(getString(arg0, "ntaxpercentage", arg1));
		objInvoiceProductItemDetails.setNquantity(getString(arg0, "nquantity", arg1));
		objInvoiceProductItemDetails.setNtotalcost(getString(arg0, "ntotalcost", arg1));
		objInvoiceProductItemDetails.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInvoiceProductItemDetails.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInvoiceProductItemDetails.setNstatus(getShort(arg0, "nstatus", arg1));
		objInvoiceProductItemDetails
				.setSquotationno(StringEscapeUtils.unescapeJava(getString(arg0, "squotationno", arg1)));
		objInvoiceProductItemDetails
				.setSquotationseqno(StringEscapeUtils.unescapeJava(getString(arg0, "squotationseqno", arg1)));
		objInvoiceProductItemDetails
				.setSproductrefno(StringEscapeUtils.unescapeJava(getString(arg0, "sproductrefno", arg1)));
		objInvoiceProductItemDetails.setNlimsproduct(getInteger(arg0, "nlimsproduct", arg1));
		objInvoiceProductItemDetails
				.setNoverallcostvalue(StringEscapeUtils.unescapeJava(getString(arg0, "noverallcostvalue", arg1)));
		objInvoiceProductItemDetails.setNindirectax(getInteger(arg0, "nindirectax", arg1));
		objInvoiceProductItemDetails
				.setSindirecttaxname(StringEscapeUtils.unescapeJava(getString(arg0, "sindirecttaxname", arg1)));
		objInvoiceProductItemDetails.setNpreregno(getInteger(arg0, "npreregno", arg1));

		return objInvoiceProductItemDetails;
	}

}
