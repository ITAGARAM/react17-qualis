package com.agaramtech.qualis.invoice.service.invoicepreferencesetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.credential.model.UserRole;
import com.agaramtech.qualis.credential.model.UserRoleScreenControl;
import com.agaramtech.qualis.credential.model.UsersRoleScreen;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.invoice.model.FieldMaster;
import com.agaramtech.qualis.invoice.model.InvoicePreferenceSetting;
import com.agaramtech.qualis.invoice.model.UserRoleFieldControl;
import com.agaramtech.qualis.invoice.model.UsersRoleField;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class InvoicePreferenceSettingDAOImpl implements InvoicePreferenceSettingDAO {

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	/**
	 * This interface declaration is used to get single select screen rights for
	 * invoice preference settings
	 * 
	 * @param inputMap [Map<String, Object>] holding screen rights, user
	 *                 information, and user role code
	 * @return a response entity which holds the single select screen rights
	 * @throws Exception that are thrown in the Service layer
	 */
	@SuppressWarnings("unused")
	private List<InvoicePreferenceSetting> getInvoicePreferenceSettingListByName(final String svalue,
			final int nmasterSiteCode) throws Exception {
		final String strQuery = "select ninvoicesettingscode from invoicepreferencesetting where svalue = N'"
				+ stringUtilityFunction.replaceQuote(svalue) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (List<InvoicePreferenceSetting>) jdbcTemplate.query(strQuery, new InvoicePreferenceSetting());
	}

	/**
	 * This interface declaration is used to get all the available
	 * InvoicePreferenceSettings with respect to site
	 * 
	 * @param inputMap [Map<String, Object>] holding user information and user role
	 *                 preference code
	 * @return a response entity which holds the list of InvoicePreferenceSetting
	 *         records with respect to site
	 * @throws Exception that are thrown in the Service layer
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> getInvoicePreferenceSetting(final Integer nuserrolescreencode,
			final UserInfo objUserInfo) throws Exception {
		// TODO Auto-generated method stub
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final ControlMaster selectedUsersRoleScreen;
		final UserRole selectedUserRole;
		final Integer nuserrolecode;
		if (nuserrolescreencode == null) {
			final String query = "select nuserrolecode,suserrolename from userrole where nuserrolecode >0 and nsitecode="
					+ objUserInfo.getNmastersitecode() + "   and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final List<UserRole> lstuserrole = jdbcTemplate.query(query, new UserRole());

			objmap.put("userrole", lstuserrole);
			if (lstuserrole.size() == 0) {
				objmap.put("SelectedUserRole", null);
				objmap.put("SelectedScreenRights", lstuserrole);
				objmap.put("ScreenRights", lstuserrole);
				objmap.put("ControlRights", lstuserrole);

				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_NOUSERROLE", objUserInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				if (nuserrolescreencode == null) {
					selectedUserRole = ((UserRole) lstuserrole.get(lstuserrole.size() - 1));
					nuserrolecode = selectedUserRole.getNuserrolecode();
					objmap.putAll(
							(Map<String, Object>) getScreenRightsByUserRoleCode(nuserrolecode, objUserInfo).getBody());
				}
				return new ResponseEntity<>(objmap, HttpStatus.OK);
			}
		} else {
			final String query = "select * from usersrolescreen where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nuserrolescreencode="
					+ nuserrolescreencode + " and nsitecode= " + objUserInfo.getNmastersitecode();
			final List<UsersRoleScreen> selectedScreenRights = jdbcTemplate.query(query, new UsersRoleScreen());

			if (!selectedScreenRights.isEmpty()) {
				final String sQuery = "select q.nformcode,ur.suserrolename,sf.nsiteformscode,us.nuserrolecode,"
						+ " coalesce(q.jsondata->'sdisplayname'->>'" + objUserInfo.getSlanguagetypecode() + "',"
						+ " q.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
						+ " us.nuserrolescreencode from qualisforms q,"
						+ " usersrolescreen us,userrole ur,sitequalisforms sf"
						+ " where q.nformcode=sf.nformcode and us.nformcode=sf.nformcode  and us.nformcode=q.nformcode and ur.nuserrolecode=us.nuserrolecode "
						+ " and sf.nsitecode=" + objUserInfo.getNmastersitecode() + " and sf.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nuserrolecode="
						+ selectedScreenRights.get(selectedScreenRights.size() - 1).getNuserrolecode()
						+ " order by nuserrolescreencode asc";

				final List<UsersRoleScreen> userrolescreenList = jdbcTemplate.query(sQuery, new UsersRoleScreen());

				objmap.put("ScreenRights", userrolescreenList);
				objmap.put("SelectedScreenRights", selectedScreenRights);
				objmap.putAll((Map<String, Object>) getControlRights(objUserInfo,
						selectedScreenRights.get(selectedScreenRights.size() - 1).getNuserrolescreencode()).getBody());
			}
			return new ResponseEntity<>(objmap, HttpStatus.OK);
		}

	}

	/**
	 * This interface declaration is used to get all the available
	 * InvoicePreferenceSettings with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of InvoicePreferenceSetting
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	public ResponseEntity<Object> getScreenRightsByUserRoleCode(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final UsersRoleScreen selectedUsersRoleScreen;
		final String query = "select q.nformcode,ur.suserrolename,sf.nsiteformscode,"
				+ " coalesce(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " q.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
				+ " uf.nuserrolefieldcode  from qualisforms q," + " userrolefield uf,userrole ur,sitequalisforms sf"
				+ " where q.nformcode=sf.nformcode and uf.nformcode=sf.nformcode  " + " and sf.nsitecode="
				+ userInfo.getNmastersitecode() + " and sf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and uf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   and ur.nuserrolecode="
				+ nuserrolecode + "  order by nuserrolefieldcode asc";

		final List<UsersRoleField> usersrolescreenList = jdbcTemplate.query(query, new UsersRoleField());

		final String querys = "select * from userrole where nuserrolecode=" + nuserrolecode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode= "
				+ userInfo.getNmastersitecode();

		final List<UserRole> userroleList = jdbcTemplate.query(querys, new UserRole());
		outputMap.put("ScreenRights", usersrolescreenList);
		outputMap.put("SelectedUserRole", userroleList.get(0));
		if (usersrolescreenList.size() > 0) {
			final String Query = "select uf.*," + " coalesce(q.jsondata->'sdisplayname'->>'"
					+ userInfo.getSlanguagetypecode() + "'," + " q.jsondata->'sdisplayname'->>'en-US') as sdisplayname "
					+ "  from userrolefield uf , qualisforms q  where" + " q.nformcode=uf.nformcode and q.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and uf.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and uf.nuserrolefieldcode="
					+ usersrolescreenList.get(usersrolescreenList.size() - 1).getNuserrolefieldcode();

			final List<UsersRoleField> selectPreferenceSettings = jdbcTemplate.query(Query, new UsersRoleField());

			outputMap.put("SelectedScreenRights", selectPreferenceSettings);
			outputMap.putAll((Map<String, Object>) getControlRights(userInfo,
					selectPreferenceSettings.get(selectPreferenceSettings.size() - 1).getNuserrolefieldcode())
					.getBody());
		} else {
			outputMap.put("SelectedScreenRights", usersrolescreenList);
			outputMap.put("ControlRights", usersrolescreenList);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get all the available
	 * InvoicePreferenceSettings with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of InvoicePreferenceSetting
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getControlRights(final UserInfo userInfo, final Integer nuserrolefieldcode)
			throws Exception {
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();

		final String query = "select " + " coalesce(qs.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + " qs.jsondata->'sdisplayname'->>'en-US') as screenname, "
				+ " coalesce(c.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " c.jsondata->'scontrolids'->>'en-US') as scontrolids, "
				+ "urs.nuserrolefieldcontrolcode,u.nuserrolefieldcode, urs.nneedfield,"
				+ "s.nsitefieldcode,urs.nneedrights,c.*,u.nuserrolecode"
				+ " from sitefieldmaster s,userrolefield u,fieldmaster c,qualisforms qs,userrolefieldcontrol urs"
				+ " where qs.nformcode=s.nformcode and s.nsitecode=" + userInfo.getNmastersitecode()
				+ " and s.nformcode=u.nformcode" + " and u.nuserrolefieldcode=" + nuserrolefieldcode + " and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and c.nfieldcode=s.nfieldcode and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nformcode=s.nformcode"
				+ " and s.nformcode=u.nformcode and urs.nformcode=u.nformcode and s.nfieldcode=urs.nfieldcode"
				+ "  and urs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<FieldMaster> controlmaster = jdbcTemplate.query(query, new FieldMaster());
		objmap.put("ControlRights", controlmaster);
		return new ResponseEntity<>(objmap, HttpStatus.OK);

	}

	/**
	 * This interface declaration is used to get all available screens for invoice
	 * preference settings with respect to site
	 * 
	 * @param inputMap [Map<String, Object>] holding user information and user role
	 *                 code
	 * @return a response entity which holds the list of available screens with
	 *         respect to site
	 * @throws Exception that are thrown in the Service layer
	 */
	@SuppressWarnings({ "unused" })
	public ResponseEntity<Object> getAvailableScreen(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		if (nuserrolecode != null) {

			final String query = "select q.nformcode ," + nuserrolecode + " as nuserrolecode,"
					+ " coalesce(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ " q.jsondata->'sdisplayname'->>'en-US') as label, " + " coalesce(q.jsondata->'sdisplayname'->>'"
					+ userInfo.getSlanguagetypecode() + "'," + " q.jsondata->'sdisplayname'->>'en-US') as value, "
					+ " coalesce(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "',"
					+ " q.jsondata->'sdisplayname'->>'en-US') as sdisplayname, " + userInfo.getNmastersitecode()
					+ "as nsitecode,q.nstatus from qualisforms q,sitequalisforms sq"
					+ " where sq.nformcode = q.nformcode and q.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nmenucode=6 and q.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sq.nsitecode="
					+ userInfo.getNmastersitecode() + " and sq.nformcode not in  "
					+ "(select urs.nformcode from  userrolefield urs  where urs.nformcode = sq.nformcode "
					+ "  and urs.nuserrolecode =" + nuserrolecode + " and urs.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") " + " and sq.nformcode in  "
					+ "(select urs.nformcode from  userrolefield urs  where urs.nformcode = sq.nformcode "
					+ "  and urs.nuserrolecode =" + userInfo.getNuserrole() + " and urs.nstatus ="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") ";
			return new ResponseEntity<>(jdbcTemplate.query(query, new UsersRoleField()), HttpStatus.OK);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTUSERROLE", userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This interface declaration is used to get all the available
	 * InvoicePreferenceSettings with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of InvoicePreferenceSetting
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public Map<String, Object> getControlRightsSeqno(final Map<String, Object> objmap) throws Exception {
		final Map<String, Object> mapSeq = new HashMap<String, Object>();
		final String sQuery = " LOCK TABLE lockscreenrights";
		jdbcTemplate.execute(sQuery);

		final String StrQuery = "select nsequenceno from seqnocredentialmanagement where stablename = 'userrolescreencontrol'; ";
		final int nuserrolecontrolcode = jdbcTemplate.queryForObject(StrQuery, Integer.class);
		final String strUpdate = " update seqnocredentialmanagement set nsequenceno= " + (nuserrolecontrolcode) + ""
				+ " where stablename ='userrolescreencontrol';";
		jdbcTemplate.execute(strUpdate);
		mapSeq.put("nuserrolecontrolcode", (nuserrolecontrolcode));
		mapSeq.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
				Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
		return mapSeq;
	}

	/**
	 * This interface declaration is used to get single select screen rights for
	 * invoice preference settings
	 * 
	 * @param inputMap [Map<String, Object>] holding screen rights, user
	 *                 information, and user role code
	 * @return a response entity which holds the single select screen rights
	 * @throws Exception that are thrown in the Service layer
	 */
	@Override
	public ResponseEntity<Object> getSingleSelectScreenRights(final List<UsersRoleField> lstusersrolescreen,
			final UserInfo objUserInfo, final Integer nuserrolecode) throws Exception {
		// TODO Auto-generated method stub
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();
		final String query;

		final String suserrolescreencode;
		suserrolescreencode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen, "getNuserrolefieldcode");

		final String strUsersRoleScreen = "select * from userrolefield where nuserrolefieldcode in ("
				+ suserrolescreencode + ") " + " and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<UsersRoleField> checkActiveUsersRoleScreen = jdbcTemplate.query(strUsersRoleScreen,
				new UsersRoleField());

		if (checkActiveUsersRoleScreen.size() > 0) {

			query = "select " + " coalesce(qs.jsondata->'sdisplayname'->>'" + objUserInfo.getSlanguagetypecode() + "',"
					+ " qs.jsondata->'sdisplayname'->>'en-US') as screenname, "
					+ " coalesce(c.jsondata->'scontrolids'->>'" + objUserInfo.getSlanguagetypecode() + "',"
					+ " c.jsondata->'scontrolids'->>'en-US') as scontrolids, "
					+ " u.nuserrolefieldcode,us.nuserrolefieldcontrolcode , "
					+ " us.nneedfield,s.nsitefieldcode,us.nneedrights,c.*,u.nuserrolecode,u.nuserrolefieldcode from "
					+ " sitefieldmaster s,userrolefield u,fieldmaster c,qualisforms qs,"
					+ " userrolefieldcontrol us where qs.nformcode=s.nformcode and s.nsitecode="
					+ objUserInfo.getNmastersitecode() + " and s.nformcode=u.nformcode "
					+ " and u.nuserrolefieldcode in(" + suserrolescreencode + ") and " + " s.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qs.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and c.nfieldcode=s.nfieldcode and c.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nformcode=s.nformcode"
					+ " and s.nformcode=u.nformcode  and us.nformcode=u.nformcode and us.nfieldcode=s.nfieldcode"
					+ "  and us.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			final List<FieldMaster> controlmaster = jdbcTemplate.query(query, new FieldMaster());

			objmap.put("ControlRights", controlmaster);
			return new ResponseEntity<>(objmap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * This interface declaration is used to get all the available
	 * InvoicePreferenceSettings with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of InvoicePreferenceSetting
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	private ResponseEntity<Object> getControlMaster(final UserInfo userInfo, final String userRoleScreenCode)
			throws Exception {
		final Map<String, Object> objMap = new HashMap<String, Object>();
		final String query = "select " + " coalesce(qs.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "'," + " qs.jsondata->'sdisplayname'->>'en-US') as screenname, "
				+ " coalesce(c.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " c.jsondata->'scontrolids'->>'en-US') as scontrolids, "
				+ " u.nuserrolefieldcode,us.nuserrolefieldcontrolcode ,s.nsitefieldcode,us.nneedrights,c.*,"
				+ " u.nuserrolecode from "
				+ " sitefieldmaster s,userrolefield u,fieldmaster c,qualisforms qs, userrolefieldcontrol us"
				+ "  where qs.nformcode=s.nformcode and s.nsitecode=" + userInfo.getNmastersitecode()
				+ " and s.nformcode=u.nformcode " + " and u.nuserrolefieldcode in(" + userRoleScreenCode + ") and "
				+ " s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and  c.nfieldcode=s.nfieldcode and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nformcode=s.nformcode"
				+ " and s.nformcode=u.nformcode  and us.nformcode=u.nformcode and us.nfieldcode=s.nfieldcode "
				+ " and us.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<FieldMaster> controlmaster = jdbcTemplate.query(query, new FieldMaster());
		objMap.put("ControlRights", controlmaster);
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	/**
	 * This interface declaration is used to get all the available
	 * InvoicePreferenceSettings with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of InvoicePreferenceSetting
	 *         records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public List<UserRoleScreenControl> getControlRightsActiveID(final UserInfo userInfo,
			final String suserrolescreencode) throws Exception {
		final String query = "select us.* from usersrolescreen u,sitecontrolmaster s,userrolescreencontrol us,controlmaster c"
				+ " where c.ncontrolcode=s.ncontrolcode and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and u.nuserrolecode=us.nuserrolecode and u.nuserrolescreencode in (" + suserrolescreencode + ")"
				+ " and us.nformcode=u.nformcode and us.ncontrolcode=s.ncontrolcode and" + " us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
				+ "  s.nformcode=u.nformcode and s.nsitecode=" + userInfo.getNmastersitecode() + "" + " and s.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		final List<UserRoleScreenControl> lstUserRoleScreenControl = jdbcTemplate.query(query,
				new UserRoleScreenControl());

		return lstUserRoleScreenControl;
	}

	/**
	 * Get all available InvoicePreferenceSettings for a specific site based on user
	 * role code
	 * 
	 * @param inputMap Request body containing user information and user role code
	 * @return ResponseEntity with list of InvoicePreferenceSetting records
	 * @throws Exception Propagates DAO layer exceptions
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	public ResponseEntity<Object> getpreferenceByUserRoleCode(final Integer nuserrolecode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final UsersRoleScreen selectedUsersRoleScreen;
		final String query = "select q.nformcode,ur.suserrolename,sf.nsiteformscode,"
				+ " coalesce(q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " q.jsondata->'sdisplayname'->>'en-US') as sdisplayname, "
				+ " uf.nuserrolefieldcode  from qualisforms q," + " userrolefield uf,userrole ur,sitequalisforms sf"
				+ " where q.nformcode=sf.nformcode and uf.nformcode=sf.nformcode  " + " and sf.nsitecode="
				+ userInfo.getNmastersitecode() + " and sf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ur.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and uf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   and ur.nuserrolecode="
				+ nuserrolecode + "  order by nuserrolefieldcode asc";

		final List<UsersRoleField> usersrolescreenList = jdbcTemplate.query(query, new UsersRoleField());

		final String querys = "select * from userrole where nuserrolecode=" + nuserrolecode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final List<UserRole> userroleList = (List<UserRole>) jdbcTemplate.query(querys, new UserRole());

		final List<UsersRoleField> userrolescreenList = jdbcTemplate.query(query, new UsersRoleField());
		outputMap.put("ScreenRights", usersrolescreenList);
		outputMap.put("SelectedUserRole", userroleList.get(0));

		final int nuserrolefieldcode = usersrolescreenList.get(0).getNuserrolefieldcode();
		if (usersrolescreenList.size() > 0) {
			final String Query = "select urf.*," + " coalesce(q.jsondata->'sdisplayname'->>'"
					+ userInfo.getSlanguagetypecode() + "'," + " q.jsondata->'sdisplayname'->>'en-US') as sdisplayname "
					+ "  from usersrolescreen urs , qualisforms q,userrolefield urf  where"
					+ " q.nformcode=urs.nformcode and q.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  urf.nformcode=q.nformcode "
					+ " and urs.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and urf.nuserrolefieldcode="
					+ usersrolescreenList.get(usersrolescreenList.size() - 1).getNuserrolefieldcode();
			final List<UsersRoleField> selectScreenRights = jdbcTemplate.query(Query, new UsersRoleField());
			outputMap.put("SelectedScreenRights", selectScreenRights);
			outputMap.putAll((Map<String, Object>) getControlRights(userInfo,
					selectScreenRights.get(selectScreenRights.size() - 1).getNuserrolefieldcode()).getBody());
		} else {
			outputMap.put("SelectedScreenRights", usersrolescreenList);
			outputMap.put("ControlRights", usersrolescreenList);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new entry to InvoicePreferenceSetting table.
	 * Need to check that there should be only one default InvoicePreferenceSetting
	 * for a site.
	 * 
	 * @param objInvoicePreferenceSetting [InvoicePreferenceSetting] object holding
	 *                                    details to be added in
	 *                                    InvoicePreferenceSetting table
	 * @param userInfo                    [UserInfo] holding logged in user details
	 *                                    based on which the list is to be fetched
	 * @return saved InvoicePreferenceSetting object with status code 200 if saved
	 *         successfully else if the InvoicePreferenceSetting already exists,
	 *         response will be returned as 'Already Exists' with status code 409
	 * @throws Exception that are thrown from this DAO layer
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public ResponseEntity<Object> createControlRights(final UserInfo userInfo,
			final UserRoleFieldControl userroleController, final List<UsersRoleField> lstusersrolescreen,
			final int nflag, final int nneedrights) throws Exception {
		final List<Object> savedControlRightsList = new ArrayList<>();
		final List<Object> beforeControlRightsList = new ArrayList<>();
		List<UserRoleScreenControl> lstBeforeSave = new ArrayList<>();
		final Map<String, Object> objMap = new HashMap<>();
		final List<String> columnids = new ArrayList<>();

		final int field = lstusersrolescreen.get(0).getNuserrolefieldcode();
		if (nflag == 1) {
			final String querys = " Select * from  UserRoleFieldControl  where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
					+ " nuserrolefieldcontrolcode=" + userroleController.getNuserrolefieldcontrolcode();
			final UserRoleFieldControl lstBeforeSaveUserRoleScreenControl = jdbcTemplate.queryForObject(querys,
					new UserRoleFieldControl());
			if (lstBeforeSaveUserRoleScreenControl != null) {
				final String query = "update userrolefieldcontrol set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', nneedrights=" + nneedrights + " where "
						+ " nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
						+ " nuserrolefieldcontrolcode=" + userroleController.getNuserrolefieldcontrolcode();
				jdbcTemplate.execute(query);
				savedControlRightsList.add(userroleController);
				beforeControlRightsList.add(lstBeforeSaveUserRoleScreenControl);
				final String std = "select * from fieldmaster where nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and " + "nfieldcode="
						+ lstBeforeSaveUserRoleScreenControl.getNfieldcode();
				final FieldMaster lstBeforeSaveUserRoleScreencontrol = jdbcTemplate.queryForObject(std,
						new FieldMaster());

				final String Fieldname = lstBeforeSaveUserRoleScreencontrol.getSfieldname();
				if (nneedrights == 3) {
					columnids.add("IDS_ENABLECONTROL");
				} else {
					columnids.add("IDS_DISABLECONTROL");
				}
				auditUtilityFunction.fnInsertAuditAction(savedControlRightsList, 2, beforeControlRightsList, columnids,
						userInfo);
				final String suserrolescreencode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen,
						"getNuserrolefieldcode");
				objMap.putAll((Map<String, Object>) getControlMaster(userInfo, suserrolescreencode).getBody());

				return new ResponseEntity<>(objMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			final int needright;
			final String suserrolescreencode;
			suserrolescreencode = stringUtilityFunction.fnDynamicListToString(lstusersrolescreen,
					"getNuserrolefieldcode");
			lstBeforeSave = getControlRightsActiveID(userInfo, suserrolescreencode);

			if (lstBeforeSave.size() > 0) {
				if (nneedrights == 3) {
					columnids.add("IDS_ENABLEALLCONTROL");
					needright = 4;
				} else {
					columnids.add("IDS_DISABLEALLCONTROL");
					needright = 3;
				}

				final String query = " update  userrolefieldcontrol set dmodifieddate='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',nneedrights=" + nneedrights + " where "
						+ " nuserrolefieldcontrolcode in (select us.nuserrolecontrolcode from userrolefield u,sitefieldmaster s,"
						+ " userrolefieldcontrol us where us.nneedrights=" + needright + " and"
						+ " u.nuserrolecode=us.nuserrolecode and u.nuserrolesfieldcode in (" + suserrolescreencode
						+ ") and " + " us.nformcode=u.nformcode  and " + " us.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
						+ " and  s.nformcode=u.nformcode and s.nsitecode=" + userInfo.getNmastersitecode() + ""
						+ " and s.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
						+ " u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")"
						+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				jdbcTemplate.execute(query);

				final List<UserRoleFieldControl> lstUserRoleScreenControl = (List<UserRoleFieldControl>) jdbcTemplate
						.query("select us.* from userrolefield u," + "sitefieldmaster s,userrolefieldcontrol us where"

								+ " and u.nuserrolecode=us.nuserrolecode and u.nuserrolesfieldcode in ("
								+ suserrolescreencode + ") " + " and us.nformcode=u.nformcode  " + " and us.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  "
								+ " s.nformcode=u.nformcode and s.nsitecode=" + userInfo.getNmastersitecode() + " and  "
								+ " s.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
								+ " u.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
								new UserRoleFieldControl());
				savedControlRightsList.add(lstUserRoleScreenControl);
				beforeControlRightsList.add(lstBeforeSave);

				auditUtilityFunction.fnInsertListAuditAction(savedControlRightsList, 2, beforeControlRightsList,
						columnids, userInfo);
				objMap.putAll((Map<String, Object>) getControlMaster(userInfo, suserrolescreencode).getBody());
				return new ResponseEntity<>(objMap, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}

	}

	/**
	 * Get all available InvoicePreferenceSettings for a specific site
	 * 
	 * @param inputMap Request body containing user information and user role field
	 *                 code
	 * @return ResponseEntity with list of InvoicePreferenceSetting records
	 * @throws Exception Propagates DAO layer exceptions
	 */
	@Override
	public ResponseEntity<Object> getSearchScreenRights(final String nuserrolefieldcode, final UserInfo objUserInfo)
			throws Exception {
		// TODO Auto-generated method stub
		final Map<String, Object> objmap = new LinkedHashMap<String, Object>();

		final String query = "select " + " coalesce(qs.jsondata->'sdisplayname'->>'"
				+ objUserInfo.getSlanguagetypecode() + "'," + " qs.jsondata->'sdisplayname'->>'en-US') as screenname, "
				+ " coalesce(c.jsondata->'scontrolids'->>'" + objUserInfo.getSlanguagetypecode() + "',"
				+ " c.jsondata->'scontrolids'->>'en-US') as scontrolids, "
				+ "u.nuserrolefieldcode,us.nuserrolefieldcontrolcode , "
				+ " us.nneedfield,s.nsitefieldcode,us.nneedrights,c.*,u.nuserrolecode from "
				+ " sitefieldmaster s,userrolefield u,fieldmaster c,qualisforms qs,"
				+ " userrolefieldcontrol us where qs.nformcode=s.nformcode and s.nsitecode="
				+ objUserInfo.getNmastersitecode() + " and s.nformcode=u.nformcode " + " and u.nuserrolefieldcode in("
				+ nuserrolefieldcode + ") and " + " s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and qs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and c.nfieldcode=s.nfieldcode and c.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nformcode=s.nformcode"
				+ " and s.nformcode=u.nformcode  and us.nformcode=u.nformcode and us.nfieldcode=s.nfieldcode"
				+ "  and u.nuserrolecode=us.nuserrolecode and us.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		final List<FieldMaster> controlmaster = jdbcTemplate.query(query, new FieldMaster());
		objmap.put("ControlRights", controlmaster);
		return new ResponseEntity<>(objmap, HttpStatus.OK);
	}

}
