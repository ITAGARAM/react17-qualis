package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.wqmis.service.WQMISService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@RequestMapping("/wqmisftksamples")
public class WQMISFTKSamplesController {
	
	private final WQMISService wqmisService;
	private RequestContext requestContext;
	
	public WQMISFTKSamplesController(WQMISService wqmisService,RequestContext requestContext) {
		super();
		this.wqmisService = wqmisService;
		this.requestContext = requestContext;
	}
	
	@PostMapping(value = "/getWQMISFTKSamples")
	public ResponseEntity<Object> getWQMISFTKSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		String fromDate = "";
		String toDate="";
		
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String currentUIDate = (String)inputMap.get("currentdate");
		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		
		
		//LOGGER.info("myData:"+myData);
		return wqmisService.getWQMISFTKSamples(fromDate, toDate, currentUIDate, userInfo, inputMap);

	}
	//WQMISTransactionAPI BY DHIVYABHARATHI
	@PostMapping(value = "/getWQMISFTKSampleParametersDetails")
	public ResponseEntity<Object> getWQMISFTKSampleParametersDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		
		return wqmisService.getWQMISFTKSampleParametersDetails(inputMap , userInfo);

	}

}
