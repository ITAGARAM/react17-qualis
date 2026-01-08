package com.agaramtech.qualis.biobank.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

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
 * Maps to 'biobankecataloguerequestexternaldetails' table.
 */
@Entity
@Table(name = "biobankecataloguerequestexternaldetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BiobankECatalogueRequestExternalDetails
		extends CustomizedResultsetRowMapper<BiobankECatalogueRequestExternalDetails>
		implements Serializable, RowMapper<BiobankECatalogueRequestExternalDetails> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiobankecatreqexternaldetailcode", nullable = false)
	private int nbiobankecatreqexternaldetailcode;

	@Column(name = "nbiobankecatreqexternalcode", nullable = false)
	private int nbiobankecatreqexternalcode;

	@Column(name = "nbioprojectcode", nullable = false)
	private int nbioprojectcode;

	@Column(name = "nproductcode", nullable = false)
	private int nproductcode;

	@Column(name = "sparentsamplecode", length = 30)
	private String sparentsamplecode;

	@Column(name = "nreqnoofsamples", nullable = false)
	private short nreqnoofsamples;

	@Column(name = "sreqminvolume", length = 10, nullable = false)
	private String sreqminvolume;

	@Column(name = "naccnoofsamples")
	private Short naccnoofsamples; // nullable

	@Column(name = "saccminvolume", length = 10)
	private String saccminvolume;

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;

	@Column(name = "ntztransactiondate", nullable = false)
	@ColumnDefault("-1")
	private int ntztransactiondate = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "noffsetdtransactiondate", nullable = false)
	@ColumnDefault("0")
	private int noffsetdtransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@Id
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	// Transient fields
	@Transient
	private transient String sformnumber;
	@Transient
	private transient String sprojecttitle;
	@Transient
	private transient String sreceivingsitename;
	@Transient
	private transient String sproductname;
	@Transient
	private transient String stransactiondate;
	@Transient
	private transient short nreceiversitecode;

	@Override
	public BiobankECatalogueRequestExternalDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
		BiobankECatalogueRequestExternalDetails objBiobankECatalogueRequestExternalDetails = new BiobankECatalogueRequestExternalDetails();
		objBiobankECatalogueRequestExternalDetails
				.setNbiobankecatreqexternaldetailcode(getInteger(rs, "nbiobankecatreqexternaldetailcode", rowNum));
		objBiobankECatalogueRequestExternalDetails
				.setNbiobankecatreqexternalcode(getInteger(rs, "nbiobankecatreqexternalcode", rowNum));
		objBiobankECatalogueRequestExternalDetails.setNbioprojectcode(getInteger(rs, "nbioprojectcode", rowNum));
		objBiobankECatalogueRequestExternalDetails.setNproductcode(getInteger(rs, "nproductcode", rowNum));
		objBiobankECatalogueRequestExternalDetails.setSparentsamplecode(getString(rs, "sparentsamplecode", rowNum));
		objBiobankECatalogueRequestExternalDetails.setNreqnoofsamples(getShort(rs, "nreqnoofsamples", rowNum));
		objBiobankECatalogueRequestExternalDetails.setSreqminvolume(getString(rs, "sreqminvolume", rowNum));
		objBiobankECatalogueRequestExternalDetails.setNaccnoofsamples(getShort(rs, "naccnoofsamples", rowNum));
		objBiobankECatalogueRequestExternalDetails.setSaccminvolume(getString(rs, "saccminvolume", rowNum));
		objBiobankECatalogueRequestExternalDetails.setDtransactiondate(getInstant(rs, "dtransactiondate", rowNum));
		objBiobankECatalogueRequestExternalDetails.setNtztransactiondate(getInteger(rs, "ntztransactiondate", rowNum));
		objBiobankECatalogueRequestExternalDetails
				.setNoffsetdtransactiondate(getInteger(rs, "noffsetdtransactiondate", rowNum));
		objBiobankECatalogueRequestExternalDetails.setNsitecode(getShort(rs, "nsitecode", rowNum));
		objBiobankECatalogueRequestExternalDetails.setNstatus(getShort(rs, "nstatus", rowNum));

		objBiobankECatalogueRequestExternalDetails.setSformnumber(getString(rs, "sformnumber", rowNum));
		objBiobankECatalogueRequestExternalDetails.setSprojecttitle(getString(rs, "sprojecttitle", rowNum));
		objBiobankECatalogueRequestExternalDetails.setSreceivingsitename(getString(rs, "sreceivingsitename", rowNum));
		objBiobankECatalogueRequestExternalDetails.setSproductname(getString(rs, "sproductname", rowNum));
		objBiobankECatalogueRequestExternalDetails.setStransactiondate(getString(rs, "stransactiondate", rowNum));
		objBiobankECatalogueRequestExternalDetails.setNreceiversitecode(getShort(rs, "nreceiversitecode", rowNum));

		return objBiobankECatalogueRequestExternalDetails;
	}
}
