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
 * This class is used to map the fields of 'emailuserquery' table of the Database.
 */
@Entity
@Table(name = "emailuserquery")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EmailUserQuery extends CustomizedResultsetRowMapper<EmailUserQuery> implements Serializable, RowMapper<EmailUserQuery> {
	

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="nemailuserquerycode")
	private int nemailuserquerycode;
	
	@Column(name="nformcode", nullable=false)
	private short nformcode;
	
	//Added by sonia on 30th Oct 2025 for jira id:BGSI-155
	@Column(name = "nemailtypecode", nullable = false)
	private short nemailtypecode;
	
	@Column(name = "ncontrolcode", nullable = false)
	private short ncontrolcode;
	
	@Column(name = "nemailscreenschedulercode", nullable = false)
	private short nemailscreenschedulercode;
	
	//Added by sonia on 18th Nov 2025 for jira id:BGSI-234
	@ColumnDefault("3")
	@Column(name = "nqueryneed", nullable = false)
	private short nqueryneed=(short)Enumeration.TransactionStatus.YES.gettransactionstatus();	
	
	@Column(name ="sdisplayname" ,length=20, nullable=false) 
	private String sdisplayname;
	
	//Added by sonia on 15th Sept 2025 for jira id:SWSM-12
	@Column(name ="scolumnname" ,length=100, nullable=false) 
	private String scolumnname;
		
	//Added by sonia on 15th Sept 2025 for jira id:SWSM-12
	@Column(name ="stablename" ,length=100, nullable=false) 
	private String stablename;
	
	@Column(name="squery", columnDefinition="text", nullable=false )
	private String squery;
		
	@Column(name = "dmodifieddate", nullable=false) 
	private Instant dmodifieddate;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	
	@Override
	public EmailUserQuery mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final EmailUserQuery objEmailUserQuery = new EmailUserQuery();
		
		objEmailUserQuery.setNemailuserquerycode(getInteger(arg0,"nemailuserquerycode",arg1));
		objEmailUserQuery.setNformcode(getShort(arg0,"nformcode",arg1));
		objEmailUserQuery.setNemailtypecode(getShort(arg0, "nemailtypecode", arg1)); //Added by sonia on 30th Oct 2025 for jira id:BGSI-155
		objEmailUserQuery.setNcontrolcode(getShort(arg0, "ncontrolcode", arg1)); //Added by sonia on 30th Oct 2025 for jira id:BGSI-155
		objEmailUserQuery.setNemailscreenschedulercode(getShort(arg0, "nemailscreenschedulercode", arg1)); 	//Added by sonia on 30th Oct 2025 for jira id:BGSI-155
		objEmailUserQuery.setNqueryneed(getShort(arg0, "nqueryneed", arg1)); 	//Added by sonia on 18th Nov 2025 for jira id:BGSI-234		
		objEmailUserQuery.setSdisplayname(StringEscapeUtils.unescapeJava(getString(arg0,"sdisplayname",arg1)));
		objEmailUserQuery.setScolumnname(StringEscapeUtils.unescapeJava(getString(arg0,"scolumnname",arg1))); //Added by sonia on 15th Sept 2025 for jira id:SWSM-12
		objEmailUserQuery.setStablename(StringEscapeUtils.unescapeJava(getString(arg0,"stablename",arg1))); //Added by sonia on 15th Sept 2025 for jira id:SWSM-12
		objEmailUserQuery.setSquery(StringEscapeUtils.unescapeJava(getString(arg0,"squery",arg1)));
		objEmailUserQuery.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objEmailUserQuery.setNstatus(getShort(arg0,"nstatus",arg1));


		return objEmailUserQuery;
	}
	
}
