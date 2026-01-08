package com.agaramtech.qualis.storagemanagement.service.sitesampleexpirymapping;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.basemaster.model.Period;
import com.agaramtech.qualis.credential.model.Site;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.storagemanagement.model.SiteSampleExpiryMapping;
import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "siteexpirymapping" table by 
 * implementing methods from its interface. 
 */
/**
 * @author sujatha.v SWSM-14 31/08/2025
 */
@Repository
@AllArgsConstructor
public class SiteSampleExpiryMappingDAOImpl implements SiteSampleExpiryMappingDAO {

	private final static Logger LOGGER = LoggerFactory.getLogger(SiteSampleExpiryMappingDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityTemplateFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final CommonFunction commonFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;

	/**
	 * This method is used to retrieve list of all available siteexpirymapping's for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and list of all active
	 *         siteexpirymapping's
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSiteSampleExpiryMapping(final UserInfo userInfo) throws Exception {
		final String strQuery = "select e.nsiteexpirymappingcode, e.sexpirydays, e.nperiodcode, e.nsitemastercode,e.sdescription, "
				+ "e.dmodifieddate, e.nsitecode, e.nstatus, s.ssitename as ssitename,"
				+ " COALESCE(p.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode()
				+ "',  p.jsondata->'speriodname'->>'" + userInfo.getSlanguagetypecode() + "') AS speriodname "
				+ " from siteexpirymapping e, site s, period p where e.nsiteexpirymappingcode > 0 and s.nsitecode = e.nsitemastercode "
				+ " and nsiteexpirymappingcode>0 and p.nperiodcode=e.nperiodcode" + " and e.nsitecode= " + userInfo.getNmastersitecode()
				+ " and e.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		;
		LOGGER.info("getSiteSampleExpiryMapping() called " + strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new SiteSampleExpiryMapping()), HttpStatus.OK);
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
	// added by sujatha ATE_274 to get the Login Site in the 1st value of dropdown on 01-09-2025
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
	 * This method is used to retrieve active siteexpirymapping object based on the
	 * specified nsiteexpirymappingcode.
	 * 
	 * @param nsiteexpirymappingcode [int] primary key of siteexpirymapping object
	 * @param userInfo               [UserInfo] holding logged in user details based
	 *                               on which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         siteexpirymapping object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public SiteSampleExpiryMapping getActiveSiteSampleExpiryMappingById(final int nsiteexpirymappingcode,
			final UserInfo userInfo) throws Exception {
		final String strQuery = "select m.nsiteexpirymappingcode, m.sexpirydays, m.nperiodcode, m.nsitemastercode,s.nsitecode, "
				+ " m.sdescription, m.dmodifieddate, m.nstatus, s.ssitename, " + " COALESCE(p.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "',  p.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "') AS speriodname " + " from siteexpirymapping m, site s, period p"
				+ " where m.nsitemastercode=s.nsitecode and m.nperiodcode=p.nperiodcode "
				+ " and m.nsiteexpirymappingcode= " + nsiteexpirymappingcode + " and m.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (SiteSampleExpiryMapping) jdbcUtilityTemplateFunction.queryForObject(strQuery,
				SiteSampleExpiryMapping.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to siteexpirymapping table.
	 * siteexpirymapping is unique across the database. Need to check for duplicate
	 * entry of siteexpirymapping for the specified site before saving into
	 * database. *
	 * 
	 * @param objSiteSampleExpiryMapping [SiteSampleExpiryMapping] object holding
	 *                                   details to be added in siteexpirymapping
	 *                                   table
	 * @param userInfo                   [UserInfo] holding logged in user details
	 *                                   based on which the list is to be fetched
	 * @return saved siteexpirymapping object with status code 200 if saved
	 *         successfully else if the siteexpirymapping for the specific site
	 *         already exists, response will be returned as 'Already Exists' with
	 *         status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createSiteSampleExpiryMapping(
			final SiteSampleExpiryMapping objSiteSampleExpiryMapping, final UserInfo userInfo) throws Exception {

		final SiteSampleExpiryMapping siteById = getSiteById(objSiteSampleExpiryMapping.getNsitecode(),
				userInfo.getNmastersitecode());

		if (siteById == null) {
			final String sQuery = " lock table siteexpirymapping "
					+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
			jdbcTemplate.execute(sQuery);

			final List<String> multilingualIdList = new ArrayList<>();
			final List<Object> saveSiteSampleExpiryMappingList = new ArrayList<>();

			String sequenceNoQuery = " select nsequenceno from seqnostoragemanagement where stablename ='siteexpirymapping' "
					+ "and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
			nsequenceNo++;
			final String insertQuery = " insert into siteexpirymapping (nsiteexpirymappingcode, sexpirydays, nperiodcode, nsitemastercode, "
					+ " sdescription, dmodifieddate, nsitecode, nstatus )" + " values(" + nsequenceNo + ",N'"
					+ stringUtilityFunction.replaceQuote(objSiteSampleExpiryMapping.getSexpirydays()) + "' ,"
					+ objSiteSampleExpiryMapping.getNperiodcode() + " ," + objSiteSampleExpiryMapping.getNsitecode()
					+ ",N'" + stringUtilityFunction.replaceQuote(objSiteSampleExpiryMapping.getSdescription()) + "' ,'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " )";
			jdbcTemplate.execute(insertQuery);

			final String updateQuery = " update seqnostoragemanagement set nsequenceno= " + nsequenceNo
					+ " where stablename='siteexpirymapping' and nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(updateQuery);
			objSiteSampleExpiryMapping.setNsiteexpirymappingcode(nsequenceNo);
			
			objSiteSampleExpiryMapping.setNsitemastercode(objSiteSampleExpiryMapping.getNsitecode());
			saveSiteSampleExpiryMappingList.add(objSiteSampleExpiryMapping);

			multilingualIdList.add("IDS_ADDSITESAMPLEEXPIRYMAPPING");

			auditUtilityFunction.fnInsertAuditAction(saveSiteSampleExpiryMappingList, 1, null, multilingualIdList,
					userInfo);
			return getSiteSampleExpiryMapping(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the siteexpirymapping object for the specified
	 * site.
	 * 
	 * @param nsitemastercode [int] name of the siteexpirymapping
	 * @param nmasterSiteCode [int] site code of the siteexpirymapping
	 * @return siteexpirymapping object based on the specified site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private SiteSampleExpiryMapping getSiteById(final int nsitemastercode, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select m.nsiteexpirymappingcode from siteexpirymapping m, site s "
				+ " where m.nsitemastercode=s.nsitecode and s.nsitecode=" + nsitemastercode + " and m.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and m.nsitecode= " + nmasterSiteCode;
		return (SiteSampleExpiryMapping) jdbcUtilityTemplateFunction.queryForObject(strQuery,
				SiteSampleExpiryMapping.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in siteexpirymapping table. Need to
	 * validate that the siteexpirymapping object to be updated is active before
	 * updating details in database. Need to check for duplicate entry of
	 * siteexpirymapping for the specified site before saving into database.
	 * 
	 * @param objSiteSampleExpiryMapping [SiteSampleExpiryMapping] object holding
	 *                                   details to be updated in siteexpirymapping
	 *                                   table
	 * @param userInfo                   [UserInfo] holding logged in user details
	 *                                   based on which the list is to be fetched
	 * @return saved siteexpirymapping object with status code 200 if saved
	 *         successfully else if the siteexpirymapping already exists, response
	 *         will be returned as 'Already Exists' with status code 409 else if the
	 *         unit to be updated is not available, response will be returned as
	 *         'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateSiteSampleExpiryMapping(
			final SiteSampleExpiryMapping objSiteSampleExpiryMapping, final UserInfo userInfo) throws Exception {

		final SiteSampleExpiryMapping activeSiteExpiryMap = getActiveSiteSampleExpiryMappingById(
				objSiteSampleExpiryMapping.getNsiteexpirymappingcode(), userInfo);
		if (activeSiteExpiryMap == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> listBeforeUpdate = new ArrayList<>();
			final List<Object> listAfterUpdate = new ArrayList<>();

			final String queryString = " select m.nsiteexpirymappingcode from siteexpirymapping m , site s"
					+ " where m.nsitemastercode=" + objSiteSampleExpiryMapping.getNsitecode()
					+ " and m.nsitecode=s.nsitecode" + " and m.nsiteexpirymappingcode<> "
					+ objSiteSampleExpiryMapping.getNsiteexpirymappingcode() + " and m.nsitecode= "
					+ userInfo.getNmastersitecode() + " and m.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final SiteSampleExpiryMapping availableSiteSampleExMapping = (SiteSampleExpiryMapping) jdbcUtilityTemplateFunction
					.queryForObject(queryString, SiteSampleExpiryMapping.class, jdbcTemplate);
			if (availableSiteSampleExMapping == null) {
				final String updateQuery = " update siteexpirymapping set sexpirydays=N'"
						+ stringUtilityFunction.replaceQuote(objSiteSampleExpiryMapping.getSexpirydays())
						+ "',nperiodcode=" + objSiteSampleExpiryMapping.getNperiodcode() + ", nsitemastercode="
						+ objSiteSampleExpiryMapping.getNsitecode() + " ,sdescription=N'"
						+ stringUtilityFunction.replaceQuote(objSiteSampleExpiryMapping.getSdescription())
						+ "', dmodifieddate= '" + dateUtilityFunction.getCurrentDateTime(userInfo)
						+ "' where nsiteexpirymappingcode= " + objSiteSampleExpiryMapping.getNsiteexpirymappingcode()
						+ " and nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				jdbcTemplate.execute(updateQuery);
				objSiteSampleExpiryMapping.setNsitemastercode(objSiteSampleExpiryMapping.getNsitecode());
				listAfterUpdate.add(objSiteSampleExpiryMapping);
				listBeforeUpdate.add(activeSiteExpiryMap);

				multilingualIDList.add("IDS_EDITSITESAMPLEEXPIRYMAPPING");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

				return getSiteSampleExpiryMapping(userInfo);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method id used to delete siteexpirymapping, only if exits else throws
	 * Alert 'Already Deleted'
	 * 
	 * @param objSiteSampleExpiryMapping [SiteSampleExpiryMapping] an Object holds
	 *                                   the record to be deleted
	 * @param userInfo                   [UserInfo] holding logged in user details
	 *                                   based on which the list is to be fetched
	 * @return a response entity with list of available siteexpirymapping objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteSiteSampleExpiryMapping(
			final SiteSampleExpiryMapping objSiteSampleExpiryMapping, final UserInfo userInfo) throws Exception {

		final SiteSampleExpiryMapping activeSiteSampleExpiry = getActiveSiteSampleExpiryMappingById(
				objSiteSampleExpiryMapping.getNsiteexpirymappingcode(), userInfo);
		if (activeSiteSampleExpiry == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> deletedSiteSampleExpiryMappingList = new ArrayList<>();
			final String updateQuery = " update siteexpirymapping set dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' , nstatus="
					+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nsiteexpirymappingcode="
					+ objSiteSampleExpiryMapping.getNsiteexpirymappingcode();

			jdbcTemplate.execute(updateQuery);
			objSiteSampleExpiryMapping.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
			deletedSiteSampleExpiryMappingList.add(objSiteSampleExpiryMapping);
			multilingualIDList.add("IDS_DELETESITESAMPLEEXPIRYMAPPING");

			auditUtilityFunction.fnInsertAuditAction(deletedSiteSampleExpiryMappingList, 1, null, multilingualIDList,
					userInfo);
			return getSiteSampleExpiryMapping(userInfo);
		}
	}

	/**
	 * This method is used to retrieve single or list of period for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return response entity object holding response status and single or list of period's.
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getPeriod(final UserInfo userInfo) throws Exception {

		String strQuery = "select pc.ndefaultstatus, p.nperiodcode, " + " COALESCE(p.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "',  p.jsondata->'speriodname'->>'"
				+ userInfo.getSlanguagetypecode() + "') AS speriodname "
				+ " from period p, periodconfig pc where p.nperiodcode=pc.nperiodcode and nformcode="
				+ userInfo.getNformcode()+ " and p.nperiodcode="+Enumeration.Period.Days.getPeriod()
				+ " and p.nstatus= "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and p.nsitecode= "
				+ userInfo.getNmastersitecode();
		LOGGER.info(strQuery);

		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new Period()), HttpStatus.OK);
	}
}
