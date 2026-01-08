package com.agaramtech.qualis.auditplan.service.auditcategory;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.auditplan.model.AuditCategory;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'auditcategory' table
 */
/**
 * @author sujatha.v
 * SWSM-3
 * 19/07/2025
 */
public interface AuditCategoryDAO {

	/**
	 * This interface declaration is used to get all the available auditcategory's with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of auditcategory records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getAuditCategory(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active auditcategory object based
	 * on the specified nauditcategorycode.
	 * @param nauditcategorycode [int] primary key of auditcategory object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of auditcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public AuditCategory getActiveAuditCategoryById(final int nauditcategorycode,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to auditcategory table.
	 * @param objAuditCategory [AuditCategory] object holding details to be added in auditcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added auditcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createAuditCategory(final AuditCategory objAuditCategory,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in auditcategory table.
	 * @param objAuditCategory [AuditCategory] object holding details to be updated in auditcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated auditcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateAuditCategory(final AuditCategory objAuditCategory,
			final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete an entry in auditcategory table.
	 * @param objAuditCategory [AuditCategory] object holding detail to be deleted from auditcategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted auditcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteAuditCategory(final AuditCategory objAuditCategory,
			final UserInfo userInfo) throws Exception;
}
