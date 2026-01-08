package com.agaramtech.qualis.storagemanagement.service.sitesampleexpirymapping;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.SiteSampleExpiryMapping;

/**
 * This interface holds declarations to perform CRUD operation on 'siteexpirymapping' table
 */
/**
 * @author sujatha.v SWSM-14 31/08/2025
 */
public interface SiteSampleExpiryMappingDAO {

	/**
	 * This interface declaration is used to get all the available siteexpirymapping's
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of siteexpirymapping records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSiteSampleExpiryMapping(final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to get period 
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the period records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getPeriod(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get all the available site 
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of site records
	 * @throws Exception that are thrown in the DAO layer
	 */
	// added by sujatha ATE_274 to get the Login Site in the 1st value of dropdown on 01-09-2025
	public ResponseEntity<Object> getSite(final UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active siteexpirymapping object
	 * based on the specified nsiteexpirymappingcode.
	 * 
	 * @param nsiteexpirymappingcode [int] primary key of siteexpirymapping object
	 * @param userInfo           [UserInfo] holding logged in user details based on
	 *                           which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         siteexpirymapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public SiteSampleExpiryMapping getActiveSiteSampleExpiryMappingById(final int nsiteexpirymappingcode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to siteexpirymapping table.
	 * 
	 * @param objSiteSampleExpiryMapping [SiteSampleExpiryMapping] object holding details to be added in
	 *                         siteexpirymapping table
	 * @param userInfo         [UserInfo] holding logged in user details and
	 *                         nmasterSiteCode [int] primary key of site object for
	 *                         which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         siteexpirymapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSiteSampleExpiryMapping(
			final SiteSampleExpiryMapping objSiteSampleExpiryMapping, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in siteexpirymapping table.
	 * 
	 * @param objSiteSampleExpiryMapping [SiteSampleExpiryMapping] object holding details to be updated
	 *                         in siteexpirymapping table
	 * @param userInfo         [UserInfo] holding logged in user details and
	 *                         nmasterSiteCode [int] primary key of site object for
	 *                         which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         siteexpirymapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSiteSampleExpiryMapping(
			final SiteSampleExpiryMapping objSiteSampleExpiryMapping, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete an entry in siteexpirymapping table.
	 * 
	 * @param objSiteSampleExpiryMapping [SiteSampleExpiryMapping] object holding detail to be deleted
	 *                         from siteexpirymapping table
	 * @param userInfo         [UserInfo] holding logged in user details and
	 *                         nmasterSiteCode [int] primary key of site object for
	 *                         which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         siteexpirymapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSiteSampleExpiryMapping(
			final SiteSampleExpiryMapping objSiteSampleExpiryMapping, final UserInfo userInfo) throws Exception;
}
