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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "printjob")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PrintJob extends CustomizedResultsetRowMapper<PrintJob> implements Serializable, RowMapper<PrintJob>{


	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "printid")
	private long printid;

	@Column(name = "printerKey")
	private String printerKey;

	@Column(name = "printerName")
	private String printerName;

	@Column(name = "printuuid")
	private String printuuid;
	
	@Column(name = "sfilename")
	private String sfilename;
	
	
	@Column(name = "printby_usercode")
	private int printby_usercode  = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Override
	public PrintJob mapRow(ResultSet arg0, int arg1) throws SQLException {
		final PrintJob printJob = new PrintJob();
		printJob.setPrintid(getLong(arg0, "printid", arg1));
		printJob.setPrinterKey(getString(arg0, "printerKey", arg1));
		printJob.setPrinterName(getString(arg0, "printerName", arg1));
		printJob.setPrintuuid(getString(arg0, "printuuid", arg1));
		printJob.setPrintby_usercode(getInteger(arg0, "printby_usercode", arg1));
		printJob.setNsitecode(getShort(arg0, "nsitecode", arg1));
		printJob.setNstatus(getShort(arg0, "nstatus", arg1));
		printJob.setSfilename(getString(arg0, "sfilename", arg1));
		
		return printJob;
	}
	
	
}
