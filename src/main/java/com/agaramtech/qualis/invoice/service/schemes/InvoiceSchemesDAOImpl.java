package com.agaramtech.qualis.invoice.service.schemes;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.InvoiceTaxCalType;
import com.agaramtech.qualis.invoice.model.InvoiceExportImport;
import com.agaramtech.qualis.invoice.model.InvoiceProductMaster;
import com.agaramtech.qualis.invoice.model.InvoiceSchemes;
import com.agaramtech.qualis.invoice.model.InvoiceSchemesProduct;
import com.agaramtech.qualis.invoice.model.InvoiceTaxtype;
import com.agaramtech.qualis.invoice.model.ProductTest;
import com.agaramtech.qualis.invoice.model.TaxProductDetails;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.RFC4180ParserBuilder;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "InvoiceSchemes" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class InvoiceSchemesDAOImpl implements InvoiceSchemesDAO {

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	/**
	 * This method is used to retrieve list of all available InvoiceSchemes for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         InvoiceSchemes
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getInvoiceSchemes(final UserInfo userInfo) {
		final Map<String, Object> objmap = new HashMap<String, Object>();

//		final String str = "select inv.nschemecode,inv.ntransactionstatus,inv.sschemename,"
//				+ "COALESCE(TO_CHAR(inv.dfromdate,'" + userInfo.getSsitereportdate() + "'),'') as sfromdate,"
//				+ "COALESCE(TO_CHAR(inv.dtodate,'" + userInfo.getSsitereportdate() + "'),'') as stodate,"
//				+ "ts.stransstatus as stransdisplaystatus from invoiceschemes inv "
//				+ "join transactionstatus ts ON ts.ntranscode = inv.ntransactionstatus " + " where inv.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and inv.nsitecode="+userInfo.getNmastersitecode()
//				+ " and inv.nschemecode<>nsitecode order by nschemecode desc";

		final String str = "SELECT inv.nschemecode, inv.ntransactionstatus, inv.sschemename, "
			    + "COALESCE(TO_CHAR(inv.dfromdate,'" + userInfo.getSsitereportdate() + "'), '') AS sfromdate, "
			    + "COALESCE(TO_CHAR(inv.dtodate,'" + userInfo.getSsitereportdate() + "'), '') AS stodate, "
			    + "ts.stransstatus AS stransdisplaystatus, "
			    + "inv.nsitecode, inv.nstatus " 
			    + "FROM invoiceschemes inv "
			    + "JOIN transactionstatus ts ON ts.ntranscode = inv.ntransactionstatus " 
			    + "WHERE inv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			    + " AND inv.nsitecode = " + userInfo.getNmastersitecode()
			    + " AND inv.nschemecode <> " + userInfo.getNmastersitecode() // Fixed: compare with value, not column name
			    + " ORDER BY inv.nschemecode DESC";
		
		final List<InvoiceSchemes> lstSchemes = jdbcTemplate.query(str, new InvoiceSchemes());
		final String strexport = "select * from invoiceexportimport where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and nmastersitecode="+userInfo.getNmastersitecode();

		final List<InvoiceExportImport> lstSchemesexport = jdbcTemplate.query(strexport, new InvoiceExportImport());

		if (!lstSchemes.isEmpty()) {
			final List<InvoiceProductMaster> productList = getProductsforSchemes(lstSchemes.get(0).getNschemecode(),userInfo);
			objmap.put("selectedProduct", productList);
			objmap.put("schemeList", lstSchemes);
			objmap.put("Export", lstSchemesexport);
			objmap.put("selectedScheme", lstSchemes.get(0));
		} else {
			objmap.put("schemeList", lstSchemes);
		}
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all available InvoiceSchemes for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         InvoiceSchemes
	 * @throws Exception that are thrown from this DAO layer
	 */
	private List<InvoiceProductMaster> getProductsforSchemes(final int nschemecode,final UserInfo userInfo) {

		final String strQuery = "select ncost,jsondata->>'nproductcode' as nproductcode,jsondata->>'sproductname' as sproductname,"
				+ "jsondata->>'nlimsproduct' as nlimsproduct,jsondata1->>'ntax' as ntax,jsondata1->>'staxname' as staxname,"
				+ "jsondata1->>'nindirecttax' as nindirectax,jsondata1->>'sindirecttaxname' as sindirecttaxname"
				+ " from invoiceschemesproducts where nschemecode=" + nschemecode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userInfo.getNmastersitecode()+" order by nproductcode desc ";

		final List<InvoiceProductMaster> filteredList = (List<InvoiceProductMaster>) jdbcTemplate.query(strQuery,
				new InvoiceProductMaster());
		return filteredList;
	}

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
	@Override
	public ResponseEntity<Object> exportproductmaster(Map<String, Object> objmap, UserInfo userInfo) throws Exception {
		Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		List<InvoiceProductMaster> lstdata = (List<InvoiceProductMaster>) getexportdatas(userInfo);

		if (lstdata.size() != 0) {

			final String sFileName = "InvoiceProductMasterScheme";
			final String OutputFileName = sFileName + ".csv";

			final String homePath = ftpUtilityFunction.getFileAbsolutePath();

			String target = System.getenv(homePath);

			target = target.trim();

			final String targetPathexport = target + Enumeration.FTP.INVOICE_EXPORT.getFTP() + OutputFileName;

			File file = new File(targetPathexport);

			FileWriter outputfile = new FileWriter(file);

			CSVWriter writer = new CSVWriter(outputfile);
			String[] header = { "Product Code", "Product Name", "OldCost", "NewCost", "TestName" };

			writer.writeNext(header);

			String ProductName = "";
			for (InvoiceProductMaster objcol2 : lstdata) {

				int ProductCode = objcol2.getNproductcode();
				String Productcodes = String.valueOf(ProductCode);

				ProductName = objcol2.getSproductname();

				Double Cost = objcol2.getNcost();

				String str2 = Double.toString(Cost);
				String productQuery = "SELECT sproductname, nlimsproduct FROM invoiceproductmaster WHERE nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND nproductcode = "
						+ ProductCode + " and nsitecode="+userInfo.getNmastersitecode();
				List<InvoiceProductMaster> productNameList = jdbcTemplate.query(productQuery,
						new InvoiceProductMaster());

				if (productNameList.isEmpty()) {
					continue; // Skip if product not found
				}
				String result;

				if (productNameList.get(0).getNlimsproduct() > 1) {
					String testQuery = "SELECT sproducttestname FROM producttest WHERE nlimsproductcode = "
							+ productNameList.get(0).getNlimsproduct() + " and nmastersitecode="+userInfo.getNmastersitecode();
					List<ProductTest> productTests = jdbcTemplate.query(testQuery, new ProductTest());
					List<String> testNames = productTests.stream().map(ProductTest::getSproducttestname)
							.collect(Collectors.toList());
					result = String.join(",\n", testNames);

				} else {
					result = "NA";
				}
				String[] exportproductdata = { Productcodes, ProductName, str2, str2, result };
				writer.writeNext(exportproductdata);
			}
			writer.flush();
			writer.close();
			String sDownloadPathName = getdownloadpathname(userInfo);
			if (sDownloadPathName != null) {
				outputMap.put("ExportExcelPath", sDownloadPathName + "InvoiceProductMasterScheme" + ".csv");
				outputMap.put("ExportExcel", "Success");
			} else {
				outputMap.put("rtn", Enumeration.ReturnStatus.FAILED.getreturnstatus().trim().toString());
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_NOPRODUCTMASTER", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

	}
	/**
	 * This method is used to find the ColumnName in InvoiceSchemes object based on
	 * the specified nschemecode.
	 *
	 */
	public List<String> findColumnName(final Object object) throws Exception {
		final List<String> lsts = new ArrayList<String>();
		final Field[] fields = object.getClass().getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			lsts.add(field.getName());
		}
		return lsts;
	}

	/**
	 * This method is used to retrieve active InvoiceSchemes object based on the
	 * specified nschemecode.
	 * 
	 * @param nschemesCode [int] primary key of InvoiceSchemes object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         InvoiceSchemes object
	 * @throws Exception that are thrown from this DAO layer
	 */

	public List<InvoiceProductMaster> getexportdatas(final UserInfo userInfo) throws Exception {

		final String queryString = " SELECT * from invoiceproductmaster where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userInfo.getNmastersitecode()+" GROUP BY nproductcode";

		final List<InvoiceProductMaster> lsttaxDetails = (List<InvoiceProductMaster>) jdbcTemplate.query(queryString,
				new InvoiceProductMaster());
		return lsttaxDetails;
	}

	/**
	 * This method is used to retrieve active InvoiceSchemes object based on the
	 * specified nschemecode.
	 * 
	 * @param nschemesCode [int] primary key of InvoiceSchemes object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         InvoiceSchemes object
	 * @throws Exception that are thrown from this DAO layer
	 */
	public String getdownloadpathname(final UserInfo userInfo) throws Exception {
		final String query = "select * from settings where nsettingcode=94 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final Settings site = (Settings) jdbcUtilityFunction.queryForObject(query, Settings.class, jdbcTemplate);
		return site.getSsettingvalue();
	}

	/**
	 * This method is used to add a new entry to InvoiceSchemes table. schemes Name
	 * is unique across the database. Need to check for duplicate entry of schemes
	 * name for the specified site before saving into database. * Need to check that
	 * there should be only one default InvoiceSchemes for a site.
	 * 
	 * @param objInvoiceSchemes [InvoiceSchemes] object holding details to be added
	 *                          in InvoiceSchemes table
	 * @param userInfo          [UserInfo] holding logged in user details based on
	 *                          which the list is to be fetched
	 * @return saved schemesmode object with status code 200 if saved successfully
	 *         else if the InvoiceSchemes already exists, response will be returned
	 *         as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createInvoiceSchemes(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final JSONObject actionType = new JSONObject();
		// get used for data is already exists or not
		final List<InvoiceSchemes> invoiceDetails = getInvoiceSchemesData(inputMap.get("sschemename").toString(),userInfo);

		if (invoiceDetails.isEmpty()) {
			int seqnoinvoiceschemes = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoinvoice where stablename='invoiceschemes'", Integer.class);
			seqnoinvoiceschemes++;
			final String str = " INSERT INTO invoiceschemes(nschemecode,sschemename, dfromdate,dtodate,ntransactionstatus,dmodifieddate,nsitecode,nstatus)"
					+ " VALUES(" + seqnoinvoiceschemes + ",'"
					+ stringUtilityFunction.replaceQuote(inputMap.get("sschemename").toString()) + "','"
					+ inputMap.get("dfromdate") + "','" + inputMap.get("dtodate") + "',"
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + +userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

			jdbcTemplate.execute(str);
			jdbcTemplate.execute("update seqnoinvoice set nsequenceno=" + seqnoinvoiceschemes
					+ "  where stablename='invoiceschemes' ");

			inputMap.put("nflag", 1);
			multilingualIDList.add("IDS_ADDSCHEMES");
			actionType.put("InvoiceSchemes", "IDS_ADDSCHEMES");
			auditUtilityFunction.fnInsertAuditAction(getInvoiceSchemesaudit(seqnoinvoiceschemes, userInfo), 1, null,
					multilingualIDList, userInfo);

			return getInvoiceSchemes(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to retrieve active InvoiceSchemes object based on the
	 * specified nschemecode.
	 * 
	 * @param nschemesCode [int] primary key of InvoiceSchemes object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         InvoiceSchemes object
	 * @throws Exception that are thrown from this DAO layer
	 */
	public List<InvoiceSchemes> getInvoiceSchemesaudit(final int nschemecode, final UserInfo userInfo) throws Exception {

		final String str = "SELECT inv.nschemecode, inv.ntransactionstatus, inv.sschemename, "
				+ "COALESCE(TO_CHAR(inv.dfromdate,'" + userInfo.getSsitereportdate() + "'),'') AS sfromdate, "
				+ "COALESCE(TO_CHAR(inv.dtodate,'" + userInfo.getSsitereportdate() + "'),'') AS stodate, "
				+ "ts.stransstatus AS stransdisplaystatus " + "FROM invoiceschemes inv "
				+ "JOIN transactionstatus ts ON ts.ntranscode = inv.ntransactionstatus " + " where nschemecode='"
				+ nschemecode + "' and inv.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and inv.nsitecode="+userInfo.getNmastersitecode();
		final List<InvoiceSchemes> lstSchemes = (List<InvoiceSchemes>) jdbcTemplate.query(str, new InvoiceSchemes());
		return lstSchemes;
	}

	/**
	 * This method is used to retrieve active InvoiceSchemes object based on the
	 * specified nschemecode.
	 * 
	 * @param nschemesCode [int] primary key of InvoiceSchemes object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         InvoiceSchemes object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSeletedScheme(final Map<String, Object> inputMap, final UserInfo userInfo) {
		final Map<String, Object> objmap = new HashMap<String, Object>();
		final String str = "select inv.nschemecode,inv.ntransactionstatus,inv.sschemename,COALESCE(TO_CHAR(inv.dfromdate,'"
				+ userInfo.getSsitereportdate() + "'),'') as sfromdate," + "COALESCE(TO_CHAR(inv.dtodate,'"
				+ userInfo.getSsitereportdate() + "'),'') as stodate,"
				+ "ts.stransstatus as stransdisplaystatus from invoiceschemes inv join transactionstatus ts ON ts.ntranscode = inv.ntransactionstatus where inv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nschemecode="
				+ inputMap.get("nschemecode") + " and inv.nsitecode="+userInfo.getNmastersitecode()+" order by nschemecode";
		final List<InvoiceSchemes> lstSchemes = jdbcTemplate.query(str, new InvoiceSchemes());
		final String strquery = "select * from invoiceexportimport where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmastersitecode="+userInfo.getNmastersitecode();
		final List<InvoiceExportImport> lstSchemesexportimport = jdbcTemplate.query(strquery, new InvoiceExportImport());
		final List<InvoiceProductMaster> productList = getProductsforSchemes(
				lstSchemes.get(lstSchemes.size() - 1).getNschemecode(),userInfo);
		objmap.put("selectedProduct", productList);
		objmap.put("Export", lstSchemesexportimport);
		objmap.put("selectedScheme", lstSchemes.get(lstSchemes.size() - 1));

		return new ResponseEntity<>(objmap, HttpStatus.OK);

	}

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
	@Override
	public ResponseEntity<Object> exportschememaster(final Map<String, Object> objmap, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		final int schemeId = (int) objmap.get("Schemeid");

		try {
			// Check scheme status

			final String strScheme = "SELECT * FROM invoiceschemes WHERE nschemecode = " + schemeId + " AND nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			+" and nsitecode="+userInfo.getNmastersitecode();

			final List<InvoiceSchemes> schemes = jdbcTemplate.query(strScheme, new InvoiceSchemes());

			if (schemes.isEmpty()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SCHEMENOTFOUNDALERT", userInfo.getSlanguagefilename()),
						HttpStatus.NOT_FOUND);
			}

			final int transactionStatus = schemes.get(0).getNtransactionstatus();
			if (transactionStatus != Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ACTIVEEXPORT", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			}

			// Fetch scheme products
			final String strProducts = "select * FROM invoiceschemesproducts WHERE nschemecode = " + schemeId
					+ " AND nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode="+userInfo.getNmastersitecode();

			final List<InvoiceSchemesProduct> schemeDetails = jdbcTemplate.query(strProducts, new InvoiceSchemesProduct());

			final String sFileName = "InvoiceScheme";

			final String OutputFileName = sFileName + ".csv";

			final String homePath = ftpUtilityFunction.getFileAbsolutePath();

			String target = System.getenv(homePath);

			target = target.trim();

			final String targetPath = target + Enumeration.FTP.INVOICE_EXPORT.getFTP() + OutputFileName;

			final File file = new File(targetPath);

			try (FileOutputStream fos = new FileOutputStream(file);
					FileWriter outputfile = new FileWriter(file);
					CSVWriter writer = new CSVWriter(outputfile, CSVWriter.DEFAULT_SEPARATOR,
							CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER,
							CSVWriter.DEFAULT_LINE_END)) {

				final String[] header = { "SchemeProduct ID", "Scheme ID", "Product ID", "ProductName", "OldCost", "NewCost",
						"TestName" };
				writer.writeNext(header);

				for (InvoiceSchemesProduct product : schemeDetails) {
					final int schemeProductId = product.getNschemeproductcode();
					final String schemeProduct = String.valueOf(schemeProductId);
					final String schemeCode = String.valueOf(product.getNschemecode());
					final String productCode = String.valueOf(product.getNproductcode());
					final String costStr = Double.toString(product.getNcost());

					final String testNames;
					String tsest = "";

					final StringBuilder formattedTestNames = new StringBuilder();
					final JSONObject jsonObject = new JSONObject(product.getJsondata());
					if (jsonObject.has("sproductstest")) {
						testNames = jsonObject.getString("sproductstest");
						String[] testNamesArray = testNames.split(",");

						// Build the output with each test name on a new line

						for (String testName : testNamesArray) {
							formattedTestNames.append(testName.trim()).append(",\n");
							tsest = formattedTestNames.toString();
						}
					} else {
						tsest = "NA";
					}
					final String productQuery = "SELECT sproductname, nlimsproduct FROM invoiceproductmaster WHERE nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND nproductcode = "
							+ product.getNproductcode() + " and nsitecode="+userInfo.getNmastersitecode();
					final List<InvoiceProductMaster> productNameList = jdbcTemplate.query(productQuery,
							new InvoiceProductMaster());

					if (productNameList.isEmpty()) {
						continue; // Skip if product not found
					}

					final String productName = productNameList.get(0).getSproductname();

					final String[] data = { schemeProduct, schemeCode, productCode, productName, costStr, costStr, tsest };
					writer.writeNext(data);
				}

			}

			final String sDownloadPathName = getdownloadpathname(userInfo);
			if (sDownloadPathName != null) {
				outputMap.put("ExportExcelPath", sDownloadPathName + "InvoiceScheme.csv");
				outputMap.put("ExportExcel", "Success");
			}

		} catch (Exception e) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_EXPORTSCHEMEERROR" + e.getMessage(), userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	* This method is used to import ProductMaster data from a CSV file for the specified site.
	*
	* @param objMultipart [MultipartFile] the uploaded CSV file containing ProductMaster data
	* @param userInfo     [UserInfo] the user information of the logged-in user
	* @return ResponseEntity indicating success or failure of the import operation
	* @throws Exception if any error occurs during processing
	*/

	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> importProductMaster(final MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final Map<String, Object> reg = objectMapper.readValue(request.getParameter("Map"), Map.class);

		final int Scheme = (int) reg.get("scheme");
		final String strer = "select * from invoiceschemes where nschemecode=" + Scheme + ""
				+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and nsitecode=" + userInfo.getNmastersitecode();
		final List<InvoiceSchemes> object = (List<InvoiceSchemes>) jdbcTemplate.query(strer, new InvoiceSchemes());
		String filename = (String) reg.get("Filename");
		filename = filename.replaceAll("\\s*\\(\\d+\\)", "").trim();

		if (object.get(0).getNtransactionstatus() == 8 && filename.matches("InvoiceScheme( \\(\\d+\\))?\\.csv")) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ADDINVOICEPRODMASTERSCHEME",
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

		final MultipartFile objmultipart = request.getFile("InvoiceImportFile");
		if (objmultipart == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTCSVIMPORT", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		String line;
		final InputStream inputStream = objmultipart.getInputStream();

		// FIX: Use CSVReader with proper configuration to handle quoted fields
		final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		String[] headers = null;
		String[] values;
		String query = "";

		if (filename.matches("Invoice(ProductMasterScheme|Scheme)( \\(\\d+\\))?\\.csv")) {
			if (filename.matches("InvoiceProductMasterScheme( \\(\\d+\\))?\\.csv")) {
				final String invschemeprods = "select * from invoiceschemesproducts where  nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and nsitecode=" + userInfo.getNmastersitecode();
				final List<InvoiceSchemesProduct> invoiceschemes = (List<InvoiceSchemesProduct>) jdbcTemplate.query(
						invschemeprods, new InvoiceSchemesProduct());
				final List<Integer> Productcodes = invoiceschemes.stream().map(InvoiceSchemesProduct::getNproductcode)
						.collect(Collectors.toList());
				final List<Double> Productcost = invoiceschemes.stream().map(InvoiceSchemesProduct::getNcost)
						.collect(Collectors.toList());
				final List<Integer> schemeid = invoiceschemes.stream().map(InvoiceSchemesProduct::getNschemecode)
						.collect(Collectors.toList());
				final List<String> stringProductCodes = Productcodes.stream().map(Object::toString)
						.collect(Collectors.toList());
				final String newHeaderproductcode = "nproductcode";
				final String newHeadercost = "ncost";

				final List<String> firstElements = new ArrayList<>();
				final InputStream objinputstream = objmultipart.getInputStream();
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				final byte[] buffer = new byte[1024];
				int len;
				while ((len = objinputstream.read(buffer)) > -1) {
					baos.write(buffer, 0, len);
				}
				baos.flush();

				// FIX: Configure CSVReader to handle quoted fields properly
				try (CSVReader csvReader = new CSVReaderBuilder(br)
						.withCSVParser(new RFC4180ParserBuilder().build())
						.build()) {
					// Read the header row
					final String[] headerscsv = csvReader.readNext();

					if (headerscsv == null) {
						System.out.println("The CSV file is empty or has no headers.");
					}

					// Process each record
					String[] record;
					while ((record = csvReader.readNext()) != null) {
						// Skip rows where first element is empty
						if (record.length > 0 && !record[0].trim().isEmpty()) {
							firstElements.add(record[0]);
						}
					}
				}

				final List<Integer> cleanedList = firstElements.stream().filter(element -> !element.trim().isEmpty())
						.map(element -> {
							try {
								return Integer.parseInt(element.trim());
							} catch (NumberFormatException e) {
								return null;
							}
						}).filter(value -> value != null).collect(Collectors.toList());

				final String sqlQuery = query + "; UPDATE invoiceschemesproducts SET nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " WHERE nproductcode NOT IN ("
						+ cleanedList + ") AND nschemecode=" + Scheme;
				final String sqlQueryschemes = sqlQuery.replace("[", "").replace("]", "");
				jdbcTemplate.execute(sqlQueryschemes);
				final InputStream inputStreams = objmultipart.getInputStream();

				final BufferedReader brcsv = new BufferedReader(new InputStreamReader(inputStreams, StandardCharsets.UTF_8));
				// FIX: Configure CSVReader to handle quoted fields properly
				try (CSVReader csvReaderimportProductMaster = new CSVReaderBuilder(brcsv)
						.withCSVParser(new RFC4180ParserBuilder().build())
						.build()) {
					headers = csvReaderimportProductMaster.readNext();
					final InputStream objinputStream = objmultipart.getInputStream();

					final BufferedReader brcsvimport = new BufferedReader(
							new InputStreamReader(objinputStream, StandardCharsets.UTF_8));
					// FIX: Configure CSVReader to handle quoted fields properly
					try (CSVReader csvReaderexport = new CSVReaderBuilder(brcsvimport)
							.withCSVParser(new RFC4180ParserBuilder().build())
							.build()) {
						final String[] header = csvReaderexport.readNext();
						final int testNameIndex = -1;

						String[] nextLine;
						while ((nextLine = csvReaderexport.readNext()) != null) {
							// FIX: Check for empty values before processing
							if (nextLine.length < 4 || nextLine[0] == null || nextLine[0].trim().isEmpty() || 
								nextLine[3] == null || nextLine[3].trim().isEmpty()) {
								continue; // Skip empty rows
							}
							
							final String pr = nextLine[0];
							final String p = pr.replaceAll("\"", "").trim();

							// FIX: Validate product code is a valid integer before using in SQL
							if (!p.matches("\\d+")) {
								continue; // Skip rows with non-numeric product codes
							}
							
							final String cost = nextLine[3];
							final String newcost = cost.replaceAll("\"", "").trim();
							
							// FIX: Validate cost is not empty before parsing
							if (newcost.isEmpty()) {
								continue; // Skip rows with empty cost
							}
							
							final double costValue;
							try {
								costValue = Double.parseDouble(newcost);
							} catch (NumberFormatException e) {
								continue; // Skip rows with invalid cost format
							}

							if ((stringProductCodes.contains(p) && schemeid.contains(Scheme))) {
								if (!invoiceschemes.isEmpty()) {

									if (!Productcost.contains(costValue)) {
										jdbcTemplate.execute(query + "; update invoiceschemesproducts set ncost = "
												+ newcost + " where nproductcode = " + p + " and nstatus= "
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " and nschemecode=" + Scheme + " ");

									}
								}
							} else {
								query = "INSERT INTO invoiceschemesproducts (";
								query += "nschemeproductcode,nschemecode";
								query += ",";
								for (int i = 0; i < headers.length; i++) {
									if (i <= 1) {
										if (i == 0) {
											headers[0] = newHeaderproductcode;
										}
										if (i == 1) {
											headers[1] = newHeadercost;
										}
										query += headers[i];
										if (i <= 1 - 1) {
											query += ",";
										}
									}
								}
								final String getSequenceNo = "select nsequenceno from seqnoinvoice where stablename ='invoiceschemesproducts'";
								int seqNo = jdbcTemplate.queryForObject(getSequenceNo, Integer.class);
								seqNo++;
								query += ", jsondata,jsondata1, dmodifieddate,nsitecode,nstatus,jsondata2";
								query += ") VALUES (";

								query += seqNo + ",";
								query += Scheme + ",";
								int i = 0;
								String nproductcode = "";
								for (i = 0; i < nextLine.length; i++) {
									if (i == 0) {
										String x = nextLine[0];
										nproductcode = x.replaceAll("\"", "").trim();
										
										// FIX: Validate product code is numeric before using in SQL
										if (!nproductcode.matches("\\d+")) {
											continue; // Skip this row if product code is not numeric
										}
										query += nproductcode;
									}
									if (i == 1) {
										String x = nextLine[3];
										String tax = x.replaceAll("\"", "").trim();
										query += tax;
									}
									if (i <= 1 - 1) {
										query += ",";
									}
								}
								final String x = nextLine[1];
								final String ProductName = x.replaceAll("\"", "");

								final JSONObject jsonObject = new JSONObject();
								final JSONObject jsonObjectobj = new JSONObject();
								final JSONArray jsonArray = new JSONArray();
								final String jsonString;
								
								// FIX: Use parameterized query or validate product code
								final String Product = "Select * from invoiceproductmaster where nproductcode = ? and nstatus= ? and nsitecode= ?";
								final List<InvoiceProductMaster> ProductDetails = jdbcTemplate.query(
									Product, 
									new Object[]{Integer.parseInt(nproductcode), Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), userInfo.getNmastersitecode()},
									new InvoiceProductMaster()
								);
								
								if (ProductDetails.size() == 0) {
									return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALERTEMPTY",
											userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
								} else {
									jsonObject.put("nlimsproduct", ProductDetails.get(0).getNlimsproduct());
									jsonObject.put("nproductcode", ProductDetails.get(0).getNproductcode());
									jsonObject.put("sproductname", ProductName);
									if (ProductDetails.get(0).getNlimsproduct() > 1) {
										final int testNameIndexs = 4;
										// FIX: Check if testNameIndexs is within bounds
										if (nextLine.length > testNameIndexs && nextLine[testNameIndexs] != null) {
											final String testName = nextLine[testNameIndexs];
											final String tests = testName.replaceAll("\\r\\n|\\r|\\n", " ");
											jsonObject.put("sproductstest", tests);
											final String[] splitTests = tests.split(",");
											for (int l = 0; l < splitTests.length; l++) {
												splitTests[l] = splitTests[l].trim();
											}
											final ObjectMapper objectMappernode = new ObjectMapper();
											final ObjectNode outerObjectNode = objectMappernode.createObjectNode();
											final ArrayNode ArrayNodeJsonArray = objectMappernode.createArrayNode();

											// ✅ FIX: Escape apostrophe in test names and use parameterized queries
											for (String test : splitTests) {
												String safeTest = test.replace("'", "''");

												final String querylimsproduct = "SELECT * FROM producttest WHERE nstatus= ? AND sproducttestname = ? AND nlimsproductcode = ? and nmastersitecode= ?";
												final List<ProductTest> testdetails = jdbcTemplate.query(
													querylimsproduct,
													new Object[]{
														Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
														safeTest,
														ProductDetails.get(0).getNlimsproduct(),
														userInfo.getNmastersitecode()
													},
													new ProductTest()
												);

												if (!testdetails.isEmpty()) {
													ObjectNode nodejsonObject = objectMappernode.createObjectNode();
													nodejsonObject.put("sproductstest",
															testdetails.get(0).getSproducttestname());
													nodejsonObject.put("specname", testdetails.get(0).getSspecname());
													nodejsonObject.put("cost", testdetails.get(0).getNtestcost());
													ArrayNodeJsonArray.add(nodejsonObject);
												}
											}
											outerObjectNode.set("TestList", ArrayNodeJsonArray);
											jsonString = objectMappernode.writeValueAsString(outerObjectNode);
										} else {
											jsonString = "{ \"TestList\": [] }";
										}
									} else {
										jsonString = "{ \"TestList\": [] }";
									}

									// TAX details same...
									final String taxproduct = "Select * from taxproductdetails where nproductcode = ? and nstatus= ? and nsitecode= ?";
									final List<TaxProductDetails> taxproductdetails = jdbcTemplate.query(
										taxproduct,
										new Object[]{Integer.parseInt(nproductcode), Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), userInfo.getNmastersitecode()},
										new TaxProductDetails()
									);
									
									if (taxproductdetails.size() != 0) {
										List<Integer> intValues = taxproductdetails.stream()
												.map(TaxProductDetails::getNtaxcode).collect(Collectors.toList());

										// FIX: Use parameterized query for tax details
										final String tax = "Select * from invoicetaxtype where nstatus= ? and nsitecode= ? and ntaxcode in (" + 
											String.join(",", Collections.nCopies(intValues.size(), "?")) + ")";
										List<Object> taxParams = new ArrayList<>();
										taxParams.add(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
										taxParams.add(userInfo.getNmastersitecode());
										taxParams.addAll(intValues);
										
										final List<InvoiceTaxtype> TaxDetails = jdbcTemplate.query(
											tax, 
											taxParams.toArray(),
											new InvoiceTaxtype()
										);
										
										final List<Short> intValue = TaxDetails.stream()
												.map(InvoiceTaxtype::getNcaltypecode).collect(Collectors.toList());
										final String Type = " SELECT *, coalesce(jsondata->'staxcaltype'->>'"
												+ userInfo.getSlanguagetypecode() + "',"
												+ " jsondata->'staxcaltype'->>'en-US') as staxcaltype "
												+ " FROM invoicetaxcaltype where nstatus= ? "
												+ " and nsitecode= ? "
												+ " and ncaltypecode in (" + String.join(",", Collections.nCopies(intValue.size(), "?")) + ")";
										List<Object> typeParams = new ArrayList<>();
										typeParams.add(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
										typeParams.add(userInfo.getNmastersitecode());
										typeParams.addAll(intValue);
										
										final List<InvoiceTaxCalType> Types = jdbcTemplate.query(
											Type,
											typeParams.toArray(),
											new InvoiceTaxCalType()
										);
										
										if (taxproductdetails.size() == 1) {
											jsonObjectobj.put("ntax", TaxDetails.get(0).getNtax());
											jsonObjectobj.put("stype", Types.get(0).getStaxcaltype());
											jsonObjectobj.put("ntaxtype", Types.get(0).getNcaltypecode());
											jsonObjectobj.put("staxname", TaxDetails.get(0).getStaxname());
										} else {
											jsonObjectobj.put("nindirecttax", TaxDetails.get(1).getNtax());
											jsonObjectobj.put("sindirecttype", Types.get(0).getStaxcaltype());
											jsonObjectobj.put("nindirecttaxtype", Types.get(0).getNcaltypecode());
											jsonObjectobj.put("sindirecttaxname", TaxDetails.get(1).getStaxname());
											jsonObjectobj.put("ntax", TaxDetails.get(0).getNtax());
											jsonObjectobj.put("stype", Types.get(1).getStaxcaltype());
											jsonObjectobj.put("ntaxtype", Types.get(1).getNcaltypecode());
											jsonObjectobj.put("staxname", TaxDetails.get(0).getStaxname());
										}
									}
									String escapedJsonObject = stringUtilityFunction.replaceQuote(jsonObject.toString());
									String escapedJsonObjectObj = stringUtilityFunction.replaceQuote(jsonObjectobj.toString());
									String escapedJsonString = stringUtilityFunction.replaceQuote(jsonString);
									query += ",'" + escapedJsonObject + "','" + escapedJsonObjectObj + "'";
									query += ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
											+ userInfo.getNmastersitecode() + ",1,'" + escapedJsonString + "'";
									query += ")";
									jdbcTemplate.execute(query + "; update seqnoinvoice set nsequenceno = " + seqNo
											+ " where stablename='invoiceschemesproducts'");
								}
							}
						}
					}
				}
			}

			else {
				final List<String> firstElements = new ArrayList<>();
				final BufferedReader bre = new BufferedReader(new InputStreamReader(objmultipart.getInputStream(), "UTF-8"));
				final String targetPathcsv;
				final InputStream inputstream = objmultipart.getInputStream();

				final BufferedReader brcsv = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
				// FIX: Configure CSVReader to handle quoted fields properly
				try (CSVReader csvReader = new CSVReaderBuilder(brcsv)
						.withCSVParser(new RFC4180ParserBuilder().build())
						.build()) {
					// Read the header row
					final String[] headerscsv = csvReader.readNext();

					if (headers == null) {
						System.out.println("The CSV file is empty or has no headers.");

					}

					// Process each record
					String[] record;
					while ((record = csvReader.readNext()) != null) {
						// Add the third element (index 2) of the record to the list
						if (record.length > 2) {
							firstElements.add(record[2]);
						}
					}
				}

				final List<Integer> cleanedList = firstElements.stream().filter(element -> !element.trim().isEmpty())
						.map(element -> {
							try {
								return Integer.parseInt(element.trim());
							} catch (NumberFormatException e) {
								// Handle parsing error if needed (e.g., log it)
								return null; // Skip invalid integers
							}
						}).filter(value -> value != null) // Remove null values from the list
						.collect(Collectors.toList());

				// Construct and execute the query
				final String sqlQuery = query + "; UPDATE invoiceschemesproducts SET nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " WHERE nproductcode NOT IN ("
						+ cleanedList + ") AND nschemecode=" + Scheme;
				final String sqlQueryreplace = sqlQuery.replace("[", "").replace("]", "");
				// Replace with your actual method to execute the query
				jdbcTemplate.execute(sqlQueryreplace);
				
				// FIX: Reset the input stream and use proper CSV parsing
				final InputStream resetInputStream = objmultipart.getInputStream();
				final BufferedReader resetBr = new BufferedReader(new InputStreamReader(resetInputStream, StandardCharsets.UTF_8));
				
				// FIX: Use CSVReader with proper configuration for the main processing
				try (CSVReader mainCsvReader = new CSVReaderBuilder(resetBr)
						.withCSVParser(new RFC4180ParserBuilder().build())
						.build()) {
					
					headers = mainCsvReader.readNext(); // Read headers
					
					String[] nextRecord;
					while ((nextRecord = mainCsvReader.readNext()) != null) {
						values = nextRecord;

						final String rowString = String.join(",", values);
						final String str = "select * from invoiceschemesproducts where nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nschemecode="
								+ Scheme + " and nsitecode="+userInfo.getNmastersitecode();
						final List<InvoiceSchemesProduct> objschemes = (List<InvoiceSchemesProduct>) jdbcTemplate
								.query(str, new InvoiceSchemesProduct());

						final List<Integer> Productcodess = objschemes.stream().map(InvoiceSchemesProduct::getNproductcode)
								.collect(Collectors.toList());
						final List<Double> Productcost = objschemes.stream().map(InvoiceSchemesProduct::getNcost)
								.collect(Collectors.toList());
						final List<Integer> schemeid = objschemes.stream().map(InvoiceSchemesProduct::getNschemecode)
								.collect(Collectors.toList());
						final List<String> stringProductCodesschemes = Productcodess.stream().map(Object::toString)
								.collect(Collectors.toList());
						
						// FIX: Check array bounds and empty values
						if (values.length < 6 || values[2] == null || values[2].trim().isEmpty() || 
							values[5] == null || values[5].trim().isEmpty()) {
							continue; // Skip invalid rows
						}
						
						final String n = values[2];
						final String productcodes = n.replaceAll("\"", "").trim();
						
						// FIX: Validate product code is not empty and is numeric
						if (productcodes.isEmpty() || !productcodes.matches("\\d+")) {
							continue;
						}
						
						// FIX: Use parameterized query for product lookup
						final String nproductcodes = "Select nproductcode from invoiceproductmaster where nproductcode= ? and nstatus= ? and nsitecode= ?";
						final List<InvoiceProductMaster> ProductDetailscode = jdbcTemplate.query(
							nproductcodes,
							new Object[]{Integer.parseInt(productcodes), Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), userInfo.getNmastersitecode()},
							new InvoiceProductMaster()
						);
						
						// FIX: Check if product exists
						if (ProductDetailscode.isEmpty()) {
							continue;
						}
						
						final int p = ProductDetailscode.get(0).getNproductcode();
						final String myString = Integer.toString(p);
						final String cost = values[5];
						final String newcost = cost.replaceAll("\"", "").trim();
						
						// FIX: Validate cost is not empty before parsing
						if (newcost.isEmpty()) {
							continue;
						}
						
						final double costValue;
						try {
							costValue = Double.parseDouble(newcost);
						} catch (NumberFormatException e) {
							continue; // Skip rows with invalid cost format
						}
						
						String tests = "";
						List<String> sproduct = new ArrayList<>();
						final String qr = "select * from invoiceschemesproducts where nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nschemecode="
								+ Scheme + " and nsitecode="+userInfo.getNmastersitecode();
						final List<InvoiceSchemesProduct> objschemesproduct = (List<InvoiceSchemesProduct>) jdbcTemplate
								.query(qr, new InvoiceSchemesProduct());

						if (!objschemesproduct.isEmpty()) {
							final JSONObject objschmjsonObject = new JSONObject(objschemesproduct.get(0).getJsondata());
							if (objschmjsonObject.has("sproductstest")) {
								final String sproductstests = objschmjsonObject.getString("sproductstest");
								sproduct = Collections.singletonList(sproductstests);
							}
						}
						
						// FIX: Check array bounds for values[6]
						if (values.length > 6 && (!values[6].equals(sproduct) || !values[6].equals("NA"))) {

							final InputStream objInputStream = objmultipart.getInputStream();

							final BufferedReader brcsvs = new BufferedReader(
									new InputStreamReader(objInputStream, StandardCharsets.UTF_8));
							// FIX: Configure CSVReader to handle quoted fields properly
							try (CSVReader csvReaders = new CSVReaderBuilder(brcsvs)
									.withCSVParser(new RFC4180ParserBuilder().build())
									.build()) {
								final String[] header = csvReaders.readNext(); // Read the header row
								if (header == null) {

								}

								int testNameIndex = -1;
								for (int i = 0; i < header.length; i++) {
									if ("TestName".equals(header[i])) {
										testNameIndex = i;
										break;
									}
								}

								if (testNameIndex == -1) {
									System.out.println("TestName column not found.");

								}

								String[] nextLine;
								while ((nextLine = csvReaders.readNext()) != null) {
									// FIX: Check array bounds for testNameIndex
									if (nextLine.length <= testNameIndex || nextLine[testNameIndex] == null) {
										continue;
									}
									
									final String testName = nextLine[testNameIndex];
									tests = testName.replaceAll("\\r\\n|\\r|\\n", "");
									final String ncode = nextLine[2];
									
									// FIX: Validate product code is numeric
									if (!ncode.matches("\\d+")) {
										continue;
									}
									
									final String[] splitTests = tests.split(",");
									if (splitTests.length > 0 && !splitTests[0].trim().isEmpty()) {
										for (int l = 0; l < splitTests.length; l++) {
											splitTests[l] = splitTests[l].trim();
										}

										final String jsonString;
										// Create a new JSONArray
										// JSONArray jsonArray = new JSONArray();

										final String costs = nextLine[5];
										final String newcosts = costs.replaceAll("\"", "");
										final double costValues;
										try {
											costValues = Double.parseDouble(newcosts);
										} catch (NumberFormatException e) {
											continue; // Skip invalid cost values
										}
										
										final ObjectMapper objectMappers = new ObjectMapper();
										final ObjectNode outerObjectNode = objectMappers.createObjectNode();
										final ArrayNode ArrayNodeJsonArray = objectMappers.createArrayNode();
										final String productCode = ncode.replaceAll("\"", "");
										
										// FIX: Use parameterized query for product lookup
										final String nproductcodesquery = "Select * from invoiceproductmaster where nproductcode= ? and nstatus= ? and nsitecode= ?";
										final List<InvoiceProductMaster> ProductDetails = jdbcTemplate.query(
											nproductcodesquery,
											new Object[]{Integer.parseInt(productCode), Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), userInfo.getNmastersitecode()},
											new InvoiceProductMaster()
										);
										
										// FIX: Check if product details exist
										if (ProductDetails.isEmpty()) {
											continue;
										}
										
										for (String test : splitTests) {
											// FIX: Escape apostrophe in test names and use parameterized queries
											String safeTest = test.replace("'", "''");
											String prodtestquery = "SELECT * FROM producttest WHERE nstatus= ? and nsitecode= ? AND sproducttestname = ? AND nlimsproductcode = ?";
											final List<ProductTest> testdetails = jdbcTemplate.query(
												prodtestquery,
												new Object[]{
													Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
													userInfo.getNmastersitecode(),
													safeTest,
													ProductDetails.get(0).getNlimsproduct()
												},
												new ProductTest()
											);

											if (!testdetails.isEmpty()) {
												final ObjectNode mapperjsonObject = objectMappers.createObjectNode();
												mapperjsonObject.put("sproductstest",
														testdetails.get(0).getSproducttestname());
												mapperjsonObject.put("specname", testdetails.get(0).getSspecname());
												mapperjsonObject.put("cost", testdetails.get(0).getNtestcost());

												// Add the ObjectNode to the ArrayNode
												ArrayNodeJsonArray.add(mapperjsonObject);
											}
										}

										// Set the ArrayNode to the outer ObjectNode
										outerObjectNode.set("TestList", ArrayNodeJsonArray);

										// Convert the outer ObjectNode to a String
										jsonString = objectMappers.writeValueAsString(outerObjectNode);
										System.out.println("Processing ncode: " + ncode); // Debugging output

										// FIXED: Escape JSON string properly
										String escapedJsonString = stringUtilityFunction.replaceQuote(jsonString);
										String escapedTests = stringUtilityFunction.replaceQuote(tests);
										
										query = "UPDATE invoiceschemesproducts " + " SET jsondata2 = '" + escapedJsonString
												+ "' ," + " ncost =  " + costValues + " , "
												+ " jsondata = jsondata || json_build_object('sproductstest', '"
												+ escapedTests + "')::jsonb "
												+ " WHERE nproductcode = " + ncode + " " + " AND nstatus= "
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
												+ " AND nschemecode = " + Scheme;

										jdbcTemplate.execute(query);
									}
								}
							}

						}

						if ((stringProductCodesschemes.contains(myString)) && (schemeid.contains(Scheme))) {
							if (!objschemes.isEmpty()) {
								if (!Productcost.contains(costValue)) {
									jdbcTemplate.execute(query + ";  UPDATE invoiceschemesproducts " + " SET ncost =  "
											+ newcost + " " +

											" WHERE nproductcode = " + myString + " " + "  AND nstatus= "
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ "  AND nschemecode =" + Scheme + "");
								}

							}

							else {

								final String schemecodeHeader = "nschemecode";
								final String productcodeHeader = "nproductcode";
								final String costHeader = "ncost";

								query = "INSERT INTO invoiceschemesproducts (";
								query += "nschemeproductcode,";
								for (int i = 0; i < headers.length; i++) {
									if (i <= 2) {
										if (i == 0) {
											headers[0] = schemecodeHeader;
										}
										if (i == 1) {
											headers[1] = productcodeHeader;
										}
										if (i == 2) {
											headers[2] = costHeader;
										}

										query += headers[i];
										if (i <= 2 - 1) {

											query += ",";

										}
									}
								}
								final String getSequenceNo = "select nsequenceno from seqnoinvoice where stablename ='invoiceschemesproducts'";
								int seqNo = jdbcTemplate.queryForObject(getSequenceNo, Integer.class);
								seqNo++;
								query += ", jsondata,jsondata1, dmodifieddate,nsitecode,nstatus,jsondata2";
								query += ") VALUES (";

								query += seqNo + ",";

								int i = 0;
								int productcode = 0;

								for (i = 0; i < values.length; i++) {

									if (i == 0) {
										final String x = values[1];
										final String tax = x.replaceAll("\"", "");
										query += tax;
									}
									if (i == 1) {
										final String x = values[2];
										final String tax = x.replaceAll("\"", "");
										
										// FIX: Validate product code is numeric
										if (!tax.matches("\\d+")) {
											continue;
										}
										
										// FIX: Use parameterized query for product lookup
										final String nproductcode = "Select nproductcode from invoiceproductmaster where nproductcode = ? and nstatus= ? and nsitecode= ?";
										final List<InvoiceProductMaster> ProductDetails = jdbcTemplate.query(
											nproductcode,
											new Object[]{Integer.parseInt(tax), Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), userInfo.getNmastersitecode()},
											new InvoiceProductMaster()
										);
										
										if (ProductDetails.isEmpty()) {
											continue;
										}
										
										productcode = ProductDetails.get(0).getNproductcode();
										query += productcode;
									}
									if (i == 2) {
										final String x = values[5];
										final String tax = x.replaceAll("\"", "");
										query += tax;
									}

									if (i <= 2 - 1) {
										query += ",";

									}
								}
								final String x = values[1];

								final JSONObject jsonObject = new JSONObject();
								final JSONObject jsonObjectobj = new JSONObject();
								
								// FIX: Use parameterized query for product details
								final String Product = "Select * from invoiceproductmaster where nproductcode = ? and nstatus= ? and nsitecode= ?";
								final List<InvoiceProductMaster> ProductDetails = jdbcTemplate.query(
									Product,
									new Object[]{productcode, Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), userInfo.getNmastersitecode()},
									new InvoiceProductMaster()
								);
								
								jsonObject.put("nlimsproduct", ProductDetails.get(0).getNlimsproduct());
								jsonObject.put("nproductcode", ProductDetails.get(0).getNproductcode());
								jsonObject.put("sproductname", ProductDetails.get(0).getSproductname());

								// FIX: Use parameterized query for tax details
								final String taxproduct = "Select * from taxproductdetails where nproductcode = ? and nstatus= ? and nsitecode= ?";
								final List<TaxProductDetails> taxproductdetails = jdbcTemplate.query(
									taxproduct,
									new Object[]{productcode, Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), userInfo.getNmastersitecode()},
									new TaxProductDetails()
								);

								final List<Integer> intValues = taxproductdetails.stream().map(TaxProductDetails::getNtaxcode)
										.collect(Collectors.toList());

								// FIX: Use parameterized query for tax lookup
								if (!intValues.isEmpty()) {
									final String tax = "Select * from invoicetaxtype where nstatus= ? and nsitecode= ? and ntaxcode in (" + 
										String.join(",", Collections.nCopies(intValues.size(), "?")) + ")";
									List<Object> taxParams = new ArrayList<>();
									taxParams.add(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
									taxParams.add(userInfo.getNmastersitecode());
									taxParams.addAll(intValues);
									
									final List<InvoiceTaxtype> TaxDetails = jdbcTemplate.query(
										tax, 
										taxParams.toArray(),
										new InvoiceTaxtype()
									);

									final List<Short> intValue = TaxDetails.stream().map(InvoiceTaxtype::getNcaltypecode)
											.collect(Collectors.toList());
									
									if (!intValue.isEmpty()) {
										final String Type = " SELECT *, coalesce(jsondata->'staxcaltype'->>'"
												+ userInfo.getSlanguagetypecode() + "',"
												+ " jsondata->'staxcaltype'->>'en-US') as staxcaltype "
												+ " FROM invoicetaxcaltype where nstatus= ? "
												+ " and nsitecode= ? "
												+ " and ncaltypecode in (" + String.join(",", Collections.nCopies(intValue.size(), "?")) + ")";
										List<Object> typeParams = new ArrayList<>();
										typeParams.add(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
										typeParams.add(userInfo.getNmastersitecode());
										typeParams.addAll(intValue);
										
										final List<InvoiceTaxCalType> Types = jdbcTemplate.query(
											Type,
											typeParams.toArray(),
											new InvoiceTaxCalType()
										);

										if (taxproductdetails.size() == 1) {
											jsonObjectobj.put("ntax", TaxDetails.get(0).getNtax());

											jsonObjectobj.put("stype", Types.get(0).getStaxcaltype());

											jsonObjectobj.put("ntaxtype", Types.get(0).getNcaltypecode());
											jsonObjectobj.put("staxname", TaxDetails.get(0).getStaxname());
										} else if (taxproductdetails.size() >= 2) {
											jsonObjectobj.put("nindirecttax", TaxDetails.get(0).getNtax());

											jsonObjectobj.put("sindirecttype", Types.get(0).getStaxcaltype());

											jsonObjectobj.put("nindirecttaxtype", Types.get(0).getNcaltypecode());
											jsonObjectobj.put("sindirecttaxname", TaxDetails.get(0).getStaxname());

											jsonObjectobj.put("ntax", TaxDetails.get(1).getNtax());

											jsonObjectobj.put("stype", Types.get(1).getStaxcaltype());

											jsonObjectobj.put("ntaxtype", Types.get(1).getNcaltypecode());
											jsonObjectobj.put("staxname", TaxDetails.get(1).getStaxname());

										}
									}
								}
								
								// FIXED: Properly escape JSON strings
								String escapedJsonObject = stringUtilityFunction.replaceQuote(jsonObject.toString());
								String escapedJsonObjectObj = stringUtilityFunction.replaceQuote(jsonObjectobj.toString());
								
								query += ",'" + escapedJsonObject + "','" + escapedJsonObjectObj + "'";
								query += ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
										+ userInfo.getNmastersitecode() + ",1";
								query += ")";

								jdbcTemplate.execute(query + "; update seqnoinvoice set nsequenceno = " + seqNo
										+ " where stablename='invoiceschemesproducts'");
							}
						}

					}
				}

			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILEALERT", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return getInvoiceSchemes(userInfo);

	}


	/**
	 * This method is used to get active InvoiceSchemes object based on the
	 * specified nschemecode.
	 * 
	 * @param nschemesCode [int] primary key of InvoiceSchemes object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         InvoiceSchemes object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getUpdateSchemeDataById(final Map<String, Object> inputMap) {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		final String str = "select inv.nschemecode,inv.sschemename,COALESCE(TO_CHAR(inv.dfromdate,'"
				+ userInfo.getSsitereportdate() + "'),'') as sfromdate," + "COALESCE(TO_CHAR(inv.dtodate,'"
				+ userInfo.getSsitereportdate() + "'),'') as stodate" + " from invoiceschemes inv where nschemecode="
				+ inputMap.get("nschemecode") + " and inv.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and inv.nsitecode="+userInfo.getNmastersitecode()+" and inv.nschemecode<>  "+Enumeration.TransactionStatus.NA.gettransactionstatus();
		final List<InvoiceSchemes> lstProduct = (List<InvoiceSchemes>) jdbcTemplate.query(str, new InvoiceSchemes());
		return new ResponseEntity<>(lstProduct, HttpStatus.OK);

	}

	/**
	 * This method is used to update entry in InvoiceSchemes table. Need to validate
	 * that the schemesmode object to be updated is active before updating details
	 * in database. Need to check for duplicate entry of schemes name for the
	 * specified site before saving into database. Need to check that there should
	 * be only one default InvoiceSchemes for a site
	 * 
	 * @param objInvoiceSchemes [InvoiceSchemes] object holding details to be
	 *                          updated in InvoiceSchemes table
	 * @param userInfo          [UserInfo] holding logged in user details based on
	 *                          which the list is to be fetched
	 * @return saved schemesmode object with status code 200 if saved successfully
	 *         else if the schemesmode already exists, response will be returned as
	 *         'Already Exists' with status code 409 else if the schemes to be
	 *         updated is not available, response will be returned as 'Already
	 *         Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateInvoiceSchemes(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final int seqnoinvoiceschemes = (int) inputMap.get("nschemecode");
		final List<InvoiceSchemes> Schemes = getInvoiceSchemesaudit(seqnoinvoiceschemes, userInfo);
		final JSONObject actionType = new JSONObject();
		final String str = "update invoiceschemes set sschemename='"
				+ stringUtilityFunction.replaceQuote(inputMap.get("sschemename").toString()) + "'," + "dfromdate='"
				+ inputMap.get("dfromdate") + "'," + "dtodate='" + inputMap.get("dtodate") + "'," + "dmodifieddate='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nschemecode="
				+ inputMap.get("nschemecode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		jdbcTemplate.execute(str);

		inputMap.put("nflag", 2);
		multilingualIDList.add("IDS_EDITSCHEMES");
		actionType.put("InvoiceSchemes", "IDS_EDITSCHEMES");
		auditUtilityFunction.fnInsertAuditAction(getInvoiceSchemesaudit(seqnoinvoiceschemes, userInfo), 2, Schemes,
				multilingualIDList, userInfo);

		return getInvoiceSchemes(userInfo);
	}

	/**
	 * This method is used to activeAndRetiredSchemes InvoiceSchemes object based on the
	 * specified nschemecode.
	 * 
	 * @param nschemesCode [int] primary key of InvoiceSchemes object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         InvoiceSchemes object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> activeAndRetiredSchemes(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final int seqnoinvoiceschemes = (int) inputMap.get("nschemecode");

		// Query to fetch invoice schemes
		final String strs = "SELECT * FROM invoiceschemesproducts WHERE nschemecode = " + seqnoinvoiceschemes+" and nsitecode="+userInfo.getNmastersitecode();
		final List<InvoiceSchemes> schemes = (List<InvoiceSchemes>) jdbcTemplate.query(strs, new InvoiceSchemes());

		// Check if no schemes are found
		if (schemes.size() == 0) {
			// Return alert if no schemes are found
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_NOSAMPLEDATATOAPPROVE", userInfo.getSlanguagefilename()),
					HttpStatus.NOT_FOUND);
		}

		final JSONObject actionType = new JSONObject();
		final String str;

		// Get transaction status from input
		final int ntransactionstatus = (int) inputMap.get("ntransactionstatus");

		// Determine action based on the transaction status
		if (ntransactionstatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			str = "UPDATE invoiceschemes SET ntransactionstatus = "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", dmodifieddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nschemecode = "
					+ inputMap.get("nschemecode") + " AND nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		} else {
			str = "UPDATE invoiceschemes SET ntransactionstatus = "
					+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ", dmodifieddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nschemecode = "
					+ inputMap.get("nschemecode") + " AND nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		}

		// Execute the update query
		jdbcTemplate.execute(str);

		// Add multilingual ID and log the action
		multilingualIDList.add("IDS_SCHEMESSTATUS");
		actionType.put("InvoiceSchemes", "IDS_SCHEMESSTATUS");
		auditUtilityFunction.fnInsertAuditAction(getInvoiceSchemesaudit(seqnoinvoiceschemes, userInfo), 1, null,
				multilingualIDList, userInfo);

		// Return success response
		return getInvoiceSchemes(userInfo);
	}

	/**
	 * This method id used to delete an entry in InvoiceSchemes table Need to check
	 * the record is already deleted or not Need to check whether the record is used
	 * in other tables such as 'invoicequotationheader','invoiceheader'
	 * 
	 * @param objInvoiceSchemes [InvoiceSchemes] an Object holds the record to be
	 *                          deleted
	 * @param userInfo          [UserInfo] holding logged in user details based on
	 *                          which the list is to be fetched
	 * @return a response entity with list of available InvoiceSchemes objects
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteInvoiceSchemes(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final int nschemecode = (int) inputMap.get("nschemecode");
		final String str = "update invoiceschemes set nstatus=" + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
				+ ",dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nschemecode="
				+ inputMap.get("nschemecode");
		jdbcTemplate.execute(str);

		final String schemesprodstr = "update invoiceschemesproducts set nstatus= "
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nschemecode="
				+ inputMap.get("nschemecode");
		jdbcTemplate.execute(schemesprodstr);

		multilingualIDList.add("IDS_DELETESCHEMES");
		auditUtilityFunction.fnInsertAuditAction(getSchemesAlreadyDeletedOrNot(nschemecode, "scheme", userInfo), 1,
				null, multilingualIDList, userInfo);

		return getInvoiceSchemes(userInfo);
	}

	/**
	 * This method is used to retrieve active InvoiceSchemes object based on the
	 * specified nschemecode.
	 * 
	 * @param nschemesCode [int] primary key of InvoiceSchemes object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         InvoiceSchemes object
	 * @throws Exception that are thrown from this DAO layer
	 */
	public List<InvoiceSchemes> getSchemesAlreadyDeletedOrNot(final int schemeid, final String screenName, final UserInfo userInfo) {
		final String strQuery;
		if (screenName.isEmpty()) {
			strQuery = "select nschemecode from invoiceschemes where nschemecode =" + schemeid + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userInfo.getNmastersitecode();
		} else {
			strQuery = "select inv.nschemecode,inv.ntransactionstatus,inv.sschemename,"
					+ " COALESCE(TO_CHAR(inv.dfromdate,'" + userInfo.getSsitereportdate() + "'),'') as sfromdate,"
					+ " COALESCE(TO_CHAR(inv.dtodate,'" + userInfo.getSsitereportdate()
					+ "'),'') as stodate from invoiceschemes inv " + " where nschemecode=" + schemeid + " and nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " and inv.nsitecode="+userInfo.getNmastersitecode();
		}
		final List<InvoiceSchemes> scheme = (List<InvoiceSchemes>) jdbcTemplate.query(strQuery, new InvoiceSchemes());
		return scheme;
	}

	/**
	 * This method is used to getInvoiceSchemesData InvoiceSchemes object based on the
	 * specified nschemecode.
	 * 
	 * @param nschemesCode [int] primary key of InvoiceSchemes object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         InvoiceSchemes object
	 * @throws Exception that are thrown from this DAO layer
	 */
	private List<InvoiceSchemes> getInvoiceSchemesData(final String sschemename,final UserInfo userInfo) throws Exception {
		final String strQuery = "select sschemename from invoiceschemes where sschemename = '"
				+ sschemename.replace("'", "''") + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode="+userInfo.getNmastersitecode();

		return (List<InvoiceSchemes>) jdbcTemplate.query(strQuery, new InvoiceSchemes());
	}
}
