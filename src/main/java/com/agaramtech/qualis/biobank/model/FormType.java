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
@Table(name = "formtype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FormType extends CustomizedResultsetRowMapper<TransferType> implements Serializable, RowMapper<FormType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nformtypecode", nullable = false)
	private short nformtypecode;

	@Column(name = "sformtypename", length = 50, nullable = false)
	private String sformtypename;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public FormType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final FormType objFormType = new FormType();

		objFormType.setNformtypecode(getShort(arg0, "nformtypecode", arg1));
		objFormType.setSformtypename(getString(arg0, "sformtypename", arg1));
		objFormType.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objFormType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objFormType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objFormType.setNstatus(getShort(arg0, "nstatus", arg1));

		return objFormType;
	}
}
