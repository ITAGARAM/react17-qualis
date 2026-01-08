package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'Invoice Customer master' table of
 * the Database.
 * 
 * @author ATE237
 * @version 11.0.0.2
 * @since 05- 09- 2025
 */

@Entity
@Table(name = "invoicecustomermaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceCustomerMaster extends CustomizedResultsetRowMapper<InvoiceCustomerMaster>
		implements Serializable, RowMapper<InvoiceCustomerMaster> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ncustomercode")
	private int ncustomercode;

	@Column(name = "scustomerreference", length = 50, nullable = false)
	private String scustomerreference;

	private transient String scustomertypename;

	@Column(name = "ntypecode")
	private int ntypecode;

	@Column(name = "scustomername", length = 50, nullable = false)
	private String scustomername;

	@Column(name = "scustomerpoc", length = 100, nullable = false)
	private String scustomerpoc;

	@Column(name = "scustomeraddress", length = 100, nullable = false)
	private String scustomeraddress;

	@Column(name = "scustomershipingaddress", length = 100, nullable = false)
	private String scustomershipingaddress;

	@Column(name = "semailid", length = 100, nullable = false)
	private String semailid;

	@Column(name = "sphone", length = 50, nullable = false)
	private String sphone;

	@Column(name = "saccountdetails", length = 100, nullable = false)
	private String saccountdetails;

	@Column(name = "sotherdetails", length = 100, nullable = false)
	private String sotherdetails;

	@Column(name = "scusttin", length = 100, nullable = false)
	private String scusttin;

	@Column(name = "scustgst", length = 100, nullable = false)
	private String scustgst;

	@Column(name = "scustomerreference1", length = 100, nullable = false)
	private String scustomerreference1;

	@Column(name = "scustomerreference2", length = 100, nullable = false)
	private String scustomerreference2;

	@Column(name = "ndiscountavailable", nullable = false)
	private int ndiscountavailable;

	@Column(name = "sprojectreference1", length = 100, nullable = false)
	private String sprojectreference1;

	@Column(name = "sprojectreference2", length = 100, nullable = false)
	private String sprojectreference2;

	@Column(name = "nusercode")
	private int nusercode;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private int nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	transient private String stransdisplaystatus;
	transient private int nsameasaddress;
	transient private String scontactname;

	@Override
	public InvoiceCustomerMaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		InvoiceCustomerMaster objInvoiceCustomerMaster = new InvoiceCustomerMaster();
		objInvoiceCustomerMaster.setNcustomercode(getInteger(arg0, "ncustomercode", arg1));
		objInvoiceCustomerMaster.setScustomerreference(getString(arg0, "scustomerreference", arg1));
		objInvoiceCustomerMaster.setScustomertypename(getString(arg0, "scustomertypename", arg1));
		objInvoiceCustomerMaster.setNtypecode(getShort(arg0, "ntypecode", arg1));
		objInvoiceCustomerMaster.setScustomername(getString(arg0, "scustomername", arg1));
		objInvoiceCustomerMaster.setScustomerpoc(getString(arg0, "scustomerpoc", arg1));
		objInvoiceCustomerMaster.setScustomeraddress(getString(arg0, "scustomeraddress", arg1));
		objInvoiceCustomerMaster.setScustomershipingaddress(getString(arg0, "scustomershipingaddress", arg1));
		objInvoiceCustomerMaster.setSemailid(getString(arg0, "semailid", arg1));
		objInvoiceCustomerMaster.setSphone(getString(arg0, "sphone", arg1));
		objInvoiceCustomerMaster.setSaccountdetails(getString(arg0, "saccountdetails", arg1));
		objInvoiceCustomerMaster.setSotherdetails(getString(arg0, "sotherdetails", arg1));
		objInvoiceCustomerMaster.setScusttin(getString(arg0, "scusttin", arg1));
		objInvoiceCustomerMaster.setScustgst(getString(arg0, "scustgst", arg1));
		objInvoiceCustomerMaster.setScustomerreference1(getString(arg0, "scustomerreference1", arg1));
		objInvoiceCustomerMaster.setScustomerreference2(getString(arg0, "scustomerreference2", arg1));
		objInvoiceCustomerMaster.setSprojectreference1(getString(arg0, "sprojectreference1", arg1));
		objInvoiceCustomerMaster.setSprojectreference2(getString(arg0, "sprojectreference2", arg1));
		objInvoiceCustomerMaster.setNdiscountavailable(getInteger(arg0, "ndiscountavailable", arg1));
		objInvoiceCustomerMaster.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInvoiceCustomerMaster.setNusercode(getInteger(arg0, "nusercode", arg1));
		objInvoiceCustomerMaster.setNsitecode(getInteger(arg0, "nsitecode", arg1));
		objInvoiceCustomerMaster.setNstatus(getShort(arg0, "nstatus", arg1));
		objInvoiceCustomerMaster.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objInvoiceCustomerMaster.setNsameasaddress(getInteger(arg0, "nsameasaddress", arg1));
		objInvoiceCustomerMaster.setScontactname(getString(arg0, "scontactname", arg1));
		return objInvoiceCustomerMaster;
	}

}