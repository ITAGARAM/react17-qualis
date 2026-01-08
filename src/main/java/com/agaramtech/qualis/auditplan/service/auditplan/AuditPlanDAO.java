package com.agaramtech.qualis.auditplan.service.auditplan;

import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.auditplan.model.AuditPlanAuditor;
import com.agaramtech.qualis.auditplan.model.AuditPlanMember;
import com.agaramtech.qualis.global.UserInfo;
/**
 * This interface holds declarations to perform CRUD operation on auditplan','auditplanhistory','auditplanmembers','auditplanauditors' table
 */
/**
* @author AT-E143 SWSM-6 05/08/2025
*/
public interface AuditPlanDAO {
	/**
	 * This DAO interface declaration will access the DAO layer calls the getFilterStatus and getAuditPlanData methods and that is used 
	 * to get all the available auditplan with respect to site and transactionstatus.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of auditplan records with respect to site and transactionstatus table.
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getAuditPlan(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This service interface declaration will access the DAO layer that is used 
	 * to get all the available auditplan with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of auditplan records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getAuditPlanData(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used 
	 * to get all the available auditmaster with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of auditmaster records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Map<String,Object> getAuditMaster(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used 
	 * to get all the available audittype with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of audittype records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Map<String,Object> getAuditType(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used 
	 * to get all the available auditcategory with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of auditcategory records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Map<String,Object> getAuditCategory(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used 
	 * to get all the available auditstandardcategory with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of auditstandardcategory records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Map<String,Object> getAuditStandardCategory(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used 
	 * to get all the available department with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of department records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Map<String,Object> getDepartment(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used 
	 * to get all the available users based on the department and respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of users records based on the department  and respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Map<String,Object> getDepartmentHead(final int ndepartmentCode,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used 
	 * to get all the available users with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of users records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public Map<String,Object> getUsers(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to
	 * add a new entry to "auditplan","auditplanhistory","auditplanmembers","auditplanauditors" table
	 * @param inputMap [Map] map object holding details to be added in "auditplan","auditplanhistory","auditplanmembers","auditplanauditors" table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added auditplan object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createAuditPlan(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to retrieve active auditplan object based
	 * on the specified nauditPlanCode.
	 * @param nauditPlanCode [int] primary key of auditplan object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of auditplan object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveAuditPlanById(final int nauditPlanCode,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to retrieve active auditplan object based
	 * on the specified nauditPlanCode.
	 * @param nauditPlanCode [int] primary key of auditplan object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of auditplan object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSelectionAuditPlanById(final int nauditPlanCode,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to
	 *  update entry in auditplan  table.
	 * @param objAuditPlan [AuditPlan] object holding details to be updated in auditplan table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated auditplan object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateAuditPlan(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to delete an entry in auditplan table.
	 * @param objAuditPlan [AuditPlan] object holding detail to be deleted from auditplan table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted auditplan object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteAuditPlan(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to retrieve list of Audit Date & Time,Scheduled,Rescheduled,Closed based on the transaction status. 
	 * @param inputMap  [Map] map object with "soperation" and "userinfo" as keys for which the data is to be fetched
	 * @return response entity object holding response status and list of all data based on the transactionstatus
	 * @throws Exception exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getTransactionDates(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to make a new entry to "auditplanhistory" table and to update selected auditplan table .
	 * @param objAuditPlan [AuditPlan] object holding details to be updated in auditplan table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated auditplan object
	 * @throws Exception exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> scheduleAuditPlan(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to make a new entry to "auditplanhistory" table and to update selected auditplan table .
	 * @param objAuditPlan [AuditPlan] object holding details to be updated in auditplan table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated auditplan object
	 * @throws Exception exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> closeAuditPlan(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used 
	 * to get all the available users for the selected audit plan and respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of users records for the selected audit plan and respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getAvailableAuditPlanMember(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used 
	 * to get all the available auditmaster for the selected audit plan and respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of auditmaster records for the selected audit plan and respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getAvailableAuditPlanAuditor(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to
	 * add a new entry to "auditplanmembers" table
	 * @param lstAuditPlanMember [AuditPlanMember] list holding details to be added in "auditplanmembers" table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added auditplanmembers object
	 * @throws Exception that are thrown in the DAO layer
	 */
	//Modified by sonia on 9th sept 2025 for jira id:SWSM-6
	public ResponseEntity<Object> createAuditPlanMember(final List<AuditPlanMember> lstAuditPlanMember,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to
	 * add a new entry to "auditplanauditors" table
	 * @param lstAuditPlanAuditor [AuditPlanAuditor] list holding details to be added in "auditplanauditors" table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added auditplanauditors object
	 * @throws Exception that are thrown in the DAO layer
	 */
	//Modified by sonia on 9th sept 2025 for jira id:SWSM-6
	public ResponseEntity<Object> createAuditPlanAuditor(List<AuditPlanAuditor> lstAuditPlanAuditor,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to delete an entry in auditplanmembers table.
	 * @param objAuditPlanMember [AuditPlanMember] object holding detail to be deleted from auditplanmembers table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted auditplanmembers object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteAuditPlanMember(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to delete an entry in auditplanauditors table.
	 * @param objAuditPlanAuditor [AuditPlanAuditor] object holding detail to be deleted from auditplanauditors table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted auditplanauditors object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteAuditPlanAuditor(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to retrieve active auditplanfile object based
	 * on the specified nauditPlanFileCode.
	 * @param inputMap [Map] map object holding details contains "nauditplancode" & "nauditplanfilecode"
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of auditplan object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveAuditPlanFileById(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception ;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to
	 * add a new entry to "auditplanfile" table
	 * @param inputMap [Map] map object holding details to be added in "auditplanfile" table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added auditplanfile object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createAuditPlanFile(final UserInfo userInfo, final MultipartHttpServletRequest request) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to
	 *  update entry in auditplanfile  table.
	 * @param inputMap [Map] map object holding details to be updated in auditplanfile table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated auditplanfile object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateAuditPlanFile(final UserInfo userInfo, final MultipartHttpServletRequest request) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to delete an entry in auditplanfile table.
	 * @param objAuditPlanFile [AuditPlanFile] object holding detail to be deleted from auditplanfile table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted auditplanfile object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteAuditPlanFile(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
	/**
	 * This DAO interface declaration will access the DAO layer that is used to fetch a file/ link which need to view
	 * @param inputMap [Map] object with keys of AuditPlanFile entity and UserInfo object.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
  								which the list is to be fetched
	 * @return response entity holds the list of AuditPlanFile details	
	 * @throws Exception that are thrown in the DAO layer
	 */ 
	public ResponseEntity<Object> viewAuditPlanFile(final Map<String, Object> inputMap,final UserInfo userInfo) throws Exception;
}
