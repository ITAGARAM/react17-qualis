package com.agaramtech.qualis.biobank.service.biothirdpartyformacceptance;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface BioThirdPartyFormAcceptanceDAO {

	public ResponseEntity<Object> getBioThirdPartyFormAcceptance(final Map<String, Object> inputMap,
			final int nbioThirdPartyFormAcceptanceCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getActiveBioThirdPartyFormAcceptance(final int nbioThirdPartyFormAcceptanceCode,
			final UserInfo userInfo) throws Exception;

//
//	public ResponseEntity<Object> getTransferType(final UserInfo userInfo) throws Exception;
//
//	public ResponseEntity<Object> getSiteBasedOnTransferType(final UserInfo userInfo) throws Exception;
//
//	public ResponseEntity<Object> getProjectBasedOnSite(final int nbioBankSiteCode, final UserInfo userInfo)
//			throws Exception;
//
////	public ResponseEntity<Object> getParentSampleBasedOnProject(final int nbioProjectCode, final UserInfo userInfo)
////			throws Exception;
//
//	public ResponseEntity<Object> getStorageType(final UserInfo userInfo) throws Exception;
//
////	public ResponseEntity<Object> getSampleTypeBySampleCode(final int nbioParentSampleCode, final int nstorageTypeCode,
////			final UserInfo userInfo) throws Exception;
//
////	public ResponseEntity<Object> getSampleReceivingDetails(final Map<String, Object> inputMap, final UserInfo userInfo)
////			throws Exception;

	public ResponseEntity<Object> moveToDisposeSamples(final int nbioThirdPartyFormAcceptanceCode,
			final String nbioThirdPartyFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> moveToReturnSamples(final int nbioThirdPartyFormAcceptanceCode,
			final String nbioThirdPartyFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> undoReturnDisposeSamples(final int nbioThirdPartyFormAcceptanceCode,
			final String nbioThirdPartyFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception;

//	public ResponseEntity<Object> storeSamples(final Map<String, Object> inputMap, final UserInfo userInfo)
//			throws Exception;

	public ResponseEntity<Object> getSampleConditionStatus(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getReason(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateSampleCondition(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getUsersBasedOnSite(final UserInfo userInfo) throws Exception;

//	public ResponseEntity<Object> getCourier(final UserInfo userInfo) throws Exception;
//
	public ResponseEntity<Object> updateReceiveBioThirdPartyFormAcceptance(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateCompleteBioThirdPartyFormAcceptance(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> validateBioThirdPartyFormAcceptance(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> onCompleteSlideOut(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public int findStatusBioThirdPartyFormAcceptance(final int nbioThirdPartyFormAcceptanceCode,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> acceptRejectBioThirdPartyFormAcceptanceSlide(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

//	public List<Map<String, Object>> getChildInitialGet(final int nbioThirdPartyFormAcceptanceCode,
//			final UserInfo userInfo) throws Exception;
//
	public ResponseEntity<Object> getReceiveBioThirdPartyFormAcceptanceDetails(
			final int nbioThirdPartyFormAcceptanceCode, final UserInfo userInfo) throws Exception;
//
////	public ResponseEntity<Object> awesomeQueryBuilderRecords(final UserInfo userInfo) throws Exception;
//
//	public ResponseEntity<Object> getStorageFreezerData(final Map<String, Object> inputMap, final UserInfo userInfo)
//			throws Exception;
//
//	public ResponseEntity<Object> getStorageStructure(final Map<String, Object> inputMap, final UserInfo userInfo)
//			throws Exception;

	public ResponseEntity<Object> moveToReturnSamplesAfterComplete(int nbioThirdPartyFormAcceptanceCode,
			String nbioThirdPartyFormAcceptanceDetailsCode, UserInfo userInfo) throws Exception;

}
