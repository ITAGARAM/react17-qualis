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
@Table(name = "jjmvillagelist")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class JjmVillageList extends CustomizedResultsetRowMapper<JjmVillageList>
		implements Serializable, RowMapper<JjmVillageList> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nvillageid")
	private int nvillageid;

	@Column(name = "npanchayatid", nullable = false)
	private int npanchayatid;

	@Column(name = "svillagename")
	private String svillagename;

	@Column(name = "nstateid", nullable = false)
    private int nstateid;

	@Column(name = "nblockid", nullable = false)
	private int nblockid;

	@Column(name = "ndistrictid", nullable = false)
	private int ndistrictid;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus;

	@Override
	public JjmVillageList mapRow(ResultSet rs, int rowNum) throws SQLException {
		final JjmVillageList village = new JjmVillageList();
		village.setNvillageid(getInteger(rs, "nvillageid", rowNum));
		village.setNpanchayatid(getInteger(rs, "npanchayatid", rowNum));
		village.setSvillagename(getString(rs, "svillagename", rowNum));
        village.setNstateid(getInteger(rs, "nstateid", rowNum));
		village.setNdistrictid(getInteger(rs, "ndistrictid", rowNum));
		village.setNblockid(getInteger(rs, "nblockid", rowNum));
		village.setNstatus(getShort(rs, "nstatus", rowNum));
		return village;
	}
}
