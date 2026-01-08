package com.agaramtech.qualis.BarcodeConfiguration.service.printbarcode;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.agaramtech.qualis.BarcodeConfiguration.model.PrintJob;
import com.agaramtech.qualis.BarcodeConfiguration.model.Printerdetails;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class PrintBarcodeServiceImpl implements PrintBarcodeService{

	private final  PrintBarcodeDAO objPrintBarcodeDAO;
	//private final CommonFunction commonFunction;

	@Transactional
	public List<Printerdetails> insertprints(final List<Printerdetails> lsprint) throws Exception
	{
		return objPrintBarcodeDAO.insertprints(lsprint);
	}

//	public PrintJob insertprintjob(MultipartFile file,	 Integer usercode,  String printer,  Integer isMultitenant) throws Exception
//	{
//		return objPrintBarcodeDAO.insertprintjob(file,usercode, printer,isMultitenant);
//	}
	
	public PrintJob insertprintjob(Map<String, Object> inputMap) throws Exception
	{
		return objPrintBarcodeDAO.insertprintjob(inputMap);
	}

	@Transactional
	public List<Map<String, Object>> getPrinterjoblist() throws Exception
	{
		return objPrintBarcodeDAO.getPrinterjoblist();
	}
}
