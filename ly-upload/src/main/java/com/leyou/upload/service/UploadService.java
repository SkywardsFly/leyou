package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyExcetion;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {

//    private static final List<String> SUFFIXES = Arrays.asList("image/png", "image/jpeg","image/bmp");

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private UploadProperties properties;

    /**
     *
     * @param file
     * @return http://image.leyou.com/group1/M00/00/00/wKg4ZVro5eCAZEMVABfYcN8vzII630.png
     */
    public String upload(MultipartFile file) {
        try {
            //校验文件类型
            String contentType = file.getContentType();
            if (!properties.getAllowTypes().contains(contentType)){
                throw new LyExcetion(ExceptionEnum.INVALID_FILE_TYPE);
            }

            //校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null){
                throw new LyExcetion(ExceptionEnum.INVALID_FILE_TYPE);
            }
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(),".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);

            //http://image.leyou.com/  +  group1/M00/00/00/wKg4ZVro5eCAZEMVABfYcN8vzII630.png
            return  properties.getBaseUrl() + storePath.getFullPath();
        } catch (IOException e) {
            log.error("[文件上传] 上传文件失败!",e);
            throw new LyExcetion(ExceptionEnum.UPLOAD_BAD_FILE);
        }


    }
}
