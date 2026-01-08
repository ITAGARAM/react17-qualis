package com.agaramtech.qualis.barcodeprinting.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "barcodefiltertype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BarcodeFilterType extends CustomizedResultsetRowMapper<BarcodeFilterType> implements Serializable,RowMapper<BarcodeFilterType>{
	
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nbarcodefiltertypecode")
	private int nbarcodefiltertypecode;
	
	@Column(name = "nformcode")
	private short nformcode;
	
	@Column(name = "sbarcodefiltertypename", length=100, nullable = false)
	private String sbarcodefiltertypename;
	
	@Column(name = "sfiltertablename", length=100, nullable = false)
	private String sfiltertablename;
	
	@Lob
	@Column(name="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Column(name = "ssqlquery", columnDefinition = "text")
	private String ssqlquery="";
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("4")
	@Column(name = "nneedformnumber", nullable = false)
	private short nneedformnumber=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();	
	
	@Transient
	private transient String sdisplaystatus;
	
	
	public BarcodeFilterType mapRow(ResultSet arg0, int arg1) throws SQLException  {		
		final BarcodeFilterType objBarcodeFilterType = new BarcodeFilterType();
		objBarcodeFilterType.setNbarcodefiltertypecode(getInteger(arg0,"nbarcodefiltertypecode",arg1));	
		objBarcodeFilterType.setSbarcodefiltertypename(getString(arg0,"sbarcodefiltertypename",arg1));	
		objBarcodeFilterType.setSfiltertablename(getString(arg0,"sfiltertablename",arg1));
		objBarcodeFilterType.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objBarcodeFilterType.setNstatus(getShort(arg0,"nstatus",arg1));
		objBarcodeFilterType.setSsqlquery(StringEscapeUtils.unescapeJava(getString(arg0, "ssqlquery", arg1)));
		objBarcodeFilterType.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));	
		objBarcodeFilterType.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objBarcodeFilterType.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objBarcodeFilterType.setNformcode(getShort(arg0,"nformcode",arg1));	
		objBarcodeFilterType.setNneedformnumber(getShort(arg0,"nneedformnumber",arg1));	

		return objBarcodeFilterType;
	}	
}
