package com.agaramtech.qualis.biobank.service.biothirdpartyreturn;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface BioThirdPartyReturnDAO {

	public ResponseEntity<Object> getBioThirdPartyReturn(final Map<String, Object> inputMap,
			final int nbioThirdPartyReturnCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getFormNumberDetails(final int noriginSiteCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getThirdPartyFormAcceptanceDetails(final int nbioThirdPartyFormAccetanceCode,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createBioThirdPartyReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getActiveBioThirdPartyReturn(final int nbioThirdPartyReturnCode, final int saveType,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getActiveBioThirdPartyReturnById(final int nbioThirdPartyReturnCode,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateBioThirdPartyReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public int findStatusThirdPartyReturn(final int nbioThirdPartyReturnCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getValidateFormDetails(final int nbioThirdPartyReturnCode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getUsersBasedOnSite(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getCourier(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createValidationBioThirdPartyReturn(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> returnThirdPartyReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> cancelThirdPartyReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> addThirdPartyReturnDetails(final int nbioThirdPartyReturnCode,
			final int noriginSiteCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createChildBioThirdPartyReturn(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> acceptRejectThirdPartyReturnSlide(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSampleConditionStatus(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getReason(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateSampleCondition(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> deleteChildThirdPartyReturn(final int nbioThirdPartyReturnCode,
			final String nbioThirdPartyReturnDetailsCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> disposeSamples(final int nbioThirdPartyReturnCode,
			final String nbioThirdPartyReturnDetailsCode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> undoDisposeSamples(final int nbioThirdPartyReturnCode,
			final String nbioThirdPartyReturnDetailsCode, final UserInfo userInfo) throws Exception;

}
