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

@Entity
@Table(name = "bioformacceptdetailshistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioFormAcceptDetailsHistory extends
		CustomizedResultsetRowMapper<BioFormAcceptDetailsHistory>
		implements Serializable, RowMapper<BioFormAcceptDetailsHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbioformacceptdetailshistorycode", nullable = false)
	private int nbioformacceptdetailshistorycode;

	@Column(name = "nbioformacceptancedetailscode", nullable = false)
	private int nbioformacceptancedetailscode;

	@Column(name = "nbioformacceptancecode", nullable = false)
	private int nbioformacceptancecode;

	@Column(name = "nsamplecondition", nullable = false)
	private short nsamplecondition;

	@Column(name = "nsamplestatus", nullable = false)
	private short nsamplestatus;

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;

	@ColumnDefault("-1")
	@Column(name = "ntztransactiondate", nullable = false)
	private int ntztransactiondate = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsetdtransactiondate", nullable = false)
	private int noffsetdtransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nusercode", nullable = false)
	private int nusercode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ndeputyusercode", nullable = false)
	private int ndeputyusercode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ndeputyuserrolecode", nullable = false)
	private int ndeputyuserrolecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public BioFormAcceptDetailsHistory mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioFormAcceptDetailsHistory objBioFormAcceptDetailsHistory = new BioFormAcceptDetailsHistory();

		objBioFormAcceptDetailsHistory
				.setNbioformacceptdetailshistorycode(getInteger(arg0, "nbioformacceptdetailshistorycode", arg1));
		objBioFormAcceptDetailsHistory
				.setNbioformacceptancedetailscode(getInteger(arg0, "nbioformacceptancedetailscode", arg1));
		objBioFormAcceptDetailsHistory.setNbioformacceptancecode(getInteger(arg0, "nbioformacceptancecode", arg1));
		objBioFormAcceptDetailsHistory.setNbioformacceptancecode(getInteger(arg0, "nbioformacceptancecode", arg1));
		objBioFormAcceptDetailsHistory.setNbioformacceptancecode(getInteger(arg0, "nbioformacceptancecode", arg1));
		objBioFormAcceptDetailsHistory.setNsamplecondition(getShort(arg0, "nsamplecondition", arg1));
		objBioFormAcceptDetailsHistory.setNsamplestatus(getShort(arg0, "nsamplestatus", arg1));
		objBioFormAcceptDetailsHistory.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioFormAcceptDetailsHistory.setNtztransactiondate(getInteger(arg0, "ntztransactiondate", arg1));
		objBioFormAcceptDetailsHistory.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioFormAcceptDetailsHistory.setNusercode(getShort(arg0, "nusercode", arg1));
		objBioFormAcceptDetailsHistory.setNuserrolecode(getShort(arg0, "nuserrolecode", arg1));
		objBioFormAcceptDetailsHistory.setNdeputyusercode(getShort(arg0, "ndeputyusercode", arg1));
		objBioFormAcceptDetailsHistory.setNdeputyuserrolecode(getShort(arg0, "ndeputyuserrolecode", arg1));
		objBioFormAcceptDetailsHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioFormAcceptDetailsHistory.setNstatus(getShort(arg0, "nstatus", arg1));

		return objBioFormAcceptDetailsHistory;
	}
}
