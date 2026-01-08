package com.agaramtech.qualis.restcontroller;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.basemaster.service.unit.UnitService;
import com.agaramtech.qualis.biobank.service.bgsitransfer.BGSITransferService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the BGSITransfer Service methods.
 */
@RestController
@RequestMapping("/bgsitransfer")
public class BGSITransferController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BGSITransferController.class);

	private RequestContext requestContext;
	private final BGSITransferService transferService;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param transferService BGSITransferService
	 */
	public BGSITransferController(RequestContext requestContext, BGSITransferService transferService) {
		super();
		this.requestContext = requestContext;
		this.transferService = transferService;
	}
	
	/**
	 * This method is used to retrieve list of available transfer records. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity  object holding response status and list of all transfer records
	 * @throws Exception exception
	 */
	
	@PostMapping(value = "/getBGSITransfer")
	public ResponseEntity<Object> getBGSITransfer(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		
		
		return transferService.getBGSITransfer(userInfo);

	}
	
	/**
	 * This method is used to retrieve a specific transfer detail record.
	 * @param inputMap  [Map] map object with "ntransfercode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "ntransfercode": 1,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with Transfer object for the specified primary key .
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveBGSITransferById")
	public ResponseEntity<Object> getActiveTransferDertailsByTransferCode(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final int ntransferCode = (int) inputMap.get("ntransfercode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 
				new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		
		return transferService.getActiveTransferDertailsByTransferCode(ntransferCode, userInfo);
	}
}
