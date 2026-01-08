package com.agaramtech.qualis.wqmis.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
//ate199 committed by DhivyaBharathi on 21-11-2025 for jjmlabofficial
		//SWSM-122 WQMIS Branch creation for inetgartion
		 
@Entity
@Data
@Table(name = "jjmlabofficial")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class JjmLabOfficial extends CustomizedResultsetRowMapper<JjmLabOfficial>
		implements Serializable, RowMapper<JjmLabOfficial> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "user_id")
	private int user_id;
	
	@Column(name = "lab_id")
	private int lab_id;
	
	@Column(name = "first_name")
	private String first_name;
	
	@Column(name = "last_name")
	private String last_name;
	
	@Column(name = "sdesignation")
	private String sdesignation;
	
	@Column(name = "role")
	private String role;
	
	@Column(name = "mobileno")
	private String mobileno;
	
	@Column(name = "nflagstatus", nullable = false)
	@ColumnDefault("1")
	private short nflagstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "nstatus", nullable = false)
	
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public JjmLabOfficial mapRow(ResultSet arg0, int arg1) throws SQLException {
		final JjmLabOfficial objjmlabofficialdata = new JjmLabOfficial();
		 
		objjmlabofficialdata.setUser_id(getInteger(arg0, "user_id", arg1));
		objjmlabofficialdata.setLab_id(getInteger(arg0, "lab_id", arg1));
		objjmlabofficialdata.setFirst_name(StringEscapeUtils.unescapeJava(getString(arg0, "first_name", arg1)));
		objjmlabofficialdata.setLast_name(StringEscapeUtils.unescapeJava(getString(arg0, "last_name", arg1)));
		objjmlabofficialdata.setSdesignation(StringEscapeUtils.unescapeJava(getString(arg0, "sdesignation", arg1)));
		objjmlabofficialdata.setRole(StringEscapeUtils.unescapeJava(getString(arg0, "role", arg1)));
		objjmlabofficialdata.setMobileno(StringEscapeUtils.unescapeJava(getString(arg0, "mobileno", arg1)));
		objjmlabofficialdata.setNstatus(getShort(arg0, "nstatus", arg1));
		objjmlabofficialdata.setNflagstatus(getShort(arg0, "nflagstatus", arg1));
		objjmlabofficialdata.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return objjmlabofficialdata;
	}
}
