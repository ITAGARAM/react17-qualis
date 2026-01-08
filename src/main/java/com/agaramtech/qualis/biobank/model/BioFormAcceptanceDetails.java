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
@Table(name = "bioformacceptancedetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioFormAcceptanceDetails extends CustomizedResultsetRowMapper<BioFormAcceptanceDetails>
		implements Serializable, RowMapper<BioFormAcceptanceDetails> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbioformacceptancedetailscode", nullable = false)
	private int nbioformacceptancedetailscode;

	@Column(name = "nbioformacceptancecode", nullable = false)
	private int nbioformacceptancecode;

	@Column(name = "nbioprojectcode", nullable = false)
	private int nbioprojectcode;

	@Column(name = "nbioparentsamplecode", nullable = false)
	private int nbioparentsamplecode;

//	@Column(name = "sparentsamplecode", length = 30)
//	private String sparentsamplecode;

	@ColumnDefault("-1")
	@Column(name = "ncohortno", nullable = false)
	private short ncohortno = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstoragetypecode", nullable = false)
	private int nstoragetypecode;

	@ColumnDefault("-1")
	@Column(name = "nproductcatcode", nullable = false)
	private int nproductcatcode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nproductcode", nullable = false)
	private int nproductcode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "srepositoryid", length = 100)
	private String srepositoryid;

	@Column(name = "svolume", length = 10)
	private String svolume;
	
	@Column(name = "sreceivedvolume", length = 10)
	private String sreceivedvolume;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Column(name = "ssubjectid", length = 50)
	private String ssubjectid;

//	@Column(name = "scasetype", length = 100)
//	private String scasetype;

	@ColumnDefault("-1")
	@Column(name = "ndiagnostictypecode", nullable = false)
	private int ndiagnostictypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ncontainertypecode", nullable = false)
	private int ncontainertypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nbiosamplereceivingcode", nullable = false)
	private int nbiosamplereceivingcode;

	@Column(name = "nsamplecondition", nullable = false)
	private short nsamplecondition;

	@Column(name = "nsamplestatus", nullable = false)
	private short nsamplestatus;

	@ColumnDefault("-1")
	@Column(name = "nreasoncode", nullable = false)
	private short nreasoncode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "slocationcode", length = 100)
	private String slocationcode;

	@ColumnDefault("-1")
	@Column(name = "nsamplestoragetransactioncode", nullable = false)
	private int nsamplestoragetransactioncode = Enumeration.TransactionStatus.NA.gettransactionstatus();

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
	private transient String ssamplecondition;

	@Transient
	private transient String ssamplestatus;

	@Transient
	private transient String sformnumber;

	@Transient
	private transient String sproductname;

	@Transient
	private transient String sproductcatname;

	@Transient
	private transient String sprojecttitle;

	@Transient
	private transient int ninstrumentcode;
	
	@Transient
	private transient String sreason;
	
	@Transient
	private transient String sparentsamplecode;
	
//	@Transient
//	private transient String ssubjectid;
	
	@Transient
	private transient String scasetype;
	
	// added by sujatha ATE_274 for adding it to jsondata column BGSI-218
	@Transient
	private transient String sextractedsampleid;
	
	@Transient
	private transient String sconcentration;
	
	@Transient
	private transient String sqcplatform;
	
	@Transient
	private transient String seluent;

	@Override
	public BioFormAcceptanceDetails mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioFormAcceptanceDetails objBioFormAcceptanceDetails = new BioFormAcceptanceDetails();

		objBioFormAcceptanceDetails
				.setNbioformacceptancedetailscode(getInteger(arg0, "nbioformacceptancedetailscode", arg1));
		objBioFormAcceptanceDetails.setNbioformacceptancecode(getInteger(arg0, "nbioformacceptancecode", arg1));
		objBioFormAcceptanceDetails.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objBioFormAcceptanceDetails.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));
		objBioFormAcceptanceDetails.setNstoragetypecode(getInteger(arg0, "nstoragetypecode", arg1));
		objBioFormAcceptanceDetails.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objBioFormAcceptanceDetails.setNbiosamplereceivingcode(getInteger(arg0, "nbiosamplereceivingcode", arg1));
		objBioFormAcceptanceDetails.setNsamplecondition(getShort(arg0, "nsamplecondition", arg1));
		objBioFormAcceptanceDetails.setNsamplestatus(getShort(arg0, "nsamplestatus", arg1));
		objBioFormAcceptanceDetails.setNreasoncode(getShort(arg0, "nreasoncode", arg1));
		objBioFormAcceptanceDetails.setSlocationcode(getString(arg0, "slocationcode", arg1));
		objBioFormAcceptanceDetails
				.setNsamplestoragetransactioncode(getInteger(arg0, "nsamplestoragetransactioncode", arg1));
		objBioFormAcceptanceDetails.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioFormAcceptanceDetails.setNtztransactiondate(getInteger(arg0, "ntztransactiondate", arg1));
		objBioFormAcceptanceDetails.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioFormAcceptanceDetails.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioFormAcceptanceDetails.setNstatus(getShort(arg0, "nstatus", arg1));
		objBioFormAcceptanceDetails.setSsamplestatus(getString(arg0, "ssamplestatus", arg1));
		objBioFormAcceptanceDetails.setSproductname(getString(arg0, "sproductname", arg1));
		objBioFormAcceptanceDetails.setSproductcatname(getString(arg0, "sproductcatname", arg1));
		objBioFormAcceptanceDetails.setSparentsamplecode(getString(arg0, "sparentsamplecode", arg1));
		objBioFormAcceptanceDetails.setScasetype(getString(arg0, "scasetype", arg1));
		objBioFormAcceptanceDetails.setSprojecttitle(getString(arg0, "sprojecttitle", arg1));
		objBioFormAcceptanceDetails.setSvolume(getString(arg0, "svolume", arg1));
		objBioFormAcceptanceDetails.setSreceivedvolume(getString(arg0, "sreceivedvolume", arg1));
		objBioFormAcceptanceDetails.setSsubjectid(getString(arg0, "ssubjectid", arg1));
		objBioFormAcceptanceDetails.setSsamplecondition(getString(arg0, "ssamplecondition", arg1));
		objBioFormAcceptanceDetails.setSrepositoryid(getString(arg0, "srepositoryid", arg1));
		objBioFormAcceptanceDetails.setSformnumber(getString(arg0, "sformnumber", arg1));
		objBioFormAcceptanceDetails.setNcohortno(getShort(arg0, "ncohortno", arg1));
		objBioFormAcceptanceDetails.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		objBioFormAcceptanceDetails.setNdiagnostictypecode(getInteger(arg0, "ndiagnostictypecode", arg1));
		objBioFormAcceptanceDetails.setNcontainertypecode(getInteger(arg0, "ncontainertypecode", arg1));
		objBioFormAcceptanceDetails.setNinstrumentcode(getInteger(arg0, "ninstrumentcode", arg1));
		objBioFormAcceptanceDetails.setSreason(getString(arg0, "sreason", arg1));
		objBioFormAcceptanceDetails.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
	// added by sujatha ATE_274 for adding it to jsondata column BGSI-218
		objBioFormAcceptanceDetails.setSextractedsampleid(getString(arg0, "sextractedsampleid", arg1));
		objBioFormAcceptanceDetails.setSconcentration(getString(arg0, "sconcentration", arg1));
		objBioFormAcceptanceDetails.setSqcplatform(getString(arg0, "sqcplatform", arg1));
		objBioFormAcceptanceDetails.setSeluent(getString(arg0, "seluent", arg1));

		return objBioFormAcceptanceDetails;
	}
}
