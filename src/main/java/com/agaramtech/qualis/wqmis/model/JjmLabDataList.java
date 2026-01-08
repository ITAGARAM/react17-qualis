
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
@Table(name = "jjmlabdatalist")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class JjmLabDataList extends CustomizedResultsetRowMapper<JjmLabDataList>
		implements Serializable, RowMapper<JjmLabDataList> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "slabid")
	private String slabid;

	@Column(name = "slabname")
	private String slabname;

	@Column(name = "slabtype")
	private String slabtype;

	@Column(name = "slabgroup")
	private String slabgroup;

	@Column(name = "nlatitude")
	private float nlatitude;

	@Column(name = "nlongitude")
	private float nlongitude;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus;

	@Override
	public JjmLabDataList mapRow(ResultSet rs, int rowNum) throws SQLException {
		final JjmLabDataList objLabDataList = new JjmLabDataList();
		objLabDataList.setSlabid(getString(rs, "slabid", rowNum));
		objLabDataList.setSlabname(getString(rs, "slabname", rowNum));
		objLabDataList.setSlabtype(getString(rs, "slabtype", rowNum));
		objLabDataList.setSlabgroup(getString(rs, "slabgroup", rowNum));
		objLabDataList.setNlatitude(getFloat(rs, "nlatitude", rowNum));
		objLabDataList.setNlongitude(getFloat(rs, "nlongitude", rowNum));
		objLabDataList.setNstatus(getShort(rs, "nstatus", rowNum));
		return objLabDataList;
	}
}
