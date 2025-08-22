package org.example.be17pickcook.domain.common.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.be17pickcook.domain.refrigerator.model.RefrigeratorItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 식재료 카테고리 엔티티 (간소화 버전)
 * - 냉장고 아이템과 상품(Product)에서 공통으로 사용
 * - 고정된 10개 카테고리 데이터 (ID와 이름만)
 */
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    /** 카테고리명: 고유값, 공백 금지 */
    @NotBlank(message = "카테고리명은 필수입니다.")
    @Size(max = 50, message = "카테고리명은 50자 이하여야 합니다.")
    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    // =================================================================
    // 연관관계 매핑 (양방향 - 선택사항)
    // =================================================================

    /** 이 카테고리에 속한 냉장고 아이템들 */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<RefrigeratorItem> refrigeratorItems = new ArrayList<>();

    // =================================================================
    // 정적 팩토리 메서드 (초기 데이터 생성용)
    // =================================================================

    /**
     * 초기 카테고리 데이터 생성을 위한 팩토리 메서드
     */
    public static Category createDefault(String name) {
        return Category.builder()
                .name(name)
                .build();
    }
}