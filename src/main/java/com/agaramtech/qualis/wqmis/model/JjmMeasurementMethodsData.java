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
//ate199 committed by DhivyaBharathi on 21-11-2025 for jjmmeasurementmethodsdata
		//SWSM-122 WQMIS Branch creation for inetgartion
@Entity
@Data
@Table(name = "jjmmeasurementmethodsdata")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class JjmMeasurementMethodsData extends CustomizedResultsetRowMapper<JjmMeasurementMethodsData>
		implements Serializable, RowMapper<JjmMeasurementMethodsData> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "mm_id")
	private int mm_id;
	
	@Column(name = "mm_name")
	private String mm_name;
	
	@Column(name = "nflagstatus", nullable = false)
	@ColumnDefault("1")
	private short nflagstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "nstatus", nullable = false)
	
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public JjmMeasurementMethodsData mapRow(ResultSet arg0, int arg1) throws SQLException {
		final JjmMeasurementMethodsData objjmmeasurementdata = new JjmMeasurementMethodsData();
		 
		objjmmeasurementdata.setMm_id(getInteger(arg0, "mm_id", arg1));
		objjmmeasurementdata.setMm_name(StringEscapeUtils.unescapeJava(getString(arg0, "mm_name", arg1)));
		objjmmeasurementdata.setNstatus(getShort(arg0, "nstatus", arg1));
		objjmmeasurementdata.setNflagstatus(getShort(arg0, "nflagstatus", arg1));
		objjmmeasurementdata.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return objjmmeasurementdata;
	}
}