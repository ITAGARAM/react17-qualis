package com.agaramtech.qualis.biobank.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'biodataaccessrequesthistory' table
 * of the Database.
 */
@Entity
@Table(name = "biodataaccessrequesthistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BiodataAccessRequestHistory extends CustomizedResultsetRowMapper<BiodataAccessRequestHistory>
		implements Serializable, RowMapper<BiodataAccessRequestHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiodataaccessrequesthistorycode", nullable = false)
	private int nbiodataaccessrequesthistorycode;

	@Id
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode;

	@Column(name = "nbiodataaccessrequestcode", nullable = false)
	private int nbiodataaccessrequestcode;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus = 8;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "ndeputyusercode", nullable = false)
	private int ndeputyusercode;

	@Column(name = "ndeputyuserrolecode", nullable = false)
	private int ndeputyuserrolecode;

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;

	@Column(name = "ntztransactiondate", nullable = false)
	private short ntztransactiondate = -1;

	@Column(name = "noffsetdtransactiondate", nullable = false)
	private int noffsetdtransactiondate = 0;

	@Column(name = "nstatus", nullable = false)
	private short nstatus = 1;

	@Override
	public BiodataAccessRequestHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		BiodataAccessRequestHistory obj = new BiodataAccessRequestHistory();

		obj.setNbiodataaccessrequesthistorycode(getInteger(arg0, "nbiodataaccessrequesthistorycode", arg1));
		obj.setNsitecode(getShort(arg0, "nsitecode", arg1));
		obj.setNbiodataaccessrequestcode(getInteger(arg0, "nbiodataaccessrequestcode", arg1));
		obj.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		obj.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		obj.setNusercode(getInteger(arg0, "nusercode", arg1));
		obj.setNdeputyusercode(getInteger(arg0, "ndeputyusercode", arg1));
		obj.setNdeputyuserrolecode(getInteger(arg0, "ndeputyuserrolecode", arg1));
		obj.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		obj.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		obj.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		obj.setNstatus(getShort(arg0, "nstatus", arg1));

		return obj;
	}

}
