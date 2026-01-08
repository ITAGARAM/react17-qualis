package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'invoicecustomertype' table of the
 * Database.
 */
@Entity
@Table(name = "invoicecustomertype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvoiceCustomerType extends CustomizedResultsetRowMapper<InvoiceCustomerType>
		implements Serializable, RowMapper<InvoiceCustomerType> {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ntypecode")
	private int ntypecode;

	@Column(name = "scustomertypename", length = 50, nullable = false)
	private String scustomertypename;

	@Column(name = "nactive", nullable = false)
	@ColumnDefault("4")
	private int nactive = 4;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	@Override
	public InvoiceCustomerType mapRow(ResultSet arg0, int arg1) throws SQLException {
		InvoiceCustomerType objInvoiceCustomerType = new InvoiceCustomerType();
		objInvoiceCustomerType.setNtypecode(getInteger(arg0, "ntypecode", arg1));
		objInvoiceCustomerType.setScustomertypename(getString(arg0, "scustomertypename", arg1));
		objInvoiceCustomerType.setNactive(getInteger(arg0, "nactive", arg1));
		objInvoiceCustomerType.setNusercode(getInteger(arg0, "nusercode", arg1));
		objInvoiceCustomerType.setNstatus(getShort(arg0, "nstatus", arg1));

		return objInvoiceCustomerType;

	}

}