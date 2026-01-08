package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.biobank.service.bgsiexternalapi.BGSIExternalApiService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/bgsiexternalapi")
public class BioBGSIExternalApiController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BioBGSIExternalApiController.class);

	private RequestContext requestContext;
	private final BGSIExternalApiService objBGSIExternalApiService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext            RequestContext to hold the request
	 * @param objBGSIExternalApiService BGSIExternalApiService
	 */

	public BioBGSIExternalApiController(RequestContext requestContext,
			BGSIExternalApiService objBGSIExternalApiService) {
		this.requestContext = requestContext;
		this.objBGSIExternalApiService = objBGSIExternalApiService;
	}

	@PostMapping(value = "/v1/updatepatientconsent")
	public ResponseEntity<Object> updatepatientconsent(@RequestBody Map<String, Object> inputMap) throws Exception {
		UserInfo objUserInfo = new UserInfo();

		objUserInfo.setSsitename("LIMS");
		objUserInfo.setSmodulename("Bio Bank Management");
		objUserInfo.setSusername("System");
		objUserInfo.setSuserrolename("Admin");
		objUserInfo.setSformname("Update Patient Consent");
		objUserInfo.setNsitecode((short) -1);
		objUserInfo.setIsutcenabled((int) 4);
		requestContext.setUserInfo(objUserInfo);

		return objBGSIExternalApiService.updatepatientconsent(inputMap, objUserInfo);
	}

	@PostMapping(value = "/v1/bioexternaltransferdata")
	public ResponseEntity<Object> bioExternalTransferData(@RequestBody Map<String, Object> inputMap) throws Exception {
		UserInfo objUserInfo = new UserInfo();
		return objBGSIExternalApiService.bioExternalTransferData(objUserInfo);
	}

	@PostMapping(value = "/v1/acknowledgetransferred")
	public ResponseEntity<Object> acknowledgeTransferred(@RequestBody Map<String, Object> inputMap) throws Exception {
		UserInfo objUserInfo = new UserInfo();
		return objBGSIExternalApiService.acknowledgeTransferred(inputMap, objUserInfo);
	}

	@PostMapping(value = "/v1/receiveoriginalsample")
	public ResponseEntity<Object> receiveOriginalSample(@RequestBody Map<String, Object> inputMap) throws Exception {
		UserInfo objUserInfo = new UserInfo();
		objUserInfo.setNmastersitecode((short) -1);
		objUserInfo.setNsitecode((short) -1);
		objUserInfo.setIsutcenabled((int) 4);
		objUserInfo.setNusercode(-1);
		objUserInfo.setNuserrole(-1);
		objUserInfo.setNdeputyusercode(-1);
		objUserInfo.setNdeputyuserrole(-1);
		return objBGSIExternalApiService.receiveOriginalSample(inputMap, objUserInfo);
	}

	@PostMapping(value = "/v1/getauthorisationtokenforngs")
	public String getAuthorisationTokenForNGS(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userinfo);
		return objBGSIExternalApiService.getAuthorisationTokenForNGS(userinfo);
	}

	@PostMapping(value = "/v1/sendacceptedthirdpartysamplestongs")
	public ResponseEntity<Object> sendAcceptedThirdPartySamplestoNGS(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userinfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userinfo);
		return objBGSIExternalApiService.sendAcceptedThirdPartySamplestoNGS(inputMap, userinfo);
	}

	@PostMapping(value = "/v1/gettotalrepoidbasedsampletype")
	public ResponseEntity<Object> getTotalRepoIdBasedSampleType(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		return objBGSIExternalApiService.getTotalRepoIdBasedSampleType(inputMap);
	}

	@PostMapping(value = "/v1/gettotalrepoidrequestedandsent")
	public ResponseEntity<Object> getTotalRepoIdRequestedAndSent(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		return objBGSIExternalApiService.getTotalRepoIdRequestedAndSent(inputMap);
	}

	@PostMapping("/v1/gettotalsubjectid")
	public ResponseEntity<Object> getTotalSubjectid(@RequestBody Map<String, Object> inputMap) throws Exception {
		return objBGSIExternalApiService.getTotalSubjectid(inputMap);
	}

	@PostMapping(value = "/v1/getstoragecapacity")
	public ResponseEntity<Object> getStorageCapacity(@RequestBody Map<String, Object> inputMap) throws Exception {
		return objBGSIExternalApiService.getStorageCapacity(inputMap);
	}

	@PostMapping(value = "/v1/receivednasample")
	public ResponseEntity<Object> receiveDNASample(@RequestBody Map<String, Object> inputMap) throws Exception {
		UserInfo objUserInfo = new UserInfo();
		objUserInfo.setNmastersitecode((short) -1);
		objUserInfo.setNsitecode((short) -1);
		objUserInfo.setIsutcenabled((int) 4);
		objUserInfo.setNusercode(-1);
		objUserInfo.setNuserrole(-1);
		objUserInfo.setNdeputyusercode(-1);
		objUserInfo.setNdeputyuserrole(-1);
		return objBGSIExternalApiService.receiveDNASample(inputMap, objUserInfo);
	}
}
