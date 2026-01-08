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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "biomovetodisposedetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioMoveToDisposeDetails extends CustomizedResultsetRowMapper<BioMoveToDisposeDetails>
		implements Serializable, RowMapper<BioMoveToDisposeDetails> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiomovetodisposedetailscode", nullable = false)
	private int nbiomovetodisposedetailscode;

	@Column(name = "nbiomovetodisposecode", nullable = false)
	private int nbiomovetodisposecode;

	@Column(name = "nbioprojectcode", nullable = false)
	private int nbioprojectcode;

	@Column(name = "nbioparentsamplecode", nullable = false)
	private int nbioparentsamplecode;

	@Column(name = "nsamplestoragetransactioncode", nullable = false)
	private int nsamplestoragetransactioncode;

	@Column(name = "svolume", length = 10)
	private String svolume;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

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

	@ColumnDefault("-1")
	@Column(name = "ndiagnostictypecode", nullable = false)
	private int ndiagnostictypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ncontainertypecode", nullable = false)
	private int ncontainertypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nsamplestatus", nullable = false)
	private short nsamplestatus;

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

	@Override
	public BioMoveToDisposeDetails mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioMoveToDisposeDetails objBioMoveToDisposeDetails = new BioMoveToDisposeDetails();

		objBioMoveToDisposeDetails
				.setNbiomovetodisposedetailscode(getInteger(arg0, "nbiomovetodisposedetailscode", arg1));
		objBioMoveToDisposeDetails.setNbiomovetodisposecode(getInteger(arg0, "nbiomovetodisposecode", arg1));
		objBioMoveToDisposeDetails.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objBioMoveToDisposeDetails.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));
		objBioMoveToDisposeDetails
				.setNsamplestoragetransactioncode(getInteger(arg0, "nsamplestoragetransactioncode", arg1));
		objBioMoveToDisposeDetails.setSvolume(getString(arg0, "svolume", arg1));
		objBioMoveToDisposeDetails.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objBioMoveToDisposeDetails.setNcohortno(getShort(arg0, "ncohortno", arg1));
		objBioMoveToDisposeDetails.setNstoragetypecode(getInteger(arg0, "nstoragetypecode", arg1));
		objBioMoveToDisposeDetails.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		objBioMoveToDisposeDetails.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objBioMoveToDisposeDetails.setNsamplecondition(getShort(arg0, "nsamplecondition", arg1));
		objBioMoveToDisposeDetails.setNdiagnostictypecode(getInteger(arg0, "ndiagnostictypecode", arg1));
		objBioMoveToDisposeDetails.setNcontainertypecode(getShort(arg0, "ncontainertypecode", arg1));
		objBioMoveToDisposeDetails.setNsamplestatus(getShort(arg0, "nsamplestatus", arg1));
		objBioMoveToDisposeDetails.setNreasoncode(getShort(arg0, "nreasoncode", arg1));
		objBioMoveToDisposeDetails.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioMoveToDisposeDetails.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objBioMoveToDisposeDetails.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioMoveToDisposeDetails.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioMoveToDisposeDetails.setNstatus(getShort(arg0, "nstatus", arg1));

		return objBioMoveToDisposeDetails;

	}
}
