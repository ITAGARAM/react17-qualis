package com.agaramtech.qualis.invoice.service.invoicebankdetails;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.InvoiceBankDetails;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'InvoiceBankDetails' table
 */
public interface InvoiceBankDetailsService {

	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available bankdetails with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of InvoiceBankDetails records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getBankDetails(UserInfo userInfo) throws Exception;

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
	public ResponseEntity<Object> createBankDetails(InvoiceBankDetails objBankDetails, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active InvoiceBankDetails
	 * object based
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of InvoiceBankDetails records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> getActiveBankDetailsById(int bankId, UserInfo userInfo) throws Exception;

	/**
	 * This method is used to update entry in InvoiceBankDetails table. Need to
	 * validate that the BankDetails object to be updated is active before updating
	 * details in database. Need to check for duplicate entry of Bank name for the
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
	public ResponseEntity<Object> updateBankDetails(InvoiceBankDetails objBankDetails, UserInfo userInfo)
			throws Exception;

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
	public ResponseEntity<Object> deleteBankDetails(InvoiceBankDetails objBankDetails, UserInfo userInfo)
			throws Exception;

}
