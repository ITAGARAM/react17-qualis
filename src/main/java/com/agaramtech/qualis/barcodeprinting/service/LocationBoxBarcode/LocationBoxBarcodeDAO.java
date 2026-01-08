package com.agaramtech.qualis.barcodeprinting.service.LocationBoxBarcode;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface LocationBoxBarcodeDAO {
	
	public ResponseEntity<Object> getLocationBoxBarcode(UserInfo userInfo,int nbarcodefiltertypecode) throws Exception;

}
