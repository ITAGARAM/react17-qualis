package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.biobank.service.cateloguedatarequestandapproval.CatelogueDataRequestandApprovalService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/biobankrequest")
public class CatelogueDataRequestandApprovalController {
	private static final Logger LOGGER = LoggerFactory.getLogger(BioParentSampleReceivingController.class);

	private RequestContext requestContext;
	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext           RequestContext to hold the request
	 * @param objBGSiECatalogueService BGSiECatalogueService
	 */
	private final CatelogueDataRequestandApprovalService catelogueDataRequestandApprovalService;

	public CatelogueDataRequestandApprovalController(RequestContext requestContext,
			CatelogueDataRequestandApprovalService catelogueDataRequestandApprovalService) {
		this.requestContext = requestContext;
		this.catelogueDataRequestandApprovalService = catelogueDataRequestandApprovalService;
	}

	/**
	 * This method is used to retrieve list of BioBank requests.
	 * 
	 * @param inputMap [Map] contains userInfo [UserInfo] holding logged-in user
	 *                 details and optional filters like fromDate, toDate,
	 *                 ntransCode Input: {"userinfo":{...}, "fromDate":"yyyy-MM-dd",
	 *                 "toDate":"yyyy-MM-dd"}
	 * @return ResponseEntity<Object> containing status and list of BioBank requests
	 * @throws Exception if any error occurs
	 */

	@PostMapping(value = "/getBioBankRequest")
	public ResponseEntity<Object> getBioDataRequest(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbioDirectTransferCode = -1;
		LOGGER.info("getBioDataRequest");
		return catelogueDataRequestandApprovalService.getBioDataRequest(inputMap, nbioDirectTransferCode, userInfo);
	}

	/**
	 * This method is used to fetch sample type data linked to a BioData request.
	 *
	 * @param inputMap [Map] contains userInfo [UserInfo] and BioData request
	 *                 details
	 * @return ResponseEntity<Object> containing sample type data
	 * @throws Exception if any error occurs
	 */
	@PostMapping(value = "/getBioDataRequestSampleTypeData")
	public ResponseEntity<Object> getBioDataRequestSampleTypeData(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getBioDataRequestSampleTypeData");
		return catelogueDataRequestandApprovalService.getBioDataRequestSampleTypeData(inputMap, userInfo);
	}

	/**
	 * This method retrieves child sample type data for a specific BioData request.
	 *
	 * @param inputMap [Map] contains userInfo [UserInfo] and child request details
	 * @return ResponseEntity<Object> with child sample type data
	 * @throws Exception if any error occurs
	 */
	@PostMapping(value = "/getChildBioDataRequestSampleTypeData")
	public ResponseEntity<Object> getChildBioDataRequestSampleTypeData(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getBioDataRequestSampleTypeData");
		return catelogueDataRequestandApprovalService.getChildBioDataRequestSampleTypeData(inputMap, userInfo);
	}

	/**
	 * This method creates a new BioData request.
	 *
	 * @param inputMap [Map] contains userInfo [UserInfo], bioprojectcode,
	 *                 sampleTypes, and request date
	 * @return ResponseEntity<Object> containing created BioData request details
	 * @throws Exception if any error occurs
	 */
	@PostMapping(value = "/createBioDataRequest")
	public ResponseEntity<Object> createBioDataRequest(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return catelogueDataRequestandApprovalService.createBioDataRequest(inputMap, userInfo);
	}

	/**
	 * This method creates a child BioData request for a given parent request.
	 *
	 * @param inputMap [Map] contains userInfo [UserInfo], parent BioData request,
	 *                 and sample details
	 * @return ResponseEntity<Object> containing created child BioData request
	 *         details
	 * @throws Exception if any error occurs
	 */

