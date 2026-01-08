package com.agaramtech.qualis.invoice.service.quotation;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'InvoiceQuotation'
 * table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class InvoiceQuotationServiceImpl implements InvoiceQuotationService {

	private final InvoiceQuotationDAO invoiceQuotationDAO;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param InvoiceQuotationDAO InvoiceQuotationDAO Interface
	 * @param commonFunction      CommonFunction holding common utility functions
	 */
	public InvoiceQuotationServiceImpl(InvoiceQuotationDAO invoiceQuotationDAO, CommonFunction commonFunction) {
		this.invoiceQuotationDAO = invoiceQuotationDAO;
	}

	/**
	 * This interface declaration is used to get the overall InvoiceQuotation with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getQuotation(final Map<String, Object> inputMap,UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return invoiceQuotationDAO.getQuotation(inputMap,userInfo);
	}

	/**
	 * This interface declaration is used to get SeletedQuotation with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getSeletedQuotation(final Map<String, Object> inputMap) throws Exception {
		return invoiceQuotationDAO.getSeletedQuotation(inputMap);
	}

	/**
	 * This interface declaration is used to get the overall CustomerQuotation with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of CustomerQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getCustomerQuotation(final Map<String, Object> inputMap) throws Exception {
		return invoiceQuotationDAO.getCustomerQuotation(inputMap);
	}

	/**
	 * This interface declaration is used to get the Payment with respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getPayment(UserInfo userInfo) throws Exception {
		return invoiceQuotationDAO.getPayment(userInfo);
	}

	/**
	 * This interface declaration is used to get the CurrencyType with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of CurrencyType with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getCurrencyType(UserInfo userInfo) throws Exception {
		return invoiceQuotationDAO.getCurrencyType(userInfo);
	}

	/**
	 * This interface declaration is used to add a new entry to InvoiceQuotation
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createQuotation(final Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception {
		return invoiceQuotationDAO.createQuotation(inputMap, objUserInfo);
	}

	/**
	 * This interface declaration is used to get the SearchFieldData
	 * InvoiceQuotation with respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getSearchFieldData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return invoiceQuotationDAO.getSearchFieldData(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to delete entry in InvoiceQuotation table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding detail to be
	 *                            deleted in InvoiceQuotation table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteQuotation(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return invoiceQuotationDAO.deleteQuotation(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to get the overall Invoice Product with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getProducts(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return invoiceQuotationDAO.getProducts(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to get the overall get scheme product with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getProductforQuotation(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return invoiceQuotationDAO.getProductforQuotation(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to get the Active currency with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public String getActivecurrency(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return invoiceQuotationDAO.getActivecurrency(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to update entry in InvoiceQuotation table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateQuotation(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return invoiceQuotationDAO.updateQuotation(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to get Active Quotation with table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveInvoiceQuotationById(final Map<String, Object> inputMap) throws Exception {
		return invoiceQuotationDAO.getActiveInvoiceQuotationById(inputMap);
	}

	/**
	 * This interface declaration is used to update Product entry in
	 * InvoiceQuotation table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateProductQuotation(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return invoiceQuotationDAO.updateProductQuotation(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to update entry in ProductQuotation table.
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateOuterProductQuotation(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return invoiceQuotationDAO.updateOuterProductQuotation(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to delete Product entry in
	 * InvoiceQuotation table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding detail to be
	 *                            deleted in InvoiceQuotation table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteProductQuotation(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return invoiceQuotationDAO.deleteProductQuotation(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to get the overall Quotation search with
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getQuotationsearch(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return invoiceQuotationDAO.getQuotationsearch(inputMap);
	}

	/**
	 * This interface declaration is used to ReportGenerate InvoiceQuotation table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> quotationReportGenerate(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return invoiceQuotationDAO.quotationReportGenerate(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to add a new entry to InvoiceQuotation
	 * table for approve
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> approveQuotation(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return invoiceQuotationDAO.approveQuotation(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to get ProductTestDetails table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getProductTestDetails(UserInfo userInfo) throws Exception {
		return invoiceQuotationDAO.getProductTestDetails(userInfo);
	}

	/**
	 * This interface declaration is used to get the Schemes with table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSchemesDetails(UserInfo userInfo) {
		return invoiceQuotationDAO.getSchemesDetails(userInfo);
	}

	/**
	 * This interface declaration is used to update entry in InvoiceQuotation table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateUsercode(final Map<String, Object> inputMap, final UserInfo userInfo) {
		return invoiceQuotationDAO.updateUsercode(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to get ProjectDetails table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getProjectDetails(UserInfo userInfo) {
		return invoiceQuotationDAO.getProjectDetails(userInfo);
	}

	/**
	 * This interface declaration is used to update entry in InvoiceQuotation
	 * Product table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> costUpdate(final Map<String, Object> inputMap) {
		return invoiceQuotationDAO.costUpdate(inputMap);
	}

	/**
	 * This interface declaration is used to getFilteredRecords in InvoiceQuotation
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getFilteredRecords(final UserInfo userInfo, final String fromDate,
			final String toDate, final String breadCrumbFrom, final String breadCrumbTo) throws Exception {
		// TODO Auto-generated method stub
		return invoiceQuotationDAO.getFilteredRecords(userInfo, fromDate, toDate, breadCrumbFrom, breadCrumbTo);
	}

}
