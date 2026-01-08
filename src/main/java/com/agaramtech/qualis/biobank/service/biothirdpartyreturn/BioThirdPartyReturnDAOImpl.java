package com.agaramtech.qualis.biobank.service.biothirdpartyreturn;

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
import com.agaramtech.qualis.biobank.model.BioThirdPartyFormAccept;
import com.agaramtech.qualis.biobank.model.BioThirdPartyFormAcceptDetails;
import com.agaramtech.qualis.biobank.model.BioThirdPartyReturn;
import com.agaramtech.qualis.biobank.model.BioThirdPartyReturnDetails;
import com.agaramtech.qualis.biobank.model.FormType;
import com.agaramtech.qualis.biobank.model.ThirdParty;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class BioThirdPartyReturnDAOImpl implements BioThirdPartyReturnDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(BioThirdPartyReturnDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	//private final JdbcTemplateUtilityFunction jdbcTemplateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final EmailDAOSupport emailDAOSupport;

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getBioThirdPartyReturn(final Map<String, Object> inputMap,
			final int nbioThirdPartyReturnCode, final UserInfo userInfo) throws Exception {
		LOGGER.info("getBioThirdPartyReturn");
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		String fromDate = (String) inputMap.get("fromDate");
		String toDate = (String) inputMap.get("toDate");
		int ntransCode = inputMap.containsKey("ntransCode") ? (int) inputMap.get("ntransCode") : -1;
		final int saveType = inputMap.containsKey("saveType") ? Integer.valueOf(inputMap.get("saveType").toString())
				: 1;
		int nthirdPartyCode = inputMap.containsKey("nthirdPartyCode") ? (int) inputMap.get("nthirdPartyCode") : -1;

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

		List<ThirdParty> getThirdParty = getThirdParty(userInfo);
		List<Map<String, Object>> lstThirdParty = new ArrayList<>();

		getThirdParty.stream().forEach(lst -> {
			if (lst != null) {
				Map<String, Object> mapStatus = new HashMap<>();
				mapStatus.put("label", lst.getSthirdpartyname());
				mapStatus.put("value", lst.getNthirdpartycode());
				mapStatus.put("item", lst);
				lstThirdParty.add(mapStatus);
			}
		});

		if (lstThirdParty.isEmpty()) {
			outputMap.put("lstThirdParty", null);
			outputMap.put("selectedThirdParty", null);
			outputMap.put("realSelectedThirdParty", null);
			outputMap.put("lstFilterStatus", null);
			outputMap.put("selectedFilterStatus", null);
			outputMap.put("realSelectedFilterStatus", null);
			outputMap.put("lstBioThirdPartyReturn", null);
			outputMap.put("selectedBioThirdPartyReturn", null);
			outputMap.put("lstChildBioThirdPartyReturn", null);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}

		if (!inputMap.containsKey("ntransCode")) {
			nthirdPartyCode = getThirdParty.get(0).getNthirdpartycode();
		} else {
			nthirdPartyCode = (int) inputMap.get("nthirdPartyCode");
		}

		final int nthirdPartyCodePK = (int) nthirdPartyCode;
		Map<String, Object> selectedThirdParty = (!lstThirdParty.isEmpty()) ? lstThirdParty.stream()
				.filter(x -> (int) x.get("value") == nthirdPartyCodePK).collect(Collectors.toList()).get(0) : null;

		outputMap.put("lstThirdParty", lstThirdParty);
		outputMap.put("selectedThirdParty", selectedThirdParty);
		outputMap.put("realSelectedThirdParty", selectedThirdParty);
		final String strConditionThirdPartyCode = nthirdPartyCode == 0 ? ""
				: " and bbr.nthirdpartycode=" + nthirdPartyCode + " ";

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

		final String strQuery = "select bbr.nbiothirdpartyreturncode, bbr.sthirdpartyreturnformnumber, bbr.jsondata->>'soriginsitename' soriginsitename,"
				+ " to_char(bbr.dreturndate, '" + userInfo.getSsitedate() + "') sreturndate, bbr.nthirdpartycode,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, bbr.ntransactionstatus,"
				+ " cm.scolorhexcode, bbr.noriginsitecode, bbr.jsondata->>'sremarks' sremarks, bbr.jsondata->>'sstorageconditionname' sstorageconditionname,"
				+ " to_char(bbr.ddeliverydate, '" + userInfo.getSsitedate()
				+ "') sdeliverydate, bbr.jsondata->>'sdispatchername' sdispatchername, bbr.jsondata->>'scouriername' scouriername,"
				+ " bbr.jsondata->>'scourierno' scourierno, bbr.jsondata->>'striplepackage' striplepackage, bbr.jsondata->>'svalidationremarks' svalidationremarks"
				+ " from biothirdpartyreturn bbr join site s" + " on s.nsitecode=bbr.noriginsitecode"
				+ " and s.nmastersitecode=" + userInfo.getNmastersitecode() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join transactionstatus ts on ts.ntranscode=bbr.ntransactionstatus and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join formwisestatuscolor fwsc on fwsc.ntranscode=ts.ntranscode and fwsc.nformcode= "
				+ userInfo.getNformcode() + " and fwsc.nsitecode=" + userInfo.getNmastersitecode()
				+ " and fwsc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join colormaster cm on fwsc.ncolorcode=cm.ncolorcode and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bbr.nsitecode="
				+ userInfo.getNtranssitecode() + " and bbr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbr.dtransactiondate between '"
				+ fromDate + "' and '" + toDate + "'" + strConditionTransCode + strConditionThirdPartyCode
				+ " order by bbr.nbiothirdpartyreturncode desc";

		final List<BioThirdPartyReturn> lstBioThirdPartyReturn = jdbcTemplate.query(strQuery,
				new BioThirdPartyReturn());

		if (!lstBioThirdPartyReturn.isEmpty()) {
			outputMap.put("lstBioThirdPartyReturn", lstBioThirdPartyReturn);
			List<BioThirdPartyReturn> lstObjBioThirdPartyReturn = null;
			if (nbioThirdPartyReturnCode == -1) {
				lstObjBioThirdPartyReturn = lstBioThirdPartyReturn;
			} else {
				lstObjBioThirdPartyReturn = lstBioThirdPartyReturn.stream()
						.filter(x -> x.getNbiothirdpartyreturncode() == nbioThirdPartyReturnCode)
						.collect(Collectors.toList());
			}
			outputMap.put("selectedBioThirdPartyReturn", lstObjBioThirdPartyReturn.get(0));

			if (saveType == 2) {
				outputMap.putAll((Map<String, Object>) getFormNumberDetails(
						lstObjBioThirdPartyReturn.get(0).getNoriginsitecode(), userInfo).getBody());
			}

			List<Map<String, Object>> lstChildBioThirdPartyReturn = getChildInitialGet(
					lstObjBioThirdPartyReturn.get(0).getNbiothirdpartyreturncode(), userInfo);
			outputMap.put("lstChildBioThirdPartyReturn", lstChildBioThirdPartyReturn);

			if (inputMap.containsKey(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())) {
				outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						inputMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
			}
		} else {
			outputMap.put("lstBioThirdPartyReturn", null);
			outputMap.put("selectedBioThirdPartyReturn", null);
			outputMap.put("lstChildBioThirdPartyReturn", null);
		}
		outputMap.put("nprimaryKeyBioThirdPartyReturn",
				inputMap.containsKey("nprimaryKeyBioThirdPartyReturn") ? inputMap.get("nprimaryKeyBioThirdPartyReturn")
						: -1);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public List<Map<String, Object>> getChildInitialGet(int nbioThirdPartyReturnCode, UserInfo userInfo)
			throws Exception {
		final String strChildGet = "select row_number() over(order by bbrd.nbiothirdpartyreturndetailscode desc) as"
				+ " nserialno, bbrd.nbiothirdpartyreturndetailscode, bbrd.nbiothirdpartyreturncode,"
				+ " bbrd.jsondata->>'sparentsamplecode' sparentsamplecode,"
				+ " bbrd.srepositoryid, concat(p.sproductname, ' (', pc.sproductcatname, ')') sproductname, bbrd.svolume, bbrd.sreturnvolume,"
				+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') ssamplecondition,"
				+ " coalesce(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') ssamplestatus, r.sreason, bbrd.nsamplestatus, bbrd.nsamplecondition,"
				+ " concat(bbrd.jsondata->>'sparentsamplecode', ' | ', bbrd.ncohortno) sparentsamplecodecohortno, bfa.sformnumber,"
				+ " COALESCE(NULLIF(bbrd.jsondata->>'sextractedsampleid', ''), '-') AS sextractedsampleid,"
				+ " COALESCE(NULLIF(bbrd.jsondata->>'sqcplatform', ''), '-') AS sqcplatform,"
				+ " COALESCE(NULLIF(bbrd.jsondata->>'seluent', ''), '-') AS seluent,"
				+ " COALESCE(NULLIF(bbrd.jsondata->>'sconcentration', ''), '-') AS sconcentration "
				+ " from biothirdpartyreturndetails bbrd join transactionstatus ts1 on"
				+ " bbrd.nsamplecondition=ts1.ntranscode and ts1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join transactionstatus ts2 on"
				+ " bbrd.nsamplestatus=ts2.ntranscode and ts2.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join product p on p.nproductcode=bbrd.nproductcode and p.nsitecode=" + userInfo.getNmastersitecode()
				+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join productcategory pc on pc.nproductcatcode=p.nproductcatcode and pc.nsitecode="
				+ userInfo.getNmastersitecode() + " and pc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join biothirdpartyformaccept bfa on bfa.nbiothirdpartyformacceptancecode=bbrd.nbiothirdpartyformacceptancecode and "
				+ " bfa.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bfa.nsitecode="
				+ userInfo.getNtranssitecode()
				+ " left join reason r on r.nreasoncode=bbrd.nreasoncode and r.nsitecode="
				+ userInfo.getNmastersitecode() + " and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bbrd.nsitecode="
				+ userInfo.getNtranssitecode() + " and bbrd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbrd.nbiothirdpartyreturncode="
				+ nbioThirdPartyReturnCode + " order by nserialno desc";
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

		final String strCountCheck = "select a.nbiothirdpartyformacceptancecode from (select bfad.nbiothirdpartyformacceptancecode, "
				+ " count(bfad.nbiothirdpartyformacceptancedetailscode) cnt from biothirdpartyformaccept bfa join"
				+ " biothirdpartyformacceptdetails bfad on bfad.nbiothirdpartyformacceptancecode=bfa.nbiothirdpartyformacceptancecode"
				+ " and bfad.nsitecode=" + userInfo.getNtranssitecode() + " and bfad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bfad.nsamplestatus="
				+ Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus() + " and bfad.nsamplestatus != "
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " where bfa.nsitecode="
				+ userInfo.getNtranssitecode() + " and bfa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by bfad.nbiothirdpartyformacceptancecode) a left join (select bbrd.nbiothirdpartyformacceptancecode,"
				+ " count(bbrd.nbiothirdpartyformacceptancedetailscode) cnt from biothirdpartyreturn bbr join biothirdpartyreturndetails bbrd on"
				+ " bbrd.nbiothirdpartyreturncode=bbr.nbiothirdpartyreturncode and bbrd.nsitecode="
				+ userInfo.getNtranssitecode() + " and bbrd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbrd.nsamplestatus != "
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + " where bbr.nsitecode="
				+ userInfo.getNtranssitecode() + " and bbr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by bbrd.nbiothirdpartyformacceptancecode) b on a.nbiothirdpartyformacceptancecode=b.nbiothirdpartyformacceptancecode"
				+ " where b.nbiothirdpartyformacceptancecode is null or a.cnt > b.cnt";
		final String strFormDetails = "select bfa.nbiothirdpartyformacceptancecode, "
				+ " bfa.nbiorequestbasedtransfercode, bfa.ntransfertypecode, bfa.nformtypecode, bfa.sformnumber, bfa.nthirdpartycode,"
				+ " bfa.noriginsitecode, bfa.jsondata->>'soriginsitename' soriginsitename from biothirdpartyformaccept bfa "
				+ " join biothirdpartyformacceptdetails bfad on bfad.nbiothirdpartyformacceptancecode=bfa.nbiothirdpartyformacceptancecode"
				+ " and bfad.nsitecode=" + userInfo.getNtranssitecode() + " and bfad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bfad.nsamplestatus="
				+ Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus()
				+ " JOIN thirdpartyusermapping tpum ON bfa.nthirdpartycode = tpum.nthirdpartycode and tpum.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tpum.nusercode = "+ userInfo.getNusercode()+" and tpum.nuserrolecode = "+userInfo.getNuserrole()
				+ " where bfa.nsitecode="
				+ userInfo.getNtranssitecode() + " and bfa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bfa.nbiothirdpartyformacceptancecode in (" + strCountCheck + ")" + concatStr
				+ " group by bfa.nbiothirdpartyformacceptancecode, bfa.nbiorequestbasedtransfercode,"
				+ " bfa.ntransfertypecode, bfa.nformtypecode, bfa.sformnumber, bfa.nthirdpartycode, bfa.noriginsitecode, soriginsitename"
				+ " order by ntransfertypecode";
		final List<BioThirdPartyFormAccept> lstFormDetails = jdbcTemplate.query(strFormDetails,
				new BioThirdPartyFormAccept());

		List<Map<String, Object>> lstFormNumberDetails = new ArrayList<>();

		lstFormDetails.stream().forEach(lst -> {
			Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getSformnumber());
			mapStatus.put("value", lst.getNbiothirdpartyformacceptancecode());
			mapStatus.put("item", lst);
			lstFormNumberDetails.add(mapStatus);
		});

		outputMap.put("lstFormNumberDetails", lstFormNumberDetails);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getThirdPartyFormAcceptanceDetails(final int nbioThirdPartyFormAccetanceCode,
			UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strThirdPartyFormAcceptanceDetails = "select bfad.nbiothirdpartyformacceptancedetailscode, bfad.nbiothirdpartyformacceptancecode, bfad.srepositoryid,"
				+ " bfad.nproductcode, concat(p.sproductname, ' (', pc.sproductcatname, ')') sproductname, bfad.nbioprojectcode, bfad.nbioparentsamplecode, "
				+ " bfad.nstoragetypecode, bfad.nbiosamplereceivingcode, bfad.nsamplecondition, bfad.nsamplestatus, "
				+ " bfad.nreasoncode, bfad.nsamplestoragetransactioncode, bfad.sreceivedvolume svolume, bfad.jsondata->>'sparentsamplecode' sparentsamplecode, bfad.ncohortno,"
				+ " bfad.jsondata->>'ssubjectid' ssubjectid, bfad.jsondata->>'scasetype' scasetype, bfad.ndiagnostictypecode, bfad.ncontainertypecode, bfad.nproductcatcode, "
				+ " bfad.jsondata->>'seluent' seluent, bfad.jsondata->>'sqcplatform' sqcplatform, bfad.jsondata->>'sconcentration' sconcentration, "
				+ " bfad.jsondata->>'sparentsamplecode' sparentsamplecode, bfad.jsondata->>'sformnumber' sformnumber,"
				+ " bfad.jsondata->>'sextractedsampleid' sextractedsampleid, bfad.jsondata->>'noriginsitecode' noriginsitecode, "
				+ " bfad.jsondata->>'nthirdpartycode' nthirdpartycode, bfad.jsondata->>'sreferencerepoid' sreferencerepoid from biothirdpartyformacceptdetails bfad"
				+ " join product p on p.nproductcode=bfad.nproductcode and p.nsitecode=" + userInfo.getNmastersitecode()
				+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join productcategory pc on pc.nproductcatcode=p.nproductcatcode and pc.nsitecode="
				+ userInfo.getNmastersitecode() + " and pc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bfad.nsitecode="
				+ userInfo.getNtranssitecode() + " and bfad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bfad.nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAccetanceCode
				+ " and bfad.nsamplestatus=" + Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus()
				+ " and bfad.nbiothirdpartyformacceptancedetailscode not in (select nbiothirdpartyformacceptancedetailscode"
				+ " from biothirdpartyreturndetails where nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
				+ " and nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAccetanceCode + ")"
				+ " order by bfad.nbiothirdpartyformacceptancedetailscode";
		final List<BioThirdPartyFormAcceptDetails> lstThirdPartyFormAcceptanceDetails = jdbcTemplate
				.query(strThirdPartyFormAcceptanceDetails, new BioThirdPartyFormAcceptDetails());

		outputMap.put("lstThirdPartyFormAcceptanceDetails", lstThirdPartyFormAcceptanceDetails);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getActiveBioThirdPartyReturn(final int nbioThirdPartyReturnCode, final int saveType,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strQuery = "select bbr.nbiothirdpartyreturncode, bbr.sthirdpartyreturnformnumber, bbr.ntransfertypecode,"
				+ " coalesce(tt.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " tt.jsondata->'sdisplayname'->>'en-US') stransfertypename,  bbr.noriginsitecode,"
				+ " bbr.jsondata->>'soriginsitename' soriginsitename, bbr.dreturndate," + " to_char(bbr.dreturndate,'"
				+ userInfo.getSsitedate() + "')  sreturndate, bbr.ntzreturndate, bbr.noffsetdreturndate,"
				+ " bbr.ntransactionstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, bbr.jsondata->>'sremarks' sremarks, bbr.nsitecode,"
				+ " bbr.nstatus, cm.scolorhexcode, bbr.jsondata->>'sstorageconditionname' sstorageconditionname, to_char(bbr.ddeliverydate, '"
				+ userInfo.getSsitedate() + "') sdeliverydate, bbr.jsondata->>'sdispatchername' sdispatchername,"
				+ " bbr.jsondata->>'scouriername' scouriername, bbr.jsondata->>'scourierno' scourierno, bbr.jsondata->>'striplepackage' striplepackage,"
				+ " bbr.jsondata->>'svalidationremarks' svalidationremarks from biothirdpartyreturn bbr"
				+ " join transfertype tt on bbr.ntransfertypecode=tt.ntransfertypecode and tt.nsitecode="
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
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbr.nbiothirdpartyreturncode="
				+ nbioThirdPartyReturnCode + " order by bbr.nbiothirdpartyreturncode desc";
		final BioThirdPartyReturn objBioThirdPartyReturn = (BioThirdPartyReturn) jdbcUtilityTemplateFunction
				.queryForObject(strQuery, BioThirdPartyReturn.class, jdbcTemplate);

		if (objBioThirdPartyReturn != null) {
			outputMap.put("selectedBioThirdPartyReturn", objBioThirdPartyReturn);
			List<Map<String, Object>> lstChildBioThirdPartyReturn = getChildInitialGet(
					objBioThirdPartyReturn.getNbiothirdpartyreturncode(), userInfo);
			outputMap.put("lstChildBioThirdPartyReturn", lstChildBioThirdPartyReturn);
			if (saveType == 2) {
				outputMap.putAll((Map<String, Object>) getFormNumberDetails(objBioThirdPartyReturn.getNoriginsitecode(),
						userInfo).getBody());
			}

		} else {
			outputMap.put("selectedBioThirdPartyReturn", null);
			outputMap.put("lstChildBioThirdPartyReturn", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public List<ThirdParty> getThirdParty(UserInfo userInfo) throws Exception {

		final String strThirdParty = "select tp.nthirdpartycode, tp.sthirdpartyname"
				+ " from thirdparty tp join thirdpartyusermapping"
				+ " tpum on tpum.nthirdpartycode=tp.nthirdpartycode and tpum.nsitecode=" + userInfo.getNmastersitecode()
				+ " and tpum.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tpum.nusercode=" + userInfo.getNusercode() + " and tpum.nuserrolecode="
				+ userInfo.getNuserrole() + " where tp.nsitecode=" + userInfo.getNmastersitecode() + " and tp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tp.nthirdpartycode > 0 order by tp.nthirdpartycode";
		return jdbcTemplate.query(strThirdParty, new ThirdParty());
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
	public ResponseEntity<Object> createBioThirdPartyReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		jdbcTemplate
				.execute("lock table lockbiothirdpartyreturn " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbiothirdpartyreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final ObjectMapper objMapper = new ObjectMapper();

		int nprimaryKeyBioThirdPartyReturn = (int) inputMap.get("nprimaryKeyBioThirdPartyReturn");

		BioThirdPartyReturn objBioThirdPartyReturnData = objMapper.convertValue(inputMap.get("bioThirdPartyReturn"),
				BioThirdPartyReturn.class);

		List<Map<String, Object>> filteredSampleReceiving = getNotExistSamplesSampleReceivingCode(
				(List<Map<String, Object>>) inputMap.get("filterSelectedSamples"), userInfo);

		if (filteredSampleReceiving.size() == 0) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYEXISTS", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final String sreturnDate = (objBioThirdPartyReturnData.getSreturndate() != null
				&& !objBioThirdPartyReturnData.getSreturndate().isEmpty()) ? "'"
						+ objBioThirdPartyReturnData.getSreturndate().toString().replace("T", " ").replace("Z", "")
						+ "'" : null;

		BioThirdPartyReturn objBioThirdPartyReturn = null;

		if (nprimaryKeyBioThirdPartyReturn != -1) {
			final String strCheckAlreadyRecordExists = "select nbiothirdpartyreturncode from biothirdpartyreturn where "
					+ " nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nbiothirdpartyreturncode="
					+ nprimaryKeyBioThirdPartyReturn;
			objBioThirdPartyReturn = (BioThirdPartyReturn) jdbcUtilityTemplateFunction
					.queryForObject(strCheckAlreadyRecordExists, BioThirdPartyReturn.class, jdbcTemplate);
		}

		int seqNoBioThirdPartyReturn = -1;
		int seqNoBioThirdPartyReturnHistory = -1;
		int nbiothirdpartyreturncode = -1;
		String strInsertThirdPartyReturn = "";
		String strInsertThirdPartyReturnHistory = "";
		String strSeqNoUpdate = "";
		String strUpdateThirdPartyReturn = "";
		String strformat="";

		if (objBioThirdPartyReturn == null) {
			seqNoBioThirdPartyReturn = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyreturn' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioThirdPartyReturn++;

			seqNoBioThirdPartyReturnHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyreturnhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioThirdPartyReturnHistory++;

			nbiothirdpartyreturncode = seqNoBioThirdPartyReturn;
			 strformat = projectDAOSupport.getSeqfnFormat("biothirdpartyreturn",
					"seqnoformatgeneratorbiobank", 0, 0, userInfo);
			strInsertThirdPartyReturn = "insert into biothirdpartyreturn(nbiothirdpartyreturncode, sthirdpartyreturnformnumber, ntransfertypecode, nformtypecode, nthirdpartycode, noriginsitecode, dreturndate,"
					+ " ntzreturndate, noffsetdreturndate, ntransactionstatus, jsondata, dtransactiondate,"
					+ " ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) values ("
					+ nbiothirdpartyreturncode + ", '" + strformat + "', "
					+ objBioThirdPartyReturnData.getNtransfertypecode() + ", "
					+ objBioThirdPartyReturnData.getNformtypecode() + ", "
					+ objBioThirdPartyReturnData.getNthirdpartycode() + ", "
					+ objBioThirdPartyReturnData.getNoriginsitecode() + ", " + sreturnDate + ", "
					+ userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", json_build_object('sremarks', '"
					+ stringUtilityFunction.replaceQuote(objBioThirdPartyReturnData.getSremarks())
					+ "', 'soriginsitename', '"
					+ stringUtilityFunction.replaceQuote(objBioThirdPartyReturnData.getSoriginsitename()) + "'), '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "); ";
			strSeqNoUpdate += "update seqnobiobankmanagement set nsequenceno=" + seqNoBioThirdPartyReturn + " where"
					+ " stablename='biothirdpartyreturn' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			strInsertThirdPartyReturnHistory = "insert into biothirdpartyreturnhistory (nbiothirdpartyreturnhistorycode, nbiothirdpartyreturncode,"
					+ " ntransactionstatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) values ("
					+ seqNoBioThirdPartyReturnHistory + ", " + nbiothirdpartyreturncode + ", "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			strInsertThirdPartyReturnHistory += "update seqnobiobankmanagement set nsequenceno="
					+ seqNoBioThirdPartyReturnHistory + " where stablename='biothirdpartyreturnhistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			inputMap.put("nprimaryKeyBioThirdPartyReturn", seqNoBioThirdPartyReturn);
		} else {
			nbiothirdpartyreturncode = objBioThirdPartyReturn.getNbiothirdpartyreturncode();
			strUpdateThirdPartyReturn = "update biothirdpartyreturn set dreturndate=" + sreturnDate + ","
					+ (objBioThirdPartyReturnData.getSremarks().isEmpty() ? ""
							: " jsondata=jsondata || {\"sremarks\": \"" + stringUtilityFunction
									.replaceQuote(objBioThirdPartyReturnData.getSremarks().toString()) + "\"},")
					+ " dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ " ntztransactiondate=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " where"
					+ " nbiothirdpartyreturncode=" + nbiothirdpartyreturncode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		}

		Map<String, Object> getMap = insertBioThirdPartyReturnDetails(
				objBioThirdPartyReturnData.getNbiothirdpartyformacceptancecode(), filteredSampleReceiving,
				nbiothirdpartyreturncode, userInfo,strformat);

		final String rtnQry = (String) getMap.get("queryString");
		final String sbioThirdPartyReturnDetailsCode = (String) getMap.get("sbioThirdPartyReturnDetailsCode");

		jdbcTemplate.execute(strInsertThirdPartyReturn + strUpdateThirdPartyReturn + rtnQry + strSeqNoUpdate
				+ strInsertThirdPartyReturnHistory);

		final List<Object> lstAuditAfter = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final String strAuditQry = auditQuery(nbiothirdpartyreturncode, sbioThirdPartyReturnDetailsCode, userInfo);
		List<BioThirdPartyReturn> lstAuditReturnDetailsAfter = jdbcTemplate.query(strAuditQry,
				new BioThirdPartyReturn());
		lstAuditAfter.addAll(lstAuditReturnDetailsAfter);
		lstAuditReturnDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_ADDRETURNSAMPLES"));
		auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);

		final int ntransCode = (int) inputMap.get("ntransCode");
		inputMap.put("ntransCode",
				ntransCode == 0 ? ntransCode : Enumeration.TransactionStatus.DRAFT.gettransactionstatus());

		return getBioThirdPartyReturn(inputMap, nprimaryKeyBioThirdPartyReturn, userInfo);
	}

	public List<Map<String, Object>> getNotExistSamplesSampleReceivingCode(
			final List<Map<String, Object>> filteredSampleReceiving, UserInfo userInfo) throws Exception {
		final String sbioSampleReceivingCode = filteredSampleReceiving.stream()
				.map(x -> String.valueOf(x.get("nbiosamplereceivingcode"))).collect(Collectors.joining(","));

		final String strSamplesGet = "select nbiosamplereceivingcode from biothirdpartyreturndetails where nbiosamplereceivingcode in ("
				+ sbioSampleReceivingCode + ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ";";
		List<Integer> versionList = jdbcTemplate.queryForList(strSamplesGet, Integer.class);

		List<Map<String, Object>> nonMatchingSamples = filteredSampleReceiving.stream()
				.filter(map -> !versionList.contains((Integer) map.get("nbiosamplereceivingcode")))
				.collect(Collectors.toList());

		return nonMatchingSamples;
	}

	public Map<String, Object> insertBioThirdPartyReturnDetails(final int nbioThirdPartyFormAcceptanceCode,
			final List<Map<String, Object>> filteredSampleReceiving, final int nbioThirdPartyReturnCode,
			final UserInfo userInfo, String strformat) throws Exception {

		Map<String, Object> rtnMap = new HashMap<>();

		String sbioThirdPartyReturnDetailsCode = "";

		int seqNoBioThirdPartyReturnDetails = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyreturndetails' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);
		int seqNoBioThirdPartyReturnDetailsHistory = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyreturndetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);

		String strInsertThirdPartyReturnDetails = "insert into biothirdpartyreturndetails (nbiothirdpartyreturndetailscode,"
				+ " nbiothirdpartyreturncode, nbiothirdpartyformacceptancecode, nbiothirdpartyformacceptancedetailscode, nbioprojectcode,"
				+ " nbioparentsamplecode, ncohortno, nstoragetypecode, nproductcatcode, nproductcode, "
				+ " svolume, sreturnvolume, jsondata, ndiagnostictypecode, ncontainertypecode,"
				+ " nbiosamplereceivingcode, nsamplecondition, srepositoryid, "
				+ " nsamplestatus, nreasoncode, nsamplestoragetransactioncode, nisexternalsample,"
				+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) values ";

		final var sFormNumberDiposeQry = "select jsondata->>'" + Enumeration.FormCode.REQUESTBASEDTRANSFER.getFormCode()
		+ "' as requestbasedransfer,jsondata->>'" + Enumeration.FormCode.ECATALOGREQUESTAPPROVAL.getFormCode()
		+ "' as ecatalogrequestapproval from biothirdpartyformacceptdetails " + "where nbiothirdpartyformacceptancecode="
		+ filteredSampleReceiving.get(0).get("nbiothirdpartyformacceptancecode") + " "
		+ "and nbiothirdpartyformacceptancedetailscode="
		+ filteredSampleReceiving.get(0).get("nbiothirdpartyformacceptancedetailscode") + " and nstatus="
		+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc  ";

		List<Map<String, Object>> lstForNumber = jdbcTemplate.queryForList(sFormNumberDiposeQry);

		var requestBasedTransferFormNumber = lstForNumber.get(0).get("requestbasedransfer");
		var eCatalogRequestApproTransferFormNumber = lstForNumber.get(0).get("ecatalogrequestapproval");
		
		var sThirdPartyReturnFormNumber = strformat;
		
				if(strformat==null) {
					sThirdPartyReturnFormNumber=jdbcTemplate.queryForObject(
							"select sthirdpartyreturnformnumber from biothirdpartyreturn where nbiothirdpartyreturncode="+nbioThirdPartyReturnCode+" and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							String.class);
				}
				
		
		String strThirdPartyReturnDetailsCode = "";

		for (Map<String, Object> objBioSampleReceiving : filteredSampleReceiving) {
			seqNoBioThirdPartyReturnDetails++;
			sbioThirdPartyReturnDetailsCode += seqNoBioThirdPartyReturnDetails + ",";
			
			// modified by sujatha ATE_274 from '' to null bgsi-218
			final String seluent = (objBioSampleReceiving.containsKey("seluent")
					&& objBioSampleReceiving.get("seluent") != null) ? "'" + objBioSampleReceiving.get("seluent") + "'"
							: "null";
			final String svolume = (objBioSampleReceiving.containsKey("svolume")
					&& objBioSampleReceiving.get("svolume") != null) ? "'" + objBioSampleReceiving.get("svolume") + "'"
							: "''";
			final String sreturnVolume = (objBioSampleReceiving.containsKey("sreturnvolume")
					&& objBioSampleReceiving.get("sreturnvolume") != null)
							? "'" + objBioSampleReceiving.get("sreturnvolume") + "'"
							: "''";
			final String scaseType = (objBioSampleReceiving.containsKey("scasetype")
					&& objBioSampleReceiving.get("scasetype") != null)
							? "'" + objBioSampleReceiving.get("scasetype") + "'"
							: "''";
			final String ssubjectId = (objBioSampleReceiving.containsKey("ssubjectid")
					&& objBioSampleReceiving.get("ssubjectid") != null)
							? "'" + objBioSampleReceiving.get("ssubjectid") + "'"
							: "''";
			final String sformNumber = (objBioSampleReceiving.containsKey("sformnumber")
					&& objBioSampleReceiving.get("sformnumber") != null)
							? "'" + objBioSampleReceiving.get("sformnumber") + "'"
							: "''";
					// modified by sujatha ATE_274 from '' to null bgsi-218
			final String sqcPlatForm = (objBioSampleReceiving.containsKey("sqcplatform")
					&& objBioSampleReceiving.get("sqcplatform") != null)
							? "'" + objBioSampleReceiving.get("sqcplatform") + "'"
							: "null";
				// modified by sujatha ATE_274 from '' to null bgsi-218
			final String sconcentration = (objBioSampleReceiving.containsKey("sconcentration")
					&& objBioSampleReceiving.get("sconcentration") != null)
							? "'" + objBioSampleReceiving.get("sconcentration") + "'"
							: "null";
			final String sreferenceRepoId = (objBioSampleReceiving.containsKey("sreferencerepoid")
					&& objBioSampleReceiving.get("sreferencerepoid") != null)
							? "'" + objBioSampleReceiving.get("sreferencerepoid") + "'"
							: "''";
			final String sparentSampleCode = (objBioSampleReceiving.containsKey("sparentsamplecode")
					&& objBioSampleReceiving.get("sparentsamplecode") != null)
							? "'" + objBioSampleReceiving.get("sparentsamplecode") + "'"
							: "''";
				// modified by sujatha ATE_274 from '' to null bgsi-218
			final String sextractedSampleId = (objBioSampleReceiving.containsKey("sextractedsampleid")
					&& objBioSampleReceiving.get("sextractedsampleid") != null)
							? "'" + objBioSampleReceiving.get("sextractedsampleid") + "'"
							: "null";

			strInsertThirdPartyReturnDetails += "(" + seqNoBioThirdPartyReturnDetails + ", " + nbioThirdPartyReturnCode
					+ ", " + nbioThirdPartyFormAcceptanceCode + ", "
					+ objBioSampleReceiving.get("nbiothirdpartyformacceptancedetailscode") + ", "
					+ objBioSampleReceiving.get("nbioprojectcode") + ", "
					+ objBioSampleReceiving.get("nbioparentsamplecode") + ", " + objBioSampleReceiving.get("ncohortno")
					+ ", " + objBioSampleReceiving.get("nstoragetypecode") + ", "
					+ objBioSampleReceiving.get("nproductcatcode") + ", " + objBioSampleReceiving.get("nproductcode")
					+ ", '" + objBioSampleReceiving.get("svolume") + "', '" + objBioSampleReceiving.get("sreturnvolume")
					+ "', jsonb_build_object('seluent', " + seluent + ", 'svolume', " + svolume + ", 'sreturnvolume', "
					+ sreturnVolume + ", 'scasetype', " + scaseType + ", 'ssubjectid', " + ssubjectId
					+ ", 'sformnumber', " + sformNumber + "," + " 'sqcplatform', " + sqcPlatForm + ", 'nproductcode', "
					+ objBioSampleReceiving.get("nproductcode") + ", 'sconcentration', " + sconcentration
					+ ", 'noriginsitecode', " + objBioSampleReceiving.get("noriginsitecode") + ", 'nthirdpartycode', "
					+ objBioSampleReceiving.get("nthirdpartycode") + ", 'sreferencerepoid', " + sreferenceRepoId
					+ ", 'sparentsamplecode', " + sparentSampleCode + ", 'sextractedsampleid', " + sextractedSampleId
					+",'" + Enumeration.FormCode.REQUESTBASEDTRANSFER.getFormCode()
					+ "', NULLIF('" + requestBasedTransferFormNumber + "','') ,'"
					+ Enumeration.FormCode.ECATALOGREQUESTAPPROVAL.getFormCode() + "', NULLIF('" +eCatalogRequestApproTransferFormNumber+"',''), '"
					+ Enumeration.FormCode.THIRDPARTYRETURN.getFormCode() + "', NULLIF('" +sThirdPartyReturnFormNumber+"','') "
					+ "), " + objBioSampleReceiving.get("ndiagnostictypecode") + ", "
					+ objBioSampleReceiving.get("ncontainertypecode") + ", "
					+ objBioSampleReceiving.get("nbiosamplereceivingcode") + ", "
					+ objBioSampleReceiving.get("nsamplecondition") + ", '" + objBioSampleReceiving.get("srepositoryid")
					+ "', " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
					+ objBioSampleReceiving.get("nreasoncode") + ", "
					+ objBioSampleReceiving.get("nsamplestoragetransactioncode") + ", "
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "),";
			strThirdPartyReturnDetailsCode += seqNoBioThirdPartyReturnDetails + ",";
		}

		strThirdPartyReturnDetailsCode = strThirdPartyReturnDetailsCode.substring(0,
				strThirdPartyReturnDetailsCode.length() - 1);

		int chainCustodyPk = jdbcTemplate
				.queryForObject("select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
		String strChainCustody = "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
				+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
				+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
				+ "select " + chainCustodyPk + "+rank() over(order by btprd.nbiothirdpartyreturndetailscode), "
				+ userInfo.getNformcode() + ","
				+ " btprd.nbiothirdpartyreturndetailscode, 'nbiothirdpartyreturndetailscode', "
				+ " 'biothirdpartyreturndetails', btpr.sthirdpartyreturnformnumber, "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", " + userInfo.getNusercode() + ", "
				+ userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
				+ userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
				+ commonFunction.getMultilingualMessage("IDS_DRAFT", userInfo.getSlanguagefilename())
				+ "'||' ['||btprd.srepositoryid||'] '||'"
				+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
				+ " '||btpr.sthirdpartyreturnformnumber, " + userInfo.getNtranssitecode() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " from biothirdpartyreturndetails btprd join biothirdpartyreturn btpr"
				+ " on btpr.nbiothirdpartyreturncode=btprd.nbiothirdpartyreturncode and" + " btpr.nsitecode="
				+ userInfo.getNtranssitecode() + " and btpr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btprd.nsitecode="
				+ userInfo.getNtranssitecode() + " and btprd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
				+ " btprd.nbiothirdpartyreturndetailscode in (" + strThirdPartyReturnDetailsCode + "); ";

		strChainCustody += " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
				+ "+ count(nbiothirdpartyreturndetailscode) from"
				+ " biothirdpartyreturndetails where nbiothirdpartyreturndetailscode in ("
				+ strThirdPartyReturnDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") where stablename="
				+ "'chaincustody' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		String strInsertThirdPartyReturnDetailsHistory = "insert into biothirdpartyreturndetailshistory"
				+ " (nbiothirdpartyreturndetailshistorycode, nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition,"
				+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
				+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) select "
				+ seqNoBioThirdPartyReturnDetailsHistory + "+ rank() over(order by nbiothirdpartyreturndetailscode)"
				+ ", nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition, "
				+ " nsamplestatus, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
				+ userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", "
				+ userInfo.getNdeputyuserrole() + ", nsitecode, "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " from biothirdpartyreturndetails where"
				+ " nbiothirdpartyreturndetailscode in (" + strThirdPartyReturnDetailsCode + ") and nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nbiothirdpartyreturndetailscode;";

		strInsertThirdPartyReturnDetails = strInsertThirdPartyReturnDetails.substring(0,
				strInsertThirdPartyReturnDetails.length() - 1) + ";";

		String strSeqNoUpdate = "update seqnobiobankmanagement set nsequenceno=" + seqNoBioThirdPartyReturnDetails
				+ " where stablename='biothirdpartyreturndetails' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		strInsertThirdPartyReturnDetailsHistory += "update seqnobiobankmanagement set nsequenceno=(select "
				+ seqNoBioThirdPartyReturnDetailsHistory
				+ "+ count(nbiothirdpartyreturndetailscode) from biothirdpartyreturndetails where "
				+ " nbiothirdpartyreturndetailscode in (" + strThirdPartyReturnDetailsCode + ") and nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") where"
				+ " stablename='biothirdpartyreturndetailshistory' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		sbioThirdPartyReturnDetailsCode = sbioThirdPartyReturnDetailsCode.substring(0,
				sbioThirdPartyReturnDetailsCode.length() - 1);

//		rtnMap.put("queryString", strInsertThirdPartyReturnDetails + strSeqNoUpdate
//				+ strInsertThirdPartyReturnDetailsHistory + strChainCustody);
//		rtnMap.put("sbioThirdPartyReturnDetailsCode", sbioThirdPartyReturnDetailsCode);
		
//		rtnMap.put("queryString", strInsertThirdPartyReturnDetails + strSeqNoUpdate
//				+ strInsertThirdPartyReturnDetailsHistory + strChainCustody);
		rtnMap.put("queryString", strInsertThirdPartyReturnDetails + strSeqNoUpdate
				+ strInsertThirdPartyReturnDetailsHistory);
		rtnMap.put("sbioThirdPartyReturnDetailsCode", sbioThirdPartyReturnDetailsCode);

		
		
		
		return rtnMap;
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getActiveBioThirdPartyReturnById(final int nbioThirdPartyReturnCode,
			final UserInfo userInfo) throws Exception {
		Map<String, Object> outputMap = new HashMap<>();
		final String strChildThirdPartyReturn = "select bbr.nbiothirdpartyreturncode,"
				+ " bbr.noriginsitecode, bbr.jsondata->>'soriginsitename' soriginsitename,"
				+ " coalesce(to_char(bbr.dreturndate, '" + userInfo.getSsitedate()
				+ "'), '-') sreturndate, bbr.jsondata->>'sremarks' sremarks, bbr.sthirdpartyreturnformnumber"
				+ " from biothirdpartyreturn bbr, site s where bbr.noriginsitecode=s.nsitecode and"
				+ " bbr.nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + "  and bbr.nsitecode="
				+ userInfo.getNtranssitecode() + " and bbr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final BioThirdPartyReturn objChildThirdPartyReturn = (BioThirdPartyReturn) jdbcUtilityTemplateFunction
				.queryForObject(strChildThirdPartyReturn, BioThirdPartyReturn.class, jdbcTemplate);

		outputMap.put("selectedChildThirdPartyReturn", objChildThirdPartyReturn);

		if (objChildThirdPartyReturn != null) {
			outputMap.putAll(
					(Map<String, Object>) getFormNumberDetails(objChildThirdPartyReturn.getNoriginsitecode(), userInfo)
							.getBody());
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> updateBioThirdPartyReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final int nbioThirdPartyReturnCode = (int) inputMap.get("nbiothirdpartyreturncode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		final List<Object> listBeforeSave = new ArrayList<>();

		int findStatus = findStatusThirdPartyReturn(nbioThirdPartyReturnCode, userInfo);

		final String strAuditQry = auditParentQuery(nbioThirdPartyReturnCode, "", "", userInfo);
		List<BioThirdPartyReturn> lstAuditBefore = jdbcTemplate.query(strAuditQry, new BioThirdPartyReturn());

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {
			String sreturnDate = (String) inputMap.get("sreturndate");
			String sremarks = (String) inputMap.get("sremarks");

			sreturnDate = (sreturnDate != null && !sreturnDate.isEmpty())
					? "'" + sreturnDate.toString().replace("T", " ").replace("Z", "") + "'"
					: null;

			final String strUpdateQry = "update biothirdpartyreturn set dreturndate=" + sreturnDate + ","
					+ " jsondata=jsondata || '{\"sremarks\": \"" + stringUtilityFunction.replaceQuote(sremarks)
					+ "\"}', dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ " ntztransactiondate=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(strUpdateQry);

			List<BioThirdPartyReturn> lstAuditAfter = jdbcTemplate.query(strAuditQry, new BioThirdPartyReturn());

			multilingualIDList.add("IDS_EDITRETURNFORM");
			listBeforeSave.add(lstAuditBefore.get(0));
			listAfterSave.add(lstAuditAfter.get(0));

			auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);
			return getBioThirdPartyReturn(inputMap, nbioThirdPartyReturnCode, userInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public int findStatusThirdPartyReturn(final int nbioThirdPartyReturnCode, final UserInfo userInfo)
			throws Exception {
		final String strStatusThirdPartyReturn = "select ntransactionstatus from biothirdpartyreturn where nbiothirdpartyreturncode="
				+ nbioThirdPartyReturnCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.queryForObject(strStatusThirdPartyReturn, Integer.class);
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getValidateFormDetails(final int nbioThirdPartyReturnCode, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> outputMap = new HashMap<>();

		final int nformStatus = findStatusThirdPartyReturn(nbioThirdPartyReturnCode, userInfo);
		if (nformStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			List<Map<String, Object>> lstChildBioThirdPartyReturn = getChildInitialGet(nbioThirdPartyReturnCode,
					userInfo);
			if (lstChildBioThirdPartyReturn.size() > 0) {
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

//		final String strUsersBasedOnSite = "select u.nusercode ndispatchercode, concat(u.sfirstname, ' ', u.slastname) sdispatchername from"
//				+ " users u join userssite us on u.nusercode=us.nusercode and us.nsitecode="
//				+ userInfo.getNtranssitecode() + " and us.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where u.nsitecode="
//				+ userInfo.getNmastersitecode() + " and u.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and u.nusercode > 0 order by u.nusercode";
		final String strUsersBasedOnSite = "select u.nusercode ndispatchercode, concat(u.sfirstname, ' ', u.slastname)"
				+ " sdispatchername from users u join (select nusercode from thirdpartyusermapping where nthirdpartycode in"
				+ " (select tpm.nthirdpartycode from thirdpartyusermapping tpm where tpm.nusercode="
				+ userInfo.getNusercode() + " and tpm.nuserrolecode=" + userInfo.getNuserrole() + " and tpm.nsitecode="
				+ userInfo.getNmastersitecode() + " and tpm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") and nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by nusercode) a on a.nusercode=u.nusercode and u.nusercode > 0 and u.nsitecode="
				+ userInfo.getNmastersitecode() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
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

	public ResponseEntity<Object> createValidationBioThirdPartyReturn(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		jdbcTemplate
				.execute("lock table lockbiothirdpartyreturn " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbiothirdpartyreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final ObjectMapper objMapper = new ObjectMapper();

		final int nbioThirdPartyReturnCode = (int) inputMap.get("nbiothirdpartyreturncode");
		final int findStatus = findStatusThirdPartyReturn(nbioThirdPartyReturnCode, userInfo);

		final List<Object> savedBioThirdPartyReturnList = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			final String strBioThirdPartyReturnDetailsCode = getToBeDisposeSamples(nbioThirdPartyReturnCode, userInfo);

			final String concatCondition = (strBioThirdPartyReturnDetailsCode != null
					&& strBioThirdPartyReturnDetailsCode != "")
							? " and nbiothirdpartyreturndetailscode not in (" + strBioThirdPartyReturnDetailsCode + ")"
							: "";

			final String concatConditionChainCustody = (strBioThirdPartyReturnDetailsCode != null
					&& strBioThirdPartyReturnDetailsCode != "")
							? " and btprd.nbiothirdpartyreturndetailscode not in (" + strBioThirdPartyReturnDetailsCode
									+ ")"
							: "";

			BioThirdPartyReturn objBioThirdPartyReturn = objMapper.convertValue(inputMap.get("bioThirdPartyReturn"),
					BioThirdPartyReturn.class);

			final String sdeliveryDate = (objBioThirdPartyReturn.getSdeliverydate() != null
					&& !objBioThirdPartyReturn.getSdeliverydate().isEmpty()) ? "'"
							+ objBioThirdPartyReturn.getSdeliverydate().toString().replace("T", " ").replace("Z", "")
							+ "'" : null;

			final String strUpdateThirdPartyReturn = "update biothirdpartyreturn set nstorageconditioncode="
					+ objBioThirdPartyReturn.getNstorageconditioncode() + ", ddeliverydate=" + sdeliveryDate
					+ ", ndispatchercode=" + objBioThirdPartyReturn.getNdispatchercode() + ", ncouriercode="
					+ objBioThirdPartyReturn.getNcouriercode() + ", jsondata=jsondata || '{\"sdispatchername\": \""
					+ objBioThirdPartyReturn.getSdispatchername() + "\", \"sstorageconditionname\": \""
					+ objBioThirdPartyReturn.getSstorageconditionname() + "\", \"scouriername\": \""
					+ objBioThirdPartyReturn.getScouriername() + "\", \"scourierno\": \""
					+ stringUtilityFunction.replaceQuote(objBioThirdPartyReturn.getScourierno())
					+ "\", \"striplepackage\": \""
					+ stringUtilityFunction.replaceQuote(objBioThirdPartyReturn.getStriplepackage())
					+ "\", \"svalidationremarks\": \""
					+ stringUtilityFunction.replaceQuote(objBioThirdPartyReturn.getSvalidationremarks())
					+ "\"}', ntransactionstatus=" + Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()
					+ ", dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ " ntztransactiondate=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final String strUpdateThirdPartyReturnDetails = "update biothirdpartyreturndetails set nsamplestatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + concatCondition + ";";

			int seqNoBioThirdPartyReturnHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyreturnhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioThirdPartyReturnHistory++;
			String strInsertThirdPartyReturnHistory = "insert into biothirdpartyreturnhistory (nbiothirdpartyreturnhistorycode, nbiothirdpartyreturncode,"
					+ " ntransactionstatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) values ("
					+ seqNoBioThirdPartyReturnHistory + ", " + nbioThirdPartyReturnCode + ", "
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			strInsertThirdPartyReturnHistory += "update seqnobiobankmanagement set nsequenceno="
					+ seqNoBioThirdPartyReturnHistory + " where stablename='biothirdpartyreturnhistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			int seqNoBioThirdPartyReturnDetailsHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyreturndetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strInsertThirdPartyReturnDetailsHistory = "insert into biothirdpartyreturndetailshistory"
					+ " (nbiothirdpartyreturndetailshistorycode, nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition,"
					+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) select "
					+ seqNoBioThirdPartyReturnDetailsHistory + "+ rank() over(order by nbiothirdpartyreturndetailscode)"
					+ ", nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition, "
					+ " nsamplestatus, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", nsitecode, "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyreturndetails where" + " nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode
					+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + concatCondition + ";";
			strInsertThirdPartyReturnDetailsHistory += "update seqnobiobankmanagement set nsequenceno=(select "
					+ seqNoBioThirdPartyReturnDetailsHistory + "+ count(nbiothirdpartyreturndetailscode) from"
					+ " biothirdpartyreturndetails where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode
					+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ") where"
					+ " stablename='biothirdpartyreturndetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			int chainCustodyPk = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strChainCustody = "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
					+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
					+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
					+ "select " + chainCustodyPk + "+rank() over(order by btprd.nbiothirdpartyreturndetailscode), "
					+ userInfo.getNformcode() + ","
					+ " btprd.nbiothirdpartyreturndetailscode, 'nbiothirdpartyreturndetailscode', "
					+ " 'biothirdpartyreturndetails', btpr.sthirdpartyreturnformnumber, "
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", " + userInfo.getNusercode()
					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
					+ commonFunction.getMultilingualMessage("IDS_VALIDATEDSAMPLE", userInfo.getSlanguagefilename())
					+ "'||' ['||btprd.srepositoryid||'] '||'"
					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
					+ " '||btpr.sthirdpartyreturnformnumber, " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyreturndetails btprd join biothirdpartyreturn btpr"
					+ " on btpr.nbiothirdpartyreturncode=btprd.nbiothirdpartyreturncode and" + " btpr.nsitecode="
					+ userInfo.getNtranssitecode() + " and btpr.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btprd.nsitecode="
					+ userInfo.getNtranssitecode() + " and btprd.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
					+ " btprd.nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and btprd.nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + concatConditionChainCustody + "; ";

			strChainCustody += " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
					+ "+ count(nbiothirdpartyreturndetailscode) from"
					+ " biothirdpartyreturndetails where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode
					+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + concatCondition
					+ ") where stablename=" + "'chaincustody' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

//			jdbcTemplate.execute(strUpdateThirdPartyReturn + strUpdateThirdPartyReturnDetails
//					+ strInsertThirdPartyReturnHistory + strInsertThirdPartyReturnDetailsHistory + strChainCustody);
			
			jdbcTemplate.execute(strUpdateThirdPartyReturn + strUpdateThirdPartyReturnDetails
					+ strInsertThirdPartyReturnHistory + strInsertThirdPartyReturnDetailsHistory );

			final String concatSelect = ", bbr.nstorageconditioncode, to_char(bbr.ddeliverydate, '"
					+ userInfo.getSsitedate()
					+ "') sdeliverydate, bbr.jsondata->>'sdispatchername' sdispatchername, bbr.ncouriercode, bbr.jsondata->>'scourierno' scourierno,"
					+ " bbr.jsondata->>'striplepackage' striplepackage, bbr.jsondata->>'svalidationremarks' svalidationremarks ";
			final String concatJoin = "";
			final String strAuditQry = auditParentQuery(nbioThirdPartyReturnCode, concatSelect, concatJoin, userInfo);
			List<BioThirdPartyReturn> lstAuditAfter = jdbcTemplate.query(strAuditQry, new BioThirdPartyReturn());

			lstAuditAfter.stream().forEach(x -> multilingualIDList.add("IDS_VALIDATERETURNFORM"));
			savedBioThirdPartyReturnList.addAll(lstAuditAfter);

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);
			return getActiveBioThirdPartyReturn(nbioThirdPartyReturnCode, 1, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public String getToBeDisposeSamples(final int nbioThirdPartyReturnCode, final UserInfo userInfo) throws Exception {
		final String strToBeDisposeSamples = "select string_agg(nbiothirdpartyreturndetailscode::text, ',') from"
				+ " biothirdpartyreturndetails where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode
				+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nsamplestatus="
				+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.queryForObject(strToBeDisposeSamples, String.class);
	}

	public ResponseEntity<Object> returnThirdPartyReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		jdbcTemplate
				.execute("lock table lockbiothirdpartyreturn " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbiothirdpartyreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbiothirdpartyformaccept " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbiothirdpartyformacceptdetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final int nbioThirdPartyReturnCode = (int) inputMap.get("nbiothirdpartyreturncode");
		final int nthirdPartyCode = (int) inputMap.get("nthirdPartyCode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		final List<Object> listBeforeSave = new ArrayList<>();

		final int findStatus = findStatusThirdPartyReturn(nbioThirdPartyReturnCode, userInfo);
		if (findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {
			final boolean availableSamples = checkSamplesAvailableToReturn(nbioThirdPartyReturnCode, userInfo);
			if (availableSamples) {

				final String strAuditQry = auditQuery(nbioThirdPartyReturnCode, "", userInfo);
				List<BioThirdPartyReturn> lstAuditBefore = jdbcTemplate.query(strAuditQry, new BioThirdPartyReturn());

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

				int chainCustodyPk = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);

				final String strBioThirdPartyReturnDetailsCode = getToBeDisposeSamples(nbioThirdPartyReturnCode,
						userInfo);
				String concatConditionThirdPartyReturnDetailsNot = (strBioThirdPartyReturnDetailsCode != ""
						&& strBioThirdPartyReturnDetailsCode != null)
								? " and nbiothirdpartyreturndetailscode not in (" + strBioThirdPartyReturnDetailsCode
										+ ")"
								: "";
				String concatConditionThirdPartyReturnDetailsChainCustodyNot = (strBioThirdPartyReturnDetailsCode != ""
						&& strBioThirdPartyReturnDetailsCode != null)
								? " and btprd.nbiothirdpartyreturndetailscode not in ("
										+ strBioThirdPartyReturnDetailsCode + ")"
								: "";
				String concatConditionThirdPartyReturnDetails = (strBioThirdPartyReturnDetailsCode != ""
						&& strBioThirdPartyReturnDetailsCode != null)
								? " and nbiothirdpartyreturndetailscode in (" + strBioThirdPartyReturnDetailsCode + ")"
								: "";
				String concatConditionThirdPartyReturnDetailsChainCustody = (strBioThirdPartyReturnDetailsCode != ""
						&& strBioThirdPartyReturnDetailsCode != null)
								? " and btprd.nbiothirdpartyreturndetailscode in (" + strBioThirdPartyReturnDetailsCode
										+ ")"
								: "";
				String strUpdateThirdPartyReturnAndDetails = "update biothirdpartyreturn set ntransactionstatus="
						+ Enumeration.TransactionStatus.RETURN.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
						+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				strUpdateThirdPartyReturnAndDetails += "update biothirdpartyreturndetails set nsamplestatus="
						+ Enumeration.TransactionStatus.RETURN.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
						+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
						+ Enumeration.TransactionStatus.STORED.gettransactionstatus()
						+ concatConditionThirdPartyReturnDetailsNot + ";";

				String strChainCustody = "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
						+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
						+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
						+ "select " + chainCustodyPk + "+rank() over(order by btprd.nbiothirdpartyreturndetailscode), "
						+ userInfo.getNformcode() + ","
						+ " btprd.nbiothirdpartyreturndetailscode, 'nbiothirdpartyreturndetailscode', "
						+ " 'biothirdpartyreturndetails', btpr.sthirdpartyreturnformnumber, "
						+ Enumeration.TransactionStatus.RETURN.gettransactionstatus() + ", " + userInfo.getNusercode()
						+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
						+ commonFunction.getMultilingualMessage("IDS_RETURNSAMPLE", userInfo.getSlanguagefilename())
						+ "'||' ['||btprd.srepositoryid||'] '||'"
						+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
						+ " '||btpr.sthirdpartyreturnformnumber, " + userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biothirdpartyreturndetails btprd join biothirdpartyreturn btpr"
						+ " on btpr.nbiothirdpartyreturncode=btprd.nbiothirdpartyreturncode and" + " btpr.nsitecode="
						+ userInfo.getNtranssitecode() + " and btpr.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btprd.nsitecode="
						+ userInfo.getNtranssitecode() + " and btprd.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
						+ " btprd.nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and nsamplestatus != "
						+ Enumeration.TransactionStatus.STORED.gettransactionstatus()
						+ concatConditionThirdPartyReturnDetailsChainCustodyNot + "; ";

				int seqNoBioThirdPartyReturnHistory = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyreturnhistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);
				seqNoBioThirdPartyReturnHistory++;

				String strInsertThirdPartyReturnHistory = "insert into biothirdpartyreturnhistory (nbiothirdpartyreturnhistorycode, nbiothirdpartyreturncode,"
						+ " ntransactionstatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
						+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) values ("
						+ seqNoBioThirdPartyReturnHistory + ", " + nbioThirdPartyReturnCode + ", "
						+ Enumeration.TransactionStatus.RETURN.gettransactionstatus() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

				strInsertThirdPartyReturnHistory += "update seqnobiobankmanagement set nsequenceno="
						+ seqNoBioThirdPartyReturnHistory
						+ " where stablename='biothirdpartyreturnhistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				int seqNoBioThirdPartyReturnDetailsHistory = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyreturndetailshistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);

				String strInsertThirdPartyReturnDetailsHistory = "insert into biothirdpartyreturndetailshistory"
						+ " (nbiothirdpartyreturndetailshistorycode, nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition,"
						+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
						+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) select "
						+ seqNoBioThirdPartyReturnDetailsHistory
						+ "+ rank() over(order by nbiothirdpartyreturndetailscode)"
						+ ", nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition, "
						+ " nsamplestatus, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
						+ userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", nsitecode, "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biothirdpartyreturndetails where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode
						+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
						+ Enumeration.TransactionStatus.STORED.gettransactionstatus()
						+ " order by nbiothirdpartyreturndetailscode;";

				strInsertThirdPartyReturnDetailsHistory += "update seqnobiobankmanagement set nsequenceno=(select "
						+ seqNoBioThirdPartyReturnDetailsHistory + "+ count(nbiothirdpartyreturndetailscode) from"
						+ " biothirdpartyreturndetails where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode
						+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
						+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ") where"
						+ " stablename='biothirdpartyreturndetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				String strBioFormAcceptance = "insert into bioformacceptance (nbioformacceptancecode, nbiodirecttransfercode, "
						+ " nbiorequestbasedtransfercode, nbiobankreturncode, nbiothirdpartyreturncode, nthirdpartycode, "
						+ " sformnumber, ntransfertypecode, nformtypecode, noriginsitecode,"
						+ " nsenderusercode, nsenderuserrolecode, dtransferdate, ntztransferdate, noffsetdtransferdate,"
						+ " ntransactionstatus, nstorageconditioncode, ddeliverydate, ntzdeliverydate,"
						+ " noffsetddeliverydate, ndispatchercode, ncouriercode, "
						+ " jsondata, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode,"
						+ " nstatus) select " + intFormAcceptancePk + ", -1, -1, -1, " + nbioThirdPartyReturnCode + ", "
						+ nthirdPartyCode + ", sthirdpartyreturnformnumber, ntransfertypecode, "
						+ Enumeration.FormType.Return.getnformtype() + ", noriginsitecode, " + userInfo.getNusercode()
						+ ", " + userInfo.getNuserrole() + ", dreturndate, ntzreturndate, noffsetdreturndate, "
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
						+ " nstorageconditioncode, ddeliverydate, ntzdeliverydate, noffsetddeliverydate, ndispatchercode,"
						+ " ncouriercode, jsondata, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
						+ userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", noriginsitecode, " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biothirdpartyreturn where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNtranssitecode() + " order by nbiothirdpartyreturncode;";

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
						+ " from biothirdpartyreturn where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode=" + userInfo.getNtranssitecode() + " order by nbiothirdpartyreturncode;";

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
						+ " ncohortno, nstoragetypecode, nproductcatcode, nproductcode, svolume, sreceivedvolume, ssubjectid, jsondata, "
						+ " ndiagnostictypecode, ncontainertypecode, nbiosamplereceivingcode, nsamplecondition, nsamplestatus,"
						+ " srepositoryid, nreasoncode, slocationcode, nsamplestoragetransactioncode,"
						+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) select "
						+ intFormAcceptanceDetailsPk + "+rank()over(order by nbiothirdpartyreturndetailscode), "
						+ intFormAcceptancePk + ", nbioprojectcode,  nbioparentsamplecode,"
						+ " ncohortno, nstoragetypecode, nproductcatcode, nproductcode, svolume, sreturnvolume, jsondata->>'ssubjectid', jsondata,"
						+ " ndiagnostictypecode, ncontainertypecode, nbiosamplereceivingcode, nsamplecondition, "
						+ " nsamplestatus, srepositoryid, nreasoncode, null, -1, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", (select noriginsitecode from biothirdpartyreturn where nbiothirdpartyreturncode="
						+ nbioThirdPartyReturnCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "), "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biothirdpartyreturndetails where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode
						+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus="
						+ Enumeration.TransactionStatus.RETURN.gettransactionstatus()
						+ " order by nbiothirdpartyreturndetailscode;";

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
						+ " (select noriginsitecode from biothirdpartyreturn where nbiothirdpartyreturncode="
						+ nbioThirdPartyReturnCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "), "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from bioformacceptancedetails where nbioformacceptancecode=" + intFormAcceptancePk
						+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by nbioformacceptancedetailscode;";

				strBioFormAcceptanceDetails += " update seqnobiobankmanagement set nsequenceno=(" + "select "
						+ intFormAcceptanceDetailsPk
						+ " + count(nbiothirdpartyreturndetailscode) from biothirdpartyreturndetails where nbiothirdpartyreturncode="
						+ nbioThirdPartyReturnCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
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

				if (strBioThirdPartyReturnDetailsCode != "" && strBioThirdPartyReturnDetailsCode != null) {

					strUpdateThirdPartyReturnAndDetails += "update biothirdpartyreturndetails set nsamplestatus="
							+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus()
							+ ", dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							+ " ntztransactiondate=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ " where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ concatConditionThirdPartyReturnDetails + ";";

					strChainCustody += "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
							+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
							+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
							+ "select " + "(select " + chainCustodyPk
							+ "+count(nbiothirdpartyreturndetailscode) from biothirdpartyreturndetails where"
							+ " nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
							+ Enumeration.TransactionStatus.STORED.gettransactionstatus()
							+ concatConditionThirdPartyReturnDetailsNot + ")"
							+ "+rank() over(order by btprd.nbiothirdpartyreturndetailscode), " + userInfo.getNformcode()
							+ "," + " btprd.nbiothirdpartyreturndetailscode, 'nbiothirdpartyreturndetailscode', "
							+ " 'biothirdpartyreturndetails', btpr.sthirdpartyreturnformnumber, "
							+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ", "
							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
							+ commonFunction.getMultilingualMessage("IDS_MOVETODISPOSESAMPLE",
									userInfo.getSlanguagefilename())
							+ "'||' ['||btprd.srepositoryid||'] '||'"
							+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
							+ " '||btpr.sthirdpartyreturnformnumber, " + userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " from biothirdpartyreturndetails btprd join biothirdpartyreturn btpr"
							+ " on btpr.nbiothirdpartyreturncode=btprd.nbiothirdpartyreturncode and"
							+ " btpr.nsitecode=" + userInfo.getNtranssitecode() + " and btpr.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btprd.nsitecode="
							+ userInfo.getNtranssitecode() + " and btprd.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
							+ " btprd.nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode
							+ concatConditionThirdPartyReturnDetailsChainCustody + "; ";

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
							+ " select " + intBioMoveToDisposePk + ", sthirdpartyreturnformnumber, ntransfertypecode, "
							+ Enumeration.FormType.Return.getnformtype() + ", nthirdpartycode, "
							+ Enumeration.TransactionStatus.RETURN.gettransactionstatus()
							+ ", noriginsitecode, jsondata->>'sremarks', '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " from biothirdpartyreturn where nsitecode=" + userInfo.getNtranssitecode()
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
							+ " nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + ";";
					strMoveToDispose += " update seqnobiobankmanagement set nsequenceno=" + intBioMoveToDisposePk
							+ " where stablename='biomovetodispose' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					// modified by sujatha ATE_274 by adding 4 new key's in the jsondata clumn
					// bgsi-218
					strMoveToDisposeDetails += "insert into biomovetodisposedetails (nbiomovetodisposedetailscode,"
							+ " nbiomovetodisposecode, nbioprojectcode, nbioparentsamplecode,"
							+ " nsamplestoragetransactioncode, svolume, jsondata, ncohortno, nstoragetypecode,"
							+ " nproductcatcode, nproductcode, nsamplecondition, ndiagnostictypecode, "
							+ " ncontainertypecode, nsamplestatus, nreasoncode, dtransactiondate, "
							+ " ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)" + " select "
							+ intBioMoveToDisposeDetailsPk + "+rank()over(order by nbiothirdpartyreturndetailscode), "
							+ intBioMoveToDisposePk
							+ ", nbioprojectcode, nbioparentsamplecode, nsamplestoragetransactioncode, sreturnvolume,"
							+ " jsonb_build_object('sparentsamplecode', jsondata->>'sparentsamplecode', 'srepositoryid',"
							+ " srepositoryid, 'ssubjectid', jsondata->>'ssubjectid', 'scasetype', jsondata->>'scasetype', 'slocationcode', '',"
							+ " 'sextractedsampleid', jsondata->>'sextractedsampleid', 'sconcentration', jsondata->>'sconcentration', "
							+ " 'sqcplatform', jsondata->>'sqcplatform', 'seluent', jsondata->>'seluent',"
							+ "'"+Enumeration.FormCode.ECATALOGREQUESTAPPROVAL.getFormCode()+"',NULLIF(jsondata->>'"+Enumeration.FormCode.ECATALOGREQUESTAPPROVAL.getFormCode()+"',''),"
							+ "'"+Enumeration.FormCode.REQUESTBASEDTRANSFER.getFormCode()+"',NULLIF(jsondata->>'"+Enumeration.FormCode.REQUESTBASEDTRANSFER.getFormCode()+"',''),"
							+ "'"+Enumeration.FormCode.THIRDPARTYRETURN.getFormCode()+"',NULLIF(jsondata->>'"+Enumeration.FormCode.THIRDPARTYRETURN.getFormCode()+"','')"
							+ "), ncohortno,"
							+ " nstoragetypecode, nproductcatcode, nproductcode, nsamplecondition, ndiagnostictypecode,"
							+ " ncontainertypecode, "
							+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ", nreasoncode, "
							+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ userInfo.getNtimezonecode() + ", "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " from biothirdpartyreturndetails where nsitecode=" + userInfo.getNtranssitecode()
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
							+ " nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode
							+ concatConditionThirdPartyReturnDetails + ";";
					strMoveToDisposeDetails += "update seqnobiobankmanagement set nsequenceno=(select "
							+ intBioMoveToDisposeDetailsPk
							+ "+ count(nbiothirdpartyreturndetailscode) from biothirdpartyreturndetails where nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode
							+ concatConditionThirdPartyReturnDetails + ") where stablename='biomovetodisposedetails'"
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				}

				strChainCustody += " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
						+ "+ count(nbiothirdpartyreturndetailscode) from"
						+ " biothirdpartyreturndetails where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode
						+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
						+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ") where stablename="
						+ "'chaincustody' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ ";";

//				jdbcTemplate.execute(
//						strUpdateThirdPartyReturnAndDetails + strBioFormAcceptance + strBioFormAcceptanceDetails
//								+ strInsertThirdPartyReturnHistory + strInsertThirdPartyReturnDetailsHistory
//								+ strMoveToDispose + strMoveToDisposeDetails + strChainCustody);
				
				jdbcTemplate.execute(
						strUpdateThirdPartyReturnAndDetails + strBioFormAcceptance + strBioFormAcceptanceDetails
								+ strInsertThirdPartyReturnHistory + strInsertThirdPartyReturnDetailsHistory
								+ strMoveToDispose + strMoveToDisposeDetails );
				
				// ===== COC: START =====
				{
				    String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				    jdbcTemplate.execute(sQuery1);

				    String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				    jdbcTemplate.execute(sQuery2);

				    String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				    jdbcTemplate.execute(sQuery3);

				    int chainCustodyPk1 = jdbcTemplate.queryForObject(
				            "select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
				                    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				            Integer.class);

				    String strChainCustody1 = "insert into chaincustody ("
				            + " nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
				            + " ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
				            + " noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
				            + " select " + chainCustodyPk1
				            + " + rank() over(order by btprd.nbiothirdpartyreturndetailscode), "
				            + userInfo.getNformcode() + ", "
				            + " btprd.nbiothirdpartyreturndetailscode, 'nbiothirdpartyreturndetailscode', "
				            + " 'biothirdpartyreturndetails', btpr.sthirdpartyreturnformnumber, "
				            + Enumeration.TransactionStatus.RETURN.gettransactionstatus() + ", "
				            + userInfo.getNusercode() + ", "
				            + userInfo.getNuserrole() + ", '"
				            + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
				            + userInfo.getNtimezonecode() + ", "
				            + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
				            + commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
				            + " [' || btprd.srepositoryid || '] ' || '"
				            + commonFunction.getMultilingualMessage("IDS_SENTFROM", userInfo.getSlanguagefilename())
				            + " [' || sorigin.ssitename || '] ' || '"
				            + commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
				            + " [' || (btprd.jsondata->>'sparentsamplecode') || '] ' || '"
				            + commonFunction.getMultilingualMessage("IDS_SENTTO", userInfo.getSlanguagefilename())
				            + " [' || sdest.ssitename || '] ' || '"
				            + commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
				            + " [' || btpr.sthirdpartyreturnformnumber || ']'"
				            + ", " + userInfo.getNtranssitecode() + ", "
				            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				            + " from biothirdpartyreturndetails btprd"
				            + " join biothirdpartyreturn btpr on btpr.nbiothirdpartyreturncode = btprd.nbiothirdpartyreturncode"
				            + " and btpr.nsitecode = " + userInfo.getNtranssitecode()
				            + " and btpr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				            + " join site sorigin on sorigin.nsitecode = btpr.noriginsitecode"
				            + " join site sdest on sdest.nsitecode = btpr.nsitecode"
				            + " where btprd.nsitecode = " + userInfo.getNtranssitecode()
				            + " and btprd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				            + " and btprd.nbiothirdpartyreturncode = " + nbioThirdPartyReturnCode
				            + " and btprd.nsamplestatus != " + Enumeration.TransactionStatus.STORED.gettransactionstatus()
				            + ";";

				    String strSeqUpdateCOC1 = " update seqnoregistration set nsequenceno=("
				            + "select " + chainCustodyPk1
				            + " + count(nbiothirdpartyreturndetailscode) from biothirdpartyreturndetails"
				            + " where nbiothirdpartyreturncode = " + nbioThirdPartyReturnCode
				            + " and nsitecode = " + userInfo.getNtranssitecode()
				            + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				            + " and nsamplestatus != " + Enumeration.TransactionStatus.STORED.gettransactionstatus()
				            + ") where stablename='chaincustody' and nstatus="
				            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				    jdbcTemplate.execute(strChainCustody1 + strSeqUpdateCOC1);
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
					mailMap.put("nbiothirdpartyreturncode", nbioThirdPartyReturnCode);
					String query = "SELECT sthirdpartyreturnformnumber,noriginsitecode FROM biothirdpartyreturn where nbiothirdpartyreturncode="
							+ nbioThirdPartyReturnCode;
					
					List<BioThirdPartyReturn> forEmailRecords = jdbcTemplate.query(query, new BioThirdPartyReturn());

					String referenceId = forEmailRecords.get(0).getSthirdpartyreturnformnumber();		
					int noriginsitecode = forEmailRecords.get(0).getNoriginsitecode();					
					mailMap.put("ssystemid", referenceId);
					mailMap.put("noriginsitecode", noriginsitecode);
					final UserInfo mailUserInfo = new UserInfo(userInfo);
					mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
					mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
					emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
				}
				
				
				return getActiveBioThirdPartyReturn(nbioThirdPartyReturnCode, 1, userInfo);
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

	public boolean checkSamplesAvailableToReturn(final int nbioThirdPartyReturnCode, UserInfo userInfo)
			throws Exception {
		final String strCheck = "select exists(select from biothirdpartyreturndetails where nbiothirdpartyreturncode="
				+ nbioThirdPartyReturnCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus not in ("
				+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + "))";
		return jdbcTemplate.queryForObject(strCheck, boolean.class);
	}

	public ResponseEntity<Object> cancelThirdPartyReturn(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		jdbcTemplate
				.execute("lock table lockbiothirdpartyreturn " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbiothirdpartyreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final int nbioThirdPartyReturnCode = (int) inputMap.get("nbiothirdpartyreturncode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		final List<Object> listBeforeSave = new ArrayList<>();

		final int recordStatus = findStatusThirdPartyReturn(nbioThirdPartyReturnCode, userInfo);

		if (recordStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| recordStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			final String strAuditQry = auditParentQuery(nbioThirdPartyReturnCode, "", "", userInfo);
			List<BioThirdPartyReturn> lstAuditBefore = jdbcTemplate.query(strAuditQry, new BioThirdPartyReturn());

			final String strCancelThirdPartyReturn = "update biothirdpartyreturn set ntransactionstatus="
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final String strCancelThirdPartyReturnDetails = "update biothirdpartyreturndetails set nsamplestatus="
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			int seqNoBioThirdPartyReturnHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyreturnhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioThirdPartyReturnHistory++;

			String strInsertThirdPartyReturnHistory = "insert into biothirdpartyreturnhistory (nbiothirdpartyreturnhistorycode, nbiothirdpartyreturncode,"
					+ " ntransactionstatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) values ("
					+ seqNoBioThirdPartyReturnHistory + ", " + nbioThirdPartyReturnCode + ", "
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			strInsertThirdPartyReturnHistory += "update seqnobiobankmanagement set nsequenceno="
					+ seqNoBioThirdPartyReturnHistory + " where stablename='biothirdpartyreturnhistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			int seqNoBioThirdPartyReturnDetailsHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyreturndetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strInsertThirdPartyReturnDetailsHistory = "insert into biothirdpartyreturndetailshistory"
					+ " (nbiothirdpartyreturndetailshistorycode, nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition,"
					+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) select "
					+ seqNoBioThirdPartyReturnDetailsHistory + "+ rank() over(order by nbiothirdpartyreturndetailscode)"
					+ ", nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition, "
					+ " nsamplestatus, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", nsitecode, "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyreturndetails where" + " nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode
					+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus()
					+ " order by nbiothirdpartyreturndetailscode;";

			strInsertThirdPartyReturnDetailsHistory += "update seqnobiobankmanagement set nsequenceno=(select "
					+ seqNoBioThirdPartyReturnDetailsHistory + "+ count(nbiothirdpartyreturndetailscode) from"
					+ " biothirdpartyreturndetailshistory where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode
					+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") where"
					+ " stablename='biothirdpartyreturndetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			int chainCustodyPk = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strChainCustody = "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
					+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
					+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
					+ "select " + chainCustodyPk + "+rank() over(order by btprd.nbiothirdpartyreturndetailscode), "
					+ userInfo.getNformcode() + ","
					+ " btprd.nbiothirdpartyreturndetailscode, 'nbiothirdpartyreturndetailscode', "
					+ " 'biothirdpartyreturndetails', btpr.sthirdpartyreturnformnumber, "
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", " + userInfo.getNusercode()
					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
					+ commonFunction.getMultilingualMessage("IDS_CANCELSAMPLE", userInfo.getSlanguagefilename())
					+ "'||' ['||btprd.srepositoryid||'] '||'"
					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
					+ " '||btpr.sthirdpartyreturnformnumber, " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyreturndetails btprd join biothirdpartyreturn btpr"
					+ " on btpr.nbiothirdpartyreturncode=btprd.nbiothirdpartyreturncode and" + " btpr.nsitecode="
					+ userInfo.getNtranssitecode() + " and btpr.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btprd.nsitecode="
					+ userInfo.getNtranssitecode() + " and btprd.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
					+ " btprd.nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + "; ";

			strChainCustody += " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
					+ "+ count(nbiothirdpartyreturndetailscode) from"
					+ " biothirdpartyreturndetails where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode
					+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ") where stablename="
					+ "'chaincustody' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strCancelThirdPartyReturn + strCancelThirdPartyReturnDetails
					+ strInsertThirdPartyReturnHistory + strInsertThirdPartyReturnDetailsHistory + strChainCustody);

			List<BioBankReturn> lstAuditAfter = jdbcTemplate.query(strAuditQry, new BioBankReturn());

			lstAuditAfter.stream().forEach(x -> multilingualIDList.add("IDS_CANCELRETURNFORM"));
			listBeforeSave.addAll(lstAuditBefore);
			listAfterSave.addAll(lstAuditAfter);

			auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);

			return getActiveBioThirdPartyReturn(nbioThirdPartyReturnCode, 1, userInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> addThirdPartyReturnDetails(final int nbioThirdPartyReturnCode,
			final int noriginSiteCode, final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final int recordStatus = findStatusThirdPartyReturn(nbioThirdPartyReturnCode, userInfo);

		if (recordStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| recordStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			List<Map<String, Object>> lstExternalInternalSampleDetails = getExternalInternalSampleDetails(
					nbioThirdPartyReturnCode, "", userInfo);

			final boolean boolReturn = lstExternalInternalSampleDetails.stream().anyMatch(
					m -> (int) m.get("nisexternalsample") == Enumeration.TransactionStatus.YES.gettransactionstatus());

			if (boolReturn) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTADDSAMPLESTOEXTERNALFORM",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
			outputMap.putAll((Map<String, Object>) getFormNumberDetails(noriginSiteCode, userInfo).getBody());

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> createChildBioThirdPartyReturn(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		jdbcTemplate
				.execute("lock table lockbiothirdpartyreturn " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbiothirdpartyreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final int nbioThirdPartyReturnCode = (int) inputMap.get("nbiothirdpartyreturncode");

		final int saveType = inputMap.containsKey("saveType") ? Integer.valueOf(inputMap.get("saveType").toString())
				: 1;

		int findStatus = findStatusThirdPartyReturn(nbioThirdPartyReturnCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			List<Map<String, Object>> filteredSampleReceiving = getNotExistSamplesSampleReceivingCode(
					(List<Map<String, Object>>) inputMap.get("filterSelectedSamples"), userInfo);

			if (filteredSampleReceiving.size() == 0) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYEXISTS",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final int nbioThirdPartyFormAcceptanceCode = Integer
					.valueOf(inputMap.get("nbiothirdpartyformacceptancecode").toString());

			Map<String, Object> getMap = insertBioThirdPartyReturnDetails(nbioThirdPartyFormAcceptanceCode,
					filteredSampleReceiving, nbioThirdPartyReturnCode, userInfo,null);

			final String rtnQry = (String) getMap.get("queryString");
			final String sbioThirdPartyReturnDetailsCode = (String) getMap.get("sbioThirdPartyReturnDetailsCode");

			String strUpdateStatus = "update biothirdpartyreturn set ntransactionstatus="
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			int seqNoBioThirdPartyReturnHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyreturnhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioThirdPartyReturnHistory++;

			String strInsertThirdPartyReturnHistory = "insert into biothirdpartyreturnhistory (nbiothirdpartyreturnhistorycode, nbiothirdpartyreturncode,"
					+ " ntransactionstatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) values ("
					+ seqNoBioThirdPartyReturnHistory + ", " + nbioThirdPartyReturnCode + ", "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			strInsertThirdPartyReturnHistory += "update seqnobiobankmanagement set nsequenceno="
					+ seqNoBioThirdPartyReturnHistory + " where stablename='biothirdpartyreturnhistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(rtnQry + strUpdateStatus + strInsertThirdPartyReturnHistory);

			final List<Object> lstAuditAfter = new ArrayList<>();
			final List<String> multilingualIDList = new ArrayList<>();
			final String strAuditQry = auditQuery(nbioThirdPartyReturnCode, sbioThirdPartyReturnDetailsCode, userInfo);
			List<BioThirdPartyReturn> lstAuditReturnDetailsAfter = jdbcTemplate.query(strAuditQry,
					new BioThirdPartyReturn());
			lstAuditAfter.addAll(lstAuditReturnDetailsAfter);
			lstAuditReturnDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_ADDRETURNSAMPLES"));
			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);
			return getActiveBioThirdPartyReturn(nbioThirdPartyReturnCode, saveType, userInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> acceptRejectThirdPartyReturnSlide(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		int nbioThirdPartyReturnCode = Integer.valueOf(inputMap.get("nbiothirdpartyreturncode").toString());
		List<Map<String, Object>> addedChildBioThirdPartyReturn = (List<Map<String, Object>>) inputMap
				.get("addedChildBioThirdPartyReturn");
		int findStatus = findStatusThirdPartyReturn(nbioThirdPartyReturnCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			if (addedChildBioThirdPartyReturn.size() > 0) {
				final String sbioThirdPartyReturnDetailsCode = addedChildBioThirdPartyReturn.stream()
						.map(x -> String.valueOf(x.get("nbiothirdpartyreturndetailscode")))
						.collect(Collectors.joining(","));
				List<Integer> lstSamples = getNonDeletedSamples(sbioThirdPartyReturnDetailsCode, userInfo);
				if (lstSamples.size() == 0) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYDELETED",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

				final String concatString = " and nsamplestatus != "
						+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + "";

				final String validatedThirdPartyReturnDetailsCode = findStatusThirdPartyReturnDetails(
						nbioThirdPartyReturnCode, sbioThirdPartyReturnDetailsCode, concatString, userInfo);

				if (validatedThirdPartyReturnDetailsCode == null) {
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

		jdbcTemplate.execute(
				"lock table lockbiothirdpartyreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final int nbioThirdPartyReturnCode = (int) inputMap.get("nbiothirdpartyreturncode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final int findStatus = findStatusThirdPartyReturn(nbioThirdPartyReturnCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			final String nbioThirdPartyReturnDetailsCode = (String) inputMap.get("nbiothirdpartyreturndetailscode");
			final int nreasonCode = (int) inputMap.get("nreasoncode");
			final int nsampleCondition = (int) inputMap.get("nsamplecondition");

			List<Integer> lstSamples = getNonDeletedSamples(nbioThirdPartyReturnDetailsCode, userInfo);
			if (lstSamples.size() == 0) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final String concatString = " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + "";

			final String validatedThirdPartyReturnDetailsCode = findStatusThirdPartyReturnDetails(
					nbioThirdPartyReturnCode, nbioThirdPartyReturnDetailsCode, concatString, userInfo);

			if (validatedThirdPartyReturnDetailsCode == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTSAMPLES",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final String concatQry = " and bbrd.nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus();
			final String strAuditAfterQry = childAuditQuery(nbioThirdPartyReturnCode, nbioThirdPartyReturnDetailsCode,
					concatQry, userInfo);
			List<BioThirdPartyReturnDetails> lstAuditReturnDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
					new BioThirdPartyReturnDetails());

			final String strUpdateQry = "update biothirdpartyreturndetails set nreasoncode=" + nreasonCode
					+ ", nsamplecondition=" + nsampleCondition + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyreturndetailscode in (" + nbioThirdPartyReturnDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ";";

			int seqNoBioThirdPartyReturnDetailsHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyreturndetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			String strInsertThirdPartyReturnDetailsHistory = "insert into biothirdpartyreturndetailshistory"
					+ " (nbiothirdpartyreturndetailshistorycode, nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition,"
					+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) select "
					+ seqNoBioThirdPartyReturnDetailsHistory + "+ rank() over(order by nbiothirdpartyreturndetailscode)"
					+ ", nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", nsitecode, "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyreturndetails where" + " nbiothirdpartyreturndetailscode in ("
					+ nbioThirdPartyReturnDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsamplestatus != " + Enumeration.TransactionStatus.STORED.gettransactionstatus()
					+ " order by nbiothirdpartyreturndetailscode;";

			strInsertThirdPartyReturnDetailsHistory += "update seqnobiobankmanagement set nsequenceno=(select "
					+ seqNoBioThirdPartyReturnDetailsHistory + "+ count(nbiothirdpartyreturndetailscode) from"
					+ " biothirdpartyreturndetailshistory where nbiothirdpartyreturndetailscode in ("
					+ nbioThirdPartyReturnDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") where"
					+ " stablename='biothirdpartyreturndetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			int chainCustodyPk = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			final String idsStatus = (nsampleCondition == Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus())
					? "IDS_ACCEPTEDSAMPLE"
					: "IDS_REJECTEDSAMPLE";

			String strChainCustody = "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
					+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
					+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
					+ "select " + chainCustodyPk + "+rank() over(order by btprd.nbiothirdpartyreturndetailscode), "
					+ userInfo.getNformcode() + ","
					+ " btprd.nbiothirdpartyreturndetailscode, 'nbiothirdpartyreturndetailscode', "
					+ " 'biothirdpartyreturndetails', btpr.sthirdpartyreturnformnumber, " + nsampleCondition + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
					+ commonFunction.getMultilingualMessage(idsStatus, userInfo.getSlanguagefilename())
					+ "'||' ['||btprd.srepositoryid||'] '||'"
					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
					+ " '||btpr.sthirdpartyreturnformnumber, " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyreturndetails btprd join biothirdpartyreturn btpr"
					+ " on btpr.nbiothirdpartyreturncode=btprd.nbiothirdpartyreturncode and" + " btpr.nsitecode="
					+ userInfo.getNtranssitecode() + " and btpr.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btprd.nsitecode="
					+ userInfo.getNtranssitecode() + " and btprd.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
					+ " btprd.nbiothirdpartyreturndetailscode in (" + nbioThirdPartyReturnDetailsCode + ")"
					+ " and nsamplestatus != " + Enumeration.TransactionStatus.STORED.gettransactionstatus() + "; ";

			strChainCustody += " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
					+ "+ count(nbiothirdpartyreturndetailscode) from"
					+ " biothirdpartyreturndetails where nbiothirdpartyreturndetailscode in ("
					+ nbioThirdPartyReturnDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsamplestatus != " + Enumeration.TransactionStatus.STORED.gettransactionstatus()
					+ ") where stablename='chaincustody' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strUpdateQry + strInsertThirdPartyReturnDetailsHistory + strChainCustody);

			List<BioThirdPartyReturnDetails> lstAuditReturnDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
					new BioThirdPartyReturnDetails());
			lstAuditBefore.addAll(lstAuditReturnDetailsBefore);
			lstAuditAfter.addAll(lstAuditReturnDetailsAfter);
			lstAuditReturnDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_SAMPLECONDITION"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioThirdPartyReturn = getChildInitialGet(nbioThirdPartyReturnCode,
					userInfo);
			outputMap.put("lstChildBioThirdPartyReturn", lstChildBioThirdPartyReturn);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> deleteChildThirdPartyReturn(final int nbioThirdPartyReturnCode,
			final String nbioThirdPartyReturnDetailsCode, final UserInfo userInfo) throws Exception {

		jdbcTemplate.execute(
				"lock table lockbiothirdpartyreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();

		final int findStatus = findStatusThirdPartyReturn(nbioThirdPartyReturnCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			List<Integer> lstSamples = getNonDeletedSamples(nbioThirdPartyReturnDetailsCode, userInfo);
			if (lstSamples.size() == 0) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final String concatString = " and nsamplestatus in ("
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", "
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ")";

			final String validatedThirdPartyReturnDetailsCode = findStatusThirdPartyReturnDetails(
					nbioThirdPartyReturnCode, nbioThirdPartyReturnDetailsCode, concatString, userInfo);

			if (validatedThirdPartyReturnDetailsCode == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDSAMPLESTODELETE",
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}

			List<Map<String, Object>> lstExternalInternalSampleDetails = getExternalInternalSampleDetails(
					nbioThirdPartyReturnCode, nbioThirdPartyReturnDetailsCode, userInfo);

			String sbioThirdPartyReturnDetailsCode = lstExternalInternalSampleDetails.stream().filter(
					m -> (int) m.get("nisexternalsample") == Enumeration.TransactionStatus.NO.gettransactionstatus())
					.map(lst -> String.valueOf(lst.get("nbiothirdpartyreturndetailscode")))
					.collect(Collectors.joining(","));

			if (sbioThirdPartyReturnDetailsCode.isEmpty()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANNOTDELETEEXTERNALSAMPLES",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final String concatQry = " and bbrd.nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus();
			final String strAuditBeforeQry = childAuditQuery(nbioThirdPartyReturnCode, sbioThirdPartyReturnDetailsCode,
					concatQry, userInfo);
			List<BioThirdPartyReturnDetails> lstAuditReturnDetailsBefore = jdbcTemplate.query(strAuditBeforeQry,
					new BioThirdPartyReturnDetails());

			final String strDeleteQry = "update biothirdpartyreturndetails set nstatus="
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyreturndetailscode in (" + sbioThirdPartyReturnDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ";";

			int seqNoBioThirdPartyReturnDetailsHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyreturndetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strInsertThirdPartyReturnDetailsHistory = "insert into biothirdpartyreturndetailshistory"
					+ " (nbiothirdpartyreturndetailshistorycode, nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition,"
					+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
					+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) select "
					+ seqNoBioThirdPartyReturnDetailsHistory + "+ rank() over(order by nbiothirdpartyreturndetailscode)"
					+ ", nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", nsitecode, "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyreturndetails where nbiothirdpartyreturndetailscode in ("
					+ sbioThirdPartyReturnDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by nbiothirdpartyreturndetailscode;";

			strInsertThirdPartyReturnDetailsHistory += "update seqnobiobankmanagement set nsequenceno=(select "
					+ seqNoBioThirdPartyReturnDetailsHistory + "+ count(nbiothirdpartyreturndetailscode) from"
					+ " biothirdpartyreturndetailshistory where nbiothirdpartyreturndetailscode in ("
					+ sbioThirdPartyReturnDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") where"
					+ " stablename='biothirdpartyreturndetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			int chainCustodyPk = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strChainCustody = "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
					+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
					+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
					+ "select " + chainCustodyPk + "+rank() over(order by btprd.nbiothirdpartyreturndetailscode), "
					+ userInfo.getNformcode() + ","
					+ " btprd.nbiothirdpartyreturndetailscode, 'nbiothirdpartyreturndetailscode', "
					+ " 'biothirdpartyreturndetails', btpr.sthirdpartyreturnformnumber, 53, " + userInfo.getNusercode()
					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
					+ commonFunction.getMultilingualMessage("IDS_DELETESAMPLE", userInfo.getSlanguagefilename())
					+ "'||' ['||btprd.srepositoryid||'] '||'"
					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
					+ " '||btpr.sthirdpartyreturnformnumber, " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyreturndetails btprd join biothirdpartyreturn btpr"
					+ " on btpr.nbiothirdpartyreturncode=btprd.nbiothirdpartyreturncode and" + " btpr.nsitecode="
					+ userInfo.getNtranssitecode() + " where btprd.nsitecode=" + userInfo.getNtranssitecode() + " and"
					+ " btprd.nbiothirdpartyreturndetailscode in (" + sbioThirdPartyReturnDetailsCode + ")"
					+ " and btprd.nsamplestatus != " + Enumeration.TransactionStatus.STORED.gettransactionstatus()
					+ "; ";

			strChainCustody += " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
					+ "+ count(nbiothirdpartyreturndetailscode) from"
					+ " biothirdpartyreturndetails where nbiothirdpartyreturndetailscode in ("
					+ sbioThirdPartyReturnDetailsCode + ") and nsamplestatus != "
					+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + ") where stablename=" + "'chaincustody' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strDeleteQry + strInsertThirdPartyReturnDetailsHistory + strChainCustody);

			lstAuditBefore.addAll(lstAuditReturnDetailsBefore);
			lstAuditReturnDetailsBefore.stream().forEach(x -> multilingualIDList.add("IDS_DELETERETURNSAMPLES"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditBefore, 1, null, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioThirdPartyReturn = getChildInitialGet(nbioThirdPartyReturnCode,
					userInfo);
			outputMap.put("lstChildBioThirdPartyReturn", lstChildBioThirdPartyReturn);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> disposeSamples(final int nbioThirdPartyReturnCode,
			final String nbioThirdPartyReturnDetailsCode, final UserInfo userInfo) throws Exception {

		jdbcTemplate.execute(
				"lock table lockbiothirdpartyreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final int findStatus = findStatusThirdPartyReturn(nbioThirdPartyReturnCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {
			final String strFindChildStatus = "select exists (select nbiothirdpartyreturndetailscode from biothirdpartyreturndetails where nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
					+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus()
					+ " and nbiothirdpartyreturndetailscode in " + "(" + nbioThirdPartyReturnDetailsCode + ")" + ")";
			boolean findChildStatus = jdbcTemplate.queryForObject(strFindChildStatus, boolean.class);
			if (findChildStatus) {

				List<Integer> lstSamples = getNonDeletedSamples(nbioThirdPartyReturnDetailsCode, userInfo);
				if (lstSamples.size() == 0) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYDELETED",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

				final String concatString = " and nsamplestatus in ("
						+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ")";

				final String validatedThirdPartyReturnDetailsCode = findStatusThirdPartyReturnDetails(
						nbioThirdPartyReturnCode, nbioThirdPartyReturnDetailsCode, concatString, userInfo);

				if (validatedThirdPartyReturnDetailsCode == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDSAMPLES",
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}

				final String strDeleteQry = "update biothirdpartyreturndetails set nsamplestatus="
						+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
						+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " where nbiothirdpartyreturndetailscode in (" + nbioThirdPartyReturnDetailsCode
						+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nsamplestatus != "
						+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				int seqNoBioThirdPartyReturnDetailsHistory = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyreturndetailshistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);

				String strInsertThirdPartyReturnDetailsHistory = "insert into biothirdpartyreturndetailshistory"
						+ " (nbiothirdpartyreturndetailshistorycode, nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition,"
						+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
						+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) select "
						+ seqNoBioThirdPartyReturnDetailsHistory
						+ "+ rank() over(order by nbiothirdpartyreturndetailscode)"
						+ ", nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition, nsamplestatus, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", nsitecode, "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biothirdpartyreturndetails where" + " nbiothirdpartyreturndetailscode in ("
						+ nbioThirdPartyReturnDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by nbiothirdpartyreturndetailscode;";

				strInsertThirdPartyReturnDetailsHistory += "update seqnobiobankmanagement set nsequenceno=(select "
						+ seqNoBioThirdPartyReturnDetailsHistory + "+ count(nbiothirdpartyreturndetailscode) from"
						+ " biothirdpartyreturndetails where nbiothirdpartyreturndetailscode in ("
						+ nbioThirdPartyReturnDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") where"
						+ " stablename='biothirdpartyreturndetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				int chainCustodyPk = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);

				String strChainCustody = "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
						+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
						+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
						+ "select " + chainCustodyPk + "+rank() over(order by btprd.nbiothirdpartyreturndetailscode), "
						+ userInfo.getNformcode() + ","
						+ " btprd.nbiothirdpartyreturndetailscode, 'nbiothirdpartyreturndetailscode', "
						+ " 'biothirdpartyreturndetails', btpr.sthirdpartyreturnformnumber, "
						+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
						+ commonFunction.getMultilingualMessage("IDS_TOBEDISPOSEDSAMPLE",
								userInfo.getSlanguagefilename())
						+ "'||' ['||btprd.srepositoryid||'] '||'"
						+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
						+ " '||btpr.sthirdpartyreturnformnumber, " + userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biothirdpartyreturndetails btprd join biothirdpartyreturn btpr"
						+ " on btpr.nbiothirdpartyreturncode=btprd.nbiothirdpartyreturncode and" + " btpr.nsitecode="
						+ userInfo.getNtranssitecode() + " and btpr.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btprd.nsitecode="
						+ userInfo.getNtranssitecode() + " and btprd.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
						+ " btprd.nbiothirdpartyreturndetailscode in (" + nbioThirdPartyReturnDetailsCode + ")"
						+ " and nsamplestatus != " + Enumeration.TransactionStatus.STORED.gettransactionstatus() + "; ";

				strChainCustody += " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
						+ "+ count(nbiothirdpartyreturndetailscode) from"
						+ " biothirdpartyreturndetails where nbiothirdpartyreturndetailscode in ("
						+ nbioThirdPartyReturnDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsamplestatus != " + Enumeration.TransactionStatus.STORED.gettransactionstatus()
						+ ") where stablename=" + "'chaincustody' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				jdbcTemplate.execute(strDeleteQry + strInsertThirdPartyReturnDetailsHistory + strChainCustody);

				final String concatQry = " and bbrd.nsamplestatus != "
						+ Enumeration.TransactionStatus.STORED.gettransactionstatus();
				final String strAuditAfterQry = childAuditQuery(nbioThirdPartyReturnCode,
						nbioThirdPartyReturnDetailsCode, concatQry, userInfo);
				List<BioThirdPartyReturnDetails> lstAuditReturnDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
						new BioThirdPartyReturnDetails());

				lstAuditAfter.addAll(lstAuditReturnDetailsAfter);
				lstAuditReturnDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_DISPOSERETURNSAMPLES"));

				auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);

				List<Map<String, Object>> lstChildBioThirdPartyReturn = getChildInitialGet(nbioThirdPartyReturnCode,
						userInfo);
				outputMap.put("lstChildBioThirdPartyReturn", lstChildBioThirdPartyReturn);
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

	public String auditQuery(final int nbioThirdPartyReturnCode, final String sbioThirdPartyReturnDetailsCode,
			final UserInfo userInfo) throws Exception {
		String strConcat = "";
		if (!sbioThirdPartyReturnDetailsCode.isEmpty() && sbioThirdPartyReturnDetailsCode != "") {
			strConcat = " and bbrd.nbiothirdpartyreturndetailscode in (" + sbioThirdPartyReturnDetailsCode + ")";
		}
		final String strAuditQry = "select bbr.nbiothirdpartyreturncode, bbr.sthirdpartyreturnformnumber, bbr.nformtypecode,"
				+ " bbr.noriginsitecode, bbr.jsondata->>'soriginsitename' soriginsitename, to_char(bbr.dreturndate, '"
				+ userInfo.getSsitedate()
				+ "') sreturndate, bbr.ntransactionstatus, bbr.jsondata->>'sremarks' sremarks,"
				+ " bbr.nstorageconditioncode, to_char(bbr.ddeliverydate, '" + userInfo.getSsitedate()
				+ "') sdeliverydate,"
				+ " bbr.ndispatchercode, bbr.jsondata->>'sdispatchername' sdispatchername, bbr.ncouriercode,"
				+ " bbr.jsondata->>'scourierno' scourierno, bbr.jsondata->>'striplepackage' striplepackage,"
				+ " bbr.jsondata->>'svalidationremarks' svalidationremarks, bbrd.srepositoryid, bbrd.nproductcode, bbrd.svolume,"
				+ " bbrd.sreturnvolume, bbrd.nsamplecondition, bbrd.nsamplestatus from biothirdpartyreturn bbr "
				+ " join biothirdpartyreturndetails bbrd on bbrd.nbiothirdpartyreturncode=bbr.nbiothirdpartyreturncode"
				+ " and bbrd.nsitecode=" + userInfo.getNtranssitecode() + " and bbrd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bbr.nsitecode="
				+ userInfo.getNtranssitecode() + " and bbr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbr.nbiothirdpartyreturncode="
				+ nbioThirdPartyReturnCode + strConcat + " order by bbr.nbiothirdpartyreturncode;";
		return strAuditQry;
	}

	public String auditParentQuery(final int nbioThirdPartyReturnCode, final String concatSelect,
			final String concatJoin, final UserInfo userInfo) throws Exception {
		final String strAuditQry = "select bbr.nbiothirdpartyreturncode, bbr.sthirdpartyreturnformnumber, bbr.noriginsitecode,"
				+ " to_char(bbr.dreturndate, '" + userInfo.getSsitedate() + "') sreturndate, bbr.ntransactionstatus"
				+ concatSelect + " from biothirdpartyreturn bbr " + concatJoin + " where bbr.nsitecode="
				+ userInfo.getNtranssitecode() + " and bbr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbr.nbiothirdpartyreturncode="
				+ nbioThirdPartyReturnCode;
		return strAuditQry;
	}

	public String childAuditQuery(final int nbioThirdPartyReturnCode, final String nbioThirdPartyReturnDetailsCode,
			final String concatQry, final UserInfo userInfo) throws Exception {
		final String strChildAuditQuery = "select bbrd.nbiothirdpartyreturndetailscode, bbrd.nbiothirdpartyreturncode,"
				+ " bbr.sthirdpartyreturnformnumber, bbrd.srepositoryid, bbrd.nproductcode, bbrd.svolume, bbrd.sreturnvolume,"
				+ " bbrd.nsamplecondition, bbrd.nsamplestatus, bbrd.nreasoncode from biothirdpartyreturndetails bbrd join biothirdpartyreturn bbr"
				+ " on bbr.nbiothirdpartyreturncode=bbrd.nbiothirdpartyreturncode and bbr.nsitecode="
				+ userInfo.getNtranssitecode() + " and bbr.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bbrd.nsitecode="
				+ userInfo.getNtranssitecode() + " and bbrd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bbrd.nbiothirdpartyreturncode="
				+ nbioThirdPartyReturnCode + " and bbrd.nbiothirdpartyreturndetailscode in ("
				+ nbioThirdPartyReturnDetailsCode + ") " + concatQry
				+ " order by bbrd.nbiothirdpartyreturndetailscode desc;";
		return strChildAuditQuery;
	}

	public List<Integer> getNonDeletedSamples(final String sbioThirdPartyReturnDetailsCode, UserInfo userInfo)
			throws Exception {

		final String strSamples = "select nbiothirdpartyreturndetailscode from biothirdpartyreturndetails where nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nbiothirdpartyreturndetailscode in (" + sbioThirdPartyReturnDetailsCode
				+ ") and nsamplestatus != " + Enumeration.TransactionStatus.CANCELED.gettransactionstatus();
		List<Integer> lstSamplesCode = jdbcTemplate.queryForList(strSamples, Integer.class);
		return lstSamplesCode;
	}

	public String findStatusThirdPartyReturnDetails(final int nbioThirdPartyReturnCode,
			final String sbioThirdPartyReturnDetailsCode, final String concatString, final UserInfo userInfo)
			throws Exception {
		final String strStatusThirdPartyReturn = "select nbiothirdpartyreturndetailscode from biothirdpartyreturndetails where"
				+ " nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and nbiothirdpartyreturndetailscode in ("
				+ sbioThirdPartyReturnDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + concatString;
		List<Integer> lstStatusThirdPartyReturn = jdbcTemplate.queryForList(strStatusThirdPartyReturn, Integer.class);
		String strThirdPartyReturnDetailsCode = null;
		if (lstStatusThirdPartyReturn.size() > 0) {
			strThirdPartyReturnDetailsCode = lstStatusThirdPartyReturn.stream().map(String::valueOf)
					.collect(Collectors.joining(", "));
		}
		return strThirdPartyReturnDetailsCode;
	}

	public ResponseEntity<Object> undoDisposeSamples(final int nbioThirdPartyReturnCode,
			final String nbioThirdPartyReturnDetailsCode, final UserInfo userInfo) throws Exception {

		jdbcTemplate.execute(
				"lock table lockbiothirdpartyreturndetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final String concatString = " and nsamplestatus in ("
				+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ")";

		final String validatedThirdPartyReturnDetailsCode = findStatusThirdPartyReturnDetails(nbioThirdPartyReturnCode,
				nbioThirdPartyReturnDetailsCode, concatString, userInfo);

		if (validatedThirdPartyReturnDetailsCode != null) {

			List<Integer> lstSamples = getNonDeletedSamples(nbioThirdPartyReturnDetailsCode, userInfo);
			if (lstSamples.size() == 0) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final String strAuditAfterQry = childAuditQuery(nbioThirdPartyReturnCode,
					validatedThirdPartyReturnDetailsCode, "", userInfo);
			List<BioThirdPartyReturnDetails> lstAuditReturnDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
					new BioThirdPartyReturnDetails());

			int thirdPartyReturnHistoryDetailsPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='biothirdpartyreturndetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			String strUpdateQry = "update biothirdpartyreturndetails set nsamplestatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyreturndetailscode in (" + validatedThirdPartyReturnDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			strUpdateQry += "insert into biothirdpartyreturndetailshistory"
					+ " (nbiothirdpartyreturndetailshistorycode, nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode,"
					+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
					+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
					+ "select " + thirdPartyReturnHistoryDetailsPk
					+ "+rank()over(order by nbiothirdpartyreturndetailscode),"
					+ " nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyreturndetails where nbiothirdpartyreturndetailscode in ("
					+ validatedThirdPartyReturnDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by nbiothirdpartyreturndetailscode;";
			strUpdateQry += " update seqnobiobankmanagement set nsequenceno=(" + "select "
					+ thirdPartyReturnHistoryDetailsPk
					+ " + count(nbiothirdpartyreturndetailscode) from biothirdpartyreturndetails where"
					+ " nbiothirdpartyreturndetailscode in (" + validatedThirdPartyReturnDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " where stablename='biothirdpartyreturndetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			int chainCustodyPk = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strChainCustody = "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
					+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
					+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
					+ "select " + chainCustodyPk + "+rank() over(order by btprd.nbiothirdpartyreturndetailscode), "
					+ userInfo.getNformcode() + ","
					+ " btprd.nbiothirdpartyreturndetailscode, 'nbiothirdpartyreturndetailscode', "
					+ " 'biothirdpartyreturndetails', btpr.sthirdpartyreturnformnumber, "
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", " + userInfo.getNusercode()
					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
					+ commonFunction.getMultilingualMessage("IDS_UNDODISPOSESAMPLE", userInfo.getSlanguagefilename())
					+ "'||' ['||btprd.srepositoryid||'] '||'"
					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
					+ " '||btpr.sthirdpartyreturnformnumber, " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyreturndetails btprd join biothirdpartyreturn btpr"
					+ " on btpr.nbiothirdpartyreturncode=btprd.nbiothirdpartyreturncode and" + " btpr.nsitecode="
					+ userInfo.getNtranssitecode() + " and btpr.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btprd.nsitecode="
					+ userInfo.getNtranssitecode() + " and btprd.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
					+ " btprd.nbiothirdpartyreturndetailscode in (" + validatedThirdPartyReturnDetailsCode + "); ";

			strChainCustody += " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
					+ "+ count(nbiothirdpartyreturndetailscode) from"
					+ " biothirdpartyreturndetails where nbiothirdpartyreturndetailscode in ("
					+ validatedThirdPartyReturnDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ") where stablename=" + "'chaincustody' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strUpdateQry + strChainCustody);

			List<BioThirdPartyReturnDetails> lstAuditReturnDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
					new BioThirdPartyReturnDetails());
			lstAuditBefore.addAll(lstAuditReturnDetailsBefore);
			lstAuditAfter.addAll(lstAuditReturnDetailsAfter);
			lstAuditReturnDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_UNDODISPOSESAMPLES"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioThirdPartyReturn = getChildInitialGet(nbioThirdPartyReturnCode,
					userInfo);
			outputMap.put("lstChildBioThirdPartyReturn", lstChildBioThirdPartyReturn);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDISPOSESAMPLES", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getExternalInternalSampleDetails(final int nbiothirdpartyreturncode,
			final String sbiothirdpartyreturndetailscode, final UserInfo userInfo) throws Exception {

		final String strConcat = sbiothirdpartyreturndetailscode.isEmpty() ? ""
				: " and nbiothirdpartyreturndetailscode in (" + sbiothirdpartyreturndetailscode + ") ";
		final List<Map<String, Object>> strGetExternalSamples = jdbcTemplate.queryForList(
				"select nbiothirdpartyreturncode, nbiothirdpartyreturndetailscode, nisexternalsample from biothirdpartyreturndetails"
						+ " where nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + strConcat
						+ " and nbiothirdpartyreturncode=" + nbiothirdpartyreturncode);

		return strGetExternalSamples;
}

}
