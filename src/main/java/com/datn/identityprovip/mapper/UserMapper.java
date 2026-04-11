package com.datn.identityprovip.mapper;

import com.datn.identityprovip.dto.request.AdminCreateUserRequest;
import com.datn.identityprovip.dto.request.AdminUserUpdateRequest;
import com.datn.identityprovip.dto.request.RegisterRequest;
import com.datn.identityprovip.dto.response.AdminUserResponse;
import com.datn.identityprovip.dto.response.UserResponse;
import com.datn.identityprovip.entity.User;
import com.datn.identityprovip.entity.UserRole;
import org.mapstruct.*;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;



@Mapper(componentModel = "spring", imports = {UUID.class, Collections.class, Collectors.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "phone", ignore = true)
    User toUser(RegisterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    User toUser(AdminCreateUserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    void updateUser(@MappingTarget User user, AdminUserUpdateRequest request);

//    @Mapping(target = "roles", expression = "java(user.getUserRoles().stream().map(ur -> ur.getRole().getName()).collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "roles", expression = "java(mapUserRolesToNames(user.getUserRoles()))")
    UserResponse toUserResponse(User user);

    default Set<String> mapUserRolesToNames(Set<UserRole> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) return null;
        return userRoles.stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toSet());
    }

    @Mapping(target = "roles", expression = "java(mapUserRolesToNames(user.getUserRoles()))")
//    @Mapping(target = "mfaVerified", source = "twoFactorEnabled")
    AdminUserResponse toAdminUserResponse(User user);

}


