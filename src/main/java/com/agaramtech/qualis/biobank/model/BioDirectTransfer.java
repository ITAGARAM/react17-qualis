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
@Table(name = "biodirecttransfer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioDirectTransfer extends CustomizedResultsetRowMapper<BioDirectTransfer>
		implements Serializable, RowMapper<BioDirectTransfer> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiodirecttransfercode", nullable = false)
	private int nbiodirecttransfercode;

	@Column(name = "sformnumber", length = 50, nullable = false)
	private String sformnumber;

	@Column(name = "ntransfertypecode", nullable = false)
	private short ntransfertypecode;

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
	private transient String sremarks;

	@Transient
	private transient String scourierno;

	@Transient
	private transient String striplepackage;

	@Transient
	private transient String svalidationremarks;

	@Transient
	private transient String stransfertypename;

	@Transient
	private transient String sreceiversitename;

	@Transient
	private transient String stransferdate;

	@Transient
	private transient String sdeliverydate;

	@Transient
	private transient String stransdisplaystatus;

	@Transient
	private transient String scolorhexcode;

	@Transient
	private transient int nbiobanksitecode;

	@Transient
	private transient int nbioprojectcode;

	@Transient
	private transient int nbioparentsamplecode;

	@Transient
	private transient int nstoragetypecode;

	@Transient
	private transient int nproductcode;

	@Transient
	private transient String sstoragetypename;

	@Transient
	private transient String sproductname;

	@Transient
	private transient String srepositoryid;

	@Transient
	private transient String sdispatchername;

	@Transient
	private transient String stransferstatus;

	@Transient
	private transient String sparentsamplecode;

	@Transient
	private transient short ncohortno;

	@Transient
	private transient String sstorageconditionname;

	@Transient
	private transient String scouriername;

	@Override
	public BioDirectTransfer mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioDirectTransfer objBioDirectTransfer = new BioDirectTransfer();

		objBioDirectTransfer.setNbiodirecttransfercode(getInteger(arg0, "nbiodirecttransfercode", arg1));
		objBioDirectTransfer.setSformnumber(getString(arg0, "sformnumber", arg1));
		objBioDirectTransfer.setNtransfertypecode(getShort(arg0, "ntransfertypecode", arg1));
		objBioDirectTransfer.setNreceiversitecode(getShort(arg0, "nreceiversitecode", arg1));
		objBioDirectTransfer.setDtransferdate(getInstant(arg0, "dtransferdate", arg1));
		objBioDirectTransfer.setNtztransferdate(getShort(arg0, "ntztransferdate", arg1));
		objBioDirectTransfer.setNoffsetdtransferdate(getInteger(arg0, "noffsetdtransferdate", arg1));
		objBioDirectTransfer.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objBioDirectTransfer.setSremarks(getString(arg0, "sremarks", arg1));
		objBioDirectTransfer.setNstorageconditioncode(getInteger(arg0, "nstorageconditioncode", arg1));
		objBioDirectTransfer.setDdeliverydate(getInstant(arg0, "ddeliverydate", arg1));
		objBioDirectTransfer.setNtzdeliverydate(getShort(arg0, "ntzdeliverydate", arg1));
		objBioDirectTransfer.setNoffsetddeliverydate(getShort(arg0, "noffsetddeliverydate", arg1));
		objBioDirectTransfer.setNdispatchercode(getInteger(arg0, "ndispatchercode", arg1));
		objBioDirectTransfer.setNcouriercode(getInteger(arg0, "ncouriercode", arg1));
		objBioDirectTransfer.setScourierno(getString(arg0, "scourierno", arg1));
		objBioDirectTransfer.setStriplepackage(getString(arg0, "striplepackage", arg1));
		objBioDirectTransfer.setSvalidationremarks(getString(arg0, "svalidationremarks", arg1));
		objBioDirectTransfer.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objBioDirectTransfer.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioDirectTransfer.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objBioDirectTransfer.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioDirectTransfer.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioDirectTransfer.setNstatus(getShort(arg0, "nstatus", arg1));
		objBioDirectTransfer.setStransfertypename(getString(arg0, "stransfertypename", arg1));
		objBioDirectTransfer.setSreceiversitename(getString(arg0, "sreceiversitename", arg1));
		objBioDirectTransfer.setStransferdate(getString(arg0, "stransferdate", arg1));
		objBioDirectTransfer.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objBioDirectTransfer.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		objBioDirectTransfer.setSdeliverydate(getString(arg0, "sdeliverydate", arg1));
		objBioDirectTransfer.setNbiobanksitecode(getInteger(arg0, "nbiobanksitecode", arg1));
		objBioDirectTransfer.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objBioDirectTransfer.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));
		objBioDirectTransfer.setNstoragetypecode(getInteger(arg0, "nstoragetypecode", arg1));
		objBioDirectTransfer.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objBioDirectTransfer.setSstoragetypename(getString(arg0, "sstoragetypename", arg1));
		objBioDirectTransfer.setSproductname(getString(arg0, "sproductname", arg1));
		objBioDirectTransfer.setSrepositoryid(getString(arg0, "srepositoryid", arg1));
		objBioDirectTransfer.setSdispatchername(getString(arg0, "sdispatchername", arg1));
		objBioDirectTransfer.setStransferstatus(getString(arg0, "stransferstatus", arg1));
		objBioDirectTransfer.setSparentsamplecode(getString(arg0, "sparentsamplecode", arg1));
		objBioDirectTransfer.setNcohortno(getShort(arg0, "ncohortno", arg1));
		objBioDirectTransfer.setSstorageconditionname(getString(arg0, "sstorageconditionname", arg1));
		objBioDirectTransfer.setScouriername(getString(arg0, "scouriername", arg1));

		return objBioDirectTransfer;

	}
}
