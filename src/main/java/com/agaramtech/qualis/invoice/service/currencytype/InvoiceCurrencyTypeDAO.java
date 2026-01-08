package com.agaramtech.qualis.invoice.service.currencytype;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.InvoiceCurrencyType;

public interface InvoiceCurrencyTypeDAO {

	/**
	 * This interface declaration is used to get the over all invoicecurrencytype
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of invoicecurrencytype with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getInvoiceCurrencyType(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to invoicecurrencytype
	 * table.
	 * 
	 * @param objInvoicecurrencytype [Invoicecurrencytype] object holding details to
	 *                               be added in invoicecurrencytype table
	 * @return response entity object holding response status and data of added
	 *         invoicecurrencytype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createInvoiceCurrencyType(InvoiceCurrencyType objInvoiceCurrencyType,
			UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active invoicecurrencytype
	 * object based on the specified ncurrencycode.
	 * 
	 * @param ncurrencycode [int] primary key of invoicecurrencytype object
	 * @return response entity object holding response status and data of
	 *         invoicecurrencytype object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> getActiveInvoiceCurrencyTypeById(int ncurrencycode, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in invoicecurrencytype
	 * table.
	 * 
	 * @param objInvoicecurrencytype [Invoicecurrencytype] object holding details to
	 *                               be updated in invoicecurrencytype table
	 * @return response entity object holding response status and data of updated
	 *         invoicecurrencytype object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> updateInvoiceCurrencyType(InvoiceCurrencyType objInvoiceCurrencyType,
			UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in invoicecurrencytype
	 * table.
	 * 
	 * @param objinvoicecurrencytype [Invoicecurrencytype] object holding detail to
	 *                               be deleted in invoicecurrencytype table
	 * @return response entity object holding response status and data of deleted
	 *         invoicecurrencytype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteInvoiceCurrencyType(InvoiceCurrencyType objInvoiceCurrencyType,
			UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active updated
	 * invoicecurrencytype object based on the specified ncurrencycode.
	 * 
	 * @param ncurrencycode [int] primary key of invoicecurrencytype object
	 * @return response entity object holding response status and data of
	 *         invoicecurrencytype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public InvoiceCurrencyType getActiveInvoiceCurrencyTypeByIdUpdate(int ncurrencycode, UserInfo userInfo)
			throws Exception;

}
