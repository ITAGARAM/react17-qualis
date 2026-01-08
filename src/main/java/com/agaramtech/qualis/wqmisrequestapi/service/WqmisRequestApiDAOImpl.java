package com.agaramtech.qualis.wqmisrequestapi.service;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.PrintWriter;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;

import com.agaramtech.qualis.attachmentscomments.model.RegistrationAttachment;
import com.agaramtech.qualis.configuration.model.FTPConfig;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.PasswordUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.wqmis.model.WQMISApi;
import com.agaramtech.qualis.wqmisrequestapi.model.WQMISIntegrationAPIDetails;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import lombok.AllArgsConstructor;

//Added by Dhivya Bharathi on 26th Nov 2025 for jira id:swsm-122
@AllArgsConstructor
@Repository
public class WqmisRequestApiDAOImpl implements WqmisRequestApiDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(WqmisRequestApiDAOImpl.class);
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final PasswordUtilityFunction passwordUtilityFunction;
	private final CommonFunction commonFunction;

	@Override
	public ResponseEntity<Object> getIntegrationApiDetails(final UserInfo userInfo) throws Exception {

		final String strQuery = "SELECT nwqmisintegrationapidetailcode, jsondata " + "FROM wqmisintegrationapidetails "
				+ "WHERE nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "AND ntransactionstatus = " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " "
				+ "ORDER BY nwqmisintegrationapidetailcode DESC";

		LOGGER.info("Get Method:" + strQuery);
		List<Map<String, Object>> list = jdbcTemplate.queryForList(strQuery);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		RestTemplate restTemplate = new RestTemplate();

		String apiQuery = "SELECT * FROM wqmisapi WHERE nwqmisapicode = 23 " + "AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		WQMISApi objWQMISApi = (WQMISApi) jdbcUtilityFunction.queryForObject(apiQuery, WQMISApi.class, jdbcTemplate);

		String apiUrl = objWQMISApi.getSurl().trim();

		for (Map<String, Object> row : list) {

			Object jsonData = row.get("jsondata");
			HttpEntity<Object> entity = new HttpEntity<>(jsonData, headers);

			try {
				ResponseEntity<String> apiResponse = restTemplate.postForEntity(apiUrl, entity, String.class);

				String responseBody = apiResponse.getBody() != null ? apiResponse.getBody().replace("'", "''") : "";

				boolean isSuccess = false;
				try {
					JSONObject json = new JSONObject(apiResponse.getBody());
					isSuccess = json.optBoolean("Status", false);

				} catch (Exception e) {

					isSuccess = false;
				}

				String updateQuery = "UPDATE wqmisintegrationapidetails SET " + "ntransactionstatus = "
						+ (isSuccess ? Enumeration.TransactionStatus.SUCCESS.gettransactionstatus()
								: Enumeration.TransactionStatus.FAILED.gettransactionstatus())
						+ ", dtransactiondate = '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
						+ ", sresponse = '" + responseBody + "' " + "WHERE nwqmisintegrationapidetailcode = "
						+ row.get("nwqmisintegrationapidetailcode");

				jdbcTemplate.update(updateQuery);

			} catch (Exception ex) {

				String safeError = ex.getMessage().replace("'", "''");

				String updateQuery = "UPDATE wqmisintegrationapidetails SET " + "ntransactionstatus = "
						+ Enumeration.TransactionStatus.FAILED.gettransactionstatus() + ", " + "dtransactiondate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "sresponse = '" + safeError + "' "
						+ "WHERE nwqmisintegrationapidetailcode = " + row.get("nwqmisintegrationapidetailcode");

				jdbcTemplate.update(updateQuery);
			}

		}

		return ResponseEntity.ok("Processed");
	}

	// Added by Mohamed Ashik on 26th Nov 2025 for jira id:swsm-122
	@Override
	public ResponseEntity<Object> createSourceSampleInfoFTKUserDemo(Map<String, Object> inputList, UserInfo userInfo) {
		try {
			Map<String, Object> response = new HashMap<>();
			String base64Format = "";
			int npreregno = (int) inputList.get("npreregno");
			int ncoaparentcode = (int) inputList.get("ncoaparentcode");
			int ncoareporthistorycode = (int) inputList.get("ncoareporthistorycode");
			int nversionno = (int) inputList.get("nversionno");
			String stransactionsamplecode = (String) inputList.get("stransactionsamplecode");
			String stransactiontestcode = (String) inputList.get("stransactiontestcode");
			String sreferenceno = (String) inputList.get("sreportno");
			int ntransactionstatus = (int) inputList.get("ntransactionstatus");
			String sresponse = "";
			if (ntransactionstatus == Enumeration.TransactionStatus.RELEASED.gettransactionstatus()) {
				String str = "select * from wqmisintegrationapidetails where sreferenceno='" + sreferenceno
						+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by nwqmisintegrationapidetailcode desc limit 1";
				List<WQMISIntegrationAPIDetails> wqmisIntegrationAPIDetails = (List<WQMISIntegrationAPIDetails>) jdbcTemplate
						.query(str, new WQMISIntegrationAPIDetails());
				if (wqmisIntegrationAPIDetails.isEmpty()) {
					LocalDateTime currentTimestamp = LocalDateTime.now();
					int seqnoapi = jdbcTemplate.queryForObject(
							"select nsequenceno from " + " seqnoapi where stablename='wqmisintegrationapidetails'",
							Integer.class);
					seqnoapi++;
					// File content change into Base64 String by Mohamed Ashik 26-11-25
					String sQuery = "select * from registrationattachment where npreregno = " + npreregno
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode=" + userInfo.getNtranssitecode()
							+ " order by nregattachmentcode desc limit 1";

					final RegistrationAttachment validateAtttachment = (RegistrationAttachment) jdbcUtilityFunction
							.queryForObject(sQuery, RegistrationAttachment.class, jdbcTemplate);

					if (validateAtttachment != null) {
						final String ftpQuery = "select * from ftpconfig where nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus = "
								+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nsitecode = "
								+ userInfo.getNmastersitecode();
						final FTPConfig objFTPConfig = (FTPConfig) jdbcUtilityFunction.queryForObject(ftpQuery,
								FTPConfig.class, jdbcTemplate);

						if (objFTPConfig != null) {
							String RtnPasswordEncrypt = passwordUtilityFunction.decryptPassword("ftpconfig", "nftpno",
									objFTPConfig.getNftpno(), "spassword");
							FTPClient ftp = new FTPClient();
							ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
							ftp.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
							ftp.setBufferSize(1000);
							boolean ftpFile = ftp.login(objFTPConfig.getSusername(), RtnPasswordEncrypt);
							ftp.enterLocalPassiveMode();
							ftp.setFileType(FTP.BINARY_FILE_TYPE);
							if (ftpFile) {
								File folder = new File(objFTPConfig.getSphysicalpath());
								try {
									if (folder.exists() == false) {
										folder.mkdir();
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
								String path = objFTPConfig.getSphysicalpath()
										+ validateAtttachment.getJsondata().get("ssystemfilename").toString();
								File file = new File(path);
								if (file.exists()) {
									byte[] fileContent = Files.readAllBytes(file.toPath());
									base64Format = Base64.getEncoder().encodeToString(fileContent);
									ftp.logout();
								}
							}
						}
					}

					String strQuery = "SELECT jsonb_build_object("
							+ "'Sample_submitted_user_id', COALESCE(NULLIF(r.jsondata->>'Submitter ID', ''), '0')::integer, "
							+ "'Sample_collection_time', COALESCE(TO_CHAR((r.jsondata->>'Sample Collection Time')::timestamp, "
							+ "''), ''), "
							+ "'Sample_testing_time', COALESCE(TO_CHAR((r.jsondata->>'Sample Testing Time')::timestamp, "
							+ "'YYYY-MM-DD\"T\"HH24:MI:SS.MS'), ''), "
							+ "'Source_id', COALESCE(NULLIF(r.jsondata->>'Source Id', ''), '0')::integer, "
							+ "'Source_state_id', 18, "
							+ "'Source_district_id', COALESCE(NULLIF(r.jsondata->'District List'->>'ndistrictid', ''), '0')::integer, "
							+ "'Source_block_id', COALESCE(NULLIF(r.jsondata->'Block List'->>'nblockid', ''), '0')::integer, "
							+ "'Source_gp_id', COALESCE(NULLIF(r.jsondata->'Grama Panjayat'->>'npanchayatid', ''), '0')::integer, "
							+ "'Source_village_id', COALESCE(NULLIF(r.jsondata->'Village List'->>'nvillageid', ''), '0')::integer, "
							+ "'Source_habitation_id', COALESCE(NULLIF(r.jsondata->'Habitation ID'->>'sourceid', ''), '0')::integer, "
							+ "'Source_latitude', COALESCE(NULLIF(r.jsondata->>'Latitude', ''), '0')::numeric, "
							+ "'Source_longitude', COALESCE(NULLIF(r.jsondata->>'Longitude', ''), '0')::numeric, "
							+ "'Address', COALESCE(r.jsondata->>'Address', ''), "
							+ "'Remark', COALESCE(r.jsondata->>'Remark', ''), "
							+ "'Remedial_action_status', CASE WHEN COALESCE(r.jsondata->>'Remedial Action Status', '') = 'Yes' THEN 1 ELSE 0 END, "
							+ "'Remedial_action_by_id', COALESCE(NULLIF(r.jsondata->'Remedial Action by ID'->>'user_id', ''), '0')::integer, "
							+ "'Remedial_action_remarks', COALESCE(r.jsondata->>'Remedial Action Remarks', ''), "
							+ "'Parameters_testing_information'," + "COALESCE(jsonb_agg(jsonb_build_object"
							+ "('test_id', rt.ntestcode,'value_safe_range_status', NULLIF(tp.spredefinedname, '')::int) ) FILTER "
							+ "(WHERE tpp.ntestpackagecode ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "),jsonb_build_array(jsonb_build_object('test_id', 0, "
							+ "'value_safe_range_status', 0))), " + "'Mime_type', '', "
							+ "'Supporting_result_photo', ''" + ") AS final_json " + "from registration r "
							+ "inner join registrationtest rt on rt.npreregno = r.npreregno and rt.nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rt.ntransactiontestcode in (" + stransactiontestcode
							+ ") and rt.ntransactionsamplecode in(" + stransactionsamplecode + ")"
							+ " inner join resultparameter rp on rt.ntransactiontestcode = rp.ntransactiontestcode "
							+ "and rt.npreregno = rp.npreregno and rp.nparametertypecode = "
							+ Enumeration.ParameterType.PREDEFINED.getparametertype() + ""
							+ " inner join testpredefinedparameter tp on rp.ntestparametercode = tp.ntestparametercode and rp.jsondata->>'sfinal'=tp.spredefinedsynonym "
							+ "inner join testpackagetest tpp on rt.ntestcode = tpp.ntestcode"
							+ " where r.npreregno in(" + npreregno + ")" + " and r.nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " group by r.npreregno, r.jsondata";

					String strFilter = (String) jdbcUtilityFunction.queryForObject(strQuery, String.class,
							jdbcTemplate);
					if (strFilter != null) {
						JSONObject obj = new JSONObject(strFilter);
						if (validateAtttachment != null && validateAtttachment
								.getJsondata().get("ssystemfilename").toString().substring(validateAtttachment
										.getJsondata().get("ssystemfilename").toString().lastIndexOf("."))
								.equals(".jpg")) {
							obj.put("Supporting_result_photo", base64Format);
							obj.put("Mime_type", "image/jpg");
						}
						String insertQuery = "INSERT INTO wqmisintegrationapidetails (nwqmisintegrationapidetailcode, ncoareporthistorycode, ncoaparentcode, nversionno, sreferenceno, sresponse,jsondata, ntransactionstatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) "
								+ "VALUES (" + seqnoapi + "," + ncoareporthistorycode + "," + ncoaparentcode + ","
								+ nversionno + ",'" + sreferenceno + "','" + sresponse + "','" + obj.toString() + "',"
								+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'" + currentTimestamp
								+ "'," + userInfo.getNtimezonecode() + ","
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
								+ userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

						jdbcTemplate.execute(insertQuery);
						jdbcTemplate.execute("update seqnoapi set nsequenceno=" + seqnoapi
								+ " where stablename='wqmisintegrationapidetails'");
						getIntegrationApiDetails(userInfo);
						return ResponseEntity.ok(response);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								"IDS_NOVALIDTESTFORFTKSAMPLES", userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_AlREADYSYNCED", userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PLEASESELECTRELEASEDRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "FAILED", "message", e.getMessage()));
		}
	}

	public List<Map<String, Object>> getPendingSamples() {
		String sql = "SELECT cp.sreportno, chd.npreregno,chd.ntransactionsamplecode AS stransactionsamplecode,cp.nregtypecode,cp.nregsubtypecode,"
				+ "string_agg(chd.ntransactiontestcode::text, ',') AS stransactiontestcode,chr.ncoaparentcode, chr.ncoareporthistorycode, chr.nversionno,cp.ntransactionstatus, "
				+ "cp.nregtypecode, cp.nregsubtypecode " + "FROM coaparent cp "
				+ "JOIN (SELECT ncoaparentcode, MAX(nversionno) AS maxversion FROM coareporthistory where nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" GROUP BY ncoaparentcode) crh "
				+ "ON cp.ncoaparentcode = crh.ncoaparentcode "
				+ "JOIN coareporthistory chr ON chr.ncoaparentcode = crh.ncoaparentcode AND chr.nversionno = crh.maxversion "
				+ "JOIN coachild chd ON cp.ncoaparentcode = chd.ncoaparentcode "
				+ "JOIN registrationtype rt ON cp.nregtypecode = rt.nregtypecode "
				+ "JOIN registrationsubtype rst ON cp.nregsubtypecode = rst.nregsubtypecode "
				+ "WHERE cp.ntransactionstatus ="+Enumeration.TransactionStatus.RELEASED.gettransactionstatus()+" AND cp.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND cp.sreportno NOT IN (SELECT sreferenceno FROM wqmisintegrationapidetails WHERE ntransactionstatus = "
				+Enumeration.TransactionStatus.SUCCESS.gettransactionstatus()+" AND nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+") and cp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+" and chd.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+" and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+" and rst.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " GROUP BY cp.sreportno, chd.npreregno, chr.ncoaparentcode, chr.ncoareporthistorycode, chr.nversionno, "
				+ "cp.nregtypecode, cp.nregsubtypecode,cp.ntransactionstatus,chd.ntransactionsamplecode,cp.nregsubtypecode,cp.nregtypecode";

		return jdbcTemplate.queryForList(sql);
	}

	// Added by Mohamed Ashik on 26th Nov 2025 for jira id:swsm-122
	@Override
	public ResponseEntity<Object> createSourceSampleInfoDeptUserDemo(Map<String, Object> inputList, UserInfo userInfo) {
		try {
			Map<String, Object> response = new HashMap<>();
			int npreregno = (int) inputList.get("npreregno");
			int ncoaparentcode = (int) inputList.get("ncoaparentcode");
			int ncoareporthistorycode = (int) inputList.get("ncoareporthistorycode");
			int nversionno = (int) inputList.get("nversionno");
			String stransactionsamplecode = (String) inputList.get("stransactionsamplecode");
			String stransactiontestcode = (String) inputList.get("stransactiontestcode");
			String sreferenceno = (String) inputList.get("sreportno");
			int ntransactionstatus = (int) inputList.get("ntransactionstatus");
			String sresponse = "";
			if (ntransactionstatus == Enumeration.TransactionStatus.RELEASED.gettransactionstatus()) {
				String str = "select * from wqmisintegrationapidetails where sreferenceno='" + sreferenceno
						+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by nwqmisintegrationapidetailcode desc limit 1";
				List<WQMISIntegrationAPIDetails> wqmisIntegrationAPIDetails = (List<WQMISIntegrationAPIDetails>) jdbcTemplate
						.query(str, new WQMISIntegrationAPIDetails());
				if (wqmisIntegrationAPIDetails.isEmpty()) {
					LocalDateTime currentTimestamp = LocalDateTime.now();
					int seqnoapi = jdbcTemplate.queryForObject(
							"select nsequenceno from " + " seqnoapi where stablename='wqmisintegrationapidetails'",
							Integer.class);
					seqnoapi++;

					String strQuery = "SELECT jsonb_build_object("
							+ "'Sample_submitted_user_id', COALESCE(NULLIF(r.jsondata->>'Submitter ID', ''), '0')::integer, "
							+ "'Lab_id', COALESCE(NULLIF(r.jsondata->'Lab ID'->>'lab_id', ''), '0')::integer, "
							+ "'Sample_collection_time', COALESCE(TO_CHAR((r.jsondata->>'Sample Collection Time')::timestamp, "
							+ "'YYYY-MM-DD\"T\"HH24:MI:SS.MS'), ''), "
							+ "'Source_id', COALESCE(NULLIF(r.jsondata->>'Source Id', ''), '0')::integer, "
							+ "'Source_state_id', 18, "
							+ "'Source_district_id', COALESCE(NULLIF(r.jsondata->'District List'->>'ndistrictid', ''), '0')::integer, "
							+ "'Source_block_id', COALESCE(NULLIF(r.jsondata->'Block List'->>'nblockid', ''), '0')::integer, "
							+ "'Source_gp_id', COALESCE(NULLIF(r.jsondata->'Grama Panjayat'->>'npanchayatid', ''), '0')::integer, "
							+ "'Source_village_id', COALESCE(NULLIF(r.jsondata->'Village List'->>'nvillageid', ''), '0')::integer, "
							+ "'Source_habitation_id', COALESCE(NULLIF(r.jsondata->'Habitation ID'->>'sourceid', ''), '0')::integer, "
							+ "'Source_pin_code', COALESCE(NULLIF(r.jsondata->>'Pincode', ''), '0')::integer, "
							+ "'Source_latitude', COALESCE(NULLIF(r.jsondata->>'Latitude', ''), '0')::numeric, "
							+ "'Source_longitude', COALESCE(NULLIF(r.jsondata->>'Longitude', ''), '0')::numeric, "
							+ "'Remark', COALESCE(r.jsondata->>'Remark', ''), "
							+ "'Sample_received_by_id', COALESCE(NULLIF(r.jsondata->'Technician User'->>'value', ''), '0')::integer, "
							+ "'Sample_received_time', COALESCE(TO_CHAR((r.jsondata->>'Sample Received Time')::timestamp, "
							+ "'YYYY-MM-DD\"T\"HH24:MI:SS.MS'), ''), "
							+ "'Date_of_result_availability', COALESCE(TO_CHAR((r.jsondata->>'Date Of Result Availability')::timestamp, "
							+ "'YYYY-MM-DD\"T\"HH24:MI:SS.MS'), ''), "
							+ "'Contamination_status', CASE WHEN COALESCE(r.jsondata->>'Contamination Status', '') = 'Yes' THEN 1 ELSE 0 END, "
							+ "'Sample_report_approval_action_by_id', COALESCE(NULLIF(r.jsondata->'Incharge User'->>'user_id', ''), '0')::integer, "
							+ "'Sample_report_approval_action_time', COALESCE(TO_CHAR((r.jsondata->>'Incharge Date')::timestamp, "
							+ "'YYYY-MM-DD\"T\"HH24:MI:SS.MS'), ''), "
							+ "'Sample_report_approval_remark', COALESCE(r.jsondata->>'Incharge Remarks', ''), "
							+ "'Remedial_action_status', CASE WHEN COALESCE(r.jsondata->>'Remedial Action Status', '') = 'Yes' THEN 1 ELSE 0 END, "
							+ "'Remedial_action_by_id', COALESCE(NULLIF(r.jsondata->'Remedial Action by ID'->>'user_id', ''), '0')::integer, "
							+ "'Remedial_action_remarks', COALESCE(r.jsondata->>'Remedial Action Remarks', ''), "
							+ "'Parameters_testing_information'," + "COALESCE(jsonb_agg(jsonb_build_object"
							+ "('test_id', rt.ntestcode,'value_safe_range_status',tp.spredefinedsynonym, "
							+ "'value_in_range',NULLIF(tp.spredefinedname, '')::int," + "'method_id','',"
							+ "'test_time', COALESCE(TO_CHAR((r.jsondata->>'Date Of Result Availability')::timestamp, "
							+ "'YYYY-MM-DD\"T\"HH24:MI:SS.MS'), '')," + "'test_remarks','',"
							+ "'equipment_ids',rui.ninstrumentnamecode," + "'reagents_ids',rum.nmaterialcode)) FILTER "
							+ "(WHERE tpp.ntestpackagecode ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "))) AS final_json "
							+ "from registration r "
							+ "inner join registrationtest rt on rt.npreregno = r.npreregno and rt.nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and rt.ntransactiontestcode in (" + stransactiontestcode
							+ ") and rt.ntransactionsamplecode in(" + stransactionsamplecode + ")"
							+ " inner join resultparameter rp on rt.ntransactiontestcode = rp.ntransactiontestcode "
							+ "and rt.npreregno = rp.npreregno and rp.nparametertypecode = "
							+ Enumeration.ParameterType.PREDEFINED.getparametertype() + ""
							+ " inner join testpredefinedparameter tp on rp.ntestparametercode = tp.ntestparametercode and rp.jsondata->>'sfinal'=tp.spredefinedsynonym "
							+ "inner join testpackagetest tpp on rt.ntestcode = tpp.ntestcode "
							+ "inner join resultusedinstrument rui on rt.ntransactiontestcode = rui.ntransactiontestcode and r.npreregno = rui.npreregno "
							+ "inner join resultusedmaterial rum on rt.ntransactiontestcode = rum.ntransactiontestcode and r.npreregno = rum.npreregno "
							+ "inner join testgrouptest tgt on rt.ntestgrouptestcode=tgt.ntestgrouptestcode"
							+ " where r.npreregno in(" + npreregno + ")" + " and r.nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
							+ " group by r.npreregno, r.jsondata";

					String strFilter = (String) jdbcUtilityFunction.queryForObject(strQuery, String.class,
							jdbcTemplate);
					if (strFilter != null) {
						String insertQuery = "INSERT INTO wqmisintegrationapidetails (nwqmisintegrationapidetailcode, ncoareporthistorycode, ncoaparentcode, nversionno, sreferenceno, sresponse,jsondata, ntransactionstatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) "
								+ "VALUES (" + seqnoapi + "," + ncoareporthistorycode + "," + ncoaparentcode + ","
								+ nversionno + ",'" + sreferenceno + "','" + sresponse + "','" + strFilter + "',"
								+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'" + currentTimestamp
								+ "'," + userInfo.getNtimezonecode() + ","
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
								+ userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

						jdbcTemplate.execute(insertQuery);
						jdbcTemplate.execute("update seqnoapi set nsequenceno=" + seqnoapi
								+ " where stablename='wqmisintegrationapidetails'");
						getSampleIntegrationApiDetails(userInfo);
						return ResponseEntity.ok(response);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								"IDS_NOVALIDTESTFORFTKSAMPLES", userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_AlREADYSYNCED", userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PLEASESELECTRELEASEDRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "FAILED", "message", e.getMessage()));
		}
	}

	// Added by Mohamed Ashik on 26th Nov 2025 for jira id:swsm-122
	public ResponseEntity<Object> getSampleIntegrationApiDetails(final UserInfo userInfo) throws Exception {

		final String strQuery = "SELECT nwqmisintegrationapidetailcode, jsondata " + "FROM wqmisintegrationapidetails "
				+ "WHERE nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "AND ntransactionstatus = " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " "
				+ "ORDER BY nwqmisintegrationapidetailcode DESC";

		LOGGER.info("Get Method:" + strQuery);
		List<Map<String, Object>> list = jdbcTemplate.queryForList(strQuery);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		RestTemplate restTemplate = new RestTemplate();

		String apiQuery = "SELECT * FROM wqmisapi WHERE nwqmisapicode = 24 " + "AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		WQMISApi objWQMISApi = (WQMISApi) jdbcUtilityFunction.queryForObject(apiQuery, WQMISApi.class, jdbcTemplate);

		String apiUrl = objWQMISApi.getSurl().trim();

		for (Map<String, Object> row : list) {

			Object jsonData = row.get("jsondata");
			HttpEntity<Object> entity = new HttpEntity<>(jsonData, headers);

			try {
				ResponseEntity<String> apiResponse = restTemplate.postForEntity(apiUrl, entity, String.class);

				String responseBody = apiResponse.getBody() != null ? apiResponse.getBody().replace("'", "''") : "";

				boolean isSuccess = false;
				try {
					JSONObject json = new JSONObject(apiResponse.getBody());
					isSuccess = json.optBoolean("Status", false);
				} catch (Exception e) {
					isSuccess = false;
				}

				String updateQuery = "UPDATE wqmisintegrationapidetails SET " + "ntransactionstatus = "
						+ (isSuccess ? Enumeration.TransactionStatus.SUCCESS.gettransactionstatus()
								: Enumeration.TransactionStatus.FAILED.gettransactionstatus())
						+ ", dtransactiondate = '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
						+ ", sresponse = '" + responseBody + "' " + "WHERE nwqmisintegrationapidetailcode = "
						+ row.get("nwqmisintegrationapidetailcode");

				jdbcTemplate.update(updateQuery);

			} catch (Exception ex) {
				String safeError = ex.getMessage().replace("'", "''");
				String updateQuery = "UPDATE wqmisintegrationapidetails SET " + "ntransactionstatus = "
						+ Enumeration.TransactionStatus.FAILED.gettransactionstatus() + ", " + "dtransactiondate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "sresponse = '" + safeError + "' "
						+ "WHERE nwqmisintegrationapidetailcode = " + row.get("nwqmisintegrationapidetailcode");

				jdbcTemplate.update(updateQuery);
			}

		}
		return ResponseEntity.ok("Processed");
	}

	// Added by Mohamed Ashik on 29th Nov 2025 for jira id:swsm-122
	@Override
	public ResponseEntity<Object> createAPIIntegrationDetails(Map<String, Object> inputList, UserInfo userInfo)
			throws Exception {
		Map<String, Object> rtnResponse = new HashMap<>();
		try {
			final int npreregno = (int) inputList.get("npreregno");
			final int ncoaparentcode = (int) inputList.get("ncoaparentcode");
			final int ncoareporthistorycode = (int) inputList.get("ncoareporthistorycode");
			final int nversionno = (int) inputList.get("nversionno");
			final String stransactionsamplecode = (String) inputList.get("stransactionsamplecode");
			final String stransactiontestcode = (String) inputList.get("stransactiontestcode");
			final String sreferenceno = (String) inputList.get("sreportno");
			final int ntransactionstatus = (int) inputList.get("ntransactionstatus");
			final int nregtypecode = (int) inputList.get("nregtypecode");
			final int nregsubtypecode = (int) inputList.get("nregsubtypecode");
			String sresponse = "";
			String strQuery = "";
			String strFilter = "";
			String base64Format = "";
			if (ntransactionstatus == Enumeration.TransactionStatus.RELEASED.gettransactionstatus()) {
				String str = "select * from wqmisintegrationapidetails where sreferenceno='" + sreferenceno
						+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by nwqmisintegrationapidetailcode desc limit 1";
				List<WQMISIntegrationAPIDetails> wqmisIntegrationAPIDetails = (List<WQMISIntegrationAPIDetails>) jdbcTemplate
						.query(str, new WQMISIntegrationAPIDetails());
				if (wqmisIntegrationAPIDetails.isEmpty()) {
					LocalDateTime currentTimestamp = LocalDateTime.now();
					int seqnoapi = jdbcTemplate.queryForObject(
							"select nsequenceno from " + " seqnoapi where stablename='wqmisintegrationapidetails'",
							Integer.class);
					seqnoapi++;
					String apiQuery = "select * from wqmisapi where nwqmisapicode in(23,24) and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					List<WQMISApi> objWqmis = (List<WQMISApi>) jdbcTemplate.query(apiQuery, new WQMISApi());
					// File content change into Base64 String by Mohamed Ashik 26-11-25
					String sQuery = "select * from registrationattachment where npreregno = " + npreregno
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and nsitecode=" + userInfo.getNtranssitecode()
							+ " order by nregattachmentcode desc limit 1";

					final RegistrationAttachment validateAtttachment = (RegistrationAttachment) jdbcUtilityFunction
							.queryForObject(sQuery, RegistrationAttachment.class, jdbcTemplate);

					if (validateAtttachment != null) {
						final String ftpQuery = "select * from ftpconfig where nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ndefaultstatus = "
								+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nsitecode = "
								+ userInfo.getNmastersitecode();
						final FTPConfig objFTPConfig = (FTPConfig) jdbcUtilityFunction.queryForObject(ftpQuery,
								FTPConfig.class, jdbcTemplate);

						if (objFTPConfig != null) {
							String RtnPasswordEncrypt = passwordUtilityFunction.decryptPassword("ftpconfig", "nftpno",
									objFTPConfig.getNftpno(), "spassword");
							FTPClient ftp = new FTPClient();
							ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
							ftp.connect(objFTPConfig.getShost(), objFTPConfig.getNportno());
							ftp.setBufferSize(1000);
							boolean ftpFile = ftp.login(objFTPConfig.getSusername(), RtnPasswordEncrypt);
							ftp.enterLocalPassiveMode();
							ftp.setFileType(FTP.BINARY_FILE_TYPE);
							if (ftpFile) {
								File folder = new File(objFTPConfig.getSphysicalpath());
								try {
									if (folder.exists() == false) {
										folder.mkdir();
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
								String path = objFTPConfig.getSphysicalpath()
										+ validateAtttachment.getJsondata().get("ssystemfilename").toString();
								File file = new File(path);
								if (file.exists()) {
									byte[] fileContent = Files.readAllBytes(file.toPath());
									base64Format = Base64.getEncoder().encodeToString(fileContent);
									ftp.logout();
								}
							}
						}
					}
					for (WQMISApi wqmisApi : objWqmis) {
						if (wqmisApi.getNregtypecode() == nregtypecode
								&& wqmisApi.getNregsubtypecode() == nregsubtypecode && wqmisApi.getNwqmisapicode() == 23) {
							strQuery = "SELECT jsonb_build_object("
									+ "'Sample_submitted_user_id', COALESCE(NULLIF(r.jsondata->>'Submitter ID', ''), '0')::integer, "
									+ "'Sample_collection_time', COALESCE(TO_CHAR((r.jsondata->>'Sample Collection Time')::timestamp, "
									+ "'YYYY-MM-DD\"T\"HH24:MI:SS'), ''), "
									+ "'Sample_testing_time', COALESCE(TO_CHAR((r.jsondata->>'Sample Testing Time')::timestamp, "
									+ "'YYYY-MM-DD\"T\"HH24:MI:SS'), ''), "
									+ "'Source_id', COALESCE(NULLIF(r.jsondata->>'Source Id', ''), '0')::integer, "
									+ "'Source_state_id', 18, "
									+ "'Source_district_id', COALESCE(NULLIF(r.jsondata->'District List'->>'ndistrictid', ''), '0')::integer, "
									+ "'Source_block_id', COALESCE(NULLIF(r.jsondata->'Block List'->>'nblockid', ''), '0')::integer, "
									+ "'Source_gp_id', COALESCE(NULLIF(r.jsondata->'Grama Panjayat'->>'npanchayatid', ''), '0')::integer, "
									+ "'Source_village_id', COALESCE(NULLIF(r.jsondata->'Village List'->>'nvillageid', ''), '0')::integer, "
									+ "'Source_habitation_id', COALESCE(NULLIF(r.jsondata->'Habitation ID'->>'sourceid', ''), '0')::integer, "
									+ "'Source_latitude', COALESCE(NULLIF(r.jsondata->>'Latitude', ''), '0')::numeric, "
									+ "'Source_longitude', COALESCE(NULLIF(r.jsondata->>'Longitude', ''), '0')::numeric, "
									+ "'Address', COALESCE(r.jsondata->>'Address', ''), "
									+ "'Remark', COALESCE(r.jsondata->>'Remarks', ''), "
									+ "'Remedial_action_status', CASE WHEN COALESCE(r.jsondata->>'Remedial Action Status', '') = 'Yes' THEN 1 ELSE 0 END, "
									+ "'Remedial_action_by_id', COALESCE(NULLIF(r.jsondata->'Remedial Action by ID'->>'user_id', ''), '0')::integer, "
									+ "'Remedial_action_remarks', COALESCE(r.jsondata->>'Remedial Action Remarks', ''), "
									+ "'Parameters_testing_information'," + "COALESCE(jsonb_agg(jsonb_build_object"
									+ "('test_id', rt.ntestcode,'value_safe_range_status', NULLIF(tp.spredefinedname, '')::int) ) FILTER "
									+ "(WHERE tpp.ntestpackagecode ="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ "),jsonb_build_array(jsonb_build_object('test_id', 0, "
									+ "'value_safe_range_status', 0))), " + "'Mime_type', '', "
									+ "'Supporting_result_photo', ''" + ") AS final_json " + "from registration r "
									+ "inner join registrationtest rt on rt.npreregno = r.npreregno and rt.nstatus ="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rt.ntransactiontestcode in (" + stransactiontestcode
									+ ") and rt.ntransactionsamplecode in(" + stransactionsamplecode + ")"
									+ " inner join resultparameter rp on rt.ntransactiontestcode = rp.ntransactiontestcode "
									+ "and rt.npreregno = rp.npreregno and rp.nparametertypecode = "
									+ Enumeration.ParameterType.PREDEFINED.getparametertype() + ""
									+ " inner join testpredefinedparameter tp on rp.ntestparametercode = tp.ntestparametercode and rp.jsondata->>'sfinal'=tp.spredefinedsynonym "
									+ "inner join testpackagetest tpp on rt.ntestcode = tpp.ntestcode"
									+ " where r.npreregno in(" + npreregno + ")" + " and r.nstatus ="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tpp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " group by r.npreregno, r.jsondata";

							strFilter = (String) jdbcUtilityFunction.queryForObject(strQuery, String.class,
									jdbcTemplate);
							if (strFilter != null) {
								JSONObject obj = new JSONObject(strFilter);
								if (validateAtttachment != null && validateAtttachment
										.getJsondata().get("ssystemfilename").toString().substring(validateAtttachment
												.getJsondata().get("ssystemfilename").toString().lastIndexOf("."))
										.equals(".jpg")) {
									obj.put("Supporting_result_photo", base64Format);
									obj.put("Mime_type", "image/jpg");
								}
								String insertQuery = "INSERT INTO wqmisintegrationapidetails (nwqmisintegrationapidetailcode, ncoareporthistorycode, ncoaparentcode, nversionno, sreferenceno, sresponse,jsondata, ntransactionstatus, dtransactiondate, ntztransactiondate,nregtypecode, nregsubtypecode, noffsetdtransactiondate, nsitecode, nstatus) "
										+ "VALUES (" + seqnoapi + "," + ncoareporthistorycode + "," + ncoaparentcode
										+ "," + nversionno + ",'" + sreferenceno + "','" + sresponse + "','" + strFilter
										+ "'," + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
										+ currentTimestamp + "'," + userInfo.getNtimezonecode() + "," + wqmisApi.getNregtypecode() + "," +wqmisApi.getNregsubtypecode()+ ","
										+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
										+ userInfo.getNtranssitecode() + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()  +")";

								jdbcTemplate.execute(insertQuery);
								jdbcTemplate.execute("update seqnoapi set nsequenceno=" + seqnoapi
										+ " where stablename='wqmisintegrationapidetails'");
								rtnResponse = getSampleAndFTKSampleIntegrationApiCall(userInfo, wqmisApi);
							} else {
								return new ResponseEntity<>(
										commonFunction.getMultilingualMessage("IDS_NOVALIDTESTFORFTKSAMPLES",
												userInfo.getSlanguagefilename()),
										HttpStatus.CONFLICT);
							}
						} else if (wqmisApi.getNregtypecode() == nregtypecode
								&& wqmisApi.getNregsubtypecode() == nregsubtypecode && wqmisApi.getNwqmisapicode() == 24) {
							strQuery = "SELECT jsonb_build_object("
									+ "'Sample_submitted_user_id', COALESCE(NULLIF(r.jsondata->>'Submitter ID', ''), '0')::integer, "
									+ "'Lab_id', COALESCE(NULLIF(r.jsondata->'Lab ID'->>'lab_id', ''), '0')::integer, "
									+ "'Sample_collection_time', COALESCE(TO_CHAR((r.jsondata->>'Sample Collection Time')::timestamp, "
									+ "'YYYY-MM-DD\"T\"HH24:MI:SS'), ''), "
									+ "'Source_id', COALESCE(NULLIF(r.jsondata->>'Source Id', ''), '0')::integer, "
									+ "'Source_state_id', 18, "
									+ "'Source_district_id', COALESCE(NULLIF(r.jsondata->'District List'->>'ndistrictid', ''), '0')::integer, "
									+ "'Source_block_id', COALESCE(NULLIF(r.jsondata->'Block List'->>'nblockid', ''), '0')::integer, "
									+ "'Source_gp_id', COALESCE(NULLIF(r.jsondata->'Grama Panjayat'->>'npanchayatid', ''), '0')::integer, "
									+ "'Source_village_id', COALESCE(NULLIF(r.jsondata->'Village List'->>'nvillageid', ''), '0')::integer, "
									+ "'Source_habitation_id', COALESCE(NULLIF(r.jsondata->'Habitation ID'->>'sourceid', ''), '0')::integer, "
									+ "'Source_pin_code', COALESCE(NULLIF(r.jsondata->>'Pincode', ''), '0')::integer, "
									+ "'Source_latitude', COALESCE(NULLIF(r.jsondata->>'Latitude', ''), '0')::numeric, "
									+ "'Source_longitude', COALESCE(NULLIF(r.jsondata->>'Longitude', ''), '0')::numeric, "
									+ "'Remark', COALESCE(r.jsondata->>'Remarks', ''), "
									+ "'Sample_received_by_id', COALESCE(NULLIF(r.jsondata->'Technician User'->>'value', ''), '0')::integer, "
									+ "'Sample_received_time', COALESCE(TO_CHAR((r.jsondata->>'Sample Received Time')::timestamp, "
									+ "'YYYY-MM-DD\"T\"HH24:MI:SS'), ''), "
									+ "'Date_of_result_availability', COALESCE(TO_CHAR((r.jsondata->>'Date Of Result Availability')::timestamp, "
									+ "'YYYY-MM-DD\"T\"HH24:MI:SS'), ''), "
									+ "'Contamination_status', CASE WHEN COALESCE(r.jsondata->>'Contamination Status', '') = 'Yes' THEN 1 ELSE 0 END, "
									+ "'Sample_report_approval_action_by_id', COALESCE(NULLIF(r.jsondata->'Incharge User'->>'user_id', ''), '0')::integer, "
									+ "'Sample_report_approval_action_time', COALESCE(TO_CHAR((r.jsondata->>'Incharge Date')::timestamp, "
									+ "'YYYY-MM-DD\"T\"HH24:MI:SS'), ''), "
									+ "'Sample_report_approval_remark', COALESCE(r.jsondata->>'Incharge Remarks', ''), "
									+ "'Remedial_action_status', CASE WHEN COALESCE(r.jsondata->>'Remedial Action Status', '') = 'Yes' THEN 1 ELSE 0 END, "
									+ "'Remedial_action_by_id', COALESCE(NULLIF(r.jsondata->'Remedial Action by ID'->>'user_id', ''), '0')::integer, "
									+ "'Remedial_action_remarks', COALESCE(r.jsondata->>'Remedial Action Remarks', ''), "
									+ "'Parameters_testing_information'," + "COALESCE(jsonb_agg(jsonb_build_object"
									+ "('test_id', rt.ntestcode,'value_safe_range_status',tp.spredefinedsynonym, "
									+ "'value_in_range',NULLIF(tp.spredefinedname, '')::int," + "'method_id','',"
									+ "'test_time', COALESCE(TO_CHAR((r.jsondata->>'Date Of Result Availability')::timestamp, "
									+ "'YYYY-MM-DD\"T\"HH24:MI:SS'), '')," + "'test_remarks','',"
									+ "'equipment_ids',rui.ninstrumentnamecode,"
									+ "'reagents_ids',rum.nmaterialcode)) FILTER " + "(WHERE tpp.ntestpackagecode ="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "))) AS final_json "
									+ "from registration r "
									+ "inner join registrationtest rt on rt.npreregno = r.npreregno and rt.nstatus ="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rt.ntransactiontestcode in (" + stransactiontestcode
									+ ") and rt.ntransactionsamplecode in(" + stransactionsamplecode + ")"
									+ " inner join resultparameter rp on rt.ntransactiontestcode = rp.ntransactiontestcode "
									+ "and rt.npreregno = rp.npreregno and rp.nparametertypecode = "
									+ Enumeration.ParameterType.PREDEFINED.getparametertype() + ""
									+ " inner join testpredefinedparameter tp on rp.ntestparametercode = tp.ntestparametercode and rp.jsondata->>'sfinal'=tp.spredefinedsynonym "
									+ "inner join testpackagetest tpp on rt.ntestcode = tpp.ntestcode "
									+ "inner join resultusedinstrument rui on rt.ntransactiontestcode = rui.ntransactiontestcode and r.npreregno = rui.npreregno "
									+ "inner join resultusedmaterial rum on rt.ntransactiontestcode = rum.ntransactiontestcode and r.npreregno = rum.npreregno "
									+ "inner join testgrouptest tgt on rt.ntestgrouptestcode=tgt.ntestgrouptestcode"
									+ " where r.npreregno in(" + npreregno + ")" + " and r.nstatus ="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and rt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and tp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tpp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and rui.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " and rum.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and tgt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
									+ " group by r.npreregno, r.jsondata";

							strFilter = (String) jdbcUtilityFunction.queryForObject(strQuery, String.class,
									jdbcTemplate);
							if (strFilter != null) {
								String insertQuery = "INSERT INTO wqmisintegrationapidetails (nwqmisintegrationapidetailcode, ncoareporthistorycode, ncoaparentcode, nversionno, sreferenceno, sresponse,jsondata, ntransactionstatus, dtransactiondate, ntztransactiondate, nregtypecode, nregsubtypecode, noffsetdtransactiondate, nsitecode, nstatus) "
										+ "VALUES (" + seqnoapi + "," + ncoareporthistorycode + "," + ncoaparentcode
										+ "," + nversionno + ",'" + sreferenceno + "','" + sresponse + "','" + strFilter
										+ "'," + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
										+ currentTimestamp + "'," + userInfo.getNtimezonecode() + "," + wqmisApi.getNregtypecode() + ","+wqmisApi.getNregsubtypecode() + ","
										+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
										+ userInfo.getNtranssitecode() + ","
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()  +")";

								jdbcTemplate.execute(insertQuery);
								jdbcTemplate.execute("update seqnoapi set nsequenceno=" + seqnoapi
										+ " where stablename='wqmisintegrationapidetails'");
								rtnResponse = getSampleAndFTKSampleIntegrationApiCall(userInfo, wqmisApi);
							} else {
								return new ResponseEntity<>(
										commonFunction.getMultilingualMessage("IDS_NOVALIDTESTFORLABSAMPLES",
												userInfo.getSlanguagefilename()),
										HttpStatus.CONFLICT);
							}
						}
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_AlREADYSYNCED", userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PLEASESELECTRELEASEDRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "FAILED", "message", e.getMessage()));
		}
		return new ResponseEntity<>(rtnResponse, HttpStatus.OK);
	}

	// Added by Mohamed Ashik on 26th Nov 2025 for jira id:swsm-122
	public Map<String, Object> getSampleAndFTKSampleIntegrationApiCall(final UserInfo userInfo, WQMISApi obj)
			throws Exception {
		Map<String, Object> response = new HashMap<>();
		final String strQuery = "SELECT nwqmisintegrationapidetailcode, jsondata " + "FROM wqmisintegrationapidetails "
				+ "WHERE nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "AND ntransactionstatus = " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() 
				+ " and nregtypecode="+ obj.getNregtypecode() +" and nregsubtypecode="+ obj.getNregsubtypecode()
				+ " ORDER BY nwqmisintegrationapidetailcode DESC";
		LOGGER.info("Get Method:" + strQuery);
		List<Map<String, Object>> list = jdbcTemplate.queryForList(strQuery);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String authToken = obj.getStokenid() + "";
		headers.add("Authorization", "Bearer " + authToken);
		RestTemplate restTemplate = new RestTemplate();
		String apiUrl = obj.getSurl().trim();

		for (Map<String, Object> row : list) {
			Object jsonData = row.get("jsondata");
			HttpEntity<Object> entity = new HttpEntity<>(jsonData, headers);
			try {
				ResponseEntity<String> apiResponse = restTemplate.postForEntity(apiUrl, entity, String.class);
				String responseBody = apiResponse.getBody() != null ? apiResponse.getBody().replace("'", "''") : "";
				boolean isSuccess = false;
				try {
					JSONObject json = new JSONObject(apiResponse.getBody());
					isSuccess = json.optBoolean("Status", false);
				} catch (Exception e) {
					isSuccess = false;
				}
				String updateQuery = "UPDATE wqmisintegrationapidetails SET " + "ntransactionstatus = "
						+ (isSuccess ? Enumeration.TransactionStatus.SUCCESS.gettransactionstatus()
								: Enumeration.TransactionStatus.FAILED.gettransactionstatus())
						+ ", dtransactiondate = '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
						+ ", sresponse = '" + responseBody + "' " + "WHERE nwqmisintegrationapidetailcode = "
						+ row.get("nwqmisintegrationapidetailcode");

				jdbcTemplate.update(updateQuery);

				if (isSuccess) {
					response.put("rtn", "Success");
				}

			} catch (Exception ex) {
				String safeError = ex.getMessage().replace("'", "''");
				String updateQuery = "UPDATE wqmisintegrationapidetails SET " + "ntransactionstatus = "
						+ Enumeration.TransactionStatus.FAILED.gettransactionstatus() + ", " + "dtransactiondate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "sresponse = '" + safeError + "' "
						+ "WHERE nwqmisintegrationapidetailcode = " + row.get("nwqmisintegrationapidetailcode");

				jdbcTemplate.update(updateQuery);
			}
		}
		return response;
	}
}
