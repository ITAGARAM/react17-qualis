package com.agaramtech.qualis.auditplan.service.auditstandardcategory;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.auditplan.model.AuditStandardCategory;
import com.agaramtech.qualis.global.UserInfo;
/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'auditstandardcategory' table
 */
/**
 * @author sujatha.v
 * SWSM-1
 * 19/07/2025
 */
public interface AuditStandardCategoryService {

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available auditstandardcategory's with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of auditstandardcategory records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getAuditStandardCategory(final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * retrieve active auditstandardcategory object based on the specified
	 * nauditstandardcatcode.
	 * 
	 * @param nauditstandardcatcode [int] primary key of auditstandardcategory object
	 * @param userInfo             [UserInfo] holding logged in user details based
	 *                             on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         auditstandardcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveAuditStandardCategoryById(final int nauditstandardcatcode,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to auditstandardcategory table.
	 * 
	 * @param objAuditStandardCategory [AuditStandardCategory] object holding details to be
	 *                           added in auditstandardcategory table
	 * @param userInfo           [UserInfo] holding logged in user details and
	 *                           nmasterSiteCode [int] primary key of site object
	 *                           for which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         auditstandardcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createAuditStandardCategory(final AuditStandardCategory objAuditStandardCategory,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * update entry in auditstandardcategory table.
	 * 
	 * @param objAuditStandardCategory [AuditStandardCategory] object holding details to be
	 *                           updated in auditstandardcategory table
	 * @param userInfo           [UserInfo] holding logged in user details and
	 *                           nmasterSiteCode [int] primary key of site object
	 *                           for which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         auditstandardcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateAuditStandardCategory(final AuditStandardCategory objAuditStandardCategory,
			final UserInfo userInfo) throws Exception;

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * delete an entry in auditstandardcategory table.
	 * 
	 * @param objAuditStandardCategory [AuditStandardCategory] object holding detail to be
	 *                           deleted from auditstandardcategory table
	 * @param userInfo           [UserInfo] holding logged in user details and
	 *                           nmasterSiteCode [int] primary key of site object
	 *                           for which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         auditstandardcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteAuditStandardCategory(final AuditStandardCategory objAuditStandardCategory,
			final UserInfo userInfo) throws Exception;

}
