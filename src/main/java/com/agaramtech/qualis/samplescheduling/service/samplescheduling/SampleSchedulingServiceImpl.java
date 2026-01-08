package com.agaramtech.qualis.samplescheduling.service.samplescheduling;


import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.samplescheduling.model.SampleScheduling;
import com.agaramtech.qualis.samplescheduling.model.SampleSchedulingFile;

/**
 * This class holds methods to perform CRUD operation on 'samplescheduling'
 * table through its DAO layer.
 * 
 * @author Mullai Balaji.V [SWSM-17] Sample Scheduling - Screen Development -
 *         Agaram Technologies
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SampleSchedulingServiceImpl implements SampleSchedulingService {

	private final SampleSchedulingDAO sampleSchedulingDAO;
	private final CommonFunction commonFunction;

	public SampleSchedulingServiceImpl(SampleSchedulingDAO sampleSchedulingDAO, CommonFunction commonFunction) {
		this.sampleSchedulingDAO = sampleSchedulingDAO;
		this.commonFunction = commonFunction;
	}

	
	/**
	 * This service implementation declaration is responsible for creating a new
	 * SampleScheduling record by accessing the DAO layer.
	 * 
	 * @param InputMap A map containing:
	 * 
	 *               Sample Scheduling details (e.g., fromYear, toYear, status,
	 *               etc.) UserInfo object holding logged-in user details
	 *               nmasterSiteCode [int] representing the primary key of the site
	 *               for which the record is to be created
	 * 
	 * @return ResponseEntity<Object> containing the status and the created Sample
	 *         Scheduling object (or error details if creation fails).
	 * @throws Exception if any error occurs in the DAO layer while creating the
	 *                   record
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSampleScheduling(Map<String, Object> InputMap) throws Exception {
		return sampleSchedulingDAO.createSampleScheduling(InputMap);
	}

	
	/**
	 * This service implementation declaration is responsible for creating a new
	 * SampleSchedulinglocation record by accessing the DAO layer.
	 * 
	 * @param InputMap A map containing:
	 * 
	 *               Sample Scheduling details (e.g., primarykey)
	 *                UserInfo object holding logged-in user details
	 *               nmasterSiteCode [int] representing the primary key of the site
	 *               for which the record is to be created
	 * 
	 * @return ResponseEntity<Object> containing the status and the created Sample
	 *         Scheduling object (or error details if creation fails).
	 * @throws Exception if any error occurs in the DAO layer while creating the
	 *                   record
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSampleSchedulingLocation(Map<String, Object> InputMap) throws Exception {
		return sampleSchedulingDAO.createSampleSchedulingLocation(InputMap);
	}
	
	
	
	/**
	 * Service interface method to mark a record as planned in the
	 * sampleschedulinghistory table.
	 *
	 * @param InputMap a map containing: - fromYear, toYear, and status details of the
	 *               complaint history - sampleschedulinghistory details - userInfo
	 *               (logged-in user details) - nmasterSiteCode (primary key of the
	 *               site object for which the list is fetched)
	 * @return ResponseEntity object containing the response status and data of the
	 *         updated sampleschedulinghistory record
	 * @throws Exception if any error occurs in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> plannedSampleScheduling(Map<String, Object> InputMap) throws Exception {
		return sampleSchedulingDAO.plannedSampleScheduling(InputMap);
	}
	
	/**
	 * This service implementation method provides access to the DAO layer to
	 * retrieve all available samplescheduling for a specific site, filtered by
	 * date range and status.
	 *
	 * @param inputMap a map containing filter criteria such as fromYear, toYear,
	 *                 and status
	 * @param userInfo an instance of [UserInfo] containing the logged-in user
	 *                 details and the nmasterSiteCode, which is the primary key of
	 *                 the site for which the complaints are to be fetched
	 *
	 * @return a ResponseEntity containing the list of sample scheduling records
	 *         associated with the specified site
	 *
	 * @throws Exception if any errors occur in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleSchedulingData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return sampleSchedulingDAO.getSampleSchedulingData(inputMap, userInfo);
	}
	
	
	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to update entry in samplescheduling table.
	 * 
	 * @param inputMap holding the date fromYear,toYear,Status samplescheduling
	 *                 details and userInfo [UserInfo] holding logged in user
	 *                 details and nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         samplescheduling object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleSchedulingLocation(Map<String, Object> InputMap) throws Exception {
		return sampleSchedulingDAO.updateSampleSchedulingLocation(InputMap);
	}
	
	
	/**
	 * This service implementation method provides access to the DAO layer to
	 * retrieve all available samplescheduling for a specific site.
	 *
	 * @param userInfo an instance of [UserInfo] containing the logged-in user
	 *                 details and the nmasterSiteCode, which is the primary key of
	 *                 the site for which the complaints are to be fetched
	 * @param inputMap a map containing filtering criteria such as fromYear, toYear,
	 *                 and status
	 *
	 * @return a ResponseEntity containing the list of sample scheduling records
	 *         associated with the specified site
	 *
	 * @throws Exception if any errors occur in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleScheduling(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return sampleSchedulingDAO.getSampleScheduling(inputMap, userInfo);
	}

	

	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to retrieve active samplescheduling object based on the
	 * specified nsampleschedulingode.
	 * 
	 * @param nsampleschedulingode [int] primary key of samplescheduling object
	 * @param userInfo               [UserInfo] holding logged in user details based
	 *                               on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         samplescheduling object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSampleSchedulingById(final int nsampleschedulingcode,
			final UserInfo userInfo) throws Exception {

		final SampleScheduling sampleScheduling = sampleSchedulingDAO
				.getActiveSampleSchedulingById(nsampleschedulingcode, userInfo);
		
		if (sampleScheduling == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(sampleScheduling, HttpStatus.OK);
		}
	}
	
	
	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to update entry in samplescheduling table.
	 * 
	 * @param inputMap holding the date fromYear,toYear,Status samplescheduling
	 *                 details and userInfo [UserInfo] holding logged in user
	 *                 details and nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         samplescheduling object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleScheduling(Map<String, Object> objMap) throws Exception {
		return sampleSchedulingDAO.updateSampleScheduling(objMap);
	}

	
	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to retrieve active sampleschedulinghistory object based on the
	 * specified nsampleschedulingcode.
	 * 
	 * @param nsampleschedulingcode [int] primary key of samplescheduling object
	 * @param userInfo               [UserInfo] holding logged in user details based
	 *                               on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         samplescheduling object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleSchedulingRecord(final int nsampleschedulingcode, final UserInfo userInfo)
			throws Exception {
		return sampleSchedulingDAO.getSampleSchedulingRecord(nsampleschedulingcode, userInfo);

	}
	
	
	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to delete an entry in samplescheduling table.
	 * 
	 * @param inputMap holding the date fromYear,toYear,Status samplescheduling
	 *                 details and userInfo [UserInfo] holding logged in user
	 *                 details and nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         samplescheduling object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSampleScheduling(Map<String, Object> objMap) throws Exception {
		return sampleSchedulingDAO.deleteSampleScheduling(objMap);
	}
	
	
	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to create the records in sampleschedulingfile
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
	@Transactional
	@Override
	public ResponseEntity<Object> createSampleSchedulingFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return sampleSchedulingDAO.createSampleSchedulingFile(request, objUserInfo);
	}
	
	
	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to create the records in sampleschedulingfile
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
	@Override
	public ResponseEntity<Object> editSampleSchedulingFile(final SampleSchedulingFile objSampleSchedulingFile,
			UserInfo objUserInfo) throws Exception {
		return sampleSchedulingDAO.editSampleSchedulingFile(objSampleSchedulingFile, objUserInfo);
	}
	
	
	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to update the records in sampleschedulingfile
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
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleSchedulingFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return sampleSchedulingDAO.updateSampleSchedulingFile(request, objUserInfo);
	}
	
	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to delete the records in sampleschedulingfile
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
	
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSampleSchedulingFile(final SampleSchedulingFile objSampleSchedulingFile,
			final UserInfo objUserInfo) throws Exception {
		return sampleSchedulingDAO.deleteSampleSchedulingFile(objSampleSchedulingFile, objUserInfo);
	}
	
	
	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to view the records in sampleschedulingfile
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
	@Transactional
	@Override
	public ResponseEntity<Object> viewAttachedSampleSchedulingFile(SampleSchedulingFile objSampleSchedulingFile,
			UserInfo objUserInfo) throws Exception {
		return sampleSchedulingDAO.viewAttachedSampleSchedulingFile(objSampleSchedulingFile, objUserInfo);
	}
	
	
	
	/**
	 * This service implementation method provides access to the DAO layer to
	 * retrieve all the available regions .
	 *
	 * @param userInfo [UserInfo] contains the details of the logged-in user,
	 *                 including the nmasterSiteCode [int], which represents the
	 *                 primary key of the site for which the region list is to be
	 *                 fetched.
	 * @return a ResponseEntity containing the list of region records associated
	 *         with the site.
	 * @throws Exception if any errors occur in the DAO layer during data retrieval.
	 */
	public ResponseEntity<Object> getRegion(final UserInfo userInfo) throws Exception {
		return sampleSchedulingDAO.getRegion(userInfo);
	}
	
	
	/**
	 * This service implementation method provides access to the DAO layer to
	 * retrieve all the available districts for a given region within a specific
	 * City or SubDivisionalLab
	 *
	 * @param nprimarykey   an integer representing the unique region code for which
	 *                      the district list is to be fetched.
	 * @param userInfo      [UserInfo] object containing the logged-in user details,
	 *                      including nmasterSiteCode [int], which is the primary
	 *                      key of the site object.
	 * @return a ResponseEntity containing the list of district records associated
	 *         with the given region 
	 * @throws Exception if any errors occur in the DAO layer during data retrieval.
	 */
	@Override
	public ResponseEntity<Object> getSubDivisionalLab(final int nprimarykey,final UserInfo userInfo) throws Exception {

		return sampleSchedulingDAO.getSubDivisionalLab(nprimarykey,userInfo);

	}
	
	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to get all the available Region with respect to District
	 * 
	 * @param nprimarykey   Holding the current Region record
	 * @param userInfo      [UserInfo] holding logged in user details and
	 *                      nmasterSiteCode [int] primary key of site object for
	 *                      which the list is to be fetched
	 * @return a response entity which holds the list of samplescheduling records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getDistrictLab(final int nprimarykey,final UserInfo userInfo) throws Exception {

		return sampleSchedulingDAO.getDistrictLab(nprimarykey,userInfo);

	}
	
	
	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to get all the available Village records with respect to City.
	 *
	 * @param nprimarykey     Holding the City or SubDivisionalLab Record (foreign key reference for
	 *                      village lookup)
	 * @param userInfo      [UserInfo] holding logged-in user details including
	 *                      nmasterSiteCode [int], which is the primary key of the
	 *                      site object for which the list is to be fetched
	 * @return a response entity which holds the list of village records with
	 *         respect to the given city 
	 * @throws Exception if any error occurs in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getLocation(final int nprimarykey,final UserInfo userInfo) throws Exception {

		return sampleSchedulingDAO.getLocation(nprimarykey,userInfo);

	}
	
	
	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to get all the available Locations records.
	 *
	 * @param nprimarykey     Holding the samplescheduling  Record
	 *                      
	 * @param userInfo      [UserInfo] holding logged-in user details including
	 *                      nmasterSiteCode [int], which is the primary key of the
	 *                      site object for which the list is to be fetched
	 * @return a response entity which holds the list of location records with
	 *         respect to the given location 
	 * @throws Exception if any error occurs in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleSchedulingLocation(final int nprimarykey,final UserInfo userInfo) throws Exception {

		return sampleSchedulingDAO.getSampleSchedulingLocation(nprimarykey,userInfo);

	}
	

	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to get all the available Village records with respect to City.
	 *
	 * @param nprimarykey     Holding the City record (foreign key reference for
	 *                      village lookup)
	 * @param userInfo      [UserInfo] holding logged-in user details including
	 *                      nmasterSiteCode [int], which is the primary key of the
	 *                      site object for which the list is to be fetched
	 * @return a response entity which holds the list of village records with
	 *         respect to the given city 
	 * @throws Exception if any error occurs in the DAO layer
	 */
	//modified by sujatha ATE_274 SWSM-117 by adding nsitehierarchyconfigcode for getting the approved site hierarchy config villages
	@Override
	public ResponseEntity<Object> getVillage(final int nprimarykey,final UserInfo userInfo, final int nsitehierarchyconfigcode) throws Exception {

		return sampleSchedulingDAO.getVillage(nprimarykey,userInfo, nsitehierarchyconfigcode);
	}
		
	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to delete an entry in sampleschedulinglocation table.
	 * 
	 * @param inputMap holding the date fromYear,toYear,Status sampleschedulinglocation
	 *                 details and userInfo [UserInfo] holding logged in user
	 *                 details and nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         sampleschedulinglocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSampleSchedulingLocation(Map<String, Object> objMap,
			final UserInfo objUserInfo) throws Exception {
		return sampleSchedulingDAO.deleteSampleSchedulingLocation(objMap, objUserInfo);
	}

}
