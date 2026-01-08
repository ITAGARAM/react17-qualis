package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.invoice.model.InvoiceProductType;
import com.agaramtech.qualis.invoice.service.invoiceproducttype.InvoiceProductTypeService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the InvoiceProductType Service methods IN-444 Product Type
 */
@RestController
@RequestMapping("/invoiceproducttype")
public class InvoiceProductTypeController {

	private RequestContext requestContext;
	private InvoiceProductTypeService invoiceProductTypeService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext            RequestContext to hold the request
	 * @param invoiceProductTypeService invoiceProductTypeService
	 */
	public InvoiceProductTypeController(RequestContext requestContext,
			InvoiceProductTypeService invoiceProductTypeService) {
		super();
		this.requestContext = requestContext;
		this.invoiceProductTypeService = invoiceProductTypeService;
	}

	/**
	 * This Method is used to get the over all InvoiceProductTypes with respect to
	 * site
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of InvoiceProductType with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping("/getInvoiceProductType")
	public ResponseEntity<Object> getInvoiceProductType(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceProductTypeService.getInvoiceProductType(userInfo);

	}

	/**
	 * This method is used to add a new entry to invoiceProductType table.
	 * 
	 * @param inputMap [Map] holds the InvoiceProductType object to be inserted
	 * @return inserted InvoiceProductType object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception
	 */
	@PostMapping("/createInvoiceProductType")
	public ResponseEntity<Object> createInvoiceProductType(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		InvoiceProductType objInvoiceProductType = objMapper.convertValue(inputMap.get("invoiceproducttype"),
				new TypeReference<>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceProductTypeService.createInvoiceProductType(objInvoiceProductType, userInfo);

	}

	/**
	 * This method is used to update entry in invoiceProductType table.
	 * 
	 * @param inputMap [Map] holds the InvoiceProductType object to be updated
	 * @return response entity object holding response status and data of updated
	 *         InvoiceProductType object
	 * @throws Exception
	 */
	@PostMapping("/updateInvoiceProductType")
	public ResponseEntity<Object> updateInvoiceProductType(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceProductType objInvoiceProductType = objMapper.convertValue(inputMap.get("invoiceproducttype"),
				new TypeReference<>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceProductTypeService.updateInvoiceProductType(objInvoiceProductType, userInfo);

	}

	/**
	 * This method id used to delete an entry in invoiceProductType table
	 * 
	 * @param inputMap [Map] holds the InvoiceProductType object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceProductType object
	 * @throws Exception
	 */
	@PostMapping("/deleteInvoiceProductType")
	public ResponseEntity<Object> deleteInvoiceProductType(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceProductType objInvoiceProductType = objMapper.convertValue(inputMap.get("invoiceproducttype"),
				new TypeReference<>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceProductTypeService.deleteInvoiceProductType(objInvoiceProductType, userInfo);

	}

	/**
	 * This method is used to get the single record in invoiceProductType table
	 * 
	 * @param inputMap [Map] holds the InvoiceProductType code to get
	 * @return response entity object holding response status and data of single
	 *         InvoiceProductType object
	 * @throws Exception
	 */
	@PostMapping("/getActiveInvoiceProductTypeById")
	public ResponseEntity<Object> getActiveInvoiceProductTypeById(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		int ntypecode = (int) inputMap.get("ntypecode");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceProductTypeService.getActiveInvoiceProductTypeById(ntypecode, userInfo);
	}

}
