package com.agaramtech.qualis.biobank.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "biodisposeformdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioDisposeFormDetails extends CustomizedResultsetRowMapper<BioDisposeFormDetails>
implements Serializable, RowMapper<BioDisposeFormDetails> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiodisposeformdetailscode", nullable = false)
	private int nbiodisposeformdetailscode;

	@Column(name = "nbiodisposeformcode", nullable = false)
	private int nbiodisposeformcode;

	@Column(name = "nbioprojectcode", nullable = false)
	private int nbioprojectcode;

	@ColumnDefault("-1")
	@Column(name = "nsamplestoragetransactioncode", nullable = false)
	private int nsamplestoragetransactioncode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nbiomovetodisposedetailscode", nullable = false)
	private int nbiomovetodisposedetailscode = Enumeration.TransactionStatus.NA.gettransactionstatus();


	@Column(name = "sparentsamplecode", length = 30)
	private String sparentsamplecode;

	@ColumnDefault("-1")
	@Column(name = "ncohortno", nullable = false)
	private short ncohortno = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstoragetypecode", nullable = false)
	private int nstoragetypecode;

	@ColumnDefault("-1")
	@Column(name = "nproductcatcode", nullable = false)
	private int nproductcatcode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nproductcode", nullable = false)
	private int nproductcode;

	@Column(name = "nsamplecondition", nullable = false)
	private short nsamplecondition;

	@Column(name = "srepositoryid", length = 100)
	private String srepositoryid;

	@Column(name = "slocationcode", length = 100)
	private String slocationcode;

	@Column(name = "svolume", length = 10)
	private String svolume;
	
	//added by sujatha ATE_274 for added new jsondata column BGSI-218
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Column(name = "ssubjectid", length = 50)
	private String ssubjectid;

	@Column(name = "scasetype", length = 100)
	private String scasetype;

	@ColumnDefault("-1")
	@Column(name = "ndiagnostictypecode", nullable = false)
	private int ndiagnostictypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ncontainertypecode", nullable = false)
	private int ncontainertypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nreasoncode", nullable = false)
	private short nreasoncode;

	@Column(name = "ntransdisposestatus", nullable = false)
	private short ntransdisposestatus;

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;

	@ColumnDefault("-1")
	@Column(name = "ntztransactiondate", nullable = false)
	private int ntztransactiondate = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsetdtransactiondate", nullable = false)
	private int noffsetdtransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();


	@Transient
	private transient String sproductname;

	@Transient
	private transient String sprojecttitle;

	@Transient
	private transient String sproductcatname;
	
	// added by sujatha ATE_274 for adding it to samplestorageadditioninfo table column BGSI-218
	@Transient
	private transient String sextractedsampleid;
		
	@Transient
	private transient String sconcentration;
		
	@Transient
	private transient String sqcplatform;
		
	@Transient
	private transient String seluent;

	@Override
	public BioDisposeFormDetails mapRow(final ResultSet arg0, final int arg1) throws SQLException {

		final var objBioDisposeFormDetails = new BioDisposeFormDetails();

		objBioDisposeFormDetails.setNbiodisposeformdetailscode(getInteger(arg0, "nbiodisposeformdetailscode", arg1));
		objBioDisposeFormDetails.setNbiodisposeformcode(getInteger(arg0, "nbiodisposeformcode", arg1));
		objBioDisposeFormDetails.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objBioDisposeFormDetails
		.setNsamplestoragetransactioncode(getInteger(arg0, "nsamplestoragetransactioncode", arg1));
		objBioDisposeFormDetails
		.setNbiomovetodisposedetailscode(getInteger(arg0, "nbiomovetodisposedetailscode", arg1));
		objBioDisposeFormDetails.setNstoragetypecode(getInteger(arg0, "nstoragetypecode", arg1));
		objBioDisposeFormDetails.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objBioDisposeFormDetails.setSrepositoryid(getString(arg0, "srepositoryid", arg1));
		objBioDisposeFormDetails.setNsamplecondition(getShort(arg0, "nsamplecondition", arg1));
		objBioDisposeFormDetails.setNtransdisposestatus(getShort(arg0, "ntransdisposestatus", arg1));
		objBioDisposeFormDetails.setNreasoncode(getShort(arg0, "nreasoncode", arg1));
		objBioDisposeFormDetails.setSlocationcode(getString(arg0, "slocationcode", arg1));
		objBioDisposeFormDetails.setSvolume(getString(arg0, "svolume", arg1));
		objBioDisposeFormDetails.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioDisposeFormDetails.setNtztransactiondate(getInteger(arg0, "ntztransactiondate", arg1));
		objBioDisposeFormDetails.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioDisposeFormDetails.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioDisposeFormDetails.setNstatus(getShort(arg0, "nstatus", arg1));
		objBioDisposeFormDetails.setSparentsamplecode(getString(arg0, "sparentsamplecode", arg1));
		objBioDisposeFormDetails.setNcohortno(getShort(arg0, "ncohortno", arg1));
		objBioDisposeFormDetails.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		objBioDisposeFormDetails.setSsubjectid(getString(arg0, "ssubjectid", arg1));
		objBioDisposeFormDetails.setScasetype(getString(arg0, "scasetype", arg1));
		objBioDisposeFormDetails.setNdiagnostictypecode(getInteger(arg0, "ndiagnostictypecode", arg1));
		objBioDisposeFormDetails.setNcontainertypecode(getInteger(arg0, "ncontainertypecode", arg1));


		objBioDisposeFormDetails.setSproductname(getString(arg0, "sproductname", arg1));
		objBioDisposeFormDetails.setSprojecttitle(getString(arg0, "sprojecttitle", arg1));
		objBioDisposeFormDetails.setSproductcatname(getString(arg0, "sproductcatname", arg1));
		// added by sujatha ATE_274 for added jsondata column BGSI-218
		objBioDisposeFormDetails.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		// added by sujatha ATE_274 for adding it to samplestorageadditionalinfo table column BGSI-218
		objBioDisposeFormDetails.setSextractedsampleid(getString(arg0, "sextractedsampleid", arg1));
		objBioDisposeFormDetails.setSconcentration(getString(arg0, "sconcentration", arg1));
		objBioDisposeFormDetails.setSqcplatform(getString(arg0, "sqcplatform", arg1));
		objBioDisposeFormDetails.setSeluent(getString(arg0, "seluent", arg1));

		return objBioDisposeFormDetails;
	}
}
