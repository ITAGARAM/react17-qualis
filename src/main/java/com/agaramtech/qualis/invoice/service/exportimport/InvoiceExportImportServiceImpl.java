package com.agaramtech.qualis.invoice.service.exportimport;


import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.invoice.model.InvoiceExportImport;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'InvoiceExportImport'
 * table through its DAO layer.
 * 
 *
 */

@Service
public class InvoiceExportImportServiceImpl implements InvoiceExportImportService {


	@Autowired
	private InvoiceExportImportDAO InvoiceExportImportDAO;
	 /**
	 * This method is used to retrieve list of all available InvoiceExportImport for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active InvoiceExportImport
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getInvoiceExportImport(UserInfo userInfo) throws Exception {

		return InvoiceExportImportDAO.getInvoiceExportImport(userInfo);
	}
	/**
	 * Exports the exporttestmaster data into a CSV file for the specified site.
	 * <p>
	 * This method retrieves exporttestmaster records from the database, enriches them with 
	 * product type information, and writes them into a CSV file with a predefined header format.
	 * The generated file is stored in the configured FTP export location.
	 * </p>
	 *
	 * @param objmap   a map containing request parameters (currently unused, reserved for future use)
	 * @param userInfo the user information of the logged-in user (used for language and site-specific processing)
	 * @return a ResponseEntity containing:
	 *         <ul>
	 *             <li><b>ExportExcelPath</b> - the path of the exported CSV file (if successful)</li>
	 *             <li><b>ExportExcel</b> - "Success" if export succeeded</li>
	 *             <li><b>rtn</b> - failure status if export failed</li>
	 *         </ul>
	 * @throws Exception if any I/O or database error occurs during export
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> exportproductmaster(Map<String, Object> objmap, UserInfo userInfo) throws Exception {
		return InvoiceExportImportDAO.exportproductmaster(objmap, userInfo);
	}
	/**
	* This method is used to import ProductMaster data from a CSV file for the specified site.
	*
	* @param objMultipart [MultipartFile] the uploaded CSV file containing ProductMaster data
	* @param userInfo     [UserInfo] the user information of the logged-in user
	* @return ResponseEntity indicating success or failure of the import operation
	* @throws Exception if any error occurs during processing
	*/
	@Override
	public ResponseEntity<Object> importProductMaster(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {

		return InvoiceExportImportDAO.importProductMaster(request, userInfo);
	}
	/**
	 * Exports the customermaster data into a CSV file for the specified site.
	 * <p>
	 * This method retrieves customermaster records from the database, enriches them with 
	 * product type information, and writes them into a CSV file with a predefined header format.
	 * The generated file is stored in the configured FTP export location.
	 * </p>
	 *
	 * @param objmap   a map containing request parameters (currently unused, reserved for future use)
	 * @param userInfo the user information of the logged-in user (used for language and site-specific processing)
	 * @return a ResponseEntity containing:
	 *         <ul>
	 *             <li><b>ExportExcelPath</b> - the path of the exported CSV file (if successful)</li>
	 *             <li><b>ExportExcel</b> - "Success" if export succeeded</li>
	 *             <li><b>rtn</b> - failure status if export failed</li>
	 *         </ul>
	 * @throws Exception if any I/O or database error occurs during export
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> exportcustomermaster(Map<String, Object> objmap, UserInfo userInfo) throws Exception {
		return InvoiceExportImportDAO.exportcustomermaster(objmap, userInfo);
	}
	/**
	* This method is used to import CustomerMaster data from a CSV file for the specified site.
	*
	* @param objMultipart [MultipartFile] the uploaded CSV file containing CustomerMaster data
	* @param userInfo     [UserInfo] the user information of the logged-in user
	* @return ResponseEntity indicating success or failure of the import operation
	* @throws Exception if any error occurs during processing
	*/
	@Override
	@Transactional
	public ResponseEntity<Object> importCustomerMaster(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {

		return InvoiceExportImportDAO.importCustomerMaster(request, userInfo);
	}
	/**
	 * This method is used to merge Client data into the CustomerMaster table
	 * (invoicecustomermaster) for the specified site.
	 * <p>
	 * - If identical client data already exists, new clients are inserted 
	 *   excluding those in the identical list.
	 * - If no identical client data is found, all clients are inserted.
	 * <br>
	 * Each client record is transformed and inserted with proper default values 
	 * for missing fields (like address, GST, phone, email).
	 * </p>
	 *
	 * @param objInvoiceExportImport an object representing Invoice Export/Import request
	 * @param userInfo               user information of the logged-in user
	 * @return ResponseEntity containing the updated InvoiceExportImport records
	 * @throws Exception if any database or processing error occurs
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> mergecustomermaster(InvoiceExportImport objInvoiceExportImport, UserInfo userInfo)
			throws Exception {

		return InvoiceExportImportDAO.mergecustomermaster(objInvoiceExportImport, userInfo);
	}
	/**
	 * This method is used to merge Client data into the productmaster table
	 * (invoiceproductmaster) for the specified site.
	 * <p>
	 * - If identical client data already exists, new clients are inserted 
	 *   excluding those in the identical list.
	 * - If no identical client data is found, all clients are inserted.
	 * <br>
	 * Each client record is transformed and inserted with proper default values 
	 * for missing fields (like address, GST, phone, email).
	 * </p>
	 *
	 * @param objInvoiceExportImport an object representing Invoice Export/Import request
	 * @param userInfo               user information of the logged-in user
	 * @return ResponseEntity containing the updated InvoiceExportImport records
	 * @throws Exception if any database or processing error occurs
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> mergeproductmaster(InvoiceExportImport objInvoiceExportImport, UserInfo userInfo)
			throws Exception {

		return InvoiceExportImportDAO.mergeproductmaster(objInvoiceExportImport, userInfo);
	}
	/**
	 * This method is used to merge Client data into the productmaster table
	 * (invoiceproductmaster) for the specified site.
	 * <p>
	 * - If identical client data already exists, new clients are inserted 
	 *   excluding those in the identical list.
	 * - If no identical client data is found, all clients are inserted.
	 * <br>
	 * Each client record is transformed and inserted with proper default values 
	 * for missing fields (like address, GST, phone, email).
	 * </p>
	 *
	 * @param objInvoiceExportImport an object representing Invoice Export/Import request
	 * @param userInfo               user information of the logged-in user
	 * @return ResponseEntity containing the updated InvoiceExportImport records
	 * @throws Exception if any database or processing error occurs
	 */
	public ResponseEntity<Object> mergeproduct(InvoiceExportImport objInvoiceExportImport, UserInfo userInfo)
			throws Exception {

		return InvoiceExportImportDAO.mergeproduct(objInvoiceExportImport, userInfo);
	}
	/**
	* This method is used to import PatientMaster data from a CSV file for the specified site.
	*
	* @param objMultipart [MultipartFile] the uploaded CSV file containing PatientMaster data
	* @param userInfo     [UserInfo] the user information of the logged-in user
	* @return ResponseEntity indicating success or failure of the import operation
	* @throws Exception if any error occurs during processing

	*/
	@Override
	public ResponseEntity<Object> importPatientMaster(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {

		return InvoiceExportImportDAO.importPatientMaster(request, userInfo);
	}
	/**
	 * This method is used to merge Client data into the patientmaster table
	 * (invoicepatientmaster) for the specified site.
	 * <p>
	 * - If identical client data already exists, new clients are inserted 
	 *   excluding those in the identical list.
	 * - If no identical client data is found, all clients are inserted.
	 * <br>
	 * Each client record is transformed and inserted with proper default values 
	 * for missing fields (like address, GST, phone, email).
	 * </p>
	 *
	 * @param objInvoiceExportImport an object representing Invoice Export/Import request
	 * @param userInfo               user information of the logged-in user
	 * @return ResponseEntity containing the updated InvoiceExportImport records
	 * @throws Exception if any database or processing error occurs
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> mergepatientmaster(InvoiceExportImport objInvoiceExportImport, UserInfo userInfo)
			throws Exception {

		return InvoiceExportImportDAO.mergepatientmaster(objInvoiceExportImport, userInfo);
	}
	/**
	 * Exports the patientmaster data into a CSV file for the specified site.
	 * <p>
	 * This method retrieves patientmaster records from the database, enriches them with 
	 * product type information, and writes them into a CSV file with a predefined header format.
	 * The generated file is stored in the configured FTP export location.
	 * </p>
	 *
	 * @param objmap   a map containing request parameters (currently unused, reserved for future use)
	 * @param userInfo the user information of the logged-in user (used for language and site-specific processing)
	 * @return a ResponseEntity containing:
	 *         <ul>
	 *             <li><b>ExportExcelPath</b> - the path of the exported CSV file (if successful)</li>
	 *             <li><b>ExportExcel</b> - "Success" if export succeeded</li>
	 *             <li><b>rtn</b> - failure status if export failed</li>
	 *         </ul>
	 * @throws Exception if any I/O or database error occurs during export
	 */
	public ResponseEntity<Object> exportpatientmaster(Map<String, Object> objmap, UserInfo userInfo) throws Exception {
		return InvoiceExportImportDAO.exportpatientmaster(objmap, userInfo);
	}

	/**
	 * This method is used to merge Client data into the testmaster table
	 * (testmaster) for the specified site.
	 * <p>
	 * - If identical client data already exists, new clients are inserted 
	 *   excluding those in the identical list.
	 * - If no identical client data is found, all clients are inserted.
	 * <br>
	 * Each client record is transformed and inserted with proper default values 
	 * for missing fields (like address, GST, phone, email).
	 * </p>
	 *
	 * @param objInvoiceExportImport an object representing Invoice Export/Import request
	 * @param userInfo               user information of the logged-in user
	 * @return ResponseEntity containing the updated InvoiceExportImport records
	 * @throws Exception if any database or processing error occurs
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> mergetestmaster(InvoiceExportImport objInvoiceExportImport, UserInfo userInfo)
			throws Exception {
		return InvoiceExportImportDAO.mergetestmaster(objInvoiceExportImport, userInfo);

	}
	@Transactional
	@Override
	public ResponseEntity<Object> mergepackagemaster(InvoiceExportImport objInvoiceExportImport, UserInfo userInfo)
			throws Exception {

		return InvoiceExportImportDAO.mergepackagemaster(objInvoiceExportImport, userInfo);
	}
	
	public ResponseEntity<Object> exportpackagemaster(Map<String, Object> objmap, UserInfo userInfo) throws Exception {
		return InvoiceExportImportDAO.exportpackagemaster(objmap, userInfo);
	}
	@Transactional
	@Override
	public ResponseEntity<Object> importpackagemaster(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {

		return InvoiceExportImportDAO.importpackagemaster(request, userInfo);
	}
}
