package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.biobank.service.biothirdpartyreturn.BioThirdPartyReturnService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/biothirdpartyreturn")
public class BioThirdPartyReturnController {
	private static final Logger LOGGER = LoggerFactory.getLogger(BioThirdPartyReturnController.class);

	private RequestContext requestContext;
	private final BioThirdPartyReturnService bioThirdPartyReturnService;

	public BioThirdPartyReturnController(RequestContext requestContext,
			BioThirdPartyReturnService bioThirdPartyReturnService) {
		this.requestContext = requestContext;
		this.bioThirdPartyReturnService = bioThirdPartyReturnService;
	}

	@PostMapping(value = "/getBioThirdPartyReturn")
	public ResponseEntity<Object> getBioThirdPartyReturn(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioThirdPartyReturnCode = -1;
		LOGGER.info("getBioThirdPartyReturn");
		return bioThirdPartyReturnService.getBioThirdPartyReturn(inputMap, nbioThirdPartyReturnCode, userInfo);
	}

	@PostMapping(value = "/getFormNumberDetails")
	public ResponseEntity<Object> getFormNumberDetails(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		int noriginSiteCode = inputMap.containsKey("noriginSiteCode") ? (int) inputMap.get("noriginSiteCode") : -1;
		return bioThirdPartyReturnService.getFormNumberDetails(noriginSiteCode, userInfo);
	}

	@PostMapping(value = "/getThirdPartyFormAcceptanceDetails")
	public ResponseEntity<Object> getThirdPartyFormAcceptanceDetails(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioThirdPartyFormAccetanceCode = Integer
				.valueOf(inputMap.get("nbiothirdpartyformacceptancecode").toString());
		return bioThirdPartyReturnService.getThirdPartyFormAcceptanceDetails(nbioThirdPartyFormAccetanceCode, userInfo);
	}

	@PostMapping(value = "/createBioThirdPartyReturn")
	public ResponseEntity<Object> createBioThirdPartyReturn(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyReturnService.createBioThirdPartyReturn(inputMap, userInfo);
	}

	@PostMapping(value = "/getActiveBioThirdPartyReturn")
	public ResponseEntity<Object> getActiveBioThirdPartyReturn(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioThirdPartyReturnCode = Integer.valueOf(inputMap.get("nbiothirdpartyreturncode").toString());
		final int saveType = inputMap.containsKey("saveType") ? Integer.valueOf(inputMap.get("saveType").toString())
				: 1;
		return bioThirdPartyReturnService.getActiveBioThirdPartyReturn(nbioThirdPartyReturnCode, saveType, userInfo);
	}

