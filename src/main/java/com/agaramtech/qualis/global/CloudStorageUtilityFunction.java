package com.agaramtech.qualis.global;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.AWSStorageConfig;
import com.agaramtech.qualis.configuration.model.FTPSubFolder;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RequiredArgsConstructor
@Component
public class CloudStorageUtilityFunction {

	private static final Logger LOGGER = LoggerFactory.getLogger(CloudStorageUtilityFunction.class);

	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final JdbcTemplate jdbcTemplate;
	
	//ALPDJ21-132-Added by suriaprahash(24/11/25)--For passworddecryption 
	private final PasswordUtilityFunction passwordUtilityFunction;
	//end ALPDJ21-132

	private final static String OS = System.getProperty("os.name").toLowerCase();
	public final static boolean IS_WINDOWS = (OS.indexOf("win") >= 0);
	public final static boolean IS_UNIX = (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);

	public Map<String, Object> getAWSClientBucket(final UserInfo userInfo) throws Exception {

		final Map<String, Object> objMap = new HashMap<>();
		final AWSStorageConfig objAWSStorageConfig = getAWSStorageCredential(userInfo);
		objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		if(objAWSStorageConfig!=null) {
		////ALPDJ21-132-Added by suriaprahash(24/11/25)--Decryptpassword for S3 credentials checking
		final String plainPassword = passwordUtilityFunction.decryptPassword("awsstorageconfig", "nawsstorageconfigcode",objAWSStorageConfig.getNawsstorageconfigcode() , "ssecretpasskey");
		objAWSStorageConfig.setSsecretpasskey(plainPassword);
		//end ALPDJ21-132


		final StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials
				.create(objAWSStorageConfig.getSaccesskeyid(), objAWSStorageConfig.getSsecretpasskey()));
		final S3Client s3 = S3Client.builder().region((Region) Region.of(objAWSStorageConfig.getSregion()))
				.credentialsProvider(credentialsProvider).build();
		LOGGER.info("S3 connection success");
		objMap.put("bucketName", objAWSStorageConfig.getSbucketname());
		objMap.put("s3", s3);
		}else {
			LOGGER.info("Check s3 connection");
			objMap.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_CHECKS3CONNECTION");

		}

