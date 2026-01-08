package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.wqmisrequestapi.service.WqmisRequestApiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
//Added by Dhivya Bharathi on 26th Nov 2025 for jira id:swsm-122
@RestController
@RequestMapping("/wqmismasterapiview")
public class WqmisRequestApiController {

	@Autowired
	private WqmisRequestApiService wqmisrequestapiservice;

	@Autowired
	private RequestContext requestContext;

	@PostMapping(value = "/getIntegrationApiDetails")
	public ResponseEntity<Object> getIntegrationApiDetails(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return wqmisrequestapiservice.getIntegrationApiDetails(userInfo);

	}

	@PostMapping("/createSourceSampleInfoFTKUserDemo")
	public ResponseEntity<Object> createSourceSampleInfoFTKUserDemo(@RequestBody Map<String, Object> inputList)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputList.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return wqmisrequestapiservice.createSourceSampleInfoFTKUserDemo(inputList, userInfo);
	}
	
	@PostMapping("/createSourceSampleInfoDeptUserDemo")
	public ResponseEntity<Object> createSourceSampleInfoDeptUserDemo(@RequestBody Map<String, Object> inputList)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputList.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return wqmisrequestapiservice.createSourceSampleInfoDeptUserDemo(inputList, userInfo);
	}
	
	@PostMapping("/createAPIIntegrationDetails")
	public ResponseEntity<Object> createAPIIntegrationDetails(@RequestBody Map<String, Object> inputList)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputList.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return wqmisrequestapiservice.createAPIIntegrationDetails(inputList, userInfo);
	}
	
}
