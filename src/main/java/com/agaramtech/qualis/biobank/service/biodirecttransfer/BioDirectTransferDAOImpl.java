package com.agaramtech.qualis.biobank.service.biodirecttransfer;

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
import com.agaramtech.qualis.biobank.model.BioDirectTransfer;
import com.agaramtech.qualis.biobank.model.BioDirectTransferDetails;
import com.agaramtech.qualis.biobank.model.BioFormAcceptanceDetails;
import com.agaramtech.qualis.biobank.model.BioParentSampleReceiving;
import com.agaramtech.qualis.biobank.model.BioSampleReceiving;
import com.agaramtech.qualis.biobank.model.StorageType;
import com.agaramtech.qualis.biobank.model.TransferType;
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
import com.agaramtech.qualis.storagemanagement.service.samplestoragetransaction.SampleStorageTransactionDAO;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "biodirecttransfer" table by
 * implementing methods from its interface.
 * 
 * @author ATE219
 * @version 11.0.0.2
 * @since 10- NOV- 2025
 */

@AllArgsConstructor
@Repository
public class BioDirectTransferDAOImpl implements BioDirectTransferDAO {

	/** Logger instance for logging Bio Direct Transfer DAO activities */
	private static final Logger LOGGER = LoggerFactory.getLogger(BioDirectTransferDAOImpl.class);

	/** Utility for common string-related operations */
	private final StringUtilityFunction stringUtilityFunction;
	/** Utility providing commonly used application-level functions */
	private final CommonFunction commonFunction;
	/** Spring JDBC template for executing SQL queries and updates */
	private final JdbcTemplate jdbcTemplate;
	/** Utility for simplifying JdbcTemplate operations */
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	/** Utility for date and time-related operations */
	private final DateTimeUtilityFunction dateUtilityFunction;
	/** DAO support class for project-related database operations */
	private final ProjectDAOSupport projectDAOSupport;
	/** Utility for reusable JdbcTemplate helper methods */
	private final JdbcTemplateUtilityFunction jdbcTemplateUtilityFunction;
	/** Utility for handling audit trail and logging operations */
	private final AuditUtilityFunction auditUtilityFunction;
	/** DAO for managing sample storage and transaction mappings */
	private final SampleStorageTransactionDAO sampleStorageMappingDAO;
	/** DAO support for handling email notification persistence */
	private final EmailDAOSupport emailDAOSupport;

