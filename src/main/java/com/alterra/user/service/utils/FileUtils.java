package com.alterra.user.service.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUtils {
    public Boolean checkFileExtension(String fileName) {
        if(fileName != null && fileName.contains(".")){
            String[] extensionList = {".png", ".jpeg", ".jpg"};

            for(String extension: extensionList) {
                if (fileName.endsWith(extension)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean checkImageSize(MultipartFile file){
        if(file.getSize() > 5000000) return false;
        return true;
    }
}
