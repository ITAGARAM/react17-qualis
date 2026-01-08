package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.configuration.model.AWSStorageConfig;
import com.agaramtech.qualis.configuration.service.awsstorageconfig.AWSStorageConfigurationService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the AWSStorageConfiguration Service methods.
 */
@RestController
@RequestMapping("/awsstorageconfig")
public class AWSStorageConfigurationController {
	private  AWSStorageConfigurationService objAWSStorageConfigurationService;
	private RequestContext requestContext;
	

	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param objAWSStorageConfigurationService AWSStorageConfigurationService
	 */
	public  AWSStorageConfigurationController(RequestContext requestContext,  AWSStorageConfigurationService objAWSStorageConfigurationService) {
		super();
		this.requestContext = requestContext;
		this.objAWSStorageConfigurationService = objAWSStorageConfigurationService;
	}
	
	/**
	 * This is method is get the all AWSCredentials with respect to default status records in the table
	 * @param nmasterSiteCode argument passed to get AWSCredentials with respect to site
	 * @return AWSStorageConfig object
	 * @throws Exception
	 */

	@PostMapping("getAWSStorageConfig")
	public ResponseEntity<Object> getAWSStorageConfig(@RequestBody Map<String, Object> inputMap) throws Exception{
		final ObjectMapper mapper = new ObjectMapper();
		final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);

		return objAWSStorageConfigurationService.getAWSStorageConfig(userInfo);
		
	}
	
	/**
	 * This method is used to add new record in the AWSStorageconfig table
	 * Accesskeyid is unique accross the table
	 * Need to check for duplicate entry of Accesskeyid for the specified site before saving into database.
	 * @param awsStorageConfig [AWSStorageConfig] object holding details to be added in AWSStorageConfig table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return saved AWSStorageConfig object with status code 200 if saved successfully else if the unit already exists, 
	 * 			response will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/createAWSStorageConfig")
	public ResponseEntity<Object> createAWSStorageConfig(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		AWSStorageConfig awsStorageConfig = mapper.convertValue(inputMap.get("awsstorageconfig"), new TypeReference<AWSStorageConfig>() {
		});
		requestContext.setUserInfo(userInfo);

		return objAWSStorageConfigurationService.createAWSStorageConfig(awsStorageConfig, userInfo);

	}
	
	/**
	 * This method is used to update entry in AWSStorageConfig table.
	 * Need to validate that the AWSStorageConfig object to be updated is active before updating 
	 * details in database.
	 *  Need to check for duplicate entry of Accesskeyid  for the specified site before saving into database.
	 *  Need to check that there should be only one default AWSCredentials  for a site
	 *  @param awsStorageConfig [AWSStorageConfig] object holding details to be updated in AWSStorageConfig table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return saved AWSStorageConfig object with status code 200 if saved successfully 
	 * 			else if the AWSStorageConfig already exists, response will be returned as 'Already Exists' with status code 409
	 *          else if the AWSStorageConfig to be updated is not available, response will be returned as 'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/updateAWSStorageConfig")
	public ResponseEntity<Object> updateAWSStorageConfig(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		AWSStorageConfig awsStorageConfig = mapper.convertValue(inputMap.get("awsstorageconfig"), new TypeReference<AWSStorageConfig>() {
		});
		requestContext.setUserInfo(userInfo);

		return objAWSStorageConfigurationService.updateAWSStorageConfig(awsStorageConfig, userInfo);

	}
	
	
	/**
	 * This method id used to delete an entry in AWSStorageConfig table
	 * Need to check the record is already deleted or not
	 * @param awsStorageConfig [AWSStorageConfig] an Object holds the record to be deleted
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with list of available AWSStorageConfig objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/deleteAWSStorageConfig")
	public ResponseEntity<Object> deleteAWSStorageConfig(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		AWSStorageConfig awsStorageConfig = mapper.convertValue(inputMap.get("awsstorageconfig"), new TypeReference<AWSStorageConfig>() {
		});
		requestContext.setUserInfo(userInfo);

		return objAWSStorageConfigurationService.deleteAWSStorageConfig(awsStorageConfig, userInfo);

	}
	
	/**
     * This method is used to retrieve active AWSCredentials object based on the specified nawsstorageconfigcode
     * @param nawsstorageconfigcode [int] primary key of AWSStorageConfig object
     * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of AWSStorageConfig  object
	 * @throws Exception that are thrown from this DAO layer
     */
	@PostMapping(value = "/getActiveAWSStorageConfigById")
	public ResponseEntity<Object> getActiveAWSStorageConfigById(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		final UserInfo userInfo = mapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		int nawsstorageconfigcode = (int) inputMap.get("nawsstorageconfigcode");

		return objAWSStorageConfigurationService.getActiveAWSStorageConfigById(nawsstorageconfigcode, userInfo);
	}

	
}
