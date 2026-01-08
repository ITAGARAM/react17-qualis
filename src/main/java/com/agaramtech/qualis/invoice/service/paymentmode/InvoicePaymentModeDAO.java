package com.agaramtech.qualis.invoice.service.paymentmode;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.invoice.model.InvoicePaymentMode;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operations on
 * 'InvoicePaymentmode' table
 * 
 */
public interface InvoicePaymentModeDAO {

	/**
	 * This interface declaration is used to get the overall InvoicePaymentmode with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoicePaymentmode with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getInvoicePaymentMode(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in InvoicePaymentmode
	 * table.
	 * 
	 * @param objInvoicePaymentMode [InvoicePaymentMode] object holding details to
	 *                              be updated in InvoicePaymentmode table
	 * @return response entity object holding response status and data of updated
	 *         InvoicePaymentmode object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateInvoicePaymentMode(InvoicePaymentMode objInvoicePaymentMode, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in InvoicePaymentmode
	 * table.
	 * 
	 * @param objInvoicePaymentMode [InvoicePaymentMode] object holding detail to be
	 *                              deleted in InvoicePaymentmode table
	 * @return response entity object holding response status and data of deleted
	 *         InvoicePaymentmode object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteInvoicePaymentMode(InvoicePaymentMode objInvoicePaymentMode, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to add a new entry to InvoicePaymentmode
	 * table.
	 * 
	 * @param objInvoicePaymentMode [InvoicePaymentMode] object holding details to
	 *                              be added in InvoicePaymentmode table
	 * @return response entity object holding response status and data of added
	 *         InvoicePaymentmode object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createInvoicePaymentMode(InvoicePaymentMode objInvoicePaymentMode, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active InvoicePaymentmode
	 * object based on the specified npaymentcode.
	 * 
	 * @param npaymentcode [int] primary key of invoicepaymentmode object
	 * @return response entity object holding response status and data of
	 *         InvoicePaymentmode object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveInvoicePaymentModeById(int npaymentcode, UserInfo userInfo) throws Exception;
}
