package com.agaramtech.qualis.biobank.service.bgsitransfer;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.basemaster.model.Unit;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on 'unit' table
 */
public interface BGSITransferDAO {
	
	/**
	 * This interface declaration is used to get all the available bgsi transfer records.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of bgsi transfer records
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getBGSITransfer(UserInfo userInfo) throws Exception;
	
	/**
	 * This interface declaration is used to retrieve active bgsi transfer details object based
	 * on the specified ntransferCode.
	 * @param ntransferCode [int] primary key of transfer object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of transferdetails object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveTransferDertailsByTransferCode(final int ntransferCode,UserInfo userInfo) throws Exception;

}
