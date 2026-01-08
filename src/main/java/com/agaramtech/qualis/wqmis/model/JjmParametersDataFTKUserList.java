
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
@Table(name = "jjmparametersdataftkuserlist")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class JjmParametersDataFTKUserList extends CustomizedResultsetRowMapper<JjmParametersDataFTKUserList>
		implements Serializable, RowMapper<JjmParametersDataFTKUserList> {

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

	@Column(name = "ndepartmentrate", nullable = false)
	private int ndepartmentrate;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus;

	@Override
	public JjmParametersDataFTKUserList mapRow(ResultSet rs, int rowNum) throws SQLException {
		final JjmParametersDataFTKUserList objjjmParametersDataFTKUserList = new JjmParametersDataFTKUserList();
		objjjmParametersDataFTKUserList.setSparameterid(getString(rs, "sparameterid", rowNum));
		objjjmParametersDataFTKUserList.setSparametername(getString(rs, "sparametername", rowNum));
		objjjmParametersDataFTKUserList.setSmeasurementunit(getString(rs, "smeasurementunit", rowNum));
		objjjmParametersDataFTKUserList.setSacceptablelimit(getString(rs, "sacceptablelimit", rowNum));
		objjjmParametersDataFTKUserList.setSpermissiblelimit(getString(rs, "spermissiblelimit", rowNum));
		objjjmParametersDataFTKUserList.setSvaluetype(getString(rs, "svaluetype", rowNum));
		objjjmParametersDataFTKUserList.setSvaluetypedescription(getString(rs, "svaluetypedescription", rowNum));
		objjjmParametersDataFTKUserList.setNdepartmentrate(getInteger(rs, "ndepartmentrate", rowNum));
		objjjmParametersDataFTKUserList.setNstatus(getShort(rs, "nstatus", rowNum));
		return objjjmParametersDataFTKUserList;
	}
}
