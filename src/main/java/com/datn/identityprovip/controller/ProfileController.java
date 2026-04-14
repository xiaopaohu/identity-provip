package com.datn.identityprovip.controller;

import com.datn.identityprovip.dto.request.AddressRequest;
import com.datn.identityprovip.dto.request.AdminUpdateProfileRequest;
import com.datn.identityprovip.dto.request.UpdateProfileRequest;
import com.datn.identityprovip.dto.response.AddressResponse;
import com.datn.identityprovip.dto.response.ApiResponse;
import com.datn.identityprovip.dto.response.MyProfileResponse;
import com.datn.identityprovip.dto.response.PublicProfileResponse;
import com.datn.identityprovip.service.AddressService;
import com.datn.identityprovip.service.ProfileService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/profiles") // Dùng chung prefix là profiles
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProfileController {

    ProfileService profileService;
    AddressService addressService;

    // ==========================================
    // SECTION 1: PERSONAL PROFILE (THÔNG TIN CÁ NHÂN)
    // ==========================================

    @GetMapping("/me")
    public ApiResponse<MyProfileResponse> getMyProfile() {
        return ApiResponse.<MyProfileResponse>builder()
                .result(profileService.getMyProfile())
                .build();
    }

    @PatchMapping("/me")
    public ApiResponse<MyProfileResponse> updateMyProfile(@RequestBody @Valid UpdateProfileRequest request) {
        return ApiResponse.<MyProfileResponse>builder()
                .result(profileService.updateMyProfile(request))
                .build();
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return ApiResponse.<String>builder()
                .result(profileService.uploadAvatar(file))
                .build();
    }

    // ==========================================
    // SECTION 2: ADDRESS MANAGEMENT (QUẢN LÝ ĐỊA CHỈ)
    // ==========================================

    @GetMapping("/me/addresses") // Thêm /me/addresses để phân biệt rõ với Profile
    public ApiResponse<List<AddressResponse>> getMyAddresses() {
        return ApiResponse.<List<AddressResponse>>builder()
                .result(addressService.getMyAddresses())
                .build();
    }

    @PostMapping("/me/addresses")
    public ApiResponse<AddressResponse> createAddress(@RequestBody @Valid AddressRequest request) {
        return ApiResponse.<AddressResponse>builder()
                .result(addressService.createAddress(request))
                .build();
    }

    @PatchMapping("/me/addresses/{id}")
    public ApiResponse<AddressResponse> updateAddress(
            @PathVariable UUID id,
            @RequestBody @Valid AddressRequest request) {
        return ApiResponse.<AddressResponse>builder()
                .result(addressService.updateAddress(id, request))
                .build();
    }

    @DeleteMapping("/me/addresses/{id}")
    public ApiResponse<Void> deleteAddress(@PathVariable UUID id) {
        addressService.deleteAddress(id);
        return ApiResponse.<Void>builder()
                .message("Address deleted successfully")
                .build();
    }

    @PatchMapping("/me/addresses/{id}/default")
    public ApiResponse<AddressResponse> setDefault(@PathVariable UUID id) {
        return ApiResponse.<AddressResponse>builder()
                .result(addressService.setDefaultAddress(id))
                .build();
    }

    // ==========================================
    // SECTION 3: ADMIN & PUBLIC (DÀNH CHO ADMIN HOẶC CÔNG KHAI)
    // ==========================================

    @GetMapping("/{id}")
    public ApiResponse<? extends PublicProfileResponse> getPublicProfile(@PathVariable UUID id) {
        return ApiResponse.<PublicProfileResponse>builder()
                .result(profileService.getProfile(id))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/internal")
    public ApiResponse<MyProfileResponse> updateProfileInternal(
            @PathVariable UUID id,
            @RequestBody AdminUpdateProfileRequest request) {
        return ApiResponse.<MyProfileResponse>builder()
                .result(profileService.updateProfileInternal(id, request))
                .build();
    }
}
