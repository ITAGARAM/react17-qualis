package com.agaramtech.qualis.biobank.service.biobankecatalogueexternal;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

public interface BiobankECatalogueExternalService {

	ResponseEntity<Object> getBiobankECatalogueExternal(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getComboDataForCatalogue(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	
	ResponseEntity<Object> getComboDataForCatalogueForAdd(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getAggregatedDataForCatalogue(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getDetailedDataForCatalogue(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> createBiobankECatalogueExternalRequest(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getBiobankECatalogueExternalByFilterSubmit(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Map<String, Object>> getActiveBiobankECatalogueExternalRequestForm(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getSiteComboForProject(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> sendBiobankECatalogueExternalRequest(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> cancelBiobankECatalogueExternalRequest(final Map<String, Object> inputMap,final  UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getProductComboDataForSampleAdd(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> createBiobankECatalogueExternalRequestSample(final Map<String, Object> inputMap,final  UserInfo userInfo) throws Exception;

	ResponseEntity<Object> updateBiobankECatalogueExternalRequestSample(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> deleteBiobankECatalogueExternalRequestSample(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Map<String, Object>> getActiveSampleDetail(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getBioSampleAvailability(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getParentSamples(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getSubjectCountsByProductAndProject(final UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getSubjectCountsByProductAndDisease(final  UserInfo userInfo) throws Exception;

	ResponseEntity<Object> getBioSampleTypeCombo(final int nselectedprojectcode, final int nselectedsitecode,final UserInfo userinfo) throws Exception;


}
