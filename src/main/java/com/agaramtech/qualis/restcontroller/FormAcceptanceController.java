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

import com.agaramtech.qualis.biobank.model.BioFormAcceptance;
import com.agaramtech.qualis.biobank.service.formacceptance.FormAcceptanceService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/formacceptance")
public class FormAcceptanceController {
	private static final Logger LOGGER = LoggerFactory.getLogger(BioParentSampleReceivingController.class);

	private RequestContext requestContext;
	private final FormAcceptanceService formAcceptanceService;

	public FormAcceptanceController(RequestContext requestContext, FormAcceptanceService formAcceptanceService) {
		this.requestContext = requestContext;
		this.formAcceptanceService = formAcceptanceService;
	}

	@PostMapping(value = "/getFormAcceptance")
	public ResponseEntity<Object> getFormAcceptance(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioFormAcceptanceCode = -1;
		LOGGER.info("getBioFormAcceptance");
		return formAcceptanceService.getFormAcceptance(inputMap, nbioFormAcceptanceCode, userInfo);
	}

	@PostMapping(value = "/getActiveBioFormAcceptance")
	public ResponseEntity<Object> getActiveBioFormAcceptance(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioFormAcceptanceCode = Integer.valueOf(inputMap.get("nbioformacceptancecode").toString());
		return formAcceptanceService.getActiveBioFormAcceptance(nbioFormAcceptanceCode, userInfo);
	}

	@PostMapping(value = "/getTransferType")
	public ResponseEntity<Object> getTransferType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return formAcceptanceService.getTransferType(userInfo);
	}

	@PostMapping(value = "/getSiteBasedOnTransferType")
	public ResponseEntity<Object> getSiteBasedOnTransferType(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return formAcceptanceService.getSiteBasedOnTransferType(userInfo);
	}

	@PostMapping(value = "/getProjectBasedOnSite")
	public ResponseEntity<Object> getProjectBasedOnSite(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		int nbioBankSiteCode = (int) inputMap.get("nbiobanksitecode");
		return formAcceptanceService.getProjectBasedOnSite(nbioBankSiteCode, userInfo);
	}

//	@PostMapping(value = "/getParentSampleBasedOnProject")
//	public ResponseEntity<Object> getParentSampleBasedOnProject(@RequestBody Map<String, Object> inputMap)
//			throws Exception {
//
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		requestContext.setUserInfo(userInfo);
//		int nbioProjectCode = (int) inputMap.get("nbioprojectcode");
//		return formAcceptanceService.getParentSampleBasedOnProject(nbioProjectCode, userInfo);
//	}

	@PostMapping(value = "/getStorageType")
	public ResponseEntity<Object> getStorageType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return formAcceptanceService.getStorageType(userInfo);
	}

//	@PostMapping(value = "/getSampleTypeBySampleCode")
//	public ResponseEntity<Object> getSampleTypeBySampleCode(@RequestBody Map<String, Object> inputMap)
//			throws Exception {
//
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		requestContext.setUserInfo(userInfo);
//		final int nbioParentSampleCode = (int) inputMap.get("nbioparentsamplecode");
//		final int nstorageTypeCode = (int) inputMap.get("nstoragetypecode");
//		return formAcceptanceService.getSampleTypeBySampleCode(nbioParentSampleCode, nstorageTypeCode, userInfo);
//	}

