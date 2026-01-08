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
import com.agaramtech.qualis.testmanagement.model.TestPackageTest;
import com.agaramtech.qualis.testmanagement.service.pacakagetestmapping.PackageTestMappingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/packagetestmapping")
public class PackageTestMappingController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PackageTestMappingController.class);

	private final RequestContext requestContext;

	private final PackageTestMappingService parameterTestMappingService;

	public PackageTestMappingController(RequestContext requestContext, PackageTestMappingService parameterTestMappingService) {
		super();
		this.requestContext = requestContext;
		this.parameterTestMappingService = parameterTestMappingService;
	}

	/**
	 * This Method is used to get the over all Test  with respect to Package
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Test Package with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	
	@PostMapping(value = "/getPackageTestMapping")
	public ResponseEntity<Object> getPackageTestMapping(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Integer nTestpackageCode = null;

		if (inputMap.get("ntestpackagecode") != null) {
			nTestpackageCode = (Integer) inputMap.get("ntestpackagecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getParameterTestMapping called");
		requestContext.setUserInfo(userInfo);
		return parameterTestMappingService.getPackageTestMapping(nTestpackageCode, userInfo);

	}
	
	/**
	 * This Method is used to get the over all Test Category with respect to site
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Test Category with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/getTestCategory")
	public ResponseEntity<Object> getTestCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getTestCategory called");
		requestContext.setUserInfo(userInfo);
		return parameterTestMappingService.getTestCategory(inputMap, userInfo);

	}
	
	/**
	 * This Method is used to get the over all Test with respect to Test Category
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Package Test with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/getTestPackageTest")
	public ResponseEntity<Map<String, Object>> getTestPackageTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getTestPackageTest called");
		requestContext.setUserInfo(userInfo);
		return parameterTestMappingService.getTestPackageTest(inputMap, userInfo);

	}
	
	/**
	 * This method is used to create a new entry in the testpackagetest table. 
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of  Package Test with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/createPackageTestMapping")
	public ResponseEntity<Object> createPackageTestMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper=new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("createPackageTestMapping called");
		requestContext.setUserInfo(userInfo);
		return parameterTestMappingService.createPackageTestMapping(inputMap, userInfo);

	}
	
	/**
	 * This method is used to delete a package test from the testpackagetest table based on the respective ntestpackagetestcode.”
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Package Test with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/deletePackageTestMapping")
	public ResponseEntity<Object> deletePackageTestMapping(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final TestPackageTest objTestPackageTest = objectMapper.convertValue(inputMap.get("packagetestmapping"),
				new TypeReference<TestPackageTest>() {
				});
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		LOGGER.info("deletePackageTestMapping called");

		requestContext.setUserInfo(userInfo);
		return parameterTestMappingService.deletePackageTestMapping(objTestPackageTest, userInfo);
	}
	
	/**
	 *This method retrieves the overall list of tests organized package-wise.
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Package Test with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/getTestPackageTestMapping")
	public ResponseEntity<Map<String, Object>> getTestPackageTestMapping(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Integer nTestpackageCode = null;

		if (inputMap.get("ntestpackagecode") != null) {
			nTestpackageCode = (Integer) inputMap.get("ntestpackagecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getParameterTestMapping called");
		requestContext.setUserInfo(userInfo);
		return parameterTestMappingService.getTestPackageTestMapping(nTestpackageCode, userInfo);

	}
}
