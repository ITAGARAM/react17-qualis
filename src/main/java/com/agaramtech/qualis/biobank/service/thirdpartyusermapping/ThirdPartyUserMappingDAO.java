package com.agaramtech.qualis.biobank.service.thirdpartyusermapping;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.biobank.model.ThirdParty;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'thirdparty'
 * 
 * @author Mullai Balaji.V BGSI-12 28/07/2025
 */
public interface ThirdPartyUserMappingDAO {

	/**
	 * This interface declaration is used to get all the available thirdparty with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of thirdparty records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getThirdParty(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get all the available
	 * thirdpartyusermapping with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of thirdpartyusermapping
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getUserRole(final int nthirdpartycode,final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get all the available
	 * thirdpartyusermapping with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of thirdpartyusermapping
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> getUsers(final int nthirdpartycode, final int nuserrolecode,final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to thirdparty table.
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

	public ResponseEntity<Object> createThirdParty(final ThirdParty objThirdParty,final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in thirdparty table.
	 * 
	 * @param objThirdparty [thirdparty] object holding details to be updated in
	 *                      thirdparty table
	 * @param userInfo      [UserInfo] holding logged in user details and
	 *                      nmasterSiteCode [int] primary key of site object for
	 *                      which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         thirdparty object
	 * @throws Exception that are thrown in the DAO layer
	 */
	// added nisngsConfirmed parameter by sujatha ATE_274 for delete validation in specific scenario BGSI-185
	public ResponseEntity<Object> updateThirdParty(final ThirdParty objThirdParty,final UserInfo userInfo, final String nisngsConfirmed) throws Exception;

	/**
	 * This interface declaration is used to retrieve active thirdparty object based
	 * on the specified nthirdpartyCode.
	 * 
	 * @param nthirdpartyCode [int] primary key of thirdparty object
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of thirdparty
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ThirdParty getActiveThirdPartyById(final int nthirdpartycode,final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete an entry in thirdparty table.
	 * 
	 * @param objDisease [thirdparty] object holding detail to be deleted from
	 *                   thirdparty table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         thirdparty object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteThirdParty(final ThirdParty objThirdParty,final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to
	 * thirdpartyusermapping table.
	 * 
	 * @param objThirdpartyusermapping [thirdpartyusermapping] object holding
	 *                                 details to be added in thirdpartyusermapping
	 *                                 table
	 * @param userInfo                 [UserInfo] holding logged in user details and
	 *                                 nmasterSiteCode [int] primary key of site
	 *                                 object for which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         thirdpartyusermapping object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createUsers(final int nthirdpartycode,final  int nuserrolecode, List<Integer> userCodes,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active thirdparty object based
	 * on the specified nthirdpartyCode.
	 * 
	 * @param nthirdpartyCode [int] primary key of thirdparty object
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of thirdparty
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getthirdpartyuser(final int nuserrolecode,final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete an entry in thirdparty table.
	 * 
	 * @param objDisease [thirdparty] object holding detail to be deleted from
	 *                   thirdparty table
	 * @param userInfo   [UserInfo] holding logged in user details and
	 *                   nmasterSiteCode [int] primary key of site object for which
	 *                   the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         thirdparty object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteUserRoleAndUser(final int nthirdpartycode, final int nusercode,
			final int nuserrolecode,final UserInfo userInfo) throws Exception;
}
