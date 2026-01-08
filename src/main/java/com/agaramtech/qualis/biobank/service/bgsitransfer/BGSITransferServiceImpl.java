package com.agaramtech.qualis.biobank.service.bgsitransfer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.basemaster.model.Unit;
import com.agaramtech.qualis.biobank.service.bgsitransfer.BGSITransferDAO;
import com.agaramtech.qualis.biobank.service.bgsitransfer.BGSITransferService;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'transfer' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class BGSITransferServiceImpl implements BGSITransferService {
	
	private final BGSITransferDAO transferDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param transferDAO BGSITransferDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public BGSITransferServiceImpl(BGSITransferDAO transferDAO, CommonFunction commonFunction) {
		this.transferDAO = transferDAO;
		this.commonFunction = commonFunction;
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available transfer records
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of transfer records
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getBGSITransfer(UserInfo userInfo) throws Exception {
		
		return transferDAO.getBGSITransfer(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active transfer details object based
	 * on the specified ntransferCode.
	 * @param ntransferCode [int] primary key of transfer object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of transfer details object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveTransferDertailsByTransferCode(int ntransferCode,UserInfo userInfo) throws Exception {
		
		return transferDAO.getActiveTransferDertailsByTransferCode(ntransferCode,userInfo);
		
	}
}
