package com.agaramtech.qualis.audittrail.model;

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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'auditarchivaltype' table of the
 * Database.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 27-11- 2025
 */
@Entity
@Table(name = "auditarchivaltype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuditArchivalType extends CustomizedResultsetRowMapper<AuditArchivalType>
		implements Serializable, RowMapper<AuditArchivalType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nauditarchivaltypecode")
	private short nauditarchivaltypecode;

	@ColumnDefault("0")
	@Column(name = "nsorter", nullable = false)
	private short nsorter =( short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus =( short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dauditdate", nullable = false)
	private Instant dauditdate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=( short)Enumeration.TransactionStatus.NA.gettransactionstatus();	

	@Transient
	private transient String sdefaultname;
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String sauditarchivaltypename;

	@Override
	public AuditArchivalType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final AuditArchivalType auditarchivaltype = new AuditArchivalType();

		auditarchivaltype.setNauditarchivaltypecode(getShort(arg0, "nauditarchivaltypecode", arg1));
		auditarchivaltype.setNsorter(getShort(arg0, "nsorter", arg1));
		auditarchivaltype.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		auditarchivaltype.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		auditarchivaltype.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		auditarchivaltype.setNsitecode(getShort(arg0, "nsitecode", arg1));
		auditarchivaltype.setNstatus(getShort(arg0, "nstatus", arg1));
		auditarchivaltype.setSauditarchivaltypename(getString(arg0, "sauditarchivaltypename", arg1));
		auditarchivaltype.setDauditdate(getInstant(arg0, "dauditdate", arg1));

		return auditarchivaltype;
	}

}
