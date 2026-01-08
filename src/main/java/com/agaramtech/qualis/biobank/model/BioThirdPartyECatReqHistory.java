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
 * Maps to 'biothirdpartyecatreqhistory' table.
 */
@Entity
@Table(name = "biothirdpartyecatreqhistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioThirdPartyECatReqHistory extends CustomizedResultsetRowMapper<BioThirdPartyECatReqHistory>
		implements Serializable, RowMapper<BioThirdPartyECatReqHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiothirdpartyecatreqhistorycode", nullable = false)
	private int nbiothirdpartyecatreqhistorycode;

	@Column(name = "nthirdpartyecatrequestcode", nullable = false)
	private int nthirdpartyecatrequestcode;

	@ColumnDefault("-1")
	@Column(name = "nusercode", nullable = false)
	private int nusercode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ndeputyusercode", nullable = false)
	private int ndeputyusercode;

	@Column(name = "ndeputyuserrolecode", nullable = false)
	private int ndeputyuserrolecode;

	@Column(name = "ntransactionstatus", nullable = false)
	private int ntransactionstatus;

	@Column(name = "scomments", length = 255)
	private String scomments;

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;

	@ColumnDefault("-1")
	@Column(name = "ntztransactiondate", nullable = false)
	private short ntztransactiondate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsettransactiondate", nullable = false)
	private int noffsettransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@Id
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private int nstatus = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	// Transient fields
	@Transient
	private transient String susername;
	@Transient
	private transient String suserrolename;
	@Transient
	private transient String sdeputyusername;
	@Transient
	private transient String sdeputyuserrolename;
	@Transient
	private transient String stransactionstatus;
	@Transient
	private transient String stransactiondate;

	@Override
	public BioThirdPartyECatReqHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
		BioThirdPartyECatReqHistory obj = new BioThirdPartyECatReqHistory();
		obj.setNbiothirdpartyecatreqhistorycode(getInteger(rs, "nbiothirdpartyecatreqhistorycode", rowNum));
		obj.setNthirdpartyecatrequestcode(getInteger(rs, "nthirdpartyecatrequestcode", rowNum));
		obj.setNusercode(getInteger(rs, "nusercode", rowNum));
		obj.setNuserrolecode(getInteger(rs, "nuserrolecode", rowNum));
		obj.setNdeputyusercode(getInteger(rs, "ndeputyusercode", rowNum));
		obj.setNdeputyuserrolecode(getInteger(rs, "ndeputyuserrolecode", rowNum));
		obj.setNtransactionstatus(getInteger(rs, "ntransactionstatus", rowNum));
		obj.setScomments(getString(rs, "scomments", rowNum));
		obj.setDtransactiondate(getInstant(rs, "dtransactiondate", rowNum));
		obj.setNtztransactiondate(getShort(rs, "ntztransactiondate", rowNum));
		obj.setNoffsettransactiondate(getInteger(rs, "noffsettransactiondate", rowNum));
		obj.setNsitecode(getShort(rs, "nsitecode", rowNum));
		obj.setNstatus(getInteger(rs, "nstatus", rowNum));
		obj.setSusername(getString(rs, "susername", rowNum));
		obj.setSuserrolename(getString(rs, "suserrolename", rowNum));
		obj.setSdeputyusername(getString(rs, "sdeputyusername", rowNum));
		obj.setSdeputyuserrolename(getString(rs, "sdeputyuserrolename", rowNum));
		obj.setStransactionstatus(getString(rs, "stransactionstatus", rowNum));
		obj.setStransactiondate(getString(rs, "stransactiondate", rowNum));
		return obj;
	}
}
