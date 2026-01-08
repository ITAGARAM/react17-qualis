package com.agaramtech.qualis.credential.service.customercomplaint;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.credential.model.CustomerComplaint;
import com.agaramtech.qualis.credential.model.CustomerComplaintFile;
import com.agaramtech.qualis.credential.model.CustomerComplaintHistory;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class CustomerComplaintDAOImpl implements CustomerComplaintDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerComplaintDAOImpl.class);
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final FTPUtilityFunction ftpUtilityFunction;

	/**
	 * This method is used to retrieve list of all available customercomplaint for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         units
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getCustomerComplaint(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		outputMap.putAll(getStatus(inputMap, userInfo));
		outputMap.putAll((Map<String, Object>) getCustomerComplaintData(inputMap, userInfo).getBody());
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all available customercomplaint for
	 * the specified site.and based on Status
	 * 
	 * * @param inputMap a map containing filtering criteria such as fromDate,
	 * toDate, and status
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         units
	 * @throws Exception that are thrown from this DAO layer
	 */
	public Map<String, Object> getStatus(final Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> map = new HashMap<>();
		final String StrQuery = "SELECT ntranscode, coalesce(jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus  " + " FROM transactionstatus"
				+ " WHERE ntranscode IN (" + Enumeration.TransactionStatus.ALL.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.CLOSED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + ") and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ntranscode asc ";
		final List<TransactionStatus> lstTransactionStatus = (List<TransactionStatus>) jdbcTemplate.query(StrQuery,
				new TransactionStatus());
		if (lstTransactionStatus.size() > 0) {
			map.put("transactionStatus", lstTransactionStatus);
			map.put("defaultTransactionStatus", lstTransactionStatus.get(0));
			map.put("realStatus", lstTransactionStatus.get(0));
			inputMap.put("ntransactionstatus", lstTransactionStatus.get(0).getNtranscode());
		} else {
			map.put("transactionStatus", null);
			map.put("defaultTransactionStatus", null);
			map.put("realStatus", null);
		}
		return map;

	}

	/**
	 * This method provides access to the DAO layer to retrieve all available
	 * customercomplaint for a specific site, filtered by date range and status.
	 *
	 * @param inputMap a map containing filter criteria such as fromDate, toDate,
	 *                 and status
	 * @param userInfo an instance of [UserInfo] containing the logged-in user
	 *                 details and the nmasterSiteCode, which is the primary key of
	 *                 the site for which the complaints are to be fetched
	 *
	 * @return a ResponseEntity containing the list of customer complaint records
	 *         associated with the specified site
	 *
	 * @throws Exception if any errors occur in the DAO layer
	 */

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getCustomerComplaintData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
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
			outputMap.put("fromDate", mapObject.get("FromDateWOUTC"));
			outputMap.put("toDate", mapObject.get("ToDateWOUTC"));

			outputMap.put("realFromDate", mapObject.get("FromDateWOUTC"));
			outputMap.put("realToDate", mapObject.get("ToDateWOUTC"));
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);
			outputMap.put("fromDate", fromDateUI);
			outputMap.put("toDate", toDateUI);
			outputMap.put("realFromDate", fromDateUI);
			outputMap.put("realToDate", toDateUI);
			fromDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
			toDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		}
		LocalDateTime fromDateTime = LocalDateTime.parse(fromDate);
		LocalDateTime toDateTime = LocalDateTime.parse(toDate);
		int ntransactionstatus = 0;
		String filterStatusQuery = "";
		if (inputMap.containsKey("ntranscode")) {
			ntransactionstatus = (int) inputMap.get("ntranscode");
		}
		if (ntransactionstatus > 0) {
			filterStatusQuery = "and ch.ntransactionstatus =" + ntransactionstatus + "" + " ";
		}
		String strQuery = "SELECT " + " c.ncustomercomplaintcode, " + " COALESCE(TO_CHAR(c.dcomplaintdate,'"
				+ userInfo.getSsitedate() + "'),'-') AS sreceiverdate, " + " c.ntzcomplaintdate, "
				+ " c.noffsetdcomplaintdate, " + " c.sreceivedfrom, " + " c.scontactnumber, " + " c.semail, "
				+ " c.scomplaintdetails, " + " c.nregioncode, region.ssitename AS sregionname, "
				+ " c.ndistrictcode, district.ssitename AS sdistrictname, "
				+ " c.ncitycode, city.ssitename AS scityname, " + " c.nvillagecode, v.svillagename AS svillagename, "
				+ " c.slocation, " + " c.slatitude, " + " c.slongitude, " + " f.ncolorcode, "
				+ " cl.scolorhexcode,ch.ntransactionstatus, " + " COALESCE( "
				+ "   ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ "   ts1.jsondata->'stransdisplaystatus'->>'en-US' " + " ) AS stransdisplaystatus "
				+ "FROM customercomplaint c " + "JOIN customercomplainthistory ch "
				+ "   ON ch.ncustomercomplaintcode = c.ncustomercomplaintcode " + "LEFT JOIN site region "
				+ "   ON c.nregioncode = region.nsitecode " + "LEFT JOIN site district "
				+ "   ON c.ndistrictcode = district.nsitecode " + "LEFT JOIN site city "
				+ "   ON c.ncitycode = city.nsitecode " + "LEFT JOIN villages v "
				+ "   ON c.nvillagecode = v.nvillagecode " + "JOIN transactionstatus ts1 "
				+ "   ON ch.ntransactionstatus = ts1.ntranscode " + "JOIN formwisestatuscolor f "
				+ "   ON ch.ntransactionstatus = f.ntranscode " + "JOIN colormaster cl "
				+ "   ON cl.ncolorcode = f.ncolorcode " + "WHERE ch.ncustomercomplainthistorycode = ANY ( "
				+ "  SELECT MAX(ch2.ncustomercomplainthistorycode) " + "  FROM customercomplainthistory ch2 "
				+ "          JOIN customercomplaint c2 "
				+ "  ON c2.ncustomercomplaintcode = ch2.ncustomercomplaintcode " + "  WHERE c2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "   AND ch2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "   AND c2.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + "   AND ch2.nsitecode = " + userInfo.getNtranssitecode() + " "
				+ "   GROUP BY c2.ncustomercomplaintcode " + "      ) " + "  AND ch.dmodifieddate BETWEEN '"
				+ fromDateTime + "' AND '" + toDateTime + "' " + "  AND c.nsitecode = " + userInfo.getNtranssitecode()
				+ " " + "  AND c.nstatus   = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "  AND f.nformcode = " + userInfo.getNformcode() + " " + "  AND ch.nstatus  = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND ch.nsitecode= "
				+ userInfo.getNtranssitecode() + " " + "  AND region.nstatus   = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND district.nstatus   = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND city.nstatus   = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND ts1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND f.nstatus   = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND f.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + "  AND cl.nstatus  = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + filterStatusQuery + " "
				+ "ORDER BY c.ncustomercomplaintcode DESC";
		List<CustomerComplaint> lstCustomerComplaint = jdbcTemplate.query(strQuery, new CustomerComplaint());
		if (!lstCustomerComplaint.isEmpty()) {
			outputMap.put("customerComplaintRecord", lstCustomerComplaint);
			outputMap.put("selectedCustomerComplaint", lstCustomerComplaint.get(0));
			outputMap.putAll(
					(Map<String, Object>) getComplaintHistory(lstCustomerComplaint.get(0).getNcustomercomplaintcode(),
							userInfo).getBody());
			outputMap.putAll((Map<String, Object>) getCustomerComplaintFile(
					lstCustomerComplaint.get(0).getNcustomercomplaintcode(), userInfo).getBody());
		} else {
			outputMap.put("customerComplaintRecord", null);
			outputMap.put("selectedCustomerComplaint", null);
			outputMap.put("complaintHistory", null);
			outputMap.put("customerComplaintFile", null);
		}
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method will access the DAO layer that is used to retrieve active
	 * customercomplainthistory object based on the specified ncustomercomplaintode.
	 * 
	 * @param ncustomercomplaintCode [int] primary key of CustomerComplaint object
	 * @param userInfo               [UserInfo] holding logged in user details based
	 *                               on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         CustomerComplaint object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getComplaintHistory(final int ncustomercomplaintcode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String strquery = "SELECT cc.ncustomercomplaintcode,cch.ncustomercomplainthistorycode ,"
				+ "cc.sreceivedfrom, " + "TO_CHAR(cch.dtransactiondate,'" + userInfo.getSpgdatetimeformat()
				+ "') AS stransactiondate, " + "COALESCE(cch.sremarks, '-') AS sremarks, TO_CHAR(cch.dmodifieddate,'"
				+ userInfo.getSpgdatetimeformat() + "') AS smodifieddate,"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US' AS stransactionstatus " + "FROM customercomplaint cc "
				+ "JOIN customercomplainthistory cch " + "ON cch.ncustomercomplaintcode = cc.ncustomercomplaintcode "
				+ "JOIN transactionstatus ts " + "ON cch.ntransactionstatus = ts.ntranscode "
				+ "WHERE cc.ncustomercomplaintcode = " + ncustomercomplaintcode + " AND cc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cc.nsitecode = "
				+ userInfo.getNtranssitecode() + " AND cch.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cch.nsitecode = "
				+ userInfo.getNtranssitecode() + " AND ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ORDER BY cch.dtransactiondate ASC";
		List<Map<String, Object>> complaintHistory = jdbcTemplate.queryForList(strquery);
		outputMap.put("complaintHistory", complaintHistory);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method provides access to the DAO layer to retrieve all the available
	 * regions
	 *
	 * @param userInfo [UserInfo] contains the details of the logged-in user,
	 *                 including the nmasterSiteCode [int], which represents the
	 *                 primary key of the site for which the region list is to be
	 *                 fetched.
	 * @return a ResponseEntity containing the list of region records associated
	 * 
	 * @throws Exception if any errors occur in the DAO layer during data retrieval.
	 */

	@Override
	public ResponseEntity<Object> getRegion(final UserInfo userInfo) throws Exception {

		String configFilter = "WITH RECURSIVE selected_config AS ( " + " SELECT * FROM sitehierarchyconfig sc "
				+ " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ "   AND jsonb_path_exists(sc.jsondata, '$.** ? (@.item.selectedNodeDetail.nsitecode == "
				+ userInfo.getNtranssitecode() + ")') " + " ORDER BY sc.dmodifieddate DESC LIMIT 1 " + " ) ";

		// modified by sujatha ATE_274 23-09-2025 to get one more field called
		// nsitehierarchyconfigcode
		String strCurrent = configFilter + " , current_site AS ( "
				+ " SELECT s.nsitecode, s.ssitename, 'Current' AS srelation " + " FROM site s "
				+ " WHERE s.nsitecode = " + userInfo.getNtranssitecode() + " ) " + " , json_expanded AS ( "
				+ " SELECT DISTINCT (elem->>'nsitecode')::int AS nsitecode, "
				+ " elem->>'ssitetypename' AS ssitetypename, " + " (elem->>'nhierarchicalorderno')::int AS nlevel, "
				+ " sc.nsitehierarchyconfigcode FROM selected_config sc, "
				+ " jsonb_path_query(sc.jsondata, '$.**.item.selectedNodeDetail') elem " + " ) "
				+ " SELECT c.nsitecode, c.ssitename, c.srelation, j.ssitetypename, j.nlevel, j.nsitehierarchyconfigcode "
				+ " FROM current_site c " + " JOIN json_expanded j ON j.nsitecode = c.nsitecode ";

		final List<CustomerComplaint> currentList = jdbcTemplate.query(strCurrent, new CustomerComplaint());
		String strParent = configFilter + " , site_hierarchy AS ( " + "    SELECT "
				+ " (sc.jsondata->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " sc.jsondata->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " sc.jsondata->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (sc.jsondata->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " (sc.jsondata->>'parentKey')::int AS parentkey, " + " sc.jsondata->'nodes' AS children "
				+ "    FROM selected_config sc " + " UNION ALL " + " SELECT "
				+ " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (child->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " (child->>'parentKey')::int AS parentkey, " + " child->'nodes' AS children "
				+ " FROM site_hierarchy sh, " + " jsonb_array_elements(sh.children) AS child " + " ) , parents AS ( "
				+ "    SELECT sh.* " + "    FROM site_hierarchy sh " + "    WHERE sh.nsitecode = "
				+ userInfo.getNtranssitecode() + "    UNION ALL " + "    SELECT sh.* " + "    FROM site_hierarchy sh "
				+ "    JOIN parents p ON sh.nsitecode = p.parentkey " + " ) SELECT " + " nsitecode, "
				+ " split_part(ssitename, '(', 1) AS ssitename, " + " ssitetypename, " + " parentkey, " + " nlevel "
				+ " FROM parents " + " WHERE nsitecode != " + userInfo.getNtranssitecode() + " ORDER BY nsitecode ";

		final List<CustomerComplaint> parentList = jdbcTemplate.query(strParent, new CustomerComplaint());

		// Child sites query
		// modified by sujatha ATE_274 23-09-2025 to get one more field called
		// nsitehierarchyconfigcode
		String strChild = configFilter + " , site_hierarchy AS ( " + "    SELECT "
				+ " elem->'item'->'selectedNodeDetail'->>'nsitecode' AS nsitecode, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (elem->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " elem->>'parentKey' AS parentkey, " + " elem->'nodes' AS children, sc.nsitehierarchyconfigcode "
				+ " FROM selected_config sc, " + " jsonb_array_elements(sc.jsondata->'nodes') AS elem " + " UNION ALL "
				+ " SELECT " + " child->'item'->'selectedNodeDetail'->>'nsitecode' AS nsitecode, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (child->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " child->>'parentKey' AS parentkey, " + " child->'nodes' AS children, sh.nsitehierarchyconfigcode "
				+ " FROM site_hierarchy sh, " + " jsonb_array_elements(sh.children) AS child " + " ) SELECT "
				+ " nsitecode::int, " + " split_part(ssitename, '(', 1) AS ssitename, " + " ssitetypename, "
				+ " parentkey::int, " + " nlevel, nsitehierarchyconfigcode " + " FROM site_hierarchy "
				+ " WHERE parentkey::int = " + userInfo.getNtranssitecode()
				+ "   OR parentkey::int IN ( SELECT nsitecode::int FROM site_hierarchy WHERE parentkey::int = "
				+ userInfo.getNtranssitecode() + " ) " + " ORDER BY nsitecode::int ";

		final List<CustomerComplaint> childList = jdbcTemplate.query(strChild, new CustomerComplaint());

		final Map<String, Object> outputMap = new LinkedHashMap<>();
		if (!currentList.isEmpty()) {
			outputMap.put("currentList", currentList);
			outputMap.put("parentList", parentList);
			outputMap.put("childList", childList);
		} else {
			outputMap.put("currentList", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method will access the DAO layer that is used to get all the available
	 * Region based on specific Region
	 * 
	 * @param nregioncode Holding the current Region record
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return a response entity which holds the list of Districts with respect to
	 *         Region
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> getDistrict(final int ndistrictCode, final UserInfo userInfo) throws Exception {
		String strTaluka = "WITH RECURSIVE site_hierarchy AS ( " + " SELECT "
				+ " (elem->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (elem->>'parentKey')::int AS parentkey, " + " elem->'nodes' AS children "
				+ " FROM sitehierarchyconfig sc, jsonb_array_elements(sc.jsondata->'nodes') AS elem "
				+ " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " UNION ALL " + " SELECT "
				+ " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (child->>'parentKey')::int AS parentkey, " + " child->'nodes' AS children "
				+ " FROM site_hierarchy sh, jsonb_array_elements(sh.children) AS child " + ") "
				+ "SELECT nsitecode, split_part(ssitename, '(', 1) AS ssitename, ssitetypename, parentkey "
				+ "FROM site_hierarchy " + "WHERE parentkey = " + ndistrictCode + " ORDER BY nsitecode;";

		final List<CustomerComplaint> talukaList = (List<CustomerComplaint>) jdbcTemplate.query(strTaluka,
				new CustomerComplaint());

		final Map<String, Object> outputMap = new LinkedHashMap<>();
		if (!talukaList.isEmpty()) {
			outputMap.put("talukaList", talukaList);
		} else {
			outputMap.put("talukaList", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method provides access to the DAO layer to retrieve all the available
	 * districts for a given City within a specific District.
	 *
	 * @param nregioncode an integer representing the unique region code for which
	 *                    the district list is to be fetched.
	 * @param userInfo    [UserInfo] object containing the logged-in user details,
	 *                    including nmasterSiteCode [int], which is the primary key
	 *                    of the site object.
	 * @return a ResponseEntity containing the list of district records.
	 * @throws Exception if any errors occur in the DAO layer during data retrieval.
	 */

	@Override
	public ResponseEntity<Object> getCity(final int nprimarykey, final UserInfo userInfo) throws Exception {
		// modified by sujatha ATE_274 23-09-2025 to get one more field called
		// nsitehierarchyconfigcode
		String strVillage = "WITH RECURSIVE site_hierarchy AS ( " + " SELECT "
				+ " (elem->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (elem->>'parentKey')::int AS parentkey, "
				+ " elem->'nodes' AS children, sc.nsitehierarchyconfigcode "
				+ " FROM sitehierarchyconfig sc, jsonb_array_elements(sc.jsondata->'nodes') AS elem "
				+ " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " UNION ALL " + " SELECT "
				+ " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (child->>'parentKey')::int AS parentkey, "
				+ " child->'nodes' AS children, sh.nsitehierarchyconfigcode "
				+ " FROM site_hierarchy sh, jsonb_array_elements(sh.children) AS child " + ") "
				+ "SELECT nsitecode, split_part(ssitename, '(', 1) AS ssitename, ssitetypename, parentkey, nsitehierarchyconfigcode "
				+ "FROM site_hierarchy " + "WHERE parentkey = " + nprimarykey + " ORDER BY nsitecode;";
		final List<CustomerComplaint> villageList = (List<CustomerComplaint>) jdbcTemplate.query(strVillage,
				new CustomerComplaint());
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		if (!villageList.isEmpty()) {
			outputMap.put("villageList", villageList);
		} else {
			outputMap.put("villageList", null);
		}
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method will access the DAO layer that is used to get all the available
	 * Village records with respect to City.
	 *
	 * @param ncitycode Holding the City record (foreign key reference for village
	 *                  lookup)
	 * @param userInfo  [UserInfo] holding logged-in user details including
	 *                  nmasterSiteCode [int], which is the primary key of the site
	 *                  object for which the list is to be fetched
	 * @return a response entity which holds the list of village records with
	 *         respect to the given city
	 * @throws Exception if any error occurs in the DAO layer
	 */

	// added by sujatha ATE_274 to get villages with one more check based on
	// nsitehierarchyconfigcode
	@Override
	public ResponseEntity<Object> getVillage(final int nsitehierarchyconfigcode, final int nprimarykey,
			final UserInfo objUserInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		// modified by sujatha V ATE_274 for getting villages without using
		// samplelocation table and get only based on sitehierarchyconfigcode
		String strQuery = "select v.nvillagecode,v.svillagename from villages v " + " where v.nnodesitecode="
				+ nprimarykey + " and v.nsitehierarchyconfigcode=" + nsitehierarchyconfigcode + " and v.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final List<CustomerComplaint> villageList = jdbcTemplate.query(strQuery, new CustomerComplaint());

		outputMap.put("villageList", villageList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method will access the DAO layer that is used to retrieve active
	 * customercomplainthistory object based on the specified sreceivedfrom.
	 * 
	 * @param sreceivedfrom [String] in CustomerComplaint object
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         CustomerComplaint object
	 * @throws Exception that are thrown in the DAO layer
	 */

	private CustomerComplaint getCustomerComplaintByName(final CustomerComplaint objCustomerComplaint,
			final UserInfo userInfo) throws Exception {
		//added stringUtilityFunction.replaceQuote() for jira -swsm-98
		String strQuery = "SELECT ncustomercomplaintcode, nregioncode, ndistrictcode, ncitycode, nvillagecode "
				+ "FROM customercomplaint " + "WHERE sreceivedfrom = N'"
				+ stringUtilityFunction.replaceQuote(objCustomerComplaint.getSreceivedfrom()) + "' "
				+ "AND nregioncode = " + objCustomerComplaint.getNregioncode() + " " + "AND ndistrictcode = "
				+ objCustomerComplaint.getNdistrictcode() + " " + "AND ncitycode = "
				+ objCustomerComplaint.getNcitycode() + " " + "AND nvillagecode = "
				+ objCustomerComplaint.getNvillagecode() + " " + "AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND nsitecode = "
				+ userInfo.getNtranssitecode() + " AND ncustomercomplaintcode <> "
				+ objCustomerComplaint.getNcustomercomplaintcode();

		return (CustomerComplaint) jdbcUtilityFunction.queryForObject(strQuery, CustomerComplaint.class, jdbcTemplate);
	}

	/**
	 * This method is responsible for creating a new CustomerComplaint record by
	 * accessing the DAO layer.
	 * 
	 * @param objmap A map containing:
	 * 
	 *               Customer complaint details (e.g., fromDate, toDate, status,
	 *               etc.) UserInfo object holding logged-in user details
	 *               nmasterSiteCode [int] representing the primary key of the site
	 *               for which the record is to be created
	 * 
	 * @return ResponseEntity<Object> containing the status and the created Customer
	 *         Complaint object (or error details if creation fails).
	 * @throws Exception if any error occurs in the DAO layer while creating the
	 *                   record
	 */
	@Override
	public ResponseEntity<Object> createCustomerComplaint(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		CustomerComplaint objCustomerComplaint = objMapper.convertValue(inputMap.get("customercomplaint"),
				CustomerComplaint.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		final CustomerComplaint existingComplaint = getCustomerComplaintByName(objCustomerComplaint, userInfo);
		if (existingComplaint == null) {
			String lockCustomerComplaint = "lock table lockcustomercomplaint "
					+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
			jdbcTemplate.execute(lockCustomerComplaint);

			final List<String> multilingualIdList = new ArrayList<>();
			final List<Object> saveCustomerComplaintList = new ArrayList<>();
			String sequenceNoQuery = "select nsequenceno from seqnocredentialmanagement where stablename ='customercomplaint'"
					+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class) + 1;

			objCustomerComplaint.setScomplaintdate(objCustomerComplaint.getDcomplaintdate() == null ? null
					: dateUtilityFunction.instantDateToStringWithFormat(objCustomerComplaint.getDcomplaintdate(),
							userInfo.getSsitedatetime()));

			String insertComplaintQuery = "INSERT INTO customercomplaint ("
					+ "ncustomercomplaintcode, dcomplaintdate, ntzcomplaintdate, noffsetdcomplaintdate, "
					+ "sreceivedfrom, scontactnumber, semail, scomplaintdetails, ncentralsitecode," // added by sujatha
																									// ATE_274
																									// 26-09-2025 to
																									// insert newly
																									// added field
																									// called
																									// ncentralsitecode
					+ "nregioncode, ndistrictcode, ncitycode, nvillagecode, "
					+ "slocation, slatitude, slongitude, dmodifieddate, nsitecode, nstatus) " + "VALUES (" + nsequenceNo
					+ ", '" + objCustomerComplaint.getDcomplaintdate() + "', '"
					+ objCustomerComplaint.getNtzcomplaintdate() + "', '-1', '"
					+ stringUtilityFunction.replaceQuote(objCustomerComplaint.getSreceivedfrom()) + "', '"
					+ stringUtilityFunction.replaceQuote(objCustomerComplaint.getScontactnumber()) + "', '"
					+ stringUtilityFunction.replaceQuote(objCustomerComplaint.getSemail()) + "', '"
					+ stringUtilityFunction.replaceQuote(objCustomerComplaint.getScomplaintdetails()) + "', "
					+ objCustomerComplaint.getNcentralsitecode() + ", " // added by sujatha ATE_274 26-09-2025 to insert
																		// newly added field called ncentralsitecode
					+ objCustomerComplaint.getNregioncode() + ", " + objCustomerComplaint.getNdistrictcode() + ", "
					+ objCustomerComplaint.getNcitycode() + ", " + objCustomerComplaint.getNvillagecode() + ", '"
					+ stringUtilityFunction.replaceQuote(objCustomerComplaint.getSlocation()) + "', '"
					+ stringUtilityFunction.replaceQuote(objCustomerComplaint.getSlatitude()) + "', '"
					+ stringUtilityFunction.replaceQuote(objCustomerComplaint.getSlongitude()) + "', '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

			jdbcTemplate.execute(insertComplaintQuery);

			jdbcTemplate.execute("update seqnocredentialmanagement set nsequenceno = " + nsequenceNo
					+ " where stablename='customercomplaint'");

			String historySeqQuery = "select nsequenceno from seqnocredentialmanagement where stablename ='customercomplainthistory'"
					+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			int historySeqNo = jdbcTemplate.queryForObject(historySeqQuery, Integer.class) + 1;

			String insertHistoryQuery = "INSERT INTO customercomplainthistory ("
					+ "ncustomercomplainthistorycode, ncustomercomplaintcode, ntransactionstatus, "
					+ "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, "
					+ "dtransactiondate, ntztransactiondate, noffsetdtransactiondate, dmodifieddate,nsitecode, nstatus) "
					+ "VALUES (" + historySeqNo + ", " + nsequenceNo + ", "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", " + userInfo.getNusercode() + ", "
					+ userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", "
					+ userInfo.getNdeputyuserrole() + ", '" + objCustomerComplaint.getDcomplaintdate() + "', "
					+ userInfo.getNtimezonecode() + ","
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ," + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

			jdbcTemplate.execute(insertHistoryQuery);

			jdbcTemplate.execute("update seqnocredentialmanagement set nsequenceno = " + historySeqNo
					+ " where stablename='customercomplainthistory'");
			objCustomerComplaint.setNcustomercomplaintcode(nsequenceNo);
			saveCustomerComplaintList.add(objCustomerComplaint);

			multilingualIdList.add("IDS_ADDCUSTOMERCOMPLAINT");
			auditUtilityFunction.fnInsertAuditAction(saveCustomerComplaintList, 1, null, multilingualIdList, userInfo);
			return getCustomerComplaintData(inputMap, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method will access the DAO layer that is used to retrieve active
	 * customercomplainthistory object based on the specified ntransactionstatus and
	 * ncustomercomplaintcode .
	 * 
	 * @param objCustomerComplaint [CustomerComplaint] in CustomerComplaint
	 * @param userInfo             [UserInfo] holding logged in user details based
	 *                             on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         CustomerComplaint object
	 * @throws Exception that are thrown in the DAO layer
	 */

	private CustomerComplaint getCustomerComplaintStatus(final CustomerComplaint objCustomerComplaint,
			final UserInfo userInfo) throws Exception {
		final String strQuery = "select ch.ntransactionstatus from 	customercomplainthistory ch where ch.ncustomercomplainthistorycode= ANY (Select Max "
				+ " (ch2.ncustomercomplainthistorycode) FROM  customercomplainthistory ch2 "
				+ " JOIN customercomplaint c2 ON c2.ncustomercomplaintcode = ch2.ncustomercomplaintcode "
				+ " WHERE ch2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and c2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ch2.nsitecode = " + userInfo.getNtranssitecode() + " and c2.nsitecode= "
				+ userInfo.getNtranssitecode() + " Group By c2.ncustomercomplaintcode) and ch.ncustomercomplaintcode = "
				+ objCustomerComplaint.getNcustomercomplaintcode() + " and ch.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ch.nsitecode = "
				+ userInfo.getNtranssitecode();
		return (CustomerComplaint) jdbcUtilityFunction.queryForObject(strQuery, CustomerComplaint.class, jdbcTemplate);
	}

	/**
	 * This method to mark a record as initiated in the customercomplainthistory
	 * table.
	 *
	 * @param objmap a map containing: - fromDate, toDate, and status details of the
	 *               complaint history - customercomplainthistory details - userInfo
	 *               (logged-in user details) - nmasterSiteCode (primary key of the
	 *               site object for which the list is fetched)
	 * @return ResponseEntity object containing the response status and data of the
	 *         updated customercomplainthistory record
	 * @throws Exception if any error occurs in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> initiateCustomerComplaint(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		CustomerComplaint objCustomerComplaint = objMapper.convertValue(inputMap.get("customercomplaint"),
				CustomerComplaint.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		CustomerComplaint customerComplaintstatus = getCustomerComplaintStatus(objCustomerComplaint, userInfo);

		// to avoid null pointer exception
		if (customerComplaintstatus != null && customerComplaintstatus
				.getNtransactionstatus() == Enumeration.TransactionStatus.INITIATED.gettransactionstatus()) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYINITIATED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}

		else {

			final CustomerComplaint customerComplaint = getActiveCustomerComplaintById(
					objCustomerComplaint.getNcustomercomplaintcode(), userInfo);
			if (customerComplaint == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

				final CustomerComplaint existingComplaint = getCustomerComplaintByName(objCustomerComplaint, userInfo);
				final List<String> multilingualIdList = new ArrayList<>();
				final List<Object> saveCustomerComplaintList = new ArrayList<>();
				if (existingComplaint == null) {
					String lockHistory = "lock table lockcustomercomplainthistory "
							+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
					jdbcTemplate.execute(lockHistory);
					String historySeqQuery = "select nsequenceno from seqnocredentialmanagement where stablename ='customercomplainthistory'"
							+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					int historySeqNo = jdbcTemplate.queryForObject(historySeqQuery, Integer.class) + 1;
					String insertHistoryQuery = "INSERT INTO customercomplainthistory ("
							+ "ncustomercomplainthistorycode, ncustomercomplaintcode, ntransactionstatus, "
							+ "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, "
							+ "dtransactiondate, ntztransactiondate, noffsetdtransactiondate,sremarks,dmodifieddate, nsitecode, nstatus) "
							+ "VALUES (" + historySeqNo + ", " + objCustomerComplaint.getNcustomercomplaintcode() + ", "
							+ Enumeration.TransactionStatus.INITIATED.gettransactionstatus() + ", "
							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", '"
							+ objCustomerComplaint.getDtransactiondate() + "', " + userInfo.getNtimezonecode() + ","
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
							+ objCustomerComplaint.getSremarks() + "'  ,'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ," + userInfo.getNtranssitecode()
							+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(insertHistoryQuery);
					jdbcTemplate.execute("update seqnocredentialmanagement set nsequenceno = " + historySeqNo
							+ " where stablename='customercomplainthistory'");
					objCustomerComplaint.setStransactiondate(objCustomerComplaint.getDtransactiondate() == null ? null
							: dateUtilityFunction.instantDateToStringWithFormat(
									objCustomerComplaint.getDtransactiondate(), userInfo.getSsitedatetime()));
					objCustomerComplaint.setNcustomercomplaintcode(historySeqNo);
					saveCustomerComplaintList.add(objCustomerComplaint);
					multilingualIdList.add("IDS_INITIATECUSTOMERCOMPLAINT");
					auditUtilityFunction.fnInsertAuditAction(saveCustomerComplaintList, 1, null, multilingualIdList,
							userInfo);
					return getCustomerComplaintDatas(inputMap, userInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
		}
	}

	/**
	 * This method provides access to the DAO layer to retrieve all available
	 * customercomplaint for a specific site, filtered by date range and status.
	 *
	 * @param inputMap a map containing filter criteria such as fromDate, toDate,
	 *                 and status
	 * @param userInfo an instance of [UserInfo] containing the logged-in user
	 *                 details and the nmasterSiteCode, which is the primary key of
	 *                 the site for which the complaints are to be fetched
	 *
	 * @return a ResponseEntity containing the list of customercomplaint records
	 *         associated with the specified site
	 *
	 * @throws Exception if any errors occur in the DAO layer
	 */
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getCustomerComplaintDatas(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		CustomerComplaint objCustomerComplaint = objMapper.convertValue(inputMap.get("customercomplaint"),
				CustomerComplaint.class);
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
			outputMap.put("fromDate", mapObject.get("FromDateWOUTC"));
			outputMap.put("toDate", mapObject.get("ToDateWOUTC"));
			outputMap.put("realFromDate", mapObject.get("FromDateWOUTC"));
			outputMap.put("realToDate", mapObject.get("ToDateWOUTC"));
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);
			outputMap.put("fromDate", fromDateUI);
			outputMap.put("toDate", toDateUI);
			outputMap.put("realFromDate", fromDateUI);
			outputMap.put("realToDate", toDateUI);
			fromDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
			toDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		}

		String strQuery = "SELECT " + " c.ncustomercomplaintcode, " + " COALESCE(TO_CHAR(c.dcomplaintdate,'"
				+ userInfo.getSsitedate() + "'),'-') AS sreceiverdate, " + " c.ntzcomplaintdate, "
				+ " c.noffsetdcomplaintdate, " + " c.sreceivedfrom, " + " c.scontactnumber, " + " c.semail, "
				+ " c.scomplaintdetails, " + " c.nregioncode, region.ssitename AS sregionname, "
				+ " c.ndistrictcode, district.ssitename AS sdistrictname, "
				+ " c.ncitycode, city.ssitename AS scityname, " + " c.nvillagecode, v.svillagename AS svillagename, "
				+ " c.slocation, " + " c.slatitude, " + " c.slongitude, " + " f.ncolorcode, "
				+ " cl.scolorhexcode,ch.ntransactionstatus, " + " COALESCE( "
				+ "   ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ "   ts1.jsondata->'stransdisplaystatus'->>'en-US' " + " ) AS stransdisplaystatus "
				+ "FROM customercomplaint c " + "JOIN customercomplainthistory ch "
				+ "   ON ch.ncustomercomplaintcode = c.ncustomercomplaintcode " + "LEFT JOIN site region "
				+ "   ON c.nregioncode = region.nsitecode " + "LEFT JOIN site district "
				+ "   ON c.ndistrictcode = district.nsitecode " + "LEFT JOIN site city "
				+ "   ON c.ncitycode = city.nsitecode " + "LEFT JOIN villages v "
				+ "   ON c.nvillagecode = v.nvillagecode " + "JOIN transactionstatus ts1 "
				+ "   ON ch.ntransactionstatus = ts1.ntranscode " + "JOIN formwisestatuscolor f "
				+ "   ON ch.ntransactionstatus = f.ntranscode " + "JOIN colormaster cl "
				+ "   ON cl.ncolorcode = f.ncolorcode " + "WHERE ch.ncustomercomplainthistorycode = ANY ( "
				+ "  SELECT MAX(ch2.ncustomercomplainthistorycode) " + "  FROM customercomplainthistory ch2 "
				+ "          JOIN customercomplaint c2 "
				+ "  ON c2.ncustomercomplaintcode = ch2.ncustomercomplaintcode " + "  WHERE c2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "   AND ch2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "   AND c2.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + "   AND ch2.nsitecode = " + userInfo.getNtranssitecode() + " "
				+ "   GROUP BY c2.ncustomercomplaintcode " + "      ) " + "  AND c.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + "  AND c.nstatus   = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND f.nformcode = "
				+ userInfo.getNformcode() + " " + "  AND ch.nstatus  = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND ch.nsitecode= "
				+ userInfo.getNtranssitecode() + " " + "  AND region.nstatus   = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND district.nstatus   = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND city.nstatus   = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND ts1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND f.nstatus   = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND f.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + "  AND cl.nstatus  = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.ncustomercomplaintcode="
				+ objCustomerComplaint.getNcustomercomplaintcode() // added by sujatha ATE_274 SWSM-69 Initiated record
																	// is still in Draft status in specific scenario.
				+ " ORDER BY c.ncustomercomplaintcode DESC";

		List<CustomerComplaint> lstCustomerComplaint = jdbcTemplate.query(strQuery, new CustomerComplaint());

		if (!lstCustomerComplaint.isEmpty()) {
			outputMap.put("selectedCustomerComplaint", lstCustomerComplaint.get(0));
			outputMap.putAll(
					(Map<String, Object>) getComplaintHistory(lstCustomerComplaint.get(0).getNcustomercomplaintcode(),
							userInfo).getBody());
			outputMap.putAll((Map<String, Object>) getCustomerComplaintFile(
					lstCustomerComplaint.get(0).getNcustomercomplaintcode(), userInfo).getBody());
		} else {
			outputMap.put("selectedCustomerComplaint", null);
			outputMap.put("complaintHistory", null);
			outputMap.put("customerComplaintFile", null);
		}
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	/**
	 * This service implementation declaration will access the DAO layer to update a
	 * record in the customercomplainthistory table by marking the complaint as
	 * closed.
	 * 
	 * @param objmap a map containing: - customercomplainthistory details to be
	 *               updated - userInfo [UserInfo] holding logged-in user details -
	 *               nmasterSiteCode [int] primary key of the site object where the
	 *               complaint belongs
	 * 
	 * @return ResponseEntity object holding the response status and the updated
	 *         customercomplainthistory data
	 * 
	 * @throws Exception if any error occurs in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> closeCustomerComplaint(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		CustomerComplaint objCustomerComplaint = objMapper.convertValue(inputMap.get("customercomplaint"),
				CustomerComplaint.class);
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		CustomerComplaint customerComplaintstatus = getCustomerComplaintStatus(objCustomerComplaint, userInfo);

		// to avoid null pointer exception
		if (customerComplaintstatus != null && customerComplaintstatus
				.getNtransactionstatus() == Enumeration.TransactionStatus.CLOSED.gettransactionstatus()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYINITIATED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			final CustomerComplaint existingComplaint = getCustomerComplaintByName(objCustomerComplaint, userInfo);
			final List<String> multilingualIdList = new ArrayList<>();
			final List<Object> saveCustomerComplaintList = new ArrayList<>();
			if (existingComplaint == null) {
				String lockHistory = "lock table lockcustomercomplainthistory "
						+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				jdbcTemplate.execute(lockHistory);
				String historySeqQuery = "select nsequenceno from seqnocredentialmanagement where stablename ='customercomplainthistory'"
						+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				int historySeqNo = jdbcTemplate.queryForObject(historySeqQuery, Integer.class) + 1;
				String insertHistoryQuery = "INSERT INTO customercomplainthistory ("
						+ "ncustomercomplainthistorycode, ncustomercomplaintcode, ntransactionstatus, "
						+ "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, "
						+ "dtransactiondate, ntztransactiondate, noffsetdtransactiondate,sremarks,dmodifieddate, nsitecode, nstatus) "
						+ "VALUES (" + historySeqNo + ", " + objCustomerComplaint.getNcustomercomplaintcode() + ", "
						+ Enumeration.TransactionStatus.CLOSED.gettransactionstatus() + ", " + userInfo.getNusercode()
						+ ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", "
						+ userInfo.getNdeputyuserrole() + ", '" + objCustomerComplaint.getDtransactiondate() + "', "
						+ userInfo.getNtimezonecode() + ","
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
						+ objCustomerComplaint.getSremarks() + "'  ,'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' ," + userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertHistoryQuery);
				jdbcTemplate.execute("update seqnocredentialmanagement set nsequenceno = " + historySeqNo
						+ " where stablename='customercomplainthistory'");
				objCustomerComplaint.setStransactiondate(objCustomerComplaint.getDtransactiondate() == null ? null
						: dateUtilityFunction.instantDateToStringWithFormat(objCustomerComplaint.getDtransactiondate(),
								userInfo.getSsitedatetime()));
				objCustomerComplaint.setNcustomercomplaintcode(historySeqNo);
				saveCustomerComplaintList.add(objCustomerComplaint);
				multilingualIdList.add("IDS_CLOSEDCUSTOMERCOMPLAINTDATE");
				auditUtilityFunction.fnInsertAuditAction(saveCustomerComplaintList, 1, null, multilingualIdList,
						userInfo);
				return getCustomerComplaintDatas(inputMap, userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method will access the DAO layer that is used to retrieve active
	 * customercomplainthistory object based on the specified ncustomercomplaintode.
	 * 
	 * @param ncustomercomplaintCode [int] primary key of CustomerComplaint object
	 * @param userInfo               [UserInfo] holding logged in user details based
	 *                               on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         CustomerComplaint object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public CustomerComplaint getActiveCustomerComplaintById(final int ncustomercomplaintcode, final UserInfo userInfo)
			throws Exception {

		final String query = "SELECT cc.ncustomercomplaintcode, " + "cc.dcomplaintdate, cc.sreceivedfrom, "
				+ "cc.scontactnumber, cc.semail, cc.dmodifieddate, "
				+ "cc.nsitecode,  cc.ncentralsitecode, central.ssitename as scentralsitename, " // added by sujatha
																								// ATE_274 26-09-2025 to
																								// get newly added field
																								// called
																								// ncentralsitecode
				+ "cc.nregioncode, region.ssitename AS sregionname, "
				+ "cc.ndistrictcode, district.ssitename AS sdistrictname, "
				+ "cc.ncitycode, city.ssitename AS scityname, " + "COALESCE(ts1.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', "
				+ "ts1.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus, "
				+ "cc.nvillagecode, v.svillagename, cc.slocation, cc.slongitude, "
				+ "cc.scomplaintdetails, cc.slatitude, cc.nstatus, " + "TO_CHAR(cc.dcomplaintdate,'"
				+ userInfo.getSpgsitedatetime() + "') AS scomplaintdate " + "FROM customercomplaint cc "
				+ "JOIN site central ON cc.ncentralsitecode = central.nsitecode AND central.nstatus = " // added by
																										// sujatha
																										// ATE_274
																										// 26-09-2025 to
																										// get newly
																										// added field
																										// called
																										// ncentralsitecode
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN site region ON cc.nregioncode = region.nsitecode AND region.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN site district ON cc.ndistrictcode = district.nsitecode AND district.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN site city ON cc.ncitycode = city.nsitecode AND city.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN villages v ON cc.nvillagecode = v.nvillagecode AND v.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN customercomplainthistory ch ON ch.ncustomercomplaintcode = cc.ncustomercomplaintcode "
				+ "JOIN transactionstatus ts1 ON ch.ntransactionstatus = ts1.ntranscode "
				+ "WHERE ch.ncustomercomplainthistorycode = ANY ( "
				+ "    SELECT MAX(ch2.ncustomercomplainthistorycode) " + "    FROM customercomplainthistory ch2 "
				+ "    JOIN customercomplaint c2 ON c2.ncustomercomplaintcode = ch2.ncustomercomplaintcode "
				+ "    WHERE c2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "    AND ch2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "    AND c2.nsitecode = " + userInfo.getNtranssitecode() + " " + "    AND ch2.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + "    GROUP BY c2.ncustomercomplaintcode " + ") "
				+ "AND cc.ncustomercomplaintcode = " + ncustomercomplaintcode + " " + "AND cc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND cc.nsitecode = "
				+ userInfo.getNtranssitecode();

		return (CustomerComplaint) jdbcUtilityFunction.queryForObject(query, CustomerComplaint.class, jdbcTemplate);
	}

	/**
	 * This method will access the DAO layer that is used to update entry in
	 * customercomplaint table.
	 * 
	 * @param inputMap holding the date fromDate,toDate,Status customercomplaint
	 *                 details and userInfo [UserInfo] holding logged in user
	 *                 details and nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         unit object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateCustomerComplaint(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		CustomerComplaint objCustomerComplaint = objMapper.convertValue(inputMap.get("customercomplaint"),
				CustomerComplaint.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final CustomerComplaint customerComplaint = getActiveCustomerComplaintById(
				objCustomerComplaint.getNcustomercomplaintcode(), userInfo);
		if (customerComplaint == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final CustomerComplaint existingComplaint = getCustomerComplaintByName(objCustomerComplaint, userInfo);

		if (existingComplaint != null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

		final String queryString = "SELECT ncustomercomplaintcode FROM customercomplaint " + "WHERE sreceivedfrom = N'"
				+ stringUtilityFunction.replaceQuote(objCustomerComplaint.getSreceivedfrom()) + "' "
				+ "AND ncustomercomplaintcode <> " + objCustomerComplaint.getNcustomercomplaintcode()
				+ " AND nsitecode = " + objCustomerComplaint.getNsitecode() + " AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final CustomerComplaint availableCustomerComplaint = (CustomerComplaint) jdbcUtilityFunction
				.queryForObject(queryString, CustomerComplaint.class, jdbcTemplate);
		if (availableCustomerComplaint != null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		if (objCustomerComplaint.getDcomplaintdate() != null) {
			objCustomerComplaint.setScomplaintdate(dateUtilityFunction
					.instantDateToString(objCustomerComplaint.getDcomplaintdate()).replace("T", " ").replace("Z", ""));
		}
		List<Object> listBeforeUpdate = new ArrayList<>();
		listBeforeUpdate.add(customerComplaint);
		final String updateQuery = "UPDATE customercomplaint SET " + "sreceivedfrom = N'"
				+ stringUtilityFunction.replaceQuote(objCustomerComplaint.getSreceivedfrom()) + "', "
				+ "scomplaintdetails = N'"
				+ stringUtilityFunction.replaceQuote(objCustomerComplaint.getScomplaintdetails()) + "', "
				+ "scontactnumber = N'" + stringUtilityFunction.replaceQuote(objCustomerComplaint.getScontactnumber())
				+ "', " + "semail = N'" + stringUtilityFunction.replaceQuote(objCustomerComplaint.getSemail()) + "', "
				+ "slocation = N'" + stringUtilityFunction.replaceQuote(objCustomerComplaint.getSlocation()) + "', "
				+ "slongitude = N'" + stringUtilityFunction.replaceQuote(objCustomerComplaint.getSlongitude()) + "', "
				+ "slatitude = N'" + stringUtilityFunction.replaceQuote(objCustomerComplaint.getSlatitude()) + "', "
				+ "ncentralsitecode= " + objCustomerComplaint.getNcentralsitecode() + ", " // added by sujatha ATE_274
																							// 26-09-2025 to update
																							// newly added field called
																							// ncentralsitecode
				+ "nregioncode = " + objCustomerComplaint.getNregioncode() + ", " + "ndistrictcode = "
				+ objCustomerComplaint.getNdistrictcode() + ", " + "ncitycode = " + objCustomerComplaint.getNcitycode()
				+ ", " + "nvillagecode = " + objCustomerComplaint.getNvillagecode() + ", " + "dcomplaintdate = "
				+ (objCustomerComplaint.getScomplaintdate() != null
						? "'" + objCustomerComplaint.getScomplaintdate() + "'"
						: "NULL")
				+ ", " + "dmodifieddate = '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' "
				+ "WHERE ncustomercomplaintcode = " + objCustomerComplaint.getNcustomercomplaintcode();
		jdbcTemplate.execute(updateQuery);

		final CustomerComplaint updatedCustomerComplaint = getActiveCustomerComplaintById(
				objCustomerComplaint.getNcustomercomplaintcode(), userInfo);
		List<Object> listAfterUpdate = new ArrayList<>();
		listAfterUpdate.add(updatedCustomerComplaint);
		List<String> multilingualIDList = new ArrayList<>();
		multilingualIDList.add("IDS_EDITCUSTOMERCOMPLAINT");
		auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo);

		return getCustomerComplaintDatas(inputMap, userInfo);
	}

	/**
	 * This method will access the DAO layer that is used to delete an entry in
	 * customercomplaint table.
	 * 
	 * @param inputMap holding the date fromDate,toDate,Status customercomplaint
	 *                 details and userInfo [UserInfo] holding logged in user
	 *                 details and nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         customercomplaint object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> deleteCustomerComplaint(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		CustomerComplaint objCustomerComplaint = objMapper.convertValue(inputMap.get("customercomplaint"),
				CustomerComplaint.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		if (!"Initiated".equals(objCustomerComplaint.getStransdisplaystatus())
				&& !"Closed".equals(objCustomerComplaint.getStransdisplaystatus())) {
			final CustomerComplaint customerComplaint = getActiveCustomerComplaintById(
					objCustomerComplaint.getNcustomercomplaintcode(), userInfo);
			if (customerComplaint == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
			final List<Object> deletedCustomerComplaint = new ArrayList<>();
			final List<String> multilingualIdList = new ArrayList<>();
			String updateQueryString = "UPDATE customercomplaint SET dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " WHERE ncustomercomplaintcode="
					+ objCustomerComplaint.getNcustomercomplaintcode() + ";";
			jdbcTemplate.execute(updateQueryString);
			updateQueryString = "UPDATE customercomplainthistory SET nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dtransactiondate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE ncustomercomplaintcode="
					+ objCustomerComplaint.getNcustomercomplaintcode() + ";";
			jdbcTemplate.execute(updateQueryString);

			final String queryFile = "SELECT * FROM customercomplaintfile " + "WHERE ncustomercomplaintcode = "
					+ objCustomerComplaint.getNcustomercomplaintcode() + " AND nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final CustomerComplaintFile files = (CustomerComplaintFile) jdbcUtilityFunction.queryForObject(queryFile,
					CustomerComplaintFile.class, jdbcTemplate);

			if (files != null) {
				updateQueryString = "UPDATE customercomplaintfile SET nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE ncustomercomplaintcode="
						+ objCustomerComplaint.getNcustomercomplaintcode() + ";";
				jdbcTemplate.execute(updateQueryString);
			}

			objCustomerComplaint.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			deletedCustomerComplaint.add(objCustomerComplaint);
			multilingualIdList.add("IDS_DELETECUSTOMERCOMPLAINT");
			auditUtilityFunction.fnInsertAuditAction(deletedCustomerComplaint, 1, null, multilingualIdList, userInfo);
			return getCustomerComplaintData(inputMap, userInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORDTODELETE",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method will access the DAO layer that is used to retrieve active
	 * customercomplainthistory object based on the specified ncustomercomplaintode.
	 * 
	 * @param ncustomercomplaintCode [int] primary key of CustomerComplaint object
	 * @param userInfo               [UserInfo] holding logged in user details based
	 *                               on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         CustomerComplaint object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getCustomerComplaintRecord(final int ncustomercomplaintcode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final CustomerComplaint customerComplaint = getActiveCustomerComplaintById(ncustomercomplaintcode, userInfo);
		if (customerComplaint == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String strQuery = "SELECT c.ncustomercomplaintcode," + " COALESCE(TO_CHAR(c.dcomplaintdate,'"
					+ userInfo.getSsitedate() + "'),'-') AS sreceiverdate," // added sreceiverdate by sujatha for
																			// SWSM-68 Customer Complaint screen ->
																			// Receive Date is not showing.
					+ " c.dcomplaintdate, c.ntzcomplaintdate, c.noffsetdcomplaintdate, "
					+ "c.sreceivedfrom, c.scontactnumber, c.semail, c.scomplaintdetails, "
					+ "c.ncentralsitecode, cs.ssitename AS scentralsitename, " // modified by sujatha ATE_274 26-09-2025
																				// to get newly added field called
																				// ncentralsitecode
					+ "c.nregioncode, r.ssitename AS sregionname, " + "c.ndistrictcode, d.ssitename AS sdistrictname, "
					+ "c.ncitycode, ci.ssitename AS scityname, " + "c.nvillagecode, v.svillagename, "
					+ "ch.ntransactionstatus, " + "c.slocation, c.slatitude, c.slongitude, "
					+ "COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "', ts1.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus "
					+ "FROM customercomplaint c "
					+ "JOIN customercomplainthistory ch ON c.ncustomercomplaintcode = ch.ncustomercomplaintcode "
					+ "JOIN transactionstatus ts1 ON ch.ntransactionstatus = ts1.ntranscode "
					+ "JOIN site cs ON c.ncentralsitecode = cs.nsitecode " // added by sujatha ATE_274 26-09-2025 to get
																			// newly added field called ncentralsitecode
					+ "JOIN site r ON c.nregioncode = r.nsitecode " + "JOIN site d ON c.ndistrictcode = d.nsitecode "
					+ "JOIN site ci ON c.ncitycode = ci.nsitecode "
					+ "JOIN villages v ON c.nvillagecode = v.nvillagecode "
					+ "WHERE ch.ncustomercomplainthistorycode = ANY ( "
					+ "   SELECT MAX(ch2.ncustomercomplainthistorycode) " + "   FROM customercomplainthistory ch2 "
					+ "   JOIN customercomplaint c2 ON c2.ncustomercomplaintcode = ch2.ncustomercomplaintcode "
					+ "   WHERE c2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "   AND ch2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "   AND c2.nsitecode = " + userInfo.getNtranssitecode() + " " + "   AND ch2.nsitecode = "
					+ userInfo.getNtranssitecode() + " " + "   GROUP BY c2.ncustomercomplaintcode " + ") "
					+ "AND c.ncustomercomplaintcode = " + ncustomercomplaintcode + " " + "AND c.nsitecode = "
					+ userInfo.getNtranssitecode() + " " + "AND c.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			final List<CustomerComplaint> lstCustomerComplaint = (List<CustomerComplaint>) jdbcTemplate.query(strQuery,
					new CustomerComplaint());
			if (lstCustomerComplaint != null) {
				CustomerComplaint selectedComplaint = lstCustomerComplaint.get(0);
				outputMap.put("selectedCustomerComplaint", selectedComplaint);
				int complaintCode = selectedComplaint.getNcustomercomplaintcode();

				outputMap.putAll((Map<String, Object>) getComplaintHistory(complaintCode, userInfo).getBody());
				outputMap.putAll((Map<String, Object>) getCustomerComplaintFile(complaintCode, userInfo).getBody());
			} else {
				outputMap.put("selectedCustomerComplaint", null);
				outputMap.put("complaintHistory", null);
			}
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	/**
	 * This method provides access to the DAO layer to retrieve all available
	 * customercomplaintfile for a specific site.
	 *
	 * @param userInfo               an instance of [UserInfo] containing the
	 *                               logged-in user details and the nmasterSiteCode,
	 *                               which is the primary key of the site for which
	 *                               the complaints are to be fetched
	 * @param ncustomercomplaintCode [int] primary key of CustomerComplaint object
	 *
	 * @return a ResponseEntity containing the list of customer complaint records
	 *         associated with the specified site
	 *
	 * @throws Exception if any errors occur in the DAO layer
	 */

	public ResponseEntity<Object> getCustomerComplaintFile(final int ncustomercomplaintcode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		String query = "select tf.noffsetdcreateddate,tf.ncustomercomplaintfilecode,"
				+ "(select  count(ncustomercomplaintfilecode) from customercomplaintfile where ncustomercomplaintfilecode>0"
				+ " and ncustomercomplaintcode = " + ncustomercomplaintcode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") as ncount,tf.sdescription,"
				+ " tf.ncustomercomplaintfilecode as nprimarycode,tf.sfilename,tf.ncustomercomplaintcode,tf.ssystemfilename,"
				+ " tf.nattachmenttypecode,coalesce(at.jsondata->'sattachmenttype'->>'"
				+ objUserInfo.getSlanguagetypecode() + "',"
				+ "	at.jsondata->'sattachmenttype'->>'en-US') as sattachmenttype, case when tf.nlinkcode=-1 then '-' else lm.jsondata->>'slinkname'"
				+ " end slinkname, tf.nfilesize," + " case when tf.nattachmenttypecode= "
				+ Enumeration.AttachmentType.LINK.gettype() + " then '-' else" + " COALESCE(TO_CHAR(tf.dcreateddate,'"
				+ objUserInfo.getSpgdatetimeformat() + "'),'-') end  as screateddate, "
				+ " tf.nlinkcode, case when tf.nlinkcode = -1 then tf.nfilesize::varchar(1000) else '-' end sfilesize"
				+ " from customercomplaintfile tf,attachmenttype at, linkmaster lm  "
				+ " where at.nattachmenttypecode = tf.nattachmenttypecode and at.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and lm.nlinkcode = tf.nlinkcode and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nsitecode = "
				+ objUserInfo.getNmastersitecode() + " and tf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.nsitecode = "
				+ objUserInfo.getNtranssitecode() + " and tf.ncustomercomplaintcode=" + ncustomercomplaintcode
				+ " order by tf.ncustomercomplaintfilecode;";
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<CustomerComplaintFile> lstCustomercomplaintfile = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(jdbcTemplate.query(query, new CustomerComplaintFile()),
						Arrays.asList("screateddate"), null, objUserInfo, false, null, false),
				new TypeReference<List<CustomerComplaintFile>>() {
				});
		outputMap.put("customerComplaintFile", lstCustomercomplaintfile);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public CustomerComplaintHistory checKCustomerComplaintIsPresent(final int ncustomercomplaintcode,
			final UserInfo objUserInfo) throws Exception {
		String strQuery = "select ncustomercomplaintcode from customercomplaint where" + " nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ncustomercomplaintcode = "
				+ ncustomercomplaintcode + " and nsitecode = " + objUserInfo.getNtranssitecode();

		CustomerComplaintHistory objCustomerComplaintHistory = (CustomerComplaintHistory) jdbcUtilityFunction
				.queryForObject(strQuery, CustomerComplaintHistory.class, jdbcTemplate);

		return objCustomerComplaintHistory;
	}

	/**
	 * This method will access the DAO layer that is used to create the records in
	 * customercomplaintfile
	 * 
	 * @param request  holding the date for upload the file in customercomplaintfile
	 *                 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of customercomplaintfile
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> createCustomerComplaintFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final String sQuery = " lock  table lockcustomercomplaintfile "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<CustomerComplaintFile> lstReqCustomerComplaintFile = objMapper.readValue(
				request.getParameter("customerComplaintFile"), new TypeReference<List<CustomerComplaintFile>>() {
				});
		if (lstReqCustomerComplaintFile != null && lstReqCustomerComplaintFile.size() > 0) {
			final CustomerComplaintHistory objCustomerComplaintHistory = checKCustomerComplaintIsPresent(
					lstReqCustomerComplaintFile.get(0).getNcustomercomplaintcode(), objUserInfo);
			if (objCustomerComplaintHistory != null) {
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (lstReqCustomerComplaintFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP
						.gettype()) {
					sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
				}
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo)
							.truncatedTo(ChronoUnit.SECONDS);
					final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
					final int noffset = dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid());
					lstReqCustomerComplaintFile.forEach(objtf -> {
						objtf.setDcreateddate(instantDate);
						if (objtf.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							objtf.setDcreateddate(instantDate);
							objtf.setNoffsetdcreateddate(noffset);
							objtf.setScreateddate(sattachmentDate.replace("T", " "));
						}
					});

					String sequencequery = "select nsequenceno from seqnocredentialmanagement where stablename ='customercomplaintfile'  "
							+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencequery, Integer.class,
							jdbcTemplate);
					nsequenceno++;
					String insertquery = "Insert into customercomplaintfile(ncustomercomplaintfilecode,ncustomercomplaintcode,nlinkcode,nattachmenttypecode,sfilename,sdescription,nfilesize,dcreateddate,noffsetdcreateddate,ntzcreateddate,ssystemfilename,dmodifieddate,nsitecode,nstatus)"
							+ "values (" + nsequenceno + ","
							+ lstReqCustomerComplaintFile.get(0).getNcustomercomplaintcode() + ","
							+ lstReqCustomerComplaintFile.get(0).getNlinkcode() + ","
							+ lstReqCustomerComplaintFile.get(0).getNattachmenttypecode() + "," + " N'"
							+ stringUtilityFunction.replaceQuote(lstReqCustomerComplaintFile.get(0).getSfilename())
							+ "',N'"
							+ stringUtilityFunction.replaceQuote(lstReqCustomerComplaintFile.get(0).getSdescription())
							+ "'," + lstReqCustomerComplaintFile.get(0).getNfilesize() + "," + " '"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ lstReqCustomerComplaintFile.get(0).getNoffsetdcreateddate() + ","
							+ objUserInfo.getNtimezonecode() + ",N'"
							+ lstReqCustomerComplaintFile.get(0).getSsystemfilename() + "','"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', "
							+ objUserInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(insertquery);
					String updatequery = "update seqnocredentialmanagement set nsequenceno =" + nsequenceno
							+ " where stablename ='customercomplaintfile'" + "  and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					jdbcTemplate.execute(updatequery);
					final List<String> multilingualIDList = new ArrayList<>();
					multilingualIDList.add(lstReqCustomerComplaintFile.get(0)
							.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
									? "IDS_ADDCUSTOMERCOMPLAINTFILE"
									: "IDS_ADDCUSTOMERCOMPLAINTLINK");
					final List<Object> listObject = new ArrayList<Object>();
					String auditqry = "select * from customercomplaintfile where ncustomercomplaintcode = "
							+ lstReqCustomerComplaintFile.get(0).getNcustomercomplaintcode() + " and nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ncustomercomplaintfilecode = " + nsequenceno;
					final List<CustomerComplaintFile> lstvalidate = (List<CustomerComplaintFile>) jdbcTemplate
							.query(auditqry, new CustomerComplaintFile());
					listObject.add(lstvalidate);
					auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CUSTOMERCOMPLAINTALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			return (getCustomerComplaintFile(lstReqCustomerComplaintFile.get(0).getNcustomercomplaintcode(),
					objUserInfo));
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method will access the DAO layer that is used to create the records in
	 * customercomplaintfile
	 * 
	 * @param request  holding the date for upload the file in customercomplaintfile
	 *                 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of customercomplaintfile
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> editCustomerComplaintFile(final CustomerComplaintFile objCustomerComplaintFile,
			final UserInfo objUserInfo) throws Exception {
		final String sEditQuery = "select  tf.ncustomercomplaintfilecode, tf.ncustomercomplaintcode, tf.nlinkcode, tf.nattachmenttypecode, "
				+ " tf.sfilename, tf.sdescription, tf.nfilesize,"
				+ " tf.ssystemfilename,  lm.jsondata->>'slinkname' as slinkname"
				+ " from customercomplaintfile tf, linkmaster lm,customercomplaint c where lm.nlinkcode = tf.nlinkcode"
				+ " and tf.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tf.nsitecode =" + objUserInfo.getNtranssitecode() + " and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nsitecode ="
				+ objUserInfo.getNmastersitecode() + " and c.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nsitecode ="
				+ objUserInfo.getNtranssitecode()
				+ " and tf.ncustomercomplaintcode=c.ncustomercomplaintcode and tf.ncustomercomplaintfilecode = "
				+ objCustomerComplaintFile.getNcustomercomplaintfilecode();
		final CustomerComplaintFile objTF = (CustomerComplaintFile) jdbcUtilityFunction.queryForObject(sEditQuery,
				CustomerComplaintFile.class, jdbcTemplate);
		if (objTF != null) {
			return new ResponseEntity<Object>(objTF, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to delete the records in customercomplaintfile
	 * 
	 * @param request  holding the date for delete the file in customercomplaintfile
	 *                 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of customercomplaintfile
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteCustomerComplaintFile(CustomerComplaintFile objCustomerComplaintFile,
			UserInfo objUserInfo) throws Exception {
		final CustomerComplaintHistory objCustomerComplaintHistory = checKCustomerComplaintIsPresent(
				objCustomerComplaintFile.getNcustomercomplaintcode(), objUserInfo);
		if (objCustomerComplaintHistory != null) {
			if (objCustomerComplaintHistory != null) {
				final String sQuery = "select * from customercomplaintfile where" + " nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ncustomercomplaintfilecode = "
						+ objCustomerComplaintFile.getNcustomercomplaintfilecode();
				final CustomerComplaintFile objTF = (CustomerComplaintFile) jdbcUtilityFunction.queryForObject(sQuery,
						CustomerComplaintFile.class, jdbcTemplate);
				if (objTF != null) {
					if (objCustomerComplaintFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					} else {
						objCustomerComplaintFile.setScreateddate(null);
					}
					final String sUpdateQuery = "update customercomplaintfile set" + "  dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'" + ", nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ " where ncustomercomplaintfilecode = "
							+ objCustomerComplaintFile.getNcustomercomplaintfilecode();
					jdbcTemplate.execute(sUpdateQuery);
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> lstObject = new ArrayList<>();
					multilingualIDList
							.add(objCustomerComplaintFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
									.gettype() ? "IDS_DELETECUSTOMERCOMPLAINTFILE" : "IDS_DELETECUSTOMERCOMPLAINTLINK");
					lstObject.add(objCustomerComplaintFile);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			return getCustomerComplaintFile(objCustomerComplaintFile.getNcustomercomplaintcode(), objUserInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CUSTOMERCOMPLAINTALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to update the records in customercomplaintfile
	 * 
	 * @param request  holding the date for update the file in customercomplaintfile
	 *                 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of customercomplaintfile
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateCustomerComplaintFile(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final List<CustomerComplaintFile> lstCustomerComplaintFile = objMapper.readValue(
				request.getParameter("customerComplaintFile"), new TypeReference<List<CustomerComplaintFile>>() {
				});
		if (lstCustomerComplaintFile != null && lstCustomerComplaintFile.size() > 0) {
			final CustomerComplaintFile objCustomerComplaintFile = lstCustomerComplaintFile.get(0);
			final CustomerComplaintHistory objCustomerComplaintHistory = checKCustomerComplaintIsPresent(
					objCustomerComplaintFile.getNcustomercomplaintcode(), objUserInfo);
			if (objCustomerComplaintHistory != null) {
				final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if (objCustomerComplaintFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
					}
				}
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final String sQuery = "select * from customercomplaintfile where" + " nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ncustomercomplaintfilecode = "
							+ objCustomerComplaintFile.getNcustomercomplaintfilecode() + " and nsitecode ="
							+ objUserInfo.getNtranssitecode();
					final CustomerComplaintFile objTF = (CustomerComplaintFile) jdbcUtilityFunction
							.queryForObject(sQuery, CustomerComplaintFile.class, jdbcTemplate);
					if (objTF != null) {
						String ssystemfilename = "";
						if (objCustomerComplaintFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
								.gettype()) {
							ssystemfilename = objCustomerComplaintFile.getSsystemfilename();
						}
						final String sUpdateQuery = "update customercomplaintfile set sfilename=N'"
								+ stringUtilityFunction.replaceQuote(objCustomerComplaintFile.getSfilename()) + "',"
								+ " sdescription=N'"
								+ stringUtilityFunction.replaceQuote(objCustomerComplaintFile.getSdescription())
								+ "', ssystemfilename= N'" + ssystemfilename + "'," + " nattachmenttypecode = "
								+ objCustomerComplaintFile.getNattachmenttypecode() + ", nlinkcode="
								+ objCustomerComplaintFile.getNlinkcode() + "," + " nfilesize = "
								+ objCustomerComplaintFile.getNfilesize() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' "
								+ "  where ncustomercomplaintfilecode = "
								+ objCustomerComplaintFile.getNcustomercomplaintfilecode();
						objCustomerComplaintFile.setDcreateddate(objTF.getDcreateddate());
						jdbcTemplate.execute(sUpdateQuery);
						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<Object>();
						multilingualIDList
								.add(objCustomerComplaintFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
										.gettype() ? "IDS_EDITCUSTOMERCOMPLAINTFILE" : "IDS_EDITCUSTOMERCOMPLAINTLINK");
						lstOldObject.add(objTF);
						auditUtilityFunction.fnInsertAuditAction(lstCustomerComplaintFile, 2, lstOldObject,
								multilingualIDList, objUserInfo);
						return (getCustomerComplaintFile(objCustomerComplaintFile.getNcustomercomplaintcode(),
								objUserInfo));
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CUSTOMERCOMPLAINTALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to view the records in customercomplaintfile
	 * 
	 * @param request  holding the date for update the file in customercomplaintfile
	 *                 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of customercomplaintfile
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> viewAttachedCustomerComplaintFile(CustomerComplaintFile objCustomerComplaintFile,
			UserInfo objUserInfo) throws Exception {
		Map<String, Object> map = new HashMap<>();
		final CustomerComplaintHistory objCustomerComplaintHistory = checKCustomerComplaintIsPresent(
				objCustomerComplaintFile.getNcustomercomplaintcode(), objUserInfo);
		if (objCustomerComplaintHistory != null) {
			String sQuery = "select * from customercomplaintfile where" + " nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ncustomercomplaintfilecode = "
					+ objCustomerComplaintFile.getNcustomercomplaintfilecode() + " and nsitecode ="
					+ objUserInfo.getNtranssitecode();
			final CustomerComplaintFile objTF = (CustomerComplaintFile) jdbcUtilityFunction.queryForObject(sQuery,
					CustomerComplaintFile.class, jdbcTemplate);

			if (objTF != null) {
				if (objTF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					map = ftpUtilityFunction.FileViewUsingFtp(objTF.getSsystemfilename(), -1, objUserInfo, "", "");
				} else {
					sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where" + " nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nlinkcode="
							+ objTF.getNlinkcode();
					LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery, LinkMaster.class,
							jdbcTemplate);

					map.put("AttachLink", objlinkmaster.getSlinkname() + objTF.getSfilename());
					objCustomerComplaintFile.setScreateddate(null);
				}
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> lstObject = new ArrayList<>();
				multilingualIDList.add(
						objCustomerComplaintFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
								? "IDS_VIEWCUSTOMERCOMPLAINTFILE"
								: "IDS_VIEWCUSTOMERCOMPLAINTLINK");
				lstObject.add(objCustomerComplaintFile);
				auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
			} else {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			}
		} else {
			return new ResponseEntity<Object>(commonFunction
					.getMultilingualMessage("IDS_CUSTOMERCOMPLAINTALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}
}