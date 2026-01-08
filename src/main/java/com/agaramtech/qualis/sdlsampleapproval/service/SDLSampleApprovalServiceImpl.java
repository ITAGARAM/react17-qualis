package com.agaramtech.qualis.sdlsampleapproval.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.ApprovalParameter;

/**
 * This class holds methods to perform get operation on through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SDLSampleApprovalServiceImpl implements SDLSampleApprovalService {

	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param SDLSampleApprovalDAO sdlsampleapprovalDAO Interface
	 */
	public SDLSampleApprovalServiceImpl(SDLSampleApprovalDAO sdlsampleapprovalDAO) {
		super();
		this.sdlsampleapprovalDAO = sdlsampleapprovalDAO;
	}

	private final SDLSampleApprovalDAO sdlsampleapprovalDAO;
	
	/**
	 * This service implementation method will access the DAO layer that is used 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and list of completed and approved records
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> getApproval(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return sdlsampleapprovalDAO.getApproval(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * This method is used to retrieve list of registrationtype values. 
	 *  * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and list of registrationtype values
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Map<String, Object>> getRegistrationType(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return sdlsampleapprovalDAO.getRegistrationType(inputMap, userInfo);
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used 
	 * This method is used to approve the test. 
	* @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
			   primary key of site object for which the list is to be fetched 			
			 * @return response entity  object holding response status and approve the test
			 * @throws Exception exception
			 */
	@Transactional
	@Override
	public ResponseEntity<? extends Object> validateApprovalActions(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

			return sdlsampleapprovalDAO.validateApprovalActions(inputMap, userInfo, true);
		}

	@Transactional
	@Override
	public ResponseEntity<Object> updateDecision(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return sdlsampleapprovalDAO.updateDecision(inputMap, userInfo);

	}

