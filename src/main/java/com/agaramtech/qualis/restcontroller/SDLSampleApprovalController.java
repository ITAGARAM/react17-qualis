package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.ApprovalParameter;
import com.agaramtech.qualis.sdlsampleapproval.service.SDLSampleApprovalService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the Unit Service methods.
 */
@RestController
@RequestMapping(value = "/sdlsampleapproval")
public class SDLSampleApprovalController {
	
	private final SDLSampleApprovalService SDLSampleApprovalService;
	private RequestContext requestContext;	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param SDLSampleApprovalService SDLSampleApprovalService
	 */
	public SDLSampleApprovalController(RequestContext requestContext, SDLSampleApprovalService SDLSampleApprovalService) {
		super();
		this.requestContext = requestContext;
		this.SDLSampleApprovalService = SDLSampleApprovalService;
	}

	/**
	 * This method is used to retrieve list of completed and approved records. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and list of completed and approved records
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getSDLSampleApproval")
	public ResponseEntity<Object> getSDLSampleApproval(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getApproval(inputMap, userInfo);

	}
	
	/**
	 * This method is used to retrieve list of registrationtype values. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and list of registrationtype values
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getRegistrationType")
	public ResponseEntity<Map<String, Object>> getRegistrationType(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getRegistrationType(inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve list of registrationsubtype values. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and list of registrationsubtype values
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getRegistrationSubType")
	public ResponseEntity<Object> getRegistrationSubType(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getRegistrationSubType(inputMap, userInfo);

	}
	
	@PostMapping(value = "/getApproveConfigVersionRegTemplateDesign")
	public ResponseEntity<Object> getApproveConfigVersionRegTemplateDesign(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getApproveConfigVersionRegTemplateDesign(inputMap, userInfo);

	}
	
	/**
	 * This method is used to retrieve filter value ex:all,draft,approved,completed. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and filter value ex:all,draft,approved,completed.
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getFilterStatus")
	public ResponseEntity<Object> getFilterStatus(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getFilterStatus(inputMap, userInfo);

	}

	@PostMapping(value = "/getApprovalVersion")
	public ResponseEntity<Object> getApprovalVersion(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getApprovalVersion(inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve list of samples values. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and list of samples values
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getApprovalSample")
	public ResponseEntity<Object> getApprovalSample(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getApprovalSample(inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve list of sub samples values. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and list of sub samples values
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getApprovalSubSample")
	public ResponseEntity<Object> getApprovalSubSample(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getApprovalSubSample(inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve list of test values. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and list of test values
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getApprovalTest")
	public ResponseEntity<Object> getApprovalTest(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getApprovalTest(inputMap, userInfo);

	}
	/**
	 * This method is used to approve the test. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and approve the test
	 * @throws Exception exception
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = "/performAction")
	public ResponseEntity<? extends Object> performAction(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		inputMap = (Map<String, Object>) inputMap.get("performaction");
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.validateApprovalActions(inputMap, userInfo);

	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/updateDecision")
	public ResponseEntity<Object> updateDecision(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		inputMap = (Map<String, Object>) inputMap.get("updatedecision");
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.updateDecision(inputMap, userInfo);

	}

//	@RequestMapping(value="/releaseSamples",method=RequestMethod.POST)
//	public ResponseEntity<Object> releaseSamples(@RequestBody Map<String, Object> inputMap) throws Exception{
//	
//			
//			ObjectMapper objMapper = new ObjectMapper();
//			UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//			requestContext.setUserInfo(userInfo);
//			return SDLSampleApprovalService.releaseSamples(inputMap,userInfo);
//			
//		
//	}
	@SuppressWarnings("unchecked")
	@PostMapping(value = "/updateEnforceStatus")
	public ResponseEntity<Object> updateEnforceStatus(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		inputMap = (Map<String, Object>) inputMap.get("enforcestatus");
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.updateEnforceStatus(inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve result value of the test. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and retrieve result value of the test
	 * @throws Exception exception
	 */
	
	@PostMapping(value = "/getapprovalparameter")
	public ResponseEntity<Map<String, Object>> getApprovalParameter(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getApprovalParameter(ntransactionTestCode, userInfo);

	}
	
	/**
	 * This method is used to retrieve result value of the test. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and retrieve result value of the test
	 * @throws Exception exception
	 */
	
	@PostMapping(value = "/getSDLApprovalParameter")
	public ResponseEntity<Map<String, Object>> getSDLApprovalParameter(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
		final String nsitecode = (String) inputMap.get("nsitecode");
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getSDLApprovalParameter(ntransactionTestCode, userInfo, nsitecode);

	}

	@PostMapping(value = "/getStatusCombo")
	public ResponseEntity<Object> getStatusCombo(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ntransactionResultCode = (int) inputMap.get("ntransactionresultcode");
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getStatusCombo(ntransactionResultCode, userInfo);

	}

	@PostMapping(value = "/getEditParameter")
	public ResponseEntity<Object> getEditParameter(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getEditParameter(ntransactionTestCode, userInfo);

	}

