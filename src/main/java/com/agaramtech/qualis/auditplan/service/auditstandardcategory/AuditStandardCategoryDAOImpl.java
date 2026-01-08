package com.agaramtech.qualis.auditplan.service.auditstandardcategory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.auditplan.model.AuditStandardCategory;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "auditstandardcategory" table by 
 * implementing methods from its interface. 
 */
/**
 * @author sujatha.v SWSM-1 19/07/2025
 */
@Repository
@AllArgsConstructor
public class AuditStandardCategoryDAOImpl implements AuditStandardCategoryDAO {

	private final static Logger LOGGER = LoggerFactory.getLogger(AuditStandardCategoryDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final CommonFunction commonFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private ValidatorDel validatorDel;

	/**
	 * This method is used to retrieve list of all available auditstandardcategory's
	 * for the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         auditstandardcategory's
	 * @throws Exception that are thrown from this DAO layer
	 */
	//Modified by sonia on 2nd sept 2025 for jira id:SWSM-12
	@Override
	public ResponseEntity<Object> getAuditStandardCategory(final UserInfo userInfo) throws Exception {
		final String strQuery = "select a.nauditstandardcatcode, a.sauditstandardcatname,a.sauditremainderdays, a.sdescription, a.dmodifieddate, "
				+ " a.nsitecode, a.nstatus from auditstandardcategory a"
				+ " where nauditstandardcatcode > 0 and a.nsitecode= " + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		LOGGER.info("getAuditStandardCategory() called " + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new AuditStandardCategory()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active auditstandardcategory object based on
	 * the specified nauditstandardcatcode.
	 * 
	 * @param nauditstandardcatcode [int] primary key of auditstandardcategory
	 *                              object
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         auditstandardcategory object
	 * @throws Exception that are thrown from this DAO layer
	 */
	//Modified by sonia on 2nd sept 2025 for jira id:SWSM-12
	@Override
	public AuditStandardCategory getActiveAuditStandardCategoryById(final int nauditstandardcatcode,
			final UserInfo userInfo) throws Exception {
		final String strQuery = "select a.nauditstandardcatcode, a.sauditstandardcatname, a.sauditremainderdays, a.sdescription, "
				+ " a.dmodifieddate, a.nsitecode, a.nstatus from auditstandardcategory a "
				+ " where a.nauditstandardcatcode= " + nauditstandardcatcode + " and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (AuditStandardCategory) jdbcUtilityTemplateFunction.queryForObject(strQuery, AuditStandardCategory.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to auditstandardcategory table.
	 * AuditStandardCategory Name is unique across the database. Need to check for
	 * duplicate entry of auditstandardcategory name for the specified site before
	 * saving into database. *
	 * 
	 * @param objAuditStandardCategory [AuditStandardCategory] object holding
	 *                                 details to be added in auditstandardcategory
	 *                                 table
	 * @param userInfo                 [UserInfo] holding logged in user details
	 *                                 based on which the list is to be fetched
	 * @return saved auditstandardcategory object with status code 200 if saved
	 *         successfully else if the auditstandardcategory already exists,
	 *         response will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	//Modified by sonia on 2nd sept 2025 for jira id:SWSM-12
	@Override
	public ResponseEntity<Object> createAuditStandardCategory(final AuditStandardCategory objAuditStandardCategory,
			final UserInfo userInfo) throws Exception {
		final AuditStandardCategory auditStandardCategoryByName = getAuditStandardCategoryByName(
				objAuditStandardCategory.getSauditstandardcatname(), userInfo.getNmastersitecode());

		if (auditStandardCategoryByName == null) {
			final String sQuery = " lock table auditstandardcategory "
					+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			final List<String> multilingualIdList = new ArrayList<>();
			final List<Object> saveAuditStandardCatList = new ArrayList<>();

			String sequenceNoQuery = " select nsequenceno from seqnoauditplan where stablename ='auditstandardcategory'";
			int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
			nsequenceNo++;
			final String insertQuery = " insert into auditstandardcategory (nauditstandardcatcode, sauditstandardcatname,sauditremainderdays, sdescription, "
					+ "dmodifieddate, nsitecode, nstatus )" + " values(" + nsequenceNo + ",N'"
					+ stringUtilityFunction.replaceQuote(objAuditStandardCategory.getSauditstandardcatname()) + "' ,N'"
										+ stringUtilityFunction.replaceQuote(objAuditStandardCategory.getSauditremainderdays()) + "' ,N'"
					+ stringUtilityFunction.replaceQuote(objAuditStandardCategory.getSdescription()) + "' ,'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " )";
			jdbcTemplate.execute(insertQuery);

			final String updateQuery = " update seqnoauditplan set nsequenceno= " + nsequenceNo
					+ " where stablename='auditstandardcategory'";
			jdbcTemplate.execute(updateQuery);

			objAuditStandardCategory.setNauditstandardcatcode(nsequenceNo);
			saveAuditStandardCatList.add(objAuditStandardCategory);

			multilingualIdList.add("IDS_ADDAUDITSTANDARDCATEGORY");

			auditUtilityFunction.fnInsertAuditAction(saveAuditStandardCatList, 1, null, multilingualIdList, userInfo);
			return getAuditStandardCategory(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the auditstandardcategory object for the
	 * specified auditstandardcategory name and site.
	 * 
	 * @param sauditstandardcatname [String] name of the auditstandardcategory
	 * @param nmasterSiteCode       [int] site code of the auditstandardcategory
	 * @return auditStandardCategory object based on the specified
	 *         auditstandardcategory name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private AuditStandardCategory getAuditStandardCategoryByName(final String sauditstandardcatname,
			final int nmasterSiteCode) throws Exception {
		final String strQuery = "select a.nauditstandardcatcode from auditstandardcategory a where a.sauditstandardcatname= N'"
				+ stringUtilityFunction.replaceQuote(sauditstandardcatname) + "' " + " and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nsitecode= " + nmasterSiteCode;
		return (AuditStandardCategory) jdbcUtilityTemplateFunction.queryForObject(strQuery, AuditStandardCategory.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to update entry in auditstandardcategory table. Need to
	 * validate that the auditstandardcategory object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * auditstandardcategory name for the specified site before saving into
	 * database.
	 * 
	 * @param objAuditStandardCategory [AuditStandardCategory] object holding
	 *                                 details to be updated in
	 *                                 auditstandardcategory table
	 * @param userInfo                 [UserInfo] holding logged in user details
	 *                                 based on which the list is to be fetched
	 * @return saved auditstandardcategory object with status code 200 if saved
	 *         successfully else if the auditstandardcategory already exists,
	 *         response will be returned as 'Already Exists' with status code 409
	 *         else if the unit to be updated is not available, response will be
	 *         returned as 'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	//Modified by sonia on 2nd sept 2025 for jira id:SWSM-12
	@Override
	public ResponseEntity<Object> updateAuditStandardCategory(final AuditStandardCategory objAuditStandardCategory,
			final UserInfo userInfo) throws Exception {
		final AuditStandardCategory auditStandardCategoryById = getActiveAuditStandardCategoryById(
				objAuditStandardCategory.getNauditstandardcatcode(), userInfo);
		if (auditStandardCategoryById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> listBeforeUpdate = new ArrayList<>();
			final List<Object> listAfterUpdate = new ArrayList<>();

			final String queryString = " select a.nauditstandardcatcode from auditstandardcategory a where a.sauditstandardcatname=N'"
					+ stringUtilityFunction.replaceQuote(objAuditStandardCategory.getSauditstandardcatname())
					+ "' and a.nauditstandardcatcode<> " + objAuditStandardCategory.getNauditstandardcatcode()
					+ " and a.nsitecode= " + objAuditStandardCategory.getNsitecode() + " and a.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final AuditStandardCategory availableAuditStandardCat = (AuditStandardCategory) jdbcUtilityTemplateFunction
					.queryForObject(queryString, AuditStandardCategory.class, jdbcTemplate);
			if (availableAuditStandardCat == null) {
				final String updateQuery = " update auditstandardcategory set sauditstandardcatname=N'"	+ stringUtilityFunction.replaceQuote(objAuditStandardCategory.getSauditstandardcatname())+"',"
										 + " sauditremainderdays=N'"+ stringUtilityFunction.replaceQuote(objAuditStandardCategory.getSauditremainderdays()) + "',"
										 + " sdescription=N'"+ stringUtilityFunction.replaceQuote(objAuditStandardCategory.getSdescription())+ "', "
										 + " dmodifieddate= '" + dateUtilityFunction.getCurrentDateTime(userInfo)+ "' "
										 + " where nauditstandardcatcode= " + objAuditStandardCategory.getNauditstandardcatcode();

				jdbcTemplate.execute(updateQuery);
				listAfterUpdate.add(objAuditStandardCategory);
				listBeforeUpdate.add(auditStandardCategoryById);

				multilingualIDList.add("IDS_EDITAUDITSTANDARDCATEGORY");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				return getAuditStandardCategory(userInfo);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete auditstandardcategor, only if exits else throws
	 * Alert 'Already Deleted'
	 * 
	 * @param objAuditStandardCategory [AuditStandardCategory] an Object holds the
	 *                                 record to be deleted
	 * @param userInfo                 [UserInfo] holding logged in user details
	 *                                 based on which the list is to be fetched
	 * @return a response entity with list of available auditstandardcategory
	 *         objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteAuditStandardCategory(final AuditStandardCategory objAuditStandardCategory,
			final UserInfo userInfo) throws Exception {
		final AuditStandardCategory activeAuditStandardCategory = getActiveAuditStandardCategoryById(
				objAuditStandardCategory.getNauditstandardcatcode(), userInfo);
		if (activeAuditStandardCategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<String> multilingualIDList = new ArrayList<>();
			//added by sujatha ATE_274 for delete validation on auditplan SWSM-22 12-09-2025
			final String query = " Select 'IDS_AUDITPLAN' as Msg from auditplan where nauditstandardcatcode= "
					+ objAuditStandardCategory.getNauditstandardcatcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//					+ " and nsitecode="+ userInfo.getNtranssitecode(); //commented by sujatha ATE_274 01-10-2025 for delete validation in different site will not through alert issue
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(objAuditStandardCategory.getNauditstandardcatcode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<Object> deletedAuditStandardCategoryList = new ArrayList<>();
				final String updateQuery = " update auditstandardcategory set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' , nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nauditstandardcatcode="
						+ objAuditStandardCategory.getNauditstandardcatcode();

				jdbcTemplate.execute(updateQuery);
				objAuditStandardCategory
						.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedAuditStandardCategoryList.add(objAuditStandardCategory);
				multilingualIDList.add("IDS_DELETEAUDITSTANDARDCATEGORY");

				auditUtilityFunction.fnInsertAuditAction(deletedAuditStandardCategoryList, 1, null, multilingualIDList,
						userInfo);
				return getAuditStandardCategory(userInfo);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
