package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.agaramtech.qualis.invoice.model.InvoicePaymentMode;
import com.agaramtech.qualis.invoice.service.invoiceheader.InvoiceHeaderService;
import com.agaramtech.qualis.invoice.service.paymentmode.InvoicePaymentModeService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the invoicepaymentmode Service methods. IN-443 PaymentMode
 */
@RestController
@RequestMapping("/invoicepaymentmode")
public class InvoicePaymentModeController {

	private RequestContext requestContext;
	private InvoicePaymentModeService invoicePaymentModeService;
	private ObjectMapper objectMapper;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext            RequestContext to hold the request
	 * @param InvoicePaymentModeService invoicePaymentModeService
	 */
	public InvoicePaymentModeController(RequestContext requestContext,
			InvoicePaymentModeService invoicePaymentModeService) {
		super();
		this.requestContext = requestContext;
		this.invoicePaymentModeService = invoicePaymentModeService;
	}

	/**
	 * This method is used to retrieve list of active InvoicePaymentMode for the
	 * specified site.
	 * 
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active countries that are to be listed
	 *         for the specified site
	 */
	@PostMapping("/getInvoicePaymentMode")
	public ResponseEntity<Object> getInvoicePaymentMode(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoicePaymentModeService.getInvoicePaymentMode(userInfo);
	}

	/**
	 * This method will is used to make a new entry to invoicepaymentmode table.
	 * 
	 * @param inputMap map object holding params ( invoicepaymentmode
	 *                 [InvoicePaymentMode] object holding details to be added in
	 *                 invoicepaymentmode table, userinfo [UserInfo] holding logged
	 *                 in user details ) Input:{ "invoicepaymentmode ": {
	 *                 "spaymentname": "gpay"}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode": -
	 *                 1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 275,"nmastersitecode": -1,"nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         invoicepaymentmode already exists/ list of InvoicePaymentMode along
	 *         with the newly added InvoicePaymentMode .
	 * @throws Exception exception
	 */

	@PostMapping("/createInvoicePaymentMode")
	public ResponseEntity<Object> createInvoicePaymentMode(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		InvoicePaymentMode objInvoicePaymentMode = objMapper.convertValue(inputMap.get("invoicepaymentmode"),
				new TypeReference<InvoicePaymentMode>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoicePaymentModeService.createInvoicePaymentMode(objInvoicePaymentMode, userInfo);
	}

	/**
	 * This method is used to update selected invoicepaymentmode details.
	 * 
	 * @param inputMap [map object holding params( invoicepaymentmode
	 *                 [InvoicePaymentMode] object holding details to be updated in
	 *                 invoicepaymentmode table, userinfo [UserInfo] holding logged
	 *                 in user details) Input:{ "invoicepaymentmode ": {
	 *                 "spaymentname": "gpay"}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 275,"nmastersitecode": -1, "nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * 
	 * 
	 * @return ResponseEntity with string message as 'Already Deleted' if the
	 *         InvoicePaymentMode record is not available/ list of all
	 *         InvoicePaymentMode and along with the updated InvoicePaymentMode.
	 * @throws Exception exception
	 */
	@PostMapping("/updateInvoicePaymentMode")
	public ResponseEntity<Object> updateInvoicePaymentMode(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoicePaymentMode objInvoicePaymentMode = objMapper.convertValue(inputMap.get("invoicepaymentmode"),
				new TypeReference<InvoicePaymentMode>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoicePaymentModeService.updateInvoicePaymentMode(objInvoicePaymentMode, userInfo);
	}

	/**
	 * This method is used to delete an entry in InvoicePaymentMode table
	 * 
	 * @param inputMap [Map] object with keys of InvoicePaymentMode entity and
	 *                 UserInfo object. Input:{ "invoicepaymentmode":
	 *                 {"npaymentmode":1}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 275,"nmastersitecode": -1, "nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         invoicepaymentmode record is not available/ string message as 'Record
	 *         is used in....' when the invoicepaymentmode is associated in
	 *         transaction / list of all InvoicePaymentMode excluding the deleted
	 *         record
	 * @throws Exception exception
	 */
	@PostMapping("/deleteInvoicePaymentMode")
	public ResponseEntity<Object> deleteInvoicePaymentMode(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoicePaymentMode objInvoicePaymentMode = objMapper.convertValue(inputMap.get("invoicepaymentmode"),
				new TypeReference<InvoicePaymentMode>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoicePaymentModeService.deleteInvoicePaymentMode(objInvoicePaymentMode, userInfo);
	}

	/**
	 * This method is used to retrieve a specific invoicepaymentmode record.
	 * 
	 * @param inputMap [Map] map object with "npaymentcode" and "userinfo" as keys
	 *                 for which the data is to be fetched Input:{ "npaymentcode":
	 *                 1, "userinfo":{ "activelanguagelist": ["en-US"],
	 *                 "isutcenabled": 4,"ndeptcode": -1,"ndeputyusercode":
	 *                 -1,"ndeputyuserrole": -1, "nformcode": 275,"nmastersitecode":
	 *                 -1,"nmodulecode": 1, "nreasoncode": 0,"nsitecode":
	 *                 1,"ntimezonecode": -1,"ntranssitecode": 1,"nusercode": -1,
	 *                 "nuserrole": -1,"nusersitecode": -1,"sdatetimeformat":
	 *                 "dd/MM/yyyy HH:mm:ss", "sgmtoffset": "UTC
	 *                 +00:00","slanguagefilename": "Msg_en_US","slanguagename":
	 *                 "English", "slanguagetypecode": "en-US", "spgdatetimeformat":
	 *                 "dd/MM/yyyy HH24:mi:ss", "spgsitedatetime": "dd/MM/yyyy
	 *                 HH24:mi:ss","sreason": "","ssitedate": "dd/MM/yyyy",
	 *                 "ssitedatetime": "dd/MM/yyyy HH:mm:ss"}}
	 * @return ResponseEntity with Invoicepaymentmode object for the specified
	 *         primary key / with string message as 'Deleted' if the
	 *         invoicepaymentmode record is not available
	 * @throws Exception exception
	 */
	@PostMapping("/getActiveInvoicePaymentModeById")
	public ResponseEntity<Object> getActiveInvoicePaymentModeById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		int npaymentcode = (int) inputMap.get("npaymentcode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoicePaymentModeService.getActiveInvoicePaymentModeById(npaymentcode, userInfo);
	}
}
