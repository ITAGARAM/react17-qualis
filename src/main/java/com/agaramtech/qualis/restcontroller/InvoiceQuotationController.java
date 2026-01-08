package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.invoice.service.quotation.InvoiceQuotationService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the InvoiceQuotationService methods.
 * IN-452 - Transaction - Quotation
 */
@RestController
@RequestMapping("/invoicequotation")
public class InvoiceQuotationController {

	private RequestContext requestContext;
	private final InvoiceQuotationService invoiceQuotationService;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param InvoiceQuotationService InvoiceQuotationService
	 */
	public InvoiceQuotationController(RequestContext requestContext, InvoiceQuotationService invoiceQuotationService) {
		super();
		this.requestContext = requestContext;
		this.invoiceQuotationService = invoiceQuotationService;
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
	@PostMapping(value = "/getInvoiceQuotation")
	public ResponseEntity<Object> getQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.getQuotation(inputMap,userInfo);
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
	@PostMapping(value = "/getCustomerQuotation")
	public ResponseEntity<Object> getCustomerQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {
		return invoiceQuotationService.getCustomerQuotation(inputMap);
	}
	/**
	 * This interface declaration is used to get SeletedQuotation with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSeletedQuotation")
	public ResponseEntity<Object> getSeletedQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {
		return invoiceQuotationService.getSeletedQuotation(inputMap);
	}
	/**
	 * This interface declaration is used to get the Payment with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getPayment")
	public ResponseEntity<Object> getPayment(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.getPayment(userInfo);

	}
	/**
	 * This interface declaration is used to get the Currency with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getCurrencyType")
	public ResponseEntity<Object> getCurrencyType(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.getCurrencyType(userInfo);

	}
	/**
	 * This interface declaration is used to get the SearchFieldData InvoiceQuotation with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSearchFieldData")
	public ResponseEntity<Object> getSearchFieldData(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.getSearchFieldData(inputMap, userInfo);

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
	@PostMapping(value = "/createInvoiceQuotation")
	public ResponseEntity<Object> createQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.createQuotation(inputMap, userInfo);
	}
	/**
	 * This interface declaration is used to delete entry in InvoiceQuotation
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding detail to be
	 *                              deleted in InvoiceQuotation table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/deleteInvoiceQuotation")
	public ResponseEntity<Object> deleteQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.deleteQuotation(inputMap, userInfo);
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
	@PostMapping(value = "/getProductforQuotation")
	public ResponseEntity<Object> getProductforQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.getProductforQuotation(inputMap, userInfo);

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
	@PostMapping(value = "/getProducts")
	public ResponseEntity<Object> getProducts(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.getProducts(inputMap, userInfo);

	}
	/**
	 * This interface declaration is used to get the Active currency with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getActivecurrency")
	public String getActivecurrency(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.getActivecurrency(inputMap, userInfo);
	}
	/**
	 * This interface declaration is used to update entry in InvoiceQuotation
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to
	 *                              be updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/updateInvoiceQuotation")
	public ResponseEntity<Object> updateQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.updateQuotation(inputMap, userInfo);
	}
	/**
	 * This interface declaration is used to get Active Quotation with
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to
	 *                              be updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/getActiveInvoiceQuotationById")
	public ResponseEntity<Object> getActiveInvoiceQuotationById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		return invoiceQuotationService.getActiveInvoiceQuotationById(inputMap);
	}
	/**
	 * This interface declaration is used to update Product entry in InvoiceQuotation
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to
	 *                              be updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/updateProductInvoiceQuotation")
	public ResponseEntity<Object> updateProductQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.updateProductQuotation(inputMap, userInfo);
	}
	/**
	 * This interface declaration is used to update entry in ProductQuotation table.
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to
	 *                              be updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/updateOuterProductQuotation")
	public ResponseEntity<Object> updateOuterProductQuotation(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.updateOuterProductQuotation(inputMap, userInfo);
	}
	/**
	 * This interface declaration is used to delete Product entry in InvoiceQuotation
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding detail to be
	 *                              deleted in InvoiceQuotation table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings("unchecked")
	@PostMapping(value = "/deleteProductInvoiceQuotation")
	public ResponseEntity<Object> deleteProductQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();

		Map<String, Object> parameterValue = (Map<String, Object>) inputMap.get("parameter");
		final UserInfo userInfo = objMapper.convertValue(parameterValue.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.deleteProductQuotation(parameterValue, userInfo);
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
	@PostMapping(value = "/getQuotationsearch")
	public ResponseEntity<Object> getQuotationsearch(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.getQuotationsearch(inputMap, userInfo);

	}
	/**
	 * This interface declaration is used to ReportGenerate InvoiceQuotation
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/quotationReportGenerate")
	public ResponseEntity<Object> quotationReportGenerate(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.quotationReportGenerate(inputMap, userInfo);
	}
	/**
	 * This interface declaration is used to add a new entry to InvoiceQuotation table for approve
	 *  
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/approveSelectedQuotation")
	public ResponseEntity<Object> approveQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.approveQuotation(inputMap, userInfo);
	}
	/**
	 * This interface declaration is used to get ProductTestDetails
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/getProductTestDetails")
	public ResponseEntity<Object> getProductTestDetails(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.getProductTestDetails(userInfo);

	}
	/**
	 * This interface declaration is used to get the Schemes with
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/getSchemesDetails")
	public ResponseEntity<Object> getSchemesDetails(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.getSchemesDetails(userInfo);

	}
	/**
	 * This interface declaration is used to update entry in InvoiceQuotation
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to
	 *                              be updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/updateUsercode")
	public ResponseEntity<Object> updateUsercode(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.updateUsercode(inputMap,userInfo);
	}
	/**
	 * This interface declaration is used to get ProjectDetails
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to
	 *                              be updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/getProjectDetails")
	public ResponseEntity<Object> getProjectDetails(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return invoiceQuotationService.getProjectDetails(userInfo);

	}
	/**
	 * This interface declaration is used to update entry in InvoiceQuotation Product
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to
	 *                              be updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/costUpdate")
	public ResponseEntity<Object> costUpdate(@RequestBody Map<String, Object> inputMap) throws Exception {
		return invoiceQuotationService.costUpdate(inputMap);
	}
	/**
	 * This interface declaration is used to getFilteredRecords in InvoiceQuotation
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to
	 *                              be updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/getFilteredRecords")
	public ResponseEntity<Object> getFilteredRecords(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final String fromDate = (String) inputMap.get("fromDate"); 
		final String toDate = (String) inputMap.get("toDate");     
		final String breadCrumbFrom = (String) inputMap.get("breadCrumbFrom"); 
		final String breadCrumbTo = (String) inputMap.get("breadCrumbTo");    
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return invoiceQuotationService.getFilteredRecords(userInfo, fromDate, toDate,breadCrumbFrom,breadCrumbTo);

	}

	
}
