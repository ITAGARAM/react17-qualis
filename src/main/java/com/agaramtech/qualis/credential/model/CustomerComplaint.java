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
 * This class is used to map the fields of 'customercomplaint' table of the
 * Database.
 * 
 * @author Mullai Balaji.V [SWSM-9] Customer Complaints - Screen Development -
 *         Agaram Technologies
 * 
 */
@Entity
@Table(name = "customercomplaint")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CustomerComplaint extends CustomizedResultsetRowMapper<CustomerComplaint>
		implements Serializable, RowMapper<CustomerComplaint> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ncustomercomplaintcode")
	private int ncustomercomplaintcode;
	@Column(name = "dcomplaintdate")
	private Instant dcomplaintdate;
	@Column(name = "noffsetdcomplaintdate", nullable = false)
	private int noffsetdcomplaintdate;
	@Column(name = "sreceivedfrom", length = 150, nullable = false)
	private String sreceivedfrom;
    //modified semail Length by MullaiBalaji  
	@Column(name = "semail", length = 100, nullable = false)
	private String semail = "";
	@Column(name = "scontactnumber", length = 30, nullable = false)
	private String scontactnumber = "";
	// added by sujatha ATE_274 26-09-2025 for audit trail to show central site
	@Column(name = "ncentralsitecode", nullable = false)
	private int ncentralsitecode;
	@Column(name = "nregioncode", nullable = false)
	private int nregioncode;
	@Column(name = "ndistrictcode", nullable = false)
	private int ndistrictcode;
	@Column(name = "ncitycode", nullable = false)
	private int ncitycode;
	@Column(name = "nvillagecode", nullable = false)
	private int nvillagecode;
	@Column(name = "slocation", length = 150)
	private String slocation = "";
	@Column(name = "scomplaintdetails", length = 150)
	private String scomplaintdetails = "";
	@Column(name = "slatitude", length = 15)
	private String slatitude = "";
	@Column(name = "slongitude", length = 15)
	private String slongitude = "";
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@ColumnDefault("-1")
	@Column(name = "ntzcomplaintdate", nullable = false)
	private short ntzcomplaintdate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient int ncustomercomplainthistorycode;
	@Transient
	private transient String sregionname = "";
	@Transient
	private transient String sdistrictname = "";
	@Transient
	private transient String scityname = "";
	@Transient
	private transient String svillagename = "";
	@Transient
	private transient String scomplaintdate;
	@Transient
	private transient String sreceiverdate;
	
	@Transient
	private transient String smodifieddate;
	@Transient
	private transient String stransactiondate;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient int ntransdisplaystatus;
	@Transient
	private transient Instant dtransactiondate;
	@Transient
	private transient short ntztransactiondate;
	@Transient
	private transient String sremarks = "";
	@Transient
	private transient int ncolorcode;
	@Transient
	private transient int ntransactionstatus;
	@Transient
	private transient String scolorhexcode = "";
	@Transient
	private transient String ssitename;
	@Transient
	private transient String ssitetypename;
	@Transient
	private transient String srelation;
	@Transient
	private transient int nlevel;
	//added by sujatha ATE_274 23-09-2025 to get villages with one more check based on nsitehierarchyconfigcode
	@Transient
	private transient int nsitehierarchyconfigcode;
	// added by sujatha ATE_274 26-09-2025 for audit trail to show central site
	@Transient
	private transient String scentralsitename;

	@Override
	public CustomerComplaint mapRow(ResultSet arg0, int arg1) throws SQLException {
		final CustomerComplaint objCustomerComplaint = new CustomerComplaint();
		objCustomerComplaint.setNcustomercomplainthistorycode(getInteger(arg0, "ncustomercomplainthistorycode", arg1));
		objCustomerComplaint.setNtransdisplaystatus(getInteger(arg0, "ntransdisplaystatus", arg1));
		objCustomerComplaint.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objCustomerComplaint.setNtransdisplaystatus(getInteger(arg0, "ntransdisplaystatus", arg1));
		objCustomerComplaint.setNcolorcode(getInteger(arg0, "ncolorcode", arg1));
		objCustomerComplaint.setNcustomercomplaintcode(getInteger(arg0, "ncustomercomplaintcode", arg1));
		objCustomerComplaint.setSreceivedfrom(getString(arg0, "sreceivedfrom", arg1));
		objCustomerComplaint.setSemail(getString(arg0, "semail", arg1));
		objCustomerComplaint.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		objCustomerComplaint.setScontactnumber(getString(arg0, "scontactnumber", arg1));
		objCustomerComplaint.setScomplaintdetails(getString(arg0, "scomplaintdetails", arg1));
		objCustomerComplaint.setStransactiondate(getString(arg0, "stransactiondate", arg1));
		objCustomerComplaint.setSlocation(getString(arg0, "slocation", arg1));
		objCustomerComplaint.setSlatitude(getString(arg0, "slatitude", arg1));
		objCustomerComplaint.setSlongitude(getString(arg0, "slongitude", arg1));
		objCustomerComplaint.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objCustomerComplaint.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objCustomerComplaint.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objCustomerComplaint.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objCustomerComplaint.setNstatus(getShort(arg0, "nstatus", arg1));
		objCustomerComplaint.setNregioncode(getInteger(arg0, "nregioncode", arg1));
		objCustomerComplaint.setNdistrictcode(getInteger(arg0, "ndistrictcode", arg1));
		objCustomerComplaint.setNcitycode(getInteger(arg0, "ncitycode", arg1));
		objCustomerComplaint.setNvillagecode(getInteger(arg0, "nvillagecode", arg1));
		objCustomerComplaint.setSregionname(getString(arg0, "sregionname", arg1));
		objCustomerComplaint.setSdistrictname(getString(arg0, "sdistrictname", arg1));
		objCustomerComplaint.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objCustomerComplaint.setScityname(getString(arg0, "scityname", arg1));
		objCustomerComplaint.setSvillagename(getString(arg0, "svillagename", arg1));
		objCustomerComplaint.setDcomplaintdate(getInstant(arg0, "dcomplaintdate", arg1));
		objCustomerComplaint.setScomplaintdate(getString(arg0, "scomplaintdate", arg1));
		objCustomerComplaint.setSreceiverdate(getString(arg0, "sreceiverdate", arg1));
		objCustomerComplaint.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		objCustomerComplaint.setSremarks(getString(arg0, "sremarks", arg1));
		objCustomerComplaint.setNoffsetdcomplaintdate(getInteger(arg0, "noffsetdcomplaintdate", arg1));
		objCustomerComplaint.setNtzcomplaintdate(getShort(arg0, "ntzcomplaintdate", arg1));
		objCustomerComplaint.setSsitename(getString(arg0, "ssitename", arg1));
		objCustomerComplaint.setSsitetypename(getString(arg0, "ssitetypename", arg1));
		objCustomerComplaint.setSrelation(getString(arg0, "srelation", arg1));
		objCustomerComplaint.setNlevel(getInteger(arg0, "nlevel", arg1));
		//added by sujatha ATE_274 23-09-2025 to get villages with one more check based on nsitehierarchyconfigcode
		objCustomerComplaint.setNsitehierarchyconfigcode(getInteger(arg0, "nsitehierarchyconfigcode", arg1));
		// added by sujatha ATE_274 26-09-2025 for audit trail to show central site
		objCustomerComplaint.setNcentralsitecode(getInteger(arg0, "ncentralsitecode", arg1));
		objCustomerComplaint.setScentralsitename(getString(arg0, "scentralsitename", arg1));

		return objCustomerComplaint;
	}

}
