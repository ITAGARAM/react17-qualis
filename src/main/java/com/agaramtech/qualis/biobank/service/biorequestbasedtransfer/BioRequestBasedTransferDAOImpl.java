package com.agaramtech.qualis.biobank.service.biorequestbasedtransfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.hpsf.Array;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.model.TransactionStatus;
import com.agaramtech.qualis.biobank.model.BioRequestBasedTransfer;
import com.agaramtech.qualis.biobank.model.BioRequestBasedTransferDetails;
import com.agaramtech.qualis.biobank.model.StorageType;
import com.agaramtech.qualis.biobank.model.TransferType;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.Product;
import com.agaramtech.qualis.project.model.BioProject;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class BioRequestBasedTransferDAOImpl implements BioRequestBasedTransferDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(BioRequestBasedTransferDAOImpl.class);

	private final AuditUtilityFunction auditUtilityFunction;
	private final CommonFunction commonFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcTemplateUtilityFunction;
	// private final JdbcTemplateUtilityFunction jdbcTemplateUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final StringUtilityFunction stringUtilityFunction;
	private final EmailDAOSupport emailDAOSupport;

	private String auditEditQuery(int nbioRequestBasedTransferCode, String nbioRequestBasedTransferDetailCode,
			UserInfo userInfo) {
		// TODO Auto-generated method stub

		final var editQuery = "select sformnumber,to_char(dtransferdate, '" + userInfo.getSdatetimeformat()
				+ "') stransferdate, jsondata->>'sremarks' sremarks from biorequestbasedtransfer "
				+ "where nbiorequestbasedtransfercode = " + nbioRequestBasedTransferCode + " " + "and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   order by 1 desc";

		return editQuery;
	}

	public String auditQuery(final int nbioRequestBasedTransferCode, final String nBioRequestBasedTransferDetailsCode,
			final UserInfo userInfo) throws Exception {
		var strConcat = "";
		if (!"".equals(nBioRequestBasedTransferDetailsCode)) {
			strConcat = " and bdtd.nbiorequestbasedtransferdetailcode in (" + nBioRequestBasedTransferDetailsCode + ")";
		}
		final var strAuditQry = "select   row_number() over(order by bdtd.nbiorequestbasedtransferdetailcode desc) as nserialno, bdtd.nbiorequestbasedtransferdetailcode,  "
				+ "  bdtd.nbiorequestbasedtransfercode,bdtd.jsondata->>'sparentsamplecode' sparentsamplecode, bdtd.srepositoryid, bdtd.slocationcode, bdtd.svolume,coalesce( ts2.jsondata -> 'stransdisplaystatus' ->> '"
				+ userInfo.getSlanguagetypecode() + "',  "
				+ "    ts2.jsondata -> 'stransdisplaystatus' ->> 'en-US') as ssamplecondition, coalesce( ts3.jsondata -> 'stransdisplaystatus' ->> '"
				+ userInfo.getSlanguagetypecode() + "',  "
				+ "    ts3.jsondata -> 'stransdisplaystatus' ->> 'en-US') as stransferstatus, r.sreason,p.sproductname ,  brt.jsondata->>'sreceiversitename' originsite,bec.sformnumber,"
				+ "brt.nreceiversitecode,bdtd.nbioprojectcode,bdtd.nproductcode,bdtd.slocationcode,bdtd.srepositoryid,bdtd.svolume,"
				+ "to_char(brt.dtransferdate,'" + userInfo.getSsitedate()
				+ "')  stransferdate,brt.jsondata->>'sremarks' sremarks,bdtd.jsondata->>'sparentsamplecode' sparentsamplecode,tt.jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode() + "' as stransfertypename ,bec.sformnumber as srequestformno "
				+ "from biorequestbasedtransferdetails bdtd  join bioecataloguereqapproval bec on bec.necatrequestreqapprovalcode= bdtd.necatrequestreqapprovalcode "
				+ "  and bec.nsitecode = bdtd.nsitecode " + "  and bec.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "join biorequestbasedtransfer brt "
				+ "on bdtd.nbiorequestbasedtransfercode = brt.nbiorequestbasedtransfercode  "
				+ "and bdtd.nsitecode = brt.nsitecode and brt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ " join transfertype tt on brt.ntransfertypecode = tt.ntransfertypecode " + "and tt.nsitecode ="
				+ userInfo.getNmastersitecode() + " " + "  and tt.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "join site s on brt.nreceiversitecode = s.nsitecode and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " join transactionstatus ts2  on bdtd.nsamplecondition = ts2.ntranscode  and ts2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ "join transactionstatus ts3  on bdtd.ntransferstatus = ts3.ntranscode  and ts3.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left join reason r  on bdtd.nreasoncode = r.nreasoncode "
				+ "  join product p on p.nproductcode = bdtd.nproductcode  and p.nsitecode ="
				+ userInfo.getNmastersitecode() + " and p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and bdtd.nsitecode = "
				+ userInfo.getNtranssitecode() + " and bdtd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and bdtd.ntransferstatus != "
				+ Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()
				+ "  and bdtd.nbiorequestbasedtransfercode = " + nbioRequestBasedTransferCode + " " + strConcat
				+ "  order by nserialno desc ";

		return strAuditQry;
	}

	private String auditQueryCourier(int nbiorequestbasedtransfercode, String string, UserInfo userInfo) {

		final var strQuery = "select dt.nbiorequestbasedtransfercode, dt.sformnumber, dt.jsondata->>'sstorageconditionname' sstorageconditionname ,to_char(dt.ddeliverydate, 'dd/MM/yyyy') sdeliverydate, "
				+ " dt.jsondata->>'sdispatchername' sdispatchername, dt.jsondata->>'scouriername' scouriername,"
				+ "dt.jsondata->>'scourierno' scourierno, dt.jsondata->>'striplepackage' striplepackage,  dt.jsondata->>'svalidationremarks' svalidationremarks,  coalesce(ts.jsondata -> 'stransdisplaystatus' ->> '"
				+ userInfo.getSlanguagetypecode() + "', "
				+ "    ts.jsondata -> 'stransdisplaystatus' ->> 'en-US') stransferstatus "
				+ "from biorequestbasedtransfer dt "
				+ " join transactionstatus ts on dt.ntransactionstatus=ts.ntranscode and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join formwisestatuscolor fwsc on fwsc.ntranscode=ts.ntranscode and fwsc.nsitecode="
				+ userInfo.getNmastersitecode() + " and fwsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fwsc.nformcode="
				+ userInfo.getNformcode() + " join colormaster cm on cm.ncolorcode=fwsc.ncolorcode and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where dt.nsitecode="
				+ userInfo.getNtranssitecode() + " and dt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dt.nbiorequestbasedtransfercode="
				+ nbiorequestbasedtransfercode + " order by dt.nbiorequestbasedtransfercode desc";
		return strQuery;
	}

	@Override
	public ResponseEntity<Object> cancelRequestBasedTransfer(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final var nbioRequestBasedTransferCode = (int) inputMap.get("nbiorequestbasedtransfercode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> defaultListAfterSave = new ArrayList<>();
		final List<Object> defaultListBeforeSave = new ArrayList<>();

		final var recordStatus = findStatusRequestBasedtTransfer(nbioRequestBasedTransferCode, userInfo);

		if (recordStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| recordStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			final var strAuditQryForm = " select brt.nbiorequestbasedtransfercode,brt.sformnumber,coalesce("
					+ "    ts.jsondata -> 'stransdisplaystatus' ->> '" + userInfo.getSlanguagetypecode() + "', "
					+ "    ts.jsondata -> 'stransdisplaystatus' ->> 'en-US'"
					+ "  ) as stransferstatus from biorequestbasedtransfer brt"
					+ "    join transactionstatus ts on brt.ntransactionstatus = ts.ntranscode " + "  and ts.nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "and brt.nbiorequestbasedtransfercode=" + nbioRequestBasedTransferCode + "  order by 1 desc";

			final var lstAuditBefore = (BioRequestBasedTransfer) jdbcTemplateUtilityFunction
					.queryForObject(strAuditQryForm, BioRequestBasedTransfer.class, jdbcTemplate);

			final var strCancelRequestBasedTransfer = "update biorequestbasedtransfer set ntransactionstatus="
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
					+ " where nbiorequestbasedtransfercode=" + nbioRequestBasedTransferCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			final var strCancelRequestBasedTransferDetails = "update biorequestbasedtransferdetails set ntransferstatus="
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
					+ " where nbiorequestbasedtransfercode=" + nbioRequestBasedTransferCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			var seqNoBioRequetBasedTransferHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biorequestbasedtransferhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioRequetBasedTransferHistory++;

			final var strInsertRequestTransferHistory = "INSERT INTO public.biorequestbasedtransferhistory("
					+ " nbiorequestbasedtransferhistorycode, nbiorequestbasedtransfercode, sformnumber, "
					+ " ntransfertypecode, ntransactionstatus, dtransactiondate, ntztransactiondate, "
					+ " noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus)"
					+ " SELECT " + seqNoBioRequetBasedTransferHistory + ", " + nbioRequestBasedTransferCode
					+ ", sformnumber,  ntransfertypecode ,"
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " FROM biorequestbasedtransfer WHERE nbiorequestbasedtransfercode = "
					+ nbioRequestBasedTransferCode + ";";

			final StringBuilder strSeqNoUpdate = new StringBuilder();

			strSeqNoUpdate.append("update seqnobiobankmanagement set nsequenceno=")
					.append(seqNoBioRequetBasedTransferHistory).append(" where")
					.append(" stablename='biorequestbasedtransferhistory' and nstatus=")
					.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");

			jdbcTemplate.execute(strCancelRequestBasedTransfer + strCancelRequestBasedTransferDetails
					+ strInsertRequestTransferHistory + strSeqNoUpdate);

			final var lstAuditAfter = (BioRequestBasedTransfer) jdbcTemplateUtilityFunction
					.queryForObject(strAuditQryForm, BioRequestBasedTransfer.class, jdbcTemplate);

			lstAuditAfter.setSformnumber("");
			defaultListBeforeSave.add(lstAuditAfter);
			defaultListAfterSave.add(lstAuditBefore);
			multilingualIDList.add("IDS_CANCELTRANSFERFORM");

			auditUtilityFunction.fnInsertAuditAction(defaultListBeforeSave, 2, defaultListAfterSave, multilingualIDList,
					userInfo);

			return getActiveBioRequestBasedTransfer(inputMap, nbioRequestBasedTransferCode, userInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public boolean checkApprovedSample(final int nbiorequestbasedtransfercode, final UserInfo userInfo)
			throws Exception {
		final var strCheckAllSamplesApproved = "select not exists (select nbiorequestbasedtransferdetailcode from biorequestbasedtransferdetails"
				+ " where nbiorequestbasedtransfercode=" + nbiorequestbasedtransfercode + " and nsamplecondition="
				+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + " and ntransferstatus="
				+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
		return jdbcTemplate.queryForObject(strCheckAllSamplesApproved, boolean.class);
	}

	public boolean checkSamplesAvailableToTransfer(final int nbioRequestBasedTransferCode, final UserInfo userInfo)
			throws Exception {
		final var strCheck = "select exists(select from biorequestbasedtransferdetails where nbiorequestbasedtransfercode="
				+ nbioRequestBasedTransferCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ntransferstatus not in ("
				+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + "))";
		return jdbcTemplate.queryForObject(strCheck, boolean.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> createBioRequestbasedTransfer(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final var objMapper = new ObjectMapper();

		final String sQuery = " lock  table lockbiorequestbasedtransfer "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final var objBioRequestBasedTransferData = objMapper.convertValue(inputMap.get("biorequestbasedtransfer"),
				BioRequestBasedTransfer.class);

		boolean isVolumeAdded = isVolumeAdded(inputMap, objBioRequestBasedTransferData, userInfo);

		if (isVolumeAdded) {

			final var nEcatRequestreqApprovalCode = (int) inputMap.get("necatrequestreqapprovalcode");

			var nBioRequestBasedTransferCode = inputMap.getOrDefault("nbiorequestbasedtransfercode",
					-1) instanceof Number ? ((Number) inputMap.get("nbiorequestbasedtransfercode")).intValue() : -1;

			final var filteredSampleReceiving = getValidationSampleToAdd(objBioRequestBasedTransferData,
					(List<Map<String, Object>>) inputMap.get("filterSelectedSamples"), userInfo);

			final var stransferDate = (objBioRequestBasedTransferData.getStransferdate() != null
					&& !objBioRequestBasedTransferData.getStransferdate().isEmpty())
							? "'" + objBioRequestBasedTransferData.getStransferdate().toString().replace("T", " ")
									.replace("Z", "") + "'"
							: null;

			final BioRequestBasedTransfer objBioRequestBasedTransfer = null;

			var seqNoBioRequstBasedTransfer = -1;
			var strInsertRequestBasedTransfer = "";
			var strInsertRequestTransferHistory = "";
			final var strSeqNoUpdate = new StringBuilder();

			if (nBioRequestBasedTransferCode < 0) {
				final var strQuery = "select becrq.*, s.ssitename soriginsitename from bioecataloguereqapproval becrq join site s on s.nsitecode=becrq.norginsitecode"
						+ " and s.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and s.nmastersitecode=" + userInfo.getNmastersitecode()
						+ " where becrq.necatrequestreqapprovalcode="
						+ objBioRequestBasedTransferData.getNecatrequestreqapprovalcode() + "  and becrq.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and becrq.nsitecode="
						+ userInfo.getNtranssitecode() + " order by 1 desc";
				final var lstBioECatlogRecord = jdbcTemplate.queryForList(strQuery);

				final var norginthirdpartycode = lstBioECatlogRecord.getFirst().get("norginthirdpartycode");
				final var norginsitecode = lstBioECatlogRecord.getFirst().get("norginsitecode");
				final var soriginsitename = lstBioECatlogRecord.getFirst().get("soriginsitename");

				seqNoBioRequstBasedTransfer = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnobiobankmanagement where stablename='biorequestbasedtransfer' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);
				seqNoBioRequstBasedTransfer++;

				var seqNoBioRequetBasedTransferHistory = jdbcTemplate.queryForObject(
						"select nsequenceno from seqnobiobankmanagement where stablename='biorequestbasedtransferhistory' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
						Integer.class);
				seqNoBioRequetBasedTransferHistory++;

				nBioRequestBasedTransferCode = seqNoBioRequstBasedTransfer;
				final var strformat = projectDAOSupport.getSeqfnFormat("biorequestbasedtransfer",
						"seqnoformatgeneratorbiobank", 0, 0, userInfo);

				strInsertRequestBasedTransfer = "insert into biorequestbasedtransfer(nbiorequestbasedtransfercode, sformnumber,"
						+ " ntransfertypecode,nthirdpartycode, nreceiversitecode, dtransferdate, ntztransferdate, noffsetdtransferdate,"
						+ " ntransactionstatus, jsondata, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,"
						+ " nsitecode, nstatus) values (" + nBioRequestBasedTransferCode + ", '" + strformat + "',"
						+ objBioRequestBasedTransferData.getNtransfertypecode() + "," + norginthirdpartycode + ","
						+ norginsitecode + "," + stransferDate + ", " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
						+ " , json_build_object('sformnumber', '" + strformat + "', 'sremarks', '"
						+ stringUtilityFunction.replaceQuote(objBioRequestBasedTransferData.getSremarks())
						+ "', 'sreceiversitename'," + " '" + soriginsitename + "'), '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

				strInsertRequestTransferHistory = "INSERT INTO public.biorequestbasedtransferhistory("
						+ "	nbiorequestbasedtransferhistorycode, nbiorequestbasedtransfercode, sformnumber, "
						+ "ntransfertypecode, ntransactionstatus, dtransactiondate, ntztransactiondate, "
						+ "noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus)"
						+ "	VALUES (" + seqNoBioRequetBasedTransferHistory + "," + " " + nBioRequestBasedTransferCode
						+ ",'" + strformat + "'," + objBioRequestBasedTransferData.getNtransfertypecode() + ","
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
						+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
						+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
						+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
						+ userInfo.getNtranssitecode() + ", "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";

				strSeqNoUpdate.append("update seqnobiobankmanagement set nsequenceno=")
						.append(seqNoBioRequstBasedTransfer).append(" where")
						.append(" stablename='biorequestbasedtransfer' and nstatus=")
						.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");

				strSeqNoUpdate.append("update seqnobiobankmanagement set nsequenceno=")
						.append(seqNoBioRequetBasedTransferHistory).append(" where")
						.append(" stablename='biorequestbasedtransferhistory' and nstatus=")
						.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");

				objBioRequestBasedTransferData.setSformnumber(strformat);
			} else {

				String sformNumber = (String) inputMap.get("sformnumber");
				objBioRequestBasedTransferData.setSformnumber(sformNumber);
				LOGGER.info("Save and Continue successful.");

			}

			Map<String, Object> rtnQry = new HashMap<>();

			if (!filteredSampleReceiving.isEmpty()) {
				rtnQry = insertBioRequestBasedTransferDetails(objBioRequestBasedTransferData, filteredSampleReceiving,
						nBioRequestBasedTransferCode, nEcatRequestreqApprovalCode, userInfo);
			}
			final var childRecord = (String) rtnQry.get("InsertQuery");

			jdbcTemplate.execute(strInsertRequestBasedTransfer + childRecord + strSeqNoUpdate.toString()
					+ strInsertRequestTransferHistory);

			if (objBioRequestBasedTransfer == null) {
				final List<Object> lstAuditAfter = new ArrayList<>();
				final List<String> multilingualIDList = new ArrayList<>();
				final var selectedChildRecord = (String) rtnQry.get("selectedChildRecord");
				final var strAuditQry = auditQuery(nBioRequestBasedTransferCode, selectedChildRecord, userInfo);
				final List<BioRequestBasedTransfer> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditQry,
						new BioRequestBasedTransfer());
				lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
				lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_ADDTRANSFERSAMPLES"));
				auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);

				// Added By Mullai Balaji for Email jira ID-BGSI-147
				String Query = "select DISTINCT (ncontrolcode) from emailconfig where ncontrolcode="
						+ inputMap.get("ncontrolcode") + " " + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				Integer value = null;
				try {
					value = jdbcTemplate.queryForObject(Query, Integer.class);
				} catch (Exception e) {
					value = null;
				}

				if (value != null) {
					final Map<String, Object> mailMap = new HashMap<String, Object>();
					mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));
					mailMap.put("nbiorequestbasedtransfercode", nBioRequestBasedTransferCode);

					String query = "SELECT sformnumber FROM biorequestbasedtransfer where nbiorequestbasedtransfercode="
							+ nBioRequestBasedTransferCode;
					String referenceId = jdbcTemplate.queryForObject(query, String.class);
					mailMap.put("ssystemid", referenceId);
					final UserInfo mailUserInfo = new UserInfo(userInfo);
					mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
					mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
					emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
				}

			}

			final var ntransCode = (int) inputMap.get("ntransCode");
			inputMap.put("ntransCode",
					ntransCode == 0 ? ntransCode : Enumeration.TransactionStatus.DRAFT.gettransactionstatus());

			inputMap.put("ntransfertypecode", (int) objBioRequestBasedTransferData.getNtransfertypecode());

			inputMap.put("nbiorequestbasedtransfercode", nBioRequestBasedTransferCode);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_ADDEDSAMPLEISHIGH", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		return getBioRequestBasedTransfer(inputMap, -1, userInfo);

	}

	private boolean isVolumeAdded(Map<String, Object> inputMap, BioRequestBasedTransfer objBioRequestBasedTransferData,
			UserInfo userInfo) throws Exception {

		boolean isSampleCountValidation = false;

		int selectedsamplecount = (int) inputMap.get("selectedsamplecount");

		final var parentSampleCodeStr = "select nreqformtypecode from bioecataloguereqapproval  where necatrequestreqapprovalcode="
				+ inputMap.get("necatrequestreqapprovalcode") + "  and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc";

		final Object nReqformTypeCodeObj = Objects.requireNonNullElse(
				jdbcTemplateUtilityFunction.queryForObject(parentSampleCodeStr, Integer.class, jdbcTemplate), -1);

		final int nReqformTypeCode = (Integer) nReqformTypeCodeObj;

		var parentSample = "";
		if (nReqformTypeCode == Enumeration.RequestFormType.BIOSAMPLEBASED.getRequestFormType()) {

			parentSample = "";

		} else if (nReqformTypeCode == Enumeration.RequestFormType.PARENTSAMPLEBASED.getRequestFormType()) {

			parentSample = " and sparentsamplecode='" + objBioRequestBasedTransferData.getSbioparentsamplecode() + "' ";

		}

		final var noOfSampleQry = "SELECT COALESCE(( SELECT naccnoofsamples "
				+ "FROM bioecataloguerequestdetails  where nbioprojectcode = "
				+ objBioRequestBasedTransferData.getNbioprojectcode() + "  AND nproductcode = "
				+ objBioRequestBasedTransferData.getNproductcode() + "  AND necatrequestreqapprovalcode = "
				+ inputMap.get("necatrequestreqapprovalcode") + " " + parentSample + "  AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND nsitecode = "
				+ userInfo.getNtranssitecode() + " " + " ), 0) AS naccnoofsamples ;";

		final var noOfSample = Objects.requireNonNullElse(
				jdbcTemplateUtilityFunction.queryForObject(noOfSampleQry, Integer.class, jdbcTemplate), 0);

		final var findRemainSample = "SELECT COALESCE(( SELECT count(*) "
				+ "FROM biorequestbasedtransferdetails  where  ntransferstatus not in ("
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ") AND nbioprojectcode = "
				+ objBioRequestBasedTransferData.getNbioprojectcode() + "  AND nproductcode = "
				+ objBioRequestBasedTransferData.getNproductcode() + "  " + " and necatrequestreqapprovalcode = "
				+ inputMap.get("necatrequestreqapprovalcode") + "  AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND nsitecode = "
				+ userInfo.getNtranssitecode() + " " + " ), 0) AS savedsamples ;";

		final var findSaveSampleInRequestApproval = Objects.requireNonNullElse(
				jdbcTemplateUtilityFunction.queryForObject(findRemainSample, Integer.class, jdbcTemplate), 0);

		int storeAddedSample = (int) noOfSample - (int) findSaveSampleInRequestApproval;
		int totalCountSample = selectedsamplecount + (int) findSaveSampleInRequestApproval;
		if (totalCountSample <= (int) noOfSample) {
			isSampleCountValidation = true;
		} else {
			isSampleCountValidation = false;
		}

		return isSampleCountValidation;
	}

	@Override
	public ResponseEntity<Object> createChildBioRequestBasedTransfer(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final var objMapper = new ObjectMapper();

		final String sQuery = " lock  table lockbiorequestbasedtransfer "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final var nbioRequestBasedTransferCode = (int) inputMap.get("nbiorequestbasedtransfercode");

		final var nEcatRequestreqApprovalCode = (int) inputMap.get("necatrequestreqapprovalcode");

		final var findStatus = findStatusRequestBasedtTransfer(nbioRequestBasedTransferCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			final var objBioRequestBasedTransferData = objMapper.convertValue(inputMap.get("biorequestbasedtransfer"),
					BioRequestBasedTransfer.class);

			boolean isVolumeAdded = isVolumeAdded(inputMap, objBioRequestBasedTransferData, userInfo);

			if (isVolumeAdded) {

				final var filteredSampleReceiving = getValidationSampleToAdd(objBioRequestBasedTransferData,
						(List<Map<String, Object>>) inputMap.get("filterSelectedSamples"), userInfo);

				if (!filteredSampleReceiving.isEmpty()) {

					Map<String, Object> rtnQry = rtnQry = insertBioRequestBasedTransferDetails(
							objBioRequestBasedTransferData, filteredSampleReceiving, nbioRequestBasedTransferCode,
							nEcatRequestreqApprovalCode, userInfo);

					final var childRecord = (String) rtnQry.get("InsertQuery");

					final var strUpdateStatus = "update biorequestbasedtransfer set ntransactionstatus="
							+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
							+ " where nbiorequestbasedtransfercode=" + nbioRequestBasedTransferCode + " and nsitecode="
							+ userInfo.getNtranssitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					jdbcTemplate.execute(childRecord + strUpdateStatus);

					final List<Object> lstAuditAfter = new ArrayList<>();
					final List<String> multilingualIDList = new ArrayList<>();
					final var selectedChildRecord = (String) rtnQry.get("selectedChildRecord");
					final var strAuditQry = auditQuery(nbioRequestBasedTransferCode, selectedChildRecord, userInfo);
					final List<BioRequestBasedTransfer> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditQry,
							new BioRequestBasedTransfer());
					lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
					lstAuditTransferDetailsAfter.stream()
							.forEach(x -> multilingualIDList.add("IDS_ADDTRANSFERSAMPLES"));
					auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);

				}

			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_ADDEDSAMPLEISHIGH", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}

			return getActiveBioRequestBasedTransfer(inputMap, nbioRequestBasedTransferCode, userInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> createValidationBioRequestBasedTransfer(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final var objMapper = new ObjectMapper();

		final String sQuery = " lock  table lockbiorequestbasedtransfer "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final var nbiorequestbasedtransfercode = (int) inputMap.get("nbiorequestbasedtransfercode");
		final var findStatus = findStatusRequestBasedtTransfer(nbiorequestbasedtransfercode, userInfo);

		final List<Object> savedBioDirectTransferList = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

			final var strBioDirectTransferDetailsCode = getToBeDisposeSamples(nbiorequestbasedtransfercode, userInfo);

			final var concatCondition = (strBioDirectTransferDetailsCode != null
					&& strBioDirectTransferDetailsCode != "")
							? " and nbiorequestbasedtransferdetailcode not in (" + strBioDirectTransferDetailsCode + ")"
							: "";

			final var objBioRequestBasedTransfer = objMapper.convertValue(inputMap.get("bioRequestBasedTransfer"),
					BioRequestBasedTransfer.class);

			final var sdeliveryDate = (objBioRequestBasedTransfer.getSdeliverydate() != null
					&& !objBioRequestBasedTransfer.getSdeliverydate().isEmpty())
							? "'" + objBioRequestBasedTransfer.getSdeliverydate().toString().replace("T", " ")
									.replace("Z", "") + "'"
							: null;

			final var strUpdateDirectTransfer = "update biorequestbasedtransfer set nstorageconditioncode="
					+ objBioRequestBasedTransfer.getNstorageconditioncode() + ", ddeliverydate=" + sdeliveryDate
					+ ", ndispatchercode=" + objBioRequestBasedTransfer.getNdispatchercode() + ", ncouriercode="
					+ objBioRequestBasedTransfer.getNcouriercode() + ", ntransactionstatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()
					+ ", jsondata=jsondata || '{\"sdispatchername\": \""
					+ objBioRequestBasedTransfer.getSdispatchername() + "\", " + "\"scouriername\": \""
					+ objBioRequestBasedTransfer.getScouriername() + "\", \"scourierno\": \""
					+ stringUtilityFunction.replaceQuote(objBioRequestBasedTransfer.getScourierno())
					+ "\", \"sstorageconditionname\": \"" + objBioRequestBasedTransfer.getSstorageconditionname()
					+ "\", \"striplepackage\": \""
					+ stringUtilityFunction.replaceQuote(objBioRequestBasedTransfer.getStriplepackage()) + "\","
					+ " \"svalidationremarks\": \""
					+ stringUtilityFunction.replaceQuote(objBioRequestBasedTransfer.getSvalidationremarks()) + "\"}'"
					+ " where nbiorequestbasedtransfercode=" + nbiorequestbasedtransfercode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			final var strUpdateDirectTransferDetails = "update biorequestbasedtransferdetails set ntransferstatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()
					+ " where nbiorequestbasedtransfercode=" + nbiorequestbasedtransfercode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + concatCondition + ";";

			var seqNoBioRequetBasedTransferHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='biorequestbasedtransferhistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);
			seqNoBioRequetBasedTransferHistory++;

			var strInsertRequestTransferHistory = "INSERT INTO public.biorequestbasedtransferhistory("
					+ " nbiorequestbasedtransferhistorycode, nbiorequestbasedtransfercode, sformnumber, "
					+ " ntransfertypecode, ntransactionstatus, dtransactiondate, ntztransactiondate, "
					+ " noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus)"
					+ " SELECT " + seqNoBioRequetBasedTransferHistory + ", " + nbiorequestbasedtransfercode
					+ ", sformnumber,  ntransfertypecode ,"
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", '"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " FROM biorequestbasedtransfer WHERE nbiorequestbasedtransfercode = "
					+ nbiorequestbasedtransfercode + ";";

			final StringBuilder strSeqNoUpdate = new StringBuilder();

			strSeqNoUpdate.append("update seqnobiobankmanagement set nsequenceno=")
					.append(seqNoBioRequetBasedTransferHistory).append(" where")
					.append(" stablename='biorequestbasedtransferhistory' and nstatus=")
					.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");

			jdbcTemplate.execute(strUpdateDirectTransfer + strUpdateDirectTransferDetails
					+ strInsertRequestTransferHistory + strSeqNoUpdate);

			final var strAuditQry = auditQueryCourier(nbiorequestbasedtransfercode, "", userInfo);
			final List<BioRequestBasedTransfer> lstAuditAfter = jdbcTemplate.query(strAuditQry,
					new BioRequestBasedTransfer());

			lstAuditAfter.stream().forEach(x -> multilingualIDList.add("IDS_VALIDATETRANSFERFORM"));
			savedBioDirectTransferList.addAll(lstAuditAfter);

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);
			return getActiveBioRequestBasedTransfer(inputMap, nbiorequestbasedtransfercode, userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> deleteChildRequestBasedTransfer(final int nBioRequestBasedTransferCode,
			final String nBioRequestBasedTransferDetailsCode, final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		new ArrayList<>();
		new ArrayList<>();

		final var findStatus = findStatusRequestBasedtTransfer(nBioRequestBasedTransferCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			// final var strAuditBeforeQry = childAuditQuery(nBioRequestBasedTransferCode,
			// nBioRequestBasedTransferDetailsCode, userInfo);
			// final List<BioRequestBasedTransferDetails> lstAuditTransferDetailsBefore =
			// jdbcTemplate
			// .query(strAuditBeforeQry, new BioRequestBasedTransferDetails());
			final var strAuditQry = auditQuery(nBioRequestBasedTransferCode, nBioRequestBasedTransferDetailsCode,
					userInfo);
			final List<BioRequestBasedTransfer> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditQry,
					new BioRequestBasedTransfer());

			final var strDeleteQry = "update biorequestbasedtransferdetails set nstatus="
					+ Enumeration.TransactionStatus.NA.gettransactionstatus()
					+ " where nbiorequestbasedtransferdetailcode in (" + nBioRequestBasedTransferDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			final int seqNoBioRequestTransferDetailsHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='bioreqbasedtransdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			var strInsertRequestTransferDetailsHistory = "INSERT INTO bioreqbasedtransdetailshistory("
					+ "nbioreqbasedtransdetailshistorycode, " + "nbiorequestbasedtransferdetailcode, "
					+ "nbiorequestbasedtransfercode, " + "nsamplecondition, " + "ntransferstatus, "
					+ "dtransactiondate, " + "ntztransactiondate, " + "noffsetdtransactiondate, " + "nusercode, "
					+ "nuserrolecode, " + "ndeputyusercode, " + "ndeputyuserrolecode, " + "nsitecode, " + "nstatus) "
					+ "SELECT " + seqNoBioRequestTransferDetailsHistory
					+ " + rank() over(order by b.nbiorequestbasedtransferdetailcode), "
					+ "b.nbiorequestbasedtransferdetailcode, " + "b.nbiorequestbasedtransfercode, "
					+ "b.nsamplecondition, b.ntransferstatus, '" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "FROM biorequestbasedtransferdetails b " + "WHERE b.nbiorequestbasedtransfercode = "
					+ nBioRequestBasedTransferCode + " AND b.nbiorequestbasedtransferdetailcode IN ("
					+ nBioRequestBasedTransferDetailsCode + " ) ;";

			strInsertRequestTransferDetailsHistory += "UPDATE seqnobiobankmanagement SET " + "nsequenceno = (SELECT "
					+ seqNoBioRequestTransferDetailsHistory + " + COUNT(nbiorequestbasedtransferdetailcode) "
					+ "FROM biorequestbasedtransferdetails " + "WHERE nbiorequestbasedtransfercode = "
					+ nBioRequestBasedTransferCode + " AND nbiorequestbasedtransferdetailcode IN ("
					+ nBioRequestBasedTransferDetailsCode + ")) "
					+ "WHERE stablename='bioreqbasedtransdetailshistory' AND nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strDeleteQry + strInsertRequestTransferDetailsHistory);

			final BioRequestBasedTransfer objBioRequestBasedTransfer = null;

			if (objBioRequestBasedTransfer == null) {
				final List<Object> lstAuditAfter = new ArrayList<>();
				final List<String> multilingualIDList = new ArrayList<>();

				lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
				lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_DELETETRANSFERSAMPLES"));
				auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);
			}

			final var lstChildBioRequestbasedTransfer = getChildInitialGet(nBioRequestBasedTransferCode, userInfo);
			outputMap.put("lstChildBioRequestbasedTransfer", lstChildBioRequestbasedTransfer);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> disposeSamples(final int nBioRequestBasedTransferCode,
			final String nBioRequestBasedTransferDetailCode, final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var findStatus = findStatusRequestBasedtTransfer(nBioRequestBasedTransferCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {
			
			final var strDeleteQry = "update biorequestbasedtransferdetails set ntransferstatus="
					+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus()
					+ " where nbiorequestbasedtransferdetailcode in (" + nBioRequestBasedTransferDetailCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+";";

			final int seqNoBioRequestTransferDetailsHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='bioreqbasedtransdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			var strInsertRequestTransferDetailsHistory = "INSERT INTO bioreqbasedtransdetailshistory("
					+ "nbioreqbasedtransdetailshistorycode, " + "nbiorequestbasedtransferdetailcode, "
					+ "nbiorequestbasedtransfercode, " + "nsamplecondition, " + "ntransferstatus, "
					+ "dtransactiondate, " + "ntztransactiondate, " + "noffsetdtransactiondate, " + "nusercode, "
					+ "nuserrolecode, " + "ndeputyusercode, " + "ndeputyuserrolecode, " + "nsitecode, " + "nstatus) "
					+ "SELECT " + seqNoBioRequestTransferDetailsHistory
					+ " + rank() over(order by b.nbiorequestbasedtransferdetailcode), "
					+ "b.nbiorequestbasedtransferdetailcode, " + "b.nbiorequestbasedtransfercode, "
					+ "b.nsamplecondition, b.ntransferstatus, '" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "FROM biorequestbasedtransferdetails b " + "WHERE b.nbiorequestbasedtransfercode = "
					+ nBioRequestBasedTransferCode + " AND b.nbiorequestbasedtransferdetailcode IN ("
					+ nBioRequestBasedTransferDetailCode + " ) AND b.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";

			strInsertRequestTransferDetailsHistory += "UPDATE seqnobiobankmanagement SET nsequenceno = (SELECT "
					+ seqNoBioRequestTransferDetailsHistory + " + COUNT(nbiorequestbasedtransferdetailcode) "
					+ "FROM biorequestbasedtransferdetails " + "WHERE nbiorequestbasedtransfercode = "
					+ nBioRequestBasedTransferCode + " AND nbiorequestbasedtransferdetailcode IN ("
					+ nBioRequestBasedTransferDetailCode + ")) "
					+ "WHERE stablename='bioreqbasedtransdetailshistory' AND nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strDeleteQry + strInsertRequestTransferDetailsHistory);

			final List<Object> lstAuditAfter = new ArrayList<>();
			final List<String> multilingualIDList = new ArrayList<>();
			final var strAuditQry = auditQuery(nBioRequestBasedTransferCode, nBioRequestBasedTransferDetailCode,
					userInfo);
			final List<BioRequestBasedTransfer> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditQry,
					new BioRequestBasedTransfer());
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_DISPOSETRANSFERSAMPLES"));
			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 1, null, multilingualIDList, userInfo);

			final var lstChildBioRequestbasedTransfer = getChildInitialGet(nBioRequestBasedTransferCode, userInfo);
			outputMap.put("lstChildBioRequestbasedTransfer", lstChildBioRequestbasedTransfer);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public int findStatusRequestBasedtTransfer(final int nbioRequestBasedCode, final UserInfo userInfo)
			throws Exception {

		final var strStatusRequestbasedTransfer = "select ntransactionstatus from biorequestbasedtransfer where nbiorequestbasedtransfercode="
				+ nbioRequestBasedCode + " and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		return (int) Objects.requireNonNullElse(
				jdbcTemplateUtilityFunction.queryForObject(strStatusRequestbasedTransfer, Integer.class, jdbcTemplate),
				-1);
		// return (int)
		// jdbcTemplateUtilityFunction.queryForObject(strStatusDirectTransfer,
		// Integer.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> getActiveBioRequestBasedTransfer(final Map<String, Object> inputMap,
			final int nBioRequestBasedTransferCode, final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var strQuery = "select dt.nbiorequestbasedtransfercode, dt.sformnumber, dt.ntransfertypecode,"
				+ " coalesce(tt.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ " tt.jsondata->'sdisplayname'->>'en-US') stransfertypename,  dt.nreceiversitecode,"
				+ " dt.jsondata->>'sreceiversitename' sreceiversitename, dt.dtransferdate,"
				+ " to_char(dt.dtransferdate,'" + userInfo.getSsitedate()
				+ "')  stransferdate, dt.ntztransferdate, dt.noffsetdtransferdate,"
				+ " dt.ntransactionstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, dt.jsondata->>'sremarks' sremarks, dt.nsitecode,"
				+ " dt.nstatus, cm.scolorhexcode,  dt.jsondata->>'sstorageconditionname' sstorageconditionname ,to_char(dt.ddeliverydate, '"
				+ userInfo.getSsitedate() + "') sdeliverydate, " + " dt.jsondata->>'sdispatchername' sdispatchername,"
				+ " dt.jsondata->>'scouriername' scouriername, dt.jsondata->>'scourierno' scourierno,"
				+ " dt.jsondata->>'striplepackage' striplepackage, dt.jsondata->>'svalidationremarks' svalidationremarks from biorequestbasedtransfer dt join transfertype tt on"
				+ " dt.ntransfertypecode=tt.ntransfertypecode and tt.nsitecode=" + userInfo.getNmastersitecode()
				+ " and tt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join transactionstatus ts on dt.ntransactionstatus=ts.ntranscode and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join formwisestatuscolor fwsc on fwsc.ntranscode=ts.ntranscode and fwsc.nsitecode="
				+ userInfo.getNmastersitecode() + " and fwsc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and fwsc.nformcode="
				+ userInfo.getNformcode() + " join colormaster cm on cm.ncolorcode=fwsc.ncolorcode and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where dt.nsitecode="
				+ userInfo.getNtranssitecode() + " and dt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and dt.nbiorequestbasedtransfercode="
				+ nBioRequestBasedTransferCode + " order by dt.nbiorequestbasedtransfercode desc";

		final var objBioRequestBasedTransferCode = (BioRequestBasedTransfer) jdbcTemplateUtilityFunction
				.queryForObject(strQuery, BioRequestBasedTransfer.class, jdbcTemplate);

		if (objBioRequestBasedTransferCode != null) {
			outputMap.put("selectedBioRequestBasedTransfer", objBioRequestBasedTransferCode);
			final var lstChildBioRequestbasedTransfer = getChildInitialGet(
					objBioRequestBasedTransferCode.getNbiorequestbasedtransfercode(), userInfo);
			outputMap.put("lstChildBioRequestbasedTransfer", lstChildBioRequestbasedTransfer);

		} else {
			outputMap.put("selectedBioRequestBasedTransfer", null);
			outputMap.put("lstChildBioRequestbasedTransfer", null);

		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getActiveBioRequestBasedTransferById(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		final var nbioRequestBasedTransferCode = (int) inputMap.get("nbiorequestbasedtransfercode");

		final Map<String, Object> outputMap = new HashMap<>();
		final var strChildRequestBasedTransfer = "SELECT nbiorequestbasedtransfercode, sformnumber, ntransfertypecode,ntztransferdate, "
				+ "ntransactionstatus, bdt.jsondata->>'sremarks' sremarks,coalesce(to_char(bdt.dtransferdate, '"
				+ userInfo.getSsitedate() + "'), '-') stransferdate "
				+ " from biorequestbasedtransfer bdt where  bdt.nbiorequestbasedtransfercode="
				+ nbioRequestBasedTransferCode + "  and bdt.nsitecode=" + userInfo.getNtranssitecode()
				+ " and bdt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";
		final var selectedRecordRequestBasedTransfer = (BioRequestBasedTransfer) jdbcTemplateUtilityFunction
				.queryForObject(strChildRequestBasedTransfer, BioRequestBasedTransfer.class, jdbcTemplate);

		final var strTransferType = " Select ntransfertypecode,coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') as stransfertypename from transfertype  where ntransfertypecode>0  and ntransfertypecode="
				+ selectedRecordRequestBasedTransfer.getNtransfertypecode() + " and  nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ndefaultstatus asc ";

		final List<TransferType> lstObjTransferType = jdbcTemplate.query(strTransferType, new TransferType());

		final List<Map<String, Object>> lstTransferType = new ArrayList<>();

		lstObjTransferType.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getStransfertypename());
			mapStatus.put("value", lst.getNtransfertypecode());
			mapStatus.put("item", lst);
			lstTransferType.add(mapStatus);
		});

		outputMap.put("selectedTransferType", lstTransferType.getFirst());

		// inputMap.put("ntransfertypecode", lstTransferType);
		// outputMap.put("getTransferTypeRecord", getTransferTypeRecord(outputMap,
		// userInfo));

		outputMap.put("selectedRecordRequestBasedTransfer", selectedRecordRequestBasedTransfer);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getBioRequestBasedTransfer(final Map<String, Object> inputMap,
			final int nbioRequestBasedTransferCode, final UserInfo userInfo) throws Exception {
		LOGGER.info("getBioRequestBasedTransferss");
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		if (inputMap.containsKey("nbiorequestbasedtransfercode")) {
			outputMap.put("nbiorequestbasedtransfercode", inputMap.get("nbiorequestbasedtransfercode"));
		}

		var fromDate = (String) inputMap.get("fromDate");
		var toDate = (String) inputMap.get("toDate");
		var ntransCode = inputMap.containsKey("ntransCode") ? (int) inputMap.get("ntransCode") : -1;

		if (!inputMap.containsKey("fromDate")) {
			final var mapObject = projectDAOSupport.getDateFromControlProperties(userInfo,
					(String) inputMap.get("currentdate"), "datetime", "FromDate");
			fromDate = (String) mapObject.get("FromDate");
			toDate = (String) mapObject.get("ToDate");
			inputMap.put("fromDate", fromDate);
			inputMap.put("toDate", toDate);
			outputMap.put("fromDate", mapObject.get("FromDateWOUTC"));
			outputMap.put("toDate", mapObject.get("ToDateWOUTC"));
			outputMap.put("realFromDate", mapObject.get("FromDateWOUTC"));
			outputMap.put("realToDate", mapObject.get("ToDateWOUTC"));
		}

		final var getFilterStatus = getFilterStatus(userInfo);
		final List<Map<String, Object>> lstFilterStatus = new ArrayList<>();

		getFilterStatus.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getStransdisplaystatus());
			mapStatus.put("value", lst.getNtranscode());
			mapStatus.put("item", lst);
			lstFilterStatus.add(mapStatus);
		});
		var status = "";
		if (!inputMap.containsKey("ntransCode")) {
			ntransCode = getFilterStatus.get(0).getNtranscode();
		} else {
			ntransCode = (int) inputMap.get("ntransCode");
			if (ntransCode == 0) {

				status = getFilterStatus.stream().map(objtranscode -> String.valueOf(objtranscode.getNtranscode()))
						.collect(Collectors.joining(","));
			}

		}
		final var transCode = (short) ntransCode;
		final var selectedFilterStatus = lstFilterStatus.stream().filter(x -> (short) x.get("value") == transCode)
				.collect(Collectors.toList()).get(0);

		outputMap.put("lstFilterStatus", lstFilterStatus);
		outputMap.put("selectedFilterStatus", selectedFilterStatus);
		outputMap.put("realSelectedFilterStatus", selectedFilterStatus);

		final var transferaTypeQuery = "select ntransfertypecode,coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') as stransfertypename from transfertype where ntransfertypecode>0  order by ndefaultstatus asc ";

		final List<TransferType> lstTransferaType = jdbcTemplate.query(transferaTypeQuery, new TransferType());

		final List<Map<String, Object>> lstFilterTransferType = new ArrayList<>();

		lstTransferaType.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getStransfertypename());
			mapStatus.put("value", lst.getNtransfertypecode());
			mapStatus.put("item", lst);
			lstFilterTransferType.add(mapStatus);
		});

		var nTransferType = -1;

		if (!inputMap.containsKey("ntransfertypecode")) {
			nTransferType = lstTransferaType.getFirst().getNtransfertypecode();
		} else {
			nTransferType = (int) inputMap.get("ntransfertypecode");
		}

		final var transferTypeCode = nTransferType;
		final var selectedTransferType = lstFilterTransferType.stream()
				.filter(x -> ((Number) x.get("value")).intValue() == transferTypeCode).collect(Collectors.toList())
				.get(0);

		outputMap.put("lstTransferType", lstFilterTransferType);
		outputMap.put("selectedTransferType", selectedTransferType);
		outputMap.put("realselectedTransferType", selectedTransferType);

		final var strConditionTransCode = ntransCode < -1 ? ""
				: ntransCode == 0 ? " and rbt.ntransactionstatus in (" + status + ") "
						: " and rbt.ntransactionstatus in (" + ntransCode + ")";

		final var strConditionTransferTypeCode = nTransferType < 0 ? ""
				: " and rbt.ntransfertypecode=" + nTransferType + " ";

		final var strQuery = "select rbt.nbiorequestbasedtransfercode,rbt.nthirdpartycode, rbt.sformnumber, rbt.ntransfertypecode,"
				+ " tt.stransfertypename,  rbt.nreceiversitecode, rbt.jsondata->>'sreceiversitename' sreceiversitename, rbt.dtransferdate,"
				+ " to_char(rbt.dtransferdate,'" + userInfo.getSsitedate()
				+ "')  stransferdate, rbt.ntztransferdate, rbt.noffsetdtransferdate,"
				+ " rbt.ntransactionstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, cm.scolorhexcode, rbt.jsondata->>'sremarks' sremarks,"
				+ " rbt.nsitecode, rbt.nstatus, rbt.jsondata->>'sstorageconditionname' sstorageconditionname, to_char(rbt.ddeliverydate, '"
				+ userInfo.getSsitedate()
				+ "') sdeliverydate, rbt.jsondata->>'sdispatchername' sdispatchername,  rbt.jsondata->>'scouriername' scouriername,"
				+ " rbt.jsondata->>'scourierno' scourierno,  rbt.jsondata->>'striplepackage' striplepackage, rbt.jsondata->>'svalidationremarks' svalidationremarks "
				+ "from biorequestbasedtransfer rbt join transfertype tt on"
				+ " rbt.ntransfertypecode=tt.ntransfertypecode and tt.nsitecode=" + userInfo.getNmastersitecode()
				+ " and tt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join transactionstatus ts on rbt.ntransactionstatus=ts.ntranscode and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join formwisestatuscolor fwsc on fwsc.ntranscode=ts.ntranscode and fwsc.nformcode="
				+ userInfo.getNformcode() + " and fwsc.nsitecode=" + userInfo.getNmastersitecode()
				+ " and fwsc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " join colormaster cm on fwsc.ncolorcode=cm.ncolorcode and cm.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where rbt.nsitecode="
				+ userInfo.getNtranssitecode() + " and rbt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and rbt.dtransactiondate between '"
				+ fromDate + "' and '" + toDate + "'" + strConditionTransCode + " " + strConditionTransferTypeCode
				+ "  order by rbt.nbiorequestbasedtransfercode desc";
		final List<BioRequestBasedTransfer> lstBioRequestBasedTransfer = jdbcTemplate.query(strQuery,
				new BioRequestBasedTransfer());

		if (!lstBioRequestBasedTransfer.isEmpty()) {
			outputMap.put("lstBioRequestBasedTransfer", lstBioRequestBasedTransfer);
			List<BioRequestBasedTransfer> lstObjBioDirectTransfer = null;
			if (nbioRequestBasedTransferCode == -1) {
				lstObjBioDirectTransfer = lstBioRequestBasedTransfer;
			} else {
				lstObjBioDirectTransfer = lstBioRequestBasedTransfer.stream()
						.filter(x -> x.getNbiorequestbasedtransfercode() == nbioRequestBasedTransferCode)
						.collect(Collectors.toList());
			}
			outputMap.put("selectedBioRequestBasedTransfer", lstObjBioDirectTransfer.get(0));

			final var lstChildBioRequestbasedTransfer = getChildInitialGet(
					lstObjBioDirectTransfer.get(0).getNbiorequestbasedtransfercode(), userInfo);
			outputMap.put("lstChildBioRequestbasedTransfer", lstChildBioRequestbasedTransfer);

			if (inputMap.containsKey(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())) {
				outputMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						inputMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
			}
		} else {
			// outputMap.put("selectedBioRequestBasedTransfer", null);
			outputMap.put("lstBioRequestBasedTransfer", new ArrayList<>());
			outputMap.put("selectedBioRequestBasedTransfer", new ArrayList<>());
			outputMap.put("lstChildBioRequestbasedTransfer", new ArrayList<>());

		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public List<Map<String, Object>> getChildInitialGet(final int nbiorequestbasedtransfercode, final UserInfo userInfo)
			throws Exception {
		// final var strChildGet = "select row_number() over(order by
		// bdtd.nbiorequestbasedtransferdetailcode desc) as"
		// + " nserialno, bdtd.nbiorequestbasedtransferdetailcode,
		// bdtd.nbiorequestbasedtransfercode,"
		// + " bpsr.sparentsamplecode, bsr.srepositoryid, bsr.slocationcode,
		// p.sproductname, bsr.svolume,"
		// + " coalesce(ts2.jsondata->'stransdisplaystatus'->>'" +
		// userInfo.getSlanguagetypecode() + "',"
		// + " ts2.jsondata->'stransdisplaystatus'->>'en-US') ssamplecondition,"
		// + " coalesce(ts3.jsondata->'stransdisplaystatus'->>'" +
		// userInfo.getSlanguagetypecode() + "',"
		// + " ts3.jsondata->'stransdisplaystatus'->>'en-US') stransferstatus,
		// r.sreason,"
		// + " coalesce(ts1.jsondata->'stransdisplaystatus'->>'" +
		// userInfo.getSlanguagetypecode()
		// + "', ts1.jsondata->'stransdisplaystatus'->>'en-US')"
		// + " ssamplestatus from biosamplereceiving bsr join biosamplereceivinghistory
		// bsrh on"
		// + " bsr.nbiosamplereceivingcode=bsrh.nbiosamplereceivingcode and
		// bsrh.nstatus="
		// + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and
		// bsrh.nsitecode="
		// + userInfo.getNtranssitecode()
		// + " and bsrh.nbiosamplereceivinghistorycode in (select
		// max(nbiosamplereceivinghistorycode) from"
		// + " biosamplereceivinghistory where nsitecode=" +
		// userInfo.getNtranssitecode() + " and nstatus="
		// + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " group by
		// nbiosamplereceivingcode)"
		// + " join transactionstatus ts1 on ts1.ntranscode=bsrh.ntransactionstatus and
		// ts1.nstatus="
		// + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		// + " join biorequestbasedtransferdetail bdtd on
		// bdtd.nbiosamplereceivingcode=bsr.nbiosamplereceivingcode"
		// + " and bdtd.nsitecode=" + userInfo.getNtranssitecode() + " and
		// bdtd.nstatus="
		// + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and
		// bdtd.ntransferstatus!="
		// + Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()
		// + " and bdtd.nbiorequestbasedtransfercode=" + nbiorequestbasedtransfercode
		// + " join transactionstatus ts2 on" + " bdtd.nsamplecondition=ts2.ntranscode
		// and ts2.nstatus="
		// + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join
		// transactionstatus ts3 on"
		// + " bdtd.ntransferstatus=ts3.ntranscode and ts3.nstatus="
		// + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " join
		// bioparentsamplereceiving bpsr on"
		// + " bpsr.nbioparentsamplecode=bdtd.nbioparentsamplecode and bpsr.nsitecode="
		// + userInfo.getNtranssitecode() + " and bpsr.nstatus="
		// + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		// + " join product p on p.nproductcode=bsr.nproductcode and p.nsitecode=" +
		// userInfo.getNmastersitecode()
		// + " and p.nstatus=" +
		// Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
		// + " left join reason r on r.nreasoncode=bdtd.nreasoncode and r.nsitecode="
		// + userInfo.getNmastersitecode() + " and r.nstatus="
		// + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by
		// nserialno desc";

		final var strChildGet = "select   row_number() over(order by bdtd.nbiorequestbasedtransferdetailcode desc) as nserialno, bdtd.nbiorequestbasedtransferdetailcode,  "
				+ "  bdtd.nbiorequestbasedtransfercode,(bdtd.jsondata ->> 'sparentsamplecode') || ' | ' || bdtd.ncohortno as  sparentsamplecode, bdtd.srepositoryid, bdtd.slocationcode, bdtd.svolume,coalesce( ts2.jsondata -> 'stransdisplaystatus' ->> '"
				+ userInfo.getSlanguagetypecode() + "',  "
				+ "    ts2.jsondata -> 'stransdisplaystatus' ->> 'en-US') as ssamplecondition, coalesce( ts3.jsondata -> 'stransdisplaystatus' ->> '"
				+ userInfo.getSlanguagetypecode() + "',  "
				+ "    ts3.jsondata -> 'stransdisplaystatus' ->> 'en-US') as stransferstatus, r.sreason,p.sproductname|| ' ( ' || pc.sproductcatname || ')' as sproductname ,  s.ssitename as originsite,bec.sformnumber "
				+ "from biorequestbasedtransferdetails bdtd  join bioecataloguereqapproval bec on bec.necatrequestreqapprovalcode= bdtd.necatrequestreqapprovalcode "
				+ "  and bec.nsitecode = bdtd.nsitecode " + "  and bec.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "join biorequestbasedtransfer brt "
				+ "on bdtd.nbiorequestbasedtransfercode = brt.nbiorequestbasedtransfercode  "
				+ "and bdtd.nsitecode = brt.nsitecode and brt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ "  join site s on brt.nreceiversitecode = s.nsitecode and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " join transactionstatus ts2  on bdtd.nsamplecondition = ts2.ntranscode  and ts2.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  "
				+ "join transactionstatus ts3  on bdtd.ntransferstatus = ts3.ntranscode  and ts3.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " left join reason r  on bdtd.nreasoncode = r.nreasoncode "
				+ "  join product p on p.nproductcode = bdtd.nproductcode  and p.nsitecode ="
				+ userInfo.getNmastersitecode() + " and p.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "  join productcategory pc on  pc.nproductcatcode = p.nproductcatcode  and pc.nsitecode ="
				+ userInfo.getNmastersitecode() + " and pc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and bdtd.nsitecode = "
				+ userInfo.getNtranssitecode() + " and bdtd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and bdtd.ntransferstatus != "
				+ Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()
				+ "  and bdtd.nbiorequestbasedtransfercode = " + nbiorequestbasedtransfercode
				+ "  order by nserialno desc ";

		final var lstChildGet = jdbcTemplate.queryForList(strChildGet);

		return lstChildGet;
	}

	@Override
	public ResponseEntity<Object> getChildRequestBasedRecord(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();

		final var nBioRequestBasedTransferCode = (int) inputMap.get("nbiorequestbasedtransfercode");

		final var strQuery = "select brf.ntransfertypecode,t.sthirdpartyname,t.nthirdpartycode, "
				+ "s.nsitecode,s.ssitename from biorequestbasedtransfer brf ,thirdparty t,site s "
				+ "where brf.nthirdpartycode=t.nthirdpartycode "
				+ "and brf.nreceiversitecode=s.nsitecode  and brf.nbiorequestbasedtransfercode="
				+ nBioRequestBasedTransferCode + " and t.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and brf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "   and s.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc";

		final var lstRequestBasedTransferCode = jdbcTemplate.queryForList(strQuery);

		final var ntransfertypecode = Integer
				.parseInt(lstRequestBasedTransferCode.getFirst().get("ntransfertypecode").toString());

		final List<Map<String, Object>> selectedBioReuestBased = new ArrayList<>();

		if (ntransfertypecode == Enumeration.TransferType.BIOBANK.getntransfertype()) {

			lstRequestBasedTransferCode.stream().forEach(lst -> {
				final Map<String, Object> mapStatus = new HashMap<>();
				mapStatus.put("label", lst.get("ssitename"));
				mapStatus.put("value", lst.get("nsitecode"));
				mapStatus.put("item", lst);
				selectedBioReuestBased.add(mapStatus);
			});

			inputMap.put("originsiteorthirdparty",
					Integer.parseInt(lstRequestBasedTransferCode.getFirst().get("nsitecode").toString()));

		} else if (ntransfertypecode == Enumeration.TransferType.THIRDPARTY.getntransfertype()) {

			lstRequestBasedTransferCode.stream().forEach(lst -> {
				final Map<String, Object> mapStatus = new HashMap<>();
				mapStatus.put("label", lst.get("sthirdpartyname"));
				mapStatus.put("value", lst.get("nthirdpartycode"));
				mapStatus.put("item", lst);
				selectedBioReuestBased.add(mapStatus);
			});

			inputMap.put("originsiteorthirdparty",
					Integer.parseInt(lstRequestBasedTransferCode.getFirst().get("nthirdpartycode").toString()));

		} else if (ntransfertypecode == Enumeration.TransferType.BIOBANKEXTERNAL.getntransfertype()) {

			lstRequestBasedTransferCode.stream().forEach(lst -> {
				final Map<String, Object> mapStatus = new HashMap<>();
				mapStatus.put("label", lst.get("sthirdpartyname"));
				mapStatus.put("value", lst.get("nthirdpartycode"));
				mapStatus.put("item", lst);
				selectedBioReuestBased.add(mapStatus);
			});

			inputMap.put("originsiteorthirdparty",
					Integer.parseInt(lstRequestBasedTransferCode.getFirst().get("nthirdpartycode").toString()));

		}

		inputMap.put("ntransfertypecode", ntransfertypecode);

		outputMap.put("lstRequestFormNo", getTransferTypeBasedFormNo(inputMap, userInfo));

		outputMap.put("selectedOrignSiteorThirdParty", selectedBioReuestBased);

		outputMap.put("ntransfertypecode", ntransfertypecode);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getCourier(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final var strCourier = "select ncouriercode, scouriername from courier where nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ncouriercode > 0 order by ncouriercode";
		final var lstGetCourier = jdbcTemplate.queryForList(strCourier);

		final List<Map<String, Object>> lstCourier = new ArrayList<>();
		lstGetCourier.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("scouriername"));
			mapStatus.put("value", lst.get("ncouriercode"));
			mapStatus.put("item", lst);
			lstCourier.add(mapStatus);
		});

		outputMap.put("lstCourier", lstCourier);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public List<TransactionStatus> getFilterStatus(final UserInfo userInfo) throws Exception {

		final var strFilterStatus = "select ts.ntranscode, ts.stransstatus, coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') stransdisplaystatus, ts.nstatus from transactionstatus ts,"
				+ " approvalstatusconfig ascf where " + " ts.ntranscode=ascf.ntranscode and ascf.nformcode="
				+ userInfo.getNformcode() + " and ascf.nstatusfunctioncode="
				+ Enumeration.ApprovalStatusFunction.FILTERSTATUS.getNstatustype() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ascf.nsitecode="
				+ userInfo.getNmastersitecode() + " order by ascf.nsorter";
		return jdbcTemplate.query(strFilterStatus, new TransactionStatus());
	}

	@Override
	public ResponseEntity<Object> getProjectBasedOnSample(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var necatrequestreqapprovalcode = (int) inputMap.get("necatrequestreqapprovalcode");

		final var nbioprojectcode = (int) inputMap.get("nbioprojectcode");

		final var strBioProject = "select p.sproductname || ' ( ' || pc.sproductcatname || ')'  as sproductname,p.nproductcode from bioecataloguerequestdetails rc,product p "
				+ " join productcategory pc on pc.nproductcatcode = p.nproductcatcode " + "  and pc.nsitecode ="
				+ userInfo.getNmastersitecode() + " " + "  and pc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " where rc.nproductcode =p.nproductcode and rc.nbioprojectcode=" + nbioprojectcode + " "
				+ "and rc.necatrequestreqapprovalcode=" + necatrequestreqapprovalcode + " " + "and rc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  " + "and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and rc.nsitecode="
				+ userInfo.getNtranssitecode() + " " + "and p.nsitecode=" + userInfo.getNmastersitecode() + " "
				+ "AND (SELECT SUM(naccnoofsamples)  FROM bioecataloguerequestdetails berd "
				+ "WHERE  berd.nproductcode = rc.nproductcode  AND berd.necatrequestreqapprovalcode = "
				+ necatrequestreqapprovalcode + " " + "AND berd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nbioprojectcode = "
				+ nbioprojectcode + ") > ( SELECT  COUNT(*) FROM  biorequestbasedtransferdetails brd "
				+ "WHERE brd.nproductcode = rc.nproductcode AND brd.necatrequestreqapprovalcode = "
				+ necatrequestreqapprovalcode + " " + "AND brd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nbioprojectcode = "
				+ nbioprojectcode + " AND brd.ntransferstatus not in ("
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ") )  "
				+ "group by p.sproductname,p.nproductcode,pc.sproductcatname ; ";

		final List<Product> lstGetProduct = jdbcTemplate.query(strBioProject, new Product());

		final List<Map<String, Object>> lstProduct = new ArrayList<>();

		if (!lstGetProduct.isEmpty()) {
			lstGetProduct.stream().forEach(lst -> {
				final Map<String, Object> mapStatus = new HashMap<>();
				mapStatus.put("label", lst.getSproductname());
				mapStatus.put("value", lst.getNproductcode());
				mapStatus.put("item", lst);
				lstProduct.add(mapStatus);
			});

			outputMap.put("lstProduct", lstProduct);
		} else {
			outputMap.put("lstProduct", null);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getProjectBasedOnSite(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var necatrequestreqapprovalcode = (int) inputMap.get("necatrequestreqapprovalcode");
		final var decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
				? "."
				: userInfo.getSdecimaloperator();

		final var fetchSiteCode = "select norginsitecode,nreqformtypecode from bioecataloguereqapproval where necatrequestreqapprovalcode="
				+ necatrequestreqapprovalcode + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";

		final var selectedBioEcatalogueReqapproval = (BioRequestBasedTransfer) jdbcTemplateUtilityFunction
				.queryForObject(fetchSiteCode, BioRequestBasedTransfer.class, jdbcTemplate);

		final var objSiteCode = selectedBioEcatalogueReqapproval.getNorginsitecode();

		final var nReqFormTypeCode = selectedBioEcatalogueReqapproval.getNreqformtypecode();

		if (nReqFormTypeCode == Enumeration.RequestFormType.BIOSAMPLEBASED.getRequestFormType()) {

			outputMap.put("needParentSample", false);

		} else if (nReqFormTypeCode == Enumeration.RequestFormType.PARENTSAMPLEBASED.getRequestFormType()) {

			outputMap.put("needParentSample", true);

		}

		final var strBioProject = "select bp.sprojecttitle,bp.nbioprojectcode from bioecataloguereqapproval  ba "
				+ "join bioecataloguerequestdetails rc  "
				+ "on ba.necatrequestreqapprovalcode=rc.necatrequestreqapprovalcode " + "and ba. ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " " + "and rc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
				+ "bioproject bp where rc.nbioprojectcode=bp.nbioprojectcode and "
				// + " REPLACE(rc.saccminvolume COLLATE \"C\", '"+ decOperator + "',
				// '.')::numeric > 0 "
				+ "rc.naccnoofsamples ::numeric >0 and ba.norginsitecode=" + objSiteCode + " "
				+ "and rc.necatrequestreqapprovalcode=" + necatrequestreqapprovalcode + "  " + "and ba.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and ba.nsitecode="
				+ userInfo.getNtranssitecode() + " " + "and rc.nsitecode=" + userInfo.getNtranssitecode() + " "
				+ "  group by bp.sprojecttitle, bp.nbioprojectcode order by 1 desc";
		final List<BioProject> lstGetBioProject = jdbcTemplate.query(strBioProject, new BioProject());

		final List<Map<String, Object>> lstBioProject = new ArrayList<>();

		if (!lstGetBioProject.isEmpty()) {
			lstGetBioProject.stream().forEach(lst -> {
				final Map<String, Object> mapStatus = new HashMap<>();
				mapStatus.put("label", lst.getSprojecttitle());
				mapStatus.put("value", lst.getNbioprojectcode());
				mapStatus.put("item", lst);
				lstBioProject.add(mapStatus);
			});

			final var nBioProjectCode = lstGetBioProject.getFirst().getNbioprojectcode();
			final var selectedBioProject = lstBioProject.stream().filter(x -> (int) x.get("value") == nBioProjectCode)
					.collect(Collectors.toList()).get(0);

			inputMap.put("nbioprojectcode", nBioProjectCode);
			outputMap.put("lstSampleType", getProjectBasedOnSample(inputMap, userInfo));

			outputMap.put("selectedBioProject", selectedBioProject);
			outputMap.put("lstBioProject", lstBioProject);
		} else {
			outputMap.put("lstBioProject", null);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getReason(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final var strReason = "select nreasoncode, sreason from reason where nsitecode=" + userInfo.getNmastersitecode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by nreasoncode";
		final var lstGetReason = jdbcTemplate.queryForList(strReason);
		final List<Map<String, Object>> lstReason = new ArrayList<>();

		lstGetReason.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("sreason"));
			mapStatus.put("value", lst.get("nreasoncode"));
			mapStatus.put("item", lst);
			lstReason.add(mapStatus);
		});

		outputMap.put("lstReason", lstReason);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getRequestAcceptanceType(final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var strRequestAcceptanceType = "SELECT necatrequestreqapprovalcode, ntransfertype, norginsitecode, "
				+ "nthirdpartycode, nbioprojectcode, nproductcode, ntrasactionstatus, svolume, nnoofsamples, srequstformno "
				+ "	FROM bioecataloguereqapproval where" + " nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and nsitecode="
				+ userInfo.getNmastersitecode() + ";";

		final var requestAcceptanceType = jdbcTemplate.queryForList(strRequestAcceptanceType);

		final List<Map<String, Object>> lstRequestAcceptanceType = new ArrayList<>();

		requestAcceptanceType.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("srequstformno"));
			mapStatus.put("value", lst.get("necatrequestreqapprovalcode"));
			mapStatus.put("item", lst);
			lstRequestAcceptanceType.add(mapStatus);
		});

		outputMap.put("lstRequestAcceptanceType", lstRequestAcceptanceType);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getRequestBasedProjectSampleParentLoad(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
				? "."
				: userInfo.getSdecimaloperator();

		final var necatrequestreqapprovalcode = (int) inputMap.get("necatrequestreqapprovalcode");

		final var nbioprojectcode = (Integer) inputMap.get("nbioprojectcode");

		final var nproductcode = (Integer) inputMap.get("nproductcode");

		final var ntransferTypeCode = (Integer) inputMap.get("ntransfertypecode");

		final var parentSampleCodeStr = "select nreqformtypecode from bioecataloguereqapproval  where necatrequestreqapprovalcode="
				+ necatrequestreqapprovalcode + "  and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc";

		final Object nReqformTypeCodeObj = Objects.requireNonNullElse(
				jdbcTemplateUtilityFunction.queryForObject(parentSampleCodeStr, Integer.class, jdbcTemplate), -1);

		final int nReqformTypeCode = (Integer) nReqformTypeCodeObj;

		var strTransferType = "";

		final List<Map<String, Object>> lstParentSample = new ArrayList<>();

		final String availabilitySubquery = " LEFT JOIN ( SELECT DISTINCT d.sparentsamplecode AS spc "
				+ "   FROM biodirecttransferdetails d "
				+ "   JOIN biodirecttransfer t ON t.nbiodirecttransfercode = d.nbiodirecttransfercode "
				+ "   WHERE t.ntransactionstatus IN (" + Enumeration.TransactionStatus.SENT.gettransactionstatus()
				+ ", " + Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ") " + "     AND t.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "     AND d.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "     AND t.nsitecode = "
				+ userInfo.getNtranssitecode() + " " + "   UNION "
				+ "   SELECT DISTINCT (rbd.jsondata->>'sparentsamplecode') AS spc "
				+ "   FROM biorequestbasedtransferdetails rbd "
				+ "   JOIN biorequestbasedtransfer rbt ON rbt.nbiorequestbasedtransfercode = rbd.nbiorequestbasedtransfercode "
				+ "   WHERE rbt.ntransactionstatus IN (" + Enumeration.TransactionStatus.SENT.gettransactionstatus()
				+ ", " + Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + ") "
				+ "     AND rbt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "     AND rbd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ "     AND rbt.nsitecode = " + userInfo.getNtranssitecode() + " "
				+ " ) b ON b.spc = sst.sparentsamplecode ";

		if (nReqformTypeCode == Enumeration.RequestFormType.BIOSAMPLEBASED.getRequestFormType()) {

			final String strSubjectDetails = " join biosubjectdetails bsd on bsd.ssubjectid=sst.ssubjectid "
					+ " and bsd.nsitecode=" + userInfo.getNmastersitecode() + " and bsd.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ((ntransferTypeCode == Enumeration.TransferType.BIOBANK.getntransfertype()
							|| ntransferTypeCode == Enumeration.TransferType.BIOBANKEXTERNAL.getntransfertype())
									? " and bsd.nissampleaccesable="
									: " and bsd.nisthirdpartysharable=")
					+ Enumeration.TransactionStatus.YES.gettransactionstatus();
