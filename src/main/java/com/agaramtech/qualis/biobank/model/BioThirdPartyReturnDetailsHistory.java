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
@Table(name = "biothirdpartyreturndetailshistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioThirdPartyReturnDetailsHistory extends CustomizedResultsetRowMapper<BioThirdPartyReturnDetailsHistory>
		implements Serializable, RowMapper<BioThirdPartyReturnDetailsHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiothirdpartyreturndetailshistorycode", nullable = false)
	private int nbiothirdpartyreturndetailshistorycode;

	@Column(name = "nbiothirdpartyreturndetailscode", nullable = false)
	private int nbiothirdpartyreturndetailscode;

	@Column(name = "nbiothirdpartyreturncode", nullable = false)
	private int nbiothirdpartyreturncode;

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
	public BioThirdPartyReturnDetailsHistory mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioThirdPartyReturnDetailsHistory objBioThirdPartyReturnDetailsHistory = new BioThirdPartyReturnDetailsHistory();

		objBioThirdPartyReturnDetailsHistory
				.setNbiothirdpartyreturndetailshistorycode(getInteger(arg0, "nbiothirdpartyreturndetailshistorycode", arg1));
		objBioThirdPartyReturnDetailsHistory
				.setNbiothirdpartyreturndetailscode(getInteger(arg0, "nbiothirdpartyreturndetailscode", arg1));
		objBioThirdPartyReturnDetailsHistory.setNbiothirdpartyreturncode(getInteger(arg0, "nbiothirdpartyreturncode", arg1));
		objBioThirdPartyReturnDetailsHistory.setNsamplecondition(getShort(arg0, "nsamplecondition", arg1));
		objBioThirdPartyReturnDetailsHistory.setNsamplestatus(getShort(arg0, "nsamplestatus", arg1));
		objBioThirdPartyReturnDetailsHistory.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioThirdPartyReturnDetailsHistory.setNtztransactiondate(getInteger(arg0, "ntztransactiondate", arg1));
		objBioThirdPartyReturnDetailsHistory.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioThirdPartyReturnDetailsHistory.setNusercode(getShort(arg0, "nusercode", arg1));
		objBioThirdPartyReturnDetailsHistory.setNuserrolecode(getShort(arg0, "nuserrolecode", arg1));
		objBioThirdPartyReturnDetailsHistory.setNdeputyusercode(getShort(arg0, "ndeputyusercode", arg1));
		objBioThirdPartyReturnDetailsHistory.setNdeputyuserrolecode(getShort(arg0, "ndeputyuserrolecode", arg1));
		objBioThirdPartyReturnDetailsHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioThirdPartyReturnDetailsHistory.setNstatus(getShort(arg0, "nstatus", arg1));

		return objBioThirdPartyReturnDetailsHistory;
	}
}
