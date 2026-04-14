package com.datn.identityprovip.repository;

import com.datn.identityprovip.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    // 1. Lấy danh sách địa chỉ chưa xóa (Dùng IdentityId cho đúng field @Id của Profile)
    List<Address> findAllByProfileIdentityIdAndIsDeletedFalse(UUID identityId);

    // 2. Tìm 1 địa chỉ để bảo mật
    Optional<Address> findByIdAndProfileIdentityId(UUID addressId, UUID identityId);

    // 3. Đếm số lượng để check cái đầu tiên (Sửa từ ByProfileId thành ByProfileIdentityId)
    long countByProfileIdentityId(UUID identityId);

    // 4. Query update mặc định (Sửa a.profile.id thành a.profile.identityId)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Address a SET a.defaultAddress = false WHERE a.profile.identityId = :identityId")
    void unsetDefaultByProfileId(UUID identityId);
}
