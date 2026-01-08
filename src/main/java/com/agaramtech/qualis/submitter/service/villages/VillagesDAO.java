package com.agaramtech.qualis.submitter.service.villages;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.Villages;

/**
 * This interface holds declarations to perform CRUD operation on 'villages' table
 */
/**
 * @author sujatha.v
 * SWSM-4
 * 22/07/2025
 */
public interface VillagesDAO {

	/**
	 * This interface declaration is used to get all the available village's with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of village records with respect
	 *         to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getVillage(final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to get the available taluka's from the approved site hierarchy config version
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of the available taluka's from the approved site hierarchy config version
	 * @throws Exception that are thrown in the DAO layer
	 */
	//added by sujatha ATE_274 to get taluka from approved version of the site hierarchy config 
	public ResponseEntity<Object> getTaluka(final UserInfo userInfo) throws Exception;


	/**
	 * This interface declaration is used to add a new entry to village table.
	 * 
	 * @param objVillage  [Villages] object holding details to be added in village table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of added village
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createVillage(final Villages objVillage, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active village object based on
	 * the specified nvillageCode.
	 * 
	 * @param nvillagecode [int] primary key of village object
	 * @param userInfo  [UserInfo] holding logged in user details based on which the
	 *                  list is to be fetched
	 * @return response entity object holding response status and data of village
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Villages getActiveVillageById(final int nvillagecode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in village table.
	 * 
	 * @param objVillage  [Villages] object holding details to be updated in village table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of updated
	 *         village object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateVillage(final Villages objVillage, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete an entry in village table.
	 * 
	 * @param objVillage  [Village] object holding detail to be deleted from village table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of deleted
	 *         village object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteVillage(final Villages objVillage, final UserInfo userInfo) throws Exception;
}
