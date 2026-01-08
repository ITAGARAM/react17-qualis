package com.agaramtech.qualis.samplescheduling.service.samplerequesting;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.samplescheduling.model.SampleRequesting;
import com.agaramtech.qualis.samplescheduling.model.SampleRequestingFile;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SampleRequestingServiceImpl implements SampleRequestingService{

	private final SampleRequestingDAO sampleRequestingDAO;
	private final CommonFunction commonFunction;

	public SampleRequestingServiceImpl(SampleRequestingDAO sampleRequestingDAO, CommonFunction commonFunction) {
		super();
		this.sampleRequestingDAO = sampleRequestingDAO;
		this.commonFunction = commonFunction;
	}

	public ResponseEntity<Object> getRegion(final UserInfo userInfo,int nsamplerequestingcode) throws Exception {
		return sampleRequestingDAO.getRegion(userInfo,nsamplerequestingcode);
	}
	
	 
	@Override
	public ResponseEntity<Object> getSubDivisionalLab(final int regionCode,final int districtCode,final UserInfo userInfo) throws Exception {

		return sampleRequestingDAO.getSubDivisionalLab(regionCode,districtCode,userInfo);

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
	public ResponseEntity<Object> getDistrictLab(final int regionCode,final UserInfo userInfo) throws Exception {

		return sampleRequestingDAO.getDistrictLab(regionCode,userInfo);

	}
	
	//modified by sujatha ATE_274 for get village also check the samplescheduling code
	@Override
	public ResponseEntity<Object> getVillageBasedOnSiteHierarchy(final int sampleschedulingcode, final int regionCode,final int districtCode, final int cityCode,final UserInfo userInfo) throws Exception {

		return sampleRequestingDAO.getVillageBasedOnSiteHierarchy(sampleschedulingcode, regionCode,districtCode,cityCode,userInfo);
	}
	
	
	
	
	@Override
	public ResponseEntity<Object> getPeriod(final String sfromyear,final UserInfo userInfo) throws Exception {

		return sampleRequestingDAO.getPeriod(sfromyear,userInfo);
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
	//modified by sujatha ATE_274 for get Location check 2 more condition using the fields
	@Override
	public ResponseEntity<Object> getLocation(final int sitehierarchyconfigcode, final int nsampleschedulingcode, final int regionCode,
			final int districtCode, final int cityCode, final int villageCode,final UserInfo userInfo) throws Exception {

		return sampleRequestingDAO.getLocation(sitehierarchyconfigcode, nsampleschedulingcode, regionCode,districtCode,cityCode,villageCode,userInfo);

	}
	
	//modified by sujatha ATE_274 SWSM-78 for one more check while getting villages
	//modified by sujatha ATE_274 SWSM-117 for add 2 more field for query validation to get the village
	@Override
	public ResponseEntity<Object> getVillages(final int nsampleschedulingcode, final int regionCode, final int districtCode,final int nprimarykey,final UserInfo userInfo) throws Exception {

		return sampleRequestingDAO.getVillages(nsampleschedulingcode,regionCode, districtCode, nprimarykey,userInfo);
	}
		
	@Transactional
	@Override
	public ResponseEntity<Object> createSampleRequesting(Map<String, Object> InputMap) throws Exception {
		return sampleRequestingDAO.createSampleRequesting(InputMap);
	}
	
	@Override
	public ResponseEntity<Object> getSampleRequesting(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return sampleRequestingDAO.getSampleRequesting(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getSampleRequestingData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return sampleRequestingDAO.getSampleRequestingData
				(inputMap, userInfo);
	}
	
	@Override
	public ResponseEntity<Object> getSampleRequestingRecord(final int nsamplerequestingcode, final UserInfo userInfo)
			throws Exception {
		return sampleRequestingDAO.getSampleRequestingRecord(nsamplerequestingcode, userInfo);

	}
	
	@Override
	public ResponseEntity<Object> getActiveSampleRequestingById(final int nsamplerequestingcode,
			final UserInfo userInfo) throws Exception {

		final SampleRequesting sampleRequesting = sampleRequestingDAO
				.getActiveSampleRequestingById(nsamplerequestingcode, userInfo);
		
		if (sampleRequesting == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(sampleRequesting, HttpStatus.OK);
		}
	}
	
	
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleRequesting(Map<String, Object> objMap) throws Exception {
		return sampleRequestingDAO.updateSampleRequesting(objMap);
	}
	
	
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSampleRequesting(Map<String, Object> objMap) throws Exception {
		return sampleRequestingDAO.deleteSampleRequesting(objMap);
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
	public ResponseEntity<Object> createSampleRequestingFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return sampleRequestingDAO.createSampleRequestingFile(request, objUserInfo);
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
	public ResponseEntity<Object> editSampleRequestingFile(final SampleRequestingFile objSampleRequestingFile,
			UserInfo objUserInfo) throws Exception {
		return sampleRequestingDAO.editSampleRequestingFile(objSampleRequestingFile, objUserInfo);
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
	public ResponseEntity<Object> updateSampleRequestingFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return sampleRequestingDAO.updateSampleRequestingFile(request, objUserInfo);
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
	public ResponseEntity<Object> deleteSampleRequestingFile(final SampleRequestingFile objSampleRequestingFile,
			final UserInfo objUserInfo) throws Exception {
		return sampleRequestingDAO.deleteSampleRequestingFile(objSampleRequestingFile, objUserInfo);
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
	public ResponseEntity<Object> viewAttachedSampleRequestingFile(SampleRequestingFile objSampleRequestingFile,
			UserInfo objUserInfo) throws Exception {
		return sampleRequestingDAO.viewAttachedSampleRequestingFile(objSampleRequestingFile, objUserInfo);
	}
	
	
	@Override
	public ResponseEntity<Object> getSampleRequestingLocation(final int nprimarykey,final UserInfo userInfo) throws Exception {

		return sampleRequestingDAO.getSampleRequestingLocation(nprimarykey,userInfo);

	}
	
	
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleRequestingLocation(Map<String, Object> InputMap) throws Exception {
		return sampleRequestingDAO.updateSampleRequestingLocation(InputMap);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSampleRequestingLocation(Map<String, Object> objMap,
			final UserInfo objUserInfo) throws Exception {
		return sampleRequestingDAO.deleteSampleRequestingLocation(objMap, objUserInfo);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> scheduledSampleRequesting(Map<String, Object> InputMap) throws Exception {
		// Modified by Gowtham on Oct 4 jira-id:SWSM-77 - to process 2 transactions Scheduling the sample and generate the sample report.
		// return sampleRequestingDAO.scheduledSampleRequesting(InputMap);
		ResponseEntity<Object> responseEntity = sampleRequestingDAO.scheduledSampleRequesting(InputMap);
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			sampleRequestingDAO.generateSampleScheduledReport(InputMap);
		}
		return responseEntity;
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> completedSampleRequesting(Map<String, Object> InputMap) throws Exception {
		return sampleRequestingDAO.completedSampleRequesting(InputMap);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> plannedSampleRequesting(Map<String, Object> InputMap) throws Exception {
		return sampleRequestingDAO.plannedSampleRequesting(InputMap);
	}
	
	@Transactional
	@Override
	public ResponseEntity<Object> createSampleRequestingLocation(Map<String, Object> InputMap) throws Exception {
		return sampleRequestingDAO.createSampleRequestingLocation(InputMap);
	}
	//Added by sonia on 4th oct 2025 for jira id:SWSM-77
	/**
	 * This service implementation method will access the DAO layer  is used Sent the Report By Mail.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status as success
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	public ResponseEntity<Object> sendReportByMail(Map<String, Object> inputMap, final UserInfo objUserInfo)throws Exception {
		if ((int) inputMap.get("ntransactionstatus") == Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus()) {
			return sampleRequestingDAO.sendReportByMail(inputMap, objUserInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PLEASESELECTSCHEDULEDRECORD",objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

		}
	}
}
