package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.auditplan.model.AuditPlanAuditor;
import com.agaramtech.qualis.auditplan.model.AuditPlanMember;
import com.agaramtech.qualis.auditplan.service.auditplan.AuditPlanService;

import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the AuditPlan Service methods.
 */
/**
* @author AT-E143 SWSM-6 05/08/2025
*/
@RestController
@RequestMapping("/auditplan")
public class AuditPlanController {

	private final RequestContext requestContext;
	private final AuditPlanService  auditPlanService ;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param auditPlanService AuditPlanService
	 */	
	public AuditPlanController(RequestContext requestContext, AuditPlanService auditPlanService) {
		super();
		this.requestContext = requestContext;
		this.auditPlanService = auditPlanService;
	}
	
	/**
	 * This method calls the getFilterStatus and getAuditPlanData methods and is used to retrieve list of available auditplan and transactionstatus.. 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * @return response entity object holding response status and list of all auditplan and transactionstatus table.
	 * @throws Exception exception
	 */
	@PostMapping(path = "/getAuditPlan")
	public ResponseEntity<Object> getAuditPlan(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditPlanService.getAuditPlan(inputMap, userInfo);
	}	
	/**
	 * This method is used to retrieve list of available auditplan. 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * @param inputMap [Map] map object with consist of From Date,To Date,Current Date
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity object holding response status and list of all auditplan
	 * @throws Exception exception
	 */
	@PostMapping(path = "/getAuditPlanData")
	public ResponseEntity<Object> getAuditPlanData(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
	
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditPlanService.getAuditPlanData(inputMap,userInfo);
	}	
	/**
	 * This method is used to retrieve list of available auditmaster. 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * @return response entity object holding response status and list of all auditmaster
	 * @throws Exception exception
	 */
	@PostMapping(path = "/getAuditMaster")
	public Map<String,Object> getAuditMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditPlanService.getAuditMaster(inputMap, userInfo);
	}	
	/**
	 * This method is used to retrieve list of available audittype. 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * @return response entity object holding response status and list of all audittype
	 * @throws Exception exception
	 */
	@PostMapping(path = "/getAuditType")
	public Map<String,Object> getAuditType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditPlanService.getAuditType(inputMap, userInfo);
	}
	/**
	 * This method is used to retrieve list of available auditcategory. 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * @return response entity object holding response status and list of all auditcategory
	 * @throws Exception exception
	 */
	@PostMapping(path = "/getAuditCategory")
	public Map<String,Object> getAuditCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditPlanService.getAuditCategory(inputMap, userInfo);
	}
	/**
	 * This method is used to retrieve list of available auditstandardcategory. 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * @return response entity object holding response status and list of all auditstandardcategory
	 * @throws Exception exception
	 */
	@PostMapping(path = "/getAuditStandardCategory")
	public Map<String,Object> getAuditStandardCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditPlanService.getAuditStandardCategory(inputMap, userInfo);
	}
	/**
	 * This method is used to retrieve list of available department. 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * @return response entity object holding response status and list of all department
	 * @throws Exception exception
	 */
	@PostMapping(path = "/getDepartment")
	public Map<String,Object> getDepartment(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditPlanService.getDepartment(inputMap,userInfo);
	}
	/**
	 * This method is used to retrieve list of available users based on the department. 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * @return response entity object holding response status and list of all users based on the department
	 * @throws Exception exception
	 */
	@PostMapping(path = "/getDepartmentHead")
	public Map<String,Object> getDepartmentHead(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		int ndepartmentCode = (int) inputMap.get("ndeptcode");		
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditPlanService.getDepartmentHead(ndepartmentCode, userInfo);
	}
	/**
	 * This method is used to retrieve list of available users. 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * @return response entity object holding response status and list of all users
	 * @throws Exception exception
	 */
	@PostMapping(path = "/getUsers")
	public Map<String,Object> getUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditPlanService.getUsers(inputMap,userInfo);
	}
	/**
	 * This method is used to make a new entry to "auditplan","auditplanhistory","auditplanmembers","auditplanauditors" table.
	 * Need to check for duplicate entry of saudittilte for the specified site.
	 * @param inputMap map object holding params ( 
	 * 								auditplan [AuditPlan]  object holding details to be added in auditplan table,
	 * 	 							auditplanhistory [AuditPlanHistory]  object holding details to be added in auditplanhistory table,
	 * 								auditplanmember [AuditPlanMember]  object holding details to be added in auditplanmembers table,
	 * 								auditplanauditor [AuditPlanAuditor]  object holding details to be added in auditplanauditors table,

	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "auditplan": { "naudittypecode":1,"nauditcategorycode":1,"nauditstandardcatcode":1,"ndeptcode":1,
									    			   "ndeptheadcode":1,"saudittitle":"IT Auditor","scompauditrep":"ret","sauditid":"-", 
									    			   "dauditdatetime":"2025-08-07","ntzauditdatetime":-1,"noffsetdauditdatetime":0},
									    "auditplanhistory": { "sremarks": "" },
									    "auditplanmember": { "nusercode": 1},
									    "auditplanauditor": { "nauditmastercode": 1 },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 266,"nmastersitecode": -1,"nmodulecode": 81,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with list of auditplan along with the newly added auditplan.
	 * @throws Exception exception
	 */
	@PostMapping(path = "/createAuditPlan")
	public ResponseEntity<Object> createAuditPlan(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		objMapper.registerModule(new JavaTimeModule());
		requestContext.setUserInfo(userInfo);
		return auditPlanService.createAuditPlan(inputMap,userInfo);
	}
	/**
	 * This method is used to retrieve a specific auditplan record.
	 * @param inputMap  [Map] map object with "nauditplancode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nauditplancode": 1,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 266,"nmastersitecode": -1,"nmodulecode": 81,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with AuditPlan object for the specified primary key /
	 * 			 with string message as	'Already Deleted' if the auditplan record is not available/
	 * 			 with string message as	'Select Draft Record Only' if the auditplan record status is not draft
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveAuditPlanById")
	public ResponseEntity<Object> getActiveAuditPlanById(@RequestBody Map<String, Object> inputMap) throws Exception {		
		final ObjectMapper objMapper = new ObjectMapper();
		final int nauditPlanCode = (int) inputMap.get("nauditplancode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return auditPlanService.getActiveAuditPlanById(nauditPlanCode, userInfo);
	}
	/**
	 * This method is used to retrieve a specific auditplan record.
	 * @param inputMap  [Map] map object with "nauditplancode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nauditplancode": 1,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 266,"nmastersitecode": -1,"nmodulecode": 81,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with AuditPlan object for the specified primary key /
	 * 			 with string message as 'Already Deleted' if the auditplan record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSelectionAuditPlanById")
	public ResponseEntity<Object> getSelectionAuditPlanById(@RequestBody Map<String, Object> inputMap) throws Exception {		
		final ObjectMapper objMapper = new ObjectMapper();
		final int nauditPlanCode = (int) inputMap.get("nauditplancode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return auditPlanService.getSelectionAuditPlanById(nauditPlanCode, userInfo);
	}
	/**
	 * This method is used to update selected auditplan details.
	 * Need to check for duplicate entry of saudittilte for the specified site.

	 * @param inputMap [map object holding params(
	 * 					auditplan [AuditPlan]  object holding details to be updated in auditplan table,
	 * 								userinfo [UserInfo] holding logged in user details) 
	 * 					Input:{
     						"auditplan": {"nauditplancode":1,"naudittypecode":1,"nauditcategorycode":1,"nauditstandardcatcode":1,"ndeptcode":1,"ndeptheadcode":1,
     									  "saudittitle":"IT Auditor","scompauditrep":"sffew" },
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 266,"nmastersitecode": -1, "nmodulecode": 81,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * 	  		
	 * @return ResponseEntity with string message as 'Already Deleted' if the auditplan record is not available/
	 *          with string message as 'Select Draft Record Only' if the auditplan record status is not draft/ 
	 * 			list of all auditplan and along with the updated auditplan.	 
	 * @throws Exception exception
	 */	
	@PostMapping(value = "/updateAuditPlan")
	public ResponseEntity<Object> updateAuditPlan(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return auditPlanService.updateAuditPlan(inputMap, userInfo);
	}
	/**
	 * This method is used to delete an entry in "auditplan","auditplanhistory","auditplanmembers","auditplanauditors","auditplanfile" table
	 * @param inputMap [Map] object with keys of AuditPlan entity and UserInfo object.
	 * 					Input:{
     						"auditplan": {"nauditplancode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 266,"nmastersitecode": -1, "nmodulecode": 81,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the auditplan record is not available/ 
	 * 			with string message as 'Select Draft Record Only' if the auditplan record is status is not draft /
	 * 			list of all auditplan excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteAuditPlan")
	public ResponseEntity<Object> deleteAuditPlan(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return auditPlanService.deleteAuditPlan(inputMap, userInfo);
	}
	/**
	 * This method is used to retrieve list of Audit Date & Time,Scheduled,Rescheduled,Closed Based on the transaction status. 
	 * @param inputMap  [Map] map object with "soperation" and "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list of all data based on the transactionstatus
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getTransactionDates")
	public ResponseEntity<Object> getTransactionDates(@RequestBody Map<String, Object> inputMap) throws Exception {		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);		
		return auditPlanService.getTransactionDates(inputMap, userInfo);
	}
	/**
	 * This method is used to make a new entry to "auditplanhistory" table and to update selected auditplan table .
	 * @param inputMap map object holding params ( 
	 * 								auditplan [AuditPlan]  object holding details to be added in auditplan table,
	 * 	 							auditplanhistory [AuditPlanHistory]  object holding details to be added in auditplanhistory table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "auditplan": { "nauditplancode": 1, "dauditdatetime": "2025-08-07","ntzdauditdatetime":-1,"noffsetdauditdatetime":0 },
									    "auditplanhistory": { "sremarks": "scheduled" },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 266,"nmastersitecode": -1,"nmodulecode": 81,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with string message as 'Already Deleted' if the auditplan record is not available/
	 *          with string message as 'Select Draft Record Only' if the auditplan record  status is not draft/ 
	 *          and return select schedule record only while trying to reschedule the draft or closed record/ 
	 * 			list of all auditplan and along with the updated auditplan.	 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/scheduleAuditPlan")
	public ResponseEntity<Object> scheduleAuditPlan(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return auditPlanService.scheduleAuditPlan(inputMap, userInfo);
	}
	/**
	 * This method is used to make a new entry to "auditplanhistory" table and to update selected auditplan table .
	 * @param inputMap map object holding params ( 
	 * 								auditplan [AuditPlan]  object holding details to be added in auditplan table,
	 * 	 							auditplanhistory [AuditPlanHistory]  object holding details to be added in auditplanhistory table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									   "auditplan": { "nauditplancode": 1, "dauditdatetime": "2025-08-07","ntzdauditdatetime":-1,"noffsetdauditdatetime":0 },
									    "auditplanhistory": { "sremarks": "closed" },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 266,"nmastersitecode": -1,"nmodulecode": 81,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with string message as 'Already Deleted' if the auditplan record is not available/
	 *          with string message as 'Select Scheduled/Rescheduled Record Only' if the auditplan record status is not scheduled or rescheduled/ 
	 * 			list of all auditplan and along with the updated auditplan.	 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/closeAuditPlan")
	public ResponseEntity<Object> closeAuditPlan(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return auditPlanService.closeAuditPlan(inputMap, userInfo);
	}
	/**
	 * This method is used to retrieve a list of all users.
	 * @param inputMap  [Map] map object with "nauditplancode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nauditplancode": 1,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 266,"nmastersitecode": -1,"nmodulecode": 81,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already Deleted' if the auditplan record is not available/
	 * 	       with string message as 'Select Draft Record Only' if the auditplan record status is not draft/ 
	 *		   list of all users.	 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getAvailableAuditPlanMember")
	public ResponseEntity<Object> getAvailableAuditPlanMember(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return auditPlanService.getAvailableAuditPlanMember(inputMap, userInfo);
	}
	/**
	 * This method is used to retrieve a list of all auditmaster.
	 * @param inputMap  [Map] map object with "nauditplancode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nauditplancode": 1,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 266,"nmastersitecode": -1,"nmodulecode": 81,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already Deleted' if the auditplan record is not available/
	 * 	       with string message as 'Select Draft Record Only' if the auditplan record status is not draft/ 
	 *		   list of all auditmaster.	 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getAvailableAuditPlanAuditor")
	public ResponseEntity<Object> getAvailableAuditPlanAuditor(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return auditPlanService.getAvailableAuditPlanAuditor(inputMap, userInfo);
	}
	/**
	 * This method is used to make a new entry to "auditplanmembers" table.
	 * @param inputMap map object holding params ( 
	 * 								auditplanmember [AuditPlanMember]  list holding details to be added in auditplanmembers table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "auditplanmember": { "nauditplancode": 1, "nusercode": 1 },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 266,"nmastersitecode": -1,"nmodulecode": 81,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with list of auditplanmember along with the newly added auditplanmember/
	 * 	 		with string message as 'Select Draft Record Only' if the auditplan record status is not draft/ 
	 * 			with string message as 'Already Deleted' if the auditplan record is not available/
	 * @throws Exception exception
	 */
	//Modified by sonia on 9th sept 2025 for jira id:SWSM-6
	@PostMapping(path = "/createAuditPlanMember")
	public ResponseEntity<Object> createAuditPlanMember(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final List<AuditPlanMember> lstAuditPlanMember = objMapper.convertValue(inputMap.get("auditplanmember"), new TypeReference<List<AuditPlanMember>>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditPlanService.createAuditPlanMember(lstAuditPlanMember,userInfo);
	}
	/**
	 * This method will is used to make a new entry to "auditplanauditors" table.
	 * @param inputMap map object holding params ( 
	 * 								auditplanauditor [AuditPlanAuditor]  list holding details to be added in auditplanauditors table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "auditplanauditor": { "nauditplancode": 1,"nauditmastercode":1 },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 266,"nmastersitecode": -1,"nmodulecode": 81,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with list of auditplanauditor along with the newly added auditplanauditor/
	 * 			with string message as 'Select Draft Record Only' if the auditplan record status is not draft/ 
	 * 			with string message as 'Already Deleted' if the auditplan record is not available/
	 * @throws Exception exception
	 */
	//Modified by sonia on 9th sept 2025 for jira id:SWSM-6
	@PostMapping(path = "/createAuditPlanAuditor")
	public ResponseEntity<Object>  createAuditPlanAuditor(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final List<AuditPlanAuditor> lstAuditPlanAuditor = objMapper.convertValue(inputMap.get("auditplanauditor"), new TypeReference<List<AuditPlanAuditor>>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditPlanService.createAuditPlanAuditor(lstAuditPlanAuditor,userInfo);
	}
	/**
	 * This method is used to delete an entry in "auditplanmembers" table
	 * @param inputMap [Map] object with keys of AuditPlanMember entity and UserInfo object.
	 * 					Input:{
     						"auditplanmember": {"nauditplancode":1,"nauditplanmembercode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 266,"nmastersitecode": -1, "nmodulecode": 81,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the auditplan record is not available/ 
	 * 			string message as 'Select Draft Record Only' if the auditplan record status is not draft /
	 * 	 		string message as 'Already deleted' if the auditplanmembers record is not available /
	 * 			list of all auditplanmembers excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteAuditPlanMember")
	public ResponseEntity<Object> deleteAuditPlanMember(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return auditPlanService.deleteAuditPlanMember(inputMap, userInfo);
	}
	/**
	 * This method is used to delete an entry in "auditplanauditors" table
	 * @param inputMap [Map] object with keys of AuditPlanAuditor entity and UserInfo object.
	 * 					Input:{
     						"auditplanauditor": {"nauditplancode":1,"nauditplanauditorcode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 266,"nmastersitecode": -1, "nmodulecode": 81,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the auditplan record is not available/ 
	 * 			string message as 'Select Draft Record Only' if the auditplan record status is not draft /
	 * 	  		string message as 'Already deleted' if the auditplanauditors record is not available /
	 * 			list of all auditplanauditors excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteAuditPlanAuditor")
	public ResponseEntity<Object> deleteAuditPlanAuditor(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"),new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return auditPlanService.deleteAuditPlanAuditor(inputMap, userInfo);
	}
	/**
	 * This method is used to retrieve a specific auditplanfile record.
	 * @param inputMap  [Map] map object with "nauditplancode","nauditplanfilecode" and "userinfo" as keys for which the data is to be fetched
	 *                  Input:{ "nauditplancode": 1,"nauditplanfilecode": 1,
							     "userinfo":{
							                "activelanguagelist": ["en-US"],
							                "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
							                "nformcode": 266,"nmastersitecode": -1,"nmodulecode": 81,
							                "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
							                "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",                
							                "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",
							                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
							                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
							                "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with AuditPlanFile object for the specified primary key / 
	 * 		   with string message as 'Already Deleted' if the auditplan record is not available/
	 * 		   with string message as 'Select Draft Record Only' if the auditplan record status is not draft/
	 * 	 	   with	string message as 'Already deleted' if the auditplanfile record is not available /
	 * @throws Exception exception
	 */
	@PostMapping(path = "/getActiveAuditPlanFileById")
	public ResponseEntity<Object> getActiveAuditPlanFileById(@RequestBody Map<String, Object> inputMap) throws Exception {		
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditPlanService.getActiveAuditPlanFileById(inputMap,userInfo);
	}
	/**
	 * This method is used to make a new entry to "auditplanfile" table.
	 * @param inputMap map object holding params ( 
	 * 								auditplanfile [AuditPlanFile]  object holding details to be added in auditplanfile table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "auditplanfile": { "nauditplancode":1,"nlinkcode":-1,"nattachmenttypecode":1,"sfilename":"AttachmentType.txt","sdescription":"good" },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 266,"nmastersitecode": -1,"nmodulecode": 81,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with list of auditplanfile along with the newly added auditplanfile/
	 * 			with string message as 'Select Draft Record Only' if the auditplan record status is not draft/ 
	 * 			with string message as 'Already Deleted' if the auditplan record is not available/
	 * 			with string message as 'Check FTP Connection' if the ftpconfig is not connected properly/
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createAuditPlanFile")
	public ResponseEntity<Object> createAuditPlanFile(MultipartHttpServletRequest request) throws Exception {		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditPlanService.createAuditPlanFile(userInfo, request);
	}
	/**
	 * This method is used to update selected auditplanfile details.
	 * @param inputMap map object holding params ( 
	 * 								auditplanfile [AuditPlanFile]  object holding details to be added in auditplanfile table,
	 * 								userinfo [UserInfo] holding logged in user details
	 *                              ) 
	 *                           Input:{
									    "auditplanfile": { "nauditplanfilecode":1,"nauditplancode":1,"nlinkcode":-1,"nattachmenttypecode":1,"sfilename":"AttachmentType.txt","sdescription":"good" },
									    "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -   1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 266,"nmastersitecode": -1,"nmodulecode": 81,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English",                "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",                "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",  "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}
									}
	 * @return ResponseEntity with list of auditplanfile along with the newly added auditplanfile/
	 * 			with string message as 'Select Draft Record Only' if the auditplan record status is not draft/ 
	 * 			with string message as 'Already Deleted' if the auditplan record is not available/
	 * 			with string message as 'Check FTP Connection' if the ftpconfig is not connected properly/
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateAuditPlanFile")
	public ResponseEntity<Object> updateAuditPlanFile(MultipartHttpServletRequest request) throws Exception {		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditPlanService.updateAuditPlanFile(userInfo, request);
	}
	/**
	 * This method is used to delete an entry in "auditplanfile" table
	 * @param inputMap [Map] object with keys of AuditPlanFile entity and UserInfo object.
	 * 					Input:{
     						"auditplanfile": {"nauditplancode":1,"nauditplanfilecode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 266,"nmastersitecode": -1, "nmodulecode": 81,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the auditplan record is not available/ 
	 * 			string message as 'Select Draft Record Only' if the auditplan record is status is not draft /
	 * 	 		string message as 'Already deleted' if the auditplanfile record is not available /
	 * 			list of all auditplanfile excluding the deleted record 
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteAuditPlanFile")
	public ResponseEntity<Object> deleteAuditPlanFile(@RequestBody Map<String, Object> inputMap) throws Exception {		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditPlanService.deleteAuditPlanFile(inputMap,userInfo);
	}
	/**
	 * This method is used to fetch a file/ link which need to view
	 * @param inputMap [Map] object with keys of AuditPlanFile entity and UserInfo object.
	 * 					Input:{
     						"auditplanfile": {"nauditplancode":1,"nauditplanfilecode":1},
    						"userinfo":{
                						"activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,  "nformcode": 266,"nmastersitecode": -1, "nmodulecode": 81,  "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,  "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",  "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US","slanguagename": "English",
 										"slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
  										"spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "", "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}	 * @return 
	 * @return response entity holds the list of AuditPlanFile details/
	 *         with string message as 'Already deleted' if the auditplan record is not available/
	 *         string message as 'Select Draft Record Only' if the auditplan record is status is not draft/
	 * 	 	   string message as 'Already deleted' if the auditplanfile record is not available /
	 * @throws Exception 
	 */ 
	@PostMapping(value = "/viewAuditPlanFile")
	public ResponseEntity<Object> viewAuditPlanFile(@RequestBody Map<String, Object> inputMap) throws Exception {		
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return auditPlanService.viewAuditPlanFile(inputMap,userInfo);
	}
}
