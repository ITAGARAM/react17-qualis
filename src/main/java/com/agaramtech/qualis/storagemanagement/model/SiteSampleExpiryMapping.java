package com.agaramtech.qualis.storagemanagement.model;

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

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "siteexpirymapping")
@Data

/**
 * This class is used to perform CRUD Operation on "siteexpirymapping" table by 
 * implementing methods from its interface. 
 */
/**
 * @author sujatha.v SWSM-14 31/08/2025
 */
public class SiteSampleExpiryMapping extends CustomizedResultsetRowMapper<SiteSampleExpiryMapping>
		implements Serializable, RowMapper<SiteSampleExpiryMapping> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nsiteexpirymappingcode")
	private int nsiteexpirymappingcode;
	
	@Column(name = "sexpirydays", nullable = false)
	private String sexpirydays;
	
	@Column(name = "nperiodcode", nullable = false)
	private short nperiodcode;
	
	@Column(name = "nsitemastercode", nullable = false)
	private int nsitemastercode;
	
	@Column(name= "sdescription", length= 300)
	private String sdescription="";
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String speriodname;
	
	@Transient
	private transient String ssitename;
	
	@Transient
	private transient String smodifieddate;

	@Override
	public SiteSampleExpiryMapping mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SiteSampleExpiryMapping siteSampleExpiryMapping=new SiteSampleExpiryMapping();
		siteSampleExpiryMapping.setNsiteexpirymappingcode(getInteger(arg0, "nsiteexpirymappingcode", arg1));
		siteSampleExpiryMapping.setSexpirydays(getString(arg0, "sexpirydays", arg1));
		siteSampleExpiryMapping.setNperiodcode(getShort(arg0, "nperiodcode", arg1));
		siteSampleExpiryMapping.setNsitemastercode(getInteger(arg0, "nsitemastercode", arg1));
		siteSampleExpiryMapping.setSdescription(getString(arg0, "sdescription", arg1));
		siteSampleExpiryMapping.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		siteSampleExpiryMapping.setNsitecode(getShort(arg0, "nsitecode", arg1));
		siteSampleExpiryMapping.setNstatus(getShort(arg0, "nstatus", arg1));
		siteSampleExpiryMapping.setSperiodname(getString(arg0, "speriodname", arg1));
		siteSampleExpiryMapping.setSsitename(getString(arg0, "ssitename", arg1));
		siteSampleExpiryMapping.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		return siteSampleExpiryMapping;
	}
}
