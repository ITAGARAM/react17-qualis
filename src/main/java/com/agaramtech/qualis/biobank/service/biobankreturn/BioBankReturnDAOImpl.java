package com.agaramtech.qualis.biobank.service.biobankreturn;

import java.util.ArrayList;
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

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.biobank.model.BioBankReturn;
import com.agaramtech.qualis.biobank.model.BioBankReturnDetails;
import com.agaramtech.qualis.biobank.model.BioFormAcceptance;
import com.agaramtech.qualis.biobank.model.BioFormAcceptanceDetails;
import com.agaramtech.qualis.biobank.model.BioThirdPartyReturn;
import com.agaramtech.qualis.biobank.model.FormType;
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
public class BioBankReturnDAOImpl implements BioBankReturnDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(BioBankReturnDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final JdbcTemplateUtilityFunction jdbcTemplateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProcessedSampleReceivingDAOImpl processedSampleReceivingDAOImpl;
	private final EmailDAOSupport emailDAOSupport;

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getBioBankReturn(final Map<String, Object> inputMap, final int nbioBankReturnCode,
			final UserInfo userInfo) throws Exception {
		LOGGER.info("getBioDirectTransfer");
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		String fromDate = (String) inputMap.get("fromDate");
		String toDate = (String) inputMap.get("toDate");
		int ntransCode = inputMap.containsKey("ntransCode") ? (int) inputMap.get("ntransCode") : -1;
		final int saveType = inputMap.containsKey("saveType") ? Integer.valueOf(inputMap.get("saveType").toString())
				: 1;

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

		getFilterStatus.stream().forEach(lst -> {
			Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getStransdisplaystatus());
			mapStatus.put("value", lst.getNtranscode());
			mapStatus.put("item", lst);
			lstFilterStatus.add(mapStatus);
		});

		if (!inputMap.containsKey("ntransCode")) {
			ntransCode = getFilterStatus.get(0).getNtranscode();
		} else {
			ntransCode = (int) inputMap.get("ntransCode");
		}

		final short transCode = (short) ntransCode;
		Map<String, Object> selectedFilterStatus = lstFilterStatus.stream()
				.filter(x -> (short) x.get("value") == transCode).collect(Collectors.toList()).get(0);

		outputMap.put("lstFilterStatus", lstFilterStatus);
		outputMap.put("selectedFilterStatus", selectedFilterStatus);
		outputMap.put("realSelectedFilterStatus", selectedFilterStatus);
		final String strConditionTransCode = ntransCode == 0 ? "" : " and bbr.ntransactionstatus=" + ntransCode + " ";

		final String strQuery = "select bbr.nbiobankreturncode, bbr.sbankreturnformnumber, bbr.jsondata->>'soriginsitename' soriginsitename,"
				+ " to_char(bbr.dreturndate, '" + userInfo.getSsitedate() + "') sreturndate,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, bbr.ntransactionstatus,"
				+ " cm.scolorhexcode, bbr.noriginsitecode, bbr.jsondata->>'sremarks' sremarks, bbr.jsondata->>'sstorageconditionname' sstorageconditionname,"
				+ " to_char(bbr.ddeliverydate, '" + userInfo.getSsitedate()
				+ "') sdeliverydate, bbr.jsondata->>'sdispatchername' sdispatchername, bbr.jsondata->>'scouriername' scouriername,"
				+ " bbr.jsondata->>'scourierno' scourierno, bbr.jsondata->>'striplepackage' striplepackage, bbr.jsondata->>'svalidationremarks' svalidationremarks"
				+ " from biobankreturn bbr "
				+ " join transactionstatus ts on ts.ntranscode=bbr.ntransactionstatus and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join formwisestatuscolor fwsc on fwsc.ntranscode=ts.ntranscode and fwsc.nformcode= "
				+ userInfo.getNformcode() + " and fwsc.nsitecode=" + userInfo.getNmastersitecode()
				+ " and fwsc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join colormaster cm on fwsc.ncolorcode=cm.ncolorcode and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bbr.nsitecode="
				+ userInfo.getNtranssitecode() + " and bbr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbr.dtransactiondate between '"
				+ fromDate + "' and '" + toDate + "'" + strConditionTransCode + " order by bbr.nbiobankreturncode desc";

		final List<BioBankReturn> lstBioBankReturn = jdbcTemplate.query(strQuery, new BioBankReturn());

		if (!lstBioBankReturn.isEmpty()) {
			outputMap.put("lstBioBankReturn", lstBioBankReturn);
			List<BioBankReturn> lstObjBioBankReturn = null;
			if (nbioBankReturnCode == -1) {
				lstObjBioBankReturn = lstBioBankReturn;
			} else {
				lstObjBioBankReturn = lstBioBankReturn.stream()
						.filter(x -> x.getNbiobankreturncode() == nbioBankReturnCode).collect(Collectors.toList());
			}
			outputMap.put("selectedBioBankReturn", lstObjBioBankReturn.get(0));

			if (saveType == 2) {
				outputMap.putAll(
						(Map<String, Object>) getFormNumberDetails(lstObjBioBankReturn.get(0).getNoriginsitecode(),
								userInfo).getBody());
			}

			List<Map<String, Object>> lstChildBioBankReturn = getChildInitialGet(
					lstObjBioBankReturn.get(0).getNbiobankreturncode(), userInfo);
			outputMap.put("lstChildBioBankReturn", lstChildBioBankReturn);

			if (inputMap.containsKey(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())) {
				outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						inputMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
			}
		} else {
			outputMap.put("lstBioBankReturn", null);
			outputMap.put("selectedBioBankReturn", null);
			outputMap.put("lstChildBioBankReturn", null);
		}
		outputMap.put("nprimaryKeyBioBankReturn",
				inputMap.containsKey("nprimaryKeyBioBankReturn") ? inputMap.get("nprimaryKeyBioBankReturn") : -1);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public List<Map<String, Object>> getChildInitialGet(int nbioBankReturnCode, UserInfo userInfo) throws Exception {
		final String strChildGet = "select row_number() over(order by bbrd.nbiobankreturndetailscode desc) as"
				+ " nserialno, bbrd.nbiobankreturndetailscode, bbrd.nbiobankreturncode, bbrd.jsondata->>'sparentsamplecode' sparentsamplecode,"
				+ " bbrd.srepositoryid, bbrd.slocationcode, concat(p.sproductname, ' (', pc.sproductcatname, ')') sproductname, bbrd.svolume, bbrd.sreturnvolume,"
				+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') ssamplecondition,"
				+ " coalesce(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') ssamplestatus, r.sreason, bbrd.nsamplestatus, bbrd.nsamplecondition,"
				+ " concat(bbrd.jsondata->>'sparentsamplecode', ' | ', bbrd.ncohortno) sparentsamplecodecohortno, bfa.sformnumber "
				+ " from biobankreturndetails bbrd join transactionstatus ts1 on"
				+ " bbrd.nsamplecondition=ts1.ntranscode and ts1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join transactionstatus ts2 on"
				+ " bbrd.nsamplestatus=ts2.ntranscode and ts2.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join product p on p.nproductcode=bbrd.nproductcode and p.nsitecode=" + userInfo.getNmastersitecode()
				+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join productcategory pc on pc.nproductcatcode=p.nproductcatcode and pc.nsitecode="
				+ userInfo.getNmastersitecode() + " and pc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join bioformacceptance bfa on bfa.nbioformacceptancecode=bbrd.nbioformacceptancecode and "
				+ " bfa.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bfa.nsitecode="
				+ userInfo.getNtranssitecode()
				+ " left join reason r on r.nreasoncode=bbrd.nreasoncode and r.nsitecode="
				+ userInfo.getNmastersitecode() + " and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bbrd.nsitecode="
				+ userInfo.getNtranssitecode() + " and bbrd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbrd.nbiobankreturncode="
				+ nbioBankReturnCode + " order by nserialno desc";
		List<Map<String, Object>> lstChildGet = jdbcTemplate.queryForList(strChildGet);

		return lstChildGet;
	}

	public List<FormType> getFormType(UserInfo userInfo) throws Exception {

		final String strFormType = "select nformtypecode, coalesce(jsondata->'sformtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "', jsondata->'sformtypename'->>'en-US') sformtypename"
				+ " from formtype where nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nformtypecode";
		return jdbcTemplate.query(strFormType, new FormType());
	}

	public ResponseEntity<Object> getFormNumberDetails(final int noriginSiteCode, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		String concatStr = "";
		if (noriginSiteCode != -1) {
			concatStr += " and bfa.noriginsitecode=" + noriginSiteCode + " ";
		}

		final String strCountCheck = "select a.nbioformacceptancecode from (select bfad.nbioformacceptancecode, "
				+ " count(bfad.nbioformacceptancedetailscode) cnt from bioformacceptance bfa join"
				+ " bioformacceptancedetails bfad on bfad.nbioformacceptancecode=bfa.nbioformacceptancecode"
				+ " and bfad.nsitecode=" + userInfo.getNtranssitecode() + " and bfad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bfad.nsamplestatus="
				+ Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus() + " and bfad.nsamplestatus != "
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " where bfa.nsitecode="
				+ userInfo.getNtranssitecode() + " and bfa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by bfad.nbioformacceptancecode) a left join (select bbrd.nbioformacceptancecode,"
				+ " count(bbrd.nbioformacceptancedetailscode) cnt from biobankreturn bbr join biobankreturndetails bbrd on"
				+ " bbrd.nbiobankreturncode=bbr.nbiobankreturncode and bbrd.nsitecode=" + userInfo.getNtranssitecode()
				+ " and bbrd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bbrd.nsamplestatus != " + Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
				+ " where bbr.nsitecode=" + userInfo.getNtranssitecode() + " and bbr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by bbrd.nbioformacceptancecode) b on a.nbioformacceptancecode=b.nbioformacceptancecode"
				+ " where b.nbioformacceptancecode is null or a.cnt > b.cnt";
		final String strFormDetails = "select bfa.nbioformacceptancecode, bfa.nbiodirecttransfercode,"
				+ " bfa.nbiorequestbasedtransfercode, bfa.ntransfertypecode, bfa.nformtypecode, bfa.sformnumber,"
				+ " bfa.noriginsitecode, bfa.jsondata->>'soriginsitename' soriginsitename from bioformacceptance bfa "
				+ " join bioformacceptancedetails bfad on bfad.nbioformacceptancecode=bfa.nbioformacceptancecode"
				+ " and bfad.nsitecode=" + userInfo.getNtranssitecode() + " and bfad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bfad.nsamplestatus="
				+ Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus() + " where bfa.nsitecode="
				+ userInfo.getNtranssitecode() + " and bfa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bfa.nbioformacceptancecode in ("
				+ strCountCheck + ")" + concatStr
				+ " group by bfa.nbioformacceptancecode, bfa.nbiodirecttransfercode, bfa.nbiorequestbasedtransfercode,"
				+ " bfa.ntransfertypecode, bfa.nformtypecode, bfa.sformnumber, bfa.noriginsitecode, soriginsitename"
				+ " order by ntransfertypecode";
		final List<BioFormAcceptance> lstFormDetails = jdbcTemplate.query(strFormDetails, new BioFormAcceptance());

		List<Map<String, Object>> lstFormNumberDetails = new ArrayList<>();

		lstFormDetails.stream().forEach(lst -> {
			Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getSformnumber());
			mapStatus.put("value", lst.getNbioformacceptancecode());
			mapStatus.put("item", lst);
			lstFormNumberDetails.add(mapStatus);
		});

		outputMap.put("lstFormNumberDetails", lstFormNumberDetails);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getFormAcceptanceDetails(final int nbioFormAccetanceCode, UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		// modified by sujatha ATE_274 by selecting extractedSampleid, concentration,
		// qcplatform & eluent used to store into biobanreturndetails bgsi-218
		final String strFormAcceptanceDetails = "select bfad.nbioformacceptancedetailscode, bfad.nbioformacceptancecode, bfad.srepositoryid,"
				+ " bfad.slocationcode, bfad.nproductcode, concat(p.sproductname, ' (', pc.sproductcatname, ')') sproductname, bfad.nbioprojectcode, bfad.nbioparentsamplecode, "
				+ " bfad.nstoragetypecode, bfad.nbiosamplereceivingcode, bfad.nsamplecondition, bfad.nsamplestatus, "
				+ " bfad.nreasoncode, bfad.nsamplestoragetransactioncode, bfad.svolume, bfad.jsondata->>'sparentsamplecode' sparentsamplecode, bfad.ncohortno,"
				+ " bfad.jsondata->>'ssubjectid' ssubjectid, bfad.jsondata->>'scasetype' scasetype, bfad.ndiagnostictypecode, bfad.ncontainertypecode, bfad.nproductcatcode, "
				+ " NULLIF(bfad.jsondata->>'sextractedsampleid', '') AS sextractedsampleid, "
				+ " NULLIF(bfad.jsondata->>'sconcentration', '') AS sconcentration, "
				+ " NULLIF(bfad.jsondata->>'sqcplatform', '') AS sqcplatform, "
				+ " NULLIF(bfad.jsondata->>'seluent', '') AS seluent, bfad.nstoragetypecode "
				+ " from bioformacceptancedetails bfad"
				+ " join product p on p.nproductcode=bfad.nproductcode and p.nsitecode=" + userInfo.getNmastersitecode()
				+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join productcategory pc on pc.nproductcatcode=p.nproductcatcode and pc.nsitecode="
				+ userInfo.getNmastersitecode() + " and pc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bfad.nsitecode="
				+ userInfo.getNtranssitecode() + " and bfad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bfad.nbioformacceptancecode="
				+ nbioFormAccetanceCode + " and bfad.nsamplestatus="
				+ Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus()
				+ " and bfad.nbioformacceptancedetailscode not in (select nbioformacceptancedetailscode"
				+ " from biobankreturndetails where nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " and nbioformacceptancecode="
				+ nbioFormAccetanceCode + ")" + " order by bfad.nbioformacceptancedetailscode";
		final List<BioFormAcceptanceDetails> lstFormAcceptanceDetails = jdbcTemplate.query(strFormAcceptanceDetails,
				new BioFormAcceptanceDetails());

		outputMap.put("lstFormAcceptanceDetails", lstFormAcceptanceDetails);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getActiveBioBankReturn(final int nbioBankReturnCode, final int saveType,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strQuery = "select bbr.nbiobankreturncode, bbr.sbankreturnformnumber, bbr.ntransfertypecode,"
				+ " coalesce(tt.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " tt.jsondata->'sdisplayname'->>'en-US') stransfertypename,  bbr.noriginsitecode,"
				+ " bbr.jsondata->>'soriginsitename' soriginsitename, bbr.dreturndate," + " to_char(bbr.dreturndate,'"
				+ userInfo.getSsitedate() + "')  sreturndate, bbr.ntzreturndate, bbr.noffsetdreturndate,"
				+ " bbr.ntransactionstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus,"
				+ " bbr.jsondata->>'sremarks' sremarks, bbr.nsitecode, bbr.nstatus, cm.scolorhexcode, bbr.jsondata->>'sstorageconditionname' sstorageconditionname,"
				+ " to_char(bbr.ddeliverydate, '" + userInfo.getSsitedate()
				+ "') sdeliverydate, bbr.jsondata->>'sdispatchername'"
				+ " sdispatchername, bbr.jsondata->>'scouriername' scouriername, bbr.jsondata->>'scourierno' scourierno,"
				+ " bbr.jsondata->>'striplepackage' striplepackage, bbr.jsondata->>'svalidationremarks' svalidationremarks"
				+ " from biobankreturn bbr join transfertype tt on bbr.ntransfertypecode=tt.ntransfertypecode and tt.nsitecode="
				+ userInfo.getNmastersitecode() + " and tt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join transactionstatus ts on bbr.ntransactionstatus=ts.ntranscode and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join formwisestatuscolor fwsc on fwsc.ntranscode=ts.ntranscode and fwsc.nsitecode="
				+ userInfo.getNmastersitecode() + " and fwsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fwsc.nformcode="
				+ userInfo.getNformcode() + " join colormaster cm on cm.ncolorcode=fwsc.ncolorcode and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bbr.nsitecode="
				+ userInfo.getNtranssitecode() + " and bbr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbr.nbiobankreturncode="
				+ nbioBankReturnCode + " order by bbr.nbiobankreturncode desc";
		final BioBankReturn objBioBankReturn = (BioBankReturn) jdbcUtilityTemplateFunction.queryForObject(strQuery,
				BioBankReturn.class, jdbcTemplate);

		if (objBioBankReturn != null) {
			outputMap.put("selectedBioBankReturn", objBioBankReturn);
			List<Map<String, Object>> lstChildBioBankReturn = getChildInitialGet(
					objBioBankReturn.getNbiobankreturncode(), userInfo);
			outputMap.put("lstChildBioBankReturn", lstChildBioBankReturn);
			if (saveType == 2) {
				outputMap.putAll(
						(Map<String, Object>) getFormNumberDetails(objBioBankReturn.getNoriginsitecode(), userInfo)
								.getBody());
			}

		} else {
			outputMap.put("selectedBioBankReturn", null);
			outputMap.put("lstChildBioBankReturn", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

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

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> createBioBankReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		jdbcTemplate.execute("lock table lockbiobankreturn " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate
				.execute("lock table lockbiobankreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final ObjectMapper objMapper = new ObjectMapper();

		int nprimaryKeyBioBankReturn = (int) inputMap.get("nprimaryKeyBioBankReturn");

		BioBankReturn objBioBankReturnData = objMapper.convertValue(inputMap.get("bioBankReturn"), BioBankReturn.class);

		List<Map<String, Object>> filteredSampleReceiving = getNotExistSamplesSampleReceivingCode(
				(List<Map<String, Object>>) inputMap.get("filterSelectedSamples"), userInfo);

		if (filteredSampleReceiving.size() == 0) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYEXISTS", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final String sreturnDate = (objBioBankReturnData.getSreturndate() != null
				&& !objBioBankReturnData.getSreturndate().isEmpty())
						? "'" + objBioBankReturnData.getSreturndate().toString().replace("T", " ").replace("Z", "")
								+ "'"
						: null;

		BioBankReturn objBioBankReturn = null;

		if (nprimaryKeyBioBankReturn != -1) {
			final String strCheckAlreadyRecordExists = "select nbiobankreturncode, sbankreturnformnumber from biobankreturn where "
					+ " nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nbiobankreturncode="
					+ nprimaryKeyBioBankReturn;
			objBioBankReturn = (BioBankReturn) jdbcTemplateUtilityFunction.queryForObject(strCheckAlreadyRecordExists,
					BioBankReturn.class, jdbcTemplate);
		}

		int seqNoBioBankReturn = -1;
		int seqNoBioBankReturnHistory = -1;
		int nbiobankreturncode = -1;
		String strInsertBankReturn = "";
		String strInsertBankReturnHistory = "";
		String strSeqNoUpdate = "";
		String strUpdateBankReturn = "";

		if (objBioBankReturn == null) {
			seqNoBioBankReturn = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biobankreturn' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioBankReturn++;

			seqNoBioBankReturnHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biobankreturnhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioBankReturnHistory++;

			nbiobankreturncode = seqNoBioBankReturn;
			final String strformat = projectDAOSupport.getSeqfnFormat("biobankreturn", "seqnoformatgeneratorbiobank", 0,
					0, userInfo);
			strInsertBankReturn = "insert into biobankreturn(nbiobankreturncode, sbankreturnformnumber, ntransfertypecode, nformtypecode, noriginsitecode, dreturndate,"
					+ " ntzreturndate, noffsetdreturndate, ntransactionstatus, jsondata, dtransactiondate,"
					+ " ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) values (" + nbiobankreturncode
					+ ", '" + strformat + "', " + objBioBankReturnData.getNtransfertypecode() + ", "
					+ objBioBankReturnData.getNformtypecode() + ", " + objBioBankReturnData.getNoriginsitecode() + ", "
					+ sreturnDate + ", " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
					+ " json_build_object('sbankreturnformnumber', '" + strformat + "', 'sremarks', '"
					+ stringUtilityFunction.replaceQuote(objBioBankReturnData.getSremarks()) + "', 'soriginsitename', '"
					+ objBioBankReturnData.getSoriginsitename() + "'), '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "); ";
			strSeqNoUpdate += "update seqnobiobankmanagement set nsequenceno=" + seqNoBioBankReturn + " where"
					+ " stablename='biobankreturn' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			strInsertBankReturnHistory = "insert into biobankreturnhistory (nbiobankreturnhistorycode, nbiobankreturncode,"
					+ " ntransactionstatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) values ("
					+ seqNoBioBankReturnHistory + ", " + nbiobankreturncode + ", "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			strInsertBankReturnHistory += "update seqnobiobankmanagement set nsequenceno=" + seqNoBioBankReturnHistory
					+ " where stablename='biobankreturnhistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			inputMap.put("nprimaryKeyBioBankReturn", seqNoBioBankReturn);
			objBioBankReturnData.setSbankreturnformnumber(strformat);
		} else {

			nbiobankreturncode = objBioBankReturn.getNbiobankreturncode();
			strUpdateBankReturn = "update biobankreturn set dreturndate=" + sreturnDate + ", "
					+ (objBioBankReturnData.getSremarks().isEmpty() ? ""
							: " jsondata=jsondata || '{\"sremarks\": \""
									+ stringUtilityFunction.replaceQuote(objBioBankReturnData.getSremarks().toString())
									+ "\"}', ")
					+ " dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ " ntztransactiondate=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " where"
					+ " nbiobankreturncode=" + nbiobankreturncode + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			objBioBankReturnData.setSbankreturnformnumber(objBioBankReturn.getSbankreturnformnumber());
		}

		Map<String, Object> getMap = insertBioBankReturnDetails(objBioBankReturnData.getNbioformacceptancecode(),
				objBioBankReturnData.getSbankreturnformnumber(), filteredSampleReceiving, nbiobankreturncode, userInfo);

		final String rtnQry = (String) getMap.get("queryString");
		final String sbioBankReturnDetailsCode = (String) getMap.get("sbioBankReturnDetailsCode");

		jdbcTemplate.execute(
				strInsertBankReturn + strUpdateBankReturn + rtnQry + strSeqNoUpdate + strInsertBankReturnHistory);
// ===== COC: START =====
//		if (sbioBankReturnDetailsCode != null && !sbioBankReturnDetailsCode.trim().isEmpty()) {
//			String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sQuery1);
//
//			String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sQuery2);
//
//			String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sQuery3);
//
//			int chainCustodyPk = jdbcTemplate.queryForObject(
//					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//					Integer.class);
//
//			String strChainCustody = "insert into chaincustody ("
//					+ " nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
//					+ " ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
//					+ " noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + chainCustodyPk
//					+ " + rank() over(order by brd.nbiobankreturndetailscode), " + userInfo.getNformcode() + ", "
//					+ " brd.nbiobankreturndetailscode, 'nbiobankreturndetailscode', "
//					+ " 'biobankreturndetails', COALESCE(br.sbankreturnformnumber, ''), "
//					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", " + userInfo.getNusercode() + ", "
//					+ userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
//					+ userInfo.getNtimezonecode() + ", "
//					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " ('"
//					+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//					+ " [' || COALESCE(brd.srepositoryid,'') || '] ' || " + " '"
//					+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//					+ " [' || COALESCE(brd.nbioparentsamplecode::text,'') || '] ' || " + " '"
//					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//					+ " [' || COALESCE(br.sbankreturnformnumber,'') || ']') , " + userInfo.getNtranssitecode() + ", "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from biobankreturndetails brd"
//					+ " join biobankreturn br on br.nbiobankreturncode = brd.nbiobankreturncode"
//					+ " and br.nsitecode = " + userInfo.getNtranssitecode() + " and br.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where brd.nsitecode = "
//					+ userInfo.getNtranssitecode() + " and brd.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and brd.nbiobankreturndetailscode in (" + sbioBankReturnDetailsCode + ");";
//
//			String strSeqUpdate = "update seqnoregistration set nsequenceno = nsequenceno + ("
//					+ " select count(*) from biobankreturndetails brd " + " where brd.nbiobankreturndetailscode in ("
//					+ sbioBankReturnDetailsCode + ")" + " and brd.nsitecode = " + userInfo.getNtranssitecode()
//					+ " and brd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ ") where stablename = 'chaincustody' and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//			jdbcTemplate.execute(strChainCustody);
//			jdbcTemplate.execute(strSeqUpdate);
//		}
		// ===== COC: END =====

		final List<Object> lstAuditAfter = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final String strAuditQry = auditQuery(nbiobankreturncode, sbioBankReturnDetailsCode, userInfo);
		List<BioBankReturn> lstAuditReturnDetailsAfter = jdbcTemplate.query(strAuditQry, new BioBankReturn());
		lstAuditAfter.addAll(lstAuditReturnDetailsAfter);
		lstAuditReturnDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_ADDRETURNSAMPLES"));
		auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);

		final int ntransCode = (int) inputMap.get("ntransCode");
		inputMap.put("ntransCode",
				ntransCode == 0 ? ntransCode : Enumeration.TransactionStatus.DRAFT.gettransactionstatus());

		return getBioBankReturn(inputMap, nprimaryKeyBioBankReturn, userInfo);
	}

	public List<Map<String, Object>> getNotExistSamplesSampleReceivingCode(
			final List<Map<String, Object>> filteredSampleReceiving, UserInfo userInfo) throws Exception {
		final String snbioformacceptancedetailscode = filteredSampleReceiving.stream()
				.map(x -> String.valueOf(x.get("nbioformacceptancedetailscode"))).collect(Collectors.joining(","));

		final String strSamplesGet = "select nbiosamplereceivingcode from biobankreturndetails where nbioformacceptancedetailscode in ("
				+ snbioformacceptancedetailscode + ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ";";
		List<Integer> versionList = jdbcTemplate.queryForList(strSamplesGet, Integer.class);

		List<Map<String, Object>> nonMatchingSamples = filteredSampleReceiving.stream()
				.filter(map -> !versionList.contains((Integer) map.get("nbiosamplereceivingcode")))
				.collect(Collectors.toList());

		return nonMatchingSamples;
	}

	public Map<String, Object> insertBioBankReturnDetails(final int nbioFormAcceptanceCode,
			final String sbankReturnFormNumber, final List<Map<String, Object>> filteredSampleReceiving,
			final int nbioBankReturnCode, final UserInfo userInfo) throws Exception {

		Map<String, Object> rtnMap = new HashMap<>();

		String sbioBankReturnDetailsCode = "";

		int seqNoBioBankReturnDetails = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnobiobankmanagement where stablename='biobankreturndetails' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);
		int seqNoBioBankReturnDetailsHistory = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnobiobankmanagement where stablename='biobankreturndetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);

		final var sFormNumberDiposeQry = "select jsondata->>'" + Enumeration.FormCode.BIODIRECTTRANSFER.getFormCode()
				+ "'" + " as directtransfer,jsondata->>'" + Enumeration.FormCode.REQUESTBASEDTRANSFER.getFormCode()
				+ "' as requestbasedransfer,jsondata->>'" + Enumeration.FormCode.ECATALOGREQUESTAPPROVAL.getFormCode()
				+ "' as ecatalogrequestapproval from bioformacceptancedetails " + "where nbioformacceptancecode="
				+ filteredSampleReceiving.get(0).get("nbioformacceptancecode") + " "
				+ "and nbioformacceptancedetailscode="
				+ filteredSampleReceiving.get(0).get("nbioformacceptancedetailscode") + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc  ";

		List<Map<String, Object>> lstForNumber = jdbcTemplate.queryForList(sFormNumberDiposeQry);

		var directTransferFormNumber = lstForNumber.get(0).get("directtransfer");
		var requestBasedTransferFormNumber = lstForNumber.get(0).get("requestbasedransfer");
		var eCatalogRequestApproTransferFormNumber = lstForNumber.get(0).get("ecatalogrequestapproval");

		String strInsertBankReturnDetails = "insert into biobankreturndetails (nbiobankreturndetailscode,"
				+ " nbiobankreturncode, nbioformacceptancecode, nbioformacceptancedetailscode, nbioprojectcode,"
				+ " nbioparentsamplecode," + " ncohortno, nstoragetypecode, nproductcatcode, nproductcode, "
				+ " svolume, sreturnvolume, jsondata," + " ndiagnostictypecode, ncontainertypecode,"
				+ " nbiosamplereceivingcode, nsamplecondition, srepositoryid, "
				+ " nsamplestatus, nreasoncode, slocationcode, nsamplestoragetransactioncode,"
				+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) values ";

		String strBankReturnDetailsCode = "";

		for (Map<String, Object> objBioSampleReceiving : filteredSampleReceiving) {
			seqNoBioBankReturnDetails++;
			sbioBankReturnDetailsCode += seqNoBioBankReturnDetails + ",";

			final String srepositoryId = (objBioSampleReceiving.get("srepositoryid") != ""
					&& objBioSampleReceiving.get("srepositoryid") != null)
							? "'" + objBioSampleReceiving.get("srepositoryid") + "'"
							: null;
			final String svolume = (objBioSampleReceiving.get("svolume") != ""
					&& objBioSampleReceiving.get("svolume") != null) ? "'" + objBioSampleReceiving.get("svolume") + "'"
							: null;
			final String sreturnVolume = (objBioSampleReceiving.get("sreturnvolume") != ""
					&& objBioSampleReceiving.get("sreturnvolume") != null)
							? "'" + objBioSampleReceiving.get("sreturnvolume") + "'"
							: null;
			final String ssubjectId = (objBioSampleReceiving.get("ssubjectid") != ""
					&& objBioSampleReceiving.get("ssubjectid") != null)
							? "'" + objBioSampleReceiving.get("ssubjectid") + "'"
							: null;
			final String scaseType = (objBioSampleReceiving.get("scasetype") != ""
					&& objBioSampleReceiving.get("scasetype") != null)
							? "'" + objBioSampleReceiving.get("scasetype") + "'"
							: null;
			final String sparentSampleCode = (objBioSampleReceiving.get("sparentsamplecode") != ""
					&& objBioSampleReceiving.get("sparentsamplecode") != null)
							? "'" + objBioSampleReceiving.get("sparentsamplecode") + "'"
							: null;
			// added by sujatha ATE_274 for adding inside jsondata of biobankreturndetails
			// bgsi-218
			final String sextractedsampleid = (objBioSampleReceiving.get("sextractedsampleid") != ""
					&& objBioSampleReceiving.get("sextractedsampleid") != null)
							? "'" + objBioSampleReceiving.get("sextractedsampleid") + "'"
							: null;
			final String sconcentration = (objBioSampleReceiving.get("sconcentration") != ""
					&& objBioSampleReceiving.get("sconcentration") != null)
							? "'" + objBioSampleReceiving.get("sconcentration") + "'"
							: null;
			final String sqcplatform = (objBioSampleReceiving.get("sqcplatform") != ""
					&& objBioSampleReceiving.get("sqcplatform") != null)
							? "'" + objBioSampleReceiving.get("sqcplatform") + "'"
							: null;
			final String seluent = (objBioSampleReceiving.get("seluent") != ""
					&& objBioSampleReceiving.get("seluent") != null) ? "'" + objBioSampleReceiving.get("seluent") + "'"
							: null;

			// modified by sujatha ATE_274 by adding the above 4 fields into the jsondata
			// bgsi-218
			strInsertBankReturnDetails += "(" + seqNoBioBankReturnDetails + ", " + nbioBankReturnCode + ", "
					+ nbioFormAcceptanceCode + ", " + objBioSampleReceiving.get("nbioformacceptancedetailscode") + ", "
					+ objBioSampleReceiving.get("nbioprojectcode") + ", "
					+ objBioSampleReceiving.get("nbioparentsamplecode") + ", " + objBioSampleReceiving.get("ncohortno")
					+ ", " + objBioSampleReceiving.get("nstoragetypecode") + ", "
					+ objBioSampleReceiving.get("nproductcatcode") + ", " + objBioSampleReceiving.get("nproductcode")
					+ ", " + svolume + ", " + sreturnVolume + ", json_build_object('sparentsamplecode', "
					+ sparentSampleCode + "," + " 'ssubjectid', " + ssubjectId + ", 'scasetype', " + scaseType
					+ ", 'sbankreturnformnumber', '" + sbankReturnFormNumber + "', 'svolume', " + svolume
					+ ", 'sreturnvolume', " + sreturnVolume + ", 'sformnumber', '" + sbankReturnFormNumber
					+ "', 'srepositoryid', " + srepositoryId + ", 'sextractedsampleid', " + sextractedsampleid
					+ ", 'sconcentration', " + sconcentration + ", 'sqcplatform', " + sqcplatform + ", 'seluent', "
					+ seluent + ",'" + Enumeration.FormCode.BGSIRETURN.getFormCode() + "','" + sbankReturnFormNumber
					+ "', '" + Enumeration.FormCode.BIODIRECTTRANSFER.getFormCode() + "', NULLIF('"
					+ directTransferFormNumber + "','') ,'" + Enumeration.FormCode.REQUESTBASEDTRANSFER.getFormCode()
					+ "', NULLIF('" + requestBasedTransferFormNumber + "','') ,'"
					+ Enumeration.FormCode.ECATALOGREQUESTAPPROVAL.getFormCode() + "', NULLIF('"
					+ eCatalogRequestApproTransferFormNumber + "', '')) , "
					+ objBioSampleReceiving.get("ndiagnostictypecode") + ", "
					+ objBioSampleReceiving.get("ncontainertypecode") + ", "
					+ objBioSampleReceiving.get("nbiosamplereceivingcode") + ", "
					+ objBioSampleReceiving.get("nsamplecondition") + ", " + srepositoryId + ", "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
					+ objBioSampleReceiving.get("nreasoncode") + ", null, "
					+ objBioSampleReceiving.get("nsamplestoragetransactioncode") + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "),";
			strBankReturnDetailsCode += seqNoBioBankReturnDetails + ",";
		}

		strBankReturnDetailsCode = strBankReturnDetailsCode.substring(0, strBankReturnDetailsCode.length() - 1);

		String strInsertBankReturnDetailsHistory = "insert into biobankreturndetailshistory"
				+ " (nbiobankreturndetailshistorycode, nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition,"
				+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
				+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) select "
				+ seqNoBioBankReturnDetailsHistory + "+ rank() over(order by nbiobankreturndetailscode)"
				+ ", nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition, " + " nsamplestatus, '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", "
				+ userInfo.getNdeputyuserrole() + ", nsitecode, "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from biobankreturndetails where"
				+ " nbiobankreturndetailscode in (" + strBankReturnDetailsCode + ") and nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nbiobankreturndetailscode;";

		strInsertBankReturnDetails = strInsertBankReturnDetails.substring(0, strInsertBankReturnDetails.length() - 1)
				+ ";";

		String strSeqNoUpdate = "update seqnobiobankmanagement set nsequenceno=" + seqNoBioBankReturnDetails
				+ " where stablename='biobankreturndetails' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		strInsertBankReturnDetailsHistory += "update seqnobiobankmanagement set nsequenceno=(select "
				+ seqNoBioBankReturnDetailsHistory
				+ "+ count(nbiobankreturndetailscode) from biobankreturndetails where "
				+ " nbiobankreturndetailscode in (" + strBankReturnDetailsCode + ") and nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") where"
				+ " stablename='biobankreturndetailshistory' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		sbioBankReturnDetailsCode = sbioBankReturnDetailsCode.substring(0, sbioBankReturnDetailsCode.length() - 1);

		rtnMap.put("queryString", strInsertBankReturnDetails + strSeqNoUpdate + strInsertBankReturnDetailsHistory);
		rtnMap.put("sbioBankReturnDetailsCode", sbioBankReturnDetailsCode);

		return rtnMap;
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getActiveBioBankReturnById(final int nbioBankReturnCode, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> outputMap = new HashMap<>();
		final String strChildBankReturn = "select bbr.nbiobankreturncode,"
				+ " bbr.noriginsitecode, bbr.jsondata->>'soriginsitename' soriginsitename,"
				+ " coalesce(to_char(bbr.dreturndate, '" + userInfo.getSsitedate()
				+ "'), '-') sreturndate, bbr.jsondata->>'sremarks' sremarks, bbr.sbankreturnformnumber"
				+ " from biobankreturn bbr where bbr.nbiobankreturncode=" + nbioBankReturnCode + "  and bbr.nsitecode="
				+ userInfo.getNtranssitecode() + " and bbr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final BioBankReturn objChildBankReturn = (BioBankReturn) jdbcUtilityTemplateFunction
				.queryForObject(strChildBankReturn, BioBankReturn.class, jdbcTemplate);

		outputMap.put("selectedChildBankReturn", objChildBankReturn);

		if (objChildBankReturn != null) {
			outputMap.putAll(
					(Map<String, Object>) getFormNumberDetails(objChildBankReturn.getNoriginsitecode(), userInfo)
							.getBody());
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> updateBioBankReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final int nbioBankReturnCode = (int) inputMap.get("nbiobankreturncode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		final List<Object> listBeforeSave = new ArrayList<>();

		int findStatus = findStatusBankReturn(nbioBankReturnCode, userInfo);

		final String strAuditQry = auditQuery(nbioBankReturnCode, "", userInfo);
		List<BioBankReturn> lstAuditBefore = jdbcTemplate.query(strAuditQry, new BioBankReturn());

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {
			String sreturnDate = (String) inputMap.get("sreturndate");
			String sremarks = (String) inputMap.get("sremarks");

			sreturnDate = (sreturnDate != null && !sreturnDate.isEmpty())
					? "'" + sreturnDate.toString().replace("T", " ").replace("Z", "") + "'"
					: null;

			final String strUpdateQry = "update biobankreturn set dreturndate=" + sreturnDate + ", jsondata=jsondata ||"
					+ " '{\"sremarks\": \"" + stringUtilityFunction.replaceQuote(sremarks) + "\"}', dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(strUpdateQry);

			List<BioBankReturn> lstAuditAfter = jdbcTemplate.query(strAuditQry, new BioBankReturn());

			multilingualIDList.add("IDS_EDITRETURNFORM");
			listBeforeSave.add(lstAuditBefore.get(0));
			listAfterSave.add(lstAuditAfter.get(0));

			auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);
			return getBioBankReturn(inputMap, nbioBankReturnCode, userInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public int findStatusBankReturn(final int nbioBankReturnCode, final UserInfo userInfo) throws Exception {
		final String strStatusBankReturn = "select ntransactionstatus from biobankreturn where nbiobankreturncode="
				+ nbioBankReturnCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.queryForObject(strStatusBankReturn, Integer.class);
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getValidateFormDetails(final int nbioBankReturnCode, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> outputMap = new HashMap<>();

		final int nformStatus = findStatusBankReturn(nbioBankReturnCode, userInfo);
		if (nformStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			List<Map<String, Object>> lstChildBioBankReturn = getChildInitialGet(nbioBankReturnCode, userInfo);
			if (lstChildBioBankReturn.size() > 0) {
				outputMap.putAll((Map<String, Object>) getStorageCondition(userInfo).getBody());
				outputMap.putAll((Map<String, Object>) getUsersBasedOnSite(userInfo).getBody());
				outputMap.putAll((Map<String, Object>) getCourier(userInfo).getBody());
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOSAMPLESAVAILABLETOVALIDATE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception {
		Map<String, Object> outputMap = new HashMap<>();

		final String strStorageCondition = "select nstorageconditioncode, sstorageconditionname from storagecondition where "
				+ " nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nstorageconditioncode > 0"
				+ " order by nstorageconditioncode";
		List<Map<String, Object>> lstGetStorageCondition = jdbcTemplate.queryForList(strStorageCondition);

		List<Map<String, Object>> lstStorageCondition = new ArrayList<>();
		lstGetStorageCondition.stream().forEach(lst -> {
			Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("sstorageconditionname"));
			mapStatus.put("value", lst.get("nstorageconditioncode"));
			mapStatus.put("item", lst);
			lstStorageCondition.add(mapStatus);
		});

		outputMap.put("lstStorageCondition", lstStorageCondition);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getUsersBasedOnSite(final UserInfo userInfo) throws Exception {
		Map<String, Object> outputMap = new HashMap<>();

		final String strUsersBasedOnSite = "select u.nusercode ndispatchercode, concat(u.sfirstname, ' ', u.slastname) sdispatchername from"
				+ " users u, userssite us where u.nusercode=us.nusercode and us.nsitecode="
				+ userInfo.getNtranssitecode() + " and u.nsitecode=" + userInfo.getNmastersitecode() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nusercode > 0 order by u.nusercode";
		List<Map<String, Object>> lstGetUsersBasedOnSite = jdbcTemplate.queryForList(strUsersBasedOnSite);

		List<Map<String, Object>> lstUsers = new ArrayList<>();
		lstGetUsersBasedOnSite.stream().forEach(lst -> {
			Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("sdispatchername"));
			mapStatus.put("value", lst.get("ndispatchercode"));
			mapStatus.put("item", lst);
			lstUsers.add(mapStatus);
		});

		outputMap.put("lstUsers", lstUsers);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getCourier(final UserInfo userInfo) throws Exception {
		Map<String, Object> outputMap = new HashMap<>();

		final String strCourier = "select ncouriercode, scouriername from courier where nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ncouriercode > 0 order by ncouriercode";
		List<Map<String, Object>> lstGetCourier = jdbcTemplate.queryForList(strCourier);

		List<Map<String, Object>> lstCourier = new ArrayList<>();
		lstGetCourier.stream().forEach(lst -> {
			Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("scouriername"));
			mapStatus.put("value", lst.get("ncouriercode"));
			mapStatus.put("item", lst);
			lstCourier.add(mapStatus);
		});

		outputMap.put("lstCourier", lstCourier);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> createValidationBioBankReturn(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		jdbcTemplate.execute("lock table lockbiobankreturn " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate
				.execute("lock table lockbiobankreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final ObjectMapper objMapper = new ObjectMapper();

		final int nbioBankReturnCode = (int) inputMap.get("nbiobankreturncode");
		final int findStatus = findStatusBankReturn(nbioBankReturnCode, userInfo);

		final List<Object> savedBioBankReturnList = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			final String strBioBankReturnDetailsCode = getToBeDisposeSamples(nbioBankReturnCode, userInfo);

			final String concatCondition = (strBioBankReturnDetailsCode != null && strBioBankReturnDetailsCode != "")
					? " and nbiobankreturndetailscode not in (" + strBioBankReturnDetailsCode + ")"
					: "";

			BioBankReturn objBioBankReturn = objMapper.convertValue(inputMap.get("bioBankReturn"), BioBankReturn.class);

			final String sdeliveryDate = (objBioBankReturn.getSdeliverydate() != null
					&& !objBioBankReturn.getSdeliverydate().isEmpty())
							? "'" + objBioBankReturn.getSdeliverydate().toString().replace("T", " ").replace("Z", "")
									+ "'"
							: null;

			final String strUpdateBankReturn = "update biobankreturn set nstorageconditioncode="
					+ objBioBankReturn.getNstorageconditioncode() + ", ddeliverydate=" + sdeliveryDate
					+ ", ndispatchercode=" + objBioBankReturn.getNdispatchercode() + ", ncouriercode="
					+ objBioBankReturn.getNcouriercode() + ", jsondata=jsondata || '{\"sdispatchername\": \""
					+ objBioBankReturn.getSdispatchername() + "\", " + " \"sstorageconditionname\": \""
					+ objBioBankReturn.getSstorageconditionname() + "\", \"scouriername\": \""
					+ objBioBankReturn.getScouriername() + "\", \"striplepackage\": \""
					+ stringUtilityFunction.replaceQuote(objBioBankReturn.getStriplepackage())
					+ "\", \"svalidationremarks\": \""
					+ stringUtilityFunction.replaceQuote(objBioBankReturn.getSvalidationremarks())
					+ "\", \"scourierno\": \"" + stringUtilityFunction.replaceQuote(objBioBankReturn.getScourierno())
					+ "\"}'" + ", ntransactionstatus=" + Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()
					+ ", dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ " ntztransactiondate=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final String strUpdateBankReturnDetails = "update biobankreturndetails set nsamplestatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + concatCondition + ";";

			int seqNoBioBankReturnHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biobankreturnhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioBankReturnHistory++;
			String strInsertBankReturnHistory = "insert into biobankreturnhistory (nbiobankreturnhistorycode, nbiobankreturncode,"
					+ " ntransactionstatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) values ("
					+ seqNoBioBankReturnHistory + ", " + nbioBankReturnCode + ", "
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			strInsertBankReturnHistory += "update seqnobiobankmanagement set nsequenceno=" + seqNoBioBankReturnHistory
					+ " where stablename='biobankreturnhistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			int seqNoBioBankReturnDetailsHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biobankreturndetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strInsertBankReturnDetailsHistory = "insert into biobankreturndetailshistory"
					+ " (nbiobankreturndetailshistorycode, nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition,"
					+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) select "
					+ seqNoBioBankReturnDetailsHistory + "+ rank() over(order by nbiobankreturndetailscode)"
					+ ", nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition, " + " nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", nsitecode, "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from biobankreturndetails where"
					+ " nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsamplestatus != " + Enumeration.TransactionStatus.STORED.gettransactionstatus()
					+ concatCondition + ";";
			strInsertBankReturnDetailsHistory += "update seqnobiobankmanagement set nsequenceno=(select "
					+ seqNoBioBankReturnDetailsHistory + "+ count(nbiobankreturndetailscode) from"
					+ " biobankreturndetails where nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ") where"
					+ " stablename='biobankreturndetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strUpdateBankReturn + strUpdateBankReturnDetails + strInsertBankReturnHistory
					+ strInsertBankReturnDetailsHistory);

			// ===== COC: START =====
//			{
//				String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery1);
//
//				String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery2);
//
//				String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery3);
//
//				int chainCustodyPk = jdbcTemplate.queryForObject(
//						"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//						Integer.class);
//
//				String whereClause = " brd.nbiobankreturncode = " + nbioBankReturnCode + " and brd.nsitecode = "
//						+ userInfo.getNtranssitecode() + " and brd.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and brd.nsamplestatus != "
//						+ Enumeration.TransactionStatus.STORED.gettransactionstatus();
//
//				if (strBioBankReturnDetailsCode != null && !strBioBankReturnDetailsCode.trim().isEmpty()) {
//					whereClause += " and brd.nbiobankreturndetailscode not in (" + strBioBankReturnDetailsCode + ")";
//				}
//
//				String strChainCustody = "insert into chaincustody ("
//						+ " nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno, "
//						+ " ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate, "
//						+ " noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + chainCustodyPk
//						+ " + rank() over(order by brd.nbiobankreturndetailscode), " + userInfo.getNformcode() + ", "
//						+ " brd.nbiobankreturndetailscode, 'nbiobankreturndetailscode', "
//						+ " 'biobankreturndetails', COALESCE(br.sbankreturnformnumber, ''), "
//						+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", "
//						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
//						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
//						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " ('"
//						+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//								.replace("'", "''")
//						+ " [' || COALESCE(brd.srepositoryid,'') || '] ' || '"
//						+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//								.replace("'", "''")
//						+ " [' || COALESCE(brd.nbioparentsamplecode::text,'') || '] ' || '"
//						+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//								.replace("'", "''")
//						+ " [' || COALESCE(br.sbankreturnformnumber,'') || ']'), " + userInfo.getNtranssitecode() + ", "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from biobankreturndetails brd"
//						+ " join biobankreturn br on br.nbiobankreturncode = brd.nbiobankreturncode"
//						+ " and br.nsitecode = " + userInfo.getNtranssitecode() + " and br.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where " + whereClause + ";";
//
//				String strSeqUpdate = "update seqnoregistration set nsequenceno = " + chainCustodyPk
//						+ " + (select count(*) from biobankreturndetails brd where " + whereClause + ")"
//						+ " where stablename = 'chaincustody' and nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//				jdbcTemplate.execute(strChainCustody);
//				jdbcTemplate.execute(strSeqUpdate);
//			}
			// ===== COC: END =====

			final String concatSelect = ", bbr.nstorageconditioncode, to_char(bbr.ddeliverydate, '"
					+ userInfo.getSsitedate()
					+ "') sdeliverydate, bbr.jsondata->>'sdispatchername' sdispatchername, bbr.ncouriercode, bbr.jsondata->>'scourierno' scourierno,"
					+ " bbr.jsondata->>'striplepackage' striplepackage, bbr.jsondata->>'svalidationremarks' svalidationremarks ";
			final String concatJoin = "";
			final String strAuditQry = auditParentQuery(nbioBankReturnCode, concatSelect, concatJoin, userInfo);
			List<BioBankReturn> lstAuditAfter = jdbcTemplate.query(strAuditQry, new BioBankReturn());

			lstAuditAfter.stream().forEach(x -> multilingualIDList.add("IDS_VALIDATERETURNFORM"));
			savedBioBankReturnList.addAll(lstAuditAfter);

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);
			return getActiveBioBankReturn(nbioBankReturnCode, 1, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public String getToBeDisposeSamples(final int nbioBankReturnCode, final UserInfo userInfo) throws Exception {
		final String strToBeDisposeSamples = "select string_agg(nbiobankreturndetailscode::text, ',') from"
				+ " biobankreturndetails where nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode="
				+ userInfo.getNtranssitecode() + " and nsamplestatus="
				+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.queryForObject(strToBeDisposeSamples, String.class);
	}

	public ResponseEntity<Object> returnBankReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		jdbcTemplate.execute("lock table lockbiobankreturn " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate
				.execute("lock table lockbiobankreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute("lock table lockbioformacceptance " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbioformacceptancedetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final int nbioBankReturnCode = (int) inputMap.get("nbiobankreturncode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		final List<Object> listBeforeSave = new ArrayList<>();

		final int findStatus = findStatusBankReturn(nbioBankReturnCode, userInfo);
		if (findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {
			final boolean availableSamples = checkSamplesAvailableToReturn(nbioBankReturnCode, userInfo);
			if (availableSamples) {

				final String strAuditQry = auditQuery(nbioBankReturnCode, "", userInfo);
				List<BioBankReturn> lstAuditBefore = jdbcTemplate.query(strAuditQry, new BioBankReturn());

				int intFormAcceptancePk = jdbcTemplate
						.queryForObject("select nsequenceno from seqnobiobankmanagement where "
								+ "stablename='bioformacceptance' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
				intFormAcceptancePk++;

				int formAcceptanceHistoryPk = jdbcTemplate
						.queryForObject("select nsequenceno from seqnobiobankmanagement where "
								+ "stablename='bioformacceptancehistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
				formAcceptanceHistoryPk++;
				int formAcceptanceHistoryDetailsPk = jdbcTemplate
						.queryForObject("select nsequenceno from seqnobiobankmanagement where "
								+ "stablename='bioformacceptdetailshistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);

				final String strBioBankReturnDetailsCode = getToBeDisposeSamples(nbioBankReturnCode, userInfo);
				String concatConditionBankReturnDetailsNot = (strBioBankReturnDetailsCode != ""
						&& strBioBankReturnDetailsCode != null)
								? " and nbiobankreturndetailscode not in (" + strBioBankReturnDetailsCode + ")"
								: "";
				String concatConditionBankReturnDetails = (strBioBankReturnDetailsCode != ""
						&& strBioBankReturnDetailsCode != null)
								? " and nbiobankreturndetailscode in (" + strBioBankReturnDetailsCode + ")"
								: "";
				String strUpdateBankReturnAndDetails = "update biobankreturn set ntransactionstatus="
						+ Enumeration.TransactionStatus.RETURN.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
						+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " where nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				strUpdateBankReturnAndDetails += "update biobankreturndetails set nsamplestatus="
						+ Enumeration.TransactionStatus.RETURN.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
						+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " where nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
						+ Enumeration.TransactionStatus.STORED.gettransactionstatus()
						+ concatConditionBankReturnDetailsNot + ";";

				int seqNoBioBankReturnHistory = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnobiobankmanagement where stablename='biobankreturnhistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);
				seqNoBioBankReturnHistory++;

				String strInsertBankReturnHistory = "insert into biobankreturnhistory (nbiobankreturnhistorycode, nbiobankreturncode,"
						+ " ntransactionstatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
						+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) values ("
						+ seqNoBioBankReturnHistory + ", " + nbioBankReturnCode + ", "
						+ Enumeration.TransactionStatus.RETURN.gettransactionstatus() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

				strInsertBankReturnHistory += "update seqnobiobankmanagement set nsequenceno="
						+ seqNoBioBankReturnHistory + " where stablename='biobankreturnhistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				int seqNoBioBankReturnDetailsHistory = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnobiobankmanagement where stablename='biobankreturndetailshistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);

				String strInsertBankReturnDetailsHistory = "insert into biobankreturndetailshistory"
						+ " (nbiobankreturndetailshistorycode, nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition,"
						+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
						+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) select "
						+ seqNoBioBankReturnDetailsHistory + "+ rank() over(order by nbiobankreturndetailscode)"
						+ ", nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition, " + " nsamplestatus, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", nsitecode, "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biobankreturndetails where nbiobankreturncode=" + nbioBankReturnCode
						+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
						+ Enumeration.TransactionStatus.STORED.gettransactionstatus()
						+ " order by nbiobankreturndetailscode;";

				strInsertBankReturnDetailsHistory += "update seqnobiobankmanagement set nsequenceno=(select "
						+ seqNoBioBankReturnDetailsHistory + "+ count(nbiobankreturndetailscode) from"
						+ " biobankreturndetails where nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
						+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ") where"
						+ " stablename='biobankreturndetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				String strBioFormAcceptance = "insert into bioformacceptance (nbioformacceptancecode, nbiodirecttransfercode,"
						+ " nbiorequestbasedtransfercode, nbiobankreturncode, sformnumber, ntransfertypecode, nformtypecode, noriginsitecode,"
						+ " nsenderusercode, nsenderuserrolecode, dtransferdate, ntztransferdate, noffsetdtransferdate,"
						+ " ntransactionstatus, nstorageconditioncode, ddeliverydate, ntzdeliverydate,"
						+ " noffsetddeliverydate, ndispatchercode, ncouriercode, jsondata, "
						+ "dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode,"
						+ " nstatus) select " + intFormAcceptancePk + ", -1, -1, " + nbioBankReturnCode
						+ ", sbankreturnformnumber," + " ntransfertypecode, 2, " + userInfo.getNtranssitecode() + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole()
						+ ", dreturndate, ntzreturndate, noffsetdreturndate, "
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
						+ " nstorageconditioncode, ddeliverydate, ntzdeliverydate, noffsetddeliverydate, ndispatchercode,"
						+ " ncouriercode, jsondata || json_build_object('sformnumber', sbankreturnformnumber, 'soriginsitename',"
						+ " '" + userInfo.getSsitename() + "')::jsonb, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", noriginsitecode, " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biobankreturn where nbiobankreturncode=" + nbioBankReturnCode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode() + " order by nbiobankreturncode;";

				strBioFormAcceptance += "insert into bioformacceptancehistory (nbioformacceptancehistorycode,"
						+ " nbioformacceptancecode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
						+ " noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode,"
						+ " nstatus) select " + formAcceptanceHistoryPk + ", " + intFormAcceptancePk + ", "
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", noriginsitecode, "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biobankreturn where nbiobankreturncode=" + nbioBankReturnCode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode() + " order by nbiobankreturncode;";

				strBioFormAcceptance += " update seqnobiobankmanagement set nsequenceno=" + intFormAcceptancePk
						+ " where stablename='bioformacceptance' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				strBioFormAcceptance += " update seqnobiobankmanagement set nsequenceno=" + formAcceptanceHistoryPk
						+ " where stablename='bioformacceptancehistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				int intFormAcceptanceDetailsPk = jdbcTemplate
						.queryForObject(
								"select nsequenceno from seqnobiobankmanagement where "
										+ "stablename='bioformacceptancedetails' and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";",
								Integer.class);

				String strBioFormAcceptanceDetails = "insert into bioformacceptancedetails (nbioformacceptancedetailscode,"
						+ " nbioformacceptancecode, nbioprojectcode, nbioparentsamplecode,"
						+ " ncohortno, nstoragetypecode,"
						+ " nproductcatcode, nproductcode, svolume, sreceivedvolume,ssubjectid, jsondata,"
						+ " ndiagnostictypecode, ncontainertypecode, "
						+ " nbiosamplereceivingcode, nsamplecondition, nsamplestatus, srepositoryid, nreasoncode, dtransactiondate,"
						+ " ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) select "
						+ intFormAcceptanceDetailsPk + "+rank()over(order by nbiobankreturndetailscode), "
						+ intFormAcceptancePk + ", nbioprojectcode, " + " nbioparentsamplecode,"
						+ " ncohortno, nstoragetypecode, nproductcatcode, nproductcode, svolume, sreturnvolume,jsondata->>'ssubjectid' as ssubjectid,"
						+ " jsondata,ndiagnostictypecode, ncontainertypecode,"
						+ " nbiosamplereceivingcode, nsamplecondition, "
						+ " nsamplestatus, srepositoryid, nreasoncode, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", (select noriginsitecode from biobankreturn where nbiobankreturncode=" + nbioBankReturnCode
						+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "), "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biobankreturndetails where nbiobankreturncode=" + nbioBankReturnCode
						+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus="
						+ Enumeration.TransactionStatus.RETURN.gettransactionstatus()
						+ " order by nbiobankreturndetailscode;";

				strBioFormAcceptanceDetails += "insert into bioformacceptdetailshistory"
						+ " (nbioformacceptdetailshistorycode, nbioformacceptancedetailscode, nbioformacceptancecode,"
						+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
						+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
						+ "select " + formAcceptanceHistoryDetailsPk
						+ "+rank()over(order by nbioformacceptancedetailscode),"
						+ " nbioformacceptancedetailscode, nbioformacceptancecode, nsamplecondition, nsamplestatus, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						+ " (select noriginsitecode from biobankreturn where nbiobankreturncode=" + nbioBankReturnCode
						+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "), "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from bioformacceptancedetails where nbioformacceptancecode=" + intFormAcceptancePk
						+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by nbioformacceptancedetailscode;";

				strBioFormAcceptanceDetails += " update seqnobiobankmanagement set nsequenceno=(" + "select "
						+ intFormAcceptanceDetailsPk
						+ " + count(nbiobankreturndetailscode) from biobankreturndetails where" + " nbiobankreturncode="
						+ nbioBankReturnCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus="
						+ Enumeration.TransactionStatus.RETURN.gettransactionstatus()
						+ ") where stablename='bioformacceptancedetails' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				strBioFormAcceptanceDetails += " update seqnobiobankmanagement set nsequenceno=(" + "select "
						+ formAcceptanceHistoryDetailsPk
						+ " + count(nbioformacceptancedetailscode) from bioformacceptancedetails where"
						+ " nbioformacceptancecode=" + intFormAcceptancePk + " and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
						+ " where stablename='bioformacceptdetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				String strMoveToDispose = "";
				String strMoveToDisposeDetails = "";

				if (strBioBankReturnDetailsCode != "" && strBioBankReturnDetailsCode != null) {

					strUpdateBankReturnAndDetails += "update biobankreturndetails set nsamplestatus="
							+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus()
							+ ", dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							+ " ntztransactiondate=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ " where nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ concatConditionBankReturnDetails + ";";

					int intBioMoveToDisposePk = jdbcTemplate
							.queryForObject(
									"select nsequenceno from seqnobiobankmanagement where "
											+ "stablename='biomovetodispose' and nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";",
									Integer.class);
					int intBioMoveToDisposeDetailsPk = jdbcTemplate
							.queryForObject(
									"select nsequenceno from seqnobiobankmanagement where "
											+ "stablename='biomovetodisposedetails' and nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";",
									Integer.class);

					intBioMoveToDisposePk++;
					strMoveToDispose += "insert into biomovetodispose (nbiomovetodisposecode, sformnumber,"
							+ " ntransfertypecode, nformtypecode, nthirdpartycode, ntransactionstatus, noriginsitecode, sremarks,"
							+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)"
							+ " select " + intBioMoveToDisposePk + ", sbankreturnformnumber, ntransfertypecode, 2, -1, "
							+ Enumeration.TransactionStatus.RETURN.gettransactionstatus()
							+ ", noriginsitecode, jsondata->>'sremarks', '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " from biobankreturn where nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
							+ " nbiobankreturncode=" + nbioBankReturnCode + ";";
					strMoveToDispose += " update seqnobiobankmanagement set nsequenceno=" + intBioMoveToDisposePk
							+ " where stablename='biomovetodispose' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					// modified by sujatha ATE_274 by adding 4 fields(extractedsampleid,
					// concentration, eluent, qcplatform) into biomovetodisposedetails when return
					// transfer bgsi-218
					strMoveToDisposeDetails += "insert into biomovetodisposedetails (nbiomovetodisposedetailscode,"
							+ " nbiomovetodisposecode, nbioprojectcode, nbioparentsamplecode,"
							+ " nsamplestoragetransactioncode, svolume, jsondata, ncohortno, nstoragetypecode,"
							+ " nproductcatcode, nproductcode, nsamplecondition, ndiagnostictypecode, "
							+ " ncontainertypecode, nsamplestatus, nreasoncode, dtransactiondate, "
							+ " ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)" + " select "
							+ intBioMoveToDisposeDetailsPk + "+rank()over(order by nbiobankreturndetailscode), "
							+ intBioMoveToDisposePk
							+ ", nbioprojectcode, nbioparentsamplecode, nsamplestoragetransactioncode, sreturnvolume,"
							+ " jsonb_build_object('sparentsamplecode', jsondata->>'sparentsamplecode', 'srepositoryid',"
							+ " srepositoryid, 'ssubjectid', jsondata->>'ssubjectid', 'scasetype', jsondata->>'scasetype', 'slocationcode', case when slocationcode is null then '' else slocationcode end, "
							+ " 'sextractedsampleid', jsondata->>'sextractedsampleid','sconcentration', jsondata->>'sconcentration',"
							+ " 'sqcplatform', jsondata->>'sqcplatform','seluent', jsondata->>'seluent'), ncohortno,"
							+ " nstoragetypecode, nproductcatcode, nproductcode, nsamplecondition, ndiagnostictypecode,"
							+ " ncontainertypecode, "
							+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ", nreasoncode, "
							+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ userInfo.getNtimezonecode() + ", "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " from biobankreturndetails where nsitecode=" + userInfo.getNtranssitecode()
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
							+ " nbiobankreturncode=" + nbioBankReturnCode + concatConditionBankReturnDetails + ";";
					strMoveToDisposeDetails += "update seqnobiobankmanagement set nsequenceno=(select "
							+ intBioMoveToDisposeDetailsPk
							+ "+ count(nbiobankreturndetailscode) from biobankreturndetails where nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nbiobankreturncode="
							+ nbioBankReturnCode + concatConditionBankReturnDetails
							+ ") where stablename='biomovetodisposedetails'" + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				}

				jdbcTemplate.execute(strUpdateBankReturnAndDetails + strBioFormAcceptance + strBioFormAcceptanceDetails
						+ strInsertBankReturnHistory + strInsertBankReturnDetailsHistory + strMoveToDispose
						+ strMoveToDisposeDetails);

				// ===== COC: START =====
				{
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

					String whereClause = " brd.nbiobankreturncode = " + nbioBankReturnCode + " and brd.nsitecode = "
							+ userInfo.getNtranssitecode() + " and brd.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and brd.nsamplestatus != "
							+ Enumeration.TransactionStatus.STORED.gettransactionstatus();

					if (concatConditionBankReturnDetailsNot != null
							&& !concatConditionBankReturnDetailsNot.trim().isEmpty()) {
						whereClause += " " + concatConditionBankReturnDetailsNot.replace("nbiobgsireturndetailscode",
								"nbiobankreturndetailscode");
					}

					String strChainCustody = "insert into chaincustody ("
					        + " nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
					        + " ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
					        + " noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + chainCustodyPk
					        + " + rank() over(order by brd.nbiobankreturndetailscode), " + userInfo.getNformcode()
					        + ", " + " brd.nbiobankreturndetailscode, 'nbiobankreturndetailscode', "
					        + " 'biobankreturndetails', COALESCE(brd.srepositoryid, ''), "
					        + Enumeration.TransactionStatus.RETURN.gettransactionstatus() + ", "
					        + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
					        + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
					        + ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					        + " ('"
					        + commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
					                .replace("'", "''")
					        + " [' || COALESCE(brd.srepositoryid,'') || '] ' || '"
					        + commonFunction
					                .getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
					                .replace("'", "''")
					        + " [' || COALESCE(brd.nbioparentsamplecode::text,'') || '] ' || '"
					        + commonFunction
					                .getMultilingualMessage("IDS_SENTFROM", userInfo.getSlanguagefilename())
					                .replace("'", "''")
					        + " [' || COALESCE(sfrom.ssitename,'') || '] ' || '"
					        + commonFunction
					                .getMultilingualMessage("IDS_SENTTO", userInfo.getSlanguagefilename())
					                .replace("'", "''")
					        + " [' || COALESCE(sto.ssitename,'') || '] ' || '"
					        + commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
					                .replace("'", "''")
					        + " [' || COALESCE(br.sbankreturnformnumber,'') || ']'), " + userInfo.getNtranssitecode()
					        + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					        + " from biobankreturndetails brd"
					        + " join biobankreturn br on br.nbiobankreturncode = brd.nbiobankreturncode"
					        + " and br.nsitecode = " + userInfo.getNtranssitecode()
					        + " and br.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					        + " join site sfrom on sfrom.nsitecode = br.noriginsitecode"
					        + " join site sto on sto.nsitecode = br.nsitecode"
					        + " where " + whereClause
					        + ";";

					String strSeqUpdate = "update seqnoregistration set nsequenceno = " + chainCustodyPk
							+ " + (select count(*) from biobankreturndetails brd where " + whereClause + ")"
							+ " where stablename = 'chaincustody' and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					jdbcTemplate.execute(strChainCustody);
					jdbcTemplate.execute(strSeqUpdate);
				}
				// ===== COC: END =====

				inputMap.put("ntransCode", Enumeration.TransactionStatus.RETURN.gettransactionstatus());

				List<BioBankReturn> lstAuditAfter = jdbcTemplate.query(strAuditQry, new BioBankReturn());

				lstAuditAfter.stream().forEach(x -> multilingualIDList.add("IDS_RETURNSAMPLES"));

				listBeforeSave.addAll(lstAuditBefore);
				listAfterSave.addAll(lstAuditAfter);

				auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
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
					mailMap.put("nbiobankreturncode", nbioBankReturnCode);
					String query = "SELECT sbankreturnformnumber,noriginsitecode FROM biobankreturn where nbiobankreturncode="
							+ nbioBankReturnCode;
					List<BioBankReturn> forEmailRecords = jdbcTemplate.query(query, new BioBankReturn());
					String referenceId = forEmailRecords.get(0).getSbankreturnformnumber();
					int noriginsitecode = forEmailRecords.get(0).getNoriginsitecode();
					mailMap.put("ssystemid", referenceId);
					mailMap.put("noriginsitecode", noriginsitecode);
					final UserInfo mailUserInfo = new UserInfo(userInfo);
					mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
					mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
					emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
				}

				return getActiveBioBankReturn(nbioBankReturnCode, 1, userInfo);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOAVAILABLESAMPLESTORETURN",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public boolean checkApprovedSample(final int nbioBankReturnCode, final UserInfo userInfo) throws Exception {
		final String strCheckAllSamplesApproved = "select not exists (select nbiobankreturndetailscode from biobankreturndetails"
				+ " where nbiobankreturncode=" + nbioBankReturnCode + " and nsamplecondition="
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " and nsamplestatus="
				+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
		return jdbcTemplate.queryForObject(strCheckAllSamplesApproved, boolean.class);
	}

	public boolean checkSamplesAvailableToReturn(final int nbioBankReturnCode, UserInfo userInfo) throws Exception {
		final String strCheck = "select exists(select from biobankreturndetails where nbiobankreturncode="
				+ nbioBankReturnCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus not in ("
				+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + "))";
		return jdbcTemplate.queryForObject(strCheck, boolean.class);
	}

	public ResponseEntity<Object> cancelBankReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		jdbcTemplate.execute("lock table lockbiobankreturn " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate
				.execute("lock table lockbiobankreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final int nbioBankReturnCode = (int) inputMap.get("nbiobankreturncode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		final List<Object> listBeforeSave = new ArrayList<>();

		final int recordStatus = findStatusBankReturn(nbioBankReturnCode, userInfo);

		if (recordStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| recordStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			final String strAuditQry = auditParentQuery(nbioBankReturnCode, "", "", userInfo);
			List<BioBankReturn> lstAuditBefore = jdbcTemplate.query(strAuditQry, new BioBankReturn());

			final String strCancelBankReturn = "update biobankreturn set ntransactionstatus="
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final String strCancelBankReturnDetails = "update biobankreturndetails set nsamplestatus="
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			int seqNoBioBankReturnHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biobankreturnhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioBankReturnHistory++;

			String strInsertBankReturnHistory = "insert into biobankreturnhistory (nbiobankreturnhistorycode, nbiobankreturncode,"
					+ " ntransactionstatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) values ("
					+ seqNoBioBankReturnHistory + ", " + nbioBankReturnCode + ", "
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			strInsertBankReturnHistory += "update seqnobiobankmanagement set nsequenceno=" + seqNoBioBankReturnHistory
					+ " where stablename='biobankreturnhistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			int seqNoBioBankReturnDetailsHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biobankreturndetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strInsertBankReturnDetailsHistory = "insert into biobankreturndetailshistory"
					+ " (nbiobankreturndetailshistorycode, nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition,"
					+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) select "
					+ seqNoBioBankReturnDetailsHistory + "+ rank() over(order by nbiobankreturndetailscode)"
					+ ", nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition, " + " nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", nsitecode, "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from biobankreturndetails where"
					+ " nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsamplestatus != " + Enumeration.TransactionStatus.STORED.gettransactionstatus()
					+ " order by nbiobankreturndetailscode;";

			strInsertBankReturnDetailsHistory += "update seqnobiobankmanagement set nsequenceno=(select "
					+ seqNoBioBankReturnDetailsHistory + "+ count(nbiobankreturndetailscode) from"
					+ " biobankreturndetailshistory where nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") where"
					+ " stablename='biobankreturndetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strCancelBankReturn + strCancelBankReturnDetails + strInsertBankReturnHistory
					+ strInsertBankReturnDetailsHistory);

			// ===== COC: START =====
//			String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sQuery1);
//
//			String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sQuery2);
//
//			String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sQuery3);
//
//			int chainCustodyPk = jdbcTemplate.queryForObject(
//					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//					Integer.class);
//
//			String strChainCustody = "insert into chaincustody ("
//					+ "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
//					+ "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
//					+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + chainCustodyPk
//					+ " + rank() over(order by brd.nbiobankreturndetailscode), " + userInfo.getNformcode() + ", "
//					+ " brd.nbiobankreturndetailscode, 'nbiobankreturndetailscode', "
//					+ " 'biobankreturndetails', COALESCE(br.sbankreturnformnumber, ''), "
//					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", " + userInfo.getNusercode()
//					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
//					+ userInfo.getNtimezonecode() + ", "
//					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " ('"
//					+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//							.replace("'", "''")
//					+ " [' || COALESCE(brd.srepositoryid,'') || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//							.replace("'", "''")
//					+ " [' || COALESCE(brd.nbioparentsamplecode::text,'') || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//							.replace("'", "''")
//					+ " [' || COALESCE(br.sbankreturnformnumber,'') || ']'), " + userInfo.getNtranssitecode() + ", "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from biobankreturndetails brd"
//					+ " join biobankreturn br on br.nbiobankreturncode = brd.nbiobankreturncode"
//					+ " and br.nsitecode = " + userInfo.getNtranssitecode() + " and br.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where brd.nbiobankreturncode = "
//					+ nbioBankReturnCode + " and brd.nsitecode = " + userInfo.getNtranssitecode()
//					+ " and brd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and brd.nsamplestatus != " + Enumeration.TransactionStatus.STORED.gettransactionstatus() + ";";
//
//			String strSeqUpdate = "update seqnoregistration set nsequenceno = " + chainCustodyPk
//					+ " + (select count(*) from biobankreturndetails brd " + " where brd.nbiobankreturncode = "
//					+ nbioBankReturnCode + " and brd.nsitecode = " + userInfo.getNtranssitecode()
//					+ " and brd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and brd.nsamplestatus != " + Enumeration.TransactionStatus.STORED.gettransactionstatus()
//					+ ") where stablename = 'chaincustody' and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//			jdbcTemplate.execute(strChainCustody);
//			jdbcTemplate.execute(strSeqUpdate);
			// ===== COC: END =====

			List<BioBankReturn> lstAuditAfter = jdbcTemplate.query(strAuditQry, new BioBankReturn());

			lstAuditAfter.stream().forEach(x -> multilingualIDList.add("IDS_CANCELRETURNFORM"));
			listBeforeSave.addAll(lstAuditBefore);
			listAfterSave.addAll(lstAuditAfter);

			auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);

			return getActiveBioBankReturn(nbioBankReturnCode, 1, userInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> addBankReturnDetails(final int nbioBankReturnCode, final int noriginSiteCode,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final int recordStatus = findStatusBankReturn(nbioBankReturnCode, userInfo);

		if (recordStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| recordStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			outputMap.putAll((Map<String, Object>) getFormNumberDetails(noriginSiteCode, userInfo).getBody());

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> createChildBioBankReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		jdbcTemplate.execute("lock table lockbiobankreturn " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate
				.execute("lock table lockbiobankreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final int nbioBankReturnCode = (int) inputMap.get("nbiobankreturncode");
		final String sbankReturnFormNumber = (String) inputMap.get("sbankreturnformnumber");

		final int saveType = inputMap.containsKey("saveType") ? Integer.valueOf(inputMap.get("saveType").toString())
				: 1;

		int findStatus = findStatusBankReturn(nbioBankReturnCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			List<Map<String, Object>> filteredSampleReceiving = getNotExistSamplesSampleReceivingCode(
					(List<Map<String, Object>>) inputMap.get("filterSelectedSamples"), userInfo);

			if (filteredSampleReceiving.size() == 0) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYEXISTS",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final int nbioFormAcceptanceCode = Integer.valueOf(inputMap.get("nbioformacceptancecode").toString());

			Map<String, Object> getMap = insertBioBankReturnDetails(nbioFormAcceptanceCode, sbankReturnFormNumber,
					filteredSampleReceiving, nbioBankReturnCode, userInfo);

			final String rtnQry = (String) getMap.get("queryString");
			final String sbioBankReturnDetailsCode = (String) getMap.get("sbioBankReturnDetailsCode");

			String strUpdateStatus = "update biobankreturn set ntransactionstatus="
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			int seqNoBioBankReturnHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biobankreturnhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioBankReturnHistory++;

			String strInsertBankReturnHistory = "insert into biobankreturnhistory (nbiobankreturnhistorycode, nbiobankreturncode,"
					+ " ntransactionstatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) values ("
					+ seqNoBioBankReturnHistory + ", " + nbioBankReturnCode + ", "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			strInsertBankReturnHistory += "update seqnobiobankmanagement set nsequenceno=" + seqNoBioBankReturnHistory
					+ " where stablename='biobankreturnhistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(rtnQry + strUpdateStatus + strInsertBankReturnHistory);

// ===== COC: START =====
//			if (sbioBankReturnDetailsCode != null && !sbioBankReturnDetailsCode.trim().isEmpty()) {
//				String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery1);
//
//				String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery2);
//
//				String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery3);
//
//				int chainCustodyPk = jdbcTemplate.queryForObject(
//						"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//						Integer.class);
//
//				String strChainCustody = "insert into chaincustody ("
//						+ " nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
//						+ " ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
//						+ " noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + chainCustodyPk
//						+ " + rank() over(order by brd.nbiobankreturndetailscode), " + userInfo.getNformcode() + ", "
//						+ " brd.nbiobankreturndetailscode, 'nbiobankreturndetailscode', "
//						+ " 'biobankreturndetails', COALESCE(br.sbankreturnformnumber, ''), "
//						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", " + userInfo.getNusercode()
//						+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
//						+ "', " + userInfo.getNtimezonecode() + ", "
//						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " ('"
//						+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//								.replace("'", "''")
//						+ " [' || COALESCE(brd.srepositoryid,'') || '] ' || '"
//						+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//								.replace("'", "''")
//						+ " [' || COALESCE(brd.nbioparentsamplecode::text,'') || '] ' || '"
//						+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//								.replace("'", "''")
//						+ " [' || COALESCE(br.sbankreturnformnumber,'') || ']'), " + userInfo.getNtranssitecode() + ", "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from biobankreturndetails brd"
//						+ " join biobankreturn br on br.nbiobankreturncode = brd.nbiobankreturncode"
//						+ " and br.nsitecode = " + userInfo.getNtranssitecode() + " and br.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where brd.nsitecode = "
//						+ userInfo.getNtranssitecode() + " and brd.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and brd.nbiobankreturndetailscode in (" + sbioBankReturnDetailsCode + ");";
//
//				String strSeqUpdate = "update seqnoregistration set nsequenceno = nsequenceno + ("
//						+ " select count(*) from biobankreturndetails brd "
//						+ " where brd.nbiobankreturndetailscode in (" + sbioBankReturnDetailsCode + ")"
//						+ " and brd.nsitecode = " + userInfo.getNtranssitecode() + " and brd.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ ") where stablename = 'chaincustody' and nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//				jdbcTemplate.execute(strChainCustody);
//				jdbcTemplate.execute(strSeqUpdate);
//			}
			// ===== COC: END =====

			final List<Object> lstAuditAfter = new ArrayList<>();
			final List<String> multilingualIDList = new ArrayList<>();
			final String strAuditQry = auditQuery(nbioBankReturnCode, sbioBankReturnDetailsCode, userInfo);
			List<BioBankReturn> lstAuditReturnDetailsAfter = jdbcTemplate.query(strAuditQry, new BioBankReturn());
			lstAuditAfter.addAll(lstAuditReturnDetailsAfter);
			lstAuditReturnDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_ADDRETURNSAMPLES"));
			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);
			return getActiveBioBankReturn(nbioBankReturnCode, saveType, userInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> acceptRejectBankReturnSlide(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		int nbioBankReturnCode = Integer.valueOf(inputMap.get("nbiobankreturncode").toString());
		List<Map<String, Object>> addedChildBioBankReturn = (List<Map<String, Object>>) inputMap
				.get("addedChildBioBankReturn");
		int findStatus = findStatusBankReturn(nbioBankReturnCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			if (addedChildBioBankReturn.size() > 0) {
				final String sbioBankReturnDetailsCode = addedChildBioBankReturn.stream()
						.map(x -> String.valueOf(x.get("nbiobankreturndetailscode"))).collect(Collectors.joining(","));
				List<Integer> lstSamples = getNonDeletedSamples(sbioBankReturnDetailsCode, userInfo);
				if (lstSamples.size() == 0) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYDELETED",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

				final String concatString = " and nsamplestatus != "
						+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + "";

				final String validatedBankReturnDetailsCode = findStatusBankReturnDetails(nbioBankReturnCode,
						sbioBankReturnDetailsCode, concatString, userInfo);

				if (validatedBankReturnDetailsCode == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTSAMPLES",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

				outputMap.putAll((Map<String, Object>) getSampleConditionStatus(userInfo).getBody());
				outputMap.putAll((Map<String, Object>) getReason(userInfo).getBody());
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTSAMPLESTOVALIDATE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getSampleConditionStatus(final UserInfo userInfo) throws Exception {
		Map<String, Object> outputMap = new HashMap<>();

		final String strSampleConditionStatus = "select ts.ntranscode nsamplecondition,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') ssamplecondition from"
				+ " approvalstatusconfig ascf, transactionstatus ts where ascf.ntranscode=ts.ntranscode"
				+ " and ascf.nstatusfunctioncode="
				+ Enumeration.ApprovalStatusFunction.VALIDATIONSTATUS.getNstatustype() + " and ascf.nformcode="
				+ userInfo.getNformcode() + " and ascf.nsitecode=" + userInfo.getNmastersitecode()
				+ " and ascf.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by ascf.nsorter";
		List<Map<String, Object>> lstGetSampleCondition = jdbcTemplate.queryForList(strSampleConditionStatus);
		List<Map<String, Object>> lstSampleCondition = new ArrayList<>();

		lstGetSampleCondition.stream().forEach(lst -> {
			Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("ssamplecondition"));
			mapStatus.put("value", lst.get("nsamplecondition"));
			mapStatus.put("item", lst);
			lstSampleCondition.add(mapStatus);
		});

		outputMap.put("lstSampleCondition", lstSampleCondition);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getReason(final UserInfo userInfo) throws Exception {
		Map<String, Object> outputMap = new HashMap<>();

		final String strReason = "select nreasoncode, sreason from reason where nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nreasoncode";
		List<Map<String, Object>> lstGetReason = jdbcTemplate.queryForList(strReason);
		List<Map<String, Object>> lstReason = new ArrayList<>();

		lstGetReason.stream().forEach(lst -> {
			Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("sreason"));
			mapStatus.put("value", lst.get("nreasoncode"));
			mapStatus.put("item", lst);
			lstReason.add(mapStatus);
		});

		outputMap.put("lstReason", lstReason);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> updateSampleCondition(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> outputMap = new HashMap<>();

		jdbcTemplate
				.execute("lock table lockbiobankreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final int nbioBankReturnCode = (int) inputMap.get("nbiobankreturncode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final int findStatus = findStatusBankReturn(nbioBankReturnCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			final String nbioBankReturnDetailsCode = (String) inputMap.get("nbiobankreturndetailscode");
			final int nreasonCode = (int) inputMap.get("nreasoncode");
			final int nsampleCondition = (int) inputMap.get("nsamplecondition");

			List<Integer> lstSamples = getNonDeletedSamples(nbioBankReturnDetailsCode, userInfo);
			if (lstSamples.size() == 0) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final String concatString = " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + "";

			final String validatedBankReturnDetailsCode = findStatusBankReturnDetails(nbioBankReturnCode,
					nbioBankReturnDetailsCode, concatString, userInfo);

			if (validatedBankReturnDetailsCode == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTSAMPLES",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final String concatQry = " and bbrd.nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus();
			final String strAuditAfterQry = childAuditQuery(nbioBankReturnCode, nbioBankReturnDetailsCode, concatQry,
					userInfo);
			List<BioBankReturnDetails> lstAuditReturnDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
					new BioBankReturnDetails());

			final String strUpdateQry = "update biobankreturndetails set nreasoncode=" + nreasonCode
					+ ", nsamplecondition=" + nsampleCondition + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiobankreturndetailscode in (" + nbioBankReturnDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ";";

			int seqNoBioBankReturnDetailsHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biobankreturndetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			String strInsertBankReturnDetailsHistory = "insert into biobankreturndetailshistory"
					+ " (nbiobankreturndetailshistorycode, nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition,"
					+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) select "
					+ seqNoBioBankReturnDetailsHistory + "+ rank() over(order by nbiobankreturndetailscode)"
					+ ", nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", nsitecode, "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from biobankreturndetails where"
					+ " nbiobankreturndetailscode in (" + nbioBankReturnDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus()
					+ " order by nbiobankreturndetailscode;";

			strInsertBankReturnDetailsHistory += "update seqnobiobankmanagement set nsequenceno=(select "
					+ seqNoBioBankReturnDetailsHistory + "+ count(nbiobankreturndetailscode) from"
					+ " biobankreturndetailshistory where nbiobankreturndetailscode in (" + nbioBankReturnDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") where"
					+ " stablename='biobankreturndetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strUpdateQry + strInsertBankReturnDetailsHistory);
			// ===== COC: START =====
//			if (nbioBankReturnDetailsCode != null && !String.valueOf(nbioBankReturnDetailsCode).trim().isEmpty()) {
//				String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery1);
//
//				String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery2);
//
//				String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery3);
//
//				int chainCustodyPk = jdbcTemplate.queryForObject(
//						"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//						Integer.class);
//
//				String strChainCustody = "insert into chaincustody ("
//						+ "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
//						+ "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
//						+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + chainCustodyPk
//						+ " + rank() over(order by brd.nbiobankreturndetailscode), " + userInfo.getNformcode() + ", "
//						+ " brd.nbiobankreturndetailscode, 'nbiobankreturndetailscode', "
//						+ " 'biobankreturndetails', COALESCE(br.sbankreturnformnumber, ''), " + nsampleCondition + ", "
//						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
//						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
//						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " ('"
//						+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//								.replace("'", "''")
//						+ " [' || COALESCE(brd.srepositoryid,'') || '] ' || '"
//						+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//								.replace("'", "''")
//						+ " [' || COALESCE(brd.nbioparentsamplecode::text,'') || '] ' || '"
//						+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//								.replace("'", "''")
//						+ " [' || COALESCE(br.sbankreturnformnumber,'') || ']'), " + userInfo.getNtranssitecode() + ", "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from biobankreturndetails brd"
//						+ " left join biobankreturn br on br.nbiobankreturncode = brd.nbiobankreturncode"
//						+ " and br.nsitecode = " + userInfo.getNtranssitecode() + " and br.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where brd.nsitecode = "
//						+ userInfo.getNtranssitecode() + " and brd.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and brd.nbiobankreturndetailscode in (" + nbioBankReturnDetailsCode + ")"
//						+ " and brd.nsamplestatus != " + Enumeration.TransactionStatus.STORED.gettransactionstatus()
//						+ ";";
//
//				String strSeqUpdate = "update seqnoregistration set nsequenceno = nsequenceno + ("
//						+ " select count(*) from biobankreturndetails brd" + " where brd.nbiobankreturndetailscode in ("
//						+ nbioBankReturnDetailsCode + ")" + " and brd.nsitecode = " + userInfo.getNtranssitecode()
//						+ " and brd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and brd.nsamplestatus != " + Enumeration.TransactionStatus.STORED.gettransactionstatus()
//						+ ") where stablename = 'chaincustody' and nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//				jdbcTemplate.execute(strChainCustody);
//				jdbcTemplate.execute(strSeqUpdate);
//			}
			// ===== COC: END =====

			List<BioBankReturnDetails> lstAuditReturnDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
					new BioBankReturnDetails());
			lstAuditBefore.addAll(lstAuditReturnDetailsBefore);
			lstAuditAfter.addAll(lstAuditReturnDetailsAfter);
			lstAuditReturnDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_SAMPLECONDITION"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioBankReturn = getChildInitialGet(nbioBankReturnCode, userInfo);
			outputMap.put("lstChildBioBankReturn", lstChildBioBankReturn);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> deleteChildBankReturn(final int nbioBankReturnCode,
			final String nbioBankReturnDetailsCode, final UserInfo userInfo) throws Exception {

		jdbcTemplate
				.execute("lock table lockbiobankreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();

		final int findStatus = findStatusBankReturn(nbioBankReturnCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			List<Integer> lstSamples = getNonDeletedSamples(nbioBankReturnDetailsCode, userInfo);
			if (lstSamples.size() == 0) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final String concatString = " and nsamplestatus in ("
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ")";

			final String validatedBankReturnDetailsCode = findStatusBankReturnDetails(nbioBankReturnCode,
					nbioBankReturnDetailsCode, concatString, userInfo);

			if (validatedBankReturnDetailsCode == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDSAMPLESTODELETE",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}

			final String concatQry = " and bbrd.nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus();
			final String strAuditBeforeQry = childAuditQuery(nbioBankReturnCode, nbioBankReturnDetailsCode, concatQry,
					userInfo);
			List<BioBankReturnDetails> lstAuditReturnDetailsBefore = jdbcTemplate.query(strAuditBeforeQry,
					new BioBankReturnDetails());

			final String strDeleteQry = "update biobankreturndetails set nstatus="
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiobankreturndetailscode in (" + nbioBankReturnDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ";";

			int seqNoBioBankReturnDetailsHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biobankreturndetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strInsertBankReturnDetailsHistory = "insert into biobankreturndetailshistory"
					+ " (nbiobankreturndetailshistorycode, nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition,"
					+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) select "
					+ seqNoBioBankReturnDetailsHistory + "+ rank() over(order by nbiobankreturndetailscode)"
					+ ", nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", nsitecode, "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from biobankreturndetails where"
					+ " nbiobankreturndetailscode in (" + nbioBankReturnDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by nbiobankreturndetailscode;";

			strInsertBankReturnDetailsHistory += "update seqnobiobankmanagement set nsequenceno=(select "
					+ seqNoBioBankReturnDetailsHistory + "+ count(nbiobankreturndetailscode) from"
					+ " biobankreturndetailshistory where nbiobankreturndetailscode in (" + nbioBankReturnDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") where"
					+ " stablename='biobankreturndetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			// ===== COC: START =====
//			if (nbioBankReturnDetailsCode != null && !nbioBankReturnDetailsCode.trim().isEmpty()) {
//				String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery1);
//
//				String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery2);
//
//				String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery3);
//
//				int chainCustodyPk = jdbcTemplate.queryForObject(
//						"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//						Integer.class);
//
//				String strChainCustody = "insert into chaincustody ("
//						+ " nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
//						+ " ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
//						+ " noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + chainCustodyPk
//						+ " + rank() over(order by brd.nbiobankreturndetailscode), " + userInfo.getNformcode() + ", "
//						+ " brd.nbiobankreturndetailscode, 'nbiobankreturndetailscode', 'biobankreturndetails',"
//						+ " COALESCE(br.sbankreturnformnumber, ''), "
//						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", " + userInfo.getNusercode()
//						+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
//						+ "', " + userInfo.getNtimezonecode() + ", "
//						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " ('"
//						+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//								.replace("'", "''")
//						+ " [' || COALESCE(brd.srepositoryid,'') || '] ' || '"
//						+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//								.replace("'", "''")
//						+ " [' || COALESCE(brd.nbioparentsamplecode::text,'') || '] ' || '"
//						+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//								.replace("'", "''")
//						+ " [' || COALESCE(br.sbankreturnformnumber,'') || ']'), " + userInfo.getNtranssitecode() + ", "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from biobankreturndetails brd"
//						+ " join biobankreturn br on br.nbiobankreturncode = brd.nbiobankreturncode"
//						+ " and br.nsitecode = " + userInfo.getNtranssitecode() + " and br.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where brd.nsitecode = "
//						+ userInfo.getNtranssitecode() + " and brd.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and brd.nsamplestatus != "
//						+ Enumeration.TransactionStatus.STORED.gettransactionstatus()
//						+ " and brd.nbiobankreturndetailscode in (" + nbioBankReturnDetailsCode + ");";
//
//				String strSeqUpdate = "update seqnoregistration set nsequenceno = nsequenceno + ("
//						+ " select count(*) from biobankreturndetails brd" + " where brd.nbiobankreturndetailscode in ("
//						+ nbioBankReturnDetailsCode + ")" + " and brd.nsitecode = " + userInfo.getNtranssitecode()
//						+ " and brd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and brd.nsamplestatus != " + Enumeration.TransactionStatus.STORED.gettransactionstatus()
//						+ ") where stablename = 'chaincustody' and nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//				jdbcTemplate.execute(strChainCustody);
//				jdbcTemplate.execute(strSeqUpdate);
//			}
			// ===== COC: END =====

			jdbcTemplate.execute(strDeleteQry + strInsertBankReturnDetailsHistory);

			lstAuditBefore.addAll(lstAuditReturnDetailsBefore);
			lstAuditReturnDetailsBefore.stream().forEach(x -> multilingualIDList.add("IDS_DELETERETURNSAMPLES"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditBefore, 1, null, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioBankReturn = getChildInitialGet(nbioBankReturnCode, userInfo);
			outputMap.put("lstChildBioBankReturn", lstChildBioBankReturn);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> disposeSamples(final int nbioBankReturnCode, final String nbioBankReturnDetailsCode,
			final UserInfo userInfo) throws Exception {

		jdbcTemplate
				.execute("lock table lockbiobankreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final int findStatus = findStatusBankReturn(nbioBankReturnCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {
			final String strFindChildStatus = "select exists (select nbiobankreturndetailscode from biobankreturndetails where nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus()
					+ " and nbiobankreturndetailscode in " + "(" + nbioBankReturnDetailsCode + ")" + ")";
			boolean findChildStatus = jdbcTemplate.queryForObject(strFindChildStatus, boolean.class);
			if (findChildStatus) {

				List<Integer> lstSamples = getNonDeletedSamples(nbioBankReturnDetailsCode, userInfo);
				if (lstSamples.size() == 0) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYDELETED",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

				final String concatString = " and nsamplestatus in ("
						+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ")";

				final String validatedBankReturnDetailsCode = findStatusBankReturnDetails(nbioBankReturnCode,
						nbioBankReturnDetailsCode, concatString, userInfo);

				if (validatedBankReturnDetailsCode == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDSAMPLES",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

				final String strDeleteQry = "update biobankreturndetails set nsamplestatus="
						+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
						+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " where nbiobankreturndetailscode in (" + nbioBankReturnDetailsCode + ") and nsitecode="
						+ userInfo.getNtranssitecode() + " and nsamplestatus != "
						+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				int seqNoBioBankReturnDetailsHistory = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnobiobankmanagement where stablename='biobankreturndetailshistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);

				String strInsertBankReturnDetailsHistory = "insert into biobankreturndetailshistory"
						+ " (nbiobankreturndetailshistorycode, nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition,"
						+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
						+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) select "
						+ seqNoBioBankReturnDetailsHistory + "+ rank() over(order by nbiobankreturndetailscode)"
						+ ", nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition, nsamplestatus, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", nsitecode, "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biobankreturndetails where" + " nbiobankreturndetailscode in ("
						+ nbioBankReturnDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by nbiobankreturndetailscode;";

				strInsertBankReturnDetailsHistory += "update seqnobiobankmanagement set nsequenceno=(select "
						+ seqNoBioBankReturnDetailsHistory + "+ count(nbiobankreturndetailscode) from"
						+ " biobankreturndetails where nbiobankreturndetailscode in (" + nbioBankReturnDetailsCode
						+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") where"
						+ " stablename='biobankreturndetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				jdbcTemplate.execute(strDeleteQry + strInsertBankReturnDetailsHistory);

				final String concatQry = " and bbrd.nsamplestatus != "
						+ Enumeration.TransactionStatus.STORED.gettransactionstatus();
				final String strAuditAfterQry = childAuditQuery(nbioBankReturnCode, nbioBankReturnDetailsCode,
						concatQry, userInfo);
				List<BioBankReturnDetails> lstAuditReturnDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
						new BioBankReturnDetails());

// ===== COC: START =====
//				{
//					String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//					jdbcTemplate.execute(sQuery1);
//
//					String sQuery2 = " lock  table lockquarantine "
//							+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//					jdbcTemplate.execute(sQuery2);
//
//					String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//					jdbcTemplate.execute(sQuery3);
//
//					int chainCustodyPk = jdbcTemplate.queryForObject(
//							"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//							Integer.class);
//
//					String strChainCustody = "insert into chaincustody ("
//							+ "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
//							+ "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
//							+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + chainCustodyPk
//							+ " + rank() over(order by brd.nbiobankreturndetailscode), " + userInfo.getNformcode()
//							+ ", " + " brd.nbiobankreturndetailscode, 'nbiobankreturndetailscode', "
//							+ " 'biobankreturndetails', COALESCE(br.sbankreturnformnumber, ''), "
//							+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ", "
//							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
//							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
//							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
//							+ " ('"
//							+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//									.replace("'", "''")
//							+ " [' || COALESCE(brd.srepositoryid,'') || '] ' || '"
//							+ commonFunction
//									.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//									.replace("'", "''")
//							+ " [' || COALESCE(brd.nbioparentsamplecode::text,'') || '] ' || '"
//							+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//									.replace("'", "''")
//							+ " [' || COALESCE(br.sbankreturnformnumber,'') || ']'), " + userInfo.getNtranssitecode()
//							+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " from biobankreturndetails brd"
//							+ " join biobankreturn br on br.nbiobankreturncode = brd.nbiobankreturncode"
//							+ " and br.nsitecode = " + userInfo.getNtranssitecode() + " and br.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where brd.nsitecode = "
//							+ userInfo.getNtranssitecode() + " and brd.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and brd.nbiobankreturndetailscode in (" + nbioBankReturnDetailsCode + ")"
//							+ " and brd.nsamplestatus != " + Enumeration.TransactionStatus.STORED.gettransactionstatus()
//							+ ";";
//
//					String strSeqUpdate = "update seqnoregistration set nsequenceno = nsequenceno + ("
//							+ " select count(*) from biobankreturndetails brd"
//							+ " where brd.nbiobankreturndetailscode in (" + nbioBankReturnDetailsCode + ")"
//							+ " and brd.nsitecode = " + userInfo.getNtranssitecode() + " and brd.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and brd.nsamplestatus != "
//							+ Enumeration.TransactionStatus.STORED.gettransactionstatus()
//							+ ") where stablename = 'chaincustody' and nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//					jdbcTemplate.execute(strChainCustody);
//					jdbcTemplate.execute(strSeqUpdate);
//				}
				// ===== COC: END =====

				lstAuditAfter.addAll(lstAuditReturnDetailsAfter);
				lstAuditReturnDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_DISPOSERETURNSAMPLES"));

				auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);

				List<Map<String, Object>> lstChildBioBankReturn = getChildInitialGet(nbioBankReturnCode, userInfo);
				outputMap.put("lstChildBioBankReturn", lstChildBioBankReturn);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public String auditQuery(final int nbioBankReturnCode, final String sbioBankReturnDetailsCode,
			final UserInfo userInfo) throws Exception {
		String strConcat = "";
		if (!sbioBankReturnDetailsCode.isEmpty() && sbioBankReturnDetailsCode != "") {
			strConcat = " and bbrd.nbiobankreturndetailscode in (" + sbioBankReturnDetailsCode + ")";
		}
		final String strAuditQry = "select bbr.nbiobankreturncode, bbr.sbankreturnformnumber, bbr.nformtypecode,"
				+ " bbr.noriginsitecode, bbr.jsondata->>'soriginsitename' soriginsitename, to_char(bbr.dreturndate, '"
				+ userInfo.getSsitedate()
				+ "') sreturndate, bbr.ntransactionstatus, bbr.jsondata->>'sremarks' sremarks, bbr.nstorageconditioncode,"
				+ " to_char(bbr.ddeliverydate, '" + userInfo.getSsitedate() + "') sdeliverydate, bbr.ndispatchercode,"
				+ " bbr.jsondata->>'sdispatchername' sdispatchername, bbr.ncouriercode, bbr.jsondata->>'scourierno' scourierno,"
				+ " bbr.jsondata->>'striplepackage' striplepackage, bbr.jsondata->>'svalidationremarks' svalidationremarks,"
				+ " bbrd.srepositoryid, bbrd.nproductcode, bbrd.svolume, bbrd.sreturnvolume, bbrd.nsamplecondition, bbrd.nsamplestatus"
				+ " from biobankreturn bbr join biobankreturndetails bbrd on bbrd.nbiobankreturncode=bbr.nbiobankreturncode and"
				+ " bbrd.nsitecode=" + userInfo.getNtranssitecode() + " and bbrd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bbr.nsitecode="
				+ userInfo.getNtranssitecode() + " and bbr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbr.nbiobankreturncode="
				+ nbioBankReturnCode + strConcat + " order by bbr.nbiobankreturncode;";
		return strAuditQry;
	}

	public String auditParentQuery(final int nbioBankReturnCode, final String concatSelect, final String concatJoin,
			final UserInfo userInfo) throws Exception {
		final String strAuditQry = "select bbr.nbiobankreturncode, bbr.sbankreturnformnumber, bbr.noriginsitecode,"
				+ " to_char(bbr.dreturndate, '" + userInfo.getSsitedate() + "') sreturndate, bbr.ntransactionstatus"
				+ concatSelect + " from biobankreturn bbr " + concatJoin + " where bbr.nsitecode="
				+ userInfo.getNtranssitecode() + " and bbr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbr.nbiobankreturncode="
				+ nbioBankReturnCode;
		return strAuditQry;
	}

	public String childAuditQuery(final int nbioBankReturnCode, final String nbioBankReturnDetailsCode,
			final String concatQry, final UserInfo userInfo) throws Exception {
		final String strChildAuditQuery = "select bbrd.nbiobankreturndetailscode, bbrd.nbiobankreturncode,"
				+ " bbr.sbankreturnformnumber, bbrd.srepositoryid, bbrd.nproductcode, bbrd.svolume, bbrd.sreturnvolume,"
				+ " bbrd.nsamplecondition, bbrd.nsamplestatus, bbrd.nreasoncode from biobankreturndetails bbrd join biobankreturn bbr"
				+ " on bbr.nbiobankreturncode=bbrd.nbiobankreturncode and bbr.nsitecode=" + userInfo.getNtranssitecode()
				+ " and bbr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " where bbrd.nsitecode=" + userInfo.getNtranssitecode() + " and bbrd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbrd.nbiobankreturncode="
				+ nbioBankReturnCode + " and bbrd.nbiobankreturndetailscode in (" + nbioBankReturnDetailsCode + ") "
				+ concatQry + " order by bbrd.nbiobankreturndetailscode desc;";
		return strChildAuditQuery;
	}

	public List<Integer> getNonDeletedSamples(final String sbioBankReturnDetailsCode, UserInfo userInfo)
			throws Exception {

		final String strSamples = "select nbiobankreturndetailscode from biobankreturndetails where nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nbiobankreturndetailscode in ("
				+ sbioBankReturnDetailsCode + ") and nsamplestatus != "
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus();
		List<Integer> lstSamplesCode = jdbcTemplate.queryForList(strSamples, Integer.class);
		return lstSamplesCode;
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getStorageFreezerData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final String sQueryFreezerId = "select ssv.jsondata, si.nsamplestoragelocationcode,si.nsamplestorageversioncode,si.nstorageinstrumentcode, "
				+ "(i.sinstrumentid ||' (' ||(SUM(ssm.nnoofcontainer) - (SELECT Count(st.sposition) positions "
				+ "FROM   samplestoragetransaction st,samplestoragemapping ssm1 WHERE "
				+ "ssl.nsamplestoragelocationcode = st.nsamplestoragelocationcode "
				+ "AND st.nsamplestoragemappingcode = ssm1.nsamplestoragemappingcode "
				+ "AND ssm1.nstorageinstrumentcode = ssm.nstorageinstrumentcode " + "AND st.nstatus = 1 "
				+ "AND st.spositionvalue :: text <> '' :: text)) || ')') sinstrumentid," + " i.ninstrumentcode, "
				+ "sum(ssm.nnoofcontainer) - " + "(SELECT Count(st.sposition) positions "
				+ "FROM   samplestoragetransaction st,samplestoragemapping ssm1 " + "WHERE "
				+ "ssl.nsamplestoragelocationcode = st.nsamplestoragelocationcode "
				+ "AND st.nsamplestoragemappingcode = ssm1.nsamplestoragemappingcode "
				+ "AND ssm1.nstorageinstrumentcode = ssm.nstorageinstrumentcode " + "AND st.nstatus = 1 "
				+ "AND st.spositionvalue :: text <> '' :: text) navailablespace " + "from " + "storageinstrument si "
				+ "JOIN samplestoragelocation ssl ON ssl.nsamplestoragelocationcode = si.nsamplestoragelocationcode  "
				+ "and si.nregionalsitecode = " + userInfo.getNtranssitecode() + " " + "and si.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN samplestorageversion ssv ON ssl.nsamplestoragelocationcode = ssv.nsamplestoragelocationcode "
				+ "and ssv.nsamplestorageversioncode = si.nsamplestorageversioncode and ssv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN samplestoragecontainerpath ssc ON ssc.nsamplestoragelocationcode = ssv.nsamplestoragelocationcode "
				+ "and ssc.nsamplestorageversioncode = ssv.nsamplestorageversioncode and ssc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN samplestoragemapping ssm ON ssm.nsamplestoragecontainerpathcode = ssc.nsamplestoragecontainerpathcode "
				+ "and ssv.nsamplestorageversioncode = si.nsamplestorageversioncode and ssm.nstorageinstrumentcode = si.nstorageinstrumentcode "
				+ "and ssm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN instrument i ON si.ninstrumentcode = i.ninstrumentcode and i.ninstrumentstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and i.ninstrumentstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join instrumentcalibration ic "
				+ " on ic.ninstrumentcode=i.ninstrumentcode and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.ncalibrationstatus="
				+ Enumeration.TransactionStatus.CALIBIRATION.gettransactionstatus() + " and ic.nsitecode="
				+ userInfo.getNtranssitecode() + " and ninstrumentcalibrationcode in (select"
				+ " max(ninstrumentcalibrationcode) from instrumentcalibration where nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by ninstrumentcode) join instrumentmaintenance im on im.ninstrumentcode=i.ninstrumentcode"
				+ " and im.nsitecode=" + userInfo.getNtranssitecode() + " and  im.nmaintenancestatus="
				+ Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus() + " and im.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and im.ninstrumentmaintenancecode in"
				+ " (select max(ninstrumentmaintenancecode) from instrumentmaintenance where nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by ninstrumentcode) "
				+ "group by ssv.jsondata, si.nsamplestoragelocationcode,si.nsamplestorageversioncode,si.nstorageinstrumentcode, "
				+ "i.sinstrumentid, i.ninstrumentcode,ssl.nsamplestoragelocationcode, ssm.nstorageinstrumentcode; ";

		final List<StorageInstrument> freezerList = jdbcTemplate.query(sQueryFreezerId, new StorageInstrument());

		if (freezerList.size() > 0) {
			outputMap.put("selectedSuggestedStorage", freezerList.get(0));
			outputMap.put("selectedFreezerData", freezerList.get(0));
			inputMap.put("nsamplestorageversioncode", freezerList.get(0).getNsamplestorageversioncode());
			inputMap.put("nstorageinstrumentcode", freezerList.get(0).getNstorageinstrumentcode());

			outputMap.put("freezerList", freezerList);
			outputMap.putAll((Map<String, Object>) getStorageStructure(inputMap, userInfo).getBody());
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_FREEZERNOTAVAILABLETOSTORE",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> getStorageStructure(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final int nsamplestorageversioncode = Integer.valueOf(inputMap.get("nsamplestorageversioncode").toString());
		final int nstorageinstrumentcode = Integer.valueOf(inputMap.get("nstorageinstrumentcode").toString());

		final String sQuery = "SELECT enrich_jsondata_by_version(" + nsamplestorageversioncode + ","
				+ nstorageinstrumentcode + ") AS jsondata; ";

		final StorageInstrument objSelectedStrcuture = (StorageInstrument) jdbcUtilityTemplateFunction
				.queryForObject(sQuery, StorageInstrument.class, jdbcTemplate);

		outputMap.put("selectedSampleStorageVersion", objSelectedStrcuture);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@SuppressWarnings({ "static-access", "unchecked" })
	public ResponseEntity<Object> storeSamples(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		jdbcTemplate.execute(
				"lock table locksamplestoragetransaction " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());

		final int nbioBankReturnCode = (int) inputMap.get("nbiobankreturncode");
		final String nbioBankReturnDetailsCode = (String) inputMap.get("nbiobankreturndetailscode");

		final String concatString = " and nsamplestatus in ("
				+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ") and nsamplecondition="
				+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus();

		final String validatedBankReturnDetailsCode = findStatusBankReturnDetails(nbioBankReturnCode,
				nbioBankReturnDetailsCode, concatString, userInfo);

		if (validatedBankReturnDetailsCode != null) {

			final int nstorageinstrumentcode = Integer.valueOf(inputMap.get("nstorageinstrumentcode").toString());

			final String storageLocQry = "select nsamplestoragelocationcode, nsamplestorageversioncode from storageinstrument "
					+ "where nstorageinstrumentcode = " + nstorageinstrumentcode + " " + "and nsitecode = "
					+ userInfo.getNmastersitecode() + " " + "and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final StorageInstrument objSelectedStructure = (StorageInstrument) jdbcUtilityTemplateFunction
					.queryForObject(storageLocQry, StorageInstrument.class, jdbcTemplate);
			final int nsamplestoragelocationcode = objSelectedStructure.getNsamplestoragelocationcode();
			final int nsamplestorageversioncode = objSelectedStructure.getNsamplestorageversioncode();

			// modified by sujatha ATE_274 bgsi-218 by selecting 4 extra keys in the
			// jsondata from the biobankreturndetails to store in movetodisposal's jsondata
			final String sampleRecQry = "select bbrd.nbiobankreturndetailscode, bbrd.nbiosamplereceivingcode,"
					+ " bbrd.srepositoryid, bbrd.svolume, bbrd.sreturnvolume, bbrd.nproductcode, p.sproductname, pc.sproductcatname,"
					+ " bbrd.nbioprojectcode, bp.sprojecttitle, bbrd.nbioparentsamplecode,"
					+ " bbrd.jsondata->>'sparentsamplecode' sparentsamplecode, bbrd.ncohortno, bbrd.nproductcatcode,"
					+ " bbrd.slocationcode, bbrd.jsondata->>'ssubjectid' ssubjectid, bbrd.jsondata->>'scasetype' scasetype, bbrd.ndiagnostictypecode,"
					+ " bbrd.ncontainertypecode, bbrd.nstoragetypecode, "
					+ " NULLIF(bbrd.jsondata->>'sextractedsampleid', '') AS sextractedsampleid, "
					+ " NULLIF(bbrd.jsondata->>'sconcentration', '') AS sconcentration, "
					+ " NULLIF(bbrd.jsondata->>'sqcplatform', '') AS sqcplatform, "
					+ " NULLIF(bbrd.jsondata->>'seluent', '') AS seluent " + " from biobankreturndetails bbrd"
					+ " join bioproject bp on bp.nbioprojectcode=bbrd.nbioprojectcode and bp.nsitecode="
					+ userInfo.getNmastersitecode() + " and bp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " join product p on p.nproductcode=bbrd.nproductcode and p.nsitecode="
					+ userInfo.getNmastersitecode() + " and p.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " join productcategory pc on pc.nproductcatcode=p.nproductcatcode and pc.nsitecode="
					+ userInfo.getNmastersitecode() + " and pc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bbrd.nsitecode="
					+ userInfo.getNtranssitecode() + " and bbrd.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and bbrd.nbiobankreturndetailscode in (" + validatedBankReturnDetailsCode
					+ ") order by bbrd.nbiobankreturndetailscode";

			List<BioBankReturnDetails> lstBioBankReturnDetails = jdbcTemplate.query(sampleRecQry,
					new BioBankReturnDetails());

			if (!lstBioBankReturnDetails.isEmpty()) {
				final int selectedCount = lstBioBankReturnDetails.size();
				final String selectedNodeID = inputMap.get("selectedNodeID").toString();

				final String sQuery = "SELECT enrich_jsondata_by_version(" + nsamplestorageversioncode + ","
						+ nstorageinstrumentcode + ") AS jsondata; ";

				final StorageInstrument objSelectedStrcuture = (StorageInstrument) jdbcUtilityTemplateFunction
						.queryForObject(sQuery, StorageInstrument.class, jdbcTemplate);
				final Map<String, Object> objJsonData = objSelectedStrcuture.getJsondata();
				Object dataListObj = objJsonData.get("data");

				List<Map<String, Object>> nodes = (List<Map<String, Object>>) dataListObj;
				List<Map<String, String>> lastNodes = processedSampleReceivingDAOImpl.findContainerLastNodes(nodes,
						selectedNodeID);

				int nseqSampleStorageTransaction = (int) jdbcTemplate.queryForObject(
						"select nsequenceno from seqnobasemaster where stablename='samplestoragetransaction' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);

				String strSampleStorageTransactionInsert = "";
				String updateSeqNoQry = "";
				String strUpdateBioBankReturnDetails = "";
				String instsamplestorageadditionalinfo = "";

				int totalCodes = selectedCount;

				int codeIndex = 0;
				for (Map<String, String> node : lastNodes) {

					String getQry = "select nsamplestoragecontainerpathcode, scontainerlastnode from samplestoragecontainerpath "
							+ "where suid='" + node.get("id") + "' and nsamplestoragelocationcode = "
							+ nsamplestoragelocationcode + " " + "and nsamplestorageversioncode = "
							+ nsamplestorageversioncode + " " + "and nsitecode = " + userInfo.getNmastersitecode() + " "
							+ "and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					Map<String, Object> containerresult = jdbcTemplate.queryForMap(getQry);

					int nsamplestoragecontainerpathcode = Integer
							.valueOf(containerresult.get("nsamplestoragecontainerpathcode").toString());
					String scontainerlastnode = containerresult.get("scontainerlastnode").toString();

					getQry = "select nsamplestoragemappingcode, nrow, ncolumn, ndirectionmastercode, ninstrumentcode"
							+ " from samplestoragemapping where nsamplestoragecontainerpathcode= "
							+ nsamplestoragecontainerpathcode + " and nstorageinstrumentcode = "
							+ nstorageinstrumentcode + " and nsitecode = " + userInfo.getNtranssitecode()
							+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					Map<String, Object> mapresult = jdbcTemplate.queryForMap(getQry);

					int nsamplestoragemappingcode = Integer
							.valueOf(mapresult.get("nsamplestoragemappingcode").toString());
					int nrow = Integer.valueOf(mapresult.get("nrow").toString());
					int ncolumn = Integer.valueOf(mapresult.get("ncolumn").toString());
					int ndirectionmastercode = Integer.valueOf(mapresult.get("ndirectionmastercode").toString());
					int ninstrumentCode = Integer.valueOf(mapresult.get("ninstrumentcode").toString());

					List<String> generatedOrder = generateOrder(nrow, ncolumn, ndirectionmastercode);

					String placeholders = generatedOrder.stream().map(s -> "'" + s + "'")
							.collect(Collectors.joining(","));

					String sql = "select sposition from samplestoragetransaction WHERE sposition in (" + placeholders
							+ ") and nsamplestoragemappingcode = " + nsamplestoragemappingcode
							+ " and nsamplestoragelocationcode = " + nsamplestoragelocationcode + " and nsitecode = "
							+ userInfo.getNtranssitecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					List<String> existingPositions = jdbcTemplate.queryForList(sql, String.class);

					List<String> missingPositions = generatedOrder.stream()
							.filter(code -> !existingPositions.contains(code)).collect(Collectors.toList());
					int availableSpace = Integer.parseInt(node.get("savailablespace"));
					for (int i = 0; i < availableSpace && codeIndex < totalCodes; i++) {
						nseqSampleStorageTransaction++;

						String slocationCode = scontainerlastnode + "-" + missingPositions.get(i);

						final String sparentSampleCode = (lstBioBankReturnDetails.get(codeIndex)
								.getSparentsamplecode() != null
								&& lstBioBankReturnDetails.get(codeIndex).getSparentsamplecode() != "")
										? "'" + lstBioBankReturnDetails.get(codeIndex).getSparentsamplecode() + "'"
										: null;
						final String sreturnVolume = (lstBioBankReturnDetails.get(codeIndex).getSreturnvolume() != null
								&& lstBioBankReturnDetails.get(codeIndex).getSreturnvolume() != "")
										? "'" + lstBioBankReturnDetails.get(codeIndex).getSreturnvolume() + "'"
										: null;
						final String ssubjectId = (lstBioBankReturnDetails.get(codeIndex).getSsubjectid() != null
								&& lstBioBankReturnDetails.get(codeIndex).getSsubjectid() != "")
										? "'" + lstBioBankReturnDetails.get(codeIndex).getSsubjectid() + "'"
										: null;
						final String scaseType = (lstBioBankReturnDetails.get(codeIndex).getScasetype() != null
								&& lstBioBankReturnDetails.get(codeIndex).getScasetype() != "")
										? "'" + lstBioBankReturnDetails.get(codeIndex).getScasetype() + "'"
										: null;

						strSampleStorageTransactionInsert += "(" + nseqSampleStorageTransaction + ","
								+ nsamplestoragelocationcode + "," + nsamplestoragemappingcode + ","
								+ lstBioBankReturnDetails.get(codeIndex).getNbioprojectcode() + ",'"
								+ missingPositions.get(i) + "' ,'"
								+ stringUtilityFunction
										.replaceQuote(lstBioBankReturnDetails.get(codeIndex).getSrepositoryid())
								+ "' ," + " json_build_object('Parent Sample Code','"
								+ stringUtilityFunction
										.replaceQuote(lstBioBankReturnDetails.get(codeIndex).getSparentsamplecode())
								+ "'," + "'Case Type','"
								+ stringUtilityFunction
										.replaceQuote(lstBioBankReturnDetails.get(codeIndex).getScasetype())
								+ "'" + ", 'Parent Sample Type','"
								+ stringUtilityFunction
										.replaceQuote(lstBioBankReturnDetails.get(codeIndex).getSproductcatname())
								+ "', 'Project Title','"
								+ stringUtilityFunction
										.replaceQuote(lstBioBankReturnDetails.get(codeIndex).getSprojecttitle())
								+ "', 'Bio Sample Type','"
								+ stringUtilityFunction
										.replaceQuote(lstBioBankReturnDetails.get(codeIndex).getSproductname())
								+ "', 'Volume (μL)','"
								+ stringUtilityFunction
										.replaceQuote(lstBioBankReturnDetails.get(codeIndex).getSreturnvolume())
								+ "')::jsonb, " + Enumeration.TransactionStatus.YES.gettransactionstatus() + ", '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ userInfo.getNtranssitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
								+ lstBioBankReturnDetails.get(codeIndex).getNbiosamplereceivingcode() + ", "
								+ ninstrumentCode + ", "
								+ lstBioBankReturnDetails.get(codeIndex).getNbioparentsamplecode() + ", "
								+ sparentSampleCode + ", " + lstBioBankReturnDetails.get(codeIndex).getNcohortno()
								+ ", " + lstBioBankReturnDetails.get(codeIndex).getNproductcatcode() + ", "
								+ lstBioBankReturnDetails.get(codeIndex).getNproductcode() + ", " + sreturnVolume + ", '"
								+ slocationCode + "', " + ssubjectId + ", " + scaseType + ", "
								+ lstBioBankReturnDetails.get(codeIndex).getNdiagnostictypecode() + ", "
								+ lstBioBankReturnDetails.get(codeIndex).getNcontainertypecode() + ", "
								+ lstBioBankReturnDetails.get(codeIndex).getNstoragetypecode() + "),";

						strUpdateBioBankReturnDetails += "update biobankreturndetails set nsamplestatus="
								+ Enumeration.TransactionStatus.STORED.gettransactionstatus()
								+ ", nsamplestoragetransactioncode=" + nseqSampleStorageTransaction
								+ ", slocationcode='" + slocationCode + "', dtransactiondate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
								+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
								+ " where nbiobankreturndetailscode = "
								+ lstBioBankReturnDetails.get(codeIndex).getNbiobankreturndetailscode()
								+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						// added by sujatha ATE_274 for insert into samplestorageadditionalinfo table
						// while storing samples BGSI-218
						instsamplestorageadditionalinfo += "(" + nseqSampleStorageTransaction + ", "
								+ (lstBioBankReturnDetails.get(codeIndex).getSextractedsampleid() == null
										|| lstBioBankReturnDetails.get(codeIndex).getSextractedsampleid().trim()
												.isEmpty()
														? "null"
														: "'" + stringUtilityFunction
																.replaceQuote(lstBioBankReturnDetails
																		.get(codeIndex).getSextractedsampleid())
																+ "'")
								+ ", "
								+ (lstBioBankReturnDetails.get(codeIndex).getSconcentration() == null
										|| lstBioBankReturnDetails.get(codeIndex).getSconcentration().trim().isEmpty()
												? "null"
												: "'" + stringUtilityFunction.replaceQuote(
														lstBioBankReturnDetails.get(codeIndex).getSconcentration())
														+ "'")
								+ ", "
								+ (lstBioBankReturnDetails.get(codeIndex).getSqcplatform() == null
										|| lstBioBankReturnDetails.get(codeIndex).getSqcplatform().trim().isEmpty()
												? "null"
												: "'" + stringUtilityFunction.replaceQuote(
														lstBioBankReturnDetails.get(codeIndex).getSqcplatform()) + "'")
								+ ", "
								+ (lstBioBankReturnDetails.get(codeIndex).getSeluent() == null
										|| lstBioBankReturnDetails.get(codeIndex).getSeluent().trim().isEmpty()
												? "null"
												: "'" + stringUtilityFunction.replaceQuote(
														lstBioBankReturnDetails.get(codeIndex).getSeluent()) + "'")
								+ ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ userInfo.getNtranssitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

						codeIndex++;
					}
					if (codeIndex >= totalCodes) {
						break;
					}
				}

				strSampleStorageTransactionInsert = " INSERT INTO public.samplestoragetransaction("
						+ " nsamplestoragetransactioncode, nsamplestoragelocationcode, nsamplestoragemappingcode,"
						+ " nprojecttypecode, sposition, spositionvalue, jsondata, npositionfilled, dmodifieddate,"
						+ " nsitecode, nstatus, nbiosamplereceivingcode, ninstrumentcode, nbioparentsamplecode, sparentsamplecode, "
						+ " ncohortno, nproductcatcode, nproductcode, sqty, slocationcode, ssubjectid, scasetype, ndiagnostictypecode, "
						+ " ncontainertypecode, nstoragetypecode) VALUES "
						+ strSampleStorageTransactionInsert.substring(0, strSampleStorageTransactionInsert.length() - 1)
						+ "; ";

				// added by sujatha ATE_274 BGSI-218 for inserting into
				// samplestorageadditionalinfo while storing the samples
				instsamplestorageadditionalinfo = " INSERT INTO public.samplestorageadditionalinfo("
						+ " nsamplestoragetransactioncode, sextractedsampleid, sconcentration, sqcplatform, seluent, dmodifieddate,"
						+ " nsitecode, nstatus)" + " VALUES"
						+ instsamplestorageadditionalinfo.substring(0, instsamplestorageadditionalinfo.length() - 1)
						+ ";";

				updateSeqNoQry = updateSeqNoQry + " update seqnobasemaster set nsequenceno = "
						+ nseqSampleStorageTransaction + " where stablename = 'samplestoragetransaction' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

				int bankReturnHistoryDetailsPk = jdbcTemplate
						.queryForObject("select nsequenceno from seqnobiobankmanagement where "
								+ "stablename='biobankreturndetailshistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);

				String sbankReturnDetailsCode = lstBioBankReturnDetails.stream()
						.map(x -> String.valueOf(x.getNbiobankreturndetailscode())).collect(Collectors.joining(","));
				String strUpdateQry = "insert into biobankreturndetailshistory"
						+ " (nbiobankreturndetailshistorycode, nbiobankreturndetailscode, nbiobankreturncode,"
						+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
						+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
						+ "select " + bankReturnHistoryDetailsPk + "+rank()over(order by nbiobankreturndetailscode),"
						+ " nbioformacceptancedetailscode, nbioformacceptancecode, nsamplecondition, nsamplestatus, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biobankreturndetails where nbiobankreturndetailscode in (" + sbankReturnDetailsCode
						+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by nbiobankreturndetailscode;";
				strUpdateQry += " update seqnobiobankmanagement set nsequenceno=(" + "select "
						+ bankReturnHistoryDetailsPk
						+ " + count(nbiobankreturndetailscode) from biobankreturndetails where"
						+ " nbiobankreturndetailscode in (" + sbankReturnDetailsCode + ") and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
						+ " where stablename='biobankreturndetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				final String strAuditAfterQry = childAuditQuery(nbioBankReturnCode, sbankReturnDetailsCode, "",
						userInfo);
				List<BioBankReturnDetails> lstAuditReturnDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
						new BioBankReturnDetails());

				// modified by sujatha ATE_274 by adding instsamplestorageadditionalinfo to
				// execute
				jdbcTemplate.execute(strSampleStorageTransactionInsert + instsamplestorageadditionalinfo
						+ updateSeqNoQry + strUpdateBioBankReturnDetails + strUpdateQry);

				// ===== COC: START =====
				if (sbankReturnDetailsCode != null && !sbankReturnDetailsCode.trim().isEmpty()) {
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

					 String strChainCustody = "insert into chaincustody ("
					            + " nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
					            + " ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
					            + " noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + chainCustodyPk
					            + " + rank() over(order by brd.nbiobankreturndetailscode), " + userInfo.getNformcode()
					            + ", " + " brd.nbiobankreturndetailscode, 'nbiobankreturndetailscode', "
					            + " 'biobankreturndetails', COALESCE(brd.srepositoryid, ''), "
					            + Enumeration.TransactionStatus.STORED.gettransactionstatus() + ", "
					            + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
					            + dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
					            + ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					            + " ('"
					            + commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
					                    .replace("'", "''")
					            + " [' || COALESCE(brd.srepositoryid,'') || '] ' || '"
					            + commonFunction
					                    .getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
					                    .replace("'", "''")
					            + " [' || COALESCE(brd.nbioparentsamplecode::text,'') || '] ' || '"
					            + commonFunction
					                    .getMultilingualMessage("IDS_STOREDSITE", userInfo.getSlanguagefilename())
					                    .replace("'", "''")
					            + " [' || COALESCE(st.ssitename,'') || '] ' || '"
					            + commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
					                    .replace("'", "''")
					            + " [' || COALESCE(br.sbankreturnformnumber,'') || ']')"
					            + ", " + userInfo.getNtranssitecode() + ", "
					            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					            + " from biobankreturndetails brd "
					            + " join biobankreturn br on br.nbiobankreturncode = brd.nbiobankreturncode "
					            + " and br.nsitecode = " + userInfo.getNtranssitecode() + " and br.nstatus = "
					            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					            + " join site st on st.nsitecode = brd.nsitecode"
					            + " where brd.nsitecode = " + userInfo.getNtranssitecode() + " and brd.nstatus = "
					            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					            + " and brd.nbiobankreturndetailscode in (" + sbankReturnDetailsCode + ")"
					            + " and brd.nsamplestatus = " + Enumeration.TransactionStatus.STORED.gettransactionstatus()
					            + ";";

					String strSeqUpdate = "update seqnoregistration set nsequenceno = nsequenceno + ("
							+ " select count(*) from biobankreturndetails brd "
							+ " where brd.nbiobankreturndetailscode in (" + sbankReturnDetailsCode + ")"
							+ " and brd.nsitecode = " + userInfo.getNtranssitecode() + " and brd.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and brd.nsamplestatus = "
							+ Enumeration.TransactionStatus.STORED.gettransactionstatus()
							+ ") where stablename = 'chaincustody' and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					jdbcTemplate.execute(strChainCustody);
					jdbcTemplate.execute(strSeqUpdate);
				}
				// ===== COC: END =====

				List<BioBankReturnDetails> lstAuditReturnDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
						new BioBankReturnDetails());
				lstAuditBefore.addAll(lstAuditReturnDetailsBefore);
				lstAuditAfter.addAll(lstAuditReturnDetailsAfter);
				lstAuditReturnDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_STORESAMPLES"));

				auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList,
						userInfo);
				List<Map<String, Object>> lstChildBioBankReturn = getChildInitialGet(nbioBankReturnCode, userInfo);
				outputMap.put("lstChildBioBankReturn", lstChildBioBankReturn);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);

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

	public static List<String> generateOrder(int rows, int columns, int direction) {
		List<String> orderArray = new ArrayList<>();

		if (direction == 1) { // A1, A2, A3, ..., B1, B2...
			for (int row = 0; row < rows; row++) {
				for (int col = 0; col < columns; col++) {
					orderArray.add(ALPHABET[row] + (col + 1));
				}
			}
		} else { // A1, B1, C1...
			for (int col = 0; col < columns; col++) {
				for (int row = 0; row < rows; row++) {
					orderArray.add(ALPHABET[row] + (col + 1));
				}
			}
		}

		return orderArray;
	}

	public String findStatusBankReturnDetails(final int nbioBankReturnCode, final String sbioBankReturnDetailsCode,
			final String concatString, final UserInfo userInfo) throws Exception {
		final String strStatusBankReturn = "select nbiobankreturndetailscode from biobankreturndetails where"
				+ " nbiobankreturncode=" + nbioBankReturnCode + " and nbiobankreturndetailscode in ("
				+ sbioBankReturnDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + concatString;
		List<Integer> lstStatusBankReturn = jdbcTemplate.queryForList(strStatusBankReturn, Integer.class);
		String strBankReturnDetailsCode = null;
		if (lstStatusBankReturn.size() > 0) {
			strBankReturnDetailsCode = lstStatusBankReturn.stream().map(String::valueOf)
					.collect(Collectors.joining(", "));
		}
		return strBankReturnDetailsCode;
	}

	public ResponseEntity<Object> undoDisposeSamples(final int nbioBankReturnCode,
			final String nbioBankReturnDetailsCode, final UserInfo userInfo) throws Exception {

		jdbcTemplate
				.execute("lock table lockbiobankreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final String concatString = " and nsamplestatus in ("
				+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ")";

		final String validatedBankReturnDetailsCode = findStatusBankReturnDetails(nbioBankReturnCode,
				nbioBankReturnDetailsCode, concatString, userInfo);

		if (validatedBankReturnDetailsCode != null) {

			List<Integer> lstSamples = getNonDeletedSamples(nbioBankReturnDetailsCode, userInfo);
			if (lstSamples.size() == 0) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final String strAuditAfterQry = childAuditQuery(nbioBankReturnCode, validatedBankReturnDetailsCode, "",
					userInfo);
			List<BioBankReturnDetails> lstAuditReturnDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
					new BioBankReturnDetails());

			int bankReturnHistoryDetailsPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='biobankreturndetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			String strUpdateQry = "update biobankreturndetails set nsamplestatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiobankreturndetailscode in (" + validatedBankReturnDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			strUpdateQry += "insert into biobankreturndetailshistory"
					+ " (nbiobankreturndetailshistorycode, nbiobankreturndetailscode, nbiobankreturncode,"
					+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
					+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
					+ "select " + bankReturnHistoryDetailsPk + "+rank()over(order by nbiobankreturndetailscode),"
					+ " nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biobankreturndetails where nbiobankreturndetailscode in (" + validatedBankReturnDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by nbiobankreturndetailscode;";
			strUpdateQry += " update seqnobiobankmanagement set nsequenceno=(" + "select " + bankReturnHistoryDetailsPk
					+ " + count(nbiobankreturndetailscode) from biobankreturndetails where"
					+ " nbiobankreturndetailscode in (" + validatedBankReturnDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " where stablename='biobankreturndetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			// ===== COC: START =====
//			{
//				String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery1);
//
//				String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery2);
//
//				String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery3);
//
//				int chainCustodyPk = jdbcTemplate.queryForObject(
//						"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//						Integer.class);
//
//				String strChainCustody = "insert into chaincustody ("
//						+ " nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
//						+ " ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
//						+ " noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + chainCustodyPk
//						+ " + rank() over(order by brd.nbiobankreturndetailscode), " + userInfo.getNformcode() + ", "
//						+ " brd.nbiobankreturndetailscode, 'nbiobankreturndetailscode', "
//						+ " 'biobankreturndetails', COALESCE(br.sbankreturnformnumber, ''), "
//						+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", "
//						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
//						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
//						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " ('"
//						+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//								.replace("'", "''")
//						+ " [' || COALESCE(brd.srepositoryid,'') || '] ' || '"
//						+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//								.replace("'", "''")
//						+ " [' || COALESCE(brd.nbioparentsamplecode::text,'') || '] ' || '"
//						+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//								.replace("'", "''")
//						+ " [' || COALESCE(br.sbankreturnformnumber,'') || ']'), " + userInfo.getNtranssitecode() + ", "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from biobankreturndetails brd"
//						+ " join biobankreturn br on br.nbiobankreturncode = brd.nbiobankreturncode"
//						+ " and br.nsitecode = " + userInfo.getNtranssitecode() + " and br.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where brd.nsitecode = "
//						+ userInfo.getNtranssitecode() + " and brd.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and brd.nbiobankreturndetailscode in (" + validatedBankReturnDetailsCode + ")"
//						+ " and brd.nsamplestatus = " + Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus()
//						+ ";";
//
//				String strSeqUpdate = "update seqnoregistration set nsequenceno = nsequenceno + ("
//						+ " select count(*) from biobankreturndetails brd" + " where brd.nbiobankreturndetailscode in ("
//						+ validatedBankReturnDetailsCode + ")" + " and brd.nsitecode = " + userInfo.getNtranssitecode()
//						+ " and brd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and brd.nsamplestatus = " + Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus()
//						+ ") where stablename = 'chaincustody' and nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//				jdbcTemplate.execute(strChainCustody);
//				jdbcTemplate.execute(strSeqUpdate);
//			}
			// ===== COC: END =====

			jdbcTemplate.execute(strUpdateQry);

			List<BioBankReturnDetails> lstAuditReturnDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
					new BioBankReturnDetails());
			lstAuditBefore.addAll(lstAuditReturnDetailsBefore);
			lstAuditAfter.addAll(lstAuditReturnDetailsAfter);
			lstAuditReturnDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_UNDODISPOSESAMPLES"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioBankReturn = getChildInitialGet(nbioBankReturnCode, userInfo);
			outputMap.put("lstChildBioBankReturn", lstChildBioBankReturn);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDISPOSESAMPLES", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

}
