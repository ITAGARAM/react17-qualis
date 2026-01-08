package com.agaramtech.qualis.invoice.service.schemes;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operations on
 * 'InvoiceSchemes' table
 * 
 */
public interface InvoiceSchemesDAO {
	/**
	 * This interface declaration is used to get the overall InvoiceSchemes with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceSchemes with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getInvoiceSchemes(UserInfo userInfo);

	/**
	 * Exports the ProductMaster data into a CSV file for the specified site.
	 * <p>
	 * This method retrieves ProductMaster records from the database, enriches them with 
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
	public ResponseEntity<Object> exportproductmaster(Map<String, Object> objmap, UserInfo userInfo) throws Exception;

	/**
	* This method is used to import ProductMaster data from a CSV file for the specified site.
	*
	* @param objMultipart [MultipartFile] the uploaded CSV file containing ProductMaster data
	* @param userInfo     [UserInfo] the user information of the logged-in user
	* @return ResponseEntity indicating success or failure of the import operation
	* @throws Exception if any error occurs during processing
	*/
	public ResponseEntity<Object> importProductMaster(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception;

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
	public ResponseEntity<Object> createInvoiceSchemes(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to get the overall InvoiceSchemes with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceSchemes with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSeletedScheme(Map<String, Object> inputMap, UserInfo userInfo);

	/**
	 * Exports the schememaster data into a CSV file for the specified site.
	 * <p>
	 * This method retrieves schememaster records from the database, enriches them with 
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
	public ResponseEntity<Object> exportschememaster(Map<String, Object> objmap, UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to get the overall InvoiceSchemes with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceSchemes with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getUpdateSchemeDataById(Map<String, Object> inputMap);

	/**
	 * This interface declaration is used to update entry in InvoiceSchemes table.
	 * 
	 * @param objInvoiceSchemes [InvoiceSchemes] object holding details to be
	 *                          updated in InvoiceSchemes table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceSchemes object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateInvoiceSchemes(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to get the overall InvoiceSchemes with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceSchemes with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> activeAndRetiredSchemes(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in InvoiceSchemes table.
	 * 
	 * @param objInvoiceSchemes [InvoiceSchemes] object holding detail to be deleted
	 *                          in InvoiceSchemes table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceSchemes object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteInvoiceSchemes(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception;

}
