package com.agaramtech.qualis.wqmis.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "jjmdistrictlist")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JjmDistrictList extends CustomizedResultsetRowMapper<JjmDistrictList>
		implements Serializable, RowMapper<JjmDistrictList> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndistrictid")
	private int ndistrictid;

	@Column(name = "nstateid", nullable = false)
	private int nstateid;

	@Column(name = "sdistrictname")
	private String sdistrictname;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus;

	@Override
	public JjmDistrictList mapRow(ResultSet arg0, int arg1) throws SQLException {
		final JjmDistrictList districtList = new JjmDistrictList();
		districtList.setNdistrictid(getInteger(arg0, "ndistrictid", arg1));
		districtList.setNstateid(getInteger(arg0, "nstateid", arg1));
		districtList.setSdistrictname(getString(arg0, "sdistrictname", arg1));
		districtList.setNstatus(getShort(arg0, "nstatus", arg1));
		return districtList;
	}
}
