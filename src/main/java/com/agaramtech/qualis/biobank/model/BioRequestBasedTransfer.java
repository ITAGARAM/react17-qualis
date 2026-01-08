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
@Table(name = "biorequestbasedtransfer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioRequestBasedTransfer extends CustomizedResultsetRowMapper<BioRequestBasedTransfer>
		implements Serializable, RowMapper<BioRequestBasedTransfer> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiorequestbasedtransfercode", nullable = false)
	private int nbiorequestbasedtransfercode;

	@Column(name = "sformnumber", length = 50, nullable = false)
	private String sformnumber;

	@Column(name = "ntransfertypecode", nullable = false)
	private short ntransfertypecode;

	@Column(name = "nthirdpartycode", nullable = false)
	private short nthirdpartycode;

	@Column(name = "nreceiversitecode", nullable = false)
	private short nreceiversitecode;

	@Column(name = "dtransferdate", nullable = false)
	private Instant dtransferdate;

	@ColumnDefault("-1")
	@Column(name = "ntztransferdate", nullable = false)
	private short ntztransferdate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsetdtransferdate", nullable = false)
	private int noffsetdtransferdate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@Column(name = "nstorageconditioncode")
	private int nstorageconditioncode;

	@Column(name = "ddeliverydate")
	private Instant ddeliverydate;

	@ColumnDefault("-1")
	@Column(name = "ntzdeliverydate", nullable = false)
	private short ntzdeliverydate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsetddeliverydate", nullable = false)
	private int noffsetddeliverydate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@Column(name = "ndispatchercode")
	private int ndispatchercode;

	@Column(name = "ncouriercode")
	private int ncouriercode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

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
	private transient int nserialno;
	
	@Transient
	private transient int nbiorequestbasedtransferdetailcode;
	
	@Transient
	private transient String ssamplecondition;
	
	@Transient
	private transient int nbioparentsamplecode;

	@Transient
	private transient int nbioprojectcode;

	@Transient
	private transient int nproductcode;

	@Transient
	private transient int nstoragetypecode;

	@Transient
	private transient String scolorhexcode;

	@Transient
	private transient String sdeliverydate;

	@Transient
	private transient String sdispatchername;

	@Transient
	private transient String sproductname;

	@Transient
	private transient String sreceiversitename;

	@Transient
	private transient String srepositoryid;

	@Transient
	private transient String sstoragetypename;

	@Transient
	private transient String stransdisplaystatus;

	@Transient
	private transient String stransferdate;

	@Transient
	private transient String stransferstatus;

	@Transient
	private transient String sreason;
	
	@Transient
	private transient String originsite;
	
	@Transient
	private transient String stransfertypename;

	@Transient
	private transient int necatrequestreqapprovalcode;

	@Transient
	private transient String ssamplestoragetransactioncode;

	@Transient
	private transient String sstorageconditionname;

	@Transient
	private transient String sformname;

	@Transient
	private transient String slocationcode;

	@Transient
	private transient String sparentsamplecode;

	@Transient
	private transient String srequestformno;

	@Transient
	private transient String svolume;

	@Transient
	private transient int norginsitecode;

	@Transient
	private transient int nreqformtypecode;

	@Transient
	private transient String sbioparentsamplecode;

	@Transient
	private transient String scouriername;

	@Transient
	private transient String sremarks;

	@Transient
	private transient String striplepackage;

	@Transient
	private transient String svalidationremarks;

	@Transient
	private transient String scourierno;

	@Override
	public BioRequestBasedTransfer mapRow(final ResultSet arg0, final int arg1) throws SQLException {

		final var objBioRequestTransfer = new BioRequestBasedTransfer();

		objBioRequestTransfer.setNserialno(getInteger(arg0, "nserialno", arg1));
		objBioRequestTransfer.setNbiorequestbasedtransferdetailcode(getInteger(arg0, "nbiorequestbasedtransferdetailcode", arg1));
		objBioRequestTransfer.setSsamplecondition(getString(arg0, "ssamplecondition", arg1));
		objBioRequestTransfer.setNbiorequestbasedtransfercode(getInteger(arg0, "nbiorequestbasedtransfercode", arg1));
		objBioRequestTransfer.setSformnumber(getString(arg0, "sformnumber", arg1));
		objBioRequestTransfer.setNtransfertypecode(getShort(arg0, "ntransfertypecode", arg1));
		objBioRequestTransfer.setNthirdpartycode(getShort(arg0, "nthirdpartycode", arg1));
		objBioRequestTransfer.setNreceiversitecode(getShort(arg0, "nreceiversitecode", arg1));
		objBioRequestTransfer.setDtransferdate(getInstant(arg0, "dtransferdate", arg1));
		objBioRequestTransfer.setNtztransferdate(getShort(arg0, "ntztransferdate", arg1));
		objBioRequestTransfer.setNoffsetdtransferdate(getInteger(arg0, "noffsetdtransferdate", arg1));
		objBioRequestTransfer.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objBioRequestTransfer.setSremarks(getString(arg0, "sremarks", arg1));
		objBioRequestTransfer.setNstorageconditioncode(getInteger(arg0, "nstorageconditioncode", arg1));
		objBioRequestTransfer.setDdeliverydate(getInstant(arg0, "ddeliverydate", arg1));
		objBioRequestTransfer.setNtzdeliverydate(getShort(arg0, "ntzdeliverydate", arg1));
		objBioRequestTransfer.setNoffsetddeliverydate(getShort(arg0, "noffsetddeliverydate", arg1));
		objBioRequestTransfer.setNdispatchercode(getInteger(arg0, "ndispatchercode", arg1));
		objBioRequestTransfer.setNcouriercode(getInteger(arg0, "ncouriercode", arg1));
		objBioRequestTransfer.setScourierno(getString(arg0, "scourierno", arg1));
		objBioRequestTransfer.setStriplepackage(getString(arg0, "striplepackage", arg1));
		objBioRequestTransfer.setSvalidationremarks(getString(arg0, "svalidationremarks", arg1));
		objBioRequestTransfer.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioRequestTransfer.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objBioRequestTransfer.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioRequestTransfer.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioRequestTransfer.setNstatus(getShort(arg0, "nstatus", arg1));
		objBioRequestTransfer.setStransfertypename(getString(arg0, "stransfertypename", arg1));
		objBioRequestTransfer.setSreceiversitename(getString(arg0, "sreceiversitename", arg1));
		objBioRequestTransfer.setStransferdate(getString(arg0, "stransferdate", arg1));
		objBioRequestTransfer.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objBioRequestTransfer.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		objBioRequestTransfer.setSdeliverydate(getString(arg0, "sdeliverydate", arg1));
		objBioRequestTransfer.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objBioRequestTransfer.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));
		objBioRequestTransfer.setNstoragetypecode(getInteger(arg0, "nstoragetypecode", arg1));
		objBioRequestTransfer.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objBioRequestTransfer.setSstoragetypename(getString(arg0, "sstoragetypename", arg1));
		objBioRequestTransfer.setSproductname(getString(arg0, "sproductname", arg1));
		objBioRequestTransfer.setSrepositoryid(getString(arg0, "srepositoryid", arg1));
		objBioRequestTransfer.setSdispatchername(getString(arg0, "sdispatchername", arg1));
		objBioRequestTransfer.setStransferstatus(getString(arg0, "stransferstatus", arg1));
		objBioRequestTransfer.setNecatrequestreqapprovalcode(getShort(arg0, "necatrequestreqapprovalcode", arg1));
		objBioRequestTransfer.setSsamplestoragetransactioncode(getString(arg0, "ssamplestoragetransactioncode", arg1));
		objBioRequestTransfer.setSstorageconditionname(getString(arg0, "sstorageconditionname", arg1));
		objBioRequestTransfer.setSformname(getString(arg0, "sformname", arg1));
		objBioRequestTransfer.setSlocationcode(getString(arg0, "slocationcode", arg1));
		objBioRequestTransfer.setSparentsamplecode(getString(arg0, "sparentsamplecode", arg1));
		objBioRequestTransfer.setSrequestformno(getString(arg0, "srequestformno", arg1));
		objBioRequestTransfer.setSvolume(getString(arg0, "svolume", arg1));
		objBioRequestTransfer.setNorginsitecode(getInteger(arg0, "norginsitecode", arg1));
		objBioRequestTransfer.setNreqformtypecode(getInteger(arg0, "nreqformtypecode", arg1));
		objBioRequestTransfer.setSbioparentsamplecode(getString(arg0, "sbioparentsamplecode", arg1));
		objBioRequestTransfer.setScouriername(getString(arg0, "scouriername", arg1));

		return objBioRequestTransfer;

	}
}