//	@PostMapping(value = "/getSampleReceivingDetails")
//	public ResponseEntity<Object> getSampleReceivingDetails(@RequestBody Map<String, Object> inputMap)
//			throws Exception {
//
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		requestContext.setUserInfo(userInfo);
//		return formAcceptanceService.getSampleReceivingDetails(inputMap, userInfo);
//	}

	@PostMapping(value = "/moveToDisposeSamples")
	public ResponseEntity<Object> moveToDisposeSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioFormAcceptanceDetailsCode = (String) inputMap.get("nbioformacceptancedetailscode");
		final int nbioFormAcceptanceCode = (int) inputMap.get("nbioformacceptancecode");
		return formAcceptanceService.moveToDisposeSamples(nbioFormAcceptanceCode, nbioFormAcceptanceDetailsCode,
				userInfo);
	}

	@PostMapping(value = "/moveToReturnSamples")
	public ResponseEntity<Object> moveToReturnSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioFormAcceptanceDetailsCode = (String) inputMap.get("nbioformacceptancedetailscode");
		final int nbioFormAcceptanceCode = (int) inputMap.get("nbioformacceptancecode");
		return formAcceptanceService.moveToReturnSamples(nbioFormAcceptanceCode, nbioFormAcceptanceDetailsCode,
				userInfo);
	}

	@PostMapping(value = "/undoReturnDisposeSamples")
	public ResponseEntity<Object> undoReturnDisposeSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioFormAcceptanceDetailsCode = (String) inputMap.get("nbioformacceptancedetailscode");
		final int nbioFormAcceptanceCode = (int) inputMap.get("nbioformacceptancecode");
		return formAcceptanceService.undoReturnDisposeSamples(nbioFormAcceptanceCode, nbioFormAcceptanceDetailsCode,
				userInfo);
	}

	@PostMapping(value = "/storeSamples")
	public ResponseEntity<Object> storeSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return formAcceptanceService.storeSamples(inputMap, userInfo);
	}

	@PostMapping(value = "/getSampleConditionStatus")
	public ResponseEntity<Object> getSampleConditionStatus(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return formAcceptanceService.getSampleConditionStatus(userInfo);
	}

	@PostMapping(value = "/getReason")
	public ResponseEntity<Object> getReason(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return formAcceptanceService.getReason(userInfo);
	}

	@PostMapping(value = "/updateSampleCondition")
	public ResponseEntity<Object> updateSampleCondition(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return formAcceptanceService.updateSampleCondition(inputMap, userInfo);
	}

	@PostMapping(value = "/getStorageCondition")
	public ResponseEntity<Object> getStorageCondition(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return formAcceptanceService.getStorageCondition(userInfo);
	}

	@PostMapping(value = "/getUsersBasedOnSite")
	public ResponseEntity<Object> getUsersBasedOnSite(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return formAcceptanceService.getUsersBasedOnSite(userInfo);
	}

	@PostMapping(value = "/getCourier")
	public ResponseEntity<Object> getCourier(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return formAcceptanceService.getCourier(userInfo);
	}

	@PostMapping(value = "/updateReceiveBioFormAcceptance")
	public ResponseEntity<Object> updateReceiveBioFormAcceptance(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return formAcceptanceService.updateReceiveBioFormAcceptance(inputMap, userInfo);
	}

	@PostMapping(value = "/updateCompleteBioFormAcceptance")
	public ResponseEntity<Object> updateCompleteBioFormAcceptance(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return formAcceptanceService.updateCompleteBioFormAcceptance(inputMap, userInfo);
	}

	@PostMapping(value = "/validateFormAcceptance")
	public ResponseEntity<Object> validateFormAcceptance(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return formAcceptanceService.validateFormAcceptance(inputMap, userInfo);
	}

	@PostMapping(value = "/onCompleteSlideOut")
	public ResponseEntity<Object> onCompleteSlideOut(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return formAcceptanceService.onCompleteSlideOut(inputMap, userInfo);
	}

	@PostMapping(value = "/findStatusFormAcceptance")
	public int findStatusFormAcceptance(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioFormAcceptanceCode = (int) inputMap.get("nbioformacceptancecode");
		return formAcceptanceService.findStatusFormAcceptance(nbioFormAcceptanceCode, userInfo);
	}

	@PostMapping(value = "/getChildInitialGet")
	public List<Map<String, Object>> getChildInitialGet(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioFormAcceptanceCode = (int) inputMap.get("nbioformacceptancecode");
		return formAcceptanceService.getChildInitialGet(nbioFormAcceptanceCode, userInfo);
	}

	@PostMapping(value = "/getReceiveFormAcceptanceDetails")
	public BioFormAcceptance getReceiveFormAcceptanceDetails(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioFormAcceptanceCode = (int) inputMap.get("nbioformacceptancecode");
		return formAcceptanceService.getReceiveFormAcceptanceDetails(nbioFormAcceptanceCode, userInfo);
	}

//	@PostMapping(value = "/awesomeQueryBuilderRecords")
//	public ResponseEntity<Object> awesomeQueryBuilderRecords(@RequestBody Map<String, Object> inputMap)
//			throws Exception {
//
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		requestContext.setUserInfo(userInfo);
//		return formAcceptanceService.awesomeQueryBuilderRecords(userInfo);
//	}

	@PostMapping(value = "/getStorageFreezerData")
	public ResponseEntity<Object> getStorageFreezerData(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return formAcceptanceService.getStorageFreezerData(inputMap, userInfo);
	}

	@PostMapping(value = "/getStorageStructure")
	public ResponseEntity<Object> getStorageStructure(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return formAcceptanceService.getStorageStructure(inputMap, userInfo);
	}
	
	@PostMapping(value = "/moveToReturnDisposeAfterCompleteForm")
	public ResponseEntity<Object> moveToReturnDisposeAfterCompleteForm(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioFormAcceptanceDetailsCode = (String) inputMap.get("nbioformacceptancedetailscode");
		final int nbioFormAcceptanceCode = (int) inputMap.get("nbioformacceptancecode");
		return formAcceptanceService.moveToReturnDisposeAfterCompleteForm(nbioFormAcceptanceCode, nbioFormAcceptanceDetailsCode,
				userInfo);
	}

}