package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'invoiceexportheader' table of the
 * Database.
 */
@Entity
@Table(name = "invoiceexportheader")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class InvoiceExportHeader extends CustomizedResultsetRowMapper<InvoiceExportHeader>
		implements Serializable, RowMapper<InvoiceExportHeader> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nimpconfigcode")
	private int nimpconfigcode;
	@Column(name = "stablename", length = 50, nullable = false)
	private String stablename;

	@Column(name = "nformcode")
	private int nformcode;

	@Column(name = "ncontrolcode")
	private int ncontrolcode;

	@Column(name = "scolumnname", length = 50, nullable = false)
	private String scolumnname;

	@Column(name = "sheadername", length = 500, nullable = false)
	private String sheadername;

	@Column(name = "nsortorder")
	private int nsortorder;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	@Override
	public InvoiceExportHeader mapRow(ResultSet arg0, int arg1) throws SQLException {
		InvoiceExportHeader objTaxtype1 = new InvoiceExportHeader();
		objTaxtype1.setNimpconfigcode(getInteger(arg0, "nimpconfigcode", arg1));
		objTaxtype1.setStablename(getString(arg0, "stablename", arg1));
		objTaxtype1.setNformcode(getInteger(arg0, "nformcode", arg1));
		objTaxtype1.setNcontrolcode(getInteger(arg0, "ncontrolcode", arg1));
		objTaxtype1.setScolumnname(getString(arg0, "scolumnname", arg1));
		objTaxtype1.setSheadername(getString(arg0, "sheadername", arg1));
		objTaxtype1.setNsortorder(getInteger(arg0, "nsortorder", arg1));
		objTaxtype1.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objTaxtype1.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objTaxtype1.setNstatus(getShort(arg0, "nstatus", arg1));

		return objTaxtype1;
	}

}
