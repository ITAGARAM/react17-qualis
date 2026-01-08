package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'invoicepreferencesetting' table of
 * the Database.
 * 
 * 
 */

@Entity
@Table(name = "invoicepreferencesetting")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoicePreferenceSetting extends CustomizedResultsetRowMapper<InvoicePreferenceSetting>
		implements Serializable, RowMapper<InvoicePreferenceSetting> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ninvoicesettingscode")
	private int ninvoicesettingscode;

	@Column(name = "sdescription", length = 500, nullable = false)
	private String sdescription;

	@Column(name = "svalue", length = 100, nullable = false)
	private String svalue;

	@Column(name = "svaluestext", length = 100, nullable = false)
	private String svaluestext;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "dmodifiedDate")
	private Instant dmodifiedDate;

	@Column(name = "nsitecode", nullable = false)
	private int nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	@Override
	public InvoicePreferenceSetting mapRow(ResultSet arg0, int arg1) throws SQLException {
		InvoicePreferenceSetting objInvoicePreferenceSetting = new InvoicePreferenceSetting();
		objInvoicePreferenceSetting.setNinvoicesettingscode(getInteger(arg0, "ninvoicesettingscode", arg1));
		objInvoicePreferenceSetting.setSdescription(getString(arg0, "sdescription", arg1));
		objInvoicePreferenceSetting.setSvalue(getString(arg0, "svalue", arg1));
		objInvoicePreferenceSetting.setSvaluestext(getString(arg0, "svaluestext", arg1));
		objInvoicePreferenceSetting.setNusercode(getInteger(arg0, "nusercode", arg1));
		objInvoicePreferenceSetting.setDmodifiedDate(getInstant(arg0, "dmodifiedDate", arg1));
		objInvoicePreferenceSetting.setNsitecode(getInteger(arg0, "nsitecode", arg1));
		objInvoicePreferenceSetting.setNstatus(getShort(arg0, "nstatus", arg1));
		return objInvoicePreferenceSetting;
	}

}
