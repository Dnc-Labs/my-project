package com.ecommerce.api.entity;

import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import com.ecommerce.api.enums.Role;
import com.ecommerce.api.enums.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role = Role.CUSTOMER;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String address;


    // - orders: 1 User có nhiều Order (@OneToMany)
    // - cart: 1 User có 1 Cart (@OneToOne)
    @OneToMany(mappedBy = "user")
    private List<Order> orderList;

    @OneToOne(mappedBy = "user")
    private Cart cart;
}
