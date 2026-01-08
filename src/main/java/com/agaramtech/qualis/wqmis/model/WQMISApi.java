package com.agaramtech.qualis.wqmis.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wqmisapi")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WQMISApi extends CustomizedResultsetRowMapper<WQMISApi> implements Serializable, RowMapper<WQMISApi> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nwqmisapicode", nullable = false)
	private short nwqmisapicode;

	@Column(name = "sapitype", length = 10, nullable = false)
	private String sapitype;

	@Column(name = "surl", length = 150, nullable = false)
	private String surl;

	@Column(name = "smethodurl", length = 100, nullable = false)
	private String smethodurl;

	@Column(name = "stablename", length = 100, nullable = false)
	private String stablename;

	@Column(name = "stokenid", length = 1000, nullable = false)
	private String stokenid;

	@Column(name = "dsyncstartdate")
	private Instant dsyncstartdate;

	@Column(name = "dsyncenddate")
	private Instant dsyncenddate;

	@Column(name = "nneedautosync", nullable = false)
	@ColumnDefault("4")
	private short nneedautosync;

	@Column(name = "nsorter", nullable = false)
	@ColumnDefault("-1")
	private short nsorter;

	@Column(name = "nismastertable", nullable = false)
	@ColumnDefault("3")
	private short nismastertable;

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "nregtypecode")
	private short nregtypecode;
	
	@Column(name = "nregsubtypecode")
	private short nregsubtypecode;

	@Transient
	private transient String ssyncstartdate;

	@Transient
	private transient String ssyncenddate;

	@Override
	public WQMISApi mapRow(ResultSet arg0, int arg1) throws SQLException {
		final WQMISApi objbioproject = new WQMISApi();
		objbioproject.setNwqmisapicode(getShort(arg0, "nwqmisapicode", arg1));
		objbioproject.setSapitype(getString(arg0, "sapitype", arg1));
		objbioproject.setSurl(getString(arg0, "surl", arg1));
		objbioproject.setSmethodurl(getString(arg0, "smethodurl", arg1));
		objbioproject.setStablename(getString(arg0, "stablename", arg1));
		objbioproject.setStokenid(getString(arg0, "stokenid", arg1));
		objbioproject.setDsyncstartdate(getInstant(arg0, "dsyncstartdate", arg1));
		objbioproject.setDsyncenddate(getInstant(arg0, "dsyncenddate", arg1));
		objbioproject.setNneedautosync(getShort(arg0, "nneedautosync", arg1));
		objbioproject.setNsorter(getShort(arg0, "nsorter", arg1));
		objbioproject.setNismastertable(getShort(arg0, "nismastertable", arg1));
		objbioproject.setNstatus(getShort(arg0, "nstatus", arg1));
		objbioproject.setNregtypecode(getShort(arg0, "nregtypecode", arg1));
		objbioproject.setNregsubtypecode(getShort(arg0, "nregsubtypecode", arg1));
		objbioproject.setSsyncstartdate(getString(arg0, "ssyncstartdate", arg1));
		objbioproject.setSsyncenddate(getString(arg0, "ssyncenddate", arg1));
		return objbioproject;
	}

}
