package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'invoicetaxtype' table of the
 * Database.
 */
@Entity
@Table(name = "invoicetaxtype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceTaxtype extends CustomizedResultsetRowMapper<InvoiceTaxtype>
		implements Serializable, RowMapper<InvoiceTaxtype> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntaxcode")
	private int ntaxcode;

	@Column(name = "staxname", length = 100, nullable = false)
	private String staxname;

	@Column(name = "nversionnocode", nullable = false)
	private short nversionnocode;

	@Column(name = "ncaltypecode", nullable = false)
	private short ncaltypecode;

	@Column(name = "sdescription", length = 255)
	private String sdescription;

	@Column(name = "ntax", nullable = false)
	private double ntax;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "ntranscode", nullable = false)
	private short ntranscode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	@Column(name = "ddatefrom")
	private Date ddatefrom;

	@Column(name = "ddateto")
	private Date ddateto;

	@Transient
	private String sdatefrom;

	@Transient
	private String sdateto;

	@Transient
	private String staxcaltype;

	@Transient
	private String sversionno;

	@Transient
	private String stransstatus;

	@Transient
	private Map<String, Object> jsondata;

	@Transient
	private int nneedrights;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nusercode")
	private int nusercode;

	@Override
	public InvoiceTaxtype mapRow(ResultSet arg0, int arg1) throws SQLException {
		InvoiceTaxtype objTaxtype = new InvoiceTaxtype();
		objTaxtype.setNtaxcode(getInteger(arg0, "ntaxcode", arg1));
		objTaxtype.setStaxname(getString(arg0, "staxname", arg1));
		objTaxtype.setNversionnocode(getShort(arg0, "nversionnocode", arg1));
		objTaxtype.setNcaltypecode(getShort(arg0, "ncaltypecode", arg1));
		objTaxtype.setSdescription(getString(arg0, "sdescription", arg1));
		objTaxtype.setNtax(getDouble(arg0, "ntax", arg1));
		objTaxtype.setSversionno(getString(arg0, "sversionno", arg1));
		objTaxtype.setStransstatus(getString(arg0, "stransstatus", arg1));
		objTaxtype.setDdatefrom(getDate(arg0, "ddatefrom", arg1));
		objTaxtype.setDdateto(getDate(arg0, "ddateto", arg1));
		objTaxtype.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objTaxtype.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objTaxtype.setNstatus(getShort(arg0, "nstatus", arg1));
		objTaxtype.setNtranscode(getShort(arg0, "ntranscode", arg1));
		objTaxtype.setNusercode(getInteger(arg0, "nusercode", arg1));
		objTaxtype.setSdatefrom(getString(arg0, "sdatefrom", arg1));
		objTaxtype.setSdateto(getString(arg0, "sdateto", arg1));
		objTaxtype.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objTaxtype.setStaxcaltype(getString(arg0, "staxcaltype", arg1));
		objTaxtype.setNneedrights(getInteger(arg0, "needrights", arg1));
		return objTaxtype;
	}
}
