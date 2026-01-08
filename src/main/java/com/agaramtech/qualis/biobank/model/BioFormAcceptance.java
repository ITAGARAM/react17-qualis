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
@Table(name = "bioformacceptance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioFormAcceptance extends CustomizedResultsetRowMapper<BioFormAcceptance>
		implements Serializable, RowMapper<BioFormAcceptance> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbioformacceptancecode", nullable = false)
	private int nbioformacceptancecode;

	@Column(name = "nbiodirecttransfercode", nullable = false)
	private int nbiodirecttransfercode;

	@Column(name = "nbiorequestbasedtransfercode", nullable = false)
	private int nbiorequestbasedtransfercode;

	@Column(name = "nbiobankreturncode", nullable = false)
	private int nbiobankreturncode;

	@ColumnDefault("-1")
	@Column(name = "nbiothirdpartyreturncode", nullable = false)
	private int nbiothirdpartyreturncode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nthirdpartycode", nullable = false)
	private int nthirdpartycode;

	@Column(name = "sformnumber", length = 50, nullable = false)
	private String sformnumber;

	@Column(name = "ntransfertypecode", nullable = false)
	private short ntransfertypecode;

	@Column(name = "nformtypecode", nullable = false)
	private short nformtypecode;

	@ColumnDefault("-1")
	@Column(name = "noriginsitecode", nullable = false)
	private short noriginsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nsenderusercode", nullable = false)
	private int nsenderusercode;

	@Column(name = "nsenderuserrolecode", nullable = false)
	private int nsenderuserrolecode;

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

	@ColumnDefault("-1")
	@Column(name = "nstorageconditioncode")
	private int nstorageconditioncode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ddeliverydate")
	private Instant ddeliverydate;

	@ColumnDefault("-1")
	@Column(name = "ntzdeliverydate", nullable = false)
	private short ntzdeliverydate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsetddeliverydate", nullable = false)
	private int noffsetddeliverydate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ndispatchercode")
	private int ndispatchercode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ncouriercode")
	private int ncouriercode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dreceiveddate")
	private Instant dreceiveddate;

	@ColumnDefault("-1")
	@Column(name = "nreceivingtemperaturecode", nullable = false)
	private int nreceivingtemperaturecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nreceivingofficercode", nullable = false)
	private int nreceivingofficercode = Enumeration.TransactionStatus.NA.gettransactionstatus();

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
	private transient String soriginsitename;

	@Transient
	private transient int nbioformacceptancedetailscode;

	@Transient
	private transient String ssentusername;

	@Transient
	private transient String stransferdate;

	@Transient
	private transient String sdeliverydate;

	@Transient
	private transient String stransdisplaystatus;

	@Transient
	private transient String scolorhexcode;

	@Transient
	private transient String sreceiveddate;

	@Transient
	private transient int nbioprojectcode;

	@Transient
	private transient int nbioparentsamplecode;

	@Transient
	private transient String ssamplestatus;

	@Transient
	private transient String sproductname;

	@Transient
	private transient String srepositoryid;

	@Transient
	private transient String sdispatchername;

	@Transient
	private transient String sstorageconditionname;

	@Transient
	private transient String scouriername;

	@Transient
	private transient String sreceivingtemperaturename;

	@Transient
	private transient String sreceivingofficername;

	@Transient
	private transient String ssamplecondition;

	@Transient
	private transient String stransferstatus;

	@Transient
	private transient String sremarks;

	@Transient
	private transient String scourierno;

	@Transient
	private transient String striplepackage;

	@Transient
	private transient String svalidationremarks;

	@Transient
	private transient String srecipientname;

	@Transient
	private transient String scompletionremarks;

	@Override
	public BioFormAcceptance mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioFormAcceptance objBioFormAcceptance = new BioFormAcceptance();

		objBioFormAcceptance.setNbioformacceptancecode(getInteger(arg0, "nbioformacceptancecode", arg1));
		objBioFormAcceptance.setNbiodirecttransfercode(getInteger(arg0, "nbiodirecttransfercode", arg1));
		objBioFormAcceptance.setNbiorequestbasedtransfercode(getInteger(arg0, "nbiorequestbasedtransfercode", arg1));
		objBioFormAcceptance.setNbiobankreturncode(getInteger(arg0, "nbiobankreturncode", arg1));
		objBioFormAcceptance.setNbiothirdpartyreturncode(getInteger(arg0, "nbiothirdpartyreturncode", arg1));
		objBioFormAcceptance.setNthirdpartycode(getInteger(arg0, "nthirdpartycode", arg1));
		objBioFormAcceptance.setSformnumber(getString(arg0, "sformnumber", arg1));
		objBioFormAcceptance.setNtransfertypecode(getShort(arg0, "ntransfertypecode", arg1));
		objBioFormAcceptance.setNformtypecode(getShort(arg0, "nformtypecode", arg1));
		objBioFormAcceptance.setNoriginsitecode(getShort(arg0, "noriginsitecode", arg1));
		objBioFormAcceptance.setNsenderusercode(getShort(arg0, "nsenderusercode", arg1));
		objBioFormAcceptance.setNsenderuserrolecode(getShort(arg0, "nsenderuserrolecode", arg1));
		objBioFormAcceptance.setDtransferdate(getInstant(arg0, "dtransferdate", arg1));
		objBioFormAcceptance.setNtztransferdate(getShort(arg0, "ntztransferdate", arg1));
		objBioFormAcceptance.setNoffsetdtransferdate(getInteger(arg0, "noffsetdtransferdate", arg1));
		objBioFormAcceptance.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objBioFormAcceptance.setSremarks(getString(arg0, "sremarks", arg1));
		objBioFormAcceptance.setNstorageconditioncode(getInteger(arg0, "nstorageconditioncode", arg1));
		objBioFormAcceptance.setDdeliverydate(getInstant(arg0, "ddeliverydate", arg1));
		objBioFormAcceptance.setNtzdeliverydate(getShort(arg0, "ntzdeliverydate", arg1));
		objBioFormAcceptance.setNoffsetddeliverydate(getShort(arg0, "noffsetddeliverydate", arg1));
		objBioFormAcceptance.setNdispatchercode(getInteger(arg0, "ndispatchercode", arg1));
		objBioFormAcceptance.setNcouriercode(getInteger(arg0, "ncouriercode", arg1));
		objBioFormAcceptance.setScourierno(getString(arg0, "scourierno", arg1));
		objBioFormAcceptance.setStriplepackage(getString(arg0, "striplepackage", arg1));
		objBioFormAcceptance.setSvalidationremarks(getString(arg0, "svalidationremarks", arg1));
		objBioFormAcceptance.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioFormAcceptance.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objBioFormAcceptance.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioFormAcceptance.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioFormAcceptance.setNstatus(getShort(arg0, "nstatus", arg1));
		objBioFormAcceptance.setSoriginsitename(getString(arg0, "soriginsitename", arg1));
		objBioFormAcceptance.setSstorageconditionname(getString(arg0, "sstorageconditionname", arg1));
		objBioFormAcceptance.setScouriername(getString(arg0, "scouriername", arg1));
		objBioFormAcceptance.setSrecipientname(getString(arg0, "srecipientname", arg1));
		objBioFormAcceptance.setDreceiveddate(getInstant(arg0, "dreceiveddate", arg1));
		objBioFormAcceptance.setNreceivingtemperaturecode(getInteger(arg0, "nreceivingtemperaturecode", arg1));
		objBioFormAcceptance.setNreceivingofficercode(getInteger(arg0, "nreceivingofficercode", arg1));
		objBioFormAcceptance.setScompletionremarks(getString(arg0, "scompletionremarks", arg1));
		objBioFormAcceptance.setSreceivingtemperaturename(getString(arg0, "sreceivingtemperaturename", arg1));
		objBioFormAcceptance.setSreceivingofficername(getString(arg0, "sreceivingofficername", arg1));
		objBioFormAcceptance.setNbioformacceptancedetailscode(getInteger(arg0, "nbioformacceptancedetailscode", arg1));
		objBioFormAcceptance.setSsentusername(getString(arg0, "ssentusername", arg1));
		objBioFormAcceptance.setSsamplecondition(getString(arg0, "ssamplecondition", arg1));
		objBioFormAcceptance.setStransferdate(getString(arg0, "stransferdate", arg1));
		objBioFormAcceptance.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objBioFormAcceptance.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		objBioFormAcceptance.setSdeliverydate(getString(arg0, "sdeliverydate", arg1));
		objBioFormAcceptance.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objBioFormAcceptance.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));
		objBioFormAcceptance.setSsamplestatus(getString(arg0, "ssamplestatus", arg1));
		objBioFormAcceptance.setSproductname(getString(arg0, "sproductname", arg1));
		objBioFormAcceptance.setSrepositoryid(getString(arg0, "srepositoryid", arg1));
		objBioFormAcceptance.setSdispatchername(getString(arg0, "sdispatchername", arg1));
		objBioFormAcceptance.setSreceiveddate(getString(arg0, "sreceiveddate", arg1));
		objBioFormAcceptance.setStransferstatus(getString(arg0, "stransferstatus", arg1));
		objBioFormAcceptance.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));

		return objBioFormAcceptance;
	}
}
