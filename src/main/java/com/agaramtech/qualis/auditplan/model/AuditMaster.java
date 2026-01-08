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
 * This class is used to map the fields of 'auditmaster' table of the Database.
 */
/**
* @author AT-E143 SWSM-2 22/07/2025
*/
@Entity
@Table(name = "auditmaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuditMaster extends CustomizedResultsetRowMapper<AuditMaster> implements Serializable,RowMapper<AuditMaster>{
	
private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nauditmastercode")
	private int nauditmastercode;
	
	@Column(name = "sauditorname", length=150, nullable = false)
	private String sauditorname;
	
	@ColumnDefault("4")
	@Column(name = "nleadauditor", nullable = false)
	private short nleadauditor=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
		
	@Column(name = "sdepartment", length=100, nullable = false)
	private String sdepartment;
	
	@Column(name = "semail", length=100, nullable = false)
	private String semail;
	
	@Column(name = "sphoneno", length=40, nullable = false)
	private String sphoneno;
	
	@Column(name = "sskilldetails", length=305, nullable = false)
	private String sskilldetails;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String sleadauditor;	
	
	@Transient
	private transient String smodifieddate;	
	
	public AuditMaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final AuditMaster objAuditMaster = new AuditMaster();
		objAuditMaster.setNauditmastercode(getInteger(arg0,"nauditmastercode",arg1));
		objAuditMaster.setSauditorname(StringEscapeUtils.unescapeJava(getString(arg0,"sauditorname",arg1)));
		objAuditMaster.setNleadauditor(getShort(arg0,"nleadauditor",arg1));		
		objAuditMaster.setSdepartment(StringEscapeUtils.unescapeJava(getString(arg0,"sdepartment",arg1)));
		objAuditMaster.setSemail(StringEscapeUtils.unescapeJava(getString(arg0,"semail",arg1)));
		objAuditMaster.setSphoneno(StringEscapeUtils.unescapeJava(getString(arg0,"sphoneno",arg1)));
		objAuditMaster.setSskilldetails(StringEscapeUtils.unescapeJava(getString(arg0,"sskilldetails",arg1)));	
		objAuditMaster.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objAuditMaster.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objAuditMaster.setNstatus(getShort(arg0,"nstatus",arg1));
		objAuditMaster.setSleadauditor(getString(arg0,"sleadauditor",arg1));		
		objAuditMaster.setSmodifieddate(getString(arg0,"smodifieddate",arg1));
			
		return objAuditMaster;
	}	

}
