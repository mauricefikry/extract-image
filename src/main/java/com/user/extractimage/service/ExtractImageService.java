package com.user.extractimage.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.web.multipart.MultipartFile;

public interface ExtractImageService {

  String extractEnglishImage(MultipartFile image) throws IOException, TesseractException;
  String extractChineseImage(MultipartFile image) throws IOException;


}
