package com.agaramtech.qualis.auditplan.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'seqnoauditplan' table of the Database.
 */
/**
* @author AT-E143 SWSM-6 05/08/2025
*/
@Entity
@Table(name = "seqnoauditplan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqNoAuditPlan extends CustomizedResultsetRowMapper<SeqNoAuditPlan> implements Serializable,RowMapper<SeqNoAuditPlan>{
	
private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "stablename", length = 30, nullable = false)
	private String stablename;

	@Column(name = "nsequenceno", nullable = false)
	private int nsequenceno;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	
	public SeqNoAuditPlan mapRow(ResultSet arg0, int arg1) throws SQLException {
		SeqNoAuditPlan objseq = new SeqNoAuditPlan();
		
		objseq.setStablename(getString(arg0,"stablename",arg1));
		objseq.setNsequenceno(getInteger(arg0,"nsequenceno",arg1));
		objseq.setNstatus(getShort(arg0,"nstatus",arg1));
		return objseq;
	}

}