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

import com.agaramtech.qualis.biobank.service.biorequestbasedtransfer.BioRequestBasedTransferService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/biorequestbasedtransfer")
public class BioRequestBasedTransferController {
	private static final Logger LOGGER = LoggerFactory.getLogger(BioRequestBasedTransferController.class);

	private final BioRequestBasedTransferService bioRequestBasedTransferService;
	private final RequestContext requestContext;

	public BioRequestBasedTransferController(final RequestContext requestContext,
			final BioRequestBasedTransferService bioRequestBasedTransferService) {
		this.requestContext = requestContext;
		this.bioRequestBasedTransferService = bioRequestBasedTransferService;
	}

	@PostMapping(value = "/cancelRequestBasedTransfer")
	public ResponseEntity<Object> cancelRequestBasedTransfer(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.cancelRequestBasedTransfer(inputMap, userInfo);
	}

	@PostMapping(value = "/createBioRequestbasedTransfer")
	public ResponseEntity<Object> createBioRequestbasedTransfer(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.createBioRequestbasedTransfer(inputMap, userInfo);
	}

	@PostMapping(value = "/createChildBioRequestBasedTransfer")
	public ResponseEntity<Object> createChildBioRequestBasedTransfer(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.createChildBioRequestBasedTransfer(inputMap, userInfo);
	}

