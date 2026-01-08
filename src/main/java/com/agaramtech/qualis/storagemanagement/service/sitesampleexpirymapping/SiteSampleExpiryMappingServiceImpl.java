package com.agaramtech.qualis.storagemanagement.service.sitesampleexpirymapping;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.SiteSampleExpiryMapping;

/**
 * This class holds methods to perform CRUD operation on 'siteexpirymapping' table through its DAO layer.
 */
/**
 * @author sujatha.v SWSM-14 31/08/2025
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SiteSampleExpiryMappingServiceImpl implements SiteSampleExpiryMappingService {

	private final SiteSampleExpiryMappingDAO siteSampleExpiryMappingDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param siteSampleExpiryMappingDAO SiteSampleExpiryMappingDAO Interface
	 * @param commonFunction             CommonFunction holding common utility
	 *                                   functions
	 */
	public SiteSampleExpiryMappingServiceImpl(SiteSampleExpiryMappingDAO siteSampleExpiryMappingDAO,
			CommonFunction commonFunction) {
		super();
		this.siteSampleExpiryMappingDAO = siteSampleExpiryMappingDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available siteexpirymapping with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of siteexpirymapping records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSiteSampleExpiryMapping(final UserInfo userInfo) throws Exception {
		return siteSampleExpiryMappingDAO.getSiteSampleExpiryMapping(userInfo);
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get period with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the period or list of period records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getPeriod(final UserInfo userInfo) throws Exception {
		return siteSampleExpiryMappingDAO.getPeriod(userInfo);
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available site 
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of site records 
	 * @throws Exception that are thrown in the DAO layer
	 */
	// added by sujatha ATE_274 to get the Login Site in the 1st value of dropdown on 01-09-2025
	@Override
	public ResponseEntity<Object> getSite(final UserInfo userInfo) throws Exception {
		return siteSampleExpiryMappingDAO.getSite(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active siteexpirymapping object based on the specified
	 * nsiteexpirymappingcode.
	 * 
	 * @param nsiteexpirymappingcode [int] primary key of siteexpirymapping object
	 * @param userInfo           [UserInfo] holding logged in user details based on
	 *                           which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         siteexpirymapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSiteSampleExpiryMappingById(final int nsiteexpirymappingcode,
			final UserInfo userInfo) throws Exception {
		final SiteSampleExpiryMapping objSiteSampleExpiryMapping = siteSampleExpiryMappingDAO
				.getActiveSiteSampleExpiryMappingById(nsiteexpirymappingcode, userInfo);
		if (objSiteSampleExpiryMapping == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(objSiteSampleExpiryMapping, HttpStatus.OK);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to siteexpirymapping table.
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
	@Transactional 
	@Override
	public ResponseEntity<Object> createSiteSampleExpiryMapping(
			final SiteSampleExpiryMapping objSiteSampleExpiryMapping, final UserInfo userInfo) throws Exception {
		return siteSampleExpiryMappingDAO.createSiteSampleExpiryMapping(objSiteSampleExpiryMapping, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in siteexpirymapping table.
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
	@Transactional
	@Override
	public ResponseEntity<Object> updateSiteSampleExpiryMapping(
			final SiteSampleExpiryMapping objSiteSampleExpiryMapping, final UserInfo userInfo) throws Exception {
		return siteSampleExpiryMappingDAO.updateSiteSampleExpiryMapping(objSiteSampleExpiryMapping, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in siteexpirymapping table.
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
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSiteSampleExpiryMapping(
			final SiteSampleExpiryMapping objSiteSampleExpiryMapping, final UserInfo userInfo) throws Exception {
		return siteSampleExpiryMappingDAO.deleteSiteSampleExpiryMapping(objSiteSampleExpiryMapping, userInfo);
	}
}
