package com.datn.identityprovip.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.datn.identityprovip.exception.AppException;
import com.datn.identityprovip.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CloudinaryService {
    Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        //Kiểm tra định dạng ảnh cơ bản
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("Người dùng cố gắng upload file không phải định dạng ảnh: {}", contentType);
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        try {
            // resource_type: "auto" giúp Cloudinary tự nhận diện (ảnh, video, raw)
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "identity_avatars",
                    "resource_type", "auto",
                    "access_mode", "public"
            ));

            log.info("Upload thành công lên Cloudinary: {}", uploadResult.get("secure_url"));
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            log.error("Cloudinary upload error - Message: {}", e.getMessage());
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }
}