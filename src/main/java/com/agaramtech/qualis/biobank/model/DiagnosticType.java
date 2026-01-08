package com.agaramtech.qualis.biobank.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
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

/**
 * This class is used to map the fields of 'diagnostictype' table of
 * the Database.
 */

@Entity
@Table(name = "diagnostictype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DiagnosticType extends CustomizedResultsetRowMapper<DiagnosticType> implements Serializable, RowMapper<DiagnosticType>{

	
private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ndiagnostictypecode")
	private int ndiagnostictypecode;

	@Column(name = "sdiagnostictypename", length = 100,nullable = false)
	private String sdiagnostictypename;

	@Column(name = "sdescirption", length = 255)
	private String sdescirption;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb",nullable = false)
	private Map<String, Object> jsondata;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Override
	public DiagnosticType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final DiagnosticType objDiagnosticType = new DiagnosticType();

		objDiagnosticType.setNdiagnostictypecode(getInteger(arg0, "ndiagnostictypecode", arg1));
		objDiagnosticType.setSdiagnostictypename(getString(arg0, "sdiagnostictypename", arg1));
		objDiagnosticType.setSdescirption(getString(arg0, "sdescirption", arg1));
		objDiagnosticType.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objDiagnosticType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objDiagnosticType.setNstatus(getShort(arg0, "nstatus", arg1));
		
		return objDiagnosticType;
	}
}
