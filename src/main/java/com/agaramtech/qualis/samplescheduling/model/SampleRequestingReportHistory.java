package com.agaramtech.qualis.samplescheduling.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'samplerequestingreporthistory' table
 * of the Database.
 */
@Entity
@Table(name = "samplerequestingreporthistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleRequestingReportHistory extends CustomizedResultsetRowMapper<SampleRequestingReportHistory>
		implements Serializable, RowMapper<SampleRequestingReportHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsamplerequestingreporthistorycode")
	private int nsamplerequestingreporthistorycode;

	@Column(name = "nsamplerequestingcode")
	private int nsamplerequestingcode;

	@Column(name = "nreporttypecode")
	private int nreporttypecode;

	@Column(name = "nreportdetailcode")
	private int nreportdetailcode;

	@Column(name = "nusercode")
	private int nusercode;

	@Column(name = "nuserrolecode")
	private int nuserrolecode;

	@Column(name = "ssystemfilename", length = 100, nullable = false)
	private String ssystemfilename;

	@Column(name = "dgenerateddate", nullable = false)
	private Instant dgenerateddate;

	@ColumnDefault("-1")
	@Column(name = "ntzgenerateddate", nullable = false)
	private short ntzgenerateddate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "noffsetdgenerateddate", nullable = false)
	private int noffsetdgenerateddate;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SampleRequestingReportHistory mapRow(ResultSet arg0, int arg1) throws SQLException {

		final SampleRequestingReportHistory objSampleRequestingReportHistory = new SampleRequestingReportHistory();

		objSampleRequestingReportHistory.setNsamplerequestingreporthistorycode(getInteger(arg0, "nsamplerequestingreporthistorycode", arg1));
		objSampleRequestingReportHistory.setNsamplerequestingcode(getInteger(arg0, "nsamplerequestingcode", arg1));
		objSampleRequestingReportHistory.setNreporttypecode(getInteger(arg0, "nreporttypecode", arg1));
		objSampleRequestingReportHistory.setNreportdetailcode(getInteger(arg0, "nreportdetailcode", arg1));
		objSampleRequestingReportHistory.setNusercode(getInteger(arg0, "nusercode", arg1));
		objSampleRequestingReportHistory.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objSampleRequestingReportHistory.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		objSampleRequestingReportHistory.setDgenerateddate(getInstant(arg0, "dgenerateddate", arg1));
		objSampleRequestingReportHistory.setNtzgenerateddate(getShort(arg0, "ntzgenerateddate", arg1));
		objSampleRequestingReportHistory.setNoffsetdgenerateddate(getInteger(arg0, "noffsetdgenerateddate", arg1));
		objSampleRequestingReportHistory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleRequestingReportHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleRequestingReportHistory.setNstatus(getShort(arg0, "nstatus", arg1));

		return objSampleRequestingReportHistory;
	}

}