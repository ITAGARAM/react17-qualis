package com.agaramtech.qualis.biobank.service.biodisposalsampleapproval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.biobank.model.BioDisposeForm;
import com.agaramtech.qualis.biobank.model.BioDisposeFormDetails;
import com.agaramtech.qualis.biobank.model.DisposalBatchType;
import com.agaramtech.qualis.biobank.model.FormType;
import com.agaramtech.qualis.biobank.model.StorageType;
import com.agaramtech.qualis.biobank.service.processedsamplereceiving.ProcessedSampleReceivingDAOImpl;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.StorageInstrument;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class BioDisposalSampleApprovalDAOImpl implements BioDisposalSampleApprovalDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(BioDisposalSampleApprovalDAOImpl.class);

	private final AuditUtilityFunction auditUtilityFunction;
	private final CommonFunction commonFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcTemplateUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final StringUtilityFunction stringUtilityFunction;
	private final EmailDAOSupport emailDAOSupport;


	@Override
	public ResponseEntity<Object> getDisposalSampleApproval(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		BioDisposalSampleApprovalDAOImpl.LOGGER.info("getBioDisposeFormss");
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		if (inputMap.containsKey("nBioDisposeFormcode")) {
			outputMap.put("nBioDisposeFormcode", inputMap.get("nBioDisposeFormcode"));
		}

		var fromDate = (String) inputMap.get("fromDate");
		var toDate = (String) inputMap.get("toDate");
		var ntransCode = inputMap.containsKey("ntransCode") ? (int) inputMap.get("ntransCode") : -1;

		if (!inputMap.containsKey("fromDate")) {
			final var mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
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

		final var getFilterStatus = getFilterStatus(userInfo);
		final List<Map<String, Object>> lstFilterStatus = new ArrayList<>();

		getFilterStatus.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getStransdisplaystatus());
			mapStatus.put("value", lst.getNtranscode());
			mapStatus.put("item", lst);
			lstFilterStatus.add(mapStatus);
		});
		var status = "";
		if (!inputMap.containsKey("ntransCode")) {
			ntransCode = getFilterStatus.get(0).getNtranscode();
		} else {
			ntransCode = (int) inputMap.get("ntransCode");
			if (ntransCode == 0) {

				status = getFilterStatus.stream().map(objtranscode -> String.valueOf(objtranscode.getNtranscode()))
						.collect(Collectors.joining(","));
			}

		}
		if (ntransCode == 0) {

			status = getFilterStatus.stream().map(objtranscode -> String.valueOf(objtranscode.getNtranscode()))
					.collect(Collectors.joining(","));
		}

		final var transCode = (short) ntransCode;
		final var selectedFilterStatus = lstFilterStatus.stream().filter(x -> (short) x.get("value") == transCode)
				.collect(Collectors.toList()).get(0);

		outputMap.put("lstFilterStatus", lstFilterStatus);
		outputMap.put("selectedFilterStatus", selectedFilterStatus);
		outputMap.put("realSelectedFilterStatus", selectedFilterStatus);

		final var strConditionTransCode = ntransCode < 0 ? ""
				: ntransCode == 0 ? " AND df.ntransactionstatus in (" + status + ") "
						: " AND df.ntransactionstatus in (" + ntransCode + ")";
		// final var strConditionTransCode = " AND df.ntransactionstatus in (" +
		// ntransCode + ")";

		final var strQuery = "SELECT df.nbiodisposeformcode, df.nthirdpartycode, df.sformnumber, df.ntransfertypecode, tt.stransfertypename, s.ssitename AS sreceiversitename, df.ntransactionstatus, "
				+ "COALESCE(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus, cm.scolorhexcode "
				+ "FROM biodisposeform df "
				+ "JOIN transfertype tt ON df.ntransfertypecode = tt.ntransfertypecode AND tt.nsitecode = "
				+ userInfo.getNmastersitecode() + " AND tt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN transactionstatus ts ON df.ntransactionstatus = ts.ntranscode AND ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN site s ON df.noriginsitecode = s.nsitecode AND s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN formwisestatuscolor fwsc ON fwsc.ntranscode = ts.ntranscode AND fwsc.nformcode = "
				+ userInfo.getNformcode() + " AND fwsc.nsitecode = " + userInfo.getNmastersitecode()
				+ " AND fwsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN colormaster cm ON fwsc.ncolorcode = cm.ncolorcode AND cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "WHERE df.nsitecode = "
				+ userInfo.getNtranssitecode() + " AND df.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND df.dtransactiondate BETWEEN '"
				+ fromDate + "' AND '" + toDate + "' " + "" + strConditionTransCode + ""
				+ "ORDER BY df.nbiodisposeformcode DESC;" + "";

		final List<BioDisposeForm> lstBioDisposeForm = jdbcTemplate.query(strQuery, new BioDisposeForm());

		if (!lstBioDisposeForm.isEmpty()) {
			outputMap.put("lstBioDisposeForm", lstBioDisposeForm);

			outputMap.put("selectedBioDisposeForm", lstBioDisposeForm.getFirst());

			final var lstChildBioDisposeForm = getChildInitialGet(lstBioDisposeForm.getFirst().getNbiodisposeformcode(),
					userInfo);
			outputMap.put("lstChildBioDisposeForm", lstChildBioDisposeForm);

			if (inputMap.containsKey(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())) {
				outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						inputMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
			}
		} else {

			outputMap.put("lstFilterStatus", lstFilterStatus);
			outputMap.put("selectedFilterStatus", selectedFilterStatus);
			outputMap.put("realSelectedFilterStatus", selectedFilterStatus);
			outputMap.put("lstBioDisposeForm", lstBioDisposeForm);
			outputMap.put("selectedBioDisposeForm", new ArrayList<>());
			outputMap.put("lstChildBioDisposeForm", new ArrayList<>());

		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getActiveDisposalSampleApproval(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final Object value = inputMap.get("nbiodisposeformcode");
		Integer nbiodisposeformcode = null;

		if (value instanceof Integer) {
			nbiodisposeformcode = (Integer) value;
		} else if (value instanceof String) {
			nbiodisposeformcode = Integer.valueOf((String) value);
		}
		//final var nbiodisposeformcode = (int) inputMap.get("nbiodisposeformcode");

		final var strQuery = "SELECT df.nbiodisposeformcode, df.nthirdpartycode, df.sformnumber, df.ntransfertypecode, tt.stransfertypename, s.ssitename AS sreceiversitename, df.ntransactionstatus, "
				+ "COALESCE(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus, cm.scolorhexcode "
				+ "FROM biodisposeform df "
				+ "JOIN transfertype tt ON df.ntransfertypecode = tt.ntransfertypecode AND tt.nsitecode = "
				+ userInfo.getNmastersitecode() + " AND tt.nstatus = "
				+ +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN transactionstatus ts ON df.ntransactionstatus = ts.ntranscode AND ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN site s ON df.noriginsitecode = s.nsitecode AND s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN formwisestatuscolor fwsc ON fwsc.ntranscode = ts.ntranscode AND fwsc.nformcode = "
				+ userInfo.getNformcode() + " AND fwsc.nsitecode = " + userInfo.getNmastersitecode()
				+ " AND fwsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN colormaster cm ON fwsc.ncolorcode = cm.ncolorcode AND cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " WHERE df.nsitecode = "
				+ userInfo.getNtranssitecode() + " AND df.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND df.nbiodisposeformcode="
				+ nbiodisposeformcode + " ORDER BY df.nbiodisposeformcode DESC;" + "";

		final var objBioDisposeForm = (BioDisposeForm) jdbcTemplateUtilityFunction.queryForObject(strQuery,
				BioDisposeForm.class, jdbcTemplate);

		if (objBioDisposeForm != null) {

			outputMap.put("selectedBioDisposeForm", objBioDisposeForm);

			final var lstChildBioDisposeForm = getChildInitialGet(nbiodisposeformcode, userInfo);

			outputMap.put("lstChildBioDisposeForm", lstChildBioDisposeForm);

		} else {
			outputMap.put("selectedBioDisposeForm", new ArrayList<>());
			outputMap.put("lstChildBioDisposeForm", new ArrayList<>());

		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	public List<TransactionStatus> getFilterStatus(final UserInfo userInfo) throws Exception {

		final var strFilterStatus = "select ts.ntranscode, ts.stransstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, ts.nstatus from transactionstatus ts,"
				+ " approvalstatusconfig ascf where " + " ts.ntranscode=ascf.ntranscode and ascf.nformcode="
				+ userInfo.getNformcode() + " and ascf.nstatusfunctioncode="
				+ Enumeration.ApprovalStatusFunction.FILTERSTATUS.getNstatustype() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ascf.nsitecode="
				+ userInfo.getNmastersitecode() + " order by ascf.nsorter";
		return jdbcTemplate.query(strFilterStatus, new TransactionStatus());
	}

	@Override
	public List<Map<String, Object>> getChildInitialGet(final int nBioDisposeFormcode, final UserInfo userInfo)
			throws Exception {

		final var strChildGet = "SELECT row_number() OVER (ORDER BY dfd.nbiodisposeformdetailscode DESC) AS "
				+ "nserialno, dfd.nbiodisposeformdetailscode, dfd.nbiodisposeformcode, dfd.nbioprojectcode,"
				+ " dfd.nsamplestoragetransactioncode,dfd.sparentsamplecode, dfd.ncohortno, dfd.nstoragetypecode, dfd.nproductcatcode, "
				+ "dfd.nproductcode, dfd.nsamplecondition, dfd.srepositoryid,COALESCE(NULLIF(dfd.slocationcode, ''), '-') AS slocationcode, dfd.svolume, "
				+ "dfd.ssubjectid, dfd.scasetype, dfd.ndiagnostictypecode, dfd.ncontainertypecode, "
				+ "COALESCE(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') AS ssamplecondition,"
				+ "COALESCE(ts3.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts3.jsondata->'stransdisplaystatus'->>'en-US') AS ssamplestatus,"
				+ " r.sreason,p.sproductname || '( '||pc.sproductcatname || ' )' as sproductname FROM biodisposeformdetails dfd "
				+ "JOIN transactionstatus ts2 ON dfd.nsamplecondition = ts2.ntranscode AND ts2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN transactionstatus ts3 ON dfd.ntransdisposestatus = ts3.ntranscode  AND ts3.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "LEFT JOIN reason r ON dfd.nreasoncode = r.nreasoncode  AND r.nsitecode="
				+ userInfo.getNmastersitecode() + "  AND r.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN product p ON p.nproductcode = dfd.nproductcode AND p.nsitecode = "
				+ userInfo.getNmastersitecode() + "  AND p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " JOIN productcategory pc ON pc.nproductcatcode = p.nproductcatcode AND pc.nsitecode = "
				+ userInfo.getNmastersitecode() + "  AND pc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " WHERE dfd.nsitecode = "
				+ userInfo.getNtranssitecode() + " AND dfd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND dfd.nbiodisposeformcode = "
				+ nBioDisposeFormcode + " " + " ORDER BY nserialno DESC; ";

		final var lstChildGet = jdbcTemplate.queryForList(strChildGet);

		return lstChildGet;

	}

	@Override
	public List<Map<String, Object>> completeDisposalSamplesApproval(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<Object> approvalDisposalSamples(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final var nbiodisposeformcode = (int) inputMap.get("nbiodisposeformcode");

		final var findStatus = findStatusDisposeSample(nbiodisposeformcode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			final var lstChildBioDisposeForm = getChildInitialGet(nbiodisposeformcode, userInfo);

			if (lstChildBioDisposeForm.size() > 0) {

				final var isCheck = getAccpectAndStoredSample(
						Enumeration.TransactionStatus.APPROVED.gettransactionstatus(), nbiodisposeformcode, "");

				if (isCheck) {
					
					final var strAuditQryForm = " select brt.nbiodisposeformcode,brt.sformnumber,coalesce("
							+ "    ts.jsondata -> 'stransdisplaystatus' ->> '" + userInfo.getSlanguagetypecode() + "', "
							+ "    ts.jsondata -> 'stransdisplaystatus' ->> 'en-US'"
							+ "  ) as stransdisplaystatus from biodisposeform brt"
							+ "    join transactionstatus ts on brt.ntransactionstatus = ts.ntranscode and ts.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and brt.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							+ "and brt.nbiodisposeformcode=" + nbiodisposeformcode + "  order by 1 desc";

					final var lstAuditBefore = (BioDisposeForm) jdbcTemplateUtilityFunction
							.queryForObject(strAuditQryForm, BioDisposeForm.class, jdbcTemplate);


					final var strUpdateDisposeSamplerDetails = "update biodisposeform set ntransactionstatus="
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
							+ " where nbiodisposeformcode=" + nbiodisposeformcode + " and nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					
					var seqNoBioDisposeFormHistory = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnobiobankmanagement where stablename='biodisposeformhistory' and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);
					seqNoBioDisposeFormHistory++;
					
					final var strInsertDIsposeFormHistory = 
						    "INSERT INTO biodisposeformhistory("
						    + "	nbiodisposeformhistorycode, nbiodisposeformcode, sformnumber, ntransfertypecode,"
						    + "	nformtypecode, ntransactionstatus, dtransactiondate, ntztransactiondate, "
						    + "	noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, "
						    + "	ndeputyuserrolecode, nsitecode, nstatus)"
						    + "	SELECT " +
						    seqNoBioDisposeFormHistory + ", " +
						    nbiodisposeformcode + ", sformnumber, ntransfertypecode,nformtypecode, " +
						    Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", '" +
						    dateUtilityFunction.getCurrentDateTime(userInfo) + "', " +
						    userInfo.getNtimezonecode() + ", " +
						    dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " +
						    userInfo.getNusercode() + ", " +
						    userInfo.getNuserrole() + ", " +
						    userInfo.getNdeputyusercode() + ", " +
						    userInfo.getNdeputyuserrole() + ", " +
						    userInfo.getNtranssitecode() + ", " +
						    Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
						    " FROM biodisposeform WHERE nbiodisposeformcode = "+nbiodisposeformcode+" ;";

					final StringBuilder strSeqNoUpdate=new StringBuilder();

					 strSeqNoUpdate.append("update seqnobiobankmanagement set nsequenceno=")
						.append(seqNoBioDisposeFormHistory).append(" where")
						.append(" stablename='biodisposeformhistory' and nstatus=")
						.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");
					

					jdbcTemplate.execute(strUpdateDisposeSamplerDetails+strInsertDIsposeFormHistory+strSeqNoUpdate);
					// ===== COC: START =====
//					if (lstChildBioDisposeForm != null && !lstChildBioDisposeForm.isEmpty()) {
//
//					    String sbiodisposeDetailsCode = lstChildBioDisposeForm.stream()
//					            .map(x -> String.valueOf(x.get("nbiodisposeformdetailscode")))
//					            .collect(Collectors.joining(","));
//
//					    if (sbiodisposeDetailsCode != null && !sbiodisposeDetailsCode.trim().isEmpty()) {
//
//							String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//							jdbcTemplate.execute(sQuery1);
//					 
//							String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//							jdbcTemplate.execute(sQuery2);
//					 
//							String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//							jdbcTemplate.execute(sQuery3);
//							
//					        int chainCustodyPk = jdbcTemplate.queryForObject(
//					                "select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//					                        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//					                Integer.class);
//
//					        String remarksPrefix = commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//					                + " ['||COALESCE(bdd.srepositoryid,'')||'] '||'"
//					                + commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//					                + " ['||COALESCE(bdd.sparentsamplecode,'')||'] '||'"
//					                + commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//					                + " ['||COALESCE(bd.sformnumber,'')||']'";
//
//
//					        String strChainCustody =
//					                "insert into chaincustody(" +
//					                        "nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,sitemno," +
//					                        "ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,ntztransactiondate," +
//					                        "noffsetdtransactiondate,sremarks,nsitecode,nstatus) " +
//					                "select " + chainCustodyPk + " + rank() over(order by bdd.nbiodisposeformdetailscode),"
//					                        + userInfo.getNformcode() + ", bdd.nbiodisposeformdetailscode, 'nbiodisposeformdetailscode', 'biodisposeformdetails', COALESCE(bd.sformnumber,''),"
//					                        + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", "
//					                        + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
//					                        + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
//					                        + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
//					                        + remarksPrefix + ", " + userInfo.getNtranssitecode() + ", "
//					                        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					                        + " from biodisposeformdetails bdd"
//					                        + " join biodisposeform bd on bd.nbiodisposeformcode = bdd.nbiodisposeformcode"
//					                        + " and bd.nsitecode = " + userInfo.getNtranssitecode()
//					                        + " and bd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					                        + " where bdd.nsitecode = " + userInfo.getNtranssitecode()
//					                        + " and bdd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					                        + " and bdd.nbiodisposeformdetailscode in (" + sbiodisposeDetailsCode + ");";
//
//					        String strSeqUpdate =
//					                " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
//					                        + " + count(nbiodisposeformdetailscode) from biodisposeformdetails"
//					                        + " where nbiodisposeformdetailscode in (" + sbiodisposeDetailsCode + ")"
//					                        + " and nsitecode = " + userInfo.getNtranssitecode()
//					                        + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					                        + ") where stablename = 'chaincustody' and nstatus = "
//					                        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//					        jdbcTemplate.execute(strChainCustody);
//					        jdbcTemplate.execute(strSeqUpdate);
//					    }
//					}
					// ===== COC: END =====

					
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> defaultListAfterSave = new ArrayList<>();
					final List<Object> defaultListBeforeSave = new ArrayList<>();
					
					final var lstAuditAfter = (BioDisposeForm) jdbcTemplateUtilityFunction
							.queryForObject(strAuditQryForm, BioDisposeForm.class, jdbcTemplate);
					
					lstAuditAfter.setSformnumber("");
					defaultListBeforeSave.add(lstAuditAfter);
					defaultListAfterSave.add(lstAuditBefore);
					multilingualIDList.add("IDS_APPROVEDDETAILS");

					auditUtilityFunction.fnInsertAuditAction(defaultListBeforeSave, 2, defaultListAfterSave, multilingualIDList,
							userInfo);

				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLEDISPOSEREJECT",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOSAMPLESAVAILABLE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			
			// Added By Mullai Balaji for Email jira ID-BGSI-147
			String Query = "select DISTINCT (ncontrolcode) from emailconfig where ncontrolcode="
					+ inputMap.get("ncontrolcode") + " " + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			Integer value = null;
			try {
				value = jdbcTemplate.queryForObject(Query, Integer.class);
			} catch (Exception e) {
				value = null;
			}
			if (value != null) {
				final Map<String, Object> mailMap = new HashMap<String, Object>();
				mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));
				mailMap.put("nbiodisposeformcode", nbiodisposeformcode);
				String query = "SELECT sformnumber FROM biodisposeform where nbiodisposeformcode="
						+ nbiodisposeformcode;
				String referenceId = jdbcTemplate.queryForObject(query, String.class);
				mailMap.put("ssystemid", referenceId);
				final UserInfo mailUserInfo = new UserInfo(userInfo);
				mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
				mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
				emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
			}

			return getActiveDisposalSampleApproval(inputMap, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	private boolean getAccpectAndStoredSample(int methodStatusCode, int nbiodisposeformcode,
			String nbiodisposeformdetailscode) throws Exception {

		var strStatus = "";

		if (methodStatusCode == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {

			strStatus = "SELECT CASE WHEN EXISTS ( SELECT * FROM biodisposeformdetails WHERE nbiodisposeformcode="
					+ nbiodisposeformcode + " " + "AND nsamplecondition="
					+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " " + "AND nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
					+ "THEN TRUE ELSE FALSE END AS isRejectSamplePresent;";

		} else if (methodStatusCode == Enumeration.TransactionStatus.STORED.gettransactionstatus()) {

			strStatus = "SELECT CASE WHEN EXISTS ( SELECT * FROM biodisposeformdetails WHERE nbiodisposeformdetailscode in ("
					+ nbiodisposeformdetailscode + ") AND nsamplecondition="
					+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " " + "AND nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
					+ "THEN TRUE ELSE FALSE END AS isRejectSamplePresent;";

		}

		return (Boolean) Objects.requireNonNullElse(
				jdbcTemplateUtilityFunction.queryForObject(strStatus, Boolean.class, jdbcTemplate), false);
	}

	@Override
	public ResponseEntity<Object> disposeSamplesApproval(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final var nbiodisposeformcode = (int) inputMap.get("nbiodisposeformcode");

		// final var nbiodisposeformdetailscode = (int)
		// inputMap.get("nbiodisposeformdetailscode");

		final var findStatus = findStatusDisposeSample(nbiodisposeformcode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {

			final var lstChildBioDisposeForm = getChildInitialGet(nbiodisposeformcode, userInfo);

			if (lstChildBioDisposeForm.size() > 0) {

				final var strStatus = "select STRING_AGG(nbiodisposeformdetailscode::TEXT, ',') AS ndisposedeatailstatuscode from biodisposeformdetails  "
						+ "where nbiodisposeformcode =" + nbiodisposeformcode + " and nsamplecondition="
						+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

				final var ndisposedeatailstatuscode = (String) jdbcTemplateUtilityFunction.queryForObject(strStatus,
						String.class, jdbcTemplate);

				final var strStatusValidation = "select * from biodisposeformdetails  " + "where nbiodisposeformcode ="
						+ nbiodisposeformcode + " and nsamplecondition="
						+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + ""
						+ " and ntransdisposestatus="
						+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

				final List<BioDisposeForm> lstBioDisposeForm = jdbcTemplate.query(strStatusValidation,
						new BioDisposeForm());

				if (lstBioDisposeForm.isEmpty() && ndisposedeatailstatuscode != null
						&& !ndisposedeatailstatuscode.trim().isEmpty()) {
					
					final var strAuditQryForm = " select brt.nbiodisposeformcode,brt.sformnumber,coalesce("
							+ "    ts.jsondata -> 'stransdisplaystatus' ->> '" + userInfo.getSlanguagetypecode() + "', "
							+ "    ts.jsondata -> 'stransdisplaystatus' ->> 'en-US'"
							+ "  ) as stransdisplaystatus from biodisposeform brt"
							+ "    join transactionstatus ts on brt.ntransactionstatus = ts.ntranscode and ts.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and brt.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
							+ "and brt.nbiodisposeformcode=" + nbiodisposeformcode + "  order by 1 desc";

					final var lstAuditBefore = (BioDisposeForm) jdbcTemplateUtilityFunction
							.queryForObject(strAuditQryForm, BioDisposeForm.class, jdbcTemplate);


					final var strUpdateDisposeSamplerDetails = "update biodisposeform set ntransactionstatus="
							+ Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()
							+ " where nbiodisposeformcode=" + nbiodisposeformcode + " and nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					final var strUpdateQry = "update biodisposeformdetails set ntransdisposestatus="
							+ Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()
							+ " where nbiodisposeformdetailscode in (" + ndisposedeatailstatuscode + ") and nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

//					final int intRetrievalPk = jdbcTemplate
//							.queryForObject(
//									"select nsequenceno from seqnostoragemanagement where "
//											+ "stablename='samplestorageretrieval' and nstatus="
//											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//									Integer.class);
//
//					// final var seqNoRetrievalCount = ndisposedeatailstatuscode.length();
//
//					final var seqNoRetrievalCount = ndisposedeatailstatuscode.split(",").length;
//
//					final var strSampleStorageRetrieval = new StringBuilder(
//							"insert into samplestorageretrieval (nsamplestorageretrievalcode,")
//							.append(" nsamplestoragetransactioncode, nsamplestoragelocationcode, nsamplestoragelistcode,")
//							.append(" nsamplestoragemappingcode, nprojecttypecode, nusercode, nuserrolecode, nbiosamplereceivingcode,")
//							.append(" nbiodirecttransfercode, sposition, spositionvalue, jsondata, ntransactionstatus, dtransactiondate,")
//							.append(" noffsetdtransactiondate, ntransdatetimezonecode, nsitecode, nstatus)")
//							.append(" select ").append(intRetrievalPk)
//							.append("+rank()over(order by bdtd.nbiodisposeformdetailscode),")
//							.append(" sst.nsamplestoragetransactioncode, sst.nsamplestoragelocationcode, -1,")
//							.append(" sst.nsamplestoragemappingcode, sst.nprojecttypecode, ")
//							.append(userInfo.getNusercode()).append(", ").append(userInfo.getNuserrole())
//							.append(", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ",")
//							.append(" bdtd.nbiodisposeformdetailscode, sst.sposition, sst.spositionvalue, sst.jsondata, ")
//							.append(Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()).append(", '")
//							.append(dateUtilityFunction.getCurrentDateTime(userInfo)).append("', ")
//							.append(dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()))
//							.append(", ").append(userInfo.getNtimezonecode()).append(", sst.nsitecode, ")
//							.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
//							.append(" from biodisposeformdetails bdtd ")
//							.append(" join samplestoragetransaction sst on bdtd.nsamplestoragetransactioncode=sst.nsamplestoragetransactioncode")
//							.append(" and sst.nsitecode=").append(userInfo.getNtranssitecode())
//							.append(" and sst.nstatus=")
//							.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
//							.append(" where bdtd.nsitecode=").append(userInfo.getNtranssitecode())
//							.append(" and bdtd.nstatus=")
//							.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
//							.append(" and bdtd.nbiodisposeformcode=").append(nbiodisposeformcode)
//							.append(" and bdtd.nbiodisposeformdetailscode in (").append(ndisposedeatailstatuscode)
//							.append(")").append(" order by bdtd.nbiodisposeformdetailscode").append(";");
//
//					strSampleStorageRetrieval.append("update seqnostoragemanagement set nsequenceno=")
//							.append(intRetrievalPk + seqNoRetrievalCount)
//							.append(" where stablename='samplestorageretrieval' and nstatus=")
//							.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");
//
					var seqNoBioDisposeFormHistory = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnobiobankmanagement where stablename='biodisposeformhistory' and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);
					seqNoBioDisposeFormHistory++;
					
					final var strInsertDIsposeFormHistory = 
						    "INSERT INTO biodisposeformhistory("
						    + "	nbiodisposeformhistorycode, nbiodisposeformcode, sformnumber, ntransfertypecode,"
						    + "	nformtypecode, ntransactionstatus, dtransactiondate, ntztransactiondate, "
						    + "	noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, "
						    + "	ndeputyuserrolecode, nsitecode, nstatus)  SELECT " +
						    seqNoBioDisposeFormHistory + ", " +
						    nbiodisposeformcode + ", sformnumber, ntransfertypecode,nformtypecode, " +
						    Enumeration.TransactionStatus.DISPOSED.gettransactionstatus() + ", '" +
						    dateUtilityFunction.getCurrentDateTime(userInfo) + "', " +
						    userInfo.getNtimezonecode() + ", " +
						    dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " +
						    userInfo.getNusercode() + ", " +
						    userInfo.getNuserrole() + ", " +
						    userInfo.getNdeputyusercode() + ", " +
						    userInfo.getNdeputyuserrole() + ", " +
						    userInfo.getNtranssitecode() + ", " +
						    Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
						    " FROM biodisposeform WHERE nbiodisposeformcode = "+nbiodisposeformcode+" ;";

					final StringBuilder strSeqNoUpdate=new StringBuilder();

					 strSeqNoUpdate.append("update seqnobiobankmanagement set nsequenceno=")
						.append(seqNoBioDisposeFormHistory).append(" where")
						.append(" stablename='biodisposeformhistory' and nstatus=")
						.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");
					
					final var strDeleteSampleStorageTransaction = "DELETE FROM samplestoragetransaction "
							+ "WHERE nsamplestoragetransactioncode IN (SELECT nsamplestoragetransactioncode "
							+ "FROM biodisposeformdetails WHERE nbiodisposeformdetailscode in ("
							+ ndisposedeatailstatuscode + ") " + "AND nsitecode=" + userInfo.getNtranssitecode()
							+ " AND nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ ");";

					// jdbcTemplate.execute(strDeleteSampleStorageTransaction);
					// strSampleStorageRetrieval.toString() +
					jdbcTemplate
							.execute(strUpdateDisposeSamplerDetails + strUpdateQry + strDeleteSampleStorageTransaction+strInsertDIsposeFormHistory+strSeqNoUpdate);

					
					// ===== COC: START =====NEW
					if (ndisposedeatailstatuscode != null && !ndisposedeatailstatuscode.trim().isEmpty()) {

						String sQuery1 = " lock  table lockregister "
								+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
						jdbcTemplate.execute(sQuery1);

						String sQuery2 = " lock  table lockquarantine "
								+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
						jdbcTemplate.execute(sQuery2);

						String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
						jdbcTemplate.execute(sQuery3);

						int chainCustodyPk = jdbcTemplate.queryForObject(
								"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
								Integer.class);

						String lblRepositoryId = stringUtilityFunction.replaceQuote(commonFunction
								.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename()));
						String lblParentSampleCode = stringUtilityFunction.replaceQuote(commonFunction
								.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename()));
						String lblDisposalFormNumber = stringUtilityFunction.replaceQuote(commonFunction
								.getMultilingualMessage("IDS_DISPOSALFORMNUMER", userInfo.getSlanguagefilename()));
						String lblReferenceFormNo = stringUtilityFunction.replaceQuote(commonFunction
								.getMultilingualMessage("IDS_REFERENCEFORMNO", userInfo.getSlanguagefilename()));
						String lblDisposedFromStore = stringUtilityFunction.replaceQuote(commonFunction
								.getMultilingualMessage("IDS_DISPOSEDFORMSTORE", userInfo.getSlanguagefilename()));
						String lblDisposedFromForm = stringUtilityFunction.replaceQuote(commonFunction
								.getMultilingualMessage("IDS_DISPOSEDFROMFORM", userInfo.getSlanguagefilename()));
						String lblSiteName = stringUtilityFunction.replaceQuote(
								commonFunction.getMultilingualMessage("IDS_SITENAME", userInfo.getSlanguagefilename()));

						String strChainCustody = "insert into chaincustody("
								+ "nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,sitemno,"
								+ "ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,"
								+ "noffsetdtransactiondate,sremarks,nsitecode,nstatus) " + "select " + chainCustodyPk
								+ " + rank() over(order by bdd.nbiodisposeformdetailscode), " + userInfo.getNformcode()
								+ ", "
								+ "bdd.nbiodisposeformdetailscode,'nbiodisposeformdetailscode','biodisposeformdetails',"
								+ "COALESCE(bdd.srepositoryid,''),"
								+ Enumeration.TransactionStatus.DISPOSED.gettransactionstatus() + ", "
								+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
								+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
								+ " CASE WHEN bdd.nbiomovetodisposedetailscode = -1 THEN '" + lblDisposedFromStore
								+ "' ELSE '" + lblDisposedFromForm + "' END || ' ' || '" + lblRepositoryId
								+ " [' || COALESCE(bdd.srepositoryid,'') || '] ' || '" + lblParentSampleCode
								+ " [' || COALESCE(bdd.sparentsamplecode,'') || '] ' || '" + lblDisposalFormNumber
								+ " [' || COALESCE(bd.sformnumber,'') || '] ' || '" + lblReferenceFormNo + " [' || "
								+ " CASE WHEN bdd.nbiomovetodisposedetailscode != -1 "
								+ "      THEN COALESCE(bmtd.sformnumber,'') " + "      ELSE '-' END " + " || '] ' || '"
								+ lblSiteName + " [' || COALESCE(si.ssitename,'') || ']'" + ", "
								+ userInfo.getNtranssitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " from biodisposeformdetails bdd"
								+ " join biodisposeform bd on bd.nbiodisposeformcode = bdd.nbiodisposeformcode"
								+ " and bd.nsitecode = " + userInfo.getNtranssitecode() + " and bd.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " left join biomovetodispose bmtd on bmtd.nbiomovetodisposecode = bdd.nbiomovetodisposecode"
								+ " and bmtd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and bmtd.nsitecode = bdd.nsitecode"
								+ " left join site si on si.nsitecode = bdd.nsitecode" + " and si.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " where bdd.nsitecode = " + userInfo.getNtranssitecode() + " and bdd.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and bd.nbiodisposeformcode in (" + nbiodisposeformcode + ");";

						String strSeqUpdate = " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
								+ " + count(nbiodisposeformdetailscode) from biodisposeformdetails"
								+ " where nbiodisposeformcode in (" + nbiodisposeformcode + ")" + " and nsitecode = "
								+ userInfo.getNtranssitecode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ ") where stablename = 'chaincustody' and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						jdbcTemplate.execute(strChainCustody);
						jdbcTemplate.execute(strSeqUpdate);
					}
					// ===== COC: END =====

					
					
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> defaultListAfterSave = new ArrayList<>();
					final List<Object> defaultListBeforeSave = new ArrayList<>();
					
					final var lstAuditAfter = (BioDisposeForm) jdbcTemplateUtilityFunction
							.queryForObject(strAuditQryForm, BioDisposeForm.class, jdbcTemplate);
					
					lstAuditAfter.setSformnumber("");
					defaultListBeforeSave.add(lstAuditAfter);
					defaultListAfterSave.add(lstAuditBefore);
					multilingualIDList.add("IDS_DISPOSESAMPLE");

					auditUtilityFunction.fnInsertAuditAction(defaultListBeforeSave, 2, defaultListAfterSave, multilingualIDList,
							userInfo);
				
				
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESHOULDBEINSTORED",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOSAMPLESAVAILABLE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			return getActiveDisposalSampleApproval(inputMap, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	private ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final var strStorageCondition = "select nstorageconditioncode, sstorageconditionname from storagecondition where "
				+ " nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nstorageconditioncode > 0"
				+ " order by nstorageconditioncode";
		final var lstGetStorageCondition = jdbcTemplate.queryForList(strStorageCondition);

		final List<Map<String, Object>> lstStorageCondition = new ArrayList<>();
		lstGetStorageCondition.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("sstorageconditionname"));
			mapStatus.put("value", lst.get("nstorageconditioncode"));
			mapStatus.put("item", lst);
			lstStorageCondition.add(mapStatus);
		});

		outputMap.put("lstStorageCondition", lstStorageCondition);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	private ResponseEntity<Object> getStorageType(final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var strStorageType = "select nstoragetypecode, coalesce(jsondata->>'en-US', jsondata->>'en-US')"
				+ " sstoragetypename from storagetype where nsitecode=" + userInfo.getNmastersitecode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nstoragetypecode > 0";
		final List<StorageType> lstGetStorageType = jdbcTemplate.query(strStorageType, new StorageType());

		final List<Map<String, Object>> lstStorageType = new ArrayList<>();

		lstGetStorageType.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getSstoragetypename());
			mapStatus.put("value", lst.getNstoragetypecode());
			mapStatus.put("item", lst);
			lstStorageType.add(mapStatus);
		});

		outputMap.put("lstStorageType", lstStorageType);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateSampleCondition(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final var nreasonCode = (int) inputMap.get("nreasoncode");

		final var nsampleCondition = (int) inputMap.get("nsamplecondition");

		final var nbiodisposeformdetailscode = (String) inputMap.get("nbiodisposeformdetailscode");

		final var nbiodisposeformcode = (int) inputMap.get("nbiodisposeformcode");

		final var findStatus = findStatusDisposeSample(nbiodisposeformcode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			
			final var strAuditAfterQry = auditQuery(nbiodisposeformcode, nbiodisposeformdetailscode,
					userInfo);
			final List<BioDisposeForm> lstAuditTransferDetailsBefore = jdbcTemplate
					.query(strAuditAfterQry, new BioDisposeForm());

			final var strUpdateQry = "update biodisposeformdetails set nreasoncode=" + nreasonCode
					+ ", nsamplecondition=" + nsampleCondition + " where nbiodisposeformdetailscode in ("
					+ nbiodisposeformdetailscode + ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			jdbcTemplate.execute(strUpdateQry);
			
			// ===== COC: START =====
//			if (nbiodisposeformdetailscode != null && !String.valueOf(nbiodisposeformdetailscode).trim().isEmpty()) {
//
//				String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery1);
//		 
//				String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery2);
//		 
//				String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery3);
//				
//			    int chainCustodyPk = jdbcTemplate.queryForObject(
//			        "select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus=" 
//			        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
//
//			    String remarksPrefix = commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename()) 
//			            + "'||' ['||COALESCE(bdd.srepositoryid,'')||'] '||'" 
//			            + commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename()) 
//			            + " ['||COALESCE(bdd.sparentsamplecode,'')||'] '||'" 
//			            + commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename()) 
//			            + " '";
//
//			    String strChainCustody = "insert into chaincustody("
//			        + "nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,sitemno,"
//			        + "ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,"
//			        + "noffsetdtransactiondate,sremarks,nsitecode,nstatus)"
//			        + " select " + chainCustodyPk + "+rank()over(order by bdd.nbiodisposeformdetailscode),"
//			        + userInfo.getNformcode() + ","
//			        + "bdd.nbiodisposeformdetailscode,'nbiodisposeformdetailscode','biodisposeformdetails',COALESCE(bd.sformnumber,''),"
//			        + nsampleCondition + ","
//			        + userInfo.getNusercode() + "," + userInfo.getNuserrole() + ",'"
//			        + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtimezonecode() + ","
//			        + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ",'"
//			        + remarksPrefix + "||COALESCE(bd.sformnumber,'')," + userInfo.getNtranssitecode() + ","
//			        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//			        + " from biodisposeformdetails bdd join biodisposeform bd on bd.nbiodisposeformcode=bdd.nbiodisposeformcode"
//			        + " and bd.nsitecode=" + userInfo.getNtranssitecode()
//			        + " and bd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//			        + " where bdd.nsitecode=" + userInfo.getNtranssitecode()
//			        + " and bdd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//			        + " and bdd.nbiodisposeformdetailscode in(" + nbiodisposeformdetailscode + ");";
//
//			    String strSeqUpdate = " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
//			            + " + count(nbiodisposeformdetailscode) from biodisposeformdetails"
//			            + " where nbiodisposeformdetailscode in(" + nbiodisposeformdetailscode + ")"
//			            + " and nsitecode=" + userInfo.getNtranssitecode()
//			            + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//			            + ") where stablename='chaincustody' and nstatus="
//			        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//			    jdbcTemplate.execute(strChainCustody);
//			    jdbcTemplate.execute(strSeqUpdate);
//			}
			// ===== COC: END =====			
			
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> lstAuditBefore = new ArrayList<>();
			final List<Object> lstAuditAfter = new ArrayList<>();
			
			final List<BioDisposeForm> lstAuditTransferDetailsAfter = jdbcTemplate
					.query(strAuditAfterQry, new BioDisposeForm());
			
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_SAMPLEVALIDATION"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		return getActiveDisposalSampleApproval(inputMap, userInfo);

	}

	private ResponseEntity<Object> getCourier(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final var strCourier = "select ncouriercode, scouriername from courier where nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ncouriercode > 0 order by ncouriercode";
		final var lstGetCourier = jdbcTemplate.queryForList(strCourier);

		final List<Map<String, Object>> lstCourier = new ArrayList<>();
		lstGetCourier.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("scouriername"));
			mapStatus.put("value", lst.get("ncouriercode"));
			mapStatus.put("item", lst);
			lstCourier.add(mapStatus);
		});

		outputMap.put("lstCourier", lstCourier);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getReason(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final var strReason = "select nreasoncode, sreason from reason where nsitecode=" + userInfo.getNmastersitecode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nreasoncode";
		final var lstGetReason = jdbcTemplate.queryForList(strReason);
		final List<Map<String, Object>> lstReason = new ArrayList<>();

		lstGetReason.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("sreason"));
			mapStatus.put("value", lst.get("nreasoncode"));
			mapStatus.put("item", lst);
			lstReason.add(mapStatus);
		});

		outputMap.put("lstReason", lstReason);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getDisposalBatchType(final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var strDisposalBatchType = " Select ndisposalbatchtypecode,coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') as sdisposalbatchtypename from disposalbatchtype  where nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ndefaultstatus asc ";
		final List<DisposalBatchType> lstObjDisposalBatchType = jdbcTemplate.query(strDisposalBatchType,
				new DisposalBatchType());

		final List<Map<String, Object>> lstDisposalBatchType = new ArrayList<>();

		lstObjDisposalBatchType.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getSdisposalbatchtypename());
			mapStatus.put("value", lst.getNdisposalbatchtypecode());
			mapStatus.put("item", lst);
			lstDisposalBatchType.add(mapStatus);
		});

		outputMap.put("lstDisposalBatchTypeCombo", lstDisposalBatchType);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getDisposalBatchFormType(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var strFormType = "select nformtypecode, coalesce(jsondata->'sformtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "', jsondata->'sformtypename'->>'en-US') sformtypename"
				+ " from formtype where nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nformtypecode";

		final List<FormType> lstObjFormType = jdbcTemplate.query(strFormType, new FormType());

		final List<Map<String, Object>> lstFormType = new ArrayList<>();

		lstObjFormType.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getSformtypename());
			mapStatus.put("value", lst.getNformtypecode());
			mapStatus.put("item", lst);
			lstFormType.add(mapStatus);
		});

		outputMap.put("lstFormType", lstFormType);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getFormTypeSiteBasedFormNumber(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

//		final var strOptionQry = "select nbiomovetodisposecode,sformnumber,nformtypecode ,nthirdpartycode "
//				+ "from biomovetodispose where nformtypecode=" + inputMap.get("nformtypecode") + " and nsitecode="
//				+ userInfo.getNtranssitecode() + " and nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nbiomovetodisposecode desc";

		final var strOptionQry = "SELECT cj.nbiomovetodisposecode, cj.sformnumber, cj.nformtypecode, cj.nthirdpartycode,  "
				+ "  COALESCE(bdf_count.biodisposeformdetails_count, 0) AS biodisposeformdetails_count,  "
				+ "  COALESCE(bdfd_count.biomovetodisposedetails_count, 0) AS biomovetodisposedetails_count "
				+ "FROM biomovetodispose cj "
				+ "LEFT JOIN (SELECT nbiomovetodisposecode, COUNT(*) AS biodisposeformdetails_count FROM biodisposeformdetails WHERE nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " GROUP BY nbiomovetodisposecode) bdf_count ON bdf_count.nbiomovetodisposecode = cj.nbiomovetodisposecode "
				+ "LEFT JOIN (SELECT nbiomovetodisposecode, COUNT(*) AS biomovetodisposedetails_count FROM biomovetodisposedetails WHERE nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " GROUP BY nbiomovetodisposecode) bdfd_count ON bdfd_count.nbiomovetodisposecode = cj.nbiomovetodisposecode "
				+ "WHERE cj.nformtypecode = " + inputMap.get("nformtypecode") + " AND cj.nsitecode = "
				+ userInfo.getNtranssitecode() + " AND cj.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND COALESCE(bdfd_count.biomovetodisposedetails_count, 0) <> COALESCE(bdf_count.biodisposeformdetails_count, 0) "
				+ "ORDER BY cj.nbiomovetodisposecode DESC; ";

		final var lststrOptionQry = jdbcTemplate.queryForList(strOptionQry);

		final List<Map<String, Object>> lstOptionForm = new ArrayList<>();

		lststrOptionQry.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("sformnumber"));
			mapStatus.put("value", lst.get("nbiomovetodisposecode"));
			mapStatus.put("item", lst);
			lstOptionForm.add(mapStatus);
		});

		outputMap.put("lstFormNumber", lstOptionForm);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getFormTypeBasedSiteAndThirdParty(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var strOptionQry = "SELECT  md.noriginsitecode,CASE  WHEN md.nformtypecode = "
				+ Enumeration.FormType.Transfer.getnformtype() + " THEN s.ssitename WHEN md.nformtypecode = "
				+ Enumeration.FormType.Return.getnformtype()
				+ " AND md.nthirdpartycode > 0 THEN CONCAT(s.ssitename, ' (', tp.sthirdpartyname, ')') "
				+ " WHEN md.nformtypecode = " + Enumeration.FormType.Return.getnformtype()
				+ " THEN s.ssitename ELSE NULL  END AS formsiteorthirdpartyname  FROM biomovetodispose md JOIN site s ON md.noriginsitecode = s.nsitecode "
				+ "JOIN thirdparty tp ON md.nthirdpartycode = tp.nthirdpartycode " + "WHERE nformtypecode="
				+ inputMap.get("nformtypecode") + " AND md.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and md.nsitecode="
				+ userInfo.getNtranssitecode() + " and tp.nsitecode=" + userInfo.getNmastersitecode() + " "
				+ "and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and tp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and md.nbiomovetodisposecode=" + inputMap.get("nbiomovetodisposecode")
				+ " order by nbiomovetodisposecode desc";

		final var lststrOptionQry = jdbcTemplate.queryForList(strOptionQry);

		final var formsiteorthirdpartyname = lststrOptionQry.get(0).get("formsiteorthirdpartyname");

		outputMap.put("formsiteorthirdpartyname", formsiteorthirdpartyname);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getExtractedColumnData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		var strQuery = "";

		strQuery = "select * from containertype where nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		var list = jdbcTemplate.queryForList(strQuery);

		outputMap.put("containerType", list);

		strQuery = "select * from containerstructure where nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		list = jdbcTemplate.queryForList(strQuery);
		outputMap.put("containerStructure", list);

		strQuery = "select p.nproductcode as nsampletypecode, sproductname from samplestoragetransaction sst, product p where"
				+ " sst.nproductcode=p.nproductcode and  sst.nsitecode=" + userInfo.getNtranssitecode()
				+ " and p.nsitecode=" + userInfo.getNmastersitecode() + " and sst.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by p.nproductcode,"
				+ " sproductname order by 1 desc";
		list = jdbcTemplate.queryForList(strQuery);

		outputMap.put("sampleType", list);

		strQuery = "select i.sinstrumentid,i.ninstrumentcode from  storageinstrument si,instrument i "
				+ "where si.ninstrumentcode=i.ninstrumentcode and  i.ninstrumentcode>0 and i.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and si.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and si.nregionalsitecode="
				+ userInfo.getNtranssitecode() + "  order by 1 desc ";

		list = jdbcTemplate.queryForList(strQuery);

		outputMap.put("lstInstrument", list);

		List<Map<String, Object>> lsTthirdPartysharableMap = new ArrayList<>();
		List<Map<String, Object>> lssampleaccesabletMap = new ArrayList<>();

		// Third-party sharable YES
		Map<String, Object> sharableYes = new HashMap<>();
		sharableYes.put("sthirdpartysharable",
				commonFunction.getMultilingualMessage("IDS_YES", userInfo.getSlanguagefilename()));
		sharableYes.put("nisthirdpartysharable", Enumeration.TransactionStatus.YES.gettransactionstatus());
		lsTthirdPartysharableMap.add(sharableYes);

		// Third-party sharable NO
		Map<String, Object> sharableNo = new HashMap<>();
		sharableNo.put("sthirdpartysharable",
				commonFunction.getMultilingualMessage("IDS_NO", userInfo.getSlanguagefilename()));
		sharableNo.put("nisthirdpartysharable", Enumeration.TransactionStatus.NO.gettransactionstatus());
		lsTthirdPartysharableMap.add(sharableNo);

		// Sample accessible YES
		Map<String, Object> sampleAccessYes = new HashMap<>();
		sampleAccessYes.put("ssampleaccesable",
				commonFunction.getMultilingualMessage("IDS_YES", userInfo.getSlanguagefilename()));
		sampleAccessYes.put("nissampleaccesable", Enumeration.TransactionStatus.YES.gettransactionstatus());
		lssampleaccesabletMap.add(sampleAccessYes);

		// Sample accessible NO
		Map<String, Object> sampleAccessNo = new HashMap<>();
		sampleAccessNo.put("ssampleaccesable",
				commonFunction.getMultilingualMessage("IDS_NO", userInfo.getSlanguagefilename()));
		sampleAccessNo.put("nissampleaccesable", Enumeration.TransactionStatus.NO.gettransactionstatus());
		lssampleaccesabletMap.add(sampleAccessNo);

		// Add to outputMap
		outputMap.put("nisthirdpartysharable", lsTthirdPartysharableMap);
		outputMap.put("nissampleaccesable", lssampleaccesabletMap);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getDynamicDisposeFilterData(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		var conditionString = Objects.toString("" + inputMap.get("filterquery"), "");
		
		final var scollate = "collate \"default\"";
		if (conditionString.contains("LIKE")) {

			while (conditionString.contains("LIKE")) {
				final var sb = conditionString;
				var sQuery = conditionString;
				final var colanindex = sb.indexOf("LIKE '");
				final var str1 = sQuery.substring(0, colanindex + 6);
				sQuery = sQuery.substring(colanindex + 6);
				final var sb3 = new StringBuilder(str1);
				final var sb4 = new StringBuilder(sQuery);
				sb3.replace(colanindex, colanindex + 4, "ilike");
				System.out.println(sQuery);
				final var indexofsv = sQuery.indexOf("'");

				sb4.replace(indexofsv, indexofsv + 1, "'" + scollate + " ");
				conditionString = sb3.toString() + sb4.toString();
			}

		}

		final var strFilterQuery = " SELECT * FROM ( SELECT  " + Enumeration.TransactionStatus.NA.gettransactionstatus()
				+ " nbiomovetodisposecode,nsamplestoragetransactioncode,p.sproductname || '( '||pc.sproductcatname || ' )' as sproductname,p.nproductcode AS nsampletypecode,nsamplestoragetransactioncode, ninstrumentcode, nprojecttypecode as nbioprojectcode, spositionvalue, nbioparentsamplecode, nbiosamplereceivingcode, sparentsamplecode, ncohortno, sst.nproductcatcode,"
				+ "sqty, slocationcode, sst.ssubjectid, sst.scasetype, nstoragetypecode,ndiagnostictypecode, ncontainertypecode, "
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " as nreasoncode,"
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " as nbiomovetodisposedetailscode,"
				+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + " as nsamplecondition , "
				+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + " as nsamplestatus "
				+ ",bsd.nissampleaccesable,bsd.nisthirdpartysharable " + "FROM samplestoragetransaction sst,product p,productcategory pc "
				+ ",biosubjectdetails bsd  where sst.nproductcode=p.nproductcode and p.nproductcatcode=pc.nproductcatcode "
				+ "and sst.ssubjectid=bsd.ssubjectid " + "and bsd.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sst.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sst.nsitecode="
				+ userInfo.getNtranssitecode() + " and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nsitecode="
				+ userInfo.getNmastersitecode() + " "
				+" and pc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and pc.nsitecode="+ userInfo.getNmastersitecode() + " "
				+ "and nsamplestoragetransactioncode not in( SELECT dfd.nsamplestoragetransactioncode "
				+ "FROM biodisposeformdetails dfd JOIN biodisposeform df ON df.nbiodisposeformcode = dfd.nbiodisposeformcode "
				+ "WHERE df.ntransactionstatus IN (" + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ") AND dfd.nsamplecondition IN ( "
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ","+Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() +" ) AND dfd.ntransdisposestatus = "
				+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + " AND df.nsitecode = "
				+ userInfo.getNtranssitecode() + " AND df.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND  dfd.nsitecode="
				+ userInfo.getNtranssitecode() + " AND dfd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " )" + ") AS tempquery where "
				+ conditionString + " ";

		final var lstfilterQry = jdbcTemplate.queryForList(strFilterQuery);

		BioDisposalSampleApprovalDAOImpl.LOGGER.info("FilterQuery --> " + strFilterQuery);

		outputMap.put("lstformTypeStoreFilter", lstfilterQry);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getSampleReceivingDetails(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var nDisposalBatchTypeCode = inputMap.get("ndisposalbatchtypecode");

		final var nFormTypeCode = inputMap.get("nformtypecode");

		final var nBioMoveToDisposeCode = inputMap.get("nbiomovetodisposecode");

		// Added ssubjectid by Gowtham R on nov 13 2025 for jira.id:BGSI-118
		//modified by sujatha ATE_274 by selecting jsondata form the biomovetodisposaldetails table used to store it into biodisposalformdetails's jsondata BGSI-218
		final var strFilterQuery = "SELECT mmd.nbiomovetodisposecode,mmd.nbioprojectcode,mmd.nbiomovetodisposedetailscode,"
				+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " as nsamplestoragetransactioncode, "
				+ "mmd.ncohortno,COALESCE(svolume, '') AS sqty, "
				+ "COALESCE(mmd.jsondata->>'srepositoryid', '') AS  spositionvalue, "
				+ "COALESCE(mmd.jsondata->>'sparentsamplecode', '') AS sparentsamplecode,  "
				+ "COALESCE(mmd.jsondata->>'ssubjectid', '') AS ssubjectid, "
				+ " mmd.jsondata::jsonb, p.sproductname || '( '||pc.sproductcatname || ' )' as sproductname,mmd.nproductcatcode,mmd.nproductcode as nsampletypecode,mmd.nsamplecondition, "
				+ "COALESCE(NULLIF(mmd.jsondata->>'slocationcode', ''), '-') AS slocationcode, "
				+ "COALESCE(NULLIF(mmd.jsondata->>'scasetype', ''), '-') AS scasetype, "
				+ "mmd.ndiagnostictypecode,mmd.ncontainertypecode,mmd.nstoragetypecode,mmd.nreasoncode,mmd.nsamplestatus "
				+ "FROM biomovetodisposedetails mmd JOIN product p ON mmd.nproductcode = p.nproductcode JOIN productcategory pc ON p.nproductcatcode=pc.nproductcatcode "
				+ "WHERE  mmd.nbiomovetodisposedetailscode not in (select nbiomovetodisposedetailscode from biodisposeformdetails where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  ) "
				+ "and  mmd.nbiomovetodisposecode = " + nBioMoveToDisposeCode + " and mmd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and mmd.nsitecode="
				+ userInfo.getNtranssitecode() + " and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nsitecode="
				+ userInfo.getNmastersitecode() + " "
				+"and pc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and pc.nsitecode="
				+ userInfo.getNmastersitecode() + " ;";

		final var lstfilterQry = jdbcTemplate.queryForList(strFilterQuery);

		BioDisposalSampleApprovalDAOImpl.LOGGER.info("FilterQuery --> " + strFilterQuery);

		outputMap.put("lstGetSampleReceivingDetails", lstfilterQry);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createDisposalSamplesApproval(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var objMapper = new ObjectMapper();

		final var objBioDisposeFormData = objMapper.convertValue(inputMap.get("biodisposeform"), BioDisposeForm.class);

		final BioDisposeForm objDisposeSample = null;

		var seqNoBioDisposeFormCode = -1;
		var strInsertRequestBasedTransfer = "";
		var strInsertDisposeTransferHistory="";
		final var strSeqNoUpdate = new StringBuilder();
		var nBioDisposeFormCode = (int) inputMap.get("nbiodisposeformcode");

		if (nBioDisposeFormCode < 0) {
			// final var strQuery = "";
			// final var lstBioECatlogRecord = jdbcTemplate.queryForList(strQuery);

			var nthirdpartycode = Enumeration.TransactionStatus.NA.gettransactionstatus();
			final var norginsitecode = userInfo.getNtranssitecode();
			var nformtypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();
			var ntransfertypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

			seqNoBioDisposeFormCode = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biodisposeform' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioDisposeFormCode++;
			nBioDisposeFormCode = seqNoBioDisposeFormCode;

			final var strformat = projectDAOSupport.getSeqfnFormat("biodisposeform", "seqnoformatgeneratorbiobank", 0,
					0, userInfo);

			if (objBioDisposeFormData.getNdisposalbatchtypecode() > 0) {
				final var moveDisposeValue = " SELECT nthirdpartycode,nformtypecode,ntransfertypecode from biomovetodispose "
						+ "where nbiomovetodisposecode=" + objBioDisposeFormData.getNdisposalbatchtypecode() + " "
						+ "AND nsitecode=" + userInfo.getNtranssitecode() + " AND nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

				final var objDisposeFormDetalis = (BioDisposeForm) jdbcTemplateUtilityFunction
						.queryForObject(moveDisposeValue, BioDisposeForm.class, jdbcTemplate);

				if (objDisposeFormDetalis != null) {
					ntransfertypecode = Optional.ofNullable(objDisposeFormDetalis.getNtransfertypecode())
							.orElse((short) Enumeration.TransactionStatus.NA.gettransactionstatus());
					nformtypecode = Optional.ofNullable(objDisposeFormDetalis.getNformtypecode())
							.orElse((short) Enumeration.TransactionStatus.NA.gettransactionstatus());
					nthirdpartycode = Optional.ofNullable(objDisposeFormDetalis.getNthirdpartycode())
							.orElse(Enumeration.TransactionStatus.NA.gettransactionstatus());
				} else {

					ntransfertypecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
					nformtypecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
					nthirdpartycode = Enumeration.TransactionStatus.NA.gettransactionstatus();
				}

			}
			nBioDisposeFormCode = seqNoBioDisposeFormCode;
			strInsertRequestBasedTransfer = "Insert into biodisposeform( nbiodisposeformcode, sformnumber, ntransfertypecode,"
					+ " nformtypecode, noriginsitecode, nthirdpartycode, ntransactionstatus, dtransactiondate, "
					+ "ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)" + "values ("
					+ seqNoBioDisposeFormCode + ", '" + strformat + "'," + ntransfertypecode + "," + nformtypecode + ","
					+ norginsitecode + ", " + nthirdpartycode + ", "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " , '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ");";

			strSeqNoUpdate.append("update seqnobiobankmanagement set nsequenceno=").append(seqNoBioDisposeFormCode)
					.append(" where").append(" stablename='biodisposeform' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
					.append(";");
			
			
			var seqNoBioDisposeFormHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biodisposeformhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioDisposeFormHistory++;
			
			 strInsertDisposeTransferHistory  = "INSERT INTO biodisposeformhistory("
			 		+ "	nbiodisposeformhistorycode, nbiodisposeformcode, sformnumber, ntransfertypecode, nformtypecode,"
			 		+ "	ntransactionstatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
			 		+ "	nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus)"
			 		+ "	VALUES(" +seqNoBioDisposeFormHistory+"," + " "
						+ seqNoBioDisposeFormCode + ",'"+strformat+"',"+ntransfertypecode+","+nformtypecode+"," + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
						+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ ");";
			
				objBioDisposeFormData.setSformnumber(strformat);
			 strSeqNoUpdate.append("update seqnobiobankmanagement set nsequenceno=")
			.append(seqNoBioDisposeFormHistory).append(" where")
			.append(" stablename='biodisposeformhistory' and nstatus=")
			.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");

		} else {

			BioDisposalSampleApprovalDAOImpl.LOGGER.info("Save and Continue successful.");

		}

		Map<String, Object> rtnQry = new HashMap<>();


		rtnQry = insertBioDisposeFormDetails(objBioDisposeFormData,
				(List<Map<String, Object>>) inputMap.get("filterSelectedSamples"), seqNoBioDisposeFormCode, userInfo);

		final var childRecord = (String) rtnQry.get("InsertQuery");

		jdbcTemplate.execute(strInsertRequestBasedTransfer + childRecord + strSeqNoUpdate.toString()+strInsertDisposeTransferHistory);

		// ===== COC: START =====
//		String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//		jdbcTemplate.execute(sQuery1);
// 
//		String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//		jdbcTemplate.execute(sQuery2);
// 
//		String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//		jdbcTemplate.execute(sQuery3);
//
//		int chainCustodyPk = jdbcTemplate
//		        .queryForObject("select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//		                + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
//
//		String strChainCustody = "insert into chaincustody(nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,sitemno,ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,noffsetdtransactiondate,sremarks,nsitecode,nstatus) "
//		        + "select (" + chainCustodyPk + " + rank() over(order by bdfd.nbiodisposeformdetailscode)), " + userInfo.getNformcode()
//		        + ",bdfd.nbiodisposeformdetailscode,'nbiodisposeformdetailscode','biodisposeformdetails',bdf.sformnumber,"
//		        + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + userInfo.getNusercode() + ","
//		        + userInfo.getNuserrole() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
//		        + userInfo.getNtimezonecode() + ","
//		        + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ",'"
//		        + "Repository ID [' || coalesce(bdfd.srepositoryid,'') || '] ' || '"
//		        + commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//		        + " [' || coalesce(bdfd.sparentsamplecode,'') || '] ' || '"
//		        + commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//		        + " ' || bdf.sformnumber," + userInfo.getNtranssitecode() + ","
//		        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//		        + " from biodisposeformdetails bdfd join biodisposeform bdf on bdf.nbiodisposeformcode=bdfd.nbiodisposeformcode and bdf.nsitecode="
//		        + userInfo.getNtranssitecode() + " and bdf.nstatus="
//		        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bdfd.nsitecode="
//		        + userInfo.getNtranssitecode() + " and bdfd.nstatus="
//		        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bdfd.nbiodisposeformcode="
//		        + nBioDisposeFormCode + ";";
//
//		String strSeqUpdateCOC = " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
//				+ "+ count(nbiodisposeformdetailscode) from"
//				+ " biodisposeformdetails where nbiodisposeformcode in ("
//				+ nBioDisposeFormCode + ") and nsitecode="
//				+ userInfo.getNtranssitecode() + " and nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") where stablename="
//				+ "'chaincustody' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//		
//		jdbcTemplate.execute(strChainCustody);
//		jdbcTemplate.execute(strSeqUpdateCOC);
		// ===== COC: END =====

		
		final var ntransCode = (int) inputMap.get("ntransCode");
		inputMap.put("ntransCode",
				ntransCode == 0 ? ntransCode : Enumeration.TransactionStatus.DRAFT.gettransactionstatus());

		inputMap.put("nbiodisposeformcode", nBioDisposeFormCode);

		final List<Object> lstAuditAfter = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final var selectedChildRecord = (String) rtnQry.get("selectedChildRecord");
		final var strAuditQry = auditQuery(nBioDisposeFormCode, selectedChildRecord, userInfo);
		 final List<BioDisposeForm> lstAuditDisposeFormDetailsAfter =
		 jdbcTemplate.query(strAuditQry,new BioDisposeForm());
		 lstAuditAfter.addAll(lstAuditDisposeFormDetailsAfter);
		 lstAuditDisposeFormDetailsAfter.stream().forEach(x ->
		 multilingualIDList.add("IDS_ADDDISPOSESAMPLES"));
		auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);

		// return new ResponseEntity<>(outputMap, HttpStatus.OK);
		
		// Added By Mullai Balaji for Email jira ID-BGSI-147
					String Query = "select DISTINCT (ncontrolcode) from emailconfig where ncontrolcode="
							+ inputMap.get("ncontrolcode") + " " + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					Integer value = null;
					try {
						value = jdbcTemplate.queryForObject(Query, Integer.class);
					} catch (Exception e) {
						value = null;
					}
					if (value != null) {
						final Map<String, Object> mailMap = new HashMap<String, Object>();
						mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));
						mailMap.put("nbiodisposeformcode", nBioDisposeFormCode);
						String query = "SELECT sformnumber FROM biodisposeform where nbiodisposeformcode="
								+ nBioDisposeFormCode;
						String referenceId = jdbcTemplate.queryForObject(query, String.class);
						mailMap.put("ssystemid", referenceId);
						final UserInfo mailUserInfo = new UserInfo(userInfo);
						mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
						mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
						emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
					}
		

		return getDisposalSampleApproval(inputMap, userInfo);

	}

	private String auditQuery(int nBioDisposeFormCode, String selectedChildRecord, UserInfo userInfo) {

		var strCondition="";
		if(selectedChildRecord!="") {
			strCondition=" AND dfd.nbiodisposeformdetailscode in ("+selectedChildRecord+")";
		}
		
		final var strQry = "SELECT df.nbiodisposeformcode, dfd.nbiodisposeformdetailscode, df.sformnumber,"
				+ " dfd.sparentsamplecode, dfd.ncohortno, dfd.srepositoryid, dfd.slocationcode, dfd.svolume, "
				+ "coalesce(t1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', t1.jsondata->'stransdisplaystatus'->>'en-US') "
				+ "AS stransdisplaystatus, coalesce(t2.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', "
				+ "t2.jsondata->'stransdisplaystatus'->>'en-US') AS ssamplecondition, "
				+ "coalesce(t3.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', t3.jsondata->'stransdisplaystatus'->>'en-US') "
				+ "AS ssamplestatus FROM biodisposeform df JOIN biodisposeformdetails dfd ON "
				+ "df.nbiodisposeformcode=dfd.nbiodisposeformcode LEFT JOIN transactionstatus t1 ON t1.ntranscode=df.ntransactionstatus"
				+ " AND t1.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN transactionstatus t2 ON t2.ntranscode=dfd.nsamplecondition AND t2.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN transactionstatus t3 ON t3.ntranscode=dfd.ntransdisposestatus AND t3.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "WHERE dfd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND df.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "AND dfd.nbiodisposeformcode="+nBioDisposeFormCode+" "+strCondition+" ORDER BY df.nbiodisposeformcode DESC;"
				+ "";

		return strQry;
	}
	
	private String auditQueryDelete(int nBioDisposeFormCode, String selectedChildRecord, UserInfo userInfo) {

		var strCondition="";
		if(selectedChildRecord!="") {
			strCondition=" AND dfd.nbiodisposeformdetailscode in ("+selectedChildRecord+") ";
		}
		
		final var strQry = "SELECT df.nbiodisposeformcode, dfd.nbiodisposeformdetailscode, df.sformnumber,"
				+ " dfd.sparentsamplecode, dfd.ncohortno, dfd.srepositoryid, dfd.slocationcode, dfd.svolume, "
				+ "coalesce(t1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', t1.jsondata->'stransdisplaystatus'->>'en-US') "
				+ "AS stransdisplaystatus, coalesce(t2.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', "
				+ "t2.jsondata->'stransdisplaystatus'->>'en-US') AS ssamplecondition, "
				+ "coalesce(t3.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', t3.jsondata->'stransdisplaystatus'->>'en-US') "
				+ "AS ssamplestatus FROM biodisposeform df JOIN biodisposeformdetails dfd ON "
				+ "df.nbiodisposeformcode=dfd.nbiodisposeformcode LEFT JOIN transactionstatus t1 ON t1.ntranscode=df.ntransactionstatus"
				+ " AND t1.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN transactionstatus t2 ON t2.ntranscode=dfd.nsamplecondition AND t2.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN transactionstatus t3 ON t3.ntranscode=dfd.ntransdisposestatus AND t3.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "WHERE  dfd.nbiodisposeformcode="+nBioDisposeFormCode+" "+strCondition+" ORDER BY df.nbiodisposeformcode DESC;"
				+ "";

		return strQry;
	}

	private Map<String, Object> insertBioDisposeFormDetails(final BioDisposeForm objBioDisposeFormData,
			final List<Map<String, Object>> filteredSampleReceiving, final int seqNoBioDisposeFormCode,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();

		int seqNoBioDisposeFormDetails = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnobiobankmanagement where stablename='biodisposeformdetails' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);
		
		

		
		
		String sFormNumberDipos = "";
		if (objBioDisposeFormData == null) {
			final var sFormNumberDiposeQry = jdbcTemplate.queryForObject(
					"select sformnumber from biodisposeform where nbiodisposeformcode="+seqNoBioDisposeFormCode+" and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					String.class);
			
		    sFormNumberDipos = sFormNumberDiposeQry;
		} else {
		    sFormNumberDipos = objBioDisposeFormData.getSformnumber();
		}


		final var selectedChildRecord = new StringBuilder();

		// added jsondata field by sujatha ATE_274 for storing data from biomovetodisposedetails's jsondata BGSI-218
		var strDisposeForm = """
				INSERT INTO biodisposeformdetails(
				nbiodisposeformdetailscode, nbiodisposeformcode, nbioprojectcode, nsamplestoragetransactioncode,nbiomovetodisposecode,nbiomovetodisposedetailscode, sparentsamplecode,
				ncohortno, nstoragetypecode, nproductcatcode, nproductcode, nsamplecondition, srepositoryid, slocationcode, svolume, jsondata, 
				ssubjectid, scasetype, ndiagnostictypecode, ncontainertypecode, nreasoncode,ntransdisposestatus, dtransactiondate,
				ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) VALUES """;

		ObjectMapper mapper = new ObjectMapper();

		for (final Map<String, Object> objBioSampleReceiving : filteredSampleReceiving) {
			seqNoBioDisposeFormDetails++;
			selectedChildRecord.append(seqNoBioDisposeFormDetails).append(",");
			// added by sujatha ATE_274 for formattig & storing jsondata into biodisposeformdetails BGSI-218
			Map<String, Object> jsonDataStr = (Map<String, Object>) objBioSampleReceiving.get("jsondata");
			
			if (jsonDataStr == null) {
			    jsonDataStr = new HashMap<>();   // initialize empty map
			}
			
			jsonDataStr.put(""+Enumeration.FormCode.DISPOSALSAMPLESPPROVAL.getFormCode()+"", sFormNumberDipos);
			
			final String jsonString = mapper.writeValueAsString(jsonDataStr);
			//commentted and added by sujatha ATE_274 for an issue -> flow with store & save bgsi-238
//			final var objJsonData = new JSONObject(jsonString);
			final JSONObject objJsonData =
			        (jsonString == null || jsonString.equals("null"))
	                ? new JSONObject()
	                : new JSONObject(jsonString);

			// added objJsonData by sujatha ATE_274 for jsondata field bgsi-178
			strDisposeForm += "(" + seqNoBioDisposeFormDetails + ", " + seqNoBioDisposeFormCode + ", "
					+ objBioSampleReceiving.get("nbioprojectcode") + ", "
					+ objBioSampleReceiving.get("nsamplestoragetransactioncode") + ","
					+ objBioSampleReceiving.get("nbiomovetodisposecode") + ","
					+ objBioSampleReceiving.get("nbiomovetodisposedetailscode") + ", " + "'"
					+ objBioSampleReceiving.get("sparentsamplecode") + "', " + objBioSampleReceiving.get("ncohortno")
					+ ", " + objBioSampleReceiving.get("nstoragetypecode") + ", "
					+ objBioSampleReceiving.get("nproductcatcode") + ", " + objBioSampleReceiving.get("nsampletypecode")
					+ ", " + objBioSampleReceiving.get("nsamplecondition") + ", " + "'"
					+ objBioSampleReceiving.get("spositionvalue") + "', " + "'"
					+ objBioSampleReceiving.get("slocationcode") + "', " + "'" + objBioSampleReceiving.get("sqty")
					+ "', '"+ objJsonData +"', "
					+ "'" + objBioSampleReceiving.get("ssubjectid") + "', " + "'"
					+ objBioSampleReceiving.get("scasetype") + "', " + objBioSampleReceiving.get("ndiagnostictypecode")
					+ ", " + objBioSampleReceiving.get("ncontainertypecode") + ", "
					+ objBioSampleReceiving.get("nreasoncode") + ", " + objBioSampleReceiving.get("nsamplestatus") + ""
					+ ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
					+ ", '" + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + "', "
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "),";

		}

		strDisposeForm = strDisposeForm.substring(0, strDisposeForm.length() - 1) + ";";

		final var strSeqNoUpdate = "update seqnobiobankmanagement set nsequenceno=" + seqNoBioDisposeFormDetails
				+ " where stablename='biodisposeformdetails' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		outputMap.put("InsertQuery", strDisposeForm + strSeqNoUpdate);
		outputMap.put("selectedChildRecord",
				selectedChildRecord.deleteCharAt(selectedChildRecord.length() - 1).toString());

		return outputMap;

	}

	@Override
	public ResponseEntity<Object> deleteDisposalSamplesApproval(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final var nbiodisposesamplecode = (int)inputMap.get("nbiodisposeformcode");

		final var nbiodisposeformdetailscode = (String)inputMap.get("nbiodisposeformdetailscode");

		final var findStatus = findStatusDisposeSample((int) nbiodisposesamplecode, userInfo);
				
		final var strDeleteQry = "update biodisposeformdetails set nstatus="
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
				+ " where nbiodisposeformdetailscode in (" + nbiodisposeformdetailscode + ") and nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		
		// ===== COC: START =====
//		if (nbiodisposeformdetailscode != null && !String.valueOf(nbiodisposeformdetailscode).trim().isEmpty()) {
//			String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sQuery1);
//	 
//			String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sQuery2);
//	 
//			String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sQuery3);
//
//		    int chainCustodyPk = jdbcTemplate
//		            .queryForObject("select coalesce(max(nchaincustodycode),0) from chaincustody where nsitecode="
//		                    + userInfo.getNtranssitecode(), Integer.class);
//
//		    String remarksPrefix = commonFunction.getMultilingualMessage("IDS_REPOSITORYID",
//		            userInfo.getSlanguagefilename()) + " ['||COALESCE(bdd.srepositoryid,'')||'] '||'"
//		            + commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//		            + " ['||COALESCE(bdd.sparentsamplecode,'')||'] '||'"
//		            + commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename()) + " '";
//
//		    String strChainCustody = "insert into chaincustody(nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,sitemno,"
//		            + "ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,noffsetdtransactiondate,"
//		            + "sremarks,nsitecode,nstatus) "
//		            + " select (" + chainCustodyPk
//		            + " + rank() over(order by bdd.nbiodisposeformdetailscode)), " + userInfo.getNformcode()
//		            + ",bdd.nbiodisposeformdetailscode,'nbiodisposeformdetailscode','biodisposeformdetails',COALESCE(bd.sformnumber,''),"
//		            + Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "," + userInfo.getNusercode() + ","
//		            + userInfo.getNuserrole() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
//		            + userInfo.getNtimezonecode() + ","
//		            + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ",'"
//		            + remarksPrefix
//		            + "||COALESCE(bd.sformnumber,'')," + userInfo.getNtranssitecode() + ","
//		            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//		            + " from biodisposeformdetails bdd"
//		            + " join biodisposeform bd on bd.nbiodisposeformcode=bdd.nbiodisposeformcode"
//		            + " and bd.nsitecode=" + userInfo.getNtranssitecode() + " and bd.nstatus="
//		            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//		            + " where bdd.nsitecode=" + userInfo.getNtranssitecode()
//		            + " and bdd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//		            + " and bdd.nbiodisposeformdetailscode in(" + nbiodisposeformdetailscode + ");";
//
//		    String strSeqUpdateCOC = " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
//		            + " + count(nbiodisposeformdetailscode) from biodisposeformdetails where nbiodisposeformdetailscode in ("
//		            + nbiodisposeformdetailscode + ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
//		            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") where stablename='chaincustody' and nstatus="
//		            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//		    jdbcTemplate.execute(strChainCustody);
//		    jdbcTemplate.execute(strSeqUpdateCOC);
//		}
		// ===== COC: END =====


		jdbcTemplate.execute(strDeleteQry);

		
		final List<Object> lstAuditAfter = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final var strAuditQry = auditQueryDelete(nbiodisposesamplecode, nbiodisposeformdetailscode, userInfo);
		 final List<BioDisposeForm> lstAuditDisposeFormDetailsAfter =
		 jdbcTemplate.query(strAuditQry,new BioDisposeForm());
		 lstAuditAfter.addAll(lstAuditDisposeFormDetailsAfter);
		 lstAuditDisposeFormDetailsAfter.stream().forEach(x ->
		 multilingualIDList.add("IDS_DELETEREQUESTSAMPLES"));
		auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);


		return getActiveDisposalSampleApproval(inputMap, userInfo);

	}

	private int findStatusDisposeSample(final int nbioDisposeSampleCode, final UserInfo userInfo) throws Exception {

		final var strStatusRequestbasedTransfer = "select ntransactionstatus from biodisposeform where nbiodisposeformcode="
				+ nbioDisposeSampleCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (int) Objects.requireNonNullElse(
				jdbcTemplateUtilityFunction.queryForObject(strStatusRequestbasedTransfer, Integer.class, jdbcTemplate),
				-1);
	}

	@Override
	public int findStatusDisposeSample(final Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		final var nbioDisposeSampleCode = inputMap.get("nbiodisposeformcode");

		final var strStatusRequestbasedTransfer = "select ntransactionstatus from biodisposeform where nbiodisposeformcode="
				+ nbioDisposeSampleCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (int) Objects.requireNonNullElse(
				jdbcTemplateUtilityFunction.queryForObject(strStatusRequestbasedTransfer, Integer.class, jdbcTemplate),
				-1);
	}

	@Override
	public ResponseEntity<Object> createChildDisposalSample(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		Map<String, Object> rtnQry = new HashMap<>();

		final var nbioDisposeSampleCode = (int) inputMap.get("nbiodisposeformcode");

		final BioDisposeForm objDisposeSample = null;

		rtnQry = insertBioDisposeFormDetails(objDisposeSample,
				(List<Map<String, Object>>) inputMap.get("filterSelectedSamples"), nbioDisposeSampleCode, userInfo);

		final var childRecord = (String) rtnQry.get("InsertQuery");

		final var strUpdateDisposeSamplerDetails = "update biodisposeform set ntransactionstatus="
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " where nbiodisposeformcode="
				+ nbioDisposeSampleCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		jdbcTemplate.execute(childRecord + strUpdateDisposeSamplerDetails);
		
		// ===== COC: START =====
//		final String selectedChildRecord1 = (String) rtnQry.get("selectedChildRecord");
//		if (selectedChildRecord1 != null && !selectedChildRecord1.trim().isEmpty()) {
//			
//			String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sQuery1);
//	 
//			String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sQuery2);
//	 
//			String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sQuery3);
//
//		    int chainCustodyPk = jdbcTemplate.queryForObject(
//		            "select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//		                    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//		            Integer.class);
//
//		    String remarksPrefix = commonFunction.getMultilingualMessage("IDS_REPOSITORYID",
//		            userInfo.getSlanguagefilename()) + " '||' ['||bdd.srepositoryid||'] '||'"
//		            + commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//		            + " ['||bdd.sparentsamplecode||'] '||'"
//		            + commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//		            + " ['||bd.sformnumber||']";
//
//		    String strChainCustody = "insert into chaincustody(nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,sitemno,"
//		            + "ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,noffsetdtransactiondate,"
//		            + "sremarks,nsitecode,nstatus) "
//		            + " select (" + chainCustodyPk
//		            + " + rank() over(order by bdd.nbiodisposeformdetailscode)), " + userInfo.getNformcode()
//		            + ",bdd.nbiodisposeformdetailscode,'nbiodisposeformdetailscode','biodisposeformdetails',"
//		            + "COALESCE(bd.sformnumber,'')," + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
//		            + userInfo.getNusercode() + "," + userInfo.getNuserrole() + ",'"
//		            + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtimezonecode() + ","
//		            + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '" + remarksPrefix
//		            + "'," + userInfo.getNtranssitecode() + ","
//		            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//		            + " from biodisposeformdetails bdd"
//		            + " join biodisposeform bd on bd.nbiodisposeformcode=bdd.nbiodisposeformcode"
//		            + " and bd.nsitecode=" + userInfo.getNtranssitecode() + " and bd.nstatus="
//		            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//		            + " where bdd.nsitecode=" + userInfo.getNtranssitecode()
//		            + " and bdd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//		            + " and bdd.nbiodisposeformdetailscode in (" + selectedChildRecord1 + ");";
//
//		    String strSeqUpdate = " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
//		            + " + count(nbiodisposeformdetailscode) from biodisposeformdetails where nbiodisposeformdetailscode in ("
//		            + selectedChildRecord1 + ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
//		            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//		            + ") where stablename='chaincustody' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//		    jdbcTemplate.execute(strChainCustody);
//		    jdbcTemplate.execute(strSeqUpdate);
//		}
		// ===== COC: END =====



		final List<Object> lstAuditAfter = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final var selectedChildRecord = (String) rtnQry.get("selectedChildRecord");
		final var strAuditQry = auditQuery(nbioDisposeSampleCode, selectedChildRecord, userInfo);
		 final List<BioDisposeForm> lstAuditDisposeFormDetailsAfter =
		 jdbcTemplate.query(strAuditQry,new BioDisposeForm());
		 lstAuditAfter.addAll(lstAuditDisposeFormDetailsAfter);
		 lstAuditDisposeFormDetailsAfter.stream().forEach(x ->
		 multilingualIDList.add("IDS_ADDDISPOSESAMPLES"));
		auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);

		
		// Added By Mullai Balaji for Email jira ID-BGSI-147
		String Query = "select DISTINCT (ncontrolcode) from emailconfig where ncontrolcode="
				+ inputMap.get("ncontrolcode") + " " + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		Integer value = null;
		try {
			value = jdbcTemplate.queryForObject(Query, Integer.class);
		} catch (Exception e) {
			value = null;
		}
		if (value != null) {
			final Map<String, Object> mailMap = new HashMap<String, Object>();
			mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));
			mailMap.put("nbiodisposeformcode", nbioDisposeSampleCode);
			
			List<Map<String, Object>> filterSelectedSamples = 
			        (List<Map<String, Object>>) inputMap.get("filterSelectedSamples");

			List<String> repositoryIdList = new ArrayList<>();

			for (Map<String, Object> sample : filterSelectedSamples) {
			    Object values = sample.get("spositionvalue");
			    if (values != null) {
			        repositoryIdList.add(values.toString());
			    }
			}

			// Convert list to SQL IN style string
			String srepositoryId = repositoryIdList.stream()
			        .map(id -> "'" + id + "'")
			        .collect(Collectors.joining(",", "(", ")"));

			mailMap.put("srepositoryid", srepositoryId);
			
			String query = "SELECT sformnumber FROM biodisposeform where nbiodisposeformcode="
					+ nbioDisposeSampleCode;
			String referenceId = jdbcTemplate.queryForObject(query, String.class);
			mailMap.put("ssystemid", referenceId);
			final UserInfo mailUserInfo = new UserInfo(userInfo);
			mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
			mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
			emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
		}
		
		return getActiveDisposalSampleApproval(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> fetchDisposeSampleValidate(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();

		//final var nbiodisposeformcode = (int) inputMap.get("nbiodisposeformcode");

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	private ResponseEntity<Map<String, Object>> getUsersBasedOnSite(final UserInfo userInfo) {

		final Map<String, Object> outputMap = new HashMap<>();

		final var strUsersBasedOnSite = "select u.nusercode ndispatchercode, concat(u.sfirstname, ' ', u.slastname) sdispatchername from"
				+ " users u, userssite us where u.nusercode=us.nusercode and us.nsitecode="
				+ userInfo.getNtranssitecode() + " and u.nsitecode=" + userInfo.getNmastersitecode() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nusercode > 0 order by u.nusercode";
		final var lstGetUsersBasedOnSite = jdbcTemplate.queryForList(strUsersBasedOnSite);

		final List<Map<String, Object>> lstUsers = new ArrayList<>();
		lstGetUsersBasedOnSite.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("sdispatchername"));
			mapStatus.put("value", lst.get("ndispatchercode"));
			mapStatus.put("item", lst);
			lstUsers.add(mapStatus);
		});

		outputMap.put("lstUsers", lstUsers);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createValidationBioDisposeSample(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final var nbiodisposeformcode = (int) inputMap.get("nbiodisposeformcode");

		final var findStatus = findStatusDisposeSample(nbiodisposeformcode, userInfo);

		final List<Object> savedBioDirectTransferList = new ArrayList<>();

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			final var lstChildBioDisposeForm = getChildInitialGet(nbiodisposeformcode, userInfo);

			if (lstChildBioDisposeForm.size() > 0) {
				
				final var strAuditQryForm = " select brt.nbiodisposeformcode,brt.sformnumber,coalesce("
						+ "    ts.jsondata -> 'stransdisplaystatus' ->> '" + userInfo.getSlanguagetypecode() + "', "
						+ "    ts.jsondata -> 'stransdisplaystatus' ->> 'en-US'"
						+ "  ) as stransdisplaystatus from biodisposeform brt"
						+ "    join transactionstatus ts on brt.ntransactionstatus = ts.ntranscode and ts.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and brt.nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
						+ "and brt.nbiodisposeformcode=" + nbiodisposeformcode + "  order by 1 desc";

				final var lstAuditBefore = (BioDisposeForm) jdbcTemplateUtilityFunction
						.queryForObject(strAuditQryForm, BioDisposeForm.class, jdbcTemplate);

				final var strUpdateDisposeSamplerDetails = "update biodisposeform set ntransactionstatus="
						+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()
						+ " where nbiodisposeformcode=" + nbiodisposeformcode + " and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				
				var seqNoBioDisposeFormHistory = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnobiobankmanagement where stablename='biodisposeformhistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);
				seqNoBioDisposeFormHistory++;
				
				final var strInsertDIsposeFormHistory = 
					    "INSERT INTO biodisposeformhistory("
					    + "	nbiodisposeformhistorycode, nbiodisposeformcode, sformnumber, ntransfertypecode,"
					    + "	nformtypecode, ntransactionstatus, dtransactiondate, ntztransactiondate, "
					    + "	noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, "
					    + "	ndeputyuserrolecode, nsitecode, nstatus)"
					    + "	SELECT " +
					    seqNoBioDisposeFormHistory + ", " +
					    nbiodisposeformcode + ", sformnumber, ntransfertypecode,nformtypecode, " +
					    Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", '" +
					    dateUtilityFunction.getCurrentDateTime(userInfo) + "', " +
					    userInfo.getNtimezonecode() + ", " +
					    dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " +
					    userInfo.getNusercode() + ", " +
					    userInfo.getNuserrole() + ", " +
					    userInfo.getNdeputyusercode() + ", " +
					    userInfo.getNdeputyuserrole() + ", " +
					    userInfo.getNtranssitecode() + ", " +
					    Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
					    " FROM biodisposeform WHERE nbiodisposeformcode = "+nbiodisposeformcode+" ;";

				final StringBuilder strSeqNoUpdate=new StringBuilder();

				 strSeqNoUpdate.append("update seqnobiobankmanagement set nsequenceno=")
					.append(seqNoBioDisposeFormHistory).append(" where")
					.append(" stablename='biodisposeformhistory' and nstatus=")
					.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");
				

				jdbcTemplate.execute(strUpdateDisposeSamplerDetails+strInsertDIsposeFormHistory+strSeqNoUpdate);
				
				// ===== COC: START =====
//				{
//					String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//					jdbcTemplate.execute(sQuery1);
//			 
//					String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//					jdbcTemplate.execute(sQuery2);
//			 
//					String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//					jdbcTemplate.execute(sQuery3);
//		
//				    int chainCustodyPk = jdbcTemplate.queryForObject(
//				            "select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//				                    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//				            Integer.class);
//
//				    String remarksPrefix = commonFunction.getMultilingualMessage("IDS_REPOSITORYID",
//				            userInfo.getSlanguagefilename())
//				            + " ['||COALESCE(bdd.srepositoryid,'')||'] '||'"
//				            + commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE",
//				                    userInfo.getSlanguagefilename())
//				            + " ['||COALESCE(bdd.sparentsamplecode,'')||'] '||'"
//				            + commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//				            + " '";
//
//				    String strChainCustody = "insert into chaincustody(nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,sitemno,"
//				            + "ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,noffsetdtransactiondate,"
//				            + "sremarks,nsitecode,nstatus)"
//				            + " select (" + chainCustodyPk
//				            + " + rank() over(order by bdd.nbiodisposeformdetailscode)), " + userInfo.getNformcode()
//				            + ",bdd.nbiodisposeformdetailscode,'nbiodisposeformdetailscode','biodisposeformdetails',"
//				            + "COALESCE(bd.sformnumber,''),"
//				            + Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ","
//				            + userInfo.getNusercode() + "," + userInfo.getNuserrole() + ",'"
//				            + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNtimezonecode() + ","
//				            + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ",'"
//				            + remarksPrefix + "||COALESCE(bd.sformnumber,'')," + userInfo.getNtranssitecode() + ","
//				            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				            + " from biodisposeformdetails bdd"
//				            + " join biodisposeform bd on bd.nbiodisposeformcode=bdd.nbiodisposeformcode"
//				            + " and bd.nsitecode=" + userInfo.getNtranssitecode()
//				            + " and bd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				            + " where bdd.nsitecode=" + userInfo.getNtranssitecode()
//				            + " and bdd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				            + " and bdd.nbiodisposeformcode=" + nbiodisposeformcode + ";";
//
//				    String strSeqUpdate = " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
//				            + " + count(nbiodisposeformdetailscode) from biodisposeformdetails where nbiodisposeformcode="
//				            + nbiodisposeformcode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
//				            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				            + ") where stablename='chaincustody' and nstatus="
//				            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//				    jdbcTemplate.execute(strChainCustody);
//				    jdbcTemplate.execute(strSeqUpdate);
//				}
				// ===== COC: END =====


				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> defaultListAfterSave = new ArrayList<>();
				final List<Object> defaultListBeforeSave = new ArrayList<>();
				
				final var lstAuditAfter = (BioDisposeForm) jdbcTemplateUtilityFunction
						.queryForObject(strAuditQryForm, BioDisposeForm.class, jdbcTemplate);
				
				lstAuditAfter.setSformnumber("");
				defaultListBeforeSave.add(lstAuditAfter);
				defaultListAfterSave.add(lstAuditBefore);
				multilingualIDList.add("IDS_VALIDATIONSTATUS");

				auditUtilityFunction.fnInsertAuditAction(defaultListBeforeSave, 2, defaultListAfterSave, multilingualIDList,
						userInfo);

				// Added By Mullai Balaji for Email jira ID-BGSI-147
				String Query = "select DISTINCT (ncontrolcode) from emailconfig where ncontrolcode="
						+ inputMap.get("ncontrolcode") + " " + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				Integer value = null;
				try {
					value = jdbcTemplate.queryForObject(Query, Integer.class);
				} catch (Exception e) {
					value = null;
				}
				if (value != null) {
					final Map<String, Object> mailMap = new HashMap<String, Object>();
					mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));
					mailMap.put("nbiodisposeformcode", nbiodisposeformcode);
					String query = "SELECT sformnumber FROM biodisposeform where nbiodisposeformcode="
							+ nbiodisposeformcode;
					String referenceId = jdbcTemplate.queryForObject(query, String.class);
					mailMap.put("ssystemid", referenceId);
					final UserInfo mailUserInfo = new UserInfo(userInfo);
					mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
					mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
					emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
				}

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOSAMPLESAVAILABLE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			return getActiveDisposalSampleApproval(inputMap, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	private String getToBeDisposeSamples(final int nbiodisposeformcode, final UserInfo userInfo)
			throws Exception {
		final var strToBeDisposeSamples = "select string_agg(nbiodisposeformdetailcode::text, ',') from"
				+ " biodisposeformdetails where nbiodisposeformcode=" + nbiodisposeformcode
				+ " and nsitecode=" + userInfo.getNtranssitecode() + " and ntransferstatus="
				+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (String) Objects.requireNonNullElse(
				jdbcTemplateUtilityFunction.queryForObject(strToBeDisposeSamples, String.class, jdbcTemplate), "");
	}

	@Override
	public ResponseEntity<Object> getSampleConditionStatus(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();

		final var nbiodisposeformcode = (int) inputMap.get("nbiodisposeformcode");

		final var findStatus = findStatusDisposeSample(nbiodisposeformcode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			final var strSampleConditionStatus = "select ts.ntranscode nsamplecondition,"
					+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') ssamplecondition from"
					+ " approvalstatusconfig ascf, transactionstatus ts where ascf.ntranscode=ts.ntranscode"
					+ " and ascf.nstatusfunctioncode="
					+ Enumeration.ApprovalStatusFunction.VALIDATIONSTATUS.getNstatustype() + " and ascf.nformcode="
					+ userInfo.getNformcode() + " and ascf.nsitecode=" + userInfo.getNmastersitecode()
					+ " and ascf.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by ascf.nsorter";
			final var lstGetSampleCondition = jdbcTemplate.queryForList(strSampleConditionStatus);
			final List<Map<String, Object>> lstSampleCondition = new ArrayList<>();

			lstGetSampleCondition.stream().forEach(lst -> {
				final Map<String, Object> mapStatus = new HashMap<>();
				mapStatus.put("label", lst.get("ssamplecondition"));
				mapStatus.put("value", lst.get("nsamplecondition"));
				mapStatus.put("item", lst);
				lstSampleCondition.add(mapStatus);
			});

			outputMap.put("lstSampleCondition", lstSampleCondition);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTSAMPLES", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getStorageFreezerData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();

		final var nbiodisposeformcode = (int) inputMap.get("nbiodisposeformcode");

		final var nbiodisposeformdetailscode = (String) inputMap.get("nbiodisposeformdetailscode");

		final var findStatus = findStatusDisposeSample(nbiodisposeformcode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {

			final boolean isCheck = getAccpectAndStoredSample(
					Enumeration.TransactionStatus.STORED.gettransactionstatus(), nbiodisposeformcode,
					nbiodisposeformdetailscode);

			if (isCheck == false) {

				var strSrepostoryId = "SELECT spositionvalue FROM samplestoragetransaction WHERE spositionvalue "
						+ "IN (SELECT srepositoryid FROM biodisposeformdetails WHERE nbiodisposeformdetailscode IN ("
						+ nbiodisposeformdetailscode + ") AND nsitecode="+userInfo.getNtranssitecode()+" AND nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+") " + "AND nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND nsitecode="+userInfo.getNtranssitecode()+" ;";

				final List<String> repositoryIds = jdbcTemplate.queryForList(strSrepostoryId, String.class);

				var hasValidValues = repositoryIds != null
						&& repositoryIds.stream().anyMatch(id -> id != null && !id.trim().isEmpty());

				if (!hasValidValues) {

					final var sQueryFreezerId = "select ssv.jsondata, si.nsamplestoragelocationcode,si.nsamplestorageversioncode,si.nstorageinstrumentcode, "
							// + "i.sinstrumentid,"
							+ "(i.sinstrumentid ||' (' ||(SUM(ssm.nnoofcontainer) - (SELECT Count(st.sposition) positions "
							+ "FROM   samplestoragetransaction st,samplestoragemapping ssm1 " + "WHERE "
							+ "ssl.nsamplestoragelocationcode = st.nsamplestoragelocationcode "
							+ "AND st.nsamplestoragemappingcode = ssm1.nsamplestoragemappingcode "
							+ "AND ssm1.nstorageinstrumentcode = ssm.nstorageinstrumentcode " + "AND st.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "AND st.spositionvalue :: text <> '' :: text)) || ')') sinstrumentid,"
							+ " i.ninstrumentcode, " + "sum(ssm.nnoofcontainer) - "
							+ "(SELECT Count(st.sposition) positions "
							+ "FROM   samplestoragetransaction st,samplestoragemapping ssm1 " + "WHERE "
							+ "ssl.nsamplestoragelocationcode = st.nsamplestoragelocationcode "
							+ "AND st.nsamplestoragemappingcode = ssm1.nsamplestoragemappingcode "
							+ "AND ssm1.nstorageinstrumentcode = ssm.nstorageinstrumentcode " + "AND st.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "AND st.spositionvalue :: text <> '' :: text) navailablespace " + "from "
							+ "storageinstrument si "
							+ "JOIN samplestoragelocation ssl ON ssl.nsamplestoragelocationcode = si.nsamplestoragelocationcode  "
							+ "and si.nregionalsitecode = " + userInfo.getNtranssitecode() + " " + "and si.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ssl.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "JOIN samplestorageversion ssv ON ssl.nsamplestoragelocationcode = ssv.nsamplestoragelocationcode "
							+ "and ssv.nsamplestorageversioncode = si.nsamplestorageversioncode and ssv.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " and ssv.napprovalstatus = "
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
							+ "JOIN samplestoragecontainerpath ssc ON ssc.nsamplestoragelocationcode = ssv.nsamplestoragelocationcode "
							+ "and ssc.nsamplestorageversioncode = ssv.nsamplestorageversioncode and ssc.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "JOIN samplestoragemapping ssm ON ssm.nsamplestoragecontainerpathcode = ssc.nsamplestoragecontainerpathcode "
							+ "and ssv.nsamplestorageversioncode = si.nsamplestorageversioncode and ssm.nstorageinstrumentcode = si.nstorageinstrumentcode "
							+ "and ssm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "JOIN instrument i ON si.ninstrumentcode = i.ninstrumentcode and i.ninstrumentstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " and i.ninstrumentstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " join instrumentcalibration ic "
							+ " on ic.ninstrumentcode=i.ninstrumentcode and ic.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ic.ncalibrationstatus="
							+ Enumeration.TransactionStatus.CALIBIRATION.gettransactionstatus() + " and ic.nsitecode="
							+ userInfo.getNtranssitecode() + " and ninstrumentcalibrationcode in (select"
							+ " max(ninstrumentcalibrationcode) from instrumentcalibration where nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " group by ninstrumentcode) join instrumentmaintenance im on im.ninstrumentcode=i.ninstrumentcode"
							+ " and im.nsitecode=" + userInfo.getNtranssitecode() + " and  im.nmaintenancestatus="
							+ Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus() + " and im.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and im.ninstrumentmaintenancecode in"
							+ " (select max(ninstrumentmaintenancecode) from instrumentmaintenance where nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " group by ninstrumentcode) "
							+ "group by ssv.jsondata, si.nsamplestoragelocationcode,si.nsamplestorageversioncode,si.nstorageinstrumentcode, "
							+ "i.sinstrumentid, i.ninstrumentcode,ssl.nsamplestoragelocationcode, ssm.nstorageinstrumentcode; ";

					final List<StorageInstrument> lstfreezerList = jdbcTemplate.query(sQueryFreezerId,
							new StorageInstrument());

					final List<Map<String, Object>> freezerList = new ArrayList<>();

					lstfreezerList.stream().forEach(lst -> {
						final Map<String, Object> mapStatus = new HashMap<>();
						mapStatus.put("label", lst.getSinstrumentid());
						mapStatus.put("value", lst.getNstorageinstrumentcode());
						mapStatus.put("item", lst);
						freezerList.add(mapStatus);
					});

					outputMap.put("freezerList", freezerList);

					return new ResponseEntity<>(outputMap, HttpStatus.OK);

				} else {

					var sRepositoryidAlreadyExist = repositoryIds.stream().map(id -> "'" + id + "'")
							.collect(Collectors.joining(","));
										
					final var strNonStoredRepoIds = (String) jdbcTemplateUtilityFunction.queryForObject("select string_agg(quote_literal(srepositoryid), ',') from"
							+ " biodisposeformdetails where srepositoryid in (" + sRepositoryidAlreadyExist 
							+ ") and ntransdisposestatus <> "+ Enumeration.TransactionStatus.STORED.gettransactionstatus() 
							+ " and nsitecode="+ userInfo.getNtranssitecode() +" and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +";", String.class, jdbcTemplate);
					
					if(strNonStoredRepoIds == null) {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYSTORED", userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
					
					sRepositoryidAlreadyExist = strNonStoredRepoIds;

					final var strUpdateQry = "update biodisposeformdetails set ntransdisposestatus="
							+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + " where srepositoryid in ("
							+ sRepositoryidAlreadyExist + ") and nsitecode=" + userInfo.getNtranssitecode()
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					jdbcTemplate.execute(strUpdateQry);

					 // ---- COC start ----New
					
					String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery1);

					String sQuery2 = " lock  table lockquarantine "
							+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery2);

					String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery3);

					int chainCustodyPk = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);

					String remarksPrefix = commonFunction.getMultilingualMessage("IDS_REPOSITORYID",
							userInfo.getSlanguagefilename())
							+ " ['||COALESCE(bdd.srepositoryid,'')||'] '||'"
							+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE",
									userInfo.getSlanguagefilename())
							+ " ['||COALESCE(bdd.sparentsamplecode,'')||'] '||'"
							+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
							+ " ['||COALESCE(bd.sformnumber,'')||'] '||'"
							+ commonFunction.getMultilingualMessage("IDS_SITENAME", userInfo.getSlanguagefilename())
							+ " ['||COALESCE(si.ssitename,'')||'] '||'" + commonFunction
									.getMultilingualMessage("IDS_SAMPLELOCATION", userInfo.getSlanguagefilename())
							+ " ['||COALESCE(bdd.slocationcode,'')||']'";

					String strChainCustody = "INSERT INTO chaincustody("
							+ "nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno, "
							+ "ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, "
							+ "sremarks, nsitecode, nstatus) " + "SELECT (" + chainCustodyPk
							+ " + RANK() OVER (ORDER BY bdd.nbiodisposeformdetailscode)), " + userInfo.getNformcode()
							+ ", " + "bdd.nbiodisposeformdetailscode, " + "'nbiodisposeformdetailscode', "
							+ "'biodisposeformdetails', " + "COALESCE(bdd.srepositoryid, ''), "
							+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ", "
							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + "'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ "'" + remarksPrefix + " , " + userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "FROM biodisposeformdetails bdd "
							+ "JOIN biodisposeform bd ON bd.nbiodisposeformcode = bdd.nbiodisposeformcode "
							+ "AND bd.nsitecode = " + userInfo.getNtranssitecode() + " " + "AND bd.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "LEFT JOIN site si ON si.nsitecode = bdd.nsitecode " + "AND si.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "WHERE bdd.nsitecode = " + userInfo.getNtranssitecode() + " " + "AND bdd.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "AND bdd.srepositoryid IN (" + sRepositoryidAlreadyExist + ");";

					String strSeqUpdate = " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
							+ " + count(nbiodisposeformdetailscode) from biodisposeformdetails where srepositoryid in ("
							+ sRepositoryidAlreadyExist + ") and nsitecode=" + userInfo.getNtranssitecode()
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ ") where stablename='chaincustody' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					jdbcTemplate.execute(strChainCustody);
					jdbcTemplate.execute(strSeqUpdate);

					 // ---- COC end ----

					
					return getActiveDisposalSampleApproval(inputMap, userInfo);

				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_REJECTEDSAMPLENOTALLOWEDTOSTORE", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTAPPROVERECORD",
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> getStorageStructure(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final int nsamplestorageversioncode = Integer.valueOf(inputMap.get("nsamplestorageversioncode").toString());
		final int nstorageinstrumentcode = Integer.valueOf(inputMap.get("nstorageinstrumentcode").toString());

		final var sQuery = "SELECT enrich_jsondata_by_version(" + nsamplestorageversioncode + ","
				+ nstorageinstrumentcode + ") AS jsondata; ";

		final var objSelectedStrcuture = (StorageInstrument) jdbcTemplateUtilityFunction.queryForObject(sQuery,
				StorageInstrument.class, jdbcTemplate);

		outputMap.put("selectedSampleStorageVersion", objSelectedStrcuture);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createStoreSamples(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		
		final var sQuerys = " lock  table locksamplestoragetransaction "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuerys);

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final var objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());

		final var nbiodisposeformcode = (int) inputMap.get("nbiodisposeformcode");
		final var nbiodisposeformdetailscode = (String) inputMap.get("nbiodisposeformdetailscode");

		final var findStatus = findStatusDisposeSample(nbiodisposeformcode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {

			final var strStatus = "select STRING_AGG(nbiodisposeformdetailscode::TEXT, ',') AS ndisposedeatailstatuscode from biodisposeformdetails  "
					+ "where nbiodisposeformdetailscode in (" + nbiodisposeformdetailscode
					+ ") and ntransdisposestatus=" + Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

			final var ndisposedeatailstatuscode = (String) jdbcTemplateUtilityFunction.queryForObject(strStatus,
					String.class, jdbcTemplate);

			final int nstorageinstrumentcode = Integer.valueOf(inputMap.get("nstorageinstrumentcode").toString());

			final var storageLocQry = "select nsamplestoragelocationcode, nsamplestorageversioncode from storageinstrument "
					+ "where nstorageinstrumentcode = " + nstorageinstrumentcode + " " + "and nsitecode = "
					+ userInfo.getNmastersitecode() + " " + "and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final var objSelectedStructure = (StorageInstrument) jdbcTemplateUtilityFunction
					.queryForObject(storageLocQry, StorageInstrument.class, jdbcTemplate);

			final var nsamplestoragelocationcode = objSelectedStructure.getNsamplestoragelocationcode();
			final var nsamplestorageversioncode = objSelectedStructure.getNsamplestorageversioncode();

			// modified by sujatha ATE_274 BGSI-218 by selecting 4 jsondata key used to insert into samplestorageaddtionalinfo table while sample store
			final var sampleRecQry = "SELECT bdd.nbiodisposeformdetailscode, bdd.nbiomovetodisposedetailscode, bdd.srepositoryid, bdd.svolume, "
					+ "bdd.svolume, bdd.nproductcode, p.sproductname, pc.sproductcatname, bdd.nbioprojectcode, bp.sprojecttitle, bdd.sparentsamplecode, "
					+ "bdd.ncohortno, bdd.nproductcatcode, bdd.slocationcode, bdd.ssubjectid, bdd.scasetype, bdd.ndiagnostictypecode, "
					+ "bdd.ncontainertypecode, bdd.nstoragetypecode, "
					+" NULLIF(bdd.jsondata->>'sextractedsampleid', '') AS sextractedsampleid, " 
					+" NULLIF(bdd.jsondata->>'sconcentration', '') AS sconcentration, " 
					+" NULLIF(bdd.jsondata->>'sqcplatform', '') AS sqcplatform, " 
					+" NULLIF(bdd.jsondata->>'seluent', '') AS seluent "
					+ "FROM biodisposeformdetails bdd "
					+ "JOIN bioproject bp ON bp.nbioprojectcode = bdd.nbioprojectcode AND bp.nsitecode = "
					+ userInfo.getNmastersitecode() + " AND bp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN product p ON p.nproductcode = bdd.nproductcode AND p.nsitecode = "
					+ userInfo.getNmastersitecode() + " AND p.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN productcategory pc ON pc.nproductcatcode = p.nproductcatcode AND pc.nsitecode = "
					+ userInfo.getNmastersitecode() + " AND pc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " WHERE bdd.nsitecode = "
					+ userInfo.getNtranssitecode() + " AND bdd.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " AND bdd.nbiodisposeformdetailscode IN (" + ndisposedeatailstatuscode + ") "
					+ "ORDER BY bdd.nbiodisposeformdetailscode";

			final List<BioDisposeFormDetails> lstBioDisposeSample = jdbcTemplate.query(sampleRecQry,
					new BioDisposeFormDetails());

			if (!lstBioDisposeSample.isEmpty()) {
				final var selectedCount = lstBioDisposeSample.size();
				final var selectedNodeID = inputMap.get("selectedNodeID").toString();

				final var sQuery = "SELECT enrich_jsondata_by_version(" + nsamplestorageversioncode + ","
						+ nstorageinstrumentcode + ") AS jsondata; ";

				final var objSelectedStrcuture = (StorageInstrument) jdbcTemplateUtilityFunction.queryForObject(sQuery,
						StorageInstrument.class, jdbcTemplate);
				final var objJsonData = objSelectedStrcuture.getJsondata();
				final var dataListObj = objJsonData.get("data");

				final var nodes = (List<Map<String, Object>>) dataListObj;
				final var lastNodes = ProcessedSampleReceivingDAOImpl.findContainerLastNodes(nodes, selectedNodeID);

				int nseqSampleStorageTransaction = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnobasemaster where stablename='samplestoragetransaction' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);

				var strSampleStorageTransactionInsert = "";
				var strUpdateQryStatus = "";
				var updateSeqNoQry = "";
				final var strUpdateBioBGSIReturnDetails = "";
				String instsamplestorageadditionalinfo="";

				final var totalCodes = selectedCount;

				var codeIndex = 0;
				for (final Map<String, String> node : lastNodes) {

					var getQry = "select nsamplestoragecontainerpathcode, scontainerlastnode from samplestoragecontainerpath "
							+ "where  suid='"+node.get("id")+"' and nsamplestoragelocationcode = " + nsamplestoragelocationcode + " "
							+ "and nsamplestorageversioncode = " + nsamplestorageversioncode + " " + "and nsitecode = "
							+ userInfo.getNmastersitecode() + " " + "and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final var containerresult = jdbcTemplate.queryForMap(getQry);

					final int nsamplestoragecontainerpathcode = Integer
							.valueOf(containerresult.get("nsamplestoragecontainerpathcode").toString());
					final var scontainerlastnode = containerresult.get("scontainerlastnode").toString();

					getQry = "select nsamplestoragemappingcode, nrow, ncolumn, ndirectionmastercode, ninstrumentcode"
							+ " from samplestoragemapping where nsamplestoragecontainerpathcode= "
							+ nsamplestoragecontainerpathcode + " and nstorageinstrumentcode = "
							+ nstorageinstrumentcode + " and nsitecode = " + userInfo.getNtranssitecode()
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final var mapresult = jdbcTemplate.queryForMap(getQry);

					final int nsamplestoragemappingcode = Integer
							.valueOf(mapresult.get("nsamplestoragemappingcode").toString());

					final int nrow = Integer.valueOf(mapresult.get("nrow").toString());

					final int ncolumn = Integer.valueOf(mapresult.get("ncolumn").toString());

					final int ndirectionmastercode = Integer.valueOf(mapresult.get("ndirectionmastercode").toString());

					final int ninstrumentCode = Integer.valueOf(mapresult.get("ninstrumentcode").toString());

					final var generatedOrder = BioDisposalSampleApprovalDAOImpl.generateOrder(nrow, ncolumn,
							ndirectionmastercode);

					final var placeholders = generatedOrder.stream().map(s -> "'" + s + "'")
							.collect(Collectors.joining(","));

					final var sql = "select sposition from samplestoragetransaction WHERE sposition in (" + placeholders
							+ ") and nsamplestoragemappingcode = " + nsamplestoragemappingcode
							+ " and nsamplestoragelocationcode = " + nsamplestoragelocationcode + " and nsitecode = "
							+ userInfo.getNtranssitecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final List<String> existingPositions = jdbcTemplate.queryForList(sql, String.class);

					final List<String> missingPositions = generatedOrder.stream()
							.filter(code -> !existingPositions.contains(code)).collect(Collectors.toList());
					final var availableSpace = Integer.parseInt(node.get("savailablespace"));
					for (var i = 0; i < availableSpace && codeIndex < totalCodes; i++) {
						nseqSampleStorageTransaction++;

						final var slocationCode = scontainerlastnode + "-" + missingPositions.get(i);

						final var sparentSampleCode = lstBioDisposeSample.get(codeIndex).getSparentsamplecode() != null
								&& lstBioDisposeSample.get(codeIndex).getSparentsamplecode() != ""
										? "'" + lstBioDisposeSample.get(codeIndex).getSparentsamplecode() + "'"
										: null;
						final var svolume = lstBioDisposeSample.get(codeIndex).getSvolume() != null
								&& lstBioDisposeSample.get(codeIndex).getSvolume() != ""
										? "'" + lstBioDisposeSample.get(codeIndex).getSvolume() + "'"
										: null;
						final var ssubjectId = lstBioDisposeSample.get(codeIndex).getSsubjectid() != null
								&& lstBioDisposeSample.get(codeIndex).getSsubjectid() != ""
										? "'" + lstBioDisposeSample.get(codeIndex).getSsubjectid() + "'"
										: null;
						final var scaseType = lstBioDisposeSample.get(codeIndex).getScasetype() != null
								&& lstBioDisposeSample.get(codeIndex).getScasetype() != ""
										? "'" + lstBioDisposeSample.get(codeIndex).getScasetype() + "'"
										: null;

						strSampleStorageTransactionInsert += "(" + nseqSampleStorageTransaction + ","
								+ nsamplestoragelocationcode + "," + nsamplestoragemappingcode + ","
								+ lstBioDisposeSample.get(codeIndex).getNbioprojectcode() + ",'"
								+ missingPositions.get(i) + "' ,'"
								+ stringUtilityFunction
										.replaceQuote(lstBioDisposeSample.get(codeIndex).getSrepositoryid())
								+ "' ," + " json_build_object('Parent Sample Code','"
								+ stringUtilityFunction
										.replaceQuote(lstBioDisposeSample.get(codeIndex).getSparentsamplecode())
								+ "'," + "'Case Type','"
								+ stringUtilityFunction.replaceQuote(lstBioDisposeSample.get(codeIndex).getScasetype())
								+ "'" + ", 'Parent Sample Type','"
								+ stringUtilityFunction
										.replaceQuote(lstBioDisposeSample.get(codeIndex).getSproductcatname())
								+ "', 'Project Title','"
								+ stringUtilityFunction
										.replaceQuote(lstBioDisposeSample.get(codeIndex).getSprojecttitle())
								+ "', 'Bio Sample Type','"
								+ stringUtilityFunction.replaceQuote(
										lstBioDisposeSample.get(codeIndex).getSproductname())
								+ "', 'Volume (μL)','"
								+ stringUtilityFunction.replaceQuote(lstBioDisposeSample.get(codeIndex).getSvolume())
								+ "')::jsonb, " + Enumeration.TransactionStatus.YES.gettransactionstatus() + ", '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ userInfo.getNtranssitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
								+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " " + ", " + ninstrumentCode
								+ ", " + Enumeration.TransactionStatus.NA.gettransactionstatus() + ", "
								+ sparentSampleCode + ", " + lstBioDisposeSample.get(codeIndex).getNcohortno() + ", "
								+ lstBioDisposeSample.get(codeIndex).getNproductcatcode() + ", "
								+ lstBioDisposeSample.get(codeIndex).getNproductcode() + ", " + svolume + ", '"
								+ slocationCode + "', " + ssubjectId + ", " + scaseType + ", "
								+ lstBioDisposeSample.get(codeIndex).getNdiagnostictypecode() + ", "
								+ lstBioDisposeSample.get(codeIndex).getNcontainertypecode() + ", "
								+ lstBioDisposeSample.get(codeIndex).getNstoragetypecode() + "),";

						strUpdateQryStatus += "update biodisposeformdetails set nsamplestoragetransactioncode="
								+ nseqSampleStorageTransaction + ",slocationcode ='" + slocationCode
								+ "' , ntransdisposestatus="
								+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + " "
								+ "where nbiodisposeformdetailscode="
								+ lstBioDisposeSample.get(codeIndex).getNbiodisposeformdetailscode()
								+ "  and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						// added by sujatha ATE_274 for insert into samplestorageadditionalinfo table while storing samples BGSI-218
						instsamplestorageadditionalinfo+="(" + nseqSampleStorageTransaction + ", "
							    + (lstBioDisposeSample.get(codeIndex).getSextractedsampleid() == null || lstBioDisposeSample.get(codeIndex).getSextractedsampleid().trim().isEmpty()
							    ? "null" : "'" + stringUtilityFunction.replaceQuote(lstBioDisposeSample.get(codeIndex).getSextractedsampleid()) + "'")
							    + ", "+ (lstBioDisposeSample.get(codeIndex).getSconcentration() == null || lstBioDisposeSample.get(codeIndex).getSconcentration().trim().isEmpty()
							    ? "null" : "'" + stringUtilityFunction.replaceQuote(lstBioDisposeSample.get(codeIndex).getSconcentration()) + "'")
							    + ", "+ (lstBioDisposeSample.get(codeIndex).getSqcplatform() == null || lstBioDisposeSample.get(codeIndex).getSqcplatform().trim().isEmpty()
							    ? "null" : "'" + stringUtilityFunction.replaceQuote(lstBioDisposeSample.get(codeIndex).getSqcplatform()) + "'")
							    + ", "+ (lstBioDisposeSample.get(codeIndex).getSeluent() == null || lstBioDisposeSample.get(codeIndex).getSeluent().trim().isEmpty()
							    ? "null" : "'" + stringUtilityFunction.replaceQuote(lstBioDisposeSample.get(codeIndex).getSeluent()) + "'")
							    + ", '"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							    + userInfo.getNtranssitecode() + ", "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "),";

						codeIndex++;
					}
					if (codeIndex >= totalCodes) {
						break;
					}
				}

				strSampleStorageTransactionInsert = " INSERT INTO samplestoragetransaction("
						+ " nsamplestoragetransactioncode, nsamplestoragelocationcode, nsamplestoragemappingcode,"
						+ " nprojecttypecode, sposition, spositionvalue, jsondata, npositionfilled, dmodifieddate,"
						+ " nsitecode, nstatus, nbiosamplereceivingcode, ninstrumentcode, nbioparentsamplecode, sparentsamplecode, "
						+ " ncohortno, nproductcatcode, nproductcode, sqty, slocationcode, ssubjectid, scasetype, ndiagnostictypecode, "
						+ " ncontainertypecode, nstoragetypecode) VALUES "
						+ strSampleStorageTransactionInsert.substring(0, strSampleStorageTransactionInsert.length() - 1)
						+ "; ";

				// added by sujatha ATE_274 BGSI-218 for inserting into samplestorageadditionalinfo while storing the samples
				instsamplestorageadditionalinfo=" INSERT INTO public.samplestorageadditionalinfo("
						+ " nsamplestoragetransactioncode, sextractedsampleid, sconcentration, sqcplatform, seluent, dmodifieddate,"
						+ " nsitecode, nstatus)"
						+ " VALUES"+instsamplestorageadditionalinfo.substring(0, instsamplestorageadditionalinfo.length() - 1) + ";";

				updateSeqNoQry = updateSeqNoQry + " update seqnobasemaster set nsequenceno = "
						+ nseqSampleStorageTransaction + " where stablename = 'samplestoragetransaction' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

				// modified by sujatha by adding instsamplestorageadditionalinfo to execute the query bgsi-218
				jdbcTemplate.execute(strSampleStorageTransactionInsert + instsamplestorageadditionalinfo +updateSeqNoQry + strUpdateBioBGSIReturnDetails
						+ strUpdateQryStatus);
				
				// ===== COC: START =====
				if (ndisposedeatailstatuscode != null && !ndisposedeatailstatuscode.trim().isEmpty()) {

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

				    String lblRepositoryId = stringUtilityFunction.replaceQuote(
				            commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename()));
				    String lblParentSampleCode = stringUtilityFunction.replaceQuote(
				            commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename()));
				    String lblFormNumber = stringUtilityFunction.replaceQuote(
				            commonFunction.getMultilingualMessage("IDS_FORMNUMBER", userInfo.getSlanguagefilename()));
				    String lblCohortNo = stringUtilityFunction.replaceQuote(
				            commonFunction.getMultilingualMessage("IDS_COHORTNO", userInfo.getSlanguagefilename()));
				    String lblProduct = stringUtilityFunction.replaceQuote(
				            commonFunction.getMultilingualMessage("IDS_PRODUCT", userInfo.getSlanguagefilename()));
				    String lblBioSampleType = stringUtilityFunction.replaceQuote(
				            commonFunction.getMultilingualMessage("IDS_BIOSAMPLETYPE", userInfo.getSlanguagefilename()));
				    String lblSiteName = stringUtilityFunction.replaceQuote(
				            commonFunction.getMultilingualMessage("IDS_SITENAME", userInfo.getSlanguagefilename()));

				    String strChainCustody =
				            "insert into chaincustody("
				            + "nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno, "
				            + "ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate, "
				            + "noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
				            + "select " + chainCustodyPk + " + rank() over(order by sst.nsamplestoragetransactioncode), "
				            + userInfo.getNformcode() + ", "
				            + "sst.nsamplestoragetransactioncode, 'nsamplestoragetransactioncode', "
				            + "'samplestoragetransaction', bdd.srepositoryid, "
				            + Enumeration.TransactionStatus.STORED.gettransactionstatus() + ", "
				            + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
				            + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
				            + userInfo.getNtimezonecode() + ", "
				            + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				            + " '"
				            + lblRepositoryId + " [' || COALESCE(bdd.srepositoryid,'') || '] , "
				            + lblParentSampleCode + " [' || COALESCE(bdd.sparentsamplecode,'') || '] , "
				            + lblFormNumber + " [' || COALESCE(bd.sformnumber,'') || '] , "
				            + lblCohortNo + " [' || COALESCE(bdd.ncohortno::text,'') || '] , "
				            + lblProduct + " [' || COALESCE(pc.sproductcatname,'') || '] , "
				            + lblBioSampleType + " [' || COALESCE(p.sproductname,'') || '] , "
				            + lblSiteName + " [' || COALESCE(si.ssitename,'') || ']' "
				            + ", "
				            + userInfo.getNtranssitecode() + ", "
				            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				            + " from samplestoragetransaction sst "
				            + " join biodisposeformdetails bdd on bdd.nsamplestoragetransactioncode = sst.nsamplestoragetransactioncode "
				            + " and bdd.nsitecode = " + userInfo.getNtranssitecode()
				            + " and bdd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				            + " join biodisposeform bd on bd.nbiodisposeformcode = bdd.nbiodisposeformcode "
				            + " and bd.nsitecode = " + userInfo.getNtranssitecode()
				            + " and bd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				            + " join product p on p.nproductcode = bdd.nproductcode "
				            + " and p.nsitecode = " + userInfo.getNmastersitecode()
				            + " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				            + " left join productcategory pc on pc.nproductcatcode = bdd.nproductcatcode "
				            + " and pc.nsitecode = " + userInfo.getNmastersitecode()
				            + " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				            + " left join site si on si.nsitecode = sst.nsitecode "
				            + " and si.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				            + " where sst.nsitecode = " + userInfo.getNtranssitecode()
				            + " and sst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				            + " and bdd.nbiodisposeformdetailscode in (" + ndisposedeatailstatuscode + ");";

				    String strSeqUpdate =
				            "update seqnoregistration set nsequenceno = (select "
				            + chainCustodyPk + " + count(sst.nsamplestoragetransactioncode) "
				            + " from samplestoragetransaction sst "
				            + " join biodisposeformdetails bdd on bdd.nsamplestoragetransactioncode = sst.nsamplestoragetransactioncode "
				            + " and bdd.nsitecode = " + userInfo.getNtranssitecode()
				            + " and bdd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				            + " where sst.nsitecode = " + userInfo.getNtranssitecode()
				            + " and sst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				            + " and bdd.nbiodisposeformdetailscode in (" + ndisposedeatailstatuscode + ")) "
				            + " where stablename = 'chaincustody' and nstatus = "
				            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				    jdbcTemplate.execute(strChainCustody);
				    jdbcTemplate.execute(strSeqUpdate);
				}
				// ===== COC: END =====


				return getActiveDisposalSampleApproval(inputMap, userInfo);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTACCEPTEDVALIDATEDRECORDTOSTORE",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTACCEPTEDVALIDATEDRECORDTOSTORE",
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	private static final String[] ALPHABET = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
			"O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

	public static List<String> generateOrder(final int rows, final int columns, final int direction) {
		final List<String> orderArray = new ArrayList<>();

		if (direction == Enumeration.DireactionMaster.LEFTTORIGHT.getDireactionMaster()) { // A1, A2, A3, ..., B1, B2...
			for (var row = 0; row < rows; row++) {
				for (var col = 0; col < columns; col++) {
					orderArray.add(BioDisposalSampleApprovalDAOImpl.ALPHABET[row] + (col + 1));
				}
			}
		} else if (direction == Enumeration.DireactionMaster.UPTODOWN.getDireactionMaster()) { // A1, B1, C1...
			for (var col = 0; col < columns; col++) {
				for (var row = 0; row < rows; row++) {
					orderArray.add(BioDisposalSampleApprovalDAOImpl.ALPHABET[row] + (col + 1));
				}
			}
		}

		return orderArray;
	}

}
