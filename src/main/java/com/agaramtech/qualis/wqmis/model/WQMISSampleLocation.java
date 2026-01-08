package com.agaramtech.qualis.wqmis.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
//ate225 committed by Mohammed Ashik on 21-11-2025 for jjmsamplelocation
//SWSM-122 WQMIS Branch creation for inetgartion
@Entity
@Table(name = "jjmsamplelocation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WQMISSampleLocation extends CustomizedResultsetRowMapper<WQMISSampleLocation>
		implements Serializable, RowMapper<WQMISSampleLocation> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "typeid")
	private int typeid;

	@Column(name = "typename", length = 50, nullable = false)
	private String typename;

	@Column(name = "nflagstatus", nullable = false)
	@ColumnDefault("0")
	private short nflagstatus;

	@Column(name = "description", nullable = false)
	private String description;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus;

	@Override
	public WQMISSampleLocation mapRow(ResultSet arg0, int arg1) throws SQLException {

		WQMISSampleLocation objSampleLocation = new WQMISSampleLocation();

		objSampleLocation.setTypeid(getInteger(arg0, "typeid", arg1));
		objSampleLocation.setTypename(getString(arg0, "typename", arg1));
		objSampleLocation.setNflagstatus(getShort(arg0, "nflagstatus", arg1));
		objSampleLocation.setDescription(getString(arg0, "description", arg1));
		;
		objSampleLocation.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleLocation.setNstatus(getShort(arg0, "nstatus", arg1));

		return objSampleLocation;
	}

}
