package com.agaramtech.qualis.basemaster.service.timezone;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.TimeZone;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

import lombok.AllArgsConstructor;


/**
 * This class is used to perform CRUD Operation on "timezone" table by 
 * implementing methods from its interface. 
 */
@AllArgsConstructor
@Repository
public class TimeZoneDAOImpl implements TimeZoneDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimeZoneDAOImpl.class);
	
	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;

	
	@Override
	public ResponseEntity<Object> getTimeZone() throws Exception
	{
		//ALPDJ21-85 - UTC - L.Subashini -16/09/2025
		String strQuery = "select ssettingvalue,ssettingname,nsettingcode from settings where nsettingcode = " + Enumeration.Settings.NEED_UTC.getNsettingcode()
							+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Settings setting = (Settings) jdbcTemplate.queryForObject(strQuery, new Settings());
		
		String conditionalString = "";
		if (setting != null && Integer.parseInt(setting.getSsettingvalue()) == Enumeration.TransactionStatus.NO.gettransactionstatus())
		{
			conditionalString = " and ntimezonecode = " + Enumeration.TransactionStatus.NA.gettransactionstatus();
		}
		strQuery	= "select * from timezone where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ conditionalString;	
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new TimeZone()), HttpStatus.OK);
		
	}

	
	@Override
	public ResponseEntity<Object> getLocalTimeByZone(UserInfo userInfo) throws Exception {
		String date = "";
		final Map<String,Object> map =new HashMap<>();
		if (userInfo.getIsutcenabled()==Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			date = dateUtilityFunction.convertUTCToSiteTime(Instant.now(), userInfo);
		}
		else {
			final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(userInfo.getSdatetimeformat());  
			final LocalDateTime now = LocalDateTime.now(); 
			date = dtf.format(now); 
		      
		}
		LOGGER.info("date:"+date);
		map.put("date", date);
		//return new ResponseEntity<Object>(date,HttpStatus.OK);
		return new ResponseEntity<Object>(map,HttpStatus.OK);
	}
}