package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.biobank.service.biodirecttransfer.BioDirectTransferService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/biodirecttransfer")
public class BioDirectTransferController {
	private static final Logger LOGGER = LoggerFactory.getLogger(BioParentSampleReceivingController.class);

	private RequestContext requestContext;
	private final BioDirectTransferService bioDirectTransferService;

	public BioDirectTransferController(RequestContext requestContext,
			BioDirectTransferService bioDirectTransferService) {
		this.requestContext = requestContext;
		this.bioDirectTransferService = bioDirectTransferService;
	}

	/**
	 * This method is used to retrieve list of all active transfer form for the specified
	 * site.
	 * 
	 * @param inputMap parameter holds the values that required to fetch the transfer form records
	 * @param nbioDirectTransferCode parameter holds the values of primary key of transfer form
	 * @return response entity object holding response status and list of all active
	 *         transfer forms
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/getBioDirectTransfer")
	public ResponseEntity<Object> getBioDirectTransfer(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioDirectTransferCode = -1;
		LOGGER.info("getBioDirectTransfer");
		return bioDirectTransferService.getBioDirectTransfer(inputMap, nbioDirectTransferCode, userInfo);
	}

	/**
	 * This method is used to retrieve active transfer form object based on the specified
	 * nbioDirectTransferCode.
	 * 
	 * @param nbioDirectTransferCode [int] primary key of transfer form object
	 * @return transfer form object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/getActiveBioDirectTransfer")
	public ResponseEntity<Object> getActiveBioDirectTransfer(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioDirectTransferCode = Integer.valueOf(inputMap.get("nbiodirecttransfercode").toString());
		return bioDirectTransferService.getActiveBioDirectTransfer(nbioDirectTransferCode, userInfo);
	}

	/**
	 * This method is used to retrieve active transfer type object
	 * 
	 * @return transfer type object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/getTransferType")
	public ResponseEntity<Object> getTransferType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.getTransferType(userInfo);
	}

	/**
	 * This method is used to retrieve active site object based on transfer type
	 * 
	 * @return site object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/getSiteBasedOnTransferType")
	public ResponseEntity<Object> getSiteBasedOnTransferType(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.getSiteBasedOnTransferType(userInfo);
	}

	/**
	 * This method is used to retrieve active project object based on site
	 * @param nbioBankSiteCode holds the primary value of the site
	 * @return project object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/getProjectBasedOnSite")
	public ResponseEntity<Object> getProjectBasedOnSite(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		int nbioBankSiteCode = (int) inputMap.get("nbiobanksitecode");
		return bioDirectTransferService.getProjectBasedOnSite(nbioBankSiteCode, userInfo);
	}

	/**
	 * This method is used to retrieve active parent sample object based on project
	 * @param nbioProjectCode holds the primary value of the project
	 * @return parent sample object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/getParentSampleBasedOnProject")
	public ResponseEntity<Object> getParentSampleBasedOnProject(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		int nbioProjectCode = (int) inputMap.get("nbioprojectcode");
		return bioDirectTransferService.getParentSampleBasedOnProject(nbioProjectCode, userInfo);
	}

	/**
	 * This method is used to retrieve active storage type object
	 * 
	 * @return parent storage type object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/getStorageType")
	public ResponseEntity<Object> getStorageType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.getStorageType(userInfo);
	}

	/**
	 * This method is used to retrieve active sample type object
	 * @param inputMap holds the primary value of sample code in the key of sparentsamplecode
	 * @return parent sample type object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/getSampleTypeBySampleCode")
	public ResponseEntity<Object> getSampleTypeBySampleCode(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.getSampleTypeBySampleCode(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve active sample receiving details object
	 * @param inputMap holds the keys that required to get sample receiving details
	 * @return sample receiving details object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/getSampleReceivingDetails")
	public ResponseEntity<Object> getSampleReceivingDetails(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.getSampleReceivingDetails(inputMap, userInfo);
	}

	/**
	 * This method is used to add a new entry to biodirecttransfer table. Need to check for
	 * duplicate entry of samples for the specified transfer form and specified site before saving into
	 * database.
	 * 
	 * @param inputMap holding the data required to create a transfer form and also the sample in the form
	 * @return response entity object holding response status and data of added
	 *         transfer form object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/createBioDirectTransfer")
	public ResponseEntity<Object> createBioDirectTransfer(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.createBioDirectTransfer(inputMap, userInfo);
	}

	/**
	 * This method is used to get the active transfer form object based on nbioDirectTransferCode
	 * @param nbioDirectTransferCode holding the primary value of the transfer form
	 * @return response entity object holding response list of transfer form
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/getActiveBioDirectTransferById")
	public ResponseEntity<Object> getActiveBioDirectTransferById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		int nbioDirectTransferCode = (int) inputMap.get("nbiodirecttransfercode");
		return bioDirectTransferService.getActiveBioDirectTransferById(nbioDirectTransferCode, userInfo);
	}

	/**
	 * This method is used to update the transfer form object
	 * @param inputMap holding the data that are changed for the transfer form
	 * @return response entity object holding response list of transfer form
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/updateBioDirectTransfer")
	public ResponseEntity<Object> updateBioDirectTransfer(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.updateBioDirectTransfer(inputMap, userInfo);
	}

	/**
	 * This method is used to delete the samples
	 * @param nbioDirectTransferCode holding the primary value of transfer form
	 * @param nbioDirectTransferDetailsCode holding the primary value of samples
	 * @return response entity object holding response status and data of deleted samples object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/deleteChildDirectTransfer")
	public ResponseEntity<Object> deleteChildDirectTransfer(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioDirectTransferDetailsCode = (String) inputMap.get("nbiodirecttransferdetailscode");
		final int nbioDirectTransferCode = (int) inputMap.get("nbiodirecttransfercode");
		return bioDirectTransferService.deleteChildDirectTransfer(nbioDirectTransferCode, nbioDirectTransferDetailsCode,
				userInfo);
	}

	/**
	 * This method is used to dispose the samples
	 * @param nbioDirectTransferCode holding the primary value of transfer form
	 * @param nbioDirectTransferDetailsCode holding the primary value of samples
	 * @return response entity object holding response status and data of disposed samples object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/disposeSamples")
	public ResponseEntity<Object> disposeSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioDirectTransferDetailsCode = (String) inputMap.get("nbiodirecttransferdetailscode");
		final int nbioDirectTransferCode = (int) inputMap.get("nbiodirecttransfercode");
		return bioDirectTransferService.disposeSamples(nbioDirectTransferCode, nbioDirectTransferDetailsCode, userInfo);
	}

	/**
	 * This method is used to undo the disposes samples
	 * @param nbioDirectTransferCode holding the primary value of transfer form
	 * @param nbioDirectTransferDetailsCode holding the primary value of samples
	 * @return response entity object holding response status and data of undo disposed samples object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/undoDisposeSamples")
	public ResponseEntity<Object> undoDisposeSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioDirectTransferDetailsCode = (String) inputMap.get("nbiodirecttransferdetailscode");
		final int nbioDirectTransferCode = (int) inputMap.get("nbiodirecttransfercode");
		return bioDirectTransferService.undoDisposeSamples(nbioDirectTransferCode, nbioDirectTransferDetailsCode,
				userInfo);
	}
	
	/**
	 * This method is used to add the samples to the transfer form
	 * @param inputMap holding the data required to add samples to the particular transfer form
	 * @return response entity object holding response status and data of transfer form object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/createChildBioDirectTransfer")
	public ResponseEntity<Object> createChildBioDirectTransfer(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.createChildBioDirectTransfer(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve active sample condition object
	 * @return sample condition object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/getSampleConditionStatus")
	public ResponseEntity<Object> getSampleConditionStatus(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.getSampleConditionStatus(userInfo);
	}

	/**
	 * This method is used to retrieve active reason object
	 * @return reason object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/getReason")
	public ResponseEntity<Object> getReason(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.getReason(userInfo);
	}

	/**
	 * This method is used to update the sample condition object
	 * @param inputMap holding the data that are changed for the sample condition
	 * @return response entity object holding response list of samples
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/updateSampleCondition")
	public ResponseEntity<Object> updateSampleCondition(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.updateSampleCondition(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve active storage condition object
	 * @return storage condition object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/getStorageCondition")
	public ResponseEntity<Object> getStorageCondition(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.getStorageCondition(userInfo);
	}

	/**
	 * This method is used to retrieve active users object based on site
	 * @return users object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/getUsersBasedOnSite")
	public ResponseEntity<Object> getUsersBasedOnSite(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.getUsersBasedOnSite(userInfo);
	}

	/**
	 * This method is used to retrieve active courier object
	 * @return courier object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/getCourier")
	public ResponseEntity<Object> getCourier(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.getCourier(userInfo);
	}

	/**
	 * This method is used to validate the transfer form
	 * @param inputMap holding the data that are validated for the transfer form
	 * @return response entity object holding response list of transfer forms
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/createValidationBioDirectTransfer")
	public ResponseEntity<Object> createValidationBioDirectTransfer(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.createValidationBioDirectTransfer(inputMap, userInfo);
	}

	/**
	 * This method is used to transfer the transfer form
	 * @param inputMap holding the data that are transfered for the transfer form
	 * @return response entity object holding response list of transfer forms
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/transferDirectTransfer")
	public ResponseEntity<Object> transferDirectTransfer(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.transferDirectTransfer(inputMap, userInfo);
	}

	/**
	 * This method is used to find the status of transfer form
	 * @param nbioDirectTransferCode holding primary value of transfer form
	 * @return response entity object holding response of primary key
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/findStatusDirectTransfer")
	public int findStatusDirectTransfer(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioDirectTransferCode = (int) inputMap.get("nbiodirecttransfercode");
		return bioDirectTransferService.findStatusDirectTransfer(nbioDirectTransferCode, userInfo);
	}

	/**
	 * This method is used to get the samples based on transfer form
	 * @param nbioDirectTransferCode holding the primary value of the transfer form
	 * @return response entity object holding response list of samples based on nbioDirectTransferCode
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/getChildInitialGet")
	public List<Map<String, Object>> getChildInitialGet(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioDirectTransferCode = (int) inputMap.get("nbiodirecttransfercode");
		return bioDirectTransferService.getChildInitialGet(nbioDirectTransferCode, userInfo);
	}

	/**
	 * This method is used to cancel the transfer form
	 * @param nbioDirectTransferCode holding primary value of transfer form
	 * @return response entity object holding response of list of transfer form
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/cancelDirectTransfer")
	public ResponseEntity<Object> cancelDirectTransfer(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.cancelDirectTransfer(inputMap, userInfo);
	}

	/**
	 * This method is used to accept or reject the samples
	 * @param inputMap holding data to accept or reject the samples
	 * @return response entity object holding response of list of samples
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/acceptRejectDirectTransferSlide")
	public ResponseEntity<Object> acceptRejectDirectTransferSlide(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDirectTransferService.acceptRejectDirectTransferSlide(inputMap, userInfo);
	}
	
	/**
	 * This method is used to check the sample that are accessible or not
	 * @param nbioDirectTransferCode holding primary value of the transfer form
	 * @return response entity object holding response of list of sample details
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@PostMapping(value = "/checkAccessibleSamples")
	public ResponseEntity<Object> checkAccessibleSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioDirectTransferCode = (int) inputMap.get("nbiodirecttransfercode");
		return bioDirectTransferService.checkAccessibleSamples(nbioDirectTransferCode, userInfo);
	}
	
}