	/**
	 * This method is used to retrieve list of all active transfer form for the specified
	 * site.
	 * 
	 * @param inputMap parameter holds the values that required to fetch the transfer form records
	 * @param nbioDirectTransferCode parameter holds the values of primary key of transfer form
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         transfer forms
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public ResponseEntity<Object> getBioDirectTransfer(final Map<String, Object> inputMap,
			final int nbioDirectTransferCode, final UserInfo userInfo) throws Exception {
		LOGGER.info("getBioDirectTransfer");
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		String fromDate = (String) inputMap.get("fromDate");
		String toDate = (String) inputMap.get("toDate");
		int ntransCode = inputMap.containsKey("ntransCode") ? (int) inputMap.get("ntransCode") : -1;

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
		final String strConditionTransCode = ntransCode == 0 ? "" : " and dt.ntransactionstatus=" + ntransCode + " ";

		final String strQuery = "select dt.nbiodirecttransfercode, dt.sformnumber, dt.ntransfertypecode,"
				+ " coalesce(tt.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " tt.jsondata->'sdisplayname'->>'en-US') stransfertypename,  dt.nreceiversitecode,"
				+ " dt.jsondata->>'sreceiversitename' sreceiversitename, dt.dtransferdate,"
				+ " to_char(dt.dtransferdate,'" + userInfo.getSsitedate()
				+ "')  stransferdate, dt.ntztransferdate, dt.noffsetdtransferdate,"
				+ " dt.ntransactionstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, cm.scolorhexcode, dt.jsondata->>'sremarks' sremarks,"
				+ " dt.nsitecode, dt.nstatus, dt.jsondata->>'sstorageconditionname' sstorageconditionname, to_char(dt.ddeliverydate, '"
				+ userInfo.getSsitedate() + "') sdeliverydate," + " dt.jsondata->>'sdispatchername' sdispatchername, "
				+ " dt.jsondata->>'scouriername' scouriername, dt.jsondata->>'scourierno' scourierno, "
				+ " dt.jsondata->>'striplepackage' striplepackage, dt.jsondata->>'svalidationremarks' svalidationremarks from biodirecttransfer dt join transfertype tt on"
				+ " dt.ntransfertypecode=tt.ntransfertypecode and tt.nsitecode=" + userInfo.getNmastersitecode()
				+ " and tt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join transactionstatus ts on dt.ntransactionstatus=ts.ntranscode and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join formwisestatuscolor fwsc on fwsc.ntranscode=ts.ntranscode and fwsc.nformcode="
				+ userInfo.getNformcode() + " and fwsc.nsitecode=" + userInfo.getNmastersitecode()
				+ " and fwsc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join colormaster cm on fwsc.ncolorcode=cm.ncolorcode and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where " + "dt.nsitecode="
				+ userInfo.getNtranssitecode() + " and dt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dt.dtransactiondate between '"
				+ fromDate + "' and '" + toDate + "'" + strConditionTransCode
				+ "  order by dt.nbiodirecttransfercode desc";
		final List<BioDirectTransfer> lstBioDirectTransfer = jdbcTemplate.query(strQuery, new BioDirectTransfer());

		if (!lstBioDirectTransfer.isEmpty()) {
			outputMap.put("lstBioDirectTransfer", lstBioDirectTransfer);
			List<BioDirectTransfer> lstObjBioDirectTransfer = null;
			if (nbioDirectTransferCode == -1) {
				lstObjBioDirectTransfer = lstBioDirectTransfer;
			} else {
				lstObjBioDirectTransfer = lstBioDirectTransfer.stream()
						.filter(x -> x.getNbiodirecttransfercode() == nbioDirectTransferCode)
						.collect(Collectors.toList());
			}
			outputMap.put("selectedBioDirectTransfer", lstObjBioDirectTransfer.get(0));

			List<Map<String, Object>> lstChildBioDirectTransfer = getChildInitialGet(
					lstObjBioDirectTransfer.get(0).getNbiodirecttransfercode(), userInfo);
			outputMap.put("lstChildBioDirectTransfer", lstChildBioDirectTransfer);

			if (inputMap.containsKey(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())) {
				outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						inputMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
			}
		} else {
			outputMap.put("lstBioDirectTransfer", null);
			outputMap.put("selectedBioDirectTransfer", null);
			outputMap.put("lstChildBioDirectTransfer", null);
		}
		outputMap.put("nprimaryKeyBioDirectTransfer",
				inputMap.containsKey("nprimaryKeyBioDirectTransfer") ? inputMap.get("nprimaryKeyBioDirectTransfer")
						: -1);
//		outputMap.putAll(sampleStorageMappingDAO.getsamplestoragetransaction(inputMap, userInfo).getBody());
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active transfer form object based on the specified
	 * nbioDirectTransferCode.
	 * 
	 * @param nbioDirectTransferCode [int] primary key of transfer form object
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return transfer form object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public ResponseEntity<Object> getActiveBioDirectTransfer(final int nbioDirectTransferCode, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strQuery = "select dt.nbiodirecttransfercode, dt.sformnumber, dt.ntransfertypecode,"
				+ " coalesce(tt.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " tt.jsondata->'sdisplayname'->>'en-US') stransfertypename,  dt.nreceiversitecode,"
				+ " dt.jsondata->>'sreceiversitename' sreceiversitename, dt.dtransferdate,"
				+ " to_char(dt.dtransferdate,'" + userInfo.getSsitedate()
				+ "')  stransferdate, dt.ntztransferdate, dt.noffsetdtransferdate,"
				+ " dt.ntransactionstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, dt.jsondata->>'sremarks' sremarks, dt.nsitecode,"
				+ " dt.nstatus, cm.scolorhexcode, dt.jsondata->>'sstorageconditionname' sstorageconditionname, to_char(dt.ddeliverydate, '"
				+ userInfo.getSsitedate() + "') sdeliverydate," + " dt.jsondata->>'sdispatchername' sdispatchername, "
				+ " dt.jsondata->>'scouriername' scouriername, dt.jsondata->>'scourierno' scourierno,"
				+ " dt.jsondata->>'striplepackage' striplepackage, dt.jsondata->>'svalidationremarks' svalidationremarks from biodirecttransfer dt join transfertype tt on"
				+ " dt.ntransfertypecode=tt.ntransfertypecode and tt.nsitecode=" + userInfo.getNmastersitecode()
				+ " and tt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join transactionstatus ts on dt.ntransactionstatus=ts.ntranscode and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join formwisestatuscolor fwsc on fwsc.ntranscode=ts.ntranscode and fwsc.nsitecode="
				+ userInfo.getNmastersitecode() + " and fwsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fwsc.nformcode="
				+ userInfo.getNformcode() + " join colormaster cm on cm.ncolorcode=fwsc.ncolorcode and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where dt.nsitecode="
				+ userInfo.getNtranssitecode() + " and dt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dt.nbiodirecttransfercode="
				+ nbioDirectTransferCode + " order by dt.nbiodirecttransfercode desc";
		final BioDirectTransfer objBioDirectTransfer = (BioDirectTransfer) jdbcUtilityTemplateFunction
				.queryForObject(strQuery, BioDirectTransfer.class, jdbcTemplate);

		if (objBioDirectTransfer != null) {
			outputMap.put("selectedBioDirectTransfer", objBioDirectTransfer);
			List<Map<String, Object>> lstChildBioDirectTransfer = getChildInitialGet(
					objBioDirectTransfer.getNbiodirecttransfercode(), userInfo);
			outputMap.put("lstChildBioDirectTransfer", lstChildBioDirectTransfer);

		} else {
			outputMap.put("selectedBioDirectTransfer", null);
			outputMap.put("lstChildBioDirectTransfer", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active status object
	 * 
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return status object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
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

	/**
	 * This method is used to retrieve active transfer type object
	 * 
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return transfer type object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
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

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active site object based on transfer type
	 * 
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return site object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
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

	/**
	 * This method is used to retrieve active project object based on site
	 * 
	 * @param nbioBankSiteCode holds the primary value of the site
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return project object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
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

	/**
	 * This method is used to retrieve active parent sample object based on project
	 * 
	 * @param nbioProjectCode holds the primary value of the project
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return parent sample object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public ResponseEntity<Object> getParentSampleBasedOnProject(final int nbioProjectCode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final String strNbiosamplereceivingcode = checkDataAlreadyExists(userInfo);

		String nbioSampleReceivingCode = strNbiosamplereceivingcode != null ? strNbiosamplereceivingcode : "-1";

		final String strParentSample = "select sst.nbioparentsamplecode, concat(sst.sparentsamplecode, ' | ',"
				+ " sst.ncohortno) sparentsamplecodecohortno, sst.sparentsamplecode, sst.ncohortno, sst.ssubjectid,"
				+ " sst.scasetype, case when a.nbioparentsamplecode is not null then 'PARTIAL' else 'FULL' end as"
				+ " savailable, case when a.nbioparentsamplecode is not null then 'Partial' else 'Full' end as scolor"
				+ " from samplestoragetransaction sst" + " join biosubjectdetails bsd on bsd.ssubjectid=sst.ssubjectid"
				+ " and bsd.nsitecode=" + userInfo.getNmastersitecode() + " and bsd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bsd.nissampleaccesable="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " left join (select nbioparentsamplecode from biodirecttransferdetails bdtd join biodirecttransfer bdt"
				+ " on bdtd.nbiodirecttransfercode=bdt.nbiodirecttransfercode and bdt.ntransactionstatus in ("
				+ Enumeration.TransactionStatus.SENT.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ") and bdt.nsitecode="
				+ userInfo.getNtranssitecode() + " and bdt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bdtd.nsitecode="
				+ userInfo.getNtranssitecode() + " and bdtd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by nbioparentsamplecode union select nbioparentsamplecode from"
				+ "  biorequestbasedtransferdetails brbtd join biorequestbasedtransfer brbt"
				+ " on brbt.nbiorequestbasedtransfercode=brbtd.nbiorequestbasedtransfercode and brbt.ntransactionstatus"
				+ " in (" + Enumeration.TransactionStatus.SENT.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ") and brbt.nsitecode="
				+ userInfo.getNtranssitecode() + " and brbt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where brbtd.nsitecode="
				+ userInfo.getNtranssitecode() + " and brbtd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by nbioparentsamplecode) a on a.nbioparentsamplecode=sst.nbioparentsamplecode"
				+ " where sst.nprojecttypecode=" + nbioProjectCode + " and sst.nsitecode="
				+ userInfo.getNtranssitecode() + " and sst.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nbiosamplereceivingcode not in ("
				+ nbioSampleReceivingCode + ") "
				+ " group by sst.nbioparentsamplecode, sparentsamplecodecohortno, sst.sparentsamplecode, sst.ncohortno, "
				+ " sst.ssubjectid, sst.scasetype, savailable, scolor order by sst.nbioparentsamplecode ";

		final List<BioParentSampleReceiving> lstGetParentSample = jdbcTemplate.query(strParentSample,
				new BioParentSampleReceiving());

		List<Map<String, Object>> lstParentSample = new ArrayList<>();

		lstGetParentSample.stream().forEach(lst -> {
			Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getSparentsamplecodecohortno());
			mapStatus.put("value", lst.getSparentsamplecodecohortno());
			mapStatus.put("item", lst);
			lstParentSample.add(mapStatus);
		});

		outputMap.put("lstParentSample", lstParentSample);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active storage type object
	 * 
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return parent storage type object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
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

	/**
	 * This method is used to retrieve active sample type object
	 * 
	 * @param inputMap holds the primary value of sample code in the key of sparentsamplecode
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return parent sample type object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public ResponseEntity<Object> getSampleTypeBySampleCode(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final int nstorageTypeCode = (int) inputMap.get("nstoragetypecode");
		final String sparentSampleCode = (String) inputMap.get("sparentsamplecode");
		final int ncohortNo = (int) inputMap.get("ncohortno");

		String strCondition = (nstorageTypeCode != -1) ? (" and sst.nstoragetypecode=" + nstorageTypeCode) : "";

		if (sparentSampleCode != "") {
			final String strSampleType = "select sst.nproductcode, concat(p.sproductname, ' (', pc.sproductcatname, ')') sproductname from samplestoragetransaction sst join "
					+ " product p on p.nproductcode=sst.nproductcode and p.nsitecode=" + userInfo.getNmastersitecode()
					+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " join productcategory pc on pc.nproductcatcode=p.nproductcatcode and pc.nsitecode="
					+ userInfo.getNmastersitecode() + " and pc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where sst.nsitecode="
					+ userInfo.getNtranssitecode() + " and sst.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and sst.nbiosamplereceivingcode not in (select nbiosamplereceivingcode from biodirecttransferdetails"
					+ " where ntransferstatus != " + Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
					+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") and sparentsamplecode='"
					+ sparentSampleCode + "' and ncohortno=" + ncohortNo + " " + strCondition
					+ " group by sst.nproductcode, p.sproductname, pc.sproductcatname order by sst.nproductcode";
			final List<Product> lstGetSampleType = jdbcTemplate.query(strSampleType, new Product());

			List<Map<String, Object>> lstSampleType = new ArrayList<>();

			lstGetSampleType.stream().forEach(lst -> {
				Map<String, Object> mapStatus = new HashMap<>();
				mapStatus.put("label", lst.getSproductname());
				mapStatus.put("value", lst.getNproductcode());
				mapStatus.put("item", lst);
				lstSampleType.add(mapStatus);
			});

			outputMap.put("lstSampleType", lstSampleType);
		} else {
			outputMap.put("lstSampleType", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active sample receiving details object
	 * 
	 * @param inputMap holds the keys that required to get sample receiving details
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return sample receiving details object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@SuppressWarnings("unused")
	public ResponseEntity<Object> getSampleReceivingDetails(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		String strProductCondition = "";
		String strStorageCondition = "";
		final String sparentSampleCode = (String) inputMap.get("sparentsamplecode");
		final int nbioBankSiteCode = Integer.parseInt(inputMap.get("nbiobanksitecode").toString());
		final int nbioProjectCode = Integer.parseInt(inputMap.get("nbioprojectcode").toString());
		final int nproductCode = Integer.parseInt(inputMap.get("nproductcode").toString());
		final int nstorageTypeCode = Integer.parseInt(inputMap.get("nstoragetypecode").toString());
		final int ncohortNo = Integer.parseInt(inputMap.get("ncohortno").toString());

		final String strNbiosamplereceivingcode = checkDataAlreadyExists(userInfo);

		String nbioSampleReceivingCode = strNbiosamplereceivingcode != null ? strNbiosamplereceivingcode : "-1";
		if (nproductCode != -1) {
			strProductCondition = " and sst.nproductcode=" + nproductCode;
		}
		if (nstorageTypeCode != -1) {
			strStorageCondition = " and sst.nstoragetypecode=" + nstorageTypeCode;
		}

		// modified by sujatha ATE_274 BGSI-218 for getting the fields from
		// samplestorageadditionalinfo joined this table which is used to store in the
		// details table's jsondata
		final String strSampleReceivingDetails = "select sst.nbiosamplereceivingcode, sst.spositionvalue srepositoryid,"
				+ " sst.slocationcode, sst.sqty svolume, concat(p.sproductname, ' (', pc.sproductcatname, ')') sproductname,"
				+ " p.nproductcode, p.nproductcatcode, sst.ndiagnostictypecode, sst.ncontainertypecode, sst.ssubjectid,"
				+ " sst.scasetype, sst.nstoragetypecode, sst.nsamplestoragetransactioncode, ssa.sextractedsampleid,ssa.sconcentration, ssa.sqcplatform, ssa.seluent"
				+ " from product p"
				+ " join productcategory pc on pc.nproductcatcode=p.nproductcatcode and pc.nsitecode="
				+ userInfo.getNmastersitecode() + " and pc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join samplestoragetransaction sst on sst.nproductcode=p.nproductcode and "
				+ " sst.sparentsamplecode='" + sparentSampleCode + "' and sst.ncohortno=" + ncohortNo + " "
				+ strProductCondition + strStorageCondition + " and sst.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sst.nsitecode="
				+ userInfo.getNtranssitecode() + " join biosubjectdetails bsd on bsd.ssubjectid=sst.ssubjectid"
				+ " and bsd.nsitecode=" + userInfo.getNmastersitecode() + " and bsd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bsd.nissampleaccesable="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " join samplestorageadditionalinfo ssa on ssa.nsamplestoragetransactioncode = sst.nsamplestoragetransactioncode"
				+ " where p.nsitecode=" + userInfo.getNmastersitecode() + " and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssa.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssa.nsitecode="
				+ userInfo.getNtranssitecode() + " and sst.nbiosamplereceivingcode not in (" + nbioSampleReceivingCode
				+ ") order by sst.nbiosamplereceivingcode";
		final List<BioSampleReceiving> lstGetSampleReceivingDetails = jdbcTemplate.query(strSampleReceivingDetails,
				new BioSampleReceiving());

		if (lstGetSampleReceivingDetails.size() > 0) {
			outputMap.put("lstGetSampleReceivingDetails", lstGetSampleReceivingDetails);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_NOSAMPLESAVAILABLE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to check the samples of the transfer form already exist or not
	 * 
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return nbiosamplereceivingcode of the all samples
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public String checkDataAlreadyExists(UserInfo userInfo) throws Exception {
		final String strCheckDataAlreadyExists = "select string_agg(nbiosamplereceivingcode::text, ',') nbiosamplereceivingcode"
				+ " from biodirecttransferdetails where ntransferstatus not in ("
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.SENT.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ") and nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.queryForObject(strCheckDataAlreadyExists, String.class);
	}

	/**
	 * This method is used to add a new entry to biodirecttransfer table. Need to check for
	 * duplicate entry of samples for the specified transfer form and specified site before saving into
	 * database.
	 * 
	 * @param inputMap holding the data required to create a transfer form and also the sample in the form
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response status and data of added
	 *         transfer form object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> createBioDirectTransfer(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		jdbcTemplate.execute("lock table lockbiodirecttransfer " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		int nprimaryKeyBioDirectTransfer = (int) inputMap.get("nprimaryKeyBioDirectTransfer");

		BioDirectTransfer objBioDirectTransferData = objMapper.convertValue(inputMap.get("bioDirectTransfer"),
				BioDirectTransfer.class);

		List<Map<String, Object>> filteredSampleReceiving = getNotExistSamplesSampleReceivingCode(
				(List<Map<String, Object>>) inputMap.get("filterSelectedSamples"), userInfo);

		if (filteredSampleReceiving.size() == 0) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYEXISTS", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final String stransferDate = (objBioDirectTransferData.getStransferdate() != null
				&& !objBioDirectTransferData.getStransferdate().isEmpty()) ? "'"
						+ objBioDirectTransferData.getStransferdate().toString().replace("T", " ").replace("Z", "")
						+ "'" : null;

		BioDirectTransfer objBioDirectTransfer = null;

		if (nprimaryKeyBioDirectTransfer != -1) {
			final String strCheckAlreadyBioBankSiteExists = "select nbiodirecttransfercode, nreceiversitecode, sformnumber from biodirecttransfer where "
					+ " nreceiversitecode=" + objBioDirectTransferData.getNbiobanksitecode() + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nbiodirecttransfercode="
					+ nprimaryKeyBioDirectTransfer;
			objBioDirectTransfer = (BioDirectTransfer) jdbcTemplateUtilityFunction
					.queryForObject(strCheckAlreadyBioBankSiteExists, BioDirectTransfer.class, jdbcTemplate);
		}

		int seqNoBioDirectTransfer = -1;
		int nbiodirecttransfercode = -1;
		String strInsertDirectTransfer = "";
		String strSeqNoUpdate = "";
		String strUpdateDirectTransfer = "";
		String strInsertDirectTransferHistory = "";

		if (objBioDirectTransfer == null) {

			var seqNoBioDirectTransferHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biodirecttransferhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioDirectTransferHistory++;

			seqNoBioDirectTransfer = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biodirecttransfer' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioDirectTransfer++;
			nbiodirecttransfercode = seqNoBioDirectTransfer;
			final String strformat = projectDAOSupport.getSeqfnFormat("biodirecttransfer",
					"seqnoformatgeneratorbiobank", 0, 0, userInfo);

			strInsertDirectTransfer = "insert into biodirecttransfer(nbiodirecttransfercode, sformnumber,"
					+ " ntransfertypecode, nreceiversitecode, dtransferdate, ntztransferdate, noffsetdtransferdate,"
					+ " ntransactionstatus, jsondata, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
					+ " nsitecode, nstatus) values (" + nbiodirecttransfercode + ", '" + strformat + "', 1, "
					+ objBioDirectTransferData.getNbiobanksitecode() + ", " + stransferDate + ", "
					+ userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
					+ " , json_build_object('sformnumber', '" + strformat + "', 'sremarks', '"
					+ stringUtilityFunction.replaceQuote(objBioDirectTransferData.getSremarks())
					+ "', 'sreceiversitename', '" + objBioDirectTransferData.getSreceiversitename() + "'), '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ");";

			strSeqNoUpdate += "update seqnobiobankmanagement set nsequenceno=" + seqNoBioDirectTransfer + " where"
					+ " stablename='biodirecttransfer' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			inputMap.put("nprimaryKeyBioDirectTransfer", seqNoBioDirectTransfer);
			objBioDirectTransferData.setSformnumber(strformat);

			strInsertDirectTransferHistory = "INSERT INTO biodirecttransferhistory("
					+ "nbiodirecttransferhistorycode, nbiodirecttransfercode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
					+ "noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus)"
					+ "	VALUES (" + seqNoBioDirectTransferHistory + "," + " " + nbiodirecttransfercode + ","
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			strInsertDirectTransferHistory += "UPDATE seqnobiobankmanagement SET nsequenceno = "
					+ seqNoBioDirectTransferHistory + " WHERE stablename = 'biodirecttransferhistory' AND nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
		} else {
			nbiodirecttransfercode = objBioDirectTransfer.getNbiodirecttransfercode();
			strUpdateDirectTransfer = "update biodirecttransfer set dtransferdate=" + stransferDate + ", "
					+ (objBioDirectTransferData.getSremarks().isEmpty() ? ""
							: " jsondata=jsondata || {\"sremarks\": \"" + stringUtilityFunction
									.replaceQuote(objBioDirectTransferData.getSremarks().toString()) + "\"},")
					+ " dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ " ntztransactiondate=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " where"
					+ " nbiodirecttransfercode=" + nbiodirecttransfercode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			objBioDirectTransferData.setSformnumber(objBioDirectTransfer.getSformnumber());
		}

		Map<String, Object> getMap = insertBioDirectTransferDetails(objBioDirectTransferData, filteredSampleReceiving,
				nbiodirecttransfercode, userInfo);

		final String rtnQry = (String) getMap.get("queryString");
		final String sbioDirectTransferDetailsCode = (String) getMap.get("sbioDirectTransferDetailsCode");

		jdbcTemplate.execute(strInsertDirectTransfer + strUpdateDirectTransfer + rtnQry + strSeqNoUpdate
				+ strInsertDirectTransferHistory);

		// ===== COC: START =====
//		if (sbioDirectTransferDetailsCode != null && !sbioDirectTransferDetailsCode.trim().isEmpty()) {
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
//			int chainCustodyPk = jdbcTemplate.queryForObject(
//					"select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
//							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
//					Integer.class);
//
//			String strChainCustody = "insert into chaincustody ("
//					+ "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
//					+ "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
//					+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + (chainCustodyPk)
//					+ " + rank() over(order by bdtd.nbiodirecttransferdetailscode), " + userInfo.getNformcode() + ", "
//					+ " bdtd.nbiodirecttransferdetailscode, 'nbiodirecttransferdetailscode', "
//					+ " 'biodirecttransferdetails', bdt.sformnumber, "
//					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", " + userInfo.getNusercode() + ", "
//					+ userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
//					+ userInfo.getNtimezonecode() + ", "
//					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " '"
//					+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//					+ " [' || bdtd.srepositoryid || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//					+ " ' || bdt.sformnumber, " + userInfo.getNtranssitecode() + ", "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " from biodirecttransferdetails bdtd"
//					+ " join biodirecttransfer bdt on bdt.nbiodirecttransfercode = bdtd.nbiodirecttransfercode"
//					+ " and bdt.nsitecode = " + userInfo.getNtranssitecode() + " and bdt.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bdtd.nsitecode = "
//					+ userInfo.getNtranssitecode() + " and bdtd.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and bdtd.nbiodirecttransferdetailscode in (" + sbioDirectTransferDetailsCode + ");";
//
//			String strSeqUpdateChain = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//					+ " + count(nbiodirecttransferdetailscode)" + " from biodirecttransferdetails"
//					+ " where nbiodirecttransferdetailscode in (" + sbioDirectTransferDetailsCode + ")"
//					+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ ") where stablename = 'chaincustody' and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//			jdbcTemplate.execute(strChainCustody);
//			jdbcTemplate.execute(strSeqUpdateChain);
//		}
		// ===== End COC =====
		final List<Object> lstAuditAfter = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();
		final String strAuditQry = auditQuery(nbiodirecttransfercode, sbioDirectTransferDetailsCode, userInfo);
		List<BioDirectTransfer> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditQry, new BioDirectTransfer());
		lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
		lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_ADDTRANSFERSAMPLES"));
		auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);

		final int ntransCode = (int) inputMap.get("ntransCode");
		inputMap.put("ntransCode",
				ntransCode == 0 ? ntransCode : Enumeration.TransactionStatus.DRAFT.gettransactionstatus());

		if (objBioDirectTransfer == null) {
			final Map<String, Object> mailMap = new HashMap<String, Object>();
			mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));
			mailMap.put("nbiodirecttransfercode", inputMap.get("nprimaryKeyBioDirectTransfer"));
			mailMap.put("nbiodirecttransferdetailscode", sbioDirectTransferDetailsCode);
			
			String query = "SELECT sformnumber FROM biodirecttransfer where nbiodirecttransfercode="
					+ nbiodirecttransfercode;
			String referenceId = jdbcTemplate.queryForObject(query, String.class);
			mailMap.put("ssystemid", referenceId);
			
			final UserInfo mailUserInfo = new UserInfo(userInfo);
			mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
			mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
			emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
		}

		return getBioDirectTransfer(inputMap, nprimaryKeyBioDirectTransfer, userInfo);

	}

	/**
	 * This method is used to get the samples which are not exists for any other forms 
	 * 
	 * @param filteredSampleReceiving holding the list of the samples which are selected to add in the form
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response list of samples which are not added for any form
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public List<Map<String, Object>> getNotExistSamplesSampleReceivingCode(
			final List<Map<String, Object>> filteredSampleReceiving, UserInfo userInfo) throws Exception {
		final String sbioSampleReceivingCode = filteredSampleReceiving.stream()
				.map(x -> String.valueOf(x.get("nbiosamplereceivingcode"))).collect(Collectors.joining(","));

		// Added received status by Gowtham on 11 nov 2025 - jira.id:BGSI-181
		final String strSamplesGet = "select nbiosamplereceivingcode from biodirecttransferdetails where nbiosamplereceivingcode in ("
				+ sbioSampleReceivingCode + ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransferstatus not in ("
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ");";
		List<Integer> versionList = jdbcTemplate.queryForList(strSamplesGet, Integer.class);

		List<Map<String, Object>> nonMatchingSamples = filteredSampleReceiving.stream()
				.filter(map -> !versionList.contains((Integer) map.get("nbiosamplereceivingcode")))
				.collect(Collectors.toList());

		return nonMatchingSamples;
	}

	/**
	 * This method is used to get the samples based on transfer form
	 * 
	 * @param nbioDirectTransferCode holding the primary value of the transfer form
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response list of samples based on nbioDirectTransferCode
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public List<Map<String, Object>> getChildInitialGet(int nbioDirectTransferCode, UserInfo userInfo)
			throws Exception {

		final String strChildGet = "select row_number() over(order by bdtd.nbiodirecttransferdetailscode desc) as nserialno,"
				+ " bdtd.nbiodirecttransferdetailscode, bdtd.nbiodirecttransfercode, bdtd.jsondata->>'sparentsamplecode' sparentsamplecode,"
				+ " bdtd.srepositoryid, concat(p.sproductname, ' (', pc.sproductcatname, ')') sproductname, bdtd.svolume, coalesce(ts1.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', ts1.jsondata->'stransdisplaystatus'->>'en-US') ssamplecondition,"
				+ " coalesce(ts2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts2.jsondata->'stransdisplaystatus'->>'en-US') stransferstatus, r.sreason, bdtd.jsondata->>'ssubjectid' ssubjectid,"
				+ " bdtd.jsondata->>'scasetype' scasetype, bdtd.nstoragetypecode, concat(bdtd.jsondata->>'sparentsamplecode', ' | ', bdtd.ncohortno)"
				+ " sparentsamplecodecohortno from biodirecttransferdetails bdtd join product p on"
				+ " p.nproductcode=bdtd.nproductcode and p.nsitecode=" + userInfo.getNmastersitecode()
				+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join productcategory pc on pc.nproductcatcode=p.nproductcatcode and pc.nsitecode="
				+ userInfo.getNmastersitecode() + " and pc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join transactionstatus ts1 on"
				+ " ts1.ntranscode=bdtd.nsamplecondition and ts1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join transactionstatus ts2 on"
				+ " ts2.ntranscode=bdtd.ntransferstatus and ts2.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " left join reason r on"
				+ " r.nreasoncode=bdtd.nreasoncode and r.nsitecode=" + userInfo.getNmastersitecode() + " and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bdtd.ntransferstatus!="
				+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus()
				+ " and bdtd.nbiodirecttransfercode=" + nbioDirectTransferCode + " and bdtd.nsitecode="
				+ userInfo.getNtranssitecode() + " and bdtd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by nserialno desc";
		return jdbcTemplate.queryForList(strChildGet);

	}

	/**
	 * This method is used to get the active transfer form object based on nbioDirectTransferCode
	 * 
	 * @param nbioDirectTransferCode holding the primary value of the transfer form
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response list of transfer form
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public ResponseEntity<Object> getActiveBioDirectTransferById(final int nbioDirectTransferCode,
			final UserInfo userInfo) throws Exception {
		Map<String, Object> outputMap = new HashMap<>();
		final String strChildDirectTransfer = "select bdt.nbiodirecttransfercode, bdt.nreceiversitecode,"
				+ " bdt.jsondata->>'sreceiversitename' sreceiversitename," + " coalesce(to_char(bdt.dtransferdate, '"
				+ userInfo.getSsitedate() + "'), '-') stransferdate, bdt.jsondata->>'sremarks' sremarks "
				+ " from biodirecttransfer bdt where bdt.nbiodirecttransfercode=" + nbioDirectTransferCode
				+ "  and bdt.nsitecode=" + userInfo.getNtranssitecode() + " and bdt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final BioDirectTransfer objChildDirectTransfer = (BioDirectTransfer) jdbcUtilityTemplateFunction
				.queryForObject(strChildDirectTransfer, BioDirectTransfer.class, jdbcTemplate);

		outputMap.put("selectedChildDirectTransfer", objChildDirectTransfer);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to update the transfer form object
	 * 
	 * @param inputMap holding the data that are changed for the transfer form
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response list of transfer form
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public ResponseEntity<Object> updateBioDirectTransfer(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final int nbioDirectTransferCode = (int) inputMap.get("nbiodirecttransfercode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		final List<Object> listBeforeSave = new ArrayList<>();

		int findStatus = findStatusDirectTransfer(nbioDirectTransferCode, userInfo);

		final String strAuditQry = auditParentQuery(nbioDirectTransferCode, "", "", userInfo);
		// jira-bgsi-212 Mullai Balaji V
		List<BioDirectTransfer> lstAuditBefore = jdbcTemplate.query(strAuditQry, new BioDirectTransfer());

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {
			String stransferDate = (String) inputMap.get("stransferdate");
			String sremarks = (String) inputMap.get("sremarks");

			stransferDate = (stransferDate != null && !stransferDate.isEmpty())
					? "'" + stransferDate.toString().replace("T", " ").replace("Z", "") + "'"
					: null;

			final String strUpdateQry = "update biodirecttransfer set dtransferdate=" + stransferDate
					+ ", jsondata=jsondata || '{\"sremarks\": \"" + stringUtilityFunction.replaceQuote(sremarks)
					+ "\"}', dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ " ntztransactiondate=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + " where"
					+ " nbiodirecttransfercode=" + nbioDirectTransferCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(strUpdateQry);

			List<BioDirectTransfer> lstAuditAfter = jdbcTemplate.query(strAuditQry, new BioDirectTransfer());

			multilingualIDList.add("IDS_EDITTRANSFERFORM");
			listBeforeSave.add(lstAuditBefore.get(0));
			listAfterSave.add(lstAuditAfter.get(0));

			auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);
			return getBioDirectTransfer(inputMap, nbioDirectTransferCode, userInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}

	/**
	 * This method is used to provide audit capture query
	 * 
	 * @param nbioDirectTransferCode holding the primary value of transfer form
	 * @param sbioDirectTransferDetailsCode holding the primary values of samples
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response string value of the query
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public String auditQuery(final int nbioDirectTransferCode, final String sbioDirectTransferDetailsCode,
			final UserInfo userInfo) throws Exception {
		String strConcat = "";
		if (!sbioDirectTransferDetailsCode.isEmpty() && sbioDirectTransferDetailsCode != "") {
			strConcat = " and bdtd.nbiodirecttransferdetailscode in (" + sbioDirectTransferDetailsCode + ")";
		}
		final String strAuditQry = "select bdt.nbiodirecttransfercode, bdt.sformnumber, bdt.nreceiversitecode,"
				+ " bdt.nreceiversitecode nbiobanksitecode, to_char(bdt.dtransferdate, '" + userInfo.getSsitedate()
				+ "') stransferdate, bdt.ntransactionstatus, bdt.jsondata->>'sremarks' sremarks, bp.nbioprojectcode, bp.sprojecttitle,"
				+ " bdtd.nproductcode, bdtd.nstoragetypecode, p.sproductname, bdtd.nbioparentsamplecode ,"
				+ " bdtd.srepositoryid, coalesce(st.jsondata->>'" + userInfo.getSlanguagetypecode()
				+ "', st.jsondata->>'en-US') sstoragetypename, bdt.nstorageconditioncode, "
				+ " to_char(bdt.ddeliverydate, '" + userInfo.getSsitedate()
				+ "') sdeliverydate, bdt.jsondata->>'sdispatchername' sdispatchername, bdt.ncouriercode, bdt.jsondata->>'scourierno' scourierno, "
				+ " bdt.jsondata->>'striplepackage' striplepackage, bdt.jsondata->>'svalidationremarks' svalidationremarks, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') stransferstatus, bdtd.svolume from biodirecttransfer bdt "
				+ " join biodirecttransferdetails bdtd on bdtd.nbiodirecttransfercode=bdt.nbiodirecttransfercode"
				+ " and bdtd.nsitecode=" + userInfo.getNtranssitecode() + " and bdtd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join bioproject bp on"
				+ " bp.nbioprojectcode=bdtd.nbioprojectcode and bp.nsitecode=" + userInfo.getNmastersitecode()
				+ " and bp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join product p"
				+ " on p.nproductcode=bdtd.nproductcode and p.nsitecode=" + userInfo.getNmastersitecode()
				+ " and p.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join storagetype st on st.nstoragetypecode=bdtd.nstoragetypecode and st.nsitecode="
				+ userInfo.getNmastersitecode() + " and st.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join transactionstatus ts on ts.ntranscode=bdtd.ntransferstatus and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bdt.nbiodirecttransfercode="
				+ nbioDirectTransferCode + " and bdt.nsitecode=" + userInfo.getNtranssitecode() + " and bdt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + strConcat
				+ " order by bdt.nbiodirecttransfercode";
		return strAuditQry;
	}

	/**
	 * This method is used to provide audit capture query for transfer form
	 * 
	 * @param nbioDirectTransferCode holding the primary value of transfer form
	 * @param concatSelect holding the string value of the query to concat select query
	 * @param concatJoin holding the string value of the query to concat join query
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response string value of the query
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public String auditParentQuery(final int nbioDirectTransferCode, final String concatSelect, final String concatJoin,
			final UserInfo userInfo) throws Exception {
		final String strParentAuditQry = "select bdt.nbiodirecttransfercode, bdt.sformnumber, bdt.nreceiversitecode,"
				+ " to_char(bdt.dtransferdate, '" + userInfo.getSsitedate()
				+ "') stransferdate, bdt.ntransactionstatus, bdt.jsondata->>'sremarks' sremarks " + concatSelect
				+ " from biodirecttransfer bdt " + concatJoin + " where bdt.nsitecode=" + userInfo.getNtranssitecode()
				+ " and bdt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bdt.nbiodirecttransfercode=" + nbioDirectTransferCode;
		return strParentAuditQry;
	}

	/**
	 * This method is used to provide audit capture query for samples
	 * 
	 * @param nbioDirectTransferCode holding the primary value of transfer form
	 * @param nbioDirectTransferDetailsCode holding the primary value of samples
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response string value of the query
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public String childAuditQuery(final int nbioDirectTransferCode, final String nbioDirectTransferDetailsCode,
			final UserInfo userInfo) throws Exception {
		final String strChildAuditQuery = "select bdt.nbiodirecttransfercode, bdt.sformnumber,"
				+ " bdtd.nbiodirecttransferdetailscode, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') ssamplecondition,"
				+ " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts1.jsondata->'stransdisplaystatus'->>'en-US')"
				+ " ssamplestatus, bdtd.srepositoryid, r.sreason, r.nreasoncode, bdtd.svolume "
				+ " from biodirecttransfer bdt join biodirecttransferdetails bdtd"
				+ " on bdtd.nbiodirecttransfercode=bdt.nbiodirecttransfercode and bdtd.nsitecode="
				+ userInfo.getNtranssitecode() + " and bdtd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join transactionstatus ts on ts.ntranscode=bdtd.nsamplecondition and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join transactionstatus ts1 on ts1.ntranscode=bdtd.ntransferstatus and ts1.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left join reason r on r.nreasoncode=bdtd.nreasoncode and r.nsitecode="
				+ userInfo.getNmastersitecode() + " and r.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bdt.nsitecode="
				+ userInfo.getNtranssitecode() + " and bdt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bdt.nbiodirecttransfercode="
				+ nbioDirectTransferCode + " and bdtd.nbiodirecttransferdetailscode in ("
				+ nbioDirectTransferDetailsCode + ") order by bdt.nbiodirecttransfercode desc;";
		return strChildAuditQuery;
	}

	/**
	 * This method is used to delete the samples
	 * 
	 * @param nbioDirectTransferCode holding the primary value of transfer form
	 * @param nbioDirectTransferDetailsCode holding the primary value of samples
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response status and data of deleted samples object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public ResponseEntity<Object> deleteChildDirectTransfer(final int nbioDirectTransferCode,
			final String nbioDirectTransferDetailsCode, final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();

		final int findStatus = findStatusDirectTransfer(nbioDirectTransferCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			List<Integer> lstSamples = getNonDeletedSamples(nbioDirectTransferDetailsCode, userInfo);
			if (lstSamples.size() == 0) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final String strAuditBeforeQry = childAuditQuery(nbioDirectTransferCode, nbioDirectTransferDetailsCode,
					userInfo);
			List<BioDirectTransferDetails> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditBeforeQry,
					new BioDirectTransferDetails());

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
//					+ " + rank() over(order by bdtd.nbiodirecttransferdetailscode), " + userInfo.getNformcode() + ", "
//					+ " bdtd.nbiodirecttransferdetailscode, 'nbiodirecttransferdetailscode', "
//					+ " 'biodirecttransferdetails', bdt.sformnumber, "
//					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", " + userInfo.getNusercode()
//					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
//					+ userInfo.getNtimezonecode() + ", "
//					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " '"
//					+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//					+ " [' || bdtd.srepositoryid || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//					+ " [' || coalesce((bdtd.jsondata->>'sparentsamplecode'),'') || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//					+ " ' || bdt.sformnumber, " + userInfo.getNtranssitecode() + ", "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " from biodirecttransferdetails bdtd"
//					+ " join biodirecttransfer bdt on bdt.nbiodirecttransfercode = bdtd.nbiodirecttransfercode"
//					+ " and bdt.nsitecode = " + userInfo.getNtranssitecode() + " and bdt.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bdtd.nsitecode = "
//					+ userInfo.getNtranssitecode() + " and bdtd.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and bdtd.nbiodirecttransferdetailscode in (" + nbioDirectTransferDetailsCode + "); ";
//
//			String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//					+ " + count(nbiodirecttransferdetailscode)" + " from biodirecttransferdetails"
//					+ " where nbiodirecttransferdetailscode in (" + nbioDirectTransferDetailsCode + ")"
//					+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ ") where stablename = 'chaincustody' and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//			jdbcTemplate.execute(strChainCustody);
//			jdbcTemplate.execute(strSeqUpdate);
			// ===== End COC =====

			final String strDeleteQry = "update biodirecttransferdetails set nstatus="
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiodirecttransferdetailscode in (" + nbioDirectTransferDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			int seqNoBioDirectTransferDetailsHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biodirecttransferdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strInsertDirectTransferDetailsHistory = "INSERT INTO biodirecttransferdetailshistory ("
					+ "nbiodirecttransferdetailshistorycode, " + "nbiodirecttransferdetailscode, "
					+ "nbiodirecttransfercode, " + "nsamplecondition, " + "ntransferstatus, " + "dtransactiondate, "
					+ "ntztransactiondate, " + "noffsetdtransactiondate, " + "nusercode, " + "nuserrolecode, "
					+ "ndeputyusercode, " + "ndeputyuserrolecode, " + "nsitecode, " + "nstatus) " + "SELECT "
					+ seqNoBioDirectTransferDetailsHistory + "+rank()over(order by b.nbiodirecttransferdetailscode), "
					+ "b.nbiodirecttransferdetailscode, "
					+ "b.nbiodirecttransfercode, b.nsamplecondition, b.ntransferstatus, '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "FROM biodirecttransferdetails b " + "WHERE b.nbiodirecttransfercode = " + nbioDirectTransferCode
					+ " AND  b.nbiodirecttransferdetailscode in ( " + nbioDirectTransferDetailsCode + ") ;";

			strInsertDirectTransferDetailsHistory += "update seqnobiobankmanagement set " + "nsequenceno = (select "
					+ seqNoBioDirectTransferDetailsHistory + " + count(nbiodirecttransferdetailscode) "
					+ "from biodirecttransferdetails WHERE nbiodirecttransfercode = " + nbioDirectTransferCode + " "
					+ "AND  nbiodirecttransferdetailscode in ( " + nbioDirectTransferDetailsCode + " ) )  "
					+ " where stablename='biodirecttransferdetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strDeleteQry + strInsertDirectTransferDetailsHistory);

			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
			lstAuditTransferDetailsBefore.stream().forEach(x -> multilingualIDList.add("IDS_DELETETRANSFERSAMPLES"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditBefore, 1, null, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioDirectTransfer = getChildInitialGet(nbioDirectTransferCode, userInfo);
			outputMap.put("lstChildBioDirectTransfer", lstChildBioDirectTransfer);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to dispose the samples
	 * 
	 * @param nbioDirectTransferCode holding the primary value of transfer form
	 * @param nbioDirectTransferDetailsCode holding the primary value of samples
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response status and data of disposed samples object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public ResponseEntity<Object> disposeSamples(final int nbioDirectTransferCode,
			final String nbioDirectTransferDetailsCode, final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final int findStatus = findStatusDirectTransfer(nbioDirectTransferCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			List<Integer> lstSamples = getNonDeletedSamples(nbioDirectTransferDetailsCode, userInfo);
			if (lstSamples.size() == 0) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final String concatString = " and ntransferstatus in ("
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ")";

			final String validatedDirectTransferDetailsCode = findStatusDirectTransferDetails(nbioDirectTransferCode,
					nbioDirectTransferDetailsCode, concatString, userInfo);

			if (validatedDirectTransferDetailsCode != null) {

				// ===== COC: START =====
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
//				String strChainCustody = "insert into chaincustody ("
//						+ "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
//						+ "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
//						+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + (chainCustodyPk)
//						+ " + rank() over(order by bdtd.nbiodirecttransferdetailscode), " + userInfo.getNformcode()
//						+ ", " + " bdtd.nbiodirecttransferdetailscode, 'nbiodirecttransferdetailscode', "
//						+ " 'biodirecttransferdetails', bdt.sformnumber, "
//						+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ", "
//						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
//						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
//						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " '"
//						+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//						+ " [' || bdtd.srepositoryid || '] ' || '"
//						+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//						+ " [' || coalesce((bdtd.jsondata::jsonb->>'sparentsamplecode'),'') || '] ' || '"
//						+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//						+ " ' || bdt.sformnumber, " + userInfo.getNtranssitecode() + ", "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " from biodirecttransferdetails bdtd"
//						+ " join biodirecttransfer bdt on bdt.nbiodirecttransfercode = bdtd.nbiodirecttransfercode"
//						+ " and bdt.nsitecode = " + userInfo.getNtranssitecode() + " and bdt.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bdtd.nsitecode = "
//						+ userInfo.getNtranssitecode() + " and bdtd.nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ " and bdtd.nbiodirecttransferdetailscode in (" + nbioDirectTransferDetailsCode + "); ";
//
//				String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//						+ " + count(nbiodirecttransferdetailscode)" + " from biodirecttransferdetails"
//						+ " where nbiodirecttransferdetailscode in (" + nbioDirectTransferDetailsCode + ")"
//						+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//						+ ") where stablename = 'chaincustody' and nstatus = "
//						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//				jdbcTemplate.execute(strChainCustody);
//				jdbcTemplate.execute(strSeqUpdate);
				// ===== End COC =====

				final String strDeleteQry = "update biodirecttransferdetails set ntransferstatus="
						+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ", dtransactiondate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
						+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
						+ " where nbiodirecttransferdetailscode in (" + nbioDirectTransferDetailsCode
						+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				final int seqNoBioDirectTransferDetailsHistory = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnobiobankmanagement where stablename='biodirecttransferdetailshistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);

				String strInsertDirectTransferDetailsHistory = "INSERT INTO biodirecttransferdetailshistory ("
						+ "nbiodirecttransferdetailshistorycode, " + "nbiodirecttransferdetailscode, "
						+ "nbiodirecttransfercode, " + "nsamplecondition, " + "ntransferstatus, " + "dtransactiondate, "
						+ "ntztransactiondate, " + "noffsetdtransactiondate, " + "nusercode, " + "nuserrolecode, "
						+ "ndeputyusercode, " + "ndeputyuserrolecode, " + "nsitecode, " + "nstatus) " + "SELECT "
						+ seqNoBioDirectTransferDetailsHistory
						+ "+rank()over(order by b.nbiodirecttransferdetailscode), "
						+ "b.nbiodirecttransferdetailscode, " + "b.nbiodirecttransfercode, "
						+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + ", "
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ "FROM biodirecttransferdetails b " + "WHERE b.nbiodirecttransfercode = "
						+ nbioDirectTransferCode + " AND  b.nbiodirecttransferdetailscode in ( "
						+ nbioDirectTransferDetailsCode + ") " + " AND b.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  ;";

				strInsertDirectTransferDetailsHistory += "update seqnobiobankmanagement set " + "nsequenceno = (select "
						+ seqNoBioDirectTransferDetailsHistory + " + count(nbiodirecttransferdetailscode) "
						+ "from biodirecttransferdetails WHERE nbiodirecttransfercode = " + nbioDirectTransferCode + " "
						+ "AND  nbiodirecttransferdetailscode in ( " + nbioDirectTransferDetailsCode + " ) AND nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")  "
						+ " where stablename='biodirecttransferdetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				jdbcTemplate.execute(strDeleteQry + strInsertDirectTransferDetailsHistory);

				final String strAuditAfterQry = childAuditQuery(nbioDirectTransferCode, nbioDirectTransferDetailsCode,
						userInfo);
				List<BioDirectTransferDetails> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
						new BioDirectTransferDetails());

				lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
				lstAuditTransferDetailsAfter.stream()
						.forEach(x -> multilingualIDList.add("IDS_DISPOSETRANSFERSAMPLES"));

				auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);

				List<Map<String, Object>> lstChildBioDirectTransfer = getChildInitialGet(nbioDirectTransferCode,
						userInfo);
				outputMap.put("lstChildBioDirectTransfer", lstChildBioDirectTransfer);
				return new ResponseEntity<>(outputMap, HttpStatus.OK);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDSAMPLES",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to undo the disposes samples
	 * 
	 * @param nbioDirectTransferCode holding the primary value of transfer form
	 * @param nbioDirectTransferDetailsCode holding the primary value of samples
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response status and data of undo disposed samples object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public ResponseEntity<Object> undoDisposeSamples(final int nbioDirectTransferCode,
			final String nbioDirectTransferDetailsCode, final UserInfo userInfo) throws Exception {

		jdbcTemplate.execute(
				"lock table lockbiodirecttransferdetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final String concatString = " and ntransferstatus in ("
				+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + ")";

		final String validatedDirectTransferDetailsCode = findStatusDirectTransferDetails(nbioDirectTransferCode,
				nbioDirectTransferDetailsCode, concatString, userInfo);

		if (validatedDirectTransferDetailsCode != null) {

			final String strAuditAfterQry = childAuditQuery(nbioDirectTransferCode, validatedDirectTransferDetailsCode,
					userInfo);
			List<BioDirectTransferDetails> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
					new BioDirectTransferDetails());

			String strUpdateQry = "update biodirecttransferdetails set ntransferstatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiodirecttransferdetailscode in (" + validatedDirectTransferDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			final int seqNoBioDirectTransferDetailsHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biodirecttransferdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strInsertDirectTransferDetailsHistory = "INSERT INTO biodirecttransferdetailshistory ("
					+ "nbiodirecttransferdetailshistorycode, " + "nbiodirecttransferdetailscode, "
					+ "nbiodirecttransfercode, " + "nsamplecondition, " + "ntransferstatus, " + "dtransactiondate, "
					+ "ntztransactiondate, " + "noffsetdtransactiondate, " + "nusercode, " + "nuserrolecode, "
					+ "ndeputyusercode, " + "ndeputyuserrolecode, " + "nsitecode, " + "nstatus) " + "SELECT "
					+ seqNoBioDirectTransferDetailsHistory + "+rank()over(order by b.nbiodirecttransferdetailscode), "
					+ "b.nbiodirecttransferdetailscode, " + "b.nbiodirecttransfercode, "
					+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + ", "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "FROM biodirecttransferdetails b " + "WHERE b.nbiodirecttransfercode = " + nbioDirectTransferCode
					+ " AND  b.nbiodirecttransferdetailscode in ( " + nbioDirectTransferDetailsCode + ") "
					+ " AND b.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  ;";

			strInsertDirectTransferDetailsHistory += "update seqnobiobankmanagement set " + "nsequenceno = (select "
					+ seqNoBioDirectTransferDetailsHistory + " + count(nbiodirecttransferdetailscode) "
					+ "from biodirecttransferdetails WHERE nbiodirecttransfercode = " + nbioDirectTransferCode + " "
					+ "AND  nbiodirecttransferdetailscode in ( " + nbioDirectTransferDetailsCode + " ) AND nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")  "
					+ " where stablename='biodirecttransferdetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strUpdateQry + strInsertDirectTransferDetailsHistory);

			// jira bgsi-215 Mullai Balaji V [18-11-2025]
			List<BioDirectTransferDetails> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
					new BioDirectTransferDetails());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_UNDODISPOSESAMPLES"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioDirectTransfer = getChildInitialGet(nbioDirectTransferCode, userInfo);
			outputMap.put("lstChildBioDirectTransfer", lstChildBioDirectTransfer);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDISPOSESAMPLES", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to add the samples to the transfer form
	 * 
	 * @param inputMap holding the data required to add samples to the particular transfer form
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response status and data of transfer form object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> createChildBioDirectTransfer(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		jdbcTemplate.execute(
				"lock table lockbiodirecttransferdetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		int nbioDirectTransferCode = (int) inputMap.get("nbiodirecttransfercode");

		int findStatus = findStatusDirectTransfer(nbioDirectTransferCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {
			BioDirectTransfer objBioDirectTransferData = objMapper.convertValue(inputMap.get("bioChildDirectTransfer"),
					BioDirectTransfer.class);

			List<Map<String, Object>> filteredSampleReceiving = getNotExistSamplesSampleReceivingCode(
					(List<Map<String, Object>>) inputMap.get("filterSelectedSamples"), userInfo);

			if (filteredSampleReceiving.size() == 0) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYEXISTS",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			Map<String, Object> getMap = insertBioDirectTransferDetails(objBioDirectTransferData,
					filteredSampleReceiving, nbioDirectTransferCode, userInfo);

			final String rtnQry = (String) getMap.get("queryString");
			final String sbioDirectTransferDetailsCode = (String) getMap.get("sbioDirectTransferDetailsCode");

			String strUpdateStatus = "update biodirecttransfer set ntransactionstatus="
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiodirecttransfercode=" + nbioDirectTransferCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(rtnQry + strUpdateStatus);

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
//					+ " + rank() over(order by bdtd.nbiodirecttransferdetailscode), " + userInfo.getNformcode() + ", "
//					+ " bdtd.nbiodirecttransferdetailscode, 'nbiodirecttransferdetailscode', "
//					+ " 'biodirecttransferdetails', bdt.sformnumber, "
//					+ Enumeration.TransactionStatus.INSERTED.gettransactionstatus() + ", " + userInfo.getNusercode()
//					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
//					+ userInfo.getNtimezonecode() + ", "
//					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " '"
//					+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//					+ " [' || bdtd.srepositoryid || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//					+ " [' || coalesce((bdtd.jsondata->>'sparentsamplecode'),'') || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//					+ " ' || bdt.sformnumber, " + userInfo.getNtranssitecode() + ", "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " from biodirecttransferdetails bdtd"
//					+ " join biodirecttransfer bdt on bdt.nbiodirecttransfercode = bdtd.nbiodirecttransfercode"
//					+ " and bdt.nsitecode = " + userInfo.getNtranssitecode() + " and bdt.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bdtd.nsitecode = "
//					+ userInfo.getNtranssitecode() + " and bdtd.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and bdtd.nbiodirecttransferdetailscode in (" + sbioDirectTransferDetailsCode + ");";
//
//			String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//					+ " + count(nbiodirecttransferdetailscode)" + " from biodirecttransferdetails"
//					+ " where nbiodirecttransferdetailscode in (" + sbioDirectTransferDetailsCode + ")"
//					+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ ") where stablename = 'chaincustody' and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//			jdbcTemplate.execute(strChainCustody);
//			jdbcTemplate.execute(strSeqUpdate);
			// ===== End COC =====
			final List<Object> lstAuditAfter = new ArrayList<>();
			final List<String> multilingualIDList = new ArrayList<>();
			final String strAuditQry = auditQuery(nbioDirectTransferCode, sbioDirectTransferDetailsCode, userInfo);
			List<BioDirectTransfer> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditQry,
					new BioDirectTransfer());
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_ADDTRANSFERSAMPLES"));
			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);
			return getActiveBioDirectTransfer(nbioDirectTransferCode, userInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}

	/**
	 * This method is used to add the samples to the transfer form
	 * 
	 * @param objBioDirectTransferData holding transfer form pojo object
	 * @param filteredSampleReceiving holding samples that are added under particular transfer form
	 * @param nbioDirectTransferCode holding primary value of the transfer form
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response status and data of transfer form object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public Map<String, Object> insertBioDirectTransferDetails(final BioDirectTransfer objBioDirectTransferData,
			final List<Map<String, Object>> filteredSampleReceiving, final int nbioDirectTransferCode,
			final UserInfo userInfo) throws Exception {

		Map<String, Object> rtnMap = new HashMap<>();

		String sbioDirectTransferDetailsCode = "";

		int seqNoBioDirectTransferDetails = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnobiobankmanagement where stablename='biodirecttransferdetails' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);

		int seqNoBioDirectTransferDetailsHistory = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnobiobankmanagement where stablename='biodirecttransferdetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);

		String strInsertDirectTransferDetails = "insert into biodirecttransferdetails (nbiodirecttransferdetailscode,"
				+ " nbiodirecttransfercode, nbioprojectcode, nbioparentsamplecode,"
				+ " ncohortno, nstoragetypecode, nproductcatcode, nproductcode, srepositoryid, svolume, ssubjectid, sparentsamplecode, jsondata,"
				+ " ndiagnostictypecode, ncontainertypecode, nbiosamplereceivingcode, nsamplecondition, ntransferstatus,"
				+ " nreasoncode, slocationcode, nsamplestoragetransactioncode, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode,"
				+ " nstatus) values ";

		String strInsertDirectTransferDetailsHistory = "INSERT INTO biodirecttransferdetailshistory("
				+ "nbiodirecttransferdetailshistorycode, nbiodirecttransferdetailscode, nbiodirecttransfercode, nsamplecondition, ntransferstatus, "
				+ "dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode, nuserrolecode,"
				+ " ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus)VALUES ";

		for (Map<String, Object> objBioSampleReceiving : filteredSampleReceiving) {
			seqNoBioDirectTransferDetails++;
			seqNoBioDirectTransferDetailsHistory++;
			sbioDirectTransferDetailsCode += seqNoBioDirectTransferDetails + ",";
			final String srepositoryId = (objBioSampleReceiving.get("srepositoryid") != ""
					&& objBioSampleReceiving.get("srepositoryid") != null)
							? "'" + objBioSampleReceiving.get("srepositoryid") + "'"
							: null;
			final String svolume = (objBioSampleReceiving.get("svolume") != ""
					&& objBioSampleReceiving.get("svolume") != null) ? "'" + objBioSampleReceiving.get("svolume") + "'"
							: null;
			final String ssubjectId = (objBioSampleReceiving.get("ssubjectid") != ""
					&& objBioSampleReceiving.get("ssubjectid") != null)
							? "'" + objBioSampleReceiving.get("ssubjectid") + "'"
							: null;
			final String scaseType = (objBioSampleReceiving.get("scasetype") != ""
					&& objBioSampleReceiving.get("scasetype") != null)
							? "'" + objBioSampleReceiving.get("scasetype") + "'"
							: null;
			final String slocationCode = (objBioSampleReceiving.get("slocationcode") != ""
					&& objBioSampleReceiving.get("slocationcode") != null)
							? "'" + objBioSampleReceiving.get("slocationcode") + "'"
							: "''";
			// added by sujatha to store the below data's into the jsondata of
			// biodirecttransferdetails table BGSI-218
			final String extractedSampleId = (objBioSampleReceiving.get("sextractedsampleid") != ""
					&& objBioSampleReceiving.get("sextractedsampleid") != null)
							? "'" + objBioSampleReceiving.get("sextractedsampleid") + "'"
							: null;
			final String concentration = (objBioSampleReceiving.get("sconcentration") != ""
					&& objBioSampleReceiving.get("sconcentration") != null)
							? "'" + objBioSampleReceiving.get("sconcentration") + "'"
							: null;
			final String qcPlatform = (objBioSampleReceiving.get("sqcplatform") != ""
					&& objBioSampleReceiving.get("sqcplatform") != null)
							? "'" + objBioSampleReceiving.get("sqcplatform") + "'"
							: null;
			final String eluent = (objBioSampleReceiving.get("seluent") != ""
					&& objBioSampleReceiving.get("seluent") != null) ? "'" + objBioSampleReceiving.get("seluent") + "'"
							: null;

			// modified by sujatha ATE_274 adding the above new fields(extractedsampleID,
			// concentration, qcplatform,eluent) into the jsondata BGSI-218
			strInsertDirectTransferDetails += "(" + seqNoBioDirectTransferDetails + ", " + nbioDirectTransferCode + ", "
					+ objBioDirectTransferData.getNbioprojectcode() + ", "
					+ objBioDirectTransferData.getNbioparentsamplecode() + ", "
					+ objBioDirectTransferData.getNcohortno() + ", " + objBioSampleReceiving.get("nstoragetypecode")
					+ ", " + objBioSampleReceiving.get("nproductcatcode") + ", "
					+ objBioSampleReceiving.get("nproductcode") + ", " + srepositoryId + ", " + svolume + ", "
					+ ssubjectId + ", '" + objBioDirectTransferData.getSparentsamplecode()
					+ "', json_build_object('sparentsamplecode', '" + objBioDirectTransferData.getSparentsamplecode()
					+ "', 'ssubjectid', " + ssubjectId + ", 'scasetype', " + scaseType + ", 'sformnumber', '"
					+ objBioDirectTransferData.getSformnumber() + "','"
					+ Enumeration.FormCode.BIODIRECTTRANSFER.getFormCode() + "','"
					+ objBioDirectTransferData.getSformnumber() + "', 'svolume', " + svolume + ", 'srepositoryid', "
					+ srepositoryId + ", 'sextractedsampleid', " + extractedSampleId + ", 'sconcentration', "
					+ concentration + ", 'sqcplatform', " + qcPlatform + ", 'seluent', " + eluent + "), "
					+ objBioSampleReceiving.get("ndiagnostictypecode") + ", "
					+ objBioSampleReceiving.get("ncontainertypecode") + ", "
					+ objBioSampleReceiving.get("nbiosamplereceivingcode") + ", "
					+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + ", "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", -1, " + slocationCode + ", "
					+ objBioSampleReceiving.get("nsamplestoragetransactioncode") + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "),";

			strInsertDirectTransferDetailsHistory += "(" + seqNoBioDirectTransferDetailsHistory + "," + " "
					+ seqNoBioDirectTransferDetails + "," + nbioDirectTransferCode + ","
					+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";

		}

		strInsertDirectTransferDetails = strInsertDirectTransferDetails.substring(0,
				strInsertDirectTransferDetails.length() - 1) + ";";

		sbioDirectTransferDetailsCode = sbioDirectTransferDetailsCode.substring(0,
				sbioDirectTransferDetailsCode.length() - 1);

		strInsertDirectTransferDetailsHistory = strInsertDirectTransferDetailsHistory.substring(0,
				strInsertDirectTransferDetailsHistory.length() - 1) + ";";

		String strSeqNoUpdate = "update seqnobiobankmanagement set nsequenceno=" + seqNoBioDirectTransferDetails
				+ " where stablename='biodirecttransferdetails' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		final String strSeqNoUpdateHistory = "update seqnobiobankmanagement set nsequenceno="
				+ seqNoBioDirectTransferDetailsHistory
				+ " where stablename='biodirecttransferdetailshistory' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		rtnMap.put("queryString", strInsertDirectTransferDetails + strInsertDirectTransferDetailsHistory
				+ strSeqNoUpdate + strSeqNoUpdateHistory);
		rtnMap.put("sbioDirectTransferDetailsCode", sbioDirectTransferDetailsCode);

		return rtnMap;
	}

	/**
	 * This method is used to retrieve active sample condition object
	 * 
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return sample condition object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
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

	/**
	 * This method is used to retrieve active reason object
	 * 
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return reason object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
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

	/**
	 * This method is used to update the sample condition object
	 * 
	 * @param inputMap holding the data that are changed for the sample condition
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response list of samples
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public ResponseEntity<Object> updateSampleCondition(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		Map<String, Object> outputMap = new HashMap<>();

		final int nbioDirectTransferCode = (int) inputMap.get("nbiodirecttransfercode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final int findStatus = findStatusDirectTransfer(nbioDirectTransferCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			final String nbioDirectTransferDetailsCode = (String) inputMap.get("nbiodirecttransferdetailscode");
			final int nreasonCode = (int) inputMap.get("nreasoncode");
			final int nsampleCondition = (int) inputMap.get("nsamplecondition");

			List<Integer> lstSamples = getNonDeletedSamples(nbioDirectTransferDetailsCode, userInfo);
			if (lstSamples.size() == 0) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final String strAuditAfterQry = childAuditQuery(nbioDirectTransferCode, nbioDirectTransferDetailsCode,
					userInfo);
			List<BioDirectTransferDetails> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
					new BioDirectTransferDetails());

			final String strUpdateQry = "update biodirecttransferdetails set nreasoncode=" + nreasonCode
					+ ", nsamplecondition=" + nsampleCondition + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiodirecttransferdetailscode in (" + nbioDirectTransferDetailsCode + ") and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			final int seqNoBioDirectTransferDetailsHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biodirecttransferdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			String strInsertDirectTransferDetailsHistory = "INSERT INTO biodirecttransferdetailshistory ("
					+ "nbiodirecttransferdetailshistorycode, " + "nbiodirecttransferdetailscode, "
					+ "nbiodirecttransfercode, " + "nsamplecondition, " + "ntransferstatus, " + "dtransactiondate, "
					+ "ntztransactiondate, " + "noffsetdtransactiondate, " + "nusercode, " + "nuserrolecode, "
					+ "ndeputyusercode, " + "ndeputyuserrolecode, " + "nsitecode, " + "nstatus) " + "SELECT "
					+ seqNoBioDirectTransferDetailsHistory + "+rank()over(order by b.nbiodirecttransferdetailscode), "
					+ "b.nbiodirecttransferdetailscode, " + "b.nbiodirecttransfercode, "
					+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + ", "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "FROM biodirecttransferdetails b " + "WHERE b.nbiodirecttransfercode = " + nbioDirectTransferCode
					+ " AND  b.nbiodirecttransferdetailscode in ( " + nbioDirectTransferDetailsCode + ") "
					+ " AND b.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  ;";

			strInsertDirectTransferDetailsHistory += "update seqnobiobankmanagement set " + "nsequenceno = (select "
					+ seqNoBioDirectTransferDetailsHistory + " + count(nbiodirecttransferdetailscode) "
					+ "from biodirecttransferdetails WHERE nbiodirecttransfercode = " + nbioDirectTransferCode + " "
					+ "AND  nbiodirecttransferdetailscode in ( " + nbioDirectTransferDetailsCode + " ) AND nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")  "
					+ " where stablename='biodirecttransferdetailshistory' and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strUpdateQry + strInsertDirectTransferDetailsHistory);

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
//			String remarksPrefix = commonFunction.getMultilingualMessage("IDS_REPOSITORYID",
//					userInfo.getSlanguagefilename()) + " ['||COALESCE(bdtd.srepositoryid,'')||'] '||'"
//					+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//					+ " ['||COALESCE((bdtd.jsondata::jsonb->>'sparentsamplecode'),'')||'] '||'"
//					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename()) + " '";
//
//			String strChainCustody = "insert into chaincustody ("
//					+ "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
//					+ "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
//					+ "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)" + " select " + (chainCustodyPk)
//					+ " + rank() over(order by bdtd.nbiodirecttransferdetailscode), " + userInfo.getNformcode() + ", "
//					+ " bdtd.nbiodirecttransferdetailscode, 'nbiodirecttransferdetailscode', "
//					+ " 'biodirecttransferdetails', bdt.sformnumber, " + nsampleCondition + ", "
//					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
//					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
//					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '" + remarksPrefix
//					+ "||bdt.sformnumber, " + userInfo.getNtranssitecode() + ", "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " from biodirecttransferdetails bdtd"
//					+ " join biodirecttransfer bdt on bdt.nbiodirecttransfercode = bdtd.nbiodirecttransfercode"
//					+ " and bdt.nsitecode = " + userInfo.getNtranssitecode() + " and bdt.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bdtd.nsitecode = "
//					+ userInfo.getNtranssitecode() + " and bdtd.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and bdtd.nbiodirecttransferdetailscode in (" + nbioDirectTransferDetailsCode + "); ";
//
//			String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//					+ " + count(nbiodirecttransferdetailscode)" + " from biodirecttransferdetails"
//					+ " where nbiodirecttransferdetailscode in (" + nbioDirectTransferDetailsCode + ")"
//					+ " and nsitecode = " + userInfo.getNtranssitecode() + " and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ ") where stablename = 'chaincustody' and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//			jdbcTemplate.execute(strChainCustody);
//			jdbcTemplate.execute(strSeqUpdate);
			// ===== End COC =====

			List<BioDirectTransferDetails> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
					new BioDirectTransferDetails());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_VALIDATESAMPLE"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			List<Map<String, Object>> lstChildBioDirectTransfer = getChildInitialGet(nbioDirectTransferCode, userInfo);
			outputMap.put("lstChildBioDirectTransfer", lstChildBioDirectTransfer);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to retrieve active storage condition object
	 * 
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return storage condition object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
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

	/**
	 * This method is used to retrieve active users object based on site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return users object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
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

	/**
	 * This method is used to retrieve active courier object
	 * 
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return courier object
	 * @throws Exception that are thrown from this DAO layer
	 */
	
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

