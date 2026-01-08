package com.agaramtech.qualis.auditplan.service.auditmaster;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.auditplan.model.AuditMaster;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'auditmaster' table through its DAO layer.
 */
/**
* @author AT-E143 SWSM-2 22/07/2025
*/
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class AuditMasterServiceImpl implements AuditMasterService {

	private final AuditMasterDAO auditMasterDAO;
	private final CommonFunction commonFunction;	
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param auditMasterDAO AuditMasterDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public AuditMasterServiceImpl(AuditMasterDAO auditMasterDAO, CommonFunction commonFunction) {
		this.auditMasterDAO = auditMasterDAO;
		this.commonFunction = commonFunction;
	}	
	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available auditmaster with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of auditmaster records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getAuditMaster(UserInfo userInfo) throws Exception {		
		return auditMasterDAO.getAuditMaster(userInfo);
	}
	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active auditmaster object based
	 * on the specified nauditMasterCode.
	 * @param nauditMasterCode [int] primary key of auditmaster object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of auditmaster object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveAuditMasterById(int nauditMasterCode,UserInfo userInfo) throws Exception {		
		final AuditMaster auditmaster = auditMasterDAO.getActiveAuditMasterById(nauditMasterCode,userInfo);
		if (auditmaster == null) {
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else {
			return new ResponseEntity<>(auditmaster, HttpStatus.OK);
		}
	}
	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to auditmaster  table.
	 * @param objAuditMaster [AuditMaster] object holding details to be added in auditmaster table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added auditmaster object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	public ResponseEntity<Object> createAuditMaster(AuditMaster objAuditMaster,UserInfo userInfo) throws Exception {
		return auditMasterDAO.createAuditMaster(objAuditMaster,userInfo);
	}
	/**
	 * This service implementation method will access the DAO layer that is used to
	 *  update entry in auditmaster  table.
	 * @param objAuditMaster [AuditMaster] object holding details to be updated in auditmaster table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated auditmaster object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	public ResponseEntity<Object> updateAuditMaster(AuditMaster objAuditMaster,UserInfo userInfo) throws Exception {
		return auditMasterDAO.updateAuditMaster(objAuditMaster,userInfo);
	}
	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in auditmaster  table.
	 * @param objAuditMaster [AuditMaster] object holding detail to be deleted from auditmaster table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted auditmaster object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	public ResponseEntity<Object> deleteAuditMaster(AuditMaster objAuditMaster,UserInfo userInfo) throws Exception {		
		return auditMasterDAO.deleteAuditMaster(objAuditMaster,userInfo);
	}	
}
