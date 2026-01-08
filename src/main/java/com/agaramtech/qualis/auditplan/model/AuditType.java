package com.agaramtech.qualis.auditplan.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * This class is used to map the fields of 'audittype' table of the Database.
 */
/**
* @author AT-E143 SWSM-6 05/08/2025
*/
@Entity
@Table(name = "audittype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuditType extends CustomizedResultsetRowMapper<AuditType> implements Serializable,RowMapper<AuditType>{
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "naudittypecode")
	private int naudittypecode;
	
	@Column(name = "saudittypename", length=100, nullable = false)
	private String saudittypename;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	
	public AuditType mapRow(ResultSet arg0, int arg1) throws SQLException  {		
		final AuditType objAuditType = new AuditType();
		objAuditType.setNaudittypecode(getInteger(arg0,"naudittypecode",arg1));	
		objAuditType.setSaudittypename(getString(arg0,"saudittypename",arg1));	
		objAuditType.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objAuditType.setNstatus(getShort(arg0,"nstatus",arg1));
		
		return objAuditType;
	}	

}
