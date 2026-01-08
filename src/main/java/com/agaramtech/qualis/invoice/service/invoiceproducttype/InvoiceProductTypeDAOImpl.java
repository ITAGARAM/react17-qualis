package com.agaramtech.qualis.invoice.service.invoiceproducttype;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.invoice.model.InvoiceProductType;

import lombok.AllArgsConstructor;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'invoiceProductType' table
 */

@AllArgsConstructor
@Repository

public class InvoiceProductTypeDAOImpl implements InvoiceProductTypeDAO {

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This method is used to retrieve active InvoiceProductType object based on the
	 * specified ntypecode.
	 * 
	 * @param ntypecode [int] primary key of InvoiceProductType object
	 * @return response entity object holding response status and data of
	 *         InvoiceProductType object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getInvoiceProductType(final UserInfo userInfo) throws Exception {
		final String str = "SELECT i.*, " + "COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "', "
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') AS sdisplaystatus "
				+ "FROM invoiceproducttype i, transactionstatus ts " + "WHERE i.ntypecode not in (1,-1) "
				+ "AND i.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " AND i.nsitecode = " + userInfo.getNmastersitecode() + " AND ts.ntranscode = i.nactive"
				+ " ORDER BY i.stypename desc";

		return new ResponseEntity<Object>(jdbcTemplate.query(str, new InvoiceProductType()), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active InvoiceProductType object based on the
	 * specified ntypecode.
	 * 
	 * @param ntypecode [int] primary key of InvoiceProductType object
	 * @return response entity object holding response status and data of
	 *         InvoiceProductType object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveInvoiceProductTypeById(final int ntypecode, final UserInfo userInfo)
			throws Exception {
		// deleteValidation
		final String query = "select 'IDS_PRODUCTMASTER' as Msg from invoiceproductmaster where ntypecode= " + ntypecode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ userInfo.getNmastersitecode();
		ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);

		boolean validRecord = false;
		if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
			validRecord = true;
			objDeleteValidation = projectDAOSupport.validateDeleteRecord(Integer.toString(ntypecode), userInfo);
			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
			} else {
				validRecord = false;
			}
		}
		final InvoiceProductType reportsettings;
		if (validRecord) {
			final String strQuery = "select i.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "',"
					+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
					+ " from invoiceproducttype i,transactionstatus ts " + " where i.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.ntranscode = i.nactive"
					+ " and i.ntypecode = " + ntypecode;
			reportsettings = (InvoiceProductType) jdbcUtilityFunction.queryForObject(strQuery, InvoiceProductType.class,
					jdbcTemplate);
		} else {

			return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(reportsettings, HttpStatus.OK);

	}

