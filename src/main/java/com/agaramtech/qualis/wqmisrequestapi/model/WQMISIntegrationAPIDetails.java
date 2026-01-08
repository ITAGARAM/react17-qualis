package com.agaramtech.qualis.wqmisrequestapi.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
//Added by Mohammed Ashik on 26th Nov 2025 for jira id:swsm-122
@Entity
@Table(name = "wqmisintegrationapidetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WQMISIntegrationAPIDetails extends CustomizedResultsetRowMapper<WQMISIntegrationAPIDetails>
implements Serializable, RowMapper<WQMISIntegrationAPIDetails>{
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nwqmisintegrationapidetailcode")
	private int nwqmisintegrationapidetailcode;

	@Column(name = "ncoareporthistorycode", nullable = false)
	private int ncoareporthistorycode;
	
	@Column(name = "ncoaparentcode", nullable = false)
	private int ncoaparentcode;
	
	@Column(name = "nversionno", nullable = false)
	private int nversionno;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Column(name = "ntransactionstatus", nullable = false)
	private int ntransactionstatus;

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;
	
	@Column(name = "ntztransactiondate", nullable = false)
	private short ntztransactiondate;
	
	@Column(name = "noffsetdtransactiondate", nullable = false)
	private int noffsetdtransactiondate;
	
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode;
	
	@Column(name = "nstatus", nullable = false)
	private short nstatus;
	
	@Column(name = "sresponse", length = 500)
	private String sresponse;

	@Column(name = "sreferenceno", length=40)
	private String sreferenceno;
	
	@Column(name = "nregtypecode")
	private short nregtypecode;
	
	@Column(name = "nregsubtypecode")
	private short nregsubtypecode;
	
	
	@Override
	public WQMISIntegrationAPIDetails mapRow(ResultSet arg0, int arg1) throws SQLException {
		WQMISIntegrationAPIDetails objWQMISIntegrationAPIDetails = new WQMISIntegrationAPIDetails();
		objWQMISIntegrationAPIDetails.setNwqmisintegrationapidetailcode(getInteger(arg0, "nwqmisintegrationapidetailcode", arg1));
		objWQMISIntegrationAPIDetails.setNcoareporthistorycode(getInteger(arg0, "ncoareporthistorycode", arg1));
		objWQMISIntegrationAPIDetails.setNcoaparentcode(getInteger(arg0, "ncoaparentcode", arg1));
		objWQMISIntegrationAPIDetails.setNversionno(getInteger(arg0, "nversionno", arg1));
		objWQMISIntegrationAPIDetails.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objWQMISIntegrationAPIDetails.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objWQMISIntegrationAPIDetails.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objWQMISIntegrationAPIDetails.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objWQMISIntegrationAPIDetails.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objWQMISIntegrationAPIDetails.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objWQMISIntegrationAPIDetails.setNstatus(getShort(arg0, "nstatus", arg1));
		objWQMISIntegrationAPIDetails.setSresponse(getString(arg0, "sresponse", arg1));
		objWQMISIntegrationAPIDetails.setSreferenceno(getString(arg0, "sreferenceno", arg1));
		objWQMISIntegrationAPIDetails.setNregtypecode(getShort(arg0, "nregtypecode", arg1));
		objWQMISIntegrationAPIDetails.setNregsubtypecode(getShort(arg0, "nregsubtypecode", arg1));
		return objWQMISIntegrationAPIDetails;
	}


}
