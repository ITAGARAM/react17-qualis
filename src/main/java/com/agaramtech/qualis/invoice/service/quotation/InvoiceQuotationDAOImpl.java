package com.agaramtech.qualis.invoice.service.quotation;

import java.util.Date;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.credential.model.UserFile;
import com.agaramtech.qualis.digitalsignature.model.DigitalSignature;
import com.agaramtech.qualis.global.*;
import com.agaramtech.qualis.invoice.model.FieldMaster;
import com.agaramtech.qualis.invoice.model.InvoiceCurrencyType;
import com.agaramtech.qualis.invoice.model.InvoiceCustomerMaster;
import com.agaramtech.qualis.invoice.model.InvoiceExeCustomerProducts;
import com.agaramtech.qualis.invoice.model.InvoiceHeader;
import com.agaramtech.qualis.invoice.model.InvoicePaymentMode;
import com.agaramtech.qualis.invoice.model.InvoiceProductMaster;
import com.agaramtech.qualis.invoice.model.InvoiceQuotationHeader;
import com.agaramtech.qualis.invoice.model.InvoiceQuotationProduct;
import com.agaramtech.qualis.invoice.model.InvoiceSchemes;
import com.agaramtech.qualis.invoice.model.InvoiceSchemesProduct;
import com.agaramtech.qualis.invoice.model.InvoiceTaxCalculation;
import com.agaramtech.qualis.invoice.model.InvoiceTestDetails;
import com.agaramtech.qualis.invoice.model.InvoiceTransactionStatus;
import com.agaramtech.qualis.invoice.model.ProductTest;
import com.agaramtech.qualis.invoice.model.ProductTestDetail;
import com.agaramtech.qualis.invoice.model.TaxProductDetails;
import com.agaramtech.qualis.invoice.model.UserRoleFieldControl;
import com.agaramtech.qualis.project.model.ProjectMaster;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "InvoiceQuotation" table by
 * implementing methods from its interface.
 */

@AllArgsConstructor
@Repository
public class InvoiceQuotationDAOImpl implements InvoiceQuotationDAO {

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ReportDAOSupport reportDAOSupport;

