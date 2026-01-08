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
 * This class is used to map the fields of 'auditcategory' table of the Database.
 */
/**
 * @author sujatha.v
 * SWSM-3
 * 19/07/2025
 */
@Entity
@Table(name = "auditcategory")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuditCategory  extends CustomizedResultsetRowMapper<AuditCategory>
implements Serializable, RowMapper<AuditCategory>{

	private final static long serialVersionUID=1L;

	@Id
	@Column(name="nauditcategorycode")
	private int nauditcategorycode;

	@Column(name= "sauditcategoryname", length= 150, nullable= false)
	private String sauditcategoryname;

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
	public AuditCategory mapRow(ResultSet arg0, int arg1)throws SQLException {
		
		final AuditCategory auditCategory= new AuditCategory();
		
		auditCategory.setNauditcategorycode(getInteger(arg0,"nauditcategorycode", arg1));
		auditCategory.setSauditcategoryname(getString(arg0,"sauditcategoryname", arg1));
		auditCategory.setSdescription(getString(arg0, "sdescription", arg1));
		auditCategory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		auditCategory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		auditCategory.setNstatus(getShort(arg0, "nstatus", arg1));
		return auditCategory;
	}
}
