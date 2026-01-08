package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.biobank.service.bgsiecatalogue.BGSiECatalogueService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/bgsiecatalogue")
public class BGSiECatalogueController {
	private static final Logger LOGGER = LoggerFactory.getLogger(BGSiECatalogueController.class);

	private RequestContext requestContext;
	private final BGSiECatalogueService objBGSiECatalogueService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext           RequestContext to hold the request
	 * @param objBGSiECatalogueService BGSiECatalogueService
	 */

	public BGSiECatalogueController(RequestContext requestContext, BGSiECatalogueService objBGSiECatalogueService) {
		this.requestContext = requestContext;
		this.objBGSiECatalogueService = objBGSiECatalogueService;
	}

	@PostMapping(value = "/getBGSiECatalogue")
	public ResponseEntity<Object> getBGSiECatalogue(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBGSiECatalogueService.getBGSiECatalogue(inputMap, userInfo);
	}

	@PostMapping(value = "/getBGSiECatalogueByFilterSubmit")
	public ResponseEntity<Object> getBGSiECatalogueByFilterSubmit(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBGSiECatalogueService.getBGSiECatalogueByFilterSubmit(inputMap, userInfo);
	}

	@PostMapping(value = "/getComboDataForCatalogue")
	public ResponseEntity<Object> getComboDataForCatalogue(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBGSiECatalogueService.getComboDataForCatalogue(inputMap, userInfo);
	}

	@PostMapping(value = "/getSiteComboForProject")
	public ResponseEntity<Object> getSiteComboForProject(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBGSiECatalogueService.getSiteComboForProject(inputMap, userInfo);
	}

	@PostMapping(value = "/getActiveBGSiECatalogueRequestForm")
	public ResponseEntity<Map<String, Object>> getActiveSampleCgetActiveBGSiECatalogueRequestFormollection(
			@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBGSiECatalogueService.getActiveBGSiECatalogueRequestForm(inputMap, userInfo);
	}

	@PostMapping(value = "/getAggregatedDataForCatalogue")
	public ResponseEntity<Object> getAggregatedDataForCatalogue(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBGSiECatalogueService.getAggregatedDataForCatalogue(inputMap, userInfo);
	}

	@PostMapping(value = "/getProductComboDataForSampleAdd")
	public ResponseEntity<Object> getProductComboDataForSampleAdd(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBGSiECatalogueService.getProductComboDataForSampleAdd(inputMap, userInfo);
	}

	@PostMapping(value = "/getDetailedDataForCatalogue")
	public ResponseEntity<Object> getDetailedDataForCatalogue(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBGSiECatalogueService.getDetailedDataForCatalogue(inputMap, userInfo);
	}

	@PostMapping(value = "/createBGSiECatalogueRequest")
	public ResponseEntity<Object> createBGSiECatalogueRequest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBGSiECatalogueService.createBGSiECatalogueRequest(inputMap, userInfo);
	}

	@PostMapping(value = "/sendBGSiECatalogueRequest")
	public ResponseEntity<Object> sendBGSiECatalogueRequest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBGSiECatalogueService.sendBGSiECatalogueRequest(inputMap, userInfo);
	}

	@PostMapping(value = "/cancelBGSiECatalogueRequest")
	public ResponseEntity<Object> cancelBGSiECatalogueRequest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBGSiECatalogueService.cancelBGSiECatalogueRequest(inputMap, userInfo);
	}

	@PostMapping(value = "/createBGSiECatalogueRequestSample")
	public ResponseEntity<Object> createBGSiECatalogueRequestSample(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBGSiECatalogueService.createBGSiECatalogueRequestSample(inputMap, userInfo);
	}

	@PostMapping(value = "/updateBGSiECatalogueRequestSample")
	public ResponseEntity<Object> updateBGSiECatalogueRequestSample(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBGSiECatalogueService.updateBGSiECatalogueRequestSample(inputMap, userInfo);
	}

	@PostMapping(value = "/deleteBGSiECatalogueRequestSample")
	public ResponseEntity<Object> deleteBGSiECatalogueRequestSample(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBGSiECatalogueService.deleteBGSiECatalogueRequestSample(inputMap, userInfo);
	}

	@PostMapping(value = "/getActiveSampleDetail")
	public ResponseEntity<Map<String, Object>> getActiveSampleDetail(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBGSiECatalogueService.getActiveSampleDetail(inputMap, userInfo);
	}

	@PostMapping(value = "/getBioSampleAvailability")
	public ResponseEntity<Object> getBioSampleAvailability(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return objBGSiECatalogueService.getBioSampleAvailability(inputMap, userinfo);
	}

	@PostMapping(value = "/getParentSamples")
	public ResponseEntity<Object> getParentSamples(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return objBGSiECatalogueService.getParentSamples(inputMap, userinfo);
	}
	
	@PostMapping("/getSubjectCountsByProductAndProject")
	public ResponseEntity<Object> getSubjectCountsByProductAndProject(@RequestBody Map<String, Object> input)
	        throws Exception {
	    final ObjectMapper objMapper = new ObjectMapper();
	    final UserInfo userinfo = objMapper.convertValue(input.get("userinfo"), UserInfo.class);
	    requestContext.setUserInfo(userinfo);
	    return objBGSiECatalogueService.getSubjectCountsByProductAndProject(userinfo);
	}
	@PostMapping("/getSubjectCountsByProductAndDisease")
	public ResponseEntity<Object> getSubjectCountsByProductAndDisease(@RequestBody Map<String, Object> input)
	        throws Exception {
	    final ObjectMapper objMapper = new ObjectMapper();
	    final UserInfo userinfo = objMapper.convertValue(input.get("userinfo"), UserInfo.class);
	    requestContext.setUserInfo(userinfo);
	    return objBGSiECatalogueService.getSubjectCountsByProductAndDisease(userinfo);
	}

	@PostMapping("/getBioSampleTypeCombo")
	public ResponseEntity<Object> getBioSampleTypeCombo(@RequestBody Map<String, Object> inputMap)
	        throws Exception {
	    final ObjectMapper objMapper = new ObjectMapper();
	    final UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
	    requestContext.setUserInfo(userinfo);
	    final int nselectedsitecode = Integer.valueOf(inputMap.get("nselectedsitecode").toString());
		final int nselectedprojectcode = Integer.valueOf(inputMap.get("nselectedprojectcode").toString());
	    return objBGSiECatalogueService.getBioSampleTypeCombo(nselectedprojectcode,nselectedsitecode, userinfo);
	}
}
