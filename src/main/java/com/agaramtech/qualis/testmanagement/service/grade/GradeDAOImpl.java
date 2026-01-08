package com.agaramtech.qualis.testmanagement.service.grade;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.Grade;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class GradeDAOImpl implements GradeDAO {
	
	 private static final Logger LOGGER = LoggerFactory.getLogger(GradeDAOImpl.class);	  
	 
	 // private final CommonFunction commonFunction; 
	  private final JdbcTemplate jdbcTemplate; 

	  @Override
		public ResponseEntity<Object> getGrade(UserInfo objUserInfo) throws Exception {
		  
		    //jsoncombodata, scombodisplaystatus added by L.Subashini - 06/09/2025 for HSPS
			final String sGradeQuery=" select ngradecode, sgradename, "
					+ " coalesce(jsondata->'sdisplayname'->>'"+objUserInfo.getSlanguagetypecode()+"', jsondata->'sdisplayname'->>'en-US') sdisplaystatus ,"
					+ " coalesce(jsoncombodata->'sdisplayname'->>'"+objUserInfo.getSlanguagetypecode()+"', jsoncombodata->'sdisplayname'->>'en-US') scombodisplaystatus ,"
					+ " ndefaultstatus from grade where "
					+ " nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and ngradecode>0;";
			LOGGER.info("Get Method:"+ sGradeQuery);
//			return new ResponseEntity<Object>(commonFunction.getMultilingualMessageList((List<Grade>)
//															jdbcTemplate.query(sGradeQuery,new Grade()), 
//															Arrays.asList("sdisplaystatus", "scombodisplaystatus"), 
//															objUserInfo.getSlanguagefilename()), 
//												HttpStatus.OK);
			
			return new ResponseEntity<Object>(jdbcTemplate.query(sGradeQuery,new Grade()),
																HttpStatus.OK);
		}
}
