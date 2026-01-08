package com.agaramtech.qualis.invoice.service.currencytype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.InvoiceCurrencyType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class holds methods to perform CRUD operation on 'invoicecurrencytype'
 * table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class InvoiceCurrencyTypeServiceImpl implements InvoiceCurrencyTypeService {

	@Autowired
	private InvoiceCurrencyTypeDAO currencyTypeDAO;

	/**
	 * This method is used to retrieve list of all active invoicecurrencytypes for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         invoicecurrencytype
	 * @throws Exception that are thrown from this Service layer
	 */

	public ResponseEntity<Object> getInvoiceCurrencyType(UserInfo userInfo) throws Exception {
		return currencyTypeDAO.getInvoiceCurrencyType(userInfo);
	}

	/**
	 * This method is used to add a new entry to InvoiceCurrencyType table. On
	 * successive insert get the new inserted record along with default status from
	 * transaction status
	 * 
	 * @param currencyTypeDAO [InvoiceCurrencyType] object holding details to be
	 *                        added in InvoiceCurrencyType table
	 * @return inserted InvoiceCurrencyType object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> createInvoiceCurrencyType(InvoiceCurrencyType objInvoiceCurrencyType,
			UserInfo userInfo) throws Exception {
		return currencyTypeDAO.createInvoiceCurrencyType(objInvoiceCurrencyType, userInfo);
	}

	/**
	 * This method is used to retrieve active InvoiceCurrencyType object based on
	 * the specified ncurrencycode.
	 * 
	 * @param ncurrencycode [int] primary key of InvoiceCurrencyType object
	 * @return response entity object holding response status and data of
	 *         InvoiceCurrencyType object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveInvoiceCurrencyTypeById(int ncurrencycode, UserInfo userInfo)
			throws Exception {
		return currencyTypeDAO.getActiveInvoiceCurrencyTypeById(ncurrencycode, userInfo);
	}

	/**
	 * This method is used to update entry in InvoiceCurrencyType table.
	 * 
	 * @param currencyTypeDAO [InvoiceCurrencyType] object holding details to be
	 *                        updated in InvoiceCurrencyType table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceCurrencyType object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> updateInvoiceCurrencyType(InvoiceCurrencyType objInvoiceCurrencyType,
			UserInfo userInfo) throws Exception {
		return currencyTypeDAO.updateInvoiceCurrencyType(objInvoiceCurrencyType, userInfo);
	}

	/**
	 * This method is used to delete an entry in InvoiceCurrencyType table
	 * 
	 * @param currencyTypeDAO [InvoiceCurrencyType] an Object holds the record to be
	 *                        deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         InvoiceCurrencyType object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> deleteInvoiceCurrencyType(InvoiceCurrencyType objInvoiceCurrencyType,
			UserInfo userInfo) throws Exception {
		return currencyTypeDAO.deleteInvoiceCurrencyType(objInvoiceCurrencyType, userInfo);
	}
}
