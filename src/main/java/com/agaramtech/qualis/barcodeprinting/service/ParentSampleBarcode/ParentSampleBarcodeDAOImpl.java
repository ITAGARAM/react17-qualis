package com.agaramtech.qualis.barcodeprinting.service.ParentSampleBarcode;

import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.barcodeprinting.model.BarcodeFilterType;
import com.agaramtech.qualis.basemaster.model.Barcode;
import com.agaramtech.qualis.basemaster.model.Printer;
import com.agaramtech.qualis.biobank.model.BioParentSampleReceiving;
import com.agaramtech.qualis.exception.service.ExceptionLogDAOImpl;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

@SuppressWarnings("unused")
@AllArgsConstructor
@Repository
public class ParentSampleBarcodeDAOImpl implements ParentSampleBarcodeDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionLogDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final CommonFunction commonFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	@Override
	public ResponseEntity<Object> getParentSampleBarcode(UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final String strQuery = "select *,coalesce(jsondata->'sbarcodefiltertypename'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " jsondata->'sbarcodefiltertypename'->>'en-US') as sdisplaystatus from barcodefiltertype where nformcode=310 order by  1";
		final List<BarcodeFilterType> lstBarcodeFilterType = (List<BarcodeFilterType>) jdbcTemplate.query(strQuery,
				new BarcodeFilterType());
		if (!lstBarcodeFilterType.isEmpty()) {

			final String lstQuery = "select row_number() OVER () AS npkid,sparentsamplecode from "
					+ lstBarcodeFilterType.get(0).getSfiltertablename() + " where " + " nsitecode="
					+ userInfo.getNtranssitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" group by sparentsamplecode";
			final List<BioParentSampleReceiving> lstBioParentSampleReceiving = (List<BioParentSampleReceiving>) jdbcTemplate
					.query(lstQuery, new BioParentSampleReceiving());
			outputMap.put("ParentSampleBarcodeData", lstBioParentSampleReceiving);
			outputMap.put("selectedBarcodeFilterType", lstBarcodeFilterType.get(0));
		}
		outputMap.put("BarcodeFilterType", lstBarcodeFilterType);
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getPrinter(UserInfo userInfo) {
		List<Printer> lstprinter = new ArrayList<>();
		Printer p1 = new Printer();

		final String getColudprint = " select ssettingvalue from settings where nsettingcode = "
				+ Enumeration.Settings.CLOUD_PRINTER.getNsettingcode() + "" + " and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		String sColudprint = jdbcTemplate.queryForObject(getColudprint, String.class);

		final int ncloudprint = Integer.valueOf(sColudprint);

		if (ncloudprint == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
			final String getPrinterlst = "select printerid nprintercode ,printername sprintername from printerdetails "
					+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and nsitecode ="+userInfo.getNtranssitecode()+" order by 1 desc;";

			lstprinter = (List<Printer>) jdbcTemplate.query(getPrinterlst, new Printer());
		} else {
			PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
			PrintService[] services = PrinterJob.lookupPrintServices();
			String serviceName = "";
			if (defaultService != null) {
				serviceName = defaultService.getName();
				p1.setSprintername(serviceName);
				lstprinter.add(p1);
			}
			// NIBSCRT-2110
			for (PrintService printer : services) {
				if (serviceName != null && !serviceName.equals(printer.getName())) {
					Printer p = new Printer();
					p.setSprintername(printer.getName());
					lstprinter.add(p);
				}
			}
		}

		return new ResponseEntity<>(lstprinter, HttpStatus.OK);
	}

	@SuppressWarnings({ "unused", "unused", "resource", "resource", "resource", "resource", "resource" })
	@Override
	public ResponseEntity<Object> PrintBarcode(Map<String, Object> inputMap) throws Exception {

		final String sQuery = " lock  table lockprintbarcode " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		String sprintername = (String) inputMap.get("sprintername");
		String squery = "";
		int nbarcode = 0;
		List<String> sBarCodeQueries = null;
		FileReader fr = null;
		BufferedWriter bufferedWriter = null;
		BufferedReader br = null;
		int copiesCount = Integer.valueOf(inputMap.get("ncount").toString());
		String str = "select * from barcode b,labelformattype lf where b.ncontrolcode =" + inputMap.get("ncontrolcode")
				+ " and b.sbarcodename='" + inputMap.get("sbarcodename") + "' and b.nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and lf.nlabelformattypecode=b.nlabelformattypecode and b.nsitecode=" + objUserInfo.getNmastersitecode()
				+ " and lf.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " "
				+ " and lf.nsitecode=" + objUserInfo.getNmastersitecode();
		List<Barcode> lst = (List<Barcode>) jdbcTemplate.query(str, new Barcode());

		if (!lst.isEmpty()) {
			int ncountnumber = lst.get(0).getNlabelcolumncount();
			nbarcode = lst.get(0).getNbarcode();
			FileInputStream psStream = null;
			final String getBarcodePath = " select ssettingvalue from settings" + " where nsettingcode = 9 "
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			String FilePath = jdbcTemplate.queryForObject(getBarcodePath, String.class);

			sBarCodeQueries = new ArrayList<>();
			squery = jdbcTemplate.queryForObject("Select ssqlquery from SqlQuery where nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsqlquerycode = "
					+ lst.get(0).getNquerycode() + "", String.class);
			String tempQry = "";
			String[] query = squery.split(" ");
			tempQry = tempQry + squery;
			if (squery.contains("<@")) {
				int aa = squery.indexOf("<@");
				int bb = (squery.indexOf("@>") + 2);
				int b = (squery.indexOf("@>"));
				tempQry = tempQry.replace(squery.substring(aa, (bb)), inputMap.get(squery.substring(aa + 2, b)) + "");
			}
			String PrnFile = FilePath + lst.get(0).getSsystemfilename();
			File fileSharedFolder = new File(PrnFile);

			if (!fileSharedFolder.exists()) {
				LOGGER.info("PRN File Not Found in Path->" + PrnFile);
				LOGGER.info("Downloading from FTP");
				final UserInfo barcodeUserInfo = new UserInfo(objUserInfo);
				barcodeUserInfo.setNformcode((short) Enumeration.FormCode.BARCODE.getFormCode());
				Map<String, Object> objmap = ftpUtilityFunction.FileViewUsingFtp(lst.get(0).getSsystemfilename(), -1,
						barcodeUserInfo, FilePath, "");
				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() == objmap.get("rtn")) {

					LOGGER.info("File Downloaded from FTP");
					fileSharedFolder = new File(PrnFile);
				} else {
					LOGGER.info("Error in downloading file from FTP");
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PRNFILEDOESNOTEXIST",
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			LOGGER.info("PRN File Found in Path->" + PrnFile);
			final String homePath = ftpUtilityFunction.getFileAbsolutePath();

			final String getColudprint = " select ssettingvalue from settings where nsettingcode = "
					+ Enumeration.Settings.CLOUD_PRINTER.getNsettingcode() + "" + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			String sColudprint = jdbcTemplate.queryForObject(getColudprint, String.class);

			final int ncloudprint = Integer.valueOf(sColudprint);

			if (ncloudprint == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final StringBuilder insertQuery = new StringBuilder();
				insertQuery.append(
						"insert into printjob (printuuid,sfilename,printerkey,printername,printby_usercode,nsitecode,nstatus) values");
				StringBuffer stringBuffer = new StringBuffer();
				List<Map<String, Object>> lstquery = jdbcTemplate.queryForList(tempQry);
				if (!lstquery.isEmpty()) {
					for (int k = 1; k <= copiesCount; k++) {
						int totalBatches = (int) Math.ceil((double) lstquery.size() / ncountnumber);
						for (int i = 0; i < totalBatches; i++) {

							LOGGER.info("New PRN File Creating->");
							String uuid = UUID.randomUUID().toString().trim() + ".prn";
							StringBuilder prnContentBuilder = new StringBuilder();
							String line;

							int startIdx = i * ncountnumber;
							int endIdx = Math.min(startIdx + ncountnumber, lstquery.size());
							List<Map<String, Object>> subList = new ArrayList<>(lstquery.subList(startIdx, endIdx));
							Map<String, Object> numberedMap = new HashMap<>();
							for (int j = 0; j < subList.size(); j++) {
								Map<String, Object> map = subList.get(j);
								for (String key : map.keySet()) {
									if(ncountnumber>1) {
										numberedMap.put(key + (j + 1), map.get(key));
										}else {
										numberedMap.put(key, map.get(key));
										}
								}
							}
							if(ncountnumber>1) {
							try (FileReader fr1 = new FileReader(PrnFile);
								     BufferedReader br1 = new BufferedReader(fr1)) {
								    String prevLine = null;
								    while ((line = br1.readLine()) != null) {
								        if (line.contains("$")) {
								            int start = line.indexOf("$");
								            int end = line.lastIndexOf("$");
								            if (start >= 0 && end > start) {
								                String keyName = line.substring(start + 1, end); 
								                String keyToReplace = "\\$" + keyName + "\\$";
								                if (numberedMap.containsKey(keyName)) {
								                    String value = numberedMap.get(keyName) != null
								                            ? numberedMap.get(keyName).toString()
								                            : "";
								                    if (value.isEmpty()) {
								                        LOGGER.warn("Value empty for key: " + keyName + ", removing related barcode lines.");
								                        prevLine = null; // reset
								                        continue; // skip this line
								                    }
								                    line = line.replaceAll(keyToReplace, value);
								                    LOGGER.info("Value Replaced " + keyToReplace + " -> " + value);
								                } else {
								                    LOGGER.warn("Missing key for " + keyName + ", skipping related barcode lines.");
								                    prevLine = null;
								                    continue;
								                }
								            }
								        }
								        if (prevLine != null) {
								            prnContentBuilder.append(prevLine).append(System.lineSeparator());
								        }
								        prevLine = line; 
								    }
								    if (prevLine != null) {
								        prnContentBuilder.append(prevLine).append(System.lineSeparator());
								    }
								    LOGGER.info("Final PRN content:\n" + prnContentBuilder.toString());
								}
							}else {
								fr = new FileReader(PrnFile);
								br = new BufferedReader(fr);
								
								Map<String, Object> map = lstquery.get(i);
								
								while ((line = br.readLine()) != null) {
									if (line.contains("$")) {
										int start = line.indexOf("$");
										int end = line.lastIndexOf("$");
										String valueSubstitutedToFilter1 = line.substring(start + 1, end);
										String keyToReplace = "\\$" + valueSubstitutedToFilter1 + "\\$";
										line = line.replaceAll(keyToReplace, numberedMap.get(valueSubstitutedToFilter1).toString());
										LOGGER.info("Value Replaced " + keyToReplace + "->"+ numberedMap.get(valueSubstitutedToFilter1).toString());
										
									} 
									//prnContentBuilder.append(line).append("\\n");
									prnContentBuilder.append(line).append(System.lineSeparator());
								}
								
								
								if (br.readLine() == null) {
									br.close();
								}
							}

							Map<String, Object> outputMap = new HashMap<String, Object>();
							 LOGGER.info("Insert Before  PRN content:\n" + prnContentBuilder.toString());
							insertQuery.append("('" + uuid + "','"
									+ stringUtilityFunction.replaceQuote(prnContentBuilder.toString()) + "',NULL,'"
									+ sprintername + "'," + objUserInfo.getNusercode() + ","
									+ objUserInfo.getNtranssitecode() + "," + ""
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "),");
							 LOGGER.info("Insert After  PRN content:\n" + prnContentBuilder.toString());

							outputMap.put("sprimarykeyvalue", -1);
							auditUtilityFunction.insertAuditAction(objUserInfo, "IDS_PRINTBARCODE", commonFunction
									.getMultilingualMessage("IDS_PRINTBARCODE", objUserInfo.getSlanguagefilename()),
									outputMap);
						}

					}
					jdbcTemplate.execute(insertQuery.substring(0, insertQuery.length() - 1));

					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SUCCESS", objUserInfo.getSlanguagefilename()),
							HttpStatus.OK);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTVALIDRECORDTOPRINT",
							objUserInfo.getSlanguagefilename()), HttpStatus.OK);
				}
			} else {
				List<Map<String, Object>> lstquery = jdbcTemplate.queryForList(tempQry);
				if (!lstquery.isEmpty()) {
					for (int k = 1; k <= copiesCount; k++) {
						int totalBatches = (int) Math.ceil((double) lstquery.size() / ncountnumber);
						for (int i = 0; i < totalBatches; i++) {
							String FileNamePath1 = System.getenv(homePath) + Enumeration.FTP.UPLOAD_PATH.getFTP()
									+ UUID.randomUUID().toString().trim() + ".prn";

							Path path = Paths.get(FileNamePath1);
							LOGGER.info("New PRN File Created in Path->" + FileNamePath1);
							bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
							String line;
							StringBuffer stringBuffer = new StringBuffer();

							int startIdx = i * ncountnumber;
							int endIdx = Math.min(startIdx + ncountnumber, lstquery.size());
							List<Map<String, Object>> subList = new ArrayList<>(lstquery.subList(startIdx, endIdx));
							Map<String, Object> numberedMap = new HashMap<>();
							for (int j = 0; j < subList.size(); j++) {
								Map<String, Object> map = subList.get(j);
							
								for (String key : map.keySet()) {
									if(ncountnumber>1) {
									numberedMap.put(key + (j + 1), map.get(key));
									}else {
									numberedMap.put(key, map.get(key));
									}
								}
							}
							if(ncountnumber>1) {
							try (FileReader fr1 = new FileReader(PrnFile);
									BufferedReader br1 = new BufferedReader(fr1)) {
								int lineCount = 0;
								String prevLine = null; 
								while ((line = br1.readLine()) != null) {
									lineCount++;
									if (line.contains("$")) {
										int start = line.indexOf("$");
										int end = line.lastIndexOf("$");
										if (start >= 0 && end > start) {
											String keyName = line.substring(start + 1, end);
											String keyToReplace = "\\$" + keyName + "\\$";
											if (numberedMap.containsKey(keyName)) {
												String value = numberedMap.get(keyName) != null
														? numberedMap.get(keyName).toString()
														: "";
												line = line.replaceAll(keyToReplace, value);
												LOGGER.info("Value Replaced " + keyToReplace + " -> " + value);
											} else {
												LOGGER.warn("Missing key for " + keyName
														+ ", skipping previous line if exists.");
												prevLine = null;
												continue; 
											}
										}
									}
									if (prevLine != null) {
										bufferedWriter.write(prevLine);
										bufferedWriter.newLine();
									}
									prevLine = line;
								}
								if (prevLine != null) {
									bufferedWriter.write(prevLine);
									bufferedWriter.newLine();
								}
								bufferedWriter.flush();
							}
						} else {
							fr = new FileReader(PrnFile);
							br = new BufferedReader(fr);
							Map<String, Object> map = lstquery.get(i);
							while ((line = br.readLine()) != null) {
								if (line.contains("$")) {
									int start = line.indexOf("$");
									int end = line.lastIndexOf("$");
									String valueSubstitutedToFilter1 = line.substring(start + 1, end);
									String keyToReplace = "\\$" + valueSubstitutedToFilter1 + "\\$";
									line = line.replaceAll(keyToReplace, numberedMap.get(valueSubstitutedToFilter1).toString());
									LOGGER.info("Value Replaced " + keyToReplace + "->"
											+ numberedMap.get(valueSubstitutedToFilter1).toString());
									bufferedWriter.write(line);
									((BufferedWriter) bufferedWriter).newLine();
									bufferedWriter.flush();
								} else {
									bufferedWriter.write(line);
									((BufferedWriter) bufferedWriter).newLine();
									bufferedWriter.flush();
								}
							}
							if (br.readLine() == null) {
								br.close();
							}
						}
							psStream = new FileInputStream(new File(FileNamePath1));
							String Printerpath = "";
							Printer objbarcode = null;
							DocFlavor psInFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
							Doc myDoc = new SimpleDoc(psStream, psInFormat, null);
							PrintServiceAttributeSet aset = new HashPrintServiceAttributeSet();
							aset.add(new PrinterName(Printerpath, null));//
							PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
							for (PrintService printer : services) {
								if (printer.getName().equalsIgnoreCase(sprintername)) {
									DocPrintJob job = printer.createPrintJob();
									System.out.println("print");

									job.print(myDoc, null);

									String word = "";
									LOGGER.info("Printer Name =>" + printer.getName());
									LOGGER.info("Barcode Printed Successfully");
								}
							}
						}
						Map<String, Object> outputMap = new HashMap<String, Object>();
						outputMap.put("sprimarykeyvalue", -1);
						auditUtilityFunction.insertAuditAction(objUserInfo, "IDS_PRINTBARCODE", commonFunction
								.getMultilingualMessage("IDS_PRINTBARCODE", objUserInfo.getSlanguagefilename()),
								outputMap);
					}
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SUCCESS", objUserInfo.getSlanguagefilename()),
							HttpStatus.OK);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTVALIDRECORDTOPRINT",
							objUserInfo.getSlanguagefilename()), HttpStatus.OK);
				}
			}

		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOBARCODECONFIGURATION",
					objUserInfo.getSlanguagefilename()), HttpStatus.ALREADY_REPORTED);
		}
	}
	
	// modified by sujatha ATE_274 by adding userInfo for throwing alert if sql query not there in table DEMO Src
	@Override
	public ResponseEntity<Object> getControlBasedBarcode(int ncontrolcode, UserInfo userInfo) throws Exception {
		final String strQuery = "select nbarcode,nquerycode,ncontrolcode,sbarcodename,sfilename,ssystemfilename "
				+ " from barcode  where  nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and ncontrolcode=" + ncontrolcode;
		final List<Barcode> lstControlBasedBarcode = (List<Barcode>) jdbcTemplate.query(strQuery, new Barcode());
		
		//added by sujatha ATE_274 for throwing alert if the query is not available in the table
		if(lstControlBasedBarcode.isEmpty()) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SQLQUERYNOTAVAILABLE",
					userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
		}
		return new ResponseEntity<>(lstControlBasedBarcode, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getParentSampleBarcodedata(Map<String, Object> inputMap) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userInfo"), new TypeReference<UserInfo>() {
		});
		final int nbarcodefiltertypecode = (int) inputMap.get("nbarcodefiltertypecode");

		final String strQuery = "select * from barcodefiltertype where nbarcodefiltertypecode ="
				+ nbarcodefiltertypecode + " order by  1";
		final List<BarcodeFilterType> lstBarcodeFilterType = (List<BarcodeFilterType>) jdbcTemplate.query(strQuery,
				new BarcodeFilterType());
		if (!lstBarcodeFilterType.isEmpty()) {

			String squery = (String) lstBarcodeFilterType.get(0).getSsqlquery();
			StringBuilder sbuilder1 = new StringBuilder();
			if (squery != null) {
				sbuilder1.append(squery);
				while (squery.contains("<@")) {
					int nStart = squery.indexOf("<@");
					int nEnd = squery.indexOf("@>");
					sbuilder1.replace(nStart, nEnd + 2, String.valueOf(userInfo.getNtranssitecode()));
					squery = sbuilder1.toString();
				}
			}
			final String lstQuery = squery;
			final List<BioParentSampleReceiving> lstBioParentSampleReceiving = (List<BioParentSampleReceiving>) jdbcTemplate
					.query(lstQuery, new BioParentSampleReceiving());
			outputMap.put("ParentSampleBarcodeData", lstBioParentSampleReceiving);
		}
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

}
