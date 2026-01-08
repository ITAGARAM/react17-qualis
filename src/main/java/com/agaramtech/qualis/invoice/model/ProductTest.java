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
 * This class is used to map the fields of 'producttest' table of the Database.
 */
@Entity
@Table(name = "producttest")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProductTest extends CustomizedResultsetRowMapper<ProductTest>
		implements Serializable, RowMapper<ProductTest> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nproducttestcode")
	private int nproducttestcode;

	@Column(name = "ninvproductcode")
	private int ninvproductcode;

	@Column(name = "nlimsproductcode")
	private int nlimsproductcode;

	@Column(name = "sproductname", length = 100, nullable = false)
	private String sproductname;

	@Column(name = "sspecname", length = 100, nullable = false)
	private String sspecname;

	@Column(name = "ntestcode")
	private int ntestcode;

	@Column(name = "sproducttestname", length = 100, nullable = false)
	private String sproducttestname;

	@Column(name = "ntestcost", nullable = false)
	private double ntestcost;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nmastersitecode", nullable = false)
	@ColumnDefault("-1")
	private short nmastersitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	@Override
	public ProductTest mapRow(ResultSet arg0, int arg1) throws SQLException {
		ProductTest objProductTest = new ProductTest();
		objProductTest.setNproducttestcode(getInteger(arg0, "nproducttestcode", arg1));
		objProductTest.setNlimsproductcode(getInteger(arg0, "nlimsproductcode", arg1));
		objProductTest.setSproductname(getString(arg0, "sproductname", arg1));
		objProductTest.setSspecname(getString(arg0, "sspecname", arg1));
		objProductTest.setSproducttestname(getString(arg0, "sproducttestname", arg1));
		objProductTest.setNtestcost(getDouble(arg0, "ntestcost", arg1));
		objProductTest.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objProductTest.setNmastersitecode(getShort(arg0, "nmastersitecode", arg1));
		objProductTest.setNstatus(getShort(arg0, "nstatus", arg1));
		objProductTest.setNtestcode(getInteger(arg0, "ntestcode", arg1));
		objProductTest.setNinvproductcode(getInteger(arg0, "ninvproductcode", arg1));
		return objProductTest;
	}

}
