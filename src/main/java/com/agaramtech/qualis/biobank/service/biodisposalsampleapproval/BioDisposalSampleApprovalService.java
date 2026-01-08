package com.agaramtech.qualis.biobank.service.biodisposalsampleapproval;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface BioDisposalSampleApprovalService {

	public ResponseEntity<Object> getDisposalSampleApproval(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public List<Map<String, Object>> getChildInitialGet(int nbiorequestbasedtransfercode, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> disposeSamplesApproval(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSampleReceivingDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateSampleCondition(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getReason(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getDisposalBatchType(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getDisposalBatchFormType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getFormTypeSiteBasedFormNumber(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getFormTypeBasedSiteAndThirdParty(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getExtractedColumnData(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getDynamicDisposeFilterData(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createDisposalSamplesApproval(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getActiveDisposalSampleApproval(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteDisposalSamplesApproval(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public int findStatusDisposeSample(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createChildDisposalSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Map<String, Object>> fetchDisposeSampleValidate(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createValidationBioDisposeSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSampleConditionStatus(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getStorageFreezerData(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getStorageStructure(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> approvalDisposalSamples(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createStoreSamples(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

}
