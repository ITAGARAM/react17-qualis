package com.agaramtech.qualis.registration.model;

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
 * This class is used to map the fields of 'registrationtestpackage' table of the Database.
 */
@Entity
@Table(name = "registrationtestpackage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class RegistrationTestPackage extends CustomizedResultsetRowMapper<RegistrationTestPackage> implements Serializable ,RowMapper<RegistrationTestPackage>{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nregistrationtestpackagecode")
	private int nregistrationtestpackagecode;
	
	@Column(name = "npreregno", nullable = false)
	private int npreregno;
	
	@Column(name = "ntransactionsamplecode", nullable = false)
	private int ntransactionsamplecode;
	
	@Column(name = "ntestpackagecode", nullable = false)
	private int ntestpackagecode;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	
	@Column(name="dmodifieddate", nullable = false)
	private Instant dmodifieddate;	

	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String sarno;
	
	@Transient
	private transient String stestpackagename;




	@Override
	public RegistrationTestPackage mapRow(ResultSet arg0, int arg1) throws SQLException {
		final RegistrationTestPackage objRegistraTionTestPackage = new RegistrationTestPackage();
		objRegistraTionTestPackage.setNregistrationtestpackagecode(getInteger(arg0,"nregistrationtestpackagecode",arg1));
		objRegistraTionTestPackage.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objRegistraTionTestPackage.setNtestpackagecode(getInteger(arg0,"ntestpackagecode",arg1));
		objRegistraTionTestPackage.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objRegistraTionTestPackage.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objRegistraTionTestPackage.setNstatus(getShort(arg0,"nstatus",arg1));
		objRegistraTionTestPackage.setSarno(getString(arg0,"sarno",arg1));
		objRegistraTionTestPackage.setStestpackagename(getString(arg0,"stestpackagename",arg1));

		return objRegistraTionTestPackage;
	}


}
