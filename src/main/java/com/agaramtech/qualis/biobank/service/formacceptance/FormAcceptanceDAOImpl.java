package com.agaramtech.qualis.biobank.service.formacceptance;

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
import com.agaramtech.qualis.biobank.model.BioFormAcceptance;
import com.agaramtech.qualis.biobank.model.BioFormAcceptanceDetails;
import com.agaramtech.qualis.biobank.model.FormType;
import com.agaramtech.qualis.biobank.model.StorageType;
import com.agaramtech.qualis.biobank.model.TransferType;
import com.agaramtech.qualis.biobank.service.processedsamplereceiving.ProcessedSampleReceivingDAOImpl;
import com.agaramtech.qualis.credential.model.Site;
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
import com.agaramtech.qualis.project.model.BioProject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class FormAcceptanceDAOImpl implements FormAcceptanceDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(FormAcceptanceDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProcessedSampleReceivingDAOImpl processedSampleReceivingDAOImpl;
	private final EmailDAOSupport emailDAOSupport;

	public ResponseEntity<Object> getFormAcceptance(final Map<String, Object> inputMap,
			final int nbioFormAcceptanceCode, final UserInfo userInfo) throws Exception {
		LOGGER.info("getBioFormAcceptance");
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		String fromDate = (String) inputMap.get("fromDate");
		String toDate = (String) inputMap.get("toDate");
		int ntransCode = inputMap.containsKey("ntransCode") ? (int) inputMap.get("ntransCode") : -1;
		int nformTypeCode = inputMap.containsKey("nformTypeCode") ? (int) inputMap.get("ntransCode") : -1;

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
		final String strConditionTransCode = ntransCode == 0 ? "" : " and bfa.ntransactionstatus=" + ntransCode + " ";

		List<FormType> getFormType = getFormType(userInfo);
		List<Map<String, Object>> lstFormType = new ArrayList<>();

		getFormType.stream().forEach(lst -> {
			Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getSformtypename());
			mapStatus.put("value", lst.getNformtypecode());
			mapStatus.put("item", lst);
			lstFormType.add(mapStatus);
		});

		if (!inputMap.containsKey("ntransCode")) {
			nformTypeCode = getFormType.get(0).getNformtypecode();
		} else {
			nformTypeCode = (int) inputMap.get("nformTypeCode");
		}

		final short nformTypeCodePK = (short) nformTypeCode;
		Map<String, Object> selectedFormType = lstFormType.stream()
				.filter(x -> (short) x.get("value") == nformTypeCodePK).collect(Collectors.toList()).get(0);

		outputMap.put("lstFormType", lstFormType);
		outputMap.put("selectedFormType", selectedFormType);
		outputMap.put("realSelectedFormType", selectedFormType);
		final String strConditionFormTypeCode = nformTypeCode == 0 ? ""
				: " and bfa.nformtypecode=" + nformTypeCode + " ";

		final String strConcatQry = " and bfa.dtransactiondate" + " between '" + fromDate + "' and '" + toDate + "' "
				+ strConditionTransCode + strConditionFormTypeCode;
		final String strQuery = getParentFormAcceptanceQry(strConcatQry, userInfo);
		final List<BioFormAcceptance> lstBioFormAcceptance = jdbcTemplate.query(strQuery, new BioFormAcceptance());

		if (!lstBioFormAcceptance.isEmpty()) {
			outputMap.put("lstBioFormAcceptance", lstBioFormAcceptance);
			List<BioFormAcceptance> lstObjBioFormAcceptance = null;
			if (nbioFormAcceptanceCode == -1) {
				lstObjBioFormAcceptance = lstBioFormAcceptance;
			} else {
				lstObjBioFormAcceptance = lstBioFormAcceptance.stream()
						.filter(x -> x.getNbioformacceptancecode() == nbioFormAcceptanceCode)
						.collect(Collectors.toList());
			}
			outputMap.put("selectedBioFormAcceptance", lstObjBioFormAcceptance.get(0));

			List<Map<String, Object>> lstChildBioFormAcceptance = getChildInitialGet(
					lstObjBioFormAcceptance.get(0).getNbioformacceptancecode(), userInfo);
			outputMap.put("lstChildBioFormAcceptance", lstChildBioFormAcceptance);

			if (inputMap.containsKey(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())) {
				outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						inputMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
			}
		} else {
			outputMap.put("lstBioFormAcceptance", null);
			outputMap.put("selectedBioFormAcceptance", null);
			outputMap.put("lstChildBioFormAcceptance", null);
		}

		outputMap.put("nprimaryKeyBioDirectTransfer",
				inputMap.containsKey("nprimaryKeyBioDirectTransfer") ? inputMap.get("nprimaryKeyBioDirectTransfer")
						: -1);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public String getParentFormAcceptanceQry(String concatString, UserInfo userInfo) throws Exception {
		final String strQry = "select bfa.nbioformacceptancecode, bfa.nbiodirecttransfercode, bfa.nbiobankreturncode,"
				+ " bfa.nbiorequestbasedtransfercode, bfa.sformnumber, bfa.nbiothirdpartyreturncode, bfa.nthirdpartycode,"
				+ " bfa.nformtypecode, coalesce(ft.jsondata->'sformtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "', ft.jsondata->'sformtypename'->>'en-US') sformtypename, bfa.ntransactionstatus,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, bfa.noriginsitecode, bfa.jsondata->>'srecipientname' srecipientname, "
				+ " bfa.jsondata->>'soriginsitename' soriginsitename, cm.scolorhexcode,"
				+ " bfa.ncouriercode, bfa.jsondata->>'scouriername' scouriername, bfa.ndispatchercode, bfa.jsondata->>'sdispatchername' sdispatchername, "
				+ " coalesce(to_char(bfa.dtransferdate, '" + userInfo.getSsitedate() + "'), '-') stransferdate, "
				+ " coalesce(to_char(bfa.ddeliverydate, '" + userInfo.getSsitedate() + "'), '-') sdeliverydate, "
				+ " coalesce(to_char(bfa.dreceiveddate, '" + userInfo.getSsitedate() + "'), '-') sreceiveddate, "
				+ " bfa.jsondata->>'sremarks' sremarks, bfa.jsondata->>'svalidationremarks' svalidationremarks, bfa.nreceivingtemperaturecode,"
				+ " bfa.jsondata->>'sreceivingtemperaturename' sreceivingtemperaturename, bfa.nstorageconditioncode,"
				+ " bfa.jsondata->>'sstorageconditionname' sstorageconditionname, bfa.nreceivingofficercode,"
				+ " bfa.jsondata->>'sreceivingofficername' sreceivingofficername, bfa.jsondata->>'scompletionremarks' scompletionremarks,"
				+ " bfa.jsondata->>'scourierno' scourierno, bfa.jsondata->>'svalidationremarks' svalidationremarks from"
				+ " bioformacceptance bfa join formtype ft on ft.nformtypecode=bfa.nformtypecode and ft.nsitecode="
				+ userInfo.getNmastersitecode() + " and ft.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join transactionstatus ts on"
				+ " ts.ntranscode=bfa.ntransactionstatus and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join formwisestatuscolor fwsc on"
				+ " fwsc.ntranscode=ts.ntranscode and fwsc.nformcode=" + userInfo.getNformcode()
				+ " and fwsc.nsitecode=" + userInfo.getNmastersitecode() + " and fwsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join colormaster cm on fwsc.ncolorcode=cm.ncolorcode and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bfa.nsitecode="
				+ userInfo.getNtranssitecode() + " and bfa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + concatString
				+ " order by bfa.nbioformacceptancecode desc";
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

	public List<FormType> getFormType(UserInfo userInfo) throws Exception {

		final String strFormType = "select nformtypecode, coalesce(jsondata->'sformtypename'->>'"
				+ userInfo.getSlanguagetypecode() + "', jsondata->'sformtypename'->>'en-US') sformtypename"
				+ " from formtype where nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nformtypecode";
		return jdbcTemplate.query(strFormType, new FormType());
	}

	public ResponseEntity<Object> getTransferType(final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strTransferType = "select ntransfertypecode, coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', jsondata->'sdisplayname'->>'en-US') stransfertypename from transfertype where nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ntransfertypecode";
		final List<TransferType> lstObjTransferType = jdbcTemplate.query(strTransferType, new TransferType());

		List<Map<String, Object>> lstTransferType = new ArrayList<>();

		lstObjTransferType.stream().forEach(lst -> {
			Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getStransfertypename());
			mapStatus.put("value", lst.getNtransfertypecode());
			mapStatus.put("item", lst);
			lstTransferType.add(mapStatus);
		});

		outputMap.put("lstTransferType", lstTransferType);
		outputMap.put("selectedTransferType", lstTransferType.get(0));
		outputMap.put("realSelectedTransferType", lstTransferType.get(0));

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getActiveBioFormAcceptance(final int nbioFormAcceptanceCode, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strConcatQry = " and bfa.nbioformacceptancecode=" + nbioFormAcceptanceCode;
		final String strQuery = getParentFormAcceptanceQry(strConcatQry, userInfo);
		final BioFormAcceptance objBioFormAcceptance = (BioFormAcceptance) jdbcUtilityTemplateFunction
				.queryForObject(strQuery, BioFormAcceptance.class, jdbcTemplate);

		if (objBioFormAcceptance != null) {
			outputMap.put("selectedBioFormAcceptance", objBioFormAcceptance);
			List<Map<String, Object>> lstChildBioFormAcceptance = getChildInitialGet(
					objBioFormAcceptance.getNbioformacceptancecode(), userInfo);
			outputMap.put("lstChildBioFormAcceptance", lstChildBioFormAcceptance);

		} else {
			outputMap.put("selectedBioDirectTransfer", null);
			outputMap.put("lstChildBioDirectTransfer", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getSiteBasedOnTransferType(final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strGetSiteBasedOnTransferType = "select s.nsitecode nbiobanksitecode, s.ssitename sbiobanksitename"
				+ " from site s, (select string_to_array(string_agg(trim(sparentsitecode), ','), ',')::int[] nsitecode"
				+ " from sitehierarchyconfigdetails where nnodesitecode=" + userInfo.getNtranssitecode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sparentsitecode is not null and sparentsitecode != '') s2,"
				+ "(select string_to_array(string_agg(trim(schildsitecode), ','), ',')::int[] nsitecode"
				+ " from sitehierarchyconfigdetails where nnodesitecode=" + userInfo.getNtranssitecode() + ""
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and schildsitecode is not null and schildsitecode != '') s3 where"
				+ " s.nsitecode = any (s2.nsitecode) or s.nsitecode = any (s3.nsitecode) and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<Site> lstSiteBasedOnTransferType = jdbcTemplate.query(strGetSiteBasedOnTransferType, new Site());

		List<Map<String, Object>> lstBioBankSite = new ArrayList<>();

		lstSiteBasedOnTransferType.stream().forEach(lst -> {
			Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getSbiobanksitename());
			mapStatus.put("value", lst.getNbiobanksitecode());
			mapStatus.put("item", lst);
			lstBioBankSite.add(mapStatus);
		});

		outputMap.put("lstBioBankSite", lstBioBankSite);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getProjectBasedOnSite(final int nbioBankSiteCode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strBioProject = "select bp.nbioprojectcode, bp.sprojecttitle from (select nbioprojectcode from"
				+ " projectsitemapping where nnodesitecode=" + userInfo.getNtranssitecode() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") psm1, (select"
				+ " nbioprojectcode from projectsitemapping where nnodesitecode=" + nbioBankSiteCode + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ") psm2, bioproject bp where psm1.nbioprojectcode=psm2.nbioprojectcode and psm2.nbioprojectcode=bp.nbioprojectcode and"
				+ " bp.nsitecode=" + userInfo.getNmastersitecode() + " and bp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by bp.nbioprojectcode";
		final List<BioProject> lstGetBioProject = jdbcTemplate.query(strBioProject, new BioProject());

		List<Map<String, Object>> lstBioProject = new ArrayList<>();

		lstGetBioProject.stream().forEach(lst -> {
			Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getSprojecttitle());
			mapStatus.put("value", lst.getNbioprojectcode());
			mapStatus.put("item", lst);
			lstBioProject.add(mapStatus);
		});

		outputMap.put("lstBioProject", lstBioProject);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getStorageType(final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strStorageType = "select nstoragetypecode, coalesce(jsondata->>'en-US', jsondata->>'en-US')"
				+ " sstoragetypename from storagetype where nsitecode=" + userInfo.getNmastersitecode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nstoragetypecode > 0";
		List<StorageType> lstGetStorageType = jdbcTemplate.query(strStorageType, new StorageType());

		List<Map<String, Object>> lstStorageType = new ArrayList<>();

		lstGetStorageType.stream().forEach(lst -> {
			Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getSstoragetypename());
			mapStatus.put("value", lst.getNstoragetypecode());
			mapStatus.put("item", lst);
			lstStorageType.add(mapStatus);
		});

		outputMap.put("lstStorageType", lstStorageType);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public List<Map<String, Object>> getChildInitialGet(int nbioFormAcceptanceCode, UserInfo userInfo)
			throws Exception {
		// modified by sujatha by selecting formnumber bgsi-292 issue
		final String strChildGet = "select row_number() over(order by bfad.nbioformacceptancedetailscode desc) as"
				+ " nserialno, bfad.nbioformacceptancedetailscode, bfad.nbioformacceptancecode, bfad.jsondata->>'sparentsamplecode' sparentsamplecode, "
				+ "bfad.srepositoryid, bfad.slocationcode, bfad.nsamplestoragetransactioncode, "
				+ "concat(p.sproductname, ' (', pc.sproductcatname, ')') sproductname, bfad.svolume, bfad.sreceivedvolume, coalesce(ts1.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', ts1.jsondata->'stransdisplaystatus'->>'en-US') ssamplecondition,"
				+ " coalesce(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts2.jsondata->'stransdisplaystatus'->>'en-US') ssamplestatus, bfad.nsamplestatus,"
				+ " bfad.nsamplecondition, r.sreason, concat(bfad.jsondata->>'sparentsamplecode', ' | ', bfad.ncohortno)"
				+ " sparentsamplecodecohortno,COALESCE(NULLIF(BFAD.JSONDATA->>'"+Enumeration.FormCode.ECATALOGREQUESTAPPROVAL.getFormCode()+"','-'),'-') AS ecatalogrequestapproval,    "
				+ " COALESCE(NULLIF(BFAD.JSONDATA->>'sformnumber','-'),'-') AS sformnumber from bioformacceptancedetails bfad "
				+ " join product p on p.nproductcode=bfad.nproductcode and p.nsitecode=" + userInfo.getNmastersitecode()
				+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join productcategory pc on pc.nproductcatcode=p.nproductcatcode and pc.nsitecode="
				+ userInfo.getNmastersitecode() + " and pc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join transactionstatus"
				+ " ts1 on bfad.nsamplecondition=ts1.ntranscode and ts1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join transactionstatus ts2 on"
				+ " bfad.nsamplestatus=ts2.ntranscode and ts2.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left join reason r on r.nreasoncode=bfad.nreasoncode and r.nsitecode="
				+ userInfo.getNmastersitecode() + " and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bfad.nsitecode="
				+ userInfo.getNtranssitecode() + " and bfad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bfad.nbioformacceptancecode="
				+ nbioFormAcceptanceCode + " order by nserialno desc";

		return jdbcTemplate.queryForList(strChildGet);
	}

	public BioFormAcceptance getReceiveFormAcceptanceDetails(int nbioFormAcceptanceCode, UserInfo userInfo)
			throws Exception {

		final String strConcatQry = " and bfa.nbioformacceptancecode=" + nbioFormAcceptanceCode;
		final String strQuery = getParentFormAcceptanceQry(strConcatQry, userInfo);
		return (BioFormAcceptance) jdbcUtilityTemplateFunction.queryForObject(strQuery, BioFormAcceptance.class,
				jdbcTemplate);

	}

	public String auditQuery(final int nbioFormAcceptanceCode, final String sbioFormAcceptanceDetailsCode,
			final UserInfo userInfo) throws Exception {
		String strConcat = "";
		if (sbioFormAcceptanceDetailsCode != null && sbioFormAcceptanceDetailsCode != "") {
			strConcat = " and bfad.nbioformacceptancedetailscode in (" + sbioFormAcceptanceDetailsCode + ") ";
		}

		final String strAuditQry = "select bfa.nbioformacceptancecode, bfad.nbioformacceptancedetailscode, bfa.sformnumber,"
				+ " bfa.nformtypecode, bfa.noriginsitecode, concat(u.sfirstname, ' ', u.slastname) ssentusername,"
				+ " bfa.nsenderuserrolecode, to_char(bfa.dtransferdate, '" + userInfo.getSsitedate()
				+ "') stransferdate," + " bfa.ntransactionstatus,"
				+ " bfa.jsondata->>'sremarks' sremarks, to_char(bfa.ddeliverydate, '" + userInfo.getSsitedate()
				+ "') sdeliverydate, bfa.jsondata->>'scouriername' scouriername," + " bfa.scourierno,"
				+ " bfa.jsondata->>'svalidationremarks' svalidationremarks, bfa.jsondata->>'srecipientname' srecipientname, to_char(bfa.dreceiveddate, '"
				+ userInfo.getSsitedate()
				+ "') sreceiveddate, bfa.nreceivingtemperaturecode, bfa.nreceivingofficercode,"
				+ " bfa.jsondata->>'sreceivingofficername' sreceivingofficername, bfa.jsondata->>'scompletionremarks' scompletionremarks,"
				+ " bfad.nbioprojectcode, bfad.nbioparentsamplecode, p.sproductname, bfad.srepositoryid,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') ssamplecondition,"
				+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts1.jsondata->'stransdisplaystatus'->>'en-US') ssamplestatus from bioformacceptance bfa"
				+ " join users u on u.nusercode=bfa.nsenderusercode and u.nsitecode=" + userInfo.getNmastersitecode()
				+ " and u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join bioformacceptancedetails bfad on bfad.nbioformacceptancecode=bfa.nbioformacceptancecode"
				+ " and bfad.nsitecode=" + userInfo.getNtranssitecode() + " and bfad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join product p on p.nproductcode=bfad.nproductcode and p.nsitecode=" + userInfo.getNmastersitecode()
				+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join transactionstatus ts on ts.ntranscode=bfad.nsamplecondition and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join transactionstatus ts1 on ts1.ntranscode=bfad.nsamplestatus and ts1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bfa.nsitecode="
				+ userInfo.getNtranssitecode() + " and bfa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bfa.nbioformacceptancecode="
				+ nbioFormAcceptanceCode + strConcat + " order by bfa.nbioformacceptancecode";
		return strAuditQry;
	}

	public String auditParentQuery(final int nbioFormAcceptanceCode, final String concatSelect, final String concatJoin,
			final UserInfo userInfo) throws Exception {
		final String strAuditQry = "select bfa.nbioformacceptancecode, bfa.sformnumber, bfa.noriginsitecode,"
				+ " bfa.ntransactionstatus " + concatSelect + " from bioformacceptance bfa " + concatJoin
				+ " where bfa.nsitecode=" + userInfo.getNtranssitecode() + " and bfa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bfa.nbioformacceptancecode="
				+ nbioFormAcceptanceCode;
		return strAuditQry;
	}

	public String childAuditQuery(final int nbioFormAcceptanceCode, final String nbioFormAcceptanceDetailsCode,
			final UserInfo userInfo) throws Exception {
		final String strChildAuditQuery = "select bfa.nbioformacceptancecode, bfad.nbioformacceptancedetailscode,"
				+ " bfa.sformnumber, coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') ssamplecondition,"
				+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') ssamplestatus, bfad.srepositoryid, bfad.nreasoncode,"
				+ " bfad.nproductcode, bfad.slocationcode, bfad.nproductcode, bfad.svolume, bfad.sreceivedvolume "
				+ " from bioformacceptance bfa join bioformacceptancedetails bfad on"
				+ " bfad.nbioformacceptancecode=bfa.nbioformacceptancecode and bfad.nsitecode="
				+ userInfo.getNtranssitecode() + " and bfad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join transactionstatus ts on ts.ntranscode=bfad.nsamplecondition and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join transactionstatus ts1 on ts1.ntranscode=bfad.nsamplestatus and ts1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bfa.nsitecode="
				+ userInfo.getNtranssitecode() + " and bfa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bfa.nbioformacceptancecode="
				+ nbioFormAcceptanceCode + " and bfad.nbioformacceptancedetailscode in ("
				+ nbioFormAcceptanceDetailsCode + ") order by bfa.nbioformacceptancecode desc;";
		return strChildAuditQuery;
	}

	public ResponseEntity<Object> moveToDisposeSamples(final int nbioFormAcceptanceCode,
			final String nbioFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception {

		jdbcTemplate.execute(
				"lock table lockbioformacceptancedetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final String concatString = " and nsamplestatus in ("
				+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.TOBERETURN.gettransactionstatus() + ")";

		final String validatedFormAcceptanceDetailsCode = findStatusFormAcceptanceDetails(nbioFormAcceptanceCode,
				nbioFormAcceptanceDetailsCode, concatString, userInfo);

		if (validatedFormAcceptanceDetailsCode != null) {

			final String strAuditAfterQry = childAuditQuery(nbioFormAcceptanceCode, validatedFormAcceptanceDetailsCode,
					userInfo);
			List<BioFormAcceptanceDetails> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
					new BioFormAcceptanceDetails());

			int formAcceptanceHistoryDetailsPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='bioformacceptdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);

			String strUpdateQry = "update bioformacceptancedetails set nsamplestatus="
					+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbioformacceptancedetailscode in (" + validatedFormAcceptanceDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			strUpdateQry += "insert into bioformacceptdetailshistory"
					+ " (nbioformacceptdetailshistorycode, nbioformacceptancedetailscode, nbioformacceptancecode,"
					+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
					+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
					+ "select " + formAcceptanceHistoryDetailsPk
					+ "+rank()over(order by nbioformacceptancedetailscode),"
					+ " nbioformacceptancedetailscode, nbioformacceptancecode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from bioformacceptancedetails where nbioformacceptancedetailscode in ("
					+ validatedFormAcceptanceDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by nbioformacceptancedetailscode;";
			strUpdateQry += " update seqnobiobankmanagement set nsequenceno=(" + "select "
					+ formAcceptanceHistoryDetailsPk
					+ " + count(nbioformacceptancedetailscode) from bioformacceptancedetails where"
					+ " nbioformacceptancedetailscode in (" + validatedFormAcceptanceDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " where stablename='bioformacceptdetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(strUpdateQry);

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
//					+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + (chainCustodyPk)
//					+ " + rank() over(order by bfad.nbioformacceptancedetailscode), " + userInfo.getNformcode() + ", "
//					+ " bfad.nbioformacceptancedetailscode, 'nbioformacceptancedetailscode', "
//					+ " 'bioformacceptancedetails', bfa.sformnumber, "
//					+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ", " + userInfo.getNusercode()
//					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
//					+ userInfo.getNtimezonecode() + ", "
//					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " '"
//					+ commonFunction.getMultilingualMessage("IDS_MOVETODISPOSESAMPLES", userInfo.getSlanguagefilename())
//					+ " ' || ' "
//					+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//					+ " [' || bfad.srepositoryid || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//					+ " [' || (bfad.jsondata->>'sparentsamplecode') || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//					+ " [' || bfa.sformnumber || ']', " + userInfo.getNtranssitecode() + ", "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " from bioformacceptancedetails bfad"
//					+ " join bioformacceptance bfa on bfa.nbioformacceptancecode = bfad.nbioformacceptancecode"
//					+ " and bfa.nsitecode = " + userInfo.getNtranssitecode() + " and bfa.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bfad.nsitecode = "
//					+ userInfo.getNtranssitecode() + " and bfad.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and bfad.nbioformacceptancedetailscode in (" + validatedFormAcceptanceDetailsCode + ")" + ";";
//
//			String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//					+ " + count(nbioformacceptancedetailscode)" + " from bioformacceptancedetails"
//					+ " where nbioformacceptancedetailscode in (" + validatedFormAcceptanceDetailsCode + ")"
//					+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ ") where stablename = 'chaincustody' and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//			jdbcTemplate.execute(strChainCustody);
//			jdbcTemplate.execute(strSeqUpdate);

			// ===== COC: END =====

			List<BioFormAcceptanceDetails> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
					new BioFormAcceptanceDetails());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_MOVETODISPOSESAMPLES"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioFormAcceptance = getChildInitialGet(nbioFormAcceptanceCode, userInfo);
			outputMap.put("lstChildBioFormAcceptance", lstChildBioFormAcceptance);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDRETURNSAMPLES",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> moveToReturnSamples(final int nbioFormAcceptanceCode,
			final String nbioFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception {

		jdbcTemplate.execute(
				"lock table lockbioformacceptancedetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final String concatString = " and nsamplestatus in ("
				+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ")";

		final String validatedFormAcceptanceDetailsCode = findStatusFormAcceptanceDetails(nbioFormAcceptanceCode,
				nbioFormAcceptanceDetailsCode, concatString, userInfo);

		if (validatedFormAcceptanceDetailsCode != null) {

			final String strAuditAfterQry = childAuditQuery(nbioFormAcceptanceCode, validatedFormAcceptanceDetailsCode,
					userInfo);
			List<BioFormAcceptanceDetails> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
					new BioFormAcceptanceDetails());

			int formAcceptanceHistoryDetailsPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='bioformacceptdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			String strUpdateQry = "update bioformacceptancedetails set nsamplestatus="
					+ Enumeration.TransactionStatus.TOBERETURN.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbioformacceptancedetailscode in (" + validatedFormAcceptanceDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			strUpdateQry += "insert into bioformacceptdetailshistory"
					+ " (nbioformacceptdetailshistorycode, nbioformacceptancedetailscode, nbioformacceptancecode,"
					+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
					+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
					+ "select " + formAcceptanceHistoryDetailsPk
					+ "+rank()over(order by nbioformacceptancedetailscode),"
					+ " nbioformacceptancedetailscode, nbioformacceptancecode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from bioformacceptancedetails where nbioformacceptancedetailscode in ("
					+ validatedFormAcceptanceDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by nbioformacceptancedetailscode;";
			strUpdateQry += " update seqnobiobankmanagement set nsequenceno=(" + "select "
					+ formAcceptanceHistoryDetailsPk
					+ " + count(nbioformacceptancedetailscode) from bioformacceptancedetails where"
					+ " nbioformacceptancedetailscode in (" + validatedFormAcceptanceDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " where stablename='bioformacceptdetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(strUpdateQry);

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
//					+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + (chainCustodyPk)
//					+ " + rank() over(order by bfad.nbioformacceptancedetailscode), " + userInfo.getNformcode() + ", "
//					+ " bfad.nbioformacceptancedetailscode, 'nbioformacceptancedetailscode', "
//					+ " 'bioformacceptancedetails', bfa.sformnumber, "
//					+ Enumeration.TransactionStatus.TOBERETURN.gettransactionstatus() + ", " + userInfo.getNusercode()
//					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
//					+ userInfo.getNtimezonecode() + ", "
//					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " '"
//					+ commonFunction.getMultilingualMessage("IDS_MOVETORETURNSAMPLES", userInfo.getSlanguagefilename())
//					+ " ' || ' "
//					+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//					+ " [' || bfad.srepositoryid || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//					+ " [' || (bfad.jsondata->>'sparentsamplecode') || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//					+ " [' || bfa.sformnumber || ']', " + userInfo.getNtranssitecode() + ", "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " from bioformacceptancedetails bfad"
//					+ " join bioformacceptance bfa on bfa.nbioformacceptancecode = bfad.nbioformacceptancecode"
//					+ " and bfa.nsitecode = " + userInfo.getNtranssitecode() + " and bfa.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bfad.nsitecode = "
//					+ userInfo.getNtranssitecode() + " and bfad.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and bfad.nbioformacceptancedetailscode in (" + validatedFormAcceptanceDetailsCode + ")" + ";";
//
//			String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//					+ " + count(nbioformacceptancedetailscode)" + " from bioformacceptancedetails"
//					+ " where nbioformacceptancedetailscode in (" + validatedFormAcceptanceDetailsCode + ")"
//					+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ ") where stablename = 'chaincustody' and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//			jdbcTemplate.execute(strChainCustody);
//			jdbcTemplate.execute(strSeqUpdate);
			// ===== COC: END =====

			List<BioFormAcceptanceDetails> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
					new BioFormAcceptanceDetails());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_MOVETORETURNSAMPLES"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioFormAcceptance = getChildInitialGet(nbioFormAcceptanceCode, userInfo);
			outputMap.put("lstChildBioFormAcceptance", lstChildBioFormAcceptance);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDDISPOSEDSAMPLES",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> undoReturnDisposeSamples(final int nbioFormAcceptanceCode,
			final String nbioFormAcceptanceDetailsCode, final UserInfo userInfo) throws Exception {

		jdbcTemplate.execute(
				"lock table lockbioformacceptancedetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final String concatString = " and nsamplestatus in ("
				+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.TOBERETURN.gettransactionstatus() + ")";

		final String validatedFormAcceptanceDetailsCode = findStatusFormAcceptanceDetails(nbioFormAcceptanceCode,
				nbioFormAcceptanceDetailsCode, concatString, userInfo);

		if (validatedFormAcceptanceDetailsCode != null) {

			final String strAuditAfterQry = childAuditQuery(nbioFormAcceptanceCode, validatedFormAcceptanceDetailsCode,
					userInfo);
			List<BioFormAcceptanceDetails> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
					new BioFormAcceptanceDetails());

			int formAcceptanceHistoryDetailsPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='bioformacceptdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			String strUpdateQry = "update bioformacceptancedetails set nsamplestatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbioformacceptancedetailscode in (" + validatedFormAcceptanceDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			strUpdateQry += "insert into bioformacceptdetailshistory"
					+ " (nbioformacceptdetailshistorycode, nbioformacceptancedetailscode, nbioformacceptancecode,"
					+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
					+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
					+ "select " + formAcceptanceHistoryDetailsPk
					+ "+rank()over(order by nbioformacceptancedetailscode),"
					+ " nbioformacceptancedetailscode, nbioformacceptancecode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from bioformacceptancedetails where nbioformacceptancedetailscode in ("
					+ validatedFormAcceptanceDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by nbioformacceptancedetailscode;";
			strUpdateQry += " update seqnobiobankmanagement set nsequenceno=(" + "select "
					+ formAcceptanceHistoryDetailsPk
					+ " + count(nbioformacceptancedetailscode) from bioformacceptancedetails where"
					+ " nbioformacceptancedetailscode in (" + validatedFormAcceptanceDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " where stablename='bioformacceptdetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(strUpdateQry);

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
//					+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + (chainCustodyPk)
//					+ " + rank() over(order by bfad.nbioformacceptancedetailscode), " + userInfo.getNformcode() + ", "
//					+ " bfad.nbioformacceptancedetailscode, 'nbioformacceptancedetailscode', "
//					+ " 'bioformacceptancedetails', bfa.sformnumber, "
//					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", " + userInfo.getNusercode()
//					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
//					+ userInfo.getNtimezonecode() + ", "
//					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " '"
//					+ commonFunction.getMultilingualMessage("IDS_UNDODISPOSERETURNSAMPLES",
//							userInfo.getSlanguagefilename())
//					+ " ' || ' "
//					+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//					+ " [' || bfad.srepositoryid || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//					+ " [' || (bfad.jsondata->>'sparentsamplecode') || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//					+ " [' || bfa.sformnumber || ']', " + userInfo.getNtranssitecode() + ", "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " from bioformacceptancedetails bfad"
//					+ " join bioformacceptance bfa on bfa.nbioformacceptancecode = bfad.nbioformacceptancecode"
//					+ " and bfa.nsitecode = " + userInfo.getNtranssitecode() + " and bfa.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bfad.nsitecode = "
//					+ userInfo.getNtranssitecode() + " and bfad.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and bfad.nbioformacceptancedetailscode in (" + validatedFormAcceptanceDetailsCode + ")" + ";";
//
//			String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//					+ " + count(nbioformacceptancedetailscode)" + " from bioformacceptancedetails"
//					+ " where nbioformacceptancedetailscode in (" + validatedFormAcceptanceDetailsCode + ")"
//					+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ ") where stablename = 'chaincustody' and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//			jdbcTemplate.execute(strChainCustody);
//			jdbcTemplate.execute(strSeqUpdate);
			// ===== COC: END =====

			List<BioFormAcceptanceDetails> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
					new BioFormAcceptanceDetails());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_UNDODISPOSERETURNSAMPLES"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioFormAcceptance = getChildInitialGet(nbioFormAcceptanceCode, userInfo);
			outputMap.put("lstChildBioFormAcceptance", lstChildBioFormAcceptance);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDISPOSERETURNSAMPLES",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings({ "static-access", "unchecked" })
	public ResponseEntity<Object> storeSamples(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		jdbcTemplate.execute(
				"lock table locksamplestoragetransaction " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String sbioFormAcceptanceDetailsCode = (String) inputMap.get("nbioformacceptancedetailscode");
		List<Map<String, Object>> nonAccessibleSamples = checkAccessibleSamples(sbioFormAcceptanceDetailsCode,
				userInfo);

		if (!nonAccessibleSamples.isEmpty()) {
			final String srepositoryId = nonAccessibleSamples.stream().map(x -> (String) x.get("srepositoryid"))
					.collect(Collectors.joining(","));
			String salertMsg = srepositoryId + " " + commonFunction
					.getMultilingualMessage("IDS_SAMPLESARENOTACCESSIBLE", userInfo.getSlanguagefilename());
			outputMap.put("containsNonAccessibleSamples", true);
			outputMap.put("salertMsg", salertMsg);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());

		final int nbioFormAcceptanceCode = (int) inputMap.get("nbioformacceptancecode");
		final String nbioFormAcceptanceDetailsCode = (String) inputMap.get("nbioformacceptancedetailscode");

		final String concatString = " and nsamplestatus in ("
				+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ") and nsamplecondition="
				+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus();

		final String validatedFormAcceptanceDetailsCode = findStatusFormAcceptanceDetails(nbioFormAcceptanceCode,
				nbioFormAcceptanceDetailsCode, concatString, userInfo);

		if (validatedFormAcceptanceDetailsCode != null) {

			final int nstorageinstrumentcode = Integer.valueOf(inputMap.get("nstorageinstrumentcode").toString());

			final String storageLocQry = "select nsamplestoragelocationcode, nsamplestorageversioncode from storageinstrument "
					+ "where nstorageinstrumentcode = " + nstorageinstrumentcode + " " + "and nsitecode = "
					+ userInfo.getNmastersitecode() + " " + "and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final StorageInstrument objSelectedStructure = (StorageInstrument) jdbcUtilityTemplateFunction
					.queryForObject(storageLocQry, StorageInstrument.class, jdbcTemplate);
			final int nsamplestoragelocationcode = objSelectedStructure.getNsamplestoragelocationcode();
			final int nsamplestorageversioncode = objSelectedStructure.getNsamplestorageversioncode();

			// modified by sujatha ATE_274 for
			// 4(sextractedsampleid,concentration,qcplatform,eluent) fields newly selected
			// for storing into samplestorageadditionalinfo table while storing sample
			// BGSI-218
			final String sampleRecQry = "select bfad.nbioformacceptancedetailscode, bfad.nbiosamplereceivingcode,"
					+ " bfad.srepositoryid, bfad.svolume, bfad.sreceivedvolume, bfad.nproductcode, p.sproductname, pc.sproductcatname,"
					+ " bfad.nbioprojectcode, bp.sprojecttitle, bfad.nbioparentsamplecode,"
					+ " bfad.jsondata->>'sparentsamplecode' sparentsamplecode, "
					+ " bfad.jsondata->>'sextractedsampleid' sextractedsampleid, "
					+ " bfad.jsondata->>'sconcentration' sconcentration, "
					+ " bfad.jsondata->>'sqcplatform' sqcplatform, " + " bfad.jsondata->>'seluent' seluent, "
					+ " bfad.ncohortno, bfad.nproductcatcode, bfad.nproductcode,"
					+ " bfad.slocationcode, bfad.jsondata->>'ssubjectid' ssubjectid, bfad.jsondata->>'scasetype' scasetype, bfad.ndiagnostictypecode,"
					+ " bfad.ncontainertypecode, bfad.nstoragetypecode from bioformacceptancedetails bfad"
					+ " join bioproject bp on bp.nbioprojectcode=bfad.nbioprojectcode and bp.nsitecode="
					+ userInfo.getNmastersitecode() + " and bp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " join product p on p.nproductcode=bfad.nproductcode and p.nsitecode="
					+ userInfo.getNmastersitecode() + " and p.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " join productcategory pc on pc.nproductcatcode=p.nproductcatcode and pc.nsitecode="
					+ userInfo.getNmastersitecode() + " and pc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bfad.nsitecode="
					+ userInfo.getNtranssitecode() + " and bfad.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and bfad.nbioformacceptancedetailscode in (" + validatedFormAcceptanceDetailsCode
					+ ") order by bfad.nbioformacceptancedetailscode";

			List<BioFormAcceptanceDetails> lstBioFormAcceptanceDetails = jdbcTemplate.query(sampleRecQry,
					new BioFormAcceptanceDetails());

			if (!lstBioFormAcceptanceDetails.isEmpty()) {
				final int selectedCount = lstBioFormAcceptanceDetails.size();
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
				String strUpdateBioFormAcceptanceDetails = "";
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

						final String sparentSampleCode = (lstBioFormAcceptanceDetails.get(codeIndex)
								.getSparentsamplecode() != null
								&& lstBioFormAcceptanceDetails.get(codeIndex).getSparentsamplecode() != "")
										? "'" + lstBioFormAcceptanceDetails.get(codeIndex).getSparentsamplecode() + "'"
										: null;
						final String sreceivedVolume = (lstBioFormAcceptanceDetails.get(codeIndex).getSreceivedvolume() != null
								&& lstBioFormAcceptanceDetails.get(codeIndex).getSreceivedvolume() != "")
										? "'" + lstBioFormAcceptanceDetails.get(codeIndex).getSreceivedvolume() + "'"
										: null;
						final String ssubjectId = (lstBioFormAcceptanceDetails.get(codeIndex).getSsubjectid() != null
								&& lstBioFormAcceptanceDetails.get(codeIndex).getSsubjectid() != "")
										? "'" + lstBioFormAcceptanceDetails.get(codeIndex).getSsubjectid() + "'"
										: null;
						final String scaseType = (lstBioFormAcceptanceDetails.get(codeIndex).getScasetype() != null
								&& lstBioFormAcceptanceDetails.get(codeIndex).getScasetype() != "")
										? "'" + lstBioFormAcceptanceDetails.get(codeIndex).getScasetype() + "'"
										: null;

						strSampleStorageTransactionInsert += "(" + nseqSampleStorageTransaction + ","
								+ nsamplestoragelocationcode + "," + nsamplestoragemappingcode + ","
								+ lstBioFormAcceptanceDetails.get(codeIndex).getNbioprojectcode() + ",'"
								+ missingPositions.get(i) + "' ,'"
								+ stringUtilityFunction
										.replaceQuote(lstBioFormAcceptanceDetails.get(codeIndex).getSrepositoryid())
								+ "' ," + " json_build_object('Parent Sample Code','"
								+ stringUtilityFunction
										.replaceQuote(lstBioFormAcceptanceDetails.get(codeIndex).getSparentsamplecode())
								+ "'," + "'Case Type','"
								+ stringUtilityFunction
										.replaceQuote(lstBioFormAcceptanceDetails.get(codeIndex).getScasetype())
								+ "'" + ", 'Parent Sample Type','"
								+ stringUtilityFunction
										.replaceQuote(lstBioFormAcceptanceDetails.get(codeIndex).getSproductcatname())
								+ "', 'Project Title','"
								+ stringUtilityFunction
										.replaceQuote(lstBioFormAcceptanceDetails.get(codeIndex).getSprojecttitle())
								+ "', 'Bio Sample Type','"
								+ stringUtilityFunction
										.replaceQuote(lstBioFormAcceptanceDetails.get(codeIndex).getSproductname())
								+ "', 'Volume (μL)','"
								+ stringUtilityFunction
										.replaceQuote(lstBioFormAcceptanceDetails.get(codeIndex).getSreceivedvolume())
								+ "')::jsonb, " + Enumeration.TransactionStatus.YES.gettransactionstatus() + ", '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ userInfo.getNtranssitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
								+ lstBioFormAcceptanceDetails.get(codeIndex).getNbiosamplereceivingcode() + ", "
								+ ninstrumentCode + ", "
								+ lstBioFormAcceptanceDetails.get(codeIndex).getNbioparentsamplecode() + ", "
								+ sparentSampleCode + ", " + lstBioFormAcceptanceDetails.get(codeIndex).getNcohortno()
								+ ", " + lstBioFormAcceptanceDetails.get(codeIndex).getNproductcatcode() + ", "
								+ lstBioFormAcceptanceDetails.get(codeIndex).getNproductcode() + ", " + sreceivedVolume + ", '"
								+ slocationCode + "', " + ssubjectId + ", " + scaseType + ", "
								+ lstBioFormAcceptanceDetails.get(codeIndex).getNdiagnostictypecode() + ", "
								+ lstBioFormAcceptanceDetails.get(codeIndex).getNcontainertypecode() + ", "
								+ lstBioFormAcceptanceDetails.get(codeIndex).getNstoragetypecode() + "),";

						strUpdateBioFormAcceptanceDetails += "update bioformacceptancedetails set nsamplestatus="
								+ Enumeration.TransactionStatus.STORED.gettransactionstatus()
								+ ", nsamplestoragetransactioncode=" + nseqSampleStorageTransaction
								+ ", slocationcode='" + slocationCode + "'" + ", dtransactiondate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
								+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
								+ " where nbioformacceptancedetailscode = "
								+ lstBioFormAcceptanceDetails.get(codeIndex).getNbioformacceptancedetailscode()
								+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						// added by sujatha ATE_274 for insert into samplestorageadditionalinfo table
						// while storing samples BGSI-218
						instsamplestorageadditionalinfo += "(" + nseqSampleStorageTransaction + ", "
								+ (lstBioFormAcceptanceDetails.get(codeIndex).getSextractedsampleid() == null
										|| lstBioFormAcceptanceDetails.get(codeIndex).getSextractedsampleid().trim()
												.isEmpty()
														? "null"
														: "'" + stringUtilityFunction
																.replaceQuote(lstBioFormAcceptanceDetails
																		.get(codeIndex).getSextractedsampleid())
																+ "'")
								+ ", "
								+ (lstBioFormAcceptanceDetails.get(codeIndex).getSconcentration() == null
										|| lstBioFormAcceptanceDetails.get(codeIndex).getSconcentration().trim()
												.isEmpty()
														? "null"
														: "'" + stringUtilityFunction
																.replaceQuote(lstBioFormAcceptanceDetails
																		.get(codeIndex).getSconcentration())
																+ "'")
								+ ", "
								+ (lstBioFormAcceptanceDetails.get(codeIndex).getSqcplatform() == null
										|| lstBioFormAcceptanceDetails.get(codeIndex).getSqcplatform().trim().isEmpty()
												? "null"
												: "'" + stringUtilityFunction.replaceQuote(
														lstBioFormAcceptanceDetails.get(codeIndex).getSqcplatform())
														+ "'")
								+ ", "
								+ (lstBioFormAcceptanceDetails.get(codeIndex).getSeluent() == null
										|| lstBioFormAcceptanceDetails.get(codeIndex).getSeluent().trim().isEmpty()
												? "null"
												: "'" + stringUtilityFunction.replaceQuote(
														lstBioFormAcceptanceDetails.get(codeIndex).getSeluent()) + "'")
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
				// added by sujatha ATE_274 BGSI-218
				instsamplestorageadditionalinfo = " INSERT INTO public.samplestorageadditionalinfo("
						+ " nsamplestoragetransactioncode, sextractedsampleid, sconcentration, sqcplatform, seluent, dmodifieddate,"
						+ " nsitecode, nstatus)" + " VALUES"
						+ instsamplestorageadditionalinfo.substring(0, instsamplestorageadditionalinfo.length() - 1)
						+ ";";

				updateSeqNoQry = updateSeqNoQry + " update seqnobasemaster set nsequenceno = "
						+ nseqSampleStorageTransaction + " where stablename = 'samplestoragetransaction' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

				int formAcceptanceHistoryDetailsPk = jdbcTemplate
						.queryForObject("select nsequenceno from seqnobiobankmanagement where "
								+ "stablename='bioformacceptdetailshistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);

				String sformAcceptanceDetailsCode = lstBioFormAcceptanceDetails.stream()
						.map(x -> String.valueOf(x.getNbioformacceptancedetailscode()))
						.collect(Collectors.joining(","));
				String strUpdateQry = "insert into bioformacceptdetailshistory"
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
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from bioformacceptancedetails where nbioformacceptancedetailscode in ("
						+ sformAcceptanceDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by nbioformacceptancedetailscode;";
				strUpdateQry += " update seqnobiobankmanagement set nsequenceno=(" + "select "
						+ formAcceptanceHistoryDetailsPk
						+ " + count(nbioformacceptancedetailscode) from bioformacceptancedetails where"
						+ " nbioformacceptancedetailscode in (" + sformAcceptanceDetailsCode + ") and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
						+ " where stablename='bioformacceptdetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				final String strAuditAfterQry = childAuditQuery(nbioFormAcceptanceCode, sformAcceptanceDetailsCode,
						userInfo);
				List<BioFormAcceptanceDetails> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
						new BioFormAcceptanceDetails());

				// modified by sujatha ATE_274 by adding instsamplestorageadditionalinfo to
				// execute BGSI-218
				jdbcTemplate.execute(strSampleStorageTransactionInsert + instsamplestorageadditionalinfo
						+ updateSeqNoQry + strUpdateBioFormAcceptanceDetails + strUpdateQry);

				// ===== COC: START ===== New
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

				String strChainCustody = "insert into chaincustody ("
				        + "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
				        + "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
				        + "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)"
				        + " select " + (chainCustodyPk)
				        + " + rank() over(order by bfad.nbioformacceptancedetailscode), "
				        + userInfo.getNformcode() + ", "
				        + " bfad.nbioformacceptancedetailscode, 'nbioformacceptancedetailscode', "
				        + " 'bioformacceptancedetails', bfad.srepositoryid, "
				        + Enumeration.TransactionStatus.STORED.gettransactionstatus() + ", "
				        + userInfo.getNusercode() + ", "
				        + userInfo.getNuserrole() + ", '"
				        + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
				        + userInfo.getNtimezonecode() + ", "
				        + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				        + " '"
				        + commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
				        + " [' || coalesce(bfad.srepositoryid,'') || '] ' || '"
				        + commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
				        + " [' || coalesce((bfad.jsondata->>'sparentsamplecode'),'') || '] ' || '"
				        + commonFunction.getMultilingualMessage("IDS_STORAGELOCATIONNAME", userInfo.getSlanguagefilename())
				        + " [' || coalesce(bfad.slocationcode,'') || '] ' || '"
				        + commonFunction.getMultilingualMessage("IDS_SENTFROM", userInfo.getSlanguagefilename())
				        + " [' || coalesce(s.ssitename,'') || '] ' || '"
				        + commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
				        + " [' || coalesce(bfa.sformnumber,'') || ']' "
				        + ", " + userInfo.getNtranssitecode() + ", "
				        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				        + " from bioformacceptancedetails bfad"
				        + " join bioformacceptance bfa on bfa.nbioformacceptancecode = bfad.nbioformacceptancecode"
				        + " join site s on s.nsitecode = bfad.nsitecode"
				        + " and bfa.nsitecode = " + userInfo.getNtranssitecode()
				        + " and bfa.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				        + " where bfad.nsitecode = " + userInfo.getNtranssitecode()
				        + " and bfad.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				        + " and bfad.nbioformacceptancedetailscode in (" + sformAcceptanceDetailsCode + ")"
				        + ";";

				String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
						+ " + count(nbioformacceptancedetailscode)" + " from bioformacceptancedetails"
						+ " where nbioformacceptancedetailscode in (" + sformAcceptanceDetailsCode + ")"
						+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ ") where stablename = 'chaincustody' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				jdbcTemplate.execute(strChainCustody);
				jdbcTemplate.execute(strSeqUpdate);
				// ===== COC: END =====

				List<BioFormAcceptanceDetails> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
						new BioFormAcceptanceDetails());
				lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
				lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
				lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_STORESAMPLES"));

				auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList,
						userInfo);
				List<Map<String, Object>> lstChildBioFormAcceptance = getChildInitialGet(nbioFormAcceptanceCode,
						userInfo);
				outputMap.put("lstChildBioFormAcceptance", lstChildBioFormAcceptance);
				outputMap.put("containsNonAccessibleSamples", false);
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
				"lock table lockbioformacceptancedetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final int nbioFormAcceptanceCode = (int) inputMap.get("nbioformacceptancecode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final int findStatus = findStatusFormAcceptance(nbioFormAcceptanceCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.RECEIVED.gettransactionstatus()) {
			final String nbioFormAcceptanceDetailsCode = (String) inputMap.get("nbioformacceptancedetailscode");
			final int nreasonCode = (int) inputMap.get("nreasoncode");
			final int nsampleCondition = (int) inputMap.get("nsamplecondition");

			final String strAuditAfterQry = childAuditQuery(nbioFormAcceptanceCode, nbioFormAcceptanceDetailsCode,
					userInfo);
			List<BioFormAcceptanceDetails> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
					new BioFormAcceptanceDetails());

			int formAcceptanceHistoryDetailsPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='bioformacceptdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);

			String strUpdateQry = "update bioformacceptancedetails set nreasoncode=" + nreasonCode
					+ ", nsamplecondition=" + nsampleCondition + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbioformacceptancedetailscode in (" + nbioFormAcceptanceDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			strUpdateQry += "insert into bioformacceptdetailshistory"
					+ " (nbioformacceptdetailshistorycode, nbioformacceptancedetailscode, nbioformacceptancecode,"
					+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
					+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
					+ " select " + formAcceptanceHistoryDetailsPk
					+ "+rank()over(order by nbioformacceptancedetailscode), "
					+ " nbioformacceptancedetailscode, nbioformacceptancecode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from bioformacceptancedetails where nbioformacceptancedetailscode in ("
					+ nbioFormAcceptanceDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			strUpdateQry += "update seqnobiobankmanagement set nsequenceno=(select " + formAcceptanceHistoryDetailsPk
					+ " + count(nbioformacceptancedetailscode) from bioformacceptancedetails where nbioformacceptancedetailscode in"
					+ " (" + nbioFormAcceptanceDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " where stablename='bioformacceptdetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(strUpdateQry);

			// ===== COC: START =====
//			if (nbioFormAcceptanceDetailsCode != null
//					&& !String.valueOf(nbioFormAcceptanceDetailsCode).trim().isEmpty()) {
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
//				int chainCustodyPk = jdbcTemplate.queryForObject(
//						"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//						Integer.class);
//
//				String remarksPrefix = (nsampleCondition == 22 || nsampleCondition == 34) ? commonFunction
//						.getMultilingualMessage((nsampleCondition == 22 ? "IDS_STATUSACCEPTED" : "IDS_STATUSREJECTED"),
//								userInfo.getSlanguagefilename())
//						+ " "
//						+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//						+ "'||' ['||br.srepositoryid||'] '||'"
//						+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//						+ " ['|| (bad.jsondata->>'sparentsamplecode') || '] '||'"
//						+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//						+ " '" : "";
//
//				String strChainCustody = "insert into chaincustody ( "
//						+ "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno, "
//						+ "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate, "
//						+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus) " + "select " + chainCustodyPk
//						+ " + rank() over(order by bad.nbioformacceptancedetailscode), " + userInfo.getNformcode()
//						+ ", bad.nbioformacceptancedetailscode, 'nbioformacceptancedetailscode', 'bioformacceptancedetails', COALESCE(bfa.sformnumber, ''), "
//						+ nsampleCondition + ", " + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
//						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
//						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + "'"
//						+ remarksPrefix + "||COALESCE(bfa.sformnumber, ''), " + userInfo.getNtranssitecode() + ", "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " from bioformacceptancedetails bad "
//						+ " left join biosamplereceiving br on br.nbiosamplereceivingcode = bad.nbiosamplereceivingcode  "
//						+ " left join bioformacceptance bfa on bfa.nbioformacceptancecode = bad.nbioformacceptancecode "
//						+ " left join bioparentsamplereceiving bpr on bpr.nbioparentsamplecode = br.nbioparentsamplecode "
//						+ " where bad.nsitecode = " + userInfo.getNtranssitecode() + " and bad.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and bad.nbioformacceptancedetailscode in (" + nbioFormAcceptanceDetailsCode + ");";
//
//				String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//						+ " + count(nbioformacceptancedetailscode) " + " from bioformacceptancedetails"
//						+ " where nbioformacceptancedetailscode in (" + nbioFormAcceptanceDetailsCode + ")"
//						+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ ") where stablename = 'chaincustody' and nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//				jdbcTemplate.execute(strChainCustody);
//				jdbcTemplate.execute(strSeqUpdate);
//			}
			// ===== COC: END =====

			List<BioFormAcceptanceDetails> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
					new BioFormAcceptanceDetails());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_SAMPLEVALIDATION"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioFormAcceptance = getChildInitialGet(nbioFormAcceptanceCode, userInfo);
			outputMap.put("lstChildBioFormAcceptance", lstChildBioFormAcceptance);
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

	public ResponseEntity<Object> updateReceiveBioFormAcceptance(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		jdbcTemplate.execute("lock table lockbioformacceptance " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbioformacceptancedetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final ObjectMapper objMapper = new ObjectMapper();

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final int nbioFormAcceptanceCode = (int) inputMap.get("nbioformacceptancecode");
		final int nbioDirectTransferCode = (int) inputMap.get("nbiodirecttransfercode");
		final int nbioRequestBasedTransferCode = (int) inputMap.get("nbiorequestbasedtransfercode");
		final int nbioBankReturnCode = (int) inputMap.get("nbiobankreturncode");
		final int nbioThirdPartyReturnCode = (int) inputMap.get("nbiothirdpartyreturncode");
		final int nformTypeCode = (int) inputMap.get("nformtypecode");
		final int noriginSiteCode = (int) inputMap.get("noriginsitecode");
		final int findStatus = findStatusFormAcceptance(nbioFormAcceptanceCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			final String concatSelect = ", to_char(bfa.ddeliverydate, '" + userInfo.getSsitedate()
					+ "') sdeliverydate, bfa.ncouriercode, bfa.jsondata->>'srecipientname' srecipientname, "
					+ " to_char(bfa.dreceiveddate, '" + userInfo.getSsitedate() + "') sreceiveddate ";
			final String strAuditQry = auditParentQuery(nbioFormAcceptanceCode, concatSelect, "", userInfo);

			List<BioFormAcceptance> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditQry,
					new BioFormAcceptance());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);

			BioFormAcceptance objBioFormAcceptance = objMapper.convertValue(inputMap.get("bioFormAcceptance"),
					BioFormAcceptance.class);

			final String sreceivedDate = (objBioFormAcceptance.getSreceiveddate() != null
					&& !objBioFormAcceptance.getSreceiveddate().isEmpty()) ? "'"
							+ objBioFormAcceptance.getSreceiveddate().toString().replace("T", " ").replace("Z", "")
							+ "'" : null;

			int formAcceptanceHistoryPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='bioformacceptancehistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			formAcceptanceHistoryPk++;
			int formAcceptanceHistoryDetailsPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='bioformacceptdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);

			final String strUpdateFormAcceptance = "update bioformacceptance set dreceiveddate=" + sreceivedDate + ", "
					+ " jsondata=jsondata || '{\"srecipientname\": \""
					+ stringUtilityFunction.replaceQuote(objBioFormAcceptance.getSrecipientname())
					+ "\"}' , ntransactionstatus=" + Enumeration.TransactionStatus.RECEIVED.gettransactionstatus()
					+ ", dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ " ntztransactiondate=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbioformacceptancecode=" + nbioFormAcceptanceCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final String strUpdateFormAcceptanceDetails = "update bioformacceptancedetails set nsamplestatus="
					+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbioformacceptancecode=" + nbioFormAcceptanceCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final String strFormAcceptanceHistory = "insert into bioformacceptancehistory (nbioformacceptancehistorycode,"
					+ " nbioformacceptancecode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
					+ " noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode,"
					+ " nstatus) values (" + formAcceptanceHistoryPk + ", " + nbioFormAcceptanceCode + ", "
					+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			final String strFormAcceptanceDetailsHistory = "insert into bioformacceptdetailshistory"
					+ " (nbioformacceptdetailshistorycode, nbioformacceptancedetailscode, nbioformacceptancecode,"
					+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
					+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
					+ "select " + formAcceptanceHistoryDetailsPk
					+ "+rank()over(order by nbioformacceptancedetailscode),"
					+ " nbioformacceptancedetailscode, nbioformacceptancecode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from bioformacceptancedetails where " + " nbioformacceptancecode=" + nbioFormAcceptanceCode
					+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by nbioformacceptancedetailscode;";
			final String strSeqNoUpdate = " update seqnobiobankmanagement set nsequenceno=" + formAcceptanceHistoryPk
					+ " where stablename='bioformacceptancehistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final String strSeqNoDetailsUpdate = " update seqnobiobankmanagement set nsequenceno=(" + "select "
					+ formAcceptanceHistoryDetailsPk
					+ " + count(nbioformacceptancedetailscode) from bioformacceptancedetails where"
					+ " nbioformacceptancecode=" + nbioFormAcceptanceCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " where stablename='bioformacceptdetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			String strUpdateDirectTransfer = "";
			String strUpdateDirectTransferDetails = "";
			String strUpdateReqBasedTransfer = "";
			String strUpdateReqBasedTransferDetails = "";
			String strUpdateBankReturn = "";
			String strUpdateBankReturnDetails = "";
			String strUpdateThirdPartyReturn = "";
			String strUpdateThirdPartyReturnDetails = "";
			if (nformTypeCode == Enumeration.FormType.Transfer.getnformtype()) {
				if (nbioDirectTransferCode != -1) {
					strUpdateDirectTransfer = "update biodirecttransfer set ntransactionstatus="
							+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", dtransactiondate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
							+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ " where nbiodirecttransfercode=" + nbioDirectTransferCode + " and nsitecode="
							+ noriginSiteCode + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					strUpdateDirectTransferDetails = "update biodirecttransferdetails set ntransferstatus="
							+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", dtransactiondate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
							+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ " where nbiodirecttransfercode=" + nbioDirectTransferCode + " and nsitecode="
							+ noriginSiteCode + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransferstatus != "
							+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ";";
				}
				if (nbioRequestBasedTransferCode != -1) {
					strUpdateReqBasedTransfer = "update biorequestbasedtransfer set ntransactionstatus="
							+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", dtransactiondate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
							+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ " where nbiorequestbasedtransfercode=" + nbioRequestBasedTransferCode + " and nsitecode="
							+ noriginSiteCode + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					strUpdateReqBasedTransferDetails = "update biorequestbasedtransferdetails set ntransferstatus="
							+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", dtransactiondate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
							+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ " where nbiorequestbasedtransfercode=" + nbioRequestBasedTransferCode + " and nsitecode="
							+ noriginSiteCode + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransferstatus != "
							+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ";";
				}
			} else {
				if (nbioBankReturnCode != -1) {
					int intBankReturnHistoryPK = jdbcTemplate
							.queryForObject(
									"select nsequenceno from seqnobiobankmanagement where "
											+ "stablename='biobankreturnhistory' and nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
									Integer.class)
							+ 1;
					int intBankReturnHistoryDetailsPK = jdbcTemplate
							.queryForObject(
									"select nsequenceno from seqnobiobankmanagement where "
											+ "stablename='biobankreturndetailshistory' and nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
									Integer.class);

					strUpdateBankReturn = "update biobankreturn set ntransactionstatus="
							+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", dtransactiondate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
							+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ " where nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode=" + noriginSiteCode
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					strUpdateBankReturn += "insert into biobankreturnhistory (nbiobankreturnhistorycode, nbiobankreturncode,"
							+ " ntransactionstatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
							+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) values ("
							+ intBankReturnHistoryPK + ", " + nbioBankReturnCode + ", "
							+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
					strUpdateBankReturn += "update seqnobiobankmanagement set nsequenceno=" + intBankReturnHistoryPK
							+ " where stablename='biobankreturnhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					strUpdateDirectTransferDetails = "update biobankreturndetails set nsamplestatus="
							+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", dtransactiondate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
							+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ " where nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode=" + noriginSiteCode
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsamplestatus not in ("
							+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ", "
							+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ");";
					strUpdateDirectTransferDetails += "insert into biobankreturndetailshistory"
							+ " (nbiobankreturndetailshistorycode, nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition,"
							+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
							+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) select "
							+ intBankReturnHistoryDetailsPK + "+ rank() over(order by nbiobankreturndetailscode)"
							+ ", nbiobankreturndetailscode, nbiobankreturncode, nsamplecondition, "
							+ " nsamplestatus, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ userInfo.getNtimezonecode() + ", "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", nsitecode, "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " from biobankreturndetails where nbiobankreturncode=" + nbioBankReturnCode
							+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsamplestatus not in ("
							+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ", "
							+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ")"
							+ " order by nbiobankreturndetailscode;";
					strUpdateDirectTransferDetails += "update seqnobiobankmanagement set nsequenceno=(select "
							+ intBankReturnHistoryDetailsPK
							+ "+ count(nbiobankreturndetailscode) from biobankreturndetails where "
							+ " nbiobankreturncode=" + nbioBankReturnCode + " and nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsamplestatus not in ("
							+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ", "
							+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ")) where"
							+ " stablename='biobankreturndetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				}
				if (nbioThirdPartyReturnCode != -1) {
					int intThirdPartyReturnHistoryPK = jdbcTemplate
							.queryForObject(
									"select nsequenceno from seqnobiobankmanagement where "
											+ "stablename='biothirdpartyreturnhistory' and nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
									Integer.class)
							+ 1;
					int intThirdPartyReturnHistoryDetailsPK = jdbcTemplate
							.queryForObject(
									"select nsequenceno from seqnobiobankmanagement where "
											+ "stablename='biothirdpartyreturndetailshistory' and nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
									Integer.class);
					strUpdateThirdPartyReturn = "update biothirdpartyreturn set ntransactionstatus="
							+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", dtransactiondate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
							+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ " where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and nsitecode="
							+ noriginSiteCode + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					strUpdateThirdPartyReturn += "insert into biothirdpartyreturnhistory (nbiothirdpartyreturnhistorycode, nbiothirdpartyreturncode,"
							+ " ntransactionstatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
							+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) values ("
							+ intThirdPartyReturnHistoryPK + ", " + nbioThirdPartyReturnCode + ", "
							+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
					strUpdateThirdPartyReturn += "update seqnobiobankmanagement set nsequenceno="
							+ intThirdPartyReturnHistoryPK
							+ " where stablename='biothirdpartyreturnhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					strUpdateThirdPartyReturnDetails = "update biothirdpartyreturndetails set nsamplestatus="
							+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", dtransactiondate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
							+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ " where nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and nsitecode="
							+ noriginSiteCode + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
							+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ";";
					strUpdateThirdPartyReturnDetails += "insert into biothirdpartyreturndetailshistory"
							+ " (nbiothirdpartyreturndetailshistorycode, nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition,"
							+ " nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,"
							+ " nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) select "
							+ intThirdPartyReturnHistoryDetailsPK
							+ "+ rank() over(order by nbiothirdpartyreturndetailscode)"
							+ ", nbiothirdpartyreturndetailscode, nbiothirdpartyreturncode, nsamplecondition, "
							+ " nsamplestatus, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ userInfo.getNtimezonecode() + ", "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", nsitecode, "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " from biothirdpartyreturndetails where" + " nbiothirdpartyreturncode="
							+ nbioThirdPartyReturnCode + " and nsitecode=" + userInfo.getNtranssitecode()
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsamplestatus != "
							+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus()
							+ " order by nbiothirdpartyreturndetailscode;";
					strUpdateThirdPartyReturnDetails += "update seqnobiobankmanagement set nsequenceno=(select "
							+ intThirdPartyReturnHistoryDetailsPK
							+ "+ count(nbiothirdpartyreturndetailscode) from biothirdpartyreturndetails where "
							+ " nbiothirdpartyreturncode=" + nbioThirdPartyReturnCode + " and nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus != "
							+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ") where"
							+ " stablename='biothirdpartyreturndetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				}
			}

			jdbcTemplate.execute(strUpdateFormAcceptance + strUpdateFormAcceptanceDetails + strUpdateDirectTransfer
					+ strUpdateDirectTransferDetails + strUpdateThirdPartyReturn + strUpdateThirdPartyReturnDetails
					+ strUpdateReqBasedTransfer + strUpdateReqBasedTransferDetails + strUpdateBankReturn
					+ strUpdateBankReturnDetails + strFormAcceptanceHistory + strFormAcceptanceDetailsHistory
					+ strSeqNoUpdate + strSeqNoDetailsUpdate);

			// ===== COC: START ===== New
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

			String strChainCustody = "insert into chaincustody ("
			        + "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
			        + "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
			        + "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)"
			        + " select " + (chainCustodyPk)
			        + " + rank() over(order by bfad.nbioformacceptancedetailscode), "
			        + userInfo.getNformcode() + ", "
			        + " bfad.nbioformacceptancedetailscode, 'nbioformacceptancedetailscode', "
			        + " 'bioformacceptancedetails', bfad.srepositoryid, "
			        + Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", "
			        + userInfo.getNusercode() + ", "
			        + userInfo.getNuserrole() + ", '"
			        + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
			        + userInfo.getNtimezonecode() + ", "
			        + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
			        + " '"
			        + commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
			        + " [' || coalesce(bfad.srepositoryid,'') || '] ' || '"
			        + commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
			        + " [' || coalesce((bfad.jsondata->>'sparentsamplecode'),'') || '] ' || '"
			        + commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
			        + " [' || coalesce(bfa.sformnumber,'') || '] ' || '"
			        + commonFunction.getMultilingualMessage("IDS_SENTFROM", userInfo.getSlanguagefilename())
			        + " [' || coalesce(s.ssitename,'') || ']' "
			        + ", " + userInfo.getNtranssitecode() + ", "
			        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			        + " from bioformacceptancedetails bfad"
			        + " join bioformacceptance bfa on bfa.nbioformacceptancecode = bfad.nbioformacceptancecode"
			        + " join site s on s.nsitecode = bfa.noriginsitecode"
			        + " and bfa.nsitecode = " + userInfo.getNtranssitecode()
			        + " and bfa.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			        + " where bfad.nsitecode = " + userInfo.getNtranssitecode()
			        + " and bfad.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			        + " and bfad.nbioformacceptancecode = " + nbioFormAcceptanceCode
			        + ";";

			String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
					+ " + count(nbioformacceptancedetailscode)" + " from bioformacceptancedetails"
					+ " where nbioformacceptancecode = " + nbioFormAcceptanceCode + " and nsitecode = "
					+ userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ") where stablename = 'chaincustody' and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strChainCustody);
			jdbcTemplate.execute(strSeqUpdate);
			// ===== COC: END =====

			List<BioFormAcceptance> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditQry,
					new BioFormAcceptance());
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_RECEIVEFORMACCEPTANCE"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			// added by sujatha ATE_274 BGSI-148 for Sending Mail when form receive action
			final Map<String, Object> mailMap = new HashMap<String, Object>();
			mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));
			mailMap.put("nbioformacceptancecode", inputMap.get("nbioformacceptancecode"));
			final UserInfo mailUserInfo = new UserInfo(userInfo);
			mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
			mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
			emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);

			return getActiveBioFormAcceptance(nbioFormAcceptanceCode, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> updateCompleteBioFormAcceptance(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		jdbcTemplate.execute("lock table lockbioformacceptance " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbioformacceptancedetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final ObjectMapper objMapper = new ObjectMapper();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final int nbioFormAcceptanceCode = (int) inputMap.get("nbioformacceptancecode");

		final int findStatus = findStatusFormAcceptance(nbioFormAcceptanceCode, userInfo);
		if (findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			final String concatSelect = ", bfa.nreceivingtemperaturecode, bfa.jsondata->>'sreceivingofficername' sreceivingofficername,"
					+ " bfa.jsondata->>'scompletionremarks' scompletionremarks ";
			final String concatJoin = "";
			final String strAuditQry = auditParentQuery(nbioFormAcceptanceCode, concatSelect, concatJoin, userInfo);
			List<BioFormAcceptance> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditQry,
					new BioFormAcceptance());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);

			final String strDisposeReturnRecord = "select nbioformacceptancedetailscode, nsamplestatus from "
					+ " bioformacceptancedetails where nbioformacceptancecode=" + nbioFormAcceptanceCode + " and "
					+ " nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsamplestatus in ("
					+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + " , "
					+ Enumeration.TransactionStatus.TOBERETURN.gettransactionstatus() + ")";
			final List<Map<String, Object>> lstDisposeReturnRecord = jdbcTemplate.queryForList(strDisposeReturnRecord);

			String disposeBioFormAcceptanceDetailsCode = lstDisposeReturnRecord.stream()
					.filter(map -> map.get("nsamplestatus")
							.equals(Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus()))
					.map(map -> String.valueOf(map.get("nbioformacceptancedetailscode")))
					.collect(Collectors.joining(","));

			String returnBioFormAcceptanceDetailsCode = lstDisposeReturnRecord.stream()
					.filter(map -> map.get("nsamplestatus")
							.equals(Enumeration.TransactionStatus.TOBERETURN.gettransactionstatus()))
					.map(map -> String.valueOf(map.get("nbioformacceptancedetailscode")))
					.collect(Collectors.joining(","));

			BioFormAcceptance objBioFormAcceptance = objMapper.convertValue(inputMap.get("bioFormAcceptance"),
					BioFormAcceptance.class);

			int formAcceptanceHistoryPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='bioformacceptancehistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			formAcceptanceHistoryPk++;
			int formAcceptanceHistoryDetailsPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='bioformacceptdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);

			String strUpdateFormAcceptance = "update bioformacceptance set nreceivingtemperaturecode="
					+ objBioFormAcceptance.getNreceivingtemperaturecode() + ", " + " nreceivingofficercode="
					+ objBioFormAcceptance.getNreceivingofficercode() + ","
					+ " jsondata=jsondata || '{\"scompletionremarks\": \""
					+ stringUtilityFunction.replaceQuote(objBioFormAcceptance.getScompletionremarks()) + "\","
					+ " \"sreceivingtemperaturename\": \""
					+ stringUtilityFunction.replaceQuote(objBioFormAcceptance.getSreceivingtemperaturename()) + "\", "
					+ " \"sreceivingofficername\": \""
					+ stringUtilityFunction.replaceQuote(objBioFormAcceptance.getSreceivingofficername()) + "\"}', "
					+ " ntransactionstatus=" + Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
					+ ", dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ " ntztransactiondate=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbioformacceptancecode=" + nbioFormAcceptanceCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			strUpdateFormAcceptance += "insert into bioformacceptancehistory (nbioformacceptancehistorycode,"
					+ " nbioformacceptancecode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
					+ " noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode,"
					+ " nstatus) values (" + formAcceptanceHistoryPk + ", " + nbioFormAcceptanceCode + ", "
					+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			strUpdateFormAcceptance += "update seqnobiobankmanagement set nsequenceno=" + formAcceptanceHistoryPk
					+ " where stablename='bioformacceptancehistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			String strMoveToDispose = "";
			String strMoveToDisposeDetails = "";

			if (!disposeBioFormAcceptanceDetailsCode.isEmpty()) {
				strUpdateFormAcceptance += "update bioformacceptancedetails set nsamplestatus="
						+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
						+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " where "
						+ " nbioformacceptancecode=" + nbioFormAcceptanceCode
						+ " and nbioformacceptancedetailscode in (" + disposeBioFormAcceptanceDetailsCode
						+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and " + " nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

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
						+ " select " + intBioMoveToDisposePk + ", sformnumber, ntransfertypecode, nformtypecode, -1, "
						+ Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()
						+ ", noriginsitecode, jsondata->>'sremarks', '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from bioformacceptance where nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
						+ " nbioformacceptancecode=" + nbioFormAcceptanceCode + ";";
				strMoveToDispose += " update seqnobiobankmanagement set nsequenceno=" + intBioMoveToDisposePk
						+ " where stablename='biomovetodispose' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				// modifed by sujatha ATE_274 for added new key's in the jsondata of
				// biomovetodisposaldetails table BGSI-218
				strMoveToDisposeDetails += "insert into biomovetodisposedetails (nbiomovetodisposedetailscode,"
						+ " nbiomovetodisposecode, nbioprojectcode, nbioparentsamplecode,"
						+ " nsamplestoragetransactioncode, svolume, jsondata, ncohortno, nstoragetypecode,"
						+ " nproductcatcode, nproductcode, nsamplecondition, ndiagnostictypecode, "
						+ " ncontainertypecode, nsamplestatus, nreasoncode, dtransactiondate, "
						+ " ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) select "
						+ intBioMoveToDisposeDetailsPk + "+rank()over(order by nbioformacceptancedetailscode), "
						+ intBioMoveToDisposePk
						+ ", nbioprojectcode, nbioparentsamplecode, nsamplestoragetransactioncode, sreceivedvolume,"
						+ " jsonb_build_object('sparentsamplecode', jsondata->>'sparentsamplecode', 'srepositoryid', srepositoryid,"
						+ " 'ssubjectid', jsondata->>'ssubjectid', 'scasetype', jsondata->>'scasetype', 'slocationcode', case when slocationcode is null then '' else slocationcode end,"
						+ " 'sextractedsampleid', jsondata->>'sextractedsampleid', 'sconcentration', jsondata->>'sconcentration', 'sqcplatform', jsondata->>'sqcplatform', 'seluent', jsondata->>'seluent' ,"
						+ " '" + Enumeration.FormCode.BIODIRECTTRANSFER.getFormCode() + "', NULLIF(jsondata->>'"
						+ Enumeration.FormCode.BIODIRECTTRANSFER.getFormCode() + "','') ,'"
						+ Enumeration.FormCode.REQUESTBASEDTRANSFER.getFormCode() + "', NULLIF(jsondata->>'"
						+ Enumeration.FormCode.REQUESTBASEDTRANSFER.getFormCode() + "','') ,'"
						+ Enumeration.FormCode.ECATALOGREQUESTAPPROVAL.getFormCode() + "', NULLIF(jsondata->>'"
						+ Enumeration.FormCode.ECATALOGREQUESTAPPROVAL.getFormCode()
						+ "', ''),'"+Enumeration.FormCode.BGSIRETURN.getFormCode()+"', NULLIF(jsondata->>'"+Enumeration.FormCode.BGSIRETURN.getFormCode()+"',''),'"+Enumeration.FormCode.THIRDPARTYRETURN.getFormCode()+"', NULLIF(jsondata->>'"+Enumeration.FormCode.THIRDPARTYRETURN.getFormCode()+"','')  ), ncohortno, nstoragetypecode,"
						+ " nproductcatcode, nproductcode, nsamplecondition, ndiagnostictypecode, ncontainertypecode, "
						+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ", nreasoncode, " + " '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from bioformacceptancedetails where nsitecode=" + userInfo.getNtranssitecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
						+ " nbioformacceptancecode=" + nbioFormAcceptanceCode
						+ " and nbioformacceptancedetailscode in (" + disposeBioFormAcceptanceDetailsCode + ");";
				strMoveToDisposeDetails += "update seqnobiobankmanagement set nsequenceno=(select "
						+ intBioMoveToDisposeDetailsPk
						+ "+ count(nbioformacceptancedetailscode) from bioformacceptancedetails where nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nbioformacceptancecode="
						+ nbioFormAcceptanceCode + " and nbioformacceptancedetailscode in ("
						+ disposeBioFormAcceptanceDetailsCode + ")) where stablename='biomovetodisposedetails'"
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			}

			if (!returnBioFormAcceptanceDetailsCode.isEmpty()) {
				strUpdateFormAcceptance += "update bioformacceptancedetails set nsamplestatus="
						+ Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
						+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " where "
						+ " nbioformacceptancecode=" + nbioFormAcceptanceCode
						+ " and nbioformacceptancedetailscode in (" + returnBioFormAcceptanceDetailsCode
						+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and " + " nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			}

			final String sbioFormAcceptanceDetailsCode = lstDisposeReturnRecord.stream()
					.map(x -> String.valueOf(x.get("nbioformacceptancedetailscode"))).collect(Collectors.joining(","));

			if (!sbioFormAcceptanceDetailsCode.isEmpty()) {
				strUpdateFormAcceptance += "insert into bioformacceptdetailshistory"
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
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from bioformacceptancedetails where nbioformacceptancedetailscode in ("
						+ sbioFormAcceptanceDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by nbioformacceptancedetailscode;";

				strUpdateFormAcceptance += " update seqnobiobankmanagement set nsequenceno=(" + "select "
						+ formAcceptanceHistoryDetailsPk
						+ " + count(nbioformacceptancedetailscode) from bioformacceptancedetails where"
						+ " nbioformacceptancedetailscode in (" + sbioFormAcceptanceDetailsCode + ") and nsitecode="
						+ userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
						+ " where stablename='bioformacceptdetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			}

			jdbcTemplate.execute(strUpdateFormAcceptance + strMoveToDispose + strMoveToDisposeDetails);

			// ===== COC: START =====

//			if (disposeBioFormAcceptanceDetailsCode != null && !disposeBioFormAcceptanceDetailsCode.isEmpty()) {
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
//				String strChainCustodyDispose = "insert into chaincustody ("
//						+ "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
//						+ "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
//						+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + (chainCustodyPk)
//						+ " + rank() over(order by bfad.nbioformacceptancedetailscode), " + userInfo.getNformcode()
//						+ ", " + " bfad.nbioformacceptancedetailscode, 'nbioformacceptancedetailscode', "
//						+ " 'bioformacceptancedetails', bfa.sformnumber, " + 25 + "  , " + userInfo.getNusercode()
//						+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
//						+ "', " + userInfo.getNtimezonecode() + ", "
//						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " '"
//						+ commonFunction.getMultilingualMessage("IDS_MOVETODISPOSESAMPLES",
//								userInfo.getSlanguagefilename())
//						+ " ' || ' "
//						+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//						+ " [' || bfad.srepositoryid || '] ' || '"
//						+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//						+ " [' || (bfad.jsondata->>'sparentsamplecode') || '] ' || '"
//						+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//						+ " [' || bfa.sformnumber || " + " ']' " + " , " + userInfo.getNtranssitecode() + ", "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " from bioformacceptancedetails bfad"
//						+ " join bioformacceptance bfa on bfa.nbioformacceptancecode = bfad.nbioformacceptancecode"
//						+ " and bfa.nsitecode = " + userInfo.getNtranssitecode() + " and bfa.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bfad.nsitecode = "
//						+ userInfo.getNtranssitecode() + " and bfad.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and bfad.nbioformacceptancedetailscode in (" + disposeBioFormAcceptanceDetailsCode + ")"
//						+ " and bfad.nsamplecondition in (22,34)" + ";";
//
//				String strSeqUpdateDispose = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//						+ " + count(nbioformacceptancedetailscode)" + " from bioformacceptancedetails"
//						+ " where nbioformacceptancedetailscode in (" + disposeBioFormAcceptanceDetailsCode + ")"
//						+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and nsamplecondition in (22,34)" + ") where stablename = 'chaincustody' and nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//				jdbcTemplate.execute(strChainCustodyDispose);
//				jdbcTemplate.execute(strSeqUpdateDispose);
//			}

			// COC for samples moved to RETURN (if any) — only for nsamplecondition 22 or 34
			if (returnBioFormAcceptanceDetailsCode != null && !returnBioFormAcceptanceDetailsCode.isEmpty()) {
				int chainCustodyPk2 = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);

				String strChainCustodyReturn = "insert into chaincustody ("
						+ "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
						+ "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
						+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + (chainCustodyPk2)
						+ " + rank() over(order by bfad.nbioformacceptancedetailscode), " + userInfo.getNformcode()
						+ ", " + " bfad.nbioformacceptancedetailscode, 'nbioformacceptancedetailscode', "
						+ " 'bioformacceptancedetails', bfa.sformnumber, " + 25 + "  , " + userInfo.getNusercode()
						+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " '"
						+ commonFunction.getMultilingualMessage("IDS_MOVETORETURNSAMPLES",
								userInfo.getSlanguagefilename())
						+ " ' || ' "
						+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
						+ " [' || bfad.srepositoryid || '] ' || '"
						+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
						+ " [' || (bfad.jsondata->>'sparentsamplecode') || '] ' || '"
						+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
						+ " [' || bfa.sformnumber || " + " ']' " + " , " + userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from bioformacceptancedetails bfad"
						+ " join bioformacceptance bfa on bfa.nbioformacceptancecode = bfad.nbioformacceptancecode"
						+ " and bfa.nsitecode = " + userInfo.getNtranssitecode() + " and bfa.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bfad.nsitecode = "
						+ userInfo.getNtranssitecode() + " and bfad.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and bfad.nbioformacceptancedetailscode in (" + returnBioFormAcceptanceDetailsCode + ")"
						+ " and bfad.nsamplecondition in (22,34)" + ";";

				String strSeqUpdateReturn = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk2
						+ " + count(nbioformacceptancedetailscode)" + " from bioformacceptancedetails"
						+ " where nbioformacceptancedetailscode in (" + returnBioFormAcceptanceDetailsCode + ")"
						+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " ) where stablename = 'chaincustody' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				jdbcTemplate.execute(strChainCustodyReturn);
				jdbcTemplate.execute(strSeqUpdateReturn);
			}

			// ===== COC: END =====

			List<BioFormAcceptance> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditQry,
					new BioFormAcceptance());
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_COMPLETEFORMACCEPTANCE"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			return getActiveBioFormAcceptance(nbioFormAcceptanceCode, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	public ResponseEntity<Object> validateFormAcceptance(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		jdbcTemplate.execute("lock table lockbioformacceptance " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbioformacceptancedetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final int nbioFormAcceptanceCode = (int) inputMap.get("nbioformacceptancecode");

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final int findStatus = findStatusFormAcceptance(nbioFormAcceptanceCode, userInfo);
		if (findStatus == Enumeration.TransactionStatus.RECEIVED.gettransactionstatus()) {

			final String concatSelect = ", to_char(bfa.dreceiveddate, '" + userInfo.getSsitedate()
					+ "') sreceiveddate ";
			final String strAuditQry = auditParentQuery(nbioFormAcceptanceCode, concatSelect, "", userInfo);
			List<BioFormAcceptance> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditQry,
					new BioFormAcceptance());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);

			int formAcceptanceHistoryPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='bioformacceptancehistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			formAcceptanceHistoryPk++;
			int formAcceptanceHistoryDetailsPk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='bioformacceptdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);

			String strValidateFormAcceptanceQuery = "update bioformacceptance set ntransactionstatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbioformacceptancecode=" + nbioFormAcceptanceCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			strValidateFormAcceptanceQuery += "insert into bioformacceptancehistory (nbioformacceptancehistorycode,"
					+ " nbioformacceptancecode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
					+ " noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode,"
					+ " nstatus) values (" + formAcceptanceHistoryPk + ", " + nbioFormAcceptanceCode + ", "
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			strValidateFormAcceptanceQuery += " update seqnobiobankmanagement set nsequenceno="
					+ formAcceptanceHistoryPk + " where stablename='bioformacceptancehistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			String strValidateFormAcceptanceDetailsQuery = "update bioformacceptancedetails set nsamplestatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbioformacceptancecode=" + nbioFormAcceptanceCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			strValidateFormAcceptanceDetailsQuery += "insert into bioformacceptdetailshistory"
					+ " (nbioformacceptdetailshistorycode, nbioformacceptancedetailscode, nbioformacceptancecode,"
					+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
					+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
					+ "select " + formAcceptanceHistoryDetailsPk
					+ "+rank()over(order by nbioformacceptancedetailscode),"
					+ " nbioformacceptancedetailscode, nbioformacceptancecode, nsamplecondition, nsamplestatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " from bioformacceptancedetails where " + " nbioformacceptancecode=" + nbioFormAcceptanceCode
					+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " order by nbioformacceptancedetailscode;";
			strValidateFormAcceptanceDetailsQuery += " update seqnobiobankmanagement set nsequenceno=(" + "select "
					+ formAcceptanceHistoryDetailsPk
					+ " + count(nbioformacceptancedetailscode) from bioformacceptancedetails where"
					+ " nbioformacceptancecode=" + nbioFormAcceptanceCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " where stablename='bioformacceptdetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(strValidateFormAcceptanceQuery + strValidateFormAcceptanceDetailsQuery);

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
//					+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + (chainCustodyPk)
//					+ " + rank() over(order by bfad.nbioformacceptancedetailscode), " + userInfo.getNformcode() + ", "
//					+ " bfad.nbioformacceptancedetailscode, 'nbioformacceptancedetailscode', "
//					+ " 'bioformacceptancedetails', bfa.sformnumber, "
//					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", " + userInfo.getNusercode()
//					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
//					+ userInfo.getNtimezonecode() + ", "
//					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " '"
//					+ commonFunction.getMultilingualMessage("IDS_VALIDATEFORMACCEPTANCE",
//							userInfo.getSlanguagefilename())
//					+ " ' || ' "
//					+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//					+ " [' || bfad.srepositoryid || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//					+ " [' || bfa.sformnumber || ']', " + userInfo.getNtranssitecode() + ", "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " from bioformacceptancedetails bfad"
//					+ " join bioformacceptance bfa on bfa.nbioformacceptancecode = bfad.nbioformacceptancecode"
//					+ " and bfa.nsitecode = " + userInfo.getNtranssitecode() + " and bfa.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bfad.nsitecode = "
//					+ userInfo.getNtranssitecode() + " and bfad.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and bfad.nbioformacceptancecode = " + nbioFormAcceptanceCode + ";";
//
//			String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//					+ " + count(nbioformacceptancedetailscode)" + " from bioformacceptancedetails"
//					+ " where nbioformacceptancecode = " + nbioFormAcceptanceCode + " and nsitecode = "
//					+ userInfo.getNtranssitecode() + " and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ ") where stablename = 'chaincustody' and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//			jdbcTemplate.execute(strChainCustody);
//			jdbcTemplate.execute(strSeqUpdate);
			// ===== COC: END =====

			List<BioFormAcceptance> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditQry,
					new BioFormAcceptance());
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_VALIDATEFORMACCEPTANCE"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			return getActiveBioFormAcceptance(nbioFormAcceptanceCode, userInfo);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTRECEIVEDRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> onCompleteSlideOut(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final int nbioFormAcceptanceCode = (int) inputMap.get("nbioformacceptancecode");

		final int findStatus = findStatusFormAcceptance(nbioFormAcceptanceCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {
			List<Map<String, Object>> lstChildBioFormAcceptance = getChildInitialGet(nbioFormAcceptanceCode, userInfo);
			if (lstChildBioFormAcceptance != null && lstChildBioFormAcceptance.size() > 0) {
				final String sbioFormAcceptanceDetailsCode = lstChildBioFormAcceptance.stream()
						.map(map -> String.valueOf(map.get("nbioformacceptancedetailscode")))
						.collect(Collectors.joining(","));

				final String concatString = " and nsamplestatus in ("
						+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ")";

				final String validatedFormAcceptanceDetailsCode = findStatusFormAcceptanceDetails(
						nbioFormAcceptanceCode, sbioFormAcceptanceDetailsCode, concatString, userInfo);

				if (validatedFormAcceptanceDetailsCode != null) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_VALIDATEDSAMPLESNOTALLOWEDTOCOMPLETE",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					Map<String, Object> outputMap = new HashMap<>();
					outputMap.putAll((Map<String, Object>) getStorageCondition(userInfo).getBody());
					outputMap.putAll((Map<String, Object>) getUsersBasedOnSite(userInfo).getBody());
					return new ResponseEntity<>(outputMap, HttpStatus.OK);
				}
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

	public int findStatusFormAcceptance(final int nbioFormAcceptanceCode, final UserInfo userInfo) throws Exception {
		final String strStatusFormAcceptance = "select ntransactionstatus from bioformacceptance where nbioformacceptancecode="
				+ nbioFormAcceptanceCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.queryForObject(strStatusFormAcceptance, Integer.class);
	}

	public String findStatusFormAcceptanceDetails(final int nbioFormAcceptanceCode,
			final String nbioFormAcceptanceDetailsCode, final String concatString, final UserInfo userInfo)
			throws Exception {
		final String strStatusFormAcceptance = "select nbioformacceptancedetailscode from bioformacceptancedetails where"
				+ " nbioformacceptancecode=" + nbioFormAcceptanceCode + " and nbioformacceptancedetailscode in ("
				+ nbioFormAcceptanceDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + concatString;
		List<Integer> lstStatusFormAcceptance = jdbcTemplate.queryForList(strStatusFormAcceptance, Integer.class);
		String strFormAcceptanceDetailsCode = null;
		if (lstStatusFormAcceptance.size() > 0) {
			strFormAcceptanceDetailsCode = lstStatusFormAcceptance.stream().map(String::valueOf)
					.collect(Collectors.joining(", "));
		}
		return strFormAcceptanceDetailsCode;
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getStorageFreezerData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();

		final String sbioFormAcceptanceDetailsCode = (String) inputMap.get("nbioFormAcceptanceDetailsCode");
		List<Map<String, Object>> nonAccessibleSamples = checkAccessibleSamples(sbioFormAcceptanceDetailsCode,
				userInfo);

		if (!nonAccessibleSamples.isEmpty()) {
			final String srepositoryId = nonAccessibleSamples.stream().map(x -> (String) x.get("srepositoryid"))
					.collect(Collectors.joining(","));
			String salertMsg = srepositoryId + " " + commonFunction
					.getMultilingualMessage("IDS_SAMPLESARENOTACCESSIBLE", userInfo.getSlanguagefilename());
			outputMap.put("containsNonAccessibleSamples", true);
			outputMap.put("salertMsg", salertMsg);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}

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
			outputMap.put("containsNonAccessibleSamples", false);
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

	public List<Map<String, Object>> checkAccessibleSamples(final String nbioFormAcceptanceDetailsCode,
			final UserInfo userInfo) throws Exception {

		final String strNonAccessibleSample = "select nbioformacceptancedetailscode, srepositoryid from"
				+ " bioformacceptancedetails bfad join biosubjectdetails bsd on bsd.ssubjectid=bfad.ssubjectid"
				+ " and bsd.nsitecode=" + userInfo.getNmastersitecode() + " and bsd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bfad.nsitecode="
				+ userInfo.getNtranssitecode() + " and bfad.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bfad.nbioformacceptancedetailscode in (" + nbioFormAcceptanceDetailsCode
				+ ") and bsd.nissampleaccesable=" + Enumeration.TransactionStatus.NO.gettransactionstatus()
				+ " order by 1 desc";
		return jdbcTemplate.queryForList(strNonAccessibleSample);

	}

	@Override
	public ResponseEntity<Object> moveToReturnDisposeAfterCompleteForm(int nbioFormAcceptanceCode,
			String nbioFormAcceptanceDetailsCode, UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();

		jdbcTemplate.execute(
				"lock table lockbioformacceptancedetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final String sQueryRetrivel = " lock  table locksamplestoragetransaction "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryRetrivel);

		final var findStatusForm = findStatusFormAcceptance(nbioFormAcceptanceCode, userInfo);

		if (findStatusForm == Enumeration.TransactionStatus.COMPLETED.gettransactionstatus()) {

			final var stroredSample = "SELECT STRING_AGG(nbioformacceptancedetailscode::text, ',')  FROM bioformacceptancedetails "
					+ "WHERE nbioformacceptancedetailscode in (" + nbioFormAcceptanceDetailsCode + ")  "
					+ "AND nsamplecondition = " + Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + "  "
					+ "AND nsamplestatus = " + Enumeration.TransactionStatus.STORED.gettransactionstatus() + " "
					+ "AND nsitecode=" + userInfo.getNtranssitecode() + " " + "AND nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  ;";

			final var nBioFormAcceptanceDetailsCodeStored = jdbcTemplate.queryForObject(stroredSample, String.class);

			if (nBioFormAcceptanceDetailsCodeStored != null) {

				final String concatString = " and nsamplestatus in ("
						+ Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus() + ")";

				final String validatedFormAcceptanceDetailsCode = findStatusFormAcceptanceDetails(
						nbioFormAcceptanceCode, nbioFormAcceptanceDetailsCode, concatString, userInfo);

				final String strAuditAfterQry = childAuditQuery(nbioFormAcceptanceCode,
						validatedFormAcceptanceDetailsCode, userInfo);

				String strValidateFormAcceptanceDetailsQuery = "update bioformacceptancedetails set nsamplestatus="
						+ Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
						+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " where nbioformacceptancedetailscode in (" + nbioFormAcceptanceDetailsCode
						+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				// History Insert

				int formAcceptanceHistoryPk = jdbcTemplate
						.queryForObject("select nsequenceno from seqnobiobankmanagement where "
								+ "stablename='bioformacceptancehistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
				formAcceptanceHistoryPk++;

				int formAcceptanceHistoryDetailsPk = jdbcTemplate
						.queryForObject("select nsequenceno from seqnobiobankmanagement where "
								+ "stablename='bioformacceptdetailshistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);

				String strValidateFormAcceptanceQuery = "insert into bioformacceptancehistory (nbioformacceptancehistorycode,"
						+ " nbioformacceptancecode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
						+ " noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode,"
						+ " nstatus) values (" + formAcceptanceHistoryPk + ", " + nbioFormAcceptanceCode + ", "
						+ Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

				strValidateFormAcceptanceQuery += " update seqnobiobankmanagement set nsequenceno="
						+ formAcceptanceHistoryPk + " where stablename='bioformacceptancehistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				strValidateFormAcceptanceDetailsQuery += "insert into bioformacceptdetailshistory"
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
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " from bioformacceptancedetails where  nbioformacceptancecode=" + nbioFormAcceptanceCode
						+ " and nbioformacceptancedetailscode in(" + nBioFormAcceptanceDetailsCodeStored
						+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by nbioformacceptancedetailscode;";

				strValidateFormAcceptanceDetailsQuery += " update seqnobiobankmanagement set nsequenceno=(" + "select "
						+ formAcceptanceHistoryDetailsPk
						+ " + count(nbioformacceptancedetailscode) from bioformacceptancedetails where"
						+ " nbioformacceptancecode=" + nbioFormAcceptanceCode + " and nbioformacceptancedetailscode in("
						+ nBioFormAcceptanceDetailsCodeStored + ") and nsitecode=" + userInfo.getNtranssitecode()
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
						+ " where stablename='bioformacceptdetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

//				int chainCustodyPk = jdbcTemplate.queryForObject(
//						"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//						Integer.class);
//
//				String strChainCustody = "insert into chaincustody ("
//						+ "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
//						+ "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
//						+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + (chainCustodyPk)
//						+ " + rank() over(order by bfad.nbioformacceptancedetailscode), " + userInfo.getNformcode()
//						+ ", " + " bfad.nbioformacceptancedetailscode, 'nbioformacceptancedetailscode', "
//						+ " 'bioformacceptancedetails', bfa.sformnumber, "
//						+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", "
//						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
//						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
//						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " '"
//						+ commonFunction.getMultilingualMessage("IDS_VALIDATEFORMACCEPTANCE",
//								userInfo.getSlanguagefilename())
//						+ " ' || ' "
//						+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//						+ " [' || bfad.srepositoryid || '] ' || '"
//						+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//						+ " [' || bfa.sformnumber || ']', " + userInfo.getNtranssitecode() + ", "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " from bioformacceptancedetails bfad"
//						+ " join bioformacceptance bfa on bfa.nbioformacceptancecode = bfad.nbioformacceptancecode"
//						+ " and bfa.nsitecode = " + userInfo.getNtranssitecode() + " and bfa.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bfad.nsitecode = "
//						+ userInfo.getNtranssitecode() + " and bfad.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and bfad.nbioformacceptancecode = " + nbioFormAcceptanceCode + ";";
//
//				strChainCustody += "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//						+ " + count(nbioformacceptancedetailscode)" + " from bioformacceptancedetails"
//						+ " where nbioformacceptancecode = " + nbioFormAcceptanceCode + " and nsitecode = "
//						+ userInfo.getNtranssitecode() + " and nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ ") where stablename = 'chaincustody' and nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//				jdbcTemplate.execute(
//						strValidateFormAcceptanceQuery + strValidateFormAcceptanceDetailsQuery + strChainCustody);

				jdbcTemplate.execute(
						strValidateFormAcceptanceQuery + strValidateFormAcceptanceDetailsQuery);

				final int intRetrievalPk = jdbcTemplate
						.queryForObject("select nsequenceno from seqnostoragemanagement where "
								+ "stablename='samplestorageretrieval' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);

				final StringBuilder strSampleStorageRetrieval = new StringBuilder()
						.append("INSERT INTO samplestorageretrieval (").append(" nsamplestorageretrievalcode,")
						.append(" nsamplestoragetransactioncode,").append(" nsamplestoragelocationcode,")
						.append(" nsamplestoragelistcode,").append(" nsamplestoragemappingcode,")
						.append(" nprojecttypecode,").append(" nusercode,").append(" nuserrolecode,")
						.append(" nbiosamplereceivingcode,").append(" sposition,").append(" spositionvalue,")
						.append(" jsondata,").append(" ntransactionstatus,").append(" ntransferstatuscode,")
						.append(" ninstrumentcode,").append(" nbioparentsamplecode,").append(" sparentsamplecode,")
						.append(" ncohortno,").append(" nproductcatcode,").append(" nproductcode,").append(" sqty,")
						.append(" slocationcode,").append(" ssubjectid,").append(" scasetype,")
						.append(" ndiagnostictypecode,").append(" ncontainertypecode,").append(" nstoragetypecode,")
						.append(" dtransactiondate,").append(" noffsetdtransactiondate,")
						.append(" ntransdatetimezonecode,").append(" nsitecode,").append(" nstatus)").append(" SELECT ")
						.append(" " + intRetrievalPk + " + RANK() OVER (ORDER BY bfad.nbioformacceptancedetailscode),")
						.append(" sst.nsamplestoragetransactioncode,").append(" sst.nsamplestoragelocationcode,")
						.append(" -1,").append(" sst.nsamplestoragemappingcode,").append(" sst.nprojecttypecode,")
						.append(" " + userInfo.getNusercode() + ",").append(" " + userInfo.getNuserrole() + ",")
						.append(" bfad.nbiosamplereceivingcode,").append(" sst.sposition,")
						.append(" sst.spositionvalue,").append(" sst.jsondata,")
						.append(" " + Enumeration.TransactionStatus.RETRIEVED.gettransactionstatus() + ",")
						.append(" " + Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus() + ",")
						.append(" sst.ninstrumentcode,").append(" bfad.nbioparentsamplecode,")
						.append(" bfad.jsondata->>'sparentsamplecode' AS sparentsamplecode,").append(" bfad.ncohortno,")
						.append(" bfad.nproductcatcode,").append(" bfad.nproductcode,")
						.append(" bfad.jsondata->>'svolume' AS sqty,").append(" bfad.slocationcode,")
						.append(" bfad.jsondata->>'ssubjectid' AS ssubjectid,")
						.append(" bfad.jsondata->>'scasetype' AS scasetype,").append(" bfad.ndiagnostictypecode,")
						.append(" bfad.ncontainertypecode,").append(" bfad.nstoragetypecode,").append(" '")
						.append(dateUtilityFunction.getCurrentDateTime(userInfo)).append("', ")
						.append(dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())).append(", ")
						.append(userInfo.getNtimezonecode()).append(", sst.nsitecode, ")
						.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
						.append(" FROM bioformacceptancedetails bfad").append(" JOIN samplestoragetransaction sst")
						.append("   ON bfad.nsamplestoragetransactioncode = sst.nsamplestoragetransactioncode")
						.append("  AND sst.nsitecode = ").append(userInfo.getNtranssitecode())
						.append("  AND sst.nstatus = ")
						.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
						.append(" WHERE bfad.nsitecode = ").append(userInfo.getNtranssitecode())
						.append(" AND bfad.nstatus = ")
						.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
						.append(" AND bfad.nbioformacceptancedetailscode in ( ").append(nbioFormAcceptanceDetailsCode)
						.append(") AND bfad.nsamplestatus=")
						.append(Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus())
						.append(" ORDER BY bfad.nbioformacceptancedetailscode;");

				strSampleStorageRetrieval.append("INSERT INTO sampleretrievaladditionalinfo (")
						.append(" nsamplestorageretrievalcode,").append(" sextractedsampleid,")
						.append(" sconcentration,").append(" sqcplatform,").append(" seluent,")
						.append(" dmodifieddate,").append(" nsitecode,").append(" nstatus)").append(" SELECT ")
						.append(" " + intRetrievalPk + " + RANK() OVER (ORDER BY bfad.nbioformacceptancedetailscode),")
						.append(" ssa.sextractedsampleid,").append(" ssa.sconcentration,").append(" ssa.sqcplatform,")
						.append(" ssa.seluent,").append(" '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',")
						.append(" ssa.nsitecode,")
						.append(" " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "")
						.append(" FROM samplestorageadditionalinfo ssa").append(" JOIN samplestoragetransaction sst")
						.append("   ON ssa.nsamplestoragetransactioncode = sst.nsamplestoragetransactioncode")
						.append("  AND sst.nsitecode = " + userInfo.getNtranssitecode() + "")
						.append("  AND sst.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "")
						.append(" JOIN bioformacceptancedetails bfad")
						.append("   ON bfad.nsamplestoragetransactioncode = sst.nsamplestoragetransactioncode")
						.append("  AND bfad.nsitecode = " + userInfo.getNtranssitecode() + "")
						.append("  AND bfad.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ "")
						.append(" WHERE bfad.nbioformacceptancedetailscode in (").append(nbioFormAcceptanceDetailsCode)
						.append(") AND bfad.nsamplestatus=")
						.append(Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus())
						.append(" ORDER BY bfad.nbioformacceptancedetailscode;");

				final int seqNoRetrievalCount = jdbcTemplate.queryForObject(
						"SELECT COUNT(bfad.nbioformacceptancedetailscode) FROM bioformacceptancedetails bfad "
								+ "JOIN samplestoragetransaction sst  ON bfad.nsamplestoragetransactioncode = sst.nsamplestoragetransactioncode "
								+ " AND sst.nsitecode = " + userInfo.getNtranssitecode() + " AND sst.nstatus ="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " WHERE bfad.nsitecode = " + userInfo.getNtranssitecode() + "  AND bfad.nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ "AND bfad.nbioformacceptancedetailscode IN (" + nbioFormAcceptanceDetailsCode
								+ ")  AND bfad.nsamplestatus="
								+ Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus() + " ;",
						Integer.class);

				strSampleStorageRetrieval.append("update seqnostoragemanagement set nsequenceno=")
						.append(intRetrievalPk + seqNoRetrievalCount)
						.append(" where stablename='samplestorageretrieval' and nstatus=")
						.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");

				jdbcTemplate.execute(strSampleStorageRetrieval.toString());

				final StringBuilder strDeleteSampleStorageTransactionDelete = new StringBuilder()

						.append("DELETE FROM samplestorageadditionalinfo ")
						.append("WHERE nsamplestoragetransactioncode IN (")
						.append("    SELECT sst.nsamplestoragetransactioncode ")
						.append("    FROM bioformacceptancedetails bfad ")
						.append("    JOIN samplestoragetransaction sst ")
						.append("      ON bfad.nsamplestoragetransactioncode = sst.nsamplestoragetransactioncode ")
						.append("     AND bfad.nsitecode = sst.nsitecode ").append("    WHERE bfad.nsitecode = ")
						.append(userInfo.getNtranssitecode()).append(" ").append("      AND bfad.nstatus = ")
						.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(" ")
						.append("      AND sst.nstatus = ")
						.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(" ")
						.append("      AND bfad.nbioformacceptancedetailscode in (")
						.append(nbioFormAcceptanceDetailsCode).append(") ").append(" AND bfad.nsamplestatus=")
						.append(Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus()).append(");")

						.append("DELETE FROM samplestoragetransaction ")
						.append("WHERE nsamplestoragetransactioncode IN (")
						.append("    SELECT sst.nsamplestoragetransactioncode ")
						.append("    FROM bioformacceptancedetails bfad ")
						.append("    JOIN samplestoragetransaction sst ")
						.append("      ON bfad.nsamplestoragetransactioncode = sst.nsamplestoragetransactioncode ")
						.append("     AND bfad.nsitecode = sst.nsitecode ").append("    WHERE bfad.nsitecode = ")
						.append(userInfo.getNtranssitecode()).append(" ").append("      AND bfad.nstatus = ")
						.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(" ")
						.append("      AND sst.nstatus = ")
						.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(" ")
						.append("      AND bfad.nbioformacceptancedetailscode IN (")
						.append(nbioFormAcceptanceDetailsCode).append(" ").append(") AND bfad.nsamplestatus=")
						.append(Enumeration.TransactionStatus.MOVETORETURN.gettransactionstatus()).append(");");

				jdbcTemplate.execute(strDeleteSampleStorageTransactionDelete.toString());

				// Audit work
				List<BioFormAcceptanceDetails> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
						new BioFormAcceptanceDetails());

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> lstAuditBefore = new ArrayList<>();
				final List<Object> lstAuditAfter = new ArrayList<>();

				List<BioFormAcceptanceDetails> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
						new BioFormAcceptanceDetails());
				lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
				lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
				lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_RETURNUSEDSAMPLES"));

				auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList,
						userInfo);

				List<Map<String, Object>> lstChildBioFormAcceptance = getChildInitialGet(nbioFormAcceptanceCode,
						userInfo);
				outputMap.put("lstChildBioFormAcceptance", lstChildBioFormAcceptance);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDRECORDNOTINSTORE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDCOMPLETERECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

}
