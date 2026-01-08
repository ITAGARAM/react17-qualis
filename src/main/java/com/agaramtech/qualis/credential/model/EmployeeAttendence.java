package com.agaramtech.qualis.credential.model;

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
 * @author sujatha.v AT-E274
 * SWSM-7 14/08/2025  
 */
@Entity
@Table(name = "employeeattendence")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EmployeeAttendence extends CustomizedResultsetRowMapper<EmployeeAttendence>
		implements Serializable, RowMapper<EmployeeAttendence> {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nempattendencecode")
	private int nempattendencecode;
	
	@Column(name = "nemptypecode",  nullable = false)
	private int nemptypecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nusercode", nullable = false)
	private int nusercode;
	
	@ColumnDefault("4")
	@Column(name = "nispresent")
	private short nispresent =(short)Enumeration.TransactionStatus.NO.gettransactionstatus();	
	
	@Column(name = "dentrydatetime")
	private Instant dentrydatetime;
	
	@ColumnDefault("-1")
	@Column(name = "ntzentrydatetime", nullable = false)
	private short ntzentrydatetime=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "noffsetdentrydatetime", nullable = false)
	private int noffsetdentrydatetime;
	
	@Column(name = "dexitdatetime")
	private Instant dexitdatetime;
	
	@ColumnDefault("-1")
	@Column(name = "ntzexitdatetime", nullable = false)
	private short ntzexitdatetime=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "noffsetdexitdatetime", nullable = false)
	private int noffsetdexitdatetime;
	
	@Column(name = "dattendencedate")
	private Instant dattendencedate;
	
	@ColumnDefault("-1")
	@Column(name = "noffsetdattendencedate", nullable = false)
	private short noffsetdattendencedate=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ntzattendencedate", nullable = false)
	private int ntzattendencedate;
	
	@Column(name = "sremarks", length = 600, nullable = false)
	private String sremarks;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient  String sentrydatetime;
	
	@Transient
	private transient  String sexitdatetime;
	
	@Transient
	private transient String sattendencedate;
	
	@Transient
	private transient String susername;
	
	@Transient
	private transient String semptypename;
	
	@Transient
	private transient String sispresent;
	
	@Transient
	private transient String smodifieddate;
	
	@Override
	public EmployeeAttendence mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final EmployeeAttendence employeeAttendence = new EmployeeAttendence();
		employeeAttendence.setNempattendencecode(getInteger(arg0, "nempattendencecode", arg1));
		employeeAttendence.setNemptypecode(getInteger(arg0, "nemptypecode", arg1));
		employeeAttendence.setNusercode(getInteger(arg0, "nusercode", arg1));
		employeeAttendence.setNispresent(getShort(arg0, "nispresent", arg1));
		employeeAttendence.setDentrydatetime(getInstant(arg0, "dentrydatetime", arg1));
		employeeAttendence.setNtzentrydatetime(getShort(arg0, "ntzentrydatetime", arg1));
		employeeAttendence.setNoffsetdentrydatetime(getInteger(arg0, "noffsetdentrydatetime", arg1));
		employeeAttendence.setDexitdatetime(getInstant(arg0, "dexitdatetime", arg1));
		employeeAttendence.setNtzexitdatetime(getShort(arg0, "ntzexitdatetime", arg1));
		employeeAttendence.setNoffsetdexitdatetime(getInteger(arg0, "noffsetdexitdatetime", arg1));
		employeeAttendence.setDattendencedate(getInstant(arg0, "dattendencedate", arg1));
		employeeAttendence.setNoffsetdattendencedate(getShort(arg0, "noffsetdattendencedate", arg1));
		employeeAttendence.setNtzattendencedate(getShort(arg0, "ntzdattendencedate", arg1));
		employeeAttendence.setSremarks(getString(arg0, "sremarks", arg1));
		employeeAttendence.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		employeeAttendence.setNsitecode(getShort(arg0, "nsitecode", arg1));
		employeeAttendence.setNstatus(getShort(arg0, "nstatus", arg1));
		employeeAttendence.setSentrydatetime(getString(arg0, "sentrydatetime", arg1));
		employeeAttendence.setSexitdatetime(getString(arg0, "sexitdatetime", arg1));
		employeeAttendence.setSattendencedate(getString(arg0, "sattendencedate", arg1));
		employeeAttendence.setSusername(getString(arg0, "susername", arg1));
		employeeAttendence.setSemptypename(getString(arg0, "semptypename", arg1));
		employeeAttendence.setSispresent(getString(arg0, "sispresent", arg1));
		employeeAttendence.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		return employeeAttendence;
	}
}
