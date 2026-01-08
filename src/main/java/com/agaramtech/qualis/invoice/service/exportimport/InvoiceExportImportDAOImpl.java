package com.agaramtech.qualis.invoice.service.exportimport;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import com.opencsv.CSVReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.multipart.MultipartFile;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.contactmaster.model.Client;
import com.agaramtech.qualis.contactmaster.model.Patient;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.CustomerFile;
import com.agaramtech.qualis.invoice.model.InvoiceCustomerMaster;
import com.agaramtech.qualis.invoice.model.InvoiceCustomerType;
import com.agaramtech.qualis.invoice.model.InvoiceExportImport;
import com.agaramtech.qualis.invoice.model.InvoicePatient;
import com.agaramtech.qualis.invoice.model.InvoiceProductMaster;
import com.agaramtech.qualis.invoice.model.InvoiceProductType;
import com.agaramtech.qualis.invoice.model.InvoiceTaxtype;
import com.agaramtech.qualis.invoice.model.ProductTest;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.agaramtech.qualis.testgroup.model.TreeTemplateManipulation;
import com.agaramtech.qualis.testmanagement.model.TestMaster;
import com.agaramtech.qualis.testmanagement.model.TestPackage;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opencsv.CSVWriter;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "InvoiceExportImport" table
 * by implementing methods from its interface.
 * 
 */
