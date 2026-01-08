package com.agaramtech.qualis.credential.service.manpower;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.credential.model.ManPower;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on 'outsourceemployee'
 * table
 */
/**
 * @author sujatha.v SWSM-8 01/08/2025
 */
public interface ManPowerService {

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available outsourceemployee with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of outsourceemployee records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getManPower(final UserInfo userInfo) throws Exception;
	
	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of site records 
	 * @throws Exception that are thrown in the DAO layer
	 */
	// added by sujatha ATE_274 to get the Login Site in the 1st value of dropdown on 26-08-2025
	public ResponseEntity<Object> getSite(final UserInfo userInfo) throws Exception;


	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to outsourceemployee table.
	 * 
	 * @param objManPower [ManPower] object holding details to be added in outsourceemployee
	 *                    table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         manpower object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createManPower(final ManPower objManPower, final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * retrieve active manpower object based on the specified nmanpowercode.
	 * 
	 * @param nmanpowercode [int] primary key of manpower object
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return response entity object holding response status and data of manpower
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveManPowerById(final int nmanpowercode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * update entry in outsourceemployee table.
	 * 
	 * @param objManPower [ManPower] object holding details to be updated in
	 *                    outsourceemployee table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         manpower object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateManPower(final ManPower objManPower, final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete an entry in outsourceemployee table.
	 * 
	 * @param objManPower [ManPower] object holding detail to be deleted from
	 *                    outsourceemployee table
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         manpower object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteManPower(final ManPower objManPower, final UserInfo userInfo) throws Exception;
}
