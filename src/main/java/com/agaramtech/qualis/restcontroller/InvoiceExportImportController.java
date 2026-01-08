package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.invoice.model.InvoiceExportImport;
import com.agaramtech.qualis.invoice.service.exportimport.InvoiceExportImportService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the InvoiceCustomerMaster Service methods
 * 
 * IN-450 - ExportImport
 */

@RestController
@RequestMapping("/invoiceexportimport")
public class InvoiceExportImportController {
	final Log logging = LogFactory.getLog(InvoiceExportImportController.class);

	private RequestContext requestContext;
	private final InvoiceExportImportService InvoiceExportImportService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext             RequestContext to hold the request
	 * @param InvoiceExportImportService InvoiceExportImportService
	 */
	public InvoiceExportImportController(RequestContext requestContext,
			InvoiceExportImportService invoiceExportImportService) {
		super();
		this.requestContext = requestContext;
		this.InvoiceExportImportService = invoiceExportImportService;
	}

	/**
	 * This method is used to retrieve list of all available InvoiceExportImport for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         InvoiceExportImport
	 * @throws Exception that are thrown from this DAO layer
	 */
	@PostMapping(value = "/getInvoiceExportImport")
	public ResponseEntity<Object> getInvoiceExportImport(@RequestBody Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.getInvoiceExportImport(userInfo);

	}

	/**
	 * Exports the ProductMaster data into a CSV file for the specified site.
	 * <p>
	 * This method retrieves ProductMaster records from the database, enriches them
	 * with product type information, and writes them into a CSV file with a
	 * predefined header format. The generated file is stored in the configured FTP
	 * export location.
	 * </p>
	 *
	 * @param objmap   a map containing request parameters (currently unused,
	 *                 reserved for future use)
	 * @param userInfo the user information of the logged-in user (used for language
	 *                 and site-specific processing)
	 * @return a ResponseEntity containing:
	 *         <ul>
	 *         <li><b>ExportExcelPath</b> - the path of the exported CSV file (if
	 *         successful)</li>
	 *         <li><b>ExportExcel</b> - "Success" if export succeeded</li>
	 *         <li><b>rtn</b> - failure status if export failed</li>
	 *         </ul>
	 * @throws Exception if any I/O or database error occurs during export
	 */
	@PostMapping(value = "/exportproductmaster")
	public ResponseEntity<Object> exportproductmaster(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		UserInfo userInfo = objmapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.exportproductmaster(objmap, userInfo);

	}

