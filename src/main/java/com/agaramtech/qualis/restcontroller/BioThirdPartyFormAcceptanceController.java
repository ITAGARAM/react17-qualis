package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.biobank.service.biothirdpartyformacceptance.BioThirdPartyFormAcceptanceService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/biothirdpartyformacceptance")
public class BioThirdPartyFormAcceptanceController {
	private static final Logger LOGGER = LoggerFactory.getLogger(BioThirdPartyFormAcceptanceController.class);

	private RequestContext requestContext;
	private final BioThirdPartyFormAcceptanceService bioThirdPartyFormAcceptanceService;

	public BioThirdPartyFormAcceptanceController(RequestContext requestContext,
			BioThirdPartyFormAcceptanceService bioThirdPartyFormAcceptanceService) {
		this.requestContext = requestContext;
		this.bioThirdPartyFormAcceptanceService = bioThirdPartyFormAcceptanceService;
	}

	@PostMapping(value = "/getBioThirdPartyFormAcceptance")
	public ResponseEntity<Object> getBioThirdPartyFormAcceptance(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioThirdPartyFormAcceptanceCode = -1;
		LOGGER.info("getBioThirdPartyFormAcceptance");
		return bioThirdPartyFormAcceptanceService.getBioThirdPartyFormAcceptance(inputMap,
				nbioThirdPartyFormAcceptanceCode, userInfo);
	}

	@PostMapping(value = "/getActiveBioThirdPartyFormAcceptance")
	public ResponseEntity<Object> getActiveBioThirdPartyFormAcceptance(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioThirdPartyFormAcceptanceCode = Integer
				.valueOf(inputMap.get("nbiothirdpartyformacceptancecode").toString());
		return bioThirdPartyFormAcceptanceService.getActiveBioThirdPartyFormAcceptance(nbioThirdPartyFormAcceptanceCode,
				userInfo);
	}
//
//	@PostMapping(value = "/getTransferType")
//	public ResponseEntity<Object> getTransferType(@RequestBody Map<String, Object> inputMap) throws Exception {
//
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		requestContext.setUserInfo(userInfo);
//		return bioThirdPartyFormAcceptanceService.getTransferType(userInfo);
//	}
//
//	@PostMapping(value = "/getSiteBasedOnTransferType")
//	public ResponseEntity<Object> getSiteBasedOnTransferType(@RequestBody Map<String, Object> inputMap)
//			throws Exception {
//
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		requestContext.setUserInfo(userInfo);
//		return bioThirdPartyFormAcceptanceService.getSiteBasedOnTransferType(userInfo);
//	}
//
//	@PostMapping(value = "/getProjectBasedOnSite")
//	public ResponseEntity<Object> getProjectBasedOnSite(@RequestBody Map<String, Object> inputMap) throws Exception {
//
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		requestContext.setUserInfo(userInfo);
//		int nbioBankSiteCode = (int) inputMap.get("nbiobanksitecode");
//		return bioThirdPartyFormAcceptanceService.getProjectBasedOnSite(nbioBankSiteCode, userInfo);
//	}
//
////	@PostMapping(value = "/getParentSampleBasedOnProject")
////	public ResponseEntity<Object> getParentSampleBasedOnProject(@RequestBody Map<String, Object> inputMap)
////			throws Exception {
////
////		final ObjectMapper objMapper = new ObjectMapper();
////		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
////		requestContext.setUserInfo(userInfo);
////		int nbioProjectCode = (int) inputMap.get("nbioprojectcode");
////		return bioThirdPartyFormAcceptanceService.getParentSampleBasedOnProject(nbioProjectCode, userInfo);
////	}
//
//	@PostMapping(value = "/getStorageType")
//	public ResponseEntity<Object> getStorageType(@RequestBody Map<String, Object> inputMap) throws Exception {
//
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		requestContext.setUserInfo(userInfo);
//		return bioThirdPartyFormAcceptanceService.getStorageType(userInfo);
//	}
//
////	@PostMapping(value = "/getSampleTypeBySampleCode")
////	public ResponseEntity<Object> getSampleTypeBySampleCode(@RequestBody Map<String, Object> inputMap)
////			throws Exception {
////
////		final ObjectMapper objMapper = new ObjectMapper();
////		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
////		requestContext.setUserInfo(userInfo);
////		final int nbioParentSampleCode = (int) inputMap.get("nbioparentsamplecode");
////		final int nstorageTypeCode = (int) inputMap.get("nstoragetypecode");
////		return bioThirdPartyFormAcceptanceService.getSampleTypeBySampleCode(nbioParentSampleCode, nstorageTypeCode, userInfo);
////	}
//
////	@PostMapping(value = "/getSampleReceivingDetails")
////	public ResponseEntity<Object> getSampleReceivingDetails(@RequestBody Map<String, Object> inputMap)
////			throws Exception {
////
////		final ObjectMapper objMapper = new ObjectMapper();
////		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
////		requestContext.setUserInfo(userInfo);
////		return bioThirdPartyFormAcceptanceService.getSampleReceivingDetails(inputMap, userInfo);
////	}