	/**
	 * This method is used to add a new entry to invoiceProductType table. Need to
	 * check for duplicate entry of InvoiceProductType name for the specified site
	 * before saving into database. Need to check that there should be only one
	 * default InvoiceProductType for a site
	 * 
	 * @param objInvoiceProductType [InvoiceProductType] object holding details to
	 *                              be added in invoiceProductType table
	 * @return inserted InvoiceProductType object and HTTP Status on successive
	 *         insert otherwise corresponding HTTP Status
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createInvoiceProductType(final InvoiceProductType objInvoiceProductType,
			final UserInfo userInfo) throws Exception {

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedInvoiceProductTypeList = new ArrayList<>();

		final List<InvoiceProductType> InvoiceProductTypeListByName = getInvoiceProductTypeListByName(
				objInvoiceProductType.getStypename(), objInvoiceProductType.getNsitecode());
		if (InvoiceProductTypeListByName.isEmpty()) {

			final String sequencenoquery = "select nsequenceno from seqnoinvoice where stablename ='invoiceproducttype'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;

			final String insertquery = "Insert into invoiceproducttype (ntypecode,stypename,sdescription,nactive,nusercode,dmodifieddate,nsitecode,nstatus) "
					+ " values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(objInvoiceProductType.getStypename()) + "',N'"
					+ stringUtilityFunction.replaceQuote(objInvoiceProductType.getSdescription()) + "',"
					+ objInvoiceProductType.getNactive() + "," + userInfo.getNusercode() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			jdbcTemplate.execute(insertquery);
			final String updatequery = "update seqnoinvoice set nsequenceno =" + nsequenceno
					+ " where stablename='invoiceproducttype'";
			jdbcTemplate.execute(updatequery);

			objInvoiceProductType.setNtypecode(nsequenceno);
			savedInvoiceProductTypeList.add(objInvoiceProductType);

			multilingualIDList.add("IDS_ADDPRODUCTTYPE");

			auditUtilityFunction.fnInsertAuditAction(savedInvoiceProductTypeList, 1, null, multilingualIDList,
					userInfo);
			return getInvoiceProductType(userInfo);
		} else {
			// Conflict = 409 - Duplicate entry --getSlanguagetypecode
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to update entry in invoiceProductType table. Need to
	 * validate that the InvoiceProductType object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * InvoiceProductType name for the specified site before saving into database.
	 * Need to check that there should be only one default InvoiceProductType for a
	 * site
	 * 
	 * @param objInvoiceProductType [InvoiceProductType] object holding details to
	 *                              be updated in invoiceProductType table
	 * @return response entity object holding response status and data of updated
	 *         InvoiceProductType object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateInvoiceProductType(final InvoiceProductType objInvoiceProductType,
			final UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		final InvoiceProductType invoiceproducttype = getActiveInvoiceProductTypeByIdforUpdate(
				objInvoiceProductType.getNtypecode(), userInfo);
		if (invoiceproducttype == null) {
			return new ResponseEntity<>(

					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),

							userInfo.getSlanguagefilename()),

					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String queryString = "select ntypecode from invoiceproducttype where stypename = '"
					+ stringUtilityFunction.replaceQuote(objInvoiceProductType.getStypename()) + "' and ntypecode <> "
					+ objInvoiceProductType.getNtypecode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="+userInfo.getNmastersitecode();

			final List<InvoiceProductType> InvoiceProductTypeListByName = (List<InvoiceProductType>) jdbcTemplate
					.query(queryString, new InvoiceProductType());
			if (InvoiceProductTypeListByName.isEmpty()) {

				final String updateQueryString = "update invoiceproducttype set stypename='"
						+ stringUtilityFunction.replaceQuote(objInvoiceProductType.getStypename())
						+ "', sdescription ='"
						+ stringUtilityFunction.replaceQuote(objInvoiceProductType.getSdescription()) + "', nactive ="
						+ objInvoiceProductType.getNactive() + ",dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + userInfo.getNmastersitecode() + "," + "'"
						+ " where ntypecode=" + objInvoiceProductType.getNtypecode() + ";";

				jdbcTemplate.execute(updateQueryString);
				listAfterUpdate.add(objInvoiceProductType);
				listBeforeUpdate.add(invoiceproducttype);

				multilingualIDList.add("IDS_EDITPRODUCTTYPE");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				return getInvoiceProductType(userInfo);
			} else {
				// Conflict = 409 - Duplicate entry
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to retrieve active Invoiceproducttype object based on the
	 * specified ntypecode.
	 * 
	 * @param objInvoiceProductType [InvoiceProductType] an Object holds the record
	 *                              to be deleted
	 * @return a response entity with corresponding HTTP status and an
	 *         InvoiceProductType object
	 * @exception Exception that are thrown from this DAO layer
	 */

	@Override
	public InvoiceProductType getActiveInvoiceProductTypeByIdforUpdate(final int ntypecode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select i.*," + "coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus "
				+ " from invoiceproducttype i,transactionstatus ts " + " where i.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.ntranscode = i.nactive"
				+ " and i.ntypecode = " + ntypecode + " and nsitecode="+userInfo.getNmastersitecode();
		return (InvoiceProductType) jdbcUtilityFunction.queryForObject(strQuery, InvoiceProductType.class,
				jdbcTemplate);
	}

	/**
	 * This method id used to delete an entry in invoiceProductType table
	 * 
	 * @param inputMap [Map] holds the InvoiceProductType object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         InvoiceProductType object
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> deleteInvoiceProductType(final InvoiceProductType objInvoiceProductType,
			final UserInfo userInfo) throws Exception {

		final ResponseEntity<Object> invoiceproducttype = getActiveInvoiceProductTypeById(
				objInvoiceProductType.getNtypecode(), userInfo);
		if (invoiceproducttype == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			// deleteValidation
			final String query = "select 'IDS_PRODUCTMASTER' as Msg from invoiceproductmaster where ntypecode= "
					+ objInvoiceProductType.getNtypecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode();
			final ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);

			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedInvoiceProductTypeList = new ArrayList<>();
				final String updateQueryString = "update invoiceproducttype set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ntypecode="
						+ objInvoiceProductType.getNtypecode();

				jdbcTemplate.execute(updateQueryString);
				objInvoiceProductType.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				deletedInvoiceProductTypeList.add(objInvoiceProductType);
				multilingualIDList.add("IDS_DELETERODUCTTYPE");
				auditUtilityFunction.fnInsertAuditAction(deletedInvoiceProductTypeList, 1, null, multilingualIDList,
						userInfo);

				return getInvoiceProductType(userInfo);
			} else {
				return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to fetch the active InvoiceProductType objects for the
	 * specified InvoiceProductType name and site.
	 * 
	 * @param ntypecode       [String] name of the InvoiceProductType
	 * @param nmasterSiteCode [int] site code of the InvoiceProductType
	 * @return list of active InvoiceProductType code(s) based on the specified
	 *         InvoiceProductType name and site
	 * @throws Exception
	 */
	private List<InvoiceProductType> getInvoiceProductTypeListByName(final String stypename, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select ntypecode from invoiceproducttype where stypename = N'"
				+ stringUtilityFunction.replaceQuote(stypename) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (List<InvoiceProductType>) jdbcTemplate.query(strQuery, new InvoiceProductType());
	}

}
