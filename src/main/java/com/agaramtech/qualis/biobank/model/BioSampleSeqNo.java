package com.agaramtech.qualis.biobank.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

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
@Table(name = "biosampleseqno")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioSampleSeqNo extends CustomizedResultsetRowMapper<BioSampleSeqNo>
		implements Serializable, RowMapper<BioSampleSeqNo> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbioprojectcode", nullable = false)
	private int nbioprojectcode;

	@Column(name = "ssequenceno", length = 10, nullable = false)
	private String ssequenceno;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public BioSampleSeqNo mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioSampleSeqNo objBioSampleSeqNo = new BioSampleSeqNo();

		objBioSampleSeqNo.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objBioSampleSeqNo.setSsequenceno(getString(arg0, "ssequenceno", arg1));
		objBioSampleSeqNo.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioSampleSeqNo.setNstatus(getShort(arg0, "nstatus", arg1));

		return objBioSampleSeqNo;
	}

}