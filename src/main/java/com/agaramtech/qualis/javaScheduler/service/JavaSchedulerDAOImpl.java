package com.agaramtech.qualis.javaScheduler.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PGobject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.agaramtech.qualis.basemaster.model.Unit;
import com.agaramtech.qualis.biobank.service.bgsiexternalapi.BGSIExternalApiDAO;
import com.agaramtech.qualis.exception.service.ExceptionLogDAO;
import com.agaramtech.qualis.externalorder.service.ExternalOrderDAO;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.release.service.ReleaseService;
import com.agaramtech.qualis.scheduler.service.SchedularGenerateReportDAO;
import  com.agaramtech.qualis.scheduler.service.SpringSchedularDAO;
import com.agaramtech.qualis.synchronisation.service.SynchronizationDAO;
import com.agaramtech.qualis.wqmis.model.JjmVillageList;
import com.agaramtech.qualis.wqmis.model.WQMISApi;
import com.agaramtech.qualis.wqmis.service.WQMISDAO;
import com.agaramtech.qualis.wqmis.service.WQMISService;
import com.agaramtech.qualis.wqmisrequestapi.service.WqmisRequestApiService;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class JavaSchedulerDAOImpl implements JavaSchedulerDAO{

	private static final Log LOGGER = LogFactory.getLog(JavaSchedulerDAOImpl.class);
	@Autowired
	private TaskScheduler taskScheduler;
	@Autowired
	private WqmisRequestApiService wqmisrequestapiservice;
	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final SynchronizationDAO synchronizationDAO;
	private final ExternalOrderDAO externalOrderDAO;
	private final SchedularGenerateReportDAO schedularGenerateReportDAO;
	private final SpringSchedularDAO springSchedularDAO;
	private final ExceptionLogDAO exceptionLogDAO;
	private final EmailDAOSupport emailDAOSupport;
	private final ReleaseService releaseService;	
	private final WQMISDAO wqmisdao;
	private final WQMISService wqmisService;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final BGSIExternalApiDAO bgsiExternalApiDAO;

	// public JavaSchedulerDAOImpl(SynchronizationDAO synchronizationDAO,
	// ExternalOrderDAO externalOrderDAO,
	// com.agaramtech.qualis.javaScheduler.service.schedularGenerateReportDAO
	// schedularGenerateReportDAO,
	// SpringSchedularDAO springSchedularDAO, ExceptionHandleDAO exceptionHandleDAO)
	// {
	//		super();
	//		this.synchronizationDAO = synchronizationDAO;
	//		this.externalOrderDAO = externalOrderDAO;
	//		this.schedularGenerateReportDAO = schedularGenerateReportDAO;
	//		this.springSchedularDAO = springSchedularDAO;
	//		this.exceptionHandleDAO = exceptionHandleDAO;
	//	}

	/*
	 * @Scheduled(cron = "${cron.expression}") //Fusion Sync public void
	 * exceuteFusionSyncProcess() {
	 *
	 * LOGGER.info("Fusion Sync Process:"); try { UserInfo userInfo=new UserInfo();
	 *
	 * userInfo.setSdatetimeformat("dd/MM/yyyy HH:mm:ss");
	 * userInfo.setSlanguagefilename("Msg_en_US"); userInfo.setNusercode(-1);
	 * userInfo.setNuserrole(-1); userInfo.setSusername("QuaLIS Admin");
	 * userInfo.setSuserrolename("QuaLIS Admin");
	 * userInfo.setSlanguagename("English"); userInfo.setSlanguagetypecode("en-US");
	 * userInfo.setNtranssitecode((short)-1);
	 *
	 * userInfo=fusionSiteDao.getUserInfo(userInfo,"Fusion Site");
	 * fusionSiteDao.syncFusionSite(userInfo);
	 *
	 * userInfo=fusionSiteDao.getUserInfo(userInfo,"Fusion Plant");
	 * fusionPlantDAO.syncFusionPlant(userInfo);
	 *
	 * userInfo=fusionSiteDao.getUserInfo(userInfo,"Fusion Users");
	 * fusionUsersDAO.syncFusionUsers(userInfo);
	 *
	 * userInfo=fusionSiteDao.getUserInfo(userInfo,"Fusion Plant User");
	 * fusionPlantUserDAO.syncFusionPlantUser(userInfo);
	 *
	 * } catch(Exception e) { LOGGER.error("error:"+ e.getMessage()); } }
	 */

	//Material Inventory Transaction

	private final Map<String, Long> pendingJobs = new ConcurrentHashMap<>();

	@Override
	@Scheduled(cron = "${cron1.expression}")
	public void materialinventoryvalidation() {

		LOGGER.info("material validity validation");
		jdbcTemplate.execute("call sp_material_inventory_expire_check()");
		jdbcTemplate.execute("call sp_material_inventory_nextvalidation()");
	}

	//Site to Site Auto Sync
	@Override
	@Scheduled(cron = "${cron2.expression}")
	public void exceuteSyncProcess() {

		LOGGER.info("Distributed Sync Process:");
		try {
			UserInfo userinfo=new UserInfo();
			final int nsyncType = 44;//Auto
			synchronizationDAO.syncRecords(userinfo, nsyncType);
		} catch (Exception e) {
			LOGGER.error("error:"+ e.getMessage());
		}

	}

	//Site to Site delete Sync
	@Override
	@Scheduled(cron = "${cron3.expression}")
	public void deleteSync() {
		LOGGER.info("Deletion of Sync:");
		try {
			LOGGER.info("Deletion of Records for every 3Months");
			synchronizationDAO.deleteSync();
		} catch (Exception e) {
			LOGGER.error("error:"+ e.getMessage());
		}

	}

	//Report Generation
	@Override
	@Scheduled(cron = "${cron4.expression}")
	public void schedularGenerateReport()  {
		LOGGER.info("Report Generation");
		try {
			schedularGenerateReportDAO.schedularGenerateReport();
		} catch (Exception e) {
			LOGGER.info("cause==>", e.getCause());
			LOGGER.info(e.getMessage());
		}

	}

	//Auto Expire- Method Validity
	@Override
	@Scheduled(cron = "${cron5.expression}")
	public void methodvalidityautoexpire() {

		LOGGER.info("method validity auto expiry");
		jdbcTemplate.execute("select * from  fn_methodvalidityautoexpire()");
	}
	/*
	 * //Email
	 * 
	 * @Override
	 * 
	 * @Scheduled(cron = "${cron6.expression}") public void schedularSendEmailTask()
	 * { LOGGER.info("mail"); try { emailDAOSupport.sendMailCommonFunction(); }
	 * catch (Exception e) { LOGGER.info("cause==>", e.getCause());
	 * LOGGER.info(e.getMessage());
	 * 
	 * } }
	 */

	//ALPDJ21-55--Added by Vignesh(16-08-2025)--Sent the report by the mail
	@Override
	@Scheduled(cron = "${cron6.expression}")
	public void schedularSendEmailTask() {
		LOGGER.info("mail");
		try {
			releaseService.sendMailCommonFunction();
		} catch (Exception e) {
			LOGGER.info("cause==>", e.getCause());
			LOGGER.info(e.getMessage());

		}
	}

	//Send to Portal Report
	@Override
	@Scheduled(cron = "${cron7.expression}")
	public void schedulerSendToPortalReport () {
		LOGGER.info("Send to Portal Report");
		UserInfo userinfo=new UserInfo();
		try {
			externalOrderDAO.SendToPortalReport(userinfo);
		} catch (Exception e) {
			LOGGER.info("cause==>", e.getCause());
			LOGGER.info(e.getMessage());
		}
	}

	//Sent Result to Portal
	@Override
	@Scheduled(cron = "${cron8.expression}")
	public void schedulerSentResultToPortal() {
		LOGGER.info("Sent Result to Portal");
		try {
			springSchedularDAO.sentResultToPortal();
		} catch (Exception e) {
			LOGGER.info("cause==>", e.getCause());
			LOGGER.info(e.getMessage());
		}
	}

	//sent External Order Status
	@Override
	@Scheduled(cron = "${cron9.expression}")
	public void schedulerSentExternalOrderStatus() {
		LOGGER.info("Send External Order Status");
		try {
			springSchedularDAO.sentExternalOrderStatus();
		} catch (Exception e) {
			LOGGER.info("cause==>", e.getCause());
			LOGGER.info(e.getMessage());
		}
	}

	@Override
	@Scheduled(cron = "${cron10.expression}")
	public void deleteExceptionLogs() {
		LOGGER.info("Exception logs:");
		try {
			LOGGER.info("Deletion of Records for every 3Months");
			exceptionLogDAO.deleteExceptionLogs();
		} catch (Exception e) {
			LOGGER.error("error:"+ e.getMessage());
		}

	}

	//Execute sync received data
	@Override
	@Scheduled(cron = "${cron11.expression}")
	public void executeSyncReceivedData() {
		LOGGER.info("Sync received data execution:");
		try {
			jdbcTemplate.execute("call sp_sync_received_data_execution()");
		} catch (Exception e) {
			LOGGER.error("error:"+ e.getMessage());
		}

	}

	//Added by sonia on 10th Feb 2025 for jira id:ALPD-5332
	@Override
	@Scheduled(cron = "${cron_scheduler.expression}")
	public void scheduler() {
		LOGGER.info("scheduler:");
		try {
			LOGGER.info("scheduler");
			UserInfo userinfo=new UserInfo();
			String currentDate = dateUtilityFunction.getCurrentDateTime(userinfo).toString().replace("T", " ")
					.replace("Z", "");
			jdbcTemplate.execute("call sp_scheduler('"+currentDate+"')");
		} catch (Exception e) {
			LOGGER.error("error:"+ e.getMessage());
		}

	}

	//Added by sonia on 11th Feb 2025 for jira id:ALPD-5317
	@Override
	@Scheduled(cron = "${cron_envirnomentalScheduler.expression}")
	public void envirnomentalScheduler() {
		LOGGER.info("environmental scheduler:");
		try {
			LOGGER.info("environmental scheduler:");
			UserInfo userinfo=new UserInfo();
			String currentDate = dateUtilityFunction.getCurrentDateTime(userinfo).toString().replace("T", " ")
					.replace("Z", "");
			jdbcTemplate.execute("call sp_environmentalscheduler('"+currentDate+"')");
		} catch (Exception e) {
			LOGGER.error("error:"+ e.getMessage());
		}

	}

	//Added by sonia on 11th Feb 2025 for jira id:ALPD-5350
	@Override
	@Scheduled(cron = "${cron_stabilityScheduler.expression}")
	public void stabilityScheduler() {
		LOGGER.info("stability scheduler:");
		try {
			LOGGER.info("stability scheduler:");
			UserInfo userinfo=new UserInfo();
			String currentDate = dateUtilityFunction.getCurrentDateTime(userinfo).toString().replace("T", " ")
					.replace("Z", "");
			jdbcTemplate.execute("call sp_stabilityscheduler('"+currentDate+"')");
		} catch (Exception e) {
			LOGGER.error("error:"+ e.getMessage());
		}

	}

	// below runs method [executeReleaseCOASync] for every 5 minutes
	@Override
	@Scheduled(cron = "${cron_12.expression}")
	@Transactional
	public void executeReleaseCOASync() {

		final String sLock = " lock table locksyncprocess " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
		jdbcTemplate.execute(sLock);

		LOGGER.info("executeReleaseCOASync start:COA Sync Process");
		try {
			LOGGER.info("executeReleaseCOASync try: COA Sync Process");
			jdbcTemplate.execute("call sp_sync_send_record()");
		} catch (Exception e) {
			LOGGER.info("executeReleaseCOASync Error: COA Sync Process");
			LOGGER.error("error:"+ e.getMessage());
		}

	}	

	//Added by Mullai Balaji on 11th Oct 2025 for jira id:SWSM-85
	@Override
	@Scheduled(cron = "${cron_sync_wqmis_jjm_MasterData.expression}")
	@Transactional
	public void wqmisMasterDataSync() {
		try {
			LOGGER.info("Master Data scheduler started:");
			UserInfo userInfo = new UserInfo();

			userInfo.setSdatetimeformat("dd/MM/yyyy HH:mm:ss");
			userInfo.setSlanguagefilename("Msg_en_US");
			userInfo.setNusercode(-1);
			userInfo.setNuserrole(-1);
			userInfo.setSusername("QuaLIS Admin");
			userInfo.setSuserrolename("QuaLIS Admin");
			userInfo.setSlanguagename("English");
			userInfo.setSlanguagetypecode("en-US");
			userInfo.setNtranssitecode((short)-1);

			// Get API list from DB
			String query = "SELECT nwqmisapicode,nsorter, surl, stokenid FROM wqmisapi WHERE nismastertable="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " AND nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ORDER BY nsorter";
			List<Map<String, Object>> apiList = jdbcTemplate.queryForList(query);
			ResponseEntity<Object> response = null;
			// Call each API sequentially
			for (Map<String, Object> apiRow : apiList) {
				Map<String, Object> inputMap = new HashMap<>();
				inputMap.put("surl", apiRow.get("surl"));
				inputMap.put("stokenid", apiRow.get("stokenid"));
				inputMap.put("sprimarykey", apiRow.get("nwqmisapicode"));
				int nsorter = (int) apiRow.get("nsorter"); 
				switch (nsorter) {
				case 1:
					response = wqmisdao.getDistrictList(inputMap, userInfo);
					break;
				case 2:
					response = wqmisdao.getBlocksList(inputMap, userInfo);
					break;
				case 3:
					response = wqmisdao.getGpList(inputMap, userInfo);
					break;
				case 4:
					response = wqmisdao.getVillageList(inputMap, userInfo);
					break;
//	                case 5:
//	                    response = wqmisdao.getLabDataList(inputMap, userInfo);
//	                    break;
				case 6:
					response = wqmisdao.getParameterDataList(inputMap, userInfo);
					break;
				case 7:
					response = wqmisdao.getParametersDataFTKUserList(inputMap, userInfo);
					break;

				case 12:
					response = wqmisdao.getLabData(inputMap, userInfo);
					break;
				case 13:
					response = wqmisdao.getLabOfficial(inputMap, userInfo);
					break;

				case 14:
					response = wqmisdao.getEquipments_data(inputMap, userInfo);
					break;

				case 15:
					response = wqmisdao.GetReagents_data(inputMap, userInfo);
					break;

				case 16:
					response = wqmisdao.GetMeasurement_methods_data(inputMap, userInfo);
					break;
				case 17:
					response = wqmisdao.GetSample_submitter(inputMap, userInfo);
					break;
				case 18:
					response = wqmisdao.GetSample_Location(inputMap, userInfo);
					break;
				case 19:
					response = wqmisdao.GetWaterSource(inputMap, userInfo);
					break;
				case 20:
					response = wqmisdao.getHblist(inputMap, userInfo);
					break;
				case 21:
					response = wqmisdao.Getdwsm_state_member_secretary(inputMap, userInfo);
					break;
				case 22:
					response = wqmisdao.Getstate_level_mapping(inputMap, userInfo);
					break;

				default:
					break;
				}
			}
			LOGGER.info(response.getStatusCode());
		} catch (Exception e) {
			LOGGER.info("Error in Master data scheduler");
		}
	}

	//Added by sonia on 11th Oct 2025 for jira id:SWSM-85
	@Override
	@Scheduled(cron = "${cron_sync_wqmisLabSamples.expression}")
	public void scheduleLabSamples() {
		try{
			LOGGER.info("Scheduled Lab Samples:Start");
			Map<String,Object> inputMap =new HashMap<>();
			UserInfo userinfo=new UserInfo();	

			userinfo.setSdatetimeformat("dd/MM/yyyy HH:mm:ss");
			userinfo.setSlanguagefilename("Msg_en_US"); 
			userinfo.setNusercode(-1);
			userinfo.setNuserrole(-1); 
			userinfo.setSusername("QuaLIS Admin");
			userinfo.setSuserrolename("QuaLIS Admin");
			userinfo.setSlanguagename("English"); 
			userinfo.setSlanguagetypecode("en-US");
			userinfo.setNtranssitecode((short)-1);

			String strQuery = "select * from wqmisapi where smethodurl ='getLabSamples' and nismastertable="
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			WQMISApi objWQMISApi = (WQMISApi) jdbcUtilityFunction.queryForObject(strQuery, WQMISApi.class,
					jdbcTemplate);
			inputMap.put("surl", objWQMISApi.getSurl());
			inputMap.put("stokenid", objWQMISApi.getStokenid());
			inputMap.put("nwqmisapicode", objWQMISApi.getNwqmisapicode());

			String strVillageQuery = "select * from jjmvillagelist where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			List<JjmVillageList> villageList = jdbcTemplate.query(strVillageQuery, new JjmVillageList());
			for (JjmVillageList village : villageList) {
				inputMap.put("nvillageid", village.getNvillageid());
				wqmisService.getLabSamples(inputMap, userinfo);
				LOGGER.info("Scheduled Lab Samples:Inserted:"+village.getNvillageid()); 
			}
			LOGGER.info("Scheduled Lab Samples:End");
		}catch(Exception e){
			LOGGER.error("error:"+ e.getMessage());				
		}
	}

	//Added by sonia on 11th Oct 2025 for jira id:SWSM-85
	@Override
	@Scheduled(cron = "${cron_sync_wqmisFTKSamples.expression}")
	public void scheduleFTKSamples() {
		try{
			LOGGER.info("Scheduled FTK Samples:Start");
			Map<String,Object> inputMap =new HashMap<>();
			UserInfo userinfo=new UserInfo();	
			userinfo.setSdatetimeformat("dd/MM/yyyy HH:mm:ss");
			userinfo.setSlanguagefilename("Msg_en_US"); 
			userinfo.setNusercode(-1);
			userinfo.setNuserrole(-1); 
			userinfo.setSusername("QuaLIS Admin");
			userinfo.setSuserrolename("QuaLIS Admin");
			userinfo.setSlanguagename("English"); 
			userinfo.setSlanguagetypecode("en-US");
			userinfo.setNtranssitecode((short)-1);

			String strQuery = "select * from wqmisapi where smethodurl ='getFTKSamples' and nismastertable="
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			WQMISApi objWQMISApi = (WQMISApi) jdbcUtilityFunction.queryForObject(strQuery, WQMISApi.class,
					jdbcTemplate);
			inputMap.put("surl", objWQMISApi.getSurl());
			inputMap.put("stokenid", objWQMISApi.getStokenid());
			inputMap.put("nwqmisapicode", objWQMISApi.getNwqmisapicode());

			String strVillageQuery = "select * from jjmvillagelist where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			List<JjmVillageList> villageList = jdbcTemplate.query(strVillageQuery, new JjmVillageList());			
			for (JjmVillageList village : villageList) {
				inputMap.put("nvillageid", village.getNvillageid());
				wqmisService.getFTKSamples(inputMap, userinfo);
				LOGGER.info("Scheduled FTK Samples:Inserted:"+village.getNvillageid());    
			}	
			LOGGER.info("Scheduled FTK Samples:End");
		}catch(Exception e){
			LOGGER.error("error:"+ e.getMessage());
		}
	}

	// Added by Mohamed Ashik on 14th Oct 2025 for jira id:SWSM-85
	@Override
	@Scheduled(cron = "${cron_sync_wqmisContaminatedLabSamples.expression}")
	public void wqmisContaminatedLabSamples() {
		try {
			LOGGER.info("Scheduled Contaminated Lab Samples:Start");
			Map<String, Object> inputMap = new HashMap<>();
			UserInfo userinfo = new UserInfo();
			userinfo.setSdatetimeformat("dd/MM/yyyy HH:mm:ss");
			userinfo.setSlanguagefilename("Msg_en_US");
			userinfo.setNusercode(-1);
			userinfo.setNuserrole(-1);
			userinfo.setSusername("QuaLIS Admin");
			userinfo.setSuserrolename("QuaLIS Admin");
			userinfo.setSlanguagename("English");
			userinfo.setSlanguagetypecode("en-US");
			userinfo.setNtranssitecode((short) -1);

			String strQuery = "select * from wqmisapi where smethodurl ='syncContaminatedLabSamples' and nismastertable="
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			WQMISApi objWQMISApi = (WQMISApi) jdbcUtilityFunction.queryForObject(strQuery, WQMISApi.class,
					jdbcTemplate);
			inputMap.put("surl", objWQMISApi.getSurl());
			inputMap.put("stokenid", objWQMISApi.getStokenid());
			inputMap.put("nwqmisapicode", objWQMISApi.getNwqmisapicode());

			String strVillageQuery = "select * from jjmvillagelist where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			List<JjmVillageList> villageList = jdbcTemplate.query(strVillageQuery, new JjmVillageList());
			for (JjmVillageList village : villageList) {
				inputMap.put("nvillageid", village.getNvillageid());
				wqmisService.syncContaminatedLabSamples(inputMap, userinfo);
				LOGGER.info("Scheduled Contaminated Lab Samples:Inserted:" + village.getNvillageid());
			}
			LOGGER.info("Scheduled Contaminated Lab Samples:End");
		} catch (Exception e) {
			LOGGER.error("error:" + e.getMessage());
		}
	}

	// Added by Mohamed Ashik on 14th Oct 2025 for jira id:SWSM-85
	@Override
	@Scheduled(cron = "${cron_sync_wqmisContaminatedFTKSamples.expression}")
	public void wqmisContaminatedFTKSamples() {
		try {
			LOGGER.info("Scheduled Contaminated FTK Samples:Start");
			Map<String, Object> inputMap = new HashMap<>();
			UserInfo userinfo = new UserInfo();
			userinfo.setSdatetimeformat("dd/MM/yyyy HH:mm:ss");
			userinfo.setSlanguagefilename("Msg_en_US");
			userinfo.setNusercode(-1);
			userinfo.setNuserrole(-1);
			userinfo.setSusername("QuaLIS Admin");
			userinfo.setSuserrolename("QuaLIS Admin");
			userinfo.setSlanguagename("English");
			userinfo.setSlanguagetypecode("en-US");
			userinfo.setNtranssitecode((short) -1);

			String strQuery = "select * from wqmisapi where smethodurl ='syncContaminatedFTKSamples' and nismastertable="
					+ Enumeration.TransactionStatus.NO.gettransactionstatus() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			WQMISApi objWQMISApi = (WQMISApi) jdbcUtilityFunction.queryForObject(strQuery, WQMISApi.class,
					jdbcTemplate);
			inputMap.put("surl", objWQMISApi.getSurl());
			inputMap.put("stokenid", objWQMISApi.getStokenid());
			inputMap.put("nwqmisapicode", objWQMISApi.getNwqmisapicode());

			String strVillageQuery = "select * from jjmvillagelist where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			List<JjmVillageList> villageList = jdbcTemplate.query(strVillageQuery, new JjmVillageList());
			for (JjmVillageList village : villageList) {
				inputMap.put("nvillageid", village.getNvillageid());
				wqmisService.syncContaminatedFTKSamples(inputMap, userinfo);
				LOGGER.info("Scheduled Contaminated FTK Samples:Inserted:" + village.getNvillageid());
			}
			LOGGER.info("Scheduled Contaminated FTK Samples:End");
		} catch (Exception e) {
			LOGGER.error("error:" + e.getMessage());
		}
	}

	//Added by Vishakh on 11th Oct 2025 for jira id:BGSI-140
	//		@Scheduled(cron = "${cron_bioExternalTransferData.expression}")
	public void bioExternalTransferDataScheduler() {
		LOGGER.info("stability scheduler:");
		try {
			LOGGER.info("bio external transfer data scheduler:");
			UserInfo userinfo=new UserInfo();
			bgsiExternalApiDAO.bioExternalTransferData(userinfo);
		} catch (Exception e) {
			LOGGER.error("error:"+ e.getMessage());
		}

	}

	//ALPDJ21-93--Added by Vignesh(30-10-2025)-->Release and report screen -> HL7 Format Conversion
	@Scheduled(cron = "${cron_hl7formatconversion.expression}")
	public void hL7Conversion() {
		LOGGER.info("hl7conversion");

		try {
			final String sQuery="select nsitecode from site s,settings ss where s.ssitecode=ss.ssettingvalue"
					+ " and s.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+ " and ss.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+""
					+" and ss.nsettingcode=30";

			final short nsitecode = jdbcTemplate.queryForObject(sQuery, Short.class);
			UserInfo userInfo=new UserInfo();
			userInfo.setNtranssitecode(nsitecode);
			springSchedularDAO.hL7ConversionScheduler(userInfo);
		} catch (Exception e) {
			LOGGER.error("error:"+ e.getMessage());
			///LOGGER.info(e.getMessage());

		}
	}
	
	//Added by sonia on 18th Nov 2025 for jira id:BGSI-234	
	@Transactional
	@Scheduled(cron = "${cron_emailScheduler.expression}")	
	public void emailScheduler() {
		LOGGER.info("emailScheduler");				
		try {
			UserInfo userInfo=new UserInfo();
			userInfo.setNmastersitecode((short)-1);
			userInfo.setSlanguagefilename("Msg_en_US");
			userInfo.setStimezoneid("Asia/Kolkata");  				    	
			userInfo.setSdatetimeformat("dd/MM/yyyy HH:mm:ss");
			userInfo.setSlanguagefilename("Msg_en_US");
			userInfo.setNusercode(-1);
			userInfo.setNuserrole(-1);
			userInfo.setSusername("QuaLIS Admin");
			userInfo.setSuserrolename("QuaLIS Admin");
			userInfo.setSlanguagename("English");
			userInfo.setSlanguagetypecode("en-US");
			userInfo.setNtranssitecode((short) -1);
			userInfo.setNtimezonecode(-1);

			final String currentDate = dateUtilityFunction.getCurrentDateTime(userInfo).toString().replace("T", " ").replace("Z", "");					
			final String getTestQuery = "SELECT * from fn_emailscheduler('"+currentDate+"')";
			final List<Map<String, Object>> results = jdbcTemplate.queryForList(getTestQuery);		   
			for (Map<String, Object> row : results) {
				final String sscheduleTypeName = (String) row.get("scheduletypename"); 
				final PGobject jsonObject = (PGobject) row.get("result_json");
				final String json = jsonObject.getValue();  
				final JSONObject obj = new JSONObject(json);
				final JSONArray remainderArray = obj.getJSONArray(sscheduleTypeName);
				for (int i = 0; i < remainderArray.length(); i++) {
					Map<String,Object> inputMap =new HashMap<String,Object>();				    	
					JSONObject item = remainderArray.getJSONObject(i);				        
					inputMap.put("nemailscreenschedulercode", (Integer) row.get("emailscreenschedulercode"));
					inputMap.put("nformcode", (Integer) row.get("formcode")); 
					if(item.has("sremainderdatetime")){
						inputMap.put("remainderDate", item.getString("sremainderdatetime"));
					}
					inputMap.put((String)row.get("columnname"),item.getInt("primarykey"));				        
					if(item.has("ssystemid")){
						inputMap.put("ssystemid",item.getString("ssystemid"));
					}				        
					emailDAOSupport.createScheduleEmailAlertTransaction(inputMap,userInfo);						        
				}
			}			   	
		} catch (Exception e) {
			LOGGER.info("cause==>", e.getCause());
			LOGGER.info(e.getMessage());

		}
	}


	//#SECURITY-VULNERABILITY-MERGING-START
	// Added by Dhivya Bharathi on 26th Nov 2025 for jira id:swsm-122
	@Scheduled(cron = "${cron_postRelease.expression}")
	public void runDelayedPostRelease() throws Exception {

		LOGGER.info("Scheduler START");

		UserInfo userinfo = new UserInfo();
		userinfo.setSdatetimeformat("dd/MM/yyyy HH:mm:ss");
		userinfo.setSlanguagefilename("Msg_en_US");
		userinfo.setNusercode(-1);
		userinfo.setNuserrole(-1);
		userinfo.setSusername("QuaLIS Admin");
		userinfo.setSuserrolename("QuaLIS Admin");
		userinfo.setSlanguagename("English");
		userinfo.setSlanguagetypecode("en-US");
		userinfo.setNtranssitecode((short) -1);

		wqmisrequestapiservice.getIntegrationApiDetails(userinfo);

		LOGGER.info("Scheduler END");
		}
	
	// Added by Dhivya Bharathi on 28th Nov 2025 for jira id:swsm-122
//	@Scheduled(cron = "${cron_transactionSync.expression}")
//	public void processFTKSamples() throws Exception {
//		UserInfo userInfo = new UserInfo();
//		userInfo.setSdatetimeformat("dd/MM/yyyy HH:mm:ss");
//		userInfo.setSlanguagefilename("Msg_en_US");
//		userInfo.setNusercode(-1);
//		userInfo.setNuserrole(-1);
//		userInfo.setSusername("QuaLIS Admin");
//		userInfo.setSuserrolename("QuaLIS Admin");
//		userInfo.setSlanguagename("English");
//		userInfo.setSlanguagetypecode("en-US");
//		userInfo.setNtranssitecode((short) -1);
//		userInfo.setNtimezonecode(330);       
//		userInfo.setStimezoneid("Asia/Kolkata"); 
//	    List<Map<String, Object>> records = wqmisrequestapiservice.getPendingFTKSamples();
//
//	    for (Map<String, Object> row : records) {
//
//	        Map<String, Object> input = new HashMap<>();
//
//	        input.put("npreregno", row.get("npreregno"));
//	        input.put("ncoaparentcode", row.get("ncoaparentcode"));
//	        input.put("ncoareporthistorycode", row.get("ncoareporthistorycode"));
//	        input.put("nversionno", row.get("nversionno"));
//	        input.put("sreportno", row.get("sreportno"));
//	        input.put("ntransactionstatus", row.get("ntransactionstatus"));
//	        input.put("stransactionsamplecode", row.get("stransactionsamplecode"));
//	        input.put("stransactiontestcode", row.get("stransactiontestcode"));
//	        input.put("nregsubtypecode", row.get("nregsubtypecode"));
//	        input.put("nregtypecode", row.get("nregtypecode"));
//	        
//	        try {
//	        	wqmisrequestapiservice.createSourceSampleInfoFTKUserDemo(input, userInfo);
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	           
//	        }
//	    }
//	}
		
		// Added by Mohamed Ashik for combined Api on 29th Nov 2025 for jira id:swsm-122
		@Scheduled(cron = "${cron_transactionAPISync.expression}")
		public void processLabAndFTKSample() throws Exception {
			UserInfo userInfo = new UserInfo();
			userInfo.setSdatetimeformat("dd/MM/yyyy HH:mm:ss");
			userInfo.setSlanguagefilename("Msg_en_US");
			userInfo.setNusercode(-1);
			userInfo.setNuserrole(-1);
			userInfo.setSusername("QuaLIS Admin");
			userInfo.setSuserrolename("QuaLIS Admin");
			userInfo.setSlanguagename("English");
			userInfo.setSlanguagetypecode("en-US");
			userInfo.setNtranssitecode((short) -1);
			userInfo.setNtimezonecode(330);
			userInfo.setStimezoneid("Asia/Kolkata");
			List<Map<String, Object>> records = wqmisrequestapiservice.getPendingSamples();

			for (Map<String, Object> row : records) {

				Map<String, Object> input = new HashMap<>();

				input.put("npreregno", row.get("npreregno"));
				input.put("ncoaparentcode", row.get("ncoaparentcode"));
				input.put("ncoareporthistorycode", row.get("ncoareporthistorycode"));
				input.put("nversionno", row.get("nversionno"));
				input.put("sreportno", row.get("sreportno"));
				input.put("ntransactionstatus", row.get("ntransactionstatus"));
				input.put("stransactionsamplecode", row.get("stransactionsamplecode").toString());
				input.put("stransactiontestcode", row.get("stransactiontestcode").toString());
				input.put("nregsubtypecode", row.get("nregsubtypecode"));
				input.put("nregtypecode", row.get("nregtypecode"));

				try {
					wqmisrequestapiservice.createAPIIntegrationDetails(input, userInfo);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	}
			
	//Added by sonia on 22nd Nov 2025 for jira id:SWSM-125
	@Scheduled(cron = "${cron_runFunctionMigration.expression}")
	@Transactional
	public void runFunctionMigration() {
		try {
			LOGGER.info("Starting function migration...");
			String query = "SELECT nmigrationcode,nsorter, smigrationname FROM jjmmigration WHERE  nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ORDER BY nsorter";
			List<Map<String, Object>> apiList = jdbcTemplate.queryForList(query);
			for (Map<String, Object> apiRow : apiList) {
				int nsorter = (int) apiRow.get("nsorter");
				switch (nsorter) {
				case 1:
					jdbcTemplate.execute("select * from  fn_jjmsitedata()");
					LOGGER.info("Executed: fn_jjmsitedata()");
					break;
				case 2:
					jdbcTemplate.execute("select * from  fn_jjmmuserdata()");
					LOGGER.info("Executed: fn_jjmmuserdata()");
					break;
				case 3:
					jdbcTemplate.execute("select * from  fn_jjminstrumentdata()");
					LOGGER.info("Executed: fn_jjminstrumentdata()");
					break;
				case 4:
					jdbcTemplate.execute("select * from  fn_jjmmethoddata()");
					LOGGER.info("Executed: fn_jjmmethoddata()");
					break;
				case 5:
					jdbcTemplate.execute("select * from   fn_jjmtestmasterdata()");
					LOGGER.info("Executed:  fn_jjmtestmasterdata()");
					break;
				case 6:
					jdbcTemplate.execute("select * from  fn_jjmmaterialdata()");
					LOGGER.info("Executed: fn_jjmmaterialdata()");
					break;
				default:
					break;
				}
				LOGGER.info("Function migration completed successfully.");
			}
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
			}
	}
	//#SECURITY-VULNERABILITY-MERGING-END



}