	/**
	 * This method is used to import ProductMaster data from a CSV file for the
	 * specified site.
	 *
	 * @param objMultipart [MultipartFile] the uploaded CSV file containing
	 *                     ProductMaster data
	 * @param userInfo     [UserInfo] the user information of the logged-in user
	 * @return ResponseEntity indicating success or failure of the import operation
	 * @throws Exception if any error occurs during processing
	 */
	@PostMapping(value = "/importProductMaster")
	public ResponseEntity<Object> importProductMaster(MultipartHttpServletRequest request) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.importProductMaster(request, userInfo);
	}

	/**
	 * Exports the customermaster data into a CSV file for the specified site.
	 * <p>
	 * This method retrieves customermaster records from the database, enriches them
	 * with product type information, and writes them into a CSV file with a
	 * predefined header format. The generated file is stored in the configured FTP
	 * export location.
	 * </p>
	 *
	 * @param objmap   a map containing request parameters (currently unused,
	 *                 reserved for future use)
	 * @param userInfo the user information of the logged-in user (used for language
	 *                 and site-specific processing)
	 * @return a ResponseEntity containing:
	 *         <ul>
	 *         <li><b>ExportExcelPath</b> - the path of the exported CSV file (if
	 *         successful)</li>
	 *         <li><b>ExportExcel</b> - "Success" if export succeeded</li>
	 *         <li><b>rtn</b> - failure status if export failed</li>
	 *         </ul>
	 * @throws Exception if any I/O or database error occurs during export
	 */
	@PostMapping(value = "/exportcustomermaster")
	public ResponseEntity<Object> exportcustomermaster(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		UserInfo userInfo = objmapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.exportcustomermaster(objmap, userInfo);

	}

	/**
	 * This method is used to import CustomerMaster data from a CSV file for the
	 * specified site.
	 *
	 * @param objMultipart [MultipartFile] the uploaded CSV file containing
	 *                     CustomerMaster data
	 * @param userInfo     [UserInfo] the user information of the logged-in user
	 * @return ResponseEntity indicating success or failure of the import operation
	 * @throws Exception if any error occurs during processing
	 */
	@PostMapping(value = "/importCustomerMaster")
	public ResponseEntity<Object> importCustomerMaster(MultipartHttpServletRequest request) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.importCustomerMaster(request, userInfo);
	}

	/**
	 * This method is used to merge Client data into the CustomerMaster table
	 * (invoicecustomermaster) for the specified site.
	 * <p>
	 * - If identical client data already exists, new clients are inserted excluding
	 * those in the identical list. - If no identical client data is found, all
	 * clients are inserted. <br>
	 * Each client record is transformed and inserted with proper default values for
	 * missing fields (like address, GST, phone, email).
	 * </p>
	 *
	 * @param objInvoiceExportImport an object representing Invoice Export/Import
	 *                               request
	 * @param userInfo               user information of the logged-in user
	 * @return ResponseEntity containing the updated InvoiceExportImport records
	 * @throws Exception if any database or processing error occurs
	 */
	@PostMapping(value = "/mergecustomermaster")
	public ResponseEntity<Object> mergecustomermaster(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceExportImport objInvoiceExportImport = objMapper.convertValue(objmap.get("invoiceexportimport"),
				new TypeReference<InvoiceExportImport>() {
				});
		UserInfo userInfo = objmapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.mergecustomermaster(objInvoiceExportImport, userInfo);

	}

	/**
	 * This method is used to merge Client data into the productmaster table
	 * (invoiceproductmaster) for the specified site.
	 * <p>
	 * - If identical client data already exists, new clients are inserted excluding
	 * those in the identical list. - If no identical client data is found, all
	 * clients are inserted. <br>
	 * Each client record is transformed and inserted with proper default values for
	 * missing fields (like address, GST, phone, email).
	 * </p>
	 *
	 * @param objInvoiceExportImport an object representing Invoice Export/Import
	 *                               request
	 * @param userInfo               user information of the logged-in user
	 * @return ResponseEntity containing the updated InvoiceExportImport records
	 * @throws Exception if any database or processing error occurs
	 */
	@PostMapping(value = "/mergeproductmaster")
	public ResponseEntity<Object> mergeproductmaster(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceExportImport objInvoiceExportImport = objMapper.convertValue(objmap.get("invoiceexportimport"),
				new TypeReference<InvoiceExportImport>() {
				});
		UserInfo userInfo = objmapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.mergeproductmaster(objInvoiceExportImport, userInfo);

	}
	@PostMapping(value = "/mergepackagemaster")
	public ResponseEntity<Object> mergepackagemaster(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceExportImport objInvoiceExportImport = objMapper.convertValue(objmap.get("invoiceexportimport"),
				new TypeReference<InvoiceExportImport>() {
				});
		UserInfo userInfo = objmapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.mergepackagemaster(objInvoiceExportImport, userInfo);

	}
	/**
	 * This method is used to merge Client data into the productmaster table
	 * (invoiceproductmaster) for the specified site.
	 * <p>
	 * - If identical client data already exists, new clients are inserted excluding
	 * those in the identical list. - If no identical client data is found, all
	 * clients are inserted. <br>
	 * Each client record is transformed and inserted with proper default values for
	 * missing fields (like address, GST, phone, email).
	 * </p>
	 *
	 * @param objInvoiceExportImport an object representing Invoice Export/Import
	 *                               request
	 * @param userInfo               user information of the logged-in user
	 * @return ResponseEntity containing the updated InvoiceExportImport records
	 * @throws Exception if any database or processing error occurs
	 */
	@PostMapping(value = "/mergeproduct")
	public ResponseEntity<Object> mergeproduct(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceExportImport objInvoiceExportImport = objMapper.convertValue(objmap.get("invoiceexportimport"),
				new TypeReference<InvoiceExportImport>() {
				});
		UserInfo userInfo = objmapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.mergeproduct(objInvoiceExportImport, userInfo);

	}

	/**
	 * This method is used to import PatientMaster data from a CSV file for the
	 * specified site.
	 *
	 * @param objMultipart [MultipartFile] the uploaded CSV file containing
	 *                     PatientMaster data
	 * @param userInfo     [UserInfo] the user information of the logged-in user
	 * @return ResponseEntity indicating success or failure of the import operation
	 * @throws Exception if any error occurs during processing
	 * 
	 */
	@PostMapping(value = "/importpatientmaster")
	public ResponseEntity<Object> importPatientMaster(MultipartHttpServletRequest request) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.importPatientMaster(request, userInfo);
	}

	/**
	 * This method is used to merge Client data into the patientmaster table
	 * (invoicepatientmaster) for the specified site.
	 * <p>
	 * - If identical client data already exists, new clients are inserted excluding
	 * those in the identical list. - If no identical client data is found, all
	 * clients are inserted. <br>
	 * Each client record is transformed and inserted with proper default values for
	 * missing fields (like address, GST, phone, email).
	 * </p>
	 *
	 * @param objInvoiceExportImport an object representing Invoice Export/Import
	 *                               request
	 * @param userInfo               user information of the logged-in user
	 * @return ResponseEntity containing the updated InvoiceExportImport records
	 * @throws Exception if any database or processing error occurs
	 */
	@PostMapping(value = "/mergepatientmaster")
	public ResponseEntity<Object> mergepatientmaster(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceExportImport objInvoiceExportImport = objMapper.convertValue(objmap.get("invoiceexportimport"),
				new TypeReference<InvoiceExportImport>() {
				});
		UserInfo userInfo = objmapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.mergepatientmaster(objInvoiceExportImport, userInfo);

	}

	/**
	 * Exports the patientmaster data into a CSV file for the specified site.
	 * <p>
	 * This method retrieves patientmaster records from the database, enriches them
	 * with product type information, and writes them into a CSV file with a
	 * predefined header format. The generated file is stored in the configured FTP
	 * export location.
	 * </p>
	 *
	 * @param objmap   a map containing request parameters (currently unused,
	 *                 reserved for future use)
	 * @param userInfo the user information of the logged-in user (used for language
	 *                 and site-specific processing)
	 * @return a ResponseEntity containing:
	 *         <ul>
	 *         <li><b>ExportExcelPath</b> - the path of the exported CSV file (if
	 *         successful)</li>
	 *         <li><b>ExportExcel</b> - "Success" if export succeeded</li>
	 *         <li><b>rtn</b> - failure status if export failed</li>
	 *         </ul>
	 * @throws Exception if any I/O or database error occurs during export
	 */
	@PostMapping(value = "/exportpatientmaster")
	public ResponseEntity<Object> exportpatientmaster(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		UserInfo userInfo = objmapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.exportpatientmaster(objmap, userInfo);

	}

	/**
	 * This method is used to merge Client data into the testmaster table
	 * (testmaster) for the specified site.
	 * <p>
	 * - If identical client data already exists, new clients are inserted excluding
	 * those in the identical list. - If no identical client data is found, all
	 * clients are inserted. <br>
	 * Each client record is transformed and inserted with proper default values for
	 * missing fields (like address, GST, phone, email).
	 * </p>
	 *
	 * @param objInvoiceExportImport an object representing Invoice Export/Import
	 *                               request
	 * @param userInfo               user information of the logged-in user
	 * @return ResponseEntity containing the updated InvoiceExportImport records
	 * @throws Exception if any database or processing error occurs
	 */
	@PostMapping(value = "/mergetestmaster")
	public ResponseEntity<Object> mergetestmaster(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceExportImport objInvoiceExportImport = objMapper.convertValue(objmap.get("invoiceexportimport"),
				new TypeReference<InvoiceExportImport>() {
				});
		UserInfo userInfo = objmapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.mergetestmaster(objInvoiceExportImport, userInfo);

	}

	/**
	 * Exports the exporttestmaster data into a CSV file for the specified site.
	 * <p>
	 * This method retrieves exporttestmaster records from the database, enriches
	 * them with product type information, and writes them into a CSV file with a
	 * predefined header format. The generated file is stored in the configured FTP
	 * export location.
	 * </p>
	 *
	 * @param objmap   a map containing request parameters (currently unused,
	 *                 reserved for future use)
	 * @param userInfo the user information of the logged-in user (used for language
	 *                 and site-specific processing)
	 * @return a ResponseEntity containing:
	 *         <ul>
	 *         <li><b>ExportExcelPath</b> - the path of the exported CSV file (if
	 *         successful)</li>
	 *         <li><b>ExportExcel</b> - "Success" if export succeeded</li>
	 *         <li><b>rtn</b> - failure status if export failed</li>
	 *         </ul>
	 * @throws Exception if any I/O or database error occurs during export
	 */
	@PostMapping(value = "/exporttestmaster")
	public ResponseEntity<Object> exporttestmaster(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		UserInfo userInfo = objmapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.exportproductmaster(objmap, userInfo);

	}

	/**
	 * This method is used to import importTestMaster data from a CSV file for the
	 * specified site.
	 *
	 * @param objMultipart [MultipartFile] the uploaded CSV file containing
	 *                     importTestMaster data
	 * @param userInfo     [UserInfo] the user information of the logged-in user
	 * @return ResponseEntity indicating success or failure of the import operation
	 * @throws Exception if any error occurs during processing
	 * 
	 */
	@PostMapping(value = "/importTestMaster")
	public ResponseEntity<Object> importTestMaster(MultipartHttpServletRequest request) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.importProductMaster(request, userInfo);
	}
	
	@PostMapping(value = "/exportpackagemaster")
	public ResponseEntity<Object> exportpackagemaster(@RequestBody Map<String, Object> objmap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		UserInfo userInfo = objmapper.convertValue(objmap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.exportpackagemaster(objmap, userInfo);

	}
	@PostMapping(value = "/importpackagemaster")
	public ResponseEntity<Object> importpackagemaster(MultipartHttpServletRequest request) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return InvoiceExportImportService.importpackagemaster(request, userInfo);
	}
}
