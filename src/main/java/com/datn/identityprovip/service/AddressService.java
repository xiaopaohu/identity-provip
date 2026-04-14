package com.datn.identityprovip.service;

import com.datn.identityprovip.dto.request.AddressRequest;
import com.datn.identityprovip.dto.response.AddressResponse;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    AddressResponse createAddress(AddressRequest request);
    List<AddressResponse> getMyAddresses();
    AddressResponse updateAddress(UUID addressId, AddressRequest request);
    void deleteAddress(UUID addressId);
    AddressResponse setDefaultAddress(UUID addressId);
}
