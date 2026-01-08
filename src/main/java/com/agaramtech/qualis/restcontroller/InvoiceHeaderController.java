package com.agaramtech.qualis.restcontroller;
 
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import com.agaramtech.qualis.invoice.service.invoiceheader.InvoiceHeaderService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
 
/**
* This controller is used to dispatch the input request to its relevant service
* methods to access the Invoice Service methods.
* IN-453 - Transaction - Invoice
*/

@RestController
@RequestMapping("/invoiceheader")
public class InvoiceHeaderController {
 
	private RequestContext requestContext;
	private final InvoiceHeaderService invoiceHeaderService;

 
	/**

	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 *
	 * @param requestContext       RequestContext to hold the request
	 * @param InvoiceHeaderService InvoiceHeaderService
	 */	
	public InvoiceHeaderController(RequestContext requestContext, InvoiceHeaderService invoiceHeaderService) {
		super();
		this.requestContext = requestContext;
		this.invoiceHeaderService = invoiceHeaderService;
	}
 
	/**
	 * This interface declaration is used to get the overall InvoiceHeader with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */ 
	@PostMapping(value = "/getInvoiceHeader")
	public ResponseEntity<Object> getQuotation(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.getInvoiceHeader(inputMap,userInfo);
	}
 
	/**
	 * This interface declaration is used to add a new entry to InvoiceHeader table.
	 *
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be added in
	 *                         InvoiceHeader table
	 * @return response entity object holding response status and data of added
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */	
	@PostMapping(value = "/createInvoiceHeader")
	public ResponseEntity<Object> createInvoice(@RequestBody Map<String, Object> inputMap) throws Exception {
 
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
 
		return invoiceHeaderService.createInvoice(inputMap, userInfo);
	}
 
	/**
	 * This interface declaration is used to get the overall InvoiceQuotation with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */	
	@PostMapping(value = "/getQuotationTab")
	public ResponseEntity<Object> getQuotationTab(@RequestBody Map<String, Object> inputMap) throws Exception {
		return invoiceHeaderService.getQuotationTab(inputMap);
 
	}
 
	/**
	 * This interface declaration is used to get the overall LIMSArno with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of LIMSArno with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */	
	@PostMapping(value = "/getArnoTab")
	public ResponseEntity<Object> getArnoTab(@RequestBody Map<String, Object> inputMap) throws Exception {
		return invoiceHeaderService.getArnoTab(inputMap);
 
	}
 
	/**
	 * This interface declaration is used to get the SeletedInvoice with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of SeletedInvoice with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */	
	@PostMapping(value = "/getSeletedInvoice")
	public ResponseEntity<Object> getSeletedInvoice(@RequestBody Map<String, Object> inputMap) throws Exception {
		return invoiceHeaderService.getSeletedInvoice(inputMap);
	}
 
	/**
	 * This interface declaration is used to update entry in InvoiceHeader table.
	 *
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be updated
	 *                         in InvoiceHeader table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */	
	@PostMapping(value = "/updateInvoiceHeader")
	public ResponseEntity<Object> updateInvoice(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.updateInvoice(inputMap, userInfo);
	}
 
	/**
	 * This interface declaration is used to delete entry in InvoiceHeader table.
	 *
	 * @param objInvoiceHeader [InvoiceHeader] object holding detail to be deleted
	 *                         in InvoiceHeader table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */	
	@PostMapping(value = "/deleteInvoice")
	public ResponseEntity<Object> deleteInvoice(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.deleteInvoice(inputMap, userInfo);
	}
 
	/**
	 * This interface declaration is used to update entry in ProductInvoice table.
	 *
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be updated
	 *                         in ProductInvoice table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */	
	@PostMapping(value = "/updateProductInvoice")
	public ResponseEntity<Object> updateProductInvoice(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.updateProductInvoice(inputMap, userInfo);
	}
 
	/**
	 * This interface declaration is used to delete entry in ProductInvoiceHeader table.
	 *
	 * @param objInvoiceHeader [InvoiceHeader] object holding detail to be deleted
	 *                         in ProductInvoiceHeader table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */	
	@SuppressWarnings("unchecked")
	@PostMapping(value = "/deleteProductInvoiceHeader")
	public ResponseEntity<Object> deleteProductInvoiceHeader(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		Map<String, Object> parameterValue = (Map<String, Object>) inputMap.get("parameter");
		final UserInfo userInfo = objMapper.convertValue(parameterValue.get("userinfo"), UserInfo.class);
 
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.deleteProductInvoiceHeader(parameterValue, userInfo);
	}
 
	/**
	 * This interface declaration is used to get the overall get scheme product with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of ProductforInvoice with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getProductforInvoice")
	public ResponseEntity<Object> getProductforInvoice(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.getProductforInvoice(inputMap, userInfo);
 
	}
 
	/**
	 * This interface declaration is used to get the overall Invoice Product with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Product InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
		@PostMapping(value = "/getProducts")
	public ResponseEntity<Object> getProducts(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.getProducts(inputMap, userInfo);
 
	}
 
	/**
	 * This interface declaration is used to get Active Invoice with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */	
	@PostMapping(value = "/getActiveInvoiceById")
	public ResponseEntity<Object> getActiveInvoiceById(@RequestBody Map<String, Object> inputMap) throws Exception {
		return invoiceHeaderService.getActiveInvoiceById(inputMap);
	}
 
