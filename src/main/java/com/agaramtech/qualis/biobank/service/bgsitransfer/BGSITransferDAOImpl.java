package com.agaramtech.qualis.biobank.service.bgsitransfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.Unit;
import com.agaramtech.qualis.basemaster.service.unit.UnitDAO;
import com.agaramtech.qualis.basemaster.service.unit.UnitDAOImpl;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
//import com.twilio.Twilio;
//import com.twilio.rest.api.v2010.account.Message;

import lombok.AllArgsConstructor;


/**
 * This class is used to perform CRUD Operation on "transfer" table by 
 * implementing methods from its interface. 
 */

@AllArgsConstructor
@Repository
public class BGSITransferDAOImpl implements BGSITransferDAO{

    private static final Logger LOGGER = LoggerFactory.getLogger(BGSITransferDAOImpl.class);
	
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	
	/**
	 * This method is used to retrieve list of all available transfer records.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active transfer records
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getBGSITransfer(final UserInfo userInfo) throws Exception {
		
		
//		final String strQuery = "select a.ntransfercode,a.nsitecode,s.ssitename,a.ndestinationsitecode,"
//				+ " (select s1.ssitename from site s1 where a.ndestinationsitecode = s1.nsitecode) destinationsitename,"
//				+ " stransferformno,dtransferdate,dactualtransferdate,stemperature,"
//				+ " ssendername,scouriername,remarks,'Booked' status,stechnicianname,"
//				+ " createddate,dupdateddate,snonsitename,striplepackaging "
//				+ " from bgsitransfer a,site s where a.nsitecode = s.nsitecode order by a.ntransfercode;";
//		final String strQuery = "select a.ntransfercode,a.nsitecode,s.ssitename,a.ndestinationsitecode,"
//				+ " (select s1.ssitename from site s1 where a.ndestinationsitecode = s1.nsitecode) destinationsitename,"
//				+ " stransferformno,a.dtransferdate,dactualtransferdate,a.stemperature,"
//				+ " ssendername,a.scouriername,remarks,'Booked' status,'Dikirim' transferstatus,stechnicianname,"
//				+ " createddate,dupdateddate,snonsitename,striplepackaging,b.dtransferdate dtransferdetaildate"
//				+ " from bgsitransfer a,site s,bgsitransferdetails b where a.nsitecode = s.nsitecode and "
//				+ " a.ntransfercode = b.ntransfercode;";
		
		final String strQuery = "select a.ntransfercode,a.nsitecode,s.ssitename,a.ndestinationsitecode,"
				+ " (select s1.ssitename from site s1 where a.ndestinationsitecode = s1.nsitecode) destinationsitename,"
				+ " stransferformno,a.dtransferdate,COALESCE(TO_CHAR(a.dtransferdate, '"
				+ userInfo.getSpgsitedatetime() + "'), '') AS stransferdate, dactualtransferdate,COALESCE(TO_CHAR(dactualtransferdate, '"
				+ userInfo.getSpgsitedatetime() + "'), '') AS actualtransferdate,a.stemperature,"
				+ " ssendername,a.scouriername,remarks,'Booked' status,'Dikirim' transferstatus,stechnicianname,"
				+ " createddate,dupdateddate,snonsitename,striplepackaging,"
				+ " (select distinct COALESCE(TO_CHAR(b.dtransferdate, '"
				+ userInfo.getSpgsitedatetime() + "'), '') from bgsitransferdetails b where b.ntransfercode = a.ntransfercode) stransferdetaildate "
				+ " from bgsitransfer a,site s where a.nsitecode = s.nsitecode order by a.ntransfercode;";
		
		List<Map<String,Object>> transferSampleData = jdbcTemplate.queryForList(strQuery);
		LOGGER.info("Get Method:"+ strQuery);
		return new ResponseEntity<Object>(transferSampleData, HttpStatus.OK);
	}
	
	/**
	 * This method is used to retrieve active transfer details records based on the specified ntransferCode.
	 * @param ntransferCode [int] primary key of transfer object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of transfer object
	 * @throws Exception that are thrown from this DAO layer
	 */	
	@Override
	public ResponseEntity<Object>  getActiveTransferDertailsByTransferCode(int ntransferCode, UserInfo userInfo) throws Exception {
		final String strQuery = "select ntransferdetcode,ntransfercode,stransferid,COALESCE(TO_CHAR(dtransferdate, '"
				+ userInfo.getSpgsitedatetime() + "'), '') AS stransferdate,stemperature,"
				+ " scouriername,sbiosampleid,stransferformulirid,'Booked' status,'Dikirim' transferstatus,"
				+ " srepositorycode,spositioncode,sparentsamplecode,ncohortno,storagecode,nqty from "
				+ " bgsitransferdetails where ntransfercode = " + ntransferCode;
		List<Map<String,Object>> transferDetailData = jdbcTemplate.queryForList(strQuery);
		LOGGER.info("Get Method:"+ strQuery);
		return new ResponseEntity<Object>(transferDetailData, HttpStatus.OK);
	}	
}
