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
@Table(name = "biothirdpartyreturn")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioThirdPartyReturn extends CustomizedResultsetRowMapper<BioThirdPartyReturn>
		implements Serializable, RowMapper<BioThirdPartyReturn> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiothirdpartyreturncode", nullable = false)
	private int nbiothirdpartyreturncode;

	@Column(name = "sthirdpartyreturnformnumber", length = 50)
	private String sthirdpartyreturnformnumber;

	@Column(name = "ntransfertypecode", nullable = false)
	private short ntransfertypecode;

	@Column(name = "nformtypecode", nullable = false)
	private short nformtypecode;

	@ColumnDefault("-1")
	@Column(name = "nthirdpartycode", nullable = false)
	private int nthirdpartycode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "noriginsitecode", nullable = false)
	private short noriginsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dreturndate", nullable = false)
	private Instant dreturndate;

	@ColumnDefault("-1")
	@Column(name = "ntzreturndate", nullable = false)
	private short ntzreturndate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsetdreturndate", nullable = false)
	private int noffsetdreturndate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

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
	private transient String sreturndate;

	@Transient
	private transient String sdeliverydate;

	@Transient
	private transient String stransdisplaystatus;

	@Transient
	private transient String scolorhexcode;

	@Transient
	private transient int nbioprojectcode;

	@Transient
	private transient int nbioparentsamplecode;

	@Transient
	private transient int nstoragetypecode;

	@Transient
	private transient int nbiothirdpartyformacceptancecode;

	@Transient
	private transient String sdispatchername;

	@Transient
	private transient int nbiothirdpartyformacceptancedetailscode;

	@Transient
	private transient String sstorageconditionname;

	@Transient
	private transient String scouriername;

	@Transient
	private transient String sparentsamplecode;

	@Transient
	private transient int ncohortno;

	@Transient
	private transient String srepositoryid;

	@Transient
	private transient int nproductcode;

	@Transient
	private transient String svolume;

	@Transient
	private transient String sreturnvolume;

	@Transient
	private transient short nsamplecondition;

	@Transient
	private transient short nsamplestatus;

	@Transient
	private transient String sremarks;

	@Transient
	private transient String scourierno;

	@Transient
	private transient String striplepackage;

	@Transient
	private transient String svalidationremarks;

	@Override
	public BioThirdPartyReturn mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioThirdPartyReturn objBioThirdPartyReturn = new BioThirdPartyReturn();

		objBioThirdPartyReturn.setNbiothirdpartyreturncode(getInteger(arg0, "nbiothirdpartyreturncode", arg1));
		objBioThirdPartyReturn
				.setNbiothirdpartyformacceptancecode(getInteger(arg0, "nbiothirdpartyformacceptancecode", arg1));
		objBioThirdPartyReturn.setSthirdpartyreturnformnumber(getString(arg0, "sthirdpartyreturnformnumber", arg1));
		objBioThirdPartyReturn.setNtransfertypecode(getShort(arg0, "ntransfertypecode", arg1));
		objBioThirdPartyReturn.setNformtypecode(getShort(arg0, "nformtypecode", arg1));
		objBioThirdPartyReturn.setNthirdpartycode(getInteger(arg0, "nthirdpartycode", arg1));
		objBioThirdPartyReturn.setNoriginsitecode(getShort(arg0, "noriginsitecode", arg1));
		objBioThirdPartyReturn.setDreturndate(getInstant(arg0, "dreturndate", arg1));
		objBioThirdPartyReturn.setNtzreturndate(getShort(arg0, "ntzreturndate", arg1));
		objBioThirdPartyReturn.setNoffsetdreturndate(getInteger(arg0, "noffsetdreturndate", arg1));
		objBioThirdPartyReturn.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objBioThirdPartyReturn.setSremarks(getString(arg0, "sremarks", arg1));
		objBioThirdPartyReturn.setNstorageconditioncode(getInteger(arg0, "nstorageconditioncode", arg1));
		objBioThirdPartyReturn.setDdeliverydate(getInstant(arg0, "ddeliverydate", arg1));
		objBioThirdPartyReturn.setNtzdeliverydate(getShort(arg0, "ntzdeliverydate", arg1));
		objBioThirdPartyReturn.setNoffsetddeliverydate(getShort(arg0, "noffsetddeliverydate", arg1));
		objBioThirdPartyReturn.setNdispatchercode(getInteger(arg0, "ndispatchercode", arg1));
		objBioThirdPartyReturn.setNcouriercode(getInteger(arg0, "ncouriercode", arg1));
		objBioThirdPartyReturn.setScourierno(getString(arg0, "scourierno", arg1));
		objBioThirdPartyReturn.setStriplepackage(getString(arg0, "striplepackage", arg1));
		objBioThirdPartyReturn.setSvalidationremarks(getString(arg0, "svalidationremarks", arg1));
		objBioThirdPartyReturn.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioThirdPartyReturn.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objBioThirdPartyReturn.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioThirdPartyReturn.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioThirdPartyReturn.setNstatus(getShort(arg0, "nstatus", arg1));
		objBioThirdPartyReturn.setSoriginsitename(getString(arg0, "soriginsitename", arg1));
		objBioThirdPartyReturn.setNbiothirdpartyformacceptancedetailscode(
				getInteger(arg0, "nbiothirdpartyformacceptancedetailscode", arg1));
		objBioThirdPartyReturn.setSstorageconditionname(getString(arg0, "sstorageconditionname", arg1));
		objBioThirdPartyReturn.setScouriername(getString(arg0, "scouriername", arg1));
		objBioThirdPartyReturn.setSreturndate(getString(arg0, "sreturndate", arg1));
		objBioThirdPartyReturn.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objBioThirdPartyReturn.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		objBioThirdPartyReturn.setSdeliverydate(getString(arg0, "sdeliverydate", arg1));
		objBioThirdPartyReturn.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objBioThirdPartyReturn.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));
		objBioThirdPartyReturn.setNstoragetypecode(getInteger(arg0, "nstoragetypecode", arg1));
		objBioThirdPartyReturn.setSdispatchername(getString(arg0, "sdispatchername", arg1));
		objBioThirdPartyReturn.setSparentsamplecode(getString(arg0, "sparentsamplecode", arg1));
		objBioThirdPartyReturn.setNcohortno(getInteger(arg0, "ncohortno", arg1));
		objBioThirdPartyReturn.setSrepositoryid(getString(arg0, "srepositoryid", arg1));
		objBioThirdPartyReturn.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objBioThirdPartyReturn.setSvolume(getString(arg0, "svolume", arg1));
		objBioThirdPartyReturn.setSreturnvolume(getString(arg0, "sreturnvolume", arg1));
		objBioThirdPartyReturn.setNsamplecondition(getShort(arg0, "nsamplecondition", arg1));
		objBioThirdPartyReturn.setNsamplestatus(getShort(arg0, "nsamplestatus", arg1));
		objBioThirdPartyReturn.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));

		return objBioThirdPartyReturn;
	}
}
