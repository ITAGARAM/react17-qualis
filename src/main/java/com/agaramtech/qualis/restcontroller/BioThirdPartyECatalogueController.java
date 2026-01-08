package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.biobank.service.biothirdpartyecatalogue.BioThirdPartyECatalogueService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/biothirdpartyecatalogue")
public class BioThirdPartyECatalogueController {
	private static final Logger LOGGER = LoggerFactory.getLogger(BioThirdPartyECatalogueController.class);

	private RequestContext requestContext;
	private final BioThirdPartyECatalogueService objBioThirdPartyECatalogueService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext           RequestContext to hold the request
	 * @param objBGSiECatalogueService BGSiECatalogueService
	 */

	public BioThirdPartyECatalogueController(RequestContext requestContext,
			BioThirdPartyECatalogueService objBioThirdPartyECatalogueService) {
		this.requestContext = requestContext;
		this.objBioThirdPartyECatalogueService = objBioThirdPartyECatalogueService;
	}

	@PostMapping(value = "/getBioThirdPartyECatalogue")
	public ResponseEntity<Object> getBioThirdPartyECatalogue(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBioThirdPartyECatalogueService.getBioThirdPartyECatalogue(inputMap, userInfo);
	}

	@PostMapping(value = "/getThirdPartyECatalogueByFilterSubmit")
	public ResponseEntity<Object> getThirdPartyECatalogueByFilterSubmit(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		LOGGER.info("getThirdPartyECatalogueByFilterSubmit...");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBioThirdPartyECatalogueService.getThirdPartyECatalogueByFilterSubmit(inputMap, userInfo);
	}

	@PostMapping(value = "/getComboDataForCatalogue")
	public ResponseEntity<Object> getComboDataForCatalogue(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBioThirdPartyECatalogueService.getComboDataForCatalogue(inputMap, userInfo);
	}

	@PostMapping(value = "/getSiteComboForProject")
	public ResponseEntity<Object> getSiteComboForProject(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBioThirdPartyECatalogueService.getSiteComboForProject(inputMap, userInfo);
	}

	@PostMapping(value = "/getActiveThirdPartyECatalogueRequestForm")
	public ResponseEntity<Map<String, Object>> getActiveThirdPartyECatalogueRequestForm(
			@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBioThirdPartyECatalogueService.getActiveThirdPartyECatalogueRequestForm(inputMap, userInfo);
	}

	@PostMapping(value = "/getAggregatedDataForCatalogue")
	public ResponseEntity<Object> getAggregatedDataForCatalogue(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBioThirdPartyECatalogueService.getAggregatedDataForCatalogue(inputMap, userInfo);
	}

	@PostMapping(value = "/getProductComboDataForSampleAdd")
	public ResponseEntity<Object> getProductComboDataForSampleAdd(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBioThirdPartyECatalogueService.getProductComboDataForSampleAdd(inputMap, userInfo);
	}

	@PostMapping(value = "/getDetailedDataForCatalogue")
	public ResponseEntity<Object> getDetailedDataForCatalogue(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBioThirdPartyECatalogueService.getDetailedDataForCatalogue(inputMap, userInfo);
	}

	@PostMapping(value = "/createThirdPartyECatalogueRequest")
	public ResponseEntity<Object> createThirdPartyECatalogueRequest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBioThirdPartyECatalogueService.createThirdPartyECatalogueRequest(inputMap, userInfo);
	}

	@PostMapping(value = "/sendThirdPartyECatalogueRequest")
	public ResponseEntity<Object> sendThirdPartyECatalogueRequest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBioThirdPartyECatalogueService.sendThirdPartyECatalogueRequest(inputMap, userInfo);
	}

	@PostMapping(value = "/cancelThirdPartyECatalogueRequest")
	public ResponseEntity<Object> cancelThirdPartyECatalogueRequest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBioThirdPartyECatalogueService.cancelThirdPartyECatalogueRequest(inputMap, userInfo);
	}

	@PostMapping(value = "/createThirdPartyECatalogueRequestSample")
	public ResponseEntity<Object> createThirdPartyECatalogueRequestSample(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBioThirdPartyECatalogueService.createThirdPartyECatalogueRequestSample(inputMap, userInfo);
	}

	@PostMapping(value = "/updateThirdPartyECatalogueRequestSample")
	public ResponseEntity<Object> updateThirdPartyECatalogueRequestSample(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBioThirdPartyECatalogueService.updateThirdPartyECatalogueRequestSample(inputMap, userInfo);
	}

	@PostMapping(value = "/deleteThirdPartyECatalogueRequestSample")
	public ResponseEntity<Object> deleteThirdPartyECatalogueRequestSample(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return objBioThirdPartyECatalogueService.deleteThirdPartyECatalogueRequestSample(inputMap, userInfo);
	}

	@PostMapping(value = "/getActiveSampleDetail")
	public ResponseEntity<Map<String, Object>> getActiveSampleDetail(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return objBioThirdPartyECatalogueService.getActiveSampleDetail(inputMap, userInfo);
	}

	@PostMapping(value = "/getBioSampleAvailability")
	public ResponseEntity<Object> getBioSampleAvailability(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return objBioThirdPartyECatalogueService.getBioSampleAvailability(inputMap, userinfo);
	}

	@PostMapping(value = "/getParentSamples")
	public ResponseEntity<Object> getParentSamples(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		return objBioThirdPartyECatalogueService.getParentSamples(inputMap, userinfo);
	}

	@PostMapping("/getSubjectCountsByProductAndProject")
	public ResponseEntity<Object> getSubjectCountsByProductAndProject(@RequestBody Map<String, Object> input)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userinfo = objMapper.convertValue(input.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userinfo);
		return objBioThirdPartyECatalogueService.getSubjectCountsByProductAndProject(userinfo);
	}

	@PostMapping("/getSubjectCountsByProductAndDisease")
	public ResponseEntity<Object> getSubjectCountsByProductAndDisease(@RequestBody Map<String, Object> input)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userinfo = objMapper.convertValue(input.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userinfo);
		return objBioThirdPartyECatalogueService.getSubjectCountsByProductAndDisease(userinfo);
	}

	@PostMapping("/getBioSampleTypeCombo")
	public ResponseEntity<Object> getBioSampleTypeCombo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userinfo);
		final int nselectedprojectcode = Integer.valueOf(inputMap.get("nselectedprojectcode").toString());
		return objBioThirdPartyECatalogueService.getBioSampleTypeCombo(nselectedprojectcode, userinfo);
	}

}
