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
@Table(name = "biobankreturndetailshistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioBankReturnDetailsHistory extends CustomizedResultsetRowMapper<BioBankReturnDetailsHistory>
		implements Serializable, RowMapper<BioBankReturnDetailsHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiobankreturndetailshistorycode", nullable = false)
	private int nbiobankreturndetailshistorycode;

	@Column(name = "nbiobankreturndetailscode", nullable = false)
	private int nbiobankreturndetailscode;

	@Column(name = "nbiobankreturncode", nullable = false)
	private int nbiobankreturncode;

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
	public BioBankReturnDetailsHistory mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioBankReturnDetailsHistory objBioBankReturnDetailsHistory = new BioBankReturnDetailsHistory();

		objBioBankReturnDetailsHistory
				.setNbiobankreturndetailshistorycode(getInteger(arg0, "nbiobankreturndetailshistorycode", arg1));
		objBioBankReturnDetailsHistory
				.setNbiobankreturndetailscode(getInteger(arg0, "nbiobankreturndetailscode", arg1));
		objBioBankReturnDetailsHistory.setNbiobankreturncode(getInteger(arg0, "nbiobankreturncode", arg1));
		objBioBankReturnDetailsHistory.setNsamplecondition(getShort(arg0, "nsamplecondition", arg1));
		objBioBankReturnDetailsHistory.setNsamplestatus(getShort(arg0, "nsamplestatus", arg1));
		objBioBankReturnDetailsHistory.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioBankReturnDetailsHistory.setNtztransactiondate(getInteger(arg0, "ntztransactiondate", arg1));
		objBioBankReturnDetailsHistory.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioBankReturnDetailsHistory.setNusercode(getShort(arg0, "nusercode", arg1));
		objBioBankReturnDetailsHistory.setNuserrolecode(getShort(arg0, "nuserrolecode", arg1));
		objBioBankReturnDetailsHistory.setNdeputyusercode(getShort(arg0, "ndeputyusercode", arg1));
		objBioBankReturnDetailsHistory.setNdeputyuserrolecode(getShort(arg0, "ndeputyuserrolecode", arg1));
		objBioBankReturnDetailsHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioBankReturnDetailsHistory.setNstatus(getShort(arg0, "nstatus", arg1));

		return objBioBankReturnDetailsHistory;
	}
}