//
//			strTransferType = "select sst.sparentsamplecode as sparentsamplecode " + "from samplestoragetransaction sst"
//					+ strSubjectDetails + " where  sst.nprojecttypecode=" + nbioprojectcode + " "
//					+ "and sst.nproductcode=" + nproductcode + "  and sst.nstatus= "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and sst.nsitecode= "
//					+ userInfo.getNtranssitecode() + " " + "group by sst.sparentsamplecode;";
			strTransferType = "select sst.sparentsamplecode as sparentsamplecode, " + "  sst.ncohortno as ncohortno, "
					+ "  CASE WHEN b.spc IS NOT NULL THEN 'PARTIAL' ELSE 'FULL' END AS savailable, "
					+ "  CASE WHEN b.spc IS NOT NULL THEN 'Partial' ELSE 'Full' END AS scolor "
					+ " from samplestoragetransaction sst " + strSubjectDetails + availabilitySubquery
					+ " where  sst.nprojecttypecode=" + nbioprojectcode + " " + "and sst.nproductcode=" + nproductcode
					+ "  and sst.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and sst.nsitecode= " + userInfo.getNtranssitecode() + " "
					+ "group by sst.sparentsamplecode, sst.ncohortno, b.spc;";

			final var lstObjRequestFormNo = jdbcTemplate.queryForList(strTransferType);

			lstObjRequestFormNo.stream().forEach(lst -> {
				final Map<String, Object> mapStatus = new HashMap<>();
				mapStatus.put("label", lst.get("sparentsamplecode"));
				mapStatus.put("value", lst.get("sparentsamplecode"));
				mapStatus.put("item", lst);
				lstParentSample.add(mapStatus);
			});

			final var sAccminVolumeQry = "select saccminvolume from bioecataloguerequestdetails  where nbioprojectcode="
					+ nbioprojectcode + "  "
					// + "and REPLACE(saccminvolume COLLATE \"C\", '" + decOperator+ "',
					// '.')::numeric > 0 "
					+ "and nproductcode=" + nproductcode + " and necatrequestreqapprovalcode="
					+ necatrequestreqapprovalcode + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc";

			final var sAccminVolume = Objects.requireNonNullElse(
					jdbcTemplateUtilityFunction.queryForObject(sAccminVolumeQry, String.class, jdbcTemplate), "");

			final var noOfSampleQry = "SELECT COALESCE(( SELECT sum(naccnoofsamples) "
					+ "FROM bioecataloguerequestdetails  where nbioprojectcode = " + nbioprojectcode
					+ "  AND nproductcode = " + nproductcode + "  AND necatrequestreqapprovalcode = "
					+ necatrequestreqapprovalcode + "  AND nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND nsitecode = "
					+ userInfo.getNtranssitecode() + " " + " ), 0) AS naccnoofsamples ;";

			final var noOfSample = Objects.requireNonNullElse(
					jdbcTemplateUtilityFunction.queryForObject(noOfSampleQry, Integer.class, jdbcTemplate), 0);

			final var findRemainSample = "SELECT COALESCE(( SELECT count(*) "
					+ "FROM biorequestbasedtransferdetails  where  ntransferstatus not in ("
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus()
					+ ") AND necatrequestreqapprovalcode = " + necatrequestreqapprovalcode + " AND nproductcode="
					+ nproductcode + "   AND nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "  AND nsitecode = " + userInfo.getNtranssitecode() + " " + " ), 0) AS savedsamples ;";

			final var findSaveSampleInRequestApproval = Objects.requireNonNullElse(
					jdbcTemplateUtilityFunction.queryForObject(findRemainSample, Integer.class, jdbcTemplate), 0);

			outputMap.put("saccminvolume", sAccminVolume);

			outputMap.put("naccnoofsamples", noOfSample);

			outputMap.put("naccnoofsamplesremaining", findSaveSampleInRequestApproval);

			outputMap.put("needParentSample", false);

		} else if (nReqformTypeCode == Enumeration.RequestFormType.PARENTSAMPLEBASED.getRequestFormType()) {

			final String strSubjectDetails = " join bioparentsamplereceiving bpsr on"
					+ " bpsr.sparentsamplecode=sst.sparentsamplecode "
					// + "and bpsr.nsitecode=" + userInfo.getNtranssitecode()
					+ " and bpsr.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " join biosubjectdetails bsd on bsd.ssubjectid=bpsr.ssubjectid " + " and bsd.nsitecode="
					+ userInfo.getNmastersitecode() + " and bsd.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ((ntransferTypeCode == Enumeration.TransferType.BIOBANK.getntransfertype()
							|| ntransferTypeCode == Enumeration.TransferType.BIOBANKEXTERNAL.getntransfertype())
									? " and bsd.nissampleaccesable="
									: " and bsd.nisthirdpartysharable=")
					+ Enumeration.TransactionStatus.YES.gettransactionstatus();

