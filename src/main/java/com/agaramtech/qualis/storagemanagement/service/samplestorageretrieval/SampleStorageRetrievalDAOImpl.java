package com.agaramtech.qualis.storagemanagement.service.samplestorageretrieval;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.basemaster.model.BulkBarcodeConfigDetails;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.SampleStorageRetrieval;
import com.agaramtech.qualis.storagemanagement.model.SiteSampleExpiryMapping;
import com.agaramtech.qualis.storagemanagement.service.samplestoragetransaction.SampleStorageTransactionDAO;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the samplestorageretrieval Service methods
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
@AllArgsConstructor
@Repository
public class SampleStorageRetrievalDAOImpl implements SampleStorageRetrievalDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleStorageRetrievalDAOImpl.class);

	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final SampleStorageTransactionDAO sampleStorageTransactionDAO;

	/**
	 * This method is used to retrieve list of available unit(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1,ntransitecode:1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getsamplestorageretrieval(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();
		String dfromdate = "";
		String dtodate = "";
		String strQuery = " select ssl.*,ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'  as stransdisplaystatus from samplestoragelocation ssl,transactionstatus ts where ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ssl.nsamplestoragelocationcode in (select nsamplestoragelocationcode from"
				+ " samplestorageversion where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and napprovalstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " and nsitecode=" + userInfo.getNmastersitecode() + ") and ssl.nsitecode="
				+ userInfo.getNtranssitecode() + " and ts.ntranscode=ssl.nmappingtranscode"
				+ " order by ssl.nsamplestoragelocationcode desc ";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("sampleStorageLocation", list);

		strQuery = "select * from product where nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode=" + userInfo.getNmastersitecode();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("sampleType", list);

		strQuery = "select * from containertype where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("containerType", list);

		strQuery = "select * from containerstructure where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("containerStructure", list);
		
		strQuery = "select * from visitnumber where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("VisitNumber", list);

		strQuery = "select * from projecttype where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("ProjectType", list);
		
		//Added by jana for All status Add in project filter
		final var nNeedInstrumentFlow = getInstrumentBased();

		if (nNeedInstrumentFlow == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			
			if (list.size() > 1) {
				final Map<String, Object> allItem = new HashMap<>();

				allItem.put("nprojecttypecode", Enumeration.TransactionStatus.ALL.gettransactionstatus());
				allItem.put("sprojecttypename",
						commonFunction.getMultilingualMessage("IDS_ALL", userInfo.getSlanguagefilename()));

				list.add(allItem);
			}
			
		}

//		strQuery = "select p.sprojecttypename,p.nprojecttypecode from projecttype p where p.nstatus=  "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nsitecode= "
//				+ userInfo.getNmastersitecode();
//		list = jdbcTemplate.queryForList(strQuery);
		
		var nprojecttypecode=Enumeration.TransactionStatus.NA.gettransactionstatus();
		if (!list.isEmpty()) {
			outputMap.put("selectedProjectType", list.get(0));
			outputMap.put("projectbarcodeconfig", list);
			outputMap.put("nprojecttypecode", list.get(0).get("nprojecttypecode"));
			
			nprojecttypecode=(int) list.get(0).get("nprojecttypecode");
			
		}else {
			outputMap.put("selectedProjectType", null);
			outputMap.put("projectbarcodeconfig", null);
			outputMap.put("nprojecttypecode", Enumeration.TransactionStatus.NA.gettransactionstatus());
		}


		final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
				(String) inputMap.get("currentdate"), "datetime", "FromDate");

		dfromdate = (String) mapObject.get("FromDate");
		dtodate = (String) mapObject.get("ToDate");
		inputMap.put("fromDate", dfromdate);
		inputMap.put("toDate", dtodate);
		outputMap.put("fromDate", mapObject.get("FromDateWOUTC"));
		outputMap.put("toDate", mapObject.get("ToDateWOUTC"));

		inputMap.put("label", "samplestorageretrieval");
		inputMap.put("valuemember", "nsamplestoragetransactioncode");
		inputMap.put("nprojecttypecode", nprojecttypecode);
		
		// ALPD-4774-Vignesh R(31-08-2024)
		inputMap.put("source", "view_sampleretrieval_"
				+ ((int) nprojecttypecode == -1 || (int) nprojecttypecode > 3 ? 0
						: (int) nprojecttypecode));
		
		outputMap.putAll(getDynamicFilterExecuteData(inputMap, userInfo).getBody());
		outputMap.putAll(getProjectbarcodeconfig(outputMap, userInfo).getBody());
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	// added by sujatha ATE_274 for dispose expired samples SWSM-37 12-09-2025
	/**
	 * This method is used to retrieve list of expired samples based on the expiry
	 * days mapped in site retention mapping.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1,ntransitecode:1}}
	 * @return response entity object holding response status and list of all
	 *         expired samples based on the expiry days mapped in site retention
	 *         mapping
	 * @throws Exception exception
	 */
	public ResponseEntity<Object> getSampleStorageTransaction(final Map<String, Object> inputMap) throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
//		String str = "SELECT st.nsamplestoragetransactioncode, st.jsondata ->> 'IDS_SAMPLEID' AS ssampleid,st.nsamplestoragelocationcode"
//		           + " FROM samplestoragetransaction st LEFT JOIN samplestoragelocation sl  "
//		           + " ON  st.nsamplestoragetransactioncode = sl.nsamplestoragelocationcode"
//		           + " where st.nsitecode = " + userInfo.getNtranssitecode()
//		           + " AND st.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//		           + " ORDER BY ssampleid asc";

		String str = " SELECT NOW() AS currentdate, st.dmodifieddate, NOW() - (se.sexpirydays::integer) * INTERVAL '1 day' AS expireddate,"
				+ " st.nsamplestoragetransactioncode, "
