package com.agaramtech.qualis.biobank.service.cateloguedatarequestandapproval;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'biodataaccessrequest' table
 */
public interface CatelogueDataRequestandApprovalDAO {

	/**
	 * This method retrieves BioData requests for the given user and site.
	 *
	 * @param inputMap               [Map] contains request parameters including
	 *                               userInfo
	 * @param nbioDirectTransferCode [int] transfer code used for filtering
	 * @param userInfo               [UserInfo] details of the logged-in user
	 * @return ResponseEntity<Object> containing the list of BioData requests
	 * @throws Exception if any error occurs in DAO layer
	 */
	public ResponseEntity<Object> getBioDataRequest(Map<String, Object> inputMap, int nbioDirectTransferCode,
			UserInfo userInfo) throws Exception;

	/**
	 * This method fetches sample type data for a given BioData request.
	 *
	 * @param inputMap [Map] contains request parameters including userInfo
	 * @param userInfo [UserInfo] details of the logged-in user
	 * @return ResponseEntity<Object> containing sample type data
	 */
	public ResponseEntity<Object> getBioDataRequestSampleTypeData(Map<String, Object> inputMap, UserInfo userInfo);

	/**
	 * This method creates a new BioData request.
	 *
	 * @param inputMap [Map] contains BioData request details and userInfo
	 * @param userInfo [UserInfo] details of the logged-in user
	 * @return ResponseEntity<Object> containing result of creation
	 * @throws Exception if any error occurs in DAO layer
	 */
	public ResponseEntity<Object> createBioDataRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This method retrieves the list of BioProjects available for the user.
	 *
	 * @param userInfo [UserInfo] details of the logged-in user
	 * @return ResponseEntity<Object> containing BioProject list
	 */
	public ResponseEntity<Object> getBioProject(UserInfo userInfo) throws Exception;

	/**
	 * This method retrieves active BioData access records.
	 *
	 * @param nbiodataAccessrequestcode [int] unique code of BioData access request
	 * @param userInfo                  [UserInfo] details of the logged-in user
	 * @return ResponseEntity<Object> containing active BioData access records
	 * @throws Exception if any error occurs in DAO layer
	 */
	public ResponseEntity<Object> getActiveBioDataAccess(int nbiodataAccessrequestcode, UserInfo userInfo)
			throws Exception;

	/**
	 * This method sends a third-party e-Catalogue request.
	 *
	 * @param inputMap [Map] contains request details and userInfo
	 * @param userInfo [UserInfo] details of the logged-in user
	 * @return ResponseEntity<Object> containing request send result
	 * @throws Exception if any error occurs
	 */
	public ResponseEntity<Object> sendThirdpartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This method cancels a third-party e-Catalogue request.
	 *
	 * @param inputMap [Map] contains cancellation details and userInfo
	 * @param userInfo [UserInfo] details of the logged-in user
	 * @return ResponseEntity<Object> containing cancellation result
	 * @throws Exception if any error occurs
	 */
	public ResponseEntity<Object> cancelThirdpartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This method approves a third-party e-Catalogue request.
	 *
	 * @param inputMap [Map] contains approval details and userInfo
	 * @param userInfo [UserInfo] details of the logged-in user
	 * @return ResponseEntity<Object> containing approval result
	 * @throws Exception if any error occurs
	 */
	public ResponseEntity<Object> approveThirdpartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This method retires a third-party e-Catalogue request.
	 *
	 * @param inputMap [Map] contains retirement details and userInfo
	 * @param userInfo [UserInfo] details of the logged-in user
	 * @return ResponseEntity<Object> containing retirement result
	 * @throws Exception if any error occurs
	 */
	public ResponseEntity<Object> retireThirdECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This method finds the current status of a BioData access request.
	 *
	 * @param nbiodataAccessrequestcode [int] unique code of BioData access request
	 * @param userInfo                  [UserInfo] details of the logged-in user
	 * @return int representing the current status
	 * @throws Exception if any error occurs
	 */
	public int findStatusrequest(int nbiodataAccessrequestcode, UserInfo userInfo) throws Exception;

	/**
	 * This method deletes a child BioData request.
	 *
	 * @param nbiodataAccessrequestcode        [int] code of parent BioData access
	 *                                         request
	 * @param nbiodataAccessrequestdetailscode [String] code(s) of child request(s)
	 *                                         to delete
	 * @param userInfo                         [UserInfo] details of the logged-in
	 *                                         user
	 * @return ResponseEntity<Object> containing deletion result
	 * @throws Exception if any error occurs
	 */
	public ResponseEntity<Object> deleteChildBioRequest(int nbiodataAccessrequestcode,
			String nbiodataAccessrequestdetailscode, UserInfo userInfo) throws Exception;

	/**
	 * This method retrieves sample type data for a child BioData request.
	 *
	 * @param inputMap [Map] contains request details and userInfo
	 * @param userInfo [UserInfo] details of the logged-in user
	 * @return ResponseEntity<Object> containing child sample type data
	 * @throws Exception if any error occurs
	 */
	public ResponseEntity<Object> getChildBioDataRequestSampleTypeData(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This method retrieves child BioData details.
	 *
	 * @param inputMap [Map] contains request details and userInfo
	 * @param userInfo [UserInfo] details of the logged-in user
	 * @return ResponseEntity<Object> containing child BioData
	 * @throws Exception if any error occurs
	 */
	public ResponseEntity<Object> getChildBioData(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This method creates a new child BioData request.
	 *
	 * @param inputMap [Map] contains request details and userInfo
	 * @param userInfo [UserInfo] details of the logged-in user
	 * @return ResponseEntity<Object> containing result of creation
	 * @throws Exception if any error occurs
	 */
	public ResponseEntity<Object> createChildBioDataRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This method rejects a third-party e-Catalogue request.
	 *
	 * @param inputMap [Map] contains rejection details and userInfo
	 * @param userInfo [UserInfo] details of the logged-in user
	 * @return ResponseEntity<Object> containing rejection result
	 * @throws Exception if any error occurs
	 */
	public ResponseEntity<Object> rejectThirdECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

}
