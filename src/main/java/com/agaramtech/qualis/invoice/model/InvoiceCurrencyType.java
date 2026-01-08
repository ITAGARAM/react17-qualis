package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import org.apache.commons.text.StringEscapeUtils;
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
 * This class is used to map the fields of 'invoicecurrencytype' table of the
 * Database.
 */
@Entity
@Table(name = "invoicecurrencytype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class InvoiceCurrencyType extends CustomizedResultsetRowMapper<InvoiceCurrencyType>
		implements Serializable, RowMapper<InvoiceCurrencyType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ncurrencycode")
	private int ncurrencycode;

	@Column(name = "scurrency")
	private String scurrency;

	@Column(name = "ssymbol")
	private String ssymbol;

	@Column(name = "nactive")
	private int nactive;

	@Column(name = "nusercode")
	private int nusercode;

	@Column(name = "dmodifiedDate")
	private Instant dmodifiedDate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	private transient String sdisplaystatus;
	private transient String smodifieddate;

	@Override
	public InvoiceCurrencyType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final InvoiceCurrencyType objCurrencyType = new InvoiceCurrencyType();

		objCurrencyType.setNcurrencycode(getInteger(arg0, "ncurrencycode", arg1));
		objCurrencyType.setScurrency(StringEscapeUtils.unescapeJava(getString(arg0, "scurrency", arg1)));
		objCurrencyType.setSsymbol(StringEscapeUtils.unescapeJava(getString(arg0, "ssymbol", arg1)));
		objCurrencyType.setNactive(getInteger(arg0, "nactive", arg1));
		objCurrencyType.setNusercode(getInteger(arg0, "nusercode", arg1));
		objCurrencyType.setDmodifiedDate(getInstant(arg0, "dmodifiedDate", arg1));
		objCurrencyType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objCurrencyType.setNstatus(getShort(arg0, "nstatus", arg1));
		objCurrencyType.setSdisplaystatus(StringEscapeUtils.unescapeJava(getString(arg0, "sdisplaystatus", arg1)));
		objCurrencyType.setSmodifieddate(StringEscapeUtils.unescapeJava(getString(arg0, "smodifieddate", arg1)));

		return objCurrencyType;

	}

}