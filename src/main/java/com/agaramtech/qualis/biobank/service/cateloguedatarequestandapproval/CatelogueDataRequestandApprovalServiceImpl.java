package com.agaramtech.qualis.biobank.service.cateloguedatarequestandapproval;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'unit' table through
 * its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class CatelogueDataRequestandApprovalServiceImpl implements CatelogueDataRequestandApprovalService {

	private final CatelogueDataRequestandApprovalDAO catelogueDataRequestandApprovalDAO;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param catelogueDataRequestandApprovalDAO CatelogueDataRequestandApprovalDAO
	 *                                           Interface
	 * @param commonFunction                     CommonFunction holding common
	 *                                           utility functions
	 */
	/**
	 * Constructor to initialize the service implementation with the DAO dependency.
	 * 
	 * @param catelogueDataRequestandApprovalDAO [CatelogueDataRequestandApprovalDAO]
	 *                                           DAO object used for performing
	 *                                           database operations
	 */
	public CatelogueDataRequestandApprovalServiceImpl(
			CatelogueDataRequestandApprovalDAO catelogueDataRequestandApprovalDAO) {
		this.catelogueDataRequestandApprovalDAO = catelogueDataRequestandApprovalDAO;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * fetch BioData request details for a given direct transfer code.
	 *
	 * @param inputMap               [Map] containing request details
	 * @param nbioDirectTransferCode [int] identifier for direct transfer
	 * @param userInfo               [UserInfo] holding logged in user details
	 * @return response entity containing BioData request details
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getBioDataRequest(Map<String, Object> inputMap, int nbioDirectTransferCode,
			UserInfo userInfo) throws Exception {
		return catelogueDataRequestandApprovalDAO.getBioDataRequest(inputMap, nbioDirectTransferCode, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * fetch BioData request sample type details.
	 *
	 * @param inputMap [Map] containing request details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return response entity containing BioData sample type details
	 */
	@Override
	public ResponseEntity<Object> getBioDataRequestSampleTypeData(Map<String, Object> inputMap, UserInfo userInfo) {
		return catelogueDataRequestandApprovalDAO.getBioDataRequestSampleTypeData(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * create a new BioData request.
	 *
	 * @param inputMap [Map] containing BioData request details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return response entity containing status of creation
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> createBioDataRequest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return catelogueDataRequestandApprovalDAO.createBioDataRequest(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * create a child BioData request.
	 *
	 * @param inputMap [Map] containing child BioData request details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return response entity containing status of creation
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> createChildBioDataRequest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return catelogueDataRequestandApprovalDAO.createChildBioDataRequest(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * fetch all active BioProjects.
	 *
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return response entity containing list of active BioProjects
	 */
	@Override
	public ResponseEntity<Object> getBioProject(UserInfo userInfo) throws Exception{
		return catelogueDataRequestandApprovalDAO.getBioProject(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * fetch active BioData access details.
	 *
	 * @param nbiodataAccessrequestcode [int] identifier for BioData access request
	 * @param userInfo                  [UserInfo] holding logged in user details
	 * @return response entity containing BioData access details
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveBioDataAccess(int nbiodataAccessrequestcode, UserInfo userInfo)
			throws Exception {
		return catelogueDataRequestandApprovalDAO.getActiveBioDataAccess(nbiodataAccessrequestcode, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * send a third-party e-catalogue request.
	 *
	 * @param inputMap [Map] containing request details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return response entity containing status of request
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> sendThirdpartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return catelogueDataRequestandApprovalDAO.sendThirdpartyECatalogueRequest(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * cancel a third-party e-catalogue request.
	 *
	 * @param inputMap [Map] containing cancellation details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return response entity containing status of cancellation
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> cancelThirdpartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return catelogueDataRequestandApprovalDAO.cancelThirdpartyECatalogueRequest(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * approve a third-party e-catalogue request.
	 *
	 * @param inputMap [Map] containing approval details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return response entity containing status of approval
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> approveThirdpartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return catelogueDataRequestandApprovalDAO.approveThirdpartyECatalogueRequest(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retire a third-party e-catalogue request.
	 *
	 * @param inputMap [Map] containing retire details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return response entity containing status of retire action
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> retireThirdECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return catelogueDataRequestandApprovalDAO.retireThirdECatalogueRequest(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * reject a third-party e-catalogue request.
	 *
	 * @param inputMap [Map] containing rejection details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return response entity containing status of rejection
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> rejectThirdECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return catelogueDataRequestandApprovalDAO.rejectThirdECatalogueRequest(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * find the status of a BioData access request.
	 *
	 * @param nbiodataAccessrequestcode [int] identifier for BioData access request
	 * @param userInfo                  [UserInfo] holding logged in user details
	 * @return status code of the BioData access request
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public int findStatusrequest(final int nbiodataAccessrequestcode, final UserInfo userInfo) throws Exception {
		return catelogueDataRequestandApprovalDAO.findStatusrequest(nbiodataAccessrequestcode, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete a child BioData request.
	 *
	 * @param nbiodataAccessrequestcode        [int] identifier for BioData access
	 *                                         request
	 * @param nbiodataAccessrequestdetailscode [String] identifiers for child
	 *                                         BioData request details
	 * @param userInfo                         [UserInfo] holding logged in user
	 *                                         details
	 * @return response entity containing status of deletion
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> deleteChildBioRequest(final int nbiodataAccessrequestcode,
			final String nbiodataAccessrequestdetailscode, final UserInfo userInfo) throws Exception {
		return catelogueDataRequestandApprovalDAO.deleteChildBioRequest(nbiodataAccessrequestcode,
				nbiodataAccessrequestdetailscode, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * fetch child BioData request sample type details.
	 *
	 * @param inputMap [Map] containing request details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return response entity containing child BioData sample type details
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getChildBioDataRequestSampleTypeData(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return catelogueDataRequestandApprovalDAO.getChildBioDataRequestSampleTypeData(inputMap, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * fetch child BioData details.
	 *
	 * @param inputMap [Map] containing request details
	 * @param userInfo [UserInfo] holding logged in user details
	 * @return response entity containing child BioData details
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getChildBioData(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return catelogueDataRequestandApprovalDAO.getChildBioData(inputMap, userInfo);
	}

}