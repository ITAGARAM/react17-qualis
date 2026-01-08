package com.agaramtech.qualis.barcodeprinting.service.ParentSampleBarcode;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface ParentSampleBarcodeService {

	public ResponseEntity<Object> getParentSampleBarcode(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getPrinter(UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> PrintBarcode(Map<String, Object> inputMap) throws Exception;
	
	// modified by sujatha ATE_274 by adding userInfo for throwing alert if sql query not there in table
	ResponseEntity<Object> getControlBasedBarcode(final int ncontrolcode, final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getParentSampleBarcodedata(Map<String, Object> inputMap) throws Exception;

}
