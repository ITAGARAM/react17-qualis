package com.agaramtech.qualis.wqmis.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

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

@Entity
@Data
@Table(name = "jjmgplist")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class JjmGpList extends CustomizedResultsetRowMapper<JjmGpList> implements Serializable, RowMapper<JjmGpList> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "npanchayatid")
	private int npanchayatid;

	@Column(name = "spanchayatname")
	private String spanchayatname;

	@Column(name = "nstateid", nullable = false)
	private int stateid;

	@Column(name = "ndistrictid", nullable = false)
	private int ndistrictid;

	@Column(name = "nblockid", nullable = false)
	private int nblockid;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus;

	@Override
	public JjmGpList mapRow(ResultSet rs, int rowNum) throws SQLException {
		final JjmGpList gp = new JjmGpList();
		gp.setNpanchayatid(getInteger(rs, "npanchayatid", rowNum));
		gp.setSpanchayatname(getString(rs, "spanchayatname", rowNum));
		gp.setStateid(getInteger(rs, "nstateid", rowNum));
		gp.setNstatus(getShort(rs, "nstatus", rowNum));
		gp.setNdistrictid(getInteger(rs, "ndistrictid", rowNum));
		gp.setNblockid(getInteger(rs, "nblockid", rowNum));
		return gp;
	}
}
