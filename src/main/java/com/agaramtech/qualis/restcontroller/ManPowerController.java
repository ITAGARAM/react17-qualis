package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.credential.model.ManPower;
import com.agaramtech.qualis.credential.service.manpower.ManPowerService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the outsourceemployee Service methods.
 */
/**
 * @author sujatha.v SWSM-8 01/08/2025
 */
@RestController
@RequestMapping("/manpower")
public class ManPowerController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManPowerController.class);

	private final ManPowerService manPowerService;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext  RequestContext to hold the request
	 * @param manPowerService ManPowerService
	 */
	public ManPowerController(ManPowerService manPowerService, RequestContext requestContext) {
		super();
		this.manPowerService = manPowerService;
		this.requestContext = requestContext;
	}

	/**
	 * This method is used to retrieve list of available outsource employee(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         manpower
	 * @throws Exception exception
	 */
	@PostMapping("/getManPower")
	public ResponseEntity<Object> getManPower(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getManPower");
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return manPowerService.getManPower(userInfo);
	}

	/**
	 * This method is used to retrieve list of available site.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         site
	 * @throws Exception exception
	 */
	// added by sujatha ATE_274 to get the Login Site in the 1st value of dropdown on 26-08-2025
	@PostMapping("/getSite")
	public ResponseEntity<Object> getSite(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return manPowerService.getSite(userInfo);
	}
	
	/**
	 * This method will is used to make a new entry to outsourceemployee table.
	 * 
	 * @param inputMap map object holding params ( manpower [ManPower] object
	 *                 holding details to be added in outsourceemployee table, userinfo
	 *                 [UserInfo] holding logged in user details ) Input:{
	 *                 "manpower": { "sfirstname": "ford", "slastname": "John",
	 *                 "sdesignation": "HR", "smobileno":"9876543456",
	 *                 "nsitemastercode": 1, "saddress":"Kolkata" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,
	 *                 "ndeputyuserrole":-1, "nformcode": 271,"nmastersitecode": -1,"nmodulecode": 3,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":-1,"ntranssitecode": 1,"nusercode": -1, 
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC +00:00",
	 *                 "slanguagefilename": "Msg_en_US","slanguagename": "English", "slanguagetypecode": "en-US", 
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":"","ssitedate": "dd/MM/yyyy", 
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         outsourceemployee already exists for the list of outsourceemployee along with the newly
	 *         added outsourceemployee.
	 * @throws Exception exception
	 */
	@PostMapping("/createManPower")
	public ResponseEntity<Object> createManPower(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final ManPower objManPower = objectMapper.convertValue(inputMap.get("manpower"),
				new TypeReference<ManPower>() {
				});
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return manPowerService.createManPower(objManPower, userInfo);
	}

	/**
	 * This method is used to retrieve a specific outsourceemployee record.
	 * 
	 * @param inputMap [Map] map object with "noutsourceempcode" and "userinfo" as keys
	 *                 for which the data is to be fetched Input:{ "noutsourceempcode":1, 
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],
	 *                 "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
	 *                 "nformcode": 271,"nmastersitecode": -1,"nmodulecode": 3, "nreasoncode": 0,
	 *                 "nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", 
	 *                 "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English", 
	 *                 "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss", 
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with ManPower object for the specified primary key /
	 *         with string message as 'Deleted' if the outsourceemployee record is not
	 *         available
	 * @throws Exception exception
	 */
	@PostMapping("/getActiveManPowerById")
	public ResponseEntity<Object> getActiveManPowerById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final int nmanpowercode = (Integer) inputMap.get("noutsourceempcode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return manPowerService.getActiveManPowerById(nmanpowercode, userInfo);
	}

	/**
	 * This method is used to update selected outsourceemployee details.
	 * 
	 * @param inputMap [map object holding params( objManPower [ManPower] object
	 *                 holding details to be updated in outsourceemployee table, userinfo
	 *                 [UserInfo] holding logged in user details) Input:{
	 *                 "manpower": {"noutsourceempcode":1,"sfirstname": "James","slastname": "Pond", 
	 *                 "sdesignation": "Sales Manager",
	 *                 "smobileno":"9876543456", "nsitemastercode":1,"saddress":"Europe"}, 
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],"isutcenabled": 4,
	 *                 "ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 271,
	 *                 "nmastersitecode": -1, "nmodulecode": 3, "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
	 *                 "ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC +00:00", 
	 *                 "slanguagefilename": "Msg_en_US","slanguagename": "English", "slanguagetypecode": "en-US", 
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         outsourceemployee record is not available/ list of all outsourceemployee's and along
	 *         with the updated outsourceemployee.
	 * @throws Exception exception
	 */
	@PostMapping("/updateManPower")
	public ResponseEntity<Object> updateManpower(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final ManPower objManPower = objectMapper.convertValue(inputMap.get("manpower"),
				new TypeReference<ManPower>() {
				});
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return manPowerService.updateManPower(objManPower, userInfo);
	}

	/**
	 * This method is used to delete an entry in outsourceemployee table
	 * 
	 * @param inputMap [Map] object with keys of ManPower entity and UserInfo
	 *                 object. Input:{ "manpower": {"noutsourceempcode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,
	 *                 "ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 271,"nmastersitecode": -1, 
	 *                 "nmodulecode": 3, "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
	 *                 "ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC +00:00", 
	 *                 "slanguagefilename": "Msg_en_US","slanguagename": "English", "slanguagetypecode": "en-US", 
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         outsourceemployee record is not available/ string message as 'Record is used
	 *         in....' when the manpower's is associated in transaction / list of
	 *         all site's excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping("/deleteManPower")
	public ResponseEntity<Object> deleteManPower(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final ManPower objManPower = objectMapper.convertValue(inputMap.get("manpower"),
				new TypeReference<ManPower>() {
				});
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return manPowerService.deleteManPower(objManPower, userInfo);
	}
}
