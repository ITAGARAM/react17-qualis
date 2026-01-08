package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.biobank.service.biobankecatalogueexternal.BiobankECatalogueExternalService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/biobankecatalogueexternal")
public class BioBankECatalogueExternalController {
	private static final Logger LOGGER = LoggerFactory.getLogger(BioBankECatalogueExternalController.class);

	private RequestContext requestContext;
	private final BiobankECatalogueExternalService objBiobankECatalogueExternalService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext           RequestContext to hold the request
	 * @param objBiobankECatalogueExternalService BiobankECatalogueExternalService
	 */

	public BioBankECatalogueExternalController(RequestContext requestContext, BiobankECatalogueExternalService objBiobankECatalogueExternalService) {
		this.requestContext = requestContext;
		this.objBiobankECatalogueExternalService = objBiobankECatalogueExternalService;
	}

	@PostMapping(value = "/getBiobankECatalogueExternal")
	public ResponseEntity<Object> getBiobankECatalogueExternal(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBiobankECatalogueExternalService.getBiobankECatalogueExternal(inputMap, userInfo);
	}

	@PostMapping(value = "/getBiobankECatalogueExternalByFilterSubmit")
	public ResponseEntity<Object> getBiobankECatalogueExternalByFilterSubmit(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBiobankECatalogueExternalService.getBiobankECatalogueExternalByFilterSubmit(inputMap, userInfo);
	}

	@PostMapping(value = "/getComboDataForCatalogue")
	public ResponseEntity<Object> getComboDataForCatalogue(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBiobankECatalogueExternalService.getComboDataForCatalogue(inputMap, userInfo);
	}
	
	@PostMapping(value = "/getComboDataForCatalogueForAdd")
	public ResponseEntity<Object> getComboDataForCatalogueForAdd(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBiobankECatalogueExternalService.getComboDataForCatalogueForAdd(inputMap, userInfo);
	}

	@PostMapping(value = "/getSiteComboForProject")
	public ResponseEntity<Object> getSiteComboForProject(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBiobankECatalogueExternalService.getSiteComboForProject(inputMap, userInfo);
	}

	@PostMapping(value = "/getActiveBiobankECatalogueExternalRequestForm")
	public ResponseEntity<Map<String, Object>> getActiveBiobankECatalogueExternalRequestForm(
			@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBiobankECatalogueExternalService.getActiveBiobankECatalogueExternalRequestForm(inputMap, userInfo);
	}

	@PostMapping(value = "/getAggregatedDataForCatalogue")
	public ResponseEntity<Object> getAggregatedDataForCatalogue(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBiobankECatalogueExternalService.getAggregatedDataForCatalogue(inputMap, userInfo);
	}

	@PostMapping(value = "/getProductComboDataForSampleAdd")
	public ResponseEntity<Object> getProductComboDataForSampleAdd(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBiobankECatalogueExternalService.getProductComboDataForSampleAdd(inputMap, userInfo);
	}

	@PostMapping(value = "/getDetailedDataForCatalogue")
	public ResponseEntity<Object> getDetailedDataForCatalogue(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBiobankECatalogueExternalService.getDetailedDataForCatalogue(inputMap, userInfo);
	}

	@PostMapping(value = "/createBiobankECatalogueExternalRequest")
	public ResponseEntity<Object> createBiobankECatalogueExternalRequest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBiobankECatalogueExternalService.createBiobankECatalogueExternalRequest(inputMap, userInfo);
	}

	@PostMapping(value = "/sendBiobankECatalogueExternalRequest")
	public ResponseEntity<Object> sendBiobankECatalogueExternalRequest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBiobankECatalogueExternalService.sendBiobankECatalogueExternalRequest(inputMap, userInfo);
	}

	@PostMapping(value = "/cancelBiobankECatalogueExternalRequest")
	public ResponseEntity<Object> cancelBiobankECatalogueExternalRequest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBiobankECatalogueExternalService.cancelBiobankECatalogueExternalRequest(inputMap, userInfo);
	}

	@PostMapping(value = "/createBiobankECatalogueExternalRequestSample")
	public ResponseEntity<Object> createBiobankECatalogueExternalRequestSample(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBiobankECatalogueExternalService.createBiobankECatalogueExternalRequestSample(inputMap, userInfo);
	}

	@PostMapping(value = "/updateBiobankECatalogueExternalRequestSample")
	public ResponseEntity<Object> updateBiobankECatalogueExternalRequestSample(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBiobankECatalogueExternalService.updateBiobankECatalogueExternalRequestSample(inputMap, userInfo);
	}

	@PostMapping(value = "/deleteBiobankECatalogueExternalRequestSample")
	public ResponseEntity<Object> deleteBiobankECatalogueExternalRequestSample(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBiobankECatalogueExternalService.deleteBiobankECatalogueExternalRequestSample(inputMap, userInfo);
	}

	@PostMapping(value = "/getActiveSampleDetail")
	public ResponseEntity<Map<String, Object>> getActiveSampleDetail(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBiobankECatalogueExternalService.getActiveSampleDetail(inputMap, userInfo);
	}

	@PostMapping(value = "/getBioSampleAvailability")
	public ResponseEntity<Object> getBioSampleAvailability(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return objBiobankECatalogueExternalService.getBioSampleAvailability(inputMap, userinfo);
	}

	@PostMapping(value = "/getParentSamples")
	public ResponseEntity<Object> getParentSamples(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return objBiobankECatalogueExternalService.getParentSamples(inputMap, userinfo);
	}
	
	@PostMapping("/getSubjectCountsByProductAndProject")
	public ResponseEntity<Object> getSubjectCountsByProductAndProject(@RequestBody Map<String, Object> input)
	        throws Exception {
	    final ObjectMapper objMapper = new ObjectMapper();
	    final UserInfo userinfo = objMapper.convertValue(input.get("userinfo"), UserInfo.class);
	    requestContext.setUserInfo(userinfo);
	    return objBiobankECatalogueExternalService.getSubjectCountsByProductAndProject(userinfo);
	}
	@PostMapping("/getSubjectCountsByProductAndDisease")
	public ResponseEntity<Object> getSubjectCountsByProductAndDisease(@RequestBody Map<String, Object> input)
	        throws Exception {
	    final ObjectMapper objMapper = new ObjectMapper();
	    final UserInfo userinfo = objMapper.convertValue(input.get("userinfo"), UserInfo.class);
	    requestContext.setUserInfo(userinfo);
	    return objBiobankECatalogueExternalService.getSubjectCountsByProductAndDisease(userinfo);
	}

	@PostMapping("/getBioSampleTypeCombo")
	public ResponseEntity<Object> getBioSampleTypeCombo(@RequestBody Map<String, Object> inputMap)
	        throws Exception {
	    final ObjectMapper objMapper = new ObjectMapper();
	    final UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	    requestContext.setUserInfo(userinfo);
	    final int nselectedsitecode = Integer.valueOf(inputMap.get("nselectedsitecode").toString());
		final int nselectedprojectcode = Integer.valueOf(inputMap.get("nselectedprojectcode").toString());
	    return objBiobankECatalogueExternalService.getBioSampleTypeCombo(nselectedprojectcode,nselectedsitecode, userinfo);
	}
}
