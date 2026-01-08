package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.CustomerFile;
import com.agaramtech.qualis.invoice.model.InvoiceCustomerMaster;
import com.agaramtech.qualis.invoice.model.UserRoleFieldControl;
import com.agaramtech.qualis.invoice.model.UsersRoleField;
import com.agaramtech.qualis.invoice.service.invoicecustomermaster.InvoiceCustomerMasterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the InvoiceCustomerMaster Service methods
 * 
 * IN-447 - InvoiceCustomerMaster
 */

@RestController
@RequestMapping("/invoicecustomermaster")
public class InvoiceCustomerMasterController {

	private RequestContext requestContext;
	private InvoiceCustomerMasterService invoiceCustomerMasterService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext               RequestContext to hold the request
	 * @param invoiceCustomerMasterService invoiceCustomerMasterService
	 */
	public InvoiceCustomerMasterController(RequestContext requestContext,
			InvoiceCustomerMasterService invoiceCustomerMasterService) {
		super();
		this.requestContext = requestContext;
		this.invoiceCustomerMasterService = invoiceCustomerMasterService;
	}

	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	@PostMapping(value = "/getInvoiceCustomerMaster")
	public ResponseEntity<Object> getInvoiceCustomerMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceCustomerMasterService.getInvoiceCustomerMaster(userInfo);

	}

	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	@PostMapping(value = "/getCustomerMaster")
	public ResponseEntity<Object> getCustomerMaster(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceCustomerMasterService.getCustomerMaster(userInfo);

	}

	/**
	 * This method is used to add a new entry to Invoicecustomermaster table. Need
	 * to check for duplicate entry of Invoicecustomermaster name for the specified
	 * site before saving into database. Need to check that there should be only one
	 * default Invoicecustomermaster for a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding
	 *                                 details to be added in Invoicecustomermaster
	 *                                 table
	 * @return inserted Invoicecustomermaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/createInvoiceCustomerMaster")
	public ResponseEntity<Object> createInvoiceCustomerMaster(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		// try {
		ObjectMapper objMapper = new ObjectMapper();
		InvoiceCustomerMaster objInvoiceCustomerMaster = objMapper.convertValue(inputMap.get("invoicecustomermaster"),
				new TypeReference<InvoiceCustomerMaster>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceCustomerMasterService.createInvoiceCustomerMaster(objInvoiceCustomerMaster, userInfo);

	}

	/**
	 * This method is used to update entry in Invoicecustomermaster table. Need to
	 * validate that the Invoicecustomermaster object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * Invoicecustomermaster name for the specified site before saving into
	 * database. Need to check that there should be only one default
	 * Invoicecustomermaster for a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding
	 *                                 details to be updated in
	 *                                 Invoicecustomermaster table
	 * @return response entity object holding response status and data of updated
	 *         Invoicecustomermaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/updateInvoiceCustomerMaster")
	public ResponseEntity<Object> updateInvoiceCustomerMaster(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceCustomerMaster objInvoiceCustomerMaster = objMapper.convertValue(inputMap.get("invoicecustomermaster"),
				new TypeReference<InvoiceCustomerMaster>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceCustomerMasterService.updateInvoiceCustomerMaster(objInvoiceCustomerMaster, userInfo);
	}

	/**
	 * This method id used to delete an entry in Invoicecustomermaster table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as 'invoiceheader','invoicequotationheader'
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] an Object holds the
	 *                                 record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         Invoicecustomermaster object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/deleteInvoiceCustomerMaster")
	public ResponseEntity<Object> deleteInvoiceCustomerMaster(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceCustomerMaster objInvoiceCustomerMaster = objMapper.convertValue(inputMap.get("invoicecustomermaster"),
				new TypeReference<InvoiceCustomerMaster>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceCustomerMasterService.deleteInvoiceCustomerMaster(objInvoiceCustomerMaster, userInfo);

	}

	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	@PostMapping(value = "/getActiveInvoiceCustomerMasterById")
	public ResponseEntity<Object> getActiveInvoiceCustomerMasterById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final int ncustomercode = (Integer) inputMap.get("ncustomercode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceCustomerMasterService.getActiveInvoiceCustomerMasterById(ncustomercode, userInfo);

	}

	/**
	 * This method is used to fetch the active Invoicecustomermaster objects for the
	 * specified Invoicecustomermaster name and site.
	 * 
	 * @param ntypecode       [String] name of the Invoicecustomermaster
	 * @param nmasterSiteCode [int] site code of the Invoicecustomermaster
	 * @return list of active Invoicecustomermaster code(s) based on the specified
	 *         Invoicecustomermaster name and site
	 * @throws Exception
	 */
	@PostMapping(value = "/getSelectedCustomerDetail")
	public ResponseEntity<Object> getSelectedCustomerDetail(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int ncustomercode = (Integer) inputMap.get("ncustomercode");
		requestContext.setUserInfo(userInfo);
		return invoiceCustomerMasterService.getSelectedCustomerDetail(userInfo, ncustomercode);
	}

	/**
	 * This method is used to add a new entry to Invoicecustomermaster table. Need
	 * to check for duplicate entry of Invoicecustomermaster name for the specified
	 * site before saving into database. Need to check that there should be only one
	 * default Invoicecustomermaster for a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding
	 *                                 details to be added in Invoicecustomermaster
	 *                                 table
	 * @return inserted Invoicecustomermaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/createCustomerFile")
	public ResponseEntity<Object> createCustomerFile(MultipartHttpServletRequest request) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceCustomerMasterService.createCustomerFile(request, userInfo);

	}

	/**
	 * This method id used to delete an entry in Invoicecustomermaster table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] an Object holds the
	 *                                 record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         Invoicecustomermaster object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/deleteCustomerFile")
	public ResponseEntity<Object> deleteCustomerFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final CustomerFile objCustomerFile = objMapper.convertValue(inputMap.get("customerfile"), CustomerFile.class);
		requestContext.setUserInfo(userInfo);
		return invoiceCustomerMasterService.deleteCustomerFile(objCustomerFile, userInfo);
	}

	/**
	 * This method is used to update entry in Invoicecustomermaster table. Need to
	 * validate that the Invoicecustomermaster object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * Invoicecustomermaster name for the specified site before saving into
	 * database. Need to check that there should be only one default
	 * Invoicecustomermaster for a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding
	 *                                 details to be updated in
	 *                                 Invoicecustomermaster table
	 * @return response entity object holding response status and data of updated
	 *         Invoicecustomermaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/updateCustomerFile")
	public ResponseEntity<Object> updateCustomerFile(MultipartHttpServletRequest request) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceCustomerMasterService.updateCustomerFile(request, userInfo);

	}

	/**
	 * This method is used to update entry in Invoicecustomermaster table. Need to
	 * validate that the Invoicecustomermaster object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * Invoicecustomermaster name for the specified site before saving into
	 * database. Need to check that there should be only one default
	 * Invoicecustomermaster for a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding
	 *                                 details to be updated in
	 *                                 Invoicecustomermaster table
	 * @return response entity object holding response status and data of updated
	 *         Invoicecustomermaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/editCustomerFile")
	public ResponseEntity<Object> editCustomerFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final CustomerFile objCustomerFile = objMapper.convertValue(inputMap.get("customerfile"), CustomerFile.class);
		requestContext.setUserInfo(userInfo);
		return invoiceCustomerMasterService.editCustomerFile(objCustomerFile, userInfo);

	}

	/**
	 * This method is used to view file from invoiceproductfile table. * @return
	 * response entity object holding response status and data of updated
	 * Invoicecustomermaster object
	 * 
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/viewAttachedCustomerFile")
	public ResponseEntity<Object> viewAttachedCustomerFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final CustomerFile objCustomerFile = objMapper.convertValue(inputMap.get("customerFile"), CustomerFile.class);
		Map<String, Object> outputMap = invoiceCustomerMasterService.viewAttachedCustomerFile(objCustomerFile,
				userInfo);
		requestContext.setUserInfo(userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to update entry in Invoicecustomermaster table. Need to
	 * validate that the Invoicecustomermaster object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * Invoicecustomermaster name for the specified site before saving into
	 * database. Need to check that there should be only one default
	 * Invoicecustomermaster for a site
	 * 
	 * @param objInvoicecustomermaster [Invoicecustomermaster] object holding
	 *                                 details to be updated in
	 *                                 Invoicecustomermaster table
	 * @return response entity object holding response status and data of updated
	 *         Invoicecustomermaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/updateControlRights")
	public ResponseEntity<Object> createControlRights(@RequestBody Map<String, Object> inputMap) throws Exception {

		// try {
		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		Integer nflag = null;
		UserRoleFieldControl userroleController = null;
		if (inputMap.containsKey("screenrights")) {

			userroleController = objmapper.convertValue(inputMap.get("screenrights"), UserRoleFieldControl.class);
		}
		List<UsersRoleField> lstusersrolescreen = (List<UsersRoleField>) objmapper
				.convertValue(inputMap.get("selectedscreenrights"), new TypeReference<List<UsersRoleField>>() {
				});
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		nflag = (Integer) inputMap.get("nflag");

		int nneedrights = (int) inputMap.get("nneedrights");

		requestContext.setUserInfo(userInfo);
		return invoiceCustomerMasterService.createControlRights(userInfo, userroleController, lstusersrolescreen, nflag,
				nneedrights);

	}

}
