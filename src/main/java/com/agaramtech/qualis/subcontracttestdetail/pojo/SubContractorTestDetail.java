package com.agaramtech.qualis.subcontracttestdetail.pojo;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "subcontractortestdetail")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SubContractorTestDetail extends CustomizedResultsetRowMapper<SubContractorTestDetail>
		implements Serializable, RowMapper<SubContractorTestDetail> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsubcontractortestdetailcode", nullable = false)
	private int nsubcontractortestdetailcode;
	@Column(name = "npreregno", nullable = false)
	private int npreregno;
	@Column(name = "ntransactionsamplecode", nullable = false)
	private int ntransactionsamplecode;
	@Column(name = "ntransactiontestcode", nullable = false)
	private int ntransactiontestcode;
	@Column(name = "ntestcode", nullable = false)
	private int ntestcode;
	@Column(name = "ntestsubcontractorcode", nullable = false)
	private int ntestsubcontractorcode;
	@Column(name = "nsuppliercode", nullable = false)
	private int nsuppliercode;
	@Column(name = "ncontrolleadtime", nullable = false)
	private short ncontrolleadtime;
	@Column(name = "nperiodcode", nullable = false)
	private short nperiodcode;
	@Column(name = "nlinkcode", nullable = false)
	private int nlinkcode;
	@Column(name = "nattachmenttypecode", nullable = false)
	private int nattachmenttypecode;
	@Column(name = "nfilesize", nullable = false)
	private int nfilesize;
	@Column(name = "sfilename", length = 100)
	private String sfilename;
	@Column(name = "ssystemfilename", length = 100)
	private String ssystemfilename;
	@Column(name = "sremarks", length = 255)
	private String sremarks;
	@Lob
	@Column(name = "jsontest", columnDefinition = "jsonb")
	private Map<String, Object> jsontest;
	@Column(name = "dexpecteddate")
	private Instant dexpecteddate;
	@Column(name = "dcontrolleaddate")
	private Instant dcontrolleaddate;
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@Column(name = "dfileupdateddate")
	private Instant dfileupdateddate;
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode;
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus;

	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String sarno;
	@Transient
	private transient String ssamplearno;
	@Transient
	private transient String stestsynonym;
	@Transient
	private transient String ssourcesitename;
	@Transient
	private transient String ssuppliername;
	@Transient
	private transient String sexpecteddate;
	@Transient
	private transient String scontrolleaddate;
	@Transient
	private transient short ntransactionstatus;
	@Transient
	private transient String slinkname;
	@Transient
	private transient String stestname;
	@Transient
	@Lob
	private transient Map<String, Object> jsonsample;
	@Transient
	private transient Map<String, Object> jsonsubsample;

	@Override
	public SubContractorTestDetail mapRow(ResultSet arg0, int arg1) throws SQLException {

		final SubContractorTestDetail objSubContractorTestDetail = new SubContractorTestDetail();

		objSubContractorTestDetail
				.setNsubcontractortestdetailcode(getInteger(arg0, "nsubcontractortestdetailcode", arg1));
		objSubContractorTestDetail.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objSubContractorTestDetail.setNtransactionsamplecode(getInteger(arg0, "ntransactionsamplecode", arg1));
		objSubContractorTestDetail.setNtransactiontestcode(getInteger(arg0, "ntransactiontestcode", arg1));
		objSubContractorTestDetail.setNtestcode(getInteger(arg0, "ntestcode", arg1));
		objSubContractorTestDetail.setNtestsubcontractorcode(getInteger(arg0, "ntestsubcontractorcode", arg1));
		objSubContractorTestDetail.setNsuppliercode(getInteger(arg0, "nsuppliercode", arg1));
		objSubContractorTestDetail.setNcontrolleadtime(getShort(arg0, "ncontrolleadtime", arg1));
		objSubContractorTestDetail.setNperiodcode(getShort(arg0, "nperiodcode", arg1));
		objSubContractorTestDetail.setNlinkcode(getInteger(arg0, "nlinkcode", arg1));
		objSubContractorTestDetail.setNattachmenttypecode(getInteger(arg0, "nattachmenttypecode", arg1));
		objSubContractorTestDetail.setNfilesize(getInteger(arg0, "nfilesize", arg1));
		objSubContractorTestDetail.setSfilename(getString(arg0, "sfilename", arg1));
		objSubContractorTestDetail.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		objSubContractorTestDetail.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objSubContractorTestDetail.setSarno(getString(arg0, "sarno", arg1));
		// objSubContractorTestDetail.setSsamplearno(getString(arg0, "ssampleid",
		// arg1));
		objSubContractorTestDetail.setSremarks(getString(arg0, "sremarks", arg1));
		objSubContractorTestDetail.setJsontest(getJsonObject(arg0, "jsontest", arg1));
		objSubContractorTestDetail.setDexpecteddate(getInstant(arg0, "dexpecteddate", arg1));
		objSubContractorTestDetail.setDcontrolleaddate(getInstant(arg0, "dcontrolleaddate", arg1));
		objSubContractorTestDetail.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSubContractorTestDetail.setDfileupdateddate(getInstant(arg0, "dfileupdateddate", arg1));
		objSubContractorTestDetail.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSubContractorTestDetail.setNstatus(getShort(arg0, "nstatus", arg1));
		objSubContractorTestDetail.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objSubContractorTestDetail.setStestsynonym(getString(arg0, "stestsynonym", arg1));
		objSubContractorTestDetail.setSsourcesitename(getString(arg0, "ssourcesitename", arg1));
		objSubContractorTestDetail.setSsuppliername(getString(arg0, "ssuppliername", arg1));
		objSubContractorTestDetail.setSexpecteddate(getString(arg0, "sexpecteddate", arg1));
		objSubContractorTestDetail.setScontrolleaddate(getString(arg0, "scontrolleaddate", arg1));
		objSubContractorTestDetail.setJsonsample(getJsonObject(arg0, "jsonsample", arg1));
		objSubContractorTestDetail.setJsonsubsample(getJsonObject(arg0, "jsonsubsample", arg1));
		objSubContractorTestDetail.setSlinkname(getString(arg0, "slinkname", arg1));
		objSubContractorTestDetail.setStestname(getString(arg0, "stestname", arg1));
		objSubContractorTestDetail.setSsamplearno(getString(arg0, "ssamplearno", arg1));

		return objSubContractorTestDetail;
	}

}
