package com.agaramtech.qualis.BarcodeConfiguration.service.printbarcode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.agaramtech.qualis.BarcodeConfiguration.model.PrintJob;
import com.agaramtech.qualis.BarcodeConfiguration.model.Printerdetails;
import com.agaramtech.qualis.basemaster.model.Barcode;
import com.agaramtech.qualis.basemaster.model.Printer;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.restcontroller.BarcodeController;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class PrintBarcodeDAOImpl implements PrintBarcodeDAO{


	private final JdbcTemplate jdbcTemplate;
	private final CommonFunction commonFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private static final Log LOGGER = LogFactory.getLog(BarcodeController.class);

	public List<Printerdetails> insertprints(List<Printerdetails> printers)
	{
		final StringBuilder insertQuery = new StringBuilder();
		
		LOGGER.info("print get and insert--->"+printers.toString());
		
		insertQuery.append("delete from printerdetails where nsitecode="+printers.get(0).getSitecode()+"; ");
		
		insertQuery.append("insert into printerdetails (printerkey,printername,nsitecode,nstatus) values");

		if(printers.size() > 0)
		{
			for (int i=0; i<=printers.size()-1;i++)
			{
				insertQuery.append("('"+printers.get(i).getPrinterKey()+"','"+printers.get(i).getPrinterName()+"',"
						+ ""+printers.get(i).getSitecode()+","+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"),");
			}
		}


		jdbcTemplate.execute(insertQuery.substring(0, insertQuery.length()-1));
		LOGGER.info("print get and insert end");
		return printers;
	}


	public List<Map<String, Object>> getPrinterjoblist() throws Exception
	{
		LOGGER.info("print Job Get and insert");
		List<Map<String, Object>> lsprinter = new ArrayList<Map<String, Object>>();
		
		final String printJobQuery = "select printId,printerName,printuuid,sfilename,printby_usercode from printjob where nstatus = 1;";
		
		List<PrintJob> lstprintjobs = jdbcTemplate.query(printJobQuery, new PrintJob());
		
//		final String homePath = ftpUtilityFunction.getFileAbsolutePath();
		List<Long> toDeletePrintIds = new ArrayList<>();
		
		for (int i = 0; i < lstprintjobs.size(); i++) {
			LOGGER.info("print Job Loop");
			Map<String, Object> objresmap = new HashMap<String, Object>();
			
			objresmap.put("printername", lstprintjobs.get(i).getPrinterName());
			objresmap.put("printby", lstprintjobs.get(i).getPrintby_usercode());
//			
//			String FileNamePath = System.getenv(homePath) + Enumeration.FTP.UPLOAD_PATH.getFTP()+ lstprintjobs.get(print).getPrintuuid();
//			
//			 byte[] fileBytes = Files.readAllBytes(Paths.get(FileNamePath));
//			 StringBuilder resultStringBuilder = new StringBuilder();
//				try {
//					ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
//					
//					
//			        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
//			            String line;
//			            while ((line = br.readLine()) != null) {
//			                resultStringBuilder.append(line).append("\n");
//			            }
//			        }
//	          
//				} catch (IllegalStateException e) {
//
//					e.printStackTrace();
//				} catch (IOException e) {	
//
//					e.printStackTrace();
//				}
				
				
			String data = lstprintjobs.get(i).getSfilename();
			objresmap.put("file", data);
			lsprinter.add(objresmap);
			toDeletePrintIds.add(lstprintjobs.get(i).getPrintid());
			
			LOGGER.info("print Job End Loop");
		}
		
		if (!toDeletePrintIds.isEmpty()) {
			LOGGER.info("print Job Delete Start");
		    String placeholders = toDeletePrintIds.stream().map(String::valueOf).collect(Collectors.joining(","));
		    String deleteSQL = "DELETE FROM printjob WHERE printid IN (" + placeholders + ")";
		    jdbcTemplate.execute(deleteSQL);
		    LOGGER.info("print Job Delete End--> "+deleteSQL);
		}
		
		return lsprinter;
		
	}
	
	
	@SuppressWarnings("unused")
	public PrintJob insertprintjob(Map<String, Object> inputMap) throws Exception {

		ObjectMapper objMapper = new ObjectMapper();
		final UserInfo objUserInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		String sprintername = (String) inputMap.get("sprintername");
		String squery = "";
		int nbarcode = 0;
		List<String> sBarCodeQueries = null;
		FileReader fr = null;
		BufferedWriter bufferedWriter = null;
		BufferedReader br = null;
		String str = "select * from barcode where ncontrolcode =" + inputMap.get("ncontrolcode") + " and sbarcodename='"
				+ inputMap.get("sbarcodename") + "' and nstatus ="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ objUserInfo.getNmastersitecode() + "";
		List<Barcode> lst = (List<Barcode>) jdbcTemplate.query(str, new Barcode());

		List<String> lsttransactionsamplecode = Arrays
				.asList(inputMap.get("ntransactionsamplecode").toString().split(","));
		List<String> lsttransactionpreregcode = Arrays.asList(inputMap.get("npreregno").toString().split(","));
		int allSubSampleCount = lsttransactionsamplecode.size();
		int allSampleCount = lsttransactionpreregcode.size();

		final String sFindSubSampleQuery = "select count(nsamplehistorycode) from registrationsamplehistory"
				+ " where nsamplehistorycode = any (select max(nsamplehistorycode) from registrationsamplehistory where ntransactionsamplecode"
				+ " in (" + inputMap.get("ntransactionsamplecode").toString() + ")  and nsitecode="
				+ objUserInfo.getNtranssitecode() + " group by ntransactionsamplecode)"
				+ " and ntransactionstatus not in (" + Enumeration.TransactionStatus.REJECTED.gettransactionstatus()
				+ ", " + Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus() + ", "
				+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")" + "  and nsitecode="
				+ objUserInfo.getNtranssitecode() + ";";

		int availableSampleCount = jdbcTemplate.queryForObject(sFindSubSampleQuery, Integer.class);

		if (allSubSampleCount == availableSampleCount) {
			final String sFindSampleQuery = "select count(nreghistorycode) from registrationhistory"
					+ " where nreghistorycode = any (select max(nreghistorycode) from registrationhistory where npreregno"
					+ " in (" + inputMap.get("npreregno").toString() + ")  and nsitecode="
					+ objUserInfo.getNtranssitecode() + " group by npreregno)" + " and ntransactionstatus not in ("
					+ Enumeration.TransactionStatus.REJECTED.gettransactionstatus() + ", "
					+ Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus() + ", "
					+ Enumeration.TransactionStatus.QUARENTINE.gettransactionstatus() + ", "
					+ Enumeration.TransactionStatus.CANCELED.gettransactionstatus() + ")" + "  and nsitecode="
					+ objUserInfo.getNtranssitecode() + ";";

			int availablePreregCount = jdbcTemplate.queryForObject(sFindSampleQuery, Integer.class);

			if (allSampleCount == availablePreregCount) {
				if (!lst.isEmpty()) {

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
						tempQry = tempQry.replace(squery.substring(aa, (bb)),
								inputMap.get(squery.substring(aa + 2, b)) + "");
					}
					String PrnFile = FilePath + lst.get(0).getSsystemfilename();
					File fileSharedFolder = new File(PrnFile);

					if (!fileSharedFolder.exists()) {
						LOGGER.info("PRN File Not Found in Path->" + PrnFile);
						LOGGER.info("Downloading from FTP");
						final UserInfo barcodeUserInfo = new UserInfo(objUserInfo);
						barcodeUserInfo.setNformcode((short) Enumeration.FormCode.BARCODE.getFormCode());
						Map<String, Object> objmap = FileViewUsingFtp(lst.get(0).getSsystemfilename(), -1,barcodeUserInfo, FilePath, "");
						if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() == objmap.get("rtn")) {

							LOGGER.info("File Downloaded from FTP");
							fileSharedFolder = new File(PrnFile);
						} else {
							LOGGER.info("Error in downloading file from FTP");
							return null;
//							return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PRNFILEDOESNOTEXIST",
//									objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						}
					}
					LOGGER.info("PRN File Found in Path->" + PrnFile);
					final String homePath = getFileAbsolutePath();
					String FileNamePath1 = System.getenv(homePath) + Enumeration.FTP.UPLOAD_PATH.getFTP()
							+ UUID.randomUUID().toString().trim() + ".prn";

					Path path = Paths.get(FileNamePath1);
					LOGGER.info("New PRN File Created in Path->" + FileNamePath1);
					bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
					String line;
					StringBuffer stringBuffer = new StringBuffer();
					List<Map<String, Object>> lstquery = jdbcTemplate.queryForList(tempQry);
					if (!lstquery.isEmpty()) {
						for (int i = 0; i < lstquery.size(); i++) {
							fr = new FileReader(PrnFile);
							br = new BufferedReader(fr);
							Map<String, Object> map = lstquery.get(i);
							while ((line = br.readLine()) != null) {
								if (line.contains("$")) {
									int start = line.indexOf("$");
									int end = line.lastIndexOf("$");
									String valueSubstitutedToFilter1 = line.substring(start + 1, end);
									String keyToReplace = "\\$" + valueSubstitutedToFilter1 + "\\$";
									line = line.replaceAll(keyToReplace, map.get(valueSubstitutedToFilter1).toString());
									LOGGER.info("Value Replaced " + keyToReplace + "->"
											+ map.get(valueSubstitutedToFilter1).toString());
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
							psStream = new FileInputStream(new File(FileNamePath1));
							String Printerpath = "";
							Printer objbarcode = null;
							DocFlavor psInFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
							DocFlavor psInFormat1 = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
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
							Map<String, Object> outputMap = new HashMap<String, Object>();
							outputMap.put("sprimarykeyvalue", -1);
//							insertAuditAction(objUserInfo, "IDS_PRINTBARCODE", commonFunction.getMultilingualMessage(
//									"IDS_PRINTBARCODE", objUserInfo.getSlanguagefilename()), outputMap);
						}
//						return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SUCCESS",
//								objUserInfo.getSlanguagefilename()), HttpStatus.OK);
						return null;
					} else {
//						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
//								"IDS_SELECTVALIDRECORDTOPRINT", objUserInfo.getSlanguagefilename()), HttpStatus.OK);
						return null;
					}
				} else {
//					return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_NOBARCODECONFIGURATION",
//							objUserInfo.getSlanguagefilename()), HttpStatus.ALREADY_REPORTED);
					return null;
				}
			} else {
//				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTREGISTEREDSAMPLEONLY",
//						objUserInfo.getSlanguagefilename()), HttpStatus.ALREADY_REPORTED);
				return null;
			}
		} else {
//			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTREGISTEREDSUBSAMPLEONLY",
//					objUserInfo.getSlanguagefilename()), HttpStatus.ALREADY_REPORTED);
			return null;
		}
	}
	
	private Map<String, Object> FileViewUsingFtp(String ssystemfilename, int i, UserInfo objUserInfo, String string,String string2) {
		return null;
	}
	
	private String getFileAbsolutePath() {

		return null;
	}

}