	@PostMapping(value = "/moveToDisposeSamples")
	public ResponseEntity<Object> moveToDisposeSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioThirdPartyFormAcceptanceDetailsCode = (String) inputMap
				.get("nbiothirdpartyformacceptancedetailscode");
		final int nbioThirdPartyFormAcceptanceCode = (int) inputMap.get("nbiothirdpartyformacceptancecode");
		return bioThirdPartyFormAcceptanceService.moveToDisposeSamples(nbioThirdPartyFormAcceptanceCode,
				nbioThirdPartyFormAcceptanceDetailsCode, userInfo);
	}

	@PostMapping(value = "/moveToReturnSamples")
	public ResponseEntity<Object> moveToReturnSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioThirdPartyFormAcceptanceDetailsCode = (String) inputMap
				.get("nbiothirdpartyformacceptancedetailscode");
		final int nbioThirdPartyFormAcceptanceCode = (int) inputMap.get("nbiothirdpartyformacceptancecode");
		return bioThirdPartyFormAcceptanceService.moveToReturnSamples(nbioThirdPartyFormAcceptanceCode,
				nbioThirdPartyFormAcceptanceDetailsCode, userInfo);
	}

	@PostMapping(value = "/undoReturnDisposeSamples")
	public ResponseEntity<Object> undoReturnDisposeSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioThirdPartyFormAcceptanceDetailsCode = (String) inputMap
				.get("nbiothirdpartyformacceptancedetailscode");
		final int nbioThirdPartyFormAcceptanceCode = (int) inputMap.get("nbiothirdpartyformacceptancecode");
		return bioThirdPartyFormAcceptanceService.undoReturnDisposeSamples(nbioThirdPartyFormAcceptanceCode,
				nbioThirdPartyFormAcceptanceDetailsCode, userInfo);
	}

//	@PostMapping(value = "/storeSamples")
//	public ResponseEntity<Object> storeSamples(@RequestBody Map<String, Object> inputMap) throws Exception {
//
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		requestContext.setUserInfo(userInfo);
//		return bioThirdPartyFormAcceptanceService.storeSamples(inputMap, userInfo);
//	}

	@PostMapping(value = "/getSampleConditionStatus")
	public ResponseEntity<Object> getSampleConditionStatus(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyFormAcceptanceService.getSampleConditionStatus(userInfo);
	}

	@PostMapping(value = "/getReason")
	public ResponseEntity<Object> getReason(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyFormAcceptanceService.getReason(userInfo);
	}

	@PostMapping(value = "/updateSampleCondition")
	public ResponseEntity<Object> updateSampleCondition(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyFormAcceptanceService.updateSampleCondition(inputMap, userInfo);
	}

	@PostMapping(value = "/getStorageCondition")
	public ResponseEntity<Object> getStorageCondition(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyFormAcceptanceService.getStorageCondition(userInfo);
	}

	@PostMapping(value = "/getUsersBasedOnSite")
	public ResponseEntity<Object> getUsersBasedOnSite(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyFormAcceptanceService.getUsersBasedOnSite(userInfo);
	}

