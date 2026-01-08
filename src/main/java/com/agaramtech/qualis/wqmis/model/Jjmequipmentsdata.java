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

//ate135 committed by Dhanalakshmi on 21-11-2025 for Jjmequipmentsdata
//SWSM-122 WQMIS Branch creation for inetgartion
@Entity
@Table(name = "jjmequipmentsdata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Jjmequipmentsdata extends CustomizedResultsetRowMapper<Jjmequipmentsdata> implements Serializable, RowMapper<Jjmequipmentsdata> {
	private static final long serialVersionUID = 1L;
	

	@Id
	@Column(name = " e_id")
	private int  e_id;
	
	@Column(name = "equipment_name", length = 100, nullable = false)
	private String equipment_name;

	@Column(name = "nflagstatus", nullable = false)
	@ColumnDefault("1")
	private short nflagstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Override
	public Jjmequipmentsdata mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final Jjmequipmentsdata objjmequipmentdata = new Jjmequipmentsdata();

		objjmequipmentdata.setE_id(getInteger(arg0, "e_id", arg1));
		objjmequipmentdata.setEquipment_name(StringEscapeUtils.unescapeJava(getString(arg0, "equipment_name", arg1)));
		objjmequipmentdata.setNstatus(getShort(arg0, "nstatus", arg1));
		objjmequipmentdata.setNflagstatus(getShort(arg0, "nflagstatus", arg1));
		objjmequipmentdata.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
        
		return objjmequipmentdata;
		
	}
}
