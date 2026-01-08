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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'ftksamplesparamters' table of the
 * Database.
 */

@Entity
@Table(name = "wqmisftksampleparameters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WQMISFTKSampleParameters extends CustomizedResultsetRowMapper<WQMISFTKSampleParameters>
		implements Serializable, RowMapper<WQMISFTKSampleParameters> {

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
	
	@Transient
	private transient String smodifieddate;

	@Override
	public WQMISFTKSampleParameters mapRow(ResultSet arg0, int arg1) throws SQLException {
		final WQMISFTKSampleParameters objtran = new WQMISFTKSampleParameters();
		objtran.setNtestid(getInteger(arg0, "ntestid", arg1));
		objtran.setSsampleid(StringEscapeUtils.unescapeJava(getString(arg0, "ssampleid", arg1)));
		objtran.setNparameterid(getInteger(arg0, "nparameterid", arg1));
		objtran.setSparametervalue(StringEscapeUtils.unescapeJava(getString(arg0, "sparametervalue", arg1)));
		objtran.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objtran.setSmodifieddate(getString(arg0,"smodifieddate",arg1));
		return objtran;
	}
}