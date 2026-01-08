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
@Table(name = "biothirdpartyformacceptdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioThirdPartyFormAcceptDetails extends CustomizedResultsetRowMapper<BioThirdPartyFormAcceptDetails>
		implements Serializable, RowMapper<BioThirdPartyFormAcceptDetails> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiothirdpartyformacceptancedetailscode", nullable = false)
	private int nbiothirdpartyformacceptancedetailscode;

	@Column(name = "nbiothirdpartyformacceptancecode", nullable = false)
	private int nbiothirdpartyformacceptancecode;

	@Column(name = "nbioprojectcode", nullable = false)
	private int nbioprojectcode;

	@Column(name = "nbioparentsamplecode", nullable = false)
	private int nbioparentsamplecode;

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

	@ColumnDefault("-1")
	@Column(name = "nproductcode", nullable = false)
	private int nproductcode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "srepositoryid", length = 100)
	private String srepositoryid;

	@Column(name = "svolume", length = 10)
	private String svolume;

	@Column(name = "sreceivedvolume", length = 10)
	private String sreceivedvolume;

	@Column(name = "ssubjectid", length = 50)
	private String ssubjectid;

//	@Column(name = "scasetype", length = 100)
//	private String scasetype;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

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

//	@Column(name = "slocationcode", length = 100)
//	private String slocationcode;

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
	private transient String seluent;

	@Transient
	private transient String sqcplatform;

	@Transient
	private transient String sconcentration;

//	@Transient
//	private transient String sparentsamplecode;

//	@Transient
//	private transient String ssubjectid;

	@Transient
	private transient String scasetype;

	@Transient
	private transient String sextractedsampleid;

	@Transient
	private transient int noriginsitecode;

	@Transient
	private transient int nthirdpartycode;

	@Transient
	private transient String sreferencerepoid;

	@Override
	public BioThirdPartyFormAcceptDetails mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioThirdPartyFormAcceptDetails objBioThirdPartyFormAcceptanceDetails = new BioThirdPartyFormAcceptDetails();

		objBioThirdPartyFormAcceptanceDetails.setNbiothirdpartyformacceptancedetailscode(
				getInteger(arg0, "nbiothirdpartyformacceptancedetailscode", arg1));
		objBioThirdPartyFormAcceptanceDetails
				.setNbiothirdpartyformacceptancecode(getInteger(arg0, "nbiothirdpartyformacceptancecode", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNstoragetypecode(getInteger(arg0, "nstoragetypecode", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objBioThirdPartyFormAcceptanceDetails
				.setNbiosamplereceivingcode(getInteger(arg0, "nbiosamplereceivingcode", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNsamplecondition(getShort(arg0, "nsamplecondition", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNsamplestatus(getShort(arg0, "nsamplestatus", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNreasoncode(getShort(arg0, "nreasoncode", arg1));
//		objBioThirdPartyFormAcceptanceDetails.setSlocationcode(getString(arg0, "slocationcode", arg1));
		objBioThirdPartyFormAcceptanceDetails
				.setNsamplestoragetransactioncode(getInteger(arg0, "nsamplestoragetransactioncode", arg1));
		objBioThirdPartyFormAcceptanceDetails.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNtztransactiondate(getInteger(arg0, "ntztransactiondate", arg1));
		objBioThirdPartyFormAcceptanceDetails
				.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNstatus(getShort(arg0, "nstatus", arg1));
		objBioThirdPartyFormAcceptanceDetails.setSsamplestatus(getString(arg0, "ssamplestatus", arg1));
		objBioThirdPartyFormAcceptanceDetails.setSproductname(getString(arg0, "sproductname", arg1));
		objBioThirdPartyFormAcceptanceDetails.setSproductcatname(getString(arg0, "sproductcatname", arg1));
		objBioThirdPartyFormAcceptanceDetails.setSparentsamplecode(getString(arg0, "sparentsamplecode", arg1));
		objBioThirdPartyFormAcceptanceDetails.setScasetype(getString(arg0, "scasetype", arg1));
		objBioThirdPartyFormAcceptanceDetails.setSprojecttitle(getString(arg0, "sprojecttitle", arg1));
		objBioThirdPartyFormAcceptanceDetails.setSvolume(getString(arg0, "svolume", arg1));
		objBioThirdPartyFormAcceptanceDetails.setSreceivedvolume(getString(arg0, "sreceivedvolume", arg1));
		objBioThirdPartyFormAcceptanceDetails.setSsubjectid(getString(arg0, "ssubjectid", arg1));
		objBioThirdPartyFormAcceptanceDetails.setSsamplecondition(getString(arg0, "ssamplecondition", arg1));
		objBioThirdPartyFormAcceptanceDetails.setSrepositoryid(getString(arg0, "srepositoryid", arg1));
		objBioThirdPartyFormAcceptanceDetails.setSformnumber(getString(arg0, "sformnumber", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNcohortno(getShort(arg0, "ncohortno", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNdiagnostictypecode(getInteger(arg0, "ndiagnostictypecode", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNcontainertypecode(getInteger(arg0, "ncontainertypecode", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNinstrumentcode(getInteger(arg0, "ninstrumentcode", arg1));
		objBioThirdPartyFormAcceptanceDetails.setSreason(getString(arg0, "sreason", arg1));
		objBioThirdPartyFormAcceptanceDetails.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objBioThirdPartyFormAcceptanceDetails.setSeluent(getString(arg0, "seluent", arg1));
		objBioThirdPartyFormAcceptanceDetails.setSqcplatform(getString(arg0, "sqcplatform", arg1));
		objBioThirdPartyFormAcceptanceDetails.setSconcentration(getString(arg0, "sconcentration", arg1));
		objBioThirdPartyFormAcceptanceDetails.setSextractedsampleid(getString(arg0, "sextractedsampleid", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNoriginsitecode(getInteger(arg0, "noriginsitecode", arg1));
		objBioThirdPartyFormAcceptanceDetails.setNthirdpartycode(getInteger(arg0, "nthirdpartycode", arg1));
		objBioThirdPartyFormAcceptanceDetails.setSreferencerepoid(getString(arg0, "sreferencerepoid", arg1));

		return objBioThirdPartyFormAcceptanceDetails;
	}
}
