package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.service.schemes.InvoiceSchemesService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller is used to dispatch the input request to its relevant service
 * methods to access the InvoiceSchemes Service methods. IN-449 Schemes
 */
@RestController
@RequestMapping("/invoiceschemes")

public class InvoiceSchemesController {

	private RequestContext requestContext;
	private InvoiceSchemesService Invoiceschemesservice;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param schemeservice  schemeservice
	 */
	public InvoiceSchemesController(RequestContext requestContext, InvoiceSchemesService Invoiceschemesservice) {
		super();
		this.requestContext = requestContext;
		this.Invoiceschemesservice = Invoiceschemesservice;
	}

	/**
	 * This method is used to retrieve list of available InvoiceSchemes(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         schemes
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getInvoiceSchemes")
	public ResponseEntity<Object> getInvoiceSchemes(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return Invoiceschemesservice.getInvoiceSchemes(userInfo);

	}

	/**
	 * This method is used to exportproductmaster list of available
	 * InvoiceSchemes(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         schemes
	 * @throws Exception exception
	 */
	@PostMapping(value = "/exportproductmaster")
	public ResponseEntity<Object> exportproductmaster(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		UserInfo userInfo = objmapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return Invoiceschemesservice.exportproductmaster(objmap, userInfo);

	}

	/**
	 * This method is used to importProductMaster list of available
	 * InvoiceSchemes(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         schemes
	 * @throws Exception exception
	 */
	@PostMapping(value = "/importProductMaster")
	public ResponseEntity<Object> importProductMaster(MultipartHttpServletRequest request, Map<String, Object> objmap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return Invoiceschemesservice.importProductMaster(request, userInfo);
	}

	/**
	 * This method is used to exportschememaster list of available
	 * InvoiceSchemes(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         schemes
	 * @throws Exception exception
	 */
	@PostMapping(value = "/exportschememaster")
	public ResponseEntity<Object> exportschememaster(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		UserInfo userInfo = objmapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return Invoiceschemesservice.exportschememaster(objmap, userInfo);
	}

	/**
	 * This interface declaration is used to add a new entry to InvoiceSchemes
	 * table.
	 * 
	 * @param objInvoiceSchemes [InvoiceSchemes] object holding details to be added
	 *                          in InvoiceSchemes table
	 * @return response entity object holding response status and data of added
	 *         InvoiceSchemes object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/createInvoiceSchemes")
	public ResponseEntity<Object> createInvoiceSchemes(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return Invoiceschemesservice.createInvoiceSchemes(inputMap, userInfo);

	}

	/**
	 * This method is used to retrieve list of all active InvoiceSchemes for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         InvoiceSchemes
	 * @throws Exception that are thrown from this Service layer
	 */
	@PostMapping(value = "/getSeletedScheme")
	public ResponseEntity<Object> getSeletedScheme(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return Invoiceschemesservice.getSeletedScheme(inputMap, userInfo);
	}

	/**
	 * This method is used to retrieve list of all active InvoiceSchemes for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         InvoiceSchemes
	 * @throws Exception that are thrown from this Service layer
	 */
	@PostMapping(value = "/getUpdateSchemeDataById")
	public ResponseEntity<Object> getUpdateSchemeDataById(@RequestBody Map<String, Object> inputMap) throws Exception {
		return Invoiceschemesservice.getUpdateSchemeDataById(inputMap);
	}

	/**
	 * This method is used to update entry in InvoiceSchemes table.
	 * 
	 * @param objInvoiceSchemes [InvoiceSchemes] object holding details to be
	 *                          updated in InvoiceSchemes table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceSchemes object
	 * @throws Exception that are thrown from this Service layer
	 */
	@PostMapping(value = "/updateInvoiceSchemes")
	public ResponseEntity<Object> updateInvoiceSchemes(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return Invoiceschemesservice.updateInvoiceSchemes(inputMap, userInfo);
	}

	/**
	 * This method is used to activeAndRetiredSchemes list of all active
	 * InvoiceSchemes for the specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         InvoiceSchemes
	 * @throws Exception that are thrown from this Service layer
	 */
	@PostMapping(value = "/activeAndRetiredSchemes")
	public ResponseEntity<Object> activeAndRetiredSchemes(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);

		return Invoiceschemesservice.activeAndRetiredSchemes(inputMap, userInfo);

	}

	/**
	 * This method is used to delete an entry in invoiceschemes table
	 * 
	 * @param inputMap [Map] object with keys of invoiceschemes entity and UserInfo
	 *                 object. Input:{ "invoiceschemes": {"nschemescode":1},
	 *                 "userinfo":{ "activelanguagelist": ["en-US"],"isutcenabled":
	 *                 4,"ndeptcode": -1,"ndeputyusercode": -1,"ndeputyuserrole":
	 *                 -1, "nformcode": 284,"nmastersitecode": -1, "nmodulecode": 1,
	 *                 "nreasoncode": 0,"nsitecode": 1,"ntimezonecode":
	 *                 -1,"ntranssitecode": 1,"nusercode": -1, "nuserrole":
	 *                 -1,"nusersitecode": -1,"sdatetimeformat": "dd/MM/yyyy
	 *                 HH:mm:ss", "sgmtoffset": "UTC +00:00", "slanguagefilename":
	 *                 "Msg_en_US","slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss", "sreason": "",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss"}}
	 * @return ResponseEntity with string message as 'Already deleted' if the
	 *         invoiceschemes record is not available/ string message as 'Record is
	 *         used in....' when the invoiceschemes is associated in transaction /
	 *         list of all invoiceschemes excluding the deleted record
	 * @throws Exception exception
	 */
	@PostMapping(value = "/deleteInvoiceSchemes")
	public ResponseEntity<Object> deleteInvoiceSchemes(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return Invoiceschemesservice.deleteInvoiceSchemes(inputMap, userInfo);
	}
}
