package com.agaramtech.qualis.invoice.service.invoicetaxtype;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;

import com.agaramtech.qualis.invoice.model.InvoiceTaxtype;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'Invoicetaxtype' table
 * 
 * @author ATE237
 * @version 11.0.0.2
 * @since 05- 09- 2025
 */
public interface InvoiceTaxtypeDAO {
	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getTaxtype(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add entry in Invoicetaxtype table.
	 * 
	 * @param objInvoicetaxtype [Invoicetaxtype] object holding detail to be deleted
	 *                          in Invoicetaxtype table
	 * @return response entity object holding response status and data of add
	 *         Invoicetaxtype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createInvoicetaxtype(InvoiceTaxtype objTaxtype, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in Invoicetaxtype table.
	 * 
	 * @param objInvoicetaxtype [Invoicetaxtype] object holding detail to be deleted
	 *                          in Invoicetaxtype table
	 * @return response entity object holding response status and data of deleted
	 *         Invoicetaxtype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteInvoicetaxtype(InvoiceTaxtype objTaxtype, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getActiveInvoiceTaxtypeById(int ntaxcode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in Invoicetaxtype table.
	 * 
	 * @param objInvoicetaxtype [Invoicetaxtype] object holding details to be
	 *                          updated in Invoicetaxtype table
	 * @return response entity object holding response status and data of updated
	 *         Invoicetaxtype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateInvoiceTaxtype(InvoiceTaxtype tax, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getTaxcaltype(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getVersionno(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public InvoiceTaxtype getActiveInvoiceTaxTypeByIdUpdate(int ntaxcode, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getStatus(UserInfo userInfo);

}
