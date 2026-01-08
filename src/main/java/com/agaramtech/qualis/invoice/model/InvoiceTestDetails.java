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
 * This class is used to map the fields of 'invoicetestdetails' table of the
 * Database.
 */

@Entity
@Table(name = "invoicetestdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("serial")
public class InvoiceTestDetails extends CustomizedResultsetRowMapper<InvoiceTestDetails>
		implements Serializable, RowMapper<InvoiceTestDetails> {

	@Column(name = "sproducttestname")
	private String sproducttestname;

	@Column(name = "ntestcost")
	private String ntestcost;

	@Column(name = "selected")
	private Boolean selected;

	@Column(name = "sspecname")
	private String sspecname;

	@Column(name = "nproducttestcode")
	private String nproducttestcode;

	@Override
	public InvoiceTestDetails mapRow(ResultSet arg0, int arg1) throws SQLException {

		final InvoiceTestDetails objInvoiceTestDetails = new InvoiceTestDetails();

		objInvoiceTestDetails
				.setSproducttestname(StringEscapeUtils.unescapeJava(getString(arg0, "sproducttestname", arg1)));
		objInvoiceTestDetails.setNtestcost(getString(arg0, "ntestcost", arg1));
		objInvoiceTestDetails.setSspecname(StringEscapeUtils.unescapeJava(getString(arg0, "sspecname", arg1)));
		objInvoiceTestDetails.setSelected(getBoolean(arg0, "selected", arg1));
		objInvoiceTestDetails
				.setNproducttestcode(StringEscapeUtils.unescapeJava(getString(arg0, "nproducttestcode", arg1)));
		return objInvoiceTestDetails;
	}

}