	@PostMapping(value = "/createValidationBioRequestBasedTransfer")
	public ResponseEntity<Object> createValidationBioRequesstBasedTransfer(
			@RequestBody final Map<String, Object> inputMap) throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.createValidationBioRequestBasedTransfer(inputMap, userInfo);
	}

	@PostMapping(value = "/deleteChildRequestBasedTransfer")
	public ResponseEntity<Object> deleteChildRequestBasedTransfer(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final var nBioRequestBasedTransferDetailsCode = (String) inputMap.get("nbiorequestbasedtransferdetailcode");
		final var nBioRequestBasedTransferCode = (int) inputMap.get("nbiorequestbasedtransfercode");
		return bioRequestBasedTransferService.deleteChildRequestBasedTransfer(nBioRequestBasedTransferCode,
				nBioRequestBasedTransferDetailsCode, userInfo);
	}

	@PostMapping(value = "/disposeSamples")
	public ResponseEntity<Object> disposeSamples(@RequestBody final Map<String, Object> inputMap) throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final var nBioRequestBasedTransferDetailCode = (String) inputMap.get("nbiorequestbasedtransferdetailcode");
		final var nBioRequestBasedTransferCode = (int) inputMap.get("nbiorequestbasedtransfercode");
		return bioRequestBasedTransferService.disposeSamples(nBioRequestBasedTransferCode,
				nBioRequestBasedTransferDetailCode, userInfo);
	}

	@PostMapping(value = "/findStatusRequestBasedtTransfer")
	public int findStatusRequestBasedtTransfer(@RequestBody final Map<String, Object> inputMap) throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final var nbiorequestbasedtransfercode = (int) inputMap.get("nbiorequestbasedtransfercode");
		return bioRequestBasedTransferService.findStatusRequestBasedtTransfer(nbiorequestbasedtransfercode, userInfo);
	}

	@PostMapping(value = "/getActiveBioRequestBasedTransfer")
	public ResponseEntity<Object> getActiveBioRequestBasedTransfer(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nBioRequestBasedTransferCode = Integer
				.valueOf(inputMap.get("nbiorequestbasedtransfercode").toString());

		return bioRequestBasedTransferService.getActiveBioRequestBasedTransfer(inputMap, nBioRequestBasedTransferCode,
				userInfo);
	}

	@PostMapping(value = "/getActiveBioRequestBasedTransferById")
	public ResponseEntity<Object> getActiveBioRequestBasedTransferById(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.getActiveBioRequestBasedTransferById(inputMap, userInfo);
	}

	@PostMapping(value = "/getBioRequestBasedTransfer")
	public ResponseEntity<Object> getBioRequestBasedTransfer(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final var nbioRequestBasedTransferCode = -1;
		return bioRequestBasedTransferService.getBioRequestBasedTransfer(inputMap, nbioRequestBasedTransferCode,
				userInfo);
	}

	@PostMapping(value = "/getChildInitialGet")
	public List<Map<String, Object>> getChildInitialGet(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final var nbiorequestbasedtransfercode = (int) inputMap.get("nbiorequestbasedtransfercode");
		return bioRequestBasedTransferService.getChildInitialGet(nbiorequestbasedtransfercode, userInfo);
	}

	@PostMapping(value = "/getChildRequestBasedRecord")
	public ResponseEntity<Object> getChildRequestBasedRecord(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.getChildRequestBasedRecord(inputMap, userInfo);
	}

	@PostMapping(value = "/getCourier")
	public ResponseEntity<Object> getCourier(@RequestBody final Map<String, Object> inputMap) throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.getCourier(userInfo);
	}

	@PostMapping(value = "/getProjectBasedOnSample")
	public ResponseEntity<Object> getProjectBasedOnSample(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return bioRequestBasedTransferService.getProjectBasedOnSample(inputMap, userInfo);
	}

	@PostMapping(value = "/getProjectBasedOnSite")
	public ResponseEntity<Object> getProjectBasedOnSite(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return bioRequestBasedTransferService.getProjectBasedOnSite(inputMap, userInfo);
	}

	@PostMapping(value = "/getReason")
	public ResponseEntity<Object> getReason(@RequestBody final Map<String, Object> inputMap) throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.getReason(userInfo);
	}

	@PostMapping(value = "/getRequestAcceptanceType")
	public ResponseEntity<Object> getRequestAcceptanceType(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.getRequestAcceptanceType(userInfo);
	}

	@PostMapping(value = "/getRequestBasedProjectSampleParentLoad")
	public ResponseEntity<Object> getRequestBasedProjectSampleParentLoad(
			@RequestBody final Map<String, Object> inputMap) throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.getRequestBasedProjectSampleParentLoad(inputMap, userInfo);
	}

	@PostMapping(value = "/getSampleConditionStatus")
	public ResponseEntity<Object> getSampleConditionStatus(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.getSampleConditionStatus(userInfo);
	}

	@PostMapping(value = "/getSampleReceivingDetails")
	public ResponseEntity<Object> getSampleReceivingDetails(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.getSampleReceivingDetails(inputMap, userInfo);
	}

	@PostMapping(value = "/getStorageCondition")
	public ResponseEntity<Object> getStorageCondition(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.getStorageCondition(userInfo);
	}

	@PostMapping(value = "/getStorageType")
	public ResponseEntity<Object> getStorageType(@RequestBody final Map<String, Object> inputMap) throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.getStorageType(userInfo);
	}

	@PostMapping(value = "/getTransferType")
	public ResponseEntity<Object> getTransferType(@RequestBody final Map<String, Object> inputMap) throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.getTransferType(userInfo);
	}

	@PostMapping(value = "/getTransferTypeBasedFormNo")

	public ResponseEntity<Object> getTransferTypeBasedFormNo(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.getTransferTypeBasedFormNo(inputMap, userInfo);
	}

	@PostMapping(value = "/getTransferTypeRecord")
	public ResponseEntity<Object> getTransferTypeRecord(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.getTransferTypeRecord(inputMap, userInfo);
	}

	@PostMapping(value = "/getUsersBasedOnSite")
	public ResponseEntity<Object> getUsersBasedOnSite(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.getUsersBasedOnSite(userInfo);
	}

	@PostMapping(value = "/requestBasedTransfer")
	public ResponseEntity<Object> requestBasedTransfer(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.requestBasedTransfer(inputMap, userInfo);
	}

	@PostMapping(value = "/updateBioRequestBasedTransfer")
	public ResponseEntity<Object> updateBioRequestBasedTransfer(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.updateBioRequestBasedTransfer(inputMap, userInfo);
	}

	@PostMapping(value = "/updateSampleCondition")
	public ResponseEntity<Object> updateSampleCondition(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.updateSampleCondition(inputMap, userInfo);
	}
	
	@PostMapping(value = "/getbioparentbasedsampleandvolume")
	public ResponseEntity<Object> getbioparentbasedsampleandvolume(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioRequestBasedTransferService.getbioparentbasedsampleandvolume(inputMap, userInfo);
	}

	@PostMapping(value = "/checkAccessibleSamples")
	public ResponseEntity<Object> checkAccessibleSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioRequestBasedTransferCode = (int) inputMap.get("nbiorequestbasedtransfercode");
		return bioRequestBasedTransferService.checkAccessibleSamples(nbioRequestBasedTransferCode, userInfo);
	}
	
	@PostMapping(value = "/undoDisposeSamples")
	public ResponseEntity<Object> undoDisposeSamples(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final String nbioRequestBasedTransferDetailsCode = (String) inputMap.get("nbiorequestbasedtransferdetailcode");
		final int nbioRequestBasedeTransferCode = (int) inputMap.get("nbiorequestbasedtransfercode");
		return bioRequestBasedTransferService.undoDisposeSamples(nbioRequestBasedeTransferCode, nbioRequestBasedTransferDetailsCode,
				userInfo);
	}
	
	
	
}