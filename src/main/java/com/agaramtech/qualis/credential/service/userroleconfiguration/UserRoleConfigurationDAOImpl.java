package com.agaramtech.qualis.credential.service.userroleconfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.credential.model.UserRoleConfig;
import com.agaramtech.qualis.credential.model.UserRoleConfigType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class UserRoleConfigurationDAOImpl implements UserRoleConfigurationDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserRoleConfigurationDAOImpl.class);

	private final JdbcTemplate jdbcTemplate;

	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;

	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getUserRoleConfiguration(final UserInfo userInfo)
			throws Exception {
		String strQuery = "";
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		if (userInfo.getNlogintypecode() == Enumeration.LoginType.INTERNAL.getnlogintype()) {
			strQuery = "select urc.*,ur.suserrolename from userrole ur "
					+ "join userrolepolicy urp on urp.nuserrolecode =ur.nuserrolecode "
					+ "join userroleconfig urc on urc.nuserrolecode=urp.nuserrolecode "
					+ "where urp.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and urp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and urc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and urc.nuserrolecode> 0 and ur.nsitecode=" + userInfo.getNmastersitecode();

		} else if (userInfo.getNlogintypecode() == Enumeration.LoginType.ADS.getnlogintype()) {
			strQuery = "select a.*, b.suserrolename from userroleconfig a, userrole b where a.nuserrolecode = b.nuserrolecode and a.nstatus = b.nstatus and a.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "and a.nuserrolecode > 0 and b.nsitecode = " + userInfo.getNmastersitecode();
		}
		
        //Ate234 Janakumar BGSI-50 User Role Configuration -> filter-based record change.

		final List<UserRoleConfig> lstUserRoleConfig = jdbcTemplate.query(strQuery, new UserRoleConfig());

		outputMap.put("lstUserRoleConfig", lstUserRoleConfig);

		final String strQueryFilter = "select nuserroleconfigtypecode, suserroleconfigtype, sdescription, nsorter, coalesce(jsondata->'suserroleconfigtype'->>'"
				+ userInfo.getSlanguagetypecode() + "',jsondata->'suserroleconfigtype'->>'en-US') as sdisplayname"
				+ " from userroleconfigtype where nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and  nuserroleconfigtypecode>0  order by nsorter asc ";

		final List<UserRoleConfigType> lstuserroleconfigtype = jdbcTemplate.query(strQueryFilter,
				new UserRoleConfigType());

		outputMap.put("lstuserroleconfigtype", lstuserroleconfigtype);

		outputMap.put("defaultValueUserRoleConfig", lstuserroleconfigtype.get(0));

		LOGGER.info("Get Method:" + strQuery);
		
        //Ate234 Janakumar BGSI-50 User Role Configuration -> filter-based record change.


		// return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new
		// UserRoleConfig()), HttpStatus.OK);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	@Override
	public UserRoleConfig getActiveUserRoleConfigurationById(final int nuserRoleCode) throws Exception {
		final String strQuery = "select a.*, b.suserrolename from userroleconfig a, userrole b where a.nuserrolecode = b.nuserrolecode and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nuserrolecode = "
				+ nuserRoleCode;

		return (UserRoleConfig) jdbcUtilityFunction.queryForObject(strQuery, UserRoleConfig.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> updateUserRoleConfiguration(final UserRoleConfig userRoleConfig,
			final UserInfo userInfo) throws Exception {

		final UserRoleConfig oldUserRoleList = getActiveUserRoleConfigurationById(userRoleConfig.getNuserrolecode());

		if (userRoleConfig.getNneedresultflow() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			final String updateQueryString1 = "update userroleconfig set nneedresultflow = "
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			jdbcTemplate.execute(updateQueryString1);
		}
        //Ate234 Janakumar BGSI-50 User Role Configuration -> filter-based record change.    --- Start

		if (userRoleConfig.getNneedtechnicianflow() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			final String updateQueryString1 = "update userroleconfig set nneedtechnicianflow = "
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			jdbcTemplate.execute(updateQueryString1);
		}
		if (userRoleConfig.getNneedprojectinvestigatorflow() == Enumeration.TransactionStatus.YES
				.gettransactionstatus()) {
			final String updateQueryString1 = "update userroleconfig set nneedprojectinvestigatorflow = "
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			jdbcTemplate.execute(updateQueryString1);

		}
//		BGSI-50	Able to enable multiple Third Party column checkboxes (Vishakh/19-09-2025)
//		if (userRoleConfig.getNneedthirdpartyflow() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
//			final String updateQueryString1 = "update userroleconfig set nneedthirdpartyflow = "
//					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nstatus= "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
//
//			jdbcTemplate.execute(updateQueryString1);
//		}
		
		// added by sujatha ATE_274 for added new column for BGSI-178 allowing only 1 role in that column enabled
		if (userRoleConfig.getNneedngsflow() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final String updateQueryString1 = "update userroleconfig set nneedngsflow = "
						+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(updateQueryString1);
		}
		// added by sujatha ATE_274 for having only 1 role in that column enabled
		if (userRoleConfig.getNneedthirdpartyflow() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final String updateQueryString1 = "update userroleconfig set nneedthirdpartyflow = "
						+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " where nstatus= "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				jdbcTemplate.execute(updateQueryString1);
		}
		//modified by sujatha ATE_274 by adding nneedngsflow in the table 		
		final String updateQueryString = "update userroleconfig set dmodifieddate='"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nneedapprovalflow = "
				+ userRoleConfig.getNneedapprovalflow() + " , nneedresultflow = " + userRoleConfig.getNneedresultflow()
				+ ", " + " nneedprojectflow =" + userRoleConfig.getNneedprojectflow() + " ,nneedtechnicianflow="
				+ userRoleConfig.getNneedtechnicianflow() + " , nneedprojectinvestigatorflow="
				+ userRoleConfig.getNneedprojectinvestigatorflow() + " ,nneedthirdpartyflow="
				+ userRoleConfig.getNneedthirdpartyflow() + " , nneedngsflow="+userRoleConfig.getNneedngsflow()
				+ "  where nuserrolecode="
				+ userRoleConfig.getNuserrolecode();
		jdbcTemplate.execute(updateQueryString);
		final List<String> multilingualIDList = new ArrayList<>();
		multilingualIDList.add("IDS_EDITUSERROLECONFIG");
		final List<Object> savedUserRoleList = new ArrayList<>();
		savedUserRoleList.add(userRoleConfig);
		final List<Object> prevUserRoleList = new ArrayList<>();
		prevUserRoleList.add(oldUserRoleList);
		auditUtilityFunction.fnInsertAuditAction(savedUserRoleList, 2, prevUserRoleList, multilingualIDList, userInfo);
		return getUserRoleConfigurationupdate(userInfo, userRoleConfig.getNuserroleconfigtypecode());

	}
	
	
	private ResponseEntity<Object> getUserRoleConfigurationupdate(final UserInfo userInfo,final int nuserroleconfigtypecode)
			throws Exception {
		String strQuery = "";
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		if (userInfo.getNlogintypecode() == Enumeration.LoginType.INTERNAL.getnlogintype()) {
			strQuery = "select urc.*,ur.suserrolename from userrole ur "
					+ "join userrolepolicy urp on urp.nuserrolecode =ur.nuserrolecode "
					+ "join userroleconfig urc on urc.nuserrolecode=urp.nuserrolecode "
					+ "where urp.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ " and urp.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and ur.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and urc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and urc.nuserrolecode> 0 and ur.nsitecode=" + userInfo.getNmastersitecode();

		} else if (userInfo.getNlogintypecode() == Enumeration.LoginType.ADS.getnlogintype()) {
			strQuery = "select a.*, b.suserrolename from userroleconfig a, userrole b where a.nuserrolecode = b.nuserrolecode and a.nstatus = b.nstatus and a.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "and a.nuserrolecode > 0 and b.nsitecode = " + userInfo.getNmastersitecode();
		}
		final List<UserRoleConfig> lstUserRoleConfig = jdbcTemplate.query(strQuery, new UserRoleConfig());

		outputMap.put("lstUserRoleConfig", lstUserRoleConfig);

		List<UserRoleConfigType> lstuserroleconfigtypedefault=null;
		if (nuserroleconfigtypecode > 0) {
			
			final String strQueryFilter = "select nuserroleconfigtypecode, suserroleconfigtype, sdescription, nsorter, coalesce(jsondata->'suserroleconfigtype'->>'"
					+ userInfo.getSlanguagetypecode() + "',jsondata->'suserroleconfigtype'->>'en-US') as sdisplayname"
					+ " from userroleconfigtype where nuserroleconfigtypecode>0 and nuserroleconfigtypecode="+nuserroleconfigtypecode+" order by nsorter asc ";

			 lstuserroleconfigtypedefault = jdbcTemplate.query(strQueryFilter,
					new UserRoleConfigType());
			
		}

		final String strQueryFilter = "select nuserroleconfigtypecode, suserroleconfigtype, sdescription, nsorter, coalesce(jsondata->'suserroleconfigtype'->>'"
				+ userInfo.getSlanguagetypecode() + "',jsondata->'suserroleconfigtype'->>'en-US') as sdisplayname"
				+ " from userroleconfigtype where nuserroleconfigtypecode>0 and nstatus ="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"   order by nsorter asc ";

		final List<UserRoleConfigType> lstuserroleconfigtype = jdbcTemplate.query(strQueryFilter,
				new UserRoleConfigType());

		outputMap.put("lstuserroleconfigtype", lstuserroleconfigtype);

		outputMap.put("defaultValueUserRoleConfig", lstuserroleconfigtypedefault.get(0));
		
        //Ate234 Janakumar BGSI-50 User Role Configuration -> filter-based record change.    ---end


		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}


}
