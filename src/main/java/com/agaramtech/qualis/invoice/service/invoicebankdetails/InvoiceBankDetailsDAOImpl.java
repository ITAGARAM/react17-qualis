package com.agaramtech.qualis.invoice.service.invoicebankdetails;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.InvoiceBankDetails;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "InvoiceBankDetails" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class InvoiceBankDetailsDAOImpl implements InvoiceBankDetailsDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceBankDetailsDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available InvoiceBankDetailss for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         InvoiceBankDetailss
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getBankDetails(final UserInfo userInfo) throws Exception {

		final String strQuery = "SELECT ibd.*," + "COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', "
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') AS sdisplaystatus "
				+ " from invoicebankdetails ibd,transactionstatus ts " + " where ibd.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ibd.nsitecode="
				+ userInfo.getNmastersitecode() + " and ts.ntranscode = ibd.nactive ORDER BY ibd.sbankname desc";

		LOGGER.info("Get Method:" + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new InvoiceBankDetails()), HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get all the available
	 * InvoiceBankDetailss with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of InvoiceBankDetails records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	private List<InvoiceBankDetails> getBankDetails(String sbankname, String saccountno, UserInfo userInfo)
			throws Exception {
		final String strQuery = "select sbankname,saccountno" + " from invoicebankdetails where sbankname =N'"
				+ sbankname + "' and saccountno=N'" + saccountno + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();

		return (List<InvoiceBankDetails>) jdbcTemplate.query(strQuery, new InvoiceBankDetails());
	}

	/**
	 * This method is used to retrieve active invoice bank details object based on
	 * the specified nbankcode.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of InvoiceBankDetails records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	private InvoiceBankDetails getInvoiceBankDetailsByDefaultStatus(short nsitecode) throws Exception {
		final String strQuery = "select * from invoicebankdetails ibd" + " where ibd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ibd.nactive="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and ibd.nsitecode = " + nsitecode;
		return (InvoiceBankDetails) jdbcUtilityFunction.queryForObject(strQuery, InvoiceBankDetails.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to InvoiceBankDetails table. Bank Name
	 * is unique across the database. Need to check for duplicate entry of Bank name
	 * for the specified site before saving into database. * Need to check that
	 * there should be only one default InvoiceBankDetails for a site.
	 * 
	 * @param objBankDetails [InvoiceBankDetails] object holding details to be added
	 *                       in InvoiceBankDetails table
	 * @param userInfo       [UserInfo] holding logged in user details based on
	 *                       which the list is to be fetched
	 * @return saved BankDetails object with status code 200 if saved successfully
	 *         else if the InvoiceBankDetails already exists, response will be
	 *         returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createBankDetails(InvoiceBankDetails objBankDetails, UserInfo userInfo)
			throws Exception {

		final String sQuery = " lock  table invoicebankdetails "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedUnitList = new ArrayList<>();

		// get used for data is already exists or not
		final List<InvoiceBankDetails> BankDetails = getBankDetails(objBankDetails.getSbankname(),
				objBankDetails.getSaccountno(), userInfo);

		if (BankDetails.isEmpty()) {

			if (objBankDetails.getNactive() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

				final InvoiceBankDetails defaultBankDetails = getInvoiceBankDetailsByDefaultStatus(
						objBankDetails.getNsitecode());

				if (defaultBankDetails != null) {

					// Copy of object before update final InvoiceBankDetails

					final InvoiceBankDetails bankdetailBeforeSave = SerializationUtils.clone(defaultBankDetails);

					final List<Object> defaultbankdetailBeforeSave = new ArrayList<>();
					defaultbankdetailBeforeSave.add(bankdetailBeforeSave);

					defaultBankDetails
							.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

					final String updateQueryString = " update invoicebankdetails set nactive="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nbankcode ="
							+ defaultBankDetails.getNbankcode();
					jdbcTemplate.execute(updateQueryString);

					final List<Object> defaultbankdetailAfterSave = new ArrayList<>();
					defaultbankdetailAfterSave.add(defaultBankDetails);

					multilingualIDList.add("IDS_ADDINVOICEBANKDETAILS");

					auditUtilityFunction.fnInsertAuditAction(defaultbankdetailAfterSave, 2, defaultbankdetailBeforeSave,
							multilingualIDList, userInfo);
					multilingualIDList.clear();
				}

			}

			String sequencenoquery = "select nsequenceno from seqnoinvoice where stablename ='invoicebankdetails'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;

			String insertquery = "Insert into invoicebankdetails (nbankcode,sbankname,saccountno,sifsccode,saddress,sotherdetails,nusercode,nactive,dmodifieddate,nsitecode,nstatus) "
					+ " values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(objBankDetails.getSbankname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objBankDetails.getSaccountno()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objBankDetails.getSifsccode()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objBankDetails.getSaddress()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objBankDetails.getSotherdetails()) + "',"
					+ userInfo.getNusercode() + "," + objBankDetails.getNactive() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + "" + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);

			String updatequery = "update seqnoinvoice set nsequenceno =" + nsequenceno
					+ " where stablename='invoicebankdetails'";
			jdbcTemplate.execute(updatequery);

			objBankDetails.setNbankcode(nsequenceno);
			savedUnitList.add(objBankDetails);

			multilingualIDList.add("IDS_ADDINVOICEBANKDETAILS");
			auditUtilityFunction.fnInsertAuditAction(savedUnitList, 1, null, multilingualIDList, userInfo);

			return getBankDetails(userInfo);

		} else {
			// Conflict = 409 - Duplicate entry --getSlanguagetypecode
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to retrieve active InvoiceBankDetails object based on the
	 * specified bankcode. InvoiceBankDetailss with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of InvoiceBankDetails records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public InvoiceBankDetails getActiveBankDetailsById(int bankId, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		final String strQuery = "select ibd.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
				+ " from invoicebankdetails ibd,transactionstatus ts " + " where ibd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and ts.ntranscode = ibd.nactive" + " and ibd.nbankcode = " + bankId;

		return (InvoiceBankDetails) jdbcUtilityFunction.queryForObject(strQuery, InvoiceBankDetails.class,
				jdbcTemplate);

	}

	/**
	 * This method is used to update entry in InvoiceBankDetails table. Need to
	 * validate that the BankDetails object to be updated is active before updating
	 * details in database. Need to check for duplicate entry of bank name for the
	 * specified site before saving into database. Need to check that there should
	 * be only one default InvoiceBankDetails for a site
	 * 
	 * @param BankDetailsDAO [InvoiceBankDetails] object holding details to be
	 *                       updated in InvoiceBankDetails table
	 * @param userInfo       [UserInfo] holding logged in user details based on
	 *                       which the list is to be fetched
	 * @return saved BankDetails object with status code 200 if saved successfully
	 *         else if the BankDetails already exists, response will be returned as
	 *         'Already Exists' with status code 409 else if the BankDetails to be
	 *         updated is not available, response will be returned as 'Already
	 *         Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */

	public ResponseEntity<Object> updateBankDetails(InvoiceBankDetails objBankDetails, UserInfo userInfo)
			throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		final InvoiceBankDetails invoiceBankDetails = getActiveBankDetailsById(objBankDetails.getNbankcode(), userInfo);

		if (invoiceBankDetails == null) {
			// status code:205
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select nbankcode from invoicebankdetails where saccountno = '"
					+ stringUtilityFunction.replaceQuote(objBankDetails.getSbankname()) + "' and nbankcode <> "
					+ objBankDetails.getNbankcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();

			final List<InvoiceBankDetails> bankDetailList = (List<InvoiceBankDetails>) jdbcTemplate.query(queryString,
					new InvoiceBankDetails());

			if (bankDetailList.isEmpty()) {
				if (objBankDetails.getNactive() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

					final InvoiceBankDetails defaultBankDetails = getInvoiceBankDetailsByDefaultStatus(
							objBankDetails.getNsitecode());

					if (defaultBankDetails != null
							&& defaultBankDetails.getNbankcode() != objBankDetails.getNbankcode()) {

						final InvoiceBankDetails BankDetailsBeforeSave = SerializationUtils.clone(defaultBankDetails);
						listBeforeUpdate.add(BankDetailsBeforeSave);
						defaultBankDetails.setNactive((int) Enumeration.TransactionStatus.NO.gettransactionstatus());

						final String updateQueryString = " update invoicebankdetails set nactive="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nbankcode="
								+ defaultBankDetails.getNbankcode();
						jdbcTemplate.execute(updateQueryString);

						listAfterUpdate.add(defaultBankDetails);

					}

				}
				final String updateQueryString = "update invoicebankdetails set sbankname=N'"
						+ stringUtilityFunction.replaceQuote(objBankDetails.getSbankname()) + "', saccountno=N'"
						+ stringUtilityFunction.replaceQuote(objBankDetails.getSaccountno()) + "', sifsccode ='"
						+ stringUtilityFunction.replaceQuote(objBankDetails.getSifsccode()) + "', saddress ='"
						+ stringUtilityFunction.replaceQuote(objBankDetails.getSaddress()) + "', sotherdetails ='"
						+ stringUtilityFunction.replaceQuote(objBankDetails.getSotherdetails()) + "',dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ", nactive ="
						+ objBankDetails.getNactive() + " where nbankcode=" + objBankDetails.getNbankcode()
						+ " and nsitecode=" + userInfo.getNmastersitecode() + ";";

				jdbcTemplate.execute(updateQueryString);

				final InvoiceBankDetails invoiceBankDetailAfterEdit = getActiveBankDetailsById(
						objBankDetails.getNbankcode(), userInfo);

				listBeforeUpdate.add(invoiceBankDetails);
				listAfterUpdate.add(invoiceBankDetailAfterEdit);
				multilingualIDList.add("IDS_EDITINVOICEBANKDETAILS");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				return getBankDetails(userInfo);
			}

			else {
				// Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This interface declaration is used to get all the available
	 * InvoiceBankDetails with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of InvoiceBankDetails records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	private InvoiceBankDetails getbankdetailsByStatus(int nmasterSiteCode) throws Exception {
		String strQuery = "Select * from invoicebankdetails where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode=" + nmasterSiteCode
				+ " and nactive=" + Enumeration.TransactionStatus.YES.gettransactionstatus();
		return (InvoiceBankDetails) jdbcUtilityFunction.queryForObject(strQuery, InvoiceBankDetails.class,
				jdbcTemplate);
	}

	/**
	 * This method id used to delete an entry in InvoiceBankDetails table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as 'InvoiceBankDetails'
	 * 
	 * @param BankDetailsDAO [InvoiceBankDetails] an Object holds the record to be
	 *                       deleted
	 * @param userInfo       [UserInfo] holding logged in user details based on
	 *                       which the list is to be fetched
	 * @return a response entity with list of available InvoiceBankDetails objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteBankDetails(InvoiceBankDetails objBankDetails, UserInfo userInfo)
			throws Exception {
		// TODO Auto-generated method stub
		final List<Object> deletedUnitList = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();

		final InvoiceBankDetails BankDetails = getActiveBankDetailsById(objBankDetails.getNbankcode(), userInfo);

		if (BankDetails == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final InvoiceBankDetails bankdetails = getbankdetailsByStatus(objBankDetails.getNsitecode());
			if (bankdetails != null && (bankdetails).getNbankcode() == objBankDetails.getNbankcode()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ACTIVEROLECANNOTBEDELETED.getreturnstatus(),
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

			final String updateQueryString = "update invoicebankdetails set dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus = "
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nbankcode="
					+ objBankDetails.getNbankcode();

			jdbcTemplate.execute(updateQueryString);

			deletedUnitList.add(objBankDetails);
			multilingualIDList.add("IDS_DELETEINVOICEBANKDETAILS");
			auditUtilityFunction.fnInsertAuditAction(deletedUnitList, 1, null, multilingualIDList, userInfo);
		}
		return getBankDetails(userInfo);
	}

}
