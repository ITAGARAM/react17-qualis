package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.auditplan.model.AuditMaster;
import com.agaramtech.qualis.auditplan.service.auditmaster.AuditMasterService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;



/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the AuditMaster Service methods.
 */
/**
* @author AT-E143 SWSM-2 22/07/2025
*/
 
@RestController
@RequestMapping("/auditmaster")
public class AuditMasterController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AuditMasterController.class);

	private RequestContext requestContext;
	private final AuditMasterService auditMasterService;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param auditMasterService AuditMasterService
	 */
	public AuditMasterController(RequestContext requestContext, AuditMasterService auditMasterService) {
		super();
		this.requestContext = requestContext;
		this.auditMasterService = auditMasterService;
	}	
	/**
	 * This method is used to retrieve list of available auditmaster(s). 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}				
	 * @return response entity  object holding response status and list of all auditmaster
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getAuditMaster")
	public ResponseEntity<Object> getAuditMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return auditMasterService.getAuditMaster(userInfo);
	}	
	/**
	 * This method will is used to make a new entry to auditmaster table.
	 * @param inputMap map object holding params ( 
	 * 								auditmaster [AuditMaster]  object holding details to be added in auditmaster table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "auditmaster": { "sauditorname": "syed", "sdepartment": "vp","semail":"syed@gmail.com","nleadauditor":3,"sskilldetails":"J2ee" },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 33,"nmastersitecode": -1,"nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with list of auditmaster along with the newly added auditmaster.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createAuditMaster")
	public ResponseEntity<Object> createAuditMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final AuditMaster objAuditMaster = objMapper.convertValue(inputMap.get("auditmaster"), new TypeReference<AuditMaster>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 	new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return auditMasterService.createAuditMaster(objAuditMaster, userInfo);
	}
	/**
	 * This method is used to update selected auditmaster details.
	 * @param inputMap [map object holding params(
	 * 					auditmaster [AuditMaster]  object holding details to be updated in auditmaster table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"auditmaster": {"nauditmastercode":1, "sauditorname": "syed ibrahim", "sdepartment": "vp","semail":"syed@gmail.com","nleadauditor":3,"sskilldetails":"J2ee" },
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}

	 * 	  		
	 * @return ResponseEntity with string message as 'Already Deleted' if the auditmaster record is not available/ 
	 * 			list of all auditmaster and along with the updated auditmaster.	 
	 * @throws Exception exception
	 */	
	@PostMapping(value = "/updateAuditMaster")
	public ResponseEntity<Object> updateAuditMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());		
		final AuditMaster objAuditMaster = objMapper.convertValue(inputMap.get("auditmaster"), new TypeReference<AuditMaster>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return auditMasterService.updateAuditMaster(objAuditMaster, userInfo);	
	}
	/**
	 * This method is used to delete an entry in auditmaster table
	 * @param inputMap [Map] object with keys of AuditMaster entity and UserInfo object.
	 * 					Input:{
     						"auditmaster": {"nauditmastercode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 33,"nmastersitecode": -1, "nmodulecode": 1,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the auditmaster record is not available/ 
	 * 			string message as 'Record is used in....' when the auditmaster is associated in transaction /
	 * 			list of all auditmaster excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteAuditMaster")
	public ResponseEntity<Object> deleteAuditMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		
		final AuditMaster objAuditMaster = objMapper.convertValue(inputMap.get("auditmaster"), new TypeReference<AuditMaster>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return auditMasterService.deleteAuditMaster(objAuditMaster, userInfo);
	}
	/**
	 * This method is used to retrieve a specific auditmaster record.
	 * @param inputMap  [Map] map object with "nauditmastercode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nauditmastercode": 1,
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
	 * @return ResponseEntity with AuditMaster object for the specified primary key / with string message as
	 * 						'Deleted' if the auditmaster record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveAuditMasterById")
	public ResponseEntity<Object> getActiveAuditMasterById(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final int nauditMasterCode = (int) inputMap.get("nauditmastercode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return auditMasterService.getActiveAuditMasterById(nauditMasterCode, userInfo);
	}
}
