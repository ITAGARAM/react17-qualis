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
@Table(name = "biobankreturndetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioBankReturnDetails extends CustomizedResultsetRowMapper<BioBankReturnDetails>
		implements Serializable, RowMapper<BioBankReturnDetails> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiobankreturndetailscode", nullable = false)
	private int nbiobankreturndetailscode;

	@Column(name = "nbiobankreturncode", nullable = false)
	private int nbiobankreturncode;

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

	@Column(name = "slocationcode", length = 100)
	private String slocationcode;

	@Column(name = "svolume", length = 10)
	private String svolume;

	@Column(name = "sreturnvolume", length = 10)
	private String sreturnvolume;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
//	@Column(name = "ssubjectid", length = 50)
//	private String ssubjectid;
//
//	@Column(name = "scasetype", length = 100)
//	private String scasetype;

	@ColumnDefault("-1")
	@Column(name = "ndiagnostictypecode", nullable = false)
	private int ndiagnostictypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ncontainertypecode", nullable = false)
	private int ncontainertypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
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
	private transient String sbankreturnformnumber;
	
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
	public BioBankReturnDetails mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioBankReturnDetails objBioBankReturnDetails = new BioBankReturnDetails();

		objBioBankReturnDetails.setNbiobankreturndetailscode(getInteger(arg0, "nbiobankreturndetailscode", arg1));
		objBioBankReturnDetails.setNbiobankreturncode(getInteger(arg0, "nbiobankreturncode", arg1));
		objBioBankReturnDetails.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objBioBankReturnDetails.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));
		objBioBankReturnDetails.setNstoragetypecode(getInteger(arg0, "nstoragetypecode", arg1));
		objBioBankReturnDetails.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objBioBankReturnDetails.setSrepositoryid(getString(arg0, "srepositoryid", arg1));
		objBioBankReturnDetails.setNbiosamplereceivingcode(getInteger(arg0, "nbiosamplereceivingcode", arg1));
		objBioBankReturnDetails.setNsamplecondition(getShort(arg0, "nsamplecondition", arg1));
		objBioBankReturnDetails.setNsamplestatus(getShort(arg0, "nsamplestatus", arg1));
		objBioBankReturnDetails.setNreasoncode(getShort(arg0, "nreasoncode", arg1));
		objBioBankReturnDetails.setSlocationcode(getString(arg0, "slocationcode", arg1));
		objBioBankReturnDetails.setSvolume(getString(arg0, "svolume", arg1));
		objBioBankReturnDetails.setSreturnvolume(getString(arg0, "sreturnvolume", arg1));
		objBioBankReturnDetails
				.setNsamplestoragetransactioncode(getInteger(arg0, "nsamplestoragetransactioncode", arg1));
		objBioBankReturnDetails.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioBankReturnDetails.setNtztransactiondate(getInteger(arg0, "ntztransactiondate", arg1));
		objBioBankReturnDetails.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioBankReturnDetails.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioBankReturnDetails.setNstatus(getShort(arg0, "nstatus", arg1));
		objBioBankReturnDetails.setSparentsamplecode(getString(arg0, "sparentsamplecode", arg1));
		objBioBankReturnDetails.setNcohortno(getShort(arg0, "ncohortno", arg1));
		objBioBankReturnDetails.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		objBioBankReturnDetails.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objBioBankReturnDetails.setSsubjectid(getString(arg0, "ssubjectid", arg1));
		objBioBankReturnDetails.setScasetype(getString(arg0, "scasetype", arg1));
		objBioBankReturnDetails.setNdiagnostictypecode(getInteger(arg0, "ndiagnostictypecode", arg1));
		objBioBankReturnDetails.setNcontainertypecode(getInteger(arg0, "ncontainertypecode", arg1));
		objBioBankReturnDetails.setSbankreturnformnumber(getString(arg0, "sbankreturnformnumber", arg1));
		objBioBankReturnDetails.setSproductname(getString(arg0, "sproductname", arg1));
		objBioBankReturnDetails.setSprojecttitle(getString(arg0, "sprojecttitle", arg1));
		objBioBankReturnDetails.setSproductcatname(getString(arg0, "sproductcatname", arg1));
		// added by sujatha ATE_274 for adding it to samplestorageadditionalinfo table column BGSI-218
		objBioBankReturnDetails.setSextractedsampleid(getString(arg0, "sextractedsampleid", arg1));
		objBioBankReturnDetails.setSconcentration(getString(arg0, "sconcentration", arg1));
		objBioBankReturnDetails.setSqcplatform(getString(arg0, "sqcplatform", arg1));
		objBioBankReturnDetails.setSeluent(getString(arg0, "seluent", arg1));
		
		return objBioBankReturnDetails;
	}
}
