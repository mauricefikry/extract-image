package com.user.extractimage.controller;

import com.user.extractimage.controller.constant.ApiPath;
import com.user.extractimage.service.ExtractImageService;
import java.io.FileNotFoundException;
import java.io.IOException;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(ApiPath.BASE_PATH)
public class ExtractImageController {

  private final ExtractImageService extractImageService;

  public ExtractImageController(ExtractImageService extractImageService) {
    this.extractImageService = extractImageService;
  }

  @PostMapping(
      value = ApiPath.ENGLISH_IMAGE,
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE}
  )
  public String extractImageEnglish(
      @RequestPart(value = "file") MultipartFile file
  ) throws TesseractException, IOException {
    return extractImageService.extractEnglishImage(file);
  }

  @PostMapping(
      value = ApiPath.CHINESE_IMAGE,
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE}
  )
  public String extractImageChinese(
      @RequestPart(value = "file") MultipartFile file
  ) throws IOException, TesseractException {

    extractImageService.extractChineseImage(file);
    return "Extract Chinese Image Success";
  }

}
