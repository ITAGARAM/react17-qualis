package com.agaramtech.qualis.testmanagement.model;

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

@Table(name = "testsubcontractor")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestSubContractor extends CustomizedResultsetRowMapper<TestSubContractor>
		implements Serializable, RowMapper<TestSubContractor> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestsubcontractorcode")
	private int ntestsubcontractorcode;

	@Column(name = "ntestcode")
	private int ntestcode;

//	@Column(name="nsuppliercatcode")
//	private int nsuppliercatcode;

	@Column(name = "nsuppliercode")
	private int nsuppliercode;

	@Column(name = "ncontrolleadtime")
	private short ncontrolleadtime;

	@Column(name = "nperiodcode")
	private short nperiodcode;

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus")
	private short ndefaultstatus;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode;

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Transient
	private transient String ssuppliername;
	@Transient
	private transient String sperioname;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String scontrolleadtime;

	@Override
	public TestSubContractor mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TestSubContractor objTestSubContractor = new TestSubContractor();
		objTestSubContractor.setNtestsubcontractorcode(getInteger(arg0, "ntestsubcontractorcode", arg1));
		objTestSubContractor.setNtestcode(getInteger(arg0, "ntestcode", arg1));
		// objTestSubContractor.setNsuppliercatcode(getInteger(arg0,"nsuppliercatcode",arg1));
		objTestSubContractor.setNsuppliercode(getInteger(arg0, "nsuppliercode", arg1));
		objTestSubContractor.setNcontrolleadtime(getShort(arg0, "ncontrolleadtime", arg1));
		objTestSubContractor.setNperiodcode(getShort(arg0, "nperiodcode", arg1));
		objTestSubContractor.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objTestSubContractor.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objTestSubContractor.setNstatus(getShort(arg0, "nstatus", arg1));
		objTestSubContractor.setSsuppliername(getString(arg0, "ssuppliername", arg1));
		objTestSubContractor.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objTestSubContractor.setSperioname(getString(arg0, "sperioname", arg1));
		objTestSubContractor.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objTestSubContractor.setScontrolleadtime(getString(arg0, "scontrolleadtime", arg1));
		return objTestSubContractor;
	}

}
