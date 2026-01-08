package com.agaramtech.qualis.samplescheduling.service.samplelocation;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.samplescheduling.model.SampleLocation;

/**
 * This interface holds declarations to perform CRUD operation on 'samplelocation' table
 */
/**
 * @author sujatha.v AT-E274 SWSM-5 24/07/2025
 */
public interface SampleLocationService {

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available samplelocation with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of samplelocation records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSampleLocation(final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available regions with respect to approved site hierarchy configuration
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of region records with respect
	 *         to approved site hierarchy configuration
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getRegion(final UserInfo userInfo) throws Exception;

	  //commented by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
//	public ResponseEntity<Object> getVillage(final int nprimarykey, final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available taluka that is mapped in the site hierarchy configuration
	 * 
	 * @param nprimarykey this is the primary key of district what they selected in the drop down
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of taluka that is mapped in the site hierarchy configuration
	 * @throws Exception that are thrown in the DAO layer
	 */
	//added by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
	public ResponseEntity<Object> getTaluka(final int nprimarykey, final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available district with respect to the district that was mapped in site hierarchy configuration
	 *  and the selected region
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of district records with
	 *         respect to site hierarchy configuraion and load's based on the selected region
	 * @throws Exception that are thrown in the DAO layer
	 */
	//added by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
//	public ResponseEntity<Object> getDistrict(final Map<String, Object> inputMap, final UserInfo userInfo)
//			throws Exception;
	public ResponseEntity<Object> getDistrict(final int nprimarykey,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available city with respect to site and the selected district
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of city records with respect
	 *         to site and load's based on selected district
	 * @throws Exception that are thrown in the DAO layer
	 */
//	public ResponseEntity<Object> getCity(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available village based on taluk
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of village records  selected taluk.
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getVillage(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to samplelocation table.
	 * 
	 * @param objSampleLocation [SampleLocation] object holding details to be added
	 *                          in samplelocation table
	 * @param userInfo          [UserInfo] holding logged in user details and
	 *                          nmasterSiteCode [int] primary key of site object for
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         samplelocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createSampleLocation(final SampleLocation objSampleLocation, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * retrieve active samplelocation object based on the specified
	 * nsamplelocationcode.
	 * 
	 * @param nsamplelocationcode [int] primary key of samplelocation object
	 * @param userInfo            [UserInfo] holding logged in user details based on
	 *                            which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         samplelocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveSampleLocationById(final int nsamplelocationcode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * update entry in samplelocation table.
	 * 
	 * @param objSampleLocation [SampleLocation] object holding details to be
	 *                          updated in samplelocation table
	 * @param userInfo          [UserInfo] holding logged in user details and
	 *                          nmasterSiteCode [int] primary key of site object for
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         samplelocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateSampleLocation(final SampleLocation objSampleLocation, final UserInfo userInfo)
			throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete an entry in samplelocation table.
	 * 
	 * @param objSampleLocation [SampleLocation] object holding detail to be deleted
	 *                          from samplelocation table
	 * @param userInfo          [UserInfo] holding logged in user details and
	 *                          nmasterSiteCode [int] primary key of site object for
	 *                          which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         samplelocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteSampleLocation(final SampleLocation objSampleLocation, final UserInfo userInfo)
			throws Exception;
}
