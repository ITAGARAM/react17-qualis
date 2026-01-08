package com.agaramtech.qualis.biobank.service.processedsamplereceiving;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.ContainerType;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.biobank.model.BioParentSampleCollection;
import com.agaramtech.qualis.biobank.model.BioParentSampleReceiving;
import com.agaramtech.qualis.biobank.model.BioSampleReceiving;
import com.agaramtech.qualis.configuration.service.sitehospitalmapping.SiteHospitalMappingDAOImpl;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TransactionDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.StorageInstrument;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class ProcessedSampleReceivingDAOImpl implements ProcessedSampleReceivingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessedSampleReceivingDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final TransactionDAOSupport transactionDAOSupport;
	private final EmailDAOSupport emailDAOSupport;

	private static final Random random = new Random();

	public List<TransactionStatus> getProcessedSampleStatuses(final UserInfo userInfo) throws Exception {

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

	@Override
	public ResponseEntity<Object> getProcessedSampleReceiving(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		List<TransactionStatus> lstTransactionstatus = new ArrayList<>();
		final String currentUIDate = (String) inputMap.get("currentdate");
		if (currentUIDate != null && currentUIDate.trim().length() != 0) {
			returnMap = projectDAOSupport.getDateFromControlProperties(userInfo, currentUIDate, "datetime", "FromDate");
		}
		lstTransactionstatus = getProcessedSampleStatuses(userInfo);
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

		List<BioParentSampleCollection> lstBioParentSampleCollection = getListBioParentSampleCollection(returnMap,
				userInfo);
		if (!lstBioParentSampleCollection.isEmpty()) {
			returnMap.put("lstBioParentSampleCollection", lstBioParentSampleCollection);
			returnMap.put("selectedBioParentSampleCollection", lstBioParentSampleCollection.getFirst());
			returnMap.put("lstBioSampleReceiving", getProcessedSampleListFromParentSample(
					lstBioParentSampleCollection.getFirst().getNbioparentsamplecollectioncode(), userInfo));

		} else {
			returnMap.put("lstBioParentSampleCollection", null);
			returnMap.put("selectedBioParentSampleCollection", null);
			returnMap.put("lstBioSampleReceiving", null);
		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getProcessedSampleByFilterSubmit(Map<String, Object> inputMap, UserInfo userInfo)
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
		List<TransactionStatus> lstTransactionstatus = getProcessedSampleStatuses(userInfo);
		returnMap.put("FilterStatus", lstTransactionstatus);
		returnMap.put("RealFilterStatuslist", lstTransactionstatus);
		List<BioParentSampleCollection> lstBioParentSampleCollection = getListBioParentSampleCollection(inputMap,
				userInfo);
		if (!lstBioParentSampleCollection.isEmpty()) {
			returnMap.put("lstBioParentSampleCollection", lstBioParentSampleCollection);
			returnMap.put("selectedBioParentSampleCollection", lstBioParentSampleCollection.getFirst());
			returnMap.put("lstBioSampleReceiving", getProcessedSampleListFromParentSample(
					lstBioParentSampleCollection.getFirst().getNbioparentsamplecollectioncode(), userInfo));

		} else {
			returnMap.put("lstBioParentSampleCollection", null);
			returnMap.put("selectedBioParentSampleCollection", null);
			returnMap.put("lstBioSampleReceiving", null);
		}

		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	private List<BioParentSampleCollection> getListBioParentSampleCollection(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {

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
			filterQry = " and bsc.ntransactionstatus = " + nfilterstatus;
		}

		// Commented by Gowtham on nov 14 2025 for jira.id:BGSI-216
		// added scolorhexcode by sujatha ATE_274 BGSI-148 for color work
//		final String sampleQry = "select concat( bsr.sparentsamplecode,'-',bsr.ncohortno,'-',pc.sproductcatname) as sbiosampledisplay, "
//				+ "bsr.ssubjectid, bsr.sparentsamplecode, bsc.nbioparentsamplecollectioncode, bsr.scasetype, bsr.ncohortno, bsr.nbioparentsamplecode, "
//				+ "bsr.ncollectionsitecode, s.ssitename as scollectionsitename, bsr.ncollectedhospitalcode, h.shospitalname, "
//				+ "bsr.nbioprojectcode, p.sprojecttitle, p.sprojectcode, "
//				+ "bsr.nstorageinstrumentcode, i.sinstrumentid, sc.sstorageconditionname as sstoragetemperature, "
//				+ "bsc.nproductcatcode, pc.sproductcatname, rc.ssitename as sreceivingsitename, bsc.nnoofsamples, "
//				+ "sc1.sstorageconditionname, " + "bsc.nstorageconditioncode, " + "bsc.scollectorname, "
//				+ "bsc.stemporarystoragename, bsc.ssendername, bsc.nrecipientusercode, concat(ru.sfirstname,' ',ru.slastname) as srecipientusername, "
//				+ "bsc.ntransactionstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
//				+ userInfo.getSlanguagetypecode()
//				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus, "
//				+ "bsc.dsamplecollectiondate, bsc.ntzsamplecollectiondate, bsc.noffsetsamplecollectiondate, tz1.stimezoneid AS stzsamplecollectiondate, "
//				+ "COALESCE(TO_CHAR(bsc.dsamplecollectiondate,'" + userInfo.getSsitedate()
//				+ "'), '') AS ssamplecollectiondate, "
//				+ "bsc.dbiobankarrivaldate, bsc.ntzbiobankarrivaldate, bsc.noffsetbiobankarrivaldate, tz2.stimezoneid AS stzbiobankarrivaldate, "
//				+ "COALESCE(TO_CHAR(bsc.dbiobankarrivaldate,'" + userInfo.getSsitedate()
//				+ "'), '') AS sbiobankarrivaldate, "
//				+ "bsc.dtemporarystoragedate, bsc.ntztemporarystoragedate, bsc.noffsettemporarystoragedate, tz3.stimezoneid AS stztemporarystoragedate, "
//				+ "COALESCE(TO_CHAR(bsc.dtemporarystoragedate,'" + userInfo.getSsitedate()
//				+ "'), '') AS stemporarystoragedate, "
//				+ "bsc.dtransactiondate, bsc.ntztransactiondate, bsc.noffsettransactiondate, tz4.stimezoneid AS stztransactiondate, "
//				+ "COALESCE(TO_CHAR(bsc.dtransactiondate,'" + userInfo.getSsitedate() + "'), '') AS stransactiondate, cm.scolorhexcode "
//				+ "from bioparentsamplereceiving bsr, bioparentsamplecollection bsc, site s, hospital h, bioproject p, "
//				+ "storageinstrument si, storagecondition sc, storagecondition sc1, instrument i, productcategory pc, site rc, users ru, "
//				+ "transactionstatus ts, timezone tz1, timezone tz2, timezone tz3, timezone tz4, formwisestatuscolor fwsc, colormaster cm "
//				+ "where bsr.ncollectionsitecode = s.nsitecode and s.nmastersitecode = " + userInfo.getNmastersitecode()
//				+ " and s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and  bsr.ncollectedhospitalcode = h.nhospitalcode and h.nsitecode = " + userInfo.getNmastersitecode()
//				+ " and h.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsr.nbioprojectcode = p.nbioprojectcode and p.nsitecode = " + userInfo.getNmastersitecode()
//				+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsr.nstorageinstrumentcode = si.nstorageinstrumentcode and si.nstorageconditioncode = sc.nstorageconditioncode "
//				+ "and si.ninstrumentcode = i.ninstrumentcode and si.nregionalsitecode in ("
//				+ userInfo.getNtranssitecode() + ",-1) and sc.nsitecode = " + userInfo.getNmastersitecode()
//				+ " and sc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and i.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and si.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ " and bsc.nstorageconditioncode = sc1.nstorageconditioncode and sc1.nsitecode = "
//				+ userInfo.getNmastersitecode() + " and sc1.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsr.nbioparentsamplecode = bsc.nbioparentsamplecode and bsc.nsitecode = "
//				+ userInfo.getNtranssitecode() + " and bsc.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsc.nproductcatcode = pc.nproductcatcode and pc.nsitecode = " + userInfo.getNmastersitecode()
//				+ " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsr.nsitecode = " + userInfo.getNtranssitecode() + " and bsr.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and bsc.nreceivingsitecode = rc.nsitecode and rc.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsc.nrecipientusercode =ru.nusercode and ru.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsc.ntransactionstatus = ts.ntranscode "
//				+ "and bsc.ntzsamplecollectiondate = tz1.ntimezonecode and tz1.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsc.ntzbiobankarrivaldate = tz2.ntimezonecode and tz2.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsc.ntztemporarystoragedate = tz3.ntimezonecode and tz3.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsc.ntztransactiondate = tz4.ntimezonecode and tz4.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + filterQry
//				+ " and bsc.dtransactiondate between '" + fromDate + "' and '" + toDate
//				+ "' and fwsc.ntranscode=ts.ntranscode and fwsc.ncolorcode=cm.ncolorcode "
//				+ " and fwsc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and fwsc.nsitecode="+userInfo.getNmastersitecode()
//				+ " and cm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " order by nbioparentsamplecollectioncode desc";
		
		final String sampleQry = "SELECT CONCAT(bsr.sparentsamplecode, '-', bsr.ncohortno, '-', pc.sproductcatname) AS sbiosampledisplay, "
				+ " bsr.ssubjectid, bsr.sparentsamplecode, bsd.nisthirdpartysharable, bsd.nissampleaccesable, bsr.ncollectionsitecode, "
				+ " s.ssitename AS scollectionsitename, CONCAT('" + commonFunction.getMultilingualMessage("IDS_THIRDPARTYSHARABLE",
				userInfo.getSlanguagefilename()) + "',COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" 
				+ userInfo.getSlanguagetypecode() + "',ts1.jsondata->'stransdisplaystatus'->>'en-US')) as sisthirdpartysharable, "
				+ " CONCAT('" + commonFunction.getMultilingualMessage("IDS_SAMPLEACCESABLE", userInfo.getSlanguagefilename())
				+ "',COALESCE(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() 
				+ " ',ts2.jsondata->'stransdisplaystatus'->>'en-US')) as sissampleaccesable, bsc.nbioparentsamplecollectioncode, "
				+ " bsr.scasetype, bsr.ncohortno, bsr.nbioparentsamplecode, bsr.ncollectedhospitalcode, h.shospitalname, "
				+ " bsr.nbioprojectcode, p.sprojecttitle, p.sprojectcode, bsr.nstorageinstrumentcode, i.sinstrumentid, "
				+ " sc.sstorageconditionname AS sstoragetemperature, bsc.nproductcatcode, pc.sproductcatname, "
				+ " rc.ssitename AS sreceivingsitename, bsc.nnoofsamples, sc1.sstorageconditionname, bsc.nstorageconditioncode, "
				+ " bsc.scollectorname, bsc.stemporarystoragename, bsc.ssendername, bsc.nrecipientusercode, "
				+ " CONCAT(ru.sfirstname, ' ', ru.slastname) AS srecipientusername, bsc.ntransactionstatus, "
				+ " COALESCE(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() 
				+ " ',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus, bsc.dsamplecollectiondate, "
				+ " bsc.ntzsamplecollectiondate, bsc.noffsetsamplecollectiondate, tz1.stimezoneid AS stzsamplecollectiondate, "
				+ " COALESCE(TO_CHAR(bsc.dsamplecollectiondate,'" + userInfo.getSsitedate() + "'), '') AS ssamplecollectiondate, "
				+ " bsc.dbiobankarrivaldate, bsc.ntzbiobankarrivaldate, bsc.noffsetbiobankarrivaldate, "
				+ " tz2.stimezoneid AS stzbiobankarrivaldate, COALESCE(TO_CHAR(bsc.dbiobankarrivaldate,'" 
				+ userInfo.getSsitedate() + "'), '') AS sbiobankarrivaldate, bsc.dtemporarystoragedate, bsc.ntztemporarystoragedate, "
				+ " bsc.noffsettemporarystoragedate, tz3.stimezoneid AS stztemporarystoragedate, COALESCE(TO_CHAR(bsc.dtemporarystoragedate,'" 
				+ userInfo.getSsitedate() + "'), '') AS stemporarystoragedate, bsc.dtransactiondate, bsc.ntztransactiondate, "
				+ " bsc.noffsettransactiondate, tz4.stimezoneid AS stztransactiondate, COALESCE(TO_CHAR(bsc.dtransactiondate,'" 
				+ userInfo.getSsitedate() + "'), '') AS stransactiondate, cm.scolorhexcode FROM bioparentsamplereceiving bsr "
				+ " JOIN bioparentsamplecollection bsc ON bsr.nbioparentsamplecode = bsc.nbioparentsamplecode "
				+ " JOIN site s ON bsr.ncollectionsitecode = s.nsitecode "
				+ " JOIN hospital h ON bsr.ncollectedhospitalcode = h.nhospitalcode "
				+ " JOIN bioproject p ON bsr.nbioprojectcode = p.nbioprojectcode "
				+ " JOIN biosubjectdetails bsd ON bsr.ssubjectid = bsd.ssubjectid "
				+ " JOIN storageinstrument si ON bsr.nstorageinstrumentcode = si.nstorageinstrumentcode "
				+ " JOIN storagecondition sc ON si.nstorageconditioncode = sc.nstorageconditioncode "
				+ " JOIN instrument i ON si.ninstrumentcode = i.ninstrumentcode "
				+ " JOIN storagecondition sc1 ON bsc.nstorageconditioncode = sc1.nstorageconditioncode "
				+ " JOIN productcategory pc ON bsc.nproductcatcode = pc.nproductcatcode "
				+ " JOIN site rc ON bsc.nreceivingsitecode = rc.nsitecode "
				+ " JOIN users ru ON bsc.nrecipientusercode = ru.nusercode "
				+ " JOIN transactionstatus ts ON bsc.ntransactionstatus = ts.ntranscode "
				+ " JOIN timezone tz1 ON bsc.ntzsamplecollectiondate = tz1.ntimezonecode "
				+ " JOIN timezone tz2 ON bsc.ntzbiobankarrivaldate = tz2.ntimezonecode "
				+ " JOIN timezone tz3 ON bsc.ntztemporarystoragedate = tz3.ntimezonecode "
				+ " JOIN timezone tz4 ON bsc.ntztransactiondate = tz4.ntimezonecode "
				+ " JOIN transactionstatus ts1 ON bsd.nisthirdpartysharable = ts1.ntranscode "
				+ " JOIN transactionstatus ts2 ON bsd.nissampleaccesable = ts2.ntranscode "
				+ " JOIN formwisestatuscolor fwsc ON fwsc.ntranscode = ts.ntranscode "
				+ " JOIN colormaster cm ON fwsc.ncolorcode = cm.ncolorcode"
				+ " WHERE s.nmastersitecode = " + userInfo.getNmastersitecode() + " and h.nsitecode = " 
				+ userInfo.getNmastersitecode() + " and bsd.nsitecode = " + userInfo.getNmastersitecode() 
				+ " and p.nsitecode = " + userInfo.getNmastersitecode() + " and si.nregionalsitecode in ("
				+ userInfo.getNtranssitecode() + ",-1) and sc.nsitecode = " + userInfo.getNmastersitecode()
				+ " and bsc.nsitecode = " + userInfo.getNtranssitecode() + " and sc1.nsitecode = " 
				+ userInfo.getNmastersitecode() + " and pc.nsitecode = " + userInfo.getNmastersitecode() 
				+ " and bsr.nsitecode = " + userInfo.getNtranssitecode() + " and fwsc.nsitecode=" 
				+ userInfo.getNmastersitecode() + " and bsc.nreceivingsitecode = rc.nsitecode "
				+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and h.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and i.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and si.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sc1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and bsr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and rc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ru.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tz1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tz2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tz3.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tz4.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and cm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and fwsc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + filterQry
				+ " and bsc.dtransactiondate between '" + fromDate + "' and '" + toDate
				+ "' ORDER BY nbioparentsamplecollectioncode DESC";

		return jdbcTemplate.query(sampleQry, new BioParentSampleCollection());
	}

	@Override
	public ResponseEntity<Map<String, Object>> getActiveSampleCollection(Map<String, Object> inputMap,
			UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		final int nbioparentsamplecollectioncode = Integer
				.valueOf(inputMap.get("nbioparentsamplecollectioncode").toString());
		
		// Commented by Gowtham on nov 14 2025 for jira.id:BGSI-216
		// added scolorhexcode by sujatha ATE_274 BGSI-148 for color work
//		final String sampleQry = "select concat( bsr.sparentsamplecode,'-',bsr.ncohortno,'-',pc.sproductcatname) as sbiosampledisplay, "
//				+ "bsr.ssubjectid, bsr.sparentsamplecode, bsc.nbioparentsamplecollectioncode, bsr.scasetype, bsr.ncohortno, bsr.nbioparentsamplecode, "
//				+ "bsr.ncollectionsitecode, s.ssitename as scollectionsitename, bsr.ncollectedhospitalcode, h.shospitalname, "
//				+ "bsr.nbioprojectcode, p.sprojecttitle, p.sprojectcode, "
//				+ "bsr.nstorageinstrumentcode, i.sinstrumentid, sc.sstorageconditionname as sstoragetemperature, "
//				+ "bsc.nproductcatcode, pc.sproductcatname, rc.ssitename as sreceivingsitename, bsc.nnoofsamples, "
//				+ "sc1.sstorageconditionname, " + "bsc.nstorageconditioncode, " + "bsc.scollectorname, "
//				+ "bsc.stemporarystoragename, bsc.ssendername, bsc.nrecipientusercode, concat(ru.sfirstname,' ',ru.slastname) as srecipientusername, "
//				+ "bsc.ntransactionstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
//				+ userInfo.getSlanguagetypecode()
//				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus, "
//				+ "bsc.dsamplecollectiondate, bsc.ntzsamplecollectiondate, bsc.noffsetsamplecollectiondate, tz1.stimezoneid AS stzsamplecollectiondate, "
//				+ "COALESCE(TO_CHAR(bsc.dsamplecollectiondate,'" + userInfo.getSsitedate()
//				+ "'), '') AS ssamplecollectiondate, "
//				+ "bsc.dbiobankarrivaldate, bsc.ntzbiobankarrivaldate, bsc.noffsetbiobankarrivaldate, tz2.stimezoneid AS stzbiobankarrivaldate, "
//				+ "COALESCE(TO_CHAR(bsc.dbiobankarrivaldate,'" + userInfo.getSsitedate()
//				+ "'), '') AS sbiobankarrivaldate, "
//				+ "bsc.dtemporarystoragedate, bsc.ntztemporarystoragedate, bsc.noffsettemporarystoragedate, tz3.stimezoneid AS stztemporarystoragedate, "
//				+ "COALESCE(TO_CHAR(bsc.dtemporarystoragedate,'" + userInfo.getSsitedate()
//				+ "'), '') AS stemporarystoragedate, "
//				+ "bsc.dtransactiondate, bsc.ntztransactiondate, bsc.noffsettransactiondate, tz4.stimezoneid AS stztransactiondate, "
//				+ "COALESCE(TO_CHAR(bsc.dtransactiondate,'" + userInfo.getSsitedate() + "'), '') AS stransactiondate,  cm.scolorhexcode "
//				+ "from bioparentsamplereceiving bsr, bioparentsamplecollection bsc, site s, hospital h, bioproject p, "
//				+ "storageinstrument si, storagecondition sc, storagecondition sc1, instrument i, productcategory pc, site rc, users ru, "
//				+ "transactionstatus ts, timezone tz1, timezone tz2, timezone tz3, timezone tz4, formwisestatuscolor fwsc, colormaster cm "
//				+ "where bsr.ncollectionsitecode = s.nsitecode and s.nmastersitecode = " + userInfo.getNmastersitecode()
//				+ " and s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and  bsr.ncollectedhospitalcode = h.nhospitalcode and h.nsitecode = " + userInfo.getNmastersitecode()
//				+ " and h.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsr.nbioprojectcode = p.nbioprojectcode and p.nsitecode = " + userInfo.getNmastersitecode()
//				+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsr.nstorageinstrumentcode = si.nstorageinstrumentcode and si.nstorageconditioncode = sc.nstorageconditioncode "
//				+ "and si.ninstrumentcode = i.ninstrumentcode and si.nregionalsitecode in ("
//				+ userInfo.getNtranssitecode() + ",-1) and sc.nsitecode = " + userInfo.getNmastersitecode()
//				+ " and sc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and i.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and si.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ " and bsc.nstorageconditioncode = sc1.nstorageconditioncode and sc1.nsitecode = "
//				+ userInfo.getNmastersitecode() + " and sc1.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsr.nbioparentsamplecode = bsc.nbioparentsamplecode and bsc.nsitecode = "
//				+ userInfo.getNtranssitecode() + " and bsc.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsc.nbioparentsamplecollectioncode = " + nbioparentsamplecollectioncode + " "
//				+ "and bsc.nproductcatcode = pc.nproductcatcode and pc.nsitecode = " + userInfo.getNmastersitecode()
//				+ " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsr.nsitecode = " + userInfo.getNtranssitecode() + " and bsr.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and bsc.nreceivingsitecode = rc.nsitecode and rc.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsc.nrecipientusercode =ru.nusercode and ru.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsc.ntransactionstatus = ts.ntranscode "
//				+ "and bsc.ntzsamplecollectiondate = tz1.ntimezonecode and tz1.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsc.ntzbiobankarrivaldate = tz2.ntimezonecode and tz2.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsc.ntztemporarystoragedate = tz3.ntimezonecode and tz3.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bsc.ntztransactiondate = tz4.ntimezonecode and tz4.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ " and fwsc.ntranscode=ts.ntranscode and fwsc.ncolorcode=cm.ncolorcode"
//				+ " and fwsc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				+ " and fwsc.nsitecode="+userInfo.getNmastersitecode()
//				+ " and cm.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
		
		final String sampleQry = "SELECT CONCAT(bsr.sparentsamplecode, '-', bsr.ncohortno, '-', pc.sproductcatname) AS sbiosampledisplay, "
				+ " bsr.ssubjectid, bsr.sparentsamplecode, bsd.nisthirdpartysharable, bsd.nissampleaccesable, bsr.ncollectionsitecode, "
				+ " s.ssitename AS scollectionsitename, CONCAT('" + commonFunction.getMultilingualMessage("IDS_THIRDPARTYSHARABLE",
				userInfo.getSlanguagefilename()) + "',COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" 
				+ userInfo.getSlanguagetypecode() + "',ts1.jsondata->'stransdisplaystatus'->>'en-US')) as sisthirdpartysharable, "
				+ " CONCAT('" + commonFunction.getMultilingualMessage("IDS_SAMPLEACCESABLE", userInfo.getSlanguagefilename())
				+ " ',COALESCE(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() 
				+ " ',ts2.jsondata->'stransdisplaystatus'->>'en-US')) as sissampleaccesable, bsc.nbioparentsamplecollectioncode, "
				+ " bsr.scasetype, bsr.ncohortno, bsr.nbioparentsamplecode, bsr.ncollectedhospitalcode, h.shospitalname, "
				+ " bsr.nbioprojectcode, p.sprojecttitle, p.sprojectcode, bsr.nstorageinstrumentcode, i.sinstrumentid, "
				+ " sc.sstorageconditionname AS sstoragetemperature, bsc.nproductcatcode, pc.sproductcatname, "
				+ " rc.ssitename AS sreceivingsitename, bsc.nnoofsamples, sc1.sstorageconditionname, bsc.nstorageconditioncode, "
				+ " bsc.scollectorname, bsc.stemporarystoragename, bsc.ssendername, bsc.nrecipientusercode, "
				+ " CONCAT(ru.sfirstname, ' ', ru.slastname) AS srecipientusername, bsc.ntransactionstatus, "
				+ " COALESCE(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() 
				+ " ',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus, bsc.dsamplecollectiondate, "
				+ " bsc.ntzsamplecollectiondate, bsc.noffsetsamplecollectiondate, tz1.stimezoneid AS stzsamplecollectiondate, "
				+ " COALESCE(TO_CHAR(bsc.dsamplecollectiondate,'" + userInfo.getSsitedate() + "'), '') AS ssamplecollectiondate, "
				+ " bsc.dbiobankarrivaldate, bsc.ntzbiobankarrivaldate, bsc.noffsetbiobankarrivaldate, "
				+ " tz2.stimezoneid AS stzbiobankarrivaldate, COALESCE(TO_CHAR(bsc.dbiobankarrivaldate,'" 
				+ userInfo.getSsitedate() + "'), '') AS sbiobankarrivaldate, bsc.dtemporarystoragedate, bsc.ntztemporarystoragedate, "
				+ " bsc.noffsettemporarystoragedate, tz3.stimezoneid AS stztemporarystoragedate, COALESCE(TO_CHAR(bsc.dtemporarystoragedate,'" 
				+ userInfo.getSsitedate() + "'), '') AS stemporarystoragedate, bsc.dtransactiondate, bsc.ntztransactiondate, "
				+ " bsc.noffsettransactiondate, tz4.stimezoneid AS stztransactiondate, COALESCE(TO_CHAR(bsc.dtransactiondate,'" 
				+ userInfo.getSsitedate() + "'), '') AS stransactiondate, cm.scolorhexcode FROM bioparentsamplereceiving bsr "
				+ " JOIN bioparentsamplecollection bsc ON bsr.nbioparentsamplecode = bsc.nbioparentsamplecode "
				+ " JOIN site s ON bsr.ncollectionsitecode = s.nsitecode "
				+ " JOIN hospital h ON bsr.ncollectedhospitalcode = h.nhospitalcode "
				+ " JOIN bioproject p ON bsr.nbioprojectcode = p.nbioprojectcode "
				+ " JOIN biosubjectdetails bsd ON bsr.ssubjectid = bsd.ssubjectid "
				+ " JOIN storageinstrument si ON bsr.nstorageinstrumentcode = si.nstorageinstrumentcode "
				+ " JOIN storagecondition sc ON si.nstorageconditioncode = sc.nstorageconditioncode "
				+ " JOIN instrument i ON si.ninstrumentcode = i.ninstrumentcode "
				+ " JOIN storagecondition sc1 ON bsc.nstorageconditioncode = sc1.nstorageconditioncode "
				+ " JOIN productcategory pc ON bsc.nproductcatcode = pc.nproductcatcode "
				+ " JOIN site rc ON bsc.nreceivingsitecode = rc.nsitecode "
				+ " JOIN users ru ON bsc.nrecipientusercode = ru.nusercode "
				+ " JOIN transactionstatus ts ON bsc.ntransactionstatus = ts.ntranscode "
				+ " JOIN timezone tz1 ON bsc.ntzsamplecollectiondate = tz1.ntimezonecode "
				+ " JOIN timezone tz2 ON bsc.ntzbiobankarrivaldate = tz2.ntimezonecode "
				+ " JOIN timezone tz3 ON bsc.ntztemporarystoragedate = tz3.ntimezonecode "
				+ " JOIN timezone tz4 ON bsc.ntztransactiondate = tz4.ntimezonecode "
				+ " JOIN transactionstatus ts1 ON bsd.nisthirdpartysharable = ts1.ntranscode "
				+ " JOIN transactionstatus ts2 ON bsd.nissampleaccesable = ts2.ntranscode "
				+ " JOIN formwisestatuscolor fwsc ON fwsc.ntranscode = ts.ntranscode "
				+ " JOIN colormaster cm ON fwsc.ncolorcode = cm.ncolorcode"
				+ " WHERE s.nmastersitecode = " + userInfo.getNmastersitecode() + " and h.nsitecode = " 
				+ userInfo.getNmastersitecode() + " and bsd.nsitecode = " + userInfo.getNmastersitecode() 
				+ " and p.nsitecode = " + userInfo.getNmastersitecode() + " and si.nregionalsitecode in ("
				+ userInfo.getNtranssitecode() + ",-1) and sc.nsitecode = " + userInfo.getNmastersitecode()
				+ " and bsc.nsitecode = " + userInfo.getNtranssitecode() + " and sc1.nsitecode = " 
				+ userInfo.getNmastersitecode() + " and pc.nsitecode = " + userInfo.getNmastersitecode() 
				+ " and bsr.nsitecode = " + userInfo.getNtranssitecode() + " and fwsc.nsitecode=" 
				+ userInfo.getNmastersitecode() + " and bsc.nreceivingsitecode = rc.nsitecode "
				+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and h.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and i.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and si.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sc1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and bsr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and pc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and rc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ru.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tz1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tz2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tz3.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tz4.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts1.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and cm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and fwsc.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bsc.nbioparentsamplecollectioncode = " + nbioparentsamplecollectioncode
				+ " ORDER BY nbioparentsamplecollectioncode DESC";

		final BioParentSampleCollection objBioParentSampleCollection = (BioParentSampleCollection) jdbcUtilityTemplateFunction
				.queryForObject(sampleQry, BioParentSampleCollection.class, jdbcTemplate);
		if (objBioParentSampleCollection != null) {
			returnMap.put("selectedBioParentSampleCollection", objBioParentSampleCollection);
			returnMap.put("lstBioSampleReceiving",
					getProcessedSampleListFromParentSample(nbioparentsamplecollectioncode, userInfo));
		} else {
			returnMap.put("selectedBioParentSampleCollection", null);
			returnMap.put("lstBioSampleReceiving", null);
		}
		return new ResponseEntity<>(returnMap, HttpStatus.OK);

	}

	public List<BioSampleReceiving> getProcessedSampleListFromParentSample(final int nbioparentsamplecollectioncode,
			final UserInfo userInfo) throws Exception {

		// modified by sujatha ATE_274 for getting the newly added 6 fields in the UI BGSI-218
		final String sampleListQry = "select csr.nbiosamplereceivingcode, csr.nbioparentsamplecollectioncode,csr.nbioparentsamplecode, "
				+ "csr.nproductcode, p.sproductname, csr.ndiagnostictypecode, " + "coalesce(d.jsondata->>'"
				+ userInfo.getSlanguagetypecode() + "',d.jsondata->>'en-US') as sdiagnostictypename, "
				+ "csr.ncontainertypecode, c.scontainertype, csr.nstoragetypecode, " + "coalesce(s.jsondata->>'"
				+ userInfo.getSlanguagetypecode() + "',s.jsondata->>'en-US') as sstoragetypename, "
				+ "csr.srepositoryid, csr.slocationcode, csr.sqty, "
				+ "csr.ntransactionstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus, "
				+ "csr.dreceiveddate, csr.ntzreceiveddate, csr.noffsetreceiveddate, tz1.stimezoneid AS stzreceiveddate, "
				+ "COALESCE(TO_CHAR(csr.dreceiveddate,'" + userInfo.getSsitedate() + "'), '') AS sreceiveddate, "
				+ "csr.dtransactiondate, csr.ntztransactiondate, csr.noffsettransactiondate, tz2.stimezoneid AS stztransactiondate, "
				+ "COALESCE(TO_CHAR(csr.dtransactiondate,'" + userInfo.getSsitedate() + "'), '') AS stransactiondate,  "
				+ "case when csr.sextractedsampleid is null then '-' when csr.sextractedsampleid = '' then '-' else csr.sextractedsampleid end sextractedsampleid, "
				+ "case when csr.sconcentration is null then '-' when csr.sconcentration = '' then '-' else csr.sconcentration end sconcentration, "
				+ "case when csr.sqcplatform is null then '-' when csr.sqcplatform = '' then '-' else csr.sqcplatform end sqcplatform, "
				+ "case when csr.seluent is null then '-' when csr.seluent = '' then '-' else csr.seluent end seluent, "
				+ "case when csr.srefformnumber is null then '-' when csr.srefformnumber = '' then '-' else csr.srefformnumber end srefformnumber, "
				+ "case when csr.sreferencerepoid is null then '-' when csr.sreferencerepoid = '' then '-' else csr.sreferencerepoid end sreferencerepoid "
				+ "from biosamplereceiving csr, product p, diagnostictype d, containertype c, storagetype s, transactionstatus ts, timezone tz1, timezone tz2 "
				+ "where csr.nbioparentsamplecollectioncode = " + nbioparentsamplecollectioncode + " "
				+ "and csr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and csr.nsitecode = " + userInfo.getNtranssitecode() + " "
				+ "and csr.nproductcode = p.nproductcode and p.nsitecode = " + userInfo.getNmastersitecode()
				+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and csr.ndiagnostictypecode = d.ndiagnostictypecode and d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and csr.ncontainertypecode = c.ncontainertypecode and c.nsitecode = " + userInfo.getNmastersitecode()
				+ " and c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and csr.nstoragetypecode = s.nstoragetypecode and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and csr.ntransactionstatus = ts.ntranscode "
				+ "and csr.ntzreceiveddate = tz1.ntimezonecode and tz1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and csr.ntztransactiondate = tz2.ntimezonecode and tz2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by csr.nbiosamplereceivingcode desc";

		return jdbcTemplate.query(sampleListQry, new BioSampleReceiving());
	}

	@Override
	public ResponseEntity<Object> getParentSampleCollectionDataForAdd(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		// Added by Gowtham on nov 14 2025 for jira.id:BGSI-216
		final int nbioparentsamplecollectioncode = (int) inputMap.get("nbioparentsamplecollectioncode");
		
		final String strQuery = "SELECT bsd.nissampleaccesable FROM biosubjectdetails bsd "
				+ " JOIN bioparentsamplecollection bpsc on bpsc.nbioparentsamplecollectioncode=" + nbioparentsamplecollectioncode 
				+ " JOIN bioparentsamplereceiving bpsr on bpsr.nbioparentsamplecode = bpsc.nbioparentsamplecode "
				+ " WHERE bsd.ssubjectid = bpsr.ssubjectid and bsd.nsitecode = " + userInfo.getNmastersitecode() 
				+ " and bpsc.nsitecode=" + userInfo.getNtranssitecode() + " and bpsr.nsitecode=" + userInfo.getNtranssitecode()
				+ " and bsd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and bpsc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and bpsr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		final Integer nissampleaccesable = (Integer) jdbcUtilityTemplateFunction.queryForObject(strQuery, Integer.class, jdbcTemplate);
		
		if (nissampleaccesable != null && (int) nissampleaccesable == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			return new ResponseEntity<>(
				commonFunction.getMultilingualMessage("IDS_SAMPLEISNOTACCESSIBLE", userInfo.getSlanguagefilename()),
				HttpStatus.EXPECTATION_FAILED);
		}
		
		returnMap.putAll((Map<String, Object>) getActiveSampleCollection(inputMap, userInfo).getBody());

		final BioParentSampleCollection objBioParentSampleCollection = (BioParentSampleCollection) returnMap
				.get("selectedBioParentSampleCollection");
		if (objBioParentSampleCollection != null && objBioParentSampleCollection
				.getNtransactionstatus() == Enumeration.TransactionStatus.UNPROCESSED.gettransactionstatus()) {
			returnMap.put("lstProduct", getProductComboData(objBioParentSampleCollection.getNproductcatcode(),
					objBioParentSampleCollection.getNbioparentsamplecollectioncode(), userInfo));
			returnMap.put("lstContainerType", getContainerTypeComboData(userInfo));
			returnMap.put("lstDiagnosticType", getDiagnosticTypeComboData(userInfo));
			returnMap.put("lstStorageType", getStorageTypeComboData(userInfo));
			return new ResponseEntity<>(returnMap, HttpStatus.OK);

		}
		return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTNOTYETPROCESSEDSTATUSRECORD",
				userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
	}

	public List<ContainerType> getContainerTypeComboData(final UserInfo userInfo) throws Exception {
		final String strQry = "select scontainertype, ncontainertypecode from containertype "
				+ "where ncontainertypecode > 0 and nsamplecontainer = "
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode() + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.query(strQry, new ContainerType());
	}

	public List<Product> getProductComboData(final int nproductcatcode, final int nbioparentsamplecollectioncode,
			final UserInfo userInfo) throws Exception {
		final String strQry = "select p.sproductname, p.nproductcode FROM product p "
//				+ "LEFT JOIN biosamplereceiving b ON b.nproductcode = p.nproductcode "
//				+ "AND b.nbioparentsamplecollectioncode = " + nbioparentsamplecollectioncode + " "
//				+ "AND b.nsitecode = " + userInfo.getNtranssitecode() + " AND b.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " 
				+ "WHERE p.nproductcatcode = " + nproductcatcode + " AND p.nproductcode > 0 AND p.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + "AND p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//				+ " AND b.nproductcode IS NULL";
		return jdbcTemplate.query(strQry, new Product());
	}

	public List<Map<String, Object>> getDiagnosticTypeComboData(final UserInfo userInfo) throws Exception {
		final String strQry = "select ndiagnostictypecode, coalesce(jsondata->>'" + userInfo.getSlanguagetypecode()
				+ "', jsondata->>'en-US') as sdiagnostictypename "
				+ "from diagnostictype where ndiagnostictypecode > 0 and nsitecode = " + userInfo.getNmastersitecode()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.queryForList(strQry);
	}

	public List<Map<String, Object>> getStorageTypeComboData(final UserInfo userInfo) throws Exception {
		final String strQry = "select nstoragetypecode, coalesce(jsondata->>'" + userInfo.getSlanguagetypecode()
				+ "', jsondata->>'en-US') as sstoragetypename "
				+ "from storagetype where nstoragetypecode > 0 and nsitecode = " + userInfo.getNmastersitecode()
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.queryForList(strQry);
	}

	@Override
	public ResponseEntity<Object> createProcessedSampleReceiving(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final int naliquotcount = Integer.valueOf(inputMap.get("naliquotcount").toString());
		final int nproductcode = Integer.valueOf(inputMap.get("nproductcode").toString());
		final int ndiagnostictypecode = Integer.valueOf(inputMap.get("ndiagnostictypecode").toString());
		final int ncontainertypecode = Integer.valueOf(inputMap.get("ncontainertypecode").toString());
		final String sreceiveddate = inputMap.containsKey("sreceiveddate")
				&& inputMap.get("sreceiveddate").toString() != null && inputMap.get("sreceiveddate").toString() != ""
						? "'" + inputMap.get("sreceiveddate").toString() + "'"
						: null;
		final BioParentSampleCollection objBioParentSampleCollection = objMapper.convertValue(
				inputMap.get("selectedBioParentSampleCollection"), new TypeReference<BioParentSampleCollection>() {
				});
		
		// Added by Gowtham on nov 14 2025 for jira.id:BGSI-216
		final String strQuery = "SELECT bsd.nissampleaccesable FROM biosubjectdetails bsd "
				+ " JOIN bioparentsamplecollection bpsc on bpsc.nbioparentsamplecollectioncode=" 
				+ objBioParentSampleCollection.getNbioparentsamplecollectioncode()
				+ " JOIN bioparentsamplereceiving bpsr on bpsr.nbioparentsamplecode = bpsc.nbioparentsamplecode "
				+ " WHERE bsd.ssubjectid = bpsr.ssubjectid and bsd.nsitecode = " + userInfo.getNmastersitecode() 
				+ " and bpsc.nsitecode=" + userInfo.getNtranssitecode() + " and bpsr.nsitecode=" + userInfo.getNtranssitecode()
				+ " and bsd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and bpsc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and bpsr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			
		final Integer nissampleaccesable = (Integer) jdbcUtilityTemplateFunction.queryForObject(strQuery, Integer.class, jdbcTemplate);
			
		if (nissampleaccesable != null && (int) nissampleaccesable == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SAMPLEISNOTACCESSIBLE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		
		if (Enumeration.TransactionStatus.UNPROCESSED.gettransactionstatus() == objBioParentSampleCollection
				.getNtransactionstatus()) {
			final int nbioparentsamplecollectioncode = objBioParentSampleCollection.getNbioparentsamplecollectioncode();
			final int nbioparentsamplecode = objBioParentSampleCollection.getNbioparentsamplecode();

			Object aliquotRaw = inputMap.get("aliquotList");
			List<Map<String, Object>> aliquotList = new ArrayList<>();
			if (aliquotRaw instanceof List) {
				aliquotList = (List<Map<String, Object>>) aliquotRaw;
			}
			if (aliquotList.size() > 0) {
				String sQuery = " lock  table lockbiosamplereceiving "
						+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
				jdbcTemplate.execute(sQuery);
				String sequenceNoQuery = "select nsequenceno from seqnobiobankmanagement where stablename ='biosamplereceiving' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";
				int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
				int start = nsequenceNo;
				sequenceNoQuery = "select nsequenceno from seqnobiobankmanagement where stablename ='biosamplereceivinghistory' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";
				int nsequencehistoryNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
				List<String> repIdList = generateNUniqueSrepositoryIds(aliquotList.size());
				int i = 0;
				String insrtQry = "";
				String insrtHisQry = "";
				for (Map<String, Object> aliquot : aliquotList) {
					nsequenceNo++;
					nsequencehistoryNo++;
					int nstoragetypecode = Integer.valueOf(aliquot.get("nstoragetypecode").toString());
					insrtQry += "(" + nsequenceNo + "," + nbioparentsamplecollectioncode + "," + nbioparentsamplecode
							+ "," + nproductcode + "," + ndiagnostictypecode + " ," + ncontainertypecode + " ,"
							+ nstoragetypecode + ", '" + repIdList.get(i) + "', null,'" + aliquot.get("volume") + "', "
							+ sreceiveddate + "," + userInfo.getNtimezonecode() + ", "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ Enumeration.TransactionStatus.ALIQUOTED.gettransactionstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
					insrtHisQry += "(" + nsequencehistoryNo + "," + nsequenceNo + "," + userInfo.getNusercode() + ", "
							+ userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", "
							+ userInfo.getNdeputyuserrole() + ", "
							+ Enumeration.TransactionStatus.ALIQUOTED.gettransactionstatus() + ", '"
							+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "', '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
					i++;
				}
				insrtQry = " INSERT INTO public.biosamplereceiving("
						+ "nbiosamplereceivingcode, nbioparentsamplecollectioncode, nbioparentsamplecode, nproductcode, ndiagnostictypecode, ncontainertypecode, nstoragetypecode, srepositoryid, slocationcode, sqty, dreceiveddate, ntzreceiveddate, noffsetreceiveddate, ntransactionstatus, dtransactiondate, ntztransactiondate, noffsettransactiondate, nsitecode, nstatus)"
						+ "VALUES " + insrtQry.substring(0, insrtQry.length() - 1) + ";";
				insrtHisQry = " INSERT INTO public.biosamplereceivinghistory("
						+ "	nbiosamplereceivinghistorycode, nbiosamplereceivingcode, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, ntransactionstatus, scomments, dtransactiondate, ntztransactiondate, noffsettransactiondate, nsitecode, nstatus)"
						+ "	VALUES " + insrtHisQry.substring(0, insrtHisQry.length() - 1) + ";";
				String updateQry = " update seqnobiobankmanagement set nsequenceno = " + nsequenceNo
						+ " where stablename = 'biosamplereceiving' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";
				updateQry = updateQry + " update seqnobiobankmanagement set nsequenceno = " + nsequencehistoryNo
						+ " where stablename = 'biosamplereceivinghistory' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

				jdbcTemplate.execute(insrtQry + insrtHisQry + updateQry);
				
				int end = nsequenceNo;
				String result = IntStream.rangeClosed(start, end) // generates numbers from start to end inclusive
						.mapToObj(String::valueOf) // convert each int to String
						.collect(Collectors.joining(", "));
				
				// ===== COC: START =====
//				if (result != null && !result.trim().isEmpty()) {
//					
//					String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//					jdbcTemplate.execute(sQuery1);
//			 
//					sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//					jdbcTemplate.execute(sQuery);
//			 
//					sQuery = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//					jdbcTemplate.execute(sQuery);
//					
//				    int chainCustodyPk = jdbcTemplate.queryForObject(
//				        "select max(nsequenceno) from seqnoregistration where stablename='chaincustody' and nstatus="
//				        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//				        Integer.class);
//
//					String lblParentSampleCode = stringUtilityFunction.replaceQuote(commonFunction
//							.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename()));
//					String lblCohortNo = stringUtilityFunction.replaceQuote(
//							commonFunction.getMultilingualMessage("IDS_COHORTNO", userInfo.getSlanguagefilename()));
//					String lblProduct = stringUtilityFunction.replaceQuote(
//							commonFunction.getMultilingualMessage("IDS_PRODUCT", userInfo.getSlanguagefilename()));
////				    String lblCaseType = stringUtilityFunction.replaceQuote(commonFunction.getMultilingualMessage("IDS_CASETYPE", userInfo.getSlanguagefilename()));
//					String lblBioSampleType = stringUtilityFunction.replaceQuote(commonFunction
//							.getMultilingualMessage("IDS_BIOSAMPLETYPE", userInfo.getSlanguagefilename()));
////				    String lblDiseaseStatus = stringUtilityFunction.replaceQuote(commonFunction.getMultilingualMessage("IDS_DISEASESTATUS", userInfo.getSlanguagefilename()));
////				    String lblContainerType = stringUtilityFunction.replaceQuote(commonFunction.getMultilingualMessage("IDS_CONTAINERTYPE", userInfo.getSlanguagefilename()));
//					String lblRepositoryId = stringUtilityFunction.replaceQuote(
//							commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename()));
////				    String lblVolume = stringUtilityFunction.replaceQuote(
////				    	    commonFunction.getMultilingualMessage("IDS_VOLUMEMICROL", userInfo.getSlanguagefilename()));
////				    	String lblReceivedDate = stringUtilityFunction.replaceQuote(
////				    	    commonFunction.getMultilingualMessage("IDS_RECEIVEDDATE", userInfo.getSlanguagefilename()));
//
//					String strChainCustody = "insert into chaincustody ( "
//				      + "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno, "
//				      + "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate, "
//							+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus) " + "select " + chainCustodyPk
//							+ " + rank() over(order by br.nbiosamplereceivingcode), " + userInfo.getNformcode() + ", "
//				      + " br.nbiosamplereceivingcode, 'nbiosamplereceivingcode', 'biosamplereceiving', bpr.sparentsamplecode, "
//				      + Enumeration.TransactionStatus.ALIQUOTED.gettransactionstatus() + ", "
//							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
//							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
//							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
//							+ lblParentSampleCode + " [' || COALESCE(bpr.sparentsamplecode, '') || '] ," + lblCohortNo
//							+ " [' || COALESCE(bpr.ncohortno::text, '') || '] ," + lblProduct
//							+ " [' || COALESCE(pc.sproductcatname, '') || '] ,"
////				      + lblCaseType + " [' || COALESCE(bpr.scasetype, '') || '] ,"
//				      + lblBioSampleType + " [' || COALESCE(pt.sproductname, '') || '] ,"
////				      + lblDiseaseStatus + " [' || COALESCE(dt.sdiagnostictypename, '') || '] ,"
////				      + lblContainerType + " [' || COALESCE(ct.scontainertype, '') || '] "
////				      + lblVolume + " [' || COALESCE(br.sqty, '') || '] , "
////				      + lblReceivedDate + " [' || COALESCE(br.dreceiveddate::text, '') || '] , "
//				      + lblRepositoryId + " [' || COALESCE(br.srepositoryid, '') || '] ' , "
//				      + userInfo.getNtranssitecode() + ", "
//				      + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				      + "from biosamplereceiving br  "
//				      + "join bioparentsamplecollection bpc on bpc.nbioparentsamplecollectioncode = br.nbioparentsamplecollectioncode "
//							+ "and bpc.nsitecode = " + userInfo.getNtranssitecode() + " " + "and bpc.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				      + "join bioparentsamplereceiving bpr on bpr.nbioparentsamplecode = br.nbioparentsamplecode  "
//							+ "and bpr.nsitecode = " + userInfo.getNtranssitecode() + " " + "and bpr.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//							+ "join product pt on br.nproductcode = pt.nproductcode and pt.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//							+ "join storagetype st on br.nstoragetypecode = st.nstoragetypecode and st.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//							+ "join diagnostictype dt on br.ndiagnostictypecode = dt.ndiagnostictypecode and dt.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//							+ "join containertype ct on br.ncontainertypecode = ct.ncontainertypecode and ct.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				      + "left join productcategory pc on pc.nproductcatcode = bpc.nproductcatcode "
//							+ "where br.nsitecode = " + userInfo.getNtranssitecode() + " and br.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				      + " and br.nbiosamplereceivingcode in (" + result + ");";
//
//
//					String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//							+ " + count(nbiosamplereceivingcode) " + " from biosamplereceiving"
//							+ " where nbiosamplereceivingcode in (" + result + ")" + " and nsitecode = "
//							+ userInfo.getNtranssitecode() + " and nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//				      + ") where stablename = 'chaincustody' and nstatus = "
//				      + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//				    
//					    jdbcTemplate.execute(strChainCustody);
//				    jdbcTemplate.execute(strSeqUpdate);
//				
//				}
				// ===== End COC =====

				final List<Object> listBeforeUpdate = new ArrayList<>();
				final List<BioSampleReceiving> lstBioSampleRec = getProcessedSampleListUsingBioSampleReceivingCode(
						result, userInfo);
				listBeforeUpdate.addAll(lstBioSampleRec);
			    final List<String> multilingualIDList = new ArrayList<>();
				lstBioSampleRec.stream().forEach(x -> multilingualIDList.add("IDS_ADDALIQUOTEDSAMPLE"));
				auditUtilityFunction.fnInsertAuditAction(listBeforeUpdate, 1, null, multilingualIDList, userInfo);
				
				//Added By Mullai Balaji V for email JIRA ID:BGSI:147
				
				String Query="select DISTINCT (ncontrolcode) from emailconfig where ncontrolcode="+inputMap.get("ncontrolcode")+" "
						+ " and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				Integer value = null;
				try {
				    value = jdbcTemplate.queryForObject(Query, Integer.class);
				} catch (Exception e) {
				    value = null; 
			}			

				if(value!=null) {
				final Map<String, Object> mailMap = new HashMap<String, Object>();
				mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));
				mailMap.put("nbioparentsamplecollectioncode", nbioparentsamplecollectioncode);
				mailMap.put("nbioparentsamplecode", nbioparentsamplecode);
				mailMap.put("nbiosamplereceivingcode", nsequenceNo);
				String query = "SELECT  DISTINCT  CONCAT( bpsr.sparentsamplecode, '|', bpsr.ncohortno ) FROM  biosamplereceiving bsr "
						+ "  JOIN bioparentsamplereceiving bpsr ON bpsr.nbioparentsamplecode = bsr.nbioparentsamplecode  "
						+ "  JOIN bioparentsamplecollection bpsc ON bpsc.nbioparentsamplecollectioncode = bsr.nbioparentsamplecollectioncode  where"
						+ "  bsr.nbioparentsamplecollectioncode = " + nbioparentsamplecollectioncode
						+ "  and bsr.nbioparentsamplecode = " + nbioparentsamplecode + " and bsr.nsitecode="
						+ userInfo.getNtranssitecode() + " and bpsr.nsitecode=" + userInfo.getNtranssitecode()
						+ " and bpsc.nsitecode=" + userInfo.getNtranssitecode() + " and bsr.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bpsr.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bpsc.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				String referenceId = jdbcTemplate.queryForObject(query, String.class);

				mailMap.put("ssystemid", referenceId);
				final UserInfo mailUserInfo = new UserInfo(userInfo);
				mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
				mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
				emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
				}
			}

			Map<String, Object> returnMap = new HashMap<String, Object>();
			returnMap.put("nbioparentsamplecollectioncode", nbioparentsamplecollectioncode);
			returnMap.putAll(getActiveSampleCollection(returnMap, userInfo).getBody());
			return new ResponseEntity<>(returnMap, HttpStatus.OK);
		}
		return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTNOTYETPROCESSEDSTATUSRECORD",
				userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);

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

	/**
	 *  * Checks if the srepositoryid already exists in biosamplereceiving table  
	 */
	private boolean srepositoryIdExistsInDB(String id) {
		String sql = "SELECT COUNT(1) FROM biosamplereceiving WHERE srepositoryid = ?";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
		return count != null && count > 0;
	}

	/**
	 *  * Generates a list of unique srepositoryid values that do not exist  * in
	 * the database or the in-memory list  *  * @param count number of unique IDs to
	 * generate  * @return list of unique srepositoryid values
	 */
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

	@Override
	public ResponseEntity<Object> deleteProcessedSampleReceiving(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final int nbioparentsamplecollectioncode = Integer
				.valueOf(inputMap.get("nbioparentsamplecollectioncode").toString());

		// final int nbiosamplereceivingcode =
		// Integer.valueOf(inputMap.get("nbiosamplereceivingcode").toString());
		final List<Object> listAfterUpdate = new ArrayList<>();

		final String sbiosamplereceivingcode = (String) inputMap.get("sbiosamplereceivingcode");

		final String result = validateProcessedSampleReceiving(sbiosamplereceivingcode, userInfo);
		if (!result.equals(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus())) {
			if (result.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
				final List<BioSampleReceiving> lstDeletedBioSampleReceiving = getProcessedSampleListUsingBioSampleReceivingCode(
						sbiosamplereceivingcode, userInfo);

				// ===== COC: START =====
//	            if (result != null && !result.trim().isEmpty()) {
//	            	
//	            	String sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//	        		jdbcTemplate.execute(sQuery);
//	         
//	        		sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//	        		jdbcTemplate.execute(sQuery);
//	         
//	        		sQuery = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//	        		jdbcTemplate.execute(sQuery);
//	            	
//	            	
//	                int chainCustodyPk = jdbcTemplate.queryForObject(
//	                        "select max(nsequenceno) from seqnoregistration where stablename='chaincustody' and nstatus="
//	                                + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//	                        Integer.class);
//
//					String lblParentSampleCode = stringUtilityFunction.replaceQuote(commonFunction
//							.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename()));
//	                String lblCohortNo = stringUtilityFunction.replaceQuote(
//	                        commonFunction.getMultilingualMessage("IDS_COHORTNO", userInfo.getSlanguagefilename()));
//	                String lblProduct = stringUtilityFunction.replaceQuote(
//	                        commonFunction.getMultilingualMessage("IDS_PRODUCT", userInfo.getSlanguagefilename()));
////	                String lblCaseType = stringUtilityFunction.replaceQuote(
////	                        commonFunction.getMultilingualMessage("IDS_CASETYPE", userInfo.getSlanguagefilename()));
//					String lblBioSampleType = stringUtilityFunction.replaceQuote(commonFunction
//							.getMultilingualMessage("IDS_BIOSAMPLETYPE", userInfo.getSlanguagefilename()));
////	                String lblStorageType = stringUtilityFunction.replaceQuote(
////	                        commonFunction.getMultilingualMessage("IDS_STORAGETYPE", userInfo.getSlanguagefilename()));
////	                String lblVolume = stringUtilityFunction.replaceQuote(
////	                	    commonFunction.getMultilingualMessage("IDS_VOLUMEMICROL", userInfo.getSlanguagefilename()));
////	                String lblContainerType = stringUtilityFunction.replaceQuote(
////	                        commonFunction.getMultilingualMessage("IDS_CONTAINERTYPE", userInfo.getSlanguagefilename()));
//	                String lblRepositoryId = stringUtilityFunction.replaceQuote(
//	                        commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename()));
//
//	                String strChainCustody = "insert into chaincustody ( "
//	                        + "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno, "
//	                        + "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate, "
//							+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus) " + "select " + chainCustodyPk
//							+ " + rank() over(order by br.nbiosamplereceivingcode), " + userInfo.getNformcode() + ", "
//	                        + " br.nbiosamplereceivingcode, 'nbiosamplereceivingcode', 'biosamplereceiving', bpr.sparentsamplecode, "
//	                        + Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", "
//							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
//							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
//							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
//							+ lblParentSampleCode + " [' || COALESCE(bpr.sparentsamplecode, '') || '] ," + lblCohortNo
//							+ " [' || COALESCE(bpr.ncohortno::text, '') || '] ," + lblProduct
//							+ " [' || COALESCE(pc.sproductcatname, '') || '] ,"
////	                        + lblCaseType + " [' || COALESCE(bpr.scasetype, '') || '] ,"
//	                        + lblBioSampleType + " [' || COALESCE(pt.sproductname, '') || '] ,"
////	                        + lblStorageType + " [' || COALESCE(st.sstoragetypename, '') || '] ,"
////	                        + lblContainerType + " [' || COALESCE(ct.scontainertype, '') || '] "
////	                        + lblVolume + " [' || COALESCE(br.sqty, '') || '] , "
//	                        + lblRepositoryId + " [' || COALESCE(br.srepositoryid, '') || '] ' , "
//	                        + userInfo.getNtranssitecode() + ", "
//	                        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//	                        + "from biosamplereceiving br  "
//	                        + "join bioparentsamplecollection bpc on bpc.nbioparentsamplecollectioncode = br.nbioparentsamplecollectioncode "
//							+ "and bpc.nsitecode = " + userInfo.getNtranssitecode() + " " + "and bpc.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//	                        + "join bioparentsamplereceiving bpr on bpr.nbioparentsamplecode = br.nbioparentsamplecode  "
//							+ "and bpr.nsitecode = " + userInfo.getNtranssitecode() + " " + "and bpr.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//							+ "join product pt on br.nproductcode = pt.nproductcode and pt.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//							+ "join storagetype st on br.nstoragetypecode = st.nstoragetypecode and st.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//							+ "join diagnostictype dt on br.ndiagnostictypecode = dt.ndiagnostictypecode and dt.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//							+ "join containertype ct on br.ncontainertypecode = ct.ncontainertypecode and ct.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//	                        + "left join productcategory pc on pc.nproductcatcode = bpc.nproductcatcode "
//							+ "where br.nsitecode = " + userInfo.getNtranssitecode() + " and br.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//	                        + " and br.nbiosamplereceivingcode in (" + sbiosamplereceivingcode + ");";
//
//
//					String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//							+ " + count(nbiosamplereceivingcode) " + " from biosamplereceiving"
//	                        + " where nbiosamplereceivingcode in (" + sbiosamplereceivingcode + ")"
//							+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//	                        + ") where stablename = 'chaincustody' and nstatus = "
//	                        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//		                jdbcTemplate.execute(strChainCustody);
//	                jdbcTemplate.execute(strSeqUpdate);
//				
//	            }
	            // ===== End COC =====		
				
				final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
				final String udpateStr = "update biosamplereceiving set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dtransactiondate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntztransactiondate = "
						+ userInfo.getNtimezonecode() + ", noffsettransactiondate = "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " where nbiosamplereceivingcode in (" + sbiosamplereceivingcode + ");";
				jdbcTemplate.execute(udpateStr);

				final List<BioSampleReceiving> lstBioSampleReceiving = getProcessedSampleListFromParentSample(
						nbioparentsamplecollectioncode, userInfo);
				listAfterUpdate.addAll(lstDeletedBioSampleReceiving);

				final List<String> multilingualIDList = new ArrayList<>();

				lstDeletedBioSampleReceiving.stream().forEach(x -> multilingualIDList.add("IDS_DELETEALIQUOTEDSAMPLE"));

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 1, null, multilingualIDList, userInfo);

				outputMap.put("lstBioSampleReceiving", lstBioSampleReceiving);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTALIQUOTEDSTATUSRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

	}

	public List<BioSampleReceiving> getProcessedSampleListUsingBioSampleReceivingCode(
			final String sbiosamplereceivingcode, final UserInfo userInfo) throws Exception {

		final String sampleListQry = "select csr.nbiosamplereceivingcode, csr.nbioparentsamplecollectioncode,csr.nbioparentsamplecode, "
				+ "csr.nproductcode, p.sproductname, csr.ndiagnostictypecode, " + "coalesce(d.jsondata->>'"
				+ userInfo.getSlanguagetypecode() + "',d.jsondata->>'en-US') as sdiagnostictypename, "
				+ "csr.ncontainertypecode, c.scontainertype, csr.nstoragetypecode, " + "coalesce(s.jsondata->>'"
				+ userInfo.getSlanguagetypecode() + "',s.jsondata->>'en-US') as sstoragetypename, "
				+ "csr.srepositoryid, csr.slocationcode, csr.sqty, "
				+ "csr.ntransactionstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus, "
				+ "csr.dreceiveddate, csr.ntzreceiveddate, csr.noffsetreceiveddate, tz1.stimezoneid AS stzreceiveddate, "
				+ "COALESCE(TO_CHAR(csr.dreceiveddate,'" + userInfo.getSsitedate() + "'), '') AS sreceiveddate, "
				+ "csr.dtransactiondate, csr.ntztransactiondate, csr.noffsettransactiondate, tz2.stimezoneid AS stztransactiondate, "
				+ "COALESCE(TO_CHAR(csr.dtransactiondate,'" + userInfo.getSsitedate() + "'), '') AS stransactiondate  "
				+ "from biosamplereceiving csr, product p, diagnostictype d, containertype c, storagetype s, transactionstatus ts, timezone tz1, timezone tz2 "
				+ "where csr.nbiosamplereceivingcode in (" + sbiosamplereceivingcode + ") " + "and csr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and csr.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + "and csr.nproductcode = p.nproductcode and p.nsitecode = "
				+ userInfo.getNmastersitecode() + " and p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and csr.ndiagnostictypecode = d.ndiagnostictypecode and d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and csr.ncontainertypecode = c.ncontainertypecode and c.nsitecode = " + userInfo.getNmastersitecode()
				+ " and c.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and csr.nstoragetypecode = s.nstoragetypecode and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and csr.ntransactionstatus = ts.ntranscode "
				+ "and csr.ntzreceiveddate = tz1.ntimezonecode and tz1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and csr.ntztransactiondate = tz2.ntimezonecode and tz2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by csr.nbiosamplereceivingcode desc";

		return jdbcTemplate.query(sampleListQry, new BioSampleReceiving());
	}
	/*
	 * public String validateProcessedSampleReceiving(final int
	 * nbiosamplereceivingcode, final UserInfo userInfo) throws Exception { final
	 * String strValidate =
	 * "select ntransactionstatus from biosamplereceiving where nsitecode=" +
	 * userInfo.getNtranssitecode() + " and nstatus=" +
	 * Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() +
	 * " and nbiosamplereceivingcode=" + nbiosamplereceivingcode; final
	 * BioSampleReceiving objBioSampleReceiving = (BioSampleReceiving)
	 * jdbcUtilityTemplateFunction .queryForObject(strValidate,
	 * BioSampleReceiving.class, jdbcTemplate); String result =
	 * Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus();
	 * 
	 * if (objBioSampleReceiving != null) { final int intValidate =
	 * objBioSampleReceiving.getNtransactionstatus(); if (intValidate ==
	 * Enumeration.TransactionStatus.ALIQUOTED.gettransactionstatus()) { result =
	 * Enumeration.ReturnStatus.SUCCESS.getreturnstatus(); } else { result =
	 * Enumeration.ReturnStatus.FAILED.getreturnstatus(); } } return result; }
	 */

	public String validateProcessedSampleReceiving(final String sbiosamplereceivingcode, final UserInfo userInfo)
			throws Exception {
		final String strValidate = "select ntransactionstatus from biosamplereceiving where nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nbiosamplereceivingcode in ("
				+ sbiosamplereceivingcode + ");";

		final List<BioSampleReceiving> lstBioSampleReceiving = (List<BioSampleReceiving>) jdbcTemplate
				.query(strValidate, new BioSampleReceiving());

		String result = Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus();

		if (lstBioSampleReceiving.size() > 0) {
			result = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			if (lstBioSampleReceiving.stream().anyMatch(obj -> obj
					.getNtransactionstatus() != Enumeration.TransactionStatus.ALIQUOTED.gettransactionstatus())) {
				result = Enumeration.ReturnStatus.FAILED.getreturnstatus();
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getStorageFreezerData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();
		final int nstorageinstrumentcode = (int) inputMap.get("nstorageinstrumentcode");
		final int nbioparentsamplecollectioncode = (int) inputMap.get("nbioparentsamplecollectioncode");

		// Added by Gowtham on nov 14 2025 for jira.id:BGSI-216
		final String strQuery = "SELECT bsd.nissampleaccesable FROM biosubjectdetails bsd "
				+ " JOIN bioparentsamplecollection bpsc on bpsc.nbioparentsamplecollectioncode=" + nbioparentsamplecollectioncode 
				+ " JOIN bioparentsamplereceiving bpsr on bpsr.nbioparentsamplecode = bpsc.nbioparentsamplecode "
				+ " WHERE bsd.ssubjectid = bpsr.ssubjectid and bsd.nsitecode = " + userInfo.getNmastersitecode() 
				+ " and bpsc.nsitecode=" + userInfo.getNtranssitecode() + " and bpsr.nsitecode=" + userInfo.getNtranssitecode()
				+ " and bsd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and bpsc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and bpsr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		final Integer nissampleaccesable = (Integer) jdbcUtilityTemplateFunction.queryForObject(strQuery, Integer.class, jdbcTemplate);
		
		if (nissampleaccesable != null && (int) nissampleaccesable == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			return new ResponseEntity<>(
				commonFunction.getMultilingualMessage("IDS_SAMPLEISNOTACCESSIBLE", userInfo.getSlanguagefilename()),
				HttpStatus.EXPECTATION_FAILED);
		}

		String suggestedStorageQuery = "";
		StorageInstrument suggestedStorage = new StorageInstrument();

		if (nstorageinstrumentcode == -1) {
			suggestedStorageQuery = "select i.sinstrumentid,i.ninstrumentcode,si.nstorageinstrumentcode from storageinstrument si, instrument i "
					+ "where si.ninstrumentcode = i.ninstrumentcode " + "and nstorageinstrumentcode = "
					+ nstorageinstrumentcode + " and si.nstatus  = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and  i.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			suggestedStorage = (StorageInstrument) jdbcUtilityTemplateFunction.queryForObject(suggestedStorageQuery,
					StorageInstrument.class, jdbcTemplate);
		}

		final String sQueryFreezerId = "select ssv.jsondata, si.nsamplestoragelocationcode,si.nsamplestorageversioncode,si.nstorageinstrumentcode, "
				// + "i.sinstrumentid,"
				+ "(i.sinstrumentid ||' (' ||(SUM(ssm.nnoofcontainer) - (SELECT Count(st.sposition) positions "
				+ "FROM   samplestoragetransaction st,samplestoragemapping ssm1 " + "WHERE "
				+ "ssl.nsamplestoragelocationcode = st.nsamplestoragelocationcode "
				+ "AND st.nsamplestoragemappingcode = ssm1.nsamplestoragemappingcode "
				+ "AND ssm1.nstorageinstrumentcode = ssm.nstorageinstrumentcode " + "AND st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "AND st.spositionvalue :: text <> '' :: text)) || ')') sinstrumentid," + " i.ninstrumentcode, "
				+ "sum(ssm.nnoofcontainer) - " + "(SELECT Count(st.sposition) positions "
				+ "FROM   samplestoragetransaction st,samplestoragemapping ssm1 " + "WHERE "
				+ "ssl.nsamplestoragelocationcode = st.nsamplestoragelocationcode "
				+ "AND st.nsamplestoragemappingcode = ssm1.nsamplestoragemappingcode "
				+ "AND ssm1.nstorageinstrumentcode = ssm.nstorageinstrumentcode " + "AND st.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "AND st.spositionvalue :: text <> '' :: text) navailablespace " + "from " + "storageinstrument si "
				+ "JOIN samplestoragelocation ssl ON ssl.nsamplestoragelocationcode = si.nsamplestoragelocationcode  "
				+ "and si.nregionalsitecode = " + userInfo.getNtranssitecode() + " " + "and si.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN samplestorageversion ssv ON ssl.nsamplestoragelocationcode = ssv.nsamplestoragelocationcode "
				+ "and ssv.nsamplestorageversioncode = si.nsamplestorageversioncode and ssv.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and ssv.napprovalstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " "
				+ "JOIN samplestoragecontainerpath ssc ON ssc.nsamplestoragelocationcode = ssv.nsamplestoragelocationcode "
				+ "and ssc.nsamplestorageversioncode = ssv.nsamplestorageversioncode and ssc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN samplestoragemapping ssm ON ssm.nsamplestoragecontainerpathcode = ssc.nsamplestoragecontainerpathcode "
				+ "and ssv.nsamplestorageversioncode = si.nsamplestorageversioncode and ssm.nstorageinstrumentcode = si.nstorageinstrumentcode "
				+ "and ssm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "JOIN instrument i ON si.ninstrumentcode = i.ninstrumentcode and i.ninstrumentstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.ninstrumentstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join instrumentcalibration ic "
				+ " on ic.ninstrumentcode=i.ninstrumentcode and ic.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ic.ncalibrationstatus="
				+ Enumeration.TransactionStatus.CALIBIRATION.gettransactionstatus() + " and ic.nsitecode="
				+ userInfo.getNtranssitecode() + " and ninstrumentcalibrationcode in (select"
				+ " max(ninstrumentcalibrationcode) from instrumentcalibration where nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by ninstrumentcode) join instrumentmaintenance im on im.ninstrumentcode=i.ninstrumentcode"
				+ " and im.nsitecode="+ userInfo.getNtranssitecode()+ " and  im.nmaintenancestatus="
				+ Enumeration.TransactionStatus.MAINTANENCE.gettransactionstatus()+ " and im.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ " and im.ninstrumentmaintenancecode in"
				+ " (select max(ninstrumentmaintenancecode) from instrumentmaintenance where nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by ninstrumentcode) group by ssv.jsondata, si.nsamplestoragelocationcode,"
				+ " si.nsamplestorageversioncode,si.nstorageinstrumentcode,i.sinstrumentid, i.ninstrumentcode,"
				+ " ssl.nsamplestoragelocationcode, ssm.nstorageinstrumentcode; ";

		final List<StorageInstrument> freezerList = jdbcTemplate.query(sQueryFreezerId, new StorageInstrument());

		// outputMap.put("selectedSuggestedStorage",suggestedStorage);

		if (nstorageinstrumentcode == -1) {
			outputMap.put("selectedSuggestedStorage", suggestedStorage);
			outputMap.put("selectedFreezerData", freezerList.size() > 0 ? freezerList.get(0) : new StorageInstrument());
			inputMap.put("nsamplestorageversioncode",
					freezerList.size() > 0 ? freezerList.get(0).getNsamplestorageversioncode() : -1);
			inputMap.put("nstorageinstrumentcode",
					freezerList.size() > 0 ? freezerList.get(0).getNstorageinstrumentcode() : -1);
		} else {
			StorageInstrument matchingInstrument = freezerList.stream().filter(obj -> obj instanceof StorageInstrument)
					.map(obj -> (StorageInstrument) obj)
					.filter(si -> si.getNstorageinstrumentcode() == nstorageinstrumentcode).findFirst().orElse(null);

			outputMap.put("selectedSuggestedStorage", matchingInstrument);
			outputMap.put("selectedFreezerData", matchingInstrument);
			inputMap.put("nsamplestorageversioncode", matchingInstrument.getNsamplestorageversioncode());
			inputMap.put("nstorageinstrumentcode", matchingInstrument.getNstorageinstrumentcode());

		}
		outputMap.put("freezerList", freezerList);
		outputMap.putAll((Map<String, Object>) getStorageStructure(inputMap, userInfo).getBody());

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
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

	@Override
	public ResponseEntity<Object> updateBioSampleCollectionAsProcessed(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		// Added by Gowtham on nov 14 2025 for jira.id:BGSI-216
		final int nbioparentsamplecollectioncode = (int) inputMap.get("nbioparentsamplecollectioncode");
		
		final String strQuery = "SELECT bsd.nissampleaccesable FROM biosubjectdetails bsd "
				+ " JOIN bioparentsamplecollection bpsc on bpsc.nbioparentsamplecollectioncode=" + nbioparentsamplecollectioncode 
				+ " JOIN bioparentsamplereceiving bpsr on bpsr.nbioparentsamplecode = bpsc.nbioparentsamplecode "
				+ " WHERE bsd.ssubjectid = bpsr.ssubjectid and bsd.nsitecode = " + userInfo.getNmastersitecode() 
				+ " and bpsc.nsitecode=" + userInfo.getNtranssitecode() + " and bpsr.nsitecode=" + userInfo.getNtranssitecode()
				+ " and bsd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and bpsc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and bpsr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		final Integer nissampleaccesable = (Integer) jdbcUtilityTemplateFunction.queryForObject(strQuery, Integer.class, jdbcTemplate);
		
		if (nissampleaccesable != null && (int) nissampleaccesable == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			return new ResponseEntity<>(
				commonFunction.getMultilingualMessage("IDS_SAMPLEISNOTACCESSIBLE", userInfo.getSlanguagefilename()),
				HttpStatus.EXPECTATION_FAILED);
		}

		returnMap.putAll((Map<String, Object>) getActiveSampleCollection(inputMap, userInfo).getBody());

		BioParentSampleCollection objBioParentSampleCollection = (BioParentSampleCollection) returnMap
				.get("selectedBioParentSampleCollection");
		if (objBioParentSampleCollection != null && objBioParentSampleCollection
				.getNtransactionstatus() == Enumeration.TransactionStatus.UNPROCESSED.gettransactionstatus()) {
			final List<BioSampleReceiving> lstBioSampleReceiving = (List<BioSampleReceiving>) returnMap
					.get("lstBioSampleReceiving");
			if (lstBioSampleReceiving != null && !lstBioSampleReceiving.isEmpty()) {

				boolean hasAliquotedStatus = lstBioSampleReceiving.stream().anyMatch(objBioReceiving -> objBioReceiving
						.getNtransactionstatus() == Enumeration.TransactionStatus.ALIQUOTED.gettransactionstatus());
				if (!hasAliquotedStatus) {
					listBeforeUpdate.add(objBioParentSampleCollection);
					final String sQuery = " lock  table lockbioparentcollection "
							+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery);

					int seqNoSampleCollectionHistory = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnobiobankmanagement where stablename='bioparentsamplecollectionhistory' and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);
					seqNoSampleCollectionHistory = seqNoSampleCollectionHistory + 1;

					final String updQry = "Update bioparentsamplecollection set ntransactionstatus = "
							+ Enumeration.TransactionStatus.PROCESSED.gettransactionstatus() + ", dtransactiondate = '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', noffsettransactiondate = "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ ", ntztransactiondate = " + userInfo.getNtimezonecode()
							+ " where nbioparentsamplecollectioncode = "
							+ objBioParentSampleCollection.getNbioparentsamplecollectioncode() + " and nsitecode = "
							+ userInfo.getNtranssitecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

					String strInsert = "insert into bioparentsamplecollectionhistory (nbioparentsamplecolhistorycode,"
							+ " nbioparentsamplecollectioncode, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode,"
							+ " ntransactionstatus, scomments, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode,"
							+ " nsitecode, nstatus) values (" + seqNoSampleCollectionHistory + ", "
							+ objBioParentSampleCollection.getNbioparentsamplecode() + ", " + userInfo.getNusercode()
							+ ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", "
							+ userInfo.getNdeputyuserrole() + ", "
							+ Enumeration.TransactionStatus.PROCESSED.gettransactionstatus() + ", '"
							+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "', '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNtimezonecode() + ", " + userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "); ";

					strInsert += "update seqnobiobankmanagement set nsequenceno=" + seqNoSampleCollectionHistory
							+ " where stablename='bioparentsamplecollectionhistory';";
					jdbcTemplate.execute(updQry + strInsert);

					// ===== COC: START =====
//					if (objBioParentSampleCollection != null) {
//						String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//						jdbcTemplate.execute(sQuery1);
//
//						String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//						jdbcTemplate.execute(sQuery2);
//				 
//						String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//						jdbcTemplate.execute(sQuery3);
//						
//						int chainCustodyPk = jdbcTemplate.queryForObject(
//								"select max(nsequenceno) from seqnoregistration where stablename='chaincustody' and nstatus="
//										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//								Integer.class);
//
//						String lblParentSampleCode = stringUtilityFunction.replaceQuote(commonFunction
//								.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename()));
//						String lblCohortNo = stringUtilityFunction.replaceQuote(
//								commonFunction.getMultilingualMessage("IDS_COHORTNO", userInfo.getSlanguagefilename()));
//						String lblCaseType = stringUtilityFunction.replaceQuote(
//								commonFunction.getMultilingualMessage("IDS_CASETYPE", userInfo.getSlanguagefilename()));
//						String lblBioSampleType = stringUtilityFunction.replaceQuote(commonFunction
//								.getMultilingualMessage("IDS_BIOSAMPLETYPE", userInfo.getSlanguagefilename()));
////						String lblProcessedDate = stringUtilityFunction.replaceQuote(commonFunction
////								.getMultilingualMessage("IDS_PROCESSDATE", userInfo.getSlanguagefilename()));
//						String lblRepositoryId = stringUtilityFunction.replaceQuote(
//							    commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename()));
//
//						
//						String strChainCustody=
//								  "insert into chaincustody(nchaincustodycode,nformcode,ntablepkno,stablepkcolumnname,stablename,sitemno,"
//								+ "ntransactionstatus,nusercode,nuserrolecode,dtransactiondate,ntztransactiondate,noffsetdtransactiondate,"
//								+ "sremarks,nsitecode,nstatus) select "
//								+ chainCustodyPk+" + rank() over(order by br.nbiosamplereceivingcode),"
//								+ userInfo.getNformcode()+","
//								+ "br.nbiosamplereceivingcode,'nbiosamplereceivingcode','biosamplereceiving',bpr.sparentsamplecode,"
//								+ Enumeration.TransactionStatus.PROCESSED.gettransactionstatus()+","
//								+ userInfo.getNusercode()+","+userInfo.getNuserrole()+",'"
//								+ dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
//								+ userInfo.getNtimezonecode()+","
//								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())+",'"
//								+ lblParentSampleCode+" [' || COALESCE(bpr.sparentsamplecode,'') || '] , "
//								+ lblCohortNo+" [' || COALESCE(bpr.ncohortno::text,'') || '] , "
//								+ lblCaseType+" [' || COALESCE(bpr.scasetype,'') || '] , "
//								+ lblRepositoryId+" [' || COALESCE(br.srepositoryid,'') || '] , "
//								+ lblBioSampleType+" [' || COALESCE(pt.sproductname,'') || ']'"
//								+ ","
//								+ userInfo.getNtranssitecode()+","
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//								+ " from biosamplereceiving br "
//								+ "join bioparentsamplecollection bpc on bpc.nbioparentsamplecollectioncode=br.nbioparentsamplecollectioncode "
//								+ " and bpc.nsitecode="+userInfo.getNtranssitecode()+" and bpc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
//								+ "join bioparentsamplereceiving bpr on bpr.nbioparentsamplecode=br.nbioparentsamplecode "
//								+ " and bpr.nsitecode="+userInfo.getNtranssitecode()+" and bpr.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
//								+ "join product pt on br.nproductcode=pt.nproductcode and pt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
//								+ "join storagetype st on br.nstoragetypecode=st.nstoragetypecode and st.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
//								+ "join diagnostictype dt on br.ndiagnostictypecode=dt.ndiagnostictypecode and dt.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
//								+ "join containertype ct on br.ncontainertypecode=ct.ncontainertypecode and ct.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
//								+ "left join productcategory pc on pc.nproductcatcode=bpc.nproductcatcode "
//								+ "where br.nbioparentsamplecollectioncode = "
//								+ objBioParentSampleCollection.getNbioparentsamplecollectioncode()
//								+ " and br.nsitecode = " + userInfo.getNtranssitecode() + " and br.nstatus = "
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//
//
//						String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//								+ " + count(nbiosamplereceivingcode) " + " from biosamplereceiving"
//								+ " where nbioparentsamplecollectioncode = "
//								+ objBioParentSampleCollection.getNbioparentsamplecollectioncode() + " and nsitecode = "
//								+ userInfo.getNtranssitecode() + " and nstatus = "
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//								+ ") where stablename = 'chaincustody' and nstatus = "
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//							jdbcTemplate.execute(strChainCustody);
//						jdbcTemplate.execute(strSeqUpdate);
//					
//					}
					// ===== COC: END =====

					outputMap.putAll((Map<String, Object>) getActiveSampleCollection(inputMap, userInfo).getBody());
					final List<String> multilingualIDList = new ArrayList<>();
					objBioParentSampleCollection = (BioParentSampleCollection) outputMap
							.get("selectedBioParentSampleCollection");

					listAfterUpdate.add(objBioParentSampleCollection);

					multilingualIDList.add("IDS_PROCESSPARENTSAMPLECOLLECTION");

					auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
							userInfo);

					return new ResponseEntity<>(outputMap, HttpStatus.OK);

				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_STOREALLSAMPLESBEFOREPROCESSING",
									userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ALIQUOTANDSTOREATLEASTONESAMPLE",
						userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}

		}
		return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTNOTYETPROCESSEDSTATUSRECORD",
				userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
	}

	@Override
	public ResponseEntity<Object> storeProcessedSampleReceiving(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		
		final var sQuerys = " lock  table locksamplestoragetransaction "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuerys);
		final int nstorageinstrumentcode = Integer.valueOf(inputMap.get("nstorageinstrumentcode").toString());

		final String storageLocQry = "select nsamplestoragelocationcode, nsamplestorageversioncode, ninstrumentcode from storageinstrument "
				+ "where nstorageinstrumentcode = " + nstorageinstrumentcode + " " + "and nsitecode = "
				+ userInfo.getNmastersitecode() + " " + "and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final StorageInstrument objSelectedStructure = (StorageInstrument) jdbcUtilityTemplateFunction
				.queryForObject(storageLocQry, StorageInstrument.class, jdbcTemplate);
		final int nsamplestoragelocationcode = objSelectedStructure.getNsamplestoragelocationcode();
		final int nsamplestorageversioncode = objSelectedStructure.getNsamplestorageversioncode();
		final int ninstrumentcode = objSelectedStructure.getNinstrumentcode();

		final ObjectMapper objmapper = new ObjectMapper();
		objmapper.registerModule(new JavaTimeModule());
		final BioParentSampleCollection objBioParentSampleCollection = objmapper
				.convertValue(inputMap.get("selectedBioParentSampleCollection"), BioParentSampleCollection.class);

		// Added by Gowtham on nov 14 2025 for jira.id:BGSI-216
		final String strQuery = "SELECT bsd.nissampleaccesable FROM biosubjectdetails bsd "
				+ " JOIN bioparentsamplecollection bpsc on bpsc.nbioparentsamplecollectioncode=" 
				+ objBioParentSampleCollection.getNbioparentsamplecollectioncode()
				+ " JOIN bioparentsamplereceiving bpsr on bpsr.nbioparentsamplecode = bpsc.nbioparentsamplecode "
				+ " WHERE bsd.ssubjectid = bpsr.ssubjectid and bsd.nsitecode = " + userInfo.getNmastersitecode() 
				+ " and bpsc.nsitecode=" + userInfo.getNtranssitecode() + " and bpsr.nsitecode=" + userInfo.getNtranssitecode()
				+ " and bsd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and bpsc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
				+ " and bpsr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					
		final Integer nissampleaccesable = (Integer) jdbcUtilityTemplateFunction.queryForObject(strQuery, Integer.class, jdbcTemplate);
					
		if (nissampleaccesable != null && (int) nissampleaccesable == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SAMPLEISNOTACCESSIBLE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final String sbiosamplereceivingcodes = inputMap.get("sbiosamplereceivingcodes").toString();
		
		final List<Object> listBeforeUpdate = new ArrayList<>();
		final List<BioSampleReceiving> lstBioSampleRec = getProcessedSampleListUsingBioSampleReceivingCode(
				sbiosamplereceivingcodes, userInfo);
		listBeforeUpdate.addAll(lstBioSampleRec);
	    final List<String> multilingualIDList = new ArrayList<>();
		lstBioSampleRec.stream().forEach(x -> multilingualIDList.add("IDS_STOREALIQUOTEDSAMPLE"));
				
		// modified by sujatha ATE_274 by selecting the extractedsampleid, qcplatform, eluent & concentration for inserting into samplestorageadditionalinfo table BGSI-218
		final String sampleRecQry = "select br.nbiosamplereceivingcode,br.nbioparentsamplecode, br.srepositoryid, br.sqty, br.ndiagnostictypecode, br.ncontainertypecode, br.nstoragetypecode, p.nproductcode, p.sproductname, "
				+ " br.sextractedsampleid, br.sconcentration, br.sqcplatform, br.seluent "
				+ "from biosamplereceiving br join product p on br.nproductcode = p.nproductcode "
				+ "and p.nsitecode = " + userInfo.getNmastersitecode() + " and p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "where br.nbiosamplereceivingcode in (" + sbiosamplereceivingcodes + ") "
				+ "and br.ntransactionstatus = " + Enumeration.TransactionStatus.ALIQUOTED.gettransactionstatus() + " "
				+ "and br.nsitecode = " + userInfo.getNtranssitecode() + " and br.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by br.nbiosamplereceivingcode";
		List<BioSampleReceiving> lstBioSampleReceiving = jdbcTemplate.query(sampleRecQry, new BioSampleReceiving());
		if (!lstBioSampleReceiving.isEmpty()) {

			final int selectedCount = lstBioSampleReceiving.size();
			final String selectedNodeID = inputMap.get("selectedNodeID").toString();

			final String sQuery = "SELECT enrich_jsondata_by_version(" + nsamplestorageversioncode + ","
					+ nstorageinstrumentcode + ") AS jsondata; ";

			final StorageInstrument objSelectedStrcuture = (StorageInstrument) jdbcUtilityTemplateFunction
					.queryForObject(sQuery, StorageInstrument.class, jdbcTemplate);
			final Map<String, Object> objJsonData = objSelectedStrcuture.getJsondata();
			Object dataListObj = objJsonData.get("data");

			int space = 0;
			if (dataListObj instanceof List) {
				space = ((List<?>) dataListObj).stream().filter(Map.class::isInstance)
						.flatMap(node -> flatten((Map<String, Object>) node))
						.filter(node -> String.valueOf(node.get("id")).equals(String.valueOf(selectedNodeID)))
						.map(node -> node.get("savailablespace")).filter(Objects::nonNull).mapToInt(val -> {
							if (val instanceof Number) {
								return ((Number) val).intValue();
							}
							try {
								return Integer.parseInt(val.toString());
							} catch (NumberFormatException e) {
								return 0;
							}
						}).findFirst().orElse(0);
			}
			if (selectedCount <= space) {
				List<Map<String, Object>> nodes = (List<Map<String, Object>>) dataListObj;
				List<Map<String, String>> lastNodes = findContainerLastNodes(nodes, selectedNodeID);
				final String storageSeqQry = "select nsequenceno from seqnobasemaster where stablename='samplestoragetransaction' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int nstorageSeq = (int) jdbcTemplate.queryForObject(storageSeqQry, Integer.class);
				int nseqNoSamRecHis = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnobiobankmanagement where stablename='biosamplereceivinghistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);
				String insrtQry = "";
				String updateQry = "";
				String insrtHisQry = "";
				String insrtsamplestorageadditionalinfo="";

				int totalCodes = selectedCount;

				int codeIndex = 0;
				for (Map<String, String> node : lastNodes) {

					String getQry = "select nsamplestoragecontainerpathcode, scontainerlastnode from samplestoragecontainerpath "
							+ "where suid ='"+node.get("id")+"' and nsamplestoragelocationcode = " + nsamplestoragelocationcode + " "
							+ "and nsamplestorageversioncode = " + nsamplestorageversioncode + " " + "and nsitecode = "
							+ userInfo.getNmastersitecode() + " " + "and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					Map<String, Object> containerresult = jdbcTemplate.queryForMap(getQry);
					int nsamplestoragecontainerpathcode = Integer
							.valueOf(containerresult.get("nsamplestoragecontainerpathcode").toString());
					String scontainerlastnode = containerresult.get("scontainerlastnode").toString();
					getQry = "select nsamplestoragemappingcode, nrow, ncolumn, ndirectionmastercode from samplestoragemapping "
							+ "where nsamplestoragecontainerpathcode= " + nsamplestoragecontainerpathcode
							+ " and nstorageinstrumentcode = " + nstorageinstrumentcode + " and nsitecode = "
							+ userInfo.getNtranssitecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					Map<String, Object> mapresult = jdbcTemplate.queryForMap(getQry);
					int nsamplestoragemappingcode = Integer
							.valueOf(mapresult.get("nsamplestoragemappingcode").toString());
					int nrow = Integer.valueOf(mapresult.get("nrow").toString());
					int ncolumn = Integer.valueOf(mapresult.get("ncolumn").toString());
					int ndirectionmastercode = Integer.valueOf(mapresult.get("ndirectionmastercode").toString());
					List<String> generatedOrder = generateOrder(nrow, ncolumn, ndirectionmastercode);
					String placeholders = generatedOrder.stream().map(s -> "'" + s + "'")
							.collect(Collectors.joining(","));
					String sql = "select sposition from samplestoragetransaction " + " WHERE sposition in ("
							+ placeholders + ") and nsamplestoragemappingcode = " + nsamplestoragemappingcode
							+ " and nsamplestoragelocationcode = " + nsamplestoragelocationcode + " and nsitecode = "
							+ userInfo.getNtranssitecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					List<String> existingPositions = jdbcTemplate.queryForList(sql, String.class);
					List<String> missingPositions = generatedOrder.stream()
							.filter(code -> !existingPositions.contains(code)).collect(Collectors.toList());
					int availableSpace = Integer.parseInt(node.get("savailablespace"));
					for (int i = 0; i < availableSpace && codeIndex < totalCodes; i++) {
						nstorageSeq++;
						nseqNoSamRecHis++;
						String slocationcode = scontainerlastnode + "-" + missingPositions.get(i);
						insrtQry += "(" + nstorageSeq + "," + nsamplestoragelocationcode + ","
								+ nsamplestoragemappingcode + "," + objBioParentSampleCollection.getNbioprojectcode()
								+ ",'" + missingPositions.get(i) + "' ,'"
								+ stringUtilityFunction
										.replaceQuote(lstBioSampleReceiving.get(codeIndex).getSrepositoryid())
								+ "' ," + " json_build_object('Parent Sample Code','"
								+ stringUtilityFunction.replaceQuote(
										objBioParentSampleCollection.getSparentsamplecode())
								+ "'," + "'Cohort No.','" + objBioParentSampleCollection.getNcohortno() + "',"
								+ "'Case Type','"
								+ stringUtilityFunction.replaceQuote(objBioParentSampleCollection.getScasetype()) + "'"
//								+ "', 'nproductcatcode'," + objBioParentSampleCollection.getNproductcatcode()
								+ ", 'Parent Sample Type','"
								+ stringUtilityFunction.replaceQuote(objBioParentSampleCollection.getSproductcatname())
								+ "'"
//								+ "', 'nbioprojectcode'," + objBioParentSampleCollection.getNbioprojectcode()
								+ ", 'Project Title','"
								+ stringUtilityFunction.replaceQuote(objBioParentSampleCollection.getSprojecttitle())
								+ "'"
//								+ "', 'nproductcode'," + lstBioSampleReceiving.get(codeIndex).getNproductcode()
								+ ", 'Bio Sample Type','"
								+ stringUtilityFunction.replaceQuote(
										lstBioSampleReceiving.get(codeIndex).getSproductname())
								+ "', 'Volume (μL)','"
								+ stringUtilityFunction.replaceQuote(lstBioSampleReceiving.get(codeIndex).getSqty())
								+ "')::jsonb, " + Enumeration.TransactionStatus.YES.gettransactionstatus() + ", '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ userInfo.getNtranssitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ", "
								+ lstBioSampleReceiving.get(codeIndex).getNbiosamplereceivingcode() + ", "
								+ ninstrumentcode + ", '"
								+ stringUtilityFunction
										.replaceQuote(objBioParentSampleCollection.getSparentsamplecode())
								+ "', " + objBioParentSampleCollection.getNcohortno() + ", "
								+ lstBioSampleReceiving.get(codeIndex).getNproductcode() + ", '"
								+ stringUtilityFunction.replaceQuote(lstBioSampleReceiving.get(codeIndex).getSqty())
								+ "', " + objBioParentSampleCollection.getNproductcatcode() + ", '"
								+ stringUtilityFunction.replaceQuote(slocationcode) + "', '"
								+ stringUtilityFunction.replaceQuote(objBioParentSampleCollection.getSsubjectid()) + "'"
								+ ", '"
								+ stringUtilityFunction.replaceQuote(objBioParentSampleCollection.getScasetype()) + "'"
								+ ", " + lstBioSampleReceiving.get(codeIndex).getNdiagnostictypecode() + ", "
								+ lstBioSampleReceiving.get(codeIndex).getNcontainertypecode() + ", "
								+ lstBioSampleReceiving.get(codeIndex).getNstoragetypecode() + "," + " "
								+ lstBioSampleReceiving.get(codeIndex).getNbioparentsamplecode() + "),";

						updateQry += " update biosamplereceiving set slocationcode = '"
								+ stringUtilityFunction.replaceQuote(slocationcode) + "', ntransactionstatus = "
								+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ", dtransactiondate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntztransactiondate = "
								+ userInfo.getNtimezonecode() + ", noffsettransactiondate = "
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
								+ " where nbiosamplereceivingcode = "
								+ lstBioSampleReceiving.get(codeIndex).getNbiosamplereceivingcode()
								+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						insrtHisQry += " (" + nseqNoSamRecHis + ", "
								+ lstBioSampleReceiving.get(codeIndex).getNbiosamplereceivingcode() + ", "
								+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
								+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
								+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ", '"
								+ stringUtilityFunction.replaceQuote(userInfo.getSreason()) + "', '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
								+ userInfo.getNtimezonecode() + ", " + userInfo.getNtranssitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
						
					// added by sujatha ATE_274 BGSI-218 for inserting into samplestorageadditionalinfo
						insrtsamplestorageadditionalinfo += "(" + nstorageSeq + ", "
							    + (lstBioSampleReceiving.get(codeIndex).getSextractedsampleid() == null || lstBioSampleReceiving.get(codeIndex).getSextractedsampleid().trim().isEmpty()
							    ? "null" : "'" + stringUtilityFunction.replaceQuote(lstBioSampleReceiving.get(codeIndex).getSextractedsampleid()) + "'")
							    + ", "+ (lstBioSampleReceiving.get(codeIndex).getSconcentration() == null || lstBioSampleReceiving.get(codeIndex).getSconcentration().trim().isEmpty()
							    ? "null" : "'" + stringUtilityFunction.replaceQuote(lstBioSampleReceiving.get(codeIndex).getSconcentration()) + "'")
							    + ", "+ (lstBioSampleReceiving.get(codeIndex).getSqcplatform() == null || lstBioSampleReceiving.get(codeIndex).getSqcplatform().trim().isEmpty()
							    ? "null" : "'" + stringUtilityFunction.replaceQuote(lstBioSampleReceiving.get(codeIndex).getSqcplatform()) + "'")
							    + ", "+ (lstBioSampleReceiving.get(codeIndex).getSeluent() == null || lstBioSampleReceiving.get(codeIndex).getSeluent().trim().isEmpty()
							    ? "null" : "'" + stringUtilityFunction.replaceQuote(lstBioSampleReceiving.get(codeIndex).getSeluent()) + "'")
							    + ", '"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							    + userInfo.getNtranssitecode() + ", "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+ "),";

						codeIndex++;
					}
					if (codeIndex >= totalCodes) {
						break;
					}
				}
				if(insrtQry.length()>0) {
					
					final String sPartitionTransaction = " CREATE TABLE IF NOT EXISTS  samplestoragetransaction_"
							+ objBioParentSampleCollection.getNbioprojectcode()
							+ " PARTITION OF samplestoragetransaction  FOR VALUES IN ("
							+ objBioParentSampleCollection.getNbioprojectcode() + ");";
					jdbcTemplate.execute(sPartitionTransaction);

				}

				insrtQry = " INSERT INTO public.samplestoragetransaction("
						+ " nsamplestoragetransactioncode, nsamplestoragelocationcode, nsamplestoragemappingcode, nprojecttypecode, sposition, spositionvalue, jsondata, npositionfilled, dmodifieddate, nsitecode, nstatus, nbiosamplereceivingcode,ninstrumentcode,sparentsamplecode,ncohortno,nproductcode,sqty,nproductcatcode,slocationcode,ssubjectid,scasetype,ndiagnostictypecode,ncontainertypecode,nstoragetypecode,nbioparentsamplecode)"
						+ "	VALUES" + insrtQry.substring(0, insrtQry.length() - 1) + ";";
				insrtHisQry = " INSERT INTO public.biosamplereceivinghistory("
						+ "	nbiosamplereceivinghistorycode, nbiosamplereceivingcode, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, ntransactionstatus, scomments, dtransactiondate, ntztransactiondate, noffsettransactiondate, nsitecode, nstatus)"
						+ "	VALUES" + insrtHisQry.substring(0, insrtHisQry.length() - 1) + ";";
				updateQry = updateQry + " update seqnobasemaster set nsequenceno = " + nstorageSeq
						+ " where stablename = 'samplestoragetransaction' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";
				updateQry = updateQry + " update seqnobiobankmanagement set nsequenceno = " + nseqNoSamRecHis
						+ " where stablename = 'biosamplereceivinghistory' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

				// added by sujatha ATE_274 BGSI-218
				insrtsamplestorageadditionalinfo=" INSERT INTO public.samplestorageadditionalinfo("
						+ " nsamplestoragetransactioncode, sextractedsampleid, sconcentration, sqcplatform, seluent, dmodifieddate, nsitecode, nstatus)"
						+ " VALUES"+insrtsamplestorageadditionalinfo.substring(0, insrtsamplestorageadditionalinfo.length() - 1) + ";";
				
				//modified by sujatha ATE_274 by adding insrtsamplestorageadditionalinfo to execute
				jdbcTemplate.execute(insrtQry + insrtHisQry + insrtsamplestorageadditionalinfo + updateQry);
				
				// ===== COC: START =====
				if (sbiosamplereceivingcodes != null && !sbiosamplereceivingcodes.trim().isEmpty()) {
					
					String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery1);
			 
					String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery2);
			 
					String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					jdbcTemplate.execute(sQuery3);
					
					
					int chainCustodyPk = jdbcTemplate.queryForObject(
							"select max(nsequenceno) from seqnoregistration where stablename='chaincustody' and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);

					String lblParentSampleCode = stringUtilityFunction.replaceQuote(commonFunction
							.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename()));
					String lblCohortNo = stringUtilityFunction.replaceQuote(
							commonFunction.getMultilingualMessage("IDS_COHORTNO", userInfo.getSlanguagefilename()));
					String lblProduct = stringUtilityFunction.replaceQuote(
							commonFunction.getMultilingualMessage("IDS_PRODUCT", userInfo.getSlanguagefilename()));
//					String lblCaseType = stringUtilityFunction.replaceQuote(
//							commonFunction.getMultilingualMessage("IDS_CASETYPE", userInfo.getSlanguagefilename()));
					String lblBioSampleType = stringUtilityFunction.replaceQuote(commonFunction
							.getMultilingualMessage("IDS_BIOSAMPLETYPE", userInfo.getSlanguagefilename()));
//					String lblContainerType = stringUtilityFunction.replaceQuote(commonFunction
//							.getMultilingualMessage("IDS_CONTAINERTYPE", userInfo.getSlanguagefilename()));
					String lblRepositoryId = stringUtilityFunction.replaceQuote(
							commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename()));
//					String lblLocation = stringUtilityFunction.replaceQuote(
//							commonFunction.getMultilingualMessage("IDS_LOCATIONCODE", userInfo.getSlanguagefilename()));
//					String lblVolume = stringUtilityFunction.replaceQuote(
//							commonFunction.getMultilingualMessage("IDS_VOLUMEMICROL", userInfo.getSlanguagefilename()));
					String lblSiteName = stringUtilityFunction.replaceQuote(
				            commonFunction.getMultilingualMessage("IDS_SITENAME", userInfo.getSlanguagefilename()));
			
					String strChainCustody = "insert into chaincustody ( "
							+ "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno, "
							+ "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate, "
							+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus) " + "select " + chainCustodyPk
							+ " + rank() over(order by br.nbiosamplereceivingcode), " + userInfo.getNformcode() + ", "
							+ " br.nbiosamplereceivingcode, 'nbiosamplereceivingcode', 'biosamplereceiving', br.srepositoryid, "
							+ Enumeration.TransactionStatus.STORED.gettransactionstatus() + ", "
							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
							+ lblParentSampleCode + " [' || COALESCE(bpr.sparentsamplecode, '') || '] , " + lblCohortNo
							+ " [' || COALESCE(bpr.ncohortno::text, '') || '] , " + lblProduct
							+ " [' || COALESCE(pc.sproductcatname, '') || '] , " 