@AllArgsConstructor
@Repository
public class InvoiceExportImportDAOImpl implements InvoiceExportImportDAO {
	/**
	 * This class is used to perform CRUD Operation on "InvoiceExportImport" table
	 * by implementing methods from its interface.
	 */
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

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
	@SuppressWarnings("unused")
	@Override
	public ResponseEntity<Object> getInvoiceExportImport(UserInfo userinfo) throws Exception {

		String query = "SELECT * FROM sampletype WHERE jsondata->'sampletypename'->>'en-US' = 'Product'";
		List<SampleType> sample = (List<SampleType>) jdbcTemplate.query(query, new SampleType());

		if (sample.get(0).getNstatus() != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			List<Product> productdatastatus = (List<Product>) getproductdatastatus(userinfo);
		}
		getcustomerdatastatus();
		getpatientdatastatus();
		getpackagedatastatus();
		getTestMasterDataStatus();
		

		String str = "";
		if (userinfo.getNuserrole() == Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			str = "select * from invoiceexportimport where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			jdbcTemplate.execute(str);
		} else {
			String Query = "select uf.nneedrights from userrolefieldcontrol uf,fieldmaster fm where uf.nfieldcode=fm.nfieldcode "
					+ " and fm.sfieldname='invoicepatientfield' and fm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			int nneedrights = jdbcTemplate.queryForObject(Query, Integer.class);
			if (nneedrights == 4) {
				String updateQueryString = "Update invoiceexportimport set nstatus="
						+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + " where ncode=3";
				jdbcTemplate.execute(updateQueryString);
			}
			if (nneedrights != 4) {
				String updateQueryString = "Update invoiceexportimport set nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where ncode=3";
				jdbcTemplate.execute(updateQueryString);
			}
			str = "select * from invoiceexportimport where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			jdbcTemplate.execute(str);
		}

		return new ResponseEntity<Object>(jdbcTemplate.query(str, new InvoiceExportImport()), HttpStatus.OK);
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


	@SuppressWarnings("unchecked")

	@Override

	public ResponseEntity<Object> exportproductmaster(Map<String, Object> objmap, UserInfo userInfo) throws Exception {
 
		Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
 
		List<InvoiceProductMaster> lstdata = (List<InvoiceProductMaster>) getexportdatas();
 
		Object object = lstdata.get(0);

		final String sFileName = "InvoiceProductMaster";

		final String OutputFileName = sFileName + ".csv";

		final String homePath = ftpUtilityFunction.getFileAbsolutePath();

		String target = System.getenv(homePath);

		target = target.trim();

		final String TargetPath = target + Enumeration.FTP.INVOICE_EXPORT.getFTP() + OutputFileName;

		File file = new File(TargetPath);

		FileWriter outputfile = new FileWriter(file);
 
		CSVWriter writer = new CSVWriter(outputfile);

		String[] header = { "Product Type", "LimsCode", "Product Name", "Description", "Invoice Description",

				"Add Text1", "Add Text2", "TaxAvailable", "Cost" };
 
		writer.writeNext(header);
 
		for (InvoiceProductMaster objcol2 : lstdata) {
 
			final String strQuery = "select i.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"

					+ userInfo.getSlanguagetypecode() + "',"

					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "

					+ " from invoiceproducttype i,transactionstatus ts " + " where i.nstatus = "

					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "

					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.ntranscode = i.nactive"

					+ " and i.ntypecode in  (" + objcol2.getNtypecode() + ")";

			final List<InvoiceProductType> obj = (List<InvoiceProductType>) jdbcTemplate.query(strQuery,

					new InvoiceProductType());

			objcol2.setStypename(obj.get(0).getStypename());

			String TypeName = objcol2.getStypename();

			String LimsCode = objcol2.getSlimscode();

			String ProductName = objcol2.getSproductname();

			String Description = objcol2.getSdescription();

			String InvoiceDescription = objcol2.getSinvoicedescription();

			String AddText1 = objcol2.getSaddtext1();

			String AddText2 = objcol2.getSaddtext2();

			int TaxAvailable = objcol2.getNtaxavailable();

			Double Cost = objcol2.getNcost();

			Double Tax = objcol2.getNtax();

			String TaxName = objcol2.getStaxname();

			String Versionno = objcol2.getSversionno();

			String str1 = Double.toString(Tax);

			String str2 = Double.toString(Cost);

			String TaxA = String.valueOf(TaxAvailable);

			short Active = objcol2.getNactive();

			String str = String.valueOf(Active);

			String[] data1 = { TypeName, LimsCode, ProductName, Description, InvoiceDescription, AddText1, AddText2,

					TaxA, str2 };

			writer.writeNext(data1);

		}

		writer.flush();

		writer.close();

		String sDownloadPathName = getdownloadpathname();

		if (sDownloadPathName != null) {

			outputMap.put("ExportExcelPath", sDownloadPathName + "InvoiceProductMaster" + ".csv");

			outputMap.put("ExportExcel", "Success");

		} else {

			outputMap.put("rtn", Enumeration.ReturnStatus.FAILED.getreturnstatus().trim().toString());

		}

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

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
	public List<String> findColumnName(Object object) throws Exception {
		List<String> lsts = new ArrayList<String>();
		Field[] fields = object.getClass().getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			lsts.add(field.getName());
		}
		return lsts;
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
	public List<InvoiceProductMaster> getexportdatas() throws Exception {

		String queryString = "select a.*" + "  from invoiceproductmaster a " + " where  a.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<InvoiceProductMaster> lsttaxDetails = (List<InvoiceProductMaster>) jdbcTemplate.query(queryString,
				new InvoiceProductMaster());
		return lsttaxDetails;
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
	public String getdownloadpathname() throws Exception {
		String settingsname = "select * from settings where nsettingcode=94 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		final Settings site = (Settings) jdbcUtilityFunction.queryForObject(settingsname, Settings.class, jdbcTemplate);
		return site.getSsettingvalue();
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
	public List<InvoiceTaxtype> getexporttax(UserInfo userInfo) throws Exception {

		String queryString = "select it.*, coalesce(iv.jsondata->'sversionno'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + "iv.jsondata->'sversionno'->>'en-US') as sversionno,"
				+ "coalesce(itc.jsondata->'staxcaltype'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "itc.jsondata->'staxcaltype'->>'en-US') as staxcaltype"
				+ " from invoicetaxtype it,invoiceversionno iv,invoicetaxcaltype itc " + " where it.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and it.ncaltypecode=itc.ncaltypecode and it.nversionnocode=iv.nversionnocode";
		List<InvoiceTaxtype> lsttaxDetails = (List<InvoiceTaxtype>) jdbcTemplate.query(queryString,
				new InvoiceTaxtype());
		return lsttaxDetails;
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
	@Override
	public ResponseEntity<Object> getProductMaster(UserInfo userinfo) throws Exception {
		// TODO Auto-generated method stub
		String str = "select ipm.nproductcode,ipm.ntypecode,ipm.slimscode,ipm.sproductname,ipm.sdescription,ipm.sinvoicedescription,"
				+ " ipm.saddtext1,ipm.saddtext2,ipm.ncost,ipt.stypename,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,ipm.ntaxavailable  from invoiceproductmaster ipm,invoiceproducttype ipt,transactionstatus ts where "
				+ " ipt.ntypecode=ipm.ntypecode and ipm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ipt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.ntranscode=ipm.ntaxavailable"
				+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by ipm.nproductcode";

		return new ResponseEntity<Object>(jdbcTemplate.query(str, new InvoiceProductMaster()), HttpStatus.OK);
	}

	/**
	* This method is used to import ProductMaster data from a CSV file for the specified site.
	*
	* @param objMultipart [MultipartFile] the uploaded CSV file containing ProductMaster data
	* @param userInfo     [UserInfo] the user information of the logged-in user
	* @return ResponseEntity indicating success or failure of the import operation
	* @throws Exception if any error occurs during processing
	*/
	 
//	@SuppressWarnings("unchecked")
//	@Override
//	public ResponseEntity<Object> importProductMaster(MultipartHttpServletRequest request, UserInfo userInfo)
//			throws Exception {
//		ObjectMapper objectMapper = new ObjectMapper();
//		Map<String, Object> reg = objectMapper.readValue(request.getParameter("Map"), Map.class);
//		
//	
//		Map<String, Object> selectedRecord = (Map<String, Object>) reg.get("selectedRecord");
//		List<Map<String, String>> filenames = (List<Map<String, String>>) selectedRecord.get("sfilename");
//
//		
//		String path = filenames.get(0).get("path");
//		//String filename = Paths.get(path).getFileName().toString();
//		    if (path != null && !path.isEmpty()) {
//		      //  String filename = sfilenameList.get(0).get("path");
//		 if(path.matches("InvoiceProductMaster \\(\\d+\\)\\.csv") || path.matches("InvoiceProductMaster\\.csv")) {
//		MultipartFile objmultipart = request.getFile("InvoiceImportFile");
//		if (objmultipart == null) {
//
//			String Std = "Select .CSV file to import";
//			return new ResponseEntity<>(Std, HttpStatus.CONFLICT);
//		}
//		InputStream objinputstream = objmultipart.getInputStream();
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		byte[] buffer = new byte[1024];
//		int len;
//		while ((len = objinputstream.read(buffer)) > -1) {
//			baos.write(buffer, 0, len);
//		}
//		baos.flush();
//		InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
//		String line;
//		BufferedReader br = new BufferedReader(new InputStreamReader(objmultipart.getInputStream(), "UTF-8"));
//		// BufferedReader br = new BufferedReader(new FileReader("C:\\Program
//		// Files\\Apache Software Foundation\\Tomcat
//		// 9.0\\webapps\\ROOT\\SharedFolder\\Export\\InvoiceProductMaster.csv"));
//		String[] headers = null;
//		String[] values;
//
//		String newHeader0 = "ntypecode";
//		String newHeader1 = "slimscode";
//		String newHeader2 = "sproductname";
//		String newHeader3 = "sdescription";
//		String newHeader4 = "sinvoicedescription";
//		String newHeader5 = "saddtext1";
//		String newHeader6 = "saddtext2";
//		String newHeader7 = "ntaxavailable";
//		String newHeader8 = "ncost";
//
//		String st2 = "select * from invoiceproductmaster where nstatus=1";
//		final List<InvoiceProductMaster> obj1 = (List<InvoiceProductMaster>) jdbcTemplate.query(st2,
//				new InvoiceProductMaster());
//		List<String> objname = obj1.stream().map(InvoiceProductMaster::getSproductname).collect(Collectors.toList());
//
//		while ((line = br.readLine()) != null) {
//			values = line.split(",");
//
//			if (headers == null) {
//				headers = values;
//
//			} else {
//				String customernme = values[2];
//				String nmes = customernme.replaceAll("\"", "");
//
//				if (!objname.contains(nmes)) {
//
//					String query = "INSERT INTO invoiceproductmaster (";
//					query += "nproductcode";
//					query += ",";
//					for (int i = 0; i < headers.length; i++) {
//						if (i <= 8) {
//							if (i == 0) {
//								headers[0] = newHeader0;
//							}
//							if (i == 1) {
//								headers[1] = newHeader1;
//							}
//							if (i == 2) {
//								headers[2] = newHeader2;
//							}
//							if (i == 3) {
//								headers[3] = newHeader3;
//							}
//							if (i == 4) {
//								headers[4] = newHeader4;
//							}
//							if (i == 5) {
//								headers[5] = newHeader5;
//							}
//							if (i == 6) {
//								headers[6] = newHeader6;
//							}
//							if (i == 7) {
//								headers[7] = newHeader7;
//							}
//							if (i == 8) {
//								headers[8] = newHeader8;
//							}
//							query += headers[i];
//							if (i <= 8 - 1) {
//
//								query += ",";
//
//							}
//						}
//					}
//					final String getSequenceNo = "select nsequenceno from seqnoinvoice where stablename ='invoiceproductmaster'";
//					int seqNo =jdbcTemplate.queryForObject(getSequenceNo, Integer.class);
//					seqNo++;
//					query += ",nusercode , dmodifieddate,nsitecode,nstatus";
//					query += ") VALUES (";
//
//					query += seqNo;
//					query += ",";
//
//					for (int i = 0; i < values.length; i++) {
//
//						if (i == 0) {
//							String y = values[0];
//							String stringWithoutQuotes = y.replaceAll("\"", "");
//							final String strQuery = "select i.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
//									+ userInfo.getSlanguagetypecode() + "',"
//									+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
//									+ " from invoiceproducttype i,transactionstatus ts " + " where i.nstatus = "
//									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
//									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//									+ " and ts.ntranscode = i.nactive" + " and i.stypename in ('" + stringWithoutQuotes
//									+ "') ";
//							final List<InvoiceProductType> obj = (List<InvoiceProductType>) jdbcTemplate.query(strQuery,
//									new InvoiceProductType());
//							int q = obj.get(0).getNtypecode();
//							query += q;
//							// query += ",";
//						}
//
//						// query += "'" + values[i] + "'";
//
//						if (i == 1) {
//							String x = values[1];
//							String tax = x.replaceAll("\"", "");
//							query += "'" + tax + "'";
//						}
//						if (i == 2) {
//							String x = values[2];
//							String tax = x.replaceAll("\"", "");
//							query += "'" + tax + "'";
//						}
//						if (i == 3) {
//							String x = values[3];
//							String tax = x.replaceAll("\"", "");
//							query += "'" + tax + "'";
//						}
//						if (i == 4) {
//							String x = values[4];
//							String sdescription = x.replaceAll("\"", "");
//							query += "'" + sdescription + "'";
//						}
//						if (i == 5) {
//							String x = values[5];
//							String tax = x.replaceAll("\"", "");
//							query += "'" + tax + "'";
//						}
//						if (i == 6) {
//							String x = values[6];
//							String tax = x.replaceAll("\"", "");
//							query += "'" + tax + "'";
//						}
//						if (i == 7) {
//							String x = values[7];
//							String tax = x.replaceAll("\"", "");
//							query += tax;
//
//						}
//						if (i == 8) {
//							String z = values[8];
//							String cost = z.replaceAll("\"", "");
//							query += cost;
//
//						}
//						if (i <= 8 - 1) {
//							query += ",";
//
//						}
//
//					}
//					query += ",-1,'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ",1";
//					query += ")";
//					jdbcTemplate.execute("UPDATE seqnoinvoice SET nsequenceno = " + seqNo
//						+ " WHERE stablename = 'invoiceproductmaster'");
//				
//				} else {
//				    String x = values[3];
//				    String tax = x.replaceAll("\"", "");
//				    String des = values[4];
//				    String sdescription = des.replaceAll("\"", "");
//				    String add1 = values[5];
//				    String addtext1 = add1.replaceAll("\"", "");
//				    String add2 = values[6];
//				    String addtext2 = add2.replaceAll("\"", "");
//				    String z = values[8];
//				    String cost = z.replaceAll("\"", "");
//
//				    String updateSql = "UPDATE invoiceproductmaster SET sdescription = ?, sinvoicedescription = ?, saddtext1 = ?, saddtext2 = ?, ncost = ? WHERE sproductname = ?";
//				    
//				    // Robust number parsing that handles scientific notation, decimals, and regular integers
//				    Number costValue;
//				    try {
//				        if (cost.contains("E") || cost.contains("e") || cost.contains(".")) {
//				            costValue = Double.parseDouble(cost);
//				        } else {
//				            costValue = Long.parseLong(cost); // Use Long to handle larger numbers
//				        }
//				    } catch (NumberFormatException e) {
//				        // Fallback to 0 if parsing fails
//				        costValue = 0;
//				    }
//				    
//				    jdbcTemplate.update(updateSql, tax, sdescription, addtext1, addtext2, costValue, nmes);
//				}
//
//			}
//		}
//		 }
//		 else {
//       	  return new ResponseEntity<>(
//	                    commonFunction.getMultilingualMessage("IDS_PRODUCTMASTERFILEALERT", userInfo.getSlanguagefilename()),
//	                    HttpStatus.EXPECTATION_FAILED);
//       }
//		    }
//		return getProductMaster(userInfo);
//	}
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> importProductMaster(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> reg = objectMapper.readValue(request.getParameter("Map"), Map.class);
		
		Map<String, Object> selectedRecord = (Map<String, Object>) reg.get("selectedRecord");
		
		// Add null checks for selectedRecord and filenames
		if (selectedRecord == null) {
			return new ResponseEntity<>("Selected record data is missing", HttpStatus.BAD_REQUEST);
		}
		
		List<Map<String, String>> filenames = (List<Map<String, String>>) selectedRecord.get("sfilename");
		
		// Check if filenames is null or empty
		if (filenames == null || filenames.isEmpty()) {
			return new ResponseEntity<>("File information is missing", HttpStatus.BAD_REQUEST);
		}
		
		// Check if the first filename entry has path
		Map<String, String> firstFile = filenames.get(0);
		if (firstFile == null || firstFile.get("path") == null) {
			return new ResponseEntity<>("File path is missing", HttpStatus.BAD_REQUEST);
		}
		
		String path = firstFile.get("path");
		
		// Check if path is valid
		if (path == null || path.trim().isEmpty()) {
			return new ResponseEntity<>("Invalid file path", HttpStatus.BAD_REQUEST);
		}
		
		// Validate file name pattern
		if (path.matches("InvoiceProductMaster \\(\\d+\\)\\.csv") || path.matches("InvoiceProductMaster\\.csv")) {
			MultipartFile objmultipart = request.getFile("InvoiceImportFile");
			if (objmultipart == null) {
				String Std = "Select .CSV file to import";
				return new ResponseEntity<>(Std, HttpStatus.CONFLICT);
			}
			
			// Check if the uploaded file is empty
			if (objmultipart.isEmpty()) {
				return new ResponseEntity<>("Uploaded file is empty", HttpStatus.BAD_REQUEST);
			}
			
			InputStream objinputstream = objmultipart.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len;
			while ((len = objinputstream.read(buffer)) > -1) {
				baos.write(buffer, 0, len);
			}
			baos.flush();
			InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(objmultipart.getInputStream(), "UTF-8"));
			
			String[] headers = null;
			String[] values;

			String newHeader0 = "ntypecode";
			String newHeader1 = "slimscode";
			String newHeader2 = "sproductname";
			String newHeader3 = "sdescription";
			String newHeader4 = "sinvoicedescription";
			String newHeader5 = "saddtext1";
			String newHeader6 = "saddtext2";
			String newHeader7 = "ntaxavailable";
			String newHeader8 = "ncost";

			String st2 = "select * from invoiceproductmaster where nstatus=1";
			final List<InvoiceProductMaster> obj1 = (List<InvoiceProductMaster>) jdbcTemplate.query(st2,
					new InvoiceProductMaster());
			List<String> objname = obj1.stream().map(InvoiceProductMaster::getSproductname).collect(Collectors.toList());

			int lineNumber = 0;
			while ((line = br.readLine()) != null) {
				lineNumber++;
				values = line.split(",");

				// Skip empty lines
				if (line.trim().isEmpty()) {
					continue;
				}

				if (headers == null) {
					headers = values;
					continue; // Skip header row for data processing
				}
				
				// Check if values array has enough elements
				if (values.length < 9) {
					// Log or handle insufficient data in CSV row
					continue; // Skip this row or handle appropriately
				}

				String customernme = values[2];
				String nmes = customernme.replaceAll("\"", "");

				if (!objname.contains(nmes)) {
					String query = "INSERT INTO invoiceproductmaster (";
					query += "nproductcode";
					query += ",";
					for (int i = 0; i < headers.length; i++) {
						if (i <= 8) {
							if (i == 0) {
								headers[0] = newHeader0;
							}
							if (i == 1) {
								headers[1] = newHeader1;
							}
							if (i == 2) {
								headers[2] = newHeader2;
							}
							if (i == 3) {
								headers[3] = newHeader3;
							}
							if (i == 4) {
								headers[4] = newHeader4;
							}
							if (i == 5) {
								headers[5] = newHeader5;
							}
							if (i == 6) {
								headers[6] = newHeader6;
							}
							if (i == 7) {
								headers[7] = newHeader7;
							}
							if (i == 8) {
								headers[8] = newHeader8;
							}
							query += headers[i];
							if (i <= 8 - 1) {
								query += ",";
							}
						}
					}
					final String getSequenceNo = "select nsequenceno from seqnoinvoice where stablename ='invoiceproductmaster'";
					int seqNo = jdbcTemplate.queryForObject(getSequenceNo, Integer.class);
					seqNo++;
					query += ",nusercode , dmodifieddate,nsitecode,nstatus";
					query += ") VALUES (";

					query += seqNo;
					query += ",";

					for (int i = 0; i < values.length; i++) {
						if (i > 8) break; // Only process first 9 columns

						if (i == 0) {
							String y = values[0];
							String stringWithoutQuotes = y.replaceAll("\"", "");
							final String strQuery = "select i.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
									+ userInfo.getSlanguagetypecode() + "',"
									+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
									+ " from invoiceproducttype i,transactionstatus ts " + " where i.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ts.ntranscode = i.nactive" + " and i.stypename in ('" + stringWithoutQuotes
									+ "') ";
							final List<InvoiceProductType> obj = (List<InvoiceProductType>) jdbcTemplate.query(strQuery,
									new InvoiceProductType());
							
							// Check if product type exists
							if (obj == null || obj.isEmpty()) {
								// Handle case where product type doesn't exist
								continue; // Skip this row or set default value
							}
							
							int q = obj.get(0).getNtypecode();
							query += q;
						}

						if (i == 1) {
							String x = values[1];
							String tax = x.replaceAll("\"", "");
							query += "'" + tax + "'";
						}
						if (i == 2) {
							String x = values[2];
							String tax = x.replaceAll("\"", "");
							query += "'" + tax + "'";
						}
						if (i == 3) {
							String x = values[3];
							String tax = x.replaceAll("\"", "");
							query += "'" + tax + "'";
						}
						if (i == 4) {
							String x = values[4];
							String sdescription = x.replaceAll("\"", "");
							query += "'" + sdescription + "'";
						}
						if (i == 5) {
							String x = values[5];
							String tax = x.replaceAll("\"", "");
							query += "'" + tax + "'";
						}
						if (i == 6) {
							String x = values[6];
							String tax = x.replaceAll("\"", "");
							query += "'" + tax + "'";
						}
						if (i == 7) {
							String x = values[7];
							String tax = x.replaceAll("\"", "");
							query += tax;
						}
						if (i == 8) {
							String z = values[8];
							String cost = z.replaceAll("\"", "");
							query += cost;
						}
						if (i <= 8 - 1) {
							query += ",";
						}
					}
					query += ",-1,'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ",1";
					query += ")";
					jdbcTemplate.execute("UPDATE seqnoinvoice SET nsequenceno = " + seqNo
						+ " WHERE stablename = 'invoiceproductmaster'");
				} else {
					// Check if values array has enough elements for update
					if (values.length >= 9) {
						String x = values[3];
						String tax = x.replaceAll("\"", "");
						String des = values[4];
						String sdescription = des.replaceAll("\"", "");
						String add1 = values[5];
						String addtext1 = add1.replaceAll("\"", "");
						String add2 = values[6];
						String addtext2 = add2.replaceAll("\"", "");
						String z = values[8];
						String cost = z.replaceAll("\"", "");

						String updateSql = "UPDATE invoiceproductmaster SET sdescription = ?, sinvoicedescription = ?, saddtext1 = ?, saddtext2 = ?, ncost = ? WHERE sproductname = ?";
						
						// Robust number parsing that handles scientific notation, decimals, and regular integers
						Number costValue;
						try {
							if (cost.contains("E") || cost.contains("e") || cost.contains(".")) {
								costValue = Double.parseDouble(cost);
							} else {
								costValue = Long.parseLong(cost); // Use Long to handle larger numbers
							}
						} catch (NumberFormatException e) {
							// Fallback to 0 if parsing fails
							costValue = 0;
						}
						
						jdbcTemplate.update(updateSql, tax, sdescription, addtext1, addtext2, costValue, nmes);
					}
				}
			}
			br.close();
		} else {
			return new ResponseEntity<>(
				commonFunction.getMultilingualMessage("IDS_PRODUCTMASTERFILEALERT", userInfo.getSlanguagefilename()),
				HttpStatus.EXPECTATION_FAILED);
		}
		
		return getProductMaster(userInfo);
	}

	private String sanitizeSqlString(String input) {
		if (input == null)
			return "";
		return input.replace("'", "''") // ✅ Escape single quotes
				.replaceAll("[\\r\\n]+", " ") // ✅ Flatten newlines
				.replaceAll("[^\\x20-\\x7E]", "") // Optional: Remove non-printable characters
				.trim();
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
	@Override
	public ResponseEntity<Object> exportcustomermaster(Map<String, Object> objmap, UserInfo userInfo) throws Exception {

		Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		List<InvoiceCustomerMaster> lstdata = (List<InvoiceCustomerMaster>) getexportcustomerdata();

		final String sFileName = "InvoiceCustomerMaster";

		final String OutputFileName = sFileName + ".csv";

		final String homePath = ftpUtilityFunction.getFileAbsolutePath();

		String target = System.getenv(homePath);

		target = target.trim();

		final String TargetPath = target + Enumeration.FTP.INVOICE_EXPORT.getFTP() + OutputFileName;

		File file = new File(TargetPath);

		FileWriter outputfile = new FileWriter(file);

		CSVWriter writer = new CSVWriter(outputfile);
		String[] header = { "Customer Code", "Customer Type name", "Customer Name", "Customer poc", " Customer Address",
				" Customer Shipping Address", "Email ID", "Phone", "Account Details ", "Other Details", "Cust Tin",
				"Cust gst", "Customer reference1", "Customer reference2", "Project reference1", "Project reference2",
				"Discount Available" };

		writer.writeNext(header);

		for (InvoiceCustomerMaster objcol2 : lstdata) {

			String CustomerCode = objcol2.getScustomerreference();
			String CustomerTypename = objcol2.getScustomertypename();
			String CustomerName = objcol2.getScustomername();
			String Customerpoc = objcol2.getScustomerpoc();
			String CustomerAddress = objcol2.getScustomeraddress();
			String CustomerShippingAddress = objcol2.getScustomershipingaddress();
			String EmailID = objcol2.getSemailid();
			String Phone = objcol2.getSphone();
			String AccountDetails = objcol2.getSaccountdetails();
			String OtherDetails = objcol2.getSotherdetails();
			String CustTin = objcol2.getScusttin();
			String Custgst = objcol2.getScustgst();
			String Customerreference1 = objcol2.getScustomerreference1();
			String Customerreference2 = objcol2.getScustomerreference2();
			int Discountavailable = objcol2.getNdiscountavailable();
			String strdiscount = Double.toString(Discountavailable);
			String Projectreference1 = objcol2.getSprojectreference1();
			String Projectreference2 = objcol2.getSprojectreference2();

			String[] datafields = { CustomerCode, CustomerTypename, CustomerName, Customerpoc, CustomerAddress,
					CustomerShippingAddress, EmailID, Phone, AccountDetails, OtherDetails, CustTin, Custgst,
					Customerreference1, Customerreference2, Projectreference1, Projectreference2, strdiscount, };

			writer.writeNext(datafields);

		}
		writer.flush();
		writer.close();

		String sDownloadPathName = getdownloadpathname();
		if (sDownloadPathName != null) {
			outputMap.put("ExportExcelPath", sDownloadPathName + "InvoiceCustomerMaster" + ".csv");
			outputMap.put("ExportExcel", "Success");

		}

		else {
			outputMap.put("rtn", Enumeration.ReturnStatus.FAILED.getreturnstatus().trim().toString());
		}
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

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
	public List<InvoiceCustomerMaster> getexportcustomerdata() throws Exception {

		String queryString = "select a.*" + "  from invoicecustomermaster a " + " where a.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<InvoiceCustomerMaster> lsttaxDetails = (List<InvoiceCustomerMaster>) jdbcTemplate.query(queryString,
				new InvoiceCustomerMaster());
		return lsttaxDetails;
	}

	/**
	* This method is used to import CustomerMaster data from a CSV file for the specified site.
	*
	* @param objMultipart [MultipartFile] the uploaded CSV file containing CustomerMaster data
	* @param userInfo     [UserInfo] the user information of the logged-in user
	* @return ResponseEntity indicating success or failure of the import operation
	* @throws Exception if any error occurs during processing
	*/
	@SuppressWarnings({ "unchecked", "resource" })
	@Override
	public ResponseEntity<Object> importCustomerMaster(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> reg = objectMapper.readValue(request.getParameter("Map"), Map.class);
		Map<String, Object> selectedRecord = (Map<String, Object>) reg.get("selectedRecord");
		List<Map<String, String>> filenames = (List<Map<String, String>>) selectedRecord.get("sfilename");
		String path = filenames.get(0).get("path");

		if (path != null && !path.isEmpty()) {
			
			if (path.matches("InvoiceCustomerMaster \\(\\d+\\)\\.csv") || path.matches("InvoiceCustomerMaster\\.csv")) {
				MultipartFile objmultipart = request.getFile("InvoiceImportFile");
				if (objmultipart == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTCSVIMPORT", userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}

				// Get existing customers for validation
				String strquery = "SELECT * FROM invoicecustomermaster WHERE nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final List<InvoiceCustomerMaster> objcustomer = (List<InvoiceCustomerMaster>) jdbcTemplate.query(strquery,
						new InvoiceCustomerMaster());
				List<String> existingPhones = objcustomer.stream().map(InvoiceCustomerMaster::getSphone).collect(Collectors.toList());

				CSVReader csvReader = new CSVReader(
						new InputStreamReader(objmultipart.getInputStream(), StandardCharsets.UTF_8));
				
				String[] headers = null;
				String[] values;

				while ((values = csvReader.readNext()) != null) {
					if (headers == null) {
						headers = values;
						continue;
					}

					// Truncate all values to 50 characters before processing
					for (int i = 0; i < values.length; i++) {
						if (values[i] != null && values[i].length() > 50) {
							values[i] = values[i].substring(0, 50);
						}
					}
					
					String phone = sanitizeSqlString(values[7]);
					
					if (!existingPhones.contains(phone)) {
						// Insert new customer
						int seqNo = jdbcTemplate.queryForObject(
								"SELECT nsequenceno FROM seqnoinvoice WHERE stablename ='invoicecustomermaster'",
								Integer.class) + 1;

						// Determine type code
						String customerType = sanitizeSqlString(values[1]);
						int typeCode = "DIRECT".equalsIgnoreCase(customerType) ? 1 : 0;

						StringBuilder query = new StringBuilder();
						query.append("INSERT INTO invoicecustomermaster (");
						query.append("ncustomercode, scustomerreference, scustomertypename, scustomername, ");
						query.append("scustomerpoc, scustomeraddress, scustomershipingaddress, semailid, ");
						query.append("sphone, saccountdetails, sotherdetails, scusttin, scustgst, ");
						query.append("scustomerreference1, scustomerreference2, sprojectreference1, ");
						query.append("sprojectreference2, ndiscountavailable, nusercode, dmodifieddate, ");
						query.append("nsitecode, nstatus, ntypecode) VALUES (");
						
						query.append(seqNo).append(", ");
						query.append("'").append(sanitizeSqlString(values[0])).append("', ");
						query.append("'").append(customerType).append("', ");
						query.append("'").append(sanitizeSqlString(values[2])).append("', ");
						query.append("'").append(sanitizeSqlString(values[3])).append("', ");
						query.append("'").append(sanitizeSqlString(values[4])).append("', ");
						query.append("'").append(sanitizeSqlString(values[5])).append("', ");
						query.append("'").append(sanitizeSqlString(values[6])).append("', ");
						query.append("'").append(phone).append("', ");
						query.append("'").append(sanitizeSqlString(values[8])).append("', ");
						query.append("'").append(sanitizeSqlString(values[9])).append("', ");
						query.append("'").append(sanitizeSqlString(values[10])).append("', ");
						query.append("'").append(sanitizeSqlString(values[11])).append("', ");
						query.append("'").append(sanitizeSqlString(values[12])).append("', ");
						query.append("'").append(sanitizeSqlString(values[13])).append("', ");
						query.append("'").append(sanitizeSqlString(values[14])).append("', ");
						query.append("'").append(sanitizeSqlString(values[15])).append("', ");
						
						// Handle numeric field
						String discount = values[16].replaceAll("\"", "").trim();
						if (!discount.matches("^[0-9.]+$")) discount = "0";
						query.append(discount).append(", ");
						
						query.append("-1, ");
						query.append("'").append(dateUtilityFunction.getCurrentDateTime(userInfo)).append("', ");
						query.append(userInfo.getNmastersitecode()).append(", ");
						query.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(", ");
						query.append(typeCode).append(")");

						jdbcTemplate.execute(query.toString());
						jdbcTemplate.execute("UPDATE seqnoinvoice SET nsequenceno = " + seqNo + 
								" WHERE stablename = 'invoicecustomermaster'");

					} else {
						StringBuilder updateQuery = new StringBuilder();
						updateQuery.append("UPDATE invoicecustomermaster SET ");
						updateQuery.append("scustomerreference = '").append(sanitizeSqlString(values[0])).append("', ");
						updateQuery.append("scustomerpoc = '").append(sanitizeSqlString(values[3])).append("', ");
						updateQuery.append("scustomeraddress = '").append(sanitizeSqlString(values[4])).append("', ");
						updateQuery.append("scustomershipingaddress = '").append(sanitizeSqlString(values[5])).append("', ");
						updateQuery.append("semailid = '").append(sanitizeSqlString(values[6])).append("', ");
						updateQuery.append("saccountdetails = '").append(sanitizeSqlString(values[8])).append("', ");
						updateQuery.append("sotherdetails = '").append(sanitizeSqlString(values[9])).append("', ");
						updateQuery.append("scusttin = '").append(sanitizeSqlString(values[10])).append("', ");
						updateQuery.append("scustgst = '").append(sanitizeSqlString(values[11])).append("', ");
						updateQuery.append("scustomerreference1 = '").append(sanitizeSqlString(values[12])).append("', ");
						updateQuery.append("scustomerreference2 = '").append(sanitizeSqlString(values[13])).append("', ");
						updateQuery.append("sprojectreference1 = '").append(sanitizeSqlString(values[14])).append("', ");
						updateQuery.append("sprojectreference2 = '").append(sanitizeSqlString(values[15])).append("' ");
						updateQuery.append("WHERE sphone = '").append(phone).append("'");

						jdbcTemplate.execute(updateQuery.toString());
					}
				}
				csvReader.close();
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CUSTOMERMASTERFILEALERT",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
		return getInvoiceCustomerMaster(userInfo);
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
	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getInvoiceCustomerMaster(UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final List<InvoiceCustomerType> CustomerTypeList = (List<InvoiceCustomerType>) getCustomerType(userInfo)
				.getBody();
		outputMap.put("customerTypeName", CustomerTypeList);

		final List<InvoiceCustomerMaster> CusmtomerMasterList = (List<InvoiceCustomerMaster>) getCustomerMastertoexport(
				userInfo).getBody();
		outputMap.put("cusmtomermasterlist", CusmtomerMasterList);
		if (CusmtomerMasterList.isEmpty()) {
			outputMap.put("selectedCustomer", null);

		} else {
			int ncustomercode = CusmtomerMasterList.get(0).getNcustomercode();
			outputMap.put("selectedCustomer", CusmtomerMasterList.get(0));
			outputMap.putAll((Map<String, Object>) getCustomerFile(ncustomercode, userInfo).getBody());
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
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
	@Override
	public ResponseEntity<Object> getCustomerType(UserInfo userinfo) throws Exception {
		final String strQuery = "select ntypecode, scustomertypename from invoicecustomertype " + " where nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new InvoiceCustomerType()), HttpStatus.OK);
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
	public ResponseEntity<Object> getCustomerMastertoexport(UserInfo userinfo) throws Exception {

		String strQuery = "select icm.*,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode()
				+ "') as stransdisplaystatus , ict.scustomertypename from invoicecustomermaster icm,invoicecustomertype ict,transactionstatus ts where ts.ntranscode=icm.ndiscountavailable and "
				+ "icm.ntypecode=ict.ntypecode and icm.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ict.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ncustomercode desc";
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new InvoiceCustomerMaster()), HttpStatus.OK);
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
	public ResponseEntity<Object> getCustomerFile(int ncustomercode, UserInfo objUserInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		String query = "select tf.noffsetdcreateddate,tf.ncustomerfilecode,(select  count(ncustomerfilecode) from customerfile where ncustomerfilecode>0 and ncustomercode = "
				+ ncustomercode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ") as ncount,tf.sdescription,"
				+ " tf.ncustomerfilecode as nprimarycode,tf.sfilename,tf.ncustomercode,tf.ssystemfilename,"
				+ " tf.nattachmenttypecode,coalesce(at.jsondata->'sattachmenttype'->>'"
				+ objUserInfo.getSlanguagetypecode() + "',"
				+ "	at.jsondata->'sattachmenttype'->>'en-US') as sattachmenttype, case when tf.nlinkcode=-1 then '-' else lm.jsondata->>'slinkname'"
				+ " end slinkname, tf.nfilesize," + " case when tf.nattachmenttypecode= "
				+ Enumeration.AttachmentType.LINK.gettype() + " then '-' else" + " COALESCE(TO_CHAR(tf.dcreateddate,'"
				+ objUserInfo.getSpgdatetimeformat() + "'),'-') end  as screateddate, "
				+ " tf.nlinkcode, case when tf.nlinkcode = -1 then tf.nfilesize::varchar(1000) else '-' end sfilesize"
				+ " from customerfile tf,attachmenttype at, linkmaster lm  "
				+ " where at.nattachmenttypecode = tf.nattachmenttypecode and at.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and lm.nlinkcode = tf.nlinkcode and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.ncustomercode=" + ncustomercode
				+ " order by tf.ncustomerfilecode;";
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		List<CustomerFile> customerFile = jdbcTemplate.query(query, new CustomerFile());

		outputMap.put("customerFile", dateUtilityFunction.getSiteLocalTimeFromUTC(customerFile,
				Arrays.asList("screateddate"), null, objUserInfo, false, null, false));

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
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

	@Override
	public ResponseEntity<Object> mergecustomermaster(InvoiceExportImport objInvoiceExportImport, UserInfo userInfo)
	        throws Exception {

	    final List<String> multilingualIDList = new ArrayList<>();
	    final List<Object> savedMergeCustomerMasterList = new ArrayList<>();
	    List<Integer> identicalclientdata = (List<Integer>) getclientdata();
	    List<Client> clientname = new ArrayList<>();
	    List<Integer> s = identicalclientdata;
	    String result = s.toString().replace("[", "").replace("]", "");
	    String strInsert = "";
	    String clientInsert = "";

	    if (identicalclientdata.size() != 0) {

	        String str = "SELECT c.sclientname, c.sclientid, cc.sphoneno, cc.semail,cs.saddress1,cs.saddress2,cs.saddress3,cc.scontactname,cc.smobileno, "
	                + " CONCAT(COALESCE(cs.saddress1, ''), ' ', COALESCE(cs.saddress2, '')) AS address, "
	                + " cs.saddress3 AS gst " + "FROM client c "
	                + " JOIN clientcontactinfo cc ON c.nclientcode = cc.nclientcode AND cc.ndefaultstatus = 3 and"
	                + " c.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
	                + " JOIN clientsiteaddress cs ON c.nclientcode = cs.nclientcode AND cs.ndefaultstatus = 3  "
	                + " WHERE c.nclientcode NOT IN (" + result + ", -1) ";

	        clientname = (List<Client>) jdbcTemplate.query(str, new Client());

	        if (clientname.size() != 0) {
	            StringJoiner joinerSample = new StringJoiner(",");
	            String sequencenoquery = "select nsequenceno from seqnoinvoice where stablename ='invoicecustomermaster'";
	            int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
	            int sequenceno = clientname.size() + nsequenceno;

	            for (Client objname : clientname) {
	                nsequenceno++;

	                String customername = objname.getSclientname();
	                String clientid = objname.getSclientid();
	                String address1 = objname.getSaddress1();
	                String address2 = objname.getSaddress2();
	                String gst = objname.getSaddress3();
	                String email = objname.getSemail();
	                String phnno = objname.getSphoneno();
	                String contactname = objname.getScontactname();
	                String mobileno = objname.getSmobileno();

	                // ✅ Escape apostrophes for required fields
	                if (customername != null) {
	                    customername = customername.replace("'", "''");
	                }
	                if (clientid != null) {
	                    clientid = clientid.replace("'", "''");
	                }
	                if (address1 != null) {
	                    address1 = address1.replace("'", "''");
	                }
	                if (address2 != null) {
	                    address2 = address2.replace("'", "''");
	                }

	                String mergedAddress;
	                if (address1 != null && !address1.trim().isEmpty() && address2 != null
	                        && !address2.trim().isEmpty()) {
	                    mergedAddress = address1 + " " + address2;
	                } else if (address1 != null && !address1.trim().isEmpty()) {
	                    mergedAddress = address1;
	                } else if (address2 != null && !address2.trim().isEmpty()) {
	                    mergedAddress = address2;
	                } else {
	                    mergedAddress = "NA";
	                }

	                mergedAddress = mergedAddress.trim();
	                gst = (gst != null && !gst.trim().isEmpty()) ? gst : "NA";
	                email = (email != null && !email.trim().isEmpty()) ? email : "NA";
	                phnno = (phnno != null && !phnno.trim().isEmpty()) ? phnno : " ";

	                joinerSample.add(mergedAddress);
	                joinerSample.add(String.valueOf(clientid));
	                joinerSample.add(String.valueOf(customername));
	                joinerSample.add(String.valueOf(phnno));
	                joinerSample.add(String.valueOf(email));
	                joinerSample.add(String.valueOf(gst));
	                joinerSample.add(String.valueOf(contactname));
	                joinerSample.add(String.valueOf(mobileno));

	                strInsert = strInsert + "(" + nsequenceno + ",'" + clientid + "','LIMS',1,'" + customername
	                        + "','NA','" + mergedAddress + "','NA','" + email + "','" + phnno + "','NA','NA','NA','"
	                        + gst + "','" + mobileno + "','" + contactname + "',3,'NA','NA','"
	                        + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
	                        + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
	            }
	            if (!strInsert.isEmpty()) {
	                strInsert = strInsert.substring(0, strInsert.length() - 1);
	            }

	            clientInsert = "INSERT INTO invoicecustomermaster(ncustomercode, scustomerreference, scustomertypename, ntypecode, scustomername, "
	                    + "scustomerpoc, scustomeraddress, scustomershipingaddress, semailid, sphone, saccountdetails, sotherdetails, scusttin, scustgst, "
	                    + "scustomerreference1, scustomerreference2, ndiscountavailable, sprojectreference1, sprojectreference2, dmodifieddate, nsitecode, nstatus) VALUES "
	                    + strInsert + ";";

	            String updatequery = "update seqnoinvoice set nsequenceno = " + sequenceno
	                    + " where stablename='invoicecustomermaster'";
	            jdbcTemplate.execute(updatequery);

	            jdbcTemplate.execute(clientInsert);
	        }

	    } else {

	        List<Client> clientdetails = (List<Client>) getclientdetails();
	        StringJoiner joinerSample = new StringJoiner(",");
	        String sequencenoquery = "select nsequenceno from seqnoinvoice where stablename ='invoicecustomermaster'";
	        int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
	        int sequenceno = clientdetails.size() + nsequenceno;
	        for (Client objname : clientdetails) {
	            nsequenceno++;

	            String customername = objname.getSclientname();
	            String clientid = objname.getSclientid();
	            String address1 = objname.getSaddress1();
	            String address2 = objname.getSaddress2();
	            String gst = objname.getSaddress3();
	            String email = objname.getSemail();
	            String phnno = objname.getSphoneno();
	            String contactname = objname.getScontactname();
	            String mobileno = objname.getSmobileno();

	            // ✅ Escape apostrophes for required fields
	            if (customername != null) {
	                customername = customername.replace("'", "''");
	            }
	            if (clientid != null) {
	                clientid = clientid.replace("'", "''");
	            }
	            if (address1 != null) {
	                address1 = address1.replace("'", "''");
	            }
	            if (address2 != null) {
	                address2 = address2.replace("'", "''");
	            }

	            String mergedAddress;
	            if (address1 != null && !address1.trim().isEmpty() && address2 != null && !address2.trim().isEmpty()) {
	                mergedAddress = address1 + " " + address2;
	            } else if (address1 != null && !address1.trim().isEmpty()) {
	                mergedAddress = address1;
	            } else if (address2 != null && !address2.trim().isEmpty()) {
	                mergedAddress = address2;
	            } else {
	                mergedAddress = "NA";
	            }

	            mergedAddress = mergedAddress.trim();
	            gst = (gst != null && !gst.trim().isEmpty()) ? gst : "NA";
	            email = (email != null && !email.trim().isEmpty()) ? email : "NA";
	            phnno = (phnno != null && !phnno.trim().isEmpty()) ? phnno : " ";

	            joinerSample.add(mergedAddress);
	            joinerSample.add(String.valueOf(clientid));
	            joinerSample.add(String.valueOf(customername));
	            joinerSample.add(String.valueOf(phnno));
	            joinerSample.add(String.valueOf(email));
	            joinerSample.add(String.valueOf(gst));
	            joinerSample.add(String.valueOf(contactname));
	            joinerSample.add(String.valueOf(mobileno));

	            strInsert = strInsert + "(" + nsequenceno + ",'" + clientid + "','LIMS',1,'" + customername + "','NA','"
	                    + mergedAddress + "','NA','" + email + "','" + phnno + "','NA','NA','NA','" + gst + "','"
	                    + mobileno + "','" + contactname + "',3,'NA','NA','"
	                    + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
	                    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

	        }

	        if (strInsert != null && !strInsert.trim().isEmpty()) {
	            strInsert = strInsert.trim();
	            if (strInsert.endsWith(",")) {
	                strInsert = strInsert.substring(0, strInsert.length() - 1);
	            }

	            clientInsert = "INSERT INTO invoicecustomermaster (ncustomercode, scustomerreference, scustomertypename, ntypecode, scustomername, scustomerpoc, scustomeraddress, scustomershipingaddress, semailid, sphone, saccountdetails, sotherdetails, scusttin, scustgst, scustomerreference1, scustomerreference2, ndiscountavailable, sprojectreference1, sprojectreference2, dmodifieddate, nsitecode, nstatus) VALUES "
	                    + strInsert + ";";
	        } else {
	            System.out.println("Nothing to merge — strInsert is empty.");
	        }

	        String updatequery = "update seqnoinvoice set nsequenceno =" + sequenceno
	                + " where stablename='invoicecustomermaster'";
	        jdbcTemplate.execute(updatequery);
	        jdbcTemplate.execute(clientInsert);

	    }

	    savedMergeCustomerMasterList.add(objInvoiceExportImport);
	    multilingualIDList.add("IDS_MERGECUSTOMERMASTER");
	    auditUtilityFunction.fnInsertAuditAction(savedMergeCustomerMasterList, 1, null, multilingualIDList, userInfo);
	    return getInvoiceExportImport(userInfo);

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

	public List<Integer> getclientdata() {

		String queryString = "select DISTINCT ct.nclientcode ,ct.sclientid , cm.scustomername  from invoicecustomermaster cm,client ct where "
				+ "cm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ "ct.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ "cm.scustomertypename = 'LIMS'"
				+ " and cm.scustomername=ct.sclientname and ct.nclientcode not in (-1)";

		List<Client> identicaldata = (List<Client>) jdbcTemplate.query(queryString, new Client());
		List<Integer> identicaldata1 = identicaldata.stream().map(Client::getNclientcode).collect(Collectors.toList());
		return identicaldata1;

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
	public List<Client> getclientdetails() {

		String Str = "SELECT c.nclientcode, c.sclientname, c.sclientid, cc.sphoneno, cc.semail, "
				+ "cs.saddress1, cs.saddress2, cs.saddress3, cc.scontactname, cc.smobileno, "
				+ "CONCAT(COALESCE(cs.saddress1, ''), ' ', COALESCE(cs.saddress2, '')) AS address, "
				+ "cs.saddress3 AS gst " + "FROM client c "
				+ "JOIN clientcontactinfo cc ON c.nclientcode = cc.nclientcode AND cc.ndefaultstatus = 3 AND c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN clientsiteaddress cs ON c.nclientcode = cs.nclientcode AND cs.ndefaultstatus = 3 "
				+ "WHERE c.nclientcode NOT IN (-1)";

		List<Client> clientdetails = (List<Client>) jdbcTemplate.query(Str, new Client());
		return clientdetails;

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
	@Override
	public ResponseEntity<Object> mergeproductmaster(InvoiceExportImport objInvoiceExportImport, UserInfo userInfo)
	        throws Exception {

	    final List<String> multilingualIDList = new ArrayList<>();
	    final List<Object> savedMergeProductMasterList = new ArrayList<>();

	    // Merge the product master
	    mergeproduct(objInvoiceExportImport, userInfo);

	    // Get all spec codes
	    List<Integer> speccode = (List<Integer>) gettestdetails();
	    String speccodes = speccode.toString().replace("[", "").replace("]", "");

	    String strInsert = "";
	    String productInsert = "";

	    // Fetch test group specifications based on speccodes
	    String str;
	    if (!speccode.isEmpty()) {
	        str = "SELECT DISTINCT " +
	                "tgs.nallottedspeccode, " +
	                "tgs.sspecname, " +
	                "tgp.nspecsampletypecode, " +
	                "tt.ntestcode, " +
	                "tt.stestsynonym, " +
	                "tt.ncost, " +
	                "m.smethodname " +
	                "FROM testgroupspecification tgs " +
	                "JOIN testgroupspecsampletype tgp ON tgs.nallottedspeccode = tgp.nallottedspeccode " +
	                "JOIN testgrouptest tt ON tt.nspecsampletypecode = tgp.nspecsampletypecode " +
	                "LEFT JOIN testmethod tm ON tt.ntestcode = tm.ntestcode " +
	                "LEFT JOIN method m ON tt.nmethodcode = m.nmethodcode " +
	                "WHERE tgs.nallottedspeccode NOT IN (" + speccodes + ") " +
	                "AND tgs.napprovalstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " " +
	                "AND tgs.ntransactionstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
	                "AND tt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
	                "AND tgs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
	                "AND tgp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
	                "AND m.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
	    } else {
	        str = "SELECT DISTINCT " +
	                "tgp.nallottedspeccode, " +
	                "tgp.nspecsampletypecode, " +
	                "tt.ntestcode, " +
	                "tt.stestsynonym, " +
	                "tt.ncost, " +
	                "tgs.sspecname, " +
	                "m.smethodname " +
	                "FROM testgroupspecification tgs " +
	                "JOIN testgroupspecsampletype tgp ON tgs.nallottedspeccode = tgp.nallottedspeccode " +
	                "JOIN testgrouptest tt ON tt.nspecsampletypecode = tgp.nspecsampletypecode " +
	                "LEFT JOIN testmethod tm ON tt.ntestcode = tm.ntestcode " +
	                "LEFT JOIN method m ON tt.nmethodcode = m.nmethodcode " +
	                "WHERE tgs.napprovalstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " " +
	                "AND tgs.ntransactionstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
	                "AND tt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
	                "AND tgs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
	                "AND tgp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
	                "AND m.nstatus = 1;";
	    }

	    List<InvoiceExportImport> testGroups = (List<InvoiceExportImport>) jdbcTemplate.query(str, new InvoiceExportImport());

	    if (!testGroups.isEmpty()) {
	        String sequencenoquery = "select nsequenceno from seqnoinvoice where stablename ='invoiceproductmaster'";
	        int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
	        int sequenceno = nsequenceno + testGroups.size();

	        for (InvoiceExportImport obj : testGroups) {
	            nsequenceno++;
	            String testName = obj.getStestsynonym() != null ? obj.getStestsynonym().replace("'", "''") : "";
	            String specName = obj.getSspecname() != null ? obj.getSspecname().replace("'", "''") : "";
	            String methodName = obj.getSmethodname() != null ? obj.getSmethodname().replace("'", "''") : "";
	            int testCode = obj.getNtestcode();
	            int allottedSpecCode = obj.getNallottedspeccode();
	            double testCost = obj.getNcost();

	         // Create valid JSON string without extra backslashes
	            String jsonData = "{\"nallottedspeccode\":" + allottedSpecCode + ",\"ntestcode\":" + testCode + "}";

	            strInsert += "(" + nsequenceno + ",1,'" + testName + " (" + specName + ")','NA','NA','NA','" + methodName +
	                    "',3," + testCost + ",'" + jsonData + "',-1,'" + 
	                    dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() +
	                    "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

	        }

	        productInsert = "insert into invoiceproductmaster(nproductcode,ntypecode,sproductname,sdescription,sinvoicedescription,saddtext1,saddtext2," +
	                "ntaxavailable,ncost,jsondata,nusercode,dmodifieddate,nsitecode,nstatus) values " +
	                strInsert.substring(0, strInsert.length() - 1) + ";";

	        jdbcTemplate.execute(productInsert);

	        String updatequery = "update seqnoinvoice set nsequenceno =" + sequenceno +
	                " where stablename='invoiceproductmaster'";
	        jdbcTemplate.execute(updatequery);
	    }

	    savedMergeProductMasterList.add(objInvoiceExportImport);
	    multilingualIDList.add("IDS_MERGEPRODUCTMASTER");
	    auditUtilityFunction.fnInsertAuditAction(savedMergeProductMasterList, 1, null, multilingualIDList, userInfo);

	    return getInvoiceExportImport(userInfo);
	}

	@Override
	public ResponseEntity<Object> mergepackagemaster(InvoiceExportImport objInvoiceExportImport, UserInfo userInfo)
	        throws Exception {
	    
	    final List<String> multilingualIDList = new ArrayList<>();
	    final List<Object> savedMergePackageMasterList = new ArrayList<>();
	    
	    // Get package codes that don't exist in invoiceproductmaster
	    List<Integer> packageData = (List<Integer>) getPackageData();
	    List<Integer> packagecodes = packageData;
	    String result = packagecodes.toString().replace("[", "").replace("]", "");
	    
	    String strInsert = "";
	    List<Map<String, Object>> packageList = new ArrayList<>();

	    if (!packagecodes.isEmpty()) {
	        String str = "SELECT DISTINCT " +
	                "tp.ntestpackagecode, " +
	                "tp.stestpackagename, " +
	                "tp.sdescription, " +
	                "tp.ntestpackageprice, " +
	                "tp.sportalrefcode, " +
	                "tp.sopenmrsrefcode, " +
	                "tp.spreventtbrefcode " +
	                "FROM testpackage tp " +
	                "WHERE tp.ntestpackagecode IN (" + result + ") " +
	                "AND tp.nstatus = 1";  // ACTIVE status

	        packageList = jdbcTemplate.queryForList(str);

	        String sequencenoquery = "select nsequenceno from seqnoinvoice where stablename ='invoiceproductmaster'";
	        int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
	        int sequenceno = packagecodes.size() + nsequenceno;

	        for (Map<String, Object> row : packageList) {
	            nsequenceno++;
	            
	            Integer packageCode = (Integer) row.get("ntestpackagecode");
	            String packageName = row.get("stestpackagename") != null ? 
	                row.get("stestpackagename").toString().replace("'", "''") : "";
	            String description = row.get("sdescription") != null ? 
	                row.get("sdescription").toString().replace("'", "''") : "NA";
	            Double packagePrice = row.get("ntestpackageprice") != null ? 
	                ((Number) row.get("ntestpackageprice")).doubleValue() : 0.0;
	            String portalRefCode = row.get("sportalrefcode") != null ? 
	                row.get("sportalrefcode").toString().replace("'", "''") : "NA";
	            String openmrsRefCode = row.get("sopenmrsrefcode") != null ? 
	                row.get("sopenmrsrefcode").toString().replace("'", "''") : "NA";
	            String preventTbRefCode = row.get("spreventtbrefcode") != null ? 
	                row.get("spreventtbrefcode").toString().replace("'", "''") : "NA";

	            // Create JSON data
	            String jsonData = "{" +
	                "\"ntestpackagecode\":" + packageCode + "," +
	                "\"stestpackagename\":\"" + packageName + "\"," +
	                "\"sportalrefcode\":\"" + portalRefCode + "\"," +
	                "\"sopenmrsrefcode\":\"" + openmrsRefCode + "\"," +
	                "\"spreventtbrefcode\":\"" + preventTbRefCode + "\"" +
	                "}";

	            strInsert += "(" + nsequenceno + ",1,'" + packageName + "','" + description + 
	                    "','Package','" + portalRefCode + "','" + openmrsRefCode + "'," +
	                    "3," + packagePrice + ",'" + jsonData + "'," + packageCode + "," + 
	                    userInfo.getNusercode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + 
	                    userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
	        }

	        if (!strInsert.isEmpty()) {
	            String packageInsert = "insert into invoiceproductmaster(nproductcode,ntypecode,sproductname,sdescription,sinvoicedescription,saddtext1,saddtext2,"
	                    + "ntaxavailable,ncost,jsondata,nlimsproduct,nusercode,dmodifieddate,nsitecode,nstatus)values"
	                    + strInsert.substring(0, strInsert.length() - 1) + ";";
	            jdbcTemplate.execute(packageInsert);

	            String updatequery = "update seqnoinvoice set nsequenceno =" + sequenceno
	                    + " where stablename='invoiceproductmaster'";
	            jdbcTemplate.execute(updatequery);
	        }
	    }

	    savedMergePackageMasterList.add(objInvoiceExportImport);
	    multilingualIDList.add("IDS_MERGEPACKAGEMASTER");
	    auditUtilityFunction.fnInsertAuditAction(savedMergePackageMasterList, 1, null, multilingualIDList, userInfo);
	    
	    return getInvoiceExportImport(userInfo);
	}

	public List<Integer> getPackageData() {
	    String queryString = "SELECT DISTINCT tp.ntestpackagecode, tp.stestpackagename, tp.ntestpackageprice " +
	            "FROM testpackage tp " +
	            "WHERE tp.nstatus = 1 " +  // ACTIVE status
	            "AND NOT EXISTS (SELECT * FROM invoiceproductmaster pm " +
	            "WHERE pm.sproductname = tp.stestpackagename " +
	            "AND pm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

	    List<Map<String, Object>> packageData = jdbcTemplate.queryForList(queryString);
	    
	    List<Integer> packageCodes = packageData.stream()
	            .map(row -> (Integer) row.get("ntestpackagecode"))
	            .filter(Objects::nonNull)
	            .collect(Collectors.toList());
	    
	    return packageCodes;
	}

	// Alternative helper method using simpler query (if you prefer)
	private List<Integer> getpackagedetails() {
	    String query = "SELECT DISTINCT " +
	                   "CAST(jsondata->>'ntestpackagecode' AS INTEGER) as packagecode " +
	                   "FROM invoiceproductmaster " +
	                   "WHERE ntypecode = 2 AND nstatus = " + 
	                   Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + 
	                   " AND jsondata IS NOT NULL " +
	                   "AND jsondata->>'ntestpackagecode' IS NOT NULL";
	    
	    List<Integer> packageCodes = new ArrayList<>();
	    try {
	        List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
	        for (Map<String, Object> row : results) {
	            if (row.get("packagecode") != null) {
	                packageCodes.add((Integer) row.get("packagecode"));
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return packageCodes;
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
	public List<Integer> gettestdetails() {

		String Query = "select jsondata->>'nallottedspeccode' as ntypecode from invoiceproductmaster where jsondata IS NOT NULL and "
				+ "nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		List<InvoiceProductMaster> testdetails = (List<InvoiceProductMaster>) jdbcTemplate.query(Query,
				new InvoiceProductMaster());
		List<Integer> speccode = testdetails.stream().map(InvoiceProductMaster::getNtypecode)
				.collect(Collectors.toList());
		return speccode;
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

	@Override
	public ResponseEntity<Object> mergeproduct(InvoiceExportImport objInvoiceExportImport, UserInfo userInfo)
			throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedMergeProductMasterList = new ArrayList<>();
		// Map<String, Object> Data = new HashMap<>();
		// final ObjectMapper objmapper = new ObjectMapper();
		List<Product> productname = new ArrayList<>();
		List<InvoiceExportImport> specimenname = new ArrayList<>();
		String strInsert = "";
		String StInsert = "";
		String TestInsert = "";
		String productInsert = "";
		List<InvoiceExportImport> producttestdetails = new ArrayList<>();
		List<TestGroupSpecification> sampletype = new ArrayList<>();
		String str = "SELECT * FROM sampletype WHERE jsondata->'sampletypename'->>'en-US' = 'Product'";
		List<SampleType> sample = (List<SampleType>) jdbcTemplate.query(str, new SampleType());
		if (sample.get(0).getNstatus() != -1) {
			String strQuery = "select * from productview";
			productname = jdbcTemplate.query(strQuery, new Product());
			String productcodes1 = " select * from invoiceproductmaster where nstatus=  "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			List<InvoiceProductMaster> productlist = (List<InvoiceProductMaster>) jdbcTemplate.query(productcodes1,
					new InvoiceProductMaster());
			if (productlist.size() != 0) {
				if (productname.size() != 0) {

					String sequencenoquery = "select nsequenceno from seqnoinvoice where stablename ='invoiceproductmaster'";
					int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
					int sequenceno = productname.size() + nsequenceno;

					for (Product objname : productname) {
						nsequenceno++;
						String product = objname.getSproductname();
						// FIX: Properly escape single quotes for SQL - replace each single quote with two single quotes
						String escapedProduct = product.replace("'", "''");
						int productcode = objname.getNproductcode();
						String Str = "select * from treetemplatemanipulation ttm,testgroupspecification tg where tg.napprovalstatus="
								+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and"
								+ " ttm.ntemplatemanipulationcode = tg.ntemplatemanipulationcode and ttm.nproductcode = "
								+ productcode + "";
						sampletype = (List<TestGroupSpecification>) jdbcTemplate.query(Str,
								new TestGroupSpecification());
						String methodname = "";
						double Totalcost = 0;
						if (sampletype.size() != 0) {

							String Query = "SELECT DISTINCT p.nproductcatcode, tp.sspecname, tm.ntemplatemanipulationcode, " +
						               "tgs.nallottedspeccode, tgs.nspecsampletypecode, tt.ntestgrouptestcode, tt.ntestcode, " +
						               "m.smethodname, tms.stestname, tt.ncost " +
						               "FROM product p, treetemplatemanipulation tm, testgroupspecification tp, " +
						               "testgroupspecsampletype tgs, testgrouptest tt, method m, testmaster tms " +
						               "WHERE tm.nproductcode = " + productcode + " " +
						               "AND tm.nproductcatcode = p.nproductcatcode " +
						               "AND tgs.nspecsampletypecode = tt.nspecsampletypecode " +
						               "AND tm.ntemplatemanipulationcode = tp.ntemplatemanipulationcode " +
						               "AND tp.nallottedspeccode = tgs.nallottedspeccode " +
						               "AND tt.ntestcode = tms.ntestcode " +
						               "AND tt.nmethodcode = m.nmethodcode " +
						               "AND m.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
						               "AND tm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
						               "AND p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
						               "AND tt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
						               "AND tgs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
						               "AND tp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " +
						               "AND tp.napprovalstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus();

						producttestdetails = (List<InvoiceExportImport>) jdbcTemplate.query(Query, new InvoiceExportImport());
							if (producttestdetails.size() >= 1) {
								String sequence = "select nsequenceno from seqnoinvoice where stablename ='producttest'";
								int nsequence = jdbcTemplate.queryForObject(sequence, Integer.class);
								int sequencenos = producttestdetails.size() + nsequence;

								for (InvoiceExportImport obj : producttestdetails) {
									nsequence++;
									String testname = obj.getStestname();
									// FIX: Properly escape single quotes for SQL
									String escapedTestname = testname.replace("'", "''");
									int testcode = obj.getNtestcode();
									double cost = obj.getNcost();
									String specname = obj.getSspecname();
									// FIX: Properly escape single quotes for SQL
									String escapedSpecname = specname.replace("'", "''");
									methodname = obj.getSmethodname();
									// FIX: Properly escape single quotes for SQL
									String escapedMethodname = methodname.replace("'", "''");
									Totalcost += cost;

									StInsert = StInsert + "(" + nsequence + "," + nsequenceno + "," + productcode + ",'"
											+ escapedProduct + "','" + escapedSpecname + "'," + testcode + ",'" + escapedTestname + "',"
											+ cost + "," + userInfo.getNmastersitecode() + ",'"
											+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
								}

								TestInsert = "insert into producttest(nproducttestcode,ninvproductcode,nlimsproductcode,sproductname,sspecname,ntestcode,sproducttestname,ntestcost,"
										+ " nmastersitecode,dmodifieddate,nstatus) values"
										+ StInsert.substring(0, StInsert.length() - 1) + ";";
								jdbcTemplate.execute(TestInsert);

								String updatequery = "update seqnoinvoice set nsequenceno =" + sequencenos
										+ " where stablename='producttest'";
								jdbcTemplate.execute(updatequery);
								TestInsert = "";
								StInsert = "";

							}
						}
						
						// FIX: Properly escape the fixed string values
						String escapedDescription = "NA".replace("'", "''");
						String escapedAddText1 = "NA".replace("'", "''");
						String escapedAddText2 = "NA".replace("'", "''");
						String escapedMethodField = "NA".replace("'", "''");
						
						if (productname.size() != 0) {
							strInsert = strInsert + "(" + nsequenceno + ",1,'" + escapedProduct + "','" + escapedDescription + "','" + escapedAddText1 + "','" + escapedAddText2 + "','" + escapedDescription + "',"+ Enumeration.TransactionStatus.YES.gettransactionstatus()+","
									+ Totalcost + ",'LIMS'," + productcode + ",-1,'"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
									+ userInfo.getNmastersitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
						} else {
							// FIX: Properly escape methodname for the else case
							String escapedMethodForElse = methodname != null ? methodname.replace("'", "''") : "NA";
							strInsert = strInsert + "(" + nsequenceno + ",1,'" + escapedProduct + "','" + escapedDescription + "','" + escapedAddText1 + "','" + escapedAddText2 + "','"
									+ escapedMethodForElse + "',"+ Enumeration.TransactionStatus.YES.gettransactionstatus()+"," + Totalcost + ",'LIMS'," + productcode + ",-1,'"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
									+ userInfo.getNmastersitecode() + ","
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
						}

					}
					

					productInsert = "insert into invoiceproductmaster(nproductcode,ntypecode,sproductname,sdescription,sinvoicedescription,saddtext1,saddtext2,"
							+ "ntaxavailable,ncost,slimsdata,nlimsproduct,nusercode,dmodifieddate,nsitecode,nstatus)values"
							+ strInsert.substring(0, strInsert.length() - 1) + ";";
					jdbcTemplate.execute(productInsert);

					String updatequery = "update seqnoinvoice set nsequenceno =" + sequenceno
							+ " where stablename='invoiceproductmaster'";
					jdbcTemplate.execute(updatequery);
				}
				if (producttestdetails.size() < 1 || sampletype.size() < 1) {

					List<TestGroupTest> testgroupspeccode = new ArrayList<>();
					String TestInsert1 = "";
					List<Integer> speccode = (List<Integer>) gettestdetails();
					List<Integer> c = speccode;
					String speccodes = c.toString().replace("[", "").replace("]", "");
					if (speccode.size() != 0 || speccode.size() == 0) {
						if (speccode.size() == 0) {
							int specode = 0;
							String query = " select tgs.nallottedspeccode,tgp.nspecsampletypecode,tt.ntestcode,tt.stestsynonym,tt.ncost from testgroupspecification tgs,testgroupspecsampletype tgp,testgrouptest tt where "
									+ " tgs.nallottedspeccode = tgp.nallottedspeccode and "
									+ " tt.nspecsampletypecode = tgp.nspecsampletypecode and  tgs.nallottedspeccode not in ("
									+ specode + ") and " + " tgs.napprovalstatus="
									+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and "
									+ " tgs.ntransactionstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
									+ " tt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and " + " tgs.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
									+ " tgp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ "";
							testgroupspeccode = (List<TestGroupTest>) jdbcTemplate.query(query, new TestGroupTest());
						}
						if (speccode.size() != 0) {
							double totalSum = 0;
							String query = "SELECT DISTINCT " + "tgs.nallottedspeccode, " + "tgs.sspecname, "
									+ "tgp.nspecsampletypecode, " + "tt.ntestcode, " + "tt.stestsynonym, "
									+ "tt.ncost, " + "m.smethodname " + "FROM testgroupspecification tgs "
									+ "JOIN testgroupspecsampletype tgp ON tgs.nallottedspeccode = tgp.nallottedspeccode "
									+ "JOIN testgrouptest tt ON tt.nspecsampletypecode = tgp.nspecsampletypecode "
									+ "LEFT JOIN testmethod tm ON tt.ntestcode = tm.ntestcode "
									+ "LEFT JOIN method m ON tt.nmethodcode = m.nmethodcode "
									+ "WHERE tgs.nallottedspeccode NOT IN (" + speccodes + ") "
									+ "AND tgs.napprovalstatus = "
									+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
									+ "AND tgs.ntransactionstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									+ "AND tt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " " + "AND tgs.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
									+ "AND tgp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " " + "AND m.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							testgroupspeccode = (List<TestGroupTest>) jdbcTemplate.query(query, new TestGroupTest());
							if (testgroupspeccode.size() != 0) {
								String sequence = "select nsequenceno from seqnoinvoice where stablename ='producttest'";
								int nsequence = jdbcTemplate.queryForObject(sequence, Integer.class);
								int sequencenos = testgroupspeccode.size() + nsequence;
								int i = 0;
								for (i = 0; i < testgroupspeccode.size(); i++) {
									String Str = "select * from treetemplatemanipulation ttm,testgroupspecification tg where tg.napprovalstatus="
											+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and"
											+ " ttm.ntemplatemanipulationcode = tg.ntemplatemanipulationcode and  tg.nallottedspeccode = "
											+ testgroupspeccode.get(i).getNallottedspeccode();
									List<TreeTemplateManipulation> sampletype1 = (List<TreeTemplateManipulation>) jdbcTemplate
											.query(Str, new TreeTemplateManipulation());
									int productcode = sampletype1.get(0).getNproductcode();
									String sspecname = sampletype1.get(0).getSspecname();
									// FIX: Properly escape single quotes for SQL
									String escapedSspecname = sspecname.replace("'", "''");
									String str1 = "select * from product where nproductcode=" + productcode + "";
									List<Product> sproductcodes = (List<Product>) jdbcTemplate.query(str1,
											new Product());
									String sproductname = sproductcodes.get(0).getSproductname();
									// FIX: Properly escape single quotes for SQL
									String escapedSproductname = sproductname.replace("'", "''");

									String productcodes = " select * from invoiceproductmaster where sproductname='"
											+ escapedSproductname + "'";
									List<InvoiceProductMaster> nproductcodes = (List<InvoiceProductMaster>) jdbcTemplate
											.query(productcodes, new InvoiceProductMaster());
									if (nproductcodes.size() != 0) {
										if (sproductname.equals(nproductcodes.get(0).getSproductname())) {

											Double productcost = nproductcodes.get(0).getNcost();
											double Totalcost = 0;

											String sproductnames = nproductcodes.get(0).getSproductname();
											// FIX: Properly escape single quotes for SQL
											String escapedSproductnames = sproductnames.replace("'", "''");
											int nproductcode = nproductcodes.get(0).getNproductcode();
											int nlimscode = nproductcodes.get(0).getNlimsproduct();
											nsequence++;
											String testname = testgroupspeccode.get(i).getStestsynonym();
											// FIX: Properly escape single quotes for SQL
											String escapedTestname = testname.replace("'", "''");
											int testcode = testgroupspeccode.get(i).getNtestcode();
											double cost = testgroupspeccode.get(i).getNcost();

											Totalcost += cost;
											totalSum = productcost + Totalcost;
											StInsert = StInsert + "(" + nsequence + "," + nproductcode + "," + nlimscode
													+ ",'" + escapedSproductnames + "','" + escapedSspecname + "'," + testcode + ",'"
													+ escapedTestname + "'," + cost + "," + userInfo.getNmastersitecode()
													+ ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
													+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
													+ "),";
										}

										TestInsert1 = "insert into producttest(nproducttestcode,ninvproductcode,nlimsproductcode,sproductname,sspecname,ntestcode,sproducttestname,ntestcost,"
												+ " nmastersitecode,dmodifieddate,nstatus) values"
												+ StInsert.substring(0, StInsert.length() - 1) + ";";
										jdbcTemplate.execute(TestInsert1);

										String updatequery1 = "update seqnoinvoice set nsequenceno =" + sequencenos
												+ " where stablename='producttest'";
										jdbcTemplate.execute(updatequery1);
										TestInsert1 = "";
										StInsert = "";
										String updatequery12 = "update invoiceproductmaster set ncost =" + totalSum
												+ " where nproductcode=" + nproductcodes.get(0).getNproductcode();
										jdbcTemplate.execute(updatequery12);
									}
								}
							}
						}
					}

				}

				else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADDPRODUCTMASTERDATA",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				double totalSum = 0;
				List<TestGroupTest> testgroupspeccode = new ArrayList<>();
				String TestInsert1 = "";
				List<Integer> speccode = (List<Integer>) gettestdetails();
				List<Integer> c = speccode;
				String speccodes = c.toString().replace("[", "").replace("]", "");
				if (speccode.size() != 0 || speccode.size() == 0) {
					if (speccode.size() == 0) {
						int specode = 0;
						String query = "SELECT tgs.nallottedspeccode, " + "tgs.sspecname, "
								+ "tgp.nspecsampletypecode, " + "tt.ntestcode, " + "tt.stestsynonym, " + "tt.ncost, "
								+ "m.smethodname " // Adding method name to the SELECT clause
								+ "FROM testgroupspecification tgs "
								+ "JOIN testgroupspecsampletype tgp ON tgs.nallottedspeccode = tgp.nallottedspeccode "
								+ "JOIN testgrouptest tt ON tt.nspecsampletypecode = tgp.nspecsampletypecode "
								+ "LEFT JOIN method m ON tt.nmethodcode = m.nmethodcode " // LEFT JOIN to get method
																							// name
								+ "WHERE tgs.nallottedspeccode NOT IN (" + specode + ") " + "AND tgs.napprovalstatus = "
								+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
								+ "AND tgs.ntransactionstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ "AND tt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " " + "AND tgs.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ "AND tgp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

						// Assuming you're using jdbcTemplate to query
						testgroupspeccode = (List<TestGroupTest>) jdbcTemplate.query(query, new TestGroupTest());

					}
					if (speccode.size() != 0) {

						String query = "SELECT tgs.nallottedspeccode, " + "tgs.sspecname, "
								+ "tgp.nspecsampletypecode, " + "tt.ntestcode, " + "tt.stestsynonym, " + "tt.ncost, "
								+ "m.smethodname " // Adding method name to the SELECT clause
								+ "FROM testgroupspecification tgs "
								+ "JOIN testgroupspecsampletype tgp ON tgs.nallottedspeccode = tgp.nallottedspeccode "
								+ "JOIN testgrouptest tt ON tt.nspecsampletypecode = tgp.nspecsampletypecode "
								+ "LEFT JOIN method m ON tt.nmethodcode = m.nmethodcode " // LEFT JOIN for methodname
								+ "WHERE tgs.nallottedspeccode NOT IN (" + speccodes + ") "
								+ "AND tgs.napprovalstatus = "
								+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
								+ "AND tgs.ntransactionstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ "AND tt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " " + "AND tgs.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ "AND tgp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "";

						testgroupspeccode = (List<TestGroupTest>) jdbcTemplate.query(query, new TestGroupTest());
						if (testgroupspeccode.size() != 0) {
							String sequence = "select nsequenceno from seqnoinvoice where stablename ='producttest'";
							int nsequence = jdbcTemplate.queryForObject(sequence, Integer.class);
							int sequencenos = testgroupspeccode.size() + nsequence;
							int i = 0;
							for (i = 0; i < testgroupspeccode.size(); i++) {
								String Str = "select * from treetemplatemanipulation ttm,testgroupspecification tg where tg.napprovalstatus="
										+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and"
										+ " ttm.ntemplatemanipulationcode = tg.ntemplatemanipulationcode and  tg.nallottedspeccode = "
										+ testgroupspeccode.get(i).getNallottedspeccode();
								List<TreeTemplateManipulation> sampletype1 = (List<TreeTemplateManipulation>) jdbcTemplate
										.query(Str, new TreeTemplateManipulation());
								int productcode = sampletype1.get(0).getNproductcode();
								String sspecname = sampletype1.get(0).getSspecname();
								// FIX: Properly escape single quotes for SQL
								String escapedSspecname = sspecname.replace("'", "''");
								String str1 = "select * from product where nproductcode=" + productcode + "";
								List<Product> sproductcodes = (List<Product>) jdbcTemplate.query(str1, new Product());
								String sproductname = sproductcodes.get(0).getSproductname();
								// FIX: Properly escape single quotes for SQL
								String escapedSproductname = sproductname.replace("'", "''");

								String productcodes = " select * from invoiceproductmaster where sproductname='"
										+ escapedSproductname + "'";
								List<InvoiceProductMaster> nproductcodes = (List<InvoiceProductMaster>) jdbcTemplate
										.query(productcodes, new InvoiceProductMaster());
								if (nproductcodes.size() != 0) {
									if (sproductname.equals(nproductcodes.get(0).getSproductname())) {

										Double productcost = nproductcodes.get(0).getNcost();
										double Totalcost = 0;
										String sproductnames = nproductcodes.get(0).getSproductname();
										// FIX: Properly escape single quotes for SQL
										String escapedSproductnames = sproductnames.replace("'", "''");
										int nproductcode = nproductcodes.get(0).getNproductcode();
										int nlimscode = nproductcodes.get(0).getNlimsproduct();
										nsequence++;
										String testname = testgroupspeccode.get(0).getStestsynonym();
										// FIX: Properly escape single quotes for SQL
										String escapedTestname = testname.replace("'", "''");
										int testcode = testgroupspeccode.get(0).getNtestcode();
										double cost = testgroupspeccode.get(0).getNcost();

										Totalcost += cost;
										totalSum = productcost + Totalcost;
										StInsert = StInsert + "(" + nsequence + "," + nproductcode + "," + nlimscode
												+ ",'" + escapedSproductnames + "','" + escapedSspecname + "'," + testcode + ",'"
												+ escapedTestname + "'," + cost + "," + userInfo.getNmastersitecode() + ",'"
												+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
									}
								}

								TestInsert1 = "insert into producttest(nproducttestcode,ninvproductcode,nlimsproductcode,sproductname,sspecname,ntestcode,sproducttestname,ntestcost,"
										+ " nmastersitecode,dmodifieddate,nstatus) values"
										+ StInsert.substring(0, StInsert.length() - 1) + ";";
								jdbcTemplate.execute(TestInsert1);

								String updatequery = "update seqnoinvoice set nsequenceno =" + sequencenos
										+ " where stablename='producttest'";
								jdbcTemplate.execute(updatequery);
								TestInsert1 = "";
								StInsert = "";
								String updatequery1 = "update invoiceproductmaster set ncost =" + totalSum
										+ " where nproductcode=" + nproductcodes.get(0).getNproductcode();
								jdbcTemplate.execute(updatequery1);
							}
						}

					}

				}
			}
		}

		else if (sample.get(0).getNstatus() == -1) {

		    String strQuery = "select * from Clinicalview";
		    specimenname = jdbcTemplate.query(strQuery, new InvoiceExportImport());
		    String productnames = "";
		    String componentname = "";

		    if (specimenname.size() != 0) {
		        String sequencenoquery = "select nsequenceno from seqnoinvoice where stablename ='invoiceproductmaster'";
		        int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
		        int sequenceno = specimenname.size() + nsequenceno;

		        for (InvoiceExportImport objname : specimenname) {
		            nsequenceno++;
		            int componentcode = objname.getNcomponentcode();
		            double Totalcost = 0;

		            String spec = objname.getSspecname();
		            String escapedSpec = spec != null ? spec.replace("'", "''") : "";

		            componentname = objname.getScomponentname();
		            String escapedComponentproductnames = componentname != null ? componentname.replace("'", "''") : "";

		            productnames = escapedComponentproductnames + "(" + escapedSpec + ")";

		            String methodname = objname.getSmethodname();
		            String escapedMethodname = methodname != null ? methodname.replace("'", "''") : "";

		            TestInsert = "";
		            StInsert = "";

		            // Always escape fixed string literals (though "NA" doesn’t need it)
		            String escapedDescriptionClinical = "NA".replace("'", "''");
		            String escapedAddText1Clinical = "NA".replace("'", "''");
		            String escapedAddText2Clinical = "NA".replace("'", "''");
		            String escapedComponentproductname = "NA".replace("'", "''");

		            // ✅ This is the line that was failing; now properly escaped:
		            productnames = escapedComponentproductnames + "(" + escapedSpec + ")";

		            strInsert = strInsert + "(" + nsequenceno + ",1,'" 
		                + escapedComponentproductnames + "','" 
		                + escapedDescriptionClinical + "','" 
		                + escapedAddText1Clinical + "','" 
		                + escapedAddText2Clinical + "','" 
		                + escapedMethodname + "'," 
		                + Enumeration.TransactionStatus.YES.gettransactionstatus() + "," 
		                + Totalcost + ",'LIMS'," 
		                + componentcode + ",'" 
		                + escapedComponentproductname + "',-1,'" 
		                + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," 
		                + userInfo.getNmastersitecode() + "," 
		                + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

		        }

		        productInsert = "insert into invoiceproductmaster(nproductcode,ntypecode,sproductname,sdescription,"
		            + "sinvoicedescription,saddtext1,saddtext2,ntaxavailable,ncost,slimsdata,nlimsproduct,scomponentname,"
		            + "nusercode,dmodifieddate,nsitecode,nstatus) values "
		            + strInsert.substring(0, strInsert.length() - 1) + ";";

		        jdbcTemplate.execute(productInsert);

		        String updatequery = "update seqnoinvoice set nsequenceno =" + sequenceno
		            + " where stablename='invoiceproductmaster'";
		        jdbcTemplate.execute(updatequery);
		    }

		}
		savedMergeProductMasterList.add(objInvoiceExportImport);
		multilingualIDList.add("IDS_MERGEPRODUCTMASTER");
		auditUtilityFunction.fnInsertAuditAction(savedMergeProductMasterList, 1, null, multilingualIDList, userInfo);

		return getInvoiceExportImport(userInfo);

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

	public List<Integer> getproductdata() {

		String queryString = "select DISTINCT pt.nproductcode,pt.sproductname,pm.nproductcode,pt.sproductname  from product pt,invoiceproductmaster pm where "
				+ "pm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ "pt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ "pm.slimsdata = 'LIMS'" + " and pt.sproductname=pm.sproductname ";
		List<Product> identicaldata = (List<Product>) jdbcTemplate.query(queryString, new Product());
		List<Integer> identicaldata1 = identicaldata.stream().map(Product::getNproductcode)
				.collect(Collectors.toList());
		return identicaldata1;

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
	public List<Product> getproductdetails() {

		String Str = "select nproductcode,sproductname from product where" + " nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + " nproductcode not in ("
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ")";

		List<Product> productdetails = (List<Product>) jdbcTemplate.query(Str, new Product());
		return productdetails;

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
	public List<Client> getcustomerdatastatus() {

		List<Integer> identicaldataclientdata = (List<Integer>) getclientdata();
		List<Client> clientname = new ArrayList<>();
		List<Integer> s = identicaldataclientdata;
		String result = s.toString().replace("[", "").replace("]", "");

		if (identicaldataclientdata.size() != 0) {

			String str = "select sclientname,sclientid from client where nclientcode not in (" + result + ",-1) and "
					+ " nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			clientname = (List<Client>) jdbcTemplate.query(str, new Client());

			if (clientname.size() != 0) {

				int count = clientname.size();
				String data = count + " client data not merged";
				String string = "update invoiceexportimport set sdatastatus='(" + data + ")' where ncode=2";
				jdbcTemplate.execute(string);
			} else {
				String query = "update invoiceexportimport set sdatastatus='(Merged)'where ncode=2";
				jdbcTemplate.execute(query);
			}
		} else if (identicaldataclientdata.size() == 0) {

			String Query = "select * from client where nclientcode not in (-1) and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			clientname = (List<Client>) jdbcTemplate.query(Query, new Client());

			if (clientname.size() != 0) {

				int count = clientname.size();
				String data = count + " client data not merged";
				String string = "update invoiceexportimport set sdatastatus='(" + data + ")' where ncode=2";
				jdbcTemplate.execute(string);
			} else {
				String query = "update invoiceexportimport set sdatastatus='(Merged)'where ncode=2";
				jdbcTemplate.execute(query);
			}
		}

		return clientname;
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
	public List<Product> getproductdatastatus(UserInfo userInfo) throws Exception {
		// final ObjectMapper objmapper = new ObjectMapper();

		List<Integer> identicaldataproductdata = (List<Integer>) getproductdata();
		List<Product> productname = new ArrayList<>();
		List<TestGroupTest> testgroupspeccode = new ArrayList<>();
		List<Integer> s = identicaldataproductdata;
		String result = s.toString().replace("[", "").replace("]", "");

		if (identicaldataproductdata.size() != 0 || identicaldataproductdata.size() == 0) {
			if (identicaldataproductdata.size() == 0) {
				String str = "select nproductcode,sproductname from product where nproductcode not in (-1) and "
						+ " nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				productname = (List<Product>) jdbcTemplate.query(str, new Product());
			}
			if (identicaldataproductdata.size() != 0) {
				String str = "select nproductcode,sproductname from product where nproductcode not in (" + result
						+ ",-1) and " + " nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ "";
				productname = (List<Product>) jdbcTemplate.query(str, new Product());
			}
			// String StInsert = "";
			// String TestInsert1 = "";
			List<Integer> speccode = (List<Integer>) gettestdetails();
			List<Integer> c = speccode;
			String speccodes = c.toString().replace("[", "").replace("]", "");
			if (speccode.size() != 0 || speccode.size() == 0) {
				if (speccode.size() == 0) {
					int specode = 0;
					String query = " select tgs.nallottedspeccode,tgp.nspecsampletypecode,tt.ntestcode,tt.stestsynonym,tt.ncost from testgroupspecification tgs,testgroupspecsampletype tgp,testgrouptest tt where "
							+ " tgs.nallottedspeccode = tgp.nallottedspeccode and "
							+ " tt.nspecsampletypecode = tgp.nspecsampletypecode and  tgs.nallottedspeccode not in ("
							+ specode + ") and " + " tgs.napprovalstatus="
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and "
							+ " tgs.ntransactionstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and " + " tt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and " + " tgs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and " + " tgp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "";
					testgroupspeccode = (List<TestGroupTest>) jdbcTemplate.query(query, new TestGroupTest());
				}
				if (speccode.size() != 0) {

					String query = " select tgs.nallottedspeccode,tgs.sspecname,tgp.nspecsampletypecode,tt.ntestcode,tt.stestsynonym,tt.ncost from testgroupspecification tgs,testgroupspecsampletype tgp,testgrouptest tt where "
							+ " tgs.nallottedspeccode = tgp.nallottedspeccode and "
							+ " tt.nspecsampletypecode = tgp.nspecsampletypecode and  tgs.nallottedspeccode not in ("
							+ speccodes + ") and " + " tgs.napprovalstatus="
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and "
							+ " tgs.ntransactionstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and " + " tt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and " + " tgs.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and " + " tgp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "";
					testgroupspeccode = (List<TestGroupTest>) jdbcTemplate.query(query, new TestGroupTest());

				}

				if (productname.size() != 0 && testgroupspeccode.size() != 0) {

					int productcount = productname.size();
					int testcount = testgroupspeccode.size();
					String productdata = productcount + " product data not merged";
					String testdata = testcount + " test data not merged";

					String string = "update invoiceexportimport set sdatastatus='(" + productdata + "," + testdata
							+ ")'where ncode=1";
					jdbcTemplate.execute(string);
				} else if (productname.size() != 0) {
					int productcount = productname.size();
					String productdata = productcount + " product data not merged";

					String string = "update invoiceexportimport set sdatastatus='(" + productdata + ")' where ncode=1";
					jdbcTemplate.execute(string);

				} else if (testgroupspeccode.size() != 0) {
					int testcount = testgroupspeccode.size();
					String testdata = testcount + " test data not merged";

					String string = "update invoiceexportimport set sdatastatus='(" + testdata + ")' where ncode=1";
					jdbcTemplate.execute(string);
				} else {
					String Qry = "update invoiceexportimport set sdatastatus='(Merged)'where ncode=1";
					jdbcTemplate.execute(Qry);

				}
			}

		}
		return productname;
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

	@Override
	public ResponseEntity<Object> exportpatientmaster(Map<String, Object> objmap, UserInfo userInfo) throws Exception {

	    Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

	    List<InvoicePatient> lstdata = (List<InvoicePatient>) getexportpatientdatas();
	    if (lstdata.size() == 0) {
	        return new ResponseEntity<>(
	                commonFunction.getMultilingualMessage("IDS_EXPORTDATASNOTFOUND", userInfo.getSlanguagefilename()),
	                HttpStatus.EXPECTATION_FAILED);

	    }

	    final String sFileName = "InvoicePatientMaster";

	    final String OutputFileName = sFileName + ".csv";

	    final String homePath = ftpUtilityFunction.getFileAbsolutePath();

	    String target = System.getenv(homePath);

	    // Fix: Check if target is null before calling trim()
	    if (target == null) {
	        // You can either throw a meaningful exception or provide a default path
	        throw new RuntimeException("Environment variable '" + homePath + "' is not set");
	        // OR use a default path:
	        // target = "/default/export/path"; // Uncomment and set appropriate default path
	    }
	    
	    target = target.trim();

	    final String targetPath1 = target + Enumeration.FTP.INVOICE_EXPORT.getFTP() + OutputFileName;

	    File file = new File(targetPath1);

	    FileWriter outputfile = new FileWriter(file);

	    CSVWriter writer = new CSVWriter(outputfile);
	    String[] header = { "FirstName", "Lastname", "DOB", "Age", "Gender", "FatherName", "PostCode", "Street",
	            "HouseNo", "FlatNo", "PhoneNo", "MobileNo", "Email", "District", "City", "Region", "Country" };
	    writer.writeNext(header);
	    for (InvoicePatient objcol2 : lstdata) {
	        String Str = "select g.jsondata->'sgendername'->>'en-US' AS sgendername,r.sregionname,c.scountryname,ct.scityname,d.sdistrictname from gender g,city ct,country c,district d,region r"
	                + " where g.ngendercode= " + objcol2.getNgendercode() + "" + " and c.ncountrycode="
	                + objcol2.getNcountrycode() + " and d.ndistrictcode=" + objcol2.getNdistrictcode()
	                + " and r.nregioncode=" + objcol2.getNregioncode() + "";
	        List<InvoicePatient> obj = (List<InvoicePatient>) jdbcTemplate.query(Str, new InvoicePatient());
	        Date D = objcol2.getDdob();
	        objcol2.setSgendername(obj.get(0).getSgendername());
	        objcol2.setSdistrictname(obj.get(0).getSdistrictname());
	        objcol2.setScityname(obj.get(0).getScityname());
	        objcol2.setSregionname(obj.get(0).getSregionname());
	        objcol2.setScountryname(obj.get(0).getScountryname());
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        String FirstName = objcol2.getSfirstname();
	        String LastName = objcol2.getSlastname();
	        String Age = objcol2.getSage();
	        String original = Age;
	        String modified = original.replace(",", "");
	        String DOB = dateFormat.format(D);
	        String Gender = objcol2.getSgendername();
	        String FatherName = objcol2.getSfathername();
	        String Postcode = objcol2.getSpostalcode();
	        String Street = objcol2.getSstreet();
	        String HouseNo = objcol2.getShouseno();
	        String FlatNo = objcol2.getSflatno();
	        String PhoneNo = objcol2.getSphoneno();
	        String MobileNo = objcol2.getSmobileno();
	        String Email = objcol2.getSemail();
	        String District = objcol2.getSdistrictname();
	        String City = objcol2.getScityname();
	        String Region = objcol2.getSregionname();
	        String Country = objcol2.getScountryname();
	        String[] data1 = { FirstName, LastName, DOB, modified, Gender, FatherName, Postcode, Street, HouseNo,
	                FlatNo, PhoneNo, MobileNo, Email, District, City, Region, Country };
	        writer.writeNext(data1);
	    }
	    writer.flush();
	    writer.close();
	    String sDownloadPathName = getdownloadpathname();
	    if (sDownloadPathName != null) {
	        outputMap.put("ExportExcelPath", sDownloadPathName + "InvoicePatientMaster" + ".csv");
	        outputMap.put("ExportExcel", "Success");
	    } else {
	        outputMap.put("rtn", Enumeration.ReturnStatus.FAILED.getreturnstatus().trim().toString());
	    }
	    return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
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

	public List<InvoicePatient> getexportpatientdatas() throws Exception {

		String queryString = "select a.*" + "  from invoicepatientmaster a " + " where  a.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<InvoicePatient> PatientDetails = (List<InvoicePatient>) jdbcTemplate.query(queryString,
				new InvoicePatient());
		return PatientDetails;
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
	@Override
	public ResponseEntity<Object> mergepatientmaster(InvoiceExportImport objInvoiceExportImport, UserInfo userInfo)
			throws Exception {

		List<String> identicaldata1 = (List<String>) getpatientdata();
		List<Patient> Patientname = new ArrayList<>();
		List<String> PatientList = new ArrayList<>();
		for (String obj : identicaldata1) {
			PatientList.add("'" + obj + "'");
		}
		String spatientid = String.join(", ", PatientList);
		System.out.println(spatientid);
		String patientInsert = "";
		if (identicaldata1.size() != 0) {
			String str = "select * from patientmaster where spatientid not in (" + spatientid + ") and " + " nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			Patientname = (List<Patient>) jdbcTemplate.query(str, new Patient());
			if (Patientname.size() != 0) {
				String sequencenoquery = "select nsequenceno from seqnoinvoice where stablename ='invoicepatientmaster'";
				int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
				int sequenceno = Patientname.size() + nsequenceno;
				for (Patient objname : Patientname) {
					nsequenceno++;
					String original = objname.getSage();
					String modified = original.replace(",", "");
					// Escape single quotes in string fields
					String patientId = objname.getSpatientid() == null ? "" : objname.getSpatientid().replace("'", "''");
					String firstName = objname.getSfirstname() == null ? "" : objname.getSfirstname().replace("'", "''");
					String lastName = objname.getSlastname() == null ? "" : objname.getSlastname().replace("'", "''");
					String fatherName = objname.getSfathername() == null ? "" : objname.getSfathername().replace("'", "''");
					String postalCode = objname.getSpostalcode() == null ? "" : objname.getSpostalcode().replace("'", "''");
					String street = objname.getSstreet() == null ? "" : objname.getSstreet().replace("'", "''");
					String houseNo = objname.getShouseno() == null ? "" : objname.getShouseno().replace("'", "''");
					String flatNo = objname.getSflatno() == null ? "" : objname.getSflatno().replace("'", "''");
					String postalCodeTemp = objname.getSpostalcodetemp() == null ? "" : objname.getSpostalcodetemp().replace("'", "''");
					String streetTemp = objname.getSstreettemp() == null ? "" : objname.getSstreettemp().replace("'", "''");
					String houseNoTemp = objname.getShousenotemp() == null ? "" : objname.getShousenotemp().replace("'", "''");
					String flatNoTemp = objname.getSflatnotemp() == null ? "" : objname.getSflatnotemp().replace("'", "''");
					String phoneNo = objname.getSphoneno() == null ? "" : objname.getSphoneno().replace("'", "''");
					String mobileNo = objname.getSmobileno() == null ? "" : objname.getSmobileno().replace("'", "''");
					String email = objname.getSemail() == null ? "" : objname.getSemail().replace("'", "''");
					String refId = objname.getSrefid() == null ? "" : objname.getSrefid().replace("'", "''");
					String passportNo = objname.getSpassportno() == null ? "" : objname.getSpassportno().replace("'", "''");
					String externalId = objname.getSexternalid() == null ? "" : objname.getSexternalid().replace("'", "''");
					
					patientInsert = patientInsert + "(" + nsequenceno + ",'"
							+ patientId + "','"
							+ firstName + "','"
							+ lastName + "','"
							+ (objname.getDdob() == null ? "" : objname.getDdob()) + "'," + " '"
							+ (modified == null ? "" : modified) + "',"
							+ (objname.getNgendercode() == 0 ? "" : objname.getNgendercode()) + ",'"
							+ fatherName + "',"
							+ (objname.getNneedmigrant() == 0 ? "" : objname.getNneedmigrant()) + "," + " "
							+ (objname.getNcountrycode() == 0 ? "" : objname.getNcountrycode()) + ","
							+ (objname.getNregioncode() == 0 ? "" : objname.getNregioncode()) + ","
							+ (objname.getNdistrictcode() == 0 ? "" : objname.getNdistrictcode()) + ","
							+ (objname.getNcitycode() == 0 ? "" : objname.getNcitycode()) + ",'"
							+ postalCode + "'," + " '"
							+ street + "','"
							+ houseNo + "','"
							+ flatNo + "',"
							+ (objname.getNneedcurrentaddress() == 0 ? "" : objname.getNneedcurrentaddress()) + ","
							+ (objname.getNregioncodetemp() == 0 ? "" : objname.getNregioncodetemp()) + ","
							+ (objname.getNdistrictcodetemp() == 0 ? "" : objname.getNdistrictcodetemp()) + ","
							+ (objname.getNcitycodetemp() == 0 ? "" : objname.getNcitycodetemp()) + ",'"
							+ postalCodeTemp + "','"
							+ streetTemp + "','"
							+ houseNoTemp + "'," + " '"
							+ flatNoTemp + "','"
							+ phoneNo + "','"
							+ mobileNo + "','"
							+ email + "','"
							+ refId + "'," + " '"
							+ passportNo + "','"
							+ externalId + "','"
							+ (dateUtilityFunction.getCurrentDateTime(userInfo) == null ? ""
									: dateUtilityFunction.getCurrentDateTime(userInfo))
							+ "'," + (userInfo.getNmastersitecode() == 0 ? "" : userInfo.getNmastersitecode()) + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
				}

				patientInsert = "insert into invoicepatientmaster(npatientno,spatientid, sfirstname, slastname, ddob, "
						+ "sage, ngendercode, sfathername, nneedmigrant,"
						+ " ncountrycode, nregioncode, ndistrictcode, ncitycode, spostalcode,"
						+ " sstreet, shouseno, sflatno, nneedcurrentaddress, nregioncodetemp, ndistrictcodetemp,"
						+ " ncitycodetemp, spostalcodetemp, sstreettemp, shousenotemp, sflatnotemp, sphoneno, "
						+ "smobileno, semail, srefid, spassportno, sexternalid, dmodifieddate, nsitecode, nstatus)"
						+ " values" + patientInsert.substring(0, patientInsert.length() - 1) + ";";

				String updatequery = "update seqnoinvoice set nsequenceno =" + sequenceno
						+ " where stablename='invoicepatientmaster'";
				jdbcTemplate.execute(updatequery);
				jdbcTemplate.execute(patientInsert);
			}
		} else if (identicaldata1.size() == 0) {
			List<Patient> patientdetails = (List<Patient>) getpatientdetails();
			String sequencenoquery = "select nsequenceno from seqnoinvoice where stablename ='invoicepatientmaster'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			int sequenceno = patientdetails.size() + nsequenceno;
			for (Patient objname : patientdetails) {
				String original = objname.getSage();
				String modified = original.replace(",", "");
				nsequenceno++;
				// Escape single quotes in string fields
				String patientId = objname.getSpatientid() == null ? "" : objname.getSpatientid().replace("'", "''");
				String firstName = objname.getSfirstname() == null ? "" : objname.getSfirstname().replace("'", "''");
				String lastName = objname.getSlastname() == null ? "" : objname.getSlastname().replace("'", "''");
				String fatherName = objname.getSfathername() == null ? "" : objname.getSfathername().replace("'", "''");
				String postalCode = objname.getSpostalcode() == null ? "" : objname.getSpostalcode().replace("'", "''");
				String street = objname.getSstreet() == null ? "" : objname.getSstreet().replace("'", "''");
				String houseNo = objname.getShouseno() == null ? "" : objname.getShouseno().replace("'", "''");
				String flatNo = objname.getSflatno() == null ? "" : objname.getSflatno().replace("'", "''");
				String postalCodeTemp = objname.getSpostalcodetemp() == null ? "" : objname.getSpostalcodetemp().replace("'", "''");
				String streetTemp = objname.getSstreettemp() == null ? "" : objname.getSstreettemp().replace("'", "''");
				String houseNoTemp = objname.getShousenotemp() == null ? "" : objname.getShousenotemp().replace("'", "''");
				String flatNoTemp = objname.getSflatnotemp() == null ? "" : objname.getSflatnotemp().replace("'", "''");
				String phoneNo = objname.getSphoneno() == null ? "" : objname.getSphoneno().replace("'", "''");
				String mobileNo = objname.getSmobileno() == null ? "" : objname.getSmobileno().replace("'", "''");
				String email = objname.getSemail() == null ? "" : objname.getSemail().replace("'", "''");
				String refId = objname.getSrefid() == null ? "" : objname.getSrefid().replace("'", "''");
				String passportNo = objname.getSpassportno() == null ? "" : objname.getSpassportno().replace("'", "''");
				String externalId = objname.getSexternalid() == null ? "" : objname.getSexternalid().replace("'", "''");
				
				patientInsert = patientInsert + "(" + nsequenceno + ",'"
						+ patientId + "','"
						+ firstName + "','"
						+ lastName + "','"
						+ (objname.getDdob() == null ? "" : objname.getDdob()) + "'," + " '"
						+ (modified == null ? "" : modified) + "',"
						+ (objname.getNgendercode() == 0 ? "" : objname.getNgendercode()) + ",'"
						+ fatherName + "',"
						+ (objname.getNneedmigrant() == 0 ? "" : objname.getNneedmigrant()) + "," + " "
						+ (objname.getNcountrycode() == 0 ? "" : objname.getNcountrycode()) + ","
						+ (objname.getNregioncode() == 0 ? "" : objname.getNregioncode()) + ","
						+ (objname.getNdistrictcodetemp() == 0 ? "" : objname.getNdistrictcodetemp()) + ","
						+ (objname.getNcitycode() == 0 ? "" : objname.getNcitycode()) + ",'"
						+ postalCode + "'," + " '"
						+ street + "','"
						+ houseNo + "','"
						+ flatNo + "',"
						+ (objname.getNneedcurrentaddress() == 0 ? "" : objname.getNneedcurrentaddress()) + ","
						+ (objname.getNregioncodetemp() == 0 ? "" : objname.getNregioncodetemp()) + ","
						+ (objname.getNdistrictcodetemp() == 0 ? "" : objname.getNdistrictcodetemp()) + ","
						+ (objname.getNcitycodetemp() == 0 ? "" : objname.getNcitycodetemp()) + ",'"
						+ postalCodeTemp + "','"
						+ streetTemp + "','"
						+ houseNoTemp + "'," + " '"
						+ flatNoTemp + "','"
						+ phoneNo + "','"
						+ mobileNo + "','"
						+ email + "','"
						+ refId + "'," + " '"
						+ passportNo + "','"
						+ externalId + "','"
						+ (dateUtilityFunction.getCurrentDateTime(userInfo) == null ? ""
								: dateUtilityFunction.getCurrentDateTime(userInfo))
						+ "'," + (userInfo.getNmastersitecode() == 0 ? "" : userInfo.getNmastersitecode()) + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

			}
			patientInsert = "insert into invoicepatientmaster(npatientno,spatientid, sfirstname, slastname, ddob, "
					+ "sage, ngendercode, sfathername, nneedmigrant,"
					+ " ncountrycode, nregioncode, ndistrictcode, ncitycode, spostalcode,"
					+ " sstreet, shouseno, sflatno, nneedcurrentaddress, nregioncodetemp, ndistrictcodetemp,"
					+ " ncitycodetemp, spostalcodetemp, sstreettemp, shousenotemp, sflatnotemp, sphoneno, "
					+ "smobileno, semail, srefid, spassportno, sexternalid, dmodifieddate, nsitecode, nstatus)"
					+ " values" + patientInsert.substring(0, patientInsert.length() - 1) + ";";

			String updatequery = "update seqnoinvoice set nsequenceno =" + sequenceno
					+ " where stablename='invoicepatientmaster'";
			jdbcTemplate.execute(updatequery);
			jdbcTemplate.execute(patientInsert);
		}
		return getInvoiceExportImport(userInfo);

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

	public List<String> getpatientdata() {
		String queryString = "SELECT pm.spatientid, " + "pm.ddob AS pm_ddob, " + "ipm.ddob AS ipm_ddob, "
				+ "pm.sphoneno AS pm_sphoneno, " + "ipm.sphoneno AS ipm_sphoneno, "
				+ "CONCAT(pm.sfirstname, ' ', pm.slastname) AS limspatientname, "
				+ "CONCAT(ipm.sfirstname, ' ', ipm.slastname) AS invpatientname " + "FROM patientmaster pm "
				+ "JOIN invoicepatientmaster ipm "
				+ "  ON (pm.ddob = ipm.ddob AND CONCAT(pm.sfirstname, ' ', pm.slastname) = CONCAT(ipm.sfirstname, ' ', ipm.slastname)) "
				+ "WHERE pm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND"
				+ " ipm.nstatus =  " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<Patient> identicaldata = (List<Patient>) jdbcTemplate.query(queryString, new Patient());

		List<String> identicaldata1 = identicaldata.stream().map(Patient::getSpatientid).collect(Collectors.toList());
		return identicaldata1;
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
	public List<Patient> getpatientdetails() {
		String Str = "select  spatientid,sfirstname,slastname,ddob,sage,ngendercode,sfathername,nneedmigrant,ncountrycode,nregioncode,ndistrictcode"
				+ "	ncitycode,spostalcode,sstreet,shouseno,sflatno,nneedcurrentaddress,nregioncodetemp,ndistrictcodetemp,ncitycodetemp,"
				+ " spostalcodetemp,sstreettemp,shousenotemp,sflatnotemp from patientmaster where" + " nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		List<Patient> patientdetails = (List<Patient>) jdbcTemplate.query(Str, new Patient());
		return patientdetails;
	}

	/**
	* This method is used to import PatientMaster data from a CSV file for the specified site.
	*
	* @param objMultipart [MultipartFile] the uploaded CSV file containing PatientMaster data
	* @param userInfo     [UserInfo] the user information of the logged-in user
	* @return ResponseEntity indicating success or failure of the import operation
	* @throws Exception if any error occurs during processing

	*/
	@SuppressWarnings({ "unchecked", "resource" })
	@Override
	public ResponseEntity<Object> importPatientMaster(MultipartHttpServletRequest request, UserInfo userInfo)
	        throws Exception {
	    ObjectMapper objectMapper = new ObjectMapper();
	    Map<String, Object> reg = objectMapper.readValue(request.getParameter("Map"), Map.class);
	    Map<String, Object> selectedRecord = (Map<String, Object>) reg.get("selectedRecord");
	    List<Map<String, String>> filenames = (List<Map<String, String>>) selectedRecord.get("sfilename");
	    String path = filenames.get(0).get("path");

	    if (path == null || path.isEmpty()) {
	        return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTCSVIMPORT",
	                userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
	    }

	    if (!path.matches("InvoicePatientMaster(?:\\(\\d+\\))?\\.csv")) {
	        return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PATIENTMASTERFILEALERT",
	                userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
	    }

	    MultipartFile objmultipart = request.getFile("InvoiceImportFile");
	    if (objmultipart == null) {
	        return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTCSVIMPORT",
	                userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
	    }

	    // Get existing patients for duplicate check
	    String st2 = "select * from invoicepatientmaster where nstatus = " + 
	            Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	    final List<InvoicePatient> obj1 = (List<InvoicePatient>) jdbcTemplate.query(st2, new InvoicePatient());
	    
	    List<String> existingPatients = obj1.stream()
	            .map(patient -> patient.getSfirstname() + " " + patient.getSlastname() + " " + patient.getSage())
	            .collect(Collectors.toList());

	    CSVReader csvReader = new CSVReader(
	            new InputStreamReader(objmultipart.getInputStream(), StandardCharsets.UTF_8));

	    String[] headers = null;
	    String[] values;
	    int successCount = 0;
	    int updateCount = 0;

	    while ((values = csvReader.readNext()) != null) {
	        if (headers == null) {
	            headers = values;
	            continue;
	        }

	        if (values.length < 17) {
	            continue; // Skip incomplete rows
	        }

	        // Clean and extract values
	        String firstName = sanitizeSqlString(values[0]);
	        String lastName = sanitizeSqlString(values[1]);
	        String dob = sanitizeSqlString(values[2]);
	        String age = sanitizeSqlString(values[3]);
	        String genderName = sanitizeSqlString(values[4]);
	        String fatherName = sanitizeSqlString(values[5]);
	        String postalCode = sanitizeSqlString(values[6]);
	        String street = sanitizeSqlString(values[7]);
	        String houseNo = sanitizeSqlString(values[8]);
	        String flatNo = sanitizeSqlString(values[9]);
	        String phoneNo = sanitizeSqlString(values[10]);
	        String mobileNo = sanitizeSqlString(values[11]);
	        String email = sanitizeSqlString(values[12]);
	        String districtName = sanitizeSqlString(values[13]);
	        String cityName = sanitizeSqlString(values[14]);
	        String regionName = sanitizeSqlString(values[15]);
	        String countryName = sanitizeSqlString(values[16]);

	        String patientKey = firstName + " " + lastName + " " + age;

	        if (!existingPatients.contains(patientKey)) {
	            // Insert new record
	            int seqNo = jdbcTemplate.queryForObject(
	                    "SELECT nsequenceno FROM seqnoinvoice WHERE stablename ='invoicepatientmaster'",
	                    Integer.class) + 1;

	            // Lookup code values
	            int genderCode = getCodeValue("gender", "ngendercode", "jsondata->'sgendername'->>'en-US'", genderName);
	            int districtCode = getCodeValue("district", "ndistrictcode", "sdistrictname", districtName);
	            int cityCode = getCodeValue("city", "ncitycode", "scityname", cityName);
	            int regionCode = getCodeValue("region", "nregioncode", "sregionname", regionName);
	            int countryCode = getCodeValue("country", "ncountrycode", "scountryname", countryName);

	            StringBuilder query = new StringBuilder();
	            query.append("INSERT INTO invoicepatientmaster (");
	            query.append("npatientno, sfirstname, slastname, ddob, sage, ngendercode, ");
	            query.append("sfathername, spostalcode, sstreet, shouseno, sflatno, ");
	            query.append("sphoneno, smobileno, semail, ndistrictcode, ncitycode, ");
	            query.append("nregioncode, ncountrycode, dmodifieddate, nsitecode, nstatus) VALUES (");

	            query.append(seqNo).append(", ");
	            query.append("'").append(firstName).append("', ");
	            query.append("'").append(lastName).append("', ");
	            query.append("'").append(dob).append("', ");
	            query.append("'").append(age).append("', ");
	            query.append(genderCode).append(", ");
	            query.append("'").append(fatherName).append("', ");
	            query.append("'").append(postalCode).append("', ");
	            query.append("'").append(street).append("', ");
	            query.append("'").append(houseNo).append("', ");
	            query.append("'").append(flatNo).append("', ");
	            query.append("'").append(phoneNo).append("', ");
	            query.append("'").append(mobileNo).append("', ");
	            query.append("'").append(email).append("', ");
	            query.append(districtCode).append(", ");
	            query.append(cityCode).append(", ");
	            query.append(regionCode).append(", ");
	            query.append(countryCode).append(", ");
	            query.append("'").append(dateUtilityFunction.getCurrentDateTime(userInfo)).append("', ");
	            query.append(userInfo.getNmastersitecode()).append(", ");
	            query.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(")");

	            jdbcTemplate.execute(query.toString());
	            jdbcTemplate.execute("UPDATE seqnoinvoice SET nsequenceno = " + seqNo + 
	                    " WHERE stablename = 'invoicepatientmaster'");
	            successCount++;

	        } else {
	            // Update existing record
	            // Lookup code values
	            int genderCode = getCodeValue("gender", "ngendercode", "jsondata->'sgendername'->>'en-US'", genderName);
	            int districtCode = getCodeValue("district", "ndistrictcode", "sdistrictname", districtName);
	            int cityCode = getCodeValue("city", "ncitycode", "scityname", cityName);
	            int regionCode = getCodeValue("region", "nregioncode", "sregionname", regionName);
	            int countryCode = getCodeValue("country", "ncountrycode", "scountryname", countryName);

	            StringBuilder updateQuery = new StringBuilder();
	            updateQuery.append("UPDATE invoicepatientmaster SET ");
	            updateQuery.append("ddob = '").append(dob).append("', ");
	            updateQuery.append("sage = '").append(age).append("', ");
	            updateQuery.append("ngendercode = ").append(genderCode).append(", ");
	            updateQuery.append("sfathername = '").append(fatherName).append("', ");
	            updateQuery.append("spostalcode = '").append(postalCode).append("', ");
	            updateQuery.append("sstreet = '").append(street).append("', ");
	            updateQuery.append("shouseno = '").append(houseNo).append("', ");
	            updateQuery.append("sflatno = '").append(flatNo).append("', ");
	            updateQuery.append("sphoneno = '").append(phoneNo).append("', ");
	            updateQuery.append("smobileno = '").append(mobileNo).append("', ");
	            updateQuery.append("semail = '").append(email).append("', ");
	            updateQuery.append("ndistrictcode = ").append(districtCode).append(", ");
	            updateQuery.append("ncitycode = ").append(cityCode).append(", ");
	            updateQuery.append("nregioncode = ").append(regionCode).append(", ");
	            updateQuery.append("ncountrycode = ").append(countryCode).append(" ");
	            updateQuery.append("WHERE sfirstname = '").append(firstName).append("' ");
	            updateQuery.append("AND slastname = '").append(lastName).append("'");

	            jdbcTemplate.execute(updateQuery.toString());
	            updateCount++;
	        }
	    }

	    Map<String, Object> result = new HashMap<>();
	    result.put("inserted", successCount);
	    result.put("updated", updateCount);
	    result.put("message", "Patient import completed successfully");

	    return new ResponseEntity<>(result, HttpStatus.OK);
	}

	private int getCodeValue(String tableName, String codeColumn, String nameColumn, String nameValue) {
	    try {
	        String query = String.format("SELECT %s FROM %s WHERE %s = '%s' AND nstatus = %d",
	                codeColumn, tableName, nameColumn, sanitizeSqlString(nameValue),
	                Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
	        
	        return jdbcTemplate.queryForObject(query, Integer.class);
	    } catch (Exception e) {
	        return 0;
	    }
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
	public List<Patient> getpatientdatastatus() {
		List<String> data = (List<String>) getpatientdata();
		List<Patient> Patientname = new ArrayList<>();
		List<String> PatientList = new ArrayList<>();
		for (String obj : data) {
			PatientList.add("'" + obj + "'");
		}
		String spatientid = String.join(", ", PatientList);
		System.out.println(spatientid);

		if (data.size() != 0) {
			String str = "select * from patientmaster where spatientid not in (" + spatientid + ") and " + "nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			Patientname = (List<Patient>) jdbcTemplate.query(str, new Patient());
			if (Patientname.size() != 0) {
				int count = Patientname.size();
				String Patientcount = count + " Patient data not merged";
				String string = "update invoiceexportimport set sdatastatus='(" + Patientcount + ")' where ncode=3";
				jdbcTemplate.execute(string);
			} else {
				String query = "update invoiceexportimport set sdatastatus='(Merged)'where ncode=3";
				jdbcTemplate.execute(query);
			}
		} else if (data.size() == 0) {
			String str = "select * from patientmaster where " + " nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			Patientname = (List<Patient>) jdbcTemplate.query(str, new Patient());
			if (Patientname.size() != 0) {
				String Query = "select * from patientmaster where nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
				Patientname = (List<Patient>) jdbcTemplate.query(Query, new Patient());
				// if(Patientname.size()!=0) {
				int count = Patientname.size();
				String Patientcount = count + " Patient data not merged";
				String string = "update invoiceexportimport set sdatastatus='(" + Patientcount + ")' where ncode=3";
				jdbcTemplate.execute(string);
			} else {
				String query = "update invoiceexportimport set sdatastatus='(Merged)'where ncode=3";
				jdbcTemplate.execute(query);
			}
		}

		return Patientname;
	}

	public List<TestPackage> getpackagedatastatus() {
	    
	    List<TestPackage> data = (List<TestPackage>) getPackageDetails();
		List<TestPackage> packagename = new ArrayList<>();
		List<String> packageList = new ArrayList<>();
		for (TestPackage obj : data) {
			packageList.add("'" + obj.getNtestpackagecode() + "'");
		}
		String spackageid = String.join(", ", packageList);
		System.out.println(spackageid);
	    
	    if (data.size() != 0) {
	        int count = packageList.size();
	        String packageCount = count + " Package data not merged";
	        String string = "update invoiceexportimport set sdatastatus='(" + packageCount + ")' where ncode=5";
	        jdbcTemplate.execute(string);
	    } else {
	        String query = "update invoiceexportimport set sdatastatus='(Merged)' where ncode=5";
	        jdbcTemplate.execute(query);
	    }
	    
	    return packagename;
	}

	public List<TestPackage> getPackageDetails() {
	    String queryString = "SELECT DISTINCT tp.ntestpackagecode, tp.stestpackagename, tp.ntestpackageprice " +
	                        "FROM testpackage tp " +
	                        "WHERE tp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	                        " AND NOT EXISTS (SELECT * FROM invoiceproductmaster pm " +
	                        "WHERE pm.sproductname = tp.stestpackagename " +
	                        "AND pm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " )";

	    List<TestPackage> packageDetails = jdbcTemplate.query(queryString, new TestPackage());
	    return packageDetails;
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

	@Override
	public ResponseEntity<Object> mergetestmaster(InvoiceExportImport objInvoiceExportImport, UserInfo userInfo)
	        throws Exception {
	    final List<String> multilingualIDList = new ArrayList<>();
	    final List<Object> savedMergeCustomerMasterList = new ArrayList<>();
	    List<Integer> identicaldatatestdata = (List<Integer>) getTestData();
	    List<TestMaster> testList = new ArrayList<>();
	    List<Integer> s = identicaldatatestdata;
	    String result = s.toString().replace("[", "").replace("]", "");
	    String strInsert = "";

	    if (identicaldatatestdata.size() != 0) {

	        String str = "SELECT DISTINCT tm.stestname, tm.ntestcode, tm.ncost, m.smethodname "
	                + "FROM testmaster tm "
	                + "LEFT JOIN testmethod tgt ON tm.ntestcode = tgt.ntestcode "
	                + "AND tgt.ndefaultstatus = 3 AND tgt.nstatus = 1 "
	                + "LEFT JOIN method m ON tgt.nmethodcode = m.nmethodcode "
	                + "WHERE tm.ntestcode IN (" + result + ", -1) "
	                + "AND tm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	        testList = (List<TestMaster>) jdbcTemplate.query(str, new TestMaster());

	        String sequencenoquery = "select nsequenceno from seqnoinvoice where stablename ='invoiceproductmaster'";
	        int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
	        int sequenceno = identicaldatatestdata.size() + nsequenceno;

	        for (TestMaster objname : testList) {
	            nsequenceno++;

	            // ✅ Escape single quotes in sproductname (stestname)
	            String testName = objname.getStestname();
	            testName = testName == null ? "" : testName.replace("'", "''");

	            String methodName = objname.getSmethodname();
	            methodName = methodName == null ? "" : methodName.replace("'", "''");

	            strInsert = strInsert + "(" + nsequenceno + ",1,'" + testName + "','NA','NA','NA','"
	                    + methodName + "',3," + objname.getNcost() + "," + null + "," + objname.getNtestcode() + ","
	                    + userInfo.getNusercode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
	                    + userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
	                    + "),";
	        }

	        String productInsert = "insert into invoiceproductmaster(nproductcode,ntypecode,sproductname,sdescription,sinvoicedescription,saddtext1,saddtext2,"
	                + "ntaxavailable,ncost,jsondata,nlimsproduct,nusercode,dmodifieddate,nsitecode,nstatus)values"
	                + strInsert.substring(0, strInsert.length() - 1) + ";";
	        jdbcTemplate.execute(productInsert);

	        String updatequery = "update seqnoinvoice set nsequenceno =" + sequenceno
	                + " where stablename='invoiceproductmaster'";
	        jdbcTemplate.execute(updatequery);
	    }

	    savedMergeCustomerMasterList.add(objInvoiceExportImport);
	    multilingualIDList.add("IDS_MERGECUSTOMERMASTER");
	    auditUtilityFunction.fnInsertAuditAction(savedMergeCustomerMasterList, 1, null, multilingualIDList, userInfo);
	    return getInvoiceExportImport(userInfo);
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
	public List<Integer> getTestData() {

		String queryString = "select DISTINCT tm.ntestcode,tm.ncost,tm.stestname from testmaster tm where "
				+ "tm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and not exists (select * from invoiceproductmaster pm where pm.sproductname = tm.stestname and pm.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

		List<TestMaster> identicaldata = (List<TestMaster>) jdbcTemplate.query(queryString, new TestMaster());
		List<Integer> identicaldatatestdata = identicaldata.stream().map(TestMaster::getNtestcode)
				.collect(Collectors.toList());
		return identicaldatatestdata;
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
	public List<TestMaster> getTestMasterDataStatus() {
		List<TestMaster> data = (List<TestMaster>) getTestDetails();
		List<TestMaster> testname = new ArrayList<>();
		List<String> testList = new ArrayList<>();
		for (TestMaster obj : data) {
			testList.add("'" + obj.getNtestcode() + "'");
		}
		String stestid = String.join(", ", testList);
		System.out.println(stestid);

		if (data.size() != 0) {
			int count = testList.size();
			String testCount = count + " Test data not merged";
			String string = "update invoiceexportimport set sdatastatus='(" + testCount + ")' where ncode=4";
			jdbcTemplate.execute(string);
		} else {
			String query = "update invoiceexportimport set sdatastatus='(Merged)' where ncode=4";
			jdbcTemplate.execute(query);
		}
		return testname;
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
	public List<TestMaster> getTestDetails() {

		String queryString = "select DISTINCT tm.ntestcode,tm.ncost,tm.stestname from testmaster tm where "
				+ "tm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and not exists (select * from invoiceproductmaster pm where pm.sproductname = tm.stestname and pm.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

		List<TestMaster> testDetails = (List<TestMaster>) jdbcTemplate.query(queryString, new TestMaster());
		return testDetails;
	}
	
	
	@SuppressWarnings("unchecked")

	@Override

	public ResponseEntity<Object> exportpackagemaster(Map<String, Object> objmap, UserInfo userInfo) throws Exception {
 
		Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
 
		List<InvoiceProductMaster> lstdata = (List<InvoiceProductMaster>) getexportdatas();
 
		Object object = lstdata.get(0);

		final String sFileName = "InvoicePackageMaster";

		final String OutputFileName = sFileName + ".csv";

		final String homePath = ftpUtilityFunction.getFileAbsolutePath();

		String target = System.getenv(homePath);

		target = target.trim();

		final String TargetPath = target + Enumeration.FTP.INVOICE_EXPORT.getFTP() + OutputFileName;

		File file = new File(TargetPath);

		FileWriter outputfile = new FileWriter(file);
 
		CSVWriter writer = new CSVWriter(outputfile);

		String[] header = { "Product Type", "LimsCode", "Product Name", "Description", "Invoice Description",

				"Add Text1", "Add Text2", "TaxAvailable", "Cost" };
 
		writer.writeNext(header);
 
		for (InvoiceProductMaster objcol2 : lstdata) {
 
			final String strQuery = "select i.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"

					+ userInfo.getSlanguagetypecode() + "',"

					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "

					+ " from invoiceproducttype i,transactionstatus ts " + " where i.nstatus = "

					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "

					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.ntranscode = i.nactive"

					+ " and i.ntypecode in  (" + objcol2.getNtypecode() + ")";

			final List<InvoiceProductType> obj = (List<InvoiceProductType>) jdbcTemplate.query(strQuery,

					new InvoiceProductType());

			objcol2.setStypename(obj.get(0).getStypename());

			String TypeName = objcol2.getStypename();

			String LimsCode = objcol2.getSlimscode();

			String ProductName = objcol2.getSproductname();

			String Description = objcol2.getSdescription();

			String InvoiceDescription = objcol2.getSinvoicedescription();

			String AddText1 = objcol2.getSaddtext1();

			String AddText2 = objcol2.getSaddtext2();

			int TaxAvailable = objcol2.getNtaxavailable();

			Double Cost = objcol2.getNcost();

			Double Tax = objcol2.getNtax();

			String TaxName = objcol2.getStaxname();

			String Versionno = objcol2.getSversionno();

			String str1 = Double.toString(Tax);

			String str2 = Double.toString(Cost);

			String TaxA = String.valueOf(TaxAvailable);

			short Active = objcol2.getNactive();

			String str = String.valueOf(Active);

			String[] data1 = { TypeName, LimsCode, ProductName, Description, InvoiceDescription, AddText1, AddText2,

					TaxA, str2 };

			writer.writeNext(data1);

		}

		writer.flush();

		writer.close();

		String sDownloadPathName = getdownloadpathname();

		if (sDownloadPathName != null) {

			outputMap.put("ExportExcelPath", sDownloadPathName + "InvoicePackageMaster" + ".csv");

			outputMap.put("ExportExcel", "Success");

		} else {

			outputMap.put("rtn", Enumeration.ReturnStatus.FAILED.getreturnstatus().trim().toString());

		}

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

	}
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> importpackagemaster(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> reg = objectMapper.readValue(request.getParameter("Map"), Map.class);
		
		Map<String, Object> selectedRecord = (Map<String, Object>) reg.get("selectedRecord");
		
		// Add null checks for selectedRecord and filenames
		if (selectedRecord == null) {
			return new ResponseEntity<>("Selected record data is missing", HttpStatus.BAD_REQUEST);
		}
		
		List<Map<String, String>> filenames = (List<Map<String, String>>) selectedRecord.get("sfilename");
		
		// Check if filenames is null or empty
		if (filenames == null || filenames.isEmpty()) {
			return new ResponseEntity<>("File information is missing", HttpStatus.BAD_REQUEST);
		}
		
		// Check if the first filename entry has path
		Map<String, String> firstFile = filenames.get(0);
		if (firstFile == null || firstFile.get("path") == null) {
			return new ResponseEntity<>("File path is missing", HttpStatus.BAD_REQUEST);
		}
		
		String path = firstFile.get("path");
		
		// Check if path is valid
		if (path == null || path.trim().isEmpty()) {
			return new ResponseEntity<>("Invalid file path", HttpStatus.BAD_REQUEST);
		}
		
		// Validate file name pattern
		if (path.matches("InvoicePackageMaster \\(\\d+\\)\\.csv") || path.matches("InvoicePackageMaster\\.csv")) {
			MultipartFile objmultipart = request.getFile("InvoiceImportFile");
			if (objmultipart == null) {
				String Std = "Select .CSV file to import";
				return new ResponseEntity<>(Std, HttpStatus.CONFLICT);
			}
			
			// Check if the uploaded file is empty
			if (objmultipart.isEmpty()) {
				return new ResponseEntity<>("Uploaded file is empty", HttpStatus.BAD_REQUEST);
			}
			
			InputStream objinputstream = objmultipart.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len;
			while ((len = objinputstream.read(buffer)) > -1) {
				baos.write(buffer, 0, len);
			}
			baos.flush();
			InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(objmultipart.getInputStream(), "UTF-8"));
			
			String[] headers = null;
			String[] values;

			String newHeader0 = "ntypecode";
			String newHeader1 = "slimscode";
			String newHeader2 = "sproductname";
			String newHeader3 = "sdescription";
			String newHeader4 = "sinvoicedescription";
			String newHeader5 = "saddtext1";
			String newHeader6 = "saddtext2";
			String newHeader7 = "ntaxavailable";
			String newHeader8 = "ncost";

			String st2 = "select * from invoiceproductmaster where nstatus=1";
			final List<InvoiceProductMaster> obj1 = (List<InvoiceProductMaster>) jdbcTemplate.query(st2,
					new InvoiceProductMaster());
			List<String> objname = obj1.stream().map(InvoiceProductMaster::getSproductname).collect(Collectors.toList());

			int lineNumber = 0;
			while ((line = br.readLine()) != null) {
				lineNumber++;
				values = line.split(",");

				// Skip empty lines
				if (line.trim().isEmpty()) {
					continue;
				}

				if (headers == null) {
					headers = values;
					continue; // Skip header row for data processing
				}
				
				// Check if values array has enough elements
				if (values.length < 9) {
					// Log or handle insufficient data in CSV row
					continue; // Skip this row or handle appropriately
				}

				String customernme = values[2];
				String nmes = customernme.replaceAll("\"", "");

				if (!objname.contains(nmes)) {
					String query = "INSERT INTO invoiceproductmaster (";
					query += "nproductcode";
					query += ",";
					for (int i = 0; i < headers.length; i++) {
						if (i <= 8) {
							if (i == 0) {
								headers[0] = newHeader0;
							}
							if (i == 1) {
								headers[1] = newHeader1;
							}
							if (i == 2) {
								headers[2] = newHeader2;
							}
							if (i == 3) {
								headers[3] = newHeader3;
							}
							if (i == 4) {
								headers[4] = newHeader4;
							}
							if (i == 5) {
								headers[5] = newHeader5;
							}
							if (i == 6) {
								headers[6] = newHeader6;
							}
							if (i == 7) {
								headers[7] = newHeader7;
							}
							if (i == 8) {
								headers[8] = newHeader8;
							}
							query += headers[i];
							if (i <= 8 - 1) {
								query += ",";
							}
						}
					}
					final String getSequenceNo = "select nsequenceno from seqnoinvoice where stablename ='invoiceproductmaster'";
					int seqNo = jdbcTemplate.queryForObject(getSequenceNo, Integer.class);
					seqNo++;
					query += ",nusercode , dmodifieddate,nsitecode,nstatus";
					query += ") VALUES (";

					query += seqNo;
					query += ",";

					for (int i = 0; i < values.length; i++) {
						if (i > 8) break; // Only process first 9 columns

						if (i == 0) {
							String y = values[0];
							String stringWithoutQuotes = y.replaceAll("\"", "");
							final String strQuery = "select i.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
									+ userInfo.getSlanguagetypecode() + "',"
									+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
									+ " from invoiceproducttype i,transactionstatus ts " + " where i.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ts.ntranscode = i.nactive" + " and i.stypename in ('" + stringWithoutQuotes
									+ "') ";
							final List<InvoiceProductType> obj = (List<InvoiceProductType>) jdbcTemplate.query(strQuery,
									new InvoiceProductType());
							
							// Check if product type exists
							if (obj == null || obj.isEmpty()) {
								// Handle case where product type doesn't exist
								continue; // Skip this row or set default value
							}
							
							int q = obj.get(0).getNtypecode();
							query += q;
						}

						if (i == 1) {
							String x = values[1];
							String tax = x.replaceAll("\"", "");
							query += "'" + tax + "'";
						}
						if (i == 2) {
							String x = values[2];
							String tax = x.replaceAll("\"", "");
							query += "'" + tax + "'";
						}
						if (i == 3) {
							String x = values[3];
							String tax = x.replaceAll("\"", "");
							query += "'" + tax + "'";
						}
						if (i == 4) {
							String x = values[4];
							String sdescription = x.replaceAll("\"", "");
							query += "'" + sdescription + "'";
						}
						if (i == 5) {
							String x = values[5];
							String tax = x.replaceAll("\"", "");
							query += "'" + tax + "'";
						}
						if (i == 6) {
							String x = values[6];
							String tax = x.replaceAll("\"", "");
							query += "'" + tax + "'";
						}
						if (i == 7) {
							String x = values[7];
							String tax = x.replaceAll("\"", "");
							query += tax;
						}
						if (i == 8) {
							String z = values[8];
							String cost = z.replaceAll("\"", "");
							query += cost;
						}
						if (i <= 8 - 1) {
							query += ",";
						}
					}
					query += ",-1,'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ",1";
					query += ")";
					jdbcTemplate.execute("UPDATE seqnoinvoice SET nsequenceno = " + seqNo
						+ " WHERE stablename = 'invoiceproductmaster'");
				} else {
					// Check if values array has enough elements for update
					if (values.length >= 9) {
						String x = values[3];
						String tax = x.replaceAll("\"", "");
						String des = values[4];
						String sdescription = des.replaceAll("\"", "");
						String add1 = values[5];
						String addtext1 = add1.replaceAll("\"", "");
						String add2 = values[6];
						String addtext2 = add2.replaceAll("\"", "");
						String z = values[8];
						String cost = z.replaceAll("\"", "");

						String updateSql = "UPDATE invoiceproductmaster SET sdescription = ?, sinvoicedescription = ?, saddtext1 = ?, saddtext2 = ?, ncost = ? WHERE sproductname = ?";
						
						// Robust number parsing that handles scientific notation, decimals, and regular integers
						Number costValue;
						try {
							if (cost.contains("E") || cost.contains("e") || cost.contains(".")) {
								costValue = Double.parseDouble(cost);
							} else {
								costValue = Long.parseLong(cost); // Use Long to handle larger numbers
							}
						} catch (NumberFormatException e) {
							// Fallback to 0 if parsing fails
							costValue = 0;
						}
						
						jdbcTemplate.update(updateSql, tax, sdescription, addtext1, addtext2, costValue, nmes);
					}
				}
			}
			br.close();
		} else {
			return new ResponseEntity<>(
				commonFunction.getMultilingualMessage("IDS_PACKAGEMASTERFILEALERT", userInfo.getSlanguagefilename()),
				HttpStatus.EXPECTATION_FAILED);
		}
		
		return getProductMaster(userInfo);
	}
}
