package com.agaramtech.qualis.biobank.service.biobankreturn;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface BioBankReturnService {

	public ResponseEntity<Object> getBioBankReturn(final Map<String, Object> inputMap, final int nbioBankReturnCode,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getFormNumberDetails(final int noriginSiteCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getFormAcceptanceDetails(final int nbioFormAccetanceCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> createBioBankReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getActiveBioBankReturn(final int nbioBankReturnCode, final int saveType,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getActiveBioBankReturnById(final int nbioBankReturnCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateBioBankReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public int findStatusBankReturn(final int nbioBankReturnCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getValidateFormDetails(final int nbioBankReturnCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getUsersBasedOnSite(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getCourier(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createValidationBioBankReturn(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> returnBankReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> cancelBankReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> addBankReturnDetails(final int nbioBankReturnCode, final int noriginSiteCode,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createChildBioBankReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> acceptRejectBankReturnSlide(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSampleConditionStatus(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getReason(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateSampleCondition(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteChildBankReturn(final int nbioBankReturnCode,
			final String nbioBankReturnDetailsCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> disposeSamples(final int nbioBankReturnCode, final String nbioBankReturnDetailsCode,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getStorageFreezerData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getStorageStructure(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> storeSamples(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> undoDisposeSamples(final int nbioBankReturnCode,
			final String nbioBankReturnDetailsCode, final UserInfo userInfo) throws Exception;

}