	/**
	 * This interface declaration is used to update entry in ProductInvoice table.
	 *
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be updated
	 *                         in Invoice Product table
	 * @return response entity object holding response status and data of updated
	 *         Invoice Product object
	 * @throws Exception that are thrown in the DAO layer
	 */	
	@PostMapping(value = "/updateOuterProductInvoice")
	public ResponseEntity<Object> updateOuterProductInvoice(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.updateOuterProductInvoice(inputMap, userInfo);
	}
 
	/**
	 * This interface declaration is used to get the overall Invoice ProductArno with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getInvoiceProductArno")
	public ResponseEntity<Object> getInvoiceProductArno(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.getInvoiceProductArno(inputMap, userInfo);
	}
 
	/**
	 * This interface declaration is used to invoiceReport Generate with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/invoiceReportGenerate")
	public ResponseEntity<Object> invoiceReportGenerate(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.invoiceReportGenerate(inputMap, userInfo);
	}
 
	/**
	 * This interface declaration is used to get the overall Invoice search with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getInvoicesearch")
	public ResponseEntity<Object> getInvoicesearch(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.getInvoicesearch(inputMap, userInfo);
	}
 
	/**
	 * This interface declaration is used to approveInvoice with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/approveInvoiceRecord")
	public ResponseEntity<Object> approveInvoiceRecord(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.approveInvoiceRecord(inputMap, userInfo);
	}
 
	/**
	 * This interface declaration is used to retire Invoice Record with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/retireInvoiceRecord")
	public ResponseEntity<Object> retireInvoiceRecord(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.retireInvoiceRecord(inputMap, userInfo);
	}
 
	/**
	 * This interface declaration is used to get the Product Test Details with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getProductTestDetails")
	public ResponseEntity<Object> getProductTestDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
 
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
 
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.getProductTestDetails(userInfo);
 
	}
 
	/**
	 * This interface declaration is used to get the Active currency with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getActivecurrencyvalue")
	public String getActivecurrency(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.getActivecurrencyvalue(inputMap, userInfo);
	}
 
	/**
	 * This interface declaration is used to get the Patient Details with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getPatientDetails")
	public ResponseEntity<Object> getPatientDetails(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.getPatientDetails(userInfo);
	}
 
	/**
	 * This interface declaration is used to get Search PatientField with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSearchPatientFieldData")
	public ResponseEntity<Object> getSearchPatientFieldData(@RequestBody Map<String, Object> inputMap)
			throws Exception {
 
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.getSearchPatientFieldData(inputMap, userInfo);
 
	}
 
	/**
	 * This interface declaration is used to get the Schemes with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getSchemes")
	public ResponseEntity<Object> getSchemes(@RequestBody Map<String, Object> inputMap) throws Exception {
 
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
 
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.getSchemes(userInfo);
 
	}
 
	/**
	 * This interface declaration is used to update UsercodeForReport.
	 *
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be updated
	 *                         in InvoiceHeader table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/updateUsercodeForReport")
	public ResponseEntity<Object> updateUsercodeForReport(@RequestBody Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.updateUsercodeForReport(inputMap, userInfo);
	}
 
	/**
	 * This interface declaration is used to update cost.
	 *
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be updated
	 *                         in InvoiceHeader Product table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@PostMapping(value = "/costUpdateCost")
	public ResponseEntity<Object> costUpdateCost(@RequestBody Map<String, Object> inputMap) throws Exception {
		return invoiceHeaderService.costUpdateCost(inputMap);
	}
 
	/**
	 * This interface declaration is used to get the Filtered Invoice Record with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@PostMapping(value = "/getFilteredRecord")
	public ResponseEntity<Object> getFilteredRecord(@RequestBody Map<String, Object> inputMap) throws Exception {
 
		final ObjectMapper objmapper = new ObjectMapper();
		final String fromDate = (String) inputMap.get("fromDate");
		final String toDate = (String) inputMap.get("toDate");
		final String breadCrumbFrom = (String) inputMap.get("breadCrumbFrom");
		final String breadCrumbTo = (String) inputMap.get("breadCrumbTo");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
 
		return invoiceHeaderService.getFilteredRecord(userInfo, fromDate, toDate, breadCrumbFrom, breadCrumbTo);
 
	}
	//Added by sonia on 16th oct 2025 for jira id:SWSM-104
	/**
	  * This method is used to Sent the Report By Mail.
	  * @param inputMap  [Map] map object with userInfo [UserInfo] holding logged in user details and nmastersitecode [int] primary key of site object for 
	  * which the list is to be fetched 	
	  * Input : {"userinfo":{nmastersitecode": -1},
	  * "ncontrolcode":1545,
	  * "ninvoiceseqcode":1,
	  * "ntransactionstatus":31							
	  * }				
	  * @return response entity  object holding response status as success
	  * @throws Exception exception
	*/	
	@PostMapping(value = "/sendReportByMail")
	public ResponseEntity<Object> sendReportByMail(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return invoiceHeaderService.sendReportByMail(inputMap, userInfo);
	}
}
 