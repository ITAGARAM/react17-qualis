package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'invoicepatientmaster' table of the
 * Database.
 */

@Entity
@Table(name = "invoicepatientmaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("serial")
public class InvoicePatient extends CustomizedResultsetRowMapper<InvoicePatient>
		implements Serializable, RowMapper<InvoicePatient> {

	@Id
	private int npatientno;
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@Column(name = "spatientid", length = 50)
	private String spatientid;
	@Column(name = "sfirstname", length = 100, nullable = false)
	private String sfirstname;
	@Column(name = "slastname", length = 100, nullable = false)
	private String slastname;
	@Column(name = "sfathername", length = 100)
	private String sfathername;
	@Column(name = "ngendercode", nullable = false)
	private short ngendercode;
	@Column(name = "ncitycode", nullable = false)
	private int ncitycode;
	@Column(name = "nneedmigrant", nullable = false)
	@ColumnDefault("4")
	private short nneedmigrant = 4;
	@Column(name = "ncountrycode", nullable = false)
	private int ncountrycode;
	@Column(name = "nregioncode", nullable = false)
	private int nregioncode;
	@Column(name = "ndistrictcode", nullable = false)
	private int ndistrictcode;
	@Column(name = "ddob", nullable = false)
	private Date ddob;
	@Column(name = "sage", length = 50, nullable = false)
	private String sage;
	@Column(name = "spostalcode", length = 20)
	private String spostalcode;
	@Column(name = "sstreet", length = 100, nullable = false)
	private String sstreet;
	@Column(name = "shouseno", length = 20, nullable = false)
	private String shouseno;
	@Column(name = "sflatno", length = 20, nullable = false)
	private String sflatno;
	@Column(name = "nneedcurrentaddress", nullable = false)
	@ColumnDefault("4")
	private short nneedcurrentaddress = 4;
	@Column(name = "nregioncodetemp")
	private int nregioncodetemp;
	@Column(name = "ndistrictcodetemp")
	private int ndistrictcodetemp;
	@Column(name = "ncitycodetemp")
	private int ncitycodetemp;
	@Column(name = "spostalcodetemp", length = 20)
	private String spostalcodetemp;
	@Column(name = "sstreettemp", length = 100)
	private String sstreettemp;
	@Column(name = "shousenotemp", length = 20)
	private String shousenotemp;
	@Column(name = "sflatnotemp", length = 20)
	private String sflatnotemp;
	@Column(name = "sphoneno", length = 20)
	private String sphoneno;
	@Column(name = "smobileno", length = 20)
	private String smobileno;
	@Column(name = "semail", length = 100)
	private String semail;
	@Column(name = "srefid", length = 255)
	private String srefid;
	@Column(name = "spassportno", length = 50)
	private String spassportno;
	@Column(name = "sexternalid", length = 255)
	private String sexternalid;
	@Column(name = "nsitecode")
	private short nsitecode = 1;
	@Column(name = "nstatus")
	private short nstatus = 1;

	private transient String sdob;
	private transient String spatientname;
	private transient String sgendername;
	private transient String scountryname;
	private transient String scityname;
	private transient String sdistrictname;
	private transient String sregionname;
	private transient String sregionnametemp;
	private transient String scitynametemp;
	private transient String sdistrictnametemp;
	private transient String sdisplaystatus;
	private transient String scurrentaddress;
	private transient String currentdate;
	private transient int npatientfiltercode;
	private transient String spatientfiltername;
	private transient Map<String, Object> jsondata;
	private transient String tree;
	private transient Map<String, Object> config;
	private transient String filterquery;
	private transient int nfilterstatus;
	private transient String sfilterstatus;
	private transient String smodifieddate;
	private transient String sarno;
	private transient String sregdate;
	private transient String scompletedate;
	private transient String sreportno;
	private transient String stransactiondate;
	private transient int npreregno;
	private transient String ssamplearno;
	private transient int noffsetdtransactiondate;
	private transient String stestsynonym;
	private transient String dcollectiondate;
	private transient String ssubmitterid;
	private transient String ssubmitterfirstname;
	private transient String ssubmitterlastname;
	private transient String ssubmitteremail;
	private transient String sshortname;
	private transient String ssubmittercode;
	private transient String stelephone;
	private transient String sinstitutionsitename;
	private transient String sinstitutioncityname;
	private transient String sinstitutiondistrictname;
	private transient String sinstitutiondistrictcode;
	private transient String sinstitutionname;
	private transient String sinstitutioncatname;
	private transient String sinstitutioncode;
	private transient String sdiagnosticcasename;
	private transient String ssubmittershortname;
	private transient String ssubmittersemail;
	private transient String ssubmittertelephone;
	private transient String sinscityname;
	private transient String sinsdistrictname;
	private transient String sinsdistrictcode;
	private transient String ssitecode;
	private transient int npatientid;

	@Override
	public InvoicePatient mapRow(ResultSet arg0, int arg1) throws SQLException {
		InvoicePatient objPatient = new InvoicePatient();
		objPatient.setNpatientno(getInteger(arg0, "npatientno", arg1));
		objPatient.setNpatientno(getShort(arg0, "ncitycode", arg1));
		objPatient.setSpatientid(getString(arg0, "spatientid", arg1));
		objPatient.setSfirstname(getString(arg0, "sfirstname", arg1));
		objPatient.setSlastname(getString(arg0, "slastname", arg1));
		objPatient.setSfathername(getString(arg0, "sfathername", arg1));
		objPatient.setNgendercode(getShort(arg0, "ngendercode", arg1));
		objPatient.setNcitycode(getInteger(arg0, "ncitycode", arg1));
		objPatient.setNcountrycode(getInteger(arg0, "ncountrycode", arg1));
		objPatient.setDdob(getDate(arg0, "ddob", arg1));
		objPatient.setSage(getString(arg0, "sage", arg1));
		objPatient.setSdistrictname(getString(arg0, "sdistrictname", arg1));
		objPatient.setSpostalcode(getString(arg0, "spostalcode", arg1));
		objPatient.setSphoneno(getString(arg0, "sphoneno", arg1));
		objPatient.setSmobileno(getString(arg0, "smobileno", arg1));
		objPatient.setSemail(getString(arg0, "semail", arg1));
		objPatient.setSrefid(getString(arg0, "srefid", arg1));
		objPatient.setSpassportno(getString(arg0, "spassportno", arg1));
		objPatient.setSexternalid(getString(arg0, "sexternalid", arg1));
		objPatient.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objPatient.setNstatus(getShort(arg0, "nstatus", arg1));
		objPatient.setSpatientname(getString(arg0, "spatientname", arg1));
		objPatient.setSgendername(getString(arg0, "sgendername", arg1));
		objPatient.setScountryname(getString(arg0, "scountryname", arg1));
		objPatient.setScityname(getString(arg0, "scityname", arg1));
		objPatient.setSdob(getString(arg0, "sdob", arg1));
		objPatient.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objPatient.setSstreet(getString(arg0, "sstreet", arg1));
		objPatient.setSstreettemp(getString(arg0, "sstreettemp", arg1));
		objPatient.setShousenotemp(getString(arg0, "shousenotemp", arg1));
		objPatient.setShouseno(getString(arg0, "shouseno", arg1));
		objPatient.setSflatno(getString(arg0, "sflatno", arg1));
		objPatient.setSflatnotemp(getString(arg0, "sflatnotemp", arg1));
		objPatient.setNcitycodetemp(getInteger(arg0, "ncitycodetemp", arg1));
		objPatient.setNregioncodetemp(getInteger(arg0, "nregioncodetemp", arg1));
		objPatient.setNregioncode(getInteger(arg0, "nregioncode", arg1));
		objPatient.setNdistrictcode(getInteger(arg0, "ndistrictcode", arg1));
		objPatient.setNdistrictcodetemp(getInteger(arg0, "ndistrictcodetemp", arg1));
		objPatient.setNneedcurrentaddress(getShort(arg0, "nneedcurrentaddress", arg1));
		objPatient.setNneedmigrant(getShort(arg0, "nneedmigrant", arg1));
		objPatient.setSpostalcodetemp(getString(arg0, "spostalcodetemp", arg1));
		objPatient.setSregionnametemp(getString(arg0, "sregionnametemp", arg1));
		objPatient.setScitynametemp(getString(arg0, "scitynametemp", arg1));
		objPatient.setSdistrictnametemp(getString(arg0, "sdistrictnametemp", arg1));
		objPatient.setSregionname(getString(arg0, "sregionname", arg1));
		objPatient.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objPatient.setScurrentaddress(getString(arg0, "scurrentaddress", arg1));
		objPatient.setCurrentdate(getString(arg0, "currentdate", arg1));
		objPatient.setSpatientfiltername(getString(arg0, "spatientfiltername", arg1));
		objPatient.setNpatientfiltercode(getInteger(arg0, "npatientfiltercode", arg1));
		objPatient.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objPatient.setTree(getString(arg0, "tree", arg1));
		objPatient.setConfig(getJsonObject(arg0, "config", arg1));
		objPatient.setFilterquery(getString(arg0, "filterquery", arg1));
		objPatient.setNfilterstatus(getInteger(arg0, "nfilterstatus", arg1));
		objPatient.setSfilterstatus(getString(arg0, "sfilterstatus", arg1));
		objPatient.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		objPatient.setSarno(getString(arg0, "sarno", arg1));
		objPatient.setSregdate(getString(arg0, "sregdate", arg1));
		objPatient.setScompletedate(getString(arg0, "scompletedate", arg1));
		objPatient.setStestsynonym(getString(arg0, "stestsynonym", arg1));
		objPatient.setSreportno(getString(arg0, "sreportno", arg1));
		objPatient.setDcollectiondate(getString(arg0, "dcollectiondate", arg1));
		objPatient.setSsubmitterfirstname(getString(arg0, "ssubmitterfirstname", arg1));
		objPatient.setSsubmitterlastname(getString(arg0, "ssubmitterlastname", arg1));
		objPatient.setSsubmittershortname(getString(arg0, "ssubmittershortname", arg1));
		objPatient.setSsubmittercode(getString(arg0, "ssubmittercode", arg1));
		objPatient.setSsubmittersemail(getString(arg0, "ssubmittersemail", arg1));
		objPatient.setSsubmitterid(getString(arg0, "ssubmitterid", arg1));
		objPatient.setSsubmittertelephone(getString(arg0, "ssubmittertelephone", arg1));
		objPatient.setSinstitutionname(getString(arg0, "sinstitutionname", arg1));
		objPatient.setSinstitutioncatname(getString(arg0, "sinstitutioncatname", arg1));
		objPatient.setSinstitutioncode(getString(arg0, "sinstitutioncode", arg1));
		objPatient.setSinstitutionsitename(getString(arg0, "sinstitutionsitename", arg1));
		objPatient.setSinscityname(getString(arg0, "sinscityname", arg1));
		objPatient.setSinsdistrictname(getString(arg0, "sinsdistrictname", arg1));
		objPatient.setSinsdistrictcode(getString(arg0, "sinsdistrictcode", arg1));
		objPatient.setNpatientid(getInteger(arg0, "npatientid", arg1));

		objPatient.setSsitecode(getString(arg0, "ssitecode", arg1));

		return objPatient;
	}

}
