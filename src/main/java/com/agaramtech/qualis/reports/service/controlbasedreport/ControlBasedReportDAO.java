package com.agaramtech.qualis.reports.service.controlbasedreport;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.reports.model.ReportMaster;

public interface ControlBasedReportDAO {

	public ResponseEntity<Object> generateControlBasedReport(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;
	
	// Added by Gowtham on Oct 4 to access this method in SampleRequestingDAOImpl for SampleScheduling jira-id:SWSM-77
	public Map<String, Object> controlBasedReportGeneration(ReportMaster reportMaster, Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> controlBasedReportParameter(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> controlBasedReportparametretable(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> controlBasedReportparametretablecolumn(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> downloadControlBasedReportparametreInsert(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception;

}