//	@PostMapping(value = "/getCourier")
//	public ResponseEntity<Object> getCourier(@RequestBody Map<String, Object> inputMap) throws Exception {
//
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		requestContext.setUserInfo(userInfo);
//		return bioThirdPartyFormAcceptanceService.getCourier(userInfo);
//	}
//
	@PostMapping(value = "/updateReceiveBioThirdPartyFormAcceptance")
	public ResponseEntity<Object> updateReceiveBioThirdPartyFormAcceptance(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyFormAcceptanceService.updateReceiveBioThirdPartyFormAcceptance(inputMap, userInfo);
	}

	@PostMapping(value = "/updateCompleteBioThirdPartyFormAcceptance")
	public ResponseEntity<Object> updateCompleteBioThirdPartyFormAcceptance(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyFormAcceptanceService.updateCompleteBioThirdPartyFormAcceptance(inputMap, userInfo);
	}

	@PostMapping(value = "/validateBioThirdPartyFormAcceptance")
	public ResponseEntity<Object> validateBioThirdPartyFormAcceptance(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyFormAcceptanceService.validateBioThirdPartyFormAcceptance(inputMap, userInfo);
	}

	@PostMapping(value = "/onCompleteSlideOut")
	public ResponseEntity<Object> onCompleteSlideOut(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyFormAcceptanceService.onCompleteSlideOut(inputMap, userInfo);
	}

	@PostMapping(value = "/findStatusBioThirdPartyFormAcceptance")
	public int findStatusBioThirdPartyFormAcceptance(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioThirdPartyFormAcceptanceCode = (int) inputMap.get("nbiothirdpartyformacceptancecode");
		return bioThirdPartyFormAcceptanceService
				.findStatusBioThirdPartyFormAcceptance(nbioThirdPartyFormAcceptanceCode, userInfo);
	}

	@PostMapping(value = "/acceptRejectBioThirdPartyFormAcceptanceSlide")
	public ResponseEntity<Object> acceptRejectBioThirdPartyFormAcceptanceSlide(
			@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyFormAcceptanceService.acceptRejectBioThirdPartyFormAcceptanceSlide(inputMap, userInfo);
	}

//
//	@PostMapping(value = "/getChildInitialGet")
//	public List<Map<String, Object>> getChildInitialGet(@RequestBody Map<String, Object> inputMap) throws Exception {
//
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		requestContext.setUserInfo(userInfo);
//		final int nbioThirdPartyFormAcceptanceCode = (int) inputMap.get("nbiothirdpartyformacceptancecode");
//		return bioThirdPartyFormAcceptanceService.getChildInitialGet(nbioThirdPartyFormAcceptanceCode, userInfo);
//	}
//
	@PostMapping(value = "/getReceiveBioThirdPartyFormAcceptanceDetails")
	public ResponseEntity<Object> getReceiveBioThirdPartyFormAcceptanceDetails(
			@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioThirdPartyFormAcceptanceCode = (int) inputMap.get("nbiothirdpartyformacceptancecode");
		return bioThirdPartyFormAcceptanceService
				.getReceiveBioThirdPartyFormAcceptanceDetails(nbioThirdPartyFormAcceptanceCode, userInfo);
	}
//
////	@PostMapping(value = "/awesomeQueryBuilderRecords")
////	public ResponseEntity<Object> awesomeQueryBuilderRecords(@RequestBody Map<String, Object> inputMap)
////			throws Exception {
////
////		final ObjectMapper objMapper = new ObjectMapper();
////		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
////		requestContext.setUserInfo(userInfo);
////		return bioThirdPartyFormAcceptanceService.awesomeQueryBuilderRecords(userInfo);
////	}
//
//	@PostMapping(value = "/getStorageFreezerData")
//	public ResponseEntity<Object> getStorageFreezerData(@RequestBody Map<String, Object> inputMap) throws Exception {
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
//		});
//		requestContext.setUserInfo(userInfo);
//		return bioThirdPartyFormAcceptanceService.getStorageFreezerData(inputMap, userInfo);
//	}
//
//	@PostMapping(value = "/getStorageStructure")
//	public ResponseEntity<Object> getStorageStructure(@RequestBody Map<String, Object> inputMap) throws Exception {
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
//		});
//		requestContext.setUserInfo(userInfo);
//		return bioThirdPartyFormAcceptanceService.getStorageStructure(inputMap, userInfo);
//	}
	
	@PostMapping(value = "/moveToReturnSamplesAfterComplete")
	public ResponseEntity<Object> moveToReturnSamplesAfterComplete(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioThirdPartyFormAcceptanceDetailsCode = (String) inputMap
				.get("nbiothirdpartyformacceptancedetailscode");
		final int nbioThirdPartyFormAcceptanceCode = (int) inputMap.get("nbiothirdpartyformacceptancecode");
		return bioThirdPartyFormAcceptanceService.moveToReturnSamplesAfterComplete(nbioThirdPartyFormAcceptanceCode,
				nbioThirdPartyFormAcceptanceDetailsCode, userInfo);
	}

}