package com.user.extractimage.service.integration;

import com.google.api.services.drive.Drive;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.springframework.web.multipart.MultipartFile;

public interface GoogleDriveService {

  Drive getInstance() throws GeneralSecurityException, IOException;

  String uploadImage(String path, MultipartFile fileImage);

}
