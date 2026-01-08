package com.agaramtech.qualis.BarcodeConfiguration.service.printbarcode;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.agaramtech.qualis.BarcodeConfiguration.model.PrintJob;
import com.agaramtech.qualis.BarcodeConfiguration.model.Printerdetails;

public interface PrintBarcodeDAO {

	
	public List<Printerdetails> insertprints(final List<Printerdetails> lsprint) throws Exception;
	
	public PrintJob insertprintjob(Map<String, Object> inputMap) throws Exception;
	
	public List<Map<String, Object>> getPrinterjoblist() throws Exception;
}
