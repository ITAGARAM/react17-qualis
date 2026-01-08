package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.InvoiceTaxtype;
import com.agaramtech.qualis.invoice.service.invoicetaxtype.InvoiceTaxtypeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the invoicetaxtype Service methods. IN-446 TaxType
 */

@RestController
@RequestMapping("/invoicetaxtype")
public class InvoiceTaxtypeController {

	private RequestContext requestContext;
	private InvoiceTaxtypeService invoiceTaxtypeService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext        RequestContext to hold the request
	 * @param invoiceTaxtypeService invoiceTaxtypeService
	 */
	public InvoiceTaxtypeController(RequestContext requestContext, InvoiceTaxtypeService invoiceTaxtypeService) {
		super();
		this.requestContext = requestContext;
		this.invoiceTaxtypeService = invoiceTaxtypeService;
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
	@PostMapping("/getInvoiceTaxtype")
	public ResponseEntity<Object> getTaxtype(@RequestBody Map<String, Object> inputMap) throws Exception {

		UserInfo userInfo = new ObjectMapper().convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceTaxtypeService.getTaxtype(userInfo);
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
	@PostMapping("/createInvoiceTaxtype")
	public ResponseEntity<Object> createInvoicetaxtype(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();

		InvoiceTaxtype objTaxtype = objMapper.convertValue(inputMap.get("invoicetaxtype"), new TypeReference<>() {
		});

		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceTaxtypeService.createInvoicetaxtype(objTaxtype, userInfo);

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

	@PostMapping("/updateInvoiceTaxtype")
	public ResponseEntity<Object> updateInvoiceTaxtype(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceTaxtype objTaxtype = objMapper.convertValue(inputMap.get("invoicetaxtype"), new TypeReference<>() {
		});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceTaxtypeService.updateInvoiceTaxtype(objTaxtype, userInfo);

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
	@PostMapping("/deleteInvoiceTaxtype")
	public ResponseEntity<Object> deleteInvoiceTaxtype(@RequestBody Map<String, Object> inputMap) throws Exception {
		// try {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceTaxtype objTaxtype = objMapper.convertValue(inputMap.get("invoicetaxtype"), new TypeReference<>() {
		});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceTaxtypeService.deleteInvoiceTaxtype(objTaxtype, userInfo);

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
	@PostMapping("/getActiveInvoiceTaxtypeById")
	public ResponseEntity<Object> getActiveInvoiceTaxtypeById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		// try {
		ObjectMapper objMapper = new ObjectMapper();
		int ntaxcode = (int) inputMap.get("ntaxcode");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceTaxtypeService.getActiveInvoiceTaxtypeById(ntaxcode, userInfo);
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
	@PostMapping("/getTaxcaltype")
	public ResponseEntity<Object> getTaxcaltype(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return invoiceTaxtypeService.getTaxcaltype(userInfo);
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

	@PostMapping("/getVersionno")
	public ResponseEntity<Object> getVersionno(@RequestBody Map<String, Object> inputMap) throws Exception {

		UserInfo userInfo = new ObjectMapper().convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return invoiceTaxtypeService.getVersionno(userInfo);
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
	@PostMapping("/getStatus")
	public ResponseEntity<Object> getStatus(@RequestBody Map<String, Object> inputMap) throws Exception {

		UserInfo userInfo = new ObjectMapper().convertValue(inputMap.get("userinfo"), UserInfo.class);

		requestContext.setUserInfo(userInfo);
		return invoiceTaxtypeService.getStatus(userInfo);
	}
}
