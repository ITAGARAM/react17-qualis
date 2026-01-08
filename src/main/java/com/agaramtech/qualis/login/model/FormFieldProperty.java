package com.agaramtech.qualis.login.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.basemaster.model.Unit;
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
@Table(name = "unit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FormFieldProperty extends CustomizedResultsetRowMapper<FormFieldProperty> implements Serializable,RowMapper<FormFieldProperty>{


	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nformfieldpropertycode")
	private int nformfieldpropertycode;

	@Column(name = "nformcode", nullable=false)
	private int nformcode;

	@Column(name = "stablename", length=50, nullable = false)
	private String stablename;

	@Column(name = "sfieldname",length=50)
	private String sfieldname;

	@ColumnDefault("-1")
	@Column(name = "nfieldsize", nullable = false)
	private int nfieldsize=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	
	@Override
	public FormFieldProperty mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final FormFieldProperty objFormFieldProperty = new FormFieldProperty();
		
		objFormFieldProperty.setNformfieldpropertycode(getInteger(arg0,"nformfieldpropertycode",arg1));
		objFormFieldProperty.setNformcode(getInteger(arg0,"nformcode",arg1));
		objFormFieldProperty.setStablename(getString(arg0,"stablename",arg1));
		objFormFieldProperty.setSfieldname(getString(arg0,"sfieldname",arg1));
		objFormFieldProperty.setNfieldsize(getInteger(arg0,"nfieldsize",arg1));
		objFormFieldProperty.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objFormFieldProperty.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objFormFieldProperty.setNstatus(getShort(arg0,"nstatus",arg1));
		
		return objFormFieldProperty;
	}

}
