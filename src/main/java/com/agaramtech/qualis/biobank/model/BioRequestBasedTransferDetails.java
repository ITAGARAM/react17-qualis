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
@Table(name = "biorequestbasedtransferdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioRequestBasedTransferDetails extends CustomizedResultsetRowMapper<BioRequestBasedTransferDetails>
		implements Serializable, RowMapper<BioRequestBasedTransferDetails> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiorequestbasedtransferdetailcode", nullable = false)
	private int nbiorequestbasedtransferdetailcode;

	@Column(name = "nbiorequestbasedtransfercode", nullable = false)
	private int nbiorequestbasedtransfercode;

	@Column(name = "nbioprojectcode", nullable = false)
	private int nbioprojectcode;

	@Column(name = "nbioparentsamplecode", nullable = false)
	private int nbioparentsamplecode;

	@Column(name = "necatrequestreqapprovalcode", nullable = false)
	private int necatrequestreqapprovalcode;

	@Column(name = "nreqformtypecode", nullable = false)
	private short nreqformtypecode;

	@Column(name = "nsamplestoragetransactioncode", nullable = false)
	private int nsamplestoragetransactioncode;

	@Column(name = "nbiosamplereceivingcode", nullable = false)
	private int nbiosamplereceivingcode;

	@ColumnDefault("-1")
	@Column(name = "ncohortno", nullable = false)
	private short ncohortno = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nstoragetypecode", nullable = false)
	private int nstoragetypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nproductcatcode", nullable = false)
	private int nproductcatcode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nproductcode", nullable = false)
	private int nproductcode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nsamplecondition", nullable = false)
	private short nsamplecondition;

	@Column(name = "srepositoryid", length = 100)
	private String srepositoryid;

	@Column(name = "slocationcode", length = 100)
	private String slocationcode;

	@Column(name = "svolume", length = 10)
	private String svolume;

	@Column(name = "ssubjectid", length = 50)
	private String ssubjectid;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@ColumnDefault("-1")
	@Column(name = "ndiagnostictypecode", nullable = false)
	private int ndiagnostictypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ncontainertypecode", nullable = false)
	private short ncontainertypecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ntransferstatus", nullable = false)
	private short ntransferstatus;

	@ColumnDefault("-1")
	@Column(name = "nreasoncode", nullable = false)
	private short nreasoncode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;

	@ColumnDefault("-1")
	@Column(name = "ntztransactiondate", nullable = false)
	private short ntztransactiondate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

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
	private transient int nbiobanksitecode;

	@Transient
	private transient Instant dtransferdate;

	@Transient
	private transient String stransferdate;

	@Transient
	private transient String sremarks;

	@Transient
	private transient String ssamplecondition;

	@Transient
	private transient String ssamplestatus;

	@Transient
	private transient String sformnumber;

	@Transient
	private transient String sreason;

	@Transient
	private transient String sparentsamplecode;

	@Transient
	private transient String scasetype;

	@Override
	public BioRequestBasedTransferDetails mapRow(ResultSet arg0, int arg1) throws SQLException {

		final var objBioRequestBasedTransferDetails = new BioRequestBasedTransferDetails();

		objBioRequestBasedTransferDetails
				.setNbiorequestbasedtransferdetailcode(getInteger(arg0, "nbiorequestbasedtransferdetailcode", arg1));
		objBioRequestBasedTransferDetails
				.setNbiorequestbasedtransfercode(getInteger(arg0, "nbiorequestbasedtransfercode", arg1));
		objBioRequestBasedTransferDetails.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objBioRequestBasedTransferDetails.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));
		objBioRequestBasedTransferDetails.setNstoragetypecode(getInteger(arg0, "nstoragetypecode", arg1));
		objBioRequestBasedTransferDetails.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objBioRequestBasedTransferDetails.setNbiosamplereceivingcode(getInteger(arg0, "nbiosamplereceivingcode", arg1));
		objBioRequestBasedTransferDetails.setNsamplecondition(getShort(arg0, "nsamplecondition", arg1));
		objBioRequestBasedTransferDetails.setNtransferstatus(getShort(arg0, "ntransferstatus", arg1));
		objBioRequestBasedTransferDetails.setNreasoncode(getShort(arg0, "nreasoncode", arg1));
		objBioRequestBasedTransferDetails.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioRequestBasedTransferDetails.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objBioRequestBasedTransferDetails.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioRequestBasedTransferDetails.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioRequestBasedTransferDetails.setNstatus(getShort(arg0, "nstatus", arg1));
		objBioRequestBasedTransferDetails.setNbiobanksitecode(getInteger(arg0, "nbiobanksitecode", arg1));
		objBioRequestBasedTransferDetails.setDtransferdate(getInstant(arg0, "dtransferdate", arg1));
		objBioRequestBasedTransferDetails.setStransferdate(getString(arg0, "stransferdate", arg1));
		objBioRequestBasedTransferDetails.setSremarks(getString(arg0, "sremarks", arg1));
		objBioRequestBasedTransferDetails.setSsamplecondition(getString(arg0, "ssamplecondition", arg1));
		objBioRequestBasedTransferDetails.setSsamplestatus(getString(arg0, "ssamplestatus", arg1));
		objBioRequestBasedTransferDetails.setSrepositoryid(getString(arg0, "srepositoryid", arg1));
		objBioRequestBasedTransferDetails.setSformnumber(getString(arg0, "sformnumber", arg1));
		objBioRequestBasedTransferDetails.setSreason(getString(arg0, "sreason", arg1));
		objBioRequestBasedTransferDetails.setSvolume(getString(arg0, "svolume", arg1));
		objBioRequestBasedTransferDetails.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objBioRequestBasedTransferDetails.setSsubjectid(getString(arg0, "ssubjectid", arg1));
		objBioRequestBasedTransferDetails.setScasetype(getString(arg0, "scasetype", arg1));
		objBioRequestBasedTransferDetails
				.setNecatrequestreqapprovalcode(getInteger(arg0, "necatrequestreqapprovalcode", arg1));
		objBioRequestBasedTransferDetails.setNreqformtypecode(getShort(arg0, "nreqformtypecode", arg1));
		objBioRequestBasedTransferDetails
				.setNsamplestoragetransactioncode(getInteger(arg0, "nsamplestoragetransactioncode", arg1));
		objBioRequestBasedTransferDetails.setNcohortno(getShort(arg0, "ncohortno", arg1));
		objBioRequestBasedTransferDetails.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		objBioRequestBasedTransferDetails.setSlocationcode(getString(arg0, "slocationcode", arg1));
		objBioRequestBasedTransferDetails.setNdiagnostictypecode(getInteger(arg0, "ndiagnostictypecode", arg1));
		objBioRequestBasedTransferDetails.setNcontainertypecode(getShort(arg0, "ncontainertypecode", arg1));

		return objBioRequestBasedTransferDetails;

	}
}
