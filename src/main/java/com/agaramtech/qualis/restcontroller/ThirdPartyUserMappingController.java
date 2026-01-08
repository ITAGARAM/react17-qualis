package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.biobank.model.ThirdParty;
import com.agaramtech.qualis.biobank.service.thirdpartyusermapping.ThirdPartyUserMappingService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the Unit Service methods.
 *
 * /* @author Mullai Balaji.V jira Id: BGSI-12 28/07/2025
 * 
 * @version
 */
@RestController
@RequestMapping("/thirdpartyusermapping")
public class ThirdPartyUserMappingController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThirdPartyUserMappingController.class);

	private RequestContext requestContext;
	private final ThirdPartyUserMappingService thirdPartyUserMappingService;

	public ThirdPartyUserMappingController(RequestContext requestContext,
			ThirdPartyUserMappingService thirdPartyUserMappingService) {
		super();
		this.requestContext = requestContext;
		this.thirdPartyUserMappingService = thirdPartyUserMappingService;
	}

	/**
	 * This Method is used to get the over all thirdparty with respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of thirdparty with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getThirdPartyUserMapping")
	public ResponseEntity<Object> getThirdParty(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getThirdPartyUserMapping called");
		requestContext.setUserInfo(userInfo);

		return thirdPartyUserMappingService.getThirdParty(userInfo);

	}

	/**
	 * This Method is used to get the over all userrole with respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of userrole with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getUserRole")
	public ResponseEntity<Object> getUserRole(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		
		final int nthirdpartycode = (int) inputMap.get("nthirdpartycode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return thirdPartyUserMappingService.getUserRole(nthirdpartycode,userInfo);

	}

	/**
	 * This Method is used to get the over all users with respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of users with respect to site
	 *         and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getUsers")
	public ResponseEntity<Object> getUsers(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		final int nuserrolecode = (int) inputMap.get("nuserrolecode");

		final int nthirdpartycode = (int) inputMap.get("nthirdpartycode");

		return thirdPartyUserMappingService.getUsers(nthirdpartycode, nuserrolecode, userInfo);

	}

	/**
	 * This method will is used to make a new entry to thirdparty table.
	 * 
	 * @param inputMap map object holding params ( thirdparty [thirdparty] object
	 *                 holding details to be added in thirdpartyusermapping table,
	 *                 userinfo [UserInfo] holding logged in user details ) Input:{
	 *                 "thirdpartyusermapping": { "sthirdpartyusermappingname":
	 *                 "masters" }, "userinfo":{ "activelanguagelist": ["en-US"],
	 *                 "isutcenabled": 4,"ndeptcode": - 1,"ndeputyusercode":
	 *                 -1,"ndeputyuserrole": -1, "nformcode": 261,"nmastersitecode":
	 *                 -1,"nmodulecode": 1, "nreasoncode": 0,"nsitecode":
	 *                 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat":
	 *                 "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC
	 *                 +00:00","slanguagefilename": "Msg_en_US","slanguagename":
	 *                 "English", "slanguagetypecode": "en-US", "spgdatetimeformat":
	 *                 "dd/MM/yyyy HH24:mi:ss", "spgsitedatetime": "dd/MM/yyyy
	 *                 HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         thirdpartyusermapping already exists/ list of thirdpartyusermapping
	 *         along with the newly added thirdpartyusermapping.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createThirdPartyUserMapping")
	public ResponseEntity<Object> createThirdParty(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		final ThirdParty objthirdParty = objMapper.convertValue(inputMap.get("thirdpartyusermapping"),
				new TypeReference<ThirdParty>() {
				});

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return thirdPartyUserMappingService.createThirdParty(objthirdParty, userInfo);
	}

	/**
	 * This method is used to update selected thirdparty details.
	 * 
	 * @param inputMap [map object holding params( thirdparty [thirdparty] object
	 *                 holding details to be updated in thirdparty table, userinfo
	 *                 [UserInfo] holding logged in user details) Input:{
	 *                 "thirdparty": {"nthirdpartycode":1,"sthirdpartyname":
	 *                 "m","saddress": "m","sphonenumber": "m","semail": "m",
	 *                 "sdescription": "m", "ndefaultstatus": 3 }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 261,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         thirdpartyusermapping record is not available/ list of all
	 *         thirdpartyusermapping and along with the updated
	 *         thirdpartyusermapping.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateThirdPartyUserMapping")
	public ResponseEntity<Object> updateThirdParty(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final ThirdParty objThirdParty = objMapper.convertValue(inputMap.get("thirdpartyusermapping"),
				new TypeReference<ThirdParty>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
	 // added nisngsConfirmed  by sujatha ATE_274 for delete validation in specific scenario BGSI-218
		final String nisngsConfirmed = String.valueOf(inputMap.get("nisngsConfirmed"));
		requestContext.setUserInfo(userInfo);
		return thirdPartyUserMappingService.updateThirdParty(objThirdParty, userInfo, nisngsConfirmed);
	}

	/**
	 * This method is used to retrieve a specific thirdparty record.
	 * 
	 * @param inputMap [Map] map object with "nthirdpartycode" and "userinfo" as
	 *                 keys for which the data is to be fetched Input:{
	 *                 "nthirdpartycode": 1, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 254,"nmastersitecode": -1,"nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with thirdparty object for the specified primary key /
	 *         with string message as 'Deleted' if the thirdparty record is not
	 *         available
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getActiveThirdPartyUserMappingById")
	public ResponseEntity<Object> getActiveThirdPartyById(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int nthirdpartycode = (int) inputMap.get("nthirdpartycode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return thirdPartyUserMappingService.getActiveThirdPartyById(nthirdpartycode, userInfo);
	}

	/**
	 * This method is used to delete an entry in thirdparty table
	 * 
	 * @param inputMap [Map] object with keys of thirdparty entity and UserInfo
	 *                 object. Input:{ "thirdparty": {"nthirdpartycode":1},
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 254,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         thirdparty record is not available/ string message as 'Record is used
	 *         in....' when the thirdpartyusermapping is associated in transaction / list of all
	 *         thirdparty excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteThirdPartyUserMapping")
	public ResponseEntity<Object> deleteThirdParty(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final ThirdParty objthirdParty = objMapper.convertValue(inputMap.get("thirdpartyusermapping"),
				new TypeReference<ThirdParty>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return thirdPartyUserMappingService.deleteThirdParty(objthirdParty, userInfo);
	}

	/**
	 * This method will is used to make a new entry to thirdpartyusermapping table.
	 * 
	 * @param inputMap map object holding params ( thirdpartyusermapping [thirdpartyusermapping]
	 *                 object holding details to be added in thirdpartyusermapping table, userinfo
	 *                 [UserInfo] holding logged in user details ) Input:{
	 *                 "thirdpartyusermapping": { "sthirdpartyname": "corono" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 261,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         thirdpartyusermapping already exists/ list of thirdpartyusermappings along with the
	 *         newly added thirdpartyusermapping.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createUserRole")
	public ResponseEntity<Object> createUsers(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return thirdPartyUserMappingService.createUsers(inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve a specific thirdparty record.
	 * 
	 * @param inputMap [Map] map object with "nthirdpartycode" and "userinfo" as
	 *                 keys for which the data is to be fetched Input:{
	 *                 "nthirdpartycode": 1, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 261,"nmastersitecode": -1,"nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with thirdparty object for the specified primary key /
	 *         with string message as 'Deleted' if the thirdparty record is not
	 *         available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getThirdPartySelectedRecord")
	public ResponseEntity<Object> getthirdpartyuser(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		//final String partyCodeStr = (String) inputMap.get("nthirdpartycode");
		//final int nthirdpartycode = Integer.parseInt(partyCodeStr);
		
		Object thirdpartyValue = inputMap.get("nthirdpartycode");

		int nthirdpartycode = 0; 

		if (thirdpartyValue != null) {

		        nthirdpartycode = Integer.parseInt(thirdpartyValue.toString());
		}

		
		requestContext.setUserInfo(userInfo);
		return thirdPartyUserMappingService.getthirdpartyuser(nthirdpartycode, userInfo);

	}

	/**
	 * This method is used to delete an entry in thirdpartyusermapping table
	 * 
	 * @param inputMap [Map] object with keys of thirdpartyusermapping entity and
	 *                 UserInfo object. Input:{ "thirdpartyusermapping":
	 *                 {"nthirdpartyusermappingcode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 261,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         thirdpartyusermapping record is not available/ string message as
	 *         'Record is used in....' when the thirdpartyusermapping is associated
	 *         in transaction / list of all thirdpartyusermappings excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteUserRoleAndUser")
	public ResponseEntity<Object> deleteUserRoleAndUser(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int nthirdpartycode = (int) inputMap.get("nthirdpartycode");
		final int nusercode = (int) inputMap.get("nusercode");
		final int nuserrolecode = (int) inputMap.get("nuserrolecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return thirdPartyUserMappingService.deleteUserRoleAndUser(nthirdpartycode, nusercode, nuserrolecode, userInfo);
	}
	
}