//					+ " st.jsondata->> 'AR.No.' as ssampleid,"    // modified by sujatha ATE_274 for the ar.no/sampleid issue 
				+ " st.spositionvalue as ssampleid, " // added by sujatha ATE_274 for the ar.no/sampleid issue
				+ " st.nsamplestoragelocationcode," + " se.sexpirydays, sl.ssamplestoragelocationname "
				+ " FROM samplestoragetransaction st LEFT JOIN samplestoragelocation sl "
				+ " ON st.nsamplestoragelocationcode = sl.nsamplestoragelocationcode"
				+ " JOIN siteexpirymapping se ON se.nsitemastercode = st.nsitecode " + " WHERE st.nprojecttypecode="
				+ inputMap.get("nprojecttypecode") + " and st.nsitecode =" + userInfo.getNtranssitecode()
				+ " and se.nsitemastercode=" + userInfo.getNtranssitecode() + " AND se.nsitecode ="
				+ userInfo.getNmastersitecode() + " AND se.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and st.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and st.dmodifieddate::DATE<=NOW() - (se.sexpirydays::integer) * INTERVAL '1 day' order by ssampleid asc";

		List<Map<String, Object>> list = jdbcTemplate.queryForList(str);
		outputMap.put("samplestoragetransaction", list);
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	// added by sujatha ATE_274 for dispose expired samples SWSM-37 12-09-2025
	/**
	 * This method is used to retrieve the logged in site and the expiry days.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1,ntransitecode:1}}
	 * @return response entity object holding response status and the logged in site
	 *         and the expiry days
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getSiteSampleExpiryMapping(final Map<String, Object> inputMap) throws Exception {
		Map<String, Object> responseMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		String siteQuery = "SELECT s.nsitecode, s.ssitename as ssitename FROM site s WHERE s.nsitecode = "
				+ userInfo.getNtranssitecode() + " AND s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		Site loginSite = (Site) jdbcUtilityFunction.queryForObject(siteQuery, Site.class, jdbcTemplate);

		String str = " select se.nsiteexpirymappingcode, se.sexpirydays, se.nsitemastercode "
				+ " from siteexpirymapping se, site s where se.nsitemastercode=s.nsitecode and "
				+ " se.nsitemastercode=" + userInfo.getNtranssitecode() + " and se.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and se.nsitecode="
				+ userInfo.getNmastersitecode() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		SiteSampleExpiryMapping obj = (SiteSampleExpiryMapping) jdbcUtilityFunction.queryForObject(str,
				SiteSampleExpiryMapping.class, jdbcTemplate);
		responseMap.put("siteSampleExpiryMap", obj);
		responseMap.put("loginSite", loginSite);
		return new ResponseEntity<>(responseMap, HttpStatus.OK);
	}

	// added by sujatha ATE_274 for dispose expired samples SWSM-37 12-09-2025
	/**
	 * This method is used to retrieve list of expired sample based on what the user
	 * enter in the Rentention period in days).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1,ntransitecode:1}}
	 * @return response entity object holding response status and list of all xpired
	 *         sample based on what the user enter in the Rentention period in days
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getExpiredSample(final Map<String, Object> inputMap) throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final String sexpirydays = String.valueOf(inputMap.get("sexpirydays"));

		String str = " SELECT NOW() AS currentdate, st.dmodifieddate, NOW() - (" + sexpirydays
				+ ") * INTERVAL '1 day' AS expireddate," + " st.nsamplestoragetransactioncode,"
//					+ " st.jsondata->> 'AR.No.' as ssampleid, " // modified by sujatha ATE_274 for the ar.no/sampleid issue 
				+ " st.spositionvalue as ssampleid, " // added by sujatha ATE_274 for the ar.no/sampleid issue
				+ " st.nsamplestoragelocationcode,"
				+ " sl.ssamplestoragelocationname FROM samplestoragetransaction st LEFT JOIN samplestoragelocation sl "
				+ " ON st.nsamplestoragelocationcode = sl.nsamplestoragelocationcode" + " WHERE st.nprojecttypecode= "
				+ inputMap.get("nprojecttypecode") + " and st.nsitecode =" + userInfo.getNtranssitecode()
				+ " and st.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and st.dmodifieddate::DATE<=NOW() - (" + sexpirydays
				+ "::integer) * INTERVAL '1 day' order by ssampleid asc";

		List<Map<String, Object>> list = jdbcTemplate.queryForList(str);
		outputMap.put("samplestoragetransaction", list);
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	// added by sujatha ATE_274 for dispose expired samples SWSM-37 12-09-2025
	/**
	 * This method is used to dispose one or more samples simultaneously and to
	 * retrieve list of disposed samples with their details.
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1,ntransitecode:1}}
	 * @return response entity object holding response status and dispose one or
	 *         more samples simultaneously and to retrieve list of disposed samples
	 *         with their details.
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> createDisposeExpiredSample(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> returnObject = new HashMap<>();
		final ObjectMapper mapper = new ObjectMapper();

		final String sQuery = " lock  table locksamplestoragetransaction "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		List<SampleStorageRetrieval> samplestorageretrieval;
		String sexpirydays = (String) inputMap.get("sexpirydays");
		String ssampleid = (String) inputMap.get("ssampleid");
		String sampleid = "'" + ssampleid.replace(",", "','") + "'";
		try {
			final String strcheck = "select nsamplestoragetransactioncode,sposition,nsamplestoragelocationcode,"
					+ "nsamplestoragemappingcode from samplestoragetransaction " + " where spositionvalue In ("
					+ sampleid + ") and nprojecttypecode=" + inputMap.get("nprojecttypecode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode();

			samplestorageretrieval = jdbcTemplate.query(strcheck, new SampleStorageRetrieval());

			if (!samplestorageretrieval.isEmpty()) {

				final String sectSeq = "select nsequenceno from seqnostoragemanagement where stablename='samplestorageretrieval' "
						+ "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final int intseq = jdbcTemplate.queryForObject(sectSeq, Integer.class);

				String str = "";

				str = "INSERT INTO public.samplestorageretrieval( "
						+ " nsamplestorageretrievalcode, nsamplestoragetransactioncode, nsamplestoragelocationcode, "
						+ " nsamplestoragelistcode, nsamplestoragemappingcode, nprojecttypecode, nusercode, nuserrolecode, "
						+ " sposition, spositionvalue, jsondata, ntransactionstatus,ntransferstatuscode, ninstrumentcode, nbioparentsamplecode, sparentsamplecode, ncohortno, nproductcatcode, nproductcode, sqty, slocationcode, ssubjectid, scasetype, "
						+ "ndiagnostictypecode, ncontainertypecode, nstoragetypecode, dtransactiondate, "
						+ " noffsetdtransactiondate, ntransdatetimezonecode, nsitecode, nstatus) " + " SELECT " + intseq
						+ " + RANK() OVER (ORDER BY nsamplestoragetransactioncode, "
						+ " nsamplestoragelocationcode) as nsamplestorageretrievalcode, "
						+ " nsamplestoragetransactioncode, " + " nsamplestoragelocationcode, " + " -1, "
						+ " nsamplestoragemappingcode, " + " nprojecttypecode, " + userInfo.getNusercode() + ", "
						+ userInfo.getNuserrole() + ", " + " sposition, " + " spositionvalue, "
						+ " jsondata || jsonb_build_object('sexpirydays','" + sexpirydays + "'), "
						+ ((boolean) inputMap.get("isRetrieve")
								? Enumeration.TransactionStatus.RETRIEVED.gettransactionstatus()
								: Enumeration.TransactionStatus.DISPOSED.gettransactionstatus())
						+ ", " + "" + Enumeration.TransactionStatus.RETRIEVED.gettransactionstatus() + ","
						+ "ninstrumentcode,nbioparentsamplecode, sparentsamplecode, ncohortno, nproductcatcode, nproductcode, "
						+ "sqty, slocationcode,ssubjectid, scasetype, ndiagnostictypecode, ncontainertypecode, nstoragetypecode, "
						+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNtimezonecode() + ", " + " nsitecode, "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " FROM samplestoragetransaction " + " WHERE nsamplestoragetransactioncode IN ("
						+ inputMap.get("nsamplestoragetransactioncode") + ") " + " AND nsamplestoragelocationcode IN ("
						+ inputMap.get("nsamplestoragelocationcode") + ");";

				str += " delete from samplestoragetransaction where nsamplestoragetransactioncode in ("
						+ inputMap.get("nsamplestoragetransactioncode") + ");";

				str += " UPDATE seqnostoragemanagement SET nsequenceno = ("
						+ " SELECT MAX(nsamplestorageretrievalcode) FROM public.samplestorageretrieval"
						+ ") WHERE stablename = 'samplestorageretrieval';";
				jdbcTemplate.execute(str);

				// ALPDJ21-116--Added by Vignesh(08-10-2025)--storestatus set to false when the
				// retrieval and disposal.
				updateSampleSendToStore(inputMap.get("nsamplestoragetransactioncode").toString(), userInfo);

				String query = "select sr.ntransactionstatus,sr.nsamplestoragelocationcode,sr.spositionvalue, sl.ssamplestoragelocationname, sr.sposition, "
						+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
						+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus "
						+ "from samplestorageretrieval sr, samplestoragelocation sl, transactionstatus ts where "
						+ " sr.nsamplestoragelocationcode=sl.nsamplestoragelocationcode and sr.ntransactionstatus=ts.ntranscode and "
						+ " sr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and sl.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and sr.nsitecode=" + userInfo.getNtranssitecode()
						+ " and sr.nsamplestoragetransactioncode in (" + inputMap.get("nsamplestoragetransactioncode")
						+ ");";
				List<SampleStorageRetrieval> transStatus = jdbcTemplate.query(query, new SampleStorageRetrieval());

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedTestList = new ArrayList<>();

				savedTestList.add(transStatus);
				multilingualIDList.add("IDS_DISPOSESAMPLE");

				auditUtilityFunction.fnInsertListAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);

				// getProjectbarcodeconfig(inputMap, userInfo);
				returnObject.putAll(getProjectbarcodeconfig(inputMap, userInfo).getBody());
				return new ResponseEntity<>(returnObject, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SAMPLENOTEXIST", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} catch (final Exception e) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SAMPLENOTEXIST", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to retrieve list of available unit(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1,ntransitecode:1}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getDynamicFilterExecuteData(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		System.out.println("JVM Bit size: " + System.getProperty("sun.arch.data.model"));
		String tableName = "";
		String getJSONKeysQuery = "";
		final Map<String, Object> returnObject = new HashMap<>();
		final String sourceName = (String) inputMap.get("source");
		String conditionString = inputMap.containsKey("conditionstring") ? (String) inputMap.get("conditionstring")
				: "";
		if (conditionString.isEmpty()) {
			conditionString = inputMap.containsKey("filterquery") ? "and " + (String) inputMap.get("filterquery") : "";
		}
		//Added by jana for All status remove the project
		var projectConditionStr=" and nprojecttypecode=" + inputMap.get("nprojecttypecode")+" ";
		
		final var nProjectTypeCode=(int)inputMap.get("nprojecttypecode");
		
		if(nProjectTypeCode == Enumeration.TransactionStatus.ALL.gettransactionstatus()) {
			projectConditionStr="";
		}

		conditionString = conditionString + projectConditionStr + " and  dtransactiondate between '" + inputMap.get("fromDate") + "'::timestamp" + " and '"
				+ inputMap.get("toDate") + "'::timestamp  ";

		final String scollate = "collate \"default\"";
		if (conditionString.contains("LIKE")) {

			while (conditionString.contains("LIKE")) {
				final String sb = conditionString;
				String sQuery = conditionString;
				final int colanindex = sb.indexOf("LIKE '");
				final String str1 = sQuery.substring(0, colanindex + 6);
				sQuery = sQuery.substring(colanindex + 6);
				final StringBuilder sb3 = new StringBuilder(str1);
				final StringBuilder sb4 = new StringBuilder(sQuery);
				sb3.replace(colanindex, colanindex + 4, "ilike");
				System.out.println(sQuery);
				final int indexofsv = sQuery.indexOf("'");

				sb4.replace(indexofsv, indexofsv + 1, "'" + scollate + " ");
				conditionString = sb3.toString() + sb4.toString();
			}

		}

		tableName = sourceName.toLowerCase();

		final String getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
				+ tableName + "' and data_type = 'jsonb'";
		String jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
		jsonField = jsonField != null ? "||" + jsonField : "";
		final String getFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
				+ tableName + "'";
		final String fields = jdbcTemplate.queryForObject(getFieldQuery, String.class);

		if (fields != null) {

			if (fields.contains(inputMap.get("valuemember").toString())) {
				getJSONKeysQuery = "select " + tableName + ".*,statusdata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus from " + tableName
						+ " where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNtranssitecode() + " and \"" + inputMap.get("valuemember")
						+ "\" > '0' " + conditionString + " ;";
			} else {
				getJSONKeysQuery = "select  " + tableName + ".*,statusdata->'stransdisplaystatus'->>'"
						+ userInfo.getSlanguagetypecode() + "' as stransdisplaystatus from " + tableName
						+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNtranssitecode() + " " + conditionString + " ;";
			}

			SampleStorageRetrievalDAOImpl.LOGGER.info("Retrieval Filter Query---> " + getJSONKeysQuery);

			List<Map<String, Object>> data = jdbcTemplate.queryForList(getJSONKeysQuery);

			data = (List<Map<String, Object>>) dateUtilityFunction.getSiteLocalTimeFromUTC(data,
					Arrays.asList("dtransactiondate"), Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null,
					false);

			returnObject.put((String) inputMap.get("label"), data);

		}
		return new ResponseEntity<>(returnObject, HttpStatus.OK);

	}

	/**
	 * This method is used to retrieve list of available unit(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode":
	 *                 -1,ntransitecode:1,nprojecttypecode: 1,spositionvalue:
	 *                 "P3092993530908"}}
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> getSelectedBarcodeData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> returnObject = new HashMap<>();
		Map<String, Object> object = new HashMap<>();
		Integer nprojecttypecode = null;
		// 25-06-25 ATE234 Janakumar ALPD-5307 sample retrieval and disposal ->Entering
		// the barcode ID and the thrown alert as "not existing" while performing
		// retrieval and disposal permits the process to proceed. String
		// sBarcodeid=(String) inputMap.get("spositionvalue");
		final String sBarcodeid = (String) inputMap.get("spositionvalue");

		String str = "select nprojecttypecode from samplestoragetransaction where spositionvalue='" + sBarcodeid.trim()
				+ "' and nprojecttypecode=" + inputMap.get("nprojecttypecode") + " and nsitecode="
				+ userInfo.getNtranssitecode();
		try {
			nprojecttypecode = jdbcTemplate.queryForObject(str, Integer.class);
		} catch (final Exception e) {
			nprojecttypecode = null;
		}
		if (nprojecttypecode == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SAMPLENOTEXIST", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		returnObject.put("nprojecttypecode", nprojecttypecode);
		returnObject.putAll(getProjectbarcodeconfig(returnObject, userInfo).getBody());
		final List<Map<String, Object>> lst = (List<Map<String, Object>>) returnObject.get("selectedProjectTypeList");
		if (lst.size() == 0) {
			nprojecttypecode = -1;
		}
		// ALPD-4774-Vignesh R(31-08-2024)
		str = "select * from view_samplelistprep_"
				+ (nprojecttypecode == -1 || nprojecttypecode > 3 ? 0 : nprojecttypecode) + " where spositionvalue='"
				+ sBarcodeid.trim() + "' and nsitecode=" + userInfo.getNtranssitecode();
		try {
			object = jdbcTemplate.queryForMap(str);
		} catch (final Exception e) {
			object = new HashMap<>();
		}
		returnObject.put("selectedBarcodeValue", object);

		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of available unit(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :{
	 *                 "fromDate": "2025-06-04T00:00:00", "toDate":
	 *                 "2025-06-11T23:59:59", "isFilterSubmit": true,
	 *                 "nprojecttypecode": 1, "nformcode": 203, "nmodulecode": 71,
	 *                 "nsitecode": 1, "ntranssitecode": 1, "ndeptcode": -1,
	 *                 "nreasoncode": 0, "nmastersitecode": -1, "nlogintypecode": 1,
	 *                 "ntimezonecode": -1, "nusersitecode": -1, "nissyncserver": 3,
	 *                 "nisstandaloneserver": 4, "istimezoneshow": 4,
	 *                 "isutcenabled": 4, "nsiteadditionalinfo": 4, "userinfo": {
	 *                 "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71 }, "ssessionid":
	 *                 "83A3B192135545A8921EA89A8DC62C0B", "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "stimezoneid": "Europe/London",
	 *                 "activelanguagelist": ["en-US"], "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "sreportlanguagecode": "en-US",
	 *                 "sreportingtoolfilename": "en.xml", "sformname": "Sample
	 *                 Retrieval and Disposal", "smodulename": "Storage Management",
	 *                 "ssitename": "THSTI BRF", "ssitecode": "SYNC", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "sloginid":
	 *                 "system", "sfirstname": "QuaLIS", "slastname": "Admin",
	 *                 "sdeptname": "NA", "sdeputyid": "system", "sdeputyusername":
	 *                 "QuaLIS Admin", "sdeputyuserrolename": "QuaLIS Admin",
	 *                 "sreason": "", "spredefinedreason": null, "spassword": null }
	 * 
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Map<String, Object>> getProjectbarcodeconfig(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> returnObject = new HashMap<>();
		List<Map<String, Object>> lst = new ArrayList<>();

		// Added by ATE234 JK based on settings table change the dynamic fields for BGSI

		final var nNeedInstrumentFlow = getInstrumentBased();

		if (nNeedInstrumentFlow == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

			final String[] fieldNames = { "Parent Sample Code", "Parent Sample Type", "Bio Sample Type",
					"Project Title", "Volume (μL)", "Cohort No.", "Case Type" };

			final List<Map<String, Object>> projectBarcodeConfig = IntStream.range(0, fieldNames.length).mapToObj(i -> {
				final Map<String, Object> map = new LinkedHashMap<>();
				map.put("sfieldname", fieldNames[i]);
				map.put("nsorter", String.valueOf(15 + i));
				return map;
			}).collect(Collectors.toList());

			returnObject.put("selectedProjectTypeList", projectBarcodeConfig);

		} else {

			final String str = "SELECT  nsorter,jsondata->>'sfieldname' as sfieldname FROM  projecttype pt , bulkbarcodeconfigversion bcv,"
					+ "  bulkbarcodeconfig bc,bulkbarcodeconfigdetails bcd "
					+ "  where bcd.nbulkbarcodeconfigcode = bc.nbulkbarcodeconfigcode and bcd.nprojecttypecode = bc.nprojecttypecode "
					+ "  and  bc.nprojecttypecode = " + inputMap.get("nprojecttypecode") + ""
					+ "  AND bcv.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ "" + "  AND  bc.nbulkbarcodeconfigcode = bcv.nbulkbarcodeconfigcode "
					+ "  AND  pt.nprojecttypecode = bc.nprojecttypecode AND pt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND bc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND bcv.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND bcd.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND bcv.nsitecode = "
					+ userInfo.getNmastersitecode() + " AND bcd.nsitecode=" + userInfo.getNmastersitecode()
					+ " order by nsorter;";

			try {
				lst = jdbcTemplate.queryForList(str);
			} catch (final Exception e) {
				lst = new ArrayList<>();
			}

			returnObject.put("selectedProjectTypeList", lst);

		}

		if (inputMap.containsKey("isFilterSubmit")) {
			inputMap.put("label", "samplestorageretrieval");
			inputMap.put("valuemember", "nsamplestoragetransactioncode");
			if (lst.size() == 0) {
				inputMap.put("view_nprojecttypecode", 0);
			} else {
				// ALPD-4774-Vignesh R(31-08-2024)
				inputMap.put("view_nprojecttypecode",
						(int) inputMap.get("nprojecttypecode") > 3 ? 0 : inputMap.get("nprojecttypecode"));
			}
			if ((int) inputMap.get("nprojecttypecode") == -1) {
				inputMap.put("view_nprojecttypecode", 0);
			}
			inputMap.put("source", "view_sampleretrieval_" + ((int) inputMap.get("view_nprojecttypecode")));
			returnObject.putAll(getDynamicFilterExecuteData(inputMap, userInfo).getBody());
		}
//		else {
//			if (lst.size() == 0) {
//				inputMap.put("view_nprojecttypecode", 0);
//			} else {
//				// ALPD-4774-Vignesh R(31-08-2024)
//				inputMap.put("view_nprojecttypecode",
//						(int) inputMap.get("nprojecttypecode") > 3 ? 0 : inputMap.get("nprojecttypecode"));
//			}
//			if ((int) inputMap.get("nprojecttypecode") == -1) {
//				inputMap.put("view_nprojecttypecode", 0);
//			}
//			inputMap.put("source", "view_sampleretrieval_" + ((int) inputMap.get("view_nprojecttypecode")));
//			returnObject.putAll(getDynamicFilterExecuteData(inputMap, userInfo).getBody());
//		}
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of available unit(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :{
	 *                 isRetrieve: true, nneedaliquot: false, nprojecttypecode: 1,
	 *                 nquantity: 0, saliquotsampleid: "", scomments: "-",
	 *                 spositionvalue: "P3092993530908", sunitname: "NA"
	 *                 userinfo:{"nformcode": 203, "nmodulecode": 71, "nsitecode":
	 *                 1, "ntranssitecode": 1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nmastersitecode": -1, "nlogintypecode": 1, "ntimezonecode":
	 *                 -1, "nusersitecode": -1, "nissyncserver": 3,
	 *                 "nisstandaloneserver": 4, "istimezoneshow": 4,
	 *                 "isutcenabled": 4, "nsiteadditionalinfo": 4, "userinfo": {
	 *                 "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71 }, "ssessionid":
	 *                 "83A3B192135545A8921EA89A8DC62C0B", "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "stimezoneid": "Europe/London",
	 *                 "activelanguagelist": ["en-US"], "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "sreportlanguagecode": "en-US",
	 *                 "sreportingtoolfilename": "en.xml", "sformname": "Sample
	 *                 Retrieval and Disposal", "smodulename": "Storage Management",
	 *                 "ssitename": "THSTI BRF", "ssitecode": "SYNC", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "sloginid":
	 *                 "system", "sfirstname": "QuaLIS", "slastname": "Admin",
	 *                 "sdeptname": "NA", "sdeputyid": "system", "sdeputyusername":
	 *                 "QuaLIS Admin", "sdeputyuserrolename": "QuaLIS Admin",
	 *                 "sreason": "", "spredefinedreason": null, "spassword": null
	 *                 }}
	 * 
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> createsamplestorageretrieval(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> returnObject = new HashMap<>();
		final ObjectMapper mapper = new ObjectMapper();

		final String sQuery = " lock  table locksamplestoragetransaction "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		SampleStorageRetrieval samplestorageretrieval;
		try {
			// ALPD-4739--Vignesh R(28-08-2024)
			final String strcheck = "select nsamplestoragetransactioncode,sposition,nsamplestoragelocationcode,nsamplestoragemappingcode from samplestoragetransaction "
					+ " where spositionvalue='" + inputMap.get("spositionvalue") + "' and nprojecttypecode="
					+ inputMap.get("nprojecttypecode") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode();

			samplestorageretrieval = (SampleStorageRetrieval) jdbcUtilityFunction.queryForObject(strcheck,
					SampleStorageRetrieval.class, jdbcTemplate);

			if (samplestorageretrieval != null) {

				final String sectSeq = "select nsequenceno from seqnostoragemanagement where stablename='samplestorageretrieval' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final int intseq = jdbcTemplate.queryForObject(sectSeq, Integer.class);

				final int nquantity = (int) inputMap.get("nquantity");

				final boolean nneedaliquot = (boolean) inputMap.get("nneedaliquot");

				final int nsamplestorageretrievalcode = intseq + 1;
				String str = "";
				
				// modified by sujatha ATE_274 by adding nbiosamplereceivingcode column insert missed before bgsi-218
				str = "INSERT INTO public.samplestorageretrieval( "
						+ "	nsamplestorageretrievalcode, nsamplestoragetransactioncode, nsamplestoragelocationcode,"
						+ " nsamplestoragelistcode, nsamplestoragemappingcode, nprojecttypecode,nusercode,nuserrolecode, nbiosamplereceivingcode, sposition, spositionvalue,"
						+ " jsondata, ntransactionstatus,ntransferstatuscode, ninstrumentcode, nbioparentsamplecode, sparentsamplecode, ncohortno, nproductcatcode, nproductcode, sqty, slocationcode, ssubjectid, scasetype,"
						+ "	ndiagnostictypecode, ncontainertypecode, nstoragetypecode, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode, nsitecode, nstatus)"
						+ "	 select  " + nsamplestorageretrievalcode + "  ," + "	nsamplestoragetransactioncode  ,"
						+ "    nsamplestoragelocationcode  ," + "	-1  , " + "    nsamplestoragemappingcode  ,"
						+ "    nprojecttypecode  ," + "    " + userInfo.getNusercode() + " ," + "    "
						+ userInfo.getNuserrole() + "  ," + "  nbiosamplereceivingcode,  sposition  ," + "    spositionvalue  ,"
						+ "    jsondata||jsonb_build_object('scomments','" + inputMap.get("scomments") + "')  ,"
						+ "	"
						+ ((boolean) inputMap.get("isRetrieve")
								? Enumeration.TransactionStatus.RETRIEVED.gettransactionstatus()
								: Enumeration.TransactionStatus.DISPOSED.gettransactionstatus())
						+ "," + "" + Enumeration.TransactionStatus.RETRIEVED.gettransactionstatus() + ","
						+ "ninstrumentcode,nbioparentsamplecode, sparentsamplecode, ncohortno, nproductcatcode, nproductcode, "
						+ "sqty, slocationcode,ssubjectid, scasetype, ndiagnostictypecode, ncontainertypecode, nstoragetypecode, "
						+ "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'  ," + "    "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "  ," + "    "
						+ userInfo.getNtimezonecode() + "  ," + "    nsitecode  ," + "    "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from samplestoragetransaction " + "   where   nsamplestoragetransactioncode="
						+ samplestorageretrieval.getNsamplestoragetransactioncode() + " and nsamplestoragelocationcode="
						+ samplestorageretrieval.getNsamplestoragelocationcode() + ";";
				
				// added by sujatha ATE_274 for inserting into sampleretrievaladditionalinfo while retrieving bgsi-218
				str += "INSERT INTO public.sampleretrievaladditionalinfo( nsamplestorageretrievalcode, sextractedsampleid, sconcentration,"
						+ " sqcplatform, seluent, dmodifieddate, nsitecode, nstatus)"
						+ " select "+ nsamplestorageretrievalcode +", sextractedsampleid, sconcentration, sqcplatform, seluent, "
						+ "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' , nsitecode, " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from samplestorageadditionalinfo where nsamplestoragetransactioncode="+ samplestorageretrieval.getNsamplestoragetransactioncode()+ ";";
						
				if (nneedaliquot) {

					final String barcodeLength = inputMap.get("saliquotsampleid").toString().replaceAll("\\s", "")
							.trim();
					final String aliquotPlanCheck = "select nsamplestoragetransactioncode,sposition,nsamplestoragelocationcode,nsamplestoragemappingcode from samplestoragetransaction "
							+ " where spositionvalue='" + barcodeLength + "' and nprojecttypecode="
							+ inputMap.get("nprojecttypecode") + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ userInfo.getNtranssitecode();

					final SampleStorageRetrieval samplestorageretrievalAliquotPlan = (SampleStorageRetrieval) jdbcUtilityFunction
							.queryForObject(aliquotPlanCheck, SampleStorageRetrieval.class, jdbcTemplate);

					boolean isBarcodeID = false;

					final String query = " select bb.nbarcodelength as nfieldlength from bulkbarcodeconfig bb,bulkbarcodeconfigversion bbv where "
							+ "bb.nprojecttypecode= " + inputMap.get("nprojecttypecode") + " "
							+ "and bb.nbulkbarcodeconfigcode=bbv.nbulkbarcodeconfigcode and bbv.ntransactionstatus="
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and bb.nsitecode="
							+ userInfo.getNmastersitecode() + " and bb.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

					final List<BulkBarcodeConfigDetails> barcodelistLength = jdbcTemplate.query(query,
							new BulkBarcodeConfigDetails());

					if (!barcodelistLength.isEmpty()) {
						final int nbarcodeLength = barcodeLength.length();
						int formattingLength = barcodelistLength.get(0).getNfieldlength();

						if (nbarcodeLength < formattingLength) {
							return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INVAILDBARCODEID",
									userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
						} else if (nbarcodeLength > formattingLength) {
							final String daugtherAliquotPlan = "select ssettingvalue from settings where nsettingcode="
									+ Enumeration.Settings.DAUGHTERALIQUOTPLAN.getNsettingcode() + " and nstatus = "
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
							final Settings objSettings = (Settings) jdbcUtilityFunction
									.queryForObject(daugtherAliquotPlan, Settings.class, jdbcTemplate);

							if (objSettings != null) {
								final int daugtherAliquotPlanLength = Integer.parseInt(objSettings.getSsettingvalue());
								formattingLength = formattingLength + daugtherAliquotPlanLength;
								if (nbarcodeLength == formattingLength) {
									isBarcodeID = true;
								} else {
									return new ResponseEntity<>(
											commonFunction.getMultilingualMessage("IDS_INVAILDBARCODEID",
													userInfo.getSlanguagefilename()),
											HttpStatus.CONFLICT);

								}
							}
						} else {
							isBarcodeID = true;
						}

					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_PROJECTCONFIGINFORMATTINGSCREEN",
										userInfo.getSlanguagefilename()),
								HttpStatus.CONFLICT);
					}
					// ALPD-5074 Sample Storage -> THIST - Daughter Aliquot count of barcode ID.

					if (samplestorageretrievalAliquotPlan == null && isBarcodeID) {

						// ATE234 Janakumar ALPD-5080 Sample Retrieval and Disposal-->While doing Input
						// for Aliquot Sample, barcode Info was showing wrongly in sample storage
						// ,repository

						final Map<String, Object> objJsonDataList = new HashMap<String, Object>();

						final Map<String, Object> inputMapList = new HashMap<String, Object>();
						inputMapList.put("jsondata", objJsonDataList);
						inputMapList.put("spositionvalue", inputMap.get("saliquotsampleid"));
						inputMapList.put("nprojecttypecode", inputMap.get("nprojecttypecode"));

						final Map<String, Object> barcodeDetails = sampleStorageTransactionDAO.readBarcode(inputMapList,
								userInfo);

						final Map<String, Object> jsondataBarcodeData = mapper
								.readValue(barcodeDetails.get("jsonValue").toString(), Map.class);

						jsondataBarcodeData.put("IDS_SAMPLEID", inputMap.get("saliquotsampleid"));
						jsondataBarcodeData.put("IDS_UNITNAME", inputMap.get("sunitname"));
						jsondataBarcodeData.put("IDS_QUANTITY", nquantity);
						jsondataBarcodeData.put("IDS_POSITION", samplestorageretrieval.getSposition());

						final String jsonString = mapper.writeValueAsString(jsondataBarcodeData);

						str += " update  samplestoragetransaction set jsondata='" + jsonString + "',spositionvalue= '"
								+ inputMap.get("saliquotsampleid") + "' " + " where   nsamplestoragetransactioncode= "
								+ samplestorageretrieval.getNsamplestoragetransactioncode() + ";";
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALIQUOTSAMPLEID",
								userInfo.getSlanguagefilename()) + " "
								+ commonFunction.getMultilingualMessage("IDS_ALREADYEXISTS",
										userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);

					}
				} else {
					str += " delete from samplestoragetransaction where nsamplestoragetransactioncode="
							+ samplestorageretrieval.getNsamplestoragetransactioncode() + ";";
					//added by sujatha ATE_274 for deleting the retrieving record in the samplestorageadditionalinfo  bgsi-218
					str += " delete from samplestorageadditionalinfo where nsamplestoragetransactioncode="
							+ samplestorageretrieval.getNsamplestoragetransactioncode() + ";";

				}
				str += " update seqnostoragemanagement set nsequenceno= " + nsamplestorageretrievalcode
						+ " where stablename='samplestorageretrieval';";

				jdbcTemplate.execute(str);

				// ALPDJ21-116--Added by Vignesh(08-10-2025)--storestatus set to false when the
				// retrieval and disposal.
				updateSampleSendToStore(String.valueOf(samplestorageretrieval.getNsamplestoragetransactioncode()),
						userInfo);

				// ===== COC: START =====
				
				String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				jdbcTemplate.execute(sQuery1);
		 
				String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				jdbcTemplate.execute(sQuery2);
		 
				String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				jdbcTemplate.execute(sQuery3);
				
				int chainCustodyPk = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);

				int nCocTxnStatus = ((boolean) inputMap.get("isRetrieve"))
						? Enumeration.TransactionStatus.RETRIEVED.gettransactionstatus()
						: Enumeration.TransactionStatus.DISPOSED.gettransactionstatus();

				String strChainCustody = "insert into chaincustody(nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,sitemno,"
				        + "ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,noffsetdtransactiondate,sremarks,nsitecode,nstatus) "
				        + "select " + (chainCustodyPk + 1) + "," + userInfo.getNformcode()
				        + ",nsamplestorageretrievalcode,'nsamplestorageretrievalcode','samplestorageretrieval',spositionvalue,"
				        + nCocTxnStatus + "," + userInfo.getNusercode() + "," + userInfo.getNuserrole() + ",'"
				        + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtimezonecode() + ","
				        + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ",'"
				        + commonFunction.getMultilingualMessage("IDS_SAMPLEID", userInfo.getSlanguagefilename())
				        + " [' || coalesce(spositionvalue,'') || '] ' || '"
				        + commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
				        + " [' || coalesce(sparentsamplecode,'') || '] ' || '"
				        + commonFunction.getMultilingualMessage("IDS_SAMPLELOCATION", userInfo.getSlanguagefilename())
				        + " [' || coalesce(slocationcode,'') || '] ' || '"
				        + commonFunction.getMultilingualMessage("IDS_SENTFROM", userInfo.getSlanguagefilename())
				        + " [' || coalesce(s.ssitename,'') || ']',"
				        + userInfo.getNtranssitecode() + ","
				        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				        + " from samplestorageretrieval"
				        + " join site s on s.nsitecode = samplestorageretrieval.nsitecode"
				        + " where nsamplestorageretrievalcode=" + nsamplestorageretrievalcode + ";";

				String strSeqUpdateCOC = "update seqnoregistration set nsequenceno=" + (chainCustodyPk + 1)
				        + " where stablename='chaincustody' and nstatus="
				        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";


				jdbcTemplate.execute(strChainCustody);
				jdbcTemplate.execute(strSeqUpdateCOC);
				// ===== COC: END =====
				
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> savedTestList = new ArrayList<>();

				samplestorageretrieval.setNsamplestorageretrievalcode(nsamplestorageretrievalcode);
				samplestorageretrieval.setSpositionvalue((String) inputMap.get("spositionvalue"));
				samplestorageretrieval.setScomments((String) inputMap.get("scomments"));

				if ((boolean) inputMap.get("isRetrieve")) {
					samplestorageretrieval
							.setNtransactionstatus(Enumeration.TransactionStatus.RETRIEVED.gettransactionstatus());
				} else {
					samplestorageretrieval
							.setNtransactionstatus(Enumeration.TransactionStatus.DISPOSED.gettransactionstatus());
				}
				savedTestList.add(samplestorageretrieval);
				multilingualIDList.add("IDS_RETRIEVEORDISPOSESAMPLE");

				// fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);

				return new ResponseEntity<>(returnObject, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SAMPLENOTEXIST", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} catch (final Exception e) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SAMPLENOTEXIST", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to retrieve list of available unit(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :{
	 *                 "fromDate": "2025-06-04T00:00:00", "toDate":
	 *                 "2025-06-11T23:59:59", "isFilterSubmit": true,
	 *                 "nprojecttypecode": 1,"retrieveDisposeSampleType":97,
	 *                 "userInfo":{"nformcode": 203, "nmodulecode": 71, "nsitecode":
	 *                 1, "ntranssitecode": 1, "ndeptcode": -1, "nreasoncode": 0,
	 *                 "nmastersitecode": -1, "nlogintypecode": 1, "ntimezonecode":
	 *                 -1, "nusersitecode": -1, "nissyncserver": 3,
	 *                 "nisstandaloneserver": 4, "istimezoneshow": 4,
	 *                 "isutcenabled": 4, "nsiteadditionalinfo": 4, "userinfo": {
	 *                 "nusercode": -1, "nuserrole": -1, "ndeputyusercode": -1,
	 *                 "ndeputyuserrole": -1, "nmodulecode": 71 }, "ssessionid":
	 *                 "83A3B192135545A8921EA89A8DC62C0B", "sconnectionString":
	 *                 "Server=localhost;Port=5433;Database=THIST02-06-1;User=postgres;Pwd=admin@123;",
	 *                 "sdatetimeformat": "dd/MM/yyyy HH:mm:ss",
	 *                 "spgdatetimeformat": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitedatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "spgsitereportdatetime": "dd/MM/yyyy HH24:mi:ss",
	 *                 "ssitedate": "dd/MM/yyyy", "ssitedatetime": "dd/MM/yyyy
	 *                 HH:mm:ss", "ssitereportdate": "dd/MM/yyyy",
	 *                 "ssitereportdatetime": "dd/MM/yyyy HH:mm:ss", "sgmtoffset":
	 *                 "UTC +00:00", "stimezoneid": "Europe/London",
	 *                 "activelanguagelist": ["en-US"], "slanguagefilename":
	 *                 "Msg_en_US", "slanguagename": "English", "slanguagetypecode":
	 *                 "en-US", "sreportlanguagecode": "en-US",
	 *                 "sreportingtoolfilename": "en.xml", "sformname": "Sample
	 *                 Retrieval and Disposal", "smodulename": "Storage Management",
	 *                 "ssitename": "THSTI BRF", "ssitecode": "SYNC", "susername":
	 *                 "QuaLIS Admin", "suserrolename": "QuaLIS Admin", "sloginid":
	 *                 "system", "sfirstname": "QuaLIS", "slastname": "Admin",
	 *                 "sdeptname": "NA", "sdeputyid": "system", "sdeputyusername":
	 *                 "QuaLIS Admin", "sdeputyuserrolename": "QuaLIS Admin",
	 *                 "sreason": "", "spredefinedreason": null, "spassword": null}
	 *                 }
	 * 
	 * @return response entity object holding response status and list of all units
	 * @throws Exception exception
	 */
	@Override
	public ResponseEntity<Object> createbulkeretrievedispose(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> returnObject = new HashMap<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedTestList = new ArrayList<>();
		final MultipartFile multipartFile = request.getFile("ImportFile");
		final int ntransactionstatus = Integer.parseInt(request.getParameter("retrieveDisposeSampleType"));
		final String fieldName = StringEscapeUtils.unescapeJava(request.getParameter("fieldName")).toString();
		final InputStream ins = multipartFile.getInputStream();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final byte[] buffer = new byte[1024];
		int len;
		while ((len = ins.read(buffer)) > -1) {
			baos.write(buffer, 0, len);
		}
		baos.flush();
		try {
			final InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
			final Workbook workbook = WorkbookFactory.create(is1);
			final Sheet sheet = workbook.getSheetAt(0);

			int rowIndex = 0;
			final List<String> lstHeader = new ArrayList<>();
			final StringJoiner positionValue = new StringJoiner(",");
			int cellIndex = 0;
			for (final Row row : sheet) {
				if (rowIndex > 0) {
					if (!lstHeader.isEmpty()) {
						for (int i = 0; i < lstHeader.size(); i++) // iteration over cell using for each loop
						{// ATE234 Janakumar ALPD-5163 Sample Retrieval and Disposal-->cant able to do
							// retrieve and disposal for imported samples(specicf scenario)
							if (row.getCell(cellIndex) != null) {
								LOGGER.info(row.getCell(cellIndex).toString().trim());
								final Cell cell = row.getCell(cellIndex);
								positionValue.add("'" + cell.toString().trim() + "'");
							} else {
								positionValue.add("");
							}
						}
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INVALIDTEMPLATEHEADERS",
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				} else {

					for (final Cell cell : row) // iteration over cell using for each loop
					{
						final String header = cell.getStringCellValue();
						if (header.trim().equals(fieldName.trim())) {
							lstHeader.add(header);
							break;
						}
						cellIndex++;
					}
				}
				rowIndex++;
			}
			if (!positionValue.toString().isEmpty()) {
				final String sQuery = " lock  table locksamplestoragetransaction "
						+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				jdbcTemplate.execute(sQuery);

				final String sectSeq = "select nsequenceno from seqnostoragemanagement where stablename='samplestorageretrieval' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final int intseq = jdbcTemplate.queryForObject(sectSeq, Integer.class);
				String queryExc = "";
				final String spositionvalue = positionValue.toString();
				
				// modified by sujatha ATE_274 by adding nbiosamplereceivingcode column for insert which is missed before bgsi-218
				queryExc = "INSERT INTO public.samplestorageretrieval( "
						+ "	nsamplestorageretrievalcode, nsamplestoragetransactioncode, nsamplestoragelocationcode,"
						+ " nsamplestoragelistcode, nsamplestoragemappingcode, nprojecttypecode,nusercode,nuserrolecode, nbiosamplereceivingcode, sposition, spositionvalue,"
						+ " jsondata, ntransactionstatus,ntransferstatuscode, ninstrumentcode, nbioparentsamplecode, sparentsamplecode, ncohortno, nproductcatcode, nproductcode, sqty, slocationcode, ssubjectid, scasetype,"
						+ "	ndiagnostictypecode, ncontainertypecode, nstoragetypecode, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode, nsitecode, nstatus)"
						+ "	 select  " + intseq + "+ROW_NUMBER() OVER ( ORDER BY nsamplestoragetransactioncode )  ,"
						+ "	nsamplestoragetransactioncode  ," + "    nsamplestoragelocationcode  ," + "	-1  , "
						+ "nsamplestoragemappingcode  ," + "    nprojecttypecode  ," + "    " + userInfo.getNusercode()
						+ " ," + "    " + userInfo.getNuserrole() + "  ," + "  nbiosamplereceivingcode,  sposition  ," + "    spositionvalue  ,"
						+ "    jsondata , " + ntransactionstatus + "  ," + ""
						+ Enumeration.TransactionStatus.RETRIEVED.gettransactionstatus() + ","
						+ "ninstrumentcode,nbioparentsamplecode, sparentsamplecode, ncohortno, nproductcatcode, nproductcode, "
						+ "sqty, slocationcode,ssubjectid, scasetype, ndiagnostictypecode, ncontainertypecode, nstoragetypecode, "
						+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'  ," + "    "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "  ," + "    "
						+ userInfo.getNtimezonecode() + "  ," + "    nsitecode  ," + "    "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from samplestoragetransaction " + "   where   spositionvalue in (" + spositionvalue + ") ;";
				
				// added by sujatha ATE_274 for inserting into sampleretrievaladditionalinfo while retrieving bgsi-218
				queryExc += "INSERT INTO public.sampleretrievaladditionalinfo( nsamplestorageretrievalcode, sextractedsampleid, sconcentration,"
						+ " sqcplatform, seluent, dmodifieddate, nsitecode, nstatus)"
						+ " select "+ intseq + "+ROW_NUMBER() OVER ( ORDER BY ssa.nsamplestoragetransactioncode )  ,"
						+ " ssa.sextractedsampleid, ssa.sconcentration, ssa.sqcplatform, ssa.seluent, "
						+ "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' , ssa.nsitecode, " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from samplestorageadditionalinfo ssa join samplestoragetransaction sst on sst.nsamplestoragetransactioncode = ssa.nsamplestoragetransactioncode"
						+ " and sst.spositionvalue in ("+ spositionvalue +");";
				
				// added by sujatha for deleting from samplestorageadditionalinfo table while retrieving sample bgsi-218
				queryExc += " delete from samplestorageadditionalinfo ssa"
							+ " where ssa.nsamplestoragetransactioncode in ("
							+ " select nsamplestoragetransactioncode  from samplestoragetransaction where "
							+ " spositionvalue in ("+ spositionvalue + "));";
				
				queryExc += " delete from samplestoragetransaction where spositionvalue in (" + spositionvalue + ");";
				
				queryExc += " update seqnostoragemanagement set nsequenceno= (select max(nsamplestorageretrievalcode) from samplestorageretrieval)"
						+ " where stablename='samplestorageretrieval';";

				jdbcTemplate.execute(queryExc);
				
				// ===== COC: START =====
				
				String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				jdbcTemplate.execute(sQuery1);
		 
				String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				jdbcTemplate.execute(sQuery2);
		 
				String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				jdbcTemplate.execute(sQuery3);
				
				int chainCustodyPk = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);

				String strChainCustody = "insert into chaincustody(nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,sitemno,"
				        + "ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,noffsetdtransactiondate,"
				        + "sremarks,nsitecode,nstatus) "
				        + "select (" + chainCustodyPk + " + rank() over(order by r.nsamplestorageretrievalcode)),"
				        + userInfo.getNformcode()
				        + ",r.nsamplestorageretrievalcode,'nsamplestorageretrievalcode','samplestorageretrieval',r.spositionvalue,"
				        + ntransactionstatus + "," + userInfo.getNusercode() + "," + userInfo.getNuserrole() + ",'"
				        + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtimezonecode() + ","
				        + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ",'"
				        + commonFunction.getMultilingualMessage("IDS_SAMPLEID", userInfo.getSlanguagefilename())
				        + " [' || coalesce(r.spositionvalue,'') || '] ' || '"
				        + commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
				        + " [' || coalesce(r.sparentsamplecode,'') || '] ' || '"
				        + commonFunction.getMultilingualMessage("IDS_SAMPLELOCATION", userInfo.getSlanguagefilename())
				        + " [' || coalesce(r.slocationcode,'') || '] ' || '"
				        + commonFunction.getMultilingualMessage("IDS_SENTFROM", userInfo.getSlanguagefilename())
				        + " [' || coalesce(s.ssitename,'') || ']',"
				        + userInfo.getNtranssitecode() + ","
				        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				        + " from samplestorageretrieval r "
				        + " join site s on s.nsitecode = r.nsitecode "
				        + " where r.spositionvalue in (" + spositionvalue + ") and r.nsamplestorageretrievalcode > " + intseq + ";";


				String strSeqUpdateCOC = " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
				        + " + count(nsamplestorageretrievalcode) from samplestorageretrieval "
				        + "where spositionvalue in (" + spositionvalue + ") and nsamplestorageretrievalcode > " + intseq
				        + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				        + ") where stablename='chaincustody' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				jdbcTemplate.execute(strChainCustody);
				jdbcTemplate.execute(strSeqUpdateCOC);
				// ===== COC: END =====

				returnObject.put("isFilterSubmit", true);
				returnObject.put("nprojecttypecode", Integer.parseInt(request.getParameter("nprojecttypecode")));
				returnObject.put("fromDate", request.getParameter("fromDate").replaceAll("T", " "));
				returnObject.put("toDate", request.getParameter("toDate").replaceAll("T", " "));
				final SampleStorageRetrieval samplestorageretrieval = new SampleStorageRetrieval();
				final String typeName = ntransactionstatus == Enumeration.TransactionStatus.RETRIEVED
						.gettransactionstatus() ? "Bulk Retrieved" : "Bulk Disposed";
				samplestorageretrieval.setNtransactionstatus(ntransactionstatus);
				samplestorageretrieval.setScomments(typeName);
				savedTestList.add(samplestorageretrieval);
				multilingualIDList.add("IDS_BULKRETRIEVEORDISPOSESAMPLE");
				auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);
				returnObject.putAll(getProjectbarcodeconfig(returnObject, userInfo).getBody());
				returnObject.remove("fromDate");
				returnObject.remove("toDate");
				return new ResponseEntity<>(returnObject, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_NORECORDINEXCEL", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}

		} catch (final Exception e) {// 4492 janakumar inproper file upload in the retrive or dispose
			e.printStackTrace();
			LOGGER.error("Error: upload file not proper file." + e);

			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_INVALIDTEMPLATEHEADERS",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	// ALPDJ21-116--Added by Vignesh(08-10-2025)--storestatus set to false when the
	// retrieval and disposal.
	private void updateSampleSendToStore(final String sSamplestorageTransactionCode, final UserInfo userInfo) {
		final String strQuery = "SELECT " + "  nsamplestoragetransactioncode, "
				+ "  NULLIF(sr.jsondata->>'npreregno', '')::int AS npreregno, "
				+ "  NULLIF(sr.jsondata->>'ntransactionsamplecode', '')::int AS ntransactionsamplecode "
				+ "FROM samplestorageretrieval sr " + "JOIN samplestoragelocation sl "
				+ "  ON sr.nsamplestoragelocationcode = sl.nsamplestoragelocationcode " + "WHERE sr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND sl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND sr.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + "  AND ( sr.jsondata->>'npreregno' <> '' "
				+ "        OR sr.jsondata->>'ntransactionsamplecode' <> '' ) "
				+ "  AND sr.nsamplestoragetransactioncode IN (" + sSamplestorageTransactionCode + ");";

		List<Map<String, Object>> storeList = jdbcTemplate.queryForList(strQuery);

		if (!storeList.isEmpty()) {

			String preregNoList = storeList.stream().map(row -> (Integer) row.get("npreregno")).filter(Objects::nonNull)
					.map(String::valueOf).collect(Collectors.joining(","));

			String transactionSampleCodeList = storeList.stream()
					.map(row -> (Integer) row.get("ntransactionsamplecode")).filter(Objects::nonNull)
					.map(String::valueOf).collect(Collectors.joining(","));

			StringBuilder strRegistrationStatusUpdate = new StringBuilder();

			if (!preregNoList.isEmpty()) {
				strRegistrationStatusUpdate
						.append("UPDATE registrationflagstatus " + "SET bstorageflag = false " + "WHERE npreregno IN ("
								+ preregNoList + ") " + "AND nsitecode = " + userInfo.getNtranssitecode() + " "
								+ "AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";");
			}

			if (!transactionSampleCodeList.isEmpty()) {
				strRegistrationStatusUpdate.append("UPDATE regsubsampleflagstatus " + "SET bstorageflag = false "
						+ "WHERE ntransactionsamplecode IN (" + transactionSampleCodeList + ") " + "AND nsitecode = "
						+ userInfo.getNtranssitecode() + " " + "AND nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";");
			}

			if (strRegistrationStatusUpdate.length() > 0) {
				jdbcTemplate.execute(strRegistrationStatusUpdate.toString());
			}
		}
	}

	// JANAKUMAR-BGSI-17-OCT-2025
	private int getInstrumentBased() throws Exception {

		final var sSetting = "select ssettingvalue from settings where nsettingcode ="
				+ Enumeration.Settings.INSTRUMENT_BASED_STORAGE.getNsettingcode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		final var sNeedStorageFlow = (String) jdbcUtilityFunction.queryForObject(sSetting, String.class, jdbcTemplate);
		final var nNeedInstrumentFlow = Integer.parseInt(sNeedStorageFlow);
		return nNeedInstrumentFlow;
	}

}
