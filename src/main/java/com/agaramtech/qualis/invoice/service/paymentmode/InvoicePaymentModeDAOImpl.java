package com.agaramtech.qualis.invoice.service.paymentmode;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.invoice.model.InvoicePaymentMode;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "invoicepaymentmode" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
@Transactional(rollbackFor = Exception.class)
public class InvoicePaymentModeDAOImpl implements InvoicePaymentModeDAO {
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final ProjectDAOSupport projectDAOSupport;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available invoicepaymentmode for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         invoicepaymentmode
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getInvoicePaymentMode(final UserInfo userinfo) throws Exception {
		final String str = "select * from invoicepaymentmode where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userinfo.getNmastersitecode() + " ORDER BY spaymentmode desc";

		return new ResponseEntity<Object>(jdbcTemplate.query(str, new InvoicePaymentMode()), HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to invoicepaymentmode table. payment
	 * Name is unique across the database. Need to check for duplicate entry of
	 * payment name for the specified site before saving into database. * Need to
	 * check that there should be only one default invoicepaymentmode for a site.
	 * 
	 * @param objInvoicePaymentMode [InvoicePaymentmode] object holding details to
	 *                              be added in invoicepaymentmode table
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return saved paymentmode object with status code 200 if saved successfully
	 *         else if the invoicepaymentmode already exists, response will be
	 *         returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> createInvoicePaymentMode(final InvoicePaymentMode objInvoicePaymentMode,
			final UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedInvoicePaymentModeList = new ArrayList<>();

		final String sQuery = "lock table InvoicePaymentMode "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<InvoicePaymentMode> invoicePaymentModeListByName = getInvoicePaymentModeListByName(
				objInvoicePaymentMode.getSpaymentmode(), objInvoicePaymentMode.getNsitecode());

		if (invoicePaymentModeListByName.isEmpty()) {
			final String sequencenoquery = "select nsequenceno from seqnoinvoice where stablename ='invoicepaymentmode'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;

			final String insertquery = "insert into invoicepaymentmode(npaymentcode,spaymentmode,nusercode,dmodifieddate,nsitecode,nstatus) values "
					+ "(" + nsequenceno + ",'"
					+ stringUtilityFunction.replaceQuote(objInvoicePaymentMode.getSpaymentmode()) + "',"
					+ userInfo.getNusercode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
					+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ")";

			jdbcTemplate.execute(insertquery);

			final String updatequery = "update seqnoinvoice set nsequenceno =" + nsequenceno
					+ " where stablename='invoicepaymentmode'";
			jdbcTemplate.execute(updatequery);

			objInvoicePaymentMode.setNpaymentcode(nsequenceno);
			savedInvoicePaymentModeList.add(objInvoicePaymentMode);

			multilingualIDList.add("IDS_ADDINVOICEPAYMENTMODE");

			auditUtilityFunction.fnInsertAuditAction(savedInvoicePaymentModeList, 1, null, multilingualIDList,
					userInfo);

			return getInvoicePaymentMode(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to update entry in invoicepaymentmode table. Need to
	 * validate that the paymentmode object to be updated is active before updating
	 * details in database. Need to check for duplicate entry of payment name for
	 * the specified site before saving into database. Need to check that there
	 * should be only one default invoicepaymentmode for a site
	 * 
	 * @param objInvoicePaymentMode [InvoicePaymentmode] object holding details to
	 *                              be updated in InvoicePaymentmode table
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return saved paymentmode object with status code 200 if saved successfully
	 *         else if the paymentmode already exists, response will be returned as
	 *         'Already Exists' with status code 409 else if the paymentmode to be
	 *         updated is not available, response will be returned as 'Already
	 *         Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateInvoicePaymentMode(final InvoicePaymentMode objInvoicePaymentMode,
			final UserInfo userInfo) throws Exception {

		final ResponseEntity<Object> InvoicePaymentMode = getActiveInvoicePaymentModeById(
				objInvoicePaymentMode.getNpaymentcode(), userInfo);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		if (InvoicePaymentMode == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select npaymentcode from invoicepaymentmode where spaymentmode = '"
					+ stringUtilityFunction.replaceQuote(objInvoicePaymentMode.getSpaymentmode())
					+ "' and npaymentcode <> " + objInvoicePaymentMode.getNpaymentcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();

			final List<InvoicePaymentMode> InvoicePaymentModeList = (List<InvoicePaymentMode>) jdbcTemplate
					.query(queryString, new InvoicePaymentMode());

			if (InvoicePaymentModeList.isEmpty()) {
				final String updateQueryString = "update invoicepaymentmode set spaymentmode='"
						+ stringUtilityFunction.replaceQuote(objInvoicePaymentMode.getSpaymentmode())
						+ "',dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where npaymentcode=" + objInvoicePaymentMode.getNpaymentcode() + ";";

				jdbcTemplate.execute(updateQueryString);
				listAfterUpdate.add(objInvoicePaymentMode);
				listBeforeUpdate.add(InvoicePaymentMode);

				multilingualIDList.add("IDS_EDITINVOICEPAYMENTMODE");

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				return getInvoicePaymentMode(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete an entry in invoicepaymentmode table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as 'invoicequotationheader','invoiceheader'
	 * 
	 * @param objInvoicePaymentMode [InvoicePaymentmode] an Object holds the record
	 *                              to be deleted
	 * @param userInfo              [UserInfo] holding logged in user details based
	 *                              on which the list is to be fetched
	 * @return a response entity with list of available invoicepaymentmode objects
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteInvoicePaymentMode(final InvoicePaymentMode objInvoicePaymentMode,
			final UserInfo userInfo) throws Exception {

		final ResponseEntity<Object> invoicepaymentmode = getActiveInvoicePaymentModeById(
				objInvoicePaymentMode.getNpaymentcode(), userInfo);
		if (invoicepaymentmode == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String query = "select 'IDS_TRANSACTION' as Msg from invoicequotationheader where npaymentmode="
					+ objInvoicePaymentMode.getNpaymentcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " union all select 'IDS_TRANSACTION' as Msg from invoiceheader where npaymentcode="
					+ objInvoicePaymentMode.getNpaymentcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();

			ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				objDeleteValidation = projectDAOSupport
						.validateDeleteRecord(Integer.toString(objInvoicePaymentMode.getNpaymentcode()), userInfo);
				if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedInvoicePaymentModeList = new ArrayList<>();

				final String updateQuery = "update invoicepaymentmode set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + "" + " where npaymentcode="
						+ objInvoicePaymentMode.getNpaymentcode();

				jdbcTemplate.execute(updateQuery);
				objInvoicePaymentMode.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				deletedInvoicePaymentModeList.add(objInvoicePaymentMode);
				multilingualIDList.add("IDS_DELETEINVOICEPAYMENTMODE");
				auditUtilityFunction.fnInsertAuditAction(deletedInvoicePaymentModeList, 1, null, multilingualIDList,
						userInfo);

				return getInvoicePaymentMode(userInfo);
			} else {
				return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to retrieve active invoicepaymentmode object based on the
	 * specified ninvoicepaymentmodeCode.
	 * 
	 * @param npaymentCode [int] primary key of invoicepaymentmode object
	 * @param userInfo     [UserInfo] holding logged in user details based on which
	 *                     the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         invoicepaymentmode object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveInvoicePaymentModeById(final int npaymentcode, final UserInfo userInfo)
			throws Exception {
		final String query = "select 'IDS_TRANSACTION' as Msg from invoicequotationheader where npaymentmode="
				+ npaymentcode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode=" + userInfo.getNmastersitecode()
				+ " union all select 'IDS_TRANSACTION' as Msg from invoiceheader where npaymentcode=" + npaymentcode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();

		ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);

		boolean validRecord = false;
		if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
			validRecord = true;
			objDeleteValidation = projectDAOSupport.validateDeleteRecord(Integer.toString(npaymentcode), userInfo);
			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
			} else {
				validRecord = false;
			}
		}

		final InvoicePaymentMode reportsettings;
		if (validRecord) {
			final String strQuery = "select spaymentmode from invoicepaymentmode where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode() + " and npaymentcode = " + npaymentcode;

			reportsettings = (InvoicePaymentMode) jdbcUtilityFunction.queryForObject(strQuery, InvoicePaymentMode.class,
					jdbcTemplate);
		} else {
			return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(reportsettings, HttpStatus.OK);
	}

	/**
	 * This method is used to fetch the paymentmode object for the specified payment
	 * name and site.
	 * 
	 * @param spaymentname    [String] name of the paymentmode
	 * @param nmasterSiteCode [int] site code of the paymentmode
	 * @return payment object based on the specified payment name and site
	 * @throws Exception that are thrown from this DAO layer
	 */

	private List<InvoicePaymentMode> getInvoicePaymentModeListByName(final String spaymentmode,
			final int nmasterSiteCode) throws Exception {
		final String strQuery = "select npaymentcode from invoicepaymentmode where spaymentmode = N'"
				+ stringUtilityFunction.replaceQuote(spaymentmode) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;

		return (List<InvoicePaymentMode>) jdbcTemplate.query(strQuery, new InvoicePaymentMode());
	}
}