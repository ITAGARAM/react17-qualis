package com.agaramtech.qualis.biobank.service.bgsitransfer;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on 'bgsitransfer' table
 */
public interface BGSITransferService {

	/**
	 * This service interface declaration will access the DAO layer that is used 
	 * to get all the available bgsi transfer records
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of transfer records 
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getBGSITransfer(UserInfo userInfo) throws Exception;
	
	/**
	 * This service interface declaration will access the DAO layer that is used to retrieve active transfer details object based
	 * on the specified ntransferCode.
	 * @param ntransferCode [int] primary key of transfer object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of transfer details object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveTransferDertailsByTransferCode(final int ntransferCode,UserInfo userInfo) throws Exception ;
	
}
