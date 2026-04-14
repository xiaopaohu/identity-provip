package com.datn.identityprovip.service.impl;

import com.datn.identityprovip.dto.event.UserActivatedEvent;
import com.datn.identityprovip.dto.request.AdminUpdateProfileRequest;
import com.datn.identityprovip.dto.request.UpdateProfileRequest;
import com.datn.identityprovip.dto.response.MyProfileResponse;
import com.datn.identityprovip.dto.response.PublicProfileResponse;
import com.datn.identityprovip.entity.Profile;
import com.datn.identityprovip.entity.User;
import com.datn.identityprovip.exception.AppException;
import com.datn.identityprovip.exception.ErrorCode;
import com.datn.identityprovip.mapper.ProfileMapper;
import com.datn.identityprovip.repository.ProfileRepository;
import com.datn.identityprovip.service.CloudinaryService;
import com.datn.identityprovip.service.ProfileService;
import com.datn.identityprovip.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileServiceImpl implements ProfileService {

    ProfileRepository profileRepository;
    ProfileMapper profileMapper;
    ApplicationEventPublisher eventPublisher;
    CloudinaryService cloudinary;

    @Override
    @EventListener
    @Transactional
    public void handleUserActivatedEvent(UserActivatedEvent event){
        User user = event.getUser();
        log.info("Bắt đầu khởi tạo profile cho User ID: {}", user.getId());
        if (profileRepository.existsById(user.getId())) {
            return;
        }
        Profile profile = profileMapper.toDefaultProfile(user);
        profile.setUser(user);
        profileRepository.saveAndFlush(profile);
    }

    @Override
    public MyProfileResponse getMyProfile() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return (MyProfileResponse) getProfile(userId);
    }

    @Override
    public PublicProfileResponse getProfile(UUID targetId) {
        Profile profile = profileRepository.findById(targetId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (targetId.equals(currentUserId)) {
            return profileMapper.toMyProfileResponse(profile);
        }
        return profileMapper.toPublicProfileResponse(profile);
    }

    @Override
    @Transactional
    public MyProfileResponse updateMyProfile(UpdateProfileRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));


        if (request.getNickname() != null && !request.getNickname().equals(profile.getNickname())) {
            if (profileRepository.existsByNickname(request.getNickname())) {
                throw new AppException(ErrorCode.NICKNAME_EXISTED);
            }
        }
        profileMapper.updateProfile(request, profile);

        return profileMapper.toMyProfileResponse(profileRepository.save(profile));
    }

    @Override
    @Transactional
    public MyProfileResponse updateProfileInternal(UUID targetId, AdminUpdateProfileRequest request) {
        Profile profile = profileRepository.findById(targetId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        if (request.getRankPoint() != null) {
            profile.setRankPoint(request.getRankPoint());
        }
        if (request.getMembershipLevel() != null) {
            profile.setMembershipLevel(request.getMembershipLevel());
        }

        log.info("Admin updated profile for user {}: RankPoint={}, Level={}",
                targetId, request.getRankPoint(), request.getMembershipLevel());

        return profileMapper.toMyProfileResponse(profileRepository.save(profile));
    }

    @Override
    @Transactional
    public String uploadAvatar(MultipartFile file) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        String url = cloudinary.uploadImage(file);
        profile.setAvatarUrl(url);
        profileRepository.save(profile);

        return url;
    }
}
