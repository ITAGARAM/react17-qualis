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
//ate225 committed by Mohammed Ashik on 21-11-2025 for statelevelmapping table
//SWSM-122 WQMIS Branch creation for inetgartion
@Entity
@Table(name = "statelevelmapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WQMISStateLevelMapping extends CustomizedResultsetRowMapper<WQMISStateLevelMapping>
implements Serializable, RowMapper<WQMISStateLevelMapping> {
	

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "lab_id")
	private String lab_id;

	@Column(name = "parameter_id", nullable = false)
	private String parameter_id;
	
	@Column(name = "method_id", nullable = false)
	private String method_id;
	
	@Column(name = "equipment_id", nullable = false)
	private String equipment_id;
	
	@Column(name = "reagent_id", nullable = false)
	private String reagent_id;

	@Column(name = "nflagstatusmethod", nullable = false)
	@ColumnDefault("0")
	private short nflagstatusmethod;
	
	@Column(name = "nflagstatusequipment", nullable = false)
	@ColumnDefault("0")
	private short nflagstatusequipment;
	
	@Column(name = "nflagstatusreagent", nullable = false)
	@ColumnDefault("0")
	private short nflagstatusreagent;
	
	@Column(name = "nflagstatus", nullable = false)
	@ColumnDefault("0")
	private short nflagstatus;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus;

	@Override
	public WQMISStateLevelMapping mapRow(ResultSet arg0, int arg1) throws SQLException {

		WQMISStateLevelMapping objStateLevelMapping = new WQMISStateLevelMapping();

		objStateLevelMapping.setLab_id(getString(arg0, "lab_id", arg1));
		objStateLevelMapping.setParameter_id(getString(arg0, "parameter_id", arg1));
		objStateLevelMapping.setMethod_id(getString(arg0, "method_id", arg1));
		objStateLevelMapping.setEquipment_id(getString(arg0, "equipment_id", arg1));
		objStateLevelMapping.setReagent_id(getString(arg0, "reagent_id", arg1));
		objStateLevelMapping.setNflagstatusmethod(getShort(arg0, "nflagstatusmethod", arg1));
		objStateLevelMapping.setNflagstatusequipment(getShort(arg0, "nflagstatusequipment", arg1));
		objStateLevelMapping.setNflagstatusreagent(getShort(arg0, "nflagstatusreagent", arg1));
		objStateLevelMapping.setNflagstatus(getShort(arg0, "nflagstatus", arg1));
		objStateLevelMapping.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objStateLevelMapping.setNstatus(getShort(arg0, "nstatus", arg1));

		return objStateLevelMapping;
	}

}
