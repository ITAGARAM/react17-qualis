package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'invoiceproducttype' table of the
 * Database.
 */
@Entity
@Table(name = "invoiceproducttype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceProductType extends CustomizedResultsetRowMapper<InvoiceProductType>
		implements Serializable, RowMapper<InvoiceProductType> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ntypecode")
	private int ntypecode;

	@Column(name = "stypename", length = 50, nullable = false)
	private String stypename;

	@Column(name = "sdescription", length = 500)
	private String sdescription;

	@Column(name = "nactive", nullable = false)
	@ColumnDefault("4")
	private int nactive = 4;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	private transient String sdisplaystatus;

	@Override

	public InvoiceProductType mapRow(ResultSet arg0, int arg1) throws SQLException {
		InvoiceProductType objInvoiceProductType = new InvoiceProductType();
		objInvoiceProductType.setNtypecode(getInteger(arg0, "ntypecode", arg1));
		objInvoiceProductType.setStypename(getString(arg0, "stypename", arg1));
		objInvoiceProductType.setSdescription(getString(arg0, "sdescription", arg1));
		objInvoiceProductType.setNactive(getInteger(arg0, "nactive", arg1));
		objInvoiceProductType.setNusercode(getInteger(arg0, "nusercode", arg1));
		objInvoiceProductType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInvoiceProductType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInvoiceProductType.setNstatus(getShort(arg0, "nstatus", arg1));
		objInvoiceProductType.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));

		return objInvoiceProductType;

	}

}
