package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.biobank.service.biobankreturn.BioBankReturnService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/biobankreturn")
public class BioBankReturnController {
	private static final Logger LOGGER = LoggerFactory.getLogger(BioBankReturnController.class);

	private RequestContext requestContext;
	private final BioBankReturnService bioBankReturnService;

	public BioBankReturnController(RequestContext requestContext, BioBankReturnService bioBankReturnService) {
		this.requestContext = requestContext;
		this.bioBankReturnService = bioBankReturnService;
	}

	@PostMapping(value = "/getBioBankReturn")
	public ResponseEntity<Object> getBioBankReturn(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioBankReturnCode = -1;
		LOGGER.info("getBioBankReturn");
		return bioBankReturnService.getBioBankReturn(inputMap, nbioBankReturnCode, userInfo);
	}

	@PostMapping(value = "/getFormNumberDetails")
	public ResponseEntity<Object> getFormNumberDetails(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		int noriginSiteCode = inputMap.containsKey("noriginSiteCode") ? (int) inputMap.get("noriginSiteCode") : -1;
		return bioBankReturnService.getFormNumberDetails(noriginSiteCode, userInfo);
	}

	@PostMapping(value = "/getFormAcceptanceDetails")
	public ResponseEntity<Object> getFormAcceptanceDetails(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioFormAccetanceCode = Integer.valueOf(inputMap.get("nbioformacceptancecode").toString());
		return bioBankReturnService.getFormAcceptanceDetails(nbioFormAccetanceCode, userInfo);
	}

	@PostMapping(value = "/createBioBankReturn")
	public ResponseEntity<Object> createBioBankReturn(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioBankReturnService.createBioBankReturn(inputMap, userInfo);
	}

	@PostMapping(value = "/getActiveBioBankReturn")
	public ResponseEntity<Object> getActiveBioBankReturn(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioBankReturnCode = Integer.valueOf(inputMap.get("nbiobankreturncode").toString());
		final int saveType = inputMap.containsKey("saveType") ? Integer.valueOf(inputMap.get("saveType").toString())
				: 1;
		return bioBankReturnService.getActiveBioBankReturn(nbioBankReturnCode, saveType, userInfo);
	}

	@PostMapping(value = "/getActiveBioBankReturnById")
	public ResponseEntity<Object> getActiveBioBankReturnById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		int nbioBankReturnCode = (int) inputMap.get("nbiobankreturncode");
		return bioBankReturnService.getActiveBioBankReturnById(nbioBankReturnCode, userInfo);
	}

	@PostMapping(value = "/updateBioBankReturn")
	public ResponseEntity<Object> updateBioBankReturn(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioBankReturnService.updateBioBankReturn(inputMap, userInfo);
	}

	@PostMapping(value = "/findStatusBankReturn")
	public int findStatusBankReturn(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioBankReturnCode = (int) inputMap.get("nbiobankreturncode");
		return bioBankReturnService.findStatusBankReturn(nbioBankReturnCode, userInfo);
	}

	@PostMapping(value = "/getValidateFormDetails")
	public ResponseEntity<Object> getValidateFormDetails(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioBankReturnCode = Integer.valueOf(inputMap.get("nbiobankreturncode").toString());
		return bioBankReturnService.getValidateFormDetails(nbioBankReturnCode, userInfo);
	}

	@PostMapping(value = "/getStorageCondition")
	public ResponseEntity<Object> getStorageCondition(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioBankReturnService.getStorageCondition(userInfo);
	}

	@PostMapping(value = "/getUsersBasedOnSite")
	public ResponseEntity<Object> getUsersBasedOnSite(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioBankReturnService.getUsersBasedOnSite(userInfo);
	}

	@PostMapping(value = "/getCourier")
	public ResponseEntity<Object> getCourier(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioBankReturnService.getCourier(userInfo);
	}

	@PostMapping(value = "/createValidationBioBankReturn")
	public ResponseEntity<Object> createValidationBioBankReturn(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioBankReturnService.createValidationBioBankReturn(inputMap, userInfo);
	}

	@PostMapping(value = "/returnBankReturn")
	public ResponseEntity<Object> returnBankReturn(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioBankReturnService.returnBankReturn(inputMap, userInfo);
	}

	@PostMapping(value = "/cancelBankReturn")
	public ResponseEntity<Object> cancelBankReturn(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioBankReturnService.cancelBankReturn(inputMap, userInfo);
	}

	@PostMapping(value = "/addBankReturnDetails")
	public ResponseEntity<Object> addBankReturnDetails(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioBankReturnCode = Integer.valueOf(inputMap.get("nbiobankreturncode").toString());
		final int noriginSiteCode = Integer.valueOf(inputMap.get("noriginsitecode").toString());
		return bioBankReturnService.addBankReturnDetails(nbioBankReturnCode, noriginSiteCode, userInfo);
	}

	@PostMapping(value = "/createChildBioBankReturn")
	public ResponseEntity<Object> createChildBioBankReturn(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioBankReturnService.createChildBioBankReturn(inputMap, userInfo);
	}

	@PostMapping(value = "/acceptRejectBankReturnSlide")
	public ResponseEntity<Object> acceptRejectBankReturnSlide(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioBankReturnService.acceptRejectBankReturnSlide(inputMap, userInfo);
	}

	@PostMapping(value = "/getSampleConditionStatus")
	public ResponseEntity<Object> getSampleConditionStatus(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioBankReturnService.getSampleConditionStatus(userInfo);
	}

	@PostMapping(value = "/getReason")
	public ResponseEntity<Object> getReason(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioBankReturnService.getReason(userInfo);
	}

	@PostMapping(value = "/updateSampleCondition")
	public ResponseEntity<Object> updateSampleCondition(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioBankReturnService.updateSampleCondition(inputMap, userInfo);
	}

	@PostMapping(value = "/deleteChildBankReturn")
	public ResponseEntity<Object> deleteChildBankReturn(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioBankReturnDetailsCode = (String) inputMap.get("nbiobankreturndetailscode");
		final int nbioBankReturnCode = (int) inputMap.get("nbiobankreturncode");
		return bioBankReturnService.deleteChildBankReturn(nbioBankReturnCode, nbioBankReturnDetailsCode, userInfo);
	}

	@PostMapping(value = "/disposeSamples")
	public ResponseEntity<Object> disposeSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioBankReturnDetailsCode = (String) inputMap.get("nbiobankreturndetailscode");
		final int nbioBankReturnCode = (int) inputMap.get("nbiobankreturncode");
		return bioBankReturnService.disposeSamples(nbioBankReturnCode, nbioBankReturnDetailsCode, userInfo);
	}

	@PostMapping(value = "/getStorageFreezerData")
	public ResponseEntity<Object> getStorageFreezerData(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return bioBankReturnService.getStorageFreezerData(inputMap, userInfo);
	}

	@PostMapping(value = "/getStorageStructure")
	public ResponseEntity<Object> getStorageStructure(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return bioBankReturnService.getStorageStructure(inputMap, userInfo);
	}

	@PostMapping(value = "/storeSamples")
	public ResponseEntity<Object> storeSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioBankReturnService.storeSamples(inputMap, userInfo);
	}

	@PostMapping(value = "/undoDisposeSamples")
	public ResponseEntity<Object> undoDisposeSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioBankReturnDetailsCode = (String) inputMap.get("nbiobankreturndetailscode");
		final int nbioBankReturnCode = (int) inputMap.get("nbiobankreturncode");
		return bioBankReturnService.undoDisposeSamples(nbioBankReturnCode, nbioBankReturnDetailsCode, userInfo);
	}

}