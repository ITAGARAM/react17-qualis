package com.agaramtech.qualis.emailmanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "emailuserroleconfig")
public class EmailUserRoleConfig extends CustomizedResultsetRowMapper<EmailUserRoleConfig>
		implements Serializable, RowMapper<EmailUserRoleConfig> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nemailuserroleconfigcode")
	private int nemailuserroleconfigcode;

	@Column(name = "nemailconfigcode", nullable = false)
	private int nemailconfigcode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient int nusercode;

	@Transient
	private transient String susername;
	
	@Transient
	private transient String suserrolename;

	@Transient
	private transient String semail;

	public EmailUserRoleConfig mapRow(ResultSet arg0, int arg1) throws SQLException {
		EmailUserRoleConfig objEmailUserroleConfig = new EmailUserRoleConfig();
		objEmailUserroleConfig.setNemailuserroleconfigcode(getInteger(arg0, "nemailuserroleconfigcode", arg1));
		objEmailUserroleConfig.setNemailconfigcode(getInteger(arg0, "nemailconfigcode", arg1));
		objEmailUserroleConfig.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objEmailUserroleConfig.setNstatus(getInteger(arg0, "nstatus", arg1));
		objEmailUserroleConfig.setSemail(getString(arg0, "semail", arg1));
		objEmailUserroleConfig.setNusercode(getInteger(arg0, "nusercode", arg1));
		objEmailUserroleConfig.setSusername(getString(arg0, "susername", arg1));
		objEmailUserroleConfig.setSuserrolename(getString(arg0, "suserrolename", arg1));
		return objEmailUserroleConfig;
	}

}
