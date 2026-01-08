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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'taxproductdetails' table of the
 * Database.
 */
@Entity
@Table(name = "taxproductdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TaxProductDetails extends CustomizedResultsetRowMapper<TaxProductDetails>
		implements Serializable, RowMapper<TaxProductDetails> {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ntaxproductcode")
	private int ntaxproductcode;

	@Column(name = "nproductcode")
	private int nproductcode;

	@Column(name = "ntaxcode")
	private int ntaxcode;

	@ColumnDefault("4")
	@Column(name = "nactive", nullable = false)
	private short nactive = 4;

	@Column(name = "sversionno", length = 50, nullable = false)
	private String sversionno;

	@Column(name = "nusercode")
	private int nusercode;

	@Column(name = "ncaltypecode", nullable = false)
	private short ncaltypecode;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	private transient String sproductname;
	private transient String sdisplaystatus;
	private transient double ntax;
	private transient String staxname;

	@Override
	public TaxProductDetails mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub

		TaxProductDetails objTaxProductDetails = new TaxProductDetails();
		objTaxProductDetails.setNtaxproductcode(getInteger(arg0, "ntaxproductcode", arg1));
		objTaxProductDetails.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objTaxProductDetails.setNtaxcode(getInteger(arg0, "ntaxcode", arg1));
		objTaxProductDetails.setNtax(getDouble(arg0, "ntax", arg1));
		objTaxProductDetails.setNactive(getShort(arg0, "nactive", arg1));
		objTaxProductDetails.setSversionno(getString(arg0, "sversionno", arg1));
		objTaxProductDetails.setNusercode(getInteger(arg0, "nusercode", arg1));
		objTaxProductDetails.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objTaxProductDetails.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objTaxProductDetails.setNstatus(getShort(arg0, "nstatus", arg1));
		objTaxProductDetails.setSproductname(getString(arg0, "sproductname", arg1));
		objTaxProductDetails.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objTaxProductDetails.setStaxname(getString(arg0, "staxname", arg1));
		objTaxProductDetails.setNcaltypecode(getShort(arg0, "ncaltypecode", arg1));
		return objTaxProductDetails;
	}
}