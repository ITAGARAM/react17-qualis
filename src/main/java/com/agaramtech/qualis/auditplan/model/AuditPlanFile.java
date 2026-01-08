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
 * This class is used to map the fields of 'auditplanfile' table of the Database.
 */
/**
* @author AT-E143 SWSM-6 05/08/2025
*/
@Entity
@Table(name = "auditplanfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuditPlanFile extends CustomizedResultsetRowMapper<AuditPlanFile> implements Serializable,RowMapper<AuditPlanFile>{
	
	private static final long serialVersionUID = 1L;
	   
	    @Id
		@Column(name = "nauditplanfilecode")
		private int nauditplanfilecode;
		
		@Column(name = "nauditplancode", nullable = false)
		private int nauditplancode;
		
		@ColumnDefault("-1")
		@Column(name = "nlinkcode", nullable = false)
		private short nlinkcode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
		
		@ColumnDefault("-1")
		@Column(name = "nattachmenttypecode", nullable = false)
		private short nattachmenttypecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
		
		@ColumnDefault("0")
		@Column(name = "nfilesize", nullable = false)
		private int nfilesize=(short)Enumeration.TransactionStatus.ALL.gettransactionstatus();
		
		@Column(name = "sfilename", length=100, nullable = false)
		private String sfilename;
		
		@Column(name = "sdescription", length=255, nullable = false)
		private String sdescription;
		
		@Column(name = "ssystemfilename", length=100, nullable = false)
		private String ssystemfilename;
		
		@Column(name = "dcreateddate", nullable = false)
		private Instant dcreateddate;
			
		@Column(name = "noffsetdcreateddate", nullable = false)
		private int noffsetdcreateddate;
		
		@ColumnDefault("-1")
		@Column(name = "ntzcreateddate", nullable = false)
		private short ntzcreateddate=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
		
		@Column(name = "dmodifieddate", nullable = false)
		private Instant dmodifieddate;
			
		@ColumnDefault("-1")
		@Column(name = "nsitecode", nullable = false)
		private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

		@ColumnDefault("1")
		@Column(name = "nstatus", nullable = false)
		private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		@Transient
		private transient String slinkname;
		
		@Transient
		private transient String sattachmenttype;
		
		@Transient
		private transient String stypename;
		
		@Transient
		private transient String sfilesize;
		
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
	    
	public AuditPlanFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		final AuditPlanFile objAuditPlanFile = new AuditPlanFile();
		objAuditPlanFile.setNauditplanfilecode(getInteger(arg0,"nauditplanfilecode",arg1));
		objAuditPlanFile.setNauditplancode(getInteger(arg0,"nauditplancode",arg1));
		objAuditPlanFile.setNlinkcode(getShort(arg0,"nlinkcode",arg1));
		objAuditPlanFile.setNattachmenttypecode(getShort(arg0,"nattachmenttypecode",arg1));
		objAuditPlanFile.setNfilesize(getInteger(arg0,"nfilesize",arg1));
		objAuditPlanFile.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0,"sfilename",arg1)));
		objAuditPlanFile.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objAuditPlanFile.setSsystemfilename(getString(arg0,"ssystemfilename",arg1));		
		objAuditPlanFile.setDcreateddate(getInstant(arg0,"dcreateddate",arg1));
		objAuditPlanFile.setNoffsetdcreateddate(getInteger(arg0,"noffsetdcreateddate",arg1));
		objAuditPlanFile.setNtzcreateddate(getShort(arg0,"ntzcreateddate",arg1));
		objAuditPlanFile.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objAuditPlanFile.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objAuditPlanFile.setNstatus(getShort(arg0,"nstatus",arg1));
		objAuditPlanFile.setSlinkname(getString(arg0,"slinkname",arg1));		
		objAuditPlanFile.setSattachmenttype(getString(arg0,"sattachmenttype",arg1));
		objAuditPlanFile.setStypename(getString(arg0,"stypename",arg1));
		objAuditPlanFile.setSfilesize(getString(arg0,"sfilesize",arg1));
		objAuditPlanFile.setScreateddate(getString(arg0,"screateddate",arg1));
		objAuditPlanFile.setSmodifieddate(getString(arg0,"smodifieddate",arg1));
		objAuditPlanFile.setNinputtimezonecode(getShort(arg0,"ninputtimezonecode",arg1));
		objAuditPlanFile.setStimezoneid(getString(arg0,"stimezoneid",arg1));		
		objAuditPlanFile.setSaudittitle(getString(arg0,"saudittitle",arg1));				

		return objAuditPlanFile;
	}

}
