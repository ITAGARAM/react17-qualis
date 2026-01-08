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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'sampleschedulinghistory' table of the
 * Database.
 * 
 * @author Mullai Balaji.V [SWSM-17] Sample Scheduling - Screen Development -
 *         Agaram Technologies
 * 
 */
@Entity
@Table(name = "sampleschedulinghistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleSchedulingHistory extends CustomizedResultsetRowMapper<SampleSchedulingHistory>
		implements Serializable, RowMapper<SampleSchedulingHistory> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsampleschedulinghistorycode", nullable = false)
	private int nsampleschedulinghistorycode;
	@Column(name = "nsampleschedulingcode", nullable = false)
	private int nsampleschedulingcode;
	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;
	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;
	@Column(name = "nusercode", nullable = false)
	private int nusercode;
	@Column(name = "ndeputyusercode", nullable = false)
	private int ndeputyusercode;
	@Column(name = "ndeputyuserrolecode", nullable = false)
	private int ndeputyuserrolecode;
	@Column(name = "sremarks", length = 150)
	private String sremarks = "";
	@Column(name = "dtransactiondate")
	private Instant dtransactiondate;
	@Column(name = "noffsetdtransactiondate", nullable = false)
	private int noffsetdtransactiondate;
	@ColumnDefault("-1")
	@Column(name = "ntztransactiondate", nullable = false)
	private short ntztransactiondate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Transient
	private transient String stransdisplaystatus;

	@Override
	public SampleSchedulingHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		SampleSchedulingHistory objSampleSchedulingHistory = new SampleSchedulingHistory();
		objSampleSchedulingHistory.setNsampleschedulinghistorycode(getInteger(arg0, "nsampleschedulinghistorycode", arg1));
		objSampleSchedulingHistory.setNsampleschedulingcode(getInteger(arg0, "nsampleschedulingcode", arg1));
		objSampleSchedulingHistory.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objSampleSchedulingHistory.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objSampleSchedulingHistory.setNusercode(getInteger(arg0, "nusercode", arg1));
		objSampleSchedulingHistory.setNdeputyusercode(getInteger(arg0, "ndeputyusercode", arg1));
		objSampleSchedulingHistory.setNdeputyuserrolecode(getInteger(arg0, "ndeputyuserrolecode", arg1));
		objSampleSchedulingHistory.setSremarks(getString(arg0, "sremarks", arg1));
		objSampleSchedulingHistory.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objSampleSchedulingHistory.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objSampleSchedulingHistory.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objSampleSchedulingHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleSchedulingHistory.setNstatus(getShort(arg0, "nstatus", arg1));
		objSampleSchedulingHistory.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		return objSampleSchedulingHistory;
	}
}
