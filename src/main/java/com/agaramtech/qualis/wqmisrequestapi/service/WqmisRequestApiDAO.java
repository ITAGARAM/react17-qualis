package com.agaramtech.qualis.wqmisrequestapi.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface WqmisRequestApiDAO {

//	ResponseEntity<Object> Source_SampleInfo_DeptUser_Demo(Map<String, Object> inputMap);

	public ResponseEntity<Object> getIntegrationApiDetails(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createSourceSampleInfoFTKUserDemo(Map<String, Object> inputList, UserInfo userInfo) throws Exception;

	public List<Map<String, Object>> getPendingSamples() throws Exception;
	
	public ResponseEntity<Object> createAPIIntegrationDetails(Map<String, Object> inputList, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createSourceSampleInfoDeptUserDemo(Map<String, Object> inputList, UserInfo userInfo);






}
