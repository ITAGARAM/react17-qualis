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

@Entity
@Table(name = "wqmisftksamples")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WQMISFTKSamples extends CustomizedResultsetRowMapper<WQMISFTKSamples>
		implements Serializable, RowMapper<WQMISFTKSamples> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestid")
	private int ntestid;

	@Id
	@Column(name = "ssampleid", length = 75, nullable = false)
	private String ssampleid;

	@Column(name = "dtesteddate", nullable = false)
	private Instant dtesteddate;

	@Column(name = "slocation", length = 255, nullable = false)
	private String slocation;

	@Column(name = "sissafe", length = 5, nullable = false)
	private String sissafe;

	@Column(name = "nsourceid", nullable = false)
	private int nsourceid;

	@Column(name = "ssourcetype", length = 150, nullable = false)
	private String ssourcetype;

	@Column(name = "ssourcesubtype", length = 150, nullable = false)
	private String ssourcesubtype;

	@Column(name = "sschemeid", length = 350, nullable = false)
	private String sschemeid;

	@Column(name = "nvillageid", nullable = false)
	private int nvillageid;

	@Column(name = "nhabitationid", nullable = false)
	private int nhabitationid;

	@Column(name = "nstatelgdcode", nullable = false)
	private int nstatelgdcode;

	@Column(name = "ndistrictlgdcode", nullable = false)
	private int ndistrictlgdcode;

	@Column(name = "nblocklgdcode", nullable = false)
	private int nblocklgdcode;

	@Column(name = "npanchayatlgdcode", nullable = false)
	private int npanchayatlgdcode;

	@Column(name = "nvillagelgdcode", nullable = false)
	private int nvillagelgdcode;

	@Column(name = "nstateid", nullable = false)
	private int nstateid;
	
	@Column(name = "ndistrictid", nullable = false)
	private int ndistrictid;
	
	@Column(name = "nblockid", nullable = false)
	private int nblockid;
	
	@Column(name = "npanchayatid", nullable = false)
	private int npanchayatid;
	
	@Column(name = "nyear", nullable = false)
	private int nyear;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Transient
	private transient long npkid;
	
	@Transient
	private transient String stesteddate;
	
	@Transient
	private transient String smodifieddate;
	
	@Override
	public WQMISFTKSamples mapRow(ResultSet arg0, int arg1) throws SQLException {
		WQMISFTKSamples objFtkSamples = new WQMISFTKSamples();
		objFtkSamples.setNtestid(getInteger(arg0, "ntestid", arg1));
		objFtkSamples.setSsampleid(StringEscapeUtils.unescapeJava(getString(arg0, "ssampleid", arg1)));
		objFtkSamples.setDtesteddate(getInstant(arg0, "dtesteddate", arg1));
		objFtkSamples.setSlocation(StringEscapeUtils.unescapeJava(getString(arg0, "slocation", arg1)));
		objFtkSamples.setSissafe(StringEscapeUtils.unescapeJava(getString(arg0, "sissafe", arg1)));
		objFtkSamples.setNsourceid(getInteger(arg0, "nsourceid", arg1));
		objFtkSamples.setSsourcetype(StringEscapeUtils.unescapeJava(getString(arg0, "ssourcetype", arg1)));
		objFtkSamples.setSsourcesubtype(StringEscapeUtils.unescapeJava(getString(arg0, "ssourcesubtype", arg1)));
		objFtkSamples.setSschemeid(StringEscapeUtils.unescapeJava(getString(arg0, "sschemeid", arg1)));
		objFtkSamples.setNvillageid(getInteger(arg0, "nvillageid", arg1));
		objFtkSamples.setNhabitationid(getInteger(arg0, "nhabitationid", arg1));
		objFtkSamples.setNstatelgdcode(getInteger(arg0, "nstatelgdcode", arg1));
		objFtkSamples.setNdistrictlgdcode(getInteger(arg0, "ndistrictlgdcode", arg1));
		objFtkSamples.setNblocklgdcode(getInteger(arg0, "nblocklgdcode", arg1));
		objFtkSamples.setNpanchayatlgdcode(getInteger(arg0, "npanchayatlgdcode", arg1));
		objFtkSamples.setNvillagelgdcode(getInteger(arg0, "nvillagelgdcode", arg1));
		objFtkSamples.setNvillagelgdcode(getInteger(arg0, "nvillagelgdcode", arg1));
		objFtkSamples.setNpkid(getLong(arg0, "npkid", arg1));
		objFtkSamples.setStesteddate(StringEscapeUtils.unescapeJava(getString(arg0, "stesteddate", arg1)));
		objFtkSamples.setNstateid(getInteger(arg0, "nstateid", arg1));
		objFtkSamples.setNdistrictid(getInteger(arg0, "ndistrictid", arg1));
		objFtkSamples.setNblockid(getInteger(arg0, "nblockid", arg1));
		objFtkSamples.setNpanchayatid(getInteger(arg0, "npanchayatid", arg1));
		objFtkSamples.setNyear(getInteger(arg0, "nyear", arg1));
		objFtkSamples.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objFtkSamples.setSmodifieddate(getString(arg0,"smodifieddate",arg1));

		return objFtkSamples;
	}

}