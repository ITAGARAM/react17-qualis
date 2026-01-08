package com.agaramtech.qualis.biobank.model;

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


@Entity
@Table(name = "seqnobiobankmanagement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqNoBioBankManagement extends CustomizedResultsetRowMapper<SeqNoBioBankManagement>  implements Serializable, RowMapper<SeqNoBioBankManagement> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "stablename", length = 255, nullable = false)
	private String stablename;
	
	@Column(name = "nsequenceno", nullable = false)
	private int nsequenceno;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SeqNoBioBankManagement mapRow(ResultSet arg0, int arg1) throws SQLException {
		SeqNoBioBankManagement objseq = new SeqNoBioBankManagement();
		objseq.setStablename(getString(arg0,"stablename",arg1));
		objseq.setNsequenceno(getInteger(arg0,"nsequenceno",arg1));
		objseq.setNstatus(getShort(arg0,"nstatus",arg1));
		return objseq;
	}
}
