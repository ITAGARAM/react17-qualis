package com.agaramtech.qualis.invoice.service.invoiceheader;

import java.util.Date;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.http.HttpResponse;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.configuration.model.SampleType;
import com.agaramtech.qualis.credential.model.UserFile;
import com.agaramtech.qualis.digitalsignature.model.DigitalSignature;
import com.agaramtech.qualis.emailmanagement.model.EmailUserQuery;
import com.agaramtech.qualis.global.*;
import com.agaramtech.qualis.invoice.model.FieldMaster;

import com.agaramtech.qualis.invoice.model.InvoiceCustomerMaster;
import com.agaramtech.qualis.invoice.model.InvoiceExeCustomerProducts;
import com.agaramtech.qualis.invoice.model.InvoiceHeader;
import com.agaramtech.qualis.invoice.model.InvoiceIrn;
import com.agaramtech.qualis.invoice.model.InvoicePatient;
import com.agaramtech.qualis.invoice.model.InvoiceProductItemDetails;
import com.agaramtech.qualis.invoice.model.InvoiceProductMaster;
import com.agaramtech.qualis.invoice.model.InvoiceQuotationHeader;
import com.agaramtech.qualis.invoice.model.InvoiceQuotationProduct;
import com.agaramtech.qualis.invoice.model.InvoiceSchemes;
import com.agaramtech.qualis.invoice.model.InvoiceSchemesProduct;
import com.agaramtech.qualis.invoice.model.InvoiceStatePinCode;
import com.agaramtech.qualis.invoice.model.InvoiceTaxCalculation;
import com.agaramtech.qualis.invoice.model.InvoiceTestDetails;
import com.agaramtech.qualis.invoice.model.InvoiceTransactionStatus;
import com.agaramtech.qualis.reports.model.ReportMaster;
import com.agaramtech.qualis.invoice.model.ProductTest;
import com.agaramtech.qualis.invoice.model.ProductTestDetail;
import com.agaramtech.qualis.invoice.model.TaxProductDetails;
import com.agaramtech.qualis.invoice.model.UnitsofInvoice;
import com.agaramtech.qualis.invoice.model.UserRoleFieldControl;

