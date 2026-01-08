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
@Table(name = "biomovetodispose")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioMoveToDispose extends CustomizedResultsetRowMapper<BioMoveToDispose>
		implements Serializable, RowMapper<BioMoveToDispose> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiomovetodisposecode", nullable = false)
	private int nbiomovetodisposecode;

	@Column(name = "sformnumber", length = 50, nullable = false)
	private String sformnumber;

	@Column(name = "ntransfertypecode", nullable = false)
	private short ntransfertypecode;

	@Column(name = "nformtypecode", nullable = false)
	private short nformtypecode;

	@Column(name = "nthirdpartycode", nullable = false)
	private int nthirdpartycode;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@Column(name = "sremarks", length = 255)
	private String sremarks;

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
	public BioMoveToDispose mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioMoveToDispose objBioMoveToDispose = new BioMoveToDispose();

		objBioMoveToDispose.setNbiomovetodisposecode(getInteger(arg0, "nbiomovetodisposecode", arg1));
		objBioMoveToDispose.setSformnumber(getString(arg0, "sformnumber", arg1));
		objBioMoveToDispose.setNtransfertypecode(getShort(arg0, "ntransfertypecode", arg1));
		objBioMoveToDispose.setNformtypecode(getShort(arg0, "nformtypecode", arg1));
		objBioMoveToDispose.setNthirdpartycode(getInteger(arg0, "nthirdpartycode", arg1));
		objBioMoveToDispose.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objBioMoveToDispose.setSremarks(getString(arg0, "sremarks", arg1));
		objBioMoveToDispose.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioMoveToDispose.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objBioMoveToDispose.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioMoveToDispose.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioMoveToDispose.setNstatus(getShort(arg0, "nstatus", arg1));

		return objBioMoveToDispose;

	}
}
