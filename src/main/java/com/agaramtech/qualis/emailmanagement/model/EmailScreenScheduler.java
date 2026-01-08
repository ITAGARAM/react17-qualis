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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emailscreenscheduler")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EmailScreenScheduler extends CustomizedResultsetRowMapper<EmailScreenScheduler> implements Serializable, RowMapper<EmailScreenScheduler>{

	private static final long serialVersionUID = 1L;
	
	@Column(name = "nemailscreenschedulercode")
	private short nemailscreenschedulercode;
	
	@Column(name="nformcode", nullable=false)
	private int nformcode;
	
	@Column(name ="sscheduletypename" ,length=100, nullable=false) 
	private String sscheduletypename;
	
	@Column(name ="scolumnname" ,length=100, nullable=false) 
	private String scolumnname;
	
	@Column(name ="stablename" ,length=100, nullable=false) 
	private String stablename;
	
	@Column(name="squery", columnDefinition="text", nullable=false )
	private String squery;
	
	@Column(name = "dmodifieddate", nullable=false) 
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	
	@Override
	public EmailScreenScheduler mapRow(final ResultSet arg0, final int arg1) throws SQLException {
		
		final EmailScreenScheduler objEmailScreenScheduler = new EmailScreenScheduler();
		objEmailScreenScheduler.setNemailscreenschedulercode(getShort(arg0, "nemailscreenschedulercode", arg1)); 
		objEmailScreenScheduler.setNformcode(getInteger(arg0, "nformcode", arg1));
		objEmailScreenScheduler.setSscheduletypename(StringEscapeUtils.unescapeJava(getString(arg0,"sscheduletypename",arg1)));
		objEmailScreenScheduler.setScolumnname(StringEscapeUtils.unescapeJava(getString(arg0,"scolumnname",arg1))); 
		objEmailScreenScheduler.setStablename(StringEscapeUtils.unescapeJava(getString(arg0,"stablename",arg1))); 
		objEmailScreenScheduler.setSquery(StringEscapeUtils.unescapeJava(getString(arg0,"squery",arg1)));
		objEmailScreenScheduler.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objEmailScreenScheduler.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objEmailScreenScheduler.setNstatus(getShort(arg0, "nstatus", arg1));
		return objEmailScreenScheduler;
		
	}
}
