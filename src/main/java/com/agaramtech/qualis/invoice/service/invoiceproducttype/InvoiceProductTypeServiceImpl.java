package com.agaramtech.qualis.invoice.service.invoiceproducttype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.InvoiceProductType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class holds methods to perform CRUD operation on 'InvoiceProductType'
 * table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class InvoiceProductTypeServiceImpl implements InvoiceProductTypeService {

	@Autowired
	private InvoiceProductTypeDAO invoiceProductTypeDAO;

	/**
	 * This method is used to retrieve list of all active InvoiceProductType for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         InvoiceProductType
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getInvoiceProductType(UserInfo userInfo) throws Exception {
		return invoiceProductTypeDAO.getInvoiceProductType(userInfo);
	}

	/**
	 * This method is used to add a new entry to invoiceProductType table. On
	 * successive insert get the new inserted record along with default status from
	 * transaction status
	 * 
	 * @param objInvoiceProductType [InvoiceProductType] object holding details to
	 *                              be added in invoiceProductType table
	 * @return inserted InvoiceProductType object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> createInvoiceProductType(InvoiceProductType objInvoiceProductType, UserInfo userInfo)
			throws Exception {

		return invoiceProductTypeDAO.createInvoiceProductType(objInvoiceProductType, userInfo);

	}

	/**
	 * This method is used to update entry in invoiceProductType table.
	 * 
	 * @param objInvoiceProductType [InvoiceProductType] object holding details to
	 *                              be updated in invoiceProductType table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceProductType object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> updateInvoiceProductType(InvoiceProductType objInvoiceProductType, UserInfo userInfo)
			throws Exception {

		return invoiceProductTypeDAO.updateInvoiceProductType(objInvoiceProductType, userInfo);

	}

	/**
	 * This method id used to delete an entry in invoiceProductType table
	 * 
	 * @param objInvoiceProductType [InvoiceProductType] an Object holds the record
	 *                              to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         InvoiceProductType object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> deleteInvoiceProductType(InvoiceProductType objInvoiceProductType, UserInfo userInfo)
			throws Exception {

		return invoiceProductTypeDAO.deleteInvoiceProductType(objInvoiceProductType, userInfo);

	}

	/**
	 * This method is used to retrieve active InvoiceProductType object based on the
	 * specified ntypecode.
	 * 
	 * @param ntypecode [int] primary key of InvoiceProductType object
	 * @return response entity object holding response status and data of
	 *         InvoiceProductType object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveInvoiceProductTypeById(int ntypecode, UserInfo userInfo) throws Exception {

		return invoiceProductTypeDAO.getActiveInvoiceProductTypeById(ntypecode, userInfo);
	}
}
