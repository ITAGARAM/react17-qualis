package com.agaramtech.qualis.instrumentmanagement.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

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

@Entity
@Table(name = "storageinstrument")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class StorageInstrument extends CustomizedResultsetRowMapper<StorageInstrument> implements  RowMapper<StorageInstrument> {

	@Id
	@Column(name = "nstorageinstrumentcode")
	private int nstorageinstrumentcode;
	
	@Column(name = "nstoragecategorycode", nullable = false)
	private int nstoragecategorycode;
	
	@Column(name = "nsamplestoragelocationcode", nullable = false)
	private int nsamplestoragelocationcode;
	
	@Column(name = "nsamplestorageversioncode", nullable = false)
	private int nsamplestorageversioncode;
	
	@Column(name = "nstorageconditioncode", nullable = false)
	private int nstorageconditioncode;
	
	@Column(name = "ninstrumentcatcode", nullable = false)
	private int ninstrumentcatcode;
	
	@Column(name = "ninstrumentcode", nullable = false)
	private int ninstrumentcode;
	
	@Column(name = "nregionalsitecode", nullable = false)
	private int nregionalsitecode;
	
	@Column(name = "nmappingtranscode", nullable = false)
	private int nmappingtranscode=Enumeration.TransactionStatus.DRAFT.gettransactionstatus();
	
	@Column(name="dmodifieddate",nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "ninstrumenttypecode", nullable = false)
	private short ninstrumenttypecode;

	@Transient
	private transient String sstoragecategoryname;
	
	@Transient
	private transient String ssamplestoragelocationname;
	
	@Transient
	private transient String sinstrumentcatname;
	
	@Transient
	private transient String sinstrumentid;
	
	@Transient
	private transient String sstorageconditionname;
	
	@Transient
	private transient String sinstrumentcode;
	
	@Transient
	private transient Map<String, Object> jsondata;
	
	@Transient
	private transient String sinstrumenttype;

	@Override
	public StorageInstrument mapRow(ResultSet arg0, int arg1) throws SQLException {

		final StorageInstrument objStorageInstrument = new StorageInstrument();

		objStorageInstrument.setNstorageinstrumentcode(getInteger(arg0,"nstorageinstrumentcode",arg1));
		objStorageInstrument.setNstoragecategorycode(getInteger(arg0,"nstoragecategorycode",arg1));
		objStorageInstrument.setNsamplestoragelocationcode(getInteger(arg0,"nsamplestoragelocationcode",arg1));
		objStorageInstrument.setNsamplestorageversioncode(getInteger(arg0,"nsamplestorageversioncode",arg1));
		objStorageInstrument.setNstorageconditioncode(getInteger(arg0,"nstorageconditioncode",arg1));
		objStorageInstrument.setNinstrumentcatcode(getInteger(arg0,"instrumentcatcode",arg1));
		objStorageInstrument.setNinstrumentcode(getInteger(arg0,"ninstrumentcode",arg1));
		objStorageInstrument.setNregionalsitecode(getInteger(arg0,"nregionalsitecode",arg1));
		objStorageInstrument.setNmappingtranscode(getInteger(arg0,"nmappingtranscode",arg1));
		objStorageInstrument.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objStorageInstrument.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objStorageInstrument.setNstatus(getShort(arg0,"nstatus",arg1));
		objStorageInstrument.setSstoragecategoryname(getString(arg0,"sstoragecategoryname",arg1));
		objStorageInstrument.setSsamplestoragelocationname(getString(arg0,"ssamplestoragelocationname",arg1));
		objStorageInstrument.setSinstrumentcatname(getString(arg0,"sinstrumentcatname",arg1));
		objStorageInstrument.setSinstrumentid(getString(arg0,"sinstrumentid",arg1));
		objStorageInstrument.setSstorageconditionname(getString(arg0,"sstorageconditionname",arg1));
		objStorageInstrument.setSinstrumentcode(getString(arg0,"sinstrumentcode",arg1));
		objStorageInstrument.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objStorageInstrument.setNinstrumenttypecode(getShort(arg0,"ninstrumenttypecode",arg1));
		objStorageInstrument.setSinstrumenttype(getString(arg0,"sinstrumenttype",arg1));

		return objStorageInstrument;
	}
	

}
