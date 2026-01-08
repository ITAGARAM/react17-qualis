package com.agaramtech.qualis.auditplan.service.auditcategory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.auditplan.model.AuditCategory;
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
 * This class is used to perform CRUD Operation on "auditcategory" table by 
 * implementing methods from its interface. 
 */
/**
 * @author sujatha.v SWSM-3 19/07/2025
 */
@Repository
@AllArgsConstructor
public class AuditCategoryDAOImpl implements AuditCategoryDAO {

	private final static Logger LOGGER = LoggerFactory.getLogger(AuditCategoryDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final CommonFunction commonFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private ValidatorDel validatorDel;

	/**
	 * This method is used to retrieve list of all available auditcategory's for the
	 * specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         auditcategory's
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getAuditCategory(UserInfo userInfo) throws Exception {
		final String strQuery = "select a.nauditcategorycode, a.sauditcategoryname, a.sdescription, a.dmodifieddate, "
				+ " a.nsitecode, a.nstatus from auditcategory a" + " where nauditcategorycode > 0 and a.nsitecode= "
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		LOGGER.info("getAuditCategory() called " + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new AuditCategory()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active auditcategory object based on the
	 * specified nauditcategorycode.
	 * 
	 * @param nauditcategorycode [int] primary key of auditcategory object
	 * @param userInfo           [UserInfo] holding logged in user details based on
	 *                           which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         auditcategory object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public AuditCategory getActiveAuditCategoryById(int nauditcategorycode, UserInfo userInfo) throws Exception {
		final String strQuery = "select a.nauditcategorycode, a.sauditcategoryname, a.sdescription, "
				+ " a.dmodifieddate, a.nsitecode, a.nstatus from auditcategory a " + " where a.nauditcategorycode= "
				+ nauditcategorycode + " and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (AuditCategory) jdbcUtilityTemplateFunction.queryForObject(strQuery, AuditCategory.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to auditcategory table. AuditCategory
	 * Name is unique across the database. Need to check for duplicate entry of
	 * auditcategory name for the specified site before saving into database. *
	 * 
	 * @param objAuditCategory [AuditCategory] object holding details to be added in
	 *                         auditcategory table
	 * @param userInfo         [UserInfo] holding logged in user details based on
	 *                         which the list is to be fetched
	 * @return saved auditcategory object with status code 200 if saved successfully
	 *         else if the auditcategory already exists, response will be returned
	 *         as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createAuditCategory(AuditCategory objAuditCategory, UserInfo userInfo)
			throws Exception {
		final AuditCategory auditCategoryByName = getAuditCategoryByName(objAuditCategory.getSauditcategoryname(),
				userInfo.getNmastersitecode());

		if (auditCategoryByName == null) {
			final String sQuery = " lock table auditcategory "
					+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			final List<String> multilingualIdList = new ArrayList<>();
			final List<Object> saveAuditCategoryList = new ArrayList<>();

			String sequenceNoQuery = " select nsequenceno from seqnoauditplan where stablename ='auditcategory' "
					+ "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
			nsequenceNo++;
			final String insertQuery = " insert into auditcategory (nauditcategorycode, sauditcategoryname, sdescription, "
					+ "dmodifieddate, nsitecode, nstatus )" + " values(" + nsequenceNo + ",N'"
					+ stringUtilityFunction.replaceQuote(objAuditCategory.getSauditcategoryname()) + "' ,N'"
					+ stringUtilityFunction.replaceQuote(objAuditCategory.getSdescription()) + "' ,'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " )";
			jdbcTemplate.execute(insertQuery);

			final String updateQuery = " update seqnoauditplan set nsequenceno= " + nsequenceNo
					+ " where stablename='auditcategory' and nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(updateQuery);

			objAuditCategory.setNauditcategorycode(nsequenceNo);
			saveAuditCategoryList.add(objAuditCategory);

			multilingualIdList.add("IDS_ADDAUDITCATEGORY");

			auditUtilityFunction.fnInsertAuditAction(saveAuditCategoryList, 1, null, multilingualIdList, userInfo);
			return getAuditCategory(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the auditcategory object for the specified
	 * auditcategory name and site.
	 * 
	 * @param sauditcategoryname [String] name of the auditcategory
	 * @param nmasterSiteCode    [int] site code of the auditcategory
	 * @return auditCategory object based on the specified auditcategory name and
	 *         site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private AuditCategory getAuditCategoryByName(final String sauditcategoryname, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select a.nauditcategorycode from auditcategory a where a.sauditcategoryname= N'"
				+ stringUtilityFunction.replaceQuote(sauditcategoryname) + "' " + " and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nsitecode= " + nmasterSiteCode;
		return (AuditCategory) jdbcUtilityTemplateFunction.queryForObject(strQuery, AuditCategory.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in auditcategory table. Need to validate
	 * that the auditcategory object to be updated is active before updating details
	 * in database. Need to check for duplicate entry of auditcategory name for the
	 * specified site before saving into database.
	 * 
	 * @param objAuditCategory [AuditCategory] object holding details to be updated
	 *                         in auditcategory table
	 * @param userInfo         [UserInfo] holding logged in user details based on
	 *                         which the list is to be fetched
	 * @return saved auditcategory object with status code 200 if saved successfully
	 *         else if the auditcategory already exists, response will be returned
	 *         as 'Already Exists' with status code 409 else if the unit to be
	 *         updated is not available, response will be returned as 'Already
	 *         Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateAuditCategory(AuditCategory objAuditCategory, UserInfo userInfo)
			throws Exception {
		final AuditCategory auditCategoryById = getActiveAuditCategoryById(objAuditCategory.getNauditcategorycode(),
				userInfo);
		if (auditCategoryById == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> listBeforeUpdate = new ArrayList<>();
			final List<Object> listAfterUpdate = new ArrayList<>();

			final String queryString = " select a.nauditcategorycode from auditcategory a where a.sauditcategoryname=N'"
					+ stringUtilityFunction.replaceQuote(objAuditCategory.getSauditcategoryname())
					+ "' and a.nauditcategorycode<> " + objAuditCategory.getNauditcategorycode() + " and a.nsitecode= "
					+ userInfo.getNmastersitecode() + " and a.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final AuditCategory availableAuditCategory = (AuditCategory) jdbcUtilityTemplateFunction
					.queryForObject(queryString, AuditCategory.class, jdbcTemplate);
			if (availableAuditCategory == null) {
				final String updateQuery = " update auditcategory set sauditcategoryname=N'"
						+ stringUtilityFunction.replaceQuote(objAuditCategory.getSauditcategoryname())
						+ "', sdescription=N'" + stringUtilityFunction.replaceQuote(objAuditCategory.getSdescription())
						+ "', dmodifieddate= '" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nauditcategorycode= " + objAuditCategory.getNauditcategorycode() + " and nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				jdbcTemplate.execute(updateQuery);
				listAfterUpdate.add(objAuditCategory);
				listBeforeUpdate.add(auditCategoryById);

				multilingualIDList.add("IDS_EDITAUDITCATEGORY");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				return getAuditCategory(userInfo);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete auditcategory, only if exits else throws Alert
	 * 'Already Deleted'
	 * 
	 * @param objAuditCategory [AuditCategory] an Object holds the record to be
	 *                         deleted
	 * @param userInfo         [UserInfo] holding logged in user details based on
	 *                         which the list is to be fetched
	 * @return a response entity with list of available auditcategory objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteAuditCategory(AuditCategory objAuditCategory, UserInfo userInfo)
			throws Exception {
		final AuditCategory activeAuditCategory = getActiveAuditCategoryById(objAuditCategory.getNauditcategorycode(),
				userInfo);
		if (activeAuditCategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final List<String> multilingualIDList = new ArrayList<>();
			//added by sujatha ATE_274 for delete validation for auditplan SWSM-22 12-09-2025
			final String query = " Select 'IDS_AUDITPLAN' as Msg from auditplan where nauditcategorycode= "
					+ objAuditCategory.getNauditcategorycode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//					+ " and nsitecode="+ userInfo.getNtranssitecode();   //commented by sujatha ATE_274 01-10-2025 for delete validation in different site will not through alert issue
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport.validateDeleteRecord(
						Integer.toString(objAuditCategory.getNauditcategorycode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<Object> deletedAuditCategoryList = new ArrayList<>();
				final String updateQuery = " update auditcategory set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' , nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nauditcategorycode="
						+ objAuditCategory.getNauditcategorycode();

				jdbcTemplate.execute(updateQuery);
				objAuditCategory.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedAuditCategoryList.add(objAuditCategory);
				multilingualIDList.add("IDS_DELETEAUDITCATEGORY");

				auditUtilityFunction.fnInsertAuditAction(deletedAuditCategoryList, 1, null, multilingualIDList,
						userInfo);
				return getAuditCategory(userInfo);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
