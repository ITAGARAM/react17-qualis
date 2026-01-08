package com.agaramtech.qualis.audittrail.model;

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
 * This entity class is used to map the fields with auditarchivalrecords table of database.
 */
@Entity
@Table(name = "auditarchivalrecords")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuditArchivalRecords extends CustomizedResultsetRowMapper<AuditArchivalRecords> implements Serializable,  RowMapper<AuditArchivalRecords> {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nauditarchivalcode")
	private long nauditarchivalcode;

	@Column(name = "dfromdate", nullable = false)
	private Instant dfromdate;
	
	@Column(name = "dtodate", nullable = false)
	private Instant dtodate;

	@Column(name = "smodulename", length = 100, nullable = false)
	private String smodulename;

	@Column(name = "sformname", length = 100, nullable = false)
	private String sformname;

	@Column(name = "suserrolename", length = 100, nullable = false)
	private String suserrolename;

	@Column(name = "susername", length = 100, nullable = false)
	private String susername;

	@Column(name = "nviewperiod", nullable = false)
	private int nviewperiod;

	@Column(name = "sauditdate", length = 255, nullable = false)
	private String sauditdate="";
	
	@Column(name = "scomments", length = 255, nullable = false)
	private String scomments="";
	
	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;
	
	@Column(name = "nmodulecode", nullable = false)
	private int nmodulecode;

	@Column(name = "nformcode", nullable = false)
	private int nformcode;
	
	@Column(name = "dmodifieddate",nullable=false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sfromdate;
	
	@Transient
	private transient String stodate;
	
	@Transient
	private transient String sviewPeriod;
	
	@Transient
	private transient int nauditarchivaltypecode;
	
	@Transient
	private transient String sauditarchivaltypename;
	
//	@Transient
//	private transient String sauditdate;
	
	
		

	@Override
	public AuditArchivalRecords mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final AuditArchivalRecords objAudit = new AuditArchivalRecords();
		
		objAudit.setNauditarchivalcode(getInteger(arg0,"nauditarchivalcode" , arg1));
		objAudit.setDfromdate(getInstant(arg0, "dfromdate", arg1));
		objAudit.setDtodate(getInstant(arg0, "dtodate", arg1));
		objAudit.setSmodulename(getString(arg0, "smodulename", arg1));
		objAudit.setSformname(getString(arg0, "sformname", arg1));
		objAudit.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objAudit.setSusername(getString(arg0, "susername", arg1));
		objAudit.setNviewperiod(getInteger(arg0,"nviewperiod" , arg1));
		objAudit.setNauditarchivaltypecode(getInteger(arg0,"nauditarchivaltypecode" , arg1));
		objAudit.setNmodulecode(getInteger(arg0,"nmodulecode" , arg1));
		objAudit.setNformcode(getInteger(arg0,"nformcode" , arg1));
		objAudit.setNuserrolecode(getInteger(arg0,"nuserrolecode" , arg1));
		objAudit.setNusercode(getInteger(arg0,"nusercode" , arg1));
		objAudit.setScomments(getString(arg0, "scomments", arg1));
		objAudit.setSfromdate(getString(arg0, "sfromdate", arg1));
		objAudit.setStodate(getString(arg0, "stodate", arg1));
		objAudit.setSviewPeriod(getString(arg0, "sviewPeriod", arg1));
		objAudit.setSauditarchivaltypename(getString(arg0, "sauditarchivaltypename", arg1));
		objAudit.setSauditdate(getString(arg0, "sauditdate", arg1));
		objAudit.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objAudit.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objAudit.setNstatus(getShort(arg0,"nstatus",arg1));
		return objAudit;
	}

}
