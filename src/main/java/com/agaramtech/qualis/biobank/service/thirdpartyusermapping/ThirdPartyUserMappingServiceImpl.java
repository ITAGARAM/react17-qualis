package com.agaramtech.qualis.biobank.service.thirdpartyusermapping;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.biobank.model.ThirdParty;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service

/**
 * This class holds methods to perform CRUD operation on 'thirdparty' table
 * through its DAO layer.
 * 
 * @author Mullai Balaji.V BGSI-12 28/07/2025
 */
public class ThirdPartyUserMappingServiceImpl implements ThirdPartyUserMappingService {

	private final ThirdPartyUserMappingDAO thirdPartyUserMappingDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param thirdPartyUserMappingDAO ThirdPartyUserMappingDAO Interface
	 * @param commonFunction           CommonFunction holding common utility
	 *                                 functions
	 */
	public ThirdPartyUserMappingServiceImpl(ThirdPartyUserMappingDAO thirdPartyUserMappingDAO,
			CommonFunction commonFunction) {
		this.thirdPartyUserMappingDAO = thirdPartyUserMappingDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available thirdparty with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of thirdparty records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getThirdParty(final UserInfo userInfo) throws Exception {

		return thirdPartyUserMappingDAO.getThirdParty(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available thirdpartyusermapping with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of thirdpartyusermapping
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> getUserRole(final int nthirdpartycode,final UserInfo userInfo) throws Exception {

		return thirdPartyUserMappingDAO.getUserRole(nthirdpartycode,userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available thirdpartyusermapping with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of thirdpartyusermapping
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getUsers(final int nthirdpartycode, final int nuserrolecode,final UserInfo userInfo)
			throws Exception {

		return thirdPartyUserMappingDAO.getUsers(nthirdpartycode, nuserrolecode, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to thirdparty table.
	 * 
	 * @param objthirdparty [thirdparty] object holding details to be added in
	 *                      thirdparty table
	 * @param userInfo      [UserInfo] holding logged in user details and
	 *                      nmasterSiteCode [int] primary key of site object for
	 *                      which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         thirdparty object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createThirdParty(final ThirdParty objThirdParty,final UserInfo userInfo) throws Exception {

		return thirdPartyUserMappingDAO.createThirdParty(objThirdParty, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in thirdparty table.
	 * 
	 * @param objthirdparty [thirdparty] object holding details to be updated in
	 *                      thirdparty table
	 * @param userInfo      [UserInfo] holding logged in user details and
	 *                      nmasterSiteCode [int] primary key of site object for
	 *                      which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         thirdparty object
	 * @throws Exception that are thrown in the DAO layer
	 */
	// added nisngsConfirmed parameter by sujatha ATE_274 for delete validation in specific scenario BGSI-218
	@Transactional
	@Override
	public ResponseEntity<Object> updateThirdParty(final ThirdParty objThirdParty,final UserInfo userInfo, final String nisngsConfirmed) throws Exception {
		return thirdPartyUserMappingDAO.updateThirdParty(objThirdParty, userInfo, nisngsConfirmed);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active thirdparty object based on the specified nthirdpartyusermappingCode.
	 * 
	 * @param nthirdpartyCode [int] primary key of thirdparty object
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of thirdparty
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveThirdPartyById(final int nthirdpartycode,final UserInfo userInfo)
			throws Exception {

		final ThirdParty thirdParty = thirdPartyUserMappingDAO.getActiveThirdPartyById(nthirdpartycode, userInfo);
		if (thirdParty == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(thirdParty, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in thirdparty table.
	 * 
	 * @param objthirdparty [thirdparty] object holding detail to be deleted from
	 *                      thirdparty table
	 * @param userInfo      [UserInfo] holding logged in user details and
	 *                      nmasterSiteCode [int] primary key of site object for
	 *                      which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         thirdparty object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteThirdParty(final ThirdParty objThirdParty,final UserInfo userInfo) throws Exception {
		return thirdPartyUserMappingDAO.deleteThirdParty(objThirdParty, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to thirdparty table.
	 * 
	 * @param objthirdparty [thirdparty] object holding details to be added in
	 *                      thirdparty table
	 * @param userInfo      [UserInfo] holding logged in user details and
	 *                      nmasterSiteCode [int] primary key of site object for
	 *                      which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         thirdparty object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createUsers(Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {
	
		int nthirdpartycode = (int) inputMap.get("nthirdpartycode");
		int nuserrolecode = (int) inputMap.get("nuserrolecode");
		String nusercodeStr = (String) inputMap.get("nusercode");

		List<Integer> userCodes = Arrays.stream(nusercodeStr.split(",")).map(String::trim).map(Integer::parseInt)
				.collect(Collectors.toList());

		return thirdPartyUserMappingDAO.createUsers(nthirdpartycode, nuserrolecode, userCodes, userInfo);

	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active thirdparty object based on the specified nthirdpartyusermappingCode.
	 * 
	 * @param nthirdpartyCode [int] primary key of thirdparty object
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of thirdparty
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> getthirdpartyuser(final int nthirdpartycode,final UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return thirdPartyUserMappingDAO.getthirdpartyuser(nthirdpartycode, userInfo);

	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in thirdparty table.
	 * 
	 * @param objthirdparty [thirdparty] object holding detail to be deleted from
	 *                      thirdparty table
	 * @param userInfo      [UserInfo] holding logged in user details and
	 *                      nmasterSiteCode [int] primary key of site object for
	 *                      which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         thirdparty object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteUserRoleAndUser(final int nthirdpartycode, final int nusercode,
			final int nuserrolecode,final UserInfo userInfo) throws Exception {
		return thirdPartyUserMappingDAO.deleteUserRoleAndUser(nthirdpartycode, nusercode, nuserrolecode, userInfo);
	}

}
