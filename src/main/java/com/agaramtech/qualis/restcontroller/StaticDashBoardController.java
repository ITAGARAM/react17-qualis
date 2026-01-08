package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.dashboard.service.staticdashboard.StaticDashBoardService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant method to access
 * the StaticDashBoardController Service methods.
 */
@RestController
@RequestMapping("/staticdashboard")
public class StaticDashBoardController {

	private static final Logger LOGGER = LoggerFactory.getLogger(StaticDashBoardController.class);
	
	
	private StaticDashBoardService staticDashBoardService;
	private RequestContext requestContext;
	
	public StaticDashBoardController(RequestContext requestContext, StaticDashBoardService staticDashBoardService) {
		super();
		this.requestContext = requestContext;
		this.staticDashBoardService = staticDashBoardService;
	}
	
	@PostMapping(value = "/getListStaticDashBoard")
	public ResponseEntity<Object> getListStaticDashBoard(@RequestBody Map<String, Object> inputMap) throws Exception{

		LOGGER.info("getListStaticDashBoard");	
//		try {
			final ObjectMapper objmapper = new ObjectMapper();				
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);		
			requestContext.setUserInfo(userInfo);
			return staticDashBoardService.getListStaticDashBoard(userInfo);
//		} 
//		catch (Exception e) {
//			LOGGER.error(e.getMessage());			
//			System.out.println(e);
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		} 
	}
	
	@PostMapping(value = "/getStaticDashBoard")
	public ResponseEntity<Object> getStaticDashBoard(@RequestBody Map<String, Object> inputMap) throws Exception{

//		try {
			final ObjectMapper objmapper = new ObjectMapper();
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final int nstaticDashBoardCode = (Integer) inputMap.get("nstaticDashBoardCode");
			final int nstaticDashBoardMasterCode = 0;
			final Map<String, Object> sparamValue = (Map<String, Object>) inputMap.get("sparamValue");
			requestContext.setUserInfo(userInfo);
			return staticDashBoardService.getStaticDashBoard(userInfo, nstaticDashBoardCode, nstaticDashBoardMasterCode, sparamValue);
//		} 
//		catch (Exception e) {
//			LOGGER.error(e.getMessage());			
//			System.out.println(e);
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		} 
	}
	@SuppressWarnings("unchecked")
	@PostMapping(value = "/getSelectionStaticDashBoard")
	public ResponseEntity<Object> getSelectionStaticDashBoard(@RequestBody Map<String, Object> inputMap) throws Exception{

		//try {
			final ObjectMapper objmapper = new ObjectMapper();	
			final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
			final int nstaticDashBoardCode = (Integer) inputMap.get("nstaticDashBoardCode");			
			final Map<String, Object> sparamValue = (Map<String, Object>) inputMap.get("sparamValue");
			return staticDashBoardService.getSelectionStaticDashBoard(userInfo, nstaticDashBoardCode, sparamValue);
//		} 
//		catch (Exception e) {
//			LOGGER.error(e.getMessage());			
//			System.out.println(e);
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		} 
	}
}
