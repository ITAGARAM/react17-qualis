package com.agaramtech.qualis.biobank.service.BioEcatalogueReqApproval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.biobank.model.BioEcatalogueReqApproval;
import com.agaramtech.qualis.biobank.model.BioEcatalogueRequestDetails;
import com.agaramtech.qualis.biobank.model.TransferType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class BioEcatalogueReqApprovalDAOImpl implements BioEcatalogueReqApprovalDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(BioEcatalogueReqApprovalDAOImpl.class);

	private final AuditUtilityFunction auditUtilityFunction;
	private final CommonFunction commonFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcTemplateUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final StringUtilityFunction stringUtilityFunction;
	private final EmailDAOSupport emailDAOSupport;

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

	public List<BioEcatalogueRequestDetails> getChildInitialGet(final int necatrequestreqapprovalcode,
			final UserInfo userInfo) throws Exception {
		LOGGER.info("getChildInitialGet - bioecataloguerequestdetails for approvalCode: {}",
				necatrequestreqapprovalcode);

		final var strQuery = "select bed.necateloguerequestdetailcode, bed.necatrequestreqapprovalcode, bed.nbioprojectcode, "
				+ "bed.nproductcode, bed.saccminvolume, bed.sreqminvolume,bed.sparentsamplecode,bed.nreqnoofsamples,bed.naccnoofsamples, bed.sremarks, bed.dtransactiondate, "
				+ "to_char(bed.dtransactiondate,'" + userInfo.getSsitedate()
				+ "') stransactiondate, bed.ntztransactiondate, bed.noffsetdtransactiondate, bed.nsitecode, bed.nstatus "
				+ "from bioecataloguerequestdetails bed " + "where bed.necatrequestreqapprovalcode="
				+ necatrequestreqapprovalcode + " and bed.nsitecode=" + userInfo.getNtranssitecode()
				+ " and bed.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by bed.necateloguerequestdetailcode desc";

		final List<BioEcatalogueRequestDetails> lst = jdbcTemplate.query(strQuery, new BioEcatalogueRequestDetails());
		return lst;
	}

	
	@Override
	public ResponseEntity<Object> getBioEcatalogueReqApproval(final Map<String, Object> inputMap,
			final int necatrequestreqapprovalcode, final UserInfo userInfo) throws Exception {
		LOGGER.info("getBioEcatalogueReqApproval");
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		if (inputMap.containsKey("necatrequestreqapprovalcode")) {
			outputMap.put("necatrequestreqapprovalcode", inputMap.get("necatrequestreqapprovalcode"));
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
		final var transCode = (short) ntransCode;
		final var selectedFilterStatus = lstFilterStatus.stream().filter(x -> (short) x.get("value") == transCode)
				.collect(Collectors.toList()).get(0);

		outputMap.put("lstFilterStatus", lstFilterStatus);
		outputMap.put("selectedFilterStatus", selectedFilterStatus);
		outputMap.put("realSelectedFilterStatus", selectedFilterStatus);

		final var transferaTypeQuery = "select ntransfertypecode, " + "coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "', " + "jsondata->'sdisplayname'->>'en-US') as stransfertypename "
				+ "from transfertype " + "where ntransfertypecode != -1 " + "order by ndefaultstatus asc";

		final List<TransferType> lstTransferaType = jdbcTemplate.query(transferaTypeQuery, new TransferType());

		final List<Map<String, Object>> lstFilterTransferType = new ArrayList<>();

		lstTransferaType.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getStransfertypename());
			mapStatus.put("value", lst.getNtransfertypecode());
			mapStatus.put("item", lst);
			lstFilterTransferType.add(mapStatus);
		});

		var nTransferType = -1;

		if (!inputMap.containsKey("ntransfertypecode")) {
			nTransferType = lstTransferaType.getFirst().getNtransfertypecode();
		} else {
			nTransferType = (int) inputMap.get("ntransfertypecode");
		}

		final var transferTypeCode = nTransferType;
		final var selectedTransferType = lstFilterTransferType.stream()
				.filter(x -> ((Number) x.get("value")).intValue() == transferTypeCode).collect(Collectors.toList())
				.get(0);

		outputMap.put("lstTransferType", lstFilterTransferType);
		outputMap.put("selectedTransferType", selectedTransferType);
		outputMap.put("realselectedTransferType", selectedTransferType);

		final var strConditionTransCode = ntransCode < -1 ? ""
				: ntransCode == 0 ? " and a.ntransactionstatus in (" + status + ") "
						: " and a.ntransactionstatus in (" + ntransCode + ")";

		final var strConditionTransferTypeCode = nTransferType < 0 ? "" : " and a.ntransfertype=" + nTransferType + " ";

		// Parent Query ---Biobank
		final var strQuery1 = "select necatrequestreqapprovalcode,ntransactionstatus,norginsitecode,sformnumber,drequesteddate, "
				+ "to_char(drequesteddate,'" + userInfo.getSsitedate() + "') strrequesteddate, "
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, "
				+ "COALESCE(TO_CHAR(drequesteddate,'dd/MM/yyyy'), '') as srequesteddate,"
				+ "cm.scolorhexcode, b.ssitename " + "from bioecataloguereqapproval a "
				+ "join transactionstatus ts on ntransactionstatus = ts.ntranscode and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "join formwisestatuscolor fwsc on fwsc.ntranscode = ts.ntranscode and fwsc.nformcode = "
				+ userInfo.getNformcode() + " and fwsc.nsitecode = " + userInfo.getNmastersitecode()
				+ " and fwsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "join colormaster cm on fwsc.ncolorcode = cm.ncolorcode and cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "join site b on a.norginsitecode = b.nsitecode " + "where a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and  norginthirdpartycode not in(-1) "
				+ " and a.nsitecode=" + userInfo.getNtranssitecode() + " and drequesteddate between '" + fromDate
				+ "' and '" + toDate + "' " + strConditionTransCode + " " + strConditionTransferTypeCode
				+ " order by necatrequestreqapprovalcode desc";

		// Parent Query ---External
		final var strQuery2 = "select necatrequestreqapprovalcode,ntransactionstatus,norginsitecode,sformnumber,norginthirdpartycode,c.sthirdpartyname,drequesteddate, "
				+ "to_char(drequesteddate,'" + userInfo.getSsitedate() + "') strrequesteddate, "
				+ "COALESCE(TO_CHAR(drequesteddate,'dd/MM/yyyy'), '') as srequesteddate,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, "
				+ "cm.scolorhexcode, b.ssitename " + "from bioecataloguereqapproval a "
				+ "join transactionstatus ts on ntransactionstatus = ts.ntranscode and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "join formwisestatuscolor fwsc on fwsc.ntranscode = ts.ntranscode and fwsc.nformcode = "
				+ userInfo.getNformcode() + " and fwsc.nsitecode = " + userInfo.getNmastersitecode()
				+ " and fwsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "join colormaster cm on fwsc.ncolorcode = cm.ncolorcode and cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "join site b on a.norginsitecode = b.nsitecode  "
				+ " join thirdparty c on a.norginthirdpartycode =c.nthirdpartycode " + " where a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and  norginthirdpartycode not in(-1) "
				+ " and a.nsitecode=" + userInfo.getNtranssitecode() + " and drequesteddate between '" + fromDate
				+ "' and '" + toDate + "' " + strConditionTransCode + " " + strConditionTransferTypeCode
				+ " order by necatrequestreqapprovalcode desc";

		String strQuerybyTransfertype;

		switch (transferTypeCode) {
		case 1:
			strQuerybyTransfertype = strQuery1;
			break;
		case 2:
			strQuerybyTransfertype = strQuery2;
			break;
		case 3:
			strQuerybyTransfertype = strQuery2;
			break;
		default:
			throw new IllegalArgumentException("Unsupported transferTypeCode: " + transferTypeCode);
		}

		final List<BioEcatalogueReqApproval> lstBioEcatalogueReqApproval = jdbcTemplate.query(strQuerybyTransfertype,
				new BioEcatalogueReqApproval());
		if (!lstBioEcatalogueReqApproval.isEmpty()) {
			outputMap.put("lstBioEcatalogueReqApproval", lstBioEcatalogueReqApproval);
			outputMap.put("selectedBioEcatalogueReqApproval", lstBioEcatalogueReqApproval.get(0));
		} else {
			outputMap.put("lstBioEcatalogueReqApproval", new ArrayList<>());
			outputMap.put("selectedBioEcatalogueReqApproval", new ArrayList<>());
		}
		int selectedparentapprovalCode = 0;
		if (lstBioEcatalogueReqApproval != null && !lstBioEcatalogueReqApproval.isEmpty()) {
			BioEcatalogueReqApproval parentapprovalCode = lstBioEcatalogueReqApproval.get(0);
			selectedparentapprovalCode = parentapprovalCode.getNecatrequestreqapprovalcode();
		}
		// Child Query
//		final var strQuery3 = "select a.necatrequestreqapprovalcode, b.necateloguerequestdetailcode,a.ntransactionstatus,a.ntransfertype, a.sformnumber, a.drequesteddate, "
//				+ "to_char(a.drequesteddate,'" + userInfo.getSsitedate() + "') strrequesteddate, "
//				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
//				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, "
//				+ "b.nproductcode, p.sproductname, bp.sprojectcode, bp.sprojecttitle, "
//				+ " b.saccminvolume, b.sreqminvolume,b.sparentsamplecode,b.nreqnoofsamples,b.naccnoofsamples, b.sremarks, " + "to_char(b.dtransactiondate,'"
//				+ userInfo.getSsitedate()
//				+ "') sdtransactiondate, b.dtransactiondate, b.ntztransactiondate, b.noffsetdtransactiondate "
//				+ "from bioecataloguereqapproval a "
//				+ "join bioecataloguerequestdetails b on a.necatrequestreqapprovalcode = b.necatrequestreqapprovalcode "
//				+ "left join product p on b.nproductcode = p.nproductcode "
//				+ "left join bioproject bp on b.nbioprojectcode = bp.nbioprojectcode "
//				+ "join transactionstatus ts on a.ntransactionstatus = ts.ntranscode and ts.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "where a.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and b.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and a.drequesteddate between '"
//				+ fromDate + "' and '" + toDate + "' " + strConditionTransCode + " " + strConditionTransferTypeCode
//				+ "and a.necatrequestreqapprovalcode  = " + selectedparentapprovalCode + " and a.nsitecode="
//				+ userInfo.getNtranssitecode() + " order by b.necateloguerequestdetailcode desc";

		final String decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
				? "."
				: userInfo.getSdecimaloperator();
		final String opForRegex = escapeForSqlRegexCharClass(decOperator);
		final String opForReplace = stringUtilityFunction.replaceQuote(decOperator);
		final String patternLiteral = "'^[0-9]+([" + opForRegex + "][0-9]+)?$' COLLATE \"default\"";

		final String castExprSt = "REPLACE(TRIM(st.sqty COLLATE \"default\"), '" + opForReplace + "', '.')::numeric";

		// Added COALESE by Gowtham R on nov 18 2025 for jira.id:BGSI-180
		final String castExprB = "REPLACE(TRIM(COALESCE(b.saccminvolume,b.sreqminvolume) COLLATE \"default\"), '" + opForReplace
				+ "', '.')::numeric";

