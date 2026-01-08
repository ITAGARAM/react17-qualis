package com.agaramtech.qualis.BarcodeConfiguration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

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
@Table(name = "printerdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Printerdetails extends CustomizedResultsetRowMapper<Printerdetails> implements Serializable, RowMapper<Printerdetails>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "printerId")
	private long printerId = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "printerKey")
	private String printerKey;
	
	@Column(name = "printerName")
	private String printerName;

	
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Transient
	private short sitecode;
	
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Override
	public Printerdetails mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Printerdetails printerdetails = new Printerdetails();
		
		printerdetails.setPrinterId(getLong(arg0, "printerId", arg1));
		printerdetails.setPrinterKey(getString(arg0, "printerKey", arg1));
		printerdetails.setPrinterName(getString(arg0, "printerName", arg1));
		printerdetails.setNsitecode(getShort(arg0, "nsitecode", arg1));
		printerdetails.setNstatus(getShort(arg0, "nstatus", arg1));
		printerdetails.setSitecode(getShort(arg0, "sitecode", arg1));
		
		return printerdetails;
	}
}
