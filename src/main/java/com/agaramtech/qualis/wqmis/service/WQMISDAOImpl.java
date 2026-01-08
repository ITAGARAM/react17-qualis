package com.agaramtech.qualis.wqmis.service;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.wqmis.model.WQMISContaminatedFTKSampleParameters;
import com.agaramtech.qualis.wqmis.model.WQMISContaminatedFTKSamples;
import com.agaramtech.qualis.wqmis.model.WQMISContaminatedLabSampleParameters;
import com.agaramtech.qualis.wqmis.model.WQMISContaminatedLabSamples;
import com.agaramtech.qualis.wqmis.model.WQMISFTKSamples;
import com.agaramtech.qualis.wqmis.model.WQMISFTKSampleParameters;
import com.agaramtech.qualis.wqmis.model.JjmBlockList;
import com.agaramtech.qualis.wqmis.model.JjmDistrictList;
import com.agaramtech.qualis.wqmis.model.JjmGpList;
import com.agaramtech.qualis.wqmis.model.JjmVillageList;
import com.agaramtech.qualis.wqmis.model.Jjmlabdata;
import com.agaramtech.qualis.wqmis.model.WQMISLabSampleParameters;
import com.agaramtech.qualis.wqmis.model.WQMISLabSamples;
import com.agaramtech.qualis.wqmis.model.WQMISApi;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.BlocksListResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.DistrictListResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.GpListResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.LabDataList;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.MeasurementDataResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.ParameterDataList;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.ParametersDataFTKUserList;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.ReagentsListResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.SampleLocationListResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.StateMemberSecretaryResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.VillageListResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.WQMISStateLevelMappingResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.WaterSourceListResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.WqmisEquipmentDataResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.WqmisHblistResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.transaction.WQMISContaminatedFTKSampleResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.transaction.WQMISContaminatedFTKSampleResults;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.transaction.WQMISContaminatedLabSampleResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.transaction.WQMISContaminatedLabSampleResults;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.transaction.WQMISFTKSampleDataResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.transaction.WQMISFTKSampleResults;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.transaction.WQMISLabSampleDataResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.transaction.WQMISLabSampleResults;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.WqmisLabDataResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.WqmisLabOfficialDataResponse;
import com.agaramtech.qualis.wqmis.model.jsonResponseParser.master.WqmisSamplesubmitterResponse;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class WQMISDAOImpl implements WQMISDAO {

	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final CommonFunction commonFunction;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final StringUtilityFunction stringUtilityFunction;

	public ResponseEntity<Object> getLabSamples(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		Instant syncStartDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		String url = inputMap.get("surl") + "?StateId=18&VillageId=" + inputMap.get("nvillageid") + "";
		String authToken = (String) inputMap.get("stokenid");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = response.getBody();
			WQMISLabSampleDataResponse objSampleDataResponse = objectMapper.readValue(jsonString,
					WQMISLabSampleDataResponse.class);
			String str = "select * from jjmvillagelist where nvillageid=" + inputMap.get("nvillageid") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			JjmVillageList objJjmVillageList = (JjmVillageList) jdbcUtilityFunction.queryForObject(str,
					JjmVillageList.class, jdbcTemplate);
			if (objSampleDataResponse != null && objJjmVillageList != null) {
				for (WQMISLabSampleResults res : objSampleDataResponse.getResult()) {
					if (res instanceof WQMISLabSampleResults) {
						if (res != null && res.getParameterResult() != null && !res.getParameterResult().isEmpty()) {
							Date testDate = res.test_date;
							SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
							String formattedTestDate = dateFormatter.format(testDate);
							Calendar cal = Calendar.getInstance();
							cal.setTime(testDate);
							int testYear = cal.get(Calendar.YEAR);
							jdbcTemplate.update("INSERT INTO public.wqmislabsamples( "
									+ "ntestid, ssampleid, dtesteddate, slocation, sissafe, nlabid, nsourceid, ssourcetype, "
									+ "ssourcesubtype, sschemeid, ssamplelocationfrom, sremedialactionstatus,nstateid,ndistrictid,"
									+ "nblockid, npanchayatid,nvillageid,nhabitationid, nstatelgdcode, ndistrictlgdcode, nblocklgdcode,"
									+ " npanchayatlgdcode, nvillagelgdcode,nyear) " + "VALUES(" + res.testId + ", N'"
									+ stringUtilityFunction.replaceQuote(res.sampleId) + "', '" + formattedTestDate
									+ "', " + " N'" + stringUtilityFunction.replaceQuote(res.location) + "', '"
									+ stringUtilityFunction.replaceQuote(res.is_safe) + "', " + res.lab_id + ", "
									+ res.sourceId + ", N'" + stringUtilityFunction.replaceQuote(res.source_type)
									+ "', N'" + stringUtilityFunction.replaceQuote(res.source_subtype) + "'," + " N'"
									+ stringUtilityFunction.replaceQuote(res.schemeId) + "', N'"
									+ stringUtilityFunction.replaceQuote(res.sampleLocationfrom) + "', " + " N'"
									+ stringUtilityFunction.replaceQuote(res.remedial_action_status) + "',"
									+ objJjmVillageList.getNstateid() + ", " + objJjmVillageList.getNdistrictid() + ","
									+ objJjmVillageList.getNblockid() + "," + " " + objJjmVillageList.getNpanchayatid()
									+ "," + res.villageId + ", " + res.habitationId + ", " + res.stateLGD_code + ","
									+ " " + res.districtLGD_Code + ", " + res.blockLGD_Code + ", "
									+ res.panchyatLGD_Code + ", " + res.villageLGD_Code + "," + testYear + ")"
									+ "ON CONFLICT (ntestid, ssampleid) DO NOTHING");

							res.getParameterResult().forEach(d -> {
								jdbcTemplate.update(
										"INSERT INTO wqmislabsampleparameters (ntestid, ssampleid,nparameterid, sparametervalue) "
												+ "VALUES (?, ?, ?, ?) ON CONFLICT (ntestid,ssampleid,nparameterid) DO NOTHING",
										d.getTestId(), res.sampleId, d.getParameterId(),
										stringUtilityFunction.replaceQuote(d.getParameterValue()));
							});
						}
					}
				}
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncStartDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode="
						+ inputMap.get("nwqmisapicode") + "");
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server
			} else {
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncStartDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode="
						+ inputMap.get("nwqmisapicode") + "");
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODATATOSYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.NO_CONTENT);// 204 No Data to Synchronize from
																					// WQMIS API
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncStartDate + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode="
					+ inputMap.get("nwqmisapicode") + "");
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR);// 500 Try to Synchronize the Data from WQMIS API Server Later
		}

	}

	@Override
	public ResponseEntity<Object> getWQMISTransactionApiDropdown(UserInfo userInfo) {
		Map<String, Object> returnMap = new HashMap<>();
		String districtQuery = "select * from jjmdistrictlist where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<JjmDistrictList> districtList = jdbcTemplate.query(districtQuery, new JjmDistrictList());
		String strQuery = "select w.*, COALESCE((to_char(w.dsyncstartdate, '" + userInfo.getSpgsitedatetime()
				+ "')), '-') as ssyncstartdate, " + "  COALESCE((to_char(w.dsyncenddate, '"
				+ userInfo.getSpgsitedatetime() + "')), '-') as ssyncenddate "
				+ " from wqmisapi w where w.nismastertable=" + Enumeration.TransactionStatus.NO.gettransactionstatus()
				+ " and w.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by w.nsorter asc";
		List<WQMISApi> wqmisList = jdbcTemplate.query(strQuery, new WQMISApi());
		returnMap.put("district", districtList);
		returnMap.put("apiData", wqmisList);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getBlock(JjmBlockList jjmBlockList) {
		Map<String, Object> returnMap = new HashMap<>();
		String blockQuery = "select * from jjmblocklist where ndistrictid = " + jjmBlockList.getNdistrictid()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<JjmBlockList> blockList = jdbcTemplate.query(blockQuery, new JjmBlockList());
		returnMap.put("block", blockList);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getPanchayat(JjmGpList jjmGpList) {
		Map<String, Object> returnMap = new HashMap<>();
		String panchayatQuery = "select * from jjmgplist where nblockid = " + jjmGpList.getNblockid()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<JjmGpList> panchayatList = jdbcTemplate.query(panchayatQuery, new JjmGpList());
		returnMap.put("panchayat", panchayatList);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getVillage(JjmVillageList jjmVillageList) {
		Map<String, Object> returnMap = new HashMap<>();
		String villageQuery = "select * from jjmvillagelist where npanchayatid = " + jjmVillageList.getNpanchayatid()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<JjmVillageList> villageList = jdbcTemplate.query(villageQuery, new JjmVillageList());
		returnMap.put("village", villageList);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	// neeraj Start
	@Override
	public ResponseEntity<Object> getWQMISLabSamples(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo, Map<String, Object> inputMap) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					currentUIDate, "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");
			outputMap.put("FromDate", (String) mapObject.get("FromDateWOUTC"));
			outputMap.put("ToDate", (String) mapObject.get("ToDateWOUTC"));
		} else {

			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);

			outputMap.put("FromDate", fromDateUI);
			outputMap.put("ToDate", toDateUI);
			fromDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
			toDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		}
		String conditionValue = "";
		if (inputMap.containsKey("ndistrictid") && inputMap.get("ndistrictid") != null
				&& (int) inputMap.get("ndistrictid") != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			conditionValue = conditionValue + " and ndistrictid=" + (int) inputMap.get("ndistrictid") + " ";
		}
		if (inputMap.containsKey("nblockid") && inputMap.get("nblockid") != null
				&& (int) inputMap.get("nblockid") != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			conditionValue = conditionValue + " and nblockid=" + (int) inputMap.get("nblockid") + " ";
		}
		if (inputMap.containsKey("npanchayatid") && inputMap.get("npanchayatid") != null
				&& (int) inputMap.get("npanchayatid") != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			conditionValue = conditionValue + " and npanchayatid=" + (int) inputMap.get("npanchayatid") + " ";
		}
		if (inputMap.containsKey("nvillageid") && inputMap.get("nvillageid") != null
				&& (int) inputMap.get("nvillageid") != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			conditionValue = conditionValue + " and nvillageid=" + (int) inputMap.get("nvillageid") + " ";
		}
		LocalDateTime fromDateTime = LocalDateTime.parse(fromDate);
		int fromYear = fromDateTime.getYear();
		LocalDateTime toDateTime = LocalDateTime.parse(toDate);
		int toYear = toDateTime.getYear();
		final String strQuery = "select row_number() OVER () AS npkid ,COALESCE(TO_CHAR(dtesteddate, '"
				+ userInfo.getSpgsitedatetime() + "'), '') as stesteddate,* from" + " wqmislabsamples  where "
				+ " dtesteddate between '" + fromDate + "' and '" + toDate + "' " + " and nyear  between " + fromYear
				+ " and " + toYear + " " + conditionValue;
		final List<WQMISLabSamples> lstLabSamples = (List<WQMISLabSamples>) jdbcTemplate.query(strQuery,
				new WQMISLabSamples());

		final String strquery = "select jd.sdistrictname ,jd.ndistrictid from jjmdistrictlist jd where "
				+ " jd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<JjmDistrictList> lstDistrictList = (List<JjmDistrictList>) jdbcTemplate.query(strquery,
				new JjmDistrictList());
		outputMap.put("DistrictList", lstDistrictList);
		outputMap.put("LabSamples", lstLabSamples);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getWQMISLabSamplesParametersDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final int ntestid = Integer.valueOf(inputMap.get("ntestid").toString());
		final String ssampleid = inputMap.get("ssampleid").toString();
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select * from  wqmislabsampleparameters where ntestid=" + ntestid + " and ssampleid='"
				+ ssampleid + "'";
		final List<WQMISLabSampleParameters> lstLabSampleParameters = (List<WQMISLabSampleParameters>) jdbcTemplate
				.query(strQuery, new WQMISLabSampleParameters());
		outputMap.put("SampleParameterDetails", lstLabSampleParameters);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}// end

	// WQMISTransactionAPI BY DHIVYABHARATHI
	public ResponseEntity<Object> getWQMISFTKSamples(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo, Map<String, Object> inputMap) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		if (currentUIDate != null && currentUIDate.trim().length() != 0) {

			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					currentUIDate, "datetime", "FromDate");

			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");

			outputMap.put("FromDate", (String) mapObject.get("FromDateWOUTC"));
			outputMap.put("ToDate", (String) mapObject.get("ToDateWOUTC"));

		} else {

			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);

			outputMap.put("FromDate", fromDateUI);
			outputMap.put("ToDate", toDateUI);

			fromDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
			toDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		}
		String conditionValue = "";
		if (inputMap.containsKey("ndistrictid") && inputMap.get("ndistrictid") != null
				&& (int) inputMap.get("ndistrictid") != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			conditionValue = conditionValue + " and ndistrictid=" + (int) inputMap.get("ndistrictid") + " ";
		}
		if (inputMap.containsKey("nblockid") && inputMap.get("nblockid") != null
				&& (int) inputMap.get("nblockid") != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			conditionValue = conditionValue + " and nblockid=" + (int) inputMap.get("nblockid") + " ";
		}
		if (inputMap.containsKey("npanchayatid") && inputMap.get("npanchayatid") != null
				&& (int) inputMap.get("npanchayatid") != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			conditionValue = conditionValue + " and npanchayatid=" + (int) inputMap.get("npanchayatid") + " ";
		}
		if (inputMap.containsKey("nvillageid") && inputMap.get("nvillageid") != null
				&& (int) inputMap.get("nvillageid") != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			conditionValue = conditionValue + " and nvillageid=" + (int) inputMap.get("nvillageid") + " ";
		}
		LocalDateTime fromDateTime = LocalDateTime.parse(fromDate);
		int fromYear = fromDateTime.getYear();
		LocalDateTime toDateTime = LocalDateTime.parse(toDate);
		int toYear = toDateTime.getYear();
		final String strQuery = "SELECT row_number() OVER () AS npkid, " + " COALESCE(TO_CHAR(dtesteddate, '"
				+ userInfo.getSpgsitedatetime() + "'), '') AS stesteddate, " + " * " + " FROM wqmisftksamples "
				+ " WHERE dtesteddate BETWEEN '" + fromDate + "' AND '" + toDate + "' " + " and nyear  between "
				+ fromYear + " and " + toYear + " " + conditionValue;
		final List<WQMISFTKSamples> lstftkSamples = (List<WQMISFTKSamples>) jdbcTemplate.query(strQuery,
				new WQMISFTKSamples());

		final String strquery = "select jd.sdistrictname ,jd.ndistrictid from jjmdistrictlist jd where "
				+ " jd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<JjmDistrictList> lstDistrictList = (List<JjmDistrictList>) jdbcTemplate.query(strquery,
				new JjmDistrictList());
		outputMap.put("DistrictList", lstDistrictList);
		outputMap.put("FtkSamples", lstftkSamples);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	// WQMISTransactionAPI BY DHIVYABHARATHI
	@Override
	public ResponseEntity<Object> getWQMISFTKSampleParametersDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final int ntestid = Integer.valueOf(inputMap.get("ntestid").toString());
		final String ssampleid = inputMap.get("ssampleid").toString();
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select * from  wqmisftksampleparameters where ntestid=" + ntestid + " and ssampleid='"
				+ ssampleid + "'";
		final List<WQMISFTKSampleParameters> lstFtkSampleParameters = (List<WQMISFTKSampleParameters>) jdbcTemplate
				.query(strQuery, new WQMISFTKSampleParameters());
		outputMap.put("FtkSampleParametersDetails", lstFtkSampleParameters);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	// added by sujatha ATE_274 09-10-2025 SWSM-85 for getting Master API in
	// JJMWQMISMasterApi screen
	@Override
	public ResponseEntity<Object> getJJMWQMISMasterApi(final Map<String, Object> inputMap) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final ObjectMapper objectMapper = new ObjectMapper();
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		String strQuery = "select w.*, COALESCE((to_char(w.dsyncstartdate, '" + userInfo.getSpgsitedatetime()
				+ "')), '-') as ssyncstartdate, " + "  COALESCE((to_char(w.dsyncenddate, '"
				+ userInfo.getSpgsitedatetime() + "')), '-') as ssyncenddate "
				+ " from wqmisapi w where w.nismastertable=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and w.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nwqmisapicode desc";
		final var jjmwqmisList = jdbcTemplate.query(strQuery, new WQMISApi());
		outputMap.put("JjmWqmisMasterList", jjmwqmisList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

//	// added by MullaiBalaji
//	@Override
//	public ResponseEntity<Object> getLabDataList(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
//		Instant startDate = dateUtilityFunction.getCurrentDateTime(userInfo);
//		RestTemplate restTemplate = new RestTemplate();
//		String url = inputMap.get("surl") + "?StateId=" + 18 + "";
//		String authToken = inputMap.get("stokenid") + "";
//		int nwqmisapicode = (int) inputMap.get("sprimarykey");
//		HttpHeaders headers = new HttpHeaders();
//		headers.add("Authorization", "Bearer " + authToken);
//		HttpEntity<String> request = new HttpEntity<>(headers);
//		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
//		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
//			ObjectMapper objectMapper = new ObjectMapper();
//			String jsonString = response.getBody();
//			LabDataList objLabDataList = objectMapper.readValue(jsonString, LabDataList.class);
//			if (objLabDataList.getData_result() != null && !objLabDataList.getData_result().isEmpty()) {
//				objLabDataList.getData_result().forEach(ld -> {
//					jdbcTemplate.update(
//							"INSERT INTO jjmlabdatalist (slabid,slabname,slabtype,slabgroup,nlatitude,nlongitude,nstateid,nstatus) "
//									+ "VALUES (?,?,?,?,?,?,?,?) ON CONFLICT (slabid) DO NOTHING",
//							ld.getLab_id(), ld.getLab_name(), ld.getLab_type(), ld.getLab_group(), ld.getLatitude(),
//							ld.getLongitude(), 18, Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
//				});
//				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
//						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
//						userInfo.getSlanguagefilename()), HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server
//			} else {
//				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
//						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODATATOSYNCFROMSERVER",
//						userInfo.getSlanguagefilename()), HttpStatus.NO_CONTENT);// 204 No Data to Synchronize from
//																					// WQMIS API
//			}
//		} else {
//			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
//					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
//			return new ResponseEntity<>(
//					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
//					HttpStatus.INTERNAL_SERVER_ERROR); // 500 Try to Synchronize the Data from WQMIS API Server Later
//		}
//	}

	// added by MullaiBalaji
	@Override
	public ResponseEntity<Object> getParameterDataList(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Instant startDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		RestTemplate restTemplate = new RestTemplate();
		String url = inputMap.get("surl") + "";
		String authToken = inputMap.get("stokenid") + "";
		int nwqmisapicode = (int) inputMap.get("sprimarykey");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = response.getBody();
			ParameterDataList objParameterDataList = objectMapper.readValue(jsonString, ParameterDataList.class);
			if (objParameterDataList.getData_result() != null && !objParameterDataList.getData_result().isEmpty()) {
				objParameterDataList.getData_result().forEach(pd -> {
					jdbcTemplate.update(
							"INSERT INTO jjmparameterdatalist (sparameterid,sparametername,smeasurementunit,sacceptablelimit,"
									+ "spermissiblelimit,svaluetype,svaluetypedescription,npublicrate,ndepartmentrate,ncomercialrate,stestparametertype,nstatus) "
									+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (sparameterid) DO NOTHING",
							pd.getParameter_id(), pd.getParameter_name(), pd.getMeasurementUnit(),
							pd.getAcceptablelimit(), pd.getPermissiblelimit(), pd.getValue_type(),
							pd.getValue_type_Description(), pd.getPublic_Rate(), pd.getDepartment_Rate(),
							pd.getComercial_Rate(), pd.getTestParameterType(),
							Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				});
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server
			} else {
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODATATOSYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.NO_CONTENT);// 204 No Data to Synchronize from
																					// WQMIS API
				// Server
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR);// 500 Try to Synchronize the Data from WQMIS API Server Later
		}
	}

	// added by MullaiBalaji
	@Override
	public ResponseEntity<Object> getParametersDataFTKUserList(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Instant startDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		RestTemplate restTemplate = new RestTemplate();
		String url = inputMap.get("surl") + "";
		String authToken = inputMap.get("stokenid") + "";
		int nwqmisapicode = (int) inputMap.get("sprimarykey");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = response.getBody();
			ParametersDataFTKUserList objParametersDataFTKUserList = objectMapper.readValue(jsonString,
					ParametersDataFTKUserList.class);
			if (objParametersDataFTKUserList.getData_result() != null
					&& !objParametersDataFTKUserList.getData_result().isEmpty()) {
				objParametersDataFTKUserList.getData_result().forEach(pd -> {
					jdbcTemplate.update(
							"INSERT INTO jjmparametersdataftkuserlist (sparameterid,sparametername,smeasurementunit,sacceptablelimit,spermissiblelimit,svaluetype,svaluetypedescription,nstatus) "
									+ "VALUES (?,?,?,?,?,?,?,?) ON CONFLICT (sparameterid) DO NOTHING",
							pd.getParameter_id(), pd.getParameter_name(), pd.getMeasurementUnit(),
							pd.getAcceptablelimit(), pd.getPermissiblelimit(), pd.getValue_type(),
							pd.getValue_type_Description(),
							Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				});
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server
			} else {
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODATATOSYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.NO_CONTENT);// 204 No Data to Synchronize from
																					// WQMIS API Server
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR);// 500 Try to Synchronize the Data from WQMIS API Server Later
		}

	}

	@Override
	public ResponseEntity<Object> getDistrictList(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Instant syncstart = dateUtilityFunction.getCurrentDateTime(userInfo);
		int nwqmisapicode = Integer.parseInt(inputMap.get("sprimarykey").toString());
		RestTemplate restTemplate = new RestTemplate();
		String url = inputMap.get("surl") + "?StateId=" + 18 + "";
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + inputMap.get("stokenid"));
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = response.getBody();
			DistrictListResponse objSampleDataResponse = objectMapper.readValue(jsonString, DistrictListResponse.class);
			if (objSampleDataResponse.getDistrictList() != null && !objSampleDataResponse.getDistrictList().isEmpty()) {
				objSampleDataResponse.getDistrictList().forEach(d -> {
					jdbcTemplate.update(
							"INSERT INTO jjmdistrictlist (ndistrictid, sdistrictname, nstateid, nstatus) "
									+ "VALUES (?, ?, ?, ?) ON CONFLICT (ndistrictid) DO NOTHING",
							d.getDistrictid(), d.getDistrictname(), 18,
							Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				});
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncstart + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server
			} else {
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncstart + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODATATOSYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.NO_CONTENT);// No Data to Synchronize from WQMIS
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncstart + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR);// 500 Try to Synchronize the Data from WQMIS API Server Later
		}
	}

	@Override
	public ResponseEntity<Object> getBlocksList(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Instant syncstart = dateUtilityFunction.getCurrentDateTime(userInfo);
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + inputMap.get("stokenid"));
		HttpEntity<String> request = new HttpEntity<>(headers);

		final String sQuery = "SELECT ndistrictid FROM jjmdistrictlist WHERE nstateid = " + 18 + " AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		final List<Integer> districtids = jdbcTemplate.queryForList(sQuery, Integer.class);
		int nwqmisapicode = Integer.parseInt(inputMap.get("sprimarykey").toString());
		if (districtids.size() > 0) {
			for (int i = 0; i < districtids.size(); i++) {
				final int districtId = districtids.get(i);
				final String url = inputMap.get("surl") + "?Districtid=" + districtids.get(i) + "";
				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
				if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
					ObjectMapper objectMapper = new ObjectMapper();
					String jsonString = response.getBody();
					BlocksListResponse objSampleDataResponse = objectMapper.readValue(jsonString,
							BlocksListResponse.class);
					if (objSampleDataResponse.getBlocksList() != null
							&& !objSampleDataResponse.getBlocksList().isEmpty()) {
						System.out.println(objSampleDataResponse.getBlocksList());
						objSampleDataResponse.getBlocksList().forEach(d -> {
							jdbcTemplate.update(
									"INSERT INTO jjmblocklist (nblockid, ndistrictid, nstateid, sblockname, nstatus) "
											+ "VALUES (?, ?, ?, ?, ?) ON CONFLICT (nblockid) DO NOTHING",
									d.getBlockid(), districtId, 18, d.getBlockName(),
									Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
						});
					}
				}
			}
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncstart + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER", userInfo.getSlanguagefilename()),
					HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncstart + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_DISTRICTDATANOTSYNCED", userInfo.getSlanguagefilename()),
					HttpStatus.OK);// 204 Data Synchronized from WQMIS API Server
		}
	}

	@Override
	public ResponseEntity<Object> getGpList(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Instant syncstart = dateUtilityFunction.getCurrentDateTime(userInfo);
		int nwqmisapicode = Integer.parseInt(inputMap.get("sprimarykey").toString());
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + inputMap.get("stokenid"));
		HttpEntity<String> request = new HttpEntity<>(headers);
		final String dQuery = "SELECT ndistrictid FROM jjmdistrictlist WHERE nstateid = 18 " + "AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		final List<Integer> districtIds = jdbcTemplate.queryForList(dQuery, Integer.class);
		final Map<Integer, Integer> blockDistrictMap = new HashMap<>();
		if (!districtIds.isEmpty()) {
			for (int districtId : districtIds) {
				final String bQuery = "SELECT nblockid, ndistrictid FROM jjmblocklist WHERE ndistrictid = " + districtId
						+ " AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.query(bQuery, (rs) -> {
					blockDistrictMap.put(rs.getInt("nblockid"), rs.getInt("ndistrictid"));
				});
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncstart + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_DISTRICTDATANOTSYNCED", userInfo.getSlanguagefilename()),
					HttpStatus.OK);
		}
		if (!blockDistrictMap.isEmpty()) {
			for (Map.Entry<Integer, Integer> entry : blockDistrictMap.entrySet()) {
				final int blockId = entry.getKey();
				final int districtId = entry.getValue();
				final String url = inputMap.get("surl") + "?Blockid=" + blockId;
				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
				if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
					ObjectMapper objectMapper = new ObjectMapper();
					String jsonString = response.getBody();
					GpListResponse objSampleDataResponse = objectMapper.readValue(jsonString, GpListResponse.class);
					if (objSampleDataResponse.getGpList() != null && !objSampleDataResponse.getGpList().isEmpty()) {
						System.out.println(objSampleDataResponse.getGpList());
						objSampleDataResponse.getGpList().forEach(d -> {
							jdbcTemplate.update(
									"INSERT INTO jjmgplist (npanchayatid, spanchayatname, nstatus, nblockid, ndistrictid, nstateid) "
											+ "VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (npanchayatid) DO NOTHING",
									d.getPanchayatId(), d.getGrampanchayatName(),
									Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), blockId, districtId,
									18);
						});
					}
				}
			}
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncstart + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER", userInfo.getSlanguagefilename()),
					HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server
		}
		jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncstart + "',dsyncenddate = '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
		return new ResponseEntity<>(
				commonFunction.getMultilingualMessage("IDS_BLOCKDATANOTSYNCED", userInfo.getSlanguagefilename()),
				HttpStatus.OK);// 204 Data Synchronized from WQMIS API Server
	}

	@Override
	public ResponseEntity<Object> getVillageList(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Instant syncstart = dateUtilityFunction.getCurrentDateTime(userInfo);
		int nwqmisapicode = Integer.parseInt(inputMap.get("sprimarykey").toString());
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + inputMap.get("stokenid"));
		HttpEntity<String> request = new HttpEntity<>(headers);
		final String dQuery = "SELECT ndistrictid FROM jjmdistrictlist WHERE nstateid = " + 18 + " AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		final List<Integer> districtids = jdbcTemplate.queryForList(dQuery, Integer.class);
		final Map<Integer, Integer> blockDistrictMap = new HashMap<>();
		final Map<Integer, Integer> gpBlockMap = new HashMap<>();
		if (!districtids.isEmpty()) {
			for (int districtId : districtids) {
				final String bQuery = "SELECT nblockid, ndistrictid FROM jjmblocklist WHERE ndistrictid = " + districtId
						+ " AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				jdbcTemplate.query(bQuery, (rs) -> {
					blockDistrictMap.put(rs.getInt("nblockid"), rs.getInt("ndistrictid"));
				});
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncstart + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_DISTRICTDATANOTSYNCED", userInfo.getSlanguagefilename()),
					HttpStatus.OK);// 204 Data Synchronized from WQMIS API Server
		}

		final List<Integer> gPanchayatids = new ArrayList<Integer>();
		if (!blockDistrictMap.isEmpty()) {
			for (int blockId : blockDistrictMap.keySet()) {
				final String gQuery = "SELECT npanchayatid FROM jjmgplist WHERE nblockid = " + blockId
						+ " AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				jdbcTemplate.query(gQuery, (rs) -> {
					gPanchayatids.add(rs.getInt("npanchayatid"));
					gpBlockMap.put(rs.getInt("npanchayatid"), blockId);
				});
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncstart + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_BLOCKDATANOTSYNCED", userInfo.getSlanguagefilename()),
					HttpStatus.OK);// 204 Data Synchronized from WQMIS API Server
		}
		if (!gPanchayatids.isEmpty()) {
			for (int gpId : gPanchayatids) {
				final String url = inputMap.get("surl") + "?PanchayatId=" + gpId;
				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
				if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
					ObjectMapper objectMapper = new ObjectMapper();
					VillageListResponse objSampleDataResponse = objectMapper.readValue(response.getBody(),
							VillageListResponse.class);
					if (objSampleDataResponse.getVillageList() != null
							&& !objSampleDataResponse.getVillageList().isEmpty()) {
						final int blockId = gpBlockMap.get(gpId);
						final int districtId = blockDistrictMap.get(blockId);
						System.out.println(objSampleDataResponse.getVillageList());
						objSampleDataResponse.getVillageList().forEach(v -> {
							jdbcTemplate.update(
									"INSERT INTO jjmvillagelist (nvillageid, npanchayatid, svillagename, nstateid, ndistrictid, nblockid, nstatus) "
											+ "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (nvillageid) DO NOTHING",
									v.getVillageId(), gpId, v.getVillageName(), 18, districtId, blockId,
									Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
						});
					}
				}
			}
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncstart + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER", userInfo.getSlanguagefilename()),
					HttpStatus.OK);
		}
		jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncstart + "',dsyncenddate = '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
		return new ResponseEntity<>(
				commonFunction.getMultilingualMessage("IDS_GPDATANOTSYNCED", userInfo.getSlanguagefilename()),
				HttpStatus.OK);// 204 Data Synchronized from WQMIS API Server
	}

	public ResponseEntity<Object> getFTKSamples(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		Instant syncStartDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		String url = inputMap.get("surl") + "?StateId=18&VillageId=" + inputMap.get("nvillageid") + "";
		String authToken = (String) inputMap.get("stokenid");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = response.getBody();
			WQMISFTKSampleDataResponse objFTKSampleDataResponse = objectMapper.readValue(jsonString,
					WQMISFTKSampleDataResponse.class);
			String str = "select * from jjmvillagelist where nvillageid=" + inputMap.get("nvillageid") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			JjmVillageList objJjmVillageList = (JjmVillageList) jdbcUtilityFunction.queryForObject(str,
					JjmVillageList.class, jdbcTemplate);
			if (objFTKSampleDataResponse != null && objJjmVillageList != null) {
				for (WQMISFTKSampleResults res : objFTKSampleDataResponse.getResult()) {
					if (res instanceof WQMISFTKSampleResults) {
						if (res != null && res.getParameterResult() != null && !res.getParameterResult().isEmpty()) {
							Date testDate = res.test_date;
							SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
							String formattedTestDate = dateFormatter.format(testDate);

							Calendar cal = Calendar.getInstance();
							cal.setTime(testDate);
							int testYear = cal.get(Calendar.YEAR);
							jdbcTemplate.update("INSERT INTO public.wqmisftksamples( "
									+ "ntestid, ssampleid, dtesteddate, slocation, sissafe,nsourceid, ssourcetype, "
									+ "ssourcesubtype, sschemeid, nstateid, ndistrictid,"
									+ "nblockid, npanchayatid,nvillageid,nhabitationid, nstatelgdcode, ndistrictlgdcode, nblocklgdcode,"
									+ " npanchayatlgdcode, nvillagelgdcode,nyear) " + "VALUES(" + res.testId + ",N'"
									+ stringUtilityFunction.replaceQuote(res.sampleId) + "', '" + formattedTestDate
									+ "', " + " N'" + stringUtilityFunction.replaceQuote(res.location) + "',N'"
									+ stringUtilityFunction.replaceQuote(res.is_safe) + "'," + " " + res.sourceId
									+ ",N'" + stringUtilityFunction.replaceQuote(res.source_type) + "', N'"
									+ stringUtilityFunction.replaceQuote(res.source_subtype) + "'," + " N'"
									+ stringUtilityFunction.replaceQuote(res.schemeId) + "',"
									+ objJjmVillageList.getNstateid() + "," + objJjmVillageList.getNdistrictid() + ","
									+ objJjmVillageList.getNblockid() + "," + " " + objJjmVillageList.getNpanchayatid()
									+ "," + res.villageId + ", " + res.habitationId + ", " + res.stateLGD_code + ","
									+ " " + res.districtLGD_Code + ", " + res.blockLGD_Code + ", "
									+ res.panchyatLGD_Code + ", " + res.villageLGD_Code + "," + testYear + ")"
									+ "ON CONFLICT (ntestid, ssampleid) DO NOTHING");
							res.getParameterResult().forEach(d -> {
								jdbcTemplate.update(
										"INSERT INTO wqmisftksampleparameters (ntestid, ssampleid,nparameterid, sparametervalue) "
												+ "VALUES (?, ?, ?, ?) ON CONFLICT (ntestid,ssampleid,nparameterid) DO NOTHING",
										d.getTestId(), res.sampleId, d.getParameterId(),
										stringUtilityFunction.replaceQuote(d.getParameterValue()));
							});
						}
					}
				}
				jdbcTemplate.update("update wqmisapi set dsyncstartdate='" + syncStartDate + "',dsyncenddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nwqmisapicode="
						+ inputMap.get("nwqmisapicode") + "");
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server
			} else {
				jdbcTemplate.update("update wqmisapi set dsyncstartdate='" + syncStartDate + "',dsyncenddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nwqmisapicode="
						+ inputMap.get("nwqmisapicode") + "");
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODATATOSYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.NO_CONTENT);// 204 No Data to Synchronize from
																					// WQMIS API
			}
		} else {
			jdbcTemplate.update("update wqmisapi set dsyncstartdate='" + syncStartDate + "',dsyncenddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nwqmisapicode="
					+ inputMap.get("nwqmisapicode") + "");
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR); // 500 Try to Synchronize the Data from WQMIS API Server Later
		}

	}

	@Override
	public ResponseEntity<Object> getWQMISContaminatedLabSamples(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo, Map<String, Object> inputMap) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					currentUIDate, "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");
			outputMap.put("FromDate", (String) mapObject.get("FromDateWOUTC"));
			outputMap.put("ToDate", (String) mapObject.get("ToDateWOUTC"));
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);
			outputMap.put("FromDate", fromDateUI);
			outputMap.put("ToDate", toDateUI);
			fromDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
			toDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		}
		String conditionValue = "";
		if (inputMap.containsKey("ndistrictid") && inputMap.get("ndistrictid") != null
				&& (int) inputMap.get("ndistrictid") != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			conditionValue = conditionValue + " and ndistrictid=" + (int) inputMap.get("ndistrictid") + " ";
		}
		if (inputMap.containsKey("nblockid") && inputMap.get("nblockid") != null
				&& (int) inputMap.get("nblockid") != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			conditionValue = conditionValue + " and nblockid=" + (int) inputMap.get("nblockid") + " ";
		}
		if (inputMap.containsKey("npanchayatid") && inputMap.get("npanchayatid") != null
				&& (int) inputMap.get("npanchayatid") != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			conditionValue = conditionValue + " and npanchayatid=" + (int) inputMap.get("npanchayatid") + " ";
		}
		if (inputMap.containsKey("nvillageid") && inputMap.get("nvillageid") != null
				&& (int) inputMap.get("nvillageid") != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			conditionValue = conditionValue + " and nvillageid=" + (int) inputMap.get("nvillageid") + " ";
		}
		LocalDateTime fromDateTime = LocalDateTime.parse(fromDate);
		int fromYear = fromDateTime.getYear();
		LocalDateTime toDateTime = LocalDateTime.parse(toDate);
		int toYear = toDateTime.getYear();
		final String strQuery = "select row_number() OVER () AS npkid ,COALESCE(TO_CHAR(dtesteddate, '"
				+ userInfo.getSpgsitedatetime() + "'), '') as stesteddate,* from"
				+ " wqmiscontaminatedlabsamples  where " + " dtesteddate between '" + fromDate + "' and '" + toDate
				+ "' " + " and nyear  between " + fromYear + " and " + toYear + " " + conditionValue;
		final List<WQMISContaminatedLabSamples> lstContaminatedLabSamples = (List<WQMISContaminatedLabSamples>) jdbcTemplate
				.query(strQuery, new WQMISContaminatedLabSamples());

		final String strquery = "select jd.sdistrictname ,jd.ndistrictid from jjmdistrictlist jd where "
				+ " jd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<JjmDistrictList> lstDistrictList = (List<JjmDistrictList>) jdbcTemplate.query(strquery,
				new JjmDistrictList());
		outputMap.put("DistrictList", lstDistrictList);
		outputMap.put("ContaminatedLabSamples", lstContaminatedLabSamples);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getWQMISContaminatedLabSampleParametersDetails(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		final int ntestid = Integer.valueOf(inputMap.get("ntestid").toString());
		final String ssampleid = inputMap.get("ssampleid").toString();
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select * from  wqmiscontaminatedlabsampleparameters where ntestid=" + ntestid
				+ " and ssampleid='" + ssampleid + "'";
		final List<WQMISContaminatedLabSampleParameters> lstContaminatedLabSampleParameters = (List<WQMISContaminatedLabSampleParameters>) jdbcTemplate
				.query(strQuery, new WQMISContaminatedLabSampleParameters());
		outputMap.put("ContaminatedLabSampleParametersDetails", lstContaminatedLabSampleParameters);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getWQMISContaminatedFTKSamples(String fromDate, String toDate, String currentUIDate,
			UserInfo userInfo, Map<String, Object> inputMap) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			final Map<String, Object> mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					currentUIDate, "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");
			outputMap.put("FromDate", (String) mapObject.get("FromDateWOUTC"));
			outputMap.put("ToDate", (String) mapObject.get("ToDateWOUTC"));
		} else {
			final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());
			final String fromDateUI = LocalDateTime.parse(fromDate, dbPattern).format(uiPattern);
			final String toDateUI = LocalDateTime.parse(toDate, dbPattern).format(uiPattern);
			outputMap.put("FromDate", fromDateUI);
			outputMap.put("ToDate", toDateUI);
			fromDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(fromDate, userInfo, true));
			toDate = dateUtilityFunction
					.instantDateToString(dateUtilityFunction.convertStringDateToUTC(toDate, userInfo, true));
		}
		String conditionValue = "";
		if (inputMap.containsKey("ndistrictid") && inputMap.get("ndistrictid") != null
				&& (int) inputMap.get("ndistrictid") != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			conditionValue = conditionValue + " and ndistrictid=" + (int) inputMap.get("ndistrictid") + " ";
		}
		if (inputMap.containsKey("nblockid") && inputMap.get("nblockid") != null
				&& (int) inputMap.get("nblockid") != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			conditionValue = conditionValue + " and nblockid=" + (int) inputMap.get("nblockid") + " ";
		}
		if (inputMap.containsKey("npanchayatid") && inputMap.get("npanchayatid") != null
				&& (int) inputMap.get("npanchayatid") != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			conditionValue = conditionValue + " and npanchayatid=" + (int) inputMap.get("npanchayatid") + " ";
		}
		if (inputMap.containsKey("nvillageid") && inputMap.get("nvillageid") != null
				&& (int) inputMap.get("nvillageid") != Enumeration.TransactionStatus.NA.gettransactionstatus()) {
			conditionValue = conditionValue + " and nvillageid=" + (int) inputMap.get("nvillageid") + " ";
		}
		LocalDateTime fromDateTime = LocalDateTime.parse(fromDate);
		int fromYear = fromDateTime.getYear();
		LocalDateTime toDateTime = LocalDateTime.parse(toDate);
		int toYear = toDateTime.getYear();
		final String strQuery = "select row_number() OVER () AS npkid ,COALESCE(TO_CHAR(dtesteddate, '"
				+ userInfo.getSpgsitedatetime() + "'), '') as stesteddate,* from"
				+ " wqmiscontaminatedftksamples  where " + " dtesteddate between '" + fromDate + "' and '" + toDate
				+ "' " + " and nyear  between " + fromYear + " and " + toYear + " " + conditionValue;
		final List<WQMISContaminatedFTKSamples> lstContaminatedFTKSamples = (List<WQMISContaminatedFTKSamples>) jdbcTemplate
				.query(strQuery, new WQMISContaminatedFTKSamples());

		final String strquery = "select jd.sdistrictname ,jd.ndistrictid from jjmdistrictlist jd where "
				+ " jd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<JjmDistrictList> lstDistrictList = (List<JjmDistrictList>) jdbcTemplate.query(strquery,
				new JjmDistrictList());
		outputMap.put("DistrictList", lstDistrictList);
		outputMap.put("ContaminatedFTKSamples", lstContaminatedFTKSamples);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getWQMISContaminatedFTKSampleParametersDetails(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		final int ntestid = Integer.valueOf(inputMap.get("ntestid").toString());
		final String ssampleid = inputMap.get("ssampleid").toString();
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select * from  wqmiscontaminatedftksampleparameters where ntestid=" + ntestid
				+ " and ssampleid='" + ssampleid + "'";
		final List<WQMISContaminatedFTKSampleParameters> lstContaminatedFTKSampleParameters = (List<WQMISContaminatedFTKSampleParameters>) jdbcTemplate
				.query(strQuery, new WQMISContaminatedFTKSampleParameters());
		outputMap.put("ContaminatedFTKSampleParametersDetails", lstContaminatedFTKSampleParameters);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> syncContaminatedLabSamples(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		String url = inputMap.get("surl") + "?StateId=18&VillageId=" + inputMap.get("nvillageid") + "";
		String authToken = (String) inputMap.get("stokenid");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		Instant syncStartDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = response.getBody();
			WQMISContaminatedLabSampleResponse objSampleResponse = objectMapper.readValue(jsonString,
					WQMISContaminatedLabSampleResponse.class);
			String str = "select * from jjmvillagelist where nvillageid=" + inputMap.get("nvillageid") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			JjmVillageList objJjmVillageList = (JjmVillageList) jdbcUtilityFunction.queryForObject(str,
					JjmVillageList.class, jdbcTemplate);
			if (objSampleResponse != null && objJjmVillageList != null) {
				for (WQMISContaminatedLabSampleResults res : objSampleResponse.getSampleResult()) {
					if (res instanceof WQMISContaminatedLabSampleResults) {
						if (res != null && res.getParameterResult() != null && !res.getParameterResult().isEmpty()
								&& objJjmVillageList != null) {
							Date testDate = res.getDtesteddate();
							String formattedTestDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(testDate);
							Calendar cal = Calendar.getInstance();
							cal.setTime(testDate);
							int testYear = cal.get(Calendar.YEAR);
							jdbcTemplate.update("INSERT INTO public.wqmiscontaminatedlabsamples( "
									+ "ntestid, ssampleid, dtesteddate, slocation, sissafe, nlabid, nsourceid, ssourcetype, "
									+ "ssourcesubtype, sschemeid, ssamplelocationfrom, sremedialactionstatus,nstateid,ndistrictid,"
									+ "nblockid, npanchayatid,nvillageid,nhabitationid, nstatelgdcode, ndistrictlgdcode, nblocklgdcode,"
									+ " npanchayatlgdcode, nvillagelgdcode,nyear) " + "VALUES(" + res.getNtestid()
									+ ", '" + res.getSsampleid() + "', '" + formattedTestDate + "', " + " '"
									+ res.getSlocation() + "', '" + res.getSissafe() + "', " + res.getNlabid() + ", "
									+ res.getNsourceid() + ", '" + res.getSsourcetype() + "', '"
									+ res.getSsourcesubtype() + "'," + " '" + res.getSschemeid() + "', '"
									+ res.getSsamplelocationfrom() + "', " + " '" + res.getSremedialactionstatus()
									+ "'," + objJjmVillageList.getNstateid() + ", " + objJjmVillageList.getNdistrictid()
									+ "," + objJjmVillageList.getNblockid() + "," + " "
									+ objJjmVillageList.getNpanchayatid() + "," + inputMap.get("nvillageid") + ", "
									+ res.getNhabitationid() + ", " + res.getNstatelgdcode() + "," + " "
									+ res.getNdistrictlgdcode() + ", " + res.getNblocklgdcode() + ", "
									+ res.getNpanchayatlgdcode() + ", " + res.getNvillagelgdcode() + "," + testYear
									+ ")" + "ON CONFLICT (ntestid, ssampleid) DO NOTHING");
							res.getParameterResult().forEach(d -> {
								jdbcTemplate.update(
										"INSERT INTO wqmiscontaminatedlabsampleparameters (ntestid, ssampleid,nparameterid, sparametervalue) "
												+ "VALUES (?, ?, ?, ?) ON CONFLICT (ntestid,ssampleid,nparameterid) DO NOTHING",
										d.getNtestid(), res.getSsampleid(), d.getNparameterid(),
										d.getSparametervalue());
							});
						}
					}
				}
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncStartDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode="
						+ inputMap.get("nwqmisapicode") + "");
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);
			} else {
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncStartDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode="
						+ inputMap.get("nwqmisapicode") + "");
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncStartDate + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode="
					+ inputMap.get("nwqmisapicode") + "");
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<Object> syncContaminatedFTKSamples(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		String url = inputMap.get("surl") + "?StateId=18&VillageId=" + inputMap.get("nvillageid") + "";
		String authToken = (String) inputMap.get("stokenid");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		Instant syncStartDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = response.getBody();
			WQMISContaminatedFTKSampleResponse objSampleResponse = objectMapper.readValue(jsonString,
					WQMISContaminatedFTKSampleResponse.class);
			String str = "select * from jjmvillagelist where nvillageid=" + inputMap.get("nvillageid") + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";
			JjmVillageList objJjmVillageList = (JjmVillageList) jdbcUtilityFunction.queryForObject(str,
					JjmVillageList.class, jdbcTemplate);
			if (objSampleResponse != null && objJjmVillageList != null) {
				for (WQMISContaminatedFTKSampleResults res : objSampleResponse.getSampleResult()) {
					if (res instanceof WQMISContaminatedFTKSampleResults) {
						if (res != null && objJjmVillageList != null) {
							Date testDate = res.getDtesteddate();
							String formattedTestDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(testDate);
							Calendar cal = Calendar.getInstance();
							cal.setTime(testDate);
							int testYear = cal.get(Calendar.YEAR);
							jdbcTemplate.update("INSERT INTO public.wqmiscontaminatedftksamples( "
									+ "ntestid, ssampleid, dtesteddate, slocation, sissafe, nlabid, nsourceid, ssourcetype, "
									+ "ssourcesubtype, sschemeid, ssamplelocationfrom, sremedialactionstatus,nstateid,ndistrictid,"
									+ "nblockid, npanchayatid,nvillageid,nhabitationid, nstatelgdcode, ndistrictlgdcode, nblocklgdcode,"
									+ " npanchayatlgdcode, nvillagelgdcode,nyear) " + "VALUES(" + res.getNtestid()
									+ ", '" + res.getSsampleid() + "', '" + formattedTestDate + "', " + " '"
									+ res.getSlocation() + "', '" + res.getSissafe() + "', " + res.getNlabid() + ", "
									+ res.getNsourceid() + ", '" + res.getSsourcetype() + "', '"
									+ res.getSsourcesubtype() + "'," + " '" + res.getSschemeid() + "', '"
									+ res.getSsamplelocationfrom() + "', " + " '" + res.getSremedialactionstatus()
									+ "'," + objJjmVillageList.getNstateid() + ", " + objJjmVillageList.getNdistrictid()
									+ "," + objJjmVillageList.getNblockid() + "," + " "
									+ objJjmVillageList.getNpanchayatid() + "," + inputMap.get("nvillageid") + ", "
									+ res.getNhabitationid() + ", " + res.getNblocklgdcode() + "," + " "
									+ res.getNdistrictlgdcode() + ", " + res.getNblocklgdcode() + ", "
									+ res.getNpanchayatlgdcode() + ", " + res.getNvillagelgdcode() + "," + testYear
									+ ")" + "ON CONFLICT (ntestid, ssampleid) DO NOTHING");
							if (res.getParameterResult() != null && !res.getParameterResult().isEmpty()) {
								res.getParameterResult().forEach(d -> {
									jdbcTemplate.update(
											"INSERT INTO wqmiscontaminatedftksampleparameters (ntestid, ssampleid,nparameterid, sparametervalue) "
													+ "VALUES (?, ?, ?, ?) ON CONFLICT (ntestid,ssampleid,nparameterid) DO NOTHING",
											d.getNtestid(), res.getSsampleid(), d.getNparameterid(),
											d.getSparametervalue());
								});
							}
						}
					}
				}
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncStartDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode="
						+ inputMap.get("nwqmisapicode") + "");
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);
			} else {
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncStartDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode="
						+ inputMap.get("nwqmisapicode") + "");
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncStartDate + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode="
					+ inputMap.get("nwqmisapicode") + "");
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
   
	//ate135 committed by Dhanalakshmi on 21-11-2025 for Getlabdata
			//SWSM-122 WQMIS Branch creation for inetgartion
	@Override
	public ResponseEntity<Object> getLabData(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Instant startDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		RestTemplate restTemplate = new RestTemplate();
		String url = inputMap.get("surl") + "?StateId=" + 18 + "";
		String authToken = inputMap.get("stokenid") + "";
		int nwqmisapicode = (int) inputMap.get("sprimarykey");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = response.getBody();
			WqmisLabDataResponse objjmlabdata = objectMapper.readValue(jsonString, WqmisLabDataResponse.class);
			if (objjmlabdata.getData_result() != null && !objjmlabdata.getData_result().isEmpty()) {
				objjmlabdata.getData_result().forEach(ld -> {
					jdbcTemplate.update(
							"INSERT INTO jjmlabdata (lab_id,lab_name,lab_type,lab_group,latitude,longitude,nflagstatus,nstatus) "
									+ " VALUES (?,?,?,?,?,?,?,?) ON CONFLICT (lab_id) DO NOTHING",
							ld.getLab_id(), ld.getLab_name(), ld.getLab_type(), ld.getLab_group(), ld.getLatitude(),
							ld.getLongitude(), 0, Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				});
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server

			} else {
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODATATOSYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.NO_CONTENT);// 204 No Data to Synchronize from
																					// WQMIS API
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR); // 500 Try to Synchronize the Data from WQMIS API Server Later
		}
	}
	//ate135 committed by Dhanalakshmi on 21-11-2025 for getEquipments_data
	//SWSM-122 WQMIS Branch creation for inetgartion
	@Override
	public ResponseEntity<Object> getEquipments_data(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Instant startDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		RestTemplate restTemplate = new RestTemplate();
		String url = inputMap.get("surl") + "";
		String authToken = inputMap.get("stokenid") + "";
		int nwqmisapicode = (int) inputMap.get("sprimarykey");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = response.getBody();
			WqmisEquipmentDataResponse objjmequipmentdata = objectMapper.readValue(jsonString,
					WqmisEquipmentDataResponse.class);
			if (objjmequipmentdata.getData_result() != null && !objjmequipmentdata.getData_result().isEmpty()) {
				objjmequipmentdata.getData_result().forEach(ed -> {
					jdbcTemplate.update(
							"INSERT INTO jjmequipmentsdata (e_id,equipment_name,nflagstatus,nstatus) "
									+ " VALUES (?,?,?,?) ON CONFLICT (e_id) DO NOTHING",
							ed.getE_id(), ed.getEquipment_name(), 0,
							Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				});
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server

			} else {
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODATATOSYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.NO_CONTENT);// 204 No Data to Synchronize from
																					// WQMIS API
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR); // 500 Try to Synchronize the Data from WQMIS API Server Later
		}
	}
	//ate135 committed by Dhanalakshmi on 21-11-2025 for GetSample_submitter
	//SWSM-122 WQMIS Branch creation for inetgartion
	@Override
	public ResponseEntity<Object> GetSample_submitter(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Instant startDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		RestTemplate restTemplate = new RestTemplate();
		String url = inputMap.get("surl") + "";
		String authToken = inputMap.get("stokenid") + "";
		int nwqmisapicode = (int) inputMap.get("sprimarykey");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = response.getBody();
			WqmisSamplesubmitterResponse objjmsamplesubmitter = objectMapper.readValue(jsonString,
					WqmisSamplesubmitterResponse.class);
			if (objjmsamplesubmitter.getData_result() != null && !objjmsamplesubmitter.getData_result().isEmpty()) {
				objjmsamplesubmitter.getData_result().forEach(sd -> {

					jdbcTemplate.update("INSERT INTO jjmsamplesubmitter "
							+ "(user_id, first_name, last_name, user_type, mobileno, email, state, "
							+ "district, block, gp, village_town, area, house_no, pin_code, " + "nflagstatus, nstatus) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
							+ "ON CONFLICT (user_id) DO NOTHING", sd.getUser_id(), sd.getFirst_name(),
							sd.getLast_name(), sd.getUser_type(), sd.getMobile(), sd.getEmail(), sd.getState(),
							sd.getDistrict(), sd.getBlock(), sd.getGp(), sd.getVillage_town(), sd.getArea(),
							sd.getHouse_no(), sd.getPin_code(), 0,
							Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

				});

				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server

			} else {
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODATATOSYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.NO_CONTENT);// 204 No Data to Synchronize from
																					// WQMIS API
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR); // 500 Try to Synchronize the Data from WQMIS API Server Later
		}

	}

	//ate135 committed by Dhanalakshmi on 21-11-2025 for getHblist
			//SWSM-122 WQMIS Branch creation for inetgartion
	@Override
	public ResponseEntity<Object> getHblist(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Instant syncstart = dateUtilityFunction.getCurrentDateTime(userInfo);
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + inputMap.get("stokenid"));
		HttpEntity<String> request = new HttpEntity<>(headers);

		final String sQuery = "SELECT nvillageid FROM jjmvillagelist WHERE nstateid = " + 18 + " AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		final List<Integer> villageids = jdbcTemplate.queryForList(sQuery, Integer.class);
		int nwqmisapicode = Integer.parseInt(inputMap.get("sprimarykey").toString());
		if (villageids.size() > 0) {
			for (int i = 0; i < villageids.size(); i++) {
				final String url = inputMap.get("surl") + "?VillageId=" + villageids.get(i) + "";
				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
				if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
					ObjectMapper objectMapper = new ObjectMapper();
					String jsonString = response.getBody();
					WqmisHblistResponse objjmhblist = objectMapper.readValue(jsonString, WqmisHblistResponse.class);
					if (objjmhblist.getHabitationList() != null && !objjmhblist.getHabitationList().isEmpty()) {
						objjmhblist.getHabitationList().forEach(ed -> {
							jdbcTemplate.update(
									"INSERT INTO jjmhblist (habitationid,habitationname,nflagstatus,nstatus) "
											+ " VALUES (?,?,?,?) ON CONFLICT (habitationid) DO NOTHING",
									ed.getHabitationId(), ed.getHabitationName(), 0,
									Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
						});

					}
				}
			}
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncstart + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER", userInfo.getSlanguagefilename()),
					HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + syncstart + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_DISTRICTDATANOTSYNCED", userInfo.getSlanguagefilename()),
					HttpStatus.OK);// 204 Data Synchronized from WQMIS API Server
		}
	}
	//ate199 committed by DhivyaBharathi on 21-11-2025 for getLabOfficial
	//SWSM-122 WQMIS Branch creation for inetgartion
	@Override
	public ResponseEntity<Object> getLabOfficial(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Instant startDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		RestTemplate restTemplate = new RestTemplate();
		String url = inputMap.get("surl") + "";
		String authToken = inputMap.get("stokenid") + "";
		int nwqmisapicode = (int) inputMap.get("sprimarykey");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = response.getBody();
			WqmisLabOfficialDataResponse objLabOfficial = objectMapper.readValue(jsonString,
					WqmisLabOfficialDataResponse.class);
			if (objLabOfficial.getData_result() != null && !objLabOfficial.getData_result().isEmpty()) {
				objLabOfficial.getData_result().forEach(lo -> {

					jdbcTemplate.update("INSERT INTO jjmlabofficial "
							+ " (user_id, lab_id, first_name, last_name, sdesignation, role, mobileno, nflagstatus, nstatus) "
							+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " + " ON CONFLICT (user_id,lab_id) DO NOTHING",
							lo.getUser_id(), lo.getLab_id(), lo.getFirst_name(), lo.getLast_name(), lo.getDesignation(),
							lo.getRole(), lo.getMobile(), 0,
							Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				});

				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server

			} else {
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODATATOSYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.NO_CONTENT);// 204 No Data to Synchronize from
																					// WQMIS API
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR); // 500 Try to Synchronize the Data from WQMIS API Server Later
		}
	}
	//ate199 committed by DhivyaBharathi on 21-11-2025 for GetMeasurement_methods_data
			//SWSM-122 WQMIS Branch creation for inetgartion
	@Override
	public ResponseEntity<Object> GetMeasurement_methods_data(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Instant startDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		RestTemplate restTemplate = new RestTemplate();
		String url = inputMap.get("surl") + "";
		String authToken = inputMap.get("stokenid") + "";
		int nwqmisapicode = (int) inputMap.get("sprimarykey");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = response.getBody();
			MeasurementDataResponse objMeasurementOfficial = objectMapper.readValue(jsonString,
					MeasurementDataResponse.class);

			if (objMeasurementOfficial.getData_result() != null && !objMeasurementOfficial.getData_result().isEmpty()) {

				objMeasurementOfficial.getData_result().forEach(m -> {

					jdbcTemplate.update(
							"INSERT INTO jjmmeasurementmethodsdata " + " (mm_id, mm_name, nflagstatus, nstatus) "
									+ " VALUES (?, ?, ?, ?) " + " ON CONFLICT (mm_id) DO NOTHING",
							m.getMm_id(), m.getMm_name(), 0,
							Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				});

				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server

			} else {
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODATATOSYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.NO_CONTENT);// 204 No Data to Synchronize from
																					// WQMIS API
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR); // 500 Try to Synchronize the Data from WQMIS API Server Later
		}
	}
	//ate225 committed by Mohammed Ashik on 21-11-2025 for GetReagents_data
	//SWSM-122 WQMIS Branch creation for inetgartion
	@Override
	public ResponseEntity<Object> GetReagents_data(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Instant startDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		RestTemplate restTemplate = new RestTemplate();
		String url = inputMap.get("surl") + "?StateId=" + 18 + "";
		String authToken = inputMap.get("stokenid") + "";
		int nwqmisapicode = (int) inputMap.get("sprimarykey");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = response.getBody();
			ReagentsListResponse objjmreagentdata = objectMapper.readValue(jsonString, ReagentsListResponse.class);
			if (objjmreagentdata.getReagentsList() != null && !objjmreagentdata.getReagentsList().isEmpty()) {
				objjmreagentdata.getReagentsList().forEach(ld -> {
					jdbcTemplate.update(
							"INSERT INTO jjmreagentsdata (reagent_id,reagent_name,nflagstatus,nstatus) "
									+ " VALUES (?,?,?,?) ON CONFLICT (reagent_id) DO NOTHING",
							ld.getReagent_id(), ld.getReagent_name(), 0,
							Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				});
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server

			} else {
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODATATOSYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.NO_CONTENT);// 204 No Data to Synchronize from
																					// WQMIS API
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR); // 500 Try to Synchronize the Data from WQMIS API Server Later
		}
	}
	//ate225 committed by Mohammed Ashik on 21-11-2025 for GetSample_Location
	//SWSM-122 WQMIS Branch creation for inetgartion

	@Override
	public ResponseEntity<Object> GetSample_Location(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Instant startDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		RestTemplate restTemplate = new RestTemplate();
		String url = inputMap.get("surl") + "?StateId=" + 18 + "";
		String authToken = inputMap.get("stokenid") + "";
		int nwqmisapicode = (int) inputMap.get("sprimarykey");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = response.getBody();
			SampleLocationListResponse objjmsampledata = objectMapper.readValue(jsonString,
					SampleLocationListResponse.class);
			if (objjmsampledata.getSampleLocationList() != null && !objjmsampledata.getSampleLocationList().isEmpty()) {
				objjmsampledata.getSampleLocationList().forEach(ld -> {
					jdbcTemplate.update(
							"INSERT INTO jjmsamplelocation (typeid,typename,nflagstatus,description,nstatus) "
									+ " VALUES (?,?,?,?,?) ON CONFLICT (typeid) DO NOTHING",
							ld.getTypeid(), ld.getTypename(), 0, ld.getDescription(),
							Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				});
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server

			} else {
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODATATOSYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.NO_CONTENT);// 204 No Data to Synchronize from
																					// WQMIS API
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR); // 500 Try to Synchronize the Data from WQMIS API Server Later
		}
	}
	//ate225 committed by Mohammed Ashik on 21-11-2025 for Getdwsm_state_member_secretary
	//SWSM-122 WQMIS Branch creation for inetgartion
	@Override
	public ResponseEntity<Object> Getdwsm_state_member_secretary(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Instant startDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		RestTemplate restTemplate = new RestTemplate();
		String url = inputMap.get("surl") + "?StateId=" + 18 + "";
		String authToken = inputMap.get("stokenid") + "";
		int nwqmisapicode = (int) inputMap.get("sprimarykey");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = response.getBody();
			StateMemberSecretaryResponse objjmtatemembersecretarydata = objectMapper.readValue(jsonString,
					StateMemberSecretaryResponse.class);
			if (objjmtatemembersecretarydata.getStateMemberSecretaryList() != null
					&& !objjmtatemembersecretarydata.getStateMemberSecretaryList().isEmpty()) {
				objjmtatemembersecretarydata.getStateMemberSecretaryList().forEach(ld -> {
					jdbcTemplate.update(
							"INSERT INTO jjmdwsmstatemembersecretary (user_id,first_name,last_name,mobile,email,state,district,nflagstatus,nstatus) "
									+ " VALUES (?,?,?,?,?,?,?,?,?) ON CONFLICT (user_id) DO NOTHING",
							ld.getUser_id(), ld.getFirst_name(), ld.getLast_name(), ld.getMobile(), ld.getEmail(),
							ld.getState(), ld.getDistrict(), 0,
							Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				});
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server

			} else {
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODATATOSYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.NO_CONTENT);// 204 No Data to Synchronize from
																					// WQMIS API
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR); // 500 Try to Synchronize the Data from WQMIS API Server Later
		}
	}
	//ate199 committed by DhivyaBharathi on 21-11-2025 for getLabOfficial
	//SWSM-122 WQMIS Branch creation for inetgartion
	@Override
	public ResponseEntity<Object> GetWaterSource(Map<String, Object> inputMap, UserInfo userInfo)
			throws DataAccessException, Exception {
		Instant startDate = dateUtilityFunction.getCurrentDateTime(userInfo);

		try {
			RestTemplate restTemplate = new RestTemplate();
			String url = inputMap.get("surl") + "?StateId=18";
			String authToken = String.valueOf(inputMap.get("stokenid"));
			int nwqmisapicode = (int) inputMap.get("sprimarykey");

			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + authToken);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
					String.class);

			if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
				WaterSourceListResponse obj = new ObjectMapper().readValue(response.getBody(),
						WaterSourceListResponse.class);

				if (obj.getResult() != null && !obj.getResult().isEmpty())
					obj.getResult().forEach(ws -> jdbcTemplate.update(
							"INSERT INTO jjmwatersource (stateid,districtid,districtname,blockid,blockname,panchayatid,panchayatname,"
									+ "villageid,villagename,habitationid,habitationname,sourceid,location,sourcetypecategoryid,sourcetypecategory,"
									+ "sourcetypeid,sourcetype,responseon,schemeid,schemename,latitude,longitude,pws_fhtcstatus,nflagstatus,nstatus) "
									+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (stateid,sourceid) DO NOTHING",
							ws.getStateid(), ws.getDistrictid(), ws.getDistrictname(), ws.getBlockid(),
							ws.getBlockname(), ws.getPanchayatid(), ws.getPanchayatname(), ws.getVillageid(),
							ws.getVillagename(), ws.getHabitationid(), ws.getHabitationname(), ws.getSourceid(),
							ws.getLocation(), ws.getSourcetypecategoryid(), ws.getSourcetypecategory(),
							ws.getSourcetypeid(), ws.getSourcetype(), ws.getResponseon(), ws.getSchemeid(),
							ws.getSchemename(), ws.getLatitude(), ws.getLongitude(), ws.getPwsFhtcStatus(), 0,
							Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()));

				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "', dsyncenddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);

				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);
			}

			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "', dsyncenddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);

			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODATATOSYNCFROMSERVER",
					userInfo.getSlanguagefilename()), HttpStatus.NO_CONTENT);

		} catch (Exception ex) {
			int nwqmisapicode = (int) inputMap.get("sprimarykey");
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "', dsyncenddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	//ate225 committed by Mohammed Ashik on 21-11-2025 for GetReagents_data
	//SWSM-122 WQMIS Branch creation for inetgartion
	@Override
	public ResponseEntity<Object> Getstate_level_mapping(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Instant startDate = dateUtilityFunction.getCurrentDateTime(userInfo);
		RestTemplate restTemplate = new RestTemplate();
		String url = inputMap.get("surl") + "?StateId=" + 18 + "";
		String authToken = inputMap.get("stokenid") + "";
		int nwqmisapicode = (int) inputMap.get("sprimarykey");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = response.getBody();
			WQMISStateLevelMappingResponse objjmstatelevelmappingdata = objectMapper.readValue(jsonString,
					WQMISStateLevelMappingResponse.class);
			if (objjmstatelevelmappingdata.getStateLevelMappingList()!= null && !objjmstatelevelmappingdata.getStateLevelMappingList().isEmpty()) {
				objjmstatelevelmappingdata.getStateLevelMappingList().forEach(ld -> {
					jdbcTemplate.update(
							"INSERT INTO statelevelmapping (lab_id,parameter_id,method_id,equipment_id,reagent_id,nflagstatusmethod,nflagstatusequipment,nflagstatusreagent,nflagstatus,nstatus) "
									+ " VALUES (?,?,?,?,?,?,?,?,?,?) ON CONFLICT (lab_id,reagent_id) DO NOTHING",
							ld.getLab_id(), ld.getParameter_id(),ld.getMethod_id(),ld.getEquipment_id(),ld.getReagent_id(),0,0,0,0,
							Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				});
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_DATASYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.OK);// 200 Data Synchronized from WQMIS API Server

			} else {
				jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NODATATOSYNCFROMSERVER",
						userInfo.getSlanguagefilename()), HttpStatus.NO_CONTENT);// 204 No Data to Synchronize from
																					// WQMIS API
			}
		} else {
			jdbcTemplate.execute("UPDATE wqmisapi SET dsyncstartdate='" + startDate + "',dsyncenddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nwqmisapicode=" + nwqmisapicode);
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_TRYAGAINLATER", userInfo.getSlanguagefilename()),
					HttpStatus.INTERNAL_SERVER_ERROR); // 500 Try to Synchronize the Data from WQMIS API Server Later
		}
	}
	

}
