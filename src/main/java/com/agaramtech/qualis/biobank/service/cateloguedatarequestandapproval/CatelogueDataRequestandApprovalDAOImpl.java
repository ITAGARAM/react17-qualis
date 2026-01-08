package com.agaramtech.qualis.biobank.service.cateloguedatarequestandapproval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.biobank.model.BiodataAccessRequest;
import com.agaramtech.qualis.biobank.model.BiodataAccessRequestDetails;
import com.agaramtech.qualis.biobank.model.ThirdParty;
import com.agaramtech.qualis.configuration.model.SiteHierarchyConfigDetails;
import com.agaramtech.qualis.credential.model.UserRoleConfig;
import com.agaramtech.qualis.credential.model.Users;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.project.model.BioProject;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "biodataaccessrequest" table
 * by implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class CatelogueDataRequestandApprovalDAOImpl implements CatelogueDataRequestandApprovalDAO {

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final AuditUtilityFunction auditUtilityFunction;
	private final EmailDAOSupport emailDAOSupport;


	/**
	 * This method is used to fetch BioData Access Request records for a given user
	 * and site. It retrieves parent BioData Access requests along with their
	 * associated child records, filtered based on provided request code, date
	 * range, and transaction status.
	 * 
	 * If the fromDate and toDate are not provided in the input map, default date
	 * values are retrieved from control properties. It also prepares filter status
	 * options to support front-end selection.
	 *
	 * @param inputMap                  [Map<String, Object>] containing request
	 *                                  parameters such as fromDate, toDate, and
	 *                                  ntransCode
	 * @param nbiodataAccessRequestCode [int] primary key of BioData Access Request
	 *                                  to be retrieved; if -1, all records will be
	 *                                  returned
	 * @param userInfo                  [UserInfo] holding logged-in user details
	 *                                  including site, user, and role information
	 * @return ResponseEntity<Object> containing a map with:
	 *         <ul>
	 *         <li>lstBioDataaccess – list of BioData Access request records</li>
	 *         <li>selectedBioDataaccess – selected BioData request (if code is
	 *         provided)</li>
	 *         <li>lstChildBioDataaccess – child records for the selected BioData
	 *         request</li>
	 *         <li>lstFilterStatus – list of available transaction filter
	 *         statuses</li>
	 *         <li>selectedFilterStatus – currently selected transaction filter</li>
	 *         <li>fromDate, toDate – effective date range applied</li>
	 *         </ul>
	 * @throws Exception if any error occurs while fetching data from the database
	 *                   or DAO layer
	 */
	public ResponseEntity<Object> getBioDataRequest(Map<String, Object> inputMap, final int nbiodataAccessRequestCode,
			UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();

		String fromDate = (String) inputMap.get("fromDate");
		String toDate = (String) inputMap.get("toDate");
		int ntransCode = inputMap.containsKey("ntransCode") ? (int) inputMap.get("ntransCode") : -1;
		if (!inputMap.containsKey("fromDate")) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					(String) inputMap.get("currentdate"), "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");

			inputMap.put("fromDate", fromDate);
			inputMap.put("toDate", toDate);

			outputMap.put("fromDate", mapObject.get("FromDateWOUTC"));
			outputMap.put("toDate", mapObject.get("ToDateWOUTC"));
			outputMap.put("realFromDate", mapObject.get("FromDateWOUTC"));
			outputMap.put("realToDate", mapObject.get("ToDateWOUTC"));
		}
		List<TransactionStatus> getFilterStatus = getFilterStatus(userInfo);
		List<Map<String, Object>> lstFilterStatus = new ArrayList<>();

		for (TransactionStatus status : getFilterStatus) {
			Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", status.getStransdisplaystatus());
			mapStatus.put("value", status.getNtranscode());
			mapStatus.put("item", status);
			lstFilterStatus.add(mapStatus);
		}
		if (!inputMap.containsKey("ntransCode")) {
			ntransCode = getFilterStatus.get(0).getNtranscode();
		}
		final String strConditionTransCode = ntransCode == 0 ? "" : " and dt.ntransactionstatus=" + ntransCode + " ";
		final short transCode = (short) ntransCode;
		Map<String, Object> selectedFilterStatus = lstFilterStatus.stream()
				.filter(x -> (short) x.get("value") == transCode).collect(Collectors.toList()).get(0);

		outputMap.put("lstFilterStatus", lstFilterStatus);
		outputMap.put("selectedFilterStatus", selectedFilterStatus);
		outputMap.put("realSelectedFilterStatus", selectedFilterStatus);

		var strFilterQry = "select * from userroleconfig where nuserrolecode=" + userInfo.getNuserrole()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  order by 1 desc";

		final var objRoleBased = (UserRoleConfig) jdbcUtilityTemplateFunction.queryForObject(strFilterQry,
				UserRoleConfig.class, jdbcTemplate);

		String strQuery = "";
		if(objRoleBased != null) {
			
			// BGSI-287 Commented by Vishakh to show all lists to all users in the site
//			if (objRoleBased.getNneedthirdpartyflow() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
//
//				strQuery = "SELECT dt.nbiodataaccessrequestcode, dt.sformnumber, dt.ntransactionstatus, "
//						+ "dt.nthirdpartycode, dt.ntzrequestcreateddate, dt.drequestcreateddate, "
//						+ "to_char(dt.drequestcreateddate, '" + userInfo.getSsitedate() + "') AS srequestdate, "
//						+ "s.ssitename sreceiversitename, " + "COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
//						+ userInfo.getSlanguagetypecode() + "', "
//						+ "ts.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus, "
//						+ "cm.scolorhexcode, dt.nsitecode, dt.nstatus, tt.nusercode, tt.nuserrolecode "
//						+ "FROM biodataaccessrequest dt "
//						+ "JOIN thirdpartyusermapping tt ON dt.nthirdpartycode = tt.nthirdpartycode " + "AND tt.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//						+ "JOIN transactionstatus ts ON dt.ntransactionstatus = ts.ntranscode " + "AND ts.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//						+ "JOIN site s ON dt.nsitecode = s.nsitecode " + "AND s.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//						+ "JOIN formwisestatuscolor fwsc ON fwsc.ntranscode = ts.ntranscode " + "AND fwsc.nformcode = "
//						+ userInfo.getNformcode() + " AND fwsc.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//						+ "JOIN colormaster cm ON fwsc.ncolorcode = cm.ncolorcode " + "AND cm.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
////						+ "   AND tt.nusercode="
////						+ userInfo.getNusercode() 
//						+ " WHERE  dt.nbiodataaccessrequestcode = any ( "
//						+ "SELECT MAX(bh1.nbiodataaccessrequestcode) FROM biodataaccessrequesthistory bh1 "
//						+ "Group by bh1.nbiodataaccessrequestcode ) " + "AND dt.nsitecode = " + userInfo.getNtranssitecode()
//						+ " AND dt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//						+ "AND dt.nthirdpartycode IN ( " + "    SELECT tp.nthirdpartycode "
//						+ "    FROM thirdpartyusermapping tm "
//						+ "    JOIN thirdparty tp ON tm.nthirdpartycode = tp.nthirdpartycode " + "    WHERE tm.nusercode = "
//						+ userInfo.getNusercode() + " " + "    AND tm.nuserrolecode = " + userInfo.getNuserrole() + " "
//						+ "    AND tm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//						+ "    AND tp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
//						// added by sujatha ATE_274 BGSI-148 for getting data's based on NGS = 4
//						+ "    AND tp.nisngs = "+ Enumeration.TransactionStatus.NO.gettransactionstatus()+ " " + ") "
//						+ "AND dt.drequestcreateddate BETWEEN '" + fromDate + "' AND '" + toDate + "' "
//						+ strConditionTransCode + " ORDER BY dt.nbiodataaccessrequestcode DESC";
//
//			} else {

				strQuery = "SELECT dt.nbiodataaccessrequestcode, dt.sformnumber, dt.ntransactionstatus, "
						+ "dt.nthirdpartycode, dt.ntzrequestcreateddate, dt.drequestcreateddate, "
						+ "to_char(dt.drequestcreateddate, '" + userInfo.getSsitedate() + "') AS srequestdate, "
						+ "s.ssitename sreceiversitename, " + "COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "', "
						+ "ts.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus, "
						+ "cm.scolorhexcode, dt.nsitecode, dt.nstatus "
						+ "FROM biodataaccessrequest dt "
						+ "JOIN transactionstatus ts ON dt.ntransactionstatus = ts.ntranscode " + "AND ts.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "JOIN site s ON dt.nsitecode = s.nsitecode " + "AND s.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "JOIN formwisestatuscolor fwsc ON fwsc.ntranscode = ts.ntranscode " + "AND fwsc.nformcode = "
						+ userInfo.getNformcode() + " AND fwsc.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "JOIN colormaster cm ON fwsc.ncolorcode = cm.ncolorcode " + "AND cm.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " WHERE  dt.nbiodataaccessrequestcode = any ( "
						+ "SELECT MAX(bh1.nbiodataaccessrequestcode) FROM biodataaccessrequesthistory bh1 "
						+ "Group by bh1.nbiodataaccessrequestcode ) " + "AND dt.nsitecode = " + userInfo.getNtranssitecode()
						+ " AND dt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
						+ "AND dt.drequestcreateddate BETWEEN '" + fromDate + "' AND '" + toDate + "' "
						+ strConditionTransCode + " ORDER BY dt.nbiodataaccessrequestcode DESC";

//			}
		}
		

		List<BiodataAccessRequest> lstBioDataaccess = strQuery.isEmpty() ? new ArrayList<>() :	objRoleBased != null ? 
				jdbcTemplate.query(strQuery, new BiodataAccessRequest()) : new ArrayList<>();

		if (!lstBioDataaccess.isEmpty()) {
			outputMap.put("lstBioDataaccess", lstBioDataaccess);

			List<BiodataAccessRequest> lstObjBioDataaccess;
			if (nbiodataAccessRequestCode == -1) {
				lstObjBioDataaccess = lstBioDataaccess;
			} else {
				lstObjBioDataaccess = lstBioDataaccess.stream()
						.filter(x -> x.getNbiodataaccessrequestcode() == nbiodataAccessRequestCode)
						.collect(Collectors.toList());
			}

			if (!lstObjBioDataaccess.isEmpty()) {
				outputMap.put("selectedBioDataaccess", lstObjBioDataaccess.get(0));
				List<Map<String, Object>> lstChildBioDataaccess = getChildInitialGet(
						lstObjBioDataaccess.get(0).getNbiodataaccessrequestcode(), userInfo);
				outputMap.put("lstChildBioDataaccess", lstChildBioDataaccess);
			} else {
				outputMap.put("selectedBioDataaccess", null);
				outputMap.put("lstChildBioDataaccess", null);
			}

			if (inputMap.containsKey(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())) {
				outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						inputMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
			}
		} else {
			outputMap.put("lstBioDataaccess", null);
			outputMap.put("selectedBioDataaccess", null);
			outputMap.put("lstChildBioDataaccess", null);
		}

		outputMap.put("nprimaryKeyBioDataAccess",
				inputMap.containsKey("nprimaryKeyBioDataAccess") ? inputMap.get("nprimaryKeyBioDataAccess") : -1);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to fetch child BioData Access Request details linked to a
	 * specific parent BioData Access Request.
	 * <p>
	 * The query retrieves the child request detail code, project title, product
	 * name, and localized transaction status for the given parent request. Only
	 * active records are considered, and the results are sorted in descending order
	 * of the request detail code.
	 * </p>
	 *
	 * @param nbiodataaccessrequestcode [int] the primary key of the parent BioData
	 *                                  Access Request whose child details need to
	 *                                  be retrieved
	 * @param userInfo                  [UserInfo] object containing logged-in user
	 *                                  details such as site code and language type
	 *                                  used for localization
	 * @return List<Map<String, Object>> list of child BioData Access Request
	 *         details, each record containing:
	 *         <ul>
	 *         <li><b>nbiodataaccessrequestdetailscode</b> – unique identifier of
	 *         the child record</li>
	 *         <li><b>sprojecttitle</b> – title of the associated project</li>
	 *         <li><b>sproductname</b> – name of the associated product</li>
	 *         <li><b>stransdisplaystatus</b> – localized display status of the
	 *         transaction</li>
	 *         </ul>
	 */
	public List<Map<String, Object>> getChildInitialGet(int nbiodataaccessrequestcode, UserInfo userInfo) {
		// modified sujatha ATE_274 query by concating productname with productcatname  by joining productcategory table
		final String strDataAccessQuery = "SELECT row_number() over(order by bard.nbiodataaccessrequestdetailscode desc) as "
				+ "	 nserialno, bard.nbiodataaccessrequestdetailscode, " + "       bp.sprojecttitle, "
				+ "   CONCAT(p.sproductname, ' (', pc.sproductcatname, ')') AS sproductname, " + "       COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', "
				+ "  ts.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus "
				+ "FROM biodataaccessrequestdetails bard " + "JOIN biodataaccessrequest bar "
				+ "     ON bard.nbiodataaccessrequestcode = bar.nbiodataaccessrequestcode " + " AND bard.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN bioproject bp ON bard.nbioprojectcode = bp.nbioprojectcode "
				+ "JOIN product p ON bard.nproductcode = p.nproductcode AND p.nsitecode = "
				+ userInfo.getNmastersitecode() + " AND p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "JOIN transactionstatus ts "
				+ "     ON bar.ntransactionstatus = ts.ntranscode AND ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " JOIN productcategory pc on pc.nproductcatcode=p.nproductcatcode "
				+ " AND pc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND pc.nsitecode="+userInfo.getNmastersitecode()
				+ " WHERE bar.nbiodataaccessrequestcode = " + nbiodataaccessrequestcode + " "
				+ " ORDER by nserialno desc";
		List<Map<String, Object>> lstChildGet = jdbcTemplate.queryForList(strDataAccessQuery);

		return lstChildGet;
	}

	/**
	 * This method retrieves the list of available sample types (products) for a
	 * given BioProject.
	 * <p>
	 * The method performs the following:
	 * <ul>
	 * <li>Validates the BioProject code from the input map.</li>
	 * <li>Fetches all distinct products (sample types) mapped to the specified
	 * project.</li>
	 * <li>Fetches all products already used in active BioData Access Requests by
	 * the logged-in user.</li>
	 * <li>Filters out the used products to return only available ones.</li>
	 * </ul>
	 * </p>
	 *
	 * @param inputMap [Map&lt;String, Object&gt;] containing request parameters,
	 *                 specifically <b>nbioprojectcode</b> which identifies the
	 *                 BioProject.
	 * @param userInfo [UserInfo] object holding details of the logged-in user,
	 *                 including user code and role used for filtering requests.
	 * @return ResponseEntity&lt;Object&gt; containing:
	 *         <ul>
	 *         <li><b>productList</b> – list of available (unused) sample types for
	 *         the project in {label, value} format.</li>
	 *         <li><b>usedProductList</b> – list of product codes already used in
	 *         requests.</li>
	 *         </ul>
	 *         If the BioProject code is invalid, a <b>Bad Request</b> response is
	 *         returned.
	 */
	@Override
	public ResponseEntity<Object> getBioDataRequestSampleTypeData(Map<String, Object> inputMap, UserInfo userInfo) {
		Integer nbioprojectcode = (Integer) inputMap.get("nbioprojectcode");
		final Map<String, Object> outputMap = new HashMap<>();

		if (nbioprojectcode == null || nbioprojectcode <= 0) {
			return ResponseEntity.badRequest().body("Invalid BioProject Code.");
		}
		
		String siteCodeQuery = " SELECT bp.sprojecttitle, pm.nbioprojectcode, pm.nnodesitecode, "
				+ "       pm.nsitehierarchyconfigcode, case when shcd.schildsitecode is null or shcd.schildsitecode = ''"
				+ " then '"+ userInfo.getNtranssitecode()+"' else concat(shcd.schildsitecode, ', "
				+ userInfo.getNtranssitecode()+"') end as schildsitecode " + " FROM projectsitemapping pm "
				+ " JOIN bioproject bp ON pm.nbioprojectcode = bp.nbioprojectcode "
				+ " JOIN sitehierarchyconfigdetails shcd ON pm.nnodesitecode = shcd.nnodesitecode "
				+ " AND pm.nsitehierarchyconfigcode = shcd.nsitehierarchyconfigcode " + " WHERE pm.nnodesitecode = "
				+ userInfo.getNtranssitecode() + " " + " AND pm.nbioprojectcode = " + nbioprojectcode + " "
				+ " AND pm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND bp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND shcd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " GROUP BY bp.sprojecttitle, pm.nbioprojectcode, pm.nnodesitecode, pm.nsitehierarchyconfigcode, shcd.schildsitecode";

		List<SiteHierarchyConfigDetails> siteDetailsList = jdbcTemplate.query(siteCodeQuery,
				new SiteHierarchyConfigDetails());

		// Extract schildsitecode list
		List<String> schildSiteCodes = siteDetailsList.stream().map(SiteHierarchyConfigDetails::getSchildsitecode)
				.filter(Objects::nonNull).collect(Collectors.toList());

		if (schildSiteCodes.isEmpty()) {
			outputMap.put("productList", Collections.emptyList());
			outputMap.put("usedProductList", Collections.emptyList());
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}

		// Convert site codes into comma-separated values
		String siteCodeCsv = schildSiteCodes.stream().collect(Collectors.joining(",")); // numeric → no quotes
		
		//modified by sujatha ATE_274 for getting the sample type along with its sample category name
		String productQuery = "SELECT DISTINCT p.nproductcode, CONCAT(p.sproductname, ' (', pc.sproductcatname, ')') AS sproductname " 
				+ "FROM samplestoragetransaction sst "+ "JOIN product p ON sst.nproductcode = p.nproductcode " 
				+ "JOIN productcategory pc on pc.nproductcatcode=p.nproductcatcode "
				+ "WHERE sst.nprojecttypecode = "+ nbioprojectcode + " " + "AND p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND sst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND pc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND sst.nsitecode in ("+ siteCodeCsv+ ")";

		final List<Product> productListName = jdbcTemplate.query(productQuery, new Product());
		List<Map<String, Object>> productList = productListName.stream().map(lst -> {
			Map<String, Object> map = new HashMap<>();
			map.put("label", lst.getSproductname());
			map.put("value", lst.getNproductcode());
			return map;
		}).collect(Collectors.toList());
		String usedProductQuery = "SELECT DISTINCT brd.nproductcode " + "FROM biodataaccessrequest br "
				+ "JOIN biodataaccessrequestdetails brd ON br.nbiodataaccessrequestcode = brd.nbiodataaccessrequestcode "
				+ "JOIN thirdpartyusermapping tpup ON tpup.nthirdpartycode = br.nthirdpartycode "
				+ "JOIN thirdparty tp ON tp.nthirdpartycode = tpup.nthirdpartycode "
				+ "WHERE br.ntransactionstatus IN (" + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.REQUESTED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ") " + "AND br.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND brd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND tpup.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND tp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND tpup.nusercode = "
				+ userInfo.getNusercode() + " " + "AND tpup.nuserrolecode = " + userInfo.getNuserrole() + " "
				+ "AND brd.nbioprojectcode = " + nbioprojectcode;

		List<Integer> usedProductList = jdbcTemplate.query(usedProductQuery, (rs, rowNum) -> rs.getInt("nproductcode"));

		List<Map<String, Object>> availableProducts = productList.stream()
				.filter(p -> !usedProductList.contains((Integer) p.get("value"))).collect(Collectors.toList());

		outputMap.put("productList", availableProducts);
		outputMap.put("usedProductList", usedProductList);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * Creates a new BioData access request along with its details and history
	 * records.
	 * 
	 * Steps performed: 1. Reads input values (project code, sample types, request
	 * date). 2. Generates a new sequence number for parent request
	 * (`biodataaccessrequest`). 3. Inserts parent record into
	 * `biodataaccessrequest`. 4. Inserts one or more child records into
	 * `biodataaccessrequestdetails` for each sample type. 5. Inserts a history
	 * record into `biodataaccessrequesthistory`. 6. Updates sequence tables for
	 * parent, child, and history. 7. Performs audit logging for the created
	 * request.
	 * 
	 * @param inputMap Map<String, Object> containing input params: - bioprojectcode
	 *                 (int): project code - sampleTypes (List<Integer>): list of
	 *                 sample type product codes - srequestdate (String): requested
	 *                 date in ISO format (nullable)
	 * @param userInfo UserInfo object with logged-in user details
	 * @return ResponseEntity<Object> with the newly created BioData request
	 * @throws Exception if any database or business logic error occurs
	 */
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> createBioDataRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		int nbioProjectCode = (int) inputMap.get("bioprojectcode");
		// commented and added by sujatha ATE_274 DEMO_SRC For issue thrown class cast exception
//		List<Integer> sampleTypes = (List<Integer>) inputMap.get("sampleTypes");
		List<Integer> sampleTypes = ((List<?>) inputMap.get("sampleTypes")).stream()
		        .map(obj -> Integer.parseInt(String.valueOf(obj))) 
		        .collect(Collectors.toList());
		String srequestdate = (String) inputMap.get("srequestdate");

		// Added by Vishakh (BGSI-287) for third party institution user issue
		final List<ThirdParty> thirdPartyUser = lstThirdParty(userInfo);
		if( thirdPartyUser.size()> 0 &&  thirdPartyUser.get(0).getNisngs()==Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			final String stransferDate;
			if (srequestdate != null && !srequestdate.isEmpty()) {
				stransferDate = "'" + srequestdate.replace("T", " ").replace("Z", "") + "'";
			} else {
				stransferDate = "'" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())
						+ "'";
			}
			
			//added by sujatha ATE_274 DEMO_SRC for checking sample type already Present in DB
			// start
			// Added ntransactionstatus in WHERE clause by Gowtham on 11 nov 2025 - jira.id:BGSI-183
			String samtype = sampleTypes.toString().replace("[", "(").replace("]", ")");
			String strCheck="SELECT bda.nproductcode FROM biodataaccessrequestdetails bda "
						+ " JOIN biodataaccessrequest bdr ON bdr.nbiodataaccessrequestcode = bda.nbiodataaccessrequestcode "
						+ " JOIN thirdpartyusermapping tm ON tm.nthirdpartycode = bdr.nthirdpartycode"
						+ " WHERE bda.nproductcode IN "+ samtype
						+ " AND bda.nsitecode ="+userInfo.getNtranssitecode()
						+ " AND bda.nbioprojectcode ="+ nbioProjectCode
						+ " AND bda.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " AND bdr.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " AND bdr.nsitecode="+userInfo.getNtranssitecode()
						+ " AND tm.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " AND tm.nsitecode="+userInfo.getNmastersitecode()
						+ " AND tm.nuserrolecode="+userInfo.getNuserrole()
						+ " AND tm.nusercode="+userInfo.getNusercode()
						+ " AND bdr.ntransactionstatus NOT IN ("
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + "," 
						+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + "," 
						+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
						+ " ) group by bda.nproductcode";
			List<Integer> dbCheck = jdbcTemplate.queryForList(strCheck, Integer.class);
			
			// Filter input sampleTypes to find new ones
			List<Integer> newSampleTypes = sampleTypes.stream()
			        .filter(type -> !dbCheck.contains(type))
			        .collect(Collectors.toList());
			
			// If all selected types already exist in DB
			if (newSampleTypes.isEmpty()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALREADYEXISTS",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
			//end
			int seqNoBioData = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biodataaccessrequest' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioData++;
	
			String sql = "SELECT tp.nthirdpartycode " + " FROM thirdpartyusermapping tm "
					+ " JOIN thirdparty tp ON tm.nthirdpartycode = tp.nthirdpartycode " + " WHERE tm.nusercode = "
					+ userInfo.getNusercode() + " AND tm.nuserrolecode = " + userInfo.getNuserrole() + " AND tm.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND tp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
			List<ThirdParty> thirdPartyList = jdbcTemplate.query(sql, new ThirdParty());
			int thirdPartyCode = thirdPartyList.stream().map(ThirdParty::getNthirdpartycode).findFirst().orElse(-1);
	
			final String strformat = projectDAOSupport.getSeqfnFormat("biodataaccessrequest", "seqnoformatgeneratorbiobank",
					0, 0, userInfo);
	
			String strInsertParent = "INSERT INTO biodataaccessrequest ("
					+ "nbiodataaccessrequestcode, nthirdpartycode, sformnumber, ntransactionstatus, drequestcreateddate,"
					+ "ntzrequestcreateddate, noffsetdrequestcreateddate, drequesteddate, ntzrequesteddate, noffsetdrequesteddate,"
					+ "nsitecode, nstatus) VALUES ( " + seqNoBioData + "," + thirdPartyCode + ",'" + strformat + "',"
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + stransferDate + ", "
					+ userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", NULL,NULL,NULL,"
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ");";
	
			String strSeqNoUpdateParent = "update seqnobiobankmanagement set nsequenceno=" + seqNoBioData
					+ " where stablename='biodataaccessrequest' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
	
			jdbcTemplate.execute(strInsertParent + strSeqNoUpdateParent);
	
			inputMap.put("nprimaryKeyBiocatalogue", seqNoBioData);
			int seqNoBioDataaccess = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biodataaccessrequestdetails' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioDataaccess++;
			
			// modified newSampleTypes by sujatha ATE_274 changed for insert the entire sample to only the sample that are filtered(which is not in db)
			if (newSampleTypes != null && !newSampleTypes.isEmpty()) {
				StringBuilder insertQueries = new StringBuilder();
	
				for (Object typeObj : newSampleTypes) {
					int sampleType = Integer.parseInt(String.valueOf(typeObj));
	
					String str = "INSERT INTO biodataaccessrequestdetails ("
							+ "nbiodataaccessrequestdetailscode, nbiodataaccessrequestcode, nbioprojectcode, nproductcode,"
							+ "nuserrolecode, nusercode, ndeputyusercode, ndeputyuserrolecode, dtransactiondate,"
							+ "noffsetdtransactiondate, nsitecode, nstatus) " + "VALUES(" + seqNoBioDataaccess + ","
							+ seqNoBioData + "," + nbioProjectCode + "," + sampleType + "," + userInfo.getNuserrole() + ","
							+ userInfo.getNusercode() + "," + userInfo.getNdeputyusercode() + ","
							+ userInfo.getNdeputyuserrole() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
							+ userInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
	
					insertQueries.append(str).append("\n");
					seqNoBioDataaccess++;
				}
	
				String strSeqNoUpdateChild = "update seqnobiobankmanagement set nsequenceno=" + (seqNoBioDataaccess - 1)
						+ " where stablename='biodataaccessrequestdetails' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
	
				insertQueries.append(strSeqNoUpdateChild);
	
				jdbcTemplate.execute(insertQueries.toString());
			}
	
			inputMap.put("nprimaryKeyBiocataloguedata", seqNoBioDataaccess);
			int seqNoBioDatahistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biodataaccessrequesthistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioDatahistory++;
	
			String strHistory = "INSERT INTO biodataaccessrequesthistory ("
					+ "nbiodataaccessrequesthistorycode, nbiodataaccessrequestcode, ntransactionstatus, nuserrolecode,"
					+ "nusercode, ndeputyusercode, ndeputyuserrolecode, dtransactiondate, noffsetdtransactiondate, nsitecode, nstatus) "
					+ "VALUES (" + seqNoBioDatahistory + "," + seqNoBioData + ","
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + userInfo.getNuserrole() + ","
					+ userInfo.getNusercode() + "," + userInfo.getNdeputyusercode() + "," + userInfo.getNdeputyuserrole()
					+ ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
					+ userInfo.getNtranssitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ");";
	
			String strSeqNoUpdateHistory = "update seqnobiobankmanagement set nsequenceno=" + seqNoBioDatahistory
					+ " where stablename='biodataaccessrequesthistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
	
			jdbcTemplate.execute(strHistory + strSeqNoUpdateHistory);
	
			inputMap.put("nprimaryKeyBiocataloguedatahistory", seqNoBioDatahistory);
	
			final List<Object> lstAuditAfter = new ArrayList<>();
			final List<String> multilingualIDList = new ArrayList<>();
			final String strAuditQry = auditQuery(seqNoBioData, -1, userInfo);
			List<BiodataAccessRequest> lstAuditBioDataAfter = jdbcTemplate.query(strAuditQry, new BiodataAccessRequest());
			lstAuditAfter.addAll(lstAuditBioDataAfter);
			lstAuditBioDataAfter.forEach(x -> multilingualIDList.add("IDS_CREATETHIRDPARTYECATALOGREQUEST"));
			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);
			return getBioDataRequest(inputMap, seqNoBioData, userInfo);
		}
		else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THIRDPARTYUSERONLYHAVERIGHTS",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	/**
	 * Builds the SQL query used to fetch audit data for a given BioData Access
	 * Request.
	 *
	 * This query joins multiple related tables (biodataaccessrequest, details,
	 * project, product, transactionstatus) to collect all necessary fields for
	 * audit logging.
	 *
	 * @param nbioDataAccessRequestCode        the parent BioData Access Request
	 *                                         code
	 * @param nbioDataAccessRequestDetailsCode the child detail code (if -1, fetches
	 *                                         all details)
	 * @param userInfo                         UserInfo object containing site,
	 *                                         language, and timezone details
	 * @return SQL query string for retrieving audit records
	 * @throws Exception if any error occurs while building the query
	 */
	public String auditQuery(final int nbioDataAccessRequestCode, final int nbioDataAccessRequestDetailsCode,
			final UserInfo userInfo) throws Exception {
		String strConcat = "";
		if (nbioDataAccessRequestDetailsCode != -1) {
			strConcat = " AND bard.nbiodataaccessrequestdetailscode=" + nbioDataAccessRequestDetailsCode;
		}

		final String strAuditQry = "SELECT bar.nbiodataaccessrequestcode, bar.sformnumber, "
				+ "       bar.ntransactionstatus,  " + "       to_char(bar.drequestcreateddate, '"
				+ userInfo.getSsitedate() + "') AS srequestdate, " + "       bp.nbioprojectcode, bp.sprojecttitle, "
				+ "       bard.nproductcode, p.sproductname, "
				+ "       coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ "                 ts.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus, "
				+ "       bar.nsitecode, bar.nstatus " + "FROM biodataaccessrequest bar "
				+ "JOIN biodataaccessrequestdetails bard ON bard.nbiodataaccessrequestcode = bar.nbiodataaccessrequestcode "
				+ "  AND bard.nsitecode=" + userInfo.getNtranssitecode() + "  AND bard.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN bioproject bp ON bp.nbioprojectcode = bard.nbioprojectcode " + "  AND bp.nsitecode="
				+ userInfo.getNmastersitecode() + "  AND bp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN product p ON p.nproductcode = bard.nproductcode " + "  AND p.nsitecode="
				+ userInfo.getNmastersitecode() + "  AND p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN transactionstatus ts ON ts.ntranscode = bar.ntransactionstatus " + "  AND ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "WHERE bar.nbiodataaccessrequestcode=" + nbioDataAccessRequestCode + " " + "  AND bar.nsitecode="
				+ userInfo.getNtranssitecode() + "  AND bar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + strConcat
				+ " ORDER BY bar.nbiodataaccessrequestcode";

		return strAuditQry;
	}

	/**
	 * Retrieves the list of filterable transaction statuses for a given user.
	 *
	 * This method joins the {@code transactionstatus} and
	 * {@code approvalstatusconfig} tables to get only the statuses configured for
	 * the user's form and site. It also fetches the localized display name (based
	 * on user's language).
	 *
	 * @param userInfo UserInfo object containing form code, site code, and language
	 *                 type
	 * @return List of {@link TransactionStatus} objects matching filter criteria
	 * @throws Exception if query execution fails
	 */
	public List<TransactionStatus> getFilterStatus(UserInfo userInfo) throws Exception {

		final String strFilterStatus = "select ts.ntranscode, ts.stransstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, ts.nstatus from transactionstatus ts,"
				+ " approvalstatusconfig ascf where " + " ts.ntranscode=ascf.ntranscode and ascf.nformcode="
				+ userInfo.getNformcode() + " and ascf.nstatusfunctioncode="
				+ Enumeration.ApprovalStatusFunction.FILTERSTATUS.getNstatustype() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ascf.nsitecode="
				+ userInfo.getNmastersitecode() + " order by ascf.nsorter";
		return jdbcTemplate.query(strFilterStatus, new TransactionStatus());
	}

	/**
	 * Retrieves a hierarchical list of BioProjects and their associated sample
	 * types.
	 *
	 * <p>
	 * Rules applied:
	 * <ul>
	 * <li>If a project has no samples → show the project node with no
	 * children.</li>
	 * <li>If a project has exactly one sample → show the project node with its
	 * single child, unless that sample is already added.</li>
	 * <li>If a project has multiple samples → show only unused samples as children.
	 * If all are used, the project is skipped.</li>
	 * </ul>
	 *
	 * @param userInfo {@link UserInfo} containing site, role, and user details
	 * @return {@link ResponseEntity} containing a structured project list with
	 *         sample type children
	 */
	@Override
	public ResponseEntity<Object> getBioProject(UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();


		// Added by Vishakh (BGSI-287) for third party institution user issue
		final List<ThirdParty> thirdPartyUser = lstThirdParty(userInfo);
		if( thirdPartyUser.size()> 0 &&  thirdPartyUser.get(0).getNisngs()==Enumeration.TransactionStatus.NO.gettransactionstatus()) {
		// Step 1: Get all active projects for this site/user
		String strQry = "SELECT bp.sprojecttitle, bp.nbioprojectcode, case when shcd.schildsitecode is null or"
				+ " shcd.schildsitecode = '' then '"+ userInfo.getNtranssitecode()+"' else"
				+ " concat(shcd.schildsitecode, ', "+ userInfo.getNtranssitecode() +"') end as schildsitecode " 
				+ " FROM projectsitemapping psm "
				+ "JOIN bioproject bp ON psm.nbioprojectcode = bp.nbioprojectcode " 
				+ " LEFT JOIN sitehierarchyconfigdetails shcd ON psm.nnodesitecode = shcd.nnodesitecode "
				+ " AND psm.nsitehierarchyconfigcode = shcd.nsitehierarchyconfigcode "
				+ "  AND shcd.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " WHERE psm.nnodesitecode = "
				+ userInfo.getNtranssitecode() + " " + "AND bp.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " AND bp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND psm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<BioProject> allProjects = jdbcTemplate.query(strQry, new BioProject());

		final String strThirdPartyMapping = "";
		List<Map<String, Object>> finalList = new ArrayList<>();

		List<String> schildSiteCodes = allProjects.stream().map(BioProject::getSchildsitecode)
				.filter(Objects::nonNull).collect(Collectors.toList());
		String siteCodeCsv = schildSiteCodes.stream().collect(Collectors.joining(","));
		
		for (BioProject project : allProjects) {
			// Step 2: Get all active sample types under this project
			String sampleQuery = "SELECT DISTINCT p.nproductcode, p.sproductname "
					+ "FROM samplestoragetransaction sst " + "JOIN product p ON sst.nproductcode = p.nproductcode "
					+ "WHERE sst.nprojecttypecode = " + project.getNbioprojectcode() + " " + "AND sst.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND p.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND sst.nsitecode in ("+ siteCodeCsv 
					+ ")";

			List<Map<String, Object>> allSamples = jdbcTemplate.query(sampleQuery, (rs, rowNum) -> {
				Map<String, Object> sampleMap = new HashMap<>();
				sampleMap.put("label", rs.getString("sproductname"));
				sampleMap.put("value", rs.getInt("nproductcode"));
				sampleMap.put("projectId", project.getNbioprojectcode());
				return sampleMap;
			});

			// Step 3: Get already added samples for this project
			String addedQuery = "SELECT DISTINCT bdard.nproductcode " + "FROM biodataaccessrequestdetails bdard "
					+ " JOIN biodataaccessrequest bdar ON bdar.nbiodataaccessrequestcode=bdard.nbiodataaccessrequestcode AND"
					+ " bdar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " AND bdar.nsitecode=" + userInfo.getNtranssitecode() + " AND bdar.ntransactionstatus NOT IN ("
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", "
					+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ", "
					+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ")"
					+ " join thirdpartyusermapping tpm on tpm.nthirdpartycode=bdar.nthirdpartycode and tpm.nuserrolecode="
					+ userInfo.getNuserrole()+ " and tpm.nusercode="+ userInfo.getNusercode()+ " and tpm.nsitecode="
					+ userInfo.getNmastersitecode()+ " and tpm.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " WHERE bdard.nbioprojectcode = " + project.getNbioprojectcode() + " AND bdard.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND bdard.nsitecode="
					+ userInfo.getNtranssitecode();

			List<Integer> addedSamples = jdbcTemplate.queryForList(addedQuery, Integer.class);

			if (allSamples.isEmpty()) {
				// ✅ Project without samples → show project node (no children)
				Map<String, Object> projectNode = new HashMap<>();
				projectNode.put("label", project.getSprojecttitle());
				projectNode.put("value", project.getNbioprojectcode());
				projectNode.put("children", Collections.emptyList());
				finalList.add(projectNode);
				continue;
			}

			if (allSamples.size() == 1) {
				// ✅ Single sample project
				Map<String, Object> onlySample = allSamples.get(0);
				if (!addedSamples.contains(onlySample.get("value"))) {
					Map<String, Object> projectNode = new HashMap<>();
					projectNode.put("label", project.getSprojecttitle());
					projectNode.put("value", project.getNbioprojectcode());
					projectNode.put("children", allSamples);
					finalList.add(projectNode);
				}
			} else {
				// ✅ Multiple samples
				List<Map<String, Object>> remainingSamples = allSamples.stream()
						.filter(s -> !addedSamples.contains(s.get("value"))).collect(Collectors.toList());

				if (!remainingSamples.isEmpty()) {
					Map<String, Object> projectNode = new HashMap<>();
					projectNode.put("label", project.getSprojecttitle());
					projectNode.put("value", project.getNbioprojectcode());
					projectNode.put("children", remainingSamples);
					finalList.add(projectNode);
				}
			}
		}

		outputMap.put("projectList", finalList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} // added by sujatha ATE_274 BGSI-148 for throwing alert
		else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THIRDPARTYUSERONLYHAVERIGHTS",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
	}
	}

	/**
	 * Retrieves an active BioData Access Request along with its child details.
	 *
	 * <p>
	 * Steps performed:
	 * <ul>
	 * <li>Fetches parent BioData request with status, site, color, and transaction
	 * info.</li>
	 * <li>Joins with third party, transaction status, site, and color configuration
	 * tables.</li>
	 * <li>Uses formwisestatuscolor to fetch UI color codes based on status.</li>
	 * <li>If found, also fetches associated child records via
	 * {@link #getChildInitialGet(int, UserInfo)}.</li>
	 * </ul>
	 *
	 * @param nbiodataAccessrequestcode primary key of BioData request
	 * @param userInfo                  current logged in user info
	 * @return ResponseEntity containing parent request and its children, or nulls
	 *         if not found
	 * @throws Exception if query execution fails
	 */
	@Override
	public ResponseEntity<Object> getActiveBioDataAccess(int nbiodataAccessrequestcode, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strQuery = "SELECT dt.nbiodataaccessrequestcode, dt.sformnumber, dt.ntransactionstatus,s.ssitename sreceiversitename, "
				+ "dt.nthirdpartycode, dt.ntzrequestcreateddate, dt.drequestcreateddate, "
				+ "to_char(dt.drequestcreateddate,'" + userInfo.getSsitedate() + "') AS srequestdate , "
				+ " dt.ntransactionstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus,"
				+ " cm.scolorhexcode , dt.nsitecode, dt.nstatus " + "FROM biodataaccessrequest dt "
				+ "JOIN transactionstatus ts ON dt.ntransactionstatus = ts.ntranscode " + "AND ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN site s ON dt.nsitecode = s.nsitecode " + "AND s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN formwisestatuscolor fwsc ON fwsc.ntranscode = ts.ntranscode " + "AND fwsc.nformcode = 299 "
				+ "AND fwsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN colormaster cm ON fwsc.ncolorcode = cm.ncolorcode " + "AND cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and dt.nbiodataaccessrequestcode =" + nbiodataAccessrequestcode
				+ " order by nbiodataaccessrequestcode desc";
		final BiodataAccessRequest objBioDirectAccess = (BiodataAccessRequest) jdbcUtilityTemplateFunction
				.queryForObject(strQuery, BiodataAccessRequest.class, jdbcTemplate);

		if (objBioDirectAccess != null) {
			outputMap.put("selectedBioDataaccess", objBioDirectAccess);
			List<Map<String, Object>> lstChildBioDataaccess = getChildInitialGet(
					objBioDirectAccess.getNbiodataaccessrequestcode(), userInfo);
			outputMap.put("lstChildBioDataaccess", lstChildBioDataaccess);

		} else {
			outputMap.put("selectedBioDataaccess", null);
			outputMap.put("lstChildBioDataaccess", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * Common handler to change the status of a Third Party e-Catalogue request.
	 *
	 * @param inputMap        Request input containing nbiodataAccessrequestcode
	 * @param userInfo        Logged in user details
	 * @param newStatus       transaction status (REQUESTED)
	 * @param multilingualKey Message key for audit/logging
	 * @return ResponseEntity with updated object or error message
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> sendThirdpartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> outputMap = new HashMap<String, Object>();
		int nbiodataAccessrequestcode = (int) inputMap.getOrDefault("nbiodataaccessrequestcode", 0);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		final List<Object> listBeforeSave = new ArrayList<>();
		
		// Added by Vishakh (BGSI-287) for third party institution user issue
		final List<ThirdParty> thirdPartyUser = lstThirdParty(userInfo);
		if( thirdPartyUser.size()> 0 &&  thirdPartyUser.get(0).getNisngs()==Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			returnMap.putAll((Map<String, Object>) getActiveBioDataAccess(nbiodataAccessrequestcode, userInfo).getBody());
	
			BiodataAccessRequest objBioThirdCatalogueRequest = (BiodataAccessRequest) returnMap
					.get("selectedBioDataaccess");
	
			if (objBioThirdCatalogueRequest != null && objBioThirdCatalogueRequest
					.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
				final List<BiodataAccessRequestDetails> lstBioThirdCatalogueDetails = (List<BiodataAccessRequestDetails>) returnMap
						.get("lstChildBioDataaccess");
				if (lstBioThirdCatalogueDetails != null && !lstBioThirdCatalogueDetails.isEmpty()) {
	
					final String sQuery = " lock  table lockbiobgsiecatalogue "
							+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery);
	
					int seqNoBGSiECatalogueHistory = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnobiobankmanagement where stablename in ('biodataaccessrequesthistory') and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);
					seqNoBGSiECatalogueHistory = seqNoBGSiECatalogueHistory + 1;
					final String strAuditQry = auditQuery(nbiodataAccessrequestcode, -1, userInfo);
					List<BiodataAccessRequest> lstAuditBefore = jdbcTemplate.query(strAuditQry, new BiodataAccessRequest());
					final String updQry = "Update biodataaccessrequest set ntransactionstatus = "
							+ Enumeration.TransactionStatus.REQUESTED.gettransactionstatus() + ", noffsetdrequesteddate = "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ ", ntzrequesteddate = " + userInfo.getNtimezonecode() + "  where nbiodataAccessrequestcode = "
							+ objBioThirdCatalogueRequest.getNbiodataaccessrequestcode() + " and nsitecode = "
							+ userInfo.getNtranssitecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";
	
					String strInsert = "INSERT INTO biodataaccessrequesthistory ("
							+ "nbiodataaccessrequesthistorycode, nbiodataaccessrequestcode, ntransactionstatus, nuserrolecode, "
							+ "nusercode, ndeputyusercode, ndeputyuserrolecode, dtransactiondate, noffsetdtransactiondate, nsitecode, nstatus) "
							+ "VALUES (" + seqNoBGSiECatalogueHistory + ", "
							+ objBioThirdCatalogueRequest.getNbiodataaccessrequestcode() + ","
							+ Enumeration.TransactionStatus.REQUESTED.gettransactionstatus() + ", "
							+ userInfo.getNuserrole() + ", " + userInfo.getNusercode() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
	
					strInsert += "update seqnobiobankmanagement set nsequenceno=" + seqNoBGSiECatalogueHistory + " where"
							+ " stablename='biodataaccessrequesthistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					jdbcTemplate.execute(updQry + strInsert);
	
					List<BiodataAccessRequest> lstAuditAfter = jdbcTemplate.query(strAuditQry, new BiodataAccessRequest());
	
					lstAuditAfter.stream().forEach(x -> multilingualIDList.add("IDS_REQUESTFORM"));
					listBeforeSave.addAll(lstAuditBefore);
					listAfterSave.addAll(lstAuditAfter);
					auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
							userInfo);
					
					
					//Added By Mullai Balaji for Email jira ID-BGSI-147
					String Query="select DISTINCT (ncontrolcode) from emailconfig where ncontrolcode="+inputMap.get("ncontrolcode")+" "
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					Integer value = null;
					try {
					    value = jdbcTemplate.queryForObject(Query, Integer.class);
					} catch (Exception e) {
					    value = null; 
					}	            
					if(value!=null) {
					final Map<String, Object> mailMap = new HashMap<String, Object>();
					mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));
					mailMap.put("nbiodataaccessrequestcode", objBioThirdCatalogueRequest.getNbiodataaccessrequestcode() );
					String query = "SELECT sformnumber FROM biodataaccessrequest where nbiodataaccessrequestcode="+objBioThirdCatalogueRequest.getNbiodataaccessrequestcode() ;
					String referenceId = jdbcTemplate.queryForObject(query, String.class);
					mailMap.put("ssystemid", referenceId);
					final UserInfo mailUserInfo = new UserInfo(userInfo);
					mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
					mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
					emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);	
					}
					outputMap.putAll(
							(Map<String, Object>) getActiveBioDataAccess(nbiodataAccessrequestcode, userInfo).getBody());
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOSAMPLESAREAVAILABLE",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
	
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTSTATUSRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THIRDPARTYUSERONLYCANSENDREQUEST",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	/**
	 * Common handler to change the status of a Third Party e-Catalogue request.
	 *
	 * @param inputMap        Request input containing nbiodataAccessrequestcode
	 * @param userInfo        Logged in user details
	 * @param newStatus       transaction status (CANCEL)
	 * @param multilingualKey Message key for audit/logging
	 * @return ResponseEntity with updated object or error message
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> cancelThirdpartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> outputMap = new HashMap<String, Object>();
		int nbiodataAccessrequestcode = (int) inputMap.getOrDefault("nbiodataaccessrequestcode", 0);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		final List<Object> listBeforeSave = new ArrayList<>();
		
		// Added by Vishakh (BGSI-287) for third party institution user issue
		final List<ThirdParty> thirdPartyUser = lstThirdParty(userInfo);
		if( thirdPartyUser.size()> 0 &&  thirdPartyUser.get(0).getNisngs()==Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			returnMap.putAll((Map<String, Object>) getActiveBioDataAccess(nbiodataAccessrequestcode, userInfo).getBody());
	
			BiodataAccessRequest objBioThirdCatalogueRequest = (BiodataAccessRequest) returnMap
					.get("selectedBioDataaccess");
	
			if (objBioThirdCatalogueRequest != null
					&& objBioThirdCatalogueRequest.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT
							.gettransactionstatus()
					|| objBioThirdCatalogueRequest.getNtransactionstatus() == Enumeration.TransactionStatus.REQUESTED
							.gettransactionstatus()) {
				final List<BiodataAccessRequestDetails> lstBioThirdCatalogueDetails = (List<BiodataAccessRequestDetails>) returnMap
						.get("lstChildBioDataaccess");
				if (lstBioThirdCatalogueDetails != null && !lstBioThirdCatalogueDetails.isEmpty()) {
	
					final String sQuery = " lock  table lockbiobgsiecatalogue "
							+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery);
	
					int seqNoBGSiECatalogueHistory = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnobiobankmanagement where stablename in ('biodataaccessrequesthistory') and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);
					seqNoBGSiECatalogueHistory = seqNoBGSiECatalogueHistory + 1;
					final String strAuditQry = auditQuery(nbiodataAccessrequestcode, -1, userInfo);
					List<BiodataAccessRequest> lstAuditBefore = jdbcTemplate.query(strAuditQry, new BiodataAccessRequest());
					final String updQry = "Update biodataaccessrequest set ntransactionstatus = "
							+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", noffsetdrequesteddate = "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ ", ntzrequesteddate = " + userInfo.getNtimezonecode() + "  where nbiodataAccessrequestcode = "
							+ objBioThirdCatalogueRequest.getNbiodataaccessrequestcode() + " and nsitecode = "
							+ userInfo.getNtranssitecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";
	
					String strInsert = "INSERT INTO biodataaccessrequesthistory ("
							+ "nbiodataaccessrequesthistorycode, nbiodataaccessrequestcode, ntransactionstatus, nuserrolecode, "
							+ "nusercode, ndeputyusercode, ndeputyuserrolecode, dtransactiondate, noffsetdtransactiondate, nsitecode, nstatus) "
							+ "VALUES (" + seqNoBGSiECatalogueHistory + ", "
							+ objBioThirdCatalogueRequest.getNbiodataaccessrequestcode() + ","
							+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", " + userInfo.getNuserrole()
							+ ", " + userInfo.getNusercode() + ", " + userInfo.getNdeputyusercode() + ", "
							+ userInfo.getNdeputyuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "', " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
	
					strInsert += "update seqnobiobankmanagement set nsequenceno=" + seqNoBGSiECatalogueHistory + " where"
							+ " stablename='biodataaccessrequesthistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					jdbcTemplate.execute(updQry + strInsert);
	
					List<BiodataAccessRequest> lstAuditAfter = jdbcTemplate.query(strAuditQry, new BiodataAccessRequest());
	
					lstAuditAfter.stream().forEach(x -> multilingualIDList.add("IDS_CANCELREQUESTFORM"));
					listBeforeSave.addAll(lstAuditBefore);
					listAfterSave.addAll(lstAuditAfter);
					auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
							userInfo);
					outputMap.putAll(
							(Map<String, Object>) getActiveBioDataAccess(nbiodataAccessrequestcode, userInfo).getBody());
	
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOSAMPLESAREAVAILABLE",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
	
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTSTATUSRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THIRDPARTYUSERONLYCANCANCELREQUEST",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	/**
	 * Common handler to change the status of a Third Party e-Catalogue request.
	 *
	 * @param inputMap        Request input containing nbiodataAccessrequestcode
	 * @param userInfo        Logged in user details
	 * @param newStatus       transaction status (APPROVE)
	 * @param multilingualKey Message key for audit/logging
	 * @return ResponseEntity with updated object or error message
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> approveThirdpartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> outputMap = new HashMap<String, Object>();
		int nbiodataAccessrequestcode = (int) inputMap.getOrDefault("nbiodataaccessrequestcode", 0);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		final List<Object> listBeforeSave = new ArrayList<>();
		
		// Added by Vishakh (BGSI-287) for third party institution user issue
		//commented and below condition added by sujatha ATE_274 for new requirement
//		final List<ThirdParty> thirdPartyUser = lstThirdParty(userInfo);
//		if( thirdPartyUser.size()> 0 &&  thirdPartyUser.get(0).getNisngs()==Enumeration.TransactionStatus.NO.gettransactionstatus()) {
		final List<Users> bioBankUsers = BioBankUsers(userInfo);
		if(bioBankUsers.size()>0) {
			returnMap.putAll((Map<String, Object>) getActiveBioDataAccess(nbiodataAccessrequestcode, userInfo).getBody());
	
			BiodataAccessRequest objBioThirdCatalogueRequest = (BiodataAccessRequest) returnMap
					.get("selectedBioDataaccess");
	
			if (objBioThirdCatalogueRequest != null && objBioThirdCatalogueRequest
					.getNtransactionstatus() == Enumeration.TransactionStatus.REQUESTED.gettransactionstatus()) {
				final List<BiodataAccessRequestDetails> lstBioThirdCatalogueDetails = (List<BiodataAccessRequestDetails>) returnMap
						.get("lstChildBioDataaccess");
				if (lstBioThirdCatalogueDetails != null && !lstBioThirdCatalogueDetails.isEmpty()) {
	
					final String sQuery = " lock  table lockbiobgsiecatalogue "
							+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery);
	
					int seqNoBGSiECatalogueHistory = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnobiobankmanagement where stablename in ('biodataaccessrequesthistory') and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);
					seqNoBGSiECatalogueHistory = seqNoBGSiECatalogueHistory + 1;
					final String strAuditQry = auditQuery(nbiodataAccessrequestcode, -1, userInfo);
					List<BiodataAccessRequest> lstAuditBefore = jdbcTemplate.query(strAuditQry, new BiodataAccessRequest());
					final String updQry = "Update biodataaccessrequest set ntransactionstatus = "
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", noffsetdrequesteddate = "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ ", ntzrequesteddate = " + userInfo.getNtimezonecode() + "  where nbiodataAccessrequestcode = "
							+ objBioThirdCatalogueRequest.getNbiodataaccessrequestcode() + " and nsitecode = "
							+ userInfo.getNtranssitecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";
	
					String strInsert = "INSERT INTO biodataaccessrequesthistory ("
							+ "nbiodataaccessrequesthistorycode, nbiodataaccessrequestcode, ntransactionstatus, nuserrolecode, "
							+ "nusercode, ndeputyusercode, ndeputyuserrolecode, dtransactiondate, noffsetdtransactiondate, nsitecode, nstatus) "
							+ "VALUES (" + seqNoBGSiECatalogueHistory + ", "
							+ objBioThirdCatalogueRequest.getNbiodataaccessrequestcode() + ","
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", " + userInfo.getNuserrole()
							+ ", " + userInfo.getNusercode() + ", " + userInfo.getNdeputyusercode() + ", "
							+ userInfo.getNdeputyuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "', " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
	
					strInsert += "update seqnobiobankmanagement set nsequenceno=" + seqNoBGSiECatalogueHistory + " where"
							+ " stablename='biodataaccessrequesthistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					jdbcTemplate.execute(updQry + strInsert);
	
					List<BiodataAccessRequest> lstAuditAfter = jdbcTemplate.query(strAuditQry, new BiodataAccessRequest());
	
					lstAuditAfter.stream().forEach(x -> multilingualIDList.add("IDS_APPROVEREQUESTFORM"));
					listBeforeSave.addAll(lstAuditBefore);
					listAfterSave.addAll(lstAuditAfter);
					auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
							userInfo);
					
					//Added By Mullai Balaji for Email jira ID-BGSI-147
					String Query="select DISTINCT (ncontrolcode) from emailconfig where ncontrolcode="+inputMap.get("ncontrolcode")+" "
							+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					Integer value = null;
					try {
					    value = jdbcTemplate.queryForObject(Query, Integer.class);
					} catch (Exception e) {
					    value = null; 
					}	            
					if(value!=null) {
					final Map<String, Object> mailMap = new HashMap<String, Object>();
					mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));
					mailMap.put("nbiodataaccessrequestcode", objBioThirdCatalogueRequest.getNbiodataaccessrequestcode() );
					String query = "SELECT sformnumber FROM biodataaccessrequest where nbiodataaccessrequestcode="+objBioThirdCatalogueRequest.getNbiodataaccessrequestcode() ;
					String referenceId = jdbcTemplate.queryForObject(query, String.class);
					mailMap.put("ssystemid", referenceId);
					final UserInfo mailUserInfo = new UserInfo(userInfo);
					mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
					mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
					emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);	
					}
									
					outputMap.putAll(
							(Map<String, Object>) getActiveBioDataAccess(nbiodataAccessrequestcode, userInfo).getBody());
				}
	
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTREQUESTSTATUSRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
			}
		else {
			//commented and added by sujatha ATE_274 for the correct alert to throw
//			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THIRDPARTYUSERONLYCANAPPROVEREQUEST",
//					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_BIOBANKUSERONLYCANAPPROVEREQUEST",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	/**
	 * Common handler to change the status of a Third Party e-Catalogue request.
	 *
	 * @param inputMap        Request input containing nbiodataAccessrequestcode
	 * @param userInfo        Logged in user details
	 * @param newStatus       transaction status (RETIRE)
	 * @param multilingualKey Message key for audit/logging
	 * @return ResponseEntity with updated object or error message
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> retireThirdECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		final List<Object> listBeforeSave = new ArrayList<>();
		int nbiodataAccessrequestcode = (int) inputMap.getOrDefault("nbiodataaccessrequestcode", 0);
		
		//commented and the below condition is added by sujatha ATE_274 for requirement
		//start
		// Added by Vishakh (BGSI-287) for third party institution user issue
//		final List<ThirdParty> thirdPartyUser = lstThirdParty(userInfo);
//		if( thirdPartyUser.size()> 0 &&  thirdPartyUser.get(0).getNisngs()==Enumeration.TransactionStatus.NO.gettransactionstatus()) {
		final List<Users> bioBankUsers = BioBankUsers(userInfo);
		if(bioBankUsers.size()>0) {
			//end
			returnMap.putAll((Map<String, Object>) getActiveBioDataAccess(nbiodataAccessrequestcode, userInfo).getBody());
	
			BiodataAccessRequest objBioThirdCatalogueRequest = (BiodataAccessRequest) returnMap
					.get("selectedBioDataaccess");
	
			if (objBioThirdCatalogueRequest != null && objBioThirdCatalogueRequest
					.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
				final List<BiodataAccessRequestDetails> lstBioThirdCatalogueDetails = (List<BiodataAccessRequestDetails>) returnMap
						.get("lstChildBioDataaccess");
				if (lstBioThirdCatalogueDetails != null && !lstBioThirdCatalogueDetails.isEmpty()) {
	
					final String sQuery = " lock  table lockbiobgsiecatalogue "
							+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery);
	
					int seqNoBGSiECatalogueHistory = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnobiobankmanagement where stablename in ('biodataaccessrequesthistory') and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);
					seqNoBGSiECatalogueHistory = seqNoBGSiECatalogueHistory + 1;
					final String strAuditQry = auditQuery(nbiodataAccessrequestcode, -1, userInfo);
					List<BiodataAccessRequest> lstAuditBefore = jdbcTemplate.query(strAuditQry, new BiodataAccessRequest());
					final String updQry = "Update biodataaccessrequest set ntransactionstatus = "
							+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ", noffsetdrequesteddate = "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ ", ntzrequesteddate = " + userInfo.getNtimezonecode() + "  where nbiodataAccessrequestcode = "
							+ objBioThirdCatalogueRequest.getNbiodataaccessrequestcode() + " and nsitecode = "
							+ userInfo.getNtranssitecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";
	
					String strInsert = "INSERT INTO biodataaccessrequesthistory ("
							+ "nbiodataaccessrequesthistorycode, nbiodataaccessrequestcode, ntransactionstatus, nuserrolecode, "
							+ "nusercode, ndeputyusercode, ndeputyuserrolecode, dtransactiondate, noffsetdtransactiondate, nsitecode, nstatus) "
							+ "VALUES (" + seqNoBGSiECatalogueHistory + ", "
							+ objBioThirdCatalogueRequest.getNbiodataaccessrequestcode() + ","
							+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ", " + userInfo.getNuserrole()
							+ ", " + userInfo.getNusercode() + ", " + userInfo.getNdeputyusercode() + ", "
							+ userInfo.getNdeputyuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "', " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
	
					strInsert += "update seqnobiobankmanagement set nsequenceno=" + seqNoBGSiECatalogueHistory + " where"
							+ " stablename='biodataaccessrequesthistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					jdbcTemplate.execute(updQry + strInsert);
	
					List<BiodataAccessRequest> lstAuditAfter = jdbcTemplate.query(strAuditQry, new BiodataAccessRequest());
	
					lstAuditAfter.stream().forEach(x -> multilingualIDList.add("IDS_RETIREREQUESTFORM"));
					listBeforeSave.addAll(lstAuditBefore);
					listAfterSave.addAll(lstAuditAfter);
					auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
							userInfo);
					outputMap.putAll(
							(Map<String, Object>) getActiveBioDataAccess(nbiodataAccessrequestcode, userInfo).getBody());
	
				}
	
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTREQUESTSTATUSRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
		else {
			//commentted and added by sujatha ATE_274 for the correct validation
//			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THIRDPARTYUSERONLYCANRETIREREQUEST",
//					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_BIOBANKUSERONLYCANRETIREREQUEST",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	/**
	 * Common handler to change the status of a Third Party e-Catalogue request.
	 *
	 * @param inputMap        Request input containing nbiodataAccessrequestcode
	 * @param userInfo        Logged in user details
	 * @param newStatus       transaction status (REJECT)
	 * @param multilingualKey Message key for audit/logging
	 * @return ResponseEntity with updated object or error message
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> rejectThirdECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> outputMap = new HashMap<String, Object>();
		int nbiodataAccessrequestcode = (int) inputMap.getOrDefault("nbiodataaccessrequestcode", 0);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		final List<Object> listBeforeSave = new ArrayList<>();
		
		// Added by Vishakh (BGSI-287) for third party institution user issue
		//commented and below condition is added by sujatha ATE_274 for new requirement
		//start
//		final List<ThirdParty> thirdPartyUser = lstThirdParty(userInfo);
//		if( thirdPartyUser.size()> 0 &&  thirdPartyUser.get(0).getNisngs()==Enumeration.TransactionStatus.NO.gettransactionstatus()) {
		final List<Users> bioBankUsers = BioBankUsers(userInfo);
		if(bioBankUsers.size()>0) {
			//end
			returnMap.putAll((Map<String, Object>) getActiveBioDataAccess(nbiodataAccessrequestcode, userInfo).getBody());
	
			BiodataAccessRequest objBioThirdCatalogueRequest = (BiodataAccessRequest) returnMap
					.get("selectedBioDataaccess");
	
			if (objBioThirdCatalogueRequest != null && objBioThirdCatalogueRequest
					.getNtransactionstatus() != Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {
				final List<BiodataAccessRequestDetails> lstBioThirdCatalogueDetails = (List<BiodataAccessRequestDetails>) returnMap
						.get("lstChildBioDataaccess");
				if (lstBioThirdCatalogueDetails != null && !lstBioThirdCatalogueDetails.isEmpty()) {
	
					final String sQuery = " lock  table lockbiobgsiecatalogue "
							+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery);
	
					int seqNoBGSiECatalogueHistory = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnobiobankmanagement where stablename in ('biodataaccessrequesthistory') and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);
					seqNoBGSiECatalogueHistory = seqNoBGSiECatalogueHistory + 1;
					final String strAuditQry = auditQuery(nbiodataAccessrequestcode, -1, userInfo);
					List<BiodataAccessRequest> lstAuditBefore = jdbcTemplate.query(strAuditQry, new BiodataAccessRequest());
					final String updQry = "Update biodataaccessrequest set ntransactionstatus = "
							+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ", noffsetdrequesteddate = "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ ", ntzrequesteddate = " + userInfo.getNtimezonecode() + "  where nbiodataAccessrequestcode = "
							+ objBioThirdCatalogueRequest.getNbiodataaccessrequestcode() + " and nsitecode = "
							+ userInfo.getNtranssitecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";
	
					String strInsert = "INSERT INTO biodataaccessrequesthistory ("
							+ "nbiodataaccessrequesthistorycode, nbiodataaccessrequestcode, ntransactionstatus, nuserrolecode, "
							+ "nusercode, ndeputyusercode, ndeputyuserrolecode, dtransactiondate, noffsetdtransactiondate, nsitecode, nstatus) "
							+ "VALUES (" + seqNoBGSiECatalogueHistory + ", "
							+ objBioThirdCatalogueRequest.getNbiodataaccessrequestcode() + ","
							+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ", " + userInfo.getNuserrole()
							+ ", " + userInfo.getNusercode() + ", " + userInfo.getNdeputyusercode() + ", "
							+ userInfo.getNdeputyuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
							+ "', " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
	
					strInsert += "update seqnobiobankmanagement set nsequenceno=" + seqNoBGSiECatalogueHistory + " where"
							+ " stablename='biodataaccessrequesthistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					jdbcTemplate.execute(updQry + strInsert);
	
					List<BiodataAccessRequest> lstAuditAfter = jdbcTemplate.query(strAuditQry, new BiodataAccessRequest());
	
					lstAuditAfter.stream().forEach(x -> multilingualIDList.add("IDS_REJECTREQUESTFORM"));
					listBeforeSave.addAll(lstAuditBefore);
					listAfterSave.addAll(lstAuditAfter);
	
					auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
							userInfo);
					outputMap.putAll(
							(Map<String, Object>) getActiveBioDataAccess(nbiodataAccessrequestcode, userInfo).getBody());
	
				}
	
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTREQUESTSTATUSRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
		else {
			// commented and added by sujatha ATE_274 for the correct alert to thrown
//			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THIRDPARTYUSERONLYCANREJECTREQUEST",
//					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_BIOBANKUSERONLYCANREJECTREQUEST",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	/**
	 * Finds the current transaction status of a BioData Access Request.
	 *
	 * @param nbiodataAccessrequestcode Unique request code for BioData Access
	 * @param userInfo                  Logged-in user information
	 * @return Transaction status (int)
	 * @throws Exception If database access fails
	 */
	@Override
	public int findStatusrequest(int nbiodataAccessrequestcode, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		final String strStatusDirectTransfer = "select ntransactionstatus from biodataaccessrequest where nbiodataaccessrequestcode="
				+ nbiodataAccessrequestcode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.queryForObject(strStatusDirectTransfer, Integer.class);
	}

	/**
	 * Deletes child BioData Access Request details (samples) and updates parent
	 * request status, but only if the request is in DRAFT or VALIDATION state.
	 *
	 * @param nbiodataAccessrequestcode        Parent BioData Access Request code
	 * @param nbiodataAccessrequestdetailscode Child BioData Access Request Details
	 *                                         code(s) (comma separated)
	 * @param userInfo                         Logged-in user info
	 * @return ResponseEntity with updated child list or error message
	 * @throws Exception If DB access or audit fails
	 */
	@Override
	public ResponseEntity<Object> deleteChildBioRequest(int nbiodataAccessrequestcode,
			String nbiodataAccessrequestdetailscode, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();

		// Added by Vishakh (BGSI-287) for third party institution user issue
		final List<ThirdParty> thirdPartyUser = lstThirdParty(userInfo);
		if( thirdPartyUser.size()> 0 &&  thirdPartyUser.get(0).getNisngs()==Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			final int findStatus = findStatusrequest(nbiodataAccessrequestcode, userInfo);
	
			if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					|| findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {
	
				final String strAuditBeforeQry = childAuditQuery(nbiodataAccessrequestcode,
						nbiodataAccessrequestdetailscode, userInfo);
				List<BiodataAccessRequest> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditBeforeQry,
						new BiodataAccessRequest());
	
				final String strDeleteQry = "update biodataaccessrequestdetails set nstatus="
						+ Enumeration.TransactionStatus.NA.gettransactionstatus()
						+ " where nbiodataaccessrequestdetailscode in (" + nbiodataAccessrequestdetailscode
						+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(strDeleteQry);
	
				lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
				lstAuditTransferDetailsBefore.stream().forEach(x -> multilingualIDList.add("IDS_DELETEREQUESTSAMPLES"));
	
				auditUtilityFunction.fnInsertAuditAction(lstAuditBefore, 1, null, multilingualIDList, userInfo);
	
				List<Map<String, Object>> lstChildGet = getChildInitialGet(nbiodataAccessrequestcode, userInfo);
				outputMap.put("lstChildGet", lstChildGet);
	
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
		else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THIRDPARTYUSERONLYCANDELETEDATAVIEWREQUEST",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	/**
	 * Builds a query to fetch audit information for specific child BioData Access
	 * Request details. Used for capturing audit trail before performing delete or
	 * update operations.
	 *
	 * @param nbiodataAccessrequestcode        Parent BioData Access Request code
	 * @param nbiodataAccessrequestdetailscode Child request details code(s) (comma
	 *                                         separated string of IDs)
	 * @param userInfo                         Logged-in user details (site,
	 *                                         language, etc.)
	 * @return SQL query string for fetching audit details
	 * @throws Exception If any exception occurs while building query
	 */
	public String childAuditQuery(final int nbiodataAccessrequestcode, final String nbiodataAccessrequestdetailscode,
			final UserInfo userInfo) throws Exception {

		final String strChildAuditQuery = "SELECT bar.nbiodataaccessrequestcode, " + "       bar.sformnumber, "
				+ "       bard.nbiodataaccessrequestdetailscode, " + "       bp.sprojecttitle, "
				+ "       p.sproductname, " + "       COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', "
				+ "                ts.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus, "
				+ "       bar.ntransactionstatus, " + "       bar.nsitecode " + "FROM biodataaccessrequest bar "
				+ "JOIN biodataaccessrequestdetails bard "
				+ "     ON bard.nbiodataaccessrequestcode = bar.nbiodataaccessrequestcode "
				+ "    AND bard.nsitecode = " + userInfo.getNtranssitecode() + " " + "    AND bard.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "LEFT JOIN bioproject bp "
				+ "     ON bp.nbioprojectcode = bard.nbioprojectcode " + "    AND bp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "LEFT JOIN product p "
				+ "     ON p.nproductcode = bard.nproductcode " + "    AND p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "JOIN transactionstatus ts "
				+ "     ON ts.ntranscode = bar.ntransactionstatus " + "    AND ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "WHERE bar.nbiodataaccessrequestcode = " + nbiodataAccessrequestcode + " " + "  AND bar.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + "  AND bar.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "  AND bard.nbiodataaccessrequestdetailscode IN (" + nbiodataAccessrequestdetailscode + ") "
				+ "ORDER BY bar.nbiodataaccessrequestcode DESC;";

		return strChildAuditQuery;
	}

	/**
	 * Retrieves the list of child BioData (projects and their sample types)
	 * available for selection under a given parent BioData access request.
	 * 
	 * <p>
	 * Steps:
	 * </p>
	 * <ul>
	 * <li>Validates the input parent request code.</li>
	 * <li>Fetches all active projects and their associated sample types for the
	 * current site.</li>
	 * <li>Excludes projects/sample types already added in the child request
	 * details.</li>
	 * <li>Groups sample types under their corresponding project and returns as
	 * response.</li>
	 * </ul>
	 * 
	 * @param inputMap contains the parent request code (nbiodataaccessrequestcode)
	 * @param userInfo user session details (site, language, timezone, etc.)
	 * @return ResponseEntity with project list and sample types, or error message
	 *         if invalid
	 */
	@SuppressWarnings({ "serial", "unchecked" })
	@Override
	public ResponseEntity<Object> getChildBioData(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		// Added by Vishakh (BGSI-287) for third party institution user issue
		final List<ThirdParty> thirdPartyUser = lstThirdParty(userInfo);
		if( thirdPartyUser.size()> 0 &&  thirdPartyUser.get(0).getNisngs()==Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			Integer nbiodataaccessrequestcode = (Integer) inputMap.get("nbiodataaccessrequestcode");
			if (nbiodataaccessrequestcode == null || nbiodataaccessrequestcode <= 0) {
				return ResponseEntity.badRequest().body("Invalid parent request code.");
			}
	
			String sql = "SELECT DISTINCT " + "       bp.nbioprojectcode, " + "       bp.sprojecttitle, "
					+ "       p.nproductcode, " + "       p.sproductname " + " FROM projectsitemapping pm "
					+ " JOIN bioproject bp ON pm.nbioprojectcode = bp.nbioprojectcode "
					+ " JOIN sitehierarchyconfigdetails shcd " + "       ON pm.nnodesitecode = shcd.nnodesitecode "
					+ "      AND pm.nsitehierarchyconfigcode = shcd.nsitehierarchyconfigcode "
					+ " LEFT JOIN samplestoragetransaction sst " + "       ON sst.nprojecttypecode = bp.nbioprojectcode "
					+ "      AND sst.nsitecode = shcd.nnodesitecode " + "      AND sst.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " LEFT JOIN product p ON p.nproductcode = sst.nproductcode " + "      AND p.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " WHERE pm.nnodesitecode = "
					+ userInfo.getNtranssitecode() + "  AND pm.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND bp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND shcd.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND NOT EXISTS ( "
					+ "        SELECT 1 " + "        FROM biodataaccessrequestdetails brd "
					+ "        WHERE brd.nbiodataaccessrequestcode = " + nbiodataaccessrequestcode
					+ "          AND brd.nbioprojectcode = bp.nbioprojectcode "
					+ "          AND (brd.nproductcode = p.nproductcode OR p.nproductcode IS NULL) "
					+ "          AND brd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  ) "
					+ " ORDER BY bp.sprojecttitle, p.sproductname";
	
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
	
			Map<Integer, Map<String, Object>> projectMap = new LinkedHashMap<>();
			for (Map<String, Object> row : rows) {
				Integer projectCode = (Integer) row.get("nbioprojectcode");
				String projectTitle = (String) row.get("sprojecttitle");
				Integer productCode = (Integer) row.get("nproductcode");
				String productName = (String) row.get("sproductname");
	
				projectMap.putIfAbsent(projectCode, new HashMap<>() {
					{
						put("nbioprojectcode", projectCode);
						put("sprojecttitle", projectTitle);
						put("sampleTypes", new ArrayList<Map<String, Object>>());
					}
				});
	
				// Add sampleType only if exists
				if (productCode != null) {
					List<Map<String, Object>> samples = (List<Map<String, Object>>) projectMap.get(projectCode)
							.get("sampleTypes");
					Map<String, Object> sample = new HashMap<>();
					sample.put("nproductcode", productCode);
					sample.put("sproductname", productName);
					samples.add(sample);
				}
			}
	
			outputMap.put("projectList", new ArrayList<>(projectMap.values()));
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THIRDPARTYUSERONLYHAVERIGHTS",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	/**
	 * Retrieves the list of sample type data associated with a BioData access
	 * request.
	 * 
	 * <p>
	 * Steps:
	 * </p>
	 * <ul>
	 * <li>Extracts required parameters from the provided input map.</li>
	 * <li>Fetches sample types for the given BioData request, filtered by site and
	 * user details.</li>
	 * <li>Validates active status to ensure only valid records are returned.</li>
	 * <li>Returns the data as a ResponseEntity in JSON format.</li>
	 * </ul>
	 *
	 * @param inputMap request payload containing parameters (e.g., request code,
	 *                 filters)
	 * @param userInfo current logged-in user context (site, role, language, etc.)
	 * @return ResponseEntity containing sample type data or an error response
	 * @throws Exception if any database or processing error occurs
	 */
	@Override
	public ResponseEntity<Object> getChildBioDataRequestSampleTypeData(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Integer nbiodataaccessrequestcode = (Integer) inputMap.get("nbiodataaccessrequestcode");
		final Map<String, Object> outputMap = new HashMap<>();

		// 1. Validate input
		if (nbiodataaccessrequestcode == null || nbiodataaccessrequestcode <= 0) {
			return ResponseEntity.badRequest().body("Invalid BioProject Code.");
		}

		String sql = "SELECT DISTINCT bp.nbioprojectcode " + " FROM biodataaccessrequestdetails brd "
				+ " JOIN bioproject bp ON brd.nbioprojectcode = bp.nbioprojectcode "
				+ " WHERE brd.nbiodataaccessrequestcode = " + nbiodataaccessrequestcode + "" + " AND brd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND bp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		Integer nbioprojectcode = jdbcTemplate.queryForObject(sql, Integer.class);

		if (nbioprojectcode == null) {
			outputMap.put("productList", Collections.emptyList());
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}

		String usedProductsQuery = " SELECT DISTINCT nproductcode " + " FROM biodataaccessrequestdetails "
				+ " WHERE nbiodataaccessrequestcode = " + nbiodataaccessrequestcode + "" + " AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		List<Integer> usedProducts = jdbcTemplate.queryForList(usedProductsQuery, Integer.class);

		// 3. Get site hierarchy details for this project
		String siteCodeQuery = " SELECT bp.sprojecttitle, pm.nbioprojectcode, pm.nnodesitecode, "
				+ "       pm.nsitehierarchyconfigcode, shcd.schildsitecode " + " FROM projectsitemapping pm "
				+ " JOIN bioproject bp ON pm.nbioprojectcode = bp.nbioprojectcode "
				+ " JOIN sitehierarchyconfigdetails shcd ON pm.nnodesitecode = shcd.nnodesitecode "
				+ " AND pm.nsitehierarchyconfigcode = shcd.nsitehierarchyconfigcode " + " WHERE pm.nnodesitecode = "
				+ userInfo.getNtranssitecode() + " " + " AND pm.nbioprojectcode = " + nbioprojectcode + " "
				+ " AND pm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND bp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND shcd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " GROUP BY bp.sprojecttitle, pm.nbioprojectcode, pm.nnodesitecode, pm.nsitehierarchyconfigcode, shcd.schildsitecode";

		List<SiteHierarchyConfigDetails> siteDetailsList = jdbcTemplate.query(siteCodeQuery,
				new SiteHierarchyConfigDetails());

		// Extract schildsitecode list
		List<String> schildSiteCodes = siteDetailsList.stream().map(SiteHierarchyConfigDetails::getSchildsitecode)
				.filter(Objects::nonNull).collect(Collectors.toList());

		if (schildSiteCodes.isEmpty()) {
			outputMap.put("productList", Collections.emptyList());
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}

		// Convert site codes into comma-separated values
		String siteCodeCsv = schildSiteCodes.stream().collect(Collectors.joining(",")); // numeric → no quotes

		// 4. Query all possible products from storage
		String productQuery = " SELECT DISTINCT p.nproductcode, p.sproductname " + " FROM samplestoragetransaction sst "
				+ " JOIN product p ON sst.nproductcode = p.nproductcode " + " WHERE sst.nprojecttypecode = "
				+ nbioprojectcode + " " + " AND p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND sst.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND sst.nsitecode IN ("
				+ siteCodeCsv + ")";

		List<Product> productListName = jdbcTemplate.query(productQuery, new Product());

		// 5. Exclude already used products
		List<Map<String, Object>> productList = productListName.stream()
				.filter(prod -> !usedProducts.contains(prod.getNproductcode())).map(prod -> {
					Map<String, Object> map = new HashMap<>();
					map.put("label", prod.getSproductname());
					map.put("value", prod.getNproductcode());
					map.put("item", prod);
					return map;
				}).collect(Collectors.toList());

		// 6. Return remaining products
		outputMap.put("productList", productList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * Creates child BioData access request records (sample types) under a parent
	 * request.
	 *
	 * <p>
	 * This method:
	 * <ul>
	 * <li>Fetches the current sequence number for biodata access request
	 * details.</li>
	 * <li>Increments the sequence for each sample type provided in the
	 * request.</li>
	 * <li>Builds and executes insert queries for the child request records.</li>
	 * <li>Updates the sequence table after inserts.</li>
	 * <li>Captures audit logs for the inserted records.</li>
	 * <li>Finally, retrieves and returns the updated parent-child BioData request
	 * data.</li>
	 * </ul>
	 *
	 * @param inputMap input payload containing parent request code, project code,
	 *                 and sample type list
	 * @param userInfo current logged-in user context
	 * @return ResponseEntity containing updated BioData request data
	 * @throws Exception if database or processing error occurs
	 */
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> createChildBioDataRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final int nbiodataaccessrequestcode = (int) inputMap.get("nbiodataaccessrequestcode");
		final int nbioProjectCode = (int) inputMap.get("bioprojectcode");
		List<Integer> sampleTypes = (List<Integer>) inputMap.get("sampleTypes");

		// Added by Vishakh (BGSI-287) for third party institution user issue
		final List<ThirdParty> thirdPartyUser = lstThirdParty(userInfo);
		if( thirdPartyUser.size()> 0 &&  thirdPartyUser.get(0).getNisngs()==Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			// ✅ Generate next seq for details
			int seqNoBioDataaccess = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biodataaccessrequestdetails' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioDataaccess++;
	
			if (sampleTypes != null && !sampleTypes.isEmpty()) {
				StringBuilder insertQueries = new StringBuilder();
	
				for (Object typeObj : sampleTypes) {
					int sampleType = Integer.parseInt(String.valueOf(typeObj));
	
					String str = "INSERT INTO biodataaccessrequestdetails ("
							+ "nbiodataaccessrequestdetailscode, nbiodataaccessrequestcode, nbioprojectcode, nproductcode, "
							+ "nuserrolecode, nusercode, ndeputyusercode, ndeputyuserrolecode, "
							+ "dtransactiondate, noffsetdtransactiondate, nsitecode, nstatus) " + "VALUES ("
							+ seqNoBioDataaccess + ", " + nbiodataaccessrequestcode + ", " + nbioProjectCode + ", "
							+ sampleType + ", " + userInfo.getNuserrole() + ", " + userInfo.getNusercode() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
	
					insertQueries.append(str).append("\n");
					seqNoBioDataaccess++;
				}
	
				// update sequence table
				String strSeqNoUpdateaccess = "UPDATE seqnobiobankmanagement SET nsequenceno=" + (seqNoBioDataaccess - 1)
						+ " " + "WHERE stablename='biodataaccessrequestdetails' AND nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
	
				insertQueries.append(strSeqNoUpdateaccess);
	
				jdbcTemplate.execute(insertQueries.toString());
				final List<Object> lstAuditAfter = new ArrayList<>();
				final List<String> multilingualIDList = new ArrayList<>();
				final String strAuditQry = auditQuery(nbiodataaccessrequestcode, -1, userInfo);
				List<BiodataAccessRequest> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditQry,
						new BiodataAccessRequest());
				lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
				lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_ADDREQUESTSAMPLES"));
				auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);
			}
	
			// ✅ After child insert, fetch parent + child list again
			return getBioDataRequest(inputMap, nbiodataaccessrequestcode, userInfo);
		}
		else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THIRDPARTYUSERONLYHAVERIGHTS",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	// Added by Vishakh (BGSI-287) for third party institution user issue
	public List<ThirdParty> lstThirdParty (UserInfo userInfo) throws Exception {
	
		String qry=" select tp.nisngs from thirdparty tp, thirdpartyusermapping tpm where tp.nthirdpartycode=tpm.nthirdpartycode "
				+ " and tpm.nusercode="+userInfo.getNusercode()+ " and tpm.nuserrolecode=" + userInfo.getNuserrole()
				+ " and tp.nisngs="+ Enumeration.TransactionStatus.NO.gettransactionstatus()
			    + " and tp.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			    + " and tp.nsitecode="+userInfo.getNmastersitecode()
			    + " and tpm.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			    + " and tpm.nsitecode="+userInfo.getNmastersitecode();

		return jdbcTemplate.query(qry, new ThirdParty());
	}
	
	// added by sujatha for validation in approve, retire & reject
	private List<Users> BioBankUsers(UserInfo userInfo) throws Exception {
		String qry="select u.nusercode from users u join userssite us on us.nusercode = u.nusercode "
				+ "join usermultirole umr on umr.nusersitecode = us.nusersitecode "
				+ "join userroleconfig urc on urc.nuserrolecode = umr.nuserrolecode "
				+ "where u.nusercode ="+userInfo.getNusercode()
				+ " and us.nsitecode ="+userInfo.getNtranssitecode()
				+ " and umr.nuserrolecode ="+userInfo.getNuserrole()
				+ " and (urc.nneedthirdpartyflow="+Enumeration.TransactionStatus.NO.gettransactionstatus()
				+ " and urc.nneedngsflow="+Enumeration.TransactionStatus.NO.gettransactionstatus()
				+ ") and u.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and us.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and umr.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and urc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();				
		return jdbcTemplate.query(qry, new Users());
	}
}
