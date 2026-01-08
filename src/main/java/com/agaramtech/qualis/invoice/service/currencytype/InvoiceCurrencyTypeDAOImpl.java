package com.agaramtech.qualis.invoice.service.currencytype;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.invoice.model.InvoiceCurrencyType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "invoicecurrencytype" table
 * by implementing methods from its interface.
 */
@AllArgsConstructor
@Repository

public class InvoiceCurrencyTypeDAOImpl implements InvoiceCurrencyTypeDAO {

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve list of all available invoicecurrencytype for
	 * the specified site.
	 *
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         invoicecurrencytype
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getInvoiceCurrencyType(final UserInfo userInfo) throws Exception {
		final String strQuery = "select ict.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus, to_char(ict.dmodifieddate, '"
				+ userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as smodifieddate"
				+ " from invoicecurrencytype ict,transactionstatus ts" + " where ict.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ict.ncurrencycode>0 and ts.ntranscode=ict.nactive and ict.nsitecode="
				+ userInfo.getNmastersitecode() + " ORDER BY scurrency desc";
		final List<String> lstcolumns = new ArrayList<>();
		lstcolumns.add("sdisplaystatus");
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new InvoiceCurrencyType()), HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to invoicecurrencytype table. currency
	 * name Name is unique across the database. Need to check for duplicate entry of
	 * currency name for the specified site before saving into database. * Need to
	 * check that there should be only one default invoicecurrencytype for a site.
	 *
	 * @param objinvoicecurrencytype [invoicecurrencytype] object holding details to
	 *                               be added in invoicecurrencytype table
	 * @param userInfo               [UserInfo] holding logged in user details based
	 *                               on which the list is to be fetched
	 * @return saved currency type object with status code 200 if saved successfully
	 *         else if the invoicecurrencytype already exists, response will be
	 *         returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createInvoiceCurrencyType(final InvoiceCurrencyType objInvoiceCurrencyType,
			final UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedinvoicecurrencyList = new ArrayList<>();

		final List<InvoiceCurrencyType> invoicecurrencyListByName = getInvoiceCurrencyListByName(
				objInvoiceCurrencyType.getScurrency(), objInvoiceCurrencyType.getSsymbol(),
				objInvoiceCurrencyType.getNsitecode());

		if (invoicecurrencyListByName.isEmpty()) {

			final String sequencenoquery = "select nsequenceno from seqnoinvoice   where stablename ='invoicecurrencytype'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;
			final String insertquery = "INSERT INTO invoicecurrencytype (ncurrencycode, scurrency, ssymbol, nactive, nusercode, dmodifieddate, nsitecode, nstatus) "
					+ "VALUES (" + nsequenceno + ", N'"
					+ stringUtilityFunction.replaceQuote(objInvoiceCurrencyType.getScurrency()) + "', N'"
					+ stringUtilityFunction.replaceQuote(objInvoiceCurrencyType.getSsymbol()) + "', "
					+ objInvoiceCurrencyType.getNactive() + ", " + userInfo.getNusercode() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

			jdbcTemplate.execute(insertquery);

			final String updatequery = "update seqnoinvoice set nsequenceno =" + nsequenceno
					+ " where stablename='invoicecurrencytype'";
			jdbcTemplate.execute(updatequery);

			objInvoiceCurrencyType.setNcurrencycode(nsequenceno);
			savedinvoicecurrencyList.add(objInvoiceCurrencyType);

			multilingualIDList.add("IDS_ADDCURRENCYTYPE");

			auditUtilityFunction.fnInsertAuditAction(savedinvoicecurrencyList, 1, null, multilingualIDList, userInfo);

			return getInvoiceCurrencyType(userInfo);

		}

		else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);

		}

	}

	/**
	 * This method is used to retrieve active invoicecurrencytype object based on
	 * the specified ninvoicecurrencytypeCode.
	 *
	 * @param userInfo [UserInfo] holding logged in user details based on which the
	 *                 list is to be fetched
	 * @return response entity object holding response status and data of
	 *         invoicecurrencytype object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings("unused")
	private InvoiceCurrencyType getInvoiceCurrencByDefaultStatus(final int nmasterSiteCode) throws Exception {
		final String strQuery = "select * from invoicecurrencytype ict" + " where ict.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ict.nactive="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and ict.nsitecode = " + nmasterSiteCode;
		return (InvoiceCurrencyType) jdbcUtilityFunction.queryForObject(strQuery, InvoiceCurrencyType.class,
				jdbcTemplate);

	}

	/**
	 * This method is used to retrieve active updated invoicecurrencytype object
	 * based on the specified ninvoicecurrencytypeCode.
	 *
	 * @param userInfo [UserInfo] holding logged in user details based on which the
	 *                 list is to be fetched
	 * @return response entity object holding response status and data of
	 *         invoicecurrencytype object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public InvoiceCurrencyType getActiveInvoiceCurrencyTypeByIdUpdate(final int ncurrencycode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "SELECT ict.*, " + "COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', "
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') AS sdisplaystatus "
				+ "FROM invoicecurrencytype ict, transactionstatus ts " + "WHERE ict.nsitecode = "
				+ userInfo.getNmastersitecode() + " AND ict.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND ts.ntranscode = ict.nactive"
				+ " AND ict.ncurrencycode = " + ncurrencycode;
		return (InvoiceCurrencyType) jdbcUtilityFunction.queryForObject(strQuery, InvoiceCurrencyType.class,
				jdbcTemplate);
	}

	/**
	 * This method is used to retrieve active invoicecurrencytype object based on
	 * the specified currency code.
	 *
	 * @param userInfo [UserInfo] holding logged in user details based on which the
	 *                 list is to be fetched
	 * @return response entity object holding response status and data of
	 *         invoicecurrencytype object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveInvoiceCurrencyTypeById(final int ncurrencycode, final UserInfo userInfo)
			throws Exception {

		final String query = "select 'IDS_TRANSACTION' as Msg from invoicequotationheader where ncurrencytype="
				+ ncurrencycode + " and " + "nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " union all" + " select 'IDS_TRANSACTION' as Msg from invoiceheader where ncurrencytype="
				+ ncurrencycode + " and " + " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nsitecode=" + userInfo.getNmastersitecode();
		;

		ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);
		boolean validRecord = false;
		if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
			validRecord = true;
			objDeleteValidation = projectDAOSupport.validateDeleteRecord(Integer.toString(ncurrencycode), userInfo);
			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
			} else {
				validRecord = false;
			}
		}
		InvoiceCurrencyType reportsettings = null;
		if (validRecord) {
			final String strQuery = "select ict.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
					+ " from invoicecurrencytype ict,transactionstatus ts " + " where ict.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ict.nsitecode="
					+ userInfo.getNmastersitecode() + " and ts.ntranscode = ict.nactive" + " and ict.ncurrencycode = "
					+ ncurrencycode;

			reportsettings = (InvoiceCurrencyType) jdbcUtilityFunction.queryForObject(strQuery,
					InvoiceCurrencyType.class, jdbcTemplate);
		} else {

			return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(reportsettings, HttpStatus.OK);

	}

	/**
	 * This method is used to update entry in invoicecurrencytype table. Need to
	 * validate that the currency type object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of Bank name
	 * for the specified site before saving into database. Need to check that there
	 * should be only one default invoicecurrencytype for a site
	 *
	 * @param CurrencyTypeDAO [invoicecurrencytype] object holding details to be
	 *                        updated in invoicecurrencytype table
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return saved currency type object with status code 200 if saved successfully
	 *         else if the currency type already exists, response will be returned
	 *         as 'Already Exists' with status code 409 else if the currency type to
	 *         be updated is not available, response will be returned as 'Already
	 *         Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> updateInvoiceCurrencyType(final InvoiceCurrencyType objInvoiceCurrencyType,
			final UserInfo userInfo) throws Exception {

		final InvoiceCurrencyType invoicecurrency = getActiveInvoiceCurrencyTypeByIdUpdate(
				objInvoiceCurrencyType.getNcurrencycode(), userInfo);
		final List<String> multilingualIDList = new ArrayList<>();

		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		if (invoicecurrency == null) {
			// status code:205
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String queryString = "select ncurrencycode from invoicecurrencytype where (ssymbol = '"
					+ stringUtilityFunction.replaceQuote(objInvoiceCurrencyType.getSsymbol()) + "' or scurrency ='"
					+ stringUtilityFunction.replaceQuote(objInvoiceCurrencyType.getScurrency())
					+ "') and ncurrencycode <> " + objInvoiceCurrencyType.getNcurrencycode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();

			final List<InvoiceCurrencyType> InvoiceCurrencyTypeList = (List<InvoiceCurrencyType>) jdbcTemplate
					.query(queryString, new InvoiceCurrencyType());

			final String queryStrings = "select nactive from invoicecurrencytype where ncurrencycode = "
					+ objInvoiceCurrencyType.getNcurrencycode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			final int nactivestatus = jdbcTemplate.queryForObject(queryStrings, Integer.class);

			if (InvoiceCurrencyTypeList.isEmpty()) {

				final String updateQueryString = "UPDATE invoicecurrencytype SET scurrency = N'"
						+ stringUtilityFunction.replaceQuote(objInvoiceCurrencyType.getScurrency()) + "', ssymbol = N'"
						+ stringUtilityFunction.replaceQuote(objInvoiceCurrencyType.getSsymbol()) + "', nactive = "
						+ objInvoiceCurrencyType.getNactive() + ", nsitecode = " + userInfo.getNmastersitecode()
						+ ", dmodifieddate = '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
						+ " WHERE ncurrencycode = " + objInvoiceCurrencyType.getNcurrencycode();

				jdbcTemplate.execute(updateQueryString);

				listAfterUpdate.add(objInvoiceCurrencyType);
				listBeforeUpdate.add(invoicecurrency);

				multilingualIDList.add("IDS_EDITCURRENCYTYPE");

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				return getInvoiceCurrencyType(userInfo);
			}

			else if (nactivestatus != objInvoiceCurrencyType.getNactive()) {
				final String updateQueryString = " update invoicecurrencytype set nactive="
						+ objInvoiceCurrencyType.getNactive() + " and nsitecode=" + userInfo.getNmastersitecode()
						+ " where ncurrencycode=" + objInvoiceCurrencyType.getNcurrencycode();
				jdbcTemplate.execute(updateQueryString);

				listAfterUpdate.add(objInvoiceCurrencyType);
				listBeforeUpdate.add(invoicecurrency);

				multilingualIDList.add("IDS_EDITCURRENCYTYPE");

				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				return getInvoiceCurrencyType(userInfo);
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
	 * This method id used to delete an entry in invoicecurrencytype table Need to
	 * check the record is already deleted or not Need to check whether the record
	 * is used in other tables such as 'invoicequotationheader','invoiceheader'
	 *
	 * @param CurrencyTypeDAO [invoicecurrencytype] an Object holds the record to be
	 *                        deleted
	 * @param userInfo        [UserInfo] holding logged in user details based on
	 *                        which the list is to be fetched
	 * @return a response entity with list of available invoicecurrencytype objects
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public ResponseEntity<Object> deleteInvoiceCurrencyType(final InvoiceCurrencyType objInvoiceCurrencyType,
			final UserInfo userInfo) throws Exception {
		final ResponseEntity<Object> invoicecurrencytype = getActiveInvoiceCurrencyTypeById(
				objInvoiceCurrencyType.getNcurrencycode(), userInfo);

		if (invoicecurrencytype == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String query = "select 'IDS_TRANSACTION' as Msg from invoicequotationheader where ncurrencytype="
					+ objInvoiceCurrencyType.getNcurrencycode() + " and " + " nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all"
					+ " select 'IDS_TRANSACTION' as Msg from invoiceheader where ncurrencytype="
					+ objInvoiceCurrencyType.getNcurrencycode() + " and " + " nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNmastersitecode();
			ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				objDeleteValidation = projectDAOSupport
						.validateDeleteRecord(Integer.toString(objInvoiceCurrencyType.getNcurrencycode()), userInfo);
				if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedInvoiceList = new ArrayList<>();
				final String updateQueryString = "update invoicecurrencytype set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ncurrencycode="
						+ objInvoiceCurrencyType.getNcurrencycode();

				jdbcTemplate.execute(updateQueryString);
				objInvoiceCurrencyType.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				deletedInvoiceList.add(objInvoiceCurrencyType);
				multilingualIDList.add("IDS_DELETECURRENCYTYPE");
				auditUtilityFunction.fnInsertAuditAction(deletedInvoiceList, 1, null, multilingualIDList, userInfo);

				return getInvoiceCurrencyType(userInfo);

			}

			else {

				return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}

		}
	}

	/**
	 * This method is used to fetch the active invoicecurrencytype objects for the
	 * specified invoicecurrencytype name and site.
	 *
	 * @param sinvoicecurrencytypename [String] name of the invoicecurrencytype
	 * @param nmasterSiteCode          [int] site code of the invoicecurrencytype
	 * @return list of active invoicecurrencytype code(s) based on the specified
	 *         invoicecurrencytype name and site
	 * @throws Exception
	 */
	private List<InvoiceCurrencyType> getInvoiceCurrencyListByName(final String scurrency, final String ssymbol,
			final int nmasterSiteCode) throws Exception {
		final String strQuery = "select ncurrencycode from invoicecurrencytype where (scurrency = N'"
				+ stringUtilityFunction.replaceQuote(scurrency) + "' or ssymbol= N'"
				+ stringUtilityFunction.replaceQuote(ssymbol) + "')  and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (List<InvoiceCurrencyType>) jdbcTemplate.query(strQuery, new InvoiceCurrencyType());
	}

}
