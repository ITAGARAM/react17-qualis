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
@Table(name = "biobankreturn")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioBankReturn extends CustomizedResultsetRowMapper<BioBankReturn>
		implements Serializable, RowMapper<BioBankReturn> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiobankreturncode", nullable = false)
	private int nbiobankreturncode;

	@Column(name = "sbankreturnformnumber", length = 50)
	private String sbankreturnformnumber;

	@Column(name = "ntransfertypecode", nullable = false)
	private short ntransfertypecode;

	@Column(name = "nformtypecode", nullable = false)
	private short nformtypecode;

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
	private transient int nbioformacceptancecode;

	@Transient
	private transient String sdispatchername;

	@Transient
	private transient int nbioformacceptancedetailscode;

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

	@Transient
	private transient String stransfertypename;

	@Override
	public BioBankReturn mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioBankReturn objBioBankReturn = new BioBankReturn();

		objBioBankReturn.setNbiobankreturncode(getInteger(arg0, "nbiobankreturncode", arg1));
		objBioBankReturn.setNbioformacceptancecode(getInteger(arg0, "nbioformacceptancecode", arg1));
		objBioBankReturn.setSbankreturnformnumber(getString(arg0, "sbankreturnformnumber", arg1));
		objBioBankReturn.setNtransfertypecode(getShort(arg0, "ntransfertypecode", arg1));
		objBioBankReturn.setNformtypecode(getShort(arg0, "nformtypecode", arg1));
		objBioBankReturn.setNoriginsitecode(getShort(arg0, "noriginsitecode", arg1));
		objBioBankReturn.setDreturndate(getInstant(arg0, "dreturndate", arg1));
		objBioBankReturn.setNtzreturndate(getShort(arg0, "ntzreturndate", arg1));
		objBioBankReturn.setNoffsetdreturndate(getInteger(arg0, "noffsetdreturndate", arg1));
		objBioBankReturn.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objBioBankReturn.setSremarks(getString(arg0, "sremarks", arg1));
		objBioBankReturn.setNstorageconditioncode(getInteger(arg0, "nstorageconditioncode", arg1));
		objBioBankReturn.setDdeliverydate(getInstant(arg0, "ddeliverydate", arg1));
		objBioBankReturn.setNtzdeliverydate(getShort(arg0, "ntzdeliverydate", arg1));
		objBioBankReturn.setNoffsetddeliverydate(getShort(arg0, "noffsetddeliverydate", arg1));
		objBioBankReturn.setNdispatchercode(getInteger(arg0, "ndispatchercode", arg1));
		objBioBankReturn.setNcouriercode(getInteger(arg0, "ncouriercode", arg1));
		objBioBankReturn.setScourierno(getString(arg0, "scourierno", arg1));
		objBioBankReturn.setStriplepackage(getString(arg0, "striplepackage", arg1));
		objBioBankReturn.setSvalidationremarks(getString(arg0, "svalidationremarks", arg1));
		objBioBankReturn.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioBankReturn.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objBioBankReturn.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioBankReturn.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioBankReturn.setNstatus(getShort(arg0, "nstatus", arg1));
		objBioBankReturn.setSoriginsitename(getString(arg0, "soriginsitename", arg1));
		objBioBankReturn.setNbioformacceptancedetailscode(getInteger(arg0, "nbioformacceptancedetailscode", arg1));
		objBioBankReturn.setSstorageconditionname(getString(arg0, "sstorageconditionname", arg1));
		objBioBankReturn.setScouriername(getString(arg0, "scouriername", arg1));
		objBioBankReturn.setSreturndate(getString(arg0, "sreturndate", arg1));
		objBioBankReturn.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objBioBankReturn.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		objBioBankReturn.setSdeliverydate(getString(arg0, "sdeliverydate", arg1));
		objBioBankReturn.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objBioBankReturn.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));
		objBioBankReturn.setNstoragetypecode(getInteger(arg0, "nstoragetypecode", arg1));
		objBioBankReturn.setSdispatchername(getString(arg0, "sdispatchername", arg1));
		objBioBankReturn.setSparentsamplecode(getString(arg0, "sparentsamplecode", arg1));
		objBioBankReturn.setNcohortno(getInteger(arg0, "ncohortno", arg1));
		objBioBankReturn.setSrepositoryid(getString(arg0, "srepositoryid", arg1));
		objBioBankReturn.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objBioBankReturn.setSvolume(getString(arg0, "svolume", arg1));
		objBioBankReturn.setSreturnvolume(getString(arg0, "sreturnvolume", arg1));
		objBioBankReturn.setNsamplecondition(getShort(arg0, "nsamplecondition", arg1));
		objBioBankReturn.setNsamplestatus(getShort(arg0, "nsamplestatus", arg1));
		objBioBankReturn.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objBioBankReturn.setStransfertypename(getString(arg0, "stransfertypename", arg1));

		return objBioBankReturn;
	}
}
