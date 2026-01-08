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
@Table(name = "bioexternaltransferdata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioExternalTransferData extends CustomizedResultsetRowMapper<BioExternalTransferData>
		implements Serializable, RowMapper<BioExternalTransferData> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbioexternaltransfercode", nullable = false)
	private int nbioexternaltransfercode;

	@Column(name = "nthirdpartycode", nullable = false)
	private int nthirdpartycode;

	@Column(name = "nbiothirdpartyformacceptancecode", nullable = false)
	private int nbiothirdpartyformacceptancecode;

	@Column(name = "sformnumber", length = 50, nullable = false)
	private String sformnumber;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "nsentstatus", nullable = false)
	private short nsentstatus;

	@Column(name = "nreceivedstatus", nullable = false)
	private short nreceivedstatus;

	@Column(name = "dreceiveddate")
	private Instant dreceiveddate;

	@Column(name = "scomments", length = 255)
	private String scomments;

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
	public BioExternalTransferData mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioExternalTransferData objBioExternalTransferData = new BioExternalTransferData();

		objBioExternalTransferData.setNbioexternaltransfercode(getInteger(arg0, "nbioexternaltransfercode", arg1));
		objBioExternalTransferData.setNthirdpartycode(getInteger(arg0, "nthirdpartycode", arg1));
		objBioExternalTransferData
				.setNbiothirdpartyformacceptancecode(getInteger(arg0, "nbiothirdpartyformacceptancecode", arg1));
		objBioExternalTransferData.setSformnumber(getString(arg0, "sformnumber", arg1));
		objBioExternalTransferData.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objBioExternalTransferData.setNsentstatus(getShort(arg0, "nsentstatus", arg1));
		objBioExternalTransferData.setNreceivedstatus(getShort(arg0, "nreceivedstatus", arg1));
		objBioExternalTransferData.setDreceiveddate(getInstant(arg0, "dreceiveddate", arg1));
		objBioExternalTransferData.setScomments(getString(arg0, "scomments", arg1));
		objBioExternalTransferData.setNusercode(getInteger(arg0, "nusercode", arg1));
		objBioExternalTransferData.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objBioExternalTransferData.setNdeputyusercode(getInteger(arg0, "ndeputyusercode", arg1));
		objBioExternalTransferData.setNdeputyuserrolecode(getInteger(arg0, "ndeputyuserrolecode", arg1));
		objBioExternalTransferData.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioExternalTransferData.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objBioExternalTransferData.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioExternalTransferData.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioExternalTransferData.setNstatus(getShort(arg0, "nstatus", arg1));

		return objBioExternalTransferData;
	}
}
