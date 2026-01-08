package com.agaramtech.qualis.samplescheduling.service.samplescheduling;

import java.time.Instant;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.samplescheduling.model.SampleLocation;
import com.agaramtech.qualis.samplescheduling.model.SampleScheduling;
import com.agaramtech.qualis.samplescheduling.model.SampleSchedulingFile;
import com.agaramtech.qualis.samplescheduling.model.SampleSchedulingHistory;
import com.agaramtech.qualis.samplescheduling.model.SampleSchedulingLocation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SampleSchedulingDAOImpl implements SampleSchedulingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleSchedulingDAOImpl.class);
	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final CommonFunction commonFunction;
	private final StringUtilityFunction stringUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	/**
	 * Service method to mark a record as Planned in the sampleschedulinglocation
	 * table.
	 *
	 * @param objmap a map containing: - fromYear, toYear, and status details of the
	 *               complaint history - sampleschedulinglocation details - userInfo
	 *               (logged-in user details) - nmasterSiteCode (primary key of the
	 *               site object for which the list is fetched)
	 * @return ResponseEntity object containing the response status and data of the
	 *         updated sampleschedulinglocation record
	 * @throws Exception if any error occurs in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> plannedSampleScheduling(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final int nsampleschedulingcode = (int) inputMap.get("primarykey");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		SampleScheduling objSampleScheduling = objMapper.convertValue(inputMap.get("samplescheduling"),
				SampleScheduling.class);

		SampleScheduling sampleSchedulingstatus = getSampleSchedulingStatus(nsampleschedulingcode, userInfo);

		String query = "Select * from sampleschedulinglocation where nsampleschedulingcode=" + nsampleschedulingcode
				+ "  " + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode = " + userInfo.getNmastersitecode();

		final List<SampleScheduling> isPresentLocation = (List<SampleScheduling>) jdbcTemplate.query(query,
				new SampleScheduling());

		if (isPresentLocation == null || isPresentLocation.isEmpty()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_ADDLOCATIONFORSAMPLESCHEDULING",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}

		if (sampleSchedulingstatus != null && sampleSchedulingstatus
				.getNtransactionstatus() == Enumeration.TransactionStatus.PLANNED.gettransactionstatus()) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ALREADYPLANNED", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {
			final SampleScheduling sampleScheduling = getActiveSampleSchedulingById(nsampleschedulingcode, userInfo);
			if (sampleScheduling == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final List<String> multilingualIdList = new ArrayList<>();
				final List<Object> saveSampleSchedulingList = new ArrayList<>();
				String lockHistory = "lock table sampleschedulinghistory "
						+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
				jdbcTemplate.execute(lockHistory);
				String historySeqQuery = "select nsequenceno from seqnosamplescheduling "
						+ " where stablename ='sampleschedulinghistory'" + "  and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int historySeqNo = jdbcTemplate.queryForObject(historySeqQuery, Integer.class) + 1;
				// Changes timezone and offset by using common functions on oct-1-2025 - SWSM-78
				String insertHistoryQuery = "INSERT INTO sampleschedulinghistory ("
						+ "nsampleschedulinghistorycode, nsampleschedulingcode, ntransactionstatus, "
						+ "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, "
						+ "dtransactiondate, ntztransactiondate, noffsetdtransactiondate,dmodifieddate,nsitecode, nstatus) "
						+ "VALUES (" + historySeqNo + ", " + nsampleschedulingcode + ", "
						+ Enumeration.TransactionStatus.PLANNED.gettransactionstatus() + ", " + userInfo.getNusercode()
						+ ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", "
						+ userInfo.getNdeputyuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "',"
						// + "', " + objSampleScheduling.getNtzsampledate() + ","+
						// Enumeration.TransactionStatus.NA.gettransactionstatus() + ",'"
						+ " " + userInfo.getNtimezonecode() + ","
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertHistoryQuery);

				jdbcTemplate.execute("update seqnosamplescheduling set nsequenceno = " + historySeqNo
						+ " where stablename='sampleschedulinghistory' and nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

				SampleScheduling sampleSchedulingStatuss = getSampleSchedulingStatus(nsampleschedulingcode, userInfo);

				saveSampleSchedulingList.add(sampleSchedulingStatuss);
				multilingualIdList.add("IDS_PLANNEDSAMPLESCHEDULING");
				multilingualIdList.add("IDS_ADDSAMPLESCHEDULING");

				auditUtilityFunction.fnInsertAuditAction(saveSampleSchedulingList, 1, null, multilingualIdList,
						userInfo);
				return getSampleSchedulingDatas(inputMap, userInfo);
			}

		}
	}

	/**
	 * This method will access the DAO layer that is used to retrieve active
	 * sampleschedulinghistory object based on the specified ntransactionstatus and
	 * nsampleschedulingcode .
	 * 
	 * @param nsampleschedulingcode [SampleScheduling] in SampleScheduling
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         SampleScheduling object
	 * @throws Exception that are thrown in the DAO layer
	 */
	private SampleScheduling getSampleSchedulingStatus(final int nsampleschedulingcode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select sh.ntransactionstatus,s.sfromyear,s.speriod from sampleschedulinghistory sh JOIN samplescheduling s ON s.nsampleschedulingcode = sh.nsampleschedulingcode"
				+ " where sh.nsampleschedulinghistorycode= ANY (Select Max "
				+ " (sh2.nsampleschedulinghistorycode) FROM  sampleschedulinghistory sh2 "
				+ " JOIN samplescheduling s2 ON s2.nsampleschedulingcode = sh2.nsampleschedulingcode "
				+ " WHERE sh2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and s2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sh2.nsitecode = " + userInfo.getNmastersitecode() + " and s2.nsitecode= "
				+ userInfo.getNmastersitecode() + " Group By s2.nsampleschedulingcode) and sh.nsampleschedulingcode = "
				+ nsampleschedulingcode + " and sh.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sh.nsitecode = "
				+ userInfo.getNmastersitecode();
		return (SampleScheduling) jdbcUtilityFunction.queryForObject(strQuery, SampleScheduling.class, jdbcTemplate);

	}

	/**
	 * This method is responsible for creating a new SampleScheduling record by
	 * accessing the DAO layer.
	 * 
	 * @param inputMap map containing:
	 * 
	 *                 Sample Scheduling details (e.g., fromYear, toYear, status,
	 *                 etc.) UserInfo object holding logged-in user details
	 *                 nmasterSiteCode [int] representing the primary key of the
	 *                 site for which the record is to be created
	 * 
	 * @return ResponseEntity<Object> containing the status and the created Sample
	 *         Scheduling object (or error details if creation fails).
	 * @throws Exception if any error occurs in the DAO layer while creating the
	 *                   record
	 */
	@Override
	public ResponseEntity<Object> createSampleScheduling(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		SampleScheduling objSampleScheduling = objMapper.convertValue(inputMap.get("samplescheduling"),
				SampleScheduling.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

//		int fromYear = Instant.parse(objSampleScheduling.getSfromyear()).atZone(ZoneId.systemDefault()).getYear();
//		String sfromyear = String.valueOf(fromYear);

		String sfromyear = objSampleScheduling.getSfromyear();

		//Modified the select query by sonia on 15th oct 2025 for jira id:SWSM-97
		String strQuery = "SELECT  sfromyear, speriod, sdescription " + "FROM samplescheduling "
				+ "WHERE sfromyear = N'" + sfromyear + "' " + "AND speriod = '" + stringUtilityFunction.replaceQuote(objSampleScheduling.getSperiod())
				+ "'  " + "AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "AND nsitecode = " + userInfo.getNmastersitecode() + "";
		// SWSM-78 Below line commented by rukshana on oct-1-2025 as for create action
		// sampleschedulingcode will not be available
		// + "AND nsampleschedulingcode <> "+
		// objSampleScheduling.getNsampleschedulingcode();

		final SampleScheduling availableSampleScheduling = (SampleScheduling) jdbcUtilityFunction
				.queryForObject(strQuery, SampleScheduling.class, jdbcTemplate);

		if (availableSampleScheduling == null) { // modified by sujatha v 25-09-2025 modified condition (operator) issue
													// while adding sample schedule throw already exist alert
			String lockSampleScheduling = "lock table locksamplescheduling "
					+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
			jdbcTemplate.execute(lockSampleScheduling);

			final List<String> multilingualIdList = new ArrayList<>();
			final List<Object> saveSampleSchedulingList = new ArrayList<>();
			String sequenceNoQuery = "select nsequenceno from seqnosamplescheduling where stablename ='samplescheduling'"
					+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class) + 1;

			//Modified the insert query by sonia on 15th oct 2025 for jira id:SWSM-97
			String insertQuery = "INSERT INTO samplescheduling ("
					+ "nsampleschedulingcode,sfromyear, speriod, sdescription, dmodifieddate, nsitecode, nstatus) "
					+ "values(" + nsequenceNo + ", '" + sfromyear + "','" + stringUtilityFunction.replaceQuote(objSampleScheduling.getSperiod()) + "','"
					+ stringUtilityFunction.replaceQuote(objSampleScheduling.getSdescription()) + "','" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

			jdbcTemplate.execute(insertQuery);

			jdbcTemplate.execute("update seqnosamplescheduling set nsequenceno = " + nsequenceNo
					+ " where stablename='samplescheduling'");

			String historySeqQuery = "select nsequenceno from seqnosamplescheduling where stablename ='sampleschedulinghistory'"
					+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			int historySeqNo = jdbcTemplate.queryForObject(historySeqQuery, Integer.class) + 1;
//Changes timezone and offset by using common functions on oct-1-2025 - SWSM-78
			String insertHistoryQuery = "INSERT INTO sampleschedulinghistory ("
					+ "nsampleschedulinghistorycode, nsampleschedulingcode, ntransactionstatus, "
					+ "nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, "
					+ "dtransactiondate, ntztransactiondate, noffsetdtransactiondate,dmodifieddate, nsitecode, nstatus) "
					+ "VALUES (" + historySeqNo + ", " + nsequenceNo + ", "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", " + userInfo.getNusercode() + ", "
					+ userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode() + ", "
					+ userInfo.getNdeputyuserrole() + ", '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNtimezonecode() + " , "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
					// + Enumeration.TransactionStatus.NA.gettransactionstatus() + ", '"
					+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
					+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

			jdbcTemplate.execute(insertHistoryQuery);

			jdbcTemplate.execute("update seqnosamplescheduling set nsequenceno = " + historySeqNo
					+ " where stablename='sampleschedulinghistory'");

			objSampleScheduling.setSfromyear(sfromyear);
			objSampleScheduling.setNsampleschedulingcode(nsequenceNo);
			saveSampleSchedulingList.add(objSampleScheduling);

			multilingualIdList.add("IDS_ADDSAMPLESCHEDULINGPLANNED");
			auditUtilityFunction.fnInsertAuditAction(saveSampleSchedulingList, 1, null, multilingualIdList, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return getSampleSchedulingData(inputMap, userInfo);
	}

	/**
	 * This method is responsible for creating a new SampleSchedulinglocation record
	 * by accessing the DAO layer.
	 * 
	 * @param inputMap map containing:
	 * 
	 *                 Sample Scheduling details (e.g., fromYear, toYear, status,
	 *                 etc.) UserInfo object holding logged-in user details
	 *                 nmasterSiteCode [int] representing the primary key of the
	 *                 site for which the record is to be created
	 * 
	 * @return ResponseEntity<Object> containing the status and the created Sample
	 *         Scheduling object (or error details if creation fails).
	 * @throws Exception if any error occurs in the DAO layer while creating the
	 *                   record
	 */
	@Override
	public ResponseEntity<Object> createSampleSchedulingLocation(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		SampleSchedulingLocation objSampleSchedulingLocation = objMapper
				.convertValue(inputMap.get("sampleschedulinglocation"), SampleSchedulingLocation.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		final SampleScheduling sampleScheduling = getActiveSampleSchedulingById(
				objSampleSchedulingLocation.getNsampleschedulingcode(), userInfo);
		if (sampleScheduling == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		//Added By Mullai Balaji V for jira SWSM=102 not to create a location for Scheduled Records 
		if (sampleScheduling.getNtransactionstatus() == Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus()
				|| sampleScheduling.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
						.gettransactionstatus()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THERECORDCHANGEDTOSCHEDULED",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}

		final SampleSchedulingLocation sampleSchedulingLocation = getSampleSchedulingLocationById(
				objSampleSchedulingLocation, userInfo);

		if (sampleSchedulingLocation != null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		String locksampleschedulinglocation = "lock table locksampleschedulinglocation"
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(locksampleschedulinglocation);

		String query = " SELECT  MAX(nsitehierarchyconfigcode)  FROM sitehierarchyconfig";
// commented by sujatha ATE_274 SWSM-115 for Sample Requesting Plan screen -> Location is not showing.
//		Integer nsitehierarchyconfigcode = jdbcTemplate.queryForObject(query, Integer.class);

		final List<String> multilingualIdList = new ArrayList<>();
		final List<Object> saveSampleSchedulingList = new ArrayList<>();
		String sequenceNoQuery = "select nsequenceno from seqnosamplescheduling where stablename ='sampleschedulinglocation'"
				+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class) + 1;
		// added by sujatha ATE_274 27-09-2025 for added a new field ncentralsitecode
		String insertQuery = "INSERT INTO sampleschedulinglocation ("
				+ "nsampleschedulinglocationcode,nsampleschedulingcode, ncentralsitecode, nregioncode, ndistrictcode,ncitycode, "
				+ "nvillagecode,nsamplelocationcode,nsitehierarchyconfigcode,dmodifieddate,nsitecode,nstatus) "
				+ "values(" + nsequenceNo + "," + objSampleSchedulingLocation.getNsampleschedulingcode() + ","
				+ objSampleSchedulingLocation.getNcentralsitecode() + ", "
				+ objSampleSchedulingLocation.getNregioncode() + "," + objSampleSchedulingLocation.getNdistrictcode()
				+ "," + objSampleSchedulingLocation.getNcitycode() + "," + objSampleSchedulingLocation.getNvillagecode()
				+ " ," + objSampleSchedulingLocation.getNsamplelocationcode() + "," 
				//modified by sujatha ATE_274 SWSM-115 Sample Requesting Plan screen -> Location is not showing, instead of max of, taking value from front-end.
				+ objSampleSchedulingLocation.getNsitehierarchyconfigcode() + ",'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

		jdbcTemplate.execute(insertQuery);

		jdbcTemplate.execute("update seqnosamplescheduling set nsequenceno = " + nsequenceNo
				+ " where stablename='sampleschedulinglocation'");

		final SampleSchedulingLocation sampleSchedulingLocations = getAuditSampleSchedulingLocationById(nsequenceNo,
				userInfo);

		objSampleSchedulingLocation.setNsampleschedulinglocationcode(nsequenceNo);
		saveSampleSchedulingList.add(sampleSchedulingLocations);

		multilingualIdList.add("IDS_ADDSAMPLESCHEDULINGLOCATION");
		auditUtilityFunction.fnInsertAuditAction(saveSampleSchedulingList, 1, null, multilingualIdList, userInfo);
		return getAllSampleSchedulingLocation(objSampleSchedulingLocation.getNsampleschedulingcode(), userInfo);
	}

	/**
	 * This method will access the DAO layer that is used to update entry in
	 * sampleschedulinglocation table.
	 * 
	 * @param inputMap holding the date fromYear,toYear,Status
	 *                 sampleschedulinglocation details and userInfo [UserInfo]
	 *                 holding logged in user details and nmasterSiteCode [int]
	 *                 primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of updated
	 *         sampleschedulinglocation object
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateSampleSchedulingLocation(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		SampleSchedulingLocation objSampleSchedulingLocation = objMapper
				.convertValue(inputMap.get("sampleschedulinglocation"), SampleSchedulingLocation.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		final SampleScheduling sampleScheduling = getActiveSampleSchedulingById(
				objSampleSchedulingLocation.getNsampleschedulingcode(), userInfo);
		if (sampleScheduling == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final SampleSchedulingLocation sampleSchedulingLocation = getAuditSampleSchedulingLocationById(
				objSampleSchedulingLocation.getNsampleschedulinglocationcode(), userInfo); // modified by sujatha for
																							// passing wrong primary key
																							// get null
		if (sampleSchedulingLocation == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final SampleSchedulingLocation sampleSchedulingLocations = getSampleSchedulingLocationById(
				objSampleSchedulingLocation, userInfo);

		if (sampleSchedulingLocations != null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final SampleScheduling sampleSchedulingTransactionStatus = getSampleSchedulingTransaction(
				objSampleSchedulingLocation.getNsampleschedulingcode(), userInfo);
//Added By Mullai Balaji V for jira ATE_273 for update the record Only which is Draft
		if (sampleSchedulingTransactionStatus.getNtransactionstatus() == Enumeration.TransactionStatus.PLANNED
				.gettransactionstatus() ||
				sampleSchedulingTransactionStatus.getNtransactionstatus() == Enumeration.TransactionStatus.SCHEDULED
				.gettransactionstatus()
				) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		List<Object> listBeforeUpdate = new ArrayList<>();
		listBeforeUpdate.add(sampleSchedulingLocation);

		String locksampleschedulinglocation = "lock table locksampleschedulinglocation"
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(locksampleschedulinglocation);

		final List<String> multilingualIdList = new ArrayList<>();
		// added ncentralsitecode by sujatha ATE_274 for added a new field
		// ncentralsitecode
		String updateQuery = "UPDATE sampleschedulinglocation SET " + "ncentralsitecode = "
				+ objSampleSchedulingLocation.getNcentralsitecode() + ", " + "nregioncode = "
				+ objSampleSchedulingLocation.getNregioncode() + ", " + "ndistrictcode = "
				+ objSampleSchedulingLocation.getNdistrictcode() + ", " + "ncitycode = "
				+ objSampleSchedulingLocation.getNcitycode() + ", " + "nvillagecode = "
				+ objSampleSchedulingLocation.getNvillagecode() + ", " + "nsamplelocationcode = "
				+ objSampleSchedulingLocation.getNsamplelocationcode() + ", " + "dmodifieddate = '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + "nsitecode = "
				+ userInfo.getNmastersitecode() + ", " + "nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " WHERE nsampleschedulinglocationcode = "
				+ objSampleSchedulingLocation.getNsampleschedulinglocationcode() + " and nsampleschedulingcode = "
				+ objSampleSchedulingLocation.getNsampleschedulingcode();

		jdbcTemplate.execute(updateQuery);

		final SampleSchedulingLocation updatedSampleScheduling = getAuditSampleSchedulingLocationById(
				objSampleSchedulingLocation.getNsampleschedulinglocationcode(), userInfo);

		List<Object> listAfterUpdate = new ArrayList<>();
		listAfterUpdate.add(updatedSampleScheduling);

		multilingualIdList.add("IDS_EDITSAMPLESCHEDULINGLOCATION");
		auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIdList, userInfo);

		return getAllSampleSchedulingLocation(objSampleSchedulingLocation.getNsampleschedulingcode(), userInfo);
	}

	/**
	 * This method will access the DAO layer that is used to retrieve active
	 * sampleschedulinhhistory object based on the specified nsampleschedulingcode.
	 * 
	 * @param nsampleschedulingcode [int] primary key of SampleScheduling object
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         SampleScheduling object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public SampleSchedulingLocation getActiveSampleSchedulingLocationById(final int nsampleschedulingcode,
			final UserInfo userInfo) throws Exception {
		// modified by sujatha ATE_274 for added a new field ncentralsitecode
		String strQuery = "SELECT " + " ssl.nsampleschedulinglocationcode, " + " ssl.nsampleschedulingcode, "
				+ " ssl.ncentralsitecode, central.ssitename as scentralsitename, "
				+ " ssl.nregioncode, region.ssitename AS sregionname, "
				+ " ssl.ndistrictcode, district.ssitename AS sdistrictname, "
				+ " ssl.ncitycode, city.ssitename AS scityname, "
				+ " ssl.nvillagecode, village.svillagename AS svillagename, location.ssamplelocationname,location.nsamplelocationcode,"
				+ " ssl.dmodifieddate, " + " ssl.nstatus " + "FROM sampleschedulinglocation ssl "
				+ "LEFT JOIN site central ON ssl.ncentralsitecode = central.nsitecode "
				+ "LEFT JOIN site region   ON ssl.nregioncode   = region.nsitecode "
				+ "LEFT JOIN site district ON ssl.ndistrictcode = district.nsitecode "
				+ "LEFT JOIN site city     ON ssl.ncitycode     = city.nsitecode "
				+ "LEFT JOIN villages village  ON ssl.nvillagecode  = village.nvillagecode "
				+ "LEFT JOIN samplelocation location ON ssl.nsamplelocationcode = location.nsamplelocationcode "
				+ "WHERE ssl.nsampleschedulingcode = " + nsampleschedulingcode + " AND ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ssl.nsitecode = "
				+ userInfo.getNmastersitecode();

		return (SampleSchedulingLocation) jdbcUtilityFunction.queryForObject(strQuery, SampleSchedulingLocation.class,
				jdbcTemplate);

	}

	/**
	 * This method is used to make entry the data in the audit for
	 * sampleSchedulingLocation
	 * 
	 * @param nsampleschedulinglocationcode [int] primary key of SampleScheduling
	 *                                      object
	 * @param userInfo                      [UserInfo] holding logged in user
	 *                                      details based on which the list is to be
	 *                                      fetched
	 * @return response entity object holding response status and data of
	 *         SampleScheduling object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public SampleSchedulingLocation getAuditSampleSchedulingLocationById(final int nsampleschedulinglocationcode,
			final UserInfo userInfo) throws Exception {

		// modified by sujatha ATE_274 for added a new field ncentralsitecode
		String strQuery = "SELECT " + " ssl.nsampleschedulinglocationcode, " + " ssl.nsampleschedulingcode, "
				+ " ssl.ncentralsitecode, central.ssitename AS scentralsitename, "
				+ " ssl.nregioncode, region.ssitename AS sregionname, "
				+ " ssl.ndistrictcode, district.ssitename AS sdistrictname, "
				+ " ssl.ncitycode, city.ssitename AS scityname, "
				+ " ssl.nvillagecode, village.svillagename AS svillagename, location.ssamplelocationname,location.nsamplelocationcode,"
				+ " ssl.dmodifieddate, " + " ssl.nstatus " + "FROM sampleschedulinglocation ssl "
				+ "LEFT JOIN site central ON ssl.ncentralsitecode = central.nsitecode "
				+ "LEFT JOIN site region   ON ssl.nregioncode   = region.nsitecode "
				+ "LEFT JOIN site district ON ssl.ndistrictcode = district.nsitecode "
				+ "LEFT JOIN site city     ON ssl.ncitycode     = city.nsitecode "
				+ "LEFT JOIN villages village  ON ssl.nvillagecode  = village.nvillagecode "
				+ "LEFT JOIN samplelocation location ON ssl.nsamplelocationcode = location.nsamplelocationcode "
				+ "WHERE ssl.nsampleschedulinglocationcode = " + nsampleschedulinglocationcode + " AND ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ssl.nsitecode = "
				+ userInfo.getNmastersitecode();

		return (SampleSchedulingLocation) jdbcUtilityFunction.queryForObject(strQuery, SampleSchedulingLocation.class,
				jdbcTemplate);

	}

	/**
	 * This method will access the DAO layer that is used to retrieve active
	 * sampleschedulinhhistory object based on the specified nsampleschedulingcode.
	 * 
	 * @param objSampleSchedulingLocation SampleSchedulingLocation object
	 * @param userInfo                    [UserInfo] holding logged in user details
	 *                                    based on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         SampleScheduling object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public SampleSchedulingLocation getSampleSchedulingLocationById(
			final SampleSchedulingLocation objSampleSchedulingLocation, final UserInfo userInfo) throws Exception {

		// added by sujatha ATE_274 for added a new field ncentralsitecode
		// modified by sujatha an issue while saving location already exit is throws's
		// incorrectly for that added one more condition to check nsamplelocationcode
		String strQuery = "SELECT " + "ssl.nsampleschedulinglocationcode, " + "ssl.nsampleschedulingcode, "
				+ "ssl.ncentralsitecode, central.ssitename AS scentralsitename, "
				+ "ssl.nregioncode, region.ssitename AS sregionname, "
				+ "ssl.ndistrictcode, district.ssitename AS sdistrictname, "
				+ "ssl.ncitycode, city.ssitename AS scityname, "
				+ "ssl.nvillagecode, village.svillagename AS svillagename, "
				+ "location.ssamplelocationname, location.nsamplelocationcode, " + "ssl.dmodifieddate, ssl.nstatus "
				+ "FROM sampleschedulinglocation ssl "
				+ "LEFT JOIN site central ON ssl.ncentralsitecode = central.nsitecode "
				+ "LEFT JOIN site region   ON ssl.nregioncode   = region.nsitecode "
				+ "LEFT JOIN site district ON ssl.ndistrictcode = district.nsitecode "
				+ "LEFT JOIN site city     ON ssl.ncitycode     = city.nsitecode "
				+ "LEFT JOIN villages village  ON ssl.nvillagecode  = village.nvillagecode "
				+ "LEFT JOIN samplelocation location ON ssl.nsamplelocationcode = location.nsamplelocationcode "
				+ "WHERE ssl.nsampleschedulingcode = " + objSampleSchedulingLocation.getNsampleschedulingcode()
				+ " AND ssl.ncentralsitecode = " + objSampleSchedulingLocation.getNcentralsitecode()
				+ " AND ssl.nregioncode = " + objSampleSchedulingLocation.getNregioncode() + " AND ssl.ndistrictcode = "
				+ objSampleSchedulingLocation.getNdistrictcode() + " AND ssl.ncitycode = "
				+ objSampleSchedulingLocation.getNcitycode() + " AND ssl.nvillagecode = "
				+ objSampleSchedulingLocation.getNvillagecode() + " AND ssl.nsamplelocationcode="
				+ objSampleSchedulingLocation.getNsamplelocationcode() + " AND ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ssl.nsitecode = "
				+ userInfo.getNmastersitecode();

		// If updating an existing record, exclude it from results
		if (objSampleSchedulingLocation.getNsampleschedulinglocationcode() > 0) {
			strQuery += " AND ssl.nsampleschedulinglocationcode <> "
					+ objSampleSchedulingLocation.getNsampleschedulinglocationcode();
		}

		// Execute the query
		return (SampleSchedulingLocation) jdbcUtilityFunction.queryForObject(strQuery, SampleSchedulingLocation.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to retrieve list of all available samplescheduling for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         samplescheduling
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getSampleScheduling(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		outputMap.putAll(filterStatus(inputMap, userInfo));
		outputMap.putAll((Map<String, Object>) getSampleSchedulingData(inputMap, userInfo).getBody());
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all available samplescheduling for
	 * the specified site.and based on Status
	 * 
	 * * @param inputMap a map containing filtering criteria such as fromYear,
	 * toYear, and status
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         samplescheduling
	 * @throws Exception that are thrown from this DAO layer
	 */
	public Map<String, Object> filterStatus(final Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> map = new HashMap<>();

		final String StrQuery = "SELECT ntranscode, coalesce(jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus  " + " FROM transactionstatus"
				+ " WHERE ntranscode IN (" + Enumeration.TransactionStatus.ALL.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.PLANNED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.SCHEDULED.gettransactionstatus() + " ) and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ntranscode asc ";
		final List<TransactionStatus> lstTransactionStatus = (List<TransactionStatus>) jdbcTemplate.query(StrQuery,
				new TransactionStatus());
		if (lstTransactionStatus.size() > 0) {
			map.put("transactionStatus", lstTransactionStatus);
			map.put("defaultTransactionStatus", lstTransactionStatus.get(0));
			map.put("realStatus", lstTransactionStatus.get(0));
			inputMap.put("ntransactionstatus", lstTransactionStatus.get(0).getNtranscode());
		} else {
			map.put("transactionStatus", null);
			map.put("defaultTransactionStatus", null);
			map.put("realStatus", null);
		}
		return map;

	}

	/**
	 * This method provides access to the DAO layer to retrieve all available
	 * samplescheduling for a specific site, filtered by date range and status.
	 *
	 * @param inputMap a map containing filter criteria such as fromYear, toYear,
	 *                 and status
	 * @param userInfo an instance of [UserInfo] containing the logged-in user
	 *                 details and the nmasterSiteCode, which is the primary key of
	 *                 the site for which the complaints are to be fetched
	 *
	 * @return a ResponseEntity containing the list of Sample Scheduling records
	 *         associated with the specified site
	 *
	 * @throws Exception if any errors occur in the DAO layer
	 */

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getSampleSchedulingData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		// final String operation = (String) inputMap.get("operation");

		final Map<String, Object> outputMap = new LinkedHashMap<>();

		int fromYear;
		int toYear;

		if (inputMap.get("fromYear") != null && inputMap.get("toYear") != null) {
			fromYear = (int) inputMap.get("fromYear");
			toYear = (int) inputMap.get("toYear");
		} else {
			String currentUIDate = (String) inputMap.get("currentdate");
			if (currentUIDate != null && !currentUIDate.trim().isEmpty()) {
				int currentYear = Year.now().getValue();
				fromYear = currentYear;
				toYear = currentYear;
			} else {
				int currentYear = Year.now().getValue();
				fromYear = currentYear;
				toYear = currentYear;
			}
		}

		outputMap.put("fromYear", fromYear);
		outputMap.put("toYear", toYear);
		int ntransactionstatus = 0;
		if (inputMap.containsKey("ntranscode")) {
			ntransactionstatus = (int) inputMap.get("ntranscode");
		}

		String filterStatusQuery = "";
		if (ntransactionstatus > 0) {
			filterStatusQuery = " AND sh.ntransactionstatus = " + ntransactionstatus + " ";
		}

//		if(operation=="create")
//		{
		final String strQuery = "SELECT s.nsampleschedulingcode, s.sfromyear, s.speriod, s.sdescription, "
				+ " s.nsitecode, s.nstatus, sh.ntransactionstatus, "
				+ " COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus,f.ncolorcode,cl.scolorhexcode "
				+ "FROM samplescheduling s "
				+ "JOIN sampleschedulinghistory sh ON sh.nsampleschedulingcode = s.nsampleschedulingcode "
				+ "JOIN transactionstatus ts1 ON sh.ntransactionstatus = ts1.ntranscode "
				+ "JOIN formwisestatuscolor f ON sh.ntransactionstatus = f.ntranscode "
				+ "JOIN colormaster cl ON cl.ncolorcode = f.ncolorcode "
				+ "WHERE sh.nsampleschedulinghistorycode = ANY ( " + "   SELECT MAX(sh2.nsampleschedulinghistorycode) "
				+ "   FROM sampleschedulinghistory sh2 "
				+ "   JOIN samplescheduling s2 ON s2.nsampleschedulingcode = sh2.nsampleschedulingcode "
				+ "   WHERE s2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "   AND sh2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "   AND s2.nsitecode = " + userInfo.getNmastersitecode() + " " + "   AND sh2.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + "   GROUP BY s2.nsampleschedulingcode " + ") "
				+ "AND s.sfromyear::int BETWEEN " + fromYear + " AND " + toYear + " "
				+ "AND s.nsampleschedulingcode > 0 " + "AND s.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ "AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "AND sh.nsitecode = " + userInfo.getNmastersitecode() + " " + "AND sh.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND ts1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND f.nformcode="
				+ userInfo.getNformcode() + " " + filterStatusQuery + "ORDER BY s.nsampleschedulingcode DESC";

		LOGGER.info("Get SampleScheduling Query: " + strQuery);
		final List<SampleScheduling> lstSampleScheduling = (List<SampleScheduling>) jdbcTemplate.query(strQuery,
				new SampleScheduling());
		if (!lstSampleScheduling.isEmpty()) {
			outputMap.put("sampleSchedulingRecord", lstSampleScheduling);
			outputMap.put("selectedSampleScheduling", lstSampleScheduling.get(0));

			outputMap.putAll(
					(Map<String, Object>) getSampleSchedulingFile(lstSampleScheduling.get(0).getNsampleschedulingcode(),
							userInfo).getBody());

			outputMap.putAll((Map<String, Object>) getAllSampleSchedulingLocation(
					lstSampleScheduling.get(0).getNsampleschedulingcode(), userInfo).getBody());

		} else {
			outputMap.put("sampleSchedulingRecord", null);
			outputMap.put("selectedSampleScheduling", null);
			outputMap.put("sampleSchedulingFile", null);
			outputMap.put("sampleSchedulingLocation", null);
		}
//		}
//		else
//		{
//			ObjectMapper objMapper = new ObjectMapper();
//			objMapper.registerModule(new JavaTimeModule());
//			SampleScheduling objSampleScheduling = objMapper.convertValue(inputMap.get("samplescheduling"),
//					SampleScheduling.class);
//			final String strQuery = "SELECT s.nsampleschedulingcode, s.sfromyear, s.speriod, s.sdescription, "
//					+ " s.nsitecode, s.nstatus, sh.ntransactionstatus, "
//					+ " COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
//					+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus,f.ncolorcode,cl.scolorhexcode "
//					+ "FROM samplescheduling s "
//					+ "JOIN sampleschedulinghistory sh ON sh.nsampleschedulingcode = s.nsampleschedulingcode "
//					+ "JOIN transactionstatus ts1 ON sh.ntransactionstatus = ts1.ntranscode "
//					+ "JOIN formwisestatuscolor f ON sh.ntransactionstatus = f.ntranscode "
//					+ "JOIN colormaster cl ON cl.ncolorcode = f.ncolorcode "
//					+ "WHERE sh.nsampleschedulinghistorycode = ANY ( " + "   SELECT MAX(sh2.nsampleschedulinghistorycode) "
//					+ "   FROM sampleschedulinghistory sh2 "
//					+ "   JOIN samplescheduling s2 ON s2.nsampleschedulingcode = sh2.nsampleschedulingcode "
//					+ "   WHERE s2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//					+ "   AND sh2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//					+ "   AND s2.nsitecode = " + userInfo.getNmastersitecode() + " " + "   AND sh2.nsitecode = "
//					+ userInfo.getNmastersitecode() + " " + "   GROUP BY s2.nsampleschedulingcode " + ") "
//					+ "AND s.nsitecode = " + userInfo.getNmastersitecode() + " " + "AND s.nsampleschedulingcode = "
//					+ objSampleScheduling.getNsampleschedulingcode() + " " + "AND s.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND sh.nsitecode = "
//					+ userInfo.getNmastersitecode() + " " + "AND sh.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND ts1.nstatus = "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND f.nformcode="
//					+ userInfo.getNformcode() + " " + "ORDER BY s.nsampleschedulingcode DESC";
//
//			LOGGER.info("Get SampleScheduling Query: " + strQuery);
//
//			final List<SampleScheduling> lstSampleScheduling = (List<SampleScheduling>) jdbcTemplate.query(strQuery,
//					new SampleScheduling());
//
//			if (!lstSampleScheduling.isEmpty()) {
//				outputMap.put("selectedSampleScheduling", lstSampleScheduling.get(0));
//			//	outputMap.putAll((Map<String, Object>)SampleSchedulingData(inputMap,userInfo));
//			
//			//	outputMap.put("sampleSchedulingRecord",lstSampleScheduling);
//				
////				ResponseEntity<Object> response = SampleSchedulingData(inputMap, userInfo);
////				outputMap.putAll((Map<String, Object>) response.getBody());
//				
//				outputMap.putAll(
//						(Map<String, Object>) getSampleSchedulingFile(lstSampleScheduling.get(0).getNsampleschedulingcode(),
//								userInfo).getBody());
//				outputMap.putAll((Map<String, Object>) getAllSampleSchedulingLocation(
//						lstSampleScheduling.get(0).getNsampleschedulingcode(), userInfo).getBody());
//			} else {
//				outputMap.put("sampleSchedulingRecord", null);
//				outputMap.put("selectedSampleScheduling", null);
//				outputMap.put("sampleSchedulingFile", null);
//				outputMap.put("sampleSchedulingLocation", null);
//			}
//			
//			
//			
//			
//		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method will access the DAO layer that is used to retrieve active
	 * sampleschedulinghistory object based on the specified nsampleschedulingcode.
	 * 
	 * @param nsampleschedulingcode [int] primary key of SampleScheduling object
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         SampleScheduling object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public SampleScheduling getActiveSampleSchedulingById(final int nsampleschedulingcode, final UserInfo userInfo)
			throws Exception {

		//Added new field ntransactionstatus By Mullai Balaji V  ATE_273 for jira swsm-102  
		final String query = "SELECT s.*, sh.ntransactionstatus " + "FROM samplescheduling s "
				+ "JOIN sampleschedulinghistory sh " + "  ON sh.nsampleschedulingcode = s.nsampleschedulingcode "
				+ "WHERE s.nsampleschedulingcode = " + nsampleschedulingcode + "  AND s.nsitecode = "
				+ userInfo.getNmastersitecode() + "  AND s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND sh.nsampleschedulinghistorycode = (" + " SELECT MAX(sh2.nsampleschedulinghistorycode) "
				+ " FROM sampleschedulinghistory sh2 " + " WHERE sh2.nsampleschedulingcode = s.nsampleschedulingcode"
				+ ")";

		return (SampleScheduling) jdbcUtilityFunction.queryForObject(query, SampleScheduling.class, jdbcTemplate);
	}

	public SampleScheduling getSampleSchedulingTransaction(final int nsampleschedulingcode, final UserInfo userInfo)
			throws Exception {

		final String query = "SELECT * " + "FROM sampleschedulinghistory h " + "WHERE h.nsampleschedulingcode =  "
				+ nsampleschedulingcode + "  AND h.nsitecode = " + userInfo.getNmastersitecode() + "  AND h.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND h.ntransactionstatus = ("
				+ " SELECT MAX(h2.ntransactionstatus) " + " FROM sampleschedulinghistory h2 "
				+ " WHERE h2.nsampleschedulingcode = h.nsampleschedulingcode " + " AND h2.nsitecode = h.nsitecode "
				+ " AND h2.nstatus = h.nstatus" + ");";

		return (SampleScheduling) jdbcUtilityFunction.queryForObject(query, SampleScheduling.class, jdbcTemplate);
	}

	/**
	 * This method will access the DAO layer that is used to update entry in
	 * samplescheduling table.
	 * 
	 * @param inputMap holding the date fromYear,toYear,Status samplescheduling
	 *                 details and userInfo [UserInfo] holding logged in user
	 *                 details and nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and data of updated
	 *         samplescheduling object
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateSampleScheduling(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		SampleScheduling objSampleScheduling = objMapper.convertValue(inputMap.get("samplescheduling"),
				SampleScheduling.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

//		int fromYear = Instant.parse(objSampleScheduling.getSfromyear()).atZone(ZoneId.systemDefault()).getYear();
//		String sfromyear = String.valueOf(fromYear);

		String sfromyear = objSampleScheduling.getSfromyear();

		final SampleScheduling sampleScheduling = getActiveSampleSchedulingById(
				objSampleScheduling.getNsampleschedulingcode(), userInfo);
		if (sampleScheduling == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		//Added by Mullai Balaji.V ATE_273 for jira SWSM-102 only update the Record which is Draft 
		if(sampleScheduling.getNtransactionstatus()!=Enumeration.TransactionStatus.DRAFT.gettransactionstatus())
		{
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);			
		}
		

		//Modified the select query by sonia on 15th oct 2025 for jira id:SWSM-97
		final String queryString = "SELECT nsampleschedulingcode FROM samplescheduling " + "WHERE sfromyear = N'"
				+ sfromyear + "' AND speriod =N'" + stringUtilityFunction.replaceQuote(objSampleScheduling.getSperiod()) + "' AND nsampleschedulingcode <> "
				+ objSampleScheduling.getNsampleschedulingcode() + " AND nsitecode = "
				+ objSampleScheduling.getNsitecode() + " AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final SampleScheduling availableSampleScheduling = (SampleScheduling) jdbcUtilityFunction
				.queryForObject(queryString, SampleScheduling.class, jdbcTemplate);

		if (availableSampleScheduling != null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

		List<Object> listBeforeUpdate = new ArrayList<>();
		listBeforeUpdate.add(sampleScheduling);

		final String updateQuery = "UPDATE samplescheduling SET " + "sfromyear = N'" + sfromyear + "', "
				+ "speriod = N'" + stringUtilityFunction.replaceQuote(objSampleScheduling.getSperiod()) + "', "
				+ "sdescription = N'" + stringUtilityFunction.replaceQuote(objSampleScheduling.getSdescription())
				+ "', " + "dmodifieddate = '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "' "
				+ "WHERE nsampleschedulingcode = " + objSampleScheduling.getNsampleschedulingcode() + " and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode = "
				+ userInfo.getNmastersitecode();
		jdbcTemplate.execute(updateQuery);

		String updateHistoryQuery = "UPDATE sampleschedulinghistory SET dtransactiondate ='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nsampleschedulingcode = "
				+ objSampleScheduling.getNsampleschedulingcode();

		jdbcTemplate.execute(updateHistoryQuery);

		final SampleScheduling updatedSampleScheduling = getActiveSampleSchedulingById(
				objSampleScheduling.getNsampleschedulingcode(), userInfo);
		List<Object> listAfterUpdate = new ArrayList<>();
		listAfterUpdate.add(updatedSampleScheduling);
		List<String> multilingualIDList = new ArrayList<>();
		multilingualIDList.add("IDS_EDITSAMPLESCHEDULING");
		auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo);

		return getSampleSchedulingDatas(inputMap, userInfo);
	}

	/**
	 * This method will access the DAO layer that is used to retrieve active
	 * sampleschedulinghistory object based on the specified nsampleschedulingcode.
	 * 
	 * @param nsampleschedulingcode [int] primary key of SampleScheduling object
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         SampleScheduling object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<Object> getSampleSchedulingRecord(final int nsampleschedulingcode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final SampleScheduling sampleScheduling = getActiveSampleSchedulingById(nsampleschedulingcode, userInfo);
		if (sampleScheduling == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		// added by Mullai balaji V for JIRA-SWSM-102 
//		else if (sampleScheduling.getNtransactionstatus() == Enumeration.TransactionStatus.SCHEDULED
//				.gettransactionstatus()
//				|| sampleScheduling.getNtransactionstatus() == Enumeration.TransactionStatus.COMPLETED
//						.gettransactionstatus()) {
//			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_THERECORDCHANGEDTOSCHEDULED",
//					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
//		} 

		else {

			final String strQuery = "SELECT s.nsampleschedulingcode, s.sfromyear, s.speriod, s.sdescription, "
					+ " s.nsitecode, s.nstatus, sh.ntransactionstatus, "
					+ " COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
					+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus,f.ncolorcode,cl.scolorhexcode  "
					+ "FROM samplescheduling s JOIN sampleschedulinghistory sh ON sh.nsampleschedulingcode = s.nsampleschedulingcode "
					+ "JOIN transactionstatus ts1 ON sh.ntransactionstatus = ts1.ntranscode "
					+ "JOIN formwisestatuscolor f ON sh.ntransactionstatus = f.ntranscode "
					+ "JOIN colormaster cl ON cl.ncolorcode = f.ncolorcode "
					+ "WHERE sh.nsampleschedulinghistorycode = ANY ( "
					+ " SELECT MAX(sh2.nsampleschedulinghistorycode) " + " FROM sampleschedulinghistory sh2 "
					+ " JOIN samplescheduling s2 ON s2.nsampleschedulingcode = sh2.nsampleschedulingcode "
					+ " WHERE s2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " AND sh2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ " AND s2.nsitecode = " + userInfo.getNmastersitecode() + " " + "      AND sh2.nsitecode = "
					+ userInfo.getNmastersitecode() + " " + " GROUP BY s2.nsampleschedulingcode " + ") "
					+ "AND s.nsampleschedulingcode > 0 " + "AND s.nsitecode = " + userInfo.getNmastersitecode() + " "
					+ "AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "AND sh.nsitecode = " + userInfo.getNmastersitecode() + " " + "AND sh.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND ts1.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND s.nsampleschedulingcode= "
					+ nsampleschedulingcode + " " + "AND f.nformcode = " + userInfo.getNformcode() + " ORDER BY 1 DESC";

			LOGGER.info("Get Method" + strQuery);

			final List<SampleScheduling> lstSampleScheduling = (List<SampleScheduling>) jdbcTemplate.query(strQuery,
					new SampleScheduling());

			if (!lstSampleScheduling.isEmpty()) {
				outputMap.put("sampleSchedulingRecord", lstSampleScheduling);
				outputMap.put("selectedSampleScheduling", lstSampleScheduling.get(0));
				outputMap.putAll((Map<String, Object>) getSampleSchedulingFile(
						lstSampleScheduling.get(0).getNsampleschedulingcode(), userInfo).getBody());
				outputMap.putAll((Map<String, Object>) getAllSampleSchedulingLocation(
						lstSampleScheduling.get(0).getNsampleschedulingcode(), userInfo).getBody());

			} else {
				outputMap.put("sampleSchedulingRecord", null);
				outputMap.put("selectedSampleScheduling", null);
			}

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	/**
	 * This method will access the DAO layer that is used to delete an entry in
	 * samplescheduling table.
	 * 
	 * @param inputMap holding the date fromYear,toYear,Status samplescheduling
	 *                 details and userInfo [UserInfo] holding logged in user
	 *                 details and nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         samplescheduling object
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteSampleScheduling(Map<String, Object> inputMap) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		SampleScheduling objSampleScheduling = objMapper.convertValue(inputMap.get("samplescheduling"),
				SampleScheduling.class);
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);

		// if (!"Planned".equals(objSampleScheduling.getStransdisplaystatus())) {
//		if (!String.valueOf(Enumeration.TransactionStatus.PLANNED.gettransactionstatus())
//				.equals(objSampleScheduling.getStransdisplaystatus())) 

		if (objSampleScheduling.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			final SampleScheduling sampleScheduling = getActiveSampleSchedulingById(
					objSampleScheduling.getNsampleschedulingcode(), userInfo);
			if (sampleScheduling == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
			//Added By Mullai Balaji ATE_273 for SWSM-102 only delete draft Record 
			
			if(sampleScheduling.getNtransactionstatus()!=Enumeration.TransactionStatus.DRAFT.gettransactionstatus())
			{
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORDTODELETE",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			
			
			final List<Object> deletedSampleScheduling = new ArrayList<>();
			final List<String> multilingualIdList = new ArrayList<>();

			String updateQueryString = "UPDATE samplescheduling SET dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " WHERE nsampleschedulingcode="
					+ objSampleScheduling.getNsampleschedulingcode() + ";";
			jdbcTemplate.execute(updateQueryString);

			updateQueryString = "UPDATE sampleschedulinghistory SET nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dtransactiondate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' WHERE nsampleschedulingcode="
					+ objSampleScheduling.getNsampleschedulingcode() + ";";
			jdbcTemplate.execute(updateQueryString);

			// SWSM-78 added by rukshana on oct-1-2025 as child tables not deleted i have
			// deleted child table

			String sSampleSchedulingQuery = "select * from sampleschedulinglocation where  nsampleschedulingcode= "
					+ objSampleScheduling.getNsampleschedulingcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and nsitecode = "
					+ userInfo.getNmastersitecode();

			final List<SampleSchedulingLocation> lstSampleLocation = (List<SampleSchedulingLocation>) jdbcTemplate
					.query(sSampleSchedulingQuery, new SampleSchedulingLocation());

			if (lstSampleLocation.size() > 0) {

				String updateQueryStringSampleLocation = "UPDATE sampleschedulinglocation SET dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " WHERE nsampleschedulingcode="
						+ objSampleScheduling.getNsampleschedulingcode() + " and nsitecode = "
						+ userInfo.getNmastersitecode();

				jdbcTemplate.execute(updateQueryStringSampleLocation);

			}

			final String sQuery = "select * from sampleschedulingfile where" + " nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsampleschedulingcode="
					+ objSampleScheduling.getNsampleschedulingcode() + "" + " and nsitecode = "
					+ userInfo.getNmastersitecode();

//			final SampleSchedulingFile objTF = (SampleSchedulingFile) jdbcUtilityFunction.queryForObject(sQuery,
//					SampleSchedulingFile.class, jdbcTemplate);
			final List<SampleSchedulingFile> lstvalidate = (List<SampleSchedulingFile>) jdbcTemplate.query(sQuery,
					new SampleSchedulingFile());
			if (lstvalidate.size() > 0) {

				final String sUpdateQuerySampleFile = "update sampleschedulingfile set" + "  dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ", nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ""
						+ " WHERE nsampleschedulingcode=" + objSampleScheduling.getNsampleschedulingcode() + "";
				jdbcTemplate.execute(sUpdateQuerySampleFile);

			}
			objSampleScheduling.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			deletedSampleScheduling.add(objSampleScheduling);
			multilingualIdList.add("IDS_DELETESAMPLESCHEDULING");
			auditUtilityFunction.fnInsertAuditAction(deletedSampleScheduling, 1, null, multilingualIdList, userInfo);
			return getSampleSchedulingData(inputMap, userInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORDTODELETE",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method will access the DAO layer that is used to create the records in
	 * sampleschedulingfile
	 * 
	 * @param request  holding the date for upload the file in sampleschedulingfile
	 *                 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of sampleschedulingfile
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> createSampleSchedulingFile(MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final String sQuery = " lock  table locksampleschedulingfile"
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<SampleSchedulingFile> lstReqSampleSchedulingFile = objMapper.readValue(
				request.getParameter("sampleSchedulingFile"), new TypeReference<List<SampleSchedulingFile>>() {
				});
		if (lstReqSampleSchedulingFile != null && lstReqSampleSchedulingFile.size() > 0) {
			final SampleSchedulingHistory objSampleSchedulingHistory = checKSampleSchedulingIsPresent(
					lstReqSampleSchedulingFile.get(0).getNsampleschedulingcode(), objUserInfo);
			if (objSampleSchedulingHistory != null) {
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (lstReqSampleSchedulingFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP
						.gettype()) {
					sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
				}
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo)
							.truncatedTo(ChronoUnit.SECONDS);
					final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
					final int noffset = dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid());
					lstReqSampleSchedulingFile.forEach(objtf -> {
						objtf.setDcreateddate(instantDate);
						if (objtf.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							objtf.setDcreateddate(instantDate);
							objtf.setNoffsetdcreateddate(noffset);
							objtf.setScreateddate(sattachmentDate.replace("T", " "));
						}
					});

					String sequencequery = "select nsequenceno from seqnosamplescheduling where stablename ='sampleschedulingfile'  "
							+ "  and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					int nsequenceno = (int) jdbcUtilityFunction.queryForObject(sequencequery, Integer.class,
							jdbcTemplate);
					nsequenceno++;
					String insertquery = "Insert into sampleschedulingfile(nsampleschedulingfilecode,nsampleschedulingcode,nlinkcode,nattachmenttypecode,sfilename,sdescription,nfilesize,dcreateddate,noffsetdcreateddate,ntzcreateddate,ssystemfilename,dmodifieddate,nsitecode,nstatus)"
							+ "values (" + nsequenceno + ","
							+ lstReqSampleSchedulingFile.get(0).getNsampleschedulingcode() + ","
							+ lstReqSampleSchedulingFile.get(0).getNlinkcode() + ","
							+ lstReqSampleSchedulingFile.get(0).getNattachmenttypecode() + "," + " N'"
							+ stringUtilityFunction.replaceQuote(lstReqSampleSchedulingFile.get(0).getSfilename())
							+ "',N'"
							+ stringUtilityFunction.replaceQuote(lstReqSampleSchedulingFile.get(0).getSdescription())
							+ "'," + lstReqSampleSchedulingFile.get(0).getNfilesize() + "," + " '"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ lstReqSampleSchedulingFile.get(0).getNoffsetdcreateddate() + ","
							+ objUserInfo.getNtimezonecode() + ",N'"
							+ lstReqSampleSchedulingFile.get(0).getSsystemfilename() + "','"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', "
							+ objUserInfo.getNmastersitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(insertquery);
					String updatequery = "update seqnosamplescheduling set nsequenceno =" + nsequenceno
							+ " where stablename ='sampleschedulingfile'" + "  and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

					jdbcTemplate.execute(updatequery);
					final List<String> multilingualIDList = new ArrayList<>();
					multilingualIDList.add(
							lstReqSampleSchedulingFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP
									.gettype() ? "IDS_ADDSAMPLESCHEDULINGFILE" : "IDS_ADDSAMPLESCHEDULINGLINK");
					final List<Object> listObject = new ArrayList<Object>();
					String auditqry = "select * from sampleschedulingfile where nsampleschedulingcode = "
							+ lstReqSampleSchedulingFile.get(0).getNsampleschedulingcode() + " and nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsampleschedulingfilecode = " + nsequenceno + " and nsitecode = "
							+ objUserInfo.getNmastersitecode();
					final List<SampleSchedulingFile> lstvalidate = (List<SampleSchedulingFile>) jdbcTemplate
							.query(auditqry, new SampleSchedulingFile());
					listObject.add(lstvalidate);
					auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESCHEDULINGALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			return (getSampleSchedulingFile(lstReqSampleSchedulingFile.get(0).getNsampleschedulingcode(), objUserInfo));
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method will access the DAO layer that is used to check the active
	 * sampleschedulingfile object based on the specified nsampleschedulingcode.
	 * 
	 * @param nsampleschedulingcode [int] primary key of SampleScheduling object
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         SampleScheduling object
	 * @throws Exception that are thrown in the DAO layer
	 */

	public SampleSchedulingHistory checKSampleSchedulingIsPresent(final int nsampleschedulingcode,
			final UserInfo objUserInfo) throws Exception {
		String strQuery = "select nsampleschedulingcode from samplescheduling where" + " nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsampleschedulingcode = "
				+ nsampleschedulingcode + " and nsitecode = " + objUserInfo.getNmastersitecode();

		SampleSchedulingHistory objSampleSchedulingHistory = (SampleSchedulingHistory) jdbcUtilityFunction
				.queryForObject(strQuery, SampleSchedulingHistory.class, jdbcTemplate);

		return objSampleSchedulingHistory;
	}

	/**
	 * This method will access the DAO layer that is used to get all the available
	 * Village records with respect to City.
	 *
	 * @param nprimarykey Holding the City record (foreign key reference for village
	 *                    lookup)
	 * @param userInfo    [UserInfo] holding logged-in user details including
	 *                    nmasterSiteCode [int], which is the primary key of the
	 *                    site object for which the list is to be fetched
	 * @return a response entity which holds the list of village records with
	 *         respect to the given city
	 * @throws Exception if any error occurs in the DAO layer
	 */
	//modified by sujatha ATE_274 SWSM-117 by adding nsitehierarchyconfigcode for getting the approved site hierarchy config villages
	@Override
	public ResponseEntity<Object> getVillage(final int nprimarykey, final UserInfo objUserInfo, final int nsitehierarchyconfigcode) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		// modified by sujatha ATE_274 for getting duplicate villages to fix that put
		// distinct in the below query
//		String strQuery = "select distinct(sl.nvillagecode),v.svillagename from samplelocation sl "
//				+ " JOIN villages v ON v.nvillagecode=sl.nvillagecode " + " where sl.ncitycode=" + nprimarykey
//				+ " and sl.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		// added by sujatha ATE_274 01-10-2025 for fixing the issue of getting the
		// duplicate villages in the add drop down
		String strQuery = "select sl.nvillagecode, v.svillagename from samplelocation sl "
				+ "JOIN villages v ON v.nvillagecode = sl.nvillagecode " + "where sl.ncitycode = " + nprimarykey
				+ " and sl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				//added by sujatha ATE_274 SWSM-117  nsitehierarchyconfigcode for getting the approved site hierarchy config villages
				+ " and v.nsitehierarchyconfigcode="+nsitehierarchyconfigcode
				+ " group by sl.nvillagecode, v.svillagename";

//		ObjectMapper objMapper = new ObjectMapper();
//		objMapper.registerModule(new JavaTimeModule());

		final List<SampleSchedulingLocation> villageList = jdbcTemplate.query(strQuery, new SampleSchedulingLocation());

		outputMap.put("villageList", villageList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve list of all available
	 * sampleschedulinglocation for the specified site.
	 * 
	 * @param nsampleschedulingcode for get the location based on the
	 *                              samplescheduling table
	 * @param userInfo              [UserInfo] holding logged in user details and
	 *                              nmasterSiteCode [int] primary key of site object
	 *                              for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         sampleschedulinglocation
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleSchedulingLocation(final int nsampleschedulinglocationcode,
			final UserInfo objUserInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		// added field by sujatha ATE_274 for added a new field ncentralsitecode
		//added new ntransactionstatus by Mullai Balaji V ATR_273 for JIRA SWSM-102
		String strQuery = "SELECT " + " ssl.nsampleschedulinglocationcode, " + " ssl.nsampleschedulingcode, "
				+ " ssl.ncentralsitecode, central.ssitename AS scentralsitename, "
				+ " ssl.nregioncode, region.ssitename AS sregionname, "
				+ " ssl.ndistrictcode, district.ssitename AS sdistrictname, "
				+ " ssl.ncitycode, city.ssitename AS scityname, "
				+ " ssl.nvillagecode, village.svillagename AS svillagename, location.ssamplelocationname,"
				+ " location.nsamplelocationcode," + " ssl.dmodifieddate, " + " ssl.nstatus,sh.ntransactionstatus "
				+ "FROM sampleschedulinglocation ssl "
				+ "LEFT JOIN site central  ON ssl.ncentralsitecode = central.nsitecode "
				+ "LEFT JOIN site region   ON ssl.nregioncode   = region.nsitecode "
				+ "LEFT JOIN site district ON ssl.ndistrictcode = district.nsitecode "
				+ "LEFT JOIN site city     ON ssl.ncitycode     = city.nsitecode "
				+ "LEFT JOIN villages village  ON ssl.nvillagecode  = village.nvillagecode "
				+ "LEFT JOIN samplelocation location ON ssl.nsamplelocationcode = location.nsamplelocationcode "
				+" LEFT JOIN sampleschedulinghistory sh ON sh.nsampleschedulingcode = ssl.nsampleschedulingcode "
				+ "WHERE ssl.nsampleschedulinglocationcode = " + nsampleschedulinglocationcode + " AND ssl.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssl.nsitecode = "
				+ objUserInfo.getNmastersitecode()+"  AND sh.nsampleschedulinghistorycode = ( SELECT MAX(sh2.nsampleschedulinghistorycode) "
						+ " FROM sampleschedulinghistory sh2  WHERE sh2.nsampleschedulingcode = ssl.nsampleschedulingcode )";

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final List<SampleSchedulingLocation> locationList = jdbcTemplate.query(strQuery,
				new SampleSchedulingLocation());

		outputMap.put("sampleSchedulingLocation", locationList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

//	public ResponseEntity<Object> getAllSampleSchedulingLocation(final int nsampleschedulingcode,
//			final UserInfo objUserInfo) throws Exception {
//
//		final Map<String, Object> outputMap = new HashMap<String, Object>();
//
//		String strQuery = "SELECT " + " ssl.nsampleschedulinglocationcode, " + " ssl.nsampleschedulingcode, "
//				+ " ssl.nregioncode, region.ssitename AS sregionname, "
//				+ " ssl.ndistrictcode, district.ssitename AS sdistrictname, "
//				+ " ssl.ncitycode, city.ssitename AS scityname, "
//				+ " ssl.nvillagecode, village.svillagename AS svillagename, location.ssamplelocationname,location.nsamplelocationcode,"
//				+ " ssl.dmodifieddate, " + " ssl.nstatus " + "FROM sampleschedulinglocation ssl "
//				+ "LEFT JOIN site region   ON ssl.nregioncode   = region.nsitecode "
//				+ "LEFT JOIN site district ON ssl.ndistrictcode = district.nsitecode "
//				+ "LEFT JOIN site city     ON ssl.ncitycode     = city.nsitecode "
//				+ "LEFT JOIN villages village  ON ssl.nvillagecode  = village.nvillagecode "
//				+ "LEFT JOIN samplelocation location ON ssl.nsamplelocationcode = location.nsamplelocationcode "
//				+ "WHERE ssl.nsampleschedulingcode = " + nsampleschedulingcode + " AND ssl.nstatus = "
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ssl.nsitecode = "
//				+ objUserInfo.getNtranssitecode();
//
//		ObjectMapper objMapper = new ObjectMapper();
//		objMapper.registerModule(new JavaTimeModule());
//
//		final List<SampleSchedulingLocation> locationList = jdbcTemplate.query(strQuery,
//				new SampleSchedulingLocation());
//
//		outputMap.put("sampleSchedulingLocation", locationList);
//		return new ResponseEntity<>(outputMap, HttpStatus.OK);
//	}

//	
//	public ResponseEntity<Object> getAllSampleSchedulingLocation(final int nsampleschedulingcode,
//	        final UserInfo userInfo) throws Exception {
//
//	    Map<String, Object> outputMap = new HashMap<>();
//
//	    // -------------------------------
//	    // Step 1: Get Current Site
//	    // -------------------------------
//	    String configFilter = "WITH RECURSIVE selected_config AS ( " +
//	            " SELECT * FROM sitehierarchyconfig sc " +
//	            " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() +
//	            "   AND jsonb_path_exists(sc.jsondata, '$.** ? (@.item.selectedNodeDetail.nsitecode == "
//	            + userInfo.getNtranssitecode() + ")') " +
//	            " ORDER BY sc.dmodifieddate DESC LIMIT 1 " +
//	            ") ";
//
//	    String strCurrent = configFilter +
//	            " , current_site AS ( " +
//	            " SELECT s.nsitecode, s.ssitename, 'Current' AS srelation " +
//	            " FROM site s " +
//	            " WHERE s.nsitecode = " + userInfo.getNtranssitecode() +
//	            " ) " +
//	            " , json_expanded AS ( " +
//	            " SELECT DISTINCT (elem->>'nsitecode')::int AS nsitecode, " +
//	            " elem->>'ssitetypename' AS ssitetypename, " +
//	            " (elem->>'nhierarchicalorderno')::int AS nlevel " +
//	            " FROM selected_config sc, " +
//	            " jsonb_path_query(sc.jsondata, '$.**.item.selectedNodeDetail') elem " +
//	            " ) " +
//	            " SELECT c.nsitecode, j.ssitetypename " +
//	            " FROM current_site c " +
//	            " JOIN json_expanded j ON j.nsitecode = c.nsitecode ";
//
//	    List<SampleScheduling> currentList = jdbcTemplate.query(strCurrent, new SampleScheduling());
//
//	    // -------------------------------
//	    // Step 2: Get Parent Sites
//	    // -------------------------------
//	    String strParent = configFilter +
//	            " , site_hierarchy AS ( " +
//	            "    SELECT " +
//	            " (sc.jsondata->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, " +
//	            " sc.jsondata->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, " +
//	            " (sc.jsondata->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, " +
//	            " (sc.jsondata->>'parentKey')::int AS parentkey, " +
//	            " sc.jsondata->'nodes' AS children " +
//	            " FROM selected_config sc " +
//	            " UNION ALL " +
//	            " SELECT " +
//	            " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, " +
//	            " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, " +
//	            " (child->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, " +
//	            " (child->>'parentKey')::int AS parentkey, " +
//	            " child->'nodes' AS children " +
//	            " FROM site_hierarchy sh, jsonb_array_elements(sh.children) AS child " +
//	            " ) , parents AS ( " +
//	            "    SELECT sh.* FROM site_hierarchy sh " +
//	            "    WHERE sh.nsitecode = " + userInfo.getNtranssitecode() +
//	            "    UNION ALL " +
//	            "    SELECT sh.* FROM site_hierarchy sh " +
//	            "    JOIN parents p ON sh.nsitecode = p.parentkey " +
//	            " ) " +
//	            " SELECT nsitecode, ssitetypename " +
//	            " FROM parents " +
//	            " WHERE nsitecode != " + userInfo.getNtranssitecode();
//
//	    List<SampleScheduling> parentList = jdbcTemplate.query(strParent, new SampleScheduling());
//
//	    // -------------------------------
//	    // Step 3: Collect codes for region, district, city
//	    // -------------------------------
//	    Set<Integer> regionCodes = new HashSet<>();
//	    Set<Integer> districtCodes = new HashSet<>();
//	    Set<Integer> cityCodes = new HashSet<>();
//
//	    for (SampleScheduling parent : parentList) {
//	        String type = parent.getSsitetypename();
//	        int code = parent.getNsitecode();
//	        if ("Region".equalsIgnoreCase(type)) regionCodes.add(code);
//	        else if ("District".equalsIgnoreCase(type)) districtCodes.add(code);
//	        else if ("City".equalsIgnoreCase(type) || "Lab".equalsIgnoreCase(type)) cityCodes.add(code);
//	    }
//
//	    for (SampleScheduling current : currentList) {
//	        String type = current.getSsitetypename();
//	        int code = current.getNsitecode();
//	        if ("Region".equalsIgnoreCase(type)) regionCodes.add(code);
//	        else if ("District".equalsIgnoreCase(type)) districtCodes.add(code);
//	        else if ("City".equalsIgnoreCase(type) || "Lab".equalsIgnoreCase(type)) cityCodes.add(code);
//	    }
//
//	    // -------------------------------
//	    // Step 4: Build dynamic query for SampleSchedulingLocation
//	    // -------------------------------
//	    StringBuilder strQuery = new StringBuilder();
//	    strQuery.append("SELECT ssl.* FROM sampleschedulinglocation ssl ")
//	            .append("WHERE ssl.nsampleschedulingcode = ").append(nsampleschedulingcode)
//	            .append(" AND ssl.nstatus = ").append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
//
//	    if (!regionCodes.isEmpty()) {
//	        strQuery.append(" AND ssl.nregioncode IN (")
//	                .append(regionCodes.toString().replaceAll("[\\[\\]\\s]", ""))
//	                .append(")");
//	    }
//
//	    if (!districtCodes.isEmpty()) {
//	        strQuery.append(" AND ssl.ndistrictcode IN (")
//	                .append(districtCodes.toString().replaceAll("[\\[\\]\\s]", ""))
//	                .append(")");
//	    }
//
//	    if (!cityCodes.isEmpty()) {
//	        strQuery.append(" AND ssl.ncitycode IN (")
//	                .append(cityCodes.toString().replaceAll("[\\[\\]\\s]", ""))
//	                .append(")");
//	    }
//
//	    List<SampleSchedulingLocation> locationList = jdbcTemplate.query(strQuery.toString(),
//	            new SampleSchedulingLocation());
//
//	    outputMap.put("sampleSchedulingLocation", locationList);
//	    return new ResponseEntity<>(outputMap, HttpStatus.OK);
//	}

//	
//	public ResponseEntity<Object> getAllSampleSchedulingLocation(final int nsampleschedulingcode,
//	        final UserInfo userInfo) throws Exception {
//
//	    Map<String, Object> outputMap = new HashMap<>();
//
//	    // -------------------------------
//	    // Step 1: Get Current Site
//	    // -------------------------------
//	    String configFilter = "WITH RECURSIVE selected_config AS ( " +
//	            " SELECT * FROM sitehierarchyconfig sc " +
//	            " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus() +
//	            "   AND jsonb_path_exists(sc.jsondata, '$.** ? (@.item.selectedNodeDetail.nsitecode == "
//	            + userInfo.getNtranssitecode() + ")') " +
//	            " ORDER BY sc.dmodifieddate DESC LIMIT 1 " +
//	            ") ";
//
//	    String strCurrent = configFilter +
//	            " , current_site AS ( " +
//	            " SELECT s.nsitecode, s.ssitename, 'Current' AS srelation " +
//	            " FROM site s " +
//	            " WHERE s.nsitecode = " + userInfo.getNtranssitecode() +
//	            " ) " +
//	            " , json_expanded AS ( " +
//	            " SELECT DISTINCT (elem->>'nsitecode')::int AS nsitecode, " +
//	            " elem->>'ssitetypename' AS ssitetypename, " +
//	            " (elem->>'nhierarchicalorderno')::int AS nlevel " +
//	            " FROM selected_config sc, " +
//	            " jsonb_path_query(sc.jsondata, '$.**.item.selectedNodeDetail') elem " +
//	            " ) " +
//	            " SELECT c.nsitecode, j.ssitetypename " +
//	            " FROM current_site c " +
//	            " JOIN json_expanded j ON j.nsitecode = c.nsitecode ";
//
//	    List<SampleScheduling> currentList = jdbcTemplate.query(strCurrent, new SampleScheduling());
//
//	    // -------------------------------
//	    // Step 2: Get Parent Sites
//	    // -------------------------------
//	    String strParent = configFilter +
//	            " , site_hierarchy AS ( " +
//	            "    SELECT " +
//	            " (sc.jsondata->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, " +
//	            " sc.jsondata->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, " +
//	            " (sc.jsondata->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, " +
//	            " (sc.jsondata->>'parentKey')::int AS parentkey, " +
//	            " sc.jsondata->'nodes' AS children " +
//	            " FROM selected_config sc " +
//	            " UNION ALL " +
//	            " SELECT " +
//	            " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, " +
//	            " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, " +
//	            " (child->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, " +
//	            " (child->>'parentKey')::int AS parentkey, " +
//	            " child->'nodes' AS children " +
//	            " FROM site_hierarchy sh, jsonb_array_elements(sh.children) AS child " +
//	            " ) , parents AS ( " +
//	            "    SELECT sh.* FROM site_hierarchy sh " +
//	            "    WHERE sh.nsitecode = " + userInfo.getNtranssitecode() +
//	            "    UNION ALL " +
//	            "    SELECT sh.* FROM site_hierarchy sh " +
//	            "    JOIN parents p ON sh.nsitecode = p.parentkey " +
//	            " ) " +
//	            " SELECT nsitecode, ssitetypename " +
//	            " FROM parents " +
//	            " WHERE nsitecode != " + userInfo.getNtranssitecode();
//
//	    List<SampleScheduling> parentList = jdbcTemplate.query(strParent, new SampleScheduling());
//
//	    // -------------------------------
//	    // Step 3: Collect codes for region, district, city
//	    // -------------------------------
//	    Set<Integer> regionCodes = new HashSet<>();
//	    Set<Integer> districtCodes = new HashSet<>();
//	    Set<Integer> cityCodes = new HashSet<>();
//
//	    for (SampleScheduling parent : parentList) {
//	        String type = parent.getSsitetypename();
//	        int code = parent.getNsitecode();
//	        if ("Region".equalsIgnoreCase(type)) regionCodes.add(code);
//	        else if ("District".equalsIgnoreCase(type)) districtCodes.add(code);
//	        else if ("City".equalsIgnoreCase(type) || "Lab".equalsIgnoreCase(type)) cityCodes.add(code);
//	    }
//
//	    for (SampleScheduling current : currentList) {
//	        String type = current.getSsitetypename();
//	        int code = current.getNsitecode();
//	        if ("Region".equalsIgnoreCase(type)) regionCodes.add(code);
//	        else if ("District".equalsIgnoreCase(type)) districtCodes.add(code);
//	        else if ("City".equalsIgnoreCase(type) || "Lab".equalsIgnoreCase(type)) cityCodes.add(code);
//	    }
//
//	    String regionIn = regionCodes.isEmpty() ? null : regionCodes.toString().replaceAll("[\\[\\]\\s]", "");
//	    String districtIn = districtCodes.isEmpty() ? null : districtCodes.toString().replaceAll("[\\[\\]\\s]", "");
//	    String cityIn = cityCodes.isEmpty() ? null : cityCodes.toString().replaceAll("[\\[\\]\\s]", "");
//
//	    // -------------------------------
//	    // Step 4: Query SampleSchedulingLocation with joins for names
//	    // -------------------------------
//	    String strQuery = "SELECT " +
//	            " ssl.nsampleschedulinglocationcode, ssl.nsampleschedulingcode, " +
//	            " ssl.nregioncode, region.ssitename AS sregionname, " +
//	            " ssl.ndistrictcode, district.ssitename AS sdistrictname, " +
//	            " ssl.ncitycode, city.ssitename AS scityname, " +
//	            " ssl.nvillagecode, village.svillagename AS svillagename, " +
//	            " location.ssamplelocationname, location.nsamplelocationcode, " +
//	            " ssl.dmodifieddate, ssl.nstatus " +
//	            "FROM sampleschedulinglocation ssl " +
//	            "LEFT JOIN site region   ON ssl.nregioncode   = region.nsitecode " +
//	            "LEFT JOIN site district ON ssl.ndistrictcode = district.nsitecode " +
//	            "LEFT JOIN site city     ON ssl.ncitycode     = city.nsitecode " +
//	            "LEFT JOIN villages village  ON ssl.nvillagecode  = village.nvillagecode " +
//	            "LEFT JOIN samplelocation location ON ssl.nsamplelocationcode = location.nsamplelocationcode " +
//	            "WHERE ssl.nsampleschedulingcode = " + nsampleschedulingcode + " " +
//	            (regionIn != null ? "AND ssl.nregioncode IN (" + regionIn + ") " : "") +
//	            (districtIn != null ? "AND ssl.ndistrictcode IN (" + districtIn + ") " : "") +
//	            (cityIn != null ? "AND ssl.ncitycode IN (" + cityIn + ") " : "") +
//	            "AND ssl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//
//	    List<SampleSchedulingLocation> locationList = jdbcTemplate.query(strQuery, new SampleSchedulingLocation());
//
//	    outputMap.put("sampleSchedulingLocation", locationList);
//	    return new ResponseEntity<>(outputMap, HttpStatus.OK);
//	}

	public ResponseEntity<Object> getAllSampleSchedulingLocation(final int nsampleschedulingcode,
			final UserInfo userInfo) throws Exception {

		Map<String, Object> outputMap = new HashMap<>();
	//modified by sujatha ATE_274 SWSM-117 by  for getting the both approved & retired location based on the site that logged in
		String configFilter = "WITH RECURSIVE selected_config AS ( " + " SELECT * FROM sitehierarchyconfig sc "
				+ " WHERE sc.ntransactionstatus IN (" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+", "
				+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
				+ ")  AND jsonb_path_exists(sc.jsondata, '$.** ? (@.item.selectedNodeDetail.nsitecode == "
				+ userInfo.getNtranssitecode() + ")') " + " ORDER BY sc.dmodifieddate DESC " + ") ";

//		String strCurrent = configFilter + " , current_site AS ( "
//				+ " SELECT s.nsitecode, s.ssitename, 'Current' AS srelation " + " FROM site s "
//				+ " WHERE s.nsitecode = " + userInfo.getNtranssitecode() + " ) " + " , json_expanded AS ( "
//				+ " SELECT DISTINCT (elem->>'nsitecode')::int AS nsitecode, "
//				+ " elem->>'ssitetypename' AS ssitetypename, " + " (elem->>'nhierarchicalorderno')::int AS nlevel "
//				+ " FROM selected_config sc, " + " jsonb_path_query(sc.jsondata, '$.**.item.selectedNodeDetail') elem "
//				+ " ) " + " SELECT c.nsitecode, j.ssitetypename, j.nlevel " + " FROM current_site c "
//				+ " JOIN json_expanded j ON j.nsitecode = c.nsitecode ";
		// added by sujatha to use group by clause in the above query modified in the
		// below
		String strCurrent = configFilter + " , current_site AS ( "
				+ " SELECT s.nsitecode, s.ssitename, 'Current' AS srelation " + " FROM site s  WHERE s.nsitecode = "
				+ userInfo.getNtranssitecode() + " ) "
				+ " , json_expanded AS ( SELECT (elem->>'nsitecode')::int AS nsitecode, "
				+ " elem->>'ssitetypename' AS ssitetypename, (elem->>'nhierarchicalorderno')::int AS nlevel "
				+ " FROM selected_config sc, jsonb_path_query(sc.jsondata, '$.**.item.selectedNodeDetail') elem "
				+ " GROUP BY (elem->>'nsitecode')::int, elem->>'ssitetypename', (elem->>'nhierarchicalorderno')::int ) "
				+ " SELECT c.nsitecode, j.ssitetypename, j.nlevel FROM current_site c  JOIN json_expanded j "
				+ " ON j.nsitecode = c.nsitecode ";

		final List<SampleScheduling> currentList = jdbcTemplate.query(strCurrent, new SampleScheduling());

		String strParent = configFilter + " , site_hierarchy AS ( " + "    SELECT "
				+ " (sc.jsondata->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " sc.jsondata->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (sc.jsondata->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " (sc.jsondata->>'parentKey')::int AS parentkey, " + " sc.jsondata->'nodes' AS children "
				+ "    FROM selected_config sc " + " UNION ALL " + " SELECT "
				+ " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (child->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " (child->>'parentKey')::int AS parentkey, " + " child->'nodes' AS children "
				+ " FROM site_hierarchy sh, jsonb_array_elements(sh.children) AS child " + " ) , parents AS ( "
				+ "    SELECT sh.* FROM site_hierarchy sh " + "    WHERE sh.nsitecode = " + userInfo.getNtranssitecode()
				+ "    UNION ALL " + "    SELECT sh.* FROM site_hierarchy sh "
				+ "    JOIN parents p ON sh.nsitecode = p.parentkey " + " ) SELECT nsitecode, ssitetypename, nlevel "
				+ " FROM parents " + " WHERE nsitecode != " + userInfo.getNtranssitecode();

		final List<SampleScheduling> parentList = jdbcTemplate.query(strParent, new SampleScheduling());

		// -------------------------------
		// Step 3: Build mapping level -> site codes dynamically
		// -------------------------------
		Map<Integer, Set<Integer>> levelToSiteCodes = new HashMap<>();

		// Include parents
		for (SampleScheduling parent : parentList) {
			int level = parent.getNlevel();
			int code = parent.getNsitecode();
			levelToSiteCodes.computeIfAbsent(level, k -> new HashSet<>()).add(code);
		}

		// Include current site
		for (SampleScheduling current : currentList) {
			int level = current.getNlevel();
			int code = current.getNsitecode();
			levelToSiteCodes.computeIfAbsent(level, k -> new HashSet<>()).add(code);
		}

		// -------------------------------
		// Step 4: Map level to database column dynamically
		// -------------------------------
		Map<Integer, String> levelToColumn = Map.of(2, "ssl.nregioncode", 3, "ssl.ndistrictcode", 4, "ssl.ncitycode");

		// -------------------------------
		// Step 5: Build SQL query dynamically
		// -------------------------------
//	    StringBuilder strQuery = new StringBuilder();
//	    strQuery.append("SELECT ssl.nsampleschedulinglocationcode, ssl.nsampleschedulingcode, ")
//	            .append(" ssl.nregioncode, region.ssitename AS sregionname, ")
//	            .append(" ssl.ndistrictcode, district.ssitename AS sdistrictname, ")
//	            .append(" ssl.ncitycode, city.ssitename AS scityname, ")
//	            .append(" ssl.nvillagecode, village.svillagename AS svillagename, ")
//	            .append(" location.ssamplelocationname, location.nsamplelocationcode, ")
//	            .append(" ssl.dmodifieddate, ssl.nstatus ")
//	            .append("FROM sampleschedulinglocation ssl ")
//	            .append("LEFT JOIN site region   ON ssl.nregioncode   = region.nsitecode ")
//	            .append("LEFT JOIN site district ON ssl.ndistrictcode = district.nsitecode ")
//	            .append("LEFT JOIN site city     ON ssl.ncitycode     = city.nsitecode ")
//	            .append("LEFT JOIN villages village  ON ssl.nvillagecode  = village.nvillagecode ")
//	            .append("LEFT JOIN samplelocation location ON ssl.nsamplelocationcode = location.nsamplelocationcode ")
//	            .append("WHERE ssl.nsampleschedulingcode = ").append(nsampleschedulingcode)
//	            .append(" AND ssl.nstatus = ").append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());

		// added centralsitecode by sujatha ATE_274 for added a new field
		// ncentralsitecode
		StringBuilder strQuery = new StringBuilder();
	//added by sujatha ATE_274 SWSM-117 for throwing alert if open this with the logged in site which is not in the approved site hierarchy configuration
		if (currentList.isEmpty()) {
		    outputMap.put("sampleSchedulingLocation", Collections.emptyList());
		    return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}

		strQuery.append("SELECT ssl.nsampleschedulinglocationcode, ssl.nsampleschedulingcode, ")
				.append(" ssl.ncentralsitecode, central.ssitename AS scentralsitename, ")
				.append(" ssl.nregioncode, region.ssitename AS sregionname, ")
				.append(" ssl.ndistrictcode, district.ssitename AS sdistrictname, ")
				.append(" ssl.ncitycode, city.ssitename AS scityname, ")
				.append(" ssl.nvillagecode, village.svillagename AS svillagename, ")
				.append(" location.ssamplelocationname, location.nsamplelocationcode, ")
				.append(" ssl.dmodifieddate, ssl.nstatus, ssl.nsitehierarchyconfigcode ")
				.append("FROM sampleschedulinglocation ssl ")
				.append("LEFT JOIN site central  ON ssl.ncentralsitecode = central.nsitecode ")
				.append("LEFT JOIN site region   ON ssl.nregioncode   = region.nsitecode ")
				.append("LEFT JOIN site district ON ssl.ndistrictcode = district.nsitecode ")
				.append("LEFT JOIN site city     ON ssl.ncitycode     = city.nsitecode ")
				.append("LEFT JOIN villages village  ON ssl.nvillagecode  = village.nvillagecode ")
				.append("LEFT JOIN samplelocation location ON ssl.nsamplelocationcode = location.nsamplelocationcode ")
				.append("WHERE ssl.nsampleschedulingcode = ").append(nsampleschedulingcode)
				.append(" AND ssl.nstatus = ").append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus());
				//commented by sujatha ATE_274 for not showing the inserted value because of checking nsitehierachyconfig code with max of no, not necessary to check
				//.append(" AND ssl.nsitehierarchyconfigcode = (SELECT MAX(nsitehierarchyconfigcode) FROM sitehierarchyconfig)");

		// -------------------------------
		// Step 6: Apply dynamic filters using level
		// -------------------------------
		for (Map.Entry<Integer, Set<Integer>> entry : levelToSiteCodes.entrySet()) {
			int level = entry.getKey();
			Set<Integer> codes = entry.getValue();
			String column = levelToColumn.get(level);
			if (column != null && !codes.isEmpty()) {
				strQuery.append(" AND ").append(column).append(" IN (")
						.append(codes.toString().replaceAll("[\\[\\]\\s]", "")).append(")");
			}
		}
	
//	    AND ssl.nregioncode IN (101,102)
//	    AND ssl.ndistrictcode IN (201)
//	    AND ssl.ncitycode IN (301,302)
		// -------------------------------
		// Step 7: Execute query
		// -------------------------------
		List<SampleSchedulingLocation> locationList = jdbcTemplate.query(strQuery.toString(),
				new SampleSchedulingLocation());

		outputMap.put("sampleSchedulingLocation", locationList);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method provides access to the DAO layer to retrieve all available
	 * sampleschedulinglocation for a specific site.
	 *
	 * @param userInfo              an instance of [UserInfo] containing the
	 *                              logged-in user details and the nmasterSiteCode,
	 *                              which is the primary key of the site for which
	 *                              the complaints are to be fetched
	 * @param nsampleschedulingcode [int] primary key of SampleScheduling object
	 *
	 * @return a ResponseEntity containing the list of sample scheduling records
	 *         associated with the specified site
	 *
	 * @throws Exception if any errors occur in the DAO layer
	 */

	public ResponseEntity<Object> getSampleSchedulingFile(final int nsampleschedulingcode, final UserInfo objUserInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		String query = "select tf.noffsetdcreateddate,tf.nsampleschedulingfilecode,"
				+ "(select  count(nsampleschedulingfilecode) from sampleschedulingfile where nsampleschedulingfilecode>0"
				+ " and nsampleschedulingcode = " + nsampleschedulingcode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") as ncount,tf.sdescription,"
				+ " tf.nsampleschedulingfilecode as nprimarycode,tf.sfilename,tf.nsampleschedulingcode,tf.ssystemfilename,"
				+ " tf.nattachmenttypecode,coalesce(at.jsondata->'sattachmenttype'->>'"
				+ objUserInfo.getSlanguagetypecode() + "',"
				+ "	at.jsondata->'sattachmenttype'->>'en-US') as sattachmenttype, case when tf.nlinkcode=-1 then '-' else lm.jsondata->>'slinkname'"
				+ " end slinkname, tf.nfilesize," + " case when tf.nattachmenttypecode= "
				+ Enumeration.AttachmentType.LINK.gettype() + " then '-' else" + " COALESCE(TO_CHAR(tf.dcreateddate,'"
				+ objUserInfo.getSpgsitedatetime() + "'),'-') end  as screateddate, "
				+ " tf.nlinkcode, case when tf.nlinkcode = -1 then tf.nfilesize::varchar(1000) else '-' end sfilesize"
				+ " from sampleschedulingfile tf,attachmenttype at, linkmaster lm  "
				+ " where at.nattachmenttypecode = tf.nattachmenttypecode and at.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and lm.nlinkcode = tf.nlinkcode and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nsitecode = "
				+ objUserInfo.getNmastersitecode() + " and tf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.nsitecode = "
				+ objUserInfo.getNmastersitecode() + " and tf.nsampleschedulingcode=" + nsampleschedulingcode
				+ " order by tf.nsampleschedulingfilecode;";
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());

		final List<SampleSchedulingFile> lstSampleSchedulingfile = objMapper
				.convertValue(
						dateUtilityFunction.getSiteLocalTimeFromUTC(
								jdbcTemplate.query(query, new SampleSchedulingFile()), Arrays.asList("screateddate"),
								Arrays.asList(objUserInfo.getStimezoneid()), objUserInfo, false, null, false),
						new TypeReference<List<SampleSchedulingFile>>() {
						});

		outputMap.put("sampleSchedulingFile", lstSampleSchedulingfile);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method will access the DAO layer that is used to create the records in
	 * sampleschedulingfile
	 * 
	 * @param request  holding the date for upload the file in sampleschedulingfile
	 *                 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of sampleschedulingfile
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> editSampleSchedulingFile(final SampleSchedulingFile objSampleSchedulingFile,
			final UserInfo objUserInfo) throws Exception {
		final String sEditQuery = "select  tf.nsampleschedulingfilecode, tf.nsampleschedulingcode, tf.nlinkcode, tf.nattachmenttypecode, "
				+ " tf.sfilename, tf.sdescription, tf.nfilesize,"
				+ " tf.ssystemfilename,  lm.jsondata->>'slinkname' as slinkname"
				+ " from sampleschedulingfile tf, linkmaster lm,samplescheduling s where lm.nlinkcode = tf.nlinkcode"
				+ " and tf.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and tf.nsitecode =" + objUserInfo.getNmastersitecode() + " and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nsitecode ="
				+ objUserInfo.getNmastersitecode() + " and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nsitecode ="
				+ objUserInfo.getNmastersitecode()
				+ " and tf.nsampleschedulingcode=s.nsampleschedulingcode and tf.nsampleschedulingfilecode = "
				+ objSampleSchedulingFile.getNsampleschedulingfilecode();
		final SampleSchedulingFile objTF = (SampleSchedulingFile) jdbcUtilityFunction.queryForObject(sEditQuery,
				SampleSchedulingFile.class, jdbcTemplate);
		if (objTF != null) {
			return new ResponseEntity<Object>(objTF, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to update the records in sampleschedulingfile
	 * 
	 * @param request  holding the date for update the file in sampleschedulingfile
	 *                 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of sampleschedulingfile
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateSampleSchedulingFile(MultipartHttpServletRequest request, UserInfo objUserInfo)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final List<SampleSchedulingFile> lstSampleSchedulingFile = objMapper.readValue(
				request.getParameter("sampleSchedulingFile"), new TypeReference<List<SampleSchedulingFile>>() {
				});
		if (lstSampleSchedulingFile != null && lstSampleSchedulingFile.size() > 0) {
			final SampleSchedulingFile objSampleSchedulingFile = lstSampleSchedulingFile.get(0);
			final SampleSchedulingHistory objSampleSchedulingHistory = checKSampleSchedulingIsPresent(
					objSampleSchedulingFile.getNsampleschedulingcode(), objUserInfo);
			if (objSampleSchedulingHistory != null) {
				final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if (objSampleSchedulingFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
					}
				}
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final String sQuery = "select * from sampleschedulingfile where" + " nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and nsampleschedulingfilecode = "
							+ objSampleSchedulingFile.getNsampleschedulingfilecode() + " and nsitecode ="
							+ objUserInfo.getNmastersitecode();
					final SampleSchedulingFile objTF = (SampleSchedulingFile) jdbcUtilityFunction.queryForObject(sQuery,
							SampleSchedulingFile.class, jdbcTemplate);
					if (objTF != null) {
						String ssystemfilename = "";
						if (objSampleSchedulingFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
								.gettype()) {
							ssystemfilename = objSampleSchedulingFile.getSsystemfilename();
						}
						final String sUpdateQuery = "update sampleschedulingfile set sfilename=N'"
								+ stringUtilityFunction.replaceQuote(objSampleSchedulingFile.getSfilename()) + "',"
								+ " sdescription=N'"
								+ stringUtilityFunction.replaceQuote(objSampleSchedulingFile.getSdescription())
								+ "', ssystemfilename= N'" + ssystemfilename + "'," + " nattachmenttypecode = "
								+ objSampleSchedulingFile.getNattachmenttypecode() + ", nlinkcode="
								+ objSampleSchedulingFile.getNlinkcode() + "," + " nfilesize = "
								+ objSampleSchedulingFile.getNfilesize() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' "
								+ "  where nsampleschedulingfilecode = "
								+ objSampleSchedulingFile.getNsampleschedulingfilecode();
						objSampleSchedulingFile.setDcreateddate(objTF.getDcreateddate());
						jdbcTemplate.execute(sUpdateQuery);
						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<Object>();
						multilingualIDList
								.add(objSampleSchedulingFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP
										.gettype() ? "IDS_EDITSAMPLESCHEDULINGFILE" : "IDS_EDITSAMPLESCHEDULINGLINK");
						lstOldObject.add(objTF);
						auditUtilityFunction.fnInsertAuditAction(lstSampleSchedulingFile, 2, lstOldObject,
								multilingualIDList, objUserInfo);
						return (getSampleSchedulingFile(objSampleSchedulingFile.getNsampleschedulingcode(),
								objUserInfo));
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESCHEDULINGALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This method will access the DAO layer that is used to delete an entry in
	 * samplescheduling table.
	 * 
	 * @param inputMap holding the date fromYear,toYear,Status samplescheduling
	 *                 details and userInfo [UserInfo] holding logged in user
	 *                 details and nmasterSiteCode [int] primary key of site object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and data of deleted
	 *         samplescheduling object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteSampleSchedulingFile(SampleSchedulingFile objSampleSchedulingFile,
			UserInfo objUserInfo) throws Exception {
		final SampleSchedulingHistory objSampleSchedulingHistory = checKSampleSchedulingIsPresent(
				objSampleSchedulingFile.getNsampleschedulingcode(), objUserInfo);
		if (objSampleSchedulingHistory != null) {
			if (objSampleSchedulingHistory != null) {
				final String sQuery = "select * from sampleschedulingfile where" + " nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsampleschedulingfilecode = " + objSampleSchedulingFile.getNsampleschedulingfilecode()
						+ "  and nsitecode = " + objUserInfo.getNmastersitecode();
				final SampleSchedulingFile objTF = (SampleSchedulingFile) jdbcUtilityFunction.queryForObject(sQuery,
						SampleSchedulingFile.class, jdbcTemplate);
				if (objTF != null) {
					if (objSampleSchedulingFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					} else {
						objSampleSchedulingFile.setScreateddate(null);
					}
					final String sUpdateQuery = "update sampleschedulingfile set" + "  dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "'" + ", nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
							+ " where nsampleschedulingfilecode = "
							+ objSampleSchedulingFile.getNsampleschedulingfilecode();
					jdbcTemplate.execute(sUpdateQuery);
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> lstObject = new ArrayList<>();
					multilingualIDList.add(
							objSampleSchedulingFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
									? "IDS_DELETESAMPLESCHEDULINGFILE"
									: "IDS_DELETESAMPLESCHEDULINGLINK");
					lstObject.add(objSampleSchedulingFile);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			return getSampleSchedulingFile(objSampleSchedulingFile.getNsampleschedulingcode(), objUserInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SAMPLESCHEDULINGALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This service implementation declaration will access the DAO layer that is
	 * used to view the records in sampleschedulingfile
	 * 
	 * @param request  holding the date for update the file in sampleschedulingfile
	 *                 table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of sampleschedulingfile
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> viewAttachedSampleSchedulingFile(SampleSchedulingFile objSampleSchedulingFile,
			UserInfo objUserInfo) throws Exception {
		Map<String, Object> map = new HashMap<>();
		final SampleSchedulingHistory objSampleSchedulingHistory = checKSampleSchedulingIsPresent(
				objSampleSchedulingFile.getNsampleschedulingcode(), objUserInfo);
		if (objSampleSchedulingHistory != null) {
			String sQuery = "select * from sampleschedulingfile where" + " nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsampleschedulingfilecode = "
					+ objSampleSchedulingFile.getNsampleschedulingfilecode() + " and nsitecode ="
					+ objUserInfo.getNmastersitecode();
			final SampleSchedulingFile objTF = (SampleSchedulingFile) jdbcUtilityFunction.queryForObject(sQuery,
					SampleSchedulingFile.class, jdbcTemplate);

			if (objTF != null) {
				if (objTF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					map = ftpUtilityFunction.FileViewUsingFtp(objTF.getSsystemfilename(), -1, objUserInfo, "", "");
				} else {
					sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where" + " nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nlinkcode="
							+ objTF.getNlinkcode();
					LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery, LinkMaster.class,
							jdbcTemplate);

					map.put("AttachLink", objlinkmaster.getSlinkname() + objTF.getSfilename());
					objSampleSchedulingFile.setScreateddate(null);
				}
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> lstObject = new ArrayList<>();
				multilingualIDList.add(
						objSampleSchedulingFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
								? "IDS_VIEWSAMPLESCHEDULINGFILE"
								: "IDS_VIEWSAMPLESCHEDULINGLINK");
				lstObject.add(objSampleSchedulingFile);
				auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
			} else {

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<Object>(commonFunction
					.getMultilingualMessage("IDS_SAMPLESCHEDULINGALREADYDELETED", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	/**
	 * This method provides access to the DAO layer to retrieve all the available
	 * regions
	 *
	 * @param userInfo [UserInfo] contains the details of the logged-in user,
	 *                 including the nmasterSiteCode [int], which represents the
	 *                 primary key of the site for which the region list is to be
	 *                 fetched.
	 * @return a ResponseEntity containing the list of region records associated
	 * 
	 * @throws Exception if any errors occur in the DAO layer during data retrieval.
	 */
//	@Override
//	public ResponseEntity<Object> getRegion(final UserInfo userInfo) throws Exception {
//
//		String configFilter = "WITH RECURSIVE selected_config AS ( " + " SELECT * FROM sitehierarchyconfig sc "
//				+ " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
//				+ "   AND jsonb_path_exists(sc.jsondata, '$.** ? (@.item.selectedNodeDetail.nsitecode == "
//				+ userInfo.getNtranssitecode() + ")') " + " ORDER BY sc.dmodifieddate DESC LIMIT 1 " + " ) ";
//
//		String strCurrent = configFilter + " , current_site AS ( "
//				+ " SELECT s.nsitecode, s.ssitename, 'Current' AS srelation " + " FROM site s "
//				+ " WHERE s.nsitecode = " + userInfo.getNtranssitecode() + " ) " + " , json_expanded AS ( "
//				+ " SELECT DISTINCT (elem->>'nsitecode')::int AS nsitecode, "
//				+ " elem->>'ssitetypename' AS ssitetypename, " + " (elem->>'nhierarchicalorderno')::int AS nlevel "
//				+ " FROM selected_config sc, " + " jsonb_path_query(sc.jsondata, '$.**.item.selectedNodeDetail') elem "
//				+ " ) " + " SELECT c.nsitecode, c.ssitename, c.srelation, j.ssitetypename, j.nlevel "
//				+ " FROM current_site c " + " JOIN json_expanded j ON j.nsitecode = c.nsitecode ";
//
//		final List<SampleScheduling> currentList = jdbcTemplate.query(strCurrent, new SampleScheduling());
//		String strParent = configFilter + " , site_hierarchy AS ( " + "    SELECT "
//				+ " (sc.jsondata->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
//				+ " sc.jsondata->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
//				+ " sc.jsondata->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
//				+ " (sc.jsondata->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
//				+ " (sc.jsondata->>'parentKey')::int AS parentkey, " + " sc.jsondata->'nodes' AS children "
//				+ "    FROM selected_config sc " + " UNION ALL " + " SELECT "
//				+ " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
//				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
//				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
//				+ " (child->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
//				+ " (child->>'parentKey')::int AS parentkey, " + " child->'nodes' AS children "
//				+ " FROM site_hierarchy sh, " + " jsonb_array_elements(sh.children) AS child " + " ) , parents AS ( "
//				+ "    SELECT sh.* " + "    FROM site_hierarchy sh " + "    WHERE sh.nsitecode = "
//				+ userInfo.getNtranssitecode() + "    UNION ALL " + "    SELECT sh.* " + "    FROM site_hierarchy sh "
//				+ "    JOIN parents p ON sh.nsitecode = p.parentkey " + " ) SELECT " + " nsitecode, "
//				+ " split_part(ssitename, '(', 1) AS ssitename, " + " ssitetypename, " + " parentkey, " + " nlevel "
//				+ " FROM parents " + " WHERE nsitecode != " + userInfo.getNtranssitecode() + " ORDER BY nsitecode ";
//
//		final List<SampleScheduling> parentList = jdbcTemplate.query(strParent, new SampleScheduling());
//
//		// Child sites query
//		String strChild = configFilter + " , site_hierarchy AS ( " + "    SELECT "
//				+ " elem->'item'->'selectedNodeDetail'->>'nsitecode' AS nsitecode, "
//				+ " elem->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
//				+ " elem->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
//				+ " (elem->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
//				+ " elem->>'parentKey' AS parentkey, " + " elem->'nodes' AS children " + " FROM selected_config sc, "
//				+ " jsonb_array_elements(sc.jsondata->'nodes') AS elem " + " UNION ALL " + " SELECT "
//				+ " child->'item'->'selectedNodeDetail'->>'nsitecode' AS nsitecode, "
//				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
//				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
//				+ " (child->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
//				+ " child->>'parentKey' AS parentkey, " + " child->'nodes' AS children " + " FROM site_hierarchy sh, "
//				+ " jsonb_array_elements(sh.children) AS child " + " ) SELECT " + " nsitecode::int, "
//				+ " split_part(ssitename, '(', 1) AS ssitename, " + " ssitetypename, " + " parentkey::int, "
//				+ " nlevel " + " FROM site_hierarchy " + " WHERE parentkey::int = " + userInfo.getNtranssitecode()
//				+ "   OR parentkey::int IN ( SELECT nsitecode::int FROM site_hierarchy WHERE parentkey::int = "
//				+ userInfo.getNtranssitecode() + " ) " + " ORDER BY nsitecode::int ";
//
//		final List<SampleScheduling> childList = jdbcTemplate.query(strChild, new SampleScheduling());
//
//		final Map<String, Object> outputMap = new LinkedHashMap<>();
//		if (!currentList.isEmpty()) {
//			outputMap.put("currentList", currentList);
//			outputMap.put("parentList", parentList);
//			outputMap.put("childList", childList);
//		} else {
//			outputMap.put("currentList", null);
//		}
//		return new ResponseEntity<>(outputMap, HttpStatus.OK);
//	}

	@Override
	public ResponseEntity<Object> getRegion(final UserInfo userInfo) throws Exception {

		String configFilter = "WITH RECURSIVE selected_config AS ( " + " SELECT * FROM sitehierarchyconfig sc "
				+ " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ "   AND jsonb_path_exists(sc.jsondata, '$.** ? (@.item.selectedNodeDetail.nsitecode == "
				+ userInfo.getNtranssitecode() + ")') " + " ORDER BY sc.dmodifieddate DESC LIMIT 1 " + " ) ";

		// -------------------------------
		// Current site
		// -------------------------------
//		String strCurrent = configFilter + " , current_site AS ( "
//				+ " SELECT s.nsitecode, s.ssitename, 'Current' AS srelation " + " FROM site s "
//				+ " WHERE s.nsitecode = " + userInfo.getNtranssitecode() + " ) " + " , json_expanded AS ( "
//				+ " SELECT DISTINCT (elem->>'nsitecode')::int AS nsitecode, "
//				+ " elem->>'ssitetypename' AS ssitetypename, " + " (elem->>'nhierarchicalorderno')::int AS nlevel, "
//				+ " sc.nsitehierarchyconfigcode " + " FROM selected_config sc, "
//				+ " jsonb_path_query(sc.jsondata, '$.**.item.selectedNodeDetail') elem " + " ) "
//				+ " SELECT c.nsitecode, c.ssitename, c.srelation, "
//				+ " j.ssitetypename, j.nlevel, j.nsitehierarchyconfigcode " + " FROM current_site c "
//				+ " JOIN json_expanded j ON j.nsitecode = c.nsitecode ";

		// modified by sujatha ATE_274 modified from the above query using distinct to
		// group by clause the in below query
		String strCurrent = configFilter + " , current_site AS ( "
				+ " SELECT s.nsitecode, s.ssitename, 'Current' AS srelation FROM site s " + " WHERE s.nsitecode = "
				+ userInfo.getNtranssitecode() + " ), json_expanded AS ( "
				+ " SELECT (elem->>'nsitecode')::int AS nsitecode, elem->>'ssitetypename' AS ssitetypename, "
				+ " (elem->>'nhierarchicalorderno')::int AS nlevel, sc.nsitehierarchyconfigcode "
				+ " FROM selected_config sc, jsonb_path_query(sc.jsondata, '$.**.item.selectedNodeDetail') elem "
				+ " GROUP BY (elem->>'nsitecode')::int, elem->>'ssitetypename', "
				+ " (elem->>'nhierarchicalorderno')::int, sc.nsitehierarchyconfigcode ) "
				+ " SELECT c.nsitecode, c.ssitename, c.srelation, j.ssitetypename, j.nlevel, j.nsitehierarchyconfigcode "
				+ " FROM current_site c JOIN json_expanded j ON j.nsitecode = c.nsitecode ";

		final List<SampleScheduling> currentList = jdbcTemplate.query(strCurrent, new SampleScheduling());
		// added condition by sujatha ATE_274 SWSM-117 to throw alert when click add location if the logged in site is not in approved site configuration
		if (!currentList.isEmpty()) {

		// -------------------------------
		// Parent sites
		// -------------------------------
		String strParent = configFilter + " , site_hierarchy AS ( " + "    SELECT "
				+ " (sc.jsondata->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " sc.jsondata->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " sc.jsondata->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (sc.jsondata->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " (sc.jsondata->>'parentKey')::int AS parentkey, " + " sc.jsondata->'nodes' AS children, "
				+ " sc.nsitehierarchyconfigcode " + "    FROM selected_config sc " + " UNION ALL " + " SELECT "
				+ " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (child->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " (child->>'parentKey')::int AS parentkey, " + " child->'nodes' AS children, "
				+ " sh.nsitehierarchyconfigcode "
				+ " FROM site_hierarchy sh, jsonb_array_elements(sh.children) AS child " + " ) , parents AS ( "
				+ "    SELECT sh.* " + "    FROM site_hierarchy sh " + "    WHERE sh.nsitecode = "
				+ userInfo.getNtranssitecode() + "    UNION ALL " + "    SELECT sh.* " + "    FROM site_hierarchy sh "
				+ "    JOIN parents p ON sh.nsitecode = p.parentkey " + " ) SELECT nsitecode, "
				+ " split_part(ssitename, '(', 1) AS ssitename, "
				+ " ssitetypename, parentkey, nlevel, nsitehierarchyconfigcode " + " FROM parents "
				+ " WHERE nsitecode != " + userInfo.getNtranssitecode() + " ORDER BY nsitecode ";

		final List<SampleScheduling> parentList = jdbcTemplate.query(strParent, new SampleScheduling());

		// -------------------------------
		// Child sites
		// -------------------------------
		String strChild = configFilter + " , site_hierarchy AS ( " + "    SELECT "
				+ " elem->'item'->'selectedNodeDetail'->>'nsitecode' AS nsitecode, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (elem->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " elem->>'parentKey' AS parentkey, " + " elem->'nodes' AS children, "
				+ " sc.nsitehierarchyconfigcode "
				+ " FROM selected_config sc, jsonb_array_elements(sc.jsondata->'nodes') AS elem " + " UNION ALL "
				+ " SELECT " + " child->'item'->'selectedNodeDetail'->>'nsitecode' AS nsitecode, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (child->'item'->'selectedNodeDetail'->>'nhierarchicalorderno')::int AS nlevel, "
				+ " child->>'parentKey' AS parentkey, " + " child->'nodes' AS children, "
				+ " sh.nsitehierarchyconfigcode "
				+ " FROM site_hierarchy sh, jsonb_array_elements(sh.children) AS child " + " ) SELECT "
				+ " nsitecode::int, " + " split_part(ssitename, '(', 1) AS ssitename, "
				+ " ssitetypename, parentkey::int, nlevel, nsitehierarchyconfigcode " + " FROM site_hierarchy "
				+ " WHERE parentkey::int = " + userInfo.getNtranssitecode()
				+ "   OR parentkey::int IN ( SELECT nsitecode::int FROM site_hierarchy WHERE parentkey::int = "
				+ userInfo.getNtranssitecode() + " ) " + " ORDER BY nsitecode::int ";

		final List<SampleScheduling> childList = jdbcTemplate.query(strChild, new SampleScheduling());

		// -------------------------------
		// Output to frontend
		// -------------------------------
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		if (!currentList.isEmpty()) {
			outputMap.put("currentList", currentList);
			outputMap.put("parentList", parentList);
			outputMap.put("childList", childList);

		} else {
			outputMap.put("currentList", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	// added condition by sujatha ATE_274 SWSM-117 to throw alert when click add location if the logged in site is not in approved site configuration
		else {
			return new ResponseEntity<>(
	                commonFunction.getMultilingualMessage("IDS_NOTINSITEHIERARCHYCONFIGURATION", userInfo.getSlanguagefilename()),
	                HttpStatus.EXPECTATION_FAILED
	            );					
	       }
	}

//	@SuppressWarnings("unused")
//	private void addToLevelMap(Map<Integer, List<SampleScheduling>> levelMap, List<SampleScheduling> data) {
//		if (data != null) {
//			for (SampleScheduling item : data) {
//				int level = item.getNlevel();
//				levelMap.computeIfAbsent(level, k -> new ArrayList<>()).add(item);
//			}
//		}
//	}

	/**
	 * This method provides access to the DAO layer to retrieve all the available
	 * districts for a given City within a specific District.
	 *
	 * @param nprimarykey an integer representing the unique region code for which
	 *                    the district list is to be fetched.
	 * @param userInfo    [UserInfo] object containing the logged-in user details,
	 *                    including nmasterSiteCode [int], which is the primary key
	 *                    of the site object.
	 * @return a ResponseEntity containing the list of district records.
	 * @throws Exception if any errors occur in the DAO layer during data retrieval.
	 */

	//modified by sujatha ATE_274 SWSM-117 selecting one more field nsitehierarchyconfigcode for use in village get
	@Override
	public ResponseEntity<Object> getSubDivisionalLab(final int nprimarykey, final UserInfo userInfo) throws Exception {
		String strVillage = "WITH RECURSIVE site_hierarchy AS ( " + " SELECT "
				+ " (elem->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (elem->>'parentKey')::int AS parentkey, nsitehierarchyconfigcode, " + " elem->'nodes' AS children "
				+ " FROM sitehierarchyconfig sc, jsonb_array_elements(sc.jsondata->'nodes') AS elem "
				+ " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " UNION ALL " + " SELECT "
				+ " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (child->>'parentKey')::int AS parentkey, nsitehierarchyconfigcode, " + " child->'nodes' AS children "
				+ " FROM site_hierarchy sh, jsonb_array_elements(sh.children) AS child " + ") "
				+ "SELECT nsitecode, split_part(ssitename, '(', 1) AS ssitename, ssitetypename, parentkey, nsitehierarchyconfigcode "
				+ "FROM site_hierarchy " + "WHERE parentkey = " + nprimarykey + " ORDER BY nsitecode;";
		final List<SampleScheduling> villageList = (List<SampleScheduling>) jdbcTemplate.query(strVillage,
				new SampleScheduling());
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		if (!villageList.isEmpty()) {
			outputMap.put("villageList", villageList);
		} else {
			outputMap.put("villageList", null);
		}
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method will access the DAO layer that is used to get all the available
	 * Region based on specific District
	 * 
	 * @param ndistrictCode Holding the current District record
	 * @param userInfo      [UserInfo] holding logged in user details and
	 *                      nmasterSiteCode [int] primary key of site object for
	 *                      which the list is to be fetched
	 * @return a response entity which holds the list of Districts with respect to
	 *         Region
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getDistrictLab(final int ndistrictCode, final UserInfo userInfo) throws Exception {
		String strTaluka = "WITH RECURSIVE site_hierarchy AS ( " + " SELECT "
				+ " (elem->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " elem->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (elem->>'parentKey')::int AS parentkey, " + " elem->'nodes' AS children "
				+ " FROM sitehierarchyconfig sc, jsonb_array_elements(sc.jsondata->'nodes') AS elem "
				+ " WHERE sc.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " UNION ALL " + " SELECT "
				+ " (child->'item'->'selectedNodeDetail'->>'nsitecode')::int AS nsitecode, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitename' AS ssitename, "
				+ " child->'item'->'selectedNodeDetail'->>'ssitetypename' AS ssitetypename, "
				+ " (child->>'parentKey')::int AS parentkey, " + " child->'nodes' AS children "
				+ " FROM site_hierarchy sh, jsonb_array_elements(sh.children) AS child " + ") "
				+ "SELECT nsitecode, split_part(ssitename, '(', 1) AS ssitename, ssitetypename, parentkey "
				+ "FROM site_hierarchy " + "WHERE parentkey = " + ndistrictCode + " ORDER BY nsitecode;";

		final List<SampleScheduling> talukaList = (List<SampleScheduling>) jdbcTemplate.query(strTaluka,
				new SampleScheduling());

		final Map<String, Object> outputMap = new LinkedHashMap<>();
		if (!talukaList.isEmpty()) {
			outputMap.put("talukaList", talukaList);
		} else {
			outputMap.put("talukaList", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method will access the DAO layer that is used to get all the available
	 * Location based on specific Village
	 * 
	 * @param nprimarykey Holding the current District record
	 * @param userInfo    [UserInfo] holding logged in user details and
	 *                    nmasterSiteCode [int] primary key of site object for which
	 *                    the list is to be fetched
	 * @return a response entity which holds the list of Districts with respect to
	 *         Region
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> getLocation(final int nprimarykey, final UserInfo userInfo) throws Exception {

		String strLocation = "select  sl.ssamplelocationname,sl.nsamplelocationcode  from samplelocation sl where sl.nvillagecode ="
				+ nprimarykey + " and  nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and  nsitecode= " + userInfo.getNmastersitecode();
		final List<SampleLocation> LocationList = (List<SampleLocation>) jdbcTemplate.query(strLocation,
				new SampleLocation());
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		if (!LocationList.isEmpty()) {
			outputMap.put("LocationList", LocationList);
		} else {
			outputMap.put("LocationList", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	/**
	 * This method will access the DAO layer that is used to delete an entry in
	 * sampleschedulinglocation table.
	 * 
	 * @param inputMap holding the date fromYear,toYear,Status
	 *                 sampleschedulinglocation details and userInfo [UserInfo]
	 *                 holding logged in user details and nmasterSiteCode [int]
	 *                 primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and data of deleted
	 *         sampleschedulinglocation object
	 * @throws Exception that are thrown in the DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteSampleSchedulingLocation(Map<String, Object> inputMap, UserInfo objUserInfo)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		SampleSchedulingLocation objSampleScheduling = objMapper.convertValue(inputMap.get("sampleSchedulingLocation"),
				SampleSchedulingLocation.class);

		final SampleScheduling sampleScheduling = getActiveSampleSchedulingById(
				objSampleScheduling.getNsampleschedulingcode(), objUserInfo);
		if (sampleScheduling == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		//SWSM-102 Added by Mullai Balaji V for only draft Record to delete
		if(sampleScheduling.getNtransactionstatus()!=Enumeration.TransactionStatus.DRAFT.gettransactionstatus())
		{
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);			
		}

		final SampleSchedulingLocation objSampleSchedulingLocation = getAuditSampleSchedulingLocationById(
				objSampleScheduling.getNsampleschedulinglocationcode(), objUserInfo);
		if (objSampleSchedulingLocation == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final List<Object> deletedSampleScheduling = new ArrayList<>();
		final List<String> multilingualIdList = new ArrayList<>();
		String updateQueryString = "UPDATE sampleschedulinglocation SET dmodifieddate='"
				+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', nstatus = "
				+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " WHERE nsampleschedulingcode="
				+ objSampleScheduling.getNsampleschedulingcode() + " and nsampleschedulinglocationcode = "
				+ objSampleScheduling.getNsampleschedulinglocationcode() + ";";
		jdbcTemplate.execute(updateQueryString);
		objSampleScheduling.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
		deletedSampleScheduling.add(objSampleScheduling);
		multilingualIdList.add("IDS_DELETESAMPLESCHEDULINGLOCATION");
		auditUtilityFunction.fnInsertAuditAction(deletedSampleScheduling, 1, null, multilingualIdList, objUserInfo);
		return getAllSampleSchedulingLocation(objSampleSchedulingLocation.getNsampleschedulingcode(), objUserInfo);
	}

	/**
	 * This method provides access to the DAO layer to retrieve all available
	 * samplescheduling for a specific site, filtered by date range and status.
	 *
	 * @param inputMap a map containing filter criteria such as fromYear, toYear,
	 *                 and status
	 * @param userInfo an instance of [UserInfo] containing the logged-in user
	 *                 details and the nmasterSiteCode, which is the primary key of
	 *                 the site for which the complaints are to be fetched
	 *
	 * @return a ResponseEntity containing the list of samplescheduling records
	 *         associated with the specified site
	 *
	 * @throws Exception if any errors occur in the DAO layer
	 */

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getSampleSchedulingDatas(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<>();
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		SampleScheduling objSampleScheduling = objMapper.convertValue(inputMap.get("samplescheduling"),
				SampleScheduling.class);
		final String strQuery = "SELECT s.nsampleschedulingcode, s.sfromyear, s.speriod, s.sdescription, "
				+ " s.nsitecode, s.nstatus, sh.ntransactionstatus, "
				+ " COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus,f.ncolorcode,cl.scolorhexcode "
				+ "FROM samplescheduling s "
				+ "JOIN sampleschedulinghistory sh ON sh.nsampleschedulingcode = s.nsampleschedulingcode "
				+ "JOIN transactionstatus ts1 ON sh.ntransactionstatus = ts1.ntranscode "
				+ "JOIN formwisestatuscolor f ON sh.ntransactionstatus = f.ntranscode "
				+ "JOIN colormaster cl ON cl.ncolorcode = f.ncolorcode "
				+ "WHERE sh.nsampleschedulinghistorycode = ANY ( " + "   SELECT MAX(sh2.nsampleschedulinghistorycode) "
				+ "   FROM sampleschedulinghistory sh2 "
				+ "   JOIN samplescheduling s2 ON s2.nsampleschedulingcode = sh2.nsampleschedulingcode "
				+ "   WHERE s2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "   AND sh2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "   AND s2.nsitecode = " + userInfo.getNmastersitecode() + " " + "   AND sh2.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + "   GROUP BY s2.nsampleschedulingcode " + ") "
				+ "AND s.nsitecode = " + userInfo.getNmastersitecode() + " " + "AND s.nsampleschedulingcode = "
				+ objSampleScheduling.getNsampleschedulingcode() + " " + "AND s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND sh.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + "AND sh.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND ts1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND f.nformcode="
				+ userInfo.getNformcode() + " " + "ORDER BY s.nsampleschedulingcode DESC";

		LOGGER.info("Get SampleScheduling Query: " + strQuery);

		final List<SampleScheduling> lstSampleScheduling = (List<SampleScheduling>) jdbcTemplate.query(strQuery,
				new SampleScheduling());

		if (!lstSampleScheduling.isEmpty()) {
			outputMap.put("selectedSampleScheduling", lstSampleScheduling.get(0));
			// outputMap.putAll((Map<String,
			// Object>)SampleSchedulingData(inputMap,userInfo));

			// outputMap.put("sampleSchedulingRecord",lstSampleScheduling);

			ResponseEntity<Object> response = SampleSchedulingData(inputMap, userInfo);
			outputMap.putAll((Map<String, Object>) response.getBody());

			outputMap.putAll(
					(Map<String, Object>) getSampleSchedulingFile(lstSampleScheduling.get(0).getNsampleschedulingcode(),
							userInfo).getBody());
			outputMap.putAll((Map<String, Object>) getAllSampleSchedulingLocation(
					lstSampleScheduling.get(0).getNsampleschedulingcode(), userInfo).getBody());
		} else {
			outputMap.put("sampleSchedulingRecord", null);
			outputMap.put("selectedSampleScheduling", null);
			outputMap.put("sampleSchedulingFile", null);
			outputMap.put("sampleSchedulingLocation", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> SampleSchedulingData(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<>();

		int fromYear;
		int toYear;

		if (inputMap.get("fromYear") != null && inputMap.get("toYear") != null) {
			fromYear = (int) inputMap.get("fromYear");
			toYear = (int) inputMap.get("toYear");
		} else {
			String currentUIDate = (String) inputMap.get("currentdate");
			if (currentUIDate != null && !currentUIDate.trim().isEmpty()) {
				int currentYear = Year.now().getValue();
				fromYear = currentYear;
				toYear = currentYear;
			} else {
				int currentYear = Year.now().getValue();
				fromYear = currentYear;
				toYear = currentYear;
			}
		}

		outputMap.put("fromYear", fromYear);
		outputMap.put("toYear", toYear);
		int ntransactionstatus = 0;
		if (inputMap.containsKey("ntranscode")) {
			ntransactionstatus = (int) inputMap.get("ntranscode");
		}

		String filterStatusQuery = "";
		if (ntransactionstatus > 0) {
			filterStatusQuery = " AND sh.ntransactionstatus = " + ntransactionstatus + " ";
		}

		final String strQuery = "SELECT s.nsampleschedulingcode, s.sfromyear, s.speriod, s.sdescription, "
				+ " s.nsitecode, s.nstatus, sh.ntransactionstatus, "
				+ " COALESCE(ts1.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ " ts1.jsondata->'stransdisplaystatus'->>'en-US') AS stransdisplaystatus,f.ncolorcode,cl.scolorhexcode "
				+ "FROM samplescheduling s "
				+ "JOIN sampleschedulinghistory sh ON sh.nsampleschedulingcode = s.nsampleschedulingcode "
				+ "JOIN transactionstatus ts1 ON sh.ntransactionstatus = ts1.ntranscode "
				+ "JOIN formwisestatuscolor f ON sh.ntransactionstatus = f.ntranscode "
				+ "JOIN colormaster cl ON cl.ncolorcode = f.ncolorcode "
				+ "WHERE sh.nsampleschedulinghistorycode = ANY ( " + "   SELECT MAX(sh2.nsampleschedulinghistorycode) "
				+ "   FROM sampleschedulinghistory sh2 "
				+ "   JOIN samplescheduling s2 ON s2.nsampleschedulingcode = sh2.nsampleschedulingcode "
				+ "   WHERE s2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "   AND sh2.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "   AND s2.nsitecode = " + userInfo.getNmastersitecode() + " " + "   AND sh2.nsitecode = "
				+ userInfo.getNmastersitecode() + " " + "   GROUP BY s2.nsampleschedulingcode " + ") "
				+ "AND s.sfromyear::int BETWEEN " + fromYear + " AND " + toYear + " "
				+ "AND s.nsampleschedulingcode > 0 " + "AND s.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ "AND s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "AND sh.nsitecode = " + userInfo.getNmastersitecode() + " " + "AND sh.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND ts1.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " AND f.nformcode="
				+ userInfo.getNformcode() + " " + filterStatusQuery + "ORDER BY s.nsampleschedulingcode DESC";

		LOGGER.info("Get SampleScheduling Query: " + strQuery);
		final List<SampleScheduling> lstSampleScheduling = (List<SampleScheduling>) jdbcTemplate.query(strQuery,
				new SampleScheduling());
		if (!lstSampleScheduling.isEmpty()) {
			outputMap.put("sampleSchedulingRecord", lstSampleScheduling);
		} else {
			outputMap.put("sampleSchedulingRecord", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

}