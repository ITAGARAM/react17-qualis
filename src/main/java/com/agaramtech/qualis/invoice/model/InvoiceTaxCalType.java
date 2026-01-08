package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

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

/**
 * This class is used to map the fields of 'invoicetaxcaltype' table of the
 * Database.
 */
@Entity
@Table(name = "invoicetaxcaltype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceTaxCalType extends CustomizedResultsetRowMapper<InvoiceTaxCalType>
		implements Serializable, RowMapper<InvoiceTaxCalType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ncaltypecode", nullable = false)
	private short ncaltypecode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Transient
	private String staxcaltype;

	@Override
	public InvoiceTaxCalType mapRow(ResultSet arg0, int arg1) throws SQLException {
		InvoiceTaxCalType objTaxtype1 = new InvoiceTaxCalType();
		objTaxtype1.setNcaltypecode(getShort(arg0, "ncaltypecode", arg1));
		objTaxtype1.setStaxcaltype(getString(arg0, "staxcaltype", arg1));
		objTaxtype1.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objTaxtype1.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objTaxtype1.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objTaxtype1.setNstatus(getShort(arg0, "nstatus", arg1));
		return objTaxtype1;
	}
}
