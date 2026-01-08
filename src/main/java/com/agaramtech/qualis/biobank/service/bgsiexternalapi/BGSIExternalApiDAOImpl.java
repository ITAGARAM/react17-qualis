package com.agaramtech.qualis.biobank.service.bgsiexternalapi;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.agaramtech.qualis.biobank.model.BioExternalTransferData;
import com.agaramtech.qualis.biobank.model.BioThirdPartyFormAccept;
import com.agaramtech.qualis.biobank.model.BioThirdPartyFormAcceptDetails;
import com.agaramtech.qualis.biobank.model.SeqNoBioBankManagement;
import com.agaramtech.qualis.biobank.model.BioSubjectDetails;
import com.agaramtech.qualis.biobank.model.ExternalUrlSettings;
import com.agaramtech.qualis.global.ApiEndPoint;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.SeqNoRegistration;
import com.agaramtech.qualis.restcontroller.RequestContext;
import com.agaramtech.qualis.samplescheduling.model.SampleRequesting;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class BGSIExternalApiDAOImpl implements BGSIExternalApiDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(BGSIExternalApiDAOImpl.class);

	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcTemplateUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final CommonFunction commonFunction;
	private final StringUtilityFunction stringUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final EmailDAOSupport emailDAOSupport;

	private static final Random random = new Random();

	@Override
	public ResponseEntity<Object> updatepatientconsent(Map<String, Object> inputMap, UserInfo userinfo)
			throws Exception {
//		RequestContext requestContext = new RequestContext();
//		requestContext.setUserInfo(userinfo);

		userinfo.setNmastersitecode((short) -1);

		String query = "select nformcode  FROM controlmaster WHERE scontrolname='updatepatientconsent'";
		short nformcode = jdbcTemplate.queryForObject(query, short.class);
		userinfo.setNformcode(nformcode);

		userinfo.setSlanguagefilename("Msg_en_US");
		userinfo.setSlanguagename("English");
		userinfo.setSlanguagetypecode("en-US");
		userinfo.setNtimezonecode(-1);
		userinfo.setStimezoneid("Asia/Jakarta");
		userinfo.setIsutcenabled(4);

		final ObjectMapper objMapper = new ObjectMapper();
//	        try {
		if (inputMap == null) {
			return buildResponse("Invalid Input Data", 404, HttpStatus.NOT_FOUND);
		}

		Object dataObj = inputMap.get("data");
		List<Map<String, Object>> items = new ArrayList<>();

		if (dataObj == null) {
			return buildResponse("Invalid Input Data", 404, HttpStatus.NOT_FOUND);
		}

		// Normalize data to List<Map<String,Object>>
		if (dataObj instanceof String) {
			items = objMapper.readValue((String) dataObj, new TypeReference<List<Map<String, Object>>>() {
			});
		} else if (dataObj instanceof List) {
			for (Object el : (List<?>) dataObj) {
				if (el instanceof Map && !((Map<String, Object>) el).isEmpty()
						&& ((Map<String, Object>) el).containsKey("ssubjectid")
						&& ((Map<String, Object>) el).containsKey("nisthirdpartysharable")
						&& ((Map<String, Object>) el).containsKey("nissampleaccesable")) {
					items.add((Map<String, Object>) el);
				} else {
					String temp = objMapper.writeValueAsString(el);
					Map<String, Object> map = null;
					try {
						map = objMapper.readValue(temp, new TypeReference<Map<String, Object>>() {
						});
					} catch (Exception e) {
						return buildResponse("Invalid Input Data", 404, HttpStatus.NOT_FOUND);
					}
					if (!map.isEmpty() && map.containsKey("ssubjectid") && map.containsKey("nisthirdpartysharable")
							&& map.containsKey("nissampleaccesable")) {
						items.add(map);
					}
				}
			}
		} else {
			return buildResponse("Invalid Input Data", 404, HttpStatus.NOT_FOUND);
		}

		if (items.isEmpty()) {
			return buildResponse("Invalid Input Data", 404, HttpStatus.NOT_FOUND);
		}

		// Updated By Mullai Balaji V for email JIRA ID: BGSI:147

		final String selectSql = "SELECT nbiosubjectdetailcode, ssubjectid, "
				+ "nisthirdpartysharable, nissampleaccesable " + "FROM biosubjectdetails WHERE ssubjectid = ?";

		final String updateSql = "UPDATE biosubjectdetails SET " + "nisthirdpartysharable = ?, "
				+ "nissampleaccesable = ?, " + "dmodifieddate = NOW() " + "WHERE ssubjectid = ?";

		final String controlSql = "SELECT DISTINCT ncontrolcode FROM controlmaster "
				+ "WHERE scontrolname='updatepatientconsent' " + "AND nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		Integer ncontrolcode = null;
		try {
			ncontrolcode = jdbcTemplate.queryForObject(controlSql, Integer.class);
		} catch (Exception e) {
			ncontrolcode = null;
		}

		List<BioSubjectDetails> thirdPartyList = new ArrayList<>();
		List<BioSubjectDetails> sampleAccessibleList = new ArrayList<>();

		for (Map<String, Object> rec : items) {

			Object idObj = rec.get("ssubjectid");
			if (idObj == null)
				continue;

			String ssubjectid = String.valueOf(idObj).trim();

			int newThirdParty = parseIntQuiet(rec.get("nisthirdpartysharable"), 3);
			int newSampleAccess = parseIntQuiet(rec.get("nissampleaccesable"), 3);

			BioSubjectDetails oldData;
			try {
				oldData = jdbcTemplate.queryForObject(selectSql, new BioSubjectDetails(), ssubjectid);
			} catch (Exception ex) {
				continue;
			}

			if (oldData == null)
				continue;

			boolean isThirdPartyChanged = oldData.getNisthirdpartysharable() != newThirdParty;
			boolean isSampleAccessChanged = oldData.getNissampleaccesable() != newSampleAccess;

			if (isThirdPartyChanged || isSampleAccessChanged) {
				jdbcTemplate.update(updateSql, newThirdParty, newSampleAccess, ssubjectid);

				if (isThirdPartyChanged) {
					thirdPartyList.add(oldData);
				}
				if (isSampleAccessChanged) {
					sampleAccessibleList.add(oldData);
				}
			}
		}

		if (ncontrolcode != null && !thirdPartyList.isEmpty()) {

			for (BioSubjectDetails obj : thirdPartyList) {

				Integer nbiosubjectdetailcode = obj.getNbiosubjectdetailcode();
				String referenceId = obj.getSsubjectid();
				Map<String, Object> mailMap = new HashMap<>();
				mailMap.put("ncontrolcode", ncontrolcode);
				mailMap.put("nbiosubjectdetailcode", nbiosubjectdetailcode);
				mailMap.put("ssystemid", referenceId);
				String srevokerightsparam = commonFunction.getMultilingualMessage("IDS_THIRDPARTYACCESSIBILITY",
						userinfo.getSlanguagefilename());
				mailMap.put("srevokerightsparam", srevokerightsparam);

				emailDAOSupport.createEmailAlertTransaction(mailMap, userinfo);
			}
		}

		if (ncontrolcode != null && !sampleAccessibleList.isEmpty()) {
			for (BioSubjectDetails obj : sampleAccessibleList) {

				Integer nbiosubjectdetailcode = obj.getNbiosubjectdetailcode();
				String referenceId = obj.getSsubjectid();

				Map<String, Object> mailMap = new HashMap<>();
				mailMap.put("ncontrolcode", ncontrolcode);
				mailMap.put("nbiosubjectdetailcode", nbiosubjectdetailcode);
				mailMap.put("ssystemid", referenceId);
				String srevokerightsparam = commonFunction.getMultilingualMessage("IDS_SAMPLEACCESSIBILITY",
						userinfo.getSlanguagefilename());
				mailMap.put("srevokerightsparam", srevokerightsparam);

				emailDAOSupport.createEmailAlertTransaction(mailMap, userinfo);
			}
		}

		// On success
		return buildResponse("Success", 200, HttpStatus.OK);

//	        } catch (Exception ex) {
//	            return buildResponse("Internal Server Error", 500, HttpStatus.INTERNAL_SERVER_ERROR);
//	        }
	}

	private ResponseEntity<Object> buildResponse(String message, int nstatus, HttpStatus httpStatus) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("message", message);
		body.put("nstatus", nstatus);
		return new ResponseEntity<>(body, httpStatus);
	}

	private int parseIntQuiet(Object obj, int defaultVal) {
		if (obj == null)
			return defaultVal;
		try {
			if (obj instanceof Number)
				return ((Number) obj).intValue();
			String s = String.valueOf(obj).trim();
			if (s.isEmpty())
				return defaultVal;
			return Integer.parseInt(s);
		} catch (Exception e) {
			return defaultVal;
		}
	}

	@Override
	public ResponseEntity<Object> bioExternalTransferData(UserInfo userInfo) throws Exception {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		final String strBioExternalTransferData = "select nbioexternaltransferdatacode, jsondata from bioexternaltransferdata"
				+ " where nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsentstatus in ("
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.SENTFAILED.gettransactionstatus() + ")";

		List<BioExternalTransferData> lstBioExternalTransferData = jdbcTemplate.query(strBioExternalTransferData,
				new BioExternalTransferData());

		final String strApiEndPoint = "select sapiendpoint, sstatictoken from apiendpoint where napiendpointcode=66 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nreadstatictoken="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();

		final ApiEndPoint objApiEndPoint = (ApiEndPoint) jdbcTemplateUtilityFunction.queryForObject(strApiEndPoint,
				ApiEndPoint.class, jdbcTemplate);

		if (objApiEndPoint == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_APIENDPOINTNOTAVAILABLE",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}

		String token = objApiEndPoint.getSstatictoken();
		if (token != null && !token.isEmpty()) {
			headers.set("Authorization", token);
		}

		if (lstBioExternalTransferData.size() > 0) {
			for (int i = 0; i < lstBioExternalTransferData.size(); i++) {
				JSONArray jsonArray = new JSONArray(lstBioExternalTransferData.get(i).getJsondata());
				String jsonParams = jsonArray.toString();
				Map<String, String> body = Collections.singletonMap("q", jsonParams);
				HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

				String rawEndpoint = objApiEndPoint.getSapiendpoint();
				String fullUrl = rawEndpoint.startsWith("http://") || rawEndpoint.startsWith("https://") ? rawEndpoint
						: "https://" + rawEndpoint;
				URI remoteUrl = new URI(fullUrl);

				SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
				requestFactory.setConnectTimeout(5000); // 5s connect
				requestFactory.setReadTimeout(10000); // 10s read
				RestTemplate restTemplate = new RestTemplate(requestFactory);

				ResponseEntity<Object> resp = restTemplate.postForEntity(remoteUrl, request, Object.class);

				final String strConcat = (resp.getStatusCode() == HttpStatus.OK)
						? (Enumeration.TransactionStatus.SENTSUCCESS.gettransactionstatus() + "")
						: (Enumeration.TransactionStatus.SENTFAILED.gettransactionstatus() + "");
				final String strLogger = (resp.getStatusCode() == HttpStatus.OK) ? "Data Sent Successful..."
						: "Data Sent Failed...";
				final String strUpdateTransferData = "update nbioexternaltransferdatacode set nsentstatus=" + strConcat
						+ ", dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "', ntztransactiondate=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " where nbioexternaltransferdatacode="
						+ lstBioExternalTransferData.get(i).getNbioexternaltransfercode() + " and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(strUpdateTransferData);
				LOGGER.info(strLogger);
			}
		}
		return null;
	}

	@Override
	public ResponseEntity<Object> acknowledgeTransferred(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final String sformNumber = (String) inputMap.get("sformnumber");
		final int nreceivedStatus = (int) inputMap.get("ntransactionstatus");
		final String dreceiveddate = (String) inputMap.get("dreceiveddate");
		final String scomments = (String) inputMap.get("scomments");
		int receivedStatusValue = (nreceivedStatus == Enumeration.TransactionStatus.RECEIVESUCCESS
				.gettransactionstatus()) ? Enumeration.TransactionStatus.RECEIVESUCCESS.gettransactionstatus()
						: Enumeration.TransactionStatus.RECEIVEFAILED.gettransactionstatus();
		final String strLogger = (nreceivedStatus == Enumeration.TransactionStatus.RECEIVESUCCESS
				.gettransactionstatus()) ? "Received Succesfully..." : "Received Failed...";

		final String strBioExternalTransferData = "update bioexternaltransferdate set nreceivedstatus="
				+ receivedStatusValue + ", dreceiveddate='" + dreceiveddate + "', scomments='"
				+ stringUtilityFunction.replaceQuote(scomments) + "', dtransactiondate='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntztransactiondate="
				+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " where nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sformnumber='" + sformNumber
				+ "';";
		jdbcTemplate.execute(strBioExternalTransferData);
		LOGGER.info(strLogger);
		return null;
	}

	@Override
	public ResponseEntity<Object> receiveOriginalSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		jdbcTemplate
				.execute("lock table lockbiothirdpartyreturn " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbiothirdpartyreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		List<Map<String, Object>> lstObj = inputMap.containsKey("samples")
				? (List<Map<String, Object>>) inputMap.get("samples")
				: new ArrayList<>();
		Map<String, Object> rtnMap = new HashMap<String, Object>();

		if (lstObj.size() > 0) {

			String strConditionValue = lstObj.stream().map(item -> "('" + item.get("sformnumber") + "', '"
					+ item.get("sreferencerepoid") + "', " + item.get("nthirdpartycode") + ")")
					.collect(Collectors.joining(","));

			String strFormDetails = "select btpfad.nbiothirdpartyformacceptancedetailscode, btpfad.nbiothirdpartyformacceptancecode,"
					+ " btpfad.nbioprojectcode, btpfad.nbioparentsamplecode, btpfad.ncohortno, btpfad.nstoragetypecode,"
					+ " btpfad.nproductcatcode, btpfad.nproductcode, btpfad.srepositoryid, btpfad.svolume, btpfad.sreceivedvolume,"
					+ " btpfad.ssubjectid, btpfad.sparentsamplecode, btpfad.jsondata, btpfad.ndiagnostictypecode,"
					+ " btpfad.ncontainertypecode, btpfad.nbiosamplereceivingcode, btpfad.nsamplecondition, btpfad.nsamplestatus,"
					+ " btpfad.nreasoncode, btpfad.nsamplestoragetransactioncode, btpfad.nsitecode from biothirdpartyformacceptdetails"
					+ " btpfad join biothirdpartyformaccept btpfa on"
					+ " btpfa.nbiothirdpartyformacceptancecode=btpfad.nbiothirdpartyformacceptancecode and btpfa.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and btpfa.ntransactionstatus="
					+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + " where btpfad.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + " and"
					+ " (btpfa.sformnumber, btpfad.srepositoryid, btpfa.nthirdpartycode) in (" + strConditionValue
					+ ") and btpfa.ntransfertypecode=" + Enumeration.TransferType.BIOBANKEXTERNAL.getntransfertype()
					+ ";";

			// Checking that samples are present in biothirdpartyformacceptdetails table or
			// not
			List<BioThirdPartyFormAcceptDetails> lstTPFormAcceptDetails = jdbcTemplate.query(strFormDetails,
					new BioThirdPartyFormAcceptDetails());

			List<BioThirdPartyFormAcceptDetails> lstTPFormAcceptDetailsFinal = lstTPFormAcceptDetails;
			List<Map<String, Object>> nonExistedSamplesLst = new ArrayList<>();
			String nonExistedRepoId = "";

			if (lstTPFormAcceptDetails.size() > 0) {
				// Checking whether samples are there or not in biothirdpartyformacceptdetails
				// table

				final int ntransSiteCode = lstTPFormAcceptDetails.get(0).getNsitecode();
				userInfo.setNtranssitecode((short) ntransSiteCode);

				List<Map<String, Object>> strAlreadyExistedSamples = jdbcTemplate.queryForList(
						"select btprd.srepositoryid, btprd.jsondata->>'sextractedsampleid' sextractedsampleid from"
								+ " biothirdpartyreturndetails btprd join biothirdpartyreturn btpr on"
								+ " btpr.nbiothirdpartyreturncode=btprd.nbiothirdpartyreturncode and btpr.nsitecode="
								+ userInfo.getNtranssitecode() + " and btpr.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " where (btprd.jsondata->>'sformnumber', btprd.srepositoryid, btpr.nthirdpartycode) in ("
								+ strConditionValue + ") and btprd.nsamplestatus != "
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
								+ " and btprd.nsitecode=" + userInfo.getNtranssitecode() + " and btprd.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

				String strAlreadyExistedRepoId = "";
				if (strAlreadyExistedSamples.size() > 0) {
					// Checking whether samples are already added in biothirdpartyreturndetails
					// table or not

					if (strAlreadyExistedSamples.size() == lstObj.size()) {
						// If all the samples are already transfered

						rtnMap.put("message", "Failed (All Sample(s) Already Exists)");
						rtnMap.put("nstatus", 409);
						LOGGER.info("message: Failed (All Sample(s) Already Exists), nstatus: " + 409);

						return new ResponseEntity<>(rtnMap, HttpStatus.CONFLICT);
					} else {
						strAlreadyExistedRepoId = strAlreadyExistedSamples.stream()
								.map(item -> (String) item.get("srepositoryid")).collect(Collectors.joining(","));

						// Removing already transferred samples and collecting remaining samples to
						// insert in biothirdpartyreturndetails table
						lstTPFormAcceptDetailsFinal = lstTPFormAcceptDetails.stream()
								.filter(pojo -> strAlreadyExistedSamples.stream()
										.map(m -> (String) m.get("srepositoryid"))
										.noneMatch(pojo.getSrepositoryid()::equals))
								.collect(Collectors.toList());
					}

				}

				final String siteTimeZoneDetails = "select s.nsitecode, s.ssitename, s.ssitecode,"
						+ " tz.ntimezonecode, tz.stimezoneid, tz.sdatetimeformat, tz.sgmtoffset from site s join timezone tz"
						+ " on tz.ntimezonecode=s.ntimezonecode and tz.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where nsitecode="
						+ ntransSiteCode + " and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nmastersitecode="
						+ userInfo.getNmastersitecode() + ";";

				// Collecting userinfo details for timezone details based on site
				Map<String, Object> mapSiteTimeZoneDetails = (Map<String, Object>) jdbcTemplate
						.queryForList(siteTimeZoneDetails).get(0);

				userInfo.setSsitecode((String) mapSiteTimeZoneDetails.get("ssiteCode"));
				userInfo.setSdatetimeformat((String) mapSiteTimeZoneDetails.get("sdatetimeformat"));
				userInfo.setNtimezonecode((int) mapSiteTimeZoneDetails.get("ntimezonecode"));
				userInfo.setStimezoneid((String) mapSiteTimeZoneDetails.get("stimezoneid"));

				if (lstObj.size() != lstTPFormAcceptDetails.size()) {

					List<BioThirdPartyFormAcceptDetails> lstTPFormAcceptDetailsDup = lstTPFormAcceptDetails;

					nonExistedSamplesLst = lstObj.stream()
							.filter(lst -> lstTPFormAcceptDetailsDup.stream().map(m -> m.getSrepositoryid())
									.noneMatch(item -> item.equals(lst.get("sreferencerepoid"))))
							.collect(Collectors.toList());

					// Collecting repositoryid which are not in biothirdpartyformacceptdetails table
					// to show in the return value
					nonExistedRepoId = nonExistedSamplesLst.stream().map(item -> (String) item.get("sreferencerepoid"))
							.collect(Collectors.joining(","));

				}

				if (lstTPFormAcceptDetailsFinal.size() == 0 && nonExistedSamplesLst.size() > 0) {
					// No samples are transferring just throws alert for already existed samples and
					// also the samples which are not in biothirdpartyformacceptdetails table
					String alertMsg = " " + nonExistedRepoId + " Doesn't Exist(s) and " + strAlreadyExistedRepoId
							+ " Already Exists";

					rtnMap.put("message", "Failed (" + alertMsg + ")");
					rtnMap.put("nstatus", 409);
					LOGGER.info("message: Failed (" + alertMsg + "), nstatus: " + 409);
					return new ResponseEntity<>(rtnMap, HttpStatus.CONFLICT);
				}

				final String strSeqNo = "select stablename, nsequenceno from seqnobiobankmanagement where stablename in"
						+ " ('biothirdpartyreturn', 'biothirdpartyreturndetails', 'biothirdpartyreturndetailshistory',"
						+ " 'biothirdpartyreturnhistory') and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				List<SeqNoBioBankManagement> lstSeqNo = jdbcTemplate.query(strSeqNo, new SeqNoBioBankManagement());

				Map<String, Object> seqNoMap = new HashMap<String, Object>();
				seqNoMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoBioBankManagement::getStablename,
						SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

				int returnSeqNo = (int) seqNoMap.get("biothirdpartyreturn");
				int returnDetailsSeqNo = (int) seqNoMap.get("biothirdpartyreturndetails");
				int returnHistorySeqNo = (int) seqNoMap.get("biothirdpartyreturnhistory");
				int returnDetailsHistorySeqNo = (int) seqNoMap.get("biothirdpartyreturndetailshistory");

				final String strformat = projectDAOSupport.getSeqfnFormat("biothirdpartyreturn",
						"seqnoformatgeneratorbiobank", 0, 0, userInfo);

				returnSeqNo++;
				String strInsertReturn = "insert into biothirdpartyreturn(nbiothirdpartyreturncode, sthirdpartyreturnformnumber,"
						+ " ntransfertypecode, nformtypecode, nthirdpartycode, noriginsitecode, dreturndate,"
						+ " ntzreturndate, noffsetdreturndate, ntransactionstatus, jsondata, dtransactiondate,"
						+ " ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) select " + returnSeqNo
						+ ", '" + strformat + "', " + Enumeration.TransferType.BIOBANKEXTERNAL.getntransfertype() + ", "
						+ Enumeration.FormType.Transfer.getnformtype() + ", " + lstObj.get(0).get("nthirdpartycode")
						+ ", noriginsitecode, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
						+ userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
						+ ", json_build_object('sremarks', ''," + " 'soriginsitename', jsondata->>'soriginsitename'), '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", nsitecode, "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biothirdpartyformaccept where sformnumber='" + lstObj.get(0).get("sformnumber")
						+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				strInsertReturn += " update seqnobiobankmanagement set nsequenceno=" + returnSeqNo + " where"
						+ " stablename='biothirdpartyreturn' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				returnHistorySeqNo++;
				String strInsertReturnHistory = "insert into biothirdpartyreturnhistory (nbiothirdpartyreturnhistorycode,"
						+ " nbiothirdpartyreturncode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
						+ " noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode,"
						+ " nsitecode, nstatus) values (" + returnHistorySeqNo + ", " + returnSeqNo + ", "
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
				strInsertReturnHistory += " update seqnobiobankmanagement set nsequenceno=" + returnHistorySeqNo
						+ " where" + " stablename='biothirdpartyreturnhistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				String strInsertReturnDetails = "insert into biothirdpartyreturndetails (nbiothirdpartyreturndetailscode,"
						+ " nbiothirdpartyreturncode, nbiothirdpartyformacceptancecode, nbiothirdpartyformacceptancedetailscode, nbioprojectcode,"
						+ " nbioparentsamplecode, ncohortno, nstoragetypecode, nproductcatcode, nproductcode, "
						+ " svolume, sreturnvolume, jsondata, ndiagnostictypecode, ncontainertypecode,"
						+ " nbiosamplereceivingcode, nsamplecondition, srepositoryid, "
						+ " nsamplestatus, nreasoncode, nsamplestoragetransactioncode, nisexternalsample,"
						+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) values ";

				String strInsertReturnDetailsHistory = "insert into biothirdpartyreturndetailshistory"
						+ " (nbiothirdpartyreturndetailshistorycode, nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition,"
						+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
						+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) values ";

				String strValueReturnDetails = "";
				String strValueReturnDetailsHistory = "";

				for (BioThirdPartyFormAcceptDetails mapObj : lstTPFormAcceptDetailsFinal) {

					Map<String, Object> matchedMap = lstObj.stream()
							.filter(m -> m.get("sreferencerepoid").equals(mapObj.getSrepositoryid())).findFirst()
							.orElse(null);

					final String seluent = (matchedMap.get("seluent").equals("")
							|| matchedMap.get("seluent").equals(null)) ? null : "'" + matchedMap.get("seluent") + "'";
					final String sqcPlatform = (matchedMap.get("sqcplatform").equals("")
							|| matchedMap.get("sqcplatform").equals(null)) ? null
									: "'" + matchedMap.get("sqcplatform") + "'";
					final String sconcentration = (matchedMap.get("sconcentration").equals("")
							|| matchedMap.get("sconcentration").equals(null)) ? null
									: "'" + matchedMap.get("sconcentration") + "'";
					final String sextractedSampleId = (matchedMap.get("sextractedsampleid").equals("")
							|| matchedMap.get("sextractedsampleid").equals(null)) ? null
									: "'" + matchedMap.get("sextractedsampleid") + "'";

					returnDetailsSeqNo++;
					strValueReturnDetails += "(" + returnDetailsSeqNo + ", " + returnSeqNo + ", "
							+ mapObj.getNbiothirdpartyformacceptancecode() + ", "
							+ mapObj.getNbiothirdpartyformacceptancedetailscode() + ", " + mapObj.getNbioprojectcode()
							+ ", " + mapObj.getNbioparentsamplecode() + ", " + mapObj.getNcohortno() + ", "
							+ mapObj.getNstoragetypecode() + ", " + mapObj.getNproductcatcode() + ", "
							+ matchedMap.get("nproductcode") + ", '" + mapObj.getSvolume() + "', '"
							+ matchedMap.get("svolume") + "', jsonb_build_object('seluent', " + seluent
							+ ", 'svolume', '" + mapObj.getSvolume() + "', 'sreturnvolume', '"
							+ matchedMap.get("svolume") + "', 'scasetype', '" + mapObj.getJsondata().get("scasetype")
							+ "', 'ssubjectid', '" + mapObj.getSsubjectid() + "', 'sformnumber', '"
							+ matchedMap.get("sformnumber") + "', 'sqcplatform', " + sqcPlatform + ", 'nproductcode', "
							+ matchedMap.get("nproductcode") + ", 'sconcentration', " + sconcentration
							+ ", 'noriginsitecode', " + matchedMap.get("noriginsitecode") + ", 'nthirdpartycode', "
							+ matchedMap.get("nthirdpartycode") + ", 'sreferencerepoid', '"
							+ matchedMap.get("sreferencerepoid") + "'," + " 'sparentsamplecode', '"
							+ matchedMap.get("sparentsamplecode") + "', 'sextractedsampleid', " + sextractedSampleId
							+ "), " + mapObj.getNdiagnostictypecode() + ", " + mapObj.getNcontainertypecode() + ", "
							+ mapObj.getNbiosamplereceivingcode() + ", " + mapObj.getNsamplecondition() + ", '"
							+ matchedMap.get("sreferencerepoid") + "', "
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", -1, "
							+ mapObj.getNsamplestoragetransactioncode() + ", "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ mapObj.getNsitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "),";

					returnDetailsHistorySeqNo++;
					strValueReturnDetailsHistory += "(" + returnDetailsHistorySeqNo + ", " + returnDetailsSeqNo + ", "
							+ returnSeqNo + ", " + mapObj.getNsamplecondition() + ", "
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
							+ mapObj.getNsitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ "),";

				}
				strValueReturnDetails = lstTPFormAcceptDetailsFinal.size() >0 ? strValueReturnDetails.substring(0, strValueReturnDetails.length() - 1) + ";":"";
				strValueReturnDetailsHistory = lstTPFormAcceptDetailsFinal.size() >0 ? strValueReturnDetailsHistory.substring(0,
						strValueReturnDetailsHistory.length() - 1) + ";":"";

				strValueReturnDetails += " update seqnobiobankmanagement set nsequenceno=" + returnDetailsSeqNo
						+ " where stablename='biothirdpartyreturndetails' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				strValueReturnDetailsHistory += " update seqnobiobankmanagement set nsequenceno="
						+ returnDetailsHistorySeqNo
						+ " where stablename='biothirdpartyreturndetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				jdbcTemplate.execute(strInsertReturn + strInsertReturnHistory + strInsertReturnDetails
						+ strValueReturnDetails + strInsertReturnDetailsHistory + strValueReturnDetailsHistory);

				String additionMsg = "";

				if (!nonExistedRepoId.isEmpty() && !strAlreadyExistedRepoId.isEmpty()) {
					// When some samples are transferred and some samples are already transferred
					// and remaining samples are not in biothirdpartyformacceptdetails table
					additionMsg = " (" + nonExistedRepoId + " Doesn't Exist(s) and " + strAlreadyExistedRepoId
							+ " Already Exists)";

				} else if (!nonExistedRepoId.isEmpty()) {
					// When some samples are transferred and remaining samples are not in
					// biothirdpartyformacceptdetails table
					additionMsg = " (" + nonExistedRepoId + " Doesn't Exist(s))";

				} else if (!strAlreadyExistedRepoId.isEmpty()) {
					// When some samples are transferred and remaining samples are already
					// transferred
					additionMsg = " (" + strAlreadyExistedRepoId + " Already Exists)";
				}
				rtnMap.put("message", "Success" + additionMsg + "");
				rtnMap.put("nstatus", 200);
				LOGGER.info("message: Success" + additionMsg + ", nstatus: " + 200);
				return new ResponseEntity<>(rtnMap, HttpStatus.OK);
			} else {
				nonExistedRepoId = lstTPFormAcceptDetails.stream().map(m -> m.getSextractedsampleid())
						.collect(Collectors.joining(","));
				rtnMap.put("message", "Failed (" + nonExistedRepoId + " Doesn't Exist(s))");
				rtnMap.put("nstatus", 404);
				LOGGER.info("message: Failed (" + nonExistedRepoId + " Doesn't Exist(s)), nstatus: " + 404);
				return new ResponseEntity<>(rtnMap, HttpStatus.NOT_FOUND);
			}

		} else {
			rtnMap.put("message", "Failed (No Sample(s) Found)");
			rtnMap.put("nstatus", 404);
			LOGGER.info("message: Failed (No Sample(s) Found), nstatus: " + 404);
			return new ResponseEntity<>(rtnMap, HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public String getAuthorisationTokenForNGS(UserInfo userinfo) throws Exception {

		final String strExternalUrlSettings = "select sexternalurl, jsondata from externalurlsettings where nexternalurlsettingcode=1 and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userinfo.getNmastersitecode();

		final ExternalUrlSettings objExternalUrlSettings = (ExternalUrlSettings) jdbcTemplateUtilityFunction
				.queryForObject(strExternalUrlSettings, ExternalUrlSettings.class, jdbcTemplate);

		if (objExternalUrlSettings == null) {
			return "IDS_URLSETTINGSUNAVAILABLE";
		}
		String sExternalUrl = objExternalUrlSettings.getSexternalurl();
		Map<String, Object> jsonMap = objExternalUrlSettings.getJsondata();
		if (sExternalUrl == null || jsonMap == null || jsonMap.isEmpty()) {
			return "IDS_INVALIDURLSETTINGS";
		}

		// Parse request body JSON
		Object bodyObj = jsonMap.get("requestbody");
		if (bodyObj == null) {
			return "IDS_REQUESTBODYUNAVAILABLE";
		}

		String requestBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(bodyObj);

		// Build HTTP POST Request
		org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
		headers.add("Content-Type", "application/x-amz-json-1.1");
		headers.add("X-Amz-Target", "AWSCognitoIdentityProviderService.InitiateAuth");

		org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(requestBody,
				headers);

		org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
		org.springframework.http.ResponseEntity<String> response = null;

		try {
			response = restTemplate.postForEntity(sExternalUrl, entity, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return "IDS_AUTHREQUESTFAILED";
		}

		if (response == null || response.getBody() == null) {
			return "IDS_EMPTYRESPONSE";
		}

		// Parse response to extract token
		com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper()
				.readTree(response.getBody());

		if (root.has("AuthenticationResult") && root.get("AuthenticationResult").has("AccessToken")) {
			return root.get("AuthenticationResult").get("AccessToken").asText();
		}

		return "IDS_TOKENNOTAVAILABLE";
	}

	
	@Override
	public ResponseEntity<Object> sendAcceptedThirdPartySamplestoNGS(final Map<String, Object> inputMap,
	        final UserInfo userInfo) throws Exception {

	    final Map<String, Object> outputMap = new LinkedHashMap<>();

	    final Object codeObj = inputMap.get("nbiothirdpartyformacceptancecode");
	    if (codeObj == null) {
	        return new ResponseEntity<>(
	                commonFunction.getMultilingualMessage("IDS_INVALIDFORM", userInfo.getSlanguagefilename()),
	                HttpStatus.EXPECTATION_FAILED);
	    }

	    final int nbiothirdpartyformacceptancecode = Integer.parseInt(String.valueOf(codeObj));

	    final String strQuery = "SELECT "
	            + "  a.nbiothirdpartyformacceptancecode, "
	            + "  (u.sfirstname || ' ' || u.slastname) AS sendername, "
	            + "  s.ssitename AS hubname, "
	            + "  d.srepositoryid AS biorepositoryid, "
	            + "  d.jsondata->>'ssubjectid' AS subjectid, "
	            + "  d.jsondata->>'sparentsamplecode' AS samplecode, "
	            + "  p.sproductname AS sampletype, "
	            + "  d.jsondata->>'scasetype' AS casetype, "
	            + "  d.sreceivedvolume AS samplevolume, "
	            + "  a.sformnumber AS formnumber, "
	            + "  a.noriginsitecode AS originsitecode, "
	            + "  a.nthirdpartycode AS thirdpartycode, "
	            + "  d.nproductcode AS productcode, "
	            + "  NULLIF(d.jsondata->>'sextractedsampleid','') AS extractedsampleid, "
	            + "  NULLIF(d.jsondata->>'sconcentration','') AS concentration, "
	            + "  d.jsondata->>'sqcplatform' AS qcplatform, "
	            + "  d.jsondata->>'seluent' AS eluent "
	            + "FROM biothirdpartyformaccept a "
	            + "JOIN biothirdpartyformacceptdetails d "
	            + "  ON d.nbiothirdpartyformacceptancecode = a.nbiothirdpartyformacceptancecode "
	            + "LEFT JOIN site s "
	            + "  ON s.nsitecode = a.noriginsitecode "
	            + "  AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
	            + "  AND s.nmastersitecode = " + userInfo.getNmastersitecode() + " "
	            + "LEFT JOIN users u "
	            + "  ON u.nusercode = a.nsenderusercode "
	            + "  AND u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
	            + "  AND u.nsitecode = " + userInfo.getNmastersitecode() + " "
	            + "LEFT JOIN product p "
	            + "  ON p.nproductcode = d.nproductcode "
	            + "  AND p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
	            + "  AND p.nsitecode = " + userInfo.getNmastersitecode() + " "
	            + "WHERE a.nbiothirdpartyformacceptancecode = " + nbiothirdpartyformacceptancecode + " "
	            + "  AND a.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
	            + "  AND d.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
	            + "  AND a.nsitecode = " + userInfo.getNtranssitecode() + " "
	            + "  AND d.nsitecode = " + userInfo.getNtranssitecode() + " "
	            + "  AND d.nsamplecondition = " + Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + " "
	            + "  AND d.nsamplestatus = " + Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + " ";

	    final List<Map<String, Object>> rows = jdbcTemplate.queryForList(strQuery);

	    final List<Map<String, Object>> lstRawSamples = new ArrayList<>();
	    final List<Map<String, Object>> lstDNASamples = new ArrayList<>();

	    for (Map<String, Object> r : rows) {
	        final String extractedSampleId = asString(r.get("extractedsampleid"));

	        if (extractedSampleId != null && !extractedSampleId.isEmpty()) {
	            // DNA
	            final Map<String, Object> dna = new LinkedHashMap<>();
	            dna.put("SenderName", asString(r.get("sendername")));
	            dna.put("HubName", asString(r.get("hubname")));
	            dna.put("BiorepositoryId", asString(r.get("biorepositoryid")));
	            dna.put("SubjectId", asString(r.get("subjectid")));
	            dna.put("SampleCode", asString(r.get("samplecode")));
	            dna.put("SampleType", asString(r.get("sampletype")));
	            dna.put("CaseType", asString(r.get("casetype")));
	            dna.put("SampleVolume", asString(r.get("samplevolume")));
	            dna.put("FormNumber", asString(r.get("formnumber")));
	            dna.put("OriginSiteCode", asString(r.get("originsitecode")));
	            dna.put("ThirdPartyCode", asString(r.get("thirdpartycode")));
	            dna.put("ProductCode", asString(r.get("productcode")));
	            dna.put("ExtractedSampleId", asString(r.get("extractedsampleid")));
	            dna.put("Concentration", asString(r.get("concentration")));
	            dna.put("QCPlatform", asString(r.get("qcplatform")));
	            dna.put("Eluent", asString(r.get("eluent")));
	            lstDNASamples.add(dna);
	        } else {
	            // Raw
	            final Map<String, Object> raw = new LinkedHashMap<>();
	            raw.put("SenderName", asString(r.get("sendername")));
	            raw.put("HubName", asString(r.get("hubname")));
	            raw.put("BiorepositoryId", asString(r.get("biorepositoryid")));
	            raw.put("SubjectId", asString(r.get("subjectid")));
	            raw.put("SampleCode", asString(r.get("samplecode")));
	            raw.put("SampleType", asString(r.get("sampletype")));
	            raw.put("CaseType", asString(r.get("casetype")));
	            raw.put("SampleVolume", asString(r.get("samplevolume")));
	            raw.put("FormNumber", asString(r.get("formnumber")));
	            raw.put("OriginSiteCode", asString(r.get("originsitecode")));
	            raw.put("ThirdPartyCode", asString(r.get("thirdpartycode")));
	            raw.put("ProductCode", asString(r.get("productcode")));
	            lstRawSamples.add(raw);
	        }
	    }

	    // 1) Get token via your existing helper
	    final String token = getAuthorisationTokenForNGS(userInfo);
	    if (token == null || token.trim().isEmpty() || token.startsWith("IDS_")) {
	        final String ids = (token == null || token.trim().isEmpty()) ? "IDS_TOKENNOTAVAILABLE" : token;
	        return new ResponseEntity<>(commonFunction.getMultilingualMessage(ids, userInfo.getSlanguagefilename()),
	                HttpStatus.EXPECTATION_FAILED);
	    }

	    // 2) Prepare HTTP client & headers
	    final org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
	    final org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
	    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
	    headers.setBearerAuth(token);

	    final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

	    final String strExternalUrlSettingsQuery = "SELECT "
	            + "  nexternalurlsettingcode, "
	            + "  sexternalurl "
	            + "FROM externalurlsettings "
	            + "WHERE nexternalurlsettingcode IN (2, 3) "
	            + "  AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
	            + "  AND nsitecode = " + userInfo.getNmastersitecode();

	    final List<ExternalUrlSettings> lstSettings = jdbcTemplate.query(strExternalUrlSettingsQuery,
	            new ExternalUrlSettings());

	    String rawUrl = null;
	    String dnaUrl = null;

	    for (ExternalUrlSettings s : lstSettings) {
	        int code = s.getNexternalurlsettingcode();
	        if (code == 2) {
	            rawUrl = s.getSexternalurl();
	        } else if (code == 3) {
	            dnaUrl = s.getSexternalurl();
	        }
	    }

	    if (rawUrl == null || dnaUrl == null) {
	        return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_URLSETTINGSUNAVAILABLE",
	                userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
	    }

	    final String RAW_URL = rawUrl;
	    final String DNA_URL = dnaUrl;

	    // 3) POST Raw samples (if any)
	    if (!lstRawSamples.isEmpty()) {
	        try {
	            final String body = mapper.writeValueAsString(lstRawSamples);
	            final org.springframework.http.HttpEntity<String> entity =
	                    new org.springframework.http.HttpEntity<>(body, headers);

	            // For 2xx, this returns normally; for 4xx/5xx it throws HttpStatusCodeException
	            final org.springframework.http.ResponseEntity<String> resp =
	                    restTemplate.postForEntity(RAW_URL, entity, String.class);

	            // If you ever customize error handler to not throw on 4xx/5xx
	            if (resp == null || !resp.getStatusCode().is2xxSuccessful()) {
	                String fallbackMsg = commonFunction.getMultilingualMessage("IDS_NGSRAWSAMPLESSYNCFAILED",
	                        userInfo.getSlanguagefilename());
	                String apiMsg = null;
	                try {
	                    if (resp != null && resp.getBody() != null) {
	                        Object respbody = resp.getBody();
	                        if (respbody instanceof String) {
	                            final ObjectMapper objMapper = new ObjectMapper();
	                            Map<String, Object> map = objMapper.readValue((String) respbody, Map.class);
	                            apiMsg = extractMessageFromApi(map);
	                        } else if (respbody instanceof Map) {
	                            apiMsg = extractMessageFromApi((Map<String, Object>) respbody);
	                        }
	                    }
	                } catch (Exception ex) {
	                    apiMsg = null;
	                }
	                String finalMsg = (apiMsg != null && !apiMsg.trim().isEmpty()) ? apiMsg : fallbackMsg;
	                return new ResponseEntity<>(finalMsg, HttpStatus.EXPECTATION_FAILED);
	            }

	        } catch (HttpStatusCodeException ex) {
	            // Here we capture 4xx / 5xx (including 404, 417, etc.) and parse response body
	            String fallbackMsg = commonFunction.getMultilingualMessage("IDS_NGSRAWSAMPLESSYNCFAILED",
	                    userInfo.getSlanguagefilename());
	            String apiMsg = null;
	            try {
	                String responseBody = ex.getResponseBodyAsString();
	                if (responseBody != null && !responseBody.trim().isEmpty()) {
	                    final ObjectMapper objMapper = new ObjectMapper();
	                    Map<String, Object> map = objMapper.readValue(responseBody, Map.class);
	                    apiMsg = extractMessageFromApi(map);
	                }
	            } catch (Exception e2) {
	                apiMsg = null;
	            }
	            String finalMsg = (apiMsg != null && !apiMsg.trim().isEmpty()) ? apiMsg : fallbackMsg;
	            return new ResponseEntity<>(finalMsg, HttpStatus.EXPECTATION_FAILED);

	        } catch (Exception ex) {
	            ex.printStackTrace();
	            return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NGSRAWSAMPLESSYNCFAILED",
	                    userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
	        }
	    }

	    // 4) POST DNA samples (if any)
	    if (!lstDNASamples.isEmpty()) {
	        try {
	            final String body = mapper.writeValueAsString(lstDNASamples);
	            final org.springframework.http.HttpEntity<String> entity =
	                    new org.springframework.http.HttpEntity<>(body, headers);

	            final org.springframework.http.ResponseEntity<String> resp =
	                    restTemplate.postForEntity(DNA_URL, entity, String.class);

	            if (resp == null || !resp.getStatusCode().is2xxSuccessful()) {
	                String fallbackMsg = commonFunction.getMultilingualMessage("IDS_NGSDNASAMPLESSYNCFAILED",
	                        userInfo.getSlanguagefilename());
	                String apiMsg = null;
	                try {
	                    if (resp != null && resp.getBody() != null) {
	                        Object respbody = resp.getBody();
	                        if (respbody instanceof String) {
	                            final ObjectMapper objMapper = new ObjectMapper();
	                            Map<String, Object> map = objMapper.readValue((String) respbody, Map.class);
	                            apiMsg = extractMessageFromApi(map);
	                        } else if (respbody instanceof Map) {
	                            apiMsg = extractMessageFromApi((Map<String, Object>) respbody);
	                        }
	                    }
	                } catch (Exception ex) {
	                    apiMsg = null;
	                }
	                String finalMsg = (apiMsg != null && !apiMsg.trim().isEmpty()) ? apiMsg : fallbackMsg;
	                return new ResponseEntity<>(finalMsg, HttpStatus.EXPECTATION_FAILED);
	            }

	        } catch (HttpStatusCodeException ex) {
	            String fallbackMsg = commonFunction.getMultilingualMessage("IDS_NGSDNASAMPLESSYNCFAILED",
	                    userInfo.getSlanguagefilename());
	            String apiMsg = null;
	            try {
	                String responseBody = ex.getResponseBodyAsString();
	                if (responseBody != null && !responseBody.trim().isEmpty()) {
	                    final ObjectMapper objMapper = new ObjectMapper();
	                    Map<String, Object> map = objMapper.readValue(responseBody, Map.class);
	                    apiMsg = extractMessageFromApi(map);
	                }
	            } catch (Exception e2) {
	                apiMsg = null;
	            }
	            String finalMsg = (apiMsg != null && !apiMsg.trim().isEmpty()) ? apiMsg : fallbackMsg;
	            return new ResponseEntity<>(finalMsg, HttpStatus.EXPECTATION_FAILED);

	        } catch (Exception ex) {
	            ex.printStackTrace();
	            return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NGSDNASAMPLESSYNCFAILED",
	                    userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
	        }
	    }

	    // 5) Build success payload (echo what we sent)
	    outputMap.put("nbiothirdpartyformacceptancecode", nbiothirdpartyformacceptancecode);
	    outputMap.put("lstRawSamples", lstRawSamples.isEmpty() ? null : lstRawSamples);
	    outputMap.put("lstDNASamples", lstDNASamples.isEmpty() ? null : lstDNASamples);

	    return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	private static String asString(Object o) {
	    return (o == null) ? null : String.valueOf(o);
	}

	@SuppressWarnings("unchecked")
	private String extractMessageFromApi(Map<String, Object> map) {
	    try {
	        if (map.containsKey("detail")) {
	            Object detail = map.get("detail");

	            if (detail instanceof Map) {
	                Map<String, Object> detailMap = (Map<String, Object>) detail;

	                if (detailMap.containsKey("message")) {
	                    return String.valueOf(detailMap.get("message"));
	                }
	            }
	        }
	    } catch (Exception e) {
	        return null;
	    }
	    return null;
	}
	
	/* commentted by abudul regarding the exception catching issue fix
	@Override
	public ResponseEntity<Object> sendAcceptedThirdPartySamplestoNGS(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<>();

		final Object codeObj = inputMap.get("nbiothirdpartyformacceptancecode");
		if (codeObj == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_INVALIDFORM", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final int nbiothirdpartyformacceptancecode = Integer.parseInt(String.valueOf(codeObj));

		final String strQuery = "SELECT " + "  a.nbiothirdpartyformacceptancecode, "
				+ "  (u.sfirstname || ' ' || u.slastname) AS sendername, " + "  s.ssitename AS hubname, "
				+ "  d.srepositoryid AS biorepositoryid, " + "  d.jsondata->>'ssubjectid' AS subjectid, "
				+ "  d.jsondata->>'sparentsamplecode' AS samplecode, " + "  p.sproductname AS sampletype, "
				+ "  d.jsondata->>'scasetype' AS casetype, " + "  d.sreceivedvolume AS samplevolume, "
				+ "  a.sformnumber AS formnumber, " + "  a.noriginsitecode AS originsitecode, "
				+ "  a.nthirdpartycode AS thirdpartycode, " + "  d.nproductcode AS productcode, "
				+ "  NULLIF(d.jsondata->>'sextractedsampleid','') AS extractedsampleid, "
				+ "  NULLIF(d.jsondata->>'sconcentration','') AS concentration, "
				+ "  d.jsondata->>'sqcplatform' AS qcplatform, " + "  d.jsondata->>'seluent' AS eluent "
				+ "FROM biothirdpartyformaccept a " + "JOIN biothirdpartyformacceptdetails d "
				+ "  ON d.nbiothirdpartyformacceptancecode = a.nbiothirdpartyformacceptancecode " + "LEFT JOIN site s "
				+ "  ON s.nsitecode = a.noriginsitecode " + "  AND s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND s.nmastersitecode = "
				+ userInfo.getNmastersitecode() + " " + "LEFT JOIN users u " + "  ON u.nusercode = a.nsenderusercode "
				+ "  AND u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "  AND u.nsitecode = " + userInfo.getNmastersitecode() + " " + "LEFT JOIN product p "
				+ "  ON p.nproductcode = d.nproductcode " + "  AND p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND p.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + "WHERE a.nbiothirdpartyformacceptancecode = "
				+ nbiothirdpartyformacceptancecode + " " + "  AND a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND a.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + "  AND d.nsitecode = " + userInfo.getNtranssitecode() + " "
				+ "  AND d.nsamplecondition = " + Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + " "
				+ "  AND d.nsamplestatus = " + Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + " ";

		final List<Map<String, Object>> rows = jdbcTemplate.queryForList(strQuery);

		final List<Map<String, Object>> lstRawSamples = new ArrayList<>();
		final List<Map<String, Object>> lstDNASamples = new ArrayList<>();

		for (Map<String, Object> r : rows) {
			final String extractedSampleId = asString(r.get("extractedsampleid"));

			if (extractedSampleId != null && !extractedSampleId.isEmpty()) {
				// DNA
				final Map<String, Object> dna = new LinkedHashMap<>();
				dna.put("SenderName", asString(r.get("sendername")));
				dna.put("HubName", asString(r.get("hubname")));
				dna.put("BiorepositoryId", asString(r.get("biorepositoryid")));
				dna.put("SubjectId", asString(r.get("subjectid")));
				dna.put("SampleCode", asString(r.get("samplecode")));
				dna.put("SampleType", asString(r.get("sampletype")));
				dna.put("CaseType", asString(r.get("casetype")));
				dna.put("SampleVolume", asString(r.get("samplevolume")));
				dna.put("FormNumber", asString(r.get("formnumber")));
				dna.put("OriginSiteCode", asString(r.get("originsitecode")));
				dna.put("ThirdPartyCode", asString(r.get("thirdpartycode")));
				dna.put("ProductCode", asString(r.get("productcode")));
				dna.put("ExtractedSampleId", asString(r.get("extractedsampleid")));
				dna.put("Concentration", asString(r.get("concentration")));
				dna.put("QCPlatform", asString(r.get("qcplatform")));
				dna.put("Eluent", asString(r.get("eluent")));
				lstDNASamples.add(dna);
			} else {
				// Raw
				final Map<String, Object> raw = new LinkedHashMap<>();
				raw.put("SenderName", asString(r.get("sendername")));
				raw.put("HubName", asString(r.get("hubname")));
				raw.put("BiorepositoryId", asString(r.get("biorepositoryid")));
				raw.put("SubjectId", asString(r.get("subjectid")));
				raw.put("SampleCode", asString(r.get("samplecode")));
				raw.put("SampleType", asString(r.get("sampletype")));
				raw.put("CaseType", asString(r.get("casetype")));
				raw.put("SampleVolume", asString(r.get("samplevolume")));
				raw.put("FormNumber", asString(r.get("formnumber")));
				raw.put("OriginSiteCode", asString(r.get("originsitecode")));
				raw.put("ThirdPartyCode", asString(r.get("thirdpartycode")));
				raw.put("ProductCode", asString(r.get("productcode")));
				lstRawSamples.add(raw);
			}
		}

		// 1) Get token via your existing helper
		final String token = getAuthorisationTokenForNGS(userInfo);
		if (token == null || token.trim().isEmpty() || token.startsWith("IDS_")) {
			final String ids = (token == null || token.trim().isEmpty()) ? "IDS_TOKENNOTAVAILABLE" : token;
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(ids, userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		// 2) Prepare HTTP client & headers
		final org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
		final org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
		headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token);

		final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

		final String strExternalUrlSettingsQuery = "SELECT " + "  nexternalurlsettingcode, " + "  sexternalurl "
				+ "FROM externalurlsettings " + "WHERE nexternalurlsettingcode IN (2, 3) " + "  AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND nsitecode = "
				+ userInfo.getNmastersitecode();

		final List<ExternalUrlSettings> lstSettings = jdbcTemplate.query(strExternalUrlSettingsQuery,
				new ExternalUrlSettings());

		String rawUrl = null;
		String dnaUrl = null;

		for (ExternalUrlSettings s : lstSettings) {
			int code = s.getNexternalurlsettingcode();
			if (code == 2) {
				rawUrl = s.getSexternalurl();
			} else if (code == 3) {
				dnaUrl = s.getSexternalurl();
			}
		}

		if (rawUrl == null || dnaUrl == null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_URLSETTINGSUNAVAILABLE",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

		final String RAW_URL = rawUrl;
		final String DNA_URL = dnaUrl;

		// 3) POST Raw samples (if any)
		if (!lstRawSamples.isEmpty()) {
			try {
				final String body = mapper.writeValueAsString(lstRawSamples);
				final org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(
						body, headers);
				final org.springframework.http.ResponseEntity<String> resp = restTemplate.postForEntity(RAW_URL, entity,
						String.class);

				if (resp == null || !resp.getStatusCode().is2xxSuccessful()) {
					String fallbackMsg = commonFunction.getMultilingualMessage("IDS_NGSRAWSAMPLESSYNCFAILED",
							userInfo.getSlanguagefilename());
					String apiMsg = null;
					try {
						if (resp != null && resp.getBody() != null) {
							Object respbody = resp.getBody();
							if (respbody instanceof String) {
								final ObjectMapper objMapper = new ObjectMapper();
								Map<String, Object> map = objMapper.readValue((String) respbody, Map.class);
								apiMsg = extractMessageFromApi(map);
							} else if (respbody instanceof Map) {
								apiMsg = extractMessageFromApi((Map<String, Object>) respbody);
							}
						}
					} catch (Exception ex) {
						apiMsg = null;
					}
					String finalMsg = (apiMsg != null && !apiMsg.trim().isEmpty()) ? apiMsg : fallbackMsg;
					return new ResponseEntity<>(finalMsg, HttpStatus.EXPECTATION_FAILED);
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NGSRAWSAMPLESSYNCFAILED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}

		// 4) POST DNA samples (if any)
		if (!lstDNASamples.isEmpty()) {
			try {
				final String body = mapper.writeValueAsString(lstDNASamples);
				final org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(
						body, headers);
				final org.springframework.http.ResponseEntity<String> resp = restTemplate.postForEntity(DNA_URL, entity,
						String.class);
				if (resp == null || !resp.getStatusCode().is2xxSuccessful()) {
					String fallbackMsg = commonFunction.getMultilingualMessage("IDS_NGSDNASAMPLESSYNCFAILED",
							userInfo.getSlanguagefilename());
					String apiMsg = null;
					try {
						if (resp != null && resp.getBody() != null) {
							Object respbody = resp.getBody();
							if (respbody instanceof String) {
								final ObjectMapper objMapper = new ObjectMapper();
								Map<String, Object> map = objMapper.readValue((String) respbody, Map.class);
								apiMsg = extractMessageFromApi(map);
							} else if (respbody instanceof Map) {
								apiMsg = extractMessageFromApi((Map<String, Object>) respbody);
							}
						}
					} catch (Exception ex) {
						apiMsg = null;
					}
					String finalMsg = (apiMsg != null && !apiMsg.trim().isEmpty()) ? apiMsg : fallbackMsg;
					return new ResponseEntity<>(finalMsg, HttpStatus.EXPECTATION_FAILED);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NGSDNASAMPLESSYNCFAILED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}

		// 5) Build success payload (echo what we sent)
		outputMap.put("nbiothirdpartyformacceptancecode", nbiothirdpartyformacceptancecode);
		outputMap.put("lstRawSamples", lstRawSamples.isEmpty() ? null : lstRawSamples);
		outputMap.put("lstDNASamples", lstDNASamples.isEmpty() ? null : lstDNASamples);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	private static String asString(Object o) {
		return (o == null) ? null : String.valueOf(o);
	}

	private String extractMessageFromApi(Map<String, Object> map) {
		try {
			if (map.containsKey("detail")) {
				Object detail = map.get("detail");

				if (detail instanceof Map) {
					Map<String, Object> detailMap = (Map<String, Object>) detail;

					if (detailMap.containsKey("message")) {
						return String.valueOf(detailMap.get("message"));
					}
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}
*/
	@Override
	public ResponseEntity<Object> getTotalRepoIdBasedSampleType(final Map<String, Object> inputMap) throws Exception {
		List<Map<String, Object>> items = new ArrayList<>();
		List<Object> errors = new ArrayList<>();
		final String diseaseCategoryName = inputMap.containsKey("disease_category")
				? cleanObj(inputMap.get("disease_category"))
				: null;

		final String diseaseName = inputMap.containsKey("disease") ? cleanObj(inputMap.get("disease")) : null;

		final String projectTitle = inputMap.containsKey("project") ? cleanObj(inputMap.get("project")) : null;

//		final String caseType = inputMap.containsKey("caseType") ? cleanObj(inputMap.get("caseType")) : null;

		final String fromCollectionDate = inputMap.containsKey("fromCollectionDate")
				? cleanObj(inputMap.get("fromCollectionDate"))
				: null;

		final String toCollectionDate = inputMap.containsKey("toCollectionDate")
				? cleanObj(inputMap.get("toCollectionDate"))
				: null;
		var strQuery = "";
		final String status1 = inputMap.containsKey("status") ? cleanObj(inputMap.get("status")) : null;
		Integer status = null;

		if (status1 != null && !status1.isBlank()) {
			String sql = "SELECT ntranscode FROM transactionstatus WHERE LOWER(stransstatus) = LOWER('"
					+ stringUtilityFunction.replaceQuote(status1.trim()) + "')";
			try {
				status = jdbcTemplate.queryForObject(sql, Integer.class);
			} catch (Exception ex) {
				status = null;
				errors.add("Query execution failed: " + ex.getMessage());

			}
		}

		if (status == Enumeration.TransactionStatus.ALIQUOTED.gettransactionstatus()) {

			strQuery = "SELECT dc.sdiseasecategoryname AS disease_category," + " d.sdiseasename AS disease,"
					+ " bp.sprojecttitle AS project," + " bpsr.scasetype AS case_type,"
					+ " p.sproductname || ' (' || pc.sproductcatname || ')' AS specimen_type,"
					+ " COUNT(bsc.srepositoryid) AS count_id_repository , 'aliquoted' as status "
					+ " FROM bioparentsamplereceiving bpsr"
					+ " JOIN public.bioparentsamplecollection bpsc ON bpsc.nbioparentsamplecode=bpsr.nbioparentsamplecode "
					+ "     AND bpsc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "     AND bpsr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN public.biosamplereceiving bsc ON bsc.nbioparentsamplecollectioncode=bpsc.nbioparentsamplecollectioncode "
					+ "     AND bsc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN public.bioproject bp ON bpsr.nbioprojectcode=bp.nbioprojectcode " + "     AND bp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN public.product p ON bsc.nproductcode=p.nproductcode " + "     AND p.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN public.productcategory pc ON p.nproductcatcode=pc.nproductcatcode "
					+ "     AND bpsc.nproductcatcode=pc.nproductcatcode"
					+ " JOIN public.diseasecategory dc ON bp.ndiseasecategorycode=dc.ndiseasecategorycode "
					+ " AND dc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN public.disease d ON bp.ndiseasecode=d.ndiseasecode " + "     AND d.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " WHERE 1=1 AND bsc.ntransactionstatus="
					+ Enumeration.TransactionStatus.ALIQUOTED.gettransactionstatus()
					+ (fromCollectionDate != null ? " AND bpsc.dsamplecollectiondate>='" + fromCollectionDate + "'"
							: "")
					+ (toCollectionDate != null ? " AND bpsc.dsamplecollectiondate<='" + toCollectionDate + "'" : "")
					+ (diseaseCategoryName != null
							? " AND LOWER(dc.sdiseasecategoryname) = LOWER('"
									+ stringUtilityFunction.replaceQuote(diseaseCategoryName) + "')"
							: "")
					+ (diseaseName != null
							? " AND LOWER(d.sdiseasename) = LOWER('" + stringUtilityFunction.replaceQuote(diseaseName)
									+ "')"
							: "")
					+ (projectTitle != null
							? " AND LOWER(bp.sprojecttitle)=LOWER('" + stringUtilityFunction.replaceQuote(projectTitle)
									+ "')"
							: "")
					+ (status != null ? " AND bsc.ntransactionstatus=" + status : "")
					+ " GROUP BY dc.sdiseasecategoryname,d.sdiseasename,bp.sprojecttitle,bpsr.scasetype,p.sproductname,pc.sproductcatname"
					+ " ORDER BY dc.sdiseasecategoryname,d.sdiseasename,bp.sprojecttitle,bpsr.scasetype;";
		}

//		-----stored samples against sample type
		if (status == Enumeration.TransactionStatus.STORED.gettransactionstatus()) {

			strQuery = " SELECT dc.sdiseasecategoryname AS disease_category, " + "        d.sdiseasename AS disease, "
					+ "        bp.sprojecttitle AS project, " + "        bpsr.scasetype AS case_type, "
					+ "        p.sproductname || ' (' || pc.sproductcatname || ')' AS specimen_type, "
					+ "        COUNT(bsc.srepositoryid) AS count_id_repository , 'stored' as status "
					+ " FROM bioparentsamplereceiving bpsr " + " JOIN public.bioparentsamplecollection bpsc "
					+ "      ON bpsc.nbioparentsamplecode = bpsr.nbioparentsamplecode " + "     AND bpsc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "     AND bpsr.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN public.biosamplereceiving bsc "
					+ "      ON bsc.nbioparentsamplecollectioncode = bpsc.nbioparentsamplecollectioncode "
					+ "     AND bsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN public.samplestoragetransaction sst "
					+ "      ON bsc.nbiosamplereceivingcode = sst.nbiosamplereceivingcode "
					+ " JOIN public.bioproject bp " + "      ON bpsr.nbioprojectcode = bp.nbioprojectcode "
					+ "     AND bp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN public.product p " + "      ON bsc.nproductcode = p.nproductcode AND p.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " JOIN public.productcategory pc "
					+ "      ON p.nproductcatcode = pc.nproductcatcode "
					+ "     AND bpsc.nproductcatcode = pc.nproductcatcode " + " JOIN public.diseasecategory dc "
					+ "      ON bp.ndiseasecategorycode = dc.ndiseasecategorycode " + "     AND dc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " JOIN public.disease d "
					+ "      ON bp.ndiseasecode = d.ndiseasecode " + "     AND d.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " WHERE 1 = 1 "
					+ (fromCollectionDate != null ? " AND bpsc.dsamplecollectiondate >= '" + fromCollectionDate + "' "
							: "")
					+ (toCollectionDate != null ? " AND bpsc.dsamplecollectiondate <= '" + toCollectionDate + "' " : "")
					+ (diseaseCategoryName != null
							? " AND LOWER(dc.sdiseasecategoryname) = LOWER('"
									+ stringUtilityFunction.replaceQuote(diseaseCategoryName) + "')"
							: "")
					+ (diseaseName != null
							? " AND LOWER(d.sdiseasename) = LOWER('" + stringUtilityFunction.replaceQuote(diseaseName)
									+ "')"
							: "")
					+ (projectTitle != null
							? " AND LOWER(bp.sprojecttitle) = LOWER('"
									+ stringUtilityFunction.replaceQuote(projectTitle) + "')"
							: "")
					+ " GROUP BY dc.sdiseasecategoryname, d.sdiseasename, bp.sprojecttitle, "
					+ "          bpsr.scasetype, p.sproductname, pc.sproductcatname "
					+ " ORDER BY dc.sdiseasecategoryname, d.sdiseasename, bp.sprojecttitle, bpsr.scasetype";
		}

//		-----Transfered samples against sample type(Direct Transfer & request based transfer)

		if (status == Enumeration.TransactionStatus.SENT.gettransactionstatus()) {

			strQuery = " SELECT disease_category, disease, project, case_type, specimen_type, status, "
					+ " SUM(count_id_repository) AS count_id_repository " + " FROM ( " + "   SELECT "
					+ "     dc.sdiseasecategoryname AS disease_category, " + "     d.sdiseasename AS disease, "
					+ "     bp.sprojecttitle AS project, " + "     dtd.jsondata->>'scasetype' AS case_type, "
					+ "     p.sproductname || ' (' || pc.sproductcatname || ')' AS specimen_type, "
					+ "     COUNT(dtd.srepositoryid) AS count_id_repository , 'sent' as status "
					+ "   FROM biodirecttransfer dt " + "   JOIN biodirecttransferdetails dtd "
					+ "     ON dt.nbiodirecttransfercode = dtd.nbiodirecttransfercode " + "   JOIN public.product p "
					+ "     ON dtd.nproductcode = p.nproductcode AND p.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   JOIN public.productcategory pc "
					+ "     ON p.nproductcatcode = pc.nproductcatcode "
					+ "     AND dtd.nproductcatcode = pc.nproductcatcode " + "     AND pc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   JOIN public.bioproject bp "
					+ "     ON dtd.nbioprojectcode = bp.nbioprojectcode AND bp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   JOIN public.diseasecategory dc "
					+ "     ON bp.ndiseasecategorycode = dc.ndiseasecategorycode AND dc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   JOIN public.disease d "
					+ "     ON bp.ndiseasecode = d.ndiseasecode AND d.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "   JOIN public.biosamplereceiving sr " + "     ON dtd.srepositoryid = sr.srepositoryid "
					+ "   JOIN public.bioparentsamplecollection psc "
					+ "     ON sr.nbioparentsamplecollectioncode = psc.nbioparentsamplecollectioncode "
					+ "   WHERE dt.ntransactionstatus = " + Enumeration.TransactionStatus.SENT.gettransactionstatus()
					+ "     AND dtd.ntransferstatus = " + Enumeration.TransactionStatus.SENT.gettransactionstatus()
					+ "     AND dt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ (fromCollectionDate != null ? " AND psc.dsamplecollectiondate >= '" + fromCollectionDate + "' "
							: "")
					+ (toCollectionDate != null ? " AND psc.dsamplecollectiondate <= '" + toCollectionDate + "' " : "")
					+ (diseaseCategoryName != null
							? " AND LOWER(dc.sdiseasecategoryname) = LOWER('"
									+ stringUtilityFunction.replaceQuote(diseaseCategoryName) + "')"
							: "")
					+ (diseaseName != null
							? " AND LOWER(d.sdiseasename) = LOWER('" + stringUtilityFunction.replaceQuote(diseaseName)
									+ "')"
							: "")
					+ (projectTitle != null
							? " AND LOWER(bp.sprojecttitle) = LOWER('"
									+ stringUtilityFunction.replaceQuote(projectTitle) + "')"
							: "")
					+ (status != null ? " AND dt.ntransactionstatus = " + status : "") + "   GROUP BY "
					+ "     dc.sdiseasecategoryname, d.sdiseasename, bp.sprojecttitle, "
					+ "     dtd.jsondata->>'scasetype', p.sproductname, pc.sproductcatname " + "   UNION ALL "
					+ "   SELECT " + "     dc.sdiseasecategoryname AS disease_category, "
					+ "     d.sdiseasename AS disease, " + "     bp.sprojecttitle AS project, "
					+ "     dtd.jsondata->>'scasetype' AS case_type, "
					+ "     p.sproductname || ' (' || pc.sproductcatname || ')' AS specimen_type, "
					+ "     COUNT(dtd.srepositoryid) AS count_id_repository , 'sent' as status "
					+ "   FROM biorequestbasedtransfer dt " + "   JOIN biorequestbasedtransferdetails dtd "
					+ "     ON dt.nbiorequestbasedtransfercode = dtd.nbiorequestbasedtransfercode "
					+ "   JOIN public.product p " + "     ON dtd.nproductcode = p.nproductcode AND p.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   JOIN public.productcategory pc "
					+ "     ON p.nproductcatcode = pc.nproductcatcode "
					+ "     AND dtd.nproductcatcode = pc.nproductcatcode " + "     AND pc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   JOIN public.bioproject bp "
					+ "     ON dtd.nbioprojectcode = bp.nbioprojectcode AND bp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   JOIN public.diseasecategory dc "
					+ "     ON bp.ndiseasecategorycode = dc.ndiseasecategorycode AND dc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   JOIN public.disease d "
					+ "     ON bp.ndiseasecode = d.ndiseasecode AND d.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "   JOIN public.biosamplereceiving sr " + "     ON dtd.srepositoryid = sr.srepositoryid "
					+ "   JOIN public.bioparentsamplecollection psc "
					+ "     ON sr.nbioparentsamplecollectioncode = psc.nbioparentsamplecollectioncode "
					+ "   WHERE dt.ntransactionstatus = " + Enumeration.TransactionStatus.SENT.gettransactionstatus()
					+ "     AND dtd.ntransferstatus = " + Enumeration.TransactionStatus.SENT.gettransactionstatus()
					+ "     AND dt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "     AND dtd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ (fromCollectionDate != null ? " AND psc.dsamplecollectiondate >= '" + fromCollectionDate + "' "
							: "")
					+ (toCollectionDate != null ? " AND psc.dsamplecollectiondate <= '" + toCollectionDate + "' " : "")
					+ (diseaseCategoryName != null
							? " AND LOWER(dc.sdiseasecategoryname) = LOWER('"
									+ stringUtilityFunction.replaceQuote(diseaseCategoryName) + "')"
							: "")
					+ (diseaseName != null
							? " AND LOWER(d.sdiseasename) = LOWER('" + stringUtilityFunction.replaceQuote(diseaseName)
									+ "')"
							: "")
					+ (projectTitle != null
							? " AND LOWER(bp.sprojecttitle) = LOWER('"
									+ stringUtilityFunction.replaceQuote(projectTitle) + "')"
							: "")
					+ (status != null ? " AND dt.ntransactionstatus = " + status : "") + "   GROUP BY "
					+ "     dc.sdiseasecategoryname, d.sdiseasename, bp.sprojecttitle, "
					+ "     dtd.jsondata->>'scasetype', p.sproductname, pc.sproductcatname " + " ) t "
					+ " GROUP BY disease_category, disease, project, case_type, specimen_type, status "
					+ " ORDER BY disease_category, disease, project, case_type;";
		}

		if (status == Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()) {
			strQuery = " SELECT dc.sdiseasecategoryname AS disease_category, d.sdiseasename AS disease, bp.sprojecttitle AS project, "
					+ " dtd.jsondata->>'scasetype' AS case_type, "
					+ " p.sproductname || ' (' || pc.sproductcatname || ')' AS specimen_type, "
					+ " COUNT(dtd.srepositoryid) AS count_id_repository , 'disposed' as status "
					+ " FROM biodisposeform dt JOIN biodisposeformdetails dtd "
					+ "      ON dt.nbiodisposeformcode = dtd.nbiodisposeformcode "
					+ " JOIN public.biosamplereceiving sr "
					+ "      ON dtd.srepositoryid = sr.srepositoryid AND sr.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN public.bioparentsamplecollection psc "
					+ "      ON sr.nbioparentsamplecollectioncode = psc.nbioparentsamplecollectioncode AND psc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " JOIN public.product p "
					+ "      ON dtd.nproductcode = p.nproductcode AND p.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " JOIN public.productcategory pc "
					+ "      ON p.nproductcatcode = pc.nproductcatcode AND dtd.nproductcatcode = pc.nproductcatcode AND pc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " JOIN public.bioproject bp "
					+ "      ON dtd.nbioprojectcode = bp.nbioprojectcode AND bp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " JOIN public.diseasecategory dc "
					+ "      ON bp.ndiseasecategorycode = dc.ndiseasecategorycode AND dc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " JOIN public.disease d "
					+ "      ON bp.ndiseasecode = d.ndiseasecode AND d.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " WHERE dt.ntransactionstatus = "
					+ Enumeration.TransactionStatus.DISPOSED.gettransactionstatus() + " AND dtd.ntransdisposestatus = "
					+ Enumeration.TransactionStatus.DISPOSED.gettransactionstatus() + " AND dt.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND dtd.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ (fromCollectionDate != null ? " AND psc.dsamplecollectiondate >= '" + fromCollectionDate + "'"
							: "")
					+ (toCollectionDate != null ? " AND psc.dsamplecollectiondate <= '" + toCollectionDate + "'" : "")
					+ (diseaseCategoryName != null
							? " AND LOWER(dc.sdiseasecategoryname) = LOWER('"
									+ stringUtilityFunction.replaceQuote(diseaseCategoryName) + "')"
							: "")
					+ (diseaseName != null
							? " AND LOWER(d.sdiseasename) = LOWER('" + stringUtilityFunction.replaceQuote(diseaseName)
									+ "')"
							: "")
					+ (projectTitle != null
							? " AND LOWER(bp.sprojecttitle) = LOWER('"
									+ stringUtilityFunction.replaceQuote(projectTitle) + "')"
							: "")
					+ (status != null ? " AND dt.ntransactionstatus = " + status : "")
					+ (status != null ? " AND dtd.ntransdisposestatus = " + status : "")
					+ " GROUP BY dc.sdiseasecategoryname, d.sdiseasename, bp.sprojecttitle, "
					+ "          dtd.jsondata->>'scasetype', p.sproductname, pc.sproductcatname, psc.dsamplecollectiondate "
					+ " ORDER BY dc.sdiseasecategoryname, d.sdiseasename, bp.sprojecttitle, dtd.jsondata->>'scasetype'";
		}

		try {
			items = jdbcTemplate.queryForList(strQuery);
		} catch (Exception e) {
			errors.add("Query execution failed: " + e.getMessage());
		}
		final Map<String, Object> pagination = new LinkedHashMap<>();
		pagination.put("page", 1);
		pagination.put("pageSize", items.size());
		pagination.put("totalPages", 1);
		pagination.put("totalItems", items.size());

		final Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("code", errors.isEmpty() ? 200 : 500);
		metadata.put("message", errors.isEmpty() ? "Successfully Retrieved RepositoryID summary"
				: "Failed to retrieve RepositoryID summary");
		metadata.put("pagination", pagination);

		final Map<String, Object> data = new LinkedHashMap<>();
		data.put("items", items);

		final Map<String, Object> finalResponse = new LinkedHashMap<>();
		finalResponse.put("apiVersion", "1.0");
		finalResponse.put("timestamp", java.time.Instant.now().toString());
		finalResponse.put("status", errors.isEmpty() ? "success" : "error");
		finalResponse.put("data", data);
		finalResponse.put("meta", metadata);
		finalResponse.put("errors", errors);

		return new ResponseEntity<>(finalResponse, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTotalRepoIdRequestedAndSent(final Map<String, Object> inputMap) throws Exception {

		final String diseaseCategoryName = inputMap.containsKey("diseaseCategoryName")
				? cleanObj((String) inputMap.get("diseaseCategoryName"))
				: null;

		final String diseaseName = inputMap.containsKey("diseaseName") ? cleanObj((String) inputMap.get("diseaseName"))
				: null;

		final String projectTitle = inputMap.containsKey("projectTitle")
				? cleanObj((String) inputMap.get("projectTitle"))
				: null;

		final String status1 = inputMap.containsKey("status") ? cleanObj((String) inputMap.get("status")) : null;

		final String fromCollectionDate = inputMap.containsKey("fromCollectionDate")
				? cleanObj((String) inputMap.get("fromCollectionDate"))
				: null;

		final String toCollectionDate = inputMap.containsKey("toCollectionDate")
				? cleanObj((String) inputMap.get("toCollectionDate"))
				: null;

		final String caseType = (String) inputMap.get("caseType");

		Integer status = null;

		if (status1 != null && !status1.isBlank()) {
			String sql = "SELECT ntranscode FROM transactionstatus WHERE LOWER(stransstatus)=LOWER(?)";
			try {
				status = jdbcTemplate.queryForObject(sql, Integer.class, status1.trim());
			} catch (Exception ex) {
				status = null;
			}
		}
		List<Map<String, Object>> items = new ArrayList<>();
		List<Object> errors = new ArrayList<>();
//ThirdParty table
		final var strQuery = "(" + " SELECT " + "  tp.sthirdpartyname AS third_party_institution, "
				+ "  dc.sdiseasecategoryname AS disease_category, " + "  d.sdiseasename AS disease, "
				+ "  bp.sprojecttitle AS project, " + "  '-' AS case_type, "
				+ "  COALESCE(det.sparentsamplecode, '-') AS specimen_type, " + "  'Requested' AS status, "
				+ "  COUNT(det.nreqnoofsamples) AS count_id_repository " + " FROM public.thirdparty tp "
				+ " LEFT JOIN public.biothirdpartyecataloguerequest req "
				+ "    ON req.nthirdpartycode = tp.nthirdpartycode "
				+ " LEFT JOIN public.biothirdpartyecataloguereqdetails det "
				+ "    ON det.nthirdpartyecatrequestcode = req.nthirdpartyecatrequestcode "
				+ " LEFT JOIN public.bioproject bp " + "    ON bp.nbioprojectcode = req.nbioprojectcode "
				+ " LEFT JOIN public.disease d " + "    ON d.ndiseasecode = bp.ndiseasecode "
				+ " LEFT JOIN public.diseasecategory dc " + "    ON dc.ndiseasecategorycode = bp.ndiseasecategorycode "
				+ " LEFT JOIN public.product p " + "    ON p.nproductcode = det.nproductcode "
				+ " LEFT JOIN public.transactionstatus ts " + "    ON ts.ntranscode = req.ntransactionstatus "
				+ " WHERE 1=1 " + "  AND req.ntransactionstatus IN ("
				+ +Enumeration.TransactionStatus.AMENDED.gettransactionstatus() + ","
				+ +Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ","
				+ +Enumeration.TransactionStatus.REQUESTED.gettransactionstatus() + ") " + "  AND tp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND req.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND det.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND bp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND dc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ (diseaseCategoryName != null
						? " AND LOWER(dc.sdiseasecategoryname) = LOWER('"
								+ stringUtilityFunction.replaceQuote(diseaseCategoryName) + "')"
						: "")
				+ (diseaseName != null
						? " AND LOWER(d.sdiseasename) = LOWER('" + stringUtilityFunction.replaceQuote(diseaseName)
								+ "')"
						: "")
				+ (projectTitle != null ? " AND LOWER(bp.sprojecttitle) = LOWER('"
						+ stringUtilityFunction.replaceQuote(projectTitle) + "')" : "")
				+ (status != null ? " AND req.ntransactionstatus = " + status : "")
				+ (fromCollectionDate != null ? " AND req.drequesteddate >= '" + fromCollectionDate + "'" : "")
				+ (toCollectionDate != null ? " AND req.drequesteddate <= '" + toCollectionDate + "'" : "")
				+ " GROUP BY "
				+ "  tp.sthirdpartyname, dc.sdiseasecategoryname, d.sdiseasename, bp.sprojecttitle, ts.stransstatus, det.sparentsamplecode "
				+ ")" + " UNION ALL " + "(" + " SELECT " + "  tp.sthirdpartyname AS third_party_institution, "
//RequestBasedTransfer				
				+ "  dc.sdiseasecategoryname AS disease_category, " + "  d.sdiseasename AS disease, "
				+ "  COALESCE(bp.sprojecttitle, '-') AS project, "
				+ "  COALESCE(brtd.jsondata->>'scasetype','-') AS case_type, "
				+ "  COALESCE(p.sproductname, '-') AS specimen_type, " + "  ts1.stransstatus AS status, "
				+ "  COUNT(brtd.srepositoryid) AS count_id_repository " + " FROM public.thirdparty tp "
				+ " LEFT JOIN public.biorequestbasedtransfer brt " + "    ON brt.nthirdpartycode = tp.nthirdpartycode "
				+ " LEFT JOIN public.biorequestbasedtransferdetails brtd "
				+ "    ON brtd.nbiorequestbasedtransfercode = brt.nbiorequestbasedtransfercode "
				+ " LEFT JOIN public.bioproject bp " + "    ON bp.nbioprojectcode = brtd.nbioprojectcode "
				+ " LEFT JOIN public.disease d " + "    ON d.ndiseasecode = bp.ndiseasecode "
				+ " LEFT JOIN public.diseasecategory dc " + "    ON dc.ndiseasecategorycode = bp.ndiseasecategorycode "
				+ " LEFT JOIN public.product p " + "    ON p.nproductcode = brtd.nproductcode "
				+ " LEFT JOIN public.transactionstatus ts1 " + "    ON ts1.ntranscode = brt.ntransactionstatus "
				+ " WHERE 1=1 " + "  AND brt.ntransfertypecode = 3 " + "  AND brt.ntransactionstatus = "
				+ Enumeration.TransactionStatus.SENT.gettransactionstatus() + "  AND tp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND brt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND brtd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND bp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND dc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ (diseaseCategoryName != null
						? " AND LOWER(dc.sdiseasecategoryname) = LOWER('"
								+ stringUtilityFunction.replaceQuote(diseaseCategoryName) + "')"
						: "")
				+ (diseaseName != null
						? " AND LOWER(d.sdiseasename) = LOWER('" + stringUtilityFunction.replaceQuote(diseaseName)
								+ "')"
						: "")
				+ (projectTitle != null ? " AND LOWER(bp.sprojecttitle) = LOWER('"
						+ stringUtilityFunction.replaceQuote(projectTitle) + "')" : "")
				+ (status != null ? " AND brt.ntransactionstatus = " + status : "")
				+ (fromCollectionDate != null ? " AND brt.dtransactiondate >= '" + fromCollectionDate + "'" : "")
				+ (toCollectionDate != null ? " AND brt.dtransactiondate <= '" + toCollectionDate + "'" : "")
				+ " GROUP BY "
				+ "  tp.sthirdpartyname, dc.sdiseasecategoryname, d.sdiseasename, bp.sprojecttitle, COALESCE(brtd.jsondata->>'scasetype','-'), p.sproductname, ts1.stransstatus "
				+ ")";

		try {
			items = jdbcTemplate.queryForList(strQuery);
		} catch (Exception e) {
			errors.add("Query execution failed: " + e.getMessage());
		}

		final Map<String, Object> pagination = new LinkedHashMap<>();
		pagination.put("page", 1);
		pagination.put("pageSize", items.size());
		pagination.put("totalPages", 1);
		pagination.put("totalItems", items.size());

		final Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("code", errors.isEmpty() ? 200 : 500);
		metadata.put("message", errors.isEmpty() ? "Successfully Retrieved RepositoryID Requested/Sent summary"
				: "Failed to retrieve RepositoryID Requested/Sent  summary");
		metadata.put("pagination", pagination);

		final Map<String, Object> data = new LinkedHashMap<>();
		data.put("items", items);

		final Map<String, Object> finalResponse = new LinkedHashMap<>();
		finalResponse.put("apiVersion", "1.0");
		finalResponse.put("timestamp", java.time.Instant.now().toString());
		finalResponse.put("status", errors.isEmpty() ? "success" : "error");
		finalResponse.put("data", data);
		finalResponse.put("meta", metadata);
		finalResponse.put("errors", errors);

		return new ResponseEntity<>(finalResponse, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTotalSubjectid(Map<String, Object> inputMap) throws Exception {

		final String sdiseasecategoryname = inputMap.containsKey("disease_category")
				? cleanObj(inputMap.get("disease_category"))
				: null;

		final String sdiseasename = inputMap.containsKey("disease") ? cleanObj(inputMap.get("disease")) : null;

		final String fromCollectionDate = inputMap.containsKey("fromCollectionDate")
				? cleanObj(inputMap.get("fromCollectionDate"))
				: null;

		final String toCollectionDate = inputMap.containsKey("toCollectionDate")
				? cleanObj(inputMap.get("toCollectionDate"))
				: null;

		final String projectTitle = inputMap.containsKey("project") ? cleanObj(inputMap.get("project")) : null;

		// final Integer projectTypeCode = (Integer) inputMap.get("projectTypeCode");
		// final Integer bioProjectCode = (Integer) inputMap.get("bioProjectCode");
		List<Map<String, Object>> items = new ArrayList<>();
		List<Object> errors = new ArrayList<>();

		String sQuery = "SELECT " + "dc.sdiseasecategoryname AS disease_category, " + "d.sdiseasename AS disease, "
				+ "bp.sprojecttitle AS project, " + "bsd.scasetype AS case_type, "
				+ "COUNT(st.ssubjectid) AS count_subject_id "
				+ "FROM samplestoragetransaction st, bioproject bp, diseasecategory dc, disease d, biosubjectdetails bsd, "
				+ "bioparentsamplereceiving bpsr, bioparentsamplecollection bpsc "
				+ "WHERE st.nprojecttypecode = bp.nbioprojectcode "
				+ "AND dc.ndiseasecategorycode = d.ndiseasecategorycode " + "AND bp.ndiseasecode = d.ndiseasecode "
				+ "AND bsd.ssubjectid = st.ssubjectid " + "AND bsd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND bp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND dc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND st.ncohortno = bpsr.ncohortno"
				+ " AND st.sparentsamplecode = bpsr.sparentsamplecode"
				+ " AND bpsr.nbioparentsamplecode = bpsc.nbioparentsamplecode"
				+ " AND bpsc.nproductcatcode = st.nproductcatcode" + " AND bpsr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND bpsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ (fromCollectionDate != null
						? " AND bpsc.dsamplecollectiondate >= '"
								+ stringUtilityFunction.replaceQuote(fromCollectionDate) + "'"
						: "")
				+ (toCollectionDate != null
						? " AND bpsc.dsamplecollectiondate <= '" + stringUtilityFunction.replaceQuote(toCollectionDate)
								+ "'"
						: "")
				+ (sdiseasecategoryname != null
						? " AND LOWER(dc.sdiseasecategoryname)=LOWER('"
								+ stringUtilityFunction.replaceQuote(sdiseasecategoryname) + "')"
						: "")
				+ (sdiseasename != null
						? " AND LOWER(d.sdiseasename)=LOWER('" + stringUtilityFunction.replaceQuote(sdiseasename) + "')"
						: "")
				+ (projectTitle != null ? " AND LOWER(bp.sprojecttitle)=LOWER('"
						+ stringUtilityFunction.replaceQuote(projectTitle) + "')" : "")
				+ " GROUP BY bsd.scasetype, st.ssubjectid, d.sdiseasename, dc.sdiseasecategoryname, bp.sprojecttitle";

		try {
			items = jdbcTemplate.queryForList(sQuery);
		} catch (Exception e) {
			errors.add("Query execution failed: " + e.getMessage());
		}

		final Map<String, Object> pagination = new LinkedHashMap<>();
		pagination.put("page", 1);
		pagination.put("pageSize", items.size());
		pagination.put("totalPages", 1);
		pagination.put("totalItems", items.size());

		final Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("code", errors.isEmpty() ? 200 : 500);
		metadata.put("message", errors.isEmpty() ? "Successfully Retrieved Total subjectID "
				: "Failed to retrieve Total subjectID values");
		metadata.put("pagination", pagination);

		final Map<String, Object> data = new LinkedHashMap<>();
		data.put("items", items);

		final Map<String, Object> finalResponse = new LinkedHashMap<>();
		finalResponse.put("apiVersion", "1.0");
		finalResponse.put("timestamp", java.time.Instant.now().toString());
		finalResponse.put("status", errors.isEmpty() ? "success" : "error");
		finalResponse.put("data", data);
		finalResponse.put("meta", metadata);
		finalResponse.put("errors", errors);

		return new ResponseEntity<>(finalResponse, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getStorageCapacity(final Map<String, Object> inputMap) throws Exception {

		List<Map<String, Object>> items = new ArrayList<>();
		List<Object> errors = new ArrayList<>();

		final String sitename = inputMap.containsKey("site") ? cleanObj(inputMap.get("site")) : null;

		Integer siteCode = null;

		if (sitename != null && !sitename.isBlank()) {
			final String siteQuery = "SELECT nsitecode FROM site WHERE LOWER(ssitename)=LOWER('"
					+ stringUtilityFunction.replaceQuote(sitename) + "') AND nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			try {
				siteCode = jdbcTemplate.queryForObject(siteQuery, Integer.class);
			} catch (Exception ex) {
				siteCode = null;
			}
		}
		final var strQuery = " SELECT s.ssitename AS site, v.sinstrumentid AS freezer, SUM(v.totalcount) AS storage_max_capacity, "
				+ " SUM(v.totalcount - COALESCE(v.navailablespace,0)) AS storage_filled"
				+ " FROM view_samplestoragelocation v LEFT JOIN site s ON s.nsitecode = v.nsitecode "
				+ " WHERE v.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ (siteCode != null ? " AND v.nsitecode = " + siteCode : "") + " GROUP BY s.ssitename, v.sinstrumentid "
				+ " ORDER BY s.ssitename DESC, v.sinstrumentid;";

		try {
			items = jdbcTemplate.queryForList(strQuery);
		} catch (Exception e) {
			errors.add("Query execution failed: " + e.getMessage());
		}
		final Map<String, Object> pagination = new LinkedHashMap<>();
		pagination.put("page", 1);
		pagination.put("pageSize", items.size());
		pagination.put("totalPages", 1);
		pagination.put("totalItems", items.size());

		final Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("code", errors.isEmpty() ? 200 : 500);
		metadata.put("message",
				errors.isEmpty() ? "Successfully Retrieved Storage capacity" : "Failed to retrieve Storage capacity");
		metadata.put("pagination", pagination);

		final Map<String, Object> data = new LinkedHashMap<>();
		data.put("items", items);

		final Map<String, Object> finalResponse = new LinkedHashMap<>();
		finalResponse.put("apiVersion", "1.0");
		finalResponse.put("timestamp", java.time.Instant.now().toString());
		finalResponse.put("status", errors.isEmpty() ? "success" : "error");
		finalResponse.put("data", data);
		finalResponse.put("meta", metadata);
		finalResponse.put("errors", errors);

		return new ResponseEntity<>(finalResponse, HttpStatus.OK);
	}

	private String cleanObj(Object value) {
		if (value == null)
			return null;
		String s = value.toString().trim();
		if (s.isEmpty())
			return null;
		if (s.startsWith("{{") && s.endsWith("}}"))
			return null;
		return s;
	}

/*
	@Override
	public ResponseEntity<Object> receiveDNASample(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		jdbcTemplate
				.execute("lock table lockbiothirdpartyreturn " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbiothirdpartyreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		List<Map<String, Object>> lstObj = inputMap.containsKey("samples")
				? (List<Map<String, Object>>) inputMap.get("samples")
				: new ArrayList<>();

		Map<String, Object> rtnMap = new HashMap<String, Object>();

		if (lstObj.size() > 0) {

			String strConditionValue = lstObj.stream().map(item -> "('" + item.get("sformnumber") + "', '"
					+ item.get("sreferencerepoid") + "', " + item.get("nthirdpartycode") + ")")
					.collect(Collectors.joining(","));

			String strConditionValueWithSampleId = lstObj.stream().map(item -> "('" + item.get("sformnumber") + "', '"
					+ item.get("sextractedsampleid") + "', " + item.get("nthirdpartycode") + ")")
					.collect(Collectors.joining(","));

			String strFormDetails = "select btpfa.sformnumber, btpfad.nbiothirdpartyformacceptancedetailscode, btpfad.nbiothirdpartyformacceptancecode,"
					+ " btpfad.nbioprojectcode, btpfad.nbioparentsamplecode, btpfad.ncohortno, btpfad.nstoragetypecode,"
					+ " btpfad.nproductcatcode, btpfad.nproductcode, btpfad.srepositoryid, btpfad.svolume, btpfad.sreceivedvolume,"
					+ " btpfad.ssubjectid, btpfad.sparentsamplecode, btpfad.jsondata, btpfad.ndiagnostictypecode,"
					+ " btpfad.ncontainertypecode, btpfad.nbiosamplereceivingcode, btpfad.nsamplecondition, btpfad.nsamplestatus,"
					+ " btpfad.nreasoncode, btpfad.nsamplestoragetransactioncode, btpfad.nsitecode from biothirdpartyformacceptdetails"
					+ " btpfad join biothirdpartyformaccept btpfa on"
					+ " btpfa.nbiothirdpartyformacceptancecode=btpfad.nbiothirdpartyformacceptancecode and btpfa.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and btpfa.ntransactionstatus="
					+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + " where btpfad.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + " and"
					+ " (btpfa.sformnumber, btpfad.srepositoryid, btpfa.nthirdpartycode) in (" + strConditionValue + ")"
					+ " and btpfa.ntransfertypecode=" + Enumeration.TransferType.BIOBANKEXTERNAL.getntransfertype()
					+ ";";

			// Checking that samples are present in biothirdpartyformacceptdetails table or
			// not
			List<BioThirdPartyFormAcceptDetails> lstTPFormAcceptDetails = jdbcTemplate.query(strFormDetails,
					new BioThirdPartyFormAcceptDetails());

			List<Map<String, Object>> lstObjFilteredMap = lstObj.stream()
					.filter(m -> lstTPFormAcceptDetails.stream()
							.anyMatch(p -> (p.getSformnumber()).equals(m.get("sformnumber"))
									&& (p.getSrepositoryid()).equals(m.get("sreferencerepoid"))))
					.collect(Collectors.toList());

			List<BioThirdPartyFormAcceptDetails> lstTPFormAcceptDetailsFinal = lstTPFormAcceptDetails;
			List<Map<String, Object>> tpFormAcceptNonExistedSamplesLst = new ArrayList<>();
			List<Map<String, Object>> finalInsertSampleList = lstObjFilteredMap;
			String nonExistedSampleId = "";

			if (lstObjFilteredMap.size() > 0) {
				// Checking whether samples are there or not in biothirdpartyformacceptdetails
				// table

				final int ntransSiteCode = lstTPFormAcceptDetails.get(0).getNsitecode();
				userInfo.setNtranssitecode((short) ntransSiteCode);

				List<Map<String, Object>> strAlreadyExistedSamples = jdbcTemplate.queryForList(
						"select btprd.srepositoryid, btprd.jsondata->>'sreferencerepoid' sreferencerepoid, btprd.jsondata->>'sextractedsampleid' sextractedsampleid from"
								+ " biothirdpartyreturndetails btprd join biothirdpartyreturn btpr on"
								+ " btpr.nbiothirdpartyreturncode=btprd.nbiothirdpartyreturncode and btpr.nsitecode="
								+ userInfo.getNtranssitecode() + " and btpr.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " where (btprd.jsondata->>'sformnumber', btprd.jsondata->>'sextractedsampleid', btpr.nthirdpartycode) in ("
								+ strConditionValueWithSampleId + ") and btprd.nsamplestatus != "
								+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
								+ " and btprd.nsitecode=" + userInfo.getNtranssitecode() + " and btprd.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

				String strAlreadyExistedSampleId = "";

				if (strAlreadyExistedSamples.size() > 0) {

					// Checking whether samples are already added in biothirdpartyreturndetails
					// table or not

					if (strAlreadyExistedSamples.size() == lstObj.size()) {
						// If all the samples are already transfered

						rtnMap.put("message", "Failed (All Sample(s) Already Exists)");
						rtnMap.put("nstatus", 409);
						LOGGER.info("message: Failed (All Sample(s) Already Exists), nstatus: " + 409);

						return new ResponseEntity<>(rtnMap, HttpStatus.CONFLICT);
					} else {

						strAlreadyExistedSampleId = strAlreadyExistedSamples.stream()
								.map(item -> (String) item.get("sextractedsampleid")).collect(Collectors.joining(","))
								+ " ";

						// Removing already transferred samples and collecting remaining samples to
						// insert in biothirdpartyreturndetails table
						finalInsertSampleList = lstObjFilteredMap.stream()
								.filter(m1 -> strAlreadyExistedSamples.stream().noneMatch(m2 -> Objects
										.equals(m1.get("sextractedsampleid"), m2.get("sextractedsampleid"))))
								.collect(Collectors.toList());

					}

				}

				final String siteTimeZoneDetails = "select s.nsitecode, s.ssitename, s.ssitecode,"
						+ " tz.ntimezonecode, tz.stimezoneid, tz.sdatetimeformat, tz.sgmtoffset from site s join timezone tz"
						+ " on tz.ntimezonecode=s.ntimezonecode and tz.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where nsitecode="
						+ ntransSiteCode + " and s.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nmastersitecode="
						+ userInfo.getNmastersitecode() + ";";

				// Collecting userinfo details for timezone details based on site
				Map<String, Object> mapSiteTimeZoneDetails = (Map<String, Object>) jdbcTemplate
						.queryForList(siteTimeZoneDetails).get(0);

				userInfo.setSsitecode((String) mapSiteTimeZoneDetails.get("ssiteCode"));
				userInfo.setSdatetimeformat((String) mapSiteTimeZoneDetails.get("sdatetimeformat"));
				userInfo.setNtimezonecode((int) mapSiteTimeZoneDetails.get("ntimezonecode"));
				userInfo.setStimezoneid((String) mapSiteTimeZoneDetails.get("stimezoneid"));

				if (lstObj.size() != lstObjFilteredMap.size()) {

					List<Map<String, Object>> lstObjMapDup = lstObjFilteredMap;

					tpFormAcceptNonExistedSamplesLst = lstObj.stream()
							.filter(lst -> lstObjMapDup.stream().map(m -> m.get("sreferencerepoid"))
									.noneMatch(item -> item.equals(lst.get("sreferencerepoid"))))
							.collect(Collectors.toList());

					// Collecting repositoryid which are not in biothirdpartyformacceptdetails table
					// to show in the return value
					nonExistedSampleId = tpFormAcceptNonExistedSamplesLst.stream()
							.map(item -> (String) item.get("sextractedsampleid")).collect(Collectors.joining(","))
							+ " ";

				}

				if (finalInsertSampleList.size() == 0 && tpFormAcceptNonExistedSamplesLst.size() > 0) {

					// No samples are transferring just throws alert for already existed samples and
					// also the samples which are not in biothirdpartyformacceptdetails table
					String alertMsg = " (" + nonExistedSampleId + "Doesn't Exist(s) and " + strAlreadyExistedSampleId
							+ "Already Exists)";

					rtnMap.put("message", "Failed " + alertMsg + "");
					rtnMap.put("nstatus", 409);
					LOGGER.info("message: Failed " + alertMsg + ", nstatus: " + 409);
					return new ResponseEntity<>(rtnMap, HttpStatus.CONFLICT);

				}

				List<String> repIdList = generateNUniqueSrepositoryIds(finalInsertSampleList.size());

				final String strSeqNo = "select stablename, nsequenceno from seqnobiobankmanagement where stablename in"
						+ " ('biothirdpartyreturn', 'biothirdpartyreturndetails', 'biothirdpartyreturndetailshistory',"
						+ " 'biothirdpartyreturnhistory') and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				List<SeqNoBioBankManagement> lstSeqNo = jdbcTemplate.query(strSeqNo, new SeqNoBioBankManagement());

				Map<String, Object> seqNoMap = new HashMap<String, Object>();
				seqNoMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoBioBankManagement::getStablename,
						SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

				int returnSeqNo = (int) seqNoMap.get("biothirdpartyreturn");
				int returnDetailsSeqNo = (int) seqNoMap.get("biothirdpartyreturndetails");
				int returnHistorySeqNo = (int) seqNoMap.get("biothirdpartyreturnhistory");
				int returnDetailsHistorySeqNo = (int) seqNoMap.get("biothirdpartyreturndetailshistory");

				final String strformat = projectDAOSupport.getSeqfnFormat("biothirdpartyreturn",
						"seqnoformatgeneratorbiobank", 0, 0, userInfo);

				returnSeqNo++;
				String strInsertReturn = "insert into biothirdpartyreturn(nbiothirdpartyreturncode, sthirdpartyreturnformnumber,"
						+ " ntransfertypecode, nformtypecode, nthirdpartycode, noriginsitecode, dreturndate,"
						+ " ntzreturndate, noffsetdreturndate, ntransactionstatus, jsondata, dtransactiondate,"
						+ " ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) select " + returnSeqNo
						+ ", '" + strformat + "', " + Enumeration.TransferType.BIOBANKEXTERNAL.getntransfertype() + ", "
						+ Enumeration.FormType.Transfer.getnformtype() + ", " + lstObj.get(0).get("nthirdpartycode")
						+ ", noriginsitecode, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
						+ userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
						+ ", json_build_object('sremarks', ''," + " 'soriginsitename', jsondata->>'soriginsitename'), '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", nsitecode, "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biothirdpartyformaccept where sformnumber='" + lstObj.get(0).get("sformnumber")
						+ "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				strInsertReturn += " update seqnobiobankmanagement set nsequenceno=" + returnSeqNo + " where"
						+ " stablename='biothirdpartyreturn' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				returnHistorySeqNo++;
				String strInsertReturnHistory = "insert into biothirdpartyreturnhistory (nbiothirdpartyreturnhistorycode,"
						+ " nbiothirdpartyreturncode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
						+ " noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode,"
						+ " nsitecode, nstatus) values (" + returnHistorySeqNo + ", " + returnSeqNo + ", "
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
				strInsertReturnHistory += " update seqnobiobankmanagement set nsequenceno=" + returnHistorySeqNo
						+ " where" + " stablename='biothirdpartyreturnhistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				String strInsertReturnDetails = "insert into biothirdpartyreturndetails (nbiothirdpartyreturndetailscode,"
						+ " nbiothirdpartyreturncode, nbiothirdpartyformacceptancecode, nbiothirdpartyformacceptancedetailscode, nbioprojectcode,"
						+ " nbioparentsamplecode, ncohortno, nstoragetypecode, nproductcatcode, nproductcode, "
						+ " svolume, sreturnvolume, jsondata, ndiagnostictypecode, ncontainertypecode,"
						+ " nbiosamplereceivingcode, nsamplecondition, srepositoryid, "
						+ " nsamplestatus, nreasoncode, nsamplestoragetransactioncode, nisexternalsample,"
						+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) values ";

				String strInsertReturnDetailsHistory = "insert into biothirdpartyreturndetailshistory"
						+ " (nbiothirdpartyreturndetailshistorycode, nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition,"
						+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
						+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) values ";

				String strValueReturnDetails = "";
				String strValueReturnDetailsHistory = "";
				int index = 0;

				for (Map<String, Object> mapObj : finalInsertSampleList) {

					BioThirdPartyFormAcceptDetails matchedMap = lstTPFormAcceptDetailsFinal.stream()
							.filter(m -> (m.getSrepositoryid().equals(mapObj.get("sreferencerepoid"))
									&& m.getSformnumber().equals(mapObj.get("sformnumber"))))
							.findFirst().orElse(null);

					final String seluent = (mapObj.get("seluent").equals("") || mapObj.get("seluent").equals(null))
							? null
							: "'" + mapObj.get("seluent") + "'";
					final String sqcPlatform = (mapObj.get("sqcplatform").equals("")
							|| mapObj.get("sqcplatform").equals(null)) ? null : "'" + mapObj.get("sqcplatform") + "'";
					final String sconcentration = (mapObj.get("sconcentration").equals("")
							|| mapObj.get("sconcentration").equals(null)) ? null
									: "'" + mapObj.get("sconcentration") + "'";
					final String sextractedSampleId = (mapObj.get("sextractedsampleid").equals("")
							|| mapObj.get("sextractedsampleid").equals(null)) ? null
									: "'" + mapObj.get("sextractedsampleid") + "'";

					returnDetailsSeqNo++;
					strValueReturnDetails += "(" + returnDetailsSeqNo + ", " + returnSeqNo + ", "
							+ matchedMap.getNbiothirdpartyformacceptancecode() + ", "
							+ matchedMap.getNbiothirdpartyformacceptancedetailscode() + ", "
							+ matchedMap.getNbioprojectcode() + ", " + matchedMap.getNbioparentsamplecode() + ", "
							+ matchedMap.getNcohortno() + ", " + matchedMap.getNstoragetypecode() + ", "
							+ matchedMap.getNproductcatcode() + ", " + mapObj.get("nproductcode") + ", '"
							+ matchedMap.getSvolume() + "', '" + mapObj.get("svolume")
							+ "', jsonb_build_object('seluent', " + seluent + ", 'svolume', '" + matchedMap.getSvolume()
							+ "', 'sreturnvolume', '" + mapObj.get("svolume") + "', 'scasetype', '"
							+ matchedMap.getJsondata().get("scasetype") + "', 'ssubjectid', '"
							+ matchedMap.getSsubjectid() + "', 'sformnumber', '" + mapObj.get("sformnumber")
							+ "', 'sqcplatform', " + sqcPlatform + ", 'nproductcode', " + mapObj.get("nproductcode")
							+ ", 'sconcentration', " + sconcentration + ", 'noriginsitecode', "
							+ mapObj.get("noriginsitecode") + ", 'nthirdpartycode', " + mapObj.get("nthirdpartycode")
							+ ", 'sreferencerepoid', '" + mapObj.get("sreferencerepoid") + "',"
							+ " 'sparentsamplecode', '" + mapObj.get("sparentsamplecode") + "', 'sextractedsampleid', "
							+ sextractedSampleId + "), " + matchedMap.getNdiagnostictypecode() + ", "
							+ matchedMap.getNcontainertypecode() + ", " + matchedMap.getNbiosamplereceivingcode() + ", "
							+ matchedMap.getNsamplecondition() + ", '" + repIdList.get(index) + "', "
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", -1, "
							+ matchedMap.getNsamplestoragetransactioncode() + ", "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ matchedMap.getNsitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

					returnDetailsHistorySeqNo++;
					strValueReturnDetailsHistory += "(" + returnDetailsHistorySeqNo + ", " + returnDetailsSeqNo + ", "
							+ returnSeqNo + ", " + matchedMap.getNsamplecondition() + ", "
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
							+ matchedMap.getNsitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

					index++;

				}

				strValueReturnDetails = strValueReturnDetails.substring(0, strValueReturnDetails.length() - 1) + ";";

				strValueReturnDetailsHistory = strValueReturnDetailsHistory.substring(0,
						strValueReturnDetailsHistory.length() - 1) + ";";

				strValueReturnDetails += " update seqnobiobankmanagement set nsequenceno=" + returnDetailsSeqNo
						+ " where stablename='biothirdpartyreturndetails' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				strValueReturnDetailsHistory += " update seqnobiobankmanagement set nsequenceno="
						+ returnDetailsHistorySeqNo
						+ " where stablename='biothirdpartyreturndetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				jdbcTemplate.execute(strInsertReturn + strInsertReturnHistory + strInsertReturnDetails
						+ strValueReturnDetails + strInsertReturnDetailsHistory + strValueReturnDetailsHistory);

				String additionMsg = "";

				if (!nonExistedSampleId.isEmpty() && !strAlreadyExistedSampleId.isEmpty()) {

					// When some samples are transferred and some samples are already transferred
					// and remaining samples are not in biothirdpartyformacceptdetails table
					additionMsg = " (" + nonExistedSampleId + "Doesn't Exist(s) and " + strAlreadyExistedSampleId
							+ "Already Exists)";

				} else if (!nonExistedSampleId.isEmpty()) {

					// When some samples are transferred and remaining samples are not in
					// biothirdpartyformacceptdetails table
					additionMsg = " (" + nonExistedSampleId + "Doesn't Exist(s))";

				} else if (!strAlreadyExistedSampleId.isEmpty()) {

					// When some samples are transferred and remaining samples are already
					// transferred
					additionMsg = " (" + strAlreadyExistedSampleId + "Already Exists)";

				}

				rtnMap.put("message", "Success" + additionMsg + "");
				rtnMap.put("nstatus", 200);
				LOGGER.info("message: Success" + additionMsg + ", nstatus: " + 200);
				return new ResponseEntity<>(rtnMap, HttpStatus.OK);

			} else {

				nonExistedSampleId = lstObjFilteredMap.stream().map(m -> (String) m.get("sextractedsampleid"))
						.collect(Collectors.joining(",")) + " ";
				rtnMap.put("message", "Failed (" + nonExistedSampleId + "Doesn't Exist(s))");
				rtnMap.put("nstatus", 404);
				LOGGER.info("message: Failed (" + nonExistedSampleId + "Doesn't Exist(s)), nstatus: " + 404);
				return new ResponseEntity<>(rtnMap, HttpStatus.NOT_FOUND);

			}

		} else {

			rtnMap.put("message", "Failed (No Sample(s) Found)");
			rtnMap.put("nstatus", 404);
			LOGGER.info("message: Failed (No Sample(s) Found), nstatus: " + 404);
			return new ResponseEntity<>(rtnMap, HttpStatus.NOT_FOUND);

		}
	}

	
	public List<String> generateNUniqueSrepositoryIds(int count) {
		Set<String> uniqueIds = new HashSet<>();
		int attempts = 0;

		while (uniqueIds.size() < count) {
			String candidate = generateValidId();

			if (!uniqueIds.contains(candidate) && !srepositoryIdExistsInDB(candidate)) {
				uniqueIds.add(candidate);
			}

			attempts++;
			if (attempts > count * 100) {
				throw new RuntimeException(
						"Failed to generate " + count + " unique srepositoryid values after many attempts");
			}
		}

		return new ArrayList<>(uniqueIds);
	}

	private String generateValidId() {
		while (true) {
			List<Character> chars = new ArrayList<>();

			for (int i = 0; i < 3; i++) {
				chars.add((char) ('A' + random.nextInt(26)));
			}

			for (int i = 0; i < 4; i++) {
				chars.add((char) ('0' + random.nextInt(10)));
			}

			Collections.shuffle(chars);

			if (chars.get(0) == '0')
				continue;

			StringBuilder sb = new StringBuilder();
			for (char c : chars) {
				sb.append(c);
			}

			return sb.toString();
		}
	}

	private boolean srepositoryIdExistsInDB(String id) {
		String sql = "SELECT COUNT(1) FROM biosamplereceiving WHERE srepositoryid = ?";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
		return count != null && count > 0;
	}
*/
	
	@Override
	public ResponseEntity<Object> receiveDNASample(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

	    jdbcTemplate
	            .execute("lock table lockbiothirdpartyreturn " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
	    jdbcTemplate.execute(
	            "lock table lockbiothirdpartyreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

	    List<Map<String, Object>> lstObj = inputMap.containsKey("samples")
	            ? (List<Map<String, Object>>) inputMap.get("samples")
	            : new ArrayList<>();

	    Map<String, Object> rtnMap = new HashMap<String, Object>();

	    if (lstObj.size() > 0) {

	        String strConditionValue = lstObj.stream().map(item -> "('" + item.get("sformnumber") + "', '"
	                + item.get("sreferencerepoid") + "', " + item.get("nthirdpartycode") + ")")
	                .collect(Collectors.joining(","));

	        String strConditionValueWithSampleId = lstObj.stream().map(item -> "('" + item.get("sformnumber") + "', '"
	                + item.get("sextractedsampleid") + "', " + item.get("nthirdpartycode") + ")")
	                .collect(Collectors.joining(","));

	        String strFormDetails = "select btpfa.sformnumber, btpfad.nbiothirdpartyformacceptancedetailscode, btpfad.nbiothirdpartyformacceptancecode,"
	                + " btpfad.nbioprojectcode, btpfad.nbioparentsamplecode, btpfad.ncohortno, btpfad.nstoragetypecode,"
	                + " btpfad.nproductcatcode, btpfad.nproductcode, btpfad.srepositoryid, btpfad.svolume, btpfad.sreceivedvolume,"
	                + " btpfad.ssubjectid, btpfad.sparentsamplecode, btpfad.jsondata, btpfad.ndiagnostictypecode,"
	                + " btpfad.ncontainertypecode, btpfad.nbiosamplereceivingcode, btpfad.nsamplecondition, btpfad.nsamplestatus,"
	                + " btpfad.nreasoncode, btpfad.nsamplestoragetransactioncode, btpfad.nsitecode from biothirdpartyformacceptdetails"
	                + " btpfad join biothirdpartyformaccept btpfa on"
	                + " btpfa.nbiothirdpartyformacceptancecode=btpfad.nbiothirdpartyformacceptancecode and btpfa.nstatus="
	                + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and btpfa.ntransactionstatus="
	                + Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + " where btpfad.nstatus="
	                + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus="
	                + Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + " and"
	                + " (btpfa.sformnumber, btpfad.srepositoryid, btpfa.nthirdpartycode) in (" + strConditionValue + ")"
	                + " and btpfa.ntransfertypecode=" + Enumeration.TransferType.BIOBANKEXTERNAL.getntransfertype()
	                + ";";

	        // Checking that samples are present in biothirdpartyformacceptdetails table or not
	        List<BioThirdPartyFormAcceptDetails> lstTPFormAcceptDetails = jdbcTemplate.query(strFormDetails,
	                new BioThirdPartyFormAcceptDetails());


	    	List<Map<String, Object>> lstObjFilteredMap = lstObj.stream()
					.filter(m -> lstTPFormAcceptDetails.stream()
							.anyMatch(p -> (p.getSformnumber()).equals(m.get("sformnumber"))
									&& (p.getSrepositoryid()).equals(m.get("sreferencerepoid"))))
					.collect(Collectors.toList());

	        List<BioThirdPartyFormAcceptDetails> lstTPFormAcceptDetailsFinal = lstTPFormAcceptDetails;

	        Map<Integer, Integer> dnaProductCodeMap = getOrCreateDnaProductCodes(lstTPFormAcceptDetailsFinal, userInfo);

	        List<Map<String, Object>> tpFormAcceptNonExistedSamplesLst = new ArrayList<>();
	        List<Map<String, Object>> finalInsertSampleList = lstObjFilteredMap;
	        String nonExistedSampleId = "";

	        if (lstObjFilteredMap.size() > 0) {
	            // Checking whether samples are there or not in biothirdpartyformacceptdetails table

	            final int ntransSiteCode = lstTPFormAcceptDetails.get(0).getNsitecode();
	            userInfo.setNtranssitecode((short) ntransSiteCode);

	            List<Map<String, Object>> strAlreadyExistedSamples = jdbcTemplate.queryForList(
	                    "select btprd.srepositoryid, btprd.jsondata->>'sreferencerepoid' sreferencerepoid, btprd.jsondata->>'sextractedsampleid' sextractedsampleid from"
	                            + " biothirdpartyreturndetails btprd join biothirdpartyreturn btpr on"
	                            + " btpr.nbiothirdpartyreturncode=btprd.nbiothirdpartyreturncode and btpr.nsitecode="
	                            + userInfo.getNtranssitecode() + " and btpr.nstatus="
	                            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
	                            + " where (btprd.jsondata->>'sformnumber', btprd.jsondata->>'sextractedsampleid', btpr.nthirdpartycode) in ("
	                            + strConditionValueWithSampleId + ") and btprd.nsamplestatus != "
	                            + Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
	                            + " and btprd.nsitecode=" + userInfo.getNtranssitecode() + " and btprd.nstatus="
	                            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

	            String strAlreadyExistedSampleId = "";

	            if (strAlreadyExistedSamples.size() > 0) {

	                // Checking whether samples are already added in biothirdpartyreturndetails
	                // table or not

	                if (strAlreadyExistedSamples.size() == lstObj.size()) {
	                    // If all the samples are already transfered

	                    rtnMap.put("message", "Failed (All Sample(s) Already Exists)");
	                    rtnMap.put("nstatus", 409);
	                    LOGGER.info("message: Failed (All Sample(s) Already Exists), nstatus: " + 409);

	                    return new ResponseEntity<>(rtnMap, HttpStatus.CONFLICT);
	                } else {

	                    strAlreadyExistedSampleId = strAlreadyExistedSamples.stream()
	                            .map(item -> (String) item.get("sextractedsampleid")).collect(Collectors.joining(","))
	                            + " ";

	                    // Removing already transferred samples and collecting remaining samples to
	                    // insert in biothirdpartyreturndetails table
	                    finalInsertSampleList = lstObjFilteredMap.stream()
	                            .filter(m1 -> strAlreadyExistedSamples.stream().noneMatch(m2 -> Objects
	                                    .equals(m1.get("sextractedsampleid"), m2.get("sextractedsampleid"))))
	                            .collect(Collectors.toList());

	                }

	            }

	            final String siteTimeZoneDetails = "select s.nsitecode, s.ssitename, s.ssitecode,"
	                    + " tz.ntimezonecode, tz.stimezoneid, tz.sdatetimeformat, tz.sgmtoffset from site s join timezone tz"
	                    + " on tz.ntimezonecode=s.ntimezonecode and tz.nstatus="
	                    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where nsitecode="
	                    + ntransSiteCode + " and s.nstatus="
	                    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nmastersitecode="
	                    + userInfo.getNmastersitecode() + ";";

	            // Collecting userinfo details for timezone details based on site
	            Map<String, Object> mapSiteTimeZoneDetails = (Map<String, Object>) jdbcTemplate
	                    .queryForList(siteTimeZoneDetails).get(0);

	            userInfo.setSsitecode((String) mapSiteTimeZoneDetails.get("ssitecode"));
	            userInfo.setSdatetimeformat((String) mapSiteTimeZoneDetails.get("sdatetimeformat"));
	            userInfo.setNtimezonecode((int) mapSiteTimeZoneDetails.get("ntimezonecode"));
	            userInfo.setStimezoneid((String) mapSiteTimeZoneDetails.get("stimezoneid"));

	            if (lstObj.size() != lstObjFilteredMap.size()) {

	                List<Map<String, Object>> lstObjMapDup = lstObjFilteredMap;

	                tpFormAcceptNonExistedSamplesLst = lstObj.stream()
	                        .filter(lst -> lstObjMapDup.stream().map(m -> m.get("sreferencerepoid"))
	                                .noneMatch(item -> item.equals(lst.get("sreferencerepoid"))))
	                        .collect(Collectors.toList());

	                // Collecting repositoryid which are not in biothirdpartyformacceptdetails table
	                // to show in the return value
	                nonExistedSampleId = tpFormAcceptNonExistedSamplesLst.stream()
	                        .map(item -> (String) item.get("sextractedsampleid")).collect(Collectors.joining(","))
	                        + " ";

	            }

	            if (finalInsertSampleList.size() == 0 && tpFormAcceptNonExistedSamplesLst.size() > 0) {

	                // No samples are transferring just throws alert for already existed samples and
	                // also the samples which are not in biothirdpartyformacceptdetails table
	                String alertMsg = " (" + nonExistedSampleId + "Doesn't Exist(s) and " + strAlreadyExistedSampleId
	                        + "Already Exists)";

	                rtnMap.put("message", "Failed " + alertMsg + "");
	                rtnMap.put("nstatus", 409);
	                LOGGER.info("message: Failed " + alertMsg + ", nstatus: " + 409);
	                return new ResponseEntity<>(rtnMap, HttpStatus.CONFLICT);

	            }

	            List<String> repIdList = generateNUniqueSrepositoryIds(finalInsertSampleList.size());

	            final String strSeqNo = "select stablename, nsequenceno from seqnobiobankmanagement where stablename in"
	                    + " ('biothirdpartyreturn', 'biothirdpartyreturndetails', 'biothirdpartyreturndetailshistory',"
	                    + " 'biothirdpartyreturnhistory') and nstatus="
	                    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	            List<SeqNoBioBankManagement> lstSeqNo = jdbcTemplate.query(strSeqNo, new SeqNoBioBankManagement());

	            Map<String, Object> seqNoMap = new HashMap<String, Object>();
	            seqNoMap = lstSeqNo.stream().collect(Collectors.toMap(SeqNoBioBankManagement::getStablename,
	                    SeqNoRegistration -> SeqNoRegistration.getNsequenceno()));

	            int returnSeqNo = (int) seqNoMap.get("biothirdpartyreturn");
	            int returnDetailsSeqNo = (int) seqNoMap.get("biothirdpartyreturndetails");
	            int returnHistorySeqNo = (int) seqNoMap.get("biothirdpartyreturnhistory");
	            int returnDetailsHistorySeqNo = (int) seqNoMap.get("biothirdpartyreturndetailshistory");

	            final String strformat = projectDAOSupport.getSeqfnFormat("biothirdpartyreturn",
	                    "seqnoformatgeneratorbiobank", 0, 0, userInfo);

	            returnSeqNo++;
	            String strInsertReturn = "insert into biothirdpartyreturn(nbiothirdpartyreturncode, sthirdpartyreturnformnumber,"
	                    + " ntransfertypecode, nformtypecode, nthirdpartycode, noriginsitecode, dreturndate,"
	                    + " ntzreturndate, noffsetdreturndate, ntransactionstatus, jsondata, dtransactiondate,"
	                    + " ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) select " + returnSeqNo
	                    + ", '" + strformat + "', " + Enumeration.TransferType.BIOBANKEXTERNAL.getntransfertype() + ", "
	                    + Enumeration.FormType.Transfer.getnformtype() + ", " + lstObj.get(0).get("nthirdpartycode")
	                    + ", noriginsitecode, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
	                    + userInfo.getNtimezonecode() + ", "
	                    + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
	                    + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
	                    + ", json_build_object('sremarks', ''," + " 'soriginsitename', jsondata->>'soriginsitename'), '"
	                    + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
	                    + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", nsitecode, "
	                    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
	                    + " from biothirdpartyformaccept where sformnumber='" + lstObj.get(0).get("sformnumber")
	                    + "' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
	            strInsertReturn += " update seqnobiobankmanagement set nsequenceno=" + returnSeqNo + " where"
	                    + " stablename='biothirdpartyreturn' and nstatus="
	                    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

	            returnHistorySeqNo++;
	            String strInsertReturnHistory = "insert into biothirdpartyreturnhistory (nbiothirdpartyreturnhistorycode,"
	                    + " nbiothirdpartyreturncode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
	                    + " noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode,"
	                    + " nsitecode, nstatus) values (" + returnHistorySeqNo + ", " + returnSeqNo + ", "
	                    + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
	                    + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
	                    + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
	                    + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
	                    + userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
	                    + userInfo.getNtranssitecode() + ", "
	                    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
	            strInsertReturnHistory += " update seqnobiobankmanagement set nsequenceno=" + returnHistorySeqNo
	                    + " where" + " stablename='biothirdpartyreturnhistory' and nstatus="
	                    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

	            String strInsertReturnDetails = "insert into biothirdpartyreturndetails (nbiothirdpartyreturndetailscode,"
	                    + " nbiothirdpartyreturncode, nbiothirdpartyformacceptancecode, nbiothirdpartyformacceptancedetailscode, nbioprojectcode,"
	                    + " nbioparentsamplecode, ncohortno, nstoragetypecode, nproductcatcode, nproductcode, "
	                    + " svolume, sreturnvolume, jsondata, ndiagnostictypecode, ncontainertypecode,"
	                    + " nbiosamplereceivingcode, nsamplecondition, srepositoryid, "
	                    + " nsamplestatus, nreasoncode, nsamplestoragetransactioncode, nisexternalsample,"
	                    + " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) values ";

	            String strInsertReturnDetailsHistory = "insert into biothirdpartyreturndetailshistory"
	                    + " (nbiothirdpartyreturndetailshistorycode, nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition,"
	                    + " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
	                    + " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) values ";

	            String strValueReturnDetails = "";
	            String strValueReturnDetailsHistory = "";
	            int index = 0;

	            for (Map<String, Object> mapObj : finalInsertSampleList) {

	                BioThirdPartyFormAcceptDetails matchedMap = lstTPFormAcceptDetailsFinal.stream()
	                        .filter(m -> (m.getSrepositoryid().equals(mapObj.get("sreferencerepoid"))
	                                && m.getSformnumber().equals(mapObj.get("sformnumber"))))
	                        .findFirst().orElse(null);

	                if (matchedMap == null) {
	                    // No matching record – skip or throw exception based on your rules
	                    continue;
	                }

	                Integer dnaProductCode = dnaProductCodeMap.get(matchedMap.getNproductcatcode());
	                if (dnaProductCode == null) {
	                    throw new RuntimeException("DNA product not found/created for product category: "
	                            + matchedMap.getNproductcatcode());
	                }
	                int productCodeToUse = dnaProductCode.intValue();

	                final String seluent = (mapObj.get("seluent").equals("") || mapObj.get("seluent").equals(null))
	                        ? null
	                        : "'" + mapObj.get("seluent") + "'";
	                final String sqcPlatform = (mapObj.get("sqcplatform").equals("")
	                        || mapObj.get("sqcplatform").equals(null)) ? null : "'" + mapObj.get("sqcplatform") + "'";
	                final String sconcentration = (mapObj.get("sconcentration").equals("")
	                        || mapObj.get("sconcentration").equals(null)) ? null
	                                : "'" + mapObj.get("sconcentration") + "'";
	                final String sextractedSampleId = (mapObj.get("sextractedsampleid").equals("")
	                        || mapObj.get("sextractedsampleid").equals(null)) ? null
	                                : "'" + mapObj.get("sextractedsampleid") + "'";

	                returnDetailsSeqNo++;
	                strValueReturnDetails += "(" + returnDetailsSeqNo + ", " + returnSeqNo + ", "
	                        + matchedMap.getNbiothirdpartyformacceptancecode() + ", "
	                        + matchedMap.getNbiothirdpartyformacceptancedetailscode() + ", "
	                        + matchedMap.getNbioprojectcode() + ", " + matchedMap.getNbioparentsamplecode() + ", "
	                        + matchedMap.getNcohortno() + ", " + matchedMap.getNstoragetypecode() + ", "
	                        + matchedMap.getNproductcatcode() + ", " + productCodeToUse + ", '"
	                        + matchedMap.getSvolume() + "', '" + mapObj.get("svolume")
	                        + "', jsonb_build_object('seluent', " + seluent + ", 'svolume', '" + matchedMap.getSvolume()
	                        + "', 'sreturnvolume', '" + mapObj.get("svolume") + "', 'scasetype', '"
	                        + matchedMap.getJsondata().get("scasetype") + "', 'ssubjectid', '"
	                        + matchedMap.getSsubjectid() + "', 'sformnumber', '" + mapObj.get("sformnumber")
	                        + "', 'sqcplatform', " + sqcPlatform + ", 'nproductcode', " + productCodeToUse
	                        + ", 'sconcentration', " + sconcentration + ", 'noriginsitecode', "
	                        + mapObj.get("noriginsitecode") + ", 'nthirdpartycode', " + mapObj.get("nthirdpartycode")
	                        + ", 'sreferencerepoid', '" + mapObj.get("sreferencerepoid") + "',"
	                        + " 'sparentsamplecode', '" + mapObj.get("sparentsamplecode") + "', 'sextractedsampleid', "
	                        + sextractedSampleId + "), " + matchedMap.getNdiagnostictypecode() + ", "
	                        + matchedMap.getNcontainertypecode() + ", " + matchedMap.getNbiosamplereceivingcode() + ", "
	                        + matchedMap.getNsamplecondition() + ", '" + repIdList.get(index) + "', "
	                        + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", -1, "
	                        + matchedMap.getNsamplestoragetransactioncode() + ", "
	                        + Enumeration.TransactionStatus.YES.gettransactionstatus() + ", '"
	                        + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
	                        + ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
	                        + matchedMap.getNsitecode() + ", "
	                        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

	                returnDetailsHistorySeqNo++;
	                strValueReturnDetailsHistory += "(" + returnDetailsHistorySeqNo + ", " + returnDetailsSeqNo + ", "
	                        + returnSeqNo + ", " + matchedMap.getNsamplecondition() + ", "
	                        + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
	                        + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
	                        + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
	                        + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
	                        + userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
	                        + matchedMap.getNsitecode() + ", "
	                        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

	                index++;

	            }

	            strValueReturnDetails = strValueReturnDetails.substring(0, strValueReturnDetails.length() - 1) + ";";

	            strValueReturnDetailsHistory = strValueReturnDetailsHistory.substring(0,
	                    strValueReturnDetailsHistory.length() - 1) + ";";

	            strValueReturnDetails += " update seqnobiobankmanagement set nsequenceno=" + returnDetailsSeqNo
	                    + " where stablename='biothirdpartyreturndetails' and nstatus="
	                    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
	            strValueReturnDetailsHistory += " update seqnobiobankmanagement set nsequenceno="
	                    + returnDetailsHistorySeqNo
	                    + " where stablename='biothirdpartyreturndetailshistory' and nstatus="
	                    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

	            jdbcTemplate.execute(strInsertReturn + strInsertReturnHistory + strInsertReturnDetails
	                    + strValueReturnDetails + strInsertReturnDetailsHistory + strValueReturnDetailsHistory);

	            String additionMsg = "";

	            if (!nonExistedSampleId.isEmpty() && !strAlreadyExistedSampleId.isEmpty()) {

	                // When some samples are transferred and some samples are already transferred
	                // and remaining samples are not in biothirdpartyformacceptdetails table
	                additionMsg = " (" + nonExistedSampleId + "Doesn't Exist(s) and " + strAlreadyExistedSampleId
	                        + "Already Exists)";

	            } else if (!nonExistedSampleId.isEmpty()) {

	                // When some samples are transferred and remaining samples are not in
	                // biothirdpartyformacceptdetails table
	                additionMsg = " (" + nonExistedSampleId + "Doesn't Exist(s))";

	            } else if (!strAlreadyExistedSampleId.isEmpty()) {

	                // When some samples are transferred and remaining samples are already
	                // transferred
	                additionMsg = " (" + strAlreadyExistedSampleId + "Already Exists)";

	            }

	            rtnMap.put("message", "Success" + additionMsg + "");
	            rtnMap.put("nstatus", 200);
	            LOGGER.info("message: Success" + additionMsg + ", nstatus: " + 200);
	            return new ResponseEntity<>(rtnMap, HttpStatus.OK);

	        } else {

	            nonExistedSampleId = lstObjFilteredMap.stream().map(m -> (String) m.get("sextractedsampleid"))
	                    .collect(Collectors.joining(",")) + " ";
	            rtnMap.put("message", "Failed (" + nonExistedSampleId + "Doesn't Exist(s))");
	            rtnMap.put("nstatus", 404);
	            LOGGER.info("message: Failed (" + nonExistedSampleId + "Doesn't Exist(s)), nstatus: " + 404);
	            return new ResponseEntity<>(rtnMap, HttpStatus.NOT_FOUND);

	        }

	    } else {

	        rtnMap.put("message", "Failed (No Sample(s) Found)");
	        rtnMap.put("nstatus", 404);
	        LOGGER.info("message: Failed (No Sample(s) Found), nstatus: " + 404);
	        return new ResponseEntity<>(rtnMap, HttpStatus.NOT_FOUND);

	    }
	}

	/**
	 * Build a map of product category -> DNA productcode.
	 * If a DNA product does not exist for a category, create it in `product`
	 * using `seqnoproductmanagement` and return the new productcode.
	 * @throws Exception 
	 */
	private Map<Integer, Integer> getOrCreateDnaProductCodes(
	        List<BioThirdPartyFormAcceptDetails> lstTPFormAcceptDetailsFinal, UserInfo userInfo) throws Exception {

	    Map<Integer, Integer> dnaProductCodeMap = new HashMap<>();

	    if (lstTPFormAcceptDetailsFinal == null || lstTPFormAcceptDetailsFinal.isEmpty()) {
	        return dnaProductCodeMap;
	    }

	    // Collect all non-null product category codes
	    Set<Integer> productCatCodes = lstTPFormAcceptDetailsFinal.stream()
	            .map(BioThirdPartyFormAcceptDetails::getNproductcatcode)
	            .filter(Objects::nonNull)
	            .collect(Collectors.toSet());

	    if (productCatCodes.isEmpty()) {
	        return dnaProductCodeMap;
	    }

	    // 1. Load existing DNA products for these categories
	    String inClause = productCatCodes.stream()
	            .map(String::valueOf)
	            .collect(Collectors.joining(","));

	    String dnaProductSql =
	            "select nproductcatcode, nproductcode " +
	            "from product " +
	            "where upper(trim(sproductname)) = 'DNA' " +
	            "and nproductcatcode in (" + inClause + ") " +
	            "and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	    List<Map<String, Object>> dnaProducts = jdbcTemplate.queryForList(dnaProductSql);

	    for (Map<String, Object> row : dnaProducts) {
	        Integer catCode = ((Number) row.get("nproductcatcode")).intValue();
	        Integer prodCode = ((Number) row.get("nproductcode")).intValue();
	        dnaProductCodeMap.put(catCode, prodCode);
	    }

	    // 2. Categories that are missing a DNA product
	    Set<Integer> missingCatCodes = productCatCodes.stream()
	            .filter(cat -> !dnaProductCodeMap.containsKey(cat))
	            .collect(Collectors.toSet());

	    if (missingCatCodes.isEmpty()) {
	        return dnaProductCodeMap;
	    }

	    // 3. Create DNA products for missing categories using seqnoproductmanagement
	    final String seqSql =
	            "select nsequenceno from seqnoproductmanagement " +
	            "where stablename='product' and nstatus=" +
	            Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	    Integer productSeqNo = jdbcTemplate.queryForObject(seqSql, Integer.class);
	    if (productSeqNo == null) {
	        throw new RuntimeException("Product sequence not found in seqnoproductmanagement for stablename='product'");
	    }

	    int currentSeq = productSeqNo;
	    StringBuilder insertProductSql = new StringBuilder();

	    for (Integer catCode : missingCatCodes) {
	        currentSeq++;

	        insertProductSql.append("insert into product(")
	                .append("nproductcode, nproductcatcode, sproductname, sdescription, dmodifieddate, nsitecode, nstatus")
	                .append(") values (")
	                .append(currentSeq).append(", ")
	                .append(catCode).append(", ")
	                .append("'DNA', ")
					.append("'DNA inserted via QuaLIS Biobank Admin', ' ")
					.append(dateUtilityFunction.getCurrentDateTime(userInfo)).append("', ")
					.append(userInfo.getNmastersitecode()).append(", ")
	                .append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
	                .append(");");

	        dnaProductCodeMap.put(catCode, currentSeq);
	    }

	    insertProductSql.append(" update seqnoproductmanagement set nsequenceno=")
	            .append(currentSeq)
	            .append(" where stablename='product' and nstatus=")
	            .append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
	            .append(";");

	    jdbcTemplate.execute(insertProductSql.toString());

	    return dnaProductCodeMap;
	}

	public List<String> generateNUniqueSrepositoryIds(int count) {
	    Set<String> uniqueIds = new HashSet<>();
	    int attempts = 0;

	    while (uniqueIds.size() < count) {
	        String candidate = generateValidId();

	        if (!uniqueIds.contains(candidate) && !srepositoryIdExistsInDB(candidate)) {
	            uniqueIds.add(candidate);
	        }

	        attempts++;
	        if (attempts > count * 100) {
	            throw new RuntimeException(
	                    "Failed to generate " + count + " unique srepositoryid values after many attempts");
	        }
	    }

	    return new ArrayList<>(uniqueIds);
	}

	private String generateValidId() {
	    while (true) {
	        List<Character> chars = new ArrayList<>();

	        for (int i = 0; i < 3; i++) {
	            chars.add((char) ('A' + random.nextInt(26)));
	        }

	        for (int i = 0; i < 4; i++) {
	            chars.add((char) ('0' + random.nextInt(10)));
	        }

	        Collections.shuffle(chars);

	        if (chars.get(0) == '0')
	            continue;

	        StringBuilder sb = new StringBuilder();
	        for (char c : chars) {
	            sb.append(c);
	        }

	        return sb.toString();
	    }
	}

	private boolean srepositoryIdExistsInDB(String id) {
	    String sql = "SELECT COUNT(1) FROM biosamplereceiving WHERE srepositoryid = ?";
	    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
	    return count != null && count > 0;
	}

}
