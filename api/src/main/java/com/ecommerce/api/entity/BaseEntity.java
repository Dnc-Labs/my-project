package com.ecommerce.api.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Base class chứa 4 audit field dùng chung cho mọi entity.
 *
 * @MappedSuperclass: KHÔNG tạo bảng riêng — field copy xuống bảng của entity con.
 * @EntityListeners(AuditingEntityListener.class): hook AuditingEntityListener vào
 * lifecycle entity (@PrePersist/@PreUpdate) để tự điền timestamp + auditor.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    @Version
    private Long version;
}
