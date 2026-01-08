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
 * This class is used to map the fields of 'biosamplereceiving' table of the
 * Database.
 */

@Entity
@Table(name = "biosamplereceiving")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioSampleReceiving extends CustomizedResultsetRowMapper<BioSampleReceiving>
		implements Serializable, RowMapper<BioSampleReceiving> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiosamplereceivingcode", nullable = false)
	private int nbiosamplereceivingcode;

	@Column(name = "nbioparentsamplecollectioncode", nullable = false)
	private int nbioparentsamplecollectioncode;

	@Column(name = "nbioparentsamplecode", nullable = false)
	private int nbioparentsamplecode;

	@ColumnDefault("-1")
	@Column(name = "nproductcode", nullable = false)
	private int nproductcode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "ndiagnostictypecode", nullable = false)
	private short ndiagnostictypecode = (short) Enumeration.DiagnosticType.NOT_IDENTIFIED.getndiagnostictypecode();

	@ColumnDefault("-1")
	@Column(name = "ncontainertypecode", nullable = false)
	private short ncontainertypecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstoragetypecode", nullable = false)
	private short nstoragetypecode = (short) Enumeration.StorageType.HUB.getnstoragetypecode();

	@Column(name = "srepositoryid", length = 100)
	private String srepositoryid;

	@Column(name = "slocationcode", length = 100)
	private String slocationcode;

	@Column(name = "sqty", length = 10, nullable = false)
	private String sqty;
	
	// added by sujatha ATE_274 for added 6 field in the table BGSI-218
	//start
	@Column(name = "sextractedsampleid", length = 50)
	private String sextractedsampleid;
	
	@Column(name = "sconcentration", length = 100)
	private String sconcentration;
	
	@Column(name = "sqcplatform", length = 500)
	private String sqcplatform;
	
	@Column(name = "seluent", length = 500)
	private String seluent;
	
	@Column(name = "srefformnumber", length = 50)
	private String srefformnumber;
	
	@Column(name = "sreferencerepoid", length = 100)
	private String sreferencerepoid;
	//end

	@Column(name = "dreceiveddate")
	private Instant dreceiveddate;

	@ColumnDefault("-1")
	@Column(name = "ntzreceiveddate", nullable = false)
	private short ntzreceiveddate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsetreceiveddate", nullable = false)
	private int noffsetreceiveddate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;

	@ColumnDefault("-1")
	@Column(name = "ntztransactiondate", nullable = false)
	private short ntztransactiondate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsettransactiondate", nullable = false)
	private int noffsettransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("114")
	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus = (short) Enumeration.TransactionStatus.ALIQUOTED.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	// Transient fields for display or extended info
	@Transient
	private transient String sproductname;

	@Transient
	private transient String sdiagnostictypename;

	@Transient
	private transient String scontainertype;

	@Transient
	private transient String sstoragetypename;

	@Transient
	private transient String sreceiveddate;

	@Transient
	private transient String stransactiondate;

	@Transient
	private transient String stzreceiveddate;

	@Transient
	private transient String stztransactiondate;

	@Transient
	private transient String stransactionstatus;

	@Transient
	private transient String sbiosampledisplay;

	@Transient
	private transient String sproductcatname;

	@Transient
	private transient String sparentsamplecode;

	@Transient
	private transient int nsamplestoragetransactioncode;
	
	@Transient
    private transient int nproductcatcode;
	
	@Transient
	private transient String svolume;
	
	@Transient
	private transient String ssubjectid;
	
	@Transient
	private transient String scasetype;
    
	@Override
	public BioSampleReceiving mapRow(final ResultSet arg0, final int arg1) throws SQLException {
		final var obj = new BioSampleReceiving();

		obj.setNbiosamplereceivingcode(getInteger(arg0, "nbiosamplereceivingcode", arg1));
		obj.setNbioparentsamplecollectioncode(getInteger(arg0, "nbioparentsamplecollectioncode", arg1));
		obj.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));
		obj.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		obj.setNdiagnostictypecode(getShort(arg0, "ndiagnostictypecode", arg1));
		obj.setNcontainertypecode(getShort(arg0, "ncontainertypecode", arg1));
		obj.setNstoragetypecode(getShort(arg0, "nstoragetypecode", arg1));
		obj.setSrepositoryid(getString(arg0, "srepositoryid", arg1));
		obj.setSlocationcode(getString(arg0, "slocationcode", arg1));
		obj.setSqty(getString(arg0, "sqty", arg1));
		obj.setDreceiveddate(getInstant(arg0, "dreceiveddate", arg1));
		obj.setNtzreceiveddate(getShort(arg0, "ntzreceiveddate", arg1));
		obj.setNoffsetreceiveddate(getInteger(arg0, "noffsetreceiveddate", arg1));
		obj.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		obj.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		obj.setNoffsettransactiondate(getInteger(arg0, "noffsettransactiondate", arg1));
		obj.setNsitecode(getShort(arg0, "nsitecode", arg1));
		obj.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		obj.setNstatus(getShort(arg0, "nstatus", arg1));
		obj.setSproductname(getString(arg0, "sproductname", arg1));
		obj.setSdiagnostictypename(getString(arg0, "sdiagnostictypename", arg1));
		obj.setScontainertype(getString(arg0, "scontainertype", arg1));
		obj.setSstoragetypename(getString(arg0, "sstoragetypename", arg1));
		obj.setSreceiveddate(getString(arg0, "sreceiveddate", arg1));
		obj.setStransactiondate(getString(arg0, "stransactiondate", arg1));
		obj.setStzreceiveddate(getString(arg0, "stzreceiveddate", arg1));
		obj.setStztransactiondate(getString(arg0, "stztransactiondate", arg1));
		obj.setStransactionstatus(getString(arg0, "stransactionstatus", arg1));
		obj.setSbiosampledisplay(getString(arg0, "sbiosampledisplay", arg1));
		obj.setSproductcatname(getString(arg0, "sproductcatname", arg1));
		obj.setSparentsamplecode(getString(arg0, "sparentsamplecode", arg1));
		obj.setNsamplestoragetransactioncode(getInteger(arg0, "nsamplestoragetransactioncode", arg1));
		obj.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		obj.setSvolume(getString(arg0, "svolume", arg1));
		obj.setSsubjectid(getString(arg0, "ssubjectid", arg1));
		obj.setScasetype(getString(arg0, "scasetype", arg1));
		// added by sujatha ATE_274 for added new 6 fields in table BGSI-218
		obj.setSextractedsampleid(getString(arg0, "sextractedsampleid", arg1));
		obj.setSconcentration(getString(arg0, "sconcentration", arg1));
		obj.setSqcplatform(getString(arg0, "sqcplatform", arg1));
		obj.setSeluent(getString(arg0, "seluent", arg1));
		obj.setSrefformnumber(getString(arg0, "srefformnumber", arg1));
		obj.setSreferencerepoid(getString(arg0, "sreferencerepoid", arg1));
		return obj;
	}
}
