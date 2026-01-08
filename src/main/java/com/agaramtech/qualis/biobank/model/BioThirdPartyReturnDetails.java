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
@Table(name = "biothirdpartyreturndetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioThirdPartyReturnDetails extends CustomizedResultsetRowMapper<BioThirdPartyReturnDetails>
		implements Serializable, RowMapper<BioThirdPartyReturnDetails> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiothirdpartyreturndetailscode", nullable = false)
	private int nbiothirdpartyreturndetailscode;

	@Column(name = "nbiothirdpartyreturncode", nullable = false)
	private int nbiothirdpartyreturncode;

	@Column(name = "nbiothirdpartyformacceptancecode", nullable = false)
	private int nbiothirdpartyformacceptancecode;
	
	@Column(name = "nbiothirdpartyformacceptancedetailscode", nullable = false)
	private int nbiothirdpartyformacceptancedetailscode;
	
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

	@Column(name = "nproductcode", nullable = false)
	private int nproductcode;

	@Column(name = "srepositoryid", length = 100)
	private String srepositoryid;
	
	@Column(name = "nbiosamplereceivingcode", nullable = false)
	private int nbiosamplereceivingcode;

	@Column(name = "nsamplecondition", nullable = false)
	private short nsamplecondition;

	@Column(name = "nsamplestatus", nullable = false)
	private short nsamplestatus;

	@Column(name = "nreasoncode", nullable = false)
	private short nreasoncode;

//	@Column(name = "slocationcode", length = 100)
//	private String slocationcode;

	@Column(name = "svolume", length = 10)
	private String svolume;

	@Column(name = "sreturnvolume", length = 10)
	private String sreturnvolume;

//	@Column(name = "ssubjectid", length = 50)
//	private String ssubjectid;
//
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
	
	@ColumnDefault("-1")
	@Column(name = "nsamplestoragetransactioncode", nullable = false)
	private int nsamplestoragetransactioncode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("4")
	@Column(name = "nisexternalsample", nullable = false)
	private short nisexternalsample = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();
	
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
	private transient String sthirdpartyreturnformnumber;
	
	@Transient
	private transient String sproductname;
	
	@Transient
	private transient String sprojecttitle;
	
	@Transient
	private transient String sproductcatname;
	
	@Transient
	private transient String sparentsamplecode;
	
	@Transient
	private transient String ssubjectid;
	
	@Transient
	private transient String scasetype;

	@Override
	public BioThirdPartyReturnDetails mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioThirdPartyReturnDetails objBioThirdPartyReturnDetails = new BioThirdPartyReturnDetails();

		objBioThirdPartyReturnDetails.setNbiothirdpartyreturndetailscode(getInteger(arg0, "nbiothirdpartyreturndetailscode", arg1));
		objBioThirdPartyReturnDetails.setNbiothirdpartyreturncode(getInteger(arg0, "nbiothirdpartyreturncode", arg1));
		objBioThirdPartyReturnDetails.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objBioThirdPartyReturnDetails.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));
		objBioThirdPartyReturnDetails.setNstoragetypecode(getInteger(arg0, "nstoragetypecode", arg1));
		objBioThirdPartyReturnDetails.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objBioThirdPartyReturnDetails.setSrepositoryid(getString(arg0, "srepositoryid", arg1));
		objBioThirdPartyReturnDetails.setNbiosamplereceivingcode(getInteger(arg0, "nbiosamplereceivingcode", arg1));
		objBioThirdPartyReturnDetails.setNsamplecondition(getShort(arg0, "nsamplecondition", arg1));
		objBioThirdPartyReturnDetails.setNsamplestatus(getShort(arg0, "nsamplestatus", arg1));
		objBioThirdPartyReturnDetails.setNreasoncode(getShort(arg0, "nreasoncode", arg1));
//		objBioThirdPartyReturnDetails.setSlocationcode(getString(arg0, "slocationcode", arg1));
		objBioThirdPartyReturnDetails.setSvolume(getString(arg0, "svolume", arg1));
		objBioThirdPartyReturnDetails.setSreturnvolume(getString(arg0, "sreturnvolume", arg1));
		objBioThirdPartyReturnDetails
				.setNsamplestoragetransactioncode(getInteger(arg0, "nsamplestoragetransactioncode", arg1));
		objBioThirdPartyReturnDetails.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioThirdPartyReturnDetails.setNtztransactiondate(getInteger(arg0, "ntztransactiondate", arg1));
		objBioThirdPartyReturnDetails.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioThirdPartyReturnDetails.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioThirdPartyReturnDetails.setNstatus(getShort(arg0, "nstatus", arg1));
		objBioThirdPartyReturnDetails.setSparentsamplecode(getString(arg0, "sparentsamplecode", arg1));
		objBioThirdPartyReturnDetails.setNcohortno(getShort(arg0, "ncohortno", arg1));
		objBioThirdPartyReturnDetails.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		objBioThirdPartyReturnDetails.setSsubjectid(getString(arg0, "ssubjectid", arg1));
		objBioThirdPartyReturnDetails.setScasetype(getString(arg0, "scasetype", arg1));
		objBioThirdPartyReturnDetails.setNdiagnostictypecode(getInteger(arg0, "ndiagnostictypecode", arg1));
		objBioThirdPartyReturnDetails.setNcontainertypecode(getInteger(arg0, "ncontainertypecode", arg1));
		objBioThirdPartyReturnDetails.setSthirdpartyreturnformnumber(getString(arg0, "sthirdpartyreturnformnumber", arg1));
		objBioThirdPartyReturnDetails.setSproductname(getString(arg0, "sproductname", arg1));
		objBioThirdPartyReturnDetails.setSprojecttitle(getString(arg0, "sprojecttitle", arg1));
		objBioThirdPartyReturnDetails.setSproductcatname(getString(arg0, "sproductcatname", arg1));
		objBioThirdPartyReturnDetails.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objBioThirdPartyReturnDetails.setNisexternalsample(getShort(arg0, "nisexternalsample", arg1));
		
		return objBioThirdPartyReturnDetails;
	}
}
