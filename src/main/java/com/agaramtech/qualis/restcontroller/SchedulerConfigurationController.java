package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.scheduler.service.SchedulerConfigurationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

//ALPD-4941 Created SchedulerConfigurationController for Scheduler configuration screen
@RestController
@RequestMapping("/schedulerconfiguration")
public class SchedulerConfigurationController {

	private static final Log LOGGER = LogFactory.getLog(SchedulerConfigurationController.class);

	private RequestContext requestContext;
	private SchedulerConfigurationService schedulerConfigurationService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param schedulerConfigurationService SchedulerConfigurationService
	 */
	public SchedulerConfigurationController(RequestContext requestContext,
			SchedulerConfigurationService schedulerConfigurationService) {
		super();
		this.requestContext = requestContext;
		this.schedulerConfigurationService = schedulerConfigurationService;
	}

	@RequestMapping(value = "/getSchedulerConfiguration", method = RequestMethod.POST)
	public ResponseEntity<Object> getSchedulerConfiguration(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return schedulerConfigurationService.getSchedulerConfiguration(inputMap, userInfo);
	}

	@RequestMapping(value = "/getSampleType", method = RequestMethod.POST)
	public ResponseEntity<Object> getSampleType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return schedulerConfigurationService.getSampleType(inputMap, userInfo);
	}

	@PostMapping(value = "/getRegistrationType")
	public ResponseEntity<Object> getRegistrationType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final int nSampleType = (int) inputMap.get("nsampletypecode");
		return schedulerConfigurationService.getRegistrationType(nSampleType, inputMap, userInfo);
	}

	@PostMapping(value = "/getRegistrationSubType")
	public ResponseEntity<Object> getRegistrationSubType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final int nregTypeCode = (int) inputMap.get("nregtypecode");
		return schedulerConfigurationService.getRegistrationSubType(nregTypeCode, inputMap, userInfo);
	}

	@PostMapping(value = "/getApprovalConfigVersion")
	public ResponseEntity<Object> getApprovalConfigVersion(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final int nregTypeCode = (int) inputMap.get("nregtypecode");
		final int nregSubTypeCode = (int) inputMap.get("nregsubtypecode");
		return schedulerConfigurationService.getApprovalConfigVersion(nregTypeCode, nregSubTypeCode, inputMap,
				userInfo);
	}

	@PostMapping(value = "/getApproveConfigVersionRegTemplate")
	public ResponseEntity<Object> getApproveConfigVersionRegTemplate(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		final int nregTypeCode = (int) inputMap.get("nregtypecode");
		final int nregSubTypeCode = (int) inputMap.get("nregsubtypecode");
		final int napproveConfigVersionCode = (int) inputMap.get("napproveconfversioncode");
		return schedulerConfigurationService.getApproveConfigVersionRegTemplate(nregTypeCode, nregSubTypeCode,
				napproveConfigVersionCode, inputMap, userInfo);
	}

	@PostMapping(value = "/getSchedulerConfigByFilterSubmit")
	public ResponseEntity<Object> getSchedulerConfigByFilterSubmit(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return schedulerConfigurationService.getSchedulerConfigByFilterSubmit(inputMap, userInfo);
	}

	@PostMapping(value = "/createSchedulerConfig")
	public ResponseEntity<Object> createSchedulerConfig(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return schedulerConfigurationService.createSchedulerConfig(inputMap, userInfo);
	}

	@PostMapping(value = "/getSchedulerConfigSubSample")
	public ResponseEntity<Object> getSchedulerConfigSubSample(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return schedulerConfigurationService.getSchedulerConfigSubSample(inputMap, userInfo);
	}

	@PostMapping(value = "/getSchedulerConfigTest")
	public ResponseEntity<Object> getSchedulerConfigTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return schedulerConfigurationService.getSchedulerConfigTest(inputMap, userInfo);
	}

	// ALPD-4941--Vignesh R(09-12-2024)---Sample configuration screen
	@PostMapping(value = "/getSchedulerConfigParameter")
	public ResponseEntity<Object> getSchedulerConfigParameter(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return schedulerConfigurationService.getSchedulerConfigParameter(inputMap, userInfo);

	}

	@PostMapping(value = "/getComponentBySpec")
	public ResponseEntity<Object> getComponentBySpec(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return schedulerConfigurationService.getComponentBySpec(inputMap);

	}

	@PostMapping(value = "/getTestfromDB")
	public ResponseEntity<Object> getTestfromDB(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return schedulerConfigurationService.getTestfromDB(inputMap);

	}

	@PostMapping(value = "/getTestfromTestPackage")
	public ResponseEntity<Object> getTestfromTestPackage(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return schedulerConfigurationService.getTestfromTestPackage(inputMap);

	}

	@PostMapping(value = "/getTestfromSection")
	public ResponseEntity<Object> getTestfromSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return schedulerConfigurationService.getTestfromSection(inputMap);

	}

	@PostMapping(value = "/getTestBasedTestSection")
	public ResponseEntity<Object> getTestBasedTestSection(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return schedulerConfigurationService.getTestBasedTestSection(inputMap);

	}

	@PostMapping(value = "/createSubSample")
	public ResponseEntity<Object> createSubSample(@RequestBody Map<String, Object> inputMap) throws Exception {

		return schedulerConfigurationService.createSubSample(inputMap);

	}

	@PostMapping(value = "/updateSchedulerConfigSubSample")
	public ResponseEntity<Object> updateSchedulerConfigSubSample(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		return schedulerConfigurationService.updateSchedulerConfigSubSample(inputMap);

	}

	@PostMapping(value = "/getEditSchedulerSubSampleComboService")
	public ResponseEntity<Object> getEditSchedulerSubSampleComboService(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return schedulerConfigurationService.getEditSchedulerSubSampleComboService(inputMap, userInfo);

	}

	@PostMapping(value = "/deleteSchedulerConfigSubSample")
	public ResponseEntity<Object> deleteSchedulerConfigSubSample(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		return schedulerConfigurationService.deleteSchedulerConfigSubSample(inputMap);

	}

	@PostMapping(value = "/getMoreTestSection")
	public ResponseEntity<Object> getMoreTestSection(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("UserInfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return schedulerConfigurationService.getMoreTestSection(inputMap, userInfo);
	}

	@PostMapping(value = "/getMoreTest")
	public ResponseEntity<Object> getMoreTest(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("UserInfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return schedulerConfigurationService.getMoreTest(inputMap, userInfo);

	}

	@PostMapping(value = "/getMoreTestPackage")
	public ResponseEntity<Object> getMoreTestPackage(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("UserInfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return schedulerConfigurationService.getMoreTestPackage(inputMap, userInfo);

	}

	@PostMapping(value = "/createTest")
	public ResponseEntity<Object> createTest(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return schedulerConfigurationService.createTest(inputMap, userInfo);

	}

	@PostMapping(value = "/deleteSchedulerConfigTest")
	public ResponseEntity<Object> deleteSchedulerConfigTest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return schedulerConfigurationService.deleteSchedulerConfigTest(inputMap, userInfo);

	}

	@PostMapping(value = "/getEditSchedulerConfigDetails")
	public ResponseEntity<Object> getEditSchedulerConfigDetails(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return schedulerConfigurationService.getEditSchedulerConfigDetails(inputMap, userInfo);

	}

	@PostMapping(value = "/updateSchedulerConfig")
	public ResponseEntity<Object> updateSchedulerConfig(@RequestBody Map<String, Object> inputMap) throws Exception {


		return schedulerConfigurationService.updateSchedulerConfig(inputMap);

	}

	@PostMapping(value = "/getSiteByUser")
	public ResponseEntity<Object> getSiteByUser(@RequestBody Map<String, Object> inputMap) throws Exception {

		return schedulerConfigurationService.getSiteByUser(inputMap);

	}

	@PostMapping(value = "/approveSchedulerConfig")
	public ResponseEntity<Object> approveSchedulerConfig(@RequestBody Map<String, Object> inputMap) throws Exception {

		return schedulerConfigurationService.approveSchedulerConfig(inputMap);

	}

	@PostMapping(value = "/getSchedulerMaster")
	public ResponseEntity<Object> getSchedulerMaster(@RequestBody Map<String, Object> inputMap) throws Exception {

		return schedulerConfigurationService.getSchedulerMaster(inputMap);

	}

	@PostMapping(value = "/deleteSchedulerConfig")
	public ResponseEntity<Object> deleteSchedulerConfig(@RequestBody Map<String, Object> inputMap) throws Exception {

		return schedulerConfigurationService.deleteSchedulerConfig(inputMap);

	}

	@PostMapping(value = "/updateActiveStatusSchedulerConfig")
	public ResponseEntity<Object> updateActiveStatusSchedulerConfig(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		return schedulerConfigurationService.updateActiveStatusSchedulerConfig(inputMap);

	}

	@PostMapping(value = "/getSchedulerMasteDetails")
	public ResponseEntity<Object> getSchedulerMasteDetails(@RequestBody Map<String, Object> inputMap) throws Exception {

		return schedulerConfigurationService.getSchedulerMasteDetails(inputMap);

	}
}
