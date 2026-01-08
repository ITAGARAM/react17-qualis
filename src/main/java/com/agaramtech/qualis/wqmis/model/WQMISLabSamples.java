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
@Table(name = "wqmislabsamples")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WQMISLabSamples extends CustomizedResultsetRowMapper<WQMISLabSamples>
		implements Serializable, RowMapper<WQMISLabSamples> {

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

	@Column(name = "ssamplelocationfrom", length = 350, nullable = false)
	private String ssamplelocationfrom;

	@Column(name = "sremedialactionstatus", length = 400, nullable = false)
	private String sremedialactionstatus;

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
	private transient String stesteddate;

	@Transient
	private transient String smodifieddate;
	
	@Transient
	private transient long npkid;

	@Override
	public WQMISLabSamples mapRow(ResultSet arg0, int arg1) throws SQLException {

		WQMISLabSamples objLabSamples = new WQMISLabSamples();

		objLabSamples.setNtestid(getInteger(arg0, "ntestid", arg1));
		objLabSamples.setSsampleid(StringEscapeUtils.unescapeJava(getString(arg0, "ssampleid", arg1)));
		objLabSamples.setDtesteddate(getInstant(arg0, "dtesteddate", arg1));
		objLabSamples.setSlocation(StringEscapeUtils.unescapeJava(getString(arg0, "slocation", arg1)));
		objLabSamples.setSissafe(StringEscapeUtils.unescapeJava(getString(arg0, "sissafe", arg1)));
		objLabSamples.setNlabid(getInteger(arg0, "nlabid", arg1));
		objLabSamples.setNsourceid(getInteger(arg0, "nsourceid", arg1));
		objLabSamples.setSsourcetype(StringEscapeUtils.unescapeJava(getString(arg0, "ssourcetype", arg1)));
		objLabSamples.setSsourcesubtype(StringEscapeUtils.unescapeJava(getString(arg0, "ssourcesubtype", arg1)));
		objLabSamples.setSschemeid(StringEscapeUtils.unescapeJava(getString(arg0, "sschemeid", arg1)));
		objLabSamples.setSsamplelocationfrom(StringEscapeUtils.unescapeJava(getString(arg0, "ssamplelocationfrom", arg1)));
		objLabSamples.setSremedialactionstatus(StringEscapeUtils.unescapeJava(getString(arg0, "sremedialactionstatus", arg1)));
		objLabSamples.setNvillageid(getInteger(arg0, "nvillageid", arg1));
		objLabSamples.setNhabitationid(getInteger(arg0, "nhabitationid", arg1));
		objLabSamples.setNstatelgdcode(getInteger(arg0, "nstatelgdcode", arg1));
		objLabSamples.setNdistrictlgdcode(getInteger(arg0, "ndistrictlgdcode", arg1));
		objLabSamples.setNblocklgdcode(getInteger(arg0, "nblocklgdcode", arg1));
		objLabSamples.setNpanchayatlgdcode(getInteger(arg0, "npanchayatlgdcode", arg1));
		objLabSamples.setNvillagelgdcode(getInteger(arg0, "nvillagelgdcode", arg1));
		objLabSamples.setStesteddate(StringEscapeUtils.unescapeJava(getString(arg0, "stesteddate", arg1)));
		objLabSamples.setNpkid(getLong(arg0, "npkid", arg1));
		objLabSamples.setNstateid(getInteger(arg0, "nstateid", arg1));
		objLabSamples.setNdistrictid(getInteger(arg0, "ndistrictid", arg1));
		objLabSamples.setNblockid(getInteger(arg0, "nblockid", arg1));
		objLabSamples.setNpanchayatid(getInteger(arg0, "npanchayatid", arg1));
		objLabSamples.setNyear(getInteger(arg0, "nyear", arg1));
		objLabSamples.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objLabSamples.setSmodifieddate(getString(arg0,"smodifieddate",arg1));


		return objLabSamples;
	}

}