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
 * This class is used to map the fields of 'invoiceproductmaster' table of the
 * Database.
 */
@Entity
@Table(name = "invoiceproductmaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceProductMaster extends CustomizedResultsetRowMapper<InvoiceProductMaster>
		implements Serializable, RowMapper<InvoiceProductMaster> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nproductcode")
	private int nproductcode;

	@Column(name = "ntypecode")
	private int ntypecode;

	@Column(name = "slimscode", length = 20, nullable = false)
	private String slimscode;

	@Column(name = "sproductname", length = 100, nullable = false)
	private String sproductname;

	@Column(name = "sdescription", length = 255)
	private String sdescription;

	@Column(name = "sinvoicedescription", length = 255)
	private String sinvoicedescription;

	@Column(name = "saddtext1", length = 100)
	private String saddtext1;

	@Column(name = "saddtext2", length = 100)
	private String saddtext2;

	@Column(name = "ntaxavailable", nullable = false)
	@ColumnDefault("4")
	private int ntaxavailable = 4;

	@Column(name = "ncost")
	private Double ncost;

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

	private transient double ntax;
	private transient String stypename;
	private transient String sdisplaystatus;

	private transient String staxname;
	private transient short nactive = 4;
	private transient short ncaltypecode;
	private transient String sversionno;
	private transient int nindirectax;
	private transient String sindirecttaxname;
	private transient int nschemecode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "slimsdata", length = 20, nullable = false)
	private String slimsdata;

	@Column(name = "nlimsproduct")
	private int nlimsproduct;

	@Override
	public InvoiceProductMaster mapRow(ResultSet arg0, int arg1) throws SQLException {

		InvoiceProductMaster objProductMaster = new InvoiceProductMaster();
		objProductMaster.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objProductMaster.setNtypecode(getInteger(arg0, "ntypecode", arg1));
		objProductMaster.setSlimsdata(getString(arg0, slimsdata, arg1));
		objProductMaster.setSlimscode(getString(arg0, "slimscode", arg1));
		objProductMaster.setSproductname(getString(arg0, "sproductname", arg1));
		objProductMaster.setSdescription(getString(arg0, "sdescription", arg1));
		objProductMaster.setSinvoicedescription(getString(arg0, "sinvoicedescription", arg1));
		objProductMaster.setSaddtext1(getString(arg0, "saddtext1", arg1));
		objProductMaster.setSaddtext2(getString(arg0, "saddtext2", arg1));
		objProductMaster.setNtaxavailable(getInteger(arg0, "ntaxavailable", arg1));
		objProductMaster.setNcost(getDouble(arg0, "ncost", arg1));
		objProductMaster.setNusercode(getInteger(arg0, "nusercode", arg1));
		objProductMaster.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objProductMaster.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objProductMaster.setNstatus(getShort(arg0, "nstatus", arg1));
		objProductMaster.setStypename(getString(arg0, "stypename", arg1));
		objProductMaster.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objProductMaster.setNtax(getDouble(arg0, "ntax", arg1));
		objProductMaster.setStaxname(getString(arg0, "staxname", arg1));
		objProductMaster.setSindirecttaxname(getString(arg0, "sindirecttaxname", arg1));
		objProductMaster.setNlimsproduct(getInteger(arg0, "nlimsproduct", arg1));
		objProductMaster.setNindirectax(getInteger(arg0, "nindirectax", arg1));
		objProductMaster.setNcaltypecode(getShort(arg0, "ncaltypecode", arg1));
		objProductMaster.setNschemecode(getInteger(arg0, "nschemecode", arg1));

		return objProductMaster;

	}

}
