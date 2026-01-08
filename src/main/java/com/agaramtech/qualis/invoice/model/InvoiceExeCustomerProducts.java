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
 * This class is used to map the fields of 'invoiceexecustomerproducts' table of
 * the Database.
 */
@Entity
@Table(name = "invoiceexecustomerproducts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceExeCustomerProducts extends CustomizedResultsetRowMapper<InvoiceExeCustomerProducts>
		implements Serializable, RowMapper<InvoiceExeCustomerProducts> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nexeproduct  ")
	private int nexeproduct;

	@Column(name = "nproductcode")
	private int nproductcode;

	@Column(name = "ncustomercode")
	private int ncustomercode;

	@Column(name = "nschemecode")
	private int nschemecode;

	@Column(name = "slno")
	private int slno;

	@Column(name = "ncost")
	private double ncost;

	@Column(name = "nunit ", length = 100, nullable = false)
	private String nunit;

	@Column(name = "nquantity")
	private int nquantity;

	@Column(name = "ntax")
	private double ntax;

	@Column(name = "ntaxpercentage")
	private double ntaxpercentage;

	@Column(name = "staxname ", length = 100, nullable = false)
	private String staxname;

	@Column(name = "ntotalcost")
	private double ntotalcost;

	@Column(name = "noverallcost")
	private double noverallcost;

	@Column(name = "sproductname ", length = 100, nullable = false)
	private String sproductname;

	@Column(name = "ndiscountpercentage")
	private double ndiscountpercentage;

	@Column(name = "ndiscountperquantity")
	private double ndiscountperquantity;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	@Column(name = "nlimsproduct")
	private int nlimsproduct;

	private transient int nindirectax;
	private transient String sindirecttaxname;

	@Override
	public InvoiceExeCustomerProducts mapRow(ResultSet arg0, int arg1) throws SQLException {

		final InvoiceExeCustomerProducts objInvoiceExeCustomerProducts = new InvoiceExeCustomerProducts();
		objInvoiceExeCustomerProducts.setNexeproduct(getInteger(arg0, "nexeproduct", arg1));
		objInvoiceExeCustomerProducts.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objInvoiceExeCustomerProducts.setNcustomercode(getInteger(arg0, "ncustomercode", arg1));
		objInvoiceExeCustomerProducts.setNschemecode(getInteger(arg0, "nschemecode", arg1));
		objInvoiceExeCustomerProducts.setSlno(getInteger(arg0, "slno", arg1));
		objInvoiceExeCustomerProducts.setNcost(getDouble(arg0, "ncost", arg1));
		objInvoiceExeCustomerProducts.setNtax(getDouble(arg0, "ntax", arg1));
		objInvoiceExeCustomerProducts.setNunit(StringEscapeUtils.unescapeJava(getString(arg0, "nunit", arg1)));
		objInvoiceExeCustomerProducts.setNquantity(getInteger(arg0, "nquantity", arg1));
		objInvoiceExeCustomerProducts.setNtaxpercentage(getDouble(arg0, "ntaxpercentage", arg1));
		objInvoiceExeCustomerProducts.setStaxname(StringEscapeUtils.unescapeJava(getString(arg0, "staxname", arg1)));
		objInvoiceExeCustomerProducts.setNtotalcost(getInteger(arg0, "ntotalcost", arg1));
		objInvoiceExeCustomerProducts.setNoverallcost(getDouble(arg0, "noverallcost", arg1));
		objInvoiceExeCustomerProducts
				.setSproductname(StringEscapeUtils.unescapeJava(getString(arg0, "sproductname", arg1)));
		objInvoiceExeCustomerProducts.setNdiscountpercentage(getDouble(arg0, "ndiscountpercentage", arg1));
		objInvoiceExeCustomerProducts.setNdiscountperquantity(getDouble(arg0, "ndiscountperquantity", arg1));
		objInvoiceExeCustomerProducts.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInvoiceExeCustomerProducts.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInvoiceExeCustomerProducts.setNstatus(getShort(arg0, "nstatus", arg1));
		objInvoiceExeCustomerProducts.setNindirectax(getInteger(arg0, "nindirectax", arg1));
		objInvoiceExeCustomerProducts.setNlimsproduct(getInteger(arg0, "nlimsproduct", arg1));
		objInvoiceExeCustomerProducts
				.setSindirecttaxname(StringEscapeUtils.unescapeJava(getString(arg0, "sindirecttaxname", arg1)));

		return objInvoiceExeCustomerProducts;
	}

}
