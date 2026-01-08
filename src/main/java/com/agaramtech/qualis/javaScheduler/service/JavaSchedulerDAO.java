package com.agaramtech.qualis.javaScheduler.service;

//Added by sonia for ALPD-4184
public interface JavaSchedulerDAO {

	//Fusion Sync
	//public void exceuteFusionSyncProcess() ;

	//Material Inventory Transaction
	public void materialinventoryvalidation();

	//Site to Site Auto Sync
	public void exceuteSyncProcess() throws Exception;

	//Site to Site delete Sync
	public void deleteSync();

	//Report Generation
	public void schedularGenerateReport();

	//Auto Expire- Method Validity
	public void methodvalidityautoexpire();

	//Email
	public void schedularSendEmailTask();

	//Send to Portal Report
	public void schedulerSendToPortalReport();

	//Sent Result to Portal
	public void schedulerSentResultToPortal();

	//Sent External Order Status
	public void schedulerSentExternalOrderStatus();

	//Exception Logs Delete
	public void deleteExceptionLogs();

	public void executeSyncReceivedData();

	//Added by sonia on 10th Feb 2025 for jira id:ALPD-5332
	public void scheduler() throws Exception;

	//Added by sonia on 11th Feb 2025 for jira id:ALPD-5317
	public void envirnomentalScheduler() throws Exception;

	//Added by sonia on 11th Feb 2025 for jira id:ALPD-5350
	public void stabilityScheduler() throws Exception;

	// below runs method [executeReleaseCOASync] for every 5 minutes
	public void executeReleaseCOASync();
	
	// below runs method [labDataSync] for every 5 minutes
    public void wqmisMasterDataSync();
    
    //Added by sonia on 11th Oct 2025 for jira id:SWSM-85
  	public void scheduleLabSamples() throws Exception;
  	
  	//Added by sonia on 11th Oct 2025 for jira id:SWSM-85
  	public void scheduleFTKSamples() throws Exception;
  	
  	//Added by Mohamed Ashik on 14th Oct 2025 for jira id:SWSM-85
  	public void wqmisContaminatedLabSamples() throws Exception;	
  	
  	//Added by Mohamed Ashik on 14th Oct 2025 for jira id:SWSM-85
  	public void wqmisContaminatedFTKSamples() throws Exception;	
  	
  	//ALPDJ21-93--Added by Vignesh(30-10-2025)-->Release and report screen -> HL7 Format Conversion
	public void hL7Conversion();
	
	//Added by sonia on 18th Nov 2025 for jira id:BGSI-234
	public void emailScheduler() throws Exception;
	
	//#SECURITY-VULNERABILITY-MERGING-START
	//Added by sonia on 22nd Nov 2025 for jira id:SWSM-125
  	public void runFunctionMigration()  throws Exception;
  	//#SECURITY-VULNERABILITY-MERGING-END
  	
  	
}


