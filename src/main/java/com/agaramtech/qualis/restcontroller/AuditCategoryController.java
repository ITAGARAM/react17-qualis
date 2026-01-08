package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.auditplan.model.AuditCategory;
import com.agaramtech.qualis.auditplan.service.auditcategory.AuditCategoryService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the Audit Category Service methods.
 */
/**
 * @author sujatha.v
 * SWSM-3
 * 19/07/2025
 */
@RestController
@RequestMapping("/auditcategory")
public class AuditCategoryController {

	private final static Logger LOGGER = LoggerFactory.getLogger(AuditCategoryController.class);
	private final AuditCategoryService auditCategoryService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext       RequestContext to hold the request
	 * @param auditCategoryService AuditCategoryService
	 */
	public AuditCategoryController(AuditCategoryService auditCategoryService, RequestContext requestContext) {
		super();
		this.auditCategoryService = auditCategoryService;
		this.requestContext = requestContext;
	}

	/**
	 * This method is used to retrieve list of available auditcategory(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         auditcategory
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getAuditCategory")
	public ResponseEntity<Object> getAuditCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getAuditCategory() called");
		requestContext.setUserInfo(userInfo);
		return auditCategoryService.getAuditCategory(userInfo);
	}

	/**
	 * This method is used to retrieve a specific auditcategory record.
	 * 
	 * @param inputMap [Map] map object with "auditcategory" and "userinfo"
	 *                 as keys for which the data is to be fetched Input:{
	 *                 "nauditcategorycode": 1, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,
	 *                 "ndeputyuserrole":-1, "nformcode": 264,"nmastersitecode": -1,"nmodulecode": 81,
	 *                 "nreasoncode": 0,"nsitecode": -1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, 
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy", 
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with AuditCategory object for the specified
	 *         primary key / with string message as 'Deleted' if the
	 *         auditcategory record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveAuditCategoryById")
	public ResponseEntity<Object> getActiveAuditCategoryById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final int nauditcategorycode = (Integer) inputMap.get("nauditcategorycode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return auditCategoryService.getActiveAuditCategoryById(nauditcategorycode, userInfo);
	}

	/**
	 * This method will is used to make a new entry to auditcategory table.
	 * 
	 * @param inputMap map object holding params ( auditcategory
	 *                 [AuditCategory] object holding details to be added in
	 *                 auditcategory table, userinfo [UserInfo] holding
	 *                 logged in user details ) 
	 *                 Input:{ "auditcategory": {
	 *                 "sauditcategoryname": "auditcat1"}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":4,"ndeptcode": - 1,"ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nformcode": 264,"nmastersitecode": -1,"nmodulecode": 81,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1, 
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC +00:00",
	 *                 "slanguagefilename": "Msg_en_US","slanguagename": "English", "slanguagetypecode": "en-US", 
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":"","ssitedate": "dd/MM/yyyy", 
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         auditcategory already exists/ list of auditcategory's
	 *         along with the newly added auditcategory.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createAuditCategory")
	public ResponseEntity<Object> createAuditCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final AuditCategory auditCategory = objectMapper.convertValue(inputMap.get("auditcategory"),
				new TypeReference<AuditCategory>() {
				});
		requestContext.setUserInfo(userInfo);
		return auditCategoryService.createAuditCategory(auditCategory, userInfo);
	}

	/**
	 * This method is used to update selected auditcategory details.
	 * 
	 * @param inputMap [map object holding params( auditcategory
	 *                 [AuditCategory] object holding details to be updated
	 *                 in auditcategory table, userinfo [UserInfo] holding
	 *                 logged in user details) Input:{ "auditcategory":
	 *                 {"nauditcategorycode":1,"sauditcategoryname": "cat2",
	 *                 "sdescription": "mv" }, "userinfo":{ "activelanguagelist":["en-US"],"isutcenabled": 4,"ndeptcode":-1,
	 *                 "ndeputyusercode": -1,"ndeputyuserrole": -1, 
	 *                 "nformcode":264,"nmastersitecode": -1, "nmodulecode": 81, "nreasoncode":0,"nsitecode": 1,
	 *                 "ntimezonecode": -1,"ntranssitecode":1,"nusercode": -1, "nuserrole": -1,"nusersitecode":"UTC +00:00", 
	 *                 "slanguagefilename":"Msg_en_US","slanguagename": "English", "slanguagetypecode":"en-US", 
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         auditcategory record is not available/ list of all
	 *         auditcategory's and along with the updated
	 *         auditcategory.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateAuditCategory")
	public ResponseEntity<Object> updateAuditCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final AuditCategory auditCategory = objectMapper.convertValue(inputMap.get("auditcategory"),
				new TypeReference<AuditCategory>() {
				});
		requestContext.setUserInfo(userInfo);
		return auditCategoryService.updateAuditCategory(auditCategory, userInfo);
	}

	/**
	 * This method is used to delete an entry in AuditCategory table
	 * 
	 * @param inputMap [Map] object with keys of AuditCategory entity and
	 *                 UserInfo object. Input:{ "auditcategory":
	 *                 {"nauditcategorycode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":4,"ndeptcode": -1,
	 *                 "ndeputyusercode": -1,"ndeputyuserrole":-1, "nformcode": 264,
	 *                 "nmastersitecode": -1, "nmodulecode":81, "nreasoncode": 0,"nsitecode": 1,
	 *                 "ntimezonecode":-1,"ntranssitecode": 1,
	 *                 "nusercode": -1, "nuserrole":-1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", 
	 *                 "sgmtoffset": "UTC +00:00", "slanguagefilename":"Msg_en_US","slanguagename": "English", 
	 *                 "slanguagetypecode":"en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         auditcategory record is not available/ string message as
	 *         'Record is used in....' when the auditcategory is associated
	 *         in transaction / list of all auditcategory's excluding the
	 *         deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteAuditCategory")
	public ResponseEntity<Object> deleteAuditCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final AuditCategory auditCategory = objectMapper.convertValue(inputMap.get("auditcategory"),
				new TypeReference<AuditCategory>() {
				});
		requestContext.setUserInfo(userInfo);
		return auditCategoryService.deleteAuditCategory(auditCategory, userInfo);
	}
}
