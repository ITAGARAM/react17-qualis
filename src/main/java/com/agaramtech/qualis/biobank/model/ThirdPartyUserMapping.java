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
 * This class is used to map the fields of 'thirdpartyusermapping' table of the Database.
 * 
 * @author Mullai Balaji.V BGSI-12 31/07/2025
 * 
 */
@Entity
@Table(name = "thirdpartyusermapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class ThirdPartyUserMapping extends CustomizedResultsetRowMapper<ThirdPartyUserMapping> implements Serializable, RowMapper<ThirdPartyUserMapping> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nthirdpartyusermappingcode")
	private int nthirdpartyusermappingcode;

	@Column(name = "nthirdpartycode", nullable = false)
	private int nthirdpartycode;
	
	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;
	
	@Column(name = "nusercode", nullable = false)
	private int nusercode;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String suserrolename="";

	@Transient
	private transient String susername="";
	
	@Transient
	private transient String sthirdpartyname;	

	
	@Override
	public ThirdPartyUserMapping mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ThirdPartyUserMapping objThirdPartyUserMapping= new ThirdPartyUserMapping();

		objThirdPartyUserMapping.setNthirdpartycode(getInteger(arg0, "nthirdpartyusermappingcode", arg1));
		objThirdPartyUserMapping.setNthirdpartycode(getInteger(arg0, "nthirdpartycode", arg1));
		objThirdPartyUserMapping.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objThirdPartyUserMapping.setNusercode(getInteger(arg0, "nusercode", arg1));
		objThirdPartyUserMapping.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objThirdPartyUserMapping.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objThirdPartyUserMapping.setNstatus(getShort(arg0, "nstatus", arg1));
		objThirdPartyUserMapping.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objThirdPartyUserMapping.setSusername(getString(arg0, "susername", arg1));
		objThirdPartyUserMapping.setSthirdpartyname(getString(arg0, "sthirdpartyname", arg1));

		return objThirdPartyUserMapping;

	}

}