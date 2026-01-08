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
@Table(name = "wqmiscontaminatedlabsamples")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WQMISContaminatedLabSamples extends CustomizedResultsetRowMapper<WQMISContaminatedLabSamples>
		implements Serializable, RowMapper<WQMISContaminatedLabSamples> {

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
	
	@Column(name = "nlabid", nullable = false)
	private int nlabid;

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
	
	@Column(name = "ssamplelocationfrom", length = 350, nullable = false)
	private String ssamplelocationfrom;

	@Column(name = "sremedialactionstatus", length = 400, nullable = false)
	private String sremedialactionstatus;

	@Transient
	private transient long npkid;

	@Transient
	private transient String stesteddate;


	@Override
	public WQMISContaminatedLabSamples mapRow(ResultSet arg0, int arg1) throws SQLException {
		WQMISContaminatedLabSamples objContaminatedLabSamples = new WQMISContaminatedLabSamples();
		objContaminatedLabSamples.setNtestid(getInteger(arg0, "ntestid", arg1));
		objContaminatedLabSamples.setSsampleid(StringEscapeUtils.unescapeJava(getString(arg0, "ssampleid", arg1)));
		objContaminatedLabSamples.setDtesteddate(getInstant(arg0, "dtesteddate", arg1));
		objContaminatedLabSamples.setSlocation(StringEscapeUtils.unescapeJava(getString(arg0, "slocation", arg1)));
		objContaminatedLabSamples.setSissafe(StringEscapeUtils.unescapeJava(getString(arg0, "sissafe", arg1)));
		objContaminatedLabSamples.setNsourceid(getInteger(arg0, "nsourceid", arg1));
		objContaminatedLabSamples.setSsourcetype(StringEscapeUtils.unescapeJava(getString(arg0, "ssourcetype", arg1)));
		objContaminatedLabSamples
				.setSsourcesubtype(StringEscapeUtils.unescapeJava(getString(arg0, "ssourcesubtype", arg1)));
		objContaminatedLabSamples.setSschemeid(StringEscapeUtils.unescapeJava(getString(arg0, "sschemeid", arg1)));
		objContaminatedLabSamples.setNvillageid(getInteger(arg0, "nvillageid", arg1));
		objContaminatedLabSamples.setNhabitationid(getInteger(arg0, "nhabitationid", arg1));
		objContaminatedLabSamples.setNstatelgdcode(getInteger(arg0, "nstatelgdcode", arg1));
		objContaminatedLabSamples.setNdistrictlgdcode(getInteger(arg0, "ndistrictlgdcode", arg1));
		objContaminatedLabSamples.setNblocklgdcode(getInteger(arg0, "nblocklgdcode", arg1));
		objContaminatedLabSamples.setNpanchayatlgdcode(getInteger(arg0, "npanchayatlgdcode", arg1));
		objContaminatedLabSamples.setNvillagelgdcode(getInteger(arg0, "nvillagelgdcode", arg1));
		objContaminatedLabSamples.setNvillagelgdcode(getInteger(arg0, "nvillagelgdcode", arg1));
		objContaminatedLabSamples.setNpkid(getLong(arg0, "npkid", arg1));
		objContaminatedLabSamples.setStesteddate(StringEscapeUtils.unescapeJava(getString(arg0, "stesteddate", arg1)));
		objContaminatedLabSamples.setNstateid(getInteger(arg0, "nstateid", arg1));
		objContaminatedLabSamples.setNdistrictid(getInteger(arg0, "ndistrictid", arg1));
		objContaminatedLabSamples.setNblockid(getInteger(arg0, "nblockid", arg1));
		objContaminatedLabSamples.setNpanchayatid(getInteger(arg0, "npanchayatid", arg1));
		objContaminatedLabSamples.setNyear(getInteger(arg0, "nyear", arg1));
		objContaminatedLabSamples.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objContaminatedLabSamples.setNlabid(getInteger(arg0, "nlabid", arg1));
		objContaminatedLabSamples
		.setSsamplelocationfrom(StringEscapeUtils.unescapeJava(getString(arg0, "ssamplelocationfrom", arg1)));
		objContaminatedLabSamples.setSremedialactionstatus(
		StringEscapeUtils.unescapeJava(getString(arg0, "sremedialactionstatus", arg1)));

		return objContaminatedLabSamples;
	}
}
