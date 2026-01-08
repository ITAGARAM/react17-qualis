package com.agaramtech.qualis.subcontracttestdetail;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.subcontracttestdetail.pojo.SubContractorTestDetail;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SubContractTestDetailDAOImpl implements SubContractTestDetailDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubContractTestDetailDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getSubContractTestDetail(UserInfo userInfo) throws Exception {
		final Map<String, Object> objMap = new HashMap<String, Object>();

		final String strOutsourceDetails = "select std.nsubcontractortestdetailcode,std.sfilename,std.ncontrolleadtime,std.nperiodcode,std.ntestcode, std.npreregno, std.ntransactionsamplecode, std.ntransactiontestcode,rsa.ssamplearno,ra.sarno,  "
				+ " case when std.sremarks = 'null' then '-' else std.sremarks end sremarks,  std.nsitecode,std.dcontrolleaddate ,TO_CHAR(std.dcontrolleaddate ,'dd/MM/yyyy') scontrolleaddate, "
				+ " std.dexpecteddate,TO_CHAR(std.dexpecteddate ,'dd/MM/yyyy') sexpecteddate,  std.jsontest->>'stestsynonym' stestsynonym,std.jsontest->>'stestname' stestname, s.ssitename ssourcesitename,CASE WHEN sp.nsuppliercode < 0 then '-'else sp.ssuppliername end as ssuppliername, "
				+ " COALESCE(ts.jsondata->'stransdisplaystatus'->>'en-US',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,"
				+ "(r.jsonuidata||jsonb_build_object('sarno',ra.sarno,'dregdate',r.jsonuidata->>'Transaction Date')::jsonb) as jsonsample"
				+ ",rs.jsonuidata jsonsubsample,std.nattachmenttypecode,"
				+ " std.nfilesize,std.sfilename,std.ssystemfilename,l.jsondata->>'slinkname' slinkname,l.nlinkcode,scth.ntransactionstatus"
				+ " from subcontractortesthistory scth,subcontractortestdetail std,registration r,registrationsample rs, "
				+ " registrationsamplearno rsa, registrationarno ra,supplier sp,transactionstatus ts,site s,linkmaster l"
				+ " where std.ntransactionsamplecode = rsa.ntransactionsamplecode" + " and r.npreregno = ra.npreregno "
				+ " and rsa.npreregno = ra.npreregno" + " and rsa.ntransactionsamplecode = rs.ntransactionsamplecode"
				+ " and std.npreregno = ra.npreregno" + " and ts.ntranscode = scth.ntransactionstatus"
				+ " and scth.nsubcontractortesthistorycode = (select max(nsubcontractortesthistorycode) from subcontractortesthistory where ntransactiontestcode = std.ntransactiontestcode group by  ntransactiontestcode)"
				+ " and std.nsuppliercode = sp.nsuppliercode and std.nlinkcode = l.nlinkcode"
				+ " and std.nsitecode=s.nsitecode  " + " and s.nsitecode = " + userInfo.getNtranssitecode() + ""
				+ " and scth.ntransactionstatus not in  ("
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")" + " and std.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  " + " and sp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  " + " and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  " + " and l.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  " + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by std.nsubcontractortestdetailcode desc;";

		final List<Map<String, Object>> lstOutsourceDetails = jdbcTemplate.queryForList(strOutsourceDetails);

		objMap.put("SubContractSamples", lstOutsourceDetails);
		return new ResponseEntity<>(objMap, HttpStatus.OK);

	}

	public SubContractorTestDetail checkValidateSubContractoretest(final int nsubcontractortestdetailcode,
			UserInfo userInfo) throws Exception {

//		final String checkSubcontractorSampelDetails = "select std.nsubcontractortestdetailcode, scth.ntransactionstatus,"
//				+ " COALESCE(ts.jsondata->'stransdisplaystatus'->>'"+userInfo.getSlanguagetypecode()+"',"
//				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,std.nattachmenttypecode,std.nfilesize,std.sfilename,std.ssystemfilename "
//				+ " from subcontractortesthistory scth,subcontractortestdetail std,transactionstatus ts "
//				+ " where scth.ntransactionstatus = ts.ntranscode "
//				+ " and scth.nsubcontractortesthistorycode = (select max(nsubcontractortesthistorycode) from subcontractortesthistory "
//				+ " where ntransactiontestcode = std.ntransactiontestcode and nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by  ntransactiontestcode)"
//				+ " and std.nsubcontractortestdetailcode ="+nsubcontractortestdetailcode+" "
//				//				+ " and scth.ntransactionstatus = "+Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()+" "
//				+ " and scth.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
//				+ " and std.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
//				+ " and ts.nstatus = "+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";
//		

		final String checkSubcontractorSampelDetails = "select std.nsubcontractortestdetailcode,std.sfilename,std.ncontrolleadtime,std.nperiodcode,std.ntestcode, std.npreregno, std.ntransactionsamplecode, std.ntransactiontestcode,rsa.ssamplearno,ra.sarno,  "
				+ " case when std.sremarks = 'null' then '-' else std.sremarks end sremarks,  std.nsitecode,std.dcontrolleaddate ,TO_CHAR(std.dcontrolleaddate ,'dd/MM/yyyy') scontrolleaddate, "
				+ " std.dexpecteddate,TO_CHAR(std.dexpecteddate ,'dd/MM/yyyy') sexpecteddate,  std.jsontest->>'stestsynonym' stestsynonym,std.jsontest->>'stestname' stestname, s.ssitename ssourcesitename,CASE WHEN sp.nsuppliercode < 0 then '-'else sp.ssuppliername end as ssuppliername, "
				+ " COALESCE(ts.jsondata->'stransdisplaystatus'->>'en-US',ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,r.jsonuidata as jsonsample,rs.jsonuidata jsonsubsample,std.nattachmenttypecode,"
				+ " std.nfilesize,std.sfilename,std.ssystemfilename,l.jsondata->>'slinkname' slinkname,l.nlinkcode,scth.ntransactionstatus"
				+ " from subcontractortesthistory scth,subcontractortestdetail std,registration r,registrationsample rs, "
				+ " registrationsamplearno rsa, registrationarno ra,supplier sp,transactionstatus ts,site s,linkmaster l"
				+ " where std.ntransactionsamplecode = rsa.ntransactionsamplecode" + " and r.npreregno = ra.npreregno "
				+ " and rsa.npreregno = ra.npreregno" + " and rsa.ntransactionsamplecode = rs.ntransactionsamplecode"
				+ " and std.npreregno = ra.npreregno" + " and ts.ntranscode = scth.ntransactionstatus"
				+ " and scth.nsubcontractortesthistorycode = (select max(nsubcontractortesthistorycode) from subcontractortesthistory where ntransactiontestcode = std.ntransactiontestcode group by  ntransactiontestcode)"
				+ " and std.nsuppliercode = sp.nsuppliercode and std.nlinkcode = l.nlinkcode"
				+ " and std.nsitecode=s.nsitecode  " + " and std.nsubcontractortestdetailcode ="
				+ nsubcontractortestdetailcode + " " + " and s.nsitecode = " + userInfo.getNtranssitecode() + ""
				+ " and scth.ntransactionstatus not in  ("
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ")" + " and std.nstatus= "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  " + " and sp.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  " + " and s.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  " + " and l.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  " + " and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by std.nsubcontractortestdetailcode desc;";

		// final List<SubContractorTestDetail> objsubConSamDetail =
		// jdbcTemplate.query(checkSubcontractorSampelDetails,new
		// SubContractorTestDetail());

		// return new ResponseEntity<>(objsubConSamDetail, HttpStatus.OK);

		return (SubContractorTestDetail) jdbcUtilityFunction.queryForObject(checkSubcontractorSampelDetails,
				SubContractorTestDetail.class, jdbcTemplate);

	}

	@Override
	public ResponseEntity<Object> getSubcontractorBytest(final int ntestcode, final int nsubcontractortestdetailcode,
			UserInfo userInfo) throws Exception {
		final Map<String, Object> returnMap = new HashMap<String, Object>();

		final SubContractorTestDetail objSubContractorTestDetail = checkValidateSubContractoretest(
				nsubcontractortestdetailcode, userInfo);

		if (objSubContractorTestDetail != null && objSubContractorTestDetail
				.getNtransactionstatus() != Enumeration.TransactionStatus.TOBESEND.gettransactionstatus()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTPENDINGTOSENT", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		final String subContractorGet = "select s.ssuppliername,s.ssuppliername || ' (' || tsc.ncontrolleadtime::text ||' '|| (p.jsondata->'speriodname'->>'en-US') || ')' as ssubcontractor ,"
				+ " s.nsuppliercode,tsc.ntestsubcontractorcode,tsc.ncontrolleadtime,p.nperiodcode"
				+ " from testsubcontractor tsc,supplier s,period p" + " where " + " p.nperiodcode = tsc.nperiodcode"
				+ " and tsc.nsuppliercode = s.nsuppliercode" + " and s.napprovalstatus = "
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "" + " and s.ntransactionstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "" + " and tsc.ntestcode = " + ntestcode
				+ " and tsc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and s.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and p.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

		final List<Map<String, Object>> lstSubcontractorDetails = jdbcTemplate.queryForList(subContractorGet);

		returnMap.put("TestSubContract", lstSubcontractorDetails);
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateSentSubContractTestdetails(
			final SubContractorTestDetail updateSubContracttestdetails, UserInfo userInfo) throws Exception {

		final SubContractorTestDetail objsubConSamTestDetail = checkValidateSubContractoretest(
				updateSubContracttestdetails.getNsubcontractortestdetailcode(), userInfo);

		if (objsubConSamTestDetail != null) {
			if (objsubConSamTestDetail.getNtransactionstatus() != Enumeration.TransactionStatus.CANCELED
					.gettransactionstatus()) {

				final LocalDate currentDate = LocalDate.now();
				// Add days alone to the current date
				final LocalDate newDate = currentDate.plusDays(updateSubContracttestdetails.getNcontrolleadtime());
				final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				final String formattedDate = newDate.format(formatter);

				final String updateQry = "update subcontractortestdetail set nsuppliercode = "
						+ updateSubContracttestdetails.getNsuppliercode() + ",dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " dexpecteddate = '" + formattedDate
						+ "',ncontrolleadtime = " + updateSubContracttestdetails.getNcontrolleadtime()
						+ ",nperiodcode = " + updateSubContracttestdetails.getNperiodcode()
						+ " where nsubcontractortestdetailcode = "
						+ updateSubContracttestdetails.getNsubcontractortestdetailcode() + " " + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				jdbcTemplate.execute(updateQry);

				final String insertHistory = "INSERT INTO subcontractortesthistory (nsubcontractortestdetailcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,ntransactionstatus,dtransactiondate,noffsetdtransactiondate,"
						+ "		ntransdatetimezonecode,scomments,nsitecode,nstatus)"
						+ "		SELECT nsubcontractortestdetailcode,npreregno,ntransactionsamplecode,ntransactiontestcode,"
						+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode()
						+ "," + userInfo.getNdeputyuserrole() + ","
						+ Enumeration.TransactionStatus.SENT.gettransactionstatus() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
						+ userInfo.getNtimezonecode() + ",''," + userInfo.getNtranssitecode() + ",1"
						+ "		from subcontractortestdetail where nsubcontractortestdetailcode = "
						+ updateSubContracttestdetails.getNsubcontractortestdetailcode() + "";

				jdbcTemplate.execute(insertHistory);

				final String auditRecord = "select ssuppliername from supplier where nsuppliercode="
						+ updateSubContracttestdetails.getNsuppliercode() + " " + "and nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ";

				final List<Map<String, Object>> lstAuditRecord = jdbcTemplate.queryForList(auditRecord);

				updateSubContracttestdetails.setSsuppliername((String) lstAuditRecord.get(0).get("ssuppliername"));
				// updateSubContracttestdetails.setSsamplearno((String)
				// lstAuditRecord.get(0).get("ssamplearno"));
				// objsubConSamTestDetail.setSsamplearno((String)
				// lstAuditRecord.get(0).get("ssamplearno"));

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> listAfterUpdate = new ArrayList<>();
				final List<Object> listBeforeUpdate = new ArrayList<>();
				objsubConSamTestDetail
						.setNtransactionstatus((short) Enumeration.TransactionStatus.SENT.gettransactionstatus());
				listAfterUpdate.add(updateSubContracttestdetails);
				listBeforeUpdate.add(objsubConSamTestDetail);
				multilingualIDList.add("IDS_SENTTHERECORD");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("TESTALREADYCANCELLED", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTPENDINGTOSENT", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
		return new ResponseEntity<>(getSubContractTestDetail(userInfo).getBody(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateReceiveSTTSubContractTest(
			final SubContractorTestDetail updateSubContracttestdetails, UserInfo userInfo) throws Exception {

		final SubContractorTestDetail objsubConSamDetail = checkValidateSubContractoretest(
				updateSubContracttestdetails.getNsubcontractortestdetailcode(), userInfo);

		if (objsubConSamDetail != null) {
			if (objsubConSamDetail.getNtransactionstatus() == Enumeration.TransactionStatus.SENT
					.gettransactionstatus()) {

				final LocalDate currentDate = LocalDate.now();
				// Add days alone to the current date
				final LocalDate newDate = currentDate.plusDays(updateSubContracttestdetails.getNcontrolleadtime());
				final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				final String formattedDate = newDate.format(formatter);

				final String updateQry = "update subcontractortestdetail set dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + " dcontrolleaddate = '"
						+ formattedDate + "' where nsubcontractortestdetailcode = "
						+ updateSubContracttestdetails.getNsubcontractortestdetailcode() + " " + " and nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

				jdbcTemplate.execute(updateQry);

				final String insertHistory = "INSERT INTO subcontractortesthistory (nsubcontractortestdetailcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,ntransactionstatus,dtransactiondate,noffsetdtransactiondate,"
						+ "		ntransdatetimezonecode,scomments,nsitecode,nstatus)"
						+ "		SELECT nsubcontractortestdetailcode,npreregno,ntransactionsamplecode,ntransactiontestcode,"
						+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode()
						+ "," + userInfo.getNdeputyuserrole() + ","
						+ Enumeration.TransactionStatus.RECEIVEDBYSTT.gettransactionstatus() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
						+ userInfo.getNtimezonecode() + ",''," + userInfo.getNtranssitecode() + ",1"
						+ "		from subcontractortestdetail where nsubcontractortestdetailcode = "
						+ updateSubContracttestdetails.getNsubcontractortestdetailcode() + "";

				jdbcTemplate.execute(insertHistory);

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> listAfterUpdate = new ArrayList<>();
				final List<Object> listBeforeUpdate = new ArrayList<>();
				objsubConSamDetail
						.setNtransactionstatus((short) Enumeration.TransactionStatus.SENT.gettransactionstatus());

				updateSubContracttestdetails.setNtransactionstatus(
						(short) Enumeration.TransactionStatus.RECEIVEDBYSTT.gettransactionstatus());

				listAfterUpdate.add(updateSubContracttestdetails);
				listBeforeUpdate.add(objsubConSamDetail);
				multilingualIDList.add("IDS_RECEIVEDBYSTT");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SELECTSENTRECORD", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTSENTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);

		}
		return new ResponseEntity<>(getSubContractTestDetail(userInfo).getBody(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateReceiveResultSubContractTest(
			final SubContractorTestDetail updateSubContracttestdetails, UserInfo userInfo) throws Exception {

		final SubContractorTestDetail objsubConSamDetail = checkValidateSubContractoretest(
				updateSubContracttestdetails.getNsubcontractortestdetailcode(), userInfo);

		if (objsubConSamDetail != null) {
			if (objsubConSamDetail.getNtransactionstatus() == Enumeration.TransactionStatus.RECEIVEDBYSTT
					.gettransactionstatus()) {

				final String insertHistory = "INSERT INTO subcontractortesthistory (nsubcontractortestdetailcode,npreregno,ntransactionsamplecode,ntransactiontestcode,nusercode,nuserrolecode,ndeputyusercode,ndeputyuserrolecode,ntransactionstatus,dtransactiondate,noffsetdtransactiondate,"
						+ "		ntransdatetimezonecode,scomments,nsitecode,nstatus)"
						+ "		SELECT nsubcontractortestdetailcode,npreregno,ntransactionsamplecode,ntransactiontestcode,"
						+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode()
						+ "," + userInfo.getNdeputyuserrole() + ","
						+ Enumeration.TransactionStatus.RESULTRECEIVED.gettransactionstatus() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "',"
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
						+ userInfo.getNtimezonecode() + ",''," + userInfo.getNtranssitecode() + ",1"
						+ "		from subcontractortestdetail where nsubcontractortestdetailcode = "
						+ updateSubContracttestdetails.getNsubcontractortestdetailcode() + "";

				jdbcTemplate.execute(insertHistory);

				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> listAfterUpdate = new ArrayList<>();
				final List<Object> listBeforeUpdate = new ArrayList<>();
				objsubConSamDetail.setNtransactionstatus(
						(short) Enumeration.TransactionStatus.RECEIVEDBYSTT.gettransactionstatus());

				updateSubContracttestdetails.setNtransactionstatus(
						(short) Enumeration.TransactionStatus.RESULTRECEIVED.gettransactionstatus());

				listAfterUpdate.add(updateSubContracttestdetails);
				listBeforeUpdate.add(objsubConSamDetail);
				multilingualIDList.add("IDS_RECEIVEDRESULT");
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTRECEIVEDSTTRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTRECEIVEDSTTRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

		}
		return new ResponseEntity<>(getSubContractTestDetail(userInfo).getBody(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateSubcontractorTestFile(MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		// final String sQuery = " lock table supplierfile "+
		// Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		// jdbcTemplate.execute(sQuery);

		final List<SubContractorTestDetail> lstReqSubContractorTestFile = objMapper.readValue(
				request.getParameter("subcontractorFile"), new TypeReference<List<SubContractorTestDetail>>() {
				});

		if (lstReqSubContractorTestFile != null && lstReqSubContractorTestFile.size() > 0) {
			final SubContractorTestDetail SubContractorTest = checkValidateSubContractoretest(
					lstReqSubContractorTestFile.get(0).getNsubcontractortestdetailcode(), userInfo);
			;

			if (SubContractorTest != null) {

				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (lstReqSubContractorTestFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP
						.gettype()) {

					sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo); // Folder Name - master
				}

				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {

					final Instant instantDate = dateUtilityFunction.getCurrentDateTime(userInfo)
							.truncatedTo(ChronoUnit.SECONDS);
					final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
					// final int noffset =
					// dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid());

					final String updatequery = "update subcontractortestdetail set sfilename = N'"
							+ stringUtilityFunction.replaceQuote(lstReqSubContractorTestFile.get(0).getSfilename())
							+ "'," + " ssystemfilename=N'"
							+ stringUtilityFunction.replaceQuote(
									lstReqSubContractorTestFile.get(0).getSsystemfilename())
							+ "'," + " nfilesize = " + lstReqSubContractorTestFile.get(0).getNfilesize()
							+ ",nattachmenttypecode = " + lstReqSubContractorTestFile.get(0).getNattachmenttypecode()
							+ "," + " nlinkcode = " + lstReqSubContractorTestFile.get(0).getNlinkcode()
							+ ",dfileupdateddate = '" + sattachmentDate + "' "
							+ " where nsubcontractortestdetailcode = "
							+ lstReqSubContractorTestFile.get(0).getNsubcontractortestdetailcode() + ";";
					jdbcTemplate.execute(updatequery);

				} else {
					// status code:417
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SUPPLIERALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			return (getSubContractTestDetail(userInfo));
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> editSubcontractorTestFile(final SubContractorTestDetail objSubContractorTestDetail,
			final UserInfo objUserInfo) throws Exception {

		final SubContractorTestDetail SubContractorTest = checkValidateSubContractoretest(
				objSubContractorTestDetail.getNsubcontractortestdetailcode(), objUserInfo);
		;

		if (SubContractorTest != null) {
			return new ResponseEntity<Object>(SubContractorTest, HttpStatus.OK);
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> viewSubcontractorSampleDetail(
			final SubContractorTestDetail objSubContractorTestDetail, final UserInfo objUserInfo) throws Exception {

		final SubContractorTestDetail SubContractorTest = checkValidateSubContractoretest(
				objSubContractorTestDetail.getNsubcontractortestdetailcode(), objUserInfo);
		;
		final Map<String, Object> returnMap = new HashMap<>();

		if (SubContractorTest != null) {

			final String str = "select jsondata->'" + 43 + "' as jsondata from mappedtemplatefieldprops"
					+ " where ndesigntemplatemappingcode = any (select ndesigntemplatemappingcode from subcontractortestdetail std, registration r "
					+ " where std.npreregno = r.npreregno and nsubcontractortestdetailcode = "
					+ objSubContractorTestDetail.getNsubcontractortestdetailcode() + ")" + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			returnMap.put("DynamicDesign", jdbcTemplate.queryForMap(str));

			return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public Map<String, Object> viewSubcontractorTestFile(SubContractorTestDetail objSubContractorTestDetail,
			UserInfo userInfo) throws Exception {
		final Map<String, Object> map = new HashMap<>();
		final SubContractorTestDetail SubContractorTest = checkValidateSubContractoretest(
				objSubContractorTestDetail.getNsubcontractortestdetailcode(), userInfo);
		;
		if (SubContractorTest != null) {
			String sQuery = "select * from subcontractortestdetail where nsubcontractortestdetailcode = "
					+ objSubContractorTestDetail.getNsubcontractortestdetailcode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final SubContractorTestDetail objSF = jdbcTemplate.queryForObject(sQuery, SubContractorTestDetail.class);
			if (objSF != null) {
				if (objSF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					// map = ftpUtilityFunction.getFileFTPUpload(objSF.getSsystemfilename(), -1,
					// userInfo); // need to discuss pass the parameter request
				} else {
					sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="
							+ objSF.getNlinkcode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final LinkMaster objlinkmaster = jdbcTemplate.queryForObject(sQuery, LinkMaster.class);
					map.put("AttachLink", objlinkmaster.getSlinkname()); // ALPD-5419 - Gowtham R - 14/02/2025 -
																			// SubContractor - Screen Bugs
				}

			} else {
				// status code:417
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			// status code:417
//			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CANCELLEDORREJECTED", objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		return map;
	}
}
