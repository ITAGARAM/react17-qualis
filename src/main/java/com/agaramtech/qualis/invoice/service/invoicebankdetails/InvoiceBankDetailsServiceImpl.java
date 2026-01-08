package com.agaramtech.qualis.invoice.service.invoicebankdetails;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.InvoiceBankDetails;

/**
 * This class holds methods to perform CRUD operation on 'InvoiceBankDetails'
 * table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class InvoiceBankDetailsServiceImpl implements InvoiceBankDetailsService {

	private final InvoiceBankDetailsDAO invoiceBankDetailsDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param InvoiceBankDetailsDAO InvoiceBankDetailsDAO Interface
	 * @param commonFunction        CommonFunction holding common utility functions
	 */
	public InvoiceBankDetailsServiceImpl(InvoiceBankDetailsDAO invoiceBankDetailsDAO, CommonFunction commonFunction) {
		this.invoiceBankDetailsDAO = invoiceBankDetailsDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available InvoiceBankDetailss with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of InvoiceBankDetails records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getBankDetails(UserInfo userInfo) throws Exception {

		return invoiceBankDetailsDAO.getBankDetails(userInfo);
	}

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * add a new entry to InvoiceBankDetails table.
	 * 
	 * @param objInvoiceBankDetails [InvoiceBankDetails] object holding details to
	 *                              be added in InvoiceBankDetails table
	 * @param userInfo              [UserInfo] holding logged in user details and
	 *                              nmasterSiteCode [int] primary key of site object
	 *                              for which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         InvoiceBankDetails object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createBankDetails(InvoiceBankDetails objBankDetails, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return invoiceBankDetailsDAO.createBankDetails(objBankDetails, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used
	 * This method is used to retrieve active InvoiceBankdetail object based on the
	 * specified nbankcode.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of InvoiceBankDetails records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveBankDetailsById(int bankId, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		final InvoiceBankDetails BankDetails = invoiceBankDetailsDAO.getActiveBankDetailsById(bankId, userInfo);
		if (BankDetails == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(BankDetails, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to update entry in InvoiceBankDetails table. Need to
	 * validate that the BankDetails object to be updated is active before updating
	 * details in database. Need to check for duplicate entry of bank name for the
	 * specified site before saving into database. Need to check that there should
	 * be only one default InvoiceBankDetails for a site
	 * 
	 * @param BankDetailsDAO [InvoiceBankDetails] object holding details to be
	 *                       updated in InvoiceBankDetails table
	 * @param userInfo       [UserInfo] holding logged in user details based on
	 *                       which the list is to be fetched
	 * @return saved BankDetails object with status code 200 if saved successfully
	 *         else if the BankDetails already exists, response will be returned as
	 *         'Already Exists' with status code 409 else if the BankDetails to be
	 *         updated is not available, response will be returned as 'Already
	 *         Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateBankDetails(InvoiceBankDetails objBankDetails, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return invoiceBankDetailsDAO.updateBankDetails(objBankDetails, userInfo);
	}

	/**
	 * This method id used to delete an entry in InvoiceBankDetails table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as 'InvoiceBankDetails'
	 * 
	 * @param BankDetailsDAO [InvoiceBankDetails] an Object holds the record to be
	 *                       deleted
	 * @param userInfo       [UserInfo] holding logged in user details based on
	 *                       which the list is to be fetched
	 * @return a response entity with list of available InvoiceBankDetails objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteBankDetails(InvoiceBankDetails objBankDetails, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return invoiceBankDetailsDAO.deleteBankDetails(objBankDetails, userInfo);
	}

}
