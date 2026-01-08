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

//ate135 committed by Dhanalakshmi on 21-11-2025 for jjmsamplesubmitter
		//SWSM-122 WQMIS Branch creation for inetgartion
@Entity
@Table(name = "jjmsamplesubmitter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Jjmsamplesubmitter extends CustomizedResultsetRowMapper<Jjmsamplesubmitter> implements Serializable, RowMapper<Jjmsamplesubmitter> {
	private static final long serialVersionUID = 1L; 
		
		@Id
		@Column(name = " user_id")
		private int  user_id;
		
		@Column(name = "first_name", length = 50, nullable = false)
		private String first_name;
		
		@Column(name = "last_name", length = 50, nullable = false)
		private String last_name;
		
		@Column(name = "user_type", length = 50, nullable = false)
		private String user_type;
		
		@Column(name = "mobileno", length = 50, nullable = false)
		private String mobile;
		
		@Column(name = "email", length = 50, nullable = false)
		private String email;
		
		@Column(name = "state", length = 25, nullable = false)
		private String state;
		
		@Column(name = "district", length = 25, nullable = false)
		private String district;
		
		@Column(name = "block", length = 25, nullable = false)
		private String block;
		
		@Column(name = "gp", length = 25, nullable = false)
		private String gp;
		
		@Column(name = "village_town", length = 25, nullable = false)
		private String village_town;
		
		
		@Column(name = "area", length = 25, nullable = false)
		private String area;
		
		@Column(name = "house_no", length = 25, nullable = false)
		private String house_no;
		
		@Column(name = "pin_code", length = 25, nullable = false)
		private String pin_code;
		
		@Column(name = "nflagstatus", nullable = false)
		@ColumnDefault("0")
		private short nflagstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		@Column(name = "dmodifieddate", nullable = false)
		private Instant dmodifieddate;
		
		@Column(name = "nstatus", nullable = false)
		@ColumnDefault("1")
		private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
	@Override
	public Jjmsamplesubmitter mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final Jjmsamplesubmitter objjmsamplesubmitter = new Jjmsamplesubmitter();

		objjmsamplesubmitter.setUser_id(getInteger(arg0, "user_id", arg1));
		objjmsamplesubmitter.setFirst_name(StringEscapeUtils.unescapeJava(getString(arg0, "first_name", arg1)));
		objjmsamplesubmitter.setLast_name(StringEscapeUtils.unescapeJava(getString(arg0, "last_name", arg1)));
		objjmsamplesubmitter.setUser_type(StringEscapeUtils.unescapeJava(getString(arg0, "user_type", arg1)));
		
		objjmsamplesubmitter.setMobile(StringEscapeUtils.unescapeJava(getString(arg0, "mobileno", arg1)));
		objjmsamplesubmitter.setEmail(StringEscapeUtils.unescapeJava(getString(arg0, "email", arg1)));
		objjmsamplesubmitter.setState(StringEscapeUtils.unescapeJava(getString(arg0, "state", arg1)));
		
		objjmsamplesubmitter.setDistrict(StringEscapeUtils.unescapeJava(getString(arg0, "district", arg1)));
		objjmsamplesubmitter.setBlock(StringEscapeUtils.unescapeJava(getString(arg0, "block", arg1)));
		objjmsamplesubmitter.setGp(StringEscapeUtils.unescapeJava(getString(arg0, "gp", arg1)));
		
		
		objjmsamplesubmitter.setVillage_town(StringEscapeUtils.unescapeJava(getString(arg0, "village_town", arg1)));
		objjmsamplesubmitter.setArea(StringEscapeUtils.unescapeJava(getString(arg0, "area", arg1)));
		objjmsamplesubmitter.setHouse_no(StringEscapeUtils.unescapeJava(getString(arg0, "house_no", arg1)));
		objjmsamplesubmitter.setPin_code(StringEscapeUtils.unescapeJava(getString(arg0, "pin_code", arg1)));

		objjmsamplesubmitter.setNstatus(getShort(arg0, "nstatus", arg1));
		objjmsamplesubmitter.setNflagstatus(getShort(arg0, "nflagstatus", arg1));
		objjmsamplesubmitter.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));

		return objjmsamplesubmitter;
		
		
	}
	
	
	
 
}
