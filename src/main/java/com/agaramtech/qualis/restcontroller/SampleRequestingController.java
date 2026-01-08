package com.agaramtech.qualis.restcontroller;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.samplescheduling.model.SampleRequestingFile;
import com.agaramtech.qualis.samplescheduling.service.samplerequesting.SampleRequestingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/samplerequesting")
public class SampleRequestingController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleRequestingController.class);
	private RequestContext requestContext;
	private final SampleRequestingService sampleRequestingService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext          RequestContext to hold the request
	 * @param sampleRequestingService SampleRequestingService
	 */
	public SampleRequestingController(RequestContext requestContext, SampleRequestingService sampleRequestingService) {
		super();
		this.requestContext = requestContext;
		this.sampleRequestingService = sampleRequestingService;
	}

	/**
	 * This Method is used to get the over all Region,District,Taluka,City
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getRegion with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getRegion")
	public ResponseEntity<Object> getRegion(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		
		int nsamplerequestingcode = inputMap.containsKey("nsamplerequestingcode") ? (int) inputMap.get("nsamplerequestingcode") : -1;
		

		return sampleRequestingService.getRegion(userInfo,nsamplerequestingcode);

	}

	/**
	 * This Method is used to get the over all SubDivisionalLab
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getSubDivisionalLab with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSubDivisionalLab")
	public ResponseEntity<Object> getSubDivisionalLab(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int regionCode = (int) inputMap.get("regionCode");
		final int districtCode = (int) inputMap.get("districtCode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return sampleRequestingService.getSubDivisionalLab(regionCode, districtCode, userInfo);
	}

	/**
	 * This Method is used to get the over all DistrictLab
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getDistrictLab with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getDistrictLab")
	public ResponseEntity<Object> getDistrictLab(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int regionCode = (int) inputMap.get("regionCode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return sampleRequestingService.getDistrictLab(regionCode, userInfo);
	}

	/**
	 * This Method is used to get the over all Location
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getLocation with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	// modified by sujatha ATE_274 swsm-78 for getting location by checking one more condition
	@PostMapping(value = "/getLocation")
	public ResponseEntity<Object> getLocation(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int regionCode = (int) inputMap.get("regionCode");
		final int districtCode = (int) inputMap.get("districtCode");
		final int cityCode = (int) inputMap.get("cityCode");
		final int villageCode = (int) inputMap.get("villageCode");
		final int sitehierarchyconfigcode=(int) inputMap.get("sitehierarchyconfigcode");
		final int nsampleschedulingcode=(int) inputMap.get("sampleschedulingcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return sampleRequestingService.getLocation(sitehierarchyconfigcode, nsampleschedulingcode, regionCode, 
				districtCode, cityCode, villageCode, userInfo);
	}

	/**
	 * This Method is used to get the over all Villages
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getVillage with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getVillageBasedOnSiteHierarchy")
	public ResponseEntity<Object> getVillageBasedOnSiteHierarchy(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int regionCode = (int) inputMap.get("regionCode");
		final int districtCode = (int) inputMap.get("districtCode");
		final int cityCode = (int) inputMap.get("cityCode");
		//modified by sujatha ATE-274 swsm-78 for one more condition check while getting village
		final int sampleschedulingcode=(int) inputMap.get("sampleschedulingcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return sampleRequestingService.getVillageBasedOnSiteHierarchy(sampleschedulingcode,regionCode, districtCode, cityCode, userInfo);
	}

	/**
	 * This Method is used to get the over all Periods from the Sample Scheduling
	 * Screen
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getPeriod with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getPeriod")
	public ResponseEntity<Object> getPeriod(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		
		final String operation = (String) inputMap.get("operation");

		 String sfromyear;
		 if (!"update".equalsIgnoreCase(operation))		{
		final CharSequence date = (CharSequence) inputMap.get("date");

		int fromYear = Instant.parse(date).atZone(ZoneId.systemDefault()).getYear();
		 sfromyear = String.valueOf(fromYear);
		}
		else
		{
			final String fromyear = (String) inputMap.get("date");
			sfromyear=fromyear;

		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return sampleRequestingService.getPeriod(sfromyear, userInfo);
	}

	/**
	 * This method will is used to make a new entry to samplerequesting table.
	 * 
	 * @param inputMap map object holding params ( samplerequesting
	 *                 [SampleRequesting] object holding details to be added in
	 *                 samplerequesting table, userinfo [UserInfo] holding logged in
	 *                 user details ) Input:{ "samplerequesting": {
	 *                 "nsamplerequestingcode": "masters" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 280,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exist s' if the
	 *         samplerequesting already exists/ list of samplerequesting along with
	 *         the newly added samplerequesting.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createSampleRequesting")
	public ResponseEntity<Object> createSampleRequesting(@RequestBody Map<String, Object> inputMap) throws Exception {

		return sampleRequestingService.createSampleRequesting(inputMap);
	}

	/**
	 * This Method is used to get the over all samplerequesting with respect to site
	 * at Initital Get
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getSampleRequesting with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSampleRequesting")
	public ResponseEntity<Object> getSampleRequesting(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getSampleRequesting called");
		requestContext.setUserInfo(userInfo);
		return sampleRequestingService.getSampleRequesting(inputMap, userInfo);
	}

	/**
	 * This Method is used to get the over all samplerequestingdata and History
	 * Record and File While refresh respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getSampleRequestingData
	 *         with respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSampleRequestingData")
	public ResponseEntity<Object> getSampleRequestingData(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleRequestingService.getSampleRequestingData(inputMap, userInfo);

	}

	/**
	 * This Method is used to get the over all samplerequestingdata and History
	 * Record and File While refresh respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getSampleRequestingRecord
	 *         with respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSampleRequestingRecord")
	public ResponseEntity<Object> getSampleRequestingRecord(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int nsamplerequestingcode = objMapper.convertValue(inputMap.get("nsamplerequestingcode"), Integer.class);
		requestContext.setUserInfo(userInfo);
		return sampleRequestingService.getSampleRequestingRecord(nsamplerequestingcode, userInfo);
	}

	/**
	 * This Method is used to get the particular record by primarykey in
	 * SampleRequesting Table
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of
	 *         getActiveSampleRequestingById with respect to site and also have the
	 *         HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveSampleRequestingById")
	public ResponseEntity<Object> getActiveSampleRequestingById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int nsamplerequestingcode = (int) inputMap.get("nsamplerequestingcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return sampleRequestingService.getActiveSampleRequestingById(nsamplerequestingcode, userInfo);
	}

	/**
	 * This method is used to update selected samplerequesting details.
	 * 
	 * @param inputMap [map object holding params( samplerequesting
	 *                 [samplerequesting] object holding details to be updated in
	 *                 samplerequesting table, userinfo [UserInfo] holding logged in
	 *                 user details) Input:{ "samplerequesting":
	 *                 {"nsamplerequestingcode":1,"nsamplerequestinglocationcode":
	 *                 1,"nregioncode": 1,"ndistrictcode": 1,"ncitycode":
	 *                 1,"nvillagecode": 1, "nlocationcode": 1, "ndefaultstatus": 3
	 *                 }, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 280,"nmastersitecode": -1, "nmodulecode": 1, "nreasoncode":
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
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         samplerequestinglocation record is not available/ list of all
	 *         samplerequestinglocation and along with the updated
	 *         samplerequestinglocation.
	 * @throws Exception exception
	 */

	@PostMapping(value = "/updateSampleRequesting")
	public ResponseEntity<Object> updateSampleRequesting(@RequestBody Map<String, Object> inputMap) throws Exception {

		return sampleRequestingService.updateSampleRequesting(inputMap);
	}

	/**
	 * This method is used to delete an entry in samplerequesting table
	 * 
	 * @param inputMap [Map] object with keys of samplerequesting entity and
	 *                 UserInfo object. Input:{ "samplerequesting":
	 *                 {"nsamplerequesting code":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 280,"nmastersitecode": -1, "nmodulecode": 1,
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
	 *         samplerequesting record is not available/ string message as 'Record
	 *         is used in....' when the samplerequesting is associated in
	 *         transaction / list of all samplerequesting excluding the deleted
	 *         record
	 * @throws Exception exception
	 */

	@PostMapping(value = "/deleteSampleRequesting")
	public ResponseEntity<Object> deleteSampleRequesting(@RequestBody Map<String, Object> inputMap) throws Exception {

		return sampleRequestingService.deleteSampleRequesting(inputMap);
	}

	/**
	 * This Method is used to create a File in samplerequestingfile respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of samplerequestingfile with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/createSampleRequestingFile")
	public ResponseEntity<Object> createSampleRequestingFile(MultipartHttpServletRequest request) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleRequestingService.createSampleRequestingFile(request, userInfo);
	}

	/**
	 * This Method is used to update a File in samplerequestingfile respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of samplerequestingfile with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/editSampleRequestingFile")
	public ResponseEntity<Object> editSampleRequestingFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final SampleRequestingFile objSampleRequestingFile = objMapper
				.convertValue(inputMap.get("samplerequestingfile"), SampleRequestingFile.class);
		requestContext.setUserInfo(userInfo);
		return sampleRequestingService.editSampleRequestingFile(objSampleRequestingFile, userInfo);
	}

	/**
	 * This method is used to edit the file an entry in samplerequestingfile table
	 * 
	 * @param inputMap [Map] object with keys of samplerequestingfile entity and
	 *                 UserInfo object. Input:{ "samplerequestingfile":
	 *                 {"samplerequestingfile":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 280,"nmastersitecode": -1, "nmodulecode": 1,
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
	 *         samplerequestingfile record is not available/ string message as
	 *         'Record is used in....' when the samplerequestingfile is associated
	 *         in transaction / list of all samplerequestingfile excluding the edit
	 *         record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateSampleRequestingFile")
	public ResponseEntity<Object> updateSampleRequestingFile(MultipartHttpServletRequest request) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleRequestingService.updateSampleRequestingFile(request, userInfo);
	}

	/**
	 * This method is used to delete an entry in samplerequestingfile table
	 * 
	 * @param inputMap [Map] object with keys of samplerequestingfile entity and
	 *                 UserInfo object. Input:{ "samplerequestingfile":
	 *                 {"nsamplerequestingfilecode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 280,"nmastersitecode": -1, "nmodulecode": 1,
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
	 *         samplerequestingfile record is not available/ string message as
	 *         'Record is used in....' when the samplerequestingfile is associated
	 *         in transaction / list of all samplerequestingfile excluding the
	 *         deleted record
	 * @throws Exception exception
	 */

	@PostMapping(value = "/deleteSampleRequestingFile")
	public ResponseEntity<Object> deleteSampleRequestingFile(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final SampleRequestingFile objSampleRequestingFile = objMapper
				.convertValue(inputMap.get("samplerequestingfile"), SampleRequestingFile.class);
		requestContext.setUserInfo(userInfo);
		return sampleRequestingService.deleteSampleRequestingFile(objSampleRequestingFile, userInfo);
	}

	/**
	 * This method is used to view the file an entry in samplerequestingfile table
	 * 
	 * @param inputMap [Map] object with keys of samplerequestingfile entity and
	 *                 UserInfo object. Input:{ "samplerequestingfile":
	 *                 {"nsamplerequestingfilecode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 280,"nmastersitecode": -1, "nmodulecode": 1,
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
	 *         samplerequestingfile record is not available/ string message as
	 *         'Record is used in....' when the samplerequestingfile is associated
	 *         in transaction / list of all samplerequestingfile excluding the view
	 *         record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/viewAttachedSampleRequestingFile")
	public ResponseEntity<Object> viewAttachedSampleRequestingFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final SampleRequestingFile objSampleRequestingFile = objMapper
				.convertValue(inputMap.get("samplerequestingfile"), SampleRequestingFile.class);
		requestContext.setUserInfo(userInfo);
		return sampleRequestingService.viewAttachedSampleRequestingFile(objSampleRequestingFile, userInfo);
	}

	/**
	 * This Method is used to get the over all SampleLocation
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getSampleRequestingLocation
	 *         with respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSampleRequestingLocation")
	public ResponseEntity<Object> getSampleRequestingLocation(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int nprimarykey = (int) inputMap.get("nsamplerequestinglocationcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return sampleRequestingService.getSampleRequestingLocation(nprimarykey, userInfo);
	}

	/**
	 * This method is used to update selected samplerequestinglocation details.
	 * 
	 * @param inputMap [map object holding params( samplerequestinglocation
	 *                 [samplerequestinglocation] object holding details to be
	 *                 updated in samplerequestinglocation table, userinfo
	 *                 [UserInfo] holding logged in user details) Input:{
	 *                 "samplerequestinglocation":
	 *                 {"nsamplerequestingcode":1,"nsamplerequestinglocationcode":
	 *                 1,"nregioncode": 1,"ndistrictcode": 1,"ncitycode":
	 *                 1,"nvillagecode": 1, "nlocationcode": 1, "ndefaultstatus": 3
	 *                 }, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 280,"nmastersitecode": -1, "nmodulecode": 1, "nreasoncode":
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
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         samplerequestinglocation record is not available/ list of all
	 *         samplerequestinglocation and along with the updated
	 *         samplerequestinglocation.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateSampleRequestingLocation")
	public ResponseEntity<Object> updateSampleRequestingLocation(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		return sampleRequestingService.updateSampleRequestingLocation(inputMap);
	}

	/**
	 * This method is used to delete an entry in samplerequestinglocation table
	 * 
	 * @param inputMap [Map] object with keys of samplerequestinglocation entity and
	 *                 UserInfo object. Input:{ "samplerequestinglocation":
	 *                 {"nsamplerequestinglocationcode":1}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 280,"nmastersitecode": -1, "nmodulecode": 1,
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
	 *         samplerequestinglocation record is not available/ string message as
	 *         'Record is used in....' when the samplerequestinglocation is
	 *         associated in transaction / list of all samplerequestinglocation
	 *         excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteSampleRequestingLocation")
	public ResponseEntity<Object> deleteSampleRequestingLocation(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return sampleRequestingService.deleteSampleRequestingLocation(inputMap, userInfo);
	}

	/**
	 * This Method is used to get the over all Villages
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getVillage with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getVillages")
	public ResponseEntity<Object> getVillages(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int nprimarykey = (int) inputMap.get("primarykey");
	//added by sujatha ATE_274 SWSM-117 by getting  villages by checking districtcode & regionCode
		final int districtCode = (int) inputMap.get("ndistrictcode");
		final int regionCode = (int) inputMap.get("nregioncode");
		//modified by sujatha ATE_274 SWSM-78 for one more check while getting villages
		final int nsampleschedulingcode=(int) inputMap.get("nsampleschedulingcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return sampleRequestingService.getVillages(nsampleschedulingcode,regionCode, districtCode, nprimarykey, userInfo);
	}

	/**
	 * This method will is used to make a record as Scheduled in
	 * SampleRequestingHistory Table
	 * 
	 * @param inputMap map object holding params ( samplerequesting
	 *                 [samplerequesting] object holding details to be added in
	 *                 samplerequesting table, userinfo [UserInfo] holding logged in
	 *                 user details ) Input:{ "samplerequesting": {
	 *                 "nsamplerequestingcode": "masters" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 280,"nmastersitecode": -1,"nmodulecode": 1,
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
	 *         samplerequesting already exists/ list of samplerequesting along with
	 *         the newly added samplerequesting.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/scheduledSampleRequesting")
	public ResponseEntity<Object> scheduledSampleRequesting(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		return sampleRequestingService.scheduledSampleRequesting(inputMap);
	}

	/**
	 * This method will is used to make a record as Completed in
	 * SampleRequestingHistory Table
	 * 
	 * @param inputMap map object holding params ( samplerequesting
	 *                 [samplerequesting] object holding details to be added in
	 *                 samplerequesting table, userinfo [UserInfo] holding logged in
	 *                 user details ) Input:{ "samplerequesting": {
	 *                 "nsamplerequestingcode": "masters" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 280,"nmastersitecode": -1,"nmodulecode": 1,
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
	 *         samplerequesting already exists/ list of samplerequesting along with
	 *         the newly added samplerequesting.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/completedSampleRequesting")
	public ResponseEntity<Object> completedSampleRequesting(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		return sampleRequestingService.completedSampleRequesting(inputMap);
	}

	/**
	 * This method will is used to make a record as Planned in
	 * SampleRequestingHistory Table
	 * 
	 * @param inputMap map object holding params ( samplerequesting
	 *                 [samplerequesting] object holding details to be added in
	 *                 samplerequesting table, userinfo [UserInfo] holding logged in
	 *                 user details ) Input:{ "samplerequesting": {
	 *                 "nsamplerequestingcode": "masters" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 280,"nmastersitecode": -1,"nmodulecode": 1,
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
	 *         samplerequesting already exists/ list of samplerequesting along with
	 *         the newly added samplerequesting.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/plannedSampleRequesting")
	public ResponseEntity<Object> plannedSampleRequesting(@RequestBody Map<String, Object> inputMap) throws Exception {
		return sampleRequestingService.plannedSampleRequesting(inputMap);
	}

	/**
	 * This method will is used to make a new entry to samplerequestinglocation
	 * table.
	 * 
	 * @param inputMap map object holding params ( samplerequestinglocation
	 *                 [SampleRequestingLocation] object holding details to be added
	 *                 in samplescheduling table, userinfo [UserInfo] holding logged
	 *                 in user details ) Input:{ "samplerequestinglocation": {
	 *                 "nsamplerequestinglocationcode": "masters" }, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 280,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exist s' if the
	 *         samplerequestinglocation already exists/ list of
	 *         samplerequestinglocation along with the newly added
	 *         samplerequestinglocation.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createSampleRequestingLocation")
	public ResponseEntity<Object> createSampleRequestingLocation(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		return sampleRequestingService.createSampleRequestingLocation(inputMap);
	}
	
	//Added by sonia on 4th oct 2025 for jira id:SWSM-77
		/**
		 * This method is used to Sent the Report By Mail.
		 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and ntranssitecode [int] primary key of site object for 
		 * which the list is to be fetched 	
		 * Input : {"userinfo":{ntranssitecode": 1},
		 * "ncontrolcode":1545,
		 * "nsamplerequestingcode":1,
		 * "ntransactionstatus":41							
		 * }				
		 * @return response entity  object holding response status as success
		 * @throws Exception exception
		 */
	@PostMapping(value = "/sendReportByMail")
	public ResponseEntity<Object> sendReportByMail(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleRequestingService.sendReportByMail(inputMap, userInfo);
	}

}
