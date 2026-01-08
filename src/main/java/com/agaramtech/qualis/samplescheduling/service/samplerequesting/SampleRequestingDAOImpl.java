package com.agaramtech.qualis.samplescheduling.service.samplerequesting;

import java.time.Instant;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.emailmanagement.model.EmailUserQuery;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.ReportDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.reports.model.ControlbasedReportvalidation;
import com.agaramtech.qualis.reports.model.ReportMaster;
import com.agaramtech.qualis.reports.service.controlbasedreport.ControlBasedReportDAO;
import com.agaramtech.qualis.samplescheduling.model.SampleRequesting;
import com.agaramtech.qualis.samplescheduling.model.SampleRequestingFile;
import com.agaramtech.qualis.samplescheduling.model.SampleRequestingHistory;
import com.agaramtech.qualis.samplescheduling.model.SampleRequestingLocation;
import com.agaramtech.qualis.samplescheduling.model.SampleSchedulingLocation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SampleRequestingDAOImpl implements SampleRequestingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleRequestingDAOImpl.class);
	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final CommonFunction commonFunction;
	private final StringUtilityFunction stringUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final EmailDAOSupport emailDAOSupport;
	private final ControlBasedReportDAO controlBasedReportDAO;
	private final ReportDAOSupport reportDAOSupport;

	@Override
	public ResponseEntity<Object> getRegion(final UserInfo userInfo,int nsamplerequestingcode) throws Exception {
		//SWSM -78 added by rukshana on oct-03-2025 to check the validation in sample location add action
		
   //SampleRequesting objSampleRequesting=null;
  // if(nsamplerequestingcode != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
	     SampleRequesting objSampleRequesting = nsamplerequestingcode != Enumeration.TransactionStatus.NA.gettransactionstatus() ?
	    		 checkValidationForSchedulingCompletion(nsamplerequestingcode,userInfo) :new SampleRequesting(); 
    // }
		
	if(objSampleRequesting != null ) {

		String configFilter = "WITH RECURSIVE selected_config AS ( " + " SELECT * FROM sitehierarchyconfig sc "
				+ " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ "   AND jsonb_path_exists(sc.jsondata, '$.** ? (@.item.selectedNodeDetail.nsitecode == "
				+ userInfo.getNtranssitecode() + ")') " + " ORDER BY sc.dmodifieddate DESC LIMIT 1 " + " ) ";

		// -------------------------------
		// Current site
		// -------------------------------
		String strCurrent = configFilter + " , current_site AS ( "
				+ " SELECT s.nsitecode, s.ssitename, 'Current' AS srelation " + " FROM site s "
				+ " WHERE s.nsitecode = " + userInfo.getNtranssitecode() + " ) " + " , json_expanded AS ( "
				+ " SELECT DISTINCT (elem->>'nsitecode')::int AS nsitecode, "
				+ " elem->>'ssitetypename' AS ssitetypename, " + " (elem->>'nhierarchicalorderno')::int AS nlevel, "
				+ " sc.nsitehierarchyconfigcode " + " FROM selected_config sc, "
				+ " jsonb_path_query(sc.jsondata, '$.**.item.selectedNodeDetail') elem " + " ) "
				+ " SELECT c.nsitecode, c.ssitename, c.srelation, "
				+ " j.ssitetypename, j.nlevel, j.nsitehierarchyconfigcode " + " FROM current_site c "
				+ " JOIN json_expanded j ON j.nsitecode = c.nsitecode ";

		final List<SampleRequesting> currentList = jdbcTemplate.query(strCurrent, new SampleRequesting());

		// -------------------------------
		// Parent sites
		// -------------------------------
		String strParent = configFilter + " , site_hierarchy AS ( " + "    SELECT "
				+ " (sc.jsondata->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " sc.jsondata->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " sc.jsondata->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (sc.jsondata->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " (sc.jsondata->>'parentKey')::int AS parentkey, " + " sc.jsondata->'nodes' AS children, "
				+ " sc.nsitehierarchyconfigcode " + "    FROM selected_config sc " + " UNION ALL " + " SELECT "
				+ " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (child->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " (child->>'parentKey')::int AS parentkey, " + " child->'nodes' AS children, "
				+ " sh.nsitehierarchyconfigcode "
				+ " FROM site_hierarchy sh, jsonb_array_elements(sh.children) AS child " + " ) , parents AS ( "
				+ "    SELECT sh.* " + "    FROM site_hierarchy sh " + "    WHERE sh.nsitecode = "
				+ userInfo.getNtranssitecode() + "    UNION ALL " + "    SELECT sh.* " + "    FROM site_hierarchy sh "
				+ "    JOIN parents p ON sh.nsitecode = p.parentkey " + " ) SELECT nsitecode, "
				+ " split_part(ssitename, '(', 1) AS ssitename, "
				+ " ssitetypename, parentkey, nlevel, nsitehierarchyconfigcode " + " FROM parents "
				+ " WHERE nsitecode != " + userInfo.getNtranssitecode() + " ORDER BY nsitecode ";

		final List<SampleRequesting> parentList = jdbcTemplate.query(strParent, new SampleRequesting());

		// -------------------------------
		// Child sites
		// -------------------------------
		String strChild = configFilter + " , site_hierarchy AS ( " + "    SELECT "
				+ " elem->'item'->'selectedNodeDetail'->>'nsitecode' AS nsitecode, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (elem->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " elem->>'parentKey' AS parentkey, " + " elem->'nodes' AS children, "
				+ " sc.nsitehierarchyconfigcode "
				+ " FROM selected_config sc, jsonb_array_elements(sc.jsondata->'nodes') AS elem " + " UNION ALL "
				+ " SELECT " + " child->'item'->'selectedNodeDetail'->>'nsitecode' AS nsitecode, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (child->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " child->>'parentKey' AS parentkey, " + " child->'nodes' AS children, "
				+ " sh.nsitehierarchyconfigcode "
				+ " FROM site_hierarchy sh, jsonb_array_elements(sh.children) AS child " + " ) SELECT "
				+ " nsitecode::int, " + " split_part(ssitename, '(', 1) AS ssitename, "
				+ " ssitetypename, parentkey::int, nlevel, nsitehierarchyconfigcode " + " FROM site_hierarchy "
				+ " WHERE parentkey::int = " + userInfo.getNtranssitecode()
				+ "   OR parentkey::int IN ( SELECT nsitecode::int FROM site_hierarchy WHERE parentkey::int = "
				+ userInfo.getNtranssitecode() + " ) " + " ORDER BY nsitecode::int ";

		final List<SampleRequesting> childList = jdbcTemplate.query(strChild, new SampleRequesting());

		// -------------------------------
		// Output to frontend
		// -------------------------------
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		if (!currentList.isEmpty()) {
			outputMap.put("currentList", currentList);
			outputMap.put("parentList", parentList);
			outputMap.put("childList", childList);

		} else {
			outputMap.put("currentList", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	  
	  }else {
		  return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTPLANNEDRECORDTODELETE",
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
	   }
	}

//	@Override
//	public ResponseEntity<Object> getSubDivisionalLab(final int nprimarykey, final UserInfo userInfo) throws Exception {
//		String strVillage = "WITH RECURSIVE site_hierarchy AS ( " + " SELECT "
//				+ " (elem->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
//				+ " elem->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
//				+ " elem->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
//				+ " (elem->>'parentKey')::int AS parentkey, " + " elem->'nodes' AS children "
//				+ " FROM sitehierarchyconfig sc, jsonb_array_elements(sc.jsondata->'nodes') AS elem "
//				+ " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
//				+ " UNION ALL " + " SELECT "
//				+ " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
//				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
//				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
//				+ " (child->>'parentKey')::int AS parentkey, " + " child->'nodes' AS children "
//				+ " FROM site_hierarchy sh, jsonb_array_elements(sh.children) AS child " + ") "
//				+ "SELECT nsitecode, split_part(ssitename, '(', 1) AS ssitename, ssitetypename, parentkey "
//				+ "FROM site_hierarchy " + "WHERE parentkey = " + nprimarykey + " ORDER BY nsitecode;";
//		final List<SampleScheduling> villageList = (List<SampleScheduling>) jdbcTemplate.query(strVillage,
//				new SampleScheduling());
//		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
//		if (!villageList.isEmpty()) {
//			outputMap.put("villageList", villageList);
//		} else {
//			outputMap.put("villageList", null);
//		}
//		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
//	}
	
	//modified by sujatha ATE_274 SWSM-117 in the end of condition on nsitehiearchyconfig code also checked which is approved
	@Override
	public ResponseEntity<Object> getSubDivisionalLab(final int regionCode, final int districtCode,
			final UserInfo userInfo) throws Exception {
		String strQuery = "SELECT DISTINCT " + " ssl.ncitycode, city.ssitename AS scityname "
				+ "FROM sampleschedulinglocation ssl " + "LEFT JOIN site city ON ssl.ncitycode = city.nsitecode "
				+ "WHERE ssl.nregioncode = " + regionCode + " AND ssl.ndistrictcode = " + districtCode
				+ " AND ssl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND ssl.nsitecode = " + userInfo.getNmastersitecode()
				+ " AND ssl.nsitehierarchyconfigcode = (SELECT MAX(nsitehierarchyconfigcode) FROM sitehierarchyconfig where ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ ")";

		final List<SampleSchedulingLocation> cityList = jdbcTemplate.query(strQuery, new SampleSchedulingLocation());

		final Map<String, Object> outputMap = new LinkedHashMap<>();
		outputMap.put("cityList", cityList.isEmpty() ? null : cityList);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

//	@Override
//	public ResponseEntity<Object> getDistrictLab(final int ndistrictCode, final UserInfo userInfo) throws Exception {
//		String strTaluka = "WITH RECURSIVE site_hierarchy AS ( " + " SELECT "
//				+ " (elem->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
//				+ " elem->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
//				+ " elem->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
//				+ " (elem->>'parentKey')::int AS parentkey, " + " elem->'nodes' AS children "
//				+ " FROM sitehierarchyconfig sc, jsonb_array_elements(sc.jsondata->'nodes') AS elem "
//				+ " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
//				+ " UNION ALL " + " SELECT "
//				+ " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
//				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
//				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
//				+ " (child->>'parentKey')::int AS parentkey, " + " child->'nodes' AS children "
//				+ " FROM site_hierarchy sh, jsonb_array_elements(sh.children) AS child " + ") "
//				+ "SELECT nsitecode, split_part(ssitename, '(', 1) AS ssitename, ssitetypename, parentkey "
//				+ "FROM site_hierarchy " + "WHERE parentkey = " + ndistrictCode + " ORDER BY nsitecode;";
//
//		final List<SampleScheduling> talukaList = (List<SampleScheduling>) jdbcTemplate.query(strTaluka,
//				new SampleScheduling());
//
//		final Map<String, Object> outputMap = new LinkedHashMap<>();
//		if (!talukaList.isEmpty()) {
//			outputMap.put("talukaList", talukaList);
//		} else {
//			outputMap.put("talukaList", null);
//		}
//		return new ResponseEntity<>(outputMap, HttpStatus.OK);
//	}
	
	//modified by sujatha ATE_274 SWSM_117 by added one more condition in nsitehierarchyconfigcode which is equal to approved one
	@Override
	public ResponseEntity<Object> getDistrictLab(final int regionCode, final UserInfo userInfo) throws Exception {
		String strQuery = "SELECT DISTINCT " + " ssl.ndistrictcode, district.ssitename AS sdistrictname "
				+ "FROM sampleschedulinglocation ssl "
				+ "LEFT JOIN site district ON ssl.ndistrictcode = district.nsitecode " + "WHERE ssl.nregioncode = "
				+ regionCode + " AND ssl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND ssl.nsitecode = " + userInfo.getNmastersitecode()
				+ " AND ssl.nsitehierarchyconfigcode = (SELECT MAX(nsitehierarchyconfigcode) FROM sitehierarchyconfig where ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ ")";

		final List<SampleSchedulingLocation> talukaList = jdbcTemplate.query(strQuery, new SampleSchedulingLocation());

		final Map<String, Object> outputMap = new LinkedHashMap<>();
		outputMap.put("talukaList", talukaList.isEmpty() ? null : talukaList);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	//modified & below lines are commentted by sujatha ATE_274 SWSM-78 for one more check while getting villages
	//modified by sujatha ATE_274 SWSM-117 for added 2 more parameter called regionCode & districtCode for query validation
	@Override
	public ResponseEntity<Object> getVillages(final int nsampleschedulingcode, final int regionCode, final int districtCode, final int nprimarykey, final UserInfo objUserInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

//		String strQuery = "select sl.nvillagecode,v.svillagename from samplelocation sl "
//				+ " JOIN villages v ON v.nvillagecode=sl.nvillagecode " + " where sl.ncitycode=" + nprimarykey
//				+ " and sl.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		String strQuery="SELECT  ssl.nvillagecode, v.svillagename FROM sampleschedulinglocation ssl"
						+ " JOIN villages v ON v.nvillagecode = ssl.nvillagecode WHERE "
						+ " ssl.nregioncode = "+regionCode
						+ " AND ssl.ndistrictcode ="+districtCode
						+ " AND ssl.nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " AND ssl.nsitecode="+objUserInfo.getNmastersitecode()
						+ " AND ssl.nsampleschedulingcode ="+nsampleschedulingcode
						+ " AND ssl.ncitycode ="+nprimarykey
						+ " GROUP BY ssl.nvillagecode, v.svillagename ORDER BY ssl.nvillagecode DESC;";
//		ObjectMapper objMapper = new ObjectMapper();
//		objMapper.registerModule(new JavaTimeModule());

		final List<SampleSchedulingLocation> villageList = jdbcTemplate.query(strQuery, new SampleSchedulingLocation());

		outputMap.put("villageList", villageList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	//modified by sujatha ATE_274 SWSM-78 for one more check while getting villages
	@Override
	public ResponseEntity<Object> getVillageBasedOnSiteHierarchy(final int sampleschedulingcode, final int regionCode, final int districtCode, final int cityCode,
			final UserInfo userInfo) throws Exception {
		String strQuery = "SELECT DISTINCT " + " ssl.nvillagecode, village.svillagename AS svillagename "
				+ "FROM sampleschedulinglocation ssl "
				+ "LEFT JOIN villages village ON ssl.nvillagecode = village.nvillagecode " + "WHERE ssl.nregioncode = "
				+ regionCode + " AND ssl.ndistrictcode = " + districtCode + " AND ssl.ncitycode = " + cityCode
				+ " AND ssl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND ssl.nsitecode = " + userInfo.getNmastersitecode()
				//commented by sujatha ATE_274 for SWSM-115 Sample Requesting Plan screen -> Location & village is not showing due to taking max of sitehierarchyconfigcode
				//+ " AND ssl.nsitehierarchyconfigcode = (SELECT MAX(nsitehierarchyconfigcode) FROM sitehierarchyconfig)"
				+ " AND ssl.nsampleschedulingcode="+sampleschedulingcode;

		final List<SampleSchedulingLocation> villageList = jdbcTemplate.query(strQuery, new SampleSchedulingLocation());

		final Map<String, Object> outputMap = new LinkedHashMap<>();
		outputMap.put("villageList", villageList.isEmpty() ? null : villageList);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

//	@Override
//	public ResponseEntity<Object> getLocation(final int nprimarykey, final UserInfo userInfo) throws Exception {
//
//		String strLocation = "select  sl.ssamplelocationname,sl.nsamplelocationcode  from samplelocation sl where sl.nvillagecode ="
//				+ nprimarykey + " and  nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ "  and  nsitecode= " + userInfo.getNmastersitecode();
//		final List<SampleLocation> LocationList = (List<SampleLocation>) jdbcTemplate.query(strLocation,
//				new SampleLocation());
//		final Map<String, Object> outputMap = new LinkedHashMap<>();
//		if (!LocationList.isEmpty()) {
//			outputMap.put("LocationList", LocationList);
//		} else {
//			outputMap.put("LocationList", null);
//		}
//		return new ResponseEntity<>(outputMap, HttpStatus.OK);
//
//	}

	//modified by sujatha ATE_274 SWSM-78 for one more check while getting location
	@Override
	public ResponseEntity<Object> getLocation(final int sitehierarchyconfigcode, final int nsampleschedulingcode, final int regionCode,
			final int districtCode, final int cityCode,
			final int villageCode, final UserInfo userInfo) throws Exception {
		String strQuery = "SELECT DISTINCT " + " ssl.nsamplelocationcode, location.ssamplelocationname "
				+ "FROM sampleschedulinglocation ssl "
				+ "LEFT JOIN samplelocation location ON ssl.nsamplelocationcode = location.nsamplelocationcode "
				+ "WHERE ssl.nregioncode = " + regionCode + " AND ssl.ndistrictcode = " + districtCode
				+ " AND ssl.ncitycode = " + cityCode + " AND ssl.nvillagecode = " + villageCode + " AND ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  AND ssl.nsitehierarchyconfigcode ="+sitehierarchyconfigcode
				+ "  AND ssl.nsampleschedulingcode="+nsampleschedulingcode;

		final List<SampleSchedulingLocation> locationList = jdbcTemplate.query(strQuery,
				new SampleSchedulingLocation());

		final Map<String, Object> outputMap = new LinkedHashMap<>();
		outputMap.put("LocationList", locationList.isEmpty() ? null : locationList);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	
	//modified & commentted by sujatha ATE_274 SWSM-78 for getting the planned samplescheduling period in the period dropdown
	@Override
	public ResponseEntity<Object> getPeriod(final String sfromyear, final UserInfo objUserInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

//		String strQuery = "SELECT DISTINCT ss.nsampleschedulingcode, ss.speriod" + " FROM samplescheduling ss"
//				+ " JOIN sampleschedulinghistory ssh"
//				+ " ON ss.nsampleschedulingcode = ssh.nsampleschedulingcode WHERE ss.sfromyear = '" + sfromyear
//				+ "'  AND ss.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " AND ssh.ntransactionstatus =" + Enumeration.TransactionStatus.PLANNED.gettransactionstatus();
		
		
		String strQuery ="SELECT ss.nsampleschedulingcode, ss.speriod FROM samplescheduling ss JOIN sampleschedulinghistory ssh "
						+ " ON ss.nsampleschedulingcode = ssh.nsampleschedulingcode WHERE ss.sfromyear = '"+sfromyear
						+ "' AND ss.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " AND ss.nsitecode="+objUserInfo.getNmastersitecode()
						+ " AND ssh.nsampleschedulinghistorycode = any(select max(nsampleschedulinghistorycode) from sampleschedulinghistory where"
						+ " nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " AND nsitecode="+objUserInfo.getNmastersitecode()
						+ " group by nsampleschedulingcode ) and ssh.ntransactionstatus ="+Enumeration.TransactionStatus.PLANNED.gettransactionstatus();

		final List<SampleRequesting> periodList = jdbcTemplate.query(strQuery, new SampleRequesting());

		outputMap.put("periodList", periodList);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createSampleRequesting(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		SampleRequesting objSampleRequesting = objMapper.convertValue(inputMap.get("samplerequesting"),
				SampleRequesting.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//SWSM-78 added by rukshana included getFullYearin front end as sfromyear as string so removed below lines
//		int fromYear = Instant.parse(objSampleRequesting.getSfromyear()).atZone(ZoneId.systemDefault()).getYear();
//		String sfromyear = String.valueOf(fromYear);
		String sfromyear = objSampleRequesting.getSfromyear();
		objSampleRequesting.setScollectiondate(objSampleRequesting.getDcollectiondate() == null ? null
				: dateUtilityFunction.instantDateToStringWithFormat(objSampleRequesting.getDcollectiondate(),
						userInfo.getSsitedatetime()));

		//Modified the select query by sonia on 17th oct 2025 for jira id:SWSM-96
		final String queryString = "SELECT nsamplerequestingcode FROM samplerequesting WHERE sfromyear = N'"+sfromyear+"'"
				+ " AND sassignedto = '"+ stringUtilityFunction.replaceQuote(objSampleRequesting.getSassignedto())+"'"
				//+ "'  AND  scontactnumber= '"
				//+ objSampleRequesting.getScontactnumber() + "' AND semail = '" + objSampleRequesting.getSemail()
			//	+ "' AND dcollectiondate ='"+objSampleRequesting.getDcollectiondate()
				//+ "' AND nsamplerequestingcode <> " + objSampleRequesting.getNsamplerequestingcode()
				+ " AND nsitecode = " + userInfo.getNtranssitecode() + " AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final SampleRequesting availableSampleRequesting = (SampleRequesting) jdbcUtilityFunction
				.queryForObject(queryString, SampleRequesting.class, jdbcTemplate);

		if (availableSampleRequesting == null) {

		String lockSampleRequesting = "lock table locksamplerequesting"
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(lockSampleRequesting);

		final List<String> multilingualIdList = new ArrayList<>();
		final List<Object> saveSampleRequestingList = new ArrayList<>();
		String sequenceNoQuery = "select nsequenceno from seqnosamplescheduling where stablename ='samplerequesting'"
				+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class) + 1;

		//modified by sujatha ATE_274 from Scollectiondate to Dcollectiondate for a bug while inserting
		
		String insertQuery = "INSERT INTO samplerequesting ("
				   + "nsamplerequestingcode,nsampleschedulingcode,sfromyear,sassignedto,scontactnumber,semail,"
				   + "dcollectiondate,ntzcollectiondate,noffsetdcollectiondate,dmodifieddate, nsitecode, nstatus) "
				   + "values(" + nsequenceNo + "," + objSampleRequesting.getNsampleschedulingcode() + ", "
				   + " N'"+ stringUtilityFunction.replaceQuote(sfromyear) + "',"
				   + " N'"+ stringUtilityFunction.replaceQuote(objSampleRequesting.getSassignedto())+ "',"
				   + " N'"+ stringUtilityFunction.replaceQuote(objSampleRequesting.getScontactnumber())+ "',"
				   + " N'"+ stringUtilityFunction.replaceQuote(objSampleRequesting.getSemail())+"',"
				   + " '" + objSampleRequesting.getDcollectiondate() + "'," + userInfo.getNtimezonecode() + ", "
				   + " "+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+", "
				   + "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() + "," 
				   + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

		jdbcTemplate.execute(insertQuery);

		jdbcTemplate.execute("update seqnosamplescheduling set nsequenceno = " + nsequenceNo
				+ " where stablename='samplerequesting'");

		String historySeqQuery = "select nsequenceno from seqnosamplescheduling where stablename ='samplerequestinghistory'"
				+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		int historySeqNo = jdbcTemplate.queryForObject(historySeqQuery, Integer.class) + 1;

		String insertHistoryQuery = "INSERT INTO samplerequestinghistory (nsamplerequestinghistorycode, nsamplerequestingcode, ntransactionstatus, "
				  + "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, sremarks,dcollectiondate,ntzcollectiondate,noffsetdcollectiondate, "
				  + "dtransactiondate, ntztransactiondate, noffsetdtransactiondate, dmodifieddate, nsitecode, nstatus) "
				  + "VALUES (" + historySeqNo + ", " + nsequenceNo + ", " + Enumeration.TransactionStatus.PLANNED.gettransactionstatus() + ", "
				  + "" + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole()+", "
				  + " N'" +stringUtilityFunction.replaceQuote(objSampleRequesting.getSremarks()) + "', "
				  + " '" + objSampleRequesting.getDcollectiondate() +"'," + userInfo.getNtimezonecode() + ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				  + "'" + dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + userInfo.getNtimezonecode() + ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				  + "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

		jdbcTemplate.execute(insertHistoryQuery);

		jdbcTemplate.execute("update seqnosamplescheduling set nsequenceno = " + historySeqNo
				+ " where stablename='samplerequestinghistory'");

//		String locationSeqQuery = "select nsequenceno from seqnosamplescheduling where stablename ='samplerequestinglocation'"
//				+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//
//		int locationSeqNo = jdbcTemplate.queryForObject(locationSeqQuery, Integer.class) + 1;

//		String insertlocationQuery = "INSERT INTO samplerequestinglocation ("
//				+ "nsamplerequestinglocationcode,nsamplerequestingcode, nregioncode, ndistrictcode,ncitycode, nvillagecode,nsamplelocationcode,dmodifieddate,nsitecode,nstatus) "
//				+ "values(" + locationSeqNo + "," + nsequenceNo + "," + objSampleRequesting.getNregioncode() + ","
//				+ objSampleRequesting.getNdistrictcode() + "," + objSampleRequesting.getNcitycode() + ","
//				+ objSampleRequesting.getNvillagecode() + " ," + objSampleRequesting.getNsamplelocationcode() + ",'"
//				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
//
//		jdbcTemplate.execute(insertlocationQuery);
//
//		jdbcTemplate.execute("update seqnosamplescheduling set nsequenceno = " + locationSeqNo
//				+ " where stablename='samplerequestinglocation'");

		// Get current sequence number for location
		String locationSeqQuery = "SELECT nsequenceno FROM seqnosamplescheduling WHERE stablename ='samplerequestinglocation' "
				+ "AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		int locationSeqNo = jdbcTemplate.queryForObject(locationSeqQuery, Integer.class) + 1;

		// Get the list of location codes (comma-separated string from input)
		String locationCodesStr = (String) inputMap.get("nsamplelocationcode");
		List<Integer> locationCodes = Arrays.stream(locationCodesStr.split(",")).map(String::trim)
				.map(Integer::parseInt).collect(Collectors.toList());

		// Loop through each location code and insert
		//need to change in RankOf Query ...now commiting due to time constraint for UAT
		for (Integer locCode : locationCodes) {
			String insertLocationQuery = "INSERT INTO samplerequestinglocation ("
					+ "nsamplerequestinglocationcode, nsamplerequestingcode,ncentralsitecode, nregioncode, ndistrictcode, ncitycode, "
					+ "nvillagecode, nsamplelocationcode, dmodifieddate, nsitecode, nstatus) " + "VALUES ("
					+ locationSeqNo + ", " + nsequenceNo + ", "+ objSampleRequesting.getNcentralsitecode() + ", " + objSampleRequesting.getNregioncode() + ", "
					+ objSampleRequesting.getNdistrictcode() + ", " + objSampleRequesting.getNcitycode() + ", "
					+ objSampleRequesting.getNvillagecode() + ", " + locCode + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

			jdbcTemplate.execute(insertLocationQuery);
			locationSeqNo++; // increment for next row
		}

		// Update the sequence table after all inserts
		jdbcTemplate.execute("UPDATE seqnosamplescheduling SET nsequenceno = " + locationSeqNo
				+ " WHERE stablename='samplerequestinglocation'");

		objSampleRequesting.setSfromyear(sfromyear);
		objSampleRequesting.setNsamplerequestingcode(nsequenceNo);
		saveSampleRequestingList.add(objSampleRequesting);

		multilingualIdList.add("IDS_ADDSAMPLEREQUESTING");
		auditUtilityFunction.fnInsertAuditAction(saveSampleRequestingList, 1, null, multilingualIdList, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		 }
		return getSampleRequestingData(inputMap, userInfo);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getSampleRequesting(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		outputMap.putAll(filterStatus(inputMap, userInfo));
		outputMap.putAll((Map<String, Object>) getSampleRequestingData(inputMap, userInfo).getBody());
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public Map<String, Object> filterStatus(final Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> map = new HashMap<>();

		final String StrQuery = "SELECT ntranscode, coalesce(jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus  " + " FROM transactionstatus"
				+ " WHERE ntranscode IN (" + Enumeration.TransactionStatus.ALL.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.PLANNED.gettransactionstatus() + ") and nstatus = "
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

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getSampleRequestingData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<>();

		int fromYear;
		int toYear;

		if (inputMap.get("fromYear") != null && inputMap.get("toYear") != null) {
			fromYear = (int) inputMap.get("fromYear");
			toYear = (int) inputMap.get("toYear");
		} else {
			String currentUIDate = (String) inputMap.get("currentdate");
			if (currentUIDate != null && !currentUIDate.trim().isEmpty()) {
				int currentYear = Year.now().getValue();
				fromYear = currentYear;
				toYear = currentYear;
			} else {
				int currentYear = Year.now().getValue();
				fromYear = currentYear;
				toYear = currentYear;
			}
		}

		outputMap.put("fromYear", fromYear);
		outputMap.put("toYear", toYear);
		int ntransactionstatus = 0;
		if (inputMap.containsKey("ntranscode")) {
			ntransactionstatus = (int) inputMap.get("ntranscode");
		}

		String filterStatusQuery = "";
		if (ntransactionstatus > 0) {
			filterStatusQuery = " AND sh.ntransactionstatus = " + ntransactionstatus + " ";
		}

//		final String strQuery = "SELECT s.nsamplerequestingcode,s.nsampleschedulingcode, s.sfromyear, s.speriod, s.sassignedto,s.scontactnumber,s.semail, to_char(s.dcollectiondate, 'YYYY-MM-DD HH24:MI:SS') as scollectiondate,"
//				+ " s.nsitecode, s.nstatus, sh.ntransactionstatus, "
//				+ " COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
//				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus,f.ncolorcode,cl.scolorhexcode "
//				+ "FROM samplerequesting s "
//				+ "JOIN samplerequestinghistory sh ON sh.nsamplerequestingcode = s.nsamplerequestingcode "
//				+ "JOIN transactionstatus ts1 ON sh.ntransactionstatus = ts1.ntranscode "
//				+ "JOIN formwisestatuscolor f ON sh.ntransactionstatus = f.ntranscode "
//				+ "JOIN colormaster cl ON cl.ncolorcode = f.ncolorcode "
//				+ "WHERE sh.nsamplerequestinghistorycode = ANY ( " + "   SELECT MAX(sh2.nsamplerequestinghistorycode) "
//				+ "   FROM samplerequestinghistory sh2 "
//				+ "   JOIN samplerequesting s2 ON s2.nsamplerequestingcode = sh2.nsamplerequestingcode "
//				+ "   WHERE s2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "   AND sh2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "   AND s2.nsitecode = " + userInfo.getNtranssitecode() + " " + "   AND sh2.nsitecode = "
//				+ userInfo.getNtranssitecode() + " " + "   GROUP BY s2.nsamplerequestingcode " + ") "
//				+ "AND s.sfromyear::int BETWEEN " + fromYear + " AND " + toYear + " "
//				+ "AND s.nsamplerequestingcode > 0 " + "AND s.nsitecode = " + userInfo.getNtranssitecode() + " "
//				+ "AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "AND sh.nsitecode = " + userInfo.getNtranssitecode() + " " + "AND sh.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND ts1.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND f.nformcode="
//				+ userInfo.getNformcode() + " " + filterStatusQuery + "ORDER BY s.nsamplerequestingcode DESC";

		// Added samplescheduling for speriod by Gowtham R on Oct 2 jira id: SWSM-78
		final String strQuery = "SELECT s.nsamplerequestingcode, s.nsampleschedulingcode, s.sfromyear, ss.speriod, s.sassignedto,"
				+ " s.scontactnumber, s.semail, to_char(s.dcollectiondate, '"+userInfo.getSsitedate()+"') as scollectiondate,"
				
				// Scheduled date (latest 41)
				+ " (SELECT to_char(sh1.dcollectiondate, '"+userInfo.getSsitedate()+"') " // Changed dtransactiondate to dcollectiondate by Gowtham R on Oct 2 jira id: SWSM-78
				+ " FROM samplerequestinghistory sh1 "
				+ " WHERE sh1.nsamplerequestingcode = s.nsamplerequestingcode "
				+ " AND sh1.ntransactionstatus = "
				+ Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus() + " AND sh1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND sh1.nsitecode = s.nsitecode "
				+ " ORDER BY sh1.nsamplerequestinghistorycode DESC LIMIT 1) as sscheduleddate, "

				// Completed date (latest 25)
				+ " (SELECT to_char(sh2.dcollectiondate, '"+userInfo.getSsitedate()+"')" // Changed dtransactiondate to dcollectiondate by Gowtham R on Oct 2 jira id: SWSM-78
				+ " FROM samplerequestinghistory sh2"
				+ " WHERE sh2.nsamplerequestingcode = s.nsamplerequestingcode"
				+ " AND sh2.ntransactionstatus = "
				+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + " AND sh2.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND sh2.nsitecode = s.nsitecode "
				+ " ORDER BY sh2.nsamplerequestinghistorycode DESC LIMIT 1) as scompleteddate,"

				+ " s.nsitecode, s.nstatus, sh.ntransactionstatus, "
				+ " COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus, "
				+ " f.ncolorcode, cl.scolorhexcode "

				+ " FROM samplerequesting s "
				+ " JOIN samplerequestinghistory sh ON sh.nsamplerequestingcode = s.nsamplerequestingcode "
				+ " JOIN transactionstatus ts1 ON sh.ntransactionstatus = ts1.ntranscode "
				+ " JOIN formwisestatuscolor f ON sh.ntransactionstatus = f.ntranscode "
				+ " JOIN colormaster cl ON cl.ncolorcode = f.ncolorcode "
				+ " JOIN samplescheduling ss ON ss.nsampleschedulingcode = s.nsampleschedulingcode "

				+ " WHERE sh.nsamplerequestinghistorycode = ANY (SELECT MAX(sh2.nsamplerequestinghistorycode) "
				+ " FROM samplerequestinghistory sh2 "
				+ " JOIN samplerequesting s2 ON s2.nsamplerequestingcode = sh2.nsamplerequestingcode "
				+ " WHERE s2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND sh2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND s2.nsitecode = " + userInfo.getNtranssitecode() + " AND sh2.nsitecode = "
				+ userInfo.getNtranssitecode() + " GROUP BY s2.nsamplerequestingcode) "

				+ " AND s.sfromyear::int BETWEEN " + fromYear + " AND " + toYear + " AND s.nsamplerequestingcode > 0 "
				+ " AND s.nsitecode = " + userInfo.getNtranssitecode() + " AND s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND sh.nsitecode = "
				+ userInfo.getNtranssitecode() + " AND sh.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ts1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND f.nformcode = "
				+ userInfo.getNformcode() + " " + filterStatusQuery + "ORDER BY s.nsamplerequestingcode DESC";

		LOGGER.info("Get SampleRequesting Query: " + strQuery);
		final List<SampleRequesting> lstSampleRequesting = (List<SampleRequesting>) jdbcTemplate.query(strQuery,
				new SampleRequesting());
		if (!lstSampleRequesting.isEmpty()) {
			outputMap.put("sampleRequestingRecord", lstSampleRequesting);
			outputMap.put("selectedSampleRequesting", lstSampleRequesting.get(0));

			outputMap.putAll(
					(Map<String, Object>) getSampleRequestingFile(lstSampleRequesting.get(0).getNsamplerequestingcode(),
							userInfo).getBody());

			outputMap.putAll((Map<String, Object>) getAllSampleRequestingLocation(
					lstSampleRequesting.get(0).getNsamplerequestingcode(), userInfo).getBody());

		} else {
			outputMap.put("sampleRequestingRecord", null);
			outputMap.put("selectedSampleRequesting", null);
			outputMap.put("sampleRequestingFile", null);
			outputMap.put("sampleRequestingLocation", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getAllSampleRequestingLocation(final int nsamplerequestingcode,
			final UserInfo objUserInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		String strQuery = "SELECT " + " ssl.nsamplerequestinglocationcode, " + " ssl.nsamplerequestingcode, "
				+ " ssl.nregioncode, region.ssitename AS sregionname, "
				+ " ssl.ndistrictcode, district.ssitename AS sdistrictname, "
				+ " ssl.ncitycode, city.ssitename AS scityname, "
				+ " ssl.nvillagecode, village.svillagename AS svillagename, location.ssamplelocationname,location.nsamplelocationcode,"
				+ " ssl.dmodifieddate, " + " ssl.nstatus " + "FROM samplerequestinglocation ssl "
				+ "LEFT JOIN site region   ON ssl.nregioncode   = region.nsitecode "
				+ "LEFT JOIN site district ON ssl.ndistrictcode = district.nsitecode "
				+ "LEFT JOIN site city     ON ssl.ncitycode     = city.nsitecode "
				+ "LEFT JOIN villages village  ON ssl.nvillagecode  = village.nvillagecode "
				+ "LEFT JOIN samplelocation location ON ssl.nsamplelocationcode = location.nsamplelocationcode "
				+ "WHERE ssl.nsamplerequestingcode = " + nsamplerequestingcode + " AND ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssl.nsitecode = "
				+ objUserInfo.getNtranssitecode();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final List<SampleRequestingLocation> locationList = jdbcTemplate.query(strQuery,
				new SampleRequestingLocation());

		outputMap.put("sampleRequestingLocation", locationList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getSampleRequestingRecord(final int nsamplerequestingode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
//		final SampleScheduling sampleScheduling = getActiveSampleSchedulingById(nsamplerequestingode, userInfo);
//		if (sampleScheduling == null) {
//			return new ResponseEntity<>(
//					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
//							userInfo.getSlanguagefilename()),
//					HttpStatus.EXPECTATION_FAILED);
//		} else {

//		final String strQuery = "SELECT s.nsamplerequestingcode,s.nsampleschedulingcode, s.sfromyear, s.speriod, s.sassignedto,s.scontactnumber,s.semail,to_char(s.dcollectiondate, 'YYYY-MM-DD HH24:MI:SS') as scollectiondate, "
//				+ " s.nsitecode, s.nstatus, sh.ntransactionstatus, "
//				+ " COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
//				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus,f.ncolorcode,cl.scolorhexcode  "
//				+ "FROM samplerequesting s JOIN samplerequestinghistory sh ON sh.nsamplerequestingcode = s.nsamplerequestingcode "
//				+ "JOIN transactionstatus ts1 ON sh.ntransactionstatus = ts1.ntranscode "
//				+ "JOIN formwisestatuscolor f ON sh.ntransactionstatus = f.ntranscode "
//				+ "JOIN colormaster cl ON cl.ncolorcode = f.ncolorcode "
//				+ "WHERE sh.nsamplerequestinghistorycode = ANY ( " + " SELECT MAX(sh2.nsamplerequestinghistorycode) "
//				+ " FROM samplerequestinghistory sh2 "
//				+ " JOIN samplerequesting s2 ON s2.nsamplerequestingcode = sh2.nsamplerequestingcode "
//				+ " WHERE s2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ " AND sh2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ " AND s2.nsitecode = " + userInfo.getNtranssitecode() + " " + "      AND sh2.nsitecode = "
//				+ userInfo.getNtranssitecode() + " " + " GROUP BY s2.nsamplerequestingcode " + ") "
//				+ "AND s.nsamplerequestingcode > 0 " + "AND s.nsitecode = " + userInfo.getNtranssitecode() + " "
//				+ "AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "AND sh.nsitecode = " + userInfo.getNtranssitecode() + " " + "AND sh.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND ts1.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND s.nsamplerequestingcode= "
//				+ nsamplerequestingode + " " + "AND f.nformcode = " + userInfo.getNformcode() + " ORDER BY 1 DESC";

		// Added samplescheduling for speriod by Gowtham R on Oct 2 jira id: SWSM-78
		final String strQuery = "SELECT s.nsamplerequestingcode, s.nsampleschedulingcode, s.sfromyear,"
				+ " ss.speriod, s.sassignedto, s.scontactnumber, s.semail, to_char(s.dcollectiondate, '" 
				+ userInfo.getSsitedate() + "') as scollectiondate, "

				// Scheduled date (latest where status = SCHEDULED)
				+ " (SELECT to_char(sh1.dcollectiondate, '"+userInfo.getSsitedate()+"') " // Changed dtransactiondate to dcollectiondate by Gowtham R on Oct 2 jira id: SWSM-78
				+ " FROM samplerequestinghistory sh1 "
				+ " WHERE sh1.nsamplerequestingcode = s.nsamplerequestingcode "
				+ " AND sh1.ntransactionstatus = "
				+ Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus() + " AND sh1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND sh1.nsitecode = s.nsitecode "
				+ " ORDER BY sh1.nsamplerequestinghistorycode DESC LIMIT 1) as sscheduleddate, "

				// Completed date (latest where status = COMPLETED)
				+ " (SELECT to_char(sh2.dcollectiondate, '"+userInfo.getSsitedate()+"') " // Changed dtransactiondate to dcollectiondate by Gowtham R on Oct 2 jira id: SWSM-78
				+ " FROM samplerequestinghistory sh2 "
				+ " WHERE sh2.nsamplerequestingcode = s.nsamplerequestingcode "
				+ " AND sh2.ntransactionstatus = "
				+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + " AND sh2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND sh2.nsitecode = s.nsitecode "
				+ " ORDER BY sh2.nsamplerequestinghistorycode DESC LIMIT 1) as scompleteddate, "
								
				+ " s.nsitecode, s.nstatus, sh.ntransactionstatus, "
				+ " COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus, "
				+ " f.ncolorcode, cl.scolorhexcode "

				+ " FROM samplerequesting s "
				+ " JOIN samplerequestinghistory sh ON sh.nsamplerequestingcode = s.nsamplerequestingcode "
				+ " JOIN transactionstatus ts1 ON sh.ntransactionstatus = ts1.ntranscode "
				+ " JOIN formwisestatuscolor f ON sh.ntransactionstatus = f.ntranscode "
				+ " JOIN colormaster cl ON cl.ncolorcode = f.ncolorcode "
				+ " JOIN samplescheduling ss ON ss.nsampleschedulingcode = s.nsampleschedulingcode "

				+ " WHERE sh.nsamplerequestinghistorycode = ANY (SELECT MAX(sh2.nsamplerequestinghistorycode) "
				+ " FROM samplerequestinghistory sh2 "
				+ " JOIN samplerequesting s2 ON s2.nsamplerequestingcode = sh2.nsamplerequestingcode "
				+ " WHERE s2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND sh2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND s2.nsitecode = " + userInfo.getNtranssitecode() + " AND sh2.nsitecode = "
				+ userInfo.getNtranssitecode() + " GROUP BY s2.nsamplerequestingcode) "
				+ " AND s.nsamplerequestingcode > 0 AND s.nsitecode = " + userInfo.getNtranssitecode()
				+ " AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND sh.nsitecode = " + userInfo.getNtranssitecode() + " AND sh.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ts1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND s.nsamplerequestingcode = "
				+ nsamplerequestingode + " AND f.nformcode = " + userInfo.getNformcode()
				+ " ORDER BY s.nsamplerequestingcode DESC";

		LOGGER.info("Get Method" + strQuery);

		final List<SampleRequesting> lstSampleRequesting = (List<SampleRequesting>) jdbcTemplate.query(strQuery,
				new SampleRequesting());

		if (!lstSampleRequesting.isEmpty()) {
			outputMap.put("sampleRequestingRecord", lstSampleRequesting);
			outputMap.put("selectedSampleRequesting", lstSampleRequesting.get(0));
			outputMap.putAll(
					(Map<String, Object>) getSampleRequestingFile(lstSampleRequesting.get(0).getNsamplerequestingcode(),
							userInfo).getBody());
			outputMap.putAll((Map<String, Object>) getAllSampleRequestingLocation(
					lstSampleRequesting.get(0).getNsamplerequestingcode(), userInfo).getBody());

		} else {
			outputMap.put("sampleRequestingRecord", null);
			outputMap.put("selectedSampleRequesting", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
		// }
	}

	@Override
	public SampleRequesting getActiveSampleRequestingById(final int nsamplerequestingcode, final UserInfo userInfo)
			throws Exception {

		// Added samplescheduling for speriod by Gowtham R on Oct 2 jira id: SWSM-78
		final String query = "SELECT sr.*, to_char(sr.dcollectiondate, '"+userInfo.getSsitedate()+"') as scollectiondate, ss.speriod "
				+ " from samplerequesting sr JOIN samplescheduling ss ON ss.nsampleschedulingcode = sr.nsampleschedulingcode"
				+ " where sr.nsamplerequestingcode = " + nsamplerequestingcode 
				+ " and sr.nsitecode = " + userInfo.getNtranssitecode() + "  and  sr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ss.nsitecode = " + userInfo.getNmastersitecode() + "  and  ss.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return (SampleRequesting) jdbcUtilityFunction.queryForObject(query, SampleRequesting.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> updateSampleRequesting(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		SampleRequesting objSampleRequesting = objMapper.convertValue(inputMap.get("samplerequesting"),
				SampleRequesting.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//SWSM-78 added by rukshana included getFullYearin front end as sfromyear as string so removed below lines
//		int fromYear = Instant.parse(objSampleRequesting.getSfromyear()).atZone(ZoneId.systemDefault()).getYear();
//		String sfromyear = String.valueOf(fromYear);
		String sfromyear = objSampleRequesting.getSfromyear();

		final SampleRequesting sampleRequesting = getActiveSampleRequestingById(
				objSampleRequesting.getNsamplerequestingcode(), userInfo);
		if (sampleRequesting == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		// Removed speriod and added nsampleschedulingcode by Gowtham R on Oct 2 jira id: SWSM-78
		//Modified the select query by sonia on 17th oct 2025 for jira id:SWSM-96
		final String queryString = "SELECT nsamplerequestingcode FROM samplerequesting " + "WHERE sfromyear = N'"
				+ sfromyear + "' AND nsampleschedulingcode = " + objSampleRequesting.getNsampleschedulingcode() 
				+ " AND sassignedto = N'" + stringUtilityFunction.replaceQuote(objSampleRequesting.getSassignedto())
				//+ "'  AND  scontactnumber= '"
				//+ objSampleRequesting.getScontactnumber() + "' AND semail = '" + objSampleRequesting.getSemail()
				//+ "'"
		//		+ "' AND dcollectiondate ='"+objSampleRequesting.getDcollectiondate()
				+ "' AND nsamplerequestingcode <> " + objSampleRequesting.getNsamplerequestingcode()
				+ " AND nsitecode = " + userInfo.getNtranssitecode() + " AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final SampleRequesting availableSampleRequesting = (SampleRequesting) jdbcUtilityFunction
				.queryForObject(queryString, SampleRequesting.class, jdbcTemplate);

		if (availableSampleRequesting != null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

		List<Object> listBeforeUpdate = new ArrayList<>();
		listBeforeUpdate.add(sampleRequesting);

		final String updateQuery = "UPDATE samplerequesting SET sfromyear = N'" + sfromyear + "', "
				+ "nsampleschedulingcode =" + objSampleRequesting.getNsampleschedulingcode() + ", "
				+ "sassignedto = N'"+ stringUtilityFunction.replaceQuote(objSampleRequesting.getSassignedto()) + "', "
				+ "scontactnumber = N'" + stringUtilityFunction.replaceQuote(objSampleRequesting.getScontactnumber())+ "', "
				+ "" + "semail = N'" + stringUtilityFunction.replaceQuote(objSampleRequesting.getSemail())
				+ "', dcollectiondate = '" + objSampleRequesting.getDcollectiondate() + "',"
				+ "" + "dmodifieddate = '"	+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' "
				+ "WHERE nsamplerequestingcode = "+ objSampleRequesting.getNsamplerequestingcode() + " "
				+ "and nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "+ userInfo.getNtranssitecode();
 
		jdbcTemplate.execute(updateQuery);

		String updateHistoryQuery = "UPDATE samplerequestinghistory SET "
				+ " dcollectiondate = '" + objSampleRequesting.getDcollectiondate()+"',"
				+ " dmodifieddate ='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
			    + " WHERE nsamplerequestingcode = "+ objSampleRequesting.getNsamplerequestingcode();

		jdbcTemplate.execute(updateHistoryQuery);

		final SampleRequesting updatedSampleRequesting = getActiveSampleRequestingById(
				objSampleRequesting.getNsamplerequestingcode(), userInfo);
		List<Object> listAfterUpdate = new ArrayList<>();
		listAfterUpdate.add(updatedSampleRequesting);
		List<String> multilingualIDList = new ArrayList<>();
		multilingualIDList.add("IDS_EDITSAMPLEREQUESTING");
		auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo);

		return getSampleRequestingDatas(inputMap, userInfo);
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getSampleRequestingDatas(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		SampleRequesting objSampleRequesting = objMapper.convertValue(inputMap.get("samplerequesting"),
				SampleRequesting.class);
//		final String strQuery = "SELECT s.nsamplerequestingcode,s.nsampleschedulingcode, s.sfromyear, s.speriod,  s.sassignedto,s.scontactnumber,s.semail,to_char(s.dcollectiondate, 'YYYY-MM-DD HH24:MI:SS') as scollectiondate,to_char(sh.dtransactiondate, 'YYYY-MM-DD HH24:MI:SS') as sscheduleddate, "
//				+ " s.nsitecode, s.nstatus, sh.ntransactionstatus, "
//				+ " COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
//				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus,f.ncolorcode,cl.scolorhexcode "
//				+ "FROM samplerequesting s "
//				+ "JOIN samplerequestinghistory sh ON sh.nsamplerequestingcode = s.nsamplerequestingcode "
//				+ "JOIN transactionstatus ts1 ON sh.ntransactionstatus = ts1.ntranscode "
//				+ "JOIN formwisestatuscolor f ON sh.ntransactionstatus = f.ntranscode "
//				+ "JOIN colormaster cl ON cl.ncolorcode = f.ncolorcode "
//				+ "WHERE sh.nsamplerequestinghistorycode = ANY ( " + "   SELECT MAX(sh2.nsamplerequestinghistorycode) "
//				+ "   FROM samplerequestinghistory sh2 "
//				+ "   JOIN samplerequesting s2 ON s2.nsamplerequestingcode = sh2.nsamplerequestingcode "
//				+ "   WHERE s2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "   AND sh2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "   AND s2.nsitecode = " + userInfo.getNtranssitecode() + " " + "   AND sh2.nsitecode = "
//				+ userInfo.getNtranssitecode() + " " + "   GROUP BY s2.nsamplerequestingcode " + ") "
//				+ "AND s.nsitecode = " + userInfo.getNtranssitecode() + " " + "AND s.nsamplerequestingcode = "
//				+ objSampleRequesting.getNsamplerequestingcode() + " " + "AND s.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND sh.nsitecode = "
//				+ userInfo.getNtranssitecode() + " " + "AND sh.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND ts1.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND f.nformcode="
//				+ userInfo.getNformcode() + " " + "ORDER BY s.nsamplerequestingcode DESC";

		// Added samplescheduling for speriod by Gowtham R on Oct 2 jira id: SWSM-78
		final String strQuery = "SELECT s.nsamplerequestingcode, s.nsampleschedulingcode, s.sfromyear, ss.speriod, s.sassignedto,"
				+ " s.scontactnumber, s.semail, to_char(s.dcollectiondate, '"+userInfo.getSsitedate()+"') as scollectiondate, "

				// scheduled date (latest for status = 41)
				+ " (SELECT to_char(sh1.dcollectiondate, '"+userInfo.getSsitedate()+"') " // Changed dtransactiondate to dcollectiondate by Gowtham R on Oct 2 jira id: SWSM-78
				+ " FROM samplerequestinghistory sh1 "
				+ " WHERE sh1.nsamplerequestingcode = s.nsamplerequestingcode "
				+ " AND sh1.ntransactionstatus = "
				+ Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus() // scheduled
				+ " AND sh1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND sh1.nsitecode = s.nsitecode "
				+ " ORDER BY sh1.nsamplerequestinghistorycode DESC LIMIT 1) as sscheduleddate, "

				// completed date (latest for status = 25)
				+ " (SELECT to_char(sh2.dcollectiondate, '"+userInfo.getSsitedate()+"') " // Changed dtransactiondate to dcollectiondate by Gowtham R on Oct 2 jira id: SWSM-78
				+ " FROM samplerequestinghistory sh2 "
				+ " WHERE sh2.nsamplerequestingcode = s.nsamplerequestingcode "
				+ " AND sh2.ntransactionstatus = "
				+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() // completed
				+ " AND sh2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND sh2.nsitecode = s.nsitecode "
				+ " ORDER BY sh2.nsamplerequestinghistorycode DESC LIMIT 1) as scompleteddate, "

				+ " s.nsitecode, s.nstatus, sh.ntransactionstatus, "
				+ " COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() 
				+ "', ts1.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus, "
				+ " f.ncolorcode, cl.scolorhexcode "

				+ " FROM samplerequesting s "
				+ " JOIN samplerequestinghistory sh ON sh.nsamplerequestingcode = s.nsamplerequestingcode "
				+ " JOIN transactionstatus ts1 ON sh.ntransactionstatus = ts1.ntranscode "
				+ " JOIN formwisestatuscolor f ON sh.ntransactionstatus = f.ntranscode "
				+ " JOIN colormaster cl ON cl.ncolorcode = f.ncolorcode "
				+ " JOIN samplescheduling ss ON ss.nsampleschedulingcode = s.nsampleschedulingcode "

				+ " WHERE sh.nsamplerequestinghistorycode = ANY (SELECT MAX(sh2.nsamplerequestinghistorycode) "
				+ " FROM samplerequestinghistory sh2 "
				+ " JOIN samplerequesting s2 ON s2.nsamplerequestingcode = sh2.nsamplerequestingcode "
				+ " WHERE s2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND sh2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND s2.nsitecode = " + userInfo.getNtranssitecode() + " AND sh2.nsitecode = "
				+ userInfo.getNtranssitecode() + " GROUP BY s2.nsamplerequestingcode) "
				+ "AND s.nsitecode = " + userInfo.getNtranssitecode() + " AND s.nsamplerequestingcode = "
				+ objSampleRequesting.getNsamplerequestingcode() + " AND s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND sh.nsitecode = "
				+ userInfo.getNtranssitecode() + " AND sh.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ts1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND f.nformcode = "
				+ userInfo.getNformcode() + " ORDER BY s.nsamplerequestingcode DESC";

		LOGGER.info("Get SampleRequesting Query: " + strQuery);

		final List<SampleRequesting> lstSampleRequesting = (List<SampleRequesting>) jdbcTemplate.query(strQuery,
				new SampleRequesting());

		if (!lstSampleRequesting.isEmpty()) {
			outputMap.put("selectedSampleRequesting", lstSampleRequesting.get(0));
			ResponseEntity<Object> response = SampleRequestingData(inputMap, userInfo);
			outputMap.putAll((Map<String, Object>) response.getBody());
			outputMap.putAll(
					(Map<String, Object>) getSampleRequestingFile(lstSampleRequesting.get(0).getNsamplerequestingcode(),
							userInfo).getBody());
			outputMap.putAll((Map<String, Object>) getAllSampleRequestingLocation(
					lstSampleRequesting.get(0).getNsamplerequestingcode(), userInfo).getBody());
		} else {
			outputMap.put("sampleRequestingRecord", null);
			outputMap.put("selectedSampleRequesting", null);
			outputMap.put("sampleRequestingFile", null);
			outputMap.put("sampleRequestingLocation", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> deleteSampleRequesting(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		SampleRequesting objSampleRequesting = objMapper.convertValue(inputMap.get("samplerequesting"),
				SampleRequesting.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

	//	if ("Planned".equals(objSampleRequesting.getStransdisplaystatus())) {
//		if (String.valueOf(Enumeration.TransactionStatus.PLANNED.gettransactionstatus())
//				.equals(objSampleRequesting.getStransdisplaystatus())) {
         if(objSampleRequesting.getNtransactionstatus() == Enumeration.TransactionStatus.PLANNED.gettransactionstatus())
            {
		
		
			final SampleRequesting sampleRequesting = getActiveSampleRequestingById(
					objSampleRequesting.getNsamplerequestingcode(), userInfo);
			if (sampleRequesting == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
			final List<Object> deletedSampleRequesting = new ArrayList<>();
			final List<String> multilingualIdList = new ArrayList<>();
			
			final String query = "select * from samplerequestingfile where nsamplerequestingcode=" + objSampleRequesting.getNsamplerequestingcode() + "";
			List<SampleRequestingFile> lstSampleRequestingFile = jdbcTemplate.query(query, new SampleRequestingFile());
			
			
			String updateQueryString = "UPDATE samplerequesting SET dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " WHERE nsamplerequestingcode="
					+ objSampleRequesting.getNsamplerequestingcode() + ";";
			jdbcTemplate.execute(updateQueryString);

			updateQueryString = "UPDATE samplerequestinghistory SET nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dtransactiondate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nsamplerequestingcode="
					+ objSampleRequesting.getNsamplerequestingcode() + ";";
			jdbcTemplate.execute(updateQueryString);

			updateQueryString = "UPDATE samplerequestinglocation SET nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nsamplerequestingcode="
					+ objSampleRequesting.getNsamplerequestingcode() + ";";
			jdbcTemplate.execute(updateQueryString);
			
			if(lstSampleRequestingFile.size()>0) {
				//jira id swsm -99 bad sql grammer 
				updateQueryString= "update samplerequestingfile set nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()+", "
								 + "dmodifieddate='"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' "
								 + "where nsamplerequestingcode=" + objSampleRequesting.getNsamplerequestingcode() + ";";
				jdbcTemplate.execute(updateQueryString);
			}

			objSampleRequesting.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			deletedSampleRequesting.add(objSampleRequesting);
			multilingualIdList.add("IDS_DELETESAMPLEREQUESTING");
			auditUtilityFunction.fnInsertAuditAction(deletedSampleRequesting, 1, null, multilingualIdList, userInfo);
			return getSampleRequestingData(inputMap, userInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTPLANNEDRECORDTODELETE",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> createSampleRequestingFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final String sQuery = " lock  table locksamplerequestingfile"
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<SampleRequestingFile> lstReqSampleRequestingFile = objMapper.readValue(
				request.getParameter("sampleRequestingFile"), new TypeReference<List<SampleRequestingFile>>() {
				});
		if (lstReqSampleRequestingFile != null && lstReqSampleRequestingFile.size() > 0) {
			final SampleRequestingHistory objSampleRequestingHistory = checKSampleRequestingIsPresent(
					lstReqSampleRequestingFile.get(0).getNsamplerequestingcode(), objUserInfo);
			if (objSampleRequestingHistory != null) {
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (lstReqSampleRequestingFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP
						.gettype()) {
					sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
				}
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo)
							.truncatedTo(ChronoUnit.SECONDS);
					final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
					final int noffset = dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid());
					lstReqSampleRequestingFile.forEach(objtf -> {
						objtf.setDcreateddate(instantDate);
						if (objtf.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							objtf.setDcreateddate(instantDate);
							objtf.setNoffsetdcreateddate(noffset);
							objtf.setScreateddate(sattachmentDate.replace("T", " "));
						}
					});

					String sequencequery = "select nsequenceno from seqnosamplescheduling where stablename ='samplerequestingfile'  "
							+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencequery, Integer.class,
							jdbcTemplate);
					nsequenceno++;
					String insertquery = "Insert into samplerequestingfile(nsamplerequestingfilecode,nsamplerequestingcode,nlinkcode,nattachmenttypecode,"
							+ "sfilename,sdescription,nfilesize,dcreateddate,noffsetdcreateddate,ntzcreateddate,ssystemfilename,dmodifieddate,nsitecode,nstatus)"
							+ "values (" + nsequenceno + ","
							+ lstReqSampleRequestingFile.get(0).getNsamplerequestingcode() + ","
							+ lstReqSampleRequestingFile.get(0).getNlinkcode() + ","
							+ lstReqSampleRequestingFile.get(0).getNattachmenttypecode() + "," + " N'"
							+ stringUtilityFunction.replaceQuote(lstReqSampleRequestingFile.get(0).getSfilename())
							+ "',N'"
							+ stringUtilityFunction.replaceQuote(lstReqSampleRequestingFile.get(0).getSdescription())
							+ "'," + lstReqSampleRequestingFile.get(0).getNfilesize() + "," + " '"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ lstReqSampleRequestingFile.get(0).getNoffsetdcreateddate() + ","
							+ objUserInfo.getNtimezonecode() + ",N'"
							+ lstReqSampleRequestingFile.get(0).getSsystemfilename() + "','"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', "
							+ objUserInfo.getNtranssitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(insertquery);
					String updatequery = "update seqnosamplescheduling set nsequenceno =" + nsequenceno
							+ " where stablename ='samplerequestingfile'" + "  and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					jdbcTemplate.execute(updatequery);
					final List<String> multilingualIDList = new ArrayList<>();
					multilingualIDList.add(
							lstReqSampleRequestingFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP
									.gettype() ? "IDS_ADDSAMPLEREQUESTINGFILE" : "IDS_ADDSAMPLEREQUESTINGLINK");
					final List<Object> listObject = new ArrayList<Object>();
					String auditqry = "select * from samplerequestingfile where nsamplerequestingcode = "
							+ lstReqSampleRequestingFile.get(0).getNsamplerequestingcode() + " and nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsamplerequestingfilecode = " + nsequenceno + " and nsitecode = "
							+ objUserInfo.getNtranssitecode();
					final List<SampleRequestingFile> lstvalidate = (List<SampleRequestingFile>) jdbcTemplate
							.query(auditqry, new SampleRequestingFile());
					listObject.add(lstvalidate);
					auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLEREQUESTINGALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			return (getSampleRequestingFile(lstReqSampleRequestingFile.get(0).getNsamplerequestingcode(), objUserInfo));
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method will access the DAO layer that is used to check the active
	 * sampleschedulingfile object based on the specified nsampleschedulingcode.
	 * 
	 * @param nsampleschedulingcode [int] primary key of SampleScheduling object
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         SampleScheduling object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public SampleRequestingHistory checKSampleRequestingIsPresent(final int nsamplerequestingcode,
			final UserInfo objUserInfo) throws Exception {
		String strQuery = "select nsamplerequestingcode from samplerequesting where" + " nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplerequestingcode = "
				+ nsamplerequestingcode + " and nsitecode = " + objUserInfo.getNtranssitecode();

		SampleRequestingHistory objSampleRequestingHistory = (SampleRequestingHistory) jdbcUtilityFunction
				.queryForObject(strQuery, SampleRequestingHistory.class, jdbcTemplate);

		return objSampleRequestingHistory;
	}

	public ResponseEntity<Object> getSampleRequestingFile(final int nsamplerequestingcode, final UserInfo objUserInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		String query = "select tf.noffsetdcreateddate,tf.nsamplerequestingfilecode,"
				+ "(select  count(nsamplerequestingfilecode) from samplerequestingfile where nsamplerequestingfilecode>0"
				+ " and nsamplerequestingcode = " + nsamplerequestingcode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") as ncount,tf.sdescription,"
				+ " tf.nsamplerequestingfilecode as nprimarycode,tf.sfilename,tf.nsamplerequestingcode,tf.ssystemfilename,"
				+ " tf.nattachmenttypecode,coalesce(at.jsondata->'sattachmenttype'->>'"
				+ objUserInfo.getSlanguagetypecode() + "',"
				+ "	at.jsondata->'sattachmenttype'->>'en-US') as sattachmenttype, case when tf.nlinkcode=-1 then '-' else lm.jsondata->>'slinkname'"
				+ " end slinkname, tf.nfilesize," + " case when tf.nattachmenttypecode= "
				+ Enumeration.AttachmentType.LINK.gettype() + " then '-' else" + " COALESCE(TO_CHAR(tf.dcreateddate,'"
				+ objUserInfo.getSpgsitedatetime() + "'),'-') end  as screateddate, "
				+ " tf.nlinkcode, case when tf.nlinkcode = -1 then tf.nfilesize::varchar(1000) else '-' end sfilesize"
				+ " from samplerequestingfile tf,attachmenttype at, linkmaster lm  "
				+ " where at.nattachmenttypecode = tf.nattachmenttypecode and at.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and lm.nlinkcode = tf.nlinkcode and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nsitecode = "
				+ objUserInfo.getNmastersitecode() + " and tf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.nsitecode = "
				+ objUserInfo.getNtranssitecode() + " and tf.nsamplerequestingcode=" + nsamplerequestingcode
				+ " order by tf.nsamplerequestingfilecode;";
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final List<SampleRequestingFile> lstSampleRequestingfile = objMapper
				.convertValue(
						dateUtilityFunction.getSiteLocalTimeFromUTC(
								jdbcTemplate.query(query, new SampleRequestingFile()), Arrays.asList("screateddate"),
								Arrays.asList(objUserInfo.getStimezoneid()), objUserInfo, false, null, false),
						new TypeReference<List<SampleRequestingFile>>() {
						});

		outputMap.put("sampleRequestingFile", lstSampleRequestingfile);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> editSampleRequestingFile(final SampleRequestingFile objSampleRequestingFile,
			final UserInfo objUserInfo) throws Exception {
		final String sEditQuery = "select  tf.nsamplerequestingfilecode, tf.nsamplerequestingcode, tf.nlinkcode, tf.nattachmenttypecode, "
				+ " tf.sfilename, tf.sdescription, tf.nfilesize,"
				+ " tf.ssystemfilename,  lm.jsondata->>'slinkname' as slinkname"
				+ " from samplerequestingfile tf, linkmaster lm,samplerequesting s where lm.nlinkcode = tf.nlinkcode"
				+ " and tf.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tf.nsitecode =" + objUserInfo.getNtranssitecode() + " and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nsitecode ="
				+ objUserInfo.getNmastersitecode() + " and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode ="
				+ objUserInfo.getNtranssitecode()
				+ " and tf.nsamplerequestingcode=s.nsamplerequestingcode and tf.nsamplerequestingfilecode = "
				+ objSampleRequestingFile.getNsamplerequestingfilecode();
		final SampleRequestingFile objTF = (SampleRequestingFile) jdbcUtilityFunction.queryForObject(sEditQuery,
				SampleRequestingFile.class, jdbcTemplate);
		if (objTF != null) {
			return new ResponseEntity<Object>(objTF, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateSampleRequestingFile(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final List<SampleRequestingFile> lstSampleRequestingFile = objMapper.readValue(
				request.getParameter("sampleRequestingFile"), new TypeReference<List<SampleRequestingFile>>() {
				});
		if (lstSampleRequestingFile != null && lstSampleRequestingFile.size() > 0) {
			final SampleRequestingFile objSampleRequestingFile = lstSampleRequestingFile.get(0);
			final SampleRequestingHistory objSampleRequestingHistory = checKSampleRequestingIsPresent(
					objSampleRequestingFile.getNsamplerequestingcode(), objUserInfo);
			if (objSampleRequestingHistory != null) {
				final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if (objSampleRequestingFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
					}
				}
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final String sQuery = "select * from samplerequestingfile where" + " nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsamplerequestingfilecode = "
							+ objSampleRequestingFile.getNsamplerequestingfilecode() + " and nsitecode ="
							+ objUserInfo.getNtranssitecode();
					final SampleRequestingFile objTF = (SampleRequestingFile) jdbcUtilityFunction.queryForObject(sQuery,
							SampleRequestingFile.class, jdbcTemplate);
					if (objTF != null) {
						String ssystemfilename = "";
						if (objSampleRequestingFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
								.gettype()) {
							ssystemfilename = objSampleRequestingFile.getSsystemfilename();
						}
						final String sUpdateQuery = "update samplerequestingfile set sfilename=N'"
								+ stringUtilityFunction.replaceQuote(objSampleRequestingFile.getSfilename()) + "',"
								+ " sdescription=N'"
								+ stringUtilityFunction.replaceQuote(objSampleRequestingFile.getSdescription())
								+ "', ssystemfilename= N'" + ssystemfilename + "'," + " nattachmenttypecode = "
								+ objSampleRequestingFile.getNattachmenttypecode() + ", nlinkcode="
								+ objSampleRequestingFile.getNlinkcode() + "," + " nfilesize = "
								+ objSampleRequestingFile.getNfilesize() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' "
								+ "  where nsamplerequestingfilecode = "
								+ objSampleRequestingFile.getNsamplerequestingfilecode();
						objSampleRequestingFile.setDcreateddate(objTF.getDcreateddate());
						jdbcTemplate.execute(sUpdateQuery);
						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<Object>();
						multilingualIDList
								.add(objSampleRequestingFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
										.gettype() ? "IDS_EDITSAMPLEREQUESTINGFILE" : "IDS_EDITSAMPLEREQUESTINGLINK");
						lstOldObject.add(objTF);
						auditUtilityFunction.fnInsertAuditAction(lstSampleRequestingFile, 2, lstOldObject,
								multilingualIDList, objUserInfo);
						return (getSampleRequestingFile(objSampleRequestingFile.getNsamplerequestingcode(),
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
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLEREQUESTINGALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> deleteSampleRequestingFile(SampleRequestingFile objSampleRequestingFile,
			UserInfo objUserInfo) throws Exception {
		final SampleRequestingHistory objSampleRequestingHistory = checKSampleRequestingIsPresent(
				objSampleRequestingFile.getNsamplerequestingcode(), objUserInfo);
		if (objSampleRequestingHistory != null) {
			if (objSampleRequestingHistory != null) {
				final String sQuery = "select * from samplerequestingfile where" + " nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsamplerequestingfilecode = " + objSampleRequestingFile.getNsamplerequestingfilecode()
						+ "  and nsitecode = " + objUserInfo.getNtranssitecode();
				final SampleRequestingFile objTF = (SampleRequestingFile) jdbcUtilityFunction.queryForObject(sQuery,
						SampleRequestingFile.class, jdbcTemplate);
				if (objTF != null) {
					if (objSampleRequestingFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					} else {
						objSampleRequestingFile.setScreateddate(null);
					}
					final String sUpdateQuery = "update samplerequestingfile set" + "  dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'" + ", nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ " where nsamplerequestingfilecode = "
							+ objSampleRequestingFile.getNsamplerequestingfilecode();
					jdbcTemplate.execute(sUpdateQuery);
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> lstObject = new ArrayList<>();
					multilingualIDList.add(
							objSampleRequestingFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
									? "IDS_DELETESAMPLEREQUESTINGFILE"
									: "IDS_DELETESAMPLEREQUESTINGLINK");
					lstObject.add(objSampleRequestingFile);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			return getSampleRequestingFile(objSampleRequestingFile.getNsamplerequestingcode(), objUserInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLEREQUESTINGALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> viewAttachedSampleRequestingFile(SampleRequestingFile objSampleRequestingFile,
			UserInfo objUserInfo) throws Exception {
		Map<String, Object> map = new HashMap<>();
		final SampleRequestingHistory objSampleRequestingHistory = checKSampleRequestingIsPresent(
				objSampleRequestingFile.getNsamplerequestingcode(), objUserInfo);
		if (objSampleRequestingHistory != null) {
			String sQuery = "select * from samplerequestingfile where" + " nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplerequestingfilecode = "
					+ objSampleRequestingFile.getNsamplerequestingfilecode() + " and nsitecode ="
					+ objUserInfo.getNtranssitecode();
			final SampleRequestingFile objTF = (SampleRequestingFile) jdbcUtilityFunction.queryForObject(sQuery,
					SampleRequestingFile.class, jdbcTemplate);

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
					objSampleRequestingFile.setScreateddate(null);
				}
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> lstObject = new ArrayList<>();
				multilingualIDList.add(
						objSampleRequestingFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
								? "IDS_VIEWSAMPLEREQUESTINGFILE"
								: "IDS_VIEWSAMPLEREQUESTINGLINK");
				lstObject.add(objSampleRequestingFile);
				auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
			} else {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<Object>(commonFunction
					.getMultilingualMessage("IDS_SAMPLEREQUESTINGALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSampleRequestingLocation(final int nsamplerequestinglocationcode,
			final UserInfo objUserInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		String strQuery = "SELECT " + " ssl.nsamplerequestinglocationcode, " + " ssl.nsamplerequestingcode, "
				+ " ssl.nregioncode, region.ssitename AS sregionname, "
				+ " ssl.ndistrictcode, district.ssitename AS sdistrictname, "
				+ " ssl.ncitycode, city.ssitename AS scityname, ssl.ncentralsitecode, state.ssitename AS sstatename, "
				+ " ssl.nvillagecode, village.svillagename AS svillagename, location.ssamplelocationname,location.nsamplelocationcode,"
				+ " ssl.dmodifieddate, " + " ssl.nstatus " + "FROM samplerequestinglocation ssl "
				+ "LEFT JOIN site region   ON ssl.nregioncode   = region.nsitecode "
				+ "LEFT JOIN site district ON ssl.ndistrictcode = district.nsitecode "
				+ "LEFT JOIN site city     ON ssl.ncitycode     = city.nsitecode "
				+ "LEFT JOIN site state    ON ssl.ncentralsitecode     = state.nsitecode "
				+ "LEFT JOIN villages village  ON ssl.nvillagecode  = village.nvillagecode "
				+ "LEFT JOIN samplelocation location ON ssl.nsamplelocationcode = location.nsamplelocationcode "
				+ "WHERE ssl.nsamplerequestinglocationcode = " + nsamplerequestinglocationcode + " AND ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssl.nsitecode = "
				+ objUserInfo.getNtranssitecode();

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final List<SampleRequestingLocation> locationList = jdbcTemplate.query(strQuery,
				new SampleRequestingLocation());

		outputMap.put("sampleRequestingLocation", locationList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateSampleRequestingLocation(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		SampleRequestingLocation objSampleRequestingLocation = objMapper
				.convertValue(inputMap.get("samplerequestinglocation"), SampleRequestingLocation.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		final SampleRequesting sampleRequesting = getActiveSampleRequestingById(
				objSampleRequestingLocation.getNsamplerequestingcode(), userInfo);
		if (sampleRequesting == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final SampleRequesting sampleRequestingLocation = getAuditSampleRequestingLocationById(
				objSampleRequestingLocation.getNsamplerequestinglocationcode(), userInfo);
		if (sampleRequestingLocation == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final SampleRequestingLocation sampleRequestingLocations = getSampleRequestingLocationById(
				objSampleRequestingLocation, userInfo);

		if (sampleRequestingLocations != null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		List<Object> listBeforeUpdate = new ArrayList<>();
		listBeforeUpdate.add(sampleRequestingLocation);

		String locksamplerequestinglocation = "lock table locksamplerequestinglocation"
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(locksamplerequestinglocation);

		final List<String> multilingualIdList = new ArrayList<>();

		String updateQuery = "UPDATE samplerequestinglocation SET " + "nregioncode = "
				+ objSampleRequestingLocation.getNregioncode() + ", " + "ndistrictcode = "
				+ objSampleRequestingLocation.getNdistrictcode() + ", " + "ncitycode = "
				+ objSampleRequestingLocation.getNcitycode() + ", " + "nvillagecode = "
				+ objSampleRequestingLocation.getNvillagecode() + ", " + "nsamplelocationcode = "
				+ objSampleRequestingLocation.getNsamplelocationcode() + ", " + "dmodifieddate = '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "nsitecode = "
				+ userInfo.getNtranssitecode() + ", " + "nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " WHERE nsamplerequestinglocationcode = "
				+ objSampleRequestingLocation.getNsamplerequestinglocationcode() + " and nsamplerequestingcode = "
				+ objSampleRequestingLocation.getNsamplerequestingcode();

		jdbcTemplate.execute(updateQuery);

		final SampleRequesting updatedSampleRequesting = getAuditSampleRequestingLocationById(
				objSampleRequestingLocation.getNsamplerequestinglocationcode(), userInfo);

		List<Object> listAfterUpdate = new ArrayList<>();
		listAfterUpdate.add(updatedSampleRequesting);

		multilingualIdList.add("IDS_EDITSAMPLEREQUESTINGLOCATION");
		auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIdList, userInfo);

		return getAllSampleRequestingLocation(objSampleRequestingLocation.getNsamplerequestingcode(), userInfo);
	}

	public SampleRequesting getAuditSampleRequestingLocationById(final int nsamplerequestinglocationcode,
			final UserInfo userInfo) throws Exception {

		String strQuery = "SELECT " + " ssl.nsamplerequestinglocationcode, " + " ssl.nsamplerequestingcode, "
				+ " ssl.nregioncode, region.ssitename AS sregionname, "
				+ " ssl.ndistrictcode, district.ssitename AS sdistrictname, "
				+ " ssl.ncitycode, city.ssitename AS scityname, "
				+ " ssl.nvillagecode, village.svillagename AS svillagename, location.ssamplelocationname,location.nsamplelocationcode,"
				+ " ssl.dmodifieddate, " + " ssl.nstatus " + "FROM samplerequestinglocation ssl "
				+ "LEFT JOIN site region   ON ssl.nregioncode   = region.nsitecode "
				+ "LEFT JOIN site district ON ssl.ndistrictcode = district.nsitecode "
				+ "LEFT JOIN site city     ON ssl.ncitycode     = city.nsitecode "
				+ "LEFT JOIN villages village  ON ssl.nvillagecode  = village.nvillagecode "
				+ "LEFT JOIN samplelocation location ON ssl.nsamplelocationcode = location.nsamplelocationcode "
				+ "WHERE ssl.nsamplerequestinglocationcode = " + nsamplerequestinglocationcode + " AND ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ssl.nsitecode = "
				+ userInfo.getNtranssitecode();

		return (SampleRequesting) jdbcUtilityFunction.queryForObject(strQuery, SampleRequesting.class, jdbcTemplate);

	}

	public SampleRequestingLocation getSampleRequestingLocationById(
			final SampleRequestingLocation objSampleRequestingLocation, final UserInfo userInfo) throws Exception {

		String strQuery = "SELECT " + "ssl.nsamplerequestinglocationcode, " + "ssl.nsamplerequestingcode, "
				+ "ssl.nregioncode, region.ssitename AS sregionname, "
				+ "ssl.ndistrictcode, district.ssitename AS sdistrictname, "
				+ "ssl.ncitycode, city.ssitename AS scityname, "
				+ "ssl.nvillagecode, village.svillagename AS svillagename, "
				+ "location.ssamplelocationname, location.nsamplelocationcode, " + "ssl.dmodifieddate, ssl.nstatus "
				+ "FROM samplerequestinglocation ssl "
				+ "LEFT JOIN site region   ON ssl.nregioncode   = region.nsitecode "
				+ "LEFT JOIN site district ON ssl.ndistrictcode = district.nsitecode "
				+ "LEFT JOIN site city     ON ssl.ncitycode     = city.nsitecode "
				+ "LEFT JOIN villages village  ON ssl.nvillagecode  = village.nvillagecode "
				+ "LEFT JOIN samplelocation location ON ssl.nsamplelocationcode = location.nsamplelocationcode "
				+ "WHERE ssl.nsamplerequestingcode = " + objSampleRequestingLocation.getNsamplerequestingcode()
				+ " AND ssl.nregioncode = " + objSampleRequestingLocation.getNregioncode() + " AND ssl.ndistrictcode = "
				+ objSampleRequestingLocation.getNdistrictcode() + " AND ssl.ncitycode = "
				+ objSampleRequestingLocation.getNcitycode() + " AND ssl.nvillagecode = "
				+ objSampleRequestingLocation.getNvillagecode() + " AND ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ssl.nsitecode = "
				+ userInfo.getNtranssitecode() + " AND ssl.nsamplelocationcode="
				+ objSampleRequestingLocation.getNsamplelocationcode();

		// If updating an existing record, exclude it from results
		if (objSampleRequestingLocation.getNsamplerequestinglocationcode() > 0) {
			strQuery += " AND ssl.nsamplerequestinglocationcode <> "
					+ objSampleRequestingLocation.getNsamplerequestinglocationcode();
		}

		// Execute the query
		return (SampleRequestingLocation) jdbcUtilityFunction.queryForObject(strQuery, SampleRequestingLocation.class,
				jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> deleteSampleRequestingLocation(Map<String, Object> inputMap, UserInfo objUserInfo)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		SampleRequestingLocation objSampleRequesting = objMapper.convertValue(inputMap.get("sampleRequestingLocation"),
				SampleRequestingLocation.class);

		final SampleRequesting sampleRequesting = getActiveSampleRequestingById(
				objSampleRequesting.getNsamplerequestingcode(), objUserInfo);
		if (sampleRequesting == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final SampleRequesting objSampleRequestingLocation = getAuditSampleRequestingLocationById(
				objSampleRequesting.getNsamplerequestinglocationcode(), objUserInfo);
		if (objSampleRequestingLocation == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final List<Object> deletedSampleRequesting = new ArrayList<>();
		final List<String> multilingualIdList = new ArrayList<>();
		//SWSM -78 added by rukshana on oct-03-2025 to check the validation in sample location add action
		SampleRequesting objDelSampleRequesting = checkValidationForSchedulingCompletion(objSampleRequesting.getNsamplerequestingcode(),objUserInfo);
		
		if(objDelSampleRequesting != null) {
			
		String updateQueryString = "UPDATE samplerequestinglocation SET dmodifieddate='"
				+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', nstatus = "
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " WHERE nsamplerequestingcode="
				+ objSampleRequesting.getNsamplerequestingcode() + " and nsamplerequestinglocationcode = "
				+ objSampleRequesting.getNsamplerequestinglocationcode() + ";";
		jdbcTemplate.execute(updateQueryString);
		objSampleRequesting.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
		deletedSampleRequesting.add(objSampleRequestingLocation);
		multilingualIdList.add("IDS_DELETESAMPLEREQUESTINGLOCATION");
		auditUtilityFunction.fnInsertAuditAction(deletedSampleRequesting, 1, null, multilingualIdList, objUserInfo);
		return getAllSampleRequestingLocation(objSampleRequestingLocation.getNsamplerequestingcode(), objUserInfo);
		
		}else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTPLANNEDRECORDTODELETE",
							objUserInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> scheduledSampleRequesting(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		Map<String, Object> sNodeServerStart = null;
		SampleRequesting objSampleRequesting = objMapper.convertValue(inputMap.get("samplerequesting"),
				SampleRequesting.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		SampleRequesting sampleRequestingstatus = getSampleRequestingStatus(objSampleRequesting, userInfo);
		
		//Added by SWSM-117 Bug fixing rukshana to check samplerequesting location is present or not
		
		String sQuery = " select nsamplerequestinglocationcode from samplerequestinglocation where nsamplerequestingcode="+objSampleRequesting.getNsamplerequestingcode()+""
				+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNtranssitecode()+"";
		
		final List<SampleRequestingLocation> lstCheckingSampleLocation = (List<SampleRequestingLocation>) jdbcTemplate
				.query(sQuery, new SampleRequestingLocation());
		
		
		if(lstCheckingSampleLocation.size()>0) {
		
		// Added by Gowtham on 4th Oct jira-id:SWSM-77
		// start
		sNodeServerStart = reportDAOSupport.validationCheckForNodeServer(inputMap, userInfo);

		if (sNodeServerStart.get("rtn").equals("Failed")) {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage("IDS_STARTNODESERVER", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		
		
		final String controlBasedQuery = "select rm.*,rd.sreportformatdetail from reportmaster rm,reportdetails rd "
				+ "where rm.nreportcode=rd.nreportcode and rm.ncontrolcode=" + inputMap.get("ncontrolcode") + " "
				+ "and rm.ntransactionstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rd.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ "and rm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and rd.nsitecode=" + userInfo.getNmastersitecode() + " and rm.nsitecode="
				+ userInfo.getNmastersitecode() + " " + ";";
		
		final ReportMaster reportMaster = (ReportMaster) jdbcUtilityFunction.queryForObject(controlBasedQuery,
				ReportMaster.class, jdbcTemplate);
		
		if (reportMaster == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADDREPORTDESIGNCONFIG", 
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} else {
			
			inputMap.put("reportMaster", reportMaster);

			final String controlBasedQueryString = "select cbr.ntranscode,ts.stransstatus  as stransactionstatus from controlbasedreportvalidation cbr,"
					+ " reportmaster rm, reportdetails rd, transactionstatus ts where cbr.nreportdetailcode=rd.nreportdetailcode"
					+ " and rm.nreportcode = rd.nreportcode and ts.ntranscode = cbr.ntranscode"
					+ " and rm.ntransactionstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and cbr.ncontrolcode= " + inputMap.get("ncontrolcode")
					+ " and cbr.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rm.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rd.ntransactionstatus= " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and rd.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and rd.nsitecode= " + userInfo.getNmastersitecode() + " and cbr.nsitecode = "
					+ userInfo.getNmastersitecode() + " and rm.nsitecode= " + userInfo.getNmastersitecode()
					+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

			final List<ControlbasedReportvalidation> controlbasedreportvalidation = (List<ControlbasedReportvalidation>) jdbcTemplate
					.query(controlBasedQueryString, new ControlbasedReportvalidation());

			boolean isValidControl = false;
			if (controlbasedreportvalidation.isEmpty()) {
				isValidControl = true;
			} else {
				isValidControl = controlbasedreportvalidation.stream()
						.anyMatch(item -> item.getNtranscode() == (int) inputMap.get("ntranscode"));
			}
			if (!isValidControl) {
				final String ntransname = controlbasedreportvalidation.stream()
						.map(objreport -> String.valueOf(objreport.getStransactionstatus()))
						.collect(Collectors.joining(","));

				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage("IDS_SELECT", userInfo.getSlanguagefilename()) + " "
								+ ntransname,
						HttpStatus.EXPECTATION_FAILED);
			}
		}
		// end
		
		// to avoid null pointer exception
		if (sampleRequestingstatus != null && sampleRequestingstatus
				.getNtransactionstatus() == Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus()) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYSCHEDULED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}else {

			final SampleRequesting sampleRequesting = getActiveSampleRequestingById(
					objSampleRequesting.getNsamplerequestingcode(), userInfo);
			if (sampleRequesting == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

//				final SampleRequesting existingComplaint = getSampleRequestingByName(objSampleRequesting, userInfo);
				final List<String> multilingualIdList = new ArrayList<>();
				final List<Object> saveSampleRequestingList = new ArrayList<>();
//				if (existingComplaint == null) {
				String lockHistory = "lock table locksamplerequestinghistory "
						+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				jdbcTemplate.execute(lockHistory);
				String historySeqQuery = "select nsequenceno from seqnosamplescheduling where stablename ='samplerequestinghistory'"
						+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int historySeqNo = jdbcTemplate.queryForObject(historySeqQuery, Integer.class) + 1;
				
			
				String insertHistoryQuery = "INSERT INTO samplerequestinghistory (nsamplerequestinghistorycode, nsamplerequestingcode, "
						  + "ntransactionstatus, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode,sremarks, "
						  + "dcollectiondate,ntzcollectiondate,noffsetdcollectiondate, dtransactiondate, ntztransactiondate, "
						  + "noffsetdtransactiondate,dmodifieddate, nsitecode, nstatus) "
						  + "VALUES (" + historySeqNo + ", " + objSampleRequesting.getNsamplerequestingcode() + ", "+ Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus() + ", "
						  + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						  + "N'"+ stringUtilityFunction.replaceQuote(objSampleRequesting.getSremarks()) + "','"+objSampleRequesting.getDcollectiondate() + "', "
						  +" "+ userInfo.getNtimezonecode() + ","+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						  +"'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', "+ userInfo.getNtimezonecode() + ","	+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						  + "'"+ dateUtilityFunction.getCurrentDateTime(userInfo)	+ "' ," + userInfo.getNtranssitecode() + ", "
						  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertHistoryQuery);

				String lockHistorys = "lock table locksampleschedulinghistory "
						+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				jdbcTemplate.execute(lockHistorys);
				String historySeqQuerys = "select nsequenceno from seqnosamplescheduling where stablename ='sampleschedulinghistory'"
						+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int historySeqno = jdbcTemplate.queryForObject(historySeqQuerys, Integer.class) + 1;

				//modified the insert query by sonia on 02nd oct 2025 for jira id:SWSM-78
				String insertHistoryquery = "INSERT INTO sampleschedulinghistory ("
						+ "nsampleschedulinghistorycode, nsampleschedulingcode, ntransactionstatus, "
						+ "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, "
						+ "dtransactiondate, ntztransactiondate, noffsetdtransactiondate,sremarks,dmodifieddate, nsitecode, nstatus) "
						+ "VALUES (" + historySeqno + ", " + objSampleRequesting.getNsampleschedulingcode() + ", "
						+ Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus() + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", '"
						+ objSampleRequesting.getDcollectiondate()+ "', " + userInfo.getNtimezonecode() + ","
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ "N'"+ stringUtilityFunction.replaceQuote(objSampleRequesting.getSremarks()) + "'  ,'"+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ userInfo.getNmastersitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertHistoryquery);
				jdbcTemplate.execute("update seqnosamplescheduling set nsequenceno = " + historySeqno
						+ " where stablename='sampleschedulinghistory'");

				jdbcTemplate.execute("update seqnosamplescheduling set nsequenceno = " + historySeqNo
						+ " where stablename='samplerequestinghistory'");

//					String completeQuery ="update sampleschedulinghistory SET nstatus="+ Enumeration.TransactionStatus.NO.gettransactionstatus()
//					+" where nsampleschedulingcode="+ objSampleRequesting.getNsampleschedulingcode()  +" and ntransactionstatus="+Enumeration.TransactionStatus.PLANNED.gettransactionstatus() ;
//					jdbcTemplate.execute(completeQuery);
//					
//				String completeQuery = "update samplescheduling SET nstatus="
//						+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nsampleschedulingcode="
//						+ objSampleRequesting.getNsampleschedulingcode();
//				jdbcTemplate.execute(completeQuery);

				objSampleRequesting.setStransactiondate(objSampleRequesting.getDtransactiondate() == null ? null
						: dateUtilityFunction.instantDateToStringWithFormat(objSampleRequesting.getDtransactiondate(),
								userInfo.getSsitedatetime()));
				objSampleRequesting.setNsamplerequestingcode(historySeqNo);
				saveSampleRequestingList.add(objSampleRequesting);
				multilingualIdList.add("IDS_SCHEDULEDSAMPLEREQUESTING");
				auditUtilityFunction.fnInsertAuditAction(saveSampleRequestingList, 1, null, multilingualIdList,
						userInfo);
				return getSampleRequestingDatas(inputMap, userInfo);
//				} else {
//					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
//							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
//							HttpStatus.CONFLICT);
//				}
			}
		  }
		}else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ADDLOCATIONSAMPLEREQUESTING", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> completedSampleRequesting(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		SampleRequesting objSampleRequesting = objMapper.convertValue(inputMap.get("samplerequesting"),
				SampleRequesting.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		SampleRequesting sampleRequestingstatus = getSampleRequestingStatus(objSampleRequesting, userInfo);

		if (sampleRequestingstatus != null && sampleRequestingstatus
				.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYCOMPLETED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

		else {

			final SampleRequesting sampleRequesting = getActiveSampleRequestingById(
					objSampleRequesting.getNsamplerequestingcode(), userInfo);
			if (sampleRequesting == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {

//				final SampleRequesting existingComplaint = getSampleRequestingByName(objSampleRequesting, userInfo);
				final List<String> multilingualIdList = new ArrayList<>();
				final List<Object> saveSampleRequestingList = new ArrayList<>();
//				if (existingComplaint == null) {
				String lockHistory = "lock table locksamplerequestinghistory "
						+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				jdbcTemplate.execute(lockHistory);
				String historySeqQuery = "select nsequenceno from seqnosamplescheduling where stablename ='samplerequestinghistory'"
						+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int historySeqNo = jdbcTemplate.queryForObject(historySeqQuery, Integer.class) + 1;
				//modified the insert query by sonia on 02nd oct 2025 for jira id:SWSM-78
				String insertHistoryQuery = "INSERT INTO samplerequestinghistory (nsamplerequestinghistorycode, nsamplerequestingcode, ntransactionstatus, "
						  + "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, "
						  + "dcollectiondate,ntzcollectiondate,noffsetdcollectiondate, "
						  + "dtransactiondate, ntztransactiondate, noffsetdtransactiondate,sremarks,"
						  + "dmodifieddate, nsitecode, nstatus) "
						  + "VALUES (" + historySeqNo + ", " + objSampleRequesting.getNsamplerequestingcode() + ", " + Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ", "
						  + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						  + "'"+ objSampleRequesting.getDcollectiondate() + "', " + userInfo.getNtimezonecode() + ","	+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						  + "'"+dateUtilityFunction.getCurrentDateTime(userInfo)+"', "+ userInfo.getNtimezonecode() + ","	+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
						  + "N'"+ stringUtilityFunction.replaceQuote(objSampleRequesting.getSremarks()) + "','" + dateUtilityFunction.getCurrentDateTime(userInfo)+ "' ," + userInfo.getNtranssitecode() + ", "
						  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				
				jdbcTemplate.execute(insertHistoryQuery);

				String lockHistorys = "lock table locksampleschedulinghistory "
						+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				jdbcTemplate.execute(lockHistorys);
				String historySeqQuerys = "select nsequenceno from seqnosamplescheduling where stablename ='sampleschedulinghistory'"
						+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int historySeqno = jdbcTemplate.queryForObject(historySeqQuerys, Integer.class) + 1;
				//modified the insert query by sonia on 02nd oct 2025 for jira id:SWSM-78
				String insertHistoryquery = "INSERT INTO sampleschedulinghistory ("
						+ "nsampleschedulinghistorycode, nsampleschedulingcode, ntransactionstatus, "
						+ "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, "
						+ "dtransactiondate, ntztransactiondate, noffsetdtransactiondate,sremarks,dmodifieddate, nsitecode, nstatus) "
						+ "VALUES (" + historySeqno + ", " + objSampleRequesting.getNsampleschedulingcode() + ", "
						+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", '"
						+ objSampleRequesting.getDcollectiondate()+ "', " + userInfo.getNtimezonecode() + ","
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
						+ "N'"+stringUtilityFunction.replaceQuote(objSampleRequesting.getSremarks()) + "'  ,'" +dateUtilityFunction.getCurrentDateTime(userInfo)+"',"+ userInfo.getNmastersitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertHistoryquery);
				
				jdbcTemplate.execute("update seqnosamplescheduling set nsequenceno = " + historySeqno
						+ " where stablename='sampleschedulinghistory'");

				jdbcTemplate.execute("update seqnosamplescheduling set nsequenceno = " + historySeqNo
						+ " where stablename='samplerequestinghistory'");

				objSampleRequesting.setStransactiondate(objSampleRequesting.getDtransactiondate() == null ? null
						: dateUtilityFunction.instantDateToStringWithFormat(objSampleRequesting.getDtransactiondate(),
								userInfo.getSsitedatetime()));
				objSampleRequesting.setNsamplerequestingcode(historySeqNo);
				saveSampleRequestingList.add(objSampleRequesting);
				multilingualIdList.add("IDS_COMPLETEDSAMPLEREQUESTING");
				auditUtilityFunction.fnInsertAuditAction(saveSampleRequestingList, 1, null, multilingualIdList,
						userInfo);
				return getSampleRequestingDatas(inputMap, userInfo);
//				} else {
//					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
//							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
//							HttpStatus.CONFLICT);
//				}
			}
		}
	}

	private SampleRequesting getSampleRequestingStatus(final SampleRequesting objSampleRequesting,
			final UserInfo userInfo) throws Exception {
		final String strQuery = "select ch.ntransactionstatus from 	samplerequestinghistory ch where ch.nsamplerequestinghistorycode= ANY (Select Max "
				+ " (ch2.nsamplerequestinghistorycode) FROM  samplerequestinghistory ch2 "
				+ " JOIN samplerequesting c2 ON c2.nsamplerequestingcode = ch2.nsamplerequestingcode "
				+ " WHERE ch2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and c2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ch2.nsitecode = " + userInfo.getNtranssitecode() + " and c2.nsitecode= "
				+ userInfo.getNtranssitecode() + " Group By c2.nsamplerequestingcode) and ch.nsamplerequestingcode = "
				+ objSampleRequesting.getNsamplerequestingcode() + " and ch.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ch.nsitecode = "
				+ userInfo.getNtranssitecode();
		return (SampleRequesting) jdbcUtilityFunction.queryForObject(strQuery, SampleRequesting.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> plannedSampleRequesting(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final int nsamplerequestingcode = (int) inputMap.get("primarykey");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		SampleRequesting objSampleRequesting = objMapper.convertValue(inputMap.get("samplerequesting"),
				SampleRequesting.class);

		SampleRequesting sampleRequestingstatus = getSampleRequestingStatus(nsamplerequestingcode, userInfo);

		if (sampleRequestingstatus != null && sampleRequestingstatus
				.getNtransactionstatus() == Enumeration.TransactionStatus.PLANNED.gettransactionstatus()) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYPLANNED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			final SampleRequesting sampleRequesting = getActiveSampleRequestingById(nsamplerequestingcode, userInfo);
			if (sampleRequesting == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final List<String> multilingualIdList = new ArrayList<>();
				final List<Object> saveSampleRequestingList = new ArrayList<>();
				String lockHistory = "lock table locksamplerequestinghistory "
						+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				jdbcTemplate.execute(lockHistory);
				String historySeqQuery = "select nsequenceno from seqnosamplescheduling "
						+ " where stablename ='samplerequestinghistory'" + "  and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int historySeqNo = jdbcTemplate.queryForObject(historySeqQuery, Integer.class) + 1;
				//modified the insert query by sonia on 02nd oct 2025 for jira id:SWSM-78
				String insertHistoryQuery = "INSERT INTO samplerequestinghistory ("
						+ "nsamplerequestinghistorycode, nsamplerequestingcode, ntransactionstatus, "
						+ "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, "
						+ "dcollectiondate,ntzcollectiondate,noffsetdcollectiondate, "
						+ "dtransactiondate, ntztransactiondate, noffsetdtransactiondate,sremarks,dmodifieddate, nsitecode, nstatus) "
						+ "VALUES (" + historySeqNo + ", " + objSampleRequesting.getNsamplerequestingcode() + ", "+ Enumeration.TransactionStatus.PLANNED.gettransactionstatus() + ","
						+ " " + userInfo.getNusercode()+ ", " + userInfo.getNuserrole() + ", "
						+ "" + userInfo.getNdeputyusercode() + ", "+ " "+userInfo.getNdeputyuserrole() + ", "
						+ "'"+ dateUtilityFunction.getCurrentDateTime(userInfo)+ "', " + userInfo.getNtimezonecode() + ","+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
						+ "'" + dateUtilityFunction.getCurrentDateTime(userInfo)+ "', " + userInfo.getNtimezonecode() + ","	+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
						+ "N'"+ stringUtilityFunction.replaceQuote(objSampleRequesting.getSremarks()) + "'  ,'" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' ," + userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

				jdbcTemplate.execute(insertHistoryQuery);

				jdbcTemplate.execute("update seqnosamplescheduling set nsequenceno = " + historySeqNo
						+ " where stablename='samplerequestinghistory' and nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

				String historySeqQuerys = "select nsequenceno from seqnosamplescheduling where stablename ='sampleschedulinghistory'"
						+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int historySeqno = jdbcTemplate.queryForObject(historySeqQuerys, Integer.class) + 1;
				//modified the insert query by sonia on 02nd oct 2025 for jira id:SWSM-78
				String insertHistoryquery = "INSERT INTO sampleschedulinghistory (nsampleschedulinghistorycode, nsampleschedulingcode, "
						+ "ntransactionstatus,nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, "
						+ "dtransactiondate, ntztransactiondate, noffsetdtransactiondate,sremarks, dmodifieddate,nsitecode, nstatus) "
						+ "VALUES (" + historySeqno + ", " + objSampleRequesting.getNsampleschedulingcode() + ", "
						+ Enumeration.TransactionStatus.PLANNED.gettransactionstatus() + ", " + userInfo.getNusercode()
						+ ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", "
						+ userInfo.getNdeputyuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "', " + userInfo.getNtimezonecode() + ","	+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ "N'" + stringUtilityFunction.replaceQuote(objSampleRequesting.getSremarks()) + "'  ,'"+ dateUtilityFunction.getCurrentDateTime(userInfo)+"'," + userInfo.getNmastersitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertHistoryquery);
				jdbcTemplate.execute("update seqnosamplescheduling set nsequenceno = " + historySeqno
						+ " where stablename='sampleschedulinghistory'");

				//commented by sonia on 02nd oct 2025 for jira id:SWSM-78
//				String completeQuery = "update samplescheduling SET nstatus="
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where nsampleschedulingcode="
//						+ objSampleRequesting.getNsampleschedulingcode();
//				jdbcTemplate.execute(completeQuery);

				SampleRequesting sampleRequestingsStatus = getSampleRequestingStatus(nsamplerequestingcode, userInfo);

				saveSampleRequestingList.add(sampleRequestingsStatus);
				multilingualIdList.add("IDS_PLANNEDSAMPLEREQUESTING");
				//multilingualIdList.add("IDS_ADDSAMPLEREQUESTING");

				auditUtilityFunction.fnInsertAuditAction(saveSampleRequestingList, 1, null, multilingualIdList,
						userInfo);
				return getSampleRequestingDatas(inputMap, userInfo);
			}

		}
	}

	private SampleRequesting getSampleRequestingStatus(final int nsamplerequestingcode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select sh.ntransactionstatus,s.sfromyear from samplerequestinghistory sh JOIN samplerequesting s ON s.nsamplerequestingcode = sh.nsamplerequestingcode"
				+ " where sh.nsamplerequestinghistorycode= ANY (Select Max "
				+ " (sh2.nsamplerequestinghistorycode) FROM  samplerequestinghistory sh2 "
				+ " JOIN samplerequesting s2 ON s2.nsamplerequestingcode = sh2.nsamplerequestingcode "
				+ " WHERE sh2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sh2.nsitecode = " + userInfo.getNtranssitecode() + " and s2.nsitecode= "
				+ userInfo.getNtranssitecode() + " Group By s2.nsamplerequestingcode) and sh.nsamplerequestingcode = "
				+ nsamplerequestingcode + " and sh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sh.nsitecode = "
				+ userInfo.getNtranssitecode();
		return (SampleRequesting) jdbcUtilityFunction.queryForObject(strQuery, SampleRequesting.class, jdbcTemplate);

	}

	@Override
	public ResponseEntity<Object> createSampleRequestingLocation(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		SampleRequestingLocation objSampleRequestingLocation = objMapper
				.convertValue(inputMap.get("samplerequestinglocation"), SampleRequestingLocation.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		final SampleRequesting sampleRequesting = getActiveSampleRequestingById(
				objSampleRequestingLocation.getNsamplerequestingcode(), userInfo);
		if (sampleRequesting == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final SampleRequestingLocation sampleRequestingLocation = getSampleRequestingLocationById(
				objSampleRequestingLocation, userInfo);

		if (sampleRequestingLocation != null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		String locksamplerequestinglocation = "lock table locksamplerequestinglocation"
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(locksamplerequestinglocation);

		final List<String> multilingualIdList = new ArrayList<>();
		final List<Object> saveSampleSchedulingList = new ArrayList<>();
		String sequenceNoQuery = "select nsequenceno from seqnosamplescheduling where stablename ='samplerequestinglocation'"
				+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class) + 1;

		String insertQuery = "INSERT INTO samplerequestinglocation ("
				+ "nsamplerequestinglocationcode,nsamplerequestingcode,ncentralsitecode,nregioncode, ndistrictcode,ncitycode, nvillagecode,nsamplelocationcode,dmodifieddate,nsitecode,nstatus) "
				+ "values(" + nsequenceNo + "," + objSampleRequestingLocation.getNsamplerequestingcode() + ","+ objSampleRequestingLocation.getNcentralsitecode() + "," 
				+ objSampleRequestingLocation.getNregioncode() + "," + objSampleRequestingLocation.getNdistrictcode()
				+ "," + objSampleRequestingLocation.getNcitycode() + "," + objSampleRequestingLocation.getNvillagecode()
				+ " ," + objSampleRequestingLocation.getNsamplelocationcode() + ",'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() + ","
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

		jdbcTemplate.execute(insertQuery);

		jdbcTemplate.execute("update seqnosamplescheduling set nsequenceno = " + nsequenceNo
				+ " where stablename='samplerequestinglocation'");

		final SampleRequesting sampleRequestings = getAuditSampleRequestingLocationById(nsequenceNo, userInfo);

		objSampleRequestingLocation.setNsamplerequestinglocationcode(nsequenceNo);
		saveSampleSchedulingList.add(sampleRequestings);

		multilingualIdList.add("IDS_ADDSAMPLEREQUESTINGLOCATION");
		auditUtilityFunction.fnInsertAuditAction(saveSampleSchedulingList, 1, null, multilingualIdList, userInfo);
		return getAllSampleRequestingLocation(objSampleRequestingLocation.getNsamplerequestingcode(), userInfo);
	}

	public ResponseEntity<Object> SampleRequestingData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<>();

		int fromYear;
		int toYear;

		if (inputMap.get("fromYear") != null && inputMap.get("toYear") != null) {
			fromYear = (int) inputMap.get("fromYear");
			toYear = (int) inputMap.get("toYear");
		} else {
			String currentUIDate = (String) inputMap.get("currentdate");
			if (currentUIDate != null && !currentUIDate.trim().isEmpty()) {
				int currentYear = Year.now().getValue();
				fromYear = currentYear;
				toYear = currentYear;
			} else {
				int currentYear = Year.now().getValue();
				fromYear = currentYear;
				toYear = currentYear;
			}
		}
		outputMap.put("fromYear", fromYear);
		outputMap.put("toYear", toYear);
		int ntransactionstatus = 0;
		if (inputMap.containsKey("ntranscode")) {
			ntransactionstatus = (int) inputMap.get("ntranscode");
		}
		String filterStatusQuery = "";
		if (ntransactionstatus > 0) {
			filterStatusQuery = " AND sh.ntransactionstatus = " + ntransactionstatus + " ";
		}

		// Added samplescheduling for speriod by Gowtham R on Oct 2 jira id: SWSM-78
		final String strQuery = "SELECT s.nsamplerequestingcode, s.nsampleschedulingcode, s.sfromyear, ss.speriod, s.sassignedto, " 
				+ " s.scontactnumber, s.semail, to_char(s.dcollectiondate, '"+userInfo.getSsitedate()+"') as scollectiondate, "

				// Scheduled date (latest 41)
				+ " (SELECT to_char(sh1.dcollectiondate, '"+userInfo.getSsitedate()+"') " // Changed dtransactiondate to dcollectiondate by Gowtham R on Oct 2 jira id: SWSM-78
				+ " FROM samplerequestinghistory sh1 "
				+ " WHERE sh1.nsamplerequestingcode = s.nsamplerequestingcode "
				+ " AND sh1.ntransactionstatus = "
				+ Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus() + " AND sh1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND sh1.nsitecode = s.nsitecode "
				+ " ORDER BY sh1.nsamplerequestinghistorycode DESC LIMIT 1) as sscheduleddate, "

				// Completed date (latest 25)
				+ " (SELECT to_char(sh2.dcollectiondate, '"+userInfo.getSsitedate()+"') " // Changed dtransactiondate to dcollectiondate by Gowtham R on Oct 2 jira id: SWSM-78
				+ " FROM samplerequestinghistory sh2 "
				+ " WHERE sh2.nsamplerequestingcode = s.nsamplerequestingcode "
				+ " AND sh2.ntransactionstatus = "
				+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + " AND sh2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND sh2.nsitecode = s.nsitecode "
				+ " ORDER BY sh2.nsamplerequestinghistorycode DESC LIMIT 1) as scompleteddate, "

				+ " s.nsitecode, s.nstatus, sh.ntransactionstatus,"
				+ " COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() 
				+ "', ts1.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus,"
				+ " f.ncolorcode, cl.scolorhexcode FROM samplerequesting s"
				+ " JOIN samplerequestinghistory sh ON sh.nsamplerequestingcode = s.nsamplerequestingcode "
				+ " JOIN transactionstatus ts1 ON sh.ntransactionstatus = ts1.ntranscode "
				+ " JOIN formwisestatuscolor f ON sh.ntransactionstatus = f.ntranscode "
				+ " JOIN colormaster cl ON cl.ncolorcode = f.ncolorcode "
				+ " JOIN samplescheduling ss ON ss.nsampleschedulingcode = s.nsampleschedulingcode"

				+ " WHERE sh.nsamplerequestinghistorycode = ANY (SELECT MAX(sh2.nsamplerequestinghistorycode) "
				+ " FROM samplerequestinghistory sh2 "
				+ " JOIN samplerequesting s2 ON s2.nsamplerequestingcode = sh2.nsamplerequestingcode "
				+ " WHERE s2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND sh2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND s2.nsitecode = " + userInfo.getNtranssitecode() + " AND sh2.nsitecode = "
				+ userInfo.getNtranssitecode() + " GROUP BY s2.nsamplerequestingcode) "

				+ "AND s.sfromyear::int BETWEEN " + fromYear + " AND " + toYear + " AND s.nsamplerequestingcode > 0 "
				+ "AND s.nsitecode = " + userInfo.getNtranssitecode() + " AND s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND sh.nsitecode = "
				+ userInfo.getNtranssitecode() + " AND sh.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ts1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND f.nformcode = "
				+ userInfo.getNformcode() + " " + filterStatusQuery + " ORDER BY s.nsamplerequestingcode DESC";

		LOGGER.info("Get SampleRequesting Query: " + strQuery);
		final List<SampleRequesting> lstSampleRequesting = (List<SampleRequesting>) jdbcTemplate.query(strQuery,
				new SampleRequesting());
		if (!lstSampleRequesting.isEmpty()) {
			outputMap.put("sampleRequestingRecord", lstSampleRequesting);

		} else {
			outputMap.put("sampleRequestingRecord", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
	//Added by sonia on 4th oct 2025 for jira id:SWSM-77
	/**
	 * This method is used to Sent the Report By Mail.	 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and ntranssitecode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{ntranssitecode": 1},
	 *                 "ncontrolcode":1545, "nsamplerequestingcode":1,
	 *                 "ntransactionstatus":41 }
	 * @return response entity object holding response status as success
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> sendReportByMail(final Map<String, Object> inputMap, final UserInfo userInfo)throws Exception {
		final Map<String, Object> responseMap = new HashMap<>();
		final Map<String, Object> mailMap = new HashMap<>();
		
		mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));
		mailMap.put("nsitecode", (int) userInfo.getNtranssitecode());
		mailMap.put("nsamplerequestingcode",inputMap.get("nsamplerequestingcode"));
	
		final String getReportQuery= "select rm.*,rd.sreportformatdetail  "
								   + "from reportmaster rm  "
								   + "join reportdetails rd on rd.nreportcode=rm.nreportcode  "
								   + "join samplerequestingreporthistory srh on srh.nreportdetailcode=rd.nreportdetailcode "
								   + "and srh.nreporttypecode=rm.nreporttypecode "
								   + "where srh.nsamplerequestingreporthistorycode =any(select max(nsamplerequestingreporthistorycode) "
								   + "from samplerequestingreporthistory where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								   + "and nsitecode ="+userInfo.getNtranssitecode()+" group by nsamplerequestingcode) "
								   + "and rm.ncontrolcode="+inputMap.get("schedulingControlCode")+" "
								   + "and srh.nsamplerequestingcode="+inputMap.get("nsamplerequestingcode")+" "
								   + "and rm.ntransactionstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								   + "and rd.ntransactionstatus="+Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" "
								   + "and rm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								   + "and rd.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								   + "and srh.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
								   + "and rd.nsitecode="+userInfo.getNmastersitecode()+" and rm.nsitecode="+userInfo.getNmastersitecode()+" "
								   + "and srh.nsitecode="+userInfo.getNtranssitecode()+" ";
		
		final ReportMaster reportMaster = (ReportMaster) jdbcUtilityFunction.queryForObject(getReportQuery,ReportMaster.class, jdbcTemplate);
		if (reportMaster == null) {
			responseMap.put("rtn", "IDS_ADDREPORTDESIGNCONFIG");
		}else {
			final String getUsersForEmail = " select squery from emailuserquery where nformcode ="+Enumeration.QualisForms.SAMPLEREQUESTINGPLAN.getqualisforms()+""
					  					  + " and sdisplayname='Sample Requesting' and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
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
	
	//SWSM -78 added by rukshana on oct-03-2025 to check the validation in sample location add action
	private SampleRequesting checkValidationForSchedulingCompletion(int nsamplerequestingcode,
			final UserInfo userInfo) throws Exception {
		final String strQuery = " select * from samplerequesting sr,samplerequestinghistory srh where "
				+ " sr.nsamplerequestingcode= "+nsamplerequestingcode+" and srh.nsamplerequestingcode=sr.nsamplerequestingcode "
				+ " and srh.nsamplerequestinghistorycode =any(select max(nsamplerequestinghistorycode) from samplerequestinghistory srh1 where "
				+ " srh1.nsamplerequestingcode= "+nsamplerequestingcode+" "
				//+ " "+Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus()+") "
				+ " and srh1.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
				+ " and srh1.nsitecode= "+userInfo.getNtranssitecode()+") "
				+ " and srh.ntransactionstatus = "+Enumeration.TransactionStatus.PLANNED.gettransactionstatus()+" and "
				+ " srh.nsitecode= "+userInfo.getNtranssitecode()+""
				+ " and sr.nsitecode="+userInfo.getNtranssitecode()+" and srh.nstatus="
				+ ""+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and srh.nsitecode="+userInfo.getNtranssitecode()+"";
				
		return (SampleRequesting) jdbcUtilityFunction.queryForObject(strQuery, SampleRequesting.class, jdbcTemplate);
	}


	/**
	 * Added by Gowtham on Oct 4 to generate SampleSchedulingReport jira-id:SWSM-77
	 * 
	 * This method will access the DAO layer that is used to generate SampleSchedulingReport
	 * and insert the file location in samplerequestingreporthistory table.
	 * 
	 * @param inputMap   			[Map<String, Object>] Map holding data to generate
	 * 								SampleSchedulingReport and insert 
	 * 								into samplerequestingreporthistory table
	 * 
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	public void generateSampleScheduledReport(Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final SampleRequesting objSampleRequesting = objMapper.convertValue(inputMap.get("samplerequesting"),SampleRequesting.class);
		ReportMaster reportMaster = (ReportMaster) inputMap.get("reportMaster");
		Map<String, Object> selectedRecord = (Map<String, Object>) inputMap.get("selectedRecord");
		
		String dCollectionDate = jdbcTemplate.queryForObject("SELECT to_char("
				+ " to_timestamp('"+objSampleRequesting.getDcollectiondate()+"','YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"'),'"+userInfo.getSsitedate()
				+ "') AS dcollectiondate", String.class);
		
		selectedRecord.put("dcollectiondate", dCollectionDate);
		
		inputMap.put("selectedRecord", selectedRecord);
		
		inputMap.put("ntranscode", Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus());
		inputMap.put("nreporttypecode", Enumeration.ReportType.CONTROLBASED.getReporttype());
		Map<String, Object> reportMap = controlBasedReportDAO.controlBasedReportGeneration(reportMaster, inputMap, userInfo);
			
		if (reportMap.containsKey("nreporttypecode") && reportMap.containsKey("nreportdetailcode") && reportMap.containsKey("outputFileName")) {
			final String reportHistorySeqQuerys = "select nsequenceno from seqnosamplescheduling"
					+ " where stablename ='samplerequestingreporthistory' and nstatus = " 
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final int reportHistorySeqno = jdbcTemplate.queryForObject(reportHistorySeqQuerys, Integer.class) + 1;
			
			final String insertReportHistoryquery = "INSERT INTO samplerequestingreporthistory(nsamplerequestingreporthistorycode,"
					+ " nsamplerequestingcode, nreporttypecode, nreportdetailcode, nusercode, nuserrolecode, ssystemfilename,"
					+ " dgenerateddate, ntzgenerateddate, noffsetdgenerateddate, dmodifieddate, nsitecode, nstatus)"
					+ " VALUES (" + reportHistorySeqno + ", " + objSampleRequesting.getNsamplerequestingcode() 
					+ ", " + reportMap.get("nreporttypecode") + ", " + reportMap.get("nreportdetailcode") + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '" + reportMap.get("outputFileName") 
					+ "', '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() 
					+ "," + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtranssitecode() 
					+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertReportHistoryquery);
			
			jdbcTemplate.execute("update seqnosamplescheduling set nsequenceno = " + reportHistorySeqno
					+ " where stablename='samplerequestingreporthistory' and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());	
		}	
	}
	
}
