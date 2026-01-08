package com.agaramtech.qualis.biobank.service.biothirdpartyformacceptance;

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
import com.agaramtech.qualis.biobank.model.BioThirdPartyFormAccept;
import com.agaramtech.qualis.biobank.model.BioThirdPartyFormAcceptDetails;
import com.agaramtech.qualis.biobank.model.ThirdParty;
import com.agaramtech.qualis.biobank.service.bgsiexternalapi.BGSIExternalApiDAOImpl;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class BioThirdPartyFormAcceptanceDAOImpl implements BioThirdPartyFormAcceptanceDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(BioThirdPartyFormAcceptanceDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final AuditUtilityFunction auditUtilityFunction;
	private final BGSIExternalApiDAOImpl objBGSIExternalApiDAOImpl;

	public ResponseEntity<Object> getBioThirdPartyFormAcceptance(final Map<String, Object> inputMap,
			final int nbioThirdPartyFormAcceptanceCode, final UserInfo userInfo) throws Exception {
		LOGGER.info("getBioThirdPartyFormAcceptance");
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		String fromDate = (String) inputMap.get("fromDate");
		String toDate = (String) inputMap.get("toDate");
		int ntransCode = inputMap.containsKey("ntransCode") ? (int) inputMap.get("ntransCode") : -1;
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
			outputMap.put("lstBioThirdPartyFormAcceptance", null);
			outputMap.put("selectedBioThirdPartyFormAcceptance", null);
			outputMap.put("lstChildBioThirdPartyFormAcceptance", null);
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
				: " and btpfa.nthirdpartycode=" + nthirdPartyCode + " ";

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
		final String strConditionTransCode = ntransCode == 0 ? "" : " and btpfa.ntransactionstatus=" + ntransCode + " ";

		final String strConcatQry = " and btpfa.dtransactiondate" + " between '" + fromDate + "' and '" + toDate + "' "
				+ strConditionTransCode + strConditionThirdPartyCode;
		final String strQuery = getParentBioThirdPartyFormAcceptanceQry(strConcatQry, userInfo);
		final List<BioThirdPartyFormAccept> lstBioThirdPartyFormAcceptance = jdbcTemplate.query(strQuery,
				new BioThirdPartyFormAccept());

		if (!lstBioThirdPartyFormAcceptance.isEmpty()) {
			outputMap.put("lstBioThirdPartyFormAcceptance", lstBioThirdPartyFormAcceptance);
			List<BioThirdPartyFormAccept> lstObjBioThirdPartyFormAcceptance = null;
			if (nbioThirdPartyFormAcceptanceCode == -1) {
				lstObjBioThirdPartyFormAcceptance = lstBioThirdPartyFormAcceptance;
			} else {
				lstObjBioThirdPartyFormAcceptance = lstBioThirdPartyFormAcceptance.stream()
						.filter(x -> x.getNbiothirdpartyformacceptancecode() == nbioThirdPartyFormAcceptanceCode)
						.collect(Collectors.toList());
			}
			outputMap.put("selectedBioThirdPartyFormAcceptance", lstObjBioThirdPartyFormAcceptance.get(0));

			List<Map<String, Object>> lstChildBioThirdPartyFormAcceptance = getChildInitialGet(
					lstObjBioThirdPartyFormAcceptance.get(0).getNbiothirdpartyformacceptancecode(), userInfo);
			outputMap.put("lstChildBioThirdPartyFormAcceptance", lstChildBioThirdPartyFormAcceptance);

			if (inputMap.containsKey(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())) {
				outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						inputMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
			}
		} else {
			outputMap.put("lstBioThirdPartyFormAcceptance", null);
			outputMap.put("selectedBioThirdPartyFormAcceptance", null);
			outputMap.put("lstChildBioThirdPartyFormAcceptance", null);
		}

		outputMap.put("nprimaryKeyBioThirdPartyFormAcceptance",
				inputMap.containsKey("nprimaryKeyBioThirdPartyFormAcceptance")
						? inputMap.get("nprimaryKeyBioThirdPartyFormAcceptance")
						: -1);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public String getParentBioThirdPartyFormAcceptanceQry(String concatString, UserInfo userInfo) throws Exception {
		final String strQry = "select btpfa.nbiothirdpartyformacceptancecode, btpfa.nbiorequestbasedtransfercode,"
				+ " btpfa.sformnumber, btpfa.nthirdpartycode, "
				+ " btpfa.nformtypecode, coalesce(ft.jsondata->'sformtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "', ft.jsondata->'sformtypename'->>'en-US') sformtypename, btpfa.ntransactionstatus,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, btpfa.noriginsitecode,"
				+ " btpfa.jsondata->>'srecipientname' srecipientname, "
				+ " btpfa.jsondata->>'soriginsitename' soriginsitename, cm.scolorhexcode,"
				+ " btpfa.nstorageconditioncode, btpfa.jsondata->>'sstorageconditionname' sstorageconditionname, "
				+ " btpfa.ncouriercode, btpfa.jsondata->>'scouriername' scouriername, btpfa.ndispatchercode,"
				+ " btpfa.jsondata->>'sdispatchername' sdispatchername, " + " coalesce(to_char(btpfa.dtransferdate, '"
				+ userInfo.getSsitedate() + "'), '-') stransferdate, " + " coalesce(to_char(btpfa.ddeliverydate, '"
				+ userInfo.getSsitedate() + "'), '-') sdeliverydate, " + " coalesce(to_char(btpfa.dreceiveddate, '"
				+ userInfo.getSsitedate() + "'), '-') sreceiveddate, "
				+ " btpfa.jsondata->>'sremarks' sremarks, btpfa.jsondata->>'svalidationremarks' svalidationremarks,"
				+ " btpfa.nreceivingtemperaturecode, btpfa.jsondata->>'sreceivingtemperaturename' sreceivingtemperaturename, "
				+ " btpfa.nreceivingofficercode,  btpfa.jsondata->>'sreceivingofficername' sreceivingofficername, "
				+ " btpfa.jsondata->>'scompletionremarks' scompletionremarks, btpfa.jsondata->>'scourierno' scourierno,"
				+ " tp.nisngs from biothirdpartyformaccept btpfa join formtype ft on"
				+ " ft.nformtypecode=btpfa.nformtypecode and ft.nsitecode=" + userInfo.getNmastersitecode()
				+ " and ft.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join transactionstatus ts on" + " ts.ntranscode=btpfa.ntransactionstatus and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join formwisestatuscolor fwsc on"
				+ " fwsc.ntranscode=ts.ntranscode and fwsc.nformcode=" + userInfo.getNformcode()
				+ " and fwsc.nsitecode=" + userInfo.getNmastersitecode() + " and fwsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join colormaster cm on fwsc.ncolorcode=cm.ncolorcode and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join thirdparty tp on tp.nthirdpartycode=btpfa.nthirdpartycode and tp.nsitecode="
				+ userInfo.getNmastersitecode() + " and tp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btpfa.nsitecode="
				+ userInfo.getNtranssitecode() + " and btpfa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + concatString
				+ " order by btpfa.nbiothirdpartyformacceptancecode desc";
		return strQry;
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

	public List<ThirdParty> getThirdParty(UserInfo userInfo) throws Exception {

		final String strThirdParty = "select tp.nthirdpartycode, tp.sthirdpartyname, tp.nisngs "
				+ " from thirdparty tp join thirdpartyusermapping"
				+ " tpum on tpum.nthirdpartycode=tp.nthirdpartycode and tpum.nsitecode=" + userInfo.getNmastersitecode()
				+ " and tpum.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tpum.nusercode=" + userInfo.getNusercode() + " and tpum.nuserrolecode="
				+ userInfo.getNuserrole() + " where tp.nsitecode=" + userInfo.getNmastersitecode() + " and tp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tp.nthirdpartycode > 0 order by tp.nthirdpartycode";
		return jdbcTemplate.query(strThirdParty, new ThirdParty());
	}

	public ResponseEntity<Object> getActiveBioThirdPartyFormAcceptance(final int nbioThirdPartyFormAcceptanceCode,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strConcatQry = " and btpfa.nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode;
		final String strQuery = getParentBioThirdPartyFormAcceptanceQry(strConcatQry, userInfo);
		final BioThirdPartyFormAccept objBioThirdPartyFormAcceptance = (BioThirdPartyFormAccept) jdbcUtilityTemplateFunction
				.queryForObject(strQuery, BioThirdPartyFormAccept.class, jdbcTemplate);

		if (objBioThirdPartyFormAcceptance != null) {
			outputMap.put("selectedBioThirdPartyFormAcceptance", objBioThirdPartyFormAcceptance);
			List<Map<String, Object>> lstChildBioThirdPartyFormAcceptance = getChildInitialGet(
					objBioThirdPartyFormAcceptance.getNbiothirdpartyformacceptancecode(), userInfo);
			outputMap.put("lstChildBioThirdPartyFormAcceptance", lstChildBioThirdPartyFormAcceptance);

		} else {
			outputMap.put("selectedBioThirdPartyDirectTransfer", null);
			outputMap.put("lstChildBioThirdPartyDirectTransfer", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public List<Map<String, Object>> getChildInitialGet(int nbioThirdPartyFormAcceptanceCode, UserInfo userInfo)
			throws Exception {

		final String strChildGet = "select row_number() over(order by btpfad.nbiothirdpartyformacceptancedetailscode desc) as"
				+ " nserialno, btpfad.nbiothirdpartyformacceptancedetailscode, btpfad.nbiothirdpartyformacceptancecode, btpfad.jsondata->>'sparentsamplecode' sparentsamplecode, "
				+ "btpfad.srepositoryid"
				+ ", btpfad.nsamplestoragetransactioncode, concat(p.sproductname, ' (', pc.sproductcatname, ')') sproductname,"
				+ "  btpfad.svolume, btpfad.sreceivedvolume,"
				+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts1.jsondata->'stransdisplaystatus'->>'en-US') ssamplecondition,"
				+ " coalesce(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') ssamplestatus, btpfad.nsamplestatus,"
				+ " btpfad.nsamplecondition, r.sreason, concat(btpfad.jsondata->>'sparentsamplecode', ' | ', btpfad.ncohortno)"
				+ " sparentsamplecodecohortno,COALESCE(NULLIF(BTPFAD.JSONDATA->>'"+Enumeration.FormCode.ECATALOGREQUESTAPPROVAL.getFormCode()+"','-'),'-') AS ecatalogrequestapproval from biothirdpartyformacceptdetails btpfad "
				+ " join product p on p.nproductcode=btpfad.nproductcode and p.nsitecode="
				+ userInfo.getNmastersitecode() + " and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join productcategory pc on pc.nproductcatcode=p.nproductcatcode and pc.nsitecode="
				+ userInfo.getNmastersitecode() + " and pc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join transactionstatus"
				+ " ts1 on btpfad.nsamplecondition=ts1.ntranscode and ts1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join transactionstatus ts2 on"
				+ " btpfad.nsamplestatus=ts2.ntranscode and ts2.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left join reason r on r.nreasoncode=btpfad.nreasoncode and r.nsitecode="
				+ userInfo.getNmastersitecode() + " and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btpfad.nsitecode="
				+ userInfo.getNtranssitecode() + " and btpfad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and btpfad.nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode
				+ " order by nserialno desc";

		return jdbcTemplate.queryForList(strChildGet);
	}

	public ResponseEntity<Object> getReceiveBioThirdPartyFormAcceptanceDetails(int nbioThirdPartyFormAcceptanceCode,
			UserInfo userInfo) throws Exception {

		final int formStatus = findStatusBioThirdPartyFormAcceptance(nbioThirdPartyFormAcceptanceCode, userInfo);
		if (formStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			final String strConcatQry = " and btpfa.nbiothirdpartyformacceptancecode="
					+ nbioThirdPartyFormAcceptanceCode;
			final String strQuery = getParentBioThirdPartyFormAcceptanceQry(strConcatQry, userInfo);
			final BioThirdPartyFormAccept objBioThirdPartyFormAcceptance = (BioThirdPartyFormAccept) jdbcUtilityTemplateFunction
					.queryForObject(strQuery, BioThirdPartyFormAccept.class, jdbcTemplate);
			return new ResponseEntity<>(objBioThirdPartyFormAcceptance, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDRETURNSAMPLES",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public String auditParentQuery(final int nbioThirdPartyFormAcceptanceCode, final String concatSelect,
			final String concatJoin, final UserInfo userInfo) throws Exception {
		final String strAuditQry = "select btpfa.nbiothirdpartyformacceptancecode, btpfa.sformnumber, btpfa.noriginsitecode,"
				+ " btpfa.ntransactionstatus " + concatSelect + " from biothirdpartyformaccept btpfa " + concatJoin
				+ " where btpfa.nsitecode=" + userInfo.getNtranssitecode() + " and btpfa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and btpfa.nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode;
		return strAuditQry;
	}

	public String childAuditQuery(final int nbioThirdPartyFormAcceptanceCode,
			final String nbioThirdPartyFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception {
		final String strChildAuditQuery = "select btpfa.nbiothirdpartyformacceptancecode, btpfad.nbiothirdpartyformacceptancedetailscode,"
				+ " btpfa.sformnumber, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', ts.jsondata->'stransdisplaystatus'->>'en-US') ssamplecondition,"
				+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') ssamplestatus, btpfad.srepositoryid, btpfad.nreasoncode,"
				+ " btpfad.nproductcode, btpfad.nproductcode, btpfad.svolume, btpfad.sreceivedvolume "
				+ " from biothirdpartyformaccept btpfa join biothirdpartyformacceptdetails btpfad on"
				+ " btpfad.nbiothirdpartyformacceptancecode=btpfa.nbiothirdpartyformacceptancecode and btpfad.nsitecode="
				+ userInfo.getNtranssitecode() + " and btpfad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join transactionstatus ts on ts.ntranscode=btpfad.nsamplecondition and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join transactionstatus ts1 on ts1.ntranscode=btpfad.nsamplestatus and ts1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btpfa.nsitecode="
				+ userInfo.getNtranssitecode() + " and btpfa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and btpfa.nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode
				+ " and btpfad.nbiothirdpartyformacceptancedetailscode in (" + nbioThirdPartyFormAcceptanceDetailsCode
				+ ") order by btpfa.nbiothirdpartyformacceptancecode desc;";
		return strChildAuditQuery;
	}

	public ResponseEntity<Object> moveToDisposeSamples(final int nbioThirdPartyFormAcceptanceCode,
			final String nbioThirdPartyFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception {

		jdbcTemplate.execute(
				"lock table lockbiothirdpartyformacceptdetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final String concatString = " and nsamplestatus in ("
				+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.TOBERETURN.gettransactionstatus() + ")";

		final String validatedBioThirdPartyFormAcceptanceDetailsCode = findStatusBioThirdPartyFormAcceptanceDetails(
				nbioThirdPartyFormAcceptanceCode, nbioThirdPartyFormAcceptanceDetailsCode, concatString, userInfo);

		if (validatedBioThirdPartyFormAcceptanceDetailsCode != null) {

			final String strAuditAfterQry = childAuditQuery(nbioThirdPartyFormAcceptanceCode,
					validatedBioThirdPartyFormAcceptanceDetailsCode, userInfo);
			List<BioThirdPartyFormAcceptDetails> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
					new BioThirdPartyFormAcceptDetails());

			int bioThirdPartyFormAcceptanceHistoryDetailsPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='biothirdpartyformacceptdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);

			int chainCustodyPk = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strUpdateQry = "update biothirdpartyformacceptdetails set nsamplestatus="
					+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyformacceptancedetailscode in ("
					+ validatedBioThirdPartyFormAcceptanceDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			String strChainCustody = "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
					+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
					+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
					+ "select " + chainCustodyPk
					+ "+rank() over(order by btpfad.nbiothirdpartyformacceptancedetailscode), "
					+ userInfo.getNformcode() + ","
					+ " btpfad.nbiothirdpartyformacceptancedetailscode, 'nbiothirdpartyformacceptancedetailscode', "
					+ " 'biothirdpartyformacceptdetails', btpfa.sformnumber, "
					+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ", " + userInfo.getNusercode()
					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
					+ commonFunction.getMultilingualMessage("IDS_TOBEDISPOSEDSAMPLE", userInfo.getSlanguagefilename())
					+ "'||' ['||btpfad.srepositoryid||'] '||'"
					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
					+ " '||btpfa.sformnumber, " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyformacceptdetails btpfad join biothirdpartyformaccept btpfa"
					+ " on btpfa.nbiothirdpartyformacceptancecode=btpfad.nbiothirdpartyformacceptancecode and"
					+ " btpfa.nsitecode=" + userInfo.getNtranssitecode() + " and btpfa.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btpfad.nsitecode="
					+ userInfo.getNtranssitecode() + " and btpfad.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
					+ " btpfad.nbiothirdpartyformacceptancedetailscode in ("
					+ validatedBioThirdPartyFormAcceptanceDetailsCode + "); ";
			strChainCustody += " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
					+ "+ count(nbiothirdpartyformacceptancedetailscode) from"
					+ " biothirdpartyformacceptdetails where nbiothirdpartyformacceptancedetailscode in ("
					+ validatedBioThirdPartyFormAcceptanceDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") where stablename="
					+ "'chaincustody' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			strUpdateQry += "insert into biothirdpartyformacceptdetailshistory"
					+ " (nbiothirdpartyformacceptdetailshistorycode, nbiothirdpartyformacceptancedetailscode, nbiothirdpartyformacceptancecode,"
					+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
					+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
					+ "select " + bioThirdPartyFormAcceptanceHistoryDetailsPk
					+ "+rank()over(order by nbiothirdpartyformacceptancedetailscode),"
					+ " nbiothirdpartyformacceptancedetailscode, nbiothirdpartyformacceptancecode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyformacceptdetails where nbiothirdpartyformacceptancedetailscode in ("
					+ validatedBioThirdPartyFormAcceptanceDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by nbiothirdpartyformacceptancedetailscode;";
			strUpdateQry += " update seqnobiobankmanagement set nsequenceno=(" + "select "
					+ bioThirdPartyFormAcceptanceHistoryDetailsPk
					+ " + count(nbiothirdpartyformacceptancedetailscode) from biothirdpartyformacceptdetails where"
					+ " nbiothirdpartyformacceptancedetailscode in (" + validatedBioThirdPartyFormAcceptanceDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " where stablename='biothirdpartyformacceptdetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			//jdbcTemplate.execute(strUpdateQry + strChainCustody);
			jdbcTemplate.execute(strUpdateQry);


			List<BioThirdPartyFormAcceptDetails> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
					new BioThirdPartyFormAcceptDetails());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_MOVETODISPOSESAMPLES"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioThirdPartyFormAcceptance = getChildInitialGet(
					nbioThirdPartyFormAcceptanceCode, userInfo);
			outputMap.put("lstChildBioThirdPartyFormAcceptance", lstChildBioThirdPartyFormAcceptance);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDRETURNSAMPLES",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> moveToReturnSamples(final int nbioThirdPartyFormAcceptanceCode,
			final String nbioThirdPartyFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception {

		jdbcTemplate.execute(
				"lock table lockbiothirdpartyformacceptdetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final String concatString = " and nsamplestatus in ("
				+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ")";

		final String validatedBioThirdPartyFormAcceptanceDetailsCode = findStatusBioThirdPartyFormAcceptanceDetails(
				nbioThirdPartyFormAcceptanceCode, nbioThirdPartyFormAcceptanceDetailsCode, concatString, userInfo);

		if (validatedBioThirdPartyFormAcceptanceDetailsCode != null) {

			final String strAuditAfterQry = childAuditQuery(nbioThirdPartyFormAcceptanceCode,
					validatedBioThirdPartyFormAcceptanceDetailsCode, userInfo);
			List<BioThirdPartyFormAcceptDetails> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
					new BioThirdPartyFormAcceptDetails());

			int bioThirdPartyFormAcceptanceHistoryDetailsPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='biothirdpartyformacceptdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			int chainCustodyPk = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strUpdateQry = "update biothirdpartyformacceptdetails set nsamplestatus="
					+ Enumeration.TransactionStatus.TOBERETURN.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyformacceptancedetailscode in ("
					+ validatedBioThirdPartyFormAcceptanceDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			String strChainCustody = "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
					+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
					+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
					+ "select " + chainCustodyPk
					+ "+rank() over(order by btpfad.nbiothirdpartyformacceptancedetailscode), "
					+ userInfo.getNformcode() + ","
					+ " btpfad.nbiothirdpartyformacceptancedetailscode, 'nbiothirdpartyformacceptancedetailscode', "
					+ " 'biothirdpartyformacceptdetails', btpfa.sformnumber, "
					+ Enumeration.TransactionStatus.TOBERETURN.gettransactionstatus() + ", " + userInfo.getNusercode()
					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
					+ commonFunction.getMultilingualMessage("IDS_TOBERETURNSAMPLE", userInfo.getSlanguagefilename())
					+ "'||' ['||btpfad.srepositoryid||'] '||'"
					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
					+ " '||btpfa.sformnumber, " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyformacceptdetails btpfad join biothirdpartyformaccept btpfa"
					+ " on btpfa.nbiothirdpartyformacceptancecode=btpfad.nbiothirdpartyformacceptancecode and"
					+ " btpfa.nsitecode=" + userInfo.getNtranssitecode() + " and btpfa.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btpfad.nsitecode="
					+ userInfo.getNtranssitecode() + " and btpfad.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
					+ " btpfad.nbiothirdpartyformacceptancedetailscode in ("
					+ validatedBioThirdPartyFormAcceptanceDetailsCode + "); ";
			strChainCustody += " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
					+ "+ count(nbiothirdpartyformacceptancedetailscode) from"
					+ " biothirdpartyformacceptdetails where nbiothirdpartyformacceptancedetailscode in ("
					+ validatedBioThirdPartyFormAcceptanceDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") where stablename="
					+ "'chaincustody' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			strUpdateQry += "insert into biothirdpartyformacceptdetailshistory"
					+ " (nbiothirdpartyformacceptdetailshistorycode, nbiothirdpartyformacceptancedetailscode, nbiothirdpartyformacceptancecode,"
					+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
					+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
					+ "select " + bioThirdPartyFormAcceptanceHistoryDetailsPk
					+ "+rank()over(order by nbiothirdpartyformacceptancedetailscode),"
					+ " nbiothirdpartyformacceptancedetailscode, nbiothirdpartyformacceptancecode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyformacceptdetails where nbiothirdpartyformacceptancedetailscode in ("
					+ validatedBioThirdPartyFormAcceptanceDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by nbiothirdpartyformacceptancedetailscode;";
			strUpdateQry += " update seqnobiobankmanagement set nsequenceno=(" + "select "
					+ bioThirdPartyFormAcceptanceHistoryDetailsPk
					+ " + count(nbiothirdpartyformacceptancedetailscode) from biothirdpartyformacceptdetails where"
					+ " nbiothirdpartyformacceptancedetailscode in (" + validatedBioThirdPartyFormAcceptanceDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " where stablename='biothirdpartyformacceptdetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			//jdbcTemplate.execute(strUpdateQry + strChainCustody);
			jdbcTemplate.execute(strUpdateQry);


			List<BioThirdPartyFormAcceptDetails> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
					new BioThirdPartyFormAcceptDetails());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_MOVETORETURNSAMPLES"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioThirdPartyFormAcceptance = getChildInitialGet(
					nbioThirdPartyFormAcceptanceCode, userInfo);
			outputMap.put("lstChildBioThirdPartyFormAcceptance", lstChildBioThirdPartyFormAcceptance);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDDISPOSEDSAMPLES",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> undoReturnDisposeSamples(final int nbioThirdPartyFormAcceptanceCode,
			final String nbioThirdPartyFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception {

		jdbcTemplate.execute(
				"lock table lockbiothirdpartyformacceptdetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final String concatString = " and nsamplestatus in ("
				+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.TOBERETURN.gettransactionstatus() + ")";

		final String validatedBioThirdPartyFormAcceptanceDetailsCode = findStatusBioThirdPartyFormAcceptanceDetails(
				nbioThirdPartyFormAcceptanceCode, nbioThirdPartyFormAcceptanceDetailsCode, concatString, userInfo);

		if (validatedBioThirdPartyFormAcceptanceDetailsCode != null) {

			final String strAuditAfterQry = childAuditQuery(nbioThirdPartyFormAcceptanceCode,
					validatedBioThirdPartyFormAcceptanceDetailsCode, userInfo);
			List<BioThirdPartyFormAcceptDetails> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
					new BioThirdPartyFormAcceptDetails());

			int bioThirdPartyFormAcceptanceHistoryDetailsPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='biothirdpartyformacceptdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			int chainCustodyPk = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strUpdateQry = "update biothirdpartyformacceptdetails set nsamplestatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyformacceptancedetailscode in ("
					+ validatedBioThirdPartyFormAcceptanceDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			String strChainCustody = "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
					+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
					+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
					+ "select " + chainCustodyPk
					+ "+rank() over(order by btpfad.nbiothirdpartyformacceptancedetailscode), "
					+ userInfo.getNformcode() + ","
					+ " btpfad.nbiothirdpartyformacceptancedetailscode, 'nbiothirdpartyformacceptancedetailscode', "
					+ " 'biothirdpartyformacceptdetails', btpfa.sformnumber, "
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", " + userInfo.getNusercode()
					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
					+ commonFunction.getMultilingualMessage("IDS_UNDODISPOSERETURNSAMPLE",
							userInfo.getSlanguagefilename())
					+ "'||' ['||btpfad.srepositoryid||'] '||'"
					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
					+ " '||btpfa.sformnumber, " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyformacceptdetails btpfad join biothirdpartyformaccept btpfa"
					+ " on btpfa.nbiothirdpartyformacceptancecode=btpfad.nbiothirdpartyformacceptancecode and"
					+ " btpfa.nsitecode=" + userInfo.getNtranssitecode() + " and btpfa.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btpfad.nsitecode="
					+ userInfo.getNtranssitecode() + " and btpfad.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
					+ " btpfad.nbiothirdpartyformacceptancedetailscode in ("
					+ validatedBioThirdPartyFormAcceptanceDetailsCode + "); ";
			strChainCustody += " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
					+ "+ count(nbiothirdpartyformacceptancedetailscode) from"
					+ " biothirdpartyformacceptdetails where nbiothirdpartyformacceptancedetailscode in ("
					+ validatedBioThirdPartyFormAcceptanceDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") where stablename="
					+ "'chaincustody' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			strUpdateQry += "insert into biothirdpartyformacceptdetailshistory"
					+ " (nbiothirdpartyformacceptdetailshistorycode, nbiothirdpartyformacceptancedetailscode, nbiothirdpartyformacceptancecode,"
					+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
					+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
					+ "select " + bioThirdPartyFormAcceptanceHistoryDetailsPk
					+ "+rank()over(order by nbiothirdpartyformacceptancedetailscode),"
					+ " nbiothirdpartyformacceptancedetailscode, nbiothirdpartyformacceptancecode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyformacceptdetails where nbiothirdpartyformacceptancedetailscode in ("
					+ validatedBioThirdPartyFormAcceptanceDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by nbiothirdpartyformacceptancedetailscode;";
			strUpdateQry += " update seqnobiobankmanagement set nsequenceno=(" + "select "
					+ bioThirdPartyFormAcceptanceHistoryDetailsPk
					+ " + count(nbiothirdpartyformacceptancedetailscode) from biothirdpartyformacceptdetails where"
					+ " nbiothirdpartyformacceptancedetailscode in (" + validatedBioThirdPartyFormAcceptanceDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " where stablename='biothirdpartyformacceptdetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			//jdbcTemplate.execute(strUpdateQry + strChainCustody);
			jdbcTemplate.execute(strUpdateQry);


			List<BioThirdPartyFormAcceptDetails> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
					new BioThirdPartyFormAcceptDetails());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_UNDODISPOSERETURNSAMPLES"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioThirdPartyFormAcceptance = getChildInitialGet(
					nbioThirdPartyFormAcceptanceCode, userInfo);
			outputMap.put("lstChildBioThirdPartyFormAcceptance", lstChildBioThirdPartyFormAcceptance);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDISPOSERETURNSAMPLES",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
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
				"lock table lockbiothirdpartyformacceptdetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final int nbioThirdPartyFormAcceptanceCode = (int) inputMap.get("nbiothirdpartyformacceptancecode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final int findStatus = findStatusBioThirdPartyFormAcceptance(nbioThirdPartyFormAcceptanceCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.RECEIVED.gettransactionstatus()) {
			final String nbioThirdPartyFormAcceptanceDetailsCode = (String) inputMap
					.get("nbiothirdpartyformacceptancedetailscode");
			final int nreasonCode = (int) inputMap.get("nreasoncode");
			final int nsampleCondition = (int) inputMap.get("nsamplecondition");

			final String strAuditAfterQry = childAuditQuery(nbioThirdPartyFormAcceptanceCode,
					nbioThirdPartyFormAcceptanceDetailsCode, userInfo);
			List<BioThirdPartyFormAcceptDetails> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
					new BioThirdPartyFormAcceptDetails());

			int bioThirdPartyFormAcceptanceHistoryDetailsPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='biothirdpartyformacceptdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			int chainCustodyPk = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strUpdateQry = "update biothirdpartyformacceptdetails set nreasoncode=" + nreasonCode
					+ ", nsamplecondition=" + nsampleCondition + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyformacceptancedetailscode in (" + nbioThirdPartyFormAcceptanceDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final String idsStatus = (nsampleCondition == Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus())
					? "IDS_ACCEPTEDSAMPLE"
					: "IDS_REJECTEDSAMPLE";
			String strChainCustody = "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
					+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
					+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
					+ "select " + chainCustodyPk
					+ "+rank() over(order by btpfad.nbiothirdpartyformacceptancedetailscode), "
					+ userInfo.getNformcode() + ","
					+ " btpfad.nbiothirdpartyformacceptancedetailscode, 'nbiothirdpartyformacceptancedetailscode', "
					+ " 'biothirdpartyformacceptdetails', btpfa.sformnumber, " + nsampleCondition + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
					+ commonFunction.getMultilingualMessage(idsStatus, userInfo.getSlanguagefilename())
					+ "'||' ['||btpfad.srepositoryid||'] '||'"
					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
					+ " '||btpfa.sformnumber, " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyformacceptdetails btpfad join biothirdpartyformaccept btpfa"
					+ " on btpfa.nbiothirdpartyformacceptancecode=btpfad.nbiothirdpartyformacceptancecode and"
					+ " btpfa.nsitecode=" + userInfo.getNtranssitecode() + " and btpfa.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btpfad.nsitecode="
					+ userInfo.getNtranssitecode() + " and btpfad.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
					+ " btpfad.nbiothirdpartyformacceptancedetailscode in (" + nbioThirdPartyFormAcceptanceDetailsCode
					+ "); ";
			strChainCustody += " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
					+ "+ count(nbiothirdpartyformacceptancedetailscode) from"
					+ " biothirdpartyformacceptdetails where nbiothirdpartyformacceptancedetailscode in ("
					+ nbioThirdPartyFormAcceptanceDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ") where stablename=" + "'chaincustody' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			strUpdateQry += "insert into biothirdpartyformacceptdetailshistory"
					+ " (nbiothirdpartyformacceptdetailshistorycode, nbiothirdpartyformacceptancedetailscode, nbiothirdpartyformacceptancecode,"
					+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
					+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
					+ " select " + bioThirdPartyFormAcceptanceHistoryDetailsPk
					+ "+rank()over(order by nbiothirdpartyformacceptancedetailscode), "
					+ " nbiothirdpartyformacceptancedetailscode, nbiothirdpartyformacceptancecode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyformacceptdetails where nbiothirdpartyformacceptancedetailscode in ("
					+ nbioThirdPartyFormAcceptanceDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			strUpdateQry += "update seqnobiobankmanagement set nsequenceno=(select "
					+ bioThirdPartyFormAcceptanceHistoryDetailsPk
					+ " + count(nbiothirdpartyformacceptancedetailscode) from biothirdpartyformacceptdetails where nbiothirdpartyformacceptancedetailscode in"
					+ " (" + nbioThirdPartyFormAcceptanceDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " where stablename='biothirdpartyformacceptdetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			//jdbcTemplate.execute(strUpdateQry + strChainCustody);
			jdbcTemplate.execute(strUpdateQry);


			List<BioThirdPartyFormAcceptDetails> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
					new BioThirdPartyFormAcceptDetails());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_SAMPLEVALIDATION"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioThirdPartyFormAcceptance = getChildInitialGet(
					nbioThirdPartyFormAcceptanceCode, userInfo);
			outputMap.put("lstChildBioThirdPartyFormAcceptance", lstChildBioThirdPartyFormAcceptance);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
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

	public ResponseEntity<Object> updateReceiveBioThirdPartyFormAcceptance(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		jdbcTemplate.execute(
				"lock table lockbiothirdpartyformaccept " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbiothirdpartyformacceptdetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final ObjectMapper objMapper = new ObjectMapper();

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final int nbioThirdPartyFormAcceptanceCode = (int) inputMap.get("nbiothirdpartyformacceptancecode");
		final int nbioRequestBasedTransferCode = (int) inputMap.get("nbiorequestbasedtransfercode");
		final int noriginSiteCode = (int) inputMap.get("noriginsitecode");
		final int findStatus = findStatusBioThirdPartyFormAcceptance(nbioThirdPartyFormAcceptanceCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			final String concatSelect = ", to_char(btpfa.ddeliverydate, '" + userInfo.getSsitedate()
					+ "') sdeliverydate, btpfa.ncouriercode, btpfa.jsondata->>'srecipientname' srecipientname, "
					+ " to_char(btpfa.dreceiveddate, '" + userInfo.getSsitedate() + "') sreceiveddate ";
			final String strAuditQry = auditParentQuery(nbioThirdPartyFormAcceptanceCode, concatSelect, "", userInfo);

			List<BioThirdPartyFormAccept> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditQry,
					new BioThirdPartyFormAccept());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);

			BioThirdPartyFormAccept objBioThirdPartyFormAcceptance = objMapper
					.convertValue(inputMap.get("bioThirdPartyFormAcceptance"), BioThirdPartyFormAccept.class);

			final String sreceivedDate = (objBioThirdPartyFormAcceptance.getSreceiveddate() != null
					&& !objBioThirdPartyFormAcceptance.getSreceiveddate().isEmpty())
							? "'" + objBioThirdPartyFormAcceptance.getSreceiveddate().toString().replace("T", " ")
									.replace("Z", "") + "'"
							: null;

			int bioThirdPartyFormAcceptanceHistoryPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='biothirdpartyformaccepthistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			bioThirdPartyFormAcceptanceHistoryPk++;
			int bioThirdPartyFormAcceptanceHistoryDetailsPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='biothirdpartyformacceptdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			int chainCustodyPk = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			final String strUpdateBioThirdPartyFormAcceptance = "update biothirdpartyformaccept set dreceiveddate="
					+ sreceivedDate + ", ntzreceiveddate=" + userInfo.getNtimezonecode() + " , noffsetdreceiveddate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ ", jsondata=jsondata || '{" + "\"srecipientname\": \""
					+ stringUtilityFunction.replaceQuote(objBioThirdPartyFormAcceptance.getSrecipientname())
					+ "\"}'::jsonb, ntransactionstatus=" + Enumeration.TransactionStatus.RECEIVED.gettransactionstatus()
					+ ", dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ " ntztransactiondate=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final String strUpdateBioThirdPartyFormAcceptanceDetails = "update biothirdpartyformacceptdetails set nsamplestatus="
					+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			String strChainCustody = "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
					+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
					+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
					+ "select " + chainCustodyPk
					+ "+rank() over(order by btpfad.nbiothirdpartyformacceptancedetailscode), "
					+ userInfo.getNformcode() + ","
					+ " btpfad.nbiothirdpartyformacceptancedetailscode, 'nbiothirdpartyformacceptancedetailscode', "
					+ " 'biothirdpartyformacceptdetails', btpfa.sformnumber, "
					+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", " + userInfo.getNusercode()
					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
					+ commonFunction.getMultilingualMessage("IDS_RECEIVEDSAMPLE", userInfo.getSlanguagefilename())
					+ "'||' ['||btpfad.srepositoryid||'] '||'"
					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
					+ " '||btpfa.sformnumber, " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyformacceptdetails btpfad join biothirdpartyformaccept btpfa"
					+ " on btpfa.nbiothirdpartyformacceptancecode=btpfad.nbiothirdpartyformacceptancecode and"
					+ " btpfa.nsitecode=" + userInfo.getNtranssitecode() + " and btpfa.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btpfad.nsitecode="
					+ userInfo.getNtranssitecode() + " and btpfad.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
					+ " btpfad.nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode + "; ";
			strChainCustody += " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
					+ "+ count(nbiothirdpartyformacceptancedetailscode) from"
					+ " biothirdpartyformacceptdetails where nbiothirdpartyformacceptancecode="
					+ nbioThirdPartyFormAcceptanceCode + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ") where stablename=" + "'chaincustody' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final String strBioThirdPartyFormAcceptanceHistory = "insert into biothirdpartyformaccepthistory (nbiothirdpartyformacceptancehistorycode,"
					+ " nbiothirdpartyformacceptancecode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
					+ " noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode,"
					+ " nstatus) values (" + bioThirdPartyFormAcceptanceHistoryPk + ", "
					+ nbioThirdPartyFormAcceptanceCode + ", "
					+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			final String strBioThirdPartyFormAcceptanceDetailsHistory = "insert into biothirdpartyformacceptdetailshistory"
					+ " (nbiothirdpartyformacceptdetailshistorycode, nbiothirdpartyformacceptancedetailscode, nbiothirdpartyformacceptancecode,"
					+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
					+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
					+ "select " + bioThirdPartyFormAcceptanceHistoryDetailsPk
					+ "+rank()over(order by nbiothirdpartyformacceptancedetailscode),"
					+ " nbiothirdpartyformacceptancedetailscode, nbiothirdpartyformacceptancecode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyformacceptdetails where " + " nbiothirdpartyformacceptancecode="
					+ nbioThirdPartyFormAcceptanceCode + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by nbiothirdpartyformacceptancedetailscode;";
			final String strSeqNoUpdate = " update seqnobiobankmanagement set nsequenceno="
					+ bioThirdPartyFormAcceptanceHistoryPk
					+ " where stablename='biothirdpartyformaccepthistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final String strSeqNoDetailsUpdate = " update seqnobiobankmanagement set nsequenceno=(" + "select "
					+ bioThirdPartyFormAcceptanceHistoryDetailsPk
					+ " + count(nbiothirdpartyformacceptancedetailscode) from biothirdpartyformacceptdetails where"
					+ " nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " where stablename='biothirdpartyformacceptdetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			String strUpdateReqBasedTransfer = "";
			String strUpdateReqBasedTransferDetails = "";
			strUpdateReqBasedTransfer = "update biorequestbasedtransfer set ntransactionstatus="
					+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiorequestbasedtransfercode=" + nbioRequestBasedTransferCode + " and nsitecode="
					+ noriginSiteCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ";";
			strUpdateReqBasedTransferDetails = "update biorequestbasedtransferdetails set ntransferstatus="
					+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiorequestbasedtransfercode=" + nbioRequestBasedTransferCode + " and nsitecode="
					+ noriginSiteCode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ntransferstatus not in ("
					+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ")" + ";";

			jdbcTemplate.execute(strUpdateBioThirdPartyFormAcceptance + strUpdateBioThirdPartyFormAcceptanceDetails
					+ strUpdateReqBasedTransfer + strUpdateReqBasedTransferDetails
					+ strBioThirdPartyFormAcceptanceHistory + strBioThirdPartyFormAcceptanceDetailsHistory
					+ strSeqNoUpdate + strSeqNoDetailsUpdate + strChainCustody);
			
			jdbcTemplate.execute(strUpdateBioThirdPartyFormAcceptance + strUpdateBioThirdPartyFormAcceptanceDetails
					+ strUpdateReqBasedTransfer + strUpdateReqBasedTransferDetails
					+ strBioThirdPartyFormAcceptanceHistory + strBioThirdPartyFormAcceptanceDetailsHistory
					+ strSeqNoUpdate + strSeqNoDetailsUpdate);
			
			// ===== COC: START =====
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

						String strChainCustody1 = "insert into chaincustody(nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,sitemno,"
								+ "ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,noffsetdtransactiondate,"
								+ "sremarks,nsitecode,nstatus) "
								+ "select (" + chainCustodyPk1
								+ " + rank() over(order by btpfad.nbiothirdpartyformacceptancedetailscode)), "
								+ userInfo.getNformcode()
								+ ",btpfad.nbiothirdpartyformacceptancedetailscode,'nbiothirdpartyformacceptancedetailscode',"
								+ "'biothirdpartyformacceptdetails',btpfad.srepositoryid,"
								+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ","
								+ userInfo.getNusercode() + ","
								+ userInfo.getNuserrole() + ",'"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ userInfo.getNtimezonecode() + ","
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ",'"
								+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
								+ " ['||btpfad.srepositoryid||'] '||'"
								+ commonFunction.getMultilingualMessage("IDS_RECEIVEDSITE", userInfo.getSlanguagefilename())
								+ " ['||s.ssitename||'] '||'"
								+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
								+ " ['||(btpfad.jsondata->>'sparentsamplecode')||'] '||'"
								+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
								+ " ['||btpfa.sformnumber||']'"
								+ "," + userInfo.getNtranssitecode() + ","
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " from biothirdpartyformacceptdetails btpfad "
								+ " join biothirdpartyformaccept btpfa on btpfa.nbiothirdpartyformacceptancecode = btpfad.nbiothirdpartyformacceptancecode"
								+ " and btpfa.nsitecode = " + userInfo.getNtranssitecode()
								+ " and btpfa.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " join site s on s.nsitecode = btpfa.noriginsitecode"
								+ " where btpfad.nsitecode = " + userInfo.getNtranssitecode()
								+ " and btpfad.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and btpfad.nbiothirdpartyformacceptancecode = " + nbioThirdPartyFormAcceptanceCode + ";";

						String strSeqUpdateCOC = " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
								+ " + count(nbiothirdpartyformacceptancedetailscode) from biothirdpartyformacceptdetails "
								+ "where nbiothirdpartyformacceptancecode = " + nbioThirdPartyFormAcceptanceCode
								+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ ") where stablename='chaincustody' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						jdbcTemplate.execute(strChainCustody1);
						jdbcTemplate.execute(strSeqUpdateCOC);
						// ===== COC: END =====
			

			List<BioThirdPartyFormAccept> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditQry,
					new BioThirdPartyFormAccept());
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream()
					.forEach(x -> multilingualIDList.add("IDS_RECEIVEBIOTHIRDPARTYFORMACCEPTANCE"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			return getActiveBioThirdPartyFormAcceptance(nbioThirdPartyFormAcceptanceCode, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> updateCompleteBioThirdPartyFormAcceptance(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		jdbcTemplate.execute(
				"lock table lockbiothirdpartyformaccept " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbiothirdpartyformacceptdetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final ObjectMapper objMapper = new ObjectMapper();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final int nbioThirdPartyFormAcceptanceCode = (int) inputMap.get("nbiothirdpartyformacceptancecode");
		final int nisNgs = (int) inputMap.get("nisngs");

//		final String sbioFormAcceptanceDetailsCode = (String) inputMap.get("nbioformacceptancedetailscode");
		List<Map<String, Object>> nonThirdPartySharableSamples = checkThirdPartySharableSamples(
				nbioThirdPartyFormAcceptanceCode, userInfo);

		if (!nonThirdPartySharableSamples.isEmpty() && nisNgs == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			final String srepositoryId = nonThirdPartySharableSamples.stream().map(x -> (String) x.get("srepositoryid"))
					.collect(Collectors.joining(","));
			String salertMsg = srepositoryId + " " + commonFunction
					.getMultilingualMessage("IDS_SAMPLESARENOTTHIRDPARTYSHARABLE", userInfo.getSlanguagefilename());
			final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
			outputMap.put("containsNotThirdPartySharableSamples", true);
			outputMap.put("salertMsg", salertMsg);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}

		final int findStatus = findStatusBioThirdPartyFormAcceptance(nbioThirdPartyFormAcceptanceCode, userInfo);
		if (findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			final String concatSelect = ", btpfa.nreceivingtemperaturecode, btpfa.jsondata->>'sreceivingofficername' sreceivingofficername,"
					+ " btpfa.jsondata->>'scompletionremarks' scompletionremarks ";
			final String concatJoin = "";
			final String strAuditQry = auditParentQuery(nbioThirdPartyFormAcceptanceCode, concatSelect, concatJoin,
					userInfo);
			List<BioThirdPartyFormAccept> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditQry,
					new BioThirdPartyFormAccept());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);

			final String strDisposeReturnRecord = "select nbiothirdpartyformacceptancedetailscode, nsamplestatus from "
					+ " biothirdpartyformacceptdetails where nbiothirdpartyformacceptancecode="
					+ nbioThirdPartyFormAcceptanceCode + " and " + " nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsamplestatus in (" + Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus()
					+ " , " + Enumeration.TransactionStatus.TOBERETURN.gettransactionstatus() + ")";
			final List<Map<String, Object>> lstDisposeReturnRecord = jdbcTemplate.queryForList(strDisposeReturnRecord);

			String disposeBioThirdPartyFormAcceptanceDetailsCode = lstDisposeReturnRecord.stream()
					.filter(map -> map.get("nsamplestatus")
							.equals(Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus()))
					.map(map -> String.valueOf(map.get("nbiothirdpartyformacceptancedetailscode")))
					.collect(Collectors.joining(","));

			String returnBioThirdPartyFormAcceptanceDetailsCode = lstDisposeReturnRecord.stream()
					.filter(map -> map.get("nsamplestatus")
							.equals(Enumeration.TransactionStatus.TOBERETURN.gettransactionstatus()))
					.map(map -> String.valueOf(map.get("nbiothirdpartyformacceptancedetailscode")))
					.collect(Collectors.joining(","));

			BioThirdPartyFormAccept objBioThirdPartyFormAcceptance = objMapper
					.convertValue(inputMap.get("bioThirdPartyFormAcceptance"), BioThirdPartyFormAccept.class);

			int bioThirdPartyFormAcceptanceHistoryPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='biothirdpartyformaccepthistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			bioThirdPartyFormAcceptanceHistoryPk++;
			int bioThirdPartyFormAcceptanceHistoryDetailsPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='biothirdpartyformacceptdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			int chainCustodyPk = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strUpdateBioThirdPartyFormAcceptance = "update biothirdpartyformaccept set nreceivingtemperaturecode="
					+ objBioThirdPartyFormAcceptance.getNreceivingtemperaturecode() + ", " + " nreceivingofficercode="
					+ objBioThirdPartyFormAcceptance.getNreceivingofficercode()
					+ ", jsondata=jsondata || '{\"scompletionremarks\":" + " \""
					+ stringUtilityFunction.replaceQuote(objBioThirdPartyFormAcceptance.getScompletionremarks()) + "\","
					+ " \"sreceivingtemperaturename\": \""
					+ stringUtilityFunction.replaceQuote(objBioThirdPartyFormAcceptance.getSreceivingtemperaturename())
					+ "\", \"sreceivingofficername\": \""
					+ stringUtilityFunction.replaceQuote(objBioThirdPartyFormAcceptance.getSreceivingofficername())
					+ "\"}', ntransactionstatus=" + Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
					+ ", dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ " ntztransactiondate=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			strUpdateBioThirdPartyFormAcceptance += "insert into biothirdpartyformaccepthistory (nbiothirdpartyformacceptancehistorycode,"
					+ " nbiothirdpartyformacceptancecode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
					+ " noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode,"
					+ " nstatus) values (" + bioThirdPartyFormAcceptanceHistoryPk + ", "
					+ nbioThirdPartyFormAcceptanceCode + ", "
					+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			strUpdateBioThirdPartyFormAcceptance += "update seqnobiobankmanagement set nsequenceno="
					+ bioThirdPartyFormAcceptanceHistoryPk
					+ " where stablename='biothirdpartyformaccepthistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			String strChainCustody = "";
			int chainCustodyPkIncrement = chainCustodyPk;

			String strMoveToDispose = "";
			String strMoveToDisposeDetails = "";

			if (!disposeBioThirdPartyFormAcceptanceDetailsCode.isEmpty()) {
				strUpdateBioThirdPartyFormAcceptance += "update biothirdpartyformacceptdetails set nsamplestatus="
						+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
						+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " where "
						+ " nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode
						+ " and nbiothirdpartyformacceptancedetailscode in ("
						+ disposeBioThirdPartyFormAcceptanceDetailsCode + ") and nsitecode="
						+ userInfo.getNtranssitecode() + " and " + " nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				strChainCustody += "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
						+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
						+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
						+ "select " + chainCustodyPkIncrement
						+ "+rank() over(order by btpfad.nbiothirdpartyformacceptancedetailscode), "
						+ userInfo.getNformcode() + ","
						+ " btpfad.nbiothirdpartyformacceptancedetailscode, 'nbiothirdpartyformacceptancedetailscode', "
						+ " 'biothirdpartyformacceptdetails', btpfa.sformnumber, "
						+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
						+ commonFunction.getMultilingualMessage("IDS_MOVETODISPOSESAMPLE",
								userInfo.getSlanguagefilename())
						+ "'||' ['||btpfad.srepositoryid||'] '||'"
						+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
						+ " '||btpfa.sformnumber, " + userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biothirdpartyformacceptdetails btpfad join biothirdpartyformaccept btpfa"
						+ " on btpfa.nbiothirdpartyformacceptancecode=btpfad.nbiothirdpartyformacceptancecode and"
						+ " btpfa.nsitecode=" + userInfo.getNtranssitecode() + " and btpfa.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btpfad.nsitecode="
						+ userInfo.getNtranssitecode() + " and btpfad.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
						+ " btpfad.nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode + " and "
						+ " btpfad.nbiothirdpartyformacceptancedetailscode in ("
						+ disposeBioThirdPartyFormAcceptanceDetailsCode + "); ";
				chainCustodyPkIncrement = chainCustodyPkIncrement
						+ disposeBioThirdPartyFormAcceptanceDetailsCode.split(",").length;

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
						+ " select " + intBioMoveToDisposePk
						+ ", sformnumber, ntransfertypecode, nformtypecode, nthirdpartycode, "
						+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
						+ ", noriginsitecode, jsondata->>'sremarks', '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biothirdpartyformaccept where nsitecode=" + userInfo.getNtranssitecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
						+ " nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode + ";";
				strMoveToDispose += " update seqnobiobankmanagement set nsequenceno=" + intBioMoveToDisposePk
						+ " where stablename='biomovetodispose' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				// modified by sujatha ATE_274 for adding 4 new keys into the jsondata column bgsi-218
				strMoveToDisposeDetails += "insert into biomovetodisposedetails (nbiomovetodisposedetailscode,"
						+ " nbiomovetodisposecode, nbioprojectcode, nbioparentsamplecode,"
						+ " nsamplestoragetransactioncode, svolume, jsondata, ncohortno, nstoragetypecode,"
						+ " nproductcatcode, nproductcode, nsamplecondition, ndiagnostictypecode, "
						+ " ncontainertypecode, nsamplestatus, nreasoncode, dtransactiondate, "
						+ " ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)" + " select "
						+ intBioMoveToDisposeDetailsPk
						+ "+rank()over(order by nbiothirdpartyformacceptancedetailscode), " + intBioMoveToDisposePk
						+ ", nbioprojectcode, nbioparentsamplecode, nsamplestoragetransactioncode, jsondata->>'sreceivedvolume',"
						+ " jsonb_build_object('sparentsamplecode', jsondata->>'sparentsamplecode', 'srepositoryid', srepositoryid,"
						+ " 'ssubjectid', jsondata->>'ssubjectid', 'scasetype', jsondata->>'scasetype', 'slocationcode', '',"
						+ " 'sextractedsampleid', jsondata->>'sextractedsampleid','sconcentration', jsondata->>'sconcentration','sqcplatform', jsondata->>'sqcplatform',"
						+ " 'seluent', jsondata->>'seluent','"+Enumeration.FormCode.ECATALOGREQUESTAPPROVAL.getFormCode()+"', NULLIF(jsondata->>'"+Enumeration.FormCode.ECATALOGREQUESTAPPROVAL.getFormCode()+"',''),'"+Enumeration.FormCode.REQUESTBASEDTRANSFER.getFormCode()+"', NULLIF(jsondata->>'"+Enumeration.FormCode.REQUESTBASEDTRANSFER.getFormCode()+"','')), ncohortno, nstoragetypecode,"
						+ " nproductcatcode, nproductcode, nsamplecondition, ndiagnostictypecode, ncontainertypecode, "
						+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ", nreasoncode, " + " '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biothirdpartyformacceptdetails where nsitecode=" + userInfo.getNtranssitecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
						+ " nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode
						+ " and nbiothirdpartyformacceptancedetailscode in ("
						+ disposeBioThirdPartyFormAcceptanceDetailsCode + ")" + ";";
				strMoveToDisposeDetails += "update seqnobiobankmanagement set nsequenceno=(select "
						+ intBioMoveToDisposeDetailsPk
						+ "+ count(nbiothirdpartyformacceptancedetailscode) from biothirdpartyformacceptdetails where "
						+ " nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode
						+ " and nbiothirdpartyformacceptancedetailscode in ("
						+ disposeBioThirdPartyFormAcceptanceDetailsCode
						+ ")) where stablename='biomovetodisposedetails'" + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			}

			if (!returnBioThirdPartyFormAcceptanceDetailsCode.isEmpty()) {
				strUpdateBioThirdPartyFormAcceptance += "update biothirdpartyformacceptdetails set nsamplestatus="
						+ Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
						+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " where "
						+ " nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode
						+ " and nbiothirdpartyformacceptancedetailscode in ("
						+ returnBioThirdPartyFormAcceptanceDetailsCode + ") and nsitecode="
						+ userInfo.getNtranssitecode() + " and " + " nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				strChainCustody += "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
						+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
						+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
						+ "select " + chainCustodyPkIncrement
						+ "+rank() over(order by btpfad.nbiothirdpartyformacceptancedetailscode), "
						+ userInfo.getNformcode() + ","
						+ " btpfad.nbiothirdpartyformacceptancedetailscode, 'nbiothirdpartyformacceptancedetailscode', "
						+ " 'biothirdpartyformacceptdetails', btpfa.sformnumber, "
						+ Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus() + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
						+ commonFunction.getMultilingualMessage("IDS_MOVETORETURNSAMPLE",
								userInfo.getSlanguagefilename())
						+ "'||' ['||btpfad.srepositoryid||'] '||'"
						+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
						+ " '||btpfa.sformnumber, " + userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biothirdpartyformacceptdetails btpfad join biothirdpartyformaccept btpfa"
						+ " on btpfa.nbiothirdpartyformacceptancecode=btpfad.nbiothirdpartyformacceptancecode and"
						+ " btpfa.nsitecode=" + userInfo.getNtranssitecode() + " and btpfa.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btpfad.nsitecode="
						+ userInfo.getNtranssitecode() + " and btpfad.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
						+ " btpfad.nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode + " and"
						+ " btpfad.nbiothirdpartyformacceptancedetailscode in ("
						+ returnBioThirdPartyFormAcceptanceDetailsCode + "); ";
				chainCustodyPkIncrement = chainCustodyPkIncrement
						+ returnBioThirdPartyFormAcceptanceDetailsCode.split(",").length;
			}

			if (!disposeBioThirdPartyFormAcceptanceDetailsCode.isEmpty()
					|| !returnBioThirdPartyFormAcceptanceDetailsCode.isEmpty()) {
				strChainCustody += " update seqnoregistration set nsequenceno=" + chainCustodyPkIncrement
						+ " where stablename='chaincustody' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			}

			final String sbioThirdPartyFormAcceptanceDetailsCode = lstDisposeReturnRecord.stream()
					.map(x -> String.valueOf(x.get("nbiothirdpartyformacceptancedetailscode")))
					.collect(Collectors.joining(","));

			if (!sbioThirdPartyFormAcceptanceDetailsCode.isEmpty()) {
				strUpdateBioThirdPartyFormAcceptance += "insert into biothirdpartyformacceptdetailshistory"
						+ " (nbiothirdpartyformacceptdetailshistorycode, nbiothirdpartyformacceptancedetailscode, nbiothirdpartyformacceptancecode,"
						+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
						+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
						+ "select " + bioThirdPartyFormAcceptanceHistoryDetailsPk
						+ "+rank()over(order by nbiothirdpartyformacceptancedetailscode),"
						+ " nbiothirdpartyformacceptancedetailscode, nbiothirdpartyformacceptancecode, nsamplecondition, nsamplestatus, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biothirdpartyformacceptdetails where nbiothirdpartyformacceptancedetailscode in ("
						+ sbioThirdPartyFormAcceptanceDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by nbiothirdpartyformacceptancedetailscode;";

				strUpdateBioThirdPartyFormAcceptance += " update seqnobiobankmanagement set nsequenceno=(" + "select "
						+ bioThirdPartyFormAcceptanceHistoryDetailsPk
						+ " + count(nbiothirdpartyformacceptancedetailscode) from biothirdpartyformacceptdetails where"
						+ " nbiothirdpartyformacceptancedetailscode in (" + sbioThirdPartyFormAcceptanceDetailsCode
						+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
						+ " where stablename='biothirdpartyformacceptdetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			}

			Map<String, Object> updatedMap = new HashMap<>();
			
			if (nisNgs == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

				ResponseEntity<Object> ngsInsertAPI = objBGSIExternalApiDAOImpl.sendAcceptedThirdPartySamplestoNGS(inputMap,userInfo) ;
				if(!ngsInsertAPI.getStatusCode().is2xxSuccessful()) {
					return ngsInsertAPI;
				}else {
					updatedMap.put("ngsSuccessMessage", commonFunction.getMultilingualMessage("IDS_NGSSAMPLESSENTSUCCESSFULLY",
							userInfo.getSlanguagefilename()));
				}
			}

//			jdbcTemplate.execute(strUpdateBioThirdPartyFormAcceptance + strChainCustody + strMoveToDispose
//					+ strMoveToDisposeDetails);
			
			jdbcTemplate.execute(strUpdateBioThirdPartyFormAcceptance  + strMoveToDispose
					+ strMoveToDisposeDetails);

			List<BioThirdPartyFormAccept> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditQry,
					new BioThirdPartyFormAccept());
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream()
					.forEach(x -> multilingualIDList.add("IDS_COMPLETEBIOTHIRDPARTYFORMACCEPTANCE"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);


			
			updatedMap
					.putAll((Map<String, Object>) getActiveBioThirdPartyFormAcceptance(nbioThirdPartyFormAcceptanceCode,
							userInfo).getBody());
			
			return new ResponseEntity<>(updatedMap, HttpStatus.OK);
//			return getActiveBioThirdPartyFormAcceptance(nbioThirdPartyFormAcceptanceCode, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> validateBioThirdPartyFormAcceptance(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		jdbcTemplate.execute(
				"lock table lockbiothirdpartyformaccept " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbiothirdpartyformacceptdetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final int nbioThirdPartyFormAcceptanceCode = (int) inputMap.get("nbiothirdpartyformacceptancecode");

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final int findStatus = findStatusBioThirdPartyFormAcceptance(nbioThirdPartyFormAcceptanceCode, userInfo);
		if (findStatus == Enumeration.TransactionStatus.RECEIVED.gettransactionstatus()) {

			final String concatSelect = ", to_char(btpfa.dreceiveddate, '" + userInfo.getSsitedate()
					+ "') sreceiveddate ";
			final String strAuditQry = auditParentQuery(nbioThirdPartyFormAcceptanceCode, concatSelect, "", userInfo);
			List<BioThirdPartyFormAccept> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditQry,
					new BioThirdPartyFormAccept());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);

			int bioThirdPartyFormAcceptanceHistoryPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='biothirdpartyformaccepthistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			bioThirdPartyFormAcceptanceHistoryPk++;
			int bioThirdPartyFormAcceptanceHistoryDetailsPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='biothirdpartyformacceptdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);

			String strValidateBioThirdPartyFormAcceptanceQuery = "update biothirdpartyformaccept set ntransactionstatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			strValidateBioThirdPartyFormAcceptanceQuery += "insert into biothirdpartyformaccepthistory (nbiothirdpartyformacceptancehistorycode,"
					+ " nbiothirdpartyformacceptancecode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
					+ " noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode,"
					+ " nstatus) values (" + bioThirdPartyFormAcceptanceHistoryPk + ", "
					+ nbioThirdPartyFormAcceptanceCode + ", "
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			strValidateBioThirdPartyFormAcceptanceQuery += " update seqnobiobankmanagement set nsequenceno="
					+ bioThirdPartyFormAcceptanceHistoryPk
					+ " where stablename='biothirdpartyformaccepthistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			int chainCustodyPk = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strValidateBioThirdPartyFormAcceptanceDetailsQuery = "update biothirdpartyformacceptdetails set nsamplestatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			String strChainCustody = "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
					+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
					+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
					+ "select " + chainCustodyPk
					+ "+rank() over(order by btpfad.nbiothirdpartyformacceptancedetailscode), "
					+ userInfo.getNformcode() + ","
					+ " btpfad.nbiothirdpartyformacceptancedetailscode, 'nbiothirdpartyformacceptancedetailscode', "
					+ " 'biothirdpartyformacceptdetails', btpfa.sformnumber, "
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", " + userInfo.getNusercode()
					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
					+ commonFunction.getMultilingualMessage("IDS_VALIDATEDSAMPLE", userInfo.getSlanguagefilename())
					+ "'||' ['||btpfad.srepositoryid||'] '||'"
					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
					+ " '||btpfa.sformnumber, " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyformacceptdetails btpfad join biothirdpartyformaccept btpfa"
					+ " on btpfa.nbiothirdpartyformacceptancecode=btpfad.nbiothirdpartyformacceptancecode and"
					+ " btpfa.nsitecode=" + userInfo.getNtranssitecode() + " and btpfa.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btpfad.nsitecode="
					+ userInfo.getNtranssitecode() + " and btpfad.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
					+ " btpfad.nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode + "; ";
			strChainCustody += " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
					+ "+ count(nbiothirdpartyformacceptancedetailscode) from"
					+ " biothirdpartyformacceptdetails where nbiothirdpartyformacceptancecode="
					+ nbioThirdPartyFormAcceptanceCode + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ") where stablename=" + "'chaincustody' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			strValidateBioThirdPartyFormAcceptanceDetailsQuery += "insert into biothirdpartyformacceptdetailshistory"
					+ " (nbiothirdpartyformacceptdetailshistorycode, nbiothirdpartyformacceptancedetailscode, nbiothirdpartyformacceptancecode,"
					+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
					+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
					+ "select " + bioThirdPartyFormAcceptanceHistoryDetailsPk
					+ "+rank()over(order by nbiothirdpartyformacceptancedetailscode),"
					+ " nbiothirdpartyformacceptancedetailscode, nbiothirdpartyformacceptancecode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from biothirdpartyformacceptdetails where nbiothirdpartyformacceptancecode="
					+ nbioThirdPartyFormAcceptanceCode + " and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by nbiothirdpartyformacceptancedetailscode;";
			strValidateBioThirdPartyFormAcceptanceDetailsQuery += " update seqnobiobankmanagement set nsequenceno=("
					+ "select " + bioThirdPartyFormAcceptanceHistoryDetailsPk
					+ " + count(nbiothirdpartyformacceptancedetailscode) from biothirdpartyformacceptdetails where"
					+ " nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " where stablename='biothirdpartyformacceptdetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//			jdbcTemplate.execute(strValidateBioThirdPartyFormAcceptanceQuery
//					+ strValidateBioThirdPartyFormAcceptanceDetailsQuery + strChainCustody);
			
			jdbcTemplate.execute(strValidateBioThirdPartyFormAcceptanceQuery
					+ strValidateBioThirdPartyFormAcceptanceDetailsQuery);

			List<BioThirdPartyFormAccept> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditQry,
					new BioThirdPartyFormAccept());
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream()
					.forEach(x -> multilingualIDList.add("IDS_VALIDATEBIOTHIRDPARTYFORMACCEPTANCE"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			return getActiveBioThirdPartyFormAcceptance(nbioThirdPartyFormAcceptanceCode, userInfo);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTRECEIVEDRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> onCompleteSlideOut(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final int nbioThirdPartyFormAcceptanceCode = (int) inputMap.get("nbiothirdpartyformacceptancecode");

		final int findStatus = findStatusBioThirdPartyFormAcceptance(nbioThirdPartyFormAcceptanceCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {
			List<Map<String, Object>> lstChildBioThirdPartyFormAcceptance = getChildInitialGet(
					nbioThirdPartyFormAcceptanceCode, userInfo);
			if (lstChildBioThirdPartyFormAcceptance != null && lstChildBioThirdPartyFormAcceptance.size() > 0) {
				Map<String, Object> outputMap = new HashMap<>();
				outputMap.putAll((Map<String, Object>) getStorageCondition(userInfo).getBody());
				outputMap.putAll((Map<String, Object>) getUsersBasedOnSite(userInfo).getBody());
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOSAMPLESAVAILABLETOCOMPLETE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public int findStatusBioThirdPartyFormAcceptance(final int nbioThirdPartyFormAcceptanceCode,
			final UserInfo userInfo) throws Exception {
		final String strStatusBioThirdPartyFormAcceptance = "select ntransactionstatus from biothirdpartyformaccept where nbiothirdpartyformacceptancecode="
				+ nbioThirdPartyFormAcceptanceCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.queryForObject(strStatusBioThirdPartyFormAcceptance, Integer.class);
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> acceptRejectBioThirdPartyFormAcceptanceSlide(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		int nbioThirdPartyFormAcceptanceCode = Integer
				.valueOf(inputMap.get("nbiothirdpartyformacceptancecode").toString());
		List<Map<String, Object>> addedChildBioThirdPartyFormAcceptance = (List<Map<String, Object>>) inputMap
				.get("addedChildBioThirdPartyFormAcceptance");
		int findStatus = findStatusBioThirdPartyFormAcceptance(nbioThirdPartyFormAcceptanceCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.RECEIVED.gettransactionstatus()) {
			if (addedChildBioThirdPartyFormAcceptance.size() > 0) {

				outputMap.putAll((Map<String, Object>) getSampleConditionStatus(userInfo).getBody());
				outputMap.putAll((Map<String, Object>) getReason(userInfo).getBody());
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTSAMPLESTOVALIDATE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTRECEIVEDRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public String findStatusBioThirdPartyFormAcceptanceDetails(final int nbioThirdPartyFormAcceptanceCode,
			final String nbioThirdPartyFormAcceptanceDetailsCode, final String concatString, final UserInfo userInfo)
			throws Exception {
		final String strStatusBioThirdPartyFormAcceptance = "select nbiothirdpartyformacceptancedetailscode from biothirdpartyformacceptdetails where"
				+ " nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode
				+ " and nbiothirdpartyformacceptancedetailscode in (" + nbioThirdPartyFormAcceptanceDetailsCode
				+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + concatString;
		List<Integer> lstStatusBioThirdPartyFormAcceptance = jdbcTemplate
				.queryForList(strStatusBioThirdPartyFormAcceptance, Integer.class);
		String strBioThirdPartyFormAcceptanceDetailsCode = null;
		if (lstStatusBioThirdPartyFormAcceptance.size() > 0) {
			strBioThirdPartyFormAcceptanceDetailsCode = lstStatusBioThirdPartyFormAcceptance.stream()
					.map(String::valueOf).collect(Collectors.joining(", "));
		}
		return strBioThirdPartyFormAcceptanceDetailsCode;
	}

	public List<Map<String, Object>> checkThirdPartySharableSamples(final int nbioThirdPartyFormAcceptanceCode,
			final UserInfo userInfo) throws Exception {

		final String strNonThirdPartySharableSample = "select nbiothirdpartyformacceptancedetailscode, srepositoryid from"
				+ " biothirdpartyformacceptdetails bfad join biosubjectdetails bsd on bsd.ssubjectid=bfad.ssubjectid"
				+ " and bsd.nsitecode=" + userInfo.getNmastersitecode() + " and bsd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bfad.nsitecode="
				+ userInfo.getNtranssitecode() + " and bfad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bfad.nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode
				+ " and bsd.nisthirdpartysharable=" + Enumeration.TransactionStatus.NO.gettransactionstatus()
				+ " and bfad.nsamplestatus not in (" + Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus()
				+ ", " + Enumeration.TransactionStatus.TOBERETURN.gettransactionstatus() + ")" + " order by 1 desc";
		return jdbcTemplate.queryForList(strNonThirdPartySharableSample);
	}

	@Override
	public ResponseEntity<Object> moveToReturnSamplesAfterComplete(int nbioThirdPartyFormAcceptanceCode,
			String nbioThirdPartyFormAcceptanceDetailsCode, UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();

		jdbcTemplate.execute(
				"lock table lockbiothirdpartyformacceptdetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final var findStatusForm = findStatusBioThirdPartyFormAcceptance(nbioThirdPartyFormAcceptanceCode, userInfo);

		if (findStatusForm == Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()) {

			final var stroredSample = "SELECT STRING_AGG(nbiothirdpartyformacceptancedetailscode::text, ',')  FROM biothirdpartyformacceptdetails "
					+ "WHERE nbiothirdpartyformacceptancedetailscode in( " + nbioThirdPartyFormAcceptanceDetailsCode +")"
					+ "  AND nsamplecondition = " + Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()
					+ "  AND nsamplestatus = " + Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()
					+ " AND nsitecode=" + userInfo.getNtranssitecode() + " AND nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  ;";

			final var nBioThirdPartyFormAcceptanceDetailsCodeStored = jdbcTemplate.queryForObject(stroredSample,
					String.class);

			if (nBioThirdPartyFormAcceptanceDetailsCodeStored != null) {

				final String strAuditAfterQry = childAuditQuery(nbioThirdPartyFormAcceptanceCode,
						nBioThirdPartyFormAcceptanceDetailsCodeStored, userInfo);
				List<BioThirdPartyFormAcceptDetails> lstAuditTransferDetailsBefore = jdbcTemplate
						.query(strAuditAfterQry, new BioThirdPartyFormAcceptDetails());
				
				
				final String strUpdateThirdPartydetails = "update biothirdpartyformacceptdetails set nsamplestatus="
						+ Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
						+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " where nbiothirdpartyformacceptancedetailscode in (" + nbioThirdPartyFormAcceptanceDetailsCode + ") and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				

				int bioThirdPartyFormAcceptanceHistoryPk = jdbcTemplate
						.queryForObject("select nsequenceno from seqnobiobankmanagement where "
								+ "stablename='biothirdpartyformaccepthistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
				bioThirdPartyFormAcceptanceHistoryPk++;

				int bioThirdPartyFormAcceptanceHistoryDetailsPk = jdbcTemplate
						.queryForObject("select nsequenceno from seqnobiobankmanagement where "
								+ "stablename='biothirdpartyformacceptdetailshistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);

				final String strBioThirdPartyFormAcceptanceHistory = "insert into biothirdpartyformaccepthistory (nbiothirdpartyformacceptancehistorycode,"
						+ " nbiothirdpartyformacceptancecode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
						+ " noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode,"
						+ " nstatus) values (" + bioThirdPartyFormAcceptanceHistoryPk + ", "
						+ nbioThirdPartyFormAcceptanceCode + ", "
						+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

				final String strBioThirdPartyFormAcceptanceDetailsHistory = "insert into biothirdpartyformacceptdetailshistory"
						+ " (nbiothirdpartyformacceptdetailshistorycode, nbiothirdpartyformacceptancedetailscode, nbiothirdpartyformacceptancecode,"
						+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
						+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
						+ "select " + bioThirdPartyFormAcceptanceHistoryDetailsPk
						+ "+rank()over(order by nbiothirdpartyformacceptancedetailscode),"
						+ " nbiothirdpartyformacceptancedetailscode, nbiothirdpartyformacceptancecode, nsamplecondition, nsamplestatus, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biothirdpartyformacceptdetails where  nbiothirdpartyformacceptancecode="
						+ nbioThirdPartyFormAcceptanceCode + " and nbiothirdpartyformacceptancedetailscode in ("
						+ nBioThirdPartyFormAcceptanceDetailsCodeStored + ") and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by nbiothirdpartyformacceptancedetailscode;";

				final String strSeqNoUpdate = " update seqnobiobankmanagement set nsequenceno="
						+ bioThirdPartyFormAcceptanceHistoryPk
						+ " where stablename='biothirdpartyformaccepthistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				final String strSeqNoDetailsUpdate = " update seqnobiobankmanagement set nsequenceno=(" + "select "
						+ bioThirdPartyFormAcceptanceHistoryDetailsPk
						+ " + count(nbiothirdpartyformacceptancedetailscode) from biothirdpartyformacceptdetails where"
						+ " nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode
						+ " and nbiothirdpartyformacceptancedetailscode in ("
						+ nBioThirdPartyFormAcceptanceDetailsCodeStored + ") and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
						+ " where stablename='biothirdpartyformacceptdetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				int chainCustodyPk = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);

				String strChainCustody = "insert into chaincustody (nchaincustodycode, nformcode, ntablepkno,"
						+ " stablepkcolumnname, stablename, sitemno, ntransactionstatus, nusercode, nuserrolecode,"
						+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
						+ "select " + chainCustodyPk
						+ "+rank() over(order by btpfad.nbiothirdpartyformacceptancedetailscode), "
						+ userInfo.getNformcode() + ","
						+ " btpfad.nbiothirdpartyformacceptancedetailscode, 'nbiothirdpartyformacceptancedetailscode', "
						+ " 'biothirdpartyformacceptdetails', btpfa.sformnumber, "
						+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", " + userInfo.getNusercode()
						+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
						+ commonFunction.getMultilingualMessage("IDS_RECEIVEDSAMPLE", userInfo.getSlanguagefilename())
						+ "'||' ['||btpfad.srepositoryid||'] '||'"
						+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
						+ " '||btpfa.sformnumber, " + userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from biothirdpartyformacceptdetails btpfad join biothirdpartyformaccept btpfa"
						+ " on btpfa.nbiothirdpartyformacceptancecode=btpfad.nbiothirdpartyformacceptancecode and"
						+ " btpfa.nsitecode=" + userInfo.getNtranssitecode() + " and btpfa.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where btpfad.nsitecode="
						+ userInfo.getNtranssitecode() + " and btpfad.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
						+ " btpfad.nbiothirdpartyformacceptancecode=" + nbioThirdPartyFormAcceptanceCode + "; ";

				strChainCustody += " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
						+ "+ count(nbiothirdpartyformacceptancedetailscode) from"
						+ " biothirdpartyformacceptdetails where nbiothirdpartyformacceptancecode="
						+ nbioThirdPartyFormAcceptanceCode + " and nsitecode=" + userInfo.getNtranssitecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ ") where stablename=" + "'chaincustody' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

//				jdbcTemplate
//						.execute(strUpdateThirdPartydetails + strBioThirdPartyFormAcceptanceHistory + strBioThirdPartyFormAcceptanceDetailsHistory
//								+ strSeqNoUpdate + strSeqNoDetailsUpdate + strChainCustody);
				
				jdbcTemplate
				.execute(strUpdateThirdPartydetails + strBioThirdPartyFormAcceptanceHistory + strBioThirdPartyFormAcceptanceDetailsHistory
						+ strSeqNoUpdate + strSeqNoDetailsUpdate);

				// ----------------- Audit

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> lstAuditBefore = new ArrayList<>();
				final List<Object> lstAuditAfter = new ArrayList<>();

				List<BioThirdPartyFormAcceptDetails> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
						new BioThirdPartyFormAcceptDetails());
				lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
				lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
				lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_RETURNUSEDSAMPLES"));

				auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList,
						userInfo);

				List<Map<String, Object>> lstChildBioThirdPartyFormAcceptance = getChildInitialGet(
						nbioThirdPartyFormAcceptanceCode, userInfo);
				
				outputMap.put("lstChildBioThirdPartyFormAcceptance", lstChildBioThirdPartyFormAcceptance);
				
				return new ResponseEntity<>(outputMap, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDSAMPLES",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDCOMPLETERECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}

}
