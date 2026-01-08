package com.agaramtech.qualis.restcontroller;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.reports.service.reportview.ReportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/reststimulsoft")
public class ReportViewStimulsoftController {

	private RequestContext requestContext;
	private final ReportService reportViewService;
//SWSM-121 Start - added the code for security vulnerability - by rukshana
	@Value("${spring.datasource.username}")
	private String databaseUserName;
	@Value("${spring.datasource.password}")
	private String databasePassword;
	@Value("${spring.datasource.url}")
	private String databaseConnectionUrl;
	//end

	public ReportViewStimulsoftController(ReportService reportViewService, RequestContext requestContext) {
		super();
		this.requestContext = requestContext;
		this.reportViewService = reportViewService;
	}

	@PostMapping(value = "/getStimulsoftView")
	public ResponseEntity<Object> getStimulsoftView(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		Integer reportModuleCode = null;
		if (inputMap.get("nreportmodulecode") != null) {
			reportModuleCode = (Integer) inputMap.get("nreportmodulecode");
		}

		return reportViewService.getStimulsoftView(reportModuleCode, userInfo);

	}
//SWSM-121 Start - added the code for security vulnerability - by rukshana	
	
	@PostMapping(value = "/urljson") // Changed GET to POST by Gowtham on dec 2 2025 for SWSM-125 - JavaReportingTool sensetive data hide through network
    public ResponseEntity<Map<String, String>> getReportUrljson() throws Exception {
    	
		String suser = databaseUserName;
		String spsw = databasePassword;
		String surl = databaseConnectionUrl;
		String sDB = surl.substring(surl.lastIndexOf("/") + 1);
		String sserver = surl.substring(surl.indexOf("//") + 2, surl.lastIndexOf(":"));
		String sport = surl.substring(surl.lastIndexOf(":") + 1, surl.lastIndexOf("/"));
//		String sConnStr = "Server=" + sserver + ";Port=" + sport + ";Database=" + sDB + ";User=" + suser + ";Pwd="
//				+ spsw + ";";
			
		 // Build response with only safe information
	    Map<String, String> response = new HashMap<>();
	    response.put("server", sserver);
	    response.put("port", sport);
	    response.put("database", sDB);
	    response.put("user", suser);
	    response.put("password", spsw);
	    // DO NOT include username or password
	    
	    // Converting the responseMap to Base64(Encoded) by Gowtham to avoid vulnarabilities on dec 2 2025 for SWSM-125
	    final ObjectMapper mapper = new ObjectMapper();		
	    String json = mapper.writeValueAsString(response);
	    String encoded = Base64.getEncoder().encodeToString(json.getBytes());
	    
	    return ResponseEntity.ok().body(Map.of("data", encoded));
//	    return ResponseEntity.ok()
//	            .contentType(MediaType.APPLICATION_JSON)
//	            .body(response);
    }
 
//end
}
