package com.agaramtech.qualis.auditplan.model;

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
 * This class is used to map the fields of 'auditplanauditors' table of the Database.
 */
/**
* @author AT-E143 SWSM-6 05/08/2025
*/
@Entity
@Table(name = "auditplanauditors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuditPlanAuditor extends CustomizedResultsetRowMapper<AuditPlan> implements Serializable,RowMapper<AuditPlanAuditor>{
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nauditplanauditorcode")
	private int nauditplanauditorcode;
		
	@Column(name = "nauditplancode", nullable = false)
	private int nauditplancode;
			
	@Column(name = "nauditmastercode", nullable = false)
	private int nauditmastercode;
	    
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
		
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String sauditorname;
	
	@Transient
	private transient String smodifieddate;	
	
	@Transient
	private transient String saudittitle;

	@Transient
	private transient String sauditid;
	
	
	public AuditPlanAuditor mapRow(ResultSet arg0, int arg1) throws SQLException {
		final AuditPlanAuditor objAuditPlanAuditor = new AuditPlanAuditor();
		objAuditPlanAuditor.setNauditplanauditorcode(getInteger(arg0,"nauditplanauditorcode",arg1));
		objAuditPlanAuditor.setNauditplancode(getInteger(arg0,"nauditplancode",arg1));
		objAuditPlanAuditor.setNauditmastercode(getInteger(arg0,"nauditmastercode",arg1));
		objAuditPlanAuditor.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objAuditPlanAuditor.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objAuditPlanAuditor.setNstatus(getShort(arg0,"nstatus",arg1));
		objAuditPlanAuditor.setSauditorname(getString(arg0,"sauditorname",arg1));
		objAuditPlanAuditor.setSmodifieddate(getString(arg0,"smodifieddate",arg1));		
		objAuditPlanAuditor.setSaudittitle(getString(arg0,"saudittitle",arg1));				
		objAuditPlanAuditor.setSauditid(getString(arg0,"sauditid",arg1));		
		return objAuditPlanAuditor;
	}
	
	

}
