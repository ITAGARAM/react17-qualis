package com.agaramtech.qualis.biobank.service.biothirdpartyecatalogue;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface BioThirdPartyECatalogueService {

	ResponseEntity<Object> getBioThirdPartyECatalogue(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getComboDataForCatalogue(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getAggregatedDataForCatalogue(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getDetailedDataForCatalogue(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> createThirdPartyECatalogueRequest(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getThirdPartyECatalogueByFilterSubmit(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	ResponseEntity<Map<String, Object>> getActiveThirdPartyECatalogueRequestForm(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getSiteComboForProject(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> sendThirdPartyECatalogueRequest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> cancelThirdPartyECatalogueRequest(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getProductComboDataForSampleAdd(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> createThirdPartyECatalogueRequestSample(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> updateThirdPartyECatalogueRequestSample(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> deleteThirdPartyECatalogueRequestSample(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	ResponseEntity<Map<String, Object>> getActiveSampleDetail(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getBioSampleAvailability(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getParentSamples(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getSubjectCountsByProductAndProject(final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getSubjectCountsByProductAndDisease(final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getBioSampleTypeCombo(final int nselectedprojectcode, final UserInfo userinfo)
			throws Exception;

}
