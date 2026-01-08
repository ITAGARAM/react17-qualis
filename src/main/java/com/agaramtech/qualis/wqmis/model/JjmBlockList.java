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
@Table(name = "jjmblocklist")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class JjmBlockList extends CustomizedResultsetRowMapper<JjmBlockList>
		implements Serializable, RowMapper<JjmBlockList> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nblockid")
	private int nblockid;

	@Column(name = "ndistrictid", nullable = false)
	private int ndistrictid;

	@Column(name = "sblockname")
	private String sblockname;

	@Column(name = "nstateid", nullable = false)
	private int stateid;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus;

	@Override
	public JjmBlockList mapRow(ResultSet rs, int rowNum) throws SQLException {
		final JjmBlockList block = new JjmBlockList();
		block.setNblockid(getInteger(rs, "nblockid", rowNum));
		block.setNdistrictid(getInteger(rs, "ndistrictid", rowNum));
		block.setSblockname(getString(rs, "sblockname", rowNum));
		block.setStateid(getInteger(rs, "nstateid", rowNum));
		block.setNstatus(getShort(rs, "nstatus", rowNum));
		return block;
	}
}
