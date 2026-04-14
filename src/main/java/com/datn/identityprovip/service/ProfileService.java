package com.datn.identityprovip.service;

import com.datn.identityprovip.dto.event.UserActivatedEvent;
import com.datn.identityprovip.dto.request.AdminUpdateProfileRequest;
import com.datn.identityprovip.dto.request.UpdateProfileRequest;
import com.datn.identityprovip.dto.response.MyProfileResponse;
import com.datn.identityprovip.dto.response.PublicProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ProfileService {
    void handleUserActivatedEvent(UserActivatedEvent event);
    MyProfileResponse getMyProfile();
    PublicProfileResponse getProfile(UUID targetId);

    MyProfileResponse updateMyProfile(UpdateProfileRequest request);

    MyProfileResponse updateProfileInternal(UUID targetId, AdminUpdateProfileRequest request);

    String uploadAvatar(MultipartFile file);
}
