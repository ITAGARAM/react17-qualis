package com.agaramtech.qualis.biobank.service.biothirdpartyecatalogue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.agaramtech.qualis.biobank.model.BioThirdPartyECatalogueReqDetails;
import com.agaramtech.qualis.biobank.model.BioThirdPartyECatalogueRequest;
import com.agaramtech.qualis.biobank.model.RequestFormType;
import com.agaramtech.qualis.biobank.model.SeqNoBioBankManagement;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.project.model.BioProject;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class BioThirdPartyECatalogueDAOImpl implements BioThirdPartyECatalogueDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(BioThirdPartyECatalogueDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getBioThirdPartyECatalogue(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		LOGGER.info("getBioThirdPartyECatalogue....");
		Map<String, Object> returnMap = new HashMap<>();
		List<TransactionStatus> lstTransactionstatus = new ArrayList<>();
		final String currentUIDate = (String) inputMap.get("currentdate");
		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			returnMap = projectDAOSupport.getDateFromControlProperties(userInfo, currentUIDate, "datetime", "FromDate");
		}
		lstTransactionstatus = getThirdPartyECatalogueRequestFormStatuses(userInfo);
		final TransactionStatus filterTransactionStatus = lstTransactionstatus.get(0);
		returnMap.put("FilterStatusValue", filterTransactionStatus);
		returnMap.put("nfilterstatus", filterTransactionStatus.getNtransactionstatus());
		returnMap.put("RealFilterStatusValue", filterTransactionStatus);
		returnMap.put("FilterStatus", lstTransactionstatus);
		returnMap.put("RealFilterStatuslist", lstTransactionstatus);
		returnMap.put("FromDateFilter", returnMap.get("FromDate"));
		returnMap.put("ToDateFilter", returnMap.get("ToDate"));
		returnMap.put("FromDate", returnMap.get("FromDateWOUTC"));
		returnMap.put("ToDate", returnMap.get("ToDateWOUTC"));
		returnMap.put("RealFromDate", returnMap.get("FromDateWOUTC"));
		returnMap.put("RealToDate", returnMap.get("ToDateWOUTC"));
		List<RequestFormType> lstReqFormType = getRequestFormTypeComboData(userInfo);
		returnMap.put("lstReqFormType", lstReqFormType);
		returnMap.put("selectedReqFormType", lstReqFormType.getFirst());
		List<BioProject> lstProject = getBioProjectComboData(userInfo);
		returnMap.put("lstBioProject", lstProject);
		List<Site> lstSite = new ArrayList<>();
		if (!lstProject.isEmpty()) {
			returnMap.put("selectedBioProject", lstProject.getFirst());
			lstSite = getSiteComboFromProject(lstProject.getFirst().getNbioprojectcode(), userInfo);
		}
		returnMap.put("lstSite", lstSite);
		returnMap.put("subjectCountsByProductAndProjectRows",
				((Map<String, Object>) getSubjectCountsByProductAndProject(userInfo).getBody()).get("rows"));
		returnMap.put("subjectCountsByProductAndDiseaseRows",
				((Map<String, Object>) getSubjectCountsByProductAndDisease(userInfo).getBody()).get("rows"));

		List<BioThirdPartyECatalogueRequest> lstBioThirdPartyECatalogueRequests = getListThirdPartyECatalogueRequest(
				returnMap, userInfo);
		if (!lstBioThirdPartyECatalogueRequests.isEmpty()) {
			returnMap.put("lstBioThirdPartyECatalogueRequests", lstBioThirdPartyECatalogueRequests);
			returnMap.put("selectedBioThirdPartyECatalogueRequest", lstBioThirdPartyECatalogueRequests.getFirst());
			returnMap.put("lstBioThirdPartyECatalogueDetails", getBioThirdPartyECatalogueDetailsFromRequest(
					lstBioThirdPartyECatalogueRequests.getFirst().getNthirdpartyecatrequestcode(), userInfo));

		} else {
			returnMap.put("lstBioThirdPartyECatalogueRequests", null);
			returnMap.put("selectedBioThirdPartyECatalogueRequest", null);
			returnMap.put("lstBioThirdPartyECatalogueDetails", null);
		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getThirdPartyECatalogueByFilterSubmit(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		final DateTimeFormatter dbPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		final DateTimeFormatter uiPattern = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());

		String fromDate = LocalDateTime.parse((String) inputMap.get("FromDate"), dbPattern).format(uiPattern);
		String toDate = LocalDateTime.parse((String) inputMap.get("ToDate"), dbPattern).format(uiPattern);

		returnMap.put("RealFromDate", fromDate);
		returnMap.put("RealToDate", toDate);

		fromDate = dateUtilityFunction.instantDateToString(
				dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("FromDate"), userInfo, true));
		toDate = dateUtilityFunction.instantDateToString(
				dateUtilityFunction.convertStringDateToUTC((String) inputMap.get("ToDate"), userInfo, true));
		inputMap.put("FromDate", fromDate);
		inputMap.put("ToDate", toDate);
		List<TransactionStatus> lstTransactionstatus = getThirdPartyECatalogueRequestFormStatuses(userInfo);
		returnMap.put("FilterStatus", lstTransactionstatus);
		returnMap.put("RealFilterStatuslist", lstTransactionstatus);

		List<BioThirdPartyECatalogueRequest> lstBioThirdPartyECatalogueRequests = getListThirdPartyECatalogueRequest(
				inputMap, userInfo);
		if (!lstBioThirdPartyECatalogueRequests.isEmpty()) {
			returnMap.put("lstBioThirdPartyECatalogueRequests", lstBioThirdPartyECatalogueRequests);
			returnMap.put("selectedBioThirdPartyECatalogueRequest", lstBioThirdPartyECatalogueRequests.getFirst());
			returnMap.put("lstBioThirdPartyECatalogueDetails", getBioThirdPartyECatalogueDetailsFromRequest(
					lstBioThirdPartyECatalogueRequests.getFirst().getNthirdpartyecatrequestcode(), userInfo));

		} else {
			returnMap.put("lstBioThirdPartyECatalogueRequests", null);
			returnMap.put("selectedBioThirdPartyECatalogueRequest", null);
			returnMap.put("lstBioThirdPartyECatalogueDetails", null);
		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getActiveThirdPartyECatalogueRequestForm(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		final int nthirdpartyecatrequestcode = Integer.valueOf(inputMap.get("nthirdpartyecatrequestcode").toString());
		final String sampleQry = "select "
				+ "bc.nthirdpartyecatrequestcode, bc.nthirdpartycode, bc.sformnumber,bc.nreqformtypecode, bc.nreceiversitecode, rs.ssitename as sreceivingsitename, "
				+ "bc.drequesteddate, bc.ntzrequesteddate, tz1.stimezoneid as stzrequesteddate, "
				+ "coalesce(rft.jsondata->'sreqformtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "', rft.jsondata->'sreqformtypename'->>'en-US') as sreqformtypename, "
				+ "COALESCE(TO_CHAR(bc.drequesteddate,'" + userInfo.getSsitedate() + "'), '') as srequesteddate, "
				+ "bc.sremarks, bc.sapprovalremarks, bc.ntransactionstatus,cm.scolorhexcode, "
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus, "
				+ "bc.dtransactiondate, bc.ntztransactiondate, tz2.stimezoneid as stztransactiondate, "
				+ "COALESCE(TO_CHAR(bc.dtransactiondate,'" + userInfo.getSsitedate() + "'), '') as stransactiondate, "
				+ "p.sprojecttitle as sprojecttitles, bc.nbioprojectcode "
				+ "from biothirdpartyecataloguerequest bc, site rs, transactionstatus ts, timezone tz1, timezone tz2, requestformtype rft, "
				+ "bioproject p, formwisestatuscolor fwsc, colormaster cm " + "where bc.nthirdpartyecatrequestcode = "
				+ nthirdpartyecatrequestcode + " and bc.nreqformtypecode = rft.nreqformtypecode and rft.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bc.nreceiversitecode = rs.nsitecode and rs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bc.ntransactionstatus = ts.ntranscode and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bc.ntzrequesteddate = tz1.ntimezonecode and tz1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bc.ntztransactiondate = tz2.ntimezonecode and tz2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bc.nbioprojectcode = p.nbioprojectcode and p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and bc.nsitecode = "
				+ userInfo.getNtranssitecode() + " and bc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and fwsc.ntranscode = ts.ntranscode and fwsc.nformcode = " + userInfo.getNformcode()
				+ " and fwsc.nsitecode = " + userInfo.getNmastersitecode() + " and fwsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and fwsc.ncolorcode=cm.ncolorcode and cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		final BioThirdPartyECatalogueRequest objBioThirdPartyECatalogueRequest = (BioThirdPartyECatalogueRequest) jdbcUtilityTemplateFunction
				.queryForObject(sampleQry, BioThirdPartyECatalogueRequest.class, jdbcTemplate);
		if (objBioThirdPartyECatalogueRequest != null) {
			returnMap.put("selectedBioThirdPartyECatalogueRequest", objBioThirdPartyECatalogueRequest);
			returnMap.put("lstBioThirdPartyECatalogueDetails",
					getBioThirdPartyECatalogueDetailsFromRequest(nthirdpartyecatrequestcode, userInfo));
		} else {
			returnMap.put("selectedBioThirdPartyECatalogueRequest", null);
			returnMap.put("lstBioThirdPartyECatalogueDetails", null);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	private List<BioThirdPartyECatalogueReqDetails> getBioThirdPartyECatalogueDetailsFromRequest(
			final int nthirdpartyecatrequestcode, final UserInfo userInfo) throws Exception {

		final String catalogueDetailQry = "select bcd.nthirdpartyecatreqdetailcode, bcd.nthirdpartyecatrequestcode, COALESCE(bcd.sparentsamplecode,'-') as sparentsamplecode, bcd.nreqnoofsamples, "
				+ "bcd.sreqminvolume, COALESCE(bcd.naccnoofsamples :: text ,'-') as naccnoofsamples, COALESCE(bcd.saccminvolume,'-') as saccminvolume, "
				+ "bc.sformnumber, s.ssitename as sreceivingsitename, "
				+ "bcd.nbioprojectcode, bp.sprojecttitle, bcd.nproductcode, pr.sproductname ||' ('||pc.sproductcatname||')' as sproductname, "
				+ "bcd.dtransactiondate, bcd.ntztransactiondate, bcd.noffsetdtransactiondate, tz1.stimezoneid as stztransactiondate, "
				+ "COALESCE(TO_CHAR(bcd.dtransactiondate,'" + userInfo.getSsitedate() + "'), '') as stransactiondate "
				+ "from biothirdpartyecataloguereqdetails bcd, biothirdpartyecataloguerequest bc, bioproject bp, site s, product pr, productcategory pc, timezone tz1 "
				+ "where bcd.nthirdpartyecatrequestcode = " + nthirdpartyecatrequestcode + " " + "and bcd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and bcd.nsitecode = "
				+ userInfo.getNtranssitecode() + " "
				+ "and bc.nthirdpartyecatrequestcode = bcd.nthirdpartyecatrequestcode and bc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bc.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + "and s.nsitecode = bc.nreceiversitecode and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nmastersitecode = "
				+ userInfo.getNmastersitecode() + " " + "and bcd.nbioprojectcode = bp.nbioprojectcode and bp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bcd.nproductcode = pr.nproductcode and pr.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ "and pr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and pc.nproductcatcode = pr.nproductcatcode and pc.nsitecode = " + userInfo.getNmastersitecode()
				+ " " + "and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bcd.ntztransactiondate = tz1.ntimezonecode and tz1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "order by bcd.nthirdpartyecatreqdetailcode desc";

		return jdbcTemplate.query(catalogueDetailQry, new BioThirdPartyECatalogueReqDetails());
	}

	private List<BioThirdPartyECatalogueRequest> getListThirdPartyECatalogueRequest(Map<String, Object> inputMap,
			UserInfo userInfo) {
		String fromDate = inputMap.containsKey("FromDateFilter") ? (String) inputMap.get("FromDateFilter")
				: (String) inputMap.get("FromDate");
		String toDate = inputMap.containsKey("ToDateFilter") ? (String) inputMap.get("ToDateFilter")
				: (String) inputMap.get("ToDate");

		Object obj = inputMap.get("nfilterstatus");
		int nfilterstatus = 0;
		if (obj instanceof Number) {
			nfilterstatus = ((Number) obj).intValue(); // works for Integer, Short, Long, Double, etc.
		}

		String filterQry = "";
		if (Enumeration.TransactionStatus.ALL.gettransactionstatus() != nfilterstatus) {
			filterQry = " and bc.ntransactionstatus = " + nfilterstatus;
		}

		final String sampleQry = "select "
				+ "bc.nthirdpartyecatrequestcode, bc.nthirdpartycode, bc.sformnumber, bc.nreceiversitecode, rs.ssitename as sreceivingsitename, "
				+ "bc.nreqformtypecode, coalesce(rft.jsondata->'sreqformtypename'->>'" + userInfo.getSlanguagetypecode()
				+ "', rft.jsondata->'sreqformtypename'->>'en-US') as sreqformtypename, "
				+ "bc.drequesteddate, bc.ntzrequesteddate, tz1.stimezoneid as stzrequesteddate, "
				+ "COALESCE(TO_CHAR(bc.drequesteddate,'" + userInfo.getSsitedate() + "'), '') as srequesteddate, "
				+ "bc.sremarks, bc.ntransactionstatus, " + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus, cm.scolorhexcode,"
				+ "bc.dtransactiondate, bc.ntztransactiondate, tz2.stimezoneid as stztransactiondate, "
				+ "COALESCE(TO_CHAR(bc.dtransactiondate,'" + userInfo.getSsitedate() + "'), '') as stransactiondate, "
				+ "p.sprojecttitle as sprojecttitles, bc.nbioprojectcode "
				+ "from site rs, transactionstatus ts, timezone tz1, timezone tz2, requestformtype rft, "
				+ "bioproject p, formwisestatuscolor fwsc, colormaster cm, biothirdpartyecataloguerequest bc "
				+ " join thirdparty tp on tp.nthirdpartycode=bc.nthirdpartycode and tp.nisngs="
				+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " and tp.nsitecode="
				+ userInfo.getNmastersitecode() + " and tp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join thirdpartyusermapping tpm on tpm.nthirdpartycode=tp.nthirdpartycode and tpm.nusercode="
				+ userInfo.getNusercode() + " and tpm.nuserrolecode=" + userInfo.getNuserrole() + " and tpm.nsitecode="
				+ userInfo.getNmastersitecode() + " and tpm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " where bc.nreceiversitecode = rs.nsitecode and rs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and bc.nreqformtypecode = rft.nreqformtypecode and rft.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bc.ntransactionstatus = ts.ntranscode and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bc.ntzrequesteddate = tz1.ntimezonecode and tz1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bc.ntztransactiondate = tz2.ntimezonecode and tz2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bc.nbioprojectcode = p.nbioprojectcode and p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and bc.nsitecode = "
				+ userInfo.getNtranssitecode() + " and bc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and fwsc.ntranscode = ts.ntranscode and fwsc.nformcode = " + userInfo.getNformcode()
				+ " and fwsc.nsitecode = " + userInfo.getNmastersitecode() + " and fwsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and fwsc.ncolorcode=cm.ncolorcode and cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + filterQry
				+ " and bc.dtransactiondate between '" + fromDate + "' and '" + toDate + "' "
				+ "order by bc.nthirdpartyecatrequestcode desc";

		return jdbcTemplate.query(sampleQry, new BioThirdPartyECatalogueRequest());
	}

	public List<TransactionStatus> getThirdPartyECatalogueRequestFormStatuses(final UserInfo userInfo)
			throws Exception {

		final String strQry = "select t.ntranscode as ntransactionstatus,"
				+ " coalesce(t.jsondata->'stransdisplaystatus'->> '" + userInfo.getSlanguagetypecode()
				+ "',t.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus"
				+ " from approvalstatusconfig ascg,transactionstatus t" + " where t.ntranscode = ascg.ntranscode"
				+ " and ascg.nformcode = " + userInfo.getNformcode() + " and t.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ascg.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ascg.nsitecode= "
				+ userInfo.getNmastersitecode() + " and ascg.nregtypecode = -1 and ascg.nregsubtypecode = -1"
				+ " group by ntransactionstatus, stransdisplaystatus order by ntransactionstatus";
		return jdbcTemplate.query(strQry, new TransactionStatus());
	}

	public List<RequestFormType> getRequestFormTypeComboData(final UserInfo userInfo) throws Exception {

		final String strQry = "select r.nreqformtypecode, coalesce(r.jsondata->'sreqformtypename'->> '"
				+ userInfo.getSlanguagetypecode() + "', r.jsondata->'sreqformtypename'->>'en-US') as sreqformtypename"
				+ " from requestformtype r where r.nsitecode = " + userInfo.getNmastersitecode() + " and r.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nreqformtypecode!=2"
				+ "	 order by r.nreqformtypecode";
		return jdbcTemplate.query(strQry, new RequestFormType());
	}

	public List<BioProject> getBioProjectComboData(final UserInfo userInfo) throws Exception {
		final String strQry = "select bp.sprojecttitle ,bp.nbioprojectcode	 from biodataaccessrequest bdar"
				+ " join biodataaccessrequestdetails bdard on bdard.nbiodataaccessrequestcode=bdar.nbiodataaccessrequestcode"
				+ " and bdard.nsitecode=" + userInfo.getNtranssitecode() + " and bdard.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join thirdparty tp"
				+ " on tp.nthirdpartycode=bdar.nthirdpartycode and tp.nsitecode=" + userInfo.getNmastersitecode()
				+ " and tp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nisngs="
				+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " join thirdpartyusermapping tpm"
				+ " on tpm.nthirdpartycode=tp.nthirdpartycode and tpm.nsitecode=" + userInfo.getNmastersitecode()
				+ " and tpm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tpm.nusercode=bdard.nusercode and tpm.nusercode=" + userInfo.getNusercode()
				+ " and tpm.nuserrolecode=bdard.nuserrolecode and tpm.nuserrolecode=" + userInfo.getNuserrole()
				+ " join bioproject bp on bp.nbioprojectcode=bdard.nbioprojectcode" + " and bp.nsitecode="
				+ userInfo.getNmastersitecode() + " and bp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bdar.nsitecode="
				+ userInfo.getNtranssitecode() + " and bdar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bdar.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " group by bp.sprojecttitle ,bp.nbioprojectcode order by bp.sprojecttitle";
		return jdbcTemplate.query(strQry, new BioProject());
	}

	public List<Site> getSiteComboFromProject(final int nbioprojectcode, final UserInfo userInfo) throws Exception {

		String strQuerySite = "select shcd.schildsitecode "
				+ "from projectsitehierarchymapping pshm, sitehierarchyconfigdetails shcd "
				+ "where pshm.nsitehierarchyconfigcode = shcd.nsitehierarchyconfigcode " + "and pshm.nbioprojectcode = "
				+ nbioprojectcode + " " + "and shcd.nnodesitecode = " + userInfo.getNtranssitecode() + " "
				+ "and pshm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and shcd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and pshm.nsitecode = " + userInfo.getNmastersitecode();

		final List<Map<String, Object>> listSiteHierMapping = jdbcTemplate.queryForList(strQuerySite);

		String ssitecode = "-1";

		if (!listSiteHierMapping.isEmpty()) {
			final String schildsitecode = (String) listSiteHierMapping.get(0).get("schildsitecode");
			if (schildsitecode != null && !schildsitecode.isEmpty()) {
				ssitecode = ssitecode + "," + schildsitecode;
			}
		}

		final String strQuery = "select nsitecode, ssitename, ndefaultstatus from site " + "where nsitecode in ("
				+ ssitecode + ") " + "and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode > 0 order by ssitename ;";

		return jdbcTemplate.query(strQuery, new Site());
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getComboDataForCatalogue(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();

		final boolean isOpenCatalogue = inputMap.containsKey("isOpenCatalogue")
				? (boolean) inputMap.get("isOpenCatalogue")
				: false;

		if (!isOpenCatalogue) {
			final int checkThirdParty = getThirdPartyCode(userInfo);

			if (checkThirdParty == -1) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_THIRDPARTYUSERONLYALLOWEDTOCREATEREQUEST",
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}

		List<RequestFormType> lstReqFormType = getRequestFormTypeComboData(userInfo);
		returnMap.put("lstReqFormType", lstReqFormType);
		returnMap.put("selectedReqFormType", lstReqFormType.getFirst());
		List<BioProject> lstProject = getBioProjectComboData(userInfo);
		returnMap.put("lstBioProject", lstProject);
		List<Site> lstSite = new ArrayList<>();
		if (!lstProject.isEmpty()) {
			lstSite = getSiteComboFromProject(lstProject.getFirst().getNbioprojectcode(), userInfo);
			returnMap.put("selectedBioProject", lstProject.getFirst());
		}
		List<Product> lstProduct = new ArrayList<>();
		if (lstSite != null && !lstSite.isEmpty()) {
			lstProduct = (List<Product>) getBioSampleTypeCombo(lstProject.getFirst().getNbioprojectcode(), userInfo)
					.getBody();
			returnMap.put("selectedSite", lstSite.getFirst());
		}
		returnMap.put("lstSite", lstSite);
		returnMap.put("lstProduct", lstProduct);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSiteComboForProject(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		final int nselectedprojectcode = Integer.valueOf(inputMap.get("nselectedprojectcode").toString());
		returnMap.put("lstSite", getSiteComboFromProject(nselectedprojectcode, userInfo));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getAggregatedDataForCatalogue(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final int nselectedprojectcode = Integer.valueOf(inputMap.get("nselectedprojectcode").toString());
		final String decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
				? "."
				: userInfo.getSdecimaloperator();
		final String opForRegex = escapeForSqlRegexCharClass(decOperator);
		final String opForReplace = stringUtilityFunction.replaceQuote(decOperator);
		final String patternLiteral = "'^[0-9]+([" + opForRegex + "][0-9]+)?$' COLLATE \"default\"";
		final String castExpr = "REPLACE(TRIM(st.sqty COLLATE \"default\"), '" + opForReplace + "', '.')::numeric";

		final String sallchildsitecurrentcodes = getChildSitesFromAllProjects(userInfo);

		// Commented by Gowtham R on nov 12 2025 jira-id:BGSI-191
//		final String strQuery = "WITH unique_subjects AS ( "
//				+ "  SELECT st.nprojecttypecode, st.nsitecode, st.nproductcode, st.nproductcatcode, st.ssubjectid "
//				+ "  FROM samplestoragetransaction st "
//				+ "  JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid " + "    AND bd.nissampleaccesable = "
//				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " AND bd.nisthirdpartysharable = "
//				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "    AND bd.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "    AND bd.nsitecode = "
//				+ userInfo.getNmastersitecode() + "  WHERE COALESCE(st.ssubjectid, '') <> '' "
//				+ "    AND st.nprojecttypecode = " + nselectedprojectcode + " " + "    AND st.nsitecode in "
////				+ userInfo.getNtranssitecode()
//				+ "("+ sallchildsitecurrentcodes +")"
//				+ " " + "    AND st.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "  GROUP BY st.nprojecttypecode, st.nsitecode, st.nproductcode, st.nproductcatcode, st.ssubjectid "
//				+ "), distinct_counts AS ( "
//				+ "  SELECT bp.sprojecttitle, bp.nbioprojectcode, u.nsitecode AS nsitecode, s.ssitename, "
//				+ " p.sproductname, u.nproductcode AS nproductcode,"
//				+ " pc.sproductcatname, u.nproductcatcode AS nproductcatcode, " + " COUNT(*) AS ndistinctsubjects "
//				+ "  FROM unique_subjects u " + "  JOIN product p ON p.nproductcode = u.nproductcode "
//				+ "    AND p.nproductcode > 0 " + "    AND p.nsitecode = " + userInfo.getNmastersitecode()
//				+ "    AND p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "  JOIN productcategory pc ON pc.nproductcatcode = u.nproductcatcode " + "    AND pc.nsitecode = "
//				+ userInfo.getNmastersitecode() + " " + "    AND pc.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "  JOIN bioproject bp ON bp.nbioprojectcode = u.nprojecttypecode " + "    AND bp.nbioprojectcode > 0 "
//				+ "    AND bp.nsitecode = " + userInfo.getNmastersitecode() + "    AND bp.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "  JOIN site s ON u.nsitecode = s.nsitecode " + "    AND s.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode="
//				+ userInfo.getNtranssitecode()
//				+ " join biodataaccessrequestdetails bdard on bdard.nbioprojectcode=bp.nbioprojectcode and bdard.nsitecode="
//				+ userInfo.getNtranssitecode() + " and bdard.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " join biodataaccessrequest bdar on bdar.nbiodataaccessrequestcode=bdard.nbiodataaccessrequestcode  "
//				+ " and bdar.nsitecode=" + userInfo.getNtranssitecode() + " and bdar.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bdar.ntransactionstatus="
//				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
//				+ "  GROUP BY bp.sprojecttitle, bp.nbioprojectcode, u.nsitecode, s.ssitename, "
//				+ " p.sproductname, u.nproductcode, pc.sproductcatname, u.nproductcatcode " + "), samples_agg AS ( "
//				+ "  SELECT bp.sprojecttitle, bp.nbioprojectcode, st.nsitecode, s.ssitename, "
//				+ " p.sproductname, st.nproductcode, pc.sproductcatname, st.nproductcatcode,"
//				+ " COUNT(1) AS ntotalsamplecount, " + " REPLACE(SUM( COALESCE( "
//				+ " CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + patternLiteral + ") " + " THEN " + castExpr
//				+ " " + " ELSE NULL " + " END, 0 ) )::text, '.', '" + decOperator + "') AS stotalqty "
//				+ "  FROM samplestoragetransaction st "
//				+ "  JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid " + "    AND bd.nissampleaccesable = "
//				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " AND bd.nisthirdpartysharable = "
//				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "    AND bd.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "    AND bd.nsitecode = "
//				+ userInfo.getNmastersitecode() + "  JOIN product p ON p.nproductcode = st.nproductcode "
//				+ "    AND p.nproductcode > 0 " + "    AND p.nsitecode = " + userInfo.getNmastersitecode()
//				+ "    AND p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "  JOIN productcategory pc ON pc.nproductcatcode = st.nproductcatcode " + "    AND pc.nsitecode = "
//				+ userInfo.getNmastersitecode() + " " + " AND pc.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "  JOIN bioproject bp ON bp.nbioprojectcode = st.nprojecttypecode "
//				+ "    AND bp.nbioprojectcode > 0 " + "    AND bp.nsitecode = " + userInfo.getNmastersitecode()
//				+ "    AND bp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "    AND st.nprojecttypecode = " + nselectedprojectcode + " "
//				+ "  JOIN site s ON st.nsitecode = s.nsitecode " + "    AND s.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "    AND st.nsitecode in "
////				+ userInfo.getNtranssitecode()
//				+ " ("+ sallchildsitecurrentcodes +") "
////				+ " join biodataaccessrequestdetails bdard on bdard.nbioprojectcode=bp.nbioprojectcode and bdard.nsitecode="
////				+ userInfo.getNtranssitecode() + " and bdard.nstatus="
////				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " join biodataaccessrequest bdar on bdar.nbiodataaccessrequestcode in (select nbiodataaccessrequestcode"
//				+ " from biodataaccessrequestdetails where nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and nbioprojectcode=st.nprojecttypecode group by nbiodataaccessrequestcode)  "
//				+ " and bdar.nsitecode=" + userInfo.getNtranssitecode() + " and bdar.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bdar.ntransactionstatus="
//				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " " + "  WHERE st.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "  GROUP BY bp.sprojecttitle, st.nsitecode, p.sproductname, "
//				+ " bp.nbioprojectcode, s.ssitename, st.nproductcode, pc.sproductcatname, st.nproductcatcode " + ") "
//				+ "SELECT sa.sprojecttitle AS sprojecttitle, sa.nbioprojectcode AS nbioprojectcode, "
//				+ " sa.nsitecode AS nsitecode, sa.ssitename AS ssitename, "
//				+ " sa.sproductcatname AS sproductcatname, sa.nproductcatcode AS nproductcatcode, "
//				+ " sa.sproductname AS sproductname, sa.nproductcode AS nproductcode,"
//				+ " sa.ntotalsamplecount AS ntotalsamplecount, "
//				+ " COALESCE(dc.ndistinctsubjects, 0) AS ntotalsubjectcount, sa.stotalqty AS stotalqty "
//				+ "FROM samples_agg sa " + "LEFT JOIN distinct_counts dc " + "  ON dc.nproductcode = sa.nproductcode "
//				+ " AND dc.nsitecode = sa.nsitecode " + " AND dc.nbioprojectcode = sa.nbioprojectcode "
//				+ " AND dc.nproductcatcode = sa.nproductcatcode "
//				+ "ORDER BY sa.sprojecttitle, sa.nsitecode, sa.sproductcatname, sa.sproductname";

		final String strQuery = "WITH unique_subjects AS ( "
				+ "  SELECT st.nprojecttypecode, st.nsitecode, st.nproductcode, st.nproductcatcode, st.ssubjectid "
				+ "  FROM samplestoragetransaction st "
				+ " JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid AND bd.nissampleaccesable = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " AND bd.nisthirdpartysharable = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "    AND bd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND bd.nsitecode = "
				+ userInfo.getNmastersitecode() + "  WHERE COALESCE(st.ssubjectid, '') <> '' "
				+ " AND st.nprojecttypecode = " + nselectedprojectcode + " AND st.nsitecode in ("
				+ sallchildsitecurrentcodes + ") AND st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  GROUP BY st.nprojecttypecode, st.nsitecode, st.nproductcode, st.nproductcatcode, st.ssubjectid "
				+ "), distinct_counts AS ( SELECT bp.sprojecttitle, bp.nbioprojectcode, u.nsitecode AS nsitecode, "
				+ " p.sproductname, u.nproductcode AS nproductcode, pc.sproductcatname, u.nproductcatcode, "
				+ " COUNT(*) AS ndistinctsubjects  FROM unique_subjects u"
				+ " JOIN product p ON p.nproductcode = u.nproductcode AND p.nproductcode > 0" + " AND p.nsitecode = "
				+ userInfo.getNmastersitecode() + " AND p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN productcategory pc ON pc.nproductcatcode = u.nproductcatcode AND pc.nsitecode = "
				+ userInfo.getNmastersitecode() + " AND pc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN bioproject bp ON bp.nbioprojectcode = u.nprojecttypecode AND bp.nbioprojectcode > 0 "
				+ "    AND bp.nsitecode = " + userInfo.getNmastersitecode() + "    AND bp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " JOIN site s ON u.nsitecode = s.nsitecode AND s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode="
				+ userInfo.getNtranssitecode()
				+ " JOIN biodataaccessrequestdetails bdard on bdard.nbioprojectcode=bp.nbioprojectcode and bdard.nsitecode="
				+ userInfo.getNtranssitecode() + " and bdard.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " JOIN biodataaccessrequest bdar on bdar.nbiodataaccessrequestcode=bdard.nbiodataaccessrequestcode  "
				+ " and bdar.nsitecode=" + userInfo.getNtranssitecode() + " and bdar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bdar.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " GROUP BY bp.sprojecttitle, bp.nbioprojectcode, u.nsitecode, "
				+ " p.sproductname, u.nproductcode, pc.sproductcatname, u.nproductcatcode ), samples_agg AS ( "
				+ " SELECT bp.sprojecttitle, bp.nbioprojectcode, p.sproductname, st.nproductcode,"
				+ " pc.sproductcatname, st.nproductcatcode, COUNT(DISTINCT st.spositionvalue) AS ntotalsamplecount, "
				+ " REPLACE(SUM( COALESCE( CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + patternLiteral
				+ " ) THEN " + castExpr + " ELSE NULL END, 0 ))::text, '.', '" + decOperator + "') AS stotalqty "
				+ "  FROM samplestoragetransaction st "
				+ " JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid AND bd.nissampleaccesable = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " AND bd.nisthirdpartysharable = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "    AND bd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND bd.nsitecode = "
				+ userInfo.getNmastersitecode() + "  JOIN product p ON p.nproductcode = st.nproductcode "
				+ " AND p.nproductcode > 0 AND p.nsitecode = " + userInfo.getNmastersitecode() + "    AND p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " JOIN productcategory pc ON pc.nproductcatcode = st.nproductcatcode AND pc.nsitecode = "
				+ userInfo.getNmastersitecode() + " AND pc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "  JOIN bioproject bp ON bp.nbioprojectcode = st.nprojecttypecode "
				+ "    AND bp.nbioprojectcode > 0 " + "    AND bp.nsitecode = " + userInfo.getNmastersitecode()
				+ "    AND bp.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " AND st.nprojecttypecode = " + nselectedprojectcode
				+ " JOIN biodataaccessrequest bdar on bdar.nsitecode=" + userInfo.getNtranssitecode()
				+ " and bdar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bdar.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " JOIN biodataaccessrequestdetails bdard on bdard.nsitecode=" + userInfo.getNtranssitecode()
				+ " and bdard.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bdar.nbiodataaccessrequestcode=bdard.nbiodataaccessrequestcode"
				+ " and bdard.nbioprojectcode=st.nprojecttypecode and bdard.nproductcode=p.nproductcode"
				+ " WHERE st.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  GROUP BY bp.sprojecttitle, p.sproductname, "
				+ " bp.nbioprojectcode, st.nproductcode, pc.sproductcatname, st.nproductcatcode) "
				+ "SELECT sa.sprojecttitle AS sprojecttitle, sa.nbioprojectcode AS nbioprojectcode, "
				+ " sa.sproductcatname AS sproductcatname, sa.nproductcatcode AS nproductcatcode, "
				+ " sa.sproductname AS sproductname, sa.nproductcode AS nproductcode,"
				+ " sa.ntotalsamplecount AS ntotalsamplecount, "
				+ " COALESCE(dc.ndistinctsubjects, 0) AS ntotalsubjectcount, sa.stotalqty AS stotalqty "
				+ " FROM samples_agg sa LEFT JOIN distinct_counts dc ON dc.nproductcode = sa.nproductcode "
				+ " AND dc.nbioprojectcode = sa.nbioprojectcode " + " AND dc.nproductcatcode = sa.nproductcatcode "
				+ " ORDER BY sa.sprojecttitle, sa.sproductcatname, sa.sproductname";

		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("lstAggregatedData", jdbcTemplate.queryForList(strQuery));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	private static String escapeForSqlRegexCharClass(String s) {
		if (s == null || s.isEmpty()) {
			return "";
		}
		// Escape regex special characters
		// Pattern: matches one of \ ^ $ . | ? * + ( ) [ ] { } -
		return s.replaceAll("([\\\\.^$|?*+()\\[\\]{}-])", "\\\\$1");
	}

	@Override
	public ResponseEntity<Object> getDetailedDataForCatalogue(Map<String, Object> inputMap, UserInfo userInfo) {
		final int nselectedprojectcode = Integer.valueOf(inputMap.get("nselectedprojectcode").toString());
		final int nselectedproductcode = Integer.valueOf(inputMap.get("nselectedproductcode").toString());

		final String strQuery = "SELECT st.nsamplestoragetransactioncode, st.nsamplestoragelocationcode, "
				+ "  st.nsamplestoragemappingcode, st.ninstrumentcode, st.nprojecttypecode, "
				+ "  st.sposition, st.spositionvalue, st.jsondata, st.npositionfilled, "
				+ "  st.nbiosamplereceivingcode, st.sparentsamplecode, st.ncohortno, "
				+ "  st.nproductcatcode, st.nproductcode, st.sqty, st.slocationcode, "
				+ "  st.ssubjectid, st.scasetype, st.ndiagnostictypecode, st.ncontainertypecode, "
				+ "  st.nstoragetypecode, st.dmodifieddate, COALESCE(TO_CHAR(st.dmodifieddate, '"
				+ userInfo.getSsitedate() + "'), '') AS sdmodifieddate, st.nsitecode, s.ssitename, " + "  st.nstatus, "
				+ "  pc.sproductcatname," + "  i.sinstrumentid," + "  coalesce(dt.jsondata->> '"
				+ userInfo.getSlanguagetypecode() + "',dt.jsondata->>'en-US') as sdiagnostictypename,"
				+ "  ct.scontainertype," + "  coalesce(stt.jsondata->> '" + userInfo.getSlanguagetypecode()
				+ "',stt.jsondata->>'en-US') as sstoragetypename," + "  p.sproductname, " + "  bp.sprojecttitle, "
				+ "dc.ndiseasecategorycode, dc.sdiseasecategoryname, d.ndiseasecode, d.sdiseasename "
				+ "FROM samplestoragetransaction st " + "JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid "
				+ "  AND bd.nissampleaccesable = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " AND bd.nisthirdpartysharable = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ "  AND bd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "  AND bd.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " JOIN productcategory pc ON pc.nproductcatcode = st.nproductcatcode AND pc.nsitecode = "
				+ userInfo.getNmastersitecode() + " AND pc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " JOIN instrument i ON i.ninstrumentcode = st.ninstrumentcode AND i.nregionalsitecode = "
				+ userInfo.getNtranssitecode() + " AND i.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " JOIN diagnostictype dt ON dt.ndiagnostictypecode = st.ndiagnostictypecode AND dt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " JOIN containertype ct ON ct.ncontainertypecode = st.ncontainertypecode AND ct.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " JOIN storagetype stt ON stt.nstoragetypecode = st.nstoragetypecode AND stt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " JOIN product p ON p.nproductcode = st.nproductcode AND p.nsitecode = "
				+ userInfo.getNmastersitecode() + " AND p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " JOIN bioproject bp ON bp.nbioprojectcode = st.nprojecttypecode AND bp.nsitecode = "
				+ userInfo.getNmastersitecode() + " AND bp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " JOIN diseasecategory dc ON dc.ndiseasecategorycode = bp.ndiseasecategorycode AND dc.nsitecode = "
				+ userInfo.getNmastersitecode() + " AND dc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " JOIN disease d ON d.ndiseasecode = bp.ndiseasecode AND d.nsitecode = "
				+ userInfo.getNmastersitecode() + " AND d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " JOIN site s ON s.nsitecode = st.nsitecode AND s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "WHERE st.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + "  AND st.nprojecttypecode = " + nselectedprojectcode + " "
				+ "  AND st.nproductcode = " + nselectedproductcode + " " + "  AND st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "ORDER BY st.nsamplestoragetransactioncode, st.ninstrumentcode;";
		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("lstDetailedData", jdbcTemplate.queryForList(strQuery));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createThirdPartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final int nselectedbioprojectcode = Integer.valueOf(inputMap.get("nselectedbioprojectcode").toString());
		final int nselectedreqformtypecode = Integer.valueOf(inputMap.get("nselectedreqformtypecode").toString());

		final String sremarks = inputMap.containsKey("sremarks") && inputMap.get("sremarks").toString() != null
				&& inputMap.get("sremarks").toString() != ""
						? "'" + stringUtilityFunction.replaceQuote(inputMap.get("sremarks").toString()) + "'"
						: null;

		String sQuery = " lock  table lockbiothirdpartyecatalogue "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final String sSeqQuery = "select nsequenceno,stablename from seqnobiobankmanagement where stablename in (N'biothirdpartyecataloguerequest',"
				+ "N'biothirdpartyecatreqhistory')";
		List<SeqNoBioBankManagement> lstSeqBioBank = jdbcTemplate.query(sSeqQuery, new SeqNoBioBankManagement());
		Map<String, Integer> seqNoMap = lstSeqBioBank.stream()
				.collect(Collectors.toMap(SeqNoBioBankManagement::getStablename,
						seqNoTestGroupmanagement -> seqNoTestGroupmanagement.getNsequenceno()));
		int seqNoThirdPartyECatReq = seqNoMap.get("biothirdpartyecataloguerequest") + 1;
		int seqNoThirdPartyECatReqHis = seqNoMap.get("biothirdpartyecatreqhistory") + 1;

		final String strformat = projectDAOSupport.getSeqfnFormat("biothirdpartyecataloguerequest",
				"seqnoformatgeneratorbiobank", 0, 0, userInfo);

		final int nthirdPartyCode = getThirdPartyCode(userInfo);

		if (nthirdPartyCode == -1) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_THIRDPARTYUSERONLYALLOWEDTOCREATEREQUEST",
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

		String strInsertThirdPartyECatalogue = "INSERT INTO public.biothirdpartyecataloguerequest("
				+ "	nthirdpartyecatrequestcode, nthirdpartycode, sformnumber, nreqformtypecode, nbioprojectcode, nreceiversitecode, drequesteddate, ntzrequesteddate,"
				+ " noffsetdrequesteddate, ntransactionstatus, sremarks, sapprovalremarks, dtransactiondate,"
				+ " ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)" + "	VALUES ("
				+ seqNoThirdPartyECatReq + ", " + nthirdPartyCode + ", '" + strformat + "', " + nselectedreqformtypecode
				+ ", " + nselectedbioprojectcode + ", " + userInfo.getNtranssitecode() + ", '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " , " + sremarks + ", null, '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ");";
		String strInsertThirdPartyECatalogueHistory = "INSERT INTO public.biothirdpartyecatreqhistory("
				+ "	nbiothirdpartyecatreqhistorycode, nthirdpartyecatrequestcode, nusercode, nuserrolecode, ndeputyusercode,"
				+ " ndeputyuserrolecode, ntransactionstatus, scomments, dtransactiondate, ntztransactiondate,"
				+ " noffsettransactiondate, nsitecode, nstatus)" + "	VALUES (" + seqNoThirdPartyECatReqHis + ","
				+ seqNoThirdPartyECatReq + "," + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
				+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
				+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "', '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ");";

		String strSeqNoUpdate = "update seqnobiobankmanagement set nsequenceno=" + seqNoThirdPartyECatReq + " where"
				+ " stablename='biothirdpartyecataloguerequest' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		strSeqNoUpdate += "update seqnobiobankmanagement set nsequenceno=" + seqNoThirdPartyECatReqHis + " where"
				+ " stablename='biothirdpartyecatreqhistory' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		inputMap.put("nthirdpartyecatrequestcode", seqNoThirdPartyECatReq);

		String rtnQry = insertThirdPartyECatalogueDetails(inputMap, userInfo);

		jdbcTemplate.execute(
				strInsertThirdPartyECatalogue + strInsertThirdPartyECatalogueHistory + rtnQry + strSeqNoUpdate);

		final List<Object> listBeforeUpdate = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.putAll((Map<String, Object>) getActiveThirdPartyECatalogueRequestForm(inputMap, userInfo).getBody());
		BioThirdPartyECatalogueRequest objBioThirdPartyECatalogueRequest = (BioThirdPartyECatalogueRequest) returnMap
				.get("selectedBioThirdPartyECatalogueRequest");
		listBeforeUpdate.add(objBioThirdPartyECatalogueRequest);
		multilingualIDList.add("IDS_ADDTHIRDPARTYECATALOGUEREQUEST");
		List<BioThirdPartyECatalogueReqDetails> lstBioThirdPartyECatalogueDetails = (List<BioThirdPartyECatalogueReqDetails>) returnMap
				.get("lstBioThirdPartyECatalogueDetails");
		listBeforeUpdate.addAll(lstBioThirdPartyECatalogueDetails);
		lstBioThirdPartyECatalogueDetails.stream()
				.forEach(x -> multilingualIDList.add("IDS_ADDTHIRDPARTYECATALOGUEREQUESTSAMPLE"));
		auditUtilityFunction.fnInsertAuditAction(listBeforeUpdate, 1, null, multilingualIDList, userInfo);

		return getThirdPartyECatalogueByFilterSubmit(inputMap, userInfo);
	}

	@SuppressWarnings("unchecked")
	public String insertThirdPartyECatalogueDetails(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final int nthirdpartyecatrequestcode = Integer.valueOf(inputMap.get("nthirdpartyecatrequestcode").toString());
		final int nselectedbioprojectcode = Integer.valueOf(inputMap.get("nselectedbioprojectcode").toString());

		final String sSeqQuery = "select nsequenceno,stablename from seqnobiobankmanagement where stablename in (N'biothirdpartyecataloguereqdetails'"
				+ ")";
		List<SeqNoBioBankManagement> lstSeqBioBank = jdbcTemplate.query(sSeqQuery, new SeqNoBioBankManagement());
		Map<String, Integer> seqNoMap = lstSeqBioBank.stream()
				.collect(Collectors.toMap(SeqNoBioBankManagement::getStablename,
						seqNoTestGroupmanagement -> seqNoTestGroupmanagement.getNsequenceno()));
		int seqNoThirdPartyECatDetails = seqNoMap.get("biothirdpartyecataloguereqdetails");

		String strThirdPartyECatalogueDetails = "INSERT INTO public.biothirdpartyecataloguereqdetails("
				+ "	nthirdpartyecatreqdetailcode, nthirdpartyecatrequestcode, nbioprojectcode, nproductcode,"
				+ " sparentsamplecode, nreqnoofsamples, sreqminvolume, naccnoofsamples, saccminvolume,"
				+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)" + "	VALUES ";

		List<Map<String, Object>> requestProductList = (List<Map<String, Object>>) inputMap.get("requestProductList");

		for (Map<String, Object> reqProduct : requestProductList) {
			seqNoThirdPartyECatDetails++;
			int nproductcode = Integer.valueOf(reqProduct.get("nproductcode").toString());
			int nreqnoofsamples = Integer.valueOf(reqProduct.get("nreqnoofsamples").toString());
			String sparentsamplecode = reqProduct.containsKey("sparentsamplecode")
					&& reqProduct.get("sparentsamplecode") != null && reqProduct.get("sparentsamplecode") != ""
					&& reqProduct.get("sparentsamplecode") != "-"
							? "'" + stringUtilityFunction.replaceQuote(reqProduct.get("sparentsamplecode").toString())
									+ "'"
							: null;
			String sreqminvolume = "'" + stringUtilityFunction.replaceQuote(reqProduct.get("sreqminvolume").toString())
					+ "'";
			strThirdPartyECatalogueDetails += "(" + seqNoThirdPartyECatDetails + ", " + nthirdpartyecatrequestcode
					+ ", " + nselectedbioprojectcode + ", " + nproductcode + ", " + sparentsamplecode + ", "
					+ nreqnoofsamples + ", " + sreqminvolume + ", null, null," + " '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "),";
		}

		strThirdPartyECatalogueDetails = strThirdPartyECatalogueDetails.substring(0,
				strThirdPartyECatalogueDetails.length() - 1) + ";";

		String strSeqNoUpdate = "update seqnobiobankmanagement set nsequenceno=" + seqNoThirdPartyECatDetails + " where"
				+ " stablename='biothirdpartyecataloguereqdetails' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		return (strThirdPartyECatalogueDetails + strSeqNoUpdate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> sendThirdPartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		returnMap.putAll((Map<String, Object>) getActiveThirdPartyECatalogueRequestForm(inputMap, userInfo).getBody());

		BioThirdPartyECatalogueRequest objBioThirdPartyECatalogueRequest = (BioThirdPartyECatalogueRequest) returnMap
				.get("selectedBioThirdPartyECatalogueRequest");
		if (objBioThirdPartyECatalogueRequest != null && objBioThirdPartyECatalogueRequest
				.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			listBeforeUpdate.add(objBioThirdPartyECatalogueRequest);

			final List<BioThirdPartyECatalogueReqDetails> lstBioThirdPartyECatalogueDetails = (List<BioThirdPartyECatalogueReqDetails>) returnMap
					.get("lstBioThirdPartyECatalogueDetails");
			if (lstBioThirdPartyECatalogueDetails != null && !lstBioThirdPartyECatalogueDetails.isEmpty()) {

				final String sQuery = " lock  table lockbiothirdpartyecatalogue "
						+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				jdbcTemplate.execute(sQuery);

				final String sSeqQuery = "select nsequenceno,stablename from seqnobiobankmanagement where stablename in ("
						+ "N'biothirdpartyecatreqhistory',N'bioecataloguereqapproval',N'bioecataloguerequestdetails',N'bioecataloguereqapprovalhistory')";
				List<SeqNoBioBankManagement> lstSeqBioBank = jdbcTemplate.query(sSeqQuery,
						new SeqNoBioBankManagement());
				Map<String, Integer> seqNoMap = lstSeqBioBank.stream()
						.collect(Collectors.toMap(SeqNoBioBankManagement::getStablename,
								seqNoTestGroupmanagement -> seqNoTestGroupmanagement.getNsequenceno()));
				int seqNoThirdPartyECatalogueHistory = seqNoMap.get("biothirdpartyecatreqhistory") + 1;
				int seqNoEcatReqApproval = seqNoMap.get("bioecataloguereqapproval") + 1;
				int seqNoEcatReqApprovalHis = seqNoMap.get("bioecataloguereqapprovalhistory") + 1;
				int seqNoEcatReqAppDetails = seqNoMap.get("bioecataloguerequestdetails");

				final String updQry = "Update biothirdpartyecataloguerequest set ntransactionstatus = "
						+ Enumeration.TransactionStatus.REQUESTED.gettransactionstatus() + ", drequesteddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', noffsetdrequesteddate = "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", ntzrequesteddate = " + userInfo.getNtimezonecode() + ", dtransactiondate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', noffsetdtransactiondate = "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", ntztransactiondate = " + userInfo.getNtimezonecode()
						+ " where nthirdpartyecatrequestcode = "
						+ objBioThirdPartyECatalogueRequest.getNthirdpartyecatrequestcode() + " and nsitecode = "
						+ userInfo.getNtranssitecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

				String strInsert = "INSERT INTO public.biothirdpartyecatreqhistory("
						+ "nbiothirdpartyecatreqhistorycode, nthirdpartyecatrequestcode, nusercode, nuserrolecode, ndeputyusercode,"
						+ " ndeputyuserrolecode, ntransactionstatus, scomments, dtransactiondate, ntztransactiondate,"
						+ " noffsettransactiondate, nsitecode, nstatus)" + "	VALUES ("
						+ seqNoThirdPartyECatalogueHistory + ","
						+ objBioThirdPartyECatalogueRequest.getNthirdpartyecatrequestcode() + ","
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						+ Enumeration.TransactionStatus.REQUESTED.gettransactionstatus() + ", '"
						+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "', '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

				strInsert += "update seqnobiobankmanagement set nsequenceno=" + seqNoThirdPartyECatalogueHistory
						+ " where" + " stablename='biothirdpartyecatreqhistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				String strInsertBioEcatReqApp = "INSERT INTO public.bioecataloguereqapproval("
						+ "	necatrequestreqapprovalcode, ntransfertype, norginsitecode, norginthirdpartycode, sformnumber, nreqformtypecode, "
						+ "drequesteddate, ntzrequesteddate, noffsetdrequesteddate, ntransactionstatus, sapprovalremarks,"
						+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)"
						+ "	VALUES (" + seqNoEcatReqApproval + ", "
						+ Enumeration.TransferType.THIRDPARTY.getntransfertype() + ", " + userInfo.getNtranssitecode()
						+ ", " + objBioThirdPartyECatalogueRequest.getNthirdpartycode() + ", '"
						+ objBioThirdPartyECatalogueRequest.getSformnumber() + "', "
						+ objBioThirdPartyECatalogueRequest.getNreqformtypecode() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ Enumeration.TransactionStatus.REQUESTED.gettransactionstatus() + ", null, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ objBioThirdPartyECatalogueRequest.getNreceiversitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
				strInsertBioEcatReqApp += "update seqnobiobankmanagement set nsequenceno=" + seqNoEcatReqApproval
						+ " where" + " stablename='bioecataloguereqapproval' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				String strInsertBioEcatReqAppHis = "INSERT INTO public.bioecataloguereqapprovalhistory("
						+ "	nbioecataloguereqapprovalhistorycode, necatrequestreqapprovalcode, nusercode, nuserrolecode, ndeputyusercode,"
						+ " ndeputyuserrolecode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
						+ " noffsetdtransactiondate, nsitecode, nstatus)" + "	VALUES (" + seqNoEcatReqApprovalHis
						+ "," + seqNoEcatReqApproval + "," + userInfo.getNusercode() + ", " + userInfo.getNuserrole()
						+ ", " + userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						+ Enumeration.TransactionStatus.REQUESTED.gettransactionstatus() + "," + " '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ objBioThirdPartyECatalogueRequest.getNreceiversitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

				strInsertBioEcatReqAppHis += "update seqnobiobankmanagement set nsequenceno=" + seqNoEcatReqApprovalHis
						+ " where" + " stablename='bioecataloguereqapprovalhistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				String strBioECatalogueRequestDetails = "INSERT INTO public.bioecataloguerequestdetails("
						+ "	necateloguerequestdetailcode, necatrequestreqapprovalcode, nbioprojectcode, nproductcode,"
						+ " sparentsamplecode, sreqminvolume, nreqnoofsamples, naccnoofsamples, saccminvolume,"
						+ " sremarks, dtransactiondate, ntztransactiondate,"
						+ " noffsetdtransactiondate, nsitecode, nstatus)" + "	VALUES";

				for (BioThirdPartyECatalogueReqDetails objBioThirdPartyECatalogueDetail : lstBioThirdPartyECatalogueDetails) {
					seqNoEcatReqAppDetails++;
					int nproductcode = objBioThirdPartyECatalogueDetail.getNproductcode();
					int nreqnoofsamples = objBioThirdPartyECatalogueDetail.getNreqnoofsamples();
					String sparentsamplecode = objBioThirdPartyECatalogueDetail.getSparentsamplecode() != null
							&& !objBioThirdPartyECatalogueDetail.getSparentsamplecode().isEmpty()
							&& !objBioThirdPartyECatalogueDetail.getSparentsamplecode().equalsIgnoreCase("-")
									? "'" + stringUtilityFunction
											.replaceQuote(objBioThirdPartyECatalogueDetail.getSparentsamplecode()) + "'"
									: null;
					String sreqminvolume = "'"
							+ stringUtilityFunction.replaceQuote(objBioThirdPartyECatalogueDetail.getSreqminvolume())
							+ "'";
					strBioECatalogueRequestDetails += "(" + seqNoEcatReqAppDetails + ", " + seqNoEcatReqApproval + ", "
							+ objBioThirdPartyECatalogueDetail.getNbioprojectcode() + ", " + nproductcode + ", "
							+ sparentsamplecode + ", " + sreqminvolume + ", " + nreqnoofsamples + ", null, null,"
							+ " null, " + "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ userInfo.getNtimezonecode() + ", "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ objBioThirdPartyECatalogueRequest.getNreceiversitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
				}

				strBioECatalogueRequestDetails = strBioECatalogueRequestDetails.substring(0,
						strBioECatalogueRequestDetails.length() - 1) + ";";

				String strSeqNoUpdate = "update seqnobiobankmanagement set nsequenceno=" + seqNoEcatReqAppDetails
						+ " where" + " stablename='bioecataloguerequestdetails' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				jdbcTemplate.execute(updQry + strInsert + strInsertBioEcatReqApp + strInsertBioEcatReqAppHis
						+ strBioECatalogueRequestDetails + strSeqNoUpdate);

				outputMap.putAll(
						(Map<String, Object>) getActiveThirdPartyECatalogueRequestForm(inputMap, userInfo).getBody());

				final List<String> multilingualIDList = new ArrayList<>();
				listAfterUpdate.add(outputMap.get("selectedBioThirdPartyECatalogueRequest"));

				multilingualIDList.add("IDS_SENDTHIRDPARTYECATALOGUEREQUEST");

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				return new ResponseEntity<>(outputMap, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADDSAMPLESINFORMTOREQUEST",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTSTATUSRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> cancelThirdPartyECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		returnMap.putAll((Map<String, Object>) getActiveThirdPartyECatalogueRequestForm(inputMap, userInfo).getBody());

		BioThirdPartyECatalogueRequest objBioThirdPartyECatalogueRequest = (BioThirdPartyECatalogueRequest) returnMap
				.get("selectedBioThirdPartyECatalogueRequest");
		if (objBioThirdPartyECatalogueRequest != null && (objBioThirdPartyECatalogueRequest
				.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| objBioThirdPartyECatalogueRequest.getNtransactionstatus() == Enumeration.TransactionStatus.REQUESTED
						.gettransactionstatus())) {

			listBeforeUpdate.add(objBioThirdPartyECatalogueRequest);

			final String sQuery = " lock  table lockbiothirdpartyecatalogue "
					+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			final String ecatReqAppStr = "select necatrequestreqapprovalcode " + "  from bioecataloguereqapproval "
					+ " where sformnumber = N'" + objBioThirdPartyECatalogueRequest.getSformnumber() + "' "
					+ "   and nsitecode   = " + objBioThirdPartyECatalogueRequest.getNreceiversitecode() + " "
					+ "   and norginsitecode = " + userInfo.getNtranssitecode() + " " + "   and nstatus     = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final Integer necatrequestreqapprovalcode = (Integer) jdbcUtilityTemplateFunction
					.queryForObject(ecatReqAppStr, Integer.class, jdbcTemplate);

			int seqNoThirdPartyECatalogueHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename in (N'biothirdpartyecatreqhistory') and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoThirdPartyECatalogueHistory = seqNoThirdPartyECatalogueHistory + 1;

			int seqNoEcatReqApprovalHis = jdbcTemplate.queryForObject("select nsequenceno from seqnobiobankmanagement "
					+ " where stablename in (N'bioecataloguereqapprovalhistory') " + "   and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);

			final String updQry = "Update biothirdpartyecataloguerequest set ntransactionstatus = "
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", drequesteddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', noffsetdrequesteddate = "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", ntzrequesteddate = "
					+ userInfo.getNtimezonecode() + ", dtransactiondate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', noffsetdtransactiondate = "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ ", ntztransactiondate = " + userInfo.getNtimezonecode() + " where nthirdpartyecatrequestcode = "
					+ objBioThirdPartyECatalogueRequest.getNthirdpartyecatrequestcode() + " and nsitecode = "
					+ userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

			String strInsert = "INSERT INTO public.biothirdpartyecatreqhistory("
					+ "	nbiothirdpartyecatreqhistorycode, nthirdpartyecatrequestcode, nusercode, nuserrolecode, ndeputyusercode,"
					+ " ndeputyuserrolecode, ntransactionstatus, scomments, dtransactiondate, ntztransactiondate,"
					+ " noffsettransactiondate, nsitecode, nstatus)" + "	VALUES (" + seqNoThirdPartyECatalogueHistory
					+ "," + objBioThirdPartyECatalogueRequest.getNthirdpartyecatrequestcode() + ","
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", "
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", '"
					+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "', '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ");";

			strInsert += "update seqnobiobankmanagement set nsequenceno=" + seqNoThirdPartyECatalogueHistory + " where"
					+ " stablename='biothirdpartyecatreqhistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			if (necatrequestreqapprovalcode != null) {
				String updApproval = "Update bioecataloguereqapproval set ntransactionstatus = "
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", dtransactiondate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ", noffsetdtransactiondate = "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", ntztransactiondate = " + userInfo.getNtimezonecode()
						+ " where necatrequestreqapprovalcode = " + (int) necatrequestreqapprovalcode
						+ "   and nsitecode = " + objBioThirdPartyECatalogueRequest.getNreceiversitecode()
						+ "   and norginsitecode = " + userInfo.getNtranssitecode() + "   and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				seqNoEcatReqApprovalHis++;
				String strInsertBioEcatReqAppHis = "INSERT INTO public.bioecataloguereqapprovalhistory("
						+ " nbioecataloguereqapprovalhistorycode, necatrequestreqapprovalcode, nusercode, nuserrolecode, ndeputyusercode,"
						+ " ndeputyuserrolecode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
						+ " noffsetdtransactiondate, nsitecode, nstatus)" + " VALUES (" + seqNoEcatReqApprovalHis + ","
						+ necatrequestreqapprovalcode + "," + userInfo.getNusercode() + ", " + userInfo.getNuserrole()
						+ ", " + userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", " + "'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ objBioThirdPartyECatalogueRequest.getNreceiversitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");" +

						"update seqnobiobankmanagement set nsequenceno=" + seqNoEcatReqApprovalHis
						+ " where stablename='bioecataloguereqapprovalhistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				jdbcTemplate.execute(updQry + strInsert + updApproval + strInsertBioEcatReqAppHis);
			} else {
				jdbcTemplate.execute(updQry + strInsert);
			}

			outputMap.putAll(
					(Map<String, Object>) getActiveThirdPartyECatalogueRequestForm(inputMap, userInfo).getBody());

			final List<String> multilingualIDList = new ArrayList<>();
			listAfterUpdate.add(outputMap.get("selectedBioThirdPartyECatalogueRequest"));

			multilingualIDList.add("IDS_CANCELTHIRDPARTYECATALOGUEREQUEST");

			auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
					userInfo);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTORREQUESTEDSTATUSRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	public BioThirdPartyECatalogueRequest getActiveThirdPartyECatalogueRequestForValidation(
			final int nthirdpartyecatrequestcode, UserInfo userInfo) throws Exception {
		final String sampleQry = "select "
				+ "bc.nthirdpartyecatrequestcode, bc.nthirdpartycode, bc.sformnumber, bc.nreqformtypecode, bc.nreceiversitecode, rs.ssitename as sreceivingsitename, "
				+ "bc.drequesteddate, bc.ntzrequesteddate, tz1.stimezoneid as stzrequesteddate, "
				+ "COALESCE(TO_CHAR(bc.drequesteddate,'" + userInfo.getSsitedate() + "'), '') as srequesteddate, "
				+ "bc.sremarks, bc.sapprovalremarks, bc.ntransactionstatus,cm.scolorhexcode, "
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus, "
				+ "bc.dtransactiondate, bc.ntztransactiondate, tz2.stimezoneid as stztransactiondate, "
				+ "COALESCE(TO_CHAR(bc.dtransactiondate,'" + userInfo.getSsitedate() + "'), '') as stransactiondate, "
				+ "p.sprojecttitle as sprojecttitles, p.nbioprojectcode "
				+ "from biothirdpartyecataloguerequest bc, site rs, transactionstatus ts, timezone tz1, timezone tz2, "
				+ "bioproject p, formwisestatuscolor fwsc, colormaster cm " + "where bc.nthirdpartyecatrequestcode = "
				+ nthirdpartyecatrequestcode + " and bc.nreceiversitecode = rs.nsitecode and rs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bc.ntransactionstatus = ts.ntranscode and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bc.ntzrequesteddate = tz1.ntimezonecode and tz1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bc.ntztransactiondate = tz2.ntimezonecode and tz2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bc.nbioprojectcode = p.nbioprojectcode and p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and bc.nsitecode = "
				+ userInfo.getNtranssitecode() + " and bc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and fwsc.ntranscode = ts.ntranscode and fwsc.nformcode = " + userInfo.getNformcode()
				+ " and fwsc.nsitecode = " + userInfo.getNmastersitecode() + " and fwsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and fwsc.ncolorcode=cm.ncolorcode and cm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		final BioThirdPartyECatalogueRequest objBioThirdPartyECatalogueRequest = (BioThirdPartyECatalogueRequest) jdbcUtilityTemplateFunction
				.queryForObject(sampleQry, BioThirdPartyECatalogueRequest.class, jdbcTemplate);

		return objBioThirdPartyECatalogueRequest;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createThirdPartyECatalogueRequestSample(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		final int nthirdpartyecatrequestcode = Integer.valueOf(inputMap.get("nthirdpartyecatrequestcode").toString());
		BioThirdPartyECatalogueRequest objBioThirdPartyECatalogue = getActiveThirdPartyECatalogueRequestForValidation(
				nthirdpartyecatrequestcode, userInfo);
		String schildSiteCode = getChildSitesFromAllProjects(userInfo);
		schildSiteCode = schildSiteCode.contains(",")
				? schildSiteCode.substring(0, schildSiteCode.lastIndexOf(",")) + ","
						+ objBioThirdPartyECatalogue.getNreceiversitecode()
				: "" + objBioThirdPartyECatalogue.getNreceiversitecode() + "";

		if (objBioThirdPartyECatalogue != null && objBioThirdPartyECatalogue
				.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			final int nproductcode = Integer.valueOf(inputMap.get("nproductcode").toString());
			String sparentsamplecode = inputMap.containsKey("sparentsamplecode")
					&& inputMap.get("sparentsamplecode") != null && inputMap.get("sparentsamplecode") != ""
					&& inputMap.get("sparentsamplecode") != "-"
							? "'" + stringUtilityFunction.replaceQuote(inputMap.get("sparentsamplecode").toString())
									+ "'"
							: null;
			final int nreqformtypecode = objBioThirdPartyECatalogue.getNreqformtypecode();

			if (nreqformtypecode == Enumeration.RequestFormType.BIOSAMPLEBASED.getRequestFormType()) {

				boolean isProductAvailable = isProductAvailableInForm(nthirdpartyecatrequestcode, nreqformtypecode,
						nproductcode, sparentsamplecode, userInfo);

				if (!isProductAvailable) {

					final String decOperator = (userInfo.getSdecimaloperator() == null
							|| userInfo.getSdecimaloperator().isEmpty()) ? "." : userInfo.getSdecimaloperator();
					final String opForRegex = escapeForSqlRegexCharClass(decOperator);
					final String opForReplace = stringUtilityFunction.replaceQuote(decOperator);
					final String patternLiteral = "'^[0-9]+([" + opForRegex + "][0-9]+)?$' COLLATE \"default\"";

					final String castExprSt = "REPLACE(TRIM(st.sqty COLLATE \"default\"), '" + opForReplace
							+ "', '.')::numeric";

					final String sreqminvolumeRaw = stringUtilityFunction
							.replaceQuote(inputMap.get("sreqminvolume").toString()).trim();
					final String sreqminvolumeTxt = "'" + sreqminvolumeRaw + "'"; // <-- QUOTED as text literal

					final String castExprB = "REPLACE(TRIM(" + sreqminvolumeTxt + " COLLATE \"default\"), '"
							+ opForReplace + "', '.')::numeric";

					final String parentSampleConditionStr = (sparentsamplecode != null
							&& !sparentsamplecode.toString().trim().isEmpty())
									? " AND st.sparentsamplecode = " + sparentsamplecode + ""
									: "";

					String availableCountQry = "SELECT COUNT(st.nsamplestoragetransactioncode) "
							+ "FROM samplestoragetransaction st "
							+ "JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid "
							+ "  AND bd.nissampleaccesable = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ " AND bd.nisthirdpartysharable = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "  AND bd.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "  AND bd.nsitecode = " + userInfo.getNmastersitecode() + " WHERE "
							+ "  st.nprojecttypecode = " + objBioThirdPartyECatalogue.getNbioprojectcode()
							+ " AND st.nproductcode  = " + nproductcode + " AND st.nsitecode in (" + schildSiteCode
							+ ") AND (CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + patternLiteral + ") THEN "
							+ castExprSt + " ELSE NULL END) " + "     >= (CASE WHEN (TRIM(" + sreqminvolumeTxt
							+ " COLLATE \"default\")) ~ (" + patternLiteral + ") THEN " + castExprB + " ELSE NULL END) "
							+ " AND st.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ parentSampleConditionStr + ";";

					int nreqnoofsamples = Integer.valueOf(inputMap.get("nreqnoofsamples").toString().trim());

					String sreqminvolumewithquotes = "'"
							+ stringUtilityFunction.replaceQuote(inputMap.get("sreqminvolume").toString().trim()) + "'";

					final int navailablecount = jdbcTemplate.queryForObject(availableCountQry, Integer.class);
					if (navailablecount >= nreqnoofsamples) {

						final String sQuery = " lock  table lockbiothirdpartyecatalogue "
								+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
						jdbcTemplate.execute(sQuery);

						int seqNoThirdPartyECatDetails = jdbcTemplate.queryForObject(
								"select nsequenceno from seqnobiobankmanagement where stablename in (N'biothirdpartyecataloguereqdetails') and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
								Integer.class);
						seqNoThirdPartyECatDetails = seqNoThirdPartyECatDetails + 1;

						String strThirdPartyECatalogueDetails = "INSERT INTO public.biothirdpartyecataloguereqdetails("
								+ "	nthirdpartyecatreqdetailcode, nthirdpartyecatrequestcode, nbioprojectcode, nproductcode,"
								+ " sparentsamplecode, sreqminvolume, nreqnoofsamples, naccnoofsamples, saccminvolume, "
								+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)"
								+ "	VALUES (" + seqNoThirdPartyECatDetails + ", " + nthirdpartyecatrequestcode + ", "
								+ objBioThirdPartyECatalogue.getNbioprojectcode() + ", " + nproductcode + ","
								+ sparentsamplecode + ", " + sreqminvolumewithquotes + ", " + nreqnoofsamples
								+ ", null, null," + " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ userInfo.getNtimezonecode() + ", "
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
								+ userInfo.getNtranssitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

						String strSeqNoUpdate = "update seqnobiobankmanagement set nsequenceno="
								+ seqNoThirdPartyECatDetails + " where"
								+ " stablename='biothirdpartyecataloguereqdetails' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						jdbcTemplate.execute(strThirdPartyECatalogueDetails + strSeqNoUpdate);
						Map<String, Object> outputMap = new HashMap<String, Object>();
						outputMap.putAll(
								(Map<String, Object>) getActiveThirdPartyECatalogueRequestForm(inputMap, userInfo)
										.getBody());
						outputMap.putAll(
								(Map<String, Object>) getProductComboDataForSampleAdd(inputMap, userInfo).getBody());

						final List<Object> listBeforeUpdate = new ArrayList<>();
						final List<String> multilingualIDList = new ArrayList<>();
						inputMap.put("nthirdpartyecatreqdetailcode", seqNoThirdPartyECatDetails);
						BioThirdPartyECatalogueReqDetails objBioThirdPartyECatalogueDetails = (BioThirdPartyECatalogueReqDetails) getActiveSampleDetail(
								inputMap, userInfo).getBody().get("selectedBioThirdPartyECatalogueDetails");

						listBeforeUpdate.add(objBioThirdPartyECatalogueDetails);
						multilingualIDList.add("IDS_ADDTHIRDPARTYECATALOGUEREQUESTSAMPLE");
						auditUtilityFunction.fnInsertAuditAction(listBeforeUpdate, 1, null, multilingualIDList,
								userInfo);

						return new ResponseEntity<>(outputMap, HttpStatus.OK);

					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_REQUESTEDQUANTITYMORETHANAVAILABLEQUANTITY",
										userInfo.getSlanguagefilename()),
								HttpStatus.CONFLICT);
					}
				} else {
					if (Enumeration.RequestFormType.BIOSAMPLEBASED.getRequestFormType() == nreqformtypecode) {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_BIOSAMPLETYPEALREADYAVAILABLE",
										userInfo.getSlanguagefilename()),
								HttpStatus.CONFLICT);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								"IDS_PARENTSAMPLECODEALREADYAVAILABLEFORGIVENBIOSAMPLETYPE",
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						"IDS_PARENTSAMPLECODECANNOTBEEMPTYFORPARENTSAMPLEBASEDFORM", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTSTATUSRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}

	}

	@Override
	public ResponseEntity<Object> updateThirdPartyECatalogueRequestSample(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		final int nthirdpartyecatrequestcode = Integer.valueOf(inputMap.get("nthirdpartyecatrequestcode").toString());
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		BioThirdPartyECatalogueRequest objBioThirdPartyECatalogue = getActiveThirdPartyECatalogueRequestForValidation(
				nthirdpartyecatrequestcode, userInfo);

		String schildSiteCode = getChildSitesFromAllProjects(userInfo);
		schildSiteCode = schildSiteCode.contains(",")
				? schildSiteCode.substring(0, schildSiteCode.lastIndexOf(",")) + ","
						+ objBioThirdPartyECatalogue.getNreceiversitecode()
				: "" + objBioThirdPartyECatalogue.getNreceiversitecode() + "";

		if (objBioThirdPartyECatalogue != null && objBioThirdPartyECatalogue
				.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			final int nreqformtypecode = objBioThirdPartyECatalogue.getNreqformtypecode();

			BioThirdPartyECatalogueReqDetails objBioThirdPartyECatalogueDetails = (BioThirdPartyECatalogueReqDetails) getActiveSampleDetail(
					inputMap, userInfo).getBody().get("selectedBioThirdPartyECatalogueDetails");

			if (objBioThirdPartyECatalogueDetails != null) {

				listBeforeUpdate.add(objBioThirdPartyECatalogueDetails);

				String sparentsamplecode = inputMap.containsKey("sparentsamplecode")
						&& inputMap.get("sparentsamplecode") != null
								? "'" + stringUtilityFunction.replaceQuote(inputMap.get("sparentsamplecode").toString())
										+ "'"
								: null;
				if (nreqformtypecode == Enumeration.RequestFormType.BIOSAMPLEBASED.getRequestFormType()) {

					final String decOperator = (userInfo.getSdecimaloperator() == null
							|| userInfo.getSdecimaloperator().isEmpty()) ? "." : userInfo.getSdecimaloperator();
					final String opForRegex = escapeForSqlRegexCharClass(decOperator);
					final String opForReplace = stringUtilityFunction.replaceQuote(decOperator);
					final String patternLiteral = "'^[0-9]+([" + opForRegex + "][0-9]+)?$' COLLATE \"default\"";
					final String castExprSt = "REPLACE(TRIM(st.sqty COLLATE \"default\"), '" + opForReplace
							+ "', '.')::numeric";
					int nreqnoofsamples = Integer.valueOf(inputMap.get("nreqnoofsamples").toString().trim());
					String sreqminvolumewithquotes = "'"
							+ stringUtilityFunction.replaceQuote(inputMap.get("sreqminvolume").toString().trim()) + "'";
					final String castExprB = "REPLACE(TRIM(" + sreqminvolumewithquotes + " COLLATE \"default\"), '"
							+ opForReplace + "', '.')::numeric";
					String parentSampleConditionStr = sparentsamplecode != null
							? " and st.sparentsamplecode = " + sparentsamplecode
							: "";

					String availableCountQry = "SELECT COUNT(st.nsamplestoragetransactioncode) "
							+ "FROM samplestoragetransaction st "
							+ "JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid "
							+ "  AND bd.nissampleaccesable = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus()
							+ " AND bd.nisthirdpartysharable = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "  AND bd.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "  AND bd.nsitecode = " + userInfo.getNmastersitecode() + " WHERE st.nprojecttypecode = "
							+ objBioThirdPartyECatalogue.getNbioprojectcode() + "  AND st.nproductcode     = "
							+ objBioThirdPartyECatalogueDetails.getNproductcode() + "  AND st.nsitecode in ("
							+ schildSiteCode + ")  AND (CASE " + "         WHEN TRIM(st.sqty COLLATE \"default\") ~ ("
							+ patternLiteral + ") " + "         THEN " + castExprSt + " " + "         ELSE NULL "
							+ "       END) >= (CASE " + "         WHEN TRIM(" + sreqminvolumewithquotes
							+ " COLLATE \"default\") ~ (" + patternLiteral + ") " + "         THEN " + castExprB + " "
							+ "         ELSE NULL " + "       END) " + "  AND st.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + parentSampleConditionStr
							+ ";";

					final int navailablecount = jdbcTemplate.queryForObject(availableCountQry, Integer.class);

					if (navailablecount >= nreqnoofsamples) {

						final String updQry = "Update biothirdpartyecataloguereqdetails set sreqminvolume = "
								+ sreqminvolumewithquotes + ", nreqnoofsamples = " + nreqnoofsamples
								+ ", sparentsamplecode = " + sparentsamplecode + ", dtransactiondate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', noffsetdtransactiondate = "
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
								+ ", ntztransactiondate = " + userInfo.getNtimezonecode()
								+ " where nthirdpartyecatreqdetailcode = "
								+ objBioThirdPartyECatalogueDetails.getNthirdpartyecatreqdetailcode()
								+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

						jdbcTemplate.execute(updQry);
						Map<String, Object> outputMap = new HashMap<String, Object>();
						outputMap.putAll((Map<String, Object>) getActiveSampleDetail(inputMap, userInfo).getBody());

						final List<String> multilingualIDList = new ArrayList<>();
						listAfterUpdate.add(outputMap.get("selectedBioThirdPartyECatalogueDetails"));

						multilingualIDList.add("IDS_EDITTHIRDPARTYECATALOGUEREQUESTSAMPLE");

						auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate,
								multilingualIDList, userInfo);
						return new ResponseEntity<>(outputMap, HttpStatus.OK);

					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage("IDS_REQUESTEDQUANTITYMORETHANAVAILABLEQUANTITY",
										userInfo.getSlanguagefilename()),
								HttpStatus.CONFLICT);
					}

				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							"IDS_PARENTSAMPLECODECANNOTBEEMPTYFORPARENTSAMPLEBASEDFORM",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTSTATUSRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	@Override
	public ResponseEntity<Object> deleteThirdPartyECatalogueRequestSample(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		final int nthirdpartyecatrequestcode = Integer.valueOf(inputMap.get("nthirdpartyecatrequestcode").toString());

		BioThirdPartyECatalogueRequest objBioThirdPartyECatalogue = getActiveThirdPartyECatalogueRequestForValidation(
				nthirdpartyecatrequestcode, userInfo);
		if (objBioThirdPartyECatalogue != null && objBioThirdPartyECatalogue
				.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			BioThirdPartyECatalogueReqDetails objBioThirdPartyECatalogueDetails = (BioThirdPartyECatalogueReqDetails) getActiveSampleDetail(
					inputMap, userInfo).getBody().get("selectedBioThirdPartyECatalogueDetails");

			if (objBioThirdPartyECatalogueDetails != null) {

				final String updQry = "Update biothirdpartyecataloguereqdetails set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dtransactiondate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', noffsetdtransactiondate = "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", ntztransactiondate = " + userInfo.getNtimezonecode()
						+ " where nthirdpartyecatreqdetailcode = "
						+ objBioThirdPartyECatalogueDetails.getNthirdpartyecatreqdetailcode() + " and nsitecode = "
						+ userInfo.getNtranssitecode() + "; ";

				jdbcTemplate.execute(updQry);
				Map<String, Object> outputMap = new HashMap<String, Object>();
				outputMap.putAll(
						(Map<String, Object>) getActiveThirdPartyECatalogueRequestForm(inputMap, userInfo).getBody());

				final List<Object> listAfterUpdate = new ArrayList<>();
				listAfterUpdate.add(objBioThirdPartyECatalogueDetails);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_DELETETHIRDPARTYECATALOGUEREQUESTSAMPLE");

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 1, null, multilingualIDList, userInfo);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ALREADYDELETED", userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTSTATUSRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	public Boolean isProductAvailableInForm(final int nthirdpartyecatrequestcode, final int nreqformtypecode,
			final int nproductcode, final String sparentsamplecode, final UserInfo userInfo) throws Exception {
		String strQuery = "";

		if (Enumeration.RequestFormType.BIOSAMPLEBASED.getRequestFormType() == nreqformtypecode) {
			strQuery = "SELECT nproductcode FROM biothirdpartyecataloguereqdetails where nthirdpartyecatrequestcode = "
					+ nthirdpartyecatrequestcode + " and nproductcode = " + nproductcode + " and nsitecode = "
					+ userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		} else if (Enumeration.RequestFormType.PARENTSAMPLEBASED.getRequestFormType() == nreqformtypecode
				&& sparentsamplecode != null) {
			strQuery = "SELECT sparentsamplecode FROM biothirdpartyecataloguereqdetails where nthirdpartyecatrequestcode = "
					+ nthirdpartyecatrequestcode + " and nproductcode = " + nproductcode + " and sparentsamplecode = "
					+ sparentsamplecode + " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		}
		List<BioThirdPartyECatalogueReqDetails> lstproductcode = jdbcTemplate.query(strQuery,
				new BioThirdPartyECatalogueReqDetails());
		if (lstproductcode != null && !lstproductcode.isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public ResponseEntity<Map<String, Object>> getActiveSampleDetail(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		final int nthirdpartyecatreqdetailcode = Integer
				.valueOf(inputMap.get("nthirdpartyecatreqdetailcode").toString());
		final String catalogueDetailQry = "select bcd.nthirdpartyecatreqdetailcode, bcd.nthirdpartyecatrequestcode, bc.sformnumber, s.ssitename as sreceivingsitename, bc.nreceiversitecode, "
				+ "bcd.nbioprojectcode, bp.sprojecttitle, bcd.nproductcode, pr.sproductname || ' (' || pc.sproductcatname || ')' as sproductname, "
				+ "bcd.sparentsamplecode, bcd.nreqnoofsamples, bcd.sreqminvolume, bcd.naccnoofsamples, bcd.saccminvolume, "
				+ "bcd.dtransactiondate, bcd.ntztransactiondate, bcd.noffsetdtransactiondate, tz1.stimezoneid as stztransactiondate, "
				+ "COALESCE(TO_CHAR(bcd.dtransactiondate,'" + userInfo.getSsitedate() + "'), '') as stransactiondate "
				+ "from biothirdpartyecataloguereqdetails bcd, biothirdpartyecataloguerequest bc, site s, bioproject bp, product pr, productcategory pc, timezone tz1 "
				+ "where bcd.nthirdpartyecatreqdetailcode = " + nthirdpartyecatreqdetailcode + " "
				+ "and bcd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bcd.nsitecode = " + userInfo.getNtranssitecode() + " "
				+ "and bc.nthirdpartyecatrequestcode = bcd.nthirdpartyecatrequestcode and bc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bc.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + "and s.nsitecode = bc.nreceiversitecode and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nmastersitecode = "
				+ userInfo.getNmastersitecode() + " " + "and bcd.nbioprojectcode = bp.nbioprojectcode and bp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bcd.nproductcode = pr.nproductcode and pr.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ "and pr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and pc.nproductcatcode = pr.nproductcatcode and pc.nsitecode = " + userInfo.getNmastersitecode()
				+ " " + "and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bcd.ntztransactiondate = tz1.ntimezonecode and tz1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "order by bcd.nthirdpartyecatreqdetailcode desc";

		final BioThirdPartyECatalogueReqDetails objBioThirdPartyECatalogueDetails = (BioThirdPartyECatalogueReqDetails) jdbcUtilityTemplateFunction
				.queryForObject(catalogueDetailQry, BioThirdPartyECatalogueReqDetails.class, jdbcTemplate);
		if (objBioThirdPartyECatalogueDetails != null) {
			returnMap.put("selectedBioThirdPartyECatalogueDetails", objBioThirdPartyECatalogueDetails);
		} else {
			returnMap.put("selectedBioThirdPartyECatalogueDetails", null);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getBioSampleAvailability(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		// read inputs (fallbacks)
		Integer nprojecttypecode = inputMap.get("nbioprojectcode") != null
				? Integer.valueOf(inputMap.get("nbioprojectcode").toString().trim())
				: 0;
		Integer nproductcode = inputMap.get("nproductcode") != null
				? Integer.valueOf(inputMap.get("nproductcode").toString().trim())
				: 0;

		// decimal / regex helpers
		final String decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
				? "."
				: userInfo.getSdecimaloperator();
		final String opForReplace = stringUtilityFunction.replaceQuote(decOperator);
		final String opForRegex = stringUtilityFunction.replaceQuote(decOperator);

		// pattern and cast expressions (COLLATE "default" preserved)
		final String patternLiteral = "'^[0-9]+([" + opForRegex + "][0-9]+)?$' COLLATE \"default\"";
		final String castExprSt = "REPLACE(TRIM(st.sqty COLLATE \"default\"), '" + opForReplace + "', '.')::numeric";

		// sreqminvolume (may be null/empty)
		String sreqminvolume = null;

		final String sallchildsitecurrentcodes = getChildSitesFromAllProjects(userInfo);

		if (inputMap.get("sreqminvolume") != null) {
			sreqminvolume = stringUtilityFunction.replaceQuote(inputMap.get("sreqminvolume").toString()).trim();
		}

		// Build SQL in the same in-line style you use elsewhere
		String sql = "SELECT COUNT(st.nsamplestoragetransactioncode) " + "FROM samplestoragetransaction st "
				+ "JOIN biosubjectdetails bs ON st.ssubjectid = bs.ssubjectid   and bs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bs.nsitecode = "
				+ userInfo.getNmastersitecode() + " WHERE st.nprojecttypecode = " + nprojecttypecode
				+ " AND st.nproductcode = " + nproductcode + " AND st.nsitecode in "
//				+ userInfo.getNtranssitecode()
				+ " (" + sallchildsitecurrentcodes + ") ";

		if (sreqminvolume != null && !sreqminvolume.isEmpty()) {
			String castExprB = "REPLACE(TRIM('" + sreqminvolume + "' COLLATE \"default\"), '" + opForReplace
					+ "', '.')::numeric";

			sql += " AND (CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + patternLiteral + ") THEN " + castExprSt
					+ " ELSE NULL END) >= (CASE WHEN (TRIM('" + sreqminvolume + "' COLLATE \"default\")) ~ ("
					+ patternLiteral + ") THEN " + castExprB + " ELSE NULL END)";
		}

		sql += " AND st.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND bs.nissampleaccesable = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " AND bs.nisthirdpartysharable=" + Enumeration.TransactionStatus.YES.gettransactionstatus() + ";";

		// Execute and return
		Integer availableCount = jdbcTemplate.queryForObject(sql, Integer.class);

		Map<String, Object> resp = new HashMap<>();
		resp.put("availablecount", availableCount);
		return new ResponseEntity<Object>(resp, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getSubjectCountsByProductAndProject(final UserInfo userInfo) throws Exception {
		final String sallchildsitecodes = getChildSitesFromAllProjects(userInfo); // e.g., "2,3,4" or null
		final String sbioprojectcodes = getProjectsofSite(userInfo); // e.g., "101,102" or null
		final int nthirdPartyCode = getThirdPartyCode(userInfo);

		// Prepare safe IN-lists
		final String siteInList = (sallchildsitecodes != null && !sallchildsitecodes.trim().isEmpty())
				? sallchildsitecodes
				: "-1";
		final String projectInList = (sbioprojectcodes != null && !sbioprojectcodes.trim().isEmpty()) ? sbioprojectcodes
				: "-1";

		final int active = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final int masterSite = userInfo.getNmastersitecode();

		final String cte = "WITH unique_subjects AS ( "
				+ "  SELECT DISTINCT st.nprojecttypecode AS nbioprojectcode, st.nproductcode, st.ssubjectid "
				+ "  FROM samplestoragetransaction st " + "  JOIN biosubjectdetails bd "
				+ "    ON bd.ssubjectid = st.ssubjectid " + "   AND bd.nissampleaccesable = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " AND bd.nisthirdpartysharable = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "   AND bd.nstatus = " + active + " "
				+ "   AND bd.nsitecode = " + masterSite + " " + " JOIN biodataaccessrequest bdar on bdar.nsitecode="
				+ userInfo.getNtranssitecode() + " and bdar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bdar.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " JOIN biodataaccessrequestdetails bdard on bdard.nsitecode=" + userInfo.getNtranssitecode()
				+ " and bdard.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bdar.nbiodataaccessrequestcode=bdard.nbiodataaccessrequestcode"
				+ " and bdard.nbioprojectcode=st.nprojecttypecode and bdard.nproductcode=st.nproductcode"
				+ " and bdar.nthirdpartycode=" + nthirdPartyCode + "  WHERE COALESCE(st.ssubjectid,'') <> '' "
				+ "    AND st.nsitecode IN (" + siteInList + ") " + "    AND st.nstatus = " + active + " " + "), "
				+ "base AS ( "
				+ "  SELECT us.ssubjectid, us.nproductcode, (p.sproductname||' ('||COALESCE(pc.sproductcatname,'')||')') AS sproductname, bp.sprojecttitle "
				+ "  FROM unique_subjects us " + "  JOIN product p " + "    ON p.nproductcode = us.nproductcode "
				+ "   AND p.nsitecode    = " + masterSite + " " + "   AND p.nstatus      = " + active + " "
				+ " LEFT JOIN productcategory pc ON pc.nproductcatcode=p.nproductcatcode" + " AND pc.nsitecode="
				+ masterSite + " AND pc.nstatus=" + active + "  JOIN bioproject bp "
				+ "    ON bp.nbioprojectcode = us.nbioprojectcode " + "   AND bp.nbioprojectcode IN (" + projectInList
				+ ") " + "   AND bp.nsitecode       = " + masterSite + " " + "   AND bp.nstatus         = " + active
				+ " " + ") ";

		final String sql = cte
				+ "SELECT sproductname, sprojecttitle, nproductcode, COUNT(DISTINCT ssubjectid) AS ndistinctsubjects "
				+ "FROM base " + "GROUP BY sproductname, sprojecttitle,nproductcode "
				+ "ORDER BY sproductname, sprojecttitle";

		Map<String, Object> out = new HashMap<>();
		out.put("rows", jdbcTemplate.queryForList(sql));
		return new ResponseEntity<>(out, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSubjectCountsByProductAndDisease(final UserInfo userInfo) throws Exception {
		final String sallchildsitecodes = getChildSitesFromAllProjects(userInfo);
		final String sbioprojectcodes = getProjectsofSite(userInfo);
		final int nthirdPartyCode = getThirdPartyCode(userInfo);

		final String siteInList = (sallchildsitecodes != null && !sallchildsitecodes.trim().isEmpty())
				? sallchildsitecodes
				: "-1";
		final String projectInList = (sbioprojectcodes != null && !sbioprojectcodes.trim().isEmpty()) ? sbioprojectcodes
				: "-1";

		final int active = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final int masterSite = userInfo.getNmastersitecode();

		final String cte = "WITH unique_subjects AS ( "
				+ "  SELECT DISTINCT st.nprojecttypecode AS nbioprojectcode, st.nproductcode, st.ssubjectid "
				+ "  FROM samplestoragetransaction st "
				+ "  JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid " + "    AND bd.nissampleaccesable = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " AND bd.nisthirdpartysharable = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + "    AND bd.nstatus = " + active + " "
				+ "    AND bd.nsitecode = " + masterSite + " " + " JOIN biodataaccessrequest bdar on bdar.nsitecode="
				+ userInfo.getNtranssitecode() + " and bdar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bdar.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " JOIN biodataaccessrequestdetails bdard on bdard.nsitecode=" + userInfo.getNtranssitecode()
				+ " and bdard.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bdar.nbiodataaccessrequestcode=bdard.nbiodataaccessrequestcode"
				+ " and bdard.nbioprojectcode=st.nprojecttypecode and bdard.nproductcode=st.nproductcode"
				+ " and bdar.nthirdpartycode=" + nthirdPartyCode + "  WHERE COALESCE(st.ssubjectid,'') <> '' "
				+ "    AND st.nsitecode IN (" + siteInList + ") " + "    AND st.nstatus = " + active + " " + "), "
				+ "base AS ( "
				+ "  SELECT us.ssubjectid, us.nproductcode, (p.sproductname||' ('||COALESCE(pc.sproductcatname,'')||')') AS sproductname,"
				+ " COALESCE(d.sdiseasename, dc.sdiseasecategoryname) AS sgroup " + "  FROM unique_subjects us "
				+ "  JOIN product p " + "    ON p.nproductcode = us.nproductcode " + "   AND p.nsitecode    = "
				+ masterSite + " " + "   AND p.nstatus      = " + active + " "
				+ " LEFT JOIN productcategory pc ON pc.nproductcatcode=p.nproductcatcode" + " AND pc.nsitecode="
				+ masterSite + " AND pc.nstatus=" + active + "  JOIN bioproject bp "
				+ "    ON bp.nbioprojectcode = us.nbioprojectcode " + "   AND bp.nbioprojectcode IN (" + projectInList
				+ ") " + "   AND bp.nsitecode       = " + masterSite + " " + "   AND bp.nstatus         = " + active
				+ " " + "  JOIN diseasecategory dc " + "    ON dc.ndiseasecategorycode = bp.ndiseasecategorycode "
				+ "   AND dc.nsitecode            = " + masterSite + " " + "   AND dc.nstatus              = " + active
				+ " " + "  LEFT JOIN disease d " + "    ON d.ndiseasecode = bp.ndiseasecode "
				+ "   AND d.nsitecode     = " + masterSite + " " + "   AND d.nstatus       = " + active + " " + ") ";

		final String sql = cte
				+ "SELECT sproductname, sgroup, nproductcode, COUNT(DISTINCT ssubjectid) AS ndistinctsubjects "
				+ "FROM base " + "GROUP BY sproductname, sgroup,nproductcode " + "ORDER BY sproductname, sgroup";

		Map<String, Object> out = new HashMap<>();
		out.put("rows", jdbcTemplate.queryForList(sql));
		return new ResponseEntity<>(out, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getProductComboDataForSampleAdd(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final int nthirdpartyecatrequestcode = Integer.valueOf(inputMap.get("nthirdpartyecatrequestcode").toString());
		BioThirdPartyECatalogueRequest objBioThirdPartyECatalogue = getActiveThirdPartyECatalogueRequestForValidation(
				nthirdpartyecatrequestcode, userInfo);

		String schildSiteCode = getChildSitesFromAllProjects(userInfo);
		schildSiteCode = schildSiteCode.contains(",")
				? schildSiteCode.substring(0, schildSiteCode.lastIndexOf(",")) + ","
						+ objBioThirdPartyECatalogue.getNreceiversitecode()
				: "" + objBioThirdPartyECatalogue.getNreceiversitecode() + "";
		final String decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
				? "."
				: userInfo.getSdecimaloperator();
		final String opForRegex = escapeForSqlRegexCharClass(decOperator);
		final String opForReplace = stringUtilityFunction.replaceQuote(decOperator);
		final String patternLiteral = "'^[0-9]+([" + opForRegex + "][0-9]+)?$' COLLATE \"default\"";
		final String castExpr = "REPLACE(TRIM(st.sqty COLLATE \"default\"), '" + opForReplace + "', '.')::numeric";

		String strQuery;

		if (Enumeration.RequestFormType.PARENTSAMPLEBASED.getRequestFormType() == objBioThirdPartyECatalogue
				.getNreqformtypecode()) {
			// Parent-based: DO NOT exclude products already used; allow same product with
			// different parent codes.
			strQuery = "SELECT p.nproductcode, (p.sproductname || ' (' || pc.sproductcatname || ')') AS sproductname"
					+ " FROM samplestoragetransaction st JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid"
					+ " AND bd.nsitecode = " + userInfo.getNmastersitecode() + " AND bd.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND bd.nissampleaccesable = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " AND bd.nisthirdpartysharable="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
					+ " JOIN product p ON p.nproductcode = st.nproductcode" + " AND p.nsitecode   = "
					+ userInfo.getNmastersitecode() + " AND p.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN productcategory pc ON pc.nproductcatcode = p.nproductcatcode" + " AND pc.nsitecode = "
					+ userInfo.getNmastersitecode() + " AND pc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " WHERE COALESCE(TRIM(st.sparentsamplecode), '') <> '' AND st.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND st.nprojecttypecode = "
					+ objBioThirdPartyECatalogue.getNbioprojectcode() + " AND st.nsitecode in (" + schildSiteCode
					+ ") AND NOT EXISTS (SELECT 1 FROM biothirdpartyecataloguereqdetails d"
					+ " WHERE d.nthirdpartyecatrequestcode = " + nthirdpartyecatrequestcode
					+ " AND d.nproductcode = st.nproductcode AND COALESCE(TRIM(d.sparentsamplecode), '') = COALESCE(TRIM(st.sparentsamplecode), '')"
					+ " AND d.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " GROUP BY p.nproductcode, p.sproductname, pc.sproductcatname ORDER BY p.sproductname";
		} else {
			// Bio-sample based: keep ORIGINAL behavior — exclude products already added to
			// the request
			// Joinned biodataaccessrequest and biodataaccessrequestdetails tables by
			// Gowtham on 13 nov 2025 for jira.id:BGSI-191
			strQuery = "SELECT bp.sprojecttitle, bp.nbioprojectcode,"
//					+ " st.nsitecode, s.ssitename,"   // commented by sujatha ATE_274 for bgsi-297 duplicate sample type load
					+ " p.sproductname || ' ('|| pc.sproductcatname || ')' as sproductname, st.nproductcode, "
					+ " COUNT(1) AS ntotalsamplecount, REPLACE(SUM(COALESCE(CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ ("
					+ patternLiteral + ") THEN " + castExpr + " ELSE NULL END, 0))::text,'.','" + decOperator
					+ "') AS stotalqty FROM samplestoragetransaction st "
					+ " JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid AND bd.nsitecode = "
					+ userInfo.getNmastersitecode() + " AND bd.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND bd.nissampleaccesable = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " AND bd.nisthirdpartysharable="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " "
					+ "JOIN product p ON p.nproductcode = st.nproductcode AND p.nproductcode > 0 "
					+ "  AND p.nsitecode = " + userInfo.getNmastersitecode() + " AND p.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " JOIN productcategory pc ON pc.nproductcatcode = p.nproductcatcode AND pc.nsitecode = "
					+ userInfo.getNmastersitecode() + " AND pc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "LEFT JOIN biothirdpartyecataloguereqdetails bcd ON bcd.nproductcode = p.nproductcode "
					+ "  AND bcd.nthirdpartyecatrequestcode = " + nthirdpartyecatrequestcode + " "
					+ "  AND bcd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "  AND bcd.nsitecode = " + userInfo.getNtranssitecode() + " "
					+ "JOIN bioproject bp ON bp.nbioprojectcode = st.nprojecttypecode AND bp.nbioprojectcode > 0 "
					+ "  AND bp.nsitecode = " + userInfo.getNmastersitecode() + " AND bp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "JOIN site s ON st.nsitecode = s.nsitecode AND s.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN biodataaccessrequest bdar on bdar.nsitecode=" + userInfo.getNtranssitecode()
					+ " and bdar.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and bdar.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " JOIN biodataaccessrequestdetails bdard on bdard.nsitecode=" + userInfo.getNtranssitecode()
					+ " and bdard.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and bdar.nbiodataaccessrequestcode=bdard.nbiodataaccessrequestcode"
					+ " and bdard.nbioprojectcode=st.nprojecttypecode and bdard.nproductcode=p.nproductcode"
					+ " WHERE st.nsitecode in (" + schildSiteCode + ") AND st.nprojecttypecode = "
					+ objBioThirdPartyECatalogue.getNbioprojectcode() + " AND bcd.nproductcode IS NULL "
					+ "GROUP BY bp.sprojecttitle,"
//					+ " st.nsitecode,"     // commented by sujatha ATE_274 for bgsi-297 duplicate sample type load
					+ " p.sproductname, bp.nbioprojectcode,"
//					+ " s.ssitename,"      // commented by sujatha ATE_274 for bgsi-297 duplicate sample type load
					+ " st.nproductcode, pc.sproductcatname  "
					+ "ORDER BY bp.sprojecttitle, "
//					+ "st.nsitecode,"       // commented by sujatha ATE_274 for bgsi-297 duplicate sample type load
					+ " p.sproductname;";
		}

		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("lstProduct", jdbcTemplate.queryForList(strQuery));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getParentSamples(final Map<String, Object> inputMap, final UserInfo userInfo) {
		final Map<String, Object> resp = new HashMap<>();

		final int active = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		// Inputs (no helper getters — follow your style)
		final Integer nthirdpartyecatrequestcode = inputMap.get("nthirdpartyecatrequestcode") != null
				? Integer.valueOf(inputMap.get("nthirdpartyecatrequestcode").toString().trim())
				: -1;

		final Integer nbioprojectcode = inputMap.get("nbioprojectcode") != null
				? Integer.valueOf(inputMap.get("nbioprojectcode").toString().trim())
				: 0;

		final Integer nproductcode = inputMap.get("nproductcode") != null
				? Integer.valueOf(inputMap.get("nproductcode").toString().trim())
				: 0;

		final String sreqminvolume = inputMap.get("sreqminvolume") != null
				? stringUtilityFunction.replaceQuote(inputMap.get("sreqminvolume").toString().trim())
				: "0";

		final Integer isEditOperation = inputMap.get("isEditOperation") != null
				? Integer.valueOf(inputMap.get("isEditOperation").toString().trim())
				: 4; // 3=Edit,4=Add

		final Integer nthirdpartyecatreqdetailcode = inputMap.get("nthirdpartyecatreqdetailcode") != null
				? Integer.valueOf(inputMap.get("nthirdpartyecatreqdetailcode").toString().trim())
				: -1;

		// Decimal operator setup
		final String decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
				? "."
				: userInfo.getSdecimaloperator();

		final String opForReplace = stringUtilityFunction.replaceQuote(decOperator);
		final String opForRegex = escapeForSqlRegexCharClass(decOperator);

		// numeric casts
		final String castExprSt = "REPLACE(TRIM(st.sqty COLLATE \"default\"), '" + opForReplace + "', '.')::numeric";
		final String castExprReq = "REPLACE(TRIM('" + sreqminvolume + "' COLLATE \"default\"), '" + opForReplace
				+ "', '.')::numeric";
		final String pattern = "'^[0-9]+([" + opForRegex + "][0-9]+)?$' COLLATE \"default\"";

		final String sql = "SELECT TRIM(st.sparentsamplecode) AS sparentsamplecode, "
				+ "       COUNT(st.nsamplestoragetransactioncode) AS availablecount "
				+ "  FROM samplestoragetransaction st " + " WHERE COALESCE(TRIM(st.sparentsamplecode),'') <> '' "
				+ "   AND st.nprojecttypecode = " + nbioprojectcode + " " + "   AND st.nsitecode        = "
				+ userInfo.getNtranssitecode() + " " + "   AND st.nproductcode     = " + nproductcode + " "
				+ "   AND st.nstatus          = " + active + " "
				+ "   AND (CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + pattern + ") THEN " + castExprSt
				+ " ELSE NULL END) " + "       >= (CASE WHEN (TRIM('" + sreqminvolume + "' COLLATE \"default\")) ~ ("
				+ pattern + ") THEN " + castExprReq + " ELSE NULL END) " +
				// Exclude existing (product+parent) pairs on this request — BUT allow the
				// current editing row
				"   AND NOT EXISTS ( " + "       SELECT 1 FROM biothirdpartyecataloguereqdetails d "
				+ "        WHERE d.nthirdpartyecatrequestcode = " + nthirdpartyecatrequestcode + " "
				+ "          AND d.nproductcode         = " + nproductcode + " "
				+ "          AND d.nstatus              = " + active + " "
				+ "          AND LOWER(COALESCE(d.sparentsamplecode,'')) = LOWER(TRIM(st.sparentsamplecode)) "
				+ "          AND ( " + "               " + isEditOperation + " <> 3 " + // if not edit, normal exclusion
				"               OR d.nthirdpartyecatreqdetailcode <> " + nthirdpartyecatreqdetailcode + // if edit,
																										// ignore the
																										// current
				// detail
				"          ) " + "   ) " + " GROUP BY TRIM(st.sparentsamplecode) "
				+ " ORDER BY TRIM(st.sparentsamplecode) ";

		final List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		resp.put("parentSamples", rows);
		return new ResponseEntity<>(resp, HttpStatus.OK);
	}

	private String getProjectsofSite(final UserInfo userInfo) throws Exception {

		String strQuery = "SELECT string_agg(nbioprojectcode::text, ',') AS sbioprojectcodes FROM projectsitemapping"
				+ " WHERE nnodesitecode = " + userInfo.getNtranssitecode() + " AND nsitecode = "
				+ userInfo.getNmastersitecode() + " AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final String sbioprojectcodes = (String) jdbcUtilityTemplateFunction.queryForObject(strQuery, String.class,
				jdbcTemplate);

		return sbioprojectcodes;
	}

	private String getChildSitesFromAllProjects(final UserInfo userInfo) throws Exception {

		String strQuery = "select string_agg(shcd.schildsitecode, ',') AS sallchildsitecodes from sitehierarchyconfigdetails shcd, projectsitemapping psm"
				+ " where shcd.nsitehierarchyconfigcode = psm.nsitehierarchyconfigcode and shcd.nnodesitecode = psm.nnodesitecode "
				+ " and psm.nnodesitecode = " + userInfo.getNtranssitecode() + " and psm.nsitecode = "
				+ userInfo.getNmastersitecode() + " and psm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and shcd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and shcd.schildsitecode"
				+ " is not null and shcd.schildsitecode <> ''";

		String sallchildsitecodes = (String) jdbcUtilityTemplateFunction.queryForObject(strQuery, String.class,
				jdbcTemplate);
		sallchildsitecodes = (sallchildsitecodes == null) ? userInfo.getNtranssitecode() + ""
				: sallchildsitecodes + "," + userInfo.getNtranssitecode();
		return sallchildsitecodes;
	}

	@Override
	public ResponseEntity<Object> getBioSampleTypeCombo(final int nselectedprojectcode, UserInfo userinfo)
			throws Exception {

		String schildSiteCode = getChildSitesFromAllProjects(userinfo);

		// Joinned biodataaccessrequest and biodataaccessrequestdetails tables by
		// Gowtham on 13 nov 2025 for jira.id:BGSI-191
		String strQuery = "select p.nproductcode, p.sproductname||' ('||pc.sproductcatname||')' as sproductname from samplestoragetransaction st"
				+ " join product p on st.nproductcode = p.nproductcode and p.nsitecode = "
				+ userinfo.getNmastersitecode() + " and p.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join productcategory pc on p.nproductcatcode = pc.nproductcatcode and pc.nsitecode = "
				+ userinfo.getNmastersitecode() + " and pc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join biosubjectdetails bs on bs.ssubjectid = st.ssubjectid and bs.nissampleaccesable = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " AND bs.nisthirdpartysharable = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and bs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bs.nsitecode = "
				+ userinfo.getNmastersitecode() + " join biodataaccessrequest bdar on bdar.nsitecode="
				+ userinfo.getNtranssitecode() + " and bdar.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bdar.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " join biodataaccessrequestdetails bdard on bdard.nsitecode=" + userinfo.getNtranssitecode()
				+ " and bdard.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bdar.nbiodataaccessrequestcode=bdard.nbiodataaccessrequestcode"
				+ " and bdard.nbioprojectcode=st.nprojecttypecode and bdard.nproductcode=p.nproductcode"
				+ " where st.nprojecttypecode = " + nselectedprojectcode + " and st.nsitecode in (" + schildSiteCode
				+ ") and st.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by p.nproductcode, p.sproductname, pc.sproductcatname";

		List<Product> lstBioSampleType = jdbcTemplate.query(strQuery, new Product());
		return new ResponseEntity<>(lstBioSampleType, HttpStatus.OK);

	}

	public int getThirdPartyCode(final UserInfo userInfo) throws Exception {
		final String strCheckThirdParty = "select coalesce((select tp.nthirdpartycode from thirdparty tp join thirdpartyusermapping tpm on"
				+ " tpm.nthirdpartycode=tp.nthirdpartycode and tpm.nsitecode=" + userInfo.getNmastersitecode()
				+ " and tpm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tpm.nusercode=" + userInfo.getNusercode() + " and tpm.nuserrolecode=" + userInfo.getNuserrole()
				+ " where tp.nsitecode=" + userInfo.getNmastersitecode() + " and tp.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tp.nisngs="
				+ Enumeration.TransactionStatus.NO.gettransactionstatus() + "), -1)";
		return jdbcTemplate.queryForObject(strCheckThirdParty, Integer.class);

	}

}
