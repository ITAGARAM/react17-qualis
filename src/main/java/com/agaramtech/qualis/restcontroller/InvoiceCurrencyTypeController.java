package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.InvoiceCurrencyType;
import com.agaramtech.qualis.invoice.service.currencytype.InvoiceCurrencyTypeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the InvoiceCurrencyType Service methods IN-441 - Currency Type
 */

@RestController
@RequestMapping("/invoicecurrencytype")
public class InvoiceCurrencyTypeController {

	private InvoiceCurrencyTypeService currencytypeservice;
	private RequestContext requestContext;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext      RequestContext to hold the request
	 * @param currencytypeservice CurrencyTypeService
	 */
	public InvoiceCurrencyTypeController(RequestContext requestContext, InvoiceCurrencyTypeService currencytypeservice) {
		super();
		this.requestContext = requestContext;
		this.currencytypeservice = currencytypeservice;
	}

	/**
	 * This method is used to retrieve list of active currencytype for the specified
	 * site.
	 * 
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active countries that are to be listed
	 *         for the specified site
	 */

	@PostMapping("/getInvoiceCurrencyType")
	public ResponseEntity<Object> getInvoiceCurrencyType(@RequestBody Map<String, Object> inputMap) throws Exception {
		UserInfo userInfo = new ObjectMapper().convertValue(inputMap.get("userinfo"), new TypeReference<>() {
		});
		requestContext.setUserInfo(userInfo);
		return currencytypeservice.getInvoiceCurrencyType(userInfo);
	}

	/**
	 * This method will is used to make a new entry to invoicecurrencytype table.
	 * 
	 * @param inputMap map object holding params ( invoicecurrencytype
	 *                 [InvoiceCurrencyType] object holding details to be added in
	 *                 invoicecurrencytype table, userinfo [UserInfo] holding logged
	 *                 in user details ) Input:{ "invoicecurrencytype ": {
	 *                 "scurrencytypename": "Rupees"}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"], "isutcenabled":
	 *                 4,"ndeptcode": - 1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 273,"nmastersitecode": -1,"nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"} }
	 * @return ResponseEntity with string message as 'Already Exists' if the
	 *         invoicecurrencytype already exists/ list of currencytype along with
	 *         the newly added currencytype .
	 * @throws Exception exception
	 */

	@PostMapping("/createInvoiceCurrencyType")
	public ResponseEntity<Object> createInvoiceCurrencyType(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		InvoiceCurrencyType objInvoiceCurrencyType = objMapper.convertValue(inputMap.get("invoicecurrencytype"),
				new TypeReference<>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<>() {
		});
		requestContext.setUserInfo(userInfo);
		return currencytypeservice.createInvoiceCurrencyType(objInvoiceCurrencyType, userInfo);
	}

	/**
	 * This method is used to retrieve a specific invoicecurrencytype record.
	 * 
	 * @param inputMap [Map] map object with "ncurrencytypecode" and "userinfo" as
	 *                 keys for which the data is to be fetched Input:{
	 *                 "ncurrencytypecode": 1, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"], "isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 273,"nmastersitecode": -1,"nmodulecode": 1, "nreasoncode":
	 *                 0,"nsitecode": 1,"ntimezonecode": -1,"ntranssitecode":
	 *                 1,"nusercode": -1, "nuserrole": -1,"nusersitecode":
	 *                 -1,"sdatetimeformat": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00","slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss","sreason":
	 *                 "","ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with invoicecurrencytype object for the specified
	 *         primary key / with string message as 'Deleted' if the
	 *         invoicecurrencytype record is not available
	 * @throws Exception exception
	 */

	@PostMapping("/getActiveInvoiceCurrencyTypeById")
	public ResponseEntity<Object> getActiveInvoiceCurrencyTypeById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		int ncurrencycode = (int) inputMap.get("ncurrencycode");
		UserInfo userInfo = new ObjectMapper().convertValue(inputMap.get("userinfo"), new TypeReference<>() {
		});
		requestContext.setUserInfo(userInfo);
		return currencytypeservice.getActiveInvoiceCurrencyTypeById(ncurrencycode, userInfo);
	}

	/**
	 * This method is used to update selected invoicecurrencytype details.
	 * 
	 * @param inputMap [map object holding params( invoicecurrencytype
	 *                 [InvoiceCurrencyType] object holding details to be updated in
	 *                 invoicecurrencytype table, userinfo [UserInfo] holding logged
	 *                 in user details) Input:{ "invoicecurrencytype ": {
	 *                 "scurrencytypename": "Rupees"}, "userinfo":{
	 *                 "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 273,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @throws Exception exception
	 */
	@PostMapping("/updateInvoiceCurrencyType")
	public ResponseEntity<Object> updateInvoiceCurrencyType(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceCurrencyType objInvoiceCurrencyType = objMapper.convertValue(inputMap.get("invoicecurrencytype"),
				new TypeReference<>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<>() {
		});
		requestContext.setUserInfo(userInfo);
		return currencytypeservice.updateInvoiceCurrencyType(objInvoiceCurrencyType, userInfo);
	}

	/**
	 * This method is used to delete an entry in invoicecurrencytype table
	 * 
	 * @param inputMap [Map] object with keys of invoicecurrencytype entity and
	 *                 UserInfo object. Input:{ "invoicecurrencytype":
	 *                 {"ncurrencycode":1}, "userinfo":{ "activelanguagelist":
	 *                 ["en-US"],"isutcenabled": 4,"ndeptcode":
	 *                 -1,"ndeputyusercode": -1,"ndeputyuserrole": -1, "nformcode":
	 *                 273,"nmastersitecode": -1, "nmodulecode": 1, "nreasoncode":
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
	 *         invoicecurrencytype record is not available/ string message as
	 *         'Record is used in....' when the invoicecurrencytype is associated in
	 *         transaction / list of all currencytype excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping("/deleteInvoiceCurrencyType")
	public ResponseEntity<Object> deleteInvoiceCurrencyType(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceCurrencyType objInvoiceCurrencyType = objMapper.convertValue(inputMap.get("invoicecurrencytype"),
				new TypeReference<>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<>() {
		});
		requestContext.setUserInfo(userInfo);
		return currencytypeservice.deleteInvoiceCurrencyType(objInvoiceCurrencyType, userInfo);
	}
}
