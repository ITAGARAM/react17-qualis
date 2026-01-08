package com.agaramtech.qualis.biobank.model;

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
 * This class is used to map the fields of 'thirdparty' table of the Database.
 * 
 * @author Mullai Balaji.V BGSI-12 28/07/2025
 * 
 */
@Entity
@Table(name = "thirdparty")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class ThirdParty extends CustomizedResultsetRowMapper<ThirdParty>
		implements Serializable, RowMapper<ThirdParty> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nthirdpartycode")
	private int nthirdpartycode;

	@Column(name = "sthirdpartyname ", length = 150, nullable = false)
	private String sthirdpartyname;

	@Column(name = "saddress ", length = 300, nullable = false)
	private String saddress="";

	@Column(name = "semail ", length = 100, nullable = false)
	private String semail="";

	@Column(name = "sphonenumber",length=20, nullable = false)
	private String sphonenumber="";

	@Column(name = "sdescription", length = 300)
	private String sdescription="";

	@ColumnDefault("4")
	@Column(name = "nisngs")
	private short nisngs = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private String sisngs;

	@Override
	public ThirdParty mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ThirdParty objThirdParty = new ThirdParty();

		objThirdParty.setNthirdpartycode(getInteger(arg0, "nthirdpartycode", arg1));
		objThirdParty.setSthirdpartyname(getString(arg0, "sthirdpartyname", arg1));
		objThirdParty.setSaddress(getString(arg0, "saddress", arg1));
		objThirdParty.setSdescription(getString(arg0, "sdescription", arg1));
		objThirdParty.setSphonenumber(getString(arg0, "sphonenumber", arg1));
		objThirdParty.setNisngs(getShort(arg0, "nisngs", arg1));
		objThirdParty.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objThirdParty.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objThirdParty.setNstatus(getShort(arg0, "nstatus", arg1));
		objThirdParty.setSemail(getString(arg0, "semail", arg1));
		objThirdParty.setSisngs(getString(arg0, "sisngs", arg1));

		return objThirdParty;

	}

}
