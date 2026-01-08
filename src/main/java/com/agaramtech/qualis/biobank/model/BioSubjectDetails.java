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

@Entity
@Table(name = "biosubjectdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BioSubjectDetails extends CustomizedResultsetRowMapper<BioSubjectDetails> implements Serializable, RowMapper<BioSubjectDetails>{

	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nbiosubjectdetailcode")
	private int nbiosubjectdetailcode;

	@Column(name = "ssubjectid", length = 100,nullable = false)
	private String ssubjectid;

	@Column(name = "scasetype", length = 150,nullable = false)
	private String scasetype;
	
	@Column(name = "sgendername", length = 100,nullable = false)
	private String sgendername;
	
	@Column(name = "nisthirdpartysharable")
	private int nisthirdpartysharable;
	
	@Column(name = "nissampleaccesable")
	private int nissampleaccesable;
	
    @Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Override
	public BioSubjectDetails mapRow(ResultSet arg0, int arg1) throws SQLException {
		final BioSubjectDetails objSubjectDetail = new BioSubjectDetails();

		objSubjectDetail.setNbiosubjectdetailcode(getInteger(arg0, "nbiosubjectdetailcode", arg1));
		objSubjectDetail.setSsubjectid(getString(arg0, "ssubjectid", arg1));
		objSubjectDetail.setScasetype(getString(arg0, "scasetype", arg1));
		objSubjectDetail.setSgendername(getString(arg0, "sgendername", arg1));
		objSubjectDetail.setNisthirdpartysharable(getInteger(arg0, "nisthirdpartysharable", arg1));
		objSubjectDetail.setNissampleaccesable(getInteger(arg0, "nissampleaccesable", arg1));
		objSubjectDetail.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSubjectDetail.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSubjectDetail.setNstatus(getShort(arg0, "nstatus", arg1));
		
		return objSubjectDetail;

	}
	
	
}
