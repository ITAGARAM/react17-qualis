package com.agaramtech.qualis.emailmanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import org.apache.commons.text.StringEscapeUtils;
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

/**
 * This class is used to map the fields of 'smssettings' table of the Database.
 */
@Entity
@Table(name = "smssettings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SmsSettings extends CustomizedResultsetRowMapper<SmsSettings>
		implements Serializable, RowMapper<SmsSettings> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsmssettingcode")
	private int nsmssettingcode;

	@Column(name = "ssid", nullable = false)
	private String ssid;

	@Column(name = "sauthtoken", nullable = false)
	private String sauthtoken;
	
	@Column(name = "svirtualno", nullable = false)
	private String svirtualno;

	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SmsSettings mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SmsSettings smsSettings = new SmsSettings();
		smsSettings.setNsmssettingcode(getInteger(arg0, "nsmssettingcode", arg1));
		smsSettings.setSsid(StringEscapeUtils.unescapeJava(getString(arg0, "ssid", arg1)));
		smsSettings.setSauthtoken(StringEscapeUtils.unescapeJava(getString(arg0, "sauthtoken", arg1)));
		smsSettings.setSvirtualno(StringEscapeUtils.unescapeJava(getString(arg0, "svirtualno", arg1)));
		smsSettings.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		smsSettings.setNsitecode(getShort(arg0, "nsitecode", arg1));
		smsSettings.setNstatus(getShort(arg0, "nstatus", arg1));
		return smsSettings;
	}

}
