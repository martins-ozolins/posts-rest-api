package com.example.com.posts_rest_api.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table
@NoArgsConstructor
@Getter
@Setter
public class RefreshToken {

    @Id
    UUID id;

    Instant expiresAt;

    Boolean revoked = false;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    User owner;

}
