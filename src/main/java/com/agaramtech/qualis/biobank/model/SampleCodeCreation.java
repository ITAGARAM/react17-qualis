package com.agaramtech.qualis.biobank.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
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
@Table(name = "productcodemapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleCodeCreation extends CustomizedResultsetRowMapper<SampleCodeCreation>
		implements Serializable, RowMapper<SampleCodeCreation> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nproductcodemappingcode ")
	private int nproductcodemappingcode=Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nproductcode ")
	private int nproductcode=Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "sproductcode", length = 2, nullable = false)
	private String sproductcode;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
    @Transient
	private transient boolean isSampletypeExists;
    
    @Transient
	private transient boolean isCodeExists;
    
    @Transient
	private transient String sprojecttypename="";
    
    @Transient
	private transient String sproductname="";
    

	@Override
	public SampleCodeCreation mapRow(final ResultSet arg0, final int arg1) throws SQLException {
		final SampleCodeCreation sampleCodeCreation = new SampleCodeCreation();
		sampleCodeCreation.setNproductcodemappingcode(getInteger(arg0, "nproductcodemappingcode", arg1));
		sampleCodeCreation.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		sampleCodeCreation.setSproductcode(StringEscapeUtils.unescapeJava(getString(arg0, "sproductcode", arg1)));
		sampleCodeCreation.setSproductname(getString(arg0, "sproductname", arg1));
		sampleCodeCreation.setSprojecttypename(getString(arg0, "sprojecttypename", arg1));
		sampleCodeCreation.setSampletypeExists(getBoolean(arg0, "isSampletypeExists", arg1));
		sampleCodeCreation.setCodeExists(getBoolean(arg0, "isCodeExists", arg1));
		sampleCodeCreation.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		sampleCodeCreation.setNsitecode(getShort(arg0, "nsitecode", arg1));
		sampleCodeCreation.setNstatus(getShort(arg0, "nstatus", arg1));
		return sampleCodeCreation;
	}

}