//			strTransferType = "select sst.sparentsamplecode as sparentsamplecode "
//					+ "from bioecataloguerequestdetails sst " + strSubjectDetails + " where  sst.nbioprojectcode="
//					+ nbioprojectcode + " " + "and sst.nproductcode=" + nproductcode + "  and sst.nstatus= "
//					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and necatrequestreqapprovalcode="
//					+ necatrequestreqapprovalcode + " group by sst.sparentsamplecode; ";

			strTransferType = "select " + "  sst.sparentsamplecode as sparentsamplecode, "
//					+ "  sst.ncohortno as ncohortno, "
					+ "  CASE WHEN b.spc IS NOT NULL THEN 'PARTIAL' ELSE 'FULL' END AS savailable, "
					+ "  CASE WHEN b.spc IS NOT NULL THEN 'red' ELSE 'black' END AS scolor "
					+ "from bioecataloguerequestdetails sst " + strSubjectDetails + availabilitySubquery
					+ " where  sst.nbioprojectcode=" + nbioprojectcode + " " + "and sst.nproductcode=" + nproductcode
					+ "  and sst.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and necatrequestreqapprovalcode=" + necatrequestreqapprovalcode + " "
					+ "group by sst.sparentsamplecode, "
//					+ "sst.ncohortno, b.spc; ";
					+ " b.spc; ";

			final var lstObjRequestFormNo = jdbcTemplate.queryForList(strTransferType);

			lstObjRequestFormNo.stream().forEach(lst -> {
				final Map<String, Object> mapStatus = new HashMap<>();
				mapStatus.put("label", lst.get("sparentsamplecode"));
				mapStatus.put("value", lst.get("sparentsamplecode"));
				mapStatus.put("item", lst);
				lstParentSample.add(mapStatus);
			});

			outputMap.put("saccminvolume", new ArrayList<>());

			outputMap.put("naccnoofsamples", new ArrayList<>());

			outputMap.put("needParentSample", true);

		}

		outputMap.put("lstParentSample", lstParentSample);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getbioparentbasedsampleandvolume(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
				? "."
				: userInfo.getSdecimaloperator();

		final var necatrequestreqapprovalcode = (int) inputMap.get("necatrequestreqapprovalcode");

		final var nbioprojectcode = (Integer) inputMap.get("nbioprojectcode");

		final var nproductcode = (Integer) inputMap.get("nproductcode");

		final var sbioparentsample = inputMap.get("sbioparentsample");

		final var sAccminVolumeQry = "select saccminvolume from bioecataloguerequestdetails  where nbioprojectcode="
				+ nbioprojectcode + "  "
				// + "and REPLACE(saccminvolume COLLATE \"C\", '" + decOperator+ "',
				// '.')::numeric > 0"
				+ " and nproductcode=" + nproductcode + " and necatrequestreqapprovalcode="
				+ necatrequestreqapprovalcode + " and sparentsamplecode='" + sbioparentsample + "' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by 1 desc";

		final var sAccminVolume = Objects.requireNonNullElse(
				jdbcTemplateUtilityFunction.queryForObject(sAccminVolumeQry, String.class, jdbcTemplate), "");

		final var noOfSampleQry = "SELECT COALESCE(( SELECT naccnoofsamples "
				+ "FROM bioecataloguerequestdetails  where nbioprojectcode = " + nbioprojectcode
				+ "  AND nproductcode = " + nproductcode + "  AND necatrequestreqapprovalcode = "
				+ necatrequestreqapprovalcode + " AND sparentsamplecode='" + sbioparentsample + "'  AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND nsitecode = "
				+ userInfo.getNtranssitecode() + " " + " ), 0) AS naccnoofsamples ;";

		final var noOfSample = Objects.requireNonNullElse(
				jdbcTemplateUtilityFunction.queryForObject(noOfSampleQry, String.class, jdbcTemplate), "");

		final var findRemainSample = "SELECT COALESCE(( SELECT count(*) "
				+ "FROM biorequestbasedtransferdetails  where  ntransferstatus not in ("
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ") AND nbioprojectcode = "
				+ nbioprojectcode + "  AND nproductcode = " + nproductcode + " " + " AND necatrequestreqapprovalcode = "
				+ necatrequestreqapprovalcode + "  AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND nsitecode = "
				+ userInfo.getNtranssitecode() + " " + " ), 0) AS savedsamples ;";

		final var findSaveSampleInRequestApproval = Objects.requireNonNullElse(
				jdbcTemplateUtilityFunction.queryForObject(findRemainSample, Integer.class, jdbcTemplate), 0);

		outputMap.put("naccnoofsamplesremaining", findSaveSampleInRequestApproval);

		outputMap.put("saccminvolume", sAccminVolume);

		outputMap.put("naccnoofsamples", noOfSample);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getSampleConditionStatus(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final var strSampleConditionStatus = "select ts.ntranscode nsamplecondition,"
				+ " coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "', ts.jsondata->'stransdisplaystatus'->>'en-US') ssamplecondition from"
				+ " approvalstatusconfig ascf, transactionstatus ts where ascf.ntranscode=ts.ntranscode"
				+ " and ascf.nstatusfunctioncode="
				+ Enumeration.ApprovalStatusFunction.VALIDATIONSTATUS.getNstatustype() + " and ascf.nformcode="
				+ userInfo.getNformcode() + " and ascf.nsitecode=" + userInfo.getNmastersitecode()
				+ " and ascf.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ts.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " order by ascf.nsorter";
		final var lstGetSampleCondition = jdbcTemplate.queryForList(strSampleConditionStatus);
		final List<Map<String, Object>> lstSampleCondition = new ArrayList<>();

		lstGetSampleCondition.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("ssamplecondition"));
			mapStatus.put("value", lst.get("nsamplecondition"));
			mapStatus.put("item", lst);
			lstSampleCondition.add(mapStatus);
		});

		outputMap.put("lstSampleCondition", lstSampleCondition);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSampleReceivingDetails(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var decOperator = (userInfo.getSdecimaloperator() == null || userInfo.getSdecimaloperator().isEmpty())
				? "."
				: userInfo.getSdecimaloperator();

		var strProductCondition = "";
		var strStorageCondition = "";
		var strBioParentSampleCondotion = "";
		var strBioProjectCondition = "";
		var strBioParentSampleConditionQty = "";
		final var sBioParentSampleCode = inputMap.get("sbioparentsamplecode").toString();
		final var nbioProjectCode = Integer.parseInt(inputMap.get("nbioprojectcode").toString());
		final var nproductCode = Integer.parseInt(inputMap.get("nproductcode").toString());
		final var nstorageTypeCode = Integer.parseInt(inputMap.get("nstoragetypecode").toString());

		final var strCheckDataAlreadyExists = "select string_agg(nsamplestoragetransactioncode ::text, ',') nsamplestoragetransactioncode"
				+ " from biorequestbasedtransferdetails where ntransferstatus not in ( "
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + "," + ""
				+ Enumeration.TransactionStatus.SENT.gettransactionstatus() + "," + ""
				+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + "," + ""
				+ Enumeration.TransactionStatus.RECEIVED.gettransactionstatus() + "" + " ) and nsitecode="
				+ userInfo.getNtranssitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final var strNsamplestoragetransactioncode = jdbcTemplate.queryForObject(strCheckDataAlreadyExists,
				String.class);

		final var nBioSampleStorageTransactionCode = strNsamplestoragetransactioncode != null
				? strNsamplestoragetransactioncode
				: Enumeration.TransactionStatus.NA.gettransactionstatus();

		if (nproductCode != -1) {
			strProductCondition = " and sst.nproductcode=" + nproductCode;
		}
		if (nstorageTypeCode != -1) {
			strStorageCondition = " and sst.nstoragetypecode=" + nstorageTypeCode;
		}

		if (nbioProjectCode != -1) {
			strBioProjectCondition = " and sst.nprojecttypecode=" + nbioProjectCode;
		}
		if (sBioParentSampleCode != null && sBioParentSampleCode != "") {
			strBioParentSampleCondotion = " and sst.sparentsamplecode='" + sBioParentSampleCode + "'";
			strBioParentSampleConditionQty = " and sparentsamplecode='" + sBioParentSampleCode + "'";
		}

		final var noOfSampleQry = "SELECT COALESCE(( SELECT " + " REPLACE(saccminvolume  COLLATE \"C\", '" + decOperator
				+ "', '.') :: numeric  " + "FROM bioecataloguerequestdetails  where nbioprojectcode = "
				+ nbioProjectCode + "  AND nproductcode = " + nproductCode + "  AND necatrequestreqapprovalcode = "
				+ inputMap.get("necatrequestreqapprovalcode") + "  AND nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  AND nsitecode = "
				+ userInfo.getNtranssitecode() + " " + strBioParentSampleConditionQty + " ), 0) AS naccnoofsamples ;";

		final var noOfSample = Objects.requireNonNullElse(
				jdbcTemplateUtilityFunction.queryForObject(noOfSampleQry, Integer.class, jdbcTemplate), "0");

		final int ntransferTypeCode = (Integer) inputMap.get("ntransfertypecode");
		var strSubjectDetails = " join biosubjectdetails bsd on bsd.ssubjectid=sst.ssubjectid " + " and bsd.nsitecode="
				+ userInfo.getNmastersitecode() + " and bsd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ((ntransferTypeCode == Enumeration.TransferType.BIOBANK.getntransfertype()
						|| ntransferTypeCode == Enumeration.TransferType.BIOBANKEXTERNAL.getntransfertype())
								? " and bsd.nissampleaccesable="
								: " and bsd.nisthirdpartysharable=")
				+ Enumeration.TransactionStatus.YES.gettransactionstatus();
		// modified by sujatha ATE_274 by selecting 4 fields by join the
		// samplestorageadditionalinfo table bgsi-218
		final var strSampleReceivingDetails = "select   sst.ncontainertypecode,p.nproductcatcode, sst.sparentsamplecode, sst.ncohortno, sst.ssubjectid, sst.scasetype,sst.ndiagnostictypecode,"
				+ "sst.nsamplestoragetransactioncode,sst.nbioparentsamplecode,sst.nbiosamplereceivingcode, sst.spositionvalue as srepositoryid, sst.slocationcode, sst.sqty,"
				+ " p.sproductname || ' ( ' || pc.sproductcatname || ')' as sproductname , p.nproductcode, "
				+ " ssa.sextractedsampleid, ssa.sconcentration, ssa.sqcplatform, ssa.seluent,sst.nstoragetypecode from  product p"
				+ " join productcategory pc on pc.nproductcatcode = p.nproductcatcode and pc.nsitecode ="
				+ userInfo.getNmastersitecode() + " " + "  and pc.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " , samplestoragetransaction sst "
				+ " join samplestorageadditionalinfo ssa on ssa.nsamplestoragetransactioncode=sst.nsamplestoragetransactioncode"
				+ " and ssa.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and ssa.nsitecode=" + userInfo.getNtranssitecode() + strSubjectDetails
				+ " where sst.nproductcode=p.nproductcode " + strBioParentSampleCondotion + strProductCondition
				+ strStorageCondition + strBioProjectCondition + " and sst.nsitecode=" + userInfo.getNtranssitecode()
				+ " and sst.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
				+ " p.nsitecode=" + userInfo.getNmastersitecode() + " and p.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and sst.nsamplestoragetransactioncode not in (" + nBioSampleStorageTransactionCode
				+ ") and REPLACE(sst.sqty  COLLATE \"C\", '" + decOperator + "', '.')::numeric >=" + noOfSample + " "
				+ "  order by sst.nsamplestoragetransactioncode";

		final var lstGetSampleReceivingDetails = jdbcTemplate.queryForList(strSampleReceivingDetails);

		if (lstGetSampleReceivingDetails.size() > 0) {
			outputMap.put("lstGetSampleReceivingDetails", lstGetSampleReceivingDetails);

			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_NOSAMPLESAVAILABLE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getStorageCondition(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final var strStorageCondition = "select nstorageconditioncode, sstorageconditionname from storagecondition where "
				+ " nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nstorageconditioncode > 0"
				+ " order by nstorageconditioncode";
		final var lstGetStorageCondition = jdbcTemplate.queryForList(strStorageCondition);

		final List<Map<String, Object>> lstStorageCondition = new ArrayList<>();
		lstGetStorageCondition.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("sstorageconditionname"));
			mapStatus.put("value", lst.get("nstorageconditioncode"));
			mapStatus.put("item", lst);
			lstStorageCondition.add(mapStatus);
		});

		outputMap.put("lstStorageCondition", lstStorageCondition);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getStorageType(final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var strStorageType = "select nstoragetypecode, coalesce(jsondata->>'en-US', jsondata->>'en-US')"
				+ " sstoragetypename from storagetype where nsitecode=" + userInfo.getNmastersitecode()
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nstoragetypecode > 0";
		final List<StorageType> lstGetStorageType = jdbcTemplate.query(strStorageType, new StorageType());

		final List<Map<String, Object>> lstStorageType = new ArrayList<>();

		lstGetStorageType.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getSstoragetypename());
			mapStatus.put("value", lst.getNstoragetypecode());
			mapStatus.put("item", lst);
			lstStorageType.add(mapStatus);
		});

		outputMap.put("lstStorageType", lstStorageType);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public String getToBeDisposeSamples(final int nbiorequestbasedtransfercode, final UserInfo userInfo)
			throws Exception {
		final var strToBeDisposeSamples = "select string_agg(nbiorequestbasedtransferdetailcode::text, ',') from"
				+ " biorequestbasedtransferdetails where nbiorequestbasedtransfercode=" + nbiorequestbasedtransfercode
				+ " and nsitecode=" + userInfo.getNtranssitecode() + " and ntransferstatus="
				+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		return jdbcTemplate.queryForObject(strToBeDisposeSamples, String.class);
	}

	@Override
	public ResponseEntity<Object> getTransferType(final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var strTransferType = " Select ntransfertypecode,coalesce(jsondata->'sdisplayname'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',jsondata->'sdisplayname'->>'en-US') as stransfertypename from transfertype  where ntransfertypecode>0 and nsitecode="
				+ userInfo.getNmastersitecode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " order by ndefaultstatus asc ";
		final List<TransferType> lstObjTransferType = jdbcTemplate.query(strTransferType, new TransferType());

		final List<Map<String, Object>> lstTransferType = new ArrayList<>();

		lstObjTransferType.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.getStransfertypename());
			mapStatus.put("value", lst.getNtransfertypecode());
			mapStatus.put("item", lst);
			lstTransferType.add(mapStatus);
		});

		outputMap.put("lstTransferTypeCombo", lstTransferType);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTransferTypeBasedFormNo(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var nTransferType = (int) inputMap.get("ntransfertypecode");

		final var originsiteorthirdparty = (int) inputMap.get("originsiteorthirdparty");

		String strTransferType = null;

		String strConcatQuery = null;

		if (nTransferType == Enumeration.TransferType.BIOBANK.getntransfertype()) {

			strConcatQuery = " AND ra.norginsitecode=" + originsiteorthirdparty + "";

		} else if (nTransferType == Enumeration.TransferType.THIRDPARTY.getntransfertype()) {

			strConcatQuery = " AND ra.norginthirdpartycode=" + originsiteorthirdparty + "";

		} else if (nTransferType == Enumeration.TransferType.BIOBANKEXTERNAL.getntransfertype()) {

			strConcatQuery = " AND ra.norginthirdpartycode=" + originsiteorthirdparty + "";
		}

		strTransferType = "SELECT ra.necatrequestreqapprovalcode, ra.sformnumber FROM bioecataloguereqapproval ra "
				+ "WHERE ra.ntransactionstatus = " + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ " AND ra.ntransfertype = " + nTransferType + " AND ra.nsitecode = " + userInfo.getNtranssitecode()
				+ " AND ra.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ strConcatQuery + " "
				+ "  AND EXISTS (SELECT 1 FROM bioecataloguerequestdetails berd WHERE berd.necatrequestreqapprovalcode = ra.necatrequestreqapprovalcode AND berd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ") "
				+ "  AND (SELECT SUM(naccnoofsamples) FROM bioecataloguerequestdetails berd WHERE berd.necatrequestreqapprovalcode = ra.necatrequestreqapprovalcode AND berd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")  "
				+ "    > (SELECT COUNT(*) FROM biorequestbasedtransferdetails brd WHERE brd.necatrequestreqapprovalcode = ra.necatrequestreqapprovalcode AND brd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " AND brd.ntransferstatus not in ("
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ") ) "
				+ "ORDER BY ra.necatrequestreqapprovalcode DESC; " + "";

//		strTransferType = "select necatrequestreqapprovalcode,sformnumber from bioecataloguereqapproval ra "
//				+ "where ra.necatrequestreqapprovalcode not in (select brd.necatrequestreqapprovalcode from biorequestbasedtransfer br,biorequestbasedtransferdetails brd ,bioecataloguerequestdetails ecra "
//				+ "where  brd.necatrequestreqapprovalcode=brd.necatrequestreqapprovalcode  "
//				+ "and brd.nproductcode=brd.nproductcode and brd.nbioprojectcode=brd.nbioprojectcode "
//				+ "and br.ntransfertypecode = " + nTransferType + " and br.ntransactionstatus="
//				+ Enumeration.TransactionStatus.SENT.gettransactionstatus() + " " + "and br.nsitecode="
//				+ userInfo.getNtranssitecode() + " " + "and brd.nsitecode=" + userInfo.getNtranssitecode() + " "
//				+ "and br.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ "and brd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
//				+ " and ecra.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) "
//				+ "and ra.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
//				+ " and ra.ntransfertype=" + nTransferType + " and ra.nsitecode=" + userInfo.getNtranssitecode() + " "
//				+ " and ra.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " "
//				+ strConcatQuery + "  order by 1 desc";

		final var lstObjRequestFormNo = jdbcTemplate.queryForList(strTransferType);

		final List<Map<String, Object>> lstRequestFormNo = new ArrayList<>();

		lstObjRequestFormNo.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("sformnumber"));
			mapStatus.put("value", lst.get("necatrequestreqapprovalcode"));
			mapStatus.put("item", lst);
			lstRequestFormNo.add(mapStatus);
		});

		outputMap.put("lstRequestFormNo", lstRequestFormNo);

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getTransferTypeRecord(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final var nTransferType = (int) inputMap.get("ntransfertypecode");

		String strTransferType = null;

		if (nTransferType == Enumeration.TransferType.BIOBANK.getntransfertype()) {
			strTransferType = "select ra.norginsitecode as originsiteorthirdparty,s.ssitename as siteorthirdparty from "
					+ " bioecataloguereqapproval ra,site s where ra.norginsitecode=s.nsitecode and  ra.nsitecode="
					+ userInfo.getNtranssitecode() + " and   ra.norginsitecode >0  and ra.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and s.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ra.ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and ntransfertype="
					+ nTransferType + " group by ra.norginsitecode,s.ssitename order by 1 desc";

			final var lstObjTransferTypeRecod = jdbcTemplate.queryForList(strTransferType);

			final List<Map<String, Object>> lstTransferTypeRecord = new ArrayList<>();

			lstObjTransferTypeRecod.stream().forEach(lst -> {
				final Map<String, Object> mapStatus = new HashMap<>();
				mapStatus.put("label", lst.get("siteorthirdparty"));
				mapStatus.put("value", lst.get("originsiteorthirdparty"));
				mapStatus.put("item", lst);
				lstTransferTypeRecord.add(mapStatus);
			});

			outputMap.put("lstTransferTypeRecord", lstTransferTypeRecord);
		} else if (nTransferType == Enumeration.TransferType.THIRDPARTY.getntransfertype()) {
			strTransferType = "select tp.nthirdpartycode as originsiteorthirdparty,tp.sthirdpartyname as siteorthirdparty from "
					+ " bioecataloguereqapproval ra,thirdparty tp  where ra.norginthirdpartycode=tp.nthirdpartycode "
					+ "and  ra.nsitecode=" + userInfo.getNtranssitecode() + " and ra.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and  tp.nsitecode="
					+ userInfo.getNmastersitecode() + " and tp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tp.nthirdpartycode>0 and nisngs=" + Enumeration.TransactionStatus.NO.gettransactionstatus()
					+ " " + "  and ra.ntransfertype=" + nTransferType
					+ " group by tp.nthirdpartycode,tp.sthirdpartyname order by 1 desc";

			final var lstObjTransferTypeRecod = jdbcTemplate.queryForList(strTransferType);

			final List<Map<String, Object>> lstTransferTypeRecord = new ArrayList<>();

			lstObjTransferTypeRecod.stream().forEach(lst -> {
				final Map<String, Object> mapStatus = new HashMap<>();
				mapStatus.put("label", lst.get("siteorthirdparty"));
				mapStatus.put("value", lst.get("originsiteorthirdparty"));
				mapStatus.put("item", lst);
				lstTransferTypeRecord.add(mapStatus);
			});

			outputMap.put("lstTransferTypeRecord", lstTransferTypeRecord);

		} else if (nTransferType == Enumeration.TransferType.BIOBANKEXTERNAL.getntransfertype()) {
			strTransferType = "select tp.nthirdpartycode as originsiteorthirdparty,tp.sthirdpartyname as siteorthirdparty from "
					+ " bioecataloguereqapproval ra,thirdparty tp  where ra.norginthirdpartycode=tp.nthirdpartycode "
					+ "and  ra.nsitecode=" + userInfo.getNtranssitecode() + " and ra.nstatus= "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and  tp.nsitecode="
					+ userInfo.getNmastersitecode() + " and tp.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and tp.nthirdpartycode>0  and nisngs="
					+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " " + " and ra.ntransfertype="
					+ nTransferType + " group by tp.nthirdpartycode,tp.sthirdpartyname order by 1 desc";

			final var lstObjTransferTypeRecod = jdbcTemplate.queryForList(strTransferType);

			final List<Map<String, Object>> lstTransferTypeRecord = new ArrayList<>();

			lstObjTransferTypeRecod.stream().forEach(lst -> {
				final Map<String, Object> mapStatus = new HashMap<>();
				mapStatus.put("label", lst.get("siteorthirdparty"));
				mapStatus.put("value", lst.get("originsiteorthirdparty"));
				mapStatus.put("item", lst);
				lstTransferTypeRecord.add(mapStatus);
			});

			outputMap.put("lstTransferTypeRecord", lstTransferTypeRecord);

		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getUsersBasedOnSite(final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final var strUsersBasedOnSite = "select u.nusercode ndispatchercode, concat(u.sfirstname, ' ', u.slastname) sdispatchername from"
				+ " users u, userssite us where u.nusercode=us.nusercode and us.nsitecode="
				+ userInfo.getNtranssitecode() + " and u.nsitecode=" + userInfo.getNmastersitecode() + " and u.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and us.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and u.nusercode > 0 order by u.nusercode";
		final var lstGetUsersBasedOnSite = jdbcTemplate.queryForList(strUsersBasedOnSite);

		final List<Map<String, Object>> lstUsers = new ArrayList<>();
		lstGetUsersBasedOnSite.stream().forEach(lst -> {
			final Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("label", lst.get("sdispatchername"));
			mapStatus.put("value", lst.get("ndispatchercode"));
			mapStatus.put("item", lst);
			lstUsers.add(mapStatus);
		});

		outputMap.put("lstUsers", lstUsers);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	private List<Map<String, Object>> getValidationSampleToAdd(
			final BioRequestBasedTransfer objBioRequestBasedTransferData,
			final List<Map<String, Object>> filteredSampleReceiving, final UserInfo userInfo) {

		var filteredSampleReceivingRemove = filteredSampleReceiving;

		final var sSampleStorageTransactionCode = objBioRequestBasedTransferData.getSsamplestoragetransactioncode();

		final var validationSample = "select string_agg(nsamplestoragetransactioncode::text ,',')  from biorequestbasedtransferdetails "
				+ "where  ntransferstatus not in (" + Enumeration.TransactionStatus.CANCELED.gettransactionstatus()
				+ "," + Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ") "
				+ " and  nsamplestoragetransactioncode in(" + sSampleStorageTransactionCode + ") and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  order by 1 desc";

		final var removeSample = jdbcTemplate.queryForObject(validationSample, String.class);

		if (removeSample != null) {
			final Set<Integer> removeSampleSet = Arrays.stream(removeSample.split(",")).filter(s -> !s.isBlank())
					.map(String::trim).map(Integer::parseInt).collect(Collectors.toSet());

			filteredSampleReceivingRemove = filteredSampleReceiving.stream().filter(map -> {
				final var numValue = (Integer) map.get("nsamplestoragetransactioncode");
				return !(numValue instanceof Integer && removeSampleSet.contains(numValue));
			}).collect(Collectors.toList());
		}

		return filteredSampleReceivingRemove;

	}

	public Map<String, Object> insertBioRequestBasedTransferDetails(
			final BioRequestBasedTransfer objBioRequestBasedTransferData,
			final List<Map<String, Object>> filteredSampleReceiving, final int nbioRequestBasedTransferCode,
			final int nEcatRequestreqApprovalCode, final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new HashMap<>();

		final var qrynReqformTypeCode = "SELECT nreqformtypecode,sformnumber  FROM bioecataloguereqapproval "
				+ "where necatrequestreqapprovalcode=" + nEcatRequestreqApprovalCode + " and " + "nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";

		final var bioRequestBasedTransferObj = (BioRequestBasedTransfer) jdbcTemplateUtilityFunction
				.queryForObject(qrynReqformTypeCode, BioRequestBasedTransfer.class, jdbcTemplate);

		final var nReqformTypeCode = bioRequestBasedTransferObj.getNreqformtypecode();

		final var sReqApproveFormNumber = bioRequestBasedTransferObj.getSformnumber();

		int seqNoBioRequetBasedTransferDetails = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnobiobankmanagement where stablename='biorequestbasedtransferdetails' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);

		int seqNoBioRequestTransferDetailsHistory = jdbcTemplate.queryForObject(
				"select nsequenceno from seqnobiobankmanagement where stablename='bioreqbasedtransdetailshistory' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
				Integer.class);

		final StringBuilder selectedChildRecord = new StringBuilder();

		var strInsertDirectTransferDetails = """
				INSERT INTO biorequestbasedtransferdetails(
				nbiorequestbasedtransferdetailcode, nbiorequestbasedtransfercode,
				nbioprojectcode, nbioparentsamplecode, necatrequestreqapprovalcode,
				nreqformtypecode, nsamplestoragetransactioncode, nbiosamplereceivingcode,
				ncohortno, nstoragetypecode, nproductcatcode, nproductcode,
				nsamplecondition, srepositoryid, slocationcode, svolume, ssubjectid, jsondata,
				ndiagnostictypecode, ncontainertypecode, ntransferstatus, nreasoncode,
				dtransactiondate, ntztransactiondate, noffsetdtransactiondate,
				nsitecode, nstatus) VALUES """;

		var strInsertRequestTransferDetailsHistory = """
							INSERT INTO public.bioreqbasedtransdetailshistory(
				nbioreqbasedtransdetailshistorycode, nbiorequestbasedtransferdetailcode, nbiorequestbasedtransfercode,
				nsamplecondition, ntransferstatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nusercode,
				nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) VALUES """;

		for (final Map<String, Object> objBioSampleReceiving : filteredSampleReceiving) {
			seqNoBioRequestTransferDetailsHistory++;
			seqNoBioRequetBasedTransferDetails++;

			selectedChildRecord.append(seqNoBioRequetBasedTransferDetails).append(",");

			final var ssubjectId = objBioSampleReceiving.get("ssubjectid") != null
					? "'" + objBioSampleReceiving.get("ssubjectid") + "'"
					: null;
			final var scaseType = objBioSampleReceiving.get("scasetype") != null
					? "'" + objBioSampleReceiving.get("scasetype") + "'"
					: null;
			final var slocationcode = objBioSampleReceiving.get("slocationcode") != null
					? "'" + objBioSampleReceiving.get("slocationcode") + "'"
					: null;
			final var srepositoryid = objBioSampleReceiving.get("srepositoryid") != null
					? "'" + objBioSampleReceiving.get("srepositoryid") + "'"
					: null;
			final var sparentsamplecode = objBioSampleReceiving.get("sparentsamplecode") != null
					? "'" + objBioSampleReceiving.get("sparentsamplecode") + "'"
					: null;
			// added by sujatha ATE_274 for inserting into requestbasedtransferdetails
			// bgsi-218
			final var sextractedsampleid = objBioSampleReceiving.get("sextractedsampleid") != null
					? "'" + objBioSampleReceiving.get("sextractedsampleid") + "'"
					: null;
			final var sconcentration = objBioSampleReceiving.get("sconcentration") != null
					? "'" + objBioSampleReceiving.get("sconcentration") + "'"
					: null;
			final var sqcplatform = objBioSampleReceiving.get("sqcplatform") != null
					? "'" + objBioSampleReceiving.get("sqcplatform") + "'"
					: null;
			final var seluent = objBioSampleReceiving.get("seluent") != null
					? "'" + objBioSampleReceiving.get("seluent") + "'"
					: null;

			// modified by sujatha ATE_274 by adding 4 new key's in the jsondata column
			// bgsi-218
			strInsertDirectTransferDetails += "(" + seqNoBioRequetBasedTransferDetails + "," + " "
					+ nbioRequestBasedTransferCode + ", " + objBioRequestBasedTransferData.getNbioprojectcode() + ", '"
					+ objBioSampleReceiving.get("nbioparentsamplecode") + "'," + nEcatRequestreqApprovalCode + ", "
					+ nReqformTypeCode + "," + objBioSampleReceiving.get("nsamplestoragetransactioncode") + ", "
					+ objBioSampleReceiving.get("nbiosamplereceivingcode") + ", "
//					+ sparentsamplecode + ", "
					+ objBioSampleReceiving.get("ncohortno") + ", " + objBioSampleReceiving.get("nstoragetypecode")
					+ "," + objBioSampleReceiving.get("nproductcatcode") + ", "
					+ objBioSampleReceiving.get("nproductcode") + ", "
					+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + ", " + srepositoryid + ", "
					+ slocationcode + ", '" + objBioSampleReceiving.get("sqty") + "'," + ssubjectId + ", "
					+ " json_build_object('sparentsamplecode'," + sparentsamplecode + ", 'ssubjectid', " + ssubjectId
					+ ", 'scasetype', " + scaseType + ", 'sformnumber', '"
					+ objBioRequestBasedTransferData.getSformnumber() + "', 'svolume', '"
					+ objBioSampleReceiving.get("sqty") + "', 'srepositoryid', " + srepositoryid
					+ ", 'sextractedsampleid', " + sextractedsampleid + ", 'sconcentration', " + sconcentration
					+ ", 'sqcplatform', " + sqcplatform + ", 'seluent', " + seluent + ",'"
					+ Enumeration.FormCode.REQUESTBASEDTRANSFER.getFormCode() + "', '"
					+ objBioRequestBasedTransferData.getSformnumber() + "','"
					+ Enumeration.FormCode.ECATALOGREQUESTAPPROVAL.getFormCode() + "', '" + sReqApproveFormNumber
					+ "'   ),"
//					+ ssubjectId + ", " + scaseType + ", " 
					+ objBioSampleReceiving.get("ndiagnostictypecode") + ", "
					+ objBioSampleReceiving.get("ncontainertypecode") + ", "
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + "," + ""
					+ Enumeration.TransactionStatus.NA.gettransactionstatus() + " ," + "'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNtranssitecode() + ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "),";

			strInsertRequestTransferDetailsHistory += "(" + seqNoBioRequestTransferDetailsHistory + ","
					+ seqNoBioRequetBasedTransferDetails + "," + nbioRequestBasedTransferCode + ","
					+ Enumeration.TransactionStatus.ACCEPTED.gettransactionstatus() + ","
					+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ",'"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode() + ","
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ","
					+ userInfo.getNusercode() + "," + userInfo.getNuserrole() + "," + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + "," + userInfo.getNtranssitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),";
		}

		strInsertDirectTransferDetails = strInsertDirectTransferDetails.substring(0,
				strInsertDirectTransferDetails.length() - 1) + ";";

		strInsertRequestTransferDetailsHistory = strInsertRequestTransferDetailsHistory.substring(0,
				strInsertRequestTransferDetailsHistory.length() - 1) + ";";

		final var strSeqNoUpdate = "update seqnobiobankmanagement set nsequenceno=" + seqNoBioRequetBasedTransferDetails
				+ " where stablename='biorequestbasedtransferdetails' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		final var strSeqNoUpdateHistory = "update seqnobiobankmanagement set nsequenceno="
				+ seqNoBioRequestTransferDetailsHistory + " where stablename='bioreqbasedtransdetailshistory' and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

		outputMap.put("InsertQuery", strInsertDirectTransferDetails + strSeqNoUpdate
				+ strInsertRequestTransferDetailsHistory + strSeqNoUpdateHistory);
		outputMap.put("selectedChildRecord",
				selectedChildRecord.deleteCharAt(selectedChildRecord.length() - 1).toString());

		return outputMap;
	}

	@Override
	public ResponseEntity<Object> requestBasedTransfer(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final String sQueryFormAcc = " lock  table lockbioformacceptance "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryFormAcc);

		final String sQueryRetrivel = " lock  table locksamplestoragetransaction "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQueryRetrivel);

		final var nBioRequestBasedTransferCode = (int) inputMap.get("nbiorequestbasedtransfercode");
		// final var nbiorequestbasedtransfercode = (int)
		// inputMap.get("nbiorequestbasedtransfercode");

		final List<String> multilingualIDList = new ArrayList<>();

		final var findStatus = findStatusRequestBasedtTransfer(nBioRequestBasedTransferCode, userInfo);
		if (findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {
			final var rejectedRecordExists = checkApprovedSample(nBioRequestBasedTransferCode, userInfo);
			if (rejectedRecordExists) {
				final var availableSamples = checkSamplesAvailableToTransfer(nBioRequestBasedTransferCode, userInfo);
				if (availableSamples) {

					final var strAuditQryForm = "select ts.jsondata->'stransdisplaystatus'->>'"
							+ userInfo.getSlanguagetypecode() + "' as stransferstatus, "
							+ "sformnumber from biorequestbasedtransfer brt,transactionstatus ts "
							+ "where brt.ntransactionstatus=ts.ntranscode " + "and brt.nbiorequestbasedtransfercode="
							+ nBioRequestBasedTransferCode + " and brt.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "; ";

					final var strAuditQryRepo = "SELECT   STRING_AGG(brdt.srepositoryid, ', ' ORDER BY brdt.nbiorequestbasedtransferdetailcode) AS srepositoryid,"
							+ "    ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
							+ "' AS stransdisplaystatus FROM biorequestbasedtransferdetails brdt "
							+ "JOIN  transactionstatus ts  ON brdt.ntransferstatus = ts.ntranscode and brdt.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "and brdt.ntransferstatus="
							+ Enumeration.TransactionStatus.TOBEDISPOSE.gettransactionstatus()
							+ " and brdt.nbiorequestbasedtransfercode=" + nBioRequestBasedTransferCode + " "
							+ "GROUP BY  ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
							+ "' ;";

					final var lstAuditBefore = (BioRequestBasedTransfer) jdbcTemplateUtilityFunction
							.queryForObject(strAuditQryForm, BioRequestBasedTransfer.class, jdbcTemplate);

					final var auditrepo = (BioRequestBasedTransfer) jdbcTemplateUtilityFunction
							.queryForObject(strAuditQryRepo, BioRequestBasedTransfer.class, jdbcTemplate);

					if (auditrepo != null) {
						lstAuditBefore.setSrepositoryid(auditrepo.getSrepositoryid());
						lstAuditBefore.setStransdisplaystatus(auditrepo.getStransdisplaystatus());
					}

					// Added by ate234 for external sample need to ssend in third party table

					final var externalTransfer = "select ntransfertypecode from biorequestbasedtransfer "
							+ "where nbiorequestbasedtransfercode=" + nBioRequestBasedTransferCode + " AND nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";

					final var objNthirdParty = (BioRequestBasedTransfer) jdbcTemplateUtilityFunction
							.queryForObject(externalTransfer, BioRequestBasedTransfer.class, jdbcTemplate);

					final int intRetrievalPk = jdbcTemplate
							.queryForObject(
									"select nsequenceno from seqnostoragemanagement where "
											+ "stablename='samplestorageretrieval' and nstatus="
											+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
									Integer.class);

					final var strBioTransferDetailsCode = getToBeDisposeSamples(nBioRequestBasedTransferCode, userInfo);
					final var concatConditionTransferDetailsNot = (strBioTransferDetailsCode != ""
							&& strBioTransferDetailsCode != null)
									? " and nbiorequestbasedtransferdetailcode not in (" + strBioTransferDetailsCode
											+ ")"
									: "";
					final var concatConditionDirectTransferDetails = (strBioTransferDetailsCode != ""
							&& strBioTransferDetailsCode != null)
									? " and nbiorequestbasedtransferdetailcode in (" + strBioTransferDetailsCode + ")"
									: "";
					final var strTransferDirectTransfer = new StringBuilder(
							"update biorequestbasedtransfer set ntransactionstatus=")
							.append(Enumeration.TransactionStatus.SENT.gettransactionstatus())
							.append(" where nbiorequestbasedtransfercode=").append(nBioRequestBasedTransferCode)
							.append(" and nsitecode=").append(userInfo.getNtranssitecode()).append(" and nstatus=")
							.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");

					strTransferDirectTransfer.append("update biorequestbasedtransferdetails set ntransferstatus=")
							.append(Enumeration.TransactionStatus.SENT.gettransactionstatus())
							.append(" where nbiorequestbasedtransfercode=").append(nBioRequestBasedTransferCode)
							.append(" and nsitecode=").append(userInfo.getNtranssitecode()).append(" and nstatus=")
							.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
							.append(concatConditionTransferDetailsNot).append(";");

					final var strSampleStorageRetrieval = new StringBuilder(
							"insert into samplestorageretrieval (nsamplestorageretrievalcode,")
							.append(" nsamplestoragetransactioncode, nsamplestoragelocationcode, nsamplestoragelistcode,")
							.append(" nsamplestoragemappingcode, nprojecttypecode, nusercode, nuserrolecode, nbiosamplereceivingcode,")
							.append(" sposition, spositionvalue, jsondata, ntransactionstatus,ntransferstatuscode,")
							.append(" ninstrumentcode, nbioparentsamplecode, sparentsamplecode, ncohortno, nproductcatcode, nproductcode, sqty,")
							.append(" slocationcode, ssubjectid, scasetype, ndiagnostictypecode, ncontainertypecode, nstoragetypecode, dtransactiondate,")
							.append(" noffsetdtransactiondate, ntransdatetimezonecode, nsitecode, nstatus)")
							.append(" select ").append(intRetrievalPk)
							.append("+rank()over(order by bdtd.nbiorequestbasedtransferdetailcode),")
							.append(" sst.nsamplestoragetransactioncode, sst.nsamplestoragelocationcode, -1,")
							.append(" sst.nsamplestoragemappingcode, sst.nprojecttypecode, ")
							.append(userInfo.getNusercode()).append(", ").append(userInfo.getNuserrole())
							.append(", bdtd.nbiosamplereceivingcode,")
							.append(" sst.sposition, sst.spositionvalue, sst.jsondata, ")
							.append(Enumeration.TransactionStatus.RETRIEVED.gettransactionstatus()).append(", ")
							.append(Enumeration.TransactionStatus.SENT.gettransactionstatus()).append(", ")
//							.append(Enumeration.TransactionStatus.NA.gettransactionstatus()).append(",") // commented and the below ninstrument is added by sujatha ATE_274 for an issue of getting -1 
							.append("sst.ninstrumentcode, ").append("bdtd.nbioparentsamplecode").append(",")
							.append("bdtd.jsondata->>'sparentsamplecode' AS sparentsamplecode").append(",")
							.append("bdtd.ncohortno").append(",").append("bdtd.nproductcatcode").append(",")
							.append("bdtd.nproductcode").append(",").append("bdtd.jsondata->>'svolume' AS sqty")
							.append(",").append("bdtd.slocationcode").append(",")
							.append("bdtd.jsondata->>'ssubjectid' AS ssubjectid").append(",")
							.append("bdtd.jsondata->>'scasetype' AS scasetype").append(",")
							.append("bdtd.ndiagnostictypecode").append(",").append("bdtd.ncontainertypecode")
							.append(",").append("bdtd.nstoragetypecode").append(",")
							.append("'" + dateUtilityFunction.getCurrentDateTime(userInfo)).append("', ")
							.append(dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()))
							.append(", ").append(userInfo.getNtimezonecode()).append(", sst.nsitecode, ")
							.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
							.append(" from biorequestbasedtransferdetails bdtd ")
							.append(" join samplestoragetransaction sst on bdtd.nsamplestoragetransactioncode=sst.nsamplestoragetransactioncode")
							.append(" and sst.nsitecode=").append(userInfo.getNtranssitecode())
							.append(" and sst.nstatus=")
							.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
							.append(" where bdtd.nsitecode=").append(userInfo.getNtranssitecode())
							.append(" and bdtd.nstatus=")
							.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
							.append(" and bdtd.nbiorequestbasedtransfercode=").append(nBioRequestBasedTransferCode)
							.append(concatConditionTransferDetailsNot)
							.append(" order by bdtd.nbiorequestbasedtransferdetailcode").append(";");

					// added by sujatha ATE_274 for inserting into sampleretrievaladditionalinfo
					// while insert in samplestorageretrieval BGSI-218
					var strSampleRetrivalAdditionalinfo = "INSERT INTO public.sampleretrievaladditionalinfo"
							+ "( nsamplestorageretrievalcode, sextractedsampleid, sconcentration,"
							+ " sqcplatform, seluent, dmodifieddate, nsitecode, nstatus)" + " select " + intRetrievalPk
							+ "+rank()over(order by bdtd.nbiorequestbasedtransferdetailcode ) ,"
							+ " ssa.sextractedsampleid, ssa.sconcentration, ssa.sqcplatform, ssa.seluent, " + "'"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' , ssa.nsitecode, "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " from samplestorageadditionalinfo ssa JOIN samplestoragetransaction sst "
							+ " ON ssa.nsamplestoragetransactioncode = sst.nsamplestoragetransactioncode "
							+ " AND sst.nsitecode =" + userInfo.getNtranssitecode() + " AND sst.nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " JOIN biorequestbasedtransferdetails bdtd ON bdtd.nbiosamplereceivingcode = sst.nbiosamplereceivingcode"
							+ " AND bdtd.nsitecode =" + userInfo.getNtranssitecode() + " AND bdtd.nstatus ="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " WHERE bdtd.nbiorequestbasedtransfercode =" + nBioRequestBasedTransferCode + " "
							+ concatConditionTransferDetailsNot // added by sujatha ATE_274 for not passing condition
																// before-> throwing 500 error
							+ " order by bdtd.nbiorequestbasedtransferdetailcode; ";

					final int seqNoRetrievalCount = jdbcTemplate.queryForObject(
							"select count(bdtd.nbiorequestbasedtransferdetailcode) from biorequestbasedtransferdetails bdtd"
									+ " join samplestoragetransaction sst on bdtd.nbiosamplereceivingcode=sst.nbiosamplereceivingcode"
									+ " and sst.nsitecode=" + userInfo.getNtranssitecode() + " and sst.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " where bdtd.nsitecode=" + userInfo.getNtranssitecode() + " and bdtd.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and bdtd.nbiorequestbasedtransfercode=" + nBioRequestBasedTransferCode
									+ concatConditionTransferDetailsNot + "",
							Integer.class);

					var strBioFormAcceptance = new StringBuilder();

					var strBioFormAcceptanceDetails = new StringBuilder();

					if (objNthirdParty.getNtransfertypecode() == Enumeration.TransferType.BIOBANK.getntransfertype()) {

						int intFormAcceptancePk = jdbcTemplate
								.queryForObject(
										"select nsequenceno from seqnobiobankmanagement where "
												+ "stablename='bioformacceptance' and nstatus="
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
										Integer.class);
						intFormAcceptancePk++;

						int formAcceptanceHistoryPk = jdbcTemplate
								.queryForObject(
										"select nsequenceno from seqnobiobankmanagement where "
												+ "stablename='bioformacceptancehistory' and nstatus="
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
										Integer.class);
						formAcceptanceHistoryPk++;
						final int formAcceptanceHistoryDetailsPk = jdbcTemplate
								.queryForObject(
										"select nsequenceno from seqnobiobankmanagement where "
												+ "stablename='bioformacceptdetailshistory' and nstatus="
												+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
										Integer.class);
						final int intFormAcceptanceDetailsPk = jdbcTemplate.queryForObject(
								"select nsequenceno from seqnobiobankmanagement where "
										+ "stablename='bioformacceptancedetails' and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";",
								Integer.class);

						strBioFormAcceptance = new StringBuilder(
								"insert into bioformacceptance (nbioformacceptancecode, nbiodirecttransfercode,")
								.append(" nbiorequestbasedtransfercode, nbiobankreturncode, sformnumber, ntransfertypecode, nformtypecode, noriginsitecode,")
								.append(" nsenderusercode, nsenderuserrolecode, dtransferdate, ntztransferdate, noffsetdtransferdate,")
								.append(" ntransactionstatus, nstorageconditioncode, ddeliverydate, ntzdeliverydate,")
								.append(" noffsetddeliverydate, ndispatchercode, ncouriercode, jsondata, ")
								.append("dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode,")
								.append(" nstatus) select ").append(intFormAcceptancePk)
								.append(", -1, nbiorequestbasedtransfercode, -1, sformnumber,")
								.append(" ntransfertypecode, 1, ").append(userInfo.getNtranssitecode()).append(", ")
								.append(userInfo.getNusercode()).append(", ").append(userInfo.getNuserrole())
								.append(", dtransferdate, ntztransferdate, noffsetdtransferdate, ")
								.append(Enumeration.TransactionStatus.DRAFT.gettransactionstatus()).append(", ")
								.append(" nstorageconditioncode, ddeliverydate, ntzdeliverydate, noffsetddeliverydate, ndispatchercode,")
								.append(" ncouriercode, jsondata || json_build_object('soriginsitename', '"
										+ userInfo.getSsitename() + "')::jsonb, ")
								.append(" '").append(dateUtilityFunction.getCurrentDateTime(userInfo)).append("', ")
								.append(userInfo.getNtimezonecode()).append(", ")
								.append(dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()))
								.append(", nreceiversitecode, ")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
								.append(" from biorequestbasedtransfer where nbiorequestbasedtransfercode=")
								.append(nBioRequestBasedTransferCode).append(" and nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
								.append(" and nsitecode=").append(userInfo.getNtranssitecode())
								.append(" order by nbiorequestbasedtransfercode;");

						strBioFormAcceptance
								.append("insert into bioformacceptancehistory (nbioformacceptancehistorycode,")
								.append(" nbioformacceptancecode, ntransactionstatus, dtransactiondate, ntztransactiondate,")
								.append(" noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode,")
								.append(" nstatus) select ").append(formAcceptanceHistoryPk).append(", ")
								.append(intFormAcceptancePk).append(", ")
								.append(Enumeration.TransactionStatus.DRAFT.gettransactionstatus()).append(", '")
								.append(dateUtilityFunction.getCurrentDateTime(userInfo)).append("', ")
								.append(userInfo.getNtimezonecode()).append(", ")
								.append(dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()))
								.append(", ").append(userInfo.getNusercode()).append(", ")
								.append(userInfo.getNuserrole()).append(", ").append(userInfo.getNdeputyusercode())
								.append(", ").append(userInfo.getNdeputyuserrole()).append(", nreceiversitecode, ")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
								.append(" from biorequestbasedtransfer where nbiorequestbasedtransfercode=")
								.append(nBioRequestBasedTransferCode).append(" and nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
								.append(" and nsitecode=").append(userInfo.getNtranssitecode())
								.append(" order by nbiorequestbasedtransfercode;");

						strBioFormAcceptanceDetails = new StringBuilder()
								.append("insert into bioformacceptancedetails (")
								.append("nbioformacceptancedetailscode, nbioformacceptancecode, nbioprojectcode, nbioparentsamplecode, ")
								.append("ncohortno, nstoragetypecode, nproductcatcode, nproductcode, srepositoryid, ")
								.append("svolume, sreceivedvolume, ssubjectid, jsondata,  ndiagnostictypecode, ")
								.append("ncontainertypecode, nbiosamplereceivingcode, nsamplecondition, nsamplestatus, nreasoncode, ")
								.append("dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus")
								.append(") select ").append(intFormAcceptanceDetailsPk)
								.append("+rank()over(order by nbiorequestbasedtransferdetailcode), ")
								.append(intFormAcceptancePk).append(", nbioprojectcode, ")
								.append(" nbioparentsamplecode,ncohortno, nstoragetypecode,nproductcatcode, ")
								.append("nproductcode,srepositoryid,svolume,svolume, jsondata->>'ssubjectid', jsondata, ")
								.append(" ndiagnostictypecode,ncontainertypecode, nbiosamplereceivingcode, nsamplecondition, ")
								.append(" ntransferstatus, nreasoncode, '")
								.append(dateUtilityFunction.getCurrentDateTime(userInfo)).append("', ")
								.append(userInfo.getNtimezonecode()).append(", ")
								.append(dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()))
								.append(", (select nreceiversitecode from biorequestbasedtransfer where nbiorequestbasedtransfercode=")
								.append(nBioRequestBasedTransferCode).append(" and nsitecode=")
								.append(userInfo.getNtranssitecode()).append(" and nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append("), ")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
								.append(" from biorequestbasedtransferdetails where nbiorequestbasedtransfercode=")
								.append(nBioRequestBasedTransferCode).append(" and nsitecode=")
								.append(userInfo.getNtranssitecode()).append(" and nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
								.append(" and ntransferstatus=")
								.append(Enumeration.TransactionStatus.SENT.gettransactionstatus())
								.append(" order by nbiorequestbasedtransferdetailcode;");

						strBioFormAcceptanceDetails.append("insert into bioformacceptdetailshistory").append(
								" (nbioformacceptdetailshistorycode, nbioformacceptancedetailscode, nbioformacceptancecode,")
								.append(" nsamplecondition, nsamplestatus, dtransactiondate, ntztransactiondate, noffsetdtransactiondate,")
								.append(" nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus) ")
								.append("select ").append(formAcceptanceHistoryDetailsPk)
								.append("+rank()over(order by nbioformacceptancedetailscode),")
								.append(" nbioformacceptancedetailscode, nbioformacceptancecode, nsamplecondition, nsamplestatus, '")
								.append(dateUtilityFunction.getCurrentDateTime(userInfo)).append("', ")
								.append(userInfo.getNtimezonecode()).append(", ")
								.append(dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()))
								.append(", ").append(userInfo.getNusercode()).append(", ")
								.append(userInfo.getNuserrole()).append(", ").append(userInfo.getNdeputyusercode())
								.append(", ").append(userInfo.getNdeputyuserrole()).append(", ")
								.append(userInfo.getNtranssitecode()).append(", ")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
								.append(" from bioformacceptancedetails where ").append(" nbioformacceptancecode=")
								.append(intFormAcceptancePk).append(" and nsitecode=")
								.append("(select nreceiversitecode from biorequestbasedtransfer where nbiorequestbasedtransfercode=")
								.append(nBioRequestBasedTransferCode).append(" and nsitecode=")
								.append(userInfo.getNtranssitecode()).append(" and nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(") ")
								.append(" and nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
								.append(" order by nbioformacceptancedetailscode;");

						strBioFormAcceptance.append(" update seqnobiobankmanagement set nsequenceno=")
								.append(intFormAcceptancePk)
								.append(" where stablename='bioformacceptance' and nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");

						strBioFormAcceptance.append(" update seqnobiobankmanagement set nsequenceno=")
								.append(formAcceptanceHistoryPk)
								.append(" where stablename='bioformacceptancehistory' and nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");

						strBioFormAcceptanceDetails.append(" update seqnobiobankmanagement set nsequenceno=(")
								.append("select ").append(formAcceptanceHistoryDetailsPk)
								.append(" + count(nbioformacceptancedetailscode) from bioformacceptancedetails where")
								.append(" nbioformacceptancecode=").append(intFormAcceptancePk)
								.append(" and nsitecode=")
								.append("(select nreceiversitecode from biorequestbasedtransfer where nbiorequestbasedtransfercode=")
								.append(nBioRequestBasedTransferCode).append(" and nsitecode=")
								.append(userInfo.getNtranssitecode()).append(" and nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(") ")
								.append(" and nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(")")
								.append(" where stablename='bioformacceptdetailshistory' and nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");

						strBioFormAcceptanceDetails.append(" update seqnobiobankmanagement set nsequenceno=(")
								.append("select ").append(intFormAcceptanceDetailsPk)
								.append(" + count(nbiorequestbasedtransferdetailcode) from biorequestbasedtransferdetails where")
								.append(" nbiorequestbasedtransfercode=").append(nBioRequestBasedTransferCode)
								.append(" and nsitecode=").append(userInfo.getNtranssitecode()).append(" and nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
								.append(" and ntransferstatus=")
								.append(Enumeration.TransactionStatus.SENT.gettransactionstatus())
								.append(") where stablename='bioformacceptancedetails' and nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");

					}

					strSampleStorageRetrieval.append("update seqnostoragemanagement set nsequenceno=")
							.append(intRetrievalPk + seqNoRetrievalCount)
							.append(" where stablename='samplestorageretrieval' and nstatus=")
							.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");

					String strMoveToDispose = "";
					String strMoveToDisposeDetails = "";
					if (strBioTransferDetailsCode != "" && strBioTransferDetailsCode != null) {
						strTransferDirectTransfer.append("update biorequestbasedtransferdetails set ntransferstatus=")
								.append(Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus())
								.append(" where nbiorequestbasedtransfercode=").append(nBioRequestBasedTransferCode)
								.append(" and nsitecode=").append(userInfo.getNtranssitecode()).append(" and nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(" ")
								.append(concatConditionDirectTransferDetails).append(";");

						int intBioMoveToDisposePk = jdbcTemplate.queryForObject(
								"select nsequenceno from seqnobiobankmanagement where "
										+ "stablename='biomovetodispose' and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";",
								Integer.class);
						int intBioMoveToDisposeDetailsPk = jdbcTemplate.queryForObject(
								"select nsequenceno from seqnobiobankmanagement where "
										+ "stablename='biomovetodisposedetails' and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";",
								Integer.class);

						intBioMoveToDisposePk++;
						strMoveToDispose += "insert into biomovetodispose (nbiomovetodisposecode, sformnumber,"
								+ " ntransfertypecode, nformtypecode, nthirdpartycode, ntransactionstatus, noriginsitecode, sremarks,"
								+ " dtransactiondate, ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)"
								+ " select " + intBioMoveToDisposePk + ", sformnumber, ntransfertypecode, 1, -1, "
								+ Enumeration.TransactionStatus.SENT.gettransactionstatus() + ", "
								+ userInfo.getNtranssitecode() + ", jsondata->>'sremarks' sremarks, '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
								+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
								+ userInfo.getNtranssitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " from biorequestbasedtransfer where nsitecode=" + userInfo.getNtranssitecode()
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
								+ " nbiorequestbasedtransfercode=" + nBioRequestBasedTransferCode + ";";
						strMoveToDispose += " update seqnobiobankmanagement set nsequenceno=" + intBioMoveToDisposePk
								+ " where stablename='biomovetodispose' and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

						// modified by sujatha ATE_274 for adding 4 new keys into the jsondata column
						// bgsi-218
						strMoveToDisposeDetails += "insert into biomovetodisposedetails (nbiomovetodisposedetailscode,"
								+ " nbiomovetodisposecode, nbioprojectcode, nbioparentsamplecode,"
								+ " nsamplestoragetransactioncode, svolume, jsondata, ncohortno, nstoragetypecode,"
								+ " nproductcatcode, nproductcode, nsamplecondition, ndiagnostictypecode, "
								+ " ncontainertypecode, nsamplestatus, nreasoncode, dtransactiondate, "
								+ " ntztransactiondate, noffsetdtransactiondate, nsitecode, nstatus)" + " select "
								+ intBioMoveToDisposeDetailsPk
								+ "+rank()over(order by nbiorequestbasedtransferdetailcode), " + intBioMoveToDisposePk
								+ ", nbioprojectcode, nbioparentsamplecode, -1, svolume, jsonb_build_object('sparentsamplecode',"
								+ " jsondata->>'sparentsamplecode', 'srepositoryid', srepositoryid, 'ssubjectid', jsondata->>'ssubjectid', "
								+ " 'scasetype', jsondata->>'scasetype', 'slocationcode', slocationcode, 'sextractedsampleid', jsondata->>'sextractedsampleid',"
								+ " 'sconcentration', jsondata->>'sconcentration', 'sqcplatform', jsondata->>'sqcplatform', 'seluent', jsondata->>'seluent'"
								+ " ), ncohortno, nstoragetypecode, nproductcatcode, nproductcode, "
								+ " nsamplecondition, ndiagnostictypecode, ncontainertypecode, "
								+ Enumeration.TransactionStatus.MOVETODISPOSE.gettransactionstatus() + ", nreasoncode, "
								+ " '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
								+ userInfo.getNtimezonecode() + ", "
								+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
								+ userInfo.getNtranssitecode() + ", "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " from biorequestbasedtransferdetails where nsitecode=" + userInfo.getNtranssitecode()
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and"
								+ " nbiorequestbasedtransfercode=" + nBioRequestBasedTransferCode
								+ concatConditionDirectTransferDetails + ";";
						strMoveToDisposeDetails += "update seqnobiobankmanagement set nsequenceno=(select "
								+ intBioMoveToDisposeDetailsPk
								+ "+ count(nbiorequestbasedtransferdetailcode) from biorequestbasedtransferdetails where "
								+ " nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nbiorequestbasedtransfercode=" + nBioRequestBasedTransferCode
								+ concatConditionDirectTransferDetails + ") where stablename='biomovetodisposedetails'"
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
					}

					// need to delete
					final var strDeleteSampleStorageTransaction = "delete from samplestoragetransaction"
							+ " where nsamplestoragetransactioncode in "
							+ "(select sst.nsamplestoragetransactioncode from biorequestbasedtransfer bdt,"
							+ " biorequestbasedtransferdetails bdtd, samplestoragetransaction sst where"
							+ " bdt.nbiorequestbasedtransfercode=bdtd.nbiorequestbasedtransfercode"
							+ " and bdt.nsitecode=bdtd.nsitecode"
							+ " and bdtd.nbiosamplereceivingcode=sst.nbiosamplereceivingcode"
							+ " and bdtd.nsitecode=sst.nsitecode and bdt.nsitecode=" + userInfo.getNtranssitecode()
							+ " and bdt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and bdtd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and sst.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and bdt.nbiorequestbasedtransfercode=" + nBioRequestBasedTransferCode + " "
							+ concatConditionTransferDetailsNot + ");";

					// added by sujatha ATE_274 for deleting samplestorageadditionalinfo while
					// deleting in samplestoragetransaction bgsi-218
					final var strDeleteSampleRetrievalAdditionalInfo = "delete from samplestorageadditionalinfo where nsamplestoragetransactioncode in ("
							+ " select sst.nsamplestoragetransactioncode from biorequestbasedtransfer bdt,"
							+ " biorequestbasedtransferdetails bdtd, samplestoragetransaction sst where"
							+ " bdt.nbiorequestbasedtransfercode=bdtd.nbiorequestbasedtransfercode"
							+ " and bdt.nsitecode=bdtd.nsitecode"
							+ " and bdtd.nbiosamplereceivingcode=sst.nbiosamplereceivingcode"
							+ " and bdtd.nsitecode=sst.nsitecode and bdt.nsitecode=" + userInfo.getNtranssitecode()
							+ " and bdt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and bdtd.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and sst.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and bdt.nbiorequestbasedtransfercode=" + nBioRequestBasedTransferCode + " "
							+ concatConditionTransferDetailsNot + ");";

					var seqNoBioRequetBasedTransferHistory = jdbcTemplate.queryForObject(
							"select nsequenceno from seqnobiobankmanagement where stablename='biorequestbasedtransferhistory' and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
							Integer.class);
					seqNoBioRequetBasedTransferHistory++;

					final var strInsertRequestTransferHistory = "INSERT INTO public.biorequestbasedtransferhistory("
							+ " nbiorequestbasedtransferhistorycode, nbiorequestbasedtransfercode, sformnumber, "
							+ " ntransfertypecode, ntransactionstatus, dtransactiondate, ntztransactiondate, "
							+ " noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ndeputyuserrolecode, nsitecode, nstatus)"
							+ " SELECT " + seqNoBioRequetBasedTransferHistory + ", " + nBioRequestBasedTransferCode
							+ ", sformnumber,  ntransfertypecode ,"
							+ Enumeration.TransactionStatus.SENT.gettransactionstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNtimezonecode()
							+ ", " + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
							+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", "
							+ userInfo.getNdeputyusercode() + ", " + userInfo.getNdeputyuserrole() + ", "
							+ userInfo.getNtranssitecode() + ", "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " FROM biorequestbasedtransfer WHERE nbiorequestbasedtransfercode = "
							+ nBioRequestBasedTransferCode + ";";

					final StringBuilder strSeqNoUpdate = new StringBuilder();

					strSeqNoUpdate.append("update seqnobiobankmanagement set nsequenceno=")
							.append(seqNoBioRequetBasedTransferHistory).append(" where")
							.append(" stablename='biorequestbasedtransferhistory' and nstatus=")
							.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");

					// modified by sujatha ATE_274 by adding insert and delete script for
					// samplestorageadditionalinfo & sampleretrieveladditionalinfo bgsi-218
					jdbcTemplate.execute(strTransferDirectTransfer.append(strSampleStorageRetrieval.toString())
							.append(strSampleRetrivalAdditionalinfo).append(strDeleteSampleRetrievalAdditionalInfo)
							.append(strDeleteSampleStorageTransaction).append(strBioFormAcceptance.toString())
							.append(strBioFormAcceptanceDetails.toString()).toString());

					jdbcTemplate.execute(strMoveToDispose + strMoveToDisposeDetails + strInsertRequestTransferHistory
							+ strSeqNoUpdate);
				
					
					// ===== COC: START =====
					{
					    String sQuery1 = " lock  table lockregister " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					    jdbcTemplate.execute(sQuery1);

					    String sQuery2 = " lock  table lockquarantine " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					    jdbcTemplate.execute(sQuery2);

					    String sQuery3 = "lock lockcancelreject" + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
					    jdbcTemplate.execute(sQuery3);

					    int chainCustodyPk = jdbcTemplate.queryForObject(
					            "select nsequenceno from seqnoregistration where stablename='chaincustody' and nstatus="
					                    + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					            Integer.class);

					    String whereClause = " brtd.nbiorequestbasedtransfercode = " + nBioRequestBasedTransferCode
					            + " and brtd.nsitecode = " + userInfo.getNtranssitecode()
					            + " and brtd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					            + " and brtd.ntransferstatus = " + Enumeration.TransactionStatus.SENT.gettransactionstatus();

					    String strChainCustody = "insert into chaincustody ("
					            + " nchaincustodycode, nformcode, ntablepkno, stablepkcolumnname, stablename, sitemno,"
					            + " ntransactionstatus, nusercode, nuserrolecode, dtransactiondate, ntztransactiondate,"
					            + " noffsetdtransactiondate, sremarks, nsitecode, nstatus)"
					            + " select " + chainCustodyPk
					            + " + rank() over(order by brtd.nbiorequestbasedtransferdetailcode), "
					            + userInfo.getNformcode() + ", "
					            + " brtd.nbiorequestbasedtransferdetailcode, 'nbiorequestbasedtransferdetailcode', "
					            + " 'biorequestbasedtransferdetails', COALESCE(brtd.srepositoryid, ''), "
					            + Enumeration.TransactionStatus.SENT.gettransactionstatus() + ", "
					            + userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", '"
					            + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					            + userInfo.getNtimezonecode() + ", "
					            + dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					            + " ('"
					            + commonFunction.getMultilingualMessage("IDS_REPOSITORYID", userInfo.getSlanguagefilename())
					                    .replace("'", "''")
					            + " [' || COALESCE(brtd.srepositoryid,'') || '] ' || '"
					            + commonFunction.getMultilingualMessage("IDS_PARENTSAMPLECODE", userInfo.getSlanguagefilename())
					                    .replace("'", "''")
					            + " [' || COALESCE(brtd.jsondata->>'sparentsamplecode','') || '] ' || '"
					            + commonFunction.getMultilingualMessage("IDS_SENTFROM", userInfo.getSlanguagefilename())
					                    .replace("'", "''")
					            + " [' || COALESCE(sfrom.ssitename,'') || '] ' || '"
					            + commonFunction.getMultilingualMessage("IDS_SENTTO", userInfo.getSlanguagefilename())
					                    .replace("'", "''")
					            + " [' || COALESCE(sto.ssitename,'') || '] ' || '"
					            + commonFunction.getMultilingualMessage("IDS_INFORMNUMBER", userInfo.getSlanguagefilename())
					                    .replace("'", "''")
					            + " [' || COALESCE(brt.sformnumber,'') || ']'), "
					            + userInfo.getNtranssitecode() + ", "
					            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					            + " from biorequestbasedtransferdetails brtd"
					            + " join biorequestbasedtransfer brt on brt.nbiorequestbasedtransfercode = brtd.nbiorequestbasedtransfercode"
					            + " and brt.nsitecode = " + userInfo.getNtranssitecode()
					            + " and brt.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					            + " join site sfrom on sfrom.nsitecode = brt.nsitecode"
					            + " join site sto on sto.nsitecode = brt.nreceiversitecode"
					            + " where " + whereClause
					            + ";";

					    String strSeqUpdateCOC = "update seqnoregistration set nsequenceno = "
					            + chainCustodyPk + " + (select count(*) from biorequestbasedtransferdetails brtd"
					            + " where " + whereClause + ")"
					            + " where stablename = 'chaincustody' and nstatus = "
					            + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

					    jdbcTemplate.execute(strChainCustody);
					    jdbcTemplate.execute(strSeqUpdateCOC);
					}
					// ===== COC: END =====
					
					
					if (objNthirdParty.getNtransfertypecode() == Enumeration.TransferType.BIOBANKEXTERNAL
							.getntransfertype()
							|| objNthirdParty.getNtransfertypecode() == Enumeration.TransferType.THIRDPARTY
									.getntransfertype()) {

						var seqNoBioThirdPartyFormAccpet = jdbcTemplate.queryForObject(
								"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyformaccept' and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
								Integer.class);

						var seqNoBioThirdPartyFormAccpetHistory = jdbcTemplate.queryForObject(
								"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyformaccepthistory' and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
								Integer.class);

						var seqNoBioThirdPartyFormAccpetDetails = jdbcTemplate.queryForObject(
								"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyformacceptdetails' and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
								Integer.class);

						var seqNoBioThirdPartyFormAccpetDetailHistory = jdbcTemplate.queryForObject(
								"select nsequenceno from seqnobiobankmanagement where stablename='biothirdpartyformacceptdetailshistory' and nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
								Integer.class);

						seqNoBioThirdPartyFormAccpet++;

						seqNoBioThirdPartyFormAccpetHistory++;

						// seqNoBioThirdPartyFormAccpetDetails++;

						// seqNoBioThirdPartyFormAccpetDetailHistory++;

						final StringBuilder sbBioThirdpartyAccpet = new StringBuilder();

						sbBioThirdpartyAccpet.append("INSERT INTO biothirdpartyformaccept (");
						sbBioThirdpartyAccpet.append("nbiothirdpartyformacceptancecode, ");
						sbBioThirdpartyAccpet.append("nbiorequestbasedtransfercode, ");
						sbBioThirdpartyAccpet.append("sformnumber, ");
						sbBioThirdpartyAccpet.append("ntransfertypecode, ");
						sbBioThirdpartyAccpet.append("nformtypecode, ");
						sbBioThirdpartyAccpet.append("nthirdpartycode, ");
						sbBioThirdpartyAccpet.append("noriginsitecode, ");
						sbBioThirdpartyAccpet.append("nsenderusercode, ");
						sbBioThirdpartyAccpet.append("nsenderuserrolecode, ");
						sbBioThirdpartyAccpet.append("dtransferdate, ");
						sbBioThirdpartyAccpet.append("ntztransferdate, ");
						sbBioThirdpartyAccpet.append("noffsetdtransferdate, ");
						sbBioThirdpartyAccpet.append("ntransactionstatus, ");
						sbBioThirdpartyAccpet.append("nstorageconditioncode, ");
						sbBioThirdpartyAccpet.append("ddeliverydate, ");
						sbBioThirdpartyAccpet.append("ntzdeliverydate, ");
						sbBioThirdpartyAccpet.append("noffsetddeliverydate, ");
						sbBioThirdpartyAccpet.append("ndispatchercode, ");
						sbBioThirdpartyAccpet.append("ncouriercode, ");
						sbBioThirdpartyAccpet.append("dreceiveddate, ");
						sbBioThirdpartyAccpet.append("ntzreceiveddate, ");
						sbBioThirdpartyAccpet.append("noffsetdreceiveddate, ");
						sbBioThirdpartyAccpet.append("nreceivingtemperaturecode, ");
						sbBioThirdpartyAccpet.append("nreceivingofficercode, ");
						sbBioThirdpartyAccpet.append("jsondata, ");
						sbBioThirdpartyAccpet.append("dtransactiondate, ");
						sbBioThirdpartyAccpet.append("ntztransactiondate, ");
						sbBioThirdpartyAccpet.append("noffsetdtransactiondate, ");
						sbBioThirdpartyAccpet.append("nsitecode, ");
						sbBioThirdpartyAccpet.append("nstatus) ");

						sbBioThirdpartyAccpet.append("SELECT ");
						sbBioThirdpartyAccpet.append("" + seqNoBioThirdPartyFormAccpet + ", ");
						sbBioThirdpartyAccpet.append("brt.nbiorequestbasedtransfercode, ");
						sbBioThirdpartyAccpet.append("brt.sformnumber, ");
						sbBioThirdpartyAccpet.append("brt.ntransfertypecode, ");
						sbBioThirdpartyAccpet
								.append("" + Enumeration.FormType.Transfer.getnformtype() + " AS nformtypecode, ");
						sbBioThirdpartyAccpet.append("brt.nthirdpartycode, ");
						sbBioThirdpartyAccpet.append("brt.nreceiversitecode AS noriginsitecode, ");
						sbBioThirdpartyAccpet.append("" + userInfo.getNusercode() + " AS nsenderusercode, ");
						sbBioThirdpartyAccpet.append("" + userInfo.getNuserrole() + " AS nsenderuserrolecode, ");
						sbBioThirdpartyAccpet.append("brt.dtransferdate, ");
						sbBioThirdpartyAccpet.append("brt.ntztransferdate, ");
						sbBioThirdpartyAccpet.append("brt.noffsetdtransferdate, ");
						sbBioThirdpartyAccpet
								.append("" + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", ");
						sbBioThirdpartyAccpet.append("brt.nstorageconditioncode, ");
						sbBioThirdpartyAccpet.append("brt.ddeliverydate, ");
						sbBioThirdpartyAccpet.append("brt.ntzdeliverydate, ");
						sbBioThirdpartyAccpet.append("brt.noffsetddeliverydate, ");
						sbBioThirdpartyAccpet.append("brt.ndispatchercode, ");
						sbBioThirdpartyAccpet.append("brt.ncouriercode, ");
						sbBioThirdpartyAccpet.append("NULL AS dreceiveddate, ");
						sbBioThirdpartyAccpet.append(
								"" + Enumeration.TransactionStatus.NA.gettransactionstatus() + " AS ntzreceiveddate, ");
						sbBioThirdpartyAccpet.append("0 AS noffsetdreceiveddate, ");
						sbBioThirdpartyAccpet.append("" + Enumeration.TransactionStatus.NA.gettransactionstatus()
								+ " AS nreceivingtemperaturecode, ");
						sbBioThirdpartyAccpet.append("" + Enumeration.TransactionStatus.NA.gettransactionstatus()
								+ " AS nreceivingofficercode, ");
						sbBioThirdpartyAccpet.append("brt.jsondata || json_build_object('soriginsitename','"
								+ userInfo.getSsitename() + "')::jsonb , ");
						sbBioThirdpartyAccpet.append("brt.dtransactiondate, ");
						sbBioThirdpartyAccpet.append("brt.ntztransactiondate, ");
						sbBioThirdpartyAccpet.append("brt.noffsetdtransactiondate, ");
						sbBioThirdpartyAccpet.append("brt.nsitecode, ");
						sbBioThirdpartyAccpet.append("brt.nstatus ");
						sbBioThirdpartyAccpet.append("FROM biorequestbasedtransfer brt ");
						sbBioThirdpartyAccpet.append("WHERE brt.nbiorequestbasedtransfercode ="
								+ nBioRequestBasedTransferCode + " AND brt.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  ;");

						sbBioThirdpartyAccpet.append("INSERT INTO biothirdpartyformaccepthistory (");
						sbBioThirdpartyAccpet.append("nbiothirdpartyformacceptancehistorycode, ");
						sbBioThirdpartyAccpet.append("nbiothirdpartyformacceptancecode, ");
						sbBioThirdpartyAccpet.append("ntransactionstatus, ");
						sbBioThirdpartyAccpet.append("dtransactiondate, ");
						sbBioThirdpartyAccpet.append("ntztransactiondate, ");
						sbBioThirdpartyAccpet.append("noffsetdtransactiondate, ");
						sbBioThirdpartyAccpet.append("nusercode, ");
						sbBioThirdpartyAccpet.append("nuserrolecode, ");
						sbBioThirdpartyAccpet.append("ndeputyusercode, ");
						sbBioThirdpartyAccpet.append("ndeputyuserrolecode, ");
						sbBioThirdpartyAccpet.append("nsitecode, ");
						sbBioThirdpartyAccpet.append("nstatus) ");

						sbBioThirdpartyAccpet.append("VALUES (").append(seqNoBioThirdPartyFormAccpetHistory)
								.append(", ").append(seqNoBioThirdPartyFormAccpet).append(", ")
								.append(Enumeration.TransactionStatus.DRAFT.gettransactionstatus()).append(", '")
								.append(dateUtilityFunction.getCurrentDateTime(userInfo)).append("', ")
								.append(userInfo.getNtimezonecode()).append(", ")
								.append(dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()))
								.append(", ").append(userInfo.getNusercode()).append(", ")
								.append(userInfo.getNuserrole()).append(", ").append(userInfo.getNdeputyusercode())
								.append(", ").append(userInfo.getNdeputyuserrole()).append(", ")
								.append(userInfo.getNtranssitecode()).append(", ")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(");");

						final StringBuilder strInsertThirdPartyFormAcceptDetails = new StringBuilder();

						strInsertThirdPartyFormAcceptDetails
								.append("INSERT INTO public.biothirdpartyformacceptdetails (");
						strInsertThirdPartyFormAcceptDetails.append("nbiothirdpartyformacceptancedetailscode, ");
						strInsertThirdPartyFormAcceptDetails.append("nbiothirdpartyformacceptancecode, ");
						strInsertThirdPartyFormAcceptDetails.append("nbioprojectcode, ");
						strInsertThirdPartyFormAcceptDetails.append("nbioparentsamplecode, ");
						strInsertThirdPartyFormAcceptDetails.append("ncohortno, ");
						strInsertThirdPartyFormAcceptDetails.append("nstoragetypecode, ");
						strInsertThirdPartyFormAcceptDetails.append("nproductcatcode, ");
						strInsertThirdPartyFormAcceptDetails.append("nproductcode, ");
						strInsertThirdPartyFormAcceptDetails.append("srepositoryid, ");
						strInsertThirdPartyFormAcceptDetails.append("svolume, ");
						strInsertThirdPartyFormAcceptDetails.append("sreceivedvolume, ");
						strInsertThirdPartyFormAcceptDetails.append("ssubjectid, ");
						strInsertThirdPartyFormAcceptDetails.append("sparentsamplecode, ");
						strInsertThirdPartyFormAcceptDetails.append("jsondata, ");
						strInsertThirdPartyFormAcceptDetails.append("ndiagnostictypecode, ");
						strInsertThirdPartyFormAcceptDetails.append("ncontainertypecode, ");
						strInsertThirdPartyFormAcceptDetails.append("nbiosamplereceivingcode, ");
						strInsertThirdPartyFormAcceptDetails.append("nsamplecondition, ");
						strInsertThirdPartyFormAcceptDetails.append("nsamplestatus, ");
						strInsertThirdPartyFormAcceptDetails.append("nreasoncode, ");
						strInsertThirdPartyFormAcceptDetails.append("nsamplestoragetransactioncode, ");
						strInsertThirdPartyFormAcceptDetails.append("dtransactiondate, ");
						strInsertThirdPartyFormAcceptDetails.append("ntztransactiondate, ");
						strInsertThirdPartyFormAcceptDetails.append("noffsetdtransactiondate, ");
						strInsertThirdPartyFormAcceptDetails.append("nsitecode, ");
						strInsertThirdPartyFormAcceptDetails.append("nstatus) ");

						strInsertThirdPartyFormAcceptDetails.append("SELECT ");
						strInsertThirdPartyFormAcceptDetails.append("" + seqNoBioThirdPartyFormAccpetDetails
								+ " + rank()over (order by brtd.nbiorequestbasedtransferdetailcode ) AS nbiothirdpartyformacceptancedetailscode, ");
						strInsertThirdPartyFormAcceptDetails.append("" + seqNoBioThirdPartyFormAccpet + ", ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.nbioprojectcode, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.nbioparentsamplecode, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.ncohortno, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.nstoragetypecode, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.nproductcatcode, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.nproductcode, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.srepositoryid, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.svolume, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.svolume, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.jsondata->>'ssubjectid', ");
						strInsertThirdPartyFormAcceptDetails
								.append("brtd.jsondata->>'sparentsamplecode' AS sparentsamplecode, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.jsondata, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.ndiagnostictypecode, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.ncontainertypecode, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.nbiosamplereceivingcode, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.nsamplecondition, ");
						strInsertThirdPartyFormAcceptDetails
								.append("" + Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.nreasoncode, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.nsamplestoragetransactioncode, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.dtransactiondate, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.ntztransactiondate, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.noffsetdtransactiondate, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.nsitecode, ");
						strInsertThirdPartyFormAcceptDetails.append("brtd.nstatus ");
						strInsertThirdPartyFormAcceptDetails.append("FROM public.biorequestbasedtransferdetails brtd ");
						strInsertThirdPartyFormAcceptDetails.append("JOIN public.biothirdpartyformaccept bfa ");
						strInsertThirdPartyFormAcceptDetails
								.append("ON bfa.nbiorequestbasedtransfercode = brtd.nbiorequestbasedtransfercode ");
						strInsertThirdPartyFormAcceptDetails.append("WHERE brtd.nbiorequestbasedtransfercode = "
								+ nBioRequestBasedTransferCode + " AND brtd.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ");
						strInsertThirdPartyFormAcceptDetails.append(" and brtd.ntransferstatus=");
						strInsertThirdPartyFormAcceptDetails
								.append(Enumeration.TransactionStatus.SENT.gettransactionstatus() + "; ");

						strInsertThirdPartyFormAcceptDetails
								.append("INSERT INTO public.biothirdpartyformacceptdetailshistory (")
								.append("nbiothirdpartyformacceptdetailshistorycode, nbiothirdpartyformacceptancedetailscode, ")
								.append("nbiothirdpartyformacceptancecode, nsamplecondition, nsamplestatus, dtransactiondate, ")
								.append("ntztransactiondate, noffsetdtransactiondate, nusercode, nuserrolecode, ndeputyusercode, ")
								.append("ndeputyuserrolecode, nsitecode, nstatus) ").append("SELECT ")
								.append(seqNoBioThirdPartyFormAccpetDetailHistory
										+ " + rank()over(order by d.nbiothirdpartyformacceptancedetailscode ) AS nbiothirdpartyformacceptdetailshistorycode")
								.append(", ")
								.append("d.nbiothirdpartyformacceptancedetailscode, d.nbiothirdpartyformacceptancecode, ")
								.append("d.nsamplecondition, "
										+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ", '")
								.append(dateUtilityFunction.getCurrentDateTime(userInfo))
								.append("' AS dtransactiondate, ").append(userInfo.getNtimezonecode()).append(", ")
								.append(dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()))
								.append(", ").append(userInfo.getNusercode()).append(", ")
								.append(userInfo.getNuserrole()).append(", ").append(userInfo.getNdeputyusercode())
								.append(", ").append(userInfo.getNdeputyuserrole()).append(", ")
								.append("d.nsitecode, d.nstatus ")
								.append("FROM public.biothirdpartyformacceptdetails d ")
								.append("WHERE d.nbiothirdpartyformacceptancecode IN (")
								.append("SELECT nbiothirdpartyformacceptancecode FROM public.biothirdpartyformaccept bfa ")
								.append("WHERE bfa.nbiorequestbasedtransfercode = ")
								.append(nBioRequestBasedTransferCode).append(" AND bfa.nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ) ; ");

//						strInsertThirdPartyFormAcceptDetails.append("INSERT INTO biothirdpartyformacceptdetailshistory (");
//						strInsertThirdPartyFormAcceptDetails.append("nbiothirdpartyformacceptdetailshistorycode, ");
//						strInsertThirdPartyFormAcceptDetails.append("nbiothirdpartyformacceptancedetailscode, ");
//						strInsertThirdPartyFormAcceptDetails.append("nbiothirdpartyformacceptancecode, ");
//						strInsertThirdPartyFormAcceptDetails.append("nsamplecondition, ");
//						strInsertThirdPartyFormAcceptDetails.append("nsamplestatus, ");
//						strInsertThirdPartyFormAcceptDetails.append("dtransactiondate, ");
//						strInsertThirdPartyFormAcceptDetails.append("ntztransactiondate, ");
//						strInsertThirdPartyFormAcceptDetails.append("noffsetdtransactiondate, ");
//						strInsertThirdPartyFormAcceptDetails.append("nusercode, ");
//						strInsertThirdPartyFormAcceptDetails.append("nuserrolecode, ");
//						strInsertThirdPartyFormAcceptDetails.append("ndeputyusercode, ");
//						strInsertThirdPartyFormAcceptDetails.append("ndeputyuserrolecode, ");
//						strInsertThirdPartyFormAcceptDetails.append("nsitecode, ");
//						strInsertThirdPartyFormAcceptDetails.append("nstatus) ");
//
//						strInsertThirdPartyFormAcceptDetails.append("VALUES (").append(seqNoBioThirdPartyFormAccpetDetailHistory+" rank()over(order by nbiorequestbasedtransferdetailcode )").append(", ")
//						.append(seqNoBioThirdPartyFormAccpetDetails).append(", ")
//						.append(seqNoBioThirdPartyFormAccpet).append(", ")
//						.append(Enumeration.TransactionStatus.SENT.gettransactionstatus()).append(", '")
//						.append(dateUtilityFunction.getCurrentDateTime(userInfo)).append("', ")
//						.append(userInfo.getNtimezonecode()).append(", ")
//						.append(dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()))
//						.append(", ").append(userInfo.getNusercode()).append(", ").append(userInfo.getNuserrole())
//						.append(", ").append(userInfo.getNdeputyusercode()).append(", ")
//						.append(userInfo.getNdeputyuserrole()).append(", ").append(userInfo.getNtranssitecode())
//						.append(", ").append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
//						.append(");");

						final StringBuilder strSeqNoUpdateThirdParty = new StringBuilder();

						strSeqNoUpdateThirdParty.append("UPDATE seqnobiobankmanagement SET nsequenceno = ")
								.append(seqNoBioThirdPartyFormAccpet)
								.append(" WHERE stablename = 'biothirdpartyformaccept' AND nstatus = ")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append("; ");

//						strSeqNoUpdateThirdParty.append("UPDATE seqnobiobankmanagement SET nsequenceno = ")
//						    .append(seqNoBioThirdPartyFormAccpetDetails)
//						    .append(" WHERE stablename = 'biothirdpartyformacceptdetails' AND nstatus = ")
//						    .append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
//						    .append("; ");
						strSeqNoUpdateThirdParty
								.append("UPDATE seqnobiobankmanagement SET nsequenceno=(SELECT "
										+ seqNoBioThirdPartyFormAccpetDetails
										+ "+COUNT(nbiorequestbasedtransferdetailcode) ")
								.append("FROM biorequestbasedtransferdetails WHERE nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
								.append(" AND nbiorequestbasedtransfercode=").append(nBioRequestBasedTransferCode)
								.append(") WHERE stablename='biothirdpartyformacceptdetails' AND nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");

						strSeqNoUpdateThirdParty.append("UPDATE seqnobiobankmanagement SET nsequenceno = ")
								.append(seqNoBioThirdPartyFormAccpetHistory)
								.append(" WHERE stablename = 'biothirdpartyformaccepthistory' AND nstatus = ")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");

//							strSeqNoUpdateThirdParty.append("UPDATE seqnobiobankmanagement SET nsequenceno = ")
//						    .append(seqNoBioThirdPartyFormAccpetDetailHistory)
//						    .append(" WHERE stablename = 'biothirdpartyformacceptdetailshistory' AND nstatus = ")
//						    .append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
//						    .append("; ");

						strSeqNoUpdateThirdParty
								.append("UPDATE seqnobiobankmanagement SET nsequenceno=(SELECT "
										+ seqNoBioThirdPartyFormAccpetDetailHistory
										+ "+COUNT(nbiorequestbasedtransferdetailcode) ")
								.append("FROM biorequestbasedtransferdetails WHERE nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus())
								.append(" AND nbiorequestbasedtransfercode=").append(nBioRequestBasedTransferCode)
								.append(") WHERE stablename='biothirdpartyformacceptdetailshistory' AND nstatus=")
								.append(Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()).append(";");

						jdbcTemplate.execute(
								sbBioThirdpartyAccpet.toString() + strInsertThirdPartyFormAcceptDetails.toString()
										+ strSeqNoUpdateThirdParty.toString());

					}

					inputMap.put("ntransCode", Enumeration.TransactionStatus.SENT.gettransactionstatus());

					final var strAuditQryRepoAfter = "SELECT   STRING_AGG(brdt.srepositoryid, ', ' ORDER BY brdt.nbiorequestbasedtransferdetailcode) AS srepositoryid,"
							+ "    ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
							+ "' AS stransdisplaystatus FROM biorequestbasedtransferdetails brdt "
							+ "JOIN  transactionstatus ts  ON brdt.ntransferstatus = ts.ntranscode and brdt.nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
							+ "and brdt.ntransferstatus="
							+ Enumeration.TransactionStatus.DISPOSED.gettransactionstatus()
							+ " and brdt.nbiorequestbasedtransfercode=" + nBioRequestBasedTransferCode + " "
							+ "GROUP BY  ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
							+ "' ;";

					final var lstAuditAfter = (BioRequestBasedTransfer) jdbcTemplateUtilityFunction
							.queryForObject(strAuditQryForm, BioRequestBasedTransfer.class, jdbcTemplate);

					final var auditAfterRepo = (BioRequestBasedTransfer) jdbcTemplateUtilityFunction
							.queryForObject(strAuditQryRepoAfter, BioRequestBasedTransfer.class, jdbcTemplate);

					final List<Object> defaultListAfterSave = new ArrayList<>();
					final List<Object> defaultListBeforeSave = new ArrayList<>();

					if (auditAfterRepo != null) {
						lstAuditAfter.setSrepositoryid(auditAfterRepo.getSrepositoryid());
						lstAuditAfter.setStransdisplaystatus(auditAfterRepo.getStransdisplaystatus());
					}
					lstAuditAfter.setSformnumber("");
					defaultListBeforeSave.add(lstAuditAfter);
					defaultListAfterSave.add(lstAuditBefore);
					multilingualIDList.add("IDS_TRANSFERSAMPLES");

					auditUtilityFunction.fnInsertAuditAction(defaultListBeforeSave, 2, defaultListAfterSave,
							multilingualIDList, userInfo);

					// Added By Mullai Balaji for Email jira ID-BGSI-147
					String Query = "select DISTINCT (ncontrolcode) from emailconfig where ncontrolcode="
							+ inputMap.get("ncontrolcode") + " " + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					Integer value = null;
					try {
						value = jdbcTemplate.queryForObject(Query, Integer.class);
					} catch (Exception e) {
						value = null;
					}

					if (value != null) {
						final Map<String, Object> mailMap = new HashMap<String, Object>();
						mailMap.put("ncontrolcode", inputMap.get("ncontrolcode"));
						mailMap.put("nbiorequestbasedtransfercode", nBioRequestBasedTransferCode);

						String receiverquery = "Select nreceiversitecode from biorequestbasedtransfer where nbiorequestbasedtransfercode= "
								+ nBioRequestBasedTransferCode + " and nstatus ="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						Integer nreceiversitecode = jdbcTemplate.queryForObject(receiverquery, Integer.class);

						mailMap.put("nreceiversitecode", nreceiversitecode);

						String query = "SELECT sformnumber FROM biorequestbasedtransfer where nbiorequestbasedtransfercode="
								+ nBioRequestBasedTransferCode;
						String referenceId = jdbcTemplate.queryForObject(query, String.class);
						mailMap.put("ssystemid", referenceId);
						final UserInfo mailUserInfo = new UserInfo(userInfo);
						mailMap.put("ntranssitecode", (int) mailUserInfo.getNtranssitecode());
						mailMap.put("nmastersitecode", (int) mailUserInfo.getNmastersitecode());
						emailDAOSupport.createEmailAlertTransaction(mailMap, mailUserInfo);
					}

					return getActiveBioRequestBasedTransfer(inputMap, nBioRequestBasedTransferCode, userInfo);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_NOAVAILABLESAMPLESTOTRANSFER",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}

			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_REMOVEREJECTEDSAMPLESTOTRANSFER",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateBioRequestBasedTransfer(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final var nbioRequestBasedTransferCode = (int) inputMap.get("nbiorequestbasedtransfercode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> listAfterSave = new ArrayList<>();
		final List<Object> listBeforeSave = new ArrayList<>();

		final var findStatus = findStatusRequestBasedtTransfer(nbioRequestBasedTransferCode, userInfo);

//		final var strAuditQry = auditQuery(nbioRequestBasedTransferCode, "", userInfo);
		final var strAuditQry = "select sformnumber, to_char(dtransferdate, '" + userInfo.getSsitedate()
				+ "') stransferdate, jsondata->>'sremarks' sremarks "
				+ " from biorequestbasedtransfer where nbiorequestbasedtransfercode=" + nbioRequestBasedTransferCode
				+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";
		final var lstAuditBefore = (BioRequestBasedTransfer) jdbcTemplateUtilityFunction.queryForObject(strAuditQry,
				BioRequestBasedTransfer.class, jdbcTemplate);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				|| findStatus == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {
			var stransferDate = (String) inputMap.get("stransferdate");
			final var sremarks = (String) inputMap.get("sremarks");

			stransferDate = (stransferDate != null && !stransferDate.isEmpty())
					? "'" + stransferDate.toString().replace("T", " ").replace("Z", "") + "'"
					: null;

			final var strUpdateQry = "update biorequestbasedtransfer set dtransferdate=" + stransferDate
					+ ", jsondata=jsondata || '{\"sremarks\": \"" + stringUtilityFunction.replaceQuote(sremarks)
					+ "\"}' where nbiorequestbasedtransfercode=" + nbioRequestBasedTransferCode + " and nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			jdbcTemplate.execute(strUpdateQry);

			final var lstAuditAfter = (BioRequestBasedTransfer) jdbcTemplateUtilityFunction.queryForObject(strAuditQry,
					BioRequestBasedTransfer.class, jdbcTemplate);

			multilingualIDList.add("IDS_EDITTRANSFERFORM");
			listBeforeSave.add(lstAuditBefore);
			listAfterSave.add(lstAuditAfter);

			auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList, userInfo);
			return getBioRequestBasedTransfer(inputMap, nbioRequestBasedTransferCode, userInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTVALIDATEDRECORD",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<Object> updateSampleCondition(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<>();

		final var nbioRequestBasedTransferCode = (int) inputMap.get("nbiorequestbasedtransfercode");
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> lstAuditBefore = new ArrayList<>();
		final List<Object> lstAuditAfter = new ArrayList<>();

		final var findStatus = findStatusRequestBasedtTransfer(nbioRequestBasedTransferCode, userInfo);

		if (findStatus == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {
			final var nbioRequestBasedtTransferDetailsCode = (String) inputMap
					.get("nbiorequestbasedtransferdetailcode");
			final var nreasonCode = (int) inputMap.get("nreasoncode");
			final var nsampleCondition = (int) inputMap.get("nsamplecondition");

			final var strAuditAfterQry = auditQuery(nbioRequestBasedTransferCode, nbioRequestBasedtTransferDetailsCode,
					userInfo);
			final List<BioRequestBasedTransferDetails> lstAuditTransferDetailsBefore = jdbcTemplate
					.query(strAuditAfterQry, new BioRequestBasedTransferDetails());

			final var strUpdateQry = "update biorequestbasedtransferdetails set nreasoncode=" + nreasonCode
					+ ", nsamplecondition=" + nsampleCondition + " where nbiorequestbasedtransferdetailcode in ("
					+ nbioRequestBasedtTransferDetailsCode + ") and nsitecode=" + userInfo.getNtranssitecode()
					+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			final int seqNoBioRequestTransferDetailsHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='bioreqbasedtransdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			var strInsertRequestTransferDetailsHistory = "INSERT INTO bioreqbasedtransdetailshistory("
					+ "nbioreqbasedtransdetailshistorycode, " + "nbiorequestbasedtransferdetailcode, "
					+ "nbiorequestbasedtransfercode, " + "nsamplecondition, " + "ntransferstatus, "
					+ "dtransactiondate, " + "ntztransactiondate, " + "noffsetdtransactiondate, " + "nusercode, "
					+ "nuserrolecode, " + "ndeputyusercode, " + "ndeputyuserrolecode, " + "nsitecode, " + "nstatus) "
					+ "SELECT " + seqNoBioRequestTransferDetailsHistory
					+ " + rank() over(order by b.nbiorequestbasedtransferdetailcode), "
					+ "b.nbiorequestbasedtransferdetailcode, " + "b.nbiorequestbasedtransfercode, "
					+ "b.nsamplecondition, b.ntransferstatus, '" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "FROM biorequestbasedtransferdetails b " + "WHERE b.nbiorequestbasedtransfercode = "
					+ nbioRequestBasedTransferCode + " AND b.nbiorequestbasedtransferdetailcode IN ("
					+ nbioRequestBasedtTransferDetailsCode + " ) AND b.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";

			strInsertRequestTransferDetailsHistory += "UPDATE seqnobiobankmanagement SET nsequenceno = (SELECT "
					+ seqNoBioRequestTransferDetailsHistory + " + COUNT(nbiorequestbasedtransferdetailcode) "
					+ "FROM biorequestbasedtransferdetails " + "WHERE nbiorequestbasedtransfercode = "
					+ nbioRequestBasedTransferCode + " AND nbiorequestbasedtransferdetailcode IN ("
					+ nbioRequestBasedtTransferDetailsCode + ")) "
					+ "WHERE stablename='bioreqbasedtransdetailshistory' AND nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strUpdateQry + strInsertRequestTransferDetailsHistory);

			final List<BioRequestBasedTransferDetails> lstAuditTransferDetailsAfter = jdbcTemplate
					.query(strAuditAfterQry, new BioRequestBasedTransferDetails());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_SAMPLEVALIDATION"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			final var lstChildBioRequestbasedTransfer = getChildInitialGet(nbioRequestBasedTransferCode, userInfo);
			outputMap.put("lstChildBioRequestbasedTransfer", lstChildBioRequestbasedTransfer);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> checkAccessibleSamples(final int nbioRequestBasedTransferCode,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final String strCheckAccessibeSample = "select nbiorequestbasedtransferdetailcode, srepositoryid, nreqformtypecode from"
				+ " biorequestbasedtransferdetails brbt join biosubjectdetails bsd on bsd.ssubjectid=brbt.ssubjectid"
				+ " and bsd.nsitecode=" + userInfo.getNmastersitecode() + " and bsd.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " where brbt.nsitecode="
				+ userInfo.getNtranssitecode() + " and brbt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and brbt.nbiorequestbasedtransfercode=" + nbioRequestBasedTransferCode
				+ " and ( case when brbt.nreqformtypecode=" + Enumeration.TransferType.BIOBANK.getntransfertype()
				+ " then bsd.nissampleaccesable when brbt.nreqformtypecode="
				+ Enumeration.TransferType.THIRDPARTY.getntransfertype() + " then bsd.nisthirdpartysharable when"
				+ " brbt.nreqformtypecode=" + Enumeration.TransferType.BIOBANKEXTERNAL.getntransfertype() + " then "
				+ " bsd.nissampleaccesable end = " + Enumeration.TransactionStatus.NO.gettransactionstatus()
				+ ") order by 1 desc";
		List<Map<String, Object>> nonAccessibleSamples = jdbcTemplate.queryForList(strCheckAccessibeSample);

		outputMap.put("nonAccessibleSamples", nonAccessibleSamples);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> undoDisposeSamples(int nbioRequestBasedeTransferCode,
			String nbioRequestBasedTransferDetailsCode, UserInfo userInfo) throws Exception {

		jdbcTemplate.execute(
				"lock  table lockbiorequestbasedtransfer " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus());

		final Map<String, Object> outputMap = new HashMap<String, Object>();

		final int validatedRequestBasedTransferCode = findStatusRequestBasedtTransfer(nbioRequestBasedeTransferCode,
				userInfo);

		if (validatedRequestBasedTransferCode == Enumeration.TransactionStatus.VALIDATION.gettransactionstatus()) {

			final String strAuditAfterQry = auditQuery(nbioRequestBasedeTransferCode,
					nbioRequestBasedTransferDetailsCode, userInfo);

			final List<BioRequestBasedTransfer> lstAuditTransferDetailsBefore = jdbcTemplate.query(strAuditAfterQry,
					new BioRequestBasedTransfer());

			final String strUpdateQry = "update biorequestbasedtransferdetails set ntransferstatus="
					+ Enumeration.TransactionStatus.VALIDATION.gettransactionstatus() + ", dtransactiondate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', ntztransactiondate="
					+ userInfo.getNtimezonecode() + ", noffsetdtransactiondate="
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid())
					+ " where nbiorequestbasedtransferdetailcode in (" + nbioRequestBasedTransferDetailsCode
					+ ") and nsitecode=" + userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			final int seqNoBioRequestTransferDetailsHistory = jdbcTemplate.queryForObject(
					"select nsequenceno from seqnobiobankmanagement where stablename='bioreqbasedtransdetailshistory' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus(),
					Integer.class);

			var strInsertRequestTransferDetailsHistory = "INSERT INTO bioreqbasedtransdetailshistory("
					+ "nbioreqbasedtransdetailshistorycode, " + "nbiorequestbasedtransferdetailcode, "
					+ "nbiorequestbasedtransfercode, " + "nsamplecondition, " + "ntransferstatus, "
					+ "dtransactiondate, " + "ntztransactiondate, " + "noffsetdtransactiondate, " + "nusercode, "
					+ "nuserrolecode, " + "ndeputyusercode, " + "ndeputyuserrolecode, " + "nsitecode, " + "nstatus) "
					+ "SELECT " + seqNoBioRequestTransferDetailsHistory
					+ " + rank() over(order by b.nbiorequestbasedtransferdetailcode), "
					+ "b.nbiorequestbasedtransferdetailcode, " + "b.nbiorequestbasedtransfercode, "
					+ "b.nsamplecondition, b.ntransferstatus, '" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "', " + userInfo.getNtimezonecode() + ", "
					+ dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid()) + ", "
					+ userInfo.getNusercode() + ", " + userInfo.getNuserrole() + ", " + userInfo.getNdeputyusercode()
					+ ", " + userInfo.getNdeputyuserrole() + ", " + userInfo.getNtranssitecode() + ", "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
					+ "FROM biorequestbasedtransferdetails b " + "WHERE b.nbiorequestbasedtransfercode = "
					+ nbioRequestBasedeTransferCode + " AND b.nbiorequestbasedtransferdetailcode IN ("
					+ nbioRequestBasedTransferDetailsCode + " ) AND b.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " ;";

			strInsertRequestTransferDetailsHistory += "UPDATE seqnobiobankmanagement SET " + "nsequenceno = (SELECT "
					+ seqNoBioRequestTransferDetailsHistory + " + COUNT(nbiorequestbasedtransferdetailcode) "
					+ "FROM biorequestbasedtransferdetails " + "WHERE nbiorequestbasedtransfercode = "
					+ nbioRequestBasedeTransferCode + " AND nbiorequestbasedtransferdetailcode IN ("
					+ nbioRequestBasedTransferDetailsCode + ")) "
					+ "WHERE stablename='bioreqbasedtransdetailshistory' AND nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";

			jdbcTemplate.execute(strUpdateQry + strInsertRequestTransferDetailsHistory);

			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> lstAuditBefore = new ArrayList<>();
			final List<Object> lstAuditAfter = new ArrayList<>();

			final List<BioRequestBasedTransfer> lstAuditTransferDetailsAfter = jdbcTemplate.query(strAuditAfterQry,
					new BioRequestBasedTransfer());
			lstAuditBefore.addAll(lstAuditTransferDetailsBefore);
			lstAuditAfter.addAll(lstAuditTransferDetailsAfter);
			lstAuditTransferDetailsAfter.stream().forEach(x -> multilingualIDList.add("IDS_UNDODISPOSESAMPLES"));

			auditUtilityFunction.fnInsertAuditAction(lstAuditAfter, 2, lstAuditBefore, multilingualIDList, userInfo);

			final List<Map<String, Object>> lstChildBioRequestbasedTransfer = getChildInitialGet(
					nbioRequestBasedeTransferCode, userInfo);
			outputMap.put("lstChildBioRequestbasedTransfer", lstChildBioRequestbasedTransfer);
			return new ResponseEntity<>(outputMap, HttpStatus.OK);

		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SELECTVALIDATEDRECORD", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

}
