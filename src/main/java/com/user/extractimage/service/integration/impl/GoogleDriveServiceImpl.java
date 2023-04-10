package com.user.extractimage.service.integration.impl;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.user.extractimage.service.integration.GoogleDriveService;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.api.services.drive.model.File;


@Service
@Slf4j
public class GoogleDriveServiceImpl implements GoogleDriveService {

  private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String TOKENS_DIRECTORY_PATH = "tokens";
  private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

  private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
    InputStream in = GoogleDriveServiceImpl.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline")
        .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setHost("127.0.0.1").setPort(8089).build();

    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }

  @Override
  public Drive getInstance() throws GeneralSecurityException, IOException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
        .setApplicationName(APPLICATION_NAME)
        .build();
  }
  private String searchFolderId(String parentId, String folderName, Drive service) throws Exception {
    String folderId = null;
    String pageToken = null;
    FileList result = null;
    File fileMetadata = new File();
    fileMetadata.setMimeType("application/vnd.google-apps.folder");
    fileMetadata.setName(folderName);
    do {
      String query = " mimeType = 'application/vnd.google-apps.folder' ";
      if (parentId == null) {
        query = query + " and 'root' in parents";
      } else {
        query = query + " and '" + parentId + "' in parents";
      }
      result = service.files().list().setQ(query)
          .setSpaces("drive")
          .setFields("nextPageToken, files(id, name)")
          .setPageToken(pageToken)
          .execute();
      for (File file : result.getFiles()) {
        if (file.getName().equalsIgnoreCase(folderName)) {
          folderId = file.getId();
        }
      }
      pageToken = result.getNextPageToken();
    } while (pageToken != null && folderId == null);
    return folderId;
  }

  private String findOrCreateFolder(String parentId, String folderName, Drive driveInstance) throws Exception {
    String folderId = searchFolderId(parentId, folderName, driveInstance);
    if (folderId != null) {
      return folderId;
    }
    File fileMetadata = new File();
    fileMetadata.setMimeType("application/vnd.google-apps.folder");
    fileMetadata.setName(folderName);

    if (parentId != null) {
      fileMetadata.setParents(Collections.singletonList(parentId));
    }
    return driveInstance.files().create(fileMetadata)
        .setFields("id")
        .execute()
        .getId();
  }

  public String getFolderId(String path) throws Exception {
    String parentId = null;
    String[] folderNames = path.split("/");
    Drive driveInstance = getInstance();
    for (String name : folderNames) {
      parentId = findOrCreateFolder(parentId, name, driveInstance);
    }
    return parentId;
  }

  @Override
  public String uploadImage(String path, MultipartFile fileImage) {
    try {
      String folderId = getFolderId(path);
      if (null != fileImage) {
        File fileMetadata = new File();
        fileMetadata.setParents(Collections.singletonList(folderId));
        fileMetadata.setName(fileImage.getOriginalFilename());
        File uploadFile = getInstance()
            .files()
            .create(fileMetadata, new InputStreamContent(
                fileImage.getContentType(),
                new ByteArrayInputStream(fileImage.getBytes()))
            )
            .setFields("id").execute();
        return uploadFile.getId();
      }
    } catch (Exception e) {
      log.error("Error: ", e);
    }
    return "Success Upload File";
  }


}
