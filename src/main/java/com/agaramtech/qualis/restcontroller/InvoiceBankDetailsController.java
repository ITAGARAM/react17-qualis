package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.invoice.model.InvoiceBankDetails;
import com.agaramtech.qualis.invoice.service.invoicebankdetails.InvoiceBankDetailsService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service methods
 * to access the Unit Service methods.
 * IN-445 Bank Details
 */
@RestController
@RequestMapping("/bankdetails")

public class InvoiceBankDetailsController {

	private RequestContext requestContext;
	private final InvoiceBankDetailsService invoiceBankDetailsService;
		
	/**
	 * This constructor injection method is used to pass the object dependencies to the class.
	 * @param requestContext RequestContext to hold the request
	 * @param invoiceBankDetailsService InvoiceBankDetailsService
	 */
	public InvoiceBankDetailsController(RequestContext requestContext, InvoiceBankDetailsService invoiceBankDetailsService) {
		super();
		this.requestContext = requestContext;
		this.invoiceBankDetailsService = invoiceBankDetailsService;
	}

	/**
	 * This method is used to retrieve list of available bankdetails(s). 
	 * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched 	
	 * 					Input : {"userinfo":{nmastersitecode": -1}}				
	 * @return response entity  object holding response status and list of all bankdetails
	 * @throws Exception exception
	 */

	@PostMapping(value = "/getBankDetails")
	public ResponseEntity<Object> getBankDetails(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceBankDetailsService.getBankDetails(userInfo);

	}
	
	/**
	 * This method is used to add a new entry to bankdetails table.
	 * 
	 * @param inputMap [Map] holds the bank object to be inserted
	 * @return inserted bankdetails object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception
	 */
	
	@PostMapping(value = "/createBankDetails")
	public ResponseEntity<Object> createBankDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		
		final ObjectMapper objMapper = new ObjectMapper();
		final InvoiceBankDetails objBankDetails = objMapper.convertValue(inputMap.get("bankdetails"), new TypeReference<InvoiceBankDetails>() {});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), 
					new TypeReference<UserInfo>() {});
		requestContext.setUserInfo(userInfo);
		return invoiceBankDetailsService.createBankDetails(objBankDetails, userInfo);
	}
	
	/**
	 * This method is used to get the single record in bankdetails table
	 * 
	 * @param inputMap [Map] holds the bank code to get
	 * @return response entity object holding response status and data of single
	 *         bankdetails object
	 * @throws Exception
	 */

	@PostMapping(value = "/getActiveBankDetailsById")
	public ResponseEntity<Object> getActiveBankDetailsById(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		int bankId = (int) inputMap.get("nbankcode");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceBankDetailsService.getActiveBankDetailsById(bankId, userInfo);

	}
	
	/**
	 * This method is used to update entry in bankdetails table.
	 * 
	 * @param inputMap [Map] holds the bank object to be updated
	 * @return response entity object holding response status and data of updated
	 *         bankdetails object
	 * @throws Exception
	 */

	@PostMapping(value = "/updateBankDetails")
	public ResponseEntity<Object> updateBankDetails(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceBankDetails objBankDetails = objMapper.convertValue(inputMap.get("bankdetails"),
				new TypeReference<InvoiceBankDetails>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceBankDetailsService.updateBankDetails(objBankDetails, userInfo);
	}
	
	/**
	 * This method id used to delete an entry in bankdetails table
	 * 
	 * @param inputMap [Map] holds the bank object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         bankdetails object
	 * @throws Exception
	 */

	@PostMapping(value = "/deleteBankDetails")
	public ResponseEntity<Object> deleteBankDetails(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceBankDetails objBankDetails = objMapper.convertValue(inputMap.get("bankdetails"),
				new TypeReference<InvoiceBankDetails>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceBankDetailsService.deleteBankDetails(objBankDetails, userInfo);

	}
	
}
