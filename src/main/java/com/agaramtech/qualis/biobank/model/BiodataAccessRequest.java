package com.agaramtech.qualis.biobank.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'biodataaccessrequest' table of the
 * Database.
 */
@Entity
@Table(name = "biodataaccessrequest")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BiodataAccessRequest extends CustomizedResultsetRowMapper<BiodataAccessRequest>
		implements Serializable, RowMapper<BiodataAccessRequest> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiodataaccessrequestcode", nullable = false)
	private int nbiodataaccessrequestcode;

	@Id
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode;

	@Column(name = "nthirdpartycode", nullable = false)
	private int nthirdpartycode = -1;

	@Column(name = "sformnumber", length = 50)
	private String sformnumber;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus = 8;

	@Column(name = "drequestcreateddate", nullable = false)
	private Instant drequestcreateddate;

	@Column(name = "ntzrequestcreateddate", nullable = false)
	private short ntzrequestcreateddate = -1;

	@Column(name = "noffsetdrequestcreateddate", nullable = false)
	private int noffsetdrequestcreateddate = 0;

	@Column(name = "drequesteddate", nullable = false)
	private Instant drequesteddate;

	@Column(name = "ntzrequesteddate", nullable = false)
	private short ntzrequesteddate = -1;

	@Column(name = "noffsetdrequesteddate", nullable = false)
	private int noffsetdrequesteddate = 0;

	@Column(name = "nstatus", nullable = false)
	private short nstatus = 1;

	@Transient
	private transient String srequestdate;
	@Transient
	private transient String sreceiversitename;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String stransactionstatus;
	@Transient
	private transient int nbioprojectcode;
	@Transient
	private transient int nproductcode;
	@Transient
	private transient String scolorhexcode;
	@Transient
	private String sprojecttitle;
	@Transient
	private String sproductname;

	@Override
	public BiodataAccessRequest mapRow(ResultSet arg0, int arg1) throws SQLException {
		BiodataAccessRequest obj = new BiodataAccessRequest();

		obj.setNbiodataaccessrequestcode(getInteger(arg0, "nbiodataaccessrequestcode", arg1));
		obj.setNsitecode(getShort(arg0, "nsitecode", arg1));
		obj.setNthirdpartycode(getInteger(arg0, "nthirdpartycode", arg1));
		obj.setSformnumber(getString(arg0, "sformnumber", arg1));
		obj.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		obj.setDrequestcreateddate(getInstant(arg0, "drequestcreateddate", arg1));
		obj.setNtzrequestcreateddate(getShort(arg0, "ntzrequestcreateddate", arg1));
		obj.setNoffsetdrequestcreateddate(getInteger(arg0, "noffsetdrequestcreateddate", arg1));
		obj.setDrequesteddate(getInstant(arg0, "drequesteddate", arg1));
		obj.setNtzrequesteddate(getShort(arg0, "ntzrequesteddate", arg1));
		obj.setNoffsetdrequesteddate(getInteger(arg0, "noffsetdrequesteddate", arg1));
		obj.setStransactionstatus(getString(arg0, "stransactionstatus", arg1));
		obj.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		obj.setNstatus(getShort(arg0, "nstatus", arg1));
		obj.setSrequestdate(getString(arg0, "srequestdate", arg1));
		obj.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		obj.setSreceiversitename(getString(arg0, "sreceiversitename", arg1));
		obj.setSprojecttitle(getString(arg0, "sprojecttitle", arg1));
		obj.setSproductname(getString(arg0, "sproductname", arg1));
		obj.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		obj.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		return obj;
	}

}
