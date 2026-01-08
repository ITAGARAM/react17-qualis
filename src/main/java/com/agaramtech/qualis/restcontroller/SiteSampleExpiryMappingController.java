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
import com.agaramtech.qualis.storagemanagement.model.SiteSampleExpiryMapping;
import com.agaramtech.qualis.storagemanagement.service.sitesampleexpirymapping.SiteSampleExpiryMappingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the Site Sample Expiry Mapping Service methods.
 */
/**
 * @author sujatha.v
 * SWSM-14
 * 31/08/2025
 */
@RestController
@RequestMapping("/siteexpirymapping")
public class SiteSampleExpiryMappingController {

	private final static Logger LOGGER = LoggerFactory.getLogger(SiteSampleExpiryMappingController.class);
	private final SiteSampleExpiryMappingService siteSampleExpiryMappingService;
	private RequestContext requestContext;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext       RequestContext to hold the request
	 * @param siteSampleExpiryMappingService SiteSampleExpiryMappingService
	 */
	public SiteSampleExpiryMappingController(SiteSampleExpiryMappingService siteSampleExpiryMappingService,
			RequestContext requestContext) {
		super();
		this.siteSampleExpiryMappingService = siteSampleExpiryMappingService;
		this.requestContext = requestContext;
	}
	
	/**
	 * This method is used to retrieve list of available siteexpirymapping(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         siteexpirymapping
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSiteSampleExpiryMapping")
	public ResponseEntity<Object> getSiteSampleExpiryMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getSiteSampleExpiryMapping() called");
		requestContext.setUserInfo(userInfo);
		return siteSampleExpiryMappingService.getSiteSampleExpiryMapping(userInfo);
	}
	
	/**
	 * This method is used to retrieve single or list of period(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and single or list of period
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getPeriod")
	public ResponseEntity<Object> getPeriod(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteSampleExpiryMappingService.getPeriod(userInfo);
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
	// added by sujatha ATE_274 to get the Login Site in the 1st value of dropdown on 01-09-2025
	@PostMapping("/getSite")
	public ResponseEntity<Object> getSite(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return siteSampleExpiryMappingService.getSite(userInfo);
	}
	
	/**
	 * This method is used to retrieve a specific siteexpirymapping record.
	 * 
	 * @param inputMap [Map] map object with "noutsourceempcode" and "userinfo" as keys
	 *                 for which the data is to be fetched Input:{ "nsiteexpirymappingcode":1, 
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],
	 *                 "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
	 *                 "nformcode": 277,"nmastersitecode": -1,"nmodulecode": 2, "nreasoncode": 0,
	 *                 "nsitecode": 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", 
	 *                 "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US","slanguagename": "English", 
	 *                 "slanguagetypecode": "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss", 
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with SiteSampleExpiryMapping object for the specified primary key /
	 *         with string message as 'Deleted' if the siteexpirymapping record is not
	 *         available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveSiteSampleExpiryMappingById")
	public ResponseEntity<Object> getActiveSiteSampleExpiryMappingById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final int nsiteexpirymappingcode = (Integer) inputMap.get("nsiteexpirymappingcode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return siteSampleExpiryMappingService.getActiveSiteSampleExpiryMappingById(nsiteexpirymappingcode, userInfo);
	}
	
	/**
	 * This method will is used to make a new entry to outsourceemployee table.
	 * 
	 * @param inputMap map object holding params ( siteexpirymapping [SiteSampleExpiryMapping] object
	 *                 holding details to be added in siteexpirymapping table, userinfo
	 *                 [UserInfo] holding logged in user details ) Input:{
	 *                 "siteexpirymapping": { "sexpirydays": "45", "nperiodcode": 4,
	 *                 "nsitemastercode": 6, "sdescription":"Kolkata" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode": -1,
	 *                 "ndeputyuserrole":-1, "nformcode": 277,"nmastersitecode": -1,"nmodulecode": 2,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":-1,"ntranssitecode": 1,"nusercode": -1, 
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC +00:00",
	 *                 "slanguagefilename": "Msg_en_US","slanguagename": "English", "slanguagetypecode": "en-US", 
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":"","ssitedate": "dd/MM/yyyy", 
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         siteexpirymapping already exists for the list of siteexpirymapping along with the newly
	 *         added siteexpirymapping.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createSiteSampleExpiryMapping")
	public ResponseEntity<Object> createSiteSampleExpiryMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final SiteSampleExpiryMapping objSiteSampleExpiryMapping = objectMapper.convertValue(inputMap.get("sitesampleexpirymapping"),
				new TypeReference<SiteSampleExpiryMapping>() {
				});
		requestContext.setUserInfo(userInfo);
		return siteSampleExpiryMappingService.createSiteSampleExpiryMapping(objSiteSampleExpiryMapping, userInfo);
	}
	
	/**
	 * This method is used to update selected siteexpirymapping details.
	 * 
	 * @param inputMap [map object holding params( objSiteSampleExpiryMapping [SiteSampleExpiryMapping] object
	 *                 holding details to be updated in outsourceemployee table, userinfo
	 *                 [UserInfo] holding logged in user details) Input:{
	 *                 "siteexpirymapping": {"nsiteexpirymappingcode":1,"sexpirydays": "20","nperiodcode": 4, 
	 *                 "nsitemastercode": 3, "nsitemastercode":1,"sdescription":"NA"}, 
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],"isutcenabled": 4,
	 *                 "ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 277,
	 *                 "nmastersitecode": -1, "nmodulecode": 2, "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
	 *                 "ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC +00:00", 
	 *                 "slanguagefilename": "Msg_en_US","slanguagename": "English", "slanguagetypecode": "en-US", 
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         siteexpirymapping record is not available/ list of all siteexpirymapping's and along
	 *         with the updated siteexpirymapping.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateSiteSampleExpiryMapping")
	public ResponseEntity<Object> updateSiteSampleExpiryMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final SiteSampleExpiryMapping objSiteSampleExpiryMapping = objectMapper.convertValue(inputMap.get("sitesampleexpirymapping"),
				new TypeReference<SiteSampleExpiryMapping>() {
				});
		requestContext.setUserInfo(userInfo);
		return siteSampleExpiryMappingService.updateSiteSampleExpiryMapping(objSiteSampleExpiryMapping, userInfo);
	}
	
	/**
	 * This method is used to delete an entry in siteexpirymapping table
	 * 
	 * @param inputMap [Map] object with keys of ManPower entity and UserInfo
	 *                 object. Input:{ "siteexpirymapping": {"nsiteexpirymappingcode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled": 4,"ndeptcode": -1,
	 *                 "ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode": 277,"nmastersitecode": -1, 
	 *                 "nmodulecode": 2, "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
	 *                 "ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,"nusersitecode": -1,
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC +00:00", 
	 *                 "slanguagefilename": "Msg_en_US","slanguagename": "English", "slanguagetypecode": "en-US", 
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         siteexpirymapping record is not available/ string message as 'Record is used
	 *         in....' when the siteexpirymapping's is associated in transaction / list of
	 *         all site's excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteSiteSampleExpiryMapping")
	public ResponseEntity<Object> deleteSiteSampleExpiryMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final SiteSampleExpiryMapping objSiteSampleExpiryMapping = objectMapper.convertValue(inputMap.get("sitesampleexpirymapping"),
				new TypeReference<SiteSampleExpiryMapping>() {
				});
		requestContext.setUserInfo(userInfo);
		return siteSampleExpiryMappingService.deleteSiteSampleExpiryMapping(objSiteSampleExpiryMapping, userInfo);
	}
}
