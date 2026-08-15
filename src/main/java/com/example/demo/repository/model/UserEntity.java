package com.example.demo.repository.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

  @Id private UUID id;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(nullable = false)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRoleEntity role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserStatusEntity status;

  private String std;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "promotion_id")
  private PromotionEntity promotion;
}
