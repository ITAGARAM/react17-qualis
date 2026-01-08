package com.agaramtech.qualis.invoice.service.invoicetaxtype;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.invoice.model.InvoiceTaxtype;
import com.agaramtech.qualis.invoice.model.InvoiceTaxCalType;
import com.agaramtech.qualis.invoice.model.InvoiceVersionNo;
import com.agaramtech.qualis.basemaster.model.TransactionStatus;
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

@AllArgsConstructor
@Repository
@Transactional(rollbackFor = Exception.class)
public class InvoiceTaxtypeDAOImpl implements InvoiceTaxtypeDAO {

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final ProjectDAOSupport projectDAOSupport;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getTaxtype(final UserInfo userInfo) throws Exception {

		final String strQuery = "select *,ts.stransstatus, TO_CHAR(ddatefrom,'" + userInfo.getSsitedate()
				+ "') as sdatefrom, TO_CHAR(ddateto,'" + userInfo.getSsitedate() + "') as sdateto,"
				+ "coalesce(iv.jsondata->'sversionno'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "iv.jsondata->'sversionno'->>'en-US') as sversionno," + "coalesce(ict.jsondata->'staxcaltype'->>'"
				+ userInfo.getSlanguagetypecode() + "'," + "ict.jsondata->'staxcaltype'->>'en-US') as staxcaltype"
				+ " from invoicetaxtype it,invoicetaxcaltype ict,invoiceversionno iv ,transactionstatus ts where iv.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ict.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "    AND it.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and it.ncaltypecode=ict.ncaltypecode and ts.ntranscode =it.ntranscode and it.nversionnocode=iv.nversionnocode"
				+ " and it.ntaxcode > 0 and it.nsitecode = " + userInfo.getNmastersitecode()
				+ " ORDER BY staxname desc";

		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new InvoiceTaxtype()), HttpStatus.OK);

	}

	/**
	 * This interface declaration is used to add entry in Invoicetaxtype table.
	 * 
	 * @param objInvoicetaxtype [Invoicetaxtype] object holding detail to be deleted
	 *                          in Invoicetaxtype table
	 * @return response entity object holding response status and data of add
	 *         Invoicetaxtype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> createInvoicetaxtype(final InvoiceTaxtype objTaxtype, final UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedtaxtList = new ArrayList<>();
		final List<InvoiceTaxtype> InvoiceTaxtypeListByName = getInvoiceTaxtypeListByName(objTaxtype.getStaxname(),
				objTaxtype.getNsitecode());

		if (InvoiceTaxtypeListByName.isEmpty()) {

			if (objTaxtype.getDdatefrom() != null) {
				final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
				final String date = sdFormat.format(objTaxtype.getDdatefrom());
				objTaxtype.setDdatefrom(sdFormat.parse(date));
				objTaxtype.setSdatefrom(date);
			}

			final String Dateto;
			Date taxToDate = new Date();

			if (objTaxtype.getDdateto() != null) {
				final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
				final String date = sdFormat.format(objTaxtype.getDdateto());
				objTaxtype.setDdateto(sdFormat.parse(date));
				objTaxtype.setSdateto(date);
				Dateto = objTaxtype.getSdateto();
				final String dateto = sdFormat.format(objTaxtype.getDdateto());
				taxToDate = sdFormat.parse(dateto);
			}

			else {
				Dateto = "NULL";
			}

			final SimpleDateFormat sdFormat = new SimpleDateFormat(userInfo.getSdatetimeformat());
			final String datefrom = sdFormat.format(objTaxtype.getDdatefrom());

			final Date taxFromDate = sdFormat.parse(datefrom);

			String insertquery = "";
			final String sequencenoquery = "select nsequenceno from SeqNoInvoice where stablename ='invoicetaxtype'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
			nsequenceno++;

			if (objTaxtype.getDdateto() != null && taxFromDate != null) {
				if (taxFromDate.before(taxToDate)) {

					insertquery = "insert into invoicetaxtype(ntaxcode,staxname,sdescription,ntax,nversionnocode,ncaltypecode,ddatefrom,ddateto,nusercode,dmodifieddate,nsitecode,nstatus,ntranscode) values "
							+ " (" + nsequenceno + ",N'" + stringUtilityFunction.replaceQuote(objTaxtype.getStaxname())
							+ "',N'" + stringUtilityFunction.replaceQuote(objTaxtype.getSdescription()) + "',"
							+ objTaxtype.getNtax() + " " + "," + objTaxtype.getNversionnocode() + ","
							+ objTaxtype.getNcaltypecode() + ",'" + objTaxtype.getSdatefrom() + "','" + Dateto + "',"
							+ userInfo.getNusercode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
							+ userInfo.getNmastersitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(insertquery);
					final String updatequery = "update SeqNoInvoice set nsequenceno =" + nsequenceno
							+ " where stablename='invoicetaxtype'";
					jdbcTemplate.execute(updatequery);

					objTaxtype.setNtaxcode(nsequenceno);
					savedtaxtList.add(objTaxtype);

					multilingualIDList.add("IDS_ADDINVOICETAXTYPE");
					auditUtilityFunction.fnInsertAuditAction(savedtaxtList, 1, null, multilingualIDList, userInfo);

				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TAXSTARTDATEALERT",
							userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
				}
			} else {
				insertquery = "insert into invoicetaxtype(ntaxcode,staxname,sdescription,ntax,nversionnocode,ncaltypecode,ddatefrom,ddateto,nusercode,dmodifieddate,nsitecode,nstatus) values "
						+ " (" + nsequenceno + ",N'" + stringUtilityFunction.replaceQuote(objTaxtype.getStaxname())
						+ "',N'" + stringUtilityFunction.replaceQuote(objTaxtype.getSdescription()) + "',"
						+ objTaxtype.getNtax() + " " + "," + objTaxtype.getNversionnocode() + ","
						+ objTaxtype.getNcaltypecode() + ",'" + objTaxtype.getSdatefrom() + "'," + Dateto + ","
						+ userInfo.getNusercode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						+ userInfo.getNmastersitecode() + ",1)";

				final String taxName = objTaxtype.getStaxname().chars().filter(c -> !Character.isDigit(c))
						.mapToObj(c -> String.valueOf((char) c)).collect(Collectors.joining());
               // % this special character are used in gst
				final String str = "select ntaxcode from invoicetaxtype where staxname collate \"C\" like '%" + taxName
						+ "%' and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and ntranscode=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and nsitecode= " + userInfo.getNmastersitecode();
				final List<InvoiceTaxtype> taxId = (List<InvoiceTaxtype>) jdbcTemplate.query(str, new InvoiceTaxtype());

				insertquery = "insert into invoicetaxtype(ntaxcode,staxname,sdescription,ntax,nversionnocode,ncaltypecode,ddatefrom,ddateto,nusercode,dmodifieddate,nsitecode,nstatus,ntranscode) values "
						+ " (" + nsequenceno + ",N'" + stringUtilityFunction.replaceQuote(objTaxtype.getStaxname())
						+ "',N'" + stringUtilityFunction.replaceQuote(objTaxtype.getSdescription()) + "',"
						+ objTaxtype.getNtax() + " " + "," + objTaxtype.getNversionnocode() + ","
						+ objTaxtype.getNcaltypecode() + ",'" + objTaxtype.getSdatefrom() + "'," + Dateto + ","
						+ userInfo.getNusercode() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						+ userInfo.getNmastersitecode() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(insertquery);

				final String updatequery = "update SeqNoInvoice set nsequenceno =" + nsequenceno
						+ " where stablename='invoicetaxtype'";
				jdbcTemplate.execute(updatequery);

				objTaxtype.setNtaxcode(nsequenceno);
				savedtaxtList.add(objTaxtype);

				if (!taxId.isEmpty()) {
					String taxTypeUpdatequery = "update invoicetaxtype set ntranscode ="
							+ Enumeration.TransactionStatus.DEACTIVE.gettransactionstatus() + " where ntaxcode="
							+ taxId.get(taxId.size() - 1).getNtaxcode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntranscode="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
							+ userInfo.getNmastersitecode();
					jdbcTemplate.execute(taxTypeUpdatequery);
				}
				multilingualIDList.add("IDS_ADDINVOICETAXTYPE");
				auditUtilityFunction.fnInsertAuditAction(savedtaxtList, 1, null, multilingualIDList, userInfo);
			}
		}

		else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
		return getTaxtype(userInfo);
	}

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public InvoiceTaxtype getActiveInvoiceTaxTypeByIdUpdate(final int ntaxcode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = "select distinct it.ntaxcode,it.staxname,it.sdescription,it.ntax,ts.stransstatus,"
				+ "it.nversionnocode,it.ncaltypecode, TO_CHAR(it.ddatefrom,'" + userInfo.getSsitedate()
				+ "') as sdatefrom, TO_CHAR(it.ddateto,'" + userInfo.getSsitedate() + "') as sdateto,"
				+ "coalesce(iv.jsondata->'sversionno'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "iv.jsondata->'sversionno'->>'en-US') as sversionno," + "coalesce(ict.jsondata->'staxcaltype'->>'"
				+ userInfo.getSlanguagetypecode() + "'," + "ict.jsondata->'staxcaltype'->>'en-US') as staxcaltype"
				+ " from invoicetaxtype it,transactionstatus ts ,invoicetaxcaltype ict,invoiceversionno iv"
				+ " where it.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ict.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and iv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.ntranscode=it.ntranscode and it.nversionnocode=iv.nversionnocode and it.ncaltypecode=ict.ncaltypecode "
				+ " and it.ntaxcode = " + ntaxcode + " and it.nsitecode= " + userInfo.getNmastersitecode();
		return (InvoiceTaxtype) jdbcUtilityFunction.queryForObject(strQuery, InvoiceTaxtype.class, jdbcTemplate);
	}

	/**
	 * This interface declaration is used to update entry in Invoicetaxtype table.
	 * 
	 * @param objInvoicetaxtype [Invoicetaxtype] object holding details to be
	 *                          updated in Invoicetaxtype table
	 * @return response entity object holding response status and data of updated
	 *         Invoicetaxtype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateInvoiceTaxtype(final InvoiceTaxtype objTaxtype, final UserInfo userInfo)
			throws Exception {

		final InvoiceTaxtype invoicetaxtype = getActiveInvoiceTaxTypeByIdUpdate(objTaxtype.getNtaxcode(), userInfo);
		final List<String> multilingualIDList = new ArrayList<>();

		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();

		if (invoicetaxtype == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select ntaxcode from invoicetaxtype where staxname = '"
					+ stringUtilityFunction.replaceQuote(objTaxtype.getStaxname()) + "' and ntaxcode <> "
					+ objTaxtype.getNtaxcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
					+ userInfo.getNmastersitecode();

			final List<InvoiceTaxtype> List = (List<InvoiceTaxtype>) jdbcTemplate.query(queryString,
					new InvoiceTaxtype());

			if (List.isEmpty()) {
				String doj = null;
				if (objTaxtype.getDdatefrom() != null) {
					final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
					doj = "N'" + sdFormat.format(objTaxtype.getDdatefrom()) + "'";
					objTaxtype.setSdatefrom(
							new SimpleDateFormat(userInfo.getSsitedate()).format(objTaxtype.getDdatefrom()));
				}
				String taxTypedoj = null;
				if (objTaxtype.getDdateto() != null) {
					final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
					taxTypedoj = "N'" + sdFormat.format(objTaxtype.getDdateto()) + "'";
					objTaxtype
							.setSdateto(new SimpleDateFormat(userInfo.getSsitedate()).format(objTaxtype.getDdateto()));
					objTaxtype.getSdateto();
				} else {
				}
				if (objTaxtype.getDdateto() != null) {
					final SimpleDateFormat sdFormat = new SimpleDateFormat(userInfo.getSdatetimeformat());
					final String datefrom = sdFormat.format(objTaxtype.getDdatefrom());

					final String dateto = sdFormat.format(objTaxtype.getDdateto());

					final Date taxFromDate = sdFormat.parse(datefrom);

					final Date taxToDate = sdFormat.parse(dateto);
					if (taxFromDate.before(taxToDate)) {

						final String updateQueryString = "update invoicetaxtype set staxname=N'"
								+ stringUtilityFunction.replaceQuote(objTaxtype.getStaxname()) + "', sdescription ='"
								+ stringUtilityFunction.replaceQuote(objTaxtype.getSdescription()) + "',ntax= "
								+ objTaxtype.getNtax() + ",nversionnocode = " + objTaxtype.getNversionnocode()
								+ ",ncaltypecode = " + objTaxtype.getNcaltypecode() + ", ddatefrom= " + doj
								+ "::timestamp," + "ddateto= " + taxTypedoj + "::timestamp," + "dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntaxcode="
								+ objTaxtype.getNtaxcode() + " and nsitecode= " + userInfo.getNmastersitecode() + ";";

						jdbcTemplate.execute(updateQueryString);

						listAfterUpdate.add(objTaxtype);
						listBeforeUpdate.add(invoicetaxtype);

						multilingualIDList.add("IDS_EDITINVOICETAXTYPE");

						auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate,
								multilingualIDList, userInfo);

					} else {

						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_TAXDATEALERT",
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}

				}

				if (objTaxtype.getDdateto() == null) {

					final String updateQueryString = "update invoicetaxtype set staxname=N'"
							+ stringUtilityFunction.replaceQuote(objTaxtype.getStaxname()) + "', sdescription ='"
							+ stringUtilityFunction.replaceQuote(objTaxtype.getSdescription()) + "',ntax= "
							+ objTaxtype.getNtax() + ",nversionnocode = " + objTaxtype.getNversionnocode()
							+ ",ncaltypecode = " + objTaxtype.getNcaltypecode() + ", ddatefrom= " + doj + "::timestamp,"
							+ "ddateto= " + taxTypedoj + "::timestamp," + "dmodifieddate='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where ntaxcode="
							+ objTaxtype.getNtaxcode() + " and nsitecode= " + userInfo.getNmastersitecode() + ";";

					jdbcTemplate.execute(updateQueryString);

					listAfterUpdate.add(objTaxtype);
					listBeforeUpdate.add(invoicetaxtype);

					multilingualIDList.add("IDS_EDITINVOICETAXTYPE");

					auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
							userInfo);

				}
			}
			return getTaxtype(userInfo);

		}
	}

	/**
	 * This interface declaration is used to delete entry in Invoicetaxtype table.
	 * 
	 * @param objInvoicetaxtype [Invoicetaxtype] object holding detail to be deleted
	 *                          in Invoicetaxtype table
	 * @return response entity object holding response status and data of deleted
	 *         Invoicetaxtype object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteInvoicetaxtype(final InvoiceTaxtype objTaxtype, final UserInfo userInfo)
			throws Exception {

		if (objTaxtype.getDdatefrom() != null) {
			final SimpleDateFormat sdFormat = new SimpleDateFormat(userInfo.getSdatetimeformat());
			String date = sdFormat.format(objTaxtype.getDdatefrom());
			objTaxtype.setDdatefrom(sdFormat.parse(date));
			objTaxtype.setSdatefrom(new SimpleDateFormat(userInfo.getSsitedate()).format(objTaxtype.getDdatefrom()));
		}

		if (objTaxtype.getDdateto() != null) {
			final SimpleDateFormat sdFormat = new SimpleDateFormat(userInfo.getSdatetimeformat());
			String date = sdFormat.format(objTaxtype.getDdateto());
			objTaxtype.setDdateto(sdFormat.parse(date));
			objTaxtype.setSdateto(new SimpleDateFormat(userInfo.getSsitedate()).format(objTaxtype.getDdateto()));
		}

		final ResponseEntity<Object> invoicetaxtype = getActiveInvoiceTaxtypeById(objTaxtype.getNtaxcode(), userInfo);

		if (invoicetaxtype == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		else {

			final String query = "select 'IDS_PRODUCTMASTER' as Msg from taxproductdetails where ntaxcode= "
					+ objTaxtype.getNtaxcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				objDeleteValidation = projectDAOSupport.validateDeleteRecord(Integer.toString(objTaxtype.getNtaxcode()),
						userInfo);
				if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}

			if (validRecord) {

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> deletedtaxtypeList = new ArrayList<>();
				final String updateQueryString = "update invoicetaxtype set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where ntaxcode="
						+ objTaxtype.getNtaxcode();

				jdbcTemplate.execute(updateQueryString);
				objTaxtype.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

				deletedtaxtypeList.add(objTaxtype);
				multilingualIDList.add("IDS_DELETEINVOICETAXTYPE");
				auditUtilityFunction.fnInsertAuditAction(deletedtaxtypeList, 1, null, multilingualIDList, userInfo);

				return getTaxtype(userInfo);

			} else {
				return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}

	}

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	@Override
	public ResponseEntity<Object> getActiveInvoiceTaxtypeById(final int ntaxcode, final UserInfo userInfo)
			throws Exception {

		final String query = "select 'IDS_PRODUCTMASTER' as Msg from taxproductdetails where ntaxcode=" + ntaxcode
				+ "  and " + "nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		ValidatorDel objDeleteValidation = projectDAOSupport.getTransactionInfo(query, userInfo);
		boolean validRecord = false;
		if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
			validRecord = true;
			objDeleteValidation = projectDAOSupport.validateDeleteRecord(Integer.toString(ntaxcode), userInfo);
			if (objDeleteValidation.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
			} else {
				validRecord = false;
			}
		}
		final InvoiceTaxtype reportsettings;
		if (validRecord) {
			final String strQuery = "select distinct it.ntaxcode,it.staxname,it.sdescription,it.ntax,ts.stransstatus,"
					+ "it.nversionnocode,it.ncaltypecode, TO_CHAR(it.ddatefrom,'" + userInfo.getSsitedate()
					+ "') as sdatefrom, TO_CHAR(it.ddateto,'" + userInfo.getSsitedate() + "') as sdateto,"
					+ "coalesce(iv.jsondata->'sversionno'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ "iv.jsondata->'sversionno'->>'en-US') as sversionno," + "coalesce(ict.jsondata->'staxcaltype'->>'"
					+ userInfo.getSlanguagetypecode() + "'," + "ict.jsondata->'staxcaltype'->>'en-US') as staxcaltype"
					+ " from invoicetaxtype it,transactionstatus ts ,invoicetaxcaltype ict,invoiceversionno iv"
					+ " where it.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ict.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and iv.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ts.ntranscode=it.ntranscode and it.nversionnocode=iv.nversionnocode and it.ncaltypecode=ict.ncaltypecode "
					+ " and it.ntaxcode = " + ntaxcode + " and it.nsitecode= " + userInfo.getNmastersitecode();
			reportsettings = (InvoiceTaxtype) jdbcUtilityFunction.queryForObject(strQuery, InvoiceTaxtype.class,
					jdbcTemplate);
		} else {

			return new ResponseEntity<>(objDeleteValidation.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(reportsettings, HttpStatus.OK);

	}

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getTaxcaltype(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select ncaltypecode,coalesce(g.jsondata->'staxcaltype'->>'"
				+ userInfo.getSlanguagetypecode() + "'," + " g.jsondata->'staxcaltype'->>'en-US') as staxcaltype "
				+ " from invoicetaxcaltype g where " + " g.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode() + " and g.ncaltypecode > 0 order by ncaltypecode desc";
		outputMap.put("invoicetaxcaltypeList", jdbcTemplate.query(strQuery, new InvoiceTaxCalType()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getVersionno(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = "select nversionnocode,coalesce(g.jsondata->'sversionno'->>'"
				+ userInfo.getSlanguagetypecode() + "'," + " g.jsondata->'sversionno'->>'en-US') as sversionno "
				+ " from invoiceversionno g where " + " g.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode() + " and g.nversionnocode > 0 order by nversionnocode desc";
		outputMap.put("invoiceversionnoList", jdbcTemplate.query(strQuery, new InvoiceVersionNo()));

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	private List<InvoiceTaxtype> getInvoiceTaxtypeListByName(final String taxname, final int nmasterSiteCode)
			throws Exception {
		final String strQuery = "select ntaxcode from invoicetaxtype where staxname = N'"
				+ stringUtilityFunction.replaceQuote(taxname) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;

		return (List<InvoiceTaxtype>) jdbcTemplate.query(strQuery, new InvoiceTaxtype());
	}

	/**
	 * This interface declaration is used to get the over all Invoicetaxtype with
	 * respect to site
	 * 
	 * @param userInfo [UserInfo]
	 * @return a response entity which holds the list of Invoicetaxtype with respect
	 *         to site and also have the HTTP response code
	 * @throws Exception
	 */
	public ResponseEntity<Object> getStatus(final UserInfo userInfo) {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String statusStrQuery = "select ntranscode,stransstatus from transactionstatus where ntranscode="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		outputMap.put("statusList", jdbcTemplate.query(statusStrQuery, new TransactionStatus()));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}
}
