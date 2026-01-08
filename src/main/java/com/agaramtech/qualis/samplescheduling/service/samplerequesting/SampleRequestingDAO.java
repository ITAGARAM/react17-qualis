package com.agaramtech.qualis.samplescheduling.service.samplerequesting;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.samplescheduling.model.SampleRequesting;
import com.agaramtech.qualis.samplescheduling.model.SampleRequestingFile;

public interface SampleRequestingDAO {

	public ResponseEntity<Object> getRegion(final UserInfo userInfo,int nsamplerequestingcode) throws Exception;
	/**
	 * This interface method provides access to the DAO layer to retrieve all the
	 * available City within a specific District.
	 *
	 * @param nprimarykey an integer representing the unique region code for which
	 *                    the district list is to be fetched.
	 * @param userInfo    [UserInfo] object containing the logged-in user details,
	 *                    including nmasterSiteCode [int], which is the primary key
	 *                    of the site object.
	 * @return a ResponseEntity containing the list of district records associated
	 *         with the given region
	 * @throws Exception if any errors occur in the DAO layer during data retrieval.
	 */
	public ResponseEntity<Object> getSubDivisionalLab(final int regionCode,final int districtCode,final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration will access the DAO layer that is used to get all
	 * Distircts based on the available Region
	 * 
	 * @param nprimarykey Holding the current Region record
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return a response entity which holds the list of samplescheduling records
	 * 
	 * @throws Exception that are thrown in the DAO layer	
	 */
	
	public ResponseEntity<Object> getDistrictLab(final int regionCode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration will access the DAO layer that is used to get all
	 * Locations based on the available in SampeLocation
	 * 
	 * @param nprimarykey Holding the current Region record
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return a response entity which holds the list of samplescheduling records
	 * 
	 * @throws Exception that are thrown in the DAO layer
	 */
	//modified by sujatha ATE_274 SWSM-78 for one more check while getting location
	public ResponseEntity<Object> getLocation(final int sitehierarchyconfigcode, final int nsampleschedulingcode, final int regionCode,
			final int districtCode, final int cityCode,final int villageCode, final UserInfo userInfo) throws Exception;

	//modified by sujatha ATE_274 SWSM-78 for one more check while getting villages
	public ResponseEntity<Object> getVillageBasedOnSiteHierarchy(final int sampleschedulingcode, final int regionCode,final int districtCode,final int cityCode,final UserInfo userInfo) throws Exception;
	
	public ResponseEntity<Object> getPeriod(final String sfromyear, final UserInfo userInfo) throws Exception;

	//modified by sujatha ATE_274 SWSM-78 for one more check while getting villages
	//added by sujatha ATE_274 SWSM-117 24-10-2025 by getting  villages by checking districtcode & regionCode
	public ResponseEntity<Object> getVillages(final int nsampleschedulingcode, final int regionCode, final int districtCode,final int nprimarykey, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createSampleRequesting(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> getSampleRequesting(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;
	public ResponseEntity<Object> getSampleRequestingData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> getSampleRequestingRecord(final int nsamplerequestingcode, final UserInfo userInfo)
			throws Exception;

	public SampleRequesting getActiveSampleRequestingById(final int nsamplerequestingcode, final UserInfo userInfo)
			throws Exception;
	
	public ResponseEntity<Object> updateSampleRequesting(Map<String, Object> objMap) throws Exception;
	
	public ResponseEntity<Object> deleteSampleRequesting(Map<String, Object> objMap) throws Exception;

	/**
	 * This interface declaration will access the DAO layer that is used to create
	 * the records in sampleschedulingfile
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
	public ResponseEntity<Object> createSampleRequestingFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception;

	/**
	 * This interface declaration will access the DAO layer that is used to create
	 * the records in sampleschedulingfile
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
	public ResponseEntity<Object> editSampleRequestingFile(final SampleRequestingFile objSampleRequestingFile,
			final UserInfo objUserInfo) throws Exception;

	/**
	 * This interface declaration will access the DAO layer that is used to update
	 * the records in sampleschedulingfile
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
	public ResponseEntity<Object> updateSampleRequestingFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception;

	/**
	 * This interface declaration will access the DAO layer that is used to delete
	 * the records in sampleschedulingfile
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
	public ResponseEntity<Object> deleteSampleRequestingFile(final SampleRequestingFile objSampleRequestingFile,
			final UserInfo objUserInfo) throws Exception;

	/**
	 * This interface declaration will access the DAO layer that is used to view the
	 * records in sampleschedulingfile
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
	public ResponseEntity<Object> viewAttachedSampleRequestingFile(final SampleRequestingFile objSampleRequestingFile,
			final UserInfo objUserInfo) throws Exception;

	
	public ResponseEntity<Object> getSampleRequestingLocation(final int nprimarykey, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateSampleRequestingLocation(Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> deleteSampleRequestingLocation(Map<String, Object> objMap, final UserInfo objUserInfo)
			throws Exception;
	
	public ResponseEntity<Object> scheduledSampleRequesting(Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> completedSampleRequesting(Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> plannedSampleRequesting(Map<String, Object> inputMap) throws Exception;
	
	public ResponseEntity<Object> createSampleRequestingLocation(Map<String, Object> inputMap) throws Exception;
	//Added by sonia on 4th oct 2025 for jira id:SWSM-77
	/**
	 * This DAO interface declaration will access the DAO layer that is used Sent the Report By Mail.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status as success
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> sendReportByMail(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	// Added by Gowtham on Oct 4 jira-id:SWSM-77

	/**
	 * Added by Gowtham on Oct 4 to generate SampleSchedulingReport jira-id:SWSM-77
	 * 
	 * This method will access the DAO layer that is used to generate SampleSchedulingReport
	 * and insert the file location in samplerequestingreporthistory table.
	 * 
	 * @param inputMap   			[Map<String, Object>] Map holding data to generate
	 * 								SampleSchedulingReport and insert 
	 * 								into samplerequestingreporthistory table
	 * 
	 * @throws Exception that are thrown in the DAO layer
	 */
	public void generateSampleScheduledReport(Map<String, Object> inputMap) throws Exception;

}
	