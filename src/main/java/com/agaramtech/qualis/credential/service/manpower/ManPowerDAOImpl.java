package com.agaramtech.qualis.credential.service.manpower;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.credential.model.ManPower;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "outsourceemployee" table by 
 * implementing methods from its interface. 
 */
/**
 * @author sujatha.v SWSM-8 01/08/2025
 */
@AllArgsConstructor
@Repository
public class ManPowerDAOImpl implements ManPowerDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManPowerDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private ValidatorDel validatorDel;

	/**
	 * This method is used to retrieve list of all active outsourceemployee for the
	 * specified site.
	 * 
	 * @param userInfo object for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         outsourceemployee
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getManPower(final UserInfo userInfo) throws Exception {
		LOGGER.info("getManPower");
		final ObjectMapper objMapper = new ObjectMapper();
		final String strManPower = "select o.noutsourceempcode, o.sfirstname, o.slastname, o.sdesignation,  "
				+ "  o.noffsetddateofjoin, o.ntzdateofjoin, o.smobileno, o.semail, o.nsitemastercode, o.saddress, "
				+ "  o.nsitecode, o.nstatus,  COALESCE((to_char(o.ddateofbirth, '" + userInfo.getSpgsitedatetime()
				+ "')), '-') as sdateofbirth, " + "  to_char(o.ddateofjoin, '" + userInfo.getSpgsitedatetime()
				+ "') as sdateofjoin, s.ssitename as ssitename " + "  from outsourceemployee o, site s "
				+ "  where s.nsitecode = o.nsitemastercode" + " and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "  o.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ "  o.noutsourceempcode > 0 and " + "  o.nsitecode = " + userInfo.getNmastersitecode();
		List<ManPower> manpowerList = jdbcTemplate.query(strManPower, new ManPower());

		final List<ManPower> lstUTCConvertedDate = objMapper.convertValue(
				dateUtilityFunction.getSiteLocalTimeFromUTC(manpowerList, Arrays.asList("sdateofjoin"),
						Arrays.asList(userInfo.getStimezoneid()), userInfo, false, null, false),
				new TypeReference<List<ManPower>>() {
				});
		return new ResponseEntity<>(lstUTCConvertedDate, HttpStatus.OK);
	}
	
	/**
	 * This method is used to retrieve list of all active site along with the current
	 * login site in the 1st.
	 * 
	 * @param userInfo object for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         site
	 * @throws Exception that are thrown from this DAO layer
	 */
	// added by sujatha ATE_274 to get the Login Site in the 1st value of dropdown on 26-08-2025
	@Override
	public ResponseEntity<Object> getSite(final UserInfo userInfo) throws Exception {
		String strQuery = "select * from site where nsitecode > 0 and nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nmastersitecode= "
				+ userInfo.getNmastersitecode() + " order by case when nsitecode="+userInfo.getNtranssitecode()
				+ " then 0 else 1 end, ndefaultstatus";
		LOGGER.info(strQuery);
		final List<Site> lstSite = jdbcTemplate.query(strQuery, new Site());
		return new ResponseEntity<Object>(lstSite, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to outsourceemployee table.
	 * AuditCategory Name is not unique across the database.
	 * 
	 * @param objManPower [ManPower] object holding details to be added in
	 *                    outsourceemployee table
	 * @param userInfo    [UserInfo] holding logged in user details based on which
	 *                    the list is to be fetched
	 * @return saved manpower object with status code 200 if saved successfully
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createManPower(final ManPower objManPower, final UserInfo userInfo) throws Exception {
		final String sQuery = " lock  table outsourceemployee "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedManPowerList = new ArrayList<>();
		final String sequencenoquery = "select nsequenceno from SeqNoCredentialManagement where stablename ='outsourceemployee' "
				+ "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
		nsequenceno++;
		final String insertQuery = "insert into outsourceemployee ("
				+ "noutsourceempcode, sfirstname, slastname, sdesignation, ddateofbirth, ddateofjoin, "
				+ "noffsetddateofjoin, ntzdateofjoin, smobileno, semail, nsitemastercode, saddress, "
				+ "dmodifieddate, nsitecode, nstatus) VALUES (" + nsequenceno + ", N'"
				+ stringUtilityFunction.replaceQuote(objManPower.getSfirstname()) + "', N'"
				+ stringUtilityFunction.replaceQuote(objManPower.getSlastname()) + "', N'"
				+ stringUtilityFunction.replaceQuote(objManPower.getSdesignation()) + "',"
				+ (objManPower.getDdateofbirth() != null ? "'" + objManPower.getDdateofbirth() + "'" : "null") + ","
				+ (objManPower.getDdateofjoin() != null ? "'" + objManPower.getDdateofjoin() + "'" : "null") + ", "
				+ objManPower.getNoffsetddateofjoin() + ", " + objManPower.getNtzdateofjoin() + ", N'"
				+ stringUtilityFunction.replaceQuote(objManPower.getSmobileno()) + "', N'"
				+ stringUtilityFunction.replaceQuote(objManPower.getSemail()) + "', " + objManPower.getNsitecode()
				+ ", N'" + stringUtilityFunction.replaceQuote(objManPower.getSaddress()) + "', '"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

		jdbcTemplate.execute(insertQuery);
		final String updatequery = "update SeqNoCredentialManagement set nsequenceno =" + nsequenceno
				+ " where stablename='outsourceemployee'" + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		jdbcTemplate.execute(updatequery);
		objManPower.setNoutsourceempcode(nsequenceno);

		objManPower.setNsitemastercode(objManPower.getNsitecode());
		objManPower.setSdateofbirth(objManPower.getDdateofbirth() == null ? null
				: dateUtilityFunction.instantDateToStringWithFormat(objManPower.getDdateofbirth(),
						userInfo.getSsitedate()));
		objManPower.setSdateofjoin(objManPower.getDdateofjoin() == null ? null
				: dateUtilityFunction.instantDateToStringWithFormat(objManPower.getDdateofjoin(),
						userInfo.getSsitedate()));
		savedManPowerList.add(objManPower);
		multilingualIDList.add("IDS_ADDMANPOWER");
		auditUtilityFunction.fnInsertAuditAction(savedManPowerList, 1, null, multilingualIDList, userInfo);
		return getManPower(userInfo);
	}

	/**
	 * This method is used to retrieve active manpower object based on the specified
	 * nmanpowercode.
	 * 
	 * @param nmanpowercode [int] primary key of manpower object
	 * @param userInfo      [UserInfo] holding logged in user details based on which
	 *                      the list is to be fetched
	 * @return response entity object holding response status and data of manpower
	 *         object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ManPower getActiveManPowerById(final int nmanpowercode, final UserInfo userInfo) throws Exception {
		final String strQuery = "select o.noutsourceempcode, o.sfirstname, o.slastname, o.sdesignation, "
				+ " o.smobileno, o.semail, o.nsitemastercode, s.ssitename, o.saddress, "
				+ " s.nsitecode, o.nstatus, to_char(o.ddateofbirth, '" + userInfo.getSpgsitedatetime() //modified by sujatha ATE_274 SWSM-34 for date issue in edit 16-09-2025
				+ "') as sdateofbirth, " + " to_char(o.ddateofjoin, '" + userInfo.getSpgsitedatetime() //modified by sujatha ATE_274 SWSM-34 for date issue in edit 16-09-2025
				+ "') as sdateofjoin, " + "  s.ssitename as ssitename from outsourceemployee o, site s "
				+ " where s.nsitecode=o.nsitemastercode and " + "s.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and o.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and o.noutsourceempcode = "
				+ nmanpowercode + " and o.nsitecode= " + userInfo.getNmastersitecode();
		final ManPower objManPower = (ManPower) jdbcUtilityFunction.queryForObject(strQuery, ManPower.class,
				jdbcTemplate);
		return objManPower;
	}

	/**
	 * This method is used to update entry in outsourceemployee table. Need to
	 * validate that the manpower object to be updated is active before updating
	 * details in database.
	 * 
	 * @param objManPower [ManPower] object holding details to be updated in
	 *                    outsourceemployee table
	 * @param userInfo    [UserInfo] holding logged in user details based on which
	 *                    the list is to be fetched
	 * @return saved manpower object with status code 200 if saved successfully.
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateManPower(final ManPower objManPower, final UserInfo userInfo) throws Exception {
		final ManPower manPower = getActiveManPowerById(objManPower.getNoutsourceempcode(), userInfo);
		if (manPower == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String updateQueryString = "update outsourceemployee set nsitemastercode="
					+ objManPower.getNsitecode() + " ,sfirstname='"
					+ stringUtilityFunction.replaceQuote(objManPower.getSfirstname()) + " ',slastname='"
					+ stringUtilityFunction.replaceQuote(objManPower.getSlastname()) + "', " + "sdesignation ='"
					+ stringUtilityFunction.replaceQuote(objManPower.getSdesignation()) + "', ddateofbirth = "
					+ (objManPower.getDdateofbirth() != null ? "'" + objManPower.getDdateofbirth() + "'" : "null")
					+ ", ddateofjoin = "
					+ (objManPower.getDdateofjoin() != null ? "'" + objManPower.getDdateofjoin() + "'" : "null")
					+ ", noffsetddateofjoin=" + objManPower.getNoffsetddateofjoin() + ", ntzdateofjoin="
					+ objManPower.getNtzdateofjoin() + ", smobileno='"
					+ stringUtilityFunction.replaceQuote(objManPower.getSmobileno()) + "', semail='"
					+ stringUtilityFunction.replaceQuote(objManPower.getSemail()) + "', saddress='"
					+ stringUtilityFunction.replaceQuote(objManPower.getSaddress()) + "', dmodifieddate ='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where noutsourceempcode="
					+ objManPower.getNoutsourceempcode() + " and nsitecode= " + userInfo.getNmastersitecode();

			jdbcTemplate.execute(updateQueryString);
			final ManPower objManPowerAfterSave = getActiveManPowerById(objManPower.getNoutsourceempcode(), userInfo);
			final List<String> multilingualIDList = new ArrayList<>();
			multilingualIDList.add("IDS_EDITMANPOWER");
			final List<Object> listAfterSave = new ArrayList<>();
			objManPower.setNsitemastercode(objManPower.getNsitecode());
			listAfterSave.add(objManPowerAfterSave);
			final List<Object> listBeforeSave = new ArrayList<>();
			listBeforeSave.add(manPower);
			auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);
			return getManPower(userInfo);
		}
	}

	/**
	 * This method id used to delete outsourceemployee, only if exits else throws
	 * Alert 'Already Deleted'
	 * 
	 * @param objManPower [ManPower] an Object holds the record to be deleted
	 * @param userInfo    [UserInfo] holding logged in user details based on which
	 *                    the list is to be fetched
	 * @return a response entity with list of available manpower objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteManPower(final ManPower objManPower, final UserInfo userInfo) throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedManPowerList = new ArrayList<>();
		final ManPower manPower = getActiveManPowerById(objManPower.getNoutsourceempcode(), userInfo);
		if (manPower == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			//added by sujatha v ATE-274 for employeeattendence delete validation
			final String query = " Select 'IDS_EMPLOYEEATTENDENCE' as Msg from employeeattendence where nusercode= "
					+ objManPower.getNoutsourceempcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;
			// modified by sujatha ATE_274 19-09-2025 for the bug--> allow to delete manpower data used in employee attendence in differenct site 
//					+ " and nsitecode="+ userInfo.getNtranssitecode(); // modified by sujatha ATE_274 SWSM-22 site specific 
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport
						.validateDeleteRecord(Integer.toString(objManPower.getNoutsourceempcode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final String updateQueryString = "update outsourceemployee set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where noutsourceempcode= "
						+ objManPower.getNoutsourceempcode() + " and nsitecode=" + userInfo.getNmastersitecode();
				jdbcTemplate.execute(updateQueryString);
				objManPower.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedManPowerList.add(objManPower);
				multilingualIDList.add("IDS_DELETEMANPOWER");
				auditUtilityFunction.fnInsertAuditAction(savedManPowerList, 1, null, multilingualIDList, userInfo);
				return getManPower(userInfo);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
}
