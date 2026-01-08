package com.agaramtech.qualis.wqmis.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
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
//ate225 committed by Mohammed Ashik on 21-11-2025 for jjmreagentsdata
//SWSM-122 WQMIS Branch creation for inetgartion
@Entity
@Table(name = "jjmreagentsdata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WQMISReagentsData extends CustomizedResultsetRowMapper<WQMISReagentsData>
		implements Serializable, RowMapper<WQMISReagentsData> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "reagent_id")
	private String reagent_id;

	@Column(name = "reagent_name", nullable = false)
	private String reagent_name;

	@Column(name = "nflagstatus", nullable = false)
	@ColumnDefault("0")
	private short nflagstatus;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus;

	@Override
	public WQMISReagentsData mapRow(ResultSet arg0, int arg1) throws SQLException {

		WQMISReagentsData objReagentData = new WQMISReagentsData();

		objReagentData.setReagent_id(getString(arg0, "reagent_id", arg1));
		objReagentData.setReagent_name(getString(arg0, "reagent_name", arg1));
		objReagentData.setNflagstatus(getShort(arg0, "nflagstatus", arg1));
		objReagentData.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objReagentData.setNstatus(getShort(arg0, "nstatus", arg1));

		return objReagentData;
	}

}
