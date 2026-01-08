package com.agaramtech.qualis.biobank.service.bioparentsamplereceiving;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
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
import org.springframework.web.client.RestTemplate;

import com.agaramtech.qualis.basemaster.model.StorageCondition;
import com.agaramtech.qualis.biobank.model.BioParentSampleCollection;
import com.agaramtech.qualis.biobank.model.BioParentSampleReceiving;
import com.agaramtech.qualis.biobank.model.ExternalUrlSettings;
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
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.product.model.ProductCategory;
import com.agaramtech.qualis.project.model.BioProject;
import com.agaramtech.qualis.project.model.Disease;
import com.agaramtech.qualis.project.model.Hospital;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class BioParentSampleReceivingDAOImpl implements BioParentSampleReceivingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(BioParentSampleReceivingDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final EmailDAOSupport emailDAOSupport;
	private ValidatorDel validatorDel;

	@Override
	public ResponseEntity<Object> getBioParentSampleReceiving(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		String fromDate = (String) inputMap.get("fromDate");
		String toDate = (String) inputMap.get("toDate");

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

		// Modified query by Gowtham on nov 14 2025 for jira.id:BGSI-216
//		final String strQuery = "select bspr.nbioparentsamplecode, bspr.sparentsamplecode, bspr.ssubjectid, bspr.scasetype, bspr.ndiseasecode, d.sdiseasename, bspr.nbioprojectcode, bp.sprojecttitle, bp.sprojectcode, "
//				+ "u.sfirstname||' '|| u.slastname as sprojectincharge, bspr.ncohortno, bspr.ncollectionsitecode, cs.ssitename as scollectionsitename, "
//				+ "bspr.ncollectedhospitalcode, ch.shospitalname, bspr.nstorageinstrumentcode, i.sinstrumentid, sc.sstorageconditionname as sstoragetemperature,"
//				+ "bspr.darrivaldate, bspr.ntzarrivaldate, bspr.noffsetarrivaldate, tz1.stimezoneid AS stzarrivaldate, "
//				+ "COALESCE(TO_CHAR(bspr.darrivaldate,'" + userInfo.getSsitedate() + "'), '') AS sarrivaldate, "
//				+ "bspr.dtransactiondate, bspr.ntztransactiondate, bspr.noffsetarrivaldate, tz2.stimezoneid AS stztransactiondate, "
//				+ "COALESCE(TO_CHAR(bspr.dtransactiondate, '" + userInfo.getSsitedate() + "'), '') AS stransactiondate "
//				+ "from bioparentsamplereceiving bspr, disease d, bioproject bp, users u, site cs, hospital ch, storageinstrument ssl,storagecondition sc, instrument i, timezone tz1, "
//				+ "timezone tz2 "
//				+ "where bspr.ndiseasecode = d.ndiseasecode and bspr.nbioprojectcode = bp.nbioprojectcode and bp.nusercode = u.nusercode "
//				+ "and bspr.ncollectionsitecode = cs.nsitecode and bspr.ncollectedhospitalcode = ch.nhospitalcode "
//				+ "and ssl.nstorageinstrumentcode = bspr.nstorageinstrumentcode and ssl.nstorageconditioncode = sc.nstorageconditioncode "
//				+ "and ssl.ninstrumentcode = i.ninstrumentcode " + "and bspr.ntzarrivaldate = tz1.ntimezonecode "
//				+ "and bspr.ntztransactiondate = tz2.ntimezonecode " + "and bspr.nsitecode = "
//				+ userInfo.getNtranssitecode() + " " + "and d.nsitecode = " + userInfo.getNmastersitecode() + " "
//				+ "and bp.nsitecode= " + userInfo.getNmastersitecode() + " " + "and u.nsitecode = "
//				+ userInfo.getNmastersitecode() + " " + "and ch.nsitecode= " + userInfo.getNmastersitecode() + " "
//				+ "and sc.nsitecode= " + userInfo.getNmastersitecode() + " " + "and ssl.nregionalsitecode in ( "
//				+ userInfo.getNtranssitecode() + ", -1) " + "and bspr.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and d.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and bp.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and u.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and cs.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ch.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ssl.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and i.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sc.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and tz1.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and tz2.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and bspr.dtransactiondate between '" + fromDate + "' and '" + toDate + "'" + " "
//				+ " order by bspr.nbioparentsamplecode";

		final String strQuery = "SELECT bspr.nbioparentsamplecode, bspr.sparentsamplecode, bspr.ssubjectid, bspr.scasetype, "
				+ " bspr.ndiseasecode, d.sdiseasename, bspr.nbioprojectcode, bp.sprojecttitle, bp.sprojectcode, "
				+ " u.sfirstname || ' ' || u.slastname AS sprojectincharge, bspr.ncohortno, bspr.ncollectionsitecode, "
				+ " cs.ssitename AS scollectionsitename, bspr.ncollectedhospitalcode, ch.shospitalname, "
				+ " bspr.nstorageinstrumentcode, i.sinstrumentid, sc.sstorageconditionname AS sstoragetemperature, "
				+ " bspr.darrivaldate, bspr.ntzarrivaldate, bspr.noffsetarrivaldate, tz1.stimezoneid AS stzarrivaldate, "
				+ " COALESCE(TO_CHAR(bspr.darrivaldate, '" + userInfo.getSsitedate() + "'), '') AS sarrivaldate, "
				+ " bspr.dtransactiondate, bspr.ntztransactiondate, bspr.noffsetarrivaldate, bsd.nisthirdpartysharable, "
				+ " bsd.nissampleaccesable, CONCAT('"
				+ commonFunction.getMultilingualMessage("IDS_THIRDPARTYSHARABLE", userInfo.getSlanguagefilename())
				+ "',COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts1.jsondata->'stransdisplaystatus'->>'en-US')) as sisthirdpartysharable, " + " CONCAT('"
				+ commonFunction.getMultilingualMessage("IDS_SAMPLEACCESABLE", userInfo.getSlanguagefilename())
				+ "',COALESCE(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ " ',ts2.jsondata->'stransdisplaystatus'->>'en-US')) as sissampleaccesable, tz2.stimezoneid AS stztransactiondate, "
				+ " COALESCE(TO_CHAR(bspr.dtransactiondate, '" + userInfo.getSsitedate()
				+ "'), '') AS stransactiondate " + " FROM bioparentsamplereceiving bspr "
				+ " JOIN disease d ON bspr.ndiseasecode = d.ndiseasecode "
				+ " JOIN bioproject bp ON bspr.nbioprojectcode = bp.nbioprojectcode "
				+ " JOIN biosubjectdetails bsd ON bspr.ssubjectid = bsd.ssubjectid "
				+ " JOIN users u ON bp.nusercode = u.nusercode "
				+ " JOIN site cs ON bspr.ncollectionsitecode = cs.nsitecode "
				+ " JOIN hospital ch ON bspr.ncollectedhospitalcode = ch.nhospitalcode "
				+ " JOIN storageinstrument ssl ON ssl.nstorageinstrumentcode = bspr.nstorageinstrumentcode "
				+ " JOIN storagecondition sc ON ssl.nstorageconditioncode = sc.nstorageconditioncode "
				+ " JOIN instrument i ON ssl.ninstrumentcode = i.ninstrumentcode "
				+ " JOIN timezone tz1 ON bspr.ntzarrivaldate = tz1.ntimezonecode "
				+ " JOIN timezone tz2 ON bspr.ntztransactiondate = tz2.ntimezonecode "
				+ " JOIN transactionstatus ts1 ON bsd.nisthirdpartysharable = ts1.ntranscode "
				+ " JOIN transactionstatus ts2 ON bsd.nissampleaccesable = ts2.ntranscode " + " WHERE bspr.nsitecode = "
				+ userInfo.getNtranssitecode() + " and d.nsitecode = " + userInfo.getNmastersitecode()
				+ " and bp.nsitecode= " + userInfo.getNmastersitecode() + " and u.nsitecode = "
				+ userInfo.getNmastersitecode() + " and ch.nsitecode= " + userInfo.getNmastersitecode()
				+ " and bsd.nsitecode= " + userInfo.getNmastersitecode() + " and ssl.nregionalsitecode in ( "
				+ userInfo.getNtranssitecode() + ", -1) and bspr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bsd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ch.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tz1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tz2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bspr.dtransactiondate between '"
				+ fromDate + "' and '" + toDate + "'" + " order by bspr.nbioparentsamplecode";

		final List<BioParentSampleReceiving> lstBioParentSampleReceiving = jdbcTemplate.query(strQuery,
				new BioParentSampleReceiving());

		if (!lstBioParentSampleReceiving.isEmpty()) {
			outputMap.put("lstBioParentSampleReceiving", lstBioParentSampleReceiving);
			outputMap.put("selectedBioParentSampleReceiving",
					lstBioParentSampleReceiving.get(lstBioParentSampleReceiving.size() - 1));
			outputMap.put("lstBioParentSampleCollection", getBioParentSampleCollection(
					lstBioParentSampleReceiving.get(lstBioParentSampleReceiving.size() - 1).getNbioparentsamplecode(),
					userInfo));

		} else {
			outputMap.put("lstBioParentSampleReceiving", null);
			outputMap.put("selectedBioParentSampleReceiving", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public List<BioParentSampleCollection> getBioParentSampleCollection(final int nbioparentsamplecode,
			final UserInfo userInfo) throws Exception {

		final String strQuery = "select bpsc.nbioparentsamplecollectioncode, bpsc.nbioparentsamplecode, bpsr.sparentsamplecode, bpsr.ncohortno, u.sfirstname||' '|| u.slastname as sprojectincharge, bp.sprojecttitle, bp.sprojectcode, "
				+ "cs.ssitename as scollectionsitename, pc.sproductcatname, bpsc.nproductcatcode,  bpsc.nnoofsamples,sc.nstorageconditioncode, sc.sstorageconditionname, "
				+ "bpsc.scollectorname, bpsc.stemporarystoragename, bpsc.ssendername, bpsc.sinformation, bpsc.nrecipientusercode, "
				+ "u1.sfirstname||' '|| u1.slastname as srecipientusername, bpsc.jsonuidata, bpsc.ntransactionstatus, "
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus, "
				+ "bpsc.dsamplecollectiondate, bpsc.ntzsamplecollectiondate, bpsc.noffsetsamplecollectiondate, tz1.stimezoneid AS stzsamplecollectiondate, "
				+ "COALESCE(TO_CHAR(bpsc.dsamplecollectiondate,'" + userInfo.getSsitedate()
				+ "'), '') AS ssamplecollectiondate, "
				+ "bpsc.dbiobankarrivaldate, bpsc.ntzbiobankarrivaldate, bpsc.noffsetbiobankarrivaldate, tz2.stimezoneid AS stzbiobankarrivaldate, "
				+ "COALESCE(TO_CHAR(bpsc.dbiobankarrivaldate,'" + userInfo.getSsitedate()
				+ "'), '') AS sbiobankarrivaldate, "
				+ "bpsc.dtemporarystoragedate, bpsc.ntztemporarystoragedate, bpsc.noffsettemporarystoragedate, tz3.stimezoneid AS stztemporarystoragedate, "
				+ "COALESCE(TO_CHAR(bpsc.dtemporarystoragedate,'" + userInfo.getSsitedate()
				+ "'), '') AS stemporarystoragedate, "
				+ "bpsc.dtransactiondate, bpsc.ntztransactiondate, bpsc.noffsettransactiondate, tz4.stimezoneid AS stztransactiondate, "
				+ "COALESCE(TO_CHAR(bpsc.dtransactiondate,'" + userInfo.getSsitedate() + "'), '') AS stransactiondate "
				+ "from bioparentsamplecollection bpsc, bioparentsamplereceiving bpsr, bioproject bp, users u, site cs, site rs, productcategory pc, "
				// changed input data to storagecondition master by sathish -> 06-AUG-2025
				+ "storagecondition sc, users u1, transactionstatus ts, timezone tz1, timezone tz2, timezone tz3, timezone tz4 "
				+ "where bpsc.nbioparentsamplecode = bpsr.nbioparentsamplecode and bpsc.nstorageconditioncode = sc.nstorageconditioncode and bpsc.nbioparentsamplecode = "
				+ nbioparentsamplecode + " and bpsc.nsitecode = " + userInfo.getNtranssitecode() + " "
				+ "and bpsr.nsitecode = " + userInfo.getNtranssitecode() + " " + "and bpsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and bpsr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsr.nbioprojectcode = bp.nbioprojectcode " + "and bp.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + "and bp.nusercode = u.nusercode " + "and u.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + "and u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsr.ncollectionsitecode = cs.nsitecode and cs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsc.nreceivingsitecode = rs.nsitecode and rs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsc.nproductcatcode =  pc.nproductcatcode and pc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsc.nrecipientusercode = u1.nusercode and u1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsc.ntransactionstatus = ts.ntranscode "
				+ "and bpsc.ntzsamplecollectiondate = tz1.ntimezonecode and tz1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsc.ntzbiobankarrivaldate = tz2.ntimezonecode and tz2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsc.ntztemporarystoragedate = tz3.ntimezonecode and tz3.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsc.ntztransactiondate = tz4.ntimezonecode and tz4.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by bpsc.nbioparentsamplecollectioncode desc";

		return jdbcTemplate.query(strQuery, new BioParentSampleCollection());
	}

	@Override
	public ResponseEntity<Object> getActiveBioParentSampleReceiving(final int nbioparentsamplecode,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		// Modified query by Gowtham on nov 14 2025 for jira.id:BGSI-216
//		final String strQuery = "select bspr.nbioparentsamplecode, bspr.sparentsamplecode, bspr.ssubjectid, bspr.scasetype, bspr.ndiseasecode, d.sdiseasename, bspr.nbioprojectcode, bp.sprojecttitle, bp.sprojectcode, "
//				+ "u.sfirstname||' '|| u.slastname as sprojectincharge, bspr.ncohortno, bspr.ncollectionsitecode, cs.ssitename as scollectionsitename, "
//				+ "bspr.ncollectedhospitalcode, ch.shospitalname, bspr.nstorageinstrumentcode, i.sinstrumentid, sc.sstorageconditionname as sstoragetemperature,"
//				+ "bspr.darrivaldate, bspr.ntzarrivaldate, bspr.noffsetarrivaldate, tz1.stimezoneid AS stzarrivaldate, "
//				+ "COALESCE(TO_CHAR(bspr.darrivaldate,'" + userInfo.getSsitedate() + "'), '') AS sarrivaldate, "
//				+ "bspr.dtransactiondate, bspr.ntztransactiondate, bspr.noffsetarrivaldate, tz2.stimezoneid AS stztransactiondate, "
//				+ "COALESCE(TO_CHAR(bspr.dtransactiondate, '" + userInfo.getSsitedate() + "'), '') AS stransactiondate "
//				+ "from bioparentsamplereceiving bspr, disease d, bioproject bp, users u, site cs, hospital ch, storageinstrument ssl, storagecondition sc, instrument i, timezone tz1, "
//				+ "timezone tz2 " + "where bspr.nbioparentsamplecode = " + nbioparentsamplecode + " "
//				+ "and bspr.ndiseasecode = d.ndiseasecode and bspr.nbioprojectcode = bp.nbioprojectcode and bp.nusercode = u.nusercode "
//				+ "and bspr.ncollectionsitecode = cs.nsitecode and bspr.ncollectedhospitalcode = ch.nhospitalcode "
//				+ "and ssl.nstorageinstrumentcode = bspr.nstorageinstrumentcode and ssl.nstorageconditioncode = sc.nstorageconditioncode and ssl.ninstrumentcode = i.ninstrumentcode and bspr.ntzarrivaldate = tz1.ntimezonecode "
//				+ "and bspr.ntztransactiondate = tz2.ntimezonecode " + "and bspr.nsitecode = "
//				+ userInfo.getNtranssitecode() + " " + "and d.nsitecode = " + userInfo.getNmastersitecode() + " "
//				+ "and bp.nsitecode= " + userInfo.getNmastersitecode() + " " + "and u.nsitecode = "
//				+ userInfo.getNmastersitecode() + " " + "and ch.nsitecode= " + userInfo.getNmastersitecode() + " "
//				+ "and sc.nsitecode= " + userInfo.getNmastersitecode() + " " + "and ssl.nregionalsitecode in ( "
//				+ userInfo.getNtranssitecode() + ", -1) " + "and bspr.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and d.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and bp.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and u.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and cs.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ch.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ssl.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and i.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sc.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and tz1.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and tz2.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by bspr.nbioparentsamplecode";

		final String strQuery = "SELECT bspr.nbioparentsamplecode, bspr.sparentsamplecode, bspr.ssubjectid, bspr.scasetype, "
				+ " bspr.ndiseasecode, d.sdiseasename, bspr.nbioprojectcode, bp.sprojecttitle, bp.sprojectcode, "
				+ " u.sfirstname || ' ' || u.slastname AS sprojectincharge, bspr.ncohortno, bspr.ncollectionsitecode, "
				+ " cs.ssitename AS scollectionsitename, bspr.ncollectedhospitalcode, ch.shospitalname, "
				+ " bspr.nstorageinstrumentcode, i.sinstrumentid, sc.sstorageconditionname AS sstoragetemperature, "
				+ " bspr.darrivaldate, bspr.ntzarrivaldate, bspr.noffsetarrivaldate, tz1.stimezoneid AS stzarrivaldate, "
				+ " COALESCE(TO_CHAR(bspr.darrivaldate, '" + userInfo.getSsitedate() + "'), '') AS sarrivaldate, "
				+ " bspr.dtransactiondate, bspr.ntztransactiondate, bspr.noffsetarrivaldate, bsd.nisthirdpartysharable, "
				+ " bsd.nissampleaccesable, CONCAT('"
				+ commonFunction.getMultilingualMessage("IDS_THIRDPARTYSHARABLE", userInfo.getSlanguagefilename())
				+ "',COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts1.jsondata->'stransdisplaystatus'->>'en-US')) as sisthirdpartysharable, " + " CONCAT('"
				+ commonFunction.getMultilingualMessage("IDS_SAMPLEACCESABLE", userInfo.getSlanguagefilename())
				+ "',COALESCE(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ " ',ts2.jsondata->'stransdisplaystatus'->>'en-US')) as sissampleaccesable, tz2.stimezoneid AS stztransactiondate, "
				+ " COALESCE(TO_CHAR(bspr.dtransactiondate, '" + userInfo.getSsitedate()
				+ "'), '') AS stransactiondate " + " FROM bioparentsamplereceiving bspr "
				+ " JOIN disease d ON bspr.ndiseasecode = d.ndiseasecode "
				+ " JOIN bioproject bp ON bspr.nbioprojectcode = bp.nbioprojectcode "
				+ " JOIN biosubjectdetails bsd ON bspr.ssubjectid = bsd.ssubjectid "
				+ " JOIN users u ON bp.nusercode = u.nusercode "
				+ " JOIN site cs ON bspr.ncollectionsitecode = cs.nsitecode "
				+ " JOIN hospital ch ON bspr.ncollectedhospitalcode = ch.nhospitalcode "
				+ " JOIN storageinstrument ssl ON ssl.nstorageinstrumentcode = bspr.nstorageinstrumentcode "
				+ " JOIN storagecondition sc ON ssl.nstorageconditioncode = sc.nstorageconditioncode "
				+ " JOIN instrument i ON ssl.ninstrumentcode = i.ninstrumentcode "
				+ " JOIN timezone tz1 ON bspr.ntzarrivaldate = tz1.ntimezonecode "
				+ " JOIN timezone tz2 ON bspr.ntztransactiondate = tz2.ntimezonecode "
				+ " JOIN transactionstatus ts1 ON bsd.nisthirdpartysharable = ts1.ntranscode "
				+ " JOIN transactionstatus ts2 ON bsd.nissampleaccesable = ts2.ntranscode "
				+ " WHERE bspr.nbioparentsamplecode = " + nbioparentsamplecode + " and bspr.nsitecode = "
				+ userInfo.getNtranssitecode() + " and d.nsitecode = " + userInfo.getNmastersitecode()
				+ " and bp.nsitecode= " + userInfo.getNmastersitecode() + " and u.nsitecode = "
				+ userInfo.getNmastersitecode() + " and ch.nsitecode= " + userInfo.getNmastersitecode()
				+ " and bsd.nsitecode= " + userInfo.getNmastersitecode() + " and ssl.nregionalsitecode in ( "
				+ userInfo.getNtranssitecode() + ", -1) and bspr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bsd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ch.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and i.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tz1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tz2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by bspr.nbioparentsamplecode";

		final BioParentSampleReceiving objBioParentSampleReceiving = (BioParentSampleReceiving) jdbcUtilityTemplateFunction
				.queryForObject(strQuery, BioParentSampleReceiving.class, jdbcTemplate);

		if (objBioParentSampleReceiving != null) {
			outputMap.putAll(
					getUserAndProductCatDetails(-1, objBioParentSampleReceiving.getNbioparentsamplecode(), userInfo));
			outputMap.put("selectedBioParentSampleReceiving", objBioParentSampleReceiving);
			outputMap.put("lstBioParentSampleCollection",
					getBioParentSampleCollection(objBioParentSampleReceiving.getNbioparentsamplecode(), userInfo));

		} else {
			outputMap.put("sampleTypeList", null);
			outputMap.put("recipientsList", null);
			outputMap.put("selectedBioParentSampleReceiving", null);
			outputMap.put("lstBioParentSampleCollection", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	public Map<String, Object> getUserAndProductCatDetails(final int nbioparentsamplecollectioncode,
			final int nbioparentsamplecode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
//		final String strUserQuery = "SELECT CONCAT(sfirstname, ' ', slastname) AS srecipientusername,nusercode as nrecipientusercode "
//				+ "FROM users where nusercode > 0 and  nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
//				+ userInfo.getNmastersitecode() + " order by nusercode desc;";

		final String strUserQuery = "select CONCAT(us.sfirstname, ' ', us.slastname) as srecipientusername,us.nusercode as nrecipientusercode from usermultirole umr, userrole ur, users us, userssite usite , userroleconfig cf  "
				+ "where umr.nuserrolecode = ur.nuserrolecode and usite.nusercode = us.nusercode and umr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and umr.ntransactionstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.ntransactionstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ur.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and umr.nusersitecode = usite.nusersitecode and usite.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and usite.nsitecode = "
				+ userInfo.getNtranssitecode() + " and ur.nuserrolecode =cf.nuserrolecode and cf.nneedtechnicianflow="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and us.nusercode >0  order by us.nusercode desc ;";

		final List<Map<String, Object>> lstRecipients = jdbcTemplate.queryForList(strUserQuery);

		final String strSampleType = "select nproductcatcode, sproductcatname, ndefaultstatus from productcategory where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and nproductcatcode > 0 and nproductcatcode not in "
				+ "(select nproductcatcode from bioparentsamplecollection where nbioparentsamplecode="
				+ nbioparentsamplecode + " and nbioparentsamplecollectioncode != " + nbioparentsamplecollectioncode
				+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") order by nproductcatcode desc";
		final List<ProductCategory> lstSampleType = jdbcTemplate.query(strSampleType, new ProductCategory());

		final String strStorageCondition = "select nstorageconditioncode,sstorageconditionname from storagecondition where nsitecode = "
				+ userInfo.getNmastersitecode() + " " + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nstorageconditioncode > 0 order by nstorageconditioncode desc";
		final List<StorageCondition> lststrStorageCondition = jdbcTemplate.query(strStorageCondition,
				new StorageCondition());

		outputMap.put("storageConditionList", lststrStorageCondition);
		outputMap.put("sampleTypeList", lstSampleType);
		outputMap.put("recipientsList", lstRecipients);
		return outputMap;
	}

	@Override
	public ResponseEntity<Object> getBioProjectforLoggedInSite(final int ndiseasecode, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select bp.nbioprojectcode, bp.sprojecttitle, bp.sprojectcode, u.sfirstname||' '||u.slastname as suserName "
				+ "from bioproject bp, users u, projectsitemapping psm " + "where bp.ndiseasecode = " + ndiseasecode
				+ " " + "and psm.nbioprojectcode = bp.nbioprojectcode " + "and bp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and psm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and bp.nusercode = u.nusercode "
				+ "and u.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and psm.nnodesitecode = " + userInfo.getNtranssitecode() + " and bp.nsitecode = "
				+ userInfo.getNmastersitecode();

		final List<BioProject> lstBioProject = jdbcTemplate.query(strQuery, new BioProject());

		if (!lstBioProject.isEmpty()) {
			outputMap.put("lstBioProject", lstBioProject);
			outputMap.put("selectedBioProject", lstBioProject.get(lstBioProject.size() - 1));

		} else {
			outputMap.put("lstBioProject", null);
			outputMap.put("selectedBioProject", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getDiseaseforLoggedInSite(final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select ndiseasecode,sdiseasename from disease where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode() + " and ndiseasecode > 0 order by ndiseasecode";

		final List<Disease> lstDisease = jdbcTemplate.query(strQuery, new Disease());

		if (!lstDisease.isEmpty()) {
			outputMap.put("lstDisease", lstDisease);
			outputMap.put("selectedDisease", lstDisease.get(lstDisease.size() - 1));
			outputMap.put("lstBioProject",
					getBioProjectforLoggedInSite(lstDisease.get(lstDisease.size() - 1).getNdiseasecode(), userInfo));

		} else {
			outputMap.put("lstDisease", null);
			outputMap.put("selectedDisease", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getCollectionSiteBasedonProject(final int nbioprojectcode, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		String strQuery = "select shcd.schildsitecode "
				+ "from projectsitehierarchymapping pshm, sitehierarchyconfigdetails shcd "
				+ "where pshm.nsitehierarchyconfigcode = shcd.nsitehierarchyconfigcode " + "and pshm.nbioprojectcode = "
				+ nbioprojectcode + " " + "and shcd.nnodesitecode = " + userInfo.getNtranssitecode() + " "
				+ "and pshm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and shcd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and pshm.nsitecode = " + userInfo.getNmastersitecode();

		final String schildsitecode = jdbcTemplate.queryForObject(strQuery, String.class);

		String ssitecode = "" + userInfo.getNtranssitecode();
		if (schildsitecode != null && !schildsitecode.isEmpty()) {
			ssitecode = ssitecode + "," + schildsitecode;
		}
		strQuery = "select nsitecode, ssitename from site " + "where nsitecode in (" + ssitecode + ") "
				+ "and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

		final List<Site> lstSite = jdbcTemplate.query(strQuery, new Site());

		if (!lstSite.isEmpty()) {
			outputMap.put("lstSite", lstSite);
			outputMap.put("selectedSite", lstSite.get(lstSite.size() - 1));
			outputMap.put("lstSiteHospital",
					getHospitalBasedonSite(lstSite.get(lstSite.size() - 1).getNsitecode(), userInfo));

		} else {
			outputMap.put("lstSite", null);
			outputMap.put("selectedSite", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getHospitalBasedonSite(final int ncollectionsitecode, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select h.nhospitalcode, h.shospitalname from hospital h, sitehospitalmapping shm "
				+ "where shm.nhospitalcode = h.nhospitalcode and shm.nmappingsitecode = " + ncollectionsitecode + " "
				+ "and h.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and shm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and h.nsitecode = " + userInfo.getNmastersitecode() + " and shm.nsitecode = "
				+ userInfo.getNmastersitecode();

		final List<Hospital> lstHospital = jdbcTemplate.query(strQuery, new Hospital());

		if (!lstHospital.isEmpty()) {
			outputMap.put("lstHospital", lstHospital);
			outputMap.put("selectedHospital", lstHospital.get(lstHospital.size() - 1));

		} else {
			outputMap.put("lstHospital", null);
			outputMap.put("selectedHospital", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getStorageStructureBasedonSite(final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		// ATE234 Janakumar StorageInstrumnet si to storage condition sc join in the
		// query for the sc.sstorageconditionname

		final String strQuery = "select ssv.jsondata, si.nsamplestoragelocationcode,si.nsamplestorageversioncode,si.nstorageinstrumentcode, "
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
				+ "AND st.spositionvalue :: text <> '' :: text) navailablespace,sc.sstorageconditionname as sstoragetemperature from "
				+ "storageinstrument si "
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
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and i.ninstrumentstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "join storagecondition sc on sc.nstorageconditioncode = si.nstorageconditioncode "
				+ "and sc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and sc.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ " join instrumentcalibration ic on ic.ninstrumentcode=i.ninstrumentcode and ic.nstatus="
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
				+ "i.sinstrumentid, i.ninstrumentcode,ssl.nsamplestoragelocationcode, ssm.nstorageinstrumentcode,sc.sstorageconditionname; ";

		final List<Map<String, Object>> lstStorageStructure = jdbcTemplate.queryForList(strQuery);

		if (!lstStorageStructure.isEmpty()) {
			outputMap.put("lstStorageStructure", lstStorageStructure);
			outputMap.put("selectedStorageStructure", lstStorageStructure.get(lstStorageStructure.size() - 1));

		} else {
			outputMap.put("lstStorageStructure", null);
			outputMap.put("selectedStorageStructure", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

//// For Production
//	@Override
//	public ResponseEntity<Object> validateSubjectID(final String ssubjectid, final UserInfo userInfo) throws Exception {
//		final Map<String, Object> outputMap = new LinkedHashMap<>();
//		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
//
//		final String strExternalUrlSettings = "select sexternalurl, jsondata " + "from externalurlsettings "
//				+ "where nexternalurlsettingcode = 4 " + "and nsitecode = " + userInfo.getNmastersitecode() + " "
//				+ "and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//
//		final ExternalUrlSettings objExternalUrlSettings = (ExternalUrlSettings) jdbcUtilityTemplateFunction
//				.queryForObject(strExternalUrlSettings, ExternalUrlSettings.class, jdbcTemplate);
//
//		if (objExternalUrlSettings == null) {
//			// URL settings row itself not found
//			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_URLSETTINGSUNAVAILABLE",
//					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
//		}
//
//		final String sExternalUrl = objExternalUrlSettings.getSexternalurl();
//		final Map<String, Object> jsonMap = objExternalUrlSettings.getJsondata();
//
//		// Basic validation of URL + JSON config
//		if (sExternalUrl == null || sExternalUrl.trim().isEmpty() || jsonMap == null || jsonMap.isEmpty()) {
//			return new ResponseEntity<>(
//					commonFunction.getMultilingualMessage("IDS_INVALIDURLSETTINGS", userInfo.getSlanguagefilename()),
//					HttpStatus.CONFLICT);
//		}
//
//		// Expecting: { "token": "fxt9..." }
//		String token = null;
//		Object tokenObj = jsonMap.get("token");
//		if (tokenObj != null) {
//			token = tokenObj.toString().trim();
//		}
//
//		if (token != null && !token.isEmpty()) {
//			// Store the full header value in JSON (e.g. "Bearer xxxxx") if needed
//			headers.set("Authorization", token);
//		}
//
//		Map<String, String> body = Collections.singletonMap("q", ssubjectid);
//		HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
//
//		String rawEndpoint = sExternalUrl;
//		String fullUrl = rawEndpoint.startsWith("http://") || rawEndpoint.startsWith("https://") ? rawEndpoint
//				: "https://" + rawEndpoint;
//		URI remoteUrl = new URI(fullUrl);
//
//		// Configure RestTemplate with timeouts
//		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
//		requestFactory.setConnectTimeout(5000); // 5s connect
//		requestFactory.setReadTimeout(10000); // 10s read
//		RestTemplate restTemplate = new RestTemplate(requestFactory);
//
//		ResponseEntity<Object> resp = restTemplate.postForEntity(remoteUrl, request, Object.class);
//
//		if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
//			@SuppressWarnings("unchecked")
//			Map<String, Object> responseBody = (Map<String, Object>) resp.getBody();
//
//			Map<String, Object> meta = responseBody.containsKey("meta") && responseBody.get("meta") instanceof Map
//					? (Map<String, Object>) responseBody.get("meta")
//					: null;
//
//			Integer metaCode = null;
//			if (meta != null && meta.get("code") != null) {
//				if (meta.get("code") instanceof Integer) {
//					metaCode = (Integer) meta.get("code");
//				} else {
//					try {
//						metaCode = Integer.parseInt(meta.get("code").toString());
//					} catch (NumberFormatException nfe) {
//						metaCode = null;
//					}
//				}
//			}
//
//			if (metaCode != null && metaCode == HttpStatus.OK.value() && responseBody.get("data") != null) {
//				outputMap.put("isValidSubjectID", true);
//				@SuppressWarnings("unchecked")
//				Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
//
//				String scaseType = data.getOrDefault("type", "").toString();
//				String sgender = data.getOrDefault("gender", "").toString();
//
//				boolean isThirdPartyShareable = !("false"
//						.equalsIgnoreCase(String.valueOf(data.get("is_third_party_shareable"))));
//				boolean isSampleAccessible = !("false"
//						.equalsIgnoreCase(String.valueOf(data.get("is_sample_accessible"))));
//
//				outputMap.put("scasetype", scaseType);
//				outputMap.put("sgendername", sgender);
//				outputMap.put("nisthirdpartysharable", isThirdPartyShareable ? 3 : 4);
//				outputMap.put("nissampleaccesable", isSampleAccessible ? 3 : 4);
//
//				String ssubjectidEsc = stringUtilityFunction.replaceQuote(ssubjectid);
//				String scasetypeEsc = stringUtilityFunction.replaceQuote(scaseType);
//				String sgenderEsc = stringUtilityFunction.replaceQuote(sgender);
//				int nisThird = (int) outputMap.get("nisthirdpartysharable");
//				int nisSample = (int) outputMap.get("nissampleaccesable");
//				String dmodified = "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'";
//
//				final String insertquery = "INSERT INTO public.biosubjectdetails ("
//						+ "ssubjectid, scasetype, sgendername, " + "nisthirdpartysharable, nissampleaccesable, "
//						+ "dmodifieddate, nsitecode, nstatus" + ") VALUES (" + "'" + ssubjectidEsc + "', " + "'"
//						+ scasetypeEsc + "', " + "'" + sgenderEsc + "', " + nisThird + ", " + nisSample + ", "
//						+ dmodified + ", " + userInfo.getNmastersitecode() + ", "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ ") ON CONFLICT (ssubjectid) DO UPDATE SET " + "scasetype = EXCLUDED.scasetype, "
//						+ "sgendername = EXCLUDED.sgendername, "
//						+ "nisthirdpartysharable = EXCLUDED.nisthirdpartysharable, "
//						+ "nissampleaccesable = EXCLUDED.nissampleaccesable, "
//						+ "dmodifieddate = EXCLUDED.dmodifieddate, " + "nsitecode = EXCLUDED.nsitecode, "
//						+ "nstatus = EXCLUDED.nstatus;";
//
//				jdbcTemplate.execute(insertquery);
//				return new ResponseEntity<>(outputMap, HttpStatus.OK);
//			} else {
//				return new ResponseEntity<>(
//						commonFunction.getMultilingualMessage("IDS_NOTVALIDSUBJECTID", userInfo.getSlanguagefilename()),
//						HttpStatus.CONFLICT);
//			}
//		} else {
//			return new ResponseEntity<>(
//					commonFunction.getMultilingualMessage("IDS_SERVICEUNAVAILABLE", userInfo.getSlanguagefilename()),
//					HttpStatus.CONFLICT);
//		}
//	}

//	For Development
	@Override
	public ResponseEntity<Object> validateSubjectID(final String ssubjectid, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();

		outputMap.put("isValidSubjectID", true);

		String scaseType = "dummy";
		String sgender = "dummy";

		// parse booleans robustly
		boolean isThirdPartyShareable = true;
		boolean isSampleAccessible = true;

		outputMap.put("scasetype", scaseType);
		outputMap.put("sgendername", sgender);
		outputMap.put("nisthirdpartysharable", isThirdPartyShareable ? 3 : 4);
		outputMap.put("nissampleaccesable", isSampleAccessible ? 3 : 4);

		// sanitize strings
		String ssubjectidEsc = stringUtilityFunction.replaceQuote(ssubjectid);
		String scasetypeEsc = stringUtilityFunction.replaceQuote(scaseType);
		String sgenderEsc = stringUtilityFunction.replaceQuote(sgender);
		int nisThird = (int) outputMap.get("nisthirdpartysharable");
		int nisSample = (int) outputMap.get("nissampleaccesable");
		String dmodified = "'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'";

		final String insertquery = "INSERT INTO public.biosubjectdetails ("
				+ "ssubjectid, scasetype, sgendername, nisthirdpartysharable, nissampleaccesable, dmodifieddate, nsitecode, nstatus"
				+ ") VALUES (" + "'" + ssubjectidEsc + "', " + "'" + scasetypeEsc + "', " + "'" + sgenderEsc + "', "
				+ nisThird + ", " + nisSample + ", " + dmodified + ", " + userInfo.getNmastersitecode() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ") ON CONFLICT (ssubjectid) DO UPDATE SET " + "scasetype = EXCLUDED.scasetype, "
				+ "sgendername = EXCLUDED.sgendername, " + "nisthirdpartysharable = EXCLUDED.nisthirdpartysharable, "
				+ "nissampleaccesable = EXCLUDED.nissampleaccesable, " + "dmodifieddate = EXCLUDED.dmodifieddate, "
				+ "nsitecode = EXCLUDED.nsitecode, " + "nstatus = EXCLUDED.nstatus;";

		jdbcTemplate.execute(insertquery);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> updateBioParentSampleReceiving(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		final short ncollectionsitecode = ((Number) inputMap.get("ncollectionsitecode")).shortValue();
		final int ncollectedhospitalcode = (int) inputMap.get("ncollectedhospitalcode");
		final int nstorageinstrumentcode = (int) inputMap.get("nstorageinstrumentcode");
		final int nbioparentsamplecode = (int) inputMap.get("nbioparentsamplecode");

		// Commented by Gowtham on nov 14 2025 for jira.id:BGSI-216
//		final int nissampleaccesable = (int) inputMap.get("nissampleaccesable");
//		
//		if (nissampleaccesable == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
//			return new ResponseEntity<>(
//					commonFunction.getMultilingualMessage("IDS_SAMPLEISNOTACCESSIBLE", userInfo.getSlanguagefilename()),
//					HttpStatus.EXPECTATION_FAILED);
//		}

		final BioParentSampleReceiving objBioParentSampleReceivingBeforEdit = getActiveBioParentSampleReceivingforAudit(
				nbioparentsamplecode, userInfo);
		listBeforeUpdate.add(objBioParentSampleReceivingBeforEdit);

		BioParentSampleReceiving objBioParentSampleReceiving = objMapper
				.convertValue(inputMap.get("parentsamplereceiving"), BioParentSampleReceiving.class);

		final String sarrivaldate = (objBioParentSampleReceiving.getSarrivaldate() != null
				&& !objBioParentSampleReceiving.getSarrivaldate().isEmpty()) ? "'"
						+ objBioParentSampleReceiving.getSarrivaldate().toString().replace("T", " ").replace("Z", "")
						+ "'" : null;

		final String updateQueryString = "update bioparentsamplereceiving set ncollectionsitecode = "
				+ ncollectionsitecode + ", ncollectedhospitalcode = " + ncollectedhospitalcode
				+ ", nstorageinstrumentcode  = " + nstorageinstrumentcode + ", darrivaldate = " + sarrivaldate
				+ ", ntzarrivaldate = " + userInfo.getNtimezonecode() + ", noffsetarrivaldate = "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
//												+", dtransactiondate = '"+dateUtilityFunction.getCurrentDateTime(userInfo)
//												+"', ntztransactiondate = "+userInfo.getNtimezonecode()
//												+", noffsettransactiondate = "+dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())									
				+ " where nbioparentsamplecode=" + nbioparentsamplecode + ";";

		jdbcTemplate.execute(updateQueryString);

		// ===== COC: START =====
//		if (nbioparentsamplecode > 0) {
//
//			String sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sQuery);
//
//			sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sQuery);
//
//			sQuery = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//			jdbcTemplate.execute(sQuery);
//
//			int chainCustodyPk = jdbcTemplate.queryForObject(
//					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//					Integer.class);
//
//			String strChainCustody = "insert into chaincustody ("
//					+ "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno, "
//					+ "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate, "
//					+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus) " + "select (" + chainCustodyPk
//					+ " + rank() over(order by bpr.nbioparentsamplecode)), " + userInfo.getNformcode() + ", "
//					+ " bpr.nbioparentsamplecode, 'nbioparentsamplecode', "
//					+ " 'bioparentsamplereceiving', bpr.sparentsamplecode, "
//					+ Enumeration.TransactionStatus.UPDATED.gettransactionstatus() + ", " + userInfo.getNusercode()
//					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
//					+ userInfo.getNtimezonecode() + ", "
//					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
//					+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//					+ " [' || bpr.sparentsamplecode || ']' || '  ' || " + "'"
//					+ commonFunction.getMultilingualMessage("IDS_COHORTNO", userInfo.getSlanguagefilename())
//					+ " [' || bpr.ncohortno || ']'" + "," + userInfo.getNtranssitecode() + ", "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " from bioparentsamplereceiving bpr " + " where bpr.nsitecode = " + userInfo.getNtranssitecode()
//					+ " and bpr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and bpr.nbioparentsamplecode = " + nbioparentsamplecode + ";";
//
//			String strSeqUpdateChain = " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
//					+ " + count(nbioparentsamplecode) from bioparentsamplereceiving " + " where nbioparentsamplecode = "
//					+ nbioparentsamplecode + " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ ") where stablename = 'chaincustody' and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//			jdbcTemplate.execute(strChainCustody);
//			jdbcTemplate.execute(strSeqUpdateChain);
//
//		}
		// ===== End COC =====

		final List<String> multilingualIDList = new ArrayList<>();
		objBioParentSampleReceiving = getActiveBioParentSampleReceivingforAudit(nbioparentsamplecode, userInfo);

		listAfterUpdate.add(objBioParentSampleReceiving);

		multilingualIDList.add("IDS_EDITPARENTSAMPLERECEIVING");

		auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo);

		// status code:200
		return getActiveBioParentSampleReceiving(nbioparentsamplecode, userInfo);

	}

	@Override
	public ResponseEntity<Object> createBioParentSampleReceiving(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final boolean isValidSubjectID = (boolean) inputMap.get("isValidSubjectID");
		if (isValidSubjectID) {
			final ObjectMapper objMapper = new ObjectMapper();
			String sQuery = " lock  table lockbioparentreceiving " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
			jdbcTemplate.execute(sQuery);
			final String ssubjectid = (String) inputMap.get("ssubjectid");
			final int nbioprojectcode = (int) inputMap.get("nbioprojectcode");

			// Added by Gowtham on nov 14 2025 for jira.id:BGSI-216
			final int nissampleaccesable = (int) inputMap.get("nissampleaccesable");
			if (nissampleaccesable == Enumeration.TransactionStatus.NO.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLEISNOTACCESSIBLE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			short ncohortno = 1;
			String sparentsamplecode = checkSubjectIdAndGetParentSampleCode(ssubjectid, nbioprojectcode);
			if (sparentsamplecode == null) {
				sQuery = "select sprojectcode from bioproject where nbioprojectcode = " + nbioprojectcode
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final String sprojectcode = jdbcTemplate.queryForObject(sQuery, String.class);
				sQuery = "select nsequenceno from bioparentseqno where nbioprojectcode = " + nbioprojectcode
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int nseqno = jdbcTemplate.queryForObject(sQuery, Integer.class);
				nseqno = nseqno + 1;
				final String paddedSeq = String.format("%05d", nseqno);
				sparentsamplecode = sprojectcode + paddedSeq;

				final String updateQry = " update  bioparentseqno set nsequenceno = " + nseqno
						+ " where nbioprojectcode = " + nbioprojectcode + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				jdbcTemplate.execute(updateQry);
			} else {
				sQuery = "SELECT MAX(ncohortno)+1 FROM bioparentsamplereceiving WHERE sparentsamplecode = N'"
						+ stringUtilityFunction.replaceQuote(sparentsamplecode) + "' and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				ncohortno = jdbcTemplate.queryForObject(sQuery, Short.class);
			}

			BioParentSampleReceiving objBioParentSampleReceiving = objMapper
					.convertValue(inputMap.get("parentsamplereceiving"), BioParentSampleReceiving.class);

			final String sarrivaldate = (objBioParentSampleReceiving.getSarrivaldate() != null
					&& !objBioParentSampleReceiving.getSarrivaldate().isEmpty())
							? "'" + objBioParentSampleReceiving.getSarrivaldate().toString().replace("T", " ")
									.replace("Z", "") + "'"
							: null;

			final String scasetype = (String) inputMap.get("scasetype");
			final int ndiseasecode = (int) inputMap.get("ndiseasecode");
			final short ncollectionsitecode = ((Number) inputMap.get("ncollectionsitecode")).shortValue();
			final int ncollectedhospitalcode = (int) inputMap.get("ncollectedhospitalcode");
			final int nstorageinstrumentcode = (int) inputMap.get("nstorageinstrumentcode");

			sQuery = "SELECT nsequenceno+1 FROM seqnobiobankmanagement WHERE stablename = 'bioparentsamplereceiving' "
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final int nbioparentsamplecode = jdbcTemplate.queryForObject(sQuery, Integer.class);

			final String insertquery = "INSERT INTO public.bioparentsamplereceiving("
					+ "	nbioparentsamplecode, sparentsamplecode, ssubjectid, scasetype, ndiseasecode, nbioprojectcode, ncohortno, ncollectionsitecode, ncollectedhospitalcode, nstorageinstrumentcode ,"
					+ " darrivaldate, ntzarrivaldate, noffsetarrivaldate, dtransactiondate, ntztransactiondate, noffsettransactiondate, nusercode, nuserrolecode, nsitecode, nstatus)"
					+ "	VALUES (" + nbioparentsamplecode + ", N'"
					+ stringUtilityFunction.replaceQuote(sparentsamplecode) + "', N'"
					+ stringUtilityFunction.replaceQuote(ssubjectid) + "', N'"
					+ stringUtilityFunction.replaceQuote(scasetype) + "', " + ndiseasecode + ", " + nbioprojectcode
					+ ", " + ncohortno + ", " + ncollectionsitecode + ", " + ncollectedhospitalcode + ", "
					+ nstorageinstrumentcode + ", " + sarrivaldate + ", " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNtranssitecode()
					+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			jdbcTemplate.execute(insertquery);

			// ===== COC: START =====
//			if (nbioparentsamplecode > 0) {
//				String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery1);
//
//				sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery);
//
//				sQuery = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//				jdbcTemplate.execute(sQuery);
//
//				int chainCustodyPk = jdbcTemplate.queryForObject(
//						"select max(nsequenceno) from seqnoregistration where stablename='chaincustody' and nstatus="
//								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//						Integer.class);
//
//				String strChainCustody = "insert into chaincustody ("
//						+ "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
//						+ "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
//						+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + (chainCustodyPk)
//						+ " + rank() over(order by bpsr.nbioparentsamplecode), " + userInfo.getNformcode() + ", "
//						+ " bpsr.nbioparentsamplecode, 'nbioparentsamplecode', "
//						+ " 'bioparentsamplereceiving', bpsr.sparentsamplecode, "
//						+ Enumeration.TransactionStatus.INSERTED.gettransactionstatus() + ", " + userInfo.getNusercode()
//						+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
//						+ "', " + userInfo.getNtimezonecode() + ", "
//						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " '"
//						+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//						+ " [' || bpsr.sparentsamplecode || ']' || '  ' || " + "'"
//						+ commonFunction.getMultilingualMessage("IDS_COHORTNO", userInfo.getSlanguagefilename())
//						+ " [' || bpsr.ncohortno || ']'" + ", " + userInfo.getNtranssitecode() + ", "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " from bioparentsamplereceiving bpsr" + " where bpsr.nsitecode = "
//						+ userInfo.getNtranssitecode() + " and bpsr.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and bpsr.nbioparentsamplecode in (" + nbioparentsamplecode + ");";
//
//				String strSeqUpdateChain = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//						+ " + count(nbioparentsamplecode)" + " from bioparentsamplereceiving"
//						+ " where nbioparentsamplecode in (" + nbioparentsamplecode + ")" + " and nsitecode = "
//						+ userInfo.getNtranssitecode() + " and nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ ") where stablename = 'chaincustody' and nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//				jdbcTemplate.execute(strChainCustody);
//				jdbcTemplate.execute(strSeqUpdateChain);
//
//			}
			// ===== End COC =====

			final String updateQry = " update  seqnobiobankmanagement set nsequenceno = " + nbioparentsamplecode
					+ " where stablename = 'bioparentsamplereceiving'" + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(updateQry);

			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> savedList = new ArrayList<>();
			objBioParentSampleReceiving = getActiveBioParentSampleReceivingforAudit(nbioparentsamplecode, userInfo);

			savedList.add(objBioParentSampleReceiving);

			multilingualIDList.add("IDS_ADDPARENTSAMPLERECEIVING");

			auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, multilingualIDList, userInfo);

			// Added By Mullai Balaji V for email JIRA ID:BGSI:147

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
				mailMap.put("nbioparentsamplecode", nbioparentsamplecode);
				String query = "SELECT CONCAT(sparentsamplecode, ' | ', ncohortno) FROM bioparentsamplereceiving where nbioparentsamplecode="
						+ nbioparentsamplecode + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode();
				String referenceId = jdbcTemplate.queryForObject(query, String.class);

				mailMap.put("ssystemid", referenceId);
				final UserInfo mailUserInfo = new UserInfo(userInfo);
				mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
				mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
				emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);

			}

			return getBioParentSampleReceiving(inputMap, userInfo);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.NOTVALIDSUBJECTID.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	public BioParentSampleReceiving getActiveBioParentSampleReceivingforAudit(final int nbioparentsamplecode,
			final UserInfo userInfo) throws Exception {

		// modified by sujatha ATE_274 by adding few more missing fields for audit trail
		// while adding parent sample receiving bgsi-249
		final String strQuery = "select bspr.nbioparentsamplecode, bsd.nisthirdpartysharable, bsd.nissampleaccesable,"
				+ "bspr.sparentsamplecode, bspr.ssubjectid, bspr.scasetype, bspr.ndiseasecode, d.sdiseasename, bspr.nbioprojectcode, bp.sprojecttitle, bp.sprojectcode, "
				+ "u.sfirstname||' '|| u.slastname as sprojectincharge, bspr.ncohortno, bspr.ncollectionsitecode, cs.ssitename as scollectionsitename, "
				+ "bspr.ncollectedhospitalcode, ch.shospitalname, bspr.nstorageinstrumentcode, i.sinstrumentid, sc.sstorageconditionname as sstoragetemperature, "
				+ " COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts1.jsondata->'stransdisplaystatus'->>'en-US') AS sisthirdpartysharable, "
				+ " COALESCE(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts2.jsondata->'stransdisplaystatus'->>'en-US') AS sissampleaccesable, "
				+ "bspr.darrivaldate, bspr.ntzarrivaldate, bspr.noffsetarrivaldate, tz1.stimezoneid AS stzarrivaldate, "
				+ "COALESCE(TO_CHAR(bspr.darrivaldate,'" + userInfo.getSsitedate() + "'), '') AS sarrivaldate, "
				+ "bspr.dtransactiondate, bspr.ntztransactiondate, bspr.noffsetarrivaldate, tz2.stimezoneid AS stztransactiondate, "
				+ "COALESCE(TO_CHAR(bspr.dtransactiondate, '" + userInfo.getSsitedate() + "'), '') AS stransactiondate "
				+ "from bioparentsamplereceiving bspr, disease d, bioproject bp, users u, site cs, hospital ch, storageinstrument ssl, storagecondition sc, instrument i, timezone tz1, "
				+ "timezone tz2, biosubjectdetails bsd, transactionstatus ts1,transactionstatus ts2 "
				+ "where bspr.nbioparentsamplecode = " + nbioparentsamplecode + " "
				+ "and bspr.ndiseasecode = d.ndiseasecode and bspr.ssubjectid = bsd.ssubjectid and bspr.nbioprojectcode = bp.nbioprojectcode and bp.nusercode = u.nusercode "
				+ " and bsd.nisthirdpartysharable = ts1.ntranscode and bsd.nissampleaccesable = ts2.ntranscode "
				+ "and bspr.ncollectionsitecode = cs.nsitecode and bspr.ncollectedhospitalcode = ch.nhospitalcode "
				+ "and ssl.nstorageinstrumentcode = bspr.nstorageinstrumentcode and ssl.nstorageconditioncode = sc.nstorageconditioncode and ssl.ninstrumentcode = i.ninstrumentcode and bspr.ntzarrivaldate = tz1.ntimezonecode "
				+ "and bspr.ntztransactiondate = tz2.ntimezonecode " + "and bspr.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + "and d.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ "and bp.nsitecode= " + userInfo.getNmastersitecode() + " " + "and u.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + "and ch.nsitecode= " + userInfo.getNmastersitecode() + " "
				+ "and sc.nsitecode= " + userInfo.getNmastersitecode() + " " + "and ssl.nregionalsitecode in ( "
				+ userInfo.getNtranssitecode() + ", -1) " + "and bspr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and bp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and cs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ch.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and i.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and tz1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and tz2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by bspr.nbioparentsamplecode";

		final BioParentSampleReceiving objBioParentSampleReceiving = (BioParentSampleReceiving) jdbcUtilityTemplateFunction
				.queryForObject(strQuery, BioParentSampleReceiving.class, jdbcTemplate);

		return objBioParentSampleReceiving;

	}

	public String checkSubjectIdAndGetParentSampleCode(final String ssubjectid, final int nbioprojectcode)
			throws Exception {

		String sparentsamplecode = null;

		final String strQuery = "SELECT sparentsamplecode FROM bioparentsamplereceiving " + "WHERE ssubjectid = N'"
				+ stringUtilityFunction.replaceQuote(ssubjectid) + "' AND nbioprojectcode = " + nbioprojectcode
				+ " AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by sparentsamplecode";

		final BioParentSampleReceiving result = (BioParentSampleReceiving) jdbcUtilityTemplateFunction
				.queryForObject(strQuery, BioParentSampleReceiving.class, jdbcTemplate);

		if (result != null) {
			sparentsamplecode = result.getSparentsamplecode();
		}

		return sparentsamplecode;
	}

	@Override
	public ResponseEntity<Object> getActiveBioParentSampleCollection(final int nbioparentsamplecollectioncode,
			final UserInfo userInfo) throws Exception {

		final String result = validateParentSampleCollection(nbioparentsamplecollectioncode, userInfo);
		if (!result.equals(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus())) {
			if (result.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
				final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
				final String strQuery = "select bpsc.nbioparentsamplecollectioncode, bpsc.nbioparentsamplecode, bpsr.sparentsamplecode, bpsr.ncohortno, u.sfirstname||' '|| u.slastname as sprojectincharge, bp.sprojecttitle, bp.sprojectcode, "
						+ "cs.ssitename as scollectionsitename, pc.sproductcatname, bpsc.nproductcatcode,  bpsc.nnoofsamples,  sc.nstorageconditioncode, sc.sstorageconditionname, "
						+ "bpsc.scollectorname, bpsc.stemporarystoragename, bpsc.ssendername, bpsc.sinformation, bpsc.nrecipientusercode, "
						+ "u1.sfirstname||' '|| u1.slastname as srecipientusername, bpsc.jsonuidata, bpsc.ntransactionstatus, "
						+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
						+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus, "
						+ "bpsc.dsamplecollectiondate, bpsc.ntzsamplecollectiondate, bpsc.noffsetsamplecollectiondate, tz1.stimezoneid AS stzsamplecollectiondate, "
						+ "COALESCE(TO_CHAR(bpsc.dsamplecollectiondate,'" + userInfo.getSpgsitedatetime()
						+ "'), '') AS ssamplecollectiondate, "
						+ "bpsc.dbiobankarrivaldate, bpsc.ntzbiobankarrivaldate, bpsc.noffsetbiobankarrivaldate, tz1.stimezoneid AS stzbiobankarrivaldate, "
						+ "COALESCE(TO_CHAR(bpsc.dbiobankarrivaldate,'" + userInfo.getSpgsitedatetime()
						+ "'), '') AS sbiobankarrivaldate, "
						+ "bpsc.dtemporarystoragedate, bpsc.ntztemporarystoragedate, bpsc.noffsettemporarystoragedate, tz1.stimezoneid AS stztemporarystoragedate, "
						+ "COALESCE(TO_CHAR(bpsc.dtemporarystoragedate,'" + userInfo.getSpgsitedatetime()
						+ "'), '') AS stemporarystoragedate, "
						+ "bpsc.dtransactiondate, bpsc.ntztransactiondate, bpsc.noffsettransactiondate, tz1.stimezoneid AS stztransactiondate, "
						+ "COALESCE(TO_CHAR(bpsc.dtransactiondate,'" + userInfo.getSpgsitedatetime()
						+ "'), '') AS stransactiondate "
						+ "from bioparentsamplecollection bpsc, bioparentsamplereceiving bpsr, bioproject bp,storagecondition sc, users u, site cs, site rs, productcategory pc, "
						+ "users u1, transactionstatus ts, timezone tz1, timezone tz2, timezone tz3, timezone tz4 "
						+ "where bpsc.nbioparentsamplecode = bpsr.nbioparentsamplecode and sc.nstorageconditioncode = bpsc.nstorageconditioncode "
						+ "and bpsc.nbioparentsamplecollectioncode = " + nbioparentsamplecollectioncode + " "
						+ "and bpsc.nsitecode = " + userInfo.getNtranssitecode() + " and sc.nsitecode = "
						+ userInfo.getNmastersitecode() + " and bpsr.nsitecode = " + userInfo.getNtranssitecode() + " "
						+ "and bpsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and sc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and bpsr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and bpsr.nbioprojectcode = bp.nbioprojectcode " + " and bp.nsitecode = "
						+ userInfo.getNmastersitecode() + " " + "and bp.nusercode = u.nusercode "
						+ " and u.nsitecode = " + userInfo.getNmastersitecode() + " " + "and u.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and bpsr.ncollectionsitecode = cs.nsitecode and cs.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and bpsc.nreceivingsitecode = rs.nsitecode and rs.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and bpsc.nproductcatcode =  pc.nproductcatcode and pc.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and bpsc.nrecipientusercode = u1.nusercode and u1.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and bpsc.ntransactionstatus = ts.ntranscode "
						+ "and bpsc.ntzsamplecollectiondate = tz1.ntimezonecode and tz1.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and bpsc.ntzbiobankarrivaldate = tz2.ntimezonecode and tz2.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and bpsc.ntztemporarystoragedate = tz3.ntimezonecode and tz3.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "and bpsc.ntztransactiondate = tz4.ntimezonecode and tz4.nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " order by bpsc.nbioparentsamplecollectioncode desc";
				final BioParentSampleCollection objBioParentSampleCollection = (BioParentSampleCollection) jdbcUtilityTemplateFunction
						.queryForObject(strQuery, BioParentSampleCollection.class, jdbcTemplate);

				if (objBioParentSampleCollection != null) {
					outputMap.putAll(getUserAndProductCatDetails(
							objBioParentSampleCollection.getNbioparentsamplecollectioncode(),
							objBioParentSampleCollection.getNbioparentsamplecode(), userInfo));
					outputMap.put("selectedBioParentSampleCollection", objBioParentSampleCollection);
				} else {
					outputMap.put("sampleTypeList", null);
					outputMap.put("recipientsList", null);
					outputMap.put("selectedBioParentSampleCollection", null);
				}

				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						"IDS_SELECTNOTYETPROCESSEDSTATUSRECORD", userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> createParentSampleCollection(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table lockbioparentcollection "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		int seqNoSampleCollection = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnobiobankmanagement where stablename='bioparentsamplecollection' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);
		int seqNoSampleCollectionHistory = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnobiobankmanagement where stablename='bioparentsamplecollectionhistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);
		seqNoSampleCollection = seqNoSampleCollection + 1;
		seqNoSampleCollectionHistory = seqNoSampleCollectionHistory + 1;

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final ObjectMapper objmapper = new ObjectMapper();
		BioParentSampleCollection objParentSampleCollection = objmapper
				.convertValue(inputMap.get("parentsamplecollection"), BioParentSampleCollection.class);

		// Added by Gowtham on nov 14 2025 for jira.id:BGSI-216
		final Map<String, Object> map = (Map<String, Object>) getActiveBioParentSampleReceiving(
				objParentSampleCollection.getNbioparentsamplecode(), userInfo).getBody();

		final BioParentSampleReceiving bioParentSampleReceiving = (BioParentSampleReceiving) map
				.get("selectedBioParentSampleReceiving");

		if (bioParentSampleReceiving.getNissampleaccesable() != Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SAMPLEISNOTACCESSIBLE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

//		String bioSampleSeqNo = createUpdateSampleSeqNo(objParentSampleCollection.getNbioprojectcode(), userInfo);

		final String ssamplecollectiondate = (objParentSampleCollection.getSsamplecollectiondate() != null
				&& !objParentSampleCollection.getSsamplecollectiondate().isEmpty())
						? "'" + objParentSampleCollection.getSsamplecollectiondate().toString().replace("T", " ")
								.replace("Z", "") + "'"
						: null;
		final String sbiobankarrivaldate = (objParentSampleCollection.getSbiobankarrivaldate() != null
				&& !objParentSampleCollection.getSbiobankarrivaldate().isEmpty())
						? "'" + objParentSampleCollection.getSbiobankarrivaldate().toString().replace("T", " ")
								.replace("Z", "") + "'"
						: null;
		final String stemporarystoragedate = (objParentSampleCollection.getStemporarystoragedate() != null
				&& !objParentSampleCollection.getStemporarystoragedate().isEmpty())
						? "'" + objParentSampleCollection.getStemporarystoragedate().toString().replace("T", " ")
								.replace("Z", "") + "'"
						: null;
		final JSONObject jsonObject = new JSONObject(objParentSampleCollection.getJsonuidata());

		String strInsert = "insert into bioparentsamplecollection (nbioparentsamplecollectioncode, nbioparentsamplecode, "
				+ "nreceivingsitecode, nproductcatcode, nnoofsamples, nstorageconditioncode, scollectorname, stemporarystoragename, "
				+ "ssendername, nrecipientusercode, sinformation, jsonuidata, dsamplecollectiondate, ntzsamplecollectiondate, "
				+ "noffsetsamplecollectiondate, dbiobankarrivaldate, ntzbiobankarrivaldate, noffsetbiobankarrivaldate, "
				+ "dtemporarystoragedate, ntztemporarystoragedate, noffsettemporarystoragedate, dtransactiondate, "
				+ "ntztransactiondate, noffsettransactiondate, ntransactionstatus, nusercode, nuserrolecode, "
//				+ "scollectionseqno, "
				+ "nsitecode, " + "nstatus) values (" + seqNoSampleCollection + ", "
				+ objParentSampleCollection.getNbioparentsamplecode() + ", "
				+ objParentSampleCollection.getNreceivingsitecode() + ", "
				+ objParentSampleCollection.getNproductcatcode() + ", " + objParentSampleCollection.getNnoofsamples()
				+ ", " + objParentSampleCollection.getNstorageconditioncode() + ", '"
				+ stringUtilityFunction.replaceQuote(objParentSampleCollection.getScollectorname()) + "', '"
				+ stringUtilityFunction.replaceQuote(objParentSampleCollection.getStemporarystoragename()) + "', '"
				+ stringUtilityFunction.replaceQuote(objParentSampleCollection.getSsendername()) + "', "
				+ objParentSampleCollection.getNrecipientusercode() + ", '"
				+ stringUtilityFunction.replaceQuote(objParentSampleCollection.getSinformation()) + "', '" + jsonObject
				+ "', " + ssamplecollectiondate + ", " + userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + sbiobankarrivaldate
				+ ", " + userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + stemporarystoragedate
				+ ", " + userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				+ Enumeration.TransactionStatus.UNPROCESSED.gettransactionstatus() + ", " + userInfo.getNusercode()
				+ ", " + userInfo.getNuserrole() + ", "
//				+ ", '" + bioSampleSeqNo + "', " 
				+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ");";
		strInsert += "insert into bioparentsamplecollectionhistory (nbioparentsamplecolhistorycode,"
				+ " nbioparentsamplecollectioncode, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode,"
				+ " ntransactionstatus, scomments, dtransactiondate, noffsetdtransactiondate, ntransdatetimezonecode,"
				+ " nsitecode, nstatus) values (" + seqNoSampleCollectionHistory + ", " + seqNoSampleCollection + ", "
				+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", "
				+ userInfo.getNdeputyuserrole() + ", "
				+ Enumeration.TransactionStatus.UNPROCESSED.gettransactionstatus() + ", null, '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
				+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
				+ userInfo.getNtimezonecode() + ", " + userInfo.getNtranssitecode() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
		strInsert += "update seqnobiobankmanagement set nsequenceno=" + seqNoSampleCollection
				+ " where stablename='bioparentsamplecollection';";
		strInsert += "update seqnobiobankmanagement set nsequenceno=" + seqNoSampleCollectionHistory
				+ " where stablename='bioparentsamplecollectionhistory';";
		jdbcTemplate.execute(strInsert);

		// ===== COC: START =====
		if (seqNoSampleCollection > 0) {
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

			final String strChainCustody = "insert into chaincustody ("
			        + "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno, "
			        + "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate, "
			        + "  noffsetdtransactiondate, sremarks, nsitecode, nstatus) "
			        + "select (" + chainCustodyPk
			        + " + rank() over(order by bpsc.nbioparentsamplecollectioncode)), "
			        + userInfo.getNformcode() + ", "
			        + "bpsc.nbioparentsamplecollectioncode, "
			        + "'nbioparentsamplecollectioncode', "
			        + "'bioparentsamplecollection', "
			        + "(bpr.sparentsamplecode || ' | ' || bpr.ncohortno), "
			        + Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ", "
			        + userInfo.getNusercode() + ", "
			        + userInfo.getNuserrole() + ", '"
			        + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
			        + userInfo.getNtimezonecode() + ", "
			        + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
			        + stringUtilityFunction.replaceQuote(
			                commonFunction.getMultilingualMessage("IDS_BIOSAMPLETYPE", userInfo.getSlanguagefilename()))
			        + " [' || pc.sproductcatname || '] ' || '"
			        + stringUtilityFunction.replaceQuote(
			                commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename()))
			        + " [' || bpr.sparentsamplecode || ']' || ' ' || '"
			        + stringUtilityFunction.replaceQuote(
			                commonFunction.getMultilingualMessage("IDS_COHORTNO", userInfo.getSlanguagefilename()))
			        + " [' || bpr.ncohortno || ']' || ' ' || '"
			        + stringUtilityFunction.replaceQuote(
			                commonFunction.getMultilingualMessage("IDS_RECEIVINGSITE", userInfo.getSlanguagefilename()))
			        + " [' || st.ssitename || ']' || ' ' || '"
			        + stringUtilityFunction.replaceQuote(
			                commonFunction.getMultilingualMessage("IDS_COLLECTIONSITE", userInfo.getSlanguagefilename()))
			        + " [' || cs.ssitename || ']' || ' ' || '"
			        + stringUtilityFunction.replaceQuote(
			                commonFunction.getMultilingualMessage("IDS_HOSPITAL", userInfo.getSlanguagefilename()))
			        + " [' || ch.shospitalname || ']' || ' ' || '"
			        + stringUtilityFunction.replaceQuote(
			                commonFunction.getMultilingualMessage("IDS_ARRIVALDATE", userInfo.getSlanguagefilename()))
			        + " [' || bpsc.dbiobankarrivaldate || ']"
			        + "', "
			        + userInfo.getNtranssitecode() + ", "
			        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
			        + " from bioparentsamplecollection bpsc "
			        + "join bioparentsamplereceiving bpr on bpr.nbioparentsamplecode = bpsc.nbioparentsamplecode "
			        + "left join productcategory pc on pc.nproductcatcode = bpsc.nproductcatcode "
			        + "left join site st on st.nsitecode = bpsc.nsitecode "
			        + "left join site cs on cs.nsitecode = bpsc.nreceivingsitecode "
			        + "left join hospital ch on ch.nhospitalcode = bpr.ncollectedhospitalcode "
			        + "where bpsc.nsitecode = " + userInfo.getNtranssitecode()
			        + " and bpsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			        + " and bpsc.nbioparentsamplecollectioncode in (" + seqNoSampleCollection + ");";


			final String strSeqUpdateChain = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
					+ " + count(nbioparentsamplecollectioncode) from bioparentsamplecollection"
					+ " where nbioparentsamplecollectioncode in (" + seqNoSampleCollection + ")" + " and nsitecode = "
					+ userInfo.getNtranssitecode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ") where stablename = 'chaincustody' and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strChainCustody+strSeqUpdateChain);
		}
		// ===== End COC =====

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedList = new ArrayList<>();
		objParentSampleCollection = getActiveBioParentSampleCollectionforAudit(seqNoSampleCollection, userInfo);

		savedList.add(objParentSampleCollection);

		multilingualIDList.add("IDS_ADDPARENTSAMPLECOLLECTION");

		auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, multilingualIDList, userInfo);

		// Added By Mullai Balaji V for email JIRA ID:BGSI:147

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
			mailMap.put("nbioparentsamplecollectioncode", seqNoSampleCollection);
			String query = "SELECT CONCAT(bpsr.sparentsamplecode, ' | ', bpsr.ncohortno) FROM bioparentsamplecollection bpsc"
					+ " Join bioparentsamplereceiving bpsr ON bpsr.nbioparentsamplecode=bpsc.nbioparentsamplecode  where bpsc.nbioparentsamplecollectioncode="
					+ seqNoSampleCollection + " and bpsr.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bpsc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bpsr.nsitecode="
					+ userInfo.getNtranssitecode() + " and bpsc.nsitecode=" + userInfo.getNtranssitecode();
			String referenceId = jdbcTemplate.queryForObject(query, String.class);
			mailMap.put("ssystemid", referenceId);
			final UserInfo mailUserInfo = new UserInfo(userInfo);
			mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
			mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
			emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
		}

		final List<BioParentSampleCollection> lstBioParentSampleCollection = getBioParentSampleCollection(
				objParentSampleCollection.getNbioparentsamplecode(), userInfo);
		outputMap.put("lstBioParentSampleCollection", lstBioParentSampleCollection);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

//	public String createUpdateSampleSeqNo(final int nbioprojectcode, final UserInfo userInfo) throws Exception {
//		String strCheckSeqNo = "select * from biosampleseqno where nbioprojectcode=" + nbioprojectcode
//				+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//		BioSampleSeqNo objBioSampleSeqNo = (BioSampleSeqNo) jdbcUtilityTemplateFunction.queryForObject(strCheckSeqNo,
//				BioSampleSeqNo.class, jdbcTemplate);
//
//		String strSampleSeqNo = "";
//		String strInsertBioSampleSeqNo = "";
//		if (objBioSampleSeqNo == null) {
//			strSampleSeqNo = "AAAA";
//			strInsertBioSampleSeqNo = "insert into biosampleseqno (nbioprojectcode, ssequenceno, nsitecode, nstatus)"
//					+ " values (" + nbioprojectcode + ", '" + strSampleSeqNo + "', " + userInfo.getNtranssitecode()
//					+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
//		} else {
//			strSampleSeqNo = AlphaSequenceGenerator(objBioSampleSeqNo.getSsequenceno(), userInfo);
//			strInsertBioSampleSeqNo = "update biosampleseqno set ssequenceno='" + strSampleSeqNo
//					+ "' where nbioprojectcode=" + nbioprojectcode + " and nsitecode=" + userInfo.getNtranssitecode()
//					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
//		}
//		jdbcTemplate.execute(strInsertBioSampleSeqNo);
//
//		return strSampleSeqNo;
//	}

//	public String AlphaSequenceGenerator(final String initialSeqNo, final UserInfo userInfo) throws Exception {
//
//		String seq = initialSeqNo;
//		char[] chars = seq.toCharArray();
//		int index = chars.length - 1;
//
//		while (index >= 0) {
//			if (chars[index] < 'Z') {
//				chars[index]++;
//				break;
//			} else {
//				chars[index] = 'A';
//				index--;
//			}
//		}
//
//		// If all are 'Z', it will reset to 'AAAA' (or handle overflow if needed)
//		seq = new String(chars);
//		return seq;
//	}

	public BioParentSampleCollection getActiveBioParentSampleCollectionforAudit(
			final int nbioparentsamplecollectioncode, final UserInfo userInfo) throws Exception {

		final String strQuery = "select bpsc.nbioparentsamplecollectioncode, bpsc.nbioparentsamplecode, bpsr.sparentsamplecode, bpsr.ncohortno, u.sfirstname||' '|| u.slastname as sprojectincharge, bp.sprojecttitle, bp.sprojectcode, "
				+ "cs.ssitename as scollectionsitename, pc.sproductcatname, bpsc.nproductcatcode,  bpsc.nnoofsamples,  sc.nstorageconditioncode, sc.sstorageconditionname stemperature, "
				+ "bpsc.scollectorname, bpsc.stemporarystoragename, bpsc.ssendername, bpsc.sinformation, bpsc.nrecipientusercode, "
				+ "u1.sfirstname||' '|| u1.slastname as srecipientusername, bpsc.jsonuidata, bpsc.ntransactionstatus, "
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransactionstatus, "
				+ "bpsc.dsamplecollectiondate, bpsc.ntzsamplecollectiondate, bpsc.noffsetsamplecollectiondate, tz1.stimezoneid AS stzsamplecollectiondate, "
				+ "COALESCE(TO_CHAR(bpsc.dsamplecollectiondate,'" + userInfo.getSpgsitedatetime()
				+ "'), '') AS ssamplecollectiondate, "
				+ "bpsc.dbiobankarrivaldate, bpsc.ntzbiobankarrivaldate, bpsc.noffsetbiobankarrivaldate, tz1.stimezoneid AS stzbiobankarrivaldate, "
				+ "COALESCE(TO_CHAR(bpsc.dbiobankarrivaldate,'" + userInfo.getSpgsitedatetime()
				+ "'), '') AS sbiobankarrivaldate, "
				+ "bpsc.dtemporarystoragedate, bpsc.ntztemporarystoragedate, bpsc.noffsettemporarystoragedate, tz1.stimezoneid AS stztemporarystoragedate, "
				+ "COALESCE(TO_CHAR(bpsc.dtemporarystoragedate,'" + userInfo.getSpgsitedatetime()
				+ "'), '') AS stemporarystoragedate, "
				+ "bpsc.dtransactiondate, bpsc.ntztransactiondate, bpsc.noffsettransactiondate, tz1.stimezoneid AS stztransactiondate, "
				+ "COALESCE(TO_CHAR(bpsc.dtransactiondate,'" + userInfo.getSpgsitedatetime()
				+ "'), '') AS stransactiondate "
				+ "from bioparentsamplecollection bpsc, bioparentsamplereceiving bpsr, bioproject bp,storagecondition sc, users u, site cs, site rs, productcategory pc, "
				+ "users u1, transactionstatus ts, timezone tz1, timezone tz2, timezone tz3, timezone tz4 "
				+ "where bpsc.nbioparentsamplecode = bpsr.nbioparentsamplecode and sc.nstorageconditioncode = bpsc.nstorageconditioncode "
				+ "and bpsc.nbioparentsamplecollectioncode = " + nbioparentsamplecollectioncode + " "
				+ "and bpsc.nsitecode = " + userInfo.getNtranssitecode() + " " + "and bpsr.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + "and bpsc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and bpsr.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and sc.nsitecode = "
				+ userInfo.getNmastersitecode() + " and bpsr.nbioprojectcode = bp.nbioprojectcode "
				+ "and bp.nsitecode = " + userInfo.getNmastersitecode() + " " + "and bp.nusercode = u.nusercode "
				+ "and u.nsitecode = " + userInfo.getNmastersitecode() + " " + "and u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsr.ncollectionsitecode = cs.nsitecode and cs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsc.nreceivingsitecode = rs.nsitecode and rs.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsc.nproductcatcode =  pc.nproductcatcode and pc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsc.nrecipientusercode = u1.nusercode and u1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsc.ntransactionstatus = ts.ntranscode "
				+ "and bpsc.ntzsamplecollectiondate = tz1.ntimezonecode and tz1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsc.ntzbiobankarrivaldate = tz2.ntimezonecode and tz2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsc.ntztemporarystoragedate = tz3.ntimezonecode and tz3.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and bpsc.ntztransactiondate = tz4.ntimezonecode and tz4.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by bpsc.nbioparentsamplecollectioncode desc";
		final BioParentSampleCollection objBioParentSampleCollection = (BioParentSampleCollection) jdbcUtilityTemplateFunction
				.queryForObject(strQuery, BioParentSampleCollection.class, jdbcTemplate);

		return objBioParentSampleCollection;

	}

	@Override
	public ResponseEntity<Object> updateParentSampleCollection(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		final BioParentSampleCollection objParentSampleCollection = objmapper
				.convertValue(inputMap.get("parentsamplecollection"), BioParentSampleCollection.class);

		final String result = validateParentSampleCollection(
				objParentSampleCollection.getNbioparentsamplecollectioncode(), userInfo);
		if (!result.equals(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus())) {

			// Commented by Gowtham on nov 14 2025 for jira.id:BGSI-216
//			final Map<String, Object> map = (Map<String, Object>) 
//					getActiveBioParentSampleReceiving(objParentSampleCollection.getNbioparentsamplecode(), userInfo).getBody();
//		
//			final BioParentSampleReceiving bioParentSampleReceiving = (BioParentSampleReceiving) map.get("selectedBioParentSampleReceiving");
//			
//			if (bioParentSampleReceiving.getNissampleaccesable() != Enumeration.TransactionStatus.YES.gettransactionstatus()) {
//				return new ResponseEntity<>(
//						commonFunction.getMultilingualMessage("IDS_SAMPLEISNOTACCESSIBLE", userInfo.getSlanguagefilename()),
//						HttpStatus.EXPECTATION_FAILED);
//			}

			if (result.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
				final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
				final String ssamplecollectiondate = (objParentSampleCollection.getSsamplecollectiondate() != null
						&& !objParentSampleCollection.getSsamplecollectiondate().isEmpty())
								? "'" + objParentSampleCollection.getSsamplecollectiondate().toString()
										.replace("T", " ").replace("Z", "") + "'"
								: null;
				final String sbiobankarrivaldate = (objParentSampleCollection.getSbiobankarrivaldate() != null
						&& !objParentSampleCollection.getSbiobankarrivaldate().isEmpty())
								? "'" + objParentSampleCollection.getSbiobankarrivaldate().toString().replace("T", " ")
										.replace("Z", "") + "'"
								: null;
				final String stemporarystoragedate = (objParentSampleCollection.getStemporarystoragedate() != null
						&& !objParentSampleCollection.getStemporarystoragedate().isEmpty())
								? "'" + objParentSampleCollection.getStemporarystoragedate().toString()
										.replace("T", " ").replace("Z", "") + "'"
								: null;
				final JSONObject jsonObject = new JSONObject(objParentSampleCollection.getJsonuidata());

				final BioParentSampleCollection objParentSampleCollectionBeforeUpdate = getActiveBioParentSampleCollectionforAudit(
						objParentSampleCollection.getNbioparentsamplecollectioncode(), userInfo);

				listBeforeUpdate.add(objParentSampleCollectionBeforeUpdate);

				final String strUpdate = "update bioparentsamplecollection set nbioparentsamplecode="
						+ objParentSampleCollection.getNbioparentsamplecode() + ", nreceivingsitecode="
						+ objParentSampleCollection.getNreceivingsitecode() + ", nproductcatcode="
						+ objParentSampleCollection.getNproductcatcode() + ", nnoofsamples="
						+ objParentSampleCollection.getNnoofsamples() + ", nstorageconditioncode="
						+ objParentSampleCollection.getNstorageconditioncode() + ", scollectorname='"
						+ stringUtilityFunction.replaceQuote(objParentSampleCollection.getScollectorname())
						+ "', stemporarystoragename='"
						+ stringUtilityFunction.replaceQuote(objParentSampleCollection.getStemporarystoragename())
						+ "', ssendername='"
						+ stringUtilityFunction.replaceQuote(objParentSampleCollection.getSsendername())
						+ "', nrecipientusercode=" + objParentSampleCollection.getNrecipientusercode()
						+ ", sinformation='"
						+ stringUtilityFunction.replaceQuote(objParentSampleCollection.getSinformation())
						+ "', jsonuidata='" + jsonObject + "', dsamplecollectiondate=" + ssamplecollectiondate
						+ ", ntzsamplecollectiondate=" + userInfo.getNtimezonecode() + ", noffsetsamplecollectiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", dbiobankarrivaldate=" + sbiobankarrivaldate + ", ntzbiobankarrivaldate="
						+ userInfo.getNtimezonecode() + ", noffsetbiobankarrivaldate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", dtemporarystoragedate=" + stemporarystoragedate + ", ntztemporarystoragedate="
						+ userInfo.getNtimezonecode() + ", noffsettemporarystoragedate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ ", dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "', ntztransactiondate=" + userInfo.getNtimezonecode() + ", noffsettransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", nusercode="
						+ userInfo.getNusercode() + ", nuserrolecode=" + userInfo.getNuserrole()
						+ " where nbioparentsamplecollectioncode="
						+ objParentSampleCollection.getNbioparentsamplecollectioncode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(strUpdate);

				// ===== COC: START =====
//				if (objParentSampleCollection.getNbioparentsamplecollectioncode() > 0) {
//
//					String sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//					jdbcTemplate.execute(sQuery);
//
//					sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//					jdbcTemplate.execute(sQuery);
//
//					sQuery = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//					jdbcTemplate.execute(sQuery);
//
//					int chainCustodyPk = jdbcTemplate.queryForObject(
//							"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//							Integer.class);
//
//					String strChainCustody = "insert into chaincustody ("
//							+ "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno, "
//							+ "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate, "
//							+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus) " + "select (" + chainCustodyPk
//							+ " + rank() over(order by bpr.nbioparentsamplecode)), " + userInfo.getNformcode()
//							+ ", bpr.nbioparentsamplecode, 'nbioparentsamplecode', "
//							+ "'bioparentsamplereceiving', bpr.sparentsamplecode, "
//							+ Enumeration.TransactionStatus.UPDATED.gettransactionstatus() + ", "
//							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
//							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
//							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
//							+ stringUtilityFunction.replaceQuote(commonFunction
//									.getMultilingualMessage("IDS_BIOSAMPLETYPE", userInfo.getSlanguagefilename()))
//							+ " [' || pc.sproductcatname || '] ' || '"
//							+ stringUtilityFunction.replaceQuote(commonFunction
//									.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename()))
//							+ " [' || bpr.sparentsamplecode || ']', " + userInfo.getNtranssitecode() + ", "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " from bioparentsamplecollection bpsc "
//							+ "join bioparentsamplereceiving bpr on bpr.nbioparentsamplecode = bpsc.nbioparentsamplecode "
//							+ "left join productcategory pc on pc.nproductcatcode = bpsc.nproductcatcode "
//							+ "where bpsc.nsitecode = " + userInfo.getNtranssitecode() + " and bpsc.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and bpsc.nbioparentsamplecollectioncode = "
//							+ objParentSampleCollection.getNbioparentsamplecollectioncode() + ";";
//
//					String strSeqUpdateChain = " update seqnoregistration set nsequenceno=(select " + chainCustodyPk
//							+ " + count(nbioparentsamplecollectioncode) from bioparentsamplecollection"
//							+ " where nbioparentsamplecollectioncode = "
//							+ objParentSampleCollection.getNbioparentsamplecollectioncode() + " and nsitecode = "
//							+ userInfo.getNtranssitecode() + " and nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ ") where stablename = 'chaincustody' and nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//					jdbcTemplate.execute(strChainCustody);
//					jdbcTemplate.execute(strSeqUpdateChain);
//
//				}
				// ===== End COC =====

				final List<String> multilingualIDList = new ArrayList<>();
				final BioParentSampleCollection objParentSampleCollectionAfterUpdate = getActiveBioParentSampleCollectionforAudit(
						objParentSampleCollection.getNbioparentsamplecollectioncode(), userInfo);

				listAfterUpdate.add(objParentSampleCollectionAfterUpdate);

				multilingualIDList.add("IDS_EDITPARENTSAMPLECOLLECTION");

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				final List<BioParentSampleCollection> lstBioParentSampleCollection = getBioParentSampleCollection(
						objParentSampleCollection.getNbioparentsamplecode(), userInfo);
				outputMap.put("lstBioParentSampleCollection", lstBioParentSampleCollection);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						"IDS_SELECTNOTYETPROCESSEDSTATUSRECORD", userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

	}

	@Override
	public ResponseEntity<Object> deleteParentSampleCollection(final int nbioparentsamplecollectioncode,
			final int nbioparentsamplecode, final UserInfo userInfo) throws Exception {
		final String result = validateParentSampleCollection(nbioparentsamplecollectioncode, userInfo);
		if (!result.equals(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus())) {
			if (result.equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {

				final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

				final BioParentSampleCollection objParentSampleCollection = getActiveBioParentSampleCollectionforAudit(
						nbioparentsamplecollectioncode, userInfo);

				// ===== COC: START =====
//				if (nbioparentsamplecollectioncode > 0) {
//
//					String sQuery = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//					jdbcTemplate.execute(sQuery);
//
//					sQuery = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//					jdbcTemplate.execute(sQuery);
//
//					sQuery = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
//					jdbcTemplate.execute(sQuery);
//
//					int chainCustodyPk = jdbcTemplate.queryForObject(
//							"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//							Integer.class);
//
//					String strChainCustody = "insert into chaincustody ("
//							+ "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno, "
//							+ "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate, "
//							+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus) " + "select (" + chainCustodyPk
//							+ " + rank() over(order by bpsc.nbioparentsamplecollectioncode)), "
//							+ userInfo.getNformcode() + ", "
//							+ "bpsc.nbioparentsamplecollectioncode, 'nbioparentsamplecollectioncode', "
//							+ "'bioparentsamplecollection', bpr.sparentsamplecode, "
//							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", "
//							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
//							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
//							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
//							+ stringUtilityFunction.replaceQuote(commonFunction
//									.getMultilingualMessage("IDS_BIOSAMPLETYPE", userInfo.getSlanguagefilename()))
//							+ " [' || pc.sproductcatname || '] ' || '"
//							+ stringUtilityFunction.replaceQuote(commonFunction
//									.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename()))
//							+ " [' || bpr.sparentsamplecode || ']' || ' ' || '"
//							+ stringUtilityFunction.replaceQuote(commonFunction.getMultilingualMessage("IDS_COHORTNO",
//									userInfo.getSlanguagefilename()))
//							+ " [' || bpr.ncohortno || ']', " + userInfo.getNtranssitecode() + ", "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " from bioparentsamplecollection bpsc "
//							+ "join bioparentsamplereceiving bpr on bpr.nbioparentsamplecode = bpsc.nbioparentsamplecode "
//							+ "left join productcategory pc on pc.nproductcatcode = bpsc.nproductcatcode "
//							+ " where bpsc.nsitecode = " + userInfo.getNtranssitecode() + " and bpsc.nstatus = "
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//							+ " and bpsc.nbioparentsamplecollectioncode = " + nbioparentsamplecollectioncode + ";";
//
				String strChainCustody = "update chaincustody set nstatus = "
			            + Enumeration.TransactionStatus.DELETED.gettransactionstatus()
			            + " where ntablepkno = " + nbioparentsamplecollectioncode
			            + " and nformcode = " + userInfo.getNformcode()
			            + " and stablename = 'bioparentsamplecollection'"
			            + " and nsitecode = " + userInfo.getNtranssitecode()
			            + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			            + ";";

					jdbcTemplate.execute(strChainCustody);
//					jdbcTemplate.execute(strSeqUpdateChain);
//				}
				// ===== End COC =====

				final String query = " select 'IDS_PROCESSEDSAMPLERECEIVING' AS Msg from biosamplereceiving  where  nbioparentsamplecollectioncode="
						+ nbioparentsamplecollectioncode + " and nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

				boolean validRecord = false;
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
					validatorDel = projectDAOSupport
							.validateDeleteRecord(Integer.toString(nbioparentsamplecollectioncode), userInfo);
					if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
						validRecord = true;
					} else {
						validRecord = false;
					}
				}

				if (validRecord) {

					final String udpateStr = "update bioparentsamplecollection set nstatus="
							+ Enumeration.TransactionStatus.NA.gettransactionstatus()
							+ " where nbioparentsamplecollectioncode=" + nbioparentsamplecollectioncode;
					jdbcTemplate.execute(udpateStr);

					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> savedList = new ArrayList<>();

					savedList.add(objParentSampleCollection);

					multilingualIDList.add("IDS_DELETEPARENTSAMPLECOLLECTION");

					auditUtilityFunction.fnInsertAuditAction(savedList, 1, null, multilingualIDList, userInfo);

					final List<BioParentSampleCollection> lstBioParentSampleCollection = getBioParentSampleCollection(
							nbioparentsamplecode, userInfo);
					outputMap.put("lstBioParentSampleCollection", lstBioParentSampleCollection);
					return new ResponseEntity<>(outputMap, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
				}

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						"IDS_SELECTNOTYETPROCESSEDSTATUSRECORD", userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	public String validateParentSampleCollection(final int nbioparentsamplecollectioncode, final UserInfo userInfo)
			throws Exception {
		final String strValidate = "select ntransactionstatus from bioparentsamplecollection where nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nbioparentsamplecollectioncode="
				+ nbioparentsamplecollectioncode;
		final BioParentSampleCollection objBioParentSampleCollection = (BioParentSampleCollection) jdbcUtilityTemplateFunction
				.queryForObject(strValidate, BioParentSampleCollection.class, jdbcTemplate);
		String result = Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus();

		if (objBioParentSampleCollection != null) {
			final int intValidate = objBioParentSampleCollection.getNtransactionstatus();
			if (intValidate == Enumeration.TransactionStatus.UNPROCESSED.gettransactionstatus()) {
				result = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			} else {
				result = Enumeration.ReturnStatus.FAILED.getreturnstatus();
			}
		}
		return result;
	}

}
