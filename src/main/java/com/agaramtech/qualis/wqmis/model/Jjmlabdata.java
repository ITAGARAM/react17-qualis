package com.agaramtech.qualis.wqmis.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
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


//ate135 committed by Dhanalakshmi on 21-11-2025 for jjmlabdata
	//SWSM-122 WQMIS Branch creation for inetgartion
@Entity
@Table(name = "jjmlabdata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Jjmlabdata extends CustomizedResultsetRowMapper<Jjmlabdata> implements Serializable, RowMapper<Jjmlabdata> {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = " lab_id")
	private int  lab_id;
	
	@Column(name = "lab_name", length = 100, nullable = false)
	private String lab_name;
	
	@Column(name = "lab_type", length = 10, nullable = false)
	private String lab_type;

	@Column(name = "lab_group", length = 50, nullable = false)
	private String lab_group;
	
	@Column(name = "latitude")
	private Double latitude;
 
	@Column(name = "longitude")
	private Double longitude;
	
	@Column(name = "nflagstatus", nullable = false)
	@ColumnDefault("0")
	private short nflagstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Override
	public Jjmlabdata mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final Jjmlabdata objjmlabdata = new Jjmlabdata();

		objjmlabdata.setLab_id(getInteger(arg0, "lab_id", arg1));
		objjmlabdata.setLab_name(StringEscapeUtils.unescapeJava(getString(arg0, "lab_name", arg1)));
		objjmlabdata.setLab_type(StringEscapeUtils.unescapeJava(getString(arg0, "lab_type", arg1)));
		objjmlabdata.setLab_group(StringEscapeUtils.unescapeJava(getString(arg0, "lab_type", arg1)));
	    objjmlabdata.setNstatus(getShort(arg0, "nstatus", arg1));
	    objjmlabdata.setNflagstatus(getShort(arg0, "nflagstatus", arg1));
		objjmlabdata.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
        objjmlabdata.setLatitude(getDouble(arg0, "latitude", arg1));
		objjmlabdata.setLongitude(getDouble(arg0, "longitude", arg1));
		return objjmlabdata;
		
		
	}

   
}
