package com.agaramtech.qualis.auditplan.service.auditmaster;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.auditplan.model.AuditMaster;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import lombok.AllArgsConstructor;
/**
 * This class is used to perform CRUD Operation on "auditmaster" table by 
 * implementing methods from its interface. 
 */
/**
* @author AT-E143 SWSM-2 22/07/2025
*/
@AllArgsConstructor
@Repository
public class AuditMasterDAOImpl implements AuditMasterDAO  {

private static final Logger LOGGER = LoggerFactory.getLogger(AuditMasterDAOImpl.class);
	
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	
	/**
	 * This method is used to retrieve list of all available auditmaster for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active auditmaster
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getAuditMaster(final UserInfo userInfo) throws Exception {
		
		final String strQuery = " select am.*,coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
							  + " ts.jsondata->'stransdisplaystatus'->>'en-US') as sleadauditor from auditmaster am "
							  + " join transactionstatus ts on am.nleadauditor =ts.ntranscode "
				              + " where ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				              + " and am.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
				              + " and am.nsitecode ="+userInfo.getNmastersitecode()+" and am.nauditmastercode >0 ";
		LOGGER.info("Get Method:"+ strQuery);
		return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new AuditMaster()), HttpStatus.OK);
	}	
	/**
	 * This method is used to retrieve active auditmaster object based on the specified nauditMasterCode.
	 * @param nauditMasterCode [int] primary key of auditmaster object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of auditmaster object
	 * @throws Exception that are thrown from this DAO layer
	 */	
	public AuditMaster getActiveAuditMasterById(int nauditMasterCode, UserInfo userInfo) throws Exception {
		final String strQuery = " select am.*,coalesce(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
				  		      + " ts.jsondata->'stransdisplaystatus'->>'en-US') as sleadauditor from auditmaster am "
				              + " join transactionstatus ts on am.nleadauditor =ts.ntranscode "
	                          + " where ts.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
	                          + " and am.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
	                          + " and am.nauditmastercode="+nauditMasterCode+" ";
		return (AuditMaster) jdbcUtilityFunction.queryForObject(strQuery, AuditMaster.class, jdbcTemplate);
	}	
	/**
	 * This method is used to add a new entry to auditmaster table.
	 * @param objAuditMaster [AuditMaster] object holding details to be added in auditmaster table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return saved auditmaster object with status code 200 if saved successfully 
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> createAuditMaster(AuditMaster objAuditMaster, UserInfo userInfo) throws Exception {		
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedAuditMasterList = new ArrayList<>();	
		final String sQuery = " lock  table auditmaster "+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);					
				
		final String sequenceNoQuery ="select nsequenceno from seqnoauditplan where stablename ='auditmaster'";
	   	int nsequenceNo = jdbcTemplate.queryForObject(sequenceNoQuery, Integer.class);
	   	nsequenceNo++;
	   		
	   	final String insertQuery = " Insert into auditmaster (nauditmastercode,sauditorname,nleadauditor,sdepartment,semail,sphoneno,sskilldetails,dmodifieddate,nsitecode,nstatus) "
	   							 + " values("+nsequenceNo+",N'"+ stringUtilityFunction.replaceQuote(objAuditMaster.getSauditorname())+"',"
	   							 + " "+objAuditMaster.getNleadauditor()+",N'" + stringUtilityFunction.replaceQuote(objAuditMaster.getSdepartment())+"', "
	   							 + " N'" + stringUtilityFunction.replaceQuote(objAuditMaster.getSemail())+"',N'" + stringUtilityFunction.replaceQuote(objAuditMaster.getSphoneno())+"',"
	   							 + " N'" + stringUtilityFunction.replaceQuote(objAuditMaster.getSskilldetails())+"','"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',"
	   							 + " "+userInfo.getNmastersitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" )";
	   			
	   	jdbcTemplate.execute(insertQuery);
	   		
	   	final String updateQuery ="update seqnoauditplan set nsequenceno ="+nsequenceNo+" where stablename='auditmaster'";
	   	jdbcTemplate.execute(updateQuery);
	   	objAuditMaster.setNauditmastercode(nsequenceNo);
	   	savedAuditMasterList.add(objAuditMaster);			
		multilingualIDList.add("IDS_ADDAUDITMASTER");			
		auditUtilityFunction.fnInsertAuditAction(savedAuditMasterList, 1, null, multilingualIDList, userInfo);
		return getAuditMaster(userInfo);			
		
	}	
	/**
	 * This method is used to update entry in auditmaster  table.
	 * Need to validate that the auditmaster object to be updated is active before updating 
	 * details in database.
	 * @param objAuditMaster [AuditMaster] object holding details to be updated in auditmaster table
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return saved auditmaster object with status code 200 if saved successfully 
	 *          else if the auditmaster to be updated is not available, response will be returned as 'Already Deleted' with status code 417
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> updateAuditMaster(AuditMaster objAuditMaster,UserInfo userInfo) throws Exception {
		
		final AuditMaster auditmaster =  getActiveAuditMasterById(objAuditMaster.getNauditmastercode(), userInfo);	
		if (auditmaster == null) 
		{
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} 
		else 
		{					
			final List<String> multilingualIDList  = new ArrayList<>();
			
			final List<Object> listAfterUpdate = new ArrayList<>();		
			final List<Object> listBeforeUpdate = new ArrayList<>();
			
			final String updateQueryString = " update auditmaster set sauditorname=N'" + stringUtilityFunction.replaceQuote(objAuditMaster.getSauditorname())+"',"
										   + " sdepartment =N'" + stringUtilityFunction.replaceQuote(objAuditMaster.getSdepartment()) + "', "
										   + " semail =N'" + stringUtilityFunction.replaceQuote(objAuditMaster.getSemail()) + "',"
										   + " sphoneno =N'" + stringUtilityFunction.replaceQuote(objAuditMaster.getSphoneno()) + "', "
										   + " sskilldetails =N'" + stringUtilityFunction.replaceQuote(objAuditMaster.getSskilldetails()) + "', "
										   + " nleadauditor="+objAuditMaster.getNleadauditor()	+ ","
										   + " dmodifieddate='" + dateUtilityFunction.getCurrentDateTime(userInfo)+"'"												
										   + " where nauditmastercode=" + objAuditMaster.getNauditmastercode() +" and nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" ";
				
			jdbcTemplate.execute(updateQueryString);
				
			listAfterUpdate.add(objAuditMaster);
			listBeforeUpdate.add(auditmaster);				
			multilingualIDList.add("IDS_EDITAUDITMASTER");									
			auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList, userInfo);						
			//status code:200
			return getAuditMaster(userInfo);
			
		}
	}
	/**
	 * This method id used to delete an entry in auditmaster table
	 * Need to check the record is already deleted or not
	 * Need to check whether the record is used in other tables  such as 'auditplan'
	 * @param objAuditMaster [AuditMaster] an Object holds the record to be deleted
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return a response entity with list of available auditmaster objects
	 * @exception Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> deleteAuditMaster(AuditMaster objAuditMaster,UserInfo userInfo) throws Exception {
		
		final AuditMaster auditmaster =  getActiveAuditMasterById(objAuditMaster.getNauditmastercode(), userInfo);	
		if (auditmaster == null) {
			//status code:417
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		} 
		else {
			//added by sujatha ATE_274 for delete validation for auditplan SWSM-22 12-09-2025
			final String query="select 'IDS_AUDITPLAN' as Msg from auditplanauditors where nauditmastercode= " 
			        		  + objAuditMaster.getNauditmastercode() + " and nstatus=" 
			        		  + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//			        		  + " and nsitecode="+ userInfo.getNtranssitecode(); //commented by sujatha ATE_274 01-10-2025 for delete validation in different site will not through alert issue
			        		 
			
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);   			
			
			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
			{		
				validRecord = true;
				validatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(objAuditMaster.getNauditmastercode()), userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) 
				{					
					validRecord = true;
				}
				else {
					validRecord = false;
				}
			}
			
			if(validRecord) {
				final List<String> multilingualIDList  = new ArrayList<>();
				final List<Object> deletedAuditMasterList = new ArrayList<>();
				
				final String updateQueryString = " update auditmaster set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+ "',"
										       + " nstatus = "+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
											   + " where nauditmastercode=" + objAuditMaster.getNauditmastercode()+";";
					
			    jdbcTemplate.execute(updateQueryString);
			    objAuditMaster.setNstatus( (short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
							
			    deletedAuditMasterList.add(objAuditMaster);						
			    multilingualIDList.add("IDS_DELETEAUDITMASTER");
			    
			    auditUtilityFunction.fnInsertAuditAction(deletedAuditMasterList, 1, null, multilingualIDList, userInfo);
				
				return getAuditMaster(userInfo);
			}
			else{
				//status code:417
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}
	
}
