package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'invoiceschemesproducts' table of the
 * Database.
 */

@Entity
@Table(name = "invoiceschemesproducts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("serial")
public class InvoiceSchemesProduct extends CustomizedResultsetRowMapper<InvoiceSchemesProduct>
		implements Serializable, RowMapper<InvoiceSchemesProduct> {

	@Id
	@Column(name = "nschemeproductcode")
	private int nschemeproductcode;

	@Column(name = "nschemecode")
	private int nschemecode;

	@Column(name = "nproductcode")
	private int nproductcode;

	@Column(name = "ncost")
	private Double ncost;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Lob
	@Column(name = "jsondata1", columnDefinition = "jsonb")
	private Map<String, Object> jsondata1;

	@Lob
	@Column(name = "jsondata2", columnDefinition = "jsonb")
	private Map<String, Object> jsondata2;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	public InvoiceSchemesProduct mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub

		InvoiceSchemesProduct objInvoiceSchemesProduct = new InvoiceSchemesProduct();
		objInvoiceSchemesProduct.setNschemeproductcode(getInteger(arg0, "nschemeproductid", arg1));
		objInvoiceSchemesProduct.setNschemecode(getInteger(arg0, "nschemecode", arg1));
		objInvoiceSchemesProduct.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objInvoiceSchemesProduct.setNcost(getDouble(arg0, "ncost", arg1));
		objInvoiceSchemesProduct.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objInvoiceSchemesProduct.setJsondata1(getJsonObject(arg0, "jsondata1", arg1));
		objInvoiceSchemesProduct.setJsondata1(getJsonObject(arg0, "jsondata2", arg1));
		objInvoiceSchemesProduct.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInvoiceSchemesProduct.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInvoiceSchemesProduct.setNstatus(getShort(arg0, "nstatus", arg1));
		return objInvoiceSchemesProduct;
	}
}