package com.agaramtech.qualis.credential.model;

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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author sujatha.v AT-E274
 * SWSM-7 14/08/2025  
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "employeetype")
@Data
public class EmployeeType extends CustomizedResultsetRowMapper<EmployeeType> implements Serializable, RowMapper<EmployeeType> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nemptypecode")
	private int nemptypecode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String semptypename;

	@Override
	public EmployeeType mapRow(ResultSet arg0, int arg1) throws SQLException {
		final EmployeeType employeeType= new EmployeeType();
		employeeType.setNemptypecode(getInteger(arg0, "nemptypecode", arg1));
		employeeType.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		employeeType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		employeeType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		employeeType.setNstatus(getShort(arg0, "nstatus", arg1));
		employeeType.setSemptypename(getString(arg0, "semptypename", arg1));
		return employeeType;
	}
}