		return objMap;
	}
	public AWSStorageConfig getAWSStorageCredential(final UserInfo userInfo) throws Exception {

		final String strAWSCredentials = "select * from awsstorageconfig where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode() + " and ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus();
		return (AWSStorageConfig) jdbcUtilityFunction.queryForObject(strAWSCredentials, AWSStorageConfig.class,
				jdbcTemplate);

	}

	public Settings getSetting(final UserInfo userInfo) throws Exception {
		final String strSettings = "select nsettingcode, ssettingname, ssettingvalue from settings where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsettingcode="
				+ Enumeration.Settings.NEEDS3STORAGE.getNsettingcode();
		return (Settings) jdbcUtilityFunction.queryForObject(strSettings, Settings.class, jdbcTemplate);
	}

	public String getFileAbsolutePath() throws Exception {
		final String homePathQuery = "select ssettingvalue from settings where nsettingcode ="
				+ Enumeration.Settings.DEPLOYMENTSERVER_HOMEPATH.getNsettingcode() + " and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final String homePath = (String) jdbcUtilityFunction.queryForObject(homePathQuery, String.class, jdbcTemplate);

		return homePath;
	}

	/**
	 * This method is used to view the uploaded attachments.
	 * @param systemFileName
	 * @param scustomPath
	 * @param subFolder
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> fileViewAWSStorage(final String systemFileName, final String scustomPath,
			final String subFolder, final UserInfo userInfo) throws Exception {
		LOGGER.info("fileViewAWSStorage entered");
		Map<String, Object> mapObj = new HashMap<>();
		Path downloadPath = null;
		String downloadsPath1 = Enumeration.FTP.FILE_PATH_TO_UPLOAD.getFTP();
		Path downloadsDir = null;
		if (!scustomPath.isEmpty()) {
			LOGGER.info("fileViewAWSStorage !scustomPath.isEmpty()");
			final File folderCheck = new File(scustomPath);
			if (!folderCheck.exists()) {

				final boolean created = folderCheck.mkdirs();
				if (created) {
					LOGGER.info("Folder created: " + folderCheck.getAbsolutePath());
				} else {
					LOGGER.info("Failed to create folder.");
				}

			}
			downloadsDir = Paths.get(scustomPath);
			downloadPath = downloadsDir.resolve(systemFileName);
			downloadsPath1 = downloadsDir.resolve(systemFileName).toString();
			LOGGER.info("downloadPath --->"+ downloadPath);
		} else {
			LOGGER.info("fileViewAWSStorage scustomPath.isEmpty()");
			final String homePath = getFileAbsolutePath();
			final String downloadsPath = getS3FileWritingPath();
			downloadsDir = Paths.get(downloadsPath);
			final File downloadsFolder = new File(downloadsPath);

			if (!downloadsFolder.exists()) {

				final boolean created = downloadsFolder.mkdirs();
				if (created) {
					System.out.println("Folder created: " + downloadsFolder.getAbsolutePath());
				} else {
					System.err.println("Failed to create folder.");
				}

			}
			downloadPath = downloadsDir.resolve(systemFileName);
			downloadsPath1 = downloadsPath1 + systemFileName;
			LOGGER.info("downloadPath --->"+ downloadPath);
		}

		final Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
		
		if(mapObjGet.get("rtn").equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
			LOGGER.info("fileViewAWSStorage getAWSClientBucket");
		final S3Client s3 = (S3Client) mapObjGet.get("s3");
		final String bucketName = (String) mapObjGet.get("bucketName");

		final String subFolderPath = (!subFolder.isEmpty()) ? (subFolder + "/") : "";

		if (Files.exists(downloadPath)) {
			Files.delete(downloadPath);
		}

		try {
			LOGGER.info("fileViewAWSStorage try");
			final GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName)
					.key(subFolderPath + systemFileName).build();

			s3.getObject(getObjectRequest, downloadPath);
			LOGGER.info("downloadPath --->"+ downloadPath);
			mapObj.put("AttachFile", systemFileName);
			mapObj.put("FilePath", downloadsPath1);
			mapObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			LOGGER.info("File downloaded successfully from S3 Storage");
		} catch (Exception e) {
			LOGGER.info("Failed to download");
			mapObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.FAILED.getreturnstatus());
		}
		}else {
			LOGGER.info("Check s3 connection");
			mapObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), mapObjGet.get("rtn"));
		}
		LOGGER.info("fileViewAWSStorage exit");
		return mapObj;
	}

	public void multiFileDownloadAWSStorage(final List<String> systemFileNames, final String scustomPath,
			final String subFolder, final UserInfo userInfo) throws Exception {

		Path downloadsDir = null;
		if (!scustomPath.isEmpty()) {

			final File folderCheck = new File(scustomPath);
			if (!folderCheck.exists()) {

				final boolean created = folderCheck.mkdirs();
				if (created) {
					LOGGER.info("Folder created: " + folderCheck.getAbsolutePath());
				} else {
					LOGGER.info("Failed to create folder.");
				}

			}
			downloadsDir = Paths.get(scustomPath);

		} else {

			final String homePath = getFileAbsolutePath();
			final String downloadsPath = getS3FileWritingPath();
			downloadsDir = Paths.get(downloadsPath);

			final File downloadsFolder = new File(downloadsPath);

			if (!downloadsFolder.exists()) {

				final boolean created = downloadsFolder.mkdirs();
				if (created) {
					LOGGER.info("Folder created: " + downloadsFolder.getAbsolutePath());
				} else {
					LOGGER.info("Failed to create folder.");
				}
			}
		}

		final Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
		
		if(mapObjGet.get("rtn").equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
		final S3Client s3 = (S3Client) mapObjGet.get("s3");
		final String bucketName = (String) mapObjGet.get("bucketName");

		final String subFolderPath = (!subFolder.isEmpty()) ? (subFolder + "/") : "";

		String strSystemFilesCreated = "";
		try {
			for (String systemFileName : systemFileNames) {

				final Path downloadFilePath = downloadsDir.resolve(systemFileName);

				if (Files.exists(downloadFilePath)) {
					Files.delete(downloadFilePath);
				}

				final GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName)
						.key(subFolderPath + systemFileName).build();

				s3.getObject(getObjectRequest, downloadFilePath);
				strSystemFilesCreated += systemFileName + ",";
			}

			if (strSystemFilesCreated != "") {
				LOGGER.info(
						"Files created : " + strSystemFilesCreated.substring(0, (strSystemFilesCreated.length() - 1)));
			}
		} catch (Exception e) {
			LOGGER.error("Error occured : " + e.getMessage());
		}
		}else {
			LOGGER.info("Check s3 connection");
		}
	}

	public String deleteFileAWSStorage(final String fileName, final String subFolder, final int ncontrolcode,
			final UserInfo userInfo) throws Exception {

		final String folderName = (subFolder != "") ? (subFolder + "/") : "";

		final Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
		if(mapObjGet.get("rtn").equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
		final S3Client s3 = (S3Client) mapObjGet.get("s3");
		final String bucketName = (String) mapObjGet.get("bucketName");

		final DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName)
				.key(folderName + fileName).build();
		s3.deleteObject(deleteObjectRequest);

		final String strRtn = "File deleted successfully: " + folderName + fileName;
		LOGGER.info(strRtn);

		return strRtn;
		}else {
			LOGGER.info("Check s3 connection");
			return Enumeration.ReturnStatus.FAILED.getreturnstatus();
		}
	}

	public String deleteFileListAWSStorage(final List<String> lstFiles, final String sChangeDirectory, final int ncontrolcode,
			final UserInfo userInfo) throws Exception {

		String changedirectory = "";
		String strRtn=Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		if (sChangeDirectory.isEmpty() || sChangeDirectory == "") {
			final String subfolderquery = "select ssubfoldername,ncontrolcode from ftpsubfolder where nformcode="
					+ userInfo.getNformcode() + "  and nsitecode=" + userInfo.getNmastersitecode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "";
			final List<FTPSubFolder> lstForm = jdbcTemplate.query(subfolderquery, new FTPSubFolder());
			if (!lstForm.isEmpty()) {
				changedirectory = lstForm.stream().filter(e -> e.getNcontrolcode() == ncontrolcode)
						.map(e -> e.getSsubfoldername()).collect(Collectors.joining(","));
			}
		} else {
			//ALPDJ21-132 Release screen fix: treat "\u200B" (zero-width space) as empty to avoid incorrect directory assignment
			// Added by Ganesh referred by Vishakh on 05/12/2025 
			changedirectory = sChangeDirectory.equals("\u200B") ? "" : sChangeDirectory;
		}
		// Commented by Ganesh
		//final String folderName = (changedirectory != "") ? (changedirectory + "/") : "";

		// Updated by Ganesh on 05/11/2025 - Correct empty check: use isEmpty() to avoid reference comparison issues
		final String folderName = (!changedirectory.isEmpty()) ? (changedirectory + "/") : "";

		final Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
		
		if(mapObjGet.get("rtn").equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())){
		final S3Client s3 = (S3Client) mapObjGet.get("s3");
		final String bucketName = (String) mapObjGet.get("bucketName");

		String strFileName = "";

		for (String fileName : lstFiles) {
			final DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName)
					.key(folderName + fileName).build();
			s3.deleteObject(deleteObjectRequest);
			strFileName += fileName + ",";
		}
		
		
		 strRtn = "File deleted successfully: " + strFileName.substring(0, strFileName.length() - 1);
		LOGGER.info(strRtn);
		strRtn = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		} else {
			LOGGER.info("Check s3 connection");
			strRtn = Enumeration.ReturnStatus.FAILED.getreturnstatus();
		}
		return strRtn;
		
	}

	public String fileUploadAWSStorage(final MultipartHttpServletRequest request,
			final Map<String, Object> credentialDetails, final UserInfo userInfo) throws Exception {
		String rtnStatus = "";
		try {

			final Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
			
			if(mapObjGet.get("rtn").equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
			final S3Client s3 = (S3Client) mapObjGet.get("s3");
			final String bucketName = (String) mapObjGet.get("bucketName");

			final int filecount = Integer.parseInt(request.getParameter("filecount"));

			for (int i = 0; i < filecount; i++) {
				final MultipartFile multiPartFile = request.getFile("uploadedFile" + i);

				if (multiPartFile == null || multiPartFile.isEmpty()) {
					continue;
				}

				Object folderObj = credentialDetails.get("folder");
				final String fileUploadDirectory = (credentialDetails.containsKey("folder")
						&& (!((String) credentialDetails.get("folder")).isEmpty())) ? (credentialDetails.get("folder") + "/") : "";
				final String uniqueFileName = fileUploadDirectory + request.getParameter("uniquefilename" + i);

				final PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName)
						.key(uniqueFileName).contentType(multiPartFile.getContentType())
						.contentLength(multiPartFile.getSize()).build();

				s3.putObject(putObjectRequest,
						RequestBody.fromInputStream(multiPartFile.getInputStream(), multiPartFile.getSize()));
				rtnStatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
			}
			}else {
				LOGGER.info("Check s3 connection");
				rtnStatus = Enumeration.ReturnStatus.FAILED.getreturnstatus();
			}

		} catch (Exception e) {
			LOGGER.error("Connection Failed: " + e);
			rtnStatus = Enumeration.ReturnStatus.FAILED.getreturnstatus();
		}
		return rtnStatus;
	}

	public Map<String, Object> fileUploadAndDownloadAWSStorage(final String pdfPath, final String outFileName,
			final String customFileName, final String changeWorkingDirectory, final String fileDownloadURL,
			final int ncontrolcode, final UserInfo userInfo) throws Exception {

		final Map<String, Object> mapObj = new HashMap<>();

		final String ssystemFileName = customFileName.isEmpty() ? outFileName : customFileName;
		final Path localFilePath = Paths.get(pdfPath + ssystemFileName);

		final String filePathToUpload = (changeWorkingDirectory.isEmpty() || changeWorkingDirectory == "")
				? ssystemFileName
				: (changeWorkingDirectory + "/" + ssystemFileName);

		final String homePath = getFileAbsolutePath();
		final String downloadsPath = getS3FileWritingPath();
		final Path downloadsDir = Paths.get(downloadsPath);

		final Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
		if(mapObjGet.get("rtn").equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
		final S3Client s3 = (S3Client) mapObjGet.get("s3");
		final String bucketName = (String) mapObjGet.get("bucketName");

		try {
			final PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName)
					.key(filePathToUpload).build();

			s3.putObject(putObjectRequest, RequestBody.fromFile(localFilePath));

			LOGGER.info("File uploaded from : " + filePathToUpload);
			LOGGER.info("File uploaded to S3: " + localFilePath);

			final Path localFilePathToDownload = downloadsDir.resolve(ssystemFileName);

			final GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName)
					.key(filePathToUpload).build();

			if (Files.exists(localFilePathToDownload)) {
				Files.delete(localFilePathToDownload);
			}

			s3.getObject(getObjectRequest, ResponseTransformer.toFile(localFilePathToDownload));
			final String downloadsPath1 = Enumeration.FTP.FILE_PATH_TO_UPLOAD.getFTP() + "/" + ssystemFileName;
			LOGGER.info("File downloaded from s3 to: " + localFilePathToDownload);

			mapObj.put("filepath", downloadsPath1);
			mapObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.SUCCESS.getreturnstatus());

		} catch (Exception e) {
			LOGGER.error("Failed : " + e.getMessage());
			mapObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.FAILED.getreturnstatus());
		}
		}else {
			LOGGER.info("Check s3 connection");
			mapObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), mapObjGet.get("rtn"));
		}
		return mapObj;
	}

	public String multiControlFileUploadAWSStorage(MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		final List<ControlMaster> controlCodeList = mapper.readValue(request.getParameter("controlcodelist"),
				new TypeReference<List<ControlMaster>>() {
				});

		String changeDirectory = "";

		Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
		if(mapObjGet.get("rtn").equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
		S3Client s3 = (S3Client) mapObjGet.get("s3");
		String bucketName = (String) mapObjGet.get("bucketName");
		String uniqueFileName = "";
		String rtnStatus = "";

		try {
			for (int j = 0; j < controlCodeList.size(); j++) {
				final int filecount = Integer
						.valueOf(request.getParameter(controlCodeList.get(j).getScontrolname() + "_filecount"));
				changeDirectory = controlCodeList.get(j).getSsubfoldername();

				for (int i = 0; i < filecount; i++) {
					MultipartFile objmultipart = request
							.getFile(controlCodeList.get(j).getScontrolname() + "_uploadedFile" + i);

					uniqueFileName = request
							.getParameter(controlCodeList.get(j).getScontrolname() + "_uniquefilename" + i);

					String fileUploadDir = changeDirectory + "/" + uniqueFileName;

					PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(fileUploadDir)
							.contentType(objmultipart.getContentType()).contentLength(objmultipart.getSize()).build();

					s3.putObject(putObjectRequest,
							RequestBody.fromInputStream(objmultipart.getInputStream(), objmultipart.getSize()));
				}
			}

			rtnStatus = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		} catch (Exception e) {
			LOGGER.info("File upload failed");
			rtnStatus = Enumeration.ReturnStatus.FAILED.getreturnstatus();
		}

		return rtnStatus;
		}else {
			LOGGER.info("Check s3 connection");
			return Enumeration.ReturnStatus.FAILED.getreturnstatus();
		}
	}

	public Map<String, Object> multiPathMultiFileDownloadAWSStorage(final Map<String, Object> fileMap,
			final List<ControlMaster> controlCodeList, final UserInfo objUserInfo, final String sCustomPath)
			throws Exception {

		final Map<String, Object> mapObjGet = getAWSClientBucket(objUserInfo);
		Map<String, Object> mapRtnObj = new HashMap<>();

		if(mapObjGet.get("rtn").equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
		final S3Client s3 = (S3Client) mapObjGet.get("s3");
		final String bucketName = (String) mapObjGet.get("bucketName");


		for (int j = 0; j < controlCodeList.size(); j++) {

			String changeWorkingDirectory = controlCodeList.get(j).getSsubfoldername();

			if (fileMap.get(controlCodeList.get(j).getScontrolname() + "_fileName") != null) {
				String filePath = (String) fileMap.get(controlCodeList.get(j).getScontrolname() + "_path");

				String sCustomName = (String) fileMap.get(controlCodeList.get(j).getScontrolname() + "_customName");
				final String ssystemfilename = (String) fileMap
						.get(controlCodeList.get(j).getScontrolname() + "_fileName");

				String sCompressfilename = ((sCustomName.isEmpty() || sCustomName == "") ? ssystemfilename
						: sCustomName);

				String finalFilePath = (sCustomPath.isEmpty() || sCustomPath == "") ? filePath : sCustomPath;

				final File folderCheck = new File(finalFilePath);

				if (!folderCheck.exists()) {
					final boolean created = folderCheck.mkdirs();
					if (created) {
						LOGGER.info("Folder created: " + folderCheck.getAbsolutePath());
					} else {
						LOGGER.info("Failed to create folder.");
					}
				}

				Path downloadsDir = Paths.get(finalFilePath);
				Path downloadPath = downloadsDir.resolve(sCompressfilename);

				if (Files.exists(downloadPath)) {
					Files.delete(downloadPath);
				}

				final String subFolderPath = (!changeWorkingDirectory.isEmpty()) ? (changeWorkingDirectory + "/") : "";

				try {

					final GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName)
							.key(subFolderPath + sCompressfilename).build();

					s3.getObject(getObjectRequest, downloadPath);

					mapRtnObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
					mapRtnObj.put(String.valueOf(controlCodeList.get(j).getNcontrolcode()), "true");
				} catch (Exception e) {
					LOGGER.info("Failed to download");
					mapRtnObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
							Enumeration.ReturnStatus.FAILED.getreturnstatus());
					mapRtnObj.put(String.valueOf(controlCodeList.get(j).getNcontrolcode()), "false");
				}

				mapRtnObj.put(controlCodeList.get(j).getScontrolname() + "_AttachFile", sCompressfilename);
				mapRtnObj.put(controlCodeList.get(j).getScontrolname() + "_FileName", sCompressfilename);
				mapRtnObj.put(controlCodeList.get(j).getScontrolname() + "_FilePath", filePath + sCompressfilename);
			}
		}
		}else {
			LOGGER.info("Check s3 connection");
			mapRtnObj.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), mapObjGet.get("rtn"));
		}
		return mapRtnObj;
	}

	public String multiPathDeleteFileAWSStorage(final Map<String, Object> fileMap, final List<ControlMaster> controlCodeList,
			UserInfo userInfo) throws Exception {
		String returnStr = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
		String changedirectory = "";

		final Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
		if(mapObjGet.get("rtn").equals(Enumeration.ReturnStatus.SUCCESS.getreturnstatus())) {
		final S3Client s3 = (S3Client) mapObjGet.get("s3");
		final String bucketName = (String) mapObjGet.get("bucketName");

		for (int j = 0; j < controlCodeList.size(); j++) {
			changedirectory = controlCodeList.get(j).getSsubfoldername() + "/";

			if (fileMap.get(controlCodeList.get(j).getScontrolname() + "_fileName") != null) {
				final String sfileName = (String) fileMap.get(controlCodeList.get(j).getScontrolname() + "_fileName");

				try {
					final DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName)
							.key(changedirectory + sfileName).build();
					s3.deleteObject(deleteObjectRequest);

					returnStr = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				} catch (Exception e) {
					LOGGER.info("File Deletion Error: " + e.getMessage());
				}

			}
		}
		}else {
			LOGGER.info("Check s3 connection");
			returnStr = Enumeration.ReturnStatus.FAILED.getreturnstatus();
		}
		return returnStr;
	}
	
	//BGSI-287	Committed by Vishakh for Different OS
	public String getS3FileWritingPath() throws Exception {
		LOGGER.info("getS3FileWritingPath ---->");
		String path = null;
		final String homePath = getFileAbsolutePath();
		
		if (IS_UNIX) {
			LOGGER.info("getS3FileWritingPath ----> UNIX");
			path = System.getenv(homePath)+File.separatorChar+Enumeration.FTP.UBUNTU_AWS_DOWNLOAD_PATH.getFTP();
		} else if (IS_WINDOWS) {
			LOGGER.info("getS3FileWritingPath ----> WINDOWS");
			path = System.getenv(homePath)+File.separatorChar+Enumeration.FTP.DOWNLOAD_PATH.getFTP();
		}
		LOGGER.info("Path :---------->" + path);
		return path;
	}
	
	
	//ALPDJ21-132-Added by suriaprahash(11/12/25)--Resolved S3 File override issue and ensured file copy sync with storage

	/**
	 * This method is used to make copy files in s3
	 * @param orgFile holds the list of orginal systemfilename already in s3
	 * @param dupFile holds the list of duplicate systemfilename to make copy in s3
	 * @param s3Folder holds the subfolder name
	 * @param userInfo [UserInfo] holding logged in user details based on  which the list is to be fetched
	 * @return a map with value with status message
	 * @throws Exception throws S3 Exception
	 */
	
	
	public Map<String, Object> copyFilesInS3(

			final List<String> orgFile, // existing names
			final List<String> dupFile, // new names
			final String s3Folder, // optional folder (prefix)
			final UserInfo userInfo // for config lookup

	) throws Exception {

		Map<String, Object> map = new HashMap<>();

		try {
			// 1. GET S3 CLIENT & BUCKET
			final Map<String, Object> mapObjGet = getAWSClientBucket(userInfo);
			final S3Client s3 = (S3Client) mapObjGet.get("s3");
			final String bucketName = (String) mapObjGet.get("bucketName");

			if (s3 == null || bucketName == null) {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), "IDS_CHECKS3CONNECTION");
				return map;
		}

			LOGGER.info("S3 bucket loaded: " + bucketName);

			/*
			 * // 2. VALIDATE LIST SIZE if (orgFile.size() != dupFile.size()) {
			 * map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
			 * "FILE_NAME_LIST_MISMATCH"); return map; }
			 */

			List<String> copiedFiles = new ArrayList<>();
			List<String> failedFiles = new ArrayList<>();

			// Normalize prefix
			String prefix = "";
			if (s3Folder != null && !s3Folder.trim().isEmpty() && !s3Folder.equals(",")) {
				prefix = s3Folder.endsWith("/") ? s3Folder : (s3Folder + "/");
				LOGGER.info("Using S3 folder prefix: " + prefix);
			}

			LOGGER.info("Starting S3 copy process...");

			// 3. LOOP THROUGH FILES
			for (int i = 0; i < orgFile.size(); i++) {

				String originalKey = prefix + orgFile.get(i);
				String duplicateKey = prefix + dupFile.get(i);

				LOGGER.info("Copying: " + originalKey + " ? " + duplicateKey);

				// --- Check if object exists ---
				try {
					s3.headObject(HeadObjectRequest.builder().bucket(bucketName).key(originalKey).build());
				} catch (Exception noObjEx) {
					LOGGER.info("NOT FOUND on S3: " + originalKey);
					failedFiles.add(originalKey + " (NOT FOUND)");
					continue;
				}

				// --- Perform S3-to-S3 copy ---
				try {
					/*CopyObjectRequest copyReq = CopyObjectRequest.builder().copySource(bucketName + "/" + originalKey)
							.destinationBucket(bucketName).destinationKey(duplicateKey).build();*/
					
					CopyObjectRequest copyReq = CopyObjectRequest.builder()
					        .sourceBucket(bucketName)      
					        .sourceKey(originalKey)        
					        .destinationBucket(bucketName)
					        .destinationKey(duplicateKey)
					        .build();


					s3.copyObject(copyReq);

					copiedFiles.add(originalKey + " ? " + duplicateKey);
					LOGGER.info("SUCCESS: Copied " + originalKey + " ? " + duplicateKey);

				} catch (Exception copyEx) {
					LOGGER.error("COPY FAILED: " + originalKey + " ? " + duplicateKey, copyEx);
					failedFiles.add(originalKey + " (COPY ERROR)");
				}
			}

			// 4. FINAL RESULT STATUS
			if (!copiedFiles.isEmpty()) {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.SUCCESS.getreturnstatus());
			} else {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						Enumeration.ReturnStatus.FAILED.getreturnstatus());
			}

			map.put("CopiedFiles", copiedFiles);
			map.put("FailedFiles", failedFiles);

		} catch (Exception ex) {
			ex.printStackTrace();
			map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					Enumeration.ReturnStatus.FAILED.getreturnstatus());
			// map.put("ErrorMessage", ex.getMessage());
		}

		return map;
	}
	//end ALPDJ21-132

	
}