	@PostMapping(value = "/getActiveBioThirdPartyReturnById")
	public ResponseEntity<Object> getActiveBioThirdPartyReturnById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		int nbioThirdPartyReturnCode = (int) inputMap.get("nbiothirdpartyreturncode");
		return bioThirdPartyReturnService.getActiveBioThirdPartyReturnById(nbioThirdPartyReturnCode, userInfo);
	}

	@PostMapping(value = "/updateBioThirdPartyReturn")
	public ResponseEntity<Object> updateBioThirdPartyReturn(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyReturnService.updateBioThirdPartyReturn(inputMap, userInfo);
	}

	@PostMapping(value = "/findStatusThirdPartyReturn")
	public int findStatusThirdPartyReturn(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioThirdPartyReturnCode = (int) inputMap.get("nbiothirdpartyreturncode");
		return bioThirdPartyReturnService.findStatusThirdPartyReturn(nbioThirdPartyReturnCode, userInfo);
	}

	@PostMapping(value = "/getValidateFormDetails")
	public ResponseEntity<Object> getValidateFormDetails(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioThirdPartyReturnCode = Integer.valueOf(inputMap.get("nbiothirdpartyreturncode").toString());
		return bioThirdPartyReturnService.getValidateFormDetails(nbioThirdPartyReturnCode, userInfo);
	}

	@PostMapping(value = "/getStorageCondition")
	public ResponseEntity<Object> getStorageCondition(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyReturnService.getStorageCondition(userInfo);
	}

	@PostMapping(value = "/getUsersBasedOnSite")
	public ResponseEntity<Object> getUsersBasedOnSite(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyReturnService.getUsersBasedOnSite(userInfo);
	}

	@PostMapping(value = "/getCourier")
	public ResponseEntity<Object> getCourier(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyReturnService.getCourier(userInfo);
	}

	@PostMapping(value = "/createValidationBioThirdPartyReturn")
	public ResponseEntity<Object> createValidationBioThirdPartyReturn(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyReturnService.createValidationBioThirdPartyReturn(inputMap, userInfo);
	}

	@PostMapping(value = "/returnThirdPartyReturn")
	public ResponseEntity<Object> returnThirdPartyReturn(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyReturnService.returnThirdPartyReturn(inputMap, userInfo);
	}

	@PostMapping(value = "/cancelThirdPartyReturn")
	public ResponseEntity<Object> cancelThirdPartyReturn(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyReturnService.cancelThirdPartyReturn(inputMap, userInfo);
	}

	@PostMapping(value = "/addThirdPartyReturnDetails")
	public ResponseEntity<Object> addThirdPartyReturnDetails(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioThirdPartyReturnCode = Integer.valueOf(inputMap.get("nbiothirdpartyreturncode").toString());
		final int noriginSiteCode = Integer.valueOf(inputMap.get("noriginsitecode").toString());
		return bioThirdPartyReturnService.addThirdPartyReturnDetails(nbioThirdPartyReturnCode, noriginSiteCode,
				userInfo);
	}

	@PostMapping(value = "/createChildBioThirdPartyReturn")
	public ResponseEntity<Object> createChildBioThirdPartyReturn(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyReturnService.createChildBioThirdPartyReturn(inputMap, userInfo);
	}

	@PostMapping(value = "/acceptRejectThirdPartyReturnSlide")
	public ResponseEntity<Object> acceptRejectThirdPartyReturnSlide(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyReturnService.acceptRejectThirdPartyReturnSlide(inputMap, userInfo);
	}

	@PostMapping(value = "/getSampleConditionStatus")
	public ResponseEntity<Object> getSampleConditionStatus(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyReturnService.getSampleConditionStatus(userInfo);
	}

	@PostMapping(value = "/getReason")
	public ResponseEntity<Object> getReason(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyReturnService.getReason(userInfo);
	}

	@PostMapping(value = "/updateSampleCondition")
	public ResponseEntity<Object> updateSampleCondition(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioThirdPartyReturnService.updateSampleCondition(inputMap, userInfo);
	}

	@PostMapping(value = "/deleteChildThirdPartyReturn")
	public ResponseEntity<Object> deleteChildThirdPartyReturn(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioThirdPartyReturnDetailsCode = (String) inputMap.get("nbiothirdpartyreturndetailscode");
		final int nbioThirdPartyReturnCode = (int) inputMap.get("nbiothirdpartyreturncode");
		return bioThirdPartyReturnService.deleteChildThirdPartyReturn(nbioThirdPartyReturnCode,
				nbioThirdPartyReturnDetailsCode, userInfo);
	}

	@PostMapping(value = "/disposeSamples")
	public ResponseEntity<Object> disposeSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioThirdPartyReturnDetailsCode = (String) inputMap.get("nbiothirdpartyreturndetailscode");
		final int nbioThirdPartyReturnCode = (int) inputMap.get("nbiothirdpartyreturncode");
		return bioThirdPartyReturnService.disposeSamples(nbioThirdPartyReturnCode, nbioThirdPartyReturnDetailsCode,
				userInfo);
	}

	@PostMapping(value = "/undoDisposeSamples")
	public ResponseEntity<Object> undoDisposeSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioThirdPartyReturnDetailsCode = (String) inputMap.get("nbiothirdpartyreturndetailscode");
		final int nbioThirdPartyReturnCode = (int) inputMap.get("nbiothirdpartyreturncode");
		return bioThirdPartyReturnService.undoDisposeSamples(nbioThirdPartyReturnCode, nbioThirdPartyReturnDetailsCode,
				userInfo);
	}

}