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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bioformacceptancehistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioFormAcceptanceHistory extends CustomizedResultsetRowMapper<BioFormAcceptanceHistory>
		implements Serializable, RowMapper<BioFormAcceptanceHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbioformacceptancehistorycode", nullable = false)
	private int nbioformacceptancehistorycode;

	@Column(name = "nbioformacceptancecode", nullable = false)
	private int nbioformacceptancecode;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;

	@ColumnDefault("-1")
	@Column(name = "ntztransactiondate", nullable = false)
	private short ntztransactiondate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

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
	public BioFormAcceptanceHistory mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioFormAcceptanceHistory objBioFormAcceptanceHistory = new BioFormAcceptanceHistory();

		objBioFormAcceptanceHistory.setNbioformacceptancecode(getInteger(arg0, "nbioformacceptancecode", arg1));
		objBioFormAcceptanceHistory.setNbioformacceptancecode(getInteger(arg0, "nbioformacceptancecode", arg1));
		objBioFormAcceptanceHistory.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objBioFormAcceptanceHistory.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioFormAcceptanceHistory.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objBioFormAcceptanceHistory.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioFormAcceptanceHistory.setNusercode(getShort(arg0, "nusercode", arg1));
		objBioFormAcceptanceHistory.setNuserrolecode(getShort(arg0, "nuserrolecode", arg1));
		objBioFormAcceptanceHistory.setNdeputyusercode(getShort(arg0, "ndeputyusercode", arg1));
		objBioFormAcceptanceHistory.setNdeputyuserrolecode(getShort(arg0, "ndeputyuserrolecode", arg1));
		objBioFormAcceptanceHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioFormAcceptanceHistory.setNstatus(getShort(arg0, "nstatus", arg1));

		return objBioFormAcceptanceHistory;
	}
}
