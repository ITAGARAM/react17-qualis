package com.agaramtech.qualis.biobank.service.cateloguedatarequestandapproval;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'biodataaccessrequest' table
 */

public interface CatelogueDataRequestandApprovalService {

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * fetch BioData request details for a given direct transfer code.
	 * 
	 * @param inputMap               [Map] containing request details
	 * @param nbioDirectTransferCode [int] identifier for direct transfer
	 * @param userInfo               [UserInfo] holding logged in user details
	 * @return a response entity containing BioData request details
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getBioDataRequest(Map<String, Object> inputMap, int nbioDirectTransferCode,
			UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * fetch BioData request sample type details.
	 * 
	 * @param inputMap [Map] containing request details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return a response entity containing sample type details for the BioData
	 *         request
	 */
	public ResponseEntity<Object> getBioDataRequestSampleTypeData(Map<String, Object> inputMap, UserInfo userInfo);

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * create a new BioData request.
	 * 
	 * @param inputMap [Map] containing BioData request details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return a response entity containing the status of request creation
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createBioDataRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * fetch all active BioProjects.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return a response entity containing the list of active BioProjects
	 */
	public ResponseEntity<Object> getBioProject(UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * fetch active BioData access details.
	 * 
	 * @param nbiodataAccessrequestcode [int] identifier for BioData access request
	 * @param userInfo                  [UserInfo] holding logged in user details
	 * @return a response entity containing BioData access details
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveBioDataAccess(int nbiodataAccessrequestcode, UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * send a third-party e-catalogue request.
	 * 
	 * @param inputMap [Map] containing request details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return a response entity containing the status of the request
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> sendThirdpartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * cancel an existing third-party e-catalogue request.
	 * 
	 * @param inputMap [Map] containing cancellation details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return a response entity containing the status of cancellation
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> cancelThirdpartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * approve a third-party e-catalogue request.
	 * 
	 * @param inputMap [Map] containing approval details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return a response entity containing the status of approval
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> approveThirdpartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * retire an existing third-party e-catalogue request.
	 * 
	 * @param inputMap [Map] containing retire details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return a response entity containing the status of retire action
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> retireThirdECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * find the status of a BioData access request.
	 * 
	 * @param nbiodataAccessrequestcode [int] identifier for BioData access request
	 * @param userInfo                  [UserInfo] holding logged in user details
	 * @return status code of the BioData access request
	 * @throws Exception that are thrown in the DAO layer
	 */
	public int findStatusrequest(int nbiodataAccessrequestcode, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete a child BioData request.
	 * 
	 * @param nbiodataAccessrequestcode        [int] identifier for BioData access
	 *                                         request
	 * @param nbiodataAccessrequestdetailscode [String] identifiers for child
	 *                                         BioData request details
	 * @param userInfo                         [UserInfo] holding logged in user
	 *                                         details
	 * @return a response entity containing the status of deletion
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteChildBioRequest(int nbiodataAccessrequestcode,
			String nbiodataAccessrequestdetailscode, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * fetch child BioData request sample type details.
	 * 
	 * @param inputMap [Map] containing request details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return a response entity containing child BioData sample type details
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getChildBioDataRequestSampleTypeData(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * fetch child BioData details.
	 * 
	 * @param inputMap [Map] containing request details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return a response entity containing child BioData details
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getChildBioData(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * create a child BioData request.
	 * 
	 * @param inputMap [Map] containing child BioData request details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return a response entity containing the status of request creation
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createChildBioDataRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * reject a third-party e-catalogue request.
	 * 
	 * @param inputMap [Map] containing rejection details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return a response entity containing the status of rejection
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> rejectThirdECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

}
