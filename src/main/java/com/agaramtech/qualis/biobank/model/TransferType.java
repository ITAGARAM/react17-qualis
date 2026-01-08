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
@Table(name = "transfertype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TransferType extends CustomizedResultsetRowMapper<TransferType>
		implements Serializable, RowMapper<TransferType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntransfertypecode", nullable = false)
	private short ntransfertypecode;

	@Column(name = "stransfertypename", length = 50, nullable = false)
	private String stransfertypename;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public TransferType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final TransferType objTransferType = new TransferType();

		objTransferType.setNtransfertypecode(getShort(arg0, "ntransfertypecode", arg1));
		objTransferType.setStransfertypename(getString(arg0, "stransfertypename", arg1));
		objTransferType.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objTransferType.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objTransferType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objTransferType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objTransferType.setNstatus(getShort(arg0, "nstatus", arg1));

		return objTransferType;
	}
}
