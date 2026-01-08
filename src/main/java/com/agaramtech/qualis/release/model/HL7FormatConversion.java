package com.agaramtech.qualis.release.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
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

@Entity
@Table(name = "hl7formatconversion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class HL7FormatConversion extends CustomizedResultsetRowMapper<HL7FormatConversion> implements Serializable, RowMapper<HL7FormatConversion> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nhl7formatconversioncode")
	private int nhl7formatconversioncode;

	@Column(name = "ncoareporthistorycode", nullable = false)
	private int ncoareporthistorycode;

	@Column(name = "ncoaparentcode", nullable = false)
	private int ncoaparentcode;

	@Column(name = "nversionno", nullable = false)
	private int nversionno;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;


	@ColumnDefault("8")
	@Column(name = "nsentstatus", nullable = false)
	private int nsentstatus = (short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus();

	@ColumnDefault("8")
	@Column(name = "nhl7formatstatus", nullable = false)
	private int nhl7formatstatus =  (short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus();
	
	@Column(name = "sfilename", length = 100)
	private String sfilename;

	@ColumnDefault("8")
	@Column(name = "nuploadstatus", nullable = false)
	private int nuploadstatus =(short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus();
	
	
	@Column(name = "scomments", length = 255)
	private String scomments;
	
	@Column(name = "dtransactiondate", nullable = false)
	private Date dtransactiondate;

	@ColumnDefault("0")
	@Column(name = "noffsetdtransactiondate")
	private int noffsetdtransactiondate=(short) Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ntransdatetimezonecode")
	private int ntransdatetimezonecode=(short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	
	
	@Override
	public HL7FormatConversion mapRow(ResultSet arg0, int arg1) throws SQLException {

		final HL7FormatConversion objCOAParent = new HL7FormatConversion();

		objCOAParent.setNhl7formatconversioncode(getInteger(arg0, "nhl7formatconversioncode", arg1));
		objCOAParent.setNcoareporthistorycode(getInteger(arg0, "ncoareporthistorycode", arg1));
		objCOAParent.setNcoaparentcode(getInteger(arg0, "ncoaparentcode", arg1));
		objCOAParent.setNversionno(getInteger(arg0, "nversionno", arg1));
		objCOAParent.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objCOAParent.setNsentstatus(getInteger(arg0, "nsentstatus", arg1));
		objCOAParent.setNhl7formatstatus(getInteger(arg0, "nhl7formatstatus", arg1));
		objCOAParent.setSfilename(getString(arg0, "sfilename", arg1));
		objCOAParent.setNuploadstatus(getInteger(arg0, "nuploadstatus", arg1));
		objCOAParent.setScomments(getString(arg0, "scomments", arg1));
		objCOAParent.setDtransactiondate(getDate(arg0,"dtransactiondate",arg1));
		objCOAParent.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objCOAParent.setNtransdatetimezonecode(getInteger(arg0,"ntransdatetimezonecode",arg1));
		objCOAParent.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objCOAParent.setNstatus(getShort(arg0,"nstatus",arg1));

		

		return objCOAParent;
	}

}
