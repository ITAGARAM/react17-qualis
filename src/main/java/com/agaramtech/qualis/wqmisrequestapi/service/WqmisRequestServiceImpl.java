package com.agaramtech.qualis.wqmisrequestapi.service;


import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;

//Added by Dhivya Bharathi on 26th Nov 2025 for jira id:swsm-122
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class WqmisRequestServiceImpl implements WqmisRequestApiService {

	
	private final WqmisRequestApiDAO wqmisRequestapiDAO;

	public  WqmisRequestServiceImpl(WqmisRequestApiDAO wqmisRequestapiDAO) {
		// TODO Auto-generated constructor stub
		super();
		this.wqmisRequestapiDAO = wqmisRequestapiDAO;
	}
	

	@Transactional
	@Override
	public ResponseEntity<Object> getIntegrationApiDetails(UserInfo userInfo) throws Exception {
		
		return wqmisRequestapiDAO.getIntegrationApiDetails(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createSourceSampleInfoFTKUserDemo(Map<String, Object> inputList, UserInfo userInfo) throws Exception {
		return wqmisRequestapiDAO.createSourceSampleInfoFTKUserDemo(inputList, userInfo);
	}


	@Override
	public List<Map<String, Object>> getPendingSamples() throws Exception {
		return wqmisRequestapiDAO.getPendingSamples();
	}


	@Override
	public ResponseEntity<Object> createSourceSampleInfoDeptUserDemo(Map<String, Object> inputList, UserInfo userInfo) {
		return wqmisRequestapiDAO.createSourceSampleInfoDeptUserDemo(inputList,userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createAPIIntegrationDetails(Map<String, Object> inputList, UserInfo userInfo) throws Exception {
		return wqmisRequestapiDAO.createAPIIntegrationDetails(inputList,userInfo);
	}



}