	@PostMapping(value = "/updateApprovalParameter")
	public ResponseEntity<Object> updateApprovalParameter(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		List<ApprovalParameter> approvalParamList = objMapper.convertValue(inputMap.get("approvalparameter"),
				new TypeReference<List<ApprovalParameter>>() {
				});
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.updateApprovalParameter(approvalParamList, userInfo);

	}

	@PostMapping(value = "/getApprovalHistory")
	public ResponseEntity<Map<String, Object>> getApprovalHistory(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getApprovalHistory(ntransactionTestCode, userInfo);

	}

	@PostMapping(value = "/getSampleApprovalHistory")
	public ResponseEntity<Map<String, Object>> getSampleApprovalHistory(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String npreregno = (String) inputMap.get("npreregno");
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getSampleApprovalHistory(npreregno, userInfo);

	}
	
	/**
	 * This method is used to retrieve result changes history value of the test. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and retrieve result changes history value of the test
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getApprovalResultChangeHistory")
	public ResponseEntity<Map<String, Object>> getApprovalResultChangeHistory(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getApprovalResultChangeHistory(ntransactionTestCode, userInfo);

	}
	
	/**
	 * This method is used to retrieve result changes history value of the test. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and retrieve result changes history value of the test
	 * @throws Exception exception
	 */
	
	@PostMapping(value = "/getSDLApprovalResultChangeHistory")
	public ResponseEntity<Map<String, Object>> getSDLApprovalResultChangeHistory(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String ntransactionTestCode = (String) inputMap.get("ntransactiontestcode");
		final String nsitecode = (String) inputMap.get("nsitecode");
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getSDLApprovalResultChangeHistory(ntransactionTestCode, userInfo, nsitecode);

	}


	@SuppressWarnings("unchecked")
	@PostMapping(value = "/viewAttachment")
	public ResponseEntity<Object> viewAttachment(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final Map<String, Object> objMap = objMapper.convertValue(inputMap.get("selectedRecord"), Map.class);
		Map<String, Object> outputMap = SDLSampleApprovalService.viewAttachment(objMap, objUserInfo);
		requestContext.setUserInfo(objUserInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

//	@RequestMapping(value="/previewSampleReport",method=RequestMethod.POST)
//	public ResponseEntity<Object> previewSampleReport(@RequestBody Map<String, Object> inputMap) throws Exception {
//		final ObjectMapper objMapper = new ObjectMapper();
//		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		requestContext.setUserInfo(userInfo);
//		return sampleCertificateDAO.reportGeneration(inputMap);
//	}

	@PostMapping(value = "/getFilterBasedTest")
	public ResponseEntity<Object> getFilterBasedTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getFilterBasedTest(inputMap, userInfo);
	}

	@PostMapping(value = "/getEnforceCommentsHistory")
	public ResponseEntity<Object> getEnforceCommentsHistory(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getEnforceCommentsHistory(inputMap, userInfo);
	}

	@PostMapping(value = "/getCOAHistory")
	public ResponseEntity<Map<String, Object>> getCOAHistory(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String npreregno = (String) inputMap.get("npreregno");
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getCOAHistory(npreregno, userInfo);
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/viewReport")
	public ResponseEntity<Object> viewReport(@RequestBody Map<String, Object> inputMap, HttpServletResponse response)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final Map<String, Object> objMap = objMapper.convertValue(inputMap.get("selectedRecord"), Map.class);
		requestContext.setUserInfo(objUserInfo);
		return SDLSampleApprovalService.viewReport(objMap, objUserInfo);
	}

	@PostMapping(value = "/getTestBasedOnCompletedBatch")
	public ResponseEntity<Object> getTestBasedOnCompletedBatch(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getTestBasedOnCompletedBatch(inputMap, userInfo);
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/checkReleaseRecord")
	public ResponseEntity<Map<String, Object>> checkReleaseRecord(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		inputMap = (Map<String, Object>) inputMap.get("performaction");
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.checkReleaseRecord(inputMap, userInfo);

	}

	@PostMapping(value = "/getSampleViewDetails")
	public ResponseEntity<Object> getSampleViewDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getSampleViewDetails(inputMap);
	}

	@PostMapping(value = "/getTestResultCorrection")
	public ResponseEntity<Map<String, Object>> getTestResultCorrection(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getTestResultCorrection(inputMap, userInfo);

	}

	@PostMapping(value = "/updateReleaseParameter")
	public ResponseEntity<Map<String, Object>> updateReleaseParameter(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.updateReleaseParameter(request, userInfo);
	}
	
	@PostMapping(value = "/getReleaseResults")
	public ResponseEntity<Object> getReleaseResults(@RequestBody Map<String, Object> inputMap) throws Exception {
		// try {
		final ObjectMapper objmapper = new ObjectMapper();
		int ntransactionresultcode = (int) inputMap.get("ntransactionresultcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return (ResponseEntity<Object>) SDLSampleApprovalService.getReleaseResults(ntransactionresultcode, userInfo);

	}

	@PostMapping(value = "/createFilterName")
	public ResponseEntity<Object> createFilterName(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.createFilterName(inputMap, userInfo);
	}

	
	@PostMapping(value = "/getTestApprovalFilterDetails")
	public ResponseEntity<? extends Object> getTestApprovalFilterDetails(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return SDLSampleApprovalService.getTestApprovalFilterDetails(inputMap, userInfo);
	}

}