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
 * This class is used to map the fields of 'unitsofinvoice' table of the
 * Database.
 */
@Entity
@Table(name = "unitsofinvoice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UnitsofInvoice extends CustomizedResultsetRowMapper<UnitsofInvoice>
		implements Serializable, RowMapper<UnitsofInvoice> {

	private static final long serialVersionUID = 1L;

	@Column(name = "sunitcode ", length = 100, nullable = false)
	private String sunitcode;

	@Column(name = "sunitname ", length = 100, nullable = false)
	private String sunitname;

	@Override
	public UnitsofInvoice mapRow(ResultSet arg0, int arg1) throws SQLException {
		UnitsofInvoice objUnitsofInvoice = new UnitsofInvoice();

		objUnitsofInvoice.setSunitcode(StringEscapeUtils.unescapeJava(getString(arg0, "sunitcode", arg1)));
		objUnitsofInvoice.setSunitname(StringEscapeUtils.unescapeJava(getString(arg0, "sunitname", arg1)));
		return objUnitsofInvoice;
	}
}