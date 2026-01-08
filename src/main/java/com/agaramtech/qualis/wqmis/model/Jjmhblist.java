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
//ate135 committed by Dhanalakshmi on 21-11-2025 for GetSample_submitter
//SWSM-122 WQMIS Branch creation for inetgartion
@Entity
@Table(name = "jjmhblist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class Jjmhblist  extends CustomizedResultsetRowMapper<Jjmhblist> implements Serializable, RowMapper<Jjmhblist> {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = " habitationid")
	private int  habitationid;
	
	@Column(name = "habitationname", length = 100, nullable = false)
	private String habitationname;

	@Column(name = "nflagstatus", nullable = false)
	@ColumnDefault("1")
	private short nflagstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Override
	public Jjmhblist mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final Jjmhblist objjmhblist = new Jjmhblist();

		objjmhblist.setHabitationid(getInteger(arg0, "habitationid", arg1));
		objjmhblist.setHabitationname(StringEscapeUtils.unescapeJava(getString(arg0, "habitationname", arg1)));
		objjmhblist.setNstatus(getShort(arg0, "nstatus", arg1));
		objjmhblist.setNflagstatus(getShort(arg0, "nflagstatus", arg1));
		objjmhblist.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
        
		return objjmhblist;
		

	
	
	}
}
