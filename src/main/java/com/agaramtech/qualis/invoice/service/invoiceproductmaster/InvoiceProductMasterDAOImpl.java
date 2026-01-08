package com.agaramtech.qualis.invoice.service.invoiceproductmaster;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.configuration.model.TreeVersionTemplate;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.invoice.model.FieldMaster;
import com.agaramtech.qualis.invoice.model.InvoiceProductFile;
import com.agaramtech.qualis.invoice.model.InvoiceProductMaster;
import com.agaramtech.qualis.invoice.model.InvoiceProductType;
import com.agaramtech.qualis.invoice.model.InvoiceTaxtype;
import com.agaramtech.qualis.invoice.model.InvoiceVersionNo;
import com.agaramtech.qualis.invoice.model.ProductTest;
import com.agaramtech.qualis.invoice.model.TaxProductDetails;
import com.agaramtech.qualis.invoice.model.UserRoleFieldControl;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.product.model.ProductCategory;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.agaramtech.qualis.testgroup.model.TreeTemplateManipulation;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.global.AuditUtilityFunction;

import com.agaramtech.qualis.global.DateTimeUtilityFunction;

import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.StringUtilityFunction;

import lombok.AllArgsConstructor;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'InvoiceProductmaster' table
 * 
 * @author ATE237
 * @version 11.0.0.2
 * @since 05- 09- 2025
 */
