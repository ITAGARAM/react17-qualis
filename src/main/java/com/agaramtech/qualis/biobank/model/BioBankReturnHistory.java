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
@Table(name = "biobankreturnhistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioBankReturnHistory extends CustomizedResultsetRowMapper<BioBankReturnHistory>
		implements Serializable, RowMapper<BioBankReturnHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiobankreturnhistorycode", nullable = false)
	private int nbiobankreturnhistorycode;

	@Column(name = "nbiobankreturncode", nullable = false)
	private int nbiobankreturncode;

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
	public BioBankReturnHistory mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioBankReturnHistory objBioBankReturnHistory = new BioBankReturnHistory();

		objBioBankReturnHistory.setNbiobankreturnhistorycode(getInteger(arg0, "nbiobankreturnhistorycode", arg1));
		objBioBankReturnHistory.setNbiobankreturncode(getInteger(arg0, "nbiobankreturncode", arg1));
		objBioBankReturnHistory.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objBioBankReturnHistory.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioBankReturnHistory.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objBioBankReturnHistory.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioBankReturnHistory.setNusercode(getShort(arg0, "nusercode", arg1));
		objBioBankReturnHistory.setNuserrolecode(getShort(arg0, "nuserrolecode", arg1));
		objBioBankReturnHistory.setNdeputyusercode(getShort(arg0, "ndeputyusercode", arg1));
		objBioBankReturnHistory.setNdeputyuserrolecode(getShort(arg0, "ndeputyuserrolecode", arg1));
		objBioBankReturnHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioBankReturnHistory.setNstatus(getShort(arg0, "nstatus", arg1));

		return objBioBankReturnHistory;
	}
}
