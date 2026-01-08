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
//ate225 committed by Mohammed Ashik on 21-11-2025 for jjmdwsmstatemembersecretary
//SWSM-122 WQMIS Branch creation for inetgartion
@Entity
@Table(name = "jjmdwsmstatemembersecretary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WQMISStateMemberSecretary extends CustomizedResultsetRowMapper<WQMISStateMemberSecretary>
		implements Serializable, RowMapper<WQMISStateMemberSecretary> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "user_id")
	private int user_id;

	@Column(name = "first_name", nullable = false)
	private String first_name;

	@Column(name = "last_name", nullable = false)
	private String last_name;

	@Column(name = "mobile", length = 50, nullable = false)
	private String mobile;

	@Column(name = "email", length = 50, nullable = false)
	private String email;

	@Column(name = "state", length = 50, nullable = false)
	private String state;

	@Column(name = "district", nullable = false)
	private String district;

	@Column(name = "nflagstatus", nullable = false)
	@ColumnDefault("0")
	private short nflagstatus;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus;

	@Override
	public WQMISStateMemberSecretary mapRow(ResultSet arg0, int arg1) throws SQLException {

		WQMISStateMemberSecretary objStateMemberSecretary = new WQMISStateMemberSecretary();

		objStateMemberSecretary.setUser_id(getInteger(arg0, "user_id", arg1));
		objStateMemberSecretary.setFirst_name(getString(arg0, "first_name", arg1));
		objStateMemberSecretary.setLast_name(getString(arg0, "last_name", arg1));
		objStateMemberSecretary.setMobile(getString(arg0, "mobile", arg1));
		objStateMemberSecretary.setEmail(getString(arg0, "email", arg1));
		objStateMemberSecretary.setState(getString(arg0, "state", arg1));
		objStateMemberSecretary.setDistrict(getString(arg0, "district", arg1));
		objStateMemberSecretary.setNflagstatus(getShort(arg0, "nflagstatus", arg1));
		objStateMemberSecretary.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objStateMemberSecretary.setNstatus(getShort(arg0, "nstatus", arg1));

		return objStateMemberSecretary;
	}

}