//							+ lblCaseType
//							+ " [' || COALESCE(bpr.scasetype, '') || '] , " 
//							+ lblVolume
//							+ " [' || COALESCE(br.sqty, '') || '] , " 
							+ lblBioSampleType + " [' || COALESCE(pt.sproductname, '') || '] , "
//							+ lblContainerType
//							+ " [' || COALESCE(ct.scontainertype, '') || '] , " 
//							+ lblLocation
//							+ " [' || COALESCE(br.slocationcode, '') || '] , " 
							+ lblRepositoryId + " [' || COALESCE(br.srepositoryid, '') || '] "
							+ " , " + lblSiteName + " [' || COALESCE(si.ssitename, '') || '] ' ,"
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "from biosamplereceiving br  "
							+ "join bioparentsamplecollection bpc on bpc.nbioparentsamplecollectioncode = br.nbioparentsamplecollectioncode "
							+ "and bpc.nsitecode = " + userInfo.getNtranssitecode() + " " + "and bpc.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "join bioparentsamplereceiving bpr on bpr.nbioparentsamplecode = br.nbioparentsamplecode  "
							+ "and bpr.nsitecode = " + userInfo.getNtranssitecode() + " " + "and bpr.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "join product pt on br.nproductcode = pt.nproductcode and pt.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "join storagetype st on br.nstoragetypecode = st.nstoragetypecode and st.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "join diagnostictype dt on br.ndiagnostictypecode = dt.ndiagnostictypecode and dt.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "join containertype ct on br.ncontainertypecode = ct.ncontainertypecode and ct.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ " left join productcategory pc on pc.nproductcatcode = bpc.nproductcatcode "
				            + " left join site si on si.nsitecode = br.nsitecode and si.nstatus = "
				            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " where br.nsitecode = " + userInfo.getNtranssitecode() + " and br.nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and br.nbiosamplereceivingcode in (" + sbiosamplereceivingcodes + ");";



					String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
							+ " + count(nbiosamplereceivingcode) " + " from biosamplereceiving"
							+ " where nbiosamplereceivingcode in (" + sbiosamplereceivingcodes + ")"
							+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ ") where stablename = 'chaincustody' and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						jdbcTemplate.execute(strChainCustody);
					jdbcTemplate.execute(strSeqUpdate);
				}
				// ===== End COC =====
				
				final List<Object> listAfterUpdate = new ArrayList<>();
				final List<BioSampleReceiving> lstBioSampleRecAfter = getProcessedSampleListUsingBioSampleReceivingCode(
						sbiosamplereceivingcodes, userInfo);
				listAfterUpdate.addAll(lstBioSampleRecAfter);
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				final Map<String, Object> outputMap = new HashMap<>();
				outputMap.put("nbioparentsamplecollectioncode",
						objBioParentSampleCollection.getNbioparentsamplecollectioncode());

				outputMap.putAll((Map<String, Object>) getActiveSampleCollection(outputMap, userInfo).getBody());
				return new ResponseEntity<>(outputMap, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_AVAILABLESPACEISLESSTHANSELECTEDNOOFSAMPLES",
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTALIQUOTEDSTATUSSAMPLESTOSTORE",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);

		}
	}

	private static Stream<Map<String, Object>> flatten(Map<String, Object> node) {
		Stream<Map<String, Object>> self = Stream.of(node);

		Object items = node.get("items");
		if (items instanceof List) {
			Stream<Map<String, Object>> children = ((List<?>) items).stream().filter(Map.class::isInstance)
					.flatMap(child -> flatten((Map<String, Object>) child));
			return Stream.concat(self, children);
		}
		return self;
	}

	public static List<Map<String, String>> findContainerLastNodes(List<Map<String, Object>> nodes,
			String selectedNodeID) {
		for (Map<String, Object> node : nodes) {
			if (selectedNodeID.toString().equals(node.get("id").toString())) {
				List<Map<String, String>> result = new ArrayList<>();
				if (Boolean.TRUE.equals(node.get("containerlastnode"))) {
					// If the selected node itself is containerlastnode
					Map<String, String> data = new HashMap<>();
					data.put("id", node.get("id").toString());
					data.put("itemhierarchy", node.get("itemhierarchy").toString());
					data.put("savailablespace", node.get("savailablespace").toString());
					result.add(data);
				} else {
					// Otherwise, search its descendants
					List<Map<String, Object>> items = (List<Map<String, Object>>) node.get("items");
					collectContainerLastNodes(items, result);
				}
				return result;
			}
			// Search recursively in children
			List<Map<String, Object>> items = (List<Map<String, Object>>) node.get("items");
			if (items != null) {
				List<Map<String, String>> found = findContainerLastNodes(items, selectedNodeID);
				if (!found.isEmpty()) {
					return found;
				}
			}
		}
		return Collections.emptyList();
	}

	// Helper to collect all descendants that are containerlastnode=true
	private static void collectContainerLastNodes(List<Map<String, Object>> nodes, List<Map<String, String>> result) {
		if (nodes == null)
			return;
		for (Map<String, Object> node : nodes) {
			if (Boolean.TRUE.equals(node.get("containerlastnode"))) {
				Map<String, String> data = new HashMap<>();
				data.put("id", node.get("id").toString());
				data.put("itemhierarchy", node.get("itemhierarchy").toString());
				data.put("savailablespace", node.get("savailablespace").toString());
				result.add(data);
			}
			List<Map<String, Object>> items = (List<Map<String, Object>>) node.get("items");
			if (items != null) {
				collectContainerLastNodes(items, result);
			}
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

}
