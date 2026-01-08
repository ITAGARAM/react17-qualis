package com.agaramtech.qualis.biobank.service.thirdpartyusermapping;

import java.util.ArrayList;
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
import com.agaramtech.qualis.biobank.model.ThirdParty;
import com.agaramtech.qualis.biobank.model.ThirdPartyUserMapping;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class ThirdPartyUserMappingDAOImpl implements ThirdPartyUserMappingDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThirdPartyUserMappingDAOImpl.class);
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available thirdparty for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         thirdparty
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getThirdParty(final UserInfo userInfo) throws Exception {
//		BGSI-12	Added nisngs column by Vishakh(19/09/2025)
		final String strQuery = "SELECT t.nthirdpartycode, t.sthirdpartyname, t.saddress, t.sphonenumber, t.semail, t.sdescription, t.nisngs,t.nsitecode, t.nstatus, "
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'"+ userInfo.getSlanguagetypecode()+"', ts.jsondata->'stransdisplaystatus'->>'en-US') sisngs "
				+ "	FROM thirdparty t join transactionstatus ts on ts.ntranscode=t.nisngs and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"  where  t.nthirdpartycode > 0 and t.nsitecode = " + userInfo.getNmastersitecode()
				+ " and t.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1";
		LOGGER.info("Get Method" + strQuery);
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final List<ThirdParty> lstThirdPartyMapping = (List<ThirdParty>) jdbcTemplate.query(strQuery, new ThirdParty());

		if (!lstThirdPartyMapping.isEmpty()) {
			outputMap.put("ThirdPartyRecord", lstThirdPartyMapping);

			outputMap.put("lstThirdPartyMapping", getDetails(
					lstThirdPartyMapping.get(lstThirdPartyMapping.size() - 1).getNthirdpartycode(), userInfo));
			outputMap.put("selectedThirdPartyMasterRecord", lstThirdPartyMapping.get(lstThirdPartyMapping.size() - 1));

		} else {
			outputMap.put("ThirdPartyRecord", null);
			outputMap.put("selectedThirdPartyMasterRecord", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	/**
	 * This method is used to retrieve list of all available thirdparty for the
	 * specified site (data grid)
	 * 
	 * @param nthirdpartycode (primary key of the table) helps to get all details in
	 *                        the table
	 * @param userInfo        [UserInfo] holding logged in user details and
	 *                        nmasterSiteCode [int] primary key of site object for
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         thirdparty
	 * @throws Exception that are thrown from this DAO layer
	 */

	public List<ThirdPartyUserMapping> getDetails(final int nthirdpartycode,final UserInfo userInfo) throws Exception {

		final String strQuery = "SELECT " + "tpum.nthirdpartyusermappingcode, " + "tpum.nthirdpartycode, "
				+ "tpum.nuserrolecode, " + "tpum.nusercode, " + "tpum.nsitecode, " + "tpum.nstatus, "
				+ "CONCAT(u.sfirstname, ' ', u.slastname) AS susername, " + "ur.suserrolename "
				+ "FROM thirdpartyusermapping tpum "
				+ "JOIN users u ON tpum.nusercode = u.nusercode AND tpum.nsitecode = u.nsitecode "
				+ "JOIN userrole ur ON tpum.nuserrolecode = ur.nuserrolecode AND tpum.nsitecode = ur.nsitecode "
				+ "WHERE tpum.nstatus = 1 " + "AND u.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND ur.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND tpum.nthirdpartycode = "
				+ nthirdpartycode + " " + "AND tpum.nsitecode = " + userInfo.getNmastersitecode() + ";";

		return (List<ThirdPartyUserMapping>) jdbcTemplate.query(strQuery, new ThirdPartyUserMapping());
	}

	/**
	 * This method is used to retrieve list of all available thirdpartyusermapping
	 * for the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         thirdpartyusermapping
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> getUserRole(final int nthirdpartycode, final UserInfo userInfo) throws Exception {

		final ThirdParty thirdParty = getActiveThirdPartyById(nthirdpartycode, userInfo);
		if (thirdParty == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			// added the below condition by sujatha ATE_274 for getting the user role based on the nneedngsflow BGSI-185
			final String query="select nisngs from thirdparty  where nthirdpartycode="+nthirdpartycode
							  + " and nstatus ="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final ThirdParty isngs = jdbcTemplate.queryForObject(query, new ThirdParty());
		    String strQuery="";
			if(isngs.getNisngs()== Enumeration.TransactionStatus.NO.gettransactionstatus()) {

			       strQuery = "select u.nuserrolecode,u.suserrolename,u.sdescription, u.dmodifieddate, u.nsitecode,"
			    		 + " u.nstatus , uc.nneedthirdpartyflow from userrole u JOIN  userroleconfig uc "
			    		 + " ON uc.nuserrolecode = u.nuserrolecode where u.nuserrolecode > 0 and u.nsitecode= "
			    		 + userInfo.getNmastersitecode() + " and u.nstatus="
			    		 + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and uc.nneedthirdpartyflow = "
			    		 + Enumeration.TransactionStatus.YES.gettransactionstatus() + " and uc.nstatus = "
			    		 + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			} else {
				   strQuery = "select u.nuserrolecode,u.suserrolename,u.sdescription, u.dmodifieddate, u.nsitecode,"
					  	 + " u.nstatus , uc.nneedthirdpartyflow from userrole u JOIN  userroleconfig uc "
						 + " ON uc.nuserrolecode = u.nuserrolecode where u.nuserrolecode > 0 and u.nsitecode= "
						 + userInfo.getNmastersitecode() + " and u.nstatus="
						 + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and uc.nneedngsflow = "
						 + Enumeration.TransactionStatus.YES.gettransactionstatus() + " and uc.nstatus = "
						 + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			}
			return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new ThirdPartyUserMapping()), HttpStatus.OK);
		}
	}

	/**
	 * This method is used to retrieve list of all available thirdpartyusermapping
	 * for the specified site.
	 * 
	 * @param nthirdpartycode (primary key of the table) helps to get all details in
	 *                        the table
	 * @param nuserrolecode   to get the details from userrole table
	 * @param userInfo        [UserInfo] holding logged in user details and
	 *                        nmasterSiteCode [int] primary key of site object for
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         thirdpartyusermapping
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getUsers(final int nthirdpartycode, final int nuserrolecode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select "
//				+ "ur.nuserrolecode, "
				+ "CONCAT(us.sfirstname,' ',us.slastname) as susername, "
				+ "us.nusercode "
//				+ ", umr.nusermultirolecode, ur.suserrolename "
				+ "from usermultirole umr, userrole ur, users us, userssite usite "
				+ "where umr.nuserrolecode = ur.nuserrolecode " + "and usite.nusercode = us.nusercode "
				+ "and umr.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and umr.ntransactionstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and us.ntransactionstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and ur.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "and umr.nusersitecode = usite.nusersitecode " + "and usite.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and us.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ur.nuserrolecode = "
				+ nuserrolecode + " " 
//				+ "and usite.nsitecode = " + userInfo.getNsitecode() + " "
				+ "and us.nusercode not in (select tum.nusercode from thirdpartyusermapping tum "
				+ "where tum.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and tum.nthirdpartycode = " + nthirdpartycode 
				+ ") group by us.nusercode, susername order by nusercode";

		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new ThirdPartyUserMapping()), HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to thirdpartyusermapping table.
	 * thirdpartyusermapping Name is unique across the database. Need to check for
	 * duplicate entry of thirdpartyusermapping name for the specified site before
	 * saving into database. * Need to check that there should be only one default
	 * thirdpartyusermapping for a site.
	 * 
	 * @param nthirdpartycode (primary key of the table) helps to get all details in
	 *                        the table
	 * @param nuserrolecode   to get the details from userrole table
	 * @param userCodes       to get users details from user tables
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return saved thirdpartyusermapping object with status code 200 if saved
	 *         successfully else if the thirdpartyusermapping already exists,
	 *         response will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createUsers(final int nthirdpartycode,final int nuserrolecode, List<Integer> userCodes,
			final UserInfo userInfo) throws Exception {


		final String userCodeValues = userCodes.stream().map(String::valueOf).collect(Collectors.joining(", "));
		
		final String deleteValidation = "SELECT nthirdpartycode,nusercode,nuserrolecode,nsitecode,nstatus FROM thirdpartyusermapping "
				+ "WHERE  nthirdpartycode = " + nthirdpartycode + " AND nusercode IN (" + userCodeValues + ")"
				+ " AND  nuserrolecode = " + nuserrolecode + " AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";

		List<ThirdPartyUserMapping> lstThirdPartyMapping = (List<ThirdPartyUserMapping>) jdbcTemplate
				.query(deleteValidation, new ThirdPartyUserMapping());

		if (!lstThirdPartyMapping.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		} else {

		
		final String lockQuery = "LOCK TABLE thirdpartyusermapping "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(lockQuery);

		final String sequenceQuery = "SELECT nsequenceno FROM seqnobiobankmanagement WHERE stablename = 'thirdpartyusermapping'";
		final int baseSeq = jdbcTemplate.queryForObject(sequenceQuery, Integer.class);

		final String userCodeValue = userCodes.stream().map(code -> "(" + code + ")").collect(Collectors.joining(", "));

		final String insertQuery = "INSERT INTO thirdpartyusermapping ("
				+ "nthirdpartyusermappingcode, nthirdpartycode, nuserrolecode, nusercode, dmodifieddate, nsitecode, nstatus) "
				+ "SELECT " + "(" + baseSeq + " + ROW_NUMBER() OVER ()) AS nthirdpartyusermappingcode, "
				+ nthirdpartycode + ", " + nuserrolecode + ", " + "usercode_tbl.usercode, " + "'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "FROM (VALUES " + userCodeValue
				+ ") AS usercode_tbl(usercode) " + "WHERE NOT EXISTS ( "
				+ "SELECT 1 FROM thirdpartyusermapping existing " + "WHERE existing.nusercode = usercode_tbl.usercode "
				+ "AND existing.nthirdpartycode = " + nthirdpartycode + " " + "AND existing.nuserrolecode = "
				+ nuserrolecode + " " + "AND existing.nsitecode = " + userInfo.getNmastersitecode() + " "
				+ "AND existing.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

		int insertedRows = jdbcTemplate.update(insertQuery);

		String updateSequenceQuery = "UPDATE seqnobiobankmanagement SET nsequenceno = " + (baseSeq + insertedRows)
				+ " WHERE stablename = 'thirdpartyusermapping'";
		jdbcTemplate.update(updateSequenceQuery);

		final String addQuery = "SELECT nthirdpartycode,nuserrolecode FROM thirdpartyusermapping "
				+ "WHERE  nthirdpartycode = " + nthirdpartycode + " AND  nuserrolecode = " + nuserrolecode
				+ " AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " group by  nthirdpartycode,nuserrolecode ;";

		final ThirdPartyUserMapping objThirdPartyUserMappingAudit = jdbcTemplate.queryForObject(addQuery,
				new ThirdPartyUserMapping());

		final String userStrg = "SELECT STRING_AGG(CONCAT(sfirstname, ' ', slastname), ', ') AS susername "
				+ "FROM users " + "WHERE nusercode IN (" + userCodeValues + ") " + "AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND nsitecode = "
				+ userInfo.getNmastersitecode() + ";";

		String susernames = jdbcTemplate.queryForObject(userStrg, String.class);

		final String thirdpartyName = "SELECT sthirdpartyname FROM thirdparty WHERE nthirdpartycode ="
				+ nthirdpartycode;

		String spartyName = jdbcTemplate.queryForObject(thirdpartyName, String.class);
		objThirdPartyUserMappingAudit.setSthirdpartyname(spartyName);
		objThirdPartyUserMappingAudit.setSusername(susernames);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedTestList = new ArrayList<>();
		savedTestList.add(objThirdPartyUserMappingAudit);

		multilingualIDList.add("IDS_ADDUSERS");

		auditUtilityFunction.fnInsertAuditAction(savedTestList, 1, null, multilingualIDList, userInfo);

	// commentted & added by sujatha ATE_274 for an issue while saving getting last record inside the current record. BGSI-218
//		return getThirdParty(userInfo);
		return getthirdpartyuser(nthirdpartycode, userInfo);

		}
	}

	/**
	 * This method is used to add a new entry to thirdparty table. thirdparty Name
	 * is unique across the database. Need to check for duplicate entry of
	 * thirdparty name for the specified site before saving into database. * Need to
	 * check that there should be only one default thirdpartyusermapping for a site.
	 * 
	 * @param objThirdparty [thirdparty] object holding details to be added in
	 *                      thirdparty table
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return saved thirdparty object with status code 200 if saved successfully
	 *         else if the thirdparty already exists, response will be returned as
	 *         'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createThirdParty(final ThirdParty objThirdParty, final UserInfo userInfo)
			throws Exception {
		final ThirdParty thirdPartyUserMappingByName = getThirdPartyByName(objThirdParty.getSthirdpartyname(),
				userInfo.getNmastersitecode());

		if (thirdPartyUserMappingByName == null) {
			final String sQuery = " lock table thirdParty " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			final List<String> multilingualIdList = new ArrayList<>();
			final List<Object> saveThirdPartyUserMappingList = new ArrayList<>();

			String sequenceNoQuery = " select nsequenceno from seqnobiobankmanagement where stablename ='thirdparty'";
			int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
			nsequenceNo++;
//			BGSI-12	Added nisngs column by Vishakh(19/09/2025)
			final String insertQuery = "INSERT INTO thirdparty ("
					+ "nthirdpartycode,sthirdpartyname, saddress, sphonenumber, semail, sdescription, nisngs, dmodifieddate, nsitecode, nstatus) "
					+ "VALUES (" + nsequenceNo + ", N'"
					+ stringUtilityFunction.replaceQuote(objThirdParty.getSthirdpartyname()) + "', N'"
					+ stringUtilityFunction.replaceQuote(objThirdParty.getSaddress()) + "' ,N'"
					+ objThirdParty.getSphonenumber() + "', N'"
					+ stringUtilityFunction.replaceQuote(objThirdParty.getSemail()) + "', N'"
					+ stringUtilityFunction.replaceQuote(objThirdParty.getSdescription())+ "', "+ objThirdParty.getNisngs() 
					+ ", '"+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertQuery);

			final String updateQuery = " update seqnobiobankmanagement set nsequenceno= " + nsequenceNo
					+ " where stablename='thirdparty'";
			jdbcTemplate.execute(updateQuery);

			objThirdParty.setNthirdpartycode(nsequenceNo);
			saveThirdPartyUserMappingList.add(objThirdParty);

			multilingualIdList.add("IDS_ADDTHIRDPARTYUSER");
			auditUtilityFunction.fnInsertAuditAction(saveThirdPartyUserMappingList, 1, null, multilingualIdList,
					userInfo);
			return getThirdParty(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}

	}

	/**
	 * This method is used to fetch the thirdparty object for the specified
	 * thirdparty name and site.
	 * 
	 * @param sthirdpartyname [String] name of the thirdparty
	 * @param nmasterSiteCode [int] site code of the thirdparty
	 * @return thirdparty object based on the specified thirdparty name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private ThirdParty getThirdPartyByName(final String sthirdpartyname,final int nmasterSiteCode) throws Exception {
		final String strQuery = "select nthirdpartycode from thirdparty  where 	sthirdpartyname= N'"
				+ stringUtilityFunction.replaceQuote(sthirdpartyname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= " + nmasterSiteCode;
		return (ThirdParty) jdbcUtilityFunction.queryForObject(strQuery, ThirdParty.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in thirdparty table. Need to validate
	 * that the thirdparty object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of thirdparty name for the
	 * specified site before saving into database. Need to check that there should
	 * be only one default thirdparty for a site
	 * 
	 * @param objThirdparty [thirdparty] object holding details to be updated in
	 *                      thirdparty table
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return saved thirdparty object with status code 200 if saved successfully
	 *         else if the thirdparty already exists, response will be returned as
	 *         'Already Exists' with status code 409 else if the thirdparty to be
	 *         updated is not available, response will be returned as 'Already
	 *         Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	// added nisngsConfirmed parameter by sujatha ATE_274 for delete validation in specific scenario BGSI-218
	@Override
	public ResponseEntity<Object> updateThirdParty(final ThirdParty objThirdParty, final UserInfo userInfo, final String nisngsConfirmed)
			throws Exception {


		final ThirdParty thirdParty = getActiveThirdPartyById(objThirdParty.getNthirdpartycode(), userInfo);
		if (thirdParty == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}		
		else {

			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> listBeforeUpdate = new ArrayList<>();
			final List<Object> listAfterUpdate = new ArrayList<>();

			final String queryString = " select nthirdpartycode from thirdparty  where sthirdpartyname=N'"
					+ stringUtilityFunction.replaceQuote(objThirdParty.getSthirdpartyname())
					+ "' and nthirdpartycode <> " + objThirdParty.getNthirdpartycode() + " and nsitecode= "
					+ objThirdParty.getNsitecode() + " and nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final ThirdParty availableThirdPartyUserMapping = (ThirdParty) jdbcUtilityFunction
					.queryForObject(queryString, ThirdParty.class, jdbcTemplate);

			if (availableThirdPartyUserMapping == null) {
				final String updateQuery = " update thirdparty set sthirdpartyname=N'"
						+ stringUtilityFunction.replaceQuote(objThirdParty.getSthirdpartyname()) + "',saddress=N'"
						+ stringUtilityFunction.replaceQuote(objThirdParty.getSaddress()) + "',sphonenumber=N'"
						+ objThirdParty.getSphonenumber() + "', semail=N'"
						+ stringUtilityFunction.replaceQuote(objThirdParty.getSemail()) + "' , sdescription=N'"
						+ stringUtilityFunction.replaceQuote(objThirdParty.getSdescription()) + "', nisngs="
						+ objThirdParty.getNisngs()+ ", dmodifieddate= '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nthirdpartycode= "
						+ objThirdParty.getNthirdpartycode();

				jdbcTemplate.execute(updateQuery);
				listAfterUpdate.add(objThirdParty);
				listBeforeUpdate.add(thirdParty);
				// added by sujatha ATE_274 for delete validation in specific scenario BGSI-218
				if(nisngsConfirmed == "true") {
					String strUpdate="update thirdpartyusermapping set nstatus="+Enumeration.TransactionStatus.DELETED.gettransactionstatus()
					       + " where nthirdpartycode in (select tm.nthirdpartycode from thirdpartyusermapping tm"
					       + " join thirdparty tp on tp.nthirdpartycode=tm.nthirdpartycode"
					       + " where tp.nthirdpartycode="+objThirdParty.getNthirdpartycode()
					       + " and tm.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					       + ") and nsitecode="+userInfo.getNmastersitecode();
					jdbcTemplate.execute(strUpdate);
				}
				multilingualIDList.add("IDS_EDITTHIRDPARTYUSER");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
			// commentted and added by sujatha ATE_274 BGSI-218 for an issue of getting the last record while saving
//				return getThirdParty(userInfo);
				return getthirdpartyuser(objThirdParty.getNthirdpartycode(), userInfo);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to retrieve active thirdparty object based on the
	 * specified nthirdpartyCode.
	 * 
	 * @param nthirdpartyCode [int] primary key of thirdparty object
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of thirdparty
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ThirdParty getActiveThirdPartyById(final int nthirdpartycode, final UserInfo userInfo) throws Exception {

		final String strQuery = "select nthirdpartycode, sthirdpartyname,saddress,sphonenumber,semail, dmodifieddate, nsitecode, nstatus,sdescription, nisngs from thirdparty "
				+ " where nthirdpartycode= " + nthirdpartycode + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (ThirdParty) jdbcUtilityFunction.queryForObject(strQuery, ThirdParty.class, jdbcTemplate);
	}

	/**
	 * This method id used to delete an entry in thirdparty table Need to check the
	 * record is already deleted or not Need to check whether the record is used in
	 * other tables such as 'thirdparty'
	 * 
	 * @param objThirdparty [thirdparty] an Object holds the record to be deleted
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return a response entity with list of available thirdparty objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteThirdParty(final ThirdParty objThirdParty, final UserInfo userInfo)
			throws Exception {

		final ThirdParty thirdParty = getActiveThirdPartyById(objThirdParty.getNthirdpartycode(), userInfo);
		if (thirdParty == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<Object> deletedThirdPartyUserMapping = new ArrayList<>();
			final List<String> multilingualIdList = new ArrayList<>();

			String updateQueryString = "update thirdparty set dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nthirdpartycode="
					+ objThirdParty.getNthirdpartycode() + ";";
			jdbcTemplate.execute(updateQueryString);

			objThirdParty.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			deletedThirdPartyUserMapping.add(objThirdParty);
			multilingualIdList.add("IDS_DELETETHIRDPARTYUSERROLE");

			auditUtilityFunction.fnInsertAuditAction(deletedThirdPartyUserMapping, 1, null, multilingualIdList,
					userInfo);
			updateQueryString = "UPDATE thirdpartyusermapping SET nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'  WHERE nthirdpartycode="
					+ objThirdParty.getNthirdpartycode() + "  ;";
			jdbcTemplate.execute(updateQueryString);
			return getThirdParty(userInfo);
		}
	}

	/**
	 * This method is used to retrieve active thirdparty object based on the
	 * specified nthirdpartyCode.
	 * 
	 * @param nthirdpartyCode [int] primary key of thirdparty object
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return response entity object holding response status and data of thirdparty
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getthirdpartyuser(final int nthirdpartycode,final UserInfo userInfo) throws Exception {
		// TODO

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final ThirdParty thirdParty = getActiveThirdPartyById(nthirdpartycode, userInfo);
		if (thirdParty == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
//			BGSI-12	Added nisngs column by Vishakh(19/09/2025)
			final String strQuery = "SELECT tp.nthirdpartycode,tp.sthirdpartyname, tp.saddress, tp.semail, tp.sphonenumber, tp.sdescription, tp.nisngs, "
					+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'"+ userInfo.getSlanguagetypecode()+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') sisngs "
					+ " FROM thirdparty tp join transactionstatus ts on ts.ntranscode=tp.nisngs and ts.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " WHERE tp.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND tp.nthirdpartycode = "
					+ nthirdpartycode + ";";

			final List<ThirdParty> lstThirdPartyMapping = (List<ThirdParty>) jdbcTemplate.query(strQuery,
					new ThirdParty());

			if (lstThirdPartyMapping != null) {
				outputMap.put("selectedThirdPartyMasterRecord", lstThirdPartyMapping.get(0));
				outputMap.put("lstThirdPartyMapping", getDetails(
						lstThirdPartyMapping.get(lstThirdPartyMapping.size() - 1).getNthirdpartycode(), userInfo));

			} else {
				outputMap.put("lstThirdPartyMapping", null);
				outputMap.put("selectedThirdPartyMasterRecord", null);
			}

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	/**
	 * This method id used to delete an entry in thirdparty table Need to check the
	 * record is already deleted or not Need to check whether the record is used in
	 * other tables such as 'thirdparty'
	 * 
	 * @param nthirdpartycode [thirdparty] an Object holds the record to be deleted
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @param nuserrolecode   to get the user role from userrole table
	 * @param nusercode       to get the users
	 * @return a response entity with list of available thirdparty objects
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteUserRoleAndUser(final int nthirdpartycode,final int nusercode,
			final int nuserrolecode,final UserInfo userInfo) throws Exception {

		final String deleteValidation = "SELECT nthirdpartycode,nusercode,nuserrolecode,nsitecode,nstatus FROM thirdpartyusermapping "
				+ "WHERE  nthirdpartycode = " + nthirdpartycode + " AND nusercode = " + nusercode
				+ " AND  nuserrolecode = " + nuserrolecode + " AND nstatus = "
				+ +Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";

		List<ThirdPartyUserMapping> lstThirdPartyMapping = (List<ThirdPartyUserMapping>) jdbcTemplate
				.query(deleteValidation, new ThirdPartyUserMapping());

		if (lstThirdPartyMapping.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String userStrg = "SELECT STRING_AGG(CONCAT(sfirstname, ' ', slastname), ', ') AS susername "
					+ "FROM users " + "WHERE nusercode = " + nusercode + " AND nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "AND nsitecode = "
					+ userInfo.getNmastersitecode() + ";";

			final String susernames = jdbcTemplate.queryForObject(userStrg, String.class);

			final String thirdpartyName = "SELECT sthirdpartyname FROM thirdparty WHERE nthirdpartycode ="
					+ nthirdpartycode;
			String spartyName = jdbcTemplate.queryForObject(thirdpartyName, String.class);

			ThirdPartyUserMapping mapping = lstThirdPartyMapping.get(0);
			mapping.setSthirdpartyname(spartyName);
			mapping.setSusername(susernames);

			final List<String> multilingualIdList = new ArrayList<>();
			final List<Object> deletedThirdPartyUserMapping = new ArrayList<>();

			multilingualIdList.add("IDS_DELETETHIRDPARTYUSERROLE");
			deletedThirdPartyUserMapping.add(mapping);

			final String deleteQuery = "UPDATE thirdpartyusermapping SET nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " " + "WHERE nthirdpartycode="
					+ nthirdpartycode + " AND nusercode=" + nusercode + " AND nuserrolecode=" + nuserrolecode + "  ;";

			jdbcTemplate.execute(deleteQuery);

			auditUtilityFunction.fnInsertAuditAction(deletedThirdPartyUserMapping, 1, null, multilingualIdList,
					userInfo);

			return getthirdpartyuser(nthirdpartycode, userInfo);

		}

	}
}
