package com.fyp.health_sync.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fyp.health_sync.enums.AuthType;
import com.fyp.health_sync.enums.UserRole;
import com.fyp.health_sync.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;

import java.sql.Blob;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Data
@AllArgsConstructor(staticName = "build")
@NoArgsConstructor
@Entity
@Builder
@Table(name="users")
public class Users {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;
    private String name;
    private String email;
    @JsonIgnore
    private String password;
    @Lob
    private byte[] profilePicture;
    private LocalDateTime createdAt;
    private Boolean isVerified;
    @Enumerated (EnumType.STRING)
    private UserStatus status;
    @Enumerated (EnumType.STRING)
    private UserRole role;
    @Enumerated (EnumType.STRING)
    private AuthType authType;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Collection<? extends GrantedAuthority> getRoles() {
        return new ArrayList<>() {{
            add(new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return role.name();
                }
            });
        }};
    }
}
