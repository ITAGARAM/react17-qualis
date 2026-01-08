package com.agaramtech.qualis.submitter.model;

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
 * @author sujatha.v SWSM-4 22/07/2025
 */
@Entity
@Table(name = "villages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Villages extends CustomizedResultsetRowMapper<Villages> implements Serializable, RowMapper<Villages>{

    private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nvillagecode")
	private int nvillagecode;

	@Column(name = "svillagename", length = 150, nullable = false)
	private String svillagename;

	// Modified by sujatha ATE_274 length from 10 to 30
	@Column(name = "svillagecode", length = 30, nullable = false)
	private String svillagecode;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	// added by sujatha ATE_274 whose value is from the sitehierarchyconfig
	@Column(name = "nnodesitecode")
	private int nnodesitecode;
	
	// added by sujatha ATE_274 whose value is from the sitehierarchyconfig
	@Column(name = "nsitehierarchyconfigcode", nullable = false)
	private int nsitehierarchyconfigcode;

	@Transient
	private transient String scityname;

	@Transient
	private transient String smodifieddate;
	
	// added by sujatha ATE_274 for getting siteconfigname from the json data of sitehierarchyconfig
	@Transient
	private transient String siteconfigname;
	
	@Transient
	private transient String ntransactionstatus;
	
	@Override
	public Villages mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Villages objVillages = new Villages();
		objVillages.setNvillagecode(getInteger(arg0, "nvillagecode", arg1));
		objVillages.setSvillagename(StringEscapeUtils.unescapeJava(getString(arg0, "svillagename", arg1)));
		objVillages.setSvillagecode(StringEscapeUtils.unescapeJava(getString(arg0, "svillagecode", arg1)));
		objVillages.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objVillages.setNstatus(getShort(arg0, "nstatus", arg1));
		objVillages.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objVillages.setScityname(getString(arg0, "scityname", arg1));
		objVillages.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		// added by sujatha ATE_274 whose value is from the sitehierarchyconfig
		objVillages.setSiteconfigname(getString(arg0, "siteconfigname", arg1));
		objVillages.setNtransactionstatus(getString(arg0, "ntransactionstatus", arg1));
		objVillages.setNsitehierarchyconfigcode(getInteger(arg0, "nsitehierarchyconfigcode", arg1));
		objVillages.setNnodesitecode(getInteger(arg0, "nnodesitecode", arg1));

		return objVillages;
	}
}