//	public ResponseEntity<Object> releaseSamples(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
//		int napprovalConfigVersionCode = sdlsampleapprovalDAO.validateApprovalVersion((String)inputMap.get("npreregno"),userInfo);
//		if(napprovalConfigVersionCode==-1){
//			
//			try {
//				return new ResponseEntity<>(CommonFunction.getMultilingualMessage("IDS_SELECTSAMPLEOFSAMEAPPROVALVERSION", userInfo.getSlanguagefilename()),HttpStatus.EXPECTATION_FAILED);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}else{
//			inputMap.put("napprovalversioncode", napprovalConfigVersionCode);
//			return sdlsampleapprovalDAO.releaseSamples(inputMap, userInfo);
//		}
//		
//	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateEnforceStatus(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return sdlsampleapprovalDAO.updateEnforceStatus(inputMap, userInfo);

	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	  * This method is used to retrieve result value of the test. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and retrieve result value of the test
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getApprovalParameter(final String ntransactionTestCode, final UserInfo userInfo)
			throws Exception {

		return sdlsampleapprovalDAO.getApprovalParameter(ntransactionTestCode, userInfo);
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used 
	  * This method is used to retrieve result value of the test. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and retrieve result value of the test
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getSDLApprovalParameter(final String ntransactionTestCode, final UserInfo userInfo,
			final String nsitecode)
			throws Exception {

		return sdlsampleapprovalDAO.getSDLApprovalParameter(ntransactionTestCode, userInfo, nsitecode);
	}
	/**
	 * This service implementation method will access the DAO layer that is used 
	   * This method is used to retrieve list of sub samples values. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and list of sub samples values
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> getApprovalSubSample(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sdlsampleapprovalDAO.getApprovalSubSample(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	   * This method is used to retrieve list of test values. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and list of test values
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> getApprovalTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sdlsampleapprovalDAO.getApprovalTest(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	   * This method is used to retrieve list of registrationsubtype values. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and list of registrationsubtype values
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> getRegistrationSubType(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sdlsampleapprovalDAO.getRegistrationSubType(inputMap, userInfo);
	}
	/**
	 * This service implementation method will access the DAO layer that is used 
	    * This method is used to retrieve filter value ex:all,draft,approved,completed. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and filter value ex:all,draft,approved,completed.
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> getFilterStatus(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sdlsampleapprovalDAO.getFilterStatus(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getApproveConfigVersionRegTemplateDesign(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return sdlsampleapprovalDAO.getApproveConfigVersionRegTemplateDesign(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getApprovalVersion(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return sdlsampleapprovalDAO.getApprovalVersion(inputMap, userInfo);
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used 
	    * This method is used to retrieve list of samples values. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and list of samples values
	 * @throws Exception exception
	 */
	@Transactional
	@Override
	public ResponseEntity< Object> getApprovalSample(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sdlsampleapprovalDAO.getApprovalSample(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getStatusCombo(final int ntransactionResultCode, final UserInfo userInfo)
			throws Exception {
		return sdlsampleapprovalDAO.getStatusCombo(ntransactionResultCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getEditParameter(final String ntransactionTestCode, final UserInfo userInfo)
			throws Exception {
		return sdlsampleapprovalDAO.getEditParameter(ntransactionTestCode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateApprovalParameter(final List<ApprovalParameter> approvalParamList,
			final UserInfo userInfo) throws Exception {
		return sdlsampleapprovalDAO.updateApprovalParameter(approvalParamList, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getApprovalHistory(final String ntransactionTestCode, final UserInfo userInfo)
			throws Exception {
		return sdlsampleapprovalDAO.getApprovalHistory(ntransactionTestCode, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getApprovalResultChangeHistory(final String ntransactionTestCode,
			final UserInfo userInfo) throws Exception {

		return sdlsampleapprovalDAO.getApprovalResultChangeHistory(ntransactionTestCode, userInfo);
	}
	/**
	 * This service implementation method will access the DAO layer that is used 
	    * This method is used to retrieve result changes history value of the test. 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int]
	   primary key of site object for which the list is to be fetched 			
	 * @return response entity  object holding response status and retrieve result changes history value of the test
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getSDLApprovalResultChangeHistory(final String ntransactionTestCode,
			final UserInfo userInfo, final String nsitecode) throws Exception {

		return sdlsampleapprovalDAO.getSDLApprovalResultChangeHistory(ntransactionTestCode, userInfo, nsitecode);
	}
	
	@Override
	public Map<String, Object> viewAttachment(final Map<String, Object> objMap, final UserInfo userInfo)
			throws Exception {

		return sdlsampleapprovalDAO.viewAttachment(objMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getSampleApprovalHistory(final String npreregno, final UserInfo userInfo)
			throws Exception {
		return sdlsampleapprovalDAO.getSampleApprovalHistory(npreregno, userInfo);
	}

	public ResponseEntity<Object> getEnforceCommentsHistory(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return sdlsampleapprovalDAO.getEnforceCommentsHistory(inputMap, userInfo);

	}

	@Transactional
	@Override
	public ResponseEntity<Object> getFilterBasedTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sdlsampleapprovalDAO.getFilterBasedComboTest(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getCOAHistory(final String npreregno, final UserInfo userInfo) throws Exception {
		return sdlsampleapprovalDAO.getCOAHistory(npreregno, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> viewReport(final Map<String, Object> objMap, final UserInfo objUserInfo)
			throws Exception {

		return sdlsampleapprovalDAO.viewReport(objMap, objUserInfo);
	}

	public ResponseEntity<Object> getTestBasedOnCompletedBatch(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		return sdlsampleapprovalDAO.getTestBasedOnCompletedBatch(inputMap, userInfo);
	}

	public ResponseEntity<Map<String, Object>> checkReleaseRecord(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return sdlsampleapprovalDAO.checkReleaseRecord(inputMap, userInfo);
	}

	public ResponseEntity<Object> getSampleViewDetails(final Map<String, Object> inputMap) throws Exception {
		return sdlsampleapprovalDAO.getSampleViewDetails(inputMap);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getTestResultCorrection(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sdlsampleapprovalDAO.getTestResultCorrection(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Map<String, Object>> updateReleaseParameter(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		return sdlsampleapprovalDAO.updateReleaseParameter(request, userInfo);
	}

	@Override
	public ResponseEntity<Object> getReleaseResults(final int ntransactionresultcode, final UserInfo userInfo)
			throws Exception {
		return sdlsampleapprovalDAO.getReleaseResults(ntransactionresultcode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createFilterName(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return sdlsampleapprovalDAO.createFilterName(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<? extends Object> getTestApprovalFilterDetails(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return sdlsampleapprovalDAO.getTestApprovalFilterDetails(inputMap, userInfo);
	}

}
