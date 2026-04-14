package com.datn.identityprovip.mapper;

import com.datn.identityprovip.dto.request.AddressRequest;
import com.datn.identityprovip.dto.response.AddressResponse;
import com.datn.identityprovip.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AddressMapper {

    // Chuyển Request thành Entity (dùng cho POST)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Address toAddress(AddressRequest request);

    @Mapping(target = "completeAddress", expression = "java(buildCompleteAddress(address))")
    AddressResponse toAddressResponse(Address address);

    // Logic PATCH địa chỉ
    void updateAddressFromRequest(AddressRequest request, @MappingTarget Address address);

    // Helper method để nối chuỗi địa chỉ
    default String buildCompleteAddress(Address address) {
        if (address == null) return "";
        return String.format("%s, %s, %s, %s",
                address.getDetailAddress(),
                address.getWardName(),
                address.getDistrictName(),
                address.getProvinceName());
    }
}
