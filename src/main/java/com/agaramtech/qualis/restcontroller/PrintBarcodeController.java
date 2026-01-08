package com.agaramtech.qualis.restcontroller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.agaramtech.qualis.BarcodeConfiguration.model.PrintJob;
import com.agaramtech.qualis.BarcodeConfiguration.model.Printerdetails;
import com.agaramtech.qualis.BarcodeConfiguration.service.printbarcode.PrintBarcodeService;

@RestController
@RequestMapping("/Barcode")
public class PrintBarcodeController {

	private final PrintBarcodeService printBarcodeService;
	private RequestContext requestContext;
	private static final Log LOGGER = LogFactory.getLog(BarcodeController.class);
	
	public PrintBarcodeController(RequestContext requestContext, PrintBarcodeService printBarcodeService) {
		super();
		this.requestContext = requestContext;
		this.printBarcodeService = printBarcodeService;
	}
	
	
	@PostMapping("/insertprints")
	public List<Printerdetails> insertprints(@RequestBody Printerdetails[]  print) throws Exception {
		List<Printerdetails> lsprint = Arrays.asList(print);

		LOGGER.info("InsertPrinterListfrom IOT -->");
		return printBarcodeService.insertprints(lsprint);
	}
	
//	
//	@PostMapping("/insertprintjob")
//    public PrintJob insertprintjob(@RequestBody MultipartFile file,	 Integer usercode,  String printer,  Integer isMultitenant)throws Exception {
//		return printBarcodeService.insertprintjob(file,usercode, printer,isMultitenant);
//	}

	@PostMapping("/insertprintjob")
	public PrintJob insertprintjob(Map<String, Object> inputMap) throws Exception
	{
		return printBarcodeService.insertprintjob(inputMap);
	}
	
	@PostMapping("/getprinterJobdetails")
	public List<Map<String, Object>> getPrintJob(Map<String, Object> printjob) throws Exception{
		   
		return printBarcodeService.getPrinterjoblist();
	}
	
	
	

}
