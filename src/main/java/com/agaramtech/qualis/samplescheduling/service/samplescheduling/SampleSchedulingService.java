package com.agaramtech.qualis.samplescheduling.service.samplescheduling;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.samplescheduling.model.SampleSchedulingFile;

public interface SampleSchedulingService {


	/**
	 * This service interface declaration is responsible for creating a new Sample
	 * Scheduling record by accessing the DAO layer.
	 * 
	 * @param inputMap A map containing:
	 * 
	 *               Sample Schdeuling details (e.g., fromYear, period, description,
	 *               etc.) UserInfo object holding logged-in user details
	 *               nmasterSiteCode [int] representing the primary key of the site
	 *               for which the record is to be created
	 * 
	 * @return ResponseEntity<Object> containing the status and the created Sample
	 *         Scheduling object (or error details if creation fails).
	 * @throws Exception if any error occurs in the DAO layer while creating the
	 *                   record
	 */
public ResponseEntity<Object> createSampleScheduling(Map<String, Object> inputMap) throws Exception;

/**
 * This service interface declaration is responsible for creating a new Sample
 * Scheduling record by accessing the DAO layer.
 * 
 * @param inputMap A map containing:
 * 
 *               Sample Schdeuling Location details.
 *               UserInfo object holding logged-in user details
 *               nmasterSiteCode [int] representing the primary key of the site
 *               for which the record is to be created
 * 
 * @return ResponseEntity<Object> containing the status and the created Sample
 *         Scheduling object (or error details if creation fails).
 * @throws Exception if any error occurs in the DAO layer while creating the
 *                   record
 */
public ResponseEntity<Object> createSampleSchedulingLocation(Map<String, Object> inputMap) throws Exception;



/**
 * Service interface method to mark a record as planned in the
 * sampleschedulinghistory table.
 *
 * @param inputMap a map containing: - samlescheduling primaryKey - userInfo
 *               (logged-in user details) - nmasterSiteCode (primary key of the
 *               site object for which the list is fetched)
 * @return ResponseEntity object containing the response status and data of the
 *         updated sampleschedulinghistory record
 * @throws Exception if any error occurs in the DAO layer
 */
public ResponseEntity<Object> plannedSampleScheduling(Map<String, Object> inputMap) throws Exception;




/**
 * This service interface method provides access to the DAO layer to retrieve
 * all available samplescheduling for a specific site, filtered by date range
 * and status.
 *
 * @param inputMap a map containing filter criteria such as fromYear, toYear,
 *                 and status
 * @param userInfo an instance of [UserInfo] containing the logged-in user
 *                 details and the nmasterSiteCode, which is the primary key of
 *                 the site for which the complaints are to be fetched
 *
 * @return a ResponseEntity containing the list of Sample Scheduling records
 *         associated with the specified site
 *
 * @throws Exception if any errors occur in the DAO layer
 */

public ResponseEntity<Object> getSampleSchedulingData(final Map<String, Object> inputMap, final UserInfo userInfo)
		throws Exception;


/**
 * This service interface declaration will access the DAO layer that is used to
 * update entry in sampleschedulingLocation table.
 * 
 * @param inputMap holding the date fromYear,toYear,Status samplescheduling
 *                 details and userInfo [UserInfo] holding logged in user
 *                 details and nmasterSiteCode [int] primary key of site object
 *                 for which the list is to be fetched
 * @return response entity object holding response status and data of updated
 *         sampleschedulingLocation object
 * @throws Exception that are thrown in the DAO layer
 */

public ResponseEntity<Object> updateSampleSchedulingLocation(Map<String, Object> inputMap) throws Exception;

/**
 * This service interface method provides access to the DAO layer to retrieve
 * all available samplescheduling for a specific site.
 *
 * @param userInfo an instance of [UserInfo] containing the logged-in user
 *                 details and the nmasterSiteCode, which is the primary key of
 *                 the site for which the complaints are to be fetched
 * @param inputMap a map containing filtering criteria such as fromYear, toYear,
 *                 and status
 *
 * @return a ResponseEntity containing the list of Sample Scheduling records
 *         associated with the specified site
 *
 * @throws Exception if any errors occur in the DAO layer
 */

public ResponseEntity<Object> getSampleScheduling(final Map<String, Object> inputMap, final UserInfo userInfo)
		throws Exception;
/**
 * This service interface declaration will access the DAO layer that is used to
 * retrieve active samplescheduling object based on the specified
 * nsampleschedulingCode.
 * 
 * @param nsampleschedulingCode [int] primary key of samplescheduling object
 * @param userInfo               [UserInfo] holding logged in user details based
 *                               on which the list is to be fetched
 * @return response entity object holding response status and data of
 *         samplescheduling object
 * @throws Exception that are thrown in the DAO layer
 */
public ResponseEntity<Object> getActiveSampleSchedulingById(final int nsampleschedulingcode,
		final UserInfo userInfo) throws Exception;

/**
 * This service interface method provides access to the DAO layer to retrieve
 * all the available districts for a given SubDivisionalLab(city) within a specific District
 *
 * @param nprimarykey   an integer representing the unique district code for which
 *                      the city list is to be fetched.
 * @param userInfo      [UserInfo] object containing the logged-in user details,
 *                      including nmasterSiteCode [int], which is the primary
 *                      key of the site object.
 * @return a ResponseEntity containing the list of district records associated
 *         with the given region .
 * @throws Exception if any errors occur in the DAO layer during data retrieval.
 */
public ResponseEntity<Object> getSubDivisionalLab(final int nprimarykey,
		final UserInfo userInfo) throws Exception;

/**
 * This service interface declaration will access the DAO layer that is used to
 * get all the available District Based on Region
 * 
 * @param nprimarykey   Holding the current Region record
 * @param userInfo      [UserInfo] holding logged in user details and
 *                      nmasterSiteCode [int] primary key of site object for
 *                      which the list is to be fetched
 * @return a response entity which holds the list of getDistrictLab records
 *         with respect to site
 * @throws Exception that are thrown in the DAO layer
 */

public ResponseEntity<Object> getDistrictLab(final int nprimarykey,
		final UserInfo userInfo) throws Exception;


/**
 * This service interface declaration will access the DAO layer that is used to
 * get all the available location Based on Region
 * 
 * @param nprimarykey   Holding the current Village record
 * @param userInfo      [UserInfo] holding logged in user details and
 *                      nmasterSiteCode [int] primary key of site object for
 *                      which the list is to be fetched
 * @return a response entity which holds the list of getLocation records
 *         with respect to site
 * @throws Exception that are thrown in the DAO layer
 */
public ResponseEntity<Object> getLocation(final int nprimarykey,
		final UserInfo userInfo) throws Exception;



/**
 * This service interface declaration will access the DAO layer that is used to
 * get all the available Village records with respect to City.
 *
 * @param nprimarykey     Holding the City record (foreign key reference for
 *                      village lookup)
 * @param userInfo      [UserInfo] holding logged-in user details including
 *                      nmasterSiteCode [int], which is the primary key of the
 *                      site object for which the list is to be fetched
 * @return a response entity which holds the list of village records with
 *         respect to the given city and site
 * @throws Exception if any error occurs in the DAO layer
 */
//modified by sujatha ATE_274 SWSM-117 by adding nsitehierarchyconfigcode for getting the approved site hierarchy config villages
public ResponseEntity<Object> getVillage(final int nprimarykey,
		final UserInfo userInfo, final int nsitehierarchyconfigcode) throws Exception;



/**
 * This service interface declaration will access the DAO layer that is used to
 * get all the available SampleLocation records.
 *
 * @param nprimarykey     Holding the samplescheduling record (foreign key reference for
 *                      Location lookup)
 * @param userInfo      [UserInfo] holding logged-in user details including
 *                      nmasterSiteCode [int], which is the primary key of the
 *                      site object for which the list is to be fetched
 * @return a response entity which holds the list of SampleSchedulingLocation records with
 *         respect to the given villages and site
 * @throws Exception if any error occurs in the DAO layer
 */
public ResponseEntity<Object> getSampleSchedulingLocation(final int nprimarykey,
		final UserInfo userInfo) throws Exception;


/**
 * This service interface declaration will access the DAO layer that is used to
 * update entry in samplescheduling table.
 * 
 * @param inputMap holding the date fromYear,toYear,Status samplescheduling
 *                 details and userInfo [UserInfo] holding logged in user
 *                 details and nmasterSiteCode [int] primary key of site object
 *                 for which the list is to be fetched
 * @return response entity object holding response status and data of updated
 *         samplescheduling object
 * @throws Exception that are thrown in the DAO layer
 */
public ResponseEntity<Object> updateSampleScheduling(Map<String, Object> inputMap) throws Exception;


/**
 * This service interface declaration will access the DAO layer that is used to
 * retrieve active sampleschedulinghistory object based on the specified
 * nsampleschedulingcode.
 * 
 * @param nsampleschedulingcode [int] primary key of SampleScheduling object
 * @param userInfo               [UserInfo] holding logged in user details based
 *                               on which the list is to be fetched
 * @return response entity object holding response status and data of
 *         SampleScheduling object
 * @throws Exception that are thrown in the DAO layer
 */
public ResponseEntity<Object> getSampleSchedulingRecord(final int nsampleschedulingcode, final UserInfo userInfo)
		throws Exception;

/**
 * This service interface declaration will access the DAO layer that is used to
 * delete an entry in samplescheduling table.
 * 
 * @param inputMap holding the date fromYear,toYear,Status samplescheduling
 *                 details and userInfo [UserInfo] holding logged in user
 *                 details and nmasterSiteCode [int] primary key of site object
 *                 for which the list is to be fetched
 * @return response entity object holding response status and data of deleted
 *         samplescheduling object
 * @throws Exception that are thrown in the DAO layer
 */
public ResponseEntity<Object> deleteSampleScheduling(Map<String, Object> objMap) throws Exception;


/**
 * This service interface declaration will access the DAO layer that is used to
 * create the records in sampleschedulingfile
 * 
 * @param request  holding the date for upload the file in sampleschedulingfile
 *                 table
 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
 *                 [int] primary key of site object for which the list is to be
 *                 fetched
 * @return a response entity which holds the list of sampleschedulingfile
 *         records with respect to site
 * @throws Exception that are thrown in the DAO layer
 */
public ResponseEntity<Object> createSampleSchedulingFile(MultipartHttpServletRequest request,
		final UserInfo objUserInfo) throws Exception;

/**
 * This service interface declaration will access the DAO layer that is used to
 * create the records in sampleschedulingfile
 * 
 * @param request  holding the date for upload the file in sampleschedulingfile
 *                 table
 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
 *                 [int] primary key of site object for which the list is to be
 *                 fetched
 * @return a response entity which holds the list of sampleschedulingfile
 *         records with respect to site
 * @throws Exception that are thrown in the DAO layer
 */
public ResponseEntity<Object> editSampleSchedulingFile(final SampleSchedulingFile objSampleSchedulingFile,
		final UserInfo objUserInfo) throws Exception;


/**
 * This service interface declaration will access the DAO layer that is used to
 * update the records in sampleschedulingfile
 * 
 * @param request  holding the date for update the file in sampleschedulingfile
 *                 table
 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
 *                 [int] primary key of site object for which the list is to be
 *                 fetched
 * @return a response entity which holds the list of sampleschedulingfile
 *         records with respect to site
 * @throws Exception that are thrown in the DAO layer
 */
public ResponseEntity<Object> updateSampleSchedulingFile(MultipartHttpServletRequest request,
		final UserInfo objUserInfo) throws Exception;


/**
 * This service interface declaration will access the DAO layer that is used to
 * delete the records in sampleschedulingfile
 * 
 * @param request  holding the date for delete the file in sampleschedulingfile
 *                 table
 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
 *                 [int] primary key of site object for which the list is to be
 *                 fetched
 * @return a response entity which holds the list of sampleschedulingfile
 *         records with respect to site
 * @throws Exception that are thrown in the DAO layer
 */
public ResponseEntity<Object> deleteSampleSchedulingFile(final SampleSchedulingFile objSampleSchedulingFile,
		final UserInfo objUserInfo) throws Exception;


/**
 * This service interface declaration will access the DAO layer that is used to
 * view the records in sampleschedulingfile
 * 
 * @param request  holding the date for view the file in sampleschedulingfile
 *                 table
 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
 *                 [int] primary key of site object for which the list is to be
 *                 fetched
 * @return a response entity which holds the list of sampleschedulingfile
 *         records with respect to site
 * @throws Exception that are thrown in the DAO layer
 */
public ResponseEntity<Object> viewAttachedSampleSchedulingFile(final SampleSchedulingFile objSampleSchedulingFile,
		final UserInfo objUserInfo) throws Exception;



public ResponseEntity<Object> getRegion(final UserInfo userInfo) throws Exception;

//public ResponseEntity<Object> getDistrict(final int nregioncode,
//		final UserInfo userInfo) throws Exception;

public ResponseEntity<Object> deleteSampleSchedulingLocation(Map<String, Object> objMap,
		final UserInfo objUserInfo) throws Exception;


}
