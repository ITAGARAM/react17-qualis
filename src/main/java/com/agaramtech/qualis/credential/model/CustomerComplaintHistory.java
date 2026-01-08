package com.agaramtech.qualis.credential.model;

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
 * This class is used to map the fields of 'customercomplainthistory' table of
 * the Database.
 * 
 * @author Mullai Balaji.V [SWSM-9] Customer Complaints - Screen Development -
 *         Agaram Technologies
 * 
 */
@Entity
@Table(name = "customercomplainthistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CustomerComplaintHistory extends CustomizedResultsetRowMapper<CustomerComplaintHistory>
		implements Serializable, RowMapper<CustomerComplaintHistory> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ncustomercomplainthistorycode", nullable = false)
	private int ncustomercomplainthistorycode;
	@Column(name = "ncustomercomplaintcode", nullable = false)
	private int ncustomercomplaintcode;
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
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Transient
	private transient String stransdisplaystatus;

	@Override
	public CustomerComplaintHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		CustomerComplaintHistory objCustomerComplaintHistory = new CustomerComplaintHistory();
		objCustomerComplaintHistory.setNcustomercomplainthistorycode(getInteger(arg0, "ncustomercomplainthistorycode", arg1));
		objCustomerComplaintHistory.setNcustomercomplaintcode(getInteger(arg0, "ncustomercomplaintcode", arg1));
		objCustomerComplaintHistory.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objCustomerComplaintHistory.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objCustomerComplaintHistory.setNusercode(getInteger(arg0, "nusercode", arg1));
		objCustomerComplaintHistory.setNdeputyusercode(getInteger(arg0, "ndeputyusercode", arg1));
		objCustomerComplaintHistory.setNdeputyuserrolecode(getInteger(arg0, "ndeputyuserrolecode", arg1));
		objCustomerComplaintHistory.setSremarks(getString(arg0, "sremarks", arg1));
		objCustomerComplaintHistory.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objCustomerComplaintHistory.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objCustomerComplaintHistory.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objCustomerComplaintHistory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objCustomerComplaintHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objCustomerComplaintHistory.setNstatus(getShort(arg0, "nstatus", arg1));
		objCustomerComplaintHistory.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		return objCustomerComplaintHistory;
	}
}
