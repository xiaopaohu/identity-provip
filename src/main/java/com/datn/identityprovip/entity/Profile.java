package com.datn.identityprovip.entity;

import com.datn.identityprovip.entity.Address;
import com.datn.identityprovip.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Profile {
    @Id
    UUID identityId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "identity_id")
    User user;

    @NotBlank(message = "FULL_NAME_REQUIRED")
    @Column(name = "full_name")
    String fullName;

    @Column(name = "nickname", unique = true)
    String nickname;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    Gender gender = Gender.UNKNOWN;

    @Past(message = "DOB_INVALID")
    @Column(name = "dob")
    LocalDate dob;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    String avatarUrl;

    @Builder.Default
    @Column(name = "rank_point")
    Integer rankPoint = 0;

    @Builder.Default
    @Column(name = "membership_level")
    String membershipLevel = "BRONZE";

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    Set<Address> addresses = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;

    public void addAddress(Address address) {
        if (addresses == null) {
            addresses = new HashSet<>();
        }
        addresses.add(address);
        address.setProfile(this);

        if (addresses.size() == 1) {
            address.setDefaultAddress(true);
        }
    }
}
