package com.agaramtech.qualis.invoice.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'producttestdetail' table of the
 * Database.
 */
@Entity
@Table(name = "producttestdetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProductTestDetail extends CustomizedResultsetRowMapper<ProductTestDetail>
		implements Serializable, RowMapper<ProductTestDetail> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nproducttestcode")
	private int nproducttestcode;

	@Column(name = "sproducttestdetail", length = 50, nullable = false)
	private String sproducttestdetail;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nmastersitecode", nullable = false)
	@ColumnDefault("-1")
	private short nmastersitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	@Override
	public ProductTestDetail mapRow(ResultSet arg0, int arg1) throws SQLException {

		final ProductTestDetail objProductTestDetail = new ProductTestDetail();

		objProductTestDetail.setNproducttestcode(getInteger(arg0, "nproducttestcode", arg1));
		objProductTestDetail
				.setSproducttestdetail(StringEscapeUtils.unescapeJava(getString(arg0, "sproducttestdetail", arg1)));
		objProductTestDetail.setNstatus(getShort(arg0, "nstatus", arg1));
		objProductTestDetail.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objProductTestDetail.setNmastersitecode(getShort(arg0, "nmastersitecode", arg1));

		return objProductTestDetail;
	}

}
