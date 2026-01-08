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
@Table(name = "biothirdpartyformaccept")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioThirdPartyFormAccept extends CustomizedResultsetRowMapper<BioThirdPartyFormAccept>
		implements Serializable, RowMapper<BioThirdPartyFormAccept> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiothirdpartyformacceptancecode", nullable = false)
	private int nbiothirdpartyformacceptancecode;

	@Column(name = "nbiorequestbasedtransfercode", nullable = false)
	private int nbiorequestbasedtransfercode;

	@Column(name = "sformnumber", length = 50, nullable = false)
	private String sformnumber;

	@Column(name = "ntransfertypecode", nullable = false)
	private short ntransfertypecode;

	@Column(name = "nformtypecode", nullable = false)
	private short nformtypecode;

	@Column(name = "nthirdpartycode", nullable = false)
	private int nthirdpartycode;

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
	@Column(name = "ntzreceiveddate", nullable = false)
	private short ntzreceiveddate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsetdreceiveddate", nullable = false)
	private int noffsetdreceiveddate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

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
	private transient String sformtypename;

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
	private transient short nisngs;

	@Transient
	private transient String ssenderusername;

	@Transient
	private transient String samples;

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
	public BioThirdPartyFormAccept mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioThirdPartyFormAccept objBioThirdPartyFormAcceptance = new BioThirdPartyFormAccept();

		objBioThirdPartyFormAcceptance
				.setNbiothirdpartyformacceptancecode(getInteger(arg0, "nbiothirdpartyformacceptancecode", arg1));
		objBioThirdPartyFormAcceptance
				.setNbiorequestbasedtransfercode(getInteger(arg0, "nbiorequestbasedtransfercode", arg1));
		objBioThirdPartyFormAcceptance.setSformnumber(getString(arg0, "sformnumber", arg1));
		objBioThirdPartyFormAcceptance.setNtransfertypecode(getShort(arg0, "ntransfertypecode", arg1));
		objBioThirdPartyFormAcceptance.setNformtypecode(getShort(arg0, "nformtypecode", arg1));
		objBioThirdPartyFormAcceptance.setNthirdpartycode(getInteger(arg0, "nthirdpartycode", arg1));
		objBioThirdPartyFormAcceptance.setNoriginsitecode(getShort(arg0, "noriginsitecode", arg1));
		objBioThirdPartyFormAcceptance.setNsenderusercode(getShort(arg0, "nsenderusercode", arg1));
		objBioThirdPartyFormAcceptance.setNsenderuserrolecode(getShort(arg0, "nsenderuserrolecode", arg1));
		objBioThirdPartyFormAcceptance.setDtransferdate(getInstant(arg0, "dtransferdate", arg1));
		objBioThirdPartyFormAcceptance.setNtztransferdate(getShort(arg0, "ntztransferdate", arg1));
		objBioThirdPartyFormAcceptance.setNoffsetdtransferdate(getInteger(arg0, "noffsetdtransferdate", arg1));
		objBioThirdPartyFormAcceptance.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objBioThirdPartyFormAcceptance.setSremarks(getString(arg0, "sremarks", arg1));
		objBioThirdPartyFormAcceptance.setNstorageconditioncode(getInteger(arg0, "nstorageconditioncode", arg1));
		objBioThirdPartyFormAcceptance.setDdeliverydate(getInstant(arg0, "ddeliverydate", arg1));
		objBioThirdPartyFormAcceptance.setNtzdeliverydate(getShort(arg0, "ntzdeliverydate", arg1));
		objBioThirdPartyFormAcceptance.setNoffsetddeliverydate(getShort(arg0, "noffsetddeliverydate", arg1));
		objBioThirdPartyFormAcceptance.setNdispatchercode(getInteger(arg0, "ndispatchercode", arg1));
		objBioThirdPartyFormAcceptance.setNcouriercode(getInteger(arg0, "ncouriercode", arg1));
		objBioThirdPartyFormAcceptance.setScourierno(getString(arg0, "scourierno", arg1));
		objBioThirdPartyFormAcceptance.setStriplepackage(getString(arg0, "striplepackage", arg1));
		objBioThirdPartyFormAcceptance.setSvalidationremarks(getString(arg0, "svalidationremarks", arg1));
		objBioThirdPartyFormAcceptance.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioThirdPartyFormAcceptance.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objBioThirdPartyFormAcceptance.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioThirdPartyFormAcceptance.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioThirdPartyFormAcceptance.setNstatus(getShort(arg0, "nstatus", arg1));
		objBioThirdPartyFormAcceptance.setSoriginsitename(getString(arg0, "soriginsitename", arg1));
		objBioThirdPartyFormAcceptance.setSstorageconditionname(getString(arg0, "sstorageconditionname", arg1));
		objBioThirdPartyFormAcceptance.setScouriername(getString(arg0, "scouriername", arg1));
		objBioThirdPartyFormAcceptance.setSrecipientname(getString(arg0, "srecipientname", arg1));
		objBioThirdPartyFormAcceptance.setDreceiveddate(getInstant(arg0, "dreceiveddate", arg1));
		objBioThirdPartyFormAcceptance.setNtzreceiveddate(getShort(arg0, "ntzreceiveddate", arg1));
		objBioThirdPartyFormAcceptance.setNoffsetdreceiveddate(getInteger(arg0, "noffsetdreceiveddate", arg1));
		objBioThirdPartyFormAcceptance
				.setNreceivingtemperaturecode(getInteger(arg0, "nreceivingtemperaturecode", arg1));
		objBioThirdPartyFormAcceptance.setNreceivingofficercode(getInteger(arg0, "nreceivingofficercode", arg1));
		objBioThirdPartyFormAcceptance.setScompletionremarks(getString(arg0, "scompletionremarks", arg1));
		objBioThirdPartyFormAcceptance.setSreceivingtemperaturename(getString(arg0, "sreceivingtemperaturename", arg1));
		objBioThirdPartyFormAcceptance.setSreceivingofficername(getString(arg0, "sreceivingofficername", arg1));
		objBioThirdPartyFormAcceptance.setSsentusername(getString(arg0, "ssentusername", arg1));
		objBioThirdPartyFormAcceptance.setStransferdate(getString(arg0, "stransferdate", arg1));
		objBioThirdPartyFormAcceptance.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objBioThirdPartyFormAcceptance.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		objBioThirdPartyFormAcceptance.setSdeliverydate(getString(arg0, "sdeliverydate", arg1));
		objBioThirdPartyFormAcceptance.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objBioThirdPartyFormAcceptance.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));
		objBioThirdPartyFormAcceptance.setSdispatchername(getString(arg0, "sdispatchername", arg1));
		objBioThirdPartyFormAcceptance.setSreceiveddate(getString(arg0, "sreceiveddate", arg1));
		objBioThirdPartyFormAcceptance.setSformtypename(getString(arg0, "sformtypename", arg1));
		objBioThirdPartyFormAcceptance.setNisngs(getShort(arg0, "nisngs", arg1));
		objBioThirdPartyFormAcceptance.setSsenderusername(getString(arg0, "ssenderusername", arg1));
		objBioThirdPartyFormAcceptance.setSamples(getString(arg0, "samples", arg1));
		objBioThirdPartyFormAcceptance.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));

		return objBioThirdPartyFormAcceptance;
	}
}
