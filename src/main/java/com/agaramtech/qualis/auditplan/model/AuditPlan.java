package com.agaramtech.qualis.auditplan.model;

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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'auditplan' table of the Database.
 */
/**
* @author AT-E143 SWSM-6 05/08/2025
*/
@Entity
@Table(name = "auditplan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuditPlan extends CustomizedResultsetRowMapper<AuditPlan> implements Serializable,RowMapper<AuditPlan> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nauditplancode")
	private int nauditplancode;
	
	@Column(name = "naudittypecode", nullable = false)
	private short naudittypecode;
	
	@Column(name = "nauditcategorycode", nullable = false)
	private int nauditcategorycode;
	
	@Column(name = "nauditstandardcatcode", nullable = false)
	private int nauditstandardcatcode;
	
	@Column(name = "ndeptcode", nullable = false)
	private int ndeptcode;
	
	@Column(name = "ndeptheadcode", nullable = false)
	private int ndeptheadcode;	
	
	@Column(name = "saudittitle", length=150, nullable = false)
	private String saudittitle;
	
	@Column(name = "scompauditrep", length=150, nullable = false)
	private String scompauditrep;
	
	@Column(name = "sauditid",length=50)  
	private String sauditid;
	
	@Column(name = "dauditdatetime", nullable = false)
	private Instant dauditdatetime;	
	
	@ColumnDefault("-1")
	@Column(name = "ntzauditdatetime", nullable = false)
	private short ntzauditdatetime=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "noffsetdauditdatetime", nullable = false)
	private int noffsetdauditdatetime;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String saudittypename;	
	
	@Transient
	private transient String sauditcategoryname;	
	
	@Transient
	private transient String sauditstandardcatname;	
	
	@Transient
	private transient String sdeptname;	
	
	@Transient
	private transient String sdeptheadname;	
	
	@Transient
	private transient String sauditdatetime;	
	
	@Transient
	private transient String smodifieddate;	
	
	@Transient
	private transient short ninputtimezonecode; 
	
	@Transient 
	private transient String stimezoneid; 
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient int  ntransactionstatus;
	
	@Transient
	private transient String scolorhexcode;
	
	@Transient
	private transient int nusercode;
	
	@Transient
	private transient int nauditmastercode;
	
	//Added by sonia on 10th Sept 2025 for jira id:SWSM-12
	@Transient
	private transient String sremainderauditdatetime;	

	
	public AuditPlan mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final AuditPlan objAuditPlan = new AuditPlan();
		objAuditPlan.setNauditplancode(getInteger(arg0,"nauditplancode",arg1));
		objAuditPlan.setNaudittypecode(getShort(arg0,"naudittypecode",arg1));
		objAuditPlan.setNauditcategorycode(getInteger(arg0,"nauditcategorycode",arg1));
		objAuditPlan.setNauditstandardcatcode(getInteger(arg0,"nauditstandardcatcode",arg1));		
		objAuditPlan.setNdeptcode(getInteger(arg0,"ndeptcode",arg1));
		objAuditPlan.setNdeptheadcode(getInteger(arg0,"ndeptheadcode",arg1));
		objAuditPlan.setSaudittitle(StringEscapeUtils.unescapeJava(getString(arg0,"saudittitle",arg1)));
		objAuditPlan.setScompauditrep(StringEscapeUtils.unescapeJava(getString(arg0,"scompauditrep",arg1)));
		objAuditPlan.setSauditid(getString(arg0,"sauditid",arg1));
		objAuditPlan.setDauditdatetime(getInstant(arg0,"dauditdatetime",arg1));
		objAuditPlan.setNtzauditdatetime(getShort(arg0,"ntzauditdatetime",arg1));
		objAuditPlan.setNoffsetdauditdatetime(getInteger(arg0,"noffsetdauditdatetime",arg1));
		objAuditPlan.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objAuditPlan.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objAuditPlan.setNstatus(getShort(arg0,"nstatus",arg1));
		objAuditPlan.setSaudittypename(getString(arg0,"saudittypename",arg1));
		objAuditPlan.setSauditcategoryname(getString(arg0,"sauditcategoryname",arg1));
		objAuditPlan.setSauditstandardcatname(getString(arg0,"sauditstandardcatname",arg1));
		objAuditPlan.setSdeptname(getString(arg0,"sdeptname",arg1));
		objAuditPlan.setSdeptheadname(getString(arg0,"sdeptheadname",arg1));
		objAuditPlan.setSauditdatetime(getString(arg0,"sauditdatetime",arg1));
		objAuditPlan.setSmodifieddate(getString(arg0,"smodifieddate",arg1));
		objAuditPlan.setNinputtimezonecode(getShort(arg0,"ninputtimezonecode",arg1));
		objAuditPlan.setStimezoneid(getString(arg0,"stimezoneid",arg1));
		objAuditPlan.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objAuditPlan.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objAuditPlan.setScolorhexcode(getString(arg0,"scolorhexcode",arg1));
		objAuditPlan.setNusercode(getInteger(arg0,"nusercode",arg1));
		objAuditPlan.setNauditmastercode(getInteger(arg0,"nauditmastercode",arg1));
		objAuditPlan.setSremainderauditdatetime(getString(arg0,"sremainderauditdatetime",arg1)); //Added by sonia on 10th Sept 2025 for jira id:SWSM-12

		return objAuditPlan;
	}	
}
