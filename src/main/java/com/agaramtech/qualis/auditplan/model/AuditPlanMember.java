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
 * This class is used to map the fields of 'auditplanmembers' table of the Database.
 */
/**
* @author AT-E143 SWSM-6 05/08/2025
*/
@Entity
@Table(name = "auditplanmembers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuditPlanMember extends CustomizedResultsetRowMapper<AuditPlanMember> implements Serializable,RowMapper<AuditPlanMember>{
	
	private static final long serialVersionUID = 1L;
    
    @Id
	@Column(name = "nauditplanmembercode")
	private int nauditplanmembercode;
	
	@Column(name = "nauditplancode", nullable = false)
	private int nauditplancode;
	
	
	@Column(name = "nusercode", nullable = false)
	private int nusercode;
    
    @Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	
	@Transient
	private transient String susername;
	
	@Transient
	private transient String smodifieddate;	
	
	@Transient
	private transient String saudittitle;

	@Transient
	private transient String sauditid;
	
	public AuditPlanMember mapRow(ResultSet arg0, int arg1) throws SQLException {
	
		final AuditPlanMember objAuditPlanMember = new AuditPlanMember();
		objAuditPlanMember.setNauditplanmembercode(getInteger(arg0,"nauditplanmembercode",arg1));
		objAuditPlanMember.setNauditplancode(getInteger(arg0,"nauditplancode",arg1));
		objAuditPlanMember.setNusercode(getInteger(arg0,"nusercode",arg1));
		objAuditPlanMember.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objAuditPlanMember.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objAuditPlanMember.setNstatus(getShort(arg0,"nstatus",arg1));
		objAuditPlanMember.setSusername(getString(arg0,"susername",arg1));
		objAuditPlanMember.setSmodifieddate(getString(arg0,"smodifieddate",arg1));		
		objAuditPlanMember.setSaudittitle(getString(arg0,"saudittitle",arg1));				
		objAuditPlanMember.setSauditid(getString(arg0,"sauditid",arg1));				

		return objAuditPlanMember;
	}	
		
}
