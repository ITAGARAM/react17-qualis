package com.agaramtech.qualis.barcodeprinting.service.RepositoryIdBarcode;

import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

public interface RepositoryIdBarcodeService {

	public ResponseEntity<Object> getRepositoryIdBarcode(UserInfo userInfo,int nbarcodefiltertypecode) throws Exception;
}
