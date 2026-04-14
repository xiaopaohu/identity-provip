package com.datn.identityprovip.service.impl;

import com.datn.identityprovip.dto.request.AddressRequest;
import com.datn.identityprovip.dto.response.AddressResponse;
import com.datn.identityprovip.entity.*;
import com.datn.identityprovip.exception.AppException;
import com.datn.identityprovip.exception.ErrorCode;
import com.datn.identityprovip.mapper.AddressMapper;
import com.datn.identityprovip.repository.*;
import com.datn.identityprovip.service.AddressService;
import com.datn.identityprovip.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressServiceImpl implements AddressService {

    AddressRepository addressRepository;
    ProfileRepository profileRepository;
    AddressMapper addressMapper;

    ProvinceRepository provinceRepository;
    DistrictRepository districtRepository;
    WardRepository wardRepository;

    @Override
    @Transactional
    public AddressResponse createAddress(AddressRequest request) {

        UUID userId = SecurityUtils.getCurrentUserId();

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        Address address = addressMapper.toAddress(request);
        address.setProfile(profile);

        // --- LOOKUP TÊN TỪ DB NỘI BỘ ---
        enrichAddressFromLocalDB(address, request);

        // Logic xử lý default (giữ nguyên của bạn)
        long addressCount = addressRepository.countByProfileIdentityId(userId);
        if (addressCount == 0 || request.isDefaultAddress()) {
            handleUnsetDefault(userId);
            address.setDefaultAddress(true);
        }

        return addressMapper.toAddressResponse(addressRepository.save(address));
    }

    @Override
    public List<AddressResponse> getMyAddresses() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return addressRepository.findAllByProfileIdentityIdAndIsDeletedFalse(userId)
                .stream()
                .sorted((a, b) -> Boolean.compare(b.isDefaultAddress(), a.isDefaultAddress()))
                .map(addressMapper::toAddressResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(UUID addressId, AddressRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Address address = addressRepository.findByIdAndProfileIdentityId(addressId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        if (request.isDefaultAddress() && !address.isDefaultAddress()) {
            handleUnsetDefault(userId);
            address.setDefaultAddress(true);
        }
        addressMapper.updateAddressFromRequest(request, address);
        return addressMapper.toAddressResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(UUID addressId) {
        UUID userId = SecurityUtils.getCurrentUserId();

        // 1. Tìm địa chỉ cần xóa
        Address address = addressRepository.findByIdAndProfileIdentityId(addressId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        boolean wasDefault = address.isDefaultAddress();

        // 2. Thực hiện xóa (Dùng xóa hẳn cho sạch DB, hoặc xóa mềm tùy Hào)
        addressRepository.delete(address);
        // Nếu dùng Soft Delete thì: address.setDeleted(true); address.setDefault(false); addressRepository.save(address);

        // 3. Nếu cái vừa xóa là cái mặc định, phải tìm cái khác thay thế ngay
        if (wasDefault) {
            // Tìm cái địa chỉ còn lại (đã trừ cái vừa xóa) và sắp xếp theo ngày tạo mới nhất
            addressRepository.findAllByProfileIdentityIdAndIsDeletedFalse(userId)
                    .stream()
                    .filter(a -> !a.getId().equals(addressId)) // Chắc chắn không lấy lại cái vừa xóa
                    .max(Comparator.comparing(Address::getCreatedAt)) // Ưu tiên cái mới tạo gần đây nhất
                    .ifPresent(another -> {
                        another.setDefaultAddress(true);
                        addressRepository.save(another);
                    });
        }
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(UUID addressId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Address address = addressRepository.findByIdAndProfileIdentityId(addressId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        if (address.isDefaultAddress()) {
            throw new AppException(ErrorCode.ADDRESS_ALREADY_DEFAULT);
        }

        handleUnsetDefault(userId);
        address.setDefaultAddress(true);
        return addressMapper.toAddressResponse(addressRepository.save(address));
    }

    private void handleUnsetDefault(UUID profileId) {
        addressRepository.unsetDefaultByProfileId(profileId);
    }

    private void enrichAddressFromLocalDB(Address address, AddressRequest request) {
        Province p = provinceRepository.findById(request.getProvinceCode())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_PROVINCE));
        District d = districtRepository.findById(request.getDistrictCode())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_DISTRICT));
        Ward w = wardRepository.findById(request.getWardCode())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_WARD));

        address.setProvinceName(p.getName());
        address.setDistrictName(d.getName());
        address.setWardName(w.getName());
    }
}
