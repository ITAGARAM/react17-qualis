package com.agaramtech.qualis.biobank.service.formacceptance;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.biobank.model.BioFormAcceptance;
import com.agaramtech.qualis.global.UserInfo;

public interface FormAcceptanceDAO {

	public ResponseEntity<Object> getFormAcceptance(final Map<String, Object> inputMap,
			final int nbioFormAcceptanceCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getActiveBioFormAcceptance(final int nbioFormAcceptanceCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getTransferType(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSiteBasedOnTransferType(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getProjectBasedOnSite(final int nbioBankSiteCode, final UserInfo userInfo)
			throws Exception;

//	public ResponseEntity<Object> getParentSampleBasedOnProject(final int nbioProjectCode, final UserInfo userInfo)
//			throws Exception;

	public ResponseEntity<Object> getStorageType(final UserInfo userInfo) throws Exception;

//	public ResponseEntity<Object> getSampleTypeBySampleCode(final int nbioParentSampleCode, final int nstorageTypeCode,
//			final UserInfo userInfo) throws Exception;

//	public ResponseEntity<Object> getSampleReceivingDetails(final Map<String, Object> inputMap, final UserInfo userInfo)
//			throws Exception;

	public ResponseEntity<Object> moveToDisposeSamples(final int nbioFormAcceptanceCode,
			final String nbioFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> moveToReturnSamples(final int nbioFormAcceptanceCode,
			final String nbioFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> undoReturnDisposeSamples(final int nbioFormAcceptanceCode,
			final String nbioFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> storeSamples(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getSampleConditionStatus(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getReason(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateSampleCondition(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getUsersBasedOnSite(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getCourier(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateReceiveBioFormAcceptance(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateCompleteBioFormAcceptance(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> validateFormAcceptance(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> onCompleteSlideOut(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public int findStatusFormAcceptance(final int nbioFormAcceptanceCode, final UserInfo userInfo) throws Exception;

	public List<Map<String, Object>> getChildInitialGet(final int nbioFormAcceptanceCode, final UserInfo userInfo)
			throws Exception;

	public BioFormAcceptance getReceiveFormAcceptanceDetails(final int nbioFormAcceptanceCode, final UserInfo userInfo)
			throws Exception;

//	public ResponseEntity<Object> awesomeQueryBuilderRecords(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getStorageFreezerData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getStorageStructure(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> moveToReturnDisposeAfterCompleteForm(int nbioFormAcceptanceCode,
			String nbioFormAcceptanceDetailsCode, UserInfo userInfo) throws Exception;

}
