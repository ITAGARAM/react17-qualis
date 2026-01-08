//package com.agaramtech.qualis.samplescheduling.model;
//
//public class SampleRequestingHistory {
//
//}
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
@Table(name = "samplerequestinghistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleRequestingHistory extends CustomizedResultsetRowMapper<SampleRequestingHistory>
		implements Serializable, RowMapper<SampleRequestingHistory> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsamplerequestinghistorycode")
	private int nsamplerequestinghistorycode;
	@Column(name = "nsamplerequestingcode", nullable = false)
	private int nsamplerequestingcode;
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
	@Column(name = "sremarks", length = 255, nullable = false)
	private String sremarks;
	
	@Column(name = "dcollectiondate", nullable = false)
	private Instant dcollectiondate;
	
	@ColumnDefault("-1")
	@Column(name = "ntzcollectiondate", nullable = false)
	private short ntzcollectiondate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "noffsetdcollectiondate", nullable = false)
	private int noffsetdcollectiondate;
	
	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;
	
	@Column(name = "noffsetdtransactiondate", nullable = false)
	private int noffsetdtransactiondate;
	
	@ColumnDefault("-1")
	@Column(name = "ntztransactiondate", nullable = false)
	private short ntztransactiondate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String scollectiondate;

	@Override
	public SampleRequestingHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		SampleRequestingHistory objSampleRequestingHistory = new SampleRequestingHistory();
		objSampleRequestingHistory.setNsamplerequestinghistorycode(getInteger(arg0, "nsamplerequestinghistorycode", arg1));
		objSampleRequestingHistory.setNsamplerequestingcode(getInteger(arg0, "nsamplerequestingcode", arg1));
		objSampleRequestingHistory.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objSampleRequestingHistory.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objSampleRequestingHistory.setNusercode(getInteger(arg0, "nusercode", arg1));
		objSampleRequestingHistory.setNdeputyusercode(getInteger(arg0, "ndeputyusercode", arg1));
		objSampleRequestingHistory.setNdeputyuserrolecode(getInteger(arg0, "ndeputyuserrolecode", arg1));
		objSampleRequestingHistory.setSremarks(getString(arg0, "sremarks", arg1));
		objSampleRequestingHistory.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objSampleRequestingHistory.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objSampleRequestingHistory.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objSampleRequestingHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleRequestingHistory.setNstatus(getShort(arg0, "nstatus", arg1));
		objSampleRequestingHistory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleRequestingHistory.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objSampleRequestingHistory.setDcollectiondate(getInstant(arg0, "dcollectiondate", arg1));
		objSampleRequestingHistory.setNoffsetdcollectiondate(getInteger(arg0, "noffsetdcollectiondate", arg1));	
		objSampleRequestingHistory.setNtzcollectiondate(getShort(arg0, "ntzcollectiondate", arg1));
		objSampleRequestingHistory.setScollectiondate(getString(arg0, "scollectiondate", arg1));
		return objSampleRequestingHistory;
	}
}
