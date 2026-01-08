package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'invoicequotationheader' table of the
 * Database.
 */

@Entity
@Table(name = "invoicetaccalculation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("serial")
public class InvoiceTaxCalculation extends CustomizedResultsetRowMapper<InvoiceTaxCalculation>
		implements Serializable, RowMapper<InvoiceTaxCalculation> {

	@Column(name = "staxname", length = 50, nullable = false)
	private String staxname;

	@Column(name = "ntaxpercentage")
	private String ntaxpercentage;

	@Column(name = "ntaxamount")
	private String ntaxamount;

	private transient String staxtype;

	@Override
	public InvoiceTaxCalculation mapRow(ResultSet arg0, int arg1) throws SQLException {
		final InvoiceTaxCalculation objTaxCalculation = new InvoiceTaxCalculation();

		objTaxCalculation.setStaxname(StringEscapeUtils.unescapeJava(getString(arg0, "staxname", arg1)));
		objTaxCalculation.setNtaxpercentage(getString(arg0, "ntaxpercentage", arg1));
		objTaxCalculation.setNtaxamount(getString(arg0, "ntaxamount", arg1));
		objTaxCalculation.setStaxtype(StringEscapeUtils.unescapeJava(getString(arg0, "staxtype", arg1)));

		return objTaxCalculation;
	}

}
