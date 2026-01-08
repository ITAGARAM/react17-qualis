package com.agaramtech.qualis.biobank.service.BioEcatalogueReqApproval;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface BioEcatalogueReqApprovalService {

	ResponseEntity<Object> getBioEcatalogueReqApproval(Map<String, Object> inputMap, int nbioRequestBasedTransferCode,
			UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getSelectedBioEcatalogueReqApproval(int nrequestapprovlcode,int ntransfertype,int ntransactionstatus, UserInfo userInfo)  throws Exception;

	public ResponseEntity<Object> updatedUserAcceptedVolume(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateRejectedRecord(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateApproveRecord(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getFilteredRequestDetails(int nbioprojectcode, int nproductcode,
			String sparentsamplecode, int ntransfertypecode, int nsitecode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getBioEcatalogueReqApprovalDetails(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;
	

}
