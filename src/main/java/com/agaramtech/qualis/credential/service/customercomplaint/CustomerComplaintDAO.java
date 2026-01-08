package com.agaramtech.qualis.credential.service.customercomplaint;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.credential.model.CustomerComplaint;
import com.agaramtech.qualis.credential.model.CustomerComplaintFile;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'customercomplaint'
 * 
* @author Mullai Balaji.V [SWSM-9] Customer Complaints - Screen Development -
 *         Agaram Technologies
 */
public interface CustomerComplaintDAO {
	
	/**
	 * This interface declaration is used to get all the available customercomplaints with respect to site
	 *
	 * @param userInfo an instance of [UserInfo] containing the logged-in user
	 *                 details and the nmasterSiteCode, which is the primary key of
	 *                 the site for which the complaints are to be fetched
	 * @param inputMap a map containing filtering criteria such as fromDate, toDate,
	 *                 and status
	 *
	 * @return a ResponseEntity containing the list of customer complaint records
	 *         associated with the specified site
	 *
	 * @throws Exception if any errors occur in the DAO layer
	 */
	public ResponseEntity<Object> getCustomerComplaint(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This  interface method provides access to the DAO layer to retrieve
	 * all available customercomplaint for a specific site, filtered by date range
	 * and status.
	 *
	 * @param inputMap a map containing filter criteria such as fromDate, toDate,
	 *                 and status
	 * @param userInfo an instance of [UserInfo] containing the logged-in user
	 *                 details and the nmasterSiteCode, which is the primary key of
	 *                 the site for which the complaints are to be fetched
	 *
	 * @return a ResponseEntity containing the list of customer complaint records
	 *         associated with the specified site
	 *
	 * @throws Exception if any errors occur in the DAO layer
	 */
	public ResponseEntity<Object> getCustomerComplaintData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This  interface method provides access to the DAO layer to retrieve
	 * all the available regions 
	 *
	 * @param userInfo [UserInfo] contains the details of the logged-in user,
	 *                 including the nmasterSiteCode [int], which represents the
	 *                 primary key of the site for which the region list is to be
	 *                 fetched.
	 * @return a ResponseEntity containing the list of region records associated
	 *         with the site.
	 * @throws Exception if any errors occur in the DAO layer during data retrieval.
	 */
	public ResponseEntity<Object> getRegion(final UserInfo userInfo) throws Exception;

	/**
	 * This  interface declaration will access the DAO layer that is
	 * used to get all Distircts based on the available Region 
	 * 
	 * @param nregioncode   Holding the current Region record
	 * @param userInfo      [UserInfo] holding logged in user details and
	 *                      nmasterSiteCode [int] primary key of site object for
	 *                      which the list is to be fetched
	 * @return a response entity which holds the list of customercomplaint records
	 *         
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getDistrict(final int nregioncode,
			final UserInfo userInfo) throws Exception;


	/**
	 * This  interface method provides access to the DAO layer to retrieve
	 * all the available City  within a specific District.
	 *
	 * @param nregioncode   an integer representing the unique region code for which
	 *                      the district list is to be fetched.
	 * @param userInfo      [UserInfo] object containing the logged-in user details,
	 *                      including nmasterSiteCode [int], which is the primary
	 *                      key of the site object.
	 * @return a ResponseEntity containing the list of district records associated
	 *         with the given region 
	 * @throws Exception if any errors occur in the DAO layer during data retrieval.
	 */
	public ResponseEntity<Object> getCity(final int ndistrictcode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This  interface declaration will access the DAO layer that is used to
	 * get all the available Village records with respect to City.
	 *
	 * @param ncitycode     Holding the City record (foreign key reference for
	 *                      village lookup)
	 * @param userInfo      [UserInfo] holding logged-in user details including
	 *                      nmasterSiteCode [int], which is the primary key of the
	 *                      site object for which the list is to be fetched
	 * @return a response entity which holds the list of village records with
	 *         respect to the given city 
	 * @throws Exception if any error occurs in the DAO layer
	 */
	//added by sujatha ATE_274 23-09-2025 to get villages with one more check based on nsitehierarchyconfigcode
	public ResponseEntity<Object> getVillage(final int nsitehierarchyconfigcode, final int ncitycode, final UserInfo userInfo)
			throws Exception;


	/**
	 * This  interface declaration is responsible for creating a new Customer
	 * Complaint record by accessing the DAO layer.
	 * 
	 * @param objmap A map containing:
	 * 
	 *               Customer complaint details (e.g., fromDate, toDate, status,
	 *               etc.) UserInfo object holding logged-in user details
	 *               nmasterSiteCode [int] representing the primary key of the site
	 *               for which the record is to be created
	 * 
	 * @return ResponseEntity<Object> containing the status and the created Customer
	 *         Complaint object (or error details if creation fails).
	 * @throws Exception if any error occurs in the DAO layer while creating the
	 *                   record
	 */
	public ResponseEntity<Object> createCustomerComplaint(Map<String, Object> objmap) throws Exception;

	/**
	 * Service  method to mark a record as initiated in the
	 * customercomplainthistory table.
	 *
	 * @param objmap a map containing: - fromDate, toDate, and status details of the
	 *               complaint history - customercomplainthistory details - userInfo
	 *               (logged-in user details) - nmasterSiteCode (primary key of the
	 *               site object for which the list is fetched)
	 * @return ResponseEntity object containing the response status and data of the
	 *         updated customercomplainthistory record
	 * @throws Exception if any error occurs in the DAO layer
	 */
	public ResponseEntity<Object> initiateCustomerComplaint(Map<String, Object> objmap) throws Exception;

	/**
	 * This  interface declaration will access the DAO layer to update a
	 * record in the customercomplainthistory table by marking the complaint as
	 * closed.
	 * 
	 * @param objmap a map containing: - customercomplainthistory details to be
	 *               updated - userInfo [UserInfo] holding logged-in user details -
	 *               nmasterSiteCode [int] primary key of the site object where the
	 *               complaint belongs
	 * 
	 * @return ResponseEntity object holding the response status and the updated
	 *         customercomplainthistory data
	 * 
	 * @throws Exception if any error occurs in the DAO layer
	 */
	public ResponseEntity<Object> closeCustomerComplaint(Map<String, Object> objmap) throws Exception;

	/**
	 * This  interface declaration will access the DAO layer that is used to
	 * retrieve active customercomplainthistory object based on the specified
	 * ncustomercomplaintode.
	 * 
	 * @param ncustomercomplaintCode [int] primary key of CustomerComplaint object
	 * @param userInfo               [UserInfo] holding logged in user details based
	 *                               on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         CustomerComplaint object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public CustomerComplaint getActiveCustomerComplaintById(final int ncustomercomplaintcode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This  interface declaration will access the DAO layer that is used to
	 * update entry in customercomplaint table.
	 * 
	 * @param inputMap holding the date fromDate,toDate,Status customercomplaint
	 *                 details and userInfo [UserInfo] holding logged in user
	 *                 details and nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         customercomplaint object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateCustomerComplaint(Map<String, Object> objMap) throws Exception;


	/**
	 * This  interface declaration will access the DAO layer that is used to
	 * delete an entry in customercomplaint table.
	 * 
	 * @param inputMap holding the date fromDate,toDate,Status customercomplaint
	 *                 details and userInfo [UserInfo] holding logged in user
	 *                 details and nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         customercomplaint object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteCustomerComplaint(Map<String, Object> objMap) throws Exception;

	/**
	 * This  interface declaration will access the DAO layer that is used to
	 * retrieve active customercomplainthistory object based on the specified
	 * ncustomercomplaintode.
	 * 
	 * @param ncustomercomplaintCode [int] primary key of CustomerComplaint object
	 * @param userInfo               [UserInfo] holding logged in user details based
	 *                               on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         CustomerComplaint object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getCustomerComplaintRecord(final int ncustomercomplaintcode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This  interface declaration will access the DAO layer that is used to
	 * create the records in customercomplaintfile
	 * 
	 * @param request  holding the date for upload the file in customercomplaintfile
	 *                 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of customercomplaintfile
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createCustomerComplaintFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception;

	/**
	 * This interface declaration will access the DAO layer that is used to
	 * create the records in customercomplaintfile
	 * 
	 * @param request  holding the date for upload the file in customercomplaintfile
	 *                 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of customercomplaintfile
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> editCustomerComplaintFile(final CustomerComplaintFile objCustomerComplaintFile,
			final UserInfo objUserInfo) throws Exception;


	/**
	 * This interface declaration will access the DAO layer that is used to
	 * delete the records in customercomplaintfile
	 * 
	 * @param request  holding the date for delete the file in customercomplaintfile
	 *                 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of customercomplaintfile
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteCustomerComplaintFile(final CustomerComplaintFile objCustomerComplaintFile,
			final UserInfo objUserInfo) throws Exception;

	/**
	 * This interface declaration will access the DAO layer that is used to
	 * update the records in customercomplaintfile
	 * 
	 * @param request  holding the date for update the file in customercomplaintfile
	 *                 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of customercomplaintfile
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateCustomerComplaintFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception;

	
	/**
	 * This interface declaration will access the DAO layer that is used to
	 * view the records in customercomplaintfile
	 * 
	 * @param request  holding the date for view the file in customercomplaintfile
	 *                 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of customercomplaintfile
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> viewAttachedCustomerComplaintFile(final CustomerComplaintFile objCustomerComplaintFile,
			final UserInfo objUserInfo) throws Exception;

}
