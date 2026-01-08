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

import com.agaramtech.qualis.biobank.service.biodisposalsampleapproval.BioDisposalSampleApprovalService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/disposalsamplesapproval")
public class BioDisposalSampleApprovalController {
	private static final Logger LOGGER = LoggerFactory.getLogger(BioDisposalSampleApprovalController.class);

	private final BioDisposalSampleApprovalService bioDisposalSampleApprovalService;
	private final RequestContext requestContext;

	public BioDisposalSampleApprovalController(final RequestContext requestContext,
			final BioDisposalSampleApprovalService bioDisposalSampleApprovalService) {
		this.requestContext = requestContext;
		this.bioDisposalSampleApprovalService = bioDisposalSampleApprovalService;
	}

	@PostMapping(value = "/getDisposalSamplesApproval")
	public ResponseEntity<Object> getDisposalSampleApproval(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.getDisposalSampleApproval(inputMap, userInfo);
	}

	@PostMapping(value = "/getChildInitialGet")
	public List<Map<String, Object>> getChildInitialGet(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final var nbiorequestbasedtransfercode = (int) inputMap.get("nbiorequestbasedtransfercode");
		return bioDisposalSampleApprovalService.getChildInitialGet(nbiorequestbasedtransfercode, userInfo);
	}

	@PostMapping(value = "/fetchDisposeSampleValidate")
	public ResponseEntity<Map<String, Object>> fetchDisposeSampleValidate(
			@RequestBody final Map<String, Object> inputMap) throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return bioDisposalSampleApprovalService.fetchDisposeSampleValidate(inputMap, userInfo);
	}

	@PostMapping(value = "/approvalDisposalSamples")
	public ResponseEntity<Object> approvalDisposalSamples(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return bioDisposalSampleApprovalService.approvalDisposalSamples(inputMap, userInfo);
	}

	@PostMapping(value = "/createValidationBioDisposeSample")
	public ResponseEntity<Object> createValidationBioDisposeSample(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return bioDisposalSampleApprovalService.createValidationBioDisposeSample(inputMap, userInfo);
	}

	@PostMapping(value = "/getSampleReceivingDetails")
	public ResponseEntity<Object> getSampleReceivingDetails(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.getSampleReceivingDetails(inputMap, userInfo);
	}

	@PostMapping(value = "/getDisposalBatchType")
	public ResponseEntity<Object> getDisposalBatchType(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.getDisposalBatchType(userInfo);
	}

	@PostMapping(value = "/getDisposalBatchFormType")
	public ResponseEntity<Object> getDisposalBatchFormType(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.getDisposalBatchFormType(inputMap, userInfo);
	}

	@PostMapping(value = "/getFormTypeSiteBasedFormNumber")
	public ResponseEntity<Object> getFormTypeSiteBasedFormNumber(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.getFormTypeSiteBasedFormNumber(inputMap, userInfo);
	}

	@PostMapping(value = "/getFormTypeBasedSiteAndThirdParty")
	public ResponseEntity<Object> getFormTypeBasedSiteAndThirdParty(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.getFormTypeBasedSiteAndThirdParty(inputMap, userInfo);
	}

	@PostMapping(value = "/getExtractedColumnData")
	public ResponseEntity<Object> getExtractedColumnData(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.getExtractedColumnData(inputMap, userInfo);
	}

	@PostMapping(value = "/createdisposalsamplesapproval")
	public ResponseEntity<Object> createDisposalSamplesApproval(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.createDisposalSamplesApproval(inputMap, userInfo);
	}

	@PostMapping(value = "/getDynamicDisposeFilterData")
	public ResponseEntity<Object> getDynamicDisposeFilterData(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.getDynamicDisposeFilterData(inputMap, userInfo);
	}

	@PostMapping(value = "/getActiveDisposalSampleApproval")
	public ResponseEntity<Object> getActiveDisposalSampleApproval(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.getActiveDisposalSampleApproval(inputMap, userInfo);
	}

	@PostMapping(value = "/updateSampleCondition")
	public ResponseEntity<Object> updateSampleCondition(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.updateSampleCondition(inputMap, userInfo);
	}

	@PostMapping(value = "/getSampleConditionStatus")
	public ResponseEntity<Object> getSampleConditionStatus(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return bioDisposalSampleApprovalService.getSampleConditionStatus(inputMap, userInfo);
	}

	@PostMapping(value = "/getReason")
	public ResponseEntity<Object> getReason(@RequestBody final Map<String, Object> inputMap) throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.getReason(userInfo);
	}

	@PostMapping(value = "/deleteDisposalSamplesApproval")
	public ResponseEntity<Object> deleteDisposalSamplesApproval(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return bioDisposalSampleApprovalService.deleteDisposalSamplesApproval(inputMap, userInfo);
	}

	@PostMapping(value = "/findStatusDisposeSample")
	public int findStatusDisposeSample(@RequestBody final Map<String, Object> inputMap) throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.findStatusDisposeSample(inputMap, userInfo);
	}

	@PostMapping(value = "/createChildDisposalSample")
	public ResponseEntity<Object> createChildDisposalSample(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.createChildDisposalSample(inputMap, userInfo);
	}

	@PostMapping(value = "/getStorageFreezerData")
	public ResponseEntity<Object> getStorageFreezerData(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.getStorageFreezerData(inputMap, userInfo);
	}

	@PostMapping(value = "/getStorageStructure")
	public ResponseEntity<Object> getStorageStructure(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.getStorageStructure(inputMap, userInfo);
	}

	@PostMapping(value = "/disposeSamplesApproval")
	public ResponseEntity<Object> disposeSamplesApproval(@RequestBody final Map<String, Object> inputMap)
			throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.disposeSamplesApproval(inputMap, userInfo);
	}

	@PostMapping(value = "/createStoreSamples")
	public ResponseEntity<Object> createStoreSamples(@RequestBody final Map<String, Object> inputMap) throws Exception {

		final var objMapper = new ObjectMapper();
		final var userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return bioDisposalSampleApprovalService.createStoreSamples(inputMap, userInfo);
	}
}