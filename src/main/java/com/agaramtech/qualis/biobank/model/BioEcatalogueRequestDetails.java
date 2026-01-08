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

@Entity
@Table(name = "bioecataloguerequestdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioEcatalogueRequestDetails extends CustomizedResultsetRowMapper<BioEcatalogueRequestDetails>
		implements Serializable, RowMapper<BioEcatalogueRequestDetails> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "necateloguerequestdetailcode", nullable = false)
	private int necateloguerequestdetailcode;

	@Column(name = "necatrequestreqapprovalcode")
	private int necatrequestreqapprovalcode;

	@Column(name = "nbioprojectcode")
	private int nbioprojectcode;

	@Column(name = "nproductcode")
	private int nproductcode;

	@Column(name = "sreqminvolume", length = 10)
	private String sreqminvolume;

	@Column(name = "saccminvolume", length = 10)
	private String saccminvolume;

	@Column(name = "sremarks", length = 255)
	private String sremarks;

	@Column(name = "dtransactiondate")
	private Instant dtransactiondate;

	@Column(name = "ntztransactiondate", nullable = false)
	private int ntztransactiondate;

	@Column(name = "noffsetdtransactiondate", nullable = false)
	private int noffsetdtransactiondate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

    @Column(name = "sparentsamplecode", length = 30)
    private String sparentsamplecode;

    @Column(name = "nreqnoofsamples", nullable = false)
    private short nreqnoofsamples;

    @Column(name = "naccnoofsamples", nullable = false)
    private short naccnoofsamples;
	
	@Transient
	private transient String sproductname;

	@Transient
	private transient String sprojectcode;

	@Transient
	private transient String sprojecttitle;
	
	@Transient
	private transient Integer navailablenoofsample;
	
	@Transient
	private transient String spositionvalue;
	
	@Transient
	private transient String sqty;
	
	@Transient
	private transient int ntransfertype;

	@Override
	public BioEcatalogueRequestDetails mapRow(final ResultSet arg0, final int arg1) throws SQLException {

		final var objbioEcatalogueRequestDetails = new BioEcatalogueRequestDetails();

		objbioEcatalogueRequestDetails
				.setNecateloguerequestdetailcode(getInteger(arg0, "necateloguerequestdetailcode", arg1));
		objbioEcatalogueRequestDetails
				.setNecatrequestreqapprovalcode(getInteger(arg0, "necatrequestreqapprovalcode", arg1));
		objbioEcatalogueRequestDetails.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objbioEcatalogueRequestDetails.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objbioEcatalogueRequestDetails.setSreqminvolume(getString(arg0, "sreqminvolume", arg1));
		objbioEcatalogueRequestDetails.setSaccminvolume(getString(arg0, "saccminvolume", arg1));
		objbioEcatalogueRequestDetails.setSremarks(getString(arg0, "sremarks", arg1));
        objbioEcatalogueRequestDetails.setSparentsamplecode(getString(arg0, "sparentsamplecode", arg1));
        objbioEcatalogueRequestDetails.setSpositionvalue(getString(arg0, "spositionvalue", arg1));
        objbioEcatalogueRequestDetails.setSqty(getString(arg0, "sqty", arg1));
        objbioEcatalogueRequestDetails.setNreqnoofsamples(getShort(arg0, "nreqnoofsamples", arg1));
        objbioEcatalogueRequestDetails.setNaccnoofsamples(getShort(arg0, "naccnoofsamples", arg1));
		objbioEcatalogueRequestDetails.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objbioEcatalogueRequestDetails.setNtztransactiondate(getInteger(arg0, "ntztransactiondate", arg1));
		objbioEcatalogueRequestDetails.setNavailablenoofsample(getInteger(arg0, "navailablenoofsample", arg1));
		objbioEcatalogueRequestDetails.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objbioEcatalogueRequestDetails.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objbioEcatalogueRequestDetails.setNstatus(getShort(arg0, "nstatus", arg1));
		objbioEcatalogueRequestDetails.setSproductname(getString(arg0, "sproductname", arg1));
		objbioEcatalogueRequestDetails.setSprojectcode(getString(arg0, "sprojectcode", arg1));
		objbioEcatalogueRequestDetails.setSprojecttitle(getString(arg0, "sprojecttitle", arg1));
		objbioEcatalogueRequestDetails.setNtransfertype(getInteger(arg0, "ntransfertype", arg1));

		return objbioEcatalogueRequestDetails;
	}
}