	/**
	 * This interface declaration is used to get the overall InvoiceQuotation with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getQuotation(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception {

		final Map<String, Object> objmap = new HashMap<>();
		final List<InvoiceQuotationHeader> QuotationList = new ArrayList<InvoiceQuotationHeader>();

		String fromDate = "";
		String toDate = "";
		
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String currentUIDate = (String) inputMap.get("currentdate");

		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					currentUIDate, "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");
			objmap.put("fromDate", mapObject.get("FromDateWOUTC"));
			objmap.put("toDate", mapObject.get("ToDateWOUTC"));
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);
			objmap.put("fromDate", fromDateUI);
			objmap.put("toDate", toDateUI);
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
		
		if (userInfo.getNuserrole() != -1) {
			final String approvalFlow = "select nneedapprovalflow from userroleconfig where nuserrolecode ="
					+ userInfo.getNuserrole() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
					+ userInfo.getNmastersitecode();
			needapprovalflow = jdbcTemplate.queryForObject(approvalFlow, Integer.class);
			objmap.put("needapprovalflow", needapprovalflow);
		}

		final String strquery = "select distinct cms.scolorhexcode as scolorcode,cm.scolorhexcode,ts.stransstatus as stransdisplaystatus,iqh.nquotationseqcode,its.ntransactionstatus,"
				+ "iqh.ncustomercode,iqh.squotationno,iqh.stenderrefno,iqh.sschemename,ins.nschemecode,iqh.ntotalamount,iqh.ntotaltaxamount,ic.ssymbol,"
				+ "iqh.nstatus,iqh.jsondata||json_build_object('nquotationseqcode',nquotationseqcode)::jsonb  as jsondata"
				+ ",iqh.jsondata->>'CustomerName' as CustomerName, COALESCE(TO_CHAR(iqh.dquotationdate,'"
				+ userInfo.getSsitereportdate() + "'),'') as squotationdate,"
				+ "COALESCE(TO_CHAR(iqh.dquotationfromdate,'" + userInfo.getSsitereportdate()
				+ "'),'')as squotationfromdate," + "COALESCE(TO_CHAR(iqh.dquotationtodate,'"
				+ userInfo.getSsitereportdate() + "'),'') as squotationtodate,"
				+ "COALESCE(TO_CHAR(iqh.dtenderrefdate,'" + userInfo.getSsitereportdate() + "'),'') as stenderrefdate,"
				+ "COALESCE(TO_CHAR(iqh.dorderrefdate,'" + userInfo.getSsitereportdate()
				+ "'),'') as sorderrefdate from invoicequotationheader iqh,colormaster cms,invoicecurrencytype ic,"
				+ "invoicetransactionstatus its ,transactionstatus ts,colormaster cm,formwisestatuscolor fsc,invoiceschemes ins where its.sreferenceid=iqh.squotationno"
				+ " and cm.ncolorcode=fsc.ncolorcode and ts.ntranscode=fsc.ntranscode and iqh.ncurrencytype=ic.ncurrencycode "
				+ " and iqh.sschemename=ins.sschemename " + " and fsc.nformcode=" + userInfo.getNformcode()
				+ " and cms.ncolorcode=" + Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()
				+ " and ts.ntranscode=its.ntransactionstatus and nquotationseqcode>0 " + " and its.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iqh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ins.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and iqh.nsitecode= "
				+ userInfo.getNmastersitecode() + " and ic.nsitecode=" + userInfo.getNmastersitecode() + ""
				+ " and fsc.nsitecode=" + userInfo.getNmastersitecode() + " and iqh.dquotationdate between '"+formattedFromDate+"'  and '"+formattedToDate+"'"
				+ " order by nquotationseqcode ";

		final List<InvoiceQuotationHeader> lstQuotation = jdbcTemplate.query(strquery, new InvoiceQuotationHeader());

		final String strQuery = "select count(*) from invoicequotationheader iqh where iqh.squotationno not in "
				+ "(select UNNEST(STRING_TO_ARRAY(squotationid,',')) from invoiceheader where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") and iqh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iqh.nsitecode= "
				+ userInfo.getNmastersitecode();
		final int count = jdbcTemplate.queryForObject(strQuery, Integer.class);
		objmap.put("Quotationcount", count);
		objmap.put("Quotation", lstQuotation);
		if (!lstQuotation.isEmpty()) {
			if("Edit".equals(inputMap.get("actiontype")) || "delete".equals(inputMap.get("actiontype"))
					|| "update".equals(inputMap.get("actiontype"))) {
			
			String quotationno=(String) inputMap.get("squotationseqno");
			List<InvoiceQuotationHeader> selectedQuotation= getInvoiceQuotation(quotationno,formattedFromDate,formattedToDate,userInfo);
			objmap.put("selectedQuotation", selectedQuotation);
			
			List<InvoiceQuotationProduct> productList = getProductsforQuotationInsert(quotationno, userInfo);
			objmap.put("selectedProduct", productList);
			
			List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(quotationno, userInfo);
			objmap.put("selectedTax", taxList);
			}
			else {
				QuotationList.add(lstQuotation.get(lstQuotation.size() - 1));
				objmap.put("selectedQuotation", QuotationList);
				List<InvoiceQuotationProduct> productList = getProductsforQuotationInsert(
						lstQuotation.get(lstQuotation.size() - 1).getSquotationno(), userInfo);
				objmap.put("selectedProduct", productList);
				List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(
						lstQuotation.get(lstQuotation.size() - 1).getSquotationno(), userInfo);
				objmap.put("selectedTax", taxList);
			}
		} else {
			objmap.put("selectedQuotation", lstQuotation);
		}

		int norderrefno = 0;
		int norderrefdate = 0;
		int nprojectcode = 0;
		int ntotalfrightchanges = 0;
		int nremarks2 = 0;
		int nremarks1 = 0;
		int nproducttestcode = 0;
		int ntenderrefno = 0;
		int ntenderrefdate = 0;
		final String st = "select * from userrolefieldcontrol where nformcode=" + userInfo.getNformcode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nneedrights="+Enumeration.TransactionStatus.YES.gettransactionstatus();
		final List<UserRoleFieldControl> listTest = (List<UserRoleFieldControl>) jdbcTemplate.query(st,
				new UserRoleFieldControl());
//		final String sntestgrouptestcode = projectDAOSupport.fndynamiclisttostring(listTest, "getNfieldcode");
		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest, "getNfieldcode");
		if (listTest.size() == 0) {
			final Map<String, Integer> quotationdetails = new HashMap<>();
			final int defaultValue = +Enumeration.TransactionStatus.NO.gettransactionstatus();
			quotationdetails.put("norderrefno", defaultValue);
			quotationdetails.put("norderrefdate", defaultValue);
			quotationdetails.put("nprojectcode", defaultValue);
			quotationdetails.put("ntotalfrightchanges", defaultValue);
			quotationdetails.put("nremarks2", defaultValue);
			quotationdetails.put("nremarks1", defaultValue);
			quotationdetails.put("nproducttestcode", defaultValue);
			quotationdetails.put("ntenderrefno", defaultValue);
			quotationdetails.put("ntenderrefdate", defaultValue);

		} else {
			final String std = "select * from fieldmaster where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "nfieldcode in ("
					+ sntestgrouptestcode + ")";
			final List<FieldMaster> listTest2 = (List<FieldMaster>) jdbcTemplate.query(std, new FieldMaster());
			final Set<String> fieldNames = listTest2.stream().map(FieldMaster::getSfieldname).collect(Collectors.toSet());
			final int defaultValue = +Enumeration.TransactionStatus.YES.gettransactionstatus();
			final int value = +Enumeration.TransactionStatus.NO.gettransactionstatus();
			norderrefno = fieldNames.contains("sorderrefno") ? defaultValue : value;
			norderrefdate = fieldNames.contains("dorderrefdate") ? defaultValue : value;
			nprojectcode = fieldNames.contains("sprojectcode") ? defaultValue : value;
			ntotalfrightchanges = fieldNames.contains("ntotalfrightcharges") ? defaultValue : value;
			nremarks2 = fieldNames.contains("sremarks2") ? defaultValue : value;
			nremarks1 = fieldNames.contains("sremarks1") ? defaultValue : value;
			nproducttestcode = fieldNames.contains("nproducttestcode") ? defaultValue : value;
			ntenderrefno = fieldNames.contains("stenderrefno") ? defaultValue : value;
			ntenderrefdate = fieldNames.contains("dtenderrefdate") ? defaultValue : value;

		}
		objmap.put("Tenderrefno", ntenderrefno);
		objmap.put("Tenderdate", ntenderrefdate);
		objmap.put("Orderrefno", norderrefno);
		objmap.put("Orderrefdate", norderrefdate);
		objmap.put("Projectcode", nprojectcode);
		objmap.put("Totalfrightchanges", ntotalfrightchanges);
		objmap.put("Remarks2", nremarks2);
		objmap.put("Remarks1", nremarks1);
		objmap.put("Quotation", lstQuotation);
		objmap.put("producttestcode", nproducttestcode);

		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	private List<InvoiceQuotationHeader> getInvoiceQuotation(String quotationno, String formattedFromDate,
			String formattedToDate, UserInfo userInfo) {
		// TODO Auto-generated method stub
		final String strquery = "select distinct cms.scolorhexcode as scolorcode,cm.scolorhexcode,ts.stransstatus as stransdisplaystatus,iqh.nquotationseqcode,its.ntransactionstatus,"
				+ "iqh.ncustomercode,iqh.squotationno,iqh.stenderrefno,iqh.sschemename,ins.nschemecode,iqh.ntotalamount,iqh.ntotaltaxamount,ic.ssymbol,"
				+ "iqh.nstatus,iqh.jsondata||json_build_object('nquotationseqcode',nquotationseqcode)::jsonb  as jsondata"
				+ ",iqh.jsondata->>'CustomerName' as CustomerName, COALESCE(TO_CHAR(iqh.dquotationdate,'"
				+ userInfo.getSsitereportdate() + "'),'') as squotationdate,"
				+ "COALESCE(TO_CHAR(iqh.dquotationfromdate,'" + userInfo.getSsitereportdate()
				+ "'),'')as squotationfromdate," + "COALESCE(TO_CHAR(iqh.dquotationtodate,'"
				+ userInfo.getSsitereportdate() + "'),'') as squotationtodate,"
				+ "COALESCE(TO_CHAR(iqh.dtenderrefdate,'" + userInfo.getSsitereportdate() + "'),'') as stenderrefdate,"
				+ "COALESCE(TO_CHAR(iqh.dorderrefdate,'" + userInfo.getSsitereportdate()
				+ "'),'') as sorderrefdate from invoicequotationheader iqh,colormaster cms,invoicecurrencytype ic,"
				+ "invoicetransactionstatus its ,transactionstatus ts,colormaster cm,formwisestatuscolor fsc,invoiceschemes ins where its.sreferenceid=iqh.squotationno"
				+ " and cm.ncolorcode=fsc.ncolorcode and ts.ntranscode=fsc.ntranscode and iqh.ncurrencytype=ic.ncurrencycode "
				+ " and iqh.sschemename=ins.sschemename " + " and fsc.nformcode=" + userInfo.getNformcode()
				+ " and cms.ncolorcode=" + Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()
				+ " and ts.ntranscode=its.ntransactionstatus and nquotationseqcode>0 " + " and its.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iqh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ins.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and iqh.nsitecode= "
				+ userInfo.getNmastersitecode() + " and ic.nsitecode=" + userInfo.getNmastersitecode() + ""
				+ " and fsc.nsitecode=" + userInfo.getNmastersitecode() + " and iqh.dquotationdate between "
				+ "'"+formattedFromDate+"'  and '"+formattedToDate+"' and iqh.squotationno='"+quotationno+"'"
				+ " order by nquotationseqcode ";

		final List<InvoiceQuotationHeader> lstQuotation = jdbcTemplate.query(strquery, new InvoiceQuotationHeader());

		return lstQuotation;
	}

	/**
	 * This interface declaration is used to get ProductsforQuotation Insert with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public List<InvoiceQuotationProduct> getProductsforQuotationInsert(final String squotationseqno, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "SELECT jsondata1::jsonb AS jsondata1, " + " nquotationitemdetailscode, squotationseqno, "
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
				+ "COALESCE(NULLIF(jsondata->>'ntaxvalue', ''), '0') AS ntaxvalue "
				+ "FROM quotationitemdetails where squotationseqno='" + squotationseqno + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode() + " order by nquotationitemdetailscode";
		List<InvoiceQuotationProduct> filteredList = (List<InvoiceQuotationProduct>) jdbcTemplate.query(strQuery,
				new InvoiceQuotationProduct());

		return filteredList;
	}

	/**
	 * This interface declaration is used to get Products for TaxCalculation with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	private List<InvoiceTaxCalculation> getProductsTaxCalculation(final String squotationseqno,final UserInfo userInfo) {

		final String str = "select jsondata->>'staxname' AS staxname,jsondata->>'ntaxpercentage' AS ntaxpercentage,SUM((jsondata->>'ntaxvalue')::numeric) AS ntaxamount"
				+ " from quotationitemdetails where squotationseqno='" + squotationseqno + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by jsondata->>'staxname', jsondata->>'ntaxpercentage' having "
				+ "jsondata->>'staxname' IS NOT NULL " + "and jsondata->>'staxname' <> '' "
				+ "and (jsondata->>'ntaxpercentage')::numeric <> 0.00 "
				+ "and SUM((jsondata->>'ntaxvalue')::numeric) <> 0.00 union all "
				+ "select qid.jsondata->>'sindirecttaxname' AS staxname,CONCAT(qid.jsondata->>'nindirectax'::text, '.00') AS ntaxpercentage,"
				+ "SUM(CAST((qid.jsondata->>'nindirecttaxamount') AS decimal(10,2))::numeric) AS ntaxamount"
				+ " from quotationitemdetails qid where qid.squotationseqno='" + squotationseqno + "' and qid.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qid.nsitecode= "
				+ userInfo.getNmastersitecode()
				+ " and qid.jsondata->>'sindirecttaxname' IS NOT NULL group by qid.jsondata->>'sindirecttaxname',qid.jsondata->>'nindirectax'";

		final List<InvoiceTaxCalculation> listOutput = (List<InvoiceTaxCalculation>) jdbcTemplate.query(str,
				new InvoiceTaxCalculation());
		if (!listOutput.isEmpty()) {
			for (InvoiceTaxCalculation obj : listOutput) {
				final String strtax = "select coalesce(jsondata->'staxcaltype'->>'" + userInfo.getSlanguagetypecode() + "'"
						+ ", jsondata->'staxcaltype'->>'en-US') as staxtype "
						+ " from invoicetaxcaltype where ncaltypecode=("
						+ "select ncaltypecode from invoicetaxtype where staxname='" + obj.getStaxname()
						+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode= " + userInfo.getNmastersitecode() + ")";

				final List<String> result = jdbcTemplate.query(strtax, (rs, rowNum) -> rs.getString("staxtype"));

				final String taxtype = (result != null && !result.isEmpty()) ? result.get(0) : "Direct";
				obj.setStaxtype(taxtype);
			}
		}

		return listOutput;
	}

	/**
	 * This interface declaration is used to get SeletedQuotation with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getSeletedQuotation(final Map<String, Object> inputMap) throws Exception {

		final Map<String, Object> objMap = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		final String strquery = "select distinct pj.nprojectmastercode,cms.scolorhexcode as scolorcode,cm.scolorhexcode,ts.stransstatus as stransdisplaystatus,iqh.nquotationseqcode,its.ntransactionstatus,ic.ssymbol,"
				+ "iqh.ncustomercode,iqh.sprojectcode,iqh.sprojectname,iqh.squotationno,iqh.stenderrefno,iqh.nproducttestcode,iph.sproducttestdetail,iqh.sschemename,ins.nschemecode,iqh.npaymentmode,iqh.spaymentdetails,iqh.ntotalamount,iqh.ntotaltaxamount,iqh.ncurrencytype,iqh.sremarks1,iqh.sremarks2,"
				+ "iqh.nstatus,iqh.sprojectcode,iqh.jsondata||json_build_object('nquotationseqcode',nquotationseqcode)::jsonb  as jsondata"
				+ ",iqh.jsondata->>'CustomerName' as CustomerName, COALESCE(TO_CHAR(iqh.dquotationdate,'"
				+ userInfo.getSsitereportdate() + "'),'') as squotationdate,"
				+ "COALESCE(TO_CHAR(iqh.dquotationfromdate,'" + userInfo.getSsitereportdate()
				+ "'),'') as squotationfromdate," + "COALESCE(TO_CHAR(iqh.dquotationtodate,'"
				+ userInfo.getSsitereportdate() + "'),'')as squotationtodate," + "COALESCE(TO_CHAR(iqh.dtenderrefdate,'"
				+ userInfo.getSsitereportdate() + "'),'')as stenderrefdate," + "COALESCE(TO_CHAR(iqh.dorderrefdate,'"
				+ userInfo.getSsitereportdate()
				+ "'),'')as sorderrefdate from invoicequotationheader iqh,producttestdetail iph,colormaster cms,invoicecurrencytype ic,"
				+ "invoicetransactionstatus its ,transactionstatus ts,colormaster cm,formwisestatuscolor fsc,invoiceschemes ins ,projectmaster pj where its.sreferenceid=iqh.squotationno"
				+ " and cm.ncolorcode=fsc.ncolorcode and iph.nproducttestcode=iqh.nproducttestcode and iqh.ncurrencytype=ic.ncurrencycode and ts.ntranscode=fsc.ntranscode and iqh.sschemename=ins.sschemename and fsc.nformcode="
				+ userInfo.getNformcode() + " and pj.sprojectname = iqh.sprojectname and cms.ncolorcode="
				+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()
				+ " and ts.ntranscode=its.ntransactionstatus and iqh.nquotationseqcode in (" + inputMap.get("quotationid")
				+ ") and its.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and iph.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and iqh.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ins.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and iqh.nsitecode= "
				+ userInfo.getNmastersitecode() + " and iph.nmastersitecode=" + userInfo.getNmastersitecode()
				+ " and ic.nsitecode=" + userInfo.getNmastersitecode() + " and fsc.nsitecode="
				+ userInfo.getNmastersitecode() + " and ins.nsitecode=" + userInfo.getNmastersitecode()
				+ " order by nquotationseqcode ";

		final List<InvoiceQuotationHeader> lstQuotation = jdbcTemplate.query(strquery, new InvoiceQuotationHeader());
		objMap.put("selectedQuotation", lstQuotation);
		final List<InvoiceQuotationProduct> productList = getProductsforQuotationInsert(
				lstQuotation.get(lstQuotation.size() - 1).getSquotationno(), userInfo);
		objMap.put("selectedProduct", productList);
		final List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(
				lstQuotation.get(lstQuotation.size() - 1).getSquotationno(), userInfo);
		objMap.put("selectedTax", taxList);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get the Payment with respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getPayment(final UserInfo userInfo) throws Exception {

		final String strQuery = "select p.npaymentcode, p.spaymentmode from invoicepaymentmode p  where p.npaymentcode<>"
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode() + "";
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new InvoicePaymentMode()), HttpStatus.OK);

	}

	/**
	 * This interface declaration is used to get the CurrencyType with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of CurrencyType with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	@Override
	public ResponseEntity<Object> getCurrencyType(final UserInfo userInfo) throws Exception {
		final Map<String, Object> objmap = new HashMap<>();
		final String strQuery = "select c.ncurrencycode,c.scurrency,c.ssymbol from  invoicecurrencytype c  where c.ncurrencycode<>"
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() 
				+ " and nactive =" + Enumeration.TransactionStatus.YES.gettransactionstatus() 
				+ " and c.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode() + "";

		final List<InvoiceCurrencyType> currency = (List<InvoiceCurrencyType>) jdbcTemplate.query(strQuery,
				new InvoiceCurrencyType());

		final Date date = new Date();
		date.setMonth(date.getMonth() + 1);
		final SimpleDateFormat sdf = new SimpleDateFormat(userInfo.getSsitereportdatetime());
		objmap.put("dquotationtodate", sdf.format(date));
		objmap.put("Currency", currency);

		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get the SearchFieldData
	 * InvoiceQuotation with respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getSearchFieldData(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		final int ncustomercode = (int) inputMap.get("customerid");
		final String strQuery = "select ic.ncustomercode,ic.scustomername,ic.scustomerreference,ic.scustomertypename,ic.scustomeraddress, "
				+ "ic.semailid,ic.sphone,ic.saccountdetails,ic.scustgst,ic.ndiscountavailable,ic.scustomerreference1,ic.scustomerreference2 from invoicecustomermaster ic where ic.ncustomercode="
				+ ncustomercode + " and ic.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ic.nsitecode= " + userInfo.getNmastersitecode() + "";
		final List<InvoiceCustomerMaster> customerList = (List<InvoiceCustomerMaster>) jdbcTemplate.query(strQuery,
				new InvoiceCustomerMaster());

		return new ResponseEntity<>(customerList, HttpStatus.OK);

	}

	/**
	 * This interface declaration is used to get the overall CustomerQuotation with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of CustomerQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getCustomerQuotation(final Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		final String strQuery = "select ic.ncustomercode,ic.scustomername,ic.scustomerreference,ic.scustomertypename,ic.scustomeraddress, "
				+ "ic.semailid,ic.sphone,ic.saccountdetails,ic.scustgst,ic.scustomerreference1,ic.scustomerreference2 from invoicecustomermaster ic where ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nsitecode= "
				+ userInfo.getNmastersitecode() + " order by ic.scustomername";
		final List<InvoiceCustomerMaster> customerList = (List<InvoiceCustomerMaster>) jdbcTemplate.query(strQuery,
				new InvoiceCustomerMaster());
		return new ResponseEntity<>(customerList, HttpStatus.OK);

	}

	/**
	 * This interface declaration is used to delete entry in InvoiceQuotation table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding detail to be
	 *                            deleted in InvoiceQuotation table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> deleteQuotation(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		final List<String> multilingualIDListProduct = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedUnitList = new ArrayList<>();
		final int nquotationseqcode = (int) inputMap.get("quotationid");

		final List<Map<String, Object>> invoiceQuotationList = (List<Map<String, Object>>) inputMap.get("invoicequotation");

		String quotationno = "";

		if (invoiceQuotationList != null && !invoiceQuotationList.isEmpty()) {
			final Map<String, Object> invoicequotation = invoiceQuotationList.get(0);

			quotationno = (String) invoicequotation.get("squotationno");
		}

		final String str = "select * from invoicetransactionstatus where sreferenceid='" + quotationno + "' ";
		final List<InvoiceTransactionStatus> status = (List<InvoiceTransactionStatus>) jdbcTemplate.query(str,
				new InvoiceTransactionStatus());
		if (status.get(0).getNtransactionstatus() != 7 && status.get(0).getNtransactionstatus() != 31) {
			final List<InvoiceQuotationHeader> quotation = (List<InvoiceQuotationHeader>) getQuotationAlreadyDeletedOrNot(
					nquotationseqcode, "", userInfo);

			if (quotation == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final String updateQueryString = "update invoicequotationheader set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nquotationseqcode="
						+ quotation.get(quotation.size() - 1).getNquotationseqcode();
				jdbcTemplate.execute(updateQueryString);

				final String updateQuery = "update quotationitemdetails set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where squotationseqno='"
						+ quotation.get(quotation.size() - 1).getSquotationno() + "'";
				jdbcTemplate.execute(updateQuery);

				multilingualIDList.add("IDS_DELETEINVOICEQUOTATION");
				multilingualIDListProduct.add("IDS_DELETEINVOICEQUOTATIONPRODUCT");
				auditUtilityFunction.fnInsertAuditAction(
						getQuotationAlreadyDeletedOrNot(nquotationseqcode, "quotation", userInfo), 1, null,
						multilingualIDList, userInfo);
				final List<InvoiceQuotationProduct> value = getProductForAudit(nquotationseqcode, "delete", userInfo);
				for (InvoiceQuotationProduct obj : value) {
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
		return getQuotation(inputMap,userInfo);
	}

	/**
	 * This interface declaration is used to get QuotationAlreadyDeletedOrNot with
	 * respect to site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public List<InvoiceQuotationHeader> getQuotationAlreadyDeletedOrNot(final int quotationId, final String screenName,
			final UserInfo userInfo) {
		final String strQuery;

		if (screenName.isEmpty()) {
			strQuery = "select nquotationseqcode from invoicequotationheader where nquotationseqcode =" + quotationId
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
		} else {
			strQuery = "select nquotationseqcode,jsondata->>'CustomerName' as CustomerName,jsondata->>'CustomerType' as CustomerType,"
					+ "jsondata->>'TotalAmountInWords' as TotalAmountInWords,"
					+ "jsondata->>'CustomerGST' as CustomerGST,jsondata->>'Address' as Address,jsondata->>'EmailId' as EmailId,"
					+ "jsondata->>'PhoneNo' as PhoneNo,squotationno,dquotationfromdate,dquotationtodate,dquotationtodate,"
					+ "ntotalamount,ntotaltaxamount,spaymentdetails,dtenderrefdate,dquotationdate,nquotationseqcode from invoicequotationheader where nquotationseqcode ="
					+ quotationId + " and nstatus=" + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
					+ " and nsitecode= " + userInfo.getNmastersitecode() + "";
		}
		final List<InvoiceQuotationHeader> quotation = (List<InvoiceQuotationHeader>) jdbcTemplate.query(strQuery,
				new InvoiceQuotationHeader());
		return quotation;
	}

	/**
	 * This interface declaration is used to get ProductforQuotation with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getProductforQuotation(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final String strQuery;
		final int ncustomercode = (int) inputMap.get("ncustomercode");

		final int nquotationitemdetailscode = (int) inputMap.get("nquotationitemdetailscode");

		final Map<String, Object> outputMap = new HashMap<>();
		final String st = "select * from invoicecustomermaster where ncustomercode=" + ncustomercode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode();
		final List<InvoiceCustomerMaster> list = (List<InvoiceCustomerMaster>) jdbcTemplate.query(st,
				new InvoiceCustomerMaster());
		final int discount = list.get(0).getNdiscountavailable();
		final int nschemecode = (int) inputMap.get("nschemecode");
		
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
		
		if (nschemecode != Enumeration.TransactionStatus.NA.gettransactionstatus()) {

			final String scheme = "SELECT nproductcode FROM invoiceschemesproducts WHERE nschemecode = " + nschemecode
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
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
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND itt.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND tpd.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and isp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " AND ic.nproductcode <> -1 AND isp.nschemecode =" + nschemecode + "  " + " AND itt.ncaltypecode ="
					+ Enumeration.LinkType.EXISTING.getType() + " UNION ALL "
					+ " SELECT DISTINCT ic.sproductname AS sproductname, "
					+ " ic.nproductcode,ic.nlimsproduct,ic.sproductname AS master_sproductname,ic.ntypecode, "
					+ " ic.slimscode,ic.sdescription,ic.sinvoicedescription,ic.saddtext1,ic.saddtext2,ic.ntaxavailable, "
					+ " itt.staxname,itt.ntax, ic.ncost AS ncost " + " FROM invoiceproductmaster ic JOIN "
					+ " taxproductdetails tpd ON ic.nproductcode = tpd.nproductcode "
					+ " JOIN invoicetaxtype itt ON itt.ntaxcode = tpd.ntaxcode " + " WHERE ic.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND itt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND tpd.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " AND ic.nproductcode <> -1 AND ic.nproductcode NOT IN ( " + " " + formattedList + "" + "    )"
					+ " AND itt.ncaltypecode =" + Enumeration.LinkType.EXISTING.getType() + " union all"
					+ " select distinct ic.sproductname,ic.nproductcode,ic.nlimsproduct,ic.sproductname AS master_sproductname,ic.ntypecode,"
					+ " ic.slimscode,ic.sdescription,ic.sinvoicedescription,ic.saddtext1,ic.saddtext2,ic.ntaxavailable,'' AS staxname,0 AS ntax,ic.ncost"
					+ " from invoiceproductmaster ic left join taxproductdetails tpd ON ic.nproductcode = tpd.nproductcode"
					+ " where ic.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ic.sproductname not in ("+productname+")"
					+ " and tpd.nproductcode IS NULL order by sproductname";

		} else {
			strQuery = 
				    "SELECT ic.nproductcode, ic.nlimsproduct, ic.sproductname, ic.ntypecode, ic.slimscode, " +
				    "ic.sdescription, ic.sinvoicedescription, ic.saddtext1, ic.saddtext2, ic.ntaxavailable, ic.ncost, " +
				    "STRING_AGG(itt.staxname, ', ') AS staxname, COALESCE(SUM(itt.ntax), 0) AS ntax " +
				    "FROM invoiceproductmaster ic " +
				    "LEFT JOIN taxproductdetails tpd ON ic.nproductcode = tpd.nproductcode " +
				    "LEFT JOIN invoicetaxtype itt ON itt.ntaxcode = tpd.ntaxcode AND itt.ncaltypecode = " + Enumeration.LinkType.EXISTING.getType() + " " +
				    "WHERE ic.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				    + " and ic.sproductname not in ("+productname+")" +
				    " GROUP BY ic.nproductcode, ic.nlimsproduct, ic.sproductname, ic.ntypecode, ic.slimscode, " +
				    "ic.sdescription, ic.sinvoicedescription, ic.saddtext1, ic.saddtext2, ic.ntaxavailable, ic.ncost " +
				    "ORDER BY ic.sproductname";
		}
		final List<InvoiceProductMaster> filteredList = jdbcTemplate.query(strQuery, new InvoiceProductMaster());

		outputMap.put("filteredList", filteredList);
		outputMap.put("Discount", discount);

		final String str = "select testList->>'sspecname' AS sspecname,testList->>'sproducttestname' AS sproducttestname,testList->>'ntestcost' AS ntestcost,testList->>'selected' AS selected,testList->>'nproducttestcode' AS nproducttestcode "
				+ "from quotationitemdetails q,jsonb_array_elements(q.jsondata1 -> 'TestList') AS testList where nquotationitemdetailscode="
				+ nquotationitemdetailscode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
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
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getProducts(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		final Map<String, Object> objMap = new HashMap<String, Object>();
		final int filterValue = (int) inputMap.get("products");
		final int ncustomercode = (int) inputMap.get("ncustomercode");
		final int nschemecode = (int) inputMap.get("nschemecode");
		List<Integer> Productcodes = new ArrayList<>();
		final int tax;
		final String st = "Select * from invoiceproductmaster where nproductcode=" + filterValue + " and nsitecode= "
				+ userInfo.getNmastersitecode();
		final List<InvoiceProductMaster> Product = (List<InvoiceProductMaster>) jdbcTemplate.query(st,
				new InvoiceProductMaster());
		if (nschemecode == Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			
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
					+ " and pt.sproductname='" + Product.get(0).getSproductname() + "'" + " and ipm.nsitecode="
					+ userInfo.getNmastersitecode() + " and pt.nmastersitecode=" + userInfo.getNmastersitecode()+"";
			
			// Add testcode condition only if testgroupspec is not empty
			if (testgroupspec != null && !testgroupspec.isEmpty() && testgroupspec.get(0) != null) {
				str += " and pt.ntestcode in("+ testCodes+")";
			}
			
			final List<ProductTest> ProductTestDetails = (List<ProductTest>) jdbcTemplate.query(str, new ProductTest());
			objMap.put("selectedTest", ProductTestDetails);
		} else {
			final String str = " select distinct testList->>'specname' AS sspecname,testList->>'sproductstest' AS sproducttestname,testList->>'cost' AS ntestcost "
					+ "from invoiceschemesproducts inp,invoiceschemes ins,jsonb_array_elements(inp.jsondata2 -> 'TestList') AS testList where "
					+ "inp.nproductcode = " + filterValue + " and inp.nschemecode=" + nschemecode + " and ins.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and inp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and inp.nsitecode= "
					+ userInfo.getNmastersitecode() + " and ins.nsitecode=" + userInfo.getNmastersitecode();
			final List<ProductTest> ProductTestDetails = (List<ProductTest>) jdbcTemplate.query(str, new ProductTest());
			objMap.put("selectedTest", ProductTestDetails);
		}
		final String GetQryExistingProd = "select * from invoiceexecustomerproducts where " + " nproductcode=" + filterValue
				+ " and ncustomercode=" + ncustomercode + " and nschemecode=" + nschemecode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode() + "";
		final List<InvoiceExeCustomerProducts> ExistingProducts = (List<InvoiceExeCustomerProducts>) jdbcTemplate
				.query(GetQryExistingProd, new InvoiceExeCustomerProducts());

		final String taxValue = "select itt.ntax as nindirectax,staxname as sindirecttaxname from invoiceproductmaster ic,invoicetaxtype itt JOIN taxproductdetails tpd "
				+ "ON itt.ntaxcode = tpd.ntaxcode where  ic.nproductcode=" + filterValue + " and tpd.nproductcode="
				+ filterValue + " and itt.ncaltypecode=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ic.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and tpd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and itt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nsitecode= "
				+ userInfo.getNmastersitecode() + " and itt.nsitecode=" + userInfo.getNmastersitecode()
				+ " and tpd.nsitecode=" + userInfo.getNmastersitecode();
		final List<InvoiceProductMaster> taxValueList = (List<InvoiceProductMaster>) jdbcTemplate.query(taxValue,
				new InvoiceProductMaster());

		if (ExistingProducts.size() != 0) {
			final String toSetProdcuts = "select nproductcode,nschemecode,sproductname,ncost,ntax, ntaxpercentage, staxname,ntotalcost, noverallcost,ndiscountpercentage, nquantity,nunit,nlimsproduct from invoiceexecustomerproducts where "
					+ " nproductcode=" + filterValue + " and ncustomercode=" + ncustomercode + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
					+ userInfo.getNmastersitecode() + "";
			final List<InvoiceExeCustomerProducts> InsertProducts = (List<InvoiceExeCustomerProducts>) jdbcTemplate
					.query(toSetProdcuts, new InvoiceExeCustomerProducts());

			final InvoiceExeCustomerProducts filteredList = InsertProducts.get(InsertProducts.size() - 1);
			if (!taxValueList.isEmpty()) {
				filteredList.setNindirectax(taxValueList.get(taxValueList.size() - 1).getNindirectax());
				filteredList.setSindirecttaxname(taxValueList.get(taxValueList.size() - 1).getSindirecttaxname());
			}
			objMap.put("selectedProduct", filteredList);

		} else if (nschemecode == Enumeration.TransactionStatus.NA.gettransactionstatus() || (nschemecode >= 0)) {
			final String strproduct = "select nproductcode from invoiceschemesproducts where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
					+ userInfo.getNmastersitecode() + " and nschemecode=" + nschemecode + "";
			final List<InvoiceSchemesProduct> filteredListproduct = (List<InvoiceSchemesProduct>) jdbcTemplate
					.query(strproduct, new InvoiceSchemesProduct());

			Productcodes = filteredListproduct.stream().map(InvoiceSchemesProduct::getNproductcode)
					.collect(Collectors.toList());
			if (!Productcodes.contains(filterValue)) {
				String strQuery = "";
				strQuery = "select * from invoiceproductmaster ic,taxproductdetails td where ic.nproductcode="
						+ filterValue + " and td.nproductcode=" + filterValue + " and ic.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nsitecode= "
						+ userInfo.getNmastersitecode() + " and td.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and td.nsitecode="
						+ userInfo.getNmastersitecode();
				final List<InvoiceProductMaster> filteredList2 = (List<InvoiceProductMaster>) jdbcTemplate.query(strQuery,
						new InvoiceProductMaster());
				if (!filteredList2.isEmpty()) {
					tax = filteredList2.get(0).getNtaxavailable();
				} else {
					tax = 4;
				}
				if (tax == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					strQuery = "SELECT ic.nproductcode, ic.nlimsproduct, ic.ntypecode, ic.slimscode, ic.sproductname, ic.ncost, ic.ntaxavailable, "
							+ "SUM(itt.ntax) AS ntax,  STRING_AGG(itt.staxname, ', ') AS staxname, "
							+ "(ic.ncost * SUM(itt.ntax) / 100) AS totaltaxamount " + "FROM invoiceproductmaster ic "
							+ "JOIN taxproductdetails tpd ON ic.nproductcode = tpd.nproductcode "
							+ "JOIN invoicetaxtype itt ON itt.ntaxcode = tpd.ntaxcode " + "WHERE ic.nproductcode = "
							+ filterValue + " " + "AND tpd.nproductcode = " + filterValue + " "
							+ "AND itt.ncaltypecode = " + Enumeration.LinkType.EXISTING.getType() + " "
							+ "AND ic.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "AND tpd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "AND itt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ic.nsitecode= " + userInfo.getNmastersitecode() + " and tpd.nsitecode= "
							+ userInfo.getNmastersitecode() + " and itt.nsitecode= " + userInfo.getNmastersitecode()
							+ " GROUP BY ic.nproductcode, ic.nlimsproduct, ic.ntypecode, ic.slimscode, ic.sproductname, ic.ncost, ic.ntaxavailable";

				} else {
					strQuery = "select * from invoiceproductmaster where nproductcode=" + filterValue + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
							+ userInfo.getNmastersitecode();
				}
				final List<InvoiceProductMaster> filteredList = (List<InvoiceProductMaster>) jdbcTemplate.query(strQuery,
						new InvoiceProductMaster());
				final InvoiceProductMaster InsertProduct = filteredList.get(filteredList.size() - 1);
				InsertProduct.setNschemecode(nschemecode);
				if (!taxValueList.isEmpty()) {
					InsertProduct.setNindirectax(taxValueList.get(taxValueList.size() - 1).getNindirectax());
					InsertProduct.setSindirecttaxname(taxValueList.get(taxValueList.size() - 1).getSindirecttaxname());
				}
				objMap.put("selectedProduct", InsertProduct);
			} else {
				final String strQuerytaxproduct;
				final String str = "select * from taxproductdetails where nproductcode=" + filterValue + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
						+ userInfo.getNmastersitecode();
				final List<TaxProductDetails> taxList = (List<TaxProductDetails>) jdbcTemplate.query(str,
						new TaxProductDetails());
				if (taxList.isEmpty()) {
					strQuerytaxproduct = "select isp.nschemecode,isp.ncost,isp.jsondata->>'nproductcode' as nproductcode,isp.jsondata->>'sproductname' as sproductname,"
							+ " isp.jsondata->>'nlimsproduct' as nlimsproduct,isp.jsondata1->>'ntax' as ntax,isp.jsondata1->>'staxname' as staxname, "
							+ "isp.jsondata->>'nactive' as nactive,isp.jsondata1->>'nindirecttax' as nindirectax,isp.jsondata1->>'sindirecttaxname' as sindirecttaxname "
							+ "from invoiceschemesproducts isp where isp.nproductcode=" + filterValue
							+ " and isp.nschemecode=" + nschemecode + " and isp.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and isp.nsitecode= "
							+ userInfo.getNmastersitecode();
				} else {
					strQuerytaxproduct = "select isp.nschemecode,isp.ncost,isp.jsondata->>'nproductcode' as nproductcode,isp.jsondata->>'sproductname' as sproductname,"
							+ " isp.jsondata->>'nlimsproduct' as nlimsproduct,isp.jsondata1->>'ntax' as ntax,isp.jsondata1->>'staxname' as staxname,"
							+ " isp.jsondata->>'nactive' as nactive,isp.jsondata1->>'nindirecttax' as nindirectax,isp.jsondata1->>'sindirecttaxname' as sindirecttaxname"
							+ " from invoiceschemesproducts isp,taxproductdetails tpd where isp.nproductcode="
							+ filterValue + " and isp.nschemecode=" + nschemecode + "" + " and tpd.ncaltypecode="
							+ Enumeration.LinkType.EXISTING.getType()
							+ " and tpd.nproductcode=isp.nproductcode and isp.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and isp.nsitecode= "
							+ userInfo.getNmastersitecode() + " and tpd.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tpd.nsitecode="
							+ userInfo.getNmastersitecode();
				}
				final List<InvoiceProductMaster> filteredList = (List<InvoiceProductMaster>) jdbcTemplate
						.query(strQuerytaxproduct, new InvoiceProductMaster());
				objMap.put("selectedProduct", filteredList.get(filteredList.size() - 1));
			}
		}

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}
	/**
	 * This interface declaration is used to get the Active currency with respect to
	 * site.
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of InvoiceQuotation with
	 *         respect to site and also have the HTTP response code
	 * @throws Exception
	 */
	public String getActivecurrency(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		final Object value = inputMap.get("ntotalamount");
		final Object CheckWordOrNum = 1;
		final Integer CheckWordorNum = (Integer) CheckWordOrNum;
		String CurrencyValue = "";
		if (value instanceof Double || value instanceof Integer && CheckWordorNum == 1) {
			System.out.println("Numeric value is a Integer");
			final double intValue = ((Number) value).doubleValue();
			System.out.println("Converted value as Integer: " + intValue);
		} else {
			System.out.println("Numeric value is a Float");
			final int amountValue = Math.round((float) Float.parseFloat((String) value));
			final double CheckWordOrNum2 = amountValue;
			System.out.println("Converted value as Integer: " + CheckWordOrNum2);
			CurrencyValue = ProjectDAOSupport.convertToWords(CheckWordOrNum2, userInfo);
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
	 * This interface declaration is used to add a new entry to InvoiceQuotation
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> createQuotation(final Map<String, Object> inputMap, final UserInfo objUserInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<String> multilingualIDListProduct = new ArrayList<>();
		final List<Object> savedUnitList = new ArrayList<>();
		final JSONObject actionType = new JSONObject();
		final JSONObject jsonObject = new JSONObject(inputMap.get("quotationJson").toString());
		final JSONObject jsonObject1 = new JSONObject(inputMap.get("taxJson").toString());
		int seqnoquotation = jdbcTemplate.queryForObject(
				"select nsequenceno from " + " seqnoinvoice where stablename='InvoiceQuotationHeader'", Integer.class);
		seqnoquotation++;
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		final String Fromdate = (String) inputMap.get("dquotationfromdate");
		final String Todate = (String) inputMap.get("dquotationtodate");
		final Date datefrom = (Date) formatter.parse(Fromdate);
		final Date dateto = (Date) formatter.parse(Todate);

		if (datefrom.before(dateto)) {
			final String strformat = projectDAOSupport.getSeqfnFormat("invoicequotationheader",
					"seqnoformatgeneratorinvquotation", 0, 0, objUserInfo);
			
			
			String dtenderrefdateValue = (inputMap.get("dtenderrefdate") == null || 
                    inputMap.get("dtenderrefdate").toString().trim().equalsIgnoreCase("null") || 
                    inputMap.get("dtenderrefdate").toString().trim().isEmpty())
                     ? "NULL"   // No quotes → actual SQL NULL
                    : "'" + inputMap.get("dtenderrefdate") + "'";  // Add quotes only if value exists
			
			String dorderrefdateValue = (inputMap.get("dorderrefdate") == null || 
                    inputMap.get("dorderrefdate").toString().trim().equalsIgnoreCase("null") || 
                    inputMap.get("dorderrefdate").toString().trim().isEmpty())
                     ? "NULL"   // No quotes → actual SQL NULL
                    : "'" + inputMap.get("dorderrefdate") + "'";  // Add quotes only if value exists
			

			final String insmat = " INSERT INTO invoicequotationheader(nquotationseqcode,squotationno,dquotationdate,dquotationfromdate,dquotationtodate,nconvertedinvoice,ncustomercode,sprojectcode,sprojectname,stenderrefno,dtenderrefdate,sschemename,dorderrefdate,ntotalamount,ntotaltaxamount,ntotalfrightcharges,npaymentmode,spaymentdetails,nbankcode,ncurrencytype,nproducttestcode,jsondata,jsondata1,sremarks1,sremarks2,dmodifieddate,nusercode,nsitecode,nstatus)"
					+ " VALUES (" + seqnoquotation + ", '" + strformat + "','" + inputMap.get("dquotationdate") + "','"
					+ inputMap.get("dquotationfromdate") + "' ,'" + inputMap.get("dquotationtodate") + "',"
					+ inputMap.get("nconvertedinvoice") + "," + inputMap.get("ncustomercode") + ",'"
					+ inputMap.get("sprojectcode") + "','" + inputMap.get("sprojectname") + "','"
					+ inputMap.get("stenderrefno") + "'," +dtenderrefdateValue+ ",'"
					+ inputMap.get("sschemename") + "'," + dorderrefdateValue + ",'"
					+ inputMap.get("ntotalamount") + "','" + inputMap.get("ntotaltaxamount") + "','"
					+ inputMap.get("ntotalfrightcharges") + "'," + inputMap.get("npaymentcode") + ",'"
					+ inputMap.get("spaymentdetails") + "'," + inputMap.get("nbankcode") + ","
					+ inputMap.get("ncurrencytype") + "," + inputMap.get("nproducttestcode") + ",'"
					+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "'::jsonb,'"
					+ stringUtilityFunction.replaceQuote(jsonObject1.toString()) + "'::jsonb,'"
					+ inputMap.get("sremarks1") + "','" + inputMap.get("sremarks2") + "','"
					+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'," + objUserInfo.getNusercode() + ","
					+ objUserInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			jdbcTemplate.execute(insmat);
			jdbcTemplate.execute("update seqnoinvoice set nsequenceno=" + seqnoquotation
					+ "  where stablename='InvoiceQuotationHeader' ");
			createproductForQuotation(inputMap, seqnoquotation, objUserInfo, strformat);
			createTransactionStatusForQuotation(seqnoquotation, objUserInfo, strformat);
			inputMap.put("nflag", 1);
			actionType.put("InvoiceQuotationHeader", "IDS_ADDQUOTATIONHEADER");
			multilingualIDList.add("IDS_ADDINVOICEQUOTATION");
			multilingualIDListProduct.add("IDS_ADDINVOICEQUOTATIONPRODUCT");
			auditUtilityFunction.fnInsertAuditAction(getQuotationAndProduct(seqnoquotation, objUserInfo), 1, null,
					multilingualIDList, objUserInfo);
			List<InvoiceQuotationProduct> value = getProductForAudit(seqnoquotation, "", objUserInfo);
			for (InvoiceQuotationProduct obj : value) {
				savedUnitList.clear();
				savedUnitList.add(obj);
				auditUtilityFunction.fnInsertAuditAction(savedUnitList, 1, null, multilingualIDListProduct,
						objUserInfo);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TAXDATEALERT", objUserInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		return getQuotation(inputMap,objUserInfo);
	}

	/**
	 * This interface declaration is used to add a new entry to product Quotation
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createproductForQuotation(final Map<String, Object> inputMap, final int quotationSeqNo,
			final UserInfo objUserInfo, final String strformat) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final JSONObject jsonObject = new JSONObject(inputMap.get("productList").toString());
		final JSONArray jsonArray1 = jsonObject.getJSONArray("ProductList");

		if (jsonArray1.isEmpty()) {
			
			return new ResponseEntity<>(
				    commonFunction.getMultilingualMessage("IDS_ADDPRODUCTS", objUserInfo.getSlanguagefilename()),
				    HttpStatus.CONFLICT
				);
			
		}
		final JSONArray jsonArray = jsonObject.getJSONArray("ProductList");
		if (jsonArray != null) {
			jsonArray.forEach(objProductList -> {
				int seqnoquotation = jdbcTemplate.queryForObject(
						"select nsequenceno from " + " seqnoinvoice where stablename='quotationitemdetails'",
						Integer.class);
				seqnoquotation++;

				JSONObject jsonObject1 = (JSONObject) objProductList;
				final JSONObject jsonObjectTest = new JSONObject(jsonObject1.get("testList").toString());
				jsonObject1.remove("testList");
				final int nlimsproduct = jsonObject1.getInt("nlimsproduct");
				try {
					jsonObject1 = productTaxCalculation(jsonObject1);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				String insertQuery = "";
				try {

					insertQuery = " INSERT INTO quotationitemdetails(nquotationitemdetailscode, nserialno, squotationseqno, dquotationdate, jsondata,jsondata1, nlimsproduct,dmodifieddate, nsitecode, nstatus)"
							+ " VALUES(" + seqnoquotation + "," + seqnoquotation + ",'" + strformat + "','"
							+ inputMap.get("dquotationdate") + "','"
							+ stringUtilityFunction.replaceQuote(jsonObject1.toString()) + "'::jsonb,'"
							+ stringUtilityFunction.replaceQuote(jsonObjectTest.toString()) + "'::jsonb," + nlimsproduct
							+ ",'" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ objUserInfo.getNmastersitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

				} catch (Exception e) {
					e.printStackTrace();
				}
				jdbcTemplate.execute(insertQuery);
				jdbcTemplate.execute("update seqnoinvoice set nsequenceno=" + seqnoquotation
						+ "  where stablename='quotationitemdetails' ");
			});
		}

		final String ssymbol = jdbcTemplate.queryForObject("select ssymbol from invoicecurrencytype where ncurrencycode= "
				+ inputMap.get("ncurrencytype") + "" + " and nsitecode= " + objUserInfo.getNmastersitecode(),
				String.class);
		final List<InvoiceQuotationProduct> lstProduct = (List<InvoiceQuotationProduct>) jdbcTemplate.query(
				"select jsondata->>'noverallcost' as noverallcost,jsondata->>'ntaxamount' as ntaxamount from quotationitemdetails where squotationseqno='"
						+ strformat + "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode= " + objUserInfo.getNmastersitecode(),
				new InvoiceQuotationProduct());
		final double totalamount = lstProduct.stream().mapToDouble(x -> Double.valueOf(x.getNoverallcost())).sum();
		final double taxamount = lstProduct.stream().mapToDouble(x -> Double.valueOf(x.getNtaxamount())).sum();
		final String overAllCost = String.format("%.2f", totalamount);
		final String totaltaxamount = String.format("%.2f", taxamount);
		inputMap.put("ntotalamount", overAllCost);
		final String totalamountinword = getActivecurrency(inputMap, objUserInfo);
		final String taxAmountInWords = getAmountInWords(inputMap, objUserInfo, "totaltaxamount", true);

		final JSONObject jsonObject2 = new JSONObject(inputMap.get("quotationJson").toString());
		jsonObject2.put("TotalAmountInWords", totalamountinword);
		jsonObject2.put("TotalAmount", overAllCost);
		jsonObject2.put("TotalTaxAmount", totaltaxamount);
		jsonObject2.put("TotalTaxAmountwords", taxAmountInWords);

		jdbcTemplate.execute("update invoicequotationheader set ntotalamount='" + (ssymbol + " " + overAllCost)
				+ "',ntotaltaxamount='" + (ssymbol + " " + totaltaxamount) + "',jsondata='"
				+ stringUtilityFunction.replaceQuote(jsonObject2.toString()) + "' where squotationno='" + strformat
				+ "'and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ objUserInfo.getNmastersitecode());

		final List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(strformat, objUserInfo);

		// update for tax overall calculation for report purpose

		final String taxJson = objMapper.writeValueAsString(taxList);
		final String str = "update invoicequotationheader set jsondata1='"
				+ stringUtilityFunction.replaceQuote(taxJson.toString()) + "' where squotationno='" + strformat
				+ "'and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ objUserInfo.getNmastersitecode();
		jdbcTemplate.execute(str);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to update entry in InvoiceQuotation table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings("unused")
	@Override
	public ResponseEntity<Object> updateQuotation(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final JSONObject actionType = new JSONObject();
		final JSONObject jsonObject = new JSONObject(inputMap.get("quotationJson").toString());
		final JSONObject jsonObject2 = new JSONObject(inputMap.get("taxJson").toString());
		final List<String> multilingualIDList = new ArrayList<>();
		final List<String> multilingualIDListProduct = new ArrayList<>();
		final List<Object> savedNewList = new ArrayList<>();
		final List<Object> savedOldList = new ArrayList<>();
		final int sequno = (int) inputMap.get("nquotationseqcode");

		final List<InvoiceQuotationHeader> QuotationHeader = getQuotationAndProduct(sequno, userInfo);
		final List<InvoiceQuotationProduct> QuotationProduct = getProductForAudit(sequno, "", userInfo);

//		final String strformat = projectDAOSupport.getSeqfnFormat("invoicequotationheader",
//				"seqnoformatgeneratorinvquotation", 0, 0, userInfo);
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		final String Fromdate = (String) inputMap.get("dquotationfromdate");
		final String Todate = (String) inputMap.get("dquotationtodate");
		final Date datefrom = formatter.parse(Fromdate);
		final Date dateto = formatter.parse(Todate);

		if (datefrom.before(dateto)) {
			final String nconvertedInvoice = inputMap.get("nconvertedinvoice") != null
					? inputMap.get("nconvertedinvoice").toString().trim()
					: "NULL";

			final String ncustomercode = inputMap.get("ncustomercode") != null
					? inputMap.get("ncustomercode").toString().trim()
					: "NULL";

			final String ntotalFrightCharges = inputMap.get("ntotalfrightcharges") != null
					? inputMap.get("ntotalfrightcharges").toString().trim()
					: "NULL";

			final JSONArray testListArray;

			if (jsonObject.has("testList") && !jsonObject.isNull("testList")) {
				testListArray = jsonObject.getJSONArray("testList");
			} else {
				System.out.println("Key 'testList' not found in JSON. Assigning an empty array.");
				testListArray = new JSONArray();
			}

			System.out.println("testListArray: " + testListArray.toString());

			
			
			String dtenderrefdateValue = (inputMap.get("dtenderrefdate") == null || 
                    inputMap.get("dtenderrefdate").toString().trim().equalsIgnoreCase("null") || 
                    inputMap.get("dtenderrefdate").toString().trim().isEmpty())
                     ? "NULL"   // No quotes → actual SQL NULL
                    : "'" + inputMap.get("dtenderrefdate") + "'";  // Add quotes only if value exists
			
			String dorderrefdateValue = (inputMap.get("dorderrefdate") == null || 
                    inputMap.get("dorderrefdate").toString().trim().equalsIgnoreCase("null") || 
                    inputMap.get("dorderrefdate").toString().trim().isEmpty())
                     ? "NULL"   // No quotes → actual SQL NULL
                    : "'" + inputMap.get("dorderrefdate") + "'";  // Add quotes only if value exists
			
			final String query = "UPDATE invoicequotationheader SET " + "dquotationdate='" + inputMap.get("dquotationdate")
					+ "', " + "dquotationfromdate='" + inputMap.get("dquotationfromdate") + "', " + "dquotationtodate='"
					+ inputMap.get("dquotationtodate") + "', " + "nconvertedinvoice=" + nconvertedInvoice + ", "
					+ "ncustomercode=" + ncustomercode + ", " + "sprojectcode='" + inputMap.get("sprojectcode") + "', "
					+ "sprojectname='" + inputMap.get("sprojectname") + "', " + "stenderrefno='"
					+ inputMap.get("stenderrefno") + "', " + "dtenderrefdate=" + dtenderrefdateValue + ","
					+ "sschemename='" + inputMap.get("sschemename") + "', " + "dorderrefdate="
					+ dorderrefdateValue + ", " + "ntotalamount='" + inputMap.get("ntotalamount") + "', "
					+ "ntotaltaxamount='" + inputMap.get("ntotaltaxamount") + "', " + "ntotalfrightcharges="
					+ ntotalFrightCharges + ", " + "npaymentmode=" + inputMap.get("npaymentcode") + ", "
					+ "nproducttestcode=" + inputMap.get("nproducttestcode") + ", " + "spaymentdetails='"
					+ inputMap.get("spaymentdetails") + "', " + "nbankcode=" + inputMap.get("nbankcode") + ", "
					+ "ncurrencytype=" + inputMap.get("ncurrencytype") + ", " + "jsondata='"
					+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "', " + "jsondata1='"
					+ stringUtilityFunction.replaceQuote(jsonObject2.toString()) + "', " + "sremarks1='"
					+ inputMap.get("sremarks1") + "', " + "sremarks2='" + inputMap.get("sremarks2") + "', "
					+ "dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "nusercode="
					+ userInfo.getNusercode() + ", " + "nsitecode=" + userInfo.getNmastersitecode() + " "
					+ "WHERE nquotationseqcode IN (" + inputMap.get("nquotationseqcode") + ");";

			jdbcTemplate.execute(query);

			updateProductQuotation(inputMap, userInfo);
			final JSONObject jsonObject1 = new JSONObject(inputMap.get("productList").toString());
			final JSONArray jsonArray = jsonObject1.getJSONArray("ProductList");
			if (jsonArray.isEmpty()) {
				String S = "Add Product Details";
				return new ResponseEntity<>(S, HttpStatus.CONFLICT);
			}
			final List<InvoiceQuotationHeader> Header = getQuotationAndProduct(sequno, userInfo);
			final List<InvoiceQuotationProduct> Product = getProductForAudit(sequno, "", userInfo);
			inputMap.put("nflag", 2);
			actionType.put("InvoiceQuotationHeader", "IDS_EDITQUOTATIONHEADER");
			multilingualIDList.add("IDS_EDITINVOICEQUOTATION");
			auditUtilityFunction.fnInsertAuditAction(Header, 2, QuotationHeader, multilingualIDList, userInfo);
			multilingualIDListProduct.add("IDS_EDITINVOICEQUOTATIONPRODUCT");
			for (int i = 0; i < Product.size() && i < QuotationProduct.size(); i++) {
				savedNewList.clear();
				savedNewList.add(Product.get(i));
				savedOldList.clear();
				savedOldList.add(QuotationProduct.get(i));
				auditUtilityFunction.fnInsertAuditAction(savedNewList, 2, savedOldList, multilingualIDListProduct,
						userInfo);
			}
		} else {
			 
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TAXDATEALERT", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
			
		}
		return getQuotation(inputMap,userInfo);
	}

	/**
	 * This interface declaration is used to get Active Quotation with table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveInvoiceQuotationById(final Map<String, Object> inputMap) {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userinfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference<UserInfo>() {
		});		
		String quantity = (String) inputMap.get("Quantity");
		String formattedQuantity = "0".equals(quantity) ? "" : quantity;
		String str = "select jsondata1::jsonb as jsondata1,nquotationitemdetailscode,squotationseqno,jsondata->>'nindirectax' as nindirectax,jsondata->>'sindirecttaxname' as sindirecttaxname,jsondata->>'nproductcode' as nproductcode,jsondata->>'sproductname' as sproductname,jsondata->>'nunit' as nunit,"
				+ "jsondata->>'nquantity' as nquantity,jsondata->>'ncost' as ncost,"
				+ "jsondata->>'ndiscountpercentage' as ndiscountpercentage,jsondata->>'staxname' as staxname,jsondata->>'ntaxamount' as ntaxamount,"
				+ "jsondata->>'ntaxvalue' as ntaxvalue,jsondata->>'ntotalcost' as ntotalcost,jsondata->>'noverallcost' as noverallcost,jsondata->>'ntaxpercentage' as ntaxpercentage,jsondata->>'slno' as nserialno "
				+ "from quotationitemdetails where squotationseqno='" + inputMap.get("QuotationId")
				+ "' and jsondata->>'sproductname'='" + inputMap.get("ProductName") + "' and jsondata->>'nquantity'='"
				+ formattedQuantity + "'" + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userinfo.getNmastersitecode();


		final List<InvoiceQuotationProduct> lstProduct = (List<InvoiceQuotationProduct>) jdbcTemplate.query(str,
				new InvoiceQuotationProduct());
		return new ResponseEntity<>(lstProduct, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to update Product entry in
	 * InvoiceQuotation table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateProductQuotation(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final JSONObject actionType = new JSONObject();
		final List<String> multilingualIDListProduct = new ArrayList<>();
		final String squotationseqno = (inputMap.get("nquotationseqcode").toString());
		final JSONObject jsonObject = new JSONObject(inputMap.get("productList").toString());
		final JSONArray jsonArray = jsonObject.getJSONArray("ProductList");
		final String strquotation = "select squotationno from invoicequotationheader where nquotationseqcode=" + squotationseqno
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		final List<InvoiceQuotationHeader> strformat = (List<InvoiceQuotationHeader>) jdbcTemplate.query(strquotation,
				new InvoiceQuotationHeader());
		String strformatno = strformat.get(strformat.size() - 1).getSquotationno();
		if (jsonArray != null) {
			jsonArray.forEach(objProductList -> {
				final String quotationitemdetailscode = (((JSONObject) objProductList).get("nquotationitemdetailscode")
						.toString());
				final int quotationitemdetails = Integer.parseInt(quotationitemdetailscode);
				JSONObject jsonObject1 = (JSONObject) objProductList;
				final JSONObject jsonObjectTest = new JSONObject(jsonObject1.get("testList").toString());
				jsonObject1.remove("testList");
				jsonObject1.remove("jsondata1");
				final int nlimsproduct = jsonObject1.getInt("nlimsproduct");
				try {
					jsonObject1 = productTaxCalculation(jsonObject1);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				if (quotationitemdetails != 0) {
					final String strquery = "update quotationitemdetails set jsondata='"
							+ stringUtilityFunction.replaceQuote(jsonObject1.toString()) + "',jsondata1='"
							+ stringUtilityFunction.replaceQuote(jsonObjectTest.toString()) + "',nlimsproduct="
							+ nlimsproduct + " where nquotationitemdetailscode=" + quotationitemdetails
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode= " + userInfo.getNmastersitecode();
					jdbcTemplate.execute(strquery);
				} else {
					int seqnoquotation = jdbcTemplate.queryForObject(
							"select nsequenceno from " + " seqnoinvoice where stablename='quotationitemdetails'",
							Integer.class);
					seqnoquotation++;
					String insertQuery = "";
					try {
						insertQuery = " INSERT INTO quotationitemdetails(nquotationitemdetailscode, nserialno, squotationseqno, dquotationdate, jsondata,jsondata1,nlimsproduct, dmodifieddate, nsitecode, nstatus)"
								+ " VALUES(" + seqnoquotation + "," + seqnoquotation + ",'" + strformatno + "','"
								+ inputMap.get("dquotationdate") + "','"
								+ stringUtilityFunction.replaceQuote(jsonObject1.toString()) + "'::jsonb,'"
								+ stringUtilityFunction.replaceQuote(jsonObjectTest.toString()) + "'::jsonb,"
								+ nlimsproduct + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ userInfo.getNmastersitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
					} catch (Exception e) {
						e.printStackTrace();
					}
					jdbcTemplate.execute(insertQuery);
					jdbcTemplate.execute("update seqnoinvoice set nsequenceno=" + seqnoquotation
							+ "  where stablename='quotationitemdetails' ");
					inputMap.put("nflag", 1);

					actionType.put("InvoiceQuotationHeader", "IDS_ADDQUOTATIONHEADER");
					multilingualIDListProduct.add("IDS_ADDINVOICEQUOTATIONPRODUCT");

					try {
						auditUtilityFunction.fnInsertAuditAction(getProductForAudit(seqnoquotation, "", userInfo), 1,
								null, multilingualIDListProduct, userInfo);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			final String str = "select jsondata->>'ntaxamount' AS ntaxamount,jsondata->>'noverallcost' AS noverallcost from quotationitemdetails where squotationseqno='"
					+ strformatno + "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode= " + userInfo.getNmastersitecode();
			final List<InvoiceQuotationProduct> productCostList = (List<InvoiceQuotationProduct>) jdbcTemplate.query(str,
					new InvoiceQuotationProduct());

			final double totalamount = productCostList.stream().mapToDouble(x -> {
				final String cost = x.getNoverallcost();
				return (cost != null && !cost.trim().isEmpty()) ? Double.parseDouble(cost) : 0.0;
			}).sum();

			final double taxamount = productCostList.stream().mapToDouble(x -> {
				final String tax = x.getNtaxamount();
				return (tax != null && !tax.trim().isEmpty()) ? Double.parseDouble(tax) : 0.0;
			}).sum();
			String taxamountSum = String.format("%.2f", taxamount);
			String overallcostSum = String.format("%.2f", totalamount);
			String ssymbol = jdbcTemplate.queryForObject(
					"select ssymbol from invoicecurrencytype where ncurrencycode=" + inputMap.get("ncurrencytype")
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode= " + userInfo.getNmastersitecode(),
					String.class);
			inputMap.put("ntotalamount", overallcostSum);
			String totalamountinword = getActivecurrency(inputMap, userInfo);
			String taxAmountInWords = getAmountInWords(inputMap, userInfo, "taxamountSum", true);

			final JSONObject jsonObject2 = new JSONObject(inputMap.get("quotationJson").toString());
			jsonObject2.put("TotalAmountInWords", totalamountinword);
			jsonObject2.put("TotalAmount", overallcostSum);
			jsonObject2.put("TotalTaxAmount", taxamountSum);
			jsonObject2.put("TotalTaxAmountwords", taxAmountInWords);

			jdbcTemplate.execute("update invoicequotationheader set ntotalamount='" + (ssymbol + " " + overallcostSum)
					+ "',ntotaltaxamount='" + (ssymbol + " " + taxamountSum) + "',jsondata='"
					+ stringUtilityFunction.replaceQuote(jsonObject2.toString()) + "' where squotationno='"
					+ strformatno + "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode= " + userInfo.getNmastersitecode());

			final List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(strformatno, userInfo);

			// update for tax overall calculation for report purpose

			final String taxJson = objMapper.writeValueAsString(taxList);
			jdbcTemplate.execute("update invoicequotationheader set jsondata1='"
					+ stringUtilityFunction.replaceQuote(taxJson.toString()) + "' where squotationno='" + strformatno
					+ "'and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
					+ userInfo.getNmastersitecode());

			return getQuotation(inputMap,userInfo);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get productTaxCalculation.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public JSONObject productTaxCalculation(final JSONObject jsonObject1) throws Exception {
		if (jsonObject1.getInt("nindirectax") != 0) {
			final double tax = (double) ((Double.parseDouble(jsonObject1.getString("ntaxvalue"))
					* jsonObject1.getInt("nindirectax")) / 100);
			final String taxAmount = String.format("%.2f", tax);
			final double overAllCost = Double.parseDouble(jsonObject1.getString("noverallcostvalue"))
					+ Double.parseDouble(taxAmount);
			final double overAllTaxAmount = Double.parseDouble(jsonObject1.getString("ntaxvalue"))
					+ Double.parseDouble(taxAmount);
			final String overAllCostFormating = String.format("%.2f", overAllCost);
			final String overAllTaxAmountFormating = String.format("%.2f", overAllTaxAmount);
			jsonObject1.put("noverallcost", overAllCostFormating);
			jsonObject1.put("ntaxamount", overAllTaxAmountFormating);
			jsonObject1.put("nindirecttaxamount", taxAmount);
		}
		return jsonObject1;
	}

	/**
	 * This interface declaration is used to update entry in ProductQuotation table.
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings("unused")
	@Override
	public ResponseEntity<Object> updateOuterProductQuotation(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		JSONObject jsonObject = new JSONObject(inputMap.get("productJson").toString());
		jsonObject = productTaxCalculation(jsonObject);
		final JSONObject jsonObjectTest = new JSONObject(jsonObject.get("testList").toString());
		jsonObject.remove("testList");
		final String strquery = "update quotationitemdetails set jsondata='"
				+ stringUtilityFunction.replaceQuote(jsonObject.toString()) + "',jsondata1='"
				+ stringUtilityFunction.replaceQuote(jsonObjectTest.toString()) + "'"
				+ " where nquotationitemdetailscode=" + inputMap.get("nquotationitemdetailscode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode();
		jdbcTemplate.execute(strquery);
		String str = "select jsondata->>'ntaxamount' as ntaxamount,jsondata->>'noverallcost' as noverallcost from quotationitemdetails where squotationseqno='"
				+ inputMap.get("squotationseqno") + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode();
		String strq = "select jsondata from invoicequotationheader where squotationno='"
				+ inputMap.get("squotationseqno") + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode();

		final List<InvoiceQuotationHeader> quotationJson = (List<InvoiceQuotationHeader>) jdbcTemplate.query(strq,
				new InvoiceQuotationHeader());

		final JSONObject jsonObject1 = new JSONObject(quotationJson.get(quotationJson.size() - 1).getJsondata());
		final String jsonString = jsonObject1.toString();
		final List<InvoiceQuotationProduct> productCostList = (List<InvoiceQuotationProduct>) jdbcTemplate.query(str,
				new InvoiceQuotationProduct());

		String taxamountSum = "";
		if (!productCostList.get(0).getNtaxamount().isEmpty())

		{

			final double taxamount = productCostList.stream().mapToDouble(x -> {
				String tax = x.getNtaxamount();
				return (tax != null && !tax.trim().isEmpty()) ? Double.parseDouble(tax) : 0.0;
			}).sum();

		}

		else {
			final double taxamount = 0;
		}

		final double overallcost = productCostList.stream().mapToDouble(x -> Double.valueOf(x.getNoverallcost())).sum();
		final String strcurrency = "select ncurrencytype from invoicequotationheader where squotationno='"
				+ inputMap.get("squotationseqno") + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode();
		final List<InvoiceQuotationHeader> quotationlist = (List<InvoiceQuotationHeader>) jdbcTemplate.query(strcurrency,
				new InvoiceQuotationHeader());
		final String overallcostSum = String.format("%.2f", overallcost);
		final int ncurrencytype = quotationlist.get(quotationlist.size() - 1).getNcurrencytype();
		final String ssymbol = jdbcTemplate.queryForObject("select ssymbol from invoicecurrencytype where ncurrencycode="
				+ ncurrencytype + " and nsitecode= " + userInfo.getNmastersitecode(), String.class);
		inputMap.put("ntotalamount", overallcostSum);
		final String totalamountinword = getActivecurrency(inputMap, userInfo);
		final String taxAmountInWords = getAmountInWords(inputMap, userInfo, "taxamountSum", true);

		jsonObject1.put("TotalAmountInWords", totalamountinword);
		jsonObject1.put("TotalAmount", overallcostSum);
		jsonObject1.put("TotalTaxAmount", taxamountSum);
		jsonObject1.put("TotalTaxAmountwords", taxAmountInWords);

		jdbcTemplate.execute("update invoicequotationheader set ntotalamount='" + (ssymbol + " " + overallcostSum)
				+ "',ntotaltaxamount='" + (ssymbol + " " + taxamountSum) + "',jsondata='"
				+ stringUtilityFunction.replaceQuote(jsonObject1.toString()) + "' where squotationno='"
				+ inputMap.get("squotationseqno") + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode());

		final String squotationno = (String) inputMap.get("squotationseqno");
		final List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(squotationno, userInfo);
		final String taxJson = objMapper.writeValueAsString(taxList);
		jdbcTemplate.execute("update invoicequotationheader set jsondata1='"
				+ stringUtilityFunction.replaceQuote(taxJson.toString()) + "' where squotationno='" + squotationno
				+ "'and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode());

		return getQuotation(inputMap,userInfo);
	}

	/**
	 * This interface declaration is used to delete Product entry in
	 * InvoiceQuotation table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding detail to be
	 *                            deleted in InvoiceQuotation table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> deleteProductQuotation(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		//final Map<String, Object> parameterValue = (Map<String, Object>) inputMap.get("parameter");
		final Object invoiceIdObj = inputMap.get("QuotationId");
		final String strquery;
		if (invoiceIdObj != null) {
			strquery = "select nquotationitemdetailscode from quotationitemdetails where squotationseqno='"
					+ invoiceIdObj + "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
					+ " and nsitecode=" + userInfo.getNmastersitecode();
		} else {
			final Object invoiceIdObjseq = inputMap.get("QuotationSeqNo");
			strquery = "select nquotationitemdetailscode from quotationitemdetails where squotationseqno='"
					+ invoiceIdObjseq + "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "" + " and nsitecode=" + userInfo.getNmastersitecode();
		}

		final List<InvoiceQuotationProduct> nquotationdetailscode = (List<InvoiceQuotationProduct>) jdbcTemplate
				.query(strquery, new InvoiceQuotationProduct());

		final List<String> multilingualIDListProduct = new ArrayList<>();
		String seqid = "";
		String str = "";
		List<InvoiceQuotationHeader> quotation = null;
		Object s2 = null;
		if (inputMap.toString().contains("QuotationId")) {
			final String strqueryitem = "update quotationitemdetails set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where squotationseqno='"
					+ invoiceIdObj + "' and nquotationitemdetailscode='"
					+ inputMap.get("nquotationitemdetailscode") + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(strqueryitem);

			str = "select jsondata->>'ntaxamount' as ntaxamount,jsondata->>'noverallcost' as noverallcost from quotationitemdetails where squotationseqno='"
					+ invoiceIdObj + "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode=" + userInfo.getNmastersitecode();
			s2 = inputMap.get("QuotationId");

		} else if (inputMap.toString().contains("QuotationSeqNo")) {
			final String item = "update quotationitemdetails set nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where squotationseqno='"
					+ inputMap.get("QuotationSeqNo") + "' and nquotationitemdetailscode='"
					+ inputMap.get("nquotationitemdetailscode") + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(item);

			str = "select jsondata->>'ntaxamount' as ntaxamount,jsondata->>'noverallcost' as noverallcost from quotationitemdetails where squotationseqno='"
					+ inputMap.get("QuotationSeqNo") + "' and nquotationitemdetailscode='"
					+ inputMap.get("nquotationitemdetailscode") + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			s2 = inputMap.get("QuotationSeqNo");
		}

		final List<InvoiceQuotationProduct> productCostList = (List<InvoiceQuotationProduct>) jdbcTemplate.query(str,
				new InvoiceQuotationProduct());

		final double taxamount = productCostList.stream().mapToDouble(x -> Double.valueOf(x.getNtaxamount())).sum();
		final double overallcost = productCostList.stream().mapToDouble(x -> Double.valueOf(x.getNoverallcost())).sum();

		multilingualIDListProduct.add("IDS_DELETEINVOICEQUOTATIONPRODUCT");
		if (inputMap.toString().contains("QuotationSeqNo")) {
			seqid = "select nquotationseqcode from invoicequotationheader where squotationno='"
					+ inputMap.get("QuotationSeqNo") + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			quotation = (List<InvoiceQuotationHeader>) jdbcTemplate.query(seqid, new InvoiceQuotationHeader());
		}

		final String seqno = inputMap.toString().contains("QuotationSeqNo") ? quotation.get(0).getSquotationno()
				: (String) inputMap.get("QuotationId");
		auditUtilityFunction.fnInsertAuditAction(
				getProductForDelete(seqno, inputMap.get("nquotationitemdetailscode"), userInfo), 1, null,
				multilingualIDListProduct, userInfo);
		if (taxamount != 0 && overallcost != 0) {
			final String value = "update invoicequotationheader set ntotalamount='" + String.valueOf(overallcost)
					+ "',ntotaltaxamount='" + String.valueOf(taxamount) + "' where squotationno='" + s2
					+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(value);
		}

		final List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(seqno, userInfo);
		final String taxJson = objMapper.writeValueAsString(taxList);
		jdbcTemplate.execute("update invoicequotationheader set jsondata1='"
				+ stringUtilityFunction.replaceQuote(taxJson.toString()) + "' where squotationno='" + seqno
				+ "'and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

		return (inputMap.toString().contains("QuotationSeqNo") ? new ResponseEntity<>(HttpStatus.OK)
				: getQuotation(inputMap,userInfo));
	}

	/**
	 * This interface declaration is used to get delete entry in QuoationProduct
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding detail to be
	 *                            deleted in InvoiceQuotation table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public List<InvoiceQuotationProduct> getProductForDelete(final String seqno, final Object nquotationitemdetailscode,
			final UserInfo userInfo) {
		final String strQuery = "select nquotationitemdetailscode,squotationseqno,jsondata->>'sproductname' as sproductname,jsondata->>'nunit' as nunit,"
				+ "jsondata->>'nquantity' as nquantity,jsondata->>'ncost' as ncost,"
				+ "jsondata->>'ndiscountpercentage' as ndiscountpercentage,"
				+ "jsondata->>'ntaxamount' as ntaxvalue,jsondata->>'ntotalcost' as ntotalcost,jsondata->>'noverallcost' as noverallcost"
				+ " from quotationitemdetails where squotationseqno='" + seqno + "' and nquotationitemdetailscode='"
				+ nquotationitemdetailscode + "' and nstatus="
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode() + "";

		final List<InvoiceQuotationProduct> productList = (List<InvoiceQuotationProduct>) jdbcTemplate.query(strQuery,
				new InvoiceQuotationProduct());

		return productList;
	}

	/**
	 * This interface declaration is used to get ProductForAudit.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding detail to be
	 *                            deleted in InvoiceQuotation table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public List<InvoiceQuotationProduct> getProductForAudit(final int squotationseqno, final String screenName, final UserInfo userInfo)
			throws Exception {
		final String str = "select squotationno from invoicequotationheader where nquotationseqcode = " + squotationseqno + "";
		final List<InvoiceQuotationHeader> squotation = (List<InvoiceQuotationHeader>) jdbcTemplate.query(str,
				new InvoiceQuotationHeader());
		final String squotationno = squotation.get(0).getSquotationno();
		final String strQuery;
		if (screenName.isEmpty()) {
			strQuery = "select nquotationitemdetailscode,squotationseqno,jsondata->>'sproductname' as sproductname,jsondata->>'nunit' as nunit,"
					+ "jsondata->>'nquantity' as nquantity,jsondata->>'ncost' as ncost,"
					+ "jsondata->>'ndiscountpercentage' as ndiscountpercentage," + "jsondata->>'staxname' as staxname,"
					+ "jsondata->>'ntaxamount' as ntaxvalue,jsondata->>'ntotalcost' as ntotalcost,jsondata->>'noverallcost' as noverallcost"
					+ " from quotationitemdetails where squotationseqno='" + squotationno + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
					+ userInfo.getNmastersitecode() + "";
		} else {
			strQuery = "select nquotationitemdetailscode,squotationseqno,jsondata->>'sproductname' as sproductname,jsondata->>'nunit' as nunit,"
					+ "jsondata->>'nquantity' as nquantity,jsondata->>'ncost' as ncost,"
					+ "jsondata->>'ndiscountpercentage' as ndiscountpercentage," + "jsondata->>'staxname' as staxname,"
					+ "jsondata->>'ntaxamount' as ntaxvalue,jsondata->>'ntotalcost' as ntotalcost,jsondata->>'noverallcost' as noverallcost"
					+ " from quotationitemdetails where squotationseqno='" + squotationno + "' and nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " and nsitecode= "
					+ userInfo.getNmastersitecode() + "";
		}
		final List<InvoiceQuotationProduct> product = (List<InvoiceQuotationProduct>) jdbcTemplate.query(strQuery,
				new InvoiceQuotationProduct());

		return product;
	}

	/**
	 * This interface declaration is used get Quotation Product.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding detail to be
	 *                            deleted in InvoiceQuotation table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public List<InvoiceQuotationHeader> getQuotationAndProduct(final int nquotationseqcode, final UserInfo userInfo)
			throws Exception {

		final String strQuery = "select jsondata->>'CustomerName' as CustomerName,jsondata->>'CustomerType' as CustomerType,"
				+ "jsondata->>'TotalAmountInWords' as TotalAmountInWords,"
				+ "jsondata->>'CustomerGST' as CustomerGST,jsondata->>'Address' as Address,jsondata->>'EmailId' as EmailId,"
				+ "jsondata->>'PhoneNo' as PhoneNo,iqh.squotationno,iqh.dquotationfromdate,iqh.dquotationtodate,iqh.dquotationtodate,"
				+ "iqh.ntotalamount,iqh.ntotaltaxamount,iqh.spaymentdetails,iqh.dtenderrefdate,iqh.stenderrefno,iqh.dquotationdate,iqh.nquotationseqcode,"
				+ "iqh.ntotalfrightcharges,ic.scurrency,iph.sproducttestdetail,iqh.dorderrefdate,iqh.sschemename,iqh.sprojectcode from invoicequotationheader iqh,producttestdetail iph,invoicecurrencytype ic  where iph.nproducttestcode=iqh.nproducttestcode and iqh.ncurrencytype=ic.ncurrencycode and"
				+ " iqh.nquotationseqcode =" + nquotationseqcode + " and iqh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iqh.nsitecode= "
				+ userInfo.getNmastersitecode() + " and iph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iph.nmastersitecode="
				+ userInfo.getNmastersitecode() + "" + " and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nsitecode="
				+ userInfo.getNmastersitecode();
		final List<InvoiceQuotationHeader> quotation = (List<InvoiceQuotationHeader>) jdbcTemplate.query(strQuery,
				new InvoiceQuotationHeader());
		return quotation;
	}

	/**
	 * This interface declaration is used to get the overall Quotation search with
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getQuotationsearch(final Map<String, Object> inputMap) throws Exception {

		final Map<String, Object> objMap = new HashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String sequotation = "select nquotationseqcode,squotationno,ncustomercode,stenderrefno,sschemename,ntotalamount,ntotaltaxamount,ntotalfrightcharges,npaymentmode,spaymentdetails,nbankcode,ncurrencytype,sprojectcode,sremarks1,sremarks2,nstatus,jsondata||json_build_object('nquotationseqcode',nquotationseqcode)::jsonb "
				+ " as jsondata," + "COALESCE(TO_CHAR(dquotationdate,'" + userInfo.getSsitedate()
				+ "'),'') as squotationdate," + "COALESCE(TO_CHAR(dquotationfromdate,'" + userInfo.getSsitedate()
				+ "'),'') as squotationfromdate," + " COALESCE(TO_CHAR(dquotationtodate,'" + userInfo.getSsitedate()
				+ "'),'') as squotationtodate," + " COALESCE(TO_CHAR(dtenderrefdate,'" + userInfo.getSsitedate()
				+ "'),'') as stenderrefdate," + " COALESCE(TO_CHAR(dorderrefdate,'" + userInfo.getSsitedate()
				+ "'),'') as sorderrefdate from invoicequotationheader where nquotationseqcode="
				+ inputMap.get("nquotationseqcode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode();
		final List<InvoiceQuotationHeader> lstQuotation = jdbcTemplate.query(sequotation, new InvoiceQuotationHeader());
		objMap.put("selectedQuotation", lstQuotation);
		final List<InvoiceQuotationProduct> productList = getProductsforQuotationInsert(
				lstQuotation.get(lstQuotation.size() - 1).getSquotationno(), userInfo);
		objMap.put("selectedProduct", productList);
		return new ResponseEntity<>(objMap, HttpStatus.OK); // status // code:200
	}

	/**
	 * This interface declaration is used to ReportGenerate InvoiceQuotation table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> quotationReportGenerate(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		final InvoiceQuotationHeader quotation = mapper.convertValue(inputMap.get("selectedQuotation"),
				InvoiceQuotationHeader.class);

		final List<Map<String, Object>> product = mapper.convertValue(inputMap.get("selectedProduct"),
				new TypeReference<List<Map<String, Object>>>() {
				});

		if (quotation != null && product != null) {

			final String str = "select nquotationitemdetailscode,squotationseqno from quotationitemdetails where squotationseqno='"
					+ quotation.getSquotationno() + "' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
					+ userInfo.getNmastersitecode() + "";
			final List<InvoiceQuotationProduct> productList = (List<InvoiceQuotationProduct>) jdbcTemplate.query(str,
					new InvoiceQuotationProduct());

			final String sequotationno = "select nquotationseqcode,squotationno from invoicequotationheader where nquotationseqcode ="
					+ inputMap.get("nquotationseqcode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
					+ userInfo.getNmastersitecode();
			final List<InvoiceQuotationHeader> lstQuotation = jdbcTemplate.query(sequotationno, new InvoiceQuotationHeader());

			if (productList != null && lstQuotation != null) {
				final Map<String, Object> returnMap = new HashMap<>();
				final String sfileName;
				String sJRXMLname = "";
				final int qType = 1;
				int ncontrolCode = -1;
				String sfilesharedfolder = "";
				String fileuploadpath = "";
				String subReportPath = "";
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
					final String securityKeyDecrypted = projectDAOSupport.decryptPassword("userdigitalsign", "nusercode",
							userInfo.getNusercode(), "ssecuritykey");
					Spassword = securityKeyDecrypted;
				}
				final String struserfile = "Select * from userfile where nusercode=" + userInfo.getNusercode() + ""
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNmastersitecode();

				final List<UserFile> UserFile = (List<UserFile>) jdbcTemplate.query(struserfile, new UserFile());

				final String report = "select ssettingvalue from reportsettings where nreportsettingcode= 22 " + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final List<String> reportPathss = jdbcTemplate.queryForList(report, String.class);
				signpath = reportPathss.get(0);
				if (sJRXMLname != null && !sJRXMLname.equals("")) {

					final Map<String, Object> jasperParameter = new HashMap<>();
					jasperParameter.put("nreportdetailcode", dynamicReport.get("nreportdetailcode"));
					//jasperParameter.put("nreporttypecode", (int) inputMap.get("nreporttypecode"));
					jasperParameter.put("nreporttypecode", 0);
					jasperParameter.put("ssubreportpath", subReportPath + folderName);
					jasperParameter.put("language", userInfo.getSlanguagetypecode());
					jasperParameter.put("simagepath", imagePath + folderName);
					jasperParameter.put("nquotationseqcode", inputMap.get("nquotationseqcode"));
					jasperParameter.put("sprimarykeyname", inputMap.get("nquotationseqcode"));
					jasperParameter.put("squotationno", quotation.getSquotationno());
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
						returnMap.put("rtn", Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
						final String auditAction = "IDS_QUOTATIONREPORT";
						final String comments = commonFunction.getMultilingualMessage("IDS_QUOTATIONNO",
								userInfo.getSlanguagefilename()) + ": " + quotation.getSquotationno() + "; ";

						final Map<String, Object> outputMap = new HashMap<>();
						outputMap.put("stablename", "invoicequotationheader");
						outputMap.put("sprimarykeyvalue", inputMap.get("nquotationseqcode"));
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
	 * This interface declaration is used to get getAmountInWords.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding detail to be
	 *                            deleted in InvoiceQuotation table
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public String getAmountInWords(final Map<String, Object> inputMap, final UserInfo userInfo, final String amountKey,
			final boolean useInvoiceJson) throws Exception {
		final JSONObject jsonObject;

		if (inputMap.containsKey("quotationJson")) {
			jsonObject = new JSONObject(inputMap.get("quotationJson").toString());
		} else {
			jsonObject = new JSONObject(inputMap.get("productJson").toString());
		}
		final String totalTaxAmount;
		if (jsonObject.has("TotalTaxAmount")) {
			totalTaxAmount = jsonObject.getString("TotalTaxAmount");
		} else {
			totalTaxAmount = jsonObject.optString("ntaxamount", "0");
		}
		final Object value = totalTaxAmount;
		final Object CheckWordOrNum = 1;
		final Integer CheckWordOrnum = (Integer) CheckWordOrNum;
		String CurrencyValue = "";
		if (!totalTaxAmount.isEmpty()) {
			if (value instanceof Double || value instanceof Integer && CheckWordOrnum == 1) {
				System.out.println("Numeric value is a Integer");
				final double intValue = ((Number) value).doubleValue();
				System.out.println("Converted value as Integer: " + intValue);
				CurrencyValue = ProjectDAOSupport.convertToWords(intValue, userInfo);
			} else {
				System.out.println("Numeric value is a Float");
				final int amountValue = Math.round((float) Float.parseFloat((String) value));
				final double CheckWordOrNum2 = amountValue;
				System.out.println("Converted value as Integer: " + CheckWordOrNum2);
				CurrencyValue = ProjectDAOSupport.convertToWords(CheckWordOrNum2, userInfo);
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
	 * This interface declaration is used to add a new entry to InvoiceQuotation for
	 * TransactionStatus table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings("unused")
	public ResponseEntity<Object> createTransactionStatusForQuotation(final int seqnoquotation, final UserInfo objUserInfo,
			final String strformat) throws Exception {

		final String str;
		int seqnotransactionstatus = jdbcTemplate.queryForObject(
				"select nsequenceno from " + " seqnoinvoice where stablename='invoicetransactionstatus'",
				Integer.class);
		seqnotransactionstatus++;
		final String s2 = String.format("%05d", seqnoquotation);
		final Year thisYear = Year.now();
		final String needrights = "select nneedrights from userrolefieldcontrol ufc,fieldmaster fm where fm.sfieldname='quotationfield' and fm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and fm.nfieldcode=ufc.nfieldcode and ufc.nformcode=" + objUserInfo.getNformcode()
				+ " and ufc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final int needapprovalrights = jdbcTemplate.queryForObject(needrights, Integer.class);
		final String user = "select * from userfile where nusercode=" + objUserInfo.getNusercode() + "" + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ objUserInfo.getNmastersitecode();

		final List<UserFile> usersign = (List<UserFile>) jdbcTemplate.query(user, new UserFile());
		if (needapprovalrights != Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			str = " INSERT INTO invoicetransactionstatus(ntransactionid, sreferenceid, nreferencetype, sdraftuser, ddraftdate, sapproveduser, dapproveddate, sinvoiceduser,dinvoiceddate,ntransactionstatus,sref1,sref2,sext1,sext2,nstatus,simageid)"
					+ " VALUES(" + seqnotransactionstatus + ",'" + strformat + "',"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'" + objUserInfo.getSusername()
					+ "','" + dateUtilityFunction.getCurrentDateTime(objUserInfo) + "','" + "" + "'," + null + ",'" + ""
					+ "'," + null + "," + +Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'" + ""
					+ "','" + "" + "','" + "" + "','" + "" + "',"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'" 
					+ usersign.get(0).getSsignimgftp() + "')";
		} else {
			str = " INSERT INTO invoicetransactionstatus(ntransactionid, sreferenceid, nreferencetype, sdraftuser, ddraftdate, sapproveduser, dapproveddate, sinvoiceduser,dinvoiceddate,ntransactionstatus,sref1,sref2,sext1,sext2,nstatus,simageid)"
					+ " VALUES(" + seqnotransactionstatus + ",'" + strformat + "',"
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",'" + objUserInfo.getSusername()
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
	 * This interface declaration is used to add a new entry to InvoiceQuotation
	 * table for approve
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> approveQuotation(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final List<String> multilingualIDList = new ArrayList<>();

		final List<InvoiceQuotationHeader> approvelist = (List<InvoiceQuotationHeader>) objmapper
				.convertValue(inputMap.get("selectedQuotation"), new TypeReference<List<InvoiceQuotationHeader>>() {
				});
		final List<InvoiceQuotationHeader> searchlist = (List<InvoiceQuotationHeader>) objmapper
				.convertValue(inputMap.get("searchedQuotation"), new TypeReference<List<InvoiceQuotationHeader>>() {
				});
		if (approvelist != null) {
			for (InvoiceQuotationHeader obj : approvelist) {
				final String Str = " select * from invoicetransactionstatus where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sreferenceid='"
						+ obj.getSquotationno() + "'";
				final List<InvoiceTransactionStatus> invoicetransactionstatus = (List<InvoiceTransactionStatus>) jdbcTemplate
						.query(Str, new InvoiceTransactionStatus());
				if (invoicetransactionstatus.get(0).getNtransactionstatus() == 8) {
					final String str = "update invoicetransactionstatus set ntransactionstatus="
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ",dapproveddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',sapproveduser='"
							+ userInfo.getSusername() + "' where sreferenceid='" + obj.getSquotationno()
							+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					jdbcTemplate.execute(str);
					multilingualIDList.add("IDS_APPROVEQUOTATION");
					auditUtilityFunction.fnInsertAuditAction(getapproveaudit(obj.getSquotationno(), userInfo), 1, null,
							multilingualIDList, userInfo);

				} else {

					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DRAFTONLYAPPROVAL",
							userInfo.getSlanguagefilename()), HttpStatus.BAD_REQUEST);
				}
			}
		}
		if (searchlist != null && searchlist.size() == 1) {
			return getQuotations(userInfo);
		}

		return getQuotation(inputMap,userInfo);
	}

	/**
	 * This interface declaration is used to get Quotations table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings("unused")
	private ResponseEntity<Object> getQuotations(final UserInfo userInfo) throws Exception {

		final int nuser = userInfo.getNuserrole();
		final Map<String, Object> objmap = new HashMap<String, Object>();
		final List<InvoiceQuotationHeader> QuotationList = new ArrayList<InvoiceQuotationHeader>();
		final int needapprovalflow;
		if (userInfo.getNuserrole() != -1) {
			final String approvalFlow = "select nneedapprovalflow from userroleconfig where nuserrolecode ="
					+ userInfo.getNuserrole() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
					+ userInfo.getNmastersitecode();
			needapprovalflow = jdbcTemplate.queryForObject(approvalFlow, Integer.class);
			objmap.put("needapprovalflow", needapprovalflow);
		}

		final String query = "select distinct cms.scolorhexcode as scolorcode,cm.scolorhexcode,ts.stransstatus as stransdisplaystatus,iqh.nquotationseqcode,its.ntransactionstatus,"
				+ "iqh.ncustomercode,iqh.squotationno,iqh.stenderrefno,iqh.sschemename,ins.nschemecode,iqh.ntotalamount,iqh.ntotaltaxamount,ic.ssymbol,"
				+ "iqh.nstatus,iqh.jsondata||json_build_object('nquotationseqcode',nquotationseqcode)::jsonb  as jsondata"
				+ ",iqh.jsondata->>'CustomerName' as CustomerName, COALESCE(TO_CHAR(iqh.dquotationdate,'"
				+ userInfo.getSsitereportdate() + "'),'') as squotationdate,"
				+ "COALESCE(TO_CHAR(iqh.dquotationfromdate,'" + userInfo.getSsitereportdate()
				+ "'),'') as squotationfromdate," + "COALESCE(TO_CHAR(iqh.dquotationtodate,'"
				+ userInfo.getSsitereportdate() + "'),'') as squotationtodate,"
				+ "COALESCE(TO_CHAR(iqh.dtenderrefdate,'" + userInfo.getSsitereportdate() + "'),'') as stenderrefdate,"
				+ "COALESCE(TO_CHAR(iqh.dorderrefdate,'" + userInfo.getSsitereportdate()
				+ "'),'') as sorderrefdate from invoicequotationheader iqh,colormaster cms,invoicecurrencytype ic,"
				+ "invoicetransactionstatus its ,transactionstatus ts,colormaster cm,formwisestatuscolor fsc,invoiceschemes ins where its.sreferenceid=iqh.squotationno"
				+ " and cm.ncolorcode=fsc.ncolorcode and ts.ntranscode=fsc.ntranscode and iqh.ncurrencytype=ic.ncurrencycode "
				+ " and iqh.sschemename=ins.sschemename " + " and fsc.nformcode=" + userInfo.getNformcode()
				+ "and cms.ncolorcode=" + Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()
				+ " and ts.ntranscode=its.ntransactionstatus and nquotationseqcode>0 " + " and its.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iqh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and ins.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and iqh.nsitecode= "
				+ userInfo.getNmastersitecode() + " and ic.nsitecode=" + userInfo.getNmastersitecode()
				+ " and fsc.nsitecode=" + userInfo.getNmastersitecode() + " and ins.nsitecode="
				+ userInfo.getNmastersitecode() + " order by nquotationseqcode ";

		final List<InvoiceQuotationHeader> lstQuotation = jdbcTemplate.query(query, new InvoiceQuotationHeader());

		final String strquery = "select count(*) from invoicequotationheader iqh where iqh.squotationno not in "
				+ "(select UNNEST(STRING_TO_ARRAY(squotationid,',')) from invoiceheader where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") and iqh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iqh.nsitecode= "
				+ userInfo.getNmastersitecode();
		final int count = jdbcTemplate.queryForObject(strquery, Integer.class);
		objmap.put("Quotationcount", count);
		objmap.put("Quotation", lstQuotation);
		if (!lstQuotation.isEmpty()) {
			QuotationList.add(lstQuotation.get(lstQuotation.size() - 1));
			objmap.put("selectedQuotation", QuotationList);
			objmap.put("searchedQuotation", QuotationList);
			List<InvoiceQuotationProduct> productList = getProductsforQuotationInsert(
					lstQuotation.get(lstQuotation.size() - 1).getSquotationno(), userInfo);
			objmap.put("selectedProduct", productList);
			List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(
					lstQuotation.get(lstQuotation.size() - 1).getSquotationno(), userInfo);
			objmap.put("selectedTax", taxList);
		} else {
			objmap.put("selectedQuotation", lstQuotation);
		}

		int norderrefno = 0;
		int norderrefdate = 0;
		int nprojectcode = 0;
		int ntotalfrightchanges = 0;
		int nremarks2 = 0;
		int nremarks1 = 0;
		int nproducttestcode = 0;
		int ntenderrefno = 0;
		int ntenderrefdate = 0;
		final String st = "select * from userrolefieldcontrol where nformcode=" + userInfo.getNformcode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nneedrights=3 ";
		final List<UserRoleFieldControl> listTest = (List<UserRoleFieldControl>) jdbcTemplate.query(st,
				new UserRoleFieldControl());
//		final String sntestgrouptestcode = projectDAOSupport.fndynamiclisttostring(listTest, "getNfieldcode");
		final String sntestgrouptestcode = stringUtilityFunction.fnDynamicListToString(listTest, "getNfieldcode");
		if (listTest.size() == 0) {
			final Map<String, Integer> quotationdetails = new HashMap<>();
			final int defaultValue = +Enumeration.TransactionStatus.NO.gettransactionstatus();
			quotationdetails.put("norderrefno", defaultValue);
			quotationdetails.put("norderrefdate", defaultValue);
			quotationdetails.put("nprojectcode", defaultValue);
			quotationdetails.put("ntotalfrightchanges", defaultValue);
			quotationdetails.put("nremarks2", defaultValue);
			quotationdetails.put("nremarks1", defaultValue);
			quotationdetails.put("nproducttestcode", defaultValue);
			quotationdetails.put("ntenderrefno", defaultValue);
			quotationdetails.put("ntenderrefdate", defaultValue);

		} else {
			final String std = "select * from fieldmaster where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "nfieldcode in ("
					+ sntestgrouptestcode + ")";
			final List<FieldMaster> listTest2 = (List<FieldMaster>) jdbcTemplate.query(std, new FieldMaster());
			final Set<String> fieldNames = listTest2.stream().map(FieldMaster::getSfieldname).collect(Collectors.toSet());
			final int defaultValue = +Enumeration.TransactionStatus.YES.gettransactionstatus();
			final int value = +Enumeration.TransactionStatus.NO.gettransactionstatus();
			norderrefno = fieldNames.contains("sorderrefno") ? defaultValue : value;
			norderrefdate = fieldNames.contains("dorderrefdate") ? defaultValue : value;
			nprojectcode = fieldNames.contains("sprojectcode") ? defaultValue : value;
			ntotalfrightchanges = fieldNames.contains("ntotalfrightcharges") ? defaultValue : value;
			nremarks2 = fieldNames.contains("sremarks2") ? defaultValue : value;
			nremarks1 = fieldNames.contains("sremarks1") ? defaultValue : value;
			nproducttestcode = fieldNames.contains("nproducttestcode") ? defaultValue : value;
			ntenderrefno = fieldNames.contains("stenderrefno") ? defaultValue : value;
			ntenderrefdate = fieldNames.contains("dtenderrefdate") ? defaultValue : value;

		}
		objmap.put("Tenderrefno", ntenderrefno);
		objmap.put("Tenderdate", ntenderrefdate);
		objmap.put("Orderrefno", norderrefno);
		objmap.put("Orderrefdate", norderrefdate);
		objmap.put("Projectcode", nprojectcode);
		objmap.put("Totalfrightchanges", ntotalfrightchanges);
		objmap.put("Remarks2", nremarks2);
		objmap.put("Remarks1", nremarks1);
		objmap.put("Quotation", lstQuotation);
		objmap.put("producttestcode", nproducttestcode);

		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get ProductTestDetails table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getProductTestDetails(final UserInfo userInfo) {

		final Map<String, Object> objmap = new HashMap<>();
		final String strQuery = "select nproducttestcode,sproducttestdetail from producttestdetail where  " + "nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmastersitecode= "
				+ userInfo.getNmastersitecode() + "";
		final List<ProductTestDetail> ProductTest = (List<ProductTestDetail>) jdbcTemplate.query(strQuery,
				new ProductTestDetail());
		objmap.put("ProductTest", ProductTest);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get approve audit table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public List<InvoiceQuotationHeader> getapproveaudit(final Object object, final UserInfo userInfo) {

		final String strQuer = "select its.ntransactionstatus,ts.stransstatus as stransdisplaystatus,iqh.ntotalamount,iqh.ntotaltaxamount,iqh.squotationno,iqh.jsondata->>'CustomerName' as CustomerName,iqh.ncurrencytype,iph.nproducttestcode from  invoicequotationheader iqh, "
				+ "invoicetransactionstatus its,producttestdetail iph,transactionstatus ts where sreferenceid='"
				+ object
				+ "' and  its.sreferenceid=iqh.squotationno  and  ts.ntranscode=its.ntransactionstatus and iph.nproducttestcode=iqh.nproducttestcode and its.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iqh.nsitecode= "
				+ userInfo.getNmastersitecode() + " and its.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iph.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and iph.nmastersitecode="
				+ userInfo.getNmastersitecode() + "" + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<InvoiceQuotationHeader> approve = (List<InvoiceQuotationHeader>) jdbcTemplate.query(strQuer,
				new InvoiceQuotationHeader());
		return approve;
	}

	/**
	 * This interface declaration is used to get SchemesDetails table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            added in InvoiceQuotation table
	 * @return response entity object holding response status and data of added
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSchemesDetails(final UserInfo userInfo) {

		final String strQuery = "select nschemecode,sschemename from invoiceschemes where ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and ((CAST(dfromdate AS DATE) <= CURRENT_DATE) OR dfromdate IS NULL)  and ((CAST(dtodate AS DATE) >= CURRENT_DATE) OR dtodate IS NULL) and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode();
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new InvoiceSchemes()), HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to update entry in InvoiceQuotation table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateUsercode(final Map<String, Object> inputMap, final UserInfo userInfo) {
		final String struser = "update invoicequotationheader set nusercode=" + userInfo.getNusercode()
				+ " where nquotationseqcode=" + inputMap.get("nquotationseqcode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode();
		jdbcTemplate.execute(struser);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get ProjectDetails table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getProjectDetails(final UserInfo userInfo) {
		final String strQuery = "select pj.nprojectmastercode,pj.sprojectcode,pj.sprojectname"
				+ " from projectmaster pj,projectmasterhistory pjh where (CAST(pj.dprojectstartdate AS DATE) >= CURRENT_DATE) and"
				+ " pjh.nprojectmastercode=pj.nprojectmastercode and pjh.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and pj.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pjh.nsitecode="
				+ userInfo.getNmastersitecode() + " union "
				+ "select pj.nprojectmastercode,pj.sprojectcode,pj.sprojectname "
				+ "from projectmaster pj where pj.nprojectmastercode="
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode();
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new ProjectMaster()), HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to update entry in InvoiceQuotation
	 * Product table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> costUpdate(final Map<String, Object> inputMap) {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userinfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference<UserInfo>() {
		});

		final String strproduct = "update invoiceproductmaster set ncost=" + inputMap.get("ncost") + " where nproductcode="
				+ inputMap.get("nproductcode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userinfo.getNmastersitecode();
		jdbcTemplate.execute(strproduct);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to getFilteredRecords in InvoiceQuotation
	 * table.
	 * 
	 * @param objInvoiceQuotation [InvoiceQuotation] object holding details to be
	 *                            updated in InvoiceQuotation table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceQuotation object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings({ "unused"})
	public ResponseEntity<Object> getFilteredRecords(final UserInfo userInfo, String fromDate, String toDate,
			String breadCrumbFrom, String breadCrumbTo) throws Exception {
		
		final Map<String, Object> responseMap = new LinkedHashMap<>();
		List<InvoiceQuotationHeader> quotationList = new ArrayList<>();
		final List<InvoiceQuotationHeader> QuotationList = new ArrayList<InvoiceQuotationHeader>();
		
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

		String baseQuery = "SELECT DISTINCT ON (iqh.nquotationseqcode) "
				+ " cms.scolorhexcode AS scolorcode, cm.scolorhexcode, ts.stransstatus AS stransdisplaystatus, "
				+ " iqh.nquotationseqcode, its.ntransactionstatus, iqh.ncustomercode, iqh.squotationno, iqh.stenderrefno, "
				+ " iqh.sschemename, ins.nschemecode, iqh.ntotalamount, iqh.ntotaltaxamount, ic.ssymbol, iqh.nstatus, "
				+ " iqh.jsondata || json_build_object('nquotationseqcode', iqh.nquotationseqcode)::jsonb AS jsondata, "
				+ " iqh.jsondata->>'CustomerName' AS CustomerName, " + "COALESCE(TO_CHAR(iqh.dquotationdate,'"
				+   userInfo.getSsitereportdate() + "'),'') AS squotationdate, "
				+ " COALESCE(TO_CHAR(iqh.dquotationfromdate,'" + userInfo.getSsitereportdate()
				+ "'),'') AS squotationfromdate, " + "COALESCE(TO_CHAR(iqh.dquotationtodate,'"
				+   userInfo.getSsitereportdate() + "'),'') AS squotationtodate, "
				+ " COALESCE(TO_CHAR(iqh.dtenderrefdate,'" + userInfo.getSsitereportdate() + "'),'') AS stenderrefdate, "
				+ " COALESCE(TO_CHAR(iqh.dorderrefdate,'" + userInfo.getSsitereportdate() + "'),'') AS sorderrefdate "
				+ " FROM invoicequotationheader iqh "
				+ " JOIN invoicetransactionstatus its ON its.sreferenceid = iqh.squotationno "
				+ " JOIN formwisestatuscolor fsc ON fsc.nformcode ="+userInfo.getNformcode()+"  AND fsc.nstatus = "
				+   Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fsc.nsitecode="
				+   userInfo.getNmastersitecode() + ""
				+ " JOIN colormaster cm ON cm.ncolorcode = fsc.ncolorcode AND cm.nstatus = "
				+   Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN transactionstatus ts ON ts.ntranscode = fsc.ntranscode AND ts.nstatus = "
				+   Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and ts.ntranscode = its.ntransactionstatus "
				+ " JOIN invoicecurrencytype ic ON iqh.ncurrencytype = ic.ncurrencycode AND ic.nstatus = "
				+   Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.nsitecode="
				+   userInfo.getNmastersitecode()
				+ " JOIN invoiceschemes ins ON iqh.sschemename = ins.sschemename AND ins.ntransactionstatus in ("
				+   Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+","+Enumeration.TransactionStatus.RETIRED.gettransactionstatus()+") and ins.nsitecode="
				+   userInfo.getNmastersitecode() + "" + " JOIN colormaster cms ON cms.ncolorcode = "+Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()+""
				+ " WHERE iqh.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and iqh.nsitecode= " + userInfo.getNmastersitecode()+""
				+ " and iqh.dquotationdate between '"+formattedFromDate+"' and '"+formattedToDate+"'"
				+ " ORDER BY iqh.nquotationseqcode desc";
		
		quotationList = jdbcTemplate.query(baseQuery, new InvoiceQuotationHeader());

     	responseMap.put("Quotation", quotationList);
		responseMap.put("Filtered", isFiltered);
		responseMap.put("fromDate", FromDate);
		responseMap.put("toDate", ToDate);
		

		if (!quotationList.isEmpty()) {
			//final InvoiceQuotationHeader selected = quotationList.get(quotationList.size() - 1);
			QuotationList.add(quotationList.get(0));
			responseMap.put("selectedQuotation", QuotationList);

			final List<InvoiceQuotationProduct> productList = getProductsforQuotationInsert(
					quotationList.get(0).getSquotationno(), userInfo);
			responseMap.put("selectedProduct", productList);
			final List<InvoiceTaxCalculation> taxList = getProductsTaxCalculation(
					quotationList.get(quotationList.size() - 1).getSquotationno(), userInfo);
			responseMap.put("selectedTax", taxList);
		}

		return new ResponseEntity<>(responseMap, HttpStatus.OK);
	}

}
