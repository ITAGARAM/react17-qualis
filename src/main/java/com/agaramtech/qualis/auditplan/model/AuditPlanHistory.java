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
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'auditplanhistory' table of the Database.
 */
/**
* @author AT-E143 SWSM-6 05/08/2025
*/
@Entity
@Table(name = "auditplanhistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuditPlanHistory extends CustomizedResultsetRowMapper<AuditPlanHistory> implements Serializable,RowMapper<AuditPlanHistory>{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nauditplanhistorycode")
	private int nauditplanhistorycode;
	
	@Column(name = "nauditplancode", nullable = false)
	private int nauditplancode;
	
	@ColumnDefault("-1")
	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nusercode", nullable = false)
	private int nusercode;
	
	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;
	
	@Column(name = "ndeputyusercode", nullable = false)
	private int ndeputyusercode;
	
	@Column(name = "ndeputyuserrolecode", nullable = false)
	private int ndeputyuserrolecode;
	
	@Column(name = "sremarks", length=255, nullable = false)
	private String sremarks;
	
	@Column(name = "dauditdatetime", nullable = false)
	private Instant dauditdatetime;
			
	@ColumnDefault("-1")
	@Column(name = "ntzauditdatetime", nullable = false)
	private short ntzauditdatetime=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "noffsetdauditdatetime", nullable = false)
	private int noffsetdauditdatetime;
	
	@Column(name = "dcreateddate", nullable = false)
	private Instant dcreateddate;
			
	@ColumnDefault("-1")
	@Column(name = "ntzcreateddate", nullable = false)
	private short ntzcreateddate=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "noffsetdcreateddate", nullable = false)
	private int noffsetdcreateddate;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String susername;
	
	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String sdeputyusername;
	
	@Transient
	private transient String sdeputyuserrolename;
	
	@Transient
	private transient String sauditdatetime;
	
	@Transient
	private transient String screateddate;	
	
	@Transient
	private transient String smodifieddate;	
	
	@Transient
	private transient short ninputtimezonecode; 
	
	@Transient 
	private transient String stimezoneid; 
	
	@Transient
	private transient String saudittitle;

	@Transient
	private transient String sauditid;
	
	@Transient
	private transient String srescheduleremarks;

	
	public AuditPlanHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final AuditPlanHistory objAuditPlanHistory = new AuditPlanHistory();
		objAuditPlanHistory.setNauditplanhistorycode(getInteger(arg0,"nauditplanhistorycode",arg1));
		objAuditPlanHistory.setNauditplancode(getInteger(arg0,"nauditplancode",arg1));
		objAuditPlanHistory.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objAuditPlanHistory.setNusercode(getInteger(arg0,"nusercode",arg1));
		objAuditPlanHistory.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objAuditPlanHistory.setNdeputyusercode(getInteger(arg0,"ndeputyusercode",arg1));
		objAuditPlanHistory.setNdeputyuserrolecode(getInteger(arg0,"ndeputyuserrolecode",arg1));
		objAuditPlanHistory.setSremarks(StringEscapeUtils.unescapeJava(getString(arg0,"sremarks",arg1)));
		objAuditPlanHistory.setDauditdatetime(getInstant(arg0,"dauditdatetime",arg1));
		objAuditPlanHistory.setNtzauditdatetime(getShort(arg0,"ntzauditdatetime",arg1));
		objAuditPlanHistory.setNoffsetdauditdatetime(getInteger(arg0,"noffsetdauditdatetime",arg1));
		objAuditPlanHistory.setDcreateddate(getInstant(arg0,"dcreateddate",arg1));
		objAuditPlanHistory.setNtzcreateddate(getShort(arg0,"ntzcreateddate",arg1));
		objAuditPlanHistory.setNoffsetdcreateddate(getInteger(arg0,"noffsetdcreateddate",arg1));
		objAuditPlanHistory.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objAuditPlanHistory.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objAuditPlanHistory.setNstatus(getShort(arg0,"nstatus",arg1));
		objAuditPlanHistory.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objAuditPlanHistory.setSusername(getString(arg0,"susername",arg1));
		objAuditPlanHistory.setSuserrolename(getString(arg0,"suserrolename",arg1));
		objAuditPlanHistory.setSdeputyusername(getString(arg0,"sdeputyusername",arg1));
		objAuditPlanHistory.setSdeputyuserrolename(getString(arg0,"sdeputyuserrolename",arg1));		
		objAuditPlanHistory.setSauditdatetime(getString(arg0,"sauditdatetime",arg1));
		objAuditPlanHistory.setScreateddate(getString(arg0,"screateddate",arg1));
		objAuditPlanHistory.setSmodifieddate(getString(arg0,"smodifieddate",arg1));
		objAuditPlanHistory.setNinputtimezonecode(getShort(arg0,"ninputtimezonecode",arg1));
		objAuditPlanHistory.setStimezoneid(getString(arg0,"stimezoneid",arg1));	
		objAuditPlanHistory.setSaudittitle(getString(arg0,"saudittitle",arg1));				
		objAuditPlanHistory.setSauditid(getString(arg0,"sauditid",arg1));				
		objAuditPlanHistory.setSrescheduleremarks(getString(arg0,"srescheduleremarks",arg1));				

		return objAuditPlanHistory;
	}	
}
