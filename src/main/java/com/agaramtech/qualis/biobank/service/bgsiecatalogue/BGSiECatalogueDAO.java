package com.agaramtech.qualis.biobank.service.bgsiecatalogue;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface BGSiECatalogueDAO {

	ResponseEntity<Object> getBGSiECatalogue(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getComboDataForCatalogue(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getAggregatedDataForCatalogue(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getDetailedDataForCatalogue(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> createBGSiECatalogueRequest(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getBGSiECatalogueByFilterSubmit(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Map<String, Object>> getActiveBGSiECatalogueRequestForm(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getSiteComboForProject(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> sendBGSiECatalogueRequest(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> cancelBGSiECatalogueRequest(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getProductComboDataForSampleAdd(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> createBGSiECatalogueRequestSample(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> updateBGSiECatalogueRequestSample(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> deleteBGSiECatalogueRequestSample(final Map<String, Object> inputMap,final  UserInfo userInfo) throws Exception;

	ResponseEntity<Map<String, Object>> getActiveSampleDetail(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getBioSampleAvailability(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getParentSamples(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getSubjectCountsByProductAndProject(final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getSubjectCountsByProductAndDisease(final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getBioSampleTypeCombo(final int nselectedprojectcode, final int nselectedsitecode, final UserInfo userinfo) throws Exception;


}
