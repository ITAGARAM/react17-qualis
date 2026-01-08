package com.agaramtech.qualis.biobank.service.bgsiexternalapi;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface BGSIExternalApiService {

	ResponseEntity<Object> updatepatientconsent(final Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception;

	ResponseEntity<Object> bioExternalTransferData(final UserInfo objUserInfo) throws Exception;
	
	ResponseEntity<Object> acknowledgeTransferred(final Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception;
	
	ResponseEntity<Object> receiveOriginalSample(final Map<String, Object> inputMap, final UserInfo objUserInfo) throws Exception;
	
	String getAuthorisationTokenForNGS(final UserInfo objUserInfo) throws Exception;

	ResponseEntity<Object> sendAcceptedThirdPartySamplestoNGS(final Map<String, Object> inputMap,final UserInfo userinfo) throws Exception;
	public ResponseEntity<Object> getTotalRepoIdBasedSampleType(final Map<String, Object> inputMap) throws Exception; 
	
	public ResponseEntity<Object> getTotalRepoIdRequestedAndSent(final Map<String, Object> inputMap) throws Exception; 

	ResponseEntity<Object> getTotalSubjectid(final Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> getStorageCapacity(final Map<String, Object> inputMap) throws Exception; 
	
	ResponseEntity<Object> receiveDNASample(final Map<String, Object> inputMap, final UserInfo objUserInfo) throws Exception;
}
