package com.agaramtech.qualis.release.model;
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
 * This class is used to map the fields of 'releasetestfilter' table of the Database.
 */
@Entity
@Table(name = "releasetestfilter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReleaseTestFilter extends CustomizedResultsetRowMapper<ReleaseTestFilter> implements Serializable,RowMapper<ReleaseTestFilter> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nreleasetestfiltercode")
	private int nreleasetestfiltercode;

	@Column(name = "sreleasetestfiltername", length=50, nullable = false)
	private String sreleasetestfiltername;
	
	@Lob
	@Column(name="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String sreleasetestfilter;
	
	
	@Override
	public ReleaseTestFilter mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final ReleaseTestFilter objReleaseTestFilter = new ReleaseTestFilter();
		
		objReleaseTestFilter.setNreleasetestfiltercode(getInteger(arg0,"nreleasetestfiltercode",arg1));
		objReleaseTestFilter.setSreleasetestfiltername(getString(arg0,"sreleasetestfiltername",arg1));
		objReleaseTestFilter.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objReleaseTestFilter.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objReleaseTestFilter.setSreleasetestfilter(getString(arg0,"sreleasetestfilter",arg1));
		objReleaseTestFilter.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objReleaseTestFilter.setNstatus(getShort(arg0,"nstatus",arg1));
		objReleaseTestFilter.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		
		return objReleaseTestFilter;
	}

}