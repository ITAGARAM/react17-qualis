package com.agaramtech.qualis.samplescheduling.service.samplelocation;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.samplescheduling.model.SampleLocation;

/**
 * This class holds methods to perform CRUD operation on 'samplelocation' table through its DAO layer.
 */
/**
 * @author sujatha.v AT-E274
 * SWSM-5
 * 24/07/2025
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
//@RequiredArgsConstructor
public class SampleLocationServiceImpl implements SampleLocationService {

	private final SampleLocationDAO sampleLocationDAO;
	private final CommonFunction commonFunction;
	
	public SampleLocationServiceImpl(SampleLocationDAO sampleLocationDAO, CommonFunction commonFunction) {
		super();
		this.sampleLocationDAO = sampleLocationDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available samplelocation's based on region, district, taluk and villages
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of samplelocation records with respect
	 *         to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleLocation(final UserInfo userInfo) throws Exception {
		return sampleLocationDAO.getSampleLocation(userInfo);
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to samplelocation table.
	 * 
	 * @param objSampleLocation  [SampleLocation] object holding details to be added in samplelocation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of added samplelocation
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSampleLocation(final SampleLocation objSampleLocation, final UserInfo userInfo)
			throws Exception {
		return sampleLocationDAO.createSampleLocation(objSampleLocation, userInfo);
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active samplelocation object based on the specified nsamplelocationcode.
	 * 
	 * @param nsamplelocationcode [int] primary key of samplelocation object
	 * @param userInfo  [UserInfo] holding logged in user details based on which the
	 *                  list is to be fetched
	 * @return response entity object holding response status and data of samplelocation
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSampleLocationById(final int nsamplelocationcode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final SampleLocation sampleLocation = sampleLocationDAO.getActiveSampleLocationById(nsamplelocationcode);
		if (sampleLocation == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
		    outputMap.put("activeSampleLocationById", sampleLocation);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in samplelocation table.
	 * 
	 * @param objSampleLocation  [SampleLocation] object holding details to be updated in samplelocation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of updated
	 *         samplelocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleLocation(final SampleLocation objSampleLocation, final UserInfo userInfo)
			throws Exception {
		return sampleLocationDAO.updateSampleLocation(objSampleLocation, userInfo);
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in samplelocation table.
	 * 
	 * @param objSampleLocation  [SampleLocation] object holding detail to be deleted from samplelocation table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of deleted
	 *         samplelocation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSampleLocation(final SampleLocation objSampleLocation, final UserInfo userInfo)
			throws Exception {
		return sampleLocationDAO.deleteSampleLocation(objSampleLocation, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available region's with respect to the approved site hierarchy configuration
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of region records with respect
	 *         to approved site hierarchy configuration
	 * @throws Exception that are thrown in the DAO layer
	 */
//	@Override
//	public ResponseEntity<Object> getRegion(UserInfo userInfo) throws Exception {
//		return sampleLocationDAO.getRegion(userInfo);
//	}
	//added by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
	@Override
	public ResponseEntity<Object> getRegion(final UserInfo userInfo) throws Exception {
		return sampleLocationDAO.getRegion(userInfo);
	}
	
	//commented by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
//	@Override
//	public ResponseEntity<Object> getVillage(final int nprimarykey,final UserInfo userInfo) throws Exception {
//		return sampleLocationDAO.getVillage(nprimarykey,userInfo);
//	}
	
	
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available taluka based on selected district mapped in the site hierarchy configuration
	 * 
	 * @nprimarykey  holding the selected district
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of taluk records with respect
	 *         to approved site hierarchy configuration and load's based on the selected district.
	 * @throws Exception that are thrown in the DAO layer
	 */
	//added by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
	@Override
	public ResponseEntity<Object> getTaluka(final int nprimarykey,final UserInfo userInfo) throws Exception {
		return sampleLocationDAO.getTaluka(nprimarykey,userInfo);
	}
	
	
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available district's based on approved site hierarchy configuration and selected region
	 * 
	 * @nprimarykey  holding the selected region
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of district records with respect
	 *         to approved site hierarchy configuration and selected region
	 * @throws Exception that are thrown in the DAO layer
	 */
	//commented by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
//	@Override
//	public ResponseEntity<Object> getDistrict(final Map<String,Object> inputMap, final UserInfo userInfo) throws Exception {
//		return sampleLocationDAO.getDistrict(inputMap, userInfo);
//	}
	
	//added by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
		@Override
		public ResponseEntity<Object> getDistrict(final int nprimarykey,final UserInfo userInfo) throws Exception {
	 
			return sampleLocationDAO.getDistrict(nprimarykey,userInfo);
	 
		}
	
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available taluk based on selected district
	 * 
	 * @inputMap [Map<String, Object] holding the select details of district
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of taluk records with respect
	 *         to site and load's based on the selected district.
	 * @throws Exception that are thrown in the DAO layer
	 */
	//commented by sujatha v ATE_274 SWSM-5 on 21-09-2025 for samplelocation(based on site hierarchy configuration)
//	@Override
//	public ResponseEntity<Object> getCity(final Map<String,Object> inputMap, final UserInfo userInfo) throws Exception {
//		return sampleLocationDAO.getCity(inputMap, userInfo);
//	}
	
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available villages based on the selected taluk
	 * 
	 * @inputMap [Map<String, Object] holding the select details of taluk
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of taluk records with respect
	 *         to site and load's based on the selected taluk.
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getVillage(final Map<String,Object> inputMap, final UserInfo userInfo) throws Exception {
		return sampleLocationDAO.getVillage(inputMap, userInfo);
	}
	
}
