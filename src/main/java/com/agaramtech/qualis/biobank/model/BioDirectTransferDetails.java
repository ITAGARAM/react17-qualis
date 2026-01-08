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
@Table(name = "biodirecttransferdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioDirectTransferDetails extends CustomizedResultsetRowMapper<BioDirectTransferDetails>
		implements Serializable, RowMapper<BioDirectTransferDetails> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiodirecttransferdetailscode", nullable = false)
	private int nbiodirecttransferdetailscode;

	@Column(name = "nbiodirecttransfercode", nullable = false)
	private int nbiodirecttransfercode;

	@Column(name = "nbioprojectcode", nullable = false)
	private int nbioprojectcode;

	@Column(name = "nbioparentsamplecode", nullable = false)
	private int nbioparentsamplecode;

	@Column(name = "sparentsamplecode", length = 30)
	private String sparentsamplecode;

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

	@Column(name = "srepositoryid", length = 100)
	private String srepositoryid;

	@Column(name = "svolume", length = 10)
	private String svolume;

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

	@Column(name = "ntransferstatus", nullable = false)
	private short ntransferstatus;

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

//	@Transient
//	private transient String sparentsamplecode;

//	@Transient
//	private transient String ssubjectid;

	@Transient
	private transient String scasetype;

	@Override
	public BioDirectTransferDetails mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioDirectTransferDetails objBioDirectTransferDetails = new BioDirectTransferDetails();

		objBioDirectTransferDetails
				.setNbiodirecttransferdetailscode(getInteger(arg0, "nbiodirecttransferdetailscode", arg1));
		objBioDirectTransferDetails.setNbiodirecttransfercode(getInteger(arg0, "nbiodirecttransfercode", arg1));
		objBioDirectTransferDetails.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objBioDirectTransferDetails.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));
		objBioDirectTransferDetails.setNstoragetypecode(getInteger(arg0, "nstoragetypecode", arg1));
		objBioDirectTransferDetails.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		objBioDirectTransferDetails.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objBioDirectTransferDetails.setNbiosamplereceivingcode(getInteger(arg0, "nbiosamplereceivingcode", arg1));
		objBioDirectTransferDetails.setNsamplecondition(getShort(arg0, "nsamplecondition", arg1));
		objBioDirectTransferDetails.setNtransferstatus(getShort(arg0, "ntransferstatus", arg1));
		objBioDirectTransferDetails.setNreasoncode(getShort(arg0, "nreasoncode", arg1));
		objBioDirectTransferDetails.setSlocationcode(getString(arg0, "slocationcode", arg1));
		objBioDirectTransferDetails
				.setNsamplestoragetransactioncode(getInteger(arg0, "nsamplestoragetransactioncode", arg1));
		objBioDirectTransferDetails.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioDirectTransferDetails.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objBioDirectTransferDetails.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioDirectTransferDetails.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioDirectTransferDetails.setNstatus(getShort(arg0, "nstatus", arg1));
		objBioDirectTransferDetails.setNbiobanksitecode(getInteger(arg0, "nbiobanksitecode", arg1));
		objBioDirectTransferDetails.setDtransferdate(getInstant(arg0, "dtransferdate", arg1));
		objBioDirectTransferDetails.setStransferdate(getString(arg0, "stransferdate", arg1));
		objBioDirectTransferDetails.setSremarks(getString(arg0, "sremarks", arg1));
		objBioDirectTransferDetails.setSsamplecondition(getString(arg0, "ssamplecondition", arg1));
		objBioDirectTransferDetails.setSsamplestatus(getString(arg0, "ssamplestatus", arg1));
		objBioDirectTransferDetails.setSrepositoryid(getString(arg0, "srepositoryid", arg1));
		objBioDirectTransferDetails.setSformnumber(getString(arg0, "sformnumber", arg1));
		objBioDirectTransferDetails.setSreason(getString(arg0, "sreason", arg1));
		objBioDirectTransferDetails.setSparentsamplecode(getString(arg0, "sparentsamplecode", arg1));
		objBioDirectTransferDetails.setNcohortno(getShort(arg0, "ncohortno", arg1));
		objBioDirectTransferDetails.setSvolume(getString(arg0, "svolume", arg1));
		objBioDirectTransferDetails.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objBioDirectTransferDetails.setSsubjectid(getString(arg0, "ssubjectid", arg1));
		objBioDirectTransferDetails.setScasetype(getString(arg0, "scasetype", arg1));
		objBioDirectTransferDetails.setNdiagnostictypecode(getInteger(arg0, "ndiagnostictypecode", arg1));
		objBioDirectTransferDetails.setNcontainertypecode(getShort(arg0, "ncontainertypecode", arg1));

		return objBioDirectTransferDetails;

	}
}
