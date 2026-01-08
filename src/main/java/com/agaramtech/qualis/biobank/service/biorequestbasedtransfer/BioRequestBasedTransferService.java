package com.agaramtech.qualis.biobank.service.biorequestbasedtransfer;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface BioRequestBasedTransferService {

	public ResponseEntity<Object> cancelRequestBasedTransfer(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createBioRequestbasedTransfer(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createChildBioRequestBasedTransfer(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createValidationBioRequestBasedTransfer(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteChildRequestBasedTransfer(int nBioRequestBasedTransferCode,
			String nBioRequestBasedTransferDetailsCode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> disposeSamples(final int nBioRequestBasedTransferCode,
			final String nBioRequestBasedTransferDetailCode, final UserInfo userInfo) throws Exception;

	public int findStatusRequestBasedtTransfer(final int nbiorequestbasedtransfercode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getActiveBioRequestBasedTransfer(Map<String, Object> inputMap,
			int nBioRequestBasedTransferCode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getActiveBioRequestBasedTransferById(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getBioRequestBasedTransfer(final Map<String, Object> inputMap,
			final int getBioRequestBasedTransfer, final UserInfo userInfo) throws Exception;

	public List<Map<String, Object>> getChildInitialGet(final int nbiorequestbasedtransfercode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getChildRequestBasedRecord(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getCourier(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getProjectBasedOnSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getProjectBasedOnSite(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getReason(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRequestAcceptanceType(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getRequestBasedProjectSampleParentLoad(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSampleConditionStatus(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSampleReceivingDetails(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getStorageType(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getTransferType(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getTransferTypeBasedFormNo(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getTransferTypeRecord(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getUsersBasedOnSite(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> requestBasedTransfer(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateBioRequestBasedTransfer(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateSampleCondition(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getbioparentbasedsampleandvolume(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> checkAccessibleSamples(final int nbioRequestBasedTransferCode,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> undoDisposeSamples(int nbioRequestBasedeTransferCode,
			String nbioRequestBasedTransferDetailsCode, UserInfo userInfo) throws Exception;

}
