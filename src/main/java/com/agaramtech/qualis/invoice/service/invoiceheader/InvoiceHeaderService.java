package com.agaramtech.qualis.invoice.service.invoiceheader;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'InvoiceHeader' table
 */
public interface InvoiceHeaderService {
	/**
	 * This interface declaration is used to get the overall InvoiceHeader with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getInvoiceHeader(Map<String, Object> inputMap,UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to InvoiceHeader table.
	 * 
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be added in
	 *                         InvoiceHeader table
	 * @return response entity object holding response status and data of added
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createInvoice(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the SeletedInvoice with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSeletedInvoice(Map<String, Object> inputMap) throws Exception;

	/**
	 * This interface declaration is used to get the overall InvoiceQuotation with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getQuotationTab(Map<String, Object> inputMap) throws Exception;

	/**
	 * This interface declaration is used to get the overall LIMSArno with respect
	 * to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of LIMSArno with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getArnoTab(Map<String, Object> inputMap) throws Exception;

	/**
	 * This interface declaration is used to update entry in InvoiceHeader table.
	 * 
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be updated
	 *                         in InvoiceHeader table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateInvoice(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to delete entry in InvoiceHeader table.
	 * 
	 * @param objInvoiceHeader [InvoiceHeader] object holding detail to be deleted
	 *                         in InvoiceHeader table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteInvoice(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update entry in productInvoiceHeader
	 * table.
	 * 
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be updated
	 *                         in productInvoiceHeader table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateProductInvoice(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in productInvoiceHeader
	 * table.
	 * 
	 * @param objInvoiceHeader [InvoiceHeader] object holding detail to be deleted
	 *                         in productInvoiceHeader table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteProductInvoiceHeader(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to get the overall get scheme product with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of ProductforInvoice with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getProductforInvoice(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to get the overall Invoice Product with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getProducts(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get Active Invoice with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getActiveInvoiceById(Map<String, Object> inputMap) throws Exception;

	/**
	 * This interface declaration is used to update entry in ProductInvoice table.
	 * 
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be updated
	 *                         in Invoice Product table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateOuterProductInvoice(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to get the overall Invoice ProductArno
	 * with respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getInvoiceProductArno(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to get the overall Invoice search with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getInvoicesearch(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to invoiceReport Generate with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> invoiceReportGenerate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to approveInvoice with respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> approveInvoiceRecord(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to retire Invoice Record with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> retireInvoiceRecord(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the Product Test Details with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getProductTestDetails(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the Active currency with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public String getActivecurrencyvalue(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the Patient Details with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getPatientDetails(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get Search PatientField with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSearchPatientFieldData(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to get the Schemes with respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSchemes(UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to update UsercodeForReport.
	 * 
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be updated
	 *                         in InvoiceHeader table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateUsercodeForReport(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update cost.
	 * 
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be updated
	 *                         in InvoiceHeader Product table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> costUpdateCost(Map<String, Object> inputMap) throws Exception;

	/**
	 * This interface declaration is used to get the Filtered Invoice Record with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getFilteredRecord(UserInfo userInfo, String fromDate, String toDate,
			String breadCrumbFrom, String breadCrumbTo) throws Exception;
	
	//Added by sonia on 16th oct 2025 for jira id:SWSM-104
	/**
	  * This service interface declaration will access the DAO layer  that is used Sent the Report By Mail.
	  * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	  * 							  which the list is to be fetched
      * @return response entity  object holding response status as success
	  * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> sendReportByMail(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	
}