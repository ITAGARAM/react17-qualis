package com.agaramtech.qualis.biobank.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Maps to 'biodisposeform' table.
 */

@Entity
@Table(name = "biodisposeform")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class BioDisposeForm extends CustomizedResultsetRowMapper<BioDisposeForm>
implements Serializable, RowMapper<BioDisposeForm> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiodisposeformcode", nullable = false)
	private int nbiodisposeformcode;

	@Column(name = "sformnumber", length = 50, nullable = false)
	private String sformnumber;

	@Column(name = "ntransfertypecode", nullable = false)
	private short ntransfertypecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nformtypecode", nullable = false)
	private short nformtypecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nthirdpartycode", nullable = false)
	private int nthirdpartycode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstorageconditioncode")
	private int nstorageconditioncode;

	@ColumnDefault("-1")
	@Column(name = "ntzdeliverydate", nullable = false)
	private short ntzdeliverydate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ddeliverydate")
	private Instant ddeliverydate;

	@Column(name = "ncouriercode")
	private int ncouriercode;

	@Column(name = "ndispatchercode")
	private int ndispatchercode;

	@Column(name = "striplepackage", length = 100)
	private String striplepackage;

	@Column(name = "svalidationremarks", length = 255)
	private String svalidationremarks;

	@Column(name = "scourierno", length = 100)
	private String scourierno;

	@ColumnDefault("0")
	@Column(name = "noffsetddeliverydate", nullable = false)
	private int noffsetddeliverydate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;

	@ColumnDefault("-1")
	@Column(name = "ntztransactiondate", nullable = false)
	private int ntztransactiondate = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsetdtransactiondate", nullable = false)
	private int noffsetdtransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@Id
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient int ndisposalbatchtypecode;

	@Transient
	private transient int nbiomovetodisposecode;

	@Transient
	private transient String stransfertypename;

	@Transient
	private transient String sreceiversitename;

	@Transient
	private transient String stransdisplaystatus;

	@Transient
	private transient String scolorhexcode;

	@Transient
	private transient String sdeliverydate;

	@Transient
	private transient String sdispatchername;
	
	@Transient
	private transient String sparentsamplecode;
	
	@Transient
	private transient String srepositoryid;
	
	@Transient
	private transient String slocationcode;
	
	@Transient
	private transient String svolume;
	
	@Transient
	private transient String ssamplecondition;
	
	@Transient
	private transient String ssamplestatus;
	

	@Override
	public BioDisposeForm mapRow(ResultSet rs, int rowNum) throws SQLException {

		final var objBioDisposeForm = new BioDisposeForm();

		// Primary fields from DB table
		objBioDisposeForm.setNbiodisposeformcode(getInteger(rs, "nbiodisposeformcode", rowNum));
		objBioDisposeForm.setSformnumber(getString(rs, "sformnumber", rowNum));
		objBioDisposeForm.setNtransfertypecode(getShort(rs, "ntransfertypecode", rowNum));
		objBioDisposeForm.setNformtypecode(getShort(rs, "nformtypecode", rowNum));
		objBioDisposeForm.setNthirdpartycode(getInteger(rs, "nthirdpartycode", rowNum));
		objBioDisposeForm.setNtransactionstatus(getShort(rs, "ntransactionstatus", rowNum));
		objBioDisposeForm.setNstorageconditioncode(getInteger(rs, "nstorageconditioncode", rowNum));
		objBioDisposeForm.setNtzdeliverydate(getShort(rs, "ntzdeliverydate", rowNum));
		objBioDisposeForm.setDdeliverydate(getInstant(rs, "ddeliverydate", rowNum));
		objBioDisposeForm.setNcouriercode(getInteger(rs, "ncouriercode", rowNum));
		objBioDisposeForm.setNdispatchercode(getInteger(rs, "ndispatchercode", rowNum));
		objBioDisposeForm.setStriplepackage(getString(rs, "striplepackage", rowNum));
		objBioDisposeForm.setSvalidationremarks(getString(rs, "svalidationremarks", rowNum));
		objBioDisposeForm.setScourierno(getString(rs, "scourierno", rowNum));
		objBioDisposeForm.setNoffsetddeliverydate(getInteger(rs, "noffsetddeliverydate", rowNum));
		objBioDisposeForm.setDtransactiondate(getInstant(rs, "dtransactiondate", rowNum));
		objBioDisposeForm.setNtztransactiondate(getInteger(rs, "ntztransactiondate", rowNum));
		objBioDisposeForm.setNoffsetdtransactiondate(getInteger(rs, "noffsetdtransactiondate", rowNum));
		objBioDisposeForm.setNsitecode(getShort(rs, "nsitecode", rowNum));
		objBioDisposeForm.setNstatus(getShort(rs, "nstatus", rowNum));

		// Transient fields
		objBioDisposeForm.setNdisposalbatchtypecode(getInteger(rs, "ndisposalbatchtypecode", rowNum));
		objBioDisposeForm.setNbiomovetodisposecode(getInteger(rs, "nbiomovetodisposecode", rowNum));
		objBioDisposeForm.setStransfertypename(getString(rs, "stransfertypename", rowNum));
		objBioDisposeForm.setSreceiversitename(getString(rs, "sreceiversitename", rowNum));
		objBioDisposeForm.setStransdisplaystatus(getString(rs, "stransdisplaystatus", rowNum));
		objBioDisposeForm.setScolorhexcode(getString(rs, "scolorhexcode", rowNum));
		objBioDisposeForm.setSdeliverydate(getString(rs, "sdeliverydate", rowNum));
		objBioDisposeForm.setSdispatchername(getString(rs, "sdispatchername", rowNum));
		objBioDisposeForm.setSparentsamplecode(getString(rs, "sparentsamplecode", rowNum));
		objBioDisposeForm.setSlocationcode(getString(rs, "slocationcode", rowNum));
		objBioDisposeForm.setSrepositoryid(getString(rs, "srepositoryid", rowNum));
		objBioDisposeForm.setSvolume(getString(rs, "svolume", rowNum));
		objBioDisposeForm.setSsamplecondition(getString(rs, "ssamplecondition", rowNum));
		objBioDisposeForm.setSsamplestatus(getString(rs, "ssamplestatus", rowNum));



		return objBioDisposeForm;
	}

	//	@Override
	//	public BioDisposeForm mapRow(ResultSet rs, int rowNum) throws SQLException {
	//
	//		final var objBioDisposeForm = new BioDisposeForm();
	//
	//		objBioDisposeForm.setNbiodisposeformcode(getInteger(rs, "nbiodisposeformcode", rowNum));
	//		objBioDisposeForm.setSformnumber(getString(rs, "sformnumber", rowNum));
	//		objBioDisposeForm.setNthirdpartycode(getShort(rs, "ntransfertypecode", rowNum));
	//		objBioDisposeForm.setNformtypecode(getShort(rs, "nformtypecode", rowNum));
	//		objBioDisposeForm.setNthirdpartycode(getInteger(rs, "nthirdpartycode", rowNum));
	//		objBioDisposeForm.setNtransactionstatus(getShort(rs, "ntransactionstatus", rowNum));
	//		objBioDisposeForm.setDtransactiondate(getInstant(rs, "dtransactiondate", rowNum));
	//		objBioDisposeForm.setNtztransactiondate(getInteger(rs, "ntztransactiondate", rowNum));
	//		objBioDisposeForm.setNoffsetdtransactiondate(getInteger(rs, "noffsetdtransactiondate", rowNum));
	//		objBioDisposeForm.setNsitecode(getShort(rs, "nsitecode", rowNum));
	//		objBioDisposeForm.setNstatus(getShort(rs, "nstatus", rowNum));
	//
	//		objBioDisposeForm.setNdisposalbatchtypecode(getInteger(rs, "ndisposalbatchtypecode", rowNum));
	//		objBioDisposeForm.setNbiomovetodisposecode(getInteger(rs, "nbiomovetodisposecode", rowNum));
	//		objBioDisposeForm.setStransfertypename(getString(rs, "stransfertypename", rowNum));
	//		objBioDisposeForm.setSreceiversitename(getString(rs, "sreceiversitename", rowNum));
	//		objBioDisposeForm.setStransdisplaystatus(getString(rs, "stransdisplaystatus", rowNum));
	//		objBioDisposeForm.setScolorhexcode(getString(rs, "scolorhexcode", rowNum));
	//
	//		return objBioDisposeForm;
	//	}

}
