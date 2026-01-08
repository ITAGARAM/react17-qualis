package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.Villages;
import com.agaramtech.qualis.submitter.service.villages.VillagesService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the Villages Service methods.
 */
/**
 * @author sujatha.v
 * SWSM-4
 * 22/07/2025
 */
@RestController
@RequestMapping("/village")
public class VillagesController {

	private static final Logger LOGGER = LoggerFactory.getLogger(VillagesController.class);

	private RequestContext requestContext;
	private final VillagesService villageService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param villageService    VillagesService
	 */
	public VillagesController(RequestContext requestContext, VillagesService villageService) {
		super();
		this.requestContext = requestContext;
		this.villageService = villageService;
	}

	/**
	 * This method is used to retrieve list of available village(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all villages
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getVillage")
	public ResponseEntity<Object> getVillage(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getVillage");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return villageService.getVillage(userInfo);
	}
	/**
	 * This method is used to retrieve list of the available taluka's from the approved site hierarchy config version
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of available taluka's from the approved site hierarchy config version
	 * @throws Exception exception
	 */
	//added by sujatha ATE_274 to get taluka from the approved site hierarchy config 
	@PostMapping(value = "/getTaluka")
	public ResponseEntity<Object> getTaluka(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getTaluk");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return villageService.getTaluka(userInfo);
	}

	/**
	 * This method will is used to make a new entry to village table.
	 * 
	 * @param inputMap map object holding params ( village [Villages] object holding
	 *                 details to be added in village table, userinfo [UserInfo]
	 *                 holding logged in user details ) Input:{ "village": {
	 *                 "svillagename": "villagename", "svillagecode": "villagecode", "ncitycode": 1 },
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 267,"nmastersitecode": -1,"nmodulecode": 70,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the village
	 *         already exists for the list of cities along with the newly added village.
	 * @throws Exception exception
	 */
	@PostMapping("/createVillage")
	public ResponseEntity<Object> createVillage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final Villages village = objMapper.convertValue(inputMap.get("village"), Villages.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return villageService.createVillage(village, userInfo);
	}

	/**
	 * This method is used to retrieve a specific village record.
	 * 
	 * @param inputMap [Map] map object with "nvillagecode" and "userinfo" as keys for
	 *                 which the data is to be fetched Input:{ "nvillagecode": 1,
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 267,"nmastersitecode": -1,"nmodulecode": 70,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with Village object for the specified primary key / with
	 *         string message as 'Deleted' if the village record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveVillageById")
	public ResponseEntity<Object> getActiveVillageById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nvillagecode = (Integer) inputMap.get("nvillagecode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return villageService.getActiveVillageById(nvillagecode, userInfo);
	}

	/**
	 * This method is used to update selected village details.
	 * 
	 * @param inputMap [map object holding params( villages [Villages] object holding
	 *                 details to be updated in village table, userinfo [UserInfo]
	 *                 holding logged in user details) Input:{ "villages":
	 *                 {"nvillagecode":1,"svillagename": "m","svillagecode": "m",
	 *                 "ndistrictcode": 1}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 267,"nmastersitecode": -1, "nmodulecode": 70, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the village
	 *         record is not available/ list of all villages and along with the
	 *         updated village.
	 * @throws Exception exception
	 */
	@PostMapping("/updateVillage")
	public ResponseEntity<Object> updateVillage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final Villages village = objMapper.convertValue(inputMap.get("village"), Villages.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return villageService.updateVillage(village, userInfo);
	}

	/**
	 * This method is used to delete an entry in village table
	 * 
	 * @param inputMap [Map] object with keys of Village entity and UserInfo object.
	 *                 Input:{ "villages": {"nvillagecode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 267,"nmastersitecode": -1, "nmodulecode":
	 *                 70, "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the village
	 *         record is not available/ string message as 'Record is used in....'
	 *         when the villages is associated in transaction / list of all cities
	 *         excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping("/deleteVillage")
	public ResponseEntity<Object> deleteVillage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final Villages village = objMapper.convertValue(inputMap.get("village"), Villages.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return villageService.deleteVillage(village, userInfo);
	}
}
