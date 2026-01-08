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
@Table(name = "biothirdpartyformacceptdetailshistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioThirdPartyFormAcceptDetailsHistory
		extends CustomizedResultsetRowMapper<BioThirdPartyFormAcceptDetailsHistory>
		implements Serializable, RowMapper<BioThirdPartyFormAcceptDetailsHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiothirdpartyformacceptdetailshistorycode", nullable = false)
	private int nbiothirdpartyformacceptdetailshistorycode;

	@Column(name = "nbiothirdpartyformacceptancedetailscode", nullable = false)
	private int nbiothirdpartyformacceptancedetailscode;

	@Column(name = "nbiothirdpartyformacceptancecode", nullable = false)
	private int nbiothirdpartyformacceptancecode;

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
	public BioThirdPartyFormAcceptDetailsHistory mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioThirdPartyFormAcceptDetailsHistory objBioThirdPartyFormAcceptDetailsHistory = new BioThirdPartyFormAcceptDetailsHistory();

		objBioThirdPartyFormAcceptDetailsHistory.setNbiothirdpartyformacceptdetailshistorycode(
				getInteger(arg0, "nbiothirdpartyformacceptdetailshistorycode", arg1));
		objBioThirdPartyFormAcceptDetailsHistory.setNbiothirdpartyformacceptancedetailscode(
				getInteger(arg0, "nbiothirdpartyformacceptancedetailscode", arg1));
		objBioThirdPartyFormAcceptDetailsHistory
				.setNbiothirdpartyformacceptancecode(getInteger(arg0, "nbiothirdpartyformacceptancecode", arg1));
		objBioThirdPartyFormAcceptDetailsHistory.setNsamplecondition(getShort(arg0, "nsamplecondition", arg1));
		objBioThirdPartyFormAcceptDetailsHistory.setNsamplestatus(getShort(arg0, "nsamplestatus", arg1));
		objBioThirdPartyFormAcceptDetailsHistory.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioThirdPartyFormAcceptDetailsHistory.setNtztransactiondate(getInteger(arg0, "ntztransactiondate", arg1));
		objBioThirdPartyFormAcceptDetailsHistory
				.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioThirdPartyFormAcceptDetailsHistory.setNusercode(getShort(arg0, "nusercode", arg1));
		objBioThirdPartyFormAcceptDetailsHistory.setNuserrolecode(getShort(arg0, "nuserrolecode", arg1));
		objBioThirdPartyFormAcceptDetailsHistory.setNdeputyusercode(getShort(arg0, "ndeputyusercode", arg1));
		objBioThirdPartyFormAcceptDetailsHistory.setNdeputyuserrolecode(getShort(arg0, "ndeputyuserrolecode", arg1));
		objBioThirdPartyFormAcceptDetailsHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioThirdPartyFormAcceptDetailsHistory.setNstatus(getShort(arg0, "nstatus", arg1));

		return objBioThirdPartyFormAcceptDetailsHistory;
	}
}
