package com.agaramtech.qualis.biobank.service.bgsiecatalogue;

import java.math.BigDecimal;
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
import com.agaramtech.qualis.biobank.model.BioBGSiECatalogue;
import com.agaramtech.qualis.biobank.model.BioBGSiECatalogueDetails;
import com.agaramtech.qualis.biobank.model.RequestFormType;
import com.agaramtech.qualis.biobank.model.SeqNoBioBankManagement;
import com.agaramtech.qualis.configuration.service.sitehospitalmapping.SiteHospitalMappingDAOImpl;
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
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.project.model.BioProject;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class BGSiECatalogueDAOImpl implements BGSiECatalogueDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(BGSiECatalogueDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final EmailDAOSupport emailDAOSupport;

	@Override
	public ResponseEntity<Object> getBGSiECatalogue(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		List<TransactionStatus> lstTransactionstatus = new ArrayList<>();
		final String currentUIDate = (String) inputMap.get("currentdate");
		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			returnMap = projectDAOSupport.getDateFromControlProperties(userInfo, currentUIDate, "datetime", "FromDate");
		}
		lstTransactionstatus = getBGSiECatalogueRequestFormStatuses(userInfo);
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

		List<BioBGSiECatalogue> lstBioBGSiECatalogueRequests = getListBGSiECatalogueRequest(returnMap, userInfo);
		if (!lstBioBGSiECatalogueRequests.isEmpty()) {
			returnMap.put("lstBioBGSiECatalogueRequests", lstBioBGSiECatalogueRequests);
			returnMap.put("selectedBioBGSiECatalogueRequest", lstBioBGSiECatalogueRequests.getFirst());
			returnMap.put("lstBioBGSiECatalogueDetails", getBioBGSiECatalogueDetailsFromRequest(
					lstBioBGSiECatalogueRequests.getFirst().getNbgsiecatrequestcode(), userInfo));

		} else {
			returnMap.put("lstBioBGSiECatalogueRequests", null);
			returnMap.put("selectedBioBGSiECatalogueRequest", null);
			returnMap.put("lstBioBGSiECatalogueDetails", null);
		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getBGSiECatalogueByFilterSubmit(Map<String, Object> inputMap, UserInfo userInfo)
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
		List<TransactionStatus> lstTransactionstatus = getBGSiECatalogueRequestFormStatuses(userInfo);
		returnMap.put("FilterStatus", lstTransactionstatus);
		returnMap.put("RealFilterStatuslist", lstTransactionstatus);

		List<BioBGSiECatalogue> lstBioBGSiECatalogueRequests = getListBGSiECatalogueRequest(inputMap, userInfo);
		if (!lstBioBGSiECatalogueRequests.isEmpty()) {
			returnMap.put("lstBioBGSiECatalogueRequests", lstBioBGSiECatalogueRequests);
			returnMap.put("selectedBioBGSiECatalogueRequest", lstBioBGSiECatalogueRequests.getFirst());
			returnMap.put("lstBioBGSiECatalogueDetails", getBioBGSiECatalogueDetailsFromRequest(
					lstBioBGSiECatalogueRequests.getFirst().getNbgsiecatrequestcode(), userInfo));

		} else {
			returnMap.put("lstBioBGSiECatalogueRequests", null);
			returnMap.put("selectedBioBGSiECatalogueRequest", null);
			returnMap.put("lstBioBGSiECatalogueDetails", null);
		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getActiveBGSiECatalogueRequestForm(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		final int nbgsiecatrequestcode = Integer.valueOf(inputMap.get("nbgsiecatrequestcode").toString());
		final String sampleQry = "select "
				+ "bc.nbgsiecatrequestcode, bc.sformnumber,bc.nreqformtypecode, bc.nreceiversitecode, rs.ssitename as sreceivingsitename, "
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
				+ "from biobgsiecatalogue bc, site rs, transactionstatus ts, timezone tz1, timezone tz2, requestformtype rft, "
				+ "bioproject p, formwisestatuscolor fwsc, colormaster cm " + "where bc.nbgsiecatrequestcode = "
				+ nbgsiecatrequestcode + " and bc.nreqformtypecode = rft.nreqformtypecode and rft.nstatus = "
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

		final BioBGSiECatalogue objBioBGSiECatalogueRequest = (BioBGSiECatalogue) jdbcUtilityTemplateFunction
				.queryForObject(sampleQry, BioBGSiECatalogue.class, jdbcTemplate);
		if (objBioBGSiECatalogueRequest != null) {
			returnMap.put("selectedBioBGSiECatalogueRequest", objBioBGSiECatalogueRequest);
			returnMap.put("lstBioBGSiECatalogueDetails",
					getBioBGSiECatalogueDetailsFromRequest(nbgsiecatrequestcode, userInfo));
		} else {
			returnMap.put("selectedBioBGSiECatalogueRequest", null);
			returnMap.put("lstBioBGSiECatalogueDetails", null);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	private List<BioBGSiECatalogueDetails> getBioBGSiECatalogueDetailsFromRequest(final int nbgsiecatrequestcode,
			final UserInfo userInfo) throws Exception {

		final String catalogueDetailQry = "select bcd.nbgsiecatdetailcode, bcd.nbgsiecatrequestcode, COALESCE(bcd.sparentsamplecode,'-') as sparentsamplecode, bcd.nreqnoofsamples, "
				+ "bcd.sreqminvolume, COALESCE(bcd.naccnoofsamples :: text ,'-') as naccnoofsamples, COALESCE(bcd.saccminvolume,'-') as saccminvolume, "
				+ "bc.sformnumber, s.ssitename as sreceivingsitename, "
				+ "bcd.nbioprojectcode, bp.sprojecttitle, bcd.nproductcode, pr.sproductname ||' ('||pc.sproductcatname||')' as sproductname, "
//				+ "bcd.srequestedvolume, bcd.sacceptedvolume,"
				+ "bcd.dtransactiondate, bcd.ntztransactiondate, bcd.noffsetdtransactiondate, tz1.stimezoneid as stztransactiondate, "
				+ "COALESCE(TO_CHAR(bcd.dtransactiondate,'" + userInfo.getSsitedate() + "'), '') as stransactiondate "
				+ "from biobgsiecataloguedetails bcd, biobgsiecatalogue bc, bioproject bp, site s, product pr, productcategory pc, timezone tz1 "
				+ "where bcd.nbgsiecatrequestcode = " + nbgsiecatrequestcode + " " + "and bcd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and bcd.nsitecode = "
				+ userInfo.getNtranssitecode() + " "
				+ "and bc.nbgsiecatrequestcode = bcd.nbgsiecatrequestcode and bc.nstatus = "
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
				+ "order by bcd.nbgsiecatdetailcode desc";

		return jdbcTemplate.query(catalogueDetailQry, new BioBGSiECatalogueDetails());
	}

	private List<BioBGSiECatalogue> getListBGSiECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo) {
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
				+ "bc.nbgsiecatrequestcode, bc.sformnumber, bc.nreceiversitecode, rs.ssitename as sreceivingsitename, "
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
				+ "from biobgsiecatalogue bc, site rs, transactionstatus ts, timezone tz1, timezone tz2, requestformtype rft, "
				+ "bioproject p, formwisestatuscolor fwsc, colormaster cm "
				+ "where bc.nreceiversitecode = rs.nsitecode and rs.nstatus = "
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
//				+ "group by bc.sformnumber, bc.nbgsiecatrequestcode, bc.nreceiversitecode, rs.ssitename, "
//				+ "bc.drequesteddate, bc.ntzrequesteddate, tz1.stimezoneid, bc.sremarks, bc.ntransactionstatus, ts.jsondata, "
//				+ "bc.dtransactiondate, bc.ntztransactiondate, tz2.stimezoneid "
				+ "order by bc.nbgsiecatrequestcode desc";

		return jdbcTemplate.query(sampleQry, new BioBGSiECatalogue());
	}

	public List<TransactionStatus> getBGSiECatalogueRequestFormStatuses(final UserInfo userInfo) throws Exception {

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
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	 order by r.nreqformtypecode";
		return jdbcTemplate.query(strQry, new RequestFormType());
	}

	public List<BioProject> getBioProjectComboData(final UserInfo userInfo) throws Exception {
		final String strQry = "SELECT bp.sprojecttitle ,bp.nbioprojectcode "
				+ "FROM projectsitemapping psm,bioproject bp WHERE psm.nbioprojectcode=bp.nbioprojectcode and bp.nsitecode="
				+ userInfo.getNmastersitecode() + " and psm.nnodesitecode=" + userInfo.getNtranssitecode()
				+ " and bp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and psm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bp.nbioprojectcode > 0" + " order by bp.sprojecttitle ;";
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

		String ssitecode = "-1"
//		+ userInfo.getNtranssitecode()
		;

		if (!listSiteHierMapping.isEmpty()) {
			final String schildsitecode = (String) listSiteHierMapping.get(0).get("schildsitecode");
			if (schildsitecode != null && !schildsitecode.isEmpty()) {
				ssitecode = ssitecode + "," + schildsitecode;
			}
		}

//		final String strQuery = "select nsitecode, ssitename, ndefaultstatus from site " + "where nsitecode in ("
//				+ ssitecode + ") " + "and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and nsitecode > 0 order by ssitename ;";

		final String strQuery = "SELECT s.nsitecode, s.ssitename, s.ndefaultstatus " + "  FROM site s "
				+ " WHERE s.nstatus   = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "   AND s.nsitecode > 0 " + "   AND s.nsitecode IN (" + ssitecode + ") "
				+ " AND EXISTS ( SELECT 1 FROM samplestoragetransaction st JOIN biosubjectdetails bs "
				+ " ON bs.ssubjectid = st.ssubjectid " + " AND bs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND bs.nissampleaccesable = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " " + " AND bs.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + " WHERE st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND st.nprojecttypecode = "
				+ nbioprojectcode + " " + " AND st.nsitecode = s.nsitecode " + " ) " + " ORDER BY s.ssitename;";

		return jdbcTemplate.query(strQuery, new Site());
	}

	@Override
	public ResponseEntity<Object> getComboDataForCatalogue(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
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
			lstProduct = (List<Product>) getBioSampleTypeCombo(lstProject.getFirst().getNbioprojectcode(),
					lstSite.getFirst().getNsitecode(), userInfo).getBody();
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

		final int nselectedsitecode = Integer.valueOf(inputMap.get("nselectedsitecode").toString());
		final int nselectedprojectcode = Integer.valueOf(inputMap.get("nselectedprojectcode").toString());

		final String decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
				? "."
				: userInfo.getSdecimaloperator();
		final String opForRegex = escapeForSqlRegexCharClass(decOperator);
		final String opForReplace = stringUtilityFunction.replaceQuote(decOperator);
		final String patternLiteral = "'^[0-9]+([" + opForRegex + "][0-9]+)?$' COLLATE \"default\"";
		final String castExpr = "REPLACE(TRIM(st.sqty COLLATE \"default\"), '" + opForReplace + "', '.')::numeric";

		final int ACTIVE = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final int YES = Enumeration.TransactionStatus.YES.gettransactionstatus();
		final int masterSite = userInfo.getNmastersitecode();

		final String strQuery = "WITH unique_subjects AS ( "
				+ "  SELECT st.nprojecttypecode, st.nsitecode, st.nproductcode, st.nproductcatcode, st.ssubjectid "
				+ "  FROM samplestoragetransaction st "
				+ "  JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid " + "    AND bd.nissampleaccesable = "
				+ YES + " " + "    AND bd.nstatus = " + ACTIVE + " " + "    AND bd.nsitecode = " + masterSite + " "
				+ "  WHERE COALESCE(st.ssubjectid, '') <> '' " + "    AND st.nprojecttypecode = " + nselectedprojectcode
				+ " " + "    AND st.nsitecode = " + nselectedsitecode + " " + "    AND st.nstatus = " + ACTIVE + " "
				+ "  GROUP BY st.nprojecttypecode, st.nsitecode, st.nproductcode, st.nproductcatcode, st.ssubjectid "
				+ "), distinct_counts AS ( "
				+ "  SELECT bp.sprojecttitle, bp.nbioprojectcode, u.nsitecode AS nsitecode, s.ssitename, "
				+ "         p.sproductname, u.nproductcode AS nproductcode, "
				+ "         pc.sproductcatname, u.nproductcatcode AS nproductcatcode, "
				+ "         COUNT(*) AS ndistinctsubjects " + "  FROM unique_subjects u "
				+ "  JOIN product p ON p.nproductcode = u.nproductcode " + "    AND p.nproductcode > 0 "
				+ "    AND p.nsitecode = " + masterSite + " " + "    AND p.nstatus = " + ACTIVE + " "
				+ "  JOIN productcategory pc ON pc.nproductcatcode = u.nproductcatcode " + "    AND pc.nsitecode = "
				+ masterSite + " " + "    AND pc.nstatus = " + ACTIVE + " "
				+ "  JOIN bioproject bp ON bp.nbioprojectcode = u.nprojecttypecode " + "    AND bp.nbioprojectcode > 0 "
				+ "    AND bp.nsitecode = " + masterSite + " " + "    AND bp.nstatus = " + ACTIVE + " "
				+ "  JOIN site s ON u.nsitecode = s.nsitecode " + "    AND s.nstatus = " + ACTIVE + " "
				+ "  GROUP BY bp.sprojecttitle, bp.nbioprojectcode, u.nsitecode, s.ssitename, "
				+ "           p.sproductname, u.nproductcode, pc.sproductcatname, u.nproductcatcode "
				+ "), samples_agg AS ( " + "  SELECT bp.sprojecttitle, bp.nbioprojectcode, st.nsitecode, s.ssitename, "
				+ "         p.sproductname, st.nproductcode, pc.sproductcatname, st.nproductcatcode, "
				+ "         COUNT(1) AS ntotalsamplecount, " + "         REPLACE(SUM(COALESCE( "
				+ "           CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + patternLiteral + ") "
				+ "                THEN " + castExpr + " " + "                ELSE NULL "
				+ "           END, 0 ) )::text, '.', '" + decOperator + "') AS stotalqty "
				+ "  FROM samplestoragetransaction st "
				+ "  JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid " + "    AND bd.nissampleaccesable = "
				+ YES + " " + "    AND bd.nstatus = " + ACTIVE + " " + "    AND bd.nsitecode = " + masterSite + " "
				+ "  JOIN product p ON p.nproductcode = st.nproductcode " + "    AND p.nproductcode > 0 "
				+ "    AND p.nsitecode = " + masterSite + " " + "    AND p.nstatus = " + ACTIVE + " "
				+ "  JOIN productcategory pc ON pc.nproductcatcode = st.nproductcatcode " + "    AND pc.nsitecode = "
				+ masterSite + " " + "    AND pc.nstatus = " + ACTIVE + " "
				+ "  JOIN bioproject bp ON bp.nbioprojectcode = st.nprojecttypecode "
				+ "    AND bp.nbioprojectcode > 0 " + "    AND bp.nsitecode = " + masterSite + " "
				+ "    AND bp.nstatus = " + ACTIVE + " " + "    AND st.nprojecttypecode = " + nselectedprojectcode + " "
				+ "  JOIN site s ON st.nsitecode = s.nsitecode " + "    AND s.nstatus = " + ACTIVE + " "
				+ "    AND st.nsitecode = " + nselectedsitecode + " " + "  WHERE st.nstatus = " + ACTIVE + " "
				+ "  GROUP BY bp.sprojecttitle, st.nsitecode, p.sproductname, "
				+ "           bp.nbioprojectcode, s.ssitename, st.nproductcode, pc.sproductcatname, st.nproductcatcode "
				+ ") " + "SELECT sa.sprojecttitle        AS sprojecttitle, "
				+ "       sa.nbioprojectcode      AS nbioprojectcode, "
				+ "       sa.nsitecode            AS nsitecode, " + "       sa.ssitename            AS ssitename, "
				+ "       sa.sproductcatname      AS sproductcatname, "
				+ "       sa.nproductcatcode      AS nproductcatcode, "
				+ "       sa.sproductname         AS sproductname, "
				+ "       sa.nproductcode         AS nproductcode, "
				+ "       sa.ntotalsamplecount    AS ntotalsamplecount, "
				+ "       COALESCE(dc.ndistinctsubjects, 0) AS ntotalsubjectcount, "
				+ "       sa.stotalqty            AS stotalqty " + "FROM samples_agg sa "
				+ "LEFT JOIN distinct_counts dc " + "  ON dc.nproductcode = sa.nproductcode "
				+ " AND dc.nsitecode = sa.nsitecode " + " AND dc.nbioprojectcode = sa.nbioprojectcode "
				+ " AND dc.nproductcatcode = sa.nproductcatcode "
				+ "ORDER BY sa.sprojecttitle, sa.nsitecode, sa.sproductcatname, sa.sproductname";

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
		final int nselectedsitecode = Integer.valueOf(inputMap.get("nselectedsitecode").toString());
		final int nselectedprojectcode = Integer.valueOf(inputMap.get("nselectedprojectcode").toString());
		final int nselectedproductcode = Integer.valueOf(inputMap.get("nselectedproductcode").toString());

		final int ACTIVE = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final int YES = Enumeration.TransactionStatus.YES.gettransactionstatus();
		final int masterSite = userInfo.getNmastersitecode();

		final String strQuery = "SELECT st.nsamplestoragetransactioncode, st.nsamplestoragelocationcode, "
				+ "       st.nsamplestoragemappingcode, st.ninstrumentcode, st.nprojecttypecode, "
				+ "       st.sposition, st.spositionvalue, st.jsondata, st.npositionfilled, "
				+ "       st.nbiosamplereceivingcode, st.sparentsamplecode, st.ncohortno, "
				+ "       st.nproductcatcode, st.nproductcode, st.sqty, st.slocationcode, "
				+ "       st.ssubjectid, st.scasetype, st.ndiagnostictypecode, st.ncontainertypecode, "
				+ "       st.nstoragetypecode, st.dmodifieddate, " + "       COALESCE(TO_CHAR(st.dmodifieddate, '"
				+ userInfo.getSsitedate() + "'), '') AS sdmodifieddate, "
				+ "       st.nsitecode, s.ssitename, st.nstatus, " + "       pc.sproductcatname, "
				+ "       i.sinstrumentid, " + "       coalesce(dt.jsondata->> '" + userInfo.getSlanguagetypecode()
				+ "  ', dt.jsondata->>'en-US') as sdiagnostictypename, " + "       ct.scontainertype, "
				+ "       coalesce(stt.jsondata->> '" + userInfo.getSlanguagetypecode()
				+ "  ', stt.jsondata->>'en-US') as sstoragetypename, " + "       p.sproductname, bp.sprojecttitle, "
				+ "       dc.ndiseasecategorycode, dc.sdiseasecategoryname, d.ndiseasecode, d.sdiseasename "
				+ "FROM samplestoragetransaction st " + "JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid "
				+ "  AND bd.nissampleaccesable = " + YES + " " + "  AND bd.nstatus = " + ACTIVE + " "
				+ "  AND bd.nsitecode = " + masterSite + " "
				+ "JOIN productcategory pc ON pc.nproductcatcode = st.nproductcatcode " + "  AND pc.nsitecode = "
				+ masterSite + " " + "  AND pc.nstatus = " + ACTIVE + " "
				+ "JOIN instrument i ON i.ninstrumentcode = st.ninstrumentcode " + "  AND i.nregionalsitecode = "
				+ nselectedsitecode + " " + "  AND i.nstatus = " + ACTIVE + " "
				+ "JOIN diagnostictype dt ON dt.ndiagnostictypecode = st.ndiagnostictypecode " + "  AND dt.nstatus = "
				+ ACTIVE + " " + "JOIN containertype ct ON ct.ncontainertypecode = st.ncontainertypecode "
				+ "  AND ct.nstatus = " + ACTIVE + " "
				+ "JOIN storagetype stt ON stt.nstoragetypecode = st.nstoragetypecode " + "  AND stt.nstatus = "
				+ ACTIVE + " " + "JOIN product p ON p.nproductcode = st.nproductcode " + "  AND p.nsitecode = "
				+ masterSite + " " + "  AND p.nstatus = " + ACTIVE + " "
				+ "JOIN bioproject bp ON bp.nbioprojectcode = st.nprojecttypecode " + "  AND bp.nsitecode = "
				+ masterSite + " " + "  AND bp.nstatus = " + ACTIVE + " "
				+ "JOIN diseasecategory dc ON dc.ndiseasecategorycode = bp.ndiseasecategorycode "
				+ "  AND dc.nsitecode = " + masterSite + " " + "  AND dc.nstatus = " + ACTIVE + " "
				+ "JOIN disease d ON d.ndiseasecode = bp.ndiseasecode " + "  AND d.nsitecode = " + masterSite + " "
				+ "  AND d.nstatus = " + ACTIVE + " " + "JOIN site s ON s.nsitecode = st.nsitecode "
				+ "  AND s.nstatus = " + ACTIVE + " " + "WHERE st.nsitecode = " + nselectedsitecode + " "
				+ "  AND st.nprojecttypecode = " + nselectedprojectcode + " " + "  AND st.nproductcode = "
				+ nselectedproductcode + " " + "  AND st.nstatus = " + ACTIVE + " "
				+ "ORDER BY st.nsamplestoragetransactioncode, st.ninstrumentcode;";

		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("lstDetailedData", jdbcTemplate.queryForList(strQuery));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createBGSiECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final int nselectedsitecode = Integer.valueOf(inputMap.get("nselectedsitecode").toString());
		final int nselectedbioprojectcode = Integer.valueOf(inputMap.get("nselectedbioprojectcode").toString());
		final int nselectedreqformtypecode = Integer.valueOf(inputMap.get("nselectedreqformtypecode").toString());

		final String sremarks = inputMap.containsKey("sremarks") && inputMap.get("sremarks").toString() != null
				&& inputMap.get("sremarks").toString() != ""
						? "'" + stringUtilityFunction.replaceQuote(inputMap.get("sremarks").toString()) + "'"
						: null;

		String sQuery = " lock  table lockbiobgsiecatalogue " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final String sSeqQuery = "select nsequenceno,stablename from seqnobiobankmanagement where stablename in (N'biobgsiecatalogue',"
				+ "N'biobgsiecataloguehistory')";
		List<SeqNoBioBankManagement> lstSeqBioBank = jdbcTemplate.query(sSeqQuery, new SeqNoBioBankManagement());
		Map<String, Integer> seqNoMap = lstSeqBioBank.stream()
				.collect(Collectors.toMap(SeqNoBioBankManagement::getStablename,
						seqNoTestGroupmanagement -> seqNoTestGroupmanagement.getNsequenceno()));
		int seqNoBgsiECatReq = seqNoMap.get("biobgsiecatalogue") + 1;
		int seqNoBgsiECatReqHis = seqNoMap.get("biobgsiecataloguehistory") + 1;

		final String strformat = projectDAOSupport.getSeqfnFormat("biobgsiecatalogue", "seqnoformatgeneratorbiobank", 0,
				0, userInfo);

		String strInsertBGSiECatalogue = "INSERT INTO public.biobgsiecatalogue("
				+ "	nbgsiecatrequestcode, sformnumber, nreqformtypecode, nbioprojectcode, nreceiversitecode, drequesteddate, ntzrequesteddate,"
				+ " noffsetdrequesteddate, ntransactionstatus, sremarks, sapprovalremarks, dtransactiondate,"
				+ " ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)" + "	VALUES (" + seqNoBgsiECatReq
				+ ", '" + strformat + "', " + nselectedreqformtypecode + ", " + nselectedbioprojectcode + ", "
				+ nselectedsitecode + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
				+ userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + " , " + sremarks + ", null, '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ");";
		String strInsertBGSiECatalogueHistory = "INSERT INTO public.biobgsiecataloguehistory("
				+ "	nbiobgsiecataloguehistorycode, nbgsiecatrequestcode, nusercode, nuserrolecode, ndeputyusercode,"
				+ " ndeputyuserrolecode, ntransactionstatus, scomments, dtransactiondate, ntztransactiondate,"
				+ " noffsettransactiondate, nsitecode, nstatus)" + "	VALUES (" + seqNoBgsiECatReqHis + ","
				+ seqNoBgsiECatReq + "," + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
				+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
				+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "', '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ");";

		String strSeqNoUpdate = "update seqnobiobankmanagement set nsequenceno=" + seqNoBgsiECatReq + " where"
				+ " stablename='biobgsiecatalogue' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		strSeqNoUpdate += "update seqnobiobankmanagement set nsequenceno=" + seqNoBgsiECatReqHis + " where"
				+ " stablename='biobgsiecataloguehistory' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		inputMap.put("nbgsiecatrequestcode", seqNoBgsiECatReq);

		String rtnQry = insertBGSiECatalogueDetails(inputMap, userInfo);

		jdbcTemplate.execute(strInsertBGSiECatalogue + strInsertBGSiECatalogueHistory + rtnQry + strSeqNoUpdate);

		final List<Object> listBeforeUpdate = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.putAll((Map<String, Object>) getActiveBGSiECatalogueRequestForm(inputMap, userInfo).getBody());
		BioBGSiECatalogue objBioBGSiECatalogueRequest = (BioBGSiECatalogue) returnMap
				.get("selectedBioBGSiECatalogueRequest");
		listBeforeUpdate.add(objBioBGSiECatalogueRequest);
		multilingualIDList.add("IDS_ADDBGSIECATALOGUEREQUEST");
		List<BioBGSiECatalogueDetails> lstBioBGSiECatalogueDetails = (List<BioBGSiECatalogueDetails>) returnMap
				.get("lstBioBGSiECatalogueDetails");
		listBeforeUpdate.addAll(lstBioBGSiECatalogueDetails);
		lstBioBGSiECatalogueDetails.stream().forEach(x -> multilingualIDList.add("IDS_ADDBGSIECATALOGUEREQUESTSAMPLE"));
		auditUtilityFunction.fnInsertAuditAction(listBeforeUpdate, 1, null, multilingualIDList, userInfo);

		return getBGSiECatalogueByFilterSubmit(inputMap, userInfo);
	}

	public String insertBGSiECatalogueDetails(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		final int nbgsiecatrequestcode = Integer.valueOf(inputMap.get("nbgsiecatrequestcode").toString());
		final int nselectedsitecode = Integer.valueOf(inputMap.get("nselectedsitecode").toString());
		final int nselectedbioprojectcode = Integer.valueOf(inputMap.get("nselectedbioprojectcode").toString());
		final int nselectedreqformtypecode = Integer.valueOf(inputMap.get("nselectedreqformtypecode").toString());

		final String sSeqQuery = "select nsequenceno,stablename from seqnobiobankmanagement where stablename in (N'biobgsiecataloguedetails',"
				+ "N'bioecataloguerequestdetails')";
		List<SeqNoBioBankManagement> lstSeqBioBank = jdbcTemplate.query(sSeqQuery, new SeqNoBioBankManagement());
		Map<String, Integer> seqNoMap = lstSeqBioBank.stream()
				.collect(Collectors.toMap(SeqNoBioBankManagement::getStablename,
						seqNoTestGroupmanagement -> seqNoTestGroupmanagement.getNsequenceno()));
		int seqNoBgsiECatDetails = seqNoMap.get("biobgsiecataloguedetails");

		String strBGSiECatalogueDetails = "INSERT INTO public.biobgsiecataloguedetails("
				+ "	nbgsiecatdetailcode, nbgsiecatrequestcode, nbioprojectcode, nproductcode,"
//				+ " srequestedvolume,"
//				+ " sacceptedvolume,"
				+ " sparentsamplecode, nreqnoofsamples, sreqminvolume, naccnoofsamples, saccminvolume,"
				+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)" + "	VALUES ";

		List<Map<String, Object>> requestProductList = (List<Map<String, Object>>) inputMap.get("requestProductList");

		for (Map<String, Object> reqProduct : requestProductList) {
			seqNoBgsiECatDetails++;
			int nproductcode = Integer.valueOf(reqProduct.get("nproductcode").toString());
			int nreqnoofsamples = Integer.valueOf(reqProduct.get("nreqnoofsamples").toString());
			String sparentsamplecode = reqProduct.containsKey("sparentsamplecode")
					&& reqProduct.get("sparentsamplecode") != null && reqProduct.get("sparentsamplecode") != ""
					&& reqProduct.get("sparentsamplecode") != "-"
					&& nselectedreqformtypecode == Enumeration.RequestFormType.PARENTSAMPLEBASED.getRequestFormType()
							? "'" + stringUtilityFunction.replaceQuote(reqProduct.get("sparentsamplecode").toString())
									+ "'"
							: null;
			String sreqminvolume = "'" + stringUtilityFunction.replaceQuote(reqProduct.get("sreqminvolume").toString())
					+ "'";
			strBGSiECatalogueDetails += "(" + seqNoBgsiECatDetails + ", " + nbgsiecatrequestcode + ", "
					+ nselectedbioprojectcode + ", " + nproductcode + ", "
//					+"'" + srequestedvolume + "', null, " 
					+ sparentsamplecode + ", " + nreqnoofsamples + ", " + sreqminvolume + ", null, null," + " '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "),";
		}

		strBGSiECatalogueDetails = strBGSiECatalogueDetails.substring(0, strBGSiECatalogueDetails.length() - 1) + ";";

		String strSeqNoUpdate = "update seqnobiobankmanagement set nsequenceno=" + seqNoBgsiECatDetails + " where"
				+ " stablename='biobgsiecataloguedetails' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		return (strBGSiECatalogueDetails + strSeqNoUpdate);
	}

	@Override
	public ResponseEntity<Object> sendBGSiECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		returnMap.putAll((Map<String, Object>) getActiveBGSiECatalogueRequestForm(inputMap, userInfo).getBody());

		BioBGSiECatalogue objBioBGSiECatalogueRequest = (BioBGSiECatalogue) returnMap
				.get("selectedBioBGSiECatalogueRequest");
		if (objBioBGSiECatalogueRequest != null && objBioBGSiECatalogueRequest
				.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			listBeforeUpdate.add(objBioBGSiECatalogueRequest);

			final List<BioBGSiECatalogueDetails> lstBioBGSiECatalogueDetails = (List<BioBGSiECatalogueDetails>) returnMap
					.get("lstBioBGSiECatalogueDetails");
			if (lstBioBGSiECatalogueDetails != null && !lstBioBGSiECatalogueDetails.isEmpty()) {

				final String sQuery = " lock  table lockbiobgsiecatalogue "
						+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				jdbcTemplate.execute(sQuery);

				final String sSeqQuery = "select nsequenceno,stablename from seqnobiobankmanagement where stablename in ("
						+ "N'biobgsiecataloguehistory',N'bioecataloguereqapproval',N'bioecataloguerequestdetails',N'bioecataloguereqapprovalhistory')";
				List<SeqNoBioBankManagement> lstSeqBioBank = jdbcTemplate.query(sSeqQuery,
						new SeqNoBioBankManagement());
				Map<String, Integer> seqNoMap = lstSeqBioBank.stream()
						.collect(Collectors.toMap(SeqNoBioBankManagement::getStablename,
								seqNoTestGroupmanagement -> seqNoTestGroupmanagement.getNsequenceno()));
				int seqNoBGSiECatalogueHistory = seqNoMap.get("biobgsiecataloguehistory") + 1;
				int seqNoEcatReqApproval = seqNoMap.get("bioecataloguereqapproval") + 1;
				int seqNoEcatReqApprovalHis = seqNoMap.get("bioecataloguereqapprovalhistory") + 1;

				int seqNoEcatReqAppDetails = seqNoMap.get("bioecataloguerequestdetails");

				final String updQry = "Update biobgsiecatalogue set ntransactionstatus = "
						+ Enumeration.TransactionStatus.REQUESTED.gettransactionstatus() + ", drequesteddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', noffsetdrequesteddate = "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", ntzrequesteddate = " + userInfo.getNtimezonecode() + ", dtransactiondate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', noffsetdtransactiondate = "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", ntztransactiondate = " + userInfo.getNtimezonecode() + " where nbgsiecatrequestcode = "
						+ objBioBGSiECatalogueRequest.getNbgsiecatrequestcode() + " and nsitecode = "
						+ userInfo.getNtranssitecode() + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

				String strInsert = "INSERT INTO public.biobgsiecataloguehistory("
						+ "	nbiobgsiecataloguehistorycode, nbgsiecatrequestcode, nusercode, nuserrolecode, ndeputyusercode,"
						+ " ndeputyuserrolecode, ntransactionstatus, scomments, dtransactiondate, ntztransactiondate,"
						+ " noffsettransactiondate, nsitecode, nstatus)" + "	VALUES (" + seqNoBGSiECatalogueHistory
						+ "," + objBioBGSiECatalogueRequest.getNbgsiecatrequestcode() + "," + userInfo.getNusercode()
						+ ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", "
						+ userInfo.getNdeputyuserrole() + ", "
						+ Enumeration.TransactionStatus.REQUESTED.gettransactionstatus() + ", '"
						+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "', '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

				strInsert += "update seqnobiobankmanagement set nsequenceno=" + seqNoBGSiECatalogueHistory + " where"
						+ " stablename='biobgsiecataloguehistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				String strInsertBioEcatReqApp = "INSERT INTO public.bioecataloguereqapproval("
						+ "	necatrequestreqapprovalcode, ntransfertype, norginsitecode, norginthirdpartycode, sformnumber, nreqformtypecode, "
						+ "drequesteddate, ntzrequesteddate, noffsetdrequesteddate, ntransactionstatus, sapprovalremarks,"
						+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)"
						+ "	VALUES (" + seqNoEcatReqApproval + ", "
						+ Enumeration.TransferType.BIOBANK.getntransfertype() + ", " + userInfo.getNtranssitecode()
						+ ", " + Enumeration.TransactionStatus.NA.gettransactionstatus() + ", '"
						+ objBioBGSiECatalogueRequest.getSformnumber() + "', "
						+ objBioBGSiECatalogueRequest.getNreqformtypecode() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ Enumeration.TransactionStatus.REQUESTED.gettransactionstatus() + ", null, '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ objBioBGSiECatalogueRequest.getNreceiversitecode() + ", "
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
						+ objBioBGSiECatalogueRequest.getNreceiversitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

				strInsertBioEcatReqAppHis += "update seqnobiobankmanagement set nsequenceno=" + seqNoEcatReqApprovalHis
						+ " where" + " stablename='bioecataloguereqapprovalhistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				String strBioECatalogueRequestDetails = "INSERT INTO public.bioecataloguerequestdetails("
						+ "	necateloguerequestdetailcode, necatrequestreqapprovalcode, nbioprojectcode, nproductcode,"
						+ " sparentsamplecode, sreqminvolume, nreqnoofsamples, naccnoofsamples, saccminvolume,"
						+ " sremarks, dtransactiondate, ntztransactiondate,"
						+ " noffsetdtransactiondate, nsitecode, nstatus)" + "	VALUES";

				for (BioBGSiECatalogueDetails objBioBGSiECatalogueDetail : lstBioBGSiECatalogueDetails) {
					seqNoEcatReqAppDetails++;
					int nproductcode = objBioBGSiECatalogueDetail.getNproductcode();
					int nreqnoofsamples = objBioBGSiECatalogueDetail.getNreqnoofsamples();
					String sparentsamplecode = objBioBGSiECatalogueDetail.getSparentsamplecode() != null
							&& !objBioBGSiECatalogueDetail.getSparentsamplecode().isEmpty()
							&& !objBioBGSiECatalogueDetail.getSparentsamplecode().equalsIgnoreCase("-")
									? "'" + stringUtilityFunction
											.replaceQuote(objBioBGSiECatalogueDetail.getSparentsamplecode()) + "'"
									: null;
					String sreqminvolume = "'"
							+ stringUtilityFunction.replaceQuote(objBioBGSiECatalogueDetail.getSreqminvolume()) + "'";
//					String srequestedvolume = objBioBGSiECatalogueDetail.getSrequestedvolume();
					strBioECatalogueRequestDetails += "(" + seqNoEcatReqAppDetails + ", " + seqNoEcatReqApproval + ", "
							+ objBioBGSiECatalogueDetail.getNbioprojectcode() + ", " + nproductcode + ", "
//							+ "'" + srequestedvolume + "', '"+ srequestedvolume +"',"
							+ sparentsamplecode + ", " + sreqminvolume + ", " + nreqnoofsamples + ", null, null,"
							+ " null, " + "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ userInfo.getNtimezonecode() + ", "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ objBioBGSiECatalogueRequest.getNreceiversitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
				}

				strBioECatalogueRequestDetails = strBioECatalogueRequestDetails.substring(0,
						strBioECatalogueRequestDetails.length() - 1) + ";";

				String strSeqNoUpdate = "update seqnobiobankmanagement set nsequenceno=" + seqNoEcatReqAppDetails
						+ " where" + " stablename='bioecataloguerequestdetails' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				jdbcTemplate.execute(updQry + strInsert + strInsertBioEcatReqApp + strInsertBioEcatReqAppHis
						+ strBioECatalogueRequestDetails + strSeqNoUpdate);

				outputMap
						.putAll((Map<String, Object>) getActiveBGSiECatalogueRequestForm(inputMap, userInfo).getBody());

				final List<String> multilingualIDList = new ArrayList<>();
				listAfterUpdate.add(outputMap.get("selectedBioBGSiECatalogueRequest"));

				multilingualIDList.add("IDS_SENDBGSIECATALOGUEREQUEST");

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
					mailMap.put("nbgsiecatrequestcode", objBioBGSiECatalogueRequest.getNbgsiecatrequestcode());
					String receiverquery = "Select nreceiversitecode from biobgsiecatalogue where nbgsiecatrequestcode= "
							+ objBioBGSiECatalogueRequest.getNbgsiecatrequestcode() + " and nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					Integer nreceiversitecode = jdbcTemplate.queryForObject(receiverquery, Integer.class);

					mailMap.put("nreceiversitecode", nreceiversitecode);

					String query = "SELECT sformnumber FROM biobgsiecatalogue where nbgsiecatrequestcode="
							+ objBioBGSiECatalogueRequest.getNbgsiecatrequestcode() + " and nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					String referenceId = jdbcTemplate.queryForObject(query, String.class);
					mailMap.put("ssystemid", referenceId);
					final UserInfo mailUserInfo = new UserInfo(userInfo);
					mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
					mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
					emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
				}

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
	public ResponseEntity<Object> cancelBGSiECatalogueRequest(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		returnMap.putAll((Map<String, Object>) getActiveBGSiECatalogueRequestForm(inputMap, userInfo).getBody());

		BioBGSiECatalogue objBioBGSiECatalogueRequest = (BioBGSiECatalogue) returnMap
				.get("selectedBioBGSiECatalogueRequest");

		if (objBioBGSiECatalogueRequest != null && (objBioBGSiECatalogueRequest
				.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| objBioBGSiECatalogueRequest.getNtransactionstatus() == Enumeration.TransactionStatus.REQUESTED
						.gettransactionstatus())) {

			listBeforeUpdate.add(objBioBGSiECatalogueRequest);

			final String sQuery = " lock  table lockbiobgsiecatalogue "
					+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			final String ecatReqAppStr = "select necatrequestreqapprovalcode " + "  from bioecataloguereqapproval "
					+ " where sformnumber = N'" + objBioBGSiECatalogueRequest.getSformnumber() + "' "
					+ "   and nsitecode   = " + objBioBGSiECatalogueRequest.getNreceiversitecode() + " "
					+ "   and norginsitecode = " + userInfo.getNtranssitecode() + " " + "   and nstatus     = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final Integer necatrequestreqapprovalcode = (Integer) jdbcUtilityTemplateFunction
					.queryForObject(ecatReqAppStr, Integer.class, jdbcTemplate);

			int seqNoBGSiECatalogueHistory = jdbcTemplate
					.queryForObject("select nsequenceno from seqnobiobankmanagement "
							+ " where stablename in (N'biobgsiecataloguehistory') " + "   and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);
			seqNoBGSiECatalogueHistory++;

			int seqNoEcatReqApprovalHis = jdbcTemplate.queryForObject("select nsequenceno from seqnobiobankmanagement "
					+ " where stablename in (N'bioecataloguereqapprovalhistory') " + "   and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(), Integer.class);

			final String updQry = "Update biobgsiecatalogue set ntransactionstatus = "
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", drequesteddate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ", noffsetdrequesteddate = "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", ntzrequesteddate = "
					+ userInfo.getNtimezonecode() + ", dtransactiondate = '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ", noffsetdtransactiondate = "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ ", ntztransactiondate = " + userInfo.getNtimezonecode() + " where nbgsiecatrequestcode = "
					+ objBioBGSiECatalogueRequest.getNbgsiecatrequestcode() + "   and nsitecode = "
					+ userInfo.getNtranssitecode() + "   and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

			String strInsert = "INSERT INTO public.biobgsiecataloguehistory("
					+ " nbiobgsiecataloguehistorycode, nbgsiecatrequestcode, nusercode, nuserrolecode, ndeputyusercode,"
					+ " ndeputyuserrolecode, ntransactionstatus, scomments, dtransactiondate, ntztransactiondate,"
					+ " noffsettransactiondate, nsitecode, nstatus)" + " VALUES (" + seqNoBGSiECatalogueHistory + ","
					+ objBioBGSiECatalogueRequest.getNbgsiecatrequestcode() + "," + userInfo.getNusercode() + ", "
					+ userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", "
					+ userInfo.getNdeputyuserrole() + ", "
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", '"
					+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "', '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ");" +

					"update seqnobiobankmanagement set nsequenceno=" + seqNoBGSiECatalogueHistory
					+ " where stablename='biobgsiecataloguehistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			if (necatrequestreqapprovalcode != null) {
				String updApproval = "Update bioecataloguereqapproval set ntransactionstatus = "
						+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", dtransactiondate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ", noffsetdtransactiondate = "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", ntztransactiondate = " + userInfo.getNtimezonecode()
						+ " where necatrequestreqapprovalcode = " + (int) necatrequestreqapprovalcode
						+ "   and nsitecode = " + objBioBGSiECatalogueRequest.getNreceiversitecode()
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
						+ objBioBGSiECatalogueRequest.getNreceiversitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");" +

						"update seqnobiobankmanagement set nsequenceno=" + seqNoEcatReqApprovalHis
						+ " where stablename='bioecataloguereqapprovalhistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				jdbcTemplate.execute(updQry + strInsert + updApproval + strInsertBioEcatReqAppHis);
			} else {
				jdbcTemplate.execute(updQry + strInsert);
			}

			outputMap.putAll((Map<String, Object>) getActiveBGSiECatalogueRequestForm(inputMap, userInfo).getBody());

			final List<String> multilingualIDList = new ArrayList<>();
			listAfterUpdate.add(outputMap.get("selectedBioBGSiECatalogueRequest"));
			multilingualIDList.add("IDS_CANCELBGSIECATALOGUEREQUEST");

			auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
					userInfo);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTORREQUESTEDSTATUSRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
	}

	public BioBGSiECatalogue getActiveBGSiECatalogueRequestForValidation(final int nbgsiecatrequestcode,
			UserInfo userInfo) throws Exception {
		final String sampleQry = "select "
				+ "bc.nbgsiecatrequestcode, bc.sformnumber, bc.nreqformtypecode, bc.nreceiversitecode, rs.ssitename as sreceivingsitename, "
				+ "bc.drequesteddate, bc.ntzrequesteddate, tz1.stimezoneid as stzrequesteddate, "
				+ "COALESCE(TO_CHAR(bc.drequesteddate,'" + userInfo.getSsitedate() + "'), '') as srequesteddate, "
				+ "bc.sremarks, bc.sapprovalremarks, bc.ntransactionstatus,cm.scolorhexcode, "
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus, "
				+ "bc.dtransactiondate, bc.ntztransactiondate, tz2.stimezoneid as stztransactiondate, "
				+ "COALESCE(TO_CHAR(bc.dtransactiondate,'" + userInfo.getSsitedate() + "'), '') as stransactiondate, "
				+ "p.sprojecttitle as sprojecttitles, p.nbioprojectcode "
				+ "from biobgsiecatalogue bc, site rs, transactionstatus ts, timezone tz1, timezone tz2, "
				+ "bioproject p, formwisestatuscolor fwsc, colormaster cm " + "where bc.nbgsiecatrequestcode = "
				+ nbgsiecatrequestcode + " and bc.nreceiversitecode = rs.nsitecode and rs.nstatus = "
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

		final BioBGSiECatalogue objBioBGSiECatalogueRequest = (BioBGSiECatalogue) jdbcUtilityTemplateFunction
				.queryForObject(sampleQry, BioBGSiECatalogue.class, jdbcTemplate);

		return objBioBGSiECatalogueRequest;
	}

//	@Override
//	public ResponseEntity<Object> getProductComboDataForSampleAdd(Map<String, Object> inputMap, UserInfo userInfo)
//			throws Exception {
//		final int nbgsiecatrequestcode = Integer.valueOf(inputMap.get("nbgsiecatrequestcode").toString());
//		BioBGSiECatalogue objBioBGSiECatalogue = getActiveBGSiECatalogueRequestForValidation(nbgsiecatrequestcode,
//				userInfo);
//
//		final String decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
//				? "."
//				: userInfo.getSdecimaloperator();
//		final String opForRegex = escapeForSqlRegexCharClass(decOperator);
//		final String opForReplace = stringUtilityFunction.replaceQuote(decOperator);
//		final String patternLiteral = "'^[0-9]+([" + opForRegex + "][0-9]+)?$' COLLATE \"default\"";
//		final String castExpr = "REPLACE(TRIM(st.sqty COLLATE \"default\"), '" + opForReplace + "', '.')::numeric";
//		final String strQuery = "SELECT bp.sprojecttitle, bp.nbioprojectcode, st.nsitecode, s.ssitename, p.sproductname, st.nproductcode,"
//				+ " COUNT(1) AS ntotalsamplecount," + " REPLACE(SUM( COALESCE( " + "    CASE "
//				+ "      WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + patternLiteral + ") " + "      THEN "
//				+ castExpr + " " + "      ELSE NULL " + "    END, 0 " + "  ))::text,'.','" + decOperator
//				+ "') AS stotalqty " + " FROM samplestoragetransaction st"
//				+ " JOIN product p ON p.nproductcode = st.nproductcode AND p.nproductcode > 0 AND p.nsitecode = "
//				+ userInfo.getNmastersitecode() + " AND p.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " LEFT JOIN biobgsiecataloguedetails bcd ON bcd.nproductcode = p.nproductcode"
//				+ "	AND bcd.nbgsiecatrequestcode = " + nbgsiecatrequestcode + "	AND bcd.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "	AND bcd.nsitecode = "
//				+ userInfo.getNtranssitecode()
//				+ " JOIN bioproject bp ON bp.nbioprojectcode = st.nprojecttypecode AND bp.nbioprojectcode > 0"
//				+ " AND bp.nsitecode = " + userInfo.getNmastersitecode() + " AND bp.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " JOIN site s ON st.nsitecode = s.nsitecode" + " AND s.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " WHERE st.nsitecode = "
//				+ objBioBGSiECatalogue.getNreceiversitecode() + " AND st.nprojecttypecode = "
//				+ objBioBGSiECatalogue.getNbioprojectcode() + " AND bcd.nproductcode IS NULL"
//				+ " GROUP BY  bp.sprojecttitle, st.nsitecode, p.sproductname, bp.nbioprojectcode, s.ssitename, st.nproductcode"
//				+ " ORDER BY bp.sprojecttitle, st.nsitecode, p.sproductname";
//
//		Map<String, Object> returnMap = new HashMap<>();
//		returnMap.put("lstProduct", jdbcTemplate.queryForList(strQuery));
//		return new ResponseEntity<>(returnMap, HttpStatus.OK);
//	}

	@Override
	public ResponseEntity<Object> createBGSiECatalogueRequestSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final int nbgsiecatrequestcode = Integer.valueOf(inputMap.get("nbgsiecatrequestcode").toString());
		BioBGSiECatalogue objBioBGSiECatalogue = getActiveBGSiECatalogueRequestForValidation(nbgsiecatrequestcode,
				userInfo);
		if (objBioBGSiECatalogue != null && objBioBGSiECatalogue
				.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			final int nproductcode = Integer.valueOf(inputMap.get("nproductcode").toString());
			String sparentsamplecode = inputMap.containsKey("sparentsamplecode")
					&& inputMap.get("sparentsamplecode") != null && inputMap.get("sparentsamplecode") != ""
					&& inputMap.get("sparentsamplecode") != "-"
					&& objBioBGSiECatalogue.getNreqformtypecode() == Enumeration.RequestFormType.PARENTSAMPLEBASED
							.getRequestFormType() ? "'"
									+ stringUtilityFunction.replaceQuote(inputMap.get("sparentsamplecode").toString())
									+ "'" : null;
			final int nreqformtypecode = objBioBGSiECatalogue.getNreqformtypecode();

			if (nreqformtypecode == Enumeration.RequestFormType.BIOSAMPLEBASED.getRequestFormType()
					|| (nreqformtypecode == Enumeration.RequestFormType.PARENTSAMPLEBASED.getRequestFormType()
							&& sparentsamplecode != null && !sparentsamplecode.toString().trim().isEmpty())) {

				boolean isProductAvailable = isProductAvailableInForm(nbgsiecatrequestcode, nreqformtypecode,
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
							&& !sparentsamplecode.toString().trim().isEmpty()
							&& objBioBGSiECatalogue
									.getNreqformtypecode() == Enumeration.RequestFormType.PARENTSAMPLEBASED
											.getRequestFormType())
													? " AND st.sparentsamplecode = " + sparentsamplecode + ""
													: "";

					String availableCountQry = "SELECT COUNT(st.nsamplestoragetransactioncode) "
							+ "FROM samplestoragetransaction st "
							+ "JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid "
							+ "  AND bd.nissampleaccesable = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " " + "  AND bd.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "  AND bd.nsitecode = " + userInfo.getNmastersitecode() + " WHERE st.nprojecttypecode = "
							+ objBioBGSiECatalogue.getNbioprojectcode() + " " + "  AND st.nproductcode = "
							+ nproductcode + " " + "  AND st.nsitecode = " + objBioBGSiECatalogue.getNreceiversitecode()
							+ " " + "  AND (CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + patternLiteral + ") "
							+ "            THEN " + castExprSt + " ELSE NULL END) " + "      >= (CASE WHEN (TRIM("
							+ sreqminvolumeTxt + " COLLATE \"default\")) ~ (" + patternLiteral + ") "
							+ "               THEN " + castExprB + " ELSE NULL END) " + "  AND st.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + parentSampleConditionStr
							+ ";";

					int nreqnoofsamples = Integer.valueOf(inputMap.get("nreqnoofsamples").toString().trim());

					String sreqminvolumewithquotes = "'"
							+ stringUtilityFunction.replaceQuote(inputMap.get("sreqminvolume").toString().trim()) + "'";

					final int navailablecount = jdbcTemplate.queryForObject(availableCountQry, Integer.class);
					if (navailablecount >= nreqnoofsamples) {

						final String sQuery = " lock  table lockbiobgsiecatalogue "
								+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
						jdbcTemplate.execute(sQuery);

						int seqNoBgsiECatDetails = jdbcTemplate.queryForObject(
								"select nsequenceno from seqnobiobankmanagement where stablename in (N'biobgsiecataloguedetails') and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
								Integer.class);
						seqNoBgsiECatDetails = seqNoBgsiECatDetails + 1;

						String strBGSiECatalogueDetails = "INSERT INTO public.biobgsiecataloguedetails("
								+ "	nbgsiecatdetailcode, nbgsiecatrequestcode, nbioprojectcode, nproductcode,"
								+ " sparentsamplecode, sreqminvolume, nreqnoofsamples, naccnoofsamples, saccminvolume, "
								+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)"
								+ "	VALUES (" + seqNoBgsiECatDetails + ", " + nbgsiecatrequestcode + ", "
								+ objBioBGSiECatalogue.getNbioprojectcode() + ", " + nproductcode + ","
								+ sparentsamplecode + ", " + sreqminvolumewithquotes + ", " + nreqnoofsamples
								+ ", null, null," + " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ userInfo.getNtimezonecode() + ", "
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
								+ userInfo.getNtranssitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

						String strSeqNoUpdate = "update seqnobiobankmanagement set nsequenceno=" + seqNoBgsiECatDetails
								+ " where" + " stablename='biobgsiecataloguedetails' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						jdbcTemplate.execute(strBGSiECatalogueDetails + strSeqNoUpdate);
						Map<String, Object> outputMap = new HashMap<String, Object>();
						outputMap.putAll(
								(Map<String, Object>) getActiveBGSiECatalogueRequestForm(inputMap, userInfo).getBody());
						outputMap.putAll(
								(Map<String, Object>) getProductComboDataForSampleAdd(inputMap, userInfo).getBody());

						final List<Object> listBeforeUpdate = new ArrayList<>();
						final List<String> multilingualIDList = new ArrayList<>();
						inputMap.put("nbgsiecatdetailcode", seqNoBgsiECatDetails);
						BioBGSiECatalogueDetails objBioBGSiECatalogueDetails = (BioBGSiECatalogueDetails) getActiveSampleDetail(
								inputMap, userInfo).getBody().get("selectedBioBGSiECatalogueDetails");

						listBeforeUpdate.add(objBioBGSiECatalogueDetails);
						multilingualIDList.add("IDS_ADDBGSIECATALOGUEREQUESTSAMPLE");
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
	public ResponseEntity<Object> updateBGSiECatalogueRequestSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final int nbgsiecatrequestcode = Integer.valueOf(inputMap.get("nbgsiecatrequestcode").toString());
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		BioBGSiECatalogue objBioBGSiECatalogue = getActiveBGSiECatalogueRequestForValidation(nbgsiecatrequestcode,
				userInfo);
		if (objBioBGSiECatalogue != null && objBioBGSiECatalogue
				.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			final int nreqformtypecode = objBioBGSiECatalogue.getNreqformtypecode();

			BioBGSiECatalogueDetails objBioBGSiECatalogueDetails = (BioBGSiECatalogueDetails) getActiveSampleDetail(
					inputMap, userInfo).getBody().get("selectedBioBGSiECatalogueDetails");

			if (objBioBGSiECatalogueDetails != null) {

				listBeforeUpdate.add(objBioBGSiECatalogueDetails);

				String sparentsamplecode = inputMap.containsKey("sparentsamplecode")
						&& inputMap.get("sparentsamplecode") != null && inputMap.get("sparentsamplecode") != "-"
						&& objBioBGSiECatalogue.getNreqformtypecode() == Enumeration.RequestFormType.PARENTSAMPLEBASED
								.getRequestFormType()
										? "'" + stringUtilityFunction
												.replaceQuote(inputMap.get("sparentsamplecode").toString()) + "'"
										: null;

				if (nreqformtypecode == Enumeration.RequestFormType.BIOSAMPLEBASED.getRequestFormType()
						|| (nreqformtypecode == Enumeration.RequestFormType.PARENTSAMPLEBASED.getRequestFormType()
								&& sparentsamplecode != null && !sparentsamplecode.toString().trim().isEmpty())) {

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
					String sreqminvolume = stringUtilityFunction.replaceQuote(inputMap.get("sreqminvolume").toString())
							.trim();
					final String castExprB = "REPLACE(TRIM(" + sreqminvolumewithquotes + " COLLATE \"default\"), '"
							+ opForReplace + "', '.')::numeric";
					String parentSampleConditionStr = sparentsamplecode != null && objBioBGSiECatalogue
							.getNreqformtypecode() == Enumeration.RequestFormType.PARENTSAMPLEBASED.getRequestFormType()
									? " and st.sparentsamplecode = " + sparentsamplecode
									: "";

					String availableCountQry = "SELECT COUNT(st.nsamplestoragetransactioncode) "
							+ "FROM samplestoragetransaction st "
							+ "JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid "
							+ "  AND bd.nissampleaccesable = "
							+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " " + "  AND bd.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "  AND bd.nsitecode = " + userInfo.getNmastersitecode() + " WHERE st.nprojecttypecode = "
							+ objBioBGSiECatalogue.getNbioprojectcode() + " " + "  AND st.nproductcode = "
							+ objBioBGSiECatalogueDetails.getNproductcode() + " " + "  AND st.nsitecode = "
							+ objBioBGSiECatalogue.getNreceiversitecode() + " " + "  AND (CASE "
							+ "         WHEN TRIM(st.sqty COLLATE \"default\") ~ (" + patternLiteral + ") "
							+ "         THEN " + castExprSt + " " + "         ELSE NULL " + "       END) >= (CASE "
							+ "         WHEN TRIM(" + sreqminvolumewithquotes + " COLLATE \"default\") ~ ("
							+ patternLiteral + ") " + "         THEN " + castExprB + " " + "         ELSE NULL "
							+ "       END) " + "  AND st.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + parentSampleConditionStr
							+ ";";

					final int navailablecount = jdbcTemplate.queryForObject(availableCountQry, Integer.class);

					if (navailablecount >= nreqnoofsamples) {

						final String updQry = "Update biobgsiecataloguedetails set sreqminvolume = "
								+ sreqminvolumewithquotes + ", nreqnoofsamples = " + nreqnoofsamples
								+ ", sparentsamplecode = " + sparentsamplecode + ", dtransactiondate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', noffsetdtransactiondate = "
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
								+ ", ntztransactiondate = " + userInfo.getNtimezonecode()
								+ " where nbgsiecatdetailcode = " + objBioBGSiECatalogueDetails.getNbgsiecatdetailcode()
								+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

						jdbcTemplate.execute(updQry);
						Map<String, Object> outputMap = new HashMap<String, Object>();
						outputMap.putAll((Map<String, Object>) getActiveSampleDetail(inputMap, userInfo).getBody());

						final List<String> multilingualIDList = new ArrayList<>();
						listAfterUpdate.add(outputMap.get("selectedBioBGSiECatalogueDetails"));

						multilingualIDList.add("IDS_EDITBGSIECATALOGUEREQUESTSAMPLE");

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
	public ResponseEntity<Object> deleteBGSiECatalogueRequestSample(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final int nbgsiecatrequestcode = Integer.valueOf(inputMap.get("nbgsiecatrequestcode").toString());

		BioBGSiECatalogue objBioBGSiECatalogue = getActiveBGSiECatalogueRequestForValidation(nbgsiecatrequestcode,
				userInfo);
		if (objBioBGSiECatalogue != null && objBioBGSiECatalogue
				.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			BioBGSiECatalogueDetails objBioBGSiECatalogueDetails = (BioBGSiECatalogueDetails) getActiveSampleDetail(
					inputMap, userInfo).getBody().get("selectedBioBGSiECatalogueDetails");

			if (objBioBGSiECatalogueDetails != null) {

				final String updQry = "Update biobgsiecataloguedetails set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dtransactiondate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', noffsetdtransactiondate = "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", ntztransactiondate = " + userInfo.getNtimezonecode() + " where nbgsiecatdetailcode = "
						+ objBioBGSiECatalogueDetails.getNbgsiecatdetailcode() + " and nsitecode = "
						+ userInfo.getNtranssitecode() + "; ";

				jdbcTemplate.execute(updQry);
				Map<String, Object> outputMap = new HashMap<String, Object>();
				outputMap
						.putAll((Map<String, Object>) getActiveBGSiECatalogueRequestForm(inputMap, userInfo).getBody());

				final List<Object> listAfterUpdate = new ArrayList<>();
				listAfterUpdate.add(objBioBGSiECatalogueDetails);
				final List<String> multilingualIDList = new ArrayList<>();
				multilingualIDList.add("IDS_DELETEBGSIECATALOGUEREQUESTSAMPLE");

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

//	public BigDecimal getAvailableVolume(final int nselectedsitecode, final int nselectedprojectcode,
//			final int nproductcode, UserInfo userInfo) throws Exception {
//
//		final String decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
//				? "."
//				: userInfo.getSdecimaloperator();
//		final String opForRegex = escapeForSqlRegexCharClass(decOperator);
//		final String opForReplace = stringUtilityFunction.replaceQuote(decOperator);
//		final String patternLiteral = "'^[0-9]+([" + opForRegex + "][0-9]+)?$' COLLATE \"default\"";
//		final String castExpr = "REPLACE(TRIM(st.sqty COLLATE \"default\"), '" + opForReplace + "', '.')::numeric";
//		final String strQuery = "SELECT SUM( COALESCE( " + "    CASE "
//				+ "      WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + patternLiteral + ") " + "      THEN "
//				+ castExpr + " " + "      ELSE NULL " + "    END, 0 " + "  ))::numeric" + " AS stotalqty "
//				+ " FROM samplestoragetransaction st"
//				+ " JOIN product p ON p.nproductcode = st.nproductcode AND p.nproductcode = " + nproductcode
//				+ " AND p.nsitecode = " + userInfo.getNmastersitecode() + " AND p.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " JOIN bioproject bp ON bp.nbioprojectcode = st.nprojecttypecode" + " AND bp.nsitecode = "
//				+ userInfo.getNmastersitecode() + " AND bp.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND st.nprojecttypecode = "
//				+ nselectedprojectcode + " JOIN site s ON st.nsitecode = s.nsitecode" + " AND s.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND st.nsitecode = "
//				+ nselectedsitecode;
//
//		BigDecimal stotalqty = jdbcTemplate.queryForObject(strQuery, BigDecimal.class);
//		if (stotalqty == null) {
//			stotalqty = BigDecimal.ZERO;
//		}
//		return stotalqty;
//	}

	public Boolean isProductAvailableInForm(final int nbgsiecatrequestcode, final int nreqformtypecode,
			final int nproductcode, final String sparentsamplecode, final UserInfo userInfo) throws Exception {
		String strQuery = "";

		if (Enumeration.RequestFormType.BIOSAMPLEBASED.getRequestFormType() == nreqformtypecode) {
			strQuery = "SELECT nproductcode FROM biobgsiecataloguedetails where nbgsiecatrequestcode = "
					+ nbgsiecatrequestcode + " and nproductcode = " + nproductcode + " and nsitecode = "
					+ userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		} else if (Enumeration.RequestFormType.PARENTSAMPLEBASED.getRequestFormType() == nreqformtypecode
				&& sparentsamplecode != null) {
			strQuery = "SELECT sparentsamplecode FROM biobgsiecataloguedetails where nbgsiecatrequestcode = "
					+ nbgsiecatrequestcode + " and nproductcode = " + nproductcode + " and sparentsamplecode = "
					+ sparentsamplecode + " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		}
		List<BioBGSiECatalogueDetails> lstproductcode = jdbcTemplate.query(strQuery, new BioBGSiECatalogueDetails());
		if (lstproductcode != null && !lstproductcode.isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public ResponseEntity<Map<String, Object>> getActiveSampleDetail(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		final int nbgsiecatdetailcode = Integer.valueOf(inputMap.get("nbgsiecatdetailcode").toString());
		final String catalogueDetailQry = "select bcd.nbgsiecatdetailcode, bcd.nbgsiecatrequestcode, bc.sformnumber, s.ssitename as sreceivingsitename, bc.nreceiversitecode, "
				+ "bcd.nbioprojectcode, bp.sprojecttitle, bcd.nproductcode, pr.sproductname || ' (' || pc.sproductcatname || ')' as sproductname, "
//				+ "bcd.srequestedvolume, bcd.sacceptedvolume,"
				+ "bcd.sparentsamplecode, bcd.nreqnoofsamples, bcd.sreqminvolume, bcd.naccnoofsamples, bcd.saccminvolume, "
				+ "bcd.dtransactiondate, bcd.ntztransactiondate, bcd.noffsetdtransactiondate, tz1.stimezoneid as stztransactiondate, "
				+ "COALESCE(TO_CHAR(bcd.dtransactiondate,'" + userInfo.getSsitedate() + "'), '') as stransactiondate "
				+ "from biobgsiecataloguedetails bcd, biobgsiecatalogue bc, site s, bioproject bp, product pr, productcategory pc, timezone tz1 "
				+ "where bcd.nbgsiecatdetailcode = " + nbgsiecatdetailcode + " " + "and bcd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and bcd.nsitecode = "
				+ userInfo.getNtranssitecode() + " "
				+ "and bc.nbgsiecatrequestcode = bcd.nbgsiecatrequestcode and bc.nstatus = "
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
				+ "order by bcd.nbgsiecatdetailcode desc";

		final BioBGSiECatalogueDetails objBioBGSiECatalogueDetails = (BioBGSiECatalogueDetails) jdbcUtilityTemplateFunction
				.queryForObject(catalogueDetailQry, BioBGSiECatalogueDetails.class, jdbcTemplate);
		if (objBioBGSiECatalogueDetails != null) {
			returnMap.put("selectedBioBGSiECatalogueDetails", objBioBGSiECatalogueDetails);
//			returnMap.put("stotalqty",
//					getAvailableVolumeForEdit(objBioBGSiECatalogueDetails.getNbgsiecatrequestcode(),
//							objBioBGSiECatalogueDetails.getNbioprojectcode(),
//							objBioBGSiECatalogueDetails.getNproductcode(), userInfo));
		} else {
			returnMap.put("selectedBioBGSiECatalogueDetails", null);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

//	public String getAvailableVolumeForEdit(final int nbgsiecatrequestcode, final int nselectedprojectcode,
//			final int nproductcode, UserInfo userInfo) throws Exception {
//
//		BioBGSiECatalogue objBioBGSiECatalogue = getActiveBGSiECatalogueRequestForValidation(nbgsiecatrequestcode,
//				userInfo);
//		final String decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
//				? "."
//				: userInfo.getSdecimaloperator();
//		final String opForRegex = escapeForSqlRegexCharClass(decOperator);
//		final String opForReplace = stringUtilityFunction.replaceQuote(decOperator);
//		final String patternLiteral = "'^[0-9]+([" + opForRegex + "][0-9]+)?$' COLLATE \"default\"";
//		final String castExpr = "REPLACE(TRIM(st.sqty COLLATE \"default\"), '" + opForReplace + "', '.')::numeric";
//		final String strQuery = "SELECT REPLACE(SUM( COALESCE( " + "    CASE "
//				+ "	WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + patternLiteral + ") THEN " + castExpr + " "
//				+ "      ELSE NULL " + "    END, 0 " + "  ))::text,'.','" + decOperator + "') AS stotalqty  "
//				+ " FROM samplestoragetransaction st"
//				+ " JOIN product p ON p.nproductcode = st.nproductcode AND p.nproductcode = " + nproductcode
//				+ " AND p.nsitecode = " + userInfo.getNmastersitecode() + " AND p.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " JOIN bioproject bp ON bp.nbioprojectcode = st.nprojecttypecode" + " AND bp.nsitecode = "
//				+ userInfo.getNmastersitecode() + " AND bp.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND st.nprojecttypecode = "
//				+ nselectedprojectcode + " JOIN site s ON st.nsitecode = s.nsitecode" + " AND s.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND st.nsitecode = "
//				+ objBioBGSiECatalogue.getNreceiversitecode();
//
//		String stotalqty = jdbcTemplate.queryForObject(strQuery, String.class);
//		if (stotalqty == null) {
//			stotalqty = "0";
//		}
//		return stotalqty;
//	}

	@Override
	public ResponseEntity<Object> getBioSampleAvailability(Map<String, Object> inputMap, UserInfo userInfo) {
//	    try {
		// read inputs (fallbacks)
		Integer nprojecttypecode = inputMap.get("nbioprojectcode") != null
				? Integer.valueOf(inputMap.get("nbioprojectcode").toString().trim())
				: 0;
		Integer nproductcode = inputMap.get("nproductcode") != null
				? Integer.valueOf(inputMap.get("nproductcode").toString().trim())
				: 0;
		Integer nsitecode = inputMap.get("nsitecode") != null
				? Integer.valueOf(inputMap.get("nsitecode").toString().trim())
				: userInfo.getNtranssitecode();

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
		if (inputMap.get("sreqminvolume") != null) {
			sreqminvolume = stringUtilityFunction.replaceQuote(inputMap.get("sreqminvolume").toString()).trim();
		}

		// Build SQL in the same in-line style you use elsewhere
		String sql = "SELECT COUNT(st.nsamplestoragetransactioncode) " + "FROM samplestoragetransaction st "
				+ "JOIN biosubjectdetails bs ON st.ssubjectid = bs.ssubjectid  and bs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bs.nsitecode = "
				+ userInfo.getNmastersitecode() + " WHERE st.nprojecttypecode = " + nprojecttypecode
				+ " AND st.nproductcode = " + nproductcode + " AND st.nsitecode = " + nsitecode;

		if (sreqminvolume != null && !sreqminvolume.isEmpty()) {
			String castExprB = "REPLACE(TRIM('" + sreqminvolume + "' COLLATE \"default\"), '" + opForReplace
					+ "', '.')::numeric";

			sql += " AND (CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + patternLiteral + ") THEN " + castExprSt
					+ " ELSE NULL END) >= (CASE WHEN (TRIM('" + sreqminvolume + "' COLLATE \"default\")) ~ ("
					+ patternLiteral + ") THEN " + castExprB + " ELSE NULL END)";
		}

		sql += " AND st.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND bs.nissampleaccesable = " + Enumeration.TransactionStatus.YES.gettransactionstatus() + ";";

		// Execute and return
		Integer availableCount = jdbcTemplate.queryForObject(sql, Integer.class);

		Map<String, Object> resp = new HashMap<>();
		resp.put("availablecount", availableCount);
		return new ResponseEntity<Object>(resp, HttpStatus.OK);

//	    } catch (Exception ex) {
//	        logger.error("Error in getBioSampleAvailability(inputMap): " + ex.getMessage(), ex);
//	        Map<String, Object> err = new HashMap<>();
//	        err.put("error", ex.getMessage());
//	        return new ResponseEntity<Object>(err, HttpStatus.INTERNAL_SERVER_ERROR);
//	    }
	}

//	@Override
//	public ResponseEntity<Object> getSubjectCountsByProductAndProject(final UserInfo userInfo) throws Exception {
//		final String sallchildsitecodes = getChildSitesFromAllProjects(userInfo);
//		final String sbioprojectcodes = getProjectsofSite(userInfo);
//
//		final String siteInList = (sallchildsitecodes != null && !sallchildsitecodes.trim().isEmpty())
//				? sallchildsitecodes
//				: "-1";
//		final String projectInList = (sbioprojectcodes != null && !sbioprojectcodes.trim().isEmpty()) ? sbioprojectcodes
//				: "-1";
//
//		final int active = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//		final int yes = Enumeration.TransactionStatus.YES.gettransactionstatus();
//		final int masterSite = userInfo.getNmastersitecode();
//
//		final String cte = "WITH unique_subjects AS ( " + "  SELECT DISTINCT "
//				+ "         st.nprojecttypecode AS nbioprojectcode, " + "         st.nproductcode, "
//				+ "         st.ssubjectid " + "  FROM samplestoragetransaction st " + "  JOIN biosubjectdetails bd "
//				+ "    ON bd.ssubjectid = st.ssubjectid " + "   AND bd.nissampleaccesable = " + yes + " "
//				+ "   AND bd.nstatus = " + active + " " + "   AND bd.nsitecode = " + masterSite + " "
//				+ "  WHERE COALESCE(st.ssubjectid,'') <> '' " + "    AND st.nsitecode IN (" + siteInList + ") "
//				+ "    AND st.nstatus = " + active + " " + "), " + "base AS ( " + "  SELECT " + "    us.ssubjectid, "
//				+ "    p.sproductname, " + "    bp.sprojecttitle " + "  FROM unique_subjects us " + "  JOIN product p "
//				+ "    ON p.nproductcode = us.nproductcode " + "   AND p.nsitecode    = " + masterSite + " "
//				+ "   AND p.nstatus      = " + active + " " + "  JOIN bioproject bp "
//				+ "    ON bp.nbioprojectcode = us.nbioprojectcode " + "   AND bp.nbioprojectcode IN (" + projectInList
//				+ ") " + "   AND bp.nsitecode       = " + masterSite + " " + "   AND bp.nstatus         = " + active
//				+ " " + ") ";
//
//		final String sql = cte + "SELECT " + "  sproductname, " + "  sprojecttitle, "
//				+ "  COUNT(DISTINCT ssubjectid) AS ndistinctsubjects " + "FROM base "
//				+ "GROUP BY sproductname, sprojecttitle " + "ORDER BY sproductname, sprojecttitle";
//
//		Map<String, Object> out = new HashMap<>();
//		out.put("rows", jdbcTemplate.queryForList(sql));
//		return new ResponseEntity<>(out, HttpStatus.OK);
//	}
//
//	@Override
//	public ResponseEntity<Object> getSubjectCountsByProductAndDisease(final UserInfo userInfo) throws Exception {
//		final String sallchildsitecodes = getChildSitesFromAllProjects(userInfo);
//		final String sbioprojectcodes = getProjectsofSite(userInfo);
//
//		final String siteInList = (sallchildsitecodes != null && !sallchildsitecodes.trim().isEmpty())
//				? sallchildsitecodes
//				: "-1";
//		final String projectInList = (sbioprojectcodes != null && !sbioprojectcodes.trim().isEmpty()) ? sbioprojectcodes
//				: "-1";
//
//		final int active = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//		final int masterSite = userInfo.getNmastersitecode();
//
//		final String cte = "WITH unique_subjects AS ( " + "  SELECT DISTINCT "
//				+ "         st.nprojecttypecode AS nbioprojectcode, " + "         st.nproductcode, "
//				+ "         st.ssubjectid " + "  FROM samplestoragetransaction st "
//				+ "  JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid " + "    AND bd.nissampleaccesable = "
//				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " " + "    AND bd.nstatus = " + active
//				+ " " + "    AND bd.nsitecode = " + masterSite + " " + "  WHERE COALESCE(st.ssubjectid,'') <> '' "
//				+ "    AND st.nsitecode IN (" + siteInList + ") " + "    AND st.nstatus = " + active + " " + "), "
//				+ "base AS ( " + "  SELECT " + "    us.ssubjectid, " + "    p.sproductname, "
//				+ "    COALESCE(d.sdiseasename, dc.sdiseasecategoryname) AS sgroup " + "  FROM unique_subjects us "
//				+ "  JOIN product p " + "    ON p.nproductcode = us.nproductcode " + "   AND p.nsitecode    = "
//				+ masterSite + " " + "   AND p.nstatus      = " + active + " " + "  JOIN bioproject bp "
//				+ "    ON bp.nbioprojectcode = us.nbioprojectcode " + "   AND bp.nbioprojectcode IN (" + projectInList
//				+ ") " + "   AND bp.nsitecode       = " + masterSite + " " + "   AND bp.nstatus         = " + active
//				+ " " + "  JOIN diseasecategory dc " + "    ON dc.ndiseasecategorycode = bp.ndiseasecategorycode "
//				+ "   AND dc.nsitecode            = " + masterSite + " " + "   AND dc.nstatus              = " + active
//				+ " " + "  LEFT JOIN disease d " + "    ON d.ndiseasecode = bp.ndiseasecode "
//				+ "   AND d.nsitecode    = " + masterSite + " " + "   AND d.nstatus      = " + active + " " + ") ";
//
//		final String sql = cte + "SELECT " + "  sproductname, " + "  sgroup, "
//				+ "  COUNT(DISTINCT ssubjectid) AS ndistinctsubjects " + "FROM base " + "GROUP BY sproductname, sgroup "
//				+ "ORDER BY sproductname, sgroup";
//
//		Map<String, Object> out = new HashMap<>();
//		out.put("rows", jdbcTemplate.queryForList(sql));
//		return new ResponseEntity<>(out, HttpStatus.OK);
//	}

	@Override
	public ResponseEntity<Object> getSubjectCountsByProductAndProject(final UserInfo userInfo) throws Exception {
		final String sallchildsitecodes = getChildSitesFromAllProjects(userInfo);
		final String sbioprojectcodes = getProjectsofSite(userInfo);

		final String siteInList = (sallchildsitecodes != null && !sallchildsitecodes.trim().isEmpty())
				? sallchildsitecodes
				: "-1";
		final String projectInList = (sbioprojectcodes != null && !sbioprojectcodes.trim().isEmpty()) ? sbioprojectcodes
				: "-1";

		final int active = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final int yes = Enumeration.TransactionStatus.YES.gettransactionstatus();
		final int masterSite = userInfo.getNmastersitecode();

		final String cte = "WITH unique_subjects AS ("
				+ " SELECT DISTINCT st.nprojecttypecode AS nbioprojectcode, st.nproductcode, st.ssubjectid"
				+ " FROM samplestoragetransaction st" + " JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid"
				+ " AND bd.nissampleaccesable = " + yes + " AND bd.nstatus = " + active + " AND bd.nsitecode = "
				+ masterSite + " WHERE COALESCE(st.ssubjectid,'') <> ''" + " AND st.nsitecode IN (" + siteInList + ")"
				+ " AND st.nstatus = " + active + "), base AS (" + " SELECT us.ssubjectid, us.nproductcode,"
				+ " (p.sproductname || ' (' || COALESCE(pc.sproductcatname,'') || ')') AS sproductname,"
				+ " bp.sprojecttitle" + " FROM unique_subjects us"
				+ " JOIN product p ON p.nproductcode = us.nproductcode" + " AND p.nsitecode = " + masterSite
				+ " AND p.nstatus = " + active
				+ " LEFT JOIN productcategory pc ON pc.nproductcatcode = p.nproductcatcode" + " AND pc.nsitecode = "
				+ masterSite + " AND pc.nstatus = " + active
				+ " JOIN bioproject bp ON bp.nbioprojectcode = us.nbioprojectcode" + " AND bp.nbioprojectcode IN ("
				+ projectInList + ")" + " AND bp.nsitecode = " + masterSite + " AND bp.nstatus = " + active + ")";

		final String sql = cte
				+ " SELECT sproductname, sprojecttitle, nproductcode, COUNT(DISTINCT ssubjectid) AS ndistinctsubjects"
				+ " FROM base GROUP BY sproductname, sprojecttitle, nproductcode"
				+ " ORDER BY sproductname, sprojecttitle";

		Map<String, Object> out = new HashMap<>();
		out.put("rows", jdbcTemplate.queryForList(sql));
		return new ResponseEntity<>(out, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSubjectCountsByProductAndDisease(final UserInfo userInfo) throws Exception {
		final String sallchildsitecodes = getChildSitesFromAllProjects(userInfo);
		final String sbioprojectcodes = getProjectsofSite(userInfo);

		final String siteInList = (sallchildsitecodes != null && !sallchildsitecodes.trim().isEmpty())
				? sallchildsitecodes
				: "-1";
		final String projectInList = (sbioprojectcodes != null && !sbioprojectcodes.trim().isEmpty()) ? sbioprojectcodes
				: "-1";

		final int active = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final int masterSite = userInfo.getNmastersitecode();

		final String cte = "WITH unique_subjects AS ("
				+ " SELECT DISTINCT st.nprojecttypecode AS nbioprojectcode, st.nproductcode, st.ssubjectid"
				+ " FROM samplestoragetransaction st" + " JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid"
				+ " AND bd.nissampleaccesable = " + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " AND bd.nstatus = " + active + " AND bd.nsitecode = " + masterSite
				+ " WHERE COALESCE(st.ssubjectid,'') <> ''" + " AND st.nsitecode IN (" + siteInList + ")"
				+ " AND st.nstatus = " + active + "), base AS (" + " SELECT us.ssubjectid, us.nproductcode,"
				+ " (p.sproductname || ' (' || COALESCE(pc.sproductcatname,'') || ')') AS sproductname,"
				+ " COALESCE(d.sdiseasename, dc.sdiseasecategoryname) AS sgroup" + " FROM unique_subjects us"
				+ " JOIN product p ON p.nproductcode = us.nproductcode" + " AND p.nsitecode = " + masterSite
				+ " AND p.nstatus = " + active
				+ " LEFT JOIN productcategory pc ON pc.nproductcatcode = p.nproductcatcode" + " AND pc.nsitecode = "
				+ masterSite + " AND pc.nstatus = " + active
				+ " JOIN bioproject bp ON bp.nbioprojectcode = us.nbioprojectcode" + " AND bp.nbioprojectcode IN ("
				+ projectInList + ")" + " AND bp.nsitecode = " + masterSite + " AND bp.nstatus = " + active
				+ " JOIN diseasecategory dc ON dc.ndiseasecategorycode = bp.ndiseasecategorycode"
				+ " AND dc.nsitecode = " + masterSite + " AND dc.nstatus = " + active
				+ " LEFT JOIN disease d ON d.ndiseasecode = bp.ndiseasecode" + " AND d.nsitecode = " + masterSite
				+ " AND d.nstatus = " + active + ")";

		final String sql = cte
				+ " SELECT sproductname, sgroup, nproductcode, COUNT(DISTINCT ssubjectid) AS ndistinctsubjects"
				+ " FROM base GROUP BY sproductname, sgroup, nproductcode" + " ORDER BY sproductname, sgroup";

		Map<String, Object> out = new HashMap<>();
		out.put("rows", jdbcTemplate.queryForList(sql));
		return new ResponseEntity<>(out, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getProductComboDataForSampleAdd(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final int nbgsiecatrequestcode = Integer.valueOf(inputMap.get("nbgsiecatrequestcode").toString());
		BioBGSiECatalogue objBioBGSiECatalogue = getActiveBGSiECatalogueRequestForValidation(nbgsiecatrequestcode,
				userInfo);

		final String decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
				? "."
				: userInfo.getSdecimaloperator();
		final String opForRegex = escapeForSqlRegexCharClass(decOperator);
		final String opForReplace = stringUtilityFunction.replaceQuote(decOperator);
		final String patternLiteral = "'^[0-9]+([" + opForRegex + "][0-9]+)?$' COLLATE \"default\"";
		final String castExpr = "REPLACE(TRIM(st.sqty COLLATE \"default\"), '" + opForReplace + "', '.')::numeric";

		String strQuery;

		if (Enumeration.RequestFormType.PARENTSAMPLEBASED.getRequestFormType() == objBioBGSiECatalogue
				.getNreqformtypecode()) {
			// Parent-based: DO NOT exclude products already used; allow same product with
			// different parent codes.
			strQuery = "SELECT p.nproductcode, (p.sproductname || ' (' || pc.sproductcatname || ')') AS sproductname"
					+ " FROM samplestoragetransaction st JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid"
					+ " AND bd.nsitecode = " + userInfo.getNmastersitecode() + " AND bd.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND bd.nissampleaccesable = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " JOIN product p ON p.nproductcode = st.nproductcode" + " AND p.nsitecode   = "
					+ userInfo.getNmastersitecode() + " AND p.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " JOIN productcategory pc ON pc.nproductcatcode = p.nproductcatcode" + " AND pc.nsitecode = "
					+ userInfo.getNmastersitecode() + " AND pc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " WHERE COALESCE(TRIM(st.sparentsamplecode), '') <> '' AND st.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND st.nprojecttypecode = "
					+ objBioBGSiECatalogue.getNbioprojectcode() + " AND st.nsitecode = "
					+ objBioBGSiECatalogue.getNreceiversitecode()
					+ " AND NOT EXISTS (SELECT 1 FROM biobgsiecataloguedetails d" + " WHERE d.nbgsiecatrequestcode = "
					+ nbgsiecatrequestcode
					+ " AND d.nproductcode = st.nproductcode AND COALESCE(TRIM(d.sparentsamplecode), '') = COALESCE(TRIM(st.sparentsamplecode), '')"
					+ " AND d.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
					+ " GROUP BY p.nproductcode, p.sproductname, pc.sproductcatname ORDER BY p.sproductname";
		} else {
			// Bio-sample based: keep ORIGINAL behavior — exclude products already added to
			// the request
			strQuery = "SELECT bp.sprojecttitle, bp.nbioprojectcode, st.nsitecode, s.ssitename, p.sproductname || ' ('|| pc.sproductcatname || ')' as sproductname, st.nproductcode, "
					+ "       COUNT(1) AS ntotalsamplecount, "
					+ "       REPLACE(SUM(COALESCE(CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + patternLiteral
					+ ") " + "                 THEN " + castExpr + " ELSE NULL END, 0))::text,'.','" + decOperator
					+ "') AS stotalqty " + "FROM samplestoragetransaction st "
					+ " JOIN biosubjectdetails bd ON bd.ssubjectid = st.ssubjectid" + " AND bd.nsitecode = "
					+ userInfo.getNmastersitecode() + " AND bd.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND bd.nissampleaccesable = "
					+ Enumeration.TransactionStatus.YES.gettransactionstatus()
					+ " JOIN product p ON p.nproductcode = st.nproductcode AND p.nproductcode > 0 "
					+ "  AND p.nsitecode = " + userInfo.getNmastersitecode() + " AND p.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "JOIN productcategory pc ON pc.nproductcatcode = p.nproductcatcode " + "  AND pc.nsitecode = "
					+ userInfo.getNmastersitecode() + " AND pc.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "LEFT JOIN biobgsiecataloguedetails bcd ON bcd.nproductcode = p.nproductcode "
					+ "  AND bcd.nbgsiecatrequestcode = " + nbgsiecatrequestcode + " " + "  AND bcd.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "  AND bcd.nsitecode = "
					+ userInfo.getNtranssitecode() + " "
					+ "JOIN bioproject bp ON bp.nbioprojectcode = st.nprojecttypecode AND bp.nbioprojectcode > 0 "
					+ "  AND bp.nsitecode = " + userInfo.getNmastersitecode() + " AND bp.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "JOIN site s ON st.nsitecode = s.nsitecode AND s.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "WHERE st.nsitecode = "
					+ objBioBGSiECatalogue.getNreceiversitecode() + " " + "  AND st.nprojecttypecode = "
					+ objBioBGSiECatalogue.getNbioprojectcode() + " " + "  AND bcd.nproductcode IS NULL "
					+ "GROUP BY bp.sprojecttitle, st.nsitecode, p.sproductname, bp.nbioprojectcode, s.ssitename, st.nproductcode, pc.sproductcatname "
					+ "ORDER BY bp.sprojecttitle, st.nsitecode, p.sproductname;";
		}

		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("lstProduct", jdbcTemplate.queryForList(strQuery));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

//	@Override
//	public ResponseEntity<Object> getParentSamples(Map<String, Object> inputMap, UserInfo userInfo) {
//		final String decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
//				? "."
//				: userInfo.getSdecimaloperator();
//		final String opForRegex = escapeForSqlRegexCharClass(decOperator);
//		final String opForReplace = stringUtilityFunction.replaceQuote(decOperator);
//		final String patternLiteral = "'^[0-9]+([" + opForRegex + "][0-9]+)?$' COLLATE \"default\"";
//		final String castExprSt = "REPLACE(TRIM(st.sqty COLLATE \"default\"), '" + opForReplace + "', '.')::numeric";
//
//		final Integer nprojecttypecode = inputMap.get("nbioprojectcode") != null
//				? Integer.valueOf(inputMap.get("nbioprojectcode").toString().trim())
//				: (inputMap.get("nprojecttypecode") != null
//						? Integer.valueOf(inputMap.get("nprojecttypecode").toString().trim())
//						: 0);
//		final Integer nproductcode = inputMap.get("nproductcode") != null
//				? Integer.valueOf(inputMap.get("nproductcode").toString().trim())
//				: 0;
//		final Integer nsitecode = inputMap.get("nsitecode") != null
//				? Integer.valueOf(inputMap.get("nsitecode").toString().trim())
//				: userInfo.getNtranssitecode();
//
//		// Include request code for filtering out already-used parent combos
//		final Integer nbgsiecatrequestcode = inputMap.get("nbgsiecatrequestcode") != null
//				? Integer.valueOf(inputMap.get("nbgsiecatrequestcode").toString().trim())
//				: null;
//
//		// optional min volume filter
//		String sreqminvolume = null;
//		if (inputMap.get("sreqminvolume") != null) {
//			sreqminvolume = stringUtilityFunction.replaceQuote(inputMap.get("sreqminvolume").toString()).trim();
//		}
//
//		// optional exact parent sample code condition
//		String parentSampleConditionStr = "";
//		if (inputMap.get("sparentsamplecode") != null) {
//			String sparentsample = stringUtilityFunction.replaceQuote(inputMap.get("sparentsamplecode").toString())
//					.trim();
//			if (!sparentsample.isEmpty()) {
//				parentSampleConditionStr = " AND st.sparentsamplecode = '" + sparentsample + "' ";
//			}
//		}
//
//		StringBuilder sql = new StringBuilder();
//		sql.append("SELECT st.sparentsamplecode AS sparentsamplecode, ")
//				.append("COUNT(st.nsamplestoragetransactioncode) AS availablecount ")
//				.append("FROM samplestoragetransaction st ")
//				.append("JOIN biosubjectdetails bs ON st.ssubjectid = bs.ssubjectid and bs.nsitecode = ")
//				.append(userInfo.getNmastersitecode()).append(" AND bs.nstatus = ")
//				.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
//				.append(" WHERE st.nprojecttypecode = ").append(nprojecttypecode).append(" AND st.nproductcode = ")
//				.append(nproductcode).append(" AND st.nsitecode = ").append(nsitecode).append(parentSampleConditionStr);
//
//		if (sreqminvolume != null && !sreqminvolume.isEmpty()) {
//			final String castExprB = "REPLACE(TRIM('" + sreqminvolume + "' COLLATE \"default\"), '" + opForReplace
//					+ "', '.')::numeric";
//			sql.append(" AND (CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (").append(patternLiteral)
//					.append(") THEN ").append(castExprSt).append(" ELSE NULL END) >= (CASE WHEN (TRIM('")
//					.append(sreqminvolume).append("' COLLATE \"default\")) ~ (").append(patternLiteral)
//					.append(") THEN ").append(castExprB).append(" ELSE NULL END)");
//		}
//
//		// Exclude already-used (product + parent) combos in this request
//		if (nbgsiecatrequestcode != null && nbgsiecatrequestcode > 0) {
//			sql.append(" AND NOT EXISTS ( ").append("SELECT 1 FROM biobgsiecataloguedetails d ")
//					.append(" WHERE d.nbgsiecatrequestcode = ").append(nbgsiecatrequestcode)
//					.append("   AND d.nproductcode = st.nproductcode ")
//					.append("   AND COALESCE(d.sparentsamplecode, '') = COALESCE(st.sparentsamplecode, '') ")
//					.append("   AND d.nstatus = ").append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
//					.append(" )");
//		}
//
//		sql.append(" AND st.nstatus = ").append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
//				.append(" AND bs.nissampleaccesable = ")
//				.append(Enumeration.TransactionStatus.YES.gettransactionstatus())
//				.append(" GROUP BY st.sparentsamplecode ").append(" ORDER BY availablecount DESC;");
//
//		List<Map<String, Object>> lstParentSamples = jdbcTemplate.queryForList(sql.toString());
//		return new ResponseEntity<>(lstParentSamples, HttpStatus.OK);
//	}

	@Override
	public ResponseEntity<Object> getParentSamples(final Map<String, Object> inputMap, final UserInfo userInfo) {
		final Map<String, Object> resp = new HashMap<>();

		final int active = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final int yes = Enumeration.TransactionStatus.YES.gettransactionstatus();

		final Integer nbgsiecatrequestcode = inputMap.get("nbgsiecatrequestcode") != null
				? Integer.valueOf(inputMap.get("nbgsiecatrequestcode").toString().trim())
				: -1;

		final Integer nbioprojectcode = inputMap.get("nbioprojectcode") != null
				? Integer.valueOf(inputMap.get("nbioprojectcode").toString().trim())
				: 0;

		final Integer nsitecode = inputMap.get("nsitecode") != null
				? Integer.valueOf(inputMap.get("nsitecode").toString().trim())
				: 0;

		final Integer nproductcode = inputMap.get("nproductcode") != null
				? Integer.valueOf(inputMap.get("nproductcode").toString().trim())
				: 0;

		final String sreqminvolume = inputMap.get("sreqminvolume") != null
				? stringUtilityFunction.replaceQuote(inputMap.get("sreqminvolume").toString().trim())
				: "0";

		final Integer isEditOperation = inputMap.get("isEditOperation") != null
				? Integer.valueOf(inputMap.get("isEditOperation").toString().trim())
				: 4; // 3=Edit, 4=Add

		final Integer nbgsiecatdetailcode = inputMap.get("nbgsiecatdetailcode") != null
				? Integer.valueOf(inputMap.get("nbgsiecatdetailcode").toString().trim())
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

		final int masterSite = userInfo.getNmastersitecode();

		final String sql = "SELECT TRIM(st.sparentsamplecode) AS sparentsamplecode, "
				+ "       COUNT(st.nsamplestoragetransactioncode) AS availablecount "
				+ "  FROM samplestoragetransaction st " + "  JOIN biosubjectdetails bd "
				+ "    ON bd.ssubjectid = st.ssubjectid " + "   AND bd.nissampleaccesable = " + yes + " "
				+ "   AND bd.nstatus = " + active + " " + "   AND bd.nsitecode = " + masterSite + " "
				+ " WHERE COALESCE(TRIM(st.sparentsamplecode),'') <> '' "
				+ "   AND COALESCE(TRIM(st.ssubjectid),'') <> '' " // ensure join key present
				+ "   AND st.nprojecttypecode = " + nbioprojectcode + " " + "   AND st.nsitecode        = " + nsitecode
				+ " " + "   AND st.nproductcode     = " + nproductcode + " " + "   AND st.nstatus          = " + active
				+ " " + "   AND (CASE WHEN (TRIM(st.sqty COLLATE \"default\")) ~ (" + pattern + ") "
				+ "             THEN " + castExprSt + " ELSE NULL END) " + "       >= (CASE WHEN (TRIM('"
				+ sreqminvolume + "' COLLATE \"default\")) ~ (" + pattern + ") " + "                THEN " + castExprReq
				+ " ELSE NULL END) " + "   AND NOT EXISTS ( " + "       SELECT 1 FROM biobgsiecataloguedetails d "
				+ "        WHERE d.nbgsiecatrequestcode = " + nbgsiecatrequestcode + " "
				+ "          AND d.nproductcode         = " + nproductcode + " "
				+ "          AND d.nstatus              = " + active + " "
				+ "          AND LOWER(COALESCE(d.sparentsamplecode,'')) = LOWER(TRIM(st.sparentsamplecode)) "
				+ "          AND ( " + "               " + isEditOperation + " <> 3 "
				+ "               OR d.nbgsiecatdetailcode <> " + nbgsiecatdetailcode + " " + "          ) " + "   ) "
				+ " GROUP BY TRIM(st.sparentsamplecode) " + " ORDER BY TRIM(st.sparentsamplecode) ";

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
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and shcd.schildsitecode is not null"
				+ " and shcd.schildsitecode <> ''";

		final String sallchildsitecodes = (String) jdbcUtilityTemplateFunction.queryForObject(strQuery, String.class,
				jdbcTemplate);

		return sallchildsitecodes;
	}

	@Override
	public ResponseEntity<Object> getBioSampleTypeCombo(final int nselectedprojectcode, final int nselectedsitecode,
			UserInfo userinfo) throws Exception {
//		final int nselectedsitecode = Integer.valueOf(inputMap.get("nselectedsitecode").toString());
//		final int nselectedprojectcode = Integer.valueOf(inputMap.get("nselectedprojectcode").toString());
		String strQuery = "select p.nproductcode, p.sproductname||' ('||pc.sproductcatname||')' as sproductname from samplestoragetransaction st"
				+ " join product p on st.nproductcode = p.nproductcode and p.nsitecode = "
				+ userinfo.getNmastersitecode() + " and p.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join productcategory pc on p.nproductcatcode = pc.nproductcatcode and pc.nsitecode = "
				+ userinfo.getNmastersitecode() + " and pc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join biosubjectdetails bs on bs.ssubjectid = st.ssubjectid and bs.nissampleaccesable = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and bs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bs.nsitecode = "
				+ userinfo.getNmastersitecode() + " where st.nprojecttypecode = " + nselectedprojectcode
				+ " and st.nsitecode = " + nselectedsitecode + " and st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by p.nproductcode, p.sproductname, pc.sproductcatname";

		List<Product> lstBioSampleType = jdbcTemplate.query(strQuery, new Product());
		return new ResponseEntity<>(lstBioSampleType, HttpStatus.OK);

	}

}
