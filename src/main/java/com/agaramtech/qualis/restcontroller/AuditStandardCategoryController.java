package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.auditplan.model.AuditStandardCategory;
import com.agaramtech.qualis.auditplan.service.auditstandardcategory.AuditStandardCategoryService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the AuditStandardCategory Service methods.
 */
/**
 * @author sujatha.v
 * SWSM-1
 * 19/07/2025
 */
@RestController
@RequestMapping("/auditstandardcategory")
public class AuditStandardCategoryController {

	private final static Logger LOGGER= LoggerFactory.getLogger(AuditStandardCategoryController.class);
	private final AuditStandardCategoryService auditStandardCategoryService;
	private RequestContext requestContext;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param auditStandardCategoryService AuditStandardCategoryService
	 */
	public AuditStandardCategoryController(AuditStandardCategoryService auditStandardCategoryService,
			RequestContext requestContext) {
		super();
		this.auditStandardCategoryService = auditStandardCategoryService;
		this.requestContext = requestContext;
	}
	
	/**
	 * This method is used to retrieve list of available auditstandardcategory(s). 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity  object holding response status and list of all auditstandardcategory
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getAuditStandardCategory")
	public ResponseEntity<Object> getAuditStandardCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getAuditStandardCategory() called");
		requestContext.setUserInfo(userInfo);
		return auditStandardCategoryService.getAuditStandardCategory(userInfo);
	}
	
	/**
	 * This method is used to retrieve a specific auditstandardcategory record.
	 * @param inputMap  [Map] map object with "auditstandardcategory" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nauditstandardcatcode": 1,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 263,"nmastersitecode": -1,"nmodulecode": 81,
							                "nreasoncode": 0,"nsitecode": -1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with AuditStandardCategory object for the specified primary key / with string message as
	 * 						'Deleted' if the auditstandardcategory record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveAuditStandardCategoryById")
	public ResponseEntity<Object> getActiveAuditStandardCategoryById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final int nauditstandardcategory = (Integer) inputMap.get("nauditstandardcatcode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return auditStandardCategoryService.getActiveAuditStandardCategoryById(nauditstandardcategory, userInfo);
	}
	
	/**
	 * This method will is used to make a new entry to auditstandardcategory table.
	 * @param inputMap map object holding params ( 
	 * 								auditstandardcategory [AuditStandardCategory]  object holding details to be added in auditstandardcategory table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "auditstandardcategory": { "sauditstandardcatname": "cat1"},
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 263,"nmastersitecode": -1,"nmodulecode": 81,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with string message as 'Already Exists' if the auditstandardcategory already exists/ 
	 * 			list of auditstandardcategory's along with the newly added auditstandardcategory.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createAuditStandardCategory")
	public ResponseEntity<Object> createAuditStandardCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final AuditStandardCategory auditStandardCategory = objectMapper.convertValue(inputMap.get("auditstandardcategory"),
				new TypeReference<AuditStandardCategory>() {});
		requestContext.setUserInfo(userInfo);
		return auditStandardCategoryService.createAuditStandardCategory(auditStandardCategory, userInfo);
	}
	
	/**
	 * This method is used to update selected auditstandardcategory details.
	 * @param inputMap [map object holding params(
	 * 					auditstandardcategory [AuditStandardCategory]  object holding details to be updated in auditstandardcategory table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"auditstandardcategory": {"nauditstandardcatcode":1,"sauditstandardcatname": "cat2", "sdescription": "m" },
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 263,"nmastersitecode": -1, "nmodulecode": 81,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}

	 * 	  		
	 * @return ResponseEntity with string message as 'Already Deleted' if the auditstandardcategory record is not available/ 
	 * 			list of all auditstandardcategory's and along with the updated auditstandardcategory.	 
	 * @throws Exception exception
	 */	
	@PostMapping(value = "/updateAuditStandardCategory")
	public ResponseEntity<Object> updateAuditStandardCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final AuditStandardCategory auditStandardCategory = objectMapper.convertValue(inputMap.get("auditstandardcategory"),
				new TypeReference<AuditStandardCategory>() {});
		requestContext.setUserInfo(userInfo);
		return auditStandardCategoryService.updateAuditStandardCategory(auditStandardCategory, userInfo);
	}
	
	/**
	 * This method is used to delete an entry in AuditStandardCategory table
	 * @param inputMap [Map] object with keys of AuditStandardCategory entity and UserInfo object.
	 * 					Input:{
     						"auditstandardcategory": {"nauditstandardcatcode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 263,"nmastersitecode": -1, "nmodulecode": 81,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the auditstandardcategory record is not available/ 
	 * 			string message as 'Record is used in....' when the auditstandardcategory is associated in transaction /
	 * 			list of all auditstandardcategory's excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteAuditStandardCategory")
	public ResponseEntity<Object> deleteAuditStandardCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final AuditStandardCategory auditStandardCategory = objectMapper.convertValue(inputMap.get("auditstandardcategory"),
				new TypeReference<AuditStandardCategory>() {});
		requestContext.setUserInfo(userInfo);
		return auditStandardCategoryService.deleteAuditStandardCategory(auditStandardCategory, userInfo);
	}
}
