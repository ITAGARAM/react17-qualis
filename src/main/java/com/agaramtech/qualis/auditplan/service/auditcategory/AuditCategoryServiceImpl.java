package com.agaramtech.qualis.auditplan.service.auditcategory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.auditplan.model.AuditCategory;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'auditcategory' table through its DAO layer.
 */
/**
 * @author sujatha.v
 * SWSM-3
 * 19/07/2025
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class AuditCategoryServiceImpl implements AuditCategoryService {

	private final AuditCategoryDAO auditCategoryDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param auditCategoryDAO AuditCategoryDAO Interface
	 * @param commonFunction   CommonFunction holding common utility functions
	 */
	public AuditCategoryServiceImpl(AuditCategoryDAO auditCategoryDAO, CommonFunction commonFunction) {
		this.auditCategoryDAO = auditCategoryDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available auditcategory with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of auditcategory records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getAuditCategory(UserInfo userInfo) throws Exception {
		return auditCategoryDAO.getAuditCategory(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * retrieve active auditcategory object based on the specified
	 * nauditcategorycode.
	 * 
	 * @param nauditcategorycode [int] primary key of auditcategory object
	 * @param userInfo           [UserInfo] holding logged in user details based on
	 *                           which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         auditcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@Override
	public ResponseEntity<Object> getActiveAuditCategoryById(int nauditcategorycode, UserInfo userInfo)
			throws Exception {
		final AuditCategory auditCategory = auditCategoryDAO.getActiveAuditCategoryById(nauditcategorycode, userInfo);
		if (auditCategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(auditCategory, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to auditcategory table.
	 * 
	 * @param objAuditCategory [AuditCategory] object holding details to be added in
	 *                         auditcategory table
	 * @param userInfo         [UserInfo] holding logged in user details and
	 *                         nmasterSiteCode [int] primary key of site object for
	 *                         which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         auditcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createAuditCategory(AuditCategory objAuditCategory, UserInfo userInfo)
			throws Exception {
		return auditCategoryDAO.createAuditCategory(objAuditCategory, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * update entry in auditcategory table.
	 * 
	 * @param objAuditCategory [AuditCategory] object holding details to be updated
	 *                         in auditcategory table
	 * @param userInfo         [UserInfo] holding logged in user details and
	 *                         nmasterSiteCode [int] primary key of site object for
	 *                         which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         auditcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateAuditCategory(AuditCategory objAuditCategory, UserInfo userInfo)
			throws Exception {
		return auditCategoryDAO.updateAuditCategory(objAuditCategory, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * delete an entry in auditcategory table.
	 * 
	 * @param objAuditCategory [AuditSCategory] object holding detail to be deleted
	 *                         from auditcategory table
	 * @param userInfo         [UserInfo] holding logged in user details and
	 *                         nmasterSiteCode [int] primary key of site object for
	 *                         which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         auditcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteAuditCategory(AuditCategory objAuditCategory, UserInfo userInfo)
			throws Exception {
		return auditCategoryDAO.deleteAuditCategory(objAuditCategory, userInfo);
	}
}
