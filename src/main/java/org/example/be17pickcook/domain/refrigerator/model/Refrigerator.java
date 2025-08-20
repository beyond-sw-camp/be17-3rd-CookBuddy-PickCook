package org.example.be17pickcook.domain.refrigerator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.be17pickcook.domain.user.model.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "refrigerators",
        uniqueConstraints = {
                // 한 유저 안에서 냉장고 이름 중복 방지 (선택)
                @UniqueConstraint(name = "uk_refrigerators_user_name", columnNames = {"user_id", "name"})
        }
)
public class Refrigerator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refrigerator_id")
    private Long id; // PK: 자동 생성 → 별도 검증 불필요

    /** 소유자: null 금지(참조 무결성) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "냉장고 소유자 정보는 필수입니다.")
    private User user;

    /** 냉장고 이름: 공백 금지 + 길이 제한 */
    @NotBlank(message = "냉장고 이름은 필수입니다.")
    @Size(max = 100, message = "냉장고 이름은 100자 이하여야 합니다.")
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    /** 생성시각: 자동 기록 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // (선택) 부모-자식 매핑: 냉장고 삭제 시 아이템 자동 제거하고 싶으면 cascade/orphanRemoval 설정
    @OneToMany(mappedBy = "refrigerator", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RefrigeratorItem> items = new ArrayList<>();
}