@Transactional(rollbackFor = Exception.class)
@AllArgsConstructor
@Repository
public class InvoiceProductMasterDAOImpl implements InvoiceProducMastertDAO {

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction fTPUtilityFunction;

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getProductMaster(UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub

		String str = "select ipm.nproductcode,ipm.ntypecode,ipm.slimscode,ipm.sproductname,ipm.sdescription,ipm.sinvoicedescription,"
				+ " ipm.saddtext1,ipm.saddtext2,ipm.ncost,ipt.stypename,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,ipm.ntaxavailable  from invoiceproductmaster ipm,invoiceproducttype ipt,transactionstatus ts where "
				+ " ipt.ntypecode=ipm.ntypecode and ipm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ipt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.ntranscode=ipm.ntaxavailable"
				+ " and ipm.nsitecode="+userInfo.getNmastersitecode()+" and ipt.nsitecode="+userInfo.getNmastersitecode()
				+ " and ipm.nproductcode <>-1 order by ipm.nproductcode desc";

		return new ResponseEntity<Object>(jdbcTemplate.query(str, new InvoiceProductMaster()), HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to InvoiceProductmaster table. Need to
	 * check for duplicate entry of InvoiceProductmaster name for the specified site
	 * before saving into database. Need to check that there should be only one
	 * default InvoiceProductmaster for a site
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] object holding details
	 *                                to be added in InvoiceProductmaster table
	 * @return inserted InvoiceProductmaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createInvoiceProductMaster(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final String sQuery = " lock  table invoiceproductmaster "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedProductList = new ArrayList<>();
		final ObjectMapper objmapper = new ObjectMapper();
		final InvoiceProductMaster invoiceProductmaster = objmapper.convertValue(inputMap.get("invoiceproduct"),
				InvoiceProductMaster.class);

		final InvoiceProductMaster objproduct = getProductListByName(invoiceProductmaster.getSproductname(),
				invoiceProductmaster.getNsitecode());
		if (objproduct == null) {

			String sequencenoquery = "select nsequenceno from seqnoinvoice where stablename ='invoiceproductmaster'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;
			String insertquery = "Insert into invoiceproductmaster (nproductcode,ntypecode,slimscode,sproductname,sdescription,sinvoicedescription,saddtext1,saddtext2,"
					+ "ntaxavailable,ncost,nusercode,dmodifieddate,nsitecode,nstatus) " + " values(" + nsequenceno + ","
					+ invoiceProductmaster.getNtypecode() + ",N'"
					+ stringUtilityFunction.replaceQuote(invoiceProductmaster.getSlimscode()) + "',N'"
					+ stringUtilityFunction.replaceQuote(invoiceProductmaster.getSproductname()) + "','"
					+ stringUtilityFunction.replaceQuote(invoiceProductmaster.getSdescription()) + "',N'"
					+ stringUtilityFunction.replaceQuote(invoiceProductmaster.getSinvoicedescription()) + "'," + "N'"
					+ stringUtilityFunction.replaceQuote(invoiceProductmaster.getSaddtext1()) + "',N'"
					+ stringUtilityFunction.replaceQuote(invoiceProductmaster.getSaddtext2()) + "'," + ""
					+ invoiceProductmaster.getNtaxavailable() + "," + invoiceProductmaster.getNcost() + ","
					+ userInfo.getNusercode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ")";

			jdbcTemplate.execute(insertquery);

			String updatequery = "update seqnoinvoice set nsequenceno =" + nsequenceno
					+ " where stablename='invoiceproductmaster'";
			jdbcTemplate.execute(updatequery);

			invoiceProductmaster.setNproductcode(nsequenceno);
			savedProductList.add(invoiceProductmaster);

			multilingualIDList.add("IDS_ADDPRODUCTMASTER");

			auditUtilityFunction.fnInsertAuditAction(savedProductList, 1, null, multilingualIDList, userInfo);

			String str = "select nproductcode from invoiceproductmaster where sproductname='"
					+ stringUtilityFunction.replaceQuote(invoiceProductmaster.getSproductname()) + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and nsitecode="+userInfo.getNmastersitecode();
			int nproductcode = jdbcTemplate.queryForObject(str, Integer.class);
			return getInvoiceByProduct(nproductcode, userInfo);

		} else {
			// Conflict = 409 - Duplicate entry --getSlanguagetypecode
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	private InvoiceProductMaster getProductListByName(final String productname, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select sproductname from invoiceproductmaster where sproductname = N'"
				+ stringUtilityFunction.replaceQuote(productname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;// users.getnmastersitecode();

		return (InvoiceProductMaster) jdbcUtilityFunction.queryForObject(strQuery, InvoiceProductMaster.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
//	@SuppressWarnings({ "unchecked", "unused" })
//	@Override
//	public ResponseEntity<Object> getSampleTypeData(UserInfo userinfo) throws Exception {
//		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
//		final List<InvoiceProductType> lstProductType = (List<InvoiceProductType>) getinvoiceProductType(userinfo).getBody();
//		outputMap.put("filterProductType", lstProductType);
//
//		String GetProduct = "select distinct pc.nproductcatcode,pc.sproductcatname,pc.sdescription,pc.ncategorybasedflow,pc.ndefaultstatus,pc.nsitecode,pc.nstatus"
//				+ " from productcategory pc, treeversiontemplate tvt, treetemplatemaster ttm "
//				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ttm.ncategorycode = pc.nproductcatcode"
//				+ " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and nproductcatcode > 0" 
//				+ " and tvt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and tvt.nsitecode = "+ userinfo.getNsitecode() 
//				+ " and ttm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and ttm.nsitecode = "+ userinfo.getNsitecode() 
//				+ " and pc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and pc.nsitecode = "+ userinfo.getNsitecode() 
//				+ " and tvt.ntransactionstatus <> "+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
//				+ " and nformcode = 24 and ncategorybasedflow=4 ";
//
//		List<Product> ProductTypeDataList = (List<Product>) jdbcTemplate.query(GetProduct, new Product());
//		outputMap.put("ProductTypeData", ProductTypeDataList);
//		if (ProductTypeDataList.size() != 0) {
//
//			String getProductItems = "select * from product where nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" nsitecode="+userinfo.getNmastersitecode()
//					+ " and nproductcatcode ="+ ProductTypeDataList.get(0).getNproductcatcode() + " and nproductcatcode > -1 "
//					+ "order by nproductcode";
//			List<Product> ProductItems = (List<Product>) jdbcTemplate.query(getProductItems, new Product());
//			outputMap.put("ProductList", ProductItems);
//
//			String getTreeTemplates = "select * from treetemplatemanipulation where nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userinfo.getNmastersitecode()
//					+" and nproductcatcode ="+ ProductTypeDataList.get(0).getNproductcatcode() + " and nproductcatcode > -1"
//					+ " order by nproductcode";
//			List<TreeTemplateManipulation> TreeItems = (List<TreeTemplateManipulation>) jdbcTemplate
//					.query(getTreeTemplates, new TreeTemplateManipulation());
//
//			@SuppressWarnings("unused")
//			String previousSlevelDescription = "";
//
//			StringBuilder concatenatedValue = new StringBuilder();
//
//			String lastLine = ""; // Track last concatenated line
//			String lastValue = ""; // Track the last value
//
//			// Declare variables to track words
//			String lastAddedWord = "";
//			String secondLastAddedWord = ""; // Store second last word
//			int lastAddedIndex = -1;
//			int secondLastAddedIndex = -1;
//
//			for (TreeTemplateManipulation item : TreeItems) {
//				System.out.println("Processing item: " + item);
//
//				String currentWord = item.getSleveldescription(); // Store the current word
//				// int lastAddedIndex = -1;
//
//				// Append only if schildnode is not empty
//				if (item.getSchildnode() != null && !item.getSchildnode().isEmpty()) {
//					lastAddedWord = currentWord; // Update last added word
//					lastAddedIndex = concatenatedValue.length();
//					concatenatedValue.append(lastAddedWord).append(", ");
//				}
//
//				else {
//					lastLine = currentWord;
//					String str = "SELECT * FROM TreeTemplateManipulation WHERE sleveldescription='" + lastLine + "'"
//							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userinfo.getNmastersitecode();
//					List<TreeTemplateManipulation> Treeitems = (List<TreeTemplateManipulation>) jdbcTemplate
//							.query(str, new TreeTemplateManipulation());
//
//					String set = "SELECT * FROM testgroupspecification " + "WHERE ntemplatemanipulationcode = "
//							+ Treeitems.get(0).getNtemplatemanipulationcode() + " AND napprovalstatus ="
//							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() +" and nsitecode="+userinfo.getNmastersitecode() 
//							+ " and ntransactionstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//
//					List<TestGroupSpecification> TreeItem = (List<TestGroupSpecification>) jdbcTemplate.query(set,
//							new TestGroupSpecification());
//
//					// ✅ Check if `TreeItemsd` is NOT empty and approval status == 31
//					if (!TreeItem.isEmpty()) {
//						concatenatedValue.append(currentWord).append("\n");
//					}
//
//					else {
//						// ❌ Remove SECOND LAST added word (not the latest one)
//						String toRemove = lastAddedWord + ", ";
//						int lastIndex = concatenatedValue.lastIndexOf(toRemove);
//
//						if (lastIndex != -1) {
//							concatenatedValue.delete(lastIndex, lastIndex + toRemove.length());
//						}
//						// Handle case where the second last word is last without ", "
//						else {
//							toRemove = lastAddedWord;
//							lastIndex = concatenatedValue.lastIndexOf(toRemove);
//							if (lastIndex != -1) {
//								concatenatedValue.delete(lastIndex, lastIndex + toRemove.length());
//							}
//						}
//
//						// ✅ Remove extra trailing comma or newline if needed
//						if (concatenatedValue.length() > 0) {
//							char lastChar = concatenatedValue.charAt(concatenatedValue.length() - 1);
//							if (lastChar == ',' || lastChar == ' ' || lastChar == '\n') {
//								concatenatedValue.deleteCharAt(concatenatedValue.length() - 1);
//								concatenatedValue.append("\n");
//							}
//						}
//					}
//				}
//			}
//
//			// Store final concatenated value
//			outputMap.put("sleveldescription", concatenatedValue.toString());
//			for (TreeTemplateManipulation treeItem : TreeItems) {
//				treeItem.setSleveldescription(concatenatedValue.toString());
//			}
//
//			outputMap.put("TreeListByProduct", TreeItems);
//
//			String getTreetemplates = "select * from treetemplatemanipulation where nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userinfo.getNmastersitecode()
//					+ " and nproductcatcode ="+ ProductTypeDataList.get(0).getNproductcatcode() + " and nproductcatcode > -1"
//					+ " and (schildnode IS NULL OR TRIM(schildnode) = '')" + " order by nproductcode";
//			List<TreeTemplateManipulation> TreeItemses = (List<TreeTemplateManipulation>) jdbcTemplate
//					.query(getTreetemplates, new TreeTemplateManipulation());
//			String queryformat = "TO_CHAR(dexpirydate,'" + userinfo.getSpgsitedatetime() + "') ";
//
//			String sQuery = "select tgs.noffsetdexpirydate,tgs.nallottedspeccode,tgs.ntemplatemanipulationcode,tgs.napproveconfversioncode,tgs.sspecname,"
//					+ "CASE WHEN tgs.sversion='' THEN  '-' ELSE tgs.sversion END sversion, sversion," + queryformat
//					+ " as sexpirydate,tgs.napprovalstatus,tgs.ntransactionstatus,coalesce(ts2.jsondata->'stransdisplaystatus'->>'"
//					+ userinfo.getSlanguagetypecode() + "',"
//					+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,tgs.ntzexpirydate,"
//					+ "coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode() + "',"
//					+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') as sapprovalstatus, tz.stimezoneid, tgs.ncomponentrequired, coalesce(ts3.jsondata->'stransdisplaystatus'->>'"
//					+ userinfo.getSlanguagetypecode() + "',"
//					+ " ts3.jsondata->'stransdisplaystatus'->>'en-US') as scomponentrequired, cm.scolorhexcode "
//					+ " from testgroupspecification tgs, transactionstatus ts1, transactionstatus ts2, transactionstatus ts3, timezone tz, "
//					+ "formwisestatuscolor fwc, colormaster cm"
//					+ " where tgs.napprovalstatus=ts1.ntranscode and tgs.ntransactionstatus=ts2.ntranscode and tgs.ncomponentrequired = ts3.ntranscode "
//					+ " and tz.ntimezonecode = tgs.ntzexpirydate and ts1.nstatus = tgs.nstatus and ts2.nstatus = tgs.nstatus and tz.nstatus = tgs.nstatus"
//					+ " and fwc.ntranscode = tgs.napprovalstatus and cm.ncolorcode = fwc.ncolorcode and fwc.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fwc.nsitecode="+userinfo.getNmastersitecode()
//					+ " and cm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
//					+ " and ts3.nstatus = tgs.nstatus and tgs.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgs.nsitecode="+userinfo.getNmastersitecode()+" and fwc.nformcode = "
//					+ Enumeration.FormCode.TESTGROUP.getFormCode() + " and tgs.ntemplatemanipulationcode="
//					+ TreeItemses.get(0).getNtemplatemanipulationcode() + " order by tgs.nallottedspeccode;";
//
//			List<TestGroupSpecification> lstTestGroupSpecification = (List<TestGroupSpecification>) jdbcTemplate
//					.query(sQuery, new TestGroupSpecification());
//
//			List<?> listSpec = dateUtilityFunction.getSiteLocalTimeFromUTC(lstTestGroupSpecification,
//					Arrays.asList("sexpirydate"), Arrays.asList(userinfo.getStimezoneid()), userinfo, true,
//					Arrays.asList("stransdisplaystatus", "sapprovalstatus", "scomponentrequired"), false);
//
//			outputMap.put("SpecificationByRootlst", listSpec);
//		}
//		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
//
//	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getSampleTypeData(UserInfo userinfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final List<InvoiceProductType> lstProductType = (List<InvoiceProductType>) getinvoiceProductType( userinfo).getBody();
		outputMap.put("filterProductType", lstProductType);

		String GetProduct = "select distinct pc.nproductcatcode,pc.sproductcatname,pc.sdescription,pc.ncategorybasedflow,pc.ndefaultstatus,pc.nsitecode,pc.nstatus"
				+ " from productcategory pc, treeversiontemplate tvt, treetemplatemaster ttm "
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ttm.ncategorycode = pc.nproductcatcode "
				+ " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nproductcatcode > 0 " + " and tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pc.nsitecode = " + -1
				+ " and tvt.ntransactionstatus <> " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ " and nformcode = 24 and ncategorybasedflow=4 ";

		List<Product> ProductTypeDataList = (List<Product>) jdbcTemplate.query(GetProduct, new Product());
		outputMap.put("ProductTypeData", ProductTypeDataList);
		if (ProductTypeDataList.size()!=0){
		String getProductItems = "select * from product where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nproductcatcode = "
				+ ProductTypeDataList.get(0).getNproductcatcode() + " and nproductcatcode > -1 "
				// + "and nproductcode =" +ProductTypeDataList.get(0).getNproductcode()
				+ "order by nproductcode";
		List<Product> ProductItems = (List<Product>) jdbcTemplate.query(getProductItems, new Product());
		outputMap.put("ProductList", ProductItems);
		String getTreeTemplates = "select * from treetemplatemanipulation where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nproductcatcode = "
				+ ProductTypeDataList.get(0).getNproductcatcode() + " and nproductcatcode > -1 "

				+ " order by nproductcode";
		List<TreeTemplateManipulation> TreeItems = (List<TreeTemplateManipulation>) jdbcTemplate.query(getTreeTemplates,
				new TreeTemplateManipulation());

		String previousSlevelDescription = "";

		StringBuilder concatenatedValue = new StringBuilder();

		
		  String lastLine = "";  // Track last concatenated line
		  String lastValue = ""; // Track the last value

		// Declare variables to track words
		  String lastAddedWord = "";
		  String secondLastAddedWord = "";  // Store second last word
		  int lastAddedIndex = -1;  
		  int secondLastAddedIndex = -1;  

		  for (TreeTemplateManipulation item : TreeItems) {
			    System.out.println("Processing item: " + item);

			    String currentWord = item.getSleveldescription(); // Store the current word
			    //int lastAddedIndex = -1;

			    // Append only if schildnode is not empty
			    if (item.getSchildnode() != null && !item.getSchildnode().isEmpty()) {
			        lastAddedWord = currentWord; // Update last added word
			        lastAddedIndex = concatenatedValue.length();
			        concatenatedValue.append(lastAddedWord).append(", ");
			    } 
			    
			    else {
			        lastLine = currentWord;
			        String safeLine = lastLine.replace("'", "''");
			        String str = "SELECT * FROM TreeTemplateManipulation WHERE sleveldescription='" + safeLine + "'";
			        List<TreeTemplateManipulation> TreeItems21 = (List<TreeTemplateManipulation>) jdbcTemplate.query(str,
			                new TreeTemplateManipulation());


			        String set = "SELECT * FROM testgroupspecification " +
		                     "WHERE ntemplatemanipulationcode = " + TreeItems21.get(0).getNtemplatemanipulationcode() + 
		                     " AND napprovalstatus = 31 " +
		                     " AND ntransactionstatus = 1";

		        List<TestGroupSpecification> TreeItems212 = (List<TestGroupSpecification>) jdbcTemplate.query(set,
		                new TestGroupSpecification());

			        // ✅ Check if `TreeItems212` is NOT empty and approval status == 31
		        if (!TreeItems212.isEmpty()) {
			            concatenatedValue.append(currentWord).append("\n");
			        } 
			        
			        else {
			            // ❌ Remove SECOND LAST added word (not the latest one)
			            String toRemove = lastAddedWord + ", ";
			            int lastIndex = concatenatedValue.lastIndexOf(toRemove);

			            if (lastIndex != -1) {
			                concatenatedValue.delete(lastIndex, lastIndex + toRemove.length());
			            } 
			            // Handle case where the second last word is last without ", "
			            else {
			                toRemove = lastAddedWord;
			                lastIndex = concatenatedValue.lastIndexOf(toRemove);
			                if (lastIndex != -1) {
			                    concatenatedValue.delete(lastIndex, lastIndex + toRemove.length());
			                }
			            }

			            // ✅ Remove extra trailing comma or newline if needed
			            if (concatenatedValue.length() > 0) {
			                char lastChar = concatenatedValue.charAt(concatenatedValue.length() - 1);
			                if (lastChar == ',' || lastChar == ' ' || lastChar == '\n') {
			                    concatenatedValue.deleteCharAt(concatenatedValue.length() - 1);
			                    concatenatedValue.append("\n");
			                }
			            }
			        }
			    }
			}

			// Store final concatenated value
			outputMap.put("sleveldescription", concatenatedValue.toString());
		for (TreeTemplateManipulation treeItem : TreeItems) {
			treeItem.setSleveldescription(concatenatedValue.toString());
		}

		outputMap.put("TreeListByProduct", TreeItems);

		String getTreeTemplates1 = "select * from treetemplatemanipulation where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nproductcatcode = "
				+ ProductTypeDataList.get(0).getNproductcatcode() + " and nproductcatcode > -1 "
				+ " and (schildnode IS NULL OR TRIM(schildnode) = '')" + " order by nproductcode ";
		List<TreeTemplateManipulation> TreeItems1 = (List<TreeTemplateManipulation>) jdbcTemplate
				.query(getTreeTemplates1, new TreeTemplateManipulation());
		String queryformat = "TO_CHAR(dexpirydate,'" + userinfo.getSpgsitedatetime() + "') ";

		String sQuery = "select tgs.noffsetdexpirydate,tgs.nallottedspeccode,tgs.ntemplatemanipulationcode,tgs.napproveconfversioncode,tgs.sspecname,"
				+ "CASE WHEN tgs.sversion='' THEN  '-' ELSE tgs.sversion END sversion, sversion," + queryformat
				+ " as sexpirydate,tgs.napprovalstatus,tgs.ntransactionstatus,coalesce(ts2.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode() + "',"
				+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,tgs.ntzexpirydate,"
				+ "coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode() + "',"
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') as sapprovalstatus, tz.stimezoneid, tgs.ncomponentrequired, coalesce(ts3.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode() + "',"
				+ " ts3.jsondata->'stransdisplaystatus'->>'en-US') as scomponentrequired, cm.scolorhexcode "
				+ " from testgroupspecification tgs, transactionstatus ts1, transactionstatus ts2, transactionstatus ts3, timezone tz, "
				+ "formwisestatuscolor fwc, colormaster cm"
				+ " where tgs.napprovalstatus=ts1.ntranscode and tgs.ntransactionstatus=ts2.ntranscode and tgs.ncomponentrequired = ts3.ntranscode "
				+ " and tz.ntimezonecode = tgs.ntzexpirydate and ts1.nstatus = tgs.nstatus and ts2.nstatus = tgs.nstatus and tz.nstatus = tgs.nstatus"
				+ " and fwc.ntranscode = tgs.napprovalstatus and cm.ncolorcode = fwc.ncolorcode and fwc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts3.nstatus = tgs.nstatus and tgs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fwc.nformcode = "
				+ Enumeration.FormCode.TESTGROUP.getFormCode() + " and tgs.ntemplatemanipulationcode="
				+ TreeItems1.get(0).getNtemplatemanipulationcode() + " order by tgs.nallottedspeccode;";

		List<TestGroupSpecification> lstTestGroupSpecification = (List<TestGroupSpecification>) jdbcTemplate
				.query(sQuery, new TestGroupSpecification());

		List<?> listSpec = dateUtilityFunction.getSiteLocalTimeFromUTC(lstTestGroupSpecification,
				Arrays.asList("sexpirydate"), Arrays.asList(userinfo.getStimezoneid()), userinfo, true,
			Arrays.asList("stransdisplaystatus", "sapprovalstatus", "scomponentrequired"), false);
		
		outputMap.put("SpecificationByRootlst", listSpec);
		}
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

	}

	/**
	 * This method is used to retrieve list of all active InvoiceProductmaster for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         InvoiceProductmaster
	 * @throws Exception that are thrown from this Service layer
	 */
	@SuppressWarnings("unused")
	private static TreeTemplateManipulation findParent(int parentId, List<TreeTemplateManipulation> treeItems) {
		for (TreeTemplateManipulation item : treeItems) {
			if (item.getNparentnode() == parentId) {
				return item;
			}
		}
		return null;
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> getSpecificationByTreetemplate(UserInfo userinfo, int nproductcatcode,
			int nproductcode, int ntreetemplatemanipulationcode, int nformcode, String lastlabel) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final List<InvoiceProductType> lstProductType = (List<InvoiceProductType>) getinvoiceProductType(userinfo).getBody();
		outputMap.put("filterProductType", lstProductType);

		String ptype = "select * from invoiceproducttype where stypename='LIMS' and  nstatus= " +
				+  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "+userinfo.getNmastersitecode();
		List<InvoiceProductType> selectedProductTypeList = (List<InvoiceProductType>) jdbcTemplate.query(ptype,
				new InvoiceProductType());
		outputMap.put("SelectedProductType", selectedProductTypeList);

		String GetProduct = "select distinct pc.nproductcatcode,pc.sproductcatname,pc.sdescription,pc.ncategorybasedflow,pc.ndefaultstatus,pc.nsitecode,pc.nstatus"
				+ " from productcategory pc, treeversiontemplate tvt, treetemplatemaster ttm "
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ttm.ncategorycode = pc.nproductcatcode "
				+ " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and pc.nsitecode="+userinfo.getNmastersitecode()
				+ " and nproductcatcode > 0 " + " and tvt.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and tvt.nsitecode="+userinfo.getNmastersitecode()
				+ " and ttm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and ttm.nsitecode="+userinfo.getNmastersitecode()
			    + " and tvt.ntransactionstatus <> "	+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ " and nformcode = 24 and ncategorybasedflow=4";

		List<Product> ProductTypeDataList = (List<Product>) jdbcTemplate.query(GetProduct, new Product());
		outputMap.put("ProductTypeData", ProductTypeDataList);

		String GetProduct1 = "select distinct *"
				+ " from productcategory pc, treeversiontemplate tvt, treetemplatemaster ttm "
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ttm.ncategorycode = pc.nproductcatcode "
				+ " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and pc.nsitecode="+userinfo.getNmastersitecode()
				+ " and nproductcatcode > 0 " + " and tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode="+userinfo.getNmastersitecode()
				+ " and ttm.nstatus = "	+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and ttm.nsitecode="+userinfo.getNmastersitecode()
				+ " and tvt.ntransactionstatus <> "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ " and nformcode = 24 and pc.nproductcatcode= " + nproductcatcode + " ";

		List<Product> ProductTypeDataList1 = (List<Product>) jdbcTemplate.query(GetProduct1, new Product());
		List<SampleType> ProductTypeDataList2 = (List<SampleType>) jdbcTemplate.query(GetProduct1, new SampleType());
		List<ProductCategory> ProductTypeDataList3 = (List<ProductCategory>) jdbcTemplate.query(GetProduct1,
				new ProductCategory());
		outputMap.put("ProductTypeData", ProductTypeDataList1);

		String sQuery = "select tvt.ntreeversiontempcode,CONCAT(tvt.sversiondescription,' (',cast(nversionno as character varying(50)),')' ) as sversiondescription,"
				+ "tvt.ntransactionstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus from treetemplatemaster ttm,treeversiontemplate tvt, transactionstatus ts"
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ts.ntranscode = tvt.ntransactionstatus and ttm.nstatus = tvt.nstatus"
				+ " and ts.nstatus = tvt.nstatus and tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode="+userinfo.getNmastersitecode() +" and ttm.nsitecode = "
				+ ProductTypeDataList3.get(0).getNsitecode() + " and ttm.ncategorycode = "
				+ ProductTypeDataList3.get(0).getNproductcatcode() + " and tvt.nsampletypecode= "
				+ ProductTypeDataList2.get(0).getNsampletypecode() + " and ttm.nformcode = " + 24
				+ " and tvt.ntransactionstatus <> " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ " order by tvt.ntreeversiontempcode desc";

		List<TreeVersionTemplate> TemplateVersionlst = (List<TreeVersionTemplate>) jdbcTemplate.query(sQuery,
				new TreeVersionTemplate());
		outputMap.put("TemplateVersionList", TemplateVersionlst);

		String getselectedProducttype = "select distinct pc.nproductcatcode,pc.sproductcatname,pc.sdescription,pc.ncategorybasedflow,pc.ndefaultstatus,pc.nsitecode,pc.nstatus"
				+ " from productcategory pc, treeversiontemplate tvt, treetemplatemaster ttm "
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ttm.ncategorycode = pc.nproductcatcode "
				+ " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pc.nsitecode="+userinfo.getNmastersitecode()
				+ " and nproductcatcode = " + nproductcatcode + " and tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode="+userinfo.getNmastersitecode()+" and ttm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttm.nsitecode = "+userinfo.getNmastersitecode()
				+ " and tvt.ntransactionstatus <> "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " and nformcode = 24";
		List<Product> getselectedPdtType = (List<Product>) jdbcTemplate.query(getselectedProducttype, new Product());
		outputMap.put("selectedProductType", getselectedPdtType);

		String getProductItems = "select * from product where nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= " +userinfo.getNmastersitecode()+ " and nproductcatcode = "
				+ nproductcatcode + " and nproductcatcode > -1 order by nproductcode ";
		List<Product> ProductItems = (List<Product>) jdbcTemplate.query(getProductItems, new Product());
		outputMap.put("ProductList", ProductItems);

		String getProductItems1 = "select * from product where nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= " +userinfo.getNmastersitecode()+ " and nproductcode= " + nproductcode
				+ " and nproductcatcode = " + nproductcatcode + " and nproductcatcode > -1 order by nproductcode ";
		List<Product> ProductItems1 = (List<Product>) jdbcTemplate.query(getProductItems1, new Product());
		outputMap.put("selectedProduct", ProductItems1);

		String getTreeTemplates = "select * from treetemplatemanipulation where nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userinfo.getNmastersitecode()+ " and nproductcatcode = "
				+ nproductcatcode + " and nproductcatcode > -1 "

				+ " order by nproductcode";
		List<TreeTemplateManipulation> TreeItems = (List<TreeTemplateManipulation>) jdbcTemplate.query(getTreeTemplates,
				new TreeTemplateManipulation());
		outputMap.put("TreeListByProduct", TreeItems);

		String getTreeTemplates1 = "select * from treetemplatemanipulation where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userinfo.getNmastersitecode()+ " and nproductcode=" + nproductcode
				+ " and nproductcatcode = " + nproductcatcode + " and nproductcatcode > -1 "
				+ " and (schildnode IS NULL OR TRIM(schildnode) = '')" + " order by nproductcode";

		List<TreeTemplateManipulation> treeItems = (List<TreeTemplateManipulation>) jdbcTemplate
				.query(getTreeTemplates1, new TreeTemplateManipulation());
		outputMap.put("selectedTreeListByProduct", treeItems);

		String previousSlevelDescription = "";

		StringBuilder concatenatedValue = new StringBuilder();
		String lastLine = ""; // Track last concatenated line
		String lastValue = ""; // Track the last value

		// Declare variables to track words
		String lastAddedWord = "";
		String secondLastAddedWord = ""; // Store second last word
		int lastAddedIndex = -1;
		int secondLastAddedIndex = -1;

		for (TreeTemplateManipulation item : TreeItems) {
			System.out.println("Processing item: " + item);

			String currentWord = item.getSleveldescription(); // Store the current word
			// int lastAddedIndex = -1;

			// Append only if schildnode is not empty
			if (item.getSchildnode() != null && !item.getSchildnode().isEmpty()) {
				lastAddedWord = currentWord; // Update last added word
				lastAddedIndex = concatenatedValue.length();
				concatenatedValue.append(lastAddedWord).append(", ");
			}

			else {
				lastLine = currentWord;
				String str = "SELECT * FROM TreeTemplateManipulation WHERE sleveldescription='" + lastLine + "'" +" and nsitecode="+userinfo.getNmastersitecode()+"" ;
				List<TreeTemplateManipulation> TreeItemstree = (List<TreeTemplateManipulation>) jdbcTemplate.query(str,
						new TreeTemplateManipulation());

				String set = "SELECT * FROM testgroupspecification " + "WHERE ntemplatemanipulationcode = "
						+ TreeItemstree.get(0).getNtemplatemanipulationcode() +  " AND napprovalstatus ="
								+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
								+ " AND ntransactionstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
								+ " and nsitecode="+userinfo.getNmastersitecode();

				List<TestGroupSpecification> TreeItemst = (List<TestGroupSpecification>) jdbcTemplate.query(set,
						new TestGroupSpecification());

				// ✅ Check if `TreeItemsd` is NOT empty and approval status == 31
				if (!TreeItemst.isEmpty()) {
					concatenatedValue.append(currentWord).append("\n");
				}

				else {
					// ❌ Remove SECOND LAST added word (not the latest one)
					String toRemove = lastAddedWord + ", ";
					int lastIndex = concatenatedValue.lastIndexOf(toRemove);

					if (lastIndex != -1) {
						concatenatedValue.delete(lastIndex, lastIndex + toRemove.length());
					}
					// Handle case where the second last word is last without ", "
					else {
						toRemove = lastAddedWord;
						lastIndex = concatenatedValue.lastIndexOf(toRemove);
						if (lastIndex != -1) {
							concatenatedValue.delete(lastIndex, lastIndex + toRemove.length());
						}
					}

					// ✅ Remove extra trailing comma or newline if needed
					if (concatenatedValue.length() > 0) {
						char lastChar = concatenatedValue.charAt(concatenatedValue.length() - 1);
						if (lastChar == ',' || lastChar == ' ' || lastChar == '\n') {
							concatenatedValue.deleteCharAt(concatenatedValue.length() - 1);
							concatenatedValue.append("\n");
						}
					}
				}
			}
		}

		// Store final concatenated value
		outputMap.put("sleveldescription", concatenatedValue.toString());

		for (TreeTemplateManipulation treeItem : TreeItems) {
			treeItem.setSleveldescription(concatenatedValue.toString());
		}

		outputMap.put("TreeListByProduct", TreeItems);
		String queryformat = "TO_CHAR(dexpirydate,'" + userinfo.getSpgsitedatetime() + "') ";
		String str = "SELECT * FROM TreeTemplateManipulation WHERE sleveldescription='" + lastlabel + "'"+" and nsitecode="+userinfo.getNmastersitecode();
		List<TreeTemplateManipulation> TreeItemstm = (List<TreeTemplateManipulation>) jdbcTemplate.query(str,
				new TreeTemplateManipulation());
		String set = "SELECT * FROM testgroupspecification " + "WHERE ntemplatemanipulationcode = "
				+ TreeItemstm.get(0).getNtemplatemanipulationcode() +  " AND napprovalstatus ="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() 
				+ " AND ntransactionstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and nsitecode="+userinfo.getNmastersitecode();

		List<TestGroupSpecification> TreeItemsm = (List<TestGroupSpecification>) jdbcTemplate.query(set,
				new TestGroupSpecification());
		sQuery = "select tgs.noffsetdexpirydate,tgs.nallottedspeccode,tgs.ntemplatemanipulationcode,tgs.napproveconfversioncode,tgs.sspecname,"
				+ "CASE WHEN tgs.sversion='' THEN  '-' ELSE tgs.sversion END sversion, sversion," + queryformat
				+ " as sexpirydate,tgs.napprovalstatus,tgs.ntransactionstatus,coalesce(ts2.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode() + "',"
				+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,tgs.ntzexpirydate,"
				+ "coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode() + "',"
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') as sapprovalstatus, tz.stimezoneid, tgs.ncomponentrequired, coalesce(ts3.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode() + "',"
				+ " ts3.jsondata->'stransdisplaystatus'->>'en-US') as scomponentrequired, cm.scolorhexcode "
				+ " from testgroupspecification tgs, transactionstatus ts1, transactionstatus ts2, transactionstatus ts3, timezone tz, "
				+ "formwisestatuscolor fwc, colormaster cm"
				+ " where tgs.napprovalstatus=ts1.ntranscode and tgs.ntransactionstatus=ts2.ntranscode and tgs.ncomponentrequired = ts3.ntranscode "
				+ " and tz.ntimezonecode = tgs.ntzexpirydate and ts1.nstatus = tgs.nstatus and ts2.nstatus = tgs.nstatus and tz.nstatus = tgs.nstatus"
				+ " and fwc.ntranscode = tgs.napprovalstatus and cm.ncolorcode = fwc.ncolorcode and fwc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and fwc.nsitecode="+userinfo.getNmastersitecode()+ " and cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts3.nstatus = tgs.nstatus and tgs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgs.nsitecode="+userinfo.getNmastersitecode()+" and fwc.nformcode = "
				+ Enumeration.FormCode.TESTGROUP.getFormCode() + " and tgs.ntemplatemanipulationcode="
				+ TreeItemsm.get(0).getNtemplatemanipulationcode()
				+ " and tgs.napprovalstatus=31 order by tgs.nallottedspeccode;";

		List<TestGroupSpecification> lstTestGroupSpecification = (List<TestGroupSpecification>) jdbcTemplate
				.query(sQuery, new TestGroupSpecification());

		List<?> listSpec = dateUtilityFunction.getSiteLocalTimeFromUTC(lstTestGroupSpecification,
				Arrays.asList("sexpirydate"), Arrays.asList(userinfo.getStimezoneid()), userinfo, true,
				Arrays.asList("stransdisplaystatus", "sapprovalstatus", "scomponentrequired"), false);

		outputMap.put("SpecificationByRootlst", listSpec);

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> getTreeTemplateByProduct(UserInfo userinfo, int nproductcatcode, int nproductcode)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final List<InvoiceProductType> lstProductType = (List<InvoiceProductType>) getinvoiceProductType(userinfo).getBody();
		outputMap.put("filterProductType", lstProductType);

		String GetProduct = "select distinct pc.nproductcatcode,pc.sproductcatname,pc.sdescription,pc.ncategorybasedflow,pc.ndefaultstatus,pc.nsitecode,pc.nstatus"
				+ " from productcategory pc, treeversiontemplate tvt, treetemplatemaster ttm "
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ttm.ncategorycode = pc.nproductcatcode"
				+ " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and pc.nsitecode="+userinfo.getNmastersitecode()
				+ " and nproductcatcode > 0" + " and tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode="+userinfo.getNmastersitecode()+" and ttm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttm.nsitecode="+userinfo.getNmastersitecode()
				+ " and tvt.ntransactionstatus <> "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ " and nformcode = 24 and ncategorybasedflow=4";

		List<Product> ProductTypeDataList = (List<Product>) jdbcTemplate.query(GetProduct, new Product());
		outputMap.put("ProductTypeData", ProductTypeDataList);

		String GetProduct1 = "select distinct *"
				+ " from productcategory pc, treeversiontemplate tvt, treetemplatemaster ttm "
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ttm.ncategorycode = pc.nproductcatcode "
				+ " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and pc.nsitecode="+userinfo.getNmastersitecode() 
				+ " and nproductcatcode > 0" + " and tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode="+userinfo.getNmastersitecode()+" and ttm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttm.nsitecode="+userinfo.getNmastersitecode()
				+ " and tvt.ntransactionstatus <> "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ " and nformcode = 24 and pc.nproductcatcode= " + nproductcatcode + "";

		List<Product> ProductTypeDataList1 = (List<Product>) jdbcTemplate.query(GetProduct1, new Product());
		List<SampleType> ProductTypeDataList2 = (List<SampleType>) jdbcTemplate.query(GetProduct1, new SampleType());
		List<ProductCategory> ProductTypeDataList3 = (List<ProductCategory>) jdbcTemplate.query(GetProduct1,
				new ProductCategory());
		// outputMap.put("ProductTypeData", ProductTypeDataList1);

		String sQuery = "select tvt.ntreeversiontempcode,CONCAT(tvt.sversiondescription,' (',cast(nversionno as character varying(50)),')' ) as sversiondescription,"
				+ "tvt.ntransactionstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus from treetemplatemaster ttm,treeversiontemplate tvt, transactionstatus ts"
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ts.ntranscode = tvt.ntransactionstatus and ttm.nstatus = tvt.nstatus"
				+ " and ts.nstatus = tvt.nstatus and tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and tvt.nsitecode="+userinfo.getNmastersitecode()+ " and ttm.nsitecode = "
				+ ProductTypeDataList3.get(0).getNsitecode() + " and ttm.ncategorycode = "
				+ ProductTypeDataList3.get(0).getNproductcatcode() + " and tvt.nsampletypecode="
				+ ProductTypeDataList2.get(0).getNsampletypecode() + " and ttm.nformcode = " + 24
				+ " and tvt.ntransactionstatus <> " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ " order by tvt.ntreeversiontempcode desc";

		List<TreeVersionTemplate> TemplateVersionlst = (List<TreeVersionTemplate>) jdbcTemplate.query(sQuery,
				new TreeVersionTemplate());
		outputMap.put("TemplateVersionList", TemplateVersionlst);

		String getselectedProducttype = "select distinct pc.nproductcatcode,pc.sproductcatname,pc.sdescription,pc.ncategorybasedflow,pc.ndefaultstatus,pc.nsitecode,pc.nstatus"
				+ " from productcategory pc, treeversiontemplate tvt, treetemplatemaster ttm "
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ttm.ncategorycode = pc.nproductcatcode "
				+ " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and pc.nsitecode="+userinfo.getNmastersitecode()
				+ " and nproductcatcode= " + nproductcatcode + " and tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode="+userinfo.getNmastersitecode()+" and ttm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttm.nsitecode="+userinfo.getNmastersitecode()
				+ " and tvt.ntransactionstatus <> "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " and nformcode = 24";
		List<Product> getselectedPdtType = (List<Product>) jdbcTemplate.query(getselectedProducttype, new Product());
		outputMap.put("selectedProductType", getselectedPdtType);

		String getProductItems = "select * from product where nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= " +userinfo.getNmastersitecode()+ " and nproductcatcode = "
				+ nproductcatcode + " and nproductcatcode > -1 order by nproductcode ";
		List<Product> ProductItems = (List<Product>) jdbcTemplate.query(getProductItems, new Product());
		outputMap.put("ProductList", ProductItems);
		String getProductItems1 = "select * from product where nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= " +userinfo.getNmastersitecode()+" and nproductcode= " + nproductcode
				+ " and nproductcatcode = " + nproductcatcode + " and nproductcatcode > -1 order by nproductcode ";
		List<Product> ProductItems1 = (List<Product>) jdbcTemplate.query(getProductItems1, new Product());
		outputMap.put("selectedProduct", ProductItems1);

		String getTreeTemplates = "select * from treetemplatemanipulation where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userinfo.getNmastersitecode()+" and nproductcode=" + nproductcode
				+ " and nproductcatcode = " + nproductcatcode + " and nproductcatcode > -1 "

				+ " order by nproductcode";
		List<TreeTemplateManipulation> TreeItems = (List<TreeTemplateManipulation>) jdbcTemplate.query(getTreeTemplates,
				new TreeTemplateManipulation());
		outputMap.put("TreeListByProduct", TreeItems);

		String previousSlevelDescription = "";

		StringBuilder concatenatedValue = new StringBuilder();
		String lastLine = ""; // Track last concatenated line
		String lastValue = ""; // Track the last value

		// Declare variables to track words
		String lastAddedWord = "";
		String secondLastAddedWord = ""; // Store second last word
		int lastAddedIndex = -1;
		int secondLastAddedIndex = -1;

		for (TreeTemplateManipulation item : TreeItems) {
			System.out.println("Processing item: " + item);

			String currentWord = item.getSleveldescription(); // Store the current word
			// int lastAddedIndex = -1;

			// Append only if schildnode is not empty
			if (item.getSchildnode() != null && !item.getSchildnode().isEmpty()) {
				lastAddedWord = currentWord; // Update last added word
				lastAddedIndex = concatenatedValue.length();
				concatenatedValue.append(lastAddedWord).append(", ");
			}

			else {
				lastLine = currentWord;
				String str = "SELECT * FROM TreeTemplateManipulation WHERE sleveldescription='" + lastLine + "'"
						     + " and nsitecode="+userinfo.getNmastersitecode();
				List<TreeTemplateManipulation> TreeItems21 = (List<TreeTemplateManipulation>) jdbcTemplate.query(str,
						new TreeTemplateManipulation());

				String set = "SELECT * FROM testgroupspecification " + "WHERE ntemplatemanipulationcode = "
						+ TreeItems21.get(0).getNtemplatemanipulationcode() + " AND napprovalstatus ="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() 
						+ " AND ntransactionstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " and nsitecode="+userinfo.getNmastersitecode();

				List<TestGroupSpecification> TreeItemsd = (List<TestGroupSpecification>) jdbcTemplate.query(set,
						new TestGroupSpecification());

				// ✅ Check if `TreeItemsd` is NOT empty and approval status == 31
				if (!TreeItemsd.isEmpty()) {
					concatenatedValue.append(currentWord).append("\n");
				}

				else {
					// ❌ Remove SECOND LAST added word (not the latest one)
					String toRemove = lastAddedWord + ", ";
					int lastIndex = concatenatedValue.lastIndexOf(toRemove);

					if (lastIndex != -1) {
						concatenatedValue.delete(lastIndex, lastIndex + toRemove.length());
					}
					
					// Handle case where the second last word is last without ", "
					else {
						toRemove = lastAddedWord;
						lastIndex = concatenatedValue.lastIndexOf(toRemove);
						if (lastIndex != -1) {
							concatenatedValue.delete(lastIndex, lastIndex + toRemove.length());
						}
					}

					// ✅ Remove extra trailing comma or newline if needed
					if (concatenatedValue.length() > 0) {
						char lastChar = concatenatedValue.charAt(concatenatedValue.length() - 1);
						if (lastChar == ',' || lastChar == ' ' || lastChar == '\n') {
							concatenatedValue.deleteCharAt(concatenatedValue.length() - 1);
							concatenatedValue.append("\n");
						}
					}
				}
			}
		}

		// Store final concatenated value
		outputMap.put("sleveldescription", concatenatedValue.toString());

		String getTreeTemplates1 = "select * from treetemplatemanipulation where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nproductcatcode = "
				+ ProductTypeDataList.get(0).getNproductcatcode() + " and nproductcatcode > -1 "
				+ " and (schildnode IS NULL OR TRIM(schildnode) = '')" + ""
				+ " and nsitecode="+userinfo.getNmastersitecode()+" order by nproductcode";
		List<TreeTemplateManipulation> treeItemst = (List<TreeTemplateManipulation>) jdbcTemplate
				.query(getTreeTemplates1, new TreeTemplateManipulation());

		for (TreeTemplateManipulation treeItem : TreeItems) {
			treeItem.setSleveldescription(concatenatedValue.toString());
		}

		outputMap.put("TreeListByProduct", TreeItems);
		String queryformat = "TO_CHAR(dexpirydate,'" + userinfo.getSpgsitedatetime() + "') ";

		sQuery = "select tgs.noffsetdexpirydate,tgs.nallottedspeccode,tgs.ntemplatemanipulationcode,tgs.napproveconfversioncode,tgs.sspecname,"
				+ "CASE WHEN tgs.sversion='' THEN  '-' ELSE tgs.sversion END sversion, sversion," + queryformat
				+ " as sexpirydate,tgs.napprovalstatus,tgs.ntransactionstatus,coalesce(ts2.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode() + "',"
				+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,tgs.ntzexpirydate,"
				+ "coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode() + "',"
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') as sapprovalstatus, tz.stimezoneid, tgs.ncomponentrequired, coalesce(ts3.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode() + "',"
				+ " ts3.jsondata->'stransdisplaystatus'->>'en-US') as scomponentrequired, cm.scolorhexcode "
				+ " from testgroupspecification tgs, transactionstatus ts1, transactionstatus ts2, transactionstatus ts3, timezone tz, "
				+ "formwisestatuscolor fwc, colormaster cm"
				+ " where tgs.napprovalstatus=ts1.ntranscode and tgs.ntransactionstatus=ts2.ntranscode and tgs.ncomponentrequired = ts3.ntranscode "
				+ " and tz.ntimezonecode = tgs.ntzexpirydate and ts1.nstatus = tgs.nstatus and ts2.nstatus = tgs.nstatus and tz.nstatus = tgs.nstatus"
				+ " and fwc.ntranscode = tgs.napprovalstatus and cm.ncolorcode = fwc.ncolorcode and fwc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fwc.nsitecode="+userinfo.getNmastersitecode()+" and cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts3.nstatus = tgs.nstatus and tgs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgs.nsitecode="+userinfo.getNmastersitecode()+" and fwc.nformcode = "
				+ Enumeration.FormCode.TESTGROUP.getFormCode() + " and tgs.ntemplatemanipulationcode="
				+ treeItemst.get(0).getNtemplatemanipulationcode() + " order by tgs.nallottedspeccode;";

		List<TestGroupSpecification> lstTestGroupSpecification = (List<TestGroupSpecification>) jdbcTemplate
				.query(sQuery, new TestGroupSpecification());

		List<?> listSpec = dateUtilityFunction.getSiteLocalTimeFromUTC(lstTestGroupSpecification,
				Arrays.asList("sexpirydate"), Arrays.asList(userinfo.getStimezoneid()), userinfo, true,
				Arrays.asList("stransdisplaystatus", "sapprovalstatus", "scomponentrequired"), false);

		outputMap.put("SpecificationByRootlst", listSpec);

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> getSampleTypeByProduct(UserInfo userinfo, int nproductcatcode) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final List<InvoiceProductType> lstProductType = (List<InvoiceProductType>) getinvoiceProductType(userinfo).getBody();
		outputMap.put("filterProductType", lstProductType);

		String GetProduct = "select distinct pc.nproductcatcode,pc.sproductcatname,pc.sdescription,pc.ncategorybasedflow,pc.ndefaultstatus,pc.nsitecode,pc.nstatus"
				+ " from productcategory pc, treeversiontemplate tvt, treetemplatemaster ttm "
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ttm.ncategorycode = pc.nproductcatcode "
				+ " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and pc.nsitecode= " +userinfo.getNmastersitecode()
				+ " and nproductcatcode > 0" + " and tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode= " +userinfo.getNmastersitecode()+" and ttm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttm.nsitecode= " +userinfo.getNmastersitecode()
				+ " and tvt.ntransactionstatus <> "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ " and nformcode = 24 and ncategorybasedflow=4 ";

		List<Product> ProductTypeDataList = (List<Product>) jdbcTemplate.query(GetProduct, new Product());
		outputMap.put("ProductTypeData", ProductTypeDataList);

		String getProductItems1 = "select * from product where nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and nsitecode= " +userinfo.getNmastersitecode()+ " and nproductcatcode = "
				+ nproductcatcode + " and nproductcatcode > -1 order by nproductcode";
		List<Product> ProductItems1 = (List<Product>) jdbcTemplate.query(getProductItems1, new Product());
		outputMap.put("selectedProduct", ProductItems1);

		String GetProduct1 = "select distinct *"
				+ " from productcategory pc, treeversiontemplate tvt, treetemplatemaster ttm "
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ttm.ncategorycode = pc.nproductcatcode"
				+ " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and pc.nsitecode="+userinfo.getNmastersitecode()
				+ " and nproductcatcode > 0" + " and tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode="+userinfo.getNmastersitecode()+" and ttm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ttm.nsitecode="+userinfo.getNmastersitecode()
				+ " and tvt.ntransactionstatus <> "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ " and nformcode = 24 and pc.nproductcatcode= " + nproductcatcode + "";

		List<Product> ProductTypeDataList1 = (List<Product>) jdbcTemplate.query(GetProduct1, new Product());
		List<SampleType> ProductTypeDataList2 = (List<SampleType>) jdbcTemplate.query(GetProduct1, new SampleType());
		List<ProductCategory> ProductTypeDataList3 = (List<ProductCategory>) jdbcTemplate.query(GetProduct1,
				new ProductCategory());

		outputMap.put("selectedProductType", ProductTypeDataList1);

		String getProductItems = "select * from product where nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userinfo.getNmastersitecode()+ " and nproductcatcode = "
				+ nproductcatcode + " and nproductcatcode > -1 order by nproductcode";
		List<Product> ProductItems = (List<Product>) jdbcTemplate.query(getProductItems, new Product());
		outputMap.put("ProductList", ProductItems);

		String getTreeTemplates = "select * from treetemplatemanipulation where nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode= " +userinfo.getNmastersitecode()
				// + " and nproductcatcode =" +ProductTypeDataList.get(0).getNproductcatcode() +
				// "and nproductcatcode > -1 order by nproductcode";
				+ " and nproductcatcode = " + nproductcatcode + " and nproductcatcode > -1 order by nproductcode";
		List<TreeTemplateManipulation> TreeItems = (List<TreeTemplateManipulation>) jdbcTemplate.query(getTreeTemplates,
				new TreeTemplateManipulation());
		outputMap.put("TreeListByProduct", TreeItems);

		String previousSlevelDescription = "";

		StringBuilder concatenatedValue = new StringBuilder();
		String lastLine = ""; // Track last concatenated line
		String lastValue = ""; // Track the last value

		// Declare variables to track words
		String lastAddedWord = "";
		String secondLastAddedWord = ""; // Store second last word
		int lastAddedIndex = -1;
		int secondLastAddedIndex = -1;

		for (TreeTemplateManipulation item : TreeItems) {
			System.out.println("Processing item: " + item);

			String currentWord = item.getSleveldescription(); // Store the current word
			// int lastAddedIndex = -1;

			// Append only if schildnode is not empty
			if (item.getSchildnode() != null && !item.getSchildnode().isEmpty()) {
				lastAddedWord = currentWord; // Update last added word
				lastAddedIndex = concatenatedValue.length();
				concatenatedValue.append(lastAddedWord).append(", ");
			}

			else {
				lastLine = currentWord;
				String str = "SELECT * FROM TreeTemplateManipulation WHERE sleveldescription='" + lastLine + "'"
						+ " and nsitecode="+userinfo.getNmastersitecode();
				List<TreeTemplateManipulation> TreeItems21 = (List<TreeTemplateManipulation>) jdbcTemplate.query(str,
						new TreeTemplateManipulation());

				String set = "SELECT * FROM testgroupspecification " + "WHERE ntemplatemanipulationcode = "
						+ TreeItems21.get(0).getNtemplatemanipulationcode() + " AND napprovalstatus ="
								+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() 
						+ " AND ntransactionstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " and nsitecode="+userinfo.getNmastersitecode();

				List<TestGroupSpecification> TreeItemsd = (List<TestGroupSpecification>) jdbcTemplate.query(set,
						new TestGroupSpecification());

				// ✅ Check if `TreeItemsd` is NOT empty and approval status == 31
				if (!TreeItemsd.isEmpty()) {
					concatenatedValue.append(currentWord).append("\n");
				}

				else {
					// ❌ Remove SECOND LAST added word (not the latest one)
					String toRemove = lastAddedWord + ", ";
					int lastIndex = concatenatedValue.lastIndexOf(toRemove);

					if (lastIndex != -1) {
						concatenatedValue.delete(lastIndex, lastIndex + toRemove.length());
					}
					// Handle case where the second last word is last without ", "
					else {
						toRemove = lastAddedWord;
						lastIndex = concatenatedValue.lastIndexOf(toRemove);
						if (lastIndex != -1) {
							concatenatedValue.delete(lastIndex, lastIndex + toRemove.length());
						}
					}

					// ✅ Remove extra trailing comma or newline if needed
					if (concatenatedValue.length() > 0) {
						char lastChar = concatenatedValue.charAt(concatenatedValue.length() - 1);
						if (lastChar == ',' || lastChar == ' ' || lastChar == '\n') {
							concatenatedValue.deleteCharAt(concatenatedValue.length() - 1);
							concatenatedValue.append("\n");
						}
					}
				}
			}
		}

		// Store final concatenated value
		outputMap.put("sleveldescription", concatenatedValue.toString());

		// Store final concatenated value
		for (TreeTemplateManipulation treeItem : TreeItems) {
			treeItem.setSleveldescription(concatenatedValue.toString());
		}

		outputMap.put("TreeListByProduct", TreeItems);

		String sQuery = "select tvt.ntreeversiontempcode,CONCAT(tvt.sversiondescription,' (',cast(nversionno as character varying(50)),')' ) as sversiondescription,"
				+ "tvt.ntransactionstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus from treetemplatemaster ttm,treeversiontemplate tvt, transactionstatus ts"
				+ " where ttm.ntemplatecode = tvt.ntemplatecode and ts.ntranscode = tvt.ntransactionstatus and ttm.nstatus = tvt.nstatus"
				+ " and ts.nstatus = tvt.nstatus and tvt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tvt.nsitecode="+userinfo.getNmastersitecode()+" and ttm.nsitecode = "
				+ ProductTypeDataList3.get(0).getNsitecode() + " and ttm.ncategorycode = "
				+ ProductTypeDataList3.get(0).getNproductcatcode() + " and tvt.nsampletypecode="
				+ ProductTypeDataList2.get(0).getNsampletypecode() + " and ttm.nformcode = " + 24
				+ " and tvt.ntransactionstatus <> " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ " order by tvt.ntreeversiontempcode desc";

		List<TreeVersionTemplate> TemplateVersionlst = (List<TreeVersionTemplate>) jdbcTemplate.query(sQuery,
				new TreeVersionTemplate());
		outputMap.put("TemplateVersionList", TemplateVersionlst);
		String getTreetemplates = "select * from treetemplatemanipulation where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userinfo.getNmastersitecode()+ " and nproductcatcode = "
				+ ProductTypeDataList.get(0).getNproductcatcode() + " and nproductcatcode > -1"
				+ " and (schildnode IS NULL OR TRIM(schildnode) = '')" + " order by nproductcode";
		List<TreeTemplateManipulation> TreeItemsr = (List<TreeTemplateManipulation>) jdbcTemplate
				.query(getTreetemplates, new TreeTemplateManipulation());
		String queryformat = "TO_CHAR(dexpirydate,'" + userinfo.getSpgsitedatetime() + "') ";
		String squery = "select tgs.noffsetdexpirydate,tgs.nallottedspeccode,tgs.ntemplatemanipulationcode,tgs.napproveconfversioncode,tgs.sspecname,"
				+ "CASE WHEN tgs.sversion='' THEN  '-' ELSE tgs.sversion END sversion, sversion," + queryformat
				+ " as sexpirydate,tgs.napprovalstatus,tgs.ntransactionstatus,coalesce(ts2.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode() + "',"
				+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,tgs.ntzexpirydate,"
				+ "coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userinfo.getSlanguagetypecode() + "',"
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') as sapprovalstatus, tz.stimezoneid, tgs.ncomponentrequired, coalesce(ts3.jsondata->'stransdisplaystatus'->>'"
				+ userinfo.getSlanguagetypecode() + "',"
				+ " ts3.jsondata->'stransdisplaystatus'->>'en-US') as scomponentrequired, cm.scolorhexcode "
				+ " from testgroupspecification tgs, transactionstatus ts1, transactionstatus ts2, transactionstatus ts3, timezone tz, "
				+ "formwisestatuscolor fwc, colormaster cm"
				+ " where tgs.napprovalstatus=ts1.ntranscode and tgs.ntransactionstatus=ts2.ntranscode and tgs.ncomponentrequired = ts3.ntranscode "
				+ " and tz.ntimezonecode = tgs.ntzexpirydate and ts1.nstatus = tgs.nstatus and ts2.nstatus = tgs.nstatus and tz.nstatus = tgs.nstatus"
				+ " and fwc.ntranscode = tgs.napprovalstatus and cm.ncolorcode = fwc.ncolorcode and fwc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fwc.nsitecode="+userinfo.getNmastersitecode()+" and cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts3.nstatus = tgs.nstatus and tgs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgs.nsitecode="+userinfo.getNmastersitecode()+" and fwc.nformcode = "
				+ Enumeration.FormCode.TESTGROUP.getFormCode() + " and tgs.ntemplatemanipulationcode="
				+ TreeItemsr.get(0).getNtemplatemanipulationcode() + " order by tgs.nallottedspeccode;";

		List<TestGroupSpecification> lstTestGroupSpecification = (List<TestGroupSpecification>) jdbcTemplate
				.query(squery, new TestGroupSpecification());

		List<?> listSpec = dateUtilityFunction.getSiteLocalTimeFromUTC(lstTestGroupSpecification,
				Arrays.asList("sexpirydate"), Arrays.asList(userinfo.getStimezoneid()), userinfo, true,
				Arrays.asList("stransdisplaystatus", "sapprovalstatus", "scomponentrequired"), false);

		outputMap.put("SpecificationByRootlst", listSpec);

		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> getProductType(UserInfo userinfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String orderbyalphabetical = "sproductname";
		if (userinfo.getNuserrole() == Enumeration.TransactionStatus.NA.gettransactionstatus()) {

			final List<InvoiceProductType> lstProductType = (List<InvoiceProductType>) getinvoiceProductType(userinfo)
					.getBody();
			outputMap.put("filterProductType", lstProductType);

			if (!lstProductType.isEmpty()) {
				int ntypecode = 0;
				final List<InvoiceProductType> defaultType = lstProductType.stream().filter(
						defTest -> defTest.getNactive() == Enumeration.TransactionStatus.YES.gettransactionstatus())
						.collect(Collectors.toList());
				if (defaultType.isEmpty()) {
					ntypecode = lstProductType.get(lstProductType.size() - 1).getNtypecode();
					outputMap.put("SelectedProductType", lstProductType.get(lstProductType.size() - 1));
					outputMap.put("selectedClientSite", null);
				} else {
					ntypecode = defaultType.get(0).getNtypecode();
					outputMap.put("SelectedProductType", defaultType.get(0));
				}

				final List<InvoiceProductMaster> lstProductMasters = (List<InvoiceProductMaster>) getProductMaster(
						userinfo).getBody();

				outputMap.put("ProductMaster", lstProductMasters);
				if (lstProductMasters.isEmpty()) {
					outputMap.put("SelectedProductMaster", null);
					outputMap.put("selectedTaxProduct", null);
					outputMap.put("TaxProduct", lstProductMasters);
					outputMap.put("ProductTestDetails", null);

				} else {
					outputMap.put("SelectedProductMaster", lstProductMasters.get(0));

					int nproductcode = lstProductMasters.get(0).getNproductcode();
					List<TaxProductDetails> lsttaxproductGet = getTaxproductDetails(userinfo, nproductcode);
					if (lsttaxproductGet.size() > 0) {

						outputMap.put("selectedTaxProduct", lsttaxproductGet.get(lsttaxproductGet.size() - 1));

					}
					List<ProductTest> getProductTest = getProductTestDetails(userinfo, nproductcode);

					if (getProductTest.size() > 0) {
						outputMap.put("selectedProductTestDetails", getProductTest.get(getProductTest.size() - 1));

					}
					outputMap.put("TaxProduct", lsttaxproductGet);
					outputMap.put("ProductTestDetails", getProductTest);
					outputMap.putAll((Map<String, Object>) getProductMasterFile(nproductcode, userinfo).getBody());
				}
			}
		} else {
			int userrolecode = userinfo.getNuserrole();

			int stext = 0;
			int stext2 = 0;
			final List<InvoiceProductType> lstProductType = (List<InvoiceProductType>) getinvoiceProductType(userinfo)
					.getBody();
			outputMap.put("filterProductType", lstProductType);

			int nuser = userinfo.getNuserrole();
			String st = "select * from userrolefieldcontrol where nformcode= " + userinfo.getNformcode()
					+ " and  nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and nneedrights=3 ";
//			List<UserRoleFieldControl> listTest = (List<UserRoleFieldControl>) jdbcTemplate.query(st,
//					new UserRoleFieldControl());
			final List<UserRoleFieldControl> CMAllTin = (List<UserRoleFieldControl>) jdbcTemplate.query(st,
					new UserRoleFieldControl());

			final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(CMAllTin, "getNfieldcode");
			if (CMAllTin.size() == 0) {
				Map<String, Integer> ProductDetails = new HashMap<>();
				int defaultValue = +Enumeration.TransactionStatus.NO.gettransactionstatus();

				ProductDetails.put("stext", defaultValue);
				ProductDetails.put("stext2", defaultValue);

			}

			else {

				String std = "select * from fieldmaster where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "nfieldcode in ("
						+ sntestgrouptestcode + ")";

				List<FieldMaster> listTest2 = (List<FieldMaster>) jdbcTemplate.query(std, new FieldMaster());

				Set<String> fieldNames = listTest2.stream().map(FieldMaster::getSfieldname).collect(Collectors.toSet());
				int defaultValue = +Enumeration.TransactionStatus.YES.gettransactionstatus();
				int value = +Enumeration.TransactionStatus.NO.gettransactionstatus();
				stext = fieldNames.contains("saddtext1") ? defaultValue : value;
				stext2 = fieldNames.contains("saddtext2") ? defaultValue : value;
			}
			if (!lstProductType.isEmpty()) {
				int ntypecode = 0;
				final List<InvoiceProductType> defaultType = lstProductType.stream().filter(
						defTest -> defTest.getNactive() == Enumeration.TransactionStatus.YES.gettransactionstatus())
						.collect(Collectors.toList());
				if (defaultType.isEmpty()) {
					ntypecode = lstProductType.get(lstProductType.size() - 1).getNtypecode();
					outputMap.put("SelectedProductType", lstProductType.get(lstProductType.size() - 1));
					outputMap.put("selectedClientSite", null);
				} else {
					ntypecode = defaultType.get(0).getNtypecode();
					outputMap.put("SelectedProductType", defaultType.get(0));
				}

				final List<InvoiceProductMaster> lstProductMasters = (List<InvoiceProductMaster>) getProductMaster(
						userinfo).getBody();

				outputMap.put("ProductMaster", lstProductMasters);

				outputMap.put("Stext", stext);
				outputMap.put("Stext2", stext2);
				outputMap.put("Userrolecode", userrolecode);
				if (lstProductMasters.isEmpty()) {
					outputMap.put("SelectedProductMaster", null);
					outputMap.put("selectedTaxProduct", null);
					outputMap.put("TaxProduct", lstProductMasters);

				} else {
					outputMap.put("SelectedProductMaster", lstProductMasters.get(0));

					List<TaxProductDetails> lsttaxproductGet = getTaxproductDetails(userinfo,
							lstProductMasters.get(0).getNproductcode());
					if (lsttaxproductGet.size() > 0) {

						outputMap.put("selectedTaxProduct", lsttaxproductGet.get(lsttaxproductGet.size() - 1));

						outputMap.put("Stext", stext);
						outputMap.put("Stext2", stext2);
						outputMap.put("Userrolecode", userrolecode);
						outputMap.put("TaxProduct", lsttaxproductGet);

						outputMap.putAll(
								(Map<String, Object>) getProductMasterFile(lstProductMasters.get(0).getNproductcode(),
										userinfo).getBody());
					}
					List<ProductTest> getProductTest = getProductTestDetails(userinfo,
							lstProductMasters.get(0).getNproductcode());
					if (getProductTest.size() > 0) {
						outputMap.put("selectedProductTestDetails", getProductTest.get(getProductTest.size() - 1));

					}
					outputMap.put("ProductTestDetails", getProductTest);
				}
			} else {
				outputMap.put("SelectedProductMaster", null);
				outputMap.put("selectedTaxProduct", null);
			}

		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	/**
	 * This method is used to retrieve list of all active InvoiceProductmaster for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         InvoiceProductmaster
	 * @throws Exception that are thrown from this Service layer
	 */
	@SuppressWarnings({ "unused" })
	private String processConcatenatedValue(String concatenatedValue, Set<String> validDescriptions) {
		// Split the concatenated value by newlines
		StringBuilder filteredValue = new StringBuilder();
		String[] lines = concatenatedValue.split("\n");

		for (String line : lines) {
			// Split by commas and check if each description is valid
			String[] descriptions = line.split(",");
			StringBuilder validLine = new StringBuilder();

			for (String description : descriptions) {
				description = description.trim(); // Remove any leading/trailing spaces
				if (validDescriptions.contains(description)) {
					if (validLine.length() > 0) {
						validLine.append(" ,");
					}
					validLine.append(description);
				}
			}

			// Add the valid line to the filtered value
			if (validLine.length() > 0) {
				filteredValue.append(validLine.toString()).append(" \n");
			}
		}

		// Return the filtered concatenated value
		return filteredValue.toString();
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	private ResponseEntity<Object> getProductMasterFile(int nproductcode, UserInfo userinfo) throws Exception {
		// TODO Auto-generated method stub
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		String query = "select tf.noffsetdcreateddate,tf.nproductmasterfilecode,(select  count(nproductmasterfilecode) from invoiceproductfile where nproductmasterfilecode>0 and nproductcode = "
				+ nproductcode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode="+userinfo.getNmastersitecode()
				+ ") as ncount,tf.sdescription,"
				+ " tf.nproductmasterfilecode as nprimarycode,tf.sfilename,tf.nproductcode,tf.ssystemfilename,"
				+ " tf.nattachmenttypecode,coalesce(at.jsondata->'sattachmenttype'->>'"
				+ userinfo.getSlanguagetypecode() + "',"
				+ "	at.jsondata->'sattachmenttype'->>'en-US') as sattachmenttype, case when tf.nlinkcode=-1 then '-' else lm.jsondata->>'slinkname'"
				+ " end slinkname, tf.nfilesize," + " case when tf.nattachmenttypecode= "
				+ Enumeration.AttachmentType.LINK.gettype() + " then '-' else" + " COALESCE(TO_CHAR(tf.dcreateddate,'"
				+ userinfo.getSpgdatetimeformat() + "'),'-') end  as screateddate, "
				+ " tf.nlinkcode, case when tf.nlinkcode = -1 then tf.nfilesize::varchar(1000) else '-' end sfilesize"
				+ " from invoiceproductfile tf,attachmenttype at, linkmaster lm  "
				+ " where at.nattachmenttypecode = tf.nattachmenttypecode and at.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and at.nsitecode="+userinfo.getNmastersitecode()
				+ " and lm.nlinkcode = tf.nlinkcode and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nsitecode="+userinfo.getNmastersitecode()
				+ " and tf.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and tf.nsitecode="+userinfo.getNmastersitecode()
				+ " and tf.nproductcode=" + nproductcode
				+ " order by tf.nproductmasterfilecode;";

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		List<InvoiceProductFile> productFile = jdbcTemplate.query(query, new InvoiceProductFile());

		outputMap.put("productFile", dateUtilityFunction.getSiteLocalTimeFromUTC(productFile,
				Arrays.asList("screateddate"), null, userinfo, false, null, false));

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	private List<TaxProductDetails> getTaxproductDetails(UserInfo userinfo, int nproductcode) {

		final String strQuery = "SELECT " + " tpd.ntaxproductcode, " + " tpd.ntaxcode, " + " tpd.nproductcode, "
				+ " tpd.nactive, " + " tpd.sversionno, " + " ipm.sproductname, " + " itt.ntax, " + " itt.staxname, "
				+ " tpd.ncaltypecode " + " FROM taxproductdetails tpd "
				+ " LEFT JOIN invoiceproductmaster ipm ON tpd.nproductcode = ipm.nproductcode AND ipm. nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ipm.nsitecode="+userinfo.getNmastersitecode()
				+ " LEFT JOIN invoicetaxtype itt ON itt.ntaxcode = tpd.ntaxcode AND itt.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and itt.nsitecode="+userinfo.getNmastersitecode()
				+ " WHERE tpd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				+ " and tpd.nsitecode="+userinfo.getNmastersitecode()+" AND tpd.nproductcode = " + nproductcode
				+ " ORDER BY tpd.ntaxproductcode";

		List<TaxProductDetails> lsttaxDetails = (List<TaxProductDetails>) jdbcTemplate.query(strQuery,
				new TaxProductDetails());

		return lsttaxDetails;
	}
	/**
	 * This method is used to retrieve list of all active InvoiceProductmaster for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         InvoiceProductmaster
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public InvoiceProductMaster getActiveInvoiceProductMasterById(int nproductcode, UserInfo userInfo)
			throws Exception {

		String strQuery = "select ipm.nproductcode,ipm.ntypecode,ipm.slimscode,ipm.sproductname,ipm.sdescription,ipm.sinvoicedescription,ipm.saddtext1,ipm.saddtext2,"
				+ " ipm.ntaxavailable,ipm.ncost,ipt.stypename,ipt.ntypecode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus from invoiceproductmaster ipm,invoiceproducttype ipt,transactionstatus ts where "
				+ " ipt.ntypecode=ipm.ntypecode and ipm.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ipt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ipm.nsitecode="+userInfo.getNmastersitecode()+" and ipt.nsitecode="+userInfo.getNmastersitecode()
				+ " and ipm.nproductcode="+ nproductcode + " and ts.ntranscode=ipm.ntaxavailable";

		// return (InvoiceProductMaster) jdbcQueryForObject(strQuery,
		// InvoiceProductMaster.class);
		return (InvoiceProductMaster) jdbcUtilityFunction.queryForObject(strQuery, InvoiceProductMaster.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to update entry in InvoiceProductmaster table. Need to
	 * validate that the InvoiceProductmaster object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * InvoiceProductmaster name for the specified site before saving into database.
	 * Need to check that there should be only one default InvoiceProductmaster for
	 * a site
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] object holding details
	 *                                to be updated in InvoiceProductmaster table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceProductmaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateInvoiceProductMaster(InvoiceProductMaster objInvoiceProductMaster,
			UserInfo userInfo) throws Exception {
		final InvoiceProductMaster productmaster = getActiveInvoiceProductMasterById(
				objInvoiceProductMaster.getNproductcode(), userInfo);

		if (productmaster == null) {
			// status code:205
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nproductcode from invoiceproductmaster where sproductname = '"
					+ stringUtilityFunction.replaceQuote(productmaster.getSproductname()) + "' and nproductcode = "
					+ objInvoiceProductMaster.getNproductcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and nsitecode="+userInfo.getNmastersitecode();

			final List<InvoiceProductMaster> productmasterList = (List<InvoiceProductMaster>) jdbcTemplate
					.query(queryString, new InvoiceProductMaster());

			if (!productmasterList.isEmpty()) {

				final String updateQuerString = "update invoiceproductmaster set nproductcode="
						+ objInvoiceProductMaster.getNproductcode() + "" + ",ntypecode="
						+ objInvoiceProductMaster.getNtypecode() + ",slimscode=N'"
						+ stringUtilityFunction.replaceQuote(objInvoiceProductMaster.getSlimscode()) + "',"
						+ "sproductname=N'"
						+ stringUtilityFunction.replaceQuote(objInvoiceProductMaster.getSproductname()) + "',"
						+ " sdescription ='"
						+ stringUtilityFunction.replaceQuote(objInvoiceProductMaster.getSdescription())
						+ "', sinvoicedescription ='"
						+ stringUtilityFunction.replaceQuote(objInvoiceProductMaster.getSinvoicedescription()) + "'"
						+ ",saddtext1='" + stringUtilityFunction.replaceQuote(objInvoiceProductMaster.getSaddtext1())
						+ "',saddtext2='" + stringUtilityFunction.replaceQuote(objInvoiceProductMaster.getSaddtext2())
						+ "'" + ", ntaxavailable=" + objInvoiceProductMaster.getNtaxavailable() + ",ncost="
						+ objInvoiceProductMaster.getNcost() + "" + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nproductcode="
						+ objInvoiceProductMaster.getNproductcode() + ";";

				jdbcTemplate.execute(updateQuerString);

				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_EDITPRODUCTMASTER");

				final List<Object> listAfterSave = new ArrayList<>();
				listAfterSave.add(objInvoiceProductMaster);

				final List<Object> listBeforeSave = new ArrayList<>();
				listBeforeSave.add(productmaster);

				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
						userInfo);

				return getInvoiceByProduct(objInvoiceProductMaster.getNproductcode(), userInfo);
			} else {
				// Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in InvoiceProductmaster table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] an Object holds the
	 *                                record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         InvoiceProductmaster object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteInvoiceProductMaster(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final List<Object> savedProductList = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		String query = "";
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		InvoiceProductMaster objInvoiceProductMaster = objMapper.convertValue(inputMap.get("ProductMaster"),
				new TypeReference<InvoiceProductMaster>() {
				});

		final InvoiceProductMaster productmaster = getActiveInvoiceProductMasterById(
				objInvoiceProductMaster.getNproductcode(), userInfo);

		if (productmaster == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			query = "select 'IDS_TRANSACTION' as Msg from quotationitemdetails where jsondata->>'sproductname'='"
					+ stringUtilityFunction.replaceQuote(objInvoiceProductMaster.getSproductname()) + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all"
					+ " select 'IDS_TRANSACTION' as Msg from invoiceproductitemdetails where jsondata->>'sproductname'='"
					+ stringUtilityFunction.replaceQuote(objInvoiceProductMaster.getSproductname()) + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				objDeleteValidation = projectDAOSupport.validateDeleteRecord(objInvoiceProductMaster.getSproductname(),
						userInfo);
				if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				// Added by Ganesh on 21/11/2025 - Delete Files in S3
				//start-ALPDJ21-132
				String invoiceProductFileQuery = "select ssystemfilename from invoiceproductfile where nproductcode ="
						+ objInvoiceProductMaster.getNproductcode() + " and nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
						+ " and nsitecode="+ userInfo.getNmastersitecode();
				final List<String> objInvoiceProductFile = jdbcTemplate.queryForList(invoiceProductFileQuery, String.class);
				if(!objInvoiceProductFile.isEmpty()) {
					fTPUtilityFunction.deleteFTPFile(objInvoiceProductFile, "", userInfo);
					final String updateString = "update invoiceproductfile set nstatus ="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", " + "dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nproductcode=" + objInvoiceProductMaster.getNproductcode() 
						+ ";";
				    jdbcTemplate.execute(updateString);
				}
				//end-ALPDJ21-132

				final String updateQueryString = "update invoiceproductmaster set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nproductcode="
						+ objInvoiceProductMaster.getNproductcode();

				jdbcTemplate.execute(updateQueryString);
				objInvoiceProductMaster
						.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				savedProductList.add(objInvoiceProductMaster);
				multilingualIDList.add("IDS_DELETEPRODUCTMASTER");
				auditUtilityFunction.fnInsertAuditAction(savedProductList, 1, null, multilingualIDList, userInfo);

				return getproductByDel(objInvoiceProductMaster.getNtypecode(), userInfo);
			}

			else {

				return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}

		}

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getProductTypes(Map<String, Object> inputMap, UserInfo userinfo) throws Exception {
		String str = "";
		if (inputMap.containsKey("ntypecode")) {
			final int ntypecode = (Integer) inputMap.get("ntypecode");
			// final int ntypecode = (Integer) inputMap.get("ntypecode");
			if (ntypecode == -1) {
				str = "select * from invoiceproducttype where nactive=3  and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and nsitecode="+userinfo.getNmastersitecode();
			} else {
				str = "select * from invoiceproducttype where nactive=3 and ntypecode=" + ntypecode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
						+ " and nsitecode="+userinfo.getNmastersitecode();
			}
		} else {
			str = "select * from invoiceproducttype where nactive=3 and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and nsitecode="+userinfo.getNmastersitecode();
		}
		return new ResponseEntity<>(jdbcTemplate.query(str, new InvoiceProductType()), HttpStatus.OK);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
//	@Override
//	public ResponseEntity<Object> getinvoiceProductType(UserInfo userinfo) throws Exception {
//		// final int ntypecode = (Integer) inputMap.get("ntypecode");
//		String str = "select * from invoiceproducttype where nactive="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" and nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userinfo.getNmastersitecode();
//		return new ResponseEntity<>(jdbcTemplate.query(str, new InvoiceProductType()), HttpStatus.OK);
//
//	}
	@Override
	public ResponseEntity<Object> getinvoiceProductType(UserInfo userinfo) throws Exception {
		//final int ntypecode = (Integer) inputMap.get("ntypecode");
		String str = "select * from invoiceproducttype where nactive=3 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		return new ResponseEntity<>(jdbcTemplate.query(str, new InvoiceProductType()), HttpStatus.OK);

	}
	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getActiveProductMasterById(final int nproductcode, final UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		String query = "";

		String squery = "select sproductname from invoiceproductmaster where nproductcode =" + nproductcode + ""
				+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and nsitecode="+userInfo.getNmastersitecode();


		final InvoiceProductMaster objInvoiceProductMaster = (InvoiceProductMaster) jdbcUtilityFunction
				.queryForObject(squery, InvoiceProductMaster.class, jdbcTemplate);

		boolean validRecord = false;
		query = "select 'IDS_TRANSACTION' as Msg from quotationitemdetails where jsondata->>'sproductname'='"
				+ stringUtilityFunction.replaceQuote(objInvoiceProductMaster.getSproductname()) + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and nsitecode="+userInfo.getNmastersitecode()+" union all"
				+ " select 'IDS_TRANSACTION' as Msg from invoiceproductitemdetails where jsondata->>'sproductname'='"
				+ stringUtilityFunction.replaceQuote(objInvoiceProductMaster.getSproductname()) + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and nsitecode="+userInfo.getNmastersitecode();
		ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);

		if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
			validRecord = true;
			objDeleteValidation = projectDAOSupport.validateDeleteRecord(objInvoiceProductMaster.getSproductname(),
					userInfo);
			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
			} else {
				validRecord = false;
			}
		}
		InvoiceProductMaster reportsettings = null;

		if (validRecord) {

			String str = "select ipm.nproductcode,ipm.ntypecode,ipm.slimscode,ipm.sproductname,ipm.sdescription,ipm.sinvoicedescription,ipm.saddtext1,ipm.saddtext2,"
					+ " ipm.ntaxavailable,ipm.ncost,ipt.stypename,ipt.ntypecode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus from invoiceproductmaster ipm,invoiceproducttype ipt,transactionstatus ts where "
					+ " ipt.ntypecode=ipm.ntypecode and ipm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ipm.nsitecode="+userInfo.getNmastersitecode()+"" 
					+ " and ipt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ipt.nsitecode="+userInfo.getNmastersitecode()+""
					+ " and ipm.nproductcode="+ nproductcode + " and ts.ntranscode=ipm.ntaxavailable";

			// return (InvoiceProductMaster) jdbcQueryForObject(str,
			// InvoiceProductMaster.class);
			// reportsettings = (InvoiceProductMaster) jdbcUtilityFunction(str,
			// InvoiceProductMaster.class);
			reportsettings = (InvoiceProductMaster) jdbcUtilityFunction.queryForObject(str, InvoiceProductMaster.class,
					jdbcTemplate);
		} else {

			return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(reportsettings, HttpStatus.OK);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@Override
	public InvoiceProductType getActiveInvoiceProductTypeById(int nproductcode, UserInfo userInfo) throws Exception {
		final String strQuery = "select ipm.nproductcode,ipm.ntypecode,ipm.sproductname,ipt.stypename from invoiceproductmaster ipm,invoiceproducttype ipt "
				+ " where ipm.nproductcode=" + nproductcode + " and ipm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ipt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ipm.nsitecode="+userInfo.getNmastersitecode()+" and ipt.nsitecode="+userInfo.getNmastersitecode()+""				
				+ " and ipm.ntypecode=ipt.ntypecode";

		// return (InvoiceProductType) jdbcQueryForObject(strQuery,
		// InvoiceProductType.class);
		return (InvoiceProductType) jdbcUtilityFunction.queryForObject(strQuery, InvoiceProductType.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getProductbyProductMaster(int ntypecode, int nmastersitecode, int nproductcode,
			final UserInfo userInfo) throws Exception {
		String strQuery = "";
		if (ntypecode != -1) {
			strQuery = " select ipm.nproductcode,ipm.ntypecode,ipm.slimscode,ipm.sproductname,ipm.sdescription,ipm.sinvoicedescription,ipm.saddtext1,ipm.saddtext2,"
					+ " ipm.ntaxavailable,ipm.ncost,ipt.stypename,ipt.ntypecode,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus from invoiceproductmaster ipm,invoiceproducttype ipt,transactionstatus ts where "
					+ " ipm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ipt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and ipm.nsitecode="+userInfo.getNmastersitecode()+" and ipt.nsitecode="+userInfo.getNmastersitecode()+""
					+ " and ipm.ntypecode=ipt.ntypecode and ipm.ntypecode=" + ntypecode
					+ " and ts.ntranscode=ipm.ntaxavailable ORDER BY ipm.nproductcode DESC";
		} else {
			strQuery = "select * from invoiceproductmaster where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode="+userInfo.getNmastersitecode();
		}
		List<InvoiceProductMaster> lstProduct = jdbcTemplate.query(strQuery, new InvoiceProductMaster());
		return new ResponseEntity<>(lstProduct, HttpStatus.OK);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings({ "unused" })
	public ResponseEntity<Object> getProductbyproductMaster(int ntypecode, int nmastersitecode, int nproductcode,
			final UserInfo userInfo, int nallottedspeccode) throws Exception {
		String strQuery = "";
		if (ntypecode != -1) {
			if (nallottedspeccode != -1) {
				strQuery = "SELECT ipm.nproductcode, ipm.ntypecode, ipm.slimscode, ipm.sproductname, ipm.sdescription, "
						+ "ipm.sinvoicedescription, ipm.saddtext1, ipm.saddtext2, ipm.ntaxavailable, ipm.ncost, "
						+ "ipt.stypename, ipt.ntypecode, " + "COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "', "
						+ "ts.jsondata->'stransdisplaystatus'->>'en-US') AS sdisplaystatus "
						+ "FROM invoiceproductmaster ipm "
						+ "JOIN invoiceproducttype ipt ON ipm.ntypecode = ipt.ntypecode "
						+ "JOIN transactionstatus ts ON ts.ntranscode = ipm.ntaxavailable " + "WHERE ipm.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND ipt.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND ipm.ntypecode = "
						+ ntypecode + " " + " AND ipm.jsondata->>'nallottedspeccode' = '" + nallottedspeccode + "'"
						+ " and ipm.nsitecode= " +userInfo.getNmastersitecode()+ " and ipt.nsitecode= " + userInfo.getNmastersitecode()
						+ " ORDER BY ipm.nproductcode desc";
			} else {

				strQuery = " SELECT ipm.*, " + " COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "', "
						+ " ts.jsondata->'stransdisplaystatus'->>'en-US') AS sdisplaystatus "
						+ " FROM invoiceproductmaster ipm "
						+ " JOIN invoiceproducttype ipt ON ipm.ntypecode = ipt.ntypecode "
						+ " JOIN transactionstatus ts ON ts.ntranscode = ipm.ntaxavailable " + " WHERE ipm.ntypecode = "
						+ ntypecode + " and ipm.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " and ipm.nsitecode="+userInfo.getNmastersitecode()+""
						+ " and ipt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ipt.nsitecode="+userInfo.getNmastersitecode()+""
						+ " ORDER BY ipm.nproductcode desc";

			}

		} else {
			strQuery = "select * from invoiceproductmaster where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode ="+userInfo.getNmastersitecode();
		}
		List<InvoiceProductMaster> lstProduct = jdbcTemplate.query(strQuery, new InvoiceProductMaster());
		return new ResponseEntity<>(lstProduct, HttpStatus.OK);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getInvoiceByProduct(final int nproductcode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final InvoiceProductType lstProductType = getActiveInvoiceProductTypeById(nproductcode, objUserInfo);
		int stext = 0;
		int stext2 = 0;
		if (lstProductType != null) {
			map.put("SelectedProductType", lstProductType);
		} else {
			map.put("SelectedProductType", null);
		}
		final List<InvoiceProductMaster> lstProductMaster = (List<InvoiceProductMaster>) getProductbyProductMaster(
				lstProductType.getNtypecode(), objUserInfo.getNmastersitecode(), 0, objUserInfo).getBody();
		map.put("ProductMaster", lstProductMaster);
		if (!lstProductMaster.isEmpty()) {
			InvoiceProductMaster invoiceproductmaster = getActiveInvoiceProductMasterById(nproductcode, objUserInfo);
			map.put("SelectedProductMaster", invoiceproductmaster);

			String st = "select * from userrolefieldcontrol where nformcode= " + objUserInfo.getNformcode()
					+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nneedrights=3 ";
			List<UserRoleFieldControl> listTest = (List<UserRoleFieldControl>) jdbcTemplate.query(st,
					new UserRoleFieldControl());
			final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest, "getNfieldcode");
			if (listTest.size() == 0) {
				Map<String, Integer> ProductDetails = new HashMap<>();
				int defaultValue = +Enumeration.TransactionStatus.NO.gettransactionstatus();

				ProductDetails.put("stext", defaultValue);
				ProductDetails.put("stext2", defaultValue);

			}

			else {

				String std = "select * from fieldmaster where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "nfieldcode in ("
						+ sntestgrouptestcode + ")";

				List<FieldMaster> listTest2 = (List<FieldMaster>) jdbcTemplate.query(std, new FieldMaster());

				Set<String> fieldNames = listTest2.stream().map(FieldMaster::getSfieldname).collect(Collectors.toSet());
				int defaultValue = +Enumeration.TransactionStatus.YES.gettransactionstatus();
				int value = +Enumeration.TransactionStatus.NO.gettransactionstatus();
				stext = fieldNames.contains("saddtext1") ? defaultValue : value;
				stext2 = fieldNames.contains("saddtext2") ? defaultValue : value;
			}
			List<TaxProductDetails> lstTaxProduct = getTaxProductDetails(objUserInfo, nproductcode);
			if (lstTaxProduct.size() > 0) {
				map.put("selectedTaxProduct", lstTaxProduct.get(lstTaxProduct.size() - 1));
				map.put("TaxProduct", lstTaxProduct);
				map.put("Stext", stext);
				map.put("Stext2", stext2);
			} else {
				map.put("TaxProduct", null);
				map.put("Stext", stext);
				map.put("Stext2", stext2);
			}
		} else {
			map.put("TaxProduct", Arrays.asList());
			map.put("SelectedProductMaster", null);
			map.put("selectedTaxProduct", null);
			map.put("Stext", stext);
			map.put("Stext2", stext2);

		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getproductByDel(final int ntypecode, final UserInfo objUserInfo) throws Exception {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final Map<String, Object> catfiltermap = new LinkedHashMap<String, Object>();
		final InvoiceProductType lstProductType = getProductTypeByproducttype(ntypecode,objUserInfo);
		if (lstProductType != null) {
			map.put("SelectedProductType", lstProductType);
			catfiltermap.put("value", lstProductType.getNtypecode());
			catfiltermap.put("label", lstProductType.getStypename());
			map.put("nfilterProductType", catfiltermap);
		} else {
			map.put("SelectedProductType", null);
		}
		final List<InvoiceProductMaster> lstproduct = (List<InvoiceProductMaster>) getProductbyProductMaster(ntypecode,
				objUserInfo.getNmastersitecode(), 0, objUserInfo).getBody();
		map.put("ProductMaster", lstproduct);
		if (!lstproduct.isEmpty()) {
			map.put("SelectedProductMaster", lstproduct.get(lstproduct.size() - 1));
		} else {
			map.put("SelectedProductMaster", null);

		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	private InvoiceProductType getProductTypeByproducttype(int ntypecode,UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		final String query = "select * from invoiceproducttype where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntypecode=" + ntypecode+""
				+ " and nsitecode="+userInfo.getNmastersitecode();
		final InvoiceProductType lstProductType = (InvoiceProductType) jdbcUtilityFunction.queryForObject(query,
				InvoiceProductType.class, jdbcTemplate);

		return lstProductType;
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getSelectedProductMasterDetail(final UserInfo userInfo, final int nproductcode)
			throws Exception {
		// TODO Auto-generated method stub
		final ResponseEntity<Object> lstProductMaster = getActiveProductMasterById(nproductcode, userInfo);
		if (lstProductMaster != null) {

			Map<String, Object> objMap = new LinkedHashMap<String, Object>();
			List<TaxProductDetails> lstTaxProduct = getTaxproductDetails(userInfo, nproductcode);
			List<ProductTest> ProductTestDetails = getProductTestDetails(userInfo, nproductcode);
			objMap.putAll((Map<String, Object>) getProductMasterFile(nproductcode, userInfo).getBody());
			objMap.put("TaxProduct", lstTaxProduct);
			if (!lstTaxProduct.isEmpty()) {
				objMap.put("selectedTaxProduct", lstTaxProduct.get(lstTaxProduct.size() - 1));
			}
			if (ProductTestDetails.size() > 0) {
				objMap.put("ProductTestDetails", ProductTestDetails);

			} else {
				objMap.put("ProductTestDetails", null);

			}
			InvoiceProductMaster lstProductMasterGet = getProductMasterByIdForInsert(nproductcode, userInfo);

			objMap.put("SelectedProductMaster", lstProductMasterGet);

			return new ResponseEntity<Object>(objMap, HttpStatus.OK);
		} else {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	private InvoiceProductMaster getProductMasterByIdForInsert(final int nproductcode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select ipm.nproductcode,ipm.ntypecode,ipm.slimscode,ipm.sproductname,ipm.sdescription,ipm.sinvoicedescription,"
				+ "ipm.saddtext1,ipm.saddtext2,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,ipm.ncost from invoiceproductmaster ipm,transactionstatus ts"
				+ " where ipm.nproductcode=" + nproductcode + " and ts.ntranscode=ipm.ntaxavailable and ipm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and ipm.nsitecode="+userInfo.getNmastersitecode();

		// return (InvoiceProductMaster) jdbcQueryForObject(strQuery,
		// InvoiceProductMaster.class);
		return (InvoiceProductMaster) jdbcUtilityFunction.queryForObject(strQuery, InvoiceProductMaster.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getProductByType(int ntypecode, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final Map<String, Object> typefiltermap = new LinkedHashMap<String, Object>();
		final InvoiceProductType lstProductType = (InvoiceProductType) getProductTypeByproducttype(ntypecode,userInfo);
		if (lstProductType != null) {
			map.put("SelectedProductType", lstProductType);
			typefiltermap.put("value", lstProductType.getNtypecode());
			typefiltermap.put("label", lstProductType.getStypename());
			map.put("filterProductType", typefiltermap);
		} else {
			map.put("SelectedProductType", null);
		}
		final List<InvoiceProductMaster> lstProductMaster = (List<InvoiceProductMaster>) getProductbyProductMaster(
				ntypecode, userInfo.getNmastersitecode(), 0, userInfo).getBody();
		map.put("ProductMaster", lstProductMaster);
		if (!lstProductMaster.isEmpty()) {
			map.put("SelectedProductMaster", lstProductMaster.get(lstProductMaster.size() - 1));
			int nproductcode = lstProductMaster.get(lstProductMaster.size() - 1).getNproductcode();
			List<TaxProductDetails> lstTaxProductGet = getTaxproductDetails(userInfo, nproductcode);
			if (lstTaxProductGet.size() > 0) {
				map.put("selectedTaxProduct", lstTaxProductGet.get(lstTaxProductGet.size() - 1));
			}
			map.putAll((Map<String, Object>) getProductMasterFile(nproductcode, userInfo).getBody());
			map.put("TaxProduct", lstTaxProductGet);
		}

		else {
			map.put("TaxProduct", Arrays.asList());
			map.put("SelectedProductMaster", null);
			map.put("selectedTaxProduct", null);

		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getproductByType(int ntypecode, UserInfo userInfo, int nallottedspeccode)
			throws Exception {
		// TODO Auto-generated method stub
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final Map<String, Object> typefiltermap = new LinkedHashMap<String, Object>();
		final InvoiceProductType lstProductType = (InvoiceProductType) getProductTypeByproducttype(ntypecode,userInfo);
		if (lstProductType != null) {
			map.put("SelectedProductType", lstProductType);
			typefiltermap.put("value", lstProductType.getNtypecode());
			typefiltermap.put("label", lstProductType.getStypename());
			map.put("filterProductType", typefiltermap);
		} else {
			map.put("SelectedProductType", null);
		}

		final List<InvoiceProductMaster> lstProductMaster = (List<InvoiceProductMaster>) getProductbyproductMaster(
				ntypecode, userInfo.getNmastersitecode(), 0, userInfo, nallottedspeccode).getBody();
		map.put("ProductMaster", lstProductMaster);
		
		if (!lstProductMaster.isEmpty()) {
			
			map.put("SelectedProductMaster", lstProductMaster.get(0));
			int nproductcode = lstProductMaster.get(lstProductMaster.size() - 1).getNproductcode();
			List<TaxProductDetails> lstTaxProductGet = getTaxproductDetails(userInfo, nproductcode);
			if (lstTaxProductGet.size() > 0) {
				map.put("selectedTaxProduct", lstTaxProductGet.get(lstTaxProductGet.size() - 1));
			}
			map.putAll((Map<String, Object>) getProductMasterFile(nproductcode, userInfo).getBody());
			map.put("TaxProduct", lstTaxProductGet);
			
		   
		}

		else {
			map.put("TaxProduct", Arrays.asList());
			map.put("SelectedProductMaster", null);
			map.put("selectedTaxProduct", null);

		}
		
		   int stext = 0;
		   int stext2 = 0;

			String st = "select * from userrolefieldcontrol where nformcode= " + userInfo.getNformcode()
					  + " and  nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and nneedrights=3 ";
//					List<UserRoleFieldControl> listTest = (List<UserRoleFieldControl>) jdbcTemplate.query(st,
//							new UserRoleFieldControl());
			final List<UserRoleFieldControl> CMAllTin = (List<UserRoleFieldControl>) jdbcTemplate.query(st,new UserRoleFieldControl());

			final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(CMAllTin, "getNfieldcode");
			if (CMAllTin.size() == 0) {
				Map<String, Integer> ProductDetails = new HashMap<>();
				int defaultValue = +Enumeration.TransactionStatus.NO.gettransactionstatus();

				map.put("Stext", defaultValue);
				map.put("Stext2", defaultValue);

			}

			else {

			String std = "select * from fieldmaster where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "nfieldcode in ("+ sntestgrouptestcode + ")";

			List<FieldMaster> listTest2 = (List<FieldMaster>) jdbcTemplate.query(std, new FieldMaster());

			Set<String> fieldNames = listTest2.stream().map(FieldMaster::getSfieldname).collect(Collectors.toSet());
			int defaultValue = +Enumeration.TransactionStatus.YES.gettransactionstatus();
			int value = +Enumeration.TransactionStatus.NO.gettransactionstatus();
			stext = fieldNames.contains("saddtext1") ? defaultValue : value;
			stext2 = fieldNames.contains("saddtext2") ? defaultValue : value;
			
			map.put("Stext", defaultValue);
			map.put("Stext2", defaultValue);
			}
		
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> getInvoiceByProduct(final UserInfo userInfo, final int nproductcode)
			throws Exception {
		// TODO Auto-generated method stub
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final InvoiceProductType lstProductType = getActiveInvoiceProductTypeById(nproductcode, userInfo);

		if (lstProductType != null) {
			map.put("SelectedProductType", lstProductType);
		} else {
			map.put("SelectedProductType", null);
		}
		final List<InvoiceProductMaster> lstProductMaster = (List<InvoiceProductMaster>) getProductbyProductMaster(
				lstProductType.getNtypecode(), userInfo.getNmastersitecode(), 0, userInfo).getBody();
		// map.put("ProductMaster", lstProductMaster);
		if (!lstProductMaster.isEmpty()) {
			InvoiceProductMaster invoiceproductmaster = getActiveInvoiceProductMasterById(nproductcode, userInfo);
			// final Client selectedClient = lstClient.get(lstClient.size()-1);
			List<ProductTest> ProductTestDetails = getProductTestDetails(userInfo, nproductcode);
			List<TaxProductDetails> lstTaxProduct = getTaxproductDetails(userInfo, nproductcode);
			if (ProductTestDetails.size() > 0) {
				map.put("ProductTestDetails", ProductTestDetails);

			} else {
				map.put("ProductTestDetails", null);
			}
			if (lstTaxProduct.size() > 0) {
				map.put("TaxProduct", lstTaxProduct);
				map.put("selectedTaxProduct", lstTaxProduct.get(lstTaxProduct.size() - 1));
			} else {
				map.put("TaxProduct", null);
			}
			map.put("SelectedProductMaster", invoiceproductmaster);

		} else {
			map.put("SelectedProductMaster", null);

		}
		
		int stext = 0;
		int stext2 = 0;
			
	    String st = "select * from userrolefieldcontrol where nformcode= " + userInfo.getNformcode()
				    + " and  nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and nneedrights=3 ";
//				List<UserRoleFieldControl> listTest = (List<UserRoleFieldControl>) jdbcTemplate.query(st,
//						new UserRoleFieldControl());
	    final List<UserRoleFieldControl> CMAllTin = (List<UserRoleFieldControl>) jdbcTemplate.query(st,new UserRoleFieldControl());

		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(CMAllTin, "getNfieldcode");
		if (CMAllTin.size() == 0) {
			Map<String, Integer> ProductDetails = new HashMap<>();
			int defaultValue = +Enumeration.TransactionStatus.NO.gettransactionstatus();

			map.put("Stext", defaultValue);
			map.put("Stext2", defaultValue);

			}

		else {

			String std = " select * from fieldmaster where nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + 
					     " and " + "nfieldcode in ("+ sntestgrouptestcode + ")";

			List<FieldMaster> listTest2 = (List<FieldMaster>) jdbcTemplate.query(std, new FieldMaster());

			Set<String> fieldNames = listTest2.stream().map(FieldMaster::getSfieldname).collect(Collectors.toSet());
			int defaultValue = +Enumeration.TransactionStatus.YES.gettransactionstatus();
			int value = +Enumeration.TransactionStatus.NO.gettransactionstatus();
			stext = fieldNames.contains("saddtext1") ? defaultValue : value;
			stext2 = fieldNames.contains("saddtext2") ? defaultValue : value;
					
			map.put("Stext", stext);
			map.put("Stext2", stext2);
		  }
		
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings({ "unused" })
	@Override
	public ResponseEntity<Object> createTaxProduct(TaxProductDetails taxproduct, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
//		final String sQuery = " lock  table lockinvoiceproductmaster " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//		getJdbcTemplate().execute(sQuery);

		final List<Object> savedTaxProductList = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();

		final InvoiceProductMaster nproductcode = (InvoiceProductMaster) getProductMasterByIdForInsert(
				taxproduct.getNproductcode(), userInfo);

		if (nproductcode == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_PRODUCTALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			String str = "select * from taxproductdetails where nproductcode=" + nproductcode.getNproductcode()
					+ " and  nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode="+userInfo.getNmastersitecode();
			List<TaxProductDetails> lsttaxProducts = (List<TaxProductDetails>) jdbcTemplate.query(str,
					new TaxProductDetails());

			String TaxProductSeq = "";
			TaxProductSeq = "select nsequenceno from seqnoinvoice where stablename='taxproductdetails'";
			int seqNo = jdbcTemplate.queryForObject(TaxProductSeq, Integer.class);
			seqNo = seqNo + 1;
			String taxProductInsert = "";
			if (taxproduct.getNtaxcode() != 0) {
				taxProductInsert = "insert into taxproductdetails(ntaxproductcode,nproductcode,ntaxcode,nactive,"
						+ "sversionno,nusercode,dmodifieddate,nsitecode,nstatus,ncaltypecode)values (" + seqNo + ","
						+ taxproduct.getNproductcode() + "," + taxproduct.getNtaxcode() + "," + taxproduct.getNactive()
						+ ",'" + taxproduct.getSversionno() + "'," + userInfo.getNusercode() + ", " + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
						+ "" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ taxproduct.getNcaltypecode() + ")";
				jdbcTemplate.execute(taxProductInsert);

				TaxProductSeq = "update seqnoinvoice set nsequenceno=" + seqNo
						+ " where stablename='taxproductdetails'";
				jdbcTemplate.execute(TaxProductSeq);
				final String strQuery = "select  ip.sproductname,it.staxname,ts.stransstatus as sdisplaystatus  from invoicetaxtype it,invoiceproductmaster ip,transactionstatus ts where "
						+ "ip.nproductcode =" + taxproduct.getNproductcode() + " and it.ntaxcode= "
						+ taxproduct.getNtaxcode() + " and ts.ntranscode=" + taxproduct.getNactive()
						+ " and it.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and it.nsitecode="+userInfo.getNmastersitecode()
						+ " and ip.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and ip.nsitecode="+userInfo.getNmastersitecode();

				List<TaxProductDetails> lsttaxProduct = (List<TaxProductDetails>) jdbcTemplate.query(strQuery,
						new TaxProductDetails());

				lsttaxProduct.get(lsttaxProduct.size() - 1).setSversionno(taxproduct.getSversionno());
				multilingualIDList.add("IDS_ADDTAXPRODUCT");
				taxproduct.setNtaxproductcode(seqNo);
				savedTaxProductList.add(lsttaxProduct.get(lsttaxProduct.size() - 1));

				auditUtilityFunction.fnInsertAuditAction(savedTaxProductList, 1, null, multilingualIDList, userInfo);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ADDTAXNAME", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
		return getInvoiceByProduct(taxproduct.getNproductcode(), userInfo);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings({ "unused" })
	private TaxProductDetails getTaxTypeByName(final String sversionno, final int nproductcode) throws Exception {
		final String strQuery = "select  sversionno from taxproductdetails where sversionno = N'"
				+ stringUtilityFunction.replaceQuote(sversionno) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and nproductcode = " + nproductcode+"";

		// return (TaxProductDetails) jdbcQueryForObject(strQuery,
		// TaxProductDetails.class);
		return (TaxProductDetails) jdbcUtilityFunction.queryForObject(strQuery, TaxProductDetails.class, jdbcTemplate);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings({ "unused" })
	private TaxProductDetails getTaxProductByDefault(final int nproductcode) throws Exception {
		final String strQuery = "select  ntaxproductcode,sversionno,nactive from taxproductdetails where "
				+ " nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and nproductcode = " + nproductcode+"";
		// return (TaxProductDetails) jdbcQueryForObject(strQuery,
		// TaxProductDetails.class);
		return (TaxProductDetails) jdbcUtilityFunction.queryForObject(strQuery, TaxProductDetails.class, jdbcTemplate);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings({ "unused" })
	private TaxProductDetails getTaxProductByEdit(final int nproductcode, int ntaxproductcode) throws Exception {
		final String strQuery = "select  ntaxproductcode,sversionno,nactive from taxproductdetails where "
				+ " nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and nproductcode = " + nproductcode + " and ntaxproductcode=" + ntaxproductcode;

		// return (TaxProductDetails) jdbcQueryForObject(strQuery,
		// TaxProductDetails.class);
		return (TaxProductDetails) jdbcUtilityFunction.queryForObject(strQuery, TaxProductDetails.class, jdbcTemplate);
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public List<TaxProductDetails> getTaxProductDetails(final UserInfo userInfo, final int nproductcode)
			throws Exception {

		final String strTaxQuery = "select tpd.ntaxproductcode,tpd.nproductcode,itt.ntax,tpd.ntaxcode,tpd.nactive,tpd.sversionno,itt.staxname,tpd.ncaltypecode "
				+ " from taxproductdetails tpd,invoiceproductmaster ipm,invoicetaxtype itt where "
				+ " ipm.nproductcode=tpd.nproductcode  and tpd.nproductcode=" + nproductcode + " "
				+ " and itt.ntaxcode=tpd.ntaxcode" + " and tpd.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ipm.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and itt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tpd.nsitecode="+userInfo.getNmastersitecode()+" and ipm.nsitecode="+userInfo.getNmastersitecode()
				+ " and itt.nsitecode="+userInfo.getNmastersitecode()
				+ " order by tpd.ntaxproductcode ";

		List<TaxProductDetails> lsttaxProduct = (List<TaxProductDetails>) jdbcTemplate.query(strTaxQuery,
				new TaxProductDetails());
		return lsttaxProduct;
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@Override
	public TaxProductDetails getTaxProductById(int nproductcode, int ntaxproductcode, UserInfo userInfo)
			throws Exception {

		String str = "select tpd.ntaxproductcode,itt.ntax,itt.staxname,tpd.nactive,tpd.sversionno,tpd.ntaxcode,tpd.ncaltypecode from taxproductdetails tpd,invoicetaxtype itt"
				+ "  where itt.ntaxcode=tpd.ntaxcode and tpd.nproductcode=" + nproductcode + " and tpd.ntaxproductcode="+ ntaxproductcode+""
				+ " and tpd.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tpd.nsitecode="+userInfo.getNmastersitecode()+""
				+ " and itt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and itt.nsitecode="+userInfo.getNmastersitecode();

		return (TaxProductDetails) jdbcUtilityFunction.queryForObject(str, TaxProductDetails.class, jdbcTemplate);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	public ResponseEntity<Object> getTaxname(UserInfo userInfo) throws Exception {

		String strQuery = "select it.staxname " + " from " + " invoicetaxtype it where  "
				+ " it.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode()+"" 
				+ " and it.ntaxcode > 0" + " group BY it.staxname";
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new InvoiceTaxtype()), HttpStatus.OK);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@SuppressWarnings({ "unused" })
	@Override
	public ResponseEntity<Object> updateTaxProduct(TaxProductDetails taxProduct, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		// final InvoiceProductMaster objProductMaster = (InvoiceProductMaster)
		// getProductMasterByIdForInsert(taxProduct.getNproductcode(), userInfo);

		final TaxProductDetails taxProductByID = (TaxProductDetails) getTaxProductById(taxProduct.getNproductcode(),
				taxProduct.getNtaxproductcode(), userInfo);
		int cal = taxProductByID.getNcaltypecode();
		int caall = taxProduct.getNcaltypecode();

		String str = "select * from taxproductdetails where nproductcode=" + taxProduct.getNproductcode()
				+ " and  nstatus="+  Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode();
		List<TaxProductDetails> lsttaxProducts = (List<TaxProductDetails>) jdbcTemplate.query(str,
				new TaxProductDetails());
		List<Short> objname = lsttaxProducts.stream().map(TaxProductDetails::getNcaltypecode)
				.collect(Collectors.toList());
		if (lsttaxProducts.size() == 1) {
			if ((cal != caall) || (cal == caall)) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> listAfterUpdate = new ArrayList<>();
				final List<Object> listBeforeUpdate = new ArrayList<>();

				final String queryString = "select sversionno from taxproductdetails where sversionno = N'"
						+ stringUtilityFunction.replaceQuote(taxProduct.getSversionno()) + "' and nproductcode = "
						+ taxProduct.getNproductcode() + " and ntaxproductcode <> " + taxProduct.getNtaxproductcode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " and nsitecode="+userInfo.getNmastersitecode();

				final String updateQueryString = "update taxproductdetails set sversionno=N'"
						+ stringUtilityFunction.replaceQuote(taxProduct.getSversionno()) + "',nproductcode= "
						+ taxProduct.getNproductcode() + ",ntaxcode= " + taxProduct.getNtaxcode() + ", nactive= "
						+ taxProduct.getNactive() + ", ncaltypecode=" + taxProduct.getNcaltypecode()
						+ ", dmodifieddate = '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
						+ " where nproductcode= " + taxProduct.getNproductcode() + " and ntaxproductcode="
						+ taxProduct.getNtaxproductcode();

				jdbcTemplate.execute(updateQueryString);

				multilingualIDList.add("IDS_EDITTAXPRODUCT");

				listAfterUpdate.add(taxProduct);

				listBeforeUpdate.add(taxProductByID);

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				final List<TaxProductDetails> lstTaxProduct = getTaxProductDetails(userInfo,
						taxProduct.getNproductcode());

				final TaxProductDetails selectedTaxProductDetails = getTaxProductById(taxProduct.getNproductcode(),
						taxProduct.getNtaxproductcode(), userInfo);

				outputMap.put("TaxProduct", lstTaxProduct);
				outputMap.put("selectedTaxProduct", selectedTaxProductDetails);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ADDDIRECT", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			}

		} else if (cal == caall) {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> listAfterUpdate = new ArrayList<>();
			final List<Object> listBeforeUpdate = new ArrayList<>();

			final String queryString = "select sversionno from taxproductdetails where sversionno = N'"
					+ stringUtilityFunction.replaceQuote(taxProduct.getSversionno()) + "' and nproductcode = "
					+ taxProduct.getNproductcode() + " and ntaxproductcode <> " + taxProduct.getNtaxproductcode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and nsitecode="+userInfo.getNmastersitecode();

			final String updateQueryString = "update taxproductdetails set sversionno=N'"
					+ stringUtilityFunction.replaceQuote(taxProduct.getSversionno()) + "',nproductcode= "
					+ taxProduct.getNproductcode() + ",ntaxcode= " + taxProduct.getNtaxcode() + ", nactive= "
					+ taxProduct.getNactive() + ", ncaltypecode=" + taxProduct.getNcaltypecode() + ", dmodifieddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nproductcode= "
					+ taxProduct.getNproductcode() + " and ntaxproductcode=" + taxProduct.getNtaxproductcode();

			jdbcTemplate.execute(updateQueryString);

			multilingualIDList.add("IDS_EDITTAXPRODUCT");

			listAfterUpdate.add(taxProduct);

			listBeforeUpdate.add(taxProductByID);

			auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
					userInfo);

			final List<TaxProductDetails> lstTaxProduct = getTaxProductDetails(userInfo, taxProduct.getNproductcode());

			final TaxProductDetails selectedTaxProductDetails = getTaxProductById(taxProduct.getNproductcode(),
					taxProduct.getNtaxproductcode(), userInfo);

			outputMap.put("TaxProduct", lstTaxProduct);
			outputMap.put("selectedTaxProduct", selectedTaxProductDetails);

		} else {
			if (taxProduct.getNcaltypecode() == 1) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TAXPRODUCTSINDIRECT",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_TAXPRODUCTSDIRECT", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}

		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	/**
	 * This method id used to delete an entry in InvoiceProductmaster table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] an Object holds the
	 *                                record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         InvoiceProductmaster object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings({ "unused" })
	@Override
	public ResponseEntity<Object> deleteTaxProduct(TaxProductDetails taxProductDetails, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		final InvoiceProductMaster objProductMaster = (InvoiceProductMaster) getProductMasterByIdForInsert(
				taxProductDetails.getNproductcode(), userInfo);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedTaxProductList = new ArrayList<>();
		
		
		String strproductname="select sproductname from invoiceproductmaster where nproductcode="+taxProductDetails.getNproductcode()+"";
		
		List<InvoiceProductMaster> ProductItems = (List<InvoiceProductMaster>) jdbcTemplate.query(strproductname, new InvoiceProductMaster());
		
		String query = "select 'IDS_TRANSACTION' as Msg from quotationitemdetails where jsondata->>'sproductname'='"
				+ stringUtilityFunction.replaceQuote(ProductItems.get(0).getSproductname()) + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and nsitecode="+userInfo.getNmastersitecode()+" union all"
				+ " select 'IDS_TRANSACTION' as Msg from invoiceproductitemdetails where jsondata->>'sproductname'='"
				+ stringUtilityFunction.replaceQuote(ProductItems.get(0).getSproductname()) + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and nsitecode="+userInfo.getNmastersitecode();
		ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);
		
		if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
			
		
		String updateQueryString = "update taxproductdetails set nstatus = "
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate = '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nproductcode="
				+ taxProductDetails.getNproductcode() + " and ntaxproductcode=" + taxProductDetails.getNtaxproductcode()
				+ ";";

		jdbcTemplate.execute(updateQueryString);

		taxProductDetails.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

		multilingualIDList.add("IDS_DELETETAXPRODUCT");
		savedTaxProductList.add(Arrays.asList(taxProductDetails));

		auditUtilityFunction.fnInsertListAuditAction(savedTaxProductList, 1, null, multilingualIDList, userInfo);
        
		}else {

			return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
		}
		
		return getInvoiceByProduct(taxProductDetails.getNproductcode(), userInfo);
	}

	/**
	 * This method is used to add a new entry to InvoiceProductmaster table. Need to
	 * check for duplicate entry of InvoiceProductmaster name for the specified site
	 * before saving into database. Need to check that there should be only one
	 * default InvoiceProductmaster for a site
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] object holding details
	 *                                to be added in InvoiceProductmaster table
	 * @return inserted InvoiceProductmaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	private List<InvoiceProductMaster> checKProductIsPresent(int nproductcode,UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		String strQuery = "select nproductcode from invoiceproductmaster where nproductcode = " + nproductcode
				+ " and  nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and nsitecode="+userInfo.getNmastersitecode();
		final List<InvoiceProductMaster> objProductMaster = (List<InvoiceProductMaster>) jdbcTemplate.query(strQuery,
				new InvoiceProductMaster());

		return objProductMaster;
	}
	/**
	 * This method is used to add a new entry to InvoiceProductmaster table. On
	 * successive insert get the new inserted record along with default status from
	 * transaction status
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster ] object holding details
	 *                                to be added in InvoiceProductmaster table
	 * @return inserted InvoiceProductmaster object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> createInvoiceProductFile(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		final ObjectMapper objMapper = new ObjectMapper();

		final List<InvoiceProductFile> lstReqProductFile = objMapper.readValue(request.getParameter("productfile"),
				new TypeReference<List<InvoiceProductFile>>() {
				});

		if (lstReqProductFile != null && lstReqProductFile.size() > 0) {
			final List<InvoiceProductMaster> objProduct = checKProductIsPresent(
					lstReqProductFile.get(0).getNproductcode(),userInfo);

			if (objProduct != null) {
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (lstReqProductFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {

					sReturnString = fTPUtilityFunction.getFileFTPUpload(request, -1, userInfo); // Folder Name - master
				}

				if (sReturnString.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {

					final Instant instantDate = dateUtilityFunction.getCurrentDateTime(userInfo)
							.truncatedTo(ChronoUnit.SECONDS);
					final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
					final int noffset = dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid());

					lstReqProductFile.forEach(objtf -> {

						objtf.setDcreateddate(instantDate);

						if (objtf.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {

							objtf.setDcreateddate(instantDate);
							objtf.setNoffsetdcreateddate(noffset);
							objtf.setScreateddate(sattachmentDate.replace("T", " "));
						}

					});

					String sequencequery = "select nsequenceno from seqnoinvoice where stablename ='invoiceproductfile'";
					int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
					nsequenceno++;
					String insertquery = "Insert into invoiceproductfile(nproductmasterfilecode,nproductcode,nlinkcode,nattachmenttypecode,sfilename,sdescription,nfilesize,dcreateddate,noffsetdcreateddate,ntzcreateddate,ssystemfilename,dmodifieddate,nsitecode,nstatus)"
							+ "values (" + nsequenceno + "," + lstReqProductFile.get(0).getNproductcode() + ","
							+ lstReqProductFile.get(0).getNlinkcode() + ","
							+ lstReqProductFile.get(0).getNattachmenttypecode() + "," + " N'"
							+ stringUtilityFunction.replaceQuote(lstReqProductFile.get(0).getSfilename()) + "',N'"
							+ stringUtilityFunction.replaceQuote(lstReqProductFile.get(0).getSdescription()) + "',"
							+ lstReqProductFile.get(0).getNfilesize() + "," + " '"
							+ lstReqProductFile.get(0).getDcreateddate() + "',"
							+ lstReqProductFile.get(0).getNoffsetdcreateddate() + "," + userInfo.getNtimezonecode()
							+ ",N'" + lstReqProductFile.get(0).getSsystemfilename() + "','"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
							+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

					jdbcTemplate.execute(insertquery);
//

					String updatequery = "update seqnoinvoice set nsequenceno =" + nsequenceno
							+ " where stablename ='invoiceproductfile'";
					jdbcTemplate.execute(updatequery);

					final List<String> multilingualIDList = new ArrayList<>();

					multilingualIDList
							.add(lstReqProductFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP
									.gettype() ? "IDS_ADDPRODUCTFILE" : "IDS_ADDPRODUCTLINK");
					final List<Object> listObject = new ArrayList<Object>();

					String auditqry = "select * from invoiceproductfile where nproductcode = "
							+ lstReqProductFile.get(0).getNproductcode() + " and nproductmasterfilecode = "
							+ nsequenceno + " and nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode="+userInfo.getNmastersitecode();

					List<InvoiceProductFile> lstvalidate = (List<InvoiceProductFile>) jdbcTemplate.query(auditqry,
							new InvoiceProductFile());
					listObject.add(lstvalidate);

					auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, multilingualIDList, userInfo);
				} else {
					// status code:417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PRODUCTALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			return (getProductMasterFile(lstReqProductFile.get(0).getNproductcode(), userInfo));
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to update entry in InvoiceProductmaster table. Need to
	 * validate that the InvoiceProductmaster object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * InvoiceProductmaster name for the specified site before saving into database.
	 * Need to check that there should be only one default InvoiceProductmaster for
	 * a site
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] object holding details
	 *                                to be updated in InvoiceProductmaster table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceProductmaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateInvoiceProductFile(MultipartHttpServletRequest request, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		final ObjectMapper objMapper = new ObjectMapper();

		final List<InvoiceProductFile> lstProductMasterFile = objMapper.readValue(request.getParameter("productfile"),
				new TypeReference<List<InvoiceProductFile>>() {
				});
		if (lstProductMasterFile != null && lstProductMasterFile.size() > 0) {
			final InvoiceProductFile objProductMasterFile = lstProductMasterFile.get(0);
			final List<InvoiceProductMaster> objProduct = checKProductIsPresent(objProductMasterFile.getNproductcode(),userInfo);

			if (objProduct != null) {
				final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

				if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if (objProductMasterFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = fTPUtilityFunction.getFileFTPUpload(request, -1, userInfo);
					}
				}

				if (sReturnString.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
					final String sQuery = "select * from invoiceproductfile where nproductmasterfilecode = "
							+ objProductMasterFile.getNproductmasterfilecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode();
					final InvoiceProductFile objTF = (InvoiceProductFile) jdbcUtilityFunction.queryForObject(sQuery,
							InvoiceProductFile.class, jdbcTemplate);

					if (objTF != null) {
						String ssystemfilename = "";
						// Added by Ganesh on 21/11/2025 - To Resolve s3 Override
						// start-ALPDJ21-132 
						if (objProductMasterFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							final String oldFileExtension = objTF.getSfilename().contains(".") ? 
									objTF.getSfilename().substring(objTF.getSfilename().lastIndexOf('.') + 1) : "";
							String newFileExtension = objProductMasterFile.getSfilename().contains(".") ? 
									objProductMasterFile.getSfilename().substring(objProductMasterFile.getSfilename().lastIndexOf('.') + 1) : "";
							if(!oldFileExtension.equals(newFileExtension)) {
								fTPUtilityFunction.deleteFTPFile(Arrays.asList(objTF.getSsystemfilename()), "", userInfo);
							}
							ssystemfilename = objProductMasterFile.getSsystemfilename();
							//end-ALPDJ21-132
						}

						final String sUpdateQuery = "update invoiceproductfile set sfilename=N'"
								+ stringUtilityFunction.replaceQuote(objProductMasterFile.getSfilename()) + "',"
								+ " sdescription=N'"
								+ stringUtilityFunction.replaceQuote(objProductMasterFile.getSdescription())
								+ "', ssystemfilename= N'" + ssystemfilename + "'," + " nattachmenttypecode = "
								+ objProductMasterFile.getNattachmenttypecode() + ", nlinkcode="
								+ objProductMasterFile.getNlinkcode() + "," + " nfilesize = "
								+ objProductMasterFile.getNfilesize() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nproductmasterfilecode = "
								+ objProductMasterFile.getNproductmasterfilecode();
						objProductMasterFile.setDcreateddate(((InvoiceProductFile) objTF).getDcreateddate());
						jdbcTemplate.execute(sUpdateQuery);

						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<Object>();
						multilingualIDList
								.add(objProductMasterFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
										.gettype() ? "IDS_EDITPRODUCTFILE" : "IDS_EDITPRODUCTLINK");
						lstOldObject.add(objTF);

						auditUtilityFunction.fnInsertAuditAction(lstProductMasterFile, 2, lstOldObject,
								multilingualIDList, userInfo);

						return (getProductMasterFile(objProductMasterFile.getNproductcode(), userInfo));
					} else {
						// status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					// status code:417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CLIENTALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method id used to delete an entry in InvoiceProductmaster table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as
	 * 'testparameter','testgrouptestparameter','transactionsampleresults'
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] an Object holds the
	 *                                record to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         InvoiceProductmaster object
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteInvoiceProductFile(InvoiceProductFile objProductFile, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		final List<InvoiceProductMaster> productmaster = checKProductIsPresent(objProductFile.getNproductcode(),userInfo);
		if (productmaster != null) {
			if (objProductFile != null) {
				final String sQuery = "select * from invoiceproductfile where nproductmasterfilecode = "
						+ objProductFile.getNproductmasterfilecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
						+ " and nsitecode="+userInfo.getNmastersitecode();
				final InvoiceProductFile objTF = (InvoiceProductFile) jdbcUtilityFunction.queryForObject(sQuery,
						InvoiceProductFile.class, jdbcTemplate);

				if (objTF != null) {
					if (objProductFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {

					} else {
						objProductFile.setScreateddate(null);
					}
					// Added by Ganesh on 21/11/2025 - ALPDJ21-132 - Delete in S3
					fTPUtilityFunction.deleteFTPFile(Arrays.asList(objTF.getSsystemfilename()), "", userInfo);

					final String sUpdateQuery = "update invoiceproductfile set" + "  dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ", nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ " where nproductmasterfilecode = " + objProductFile.getNproductmasterfilecode();
					jdbcTemplate.execute(sUpdateQuery);
					
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> lstObject = new ArrayList<>();
					multilingualIDList
							.add(objProductFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
									? "IDS_DELETEPRODUCTFILE"
									: "IDS_DELETEPRODUCTLINK");
					lstObject.add(objProductFile);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, userInfo);
				} else {
					// status code:417
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			}
			return getProductMasterFile(objProductFile.getNproductcode(), userInfo);
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_PRODUCTALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to update entry in InvoiceProductmaster table. Need to
	 * validate that the InvoiceProductmaster object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * InvoiceProductmaster name for the specified site before saving into database.
	 * Need to check that there should be only one default InvoiceProductmaster for
	 * a site
	 * 
	 * @param objInvoiceProductmaster [InvoiceProductmaster] object holding details
	 *                                to be updated in InvoiceProductmaster table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceProductmaster object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> editInvoiceProductFile(InvoiceProductFile objProductFile, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		final String sEditQuery = "select tf.nproductmasterfilecode, tf.nproductcode, tf.nlinkcode, tf.nattachmenttypecode, tf.sfilename, tf.sdescription, tf.nfilesize,"
				+ " tf.ssystemfilename,  lm.jsondata->>'slinkname' as slinkname"
				+ " from invoiceproductfile tf, linkmaster lm where lm.nlinkcode = tf.nlinkcode" + " and tf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tf.nsitecode="+userInfo.getNmastersitecode()+"" 
				+ " and lm.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and lm.nsitecode="+userInfo.getNmastersitecode()+"" 
				+ " and tf.nproductmasterfilecode = "+ objProductFile.getNproductmasterfilecode();
		final InvoiceProductFile objTF = (InvoiceProductFile) jdbcUtilityFunction.queryForObject(sEditQuery,
				InvoiceProductFile.class, jdbcTemplate);
		if (objTF != null) {
			return new ResponseEntity<Object>(objTF, HttpStatus.OK);
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public Map<String, Object> viewAttachedInvoiceProductFile(final InvoiceProductFile objProductFile,
			final UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> map = new HashMap<>();
		
		final List<InvoiceProductMaster> objProductMaster = checKProductIsPresent(objProductFile.getNproductcode(), userInfo);
	
		
		if (objProductMaster != null) {

			String sQuery = "select * from invoiceproductfile where nproductmasterfilecode = "
					+ objProductFile.getNproductmasterfilecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			
			final InvoiceProductFile objTF = (InvoiceProductFile) jdbcUtilityFunction.queryForObject(sQuery, InvoiceProductFile.class,jdbcTemplate);
			if (objTF != null) {
				
				
					if (objTF.getNattachmenttypecode()== Enumeration.AttachmentType.FTP.gettype()) {
						map = fTPUtilityFunction.FileViewUsingFtp(objTF.getSsystemfilename(), -1, userInfo, "", "");
					} 
				
				} else {
					sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="
							+ ((InvoiceProductFile) objTF).getNlinkcode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ userInfo.getNmastersitecode();
					final LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery,
							LinkMaster.class, jdbcTemplate);

					map.put("AttachLink",
							objlinkmaster.getSlinkname() + ((InvoiceProductFile) objTF).getSfilename());
					objProductFile.setScreateddate(null);
				}
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> lstObject = new ArrayList<>();
				multilingualIDList
						.add(objProductFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
								? "IDS_VIEWPRODUCTFILE"
								: "IDS_VIEWPRODUCTLINK");
				lstObject.add(objProductFile);
				auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, userInfo);
			} else {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()));
				return map;
			}
		
		return map;
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getProductTests(UserInfo userinfo) {

		String Str = "select nproducttestcode,sspecname,sproducttestname,ntestcost from producttest where nproducttestcode=1 "
				+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nmastersitecode="+userinfo.getNmastersitecode();
		return new ResponseEntity<Object>(jdbcTemplate.query(Str, new InvoiceProductMaster()), HttpStatus.OK);
	}

	private List<ProductTest> getProductTestDetails(UserInfo userinfo, int nproductcode) {

		final String strQuery = " select * from invoiceproductmaster ipm where " + " nproductcode = " + nproductcode
				+ "" + " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and ipm.nsitecode="+userinfo.getNmastersitecode();

		List<InvoiceProductMaster> lstProductmaster = (List<InvoiceProductMaster>) jdbcTemplate.query(strQuery,
				new InvoiceProductMaster());

		final String strQueryprodut = " select pt.nproducttestcode,ipm.nlimsproduct,pt.sspecname,pt.sproducttestname,pt.ntestcost from producttest pt,invoiceproductmaster ipm where "
				+ " ipm.nproductcode = " + nproductcode
				+ " and pt.nlimsproductcode=ipm.nlimsproduct and pt.sproductname='"
				+ lstProductmaster.get(0).getSproductname() + "'" + " and ipm.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and ipm.nsitecode="+userinfo.getNmastersitecode()+"  and pt. nstatus="
			    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and pt.nmastersitecode="+userinfo.getNmastersitecode();

		List<ProductTest> ProductTestDetails = (List<ProductTest>) jdbcTemplate.query(strQueryprodut,
				new ProductTest());

		return ProductTestDetails;
	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getSearchFieldData(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		final int ntaxcode = (int) inputMap.get("Versionno");

		String strQuery = "select * from invoicetaxtype ic where ic.ntaxcode=" + ntaxcode + " and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userInfo.getNmastersitecode();

		List<InvoiceTaxtype> customerLists = (List<InvoiceTaxtype>) jdbcTemplate.query(strQuery, new InvoiceTaxtype());
		int nversionnocode = customerLists.get(0).getNversionnocode();
		String str = "select *, coalesce(jsondata->'sversionno'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " jsondata->'sversionno'->>'en-US') as sversionno from invoiceversionno where nversionnocode="
				+ nversionnocode + " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" nsitecode="+userInfo.getNmastersitecode();
		final List<InvoiceVersionNo> customerList = (List<InvoiceVersionNo>) jdbcTemplate.query(str,
				new InvoiceVersionNo());

		return new ResponseEntity<>(customerList, HttpStatus.OK);

	}

	/**
	 * This method is used to fetch the active InvoiceProductmaster objects for the
	 * specified InvoiceProductmaster name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductmaster
	 * @param nmasterSiteCode [int] site code of the InvoiceProductmaster
	 * @return list of active InvoiceProductmaster code(s) based on the specified
	 *         InvoiceProductmaster name and site
	 * @throws Exception
	 */

	public ResponseEntity<Object> getTaxtype(UserInfo userInfo, int Productcode, Map<String, Object> inputMap)
			throws Exception {
		String str = "select * from taxproductdetails where nproductcode=" + Productcode + " and  nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and nsitecode="+userInfo.getNmastersitecode();
		List<TaxProductDetails> lsttaxProduct = (List<TaxProductDetails>) jdbcTemplate.query(str,
				new TaxProductDetails());
		int Caltypecode = 0;
		if (inputMap.containsKey("ntaxproductcode")) {
			int TaxProductcode = (int) inputMap.get("ntaxproductcode");
			String strq = "select * from taxproductdetails where ntaxproductcode=" + TaxProductcode + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and nsitecode="+userInfo.getNmastersitecode();
			List<TaxProductDetails> lsttaxProducttax = (List<TaxProductDetails>) jdbcTemplate.query(strq,
					new TaxProductDetails());
			Caltypecode = lsttaxProducttax.get(0).getNcaltypecode();
		}

		String strQuery = "";
		
		String strproductname="select sproductname from invoiceproductmaster where nproductcode="+Productcode+"";
		
		List<InvoiceProductMaster> ProductItems = (List<InvoiceProductMaster>) jdbcTemplate.query(strproductname, new InvoiceProductMaster());
		
		String query = "select 'IDS_TRANSACTION' as Msg from quotationitemdetails where jsondata->>'sproductname'='"
				+ stringUtilityFunction.replaceQuote(ProductItems.get(0).getSproductname()) + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +" and nsitecode="+userInfo.getNmastersitecode()+" union all"
				+ " select 'IDS_TRANSACTION' as Msg from invoiceproductitemdetails where jsondata->>'sproductname'='"
				+ stringUtilityFunction.replaceQuote(ProductItems.get(0).getSproductname()) + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and nsitecode="+userInfo.getNmastersitecode();
		ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);
		
		if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
			
		if ((lsttaxProduct.size() == 1) && (Caltypecode == 2)) {
			strQuery = "select *, TO_CHAR(ddatefrom,'" + userInfo.getSsitedate() + "') as sdatefrom, TO_CHAR(ddateto,'"
					+ userInfo.getSsitedate() + "') as sdateto," + "coalesce(iv.jsondata->'sversionno'->>'"
					+ userInfo.getSlanguagetypecode() + "'," + "iv.jsondata->'sversionno'->>'en-US') as sversionno,"
					+ "coalesce(ict.jsondata->'staxcaltype'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ "ict.jsondata->'staxcaltype'->>'en-US') as staxcaltype"
					+ " from invoicetaxtype it,invoicetaxcaltype ict,invoiceversionno iv where "
					+ " iv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and iv.nsitecode="+userInfo.getNmastersitecode()+"" 
					+ " and ict.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and ict.nsitecode="+userInfo.getNmastersitecode()+""  
					+ " AND it.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and it.nsitecode="+userInfo.getNmastersitecode()+"" 
					+ "  and it.ncaltypecode=ict.ncaltypecode and it.ncaltypecode=2 and it.nversionnocode=iv.nversionnocode"
					+ " and it.ntaxcode > 0 and it.nsitecode = " + userInfo.getNmastersitecode()
					+ " ORDER BY staxname desc";
		} else if ((lsttaxProduct.size() >= 5) && (lsttaxProduct.get(0).getNcaltypecode() == 2)) {

			strQuery = "select *, TO_CHAR(ddatefrom,'" + userInfo.getSsitedate() + "') as sdatefrom, TO_CHAR(ddateto,'"
					+ userInfo.getSsitedate() + "') as sdateto," + "coalesce(iv.jsondata->'sversionno'->>'"
					+ userInfo.getSlanguagetypecode() + "'," + "iv.jsondata->'sversionno'->>'en-US') as sversionno,"
					+ "coalesce(ict.jsondata->'staxcaltype'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ "ict.jsondata->'staxcaltype'->>'en-US') as staxcaltype"
					+ " from invoicetaxtype it,invoicetaxcaltype ict,invoiceversionno iv where "
					+ " iv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iv.nsitecode="+userInfo.getNmastersitecode()+"" 
					+ " and ict.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ict.nsitecode="+userInfo.getNmastersitecode()+""  
					+ "  AND it.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and it.nsitecode="+userInfo.getNmastersitecode()+"" 
					+ " and ntranscode="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and it.ncaltypecode=ict.ncaltypecode and it.ncaltypecode=1 and it.nversionnocode=iv.nversionnocode"
					+ " and it.ntaxcode > 0 and it.nsitecode = " + userInfo.getNmastersitecode()
					+ " ORDER BY staxname desc";
		}

		else if ((lsttaxProduct.size() == 1) && (lsttaxProduct.get(0).getNcaltypecode() == 1)) {
			strQuery = "select *, TO_CHAR(ddatefrom,'" + userInfo.getSsitedate() + "') as sdatefrom, TO_CHAR(ddateto,'"
					+ userInfo.getSsitedate() + "') as sdateto," + "coalesce(iv.jsondata->'sversionno'->>'"
					+ userInfo.getSlanguagetypecode() + "'," + "iv.jsondata->'sversionno'->>'en-US') as sversionno,"
					+ "coalesce(ict.jsondata->'staxcaltype'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ "ict.jsondata->'staxcaltype'->>'en-US') as staxcaltype"
					+ " from invoicetaxtype it,invoicetaxcaltype ict,invoiceversionno iv where"
					+ " iv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iv.nsitecode="+userInfo.getNmastersitecode()+""
					+ " and ict.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ict.nsitecode="+userInfo.getNmastersitecode()+""
					+ " AND it.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and it.nsitecode="+userInfo.getNmastersitecode()+""
					+ " and it.ncaltypecode=ict.ncaltypecode and it.ncaltypecode=2 and it.nversionnocode=iv.nversionnocode"
					+ " and it.ntaxcode > 0 and it.nsitecode = " + userInfo.getNmastersitecode()
					+ " ORDER BY staxname desc";
		} else if ((lsttaxProduct.size() == 0) || (Caltypecode == 2)) {
			strQuery = "select *, TO_CHAR(ddatefrom,'" + userInfo.getSsitedate() + "') as sdatefrom, TO_CHAR(ddateto,'"
					+ userInfo.getSsitedate() + "') as sdateto," + "coalesce(iv.jsondata->'sversionno'->>'"
					+ userInfo.getSlanguagetypecode() + "'," + "iv.jsondata->'sversionno'->>'en-US') as sversionno,"
					+ "coalesce(ict.jsondata->'staxcaltype'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ "ict.jsondata->'staxcaltype'->>'en-US') as staxcaltype"
					+ " from invoicetaxtype it,invoicetaxcaltype ict,invoiceversionno iv where "
					+ " iv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iv.nsitecode="+userInfo.getNmastersitecode()+""
					+ " and ict.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ict.nsitecode="+userInfo.getNmastersitecode()+""
					+ " and it.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and it.nsitecode="+userInfo.getNmastersitecode()+""
					+ " and ntranscode="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and it.ncaltypecode=ict.ncaltypecode and it.ncaltypecode=2 and it.nversionnocode=iv.nversionnocode"
					+ " and it.ntaxcode > 0 and it.nsitecode = " + userInfo.getNmastersitecode()
					+ " ORDER BY staxname desc";
		} else if ((lsttaxProduct.size() == 2) || (Caltypecode == 1)) {
			strQuery = "select *, TO_CHAR(ddatefrom,'" + userInfo.getSsitedate() + "') as sdatefrom, TO_CHAR(ddateto,'"
					+ userInfo.getSsitedate() + "') as sdateto," + "coalesce(iv.jsondata->'sversionno'->>'"
					+ userInfo.getSlanguagetypecode() + "'," + "iv.jsondata->'sversionno'->>'en-US') as sversionno,"
					+ "coalesce(ict.jsondata->'staxcaltype'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ "ict.jsondata->'staxcaltype'->>'en-US') as staxcaltype"
					+ " from invoicetaxtype it,invoicetaxcaltype ict,invoiceversionno iv where "
					+ " iv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iv.nsitecode="+userInfo.getNmastersitecode()+""
					+ " and ict.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ict.nsitecode="+userInfo.getNmastersitecode()+""
					+ " AND it.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and it.nsitecode="+userInfo.getNmastersitecode()+""
					+ " and it.ncaltypecode=ict.ncaltypecode and it.ncaltypecode=1 and it.nversionnocode=iv.nversionnocode"
					+ " and it.ntaxcode > 0 and it.nsitecode = " + userInfo.getNmastersitecode()
					+ " ORDER BY staxname desc";
		} else {
			strQuery = "select *, TO_CHAR(ddatefrom,'" + userInfo.getSsitedate() + "') as sdatefrom, TO_CHAR(ddateto,'"
					+ userInfo.getSsitedate() + "') as sdateto," + "coalesce(iv.jsondata->'sversionno'->>'"
					+ userInfo.getSlanguagetypecode() + "'," + "iv.jsondata->'sversionno'->>'en-US') as sversionno,"
					+ "coalesce(ict.jsondata->'staxcaltype'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ "ict.jsondata->'staxcaltype'->>'en-US') as staxcaltype"
					+ " from invoicetaxtype it,invoicetaxcaltype ict,invoiceversionno iv where "
					+ " iv.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iv.nsitecode="+userInfo.getNmastersitecode()+""
					+ " and ict.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ict.nsitecode="+userInfo.getNmastersitecode()+""
					+ " AND it.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and it.nsitecode="+userInfo.getNmastersitecode()+""
					+ " and it.ncaltypecode=ict.ncaltypecode and it.nversionnocode=iv.nversionnocode"
					+ " and it.ntaxcode > 0 and it.nsitecode = " + userInfo.getNmastersitecode()
					+ " ORDER BY staxname desc";

		}
		
		}else {

			return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
		}
		
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new InvoiceTaxtype()), HttpStatus.OK);

	}

}