//		final var strQuery3 = "select a.necatrequestreqapprovalcode, b.necateloguerequestdetailcode, a.ntransactionstatus, a.ntransfertype, "
//				+ "a.sformnumber, a.drequesteddate, " + "to_char(a.drequesteddate,'" + userInfo.getSsitedate()
//				+ "') strrequesteddate, " + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
//				+ userInfo.getSlanguagetypecode() + "', "
//				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, "
//				+ "b.nproductcode, p.sproductname, bp.sprojectcode, bp.sprojecttitle, "
//				+ "b.saccminvolume, b.sreqminvolume, b.sparentsamplecode, "
//				+ "b.nreqnoofsamples, b.naccnoofsamples, b.sremarks, " + "to_char(b.dtransactiondate,'"
//				+ userInfo.getSsitedate() + "') sdtransactiondate, "
//				+ "b.dtransactiondate, b.ntztransactiondate, b.noffsetdtransactiondate, "
//				+ "(select count(*) from samplestoragetransaction st " + " where ("
//				+ "       (b.sparentsamplecode is null and (st.sparentsamplecode is null or st.sparentsamplecode = '')) "
//				+ "    or (b.sparentsamplecode is not null and b.sparentsamplecode <> '' and st.sparentsamplecode = b.sparentsamplecode)"
//				+ "       ) " + "   and st.nprojecttypecode = b.nbioprojectcode "
//				+ "   and st.nproductcode = b.nproductcode " + "   and st.nsitecode = " + userInfo.getNtranssitecode()
//				+ "   and (CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + patternLiteral + ") THEN " + castExprSt
//				+ " ELSE NULL END) " + "       >= (CASE WHEN (TRIM(b.sreqminvolume COLLATE \"default\")) ~ ("
//				+ patternLiteral + ") THEN " + castExprB + " ELSE NULL END) " + "   and st.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + "as navailablenoofsample "
//				+ "from bioecataloguereqapproval a "
//				+ "join bioecataloguerequestdetails b on a.necatrequestreqapprovalcode = b.necatrequestreqapprovalcode "
//				+ "left join product p on b.nproductcode = p.nproductcode "
//				+ "left join bioproject bp on b.nbioprojectcode = bp.nbioprojectcode "
//				+ "join transactionstatus ts on a.ntransactionstatus = ts.ntranscode " + "and ts.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "where a.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and b.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and a.drequesteddate between '"
//				+ fromDate + "' and '" + toDate + "' " + strConditionTransCode + " " + strConditionTransferTypeCode
//				+ "and a.necatrequestreqapprovalcode = " + selectedparentapprovalCode + " " + "and a.nsitecode = "
//				+ userInfo.getNtranssitecode() + " " + "order by b.necateloguerequestdetailcode desc";

