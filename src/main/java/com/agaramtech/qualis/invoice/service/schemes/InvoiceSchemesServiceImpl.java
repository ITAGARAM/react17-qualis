package com.agaramtech.qualis.invoice.service.schemes;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.UserInfo;
/**
 * This class holds methods to perform CRUD operation on 'InvoiceSchemes' table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class InvoiceSchemesServiceImpl implements InvoiceSchemesService {

	@Autowired
	private InvoiceSchemesDAO invoiceSchemesDAO;
	/**
	 * This method is used to retrieve list of all active InvoiceSchemes for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active InvoiceSchemes
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getInvoiceSchemes(UserInfo userInfo) {
		return invoiceSchemesDAO.getInvoiceSchemes(userInfo);
	}
	/**
	 * This method is used to exportproductmaster list of all active InvoiceSchemes for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active InvoiceSchemes
	 * @throws Exception that are thrown from this Service layer
	 */
	 @Override
	    @Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> exportproductmaster(Map<String, Object> objmap,UserInfo userInfo) throws Exception{
		return invoiceSchemesDAO.exportproductmaster(objmap,userInfo);
	}
	/**
	 * This method is used to importProductMaster list of all active InvoiceSchemes for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active InvoiceSchemes
	 * @throws Exception that are thrown from this Service layer
	 */
	 @Override
	    @Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> importProductMaster(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		return invoiceSchemesDAO.importProductMaster(request, userInfo);
	}
/**
	 * This method is used to add a new entry to InvoiceSchemes table.
	 * On successive insert get the new inserted record along with default status from transaction status 
	 * @param objInvoiceSchemes [InvoiceSchemes] object holding details to be added in InvoiceSchemes table
	 * @return inserted InvoiceSchemes object and HTTP Status on successive insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	 @Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> createInvoiceSchemes(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return invoiceSchemesDAO.createInvoiceSchemes(inputMap,userInfo);
	}
	/**
	 * This method is used to retrieve list of all active InvoiceSchemes for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active InvoiceSchemes
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getSeletedScheme(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return invoiceSchemesDAO.getSeletedScheme(inputMap,userInfo);
	}
	/**
	 * This method is used to exportschememaster list of all active InvoiceSchemes for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active InvoiceSchemes
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	 @Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> exportschememaster(Map<String, Object> objmap, UserInfo userInfo) throws Exception {
		return invoiceSchemesDAO.exportschememaster(objmap,userInfo);
	}
	/**
	 * This method is used to get  in InvoiceSchemes  table.
	 * @param objInvoiceSchemes [InvoiceSchemes] object holding details to be updated in InvoiceSchemes table
	 * @return response entity object holding response status and data of updated InvoiceSchemes object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	 @Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> getUpdateSchemeDataById(Map<String, Object> inputMap) {
		return invoiceSchemesDAO.getUpdateSchemeDataById(inputMap);
	}
	/**
	 * This method is used to update entry in InvoiceSchemes  table.
	 * @param objInvoiceSchemes [InvoiceSchemes] object holding details to be updated in InvoiceSchemes table
	 * @return response entity object holding response status and data of updated InvoiceSchemes object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	 @Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> updateInvoiceSchemes(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return invoiceSchemesDAO.updateInvoiceSchemes(inputMap,userInfo);

	}
/**
	 * This method is used to activeAndRetiredSchemes list of all active InvoiceSchemes for the specified site.
	 * @param userInfo [UserInfo] primary key of site object for which the list is to be fetched
	 * @return response entity  object holding response status and list of all active InvoiceSchemes
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	 @Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> activeAndRetiredSchemes(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return invoiceSchemesDAO.activeAndRetiredSchemes(inputMap,userInfo);
	}
	/**
	 * This method id used to delete an entry in InvoiceSchemes table
	 * @param objInvoiceSchemes [InvoiceSchemes] an Object holds the record to be deleted
	 * @return a response entity with corresponding HTTP status and an InvoiceSchemes object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Override
	 @Transactional(readOnly = false, rollbackFor = Exception.class)
	public ResponseEntity<Object> deleteInvoiceSchemes(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return invoiceSchemesDAO.deleteInvoiceSchemes(inputMap,userInfo);
	}

}
