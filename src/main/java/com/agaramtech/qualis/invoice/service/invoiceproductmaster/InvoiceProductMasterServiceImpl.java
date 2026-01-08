package com.agaramtech.qualis.invoice.service.invoiceproductmaster;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.InvoiceProductFile;
import com.agaramtech.qualis.invoice.model.InvoiceProductMaster;
import com.agaramtech.qualis.invoice.model.TaxProductDetails;

/**
 * This class holds methods to perform CRUD operation on 'InvoiceProductmaster'
 * table through its DAO layer.
 * 
 * @author ATE237
 * @version 11.0.0.2
 * @since 05- 09- 2025
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class InvoiceProductMasterServiceImpl implements InvoiceProductMasterService {

	@Autowired
	private InvoiceProducMastertDAO InvoiceProducMastertDAO;

	@Autowired
	private CommonFunction commonFunction;

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
	public ResponseEntity<Object> getProductMaster(UserInfo userinfo) throws Exception {
		return InvoiceProducMastertDAO.getProductMaster(userinfo);

	}

	/**
	 * This method is used to add a new entry to InvoiceProductmaster table. On
	 * successive insert get the new inserted record along with default status from
	 * transaction status
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster ] object holding details
	 *                                to be added in InvoiceProductmaster table
	 * @return inserted InvoiceProductmaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> createInvoiceProductMaster(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.createInvoiceProductMaster(inputMap, userInfo);
	}

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
	@Override
	public ResponseEntity<Object> getProductType(UserInfo userinfo) throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.getProductType(userinfo);
	}

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
	@Override
	public ResponseEntity<Object> getSampleTypeData(UserInfo userinfo) throws Exception {
		return InvoiceProducMastertDAO.getSampleTypeData(userinfo);
	}

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
	@Override
	public ResponseEntity<Object> getSampleTypeByProduct(UserInfo userinfo, int nproductCatcode) throws Exception {
		return InvoiceProducMastertDAO.getSampleTypeByProduct(userinfo, nproductCatcode);
	}

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
	@Override
	public ResponseEntity<Object> getTreeTemplateByProduct(UserInfo userinfo, int nproductCatcode, int nproductcode)
			throws Exception {
		return InvoiceProducMastertDAO.getTreeTemplateByProduct(userinfo, nproductCatcode, nproductcode);
	}

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
	public ResponseEntity<Object> getSpecificationByTreetemplate(UserInfo userinfo, int nproductCatcode,
			int nproductcode, int ntreetemplatemanipulationcode, int nformcode, String lastlabel) throws Exception {
		return InvoiceProducMastertDAO.getSpecificationByTreetemplate(userinfo, nproductCatcode, nproductcode,
				ntreetemplatemanipulationcode, nformcode, lastlabel);

	}

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
	@Override
	public ResponseEntity<Object> getActiveInvoiceProductMasterById(int nproductcode, UserInfo userInfo)
			throws Exception {
		final InvoiceProductMaster productmaster = InvoiceProducMastertDAO
				.getActiveInvoiceProductMasterById(nproductcode, userInfo);
		if (productmaster == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(productmaster, HttpStatus.OK);
		}

	}

	/**
	 * This method is used to update entry in InvoiceProductmaster table.
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster ] object holding details
	 *                                to be updated in InvoiceProductmaster table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceProductmaster object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> updateInvoiceProductMaster(InvoiceProductMaster objInvoiceProductMaster,
			UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.updateInvoiceProductMaster(objInvoiceProductMaster, userInfo);
	}

	/**
	 * This method id used to delete an entry in InvoiceProductmaster table
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster ] an Object holds the
	 *                                record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         InvoiceProductmaster object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> deleteInvoiceProductMaster(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.deleteInvoiceProductMaster(inputMap, userInfo);
	}

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

	@Override
	public ResponseEntity<Object> getProductTypes(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.getProductTypes(inputMap, userInfo);
	}

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
	@Override
	public ResponseEntity<Object> getinvoiceProductType(UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.getinvoiceProductType(userInfo);
	}

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

	@Override
	public ResponseEntity<Object> getActiveProductMasterById(final int nproductcode, final UserInfo userInfo)
			throws Exception {

		return InvoiceProducMastertDAO.getActiveProductMasterById(nproductcode, userInfo);
	}

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
	@Override
	public ResponseEntity<Object> getSelectedProductMasterDetail(UserInfo userInfo, int nproductcode) throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.getSelectedProductMasterDetail(userInfo, nproductcode);
	}

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
	@Override
	public ResponseEntity<Object> getProductByType(int ntypecode, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.getProductByType(ntypecode, userInfo);
	}

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
	@Override
	public ResponseEntity<Object> getproductByType(int ntypecode, UserInfo userInfo, int nallottedspeccode)
			throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.getproductByType(ntypecode, userInfo, nallottedspeccode);
	}

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
	@Override
	public ResponseEntity<Object> getInvoiceByProduct(UserInfo userInfo, final int nproductcode) throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.getInvoiceByProduct(userInfo, nproductcode);
	}

	/**
	 * This method is used to add a new entry to InvoiceProductmaster table. On
	 * successive insert get the new inserted record along with default status from
	 * transaction status
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster ] object holding details
	 *                                to be added in InvoiceProductmaster table
	 * @return inserted InvoiceProductmaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> createTaxProduct(TaxProductDetails taxproduct, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.createTaxProduct(taxproduct, userInfo);
	}

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
	@Override
	public ResponseEntity<Object> getTaxProductById(int nproductcode, int ntaxproductid, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		TaxProductDetails objTaxProductDetails = InvoiceProducMastertDAO.getTaxProductById(nproductcode, ntaxproductid,
				userInfo);
		if (objTaxProductDetails != null) {
			return new ResponseEntity<Object>(objTaxProductDetails, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

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
	@Override
	public ResponseEntity<Object> getTaxname(UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.getTaxname(userInfo);
	}

	/**
	 * This method is used to update entry in InvoiceProductmaster table.
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster ] object holding details
	 *                                to be updated in InvoiceProductmaster table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceProductmaster object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> updateTaxProduct(TaxProductDetails taxProduct, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.updateTaxProduct(taxProduct, userInfo);
	}

	/**
	 * This method id used to delete an entry in InvoiceProductmaster table
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster ] an Object holds the
	 *                                record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         InvoiceProductmaster object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> deleteTaxProduct(TaxProductDetails taxProductDetails, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.deleteTaxProduct(taxProductDetails, userInfo);
	}

	/**
	 * This method is used to add a new entry to InvoiceProductmaster table. On
	 * successive insert get the new inserted record along with default status from
	 * transaction status
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster ] object holding details
	 *                                to be added in InvoiceProductmaster table
	 * @return inserted InvoiceProductmaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> createInvoiceProductFile(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.createInvoiceProductFile(request, userInfo);
	}

	/**
	 * This method is used to update entry in InvoiceProductmaster table.
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster ] object holding details
	 *                                to be updated in InvoiceProductmaster table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceProductmaster object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> updateInvoiceProductFile(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.updateInvoiceProductFile(request, userInfo);
	}

	/**
	 * This method id used to delete an entry in InvoiceProductmaster table
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster ] an Object holds the
	 *                                record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         InvoiceProductmaster object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> deleteInvoiceProductFile(InvoiceProductFile objProductFile, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.deleteInvoiceProductFile(objProductFile, userInfo);
	}

	/**
	 * This method is used to update entry in InvoiceProductmaster table.
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster ] object holding details
	 *                                to be updated in InvoiceProductmaster table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceProductmaster object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> editInvoiceProductFile(InvoiceProductFile objProductFile, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.editInvoiceProductFile(objProductFile, userInfo);
	}

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
	@Override
	public Map<String, Object> viewAttachedInvoiceProductFile(InvoiceProductFile objProductFile, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.viewAttachedInvoiceProductFile(objProductFile, userInfo);
	}

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

	public ResponseEntity<Object> getProductTests(UserInfo userinfo) throws Exception {
		return InvoiceProducMastertDAO.getProductTests(userinfo);

	}

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
	@Override
	public ResponseEntity<Object> getSearchFieldData(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return InvoiceProducMastertDAO.getSearchFieldData(inputMap, userInfo);
	}

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
	@Override
	public ResponseEntity<Object> getTaxtype(UserInfo userInfo, int Productcode, Map<String, Object> inputMap)
			throws Exception {
		// TODO Auto-generated method stub
		return InvoiceProducMastertDAO.getTaxtype(userInfo, Productcode, inputMap);
	}

}