import com.agaramtech.qualis.registration.model.RegistrationHistory;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "InvoiceHeader" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class InvoiceHeaderDAOImpl implements InvoiceHeaderDAO {

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ReportDAOSupport reportDAOSupport;
	private final PasswordUtilityFunction passwordUtilityFunction;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final EmailDAOSupport emailDAOSupport;

	/**
	 * This interface declaration is used to get the overall InvoiceHeader with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getInvoiceHeader(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> objMap = new HashMap<String, Object>();
		final List<InvoiceHeader> invoiceList = new ArrayList<InvoiceHeader>();

		String fromDate = "";
		String toDate = "";
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String currentUIDate = (String) inputMap.get("currentdate");
		
//		if (currentUIDate == null) {
//		    currentUIDate = dateUtilityFunction.getCurrentDateTime(userInfo);
//		}

		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					currentUIDate, "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");
			objMap.put("fromDate", mapObject.get("FromDateWOUTC"));
			objMap.put("toDate", mapObject.get("ToDateWOUTC"));
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);
			objMap.put("fromDate", fromDateUI);
			objMap.put("toDate", toDateUI);
			fromDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
			toDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		}
		LocalDateTime fromDateTime = LocalDateTime.parse(fromDate);
		LocalDateTime toDateTime = LocalDateTime.parse(toDate);
		DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formattedFromDate = fromDateTime.format(myFormatObj);
		String formattedToDate = toDateTime.format(myFormatObj);

		final int needapprovalflow;
		if (userInfo.getNuserrole() != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			final String approvalFlow = "select nneedapprovalflow from userroleconfig where nuserrolecode ="
					+ userInfo.getNuserrole() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			needapprovalflow = jdbcTemplate.queryForObject(approvalFlow, Integer.class);
			objMap.put("needapprovalflow", needapprovalflow);
		}

		final String strQuery = "select cms.scolorhexcode as scolorcode,cm.scolorhexcode,ts.stransstatus as stransdisplaystatus,its.ntransactionstatus,ih.ninvoiceseqcode,ih.sinvoiceno,ih.squotationid,ih.ncustomercode,ih.sprojectcode,ih.sprocessno,ih.sschemename,ih.spackdoctrefno,"
				+ " ih.spackagerefdetails,ih.ntotalamount,ih.ntotaltaxamount,ih.ntotalfrightcharges,ins.nschemecode,ih.npaymentcode,ih.spaymentdetails,ih.nbankcode,ih.ncurrencytype,ih.nstatus,ic.ssymbol,"
				+ " ih.jsondata||json_build_object('ninvoiceseqcode',ninvoiceseqcode)::jsonb "
				+ " as jsondata,ih.jsondata->>'CustomerName' as CustomerName," + " COALESCE(TO_CHAR(dinvoicedate,'"
				+ userInfo.getSsitedate() + "'),'') as sinvoicedate," + " COALESCE(TO_CHAR(dquotationdate,'"
				+ userInfo.getSsitedate() + "'),'') as squotationdate," + " COALESCE(TO_CHAR(dprocessdate,'"
				+ userInfo.getSsitedate() + "'),'') as sprocessdate," + " COALESCE(TO_CHAR(dorderreferencedate,'"
				+ userInfo.getSsitedate() + "'),'') as sorderreferencedate," + " COALESCE(TO_CHAR(dpackdocrefdate,'"
				+ userInfo.getSsitedate() + "'),'') as spackdocrefdate"
				+ " from invoiceheader ih,colormaster cms,invoicecurrencytype ic,"
				+ "invoicetransactionstatus its ,transactionstatus ts,colormaster cm,formwisestatuscolor fsc,invoiceschemes ins where its.sreferenceid=ih.sinvoiceno and ts.ntranscode=its.ntransactionstatus "
				+ " and cm.ncolorcode=fsc.ncolorcode and ts.ntranscode=fsc.ntranscode and  ih.ncurrencytype=ic.ncurrencycode and ih.sschemename=ins.sschemename and  fsc.nformcode="
				+ userInfo.getNformcode() + " and cms.ncolorcode= "
				+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()
				+ " and ninvoiceseqcode>0 and ih.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ih.nsitecode=" + userInfo.getNmastersitecode() + " and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fsc.nsitecode="
				+ userInfo.getNmastersitecode() + " and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nsitecode="
				+ userInfo.getNmastersitecode() + " and ins.ntransactionstatus IN ("
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ") and ih.dinvoicedate between '"
				+ formattedFromDate + "' and '" + formattedToDate + "' order by ninvoiceseqcode ";
		// + ") order by ninvoiceseqcode ";

		final List<InvoiceHeader> headerList = jdbcTemplate.query(strQuery, new InvoiceHeader());

		objMap.put("Invoice", headerList);
		if (!headerList.isEmpty()) {
						
			if("Edit".equals(inputMap.get("actiontype")) || "delete".equals(inputMap.get("actiontype"))
					|| "update".equals(inputMap.get("actiontype"))) {
				String invoiceseq=(String) inputMap.get("sinvoiceseqno");
				List<InvoiceHeader> selectedList= getInvoiceHeader(invoiceseq,formattedFromDate,formattedToDate,userInfo);
				objMap.put("selectedInvoice", selectedList);
						
				List<InvoiceProductItemDetails> productList = getProductsforInvoiceInsert(invoiceseq, userInfo);
				objMap.put("selectedProduct", productList);
				
				List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(invoiceseq, userInfo);
				objMap.put("selectedTax", taxList);
				
			}else {
				
				invoiceList.add(headerList.get(headerList.size() - 1));
				objMap.put("selectedInvoice", invoiceList);
				
				List<InvoiceProductItemDetails> productList = getProductsforInvoiceInsert(
						headerList.get(headerList.size() - 1).getSinvoiceno(), userInfo);
				objMap.put("selectedProduct", productList);
				
				List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(
						headerList.get(headerList.size() - 1).getSinvoiceno(), userInfo);
				objMap.put("selectedTax", taxList);
			}
					
		} else {
			objMap.put("selectedInvoice", headerList);
		}

		final int npackagerefdetails;
		final int ntotalfrightchanges;
		final int norderrefno;
		final int packdocref;
		final int packdocrefdate;
		final int nproducttestcode;
		int invoicepatientfield = 0;
		final String st = "select * from userrolefieldcontrol where nformcode=" + userInfo.getNformcode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nneedrights="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "";
		final List<UserRoleFieldControl> listTest = (List<UserRoleFieldControl>) jdbcTemplate.query(st,
				new UserRoleFieldControl());
//		final String sntestgrouptestcode = projectDAOSupport.fndynamiclisttostring(listTest, "getNfieldcode");
		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest, "getNfieldcode");
		final Map<String, Integer> Invoicedetails = new HashMap<>();
		if (listTest.size() == Enumeration.TransactionStatus.ALL.gettransactionstatus()) {

			final int defaultValue = +Enumeration.TransactionStatus.NO.gettransactionstatus();
			Invoicedetails.put("npackagerefdetails", defaultValue);
			Invoicedetails.put("ntotalfrightchanges", defaultValue);
			Invoicedetails.put("norderrefno", defaultValue);
			Invoicedetails.put("packdocref", defaultValue);
			Invoicedetails.put("packdocrefdate", defaultValue);
			Invoicedetails.put("nproducttestcode", defaultValue);
			Invoicedetails.put("invoicepatientfield", defaultValue);

			objMap.put("Totalfrightchanges", Invoicedetails.get("ntotalfrightchanges"));
			objMap.put("PackageDetails", Invoicedetails.get("npackagerefdetails"));
			objMap.put("Orderrefno", Invoicedetails.get("norderrefno"));
			objMap.put("Packdoref", Invoicedetails.get("packdocref"));
			objMap.put("Packrefdate", Invoicedetails.get("packdocrefdate"));
			objMap.put("producttestcode", Invoicedetails.get("nproducttestcode"));
			objMap.put("PatientDetails", Invoicedetails.get("invoicepatientfield"));
		} else {
			final String std = "select * from fieldmaster where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "nfieldcode in ("
					+ sntestgrouptestcode + ")";

			final List<FieldMaster> fieldlistTest = (List<FieldMaster>) jdbcTemplate.query(std, new FieldMaster());

			final Set<String> fieldNames = fieldlistTest.stream().map(FieldMaster::getSfieldname)
					.collect(Collectors.toSet());
			final int defaultValue = +Enumeration.TransactionStatus.YES.gettransactionstatus();
			final int value = +Enumeration.TransactionStatus.NO.gettransactionstatus();
			norderrefno = fieldNames.contains("sorderreferenceno") ? defaultValue : value;
			packdocref = fieldNames.contains("spackdoctrefno") ? defaultValue : value;
			packdocrefdate = fieldNames.contains("dpackdocrefdate") ? defaultValue : value;
			npackagerefdetails = fieldNames.contains("spackagerefdetails") ? defaultValue : value;
			ntotalfrightchanges = fieldNames.contains("ntotalfrightcharges") ? defaultValue : value;
			nproducttestcode = fieldNames.contains("nproducttestcode") ? defaultValue : value;
			invoicepatientfield = fieldNames.contains("invoicepatientfield") ? defaultValue : value;

			objMap.put("Totalfrightchanges", ntotalfrightchanges);
			objMap.put("PackageDetails", npackagerefdetails);
			objMap.put("Orderrefno", norderrefno);
			objMap.put("Packdoref", packdocref);
			objMap.put("Packrefdate", packdocrefdate);
			objMap.put("producttestcode", nproducttestcode);

			objMap.put("PatientDetails", invoicepatientfield);

		}

		objMap.put("Invoice", headerList);

		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}
	
	





	private List<InvoiceHeader> getInvoiceHeader(String invoiceseq, String formattedFromDate, String formattedToDate,
			UserInfo userInfo) {
		final String strQuery = "select cms.scolorhexcode as scolorcode,cm.scolorhexcode,ts.stransstatus as stransdisplaystatus,its.ntransactionstatus,ih.ninvoiceseqcode,ih.sinvoiceno,ih.squotationid,ih.ncustomercode,ih.sprojectcode,ih.sprocessno,ih.sschemename,ih.spackdoctrefno,"
				+ " ih.spackagerefdetails,ih.ntotalamount,ih.ntotaltaxamount,ih.ntotalfrightcharges,ins.nschemecode,ih.npaymentcode,ih.spaymentdetails,ih.nbankcode,ih.ncurrencytype,ih.nstatus,ic.ssymbol,"
				+ " ih.jsondata||json_build_object('ninvoiceseqcode',ninvoiceseqcode)::jsonb "
				+ " as jsondata,ih.jsondata->>'CustomerName' as CustomerName," + " COALESCE(TO_CHAR(dinvoicedate,'"
				+ userInfo.getSsitedate() + "'),'') as sinvoicedate," + " COALESCE(TO_CHAR(dquotationdate,'"
				+ userInfo.getSsitedate() + "'),'') as squotationdate," + " COALESCE(TO_CHAR(dprocessdate,'"
				+ userInfo.getSsitedate() + "'),'') as sprocessdate," + " COALESCE(TO_CHAR(dorderreferencedate,'"
				+ userInfo.getSsitedate() + "'),'') as sorderreferencedate," + " COALESCE(TO_CHAR(dpackdocrefdate,'"
				+ userInfo.getSsitedate() + "'),'') as spackdocrefdate"
				+ " from invoiceheader ih,colormaster cms,invoicecurrencytype ic,"
				+ "invoicetransactionstatus its ,transactionstatus ts,colormaster cm,formwisestatuscolor fsc,invoiceschemes ins where its.sreferenceid=ih.sinvoiceno and ts.ntranscode=its.ntransactionstatus "
				+ " and cm.ncolorcode=fsc.ncolorcode and ts.ntranscode=fsc.ntranscode and  ih.ncurrencytype=ic.ncurrencycode and ih.sschemename=ins.sschemename and  fsc.nformcode="
				+ userInfo.getNformcode() + " and cms.ncolorcode= "
				+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()
				+ " and ninvoiceseqcode>0 and ih.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ih.nsitecode=" + userInfo.getNmastersitecode() + " and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fsc.nsitecode="
				+ userInfo.getNmastersitecode() + " and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nsitecode="
				+ userInfo.getNmastersitecode() + " and ins.ntransactionstatus IN ("
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ") and ih.dinvoicedate between '"
				+ formattedFromDate + "' and '" + formattedToDate + "' "
				+ " and ih.sinvoiceno='"+invoiceseq+"' order by ninvoiceseqcode ";
		List<InvoiceHeader> headerList = jdbcTemplate.query(strQuery, new InvoiceHeader());
		return headerList;
	}







	/**
	 * This interface declaration is used to Products TaxCalculation with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	private List<InvoiceTaxCalculation> getProductsTaxCalculation(final String sinvoiceseqno, final UserInfo userInfo) {

		final String str = "select jsondata->>'staxname' AS staxname,jsondata->>'ntaxpercentage' AS ntaxpercentage,SUM((jsondata->>'ntaxvalue')::numeric) AS ntaxamount"
				+ " from invoiceproductitemdetails where sinvoiceseqno='" + sinvoiceseqno + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + ""
				+ " group by jsondata->>'staxname', jsondata->>'ntaxpercentage' having "
				+ "jsondata->>'staxname' IS NOT NULL " + "and jsondata->>'staxname' <> '' "
				+ "and (jsondata->>'ntaxpercentage')::numeric <> 0.00 "
				+ "and SUM((jsondata->>'ntaxvalue')::numeric) <> 0.00 union all "
				+ "select qid.jsondata->>'sindirecttaxname' AS staxname,CONCAT(qid.jsondata->>'nindirectax'::text, '.00') AS ntaxpercentage,"
				+ "SUM(CAST((qid.jsondata->>'nindirecttaxamount') AS decimal(10,2))::numeric) AS ntaxamount "
				+ "from invoiceproductitemdetails qid where qid.sinvoiceseqno='" + sinvoiceseqno + "' and qid.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qid.nsitecode="
				+ userInfo.getNmastersitecode() + ""
				+ " and qid.jsondata->>'sindirecttaxname' IS NOT NULL group by qid.jsondata->>'sindirecttaxname',qid.jsondata->>'nindirectax'";

		final List<InvoiceTaxCalculation> listOutput = (List<InvoiceTaxCalculation>) jdbcTemplate.query(str,
				new InvoiceTaxCalculation());
		if (!listOutput.isEmpty()) {
			for (InvoiceTaxCalculation obj : listOutput) {
				final String strtax = "select coalesce(jsondata->'staxcaltype'->>'" + userInfo.getSlanguagetypecode()
						+ "'," + " jsondata->'staxcaltype'->>'en-US') as staxtype "
						+ " from invoicetaxcaltype where ncaltypecode=("
						+ " select ncaltypecode from invoicetaxtype where staxname='" + obj.getStaxname()
						+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNmastersitecode() + ")";

				final List<String> result = jdbcTemplate.query(strtax, (rs, rowNum) -> rs.getString("staxtype"));

				final String taxtype = (result != null && !result.isEmpty()) ? result.get(0) : "Direct";
				obj.setStaxtype(taxtype);
			}
		}

		return listOutput;
	}

	/**
	 * This interface declaration is used to get Products Invoice with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public List<InvoiceProductItemDetails> getProductsforInvoiceInsert(final String sinvoiceseqno,
			final UserInfo userInfo) throws Exception {

		final String strQuery = "SELECT jsondata1::jsonb AS jsondata1, "
				+ "ninvoiceproductitemdetailscode, sinvoiceseqno, "
				+ "COALESCE(NULLIF(jsondata->>'nindirectax', ''), '0') AS nindirectax, "
				+ "COALESCE(jsondata->>'sindirecttaxname', '') AS sindirecttaxname, "
				+ "COALESCE(jsondata->>'squotationno', '') AS squotationno, "
				+ "COALESCE(NULLIF(jsondata->>'noverallcostvalue', ''), '0') AS noverallcostvalue, "
				+ "COALESCE(jsondata->>'sproductname', '') AS sproductname, "
				+ "COALESCE(NULLIF(jsondata->>'nunit', ''), '0') AS nunit, "
				+ "COALESCE(NULLIF(jsondata->>'nquantity', ''), '0') AS nquantity, "
				+ "COALESCE(NULLIF(jsondata->>'ncost', ''), '0') AS ncost, "
				+ "COALESCE(NULLIF(jsondata->>'ndiscountpercentage', ''), '0') AS ndiscountpercentage, "
				+ "COALESCE(jsondata->>'staxname', '') AS staxname, "
				+ "COALESCE(NULLIF(jsondata->>'slno', ''), '0') AS slno, "
				+ "COALESCE(NULLIF(jsondata->>'ntotalcost', ''), '0') AS ntotalcost, "
				+ "COALESCE(NULLIF(jsondata->>'nproductcode', ''), '0') AS nproductcode, "
				+ "COALESCE(jsondata->>'squotationseqno', '') AS squotationseqno, "
				+ "COALESCE(NULLIF(jsondata->>'ntaxpercentage', ''), '0') AS ntaxpercentage, "
				+ "COALESCE(NULLIF(jsondata->>'noverallcost', ''), '0') AS noverallcost, "
				+ "COALESCE(NULLIF(jsondata->>'ntaxamount', ''), '0') AS ntaxamount, "
				+ "COALESCE(NULLIF(jsondata->>'ntaxvalue', ''), '0') AS ntaxvalue, "
				+ "COALESCE(jsondata->>'squotationseqno', '') AS squotationseqno "
				+ "FROM invoiceproductitemdetails  where sinvoiceseqno='" + sinvoiceseqno + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + "" + " order by ninvoiceproductitemdetailscode";
		final List<InvoiceProductItemDetails> filteredList = (List<InvoiceProductItemDetails>) jdbcTemplate
				.query(strQuery, new InvoiceProductItemDetails());

		return filteredList;
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
	@Override
	public ResponseEntity<Object> createInvoice(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<String> multilingualIDListProduct = new ArrayList<>();
		final List<Object> savedUnitList = new ArrayList<>();
		final JSONObject actionType = new JSONObject();

		final JSONObject jsonObject = new JSONObject(inputMap.get("invoiceJson").toString());
		final JSONObject taxjsonObject = new JSONObject(inputMap.get("taxJson").toString());

		int seqnoinvoice = jdbcTemplate.queryForObject(
				"select nsequenceno from " + " seqnoinvoice where stablename='invoiceheader'", Integer.class);
		seqnoinvoice++;

		final String strformat = projectDAOSupport.getSeqfnFormat("invoiceheader", "seqnoformatgeneratorinvoice", 0, 0,
				userInfo);
		final String insmat = " INSERT INTO invoiceheader(ninvoiceseqcode,sinvoiceno,dinvoicedate,squotationid,dquotationdate,"
				+ "ncustomercode,sprojectcode,sprocessno,dprocessdate,sschemename,dorderreferencedate,spackdoctrefno,dpackdocrefdate,"
				+ "spackagerefdetails,ntotalamount,ntotaltaxamount,ntotalfrightcharges,npaymentcode,spaymentdetails,nbankcode,ncurrencytype,nproducttestcode,jsondata,jsondata1,"
				+ "dmodifieddate,nsitecode,nstatus,nusercode)" + " VALUES (" + seqnoinvoice + ", '" + strformat + "','"
				+ inputMap.get("dinvoicedate") + "','QID'," + "'" + inputMap.get("dquotationdate") + "' ,"
				+ inputMap.get("ncustomercode") + ",'" + inputMap.get("sprojectcode") + "','"
				+ inputMap.get("sprocessno") + "','" + inputMap.get("dprocessdate") + "','"
				+ inputMap.get("sschemename") + "','" + inputMap.get("dorderreferencedate") + "','"
				+ inputMap.get("spackdoctrefno") + "','" + inputMap.get("dpackdocrefdate") + "','"
				+ inputMap.get("spackagerefdetails") + "','" + inputMap.get("ntotalamount") + "','"
				+ inputMap.get("ntotaltaxamount") + "','" + inputMap.get("ntotalfrightcharges") + "','"
				+ inputMap.get("npaymentcode") + "','" + inputMap.get("spaymentdetails") + "',"
				+ inputMap.get("nbankcode") + ",'" + inputMap.get("ncurrencytype") + "',"
				+ inputMap.get("nproducttestcode") + ",'" + stringUtilityFunction.replaceQuote(jsonObject.toString())
				+ "'::jsonb,'" + stringUtilityFunction.replaceQuote(taxjsonObject.toString()) + "'::jsonb,'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "," + userInfo.getNusercode() + ");";

		jdbcTemplate.execute(insmat);
		jdbcTemplate
				.execute("update seqnoinvoice set nsequenceno=" + seqnoinvoice + "  where stablename='invoiceheader' ");
		createproductInvoice(inputMap, seqnoinvoice, userInfo, strformat);
		createTransactionStatusForInvoice(seqnoinvoice, userInfo, strformat);
		actionType.put("InvoiceHeader", "IDS_ADDINVOICEHEADER");
		inputMap.put("nflag", 1);
		multilingualIDList.add("IDS_ADDINVOICEHEADER");
		multilingualIDListProduct.add("IDS_ADDINVOICEHEADERPRODUCT");

		auditUtilityFunction.fnInsertAuditAction(getInvoiceforAudit(seqnoinvoice, userInfo), 1, null,
				multilingualIDList, userInfo);
		final List<InvoiceProductItemDetails> value = getInvoiceProductForAudit(seqnoinvoice, "", userInfo);
		for (InvoiceProductItemDetails obj : value) {
			savedUnitList.clear();
			savedUnitList.add(obj);
			auditUtilityFunction.fnInsertAuditAction(savedUnitList, 1, null, multilingualIDListProduct, userInfo);
		}
		return getInvoiceHeader(inputMap, userInfo);

	}

	/**
	 * This interface declaration is used to get the Invoice for Audit with respect
	 * to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public List<InvoiceHeader> getInvoiceforAudit(final int squotationseqno, final UserInfo userInfo) throws Exception {
		final String strQuery = "select sprocessno, jsondata->>'CustomerName' as CustomerName,jsondata->>'CustomerType' as CustomerType,"
				+ "jsondata->>'TotalAmountInWords' as TotalAmountInWords,"
				+ "jsondata->>'CustomerGST' as CustomerGST,jsondata->>'Address' as Address,jsondata->>'EmailId' as EmailId,"
				+ "jsondata->>'PhoneNo' as PhoneNo,jsondata->>'PAge' as PAge,jsondata->>'PDOB' as PDOB,"
				+ "jsondata->>'PEmail' as PEmail,jsondata->>'PMobileNo' as PMobileNo,jsondata->>'PatientId' as PatientId,jsondata->>'PFatherName' as PFatherName,jsondata->>'PatientName' as PatientName,"
				+ "ih.ntotalfrightcharges,ic.scurrency,iph.sproducttestdetail,ih.sschemename from invoiceheader ih,producttestdetail iph,"
				+ "invoicecurrencytype ic  where iph.nproducttestcode=ih.nproducttestcode and ih.ncurrencytype=ic.ncurrencycode and"
				+ " ih.ninvoiceseqcode = " + squotationseqno + " and ih.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ih.nsitecode="
				+ userInfo.getNmastersitecode() + "" + " and iph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iph.nmastersitecode="
				+ userInfo.getNmastersitecode() + "" + " and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nsitecode="
				+ userInfo.getNmastersitecode();

		final List<InvoiceHeader> quotation = (List<InvoiceHeader>) jdbcTemplate.query(strQuery, new InvoiceHeader());
		return quotation;
	}

	/**
	 * This interface declaration is used to get Invoice Product For Audit with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public List<InvoiceProductItemDetails> getInvoiceProductForAudit(final int squotationseqno, final String screenName,
			final UserInfo userInfo) throws Exception {

		final String str = "select sinvoiceno from invoiceheader where ninvoiceseqcode = " + squotationseqno + "";
		final List<InvoiceHeader> squotation = (List<InvoiceHeader>) jdbcTemplate.query(str, new InvoiceHeader());
		final String squotationno = squotation.get(0).getSinvoiceno();
		final String strQuery;
		if (screenName.isEmpty()) {
			strQuery = "SELECT ninvoiceproductitemdetailscode, sinvoiceseqno, "
					+ "jsondata->>'squotationseqno' AS squotationno, " + "jsondata->>'sproductname' AS sproductname, "
					+ "jsondata->>'nunit' AS nunit, " + "jsondata->>'nquantity' AS nquantity, "
					+ "NULLIF(jsondata->>'ncost', '')::double precision AS ncost, " + // Safely handle empty strings for
																						// ncost
					"NULLIF(jsondata->>'ndiscountpercentage', '')::double precision AS ndiscountpercentage, " + // Safely
																												// handle
																												// empty
																												// strings
																												// for
																												// ndiscountpercentage
					"NULLIF(jsondata->>'ntaxvalue', '')::double precision AS ntaxvalue, " + // Safely handle empty
																							// strings for ntaxvalue
					"jsondata->>'slno' AS slno, " + "jsondata->>'staxname' AS staxname, "
					+ "NULLIF(jsondata->>'ntaxpercentage', '')::double precision AS ntaxpercentage, " + // Safely handle
																										// empty strings
																										// for
																										// ntaxpercentage
					"NULLIF(jsondata->>'ntotalcost', '')::double precision AS ntotalcost, " + // Safely handle empty
																								// strings for
																								// ntotalcost
					"NULLIF(jsondata->>'noverallcost', '')::double precision AS noverallcost " + // Safely handle empty
																									// strings for
																									// noverallcost
					"FROM invoiceproductitemdetails " + "WHERE sinvoiceseqno='" + squotationno + "' AND nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();

		} else {
			strQuery = "select ninvoiceproductitemdetailscode,sinvoiceseqno,jsondata->>'sproductname' as sproductname,jsondata->>'nunit' as nunit,"
					+ "jsondata->>'nquantity' as nquantity,jsondata->>'ncost' as ncost,"
					+ "jsondata->>'ndiscountpercentage' as ndiscountpercentage,"
					+ "jsondata->>'ntaxvalue' as ntaxvalue," + "jsondata->>'staxname' as staxname,"
					+ "jsondata->>'ntaxpercentage' as ntaxpercentage,"
					+ "jsondata->>'ntotalcost' as ntotalcost,jsondata->>'noverallcost' as noverallcost"
					+ " from invoiceproductitemdetails where sinvoiceseqno='" + squotationno + "' and nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
		}
		final List<InvoiceProductItemDetails> product = (List<InvoiceProductItemDetails>) jdbcTemplate.query(strQuery,
				new InvoiceProductItemDetails());

		return product;
	}

	/**
	 * This interface declaration is used to add a new entry to ProductInvoiceHeader
	 * table.
	 * 
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be added in
	 *                         ProductInvoiceHeader table
	 * @return response entity object holding response status and data of added
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings({ "unused" })
	public ResponseEntity<Object> createproductInvoice(final Map<String, Object> inputMap, final int invoiceSeqNo,
			final UserInfo objUserInfo, final String strformat) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final JSONObject jsonObject = new JSONObject(inputMap.get("productList").toString());
		if (jsonObject.isEmpty()) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ADDPRODUCTDETAILS", objUserInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}
		final JSONArray jsonArray = jsonObject.getJSONArray("ProductList");
		if (jsonArray != null) {
			jsonArray.forEach(objProductList -> {

				JSONObject prodlistjsonObject = (JSONObject) objProductList;

				String sproductname = "";
				String sProductRefNo = "";
				int nproductslno = 0;

				if (prodlistjsonObject.has("squotationseqno")) {

					sproductname = prodlistjsonObject.getString("squotationseqno");
					sProductRefNo = prodlistjsonObject.getString("squotationseqno");

				}

				String name = sproductname + ",";
				name += sproductname + ",";

				int seqnoinvoice = jdbcTemplate.queryForObject(
						"select nsequenceno from " + " seqnoinvoice where stablename='invoiceproductitemdetails'",
						Integer.class);
				seqnoinvoice++;

				String insertQuery = "";
				String st = "";
				try {
					prodlistjsonObject = productTaxCalculation(prodlistjsonObject);

				} catch (Exception e) {
					e.printStackTrace();
				}
				if (prodlistjsonObject.has("slno")) {
					nproductslno = prodlistjsonObject.getInt("slno");
				} else {
					nproductslno = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				}
				try {
					final JSONObject jsonObjectTest = new JSONObject(prodlistjsonObject.get("testList").toString());
					prodlistjsonObject.remove("testList");
					if (prodlistjsonObject.has("jsondata1")) {
						prodlistjsonObject.remove("jsondata1");
					}
					final int nlimsproduct = prodlistjsonObject.getInt("nlimsproduct");
					insertQuery = " INSERT INTO invoiceproductitemdetails(ninvoiceproductitemdetailscode , nserialno, sinvoiceseqno, dinvoicedate, jsondata,jsondata1,dmodifieddate, nsitecode, nstatus,sproductrefno,nproductslno,nlimsproduct)"
							+ " VALUES(" + seqnoinvoice + "," + seqnoinvoice + ",'" + strformat + "','"
							+ inputMap.get("dinvoicedate") + "','"
							+ stringUtilityFunction.replaceQuote(prodlistjsonObject.toString()) + "'::jsonb," + "'"
							+ stringUtilityFunction.replaceQuote(jsonObjectTest.toString()) + "'::jsonb," + "'"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ objUserInfo.getNmastersitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'" + sProductRefNo + "','"
							+ prodlistjsonObject.getInt("slno") + "'," + nlimsproduct + ")";

					st = "update invoiceheader set squotationid='" + name + "' where ninvoiceseqcode=" + invoiceSeqNo;

				} catch (Exception e) {
					e.printStackTrace();
				}
				if (!prodlistjsonObject.has("slno") || prodlistjsonObject.getInt("slno") == 0) {
					nproductslno++;
				}
				jdbcTemplate.execute(insertQuery);
				jdbcTemplate.execute(st);

				jdbcTemplate.execute("update seqnoinvoice set nsequenceno=" + seqnoinvoice
						+ "  where stablename='invoiceproductitemdetails' ");

			});
			try {
				insertExeProducts(inputMap, invoiceSeqNo, objUserInfo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		final String ssymbol = jdbcTemplate.queryForObject(
				"select ssymbol from invoicecurrencytype where ncurrencycode=" + inputMap.get("ncurrencytype") + " "
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + objUserInfo.getNmastersitecode() + "",
				String.class);
		final List<InvoiceQuotationProduct> lstProduct = (List<InvoiceQuotationProduct>) jdbcTemplate.query(
				"select jsondata->>'noverallcost' as noverallcost,jsondata->>'ntaxamount' as ntaxamount from invoiceproductitemdetails where sinvoiceseqno='"
						+ strformat + "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + objUserInfo.getNmastersitecode() + "",
				new InvoiceQuotationProduct());
		final double totalamount = lstProduct.stream().mapToDouble(x -> Double.valueOf(x.getNoverallcost())).sum();
		final double taxamount = lstProduct.stream().mapToDouble(x -> Double.valueOf(x.getNtaxamount())).sum();
		final String overAllCost = String.format("%.2f", totalamount);
		final String totaltaxamount = String.format("%.2f", taxamount);

		inputMap.put("ntotalamount", overAllCost);
		final String totalamountinword = getActivecurrencyvalue(inputMap, objUserInfo);
		final String taxAmountInWords = getAmountInWords(inputMap, objUserInfo, "totaltaxamount");

		final JSONObject invjsonObject = new JSONObject(inputMap.get("invoiceJson").toString());
		invjsonObject.put("TotalAmountInWords", totalamountinword);

		invjsonObject.put("TotalAmount", overAllCost);
		invjsonObject.put("TotalTaxAmount", totaltaxamount);
		invjsonObject.put("TotalTaxAmountInWords", taxAmountInWords);

		// update for indirect tax amount as to calculate in total amount
		jdbcTemplate.execute("update invoiceheader set ntotalamount='" + (ssymbol + " " + overAllCost)
				+ "',ntotaltaxamount='" + (ssymbol + " " + totaltaxamount) + "',jsondata='"
				+ stringUtilityFunction.replaceQuote(invjsonObject.toString()) + "' where sinvoiceno='" + strformat
				+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

		// update for tax overall calculation for report purpose
		final List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(strformat, objUserInfo);
		final String taxJson = objMapper.writeValueAsString(taxList);
		final String str = "update invoiceheader set jsondata1='"
				+ stringUtilityFunction.replaceQuote(taxJson.toString()) + "' where sinvoiceno='" + strformat
				+ "'and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		jdbcTemplate.execute(str);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get the product Tax Calculation with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of ProductInvoiceHeader with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public JSONObject productTaxCalculation(final JSONObject taxcaljsonObject) throws Exception {

		if (taxcaljsonObject.getInt("nindirectax") != 0) {
			final double tax = (double) ((Double.parseDouble(taxcaljsonObject.getString("ntaxvalue"))
					* taxcaljsonObject.getInt("nindirectax")) / 100);
			final String taxAmount = String.format("%.2f", tax);
			final double overAllCost = Double.parseDouble(taxcaljsonObject.getString("noverallcostvalue"))
					+ Double.parseDouble(taxAmount);
			final double overAllTaxAmount = Double.parseDouble(taxcaljsonObject.getString("ntaxvalue"))
					+ Double.parseDouble(taxAmount);
			final String overAllCostFormating = String.format("%.2f", overAllCost);
			final String overAllTaxAmountFormating = String.format("%.2f", overAllTaxAmount);
			taxcaljsonObject.put("noverallcost", overAllCostFormating);
			taxcaljsonObject.put("ntaxamount", overAllTaxAmountFormating);
			taxcaljsonObject.put("nindirecttaxamount", taxAmount);
		}
		return taxcaljsonObject;
	}

	/**
	 * This interface declaration is used to add a new entry to invoice execute
	 * products table.
	 * 
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be added in
	 *                         InvoiceHeader table
	 * @return response entity object holding response status and data of added
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> insertExeProducts(final Map<String, Object> inputMap, final int invoiceSeqNo,
			final UserInfo objUserInfo) throws Exception {
		final JSONObject jsonObject = new JSONObject(inputMap.get("productList").toString());

		if (jsonObject.isEmpty()) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ADDPRODUCTDETAILS", objUserInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		final JSONArray jsonArray = jsonObject.getJSONArray("ProductList");
		if (jsonArray != null) {
			jsonArray.forEach(objProductList -> {

				final JSONObject prodlistjsonObject = (JSONObject) objProductList;

				final int nproductcode = prodlistjsonObject.getInt("nproductcode");
				final int slno = prodlistjsonObject.getInt("slno");
				final int nlimsproduct = prodlistjsonObject.getInt("nlimsproduct");

				final String nunit;
				if (prodlistjsonObject.has("nunit")) {
					nunit = prodlistjsonObject.getString("nunit");
				} else {
					nunit = "0";
				}

				final int ndiscountpercentage;
				if (prodlistjsonObject.has("ndiscountpercentage")) {
					ndiscountpercentage = prodlistjsonObject.getInt("ndiscountpercentage");
				} else {
					ndiscountpercentage = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				}
				final double ncost = prodlistjsonObject.getDouble("ncost");
				final String sproductname = prodlistjsonObject.getString("sproductname");
				final int nquantity;
				if (prodlistjsonObject.has("nquantity")) {
					nquantity = prodlistjsonObject.getInt("nquantity");
				} else {
					nquantity = 1;
				}

				final double ntax = prodlistjsonObject.getDouble("ntaxvalue");
				final double ntaxpercentage = Double.valueOf((String) prodlistjsonObject.get("ntaxpercentage"));
				final String staxname = prodlistjsonObject.getString("staxname");
				final double ntotalcost = prodlistjsonObject.getDouble("ntotalcost");
				final double noverallcost = prodlistjsonObject.getDouble("noverallcost");

				int seqnoinvoice = jdbcTemplate.queryForObject(
						"select nsequenceno from " + " seqnoinvoice where stablename='invoiceexecustomerproducts'",
						Integer.class);
				seqnoinvoice++;
				String insertQuery = "";

				try {
					insertQuery = " INSERT INTO invoiceexecustomerproducts(nexeproduct, nproductcode, ncustomercode, nschemecode,slno, ncost, nunit, nquantity, ntax, ntaxpercentage, staxname, ntotalcost, noverallcost, sproductname,ndiscountpercentage, ndiscountperquantity, dmodifieddate, nsitecode,nlimsproduct, nstatus)"
							+ " VALUES(" + seqnoinvoice + "," + nproductcode + "," + inputMap.get("ncustomercode") + ","
							+ inputMap.get("nschemecode") + "," + slno + "," + ncost + ",'" + nunit + "'," + nquantity
							+ "," + ntaxpercentage + "," + ntax + ",'" + staxname + "'," + ntotalcost + ","
							+ noverallcost + ",'" + sproductname + "'," + ndiscountpercentage + "," + ncost + ",'"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ objUserInfo.getNmastersitecode() + "," + nlimsproduct + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

				} catch (Exception e) {
					e.printStackTrace();
				}
				jdbcTemplate.execute(insertQuery);

				jdbcTemplate.execute("update seqnoinvoice set nsequenceno=" + seqnoinvoice
						+ "  where stablename='invoiceexecustomerproducts' ");
			});
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get the SeletedInvoice with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getSeletedInvoice(final Map<String, Object> inputMap) throws Exception {
		final Map<String, Object> objMap = new HashMap<String, Object>();

		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		final int nuser = userInfo.getNuserrole();
		if (nuser == -1) {
			final String strQuery = "select cms.scolorhexcode as scolorcode,cm.scolorhexcode,ts.stransstatus as stransdisplaystatus,its.ntransactionstatus,ih.ninvoiceseqcode,ih.sinvoiceno,ih.squotationid,ih.ncustomercode,ih.sprojectcode,ih.sprocessno,ih.sschemename,ih.spackdoctrefno,"
					+ " ih.spackagerefdetails,ih.ntotalamount,ih.ntotaltaxamount,ins.nschemecode,ih.ntotalfrightcharges,ih.npaymentcode,ih.spaymentdetails,ih.nbankcode,ih.ncurrencytype,ih.nstatus,ih.nproducttestcode,iph.sproducttestdetail,ic.ssymbol,"
					+ " ih.jsondata||json_build_object('ninvoiceseqcode',ninvoiceseqcode)::jsonb "
					+ " as jsondata,ih.jsondata->>'CustomerName' as CustomerName," + " COALESCE(TO_CHAR(dinvoicedate,'"
					+ userInfo.getSsitedate() + "'),'') as sinvoicedate," + " COALESCE(TO_CHAR(dquotationdate,'"
					+ userInfo.getSsitedate() + "'),'') as squotationdate," + " COALESCE(TO_CHAR(dprocessdate,'"
					+ userInfo.getSsitedate() + "'),'') as sprocessdate," + " COALESCE(TO_CHAR(dorderreferencedate,'"
					+ userInfo.getSsitedate() + "'),'') as sorderreferencedate," + " COALESCE(TO_CHAR(dpackdocrefdate,'"
					+ userInfo.getSsitedate() + "'),'') as spackdocrefdate"
					+ " from invoiceheader ih,producttestdetail iph,colormaster cms,invoicecurrencytype ic,"
					+ "invoicetransactionstatus its ,transactionstatus ts,colormaster cm,formwisestatuscolor fsc,invoiceschemes ins  where its.sreferenceid=ih.sinvoiceno and ts.ntranscode=its.ntransactionstatus "
					+ " and cm.ncolorcode=fsc.ncolorcode and iph.nproducttestcode=ih.nproducttestcode and ih.ncurrencytype=ic.ncurrencycode and ts.ntranscode=fsc.ntranscode and ih.sschemename=ins.sschemename and fsc.nformcode= "
					+ userInfo.getNformcode() + " and cms.ncolorcode= "
					+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + " and ninvoiceseqcode in("
					+ inputMap.get("invoiceid") + ") and ih.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ih.nsitecode="
					+ userInfo.getNmastersitecode() + " and cm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iph.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iph.nmastersitecode="
					+ userInfo.getNmastersitecode() + " and ic.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nsitecode="
					+ userInfo.getNmastersitecode() + " and fsc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fsc.nsitecode="
					+ userInfo.getNmastersitecode() + " and ins.ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and its.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cms.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ninvoiceseqcode ";
			final List<InvoiceHeader> lstInvoice = jdbcTemplate.query(strQuery, new InvoiceHeader());
			objMap.put("selectedInvoice", lstInvoice);

			final List<InvoiceProductItemDetails> productList = getProductsforInvoiceInsert(
					lstInvoice.get(lstInvoice.size() - 1).getSinvoiceno(), userInfo);
			objMap.put("selectedProduct", productList);
			final List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(
					lstInvoice.get(lstInvoice.size() - 1).getSinvoiceno(), userInfo);
			objMap.put("selectedTax", taxList);

		} else {
			final String strquery = "select cms.scolorhexcode as scolorcode,cm.scolorhexcode,ts.stransstatus as stransdisplaystatus,its.ntransactionstatus,ih.ninvoiceseqcode,ih.sinvoiceno,ih.squotationid,ih.ncustomercode,ih.sprojectcode,ih.sprocessno,ih.sschemename,ih.spackdoctrefno,"
					+ " ih.spackagerefdetails,ih.ntotalamount,ih.ntotaltaxamount,ih.ntotalfrightcharges,ih.npaymentcode,ih.spaymentdetails,ih.nbankcode,ih.ncurrencytype,ih.nproducttestcode,iph.sproducttestdetail,ih.nstatus,ic.ssymbol,"
					+ " ih.jsondata||json_build_object('ninvoiceseqcode',ninvoiceseqcode)::jsonb "
					+ " as jsondata,ih.jsondata->>'CustomerName' as CustomerName," + " COALESCE(TO_CHAR(dinvoicedate,'"
					+ userInfo.getSsitedate() + "'),'') as sinvoicedate," + " COALESCE(TO_CHAR(dquotationdate,'"
					+ userInfo.getSsitedate() + "'),'') as squotationdate," + " COALESCE(TO_CHAR(dprocessdate,'"
					+ userInfo.getSsitedate() + "'),'') as sprocessdate," + " COALESCE(TO_CHAR(dorderreferencedate,'"
					+ userInfo.getSsitedate() + "'),'') as sorderreferencedate," + " COALESCE(TO_CHAR(dpackdocrefdate,'"
					+ userInfo.getSsitedate() + "'),'') as spackdocrefdate"
					+ " from invoiceheader ih,producttestdetail iph,colormaster cms,invoicecurrencytype ic,"
					+ "invoicetransactionstatus its ,transactionstatus ts,colormaster cm,formwisestatuscolor fsc where its.sreferenceid=ih.sinvoiceno and ts.ntranscode=its.ntransactionstatus "
					+ " and cm.ncolorcode=fsc.ncolorcode and ts.ntranscode=fsc.ntranscode and iph.nproducttestcode=ih.nproducttestcode and ih.ncurrencytype=ic.ncurrencycode and fsc.nformcode= "
					+ userInfo.getNformcode() + " and cms.ncolorcode = "
					+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + " and ninvoiceseqcode in("
					+ inputMap.get("invoiceid") + ") and ih.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ih.nsitecode="
					+ userInfo.getNmastersitecode() + " and cm.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nsitecode="
					+ userInfo.getNmastersitecode() + " and fsc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fsc.nsitecode="
					+ userInfo.getNmastersitecode() + "" + " and iph.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iph.nmastersitecode="
					+ userInfo.getNmastersitecode() + " and its.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ninvoiceseqcode";

			final List<InvoiceHeader> lstInvoice = jdbcTemplate.query(strquery, new InvoiceHeader());
			objMap.put("selectedInvoice", lstInvoice);
			final List<InvoiceProductItemDetails> productList = getProductsforInvoiceInsert(
					lstInvoice.get(lstInvoice.size() - 1).getSinvoiceno(), userInfo);
			objMap.put("selectedProduct", productList);

			final int npackagerefdetails;
			final int ntotalfrightchanges;
			final int norderrefno;
			final int packdocref;
			final int packdocrefdate;
			final int nproducttestcode;
			final int invoicepatientfield;

			final String st = "select * from userrolefieldcontrol where nformcode=" + userInfo.getNformcode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nneedrights=" + Enumeration.TransactionStatus.YES.gettransactionstatus();
			final List<UserRoleFieldControl> listTest = (List<UserRoleFieldControl>) jdbcTemplate.query(st,
					new UserRoleFieldControl());
//			final String sntestgrouptestcode = projectDAOSupport.fndynamiclisttostring(listTest, "getNfieldcode");
			final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest, "getNfieldcode");
			if (listTest.size() == Enumeration.TransactionStatus.ALL.gettransactionstatus()) {
				npackagerefdetails = 4;
				ntotalfrightchanges = 4;
				norderrefno = 4;
				packdocref = 4;
				packdocrefdate = 4;
				nproducttestcode = 4;
				invoicepatientfield = 4;
			} else {
				String std = "select * from fieldmaster where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "nfieldcode in ("
						+ sntestgrouptestcode + ")";

				final List<FieldMaster> fieldlistTest = (List<FieldMaster>) jdbcTemplate.query(std, new FieldMaster());

				final Set<String> fieldNames = fieldlistTest.stream().map(FieldMaster::getSfieldname)
						.collect(Collectors.toSet());
				norderrefno = fieldNames.contains("sorderreferenceno") ? 3 : 4;
				packdocref = fieldNames.contains("spackdoctrefno") ? 3 : 4;
				packdocrefdate = fieldNames.contains("dpackdocrefdate") ? 3 : 4;
				npackagerefdetails = fieldNames.contains("spackagerefdetails") ? 3 : 4;
				ntotalfrightchanges = fieldNames.contains("ntotalfrightcharges") ? 3 : 4;
				nproducttestcode = fieldNames.contains("nproducttestcode") ? 3 : 4;
				invoicepatientfield = fieldNames.contains("invoicepatientfield") ? 3 : 4;
			}
			objMap.put("Totalfrightchanges", ntotalfrightchanges);
			objMap.put("PackageDetails", npackagerefdetails);
			objMap.put("Orderrefno", norderrefno);
			objMap.put("Packdoref", packdocref);
			objMap.put("Packrefdate", packdocrefdate);
			objMap.put("producttestcode", nproducttestcode);

			objMap.put("PatientDetails", invoicepatientfield);

		}
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get the overall InvoiceQuotation with
	 * respect to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ResponseEntity<Object> getQuotationTab(final Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference<UserInfo>() {
		});
		final Map<String, Object> objMap = new HashMap<String, Object>();
		final List<InvoiceQuotationProduct> lstproduct = new ArrayList();

		final int nCusId = (int) inputMap.get("ncustomercode");
		final List<InvoiceProductItemDetails> lstProdRef = new ArrayList<>();
		final List<InvoiceQuotationHeader> lstExstQTno = new ArrayList<>();
		List<InvoiceQuotationHeader> lstQuotation = new ArrayList<>();
		List<String> seqList = new ArrayList<>();

		final String  strapproveinvoice="select ih.sinvoiceno,ipd.sproductrefno from  invoiceheader ih,invoicetransactionstatus its,"
				+ " invoiceproductitemdetails ipd  where ncustomercode="+nCusId+" and its.sreferenceid=ih.sinvoiceno "
				+ " and ntransactionstatus="+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" and ipd.sinvoiceseqno=ih.sinvoiceno";
		final List<InvoiceHeader> lstprod = jdbcTemplate.query(strapproveinvoice, new InvoiceHeader());
		
		List<String> productRefList = lstprod.stream().map(InvoiceHeader::getSproductrefno).filter(s -> s != null && !s.trim().isEmpty()).collect(Collectors.toList());

		String approveprod = productRefList.stream().map(s -> "'" + s + "'").collect(Collectors.joining(", "));
		
		List<String> approveList =lstprod.stream().map(InvoiceHeader::getSproductrefno).filter(s -> s != null && !s.trim().isEmpty())
		               .map(s -> "'" + s + "'").collect(Collectors.toList());		
		
		List<Map<String, Object>> itemDetails = (List<Map<String, Object>>) inputMap.get("quotationno");

		for (Map<String, Object> item : itemDetails) {
		    Object seq = item.get("squotationseqno");
		    if (seq != null && !seq.toString().isEmpty()) {
		        seqList.add("'" + seq.toString() + "'");
		    }
		}
		
		seqList.addAll(approveList);
		
		String quotationno = String.join(",", seqList);		
			
		String notInClause = "";
		if (quotationno != null && !quotationno.isEmpty()) {
		    notInClause = " AND iqh.squotationno NOT IN (" + quotationno + ")";
		}
		
	

		
		final String strGetCustomer = "select sinvoiceno from invoiceheader ih,invoicetransactionstatus its where ih.ncustomercode=" + nCusId
				+ " and ih.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ih.nsitecode="
				+ userInfo.getNmastersitecode()+ " and its.sreferenceid=ih.sinvoiceno and its.ntransactionstatus not in "
			    + "("+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+")";
		final List<InvoiceHeader> lstcustomers = jdbcTemplate.query(strGetCustomer, new InvoiceHeader());

		for (InvoiceHeader obj : lstcustomers) {
			final String strGetProductRefno = "select sproductrefno from invoiceproductitemdetails where sinvoiceseqno= '"
					+ obj.getSinvoiceno() + "' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode()+" and sproductrefno not in ("+approveprod+")";
			final List<InvoiceProductItemDetails> tempProdRef = jdbcTemplate.query(strGetProductRefno,
					new InvoiceProductItemDetails());
			lstProdRef.addAll(tempProdRef);
		}

		for (InvoiceProductItemDetails objquotation : lstProdRef) {
			final String getExistingQTno = "select squotationno from invoicequotationheader where squotationno ='"
					+ objquotation.getSproductrefno() + "' and ncustomercode= '" + nCusId + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			;
			final List<InvoiceQuotationHeader> tempExstQTno = jdbcTemplate.query(getExistingQTno,
					new InvoiceQuotationHeader());
			lstExstQTno.addAll(tempExstQTno);
		}

		final StringBuilder squotationnos = new StringBuilder();

		for (InvoiceQuotationHeader header : lstExstQTno) {
			if (squotationnos.length() > 0) {
				squotationnos.append(",");
			}
			squotationnos.append("'").append(header.getSquotationno()).append("'");
		}

		if (lstExstQTno.isEmpty()) {
			final String strQuery = " select squotationno," + " COALESCE(TO_CHAR(dquotationdate,'"
					+ userInfo.getSsitedate() + "'),'') as squotationdate ,ncustomercode "
					+ " from invoicequotationheader iqh,invoicetransactionstatus its where iqh.ncustomercode = " + inputMap.get("ncustomercode") + ""
					+ " and iqh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iqh.nsitecode="
					+ userInfo.getNmastersitecode() + " and iqh.squotationno=its.sreferenceid and its.ntransactionstatus="
					+ ""+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" "
					+  notInClause+" order by iqh.dquotationdate desc";
//
//            final String strQuery = " select squotationno," + " COALESCE(TO_CHAR(dquotationdate,'"
//					+ userInfo.getSsitedate() + "'),'') as squotationdate ,ncustomercode "
//					+ " from invoicequotationheader where ncustomercode = " + inputMap.get("ncustomercode") + ""
//					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
//					+ userInfo.getNmastersitecode() + " order by dquotationdate desc";

			lstQuotation = jdbcTemplate.query(strQuery, new InvoiceQuotationHeader());

		} else {
			final String strQuery = "select squotationno, " + "COALESCE(TO_CHAR(dquotationdate,'"
					+ userInfo.getSsitedate() + "'),'') as squotationdate,ncustomercode "
					+ "from invoicequotationheader iqh where iqh.ncustomercode = " + inputMap.get("ncustomercode")
					+ " and iqh.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iqh.nsitecode="
					+ userInfo.getNmastersitecode() + " "+notInClause+""
					+ " order by iqh.dquotationdate desc";
			lstQuotation = jdbcTemplate.query(strQuery, new InvoiceQuotationHeader());
		}


		if(lstQuotation.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_QUOTATIONNOTAVALIABLE", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}else {
			
			for (InvoiceQuotationHeader obj : lstQuotation) {
				final String strquery = "select jsondata1::jsonb as jsondata1,nquotationitemdetailscode,squotationseqno,nlimsproduct,jsondata->>'sproductname' as sproductname,jsondata->>'nunit' as nunit,"
						+ "jsondata->>'nquantity' as nquantity,jsondata->>'ncost' as ncost,jsondata->>'slno' as nserialno,jsondata->>'ntotalcost' as ntotalcost,"
						+ "jsondata->>'ndiscountpercentage' as ndiscountpercentage,jsondata->>'nproductcode' as nproductcode,jsondata->>'slno' as slno,jsondata->>'staxname' as staxname,jsondata->>'nlimsproduct' as nlimsproduct,"
						+ "jsondata->>'ntaxvalue' as ntaxvalue,jsondata->>'ntaxamount' as ntaxamount,jsondata->>'noverallcost' as noverallcost,jsondata->>'ntaxpercentage' as ntaxpercentage,jsondata->>'noverallcostvalue' as noverallcostvalue"
						+ ",jsondata->>'nindirectax' as nindirectax,jsondata->>'sindirecttaxname' as sindirecttaxname,jsondata->>'nindirecttaxamount' as nindirecttaxamount"
						+ " from quotationitemdetails ,invoicetransactionstatus its where quotationitemdetails.squotationseqno='" + obj.getSquotationno() + "' and quotationitemdetails.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and quotationitemdetails.nsitecode="
						+ userInfo.getNmastersitecode() + " and quotationitemdetails.squotationseqno=its.sreferenceid "
						+ " order by quotationitemdetails.nquotationitemdetailscode";
				final List<InvoiceQuotationProduct> filteredList = (List<InvoiceQuotationProduct>) jdbcTemplate
						.query(strquery, new InvoiceQuotationProduct());
				
				lstproduct.addAll(filteredList);				
		    }
		}	
		objMap.put("quotationList", lstQuotation);
		objMap.put("productList", lstproduct);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get the overall LIMSArno with respect
	 * to site.
	 *
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of LIMSArno with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getArnoTab(Map<String, Object> inputMap) throws Exception {
	    ObjectMapper objMapper = new ObjectMapper();

	    UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference<UserInfo>() {});
	    String scustomername = (String) inputMap.get("scustomername");

	    String sarno = 
	        "SELECT ip.jsondata->>'squotationseqno' AS squotationno " +
	        "FROM invoiceproductitemdetails ip " +
	        "JOIN invoiceheader iqh ON iqh.sinvoiceno = ip.sinvoiceseqno " +
	        "JOIN invoicetransactionstatus its ON iqh.sinvoiceno = its.sreferenceid " +
	        "WHERE ip.nstatus = 1 " +
	        "AND iqh.nstatus = ? " +
	        "AND iqh.jsondata->>'CustomerName' = ? " +
	        "AND its.ntransactionstatus != ?";

	    List<InvoiceProductItemDetails> lstQuotation = jdbcTemplate.query(
	        sarno,
	        new Object[] {
	            Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
	            scustomername,
	            Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
	        },
	        new InvoiceProductItemDetails()
	    );


	    // 3. Build list of quotation numbers from DB
	    List<String> quotationList = lstQuotation.stream()
	            .map(InvoiceProductItemDetails::getSquotationno)
	            .filter(Objects::nonNull)
	            .collect(Collectors.toList());

	 // Extract arno list from inputMap
	    List<Map<String, Object>> arnoList =
	            (List<Map<String, Object>>) inputMap.get("arno");

	    // Prepare list to store all quotation numbers from frontend
	    Set<String> frontendQuotationList = new HashSet<>();
	    
	    if (arnoList != null) {
	        for (Map<String, Object> item : arnoList) {

	            // some items have value in “squotationseqno” (checked in your sample)
	            String qno = (String) item.get("squotationseqno");

	            if (qno != null && !qno.trim().isEmpty()) {
	                frontendQuotationList.add(qno.trim());
	            }
	        }
	    }

//	    String newQuotationNo = null;
//	    
//	    if (arnoList != null && !arnoList.isEmpty()) {
//	        Map<String, Object> arnoItem = arnoList.get(0);
//	        newQuotationNo = (String) arnoItem.get("squotationno"); // "LL/OM/2025/000022"
//	    }

	    // 5. Add frontend quotation number to list
	    if (frontendQuotationList != null && !frontendQuotationList.isEmpty()) {
	        quotationList.addAll(frontendQuotationList);
	    }

	    // 6. Convert list to SQL IN format ('A','B','C')
	    String squotationno = quotationList.isEmpty()
	            ? "''"
	            : quotationList.stream()
	                    .map(q -> "'" + q + "'")
	                    .collect(Collectors.joining(","));


	 // Final query
	 String strQuery =
	     "SELECT * FROM view_arno " +
	     "WHERE scustomername IN (?, 'NA') " +
	     "AND squotationno NOT IN (" + squotationno + ")";

	    List<RegistrationHistory> lstInvoice = jdbcTemplate.query(
	        strQuery,
	        new Object[]{ scustomername },
	        new RegistrationHistory()
	    );

	    return new ResponseEntity<>(lstInvoice, HttpStatus.OK);
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
	@Override
	public ResponseEntity<Object> updateInvoice(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final JSONObject actionType = new JSONObject();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<String> multilingualIDListProduct = new ArrayList<>();
		final int sequno = (int) inputMap.get("ninvoiceseqcode");
		final List<Object> savedNewList = new ArrayList<>();
		final List<Object> savedOldList = new ArrayList<>();
		final List<InvoiceHeader> InvoiceHeader = getInvoiceforAudit(sequno, userInfo);
		final List<InvoiceProductItemDetails> InvoiceProduct = getInvoiceProductForAudit(sequno, "", userInfo);

		final JSONObject jsonObject = new JSONObject(inputMap.get("invoiceJson").toString());
		final JSONObject taxjsonObject = new JSONObject(inputMap.get("taxJson").toString());

		String ntotalAmount = String.valueOf(inputMap.get("ntotalamount"));
		ntotalAmount = (!"null".equalsIgnoreCase(ntotalAmount) && !ntotalAmount.trim().isEmpty()) ? ntotalAmount : "0";

		String ntotalFrightCharges = String.valueOf(inputMap.get("ntotalfrightcharges"));
		ntotalFrightCharges = (!"null".equalsIgnoreCase(ntotalFrightCharges) && !ntotalFrightCharges.trim().isEmpty())
				? ntotalFrightCharges
				: "0";

		String ntotalTaxAmount = String.valueOf(inputMap.get("ntotaltaxamount"));
		ntotalTaxAmount = (!"null".equalsIgnoreCase(ntotalTaxAmount) && !ntotalTaxAmount.trim().isEmpty())
				? ntotalTaxAmount
				: "0";

		String ncurrencyType = String.valueOf(inputMap.get("ncurrencytype"));
		ncurrencyType = (!"null".equalsIgnoreCase(ncurrencyType) && !ncurrencyType.trim().isEmpty()) ? ncurrencyType
				: "NULL";

		String ncustomercode = String.valueOf(inputMap.get("ncustomercode"));
		ncustomercode = (!"null".equalsIgnoreCase(ncustomercode) && !ncustomercode.trim().isEmpty()) ? ncustomercode
				: "NULL";

		// Check if "testList" key exists before accessing it
		JSONArray testListArray;

		if (jsonObject.has("testList") && !jsonObject.isNull("testList")) {
			testListArray = jsonObject.getJSONArray("testList");
		} else {
			System.out.println("Key 'testList' not found in JSON. Assigning an empty array.");
			testListArray = new JSONArray(); // Avoid crashing by using an empty array
		}

		// Now you can use testListArray safely
		System.out.println("testListArray: " + testListArray.toString());

		final String safeJsonData = stringUtilityFunction.replaceQuote(jsonObject.toString());
		final String strsafeJsonData = stringUtilityFunction.replaceQuote(taxjsonObject.toString());

		final String strquery = "UPDATE invoiceheader SET " + "ntotalamount='" + ntotalAmount + "', "
				+ "ntotalfrightcharges='" + ntotalFrightCharges + "', " + "ntotaltaxamount='" + ntotalTaxAmount + "', "
				+ "sschemename='" + inputMap.getOrDefault("sschemename", "") + "', " + "ncurrencytype="
				+ (ncurrencyType.equals("NULL") ? "NULL" : ncurrencyType) + ", " + "spaymentdetails='"
				+ inputMap.getOrDefault("spaymentdetails", "") + "', " + "npaymentcode="
				+ inputMap.getOrDefault("npaymentcode", "NULL") + ", " + "nproducttestcode="
				+ inputMap.getOrDefault("nproducttestcode", "NULL") + ", " + "dinvoicedate='"
				+ inputMap.getOrDefault("dinvoicedate", "NULL") + "', " + "dquotationdate='"
				+ inputMap.getOrDefault("dquotationdate", "NULL") + "', " + "dprocessdate='"
				+ inputMap.getOrDefault("dprocessdate", "NULL") + "', " + "dorderreferencedate='"
				+ inputMap.getOrDefault("dorderreferencedate", "NULL") + "', " + "dpackdocrefdate='"
				+ inputMap.getOrDefault("dpackdocrefdate", "NULL") + "', " + "ncustomercode="
				+ (ncustomercode.equals("NULL") ? "NULL" : ncustomercode) + ", " + "sprojectcode='"
				+ inputMap.getOrDefault("sprojectcode", "") + "', " + "sprocessno='"
				+ inputMap.getOrDefault("sprocessno", "") + "', " + "spackdoctrefno='"
				+ inputMap.getOrDefault("spackdoctrefno", "") + "', " + "spackagerefdetails='"
				+ inputMap.getOrDefault("spackagerefdetails", "") + "', " + "nbankcode="
				+ inputMap.getOrDefault("nbankcode", "NULL") + ", " + "jsondata='" + safeJsonData + "', "
				+ "jsondata1='" + strsafeJsonData + "', " + "dmodifieddate='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "nsitecode="
				+ userInfo.getNmastersitecode() + ", " + "nusercode=" + userInfo.getNusercode() + " "
				+ "WHERE ninvoiceseqcode=" + inputMap.getOrDefault("ninvoiceseqcode", "NULL");

		jdbcTemplate.execute(strquery);

		updateProductInvoice(inputMap, userInfo);

		final JSONObject prodlistjsonObject = new JSONObject(inputMap.get("productList").toString());

		final JSONArray jsonArray = prodlistjsonObject.getJSONArray("ProductList");
		if (jsonArray.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ADDPRODUCTDETAILS", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		final List<InvoiceHeader> InvoiceHeaderaudit = getInvoiceforAudit(sequno, userInfo);
		final List<InvoiceProductItemDetails> InvoiceProductaudit = getInvoiceProductForAudit(sequno, "", userInfo);
		inputMap.put("nflag", 2);
		actionType.put("InvoiceHeader", "IDS_EDITINVOICEHEADER");
		multilingualIDList.add("IDS_EDITINVOICEHEADER");
		auditUtilityFunction.fnInsertAuditAction(InvoiceHeaderaudit, 2, InvoiceHeader, multilingualIDList, userInfo);
		multilingualIDListProduct.add("IDS_EDITINVOICEPRODUCT");
		for (int i = 0; i < InvoiceProductaudit.size() && i < InvoiceProduct.size(); i++) {
			savedNewList.clear();
			savedNewList.add(InvoiceProductaudit.get(i));
			savedOldList.clear();
			savedOldList.add(InvoiceProduct.get(i));
			auditUtilityFunction.fnInsertAuditAction(savedNewList, 2, savedOldList, multilingualIDListProduct,
					userInfo);
		}

		return getInvoiceHeader(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to update entry in productInvoiceHeader
	 * table.
	 * 
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be updated
	 *                         in productInvoiceHeader table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings("unused")
	@Override
	public ResponseEntity<Object> updateProductInvoice(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final JSONObject actionType = new JSONObject();
		final List<String> multilingualIDListProduct = new ArrayList<>();
		final JSONObject jsonObject = new JSONObject(inputMap.get("productList").toString());
		final JSONArray jsonArray = jsonObject.getJSONArray("ProductList");
		final String sinvoiceseqno = (inputMap.get("ninvoiceseqcode").toString());
		final String strquery = "select sinvoiceno from invoiceheader where ninvoiceseqcode=" + sinvoiceseqno
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		final List<InvoiceHeader> strformat = (List<InvoiceHeader>) jdbcTemplate.query(strquery, new InvoiceHeader());
		final String strformatno = strformat.get(0).getSinvoiceno();
		if (jsonArray != null) {
			jsonArray.forEach(objProductList -> {
				String invoiceitemdetailscode = "";

				JSONObject prodlistjsonObject = (JSONObject) objProductList;

				if (prodlistjsonObject.has("nquotationitemdetailscode")) {
					invoiceitemdetailscode = "0";
				} else if (prodlistjsonObject.has("ninvoiceproductitemdetailscode")) {
					invoiceitemdetailscode = prodlistjsonObject.get("ninvoiceproductitemdetailscode").toString();
				}

				final int invoiceitemdetails = Integer.parseInt(invoiceitemdetailscode);
				final int nlimsproduct = prodlistjsonObject.getInt("nlimsproduct");

				// ✅ Check if "testList" exists before accessing it
				final JSONObject jsonObjectTest;
				if (prodlistjsonObject.has("testList") && !prodlistjsonObject.isNull("testList")) {
					jsonObjectTest = new JSONObject(prodlistjsonObject.get("testList").toString());
				} else {
					System.out.println("Key 'testList' not found in jsonObject1. Assigning an empty JSONObject.");
					jsonObjectTest = new JSONObject(); // Assign an empty object to avoid error
				}

				prodlistjsonObject.remove("testList");
				prodlistjsonObject.remove("jsondata1");

				try {
					prodlistjsonObject = productTaxCalculation(prodlistjsonObject);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (invoiceitemdetails != 0) {
					final String queryinvoice = "update invoiceproductitemdetails set jsondata='"
							+ stringUtilityFunction.replaceQuote(prodlistjsonObject.toString()) + "',jsondata1='"
							+ stringUtilityFunction.replaceQuote(jsonObjectTest.toString()) + "',nlimsproduct="
							+ nlimsproduct + "  where ninvoiceproductitemdetailscode=" + invoiceitemdetails
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					jdbcTemplate.execute(queryinvoice);
				} else {
					int seqnoquotation = jdbcTemplate.queryForObject(
							"select nsequenceno from " + " seqnoinvoice where stablename='invoiceproductitemdetails'",
							Integer.class);
					seqnoquotation++;
					final String invseqstr = String.format("%05d", inputMap.get("ninvoiceseqcode"));
					String insertQuery = "";
					try {
						insertQuery = " INSERT INTO invoiceproductitemdetails(ninvoiceproductitemdetailscode, nserialno, sinvoiceseqno, dinvoicedate, jsondata,jsondata1,nlimsproduct, dmodifieddate, nsitecode, nstatus)"
								+ " VALUES(" + seqnoquotation + "," + seqnoquotation + ",'" + strformatno + "','"
								+ inputMap.get("dquotationdate") + "','"
								+ stringUtilityFunction.replaceQuote(prodlistjsonObject.toString()) + "'::jsonb,'"
								+ stringUtilityFunction.replaceQuote(jsonObjectTest.toString()) + "'::jsonb,"
								+ +nlimsproduct + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
					} catch (Exception e) {
						e.printStackTrace();
					}
					jdbcTemplate.execute(insertQuery);
					jdbcTemplate.execute("update seqnoinvoice set nsequenceno=" + seqnoquotation
							+ "  where stablename='invoiceproductitemdetails' ");
					inputMap.put("nflag", 1);

					actionType.put("InvoicHeader", "IDS_ADDINVOICEHEADER");
					multilingualIDListProduct.add("IDS_ADDINVOICEHEADERPRODUCT");
					try {
						auditUtilityFunction.fnInsertAuditAction(
								getInvoiceProductForAudit(seqnoquotation, "", userInfo), 1, null,
								multilingualIDListProduct, userInfo);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			final String ssymbol = jdbcTemplate.queryForObject(
					"select ssymbol from invoicecurrencytype where ncurrencycode=" + inputMap.get("ncurrencytype"),
					String.class);
			final List<InvoiceProductItemDetails> lstProduct = jdbcTemplate
					.query("SELECT jsondata->>'noverallcost' AS noverallcost, jsondata->>'ntaxamount' AS ntaxamount "
							+ "FROM invoiceproductitemdetails WHERE sinvoiceseqno='" + strformatno + "' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ userInfo.getNmastersitecode(), new InvoiceProductItemDetails());
			final double totalamount = lstProduct.stream().mapToDouble(x -> {
				final String cost = x.getNoverallcost();
				return (cost != null && !cost.trim().isEmpty()) ? Double.parseDouble(cost) : 0.0;
			}).sum();

			final double taxamount = lstProduct.stream().mapToDouble(x -> {
				final String tax = x.getNtaxamount();
				return (tax != null && !tax.trim().isEmpty()) ? Double.parseDouble(tax) : 0.0;
			}).sum();

			final String overAllCost = String.format("%.2f", totalamount);
			final String totaltaxamount = String.format("%.2f", taxamount);

			inputMap.put("ntotalamount", overAllCost);
			final String totalamountinword = getActivecurrencyvalue(inputMap, userInfo);
			final String taxAmountInWords = getAmountInWords(inputMap, userInfo, "totaltaxamount");

			final JSONObject invjsonObject = new JSONObject(inputMap.get("invoiceJson").toString());
			invjsonObject.put("TotalAmountInWords", totalamountinword);
			invjsonObject.put("TotalAmount", overAllCost);
			invjsonObject.put("TotalTaxAmount", totaltaxamount);
			invjsonObject.put("TotalTaxAmountInWords", taxAmountInWords);

			// update for indirect tax amount as to calculate in total amount
			jdbcTemplate.execute("update invoiceheader set ntotalamount='" + (ssymbol + " " + overAllCost)
					+ "',ntotaltaxamount='" + (ssymbol + " " + totaltaxamount) + "',jsondata='"
					+ stringUtilityFunction.replaceQuote(invjsonObject.toString()) + "' where sinvoiceno='"
					+ strformatno + "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

			// update for tax overall calculation for report purpose
			final List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(strformatno, userInfo);
			final String taxJson = objMapper.writeValueAsString(taxList);
			jdbcTemplate.execute("update invoiceheader set jsondata1='"
					+ stringUtilityFunction.replaceQuote(taxJson.toString()) + "' where sinvoiceno='" + strformatno
					+ "'and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

			return getInvoiceHeader(inputMap, userInfo);
		}
		return new ResponseEntity<>(HttpStatus.OK);

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
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> deleteInvoice(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final int ninvoiceseqcode = (int) inputMap.get("invoiceid");

		final List<Map<String, Object>> invoiceHeaderList = (List<Map<String, Object>>) inputMap.get("invoiceheader");
		String invoiceNo = "";
		// Check if the list is not empty
		if (invoiceHeaderList != null && !invoiceHeaderList.isEmpty()) {
			Map<String, Object> invoiceHeader = invoiceHeaderList.get(0);
			// Extract sinvoiceno from the first element
			invoiceNo = (String) invoiceHeader.get("sinvoiceno");
		}

		final String str = "select * from invoicetransactionstatus where sreferenceid='" + invoiceNo + "' "
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<InvoiceTransactionStatus> status = (List<InvoiceTransactionStatus>) jdbcTemplate.query(str,
				new InvoiceTransactionStatus());

		if (status.get(0).getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
				&& status.get(0).getNtransactionstatus() != Enumeration.TransactionStatus.APPROVED
						.gettransactionstatus()) {

			final List<String> multilingualIDListProduct = new ArrayList<>();
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedUnitList = new ArrayList<>();
			final List<InvoiceHeader> invoice = (List<InvoiceHeader>) getInvoiceAlreadyDeletedOrNot(ninvoiceseqcode, "",
					userInfo);

			if (invoice == null) {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final String updateQueryString = "update invoiceheader set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ninvoiceseqcode="
						+ invoice.get(invoice.size() - 1).getNinvoiceseqcode();

				jdbcTemplate.execute(updateQueryString);

				final String queryproduct = "update invoiceproductitemdetails set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where sinvoiceseqno='"
						+ invoice.get(invoice.size() - 1).getSinvoiceno() + "'and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(queryproduct);

				multilingualIDList.add("IDS_DELETEINVOICE");
				multilingualIDListProduct.add("IDS_DELETEINVOICEPRODUCT");
				auditUtilityFunction.fnInsertAuditAction(
						getInvoiceAlreadyDeletedOrNot(ninvoiceseqcode, "invoice", userInfo), 1, null,
						multilingualIDList, userInfo);
				final List<InvoiceProductItemDetails> value = getInvoiceProductForAudit(ninvoiceseqcode, "delete",
						userInfo);
				for (InvoiceProductItemDetails obj : value) {
					savedUnitList.clear();
					savedUnitList.add(obj);
					auditUtilityFunction.fnInsertAuditAction(savedUnitList, 1, null, multilingualIDListProduct,
							userInfo);

				}

			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORDTODELETE",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		return getInvoiceHeader(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to check delete InvoiceAlreadyDeletedOrNot
	 * InvoiceHeader table.
	 * 
	 * @param objInvoiceHeader [InvoiceHeader] object holding detail to be deleted
	 *                         in InvoiceHeader table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public List<InvoiceHeader> getInvoiceAlreadyDeletedOrNot(final int ninvoiceseqcode, final String screenName,
			final UserInfo userInfo) {
		final String strQuery;

		if (screenName.isEmpty()) {
			strQuery = "select ninvoiceseqcode from invoiceheader where ninvoiceseqcode=" + ninvoiceseqcode
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
		} else {
			strQuery = "select ninvoiceseqcode,jsondata->>'CustomerName' as CustomerName,jsondata->>'CustomerType' as CustomerType,"
					+ "jsondata->>'TotalAmountInWords' as TotalAmountInWords,"
					+ "jsondata->>'CustomerGST' as CustomerGST,jsondata->>'PhoneNo' as PhoneNo,"
					+ "jsondata->>'EmailId' as EmailId," + "jsondata->>'Address' as Address,"
					+ "jsondata->>'TotalAmountInWords' as TotalAmountInWords"
					+ " from invoiceheader where ninvoiceseqcode=" + ninvoiceseqcode + " and nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "";

		}
		final List<InvoiceHeader> invoice = (List<InvoiceHeader>) jdbcTemplate.query(strQuery, new InvoiceHeader());

		return invoice;

	}

	/**
	 * This interface declaration is used to delete entry in productInvoiceHeader
	 * table.
	 * 
	 * @param objInvoiceHeader [InvoiceHeader] object holding detail to be deleted
	 *                         in productInvoiceHeader table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> deleteProductInvoiceHeader(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		String str = "";
//		final Map<String, Object> parameterValue = (Map<String, Object>) inputMap.get("parameter");
		Object obj = null;
		int slno = 0;
		if (inputMap.toString().contains("slno")) {
			slno = (int) inputMap.get("slno");
		}
		Object invoiceIdObj = inputMap.get("InvoiceId");
		if (invoiceIdObj != null) {

			str = "select ninvoiceproductitemdetailscode from invoiceproductitemdetails where sinvoiceseqno='"
					+ invoiceIdObj + "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + userInfo.getNmastersitecode();
		} else {

			final Object invoiceIdobj = inputMap.get("InvoiceSeqNo");
			str = "select ninvoiceproductitemdetailscode from invoiceproductitemdetails where sinvoiceseqno='"
					+ invoiceIdobj + "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + userInfo.getNmastersitecode();
		}

		if (inputMap.toString().contains("InvoiceId")) {

			final String queryproduct = "update invoiceproductitemdetails set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where sinvoiceseqno='"
					+ invoiceIdObj + "' and ninvoiceproductitemdetailscode='"
					+ inputMap.get("ninvoiceproductitemdetailscode") + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(queryproduct);

			str = "select jsondata->>'ntaxvalue' as ntaxvalue,jsondata->>'noverallcost' as noverallcost from invoiceproductitemdetails where sinvoiceseqno='"
					+ invoiceIdObj + "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + userInfo.getNmastersitecode();
			obj = inputMap.get("InvoiceId");
		} else if (slno != 0 && slno > 0 && inputMap.toString().contains("InvoiceSeqNo")) {
			final String queryproduct = "update invoiceproductitemdetails set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where sinvoiceseqno='"
					+ inputMap.get("InvoiceSeqNo") + "' and ninvoiceproductitemdetailscode='"
					+ inputMap.get("ninvoiceproductitemdetailscode") + "' and nproductslno=" + slno
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(queryproduct);

			str = "select jsondata->>'ntaxamount' as ntaxamount,jsondata->>'noverallcost' as noverallcost from invoiceproductitemdetails where sinvoiceseqno='"
					+ inputMap.get("InvoiceSeqNo") + "' and nproductslno=" + slno + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			obj = inputMap.get("InvoiceSeqNo");

		} else if (inputMap.toString().contains("InvoiceSeqNo")) {

			final String queryproduct = "update invoiceproductitemdetails set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where sinvoiceseqno='"
					+ inputMap.get("InvoiceSeqNo") + "' and ninvoiceproductitemdetailscode='"
					+ inputMap.get("ninvoiceproductitemdetailscode") + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(queryproduct);

			str = "select jsondata->>'ntaxamount' as ntaxamount,jsondata->>'noverallcost' as noverallcost from invoiceproductitemdetails where sinvoiceseqno='"
					+ inputMap.get("InvoiceSeqNo") + "' and ninvoiceproductitemdetailscode="
					+ inputMap.get("ninvoiceproductitemdetailscode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			obj = inputMap.get("InvoiceSeqNo");

		}

		final List<InvoiceProductItemDetails> productCostList = (List<InvoiceProductItemDetails>) jdbcTemplate
				.query(str, new InvoiceProductItemDetails());

		// Ashik
		if (productCostList != null && !productCostList.isEmpty()) {
			final double taxamount = productCostList.stream().mapToDouble(x -> Double.valueOf(x.getNtaxvalue())).sum();
			final double overallcost = productCostList.stream().mapToDouble(x -> Double.valueOf(x.getNoverallcost()))
					.sum();

			// Use PreparedStatement to prevent SQL injection
			final String value = "update invoiceheader set ntotalamount=?, ntotaltaxamount=? where sinvoiceno=? and nstatus=?";
			jdbcTemplate.update(value, overallcost, taxamount, obj,
					Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
		}
		final List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(obj.toString(), userInfo);
		final String taxJson = objMapper.writeValueAsString(taxList);
		jdbcTemplate.execute("update invoiceheader set jsondata1='"
				+ stringUtilityFunction.replaceQuote(taxJson.toString()) + "' where sinvoiceno='" + obj.toString()
				+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

		return (inputMap.toString().contains("InvoiceSeqNo") ? new ResponseEntity<>(HttpStatus.OK)
				: getInvoiceHeader(inputMap, userInfo));
	}

	/**
	 * This interface declaration is used to get the overall get scheme product with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of ProductforInvoice with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getProductforInvoice(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final String strQuery;
		final int ncustomercode = (int) inputMap.get("ncustomercode");
		final int nschemecode = (int) inputMap.get("nschemecode");
		final int ninvoiceproductitemdetailscode = (int) inputMap.get("ninvoiceproductitemdetailscode");
		final Map<String, Object> outputMap = new HashMap<>();
		final String st = "select * from invoicecustomermaster where ncustomercode=" + ncustomercode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		final List<InvoiceCustomerMaster> list = (List<InvoiceCustomerMaster>) jdbcTemplate.query(st,
				new InvoiceCustomerMaster());
		final int discount = list.get(0).getNdiscountavailable();
		
		List<Map<String, Object>> productList =(List<Map<String, Object>>) inputMap.get("productlist");

		List<String> quotationSeqNos =
		        (productList == null || productList.isEmpty())
		        ? List.of("")
		        : productList.stream()
		            .map(item -> (String) item.get("sproductname"))
		            .filter(s -> s != null && !s.trim().isEmpty())
		            .distinct()
		            .toList();

		String productname = quotationSeqNos.stream().map(s -> "'" + s + "'").collect(Collectors.joining(", "));
		
		if (nschemecode != Enumeration.TransactionStatus.NA.gettransactionstatus() && nschemecode != 0) {

			final String scheme = "SELECT nproductcode FROM invoiceschemesproducts WHERE nschemecode = " + nschemecode
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			final List<InvoiceSchemesProduct> nscheme = jdbcTemplate.query(scheme, new InvoiceSchemesProduct());
			final List<Integer> Productcodes = nscheme.stream().map(InvoiceSchemesProduct::getNproductcode)
					.collect(Collectors.toList());

			final String formattedList = Productcodes.stream().map(String::valueOf).collect(Collectors.joining(", "));
			strQuery = " SELECT DISTINCT COALESCE(isp.jsondata->>'sproductname', ic.sproductname) AS sproductname,"
					+ " ic.nproductcode,ic.nlimsproduct,ic.sproductname AS master_sproductname,ic.ntypecode, "
					+ " ic.slimscode,ic.sdescription,ic.sinvoicedescription,ic.saddtext1,ic.saddtext2,ic.ntaxavailable, "
					+ " itt.staxname,itt.ntax,COALESCE(isp.ncost, ic.ncost) AS ncost FROM invoiceschemesproducts isp "
					+ " JOIN invoiceproductmaster ic ON isp.nproductcode = ic.nproductcode "
					+ " JOIN taxproductdetails tpd ON ic.nproductcode = tpd.nproductcode "
					+ " JOIN invoicetaxtype itt ON itt.ntaxcode = tpd.ntaxcode " + " WHERE ic.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND itt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND tpd.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and isp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " AND ic.nproductcode <> -1 AND isp.nschemecode =" + nschemecode + "  " + " AND itt.ncaltypecode ="
					+ Enumeration.LinkType.EXISTING.getType() + " UNION ALL "
					+ " SELECT DISTINCT ic.sproductname AS sproductname, "
					+ " ic.nproductcode,ic.nlimsproduct,ic.sproductname AS master_sproductname,ic.ntypecode, "
					+ " ic.slimscode,ic.sdescription,ic.sinvoicedescription,ic.saddtext1,ic.saddtext2,ic.ntaxavailable, "
					+ " itt.staxname,itt.ntax, ic.ncost AS ncost " + " FROM invoiceproductmaster ic JOIN "
					+ " taxproductdetails tpd ON ic.nproductcode = tpd.nproductcode "
					+ " JOIN invoicetaxtype itt ON itt.ntaxcode = tpd.ntaxcode " + " WHERE ic.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND itt.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND tpd.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " AND ic.nproductcode <> -1 AND ic.nproductcode NOT IN ( " + " " + formattedList + "" + "    )"
					+ " AND itt.ncaltypecode =" + Enumeration.LinkType.EXISTING.getType() + " union all"
					+ " select distinct ic.sproductname,ic.nproductcode,ic.nlimsproduct,ic.sproductname AS master_sproductname,ic.ntypecode,"
					+ " ic.slimscode,ic.sdescription,ic.sinvoicedescription,ic.saddtext1,ic.saddtext2,ic.ntaxavailable,'' AS staxname,0 AS ntax,ic.ncost"
					+ " from invoiceproductmaster ic left join taxproductdetails tpd ON ic.nproductcode = tpd.nproductcode"
					+ " where ic.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tpd.nproductcode IS NULL  and ic.sproductname not in ("+productname+") order by sproductname";
		} else {
			strQuery = "SELECT ic.nproductcode, ic.nlimsproduct, ic.sproductname, ic.ntypecode, ic.slimscode, "
			        + "ic.sdescription, ic.sinvoicedescription, ic.saddtext1, ic.saddtext2, ic.ntaxavailable, ic.ncost, "
			        + " STRING_AGG(itt.staxname, ', ') AS staxname, COALESCE(SUM(itt.ntax), 0) AS ntax "
			        + "FROM invoiceproductmaster ic "
			        + "LEFT JOIN taxproductdetails tpd ON ic.nproductcode = tpd.nproductcode "
			        + "LEFT JOIN invoicetaxtype itt ON itt.ntaxcode = tpd.ntaxcode AND itt.ncaltypecode = "
			        + Enumeration.LinkType.EXISTING.getType() + " "
			        + "WHERE ic.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
			        + " and ic.sproductname not in ("+productname+") "
			        + "GROUP BY ic.nproductcode, ic.nlimsproduct, ic.sproductname, ic.ntypecode, ic.slimscode, "
			        + "ic.sdescription, ic.sinvoicedescription, ic.saddtext1, ic.saddtext2, ic.ntaxavailable, ic.ncost "
			        + "ORDER BY ic.sproductname";
		}
		final List<InvoiceProductMaster> filteredList = jdbcTemplate.query(strQuery, new InvoiceProductMaster());

		outputMap.put("filteredList", filteredList);
		outputMap.put("Discount", discount);

		final String str = "select testList->>'sspecname' AS sspecname,testList->>'sproducttestname' AS sproducttestname,testList->>'ntestcost' AS ntestcost,testList->>'selected' AS selected,testList->>'nproducttestcode' AS nproducttestcode "
				+ "from invoiceproductitemdetails q,jsonb_array_elements(q.jsondata1 -> 'TestList') AS testList where ninvoiceproductitemdetailscode="
				+ ninvoiceproductitemdetailscode + " and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nsitecode="
				+ userInfo.getNmastersitecode();
		final List<InvoiceTestDetails> testList = (List<InvoiceTestDetails>) jdbcTemplate.query(str,
				new InvoiceTestDetails());
		outputMap.put("selectedTest", testList);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get the overall Invoice Product with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */

	@Override
	public ResponseEntity<Object> getProducts(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> objMap = new HashMap<String, Object>();

		final int filterValue = (int) inputMap.get("products");
		final int ncustomercode = (int) inputMap.get("ncustomercode");
		final int nschemecode = (int) inputMap.get("nschemecode");
		List<Integer> Productcodes = new ArrayList<>();
		final int tax;
		final String st = "Select * from invoiceproductmaster where nproductcode=" + filterValue + "" + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		final List<InvoiceProductMaster> Product = (List<InvoiceProductMaster>) jdbcTemplate.query(st,
				new InvoiceProductMaster());
		if (nschemecode == Enumeration.TransactionStatus.NA.gettransactionstatus()
				|| nschemecode == Enumeration.TransactionStatus.ALL.gettransactionstatus()) {
			
			final String strspec=" select tgs.sspecname,tgt.stestsynonym,tgt.ntestcode from testgroupspecification tgs,"
					+ " testgroupspecsampletype tgss,testgrouptest tgt"
					+ " where tgs.nallottedspeccode=tgss.nallottedspeccode and tgss.nspecsampletypecode=tgt.nspecsampletypecode"
					+ " and tgt.nisvisible="+Enumeration.TransactionStatus.YES.gettransactionstatus()+" "
					+ " and tgs.sspecname in (select max(sspecname) from producttest "
					+ " where sproductname='" + Product.get(0).getSproductname() + "')"
					+ " and tgs.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and tgss.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
			final List<TestGroupSpecification> testgroupspec=(List<TestGroupSpecification>) jdbcTemplate.query(strspec, new TestGroupSpecification());
							
			String testCodes = testgroupspec.stream().map(spec -> String.valueOf(spec.getNtestcode())).collect(Collectors.joining(","));
			
			String str = " select distinct pt.nproducttestcode,pt.ninvproductcode as nproductcode,ipm.nlimsproduct,pt.sspecname,pt.sproducttestname,pt.ntestcost from producttest pt,invoiceproductmaster ipm where "
					+ " ipm.nproductcode = " + filterValue + " and pt.nlimsproductcode=ipm.nlimsproduct "
					+ " and ipm.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ipm.nsitecode=" + userInfo.getNmastersitecode() + " and pt.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pt.nmastersitecode="
					+ userInfo.getNmastersitecode() + " and pt.sproductname='" + Product.get(0).getSproductname() + "'";
			
			// Add testcode condition only if testgroupspec is not empty
			if (testgroupspec != null && !testgroupspec.isEmpty() && testgroupspec.get(0) != null) {
				str += " and pt.ntestcode in("+ testCodes+")";
			}
			
			final List<ProductTest> ProductTestDetails = (List<ProductTest>) jdbcTemplate.query(str, new ProductTest());
			objMap.put("selectedTest", ProductTestDetails);
		} else {
			final String str = " select distinct testList->>'specname' AS sspecname,testList->>'sproductstest' AS sproducttestname,testList->>'cost' AS ntestcost "
					+ " from invoiceschemesproducts inp,invoiceschemes ins,jsonb_array_elements(inp.jsondata2 -> 'TestList') AS testList where "
					+ " inp.nproductcode = " + filterValue + " and inp.nschemecode=" + nschemecode
					+ " and ins.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ins.nsitecode=" + userInfo.getNmastersitecode() + " and inp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and inp.nsitecode="
					+ userInfo.getNmastersitecode();
			final List<ProductTest> ProductTestDetails = (List<ProductTest>) jdbcTemplate.query(str, new ProductTest());
			objMap.put("selectedTest", ProductTestDetails);
		}
		final String GetQryExistingProd = "select * from invoiceexecustomerproducts where " + " nproductcode="
				+ filterValue + " and ncustomercode=" + ncustomercode + " and nschemecode=" + nschemecode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();

		final List<InvoiceExeCustomerProducts> ExistingProducts = (List<InvoiceExeCustomerProducts>) jdbcTemplate
				.query(GetQryExistingProd, new InvoiceExeCustomerProducts());

		final String taxValue = "select itt.ntax as nindirectax,staxname as sindirecttaxname from invoiceproductmaster ic,invoicetaxtype itt JOIN taxproductdetails tpd "
				+ " ON itt.ntaxcode = tpd.ntaxcode where  ic.nproductcode=" + filterValue + " and tpd.nproductcode="
				+ filterValue + " and itt.ncaltypecode=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ic.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ic.nsitecode=" + userInfo.getNmastersitecode() + "" + " and tpd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tpd.nsitecode="
				+ userInfo.getNmastersitecode() + " and itt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and itt.nsitecode="
				+ userInfo.getNmastersitecode();
		final List<InvoiceProductMaster> taxValueList = (List<InvoiceProductMaster>) jdbcTemplate.query(taxValue,
				new InvoiceProductMaster());

		if (ExistingProducts.size() != 0) {
			final String toSetProdcuts = "select nproductcode,nschemecode,sproductname,ncost,ntax, ntaxpercentage, staxname,ntotalcost, noverallcost,ndiscountpercentage, nquantity,nunit,nlimsproduct from invoiceexecustomerproducts where "
					+ " nproductcode=" + filterValue + " and ncustomercode=" + ncustomercode + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			final List<InvoiceExeCustomerProducts> InsertProducts = (List<InvoiceExeCustomerProducts>) jdbcTemplate
					.query(toSetProdcuts, new InvoiceExeCustomerProducts());

			final InvoiceExeCustomerProducts lastInsertedProduct = InsertProducts.get(InsertProducts.size() - 1);
			if (!taxValueList.isEmpty()) {
				lastInsertedProduct.setNindirectax(taxValueList.get(taxValueList.size() - 1).getNindirectax());
				lastInsertedProduct
						.setSindirecttaxname(taxValueList.get(taxValueList.size() - 1).getSindirecttaxname());
			}

			objMap.put("selectedProduct", lastInsertedProduct);

		}

		else if (nschemecode == Enumeration.TransactionStatus.NA.gettransactionstatus() || (nschemecode >= 0)) {
			final String strscheme = "select nproductcode from invoiceschemesproducts where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nschemecode=" + nschemecode
					+ " and nsitecode=" + userInfo.getNmastersitecode();
			final List<InvoiceSchemesProduct> filteredListschemes = (List<InvoiceSchemesProduct>) jdbcTemplate
					.query(strscheme, new InvoiceSchemesProduct());
			Productcodes = filteredListschemes.stream().map(InvoiceSchemesProduct::getNproductcode)
					.collect(Collectors.toList());
			if (!Productcodes.contains(filterValue)) {
				String strQuery = "";
				strQuery = "select * from invoiceproductmaster ic,taxproductdetails td where ic.nproductcode="
						+ filterValue + " and td.nproductcode=" + filterValue + " and ic.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nsitecode="
						+ userInfo.getNmastersitecode() + "" + " and td.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and td.nsitecode="
						+ userInfo.getNmastersitecode();
				final List<InvoiceProductMaster> invprodfilteredList = (List<InvoiceProductMaster>) jdbcTemplate
						.query(strQuery, new InvoiceProductMaster());
				if (!invprodfilteredList.isEmpty()) {
					tax = invprodfilteredList.get(0).getNtaxavailable();
				} else {
					tax = 4;
				}
				if (tax == 3) {
					strQuery = "SELECT ic.nproductcode, ic.ntypecode, ic.nlimsproduct, ic.slimscode, ic.sproductname, ic.ncost, "
							+ " ic.ntaxavailable, SUM(itt.ntax) AS ntax,  STRING_AGG(itt.staxname, ', ') AS staxname, "
							+ " (ic.ncost * SUM(itt.ntax) / 100) AS totaltaxamount " + "FROM invoiceproductmaster ic "
							+ " JOIN taxproductdetails tpd ON ic.nproductcode = tpd.nproductcode "
							+ " JOIN invoicetaxtype itt ON itt.ntaxcode = tpd.ntaxcode " + "WHERE ic.nproductcode = "
							+ filterValue + " " + "AND tpd.nproductcode = " + filterValue + " "
							+ " AND itt.ncaltypecode = " + Enumeration.LinkType.EXISTING.getType() + " "
							+ " AND ic.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ic.nsitecode=" + userInfo.getNmastersitecode() + "" + " AND tpd.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tpd.nsitecode="
							+ userInfo.getNmastersitecode() + "" + " AND itt.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and itt.nsitecode="
							+ userInfo.getNmastersitecode() + ""
							+ " GROUP BY ic.nproductcode, ic.ntypecode, ic.nlimsproduct, ic.slimscode, ic.sproductname, ic.ncost, ic.ntaxavailable";

				} else {
					strQuery = "select * from invoiceproductmaster where nproductcode=" + filterValue + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ userInfo.getNmastersitecode();
				}

				final List<InvoiceProductMaster> filteredList = (List<InvoiceProductMaster>) jdbcTemplate
						.query(strQuery, new InvoiceProductMaster());
				if (filteredList != null && !filteredList.isEmpty()) {
					InvoiceProductMaster lastInsertedProduct = filteredList.get(filteredList.size() - 1);

					lastInsertedProduct.setNschemecode(nschemecode);
					if (!taxValueList.isEmpty()) {
						lastInsertedProduct.setNindirectax(taxValueList.get(taxValueList.size() - 1).getNindirectax());
						lastInsertedProduct
								.setSindirecttaxname(taxValueList.get(taxValueList.size() - 1).getSindirecttaxname());
					}

					objMap.put("selectedProduct", lastInsertedProduct);
				}
			} else {
				final String strquery;
				final String str = "select * from taxproductdetails where nproductcode=" + filterValue + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNmastersitecode();
				final List<TaxProductDetails> taxList = (List<TaxProductDetails>) jdbcTemplate.query(str,
						new TaxProductDetails());
				if (taxList.isEmpty()) {
					strquery = "select isp.nschemecode,isp.ncost,isp.jsondata->>'nproductcode' as nproductcode,isp.jsondata->>'sproductname' as sproductname,"
							+ " isp.jsondata->>'nlimsproduct' as nlimsproduct,isp.jsondata1->>'ntax' as ntax,isp.jsondata1->>'staxname' as staxname, "
							+ "isp.jsondata->>'nactive' as nactive,isp.jsondata1->>'nindirecttax' as nindirectax,isp.jsondata1->>'sindirecttaxname' as sindirecttaxname "
							+ "from invoiceschemesproducts isp where isp.nproductcode=" + filterValue
							+ " and isp.nschemecode=" + nschemecode + " and isp.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and isp.nsitecode="
							+ userInfo.getNmastersitecode();
				} else {
					strquery = "select isp.nschemecode,isp.ncost,isp.jsondata->>'nproductcode' as nproductcode,isp.jsondata->>'sproductname' as sproductname,"
							+ " isp.jsondata->>'nlimsproduct' as nlimsproduct,isp.jsondata1->>'ntax' as ntax,isp.jsondata1->>'staxname' as staxname,"
							+ " isp.jsondata->>'nactive' as nactive,isp.jsondata1->>'nindirecttax' as nindirectax,isp.jsondata1->>'sindirecttaxname' as sindirecttaxname"
							+ " from invoiceschemesproducts isp,taxproductdetails tpd where isp.nproductcode="
							+ filterValue + " and isp.nschemecode=" + nschemecode + "" + " and tpd.ncaltypecode="
							+ Enumeration.LinkType.EXISTING.getType()
							+ " and tpd.nproductcode=isp.nproductcode and tpd.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tpd.nsitecode="
							+ userInfo.getNmastersitecode() + "" + " and isp.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and isp.nsitecode="
							+ userInfo.getNmastersitecode();
				}
				final List<InvoiceProductMaster> filteredList = (List<InvoiceProductMaster>) jdbcTemplate
						.query(strquery, new InvoiceProductMaster());
				objMap.put("selectedProduct", filteredList.get(filteredList.size() - 1));
			}
		}

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}
	/**
	 * This interface declaration is used to get Active Invoice with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getActiveInvoiceById(final Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userinfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference<UserInfo>() {
		});

		String str = " SELECT ninvoiceproductitemdetailscode, sinvoiceseqno, "
		        + " NULLIF(jsondata->>'nindirectax', '')::float AS nindirectax, "
		        + " jsondata->>'sindirecttaxname' AS sindirecttaxname, "
		        + " jsondata->>'sproductname' AS sproductname, "
		        + " COALESCE(jsondata->>'nunit', '') AS nunit, "
		        + " NULLIF(jsondata->>'nquantity', '')::float AS nquantity, "
		        + " NULLIF(jsondata->>'ncost', '')::float AS ncost, "
		        + " NULLIF(jsondata->>'ndiscountpercentage', '')::float AS ndiscountpercentage, "
		        + " jsondata->>'slno' AS slno, "
		        + " jsondata->>'staxname' AS staxname, "
		        + " NULLIF(jsondata->>'ntaxvalue', '')::float AS ntaxvalue, "
		        + " NULLIF(jsondata->>'nproductcode', '')::float AS nproductcode, "
		        + " NULLIF(jsondata->>'ntaxamount', '')::float AS ntaxamount, "
		        + " NULLIF(jsondata->>'ntotalcost', '')::float AS ntotalcost, "
		        + " NULLIF(jsondata->>'noverallcost', '')::float AS noverallcost, "
		        + " NULLIF(jsondata->>'ntaxpercentage', '')::float AS ntaxpercentage, "
		        + " NULLIF(jsondata->>'nserialno', '')::float AS nserialno, "
		        + " COALESCE(jsondata->>'squotationseqno', '') AS squotationseqno "		      
		        + " FROM invoiceproductitemdetails "
		        + " WHERE jsondata->>'sproductname' = '" + inputMap.get("ProductName") + "' "
		        + " AND (NULLIF(jsondata->>'nquantity', '') IS NULL OR jsondata->>'nquantity' ~ '^[0-9]+(\\.[0-9]+)?$') "
		        + " AND ninvoiceproductitemdetailscode = " + inputMap.get("ProductID")
		        + " AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		        + " AND nsitecode = " + userinfo.getNmastersitecode();

		final List<InvoiceProductItemDetails> lstProduct = (List<InvoiceProductItemDetails>) jdbcTemplate.query(str,
				new InvoiceProductItemDetails());
		return new ResponseEntity<>(lstProduct, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to update entry in ProductInvoice table.
	 * 
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be updated
	 *                         in Invoice Product table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings("unused")
	@Override
	public ResponseEntity<Object> updateOuterProductInvoice(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		JSONObject jsonObject = new JSONObject(inputMap.get("productJson").toString());
		final ObjectMapper objMapper = new ObjectMapper();
		final JSONObject jsonObjectTest = new JSONObject(jsonObject.get("testList").toString());
		jsonObject.remove("testList");
		jsonObject = productTaxCalculation(jsonObject);
		
		
		
		String queryproduct = "update invoiceproductitemdetails set jsondata='"
				+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "',jsondata1='"
				+ stringUtilityFunction.replaceQuote(jsonObjectTest.toString()) + "'"
				+ " where ninvoiceproductitemdetailscode=" + inputMap.get("ninvoiceproductitemdetailscode")
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		jdbcTemplate.execute(queryproduct);

		final String str = "select jsondata->>'ntaxamount' as ntaxamount,jsondata->>'noverallcost' as noverallcost from invoiceproductitemdetails where sinvoiceseqno='"
				+ inputMap.get("sinvoiceseqno") + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		final List<InvoiceProductItemDetails> productCostList = (List<InvoiceProductItemDetails>) jdbcTemplate
				.query(str, new InvoiceProductItemDetails());

		final String strq = "select jsondata from invoiceheader where sinvoiceno='" + inputMap.get("sinvoiceseqno")
				+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();

		final List<InvoiceQuotationHeader> invoiceJson = (List<InvoiceQuotationHeader>) jdbcTemplate.query(strq,
				new InvoiceQuotationHeader());

		final JSONObject invoicejsonObject = new JSONObject(invoiceJson.get(invoiceJson.size() - 1).getJsondata());

//Calculate taxamount and overallcost from the multiple list    ------- Mohamed Ashik

		String taxamountSum = "";
		if (!productCostList.get(0).getNtaxamount().isEmpty())

		{

			final double taxAmount = productCostList.stream()
					.mapToDouble(x -> Optional.ofNullable(x.getNtaxamount())
							.filter(taxAmountStr -> !taxAmountStr.trim().isEmpty()).map(Double::valueOf).orElse(0.0))
					.sum();

			taxamountSum = String.format("%.2f", taxAmount);

		}

		else {
			final double taxamount = 0;
		}

		final double overallcost = productCostList.stream().mapToDouble(x -> Double.valueOf(x.getNoverallcost())).sum();
		final String strcurrencytype = "select ncurrencytype from invoiceheader where sinvoiceno='"
				+ inputMap.get("sinvoiceseqno") + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		final List<InvoiceHeader> invoiceList = (List<InvoiceHeader>) jdbcTemplate.query(strcurrencytype,
				new InvoiceHeader());

		final String overallcostSum = String.format("%.2f", overallcost);
		final int ncurrencytype = invoiceList.get(invoiceList.size() - 1).getNcurrencytype();
		final String ssymbol = jdbcTemplate
				.queryForObject("select ssymbol from invoicecurrencytype where ncurrencycode=" + ncurrencytype + ""
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNmastersitecode() + "", String.class);
		inputMap.put("ntotalamount", overallcostSum);
		final String totalamountinword = getActivecurrencyvalue(inputMap, userInfo);
		final String taxAmountInWords = getAmountInWords(inputMap, userInfo, "taxamountSum");
		invoicejsonObject.put("TotalAmountInWords", totalamountinword);
		invoicejsonObject.put("TotalAmount", overallcostSum);
		invoicejsonObject.put("TotalTaxAmount", taxAmountInWords);
		jdbcTemplate.execute("update invoiceheader set ntotalamount='" + (ssymbol + " " + overallcostSum)
				+ "',ntotaltaxamount='" + (ssymbol + " " + taxamountSum) + "',jsondata='"
				+ stringUtilityFunction.replaceQuote(invoicejsonObject.toString()) + "' where sinvoiceno='"
				+ inputMap.get("sinvoiceseqno") + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

		// update for tax overall calculation for report purpose
		final String sinvoiceseqno = (String) inputMap.get("sinvoiceseqno");
		final List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(sinvoiceseqno, userInfo);
		final String taxJson = objMapper.writeValueAsString(taxList);
		jdbcTemplate.execute("update invoiceheader set jsondata1='"
				+ stringUtilityFunction.replaceQuote(taxJson.toString()) + "' where sinvoiceno='" + sinvoiceseqno
				+ "'and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
		return getInvoiceHeader(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to get the overall Invoice ProductArno
	 * with respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> getInvoiceProductArno(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final List<Object> ArnoList = (List<Object>) inputMap.get("Arno");
		final List<Object> List = new ArrayList<>();
		int nsqno = 1;
		final Map<String, Object> selectedRecord = (Map<String, Object>) inputMap.get("selectedRecord");
		final String customerType = (String) selectedRecord.get("scustomertypename");

		if (customerType == null || customerType.isEmpty()) {

			return new ResponseEntity<>(
				    commonFunction.getMultilingualMessage("IDS_ENTERCUSTOMERTYPE", userInfo.getSlanguagefilename()),
				    HttpStatus.BAD_REQUEST
				);
		}
		List<InvoiceProductItemDetails> ProductDetails = new ArrayList<>();
		if (ArnoList.size() != 0) {

			for (Object objname : ArnoList) {

				final Object Preregno = ((HashMap<String, Object>) objname).get("npreregno");
				String Strsample = "select distinct jsondata->>'ntransactiontestcode' AS nproducttestcode, "
						+ "jsondata->>'ssectionname' AS sspecname, "
						+ "jsondata->>'stestname' AS sproducttestname, " + "jsondata->>'ncost' AS ntestcost "
						+ "from registrationtest where npreregno=" + Preregno + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final List<SampleType> Product = (List<SampleType>) jdbcTemplate.query(Strsample, new SampleType());
				if (Product.size() != Enumeration.TransactionStatus.ALL.gettransactionstatus()) {
					final String Str = " select * from view_arnoproduct WHERE npreregno=" + Preregno + " ";

					ProductDetails = (List<InvoiceProductItemDetails>) jdbcTemplate.query(Str,
							new InvoiceProductItemDetails());
				} else {
					final String Str = " select * from view_arnospecimen WHERE npreregno=" + Preregno + " ";
					ProductDetails = (List<InvoiceProductItemDetails>) jdbcTemplate.query(Str,
							new InvoiceProductItemDetails());
				}

				if (ProductDetails.size() != 0) {

					final String str = "select distinct jsondata->>'ntransactiontestcode' AS nproducttestcode, "
							+ " jsondata->>'ssectionname' AS sspecname, "
							+ " jsondata->>'stestname' AS sproducttestname, " + "jsondata->>'ncost' AS ntestcost "
							+ " from registrationtest where npreregno=" + Preregno + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode();
					final List<ProductTest> testList = (List<ProductTest>) jdbcTemplate.query(str, new ProductTest());

					// Assuming testList is already populated and contains ProductTest objects
					for (int i = 0; i < ProductDetails.size(); i++) {
						final InvoiceProductItemDetails obj = ProductDetails.get(i); // Get the current product
						final String Productname = obj.getSproductname(); // Get the product name
						final double productcost = obj.getNcost(); // Get the product cost
						String tax = obj.getNtaxpercentage();
						final double producttaxpercentage = (tax == null || tax.isBlank()) ? 0.0 : Double.valueOf(tax); // Get the tax percentage
						final String taxPercentage = String.format("%.2f", producttaxpercentage); // Format the tax percentage
						// Calculate product tax value

						double totalTestCost = 0.0;

						for (ProductTest test : testList) {
							final double ntestcost = test.getNtestcost(); // Get the ntestcost from each ProductTest
							totalTestCost += ntestcost; // Accumulate the total test cost
						}

						// percentage
						final double producttaxvalue = (producttaxpercentage / 100) * totalTestCost;

						// Calculate totalAmount including the updated producttaxvalue
						final double totalAmount = producttaxvalue + totalTestCost;

						// You can now use the updated totalAmount for further calculations or
						// processing

						// You can now use the updated totalAmount in your further logic

						final DecimalFormat df = new DecimalFormat("#.##");
						final String formattedTotalAmount = df.format(totalAmount);

						obj.setSproductname(Productname);
						obj.setNtaxpercentage(taxPercentage);
						obj.setNtaxvalue(String.valueOf(producttaxvalue));
						obj.setNcost(productcost);
						obj.setNtotalcost(String.valueOf(totalTestCost));
						obj.setNoverallcost(String.valueOf(formattedTotalAmount));
						obj.setNdiscountpercentage("0");
						obj.setNquantity("1");
						obj.setSlno(nsqno);
						obj.setNoverallcostvalue(String.valueOf(formattedTotalAmount));
						obj.setNtaxamount(String.valueOf(producttaxvalue));
						obj.setNproductcode(obj.getNproductcode());
						obj.setNindirectax(0);
						obj.setTestList(testList);
						List.add(obj);
						nsqno++;

						final String taxValue = "select itt.ntax as nindirectax,staxname as sindirecttaxname from invoiceproductmaster ic,invoicetaxtype itt JOIN taxproductdetails tpd "
								+ "ON itt.ntaxcode = tpd.ntaxcode where  ic.nproductcode=" + obj.getNproductcode()
								+ " and tpd.nproductcode=" + obj.getNproductcode() + " and itt.ncaltypecode="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nsitecode="+userInfo.getNmastersitecode()
								+ " and tpd.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and tpd.nsitecode="+userInfo.getNmastersitecode()
								+ " and itt.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and itt.nsitecode="+userInfo.getNmastersitecode();
						final List<InvoiceProductMaster> taxValueList = (List<InvoiceProductMaster>) jdbcTemplate
								.query(taxValue, new InvoiceProductMaster());

						obj.setNindirectax(
								!taxValueList.isEmpty() ? taxValueList.get(taxValueList.size() - 1).getNindirectax()
										: 0);
						obj.setSindirecttaxname(!taxValueList.isEmpty()
								? taxValueList.get(taxValueList.size() - 1).getSindirecttaxname()
								: null);

					}
					outputMap.put("ProductDetails", List);
				}
			}

		}

		if (!List.isEmpty()) {
			final double taxamount = List.stream()
					.mapToDouble(x -> Double.valueOf(((InvoiceProductItemDetails) x).getNtaxvalue())).sum();
			final double overallcost = List.stream()
					.mapToDouble(x -> Double.valueOf(((InvoiceProductItemDetails) x).getNoverallcost())).sum();

			outputMap.put("ntotalamount", overallcost);

			final String totalamountinword = getActivecurrencyvalue(outputMap, userInfo);
			outputMap.put("TotalAmountInWords", totalamountinword);
			final String taxAmountInWords = getAmountInWords(inputMap, userInfo, "taxamount");
			outputMap.put("ntotaltaxamount", taxAmountInWords);

		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
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
	public ResponseEntity<Object> getInvoicesearch(final Map<String, Object> inputMap) throws Exception {

		final Map<String, Object> objMap = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		final String strQuery = "select ninvoiceseqcode,sinvoiceno,squotationid,ncustomercode,sprojectcode,sprocessno,sschemename,spackdoctrefno,"
				+ " spackagerefdetails,ntotalamount,ntotaltaxamount,ntotalfrightcharges,npaymentcode,spaymentdetails, nbankcode,ncurrencytype,nstatus,"
				+ " jsondata||json_build_object('ninvoiceseqcode',ninvoiceseqcode)::jsonb "
				+ " as jsondata,jsondata->>'CustomerName' as CustomerName," + " COALESCE(TO_CHAR(dinvoicedate,'"
				+ userInfo.getSsitedate() + "'),'') as sinvoicedate," + " COALESCE(TO_CHAR(dquotationdate,'"
				+ userInfo.getSsitedate() + "'),'') as squotationdate," + " COALESCE(TO_CHAR(dprocessdate,'"
				+ userInfo.getSsitedate() + "'),'') as sprocessdate," + " COALESCE(TO_CHAR(dorderreferencedate,'"
				+ userInfo.getSsitedate() + "'),'') as sorderreferencedate," + " COALESCE(TO_CHAR(dpackdocrefdate,'"
				+ userInfo.getSsitedate() + "'),'') as spackdocrefdate" + " from invoiceheader where ninvoiceseqcode="
				+ inputMap.get("ninvoiceseqcode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + "  order by ninvoiceseqcode ";

		final List<InvoiceHeader> headerList = jdbcTemplate.query(strQuery, new InvoiceHeader());

		objMap.put("selectedInvoice", headerList);

		final List<InvoiceProductItemDetails> productList = getProductsforInvoiceInsert(
				headerList.get(headerList.size() - 1).getSinvoiceno(), userInfo);
		objMap.put("selectedProduct", productList);

		return new ResponseEntity<>(objMap, HttpStatus.OK); // status // code:200
	}

	/**
	 * This interface declaration is used to invoiceReport Generate with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> invoiceReportGenerate(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		final InvoiceHeader invoice = mapper.convertValue(inputMap.get("selectedInvoice"), InvoiceHeader.class);

		final List<Map<String, Object>> product = mapper.convertValue(inputMap.get("selectedProduct"),
				new TypeReference<List<Map<String, Object>>>() {
				});

		if (invoice != null && product != null) {

			final String queryseq = "select ninvoiceseqcode,sinvoiceno from invoiceheader where ninvoiceseqcode ="
					+ inputMap.get("ninvoiceseqcode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			final List<InvoiceHeader> lstInvoice = jdbcTemplate.query(queryseq, new InvoiceHeader());

			final String str = "select ninvoiceproductitemdetailscode from invoiceproductitemdetails where sinvoiceseqno='"
					+ invoice.getSinvoiceno() + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			final List<InvoiceProductItemDetails> productList = (List<InvoiceProductItemDetails>) jdbcTemplate
					.query(str, new InvoiceProductItemDetails());

			if (productList != null && lstInvoice != null) {

				final Map<String, Object> returnMap = new HashMap<>();
				final String sfileName;
				String sJRXMLname = "";
				int qType = 1;
				int ncontrolCode = -1;
				final String sfilesharedfolder;
				String fileuploadpath = "";
				final String subReportPath;
				final String imagePath;
				final String pdfPath;
				final String sreportingtoolURL;
				final String signpath;
				final String getFileuploadpath = "select ssettingvalue from reportsettings where nreportsettingcode in ("
						+ Enumeration.ReportSettings.REPORT_PATH.getNreportsettingcode() + ","
						+ Enumeration.ReportSettings.REPORT_PDF_PATH.getNreportsettingcode() + ","
						+ Enumeration.ReportSettings.REPORTINGTOOL_URL.getNreportsettingcode() + ") "
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by nreportsettingcode";

				final List<String> reportPaths = jdbcTemplate.queryForList(getFileuploadpath, String.class);
				fileuploadpath = reportPaths.get(0);
				subReportPath = reportPaths.get(0);
				imagePath = reportPaths.get(0);
				pdfPath = reportPaths.get(1);
				sreportingtoolURL = reportPaths.get(2);
				if (inputMap.containsKey("ncontrolcode")) {
					ncontrolCode = (int) inputMap.get("ncontrolcode");
				}

				sJRXMLname = "InvoiceQuotation.jrxml";
				sfileName = "InvoiceQuotation_" + inputMap.get("nquotationseqcode");
				;
				inputMap.put("ncontrolcode",
						(int) inputMap.get("nreporttypecode") == Enumeration.ReportType.CONTROLBASED.getReporttype()
								? inputMap.get("ncontrolcode")
								: ncontrolCode);

				final UserInfo userInfoWithReportFormCode = new UserInfo(userInfo);
				userInfoWithReportFormCode.setNformcode((short) Enumeration.FormCode.REPORTCONFIG.getFormCode());
				final Map<String, Object> dynamicReport = reportDAOSupport.getDynamicReports(inputMap,
						userInfoWithReportFormCode);
				final String folderName;
				if (((String) dynamicReport.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))
						.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {

					sJRXMLname = (String) dynamicReport.get("JRXMLname");
					folderName = (String) dynamicReport.get("folderName");
					fileuploadpath = fileuploadpath + folderName;
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOREPORTFOUNDFORSPEC",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

				sfilesharedfolder = fileuploadpath + sJRXMLname;
				final File JRXMLFile = new File(sfilesharedfolder);
				final String struser = "Select * from userdigitalsign where nusercode=" + userInfo.getNusercode() + ""
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNmastersitecode();

				final List<DigitalSignature> sign = (List<DigitalSignature>) jdbcTemplate.query(struser,
						new DigitalSignature());
				String Spassword = "";
				if (sign != null && !sign.isEmpty()) {
					String securityKeyDecrypted = passwordUtilityFunction.decryptPassword("userdigitalsign",
							"nusercode", userInfo.getNusercode(), "ssecuritykey");
					Spassword = securityKeyDecrypted;
				}
				final String struserfile = "Select * from userfile where nusercode=" + userInfo.getNusercode() + ""
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNmastersitecode();
				final List<UserFile> UserFile = (List<UserFile>) jdbcTemplate.query(struserfile, new UserFile());

				final String report = "select ssettingvalue from reportsettings where nreportsettingcode= 22 "
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final List<String> reportPathss = jdbcTemplate.queryForList(report, String.class);

				signpath = reportPathss.get(0);

				if (sJRXMLname != null && !sJRXMLname.equals("")) {

					final Map<String, Object> jasperParameter = new HashMap<>();
					jasperParameter.put("nreportdetailcode", dynamicReport.get("nreportdetailcode"));
					//jasperParameter.put("nreporttypecode", (int) inputMap.get("nreporttypecode"));
					jasperParameter.put("nreporttypecode", 0);
					jasperParameter.put("ssubreportpath", subReportPath + folderName);
					jasperParameter.put("simagepath", imagePath + folderName);
					jasperParameter.put("language", userInfo.getSlanguagetypecode());
					jasperParameter.put("ninvoiceseqcode", inputMap.get("ninvoiceseqcode"));
					jasperParameter.put("sprimarykeyname", inputMap.get("ninvoiceseqcode"));
					jasperParameter.put("sinvoiceno", invoice.getSinvoiceno());
					jasperParameter.put("sreportingtoolURL", sreportingtoolURL);
					if (sign != null && !sign.isEmpty()) {
						returnMap.putAll(reportDAOSupport.compileAndPrintReport(jasperParameter, JRXMLFile, qType,
								pdfPath, sfileName, userInfo, "", ncontrolCode, false,
								signpath + UserFile.get(0).getSsignimgftp(), signpath + sign.get(0).getSdigisignftp(),
								Spassword));
					} else {
						returnMap.putAll(reportDAOSupport.compileAndPrintReport(jasperParameter, JRXMLFile, qType,
								pdfPath, sfileName, userInfo, "", ncontrolCode, false, "", "", ""));
					}
					final String uploadStatus = (String) returnMap
							.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
					if (uploadStatus.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
						if (inputMap.containsKey("nreporttypecode") && dynamicReport.containsKey("nreportdetailcode")) {
							final String invoiceReportHistory = "select nsequenceno from seqnoinvoice"
									+ " where stablename ='invoicereporthistory' and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							final int reportHistorySeqno = jdbcTemplate.queryForObject(invoiceReportHistory,
									Integer.class) + 1;

							final String insertReportHistoryquery = "INSERT INTO invoicereporthistory(ninvoicereporthistorycode,"
									+ " ninvoiceseqcode, nreporttypecode, nreportdetailcode, nusercode, nuserrolecode, ssystemfilename,"
									+ " dgenerateddate, ntzgenerateddate, noffsetdgenerateddate, dmodifieddate, nsitecode, nstatus)"
									+ " VALUES (" + reportHistorySeqno + ", " + inputMap.get("ninvoiceseqcode") + ", "
									+ inputMap.get("nreporttypecode") + ", " + dynamicReport.get("nreportdetailcode")
									+ ", " + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
									+ returnMap.get("outputFileName") + "', '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
									+ userInfo.getNtimezonecode() + ","
									+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
									+ userInfo.getNtranssitecode() + ", "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

							jdbcTemplate.execute(insertReportHistoryquery);
							jdbcTemplate.execute("update seqnoinvoice set nsequenceno = " + reportHistorySeqno
									+ " where stablename='invoicereporthistory' and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
						}
						returnMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
						final String auditAction = "IDS_INVOICEREPORT";
						final String comments = commonFunction.getMultilingualMessage("IDS_INVOICENO",
								userInfo.getSlanguagefilename()) + ": " + invoice.getSinvoiceno() + "; ";
						final Map<String, Object> outputMap = new HashMap<>();
						outputMap.put("stablename", "invoiceheader");
						outputMap.put("sprimarykeyvalue", inputMap.get("ninvoiceseqcode"));
						auditUtilityFunction.insertAuditAction(userInfo, auditAction, comments, outputMap);
					}
				} else {

					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_NOREPORTFOUND", userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);

				}
				return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_REPORTCANNOTGENERATEWITHOUTPRODUCT",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}

	/**
	 * This interface declaration is used to add a new entry to InvoiceHeader table
	 * for TransactionStatus.
	 * 
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be added in
	 *                         InvoiceHeader table
	 * @return response entity object holding response status and data of added
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createTransactionStatusForInvoice(final int seqnoquotation,
			final UserInfo objUserInfo, final String strformat) throws Exception {

		final String str;
		int seqnotransactionstatus = jdbcTemplate.queryForObject(
				"select nsequenceno from " + " seqnoinvoice where stablename='invoicetransactionstatus'",
				Integer.class);
		seqnotransactionstatus++;

		final String needrights = "select nneedrights from userrolefieldcontrol ufc,fieldmaster fm where fm.sfieldname='invoicefield' and fm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and fm.nfieldcode=ufc.nfieldcode and ufc.nformcode=" + objUserInfo.getNformcode()
				+ " and ufc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and fm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final int needapprovalrights = jdbcTemplate.queryForObject(needrights, Integer.class);
		final String user = "select * from userfile where nusercode=" + objUserInfo.getNusercode() + ""
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ objUserInfo.getNmastersitecode();
		final List<UserFile> usersign = (List<UserFile>) jdbcTemplate.query(user, new UserFile());
		if (needapprovalrights != Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			str = " INSERT INTO invoicetransactionstatus(ntransactionid, sreferenceid, nreferencetype, sdraftuser, ddraftdate, sapproveduser, dapproveddate, sinvoiceduser,dinvoiceddate,ntransactionstatus,sref1,sref2,sext1,sext2,nstatus,simageid)"
					+ " VALUES(" + seqnotransactionstatus + ",'" + strformat + "',"
					+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + ",'" + objUserInfo.getSusername()
					+ "','" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "','" + "" + "'," + null + ",'" + ""
					+ "'," + null + "," + +Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'" + ""
					+ "','" + "" + "','" + "" + "','" + "" + "',"
					  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'" 
					    + usersign.get(0).getSsignimgftp() + "')";
			
		} else {
			str = " INSERT INTO invoicetransactionstatus(ntransactionid, sreferenceid, nreferencetype, sdraftuser, ddraftdate, sapproveduser, dapproveddate, sinvoiceduser,dinvoiceddate,ntransactionstatus,sref1,sref2,sext1,sext2,nstatus,simageid)"
					+ " VALUES(" + seqnotransactionstatus + ",'" + strformat + "',"
					+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + ",'" + objUserInfo.getSusername()
					+ "','" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "','" + "" + "'," + null + ",'" + ""
					+ "'," + null + "," + +Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ",'" + ""
					+ "','" + "" + "','" + "" + "','" + "" + "',"
					 + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'" 
					    + usersign.get(0).getSsignimgftp() + "')";
		}

		jdbcTemplate.execute(str);
		jdbcTemplate.execute("update seqnoinvoice set nsequenceno=" + seqnotransactionstatus
				+ "  where stablename='invoicetransactionstatus' ");

		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to approveInvoice with respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */

	@SuppressWarnings("unused")
	public ResponseEntity<Object> approveInvoiceRecord(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<InvoiceHeader> approvelist = (List<InvoiceHeader>) objmapper
				.convertValue(inputMap.get("selectedInvoice"), new TypeReference<List<InvoiceHeader>>() {
				});
		final List<InvoiceHeader> searchlist = (List<InvoiceHeader>) objmapper
				.convertValue(inputMap.get("searchedInvoice"), new TypeReference<List<InvoiceHeader>>() {
				});

		// Loop through the invoice list to check customerGST
		for (InvoiceHeader obj : approvelist) {
			final String customerGST = obj.getJsondata() != null ? (String) obj.getJsondata().get("CustomerGST") : null;

			// Only authenticate if CustomerGST is not null
			String authToken = null;
			if (customerGST != "") {
				EInvoiceService eInvoiceService = new EInvoiceService();
				authToken = eInvoiceService.authenticate(); // Only authenticate if CustomerGST is present
			}

			final String str = "select * from invoicetransactionstatus where sreferenceid='" + obj.getSinvoiceno() + "'"
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<InvoiceTransactionStatus> status = (List<InvoiceTransactionStatus>) jdbcTemplate.query(str,
					new InvoiceTransactionStatus());

			if (status.get(0).getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
					&& status.get(0).getNtransactionstatus() != Enumeration.TransactionStatus.APPROVED
							.gettransactionstatus()) {

				if (approvelist != null) {
					for (InvoiceHeader invoiceObj : approvelist) {
						// Update transaction status for the invoice
						final String user = "select * from userfile where nusercode=" + userInfo.getNusercode() + ""
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nsitecode=" + userInfo.getNmastersitecode();
						final List<UserFile> usersign = (List<UserFile>) jdbcTemplate.query(user, new UserFile());

						
						final String querystatus = "UPDATE invoicetransactionstatus SET ntransactionstatus="
							    + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", dapproveddate='"
							    + dateUtilityFunction.getCurrentDateTime(userInfo) + "', sapproveduser='"
							    + userInfo.getSusername() 
							    + "' WHERE sreferenceid='" + invoiceObj.getSinvoiceno() + "' AND nstatus="
							    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//						final String querystatus = "UPDATE invoicetransactionstatus SET ntransactionstatus="
//								+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", dapproveddate='"
//								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', sapproveduser='"
//								+ userInfo.getSusername() + "', simageid='" + usersign.get(0).getSsignimgftp() + "'"
//								+ " WHERE sreferenceid='" + invoiceObj.getSinvoiceno() + "' AND nstatus="
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						jdbcTemplate.execute(querystatus);
						multilingualIDList.add("IDS_APPROVEINVOICE");
						auditUtilityFunction.fnInsertAuditAction(
								getapproveauditinvoice(invoiceObj.getSinvoiceno(), userInfo), 1, null,
								multilingualIDList, userInfo);

						// Only proceed with IRN generation if the customerGST is not null
						if (customerGST != "") {
							// Call external API with authentication token for IRN generation
							generateIRNForInvoice(authToken, getirninvoice(obj.getSinvoiceno(), userInfo), userInfo);
						}
					}
				}

				// Checking if IRN generation is successful
				final String irnErrorMessage = "SELECT sirnstatusmessage FROM invoiceirn WHERE sinvoiceno = '"
						+ approvelist.get(0).getSinvoiceno() + "'";

				final List<InvoiceIrn> statusirn = (List<InvoiceIrn>) jdbcTemplate.query(irnErrorMessage,
						new InvoiceIrn());

			}
		}
		// If only one invoice is searched, return header for that invoice
		if (searchlist != null && searchlist.size() == 1) {
			return getInvoiceHeaders(userInfo);
		}

		return getInvoiceHeader(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to add a new entry to InvoiceHeader table
	 * for generateIRNForInvoice.
	 * 
	 * @param objInvoiceHeader [InvoiceHeader] object holding details to be added in
	 *                         InvoiceHeader table
	 * @return response entity object holding response status and data of added
	 *         InvoiceHeader object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings("unused")
	public ResponseEntity<Object> generateIRNForInvoice(final String authToken, final List<InvoiceHeader> obj,
			final UserInfo userInfo) throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost request = new HttpPost(
				"https://apisandbox.whitebooks.in/einvoice/type/GENERATE/version/V1_03?email=dhivyadhakshna25@gmail.com");

		// **Headers**
		request.addHeader("Content-Type", "application/json");
		request.addHeader("Auth-token", authToken);
		request.addHeader("username", "BVMGSP");
		request.addHeader("gstin", "29AAGCB1286Q000");
		request.addHeader("client_id", "EINS9bb12e46-1325-4d63-b418-bca3ac9bc2d8");
		request.addHeader("client_secret", "EINSe3b9b484-f601-44e3-a6a1-c18ee8ffbdd7");
		request.addHeader("email", "dhivyadhakshna25@gmail.com");

		// Assuming obj is a list of InvoiceHeader, use the first item (obj.get(0))
		final String sinvoiceno = obj.get(0).getSinvoiceno(); // Get the invoice number from the first item
		final Instant sinvoicedate = obj.get(0).getDinvoicedate(); // Get the invoice date from the first item
		final LocalDate localDate = sinvoicedate.atZone(ZoneId.systemDefault()).toLocalDate();
		final String formattedDate = localDate.format(DateTimeFormatter.ofPattern(userInfo.getSsitereportdate()));

		// String formattedDate =
		// localDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
		final String query = "SELECT COALESCE(ipd.jsondata->>'sproductname', '') AS sproductname, "
				+ "COALESCE(ipd.jsondata->>'nquantity', '0') AS nquantity, "
				+ "COALESCE(ipd.jsondata->>'nunit', '0') AS nunit, "
				+ "COALESCE(ipd.jsondata->>'ncost', '0.0') AS ncost, "
				+ "COALESCE(ipd.jsondata->>'noverallcostvalue', '0.0') AS noverallcostvalue, "
				+ "COALESCE(ipd.jsondata->>'ndiscountpercentage', '0.0') AS ndiscountpercentage, "
				+ "COALESCE(ipd.jsondata->>'ntotalcost', '0.0') AS ntotalcost " + "FROM invoiceproductitemdetails ipd "
				+ "WHERE ipd.sinvoiceseqno = '" + sinvoiceno + "' and ipd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ipd.nsitecode="
				+ userInfo.getNmastersitecode();
		final List<InvoiceProductItemDetails> status = (List<InvoiceProductItemDetails>) jdbcTemplate.query(query,
				new InvoiceProductItemDetails());

		// **JSON Payload**
		JSONObject payload = new JSONObject();
		payload.put("Version", "1.1");

		// **Transaction Details**
		JSONObject tranDtls = new JSONObject();
		tranDtls.put("TaxSch", "GST");
		tranDtls.put("SupTyp", "B2B");
		payload.put("TranDtls", tranDtls);

		// **Document Details**
		JSONObject docDtls = new JSONObject();
		docDtls.put("Typ", "INV");
		docDtls.put("No", sinvoiceno); // Use sinvoiceno here
		docDtls.put("Dt", formattedDate); // Use sinvoicedate here
		payload.put("DocDtls", docDtls);

		// **Seller Details**
		JSONObject sellerDtls = new JSONObject();
		sellerDtls.put("Gstin", "29AAGCB1286Q000");
		sellerDtls.put("LglNm", "ABC company pvt ltd");
		sellerDtls.put("Addr1", "5th block, kuvempu layout");
		sellerDtls.put("Loc", "GANDHINAGAR");
		sellerDtls.put("Pin", 560001);
		sellerDtls.put("Stcd", "29");
		payload.put("SellerDtls", sellerDtls);

		final String customerGSTIN = obj.get(0).getCustomerGST();
		final String extractedStateCode = customerGSTIN.substring(0, 2);

		// Fetch PIN from Database

		final String strpin = "select npinCode from invoicestatepincode where sstateCode = '" + extractedStateCode
				+ "'";
		final List<InvoiceStatePinCode> pin = (List<InvoiceStatePinCode>) jdbcTemplate.query(strpin,
				new InvoiceStatePinCode());
		final JSONObject buyerDtls = new JSONObject();
		buyerDtls.put("Gstin", customerGSTIN);
		buyerDtls.put("LglNm", "XYZ company pvt ltd");
		if (obj != null && !obj.isEmpty() && obj.get(0).getAddress() != null && !obj.get(0).getAddress().isEmpty()) {

			buyerDtls.put("Addr1", obj.get(0).getAddress());

		}

		buyerDtls.put("Loc", "GANDHINAGAR");

		if (pin.size() != 0) {

			buyerDtls.put("Pin", pin.get(0).getNpinCode());

		}

		buyerDtls.put("Stcd", extractedStateCode);
		buyerDtls.put("POS", extractedStateCode);

		payload.put("BuyerDtls", buyerDtls);

		String sirnStatusMessage = "IRN Generated Successfully";
		final JSONArray itemList = new JSONArray();
		for (InvoiceProductItemDetails item : status) {
			final String strn = "select saddtext1 from invoiceproductmaster where sproductname = '"
					+ item.getSproductname() + "'" + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			final List<InvoiceProductMaster> product = (List<InvoiceProductMaster>) jdbcTemplate.query(strn,
					new InvoiceProductMaster());
			final String strunit = "Select * from unitsofinvoice where sunitname ='" + item.getNunit() + "'";

			final List<UnitsofInvoice> unit = (List<UnitsofInvoice>) jdbcTemplate.query(strunit, new UnitsofInvoice());

			final JSONObject itemDetails = new JSONObject();
			itemDetails.put("SlNo", "1");
			itemDetails.put("IsServc", "N");
			itemDetails.put("PrdDesc", item.getSproductname());
			itemDetails.put("HsnCd", product.get(0).getSaddtext1());
			itemDetails.put("Qty", item.getNquantity());
			itemDetails.put("Unit", item.getNunit());
			itemDetails.put("UnitPrice", item.getNcost());
			itemDetails.put("TotAmt", item.getNoverallcostvalue());

			final double totalAmt = Double.parseDouble(item.getNoverallcostvalue());
//			double discount = item.getNdiscountpercentage() == null ? 0.0
//					: Double.parseDouble(item.getNdiscountpercentage());
			final double discount = (item.getNdiscountpercentage() == null || item.getNdiscountpercentage().isEmpty())
					? 0.0
					: Double.parseDouble(item.getNdiscountpercentage());
			final double taxableValue = Math.round((totalAmt - discount) * 100.0) / 100.0;
			itemDetails.put("AssAmt", taxableValue);
			itemDetails.put("Discount", discount);

			double gstRate = 12.0; // Example GST rate
			final boolean isIntraState = sellerDtls.getString("Stcd")
					.equals(payload.getJSONObject("BuyerDtls").getString("POS"));
			itemDetails.put("GstRt", gstRate);
			double cgstAmt = isIntraState ? (taxableValue * (gstRate / 2)) / 100 : 0.00;
			double sgstAmt = isIntraState ? (taxableValue * (gstRate / 2)) / 100 : 0.00;
			double igstAmt = isIntraState ? 0.00 : (taxableValue * gstRate) / 100;

			itemDetails.put("CgstAmt", Math.round(cgstAmt * 100.0) / 100.0);
			itemDetails.put("SgstAmt", Math.round(sgstAmt * 100.0) / 100.0);
			itemDetails.put("IgstAmt", Math.round(igstAmt * 100.0) / 100.0);

			final double totalItemValue = taxableValue + cgstAmt + sgstAmt + igstAmt;
			itemDetails.put("TotItemVal", Math.round(totalItemValue * 100.0) / 100.0);

			// ✅ Condition-Based Tax Handling
			if (isIntraState) {
				itemDetails.put("CGST", Math.round(cgstAmt * 100.0) / 100.0);
				itemDetails.put("SGST", Math.round(sgstAmt * 100.0) / 100.0);
				itemDetails.put("IGST", 0.00);
			} else {
				itemDetails.put("CGST", 0.00);
				itemDetails.put("SGST", 0.00);
				itemDetails.put("IGST", Math.round(igstAmt * 100.0) / 100.0);
			}

			itemList.put(itemDetails);
		}

		payload.put("ItemList", itemList);

		double totalAssVal = 0.0;
		double totalCgstVal = 0.0;
		double totalSgstVal = 0.0;
		double totalIgstVal = 0.0;
		double totalDiscount = 0.0;
		double totalInvoiceValue = 0.0;

		for (int i = 0; i < itemList.length(); i++) {
			JSONObject item = itemList.getJSONObject(i);
			totalAssVal += item.getDouble("AssAmt");
			totalCgstVal += item.getDouble("CgstAmt");
			totalSgstVal += item.getDouble("SgstAmt");
			totalIgstVal += item.getDouble("IgstAmt");
			totalDiscount += item.getDouble("Discount");
			totalInvoiceValue += item.getDouble("TotItemVal");
		}

		// ✅ Populate ValDtls based on updated values
		final JSONObject valDtls = new JSONObject();
		valDtls.put("AssVal", Math.round(totalAssVal * 100.0) / 100.0);
		valDtls.put("CgstVal", Math.round(totalCgstVal * 100.0) / 100.0);
		valDtls.put("SgstVal", Math.round(totalSgstVal * 100.0) / 100.0);
		valDtls.put("IgstVal", Math.round(totalIgstVal * 100.0) / 100.0);
		valDtls.put("Discount", Math.round(totalDiscount * 100.0) / 100.0);
		valDtls.put("TotInvVal", Math.round(totalInvoiceValue * 100.0) / 100.0);

		payload.put("ValDtls", valDtls);

		System.out.println("Request Payload: " + payload.toString(2));

		final StringEntity entity = new StringEntity(payload.toString());
		request.setEntity(entity);
		final HttpResponse response = httpClient.execute(request);
		final String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");

		System.out.println(" Response from API: " + responseString);

		final JSONObject responseJson = new JSONObject(responseString);

		if ("0".equals(responseJson.getString("status_cd"))) {
			final String errorMessageirn = "IRN generation failed: " + responseJson.getString("status_desc");
			System.out.println("Error: " + errorMessageirn);

			// Alert message for UI
			final JSONObject alertResponse = new JSONObject();
			alertResponse.put("status", "error");
			alertResponse.put("message", errorMessageirn);

			// Get the next sequence ID for invoiceirn table
			final int seqnoinvoiceirn = jdbcTemplate
					.queryForObject("SELECT COALESCE(MAX(ninvoiceirnseqcode), 0) + 1 FROM invoiceirn", Integer.class);

			// Determine IRN status
			final String sirnStatus = "Failed"; // Default status for failed IRN generation
			if (buyerDtls.optString("Addr1", "").trim().isEmpty()
					|| buyerDtls.optString("Gstin", "").trim().isEmpty()) {
				sirnStatusMessage = "Customer does not have an address or GST! Cannot generate IRN";
				// isError = true;
			}

			// Validate GSTIN format
			final String gstin = obj.get(0).getCustomerGST();
			if (!gstin.matches("([0-9]{2}[A-Z0-9]{13})|URP")) {
				sirnStatusMessage = "Invalid GSTIN format! Cannot generate IRN";
				// isError = true;
			}
			final JSONArray errorsArray = new JSONArray(responseJson.getString("status_desc"));
			String errorMessage = errorsArray.getJSONObject(0).getString("ErrorMessage");

			errorMessage = errorMessage.replace("'", "''");
			String sql = "INSERT INTO invoiceirn (ninvoiceirnseqcode, sinvoiceno, sirnno, nackno, dackdate, sirnstatus, "
					+ "dmodifieddate, nusercode, nsitecode, nstatus, sirnstatusmessage) VALUES (" + seqnoinvoiceirn
					+ ", '" + obj.get(0).getSinvoiceno() + "', " + "NULL, NULL, NULL, '" + sirnStatus + "', '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + ","
					+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ", '" + errorMessage + "');";

			jdbcTemplate.execute(sql);

		} else {

			final JSONObject responsejson = new JSONObject(responseString);

			String sirnStatus = "";
			final String statusDesc = responsejson.getString("status_desc");
			if ("GSTR request succeeds".equals(statusDesc)) {
				sirnStatus = "Generated";
			}
			final JSONObject data = responseJson.getJSONObject("data");
			final String irn = data.getString("Irn");
			System.out.println("Successfully Generated IRN: " + irn);

			final int seqnoinvoiceirn = jdbcTemplate
					.queryForObject("SELECT COALESCE(MAX(ninvoiceirnseqcode), 0) + 1 FROM invoiceirn", Integer.class);

			final String sql = "INSERT INTO invoiceirn (ninvoiceirnseqcode, sinvoiceno, sirnno, nackno, dackdate, sirnstatus, "
					+ "dmodifieddate, nusercode, nsitecode, nstatus, sirnstatusmessage) VALUES (" + seqnoinvoiceirn
					+ ", '" + obj.get(0).getSinvoiceno() + "', "
					+ (data.isNull("Irn") ? "NULL" : "'" + data.getString("Irn") + "'") + ", "
					+ (data.isNull("AckNo") ? "NULL" : data.getLong("AckNo")) + ", "
					+ (data.isNull("AckDt") ? "NULL" : "'" + data.getString("AckDt") + "'") + ", '" + sirnStatus
					+ "', '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNusercode() + ","
					+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ", '" + sirnStatusMessage + "');";

			jdbcTemplate.execute(sql);

		}

		return ResponseEntity.ok(sirnStatusMessage);

	}

	@SuppressWarnings("unused")
	private static double round(final double value) {
		return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	public static String formatInvoiceDate(final String date, final UserInfo userInfo) throws Exception {
		final SimpleDateFormat inputFormat = new SimpleDateFormat(userInfo.getSsitereportdate()); // Original format
		final SimpleDateFormat outputFormat = new SimpleDateFormat(userInfo.getSsitereportdate()); // Target format
		final Date parsedDate = inputFormat.parse(date);
		return outputFormat.format(parsedDate);
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
	@SuppressWarnings("unused")
	private ResponseEntity<Object> getInvoiceHeaders(final UserInfo userInfo) throws Exception {
		final Map<String, Object> objMap = new HashMap<String, Object>();

		final int nuser = userInfo.getNuserrole();
		final List<InvoiceHeader> invoiceList = new ArrayList<InvoiceHeader>();

		final int needapprovalflow;
		if (userInfo.getNuserrole() != -1) {
			final String approvalFlow = "select nneedapprovalflow from userroleconfig where nuserrolecode ="
					+ userInfo.getNuserrole() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			needapprovalflow = jdbcTemplate.queryForObject(approvalFlow, Integer.class);
			objMap.put("needapprovalflow", needapprovalflow);
		}

		final String strQuery = "select cms.scolorhexcode as scolorcode,cm.scolorhexcode,ts.stransstatus as stransdisplaystatus,its.ntransactionstatus,ih.ninvoiceseqcode,ih.sinvoiceno,ih.squotationid,ih.ncustomercode,ih.sprojectcode,ih.sprocessno,ih.sschemename,ih.spackdoctrefno,"
				+ " ih.spackagerefdetails,ih.ntotalamount,ih.ntotaltaxamount,ih.ntotalfrightcharges,ins.nschemecode,ih.npaymentcode,ih.spaymentdetails,ih.nbankcode,ih.ncurrencytype,ih.nstatus,ic.ssymbol,"
				+ " ih.jsondata||json_build_object('ninvoiceseqcode',ninvoiceseqcode)::jsonb "
				+ " as jsondata,ih.jsondata->>'CustomerName' as CustomerName," + " COALESCE(TO_CHAR(dinvoicedate,'"
				+ userInfo.getSsitedate() + "'),'') as sinvoicedate," + " COALESCE(TO_CHAR(dquotationdate,'"
				+ userInfo.getSsitedate() + "'),'') as squotationdate," + " COALESCE(TO_CHAR(dprocessdate,'"
				+ userInfo.getSsitedate() + "'),'') as sprocessdate," + " COALESCE(TO_CHAR(dorderreferencedate,'"
				+ userInfo.getSsitedate() + "'),'') as sorderreferencedate," + " COALESCE(TO_CHAR(dpackdocrefdate,'"
				+ userInfo.getSsitedate() + "'),'') as spackdocrefdate"
				+ " from invoiceheader ih,colormaster cms,invoicecurrencytype ic,"
				+ "invoicetransactionstatus its ,transactionstatus ts,colormaster cm,formwisestatuscolor fsc,invoiceschemes ins where its.sreferenceid=ih.sinvoiceno and ts.ntranscode=its.ntransactionstatus "
				+ " and cm.ncolorcode=fsc.ncolorcode and ts.ntranscode=fsc.ntranscode and  ih.ncurrencytype=ic.ncurrencycode and ih.sschemename=ins.sschemename and  fsc.nformcode="
				+ userInfo.getNformcode() + "and cms.ncolorcode= "
				+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()
				+ "and ninvoiceseqcode>0 and ih.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ih.nsitecode=" + userInfo.getNmastersitecode() + " and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fsc.nsitecode="
				+ userInfo.getNmastersitecode() + " and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nsitecode="
				+ userInfo.getNmastersitecode() + " and its.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ins.ntransactionstatus IN ("
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ") order by ninvoiceseqcode ";

		final List<InvoiceHeader> headerList = jdbcTemplate.query(strQuery, new InvoiceHeader());

		objMap.put("Invoice", headerList);
		if (!headerList.isEmpty()) {
			invoiceList.add(headerList.get(headerList.size() - 1));
			objMap.put("selectedInvoice", invoiceList);
			objMap.put("searchedInvoice", invoiceList);
			final List<InvoiceProductItemDetails> productList = getProductsforInvoiceInsert(
					headerList.get(headerList.size() - 1).getSinvoiceno(), userInfo);
			objMap.put("selectedProduct", productList);
			final List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(
					headerList.get(headerList.size() - 1).getSinvoiceno(), userInfo);
			objMap.put("selectedTax", taxList);
		} else {
			objMap.put("selectedInvoice", headerList);
			objMap.put("searchedInvoice", headerList);
		}

		final int npackagerefdetails;
		final int ntotalfrightchanges;
		final int norderrefno;
		final int packdocref;
		final int packdocrefdate;
		final int nproducttestcode;
		final int invoicepatientfield;
		final String st = "select * from userrolefieldcontrol where nformcode=" + userInfo.getNformcode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nneedrights="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus();
		final List<UserRoleFieldControl> listTest = (List<UserRoleFieldControl>) jdbcTemplate.query(st,
				new UserRoleFieldControl());
//		final String sntestgrouptestcode = projectDAOSupport.fndynamiclisttostring(listTest, "getNfieldcode");
		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest, "getNfieldcode");
		final Map<String, Integer> Invoicedetails = new HashMap<>();
		if (listTest.size() == Enumeration.TransactionStatus.ALL.gettransactionstatus()) {

			final int defaultValue = +Enumeration.TransactionStatus.NO.gettransactionstatus();
			Invoicedetails.put("npackagerefdetails", defaultValue);
			Invoicedetails.put("ntotalfrightchanges", defaultValue);
			Invoicedetails.put("norderrefno", defaultValue);
			Invoicedetails.put("packdocref", defaultValue);
			Invoicedetails.put("packdocrefdate", defaultValue);
			Invoicedetails.put("nproducttestcode", defaultValue);
			Invoicedetails.put("invoicepatientfield", defaultValue);

			objMap.put("Totalfrightchanges", Invoicedetails.get("ntotalfrightchanges"));
			objMap.put("PackageDetails", Invoicedetails.get("npackagerefdetails"));
			objMap.put("Orderrefno", Invoicedetails.get("norderrefno"));
			objMap.put("Packdoref", Invoicedetails.get("packdocref"));
			objMap.put("Packrefdate", Invoicedetails.get("packdocrefdate"));
			objMap.put("producttestcode", Invoicedetails.get("nproducttestcode"));
			objMap.put("PatientDetails", Invoicedetails.get("invoicepatientfield"));
		} else {
			final String std = "select * from fieldmaster where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "nfieldcode in ("
					+ sntestgrouptestcode + ")";

			final List<FieldMaster> fieldlistTest = (List<FieldMaster>) jdbcTemplate.query(std, new FieldMaster());

			final Set<String> fieldNames = fieldlistTest.stream().map(FieldMaster::getSfieldname)
					.collect(Collectors.toSet());
			final int defaultValue = +Enumeration.TransactionStatus.YES.gettransactionstatus();
			final int value = +Enumeration.TransactionStatus.NO.gettransactionstatus();
			norderrefno = fieldNames.contains("sorderreferenceno") ? defaultValue : value;
			packdocref = fieldNames.contains("spackdoctrefno") ? defaultValue : value;
			packdocrefdate = fieldNames.contains("dpackdocrefdate") ? defaultValue : value;
			npackagerefdetails = fieldNames.contains("spackagerefdetails") ? defaultValue : value;
			ntotalfrightchanges = fieldNames.contains("ntotalfrightcharges") ? defaultValue : value;
			nproducttestcode = fieldNames.contains("nproducttestcode") ? defaultValue : value;
			invoicepatientfield = fieldNames.contains("invoicepatientfield") ? defaultValue : value;

			objMap.put("Totalfrightchanges", ntotalfrightchanges);
			objMap.put("PackageDetails", npackagerefdetails);
			objMap.put("Orderrefno", norderrefno);
			objMap.put("Packdoref", packdocref);
			objMap.put("Packrefdate", packdocrefdate);
			objMap.put("producttestcode", nproducttestcode);

			objMap.put("PatientDetails", invoicepatientfield);

		}

		objMap.put("Invoice", headerList);
		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to retire Invoice Record with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> retireInvoiceRecord(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final List<String> multilingualIDList = new ArrayList<>();

		final List<InvoiceHeader> retirelist = (List<InvoiceHeader>) objmapper
				.convertValue(inputMap.get("selectedInvoice"), new TypeReference<List<InvoiceHeader>>() {
				});
		final List<InvoiceHeader> searchlist = (List<InvoiceHeader>) objmapper
				.convertValue(inputMap.get("searchedInvoice"), new TypeReference<List<InvoiceHeader>>() {
				});
		for (InvoiceHeader obj : retirelist) {
			final String querystatus = "update invoicetransactionstatus set ntransactionstatus="
					+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ",dapproveddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',sapproveduser='" + userInfo.getSusername()
					+ "' where sreferenceid='" + obj.getSinvoiceno() + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			jdbcTemplate.execute(querystatus);
			multilingualIDList.add("IDS_RETIREINVOICE");
			auditUtilityFunction.fnInsertAuditAction(getapproveauditinvoice(obj.getSinvoiceno(), userInfo), 1, null,
					multilingualIDList, userInfo);
		}
		// If searchedInvoice size is 1, return invoice header
		if (searchlist != null && searchlist.size() == 1) {
			return getInvoiceHeaders(userInfo);
		}
		return getInvoiceHeader(inputMap, userInfo);
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
	@Override
	public ResponseEntity<Object> getProductTestDetails(final UserInfo userInfo) {
		final Map<String, Object> objmap = new HashMap<>();

		final String strQuery = "select nproducttestcode,sproducttestdetail from producttestdetail  where  "
				+ " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmastersitecode="
				+ userInfo.getNmastersitecode() + " order by 1 asc ";
		final List<ProductTestDetail> ProductTest = (List<ProductTestDetail>) jdbcTemplate.query(strQuery,
				new ProductTestDetail());
		objmap.put("ProductTest", ProductTest);

		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get the overall InvoiceHeader for
	 * approve audit invoice with respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public List<InvoiceHeader> getapproveauditinvoice(final Object object, final UserInfo userInfo) {
		/// String strQuery = "";

		final String strQuer = "select its.ntransactionstatus,ts.stransstatus as stransdisplaystatus,ih.sinvoiceno,ih.ntotalamount,ih.ntotaltaxamount,ih.ntotalfrightcharges,ih.jsondata->>'CustomerName' as CustomerName,ih.ncurrencytype,iph.nproducttestcode from  invoiceheader ih, "
				+ "invoicetransactionstatus its,producttestdetail iph,transactionstatus ts where sreferenceid='"
				+ object
				+ "' and  its.sreferenceid=ih.sinvoiceno  and  ts.ntranscode=its.ntransactionstatus and iph.nproducttestcode=ih.nproducttestcode and its.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ih.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ih.nsitecode="
				+ userInfo.getNmastersitecode() + " and iph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iph.nmastersitecode="
				+ userInfo.getNmastersitecode() + "" + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<InvoiceHeader> approve = (List<InvoiceHeader>) jdbcTemplate.query(strQuer, new InvoiceHeader());
		return approve;
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
	public List<InvoiceHeader> getirninvoice(final Object object, final UserInfo userInfo) {
		final String strQuer = " SELECT its.ntransactionstatus, ts.stransstatus AS stransdisplaystatus, "
				+ " ih.sinvoiceno, ih.ntotalamount, ih.ntotaltaxamount, ih.ntotalfrightcharges, "
				+ " ih.jsondata->>'CustomerName' AS CustomerName, ih.jsondata->>'CustomerGST' AS CustomerGST, "
				+ " COALESCE(ipd.jsondata->>'staxname', '') AS staxname, "
				+ " COALESCE(ih.jsondata->>'Address', '') AS Address, "
				+ " COALESCE(ipd.jsondata->>'sproductname', '') AS sproductname, "
				+ " COALESCE(ipd.jsondata->>'nquantity', '0') AS nquantity, "
				+ " COALESCE(ipd.jsondata->>'nunit', '0') AS nunit, "
				+ " COALESCE(ipd.jsondata->>'ncost', '0.0') AS ncost, "
				+ " COALESCE(ipd.jsondata->>'noverallcostvalue', '0.0') AS noverallcostvalue, "
				+ " COALESCE(ipd.jsondata->>'ndiscountpercentage', '0.0') AS ndiscountpercentage, "
				+ " COALESCE(ipd.jsondata->>'ntotalcost', '0.0') AS ntotalcost, "
				+ " ih.ncurrencytype, iph.nproducttestcode, ih.dinvoicedate " + " FROM invoiceheader ih "
				+ " JOIN invoicetransactionstatus its ON its.sreferenceid = ih.sinvoiceno "
				+ " JOIN invoiceproductitemdetails ipd ON ih.sinvoiceno = ipd.sinvoiceseqno "
				+ " JOIN transactionstatus ts ON ts.ntranscode = its.ntransactionstatus "
				+ " JOIN producttestdetail iph ON iph.nproducttestcode = ih.nproducttestcode "
				+ " WHERE  sreferenceid='" + object + "' and ih.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ih.nsitecode="
				+ userInfo.getNmastersitecode() + "" + " and its.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ipd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ipd.nsitecode="
				+ userInfo.getNmastersitecode() + "" + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and iph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iph.nmastersitecode="
				+ userInfo.getNmastersitecode();

		final List<InvoiceHeader> approve = (List<InvoiceHeader>) jdbcTemplate.query(strQuer, new InvoiceHeader());
		return approve;
	}

	/**
	 * This interface declaration is used to get Search PatientField with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getSearchPatientFieldData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select concat(sfirstname,' ',slastname) as spatientname, COALESCE(TO_CHAR(ddob,'"
				+ userInfo.getSsitedate()
				+ "'),'') as sdob,sage,sfathername,smobileno,semail,npatientno as npatientid from invoicepatientmaster "
				+ "where npatientno=" + inputMap.get("npatientno") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();

		final List<InvoicePatient> patientList = (List<InvoicePatient>) jdbcTemplate.query(strQuery,
				new InvoicePatient());

		return new ResponseEntity<>(patientList, HttpStatus.OK);

	}

	/**
	 * This interface declaration is used to get the Active currency with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public String getActivecurrencyvalue(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		final Object value = inputMap.get("ntotalamount");
		final Object CheckWordOrNum = 1;
		final Integer invCheckWordOrNum = (Integer) CheckWordOrNum;
		String CurrencyValue = "";
		if (value instanceof Double || value instanceof Integer && invCheckWordOrNum == 1) {
			System.out.println("Numeric value is a Integer");
			final double intValue = ((Number) value).doubleValue();
			System.out.println("Converted value as Integer: " + intValue);
			CurrencyValue = ProjectDAOSupport.convertToWords(intValue, userInfo);
		} else {
			System.out.println("Numeric value is a Float");
			final int amountValue = Math.round((float) Float.parseFloat((String) value));
			final double CheckWordOrNumber = amountValue;
			System.out.println("Converted value as Integer: " + CheckWordOrNumber);
			CurrencyValue = ProjectDAOSupport.convertToWords(CheckWordOrNumber, userInfo);
		}
		if (userInfo.getSlanguagetypecode().equals("en-US")) {
			CurrencyValue = CurrencyValue + " Rupess Only ";
			System.out.println("Doller " + CurrencyValue);
		} else if (userInfo.getSlanguagetypecode().equals("ru-RU")) {
			CurrencyValue = CurrencyValue + " рубль Только ";
			System.out.println("Rupess  " + CurrencyValue);
		} else if (userInfo.getSlanguagetypecode().equals("tg-TG")) {
			CurrencyValue = CurrencyValue + " сомонӣ танҳо ";
			System.out.println("Europen " + CurrencyValue);
		}
		return CurrencyValue;
	}

	/**
	 * This interface declaration is used to get AmountInWords in InvoiceHeader with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public String getAmountInWords(final Map<String, Object> inputMap, final UserInfo userInfo, final String amountKey)
			throws Exception {
		final JSONObject jsonObject;

		if (inputMap.containsKey("invoiceJson")) {
			jsonObject = new JSONObject(inputMap.get("invoiceJson").toString());
		} else if (inputMap.containsKey("productJson")) {
			jsonObject = new JSONObject(inputMap.get("productJson").toString());
		} else {
			jsonObject = new JSONObject();
		}
		if (inputMap.containsKey("Arno") && inputMap.get("Arno") != null) {
			final Object arnoData = inputMap.get("Arno");

			if (arnoData instanceof List) {
				final JSONArray arnoArray = new JSONArray((List<?>) arnoData);
				jsonObject.put("Arno", arnoArray);
			} else if (arnoData instanceof String) {
				// If it's a JSON string, parse directly
				final JSONArray arnoArray = new JSONArray(arnoData.toString());
				jsonObject.put("Arno", arnoArray);
			} else {
				// Fallback to safe handling
				jsonObject.put("Arno", new JSONArray());
			}
		}

		// Extract TotalTaxAmount from the invoiceJson
		final String totalTaxAmount;
		if (jsonObject.has("TotalTaxAmount")) {
			totalTaxAmount = jsonObject.getString("TotalTaxAmount");
		} else {
			totalTaxAmount = jsonObject.optString("ntaxamount", "0");
		}
		final Object value = totalTaxAmount;
		final Object CheckWordOrNum = 1;
		final Integer invCheckWordOrNum = (Integer) CheckWordOrNum;
		String CurrencyValue = "";
		if (!totalTaxAmount.isEmpty()) {
			if (value instanceof Double || value instanceof Integer && invCheckWordOrNum == 1) {
				System.out.println("Numeric value is a Integer");
				final double intValue = ((Number) value).doubleValue();
				System.out.println("Converted value as Integer: " + intValue);
				CurrencyValue = ProjectDAOSupport.convertToWords(intValue, userInfo);
			} else {
				System.out.println("Numeric value is a Float");
				final int amountValue = Math.round((float) Float.parseFloat((String) value));
				final double invCheckWordOrNumber = amountValue;
				System.out.println("Converted value as Integer: " + invCheckWordOrNumber);
				CurrencyValue = ProjectDAOSupport.convertToWords(invCheckWordOrNumber, userInfo);
			}
			if (userInfo.getSlanguagetypecode().equals("en-US")) {
				CurrencyValue = CurrencyValue + " Rupess Only ";
				System.out.println("Doller " + CurrencyValue);
			} else if (userInfo.getSlanguagetypecode().equals("ru-RU")) {
				CurrencyValue = CurrencyValue + " рубль Только ";
				System.out.println("Rupess  " + CurrencyValue);
			} else if (userInfo.getSlanguagetypecode().equals("tg-TG")) {
				CurrencyValue = CurrencyValue + " сомонӣ танҳо ";
				System.out.println("Europen " + CurrencyValue);
			}
		}
		return CurrencyValue;
	}

	/**
	 * This interface declaration is used to get the Patient Details with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getPatientDetails(final UserInfo userInfo) throws Exception {

		final String strQuery = "select concat(sfirstname,' ',slastname) as spatientname, COALESCE(TO_CHAR(ddob,'"
				+ userInfo.getSsitedate()
				+ "'),'') as sdob,sage,sfathername,smobileno,semail,npatientno as npatientid from invoicepatientmaster where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " order by sfirstname";

		final List<InvoicePatient> patientList = (List<InvoicePatient>) jdbcTemplate.query(strQuery,
				new InvoicePatient());

		return new ResponseEntity<>(patientList, HttpStatus.OK);

	}

	/**
	 * This interface declaration is used to get the Schemes with respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceHeader with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getSchemes(final UserInfo userInfo) {
		final String strQuery = "select nschemecode,sschemename from invoiceschemes where ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and ((CAST(dfromdate AS DATE) <= CURRENT_DATE) OR dfromdate IS NULL)  and ((CAST(dtodate AS DATE) >= CURRENT_DATE) OR dtodate IS NULL) "
				+ " and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		return new ResponseEntity<>((List<InvoiceSchemes>) jdbcTemplate.query(strQuery, new InvoiceSchemes()),
				HttpStatus.OK);
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
	@Override
	public ResponseEntity<Object> updateUsercodeForReport(final Map<String, Object> inputMap, final UserInfo userInfo) {
		final String strupdate = "update invoiceheader set nusercode=" + userInfo.getNusercode()
				+ " where ninvoiceseqcode=" + inputMap.get("ninvoiceseqcode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		jdbcTemplate.execute(strupdate);
		return new ResponseEntity<>(HttpStatus.OK);
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
	@Override
	public ResponseEntity<Object> costUpdateCost(final Map<String, Object> inputMap) {
		final String strupdate = "update invoiceproductmaster set ncost=" + inputMap.get("ncost")
				+ " where nproductcode=" + inputMap.get("nproductcode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		jdbcTemplate.execute(strupdate);
		return new ResponseEntity<>(HttpStatus.OK);
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
	@SuppressWarnings("unused")
	public ResponseEntity<Object> getFilteredRecord(final UserInfo userInfo, String fromDate, String toDate,
			String breadCrumbFrom, String breadCrumbTo) throws Exception {
		final Map<String, Object> responseMap = new LinkedHashMap<>();
		final List<InvoiceHeader> invoiceList = new ArrayList<InvoiceHeader>();

		List<InvoiceHeader> headerList = new ArrayList<>();

		// Check if filtering is requested
		final boolean isFiltered = (fromDate != null && !fromDate.isEmpty()) && (toDate != null && !toDate.isEmpty());

    	final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		String formattedFromDate = "";
		String formattedToDate = "";

		String FromDate = "";
		String ToDate = "";

		if (isFiltered) {
			fromDate = fromDate.replace("T", " ").replace("Z", "");
			toDate = toDate.replace("T", " ").replace("Z", "");

			final LocalDateTime Fromdate = LocalDateTime.parse(fromDate, formatter);
			final LocalDateTime Todate = LocalDateTime.parse(toDate, formatter);
	
			DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			formattedFromDate = Fromdate.format(myFormatObj);
			formattedToDate = Todate.format(myFormatObj);

			DateTimeFormatter myObjdate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			FromDate = Fromdate.format(myObjdate);
			ToDate = Todate.format(myObjdate);

		}

		final String strQuery = "SELECT cms.scolorhexcode AS scolorcode, cm.scolorhexcode, ts.stransstatus AS stransdisplaystatus, its.ntransactionstatus, "
				+ " ih.ninvoiceseqcode, ih.sinvoiceno, ih.squotationid, ih.ncustomercode, ih.sprojectcode, ih.sprocessno, ih.sschemename, ih.spackdoctrefno, "
				+ " ih.spackagerefdetails, ih.ntotalamount, ih.ntotaltaxamount, ih.ntotalfrightcharges, ins.nschemecode, ih.npaymentcode, ih.spaymentdetails, "
				+ " ih.nbankcode, ih.ncurrencytype, ih.nstatus, ic.ssymbol, "
				+ " ih.jsondata || json_build_object('ninvoiceseqcode', ninvoiceseqcode)::jsonb AS jsondata, "
				+ " ih.jsondata->>'CustomerName' AS CustomerName, " + "COALESCE(TO_CHAR(dinvoicedate, '"
				+ userInfo.getSsitedate() + "'), '') AS sinvoicedate, " + "COALESCE(TO_CHAR(dquotationdate, '"
				+ userInfo.getSsitedate() + "'), '') AS squotationdate, " + "COALESCE(TO_CHAR(dprocessdate, '"
				+ userInfo.getSsitedate() + "'), '') AS sprocessdate, " + "COALESCE(TO_CHAR(dorderreferencedate, '"
				+ userInfo.getSsitedate() + "'), '') AS sorderreferencedate, " + "COALESCE(TO_CHAR(dpackdocrefdate, '"
				+ userInfo.getSsitedate() + "'), '') AS spackdocrefdate "
				+ " FROM invoiceheader ih, colormaster cms, invoicecurrencytype ic, invoicetransactionstatus its, transactionstatus ts, "
				+ " colormaster cm, formwisestatuscolor fsc, invoiceschemes ins "
				+ " WHERE its.sreferenceid = ih.sinvoiceno " + "AND ts.ntranscode = its.ntransactionstatus "
				+ " AND cm.ncolorcode = fsc.ncolorcode " + "AND ts.ntranscode = fsc.ntranscode "
				+ " AND ih.ncurrencytype = ic.ncurrencycode " + "AND ih.sschemename = ins.sschemename "
				+ " AND fsc.nformcode = " + userInfo.getNformcode() + " " + "AND cms.ncolorcode = "
				+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + " " + "AND ih.ninvoiceseqcode > 0 "
				+ " AND ih.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ih.nsitecode=" + userInfo.getNmastersitecode() + "" + " and cms.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND ic.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nsitecode="
				+ userInfo.getNmastersitecode() + "" + " and its.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and ts.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " AND cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND fsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fsc.nsitecode="
				+ userInfo.getNmastersitecode() + "" + " and ins.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ins.nsitecode="
				+ userInfo.getNmastersitecode() + "" + " AND ins.ntransactionstatus IN ("
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ") "
				+ " and ih.dinvoicedate between '"+formattedFromDate+"' and '"+formattedToDate+"'"
				+ " ORDER BY ih.ninvoiceseqcode desc";

		headerList = jdbcTemplate.query(strQuery, new InvoiceHeader());

		responseMap.put("Invoice", headerList);
		responseMap.put("Filtered", isFiltered);
		responseMap.put("fromDate", FromDate);
		responseMap.put("toDate", ToDate);


		if (!headerList.isEmpty()) {
			//final InvoiceHeader selected = headerList.get(headerList.size() - 1);

			invoiceList.add(headerList.get(0));
			responseMap.put("selectedInvoice", invoiceList);
			final List<InvoiceProductItemDetails> productList = getProductsforInvoiceInsert(
					headerList.get(0).getSinvoiceno(), userInfo);
			responseMap.put("selectedProduct", productList);
			final List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(
					headerList.get(headerList.size() - 1).getSinvoiceno(), userInfo);
			responseMap.put("selectedTax", taxList);
		}

		return new ResponseEntity<>(responseMap, HttpStatus.OK);
	}
	
	//Added by sonia on 16th oct 2025 for jira id:SWSM-104
		/**
		 * This method is used to Sent the Report By Mail.	 
		 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
		 *                 user details and nmastersitecode [int] primary key of site
		 *                 object for which the list is to be fetched Input :
		 *                 {"userinfo":{nmastersitecode": 1},
		 *                 "ncontrolcode":1545, "ninvoiceseqcode":1,
		 *                 "ntransactionstatus":31 }
		 * @return response entity object holding response status as success
		 * @throws Exception exception
		 */
	public ResponseEntity<Object> sendReportByMail(final Map<String, Object> inputMap, final UserInfo userInfo)throws Exception {
		final Map<String, Object> responseMap = new HashMap<>();
		final Map<String, Object> mailMap = new HashMap<>();
		
		mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));
		mailMap.put("nsitecode", (int) userInfo.getNmastersitecode());
		mailMap.put("ninvoiceseqcode",inputMap.get("ninvoiceseqcode"));
		mailMap.put("ssystemid",inputMap.get("sinvoiceno"));

		final String getReportQuery= "select rm.*,rd.sreportformatdetail  "
								   + "from reportmaster rm  "
								   + "join reportdetails rd on rd.nreportcode=rm.nreportcode  "
								   + "join invoicereporthistory irh on irh.nreportdetailcode=rd.nreportdetailcode "
								   + "and irh.nreporttypecode=rm.nreporttypecode "
								   + "where irh.ninvoicereporthistorycode =any(select max(ninvoicereporthistorycode) "
								   + "from invoicereporthistory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								   + "and nsitecode ="+userInfo.getNmastersitecode()+" group by ninvoiceseqcode) "
								   + "and rm.ncontrolcode="+inputMap.get("reportControlCode")+" "
								   + "and irh.ninvoiceseqcode="+inputMap.get("ninvoiceseqcode")+" "
								   + "and rm.ntransactionstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								   + "and rd.ntransactionstatus="+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" "
								   + "and rm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								   + "and rd.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								   + "and irh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								   + "and rd.nsitecode="+userInfo.getNmastersitecode()+" and rm.nsitecode="+userInfo.getNmastersitecode()+" "
								   + "and irh.nsitecode="+userInfo.getNmastersitecode()+" ";
		
		final ReportMaster reportMaster = (ReportMaster) jdbcUtilityFunction.queryForObject(getReportQuery,ReportMaster.class, jdbcTemplate);
		if (reportMaster == null) {
			responseMap.put("rtn", "IDS_SELECTGENERATEDREPORT");
		}else {
			final String getUsersForEmail = " select squery from emailuserquery where nformcode ="+Enumeration.QualisForms.INVOICE.getqualisforms()+""
					  					  + " and sdisplayname='Invoice' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
			final EmailUserQuery emailUserQueryObj = (EmailUserQuery) jdbcUtilityFunction.queryForObject(getUsersForEmail, EmailUserQuery.class, jdbcTemplate);
			final String ReplacedFinalQuery = projectDAOSupport.fnReplaceParameter(emailUserQueryObj.getSquery(),mailMap);
			final List<Map<String, Object>> listOfMap = jdbcTemplate.queryForList(ReplacedFinalQuery);		
			if(listOfMap.size()==0) {
				responseMap.put("rtn", "IDS_MAILIDNOTAVAILABLE");
			} else {
				responseMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());				
				emailDAOSupport.createEmailAlertTransaction(mailMap, userInfo);			
			}			
		}	
		return new ResponseEntity<>(responseMap, HttpStatus.OK);
	}
	

}