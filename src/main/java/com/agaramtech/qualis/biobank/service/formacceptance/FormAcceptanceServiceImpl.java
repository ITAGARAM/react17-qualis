package com.agaramtech.qualis.biobank.service.formacceptance;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.biobank.model.BioFormAcceptance;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class FormAcceptanceServiceImpl implements FormAcceptanceService {

	private final FormAcceptanceDAO formAcceptanceDAO;

	public FormAcceptanceServiceImpl(FormAcceptanceDAO formAcceptanceDAO) {
		this.formAcceptanceDAO = formAcceptanceDAO;
	}

	@Override
	public ResponseEntity<Object> getFormAcceptance(final Map<String, Object> inputMap,
			final int nbioFormAcceptanceCode, final UserInfo userInfo) throws Exception {
		return formAcceptanceDAO.getFormAcceptance(inputMap, nbioFormAcceptanceCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveBioFormAcceptance(final int nbioFormAcceptanceCode, final UserInfo userInfo)
			throws Exception {
		return formAcceptanceDAO.getActiveBioFormAcceptance(nbioFormAcceptanceCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTransferType(final UserInfo userInfo) throws Exception {
		return formAcceptanceDAO.getTransferType(userInfo);
	}

	@Override
	public ResponseEntity<Object> getSiteBasedOnTransferType(final UserInfo userInfo) throws Exception {
		return formAcceptanceDAO.getSiteBasedOnTransferType(userInfo);
	}

	@Override
	public ResponseEntity<Object> getProjectBasedOnSite(final int nbioBankSiteCode, final UserInfo userInfo)
			throws Exception {
		return formAcceptanceDAO.getProjectBasedOnSite(nbioBankSiteCode, userInfo);
	}

//	@Override
//	public ResponseEntity<Object> getParentSampleBasedOnProject(final int nbioProjectCode, final UserInfo userInfo)
//			throws Exception {
//		return formAcceptanceDAO.getParentSampleBasedOnProject(nbioProjectCode, userInfo);
//	}

	@Override
	public ResponseEntity<Object> getStorageType(final UserInfo userInfo) throws Exception {
		return formAcceptanceDAO.getStorageType(userInfo);
	}

//	@Override
//	public ResponseEntity<Object> getSampleTypeBySampleCode(final int nbioParentSampleCode, final int nstorageTypeCode,
//			final UserInfo userInfo) throws Exception {
//		return formAcceptanceDAO.getSampleTypeBySampleCode(nbioParentSampleCode, nstorageTypeCode, userInfo);
//	}

//	@Override
//	public ResponseEntity<Object> getSampleReceivingDetails(final Map<String, Object> inputMap, final UserInfo userInfo)
//			throws Exception {
//		return formAcceptanceDAO.getSampleReceivingDetails(inputMap, userInfo);
//	}

	@Override
	@Transactional
	public ResponseEntity<Object> moveToDisposeSamples(final int nbioFormAcceptanceCode,
			final String nbioFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception {
		return formAcceptanceDAO.moveToDisposeSamples(nbioFormAcceptanceCode, nbioFormAcceptanceDetailsCode, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> moveToReturnSamples(final int nbioFormAcceptanceCode,
			final String nbioFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception {
		return formAcceptanceDAO.moveToReturnSamples(nbioFormAcceptanceCode, nbioFormAcceptanceDetailsCode, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> undoReturnDisposeSamples(final int nbioFormAcceptanceCode,
			final String nbioFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception {
		return formAcceptanceDAO.undoReturnDisposeSamples(nbioFormAcceptanceCode, nbioFormAcceptanceDetailsCode,
				userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> storeSamples(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return formAcceptanceDAO.storeSamples(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSampleConditionStatus(final UserInfo userInfo) throws Exception {
		return formAcceptanceDAO.getSampleConditionStatus(userInfo);
	}

	@Override
	public ResponseEntity<Object> getReason(final UserInfo userInfo) throws Exception {
		return formAcceptanceDAO.getReason(userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateSampleCondition(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return formAcceptanceDAO.updateSampleCondition(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception {
		return formAcceptanceDAO.getStorageCondition(userInfo);
	}

	@Override
	public ResponseEntity<Object> getUsersBasedOnSite(final UserInfo userInfo) throws Exception {
		return formAcceptanceDAO.getUsersBasedOnSite(userInfo);
	}

	@Override
	public ResponseEntity<Object> getCourier(final UserInfo userInfo) throws Exception {
		return formAcceptanceDAO.getCourier(userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateReceiveBioFormAcceptance(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return formAcceptanceDAO.updateReceiveBioFormAcceptance(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateCompleteBioFormAcceptance(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return formAcceptanceDAO.updateCompleteBioFormAcceptance(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> validateFormAcceptance(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return formAcceptanceDAO.validateFormAcceptance(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> onCompleteSlideOut(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return formAcceptanceDAO.onCompleteSlideOut(inputMap, userInfo);
	}

	@Override
	public int findStatusFormAcceptance(final int nbioFormAcceptanceCode, final UserInfo userInfo) throws Exception {
		return formAcceptanceDAO.findStatusFormAcceptance(nbioFormAcceptanceCode, userInfo);
	}

	@Override
	public List<Map<String, Object>> getChildInitialGet(final int nbioFormAcceptanceCode, final UserInfo userInfo)
			throws Exception {
		return formAcceptanceDAO.getChildInitialGet(nbioFormAcceptanceCode, userInfo);
	}

	@Override
	public BioFormAcceptance getReceiveFormAcceptanceDetails(final int nbioFormAcceptanceCode, final UserInfo userInfo)
			throws Exception {
		return formAcceptanceDAO.getReceiveFormAcceptanceDetails(nbioFormAcceptanceCode, userInfo);
	}

//	@Override
//	public ResponseEntity<Object> awesomeQueryBuilderRecords(final UserInfo userInfo) throws Exception {
//		return formAcceptanceDAO.awesomeQueryBuilderRecords(userInfo);
//	}

	@Override
	public ResponseEntity<Object> getStorageFreezerData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return formAcceptanceDAO.getStorageFreezerData(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getStorageStructure(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return formAcceptanceDAO.getStorageStructure(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> moveToReturnDisposeAfterCompleteForm(int nbioFormAcceptanceCode,
			String nbioFormAcceptanceDetailsCode, UserInfo userInfo) throws Exception {
		return formAcceptanceDAO.moveToReturnDisposeAfterCompleteForm(nbioFormAcceptanceCode, nbioFormAcceptanceDetailsCode,
				userInfo);
	}

}
