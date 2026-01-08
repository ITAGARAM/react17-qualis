package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.biobank.model.SampleCodeCreation;
import com.agaramtech.qualis.biobank.service.samplecodecreation.SampleCodeCreationService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the samplecodecreation Service methods
 */
@RestController
@RequestMapping("/samplecodecreation")
public class SampleCodeCreationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleCodeCreationController.class);

	private final RequestContext requestContext;
	private final SampleCodeCreationService sampleCodeCreationService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext              RequestContext to hold the request
	 * @param sampleCodeCreationService sampleCodeCreationService
	 */
	public SampleCodeCreationController(final RequestContext requestContext,
			final SampleCodeCreationService sampleCodeCreationService) {
		super();
		this.requestContext = requestContext;
		this.sampleCodeCreationService = sampleCodeCreationService;
	}

	/**
	 * This method is used to retrieve list of available samplecodecreation(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         samplecodecreations
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSampleCodeCreation")
	public ResponseEntity<Object> getSampleCodeCreation(@RequestBody final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getSampleCodeCreation called");
		requestContext.setUserInfo(userInfo);
		return sampleCodeCreationService.getSampleCodeCreation(userInfo);
	}
	
	/**
	 * This method is used to retrieve a specific samplecodecreation record.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return ResponseEntity with SampleCodeCreation object for the specified
	 *         primary key / with string message as 'Already Deleted' if the
	 *         samplecodecreation record is not available
	 * @throws Exception exception
	 */
	
	@PostMapping(value = "/getProduct")
	public ResponseEntity<Object> getProduct(@RequestBody final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getProduct called");
		requestContext.setUserInfo(userInfo);
		return sampleCodeCreationService.getProduct(userInfo);
	}

	/**
	 * This method is used to retrieve a specific samplecodecreation record.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return ResponseEntity with SampleCodeCreation object for the specified
	 *         primary key / with string message as 'Already Deleted' if the
	 *         samplecodecreation record is not available
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getActiveSampleCodeCreationById")
	public ResponseEntity<Object> getActiveSampleCodeCreationById(@RequestBody final Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nsamplecodecreationcode = (Integer) inputMap.get("nproductcodemappingcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleCodeCreationService.getActiveSampleCodeCreationById(nsamplecodecreationcode, userInfo);
	}

	/**
	 * This method will is used to make a new entry to samplecodecreation table.
	 * 
	 * @param inputMap map object holding params ( 
	 *            sampleCollectionType [SampleCodeCreation] object holding details to be added in samplecodecreation table,
	 *            userinfo [UserInfo] holding logged in user details ) 
	 *            Input:{ 
	 *                "samplecodecreation": {
	 *                       "nprojecttypecode": 1, "scode": 03,"nproductcode": 3},
	 *                 "userinfo":{ 
	 *                       "activelanguagelist": ["en-US"], "isutcenabled": 4,
	 *                       "ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
	 *                       "nformcode": 190,"nmastersitecode": -1,"nmodulecode": 76,
	 *                       "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
	 *                       "ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,
	 *                       "nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                       "sgmtoffset": "UTC +00:00","slanguagefilename": "Msg_en_US",
	 *                       "slanguagename": "English", "slanguagetypecode": "en-US",
	 *                       "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                       "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason": "",
	 *                       "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"} }
	 *                       
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         samplecodecreation already exists/ list of samplecodecreation
	 *         along with the newly added samplecodecreation.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/createSampleCodeCreation")
	public ResponseEntity<Object> createSampleCodeCreation(@RequestBody final Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final SampleCodeCreation sampleCodeCeartion = objMapper.convertValue(inputMap.get("samplecodecreation"),
				SampleCodeCreation.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleCodeCreationService.createSampleCodeCreation(sampleCodeCeartion, userInfo);
	}

	/**
	 * This method is used to update selected samplecodecreation details.
	 * 
	 * @param inputMap [map object holding params( 
	 *            samplecodecreation [SampleCodeCreation] object holding details to be updated in samplecodecreation table,
	 *            userinfo [UserInfo] holding logged in user details) 
	 *            Input:{ 
	 *                "samplecodecreation":{
	 *                      "nprojecttypecode":1,"nproductcode": 3,"scode": 04, },
	 *                 "userinfo":{ 
	 *                      "activelanguagelist": ["en-US"],"isutcenabled": 4,
	 *                      "ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
	 *                      "nformcode": 190,"nmastersitecode": -1, "nmodulecode": 76,
	 *                      "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
	 *                      "ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,
	 *                      "nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                      "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US",
	 *                      "slanguagename": "English", "slanguagetypecode": "en-US",
	 *                      "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                      "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                      "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         samplecodecreation record is not available/ list of all
	 *         samplecodecreations and along with the updated
	 *         samplecodecreations.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/updateSampleCodeCreation")
	public ResponseEntity<Object> updateSampleCodeCreation(@RequestBody final Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final SampleCodeCreation collectiontubetype = objMapper.convertValue(inputMap.get("samplecodecreation"),
				SampleCodeCreation.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleCodeCreationService.updateSampleCodeCreation(collectiontubetype, userInfo);
	}

	/**
	 * This method is used to delete an entry in SampleCodeCreation table
	 * 
	 * @param inputMap [Map] object with keys of SampleCodeCreation entity and UserInfo object. 
	 *               Input:{ 
	 *                     "samplecodecreation":{"nsamplecodecreationcode":1}, 
	 *                     "userinfo":{
	 *                            "activelanguagelist": ["en-US"],"isutcenabled": 4,
	 *                            "ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole": -1,
	 *                            "nformcode": 190,"nmastersitecode": -1, "nmodulecode": 76,
	 *                            "nreasoncode": 0,"nsitecode": 1,"ntimezonecode": -1,
	 *                            "ntranssitecode": 1,"nusercode": -1, "nuserrole": -1,
	 *                            "nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                            "sgmtoffset": "UTC +00:00", "slanguagefilename": "Msg_en_US",
	 *                            "slanguagename": "English", "slanguagetypecode": "en-US",
	 *                            "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                            "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                            "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 *                            
	 * @return ResponseEntity with string message as 'Already deleted' if the samplecodecreation
	 *         record is not available/ string message as 'Record is used in....'
	 *         when the samplecodecreation is associated in transaction / list of all samplecodecreations
	 *         excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteSampleCodeCreation")
	public ResponseEntity<Object> deleteSampleCodeCreation(@RequestBody final Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final SampleCodeCreation samplecodecreation = objMapper.convertValue(inputMap.get("samplecodecreation"),
				SampleCodeCreation.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleCodeCreationService.deleteSampleCodeCreation(samplecodecreation, userInfo);
	}
}
