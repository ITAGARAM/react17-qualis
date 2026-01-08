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
import com.agaramtech.qualis.samplescheduling.model.SampleLocation;
import com.agaramtech.qualis.samplescheduling.service.samplelocation.SampleLocationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the SampleLocation Service methods.
 */
/**
 * @author sujatha.v AT-E274 SWSM-5 24/07/2025
 */
@RestController
@RequestMapping("/samplelocation")
public class SampleLocationController {

	private static final Logger LOGGER  = LoggerFactory.getLogger(SampleLocationController.class);

	private RequestContext requestContext;
	private final SampleLocationService sampleLocationService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext        RequestContext to hold the request
	 * @param sampleLocationService SampleLocationService
	 */
	public SampleLocationController(RequestContext requestContext, SampleLocationService sampleLocationService) {
		super();
		this.requestContext = requestContext;
		this.sampleLocationService = sampleLocationService;
	}

	/**
	 * This method is used to retrieve list of available samplelocation(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         samplelocation
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSampleLocation")
	public ResponseEntity<Object> getSampleLocation(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getSampleLocation");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleLocationService.getSampleLocation(userInfo);
	}

	/**
	 * This method is used to retrieve list of mapped region(s) in the site hierarchy configuration.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         regions
	 * @throws Exception exception
	 */
    //commented by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
//	@PostMapping(value = "/getRegion")
//	public ResponseEntity<Object> getRegion(@RequestBody Map<String, Object> inputMap) throws Exception {
//		ObjectMapper objMapper = new ObjectMapper();
//		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
//		});
//		requestContext.setUserInfo(userInfo);
//		LOGGER.info("Get Controller -->"+userInfo);
//		return sampleLocationService.getRegion(userInfo);
//	}

	//added by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
	@PostMapping(value = "/getRegion")
	public ResponseEntity<Object> getRegion(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return sampleLocationService.getRegion(userInfo);
	} 
	
	 //commented by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
//	@PostMapping(value = "/getVillage")
//	public ResponseEntity<Object> getVillage(@RequestBody Map<String, Object> inputMap)
//				throws Exception {
//	 
//			final ObjectMapper objMapper = new ObjectMapper();
//			final int nprimarykey = (int) inputMap.get("primarykey");
//			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
//			});
//			requestContext.setUserInfo(userInfo);
//	 
//			return sampleLocationService.getVillage(nprimarykey, userInfo);
//		}
		
	/**
	 * This method is used to retrieve list of available taluka based on the
	 * the subdivisional lab(taluka) mapped in the site hierarchy configuration
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all taluka
	 *         based on the subdivisional lab(taluka) mapped in the site hierarchy configuration
	 * @throws Exception excepon
	 */
	//added by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
		@PostMapping(value = "/getTaluka")
		public ResponseEntity<Object> getTaluka(@RequestBody Map<String, Object> inputMap)
				throws Exception {
	 
			final ObjectMapper objMapper = new ObjectMapper();
			final int nprimarykey = (int) inputMap.get("primarykey");
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
			});
			requestContext.setUserInfo(userInfo);
	 
			return sampleLocationService.getTaluka(nprimarykey, userInfo);
		}

	/**
	 * This method is used to retrieve list of available district(s) based on the
	 * the district which is mapped in the site hierarchy configuration.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         district based on the district which is mapped in the site hierarchy configuration
	 * @throws Exception exception
	 */
    //commented by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
//	@PostMapping(value = "/getDistrict")
//	public ResponseEntity<Object> getDistrict(@RequestBody Map<String, Object> inputMap) throws Exception {
//
//		ObjectMapper objMapper = new ObjectMapper();
//		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
//		});
//		requestContext.setUserInfo(userInfo);
//		return sampleLocationService.getDistrict(inputMap, userInfo);
//	}
		//added by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
		@PostMapping(value = "/getDistrict")
		public ResponseEntity<Object> getDistrict(@RequestBody Map<String, Object> inputMap)
				throws Exception {
	 
			final ObjectMapper objMapper = new ObjectMapper();
			final int nprimarykey = (int) inputMap.get("primarykey");
			final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
			});
			requestContext.setUserInfo(userInfo);
	 
			return sampleLocationService.getDistrict(nprimarykey, userInfo);
		}

	/**
	 * This method is used to retrieve list of available cities based on the
	 * selected district.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all city
	 *         based on district sected.
	 * @throws Exception excepon
	 */
   //commented by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