//		final String strConcatFormType = " join biosubjectdetails bsd on bsd.ssubjectid=st.ssubjectid and bsd.nsitecode="+ userInfo.getNmastersitecode() 
//				+ " and bsd.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
//				+ (transferTypeCode == Enumeration.TransferType.BIOBANK.getntransfertype() ? "nissampleaccesable=" : "nisthirdpartysharable=")
//				+ Enumeration.TransactionStatus.YES.gettransactionstatus()+ " ";
		
		final String strConcatFormType = getConcatJoinBioSubjectDetails(transferTypeCode, userInfo);
		
		// modified by sujatha ATE_274 by joining product category table for selection of productname by concating with its productcatgory name DEMO src
		// Added COALESCE for saccminvolume and naccnoofsamples by Gowtham R on nov 18 2025 for jira.id:BGSI-180
		final var strQuery3 = "select a.necatrequestreqapprovalcode, b.nbioprojectcode, b.necateloguerequestdetailcode, a.ntransactionstatus, a.ntransfertype, "
				+ "a.sformnumber, a.drequesteddate, " + "to_char(a.drequesteddate,'" + userInfo.getSsitedate()
				+ "') strrequesteddate, " + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', "
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, "
				+ "b.nproductcode, concat(p.sproductname, ' (', pc.sproductcatname, ')') sproductname, bp.sprojectcode, bp.sprojecttitle, "
				+ "COALESCE(b.saccminvolume, '-1') AS saccminvolume, b.sreqminvolume, coalesce(b.sparentsamplecode, '-') sparentsamplecode, "
				+ "b.nreqnoofsamples, COALESCE(b.naccnoofsamples,-1) AS naccnoofsamples, b.sremarks, " + "to_char(b.dtransactiondate,'"
				+ userInfo.getSsitedate() + "') sdtransactiondate, "
				+ "b.dtransactiondate, b.ntztransactiondate, b.noffsetdtransactiondate, "
				+ "(select count(*) from samplestoragetransaction st " 
				+ strConcatFormType
				+ " where "
				+ " ( b.sparentsamplecode IS NULL OR b.sparentsamplecode = '' OR st.sparentsamplecode = b.sparentsamplecode ) "
				+ "   and st.nprojecttypecode = b.nbioprojectcode " + "   and st.nproductcode = b.nproductcode "
				+ "   and st.nsitecode = " + userInfo.getNtranssitecode()
				+ "   and (CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + patternLiteral + ") THEN " + castExprSt
				+ " ELSE NULL END) " + "       >= (CASE WHEN (TRIM(b.sreqminvolume COLLATE \"default\")) ~ ("
				+ patternLiteral + ") THEN " + castExprB + " ELSE NULL END) " + "   and st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + "as navailablenoofsample, "
				+ "(select st.spositionvalue from samplestoragetransaction st " 
				+ strConcatFormType
				+ " where "
				+ " ( b.sparentsamplecode IS NULL OR b.sparentsamplecode = '' OR st.sparentsamplecode = b.sparentsamplecode ) "
				+ "   and st.nprojecttypecode = b.nbioprojectcode " + "   and st.nproductcode = b.nproductcode "
				+ "   and st.nsitecode = " + userInfo.getNtranssitecode() + "   and st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by st.dmodifieddate desc limit 1) as spositionvalue, "
				+ "(select st.sqty from samplestoragetransaction st " 
				+ strConcatFormType
				+ " where "
				+ " ( b.sparentsamplecode IS NULL OR b.sparentsamplecode = '' OR st.sparentsamplecode = b.sparentsamplecode ) "
				+ "   and st.nprojecttypecode = b.nbioprojectcode " + "   and st.nproductcode = b.nproductcode "
				+ "   and st.nsitecode = " + userInfo.getNtranssitecode() + "   and st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by st.dmodifieddate desc limit 1) as sqty, a.ntransfertype " + "from bioecataloguereqapproval a "
				+ "join bioecataloguerequestdetails b on a.necatrequestreqapprovalcode = b.necatrequestreqapprovalcode "
				+ "left join product p on b.nproductcode = p.nproductcode "
				+ "left join bioproject bp on b.nbioprojectcode = bp.nbioprojectcode "
				+ "join transactionstatus ts on a.ntransactionstatus = ts.ntranscode " + "and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
				+ "join productcategory pc on pc.nproductcatcode=p.nproductcatcode "
				+ "where a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and b.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and a.drequesteddate between '"
				+ fromDate + "' and '" + toDate + "' " + strConditionTransCode + " " + strConditionTransferTypeCode
				+ "and a.necatrequestreqapprovalcode = " + selectedparentapprovalCode + " " + "and a.nsitecode = "
				+ userInfo.getNtranssitecode() + " " 
				+ " and pc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pc.nsitecode="+userInfo.getNmastersitecode()+ " "
				+ "order by b.necateloguerequestdetailcode asc";

		final List<BioEcatalogueRequestDetails> lstBioEcatalogueRequestDetails = jdbcTemplate.query(strQuery3,
				new BioEcatalogueRequestDetails());

		if (!lstBioEcatalogueRequestDetails.isEmpty()) {
			outputMap.put("lstBioEcatalogueRequestDetails", lstBioEcatalogueRequestDetails);
			outputMap.put("realLstBioEcatalogueRequestDetails", lstBioEcatalogueRequestDetails); // Added by Gowtham R on nov 18 2025 for jira.id:BGSI-180
		} else {
			outputMap.put("lstBioEcatalogueRequestDetails", new ArrayList<>());

		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	private String escapeForSqlRegexCharClass(String decOperator) {
		if (decOperator == null || decOperator.isEmpty()) {
			return "";
		}
		return decOperator.replaceAll("([\\\\.^$|?*+()\\[\\]{}-])", "\\\\$1");
	}
	//changed by sathish on 28-11-2025
	private String getConcatJoinBioSubjectDetails(final int ntransferTypeCode, final UserInfo userInfo) throws Exception {
		final String strConcatFormType = " join biosubjectdetails bsd on bsd.ssubjectid=st.ssubjectid and bsd.nsitecode="+ userInfo.getNmastersitecode() 
			+ " and bsd.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
			+ ((ntransferTypeCode == Enumeration.TransferType.BIOBANK.getntransfertype() || ntransferTypeCode == Enumeration.TransferType.BIOBANKEXTERNAL
			.getntransfertype()) ? "nissampleaccesable=" : "nisthirdpartysharable=")
			+ Enumeration.TransactionStatus.YES.gettransactionstatus()+ " ";
		return strConcatFormType;
	}

	@Override
	public ResponseEntity<Object> getFilteredRequestDetails(int nbioprojectcode, int nproductcode,
			String sparentsamplecode, int ntransfertypecode, int nsitecode, UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<>();

		final String strConcatFormType = getConcatJoinBioSubjectDetails(ntransfertypecode, userInfo);

		String strQuery = "SELECT st.sparentsamplecode, st.spositionvalue, st.sqty, p.sproductname, bp.sprojecttitle "
				+ "FROM samplestoragetransaction st "+ strConcatFormType +" JOIN product p ON st.nproductcode = p.nproductcode "
				+ "JOIN bioproject bp ON st.nprojecttypecode = bp.nbioprojectcode " + "WHERE st.nprojecttypecode = "
				+ nbioprojectcode + " AND st.nproductcode = " + nproductcode + " AND st.nsitecode = "
				+ userInfo.getNtranssitecode() + " AND st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		//&& !sparentsamplecode.contains("-") Added by AT-E234 jana
		if (sparentsamplecode != null && !sparentsamplecode.trim().isEmpty() && !sparentsamplecode.contains("-")) {
			strQuery += " AND st.sparentsamplecode = '" + sparentsamplecode.trim() + "'";
		}

		strQuery += " ORDER BY st.dmodifieddate DESC";

		final List<BioEcatalogueRequestDetails> lst = jdbcTemplate.query(strQuery, new BioEcatalogueRequestDetails());

		if (!lst.isEmpty()) {
			outputMap.put("lstSelectedsamplestoragetransaction", lst);
		} else {
			outputMap.put("lstSelectedsamplestoragetransaction", new ArrayList<>());
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSelectedBioEcatalogueReqApproval(int necatrequestreqapprovalcode,
			int ntransfertype, int ntransactionstatus, UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();

//		final String strQuery = "SELECT a.necateloguerequestdetailcode, a.necatrequestreqapprovalcode, a.nbioprojectcode, "
//				+ "bp.sprojectcode, bp.sprojecttitle, " + "a.nproductcode, p.sproductname, "
//				+ " a.saccminvolume, a.sreqminvolume,a.sparentsamplecode,a.nreqnoofsamples,a.naccnoofsamples, a.sremarks, "
//				+ "a.dtransactiondate, a.ntztransactiondate, a.noffsetdtransactiondate, " + "a.nsitecode, a.nstatus "
//				+ "FROM bioecataloguerequestdetails a " + "LEFT JOIN product p ON a.nproductcode = p.nproductcode "
//				+ "LEFT JOIN bioproject bp ON a.nbioprojectcode = bp.nbioprojectcode "
//				+ "WHERE a.necatrequestreqapprovalcode = " + necatrequestreqapprovalcode + " and a.nsitecode="
//				+ userInfo.getNtranssitecode() + " ORDER BY a.necateloguerequestdetailcode DESC";
		
		final String decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
				? "."
				: userInfo.getSdecimaloperator();
		final String opForRegex = escapeForSqlRegexCharClass(decOperator);
		final String opForReplace = stringUtilityFunction.replaceQuote(decOperator);
		final String patternLiteral = "'^[0-9]+([" + opForRegex + "][0-9]+)?$' COLLATE \"default\"";
		final String castExprSt = " REPLACE(TRIM(st.sqty COLLATE \"default\"), '" + opForReplace + "', '.')::numeric";

		// Added COALESE by Gowtham R on nov 18 2025 for jira.id:BGSI-180
		final String castExprA = " REPLACE(TRIM(COALESCE(a.saccminvolume,a.sreqminvolume) COLLATE \"default\"), '" + opForReplace
				+ "', '.')::numeric";

		final String strConcatFormType = getConcatJoinBioSubjectDetails(ntransfertype, userInfo);
		
		// modified by sujatha ATE_274 by joining product category table for selection of productname by concating with its productcatgory name DEMO src
//		final String strQuery = "SELECT a.necateloguerequestdetailcode, a.necatrequestreqapprovalcode, a.nbioprojectcode, "
//				+ "bp.sprojectcode, bp.sprojecttitle, " + "a.nproductcode, p.sproductname, "
//				+ "a.saccminvolume, a.sreqminvolume, a.sparentsamplecode, "
//				+ "a.nreqnoofsamples, a.naccnoofsamples, a.sremarks, "
//				+ "a.dtransactiondate, a.ntztransactiondate, a.noffsetdtransactiondate, " + "a.nsitecode, a.nstatus, "
//				+ "(SELECT COUNT(*) FROM samplestoragetransaction st " + "  WHERE ("
//				+ "(a.sparentsamplecode IS NULL AND (st.sparentsamplecode IS NULL OR st.sparentsamplecode = '')) "
//				+ "OR (a.sparentsamplecode IS NOT NULL AND a.sparentsamplecode <> '' AND st.sparentsamplecode = a.sparentsamplecode)"
//				+ " ) " + "    AND st.nprojecttypecode = a.nbioprojectcode " + " AND st.nproductcode = a.nproductcode "
//				+ " AND st.nsitecode = " + userInfo.getNtranssitecode() + " AND st.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " AND (CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + patternLiteral + ") THEN " + castExprSt
//				+ " ELSE NULL END) " + "  >= (CASE WHEN (TRIM(a.sreqminvolume COLLATE \"default\")) ~ ("
//				+ patternLiteral + ") THEN " + castExprA + " ELSE NULL END) " + ") AS navailablenoofsample "
//				+ " FROM bioecataloguerequestdetails a " + "LEFT JOIN product p ON a.nproductcode = p.nproductcode "
//				+ " LEFT JOIN bioproject bp ON a.nbioprojectcode = bp.nbioprojectcode "
//				+ " WHERE a.necatrequestreqapprovalcode = " + necatrequestreqapprovalcode + " AND a.nsitecode = "
//				+ userInfo.getNtranssitecode() + " " + "ORDER BY a.necateloguerequestdetailcode DESC";
		final String strQuery = "SELECT a.necateloguerequestdetailcode, a.necatrequestreqapprovalcode, a.nbioprojectcode, "
				+ "bp.sprojectcode, bp.sprojecttitle, " + "a.nproductcode, concat(p.sproductname, ' (', pc.sproductcatname, ')') sproductname, "
				+ "COALESCE(a.saccminvolume, '-1') AS saccminvolume, a.sreqminvolume, "
				+ "coalesce(a.sparentsamplecode, '-') sparentsamplecode, a.nreqnoofsamples, COALESCE(a.naccnoofsamples,-1) AS naccnoofsamples, a.sremarks, "
				+ "a.dtransactiondate, a.ntztransactiondate, a.noffsetdtransactiondate, " + "a.nsitecode, a.nstatus, "
				+ "(SELECT COUNT(*) FROM samplestoragetransaction st " + strConcatFormType + "  WHERE "
				+ " ( a.sparentsamplecode IS NULL OR a.sparentsamplecode = '' OR st.sparentsamplecode = a.sparentsamplecode ) "
				+ "    AND st.nprojecttypecode = a.nbioprojectcode " + "    AND st.nproductcode = a.nproductcode "
				+ "    AND st.nsitecode = " + userInfo.getNtranssitecode() + "    AND st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "    AND (CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + patternLiteral + ") THEN " + castExprSt
				+ " ELSE NULL END) " + "        >= (CASE WHEN (TRIM(a.sreqminvolume COLLATE \"default\")) ~ ("
				+ patternLiteral + ") THEN " + castExprA + " ELSE NULL END) " + ") AS navailablenoofsample, "
				+ "(SELECT st.spositionvalue FROM samplestoragetransaction st " + strConcatFormType + "  WHERE "
				+ " ( a.sparentsamplecode IS NULL OR a.sparentsamplecode = '' OR st.sparentsamplecode = a.sparentsamplecode ) "
				+ "    AND st.nprojecttypecode = a.nbioprojectcode " + "    AND st.nproductcode = a.nproductcode "
				+ "    AND st.nsitecode = " + userInfo.getNtranssitecode() + "    AND st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " ORDER BY st.dmodifieddate DESC LIMIT 1) AS spositionvalue, "
				+ "(SELECT st.sqty FROM samplestoragetransaction st " + strConcatFormType + "  WHERE "
				+ " ( a.sparentsamplecode IS NULL OR a.sparentsamplecode = '' OR st.sparentsamplecode = a.sparentsamplecode ) "
				+ "    AND st.nprojecttypecode = a.nbioprojectcode " + "    AND st.nproductcode = a.nproductcode "
				+ "    AND st.nsitecode = " + userInfo.getNtranssitecode() + "    AND st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " ORDER BY st.dmodifieddate DESC LIMIT 1) AS sqty " + "FROM bioecataloguerequestdetails a "
				+ "LEFT JOIN product p ON a.nproductcode = p.nproductcode "
				+ "LEFT JOIN bioproject bp ON a.nbioprojectcode = bp.nbioprojectcode "
				+ "JOIN productcategory pc on pc.nproductcatcode=p.nproductcatcode "
				+ "WHERE a.necatrequestreqapprovalcode = " + necatrequestreqapprovalcode + " AND a.nsitecode = "
				+ userInfo.getNtranssitecode() + " "
				+ " AND pc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND pc.nsitecode="+userInfo.getNmastersitecode()
				+ " ORDER BY a.necateloguerequestdetailcode ASC";

		final List<BioEcatalogueRequestDetails> lstBioEcatalogueRequestDetails = jdbcTemplate.query(strQuery,
				new BioEcatalogueRequestDetails());
		outputMap.put("lstBioEcatalogueRequestDetails", lstBioEcatalogueRequestDetails);
		outputMap.put("realLstBioEcatalogueRequestDetails", lstBioEcatalogueRequestDetails); // Added by Gowtham R on nov 18 2025 for jira.id:BGSI-180

		String strQuerybyTransfertype;

		strQuerybyTransfertype = (ntransfertype == 1)
				? "select necatrequestreqapprovalcode,ntransactionstatus,norginsitecode,b.nsitecode,sformnumber,drequesteddate, "
						+ "to_char(drequesteddate,'" + userInfo.getSsitedate() + "') strrequesteddate, "
						+ "COALESCE(TO_CHAR(drequesteddate,'dd/MM/yyyy'), '') as srequesteddate,"
						+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
						+ "ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, "
						+ "cm.scolorhexcode, b.ssitename,c.ssitename as sselectedsitename  "
						+ "from bioecataloguereqapproval a "
						+ "join transactionstatus ts on ntransactionstatus = ts.ntranscode and ts.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "join formwisestatuscolor fwsc on fwsc.ntranscode = ts.ntranscode and fwsc.nformcode = "
						+ userInfo.getNformcode() + " and fwsc.nsitecode = " + userInfo.getNmastersitecode()
						+ " and fwsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "join colormaster cm on fwsc.ncolorcode = cm.ncolorcode and cm.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "join site b on a.norginsitecode = b.nsitecode " + " join site c on c.nsitecode = "
						+ userInfo.getNsitecode() + " " + " where a.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransfertype= "
						+ ntransfertype + " and necatrequestreqapprovalcode = " + necatrequestreqapprovalcode
						+ " and a.nsitecode=" + userInfo.getNtranssitecode()
						+ " order by necatrequestreqapprovalcode desc"
				: "select necatrequestreqapprovalcode,sformnumber,ntransactionstatus,norginsitecode,b.nsitecode,norginthirdpartycode,c.sthirdpartyname,drequesteddate, "
						+ "to_char(drequesteddate,'" + userInfo.getSsitedate() + "') strrequesteddate, "
						+ "COALESCE(TO_CHAR(drequesteddate,'dd/MM/yyyy'), '') as srequesteddate,"
						+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
						+ "ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, "
						+ "cm.scolorhexcode, b.ssitename , d.ssitename as sselectedsitename   "
						+ "from bioecataloguereqapproval a "
						+ "join transactionstatus ts on ntransactionstatus = ts.ntranscode and ts.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "join formwisestatuscolor fwsc on fwsc.ntranscode = ts.ntranscode and fwsc.nformcode = "
						+ userInfo.getNformcode() + " and fwsc.nsitecode = " + userInfo.getNmastersitecode()
						+ " and fwsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "join colormaster cm on fwsc.ncolorcode = cm.ncolorcode and cm.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "join site b on a.norginsitecode = b.nsitecode  "
						+ " join thirdparty c on a.norginthirdpartycode =c.nthirdpartycode "
						+ " join site d on d.nsitecode = " + userInfo.getNsitecode() + " where a.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransfertype= "
						+ ntransfertype + " and necatrequestreqapprovalcode = " + necatrequestreqapprovalcode
						+ " and a.nsitecode=" + userInfo.getNtranssitecode()

						+ " order by necatrequestreqapprovalcode desc";

		final BioEcatalogueReqApproval objBioEcatalogueReqApproval = (BioEcatalogueReqApproval) jdbcTemplateUtilityFunction
				.queryForObject(strQuerybyTransfertype, BioEcatalogueReqApproval.class, jdbcTemplate);

		if (objBioEcatalogueReqApproval != null) {
			outputMap.put("selectedBioEcatalogueReqApproval", objBioEcatalogueReqApproval);

		} else {
			outputMap.put("selectedBioEcatalogueReqApproval", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public int findStatusforRequest(final int necatrequestreqapprovalcode, final UserInfo userInfo) throws Exception {
		final String strStatusCheck = "select ntransactionstatus from bioecataloguereqapproval where necatrequestreqapprovalcode="
				+ necatrequestreqapprovalcode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.queryForObject(strStatusCheck, Integer.class);

	}

	@Override
	public ResponseEntity<Object> updatedUserAcceptedVolume(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
	    Object detailsObj = inputMap.get("bioecataloguerequestdetails");
	    if (detailsObj == null || !(detailsObj instanceof List) || ((List<?>) detailsObj).isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_NOSAMPLESAVAILABLE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
	    }	
		
		int necatrequestreqapprovalcode = (int) inputMap.get("necatrequestreqapprovalcode");
		int ntransfertype = (int) inputMap.get("ntransfertype");
		int ntransactionstatus = (int) inputMap.get("ntransactionstatus");
		String sformnumber = (String) inputMap.get("sformnumber");
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();

		// Validate
		final int ValidateAccepted = findStatusforRequest(necatrequestreqapprovalcode, userInfo);
		if (ValidateAccepted == Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus()
				|| ValidateAccepted == Enumeration.TransactionStatus.REJECTED.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTREQUESTEDRECORDS",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

		List<Map<String, Object>> detailsList = (List<Map<String, Object>>) inputMap.get("bioecataloguerequestdetails");
		for (Map<String, Object> details : detailsList) {
		    int necateloguerequestdetailcode1 = (int) details.get("necateloguerequestdetailcode");
		    String naccnoofsamples = String.valueOf(details.get("naccnoofsamples"));
		    String saccminvolume = String.valueOf(details.get("saccminvolume"));

		    String strUpdateQry1 = "update bioecataloguerequestdetails set dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', naccnoofsamples=" + naccnoofsamples
		            + ", saccminvolume='" + stringUtilityFunction.replaceQuote(saccminvolume) + "'"
					+ " where necateloguerequestdetailcode=" + necateloguerequestdetailcode1 + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and necatrequestreqapprovalcode=" + necatrequestreqapprovalcode
		            + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		    jdbcTemplate.execute(strUpdateQry1);
		}

		BioEcatalogueReqApproval objBioBGSiECatalogueDetails = (BioEcatalogueReqApproval) ((Map<?, ?>) getSelectedBioEcatalogueReqApproval(
				necatrequestreqapprovalcode, ntransfertype, ntransactionstatus, userInfo).getBody())
				.get("selectedBioEcatalogueReqApproval");

		listBeforeUpdate.add(objBioBGSiECatalogueDetails);

		if (necatrequestreqapprovalcode != 0) {
			int nbioecataloguereqapprovalhistorycodePk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='bioecataloguereqapprovalhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			nbioecataloguereqapprovalhistorycodePk++;

			String strBioEcatalogueReqApprovalHistory = "INSERT INTO bioecataloguereqapprovalhistory ("
					+ "nbioecataloguereqapprovalhistorycode, necatrequestreqapprovalcode, ntransactionstatus, "
					+ "dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode, nuserrolecode, "
					+ "ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) " + "VALUES ("
					+ nbioecataloguereqapprovalhistorycodePk + ", " + necatrequestreqapprovalcode + ", "
					+ Enumeration.TransactionStatus.AMENDED.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNsitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			jdbcTemplate.execute(strBioEcatalogueReqApprovalHistory);

			String updateSeqNo = " update seqnobiobankmanagement set nsequenceno="
					+ nbioecataloguereqapprovalhistorycodePk
					+ " where stablename='bioecataloguereqapprovalhistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(updateSeqNo);

			String strUpdateQry2 = "update bioecataloguereqapproval set dtransactiondate= '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus = '"
					+ Enumeration.TransactionStatus.AMENDED.gettransactionstatus() + "'"
					+ "where necatrequestreqapprovalcode=" + necatrequestreqapprovalcode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(strUpdateQry2);

//			if (ntransfertype == 1) {
//
//				String strSelectQry = "select nbgsiecatrequestcode from biobgsiecatalogue " + "where sformnumber='"
//						+ stringUtilityFunction.replaceQuote(sformnumber) + "'" + " and nsitecode=" + norginsitecode
//						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//				Integer nbgsiecatrequestcode = 0;
//				try {
//					nbgsiecatrequestcode = jdbcTemplate.queryForObject(strSelectQry, Integer.class);
//				} catch (EmptyResultDataAccessException e) {
//					nbgsiecatrequestcode = null;
//				}
//				if (nbgsiecatrequestcode != null && nbgsiecatrequestcode > 0) {
//
//				String strUpdateQry3 = "update biobgsiecatalogue set dtransactiondate= '"
//						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus = '"
//						+ Enumeration.TransactionStatus.AMENDED.gettransactionstatus() + "'"
//						+ " where nbgsiecatrequestcode=" + nbgsiecatrequestcode + " and nsitecode=" + norginsitecode
//						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//
//				jdbcTemplate.execute(strUpdateQry3);
//
//				int seqNoBGSiECatalogueHistory = jdbcTemplate.queryForObject(
//						"select nsequenceno from seqnobiobankmanagement where stablename in (N'biobgsiecataloguehistory') and nstatus="
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//						Integer.class);
//				seqNoBGSiECatalogueHistory = seqNoBGSiECatalogueHistory + 1;
//
//				String strBioBgsieCatalogueHistory = "INSERT INTO biobgsiecataloguehistory ("
//						+ "nbiobgsiecataloguehistorycode, nbgsiecatrequestcode, nusercode, nuserrolecode, "
//						+ "ndeputyusercode, ndeputyuserrolecode, ntransactionstatus, scomments, "
//						+ "dtransactiondate, ntztransactiondate, noffsettransactiondate, nsitecode, nstatus) VALUES ("
//						+ seqNoBGSiECatalogueHistory + ", " + nbgsiecatrequestcode + ", " + userInfo.getNusercode()
//						+ ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", "
//						+ userInfo.getNdeputyuserrole() + ", " + userInfo.getNdeputyuserrole() + ", "
//						+ Enumeration.TransactionStatus.AMENDED.gettransactionstatus() + ", '"
//						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
//						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
//						+ userInfo.getNsitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ ");";
//				jdbcTemplate.execute(strBioBgsieCatalogueHistory);
//
//				String strInsert = "update seqnobiobankmanagement set nsequenceno=" + seqNoBGSiECatalogueHistory
//						+ " where" + " stablename='biobgsiecataloguehistory' and nstatus="
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//				jdbcTemplate.execute(strInsert);
//				}
				
//				biothirdpartyecataloguerequest,biothirdpartyecataloguereqdetails
//			    String strSelectQry12 = "select nthirdpartyecatrequestcode from biothirdpartyecataloguerequest "
//			            + "where sformnumber='" + stringUtilityFunction.replaceQuote(sformnumber) + "'"
//			            + " and nsitecode=" + norginsitecode
//			            + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//
//			    Integer nthirdpartyecatrequestcode = 0;
//			    try {
//			        nthirdpartyecatrequestcode = jdbcTemplate.queryForObject(strSelectQry12, Integer.class);
//			    } catch (EmptyResultDataAccessException e) {
//			        nthirdpartyecatrequestcode = null;
//			    }
//
//			    if (nthirdpartyecatrequestcode != null && nthirdpartyecatrequestcode > 0) {
//
//			        @SuppressWarnings("unchecked")
//			        List<Map<String, Object>> biothirdpartydetails =
//			                (List<Map<String, Object>>) inputMap.get("bioecataloguerequestdetails");
//
//			        for (Map<String, Object> details : biothirdpartydetails) {
//
//			            int naccnoofsample   = Integer.parseInt(String.valueOf(details.get("naccnoofsamples")));
//			            int nbioprojectcode  = Integer.parseInt(String.valueOf(details.get("nbioprojectcode")));
//			            int nproductcode     = Integer.parseInt(String.valueOf(details.get("nproductcode")));
//			            String saccminvolume = String.valueOf(details.get("saccminvolume"));
//
//			            Object sparentObj = details.get("sparentsamplecode");
//			            String sparentsamplecode = (sparentObj != null) ? String.valueOf(sparentObj).trim() : null;
//
//			            String strWhereSampleCode;
//			            if (sparentsamplecode != null && !sparentsamplecode.isEmpty()) {
//			                strWhereSampleCode = " and sparentsamplecode='"
//			                        + stringUtilityFunction.replaceQuote(sparentsamplecode) + "'";
//			            } else {
//			                strWhereSampleCode = "";
//			            }
//
//			            String strUpdateQry1 = "update biothirdpartyecataloguereqdetails set dtransactiondate='"
//			                    + dateUtilityFunction.getCurrentDateTime(userInfo) + "', saccminvolume='"
//			                    + stringUtilityFunction.replaceQuote(saccminvolume) + "', naccnoofsamples="
//			                    + naccnoofsample
//			                    + " where nbioprojectcode=" + nbioprojectcode
//			                    + " and nproductcode=" + nproductcode
//			                    + strWhereSampleCode
//			                    + " and nsitecode=" + userInfo.getNtranssitecode()
//			                    + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//
//			            jdbcTemplate.execute(strUpdateQry1);
//			        }
//
//			        String strUpdateQry3 = "update biothirdpartyecataloguerequest set dtransactiondate='"
//			                + dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus='"
//			                + Enumeration.TransactionStatus.AMENDED.gettransactionstatus() + "'"
//			                + " where nthirdpartyecatrequestcode=" + nthirdpartyecatrequestcode
//			                + " and nsitecode=" + norginsitecode
//			                + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//
//			        jdbcTemplate.execute(strUpdateQry3);
//			    }

//			}
			
			// === COC: START (after updates / history) ===
//			List<Integer> detailIds = new ArrayList<>();
//			for (Map<String, Object> details : detailsList) {
//				detailIds.add((int) details.get("necateloguerequestdetailcode"));
//			}
//
//			if (!detailIds.isEmpty()) {
//				String detailIdCsv = detailIds.stream().map(String::valueOf).collect(Collectors.joining(","));
//
//				int chainCustodyPk = jdbcTemplate.queryForObject(
//						"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//						Integer.class);
//
//				String strChainCustody = "insert into chaincustody ("
//						+ " nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno, "
//						+ " ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate, "
//						+ " noffsetdtransactiondate, sremarks, nsitecode, nstatus) " + " select " + chainCustodyPk
//						+ " + rank() over(order by berdd.necateloguerequestdetailcode), " + userInfo.getNformcode()
//						+ ", " + " berdd.necateloguerequestdetailcode, 'necateloguerequestdetailcode', "
//						+ " 'bioecataloguerequestdetails', COALESCE('" + stringUtilityFunction.replaceQuote(sformnumber)
//						+ "', ''), " + Enumeration.TransactionStatus.AMENDED.gettransactionstatus() + ", "
//						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
//						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
//						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " (" + " '"
//						+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//						+ " [' || COALESCE(berdd.sparentsamplecode,'') || '] ' || " + " '"
//						+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//						+ " [' || COALESCE('" + stringUtilityFunction.replaceQuote(sformnumber) + "','') || ']') "
//						+ ", " + userInfo.getNtranssitecode() + ", "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//						+ " from bioecataloguerequestdetails berdd " + " where berdd.necateloguerequestdetailcode in ("
//						+ detailIdCsv + ") " + "   and berdd.necatrequestreqapprovalcode = "
//						+ necatrequestreqapprovalcode + " " + "   and berdd.nsitecode = " + userInfo.getNtranssitecode()
//						+ " " + "   and berdd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ ";";
//
//				String strSeqUpdate = "update seqnoregistration set nsequenceno = nsequenceno + " + detailIds.size()
//						+ " where stablename='chaincustody' and nstatus="
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//				jdbcTemplate.execute(strChainCustody);
//				jdbcTemplate.execute(strSeqUpdate);
//
//			}
			// === COC: END ===

			BioEcatalogueReqApproval objBioBGSiECatalogueDetails1 = (BioEcatalogueReqApproval) ((Map<?, ?>) getSelectedBioEcatalogueReqApproval(
					necatrequestreqapprovalcode, ntransfertype, ntransactionstatus, userInfo).getBody())
					.get("selectedBioEcatalogueReqApproval");

			listAfterUpdate.add(objBioBGSiECatalogueDetails1);
			multilingualIDList.add("IDS_AMENDED");
			auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
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
				mailMap.put("necatrequestreqapprovalcode", necatrequestreqapprovalcode);

				String receiverquery = "Select norginsitecode from bioecataloguereqapproval where necatrequestreqapprovalcode= "
						+ necatrequestreqapprovalcode + " and nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode = " + userInfo.getNtranssitecode();
				Integer orginsitecode = jdbcTemplate.queryForObject(receiverquery, Integer.class);

				mailMap.put("norginsitecode", orginsitecode);

				String query = "SELECT sformnumber FROM bioecataloguereqapproval where necatrequestreqapprovalcode="
						+ necatrequestreqapprovalcode + " and nstatus ="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				String referenceId = jdbcTemplate.queryForObject(query, String.class);
				mailMap.put("ssystemid", referenceId);
				final UserInfo mailUserInfo = new UserInfo(userInfo);
				mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
				mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
				emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
			}
			
			
		}
		return getSelectedBioEcatalogueReqApproval(necatrequestreqapprovalcode, ntransfertype, ntransactionstatus,
				userInfo);
	}

	@Override
	public ResponseEntity<Object> updateRejectedRecord(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
	    Object detailsObj = inputMap.get("bioecataloguerequestdetails");
	    if (detailsObj == null || !(detailsObj instanceof List) || ((List<?>) detailsObj).isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_NOSAMPLESAVAILABLE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
	    }
		int necatrequestreqapprovalcode = (int) inputMap.get("necatrequestreqapprovalcode");
		int ntransfertype = (int) inputMap.get("ntransfertype");
		int ntransactionstatus = (int) inputMap.get("ntransactionstatus");
		int norginsitecode = (int) inputMap.get("norginsitecode");
		String sformnumber = (String) inputMap.get("sformnumber");
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();

		// Validate
		final int ValidateAccepted = findStatusforRequest(necatrequestreqapprovalcode, userInfo);
		if (ValidateAccepted == Enumeration.TransactionStatus.REJECTED.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTREQUESTEDRECORDS",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

		BioEcatalogueReqApproval objBioBGSiECatalogueDetails = (BioEcatalogueReqApproval) ((Map<?, ?>) getSelectedBioEcatalogueReqApproval(
				necatrequestreqapprovalcode, ntransfertype, ntransactionstatus, userInfo).getBody())
				.get("selectedBioEcatalogueReqApproval");

		listBeforeUpdate.add(objBioBGSiECatalogueDetails);

		if (necatrequestreqapprovalcode != 0) {
			int nbioecataloguereqapprovalhistorycodePk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='bioecataloguereqapprovalhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			nbioecataloguereqapprovalhistorycodePk++;

			String strBioEcatalogueReqApprovalHistory = "insert into bioecataloguereqapprovalhistory ("
					+ "nbioecataloguereqapprovalhistorycode, " + "necatrequestreqapprovalcode, "
					+ "ntransactionstatus, " + "dtransactiondate, " + "ntztransactiondate, "
					+ "noffsetdtransactiondate, " + "nusercode, " + "nuserrolecode, " + "ndeputyusercode, "
					+ "ndeputyuserrolecode, " + "nsitecode, " + "nstatus" + ") values ("
					+ nbioecataloguereqapprovalhistorycodePk + ", " + necatrequestreqapprovalcode + ", "
					+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNsitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			jdbcTemplate.execute(strBioEcatalogueReqApprovalHistory);

			String updateSeqNo = " update seqnobiobankmanagement set nsequenceno="
					+ nbioecataloguereqapprovalhistorycodePk
					+ " where stablename='bioecataloguereqapprovalhistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(updateSeqNo);

			String strUpdateQry2 = "update bioecataloguereqapproval set dtransactiondate= '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus = '"
					+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + "'"
					+ "where necatrequestreqapprovalcode=" + necatrequestreqapprovalcode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(strUpdateQry2);

			if (ntransfertype == Enumeration.TransferType.BIOBANK.getntransfertype()) {

				String strSelectQry = "select nbgsiecatrequestcode from biobgsiecatalogue where sformnumber='"
						+ stringUtilityFunction.replaceQuote(sformnumber) + "' and nsitecode=" + norginsitecode
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final Integer nbgsiecatrequestcode = (Integer) jdbcTemplateUtilityFunction.queryForObject(strSelectQry, Integer.class, jdbcTemplate);
				
				if (nbgsiecatrequestcode != null && nbgsiecatrequestcode > 0) {
					
					String sQuery = " lock  table lockbiobgsiecatalogue " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery);

					String strUpdateQry3 = "update biobgsiecatalogue set dtransactiondate= '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus = '"
							+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + "'"
							+ " where nbgsiecatrequestcode=" + nbgsiecatrequestcode + " and nsitecode=" + norginsitecode
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					jdbcTemplate.execute(strUpdateQry3);

					int seqNoBGSiECatalogueHistory = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnobiobankmanagement where stablename in (N'biobgsiecataloguehistory') and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);
					seqNoBGSiECatalogueHistory = seqNoBGSiECatalogueHistory + 1;

					String strBioBgsieCatalogueHistory = "INSERT INTO biobgsiecataloguehistory ("
							+ "nbiobgsiecataloguehistorycode, nbgsiecatrequestcode, nusercode, nuserrolecode, "
							+ "ndeputyusercode, ndeputyuserrolecode, ntransactionstatus, scomments, "
							+ "dtransactiondate, ntztransactiondate, noffsettransactiondate, nsitecode, nstatus) VALUES ("
							+ seqNoBGSiECatalogueHistory + ", " + nbgsiecatrequestcode + ", " + userInfo.getNusercode()
							+ ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", "
							+ userInfo.getNdeputyuserrole() + ", " + userInfo.getNdeputyuserrole() + ", "
							+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNsitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
					jdbcTemplate.execute(strBioBgsieCatalogueHistory);

					String strInsert = "update seqnobiobankmanagement set nsequenceno=" + seqNoBGSiECatalogueHistory
							+ " where" + " stablename='biobgsiecataloguehistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					jdbcTemplate.execute(strInsert);
				}
			}
			
			if (ntransfertype == Enumeration.TransferType.BIOBANKEXTERNAL.getntransfertype()) {

				String strSelectQry = "select nbiobankecatreqexternalcode from biobankecataloguerequestexternal where sformnumber='"
						+ stringUtilityFunction.replaceQuote(sformnumber) + "' and nsitecode=" + norginsitecode
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final Integer nbiobankecatreqexternalcode = (Integer) jdbcTemplateUtilityFunction.queryForObject(strSelectQry, Integer.class, jdbcTemplate);
				
				if (nbiobankecatreqexternalcode != null && nbiobankecatreqexternalcode > 0) {
					
					final String sQuery = " lock  table lockbiobankecataloguerequestexternal "
							+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery);

					String strUpdateQry3 = "update biobankecataloguerequestexternal set dtransactiondate= '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus = '"
							+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + "'"
							+ " where nbiobankecatreqexternalcode=" + nbiobankecatreqexternalcode + " and nsitecode=" + norginsitecode
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					jdbcTemplate.execute(strUpdateQry3);

					int seqNoExternalHistory = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnobiobankmanagement where stablename in ('biobankecataloguerequestexternalhistory') and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);
					seqNoExternalHistory = seqNoExternalHistory + 1;
					
					String strExternalHistoryInsert = "INSERT INTO biobankecataloguerequestexternalhistory ("
							+ "nbiobankecatreqexternalhistorycode, nbiobankecatreqexternalcode, "
							+ "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, "
							+ "ntransactionstatus, scomments, dtransactiondate, ntztransactiondate, noffsettransactiondate, "
							+ "nsitecode, nstatus) VALUES (" + seqNoExternalHistory + ", " + nbiobankecatreqexternalcode
							+ ", " + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
							+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "','"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNsitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

					jdbcTemplate.execute(strExternalHistoryInsert);

					String strInsert = "update seqnobiobankmanagement set nsequenceno=" + seqNoExternalHistory
							+ " where stablename='biobankecataloguerequestexternalhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					jdbcTemplate.execute(strInsert);
				}
			}
			
			if (ntransfertype == Enumeration.TransferType.THIRDPARTY.getntransfertype()) {

				String strSelectQry = "select nthirdpartyecatrequestcode from biothirdpartyecataloguerequest where sformnumber='"
						+ stringUtilityFunction.replaceQuote(sformnumber) + "' and nsitecode=" + norginsitecode
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final Integer nthirdpartyecatrequestcode = (Integer) jdbcTemplateUtilityFunction.queryForObject(strSelectQry, Integer.class, jdbcTemplate);
				
				if (nthirdpartyecatrequestcode != null && nthirdpartyecatrequestcode > 0) {
					
					final String sQuery = " lock  table lockbiothirdpartyecatalogue "
							+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery);

					String strUpdateQry3 = "update biothirdpartyecataloguerequest set dtransactiondate= '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus = '"
							+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + "'"
							+ " where nthirdpartyecatrequestcode=" + nthirdpartyecatrequestcode + " and nsitecode=" + norginsitecode
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					jdbcTemplate.execute(strUpdateQry3);

					int seqNoTPHistory = jdbcTemplate.queryForObject("select nsequenceno from seqnobiobankmanagement "
							+ "where stablename in ('biothirdpartyecatreqhistory') " + "and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
					seqNoTPHistory = seqNoTPHistory + 1;

					String strTPHistoryInsert = "insert into biothirdpartyecatreqhistory ("
							+ "nbiothirdpartyecatreqhistorycode, nthirdpartyecatrequestcode, "
							+ "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, "
							+ "ntransactionstatus, scomments, dtransactiondate, ntztransactiondate, noffsettransactiondate, "
							+ "nsitecode, nstatus) values (" + seqNoTPHistory + ", " + nthirdpartyecatrequestcode + ", "
							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ norginsitecode + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ ");";

					jdbcTemplate.execute(strTPHistoryInsert);

					String strInsert = "update seqnobiobankmanagement set nsequenceno=" + seqNoTPHistory
							+ " where stablename='biothirdpartyecatreqhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					jdbcTemplate.execute(strInsert);
				}
			}
			
			// ===== COC: START =====
//			int chainCustodyPk = jdbcTemplate.queryForObject(
//					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//					Integer.class);
//
//			String strChainCustody = "insert into chaincustody("
//					+ "nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,sitemno,"
//					+ "ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,"
//					+ "noffsetdtransactiondate,sremarks,nsitecode,nstatus) select " + chainCustodyPk
//					+ " + rank() over(order by bdrd.necateloguerequestdetailcode)," + userInfo.getNformcode() + ","
//					+ "bdrd.necateloguerequestdetailcode,'nbioecataloguerequestdetailscode',"
//					+ "'bioecataloguerequestdetails',bdr.sformnumber,"
//					+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + "," + userInfo.getNusercode()
//					+ "," + userInfo.getNuserrole() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
//					+ userInfo.getNtimezonecode() + ","
//					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ",'"
//					+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//					+ " [' || COALESCE(bdrd.sparentsamplecode,'') || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//					+ " [' || COALESCE(bdr.sformnumber,'') || ']'," + userInfo.getNtranssitecode() + ","
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " from bioecataloguerequestdetails bdrd "
//					+ "join bioecataloguereqapproval bdr on bdr.necatrequestreqapprovalcode=bdrd.necatrequestreqapprovalcode "
//					+ "and bdr.nsitecode=" + userInfo.getNtranssitecode() + " and bdr.nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " where bdrd.necatrequestreqapprovalcode=" + necatrequestreqapprovalcode + " and bdrd.nsitecode="
//					+ userInfo.getNtranssitecode() + " and bdrd.nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//			String strSeqUpdate = " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
//					+ " + count(necateloguerequestdetailcode) from"
//					+ " bioecataloguerequestdetails where necatrequestreqapprovalcode = " + necatrequestreqapprovalcode
//					+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
//			        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ ") where stablename='chaincustody' and nstatus="
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//			jdbcTemplate.execute(strChainCustody);
//			jdbcTemplate.execute(strSeqUpdate);
			// ===== COC: END =====

			BioEcatalogueReqApproval objBioBGSiECatalogueDetails1 = (BioEcatalogueReqApproval) ((Map<?, ?>) getSelectedBioEcatalogueReqApproval(
					necatrequestreqapprovalcode, ntransfertype, ntransactionstatus, userInfo).getBody())
					.get("selectedBioEcatalogueReqApproval");

			listAfterUpdate.add(objBioBGSiECatalogueDetails1);
			multilingualIDList.add("IDS_REJECTED");
			auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
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
				mailMap.put("necatrequestreqapprovalcode", necatrequestreqapprovalcode);

				String receiverquery = "Select norginsitecode from bioecataloguereqapproval where necatrequestreqapprovalcode= "
						+ necatrequestreqapprovalcode + " and nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode = " + userInfo.getNtranssitecode();
				Integer orginsitecode = jdbcTemplate.queryForObject(receiverquery, Integer.class);

				mailMap.put("norginsitecode", orginsitecode);

				String query = "SELECT sformnumber FROM bioecataloguereqapproval where necatrequestreqapprovalcode="
						+ necatrequestreqapprovalcode + " and nstatus ="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				String referenceId = jdbcTemplate.queryForObject(query, String.class);
				mailMap.put("ssystemid", referenceId);
				final UserInfo mailUserInfo = new UserInfo(userInfo);
				mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
				mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
				emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
			}
			
			
		}
		return getSelectedBioEcatalogueReqApproval(necatrequestreqapprovalcode, ntransfertype, ntransactionstatus,
				userInfo);

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> updateApproveRecord(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Object detailsObj = inputMap.get("bioecataloguerequestdetails");
		if (detailsObj == null || !(detailsObj instanceof List) || ((List<?>) detailsObj).isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_NOSAMPLESAVAILABLE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		int necatrequestreqapprovalcode = (int) inputMap.get("necatrequestreqapprovalcode");
		int ntransfertype = (int) inputMap.get("ntransfertype");
		int ntransactionstatus = (int) inputMap.get("ntransactionstatus");
		int norginsitecode = (int) inputMap.get("norginsitecode");
		String sformnumber = (String) inputMap.get("sformnumber");
		String sapprovalremarks = (String) inputMap.get("sapprovalremarks");

		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();

		// Validate
		final int ValidateAccepted = findStatusforRequest(necatrequestreqapprovalcode, userInfo);
		if (ValidateAccepted == Enumeration.TransactionStatus.REJECTED.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTREQUESTEDRECORDS",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

		// Modified by Gowtham R on nov 18 2025 for jira.id:BGSI-180
		Map<String,Object> selectedBioEcatalogueReqApproval = (Map<String, Object>) getSelectedBioEcatalogueReqApproval(
				necatrequestreqapprovalcode, ntransfertype, ntransactionstatus, userInfo).getBody();
		
		BioEcatalogueReqApproval objBioBGSiECatalogueDetails = (BioEcatalogueReqApproval) 
				selectedBioEcatalogueReqApproval.get("selectedBioEcatalogueReqApproval");

		listBeforeUpdate.add(objBioBGSiECatalogueDetails);

		// Added by Gowtham R on nov 18 2025 for jira.id:BGSI-180
		List<BioEcatalogueRequestDetails> lstBioEcatalogueRequestDetails = (List<BioEcatalogueRequestDetails>) 
				selectedBioEcatalogueReqApproval.get("lstBioEcatalogueRequestDetails");
		
		lstBioEcatalogueRequestDetails = lstBioEcatalogueRequestDetails.stream()
				.filter(item -> (item.getNaccnoofsamples() != -1 ? item.getNaccnoofsamples() : item.getNreqnoofsamples()) > item.getNavailablenoofsample())
				.collect(Collectors.toList());
		
		if(!lstBioEcatalogueRequestDetails.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_NOSAMPLESAVAILABLE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		if (necatrequestreqapprovalcode != 0) {
			int nbioecataloguereqapprovalhistorycodePk = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement where "
							+ "stablename='bioecataloguereqapprovalhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			nbioecataloguereqapprovalhistorycodePk++;

			String strBioEcatalogueReqApprovalHistory = "insert into bioecataloguereqapprovalhistory ("
					+ "nbioecataloguereqapprovalhistorycode, " + "necatrequestreqapprovalcode, "
					+ "ntransactionstatus, " + "dtransactiondate, " + "ntztransactiondate, "
					+ "noffsetdtransactiondate, " + "nusercode, " + "nuserrolecode, " + "ndeputyusercode, "
					+ "ndeputyuserrolecode, " + "nsitecode, " + "nstatus" + ") values ("
					+ nbioecataloguereqapprovalhistorycodePk + ", " + necatrequestreqapprovalcode + ", "
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNsitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			jdbcTemplate.execute(strBioEcatalogueReqApprovalHistory);

			String updateSeqNo = " update seqnobiobankmanagement set nsequenceno="
					+ nbioecataloguereqapprovalhistorycodePk
					+ " where stablename='bioecataloguereqapprovalhistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(updateSeqNo);

			List<Map<String, Object>> detailsList = (List<Map<String, Object>>) detailsObj;

			for (Map<String, Object> detail : detailsList) {
				int necateloguerequestdetailcode = (int) detail.get("necateloguerequestdetailcode");
				String sreqminvolume = String.valueOf(detail.get("sreqminvolume"));
				int nreqnoofsamples = (int) detail.get("nreqnoofsamples");

				// Modified 0 to 1 by Gowtham R on nov 18 2025 for jira.id:BGSI-180
				String saccminvolume = detail.get("saccminvolume") != null ? String.valueOf(detail.get("saccminvolume"))
						: "-1";
				int naccnoofsamples = detail.get("naccnoofsamples") != null ? (int) detail.get("naccnoofsamples") : -1;

				String updateDetailQuery = "";

				if ((saccminvolume != null && !saccminvolume.equals("-1") && !saccminvolume.isEmpty())) {
					updateDetailQuery = "UPDATE bioecataloguerequestdetails SET " + "saccminvolume = '" + saccminvolume
							+ "', " + "naccnoofsamples = " + naccnoofsamples + ", " + "dtransactiondate = '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' "
							+ "WHERE necateloguerequestdetailcode = " + necateloguerequestdetailcode + " "
							+ "AND necatrequestreqapprovalcode = " + necatrequestreqapprovalcode + " "
							+ "AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				} else {
					updateDetailQuery = "UPDATE bioecataloguerequestdetails SET " + "saccminvolume = '" + sreqminvolume
							+ "', " + "naccnoofsamples = " + nreqnoofsamples + ", " + "dtransactiondate = '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' "
							+ "WHERE necateloguerequestdetailcode = " + necateloguerequestdetailcode + " "
							+ "AND necatrequestreqapprovalcode = " + necatrequestreqapprovalcode + " "
							+ "AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
				}

				jdbcTemplate.execute(updateDetailQuery);
			}
			String strUpdateQry2 = "update bioecataloguereqapproval set " + "dtransactiondate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "ntransactionstatus = '"
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "', " + "sapprovalremarks = '"
					+ stringUtilityFunction.replaceQuote(sapprovalremarks) + "' "
					+ "where necatrequestreqapprovalcode = " + necatrequestreqapprovalcode + " and nsitecode = "
					+ userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			
			jdbcTemplate.execute(strUpdateQry2);

			if (ntransfertype == Enumeration.TransferType.BIOBANK.getntransfertype()) {

				String strSelectQry = "select nbgsiecatrequestcode from biobgsiecatalogue " + "where sformnumber='"
						+ stringUtilityFunction.replaceQuote(sformnumber) + "'" + " and nsitecode=" + norginsitecode
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				Integer nbgsiecatrequestcode = (Integer) jdbcTemplateUtilityFunction.queryForObject(strSelectQry, Integer.class, jdbcTemplate);
				
				if (nbgsiecatrequestcode != null && nbgsiecatrequestcode > 0) {

					List<Map<String, Object>> biobgsiecataloguedetails = (List<Map<String, Object>>) inputMap
							.get("bioecataloguerequestdetails");
					
					String sQuery = " lock  table lockbiobgsiecatalogue " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery);

					for (Map<String, Object> details : biobgsiecataloguedetails) {
						String sreqminvolume = String.valueOf(details.get("sreqminvolume"));
						int nreqnoofsamples = Integer.parseInt(String.valueOf(details.get("nreqnoofsamples")));

						String saccminvolume = details.get("saccminvolume") != null
								? String.valueOf(details.get("saccminvolume")).trim()
								: "";
						Integer naccnoofsamples = (details.get("naccnoofsamples") != null)
								? Integer.parseInt(String.valueOf(details.get("naccnoofsamples")))
								: null;

						String finalSaccMinVolume = (saccminvolume == null || saccminvolume.trim().isEmpty() || saccminvolume.equals("-1")) // Added saccminvolume(-1) by Gowtham R on nov 18 2025 for jira.id:BGSI-180
								? stringUtilityFunction.replaceQuote(sreqminvolume)
								: stringUtilityFunction.replaceQuote(saccminvolume);

						// Modified 0 to 1 by Gowtham R on nov 18 2025 for jira.id:BGSI-180
						int finalNaccNoOfSamples = (naccnoofsamples == null || naccnoofsamples == -1)
								? nreqnoofsamples
								: naccnoofsamples;
						
						int nbioprojectcode = Integer.parseInt(String.valueOf(details.get("nbioprojectcode")));
						int nproductcode = Integer.parseInt(String.valueOf(details.get("nproductcode")));
						String sparentsamplecode = details.get("sparentsamplecode") != null
								? String.valueOf(details.get("sparentsamplecode")).trim()
								: "";

						String strWhereSampleCode = sparentsamplecode.isEmpty() ? ""
								: " and sparentsamplecode='" + stringUtilityFunction.replaceQuote(sparentsamplecode)
										+ "'";

						String strUpdateQry1 = "update biobgsiecataloguedetails set dtransactiondate='"
							    + dateUtilityFunction.getCurrentDateTime(userInfo) + "', saccminvolume='"
							    + finalSaccMinVolume + "', naccnoofsamples=" + finalNaccNoOfSamples
								+ " where nbioprojectcode=" + nbioprojectcode + " and nproductcode=" + nproductcode
								+ ((strWhereSampleCode != null && !strWhereSampleCode.isEmpty()) ? strWhereSampleCode
										: "")
								+ ((nbgsiecatrequestcode != null && nbgsiecatrequestcode > 0)
										? " and nbgsiecatrequestcode=" + nbgsiecatrequestcode
										: "")
								+ " and nsitecode=" + norginsitecode + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

						jdbcTemplate.execute(strUpdateQry1);
					}

					String strUpdateQry3 = "update biobgsiecatalogue set dtransactiondate= '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus = '"
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "',"
							+ " sapprovalremarks='" + stringUtilityFunction.replaceQuote(sapprovalremarks)
							+ "' where nbgsiecatrequestcode=" + nbgsiecatrequestcode + " and nsitecode=" + norginsitecode
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					jdbcTemplate.execute(strUpdateQry3);

					int seqNoBGSiECatalogueHistory = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnobiobankmanagement where stablename in (N'biobgsiecataloguehistory') and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);
					seqNoBGSiECatalogueHistory = seqNoBGSiECatalogueHistory + 1;

					String strBioBgsieCatalogueHistory = "INSERT INTO biobgsiecataloguehistory ("
							+ "nbiobgsiecataloguehistorycode, nbgsiecatrequestcode, nusercode, nuserrolecode, "
							+ "ndeputyusercode, ndeputyuserrolecode, ntransactionstatus, scomments, "
							+ "dtransactiondate, ntztransactiondate, noffsettransactiondate, nsitecode, nstatus) VALUES ("
							+ seqNoBGSiECatalogueHistory + ", " + nbgsiecatrequestcode + ", " + userInfo.getNusercode()
							+ ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", "
							+ userInfo.getNdeputyuserrole() + ", " + userInfo.getNdeputyuserrole() + ", "
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNsitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
					jdbcTemplate.execute(strBioBgsieCatalogueHistory);

					String strInsert = "update seqnobiobankmanagement set nsequenceno=" + seqNoBGSiECatalogueHistory
							+ " where" + " stablename='biobgsiecataloguehistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					jdbcTemplate.execute(strInsert);
				}

			}
			if (ntransfertype == Enumeration.TransferType.BIOBANKEXTERNAL.getntransfertype()) {

				Integer nbiobankecatreqexternalcode = null;
				try {
					String strSelectExternalMain = "select nbiobankecatreqexternalcode from biobankecataloguerequestexternal "
							+ "where sformnumber='" + stringUtilityFunction.replaceQuote(sformnumber) + "' "
							+ "and nreceiversitecode=" + norginsitecode + " " + "and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					
					nbiobankecatreqexternalcode = jdbcTemplate.queryForObject(strSelectExternalMain, Integer.class);
				} catch (Exception ex) {
					nbiobankecatreqexternalcode = null;
				}
					
				if (nbiobankecatreqexternalcode != null && nbiobankecatreqexternalcode > 0) {

//					@SuppressWarnings("unchecked")
//					List<Map<String, Object>> externalDetails = (List<Map<String, Object>>) (inputMap
//							.get("biobankecataloguerequestexternaldetails") != null
//									? inputMap.get("biobankecataloguerequestexternaldetails")
//									: inputMap.get("biobankecatalogrequestexternaldetails"));
					List<Map<String, Object>> externalDetails = (List<Map<String, Object>>) inputMap
							.get("bioecataloguerequestdetails");
					
					if (externalDetails != null) {
						for (Map<String, Object> details : externalDetails) {

							String sreqminvolume = String.valueOf(details.get("sreqminvolume"));
							int nreqnoofsamples = Integer.parseInt(String.valueOf(details.get("nreqnoofsamples")));

							String saccminvolume = details.get("saccminvolume") != null
									? String.valueOf(details.get("saccminvolume")).trim()
									: "";
							Integer naccnoofsamples = (details.get("naccnoofsamples") != null)
									? Integer.parseInt(String.valueOf(details.get("naccnoofsamples")))
									: null;

							String finalSaccMinVolume = (saccminvolume == null || saccminvolume.trim().isEmpty() || saccminvolume.equals("-1")) // Added saccminvolume(-1) by Gowtham R on nov 18 2025 for jira.id:BGSI-180
									? stringUtilityFunction.replaceQuote(sreqminvolume)
									: stringUtilityFunction.replaceQuote(saccminvolume);

							// Modified 0 to 1 by Gowtham R on nov 18 2025 for jira.id:BGSI-180
							int finalNaccNoOfSamples = (naccnoofsamples == null || naccnoofsamples == -1)
									? nreqnoofsamples
									: naccnoofsamples;
							
							int nbioprojectcode = Integer.parseInt(String.valueOf(details.get("nbioprojectcode")));
							int nproductcode = Integer.parseInt(String.valueOf(details.get("nproductcode")));
							String sparentsamplecode = details.get("sparentsamplecode") != null
									? String.valueOf(details.get("sparentsamplecode")).trim()
									: "";

							String strWhereSampleCode = sparentsamplecode.isEmpty() ? ""
									: " and sparentsamplecode='" + stringUtilityFunction.replaceQuote(sparentsamplecode)
											+ "'";

							String strUpdateDetails = "update biobankecataloguerequestexternaldetails set "
								    + "dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
									+ "saccminvolume='" + finalSaccMinVolume + "', " + "naccnoofsamples="
									+ finalNaccNoOfSamples + " " + "where nbiobankecatreqexternalcode =  "
									+ nbiobankecatreqexternalcode + " and nbioprojectcode=" + nbioprojectcode
								    + " and nproductcode=" + nproductcode
									+ ((strWhereSampleCode != null && !strWhereSampleCode.isEmpty())
											? strWhereSampleCode
											: "")
									+ " and nsitecode=" + norginsitecode + " and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

							jdbcTemplate.execute(strUpdateDetails);
				}
					}
					
					final String sQuery = " lock  table lockbiobankecataloguerequestexternal "
							+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery);

					String strApproveMain = "update biobankecataloguerequestexternal set dtransactiondate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntransactionstatus="
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ","
							+ " sapprovalremarks='" + stringUtilityFunction.replaceQuote(sapprovalremarks)
							+ "' where nbiobankecatreqexternalcode=" + nbiobankecatreqexternalcode
							+ " and nreceiversitecode=" + norginsitecode + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					jdbcTemplate.execute(strApproveMain);

					int seqNoExternalHistory = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnobiobankmanagement "
									+ "where stablename in (N'biobankecataloguerequestexternalhistory') "
									+ "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);
					seqNoExternalHistory = seqNoExternalHistory + 1;

					String strExternalHistoryInsert = "INSERT INTO biobankecataloguerequestexternalhistory ("
							+ "nbiobankecatreqexternalhistorycode, " + "nbiobankecatreqexternalcode, "
							+ "nusercode, nuserrolecode, " + "ndeputyusercode, ndeputyuserrolecode, "
							+ "ntransactionstatus, scomments, "
							+ "dtransactiondate, ntztransactiondate, noffsettransactiondate, "
							+ "nsitecode, nstatus) VALUES (" + seqNoExternalHistory + ", " + nbiobankecatreqexternalcode
							+ ", " + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", " + "'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNsitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

					jdbcTemplate.execute(strExternalHistoryInsert);

					String strSeqUpdate = "update seqnobiobankmanagement set nsequenceno=" + seqNoExternalHistory + " "
							+ "where stablename='biobankecataloguerequestexternalhistory' " + "and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					jdbcTemplate.execute(strSeqUpdate);
			}
			}
			if (ntransfertype == Enumeration.TransferType.THIRDPARTY.getntransfertype()) {

				String strSelectThirdPartyMain = "select nthirdpartyecatrequestcode from biothirdpartyecataloguerequest "
						+ "where sformnumber='" + stringUtilityFunction.replaceQuote(sformnumber) + "' "
						+ "and nreceiversitecode=" + norginsitecode + " " + "and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				
				Integer nthirdpartyecatrequestcode = (Integer) jdbcTemplateUtilityFunction.queryForObject(strSelectThirdPartyMain, Integer.class, jdbcTemplate);

				if (nthirdpartyecatrequestcode != null && nthirdpartyecatrequestcode > 0) {

//					@SuppressWarnings("unchecked")
//					List<Map<String, Object>> tpDetails = (List<Map<String, Object>>) inputMap
//							.get("biothirdpartyecataloguedetails");
					List<Map<String, Object>> tpDetails = (List<Map<String, Object>>) inputMap
							.get("bioecataloguerequestdetails");

					if (tpDetails != null && !tpDetails.isEmpty()) {
						
						final String sQuery = " lock  table lockbiothirdpartyecatalogue "
								+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
						jdbcTemplate.execute(sQuery);
						
						for (Map<String, Object> details : tpDetails) {

							String sreqminvolume = String.valueOf(details.get("sreqminvolume"));
							int nreqnoofsamples = Integer.parseInt(String.valueOf(details.get("nreqnoofsamples")));

							String saccminvolume = details.get("saccminvolume") != null
									? String.valueOf(details.get("saccminvolume")).trim()
									: "";
							Integer naccnoofsamples = (details.get("naccnoofsamples") != null)
									? Integer.parseInt(String.valueOf(details.get("naccnoofsamples")))
									: null;

							String finalSaccMinVolume = (saccminvolume == null || saccminvolume.trim().isEmpty() || saccminvolume.equals("-1")) // Added saccminvolume(-1) by Gowtham R on nov 18 2025 for jira.id:BGSI-180
									? stringUtilityFunction.replaceQuote(sreqminvolume)
									: stringUtilityFunction.replaceQuote(saccminvolume);

							// Modified 0 to 1 by Gowtham R on nov 18 2025 for jira.id:BGSI-180
							int finalNaccNoOfSamples = (naccnoofsamples == null || naccnoofsamples == -1)
									? nreqnoofsamples
									: naccnoofsamples;

							int nbioprojectcode = Integer.parseInt(String.valueOf(details.get("nbioprojectcode")));
							int nproductcode = Integer.parseInt(String.valueOf(details.get("nproductcode")));
							String sparentsamplecode = details.get("sparentsamplecode") != null
									? String.valueOf(details.get("sparentsamplecode")).trim()
									: "";

							String strWhereSampleCode = sparentsamplecode.isEmpty() ? ""
									: " and sparentsamplecode='" + stringUtilityFunction.replaceQuote(sparentsamplecode)
											+ "'";

							String strUpdateTPDetails = "update biothirdpartyecataloguereqdetails set "
								    + "dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								    + "ntztransactiondate=" + userInfo.getNtimezonecode() + ", "
									+ "noffsetdtransactiondate="
									+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
									+ "saccminvolume='" + finalSaccMinVolume + "', " + "naccnoofsamples="
									+ finalNaccNoOfSamples + " " + "where nthirdpartyecatrequestcode="
									+ nthirdpartyecatrequestcode + " " + " and nbioprojectcode=" + nbioprojectcode
								    + " and nproductcode=" + nproductcode
									+ ((strWhereSampleCode != null && !strWhereSampleCode.isEmpty())
											? strWhereSampleCode
											: "")
									+ " and nsitecode=" + norginsitecode + " and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

							jdbcTemplate.execute(strUpdateTPDetails);
						}
					}

					String strApproveThirdPartyMain = "update biothirdpartyecataloguerequest set "
							+ "dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ "ntztransactiondate=" + userInfo.getNtimezonecode() + ", " + "noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ "ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
							+ ", sapprovalremarks='" + stringUtilityFunction.replaceQuote(sapprovalremarks) + "' "
							+ "where nthirdpartyecatrequestcode=" + nthirdpartyecatrequestcode + " "
							+ "and nreceiversitecode=" + norginsitecode + " " + "and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					jdbcTemplate.execute(strApproveThirdPartyMain);

					// ===== History insert (biothirdpartyecathistory) =====
					int seqNoTPHistory = jdbcTemplate.queryForObject("select nsequenceno from seqnobiobankmanagement "
							+ "where stablename in ('biothirdpartyecatreqhistory') " + "and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
					seqNoTPHistory = seqNoTPHistory + 1;

					String strTPHistoryInsert = "insert into biothirdpartyecatreqhistory ("
							+ "nbiothirdpartyecatreqhistorycode, " + "nthirdpartyecatrequestcode, "
							+ "nusercode, nuserrolecode, " + "ndeputyusercode, ndeputyuserrolecode, "
							+ "ntransactionstatus, scomments, "
							+ "dtransactiondate, ntztransactiondate, noffsettransactiondate, "
							+ "nsitecode, nstatus) values (" + seqNoTPHistory + ", " + nthirdpartyecatrequestcode + ", "
							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", " + "'"
							+ stringUtilityFunction.replaceQuote(sapprovalremarks) + "', " + "'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ norginsitecode + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ ");";

					jdbcTemplate.execute(strTPHistoryInsert);

					String strTPHistorySeqUpdate = "update seqnobiobankmanagement set nsequenceno=" + seqNoTPHistory
							+ " " + "where stablename='biothirdpartyecatreqhistory' " + "and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					jdbcTemplate.execute(strTPHistorySeqUpdate);
					// ===== End history =====
				}
			}

			// === COC: START (Approve flow) ===
//			List<Integer> cocDetailIds = new ArrayList<>();
//			for (Map<String, Object> d : (List<Map<String, Object>>) detailsObj) {
//				cocDetailIds.add((int) d.get("necateloguerequestdetailcode"));
//			}
//
//			if (!cocDetailIds.isEmpty()) {
//				String cocIdCsv = cocDetailIds.stream().map(String::valueOf).collect(Collectors.joining(","));
//
//				int chainCustodyPk = jdbcTemplate.queryForObject(
//						"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//						Integer.class);
//
//				String strChainCustody = "insert into chaincustody ("
//				        + " nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno, "
//				        + " ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate, "
//				        + " noffsetdtransactiondate, sremarks, nsitecode, nstatus) " + " select " + chainCustodyPk
//				        + " + rank() over(order by berdd.necateloguerequestdetailcode), " + userInfo.getNformcode()
//				        + ", " + " berdd.necateloguerequestdetailcode, 'necateloguerequestdetailcode', "
//				        + " 'bioecataloguerequestdetails', " + " COALESCE('"
//				        + stringUtilityFunction.replaceQuote(sformnumber) + "', ''), "
//				        + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + ", " + userInfo.getNusercode()
//				        + ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
//				        + "', " + userInfo.getNtimezonecode() + ", "
//				        + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " (" + " '"
//				        + commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//				        + " [' || " + " COALESCE(NULLIF(berdd.sparentsamplecode,''), '') || " + " '] ' || " + " '"
//				        + commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//				        + " [' || " + " COALESCE('" + stringUtilityFunction.replaceQuote(sformnumber) + "', '') || "
//				        + " ']') " + ", " + userInfo.getNtranssitecode() + ", "
//				        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				        + " from bioecataloguerequestdetails berdd " + " where berdd.necateloguerequestdetailcode in ("
//				        + cocIdCsv + ") " + "   and berdd.necatrequestreqapprovalcode = " + necatrequestreqapprovalcode
//				        + " " + "   and berdd.nsitecode = " + userInfo.getNtranssitecode() + " "
//				        + "   and berdd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//				String strSeqUpdate = "update seqnoregistration set nsequenceno = nsequenceno + " + cocDetailIds.size()
//					    + " where stablename='chaincustody' and nstatus="
//					    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//					jdbcTemplate.execute(strSeqUpdate);
//				
//				jdbcTemplate.execute(strChainCustody);
//			}
			// === COC: END ===

			BioEcatalogueReqApproval objBioBGSiECatalogueDetails1 = (BioEcatalogueReqApproval) ((Map<?, ?>) getSelectedBioEcatalogueReqApproval(
					necatrequestreqapprovalcode, ntransfertype, ntransactionstatus, userInfo).getBody())
					.get("selectedBioEcatalogueReqApproval");

			listAfterUpdate.add(objBioBGSiECatalogueDetails1);
			multilingualIDList.add("IDS_APPROVED");
			auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
					userInfo);
			
			// added by sujatha ATE_274 BGSI-148 for Sending Mail when approve the request
			final Map<String, Object> mailMap = new HashMap<String, Object>();
			mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));
			mailMap.put("necatrequestreqapprovalcode", inputMap.get("necatrequestreqapprovalcode"));
			mailMap.put("orginsitecode",inputMap.get("norginsitecode") );
			final UserInfo mailUserInfo = new UserInfo(userInfo);
			mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
			mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
		    emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
		    
		}
		return getSelectedBioEcatalogueReqApproval(necatrequestreqapprovalcode, ntransfertype, ntransactionstatus,
				userInfo);

	}

	// Added by Gowtham R on nov 18 2025 for jira.id:BGSI-180
	@Override
	public ResponseEntity<Object> getBioEcatalogueReqApprovalDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		
		List<Map<String, Object>> lstBioEcatalogueRequestDetails = (List<Map<String, Object>>) inputMap.get("lstBioEcatalogueRequestDetails");
		
		final int necatrequestreqapprovalcode = (int) inputMap.get("necatrequestreqapprovalcode");
		final int ntransfertype = (int) inputMap.get("ntransfertype");
		final String decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
				? "."
				: userInfo.getSdecimaloperator();
		final String opForRegex = escapeForSqlRegexCharClass(decOperator);
		final String opForReplace = stringUtilityFunction.replaceQuote(decOperator);
		final String patternLiteral = "'^[0-9]+([" + opForRegex + "][0-9]+)?$' COLLATE \"default\"";
		final String castExprSt = " REPLACE(TRIM(st.sqty COLLATE \"default\"), '" + opForReplace + "', '.')::numeric";
		
		for (int i=0; i<lstBioEcatalogueRequestDetails.size(); i++) {
			final String castExprA = " REPLACE(TRIM(COALESCE('" + lstBioEcatalogueRequestDetails.get(i).get("saccminvolume") 
				+ "','" + lstBioEcatalogueRequestDetails.get(i).get("sreqminvolume") + "') COLLATE \"default\"), '" + opForReplace + "', '.')::numeric";
			
			final String strConcatFormType = getConcatJoinBioSubjectDetails(ntransfertype, userInfo);
			
			final String strQuery = "SELECT COUNT(1) FROM samplestoragetransaction st " + strConcatFormType + " WHERE "
					+ " st.nprojecttypecode = " + lstBioEcatalogueRequestDetails.get(i).get("nbioprojectcode") 
					+ " AND st.nproductcode = " + lstBioEcatalogueRequestDetails.get(i).get("nproductcode")
					+ " AND st.nsitecode = " + userInfo.getNtranssitecode() + " AND st.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " AND (CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + patternLiteral + ") THEN " + castExprSt
					+ " ELSE NULL END) >= (CASE WHEN (TRIM('" + lstBioEcatalogueRequestDetails.get(i).get("sreqminvolume") 
					+ "' COLLATE \"default\")) ~ (" + patternLiteral + ") THEN " + castExprA + " ELSE NULL END) ";
			
			List<Map<String, Object>> map = jdbcTemplate.queryForList(strQuery);
			
			lstBioEcatalogueRequestDetails.get(i).put("navailablenoofsample", map.get(0).get("count"));
		}
		
		outputMap.put("lstBioEcatalogueRequestDetails", lstBioEcatalogueRequestDetails);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

}
