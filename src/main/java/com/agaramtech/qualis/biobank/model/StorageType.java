package com.agaramtech.qualis.biobank.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * This class is used to map the fields of 'storagetype' table of
 * the Database.
 */

@Entity
@Table(name = "storagetype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StorageType extends CustomizedResultsetRowMapper<StorageType> implements Serializable, RowMapper<StorageType>{

	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nstoragetypecode")
	private int nstoragetypecode;

	@Column(name = "sstoragetypename", length = 100,nullable = false)
	private String sstoragetypename;

	@Column(name = "sstoragetypecode", length = 5)
	private String sstoragetypecode;

	@Column(name = "sdescirption", length = 255)
	private String sdescirption;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	
	@Override
	public StorageType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final StorageType objStorageType = new StorageType();

		objStorageType.setNstoragetypecode(getInteger(arg0, "nstoragetypecode", arg1));
		objStorageType.setSstoragetypename(getString(arg0, "sstoragetypename", arg1));
		objStorageType.setSstoragetypecode(getString(arg0, "sstoragetypecode", arg1));
		objStorageType.setSdescirption(getString(arg0, "sdescirption", arg1));
		objStorageType.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objStorageType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objStorageType.setNstatus(getShort(arg0, "nstatus", arg1));
		
		return objStorageType;
	}
	
}