	/**
	 * This method is used to validate the transfer form
	 * 
	 * @param inputMap holding the data that are validated for the transfer form
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response list of transfer forms
	 * @throws Exception that are thrown from this DAO layer
	 */

	public ResponseEntity<Object> createValidationBioDirectTransfer(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();

		final int nbioDirectTransferCode = (int) inputMap.get("nbiodirecttransfercode");
		final int findStatus = findStatusDirectTransfer(nbioDirectTransferCode, userInfo);

		final List<Object> savedBioDirectTransferList = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			final String strBioDirectTransferDetailsCode = getToBeDisposeSamples(nbioDirectTransferCode, userInfo);

			final String concatCondition = (strBioDirectTransferDetailsCode != null
					&& strBioDirectTransferDetailsCode != "")
							? " and nbiodirecttransferdetailscode not in (" + strBioDirectTransferDetailsCode + ")"
							: "";

			BioDirectTransfer objBioDirectTransfer = objMapper.convertValue(inputMap.get("bioDirectTransfer"),
					BioDirectTransfer.class);

			final String sdeliveryDate = (objBioDirectTransfer.getSdeliverydate() != null
					&& !objBioDirectTransfer.getSdeliverydate().isEmpty()) ? "'"
							+ objBioDirectTransfer.getSdeliverydate().toString().replace("T", " ").replace("Z", "")
							+ "'" : null;
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
//					+ " + rank() over(order by bdtd.nbiodirecttransferdetailscode), " + userInfo.getNformcode() + ", "
//					+ " bdtd.nbiodirecttransferdetailscode, 'nbiodirecttransferdetailscode', "
//					+ " 'biodirecttransferdetails', bdt.sformnumber, "
//					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", " + userInfo.getNusercode()
//					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
//					+ userInfo.getNtimezonecode() + ", "
//					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", " + " '"
//					+ " Repository ID [' || bdtd.srepositoryid || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//					+ " [' || coalesce((bdtd.jsondata::jsonb->>'sparentsamplecode'),'') || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//					+ " ' || bdt.sformnumber, " + userInfo.getNtranssitecode() + ", "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " from biodirecttransferdetails bdtd"
//					+ " join biodirecttransfer bdt on bdt.nbiodirecttransfercode = bdtd.nbiodirecttransfercode"
//					+ " and bdt.nsitecode = " + userInfo.getNtranssitecode() + " and bdt.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bdtd.nsitecode = "
//					+ userInfo.getNtranssitecode() + " and bdtd.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and bdtd.nbiodirecttransfercode = " + nbioDirectTransferCode + concatCondition + ";";
//
//			String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//					+ " + count(nbiodirecttransferdetailscode)" + " from biodirecttransferdetails"
//					+ " where nbiodirecttransfercode = " + nbioDirectTransferCode + " and nsitecode = "
//					+ userInfo.getNtranssitecode() + " and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + concatCondition
//					+ ") where stablename = 'chaincustody' and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//			jdbcTemplate.execute(strChainCustody);
//			jdbcTemplate.execute(strSeqUpdate);
			// ===== End COC =====

			final String strUpdateDirectTransfer = "update biodirecttransfer set nstorageconditioncode="
					+ objBioDirectTransfer.getNstorageconditioncode() + ", ddeliverydate=" + sdeliveryDate
					+ ", ndispatchercode=" + objBioDirectTransfer.getNdispatchercode() + ", ncouriercode="
					+ objBioDirectTransfer.getNcouriercode() + ", ntransactionstatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ ", jsondata=jsondata || '{\"scourierno\": \""
					+ stringUtilityFunction.replaceQuote(objBioDirectTransfer.getScourierno())
					+ "\", \"striplepackage\": \""
					+ stringUtilityFunction.replaceQuote(objBioDirectTransfer.getStriplepackage()) + "\","
					+ " \"svalidationremarks\": \""
					+ stringUtilityFunction.replaceQuote(objBioDirectTransfer.getSvalidationremarks())
					+ "\", \"scouriername\": \""
					+ stringUtilityFunction.replaceQuote(objBioDirectTransfer.getScouriername())
					+ "\", \"sstorageconditionname\": \""
					+ stringUtilityFunction.replaceQuote(objBioDirectTransfer.getSstorageconditionname())
					+ "\", \"sdispatchername\": \""
					+ stringUtilityFunction.replaceQuote(objBioDirectTransfer.getSdispatchername()) + "\"}'"
					+ " where nbiodirecttransfercode=" + nbioDirectTransferCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			final String strUpdateDirectTransferDetails = "update biodirecttransferdetails set ntransferstatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiodirecttransfercode=" + nbioDirectTransferCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + concatCondition + ";";

			var seqNoBioDirectTransferHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biodirecttransferhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioDirectTransferHistory++;

			var strInsertDirectTransferHistory = "INSERT INTO biodirecttransferhistory("
					+ "nbiodirecttransferhistorycode, nbiodirecttransfercode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
					+ "noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus)"
					+ "	VALUES (" + seqNoBioDirectTransferHistory + "," + " " + nbioDirectTransferCode + ","
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			strInsertDirectTransferHistory += "UPDATE seqnobiobankmanagement SET nsequenceno = "
					+ seqNoBioDirectTransferHistory + " WHERE stablename = 'biodirecttransferhistory' AND nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate
					.execute(strUpdateDirectTransfer + strUpdateDirectTransferDetails + strInsertDirectTransferHistory);

			final String concatSelect = ", bdt.nstorageconditioncode, to_char(bdt.ddeliverydate, '"
					+ userInfo.getSsitedate() + "') sdeliverydate,"
					+ " bdt.jsondata->>'sdispatchername' sdispatchername, "
					+ " bdt.ncouriercode, bdt.jsondata->>'scourierno' scourierno,"
					+ " bdt.jsondata->>'striplepackage' striplepackage, bdt.jsondata->>'svalidationremarks' svalidationremarks ";
			final String concatJoin = "";
			final String strAuditQry = auditParentQuery(nbioDirectTransferCode, concatSelect, concatJoin, userInfo);
			List<BioDirectTransfer> lstAuditAfter = jdbcTemplate.query(strAuditQry, new BioDirectTransfer());

			lstAuditAfter.stream().forEach(x -> multilingualIDList.add("IDS_VALIDATETRANSFERFORM"));
			savedBioDirectTransferList.addAll(lstAuditAfter);
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
				mailMap.put("nbiodirecttransfercode", nbioDirectTransferCode);
				String query = "SELECT sformnumber FROM biodirecttransfer where nbiodirecttransfercode="
						+ nbioDirectTransferCode;
				String referenceId = jdbcTemplate.queryForObject(query, String.class);
				mailMap.put("ssystemid", referenceId);
				final UserInfo mailUserInfo = new UserInfo(userInfo);
				mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
				mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
				emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
			}
			return getActiveBioDirectTransfer(nbioDirectTransferCode, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	/**
	 * This method is used to transfer the transfer form
	 * 
	 * @param inputMap holding the data that are transfered for the transfer form
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response list of transfer forms
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public ResponseEntity<Object> transferDirectTransfer(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		jdbcTemplate.execute("lock table lockbioformacceptance " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				"lock table lockbioformacceptancedetails " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());
		jdbcTemplate.execute(
				" lock  table locksamplestoragetransaction " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final int nbioDirectTransferCode = (int) inputMap.get("nbiodirecttransfercode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		final List<Object> listBeforeSave = new ArrayList<>();

		final int findStatus = findStatusDirectTransfer(nbioDirectTransferCode, userInfo);
		if (findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {
			final boolean rejectedRecordExists = checkApprovedSample(nbioDirectTransferCode, userInfo);
			if (rejectedRecordExists) {
				final boolean availableSamples = checkSamplesAvailableToTransfer(nbioDirectTransferCode, userInfo);
				if (availableSamples) {

					final String strAuditQry = auditQuery(nbioDirectTransferCode, "", userInfo);
					List<BioDirectTransfer> lstAuditBefore = jdbcTemplate.query(strAuditQry, new BioDirectTransfer());

					final int intRetrievalPk = jdbcTemplate
							.queryForObject(
									"select nsequenceno from seqnostoragemanagement where "
											+ "stablename='samplestorageretrieval' and nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
									Integer.class);

					int intFormAcceptancePk = jdbcTemplate
							.queryForObject(
									"select nsequenceno from seqnobiobankmanagement where "
											+ "stablename='bioformacceptance' and nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
									Integer.class);
					intFormAcceptancePk++;

					int formAcceptanceHistoryPk = jdbcTemplate
							.queryForObject(
									"select nsequenceno from seqnobiobankmanagement where "
											+ "stablename='bioformacceptancehistory' and nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
									Integer.class);
					formAcceptanceHistoryPk++;
					int formAcceptanceHistoryDetailsPk = jdbcTemplate
							.queryForObject(
									"select nsequenceno from seqnobiobankmanagement where "
											+ "stablename='bioformacceptdetailshistory' and nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
									Integer.class);

					final String strBioDirectTransferDetailsCode = getToBeDisposeSamples(nbioDirectTransferCode,
							userInfo);
					String concatConditionDirectTransferDetailsNot = (strBioDirectTransferDetailsCode != ""
							&& strBioDirectTransferDetailsCode != null)
									? " and nbiodirecttransferdetailscode not in (" + strBioDirectTransferDetailsCode
											+ ")"
									: "";
					String concatConditionDirectTransferDetails = (strBioDirectTransferDetailsCode != ""
							&& strBioDirectTransferDetailsCode != null)
									? " and nbiodirecttransferdetailscode in (" + strBioDirectTransferDetailsCode + ")"
									: "";

					// ===== COC: START =====New

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
					        + "  nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
					        + "  ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
					        + "  noffsetdtransactiondate, sremarks, nsitecode, nstatus)"
					        + " select " + (chainCustodyPk)
					        + " + rank() over(order by bdtd.nbiodirecttransferdetailscode), "
					        + userInfo.getNformcode() + ", "
					        + " bdtd.nbiodirecttransferdetailscode, 'nbiodirecttransferdetailscode', "
					        + " 'biodirecttransferdetails', bdtd.srepositoryid , "
					        + Enumeration.TransactionStatus.RETRIEVED.gettransactionstatus() + ", "
					        + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
					        + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					        + userInfo.getNtimezonecode() + ", "
					        + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					        + " '"
					        + commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
					        + " [' || bdtd.srepositoryid || '] ' || '"
					        + commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
					        + " ' || bdt.sformnumber || ' , ' || '"
					        + commonFunction.getMultilingualMessage("IDS_SENTFROM", userInfo.getSlanguagefilename())
					        + " [' || COALESCE(sf.ssitename, '') || '] , ' || '"
					        + commonFunction.getMultilingualMessage("IDS_SENTTO", userInfo.getSlanguagefilename())
					        + " [' || COALESCE(st.ssitename, '') || ']' , "
					        + userInfo.getNtranssitecode() + ", "
					        + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					        + " from biodirecttransferdetails bdtd"
					        + " join biodirecttransfer bdt on bdt.nbiodirecttransfercode = bdtd.nbiodirecttransfercode"
					        + " and bdt.nsitecode = " + userInfo.getNtranssitecode()
					        + " and bdt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					        + " left join site sf on sf.nsitecode = bdt.nsitecode"
					        + " and sf.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					        + " left join site st on st.nsitecode = bdt.nreceiversitecode"
					        + " and st.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					        + " where bdtd.nsitecode = " + userInfo.getNtranssitecode()
					        + " and bdtd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					        + " and bdtd.nbiodirecttransfercode = " + nbioDirectTransferCode + ";";

					String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
							+ " + count(nbiodirecttransferdetailscode)" + " from biodirecttransferdetails"
							+ " where nbiodirecttransfercode = " + nbioDirectTransferCode + " and nsitecode = "
							+ userInfo.getNtranssitecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ ") where stablename = 'chaincustody' and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					jdbcTemplate.execute(strChainCustody);
					jdbcTemplate.execute(strSeqUpdate);
					// ===== End COC =====

					String strTransferDirectTransfer = "update biodirecttransfer set ntransactionstatus="
							+ Enumeration.TransactionStatus.SENT.gettransactionstatus() + ", dtransactiondate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
							+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ " where nbiodirecttransfercode=" + nbioDirectTransferCode + " and nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					strTransferDirectTransfer += "update biodirecttransferdetails set ntransferstatus="
							+ Enumeration.TransactionStatus.SENT.gettransactionstatus() + ", dtransactiondate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
							+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ " where nbiodirecttransfercode=" + nbioDirectTransferCode + " and nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ concatConditionDirectTransferDetailsNot + ";";

					var strSampleStorageRetrieval = "insert into samplestorageretrieval (nsamplestorageretrievalcode,"
							+ " nsamplestoragetransactioncode, nsamplestoragelocationcode, nsamplestoragelistcode,"
							+ " nsamplestoragemappingcode, nprojecttypecode, nusercode, nuserrolecode, nbiosamplereceivingcode,"
							+ " sposition, spositionvalue, jsondata, ntransactionstatus,ntransferstatuscode, "
							+ "ninstrumentcode, nbioparentsamplecode, sparentsamplecode, ncohortno, nproductcatcode, nproductcode, sqty,"
							+ " slocationcode, ssubjectid, scasetype, ndiagnostictypecode, ncontainertypecode, nstoragetypecode, dtransactiondate,"
							+ "noffsetdtransactiondate, ntransdatetimezonecode, nsitecode, nstatus)" + " select "
							+ intRetrievalPk + "+rank()over(order by bdtd.nbiodirecttransferdetailscode),"
							+ " sst.nsamplestoragetransactioncode, sst.nsamplestoragelocationcode, -1,"
							+ " sst.nsamplestoragemappingcode, sst.nprojecttypecode, " + userInfo.getNusercode() + ", "
							+ userInfo.getNuserrole() + ", bdtd.nbiosamplereceivingcode,"
							+ " sst.sposition, sst.spositionvalue, sst.jsondata, "
							+ Enumeration.TransactionStatus.RETRIEVED.gettransactionstatus() + ","
							+ Enumeration.TransactionStatus.SENT.gettransactionstatus() + ","
//							+ Enumeration.TransactionStatus.NA.gettransactionstatus() // commentted and the below ninstrumentcode added by sujatha ATE_274 for an issue of getting -1
							+ " sst.ninstrumentcode, "
							+ " bdtd.nbioparentsamplecode, bdtd.jsondata->>'sparentsamplecode', bdtd.ncohortno, "
							+ "bdtd.nproductcatcode,  bdtd.nproductcode, bdtd.jsondata->>'svolume', bdtd.slocationcode, "
							+ "bdtd.jsondata->>'ssubjectid', bdtd.jsondata->>'scasetype', bdtd.ndiagnostictypecode,"
							+ " bdtd.ncontainertypecode, bdtd.nstoragetypecode," + "'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNtimezonecode() + ", sst.nsitecode, "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " from biodirecttransferdetails bdtd "
							+ " join samplestoragetransaction sst on bdtd.nbiosamplereceivingcode=sst.nbiosamplereceivingcode"
							+ " and sst.nsitecode=" + userInfo.getNtranssitecode() + " and sst.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bdtd.nsitecode="
							+ userInfo.getNtranssitecode() + " and bdtd.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and bdtd.nbiodirecttransfercode=" + nbioDirectTransferCode
							+ concatConditionDirectTransferDetailsNot + " order by bdtd.nbiodirecttransferdetailscode"
							+ ";";

					// added by sujatha ATE_274 for inserting into sampleretrievaladditionalinfo
					// while inserting in samplestorageretrieval bgsi-218
					var strSampleRetrivalAdditionalinfo = "INSERT INTO public.sampleretrievaladditionalinfo"
							+ "( nsamplestorageretrievalcode, sextractedsampleid, sconcentration,"
							+ " sqcplatform, seluent, dmodifieddate, nsitecode, nstatus)" + " select " + intRetrievalPk
							+ "+rank()over(order by bdtd.nbiodirecttransferdetailscode ) ,"
							+ " ssa.sextractedsampleid, ssa.sconcentration, ssa.sqcplatform, ssa.seluent, " + "'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' , ssa.nsitecode, "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " from samplestorageadditionalinfo ssa JOIN samplestoragetransaction sst "
							+ " ON ssa.nsamplestoragetransactioncode = sst.nsamplestoragetransactioncode "
							+ " AND sst.nsitecode =" + userInfo.getNtranssitecode() + " AND sst.nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " JOIN biodirecttransferdetails bdtd ON bdtd.nbiosamplereceivingcode = sst.nbiosamplereceivingcode"
							+ " AND bdtd.nsitecode =" + userInfo.getNtranssitecode() + " AND bdtd.nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " WHERE bdtd.nbiodirecttransfercode =" + nbioDirectTransferCode
							+ concatConditionDirectTransferDetailsNot
							+ " order by bdtd.nbiodirecttransferdetailscode ;";

					final int seqNoRetrievalCount = jdbcTemplate.queryForObject(
							"select count(bdtd.nbiodirecttransferdetailscode) from biodirecttransferdetails bdtd"
									+ " join samplestoragetransaction sst on bdtd.nbiosamplereceivingcode=sst.nbiosamplereceivingcode"
									+ " and sst.nsitecode=" + userInfo.getNtranssitecode() + " and sst.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " where bdtd.nsitecode=" + userInfo.getNtranssitecode() + " and bdtd.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and bdtd.nbiodirecttransfercode=" + nbioDirectTransferCode
									+ concatConditionDirectTransferDetailsNot + ";",
							Integer.class);

					String strBioFormAcceptance = "insert into bioformacceptance (nbioformacceptancecode, nbiodirecttransfercode,"
							+ " nbiorequestbasedtransfercode, nbiobankreturncode, sformnumber, ntransfertypecode, nformtypecode, noriginsitecode,"
							+ " nsenderusercode, nsenderuserrolecode, dtransferdate, ntztransferdate, noffsetdtransferdate,"
							+ " ntransactionstatus," + " nstorageconditioncode, ddeliverydate, ntzdeliverydate,"
							+ " noffsetddeliverydate, ndispatchercode, ncouriercode," + " jsondata, "
							+ "dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode,"
							+ " nstatus) select " + intFormAcceptancePk
							+ ", nbiodirecttransfercode, -1, -1, sformnumber," + " ntransfertypecode, 1, "
							+ userInfo.getNtranssitecode() + ", " + userInfo.getNusercode() + ", "
							+ userInfo.getNuserrole() + ", dtransferdate, ntztransferdate, noffsetdtransferdate, "
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
							+ " nstorageconditioncode, ddeliverydate, ntzdeliverydate, noffsetddeliverydate, ndispatchercode,"
							+ " ncouriercode," + " jsondata || '{\"soriginsitename\": \"" + userInfo.getSsitename()
							+ "\"}', " + " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ userInfo.getNtimezonecode() + ", "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ ", nreceiversitecode, " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " from biodirecttransfer where nbiodirecttransfercode=" + nbioDirectTransferCode
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode=" + userInfo.getNtranssitecode() + " order by nbiodirecttransfercode;";

					strBioFormAcceptance += "insert into bioformacceptancehistory (nbioformacceptancehistorycode,"
							+ " nbioformacceptancecode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
							+ " noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode,"
							+ " nstatus) select " + formAcceptanceHistoryPk + ", " + intFormAcceptancePk + ", "
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole()
							+ ", nreceiversitecode, " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " from biodirecttransfer where nbiodirecttransfercode=" + nbioDirectTransferCode
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsitecode=" + userInfo.getNtranssitecode() + " order by nbiodirecttransfercode;";

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
							+ " nbioformacceptancecode, nbioprojectcode, nbioparentsamplecode," + " ncohortno,"
							+ " nstoragetypecode, nproductcatcode, nproductcode, srepositoryid, svolume, sreceivedvolume, ssubjectid, jsondata,"
							+ " ndiagnostictypecode, "
							+ " ncontainertypecode, nbiosamplereceivingcode, nsamplecondition, nsamplestatus, nreasoncode, dtransactiondate,"
							+ " ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus) select "
							+ intFormAcceptanceDetailsPk + "+rank()over(order by nbiodirecttransferdetailscode), "
							+ intFormAcceptancePk + ", nbioprojectcode, " + " nbioparentsamplecode,"
							+ " ncohortno, nstoragetypecode, nproductcatcode, nproductcode,"
							+ " srepositoryid, svolume, svolume, jsondata->>'ssubjectid', jsondata,"
							+ " ndiagnostictypecode, ncontainertypecode, nbiosamplereceivingcode,"
							+ " nsamplecondition, " + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
							+ ", -1, '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
							+ userInfo.getNtimezonecode() + ", "
							+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
							+ ", (select nreceiversitecode from biodirecttransfer where nbiodirecttransfercode="
							+ nbioDirectTransferCode + " and nsitecode=" + userInfo.getNtranssitecode()
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "), "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " from biodirecttransferdetails where nbiodirecttransfercode=" + nbioDirectTransferCode
							+ " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransferstatus="
							+ Enumeration.TransactionStatus.SENT.gettransactionstatus()
							+ " order by nbiodirecttransferdetailscode;";

					strBioFormAcceptanceDetails += "insert into bioformacceptdetailshistory"
							+ " (nbioformacceptdetailshistorycode, nbioformacceptancedetailscode, nbioformacceptancecode,"
							+ " nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
							+ " nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) "
							+ "select " + formAcceptanceHistoryDetailsPk
							+ "+rank()over(order by nbioformacceptancedetailscode),"
							+ " nbioformacceptancedetailscode, nbioformacceptancecode, nsamplecondition, nsamplestatus, '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " from bioformacceptancedetails where " + " nbioformacceptancecode=" + intFormAcceptancePk
							+ " and nsitecode="
							+ "(select nreceiversitecode from biodirecttransfer where nbiodirecttransfercode="
							+ nbioDirectTransferCode + " and nsitecode=" + userInfo.getNtranssitecode()
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " order by nbioformacceptancedetailscode;";

					strBioFormAcceptanceDetails += " update seqnobiobankmanagement set nsequenceno=(" + "select "
							+ formAcceptanceHistoryDetailsPk
							+ " + count(nbioformacceptancedetailscode) from bioformacceptancedetails where"
							+ " nbioformacceptancecode=" + intFormAcceptancePk + " and nsitecode="
							+ "(select nreceiversitecode from biodirecttransfer where nbiodirecttransfercode="
							+ nbioDirectTransferCode + " and nsitecode=" + userInfo.getNtranssitecode()
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
							+ " where stablename='bioformacceptdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					strBioFormAcceptanceDetails += " update seqnobiobankmanagement set nsequenceno=(" + "select "
							+ intFormAcceptanceDetailsPk
							+ " + count(nbiodirecttransferdetailscode) from biodirecttransferdetails where"
							+ " nbiodirecttransfercode=" + nbioDirectTransferCode + " and nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransferstatus="
							+ Enumeration.TransactionStatus.SENT.gettransactionstatus()
							+ ") where stablename='bioformacceptancedetails' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					strSampleStorageRetrieval += "update seqnostoragemanagement set nsequenceno="
							+ (intRetrievalPk + seqNoRetrievalCount)
							+ " where stablename='samplestorageretrieval' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					String strMoveToDispose = "";
					String strMoveToDisposeDetails = "";
					if (strBioDirectTransferDetailsCode != "" && strBioDirectTransferDetailsCode != null) {
						strTransferDirectTransfer += "update biodirecttransferdetails set ntransferstatus="
								+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus()
								+ ", dtransactiondate='" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
								+ " ntztransactiondate=" + userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
								+ " where nbiodirecttransfercode=" + nbioDirectTransferCode + " and nsitecode="
								+ userInfo.getNtranssitecode() + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
								+ concatConditionDirectTransferDetails + ";";

						int intBioMoveToDisposePk = jdbcTemplate.queryForObject(
								"select nsequenceno from seqnobiobankmanagement where "
										+ "stablename='biomovetodispose' and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";",
								Integer.class);
						int intBioMoveToDisposeDetailsPk = jdbcTemplate.queryForObject(
								"select nsequenceno from seqnobiobankmanagement where "
										+ "stablename='biomovetodisposedetails' and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";",
								Integer.class);
						intBioMoveToDisposePk++;
						strMoveToDispose += "insert into biomovetodispose (nbiomovetodisposecode, sformnumber,"
								+ " ntransfertypecode, nformtypecode, nthirdpartycode, ntransactionstatus, noriginsitecode, sremarks,"
								+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)"
								+ " select " + intBioMoveToDisposePk + ", sformnumber, ntransfertypecode, 1, -1, "
								+ Enumeration.TransactionStatus.SENT.gettransactionstatus() + ", "
								+ userInfo.getNtranssitecode() + ", jsondata->>'sremarks', '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
								+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
								+ userInfo.getNtranssitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " from biodirecttransfer where nsitecode=" + userInfo.getNtranssitecode()
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
								+ " nbiodirecttransfercode=" + nbioDirectTransferCode + ";";
						strMoveToDispose += " update seqnobiobankmanagement set nsequenceno=" + intBioMoveToDisposePk
								+ " where stablename='biomovetodispose' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
						// modified by sujatha ATE_274 by adding 4 more keys in the json data bgsi-218
						strMoveToDisposeDetails += "insert into biomovetodisposedetails (nbiomovetodisposedetailscode,"
								+ " nbiomovetodisposecode, nbioprojectcode, nbioparentsamplecode,"
								+ " nsamplestoragetransactioncode, svolume, jsondata, ncohortno, nstoragetypecode,"
								+ " nproductcatcode, nproductcode, nsamplecondition, ndiagnostictypecode, "
								+ " ncontainertypecode, nsamplestatus, nreasoncode, dtransactiondate, "
								+ " ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)" + " select "
								+ intBioMoveToDisposeDetailsPk + "+rank()over(order by nbiodirecttransferdetailscode), "
								+ intBioMoveToDisposePk
								+ ", nbioprojectcode, nbioparentsamplecode, nsamplestoragetransactioncode, svolume, jsonb_build_object('sparentsamplecode',"
								+ " jsondata->>'sparentsamplecode', 'srepositoryid', srepositoryid, 'ssubjectid', jsondata->>'ssubjectid', "
								+ " 'scasetype', jsondata->>'scasetype', 'slocationcode', case when slocationcode is null then '' else slocationcode end,"
								+ " 'sextractedsampleid', jsondata->>'sextractedsampleid','sconcentration', jsondata->>'sconcentration','sqcplatform', jsondata->>'sqcplatform',"
								+ " 'seluent', jsondata->>'seluent','"+Enumeration.FormCode.BIODIRECTTRANSFER.getFormCode()+"',jsondata->>'sformnumber'), ncohortno, nstoragetypecode, nproductcatcode, nproductcode, "
								+ " nsamplecondition, ndiagnostictypecode, ncontainertypecode, "
								+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ", nreasoncode, "
								+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ userInfo.getNtimezonecode() + ", "
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
								+ userInfo.getNtranssitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " from biodirecttransferdetails where nsitecode=" + userInfo.getNtranssitecode()
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
								+ " nbiodirecttransfercode=" + nbioDirectTransferCode
								+ concatConditionDirectTransferDetails + ";";

						strMoveToDisposeDetails += "update seqnobiobankmanagement set nsequenceno=(select "
								+ intBioMoveToDisposeDetailsPk
								+ "+ count(nbiodirecttransferdetailscode) from biodirecttransferdetails where "
								+ " nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nbiodirecttransfercode=" + nbioDirectTransferCode
								+ concatConditionDirectTransferDetails + ") where stablename='biomovetodisposedetails'"
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					}

					final String strDeleteSampleStorageTransaction = "delete from samplestoragetransaction"
							+ " where nsamplestoragetransactioncode in "
							+ "(select sst.nsamplestoragetransactioncode from biodirecttransfer bdt,"
							+ " biodirecttransferdetails bdtd, samplestoragetransaction sst where"
							+ " bdt.nbiodirecttransfercode=bdtd.nbiodirecttransfercode"
							+ " and bdt.nsitecode=bdtd.nsitecode"
							+ " and bdtd.nbiosamplereceivingcode=sst.nbiosamplereceivingcode"
							+ " and bdtd.nsitecode=sst.nsitecode and bdt.nsitecode=" + userInfo.getNtranssitecode()
							+ " and bdt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and bdtd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and sst.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and bdt.nbiodirecttransfercode=" + nbioDirectTransferCode + " "
							+ concatConditionDirectTransferDetailsNot + ");";

					// added by sujatha ATE_274 for deleting the record in
					// samplestorageadditionalinfo bgsi-218
					final String strDeleteSampleStorageAdditionalInfo = "delete from samplestorageadditionalinfo"
							+ " where nsamplestoragetransactioncode in ("
							+ " select sst.nsamplestoragetransactioncode from biodirecttransfer bdt,"
							+ " biodirecttransferdetails bdtd, samplestoragetransaction sst where"
							+ " bdt.nbiodirecttransfercode=bdtd.nbiodirecttransfercode"
							+ " and bdt.nsitecode=bdtd.nsitecode"
							+ " and bdtd.nbiosamplereceivingcode=sst.nbiosamplereceivingcode"
							+ " and bdtd.nsitecode=sst.nsitecode and bdt.nsitecode=" + userInfo.getNtranssitecode()
							+ " and bdt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and bdtd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and sst.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and bdt.nbiodirecttransfercode=" + nbioDirectTransferCode + " "
							+ concatConditionDirectTransferDetailsNot + " );";

					var seqNoBioDirectTransferHistory = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnobiobankmanagement where stablename='biodirecttransferhistory' and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);
					seqNoBioDirectTransferHistory++;

					var strInsertDirectTransferHistory = "INSERT INTO biodirecttransferhistory("
							+ "nbiodirecttransferhistorycode, nbiodirecttransfercode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
							+ "noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus)"
							+ "	VALUES (" + seqNoBioDirectTransferHistory + "," + " " + nbioDirectTransferCode + ","
							+ Enumeration.TransactionStatus.SENT.gettransactionstatus() + ",'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

					strInsertDirectTransferHistory += "UPDATE seqnobiobankmanagement SET nsequenceno = "
							+ seqNoBioDirectTransferHistory
							+ " WHERE stablename = 'biodirecttransferhistory' AND nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					// modified by sujatha ATE_274 by adding the insert & delete script for
					// sampleretrieveladditionalinfo & samplestorageadditionalinfo bgsi-218
					jdbcTemplate.execute(strTransferDirectTransfer + strSampleStorageRetrieval
							+ strSampleRetrivalAdditionalinfo + strBioFormAcceptance + strBioFormAcceptanceDetails
							+ strDeleteSampleStorageAdditionalInfo + strDeleteSampleStorageTransaction
							+ strMoveToDispose + strMoveToDisposeDetails + strInsertDirectTransferHistory);
					inputMap.put("ntransCode", Enumeration.TransactionStatus.SENT.gettransactionstatus());

					List<BioDirectTransfer> lstAuditAfter = jdbcTemplate.query(strAuditQry, new BioDirectTransfer());

					lstAuditAfter.stream().forEach(x -> multilingualIDList.add("IDS_TRANSFERSAMPLES"));
					;
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
						mailMap.put("nbiodirecttransfercode", nbioDirectTransferCode);

						String receiverquery = "Select nreceiversitecode from biodirecttransfer where nbiodirecttransfercode= "
								+ nbioDirectTransferCode + " and nstatus ="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						Integer nreceiversitecode = jdbcTemplate.queryForObject(receiverquery, Integer.class);
						mailMap.put("nreceiversitecode", nreceiversitecode);
						String query = "SELECT sformnumber FROM biodirecttransfer where nbiodirecttransfercode="
								+ nbioDirectTransferCode + " and nstatus ="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						String referenceId = jdbcTemplate.queryForObject(query, String.class);
						mailMap.put("ssystemid", referenceId);
						final UserInfo mailUserInfo = new UserInfo(userInfo);
						mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
						mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
						emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
					}

					return getActiveBioDirectTransfer(nbioDirectTransferCode, userInfo);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_NOAVAILABLESAMPLESTOTRANSFER",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_REMOVEREJECTEDSAMPLESTOTRANSFER",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to check the available samples under transfer form
	 * 
	 * @param nbioDirectTransferCode holding primary value of transfer form
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response list of samples
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public boolean checkSamplesAvailableToTransfer(final int nbioDirectTransferCode, UserInfo userInfo)
			throws Exception {
		final String strCheck = "select exists(select from biodirecttransferdetails where nbiodirecttransfercode="
				+ nbioDirectTransferCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransferstatus not in ("
				+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + "))";
		return jdbcTemplate.queryForObject(strCheck, boolean.class);
	}

	/**
	 * This method is used to find the status of transfer form
	 * 
	 * @param nbioDirectTransferCode holding primary value of transfer form
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response of primary key
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public int findStatusDirectTransfer(final int nbioDirectTransferCode, final UserInfo userInfo) throws Exception {
		final String strStatusDirectTransfer = "select ntransactionstatus from biodirecttransfer where nbiodirecttransfercode="
				+ nbioDirectTransferCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.queryForObject(strStatusDirectTransfer, Integer.class);
	}

	/**
	 * This method is used to check the approved samples
	 * 
	 * @param nbioDirectTransferCode holding primary value of transfer form
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response of boolean value
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public boolean checkApprovedSample(final int nbioDirectTransferCode, final UserInfo userInfo) throws Exception {
		final String strCheckAllSamplesApproved = "select not exists (select nbiodirecttransferdetailscode from biodirecttransferdetails"
				+ " where nbiodirecttransfercode=" + nbioDirectTransferCode + " and nsamplecondition="
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " and ntransferstatus="
				+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
		return jdbcTemplate.queryForObject(strCheckAllSamplesApproved, boolean.class);
	}

	/**
	 * This method is used to cancel the transfer form
	 * 
	 * @param inputMap data to cancel the transfer form
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response of list of transfer form
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public ResponseEntity<Object> cancelDirectTransfer(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final int nbioDirectTransferCode = (int) inputMap.get("nbiodirecttransfercode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		final List<Object> listBeforeSave = new ArrayList<>();

		final int recordStatus = findStatusDirectTransfer(nbioDirectTransferCode, userInfo);

		if (recordStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| recordStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			final String strAuditQry = auditParentQuery(nbioDirectTransferCode, "", "", userInfo);
			List<BioDirectTransfer> lstAuditBefore = jdbcTemplate.query(strAuditQry, new BioDirectTransfer());

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
//					+ " + rank() over(order by bdtd.nbiodirecttransferdetailscode), " + userInfo.getNformcode() + ", "
//					+ " bdtd.nbiodirecttransferdetailscode, 'nbiodirecttransferdetailscode', "
//					+ " 'biodirecttransferdetails', bdt.sformnumber, "
//					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", " + userInfo.getNusercode()
//					+ ", " + userInfo.getNuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
//					+ userInfo.getNtimezonecode() + ", "
//					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
//					+ commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
//					+ " [' || bdtd.srepositoryid || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
//					+ " [' || coalesce((bdtd.jsondata::jsonb->>'sparentsamplecode'),'') || '] ' || '"
//					+ commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
//					+ " ' || bdt.sformnumber, " + userInfo.getNtranssitecode() + ", "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " from biodirecttransferdetails bdtd"
//					+ " join biodirecttransfer bdt on bdt.nbiodirecttransfercode = bdtd.nbiodirecttransfercode"
//					+ " and bdt.nsitecode = " + userInfo.getNtranssitecode() + " and bdt.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bdtd.nsitecode = "
//					+ userInfo.getNtranssitecode() + " and bdtd.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ " and bdtd.nbiodirecttransfercode = " + nbioDirectTransferCode + ";";
//
//			String strSeqUpdate = "update seqnoregistration set nsequenceno = (select " + chainCustodyPk
//					+ " + count(nbiodirecttransferdetailscode)" + " from biodirecttransferdetails"
//					+ " where nbiodirecttransfercode = " + nbioDirectTransferCode + " and nsitecode = "
//					+ userInfo.getNtranssitecode() + " and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
//					+ ") where stablename = 'chaincustody' and nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
//
//			jdbcTemplate.execute(strChainCustody);
//			jdbcTemplate.execute(strSeqUpdate);
			// ===== End COC =====

			final String strCancelDirectTransfer = "update biodirecttransfer set ntransactionstatus="
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiodirecttransfercode=" + nbioDirectTransferCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			final String strCancelDirectTransferDetails = "update biodirecttransferdetails set ntransferstatus="
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiodirecttransfercode=" + nbioDirectTransferCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			var seqNoBioDirectTransferHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biodirecttransferhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioDirectTransferHistory++;

			var strInsertDirectTransferHistory = "INSERT INTO biodirecttransferhistory("
					+ "nbiodirecttransferhistorycode, nbiodirecttransfercode, ntransactionstatus, dtransactiondate, ntztransactiondate,"
					+ "noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus)"
					+ "	VALUES (" + seqNoBioDirectTransferHistory + "," + " " + nbioDirectTransferCode + ","
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

			strInsertDirectTransferHistory += "UPDATE seqnobiobankmanagement SET nsequenceno = "
					+ seqNoBioDirectTransferHistory + " WHERE stablename = 'biodirecttransferhistory' AND nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate
					.execute(strCancelDirectTransfer + strCancelDirectTransferDetails + strInsertDirectTransferHistory);

			List<BioDirectTransfer> lstAuditAfter = jdbcTemplate.query(strAuditQry, new BioDirectTransfer());

			lstAuditAfter.stream().forEach(x -> multilingualIDList.add("IDS_CANCELTRANSFERFORM"));
			listBeforeSave.addAll(lstAuditBefore);
			listAfterSave.addAll(lstAuditAfter);

			auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);

			return getActiveBioDirectTransfer(nbioDirectTransferCode, userInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method is used to accept or reject the samples
	 * 
	 * @param inputMap holding data to accept or reject the samples
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response of list of samples
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> acceptRejectDirectTransferSlide(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final int nbioDirectTransferCode = (int) inputMap.get("nbiodirecttransfercode");
		List<Map<String, Object>> addedChildBioDirectTransfer = (List<Map<String, Object>>) inputMap
				.get("addedChildBioDirectTransfer");
		int findStatus = findStatusDirectTransfer(nbioDirectTransferCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			if (addedChildBioDirectTransfer.size() > 0) {
				final String sbioDirectTransferDetailsCode = addedChildBioDirectTransfer.stream()
						.map(x -> String.valueOf(x.get("nbiodirecttransferdetailscode")))
						.collect(Collectors.joining(","));
				List<Integer> lstSamples = getNonDeletedSamples(sbioDirectTransferDetailsCode, userInfo);
				if (lstSamples.size() == 0) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESALREADYDELETED",
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

	/**
	 * This method is used to get the disposed sample object
	 * 
	 * @param nbioDirectTransferCode holding primary value of the transfer form
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response of disposed sample list
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public String getToBeDisposeSamples(final int nbioDirectTransferCode, final UserInfo userInfo) throws Exception {
		final String strToBeDisposeSamples = "select string_agg(nbiodirecttransferdetailscode::text, ',') from"
				+ " biodirecttransferdetails where nbiodirecttransfercode=" + nbioDirectTransferCode + " and nsitecode="
				+ userInfo.getNtranssitecode() + " and ntransferstatus="
				+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.queryForObject(strToBeDisposeSamples, String.class);
	}

	/**
	 * This method is used to get the samples which are not deleted from the transfer form
	 * 
	 * @param sbioDirectTransferDetailsCode holding primary values of the samples
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response of non deleted sample list
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public List<Integer> getNonDeletedSamples(final String sbioDirectTransferDetailsCode, UserInfo userInfo)
			throws Exception {

		final String strSamples = "select nbiodirecttransferdetailscode from biodirecttransferdetails where nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nbiodirecttransferdetailscode in (" + sbioDirectTransferDetailsCode
				+ ") and ntransferstatus != " + Enumeration.TransactionStatus.CANCELED.gettransactionstatus();
		List<Integer> lstSamplesCode = jdbcTemplate.queryForList(strSamples, Integer.class);
		return lstSamplesCode;
	}

	/**
	 * This method is used to find the samples status object
	 * 
	 * @param nbioDirectTransferCode holding primary value of the transfer form
	 * @param nbioDirectTransferDetailsCode holding primary value of the samples
	 * @param concatString holding string value that are used for the query to concat
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response of query to execute
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	public String findStatusDirectTransferDetails(final int nbioDirectTransferCode,
			final String nbioDirectTransferDetailsCode, final String concatString, final UserInfo userInfo)
			throws Exception {
		final String strStatusDirectTransfer = "select nbiodirecttransferdetailscode from biodirecttransferdetails where"
				+ " nbiodirecttransfercode=" + nbioDirectTransferCode + " and nbiodirecttransferdetailscode in ("
				+ nbioDirectTransferDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + concatString;
		List<Integer> lstStatusDirectTransfer = jdbcTemplate.queryForList(strStatusDirectTransfer, Integer.class);
		String strDirectTransferDetailsCode = null;
		if (lstStatusDirectTransfer.size() > 0) {
			strDirectTransferDetailsCode = lstStatusDirectTransfer.stream().map(String::valueOf)
					.collect(Collectors.joining(", "));
		}
		return strDirectTransferDetailsCode;
	}

	/**
	 * This method is used to check the sample that are accessible or not
	 * 
	 * @param nbioDirectTransferCode holding primary value of the transfer form
	 * @param userInfo [UserInfo] holding logged in user details based on which the list is to be fetched
	 * @return response entity object holding response of list of sample details
	 * @throws Exception that are thrown from this DAO layer
	 */
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> checkAccessibleSamples(final int nbioDirectTransferCode, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final String strCheckAccessibeSample = "select nbiodirecttransferdetailscode, srepositoryid from"
				+ " biodirecttransferdetails bdtd join biosubjectdetails bsd on bsd.ssubjectid=bdtd.ssubjectid"
				+ " and bsd.nsitecode=" + userInfo.getNmastersitecode() + " and bsd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where bdtd.nsitecode="
				+ userInfo.getNtranssitecode() + " and bdtd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bdtd.nbiodirecttransfercode="
				+ nbioDirectTransferCode + " and bsd.nissampleaccesable="
				+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " order by 1 desc";
		List<Map<String, Object>> nonAccessibleSamples = jdbcTemplate.queryForList(strCheckAccessibeSample);

		outputMap.put("nonAccessibleSamples", nonAccessibleSamples);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

}
