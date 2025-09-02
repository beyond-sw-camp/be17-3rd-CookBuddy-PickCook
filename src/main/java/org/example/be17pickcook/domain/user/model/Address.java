// 📁 org.example.be17pickcook.domain.user.model.Address.java

package org.example.be17pickcook.domain.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 배송지 엔티티
 * - 사용자별 배송지 정보 관리
 * - 기본배송지 설정 지원
 * - 소프트 삭제 없이 단순 구조
 */
@Entity
@Table(
        name = "addresses",
        indexes = {
                @Index(name = "idx_user_default", columnList = "user_id, is_default"),
                @Index(name = "idx_user_created", columnList = "user_id, created_at")
        }
)
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    // =================================================================
    // 기본 필드
    // =================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    // =================================================================
    // 연관관계 필드
    // =================================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // =================================================================
    // 주소 정보 필드
    // =================================================================

    @Column(name = "postal_code", length = 10, nullable = false)
    @NotBlank(message = "우편번호는 필수입니다.")
    private String postalCode;

    @Column(name = "road_address", length = 200, nullable = false)
    @NotBlank(message = "도로명주소는 필수입니다.")
    private String roadAddress;

    @Column(name = "detail_address", length = 100, nullable = false)
    @NotBlank(message = "상세주소는 필수입니다.")
    private String detailAddress;

    // =================================================================
    // 기본배송지 관리 필드
    // =================================================================

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    // =================================================================
    // 시간 정보 필드
    // =================================================================

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // =================================================================
    // 비즈니스 로직 메서드
    // =================================================================

    /**
     * 기본배송지로 설정
     */
    public void setAsDefault() {
        this.isDefault = true;
    }

    /**
     * 일반배송지로 변경
     */
    public void removeDefault() {
        this.isDefault = false;
    }

    /**
     * 주소 정보 업데이트
     */
    public void updateAddress(String postalCode, String roadAddress, String detailAddress) {
        if (postalCode != null && !postalCode.isBlank()) {
            this.postalCode = postalCode;
        }
        if (roadAddress != null && !roadAddress.isBlank()) {
            this.roadAddress = roadAddress;
        }
        if (detailAddress != null && !detailAddress.isBlank()) {
            this.detailAddress = detailAddress;
        }
    }

    /**
     * 전체 주소 문자열 반환
     */
    public String getFullAddress() {
        return String.format("(%s) %s %s", postalCode, roadAddress, detailAddress);
    }
}