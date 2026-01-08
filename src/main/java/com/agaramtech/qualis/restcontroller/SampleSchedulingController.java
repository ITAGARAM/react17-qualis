package com.agaramtech.qualis.restcontroller;

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
import com.agaramtech.qualis.samplescheduling.model.SampleSchedulingFile;
import com.agaramtech.qualis.samplescheduling.service.samplescheduling.SampleSchedulingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the SampleScheduling Service methods.
 * 
 * @author Mullai Balaji.V [SWSM-17] Sample Scheduling - Screen Development -
 *         Agaram Technologies
 */

@RestController
@RequestMapping("/samplescheduling")
public class SampleSchedulingController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleSchedulingController.class);
	private RequestContext requestContext;
	private final SampleSchedulingService sampleSchedulingService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext          RequestContext to hold the request
	 * @param sampleSchedulingService SampleSchedulingService
	 */
	public SampleSchedulingController(RequestContext requestContext, SampleSchedulingService sampleSchedulingService) {
		super();
		this.requestContext = requestContext;
		this.sampleSchedulingService = sampleSchedulingService;
	}

	/**
	 * This method will is used to make a new entry to samplescheduling table.
	 * 
	 * @param inputMap map object holding params ( samplescheduling
	 *                 [SampleScheduling] object holding details to be added in
	 *                 samplescheduling table, userinfo [UserInfo] holding logged in
	 *                 user details ) Input:{ "samplescheduling": {
	 *                 "nsampleschedulingcode": "masters" }, "userinfo":{
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
	 *         samplescheduling already exists/ list of samplescheduling along with
	 *         the newly added samplescheduling.
	 * @throws Exception exception
	 */

	@PostMapping(value = "/createSampleScheduling")
	public ResponseEntity<Object> createSampleScheduling(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return sampleSchedulingService.createSampleScheduling(inputMap);
	}

	/**
	 * This method will is used to make a new entry to sampleschedulinglocation
	 * table.
	 * 
	 * @param inputMap map object holding params ( sampleschedulinglocation
	 *                 [SampleSchedulingLocation] object holding details to be added
	 *                 in samplescheduling table, userinfo [UserInfo] holding logged
	 *                 in user details ) Input:{ "sampleschedulinglocation": {
	 *                 "nsampleschedulinglocationcode": "masters" }, "userinfo":{
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
	 *         sampleschedulinglocation already exists/ list of
	 *         sampleschedulinglocation along with the newly added
	 *         sampleschedulinglocation.
	 * @throws Exception exception
	 */

	@PostMapping(value = "/createSampleSchedulingLocation")
	public ResponseEntity<Object> createSampleSchedulingLocation(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		return sampleSchedulingService.createSampleSchedulingLocation(inputMap);
	}

	/**
	 * This method will is used to make a record as Planned in
	 * SampleSchedulingHistory Table
	 * 
	 * @param inputMap map object holding params ( samplescheduing [samplescheduing]
	 *                 object holding details to be added in samplescheduing table,
	 *                 userinfo [UserInfo] holding logged in user details ) Input:{
	 *                 "samplescheduing": { "nsamplescheduingcode": "masters" },
	 *                 "userinfo":{ "activelanguagelist": ["en-US"], "isutcenabled":
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
	 *         samplescheduing already exists/ list of samplescheduing along with
	 *         the newly added samplescheduing.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/plannedSampleScheduling")
	public ResponseEntity<Object> plannedSampleScheduling(@RequestBody Map<String, Object> inputMap) throws Exception {
		return sampleSchedulingService.plannedSampleScheduling(inputMap);
	}

	/**
	 * This method is used to update selected sampleschedulinglocation details.
	 * 
	 * @param inputMap [map object holding params( sampleschedulinglocation
	 *                 [sampleschedulinglocation] object holding details to be
	 *                 updated in sampleschedulinglocation table, userinfo
	 *                 [UserInfo] holding logged in user details) Input:{
	 *                 "sampleschedulinglocation":
	 *                 {"nsampleschedulingcode":1,"nsampleschedulinglocationcode":
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
	 *         sampleschedulinglocation record is not available/ list of all
	 *         sampleschedulinglocation and along with the updated
	 *         sampleschedulinglocation.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateSampleSchedulingLocation")
	public ResponseEntity<Object> updateSampleSchedulingLocation(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		return sampleSchedulingService.updateSampleSchedulingLocation(inputMap);
	}

	/**
	 * This Method is used to get the over all samplescheduling with respect to site
	 * at Initital Get
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getSampleScheduling with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSampleScheduling")
	public ResponseEntity<Object> getSampleScheduling(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getSampleScheduling called");
		requestContext.setUserInfo(userInfo);
		return sampleSchedulingService.getSampleScheduling(inputMap, userInfo);
	}

	/**
	 * This Method is used to get the particular record by primarykey in
	 * SampleScheduling Table
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of
	 *         getActiveSampleSchedulingById with respect to site and also have the
	 *         HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveSampleSchedulingById")
	public ResponseEntity<Object> getActiveSampleSchedulingById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int nsampleschedulingcode = (int) inputMap.get("nsampleschedulingcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return sampleSchedulingService.getActiveSampleSchedulingById(nsampleschedulingcode, userInfo);
	}

	/**
	 * This method is used to update selected samplescheduling details.
	 * 
	 * @param inputMap [map object holding params( samplescheduling
	 *                 [samplescheduling] object holding details to be updated in
	 *                 samplescheduling table, userinfo [UserInfo] holding logged in
	 *                 user details) Input:{ "samplescheduling":
	 *                 {"nsampleschedulingcode":1,"nsampleschedulinglocationcode":
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
	 *         sampleschedulinglocation record is not available/ list of all
	 *         sampleschedulinglocation and along with the updated
	 *         sampleschedulinglocation.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateSampleScheduling")
	public ResponseEntity<Object> updateSampleScheduling(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return sampleSchedulingService.updateSampleScheduling(inputMap);
	}

	/**
	 * This Method is used to get the particular record by primarykey in
	 * samplescheduling Table
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getSampleSchedulingRecord
	 *         with respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSampleSchedulingRecord")
	public ResponseEntity<Object> getSampleSchedulingRecord(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		final int nsampleschedulingcode = objMapper.convertValue(inputMap.get("nsampleschedulingcode"), Integer.class);
		requestContext.setUserInfo(userInfo);
		return sampleSchedulingService.getSampleSchedulingRecord(nsampleschedulingcode, userInfo);
	}

	/**
	 * This method is used to delete an entry in samplescheduling table
	 * 
	 * @param inputMap [Map] object with keys of samplescheduling entity and
	 *                 UserInfo object. Input:{ "samplescheduling":
	 *                 {"nsampleschedulingcode":1}, "userinfo":{
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
	 *         samplescheduling record is not available/ string message as 'Record
	 *         is used in....' when the samplescheduling is associated in
	 *         transaction / list of all sampleschedulings excluding the deleted
	 *         record
	 * @throws Exception exception
	 */

	@PostMapping(value = "/deleteSampleScheduling")
	public ResponseEntity<Object> deleteSampleScheduling(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleSchedulingService.deleteSampleScheduling(inputMap);
	}

	/**
	 * This Method is used to create a File in sampleschedulingfile respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of sampleschedulingfile with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/createSampleSchedulingFile")
	public ResponseEntity<Object> createSampleSchedulingFile(MultipartHttpServletRequest request) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleSchedulingService.createSampleSchedulingFile(request, userInfo);
	}

	/**
	 * This Method is used to update a File in sampleschedulingfile respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of sampleschedulingfile with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/editSampleSchedulingFile")
	public ResponseEntity<Object> editSampleSchedulingFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final SampleSchedulingFile objSampleSchedulingFile = objMapper
				.convertValue(inputMap.get("sampleschedulingfile"), SampleSchedulingFile.class);
		requestContext.setUserInfo(userInfo);
		return sampleSchedulingService.editSampleSchedulingFile(objSampleSchedulingFile, userInfo);
	}

	/**
	 * This method is used to edit the file an entry in sampleschedulingfile table
	 * 
	 * @param inputMap [Map] object with keys of sampleschedulingfile entity and
	 *                 UserInfo object. Input:{ "sampleschedulingfile":
	 *                 {"nsampleschedulingfilecode":1}, "userinfo":{
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
	 *         sampleschedulingfile record is not available/ string message as
	 *         'Record is used in....' when the sampleschedulingfile is associated
	 *         in transaction / list of all sampleschedulingfile excluding the edit
	 *         record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateSampleSchedulingFile")
	public ResponseEntity<Object> updateSampleSchedulingFile(MultipartHttpServletRequest request) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleSchedulingService.updateSampleSchedulingFile(request, userInfo);
	}

	/**
	 * This method is used to delete an entry in sampleschedulingfile table
	 * 
	 * @param inputMap [Map] object with keys of sampleschedulingfile entity and
	 *                 UserInfo object. Input:{ "sampleschedulingfile":
	 *                 {"nsampleschedulingfilecode":1}, "userinfo":{
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
	 *         sampleschedulingfile record is not available/ string message as
	 *         'Record is used in....' when the sampleschedulingfile is associated
	 *         in transaction / list of all sampleschedulingfile excluding the
	 *         deleted record
	 * @throws Exception exception
	 */

	@PostMapping(value = "/deleteSampleSchedulingFile")
	public ResponseEntity<Object> deleteSampleSchedulingFile(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final SampleSchedulingFile objSampleSchedulingFile = objMapper
				.convertValue(inputMap.get("sampleschedulingfile"), SampleSchedulingFile.class);
		requestContext.setUserInfo(userInfo);
		return sampleSchedulingService.deleteSampleSchedulingFile(objSampleSchedulingFile, userInfo);
	}

	/**
	 * This method is used to view the file an entry in sampleschedulingfile table
	 * 
	 * @param inputMap [Map] object with keys of sampleschedulingfile entity and
	 *                 UserInfo object. Input:{ "sampleschedulingfile":
	 *                 {"nsampleschedulingfilecode":1}, "userinfo":{
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
	 *         sampleschedulingfile record is not available/ string message as
	 *         'Record is used in....' when the sampleschedulingfile is associated
	 *         in transaction / list of all sampleschedulingfile excluding the view
	 *         record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/viewAttachedSampleSchedulingFile")
	public ResponseEntity<Object> viewAttachedSampleSchedulingFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final SampleSchedulingFile objSampleSchedulingFile = objMapper
				.convertValue(inputMap.get("sampleschedulingfile"), SampleSchedulingFile.class);
		requestContext.setUserInfo(userInfo);
		return sampleSchedulingService.viewAttachedSampleSchedulingFile(objSampleSchedulingFile, userInfo);
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

		return sampleSchedulingService.getRegion(userInfo);

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
		final int nprimarykey = (int) inputMap.get("primarykey");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return sampleSchedulingService.getSubDivisionalLab(nprimarykey, userInfo);
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
		final int nprimarykey = (int) inputMap.get("primarykey");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return sampleSchedulingService.getDistrictLab(nprimarykey, userInfo);
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
	@PostMapping(value = "/getLocation")
	public ResponseEntity<Object> getLocation(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int nprimarykey = (int) inputMap.get("primarykey");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return sampleSchedulingService.getLocation(nprimarykey, userInfo);
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
	@PostMapping(value = "/getVillage")
	public ResponseEntity<Object> getVillage(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int nprimarykey = (int) inputMap.get("primarykey");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
	//added by sujatha ATE_274 SWSM-117 by adding nsitehierarchyconfigcode for getting the approved site hierarchy config villages
		final int nsitehierarchyconfigcode = inputMap.get("nsitehierarchyconfigcode") != null
		        ? (int) inputMap.get("nsitehierarchyconfigcode")
		        : -1;
		requestContext.setUserInfo(userInfo);

		return sampleSchedulingService.getVillage(nprimarykey, userInfo, nsitehierarchyconfigcode);
	}

	/**
	 * This Method is used to get the over all SampleLocation
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getSampleSchedulingLocation
	 *         with respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSampleSchedulingLocation")
	public ResponseEntity<Object> getSampleSchedulingLocation(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final int nprimarykey = (int) inputMap.get("nsampleschedulinglocationcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return sampleSchedulingService.getSampleSchedulingLocation(nprimarykey, userInfo);
	}

	/**
	 * This method is used to delete an entry in sampleschedulinglocation table
	 * 
	 * @param inputMap [Map] object with keys of sampleschedulinglocation entity and
	 *                 UserInfo object. Input:{ "sampleschedulinglocation":
	 *                 {"nsampleschedulinglocationcode":1}, "userinfo":{
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
	 *         sampleschedulinglocation record is not available/ string message as
	 *         'Record is used in....' when the sampleschedulinglocation is
	 *         associated in transaction / list of all sampleschedulinglocation
	 *         excluding the deleted record
	 * @throws Exception exception
	 */

	@PostMapping(value = "/deleteSampleSchedulingLocation")
	public ResponseEntity<Object> deleteSampleSchedulingLocation(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return sampleSchedulingService.deleteSampleSchedulingLocation(inputMap, userInfo);
	}

	/**
	 * This Method is used to get the over all sampleschedulingdata and History
	 * Record and File While refresh respect to site
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return a response entity which holds the list of getSampleSchedulingData
	 *         with respect to site and also have the HTTP response code
	 * @throws Exception
	 */

	@PostMapping(value = "/getSampleSchedulingData")
	public ResponseEntity<Object> getSampleSchedulingData(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleSchedulingService.getSampleSchedulingData(inputMap, userInfo);

	}

}
