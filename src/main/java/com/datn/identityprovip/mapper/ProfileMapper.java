package com.datn.identityprovip.mapper;

import com.datn.identityprovip.dto.response.MyProfileResponse;
import com.datn.identityprovip.dto.request.UpdateProfileRequest;
import com.datn.identityprovip.dto.response.PublicProfileResponse;
import com.datn.identityprovip.entity.Profile;
import com.datn.identityprovip.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProfileMapper {
    MyProfileResponse toMyProfileResponse(Profile profile);
    PublicProfileResponse toPublicProfileResponse(Profile profile);

    void updateProfile(UpdateProfileRequest request, @MappingTarget Profile profile);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "identityId", ignore = true)
    @Mapping(target = "fullName", constant = "Người dùng mới")
    @Mapping(target = "gender", constant = "UNKNOWN")
    @Mapping(target = "rankPoint", constant = "0")
    @Mapping(target = "membershipLevel", constant = "BRONZE")
    @Mapping(target = "nickname", expression = "java(\"User_\" + user.getId().toString().substring(0, 5))")
    @Mapping(target = "avatarUrl", expression = "java(\"https://api.dicebear.com/7.x/miniavs/svg?seed=\" + user.getEmail())")
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Profile toDefaultProfile(User user);
}
