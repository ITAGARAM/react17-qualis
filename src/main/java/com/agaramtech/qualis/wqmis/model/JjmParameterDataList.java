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
@Table(name = "jjmparameterdatalist")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class JjmParameterDataList extends CustomizedResultsetRowMapper<JjmParameterDataList>
		implements Serializable, RowMapper<JjmParameterDataList> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "sparameterid")
	private String sparameterid;

	@Column(name = "sparametername")
	private String sparametername;

	@Column(name = "smeasurementunit")
	private String smeasurementunit;

	@Column(name = "sacceptablelimit")
	private String sacceptablelimit;

	@Column(name = "spermissiblelimit")
	private String spermissiblelimit;

	@Column(name = "svaluetype")
	private String svaluetype;

	@Column(name = "svaluetypedescription")
	private String svaluetypedescription;

	@Column(name = "npublicrate", nullable = false)
	private int npublicrate;

	@Column(name = "ndepartmentrate", nullable = false)
	private int ndepartmentrate;

	@Column(name = "ncomercialrate", nullable = false)
	private int ncomercialrate;

	@Column(name = "stestparametertype")
	private String stestparametertype;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus;

	@Override
	public JjmParameterDataList mapRow(ResultSet rs, int rowNum) throws SQLException {
		final JjmParameterDataList objjjmParameterDataList = new JjmParameterDataList();
		objjjmParameterDataList.setSparameterid(getString(rs, "sparameterid", rowNum));
		objjjmParameterDataList.setSparametername(getString(rs, "sparametername", rowNum));
		objjjmParameterDataList.setSmeasurementunit(getString(rs, "smeasurementunit", rowNum));
		objjjmParameterDataList.setSacceptablelimit(getString(rs, "sacceptablelimit", rowNum));
		objjjmParameterDataList.setSpermissiblelimit(getString(rs, "spermissiblelimit", rowNum));
		objjjmParameterDataList.setSvaluetype(getString(rs, "svaluetype", rowNum));
		objjjmParameterDataList.setSvaluetypedescription(getString(rs, "svaluetypedescription", rowNum));
		objjjmParameterDataList.setNpublicrate(getInteger(rs, "npublicrate", rowNum));
		objjjmParameterDataList.setNdepartmentrate(getInteger(rs, "ndepartmentrate", rowNum));
		objjjmParameterDataList.setNcomercialrate(getInteger(rs, "ncomercialrate", rowNum));
		objjjmParameterDataList.setStestparametertype(getString(rs, "stestparametertype", rowNum));
		objjjmParameterDataList.setNstatus(getShort(rs, "nstatus", rowNum));
		return objjjmParameterDataList;
	}
}
