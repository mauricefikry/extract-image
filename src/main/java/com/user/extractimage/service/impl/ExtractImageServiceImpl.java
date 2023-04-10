package com.user.extractimage.service.impl;

import com.user.extractimage.service.ExtractImageService;
import com.user.extractimage.service.integration.GoogleDriveService;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
@Slf4j
public class ExtractImageServiceImpl implements ExtractImageService {

  private static final String FOLDER = "Extract_Image";
  private static final String ENGLISH_LANGUAGE = "eng";
  private static final String CHINESE_LANGUAGE = "chi_sim";
  private static final String ALPHABET_WORD = "o";


  private final GoogleDriveService googleDriveService;

  public ExtractImageServiceImpl(GoogleDriveService googleDriveService) {
    this.googleDriveService = googleDriveService;
  }

  @Override
  public String extractEnglishImage(MultipartFile image) throws IOException {

    log.info("Extract English Image");

    log.info("## Start Upload Google Drive English Image");
    googleDriveService.uploadImage(FOLDER, image);
    log.info("## Finish Upload Google Drive English Image");

    storeFile(image);
    String text = ocrImage(ENGLISH_LANGUAGE, image.getOriginalFilename());

    StringBuilder result = changeColorEnglishText(text);
    return result.toString();
  }

  @Override
  public String extractChineseImage(MultipartFile image) throws IOException {

    log.info("Extract Chinese Image");

    log.info("## Start Upload Google Drive English Image");
    googleDriveService.uploadImage(FOLDER, image);
    log.info("## Finish Upload Google Drive English Image");

    storeFile(image);
    String text = ocrImage(CHINESE_LANGUAGE, image.getOriginalFilename());

    StringBuilder result = changeColorEnglishText(text);
    return result.toString();
  }


  public void storeFile(MultipartFile file) throws IOException {
    File fileImage = new File("image", Objects.requireNonNull(file.getOriginalFilename()));
    FileUtils.writeByteArrayToFile(fileImage, file.getBytes());
  }

  public String ocrImage(String language, String imageName) {

    log.info("## OCR Image "+language);
    ITesseract iTesseract = new Tesseract();
    iTesseract.setDatapath("src/main/resources/extract-data");
    iTesseract.setLanguage(language);
    String text = "";
    try {
      text = iTesseract.doOCR(new File("image/"+imageName));
    } catch (TesseractException e) {
      log.error(e.getMessage());
      return "Error while reading image";
    }

    return text;
  }

  public StringBuilder changeColorEnglishText(String text) {

    String[] textArr = text.split(" ");
    StringBuilder resultData = new StringBuilder();
    resultData.append("<html>").append("<body>");

    for (String str : textArr) {
      if (!str.toLowerCase().contains(ALPHABET_WORD)) {
        resultData
            .append("<div>")
            .append(str)
            .append(" ")
            .append("</div>");
      } else {
        resultData
            .append("<div>")
            .append("<p style='color:blue>")
            .append(str)
            .append("</p>")
            .append("</div>");
      }
    }
    resultData.append("</body>").append("<html>");

    return resultData;
}


}
