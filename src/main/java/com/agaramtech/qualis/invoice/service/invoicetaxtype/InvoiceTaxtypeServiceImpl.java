package com.agaramtech.qualis.invoice.service.invoicetaxtype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.InvoiceTaxtype;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class InvoiceTaxtypeServiceImpl implements InvoiceTaxtypeService {

	@Autowired
	private InvoiceTaxtypeDAO invoiceTaxtypeDAO;

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getTaxtype(UserInfo userInfo) throws Exception {
		return invoiceTaxtypeDAO.getTaxtype(userInfo);
	}

	/**
	 * This interface declaration is used to add entry in Invoicetaxtype table.
	 * 
	 * @param objInvoicetaxtype [Invoicetaxtype] object holding detail to be deleted
	 *                          in Invoicetaxtype table
	 * @return response entity object holding response status and data of add
	 *         Invoicetaxtype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> createInvoicetaxtype(InvoiceTaxtype objTaxtype, UserInfo userInfo) throws Exception {
		return invoiceTaxtypeDAO.createInvoicetaxtype(objTaxtype, userInfo);
	}

	/**
	 * This interface declaration is used to delete entry in Invoicetaxtype table.
	 * 
	 * @param objInvoicetaxtype [Invoicetaxtype] object holding detail to be deleted
	 *                          in Invoicetaxtype table
	 * @return response entity object holding response status and data of deleted
	 *         Invoicetaxtype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> deleteInvoiceTaxtype(InvoiceTaxtype objTaxtype, UserInfo userInfo) throws Exception {
		return invoiceTaxtypeDAO.deleteInvoicetaxtype(objTaxtype, userInfo);
	}

	/**
	 * This interface declaration is used to update entry in Invoicetaxtype table.
	 * 
	 * @param objInvoicetaxtype [Invoicetaxtype] object holding details to be
	 *                          updated in Invoicetaxtype table
	 * @return response entity object holding response status and data of updated
	 *         Invoicetaxtype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> updateInvoiceTaxtype(InvoiceTaxtype objTaxtype, UserInfo userInfo) throws Exception {
		return invoiceTaxtypeDAO.updateInvoiceTaxtype(objTaxtype, userInfo);
	}

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getActiveInvoiceTaxtypeById(int ntaxcode, UserInfo userInfo) throws Exception {
		return invoiceTaxtypeDAO.getActiveInvoiceTaxtypeById(ntaxcode, userInfo);
	}

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getTaxcaltype(final UserInfo userInfo) throws Exception {
		return invoiceTaxtypeDAO.getTaxcaltype(userInfo);
	}

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getVersionno(final UserInfo userInfo) throws Exception {
		return invoiceTaxtypeDAO.getVersionno(userInfo);
	}

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getStatus(UserInfo userInfo) {
		return invoiceTaxtypeDAO.getStatus(userInfo);
	}

}
