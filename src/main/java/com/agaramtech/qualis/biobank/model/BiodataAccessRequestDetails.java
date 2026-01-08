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
 * This class is used to map the fields of 'biodataaccessrequestdetails' table
 * of the Database.
 */
@Entity
@Table(name = "biodataaccessrequestdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BiodataAccessRequestDetails extends CustomizedResultsetRowMapper<BiodataAccessRequestDetails>
		implements Serializable, RowMapper<BiodataAccessRequestDetails> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbiodataaccessrequestdetailscode", nullable = false)
	private int nbiodataaccessrequestdetailscode;

	@Id
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode;

	@Column(name = "nbiodataaccessrequestcode", nullable = false)
	private int nbiodataaccessrequestcode;

	@Column(name = "nbioprojectcode", nullable = false)
	private int nbioprojectcode;

	@Column(name = "nproductcode", nullable = false)
	private int nproductcode;

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

	@Transient
	private String sprojecttitle;
	@Transient
	private String sproductname;

	@Override
	public BiodataAccessRequestDetails mapRow(ResultSet arg0, int arg1) throws SQLException {
		BiodataAccessRequestDetails obj = new BiodataAccessRequestDetails();

		obj.setNbiodataaccessrequestdetailscode(getInteger(arg0, "nbiodataaccessrequestdetailscode", arg1));
		obj.setNsitecode(getShort(arg0, "nsitecode", arg1));
		obj.setNbiodataaccessrequestcode(getInteger(arg0, "nbiodataaccessrequestcode", arg1));
		obj.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		obj.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		obj.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		obj.setNusercode(getInteger(arg0, "nusercode", arg1));
		obj.setNdeputyusercode(getInteger(arg0, "ndeputyusercode", arg1));
		obj.setNdeputyuserrolecode(getInteger(arg0, "ndeputyuserrolecode", arg1));
		obj.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		obj.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		obj.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		obj.setNstatus(getShort(arg0, "nstatus", arg1));
		obj.setSprojecttitle(getString(arg0, "sprojecttitle", arg1));
		obj.setSproductname(getString(arg0, "sproductname", arg1));

		return obj;
	}

}
