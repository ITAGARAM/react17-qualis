package com.agaramtech.qualis.invoice.service.invoiceproducttype;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.InvoiceProductType;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'invoiceProductType' table
 */
public interface InvoiceProductTypeService {

	/**
	 * This interface declaration is used to get the over all InvoiceProductType
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceProductType with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getInvoiceProductType(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to invoiceProductType
	 * table.
	 * 
	 * @param objInvoiceProductType [InvoiceProductType] object holding details to
	 *                              be added in invoiceProductType table
	 * @return response entity object holding response status and data of added
	 *         InvoiceProductType object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createInvoiceProductType(InvoiceProductType objInvoiceProductType, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in invoiceProductType
	 * table.
	 * 
	 * @param objInvoiceProductType [InvoiceProductType] object holding details to
	 *                              be updated in invoiceProductType table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceProductType object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateInvoiceProductType(InvoiceProductType objInvoiceProductType, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in invoiceProductType
	 * table.
	 * 
	 * @param objInvoiceProductType [InvoiceProductType] object holding detail to be
	 *                              deleted in invoiceProductType table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceProductType object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public ResponseEntity<Object> deleteInvoiceProductType(InvoiceProductType objInvoiceProductType, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retrieve active InvoiceProductType
	 * object based on the specified ntypecode.
	 * 
	 * @param ntypecode [int] primary key of InvoiceProductType object
	 * @return response entity object holding response status and data of
	 *         InvoiceProductType object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveInvoiceProductTypeById(int ntypecode, UserInfo userInfo) throws Exception;

}
