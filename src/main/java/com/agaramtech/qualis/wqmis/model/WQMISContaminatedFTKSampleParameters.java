package com.agaramtech.qualis.wqmis.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
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
@Table(name = "wqmiscontaminatedftksampleparameters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WQMISContaminatedFTKSampleParameters extends CustomizedResultsetRowMapper<WQMISContaminatedFTKSampleParameters>
		implements Serializable, RowMapper<WQMISContaminatedFTKSampleParameters> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestid")
	private int ntestid;

	@Id
	@Column(name = "ssampleid", length = 75, nullable = false)
	private String ssampleid;

	@Id
	@Column(name = "nparameterid")
	private int nparameterid;

	@Column(name = "sparametervalue", length = 255, nullable = false)
	private String sparametervalue;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Override
	public WQMISContaminatedFTKSampleParameters mapRow(ResultSet arg0, int arg1) throws SQLException {
		final WQMISContaminatedFTKSampleParameters objContaminatedFTKSampleParameters = new WQMISContaminatedFTKSampleParameters();
		objContaminatedFTKSampleParameters.setNtestid(getInteger(arg0, "ntestid", arg1));
		objContaminatedFTKSampleParameters
				.setSsampleid(StringEscapeUtils.unescapeJava(getString(arg0, "ssampleid", arg1)));
		objContaminatedFTKSampleParameters.setNparameterid(getInteger(arg0, "nparameterid", arg1));
		objContaminatedFTKSampleParameters
				.setSparametervalue(StringEscapeUtils.unescapeJava(getString(arg0, "sparametervalue", arg1)));
		objContaminatedFTKSampleParameters.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));

		return objContaminatedFTKSampleParameters;
	}
}
