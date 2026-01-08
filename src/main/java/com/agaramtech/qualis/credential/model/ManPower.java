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
 * @author sujatha.v AT-E274
 * SWSM-8 01/08/2025  
 */
@Entity
@Table(name = "outsourceemployee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ManPower extends CustomizedResultsetRowMapper<ManPower> implements Serializable, RowMapper<ManPower>{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "noutsourceempcode")
	private int noutsourceempcode;
	
	@Column(name = "sfirstname", length = 150, nullable = false)
	private String sfirstname;

	@Column(name = "slastname", length = 150, nullable = false)
	private String slastname;
	
	@Column(name = "sdesignation", length = 150, nullable = false)
	private String sdesignation;
	
	@Column(name = "ddateofbirth")
	private Instant ddateofbirth;
	
	@Column(name = "ddateofjoin")
	private Instant ddateofjoin;
	
	@ColumnDefault("-1")
	@Column(name = "ntzdateofjoin", nullable = false)
	private short ntzdateofjoin=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "noffsetddateofjoin", nullable = false)
	private int noffsetddateofjoin;
	
	@Column(name = "smobileno", length = 25, nullable=false)
	private String smobileno;
	
    //modified semail Length by MullaiBalaji jira swsm-105 
	@Column(name = "semail", length = 100)
	private String semail;
	
	@Column(name = "nsitemastercode")
	private int nsitemastercode;
	
	@Column(name = "saddress", length = 300, nullable = false)
	private String saddress;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	
	@Transient
	private transient  String sdateofbirth;
	
	@Transient
	private transient  String sdateofjoin;
	
	@Transient
	private transient String ssitename;

	@Transient
	private transient String smodifieddate;
	
	@Transient
	private transient String susername;
	

	@Override
	public ManPower mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ManPower objManPower = new ManPower();
		objManPower.setNoutsourceempcode(getInteger(arg0, "noutsourceempcode", arg1));
		objManPower.setSfirstname(getString(arg0, "sfirstname", arg1));
		objManPower.setSlastname(getString(arg0, "slastname", arg1));
		objManPower.setSdesignation(getString(arg0, "sdesignation", arg1));
		objManPower.setDdateofbirth(getInstant(arg0, "ddateofbirth", arg1));
		objManPower.setDdateofjoin(getInstant(arg0, "ddateofjoin", arg1));
		objManPower.setNtzdateofjoin(getShort(arg0, "ntzdateofjoin", arg1));
		objManPower.setNoffsetddateofjoin(getInteger(arg0, "noffsetddateofjoin", arg1));
		objManPower.setSmobileno(getString(arg0, "smobileno", arg1));
		objManPower.setSemail(getString(arg0, "semail", arg1));
		objManPower.setNsitemastercode(getInteger(arg0, "nsitemastercode", arg1));
		objManPower.setSaddress(getString(arg0, "saddress", arg1));
		objManPower.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objManPower.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objManPower.setNstatus(getShort(arg0, "nstatus", arg1));
		objManPower.setSdateofbirth(getString(arg0,"sdateofbirth",arg1));
		objManPower.setSdateofjoin(getString(arg0,"sdateofjoin",arg1));
		objManPower.setSsitename(getString(arg0, "ssitename", arg1));
		objManPower.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		objManPower.setSusername(getString(arg0, "susername", arg1));

		return objManPower;
	}
}
