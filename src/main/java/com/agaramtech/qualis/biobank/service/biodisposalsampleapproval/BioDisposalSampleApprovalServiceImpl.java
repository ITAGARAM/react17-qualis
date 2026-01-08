package com.agaramtech.qualis.biobank.service.biodisposalsampleapproval;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class BioDisposalSampleApprovalServiceImpl implements BioDisposalSampleApprovalService {

	private final BioDisposalSampleApprovalDAO bioDisposalSampleApprovalDAO;

	public BioDisposalSampleApprovalServiceImpl(final BioDisposalSampleApprovalDAO bioDisposalSampleApprovalDAO) {
		this.bioDisposalSampleApprovalDAO = bioDisposalSampleApprovalDAO;
	}

	@Override
	public ResponseEntity<Object> getDisposalSampleApproval(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return bioDisposalSampleApprovalDAO.getDisposalSampleApproval(inputMap, userInfo);
	}

	@Override
	public List<Map<String, Object>> getChildInitialGet(final int nbiorequestbasedtransfercode, final UserInfo userInfo)
			throws Exception {

		return bioDisposalSampleApprovalDAO.getChildInitialGet(nbiorequestbasedtransfercode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> disposeSamplesApproval(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDisposalSampleApprovalDAO.disposeSamplesApproval(inputMap, userInfo);

	}

	@Override
	public ResponseEntity<Object> getSampleReceivingDetails(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDisposalSampleApprovalDAO.getSampleReceivingDetails(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getDisposalBatchType(final UserInfo userInfo) throws Exception {
		return bioDisposalSampleApprovalDAO.getDisposalBatchType(userInfo);

	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleCondition(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDisposalSampleApprovalDAO.updateSampleCondition(inputMap, userInfo);

	}

	@Override
	public ResponseEntity<Object> getReason(final UserInfo userInfo) throws Exception {
		return bioDisposalSampleApprovalDAO.getReason(userInfo);

	}

	@Override
	public ResponseEntity<Object> getDisposalBatchFormType(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDisposalSampleApprovalDAO.getDisposalBatchFormType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getFormTypeSiteBasedFormNumber(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDisposalSampleApprovalDAO.getFormTypeSiteBasedFormNumber(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getFormTypeBasedSiteAndThirdParty(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDisposalSampleApprovalDAO.getFormTypeBasedSiteAndThirdParty(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getExtractedColumnData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDisposalSampleApprovalDAO.getExtractedColumnData(inputMap, userInfo);

	}

	@Override
	public ResponseEntity<Object> getDynamicDisposeFilterData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDisposalSampleApprovalDAO.getDynamicDisposeFilterData(inputMap, userInfo);

	}

	@Transactional
	@Override
	public ResponseEntity<Object> createDisposalSamplesApproval(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDisposalSampleApprovalDAO.createDisposalSamplesApproval(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveDisposalSampleApproval(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDisposalSampleApprovalDAO.getActiveDisposalSampleApproval(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteDisposalSamplesApproval(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDisposalSampleApprovalDAO.deleteDisposalSamplesApproval(inputMap, userInfo);
	}

	@Override
	public int findStatusDisposeSample(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		return bioDisposalSampleApprovalDAO.findStatusDisposeSample(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createChildDisposalSample(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDisposalSampleApprovalDAO.createChildDisposalSample(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> fetchDisposeSampleValidate(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioDisposalSampleApprovalDAO.fetchDisposeSampleValidate(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createValidationBioDisposeSample(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return bioDisposalSampleApprovalDAO.createValidationBioDisposeSample(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSampleConditionStatus(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return bioDisposalSampleApprovalDAO.getSampleConditionStatus(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getStorageFreezerData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDisposalSampleApprovalDAO.getStorageFreezerData(inputMap, userInfo);

	}

	@Override
	public ResponseEntity<Object> getStorageStructure(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return bioDisposalSampleApprovalDAO.getStorageStructure(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> approvalDisposalSamples(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioDisposalSampleApprovalDAO.approvalDisposalSamples(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createStoreSamples(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		return bioDisposalSampleApprovalDAO.createStoreSamples(inputMap, userInfo);
	}


}
