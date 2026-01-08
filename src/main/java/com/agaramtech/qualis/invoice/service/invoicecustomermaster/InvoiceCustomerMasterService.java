package com.agaramtech.qualis.invoice.service.invoicecustomermaster;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;

import com.agaramtech.qualis.invoice.model.UserRoleFieldControl;
import com.agaramtech.qualis.invoice.model.UsersRoleField;
import com.agaramtech.qualis.invoice.model.CustomerFile;
import com.agaramtech.qualis.invoice.model.InvoiceCustomerMaster;


public interface InvoiceCustomerMasterService {
	 /**
		 * This method is used to fetch the active Invoicecustomermaster objects for the
		 * specified Invoicecustomermaster name and site.
		 * 
		 * @param ntypecode       [String] name of the Invoicecustomermaster
		 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
		 * @return list of active Invoicecustomermaster code(s) based on the specified
		 *         Invoicecustomermaster name and site
		 * @throws Exception
		 */
	public ResponseEntity<Object> getInvoiceCustomerMaster(UserInfo userInfo) throws Exception;
	 /**
		 * This method is used to fetch the active Invoicecustomermaster objects for the
		 * specified Invoicecustomermaster name and site.
		 * 
		 * @param ntypecode       [String] name of the Invoicecustomermaster
		 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
		 * @return list of active Invoicecustomermaster code(s) based on the specified
		 *         Invoicecustomermaster name and site
		 * @throws Exception
		 */
	public ResponseEntity<Object> getActiveInvoiceCustomerMasterById(final int ncustomercode,UserInfo userInfo) throws Exception ;
	/**
	 * This method is used to add a new entry to Invoicecustomermaster table. Need to
	 * check for duplicate entry of Invoicecustomermaster name for the specified site
	 * before saving into database. Need to check that there should be only one
	 * default Invoicecustomermaster for a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding details
	 *                                to be added in Invoicecustomermaster table
	 * @return inserted Invoicecustomermaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> createInvoiceCustomerMaster(InvoiceCustomerMaster objInvoiceCustomerMaster,UserInfo userInfo) throws Exception;
	/**
	 * This method is used to update entry in Invoicecustomermaster table. Need to
	 * validate that the Invoicecustomermaster object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * Invoicecustomermaster name for the specified site before saving into database.
	 * Need to check that there should be only one default Invoicecustomermaster for
	 * a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding details
	 *                                to be updated in Invoicecustomermaster table
	 * @return response entity object holding response status and data of updated
	 *         Invoicecustomermaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> updateInvoiceCustomerMaster(InvoiceCustomerMaster objInvoiceCustomerMaster,UserInfo userInfo) throws Exception;
	/**
	 * This method id used to delete an entry in Invoicecustomermaster table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] an Object holds the
	 *                                record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         Invoicecustomermaster object
	 * @exception Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> deleteInvoiceCustomerMaster(InvoiceCustomerMaster objInvoiceCustomerMaster,UserInfo userInfo) throws Exception;
	 /**
		 * This method is used to fetch the active Invoicecustomermaster objects for the
		 * specified Invoicecustomermaster name and site.
		 * 
		 * @param ntypecode       [String] name of the Invoicecustomermaster
		 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
		 * @return list of active Invoicecustomermaster code(s) based on the specified
		 *         Invoicecustomermaster name and site
		 * @throws Exception
		 */
	public ResponseEntity<Object> getSelectedCustomerDetail(UserInfo userInfo, int ncustomercode) throws Exception;
	/**
	 * This method is used to add a new entry to Invoicecustomermaster table. Need to
	 * check for duplicate entry of Invoicecustomermaster name for the specified site
	 * before saving into database. Need to check that there should be only one
	 * default Invoicecustomermaster for a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding details
	 *                                to be added in Invoicecustomermaster table
	 * @return inserted Invoicecustomermaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> createCustomerFile(MultipartHttpServletRequest request, UserInfo userInfo) throws Exception;
	/**
	 * This method id used to delete an entry in Invoicecustomermaster table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] an Object holds the
	 *                                record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         Invoicecustomermaster object
	 * @exception Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> deleteCustomerFile(CustomerFile objCustomerFile, UserInfo objUserInfo) throws Exception;
	/**
	 * This method is used to update entry in Invoicecustomermaster table. Need to
	 * validate that the Invoicecustomermaster object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * Invoicecustomermaster name for the specified site before saving into database.
	 * Need to check that there should be only one default Invoicecustomermaster for
	 * a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding details
	 *                                to be updated in Invoicecustomermaster table
	 * @return response entity object holding response status and data of updated
	 *         Invoicecustomermaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> updateCustomerFile(MultipartHttpServletRequest request,  UserInfo objUserInfo)throws Exception;
	/**
	 * This method is used to update entry in Invoicecustomermaster table. Need to
	 * validate that the Invoicecustomermaster object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * Invoicecustomermaster name for the specified site before saving into database.
	 * Need to check that there should be only one default Invoicecustomermaster for
	 * a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding details
	 *                                to be updated in Invoicecustomermaster table
	 * @return response entity object holding response status and data of updated
	 *         Invoicecustomermaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> editCustomerFile(CustomerFile objCustomerFile, UserInfo userInfo) throws Exception;
	 /**
		 * This method is used to viewAttachedCustomerFile the active Invoicecustomermaster objects for the
		 * specified Invoicecustomermaster name and site.
		 * 
		 * @param ntypecode       [String] name of the Invoicecustomermaster
		 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
		 * @return list of active Invoicecustomermaster code(s) based on the specified
		 *         Invoicecustomermaster name and site
		 * @throws Exception
		 */
	public Map<String, Object> viewAttachedCustomerFile(CustomerFile objCustomerFile, UserInfo userInfo)throws Exception;
	 /**
		 * This method is used to view file from invoiceproductfile table. * @return
	     * response entity object holding response status and data of updated
	     * Invoicecustomermaster object
		 * @param ntypecode       [String] name of the Invoicecustomermaster
		 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
		 * @return list of active Invoicecustomermaster code(s) based on the specified
		 *         Invoicecustomermaster name and site
		 * @throws Exception
		 */
	public ResponseEntity<Object> getCustomerMaster(UserInfo userInfo) throws Exception;
	
	/**
	 * This method is used to add a new entry to Invoicecustomermaster table. Need to
	 * check for duplicate entry of Invoicecustomermaster name for the specified site
	 * before saving into database. Need to check that there should be only one
	 * default Invoicecustomermaster for a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding details
	 *                                to be added in Invoicecustomermaster table
	 * @return inserted Invoicecustomermaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> createControlRights(UserInfo userInfo, UserRoleFieldControl userroleController,
			List<UsersRoleField> lstusersrolescreen, Integer nflag, int nneedrights) throws Exception;



}
