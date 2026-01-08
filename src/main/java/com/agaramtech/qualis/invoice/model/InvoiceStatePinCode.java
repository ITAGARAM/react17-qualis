package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.apache.commons.text.StringEscapeUtils;

import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'invoicestatepincode' table of the
 * Database.
 */
@Entity
@Table(name = "invoicestatepincode")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceStatePinCode extends CustomizedResultsetRowMapper<InvoiceStatePinCode>
		implements Serializable, RowMapper<InvoiceStatePinCode> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "sstateCode")
	private String sstateCode;

	@Column(name = "npinCode")
	private int npinCode;

	@Column(name = "sstateName")
	private String sstateName;

	@Override
	public InvoiceStatePinCode mapRow(ResultSet arg0, int arg1) throws SQLException {
		final InvoiceStatePinCode objpincode = new InvoiceStatePinCode();
		objpincode.setSstateCode(StringEscapeUtils.unescapeJava(getString(arg0, "sstateCode", arg1)));
		objpincode.setNpinCode(getInteger(arg0, "npinCode", arg1));
		objpincode.setSstateName(StringEscapeUtils.unescapeJava(getString(arg0, "sstateName", arg1)));
		return objpincode;
	}
}
