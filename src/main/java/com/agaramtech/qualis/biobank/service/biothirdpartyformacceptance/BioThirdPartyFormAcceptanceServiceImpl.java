package com.agaramtech.qualis.biobank.service.biothirdpartyformacceptance;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class BioThirdPartyFormAcceptanceServiceImpl implements BioThirdPartyFormAcceptanceService {

	private final BioThirdPartyFormAcceptanceDAO bioThirdPartyFormAcceptanceDAO;

	public BioThirdPartyFormAcceptanceServiceImpl(BioThirdPartyFormAcceptanceDAO bioThirdPartyFormAcceptanceDAO) {
		this.bioThirdPartyFormAcceptanceDAO = bioThirdPartyFormAcceptanceDAO;
	}

	@Override
	public ResponseEntity<Object> getBioThirdPartyFormAcceptance(final Map<String, Object> inputMap,
			final int nbioThirdPartyFormAcceptanceCode, final UserInfo userInfo) throws Exception {
		return bioThirdPartyFormAcceptanceDAO.getBioThirdPartyFormAcceptance(inputMap, nbioThirdPartyFormAcceptanceCode,
				userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveBioThirdPartyFormAcceptance(final int nbioThirdPartyFormAcceptanceCode,
			final UserInfo userInfo) throws Exception {
		return bioThirdPartyFormAcceptanceDAO.getActiveBioThirdPartyFormAcceptance(nbioThirdPartyFormAcceptanceCode,
				userInfo);
	}

//
//	@Override
//	public ResponseEntity<Object> getTransferType(final UserInfo userInfo) throws Exception {
//		return bioThirdPartyFormAcceptanceDAO.getTransferType(userInfo);
//	}
//
//	@Override
//	public ResponseEntity<Object> getSiteBasedOnTransferType(final UserInfo userInfo) throws Exception {
//		return bioThirdPartyFormAcceptanceDAO.getSiteBasedOnTransferType(userInfo);
//	}
//
//	@Override
//	public ResponseEntity<Object> getProjectBasedOnSite(final int nbioBankSiteCode, final UserInfo userInfo)
//			throws Exception {
//		return bioThirdPartyFormAcceptanceDAO.getProjectBasedOnSite(nbioBankSiteCode, userInfo);
//	}
//
////	@Override
////	public ResponseEntity<Object> getParentSampleBasedOnProject(final int nbioProjectCode, final UserInfo userInfo)
////			throws Exception {
////		return bioThirdPartyFormAcceptanceDAO.getParentSampleBasedOnProject(nbioProjectCode, userInfo);
////	}
//
//	@Override
//	public ResponseEntity<Object> getStorageType(final UserInfo userInfo) throws Exception {
//		return bioThirdPartyFormAcceptanceDAO.getStorageType(userInfo);
//	}
//
////	@Override
////	public ResponseEntity<Object> getSampleTypeBySampleCode(final int nbioParentSampleCode, final int nstorageTypeCode,
////			final UserInfo userInfo) throws Exception {
////		return bioThirdPartyFormAcceptanceDAO.getSampleTypeBySampleCode(nbioParentSampleCode, nstorageTypeCode, userInfo);
////	}
//
////	@Override
////	public ResponseEntity<Object> getSampleReceivingDetails(final Map<String, Object> inputMap, final UserInfo userInfo)
////			throws Exception {
////		return bioThirdPartyFormAcceptanceDAO.getSampleReceivingDetails(inputMap, userInfo);
////	}

	@Override
	@Transactional
	public ResponseEntity<Object> moveToDisposeSamples(final int nbioThirdPartyFormAcceptanceCode,
			final String nbioThirdPartyFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception {
		return bioThirdPartyFormAcceptanceDAO.moveToDisposeSamples(nbioThirdPartyFormAcceptanceCode,
				nbioThirdPartyFormAcceptanceDetailsCode, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> moveToReturnSamples(final int nbioThirdPartyFormAcceptanceCode,
			final String nbioThirdPartyFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception {
		return bioThirdPartyFormAcceptanceDAO.moveToReturnSamples(nbioThirdPartyFormAcceptanceCode,
				nbioThirdPartyFormAcceptanceDetailsCode, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> undoReturnDisposeSamples(final int nbioThirdPartyFormAcceptanceCode,
			final String nbioThirdPartyFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception {
		return bioThirdPartyFormAcceptanceDAO.undoReturnDisposeSamples(nbioThirdPartyFormAcceptanceCode,
				nbioThirdPartyFormAcceptanceDetailsCode, userInfo);
	}

//	@Override
//	@Transactional
//	public ResponseEntity<Object> storeSamples(final Map<String, Object> inputMap, final UserInfo userInfo)
//			throws Exception {
//		return bioThirdPartyFormAcceptanceDAO.storeSamples(inputMap, userInfo);
//	}

	@Override
	public ResponseEntity<Object> getSampleConditionStatus(final UserInfo userInfo) throws Exception {
		return bioThirdPartyFormAcceptanceDAO.getSampleConditionStatus(userInfo);
	}

	@Override
	public ResponseEntity<Object> getReason(final UserInfo userInfo) throws Exception {
		return bioThirdPartyFormAcceptanceDAO.getReason(userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateSampleCondition(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioThirdPartyFormAcceptanceDAO.updateSampleCondition(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception {
		return bioThirdPartyFormAcceptanceDAO.getStorageCondition(userInfo);
	}

	@Override
	public ResponseEntity<Object> getUsersBasedOnSite(final UserInfo userInfo) throws Exception {
		return bioThirdPartyFormAcceptanceDAO.getUsersBasedOnSite(userInfo);
	}

//	@Override
//	public ResponseEntity<Object> getCourier(final UserInfo userInfo) throws Exception {
//		return bioThirdPartyFormAcceptanceDAO.getCourier(userInfo);
//	}
//
	@Override
	@Transactional
	public ResponseEntity<Object> updateReceiveBioThirdPartyFormAcceptance(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioThirdPartyFormAcceptanceDAO.updateReceiveBioThirdPartyFormAcceptance(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> updateCompleteBioThirdPartyFormAcceptance(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioThirdPartyFormAcceptanceDAO.updateCompleteBioThirdPartyFormAcceptance(inputMap, userInfo);
	}

	@Override
	@Transactional
	public ResponseEntity<Object> validateBioThirdPartyFormAcceptance(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioThirdPartyFormAcceptanceDAO.validateBioThirdPartyFormAcceptance(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> onCompleteSlideOut(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return bioThirdPartyFormAcceptanceDAO.onCompleteSlideOut(inputMap, userInfo);
	}

	@Override
	public int findStatusBioThirdPartyFormAcceptance(final int nbioThirdPartyFormAcceptanceCode,
			final UserInfo userInfo) throws Exception {
		return bioThirdPartyFormAcceptanceDAO.findStatusBioThirdPartyFormAcceptance(nbioThirdPartyFormAcceptanceCode,
				userInfo);
	}

	@Override
	public ResponseEntity<Object> acceptRejectBioThirdPartyFormAcceptanceSlide(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return bioThirdPartyFormAcceptanceDAO.acceptRejectBioThirdPartyFormAcceptanceSlide(inputMap, userInfo);
	}

//	@Override
//	public List<Map<String, Object>> getChildInitialGet(final int nbioThirdPartyFormAcceptanceCode, final UserInfo userInfo)
//			throws Exception {
//		return bioThirdPartyFormAcceptanceDAO.getChildInitialGet(nbioThirdPartyFormAcceptanceCode, userInfo);
//	}
//
	@Override
	public ResponseEntity<Object> getReceiveBioThirdPartyFormAcceptanceDetails(
			final int nbioThirdPartyFormAcceptanceCode, final UserInfo userInfo) throws Exception {
		return bioThirdPartyFormAcceptanceDAO
				.getReceiveBioThirdPartyFormAcceptanceDetails(nbioThirdPartyFormAcceptanceCode, userInfo);
	}
//
////	@Override
////	public ResponseEntity<Object> awesomeQueryBuilderRecords(final UserInfo userInfo) throws Exception {
////		return bioThirdPartyFormAcceptanceDAO.awesomeQueryBuilderRecords(userInfo);
////	}
//
//	@Override
//	public ResponseEntity<Object> getStorageFreezerData(final Map<String, Object> inputMap, final UserInfo userInfo)
//			throws Exception {
//		return bioThirdPartyFormAcceptanceDAO.getStorageFreezerData(inputMap, userInfo);
//	}
//
//	@Override
//	public ResponseEntity<Object> getStorageStructure(final Map<String, Object> inputMap, final UserInfo userInfo)
//			throws Exception {
//		return bioThirdPartyFormAcceptanceDAO.getStorageStructure(inputMap, userInfo);
//	}

	@Override
	@Transactional
	public ResponseEntity<Object> moveToReturnSamplesAfterComplete(int nbioThirdPartyFormAcceptanceCode,
			String nbioThirdPartyFormAcceptanceDetailsCode, UserInfo userInfo) throws Exception {
		return bioThirdPartyFormAcceptanceDAO.moveToReturnSamplesAfterComplete(nbioThirdPartyFormAcceptanceCode,
				nbioThirdPartyFormAcceptanceDetailsCode, userInfo);
	}

}
