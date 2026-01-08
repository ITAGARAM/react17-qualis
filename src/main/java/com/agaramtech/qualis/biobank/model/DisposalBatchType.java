package com.agaramtech.qualis.biobank.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'disposalbatchtype' table of the
 * Database.
 */

@Entity
@Table(name = "disposalbatchtype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DisposalBatchType extends CustomizedResultsetRowMapper<DisposalBatchType>
implements Serializable, RowMapper<DisposalBatchType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndisposalbatchtypecode", nullable = false)
	private short ndisposalbatchtypecode;

	@Column(name = "sdisposalbatchtypename", length = 50, nullable = false)
	private String sdisposalbatchtypename;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public DisposalBatchType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final var objDisposalBatchType = new DisposalBatchType();

		objDisposalBatchType.setNdisposalbatchtypecode(getShort(arg0, "ndisposalbatchtypecode", arg1));
		objDisposalBatchType.setSdisposalbatchtypename(getString(arg0, "sdisposalbatchtypename", arg1));
		objDisposalBatchType.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objDisposalBatchType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objDisposalBatchType.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objDisposalBatchType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objDisposalBatchType.setNstatus(getShort(arg0, "nstatus", arg1));

		return objDisposalBatchType;
	}

}
