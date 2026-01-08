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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'auditstandardcategory' table of the Database.
 */
/**
 * @author sujatha.v
 * SWSM-1
 * 19/07/2025
 */
@Entity
@Table(name = "auditstandardcategory")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuditStandardCategory extends CustomizedResultsetRowMapper<AuditStandardCategory>
implements Serializable, RowMapper<AuditStandardCategory>{
	private final static long serialVersionUID=1L;

	@Id
	@Column(name="nauditstandardcatcode")
	private int nauditstandardcatcode;

	@Column(name= "sauditstandardcatname", length= 150, nullable= false)
	private String sauditstandardcatname;
	
	//Added by sonia on 2nd Sept 2025 for jira id:SWSM-12
	@Column(name= "sauditremainderdays", length= 4, nullable= false)
	private String sauditremainderdays;

	@Column(name= "sdescription", length= 300)
	private String sdescription="";

	@Column(name= "dmodifieddate", nullable= false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name="nsitecode", nullable= false)
	private short nsitecode= (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name= "nstatus", nullable= false)
	private short nstatus= (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public AuditStandardCategory mapRow(ResultSet arg0, int arg1)throws SQLException {
		
		final AuditStandardCategory auditStandardCategory= new AuditStandardCategory();
		
		auditStandardCategory.setNauditstandardcatcode(getInteger(arg0,"nauditstandardcatcode", arg1));
		auditStandardCategory.setSauditstandardcatname(getString(arg0,"sauditstandardcatname", arg1));
		auditStandardCategory.setSauditremainderdays(getString(arg0,"sauditremainderdays", arg1)); //Added by sonia on 2nd Sept 2025 for jira id:SWSM-12
		auditStandardCategory.setSdescription(getString(arg0, "sdescription", arg1));
		auditStandardCategory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		auditStandardCategory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		auditStandardCategory.setNstatus(getShort(arg0, "nstatus", arg1));
		return auditStandardCategory;
	}
}
