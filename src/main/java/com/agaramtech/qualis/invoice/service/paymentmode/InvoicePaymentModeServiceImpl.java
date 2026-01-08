package com.agaramtech.qualis.invoice.service.paymentmode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.InvoicePaymentMode;

/**
 * This class holds methods to perform CRUD operation on 'InvoicePaymentmode'
 * table through its DAO layer.
 * 
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class InvoicePaymentModeServiceImpl implements InvoicePaymentModeService {

	@Autowired
	private InvoicePaymentModeDAO invoicePaymentModeDAO;

	/**
	 * This method is used to retrieve list of all active invoicepaymentmode for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         invoicepaymentmode
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getInvoicePaymentMode(UserInfo userInfo) throws Exception {
		return invoicePaymentModeDAO.getInvoicePaymentMode(userInfo);
	}

	/**
	 * This method is used to add a new entry to InvoicePaymentmode table. On
	 * successive insert get the new inserted record along with default status from
	 * transaction status
	 * 
	 * @param objInvoicePaymentMode [InvoicePaymentMode] object holding details to
	 *                              be added in InvoicePaymentmode table
	 * @return inserted InvoicePaymentmode object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> createInvoicePaymentMode(InvoicePaymentMode objInvoicePaymentMode, UserInfo userInfo)
			throws Exception {
		return invoicePaymentModeDAO.createInvoicePaymentMode(objInvoicePaymentMode, userInfo);
	}

	/**
	 * This method is used to update entry in InvoicePaymentmode table.
	 * 
	 * @param objInvoicePaymentMode [InvoicePaymentMode] object holding details to
	 *                              be updated in InvoicePaymentmode table
	 * @return response entity object holding response status and data of updated
	 *         InvoicePaymentmode object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> updateInvoicePaymentMode(InvoicePaymentMode objInvoicePaymentMode, UserInfo userInfo)
			throws Exception {
		return invoicePaymentModeDAO.updateInvoicePaymentMode(objInvoicePaymentMode, userInfo);
	}

	/**
	 * This method is used to delete an entry in InvoicePaymentmode table
	 * 
	 * @param objInvoicePaymentMode [InvoicePaymentMode] an Object holds the record
	 *                              to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         InvoicePaymentmode object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> deleteInvoicePaymentMode(InvoicePaymentMode objInvoicePaymentMode, UserInfo userInfo)
			throws Exception {
		return invoicePaymentModeDAO.deleteInvoicePaymentMode(objInvoicePaymentMode, userInfo);
	}

	/**
	 * This method is used to retrieve active InvoicePaymentmode object based on the
	 * specified npaymentcode.
	 * 
	 * @param npaymentcode [int] primary key of InvoicePaymentmode object
	 * @return response entity object holding response status and data of
	 *         InvoicePaymentmode object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveInvoicePaymentModeById(int npaymentcode, UserInfo userInfo)
			throws Exception {
		return invoicePaymentModeDAO.getActiveInvoicePaymentModeById(npaymentcode, userInfo);
	}

}
