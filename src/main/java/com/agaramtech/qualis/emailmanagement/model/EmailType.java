package com.agaramtech.qualis.emailmanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import org.apache.commons.text.StringEscapeUtils;
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

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "emailtype")
public class EmailType extends CustomizedResultsetRowMapper<EmailType> implements Serializable, RowMapper<EmailType>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nemailtypecode")
	private short nemailtypecode;

	@Column(name = "semailtypename", length = 150, nullable = false)
	private String semailtypename;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private int nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Override
	public EmailType mapRow(ResultSet arg0, int arg1) throws SQLException {
		EmailType emailType = new EmailType();
		emailType.setNemailtypecode(getShort(arg0, "nemailtypecode", arg1));
		emailType.setSemailtypename(StringEscapeUtils.unescapeJava(getString(arg0, "semailtypename", arg1)));
		emailType.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		emailType.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		emailType.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		emailType.setNsitecode(getInteger(arg0, "nsitecode", arg1));
		emailType.setNstatus(getInteger(arg0, "nstatus", arg1));
		return emailType;
	}

}