	@PostMapping(value = "/createChildBioDataRequest")
	public ResponseEntity<Object> createChildBioDataRequest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return catelogueDataRequestandApprovalService.createChildBioDataRequest(inputMap, userInfo);
	}

	/**
	 * This method retrieves list of BioProjects for the logged-in user.
	 *
	 * @param inputMap [Map] contains userInfo [UserInfo]
	 * @return ResponseEntity<Object> containing available BioProjects
	 * @throws Exception if any error occurs
	 */
	@PostMapping(value = "/getBioProject")
	public ResponseEntity<Object> getBioProject(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return catelogueDataRequestandApprovalService.getBioProject(userInfo);
	}

	/**
	 * This method retrieves active BioDataAccess records.
	 *
	 * @param inputMap [Map] contains userInfo [UserInfo] and
	 *                 nbiodataaccessrequestcode
	 * @return ResponseEntity<Object> containing active BioDataAccess details
	 * @throws Exception if any error occurs
	 */
	@PostMapping(value = "/getActiveBioDataAccess")
	public ResponseEntity<Object> getActiveBioDataAccess(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbiodataAccessrequestcode = Integer.valueOf(inputMap.get("nbiodataaccessrequestcode").toString());
		return catelogueDataRequestandApprovalService.getActiveBioDataAccess(nbiodataAccessrequestcode, userInfo);
	}

	/**
	 * This method sends request a third-party ECatalogue request for approval.
	 *
	 * @param inputMap [Map] contains userInfo [UserInfo] and BioData request
	 *                 details
	 * @return ResponseEntity<Object> with updated request status
	 * @throws Exception if any error occurs
	 */
	@PostMapping(value = "/sendThirdpartyECatalogueRequest")
	public ResponseEntity<Object> sendThirdpartyECatalogueRequest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return catelogueDataRequestandApprovalService.sendThirdpartyECatalogueRequest(inputMap, userInfo);
	}

	/**
	 * This method cancels a third-party ECatalogue request.
	 *
	 * @param inputMap [Map] contains userInfo [UserInfo] and BioData request
	 *                 details
	 * @return ResponseEntity<Object> with cancellation result
	 * @throws Exception if any error occurs
	 */
	@PostMapping(value = "/cancelThirdpartyECatalogueRequest")
	public ResponseEntity<Object> cancelThirdpartyECatalogueRequest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return catelogueDataRequestandApprovalService.cancelThirdpartyECatalogueRequest(inputMap, userInfo);
	}

	/**
	 * This method approves a third-party ECatalogue request.
	 *
	 * @param inputMap [Map] contains userInfo [UserInfo] and BioData request
	 *                 details
	 * @return ResponseEntity<Object> with approval result
	 * @throws Exception if any error occurs
	 */
	@PostMapping(value = "/approveThirdpartyECatalogueRequest")
	public ResponseEntity<Object> approveThirdpartyECatalogueRequest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return catelogueDataRequestandApprovalService.approveThirdpartyECatalogueRequest(inputMap, userInfo);
	}

	/**
	 * This method rejects a third-party ECatalogue request.
	 *
	 * @param inputMap [Map] contains userInfo [UserInfo] and BioData request
	 *                 details
	 * @return ResponseEntity<Object> with rejection result
	 * @throws Exception if any error occurs
	 */
	@PostMapping(value = "/rejectThirdECatalogueRequest")
	public ResponseEntity<Object> rejectThirdECatalogueRequest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return catelogueDataRequestandApprovalService.rejectThirdECatalogueRequest(inputMap, userInfo);
	}

	/**
	 * This method retire a third-party ECatalogue request.
	 *
	 * @param inputMap [Map] contains userInfo [UserInfo] and BioData request
	 *                 details
	 * @return ResponseEntity<Object> with retire result
	 * @throws Exception if any error occurs
	 */
	@PostMapping(value = "/retireThirdECatalogueRequest")
	public ResponseEntity<Object> retireThirdECatalogueRequest(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return catelogueDataRequestandApprovalService.retireThirdECatalogueRequest(inputMap, userInfo);
	}

	/**
	 * This method retrieves the current status of a BioDataAccess request.
	 *
	 * @param inputMap [Map] contains userInfo [UserInfo] and
	 *                 nbiodataaccessrequestcode
	 * @return int representing current request status
	 * @throws Exception if any error occurs
	 */
	@PostMapping(value = "/findStatusrequest")
	public int findStatusrequest(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		final int nbiodataAccessrequestcode = (int) inputMap.get("nbiodataaccessrequestcode");
		return catelogueDataRequestandApprovalService.findStatusrequest(nbiodataAccessrequestcode, userInfo);
	}

	/**
	 * Utility method to recursively search for a value in a nested map structure
	 * based on a given key.
	 *
	 * @param map       [Map] input map which may contain nested maps
	 * @param searchKey [String] the key to search for
	 * @return Object the value corresponding to the searchKey if found, otherwise
	 *         null
	 */
	@SuppressWarnings("unchecked")
	public static Object getValueByKey(Map<String, Object> map, String searchKey) {
		if (map == null || searchKey == null) {
			return null;
		}

		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			if (searchKey.equals(key)) {
				return value;
			}

			if (value instanceof Map) {
				Object nestedResult = getValueByKey((Map<String, Object>) value, searchKey);
				if (nestedResult != null) {
					return nestedResult;
				}
			}
		}
		return null;
	}

	/**
	 * This method is used to delete child BioData request(s) linked to a parent
	 * BioData request. It extracts the parent BioData request code and child
	 * request detail codes from the input map, retrieves the logged-in user
	 * details, and passes them to the service layer for deletion.
	 *
	 * @param inputMap [Map] input data containing: - userinfo [UserInfo]: logged-in
	 *                 user details - nbiodataaccessrequestcode [int]: parent
	 *                 BioData request code - nbiodataaccessrequestdetailscode
	 *                 [String]: comma-separated child request detail codes
	 * @return ResponseEntity<Object> response object indicating the success/failure
	 *         of delete operation
	 * @throws Exception if any error occurs during processing
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = "/deleteChildBioRequest")
	public ResponseEntity<Object> deleteChildBioRequest(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Object requestCode = getValueByKey(inputMap, "nbiodataaccessrequestcode");
		Object detailCodes = getValueByKey(inputMap, "nbiodataaccessrequestdetailscode");
//		Map<String, Object> inputData = (Map<String, Object>) inputMap.get("inputData");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		int nbiodataAccessrequestcode = Integer.parseInt(requestCode.toString());
		String nbiodataAccessrequestdetailscode = detailCodes.toString();
		return catelogueDataRequestandApprovalService.deleteChildBioRequest(nbiodataAccessrequestcode,
				nbiodataAccessrequestdetailscode, userInfo);
	}

	/**
	 * This method is used to retrieve child BioData associated with a BioData
	 * request. It fetches the logged-in user details from the request map and
	 * queries the service layer to get all child BioData records for the provided
	 * request.
	 *
	 * @param inputMap [Map] input data containing: - userinfo [UserInfo]: logged-in
	 *                 user details - nbiodataaccessrequestcode [int]: parent
	 *                 BioData request code
	 * @return ResponseEntity<Object> containing list of child BioData records
	 * @throws Exception if any error occurs during data retrieval
	 */
	@PostMapping(value = "/getChildBioData")
	public ResponseEntity<Object> getChildBioData(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getChildBioData");
		return catelogueDataRequestandApprovalService.getChildBioData(inputMap, userInfo);
	}

}