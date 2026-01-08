package com.agaramtech.qualis.invoice.service.invoiceproductmaster;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.InvoiceProductFile;
import com.agaramtech.qualis.invoice.model.InvoiceProductMaster;
import com.agaramtech.qualis.invoice.model.InvoiceProductType;
import com.agaramtech.qualis.invoice.model.TaxProductDetails;

/**
 * This interface holds declarations to perform CRUD operations on
 * 'InvoiceProductmaster' table
 * 
 * @author ATE237
 * @since 11 - October -2023
 */

public interface InvoiceProducMastertDAO {

	/**
	 * This interface declaration is used to get the overall InvoiceProductmaster
	 * with respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceProductmaster with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getProductMaster(UserInfo userinfo) throws Exception;

	/**
	 * This method is used to add a new entry to InvoiceProductmaster table. Need to
	 * check for duplicate entry of InvoiceProductmaster name for the specified site
	 * before saving into database. Need to check that there should be only one
	 * default InvoiceProductmaster for a site
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] object holding details
	 *                                to be added in InvoiceProductmaster table
	 * @return inserted InvoiceProductmaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> createInvoiceProductMaster(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This method is used to retrieve list of all active InvoiceProductmaster for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         InvoiceProductmaster
	 * @throws Exception that are thrown from this Service layer
	 */
	public ResponseEntity<Object> getProductType(UserInfo userinfo) throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public InvoiceProductMaster getActiveInvoiceProductMasterById(int nproductcode, UserInfo userInfo) throws Exception;

	/**
	 * This method is used to update entry in InvoiceProductmaster table.
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] object holding details
	 *                                to be updated in InvoiceProductmaster table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceProductmaster object
	 * @throws Exception that are thrown from this Service layer
	 */
	public ResponseEntity<Object> updateInvoiceProductMaster(InvoiceProductMaster objInvoiceProductMaster,
			UserInfo userInfo) throws Exception;

	/**
	 * This method id used to delete an entry in InvoiceProductmaster table
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster ] an Object holds the
	 *                                record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         InvoiceProductmaster object
	 * @exception Exception that are thrown from this Service layer
	 */
	public ResponseEntity<Object> deleteInvoiceProductMaster(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getProductTypes(Map<String, Object> inputMap, UserInfo userinfo) throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getinvoiceProductType(UserInfo userinfo) throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getActiveProductMasterById(final int nproductcode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public InvoiceProductType getActiveInvoiceProductTypeById(int nproductcode, UserInfo userInfo) throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getproductByDel(int ntypecode, UserInfo objUserInfo) throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSelectedProductMasterDetail(UserInfo userInfo, int nproductcode) throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getProductByType(int ntypecode, UserInfo userInfo) throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getproductByType(int ntypecode, UserInfo userInfo, int nallottedspeccode)
			throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getInvoiceByProduct(UserInfo userInfo, int nproductcode) throws Exception;

	/**
	 * This method is used to add a new entry to InvoiceProductmaster table. Need to
	 * check for duplicate entry of InvoiceProductmaster name for the specified site
	 * before saving into database. Need to check that there should be only one
	 * default InvoiceProductmaster for a site
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] object holding details
	 *                                to be added in InvoiceProductmaster table
	 * @return inserted InvoiceProductmaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> createTaxProduct(TaxProductDetails taxproduct, UserInfo userInfo) throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public TaxProductDetails getTaxProductById(int nproductcode, int ntaxproductcode, UserInfo userInfo)
			throws Exception;

	/**
	 * This method is used to update entry in InvoiceProductmaster table.
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] object holding details
	 *                                to be updated in InvoiceProductmaster table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceProductmaster object
	 * @throws Exception that are thrown from this Service layer
	 */
	public ResponseEntity<Object> updateTaxProduct(TaxProductDetails taxProduct, UserInfo userInfo) throws Exception;

	/**
	 * This method id used to delete an entry in InvoiceProductmaster table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] an Object holds the
	 *                                record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         InvoiceProductmaster object
	 * @exception Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> deleteTaxProduct(TaxProductDetails taxProductDetails, UserInfo userInfo)
			throws Exception;

	/**
	 * This method is used to add a new entry to InvoiceProductmaster table. Need to
	 * check for duplicate entry of InvoiceProductmaster name for the specified site
	 * before saving into database. Need to check that there should be only one
	 * default InvoiceProductmaster for a site
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] object holding details
	 *                                to be added in InvoiceProductmaster table
	 * @return inserted InvoiceProductmaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> createInvoiceProductFile(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception;

	/**
	 * This method is used to update entry in InvoiceProductmaster table.
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] object holding details
	 *                                to be updated in InvoiceProductmaster table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceProductmaster object
	 * @throws Exception that are thrown from this Service layer
	 */
	public ResponseEntity<Object> updateInvoiceProductFile(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception;

	/**
	 * This method id used to delete an entry in InvoiceProductmaster table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] an Object holds the
	 *                                record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         InvoiceProductmaster object
	 * @exception Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> deleteInvoiceProductFile(InvoiceProductFile objProductFile, UserInfo userInfo)
			throws Exception;

	/**
	 * This method is used to update entry in InvoiceProductmaster table.
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] object holding details
	 *                                to be updated in InvoiceProductmaster table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceProductmaster object
	 * @throws Exception that are thrown from this Service layer
	 */
	public ResponseEntity<Object> editInvoiceProductFile(InvoiceProductFile objProductFile, UserInfo userInfo)
			throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public Map<String, Object> viewAttachedInvoiceProductFile(final InvoiceProductFile objProductFile,
			UserInfo userInfo) throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getProductTests(UserInfo userinfo);

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */

	public ResponseEntity<Object> getSearchFieldData(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getTaxname(UserInfo userInfo) throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getTaxtype(UserInfo userInfo, int Productcode, Map<String, Object> inputMap)
			throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSampleTypeData(UserInfo userinfo) throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSampleTypeByProduct(UserInfo userinfo, int nproductCatcode) throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getTreeTemplateByProduct(UserInfo userinfo, int nproductCatcode, int nproductcode)
			throws Exception;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSpecificationByTreetemplate(UserInfo userinfo, int nproductCatcode,
			int nproductcode, int ntreetemplatemanipulationcode, int nformcode, String lastlabel) throws Exception;

}