//	@PostMapping(value = "/getCity")
//	public ResponseEntity<Object> getCity(@RequestBody M<String, Object> inputMap) rows Exception {
//		LOGGER.info("getCity");
//		fin ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.g("userio"), new TypeReference<UserInfo>() {
//	);
//		requestContext.setUserInfo(userInfo);
//		return sameLocationService.getCity(inputMap, userInfo);
//	}

	/**
	 * This method is used to retrieve list of available village(s) based on the
	 * taluka that is mapped in the site hierarchy configuration which is get in the village dropdown.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         villages based on that is mapped in the site hierarchy configuration.
	 * @throws Exception exceptio
	 */
	//added by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
	@PostMapping(value = "/getVillage")
	public ResponseEntity<Object> getVillage(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getVillage");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleLocationService.getVillage(inputMap, userInfo);
	}

	/**
	 * This method will is used to make a new entry to samplelocation table.
	 * 
	 * @param inputMap map object holding params ( objSampleLocation
	 *                 [SampleLocation] object holding details to be added in
	 *                 samplelocation table, userinfo [UserInfo] holding logged in
	 *                 user details ) Input:{ "samplelocation": {
	 *                 "ssamplelocationname": "samplelocation name", "nregioncode":
	 *                 1, "ndistrictcode": 2, "ncitycode": 1, "nvillagecode": 1,
	 *                 slatitude: "AB", "slongitude": "XY", "sdescription": "New
	 *                 Location"}, "userinfo":{ "activelanguagelist": ["en-US"],
	 *                 "isutcenabled": -1, "nformcode": 269, "nmastersitecode":
	 *                 -1,"nmodulecode": 82, "nreasoncode": 0,"nsitecode":
	 *                 1,"ntimezonecode": -1,"ntranssitecode": 1, "nusercode": -1,
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat":
	 *                 "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC
	 *                 +00:00","slanguagefilename":"Msg_en_US","slanguagename":
	 *                 "English", "slanguagetypecode": "en-US", "spgdatetimeformat":
	 *                 "dd/MM/yyyy HH24:mi:ss", "spgsitedatetime": "dd/MM/yyyy
	 *                 HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         samplelocation already exists for the list of cities along with the
	 *         newly added samplelocation.
	 * @throws Exception exception
	 */
	@PostMapping("/createSampleLocation")
	public ResponseEntity<Object> createSampleLocation(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final SampleLocation objSampleLocation = objMapper.convertValue(inputMap.get("samplelocation"),
				SampleLocation.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleLocationService.createSampleLocation(objSampleLocation, userInfo);
	}

	/**
	 * This method is used to retrieve a specific samplelocation record.
	 * 
	 * @param inputMap [Map] map object with "nsamplelocationcode" and "userinfo" as
	 *                 keys for which the data is to be fetched Input:{
	 *                 "nsamplelocationcode": 1, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 269,"nmastersitecode": -1,"nmodulecode": 82, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with SampleLocation object for the specified primary
	 *         key / with string message as 'Deleted' if the samplelocation record
	 *         is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveSampleLocationById")
	public ResponseEntity<Object> getActiveSampleLocationById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nsamplelocationcode= (Integer) inputMap.get("nsamplelocationcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleLocationService.getActiveSampleLocationById(nsamplelocationcode, userInfo);
	}

	/**
	 * This method is used to update selected samplelocation details.
	 * 
	 * @param inputMap [map object holding params( objSampleLocation
	 *                 [SampleLocation] object holding details to be updated in
	 *                 sampleloation table, userinfo [UserInfo] holding logged in
	 *                 user details) Input:{ "samplelocation":
	 *                 {"nsamplelocationcode":1,"ssamplelocationname":
	 *                 "samplelocationname","nregioncode": 1, "ndistrictcode": 1,
	 *                 "ncitycode": 2, "nvillagecode": 1, "sdescription": "NEW
	 *                 ONE"}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 269,"nmastersitecode": -1, "nmodulecode": 82, "nreasoncode":
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
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         samplelocation record is not available/ list of all samplelocation's
	 *         and along with the updated samplelocation.
	 * @throws Exception exception
	 */
	@PostMapping("/updateSampleLocation")
	public ResponseEntity<Object> updateSampleLocation(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final SampleLocation ObjSampleLocation = objMapper.convertValue(inputMap.get("samplelocation"),
				SampleLocation.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleLocationService.updateSampleLocation(ObjSampleLocation, userInfo);
	}

	/**
	 * This method is used to delete an entry in samplelocation table
	 * 
	 * @param inputMap [Map] object with keys of samplelocation entity and UserInfo
	 *                 object. Input:{ "samplelocation": {"nsamplelocationcode":1},
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 269,"nmastersitecode": -1, "nmodulecode":
	 *                 82, "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         samplelocation record is not available/ string message as 'Record is
	 *         used in....' when the samplelocation is associated in transaction /
	 *         list of all cities excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping("/deleteSampleLocation")
	public ResponseEntity<Object> deleteSampleLocation(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final SampleLocation objSampleLocation = objMapper.convertValue(inputMap.get("samplelocation"),
				SampleLocation.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleLocationService.deleteSampleLocation(objSampleLocation, userInfo);
	}
}
