package com.agaramtech.qualis.submitter.service.villages;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.model.Villages;
import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'villages' table through its DAO layer.
 */
/**
 * @author sujatha.v
 * SWSM-4
 * 22/07/2025
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class VillagesServiceImpl implements VillagesService{

	private final VillagesDAO villageDAO;
	private final CommonFunction commonFunction;

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available village's with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of village records with respect
	 *         to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getVillage(UserInfo userInfo) throws Exception {
		return villageDAO.getVillage(userInfo);
	}
	
	//added by sujatha ATE_274 to get taluka from the approved version of site hierarchy config 
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available taluka from the sitehierarchyconfig detail table
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of taluka records with respect
	 *         to site hierarchy config screen
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getTaluka(UserInfo userInfo) throws Exception {
		return villageDAO.getTaluka(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to village table.
	 * 
	 * @param objVillage  [Villages] object holding details to be added in village table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of added village
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createVillage(Villages objVillage, UserInfo userInfo) throws Exception {
		return villageDAO.createVillage(objVillage, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active village object based on the specified nvillagecode.
	 * 
	 * @param nvillagecode [int] primary key of village object
	 * @param userInfo  [UserInfo] holding logged in user details based on which the
	 *                  list is to be fetched
	 * @return response entity object holding response status and data of village
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveVillageById(int nvillagecode, UserInfo userInfo) throws Exception {
		final Villages village = villageDAO.getActiveVillageById(nvillagecode, userInfo);
		if (village == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(village, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in villages table.
	 * 
	 * @param objVillage  [Villages] object holding details to be updated in village table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of updated
	 *         village object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateVillage(Villages objVillage, UserInfo userInfo) throws Exception {
		return villageDAO.updateVillage(objVillage, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in village table.
	 * 
	 * @param objVillage  [Villages] object holding detail to be deleted from villages table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of deleted
	 *         village object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteVillage(Villages objVillage, UserInfo userInfo) throws Exception {
		return villageDAO.deleteVillage(objVillage, userInfo);
	}
}
